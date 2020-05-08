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
 package org.eclipse.jubula.client.ui.rcp.handlers;
 
 import java.util.HashSet;
 import java.util.Locale;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jface.window.Window;
 import org.eclipse.jubula.client.core.businessprocess.IWritableComponentNameMapper;
 import org.eclipse.jubula.client.core.businessprocess.TestExecution;
 import org.eclipse.jubula.client.core.commands.CAPRecordedCommand;
 import org.eclipse.jubula.client.core.communication.AUTConnection;
 import org.eclipse.jubula.client.core.constants.InitialValueConstants;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.DataState;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.RecordModeState;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.UpdateState;
 import org.eclipse.jubula.client.core.model.INodePO;
import org.eclipse.jubula.client.core.model.ISpecObjContPO;
 import org.eclipse.jubula.client.core.model.ISpecTestCasePO;
 import org.eclipse.jubula.client.core.model.ITestCasePO;
 import org.eclipse.jubula.client.core.model.NodeMaker;
 import org.eclipse.jubula.client.core.persistence.GeneralStorage;
 import org.eclipse.jubula.client.core.persistence.NodePM;
 import org.eclipse.jubula.client.core.persistence.PMException;
 import org.eclipse.jubula.client.core.persistence.Persistor;
 import org.eclipse.jubula.client.ui.constants.Constants;
 import org.eclipse.jubula.client.ui.constants.ContextHelpIds;
 import org.eclipse.jubula.client.ui.constants.IconConstants;
 import org.eclipse.jubula.client.ui.rcp.Plugin;
 import org.eclipse.jubula.client.ui.rcp.businessprocess.WorkingLanguageBP;
 import org.eclipse.jubula.client.ui.rcp.controllers.PMExceptionHandler;
 import org.eclipse.jubula.client.ui.rcp.controllers.TestExecutionContributor;
 import org.eclipse.jubula.client.ui.rcp.dialogs.InputDialog;
 import org.eclipse.jubula.client.ui.rcp.editors.JBEditorHelper;
 import org.eclipse.jubula.client.ui.rcp.editors.JBEditorHelper.EditableState;
 import org.eclipse.jubula.client.ui.rcp.editors.TestCaseEditor;
 import org.eclipse.jubula.client.ui.rcp.handlers.open.AbstractOpenHandler;
 import org.eclipse.jubula.client.ui.rcp.i18n.Messages;
 import org.eclipse.jubula.client.ui.rcp.utils.KeyConverter;
 import org.eclipse.jubula.client.ui.rcp.utils.Utils;
 import org.eclipse.jubula.client.ui.utils.DialogUtils;
 import org.eclipse.jubula.client.ui.utils.JobUtils;
 import org.eclipse.jubula.tools.constants.CommandConstants;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.jubula.tools.exception.CommunicationException;
 import org.eclipse.jubula.tools.exception.JBException;
 import org.eclipse.jubula.tools.exception.ProjectDeletedException;
 import org.eclipse.jubula.tools.registration.AutIdentifier;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 /**
  * @author BREDEX GmbH
  * @created Mar 19, 2010
  */
 public class StartObservationModeHandler extends AbstractRunningAutHandler {
 
     /** ID of command parameter for Running AUT to connect to for mapping */
     public static final String RUNNING_AUT = "org.eclipse.jubula.client.ui.rcp.commands.StartObservationModeCommand.parameter.runningAut"; //$NON-NLS-1$
 
     /** the logger */
     private static final Logger LOG = LoggerFactory
             .getLogger(StartObservationModeHandler.class);
     
     /**
      * @author BREDEX GmbH
      * @created Mar 22, 2010
      */
     private static class StartObservationModeJob extends Job {
         /** test case */
         private ISpecTestCasePO m_workCopy;
         
         /** The ComponentNamesDecorator associated with the 
          *  edit session of the spec test case.
          */
         private IWritableComponentNameMapper m_compNamesMapper;
         
         /** key modifier to activate/deactivate check mode */
         private int m_checkModeMods;
         
         /** key to activate/deactivate check mode */
         private int m_checkModeKey;
         
         /** key modifier for checking component */
         private int m_checkCompMods;
         
         /** key for checking component */
         private int m_checkCompKey;
         
         /** key modifier for selcting elements */
         private int m_recordCompMods;
         
         /** key for selcting elements */
         private int m_recordCompKey;
         
         /** key modifier for selcting elements */
         private int m_recordApplMods;
         
         /** key for selcting elements */
         private int m_recordApplKey;
         
         /** true if dialog should be open */
         private boolean m_dialogOpen;
         
         /** single line trigger */
         private SortedSet<String> m_singleLineTrigger;
         
         /** multi line trigger */
         private SortedSet<String> m_multiLineTrigger;
         
         /** locale for record */
         private Locale m_localeForRecord;
         
         /** editor for current observation */
         private TestCaseEditor m_editor;
         
         /** aut id of connected aut */
         private AutIdentifier m_autId;
 
         /**
          * @param workCopy  SpecTestCasePO
          * @param compNamesMapper The ComponentNamesDecorator associated with the 
          *                        edit session of the spec test case.
          * @param checkModeMods key modifier to activate/deactivate check mode
          * @param checkModeKey key to activate/deactivate check mode
          * @param checkCompMods key modifier for checking component
          * @param checkCompKey key for checking component
          * @param recordCompMods key modifier for selcting elements
          * @param recordCompKey key for selcting elements
          * @param recordApplMods key modifier for selcting elements
          * @param recordApplKey key for selcting elements
          * @param dialogOpen boolean
          * @param singleLineTrigger SortedSet of single line trigger
          * @param multiLineTrigger SortedSet of multi line trigger
          * @param localeForRecord Locale for record
          * @param editor TestCaseEditor
          * @param autId AutIdentifier of connected aut
          */
         public StartObservationModeJob(ISpecTestCasePO workCopy,
                 IWritableComponentNameMapper compNamesMapper,
                 int recordCompMods, int recordCompKey, int recordApplMods,
                 int recordApplKey, int checkModeMods, int checkModeKey,
                 int checkCompMods, int checkCompKey, boolean dialogOpen,
                 SortedSet<String> singleLineTrigger,
                 SortedSet<String> multiLineTrigger, Locale localeForRecord,
                 TestCaseEditor editor, AutIdentifier autId) {
             super("Start Observation Mode"); //$NON-NLS-1$
             m_workCopy = workCopy;
             m_compNamesMapper = compNamesMapper;
             m_recordCompMods = recordCompMods;
             m_recordCompKey = recordCompKey;
             m_recordApplMods = recordApplMods;
             m_recordApplKey = recordApplKey;
             m_checkModeMods = checkModeMods;
             m_checkModeKey = checkModeKey;
             m_checkCompMods = checkCompMods;
             m_checkCompKey = checkCompKey;
             m_dialogOpen = dialogOpen;
             m_singleLineTrigger = singleLineTrigger;
             m_multiLineTrigger = multiLineTrigger;
             m_localeForRecord = localeForRecord;
             m_editor = editor;
             m_autId = autId;
         }
 
         /**
          * {@inheritDoc}
          */
         protected IStatus run(IProgressMonitor monitor) {
             
             try {
                 if (AUTConnection.getInstance().connectToAut(m_autId, 
                         new NullProgressMonitor())) {
                     final String toolkit = TestExecution.getInstance()
                         .getConnectedAut().getToolkit();
                     if (toolkit.equals(CommandConstants.SWT_TOOLKIT)
                             || toolkit.equals(CommandConstants.RCP_TOOLKIT)) {
                         m_checkModeMods = KeyConverter.convertSwingStateMask(
                                 m_checkModeMods);
                         m_checkModeKey = KeyConverter.convertSwingToSwt(
                                 m_checkModeKey);
                         m_checkCompMods = KeyConverter.convertSwingStateMask(
                                 m_checkCompMods);
                         m_checkCompKey = KeyConverter.convertSwingToSwt(
                                 m_checkCompKey);
                     }
                     TestExecutionContributor.getInstance().getClientTest()
                     .startRecordTestCase(m_workCopy, m_compNamesMapper,
                             m_recordCompMods, m_recordCompKey, m_recordApplMods,
                             m_recordApplKey, m_checkModeMods, m_checkModeKey,
                             m_checkCompMods, m_checkCompKey, m_dialogOpen,
                             m_singleLineTrigger, m_multiLineTrigger,
                             m_localeForRecord);
                 }
             } catch (CommunicationException ce) {
                 LOG.error(ce.getMessage());
                 // HERE: notify the listeners about unsuccessfull mode change
             }
             CAPRecordedCommand.setRecordListener(m_editor);
             DataEventDispatcher.getInstance().fireRecordModeStateChanged(
                     RecordModeState.running);
             return Status.OK_STATUS;
         }
         
         /**
          * {@inheritDoc}
          */
         public boolean belongsTo(Object family) {
             if (family instanceof StartObservationModeHandler) {
                 return true;
             }
             
             return super.belongsTo(family);
         }
 
     }
 
     /**
      * {@inheritDoc}
      */
     public Object execute(ExecutionEvent event) {
         AutIdentifier runningAut = getRunningAut(event, RUNNING_AUT);
 
         if (!Utils.openPerspective(Constants.SPEC_PERSPECTIVE)) {
             return null;
         }
         TestCaseEditor editor;
         if (Plugin.getActiveEditor() instanceof TestCaseEditor) {
             editor = (TestCaseEditor)Plugin.getActiveEditor();
         } else {
             editor = askForNewTC();
         }
         if (editor != null
                 && editor.getEditorHelper().requestEditableState() 
                 == JBEditorHelper.EditableState.OK) {
             setEditor(editor, runningAut);
         }
 
         return null;
     }
 
     /**
      * ask for a TC Name and returns a new TC Editor
      * 
      * @return SpecTestCaseEditor
      */
     private TestCaseEditor askForNewTC() {
         TestCaseEditor editor = null;
         String standardName = 
             InitialValueConstants.DEFAULT_TEST_CASE_NAME_OBSERVED;
         int index = 1;
         String newName = standardName + index;
         final Set<String> usedNames = new HashSet<String>();
         // generate a unique name
         for (Object node : GeneralStorage.getInstance().getProject()
                 .getSpecObjCont().getSpecObjList()) {
             if (Persistor.isPoSubclass((INodePO)node, ITestCasePO.class)
                     && ((INodePO)node).getName().startsWith(standardName)) {
                 usedNames.add(((INodePO)node).getName());
             }
         }
         while (usedNames.contains(newName)) {
             index++;
             newName = standardName + index;
         }
         InputDialog dialog = createDialog(newName, usedNames);
         dialog.setHelpAvailable(true);
         dialog.create();
         DialogUtils.setWidgetNameForModalDialog(dialog);
         Plugin.getHelpSystem().setHelp(dialog.getShell(),
                 ContextHelpIds.DIALOG_OBS_TC_SAVE);
         dialog.open();
         if (Window.OK == dialog.getReturnCode()) {
             String tcName = dialog.getName();
            final INodePO parentPO = ISpecObjContPO.TCB_ROOT_NODE;
             ISpecTestCasePO recSpecTestCase = NodeMaker
                     .createSpecTestCasePO(tcName);
             try {
                 NodePM.addAndPersistChildNode(parentPO, recSpecTestCase, null,
                         NodePM.getCmdHandleChild(parentPO, recSpecTestCase));
                 DataEventDispatcher.getInstance().fireDataChangedListener(
                         recSpecTestCase, DataState.Added, UpdateState.all);
                 editor = (TestCaseEditor)AbstractOpenHandler
                         .openEditor(recSpecTestCase);
             } catch (PMException e) {
                 PMExceptionHandler.handlePMExceptionForMasterSession(e);
             } catch (ProjectDeletedException e) {
                 PMExceptionHandler.handleGDProjectDeletedException();
             }
         }
         dialog.close();
         return editor;
     }
 
     /**
      * set this editor for recording test case
      * 
      * @param editor SpecTestCaseEditor
      * @param autId AutIdentifier
      */
     private void setEditor(TestCaseEditor editor, AutIdentifier autId) {
         if (editor.getEditorHelper()
                 .requestEditableState() != EditableState.OK) {
             editor.getEditorHelper().setDirty(true);
         }
         int recordCompMods = Plugin.getDefault().getPreferenceStore().getInt(
                 Constants.RECORDMOD_COMP_MODS_KEY);
         int recordCompKey = Plugin.getDefault().getPreferenceStore().getInt(
                 Constants.RECORDMOD_COMP_KEY_KEY);
         int recordApplMods = Plugin.getDefault().getPreferenceStore().getInt(
                 Constants.RECORDMOD_APPL_MODS_KEY);
         int recordApplKey = Plugin.getDefault().getPreferenceStore().getInt(
                 Constants.RECORDMOD_APPL_KEY_KEY);
 
         int checkModeMods = Plugin.getDefault().getPreferenceStore().getInt(
                 Constants.CHECKMODE_MODS_KEY);
         int checkModeKey = Plugin.getDefault().getPreferenceStore().getInt(
                 Constants.CHECKMODE_KEY_KEY);
         int checkCompMods = Plugin.getDefault().getPreferenceStore().getInt(
                 Constants.CHECKCOMP_MODS_KEY);
         int checkCompKey = Plugin.getDefault().getPreferenceStore().getInt(
                 Constants.CHECKCOMP_KEY_KEY);
         boolean dialogOpen = Plugin.getDefault().getPreferenceStore()
                 .getBoolean(Constants.SHOWRECORDDIALOG_KEY);
 
         SortedSet<String> singleLineTrigger = new TreeSet<String>();
         SortedSet<String> multiLineTrigger = new TreeSet<String>();
         try {
             singleLineTrigger = 
                 org.eclipse.jubula.client.ui.preferences.utils.Utils
                     .decodeStringToSet(Plugin.getDefault().getPreferenceStore()
                             .getString(Constants.SINGLELINETRIGGER_KEY),
                             StringConstants.SEMICOLON);
             multiLineTrigger = 
                 org.eclipse.jubula.client.ui.preferences.utils.Utils
                     .decodeStringToSet(Plugin.getDefault().getPreferenceStore()
                             .getString(Constants.MULTILINETRIGGER_KEY),
                             StringConstants.SEMICOLON);
         } catch (JBException e) {
             e.printStackTrace();
         }
 
         final Locale localeForRecord = WorkingLanguageBP.getInstance()
                 .getWorkingLanguage();
         final ISpecTestCasePO workCopy = 
             (ISpecTestCasePO)editor.getEditorHelper().getEditSupport()
                 .getWorkVersion();
         final IWritableComponentNameMapper compNamesMapper = editor
                 .getEditorHelper().getEditSupport().getCompMapper();
         
         Job startObservationModeJob = new StartObservationModeJob(
                 workCopy, compNamesMapper,
                 recordCompMods, recordCompKey, recordApplMods,
                 recordApplKey, checkModeMods, checkModeKey,
                 checkCompMods, checkCompKey, dialogOpen,
                 singleLineTrigger, multiLineTrigger, localeForRecord,
                 editor, autId);
         startObservationModeJob.setSystem(true);
         JobUtils.executeJob(startObservationModeJob, null);
     }
 
     /**
      * @param newName
      *            new name
      * @param usedNames
      *            used name
      * @return input dialog
      */
     private InputDialog createDialog(String newName,
             final Set<String> usedNames) {
         InputDialog dialog = new InputDialog(Plugin.getShell(), 
                 Messages.RecordTestCaseActionTCTitle,
                 newName, Messages.RecordTestCaseActionTCMessage,
                 Messages.RecordTestCaseActionTCLabel,
                 Messages.RenameActionTCError,
                 Messages.RecordTestCaseActionDoubleTCName,
                 IconConstants.OBSERVE_TC_DIALOG_STRING, 
                 Messages.RecordTestCaseActionTCShell,
                 false) {
             protected boolean isInputAllowed() {
                 if (usedNames.contains((getInputFieldText()))) {
                     return false;
                 }
                 return super.isInputAllowed();
             }
         };
         return dialog;
     }
 
 }
