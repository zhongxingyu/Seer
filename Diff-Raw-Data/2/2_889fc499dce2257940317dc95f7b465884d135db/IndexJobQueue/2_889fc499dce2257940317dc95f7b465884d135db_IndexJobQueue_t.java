 package com.gentics.cr.util.indexing;
 
 import java.util.ArrayList;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import org.apache.log4j.Logger;
 
 import com.gentics.cr.CRConfig;
 
 /**
  * JobQueue worker class.
  * Last changed: $Date: 2009-09-02 17:57:48 +0200 (Mi, 02 Sep 2009) $
  * @version $Revision: 180 $
  * @author $Author: supnig@constantinopel.at $
  *
  */
 public class IndexJobQueue {
 
 	/**
 	 * Log4j logger for error and debug messages.
 	 */
 	private static final Logger LOGGER = Logger.getLogger(IndexJobQueue.class);
 
 	/**
 	 * Miliseconds in a second.
 	 */
 	private static final int MILISECONDS_IN_A_SECOND = 1000;
 
 	/**
 	 * Default interval.
 	 */
 	private static final int DEFAULT_INTERVAL = 5;
 	/**
 	 * Confiuration key to set if only empty jobs should be hidden in lastjobs
 	 * array.
 	 */
 	private static final String HIDE_EMPTY_JOBS_KEY = "HIDEEMPTYJOBS";
 
 	/**
 	 * Configuration key to set check interval of job queue.
 	 */
 	private static final String INTERVAL_KEY = "CHECKINTERVAL";
 
 	/**
 	 * Configuration key to set size of lastjobs array.
 	 */
 	private static final String SIZE_KEY = "LASTJOBS_SIZE";
 
 	/**
 	 * Queue containing the jobs to do. 
 	 */
 	private LinkedBlockingQueue<AbstractUpdateCheckerJob> queue;
 
 	/**
 	 * Daemon that run one job at a time from {@link #queue}.
 	 */
 	private Thread indexJobQueueWorkerDaemon;
 	/**
 	 * if the worker is stopped.
 	 */
 	private boolean stop = false;
 	/**
 	 * if the worker is in a paused state.
 	 */
 	private boolean paused = false;
 	/**
 	 * interval of the worker.
 	 */
 	private int interval = DEFAULT_INTERVAL;
 	/**
 	 * the current job instance.
 	 */
 	private Thread currentJob;
 	/**
 	 * the corrent job.
 	 */
 	private AbstractUpdateCheckerJob currentJI;
 
 	/**
 	 * Array containing the last jobs for statistics.
 	 */
 	private ArrayList<AbstractUpdateCheckerJob> lastJobs;
 
 	/**
 	 * sets if we only save jobs that actually did something. (not only
 	 * performing an update check)
 	 */
 	private boolean hideEmptyJobs = false;
 
 	/**
 	 * size of lastjobs array.
 	 */
 	private int lastJobsSize = 3;
 
 	/**
 	 * PauseMonitor.
 	 */
 	private Object pauseMonitor = new Object();
 
 	/**
 	 * Create new instance of JobQueue.
 	 * @param config configuration of the job queue
 	 */
 	public IndexJobQueue(final CRConfig config) {
 		interval = config.getInteger(INTERVAL_KEY, interval);
 		lastJobsSize = config.getInteger(SIZE_KEY, lastJobsSize);
 		hideEmptyJobs = config.getBoolean(HIDE_EMPTY_JOBS_KEY, hideEmptyJobs);
 
 		queue = new LinkedBlockingQueue<AbstractUpdateCheckerJob>();
 		lastJobs = new ArrayList<AbstractUpdateCheckerJob>(lastJobsSize);
 		indexJobQueueWorkerDaemon = new Thread(new Runnable() {
 			public void run() {
 				workQueue();
 			}
 		});
 		indexJobQueueWorkerDaemon.setName("IndexJobQueueWorker-" + config.getName());
 		indexJobQueueWorkerDaemon.setDaemon(true);
 		LOGGER.debug("Created IndexJobQueue-WorkerThread for " + config.getName());
 	}
 
 	/**
 	 * Returns true if the worker is running and processing the queue.
 	 * @return <code>true</code> if the worker is running.
 	 */
 	public final boolean isRunning() {
 		return (!this.paused && !this.stop && this.indexJobQueueWorkerDaemon.isAlive());
 	}
 
 	/**
 	 * Get an ArrayList with the three last jobs. If there where no jobs the
 	 * list is going to be empty.
 	 * @return array of last jobs
 	 */
 	public final ArrayList<AbstractUpdateCheckerJob> getLastJobs() {
 		return this.lastJobs;
 	}
 
 	/**
 	 * Add a Job to the list of finished jobs. Always keeps as much jobs as
 	 * configured in {@link #lastJobsSize}. Default is 3.
 	 * Only for display in the indexer servlet.
 	 * @param job job to add to the last jobs array.
 	 */
 	private void addToLastJobs(final AbstractUpdateCheckerJob job) {
 		if (!hideEmptyJobs || job.getObjectsDone() > 0) {
 			lastJobs.add(0, job);
 			if (lastJobs.size() > lastJobsSize) {
 				lastJobs.remove(lastJobsSize);
 			}
 		}
 	}
 
 	/**
 	 * Returns current Index job or null if none is being processed
 	 * at the moment.
 	 * @return the current job
 	 */
 	public final AbstractUpdateCheckerJob getCurrentJob() {
 		return this.currentJI;
 	}
 
 	/**
 	 * Check the queue for new jobs each <interval> seconds.
 	 */
 	private void workQueue() {
 		boolean interrupted = false;
 
 		while (!interrupted && !stop) {
 			try {
 				synchronized (this.pauseMonitor) {
 					while (paused) {
 						this.pauseMonitor.wait();
 					}
 				}
 				AbstractUpdateCheckerJob j = this.queue.poll();
 				if (j != null) {
 					LOGGER.debug("Starting Job - " + j.getIdentifyer());
 					synchronized (IndexJobQueue.this) {
 						currentJI = j;
 						currentJob = new Thread(j);
 						currentJob.setName("Current Index Job - " + j.getIdentifyer());
 						currentJob.setDaemon(true);
 						currentJob.start();
 						if (currentJob.isAlive()) {
 							currentJob.join();
 						}
 						addToLastJobs(j);
 						currentJob = null;
 						currentJI = null;
 					}
 					LOGGER.debug("Finished Job - " + j.getIdentifyer());
 				}
 				// Wait for next cycle
 				if (!Thread.currentThread().isInterrupted()) {
 					Thread.sleep(interval * MILISECONDS_IN_A_SECOND);
 				} else {
 					interrupted = true;
 				}
 			} catch (InterruptedException e) {
 				interrupted = true;
 			}
 		}
 		this.stop = true;
 	}
 
 	/**
 	 * Stops all working Jobs and ends the worker queue.
 	 * This method has to be called before program can exit
 	 */
 	public final void stop() {
 		if (currentJob != null) {
 			if (currentJob.isAlive()) {
 				currentJob.interrupt();
 			}
 		}
 
 		//END CURRENT JOB
 		synchronized (IndexJobQueue.this) {
 			//WAIT FOR CURRENT JOB TO END
 			if (currentJob != null) {
 				try {
 					if (currentJob.isAlive()) {
 						//INTERRUPT IF A NEW JOB HAS BEEN CREATED
 						if (!currentJob.isInterrupted()) {
 							currentJob.interrupt();
 						}
 						currentJob.join();
 					}
 
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 			//TODO Clear queue and stop each queued job
 			this.queue.clear();
 			//END WORKER THREAD
 			if (indexJobQueueWorkerDaemon != null) {
 				if (indexJobQueueWorkerDaemon.isAlive()) {
 					indexJobQueueWorkerDaemon.interrupt();
 					try {
 						indexJobQueueWorkerDaemon.join();
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Starts the worker that is processing the Indexer Queue.
 	 */
 	public final void startWorker() {
 		this.indexJobQueueWorkerDaemon.start();
 		this.stop = false;
 	}
 
 	/**
 	 * Stops the queue worker. The worker cannot be resumed afterwards.
 	 */
 	public final void stopWorker() {
 		this.stop = true;
 		try {
 			final int waitTime = 5000;
 			this.indexJobQueueWorkerDaemon.join(waitTime);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Sets the worker in a paused state. The worker can be resumed afterwards.
 	 */
 	public final void pauseWorker() {
 		synchronized (this.pauseMonitor) {
 			paused = true;
 		}
 	}
 
 	/**
 	 * Resumes the worker from a paused state. If the worker is not in a
 	 * paused state, nothing will happen.
 	 */
 	public final void resumeWorker() {
 		synchronized (this.pauseMonitor) {
 			paused = false;
 			this.pauseMonitor.notify();
 		}
 	}
 
 	/**
 	 * Adds a CRIndexJob to the Job Queue.
 	 * @param job job to add to teh queue
 	 * @return <code>true</code> if job was added, otherwhise it returns
 	 * <code>false</code>
 	 */
	public final boolean addJob(final AbstractUpdateCheckerJob job) {
 		if (!queue.contains(job)) {
 			return queue.offer(job);
 		}
 		return false;
 	}
 
 	/**
 	 * Get Number of Jobs in the Queue.
 	 * @return size of the queue
 	 */
 	public final int getSize() {
 		return this.queue.size();
 	}
 
 	/**
 	 * Returns configured interval for checking the queue for new jobs.
 	 * @return interval
 	 */
 	public final int getInterval() {
 		return this.interval;
 	}
 
 }
