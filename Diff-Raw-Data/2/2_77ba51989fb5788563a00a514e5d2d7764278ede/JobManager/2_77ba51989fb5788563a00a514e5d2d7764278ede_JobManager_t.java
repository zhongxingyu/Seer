 package edu.illinois.cs.mr.jm;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PipedInputStream;
 import java.io.PipedOutputStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import edu.illinois.cs.mapreduce.api.InputFormat;
 import edu.illinois.cs.mapreduce.api.Partition;
 import edu.illinois.cs.mapreduce.api.Partitioner;
 import edu.illinois.cs.mr.Node;
 import edu.illinois.cs.mr.NodeConfiguration;
 import edu.illinois.cs.mr.NodeID;
 import edu.illinois.cs.mr.fs.FileSystemService;
 import edu.illinois.cs.mr.fs.Path;
 import edu.illinois.cs.mr.fs.QualifiedPath;
 import edu.illinois.cs.mr.te.TaskExecutorMapTask;
 import edu.illinois.cs.mr.te.TaskExecutorReduceTask;
 import edu.illinois.cs.mr.te.TaskExecutorService;
 import edu.illinois.cs.mr.util.ReflectionUtil;
 import edu.illinois.cs.mr.util.Status.State;
 
 /**
  * This job manager splits jobs into individual tasks and distributes them to
  * the TaskExecutors in the cluster. It derives job status from periodic status
  * updates from the TaskExecutors.
  */
 public class JobManager implements JobManagerService {
 
     private final NodeConfiguration config;
     private final AtomicInteger counter;
     private final Map<JobID, Job> jobs;
     private Node node;// quasi immutable
 
     public JobManager(NodeConfiguration config) {
         this.config = config;
         this.counter = new AtomicInteger();
         this.jobs = new TreeMap<JobID, Job>();
     }
 
     @Override
     public void start(Node node) {
         this.node = node;
     }
 
     @Override
     public void stop() {
         // nothing to do
     }
 
     /**
      * Returns an array of job IDs
      * 
      * @return
      */
     public JobID[] getJobIDs() {
         synchronized (jobs) {
            return jobs.keySet().toArray(new JobID[0]);
         }
     }
 
     /**
      * @see {@link edu.illinois.cs.mapreduce.JobMangerService#}
      */
     @Override
     public JobStatus getJobStatus(JobID jobID) throws IOException {
         Job job;
         synchronized (jobs) {
             job = jobs.get(jobID);
         }
         return job.toImmutableStatus();
     }
 
     public Iterable<JobStatus> getJobStatuses() {
         synchronized (jobs) {
             Collection<Job> jobList = jobs.values();
             Collection<JobStatus> statuses = new ArrayList<JobStatus>(jobList.size());
             for (Job job : jobList)
                 statuses.add(job.toImmutableStatus());
             return statuses;
         }
     }
 
     /**
      * @see edu.illinois.cs.mr.jm.JobManagerService#submitJob(java.io.File,
      *      java.io.File)
      */
     @Override
     public JobID submitJob(File jarFile, File inputFile) throws IOException {
         // 1. create job
         JobDescriptor descriptor = JobDescriptor.read(jarFile);
         JobID jobId = new JobID(config.nodeId, counter.incrementAndGet());
         Job job = new Job(jobId, jarFile.getName(), descriptor);
         synchronized (jobs) {
             jobs.put(jobId, job);
         }
         // 2. submit tasks
         try {
             submitMapTasks(job, jarFile, inputFile);
         } catch (Throwable t) {
             job.setState(State.FAILED);
             if (t instanceof IOException)
                 throw (IOException)t;
             if (t instanceof RuntimeException)
                 throw (RuntimeException)t;
             if (t instanceof Error)
                 throw (Error)t;
             throw new RuntimeException(t);
         }
         return jobId;
     }
 
     /**
      * <p>
      * Tasks are created on the same thread, because we need data from the
      * inputFile, but do not want to assume that it will still exist after
      * submitJob returns. I.e. a user is free to delete or edit the input file
      * after submitting the job. We also do not want to create a copy of the
      * file, because we want to support very large input files, so partitioning
      * it directly is faster.
      * </p>
      * <p>
      * Task attempts can be submitted as soon as the necessary data is copied
      * and the task attempt is registered with the job. However, we only submit
      * attempts after the next attempt has been created and registered.
      * Otherwise, if all currently registered attempts complete quickly before
      * scheduling the next one, the JobManager may think the map phase has
      * completed and schedule the reducer.
      * </p>
      * 
      * @param job
      * @param jarFile
      * @param inputFile
      * @throws IllegalAccessException
      * @throws InstantiationException
      * @throws ClassNotFoundException
      * @throws IOException
      * @throws InterruptedException 
      */
     private void submitMapTasks(Job job, File jarFile, File inputFile) throws IOException, InterruptedException {
         JobDescriptor descriptor = job.getDescriptor();
         InputFormat<?, ?, ?> inputFormat = ReflectionUtil.newInstance(descriptor.getInputFormatClass(), jarFile);
         InputStream is = new FileInputStream(inputFile);
         try {
             final Partitioner<?> partitioner = inputFormat.createPartitioner(is, descriptor.getProperties());
             Set<NodeID> nodesWithJar = new HashSet<NodeID>();
             int num = 0;
             MapTask previous = null;
             Attempt previousAttempt = null;
             while (!partitioner.isEOF()) {
                 // 1. ask load balancer which node to place task on
                 NodeID targetNodeId = node.getLoadBalancer().selectNode();
 
                 // 2. create task ID for the partition
                 TaskID taskId = new TaskID(job.getId(), num, true);
                 Path inputPath = job.getPath().append(taskId + "-input");
 
                 // 3. write partition to node's file system
                 final FileSystemService fs = node.getFileSystemService(targetNodeId);
                 Partition partition = writePartition(partitioner, inputPath, fs);
 
                 // 4. write job file if not already written
                 if (!nodesWithJar.contains(targetNodeId)) {
                     InputStream fis = new FileInputStream(jarFile);
                     try {
                         fs.write(job.getJarPath(), fis);
                     } finally {
                         fis.close();
                     }
                     nodesWithJar.add(targetNodeId);
                 }
 
                 // 5. create and register task
                 MapTask task = new MapTask(taskId, partition, inputPath);
                 job.addTask(task);
 
                 // 6. create and register task attempt
                 AttemptID attemptID = task.nextAttemptID();
                 Path outputPath = job.getPath().append(attemptID.toQualifiedString(1) + "-output");
                 Attempt attempt = new Attempt(attemptID, targetNodeId, outputPath);
                 task.addAttempt(attempt);
 
                 // 7. submit previous task
                 if (previous != null)
                     submitMapTaskAttempt(job, previous, previousAttempt);
                 previous = task;
                 previousAttempt = attempt;
                 num++;
             }
             // submit last task attempt
             if (previous != null)
                 submitMapTaskAttempt(job, previous, previousAttempt);
         } finally {
             is.close();
         }
     }
 
     /**
      * the only purpose of this complex chunk of code is to turn the
      * OutputStream the partitioner needs into an InputStream for the file
      * system
      * @throws InterruptedException 
      */
     private Partition writePartition(final Partitioner<?> partitioner, Path inputPath, final FileSystemService fs)
         throws IOException, InterruptedException {
         Partition partition;
         final PipedOutputStream pos = new PipedOutputStream();
         try {
             final PipedInputStream pis = new PipedInputStream(pos);
             Future<Partition> future = node.getExecutorService().submit(new Callable<Partition>() {
                 @Override
                 public Partition call() throws IOException {
                     try {
                         return partitioner.writePartition(pos);
                     } finally {
                         pos.close();
                     }
                 }
             });
             fs.write(inputPath, pis);
             try {
                 partition = future.get();
             } catch (ExecutionException e) {
                 Throwable t = e.getCause();
                 if (t instanceof IOException)
                     throw (IOException)t;
                 if (t instanceof Error)
                     throw (Error)t;
                 if (t instanceof RuntimeException)
                     throw (RuntimeException)t;
                 throw new RuntimeException(e);
             }
         } finally {
             pos.close();
         }
         return partition;
     }
 
     private void submitMapTaskAttempt(Job job, MapTask task, Attempt attempt) throws IOException {
         TaskExecutorService taskExecutor = node.getTaskExecutorService(attempt.getTargetNodeID());
         taskExecutor.execute(new TaskExecutorMapTask(attempt.getId(), job.getJarPath(), job.getDescriptor(), attempt
             .getOutputPath(), attempt.getTargetNodeID(), task.getPartition(), task.getInputPath()));
     }
 
     /**
      * Currently we schedule one reduce task once all map tasks have completed.
      * The framework could be extended to support multiple reducers. In that
      * case each job would output multiple output files that the user would have
      * to aggregate. The map output files are not copied here, but by the reduce
      * task itself when run.
      * 
      * @param job
      * @throws IOException
      */
     private void submitReduceTasks(Job job) throws IOException {
         // 1. create reduce task
         TaskID taskID = new TaskID(job.getId(), 1, false);
         // collect all map output paths
         List<QualifiedPath> inputPaths = new ArrayList<QualifiedPath>();
         synchronized (job) {
             for (MapTask mapTask : job.getMapTasks()) {
                 Attempt attempt = mapTask.getSuccessfulAttempt();
                 if (attempt == null)
                     throw new IllegalStateException("Map task " + mapTask.getId()
                         + " does not have a succeeded attempt");
                 QualifiedPath qPath = new QualifiedPath(attempt.getTargetNodeID(), attempt.getOutputPath());
                 inputPaths.add(qPath);
             }
         }
         ReduceTask task = new ReduceTask(taskID, inputPaths);
 
         // 2. create attempt
         AttemptID attemptId = new AttemptID(taskID, 1);
         Path outputPath = job.getPath().append("output");
         Attempt attempt = new Attempt(attemptId, config.nodeId, outputPath);
         task.addAttempt(attempt);
 
         // 3. register and submit task
         job.addTask(task);
         submitReduceTaskAttemp(job, task, attempt);
     }
 
     private void submitReduceTaskAttemp(Job job, ReduceTask task, Attempt attempt) throws IOException {
         TaskExecutorService taskExecutor = node.getTaskExecutorService(attempt.getTargetNodeID());
         taskExecutor.execute(new TaskExecutorReduceTask(attempt.getId(), job.getJarPath(), job.getDescriptor(), attempt
             .getOutputPath(), attempt.getTargetNodeID(), task.getInputPaths()));
     }
 
     /**
      * Updates all local jobs with the attempt status sent by a remote node. The
      * contract requires that attempt status objects be sorted by id.
      */
     @Override
     public boolean updateStatus(AttemptStatus[] statuses) throws IOException {
         boolean stateChange = false;
         if (statuses.length > 0) {
             int offset = 0, len = 1;
             JobID jobId = statuses[0].getJobID();
             for (int i = 1; i < statuses.length; i++) {
                 JobID current = statuses[i].getJobID();
                 if (!current.equals(jobId)) {
                     stateChange |= updateJobStatus(jobId, statuses, offset, len);
                     offset = i;
                     len = 1;
                     jobId = current;
                 } else {
                     len++;
                 }
             }
             stateChange |= updateJobStatus(jobId, statuses, offset, len);
         }
         return stateChange;
     }
 
     /**
      * Updates the status of the job for the given ID and schedules a task to
      * start the job's reduce phase if the job was transitioned into the reduce
      * phase.
      * 
      * @param jobId
      * @param statuses
      * @param offset
      * @param length
      * @return
      * @throws IOException
      */
     private boolean updateJobStatus(JobID jobId, AttemptStatus[] statuses, int offset, int length) throws IOException {
         boolean stateChanged;
         final Job job;
         synchronized (jobs) {
             job = jobs.get(jobId);
         }
         synchronized (job) {
             Phase phase = job.getPhase();
             stateChanged = job.updateStatus(statuses, offset, length);
             if (stateChanged && phase != job.getPhase()) {
                 node.getExecutorService().submit(new Runnable() {
                     @Override
                     public void run() {
                         try {
                             submitReduceTasks(job);
                         } catch (IOException e) {
                             e.printStackTrace();
                         }
                     }
                 });
             }
         }
         return stateChanged;
     }
 
     /**
      * transfer a task attempt from current node to a different one
      * 
      * @param busy
      * @param free
      * @throws IOException
      */
     public void migrateTask(NodeID target, AttemptStatus attempt) throws IOException {
         Job job;
         synchronized (jobs) {
             job = jobs.get(attempt.getJobID());
         }
         TaskID taskId = attempt.getTaskID();
         if (!taskId.isMap())
             throw new IllegalArgumentException("Can currently only transfer map tasks");
         MapTask task = (MapTask)job.getMapTask(taskId);
 
         NodeID source = attempt.getTargetNodeID();
         FileSystemService sourceFs = node.getFileSystemService(source);
         FileSystemService targetFs = node.getFileSystemService(target);
 
         // make sure jar is present
         Path jarPath = job.getJarPath();
         if (!targetFs.exists(jarPath))
             targetFs.write(jarPath, sourceFs.read(jarPath));
 
         // make sure input file is present
         Path inputPath = task.getInputPath();
         if (!targetFs.exists(inputPath))
             targetFs.write(inputPath, sourceFs.read(inputPath));
 
         // create a new attempt
         AttemptID attemptID = task.nextAttemptID();
         Path outputPath = job.getPath().append(attemptID.toQualifiedString(1) + "-output");
         Attempt newAttempt = new Attempt(attemptID, target, outputPath);
         task.addAttempt(newAttempt);
 
         submitMapTaskAttempt(job, task, newAttempt);
         try {
             node.getTaskExecutorService(source).cancel(attempt.getId(), 10, TimeUnit.SECONDS);
         } catch (TimeoutException e) {
             // ignore
             e.printStackTrace();
         }
         return;
     }
 }
