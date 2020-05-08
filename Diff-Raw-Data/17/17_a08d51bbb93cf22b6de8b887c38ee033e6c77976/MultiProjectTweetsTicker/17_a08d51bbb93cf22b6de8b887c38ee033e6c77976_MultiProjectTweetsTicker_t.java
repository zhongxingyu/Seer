 package org.hackystat.tickertape.ticker;
 
 import java.util.Date;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.logging.Logger;
 
 import org.hackystat.sensorbase.client.SensorBaseClient;
 import org.hackystat.tickertape.notifier.twitter.TwitterNotifier;
 import org.hackystat.tickertape.tickerlingua.HackystatUser;
 import org.hackystat.tickertape.tickerlingua.NotificationService;
 import org.hackystat.tickertape.tickerlingua.TickerLingua;
 import org.hackystat.tickertape.tickerlingua.Tickertape;
 import org.hackystat.tickertape.tickerlingua.HackystatProject;
 import org.hackystat.tickertape.ticker.data.ProjectSensorDataLog;
 
 /**
  * A Ticker. 
  * @author Philip Johnson
  */
 public class MultiProjectTweetsTicker implements Ticker {
   
   private TwitterNotifier twitterNotifier;
   private Tickertape tickertape;
   private Logger logger;
   private TickerLingua tickerLingua;
   private Map<String, ProjectSensorDataLog> project2log = 
     new HashMap<String, ProjectSensorDataLog>();
   
   /** The string used as the key for the TimeBetweenTweets property. */
   public static final String TIME_BETWEEN_TWEETS_KEY = "TimeBetweenTweets";
 
 
   /**
    * The run method for this ticker. 
    * @param tickertape The Tickertape instance indicating what to do. 
    * @param tickerLingua The TickerLingua instance with global data info.
    * @param logger The logger to be used to communicate status. 
    */
   public void run(Tickertape tickertape, TickerLingua tickerLingua, Logger logger) {
     logger.info("Running MultiProjectTweetsTicker...");
     // Always do this every time. 
     this.tickertape = tickertape;
     this.logger = logger;
     this.tickerLingua = tickerLingua;
     notify(null);  // Force a tweet just the first time we run this Ticker. 
     
     // Process each project.
     for (HackystatProject project : tickertape.getHackystatProjects()) {
       // Add newlines to help readability.
       this.logger.info("\n\nChecking status of project " + project.getName());
       
       // Find or create the ProjectSensorDataLog.
       String projectName = project.getName();
       String projectOwner = project.getHackystatOwner().getHackystatUserAccount();
       HackystatUser authUser = project.getHackystatAuthUser();
       SensorBaseClient client = authUser.getSensorBaseClient();
       ProjectSensorDataLog log = this.getProjectSensorDataLog(client, this.getMaxLife(), 
           projectOwner, projectName, logger);
       
       // Now get any new data.
       log.update();
           
       for (String user : log.getProjectParticipants()) {
        this.logger.fine(String.format("User %s %s sensor data and %s a recent tweet.",
            user, 
            (log.hasRecentSensorData(user) ? "has" : "does not have"), 
            (log.hasRecentTweet(user) ? "has" : "does not have")
            ));
         if (log.hasRecentSensorData(user) && !log.hasRecentTweet(user)) {
           String workedOnMsg = this.getWorkedOnMsg(log, user);
           String builtMsg = this.getBuiltMsg(log, user);
           String testMsg = this.getTestMsg(log, user);
           String toolMsg = log.getToolString(user);
           StringBuffer buff = new StringBuffer();
           buff.append(getShortName(user)).append(' ');
           if (workedOnMsg != null) {
             buff.append(workedOnMsg).append(", ");
           }
           if (builtMsg != null) {
             buff.append(builtMsg).append(", ");
           }
           if (testMsg != null) {
             buff.append(testMsg).append(", ");
           }
           if (toolMsg != null) {
             buff.append("using ").append(toolMsg).append(" for ").append(project.getShortName());
           }
           notify(buff.toString());
           log.setTweet(user);
         }
       }
     }
     logger.info("Finished running MultiProjectTweetsTicker.");
   }
   
   /**
    * Returns a string indicating the files worked on, or null if no files were worked on.
    * @param log The ProjectSensorData log. 
    * @param user The user of interest.
    * @return The string indicating work, or null.
    */
   private String getWorkedOnMsg(ProjectSensorDataLog log, String user) {
     String mostWorkedFile = log.mostWorkedOnFile(user);
     return (mostWorkedFile == null) ?  null :
         String.format("worked on %s file(s) (including %s)", log.getNumFilesWorkedOn(user),
             mostWorkedFile);
   }
   
   /**
    * Returns a string indicating build activity.
    * @param log The ProjectSensorDataLog.
    * @param user The user. 
    * @return A string indicating build activity, or null if there was none.
    */
   private String getBuiltMsg(ProjectSensorDataLog log, String user) {
     int numBuilds = log.getSensorDataCount(user, "Build");
     return (numBuilds == 0) ? null :
       String.format("built %s time(s) (%s successful)", numBuilds, 
           log.getBuildSuccessCount(user));
   }
   
   /**
    * Returns a string indicating build activity.
    * @param log The ProjectSensorDataLog.
    * @param user The user. 
    * @return A string indicating build activity, or null if there was none.
    */
   private String getTestMsg(ProjectSensorDataLog log, String user) {
     int numTests = log.getSensorDataCount(user, "UnitTest");
     return (numTests == 0) ? null :
       String.format("ran %s test(s) (%s passing)", numTests, log.getTestPassCount(user));
   }
   
   
   /**
    * Returns the shortname associated with this email, or the email if the shortname could not be 
    * found.
    * @param email The email corresponding to a Hackystat user account.
    * @return The short name or the email. 
    */
   private String getShortName(String email) {
     for (HackystatUser user : this.tickerLingua.getHackystatUsers()) {
       if (email.equals(user.getHackystatUserAccount())) {
         return user.getShortName();
       }
     }
     return email;
   }
   
   /**
    * Return the ProjectSensorDataLog for this project, creating it if it does not yet exist. 
    * @param client The SensorBaseClient.
    * @param maxLife The maxLife for sensor data entries in this log.
    * @param projectOwner The owner.
    * @param projectName The name.
    * @param logger The logger to be used if things go wrong. 
    * @return The ProjectSensorDataLog.
    */
   private ProjectSensorDataLog getProjectSensorDataLog(SensorBaseClient client, double maxLife,
       String projectOwner, String projectName, Logger logger) {
     // If we can't find it, create it.
     if (!this.project2log.containsKey(projectName)) {
       ProjectSensorDataLog log = new ProjectSensorDataLog(client, maxLife, projectOwner,
           projectName, logger);
       this.project2log.put(projectName, log);
     }
     return this.project2log.get(projectName);    
   }
   
 
   /**
    * Sends a message to the Twitter account associated with this ticker, if the message is not null.
    * @param message The message to be sent. 
    */
   private void notify(String message) {
     if (this.twitterNotifier == null) {
       NotificationService notifier = tickertape.getNotificationServices().get(0);
       this.twitterNotifier = new TwitterNotifier(notifier.getId(), notifier.getPassword(), logger);
       this.twitterNotifier.notify("Starting up a new TwitterNotifier at: " + new Date());
     }
     if (message != null) {
       this.twitterNotifier.notify(message);
     }
   }
   
   /**
    * Gets the maxLife by looking for the TimeBetweenTweets ticker property and converting it to
    * a double.  Returns 0.5 (30 minutes) if the property is not found or could not be parsed.
    * @return The maxLife. 
    */
   private double getMaxLife() {
     double defaultMaxLife = 0.5;
     String timeBetweenTweets = this.tickertape.getTickerProperties().get(TIME_BETWEEN_TWEETS_KEY);
     if (timeBetweenTweets == null) {
       return defaultMaxLife;
     }
     try {
       return Double.parseDouble(timeBetweenTweets);
     }
     catch (Exception e) {
       this.logger.warning("Error parsing TimeBetweenTweets: " + timeBetweenTweets);
       return defaultMaxLife;
     }
   }
 }
