 package info.mikaelsvensson.ftpbackup.model;
 
 import info.mikaelsvensson.ftpbackup.util.I18n;
 
 import javax.xml.bind.annotation.XmlEnumValue;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public enum ArchiveFileNameTemplate {
     @XmlEnumValue("dot-date")
     DOT_DATE {
         @Override
         public String getArchivedFilePathFromRoot(String name, Date date, char pathSeparator) {
             return name + "." + YYYYMMDD_HHMMSS_DATEFORMAT.format(date);
         }
 
         @Override
         public Date getArchivingDate(String filename) {
             Matcher matcher = YYYYMMDD_HHMMSS_END_OF_STRING_PATTERN.matcher(filename);
             if (matcher.matches()) {
                 try {
                     String formattedDate = matcher.group(2);
                     return YYYYMMDD_HHMMSS_DATEFORMAT.parse(formattedDate);
                 } catch (ParseException e) {
                     return null;
                 }
             }
             return null;
         }
 
         @Override
         public String getOriginalFileName(String filename, char pathSeparator) {
             Matcher matcher = YYYYMMDD_HHMMSS_END_OF_STRING_PATTERN.matcher(filename);
             if (matcher.matches()) {
                 return matcher.group(1);
             }
             return null;
         }
     },
     @XmlEnumValue("archive-folder")
     ROOT_ARCHIVE_FOLDER {
         @Override
         public String getArchivedFilePathFromRoot(String name, Date date, char pathSeparator) {
             return ARCHIVE_FOLDER_NAME + pathSeparator + name + "." + YYYYMMDD_HHMMSS_DATEFORMAT.format(date);
         }
 
         @Override
         public Date getArchivingDate(String filename) {
             Matcher matcher = YYYYMMDD_HHMMSS_END_OF_STRING_PATTERN.matcher(filename);
             if (matcher.matches()) {
                 try {
                     String formattedDate = matcher.group(2);
                     return YYYYMMDD_HHMMSS_DATEFORMAT.parse(formattedDate);
                 } catch (ParseException e) {
                     return null;
                 }
             }
             return null;
         }
 
         @Override
         public String getOriginalFileName(String filename, char pathSeparator) {
             Matcher matcher = YYYYMMDD_HHMMSS_END_OF_STRING_PATTERN.matcher(filename.substring(ARCHIVE_FOLDER_NAME.length() + 1));
             if (matcher.matches()) {
                 return matcher.group(1);
             }
             return null;
         }
     },
     @XmlEnumValue("per-date-archive-folder")
     PER_DATE_ARCHIVE_FOLDER {
         @Override
         public String getArchivedFilePathFromRoot(String name, Date date, char pathSeparator) {
             return ARCHIVE_FOLDER_NAME + pathSeparator + YYYYMMDD_HHMMSS_DATEFORMAT.format(date) + pathSeparator + name;
         }
 
         @Override
         public Date getArchivingDate(String filename) {
             try {
                 int beginIndex = ARCHIVE_FOLDER_NAME.length() + 1;
                 int endIndex = beginIndex + YYYYMMDD_HHMMSS.length();
                 if (endIndex < filename.length()) {
                     String formattedDate = filename.substring(beginIndex, endIndex);
                     return YYYYMMDD_HHMMSS_DATEFORMAT.parse(formattedDate);
                 } else {
                     return null;
                 }
             } catch (ParseException e) {
                 return null;
             }
         }
 
         @Override
         public String getOriginalFileName(String filename, char pathSeparator) {
             return filename.substring(ARCHIVE_FOLDER_NAME.length() + 1 + YYYYMMDD_HHMMSS.length() + 1);
         }
     };
 
 // ------------------------------ FIELDS ------------------------------
 
     private static final String ARCHIVE_FOLDER_NAME = ".archive";
     private static final String HHMMSS_PATTERN = "([01][0-9]|2[0-3])[0-5][0-9][0-5][0-9])";
     private static final String YYYYMMDD_HHMMSS = "yyyyMMdd-HHmmss";
     private static final SimpleDateFormat YYYYMMDD_HHMMSS_DATEFORMAT = new SimpleDateFormat(YYYYMMDD_HHMMSS);
     private static final String YYYYMMDD_PATTERN = "((19|20)[0-9]{2}(0[0-9]|1[0-2])([012][0-9]|3[01])";
    private static final Pattern YYYYMMDD_HHMMSS_END_OF_STRING_PATTERN = Pattern.compile("(.*)\\." + YYYYMMDD_PATTERN + "-" + HHMMSS_PATTERN + "$");
 
 // ------------------------ CANONICAL METHODS ------------------------
 
     @Override
     public String toString() {
         return I18n.text(I18n.CORE_STRINGS, ArchiveFileNameTemplate.class, name(), new Object[]{});
     }
 
 // -------------------------- OTHER METHODS --------------------------
 
     public abstract String getArchivedFilePathFromRoot(String name, Date date, char pathSeparator);
 
     public abstract String getOriginalFileName(String filename, char pathSeparator);
 
     /**
      * Determines if specified file is an archive file, according to the current "archiving template".
      *
      * @param filename
      * @return
      */
     public boolean isArchivedFile(String filename) {
         return getArchivingDate(filename) != null;
     }
 
     public abstract Date getArchivingDate(String filename);
 }
