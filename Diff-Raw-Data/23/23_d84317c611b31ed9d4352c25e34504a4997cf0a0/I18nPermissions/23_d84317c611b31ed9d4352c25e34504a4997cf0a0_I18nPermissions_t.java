 package org.synyx.minos.i18n;
 
 import java.util.ArrayList;
 import java.util.List;
 
 
 /**
  * Permissions for i18n
  *
  * @author Marc Kannegiesser - kannegiesser@synyx.de
  */
 public abstract class I18nPermissions {
 
     public static final String I18N_CREATE_LANGUAGES = "I18N_CREATE_LANGUAGES";
     public static final String I18N_DELETE_LANGUAGES = "I18N_DELETE_LANGUAGES";
     public static final String I18N_LIST_LANGUAGES = "I18N_LIST_LANGUAGES";
     public static final String[] I18N_MANAGE_LANGUAGES = {
         I18N_CREATE_LANGUAGES, I18N_DELETE_LANGUAGES, I18N_LIST_LANGUAGES
     };
 
     public static final String I18N_LIST_BASES = "I18N_LIST_BASES";
     public static final String[] I18N_MANAGE_BASES = { I18N_LIST_BASES };
 
     public static final String I18N_EDIT_MESSAGES = "I18N_EDIT_MESSAGES";
     public static final String I18N_LIST_MESSAGES = "I18N_LIST_MESSAGES";
     public static final String[] I18N_MANAGE_MESSAGES = { I18N_EDIT_MESSAGES, I18N_LIST_MESSAGES };
 
     public static final String I18N_IMPORT_MESSAGES = "I18N_IMPORT_MESSAGES";
     public static final String I18N_EXPORT_MESSAGES = "I18N_EXPORT_MESSAGES";
     public static final String I18N_UPLOAD_MESSAGES = "I18N_UPLOAD_MESSAGES";
     public static final String[] I18N_IMPORT_EXPORT_MESSAGES = {
         I18N_IMPORT_MESSAGES, I18N_EXPORT_MESSAGES, I18N_UPLOAD_MESSAGES
     };
 
     public static final String I18N_REINITIALIZE_MESSAGES = "I18N_REINITIALIZE_MESSAGES";
 
     public static final String[] I18N_ADMINISTRATION;
 
     static {
         List<String> allpermissions = new ArrayList<String>();
 
         for (String p : I18N_MANAGE_LANGUAGES) {
             allpermissions.add(p);
         }
 
         for (String p : I18N_MANAGE_BASES) {
             allpermissions.add(p);
         }
 
         for (String p : I18N_MANAGE_MESSAGES) {
             allpermissions.add(p);
         }
 
         for (String p : I18N_IMPORT_EXPORT_MESSAGES) {
             allpermissions.add(p);
         }
 
         allpermissions.add(I18N_REINITIALIZE_MESSAGES);
        I18N_ADMINISTRATION = allpermissions.toArray(new String[allpermissions.size()]);
     }
 
     /**
      * Private constructor to prevent instantation and subclassing.
      */
     private I18nPermissions() {
     }
 }
