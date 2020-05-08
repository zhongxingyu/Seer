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
 package org.eclipse.jubula.client.ui.rcp.constants;
 
 import org.eclipse.ui.actions.ActionFactory;
 
 
 /**
  * Constants for all used RCP CommandIDs
  *
  * @author BREDEX GmbH
  * @created Jul 30, 2010
  */
 public interface RCPCommandIDs {
     /** the ID of the "add event handler" command */
     public static final String ADD_EVENT_HANDLER_COMMAND_ID = "org.eclipse.jubula.client.ui.rcp.commands.AddEventHandler"; //$NON-NLS-1$
     
     /** the ID of the "copy ID" command */
     public static final String COPY_ID_COMMAND_ID = "org.eclipse.jubula.client.ui.rcp.commands.CopyID"; //$NON-NLS-1$
     
     /** the ID of the "delete project" command */
     public static final String DELETE_PROJECT_COMMAND_ID = "org.eclipse.jubula.client.ui.rcp.commands.DeleteProject"; //$NON-NLS-1$
     
     /** the ID of the "file export" command */
     public static final String ECLIPSE_RCP_FILE_EXPORT_COMMAND_ID  = 
             ActionFactory.EXPORT.getCommandId();
     
     /** the ID of the "file import" command */
     public static final String ECLIPSE_RCP_FILE_IMPORT_COMMAND_ID  = 
             ActionFactory.IMPORT.getCommandId();
     
     /** the ID of the "edit parameters" command */
     public static final String EDIT_PARAMETERS_COMMAND_ID = "org.eclipse.jubula.client.ui.rcp.commands.EditParameters"; //$NON-NLS-1$
     
     /** the ID of the "extract test case" command */
     public static final String EXTRACT_TESTCASE_COMMAND_ID = "org.eclipse.jubula.client.ui.rcp.commands.ExtractTestCase"; //$NON-NLS-1$
 
     /** the ID of the "highlight in AUT" command */
     public static final String HIGHLIGHT_IN_AUT_COMMAND_ID = "org.eclipse.jubula.client.ui.rcp.commands.HighlightInAUT"; //$NON-NLS-1$
     
     /** the ID of the "map into category" command */
     public static final String MAP_INTO_CATEGORY_COMMAND_ID = "org.eclipse.jubula.client.ui.rcp.commands.MapIntoOMCategory"; //$NON-NLS-1$
     
     /** the ID of the "find" command */
     public static final String FIND_COMMAND_ID = 
             ActionFactory.FIND.getCommandId();
     
     /** the ID of the "import project" command */
     public static final String IMPORT_PROJECT_COMMAND_ID = "org.eclipse.jubula.client.ui.rcp.commands.ImportProject"; //$NON-NLS-1$
 
     /**
      * <code>IMPORT_WIZARD_PARAM_ID</code>
      */
     public static final String IMPORT_WIZARD_PARAM_ID = "importWizardId"; //$NON-NLS-1$
     
     /** the ID of the "new cap" command */
     public static final String NEW_CAP_COMMAND_ID = "org.eclipse.jubula.client.ui.rcp.commands.newCap"; //$NON-NLS-1$
     
     /** the ID of the "new category" command */
     public static final String NEW_CATEGORY_COMMAND_ID = "org.eclipse.jubula.client.ui.rcp.commands.CreateNewCategoryCommand"; //$NON-NLS-1$
 
    /** the ID of the "new component name" command */
    public static final String NEW_COMPONENT_NAME_COMMAND_ID = "org.eclipse.jubula.client.ui.rcp.commands.CreateNewLogicalNameCommand"; //$NON-NLS-1$
    
     /** the ID of the "new project" command */
     public static final String NEW_PROJECT_COMMAND_ID = "org.eclipse.jubula.client.ui.rcp.commands.NewProject"; //$NON-NLS-1$
     
     /** the ID of the "add new test data manager" command */
     public static final String NEW_TESTDATACUBE_COMMAND_ID = "org.eclipse.jubula.client.ui.rcp.commands.AddNewTestDataManager"; //$NON-NLS-1$
 
     /** the ID of the "new test case" command */
     public static final String NEW_TESTCASE_COMMAND_ID = "org.eclipse.jubula.client.ui.rcp.commands.NewTestCaseCommand"; //$NON-NLS-1$
     
     /** the ID of the "new test job" command */
     public static final String NEW_TESTJOB_COMMAND_ID = "org.eclipse.jubula.client.ui.rcp.commands.NewTestJobCommand"; //$NON-NLS-1$
     
     /** the ID of the "new test suite" command */
     public static final String NEW_TESTSUITE_COMMAND_ID = "org.eclipse.jubula.client.ui.rcp.commands.NewTestSuiteCommand"; //$NON-NLS-1$
     
     /** the ID of the "delete unused logical component names in OME" command */
     public static final String OME_DELETE_UNUSED_COMPONENT_NAME_COMMAND_ID = "org.eclipse.jubula.client.ui.rcp.commands.OMEDeleteUnusedComponentNames"; //$NON-NLS-1$
     
     /** the ID of the "open central test data editor" command */
     public static final String OPEN_CENTRAL_TESTDATA_EDITOR_COMMAND_ID = "org.eclipse.jubula.client.ui.rcp.commands.OpenCentralTestDataEditor"; //$NON-NLS-1$
     
     /** the ID of the "open object mapping editor" command */
     public static final String OPEN_OBJECTMAPPING_EDITOR_COMMAND_ID = "org.eclipse.jubula.client.ui.rcp.commands.OpenObjectMappingEditor"; //$NON-NLS-1$
     
     /** the ID of the "open project" command */
     public static final String OPEN_PROJECT_COMMAND_ID = "org.eclipse.jubula.client.ui.rcp.commands.OpenProject"; //$NON-NLS-1$
     
     /** the ID of the "open test case" command */
     public static final String OPEN_TESTCASE_COMMAND_ID = "org.eclipse.jubula.client.ui.rcp.commands.OpenTestCase"; //$NON-NLS-1$
 
     /** the ID of the "open test case editor" command */
     public static final String OPEN_TESTCASE_EDITOR_COMMAND_ID = "org.eclipse.jubula.client.ui.rcp.commands.OpenTestcaseEditor"; //$NON-NLS-1$
     
     /** the ID of the "open test job editor" command */
     public static final String OPEN_TESTJOB_EDITOR_COMMAND_ID = "org.eclipse.jubula.client.ui.rcp.commands.OpenTestJobEditor"; //$NON-NLS-1$
     
     /** the ID of the "open test suite editor" command */
     public static final String OPEN_TESTSUITE_EDITOR_COMMAND_ID = "org.eclipse.jubula.client.ui.rcp.commands.OpenTestsuiteEditor"; //$NON-NLS-1$
     
     /** the ID of the "Pause Test Suite" command */
     public static final String PAUSE_TEST_SUITE_COMMAND_ID = "org.eclipse.jubula.client.ui.rcp.commands.PauseTestSuiteCommand"; //$NON-NLS-1$
     
     /** the ID of the "open project properties" command */
     public static final String PROJECT_PROPERTIES_COMMAND_ID = "org.eclipse.jubula.client.ui.rcp.commands.ProjectProperties"; //$NON-NLS-1$
     
     /** the ID of the "reference test case" command */
     public static final String REFERENCE_TC_COMMAND_ID = "org.eclipse.jubula.client.ui.rcp.commands.newTestCaseReference"; //$NON-NLS-1$
     
     /** the ID of the "rename" command */
     public static final String RENAME_COMMAND_ID = "org.eclipse.ui.edit.rename"; //$NON-NLS-1$
     
     /** the ID of the "replace with test case" command */
     public static final String REPLACE_WITH_TESTCASE_COMMAND_ID = "org.eclipse.jubula.client.ui.rcp.commands.ReplaceWithTestCase"; //$NON-NLS-1$
     
     /** the ID of the "revert changes" command */
     public static final String REVERT_CHANGES_COMMAND_ID = "org.eclipse.jubula.client.ui.rcp.commands.RevertChanges"; //$NON-NLS-1$
     
     /** the ID of the "save as new" command */
     public static final String SAVE_AS_NEW_COMMAND_ID = "org.eclipse.jubula.client.ui.rcp.commands.SaveAsNew"; //$NON-NLS-1$
     
     /** the ID of the "show responsible node" command */
     public static final String SHOW_RESPONSIBLE_NODE_COMMAND_ID = "org.eclipse.jubula.client.ui.rcp.commands.ShowResponsibleNodes"; //$NON-NLS-1$
     
     /** the ID of the "show where used" command */
     public static final String SHOW_WHERE_USED_COMMAND_ID = "org.eclipse.jubula.client.ui.rcp.commands.ShowWhereUsed"; //$NON-NLS-1$
     
     /** the ID of the "choose/start AUT" command */
     public static final String START_AUT_COMMAND_ID = "org.eclipse.jubula.client.ui.rcp.commands.ChooseAutCommand"; //$NON-NLS-1$
     
     /** the ID of the "Start Object Mapping Mode" command */
     public static final String START_OBJECT_MAPPING_MODE_COMMAND_ID =  "org.eclipse.jubula.client.ui.rcp.commands.OMStartMappingModeCommand"; //$NON-NLS-1$
 
     /** the ID of the "Start Observation Mode" command */
     public static final String START_OBSERVATION_MODE_COMMAND_ID = "org.eclipse.jubula.client.ui.rcp.commands.StartObservationModeCommand"; //$NON-NLS-1$
     
     /** the ID of the "choose/start Test Job" command */
     public static final String START_TEST_JOB_COMMAND_ID = "org.eclipse.jubula.client.ui.rcp.commands.StartTestJobCommand"; //$NON-NLS-1$
     
     /** the ID of the "choose/start Test Suite" command */
     public static final String START_TEST_SUITE_COMMAND_ID = "org.eclipse.jubula.client.ui.rcp.commands.StartTestSuiteCommand"; //$NON-NLS-1$
     
     /** the ID of the "Stop Test Suite" command */
     public static final String STOP_TEST_SUITE_COMMAND_ID = "org.eclipse.jubula.client.ui.rcp.commands.StopSuiteCommand"; //$NON-NLS-1$
     
     /** the ID of the "toggle active state" command */
     public static final String TOGGLE_ACTIVE_STATE_COMMAND_ID = "org.eclipse.jubula.client.ui.rcp.commands.ToggleActiveStatus"; //$NON-NLS-1$
 
     /** the ID of the "Pause on Error" command */
     public static final String TOGGLE_PAUSE_ON_ERROR_COMMAND_ID = "org.eclipse.jubula.client.ui.rcp.commands.togglePauseOnErrorCommand"; //$NON-NLS-1$
 }
