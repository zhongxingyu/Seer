 package com.kn.groupBuilder.Storage;
 
 import javax.print.PrintService;
 
 /**
  * Storage for the settings used in the application. Contains various elements that can be saved via XML file.
  * 
  * @author dennis.markmann
  * @since JDK.1.7.0_21
  * @version 1.0
  */
 
 public class Settings { // NO_UCD
 
    private String path = System.getProperty("user.home") + "\\GroupBuilder\\";;
 
     private String language = "english"; // german or english
     private boolean archive = true;
     private int archivingDays = 7; // if the value is 0 the archiving will be disabled
     private boolean sendMailAutomatically = false;
     private boolean printAutomatically = false;
     private PrintService printService;
 
     public final String getLanguage() {
         return this.language;
     }
 
     public final void setLanguage(final String language) {
         this.language = language;
     }
 
     public final boolean isArchived() {
         return this.archive;
     }
 
     public final void setArchived(final boolean archive) {
         this.archive = archive;
     }
 
     public final int getArchivingDays() {
         return this.archivingDays;
     }
 
     public final void setArchivingDays(final int archivingDays) {
         this.archivingDays = archivingDays;
     }
 
     public final boolean isSendMailAutomatically() {
         return this.sendMailAutomatically;
     }
 
     public final void setSendMailAutomatically(final boolean sendMailAutomatically) {
         this.sendMailAutomatically = sendMailAutomatically;
     }
 
     public final boolean isPrintAutomatically() {
         return this.printAutomatically;
     }
 
     public final void setPrintAutomatically(final boolean printAutomatically) {
         this.printAutomatically = printAutomatically;
     }
 
     public final String getPath() {
         return this.path;
     }
 
     public final void setPath(final String path) {
         this.path = path;
     }
 
     public final PrintService getPrintService() {
         return this.printService;
     }
 
     public final void setPrintService(final PrintService printService) {
         this.printService = printService;
     }
 }
