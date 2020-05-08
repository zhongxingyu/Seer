 package com.powersurgepub.psfiles;
 
   import com.powersurgepub.psutils.*;
   import java.io.*;
   import java.text.*;
   import java.util.*;
 
 /**
  A specification for a file or other data store. In addition to the file's
  location, a file type, a file format, a last access date, and a last backup
  date are also stored. 
  */
 public class FileSpec {
   
   public static final String EQUALS = "=";
   public static final String DELIMITER = ";";
   public static final String PATH = "path";
   public static final String TYPE = "type";
   public static final String FORMAT = "format";
   public static final String LAST_ACCESS = "last-access";
   public static final String LAST_BACKUP = "last-backup";
   public static final String BACKUP_FOLDER = "backup-folder";
 
   public static final String RECENT_FILE            = "recent-file";
   public static final String RECENT_FILE_TYPE       = "recent-file-type";
   public static final String RECENT_FILE_NAME       = "recent-file-name";
   public static final String RECENT_FILE_FORMAT     = "recent-file-format";
   public static final String RECENT_FILE_DATE       = "recent-file-date";
   public static final String OLD_RECENT_FILE_PREFIX = "recent.file";
   
   public static final int    BRIEF_DISPLAY_NAME_MAX_LENGTH = 30;
   
 
   private             File   file = null;
   private             String type   = "";
   private             String path   = "";
   private             String format = "";
   private             Date   lastAccessDate   = new Date();
   private             Date   lastBackupDate   = new Date();
   private             String backupFolder = "";
 
   /**
    Construct a FileSpec without any data.
    */
   public FileSpec () {
 
   }
 
   /**
    Construct a FileSpec with a path.
 
    @param path A file path or url, typically.
    */
   public FileSpec (String path) {
     setPath(path);
   }
   
   /**
    Construct a file spec with a File object. 
   @param file 
   */
   public FileSpec (File file) {
     setFile(file);
   }
 
   /**
    Load this file specification from a recent file user preference.
 
    @param prefsQualifier Used to qualify a particular group of recent files
                          within an application. Typically supplied as an
                          empty string.
    @param recentFileNumber The number identifying the position of the recent
                            file in a list. Zero would identify the first
                            file in the list, and the file most recently
                            accessed. 
    */
   public void loadFromRecentPrefs (String prefsQualifier, int recentFileNumber) {
 
     // Apend the file number to the keys
     String keySuffix = String.valueOf(recentFileNumber);
 
     UserPrefs prefs = UserPrefs.getShared();
     
     String fileInfo = prefs.getPref
         (prefsQualifier + RECENT_FILE + "-" + keySuffix, "");
     if (fileInfo.length() > 0) {
       setFileInfo (fileInfo);
     } else {
       // Load the path
       setPath (prefs.getPref(prefsQualifier + RECENT_FILE_NAME
           + "-" + keySuffix, ""));
 
       if (path.length() == 0) {
         setPath (prefs.getPref(OLD_RECENT_FILE_PREFIX
             + keySuffix));
       } else {
         // Load the type
         setType (prefs.getPref(prefsQualifier + RECENT_FILE_TYPE + "-"
             + keySuffix, ""));
 
         // Load the format
         setFormat (prefs.getPref(prefsQualifier + RECENT_FILE_FORMAT + "-"
             + keySuffix, ""));
 
         // Load the lastAccessDate last accessed
         setLastAccessDate (prefs.getPref(prefsQualifier + RECENT_FILE_DATE + "-"
             + keySuffix, ""));
         
         // Load the generic backup folder, if one is available
         setBackupFolder (prefs.getPref (BACKUP_FOLDER, ""));
 
       }
     }
   }
 
   /**
    Save this file specification to a recent file user preference.
 
    @param prefsQualifier Used to qualify a particular group of recent files
                          within an application. Typically supplied as an
                          empty string.
    @param recentFileNumber The number identifying the position of the recent
                            file in a list. Zero would identify the first
                            file in the list, and the file most recently
                            accessed.
    */
   public void saveToRecentPrefs (String prefsQualifier, int recentFileNumber) {

     // Apend the file number to the keys
     String keySuffix = String.valueOf(recentFileNumber);
 
     UserPrefs prefs = UserPrefs.getShared();
     
     // Save the entire bundle as one preference
     prefs.setPref(prefsQualifier + RECENT_FILE + "-" + keySuffix, getFileInfo());
   }
   
   public void setFileInfo(String fileInfo) {
     int i = 0;
     int equalsIndex;
     int delimIndex;
     while (i >= 0 && i < fileInfo.length()) {
       while (i < fileInfo.length()
           && Character.isWhitespace(fileInfo.charAt(i))) {
         i++;
       }
       equalsIndex = fileInfo.indexOf(EQUALS, i);
       delimIndex = -1;
       if (equalsIndex > 0) {
         delimIndex = fileInfo.indexOf(DELIMITER, equalsIndex);
         if (delimIndex < 0) {
           delimIndex = fileInfo.length();
         } // end if we found a delimiter
         String name = fileInfo.substring(i, equalsIndex);
         String data = fileInfo.substring(equalsIndex + 1, delimIndex);
         setAttribute (name, data);
       } // end if we found an equals sign
       i = delimIndex;
       if (i < fileInfo.length()) {
         i++;
       }
     } // end while more characters to evaluate
   } // end method setFileInfo
   
   public void setAttribute (String name, String data) {
     if (name.equalsIgnoreCase(PATH)) {
       setPath (data);
     }
     else
     if (name.equalsIgnoreCase(TYPE)) {
       setType (data);
     }
     else
     if (name.equalsIgnoreCase(FORMAT)) {
       setFormat (data);
     }
     else
     if (name.equalsIgnoreCase(LAST_ACCESS)) {
       setLastAccessDate (data);
     }
     else
     if (name.equalsIgnoreCase(LAST_BACKUP)) {
       setLastBackupDate (data);
     }
     else
     if (name.equalsIgnoreCase(BACKUP_FOLDER)) {
       setBackupFolder (data);
     }
   }
   
   public String getFileInfo() {
     StringBuilder str = new StringBuilder();
     addAttribute(str, PATH, path);
     addAttribute(str, TYPE, type);
     addAttribute(str, FORMAT, format);
     addAttribute(str, LAST_ACCESS, getLastAccessDateAsString());
     addAttribute(str, LAST_BACKUP, getLastBackupDateAsString());
     addAttribute(str, BACKUP_FOLDER, getBackupFolder());
     return str.toString();
   }
   
   private void addAttribute (StringBuilder str, String name, String data) {
     if (data.length() > 0) {
       str.append(name);
       str.append(EQUALS);
       str.append(data);
       str.append(DELIMITER);
     }
   }
   
   public void setFile (File file) {
     this.file = file;
     path = file.getAbsolutePath();
     try {
       path = file.getCanonicalPath();
     } catch (java.io.IOException e) {
       
     }
   }
   
   public void setPath(String path) {
     this.path = path;
     file = new File(path);
   }
   
   public File getFile() {
     return file;
   }
   
   public File getFolder() {
     if (file == null) {
       return null;
     }
     else
     if (! file.exists()) {
       return file;
     }
     else
     if (file.isDirectory()) {
       return file;
     }
     else
     if (file.isFile()) {
       return file.getParentFile();
     } else {
       return null;
     }
 
   }
   
   /**
    Indicate whether the given file is accessible in the file system. 
   
    @return True if a file exists, false otherwise. 
   */
   public boolean exists() {
     if (file != null) {
       return file.exists();
     } else {
       return false;
     }
   }
 
   /**
    Set the type of file specification. Could be a file, url, etc.
 
    @param type File, url, etc.
    */
   public void setType (String type) {
     this.type = type;
   }
 
   /**
    Get the type of file specification.
 
    @return File, url, etc.
    */
   public String getType () {
     return type;
   }
 
   /**
    Does the file specification have a path?
 
    @return True if the path has a non-zero length.
    */
   public boolean hasPath () {
     return (path.length() > 0);
   }
 
   /**
    Return the path of the file or url.
 
    @return Name of the file or url.
    */
   public String getPath () {
     return path;
   }
 
   /**
    Return a path suitable for display. Currently replaces occurrences of "%20"
    with spaces.
 
    @return A path suitable for display.
    */
   public String getDisplayName () {
     StringBuffer work = new StringBuffer (path);
     int i = 0;
     while (i >= 0) {
       i = work.indexOf("%20", i);
       if (i >= 0) {
         work.delete (i, i + 3);
         work.insert (i, " ");
         i++;
       } // end if search string was found
     } // end while still finding occurrences of the search string
     return work.toString();
   }
 
   /**
    Replaces occurrences of "%20" with spaces, and only show the file name
    and as many of its enclosing folders as can fit in a reasonable length.
 
    @return A brief path suitable for display.
    */
   public String getBriefDisplayName () {
     String displayName = getDisplayName();
     int lastSlashIndex = -1;
     int j = displayName.length() - 1;
     int length = 0;
     while (j >= 0 && length < BRIEF_DISPLAY_NAME_MAX_LENGTH) {
       if (displayName.charAt(j) == '/' || displayName.charAt(j) == '\\') {
         lastSlashIndex = j;
       }
       j--;
       length++;
     }
     return displayName.substring(lastSlashIndex + 1);
   }
 
   /**
    Set the format of the data contained in the file.
 
    @param format Indicator of the format of the data contained in the file.
    */
   public void setFormat (String format) {
     this.format = format;
   }
 
   /**
    Return the format of the data contained in the file.
 
    @return Indicator of the format of the data contained in the file.
    */
   public String getFormat () {
     return format;
   }
 
   /**
    Set the lastAccessDate on which the file was last accessed by the application currently
    running.
 
    @param dateStr A string representing the lastAccessDate on which the file was last
                   accessed by the application currently running.
    */
   public void setLastAccessDate (String dateStr) {
     DateFormat formatter = DateFormat.getDateTimeInstance();
     if (dateStr.length() == 0) {
       setLastAccessDateToNow();
     } else {
       try {
         lastAccessDate = formatter.parse(dateStr);
       } catch (ParseException e) {
         setLastAccessDateToNow();
       } // end catch Parse Exception
     } // end if lastAccessDate string length > 0
   } // end method setLastAccessDate
 
   /**
    Set the lastAccessDate and time on which the file was last accessed by the
    application currently running.
 
    @param lastAccessDate    A string representing the lastAccessDate on which the file was last
                   accessed by the application currently running.
    */
   public void setLastAccessDate (Date lastAccessDate) {
     this.lastAccessDate = lastAccessDate;
   }
 
   /**
    Set the lastAccessDate and time to right now.
    */
   public void setLastAccessDateToNow () {
     lastAccessDate = new Date();
   }
 
   /**
    Get the lastAccessDate and time on which the currently running application last
    accessed this file.
 
    @return The lastAccessDate and time on which the currently running application last
    accessed this file.
    */
   public Date getLastAccessDate () {
     return lastAccessDate;
   }
   
   /**
    Get the lastAccessDate and time on which the currently running application last
    accessed this file.
 
    @return The lastAccessDate and time on which the currently running application last
    accessed this file.
    */
   public String getLastAccessDateAsString () {
     DateFormat formatter = DateFormat.getDateTimeInstance();
     return (formatter.format(getLastAccessDate()));
   }
 
   /**
    Set the lastBackupDate on which the file was last accessed by the application currently
    running.
 
    @param dateStr A string representing the lastBackupDate on which the file was last
                   accessed by the application currently running.
    */
   public void setLastBackupDate (String dateStr) {
     DateFormat formatter = DateFormat.getDateTimeInstance();
     if (dateStr.length() == 0) {
       setLastBackupDateToNow();
     } else {
       try {
         lastBackupDate = formatter.parse(dateStr);
       } catch (ParseException e) {
         setLastBackupDateToNow();
       } // end catch Parse Exception
     } // end if lastBackupDate string length > 0
   } // end method setLastBackupDate
 
   /**
    Set the lastBackupDate and time on which the file was last accessed by the
    application currently running.
 
    @param lastBackupDate    A string representing the lastBackupDate on which the file was last
                   accessed by the application currently running.
    */
   public void setLastBackupDate (Date lastBackupDate) {
     this.lastBackupDate = lastBackupDate;
   }
 
   /**
    Set the lastBackupDate and time to right now.
    */
   public void setLastBackupDateToNow () {
     lastBackupDate = new Date();
   }
 
   /**
    Get the lastBackupDate and time on which the currently running application last
    accessed this file.
 
    @return The lastBackupDate and time on which the currently running application last
    accessed this file.
    */
   public Date getLastBackupDate () {
     return lastBackupDate;
   }
   
   /**
    Get the lastBackupDate and time on which the currently running application last
    accessed this file.
 
    @return The lastBackupDate and time on which the currently running application last
    accessed this file.
    */
   public String getLastBackupDateAsString () {
     DateFormat formatter = DateFormat.getDateTimeInstance();
     return (formatter.format(getLastBackupDate()));
   }
   
   public void setBackupFolder (File backupFolder) {
     if (backupFolder.isFile()) {
       backupFolder = backupFolder.getParentFile();
     }
     try {
       this.backupFolder = backupFolder.getCanonicalPath();
     } catch (java.io.IOException e) {
       this.backupFolder = backupFolder.getAbsolutePath();
     }
   }
   
   public void setBackupFolder (String backupFolder) {
     this.backupFolder = backupFolder;
   }
   
   public String getBackupFolder () {
     return backupFolder;
   }
   
   public String toString() {
     return path;
   }
   
 }
