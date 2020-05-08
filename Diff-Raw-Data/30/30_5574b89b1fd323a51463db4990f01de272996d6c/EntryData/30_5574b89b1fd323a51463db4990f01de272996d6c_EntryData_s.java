 package Ide.desktopentry;
 
 import java.util.logging.Logger;
 
 import Ide.locale.LcMessages;
 
 final class EntryData {
   
   private final static Logger log = Logger.getLogger(EntryData.class.getCanonicalName());
   
   String type = null;
   LocalizedKey name = new LocalizedKey();
   LocalizedKey genericName = new LocalizedKey();
   boolean visible = true;
   LocalizedKey comment = new LocalizedKey();
   LocalizedKey icon = new LocalizedKey();
   String tryExec;
   Exec exec;
   
   private String nameString;
   private String genericNameString;
   private String commentString;
   private String iconString;
   
   private boolean nameSet;
   private boolean genericNameSet;
   private boolean commentSet;
   private boolean iconSet;
   private boolean execSet;
   
   public boolean validate(String filePath, LcMessages lcMessages) {
     if (type == null) {
       log.warning(filePath + " doesn't contain Type key! File is invalid.");
       return false;
     }
     
     try {
       nameString = name.value(lcMessages);
       nameSet = true;
     } catch (DefaultLocalizedKeyMissingException e) {
       log.warning(filePath + " doesn't contain Name key! File is invalid.");
       return false;
     }
     if (nameString == null) {
       log.warning(filePath + " doesn't contain Name key! File is invalid.");
       return false;
     }
     
     try {
       genericNameString = genericName.value(lcMessages);
       genericNameSet = true;
     } catch (DefaultLocalizedKeyMissingException e) {
       log.warning(filePath + " doesn't contain GenericName key, but contains its translated versions! File is invalid.");
       return false;
     }
     
     try {
       commentString = comment.value(lcMessages);
       commentSet = true;
     } catch (DefaultLocalizedKeyMissingException e) {
       log.warning(filePath + " doesn't contain Comment key, but contains its translated versions! File is invalid.");
       return false;
     }
     
     try {
       iconString = icon.value(lcMessages);
       iconSet = true;
     } catch (DefaultLocalizedKeyMissingException e) {
       log.warning(filePath + " doesn't contain Icon key, but contains its translated versions! File is invalid.");
       return false;
     }
     
     if (exec == null) {
      log.warning(filePath + " doesn't contain Exec key. File is invalid.");
      return false;
     }
     
     return true;
   }
 
   public String getNameString() {
     check(nameSet);
     return nameString;
   }
 
   private void check(boolean o) {
     if (!o) throw new IllegalStateException("validate() hasn't been run!");
   }
 
   public String getGenericNameString() {
     check(genericNameSet);
     return genericNameString;
   }
 
   public String getCommentString() {
     check(commentSet);
     return commentString;
   }
   
   public String getIconString() {
     check(iconSet);
     return iconString;
   }
   
   public Exec getExec() {
     check(execSet);
     return exec;
   }
 }
