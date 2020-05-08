 package org.amanzi.neo.loader.ui;
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
 
 
 import java.text.MessageFormat;
 import java.util.ResourceBundle;
 
 import org.eclipse.osgi.util.NLS;
 
 public class NeoLoaderPluginMessages extends NLS {
     
     private static final String BUNDLE_NAME = NeoLoaderPluginMessages.class.getName();
 
     private static ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE_NAME);
     
    public static String NetworkConfigurationImportWizard_PAGE_DESCR;
    
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
     
     public static String TemsImportWizard_PAGE_TITLE;
     public static String TemsImportWizard_PAGE_DESCR;
     
     public static String Console_ErrorOnClose;
     public static String NetworkDialog_DialogTitle;
 	public static String DriveDialog_AddAllButtonText;
 	public static String DriveDialog_RemoveAllButtonText;
 	public static String DriveDialog_DatasetLabel;
     public static String ADD_LAYER_MESSAGE;
     public static String ADD_NEW_MAP_MESSAGE;
     public static String ADD_LAYER_TITLE;
     public static String TOGLE_MESSAGE;
     public static String TOGLE_MESSAGE_ADD_PROBE;
     public static String TOGLE_MESSAGE_ADD_EVENTS;
     public static String TOGLE_MESSAGE_ADD_CALLS;
     public static String CorrelateDialog_DialogTitle;
     public static String CorrelateDialog_CorrelateButtonText;
     public static String CorrelateDialog_select_probe_data;
     public static String CorrelateDialog_select_drive_data;
     public static String AMSImport_page_title;
     public static String AMSImport_page_descr;
     public static String AMSImport_dataset;
     public static String AMSImport_network;
     public static String AMSImport_directory;
     public static String AMSImport_dir_editor_title;
     
     public static String PrefNetwork_title_network;
     public static String PrefNetwork_title_sector;
     public static String PrefNetwork_title_site;
     public static String PrefNetwork_title_oprional;
     public static String PrefNetwork_field_city;
     public static String PrefNetwork_field_msc;
     public static String PrefNetwork_field_bsc;
     public static String PrefNetwork_field_site;
     public static String PrefNetwork_field_sector;
     public static String PrefNetwork_field_sector_ci;
     public static String PrefNetwork_field_sector_lac;
     public static String PrefNetwork_field_latitude;
     public static String PrefNetwork_field_longitude;
     public static String PrefNetwork_field_beamwidth;
     public static String PrefNetwork_field_azimuth;
 
     public static String PrefProbe_title;
     public static String PrefProbe_field_name;
     public static String PrefProbe_field_probe_type;
     public static String PrefProbe_field_latitude;
     public static String PrefProbe_field_longitude;
 
     public static String PrefNeighbour_title;
     public static String PrefNeighbour_title_server;
     public static String PrefNeighbour_title_neighbour;
     public static String PrefNeighbour_field_srv_name;
     public static String PrefNeighbour_field_srv_ci;
     public static String PrefNeighbour_field_srv_lac;
     public static String PrefNeighbour_field_nbr_name;
     public static String PrefNeighbour_field_nbr_ci;
     public static String PrefNeighbour_field_nbr_lac;
     
     public static String PrefTransmission_title;
     public static String PrefTransmission_title_server;
     public static String PrefTransmission_title_neighbour;
     public static String PrefTransmission_field_Site_ID;
     public static String PrefTransmission_field_Site_No;
     public static String PrefTransmission_field_ITEM_Name;
     
     
     public static String GpehWindowTitle;
     public static String GpehTitle;
     public static String GpehDescr;
     public static String GpehLbOSS;
     public static String GpehImportDirEditorTitle;
     public static String GpehOptionsTitle;
     public static String GpehOptionsDescr;
 
     public static String NetworkSiteImportWizard_PAGE_TITLE;
     public static String NetworkSiteImportWizard_PAGE_DESCR;
     public static String NetworkSiteImportWizard_NETWORK;
     public static String NetworkSiteImportWizard_FILE; 
     public static String NetworkSiteImportWizard_DATA_TYPE;
     
     public static String NetworkSiteImportWizardPage_NETWORK_MUST_EXIST;
 
     public static String NetworkSiteImportWizardPage_NO_FILE;
 
     public static String NetworkSiteImportWizardPage_NO_NETWORK;
 
     public static String NetworkSiteImportWizardPage_NO_TYPE;
 
     public static String NetworkSiteImportWizardPage_RESTRICTED_NETWORK_NAME;
 
     public static String NetworkSiteImportWizardPage_WRONG_TYPE_FOR_NETWORK;
 
     public static String OSS;
     
     public static String CommonCRSPreference_button_ADD;
     public static String CommonCRSPreference_button_REMOVE;
     public static String CRSdialog_TITLE;
     public static String CRSdialog_button_SAVE;
     public static String CRSdialog_button_CANSEL;
     public static String CRSdialog_label_Select;
     
     public static String CBC_BCS_0;
     public static String CBC_BCS_2;
     
     public static String CME_Error_2;
     public static String CME_Error_3;
     public static String CME_Error_4;
     public static String CME_Error_20;
     public static String CME_Error_24;
     public static String CME_Error_25;
     public static String CME_Error_26;
     public static String CME_Error_27;
     public static String CME_Error_30;
     public static String CME_Error_32;
     public static String CME_Error_33;
     public static String CME_Error_34;
     public static String CME_Error_35;
     public static String CME_Error_36;
     public static String CME_Error_37;
     public static String CME_Error_100;
     public static String CME_Error_512;
     public static String CME_Error_513;
     public static String CME_Error_514;
     public static String CME_Error_515;
     public static String CME_Error_516;
     public static String CME_Error_517;
     public static String CME_Error_518;    
     
     public static String CNUM_Number_Type_0;
     public static String CNUM_Number_Type_1;
     public static String CNUM_Number_Type_2;
     public static String CNUM_Number_Type_3;
     
     public static String CTCC_Hook_0;
     public static String CTCC_Hook_1;
     public static String CTCC_Simplex_0;
     public static String CTCC_Simplex_1;
     public static String CTCC_AI_service_0;
     public static String CTCC_AI_service_1; 
     public static String CTCC_AI_service_2;
     public static String CTCC_AI_service_5;
     public static String CTCC_End_to_end_encryption_0;
     public static String CTCC_End_to_end_encryption_1;
     public static String CTCC_Comms_Type_0;
     public static String CTCC_Comms_Type_1;
     public static String CTCC_Comms_Type_3;
     public static String CTCC_Slots_Codec_0;
     public static String CTCC_Slots_Codec_1;
     
     public static String CTCR_Disconnect_Cause_0;
     public static String CTCR_Disconnect_Cause_1;
     public static String CTCR_Disconnect_Cause_2;
     public static String CTCR_Disconnect_Cause_3;
     public static String CTCR_Disconnect_Cause_4;
     public static String CTCR_Disconnect_Cause_5;
     public static String CTCR_Disconnect_Cause_6;
     public static String CTCR_Disconnect_Cause_7;
     public static String CTCR_Disconnect_Cause_8;
     public static String CTCR_Disconnect_Cause_9;
     public static String CTCR_Disconnect_Cause_10;
     public static String CTCR_Disconnect_Cause_11;
     public static String CTCR_Disconnect_Cause_12;
     public static String CTCR_Disconnect_Cause_13;
     public static String CTCR_Disconnect_Cause_14;
     public static String CTCR_Disconnect_Cause_15;
     public static String CTCR_Disconnect_Cause_16;
     public static String CTCR_Disconnect_Cause_17;
     public static String CTCR_Disconnect_Cause_18;
     public static String CTCR_Disconnect_Cause_19;
     public static String CTCR_Disconnect_Cause_20;
     
     public static String CTICN_Call_Status_0;
     
     public static String CTICN_Calling_Party_Ident_Type_0;
     public static String CTICN_Calling_Party_Ident_Type_1;
     
     public static String CTSDC_Area_0;
     
     public static String CTSDC_RxTx_0;
     public static String CTSDC_RxTx_1;
     
     public static String CTSDC_Priority_0;
     public static String CTSDC_Priority_n;
     
     public static String CTSDS_access_priority_low;
     public static String CTSDS_access_priority_high;
     public static String CTSDS_access_priority_emer;
     public static String CTSDS_ai_service_12;
     public static String CTSDS_ai_service_13;
     public static String CTSDS_ai_service_28;
     public static String CTSDS_ai_service_29;
     
     public static String CMGS_STATUS_stored_unsent;
     public static String CMGS_STATUS_stored_sent;
     public static String CMGS_STATUS_deleted_unsent;
     public static String CMGS_STATUS_deleted_sent;
     
     public static String CTSG_group_selected;
     public static String CTSG_group_not_scanned;
     public static String CTSG_group_psg_low;
     public static String CTSG_group_psg_normal;
     public static String CTSG_group_psg_high;
     public static String CTSG_group_locked;
     public static String CTSG_group_always_scanned;
     
     public static String CDTXC_tx_rq_allowed;
     public static String CDTXC_tx_rq_not_allowed;
     
     public static String CTXG_tx_granted;
     public static String CTXG_tx_not_granted;
     public static String CTXG_tx_queued;
     public static String CTXG_tx_granted_another;
     
     public static String ATE_echo_off;
     public static String ATE_echo_on;
     
     public static String ATQ_terminal_off;
     public static String ATQ_terminal_on;
     
     public static String ATS0_answer_0;
     public static String ATS0_answer_n;
     
     public static String CREG_registered_status_0;
     public static String CREG_registered_status_1;
     public static String CREG_registered_status_2;
     public static String CREG_registered_status_3;
     
     public static String CSPDCS_not_detected;
     public static String CSPDCS_detected;
     public static String CSPDCS_no_restriction;
     public static String CSPDCS_restriction;
     
     public static String CSPTSS_stack_0;
     public static String CSPTSS_stack_1;
     public static String CSPTSS_stack_2;
     public static String CSPTSS_stack_3;
     public static String CSPTSSA_stack;
     
     public static String CTSP_service_profile_0;
     public static String CTSP_service_profile_1;
     public static String CTSP_service_profile_3;
     public static String CTSP_service_profile_17;
     public static String CTSP_service_profile_19;
     public static String CTSP_service_layer1_0;
     public static String CTSP_service_layer1_2;
     public static String CTSP_service_layer1_3;
     public static String CTSP_service_layer2_0;
     public static String CTSP_service_layer2_1;
     public static String CTSP_service_layer2_20;
     
     public static String CTXD_TxDemandPriority_0;
     public static String CTXD_TxDemandPriority_1;
     public static String CTXD_TxDemandPriority_2;
     public static String CTXD_TxDemandPriority_3;
     
     public static String CSPTD_state_enabled;
     public static String CSPTD_tei_state_disabled;
     public static String CSPTD_itsi_state_disabled;
     
     public static String CTXN_tx_cont_0;
     public static String CTXN_tx_cont_1;
 
     public static String Loader_fake_validator;
 
     public static String RemoteServerUrlPage_0;
 
     public static String RemoteServerUrlPage_1;
     
     public static String PrefUrl_imei;
     public static String PrefUrl_imsi;
     
     private NeoLoaderPluginMessages() {
     }
 
     public static ResourceBundle getResourceBundle() {
         return resourceBundle;
     }
     
     public static String getFormattedString(String key, Object ... args) {
         return MessageFormat.format(key, (Object[])args);
     }
 
     static {
         NLS.initializeMessages(BUNDLE_NAME, NeoLoaderPluginMessages.class);
     }
 
 }
