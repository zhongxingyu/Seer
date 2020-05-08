 //
 // Copyright (c) 2010 by jbo - Josef Baro
 // 
 // Project: jbogx2D
 // File: Version.java
 // Created: 20.02.2010 - 20:20:47
 //
 package de.jbo.jbogx2d.base;
 
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 /**
  * Defines version information.
  * 
  * @author Josef Baro (jbo) <br>
  * @version 20.02.2010 jbo - created <br>
  */
 public class Version {
     /** Major version. */
     private int versionMajor = 0;
 
     /** Minor version. */
     private int versionMinor = 0;
 
     /** Bugfix version. */
     private int versionBugfix = 0;
 
     /** Build date timestamp. */
     private Calendar versionBuildDate = new GregorianCalendar();
 
     /**
      * Creates a new instance.
      * 
      * @param major
      *            The major version.
      * @param minor
      *            The minor version.
      * @param bugfix
      *            The bugfix version.
      * @param buildDate
      *            The build-date.
      */
     public Version(int major, int minor, int bugfix, Calendar buildDate) {
         versionMajor = major;
         versionMinor = minor;
         versionBugfix = bugfix;
         versionBuildDate = buildDate;
     }
 
     /**
      * Returns the current major-version.
      * 
      * @return The current major-version.
      */
     public final int getVersionMajor() {
         return versionMajor;
     }
 
     /**
      * Returns the current minor-version.
      * 
      * @return The current minor-version.
      */
     public final int getVersionMinor() {
         return versionMinor;
     }
 
     /**
      * Returns the current bugfix-version.
      * 
      * @return The current bugfix-version.
      */
     public final int getVersionBugfix() {
         return versionBugfix;
     }
 
     /**
      * Returns the build date.
      * 
      * @return The build date.
      */
     public final Calendar getVersionBuildDate() {
         return versionBuildDate;
     }
 
     /**
      * Returns a string-version of this instance. <br>
      * <b>Format:</b><br>
      * 
      * <pre>
      * major.minor.bugfix bYYYYMMddhhmmss
      * </pre>
      * 
      * @return String version.
      */
     public final String getVersionString() {
         return toString();
     }
 
     /*
      * @see java.lang.Object#toString()
      */
     @Override
    public String toString() {
         int temp = 0;
         StringBuffer buffer = new StringBuffer();
 
         buffer.append(getVersionMajor());
         buffer.append('.');
         buffer.append(getVersionMinor());
         buffer.append('.');
         buffer.append(getVersionBugfix());
         buffer.append(' ');
         buffer.append('b');
         temp = versionBuildDate.get(Calendar.YEAR);
         if (temp < 10) {
             buffer.append(0);
         }
         buffer.append(temp);
         temp = versionBuildDate.get(Calendar.MONTH) + 1;
         if (temp < 10) {
             buffer.append(0);
         }
         buffer.append(temp);
         temp = versionBuildDate.get(Calendar.DAY_OF_MONTH);
         if (temp < 10) {
             buffer.append(0);
         }
         buffer.append(temp);
         temp = versionBuildDate.get(Calendar.HOUR_OF_DAY);
         if (temp < 10) {
             buffer.append(0);
         }
         buffer.append(temp);
         temp = versionBuildDate.get(Calendar.MINUTE);
         if (temp < 10) {
             buffer.append(0);
         }
         buffer.append(temp);
         temp = versionBuildDate.get(Calendar.SECOND);
         if (temp < 10) {
             buffer.append(0);
         }
         buffer.append(temp);
 
         return buffer.toString();
     }
 }
