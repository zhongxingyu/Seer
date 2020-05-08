 package ecologylab.concurrent;
 
 import java.util.*;
 import java.nio.channels.ClosedByInterruptException;
 
 import ecologylab.appframework.Memory;
 import ecologylab.appframework.OutOfMemoryErrorHandler;
 import ecologylab.appframework.StatusReporter;
 import ecologylab.collections.FloatWeightSet;
 import ecologylab.generic.Debug;
 import ecologylab.generic.DispatchTarget;
 import ecologylab.generic.Generic;
 import ecologylab.generic.MathTools;
 import ecologylab.generic.NewPorterStemmer;
 import ecologylab.io.BasicSite;
 import ecologylab.io.DownloadProcessor;
 import ecologylab.io.Downloadable;
 
 /**
  * Non-linear flow multiplexer. Tracks downloads of <code>Downloadable</code> objects. Dispatches
  * downloaded media to the appropriate <code>DispatchTarget</code>.
  * <p>
  * Looks out for timeout conditions. In case they happen, records state in the <code>bad</code>
  * slot, and dispatches, as well.
  */
 public class DownloadMonitor<T extends Downloadable> extends Monitor implements
 		DownloadProcessor<T>
 {
 	private static final int	TWELVE_HOURS_IN_MILLIS	= 1000*60*60*12;
 
 	static HashMap<Thread, NewPorterStemmer>	stemmersHash					= new HashMap<Thread, NewPorterStemmer>();
 
 	// ////////////////// queues for media that gets downloaded /////////////////
 	/**
 	 * This is the queue of DownloadClosures waiting to be downloaded.
 	 */
 	private Vector<DownloadClosure>						toDownload						= new Vector<DownloadClosure>(30);
 
 	static final int	NO_SLEEP							= 0;
 	static final int 	REGULAR_SLEEP					= 400;
 	static final int	SHORT_SLEEP						= 100;
 	static final int	LOW_MEMORY_SLEEP			= 3000;
 	static final int	LOW_MEMORY_THRESHOLD	= 2 * Memory.DANGER_THRESHOLD;
 
 	int									dispatched;
 	private int				pending;
 	private boolean		paused;
 
 	/**
 	 * Settable variable reduces sleep between downloads to speed up collecting (SHORT_SLEEP).
 	 * 
 	 */
 	private boolean				hurry;
 	private boolean				dontWait;
 	private Thread[]				downloadThreads;
 	private int[]					priorities;
 	private int						numDownloadThreads;
 	private String					name;
 	private StatusReporter	status;
 
 	// ////////////////// queues for media that gets downloaded /////////////////
 
 	protected boolean							finished;
 	private static ThreadGroup		THREAD_GROUP	= new ThreadGroup("DownloadMonitor");
 
 	private int									lowPriority;
 	private int									midPriority;
 	private int									highPriority;
 
 	private final int						highThreshold;
 	private final int						midThreshold;
 
 	public static final int		HIGHER_PRIORITY				= 4;
 	public static final int		HIGH_PRIORITY					= 3;
 	public static final int		MID_PRIORITY					= 2;
 	public static final int		LOW_PRIORITY					= 1;
 	public static final int		MAX_WAIT_TIME					= 1000;
 	private static final int		MAX_WAIT_WHEN_PAUSED	= 5000;
 
 
 	private final Object					TOO_MANY_PENDING_LOCK	= new Object();
 
 	public DownloadMonitor(String name, int numDownloadThreads)
 	{
 		this(name, numDownloadThreads, 0);
 	}
 
 	public DownloadMonitor(String name, int numDownloadThreads, int priorityBoost)
 	{
 		this.numDownloadThreads = numDownloadThreads;
 		this.name = name;
 
 		highThreshold = numDownloadThreads * 2;
 		midThreshold = numDownloadThreads + 1;
 		finished = false;
 
 		lowPriority = LOW_PRIORITY + priorityBoost;
 		midPriority = MID_PRIORITY + priorityBoost;
 		highPriority = HIGH_PRIORITY + priorityBoost;
 	}
 
 	// ----------------------- perform downloads ------------------------------//
 	/**
 	 * Entry point for <code>Downloadable</code>s that want to be downloaded by 1 of our
 	 * performDownload() threads. Starts the performDownload() threads, if necessary.
 	 * <p/>
 	 * After performDownload() is called on the Downloadable, then in the normal case, downloadDone()
 	 * is called, and then the DispatchTarget is called. In the error case, handleIOError() or
 	 * handleTimeout() is called.
 	 */
 	public void download(T thatDownloadable, DispatchTarget<T> dispatchTarget)
 	{
 		synchronized (toDownload)
 		{
 			debug("\n download("+thatDownloadable.purl() + ")");
 			toDownload.add(new DownloadClosure<T>(thatDownloadable, dispatchTarget, this));
 			if (downloadThreads == null)
 				startPerformDownloadsThreads();
 			else
 				toDownload.notify();
 		}
 	}
 
 	/**
 	 * Cancel a download that has been queued, but not yet started.
 	 * 
 	 * @param thatDownloadable
 	 */
 	public void cancelDownload(Downloadable thatDownloadable)
 	{
 		synchronized (toDownload)
 		{
 			for (DownloadClosure dc : toDownload)
 			{
 				if (dc.downloadable.equals(thatDownloadable))
 				{
 					toDownload.remove(dc);
 					break;
 				}
 			}
 		}
 	}
 
 	/**
 	 * Set the priority of the download thread. The more backed up we are, the higher the priority.
 	 * 
 	 * @param t
 	 * @return
 	 */
 	private int setDownloadPriority()
 	{
 		return setDownloadPriority(Thread.currentThread());
 	}
 
 	/**
 	 * Set the priority of the download thread. The more backed up we are, the higher the priority.
 	 * 
 	 * @param t
 	 * @return
 	 */
 	private int setDownloadPriority(Thread t)
 	{
 		int waiting = toDownload.size();
 		int priority;
 
 		if (waiting >= midThreshold)
 			priority = midPriority;
 		else if (waiting >= highThreshold)
 			priority = midPriority;
 		else
 			priority = lowPriority;
 
 		Generic.setPriority(t, priority);
 
 		return priority;
 	}
 
 	/**
 	 * Create a new Thread that runs performDownloads().
 	 * 
 	 * @param i
 	 * @return
 	 */
 	protected Thread newPerformDownloadsThread(int i)
 	{
 		return newPerformDownloadsThread(i, "");
 	}
 
 	/**
 	 * Create a new Thread that runs performDownloads().
 	 * 
 	 * @param i
 	 * @param s
 	 * @return
 	 */
 	protected Thread newPerformDownloadsThread(int i, String s)
 	{
 		return new Thread(THREAD_GROUP, toString() + "-download " + i + " " + s)
 		{
 			public void run()
 			{
 				performDownloads();
 			}
 		};
 	}
 
 	/**
 	 * Creates and starts up our performDownloads() Threads.
 	 * 
 	 */
 	private void startPerformDownloadsThreads()
 	{
 		if (downloadThreads == null)
 		{
 			finished = false;
 			downloadThreads = new Thread[numDownloadThreads];
 			priorities = new int[numDownloadThreads];
 			for (int i = 0; i < numDownloadThreads; i++)
 			{
 				Thread thatThread = newPerformDownloadsThread(i);
 				downloadThreads[i] = thatThread;
 				thatThread.setPriority(lowPriority);
 				priorities[i] = lowPriority;
 				// ThreadDebugger.registerMyself(thatThread);
 				thatThread.start();
 			}
 		}
 	}
 
 	public void pause(int sleepTime)
 	{
 		pause(true);
 		Generic.sleep(sleepTime);
 		unpause();
 	}
 	
 	public void pause()
 	{
 		pause(true);
 	}
 
 	public void unpause()
 	{
 		if (paused)
 		{
 			pause(false);
 			notifyAll(toDownload);
 		}
 	}
 
 	public void pause(boolean paused)
 	{
 		synchronized (toDownload)
 		{
 			// debug("pause("+paused);
 			this.paused = paused;
 
 			int[] priorities = this.priorities; // avoid race
 			if (paused)
 			{
 				if (downloadThreads != null)
 				{
 					for (int i = 0; i < numDownloadThreads; i++)
 					{
 						Thread thatThread = downloadThreads[i];
 						if (thatThread != null)
 						{
 							int thatPriority = thatThread.getPriority();
 							priorities[i] = thatPriority;
 							if (Thread.MIN_PRIORITY < thatPriority)
 								thatThread.setPriority(Thread.MIN_PRIORITY);
 						}
 					}
 				}
 			}
 			else
 			{
 				if (downloadThreads != null)
 				{
 					for (int i = 0; i < numDownloadThreads; i++)
 					{
 						// restore priorities
 						Thread t = downloadThreads[i];
 						if ((t != null) && (priorities != null) && t.isAlive())
 						{
 							// debug("restore priority to " + priorities[i]);
 							int thatPriority = priorities[i];
 							if (thatPriority <= 0)
 								thatPriority = 1;
 							t.setPriority(thatPriority);
 						}
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Keep track of system millis that this site can be hit at.
 	 */
 	public static Hashtable<BasicSite, Long> siteTimeTable = new Hashtable<BasicSite, Long>();
 	/**
 	 * The heart of the workhorse Threads. It loops, pulling a DownloadClosure off the toDownload
 	 * queue, calling its performDownload() method, and then calling dispatch() if there is a
 	 * dispatchTarget.
 	 * 
 	 * 
 	 */
 	void performDownloads()
 	{
 		Thread downloadThread = Thread.currentThread();
 		while (!finished) // major sleep at the bottom
 		{
 			// keep track if local file to avoid wait
 			boolean isLocalFile						= false;
 			
 			DownloadClosure thatClosure = null;	// define out here to use outside of synchronized
 			synchronized (toDownload)
 			{
 				// debug("-- got lock");
 				if (paused)
 					wait(toDownload, MAX_WAIT_WHEN_PAUSED);
 				if (toDownload.isEmpty())
 					wait(toDownload, MAX_WAIT_TIME);
 				if (finished)
 					break;
 
 				// Let's assume that the change in time while iterating over toDownload doesn't matter.
 				// We don't want to hit this method for every item.
 				final long currentTimeMillis 	= System.currentTimeMillis();
 				int closureNum 								= 0;
 				final int toDownloadSize 			= toDownloadSize();
 				if (toDownloadSize > 0)
 				{
 					while (closureNum < toDownloadSize)
 					{
 						thatClosure 		= toDownload.get(closureNum);
 						BasicSite site 	= thatClosure.downloadable.getSite();
 						
 						if (site != null && site.constrainDownloadInterval())
 						{
 							Long nextDownloadableAt 	= siteTimeTable.get(site);
 							if(nextDownloadableAt != null)
 							{
 								long timeRemaining 			= nextDownloadableAt - currentTimeMillis;
 								if (timeRemaining < 0 && !thatClosure.cancel())
 								{
 									debug("\t\t-- Downloading: " + thatClosure.downloadable + " at\t" + new Date(currentTimeMillis) + " --");
 									setNextAvailableTimeForSite(site);
 									break;
 								}
 								else // Its not time yet, so skip this downloadable.
 								{
 	//								debug("Ignoring downloadable: " + thatClosure.downloadable + ". need atleast another: "
 	//										+ ((float) timeRemaining / 1000.0) + " seconds");
 									thatClosure = null;
 								}
 							}
 							else
 							{ // No nextDownloadableAt time found for this site, put in a new value, and accept this downloadClosure
 								setNextAvailableTimeForSite(site);
 								break;
 							}
 						}
 						else if (site != null && site.isDownloading())
 						{	// Another one from this site is already downloading, so skip this downloadable.
 							thatClosure = null;
 						}
 						else if (!thatClosure.cancel())
 						{
 							// Site-less downloadables	
 							isLocalFile = thatClosure.downloadable.purl().isFile();
 							break;
 						}
 						closureNum++;
 						thatClosure = null;
 					}	// end while
 					if (thatClosure != null)
 					{	// We have a satisfactory downloadClosure, ready to be downloaded. Remove from toDownload Vector
 //						toDownload.remove(thatClosure);
 						toDownload.remove(closureNum);
 //						System.out.println("Download Queue after this download:");
 //						int tempClosureNum = 0;
 //						for(DownloadClosure closure : toDownload)
 //							System.out.println("\t\t"+ tempClosureNum+ ": " + closure);
 					}
 				}
 			}	// end synchronized
 
 			boolean lowMemory = Memory.reclaimIfLow();
 
 			if (thatClosure != null)
 			{
 				synchronized (TOO_MANY_PENDING_LOCK)
 				{
 					if (!this.highNumberWaiting())
 						TOO_MANY_PENDING_LOCK.notifyAll();
 				}
 				if (lowMemory)
 				{
 					if (status != null)
 						status.display("Running out of memory, so not downloading new files.", 6);
 					toDownload.insertElementAt(thatClosure, 0); // put it back into the queue
 				}
 				else
 				{
 					try
 					{
 						pending++;
 						// ThreadDebugger.waitIfPaused(downloadThread);
 						// NEW -- set the priority of the download, based on how backed up we are
 						setDownloadPriority();
 						thatClosure.performDownload();
 
 						// Just when the download is done, remove from the potentialTimeouts.
 						if (thatClosure.downloadable.isDownloadDone())
 						{
 							thatClosure.dispatch();
 						}
 					}
 					catch (ThreadDeath e)
 					{
 						debug("ThreadDeath in performDownloads() loop");
 						e.printStackTrace();
 						throw e;
 					}
 					catch (ClosedByInterruptException e)
 					{
 						debug("Recovering from ClosedByInterruptException in performDownloads() loop.");
 						e.printStackTrace();
 						thatClosure.ioError();
 					}
 					catch (OutOfMemoryError e)
 					{
 						finished = true; // give up!
 						OutOfMemoryErrorHandler.handleException(e);
 
 					}
 					catch (Throwable e)
 					{
 						boolean interrupted = Thread.interrupted();
 						String interruptedStr = interrupted ? " interrupted" : "";
 						// TODO -- i'm concerned that we might need different error handling for different kinds
 						// of errors.
 						debugA("performDownloads() -- recovering from " + interruptedStr + " exception on "
 								+ thatClosure + ":");
 						e.printStackTrace();
 						thatClosure.ioError();
 					}
 					finally
 					{
 						pending--;
 						BasicSite site	= thatClosure.downloadable.getSite();
 						if (site != null)
 							site.endDownloading();
 					}
 				}
 			}
 
 			int sleepTime = dontWait || isLocalFile ? NO_SLEEP
 											: (lowMemory ? LOW_MEMORY_SLEEP 
 											: (hurry ? SHORT_SLEEP
 											: (REGULAR_SLEEP + MathTools.random(100))));
 //			debug("\t\t-------\tSleeping for: " + sleepTime);
 			Generic.sleep(sleepTime);
 		} // while (!finished)
 		debug("exiting -- " + Thread.currentThread());
 	}
 
 	/**
 	 * 
 	 * @param site
 	 */
 	private void setNextAvailableTimeForSite(BasicSite site)
 	{
 		synchronized(siteTimeTable)
 		{
 		//The next time we encounter the site, get a different interval.
 			siteTimeTable.put(site, System.currentTimeMillis() + site.getDecentDownloadInterval()); 
 		}
 	}
 	
 	public void setAbnormallyLongNextAvailableTimeForSite(BasicSite site)
 	{
 		siteTimeTable.put(site, System.currentTimeMillis() + TWELVE_HOURS_IN_MILLIS);
 	}
 
 	public String toString()
 	{
 		return super.toString() + "[" + name + "]";
 	}
 
 	public void stop()
 	{
 		stop(false);
 	}
 
 	/**
 	 * Stop our threads.
 	 */
 	public void stop(boolean kill)
 	{
 		// debug("stop()");
 		finished = true;
 
 		notifyAll(toDownload);
 
 		if (downloadThreads != null)
 		{
 			for (int i = 0; i < downloadThreads.length; i++)
 			{
 				Thread thatThread = downloadThreads[i];
 				if (kill)
 					thatThread.stop();
 				downloadThreads[i] = null;
 			}
 			downloadThreads = null;
 		}
 	}
 
 	public int waitingToDownload()
 	{
 		return toDownload.size();
 	}
 
 	/**
 	 * @return true if we're backed up with unresloved downloads.
 	 */
 	public boolean highNumberWaiting()
 	{
 		return toDownload.size() > highThreshold;
 	}
 
 	public boolean midNumberWaiting()
 	{
 		return toDownload.size() > midThreshold;
 	}
 
 	public int lowPriority()
 	{
 		return lowPriority;
 	}
 
 	public int midPriority()
 	{
 		return midPriority;
 	}
 
 	public int highPriority()
 	{
 		return highPriority;
 	}
 
 	public int pending()
 	{
 		return pending;
 	}
 
 	/**
 	 * Set whether or not this download monitor needs to wait after each download attempt
 	 * @param noWait
 	 */
 	public void setNoWait(boolean noWait)
 	{
 		dontWait = noWait;
 	}
 	
 	public void setHurry(boolean hurry)
 	{
 		this.hurry = hurry;
 		debug("setHurry(" + hurry);
 	}
 
 	public static NewPorterStemmer getStemmer()
 	{
 		Thread currentThread = Thread.currentThread();
 		NewPorterStemmer stemmer = stemmersHash.get(currentThread);
 		if (stemmer == null)
 		{
 			stemmer = new NewPorterStemmer();
 			stemmersHash.put(currentThread, stemmer);
 		}
 		return stemmer;
 	}
 
 	/**
 	 * check the number of elements in the toDownload Queue
 	 * 
 	 * @return
 	 */
 	public int toDownloadSize()
 	{
 		return toDownload.size();
 	}
 
 	/**
 	 * Stop performing downloads, and then Get rid of queued DownloadClosures.
 	 */
 	public void clear()
 	{
 		pause();
 		toDownload.clear();
 	}
 
 	public void waitIfTooManyPending()
 	{
 		if (!highNumberWaiting())
 			return;
 		synchronized (TOO_MANY_PENDING_LOCK)
 		{
 			try
 			{
 				debug("wait() on TOO_MANY_PENDING_LOCK");
 				printQueue();
 				TOO_MANY_PENDING_LOCK.wait();
 				debug("finished wait() on TOO_MANY_PENDING_LOCK");
 			}
 			catch (InterruptedException e)
 			{
 				Debug.weird(this, "Interrupted while waiting for TOO_MANY_PENDING_LOCK");
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public void printQueue()
 	{
 		synchronized (toDownload)
 		{
 			System.out.println(this.toString() + "QUEUE:");
 			for (DownloadClosure d : toDownload)
 			{
 				System.out.println("\t" + d.downloadable);
 			}
 		}
 		System.out.println("\n");
 	}
 
 	public StatusReporter getStatus()
 	{
 		return status;
 	}
 
 	public void setStatus(StatusReporter status)
 	{
 		this.status = status;
 	}
 
 	/**
 	 * Removes all downloadClosures that come from this site.
 	 * @param site
 	 */
 	public void removeAllDownloadClosuresFromSite(BasicSite site)
 	{
 		synchronized(toDownload)
 		{
 			ArrayList<Integer> indexesToRemove = new ArrayList<Integer>();
 			int index = 0;
 			for(DownloadClosure d : toDownload)
 				if(d.downloadable != null && d.downloadable.getSite() == site)
 					indexesToRemove.add(index++);
 			if(indexesToRemove.size() > 0)
 			{
 				debug("Removing " + indexesToRemove.size() + " from the queue");
 				for(int removeIndex : indexesToRemove)
 					toDownload.remove(removeIndex);
 			}
 			else
 				debug("Nothing to remove for site: " + site);
 		}
 	}
 	
 	/**
 	 * @return the paused
 	 */
 	public boolean isPaused()
 	{
 		return paused;
 	}
 
 	public int size()
 	{
 		return toDownloadSize();
 	}
 }
