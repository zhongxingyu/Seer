 /* *****************************************************************************
  * DependencyTracker.java
 * ****************************************************************************/
 
 /* J_LZ_COPYRIGHT_BEGIN *******************************************************
 * Copyright 2001-2004, 2008 Laszlo Systems, Inc.  All Rights Reserved.              *
 * Use is subject to license terms.                                            *
 * J_LZ_COPYRIGHT_END *********************************************************/
 
 package org.openlaszlo.cm;
 
 import org.apache.log4j.*;
 import java.io.*;
 import java.util.*;
 import org.openlaszlo.server.LPS;
 import org.openlaszlo.utils.ChainedException;
 
 /** Tracks compilation dependencies, so that it can tell if a file
  * needs to be recompiled.  An instance stores version information
  * about all the source files that an object file depends on (the
  * files such that, if one changed, the object file would be out of
  * date).
  *
  * @author Oliver steele
  */
 public class DependencyTracker implements java.io.Serializable {
     private static Logger mLogger  = Logger.getLogger(DependencyTracker.class);
 
     /** Records information about the version of a file.
      */
     private static class FileInfo implements java.io.Serializable {
         /** pathname */
         private String mPathname;
         /** last mod time */
         private long mLastMod;
         /** can read? */
         private boolean mCanRead;
         /** File length */
         private long mLength;
         
         /** Constructs an instance.
          * @param pathname the name of a file
          */
         FileInfo(String pathname) {
             mPathname = pathname;
             File file = new File(pathname);
             // Cope with directory indexes for now
             // FIXME: [2003-05-09 bloch] is this the right place
             // for this?
             if (file.isDirectory()) {
                 file = new File(pathname + File.separator + "library.lzx");
             }
             // Truncate to an seconds
             mLastMod = ((long)(file.lastModified() / 1000L)) * 1000L;
             mCanRead = file.canRead();
             //mLogger.debug("lm: " + mLastMod);
             mLength = file.length();
         }
         
         /** Returns true iff this FileInfo has up to date information
          * compared to the fileinfo argument.
          * @param info another FileInfo
          * @return see documentation
          */
         boolean isUpToDate(FileInfo info) {
             return this.mLastMod == info.mLastMod
                 && this.mCanRead == info.mCanRead
                 && this.mLength == info.mLength;
         }
     };
 
     /** A list of FileInfo records for files that are depended on. */
     private final Vector mDependencies = new Vector();
     private Properties mProperties;
     private String mWebappPath;
 
     public DependencyTracker(Properties properties) {
         this.mProperties = properties;
         this.mWebappPath = LPS.HOME(); // get it from global
     }
     
     /** Add the specified file to the list of file dependencies.
      * @param file a file
      */
     public void addFile(File file) {
        mLogger.debug("addFile Path is " + file.getPath());
         FileInfo fi = new FileInfo(file.getPath());
         try {
             fi.mPathname = file.getCanonicalPath();
         } catch (java.io.IOException e) {
             throw new ChainedException(e);
         }
         mDependencies.add(fi);
     }
 
 
     /**
      * Copy file info from the given tracker to me, omitting
      * omitting then given file.
      */
     public void copyFiles(DependencyTracker t, File omitMe) {
         try {
             for (Iterator e = t.mDependencies.iterator(); e.hasNext(); ) {
                 FileInfo f = (FileInfo)e.next();
                 if (! f.mPathname.equals(omitMe.getCanonicalPath())) {
                     mDependencies.add(f);
                 }
             }
         } catch (java.io.IOException e) {
             throw new ChainedException(e);
         }
     }
 
 
     /** This will update the FileInfo object chain to use the (possibly new)
      * webappPath once the DependencyTracker object has been reconstitutded
      * from ondisk cache.
      */
     public void updateWebappPath() {
         String webappPath = LPS.HOME(); // get it from global
         if (webappPath.equals(mWebappPath))
             return;
         mLogger.debug(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="updating webappPath from: " + p[0]
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 DependencyTracker.class.getName(),"051018-128", new Object[] {mWebappPath})
 );
         mLogger.debug(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="updating webappPath to:   " + p[0]
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 DependencyTracker.class.getName(),"051018-136", new Object[] {webappPath})
 );
         for (Iterator e = mDependencies.iterator(); e.hasNext(); ) {
             FileInfo saved = (FileInfo) e.next();
             if (saved.mPathname.startsWith(mWebappPath)) {
                 mLogger.debug(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="updating dependencies from: " + p[0]
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 DependencyTracker.class.getName(),"051018-147", new Object[] {saved.mPathname})
 );
                 saved.mPathname = webappPath +
                         saved.mPathname.substring(mWebappPath.length());
                 mLogger.debug(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="updating dependencies to  : " + p[0]
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 DependencyTracker.class.getName(),"051018-157", new Object[] {saved.mPathname})
 );
             }
         }
         mWebappPath = webappPath;
     }
     
     public boolean isUpToDate(Properties properties) {
         return isUpToDate(properties, null);
     }
 
     /** Returns true iff all the files listed in this tracker's
      * dependency list exist and are at the same version as when they
      * were recorded.
      * @return a boolean
      */
     public boolean isUpToDate(Properties properties, List staleDeps) 
     {
         Iterator e;
         
         // fixes bug 962 
         {
             if (mProperties.size() != properties.size()) {
                 mLogger.debug(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="my size " + p[0]
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 DependencyTracker.class.getName(),"051018-181", new Object[] {new Integer(mProperties.size())})
 );
                 mLogger.debug(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="new size " + p[0]
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 DependencyTracker.class.getName(),"051018-189", new Object[] {new Integer(properties.size())})
 );
                 return false;
             }
 
             for (e = mProperties.keySet().iterator(); e.hasNext(); ) {
                 String key = (String) e.next();
                 String val0 = mProperties.getProperty(key);
                 String val1 = properties.getProperty(key);
 
                 // val0 can't be null; properties don't allow that
                 if (val1 == null || ! val0.equals(val1)) {
                     mLogger.debug(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="Missing or changed property: " + p[0]
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 DependencyTracker.class.getName(),"051018-207", new Object[] {val0})
 );
                     return false;
                 }
             }
         }
         
         boolean isUpToDate = true;
 
         for (e = mDependencies.iterator(); e.hasNext(); ) {
             FileInfo saved = (FileInfo) e.next();
             FileInfo current = new FileInfo(saved.mPathname);
             if (!saved.isUpToDate(current)) {
                 mLogger.debug(saved.mPathname + " has changed");
                 mLogger.debug("was " + saved.mLastMod);
                 mLogger.debug(" is " + current.mLastMod);
                 
                 isUpToDate = false;
                 
                 if (staleDeps != null) {
                     staleDeps.add(new File(saved.mPathname));
                 } else {
                     break;
                 }
             }
         }
         
         return isUpToDate;
     }
 }
