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
     public static final String ADD_EVENT_HANDLER = "org.eclipse.jubula.client.ui.rcp.commands.AddEventHandler"; //$NON-NLS-1$
     
     /** the ID of the "copy ID" command */
     public static final String COPY_ID = "org.eclipse.jubula.client.ui.rcp.commands.CopyID"; //$NON-NLS-1$
     
     /** the ID of the "connnect to AUT Agent" command */
     public static final String CONNECT_TO_AUT_AGENT = "org.eclipse.jubula.client.ui.rcp.commands.ConnectToAUTAgentCommand"; //$NON-NLS-1$
     
     /** the ID of the "delete project" command */
     public static final String DELETE_PROJECT = "org.eclipse.jubula.client.ui.rcp.commands.DeleteProject"; //$NON-NLS-1$
     
     /** the ID of the "file export" command */
     public static final String ECLIPSE_RCP_FILE_EXPORT  = 
             ActionFactory.EXPORT.getCommandId();
     
     /** the ID of the "file import" command */
     public static final String ECLIPSE_RCP_FILE_IMPORT  = 
             ActionFactory.IMPORT.getCommandId();
     
     /** the ID of the "edit parameters" command */
     public static final String EDIT_PARAMETERS = "org.eclipse.jubula.client.ui.rcp.commands.EditParameters"; //$NON-NLS-1$
     
     /** the ID of the "extract test case" command */
     public static final String EXTRACT_TESTCASE = "org.eclipse.jubula.client.ui.rcp.commands.ExtractTestCase"; //$NON-NLS-1$
 
     /** the ID of the "highlight in AUT" command */
     public static final String HIGHLIGHT_IN_AUT = "org.eclipse.jubula.client.ui.rcp.commands.HighlightInAUT"; //$NON-NLS-1$
     
     /** the ID of the "map into category" command */
     public static final String MAP_INTO_CATEGORY = "org.eclipse.jubula.client.ui.rcp.commands.MapIntoOMCategory"; //$NON-NLS-1$
     
     /** the ID of the "find" command */
     public static final String FIND = ActionFactory.FIND.getCommandId();
     
     /**
      * <code>IMPORT_WIZARD_PARAM_ID</code>
      */
     public static final String IMPORT_WIZARD_PARAM_ID = "importWizardId"; //$NON-NLS-1$
     
     /** the ID of the "new cap" command */
     public static final String NEW_CAP = "org.eclipse.jubula.client.ui.rcp.commands.newCap"; //$NON-NLS-1$
     
     /** the ID of the "new category" command */
     public static final String NEW_CATEGORY = "org.eclipse.jubula.client.ui.rcp.commands.CreateNewCategoryCommand"; //$NON-NLS-1$
 
     /** the ID of the "new component name" command */
     public static final String NEW_COMPONENT_NAME = "org.eclipse.jubula.client.ui.rcp.commands.CreateNewLogicalNameCommand"; //$NON-NLS-1$
     
     /** the ID of the "add new test data manager" command */
     public static final String NEW_TESTDATACUBE = "org.eclipse.jubula.client.ui.rcp.commands.AddNewTestDataManager"; //$NON-NLS-1$
 
     /** the ID of the "new test case" command */
     public static final String NEW_TESTCASE = "org.eclipse.jubula.client.ui.rcp.commands.NewTestCaseCommand"; //$NON-NLS-1$
     
     /** the ID of the "new test job" command */
     public static final String NEW_TESTJOB = "org.eclipse.jubula.client.ui.rcp.commands.NewTestJobCommand"; //$NON-NLS-1$
     
     /** the ID of the "new test suite" command */
     public static final String NEW_TESTSUITE = "org.eclipse.jubula.client.ui.rcp.commands.NewTestSuiteCommand"; //$NON-NLS-1$
     
     /** the ID of the "delete unused logical component names in OME" command */
     public static final String OME_DELETE_UNUSED_COMPONENT_NAME = "org.eclipse.jubula.client.ui.rcp.commands.OMEDeleteUnusedComponentNames"; //$NON-NLS-1$
     
     /** the ID of the "open central test data editor" command */
     public static final String OPEN_CENTRAL_TESTDATA_EDITOR = "org.eclipse.jubula.client.ui.rcp.commands.OpenCentralTestDataEditor"; //$NON-NLS-1$
     
     /** the ID of the "open object mapping editor" command */
     public static final String OPEN_OBJECTMAPPING_EDITOR = "org.eclipse.jubula.client.ui.rcp.commands.OpenObjectMappingEditor"; //$NON-NLS-1$
     
     /** the ID of the "open project" command */
     public static final String OPEN_PROJECT = "org.eclipse.jubula.client.ui.rcp.commands.OpenProject"; //$NON-NLS-1$
     
     /** the ID of the "open test case" command */
     public static final String OPEN_TESTCASE = "org.eclipse.jubula.client.ui.rcp.commands.OpenTestCase"; //$NON-NLS-1$
 
     /** the ID of the "open test case editor" command */
     public static final String OPEN_TESTCASE_EDITOR = "org.eclipse.jubula.client.ui.rcp.commands.OpenTestcaseEditor"; //$NON-NLS-1$
     
     /** the ID of the "open test job editor" command */
     public static final String OPEN_TESTJOB_EDITOR = "org.eclipse.jubula.client.ui.rcp.commands.OpenTestJobEditor"; //$NON-NLS-1$
     
     /** the ID of the "open test suite editor" command */
     public static final String OPEN_TESTSUITE_EDITOR = "org.eclipse.jubula.client.ui.rcp.commands.OpenTestsuiteEditor"; //$NON-NLS-1$
     
     /** the ID of the "Pause Test Suite" command */
     public static final String PAUSE_TEST_SUITE = "org.eclipse.jubula.client.ui.rcp.commands.PauseTestSuiteCommand"; //$NON-NLS-1$
     
     /** the ID of the "open project properties" command */
     public static final String PROJECT_PROPERTIES = "org.eclipse.jubula.client.ui.rcp.commands.ProjectProperties"; //$NON-NLS-1$
     
     /** the ID of the "reference test case" command */
     public static final String REFERENCE_TC = "org.eclipse.jubula.client.ui.rcp.commands.newTestCaseReference"; //$NON-NLS-1$
     
     /** the ID of the "rename" command */
     public static final String RENAME = "org.eclipse.ui.edit.rename"; //$NON-NLS-1$
     
     /** the ID of the "replace with test case" command */
     public static final String REPLACE_WITH_TESTCASE = "org.eclipse.jubula.client.ui.rcp.commands.ReplaceWithTestCase"; //$NON-NLS-1$
     
     /** the ID of the "revert changes" command */
     public static final String REVERT_CHANGES = "org.eclipse.jubula.client.ui.rcp.commands.RevertChanges"; //$NON-NLS-1$
     
     /** the ID of the "save as new" command */
     public static final String SAVE_AS_NEW = "org.eclipse.jubula.client.ui.rcp.commands.SaveAsNew"; //$NON-NLS-1$
     
     /** the ID of the "show responsible node" command */
     public static final String SHOW_RESPONSIBLE_NODE = "org.eclipse.jubula.client.ui.rcp.commands.ShowResponsibleNodes"; //$NON-NLS-1$
     
     /** the ID of the "show where used" command */
     public static final String SHOW_WHERE_USED = "org.eclipse.jubula.client.ui.rcp.commands.ShowWhereUsed"; //$NON-NLS-1$
     
     /** the ID of the "choose/start AUT" command */
     public static final String START_AUT = "org.eclipse.jubula.client.ui.rcp.commands.ChooseAutCommand"; //$NON-NLS-1$
     
     /** the ID of the "Start Object Mapping Mode" command */
     public static final String START_OBJECT_MAPPING_MODE =  "org.eclipse.jubula.client.ui.rcp.commands.OMStartMappingModeCommand"; //$NON-NLS-1$
 
     /** the ID of the "Start Observation Mode" command */
     public static final String START_OBSERVATION_MODE = "org.eclipse.jubula.client.ui.rcp.commands.StartObservationModeCommand"; //$NON-NLS-1$
     
     /** the ID of the "choose/start Test Job" command */
     public static final String START_TEST_JOB = "org.eclipse.jubula.client.ui.rcp.commands.StartTestJobCommand"; //$NON-NLS-1$
     
     /** the ID of the "choose/start Test Suite" command */
     public static final String START_TEST_SUITE = "org.eclipse.jubula.client.ui.rcp.commands.StartTestSuiteCommand"; //$NON-NLS-1$
     
     /** the ID of the "Stop Test Suite" command */
     public static final String STOP_TEST_SUITE = "org.eclipse.jubula.client.ui.rcp.commands.StopSuiteCommand"; //$NON-NLS-1$
     
     /** the ID of the "toggle active state" command */
     public static final String TOGGLE_ACTIVE_STATE = "org.eclipse.jubula.client.ui.rcp.commands.ToggleActiveStatus"; //$NON-NLS-1$
 
     /** the ID of the "Pause on Error" command */
     public static final String TOGGLE_PAUSE_ON_ERROR = "org.eclipse.jubula.client.ui.rcp.commands.togglePauseOnErrorCommand"; //$NON-NLS-1$
     
     /** the ID of the "Choose html window" command */
     public static final String CHOOSE_HTML_WINDOW = "org.eclipse.jubula.client.ui.rcp.commands.html.ChooseAuTWindow"; //$//$NON-NLS-1$
    
    /** the ID of the "set main view instance" command */
    public static final String SET_MAIN_VIEW_INSTANCE = "org.eclipse.jubula.client.ui.rcp.commands.SetAsMainViewInstance"; //$//$NON-NLS-1$

    /** the ID of the "collapse all" command */
    public static final String COLLAPSE_ALL = "org.eclipse.jubula.client.ui.rcp.commands.CollapseAll"; //$//$NON-NLS-1$
 }
