 package lab;
 
 import sun.font.CreatedFontTracker;
 
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 
 /**
  * Created with IntelliJ IDEA.
  * User: ron
  * Date: 1/5/13
  * Time: 9:14 PM
  * To change this template use File | Settings | File Templates.
  */
 public class Crawler {
     private int Tasks = 0;
     private int MAX_TASKS = 0;
 
     private static Crawler instance = null;
     public static final int STATUS_READY = 1;
     public static final int STATUS_BUSY = 2;
     public static final int ADD_STATUS_SUCCESS = 1;
     public static final int ADD_STATUS_RUNNING = 2;
    public static final int STATUS_ERROR_BAD_URL = 3;
 
     private boolean singleTask = false;
 
     CrawlerTaskPool taskPool;
 
     public Crawler() {
 
         taskPool = new CrawlerTaskPool();
         singleTask = false;
         if (1 == ConfigManager.getInstance().getMaxCrawlerThreads())
         {
             singleTask = true;
         }
     }
 
     private String getDomainName(String URLToParse)
     {
         String [] ParsedURL = URLToParse.split("/");
         switch (ParsedURL.length)
         {
             case (0):
                    return ""; // TODO: BUGBUG - Get reiter error code
             case (1):
                 if (ParsedURL[0].substring(0,3) == "http")
                    return ""; // TODO: BUGBUG - get reiter error code
                 else
                     return ParsedURL[0];
                 //BUGBUG - notice that if you remove the if/else then you must add:
                 //break;
             case (2):
                 if (ParsedURL[0].substring(0,3) == "http")
                 {
                     return ParsedURL[1];
                 } else
                 {
                     return ParsedURL[0];
                 }
                 //BUGBUG - notice that if you remove the if/else then you must add:
                 //break;
         }
 
         return null;
     }
 
     private int createTask(String URLToAdd, boolean ignoreRobots)
     {
         try {
             // Check that the URL is valid
            String domain = getDomainName(URLToAdd);
            if (domain == "")
            {
                return STATUS_ERROR_BAD_URL;
            }

            InetAddress.getByName(domain);
 
             // Create the new Task
             if (singleTask && (taskPool.getTaskCount() > 0))
             {
                return STATUS_BUSY; // TODO: BUGBUG: Reiter to set up error codes
             }  else
             {
                 taskPool.enqueue(URLToAdd);
             }
 
         } catch (UnknownHostException e) {
             Logger.error("Failed parsing requested URL: " + URLToAdd);
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            return STATUS_ERROR_BAD_URL;
         }
        return STATUS_READY;
     }
 
     public int add(String URLToAdd, boolean ignoreRobots)
     {
         try {
             return createTask(URLToAdd, ignoreRobots);
         }
         catch (RuntimeException e)
         {
             return -1; // TODO: BUGBUG: Reiter to define error codes
         }
 
     }
 
     public int getStatus()
     {
         if (MAX_TASKS == Tasks)
         {
             return -1; // TODO: BUGBUG - Retier to define return error
         }
         return -1;
     }
 
     public static Crawler getInstance() {
         if (instance == null) {
             instance = new Crawler();
         }
 
         return instance;
     }
 }
