 package cmu.ds.mr.mapred;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.rmi.NotBoundException;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 
 import cmu.ds.mr.conf.JobConf;
 import cmu.ds.mr.io.FileSplit;
 import cmu.ds.mr.io.FileSplitter;
 import cmu.ds.mr.util.Log;
 import cmu.ds.mr.util.Util;
 
 /**
  * JobClient can 1. Configure job 2. Kill job 3. Query progress or status
  * */
 public class JobClient {
 
   private static final Log LOG = new Log("JobClient.class");
 
   private JobConf jobConf;
   private JobSubmissionProtocol jobTrackerProxy;
   private Job job; // Implementation of RunningJob
 
   private String sysDir; // root directory of all job related files
 
   private Properties prop = new Properties();
 
   
   public JobClient(){}
   
   public JobClient(JobConf jobConf) throws FileNotFoundException, IOException, NotBoundException {
     super();
     this.jobConf = jobConf;
     readConfig();
     // initialize remote object (jobTrackerProxy)
     initProxy();
   }
   
   public void initProxy() throws RemoteException, NotBoundException {
     if (System.getSecurityManager() == null) {
       System.setSecurityManager(new SecurityManager());
     }
     // get job tracker start address fomr jobConf
    Registry registry = LocateRegistry.getRegistry("localhost");
     
     //TODO: TEST
     //Registry registry = LocateRegistry.getRegistry(jobConf.getJobTrackerAddr());
     
     
     
     jobTrackerProxy = (JobSubmissionProtocol) registry.lookup(Util.SERVICE_NAME);
   }
 
   public void readConfig() throws FileNotFoundException, IOException {
     prop.load(new FileInputStream(Util.CONFIG_PATH));
   }
 
   public static RunningJob runJob(JobConf jobConf) throws IOException, InterruptedException, NotBoundException {
     JobClient jc = new JobClient(jobConf);
     RunningJob job = jc.submitJob(jobConf);
 
     // query status and state every second
     if (!jc.monitorAndPrintJob(jobConf, job)) {
       throw new IOException("Job failed!");
     }
     // job done
     return job;
   }
 
   /**
    * Monitor and print job status
    * */
   private boolean monitorAndPrintJob(JobConf jobConf, RunningJob job) throws IOException,
           InterruptedException {
     JobID jid = job.getID();
     String logstrPre = "";
 
     while (!job.isComplete()) {
       Thread.sleep(Util.TIME_INTERVAL_MONITOR);
 
       // ask job tracker for new job
       JobStatus jobStatusNew = jobTrackerProxy.getJobStatus(jid);
       job.setJobStatus(jobStatusNew);
 
       String logstr = String.format("%s: map %.1f\treduce %.1f", jid.toString(),
               job.mapProgress(), job.reduceProgress());
       if(!logstr.equals(logstrPre)) {
         LOG.info(logstr);
         System.out.println(logstr);
         logstrPre = logstr;
       }
     }
     return true;
   }
 
   public RunningJob submitJob(JobConf jobConf) throws IOException {
     // step 2: get new job ID
     JobID jid = jobTrackerProxy.getNewJobId();
     System.out.println(jid.getId());
     
     // TODO: split input files
     //String jobRootDir = getSystemDir().toString() + File.separatorChar + jid.toString();
     FileSplitter splitter = new FileSplitter();
     List<FileSplit> splitFiles = splitter.getSplits(jobConf);
     jobConf.setSplitFiles(splitFiles);
     jobConf.setNumMapTasks(splitFiles.size());  // set # of maps 
 
     // step 3: submit job
     JobStatus status = jobTrackerProxy.submitJob(jid, jobConf);
     if (status != null) {
       // return a RunningJob (Job class)
       return new Job(jid, jobConf, status);
     } else {
       LOG.error("Could not launch job");
       throw new IOException("Could not launch job");
     }
   }
  
   /**
    * Get the root directory of
    * */
 //  public String getSystemDir() {
 //    return prop.getProperty(Util.SYS_ROOT_DIR);
 //  }
 //
 //  
 //  public static void main(String[] args) {
 //    JobConf jobconf = new JobConf();
 //  }
 }
