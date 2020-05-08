 package hu.juranyi.zsolt.heritrixremote.model;
 
 import java.util.ArrayList;
 import java.util.List;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 public class Heritrix {
 
     private final String hostPort, userPass;
     private String indexPage;
     private List<Job> jobs;
     private String jobsDirectory;
 
     public Heritrix(String hostPort, String userPass) {
         this.hostPort = hostPort;
         this.userPass = userPass;
     }
 
     public String getHostPort() {
         return hostPort;
     }
 
     public String getUserPass() {
         return userPass;
     }
 
     public String getIndexPage() {
         if (null == indexPage) {
             indexPage = fetchIndexPage();
         }
         return indexPage;
     }
 
     public List<Job> getJobs() {
         if (null == jobs) {
             jobs = fetchJobs();
         }
         return jobs;
     }
 
     public String getJobsDirectory() {
         if (null == jobsDirectory) {
             jobsDirectory = fetchJobsDirectory();
         }
         return jobsDirectory;
     }
 
     private String fetchIndexPage() {
         try {
             return new HeritrixCall(this).getResponse();
         } catch (Exception ex) {
             System.out.println("ERROR: Failed to fetch Heritrix index page.");
             System.exit(1); // TODO EXIT WITH ERROR CODE
             return null;
         }
     }
 
     private List<Job> fetchJobs() {
         ArrayList<Job> jobs = new ArrayList<Job>();
        Elements jobLinks = Jsoup.parse(getIndexPage()).select("a[href~=job/]");
         if (jobLinks.isEmpty()) {
             System.out.println("ERROR: Failed to parse or there aren't any crawling jobs.");
             System.exit(1); // TODO EXIT WITH ERROR CODE
         } else {
             for (Element e : jobLinks) {
                 jobs.add(new Job(this, e.text()));
             }
         }
         return jobs;
     }
 
     private String fetchJobsDirectory() {
         Elements jobsDirLinks = Jsoup.parse(getIndexPage()).select("a[href=jobsdir]");
         if (jobsDirLinks.isEmpty()) {
             System.out.println("ERROR: Failed to parse jobs directory.");
             System.exit(1); // TODO EXIT WITH ERROR CODE
             return null;
         } else {
             return jobsDirLinks.first().text();
         }        
     }
 }
