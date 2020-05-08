 package com.kn.groupBuilder.FileOperations.Writer;
 
 import java.io.File;
 import java.util.Date;
 
 import com.kn.groupBuilder.Exceptions.NotToHandleException;
 import com.kn.groupBuilder.Storage.Pojo;
 
 import dennis.markmann.MyLibraries.DefaultJobs.DateHelper;
 
 /**
  * Used to delete all different kind of folders.
  * 
  * @author dennis.markmann
  * @since JDK.1.7.0_21
  * @version 1.0
  */
 
 public class FileCleaner {
 
     final void updateArchive(final Pojo pojo) {
 
        final File filePath = new File(pojo.getSettings().getPath() + "Archive\\");
        try {
            for (final File file : filePath.listFiles()) {
                if (pojo.getSettings().isArchived() == false || this.checkDeletionDate(file.getName(), pojo)) {
                    this.cleanFolder(file.getPath());
                 }
             }
        } catch (final java.lang.NullPointerException e) {
            new NotToHandleException(e.getStackTrace());
         }
     }
 
     public final void cleanFolder(final String path) {
 
         final File filePath = new File(path);
         try {
             for (final File file : filePath.listFiles()) {
                 if (!file.isDirectory()) {
                     file.delete();
                 } else {
                     this.cleanFolder(file.getAbsolutePath());
                 }
             }
             filePath.delete();
         } catch (final java.lang.NullPointerException e) {
             new NotToHandleException(e.getStackTrace());
         }
     }
 
     private boolean checkDeletionDate(final String fileName, final Pojo pojo) {
         final DateHelper helper = new DateHelper();
 
         final Date archiveDate = helper.parseStringToDate(fileName.substring(0, fileName.indexOf("_")));
         helper.addTime(0, 0, -pojo.getSettings().getArchivingDays(), 0, 0, 0);
         final Date deletionDate = helper.parseStringToDate(helper.getDate());
 
        if (archiveDate.before(deletionDate) || archiveDate.equals(deletionDate)) {
             return true;
         }
         return false;
 
     }
 }
