 /*******************************************************************************
  * Copyright (c) 2004, 2010 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.client.ui.constants;
 
 /**
  * This class contains constants for the context sensitive help. Constants are
  * built as follows: The name of the help-plugin + dot + the context-id of the
  * context.xml.
  * 
  * @author BREDEX GmbH
  * @created 13.07.2004
  */
 public interface ContextHelpIds {
     
     /** ID of the help system */
     public static final String HELP = "org.eclipse.ui.help"; //$NON-NLS-1$
     
     /** ID of the HelpPlugin */
     public static final String HELP_PLUGIN_ID = "org.eclipse.jubula.client.ua.help"; //$NON-NLS-1$
 
     /** Praefix = Plugin_id + . */
     public static final String PRAEFIX = HELP_PLUGIN_ID + "."; //$NON-NLS-1$
     
     /** ContextHelpId for adding existing test cases */
     public static final String TESTCASE_ADD_EXISTING = PRAEFIX
         + "testCaseAddExistingContextId"; //$NON-NLS-1$
 
     /** ContextHelpId for open existing test cases */
     public static final String OPEN_EXISTING_TESTCASE = PRAEFIX
         + "testCaseOpenExistingContextId"; //$NON-NLS-1$
     
     /** ContextHelpId for moving to new project */
     public static final String TESTCASE_MOVE_EXTERNAL = PRAEFIX
         + "testCaseMoveExternalContextId"; //$NON-NLS-1$
     
     /** ContextHelpId for TestExecView */
     public static final String TEST_SUITE_VIEW = PRAEFIX
         + "testExecViewContextId"; //$NON-NLS-1$
 
     /** ContextHelpId for TestSpecView */
     public static final String TEST_SPEC_VIEW = PRAEFIX
         + "testSpecificationViewContextId"; //$NON-NLS-1$
 
     /** ContextHelpId for ComponentNamesBrowser */
     public static final String COMPONENT_NAMES_BROWSER = PRAEFIX
         + "guidancerComponentNameBrowserContextId"; //$NON-NLS-1$
     
     /** ContextHelpId for guidancerPropertiesView */
     public static final String JB_PROPERTIES_VIEW = PRAEFIX
         + "guidancerPropertiesViewContextId"; //$NON-NLS-1$
 
     /** ContextHelpId for guidancerDataSetView */
     public static final String JB_DATASET_VIEW = PRAEFIX
         + "guidancerDataSetViewContextId"; //$NON-NLS-1$
     
     /** ContextHelpId for SpecTestCaseEditor */
     public static final String JB_SPEC_TESTCASE_EDITOR = PRAEFIX
         + "guidancerSpecTestCaseEditorContextId"; //$NON-NLS-1$
     
     /** ContextHelpId for Test Job Editor */
     public static final String TEST_JOB_EDITOR = PRAEFIX
         + "guidancerTestJobEditorContextId"; //$NON-NLS-1$
     
     /** ContextHelpId for central test data editor */
     public static final String CENTRAL_TESTDATA_EDITOR = PRAEFIX
         + "guidancerCentralTestDataEditorContextId"; //$NON-NLS-1$
     
     /** ContextHelpId for ModelEditor */
     public static final String GUIDANCER_MODEL_EDITOR = PRAEFIX
         + "guidancerModelEditorContextId"; //$NON-NLS-1$
 
     /** ContextHelpId for server/port dialog */
     public static final String SERVER_PORT_ID = PRAEFIX 
         + "serverPortContextId"; //$NON-NLS-1$
 
     /** ContextHelpId for import dialog */
     public static final String IMPORT_PROJECT_DIALOG = PRAEFIX
         + "importProjectDialogContextId"; //$NON-NLS-1$
     
     /** ContextHelpId for JRE selection dialog */
     public static final String JRE_CHOOSE_DIALOG = PRAEFIX
         + "jreChooseContextId"; //$NON-NLS-1$
 
     /** test suite editor */
     public static final String TEST_SUITE_EDITOR = PRAEFIX
         + "testSuiteEditorContextId"; //$NON-NLS-1$
     
     /** create new test suite */
     public static final String TEST_SUITE_CREATE = PRAEFIX
         + "testSuiteCreateContextId"; //$NON-NLS-1$
     
     /** start a test suite */
     public static final String TEST_SUITE_START = PRAEFIX
         + "testSuiteStartContextId"; //$NON-NLS-1$
 
     /** help context id for new cap dialog */
     public static final String CAP = PRAEFIX 
         + "capContextId"; //$NON-NLS-1$
     
     /** help context id for componentNameView */
     public static final String COMP_NAME = PRAEFIX 
         + "compNameViewContextId"; //$NON-NLS-1$
 
     /** help context id for the project wizard pages: 1st page */
     public static final String PROJECT_WIZARD = PRAEFIX
         + "projectWizardContextId"; //$NON-NLS-1$
 
     /** help context id for the test result view  */
     public static final String RESULT_TREE_VIEW = PRAEFIX
         + "testResultViewContextId"; //$NON-NLS-1$
     
     /** help context id for the db_login dialog */
     public static final String DB_LOGIN_ID = PRAEFIX
         + "dbLoginContextId"; //$NON-NLS-1$
 
     /** help context id for the database migration assistant */
     public static final String DATABASE_MIGRATION_ASSISTANT = PRAEFIX
         + "databaseMigrationAssistantContextId"; //$NON-NLS-1$
     
     /** help context id for the problem view */
     public static final String PROBLEM_VIEW = PRAEFIX 
         + "problemViewContextId"; //$NON-NLS-1$
 
     /** help context id for the object mapping editor */
     public static final String OBJECT_MAP_EDITOR = PRAEFIX
         + "objectMapEditorContextId"; //$NON-NLS-1$
 
     /** help context id for the guidancer preference page */
     public static final String PREFPAGE_BASIC = PRAEFIX
         + "prefPageBasicContextId"; //$NON-NLS-1$
 
     /** help context id for the test data preference page */
     public static final String PREFPAGE_TESTDATA = PRAEFIX
         + "prefPageTestDataContextId"; //$NON-NLS-1$
 
     /** help context id for the object mapping preference page */
     public static final String PREFPAGE_OBJECT_MAP = PRAEFIX
         + "prefPageObjectMapContextId"; //$NON-NLS-1$
 
     /** help context id for the server preference page */
     public static final String PREFPAGE_SERVER = PRAEFIX
         + "prefPageServerContextId"; //$NON-NLS-1$
     
     /** help context id preference page */
     public static final String PREFPAGE_EMBEDDED_CHRONON = PRAEFIX
         + "prefPageEmbeddedChrononContextId"; //$NON-NLS-1$
     
     /** help context id for the server preference page */
     public static final String PREFPAGE_EMBEDDED_AGENT = PRAEFIX
         + "prefPageEmbeddedAgentContextId"; //$NON-NLS-1$
 
     /** help context id for the key combination preference page */
     public static final String PREFPAGE_KEYCOMB = PRAEFIX
         + "prefPageKeyCombContextId"; //$NON-NLS-1$
     
     /** help context id for the observation preference page */
     public static final String PREFPAGE_OBSERV = PRAEFIX
         + "prefPageObserveJavaContextId"; //$NON-NLS-1$
     
     /** help context id for the test case diagram */
     public static final String PREFPAGE_TCD_APPEARANCE = PRAEFIX
         + "prefPageTCDAppearanceContextId"; //$NON-NLS-1$
     
     /** help context id for the test case diagram */
     public static final String PREFPAGE_TCD_CONNECTIONS = PRAEFIX
         + "prefPageTCDConnectionsContextId"; //$NON-NLS-1$
     
     /** help context id for the test case diagram */
     public static final String PREFPAGE_TCD_GENERAL = PRAEFIX
         + "prefPageTCDGeneralContextId"; //$NON-NLS-1$
     
     /** help context id for the test case diagram */
     public static final String PREFPAGE_TCD_PRINTING = PRAEFIX
         + "prefPageTCDPrintingContextId"; //$NON-NLS-1$
     
     /** help context id for the test case diagram */
     public static final String PREFPAGE_TCD_RULERS = PRAEFIX
         + "prefPageTCDRulersContextId"; //$NON-NLS-1$
     
     /** help context id for the open project dialog after double click in probview */
     public static final String PROJECT_DB_NAME = PRAEFIX
         + "projectFromDBContextId"; //$NON-NLS-1$
 
     /** help context id for the test result preference pages */
     public static final String PREFPAGE_TESTRESULT = PRAEFIX
         + "prefPageTestResultContextId"; //$NON-NLS-1$
     
     /** help context id for the delete project dialog */
     public static final String DELETE_PROJECT = PRAEFIX
         + "deleteProjectDialogContextId"; //$NON-NLS-1$
     
     /** help context id for the open project dialog */
     public static final String OPEN_PROJECT = PRAEFIX
         + "openProjectDialogContextId"; //$NON-NLS-1$
     
     /** help context id for the search result view */
     public static final String JB_SEARCH_RESULT_VIEW = PRAEFIX
         + "searchResultViewContextId"; //$NON-NLS-1$
 
     /** ContextHelpId for Composite Extensions project properties page */
     public static final String COMPOSITE_EXTENSIONS_PROPERTY_PAGE = PRAEFIX
         + "compositeExtensionsPageContextId"; //$NON-NLS-1$
 
     /** ContextHelpId for AUT settings page: aut list in settings*/
     public static final String AUT_PROPERTY_PAGE = PRAEFIX
         + "autSettingsPageContextId"; //$NON-NLS-1$
     
     /** ContextHelpId for project settings page*/
     public static final String PROJECT_PROPERTY_PAGE = PRAEFIX
         + "projectSettingsPageContextId"; //$NON-NLS-1$
     
     /** ContextHelpId for aut configuration setting dialog */
     public static final String AUT_CONFIG_PROP_DIALOG = PRAEFIX
         + "autConfigPropDialogContextId"; //$NON-NLS-1$
     
     /** ContextHelpId for AUT settings define aut dialog*/
     public static final String AUT_CONFIGURATION = PRAEFIX
         + "guidancerAutConfigurationContextId"; //$NON-NLS-1$
 
     
     /** help context id for the aut setting wizard page (define aut) */
     public static final String AUT_SETTING_WIZARD_PAGE = PRAEFIX
         + "autSettingWizardPagePageContextId"; //$NON-NLS-1$
     
     /** help context id for the aut configuration settings wizard page */
     public static final String AUT_CONFIG_SETTING_WIZARD_PAGE = PRAEFIX
         + "autConfigSettingWizardPagePageContextId"; //$NON-NLS-1$
 
     /** help context id for a wizard page */
     public static final String 
     REFACTOR_REPLACE_ADDITIONAL_INFORMATION_WIZARD_PAGE = PRAEFIX
             + "refactorReplaceAdditionalInformationWizardPagePageContextId"; //$NON-NLS-1$
 
     /** help context id for a wizard page */
     public static final String 
     REFACTOR_REPLACE_CHOOSE_TEST_CASE_WIZARD_PAGE = PRAEFIX
             + "refactorReplaceChooseTestCaseWizardPagePageContextId"; //$NON-NLS-1$
 
     /** help context id for a wizard page */
     public static final String 
     REFACTOR_REPLACE_MATCH_PARAMETER_WIZARD_PAGE = PRAEFIX
             + "refactorReplaceMatchParameterWizardPagePageContextId"; //$NON-NLS-1$
 
     /** help context id for a wizard page */
     public static final String 
     REFACTOR_REPLACE_MATCH_COMP_NAMES_WIZARD_PAGE = PRAEFIX
             + "refactorReplaceMatchComponentNamesWizardPagePageContextId"; //$NON-NLS-1$
 
    /** help context id for a wizard */
     public static final String
    SEARCH_REFACTOR_REPLACE_EXECUTION_TEST_CASE_WIZARD = PRAEFIX
            + "searchRefactorReplaceExecutionTestCaseWizardContextId"; //$NON-NLS-1$
 
     /** help context id for a wizard page */
     public static final String
     SEARCH_REFACTOR_CHANGE_CTDS_COLUMN_USAGE_WIZARD_PAGE = PRAEFIX
             + "searchRefactorChangeCtdsColumnUsageWizardPagePageContextId"; //$NON-NLS-1$
 
     /** help context id for the nag dialog to import all required projects */
     public static final String IMPORT_ALL_REQUIRED_PROJECTS = PRAEFIX
         + "openProjectImportAllRequiredProjectsContextId"; //$NON-NLS-1$
     
     /** help context id for the aut wizard page for automatically technical component names name assignment*/
     public static final String AUT_WIZARD_PAGE_GENERATE_NAMES = PRAEFIX
         + "autWizardPageGenerateNamesPageContextId"; //$NON-NLS-1$
     
     /** help context id for the appearance pref page */
     public static final String APPEARANCE_PREF_PAGE = PRAEFIX
         + "appearancePrefPageContextId"; //$NON-NLS-1$
 
     /** help context id for the editor pref page */
     public static final String EDITOR_PREF_PAGE = PRAEFIX
         + "editorPrefPageContextId"; //$NON-NLS-1$
     
     /** help context id for the add event handler dialog */
     public static final String EVENT_HANDLER_ADD = PRAEFIX
         + "eventHandlerAddContextId"; //$NON-NLS-1$
 
     /** help context id for the edit parameters dialog */
     public static final String EDIT_PARAMETERS = PRAEFIX
         + "editParametersDialogContextId"; //$NON-NLS-1$
 
     /** help context id for aut classpath */
     public static final String CLASSPATH_AUT = PRAEFIX
         + "classpathForAUTContextId"; //$NON-NLS-1$
         
     /** help context id for the find dialog */
     public static final String FIND_DIALOG = PRAEFIX
         + "findDialogContextId"; //$NON-NLS-1$
 
     /** for double-click on problem view entry for no project open */
     public static final String PROBLEM_VIEW_NO_PROJECT = PRAEFIX
         + "problemViewNoProjectContextId"; //$NON-NLS-1$
 
     /** for new test case dialog */
     public static final String DIALOG_NEW_TESTCASE = PRAEFIX
         + "testcaseNewContextId"; //$NON-NLS-1$
 
     /** dialog for renaming objects */
     public static final String DIALOG_RENAME = PRAEFIX
         + "dialogRenameContextId"; //$NON-NLS-1$
 
     /** dialog for creating a new category */
     public static final String DIALOG_NEW_CATEGORY = PRAEFIX
         + "dialogNewCategoryContextId"; //$NON-NLS-1$
 
     /** for extract test case action dialog */
     public static final String DIALOG_TESTCASE_EXTRACT = PRAEFIX
         + "dialogTestcaseExtractContextId"; //$NON-NLS-1$
 
     /** for new test suite dialog */
     public static final String DIALOG_TS_NEW = PRAEFIX
         + "dialogTestsuiteNewContextId"; //$NON-NLS-1$
 
     /** for new test suite dialog */
     public static final String DIALOG_TJ_NEW = PRAEFIX
         + "dialogTestJobNewContextId"; //$NON-NLS-1$
     
     /** for new OM Category dialog */
     public static final String DIALOG_OM_CAT_NEW = PRAEFIX
         + "dialogOMCategoryNewContextId"; //$NON-NLS-1$
 
     /** for OM Category selection dialog */
     public static final String DIALOG_OM_CAT = PRAEFIX
         + "dialogOMCategoryContextId"; //$NON-NLS-1$
 
     /** for "Save Project As..." Dialog */
     public static final String DIALOG_PROJECT_SAVEAS = PRAEFIX
         + "dialogProjectSaveAsContextId"; //$NON-NLS-1$
     
     /** for StartObservationModeAction dialog */
     public static final String DIALOG_OBS_TC_SAVE = PRAEFIX
         + "dialogObsTestcaseSaveContextId"; //$NON-NLS-1$
 
     /** help context id for create new project version dialog */
     public static final String DIALOG_PROJECT_CREATENEWVERSION = PRAEFIX
         + "dialogProjectCreateNewVersionContextId"; //$NON-NLS-1$
 
     /** for project rename dialog */
     public static final String DIALOG_PROJECT_IMPORT_RENAME = PRAEFIX
         + "dialogProjectImportRenameContextId"; //$NON-NLS-1$
 
     /** for the "add new test case" dialog */
     public static final String DIALOG_TC_ADD_NEW = PRAEFIX
         + "dialogTestcaseAddNewContextId"; //$NON-NLS-1$
 
     /** for the used project page in the settings dialog */
     public static final String PROJECT_USED_PROPERTY_PAGE = PRAEFIX
         + "projectUsedPropertyPageContextId"; //$NON-NLS-1$
 
     /** for the used toolkit page in the settings dialog */
     public static final String TOOLKIT_PROPERTY_PAGE = PRAEFIX
         + "toolkitPropertyPageContextId"; //$NON-NLS-1$
    
     /** for the support request mail dialog */
     public static final String SUPPORT_REQUEST_DIALOG = PRAEFIX
         + "supportRequestDialogContextId"; //$NON-NLS-1$
     
     /** for the delayed rcp nag dialog */
     public static final String RCP_AUT_START_DELAY_DIALOG = PRAEFIX
         + "supportRCPAUTStartDelayContextId"; //$NON-NLS-1$
     
     /** for the rename component name dialog */
     public static final String RENAME_COMPONENT_NAME = PRAEFIX
         + "renameComponentNameContextId"; //$NON-NLS-1$
     
     /** for the new component name dialog */
     public static final String NEW_COMPONENT_NAME = PRAEFIX
         + "newComponentNameContextId"; //$NON-NLS-1$
     
     /** for the new test data cube name dialog */
     public static final String NEW_TESTDATACUBE_NAME = PRAEFIX
         + "newTestDataCubeNameContextId"; //$NON-NLS-1$
 
     /** dialog for creating a new Test Data Category */
     public static final String NEW_TEST_DATA_CATEGORY = PRAEFIX
         + "newTestDataCategoryContextId"; //$NON-NLS-1$
 
     /** for the merge component name dialog */
     public static final String MERGE_COMPONENT_NAME = PRAEFIX
         + "mergeComponentNameContextId"; //$NON-NLS-1$
     
     /** for the generation wizard */
     public static final String GENERATION_WIZARD = PRAEFIX
         + "generationWizardContextId"; //$NON-NLS-1$
     
     /** for the new model wizard */
     public static final String NEW_MODEL_WIZARD = PRAEFIX
         + "newModelWizardContextId"; //$NON-NLS-1$
     
     /** for the import model wizard */
     public static final String IMPORT_MODEL_WIZARD = PRAEFIX
         + "importModelWizardContextId"; //$NON-NLS-1$
 
     /** ContextHelpId for the Inspector (View) */
     public static final String INSPECTOR_VIEW = PRAEFIX
         + "inspectorViewContextId"; //$NON-NLS-1$
 
     /** ContextHelpId for the Running AUTs View */
     public static final String RUNNING_AUTS_VIEW = PRAEFIX
         + "runningAutsViewContextId"; //$NON-NLS-1$
     
     /** help context id for the test result summary view  */
     public static final String TESTRESULT_SUMMARY_VIEW = PRAEFIX
         + "testResultSummaryViewContextId"; //$NON-NLS-1$
 
     /**
      * help context id for the add comment dialog in the test result summary
      * view
      */
     public static final String ADD_COMMENT = PRAEFIX
         + "testResultSummaryAddCommentContextId"; //$NON-NLS-1$
     
     /** help context id for the Database Connection Configuration dialog */
     public static final String DATABASE_CONNECTION_CONFIGURATION_DIALOG = 
         PRAEFIX + "databaseConnectionConfigurationDialogContextId"; //$NON-NLS-1$
     
 }
