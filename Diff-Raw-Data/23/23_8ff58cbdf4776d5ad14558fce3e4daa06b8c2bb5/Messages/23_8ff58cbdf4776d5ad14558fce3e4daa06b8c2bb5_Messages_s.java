 /*******************************************************************************
  * Copyright (c) 2005 Oracle Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Gerry Kessler - initial API and implementation
  *    Ian Trimble - changed to work correctly with org.eclipse.osgi.util.NLS
  *******************************************************************************/ 
 package org.eclipse.jst.jsf.ui.internal;
 
 import org.eclipse.osgi.util.NLS;
 
 /**
  * String resource handler.
  * 
  * @author Gerry Kessler - Oracle, Ian Trimble - Oracle
  */
 public class Messages extends NLS {
 	private static final String BUNDLE_NAME = "org.eclipse.jst.jsf.ui.internal.messages"; //$NON-NLS-1$
 
 	/**
 	 * see messages.properties
 	 */
 	public static String JSFFacetInstallPage_JSFLibraryLabel0;
 
     /**
      * see messages.properties
      */
 	public static String JSFFacetInstallPage_title;
     /**
      * see messages.properties
      */
 	public static String JSFFacetInstallPage_description;
     /**
      * see messages.properties
      */
 	public static String JSFFacetInstallPage_JSFImplLabel;
     /**
      * see messages.properties
      */
 	public static String JSFFacetInstallPage_Add1;
     /**
      * see messages.properties
      */
 	public static String JSFFacetInstallPage_Add2;
     /**
      * see messages.properties
      */
 	public static String JSFFacetInstallPage_DeployJarsLabel;
     /**
      * see messages.properties
      */
 	public static String JSFFacetInstallPage_JSFConfigLabel;
     /**
      * see messages.properties
      */
 	public static String JSFFacetInstallPage_JSFServletNameLabel;
     /**
      * see messages.properties
      */
 	public static String JSFFacetInstallPage_JSFServletClassNameLabel;
     /**
      * see messages.properties
      */
 	public static String JSFFacetInstallPage_JSFURLMappingLabel;
     /**
      * see messages.properties
      */
 	public static String JSFFacetInstallPage_PatternDialogTitle;
     /**
      * see messages.properties
      */
 	public static String JSFFacetInstallPage_PatternDialogDesc;
     /**
      * see messages.properties
      */
 	public static String JSFFacetInstallPage_Remove;
     /**
      * see messages.properties
      */
 	public static String JSFFacetInstallPage_PatternEmptyMsg;
     /**
      * see messages.properties
      */
 	public static String JSFFacetInstallPage_PatternSpecifiedMsg;
     /**
      * see messages.properties
      */
 	public static String JSFFacetInstallPage_ErrorNoWebAppDataModel;
     /**
      * see messages.properties
      */
 	public static String JSFLibrariesPreferencePage_DefinedJSFLibraries;
     /**
      * see messages.properties
      */
 	public static String JSFLibrariesPreferencePage_New;
     /**
      * see messages.properties
      */
 	public static String JSFLibrariesPreferencePage_Edit;
     /**
      * see messages.properties
      */
 	public static String JSFLibrariesPreferencePage_Remove;
     /**
      * see messages.properties
      */
 	public static String JSFLibrariesPreferencePage_CannotRemovePluginProvidedTitle;
     /**
      * see messages.properties
      */
 	public static String JSFLibrariesPreferencePage_CannotRemovePluginProvidedMessage;
     /**
      * see messages.properties
      */
 	public static String JSFLibrariesPreferencePage_MakeDefault;
     /**
      * see messages.properties
      */
 	public static String JSFLibrariesPreferencePage_Description;
     /**
      * see messages.properties
      */
 	public static String JSFLibrariesPreferencePage_CannotModifyPluginProvidedTitle;
     /**
      * see messages.properties
      */
 	public static String JSFLibrariesPreferencePage_CannotModifyPluginProvidedMessage;
     /**
      * see messages.properties
      */
 	public static String JSFLibraryConfigControl_Add;
     /**
      * see messages.properties
      */
 	public static String JSFLibraryConfigControl_AddAll;
     /**
      * see messages.properties
      */
 	public static String JSFLibraryConfigControl_ComponentLibrary;
     /**
      * see messages.properties
      */
 	public static String JSFLibraryConfigControl_DeployJAR;
     /**
      * see messages.properties
      */
 	public static String JSFLibraryConfigControl_ImplementationLibrary;
     /**
      * see messages.properties
      */
 	public static String JSFLibraryConfigControl_NewComponentLibrary;
     /**
      * see messages.properties
      */
 	public static String JSFLibraryConfigControl_NewImplementationLibrary;
     /**
      * see messages.properties
      */
 	public static String JSFLibraryConfigControl_NullProject;
     /**
      * see messages.properties
      */
 	public static String JSFLibraryConfigControl_Remove;
     /**
      * see messages.properties
      */
 	public static String JSFLibraryConfigControl_RemoveAll;
     /**
      * see messages.properties
      */
 	public static String JSFLibraryConfigControl_TH_Deploy;
     /**
      * see messages.properties
      */
 	public static String JSFLibraryConfigControl_TH_LibraryName;
 
 
     /**
      * see messages.properties
      */
 	public static String JSFLibraryContainerWizardPage_PageName;
     /**
      * see messages.properties
      */
 	public static String JSFLibraryContainerWizardPage_Title;
     /**
      * see messages.properties
      */
 	public static String JSFLibraryContainerWizardPage_Description;
     /**
      * see messages.properties
      */
 	public static String JSFLibraryContainerWizardPage_WarningNoJSFFacet;
     /**
      * see messages.properties
      */
 	public static String JSFLibraryContainerWizardPage_JSFLibraries;
     /**
      * see messages.properties
      */
 	public static String JSFLibraryContainerWizardPage_Add;
     /**
      * see messages.properties
      */
 	public static String JSFLibraryContainerWizardPage_Edit;
     /**
      * see messages.properties
      */
 	public static String JSFLibraryContainerWizardPage_ErrorInitializing;
     /**
      * see messages.properties
      */
 	public static String JSFLibraryContainerWizardPage_ImplAlreadyPresent;
     /**
      * see messages.properties
      */
 	public static String JSFLibraryContainerWizardPage_SelectOneImpl;
     /**
      * see messages.properties
      */    
 	public static String JSFLibraryContainerWizardPage_V1Registry_Warning_DialogTitle;
     /**
      * see messages.properties
      */
 	public static String JSFLibraryContainerWizardPage_V1Registry_Warning_DialogText;
     /**
      * see messages.properties
      */
     public static String JSFLibraryContainerWizardPage_EditLibrary_DescriptionText;
     /**
      * see messages.properties
      */
 	public static String JSFLibraryPropertyPage_No_JSF_Facet_Installed;
     /**
      * see messages.properties
      */
 	public static String JSFLibraryPropertyPage_No_JSF_Implementation_Lib_Selected;
     /**
      * see messages.properties
      */
 	public static String JSFLibraryWizard_DESCRIPTION;
     /**
      * see messages.properties
      */
 	public static String JSFLibraryWizard_IMPLS_ONLY_DESC;
     /**
      * see messages.properties
      */
 	public static String JSFLibraryWizard_CreateImplementation;
     /**
      * see messages.properties
      */
 	public static String JSFLibraryWizard_CreateJSFLibrary;
     /**
      * see messages.properties
      */
 	public static String JSFLibraryWizard_EditJSFLibrary;
     /**
      * see messages.properties
      */
 	public static String JSFLibraryWizard_JSFLibrary;
     /**
      * see messages.properties
      */
     public static String JSFLibraryWizard_JSFLibraryWizard_DontShowThisAgain_CheckBoxLabel;
     /**
      * see messages.properties
      */
 	public static String JSFLibraryWizard_LibraryName;
     /**
      * see messages.properties
      */
     public static String JSFLibraryWizard_V1JSFLibrary_DialogMessage;
     /**
      * see messages.properties
      */
     public static String JSFLibraryWizard_V1JSFLibrary_DialogTitle;
     /**
      * see messages.properties
      */
 	public static String JSFLibraryWizard_VersionSupported;
     /**
      * see messages.properties
      */
 	public static String JSFLibraryWizard_LibraryJars;
     /**
      * see messages.properties
      */
 	public static String JSFLibraryWizard_IsJSFImplementation;
     /**
      * see messages.properties
      */
 	public static String JSFLibraryWizard_DeployJars;
     /**
      * see messages.properties
      */
 	public static String JSFLibraryWizard_Add;
     /**
      * see messages.properties
      */
 	public static String JSFLibraryWizard_Remove;
     /**
      * see messages.properties
      */
 	public static String JSFLibraryWizard_ExtJarFileDialogTitle;
     /**
      * see messages.properties
      */
 	public static String JSFLibraryWizard_ValidateNoJars;
     /**
      * see messages.properties
      */
 	public static String JSFLibraryWizard_ValidateNoLibraryName;
     /**
      * see messages.properties
      */
 	public static String JSFLibraryWizard_ValidateExistingLibraryName;
 
     /**
      * Title set on the el validation preference panel
      */
     public static String JSFValidationPreferencePage_ELPrefPanel_Title;
     /**
      * Title set on the checkbox that enables/disables build validation for EL
      */
     public static String JSFValidationPreferencePage_ELPrefPanel_BuildValidationCheckBoxTitle;
     /**
      * Title set on the checkbox that enables/disables incremental (as you type)
      * validation for EL
      */
     public static String JSFValidationPreferencePage_ELPrefPanel_IncrementalValidationCheckBoxTitle;
 
     static {
 		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
 	}
 }
