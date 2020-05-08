 /******************************************************************************* 
  * Copyright (c) 2008 Red Hat, Inc. 
  * Distributed under license by Red Hat, Inc. All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Red Hat, Inc. - initial API and implementation 
  ******************************************************************************/ 
 
 package org.jboss.tools.ws.ui.messages;
 
 import org.eclipse.osgi.util.NLS;
 
 /**
  * @author Grid Qian
  */
 public class JBossWSUIMessages extends NLS {
 
 	private static final String BUNDLE_NAME = "org.jboss.tools.ws.ui.messages.JBossWSUI";		//$NON-NLS-1$
 
 	private JBossWSUIMessages() {
 		// Do not instantiate
 	}
 
 	public static String Bin;
 	public static String Command;
 	public static String Client;
 	public static String DelimitedStringList_Msg_No_Btn;
 	public static String DelimitedStringList_Msg_Text_ParseByAmpersand;
 	public static String DelimitedStringList_Msg_Text_ParseByComma;
 	public static String DelimitedStringList_Msg_Title_ParseByAmpersand;
 	public static String DelimitedStringList_Msg_Title_ParseByComma;
 	public static String DelimitedStringList_Msg_Yes_Btn;
 	public static String DelimitedStringList_NO_COMMAS_WARNING;
 	public static String DelimitedStringList_NO_EQUALS_DELIMITER_WARNING;
 	public static String Lib;
 	public static String Endorsed;
 	
 	public static String JBossRSGenerateWizard_RS_Wizard_Window_Title;
 	public static String JBossRSGenerateWizardPage_Error_RestEasyJarsNotFoundInRuntime;
 	public static String JBossRSGenerateWizardPage_Label_Application_Class_Name;
 	public static String JBossRSGenerateWizardPage_Page_title;
	public static String JBossRSGenerateWizardPage_ServiceName_Tooltip;
 	public static String JBossRSGenerateWizardPage_UpdateWebXMLCheckbox;
 	public static String JBossRSGenerateWizardValidator_ERROR_Can_Only_Add_Sample_Once;
 	public static String JBossRuntimeListFieldEditor_ActionAdd;
 	public static String JBossRuntimeListFieldEditor_ActionEdit;
 	public static String JBossRuntimeListFieldEditor_ActionRemove;
 	public static String JBossRuntimeListFieldEditor_ErrorMessageAtLeastOneJar;
 	public static String JBossWS_Runtime_List_Field_Editor_Name;
 	public static String JBossWS_Runtime_List_Field_Editor_Version;
 	public static String JBossWS_Runtime_List_Field_Editor_Path;
 	public static String JBossWS_Runtime_Delete_Confirm_Title;
 	public static String JBossWS_Runtime_Delete_Used_Confirm;
 	public static String JBossWS_Runtime_Delete_Not_Used_Confirm;
 	public static String JBossWS_Runtime_List_Field_Editor_Edit_Runtime;
 	public static String JBossWS_Runtime_List_Field_Editor_Modify_Runtime;
 	public static String JBossWS_Runtime_List_Field_Editor_New_Runtime;
 	public static String JBossWS_Runtime_List_Field_Editor_Runtime_Already_Exists;
 	public static String JBossWS_Runtime_List_Field_Editor_Runtime;
 	public static String JBossWS_Runtime_List_Field_Editor_Create_A_Runtime;
 	public static String JBossWS_Runtime_List_Field_Editor_Home_Folder;
 	public static String JBossWS_SWT_Field_Editor_Factory_Select_Home_Folder;
 	public static String JBossWS_Runtime_List_Field_Editor_Name2;
 	public static String JBossWS_Runtime_Check_Field_Default_Classpath;
 	public static String JBossWS_Preference_Page_Runtimes;
 	public static String JBossWSLibraryListFieldEditor_ActionAdd;
 	public static String JBossWSLibraryListFieldEditor_ActionRemove;
 	public static String JBossWSLibraryListFieldEditor_LIBRARY_JARS;
 	public static String JBossWS_GenerateWizard_Title;
 	public static String JBossWS_GenerateWizard_GenerateWizardPage_Title;
 	public static String JBossWS_GenerateWizard_GenerateWizardPage_Description;
 	public static String JBossWS_GenerateWizard_GenerateWizardPage_ServiceName_Label;
 	public static String JBossWS_GenerateWizard_GenerateWizardPage_Package_Label;
 	public static String JBossWS_GenerateWizard_GenerateWizardPage_ClassName_Label;
 	public static String JBossWS_GenerateWizard_MessageDialog_Title;
 	public static String JBossWS_UI_PLUGIN_NO_MESSAGES;
 	public static String JBossWS_GenerateWizard_GenerateWizardPage_Project_Group;
 	public static String JBossWS_GenerateWizard_GenerateWizardPage_Project_Group_Tooltip;
 	public static String JBossWS_GenerateWizard_GenerateWizardPage_Web_Service_Group;
 	public static String JBossWS_GenerateWizard_GenerateWizardPage_Class_Group;
 	
 	public static String JBossWSFacetInstallPage_ServerSuppliedJBossWS;
 	public static String JBossWSFacetInstallPage_New;
 	public static String JBossWSFacetInstallPage_Deploy;
 	public static String JBossWSRuntimeInstallPage_NoTargetRuntime;
 	public static String JBossWSRuntimeInstallPage_NoValidJBossWSRuntime;
 	public static String JBossWSFacetInstallPage_Title;
 	public static String JBossWSFacetInstallPage_Description;
 	
 	public static String Error_JBossWS_GenerateWizard_NoProjectSelected;
 	public static String Error_JBossWS_Label_Runtime_Load;
 	public static String Error_JBossWS_Basic_Editor_Composite;
 	public static String Error_JBossWS_Basic_Editor_Support;
 	public static String Error_JBossWS_Runtime_List_Field_Editor_Path_To_Home_Diretory_Cannot_Be_Empty;
 	public static String Error_JBossWS_Runtime_List_Field_Editor_Runtime_Name_Is_Not_Correct;
 	public static String Error_JBossWS_Runtime_List_Field_Editor_Name_Cannot_Be_Empty;
 	public static String Error_JBossWS_Runtime_List_Field_Editor_Inputelement_Must_Be_An_Instance_Of_List;
 	public static String Error_JBossWS_GenerateWizard_NotDynamicWebProject;
 	public static String Error_JBossWS_GenerateWizard_ClassName_Same;
 	public static String Error_JBossWS_GenerateWizard_PackageName_Cannot_Be_Empty;
 	public static String Error_JBossWS_GenerateWizard_ClassName_Cannot_Be_Empty;
 	public static String Error_JBossWS_GenerateWizard_ServiceName_Empty;
 	public static String Error_JBossWS_GenerateWizard_IsOutputFolder;
 	public static String Error_JBossWS_GenerateWizard_PackageExists;
 	public static String Error_JBossWS_GenerateWizard_PackageNotShown;
 	public static String Error_JBossWS_GenerateWizard_PackageExistsDifferentCase;
 	public static String Error_JBossWS_GenerateWizard_NoSrcInProject;
 
 	public static String Error_WS_No_Runtime_Specifed;
 	public static String Error_WS_Chose_runtime;
 	public static String Error_JBossWSRuntimeConfigBlock_Duplicated_Jar;
 	
 	public static String JBossWS_DelimitedStringList_EditValue_Dialog_Title;
 	public static String JBossWS_DelimitedStringList_EditValue_Dialog_Message;
 	public static String JBossWSAnnotatedClassWizard_Annotated_Class_WS_Wizard_Title;
 	public static String JBossWSAnnotatedClassWizardPage_Application_Class_Browse_btn;
 	public static String JBossWSAnnotatedClassWizardPage_Application_Class_field;
 	public static String JBossWSAnnotatedClassWizardPage_JAXRS_Button;
 	public static String JBossWSAnnotatedClassWizardPage_JAXWS_Button;
 	public static String JBossWSAnnotatedClassWizardPage_package_browse_btn;
 	public static String JBossWSAnnotatedClassWizardPage_package_name_field;
 	public static String JBossWSAnnotatedClassWizardPage_PageDescription;
 	public static String JBossWSAnnotatedClassWizardPage_PageTitle;
 	public static String JBossWSAnnotatedClassWizardPage_Project_Group;
 	public static String JBossWSAnnotatedClassWizardPage_Projects_Combo_Tooltip;
 	public static String JBossWSAnnotatedClassWizardPage_Service_class_Browse_btn;
 	public static String JBossWSAnnotatedClassWizardPage_Service_class_field;
 	public static String JBossWSAnnotatedClassWizardPage_Service_implementation_group;
 	public static String JBossWSAnnotatedClassWizardPage_Service_Name_field;
 	public static String JBossWSAnnotatedClassWizardPage_Update_Web_xml_checkbox;
 	public static String JBossWSAnnotatedClassWizardPage_Web_Service_Group;
 	public static String JBossWSAnnotatedClassWizardPage_WS_Tech_Group;
 	public static String JAXRSWSTestView_Action_URL_Label;
 	public static String JAXRSWSTestView_Button_Get_From_WSDL;
 	public static String JAXRSWSTestView_Configure_Monitor_Button;
 	public static String JAXRSWSTestView_CopyResultMenu_Text;
 	public static String JAXRSWSTestView_CopyResultsMenu;
 	public static String JAXRSWSTestView_Error_Msg_Starting_Monitor;
 	public static String JAXRSWSTestView_Error_Title_Starting_Monitor;
 	public static String JAXRSWSTestView_Exception_Status;
 	public static String JAXRSWSTestView_HTTP_Method_Label;
 	public static String JAXRSWSTestView_Invoke_Label;
 	public static String JAXRSWSTestView_Invoking_WS_Status;
 	public static String JAXRSWSTestView_JAXRS_Success_Status;
 	public static String JAXRSWSTestView_JAXWS_Success_Status;
 	public static String JAXRSWSTestView_Message_Service_Invocation_Cancelled;
 	public static String JAXRSWSTestView_Message_Unsuccessful_Test;
 	public static String JAXRSWSTestView_Open_Monitor_Button;
 	public static String JAXRSWSTestView_Open_Response_Tag_Contents_in_XML_Editor;
 	public static String JAXRSWSTestView_Open_Result_in_XML_Editor;
 	public static String JAXRSWSTestView_Request_Body_Label;
 	public static String JAXRSWSTestView_Request_Header_Label;
 	public static String JAXRSWSTestView_Request_Parameters_Label;
 	public static String JAXRSWSTestView_Results_Body_Label;
 	public static String JAXRSWSTestView_Results_Header_Label;
 	public static String JAXRSWSTestView_Service_URL_Label;
 	public static String JAXRSWSTestView_Set_Sample_Data_Label;
 	public static String JAXRSWSTestView_Web_Service_Type_Label;
 	public static String JAXRSWSTestView2_BodyText_Section;
 	public static String JAXRSWSTestView2_Checkbox_Basic_Authentication;
 	public static String JAXRSWSTestView2_GetFromWSDL_Tooltip;
 	public static String JAXRSWSTestView2_Go_Tooltip;
 	public static String JAXRSWSTestView2_Headers_Section;
 	public static String JAXRSWSTestView2_Msg_No_Results_Check_Headers;
 	public static String JAXRSWSTestView2_OpenInEditor_Action;
 	public static String JAXRSWSTestView2_Parameters_Section;
 	public static String JAXRSWSTestView2_RequestDetails_Section;
 	public static String JAXRSWSTestView2_ResponseBody_Section;
 	public static String JAXRSWSTestView2_ResponseDetails_Section;
 	public static String JAXRSWSTestView2_ResponseHeaders_Section;
 	public static String JAXRSWSTestView2_SaveResponseText_Error;
 	public static String JAXRSWSTestView2_SaveResponseText_tooltip;
 	public static String JAXRSWSTestView2_ShowInBrowser_Tooltip;
 	public static String JAXRSWSTestView2_ShowRaw_Tooltip;
 	public static String JAXRSWSTestView2_Text_Msg_May_Be_Out_of_Date;
 	public static String JAXRSWSTestView2_Title_Msg_May_Be_Out_of_Date;
 	public static String ResultsXMLStorageInput_WS_Invocation_Results_Prefix;
 	public static String TesterWSDLUtils_WSDL_HTTPS_Secured_Inaccessible;
 	public static String TesterWSDLUtils_WSDL_Inaccessible;
 
 	public static String UidPwdDialog_Description;
 	public static String UidPwdDialog_PWD_Label;
 	public static String UidPwdDialog_Title;
 	public static String UidPwdDialog_UID_Label;
 	public static String WSDLBrowseDialog_Dialog_Title;
 	public static String WSDLBrowseDialog_Error_Msg_Invalid_URL;
 	public static String WSDLBrowseDialog_Error_Msg_Parse_Error;
 	public static String WSDLBrowseDialog_Error_Retrieving_WSDL;
 	public static String WSDLBrowseDialog_FS_Browse;
 	public static String WSDLBrowseDialog_Group_Title;
 	public static String WSDLBrowseDialog_Message;
 	public static String WSDLBrowseDialog_No_Services_Available;
 	public static String WSDLBrowseDialog_No_Services_Available_Warning;
 	public static String WSDLBrowseDialog_Operation_Field;
 	public static String WSDLBrowseDialog_Port_Field;
 	public static String WSDLBrowseDialog_Service_Field;
 	public static String WSDLBrowseDialog_Status_Invalid_URL;
 	public static String WSDLBrowseDialog_Status_ParsingWSDLFromURL;
 	public static String WSDLBrowseDialog_Status_WSDL_Unavailable;
 	public static String WSDLBrowseDialog_Title;
 	public static String WSDLBrowseDialog_URL_Browse;
 	public static String WSDLBrowseDialog_WS_Browse;
 	public static String WSDLBrowseDialog_WS_Browse_Msg;
 	public static String WSDLBrowseDialog_WS_Browse_Select_WSDL_Msg;
 	public static String WSDLBrowseDialog_WS_Browse_Select_WSDL_Title;
 	public static String WSDLBrowseDialog_WSDL_URI_Field;
 	public static String WSDLBrowseDialog_WSDL_URL_Dialog_Title;
 	public static String WSDLBrowseDialog_WSDL_URL_Prompt;
 
 	public static String WSTestUtils_SaveResponseText_Error_Msg;
 	public static String WSTestUtils_SaveResponseText_Title;
 
 	static {
 		NLS.initializeMessages(BUNDLE_NAME, JBossWSUIMessages.class);
 	}
 }
