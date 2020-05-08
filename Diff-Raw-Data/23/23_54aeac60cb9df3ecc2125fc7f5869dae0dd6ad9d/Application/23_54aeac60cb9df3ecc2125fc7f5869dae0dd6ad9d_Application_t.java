 package controllers;
 
 import java.util.List;
 
 import org.apache.commons.io.filefilter.NotFileFilter;
 
 import models.JobApplication;
 import models.JobApplication.JobStatus;
 import play.Logger;
 import play.Play;
 import play.mvc.Controller;
 import play.mvc.With;
 import play.mvc.Before;
 
 @With(Auth.class) 
 public class Application extends Controller {
    
     @Before
     static void data() {
         renderArgs.put("newCount", JobApplication.count("status = ?", JobApplication.JobStatus.NEW));
         renderArgs.put("inprogressCount", JobApplication.count("status = ?", JobApplication.JobStatus.INPROGRESS));
         renderArgs.put("archivedCount", JobApplication.count("status = ?", JobApplication.JobStatus.ARCHIVED));
     }
 
    public static void index() {
        new jobs.HouseKeeping().now();
        box("new", 0);
    }

     public static void check() throws Exception {
         new jobs.FetchEmails().now().get();
        box("new", 0);
     }
 
     // API
 
    public static void box(String boxid, int pageId) {
         JobStatus status = JobStatus.find(boxid);
         if (status == null || status == JobStatus.DELETED) notFound();
         List<JobApplication> applications = JobApplication.find("status = ? order by submitted desc", status)
             .from(JobApplication.PER_PAGE * pageId).fetch(JobApplication.PER_PAGE);
         int pageCount = JobApplication.pageCount(status);
         if (pageId >= pageCount) notFound();
        render(boxid, pageId, applications, pageCount);
     }
 
     public static void candidate() {
         if (Play.mode.isProd()) notFound();
         render();
     }
 
 
 }
