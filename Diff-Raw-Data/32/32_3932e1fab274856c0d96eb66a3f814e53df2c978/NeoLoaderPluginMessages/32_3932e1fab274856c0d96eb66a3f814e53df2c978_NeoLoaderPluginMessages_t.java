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
     public static String CorrelateDialog_select_probe_data;
     public static String CorrelateDialog_select_drive_data;
     public static String ETSIImport_page_title;
     public static String ETSIImport_page_descr;
     public static String ETSIImport_dataset;
     public static String ETSIImport_network;
     public static String ETSIImport_directory;
     public static String ETSIImport_dir_editor_title;
     
     public static String PrefNetwork_title;
     public static String PrefNetwork_field_city;
     public static String PrefNetwork_field_msc;
     public static String PrefNetwork_field_bsc;
     public static String PrefNetwork_field_site;
     public static String PrefNetwork_field_sector;
     public static String PrefNetwork_field_latitude;
     public static String PrefNetwork_field_longitude;
     
     public static String PrefSite_title;
     public static String PrefSite_field_site;
     public static String PrefSite_field_beamwidth;
     public static String PrefSite_field_azimuth;
 
     public static String PrefProbe_title;
     public static String PrefProbe_field_name;
     public static String PrefProbe_field_probe_type;
     public static String PrefProbe_field_latitude;
     public static String PrefProbe_field_longitude;
 
     public static String PrefNeighbour_title;
     public static String PrefNeighbour_title_server;
     public static String PrefNeighbour_title_neighbour;
     public static String PrefNeighbour_field_ci;
     public static String PrefNeighbour_field_bts;
     public static String PrefNeighbour_field_lac;
     public static String PrefNeighbour_field_adj_ci;
     public static String PrefNeighbour_field_adj_bts;
     public static String PrefNeighbour_field_adj_lac;
     
     public static String GpehWindowTitle;
     public static String GpehTitle;
     public static String GpehDescr;
     public static String GpehLbOSS;
     public static String GpehImportDirEditorTitle;
 
    public static String NetworkSiteImportWizard_PAGE_TITLE;
    public static String NetworkSiteImportWizard_PAGE_DESCR;
    public static String NetworkSiteImportWizard_NETWORK;
    public static String NetworkSiteImportWizard_FILE; 
    public static String NetworkSiteImportWizard_DATA_TYPE;
    
     public static String OSS;
     
     private NeoLoaderPluginMessages() {
     }
 
     public static ResourceBundle getResourceBundle() {
         return resourceBundle;
     }
 
     static {
         NLS.initializeMessages(BUNDLE_NAME, NeoLoaderPluginMessages.class);
     }
 
 }
