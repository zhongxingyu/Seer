 package org.hackystat.sensor.ant.svn;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.Map;
 
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.Task;
 import org.hackystat.sensorshell.SensorProperties;
 import org.hackystat.sensorshell.SensorPropertiesException;
 import org.hackystat.sensorshell.SensorShell;
 import org.hackystat.sensorshell.usermap.SensorShellMap;
 import org.hackystat.sensorshell.usermap.SensorShellMapException;
 import org.hackystat.utilities.time.period.Day;
 
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
   private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
   private String fromDateString, toDateString;
   private Date fromDate, toDate;
   private boolean isVerbose = false;
 
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
    * Checks and make sure all properties are set up correctly.
    *
    * @throws BuildException If any error is detected in the property setting.
    */
   private void processProperties() throws BuildException {
     if (this.repositoryName == null || this.repositoryName.length() == 0) {
       throw new BuildException("Attribute 'repositoryName' must be set.");
     }
     if (this.repositoryUrl == null || this.repositoryUrl.length() == 0) {
       throw new BuildException("Attribute 'repositoryUrl' must be set.");
     }
 
     // If fromDate and toDate not set, we only extract commit information for
     // the previous day.
     if (this.fromDateString == null && this.toDateString == null) {
       Day previousDay = Day.getInstance().inc(-1);
       this.fromDate = new Date(previousDay.getFirstTickOfTheDay() - 1);
       this.toDate = new Date(previousDay.getLastTickOfTheDay());
     }
     else {
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
        throw new BuildException("Unable to parse 'fromDate' or 'toDate'.");
       }
 
       if (this.fromDate.compareTo(this.toDate) > 0) {
         throw new BuildException("Attribute 'fromDate' must be a date before 'toDate'.");
       }
     }
   }
 
   /**
    * Returns true if both of the to and from date strings have been set by the
    * client. Both dates must be set or else this sensor will not know which
    * revisions to grab commit information.
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
   public void execute() throws BuildException {
     this.processProperties(); // sanity check.
     if (this.isVerbose) {
       System.out.println("Processing commits between " + this.fromDate + "(exclusive) to "
           + this.toDate + "(inclusive)");
     }
 
     try {
       Map<String, SensorShell> shellCache = new HashMap<String, SensorShell>();
       SensorShellMap shellMap = new SensorShellMap("svn");
       SVNCommitProcessor processor = new SVNCommitProcessor(this.repositoryUrl, this.userName,
           this.password);
       long startRevision = processor.getRevisionNumber(this.fromDate) + 1;
       long endRevision = processor.getRevisionNumber(this.toDate);
       int entriesAdded = 0;
       for (long revision = startRevision; revision <= endRevision; revision++) {
         CommitRecord commitRecord = processor.getCommitRecord(revision);
         if (commitRecord != null) {
           if (this.isVerbose) {
             System.out.println(commitRecord);
           }
           String author = commitRecord.getAuthor();
           String message = commitRecord.getMessage();
           Date commitTime = commitRecord.getCommitTime();
 
           for (CommitRecordEntry entry : commitRecord.getCommitRecordEntries()) {
             SensorShell shell = this.getShell(shellCache, shellMap, author);
             this.processCommitEntry(shell, author, message, commitTime, revision, entry);
             entriesAdded++;
           }
         }
       }
 
       boolean isServerAvailable = false;
       // Send the sensor data after all entries have been processed.
       for (SensorShell shell : shellCache.values()) {
         isServerAvailable = shell.ping();
         shell.send();
         shell.quit();
       }
 
       SensorProperties props = new SensorProperties();
       if (isServerAvailable) {
         System.out.println(entriesAdded + " entries sent to " + props.getHackystatHost());
       }
       else {
         System.out.println("Server not available. Storing " + entriesAdded
             + " data entries offline.");
       }
     }
     catch (Exception ex) {
       throw new BuildException(ex);
     }
   }
 
   /**
    * Returns the shell associated with the specified author. The shellCache is
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
    * @throws SensorPropertiesException thrown if there is a problem retrieving
    * the Hackystat host from the v8.sensor.properties file.
    */
   private SensorShell getShell(Map<String, SensorShell> shellCache, SensorShellMap shellMap,
       String author) throws SensorShellMapException, SensorPropertiesException {
     if (shellCache.containsKey(author)) {
       return shellCache.get(author); // Returns a cached shell instance.
     }
     else {
       // If there is no user mapping, attempt to create a default user shell.
       if (!shellMap.hasUserShell(author)) {
         if ("".equals(this.defaultHackystatAccount)
             || "".equals(this.defaultHackystatPassword)) {
           throw new BuildException("A user mapping for the user, " + author
               + " was not found and no default Hackystat account was provided.");
         }
         SensorProperties currentProps = new SensorProperties();
         SensorProperties props = new SensorProperties(currentProps.getHackystatHost(),
             this.defaultHackystatAccount, this.defaultHackystatPassword);
 
         SensorShell shell = new SensorShell(props, false, "svn");
         shellCache.put(author, shell);
         return shell;
       }
       return shellMap.getUserShell(author); // Returns the usermap shell.
     }
   }
 
   /**
    * Processes a commit record entry and extracts relevant metrics.
    *
    * @param shell The shell that the commit record information is added to.
    * @param author The author of the commit.
    * @param message The commit log message.
    * @param commitTime The commit time.
    * @param revision The revision number.
    * @param entry The commit record entry.
    *
    * @throws Exception If there is any error.
    */
   private void processCommitEntry(SensorShell shell, String author, String message,
       Date commitTime, long revision, CommitRecordEntry entry) throws Exception {
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
       pMap.put("SensorDataType", "Commit");
       pMap.put("Resource", file);
       pMap.put("repository", this.repositoryName);
       pMap.put("totalLines", String.valueOf(totalLines));
       pMap.put("linesAdded", String.valueOf(linesAdded));
       pMap.put("linesDeleted", String.valueOf(linesDeleted));
       pMap.put("log", message);
       shell.add(pMap);
     }
   }
 }
