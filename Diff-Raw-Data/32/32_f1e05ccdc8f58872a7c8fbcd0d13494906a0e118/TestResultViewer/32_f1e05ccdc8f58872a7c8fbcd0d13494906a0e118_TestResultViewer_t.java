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
 package org.eclipse.jubula.client.ui.editors;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Stack;
 
 import javax.persistence.EntityManager;
 
 import org.apache.commons.lang.StringUtils;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.core.runtime.SubMonitor;
 import org.eclipse.jface.action.GroupMarker;
 import org.eclipse.jface.action.IMenuListener;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.viewers.DecoratingLabelProvider;
 import org.eclipse.jface.viewers.DecorationContext;
 import org.eclipse.jface.viewers.IDecorationContext;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jubula.client.core.businessprocess.TestresultSummaryBP;
 import org.eclipse.jubula.client.core.model.INodePO;
 import org.eclipse.jubula.client.core.model.IParameterDetailsPO;
 import org.eclipse.jubula.client.core.model.ITestResultPO;
 import org.eclipse.jubula.client.core.model.ITestResultSummaryPO;
 import org.eclipse.jubula.client.core.model.ITestSuitePO;
 import org.eclipse.jubula.client.core.model.NodeMaker;
 import org.eclipse.jubula.client.core.model.PoMaker;
 import org.eclipse.jubula.client.core.model.TestResultNode;
 import org.eclipse.jubula.client.core.model.TestResultParameter;
 import org.eclipse.jubula.client.core.persistence.GeneralStorage;
 import org.eclipse.jubula.client.core.persistence.NodePM;
 import org.eclipse.jubula.client.core.persistence.Persistor;
 import org.eclipse.jubula.client.core.persistence.TestResultPM;
 import org.eclipse.jubula.client.ui.Plugin;
 import org.eclipse.jubula.client.ui.constants.CommandIDs;
 import org.eclipse.jubula.client.ui.constants.Constants;
 import org.eclipse.jubula.client.ui.constants.ContextHelpIds;
 import org.eclipse.jubula.client.ui.i18n.Messages;
 import org.eclipse.jubula.client.ui.provider.contentprovider.TestResultTreeViewContentProvider;
 import org.eclipse.jubula.client.ui.provider.labelprovider.TestResultTreeViewLabelProvider;
 import org.eclipse.jubula.client.ui.utils.CommandHelper;
 import org.eclipse.jubula.client.ui.views.IJBPart;
 import org.eclipse.jubula.client.ui.views.ITreeViewerContainer;
 import org.eclipse.jubula.client.ui.views.NonSortedPropertySheetPage;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.jubula.tools.i18n.I18n;
 import org.eclipse.jubula.tools.objects.event.TestErrorEvent;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.IWorkbenchActionConstants;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.part.EditorPart;
 import org.eclipse.ui.progress.IProgressService;
 import org.eclipse.ui.views.properties.IPropertySheetPage;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 /**
  * Viewer for Test Results associated with a Test Result Summary.
  *
  * @author BREDEX GmbH
  * @created May 17, 2010
  */
 public class TestResultViewer extends EditorPart implements ISelectionProvider,
     ITreeViewerContainer, IAdaptable, IJBPart {
     /** Constant: Editor ID */
     public static final String EDITOR_ID = 
         "org.eclipse.jubula.client.ui.editors.TestResultViewer"; //$NON-NLS-1$
     
     /** 
      * ID of the decoration context property for Test Suite end time.
      * The value of the property is a {@link java.util.Date}.
      */
     public static final String DECORATION_CONTEXT_SUITE_END_TIME_ID = 
         "org.eclipse.jubula.client.ui.editors.TestResultViewer.testSuiteEndTime"; //$NON-NLS-1$
     
     /** the logger */
     private static final Logger LOG = 
         LoggerFactory.getLogger(TestResultViewer.class);
 
     /**
      * Operation to gather Test Result information from the database and use 
      * that information to construct a Test Result tree.
      *
      * @author BREDEX GmbH
      * @created May 18, 2010
      */
     public static final class GenerateTestResultTreeOperation 
             implements IRunnableWithProgress {
 
         /** 
          * Reverse lookup for test error event IDs. This is necessary because the
          * values stored in the database are internationalized, whereas most of the
          * time we really need the ID itself. 
          */
         private static Map<String, String> eventIdReverseLookup =
             new HashMap<String, String>();
 
         static {
             //FIXME NLS
             eventIdReverseLookup.put(I18n.getString(
                     TestErrorEvent.ID.COMPONENT_NOT_FOUND), 
                 TestErrorEvent.ID.COMPONENT_NOT_FOUND);
             eventIdReverseLookup.put(I18n.getString(
                     TestErrorEvent.ID.CONFIGURATION_ERROR), 
                 TestErrorEvent.ID.CONFIGURATION_ERROR);
             eventIdReverseLookup.put(I18n.getString(
                     TestErrorEvent.ID.ACTION_ERROR), 
                 TestErrorEvent.ID.ACTION_ERROR);
             eventIdReverseLookup.put(I18n.getString(
                     TestErrorEvent.ID.VERIFY_FAILED), 
                 TestErrorEvent.ID.VERIFY_FAILED);
         }
 
         /** the database ID of the summary for which to generate the tree */
         private Long m_summaryId;
         
         /** the database ID of the Project associated with the test run */
         private Long m_parentProjectId;
 
         /** the root node of the created Test Result tree */
         private TestResultNode m_rootNode;
 
         /** the manager for Test Result entities */
         private EntityManager m_session;
         
         /**
          * Constructor
          * 
          * @param summaryId The database ID of the summary for which to generate the
          *                  tree.
          * @param parentProjectId The database ID of the Project associated 
          *                        with the test run.
          * @param session The manager for Test Result entities. The caller is
          *                  responsible for managing the session (opening, 
          *                  closing, etc.). Closing the session before or during
          *                  the operation will cause errors.
          */
         public GenerateTestResultTreeOperation(
                 Long summaryId, Long parentProjectId, EntityManager session) {
             m_summaryId = summaryId;
             m_parentProjectId = parentProjectId;
             m_session = session;
         }
         
         /**
          * {@inheritDoc}
          */
         public void run(IProgressMonitor pMonitor) {
             SubMonitor monitor = SubMonitor.convert(pMonitor,
                 "Fetching test result data...", 3); //$NON-NLS-1$
             try {
                 monitor.subTask("Loading test results from database..."); //$NON-NLS-1$
                 List<ITestResultPO> testResultList = TestResultPM
                     .computeTestResultListForSummary(m_session, m_summaryId);
                 monitor.worked(1);
                 
                 TestResultNode createdNode = null;
                 Stack<TestResultNode> parentNodeStack = 
                     new Stack<TestResultNode>();
                 Set<String> allGuids = new HashSet<String>();
                 for (ITestResultPO result : testResultList) {
                     allGuids.add(result.getInternalKeywordGuid());
                 }
 
                 monitor.subTask("Loading " + allGuids.size()  //$NON-NLS-1$
                     + " backing nodes from database..."); //$NON-NLS-1$
                 Map<String, INodePO> guidToNodeMap = NodePM.getNodes(
                         m_parentProjectId, allGuids, 
                         GeneralStorage.getInstance().getMasterSession());
                 monitor.worked(1);
 
                 int remainingWork = testResultList.size();
                 SubMonitor sMonitor = SubMonitor.convert(monitor,
                     "Fetching test result data...", //$NON-NLS-1$
                     remainingWork);
                 sMonitor.subTask("Creating test result tree...");
                 for (ITestResultPO result : testResultList) {
                     if (pMonitor.isCanceled()) {
                         throw new OperationCanceledException();
                     }
                     int keywordLevel = result.getKeywordLevel();
                     if (keywordLevel > parentNodeStack.size()) {
                         parentNodeStack.push(createdNode);
                     } else {
                         while (keywordLevel < parentNodeStack.size()) {
                             parentNodeStack.pop();
                         }
                     }
                     INodePO backingNode = 
                         guidToNodeMap.get(result.getInternalKeywordGuid()); 
                     final boolean backingNodeExists = backingNode != null;
                     if (!backingNodeExists) {
                         backingNode = generateBackingNode(result);
                     }
                     
                     createdNode = new TestResultNode(backingNodeExists, 
                             backingNode, 
                             parentNodeStack.isEmpty() ? null 
                                     : parentNodeStack.peek());
                     createdNode.setComponentName(result.getComponentName());
                     createdNode.setComponentType(result.getComponentType());
                     createdNode.setTaskId(result.getTaskId());
                     
                     for (IParameterDetailsPO param 
                             : result.getUnmodifiableParameterList()) {
                         createdNode.addParameter(
                                 new TestResultParameter(param));
                     }
 
                     createdNode.setResult(result.getInternalKeywordStatus(), 
                             generateTestErrorEvent(result));
                     createdNode.setScreenshot(result.getImage());
                     createdNode.setTimestamp(result.getTimestamp());
                    createdNode.setOmHeuristicEquivalence(result
                            .getOmHeuristicEquivalence());
                     if (m_rootNode == null) {
                         m_rootNode = createdNode;
                     }
                     sMonitor.worked(1);
                 }
             } finally {
                 pMonitor.done();
             }
         }
 
         /**
          * 
          * @return the root node of the Test Result tree generated by this 
          *         operation. Behavior when this method is called before the 
          *         operation is complete is undefined.
          */
         public TestResultNode getRootNode() {
             return m_rootNode;
         }
 
         /**
          * Creates and returns a transient keyword suitable for backing the 
          * given result.
          * 
          * @param result The result for which to generate a backing keyword.
          * @return a transient keyword that backs the given result, or 
          *         <code>null</code> if the keyword type is not recognized.
          */
         private INodePO generateBackingNode(ITestResultPO result) {
             switch (result.getInternalKeywordType()) {
                 case TestresultSummaryBP.TYPE_TEST_STEP:
                     // FIXME zeb in order to construct a valid Test Step, we
                     // sometimes use whitespace (" ") as a
                     // placeholder. This works so far, as the only
                     // validation is that the string is neither null
                     // nor empty, but this may cause problems in
                     // future.
                     String componentName = !StringUtils.isEmpty(result
                             .getComponentName()) ? result.getComponentName()
                             : StringConstants.SPACE;
                     return NodeMaker.createCapPO(result.getKeywordName(),
                             componentName,
                         result.getInternalComponentType() != null ? result
                                 .getInternalComponentType()
                                 : StringConstants.SPACE,
                         result.getInternalActionName() != null ? result
                                 .getInternalActionName()
                                 : StringConstants.SPACE);
                 case TestresultSummaryBP.TYPE_TEST_CASE:
                     return NodeMaker.createSpecTestCasePO(
                             result.getKeywordName(),
                             result.getInternalKeywordGuid());
                 case TestresultSummaryBP.TYPE_TEST_SUITE:
                     ITestSuitePO backingTestSuite = NodeMaker
                             .createTestSuitePO(result.getKeywordName(),
                                     result.getInternalKeywordGuid());
                     ITestResultSummaryPO summary = GeneralStorage
                         .getInstance().getMasterSession()
                             .find(PoMaker.getTestResultSummaryClass(),
                                     m_summaryId);
                     backingTestSuite.setAut(PoMaker.createAUTMainPO(summary
                             .getAutName()));
                     return backingTestSuite;
                 default:
                     return null;
             }
         }
 
         /**
          * 
          * @param result The result for which to generate a test error event.
          * @return a test error event corresponding to the given result, or 
          *         <code>null</code> if the given result is not an error result.
          */
         private TestErrorEvent generateTestErrorEvent(ITestResultPO result) {
             TestErrorEvent errorEvent = null;
             if (result.getInternalKeywordStatus() == TestResultNode.ERROR) {
                 errorEvent = new TestErrorEvent(
                     eventIdReverseLookup.get(result.getStatusType()));
                 if (result.getStatusDescription() != null) {
                     errorEvent.addProp(
                             TestErrorEvent.Property.DESCRIPTION_KEY, 
                             result.getStatusDescription());
                 }
                 if (result.getActualValue() != null) {
                     errorEvent.addProp(
                             TestErrorEvent.Property.ACTUAL_VALUE_KEY, 
                             result.getActualValue());
                 }
                 if (result.getStatusOperator() != null) {
                     errorEvent.addProp(
                             TestErrorEvent.Property.OPERATOR_KEY, 
                             result.getStatusOperator());
                 }
                 if (result.getExpectedValue() != null) {
                     errorEvent.addProp(
                             TestErrorEvent.Property.PATTERN_KEY, 
                             result.getExpectedValue());
                 }
                 
                 return errorEvent;
             }
             
             return null;
         }
     }
     
     /** the viewer */
     private TreeViewer m_viewer;
     
     /** the manager for Test Result entities */
     private EntityManager m_session;
     
     /** 
      * whether the Test Results loaded in this viewer will be cached in the 
      * master session 
      */
     private boolean m_cacheResults;
 
     /**
      * the root node
      */
     private TestResultNode m_testResultRootNode;
     
     /** {@inheritDoc} */
     public void doSave(IProgressMonitor monitor) {
         // "Save" not supported. Do nothing.
     }
 
     /** {@inheritDoc} */
     public void doSaveAs() {
         // "Save as" not supported. Do nothing.
     }
     
     /** {@inheritDoc} */
     public Object getAdapter(Class key) {
         if (key.equals(IPropertySheetPage.class)) {
             return new NonSortedPropertySheetPage();
         }
         return super.getAdapter(key);
     }
 
     /** {@inheritDoc} */
     public void init(IEditorSite site, IEditorInput input) 
         throws PartInitException {
 
         if (input instanceof TestResultEditorInput) {
             setSite(site);
             setInput(input);
             setPartName(input.getName());
         } else {
             throw new PartInitException(Messages.EditorInitCreateError);
         }
         
     }
 
     /**
      * Generates a Test Result tree and returns the root node of the generated 
      * tree.
      * 
      * @param summaryId The database ID of the summary for which to generate the
      *                  tree.
      * @param parentProjectId The database ID of the Project associated with 
      *                        the test run.
      * @return the root node of the generated Test Result tree.
      * 
      * @throws InterruptedException if the operation was cancelled by the user.
      */
     private TestResultNode generateTestResult(
             Long summaryId, Long parentProjectId) throws InterruptedException {
         
         IProgressService progressService = 
             (IProgressService)getSite().getService(IProgressService.class);
         
         GenerateTestResultTreeOperation operation = 
             new GenerateTestResultTreeOperation(summaryId, 
                     parentProjectId, m_cacheResults 
                         ? GeneralStorage.getInstance().getMasterSession() 
                                 : m_session);
         
         try {
             progressService.busyCursorWhile(operation);
         } catch (InvocationTargetException e) {
             LOG.error(Messages.ErrorFetchingTestResultInformation 
                 + StringConstants.DOT, e);
         } catch (OperationCanceledException oce) {
             throw new InterruptedException();
         }
         
         return operation.getRootNode();
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     public boolean isDirty() {
         return false;
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     public boolean isSaveAsAllowed() {
         return false;
     }
 
     /**
      * {@inheritDoc}
      */
     public void createPartControl(Composite parent) {
         TestResultEditorInput editorInput = 
             (TestResultEditorInput)getEditorInput();
         m_cacheResults = Plugin.getDefault().getPreferenceStore().getBoolean(
                 Constants.PREF_KEY_CACHE_TEST_RESULTS);
         if (!m_cacheResults) {
             m_session = Persistor.instance().openSession();
         }
         m_viewer = new TreeViewer(parent);
         m_viewer.setContentProvider(new TestResultTreeViewContentProvider());
         DecoratingLabelProvider labelProvider = new DecoratingLabelProvider(
             new TestResultTreeViewLabelProvider(), 
             PlatformUI.getWorkbench()
                 .getDecoratorManager().getLabelDecorator());
         IDecorationContext decorationContext = 
             labelProvider.getDecorationContext();
         if (decorationContext instanceof DecorationContext) {
             ((DecorationContext)decorationContext).putProperty(
                     DECORATION_CONTEXT_SUITE_END_TIME_ID, 
                     editorInput.getTestSuiteEndTime());
         }
         m_viewer.setLabelProvider(labelProvider);
 
         getSite().setSelectionProvider(m_viewer);
         createContextMenu(m_viewer);
         m_viewer.setAutoExpandLevel(2);
         PlatformUI.getWorkbench().getHelpSystem().setHelp(m_viewer.getControl(),
                 ContextHelpIds.RESULT_TREE_VIEW);
         try {
             setTestResultRootNode(generateTestResult(
                     editorInput.getTestResultSummaryId(),
                     editorInput.getParentProjectId()));
             m_viewer.setInput(new TestResultNode[] {getTestResultRootNode()});
         } catch (InterruptedException ie) {
             // Operation was cancelled by user
             m_viewer.getControl().dispose();
             m_viewer = null;
             new Label(parent, SWT.NONE).setText(
                     Messages.EditorsOpenEditorOperationCanceled);
         }
     }
 
     /**
      * @param viewer the tree viewer
      */
     private void createContextMenu(TreeViewer viewer) {
         // Create menu manager.
         MenuManager menuMgr = new MenuManager();
         menuMgr.setRemoveAllWhenShown(true);
         menuMgr.addMenuListener(new IMenuListener() {
             public void menuAboutToShow(IMenuManager mgr) {
                 fillContextMenu(mgr);
             }
         });
         // Create menu.
         Control viewerControl = viewer.getControl();
         Menu menu = menuMgr.createContextMenu(viewerControl);
         viewerControl.setMenu(menu);
         // Register menu for extension.
         getSite().registerContextMenu(menuMgr, getTreeViewer());
     }
     
     /**
      * @param mgr the menu manager
      */
     private void fillContextMenu(IMenuManager mgr) {
         CommandHelper.createContributionPushItem(mgr,
                         CommandIDs.EXPAND_TREE_ITEM_COMMAND_ID);
         mgr.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     public void setFocus() {
         if (m_viewer != null && !m_viewer.getControl().isDisposed()) {
             m_viewer.getControl().setFocus();
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void addSelectionChangedListener(
         ISelectionChangedListener listener) {
         m_viewer.addSelectionChangedListener(listener);
     }
 
     /**
      * {@inheritDoc}
      */
     public ISelection getSelection() {
         return m_viewer.getSelection();
     }
 
     /**
      * {@inheritDoc}
      */
     public void removeSelectionChangedListener(
             ISelectionChangedListener listener) {
         m_viewer.removeSelectionChangedListener(listener);
     }
 
     /**
      * {@inheritDoc}
      */
     public void setSelection(ISelection selection) {
         m_viewer.setSelection(selection);
     }
 
     /**
      * {@inheritDoc}
      */
     public TreeViewer getTreeViewer() {
         return m_viewer;
     }
     
     @Override
     public void dispose() {
         super.dispose();
         Persistor.instance().dropSession(m_session);
     }
 
     /**
      * @return the testResultRootNode
      */
     public TestResultNode getTestResultRootNode() {
         return m_testResultRootNode;
     }
 
     /**
      * @param testResultRootNode the testResultRootNode to set
      */
     private void setTestResultRootNode(TestResultNode testResultRootNode) {
         m_testResultRootNode = testResultRootNode;
     }
 }
