 /**
  *
  */
 package org.jdcp.server;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.io.RandomAccessFile;
 import java.nio.ByteBuffer;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.UUID;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.jdcp.job.Host;
 import org.jdcp.job.JobExecutionException;
 import org.jdcp.job.JobExecutionWrapper;
 import org.jdcp.job.ParallelizableJob;
 import org.jdcp.job.TaskDescription;
 import org.jdcp.job.TaskWorker;
 import org.jdcp.remote.JobService;
 import org.jdcp.scheduling.TaskScheduler;
 import org.jdcp.server.classmanager.ClassManager;
 import org.jdcp.server.classmanager.ParentClassManager;
 import org.selfip.bkimmel.io.FileUtil;
 import org.selfip.bkimmel.progress.ProgressMonitor;
 import org.selfip.bkimmel.rmi.Serialized;
 import org.selfip.bkimmel.util.UnexpectedException;
 import org.selfip.bkimmel.util.classloader.SandboxedStrategyClassLoader;
 
 /**
  * @author brad
  *
  */
 public final class JobServer implements JobService {
 
 	private static final int DEFAULT_IDLE_SECONDS = 10;
 
 	private final ProgressMonitor monitor;
 
 	private final TaskScheduler scheduler;
 
 	private final ParentClassManager classManager;
 
 	private final File outputDirectory;
 
 	private final Map<UUID, ScheduledJob> jobs = new HashMap<UUID, ScheduledJob>();
 
 	private final Logger logger;
 
 	private TaskDescription idleTask = new TaskDescription(null, 0, DEFAULT_IDLE_SECONDS);
 
 	/**
 	 * Creates a new <code>JobServer</code>.
 	 * @param outputDirectory The directory to write job results to.
 	 * @param monitor The <code>ProgressMonitor</code> to report to.
 	 * @param scheduler The <code>TaskScheduler</code> to use to assign
 	 * 		tasks.
 	 * @param classManager The <code>ParentClassManager</code> to use to
 	 * 		store and retrieve class definitions.
 	 */
 	public JobServer(File outputDirectory, ProgressMonitor monitor, TaskScheduler scheduler, ParentClassManager classManager) throws IllegalArgumentException {
 		if (!outputDirectory.isDirectory()) {
 			throw new IllegalArgumentException("outputDirectory must be a directory.");
 		}
 		this.outputDirectory = outputDirectory;
 		this.monitor = monitor;
 		this.scheduler = scheduler;
 		this.classManager = classManager;
 
 		// TODO Replace this with a logger passed from the constructor.
 		this.logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jdcp.remote.JobService#createJob(java.lang.String)
 	 */
 	public UUID createJob(String description) throws SecurityException {
 		ScheduledJob sched = new ScheduledJob(description, monitor);
 		jobs.put(sched.id, sched);
 		return sched.id;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jdcp.remote.JobService#submitJob(org.selfip.bkimmel.rmi.Envelope, java.util.UUID)
 	 */
 	public void submitJob(Serialized<ParallelizableJob> job, UUID jobId)
 			throws IllegalArgumentException, SecurityException, ClassNotFoundException, JobExecutionException {
 		ScheduledJob sched = jobs.get(jobId);
 		if (sched == null || sched.job != null) {
 			throw new IllegalArgumentException("No pending job with provided Job ID");
 		}
 
 		try {
 			sched.initializeJob(job);
 			sched.scheduleNextTask();
 		} catch (JobExecutionException e) {
 			handleJobExecutionException(e, jobId);
 			throw e;
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jdcp.remote.JobService#submitJob(org.selfip.bkimmel.rmi.Envelope, java.lang.String)
 	 */
 	public UUID submitJob(Serialized<ParallelizableJob> job, String description)
 			throws SecurityException, ClassNotFoundException, JobExecutionException {
 		ScheduledJob sched = new ScheduledJob(description, monitor);
 		jobs.put(sched.id, sched);
 
 		try {
 			sched.initializeJob(job);
 			sched.scheduleNextTask();
 		} catch (JobExecutionException e) {
 			handleJobExecutionException(e, sched.id);
 			throw e;
 		}
 
 		return sched.id;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jdcp.remote.JobService#cancelJob(java.util.UUID)
 	 */
 	public void cancelJob(UUID jobId) throws IllegalArgumentException, SecurityException {
 		if (!jobs.containsKey(jobId)) {
 			throw new IllegalArgumentException("No job with provided Job ID");
 		}
 
 		cancelScheduledJob(jobId);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jdcp.remote.JobService#getTaskWorker(java.util.UUID)
 	 */
 	public Serialized<TaskWorker> getTaskWorker(UUID jobId)
 			throws IllegalArgumentException, SecurityException {
 		ScheduledJob sched = jobs.get(jobId);
 		if (sched == null) {
 			throw new IllegalArgumentException("No submitted job with provided Job ID");
 		}
 
 		return sched.worker;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jdcp.remote.JobService#requestTask()
 	 */
 	public synchronized TaskDescription requestTask() throws SecurityException {
 		TaskDescription taskDesc = scheduler.getNextTask();
 		if (taskDesc == null) {
 			return idleTask;
 		}
 
 		ScheduledJob sched = jobs.get(taskDesc.getJobId());
 		try {
 			sched.scheduleNextTask();
 		} catch (JobExecutionException e) {
 			handleJobExecutionException(e, sched.id);
 		}
 		return taskDesc;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jdcp.remote.JobService#submitTaskResults(java.util.UUID, int, org.selfip.bkimmel.rmi.Envelope)
 	 */
 	public void submitTaskResults(UUID jobId, int taskId,
 			Serialized<Object> results) throws SecurityException {
 		ScheduledJob sched = jobs.get(jobId);
 		if (sched == null) {
 			throw new IllegalArgumentException("No submitted job with provided Job ID");
 		}
 
 		try {
 			if (sched.submitTaskResults(taskId, results)) { // job is complete
 				sched.monitor.notifyComplete();
 				jobs.remove(jobId);
 				scheduler.removeJob(jobId);
 				classManager.releaseChildClassManager(sched.classManager);
 			}
 		} catch (JobExecutionException e) {
 			handleJobExecutionException(e, jobId);
 		} catch (ClassNotFoundException e) {
 			logger.log(Level.WARNING,
 					"Exception thrown submitting results of task for job "
 							+ jobId.toString(), e);
 			cancelScheduledJob(jobId);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jdcp.remote.JobService#getClassDefinition(java.lang.String, java.util.UUID)
 	 */
 	public byte[] getClassDefinition(String name, UUID jobId)
 			throws SecurityException {
 		ScheduledJob sched = jobs.get(jobId);
 		if (sched == null) {
 			throw new IllegalArgumentException("No job with provided Job ID");
 		}
 
 		ByteBuffer def = sched.classManager.getClassDefinition(name);
 		if (def.hasArray() && def.arrayOffset() == 0) {
 			return def.array();
 		} else {
 			byte[] bytes = new byte[def.remaining()];
 			def.get(bytes);
 			return bytes;
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jdcp.remote.JobService#getClassDigest(java.lang.String, java.util.UUID)
 	 */
 	public byte[] getClassDigest(String name, UUID jobId)
 			throws SecurityException {
 		ScheduledJob sched = jobs.get(jobId);
 		if (sched == null) {
 			throw new IllegalArgumentException("No job with provided Job ID");
 		}
 
 		return sched.classManager.getClassDigest(name);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jdcp.remote.JobService#getClassDigest(java.lang.String)
 	 */
 	public byte[] getClassDigest(String name) throws SecurityException {
 		return classManager.getClassDigest(name);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jdcp.remote.JobService#setClassDefinition(java.lang.String, byte[])
 	 */
 	public void setClassDefinition(String name, byte[] def)
 			throws SecurityException {
 		classManager.setClassDefinition(name, def);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jdcp.remote.JobService#setClassDefinition(java.lang.String, java.util.UUID, byte[])
 	 */
 	public void setClassDefinition(String name, UUID jobId, byte[] def)
 			throws IllegalArgumentException, SecurityException {
 		ScheduledJob sched = jobs.get(jobId);
 		if (sched == null || sched.job != null) {
 			throw new IllegalArgumentException("No pending job with provided Job ID");
 		}
 
 		sched.classManager.setClassDefinition(name, def);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jdcp.remote.JobService#setIdleTime(int)
 	 */
 	public void setIdleTime(int idleSeconds) throws IllegalArgumentException,
 			SecurityException {
 		idleTask = new TaskDescription(null, 0, idleSeconds);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jdcp.remote.JobService#setJobPriority(java.util.UUID, int)
 	 */
 	public void setJobPriority(UUID jobId, int priority)
 			throws IllegalArgumentException, SecurityException {
 		if (!jobs.containsKey(jobId)) {
 			throw new IllegalArgumentException("No job with provided Job ID");
 		}
 
 		scheduler.setJobPriority(jobId, priority);
 	}
 
 	private void handleJobExecutionException(JobExecutionException e, UUID jobId) {
 		logger.log(Level.WARNING, "Exception thrown from job " + jobId.toString(), e);
 		cancelScheduledJob(jobId);
 	}
 
 	private void cancelScheduledJob(UUID jobId) {
 		ScheduledJob sched = jobs.remove(jobId);
 		if (sched != null) {
 			sched.monitor.notifyCancelled();
 			jobs.remove(jobId);
 			scheduler.removeJob(jobId);
 			classManager.releaseChildClassManager(sched.classManager);
 		}
 	}
 
 	/**
 	 * Represents a <code>ParallelizableJob</code> that has been submitted
 	 * to this <code>JobMasterServer</code>.
 	 * @author bkimmel
 	 */
 	private class ScheduledJob implements Host {
 
 		/** The <code>ParallelizableJob</code> to be processed. */
 		public JobExecutionWrapper				job;
 
 		/** The <code>UUID</code> identifying the job. */
 		public final UUID						id;
 
 		/** A description of the job. */
 		public final String						description;
 
 		/** The <code>TaskWorker</code> to use to process tasks for the job. */
 		public Serialized<TaskWorker>				worker;
 
 		/**
 		 * The <code>ProgressMonitor</code> to use to monitor the progress of
 		 * the <code>Job</code>.
 		 */
 		public final ProgressMonitor			monitor;
 
 		/**
 		 * The <code>ClassManager</code> to use to store the class definitions
 		 * applicable to this job.
 		 */
 		public final ClassManager				classManager;
 
 		private final File						workingDirectory;
 
 		/**
 		 * Initializes the scheduled job.
 		 * @param description A description of the job.
 		 * @param monitor The <code>ProgressMonitor</code> from which to create a child
 		 * 		monitor to use to monitor the progress of the
 		 * 		<code>ParallelizableJob</code>.
 		 */
 		public ScheduledJob(String description, ProgressMonitor monitor) {
 
 			this.id					= UUID.randomUUID();
 			this.description		= description;
 
 			//String title			= String.format("%s (%s)", this.job.getClass().getSimpleName(), this.id.toString());
 			this.monitor			= monitor.createChildProgressMonitor(description);
 			this.monitor.notifyStatusChanged("Awaiting job submission");
 
 			this.classManager		= JobServer.this.classManager.createChildClassManager();
 
 			this.workingDirectory	= new File(outputDirectory, id.toString());
 
 		}
 
 		public void initializeJob(Serialized<ParallelizableJob> job) throws ClassNotFoundException, JobExecutionException {
 			ClassLoader loader	= new SandboxedStrategyClassLoader(classManager, JobServer.class.getClassLoader());
 			this.job			= new JobExecutionWrapper(job.deserialize(loader));
 			this.worker			= new Serialized<TaskWorker>(this.job.worker());
 			this.monitor.notifyStatusChanged("");
 
 			this.workingDirectory.mkdir();
 			this.job.initialize(this);
 		}
 
 		public boolean submitTaskResults(int taskId, Serialized<Object> results) throws ClassNotFoundException, JobExecutionException {
 			ClassLoader cl = job.getClass().getClassLoader();
 			Object task = scheduler.remove(id, taskId);
			job.submitTaskResults(task, results.deserialize(cl), monitor);
 
			if (job.isComplete()) {
				finalizeJob();
				return true;
 			}
 
 			return false;
 		}
 
 		public void scheduleNextTask() throws JobExecutionException {
			scheduler.add(id, job.getNextTask());
 		}
 
 		/**
 		 * Writes the results of a <code>ScheduledJob</code> to the output
 		 * directory.
 		 * @param sched The <code>ScheduledJob</code> to write results for.
 		 * @throws JobExecutionException
 		 */
 		private void finalizeJob() throws JobExecutionException {
 
 			assert(job.isComplete());
 
 			job.finish();
 
 			try {
 
 				String				filename		= String.format("%s.zip", id.toString());
 				File				outputFile		= new File(outputDirectory, filename);
 
 				File				logFile			= new File(workingDirectory, "job.log");
 				PrintStream			log				= new PrintStream(new FileOutputStream(logFile));
 
 				log.printf("%tc: Job %s completed.", new Date(), id.toString());
 				log.println();
 				log.flush();
 				log.close();
 
 				FileUtil.zip(outputFile, workingDirectory);
 
 			} catch (IOException e) {
 				logger.log(Level.WARNING, "Exception caught while finalizing job " + id.toString(), e);
 			}
 
 		}
 
 		private File getWorkingFile(String path) {
 			File file = new File(workingDirectory, path);
 			if (!FileUtil.isAncestor(file, workingDirectory)) {
 				throw new IllegalArgumentException("path must not reference parent directory.");
 			}
 			return file;
 		}
 
 		@Override
 		public FileOutputStream createFileOutputStream(String path) {
 			File file = getWorkingFile(path);
 			File dir = file.getParentFile();
 			dir.mkdirs();
 
 			try {
 				return new FileOutputStream(path);
 			} catch (FileNotFoundException e) {
 				throw new UnexpectedException(e);
 			}
 		}
 
 		@Override
 		public RandomAccessFile createRandomAccessFile(String path) {
 			File file = getWorkingFile(path);
 			File dir = file.getParentFile();
 			dir.mkdirs();
 
 			try {
 				return new RandomAccessFile(path, "rw");
 			} catch (FileNotFoundException e) {
 				throw new UnexpectedException(e);
 			}
 		}
 
 	}
 
 }
