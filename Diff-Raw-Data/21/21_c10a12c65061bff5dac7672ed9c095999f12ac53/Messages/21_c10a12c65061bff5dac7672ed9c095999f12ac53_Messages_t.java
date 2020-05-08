 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 package org.amanzi.splash.utilities;
 
 import java.text.MessageFormat;
 import java.util.ResourceBundle;
 
 import org.eclipse.osgi.util.NLS;
 
 public class Messages extends NLS {
 
     private static final String BUNDLE_NAME = Messages.class.getName();
     private static ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE_NAME);
     
     public static String Copy_Error_Title;
     public static String Open_Spreadsheet_Error_Title;
     public static String Open_Spreadsheet_Error_Message;
     
     public static String Format_Error_Title;
     public static String Format_Error_Message;
     
     public static String Wizard_Error_Message;
     public static String File_Import_Wizard_Title;
     public static String Excel_Import_Title;
     public static String Excel_Import_Page_Description;
     public static String File_Editor_Text;
     public static String CSV_Import_Title;
     public static String CSV_Imoprt_Page_Description;
     public static String Import_Wizard_New_File_Name;
     
     public static String Import_Builder_Add_Button_Name;
     public static String Import_Builder_Delete_Button_Name;
     public static String Import_Builder_Run_Button_Name;
     public static String Import_Builder_Filter_Filename_Field;
     public static String Import_Builder_Create_Script_Job_Name;
     
    public static String Import_Job; 
    
     private Messages() {
     }
 
     public static ResourceBundle getResourceBundle() {
         return resourceBundle;
     }
     
     public static String getFormattedString(String key, String ... args) {
         return MessageFormat.format(key, (Object[])args);
     }
 
     static {
         NLS.initializeMessages(BUNDLE_NAME, Messages.class);
     }
     
 }
