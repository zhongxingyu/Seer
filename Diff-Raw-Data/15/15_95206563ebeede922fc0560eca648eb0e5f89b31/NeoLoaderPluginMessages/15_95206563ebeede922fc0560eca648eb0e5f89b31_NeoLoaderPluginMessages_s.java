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
 package org.amanzi.neo.loader.internal;
 
 import java.util.ResourceBundle;
 
 import org.eclipse.osgi.util.NLS;
 
 public class NeoLoaderPluginMessages extends NLS {
     
     private static final String BUNDLE_NAME = NeoLoaderPluginMessages.class.getName();
 
     private static ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE_NAME);
     
     public static String DriveDialog_RemoveButtonText;
     public static String DriveDialog_AddButtonText;
     public static String DriveDialog_BrowseButtonText;
     public static String DriveDialog_LoadButtonText;
     public static String DriveDialog_CancelButtonText;
     public static String DriveDialog_FilesToLoadListLabel;
     public static String DriveDialog_FilesToChooseListLabel;
     public static String DriveDialog_DialogTitle;
     public static String DriveDialog_FileDialogTitle;
     public static String DriveDialog_MonitorName;
     public static String Console_ErrorOnClose;
     public static String NetworkDialog_DialogTitle;
 	public static String DriveDialog_AddAllButtonText;
 	public static String DriveDialog_RemoveAllButtonText;
 	public static String DriveDialog_DatasetLabel;
     public static String ADD_LAYER_MESSAGE;
     public static String ADD_NEW_MAP_MESSAGE;
     public static String ADD_LAYER_TITLE;
     public static String TOGLE_MESSAGE;
     public static String CorrelateDialog_DialogTitle;
     public static String CorrelateDialog_CorrelateButtonText;
     
     private NeoLoaderPluginMessages() {
     }
 
     public static ResourceBundle getResourceBundle() {
         return resourceBundle;
     }
 
     static {
         NLS.initializeMessages(BUNDLE_NAME, NeoLoaderPluginMessages.class);
     }
 
 }
