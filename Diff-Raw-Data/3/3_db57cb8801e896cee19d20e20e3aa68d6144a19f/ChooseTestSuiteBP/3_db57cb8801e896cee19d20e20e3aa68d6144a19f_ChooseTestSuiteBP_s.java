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
 package org.eclipse.jubula.client.ui.rcp.businessprocess;
 
 import java.util.List;
 import java.util.Locale;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.jubula.client.core.agent.AutAgentRegistration;
 import org.eclipse.jubula.client.core.businessprocess.TestExecutionEvent;
 import org.eclipse.jubula.client.core.businessprocess.db.TestSuiteBP;
 import org.eclipse.jubula.client.core.businessprocess.problems.ProblemFactory;
 import org.eclipse.jubula.client.core.events.DataChangedEvent;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.AutState;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.DataState;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.IAutStateListener;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.IDataChangedListener;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.ILanguageChangedListener;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.IOMStateListener;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.IProjectLoadedListener;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.IProjectStateListener;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.IRecordModeStateListener;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.ITestSuiteStateListener;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.OMState;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.ProjectState;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.RecordModeState;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.UpdateState;
 import org.eclipse.jubula.client.core.model.IPersistentObject;
 import org.eclipse.jubula.client.core.model.IProjectPO;
 import org.eclipse.jubula.client.core.model.ITestSuitePO;
 import org.eclipse.jubula.client.core.persistence.GeneralStorage;
 import org.eclipse.jubula.client.ui.constants.Constants;
 import org.eclipse.jubula.client.ui.rcp.Plugin;
 import org.eclipse.jubula.client.ui.rcp.controllers.TestExecutionGUIController;
 import org.eclipse.jubula.client.ui.rcp.i18n.Messages;
 import org.eclipse.jubula.client.ui.rcp.utils.Utils;
 import org.eclipse.jubula.tools.exception.Assert;
 import org.eclipse.jubula.tools.registration.AutIdentifier;
 
 
 /**
  * @author BREDEX GmbH
  * @created 16.08.2006
  */
 public class ChooseTestSuiteBP extends AbstractActionBP {
     
     /**
      * state of testsuite after save action
      */
     public enum TestSuiteState {
         /**<code>unchanged</code> no object was saved */
         /**<code>complete</code> testsuite is executable after save */
         /**<code>incomplete</code> testsuite is not complete after save */
         unchanged, complete, incomplete
     }
 
     /** single instance */
     private static ChooseTestSuiteBP instance = null;
     
     /** last started testsuite */
     private ITestSuitePO m_lastUsedTestSuite;
     
     /** last used AUT for test execution */
     private AutIdentifier m_lastUsedAUT;
     
     /** flag for running state of testexecution */
     private boolean m_isTestRunning = false;
     
     /** flag for running state of AUT */
     private boolean m_isAutStarted = false;
     
     /** flag for project state */
     private boolean m_isProjectLoaded = false;
     
     /** flag for availability of at least one startable testsuite */
     private boolean m_atLeastOneTsAvailable = true;
     
     /** flag for state of OM Mode */
     private boolean m_isOmMode = false;
     
     /** flag for state of Record Mode */
     private boolean m_isRecordMode = false;
     
     /**
      * <code>m_langChangedListener</code> listener for modification of working language
      */
     private ILanguageChangedListener m_langChangedListener = 
         new ILanguageChangedListener() {
             /**
              * @param locale the new Locale
              */
             @SuppressWarnings("synthetic-access") 
             public void handleLanguageChanged(Locale locale) {
                 updateTestSuiteButtonState(true);
             }
         };
 
     /**
      * <code>m_projPropModifyListener</code> listener for modification of project properties
      */
     private IProjectStateListener m_projPropModifyListener =
         new IProjectStateListener() {
             /** {@inheritDoc} */
             public void handleProjectStateChanged(ProjectState state) {
                 if (ProjectState.prop_modified.equals(state)) {
                     updateTestSuiteButtonState(true);
                 }
             }
         };
     
     /** listener for loading of project */
     private IProjectLoadedListener m_projLoadedListener = 
         new IProjectLoadedListener() {
         
             /** {@inheritDoc} */
             @SuppressWarnings("synthetic-access") 
             public void handleProjectLoaded() {
                 m_isProjectLoaded = true;
                 m_lastUsedTestSuite = null;
                 m_isTestRunning = false;
                 m_isAutStarted = false;
                 m_atLeastOneTsAvailable = false;
                 updateTestSuiteButtonState(false);
             }
 
         };
     
     
     /** listener for modification of testsuite state */
     private ITestSuiteStateListener m_testSuiteStateListener = 
         new ITestSuiteStateListener() {
         
             @SuppressWarnings("synthetic-access") 
             public void handleTSStateChanged(TestExecutionEvent event) {
                 switch (event.getState()) {
                     case TestExecutionEvent.TEST_EXEC_START:
                     case TestExecutionEvent.TEST_EXEC_RESTART:
                     case TestExecutionEvent.TEST_EXEC_PAUSED:
                         m_isTestRunning = true;
                         break;
                     case TestExecutionEvent.TEST_EXEC_ERROR:
                     case TestExecutionEvent.TEST_EXEC_STOP:
                     case TestExecutionEvent.TEST_EXEC_FAILED:
                     case TestExecutionEvent.TEST_EXEC_FINISHED:
                     case TestExecutionEvent.TEST_EXEC_OK:
                     case TestExecutionEvent.TEST_EXEC_COMPONENT_FAILED:
                         m_isTestRunning = false;
                         break;
                     default:
                         break;
                 }
                 updateTestSuiteButtonState(false);
             }
         };
     
     /**
      * <code>m_autStateListener</code> listener for modification of aut state
      */
     private IAutStateListener m_autStateListener = 
         new IAutStateListener() {
         /**
          * @param state state from AUT
          */
             @SuppressWarnings("synthetic-access") 
             public void handleAutStateChanged(AutState state) {
                 switch (state) {
                     case running:
                         m_isAutStarted = true;
                         updateTestSuiteButtonState(true);
                         break;
                     case notRunning:
                         m_isAutStarted = false;
                         m_isOmMode = false;
                         m_isRecordMode = false;
                         updateTestSuiteButtonState(false);
                         break;
                     default:
                         Assert.notReached(Messages.UnhandledAutState);
                 }
             }
         };
         
     
     
     
     /**
      * <code>m_currentProjDeletedListener</code>listener for deletion of current project
      */
     private IDataChangedListener m_currentProjDeletedListener =
         new IDataChangedListener() {
             /** {@inheritDoc} */
             public void handleDataChanged(DataChangedEvent... events) {
                 for (DataChangedEvent e : events) {
                     handleDataChanged(e.getPo(), e.getDataState(),
                             e.getUpdateState());
                 }
             }
             
             @SuppressWarnings("synthetic-access") 
             public void handleDataChanged(IPersistentObject po, 
                 DataState dataState, 
                 UpdateState updateState) {
                 if (updateState == UpdateState.onlyInEditor) {
                     return;
                 }
                 if (dataState == DataState.Deleted && po instanceof IProjectPO
                     && GeneralStorage.getInstance().getProject() == null) {
                     m_isProjectLoaded = false;
                     m_lastUsedTestSuite = null;
                     m_isAutStarted = false;
                     m_atLeastOneTsAvailable = false;
                     updateTestSuiteButtonState(false);
                 }
             }
         };
     
     
     /**
      * <code>m_omStateListener</code> listener for modification of OM Mode
      */
     private IOMStateListener m_omStateListener = new IOMStateListener() {
         
         @SuppressWarnings("synthetic-access") 
         public void handleOMStateChanged(OMState state) {
             switch (state) {
                 case running:
                     m_isOmMode = true;
                     m_isRecordMode = false;
                     break;
                 case notRunning:
                     m_isOmMode = false;
                     break;
                 default:
                     Assert.notReached(Messages.UnsupportedObjectMappingState);
             }
             updateTestSuiteButtonState(true);
         }
     };
     
     /**
      * <code>m_recordModeStateListener</code> listener for modification of Record Mode
      */
     private IRecordModeStateListener m_recordModeStateListener = 
         new IRecordModeStateListener() {
         
             @SuppressWarnings("synthetic-access") 
             public void handleRecordModeStateChanged(RecordModeState state) {
                 switch (state) {
                     case running:
                         m_isRecordMode = true;
                         m_isOmMode = false;
                         break;
                     case notRunning:
                         m_isRecordMode = false;
                         break;
                     default:
                         Assert.notReached(Messages.UnsupportedRecordModeState);
                 }
                 updateTestSuiteButtonState(true);                
             }
         };
       
     /** listener for save of editor */    
     private IDataChangedListener m_dataChangedListener = 
         new IDataChangedListener() {
             /** {@inheritDoc} */
             public void handleDataChanged(DataChangedEvent... events) {
                 updateTestSuiteButtonState(true);
             }
         };
 
     /**
      * private constructor
      */
     private ChooseTestSuiteBP() {
         init();
     }
    
     /**
      * @return single instance
      */
     public static ChooseTestSuiteBP getInstance() {
         if (instance == null) {
             instance = new ChooseTestSuiteBP();
         }
         return instance;
     }
     
     /**
      * @return all startable testsuites
      */
     public SortedSet<ITestSuitePO> getAllTestSuites() {
         SortedSet<ITestSuitePO> testSuites = new TreeSet<ITestSuitePO>();
         IProjectPO project = GeneralStorage.getInstance().getProject();
         if (project != null) {
             List<ITestSuitePO> tsInProject = 
                 TestSuiteBP.getListOfTestSuites(project);
             for (ITestSuitePO ts : tsInProject) {
                 if (isTestSuiteStartable(ts)) {
                     testSuites.add(ts);
                 }                    
             }
         }
         return testSuites;        
     }
 
     /**
      * @param ts current testsuite
      * @return if testsuite meets all requirements for execution
      */
     public boolean isTestSuiteStartable(ITestSuitePO ts) {
         return areGeneralRequirementsAchieved(ts)
             && isTestSuiteComplete(ts);            
     }
 
     /**
      * @param ts current testsuite
      * @return if testsuite meets general requirements for execution
      */
     private boolean areGeneralRequirementsAchieved(ITestSuitePO ts) {
         Locale workingLanguage = 
             WorkingLanguageBP.getInstance().getWorkingLanguage();
         return workingLanguage != null
             && AutAgentRegistration.getRunningAuts(
                     GeneralStorage.getInstance().getProject(), null)
                         .keySet().contains(ts.getAut());
     }
     
     /**
      * @param ts current testsuite
      * @return if the given testsuite has severe problems
      */
     public boolean isTestSuiteComplete(ITestSuitePO ts) {
         boolean hasSevereProblems = !ProblemFactory.hasProblem(ts) ? false
                 : ProblemFactory.getWorstProblem(ts.getProblems()).getStatus()
                         .getSeverity() == IStatus.ERROR;
         return !hasSevereProblems;
     }
 
     /**
      * @param testSuite last started testsuite
      */
     public void setLastUsedTestSuite(ITestSuitePO testSuite) {
         m_lastUsedTestSuite = testSuite;        
     }
 
     /**
      * @return Returns the last used TestSuite or null if no TestSuite is
      * available.
      */
     public ITestSuitePO getLastUsedTestSuite() {
         for (ITestSuitePO ts : getAllTestSuites()) {
             if (ts.equals(m_lastUsedTestSuite)) {
                 m_lastUsedTestSuite = ts;
                 return ts;
             }
         }
         return null;
     }
     
     /**
      * {@inheritDoc}
      */
     public boolean isEnabled() {
         return m_isProjectLoaded 
             && m_isAutStarted 
             && m_atLeastOneTsAvailable 
             && !m_isTestRunning
             && !m_isOmMode
             && !m_isRecordMode;
     }
     
     /**
      * @return if general requirements will be met to start any testsuite
      */
     public boolean isInfrastructureReady() {
         return m_isProjectLoaded 
             && m_isAutStarted
             && !m_isTestRunning
             && !m_isOmMode
             && !m_isRecordMode;
     }
     
     
     
     /**
      * updates the EnabledState of StartTestSuiteButton
      * @param validateNumberOfTS determines, if the number of available testsuites
      * is to validate
      */
     private void updateTestSuiteButtonState(boolean validateNumberOfTS) {
         if (validateNumberOfTS) {
             validateNumberOfAvailableTestSuites();
         }
         setEnabledStatus();       
     }
     
     /**
      * 
      */
     private void validateNumberOfAvailableTestSuites() {
         SortedSet<ITestSuitePO> allTestSuites = getAllTestSuites();
         m_atLeastOneTsAvailable = allTestSuites.size() >= 1;        
     }
 
     /**
      * 
      */
     private void init() {
         final DataEventDispatcher ded = DataEventDispatcher.getInstance();
         ded.addProjectLoadedListener(m_projLoadedListener, true);
         ded.addDataChangedListener(m_currentProjDeletedListener, true);
         ded.addAutStateListener(m_autStateListener, true);
         ded.addLanguageChangedListener(m_langChangedListener, true);
         ded.addProjectStateListener(m_projPropModifyListener);
         ded.addTestSuiteStateListener(m_testSuiteStateListener, true);
         ded.addOMStateListener(m_omStateListener, true);
         ded.addRecordModeStateListener(m_recordModeStateListener, true);
         ded.addDataChangedListener(m_dataChangedListener, true);
     }
     
     /**
      * convenience method to save editors and start an incomplete or complete testsuite
      * with changing to execution perspective
      * @param ts testsuite to run
      * @param autId The ID of the Running AUT on which the test will take place.
      * @param autoScreenshot
      *            whether screenshots should be automatically taken in case of
      *            test execution errors
      */
     public void runTestSuite(ITestSuitePO ts, AutIdentifier autId,
             boolean autoScreenshot) {
         TestSuiteState state = validateSaveState(ts);
         if (state != TestSuiteState.incomplete) {
             executeTestSuite(ts, autId, autoScreenshot);
         }
     }
     
     /**
      * @param ts testsuite to validate
      * @return executable state of testsuite
      */
     public TestSuiteState validateSaveState(ITestSuitePO ts) {
         if (Plugin.getDefault().anyDirtyStar()) {
             boolean isSaved = Plugin.getDefault().showSaveEditorDialog();
             if (isSaved) {
                 SortedSet<ITestSuitePO> allTestSuites = 
                     getAllTestSuites();
                 if (allTestSuites.contains(ts)) {
                     return TestSuiteState.complete;
                 } 
                return TestSuiteState.incomplete;
             }
         }
         return TestSuiteState.unchanged;
     }
 
     /**
      * this method can be used for complete and incomplete testsuites
      * 
      * @param tsToStart
      *            current testsuite
      * @param autId
      *            The ID of the Running AUT on which the test will take place.
      * @param autoScreenshot
      *            whether screenshots should be automatically taken in case of
      *            test execution errors
      */
     public void executeTestSuite(ITestSuitePO tsToStart, AutIdentifier autId,
             boolean autoScreenshot) {
         if (!Utils.openPerspective(Constants.EXEC_PERSPECTIVE)) {
             return;
         }
         TestExecutionGUIController.startTestSuite(tsToStart, autId,
                 autoScreenshot);
         ChooseTestSuiteBP.getInstance().setLastUsedTestSuite(tsToStart);
         ChooseTestSuiteBP.getInstance().setLastUsedAUT(autId);
 
     }
 
     /**
      * @param lastUsedAUT the lastUsedAUT to set
      */
     public void setLastUsedAUT(AutIdentifier lastUsedAUT) {
         m_lastUsedAUT = lastUsedAUT;
     }
 
     /**
      * @return the lastUsedAUT
      */
     public AutIdentifier getLastUsedAUT() {
         return m_lastUsedAUT;
     }
 }
