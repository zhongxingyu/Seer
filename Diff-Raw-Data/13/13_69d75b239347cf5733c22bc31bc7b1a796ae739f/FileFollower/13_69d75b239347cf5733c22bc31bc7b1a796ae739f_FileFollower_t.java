 /*
  * Copyright (C) 2000-2003 Greg Merrill (greghmerrill@yahoo.com)
  * 
  * This file is part of Follow (http://follow.sf.net).
  * 
  * Follow is free software; you can redistribute it and/or modify it under the
  * terms of version 2 of the GNU General Public License as published by the Free
  * Software Foundation.
  * 
  * Follow is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
  * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * Follow; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
  * Suite 330, Boston, MA 02111-1307 USA
  */
 
 package ghm.follow;
 
 import ghm.follow.io.OutputDestination;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Instances of this class 'follow' a particular text file, assmebling that file's characters into
  * Strings and sending them to instances of {@link OutputDestination}. The name and behavior of
  * this class are inspired by the '-f' (follow) flag of the UNIX command 'tail'.
  * 
  * @see OutputDestination
  * @author <a href="mailto:greghmerrill@yahoo.com">Greg Merrill</a>
  */
 public class FileFollower
 {
 
 	/**
 	 * Constructs a new FileFollower; invoking this constructor does <em>not</em> cause the new
 	 * object to begin following the supplied file. In order to begin following, one must call
 	 * {@link #start()}.
 	 * 
 	 * @param file
 	 *            file to be followed
 	 * @param bufferSize
 	 *            number of chars to be read each time the file is accessed
 	 * @param latency
 	 *            each time a FileFollower's running thread encounters the end of the file in its
 	 *            stream, it will rest for this many milliseconds before checking to see if there
 	 *            are any more bytes in the file
 	 * @param initialOutputDestinations
 	 *            an initial array of OutputDestinations which will be used when printing the
 	 *            contents of the file (this array may be <tt>null</tt>)
 	 */
 	public FileFollower(File file, int bufferSize, int latency,
 			OutputDestination[] initialOutputDestinations)
 	{
 		file_ = file;
 		bufferSize_ = bufferSize;
 		latency_ = latency;
 
 		int initOutputDestsSize = (initialOutputDestinations != null) ? initialOutputDestinations.length
 				: 0;
 		outputDestinations_ = new ArrayList<OutputDestination>(initOutputDestsSize);
 		for (int i = 0; i < initOutputDestsSize; i++)
 		{
 			outputDestinations_.add(initialOutputDestinations[i]);
 		}
 	}
 
 	/**
 	 * Identical to {@link #FileFollower(File, int, int, OutputDestination[])}, except that a
 	 * default buffer size (32,768 characters) and latency (1000 milliseconds) are used.
 	 * 
 	 * @see #FileFollower(File, int, int, OutputDestination[])
 	 */
 	public FileFollower(File file, OutputDestination[] initialOutputDestinations)
 	{
 		// Initial buffer size pilfered from org.gjt.sp.jedit.Buffer. I'm not
 		// sure whether this is a truly optimal buffer size.
 		this(file, 32768, // Don't change without updating docs!
 				1000, // Don't change without updating docs!
 				initialOutputDestinations);
 	}
 
 	/**
 	 * Cause this FileFollower to spawn a thread which will follow the file supplied in the
 	 * constructor and send its contents to all of the FileFollower's OutputDestinations.<br>
 	 * <br>
 	 * If this FileFollower is running but paused, this method equates to calling unpause().
 	 */
 	public synchronized void start()
 	{
 		if (continueRunning_ && paused_)
 		{
 			unpause();
 		}
 		else
 		{
 			continueRunning_ = true;
 			paused_ = false;
 			runnerThread_ = new Thread(new Runner(), getFollowedFile().getName());
 			runnerThread_.start();
 		}
 	}
 
 	public synchronized void pause()
 	{
 		paused_ = true;
 	}
 
 	public synchronized void unpause()
 	{
 		paused_ = false;
 	}
 
 	public synchronized void restart()
 	{
 		needsRestart_ = true;
 		runnerThread_.interrupt();
 	}
 
 	/**
 	 * Cause this FileFollower to stop following the file supplied in the constructor after it
 	 * flushes the characters it's currently reading to all its OutputDestinations.
 	 */
 	public synchronized void stop()
 	{
 		continueRunning_ = false;
 		runnerThread_.interrupt();
 	}
 
 	/**
 	 * Like {@link #stop()}, but this method will not exit until the thread which is following the
 	 * file has finished executing (i.e., stop synchronously).
 	 */
 	public synchronized void stopAndWait() throws InterruptedException
 	{
 		stop();
 		while (runnerThread_.isAlive())
 		{
 			Thread.yield();
 		}
 	}
 
 	/**
 	 * Send the supplied string to all OutputDestinations
 	 * 
 	 * @param s
 	 */
 	private synchronized void print(String s)
 	{
 		for (OutputDestination out : outputDestinations_)
 		{
 			out.print(s);
 		}
 	}
 
 	/**
 	 * Clear all OutputDestinations
 	 */
 	private synchronized void clear()
 	{
 		for (OutputDestination out : outputDestinations_)
 		{
 			out.clear();
 		}
 	}
 
 	/**
 	 * Add another OutputDestination to which the followed file's contents should be printed.
 	 * 
 	 * @param outputDestination
 	 *            OutputDestination to be added
 	 */
 	public boolean addOutputDestination(OutputDestination outputDestination)
 	{
 		return outputDestinations_.add(outputDestination);
 	}
 
 	/**
 	 * Remove the supplied OutputDestination from the list of OutputDestinations to which the
 	 * followed file's contents should be printed.
 	 * 
 	 * @param outputDestination
 	 *            OutputDestination to be removed
 	 */
 	public boolean removeOutputDestination(OutputDestination outputDestination)
 	{
 		return outputDestinations_.remove(outputDestination);
 	}
 
 	/**
 	 * Returns the List which maintains all OutputDestinations for this FileFollower.
 	 * 
 	 * @return contains all OutputDestinations for this FileFollower
 	 */
 	public List<OutputDestination> getOutputDestinations()
 	{
 		return outputDestinations_;
 	}
 
 	/**
 	 * Returns the file which is being followed by this FileFollower
 	 * 
 	 * @return file being followed
 	 */
 	public File getFollowedFile()
 	{
 		return file_;
 	}
 
 	/**
 	 * Returns the following state of a file
 	 * 
 	 * @return true if being followed, false if not being followed
 	 */
 	public boolean isBeingFollowed()
 	{
 		return continueRunning_;
 	}
 
 	/**
 	 * Returns the pause state of the follower.
 	 * 
 	 * @return true if paused, false otherwise
 	 */
 	public boolean isPaused()
 	{
 		return paused_;
 	}
 
 	/**
 	 * Returns the size of the character buffer used to read characters from the followed file. Each
 	 * time the file is accessed, this buffer is filled.
 	 * 
 	 * @return size of the character buffer
 	 */
 	public int getBufferSize()
 	{
 		return bufferSize_;
 	}
 
 	/**
 	 * Sets the size of the character buffer used to read characters from the followed file.
 	 * Increasing buffer size will improve efficiency but increase the amount of memory used by the
 	 * FileFollower.<br>
 	 * <em>NOTE:</em> Setting this value will <em>not</em> cause a running FileFollower to
 	 * immediately begin reading characters into a buffer of the newly specified size. You must stop &
 	 * restart the FileFollower in order for changes to take effect.
 	 * 
 	 * @param bufferSize
 	 *            size of the character buffer
 	 */
 	public void setBufferSize(int bufferSize)
 	{
 		bufferSize_ = bufferSize;
 	}
 
 	/**
 	 * Returns the time (in milliseconds) which a FileFollower spends sleeping each time it
 	 * encounters the end of the followed file.
 	 * 
 	 * @return latency, in milliseconds
 	 */
 	public int getLatency()
 	{
 		return latency_;
 	}
 
 	/**
 	 * Sets the time (in milliseconds) which a FileFollower spends sleeping each time it encounters
 	 * the end of the followed file. Note that extremely low latency values may cause thrashing
 	 * between the FileFollower's running thread and other threads in an application. A change in
 	 * this value will be reflected the next time the FileFollower's running thread sleeps.
 	 * 
 	 * @param latency
 	 *            latency, in milliseconds
 	 */
 	public void setLatency(int latency)
 	{
 		latency_ = latency;
 	}
 
 	protected int bufferSize_;
 
 	protected int latency_;
 
 	protected File file_;
 
 	protected List<OutputDestination> outputDestinations_;
 
 	protected boolean continueRunning_;
 
 	protected boolean needsRestart_;
 
 	protected Thread runnerThread_;
 
 	protected boolean paused_;
 
 	/**
 	 * Instances of this class are used to run a thread which follows a FileFollower's file and
 	 * sends prints its contents to OutputDestinations. This class should only handle the gathering
 	 * of data from the followed file. Actually writing to the output destinations is handled by the
 	 * outer class (FileFollower).
 	 */
 	class Runner implements Runnable
 	{
 		private Logger log = LoggerFactory.getLogger(Runner.class.getName());
 
 		public void run()
 		{
 			log.debug("entering FileFollower.run()");
 
 			while (continueRunning_)
 			{
 				runAction();
 			}
 
 			log.debug("exiting FileFollower.run()");
 		}
 
 		protected void runAction()
 		{
 			try
 			{
 				clear();
 				long fileSize = file_.length();
 				byte[] byteArray = new byte[bufferSize_];
 				int numBytesRead;
 				long lastActivityTime = file_.lastModified();
 
 				// create some stream readers to handle the file
 				FileInputStream fis = new FileInputStream(file_);
 				BufferedInputStream bis = new BufferedInputStream(fis);
 
 				// start at the beginning of the file
 				long startingPoint_ = 0;
 
 				// if the file size is bigger than the buffer size, skip to the
 				// end of the file if not performing a restart
				if (fileSize > bufferSize_)
 				{
 					startingPoint_ = fileSize - bufferSize_;
 				}
 				// reset the restart flag
 				needsRestart_ = false;
 
 				log.debug("Starting point: {}; Last activity: {}", startingPoint_,
 						lastActivityTime);
 
 				bis.skip(startingPoint_);
 
 				while (continueRunning_ && !needsRestart_)
 				{
 					if (!paused_)
 					{
 						lastActivityTime = System.currentTimeMillis();
 
 						numBytesRead = bis.read(byteArray, 0, byteArray.length);
 
 						boolean dataWasFound = (numBytesRead > 0);
 
 						log.debug("Bytes read: {}; dataWasFound: {}", numBytesRead,
 								dataWasFound);
 
 						// if data was found, print it and log activity time
 						if (dataWasFound)
 						{
 							String output = new String(byteArray, 0, numBytesRead);
 
 							// print the output to the listeners
 							print(output);
 						}
 						// no data found so check the file and restart if needed
 						else
 						{
 							// check if the file handle has become stale (file
 							// was modified, but no data was read).
 							boolean fileExists = file_.exists();
 							// removed check for 0 length because a file could change by
 							// being cleared out
 							// && (file_.length() > 0);
 							boolean fileHasChanged = file_.lastModified() > lastActivityTime;
 
 							if (fileExists && fileHasChanged)
 							{
 								log.debug("Needs restart [fileExists={}; fileHasChanged={}]",
 										fileExists, fileHasChanged);
 								needsRestart_ = true;
 							}
 						}
 
 						boolean allDataRead = (numBytesRead < byteArray.length);
 
 						if (allDataRead && !needsRestart_)
 						{
 							log.debug("Sleeping for {}ms [allDataRead:{}; needsRestart:{}]",
 									new Object[] { latency_, allDataRead, needsRestart_ });
 							sleep();
 						}
 					}
 					else
 					{
 						log.debug("Runner paused.");
 						sleep();
 					}
 				}
 				log.debug("exiting Runner.runAction [continueRunning_={}; needsRestart_={}]",
 						continueRunning_, needsRestart_);
 				bis.close();
 				fis.close();
 			}
 			catch (IOException e)
 			{
 				log.error("IOException while following file", e);
 			}
 		}
 
 		private void sleep()
 		{
 			try
 			{
 				Thread.sleep(latency_);
 			}
 			catch (InterruptedException e)
 			{
 				// Interrupt may be thrown manually by stop()
 				log.debug("DIED IN MY SLEEP");
 			}
 		}
 	}
 
 	/** Line separator, retrieved from System properties & stored statically. */
 	protected static final String lineSeparator = System.getProperty("line.separator");
 }
