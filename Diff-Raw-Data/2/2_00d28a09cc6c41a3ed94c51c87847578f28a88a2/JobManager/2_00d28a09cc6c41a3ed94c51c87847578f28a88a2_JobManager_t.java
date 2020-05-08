 package edu.illinois.cs.mapreduce;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import edu.illinois.cs.mapreduce.JobStatus.Phase;
 import edu.illinois.cs.mapreduce.Status.State;
 import edu.illinois.cs.mapreduce.api.InputFormat;
 import edu.illinois.cs.mapreduce.api.Partition;
 import edu.illinois.cs.mapreduce.api.Partitioner;
 
 /**
  * TODO recover from common failures <br>
  * TODO synchronize properly
  */
 public class JobManager implements JobManagerService {
 
     private final NodeID nodeId;
     private Cluster cluster;
     private final ExecutorService executorService;
     private final AtomicInteger counter;
     private final Map<JobID, Job> jobs;
 
     JobManager(NodeID id) {
         this.nodeId = id;
         this.counter = new AtomicInteger();
         this.jobs = new ConcurrentHashMap<JobID, Job>();
         this.executorService = Executors.newCachedThreadPool();
     }
 
     public void start(Cluster cluster) {
         this.cluster = cluster;
     }
 
     @Override
     public JobID submitJob(File jarFile, File inputFile) throws IOException {
         // 1. create job
         JobID jobId = new JobID(nodeId, counter.incrementAndGet());
         Job job = new Job(jobId, jarFile.getName());
         jobs.put(jobId, job);
         // 2. submit tasks
         scheduleMapTasks(job, jarFile, inputFile);
         return jobId;
     }
 
     private void scheduleMapTasks(Job job, File jarFile, File inputFile) throws IOException {
         JobDescriptor descriptor = JobDescriptor.read(jarFile);
         ClassLoader cl = new URLClassLoader(new URL[] {jarFile.toURI().toURL()});
         InputFormat<?, ?, ?> inputFormat;
         try {
             inputFormat = (InputFormat<?, ?, ?>)cl.loadClass(descriptor.getInputFormatClass()).newInstance();
         } catch (Exception e) {
             throw new IOException(e);
         }
 
         InputStream is = new FileInputStream(inputFile);
         try {
             Partitioner partitioner = inputFormat.createPartitioner(is, descriptor.getProperties());
             Set<NodeID> nodesWithJar = new HashSet<NodeID>();
             int num = 0;
             List<NodeID> nodeIds = cluster.getNodeIds();
             while (!partitioner.isEOF()) {
                 // 1. create sub task for the partition
                 TaskID taskId = new TaskID(job.getId(), num);
                 Path inputPath = job.getPath().append(taskId + "_input");
                 Task task = new Task(taskId, inputPath);
                 job.addMapTask(task);
 
                 // 2. chose node to run task on
                 // current selection policy: round-robin
                 // TODO: capacity-based selection policy
                 NodeID nodeId = nodeIds.get(num % nodeIds.size());
 
                 // 3. write partition to node's file system
                 FileSystemService fs = cluster.getFileSystemService(nodeId);
                 OutputStream os = fs.write(inputPath);
                 Partition partition;
                 try {
                     partition = partitioner.writePartition(os);
                 } finally {
                     os.close();
                 }
 
                 // 4. write job file if not already written
                 if (!nodesWithJar.contains(nodeId)) {
                     fs.copy(job.getJarPath(), jarFile);
                     nodesWithJar.add(nodeId);
                 }
 
                 // 5. create and submit task attempt
                 TaskAttemptID attemptID = new TaskAttemptID(taskId, task.nextAttemptID());
                 Path outputPath = job.getPath().append(attemptID.toQualifiedString(1) + "_output");
                 TaskAttempt attempt =
                     new TaskAttempt(attemptID, nodeId, job.getJarPath(), descriptor, partition, inputPath, outputPath);
                 task.addAttempt(attempt);
 
                 // 6. submit task
                 TaskExecutorService taskExecutor = cluster.getTaskExecutorService(nodeId);
                 taskExecutor.execute(new TaskAttempt(attempt));
 
                 num++;
             }
         } finally {
             is.close();
         }
     }
 
     private void scheduleReduceTasks(Job job) {
         job.getStatus().setPhase(Phase.REDUCE);
         // TODO
     }
 
     @Override
     public JobStatus getJobStatus(JobID jobID) throws IOException {
         JobStatus jobStatus = jobs.get(jobID).getStatus();
         return new JobStatus(jobStatus);
     }
 
     /*
      * makes use of the fact that statuses are sorted
      */
     @Override
     public boolean updateStatus(TaskAttemptStatus[] statuses) throws IOException {
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
 
     private boolean updateJobStatus(JobID jobId, TaskAttemptStatus[] statuses, int offset, int length) {
         final Job job = jobs.get(jobId);
         boolean stateChange = false;
         int off = offset, len = 1;
         TaskID taskId = statuses[offset].getTaskID();
         for (int i = offset + 1; i < offset + length; i++) {
             TaskID current = statuses[i].getTaskID();
             if (!current.equals(taskId)) {
                 stateChange |= updateTaskStatus(job.getMapTasks(taskId), statuses, off, len);
                 off = i;
                len = 1;
                 taskId = current;
             } else {
                 len++;
             }
         }
         stateChange |= updateTaskStatus(job.getMapTasks(taskId), statuses, off, len);
         // if any tasks have changed, recompute the job status
         if (stateChange)
             stateChange = job.updateStatus();
         // if map phase has completed successfully, start the reduce phase
         if (stateChange && job.getStatus().getState() == State.SUCCEEDED && job.getStatus().getPhase() == Phase.MAP)
             executorService.submit(new Runnable() {
                 @Override
                 public void run() {
                     scheduleReduceTasks(job);
                 }
             });
         return stateChange;
     }
 
     private boolean updateTaskStatus(Task task, TaskAttemptStatus[] statuses, int offset, int length) {
         boolean stateChange = false;
         for (int i = offset; i < offset + length; i++) {
             TaskAttemptStatus newStatus = statuses[i];
             TaskAttempt attempt = task.getAttempt(newStatus.getId());
             TaskAttemptStatus localStatus = attempt.getStatus();
             stateChange |= updateTaskAttemptStatus(localStatus, newStatus);
         }
         return stateChange ? task.updateStatus() : false;
     }
 
     private static boolean updateTaskAttemptStatus(TaskAttemptStatus localStatus, TaskAttemptStatus newStatus) {
         State localState = localStatus.getState();
         State newState = newStatus.getState();
         if (localState == newState)
             return false;
         localStatus.setState(newState);
         localStatus.setMessage(newStatus.getMessage());
         return true;
     }
 
 }
