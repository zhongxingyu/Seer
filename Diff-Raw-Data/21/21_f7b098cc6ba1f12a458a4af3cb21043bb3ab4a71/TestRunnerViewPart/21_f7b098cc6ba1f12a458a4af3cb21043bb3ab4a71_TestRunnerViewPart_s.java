 /*******************************************************************************
  * Copyright (c) 2000, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *     Julien Ruaux: jruaux@octo.com see bug 25324 Ability to know when tests are finished [junit] 
  *     Vincent Massol: vmassol@octo.com 25324 Ability to know when tests are finished [junit]
  *     Sebastian Davids: sdavids@gmx.de 35762 JUnit View wasting a lot of screen space [JUnit]
  *******************************************************************************/
 package org.eclipse.dltk.internal.testing.ui;
 
 import java.io.File;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.ILock;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
 import org.eclipse.debug.ui.DebugUITools;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.ElementChangedEvent;
 import org.eclipse.dltk.core.IElementChangedListener;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.IModelElementDelta;
 import org.eclipse.dltk.core.IScriptProject;
 import org.eclipse.dltk.internal.testing.Messages;
 import org.eclipse.dltk.internal.testing.launcher.NullTestRunnerUI;
 import org.eclipse.dltk.internal.testing.model.DLTKTestingModel;
 import org.eclipse.dltk.internal.testing.model.ITestRunSessionListener;
 import org.eclipse.dltk.internal.testing.model.ITestSessionListener;
 import org.eclipse.dltk.internal.testing.model.TestCaseElement;
 import org.eclipse.dltk.internal.testing.model.TestElement;
 import org.eclipse.dltk.internal.testing.model.TestRunSession;
 import org.eclipse.dltk.testing.DLTKTestingConstants;
 import org.eclipse.dltk.testing.DLTKTestingMessages;
 import org.eclipse.dltk.testing.DLTKTestingPlugin;
 import org.eclipse.dltk.testing.ITestRunnerUI;
 import org.eclipse.dltk.testing.ITestSession;
 import org.eclipse.dltk.testing.model.ITestElement;
 import org.eclipse.dltk.testing.model.ITestRunSession;
 import org.eclipse.dltk.testing.model.ITestElement.Result;
 import org.eclipse.dltk.ui.viewsupport.ViewHistory;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.IMenuListener;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.IStatusLineManager;
 import org.eclipse.jface.action.IToolBarManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CLabel;
 import org.eclipse.swt.custom.SashForm;
 import org.eclipse.swt.custom.ViewForm;
 import org.eclipse.swt.dnd.Clipboard;
 import org.eclipse.swt.events.ControlEvent;
 import org.eclipse.swt.events.ControlListener;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Layout;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.ToolBar;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.IEditorActionBarContributor;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IMemento;
 import org.eclipse.ui.IPartListener2;
 import org.eclipse.ui.IViewPart;
 import org.eclipse.ui.IViewSite;
 import org.eclipse.ui.IWorkbenchActionConstants;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.IWorkbenchPartReference;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.actions.ActionFactory;
 import org.eclipse.ui.handlers.IHandlerActivation;
 import org.eclipse.ui.handlers.IHandlerService;
 import org.eclipse.ui.part.EditorActionBarContributor;
 import org.eclipse.ui.part.ViewPart;
 import org.eclipse.ui.progress.IWorkbenchSiteProgressService;
 import org.eclipse.ui.progress.UIJob;
 
 import com.ibm.icu.text.DateFormat;
 import com.ibm.icu.text.NumberFormat;
 import com.ibm.icu.text.SimpleDateFormat;
 
 /**
  * A ViewPart that shows the results of a test run.
  */
 public class TestRunnerViewPart extends ViewPart {
 
 	public static final String NAME = "org.eclipse.dltk.testing.ResultView"; //$NON-NLS-1$
 
 	private static final String RERUN_LAST_COMMAND = "org.eclipse.dltk.testing.testingShortcut.rerunLast"; //$NON-NLS-1$
 	private static final String RERUN_FAILED_FIRST_COMMAND = "org.eclipse.dltk.testing.testingShortcut.rerunFailedFirst"; //$NON-NLS-1$
 
 	static final int REFRESH_INTERVAL = 200;
 
 	static final int LAYOUT_FLAT = 0;
 	static final int LAYOUT_HIERARCHICAL = 1;
 
 	/**
 	 * Whether the output scrolls and reveals tests as they are executed.
 	 */
 	protected boolean fAutoScroll = true;
 	/**
 	 * The current orientation; either <code>VIEW_ORIENTATION_HORIZONTAL</code>
 	 * <code>VIEW_ORIENTATION_VERTICAL</code>, or
 	 * <code>VIEW_ORIENTATION_AUTOMATIC</code>.
 	 */
 	private int fOrientation = VIEW_ORIENTATION_AUTOMATIC;
 	/**
 	 * The current orientation; either <code>VIEW_ORIENTATION_HORIZONTAL</code>
 	 * <code>VIEW_ORIENTATION_VERTICAL</code>.
 	 */
 	private int fCurrentOrientation;
 	/**
 	 * The current layout mode (LAYOUT_FLAT or LAYOUT_HIERARCHICAL).
 	 */
 	private int fLayout = LAYOUT_HIERARCHICAL;
 
 	// private boolean fTestIsRunning= false;
 
 	protected DLTKTestingProgressBar fProgressBar;
 	protected ProgressImages fProgressImages;
 	protected Image fViewImage;
 	protected CounterPanel fCounterPanel;
 	protected boolean fShowOnErrorOnly = false;
 	protected Clipboard fClipboard;
 	protected volatile String fInfoMessage;
 
 	private FailureTrace fFailureTrace;
 
 	private TestViewer fTestViewer;
 	/**
 	 * Is the UI disposed?
 	 */
 	private boolean fIsDisposed = false;
 
 	/**
 	 * Actions
 	 */
 	private Action fNextAction;
 	private Action fPreviousAction;
 
 	private StopAction fStopAction;
 	private DLTKTestingCopyAction fCopyAction;
 
 	private Action fRerunLastTestAction;
 	private IHandlerActivation fRerunLastActivation;
 	private Action fRerunFailedFirstAction;
 	private IHandlerActivation fRerunFailedFirstActivation;
 
 	private Action fFailuresOnlyFilterAction;
 	private ScrollLockAction fScrollLockAction;
 	private ToggleOrientationAction[] fToggleOrientationActions;
 	private ShowTestHierarchyAction fShowTestHierarchyAction;
 	private ActivateOnErrorAction fActivateOnErrorAction;
 	private IMenuListener fViewMenuListener;
 
 	private TestRunSession fTestRunSession;
 	private TestSessionListener fTestSessionListener;
 
 	private RunnerViewHistory fViewHistory;
 	private TestRunSessionListener fTestRunSessionListener;
 
 	final Image fStackViewIcon = TestRunnerViewPart
 			.createImage("eview16/stackframe.gif");//$NON-NLS-1$
 	final Image fTestRunOKIcon = TestRunnerViewPart
 			.createImage("eview16/testingsucc.gif"); //$NON-NLS-1$
 	final Image fTestRunFailIcon = TestRunnerViewPart
 			.createImage("eview16/testingerr.gif"); //$NON-NLS-1$
 	final Image fTestRunOKDirtyIcon = TestRunnerViewPart
 			.createImage("eview16/testingsuccq.gif"); //$NON-NLS-1$
 	final Image fTestRunFailDirtyIcon = TestRunnerViewPart
 			.createImage("eview16/testingerrq.gif"); //$NON-NLS-1$
 
 	final Image fTestIcon = TestRunnerViewPart.createImage("obj16/test.gif"); //$NON-NLS-1$
 	final Image fTestOkIcon = TestRunnerViewPart
 			.createImage("obj16/testok.gif"); //$NON-NLS-1$
 	final Image fTestErrorIcon = TestRunnerViewPart
 			.createImage("obj16/testerr.gif"); //$NON-NLS-1$
 	final Image fTestFailIcon = TestRunnerViewPart
 			.createImage("obj16/testfail.gif"); //$NON-NLS-1$
 	final Image fTestSkippedIcon = TestRunnerViewPart
 			.createImage("obj16/testskipped.gif"); //$NON-NLS-1$
 	final Image fTestBlockedIcon = TestRunnerViewPart
 			.createImage("obj16/testblocked.gif"); //$NON-NLS-1$
 	final Image fTestAbortedIcon = TestRunnerViewPart
 			.createImage("obj16/testabort.gif"); //$NON-NLS-1$
 	final Image fTestRunningIcon = TestRunnerViewPart
 			.createImage("obj16/testrun.gif"); //$NON-NLS-1$
 	final Image fTestIgnoredIcon = TestRunnerViewPart
 			.createImage("obj16/testignored.gif"); //$NON-NLS-1$
 	final Image fTestUnknownIcon = TestRunnerViewPart
 			.createImage("obj16/testunknown.gif"); //$NON-NLS-1$
 
 	final ImageDescriptor fSuiteIconDescriptor = DLTKTestingPlugin
 			.getImageDescriptor("obj16/tsuite.gif"); //$NON-NLS-1$
 	final ImageDescriptor fSuiteOkIconDescriptor = DLTKTestingPlugin
 			.getImageDescriptor("obj16/tsuiteok.gif"); //$NON-NLS-1$
 	final ImageDescriptor fSuiteErrorIconDescriptor = DLTKTestingPlugin
 			.getImageDescriptor("obj16/tsuiteerror.gif"); //$NON-NLS-1$
 	final ImageDescriptor fSuiteFailIconDescriptor = DLTKTestingPlugin
 			.getImageDescriptor("obj16/tsuitefail.gif"); //$NON-NLS-1$
 	final ImageDescriptor fSuiteRunningIconDescriptor = DLTKTestingPlugin
 			.getImageDescriptor("obj16/tsuiterun.gif"); //$NON-NLS-1$
 
 	final Image fSuiteIcon = fSuiteIconDescriptor.createImage();
 	final Image fSuiteOkIcon = fSuiteOkIconDescriptor.createImage();
 	final Image fSuiteErrorIcon = fSuiteErrorIconDescriptor.createImage();
 	final Image fSuiteFailIcon = fSuiteFailIconDescriptor.createImage();
 
 	final Image fSuiteAbortedIcon = TestRunnerViewPart
 			.createImage("obj16/tsuiteabort.gif"); //$NON-NLS-1$
 	final Image fSuiteSkippedIcon = TestRunnerViewPart
 			.createImage("obj16/tsuiteskipped.gif"); //$NON-NLS-1$
 	final Image fSuiteBlockedIcon = TestRunnerViewPart
 			.createImage("obj16/tsuiteblocked.gif"); //$NON-NLS-1$
 	final Image fSuiteUnknownIcon = TestRunnerViewPart
 			.createImage("obj16/tsuiteunknown.gif"); //$NON-NLS-1$
 
 	final Image fSuiteRunningIcon = fSuiteRunningIconDescriptor.createImage();
 
 	final Image fCategoryIcon = TestRunnerViewPart
 			.createImage("obj16/category.gif"); //$NON-NLS-1$
 	final Image fCategoryOkIcon = TestRunnerViewPart
 			.createImage("obj16/category-ok.gif"); //$NON-NLS-1$
 	final Image fCategoryErrorIcon = TestRunnerViewPart
 			.createImage("obj16/category-error.gif"); //$NON-NLS-1$
 	final Image fCategoryFailIcon = TestRunnerViewPart
 			.createImage("obj16/category-fail.gif"); //$NON-NLS-1$
 	final Image fCategoryAbortedIcon = TestRunnerViewPart
 			.createImage("obj16/category-abort.gif"); //$NON-NLS-1$
 	final Image fCategorySkippedIcon = TestRunnerViewPart
 			.createImage("obj16/category-skipped.gif"); //$NON-NLS-1$
 	final Image fCategoryBlockedIcon = TestRunnerViewPart
 			.createImage("obj16/category-blocked.gif"); //$NON-NLS-1$
 	final Image fCategoryUnknownIcon = TestRunnerViewPart
 			.createImage("obj16/category-unknown.gif"); //$NON-NLS-1$
 	final Image fCategoryRunningIcon = TestRunnerViewPart
 			.createImage("obj16/category-run.gif"); //$NON-NLS-1$
 
 	// Persistence tags.
 	static final String TAG_PAGE = "page"; //$NON-NLS-1$
 	static final String TAG_RATIO = "ratio"; //$NON-NLS-1$
 	static final String TAG_TRACEFILTER = "tracefilter"; //$NON-NLS-1$ 
 	static final String TAG_ORIENTATION = "orientation"; //$NON-NLS-1$
 	static final String TAG_SCROLL = "scroll"; //$NON-NLS-1$
 	/**
 	 * @since 3.2
 	 */
 	static final String TAG_LAYOUT = "layout"; //$NON-NLS-1$
 	/**
 	 * @since 3.2
 	 */
 	static final String TAG_FAILURES_ONLY = "failuresOnly"; //$NON-NLS-1$
 
 	// orientations
 	static final int VIEW_ORIENTATION_VERTICAL = 0;
 	static final int VIEW_ORIENTATION_HORIZONTAL = 1;
 	static final int VIEW_ORIENTATION_AUTOMATIC = 2;
 
 	private IMemento fMemento;
 
 	Image fOriginalViewImage;
 	IElementChangedListener fDirtyListener;
 
 	// private CTabFolder fTabFolder;
 	private SashForm fSashForm;
 
 	private Composite fCounterComposite;
 	private Composite fParent;
 
 	/**
 	 * A Job that periodically updates view description, counters, and progress
 	 * bar.
 	 */
 	private UpdateUIJob fUpdateJob;
 
 	/**
 	 * A Job that runs as long as a test run is running. It is used to show
 	 * busyness for running jobs in the view (title in italics).
 	 */
 	private JUnitIsRunningJob fJUnitIsRunningJob;
 	private ILock fJUnitIsRunningLock;
 	public static final Object FAMILY_JUNIT_RUN = new Object();
 
 	private IPartListener2 fPartListener = new IPartListener2() {
 		public void partActivated(IWorkbenchPartReference ref) {
 		}
 
 		public void partBroughtToTop(IWorkbenchPartReference ref) {
 		}
 
 		public void partInputChanged(IWorkbenchPartReference ref) {
 		}
 
 		public void partClosed(IWorkbenchPartReference ref) {
 		}
 
 		public void partDeactivated(IWorkbenchPartReference ref) {
 		}
 
 		public void partOpened(IWorkbenchPartReference ref) {
 		}
 
 		public void partVisible(IWorkbenchPartReference ref) {
 			if (getSite().getId().equals(ref.getId())) {
 				fPartIsVisible = true;
 			}
 		}
 
 		public void partHidden(IWorkbenchPartReference ref) {
 			if (getSite().getId().equals(ref.getId())) {
 				fPartIsVisible = false;
 			}
 		}
 	};
 
 	protected boolean fPartIsVisible = false;
 
 	private class RunnerViewHistory extends ViewHistory {
 
 		public void configureHistoryListAction(IAction action) {
 			action.setText(DLTKTestingMessages.TestRunnerViewPart_history);
 		}
 
 		public void configureHistoryDropDownAction(IAction action) {
 			action
 					.setToolTipText(DLTKTestingMessages.TestRunnerViewPart_test_run_history);
 			DLTKTestingPlugin.setLocalImageDescriptors(action,
 					"history_list.gif"); //$NON-NLS-1$
 		}
 
 		public Action getClearAction() {
 			return new ClearAction();
 		}
 
 		public String getHistoryListDialogTitle() {
 			return DLTKTestingMessages.TestRunnerViewPart_test_runs;
 		}
 
 		public String getHistoryListDialogMessage() {
 			return DLTKTestingMessages.TestRunnerViewPart_select_test_run;
 		}
 
 		public Shell getShell() {
 			return fParent.getShell();
 		}
 
 		public List getHistoryEntries() {
 			return DLTKTestingPlugin.getModel().getTestRunSessions();
 		}
 
 		public Object getCurrentEntry() {
 			return fTestRunSession;
 		}
 
 		public void setActiveEntry(Object entry) {
 			TestRunSession deactivatedSession = setActiveTestRunSession((TestRunSession) entry);
 			if (deactivatedSession != null)
 				deactivatedSession.swapOut();
 		}
 
 		public void setHistoryEntries(List remainingEntries, Object activeEntry) {
 			setActiveTestRunSession((TestRunSession) activeEntry);
 
 			List testRunSessions = DLTKTestingPlugin.getModel()
 					.getTestRunSessions();
 			testRunSessions.removeAll(remainingEntries);
 			for (Iterator iter = testRunSessions.iterator(); iter.hasNext();) {
 				DLTKTestingPlugin.getModel().removeTestRunSession(
 						(TestRunSession) iter.next());
 			}
 			for (Iterator iter = remainingEntries.iterator(); iter.hasNext();) {
 				TestRunSession remaining = (TestRunSession) iter.next();
 				remaining.swapOut();
 			}
 		}
 
 		public ImageDescriptor getImageDescriptor(Object element) {
 			TestRunSession session = (TestRunSession) element;
 			if (session.isStopped())
 				return fSuiteIconDescriptor;
 
 			if (session.isRunning())
 				return fSuiteRunningIconDescriptor;
 
 			Result result = session.getTestResult(true);
 			if (result == Result.OK)
 				return fSuiteOkIconDescriptor;
 			else if (result == Result.ERROR)
 				return fSuiteErrorIconDescriptor;
 			else if (result == Result.FAILURE)
 				return fSuiteFailIconDescriptor;
 			else
 				return fSuiteIconDescriptor;
 		}
 
 		public String getText(Object element) {
 			TestRunSession session = (TestRunSession) element;
 			if (session.getStartTime() == 0) {
 				return session.getTestRunName();
 			} else {
 				String startTime = DateFormat.getDateTimeInstance().format(
 						new Date(session.getStartTime()));
 				return Messages
 						.format(
 								DLTKTestingMessages.TestRunnerViewPart_testName_startTime,
 								new Object[] { session.getTestRunName(),
 										startTime });
 			}
 		}
 
 		public void addMenuEntries(MenuManager manager) {
 			manager.appendToGroup(IWorkbenchActionConstants.MB_ADDITIONS,
 					new ImportTestRunSessionAction(fParent.getShell()));
 			if (fTestRunSession != null)
 				manager.appendToGroup(IWorkbenchActionConstants.MB_ADDITIONS,
 						new ExportTestRunSessionAction(fParent.getShell(),
 								fTestRunSession));
 		}
 
 		public String getMaxEntriesMessage() {
 			return DLTKTestingMessages.TestRunnerViewPart_max_remembered;
 		}
 
 		public int getMaxEntries() {
 			IPreferenceStore store = DLTKTestingPlugin.getDefault()
 					.getPreferenceStore();
 			return store.getInt(DLTKTestingPreferencesConstants.MAX_TEST_RUNS);
 		}
 
 		public void setMaxEntries(int maxEntries) {
 			IPreferenceStore store = DLTKTestingPlugin.getDefault()
 					.getPreferenceStore();
 			store.setValue(DLTKTestingPreferencesConstants.MAX_TEST_RUNS,
 					maxEntries);
 		}
 	}
 
 	private static class ImportTestRunSessionAction extends Action {
 		private final Shell fShell;
 
 		public ImportTestRunSessionAction(Shell shell) {
 			super(
 					DLTKTestingMessages.TestRunnerViewPart_ImportTestRunSessionAction_name);
 			fShell = shell;
 		}
 
 		public void run() {
 			FileDialog importDialog = new FileDialog(fShell, SWT.OPEN);
 			importDialog
 					.setText(DLTKTestingMessages.TestRunnerViewPart_ImportTestRunSessionAction_title);
 			importDialog.setFilterExtensions(new String[] { "*.xml", "*.*" }); //$NON-NLS-1$ //$NON-NLS-2$
 			String path = importDialog.open();
 			if (path == null)
 				return;
 
 			// TODO: MULTI: getFileNames()
 			File file = new File(path);
 
 			try {
 				DLTKTestingModel.importTestRunSession(file);
 			} catch (CoreException e) {
 				DLTKTestingPlugin.log(e);
 				ErrorDialog
 						.openError(
 								fShell,
 								DLTKTestingMessages.TestRunnerViewPart_ImportTestRunSessionAction_error_title,
 								e.getStatus().getMessage(), e.getStatus());
 			}
 		}
 	}
 
 	private static class ExportTestRunSessionAction extends Action {
 		private final TestRunSession fTestRunSession;
 		private final Shell fShell;
 
 		public ExportTestRunSessionAction(Shell shell,
 				TestRunSession testRunSession) {
 			super(
 					DLTKTestingMessages.TestRunnerViewPart_ExportTestRunSessionAction_name);
 			fShell = shell;
 			fTestRunSession = testRunSession;
 		}
 
 		public void run() {
 			FileDialog exportDialog = new FileDialog(fShell, SWT.SAVE);
 			exportDialog
 					.setText(DLTKTestingMessages.TestRunnerViewPart_ExportTestRunSessionAction_title);
 			exportDialog.setFileName(getFileName());
 			exportDialog.setFilterExtensions(new String[] { "*.xml", "*.*" }); //$NON-NLS-1$ //$NON-NLS-2$
 			String path = exportDialog.open();
 			if (path == null)
 				return;
 
 			// TODO: MULTI: getFileNames()
 			File file = new File(path);
 
 			try {
 				DLTKTestingModel.exportTestRunSession(fTestRunSession, file);
 			} catch (CoreException e) {
 				DLTKTestingPlugin.log(e);
 				ErrorDialog
 						.openError(
 								fShell,
 								DLTKTestingMessages.TestRunnerViewPart_ExportTestRunSessionAction_error_title,
 								e.getStatus().getMessage(), e.getStatus());
 			}
 		}
 
 		private String getFileName() {
 			String testRunName = fTestRunSession.getTestRunName();
 			long startTime = fTestRunSession.getStartTime();
 			if (startTime == 0)
 				return testRunName;
 
 			String isoTime = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date(startTime)); //$NON-NLS-1$
 			return testRunName + " " + isoTime + ".xml"; //$NON-NLS-1$ //$NON-NLS-2$
 		}
 	}
 
 	private class TestRunSessionListener implements ITestRunSessionListener {
 		public void sessionAdded(TestRunSession testRunSession) {
 			if (getSite().getWorkbenchWindow() == DLTKTestingPlugin
 					.getActiveWorkbenchWindow()) {
 				TestRunSession deactivatedSession = setActiveTestRunSession(testRunSession);
 				if (deactivatedSession != null)
 					deactivatedSession.swapOut();
 				String testRunName = fTestRunSession.getTestRunName();
 				String msg;
 				if (testRunSession.getLaunch() != null) {
 					msg = Messages.format(
 							DLTKTestingMessages.TestRunnerViewPart_Launching,
 							new Object[] { testRunName });
 				} else {
 					msg = testRunName;
 				}
 				setContentDescription(msg);
 			}
 		}
 
 		public void sessionRemoved(TestRunSession testRunSession) {
 			if (testRunSession.equals(fTestRunSession)) {
 				List testRunSessions = DLTKTestingPlugin.getModel()
 						.getTestRunSessions();
 				TestRunSession deactivatedSession;
 				if (!testRunSessions.isEmpty()) {
 					deactivatedSession = setActiveTestRunSession((TestRunSession) testRunSessions
 							.get(0));
 				} else {
 					deactivatedSession = setActiveTestRunSession(null);
 				}
 				if (deactivatedSession != null)
 					deactivatedSession.swapOut();
 			}
 		}
 	}
 
 	private class TestSessionListener implements ITestSessionListener {
 		public void sessionStarted() {
 			fTestViewer.registerViewersRefresh();
 			fShowOnErrorOnly = getShowOnErrorOnly();
 
 			startUpdateJobs();
 
 			fStopAction.setEnabled(true);
 			fRerunLastTestAction.setEnabled(true);
 		}
 
 		public void sessionEnded(long elapsedTime) {
 			fTestViewer.registerAutoScrollTarget(null);
 
 			String[] keys = { elapsedTimeAsString(elapsedTime) };
 			String msg = Messages
 					.format(
 							DLTKTestingMessages.TestRunnerViewPart_message_finish,
 							keys);
 			registerInfoMessage(msg);
 
 			postSyncRunnable(new Runnable() {
 				public void run() {
 					if (isDisposed())
 						return;
 					fStopAction.setEnabled(lastLaunchIsKeptAlive());
 					updateRerunFailedFirstAction();
 					processChangesInUI();
 					if (hasErrorsOrFailures()) {
 						selectFirstFailure();
 					}
 					if (fDirtyListener == null) {
 						fDirtyListener = new DirtyListener();
 						DLTKCore.addElementChangedListener(fDirtyListener);
 					}
 					warnOfContentChange();
 				}
 			});
 			stopUpdateJobs();
 		}
 
 		public void sessionStopped(final long elapsedTime) {
 			fTestViewer.registerAutoScrollTarget(null);
 
 			registerInfoMessage(DLTKTestingMessages.TestRunnerViewPart_message_stopped);
 			handleStopped();
 		}
 
 		public void sessionTerminated() {
 			fTestViewer.registerAutoScrollTarget(null);
 
 			registerInfoMessage(DLTKTestingMessages.TestRunnerViewPart_message_terminated);
 			handleStopped();
 		}
 
 		public void runningBegins() {
 			if (!fShowOnErrorOnly)
 				postShowTestResultsView();
 		}
 
 		public void testStarted(TestCaseElement testCaseElement) {
 			fTestViewer.registerAutoScrollTarget(testCaseElement);
 			fTestViewer.registerViewerUpdate(testCaseElement);
 			registerInfoMessage(getTestRunnerUI().getTestStartedMessage(
 					testCaseElement));
 		}
 
 		public void testFailed(TestElement testElement,
 				TestElement.Status status, String trace, String expected,
 				String actual, int code) {
 			if (isAutoScroll()) {
 				fTestViewer.registerFailedForAutoScroll(testElement);
 			}
 			fTestViewer.registerViewerUpdate(testElement);
 
 			// show the view on the first error only
 			if (fShowOnErrorOnly && (getErrorsPlusFailures() == 1))
 				postShowTestResultsView();
 
 			// TODO:
 			// [Bug 35590] JUnit window doesn't report errors from
 			// junit.extensions.TestSetup [JUnit]
 			// when a failure occurs in test setup then no test is running
 			// to update the views we artificially signal the end of a test run
 			// if (!fTestIsRunning) {
 			// fTestIsRunning= false;
 			// testEnded(testCaseElement);
 			// }
 		}
 
 		public void testEnded(TestCaseElement testCaseElement) {
 			fTestViewer.registerViewerUpdate(testCaseElement);
 		}
 
 		public void testReran(TestCaseElement testCaseElement,
 				TestElement.Status status, String trace, String expectedResult,
 				String actualResult) {
 			fTestViewer.registerViewerUpdate(testCaseElement); // TODO:
 			// autoExpand?
 			postSyncProcessChanges();
 			showFailure(testCaseElement);
 		}
 
 		public void testAdded(TestElement testElement) {
 			fTestViewer.registerTestAdded(testElement);
 		}
 
 		public boolean acceptsSwapToDisk() {
 			return false;
 		}
 	}
 
 	private class UpdateUIJob extends UIJob {
 		private boolean fRunning = true;
 
 		public UpdateUIJob(String name) {
 			super(name);
 			setSystem(true);
 		}
 
 		public IStatus runInUIThread(IProgressMonitor monitor) {
 			if (!isDisposed()) {
 				processChangesInUI();
 			}
 			schedule(REFRESH_INTERVAL);
 			return Status.OK_STATUS;
 		}
 
 		public void stop() {
 			fRunning = false;
 		}
 
 		public boolean shouldSchedule() {
 			return fRunning;
 		}
 	}
 
 	private class JUnitIsRunningJob extends Job {
 		public JUnitIsRunningJob(String name) {
 			super(name);
 			setSystem(true);
 		}
 
 		public IStatus run(IProgressMonitor monitor) {
 			// wait until the test run terminates
 			fJUnitIsRunningLock.acquire();
 			return Status.OK_STATUS;
 		}
 
 		public boolean belongsTo(Object family) {
 			return family == TestRunnerViewPart.FAMILY_JUNIT_RUN;
 		}
 	}
 
 	private class ClearAction extends Action {
 		public ClearAction() {
 			setText(DLTKTestingMessages.TestRunnerViewPart_clear_history_label);
 
 			boolean enabled = false;
 			List testRunSessions = DLTKTestingPlugin.getModel()
 					.getTestRunSessions();
 			for (Iterator iter = testRunSessions.iterator(); iter.hasNext();) {
 				ITestSession testRunSession = (ITestSession) iter.next();
 				if (!testRunSession.isRunning() && !testRunSession.isStarting()) {
 					enabled = true;
 					break;
 				}
 			}
 			setEnabled(enabled);
 		}
 
 		public void run() {
 			List testRunSessions = getRunningSessions();
 			Object first = testRunSessions.isEmpty() ? null : testRunSessions
 					.get(0);
 			fViewHistory.setHistoryEntries(testRunSessions, first);
 		}
 
 		private List getRunningSessions() {
 			List testRunSessions = DLTKTestingPlugin.getModel()
 					.getTestRunSessions();
 			for (Iterator iter = testRunSessions.iterator(); iter.hasNext();) {
 				ITestSession testRunSession = (ITestSession) iter.next();
 				if (!testRunSession.isRunning() && !testRunSession.isStarting()) {
 					iter.remove();
 				}
 			}
 			return testRunSessions;
 		}
 	}
 
 	private class StopAction extends Action {
 		public StopAction() {
 			setText(DLTKTestingMessages.TestRunnerViewPart_stopaction_text);
 			setToolTipText(DLTKTestingMessages.TestRunnerViewPart_stopaction_tooltip);
 			DLTKTestingPlugin.setLocalImageDescriptors(this, "stop.gif"); //$NON-NLS-1$
 		}
 
 		public void run() {
 			stopTest();
 			setEnabled(false);
 		}
 	}
 
 	private class RerunLastAction extends Action {
 		public RerunLastAction() {
 			setText(DLTKTestingMessages.TestRunnerViewPart_rerunaction_label);
 			setToolTipText(DLTKTestingMessages.TestRunnerViewPart_rerunaction_tooltip);
 			DLTKTestingPlugin.setLocalImageDescriptors(this, "relaunch.gif"); //$NON-NLS-1$
 			setEnabled(false);
 			setActionDefinitionId(RERUN_LAST_COMMAND);
 		}
 
 		public void run() {
 			rerunTestRun();
 		}
 	}
 
 	private class RerunLastFailedFirstAction extends Action {
 		public RerunLastFailedFirstAction() {
 			setText(DLTKTestingMessages.TestRunnerViewPart_rerunfailuresaction_label);
 			setToolTipText(DLTKTestingMessages.TestRunnerViewPart_rerunfailuresaction_tooltip);
 			DLTKTestingPlugin.setLocalImageDescriptors(this, "relaunchf.gif"); //$NON-NLS-1$
 			setEnabled(false);
 			setActionDefinitionId(RERUN_FAILED_FIRST_COMMAND);
 		}
 
 		public void run() {
 			rerunTestFailedFirst();
 		}
 	}
 
 	private static class ActionHandlerWrapper extends AbstractHandler {
 
 		public ActionHandlerWrapper(Action action) {
 			this.action = action;
 		}
 
 		private final Action action;
 
 		public Object execute(ExecutionEvent event) throws ExecutionException {
 			action.run();
 			return null;
 		}
 
 		public boolean isEnabled() {
 			return action.isEnabled();
 		}
 
 	}
 
 	private class ToggleOrientationAction extends Action {
 		private final int fActionOrientation;
 
 		public ToggleOrientationAction(int orientation) {
 			super("", AS_RADIO_BUTTON); //$NON-NLS-1$
 			if (orientation == TestRunnerViewPart.VIEW_ORIENTATION_HORIZONTAL) {
 				setText(DLTKTestingMessages.TestRunnerViewPart_toggle_horizontal_label);
 				setImageDescriptor(DLTKTestingPlugin
 						.getImageDescriptor("elcl16/th_horizontal.gif")); //$NON-NLS-1$				
 			} else if (orientation == TestRunnerViewPart.VIEW_ORIENTATION_VERTICAL) {
 				setText(DLTKTestingMessages.TestRunnerViewPart_toggle_vertical_label);
 				setImageDescriptor(DLTKTestingPlugin
 						.getImageDescriptor("elcl16/th_vertical.gif")); //$NON-NLS-1$				
 			} else if (orientation == TestRunnerViewPart.VIEW_ORIENTATION_AUTOMATIC) {
 				setText(DLTKTestingMessages.TestRunnerViewPart_toggle_automatic_label);
 				setImageDescriptor(DLTKTestingPlugin
 						.getImageDescriptor("elcl16/th_automatic.gif")); //$NON-NLS-1$				
 			}
 			fActionOrientation = orientation;
 			PlatformUI
 					.getWorkbench()
 					.getHelpSystem()
 					.setHelp(
 							this,
 							IDLTKTestingHelpContextIds.RESULTS_VIEW_TOGGLE_ORIENTATION_ACTION);
 		}
 
 		public int getOrientation() {
 			return fActionOrientation;
 		}
 
 		public void run() {
 			if (isChecked()) {
 				fOrientation = fActionOrientation;
 				computeOrientation();
 			}
 		}
 	}
 
 	/**
 	 * Listen for for modifications to Java elements
 	 */
 	private class DirtyListener implements IElementChangedListener {
 		public void elementChanged(ElementChangedEvent event) {
 			processDelta(event.getDelta());
 		}
 
 		private boolean processDelta(IModelElementDelta delta) {
 			int kind = delta.getKind();
 			int details = delta.getFlags();
 			int type = delta.getElement().getElementType();
 
 			switch (type) {
 			// Consider containers for class files.
 			case IModelElement.SCRIPT_MODEL:
 			case IModelElement.SCRIPT_PROJECT:
 			case IModelElement.PROJECT_FRAGMENT:
 			case IModelElement.SCRIPT_FOLDER:
 				// If we did something different than changing a child we flush
 				// the undo / redo stack.
 				if (kind != IModelElementDelta.CHANGED
 						|| details != IModelElementDelta.F_CHILDREN) {
 					codeHasChanged();
 					return false;
 				}
 				break;
 			case IModelElement.SOURCE_MODULE:
 				// if we have changed a primary working copy (e.g created,
 				// removed, ...)
 				// then we do nothing.
 				if ((details & IModelElementDelta.F_PRIMARY_WORKING_COPY) != 0)
 					return true;
 				codeHasChanged();
 				return false;
 			default:
 				codeHasChanged();
 				return false;
 			}
 
 			IModelElementDelta[] affectedChildren = delta.getAffectedChildren();
 			if (affectedChildren == null)
 				return true;
 
 			for (int i = 0; i < affectedChildren.length; i++) {
 				if (!processDelta(affectedChildren[i]))
 					return false;
 			}
 			return true;
 		}
 	}
 
 	private class FailuresOnlyFilterAction extends Action {
 		public FailuresOnlyFilterAction() {
 			super(DLTKTestingMessages.TestRunnerViewPart_show_failures_only,
 					AS_CHECK_BOX);
 			setToolTipText(DLTKTestingMessages.TestRunnerViewPart_show_failures_only);
 			setImageDescriptor(DLTKTestingPlugin
 					.getImageDescriptor("obj16/failures.gif")); //$NON-NLS-1$
 		}
 
 		public void run() {
 			setShowFailuresOnly(isChecked());
 		}
 	}
 
 	private class ShowTestHierarchyAction extends Action {
 
 		public ShowTestHierarchyAction() {
 			super(DLTKTestingMessages.TestRunnerViewPart_hierarchical_layout,
 					IAction.AS_CHECK_BOX);
 			setImageDescriptor(DLTKTestingPlugin
 					.getImageDescriptor("elcl16/hierarchicalLayout.gif")); //$NON-NLS-1$
 		}
 
 		public void run() {
 			int mode = isChecked() ? LAYOUT_HIERARCHICAL : LAYOUT_FLAT;
 			setLayoutMode(mode);
 		}
 	}
 
 	private class ActivateOnErrorAction extends Action {
 		public ActivateOnErrorAction() {
 			super(
 					DLTKTestingMessages.TestRunnerViewPart_activate_on_failure_only,
 					IAction.AS_CHECK_BOX);
 			//setImageDescriptor(JUnitPlugin.getImageDescriptor("obj16/failures.gif")); //$NON-NLS-1$
 			update();
 		}
 
 		public void update() {
 			setChecked(getShowOnErrorOnly());
 		}
 
 		public void run() {
 			boolean checked = isChecked();
 			fShowOnErrorOnly = checked;
 			IPreferenceStore store = DLTKTestingPlugin.getDefault()
 					.getPreferenceStore();
 			store.setValue(DLTKTestingPreferencesConstants.SHOW_ON_ERROR_ONLY,
 					checked);
 		}
 	}
 
 	public void init(IViewSite site, IMemento memento) throws PartInitException {
 		super.init(site, memento);
 		fMemento = memento;
 		IWorkbenchSiteProgressService progressService = getProgressService();
 		if (progressService != null)
 			progressService
 					.showBusyForFamily(TestRunnerViewPart.FAMILY_JUNIT_RUN);
 	}
 
 	private IWorkbenchSiteProgressService getProgressService() {
 		Object siteService = getSite().getAdapter(
 				IWorkbenchSiteProgressService.class);
 		if (siteService != null)
 			return (IWorkbenchSiteProgressService) siteService;
 		return null;
 	}
 
 	public void saveState(IMemento memento) {
 		if (fSashForm == null) {
 			// part has not been created
 			if (fMemento != null) // Keep the old state;
 				memento.putMemento(fMemento);
 			return;
 		}
 
 		// int activePage= fTabFolder.getSelectionIndex();
 		// memento.putInteger(TAG_PAGE, activePage);
 		memento.putString(TAG_SCROLL,
 				fScrollLockAction.isChecked() ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
 		int weigths[] = fSashForm.getWeights();
 		int ratio = (weigths[0] * 1000) / (weigths[0] + weigths[1]);
 		memento.putInteger(TAG_RATIO, ratio);
 		memento.putInteger(TAG_ORIENTATION, fOrientation);
 
 		memento.putString(TAG_FAILURES_ONLY, fFailuresOnlyFilterAction
 				.isChecked() ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
 		memento.putInteger(TAG_LAYOUT, fLayout);
 	}
 
 	private void restoreLayoutState(IMemento memento) {
 		// Integer page= memento.getInteger(TAG_PAGE);
 		// if (page != null) {
 		// int p= page.intValue();
 		// if (p < fTestRunTabs.size()) { // tab count can decrease if a
 		// contributing plug-in is removed
 		// fTabFolder.setSelection(p);
 		// fActiveRunTab= (TestRunTab)fTestRunTabs.get(p);
 		// }
 		// }
 		Integer ratio = memento.getInteger(TAG_RATIO);
 		if (ratio != null)
 			fSashForm.setWeights(new int[] { ratio.intValue(),
 					1000 - ratio.intValue() });
 		Integer orientation = memento.getInteger(TAG_ORIENTATION);
 		if (orientation != null)
 			fOrientation = orientation.intValue();
 		computeOrientation();
 		String scrollLock = memento.getString(TAG_SCROLL);
 		if (scrollLock != null) {
 			fScrollLockAction.setChecked(scrollLock.equals("true")); //$NON-NLS-1$
 			setAutoScroll(!fScrollLockAction.isChecked());
 		}
 
 		Integer layout = memento.getInteger(TAG_LAYOUT);
 		int layoutValue = LAYOUT_HIERARCHICAL;
 		if (layout != null)
 			layoutValue = layout.intValue();
 
 		String failuresOnly = memento.getString(TAG_FAILURES_ONLY);
 		boolean showFailuresOnly = false;
 		if (failuresOnly != null)
 			showFailuresOnly = failuresOnly.equals("true"); //$NON-NLS-1$
 
 		setFilterAndLayout(showFailuresOnly, layoutValue);
 	}
 
 	/**
 	 * Stops the currently running test and shuts down the RemoteTestRunner
 	 */
 	public void stopTest() {
 		if (fTestRunSession != null) {
 			if (fTestRunSession.isRunning()) {
 				setContentDescription(DLTKTestingMessages.TestRunnerViewPart_message_stopping);
 			}
 			fTestRunSession.stopTestRun();
 		}
 	}
 
 	private void startUpdateJobs() {
 		postSyncProcessChanges();
 
 		if (fUpdateJob != null) {
 			return;
 		}
 		fJUnitIsRunningJob = new JUnitIsRunningJob(
 				DLTKTestingMessages.TestRunnerViewPart_wrapperJobName);
 		fJUnitIsRunningLock = Job.getJobManager().newLock();
 		// acquire lock while a test run is running
 		// the lock is released when the test run terminates
 		// the wrapper job will wait on this lock.
 		fJUnitIsRunningLock.acquire();
 		getProgressService().schedule(fJUnitIsRunningJob);
 
 		fUpdateJob = new UpdateUIJob(
 				DLTKTestingMessages.TestRunnerViewPart_jobName);
 		fUpdateJob.schedule(REFRESH_INTERVAL);
 	}
 
 	private void stopUpdateJobs() {
 		if (fUpdateJob != null) {
 			fUpdateJob.stop();
 			fUpdateJob = null;
 		}
 		if (fJUnitIsRunningJob != null && fJUnitIsRunningLock != null) {
 			fJUnitIsRunningLock.release();
 			fJUnitIsRunningJob = null;
 		}
 		postSyncProcessChanges();
 	}
 
 	private void processChangesInUI() {
 		if (fSashForm.isDisposed())
 			return;
 
 		doShowInfoMessage();
 		refreshCounters();
 
 		if (!fPartIsVisible)
 			updateViewTitleProgress();
 		else {
 			updateViewIcon();
 		}
 		boolean hasErrorsOrFailures = hasErrorsOrFailures();
 		fNextAction.setEnabled(hasErrorsOrFailures);
 		fPreviousAction.setEnabled(hasErrorsOrFailures);
 
 		fTestViewer.processChangesInUI();
 	}
 
 	/**
 	 * Stops the currently running test and shuts down the RemoteTestRunner
 	 */
 	public void rerunTestRun() {
 		if (lastLaunchIsKeptAlive()) {
 			// prompt for terminating the existing run
 			if (MessageDialog.openQuestion(getSite().getShell(),
 					DLTKTestingMessages.TestRunnerViewPart_terminate_title,
 					DLTKTestingMessages.TestRunnerViewPart_terminate_message)) {
 				stopTest(); // TODO: wait for termination
 			}
 		}
 
 		if (fTestRunSession == null)
 			return;
 		ILaunch launch = fTestRunSession.getLaunch();
 		if (launch == null)
 			return;
 		ILaunchConfiguration launchConfiguration = launch
 				.getLaunchConfiguration();
 		if (launchConfiguration == null)
 			return;
 
 		ILaunchConfiguration configuration = prepareLaunchConfigForRelaunch(launchConfiguration);
 		relaunch(configuration, launch.getLaunchMode());
 	}
 
 	private ILaunchConfiguration prepareLaunchConfigForRelaunch(
 			ILaunchConfiguration configuration) {
 		try {
 			String attribute = configuration.getAttribute(
 					DLTKTestingConstants.ATTR_FAILURES_NAMES, ""); //$NON-NLS-1$
 			if (attribute.length() != 0) {
 				String configName = Messages.format(
 						DLTKTestingMessages.TestRunnerViewPart_configName,
 						configuration.getName());
 				ILaunchConfigurationWorkingCopy tmp = configuration
 						.copy(configName);
 				tmp.setAttribute(DLTKTestingConstants.ATTR_FAILURES_NAMES, ""); //$NON-NLS-1$
 				return tmp;
 			}
 		} catch (CoreException e) {
 			// fall through
 		}
 		return configuration;
 	}
 
 	public void rerunTestFailedFirst() {
 		if (lastLaunchIsKeptAlive()) {
 			// prompt for terminating the existing run
 			if (MessageDialog.openQuestion(getSite().getShell(),
 					DLTKTestingMessages.TestRunnerViewPart_terminate_title,
 					DLTKTestingMessages.TestRunnerViewPart_terminate_message)) {
 				if (fTestRunSession != null)
 					fTestRunSession.stopTestRun();
 			}
 		}
 		ILaunch launch = fTestRunSession.getLaunch();
 		if (launch != null && launch.getLaunchConfiguration() != null) {
 			ILaunchConfiguration launchConfiguration = launch
 					.getLaunchConfiguration();
 			if (launchConfiguration != null) {
 				try {
 					String oldName = launchConfiguration.getName();
 					String oldFailuresFilename = launchConfiguration
 							.getAttribute(
 									DLTKTestingConstants.ATTR_FAILURES_NAMES,
 									(String) null);
 					String configName;
 					if (oldFailuresFilename != null) {
 						configName = oldName;
 					} else {
 						configName = Messages
 								.format(
 										DLTKTestingMessages.TestRunnerViewPart_rerunFailedFirstLaunchConfigName,
 										oldName);
 					}
 					ILaunchConfigurationWorkingCopy tmp = launchConfiguration
 							.copy(configName);
 					tmp.setAttribute(DLTKTestingConstants.ATTR_FAILURES_NAMES,
 							fTestRunSession.getTestRunnerUI().collectFailures(
 									fTestRunSession));
 					relaunch(tmp, launch.getLaunchMode());
 					return;
 				} catch (CoreException e) {
 					ErrorDialog
 							.openError(
 									getSite().getShell(),
 									DLTKTestingMessages.TestRunnerViewPart_error_cannotrerun,
 									e.getMessage(), e.getStatus());
 				}
 			}
 			MessageDialog
 					.openInformation(
 							getSite().getShell(),
 							DLTKTestingMessages.TestRunnerViewPart_cannotrerun_title,
 							DLTKTestingMessages.TestRunnerViewPart_cannotrerurn_message);
 		}
 	}
 
 	private void relaunch(ILaunchConfiguration configuration, String launchMode) {
 		DebugUITools.launch(configuration, launchMode);
 	}
 
 	public void setAutoScroll(boolean scroll) {
 		fAutoScroll = scroll;
 	}
 
 	public boolean isAutoScroll() {
 		return fAutoScroll;
 	}
 
 	public void selectNextFailure() {
 		fTestViewer.selectFailure(true);
 	}
 
 	public void selectPreviousFailure() {
 		fTestViewer.selectFailure(false);
 	}
 
 	protected void selectFirstFailure() {
 		fTestViewer.selectFirstFailure();
 	}
 
 	private boolean hasErrorsOrFailures() {
 		return getErrorsPlusFailures() > 0;
 	}
 
 	private int getErrorsPlusFailures() {
 		if (fTestRunSession == null)
 			return 0;
 		else
 			return fTestRunSession.getErrorCount()
 					+ fTestRunSession.getFailureCount();
 	}
 
 	private String elapsedTimeAsString(long runTime) {
 		return NumberFormat.getInstance().format((double) runTime / 1000);
 	}
 
 	private void handleStopped() {
 		postSyncRunnable(new Runnable() {
 			public void run() {
 				if (isDisposed())
 					return;
 				resetViewIcon();
 				fStopAction.setEnabled(false);
 				updateRerunFailedFirstAction();
 			}
 		});
 		stopUpdateJobs();
 	}
 
 	private void resetViewIcon() {
 		fViewImage = fOriginalViewImage;
 		firePropertyChange(IWorkbenchPart.PROP_TITLE);
 	}
 
 	private void updateViewIcon() {
 		if (fTestRunSession == null || fTestRunSession.isStopped()
 				|| fTestRunSession.isRunning()
 				|| fTestRunSession.getStartedCount() == 0)
 			fViewImage = fOriginalViewImage;
 		else if (hasErrorsOrFailures())
 			fViewImage = fTestRunFailIcon;
 		else
 			fViewImage = fTestRunOKIcon;
 		firePropertyChange(IWorkbenchPart.PROP_TITLE);
 	}
 
 	private void updateViewTitleProgress() {
 		if (fTestRunSession != null) {
 			if (fTestRunSession.isRunning()) {
 				Image progress = fProgressImages.getImage(fTestRunSession
 						.getStartedCount(), fTestRunSession.getTotalCount(),
 						fTestRunSession.getErrorCount(), fTestRunSession
 								.getFailureCount());
 				if (progress != fViewImage) {
 					fViewImage = progress;
 					firePropertyChange(IWorkbenchPart.PROP_TITLE);
 				}
 			} else {
 				updateViewIcon();
 			}
 		} else {
 			resetViewIcon();
 		}
 	}
 
 	/**
 	 * @param testRunSession
 	 *            new active test run session
 	 * @return deactivated session, or <code>null</code> iff no session got
 	 *         deactivated
 	 */
 	private TestRunSession setActiveTestRunSession(TestRunSession testRunSession) {
 		/*
 		 * - State: fTestRunSession fTestSessionListener Jobs
 		 * fTestViewer.processChangesInUI(); - UI: fCounterPanel fProgressBar
 		 * setContentDescription / fInfoMessage setTitleToolTip view icons
 		 * statusLine fFailureTrace
 		 * 
 		 * action enablement
 		 */
 		if (fTestRunSession == testRunSession)
 			return null;
 
 		if (fTestRunSession != null && fTestSessionListener != null) {
 			fTestRunSession.removeTestSessionListener(fTestSessionListener);
 			fTestSessionListener = null;
 		}
 
 		TestRunSession deactivatedSession = fTestRunSession;
 
 		fTestRunSession = testRunSession;
 		fTestViewer.registerActiveSession(testRunSession);
 
 		if (fSashForm.isDisposed()) {
 			stopUpdateJobs();
 			return deactivatedSession;
 		}
 
 		if (testRunSession == null) {
 			setTitleToolTip(null);
 			resetViewIcon();
 			clearStatus();
 			fFailureTrace.reset();
 
 			registerInfoMessage(" "); //$NON-NLS-1$
 			stopUpdateJobs();
 
 			fStopAction.setEnabled(false);
 			fRerunFailedFirstAction.setEnabled(false);
 			fRerunLastTestAction.setEnabled(false);
 
 		} else {
 			fTestSessionListener = new TestSessionListener();
 			fTestRunSession.addTestSessionListener(fTestSessionListener);
 
 			setTitleToolTip();
 
 			clearStatus();
 			fFailureTrace.reset();
 			registerInfoMessage(fTestRunSession.getTestRunName());
 
 			updateRerunFailedFirstAction();
 			fRerunLastTestAction
 					.setEnabled(fTestRunSession.getLaunch() != null);
 
 			if (fTestRunSession.isRunning()) {
 				startUpdateJobs();
 
 				fStopAction.setEnabled(true);
 
 			} else /* old or fresh session: don't want jobs at this stage */{
 				stopUpdateJobs();
 
 				fStopAction.setEnabled(fTestRunSession.isKeptAlive());
 				fTestViewer.expandFirstLevel();
 			}
 		}
 		return deactivatedSession;
 	}
 
 	private void updateRerunFailedFirstAction() {
 		boolean state = hasErrorsOrFailures()
 				&& fTestRunSession.getTestRunnerUI().canRerunFailures()
 				&& fTestRunSession.getLaunch() != null;
 		fRerunFailedFirstAction.setEnabled(state);
 	}
 
 	private void setTitleToolTip() {
 		String testKindDisplayStr = fTestRunSession.getTestRunnerUI()
 				.getDisplayName();
 
 		if (testKindDisplayStr != null)
 			setTitleToolTip(NLS.bind(
 					DLTKTestingMessages.TestRunnerViewPart_titleToolTip,
 					fTestRunSession.getTestRunName(), testKindDisplayStr));
 		else
 			setTitleToolTip(fTestRunSession.getTestRunName());
 	}
 
 	public synchronized void dispose() {
 		fIsDisposed = true;
 		if (fTestRunSessionListener != null)
 			DLTKTestingPlugin.getModel().removeTestRunSessionListener(
 					fTestRunSessionListener);
 
 		IHandlerService handlerService = (IHandlerService) getSite()
 				.getWorkbenchWindow().getService(IHandlerService.class);
 		handlerService.deactivateHandler(fRerunLastActivation);
 		handlerService.deactivateHandler(fRerunFailedFirstActivation);
 		setActiveTestRunSession(null);
 
 		if (fProgressImages != null)
 			fProgressImages.dispose();
 		getViewSite().getPage().removePartListener(fPartListener);
 
 		disposeImages();
 		if (fClipboard != null)
 			fClipboard.dispose();
 		if (fViewMenuListener != null) {
 			getViewSite().getActionBars().getMenuManager().removeMenuListener(
 					fViewMenuListener);
 		}
 		if (fDirtyListener != null) {
 			DLTKCore.removeElementChangedListener(fDirtyListener);
 			fDirtyListener = null;
 		}
 	}
 
 	private void disposeImages() {
 		fTestRunOKIcon.dispose();
 		fTestRunFailIcon.dispose();
 		fStackViewIcon.dispose();
 		fTestRunOKDirtyIcon.dispose();
 		fTestRunFailDirtyIcon.dispose();
 
 		fTestIcon.dispose();
 		fTestRunningIcon.dispose();
 		fTestOkIcon.dispose();
 		fTestErrorIcon.dispose();
 		fTestFailIcon.dispose();
 		fTestIgnoredIcon.dispose();
 
 		fSuiteIcon.dispose();
 		fSuiteRunningIcon.dispose();
 		fSuiteErrorIcon.dispose();
 		fSuiteFailIcon.dispose();
 
 		fCategoryIcon.dispose();
 		fCategoryOkIcon.dispose();
 		fCategoryErrorIcon.dispose();
 		fCategoryFailIcon.dispose();
 		fCategoryAbortedIcon.dispose();
 		fCategorySkippedIcon.dispose();
 		fCategoryBlockedIcon.dispose();
 		fCategoryUnknownIcon.dispose();
 		fCategoryRunningIcon.dispose();
 	}
 
 	private void postSyncRunnable(Runnable r) {
 		if (!isDisposed())
 			getDisplay().syncExec(r);
 	}
 
 	private void refreshCounters() {
 		// TODO: Inefficient. Either
 		// - keep a boolean fHasTestRun and update only on changes, or
 		// - improve components to only redraw on changes (once!).
 
 		int startedCount;
 		int ignoredCount;
 		int totalCount;
 		int errorCount;
 		int failureCount;
 		boolean hasErrorsOrFailures;
 		boolean stopped;
 
 		if (fTestRunSession != null) {
 			startedCount = fTestRunSession.getStartedCount();
 			ignoredCount = fTestRunSession.getIgnoredCount();
 			totalCount = fTestRunSession.getTotalCount();
 			errorCount = fTestRunSession.getErrorCount();
 			failureCount = fTestRunSession.getFailureCount();
 			hasErrorsOrFailures = errorCount + failureCount > 0;
 			stopped = fTestRunSession.isStopped();
 		} else {
 			startedCount = 0;
 			ignoredCount = 0;
 			totalCount = 0;
 			errorCount = 0;
 			failureCount = 0;
 			hasErrorsOrFailures = false;
 			stopped = false;
 		}
 
 		fCounterPanel.setTotal(totalCount);
 		fCounterPanel.setRunValue(startedCount, ignoredCount);
 		fCounterPanel.setErrorValue(errorCount);
 		fCounterPanel.setFailureValue(failureCount);
 
 		int ticksDone;
 		if (startedCount == 0)
 			ticksDone = 0;
 		else if (startedCount == totalCount && !fTestRunSession.isRunning())
 			ticksDone = totalCount;
 		else
 			ticksDone = startedCount - 1;
 
 		fProgressBar.reset(hasErrorsOrFailures, stopped, ticksDone, totalCount);
 	}
 
 	protected void postShowTestResultsView() {
 		postSyncRunnable(new Runnable() {
 			public void run() {
 				if (isDisposed())
 					return;
 				showTestResultsView();
 			}
 		});
 	}
 
 	public void showTestResultsView() {
 		IWorkbenchWindow window = getSite().getWorkbenchWindow();
 		IWorkbenchPage page = window.getActivePage();
 		TestRunnerViewPart testRunner = null;
 
 		if (page != null) {
 			try { // show the result view
 				testRunner = (TestRunnerViewPart) page
 						.findView(TestRunnerViewPart.NAME);
 				if (testRunner == null) {
 					IWorkbenchPart activePart = page.getActivePart();
 					testRunner = (TestRunnerViewPart) page
 							.showView(TestRunnerViewPart.NAME);
 					// restore focus
 					page.activate(activePart);
 				} else {
 					page.bringToTop(testRunner);
 				}
 			} catch (PartInitException pie) {
 				DLTKTestingPlugin.log(pie);
 			}
 		}
 	}
 
 	protected void doShowInfoMessage() {
 		if (fInfoMessage != null) {
 			setContentDescription(fInfoMessage);
 			fInfoMessage = null;
 		}
 	}
 
 	protected void registerInfoMessage(String message) {
 		fInfoMessage = message;
 	}
 
 	private SashForm createSashForm(Composite parent) {
 		fSashForm = new SashForm(parent, SWT.VERTICAL);
 
 		ViewForm top = new ViewForm(fSashForm, SWT.NONE);
 
 		Composite empty = new Composite(top, SWT.NONE);
 		empty.setLayout(new Layout() {
 			protected Point computeSize(Composite composite, int wHint,
 					int hHint, boolean flushCache) {
 				return new Point(1, 1); // (0, 0) does not work with
 				// super-intelligent ViewForm
 			}
 
 			protected void layout(Composite composite, boolean flushCache) {
 			}
 		});
 		top.setTopLeft(empty); // makes ViewForm draw the horizontal separator
 		// line ...
 		fTestViewer = new TestViewer(top, fClipboard, this);
 		top.setContent(fTestViewer.getTestViewerControl());
 
 		ViewForm bottom = new ViewForm(fSashForm, SWT.NONE);
 
 		CLabel label = new CLabel(bottom, SWT.NONE);
 		label.setText(DLTKTestingMessages.TestRunnerViewPart_label_failure);
 		label.setImage(fStackViewIcon);
 		bottom.setTopLeft(label);
 		ToolBar failureToolBar = new ToolBar(bottom, SWT.FLAT | SWT.WRAP);
 		bottom.setTopCenter(failureToolBar);
 		fFailureTrace = new FailureTrace(bottom, fClipboard, this,
 				failureToolBar);
 		bottom.setContent(fFailureTrace.getComposite());
 
 		fSashForm.setWeights(new int[] { 50, 50 });
 		return fSashForm;
 	}
 
 	private void clearStatus() {
 		getStatusLine().setMessage(null);
 		getStatusLine().setErrorMessage(null);
 	}
 
 	public void setFocus() {
 		if (fTestViewer != null)
 			fTestViewer.getTestViewerControl().setFocus();
 	}
 
 	public void createPartControl(Composite parent) {
 		fParent = parent;
 		addResizeListener(parent);
 		fClipboard = new Clipboard(parent.getDisplay());
 
 		GridLayout gridLayout = new GridLayout();
 		gridLayout.marginWidth = 0;
 		gridLayout.marginHeight = 0;
 		parent.setLayout(gridLayout);
 
 		fViewHistory = new RunnerViewHistory();
 		configureToolBar();
 
 		fCounterComposite = createProgressCountPanel(parent);
 		fCounterComposite.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
 				| GridData.HORIZONTAL_ALIGN_FILL));
 		SashForm sashForm = createSashForm(parent);
 		sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
 
 		IActionBars actionBars = getViewSite().getActionBars();
 		fCopyAction = new DLTKTestingCopyAction(fFailureTrace, fClipboard);
 		actionBars.setGlobalActionHandler(ActionFactory.COPY.getId(),
 				fCopyAction);
 
 		fOriginalViewImage = getTitleImage();
 		fProgressImages = new ProgressImages();
 		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
 				IDLTKTestingHelpContextIds.RESULTS_VIEW);
 
 		getViewSite().getPage().addPartListener(fPartListener);
 
 		setFilterAndLayout(false, LAYOUT_HIERARCHICAL);
 		if (fMemento != null) {
 			restoreLayoutState(fMemento);
 		}
 		fMemento = null;
 
 		fTestRunSessionListener = new TestRunSessionListener();
 		DLTKTestingPlugin.getModel().addTestRunSessionListener(
 				fTestRunSessionListener);
 	}
 
 	private void addResizeListener(Composite parent) {
 		parent.addControlListener(new ControlListener() {
 			public void controlMoved(ControlEvent e) {
 			}
 
 			public void controlResized(ControlEvent e) {
 				computeOrientation();
 			}
 		});
 	}
 
 	void computeOrientation() {
 		if (fOrientation != VIEW_ORIENTATION_AUTOMATIC) {
 			fCurrentOrientation = fOrientation;
 			setOrientation(fCurrentOrientation);
 		} else {
 			Point size = fParent.getSize();
 			if (size.x != 0 && size.y != 0) {
 				if (size.x > size.y)
 					setOrientation(VIEW_ORIENTATION_HORIZONTAL);
 				else
 					setOrientation(VIEW_ORIENTATION_VERTICAL);
 			}
 		}
 	}
 
 	private void configureToolBar() {
 		IActionBars actionBars = getViewSite().getActionBars();
 		IToolBarManager toolBar = actionBars.getToolBarManager();
 		IMenuManager viewMenu = actionBars.getMenuManager();
 
 		fNextAction = new ShowNextFailureAction(this);
 		fNextAction.setEnabled(false);
 		actionBars.setGlobalActionHandler(ActionFactory.NEXT.getId(),
 				fNextAction);
 
 		fPreviousAction = new ShowPreviousFailureAction(this);
 		fPreviousAction.setEnabled(false);
 		actionBars.setGlobalActionHandler(ActionFactory.PREVIOUS.getId(),
 				fPreviousAction);
 
 		fStopAction = new StopAction();
 		fStopAction.setEnabled(false);
 
 		fRerunLastTestAction = new RerunLastAction();
 		IHandlerService handlerService = (IHandlerService) getSite()
 				.getWorkbenchWindow().getService(IHandlerService.class);
 		fRerunLastActivation = handlerService.activateHandler(
 				RERUN_LAST_COMMAND, new ActionHandlerWrapper(
 						fRerunLastTestAction));
 
 		fRerunFailedFirstAction = new RerunLastFailedFirstAction();
 		fRerunFailedFirstActivation = handlerService.activateHandler(
 				RERUN_FAILED_FIRST_COMMAND, new ActionHandlerWrapper(
 						fRerunFailedFirstAction));
 
 		fFailuresOnlyFilterAction = new FailuresOnlyFilterAction();
 
 		fScrollLockAction = new ScrollLockAction(this);
 		fScrollLockAction.setChecked(!fAutoScroll);
 
 		fToggleOrientationActions = new ToggleOrientationAction[] {
 				new ToggleOrientationAction(VIEW_ORIENTATION_VERTICAL),
 				new ToggleOrientationAction(VIEW_ORIENTATION_HORIZONTAL),
 				new ToggleOrientationAction(VIEW_ORIENTATION_AUTOMATIC) };
 
 		fShowTestHierarchyAction = new ShowTestHierarchyAction();
 
 		toolBar.add(fNextAction);
 		toolBar.add(fPreviousAction);
 		toolBar.add(fFailuresOnlyFilterAction);
 		toolBar.add(fScrollLockAction);
 		toolBar.add(new Separator());
 		toolBar.add(fRerunLastTestAction);
 		toolBar.add(fRerunFailedFirstAction);
 		toolBar.add(fStopAction);
 		toolBar.add(fViewHistory.createHistoryDropDownAction());
 
 		viewMenu.add(fShowTestHierarchyAction);
 		viewMenu.add(new Separator());
 
 		MenuManager layoutSubMenu = new MenuManager(
 				DLTKTestingMessages.TestRunnerViewPart_layout_menu);
 		for (int i = 0; i < fToggleOrientationActions.length; ++i) {
 			layoutSubMenu.add(fToggleOrientationActions[i]);
 		}
 		viewMenu.add(layoutSubMenu);
 		viewMenu.add(new Separator());
 
 		viewMenu.add(fFailuresOnlyFilterAction);
 
 		fActivateOnErrorAction = new ActivateOnErrorAction();
 		viewMenu.add(fActivateOnErrorAction);
 		fViewMenuListener = new IMenuListener() {
 			public void menuAboutToShow(IMenuManager manager) {
 				fActivateOnErrorAction.update();
 			}
 		};
 
 		viewMenu.addMenuListener(fViewMenuListener);
 
 		actionBars.updateActionBars();
 	}
 
 	private IStatusLineManager getStatusLine() {
 		// we want to show messages globally hence we
 		// have to go through the active part
 		IViewSite site = getViewSite();
 		IWorkbenchPage page = site.getPage();
 		IWorkbenchPart activePart = page.getActivePart();
 
 		if (activePart instanceof IViewPart) {
 			IViewPart activeViewPart = (IViewPart) activePart;
 			IViewSite activeViewSite = activeViewPart.getViewSite();
 			return activeViewSite.getActionBars().getStatusLineManager();
 		}
 
 		if (activePart instanceof IEditorPart) {
 			IEditorPart activeEditorPart = (IEditorPart) activePart;
 			IEditorActionBarContributor contributor = activeEditorPart
 					.getEditorSite().getActionBarContributor();
 			if (contributor instanceof EditorActionBarContributor)
 				return ((EditorActionBarContributor) contributor)
 						.getActionBars().getStatusLineManager();
 		}
 		// no active part
 		return getViewSite().getActionBars().getStatusLineManager();
 	}
 
 	protected Composite createProgressCountPanel(Composite parent) {
 		Composite composite = new Composite(parent, SWT.NONE);
 		GridLayout layout = new GridLayout();
 		composite.setLayout(layout);
 		setCounterColumns(layout);
 
 		fCounterPanel = new CounterPanel(composite);
 		fCounterPanel.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
 				| GridData.HORIZONTAL_ALIGN_FILL));
 		fProgressBar = new DLTKTestingProgressBar(composite);
 		fProgressBar.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
 				| GridData.HORIZONTAL_ALIGN_FILL));
 		return composite;
 	}
 
 	public void handleTestSelected(TestElement test) {
 		showFailure(test);
 		fCopyAction.handleTestSelected(test);
 	}
 
 	private void showFailure(final TestElement test) {
 		postSyncRunnable(new Runnable() {
 			public void run() {
 				if (!isDisposed())
 					fFailureTrace.showFailure(test);
 			}
 		});
 	}
 
 	public final ITestRunnerUI getTestRunnerUI() {
 		return fTestRunSession == null ? NullTestRunnerUI.getInstance()
 				: fTestRunSession.getTestRunnerUI();
 	}
 
 	/**
 	 * @return the Java project, or <code>null</code>
 	 */
 	public IScriptProject getLaunchedProject() {
 		return fTestRunSession == null ? null : fTestRunSession
 				.getLaunchedProject();
 	}
 
 	public ILaunch getLaunch() {
 		return fTestRunSession == null ? null : fTestRunSession.getLaunch();
 	}
 
 	public static Image createImage(String path) {
 		return DLTKTestingPlugin.getImageDescriptor(path).createImage();
 	}
 
 	private boolean isDisposed() {
 		return fIsDisposed || fCounterPanel.isDisposed();
 	}
 
 	private Display getDisplay() {
 		return getViewSite().getShell().getDisplay();
 	}
 
 	/*
 	 * @see IWorkbenchPart#getTitleImage()
 	 */
 	public Image getTitleImage() {
 		if (fOriginalViewImage == null)
 			fOriginalViewImage = super.getTitleImage();
 
 		if (fViewImage == null)
 			return super.getTitleImage();
 		return fViewImage;
 	}
 
 	void codeHasChanged() {
 		if (fDirtyListener != null) {
 			DLTKCore.removeElementChangedListener(fDirtyListener);
 			fDirtyListener = null;
 		}
 		if (fViewImage == fTestRunOKIcon)
 			fViewImage = fTestRunOKDirtyIcon;
 		else if (fViewImage == fTestRunFailIcon)
 			fViewImage = fTestRunFailDirtyIcon;
 
 		Runnable r = new Runnable() {
 			public void run() {
 				if (isDisposed())
 					return;
 				firePropertyChange(IWorkbenchPart.PROP_TITLE);
 			}
 		};
 		if (!isDisposed())
 			getDisplay().asyncExec(r);
 	}
 
 	public boolean isCreated() {
 		return fCounterPanel != null;
 	}
 
 	public void rerunTest(ITestElement testElement, String launchMode) {
 		DebugUITools.saveAndBuildBeforeLaunch();
 		try {
 			boolean couldLaunch = fTestRunSession.rerunTest(testElement,
 					launchMode);
 			if (!couldLaunch) {
 				MessageDialog
 						.openInformation(
 								getSite().getShell(),
 								DLTKTestingMessages.TestRunnerViewPart_cannotrerun_title,
 								DLTKTestingMessages.TestRunnerViewPart_cannotrerurn_message);
 			} else if (fTestRunSession.isKeptAlive()) {
 				TestCaseElement testCaseElement = (TestCaseElement) fTestRunSession
 						.getTestElement(testElement.getId());
 				testCaseElement.setStatus(TestElement.Status.RUNNING, null,
 						null, null);
 				fTestViewer.registerViewerUpdate(testCaseElement);
 				postSyncProcessChanges();
 			}
 
 		} catch (CoreException e) {
 			ErrorDialog.openError(getSite().getShell(),
 					DLTKTestingMessages.TestRunnerViewPart_error_cannotrerun, e
 							.getMessage(), e.getStatus());
 		}
 	}
 
 	private void postSyncProcessChanges() {
 		postSyncRunnable(new Runnable() {
 			public void run() {
 				processChangesInUI();
 			}
 		});
 	}
 
 	public void warnOfContentChange() {
 		IWorkbenchSiteProgressService service = getProgressService();
 		if (service != null)
 			service.warnOfContentChange();
 	}
 
 	public boolean lastLaunchIsKeptAlive() {
 		return fTestRunSession != null && fTestRunSession.isKeptAlive();
 	}
 
 	private void setOrientation(int orientation) {
 		if ((fSashForm == null) || fSashForm.isDisposed())
 			return;
 		boolean horizontal = orientation == VIEW_ORIENTATION_HORIZONTAL;
 		fSashForm.setOrientation(horizontal ? SWT.HORIZONTAL : SWT.VERTICAL);
 		for (int i = 0; i < fToggleOrientationActions.length; ++i)
 			fToggleOrientationActions[i]
 					.setChecked(fOrientation == fToggleOrientationActions[i]
 							.getOrientation());
 		fCurrentOrientation = orientation;
 		GridLayout layout = (GridLayout) fCounterComposite.getLayout();
 		setCounterColumns(layout);
 		fParent.layout();
 	}
 
 	private void setCounterColumns(GridLayout layout) {
 		if (fCurrentOrientation == VIEW_ORIENTATION_HORIZONTAL)
 			layout.numColumns = 2;
 		else
 			layout.numColumns = 1;
 	}
 
 	private static boolean getShowOnErrorOnly() {
 		IPreferenceStore store = DLTKTestingPlugin.getDefault()
 				.getPreferenceStore();
 		return store
 				.getBoolean(DLTKTestingPreferencesConstants.SHOW_ON_ERROR_ONLY);
 	}
 
 	public FailureTrace getFailureTrace() {
 		return fFailureTrace;
 	}
 
 	void setShowFailuresOnly(boolean failuresOnly) {
 		setFilterAndLayout(failuresOnly, fLayout);
 	}
 
 	private void setLayoutMode(int mode) {
 		setFilterAndLayout(fFailuresOnlyFilterAction.isChecked(), mode);
 	}
 
 	private void setFilterAndLayout(boolean failuresOnly, int layoutMode) {
 		fShowTestHierarchyAction.setChecked(layoutMode == LAYOUT_HIERARCHICAL);
 		fLayout = layoutMode;
 		fFailuresOnlyFilterAction.setChecked(failuresOnly);
 		fTestViewer.setShowFailuresOnly(failuresOnly, layoutMode);
 	}
 
 	public ITestRunSession getCurrentSession() {
 		return fTestRunSession;
 	}
 }
