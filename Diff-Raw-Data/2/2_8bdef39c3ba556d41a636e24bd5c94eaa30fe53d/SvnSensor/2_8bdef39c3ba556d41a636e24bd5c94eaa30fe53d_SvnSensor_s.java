 package org.hackystat.sensor.ant.svn;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.Map;
 
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.Task;
 import org.hackystat.sensorshell.SensorShellProperties;
 import org.hackystat.sensorshell.SensorShellException;
 import org.hackystat.sensorshell.SensorShell;
 import org.hackystat.sensorshell.usermap.SensorShellMap;
 import org.hackystat.sensorshell.usermap.SensorShellMapException;
 import org.hackystat.utilities.time.period.Day;
 import org.hackystat.utilities.tstamp.Tstamp;
 import org.hackystat.utilities.tstamp.TstampSet;
 
 /**
  * Ant task to extract the svn commits and send those information to Hackystat
  * server. Note: For binary files, the values for lines addes, lines deleted,
  * and total lines are meaningless.
  * 
  * @author Qin ZHANG
  * @author Austen Ito (v8 port)
  */
 public class SvnSensor extends Task {
   private String repositoryName;
   private String repositoryUrl;
   private String userName;
   private String password;
   private String fileNamePrefix;
   private String defaultHackystatAccount = "";
   private String defaultHackystatPassword = "";
   private String defaultHackystatSensorbase = "";
   private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
   private String fromDateString, toDateString;
   private Date fromDate, toDate;
   private boolean isVerbose = false;
   private String tool = "svn";
   private String lastIntervalInMinutesString = "";
   private int lastIntervalInMinutes;
 
   /**
    * Sets the svn repository name. This name can be any string. It's used in
    * commit metric to identify from which svn repository metric is retrieved.
    * 
    * @param repositoryName The name of the svn repository.
    */
   public void setRepositoryName(String repositoryName) {
     this.repositoryName = repositoryName;
   }
 
   /**
    * Sets the url to the svn repository. It can points to any subdirectory in
    * the repository. However, note that this sensor only supports http|https|svn
    * protocol.
    * 
    * @param repositoryUrl The url to the svn repository.
    */
   public void setRepositoryUrl(String repositoryUrl) {
     this.repositoryUrl = repositoryUrl;
   }
 
   /**
    * Sets the user name to access the SVN repository. If not set, then anonymous
    * credential is used.
    * 
    * @param userName The user name.
    */
   public void setUserName(String userName) {
     this.userName = userName;
   }
 
   /**
    * Sets if verbose mode has been enabled.
    * @param isVerbose true if verbose mode is enabled, false if not.
    */
   public void setVerbose(boolean isVerbose) {
     this.isVerbose = isVerbose;
   }
 
   /**
    * Sets the password for the user name.
    * 
    * @param password The password.
    */
   public void setPassword(String password) {
     this.password = password;
   }
 
   /**
    * Sets a string to be prepended to the file path in commit metric. Recall
    * that svn sensor can only get relative file path to the svn repository root,
    * however, most hackystat analysis requires fully qualified file path. This
    * prefix can be used to turn relative file path into some pseudo fully
    * qualified file path.
    * 
    * @param fileNamePrefix The string to be prepended to the file path in commit
    * metric.
    */
   public void setFileNamePrefix(String fileNamePrefix) {
     this.fileNamePrefix = fileNamePrefix;
   }
 
   /**
    * Sets a default Hackystat account to which to send commit data when there is
    * no svn committer to Hackystat account mapping.
    * 
    * @param defaultHackystatAccount The default Hackystat account.
    */
   public void setDefaultHackystatAccount(String defaultHackystatAccount) {
     this.defaultHackystatAccount = defaultHackystatAccount;
   }
 
   /**
    * Sets the default Hackystat account password.
    * @param defaultHackystatPassword the default account password.
    */
   public void setDefaultHackystatPassword(String defaultHackystatPassword) {
     this.defaultHackystatPassword = defaultHackystatPassword;
   }
 
   /**
    * Sets the default Hackystat sensorbase server.
    * @param defaultHackystatSensorbase the default sensorbase server.
    */
   public void setDefaultHackystatSensorbase(String defaultHackystatSensorbase) {
     this.defaultHackystatSensorbase = defaultHackystatSensorbase;
   }
 
   /**
    * Sets the optional fromDate. If fromDate is set, toDate must be set. This
    * field must be conform to yyyy-MM-dd format.
    * 
    * @param fromDateString The first date from which we send commit information
    * to Hackystat server.
    */
   public void setFromDate(String fromDateString) {
     this.fromDateString = fromDateString;
   }
 
   /**
    * Sets the optional toDate. If toDate is set, fromDate must be set. This
    * field must be conform to yyyy-MM-dd format.
    * 
    * @param toDateString The last date to which we send commit information to
    * Hackystat server.
    */
   public void setToDate(String toDateString) {
     this.toDateString = toDateString;
   }
   
   /**
    * Sets the last interval in minutes. 
    * 
    * @param lastIntervalInMinutes The preceding interval in minutes to poll.  
    */
   public void setLastIntervalInMinutes(String lastIntervalInMinutes) {
     this.lastIntervalInMinutesString = lastIntervalInMinutes;
   }
 
   /**
    * Checks and make sure all properties are set up correctly.
    * 
    * @throws BuildException If any error is detected in the property setting.
    */
   private void validateProperties() throws BuildException {
     if (this.repositoryName == null || this.repositoryName.length() == 0) {
       throw new BuildException("Attribute 'repositoryName' must be set.");
     }
     if (this.repositoryUrl == null || this.repositoryUrl.length() == 0) {
       throw new BuildException("Attribute 'repositoryUrl' must be set.");
     }
 
     // If lastIntervalInMinutes is set, then we define fromDate and toDate appropriately and return.
     if (!this.lastIntervalInMinutesString.equals("")) {
       try {
         this.lastIntervalInMinutes = Integer.parseInt(this.lastIntervalInMinutesString);
         long now = (new Date()).getTime();
         this.toDate = new Date(now);
        long intervalMillis = 1000 * 60 * this.lastIntervalInMinutes;
         this.fromDate = new Date(now - intervalMillis);
         return;
       }
       catch (Exception e) {
         throw new BuildException("Attribute 'lastIntervalInMinutes' must be an integer.", e);
       }
     }
 
     // If lastIntervalInMinutes, fromDate, and toDate not set, we extract commit information for
     // the previous 25 hours. (This ensures that running the sensor as part of a daily build
     // should have enough "overlap" to not miss any entries.)
     // Then return.
     if (this.fromDateString == null && this.toDateString == null) {
       long now = (new Date()).getTime();
       this.toDate = new Date(now);
       long twentyFiveHoursMillis = 1000 * 60 * 60 * 25;
       this.fromDate = new Date(now - twentyFiveHoursMillis);
       return;
     }
 
     // Finally, we try to deal with the user provided from and to dates.
     try {
       if (this.hasSetToAndFromDates()) {
         this.fromDate = new Date(Day.getInstance(this.dateFormat.parse(this.fromDateString))
             .getFirstTickOfTheDay() - 1);
         this.toDate = new Date(Day.getInstance(this.dateFormat.parse(this.toDateString))
             .getLastTickOfTheDay());
       }
       else {
         throw new BuildException(
             "Attributes 'fromDate' and 'toDate' must either be both set or both not set.");
       }
     }
     catch (ParseException ex) {
       throw new BuildException("Unable to parse 'fromDate' or 'toDate'.", ex);
     }
 
     if (this.fromDate.compareTo(this.toDate) > 0) {
       throw new BuildException("Attribute 'fromDate' must be a date before 'toDate'.");
     }
 
   }
 
   /**
    * Returns true if both of the to and from date strings have been set by the client. Both dates
    * must be set or else this sensor will not know which revisions to grab commit information.
    * 
    * @return true if both the to and from date strings have been set.
    */
   private boolean hasSetToAndFromDates() {
     return (this.fromDateString != null) && (this.toDateString != null);
   }
 
   /**
    * Extracts commit information from SVN server, and sends them to the
    * Hackystat server.
    * 
    * @throws BuildException If the task fails.
    */
   @Override
   public void execute() throws BuildException {
     this.validateProperties(); // sanity check.
     if (this.isVerbose) {
       System.out.printf("Processing commits for %s between %s (exclusive) and %s (inclusive)%n",
           this.repositoryUrl, this.fromDate, this.toDate);
     }
 
     try {
       Map<String, SensorShell> shellCache = new HashMap<String, SensorShell>();
       SensorShellMap shellMap = new SensorShellMap(this.tool);
       if (this.isVerbose) {
         System.out.println("Checking for user maps at: " + shellMap.getUserMapFile());
         System.out.println("SVN accounts found: " + shellMap.getToolAccounts(this.tool));
       }
       
       try {
         shellMap.validateHackystatInfo(this.tool);
       }
       catch (Exception e) {
         System.out.println("Warning: UserMap validation failed: " + e.getMessage());
       }
       SVNCommitProcessor processor = new SVNCommitProcessor(this.repositoryUrl, this.userName,
           this.password);
       long startRevision = processor.getRevisionNumber(this.fromDate) + 1;
       long endRevision = processor.getRevisionNumber(this.toDate);
       int entriesAdded = 0;
       TstampSet tstampSet = new TstampSet();
       for (long revision = startRevision; revision <= endRevision; revision++) {
         CommitRecord commitRecord = processor.getCommitRecord(revision);
         if (commitRecord != null) {
           String author = commitRecord.getAuthor();
           String message = commitRecord.getMessage();
           Date commitTime = commitRecord.getCommitTime();
 
           for (CommitRecordEntry entry : commitRecord.getCommitRecordEntries()) {
             if (this.isVerbose) {
               System.out.println("Retrieved SVN data: " + 
                   commitRecord.toString() + " - " + entry.toString());
             }
             // Find the shell, if possible.
             SensorShell shell = this.getShell(shellCache, shellMap, author);
             if (shell != null) {
               this.processCommitEntry(shell, author, message, tstampSet
                   .getUniqueTstamp(commitTime.getTime()), commitTime, revision, entry);
               entriesAdded++;
             }
           }
         }
       }
       if (this.isVerbose) {
         System.out.println("Found " + entriesAdded + " commit records.");
       }
 
       // Send the sensor data after all entries have been processed.
       for (SensorShell shell : shellCache.values()) {
         if (this.isVerbose) {
           System.out.println("Sending data to " + shell.getProperties().getSensorBaseUser() + 
               " at " + shell.getProperties().getSensorBaseHost());
         }
         shell.send();
         shell.quit();
       }
     }
     catch (Exception ex) {
       throw new BuildException(ex);
     }
   }
 
   /**
    * Returns the shell associated with the specified author, or null if not found. 
    * The shellCache is
    * used to store SensorShell instances associated with the specified user. The
    * SensorShellMap contains the SensorShell instances built from the
    * UserMap.xml file. This method should be used to retrieve the SensorShell
    * instances to avoid the unnecessary creation of SensorShell instances when
    * sending data for each commit entry. Rather than using a brand new
    * SensorShell instance, this method finds the correct shell in the map,
    * cache, or creates a brand new shell to use.
    * @param shellCache the mapping of author to SensorShell.
    * @param shellMap the mapping of author to SensorShell created by a usermap
    * entry.
    * @param author the author used to retrieve the shell instance.
    * @return the shell instance associated with the author name.
    * @throws SensorShellMapException thrown if there is a problem retrieving the
    * shell instance.
    * @throws SensorShellException thrown if there is a problem retrieving
    * the Hackystat host from the v8.sensor.properties file.
    */
   private SensorShell getShell(Map<String, SensorShell> shellCache, SensorShellMap shellMap,
       String author) throws SensorShellMapException, SensorShellException {
     if (shellCache.containsKey(author)) {
       return shellCache.get(author); // Returns a cached shell instance.
     }
     else {
       // If the shell user mapping has a shell, add it to the shell cache.
       if (shellMap.hasUserShell(author)) {
         SensorShell shell = shellMap.getUserShell(author);
         shellCache.put(author, shell);
         return shell;
       }
       else { // Create a new shell and add it to the cache.
         if ("".equals(this.defaultHackystatAccount)
             || "".equals(this.defaultHackystatPassword)
             || "".equals(this.defaultHackystatSensorbase)) {
           System.out.println("Warning: A user mapping for the user, " + author
               + " was not found and no default Hackystat account login, password, "
               + "or server was provided. Data ignored.");
           return null;
         }
         SensorShellProperties props = new SensorShellProperties(this.defaultHackystatSensorbase,
             this.defaultHackystatAccount, this.defaultHackystatPassword);
 
         SensorShell shell = new SensorShell(props, false, "svn");
         shellCache.put(author, shell);
         return shell;
       }
     }
   }
 
   /**
    * Processes a commit record entry and extracts relevant metrics.
    * 
    * @param shell The shell that the commit record information is added to.
    * @param author The author of the commit.
    * @param message The commit log message.
    * @param timestamp the unique timestamp that is associated with the specified
    * entry.
    * @param commitTime The commit time.
    * @param revision The revision number.
    * @param entry The commit record entry.
    * 
    * @throws Exception If there is any error.
    */
   private void processCommitEntry(SensorShell shell, String author, String message,
       long timestamp, Date commitTime, long revision, CommitRecordEntry entry)
     throws Exception {
     if (shell != null && entry.isFile()) {
       String file = this.fileNamePrefix == null ? "" : this.fileNamePrefix;
       if (entry.getToPath() == null) {
         file += entry.getFromPath();
       }
       else {
         file += entry.getToPath();
       }
       // if binary file, then totalLines, linesAdded, linesDeleted all set to
       // zero.
       int totalLines = entry.isTextFile() ? entry.getTotalLines() : 0;
       int linesAdded = entry.isTextFile() ? entry.getLinesAdded() : 0;
       int linesDeleted = entry.isTextFile() ? entry.getLinesDeleted() : 0;
 
       Map<String, String> pMap = new HashMap<String, String>();
       String timestampString = Tstamp.makeTimestamp(timestamp).toString();
       pMap.put("SensorDataType", "Commit");
       pMap.put("Resource", file);
       pMap.put("Tool", "Subversion");
       pMap.put("Timestamp", timestampString);
       pMap.put("Runtime", Tstamp.makeTimestamp(commitTime.getTime()).toString());
       pMap.put("repository", this.repositoryName);
       pMap.put("totalLines", String.valueOf(totalLines));
       pMap.put("linesAdded", String.valueOf(linesAdded));
       pMap.put("linesDeleted", String.valueOf(linesDeleted));
       pMap.put("log", message);
       shell.add(pMap);
       if (this.isVerbose) {
         System.out.printf("Sending SVN Commit: Timestamp: %s Resource: %s User: %s%n", 
             timestampString, file, shell.getProperties().getSensorBaseUser());
       }
     }
   }
 }
