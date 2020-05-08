 package fi.helsinki.cs.oato;
 
 import java.util.*;
 import java.io.IOException;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.HttpClient;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpEntity;
 import org.apache.http.util.EntityUtils;
 import org.json.simple.JSONValue;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 import org.joda.time.DateTime;
 
 public class OnlineCourseSource extends Observable {
     public static final String SOURCE_URL = "http://www.cs.helsinki.fi/u/tkairi/rajapinta/courses.json";
     public static final DateTimeFormatter formatter = DateTimeFormat.forPattern("YYYY-MM-dd");
 
     private List<Course> courseList;
     private boolean ready = false;
 
     /** Create a new OnlineCourseSource. Constructor will return immediately and start fetching the data in the background.
         You can check if the data is already available by calling isReady(), and get the data with getCourses() */
     OnlineCourseSource() {
        (new FetcherThread()).start();
     }
 
     public boolean isReady() {
         return ready;
     }
 
     public List<Course> getCourses() {
         if (!ready) {
             /* Error! You should not try to get empty course list, but check readiness with isReady() first */
             return Collections.unmodifiableList(new ArrayList<Course>());
         }
 
         return Collections.unmodifiableList(courseList);
     }
 
     class FetcherThread extends Thread {
         HttpClient client;
         
         /* If parsing fails, sleeps 5 sec to avoid hammering. Increases geometrically. */
         long errorSleep = 5 * 1000;
 
         FetcherThread() {
             super();
             client = new DefaultHttpClient();
         }
 
         public void run() {
             while (true) {
                 try {
                     courseList = parse(fetch());
                     if (Main.DEBUG) {
                         System.err.println("Got following courses:");
                         for (Course c : courseList) {
                             System.err.println(c);
                         }
                     }
                     ready = true;
                     setChanged();
                     notifyObservers();
                     return;
                 } catch (Exception e) {
                     System.err.println("While fetching online course data: " + e);
                     if (Main.DEBUG) {
                         e.printStackTrace();
                     }
                     try {
                         this.sleep(errorSleep);
                     } catch (InterruptedException ie) { }
                     errorSleep *= 2;
                     System.err.println("Retrying fetch");
                     continue;
                 }
             }
         }
 
         private String fetch() throws IOException {
             HttpGet req = new HttpGet(SOURCE_URL);
             HttpResponse response = client.execute(req);
             HttpEntity entity = response.getEntity();
             if (entity != null) {
                 return EntityUtils.toString(entity);
             } else {
                 throw new IOException("Received nothing");
             }
         }
 
         private List<Course> parse(String s) {
             Map topMap = (Map) JSONValue.parse(s);
             List onlineCourses = (List) topMap.get("courses");
             List<Course> localCourses = new ArrayList<Course>(onlineCourses.size());
 
             for (Iterator i=onlineCourses.iterator(); i.hasNext();) {
                 Map item = (Map) i.next();
                 localCourses.add(new Course((String) item.get("course"),
                     formatter.parseDateTime((String) item.get("start_date")),
                     formatter.parseDateTime((String) item.get("end_date"))));
             }
 
             return localCourses;
         }
     }
 }
