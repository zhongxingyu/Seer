 package nz.org.nesi.appmanage.model;
 
 import com.beust.jcommander.internal.Lists;
 import nz.org.nesi.appmanage.exceptions.AppFileException;
 import org.apache.commons.lang.StringUtils;
 
 import java.io.File;
 import java.util.List;
 
 /**
  * Project: Applications
  * <p/>
  * Written by: Markus Binsteiner
  * Date: 15/11/13
  * Time: 3:11 PM
  */
 public class Jobs {
 
     public static final String JOB_FOLDER = "jobs";
 
     private final File appRoot;
     private final File appFolder;
     private final File jobsFolder;
 
     private final List<Job> jobs = Lists.newLinkedList();
 
     public Jobs(File appFolder, File appRoot) {
         if (appRoot == null || !appRoot.exists() || !appRoot.isDirectory()) {
             throw new AppFileException("Application root '" + appRoot.getAbsolutePath() + "' not valid");
         }
         if (appFolder == null || !appFolder.isDirectory() || ".git".equals(appFolder.getName())
                 || "scripts".equals(appFolder.getName()) || "logs".equals(appFolder.getName())) {
             throw new AppFileException("Application folder '" + appFolder.getAbsolutePath() + "' not valid");
         }
         this.appRoot = appRoot;
         this.appFolder = appFolder;
         this.jobsFolder = new File(appFolder, JOB_FOLDER);
 
         File[] files = this.jobsFolder.listFiles();
         if (files != null) {
 
             for (File file : files) {
                 if (!file.isDirectory()) {
                     continue;
                 }
                 Job job = new Job(file, appRoot);
                 jobs.add(job);
             }
         }
     }
 
     public File getApplicationRoot() {
         return appRoot;
     }
 
     public File getApplicationFolder() {
         return appFolder;
     }
 
     public File getJobsFolder() {
         return jobsFolder;
     }
 
     public List<Job> getJobs() {
         return jobs;
     }
 
     public boolean hasJobs() {
         return jobs.size() > 0;
     }
 
     public boolean hasLlJobs() {
         return getLlJobs().size() > 0;
     }
 
     public boolean hasGrisuJobs() {
         return getGrisuJobs().size() > 0;
     }
 
     public List<Job> getLlJobs() {
         List<Job> result = Lists.newArrayList();
         for ( Job j : getJobs() ) {
             if (StringUtils.isNotBlank(j.getLl()) ) {
                 result.add(j);
             }
         }
         return result;
     }
 
     public List<Job> getGrisuJobs() {
         List<Job> result = Lists.newArrayList();
         for ( Job j : getJobs() ) {
             if (StringUtils.isNotBlank(j.getGrisu()) ) {
                 result.add(j);
             }
         }
         return result;
     }
 
     public String toString() {
         StringBuffer result = new StringBuffer();
         for ( Job j : getJobs() ) {
             result.append(j.getJobBaseName()+" ");
         }
         return result.toString();
     }
 }
