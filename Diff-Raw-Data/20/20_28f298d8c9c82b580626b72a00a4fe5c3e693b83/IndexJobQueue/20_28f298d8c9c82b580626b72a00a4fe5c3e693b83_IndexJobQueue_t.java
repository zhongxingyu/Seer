 package com.gentics.cr.util.indexing;
 
 import java.util.ArrayList;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import com.gentics.cr.CRConfig;
 /**
  * 
  * Last changed: $Date: 2009-09-02 17:57:48 +0200 (Mi, 02 Sep 2009) $
  * @version $Revision: 180 $
  * @author $Author: supnig@constantinopel.at $
  *
  */
 public class IndexJobQueue{
 	
 	private static final String INTERVAL_KEY = "CHECKINTERVAL";
 	
 	private LinkedBlockingQueue<AbstractUpdateCheckerJob> queue;
 	private Thread d;
 	private boolean stop=false;
 	private int interval = 5; // Default 5 sec interval for checking
 	private Thread currentJob;
 	private AbstractUpdateCheckerJob currentJI;
 	private ArrayList<AbstractUpdateCheckerJob> lastJobs;
 	
 	/**
 	 * Create new instance of JobQueue
 	 * @param config
 	 */
 	public IndexJobQueue(CRConfig config) {
 		String i = (String)config.get(INTERVAL_KEY);
 		if(i!=null)this.interval = new Integer(i);
 		queue = new LinkedBlockingQueue<AbstractUpdateCheckerJob>();
 		lastJobs = new ArrayList<AbstractUpdateCheckerJob>(3);
 		this.d = new Thread(new Runnable(){
 			public void run()
 			{
 				workQueue();
 			}
 		});
 		this.d.setName("IndexJobQueueWorker-" + config.getName());
		this.d.setDaemon(true);
 	}
 	
 	/**
 	 * Returns true if the worker is running and processing the queue
 	 * @return
 	 */
 	public boolean isRunning()
 	{
 		return (!this.stop && this.d.isAlive());
 	}
 	
 	/**
 	 * Get an ArrayList with the three last jobs. If there where no jobs the list is going to be empty.
 	 * @return
 	 */
 	public ArrayList<AbstractUpdateCheckerJob> getLastJobs()
 	{
 		return this.lastJobs;
 	}
 	
 	/**
 	 * Add a Job to the list of finished jobs
 	 * Always keeps 3 jobs
 	 * Only for display in the indexer servlet
 	 * @param j
 	 */
 	private void addToLastJobs(AbstractUpdateCheckerJob j)
 	{
 		ArrayList<AbstractUpdateCheckerJob> l = new ArrayList<AbstractUpdateCheckerJob>(3);
 		l.add(j);
 		int i=0;
 		for(AbstractUpdateCheckerJob e:lastJobs)
 		{
 			if(e!=null)l.add(e);
 			i++;
 			if(i>=2)break;
 		}
 		lastJobs = l;
 	}
 	
 	/**
 	 * Returns current Index job or null if none is being processed at the moment
 	 * @return
 	 */
 	public AbstractUpdateCheckerJob getCurrentJob()
 	{
 		return this.currentJI;
 	}
 	
 	/**
 	 * Check the queue for new jobs each <interval> seconds
 	 */
 	private void workQueue()
 	{
 		boolean interrupted = false;
 
 		while (!interrupted && !stop) {
 			try {
 				AbstractUpdateCheckerJob j = this.queue.poll();
 				if(j!=null)
 				{
 					synchronized(IndexJobQueue.this)
 					{
 						currentJI = j;
 						currentJob = new Thread(j);
 						currentJob.setName("Current Index Job");
						currentJob.setDaemon(true);
 						currentJob.start();
 						if(currentJob.isAlive())
 						{
 							currentJob.join();
 						}
 						addToLastJobs(j);
 						currentJob = null;
 						currentJI = null;
 					}
 				}
 				// Wait for next cycle
 				if(!Thread.currentThread().isInterrupted())
 					Thread.sleep(interval * 1000);
 				else
 					interrupted = true;
 			} catch (InterruptedException e) {
 				interrupted = true;
 			}
 		}
 		this.stop=true;
 	}
 	
 	/**
 	 * Stops all working Jobs and ends the worker queue
 	 * This method has to be called before program can exit
 	 */
 	public void stop()
 	{
 		
 		
 		if(currentJob!=null)
 		{
 			if(currentJob.isAlive())
 			{
 				currentJob.interrupt();
 			}
 		}
 		
 		
 		//END CURRENT JOB
 		synchronized(IndexJobQueue.this)
 		{
 			//WAIT FOR CURRENT JOB TO END
 			if(currentJob!=null)
 			{
 				try {
 						if(currentJob.isAlive())
 						{
 							//INTERRUPT IF A NEW JOB HAS BEEN CREATED
 							if(!currentJob.isInterrupted())
 								currentJob.interrupt();
 							currentJob.join();
 						}
 						
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 			}
 			//TODO Clear queue and stop each queued job
 			this.queue.clear();
 			//END WORKER THREAD
 			if(d!=null)
 			{ 
 				if(d.isAlive())
 				{
 					d.interrupt();
 					try {
 						d.join();
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Starts the worker that is processing the Indexer Queue
 	 */
 	public void startWorker()
 	{
 		this.d.start();
 		this.stop=false;
 	}
 	
 	/**
 	 * Stops the queue worker
 	 */
 	public void stopWorker()
 	{
 		this.stop=true;
 		try {
 			this.d.join(5000);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	
 	/**
 	 * Adds a CRIndexJob to the Job Queue
 	 * @param job
 	 * @return
 	 */
 	public synchronized boolean addJob(AbstractUpdateCheckerJob job)
 	{
 		if(!queue.contains(job))
 		{
 			return queue.offer(job);
 		}
 		return false;
 	}
 	
 	/**
 	 * Get Number of Jobs in the Queue
 	 * @return
 	 */
 	public int getSize()
 	{
 		return this.queue.size();
 	}
 	
 		
 	/**
 	 * Returns configured interval for checking the queue for new jobs
 	 * @return
 	 */
 	public int getInterval()
 	{
 		return this.interval;
 	}
 		
 }
