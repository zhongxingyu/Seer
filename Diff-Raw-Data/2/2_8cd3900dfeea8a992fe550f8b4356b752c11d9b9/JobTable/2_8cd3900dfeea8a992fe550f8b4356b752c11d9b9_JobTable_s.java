 package os;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map.Entry;
 import java.util.Random;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 
 import job.Job;
 import job.JobStatus;
 
 
 /**
  * os/JobTable.java 
  * <br><br>
  * This class is generating identifiers in some range as PIDs and here are 
  * stored Jobs, which are running in our beautiful OS!
  * <br><br>
  * Each generated PID is <b>unique</b> within same instance of this class.
  * It is possible to release used IDs in order to use them again.
  * <br><br>
  * Uses <b>hashmap</b>, which is not thread-safe. But we dont care, because we do 
  * synchronization ourselves.
  * 
  * <i><br><br>
  * The set of processes on the Linux system is represented as a collection 
  * of struct task_struct structures which are linked in two ways: 
  *  	<li>as a hashtable, hashed by pid, and
  *  	<li>as a circular, doubly-linked list using p->next_task and p->prev_task
  * <br><br>
  * The hashtable is called pidhash[] and is defined in include/linux/sched.h:
  * </i>
  * 
  * @author Pavel Čurda
  * 
  * @team <i>OutOfMemory</i> for KIV/OS 2013
  * @teamLeader Radek Petruška radekp25@students.zcu.cz
  * 
  */
 public final class JobTable {
 	
 	/**
 	 * How maby jobs can we take from start.
 	 */
 	private static final int CAPACITY = 503;
 	/**
 	 * If we have more jobs than CAPACITY * LOAD_FACTOR, start remapping.
 	 * <br><br>
 	 * It should be in range (0;1>, default value is 0.75f.
 	 */
 	private static final float LOAD_FACTOR = 0.75f;
 	
 	/**
 	 * Number of Jobs, which are currently in OS.
 	 */
 	private int numberOfJobsNow;
 	/**
 	 * Everything has to own limit!
 	 */
 	private final int numberOfJobsMax;
 
 	/** minimum PID for normal jobs */
 	private final int PID_MIN;
 	/** maximum PID for normal jobs */
 	private final int PID_MAX;
 	/** Range of random which is added to last PID */
 	private final int PID_RANGE;
 	
 	private final Random random;
 	private int lastGenerated;
 	
 	/** The mighty hashTable */
 	private final HashMap<Integer, Job> map;
 	
 	/** Number of times when Name for new job was wrong */
 	private long statNotFound;
 	/** Number of times when we rejected to make new job */
 	private long statRejected;
 	/** Number of colisions in our random generator + hash table */
 	private long statPidTaken;
 	/** Number of Jobs ever created successfully. */
 	private long statPidGiven;
 	
 	/**
 	 * Multiple threads may want to create procces at once!
 	 */
 	private final ReentrantReadWriteLock lock;
 
 	
 	
 	/**
 	 * Prepares the table.
 	 * <br><br>
 	 * The user's jobs PIDs have to be in range from pidMin to pidMax. There can
 	 * be maxJobsCount at the same time in the table.
 	 * 
 	 * @param pidMin 
 	 * @param pidMax
 	 * @param maxJobsCount
 	 */
 	public JobTable(int pidMin, int pidMax, int maxJobsCount) {
 
 		this.map = new HashMap<Integer, Job>(CAPACITY, LOAD_FACTOR);
 		
 		this.numberOfJobsMax = maxJobsCount;
 		
 		this.PID_MAX = pidMax;
 		this.PID_MIN = pidMin;
 		this.PID_RANGE = 11;
 		
 		this.statNotFound = 0;
 		this.statPidTaken = 0;
 		this.statRejected = 0;
 		this.statPidGiven = 0;
 		
 		//TODO after all debug delete this.
 		this.random = new Random(58750120000003l);
 		this.lastGenerated = this.PID_MIN;
 		
 		//TODO add read/write locks for multiple shells at once!
 		this.lock = new ReentrantReadWriteLock();
 		
 		
 	}
 	
 	/**
 	 * Create new job by its name and puts him in map of running jobs.
 	 * THREAD SAFE.
 	 * 
 	 * @param parentPID PID of the parent. Can be null.
 	 * @param nameOfJob Name of the class you want to create instance of.
 	 * @param stdErr Pipe for writing errors.
 	 * 
 	 * @return PID of new job or NULL if we cant create any more jobs.
 	 * 
 	 * @throws ClassNotFoundException If you give wrong class name.
 	 * @throws InterruptedException SIG_KILL while making new job.
 	 */
 	public Integer createJob(Integer parentPID, String nameOfJob, Pipe stdErr) 
 			throws InterruptedException, ClassNotFoundException{
 		
 		this.lock.writeLock().lockInterruptibly();
 		
 		if (this.numberOfJobsNow == this.numberOfJobsMax){
 			this.statRejected++;
 			this.lock.writeLock().unlock();
 			return(null);
 		}
 		
 		int idNew;
 		Integer id;
 		
 		if (this.lastGenerated > (this.PID_MAX + this.PID_RANGE + 2)){
 			//Reset PID.
 			this.lastGenerated = this.PID_MIN;
 		}
 		
 		
 		while(true){
 			
 			/** 1 - 11 */
 			idNew = Math.abs(this.random.nextInt()) % this.PID_RANGE + 1;
 			idNew += this.lastGenerated;
 			id = Integer.valueOf(idNew);
 			
 			if(this.map.containsKey(id)){
 				//Try again.
 				this.statPidTaken++;
 				/** Creators of HASHmap boast that this operation is O(1) */
 			} else {
 				//We have new ID!
 				this.lastGenerated = idNew;
 				break;
 			}
 		}
 		
 		
 		try {
 			/** we have the ID ready, now lets see the class. */
 			Job newJob = findJobByName(nameOfJob, id, stdErr);
 			
 			if (newJob == null){
 				System.err.println("ERROR in JobTable 001!");
 				this.statRejected++;
 				this.lock.writeLock().unlock();
 				return(null);
 			}
 			
 			
 			if (parentPID == null){
 				newJob.setParentPID(Init.initPID);
 			} else {
 				newJob.setParentPID(parentPID);
 			}
 			
 			this.map.put(id, newJob);
 			
 			this.numberOfJobsNow++;
 			this.statPidGiven++;
 			this.lock.writeLock().unlock();
 			return(id);
 			
 			
 		} catch (ClassNotFoundException e) {
 			this.lock.writeLock().unlock();
 			throw new ClassNotFoundException();
 		}
 	}
 	
 	
 	
 	
 	/**
 	 * Try to find any class extended by Job by its name.
 	 * 
 	 * @param name of class (job.Echo)
 	 * @param ID (PID for the new process)
 	 * @param stdErr (Pipe when the job will get an error)
 	 * 
 	 * @return New instance of such class.
 	 * 
 	 * @throws ClassNotFoundException if your name is wrong.
 	 */
 	public static Job findJobByName(String name, Integer ID, Pipe stdErr) 
 			throws ClassNotFoundException{
 		
 		try{
 			Class<?> jobAny = Class.forName(name);
 			Class<?>[] conTypes = {Integer.class, Pipe.class};
 				
 			Constructor<?> construct = jobAny.getConstructor(conTypes);
 			Object [] parameters = {ID, stdErr};
 				
 			Object myClass = construct.newInstance(parameters);	
 			return ((Job) (myClass));
 		} catch (SecurityException e) {
 			e.printStackTrace();
 		} catch (NoSuchMethodException e) {
 			//nothing
 		} catch (IllegalArgumentException e) {
 			//nothing
 		} catch (InstantiationException e) {
 			//nothing
 		} catch (IllegalAccessException e) {
 			//nothing
 		} catch (InvocationTargetException e) {
 			//nothing
 		} catch (ClassNotFoundException e) {
 			//nothing
 		} catch (Exception e) {
 			e.printStackTrace();
 
 		}
 		
 		throw new ClassNotFoundException("Not found or wrong class: " + name);
 		
 	}
 
 	/**
 	 * Returns informations about created Jobs and how well did it go.
 	 * 
 	 * @return All statistic about Jobs in one big String.
 	 */
 	public String statistic() {
 
 		StringBuilder text = new StringBuilder();
 		text.append("\n##################\n");
 		
 		text.append("\nTotal PIDs given      : " + this.statPidGiven);
 		text.append("\nTotal PIDs colisions  : " + this.statPidTaken);
 		text.append("\n");
 		text.append("\nTotal Jobs rejected   : " + this.statRejected);
 		text.append("\nTotal Jobs not found  : " + this.statNotFound);
 		
 		text.append("\n##################\n");
 		return (text.toString());
 	}
 	
 	/**
 	 * Add systemJob to jobTable. Used only by OS.
 	 * 
 	 * @param systemJob to add.
 	 */
 	public void addJob(Job systemJob){
 		
 		this.lock.writeLock().lock();
 		
 		this.map.put(systemJob.PID, systemJob);
 		this.numberOfJobsNow++;
 	
 		this.lock.writeLock().unlock();
 	}
 	
 
 
 	/**
 	 * Sets every PIPE and arguments for specified PID.
 	 * <br><br>
 	 * You can set these only when job is in status NEW. It is possible to close
 	 * Pipes if you set them NULL (only StdErr will  stay open).
 	 * 
 	 * @param PID of Process you want set.
 	 * @param stdIn
 	 * @param stdOut
 	 * @param stdErr
 	 * @param args
 	 * @return 0 if OK; - 1 if Job isnt NEW; -2 if PID is invalid.
 	 * @throws InterruptedException 
 	 */
 	public int setPipes(Integer PID, Pipe stdIn, Pipe stdOut, Pipe stdErr, 
 			String [] args) throws InterruptedException{
 		
 		this.lock.readLock().lockInterruptibly();
 		
 		Job job = this.map.get(PID);
 		
 		if(job == null){
 			this.lock.readLock().unlock();
 			return(-2);
 		}
 		
 		if(job.getStatus() != JobStatus.NEW){
 			this.lock.readLock().unlock();
 			return(-1);
 		}
 		
 		
 		job.setStdIn(stdIn);
 		job.setStdOut(stdOut);
 		job.setStdErr(stdErr);
 		job.setArguments(args);
 		
 		this.lock.readLock().unlock();
 		return(0);
 	}
 	
 	/**
 	 * Sets output PIPE for specified PID.
 	 * <br><br>
 	 * You can set these only when job is in status NEW. It is possible to close
 	 * Pipes if you set them NULL (only StdErr will  stay open).
 	 * 
 	 * @param PID of Process you want set.
 	 * @param stdOut
 	 * @return 0 if OK; - 1 if Job isnt NEW; -2 if PID is invalid.
 	 * @throws InterruptedException 
 	 */
 	public int setOutputPipe(Integer PID, Pipe stdOut) throws InterruptedException{
 		
 		this.lock.readLock().lockInterruptibly();
 		
 		Job job = this.map.get(PID);
 		
 		if(job == null){
 			this.lock.readLock().unlock();
 			return(-2);
 		}
 		
 		if(job.getStatus() != JobStatus.NEW){
 			this.lock.readLock().unlock();
 			return(-1);
 		}
 		
 		job.setStdOut(stdOut);
 		
 		this.lock.readLock().unlock();
 		return(0);
 	}
 	
 	/**
 	 * Sets input PIPE for specified PID.
 	 * <br><br>
 	 * You can set these only when job is in status NEW. It is possible to close
 	 * Pipes if you set them NULL (only StdErr will  stay open).
 	 * 
 	 * @param PID of Process you want set.
 	 * @param stdIn
 	 * @return 0 if OK; - 1 if Job isnt NEW; -2 if PID is invalid.
 	 * @throws InterruptedException 
 	 */
 	public int setInputPipe(Integer PID, Pipe stdIn) throws InterruptedException{
 		
 		this.lock.readLock().lockInterruptibly();
 		
 		Job job = this.map.get(PID);
 		
 		if(job == null){
 			this.lock.readLock().unlock();
 			return(-2);
 		}
 		
 		if(job.getStatus() != JobStatus.NEW){
 			this.lock.readLock().unlock();
 			return(-1);
 		}
 		
 		job.setStdIn(stdIn);
 		
 		this.lock.readLock().unlock();
 		return(0);
 	}
 
 	/**
 	 * @param PID to be checked.
 	 * @return true, if PID is in range of user's jobs.
 	 */
 	public boolean isOk(Integer PID) {
 
 		int pid = PID.intValue();
 		
 		if (pid < this.PID_MIN){
 			return (false);
 		}
 		
 		if (pid > this.PID_MAX){
 			return (false);
 		}
 		
 		return (true);
 	}
 	
 	/**
 	 * Start the job (from NEW to RUNNING). After this point you can not 
 	 * touch job's pipes.
 	 * 
 	 * @param PID of Job you want to start.
 	 * @return 0 if OK; - 1 if Job isnt NEW; -2 if PID is invalid.
 	 * @throws InterruptedException 
 	 */
 	public int startJob(Integer PID) throws InterruptedException{
 		
 		this.lock.readLock().lockInterruptibly();
 		
 		Job job = this.map.get(PID);
 		
 		if(job == null){
 			this.lock.readLock().unlock();
 			return(-2);
 		}
 		
 		if(job.getStatus() != JobStatus.NEW){
 			this.lock.readLock().unlock();
 			return(-1);
 		}
 		
 		job.live();
 		this.lock.readLock().unlock();
 		return(0);
 	}
 	
 	/**
 	 * Returns man page for some job.
 	 * 
 	 * @param name of the job.
 	 * @return String.
 	 * @throws ClassNotFoundException if name of job is bad.
 	 */
 	public static String getManual(String name) throws ClassNotFoundException{
 		
 		Job job = findJobByName(name, Integer.valueOf(0), null);
 		return(job.getManual());
 		
 	}
 	
 	/**
 	 * Sends the SIG_KILL to some job by PID.
 	 * 
 	 * @param PID of job to kill.
 	 * @return 0 if OK; -2 if PID is invalid.
 	 * @throws InterruptedException
 	 */
 	public int killJob(Integer PID) throws InterruptedException{
 		
 		this.lock.readLock().lockInterruptibly();
 		
 		Job job = this.map.get(PID);
 		
 		if(job == null){
 			this.lock.readLock().unlock();
 			return(-2);
 		}
 		
 		
 		job.SIG_KILL();
 		this.lock.readLock().unlock();
 		return(0);
 	}
 	
 	
 	/**
 	 * Returns the instance of Job by its PID.
 	 * 
 	 * @param PID of the job to return.
 	 * @return null, if there is no such job.
 	 * @throws InterruptedException SIG_KILLED while waiting.
 	 */
 	public Job getJob(Integer PID) throws InterruptedException{
 		
 		this.lock.readLock().lockInterruptibly();
 		
 		Job job = this.map.get(PID);
 		
 		this.lock.readLock().unlock();
 		
 		return(job);
 	}
 	
 	/**
 	 * Blocking operation: waits for any job (by PID) to complete or to be 
 	 * SIG_KILLed. 
 	 * 
 	 * @param PID of the job.
 	 * @return 0 if job has finished. -1 if the job wasn't running.
 	 * @throws InterruptedException
 	 */
 	public int joinJob(Integer PID) throws InterruptedException{
 		
 		this.lock.readLock().lockInterruptibly();
 		Job job = this.map.get(PID);
 		this.lock.readLock().unlock();
 		
 		if(job == null){
 				/**
 				 * The job has finished and watchDog deleted it from table.
 				 * This is rare case. OR the PID was invalid.
 				 */
 				return(0);
 		}
 		
 		if(job.getStatus() == JobStatus.NEW){
 			// This can make deadLock: Joining thread, which is not running.
 			return(-1);
 		}
 		
 		job.join();
 		return(0);
 	}
 
 	/**
 	 * Deletes any KILLED / ENDED jobs from table and releases PIDs.
 	 * <br> O(n).
 	 * 
 	 * @throws InterruptedException 
 	 */
 	public void cleanDeadJobs() throws InterruptedException {
 
 		this.lock.writeLock().lockInterruptibly();
 		
 		Iterator<Entry<Integer, Job>> iter = this.map.entrySet().iterator();
 		int size = this.map.size();
 		Job job;
 		
 		for (int i = 0; i < size; i++){
 			
 			job = iter.next().getValue();
 			
 			if(job.getStatus().isFinished()){
 				iter.remove();
 				this.numberOfJobsNow--;
 			}
 			
 		}
 		
 		
 		this.lock.writeLock().unlock();
 		
 	}
 	
 	/**
 	 * Write all jobs to System.out in some clever way. Use only for debugging.
 	 * 
 	 * @throws InterruptedException
 	 */
 	public void writeAllJobs() {
 		
 		//TODO 
 		this.lock.readLock().lock();
 		
 		Iterator<Entry<Integer, Job>> iter = this.map.entrySet().iterator();
 		int size = this.map.size();
 		Job job;
 		
 		System.out.println("PID     State      Name");
 		for (int i = 0; i < size; i++){
 			
 			job = iter.next().getValue();
 			
 			System.out.println(job);
 			
 		}
 		System.out.println("\nJobs: " + this.numberOfJobsNow);
 		
 		this.lock.readLock().unlock();
 		
 	}
 	
 	/**
 	 * Makes a list oj running jobs in some formatted way. Great for Job PS.
 	 * 
 	 * @param corpsesToo TRUE if you want to write even dead Jobs.
 	 * @return list of jobs in JobTable in String representation.
 	 * @throws InterruptedException
 	 */
 	public String getAllJobs(boolean corpsesToo) throws InterruptedException{
 		
 		StringBuilder report = new StringBuilder(500);
 		
 		this.lock.readLock().lockInterruptibly();
 		
 		Iterator<Entry<Integer, Job>> iter = this.map.entrySet().iterator();
 		int size = this.map.size();
 		Job job;
 		
 		report.append("PID     State      Name\n");
 		int count = 0;
 		
 		if(corpsesToo){
 			
 			for (int i = 0; i < size; i++){
 				
 				job = iter.next().getValue();
 				report.append(job.toString());
 				report.append("\n");
 				count++;
 				
 			}
 			
 		} else {
 			
 			
 			for (int i = 0; i < size; i++){
 				
 				job = iter.next().getValue();
 				
 				if(job.getStatus().isFinished()){
 					//ignore dead jobs.
 				} else {
 					report.append(job.toString());
 					report.append("\n");
 					count++;
 				}
 			}
 			
 		}
 		
 
 		report.append("\nJobs: " + count);
 		
 		this.lock.readLock().unlock();
 		return(report.toString());
 		
 	}
 
 	/**
 	 * Kills all jobs except the system's ones.
 	 */
 	public void killAllJobs() {
 		
 		this.lock.readLock().lock();
 		
 		Iterator<Entry<Integer, Job>> iter = this.map.entrySet().iterator();
 		int size = this.map.size();
 		Job job;
 		
 		for (int i = 0; i < size; i++){
 			
 			job = iter.next().getValue();
 			
 			if(job.getStatus().isFinished()){
 				//ignore finished jobs
 				continue;
 			}
 			
 			if(isOk(job.PID)){
 				// is user space Job
 				job.SIG_KILL();
 			}
 			
 		}
 		
 		this.lock.readLock().unlock();
 
 	}
 		
 	
 	
 }
