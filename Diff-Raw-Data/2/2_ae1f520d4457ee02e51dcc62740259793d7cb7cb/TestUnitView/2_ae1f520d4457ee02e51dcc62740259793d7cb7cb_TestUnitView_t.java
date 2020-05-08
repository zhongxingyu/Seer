 package org.rubypeople.rdt.testunit.views;
 
 import java.net.MalformedURLException;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExtensionPoint;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.MultiStatus;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
 import org.eclipse.debug.core.ILaunchManager;
 import org.eclipse.debug.ui.DebugUITools;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IStatusLineManager;
 import org.eclipse.jface.action.IToolBarManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CLabel;
 import org.eclipse.swt.custom.CTabFolder;
 import org.eclipse.swt.custom.SashForm;
 import org.eclipse.swt.custom.ViewForm;
 import org.eclipse.swt.dnd.Clipboard;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.ToolBar;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.IEditorActionBarContributor;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IViewPart;
 import org.eclipse.ui.IViewSite;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.part.EditorActionBarContributor;
 import org.eclipse.ui.part.ViewPart;
 import org.eclipse.ui.progress.UIJob;
 import org.rubypeople.rdt.core.IRubyElement;
 import org.rubypeople.rdt.core.IRubyProject;
 import org.rubypeople.rdt.core.IType;
 import org.rubypeople.rdt.testunit.ITestRunListener;
 import org.rubypeople.rdt.testunit.TestunitPlugin;
 import org.rubypeople.rdt.testunit.launcher.TestUnitLaunchConfigurationDelegate;
 
 public class TestUnitView extends ViewPart implements ITestRunListener3 {
 
 	public static final String NAME = "org.rubypeople.rdt.testunit.views.TestUnitView";
 
 	static final int REFRESH_INTERVAL = 200;
 
	public static final String ID_EXTENSION_POINT_TESTRUN_TABS = TestunitPlugin.PLUGIN_ID + "." + "internalTestRunTabs"; //$NON-NLS-1$ //$NON-NLS-2$
 
 	//orientations
 	static final int VIEW_ORIENTATION_VERTICAL = 0;
 	static final int VIEW_ORIENTATION_HORIZONTAL = 1;
 	static final int VIEW_ORIENTATION_AUTOMATIC = 2;
 
 	final Image fStackViewIcon = TestUnitView.createImage("eview16/stackframe.gif");//$NON-NLS-1$
 
 	/**
 	 * The currently active run tab
 	 */
 	private TestRunTab fActiveRunTab;
 
 	/**
 	 * The collection of ITestRunTabs
 	 */
 	protected Vector fTestRunTabs = new Vector();
 
 	/**
 	 * Map storing TestInfos for each executed test keyed by the test name.
 	 */
 	private Map fTestInfos = new HashMap();
 
 	/**
 	 * Is the UI disposed
 	 */
 	private boolean fIsDisposed = false;
 	/**
 	 * The client side of the remote test runner
 	 */
 	private RemoteTestRunnerClient fTestRunnerClient;
 
 	/**
 	 * The launcher that has started the test
 	 */
 	private String fLaunchMode;
 	private ILaunch fLastLaunch;
 
 	/**
 	 * Actions
 	 */
 	private Action fRerunLastTestAction;
 
 	/**
 	 * Number of executed tests during a test run
 	 */
 	protected volatile int fExecutedTests;
 	/**
 	 * Number of errors during this test run
 	 */
 	protected volatile int fErrorCount;
 	/**
 	 * Number of failures during this test run
 	 */
 	protected volatile int fFailureCount;
 	/**
 	 * Number of tests run
 	 */
 	protected volatile int fTestCount;
 
 	/**
 	 * The first failure of a test run. Used to reveal the first failed tests at
 	 * the end of a run.
 	 */
 	private List fFailures = new ArrayList();
 
 	protected boolean fShowOnErrorOnly = false;
 
 	private CounterPanel fCounterPanel;
 	private TestUnitProgressBar fProgressBar;
 	private int fCurrentOrientation;
 	private Composite fCounterComposite;
 	private SashForm fSashForm;
 	private CTabFolder fTabFolder;
 	private FailureTrace fFailureTrace;
 	private Clipboard fClipboard;
 	protected volatile String fStatus;
 
 	private UpdateUIJob fUpdateJob;
 
 	/**
 	 * Whether the output scrolls and reveals tests as they are executed.
 	 */
 	private boolean fAutoScroll = true;
 
 	private ScrollLockAction fScrollLockAction;
 
     private IRubyProject fTestProject;
 
 	/**
 	 * The constructor.
 	 */
 	public TestUnitView() {}
 
 	public static Image createImage(String path) {
 		try {
 			ImageDescriptor id = ImageDescriptor.createFromURL(TestunitPlugin.makeIconFileURL(path));
 			return id.createImage();
 		} catch (MalformedURLException e) {
 			// fall through
 		}
 		return null;
 	}
 
 	/**
 	 * This is a callback that will allow us to create the viewer and initialize
 	 * it.
 	 */
 	public void createPartControl(Composite parent) {
 		fClipboard = new Clipboard(parent.getDisplay());
 
 		GridLayout gridLayout = new GridLayout();
 		gridLayout.marginWidth = 0;
 		gridLayout.marginHeight = 0;
 		parent.setLayout(gridLayout);
 
 		configureToolBar();
 
 		fCounterComposite = createProgressCountPanel(parent);
 		fCounterComposite.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
 		SashForm sashForm = createSashForm(parent);
 		sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
 	}
 
 	private void configureToolBar() {
 		IActionBars actionBars = getViewSite().getActionBars();
 		IToolBarManager toolBar = actionBars.getToolBarManager();
 		// TODO Uncomment when other actions and orientation are available
 		//IMenuManager viewMenu = actionBars.getMenuManager();
 		fRerunLastTestAction = new RerunLastAction();
 		fScrollLockAction = new ScrollLockAction(this);
 		//fNextAction= new ShowNextFailureAction(this);
 		//fPreviousAction= new ShowPreviousFailureAction(this);
 		//fStopAction= new StopAction();
 		//fNextAction.setEnabled(false);
 		//fPreviousAction.setEnabled(false);
 		//fStopAction.setEnabled(false);
 		//actionBars.setGlobalActionHandler(ActionFactory.NEXT.getId(),
 		// fNextAction);
 		//actionBars.setGlobalActionHandler(ActionFactory.PREVIOUS.getId(),
 		// fPreviousAction);
 
 		//toolBar.add(fNextAction);
 		//toolBar.add(fPreviousAction);
 		//toolBar.add(fStopAction);
 		toolBar.add(new Separator());
 		toolBar.add(fRerunLastTestAction);
 		toolBar.add(fScrollLockAction);
 
 		//for (int i = 0; i < fToggleOrientationActions.length; ++i)
 		//	viewMenu.add(fToggleOrientationActions[i]);
 
 		fScrollLockAction.setChecked(!fAutoScroll);
 
 		actionBars.updateActionBars();
 	}
 
 	private SashForm createSashForm(Composite parent) {
 		fSashForm = new SashForm(parent, SWT.VERTICAL);
 		ViewForm top = new ViewForm(fSashForm, SWT.NONE);
 		fTabFolder = createTestRunTabs(top);
 		fTabFolder.setLayoutData(new TabFolderLayout());
 		top.setContent(fTabFolder);
 
 		ViewForm bottom = new ViewForm(fSashForm, SWT.NONE);
 		CLabel label = new CLabel(bottom, SWT.NONE);
 		label.setText(TestUnitMessages.getString("TestRunnerViewPart.label.failure")); //$NON-NLS-1$
 		label.setImage(fStackViewIcon);
 		bottom.setTopLeft(label);
 
 		ToolBar failureToolBar = new ToolBar(bottom, SWT.FLAT | SWT.WRAP);
 		bottom.setTopCenter(failureToolBar);
 		fFailureTrace = new FailureTrace(bottom, fClipboard, this, failureToolBar);
 		bottom.setContent(fFailureTrace.getComposite());
 
 		fSashForm.setWeights(new int[] { 50, 50});
 		return fSashForm;
 	}
 
 	protected CTabFolder createTestRunTabs(Composite parent) {
 		CTabFolder tabFolder = new CTabFolder(parent, SWT.TOP);
 		tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL));
 
 		loadTestRunTabs(tabFolder);
 		tabFolder.setSelection(0);
 		fActiveRunTab = (TestRunTab) fTestRunTabs.firstElement();
 
 		tabFolder.addSelectionListener(new SelectionAdapter() {
 
 			public void widgetSelected(SelectionEvent event) {
 				testTabChanged(event);
 			}
 		});
 		return tabFolder;
 	}
 
 	private void testTabChanged(SelectionEvent event) {
 		for (Enumeration e = fTestRunTabs.elements(); e.hasMoreElements();) {
 			TestRunTab v = (TestRunTab) e.nextElement();
 			if (((CTabFolder) event.widget).getSelection().getText() == v.getName()) {
 				v.setSelectedTest(fActiveRunTab.getSelectedTestId());
 				fActiveRunTab = v;
 				fActiveRunTab.activate();
 			}
 		}
 	}
 
 	private void loadTestRunTabs(CTabFolder tabFolder) {
 		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(ID_EXTENSION_POINT_TESTRUN_TABS);
 		if (extensionPoint == null) { return; }
 		IConfigurationElement[] configs = extensionPoint.getConfigurationElements();
 		MultiStatus status = new MultiStatus(TestunitPlugin.PLUGIN_ID, IStatus.OK, "Could not load some testRunTabs extension points", null); //$NON-NLS-1$ 	
 
 		for (int i = 0; i < configs.length; i++) {
 			try {
 				TestRunTab testRunTab = (TestRunTab) configs[i].createExecutableExtension("class"); //$NON-NLS-1$
 				testRunTab.createTabControl(tabFolder, fClipboard, this);
 				fTestRunTabs.addElement(testRunTab);
 			} catch (CoreException e) {
 				status.add(e.getStatus());
 			}
 		}
 		if (!status.isOK()) {
 			TestunitPlugin.log(status);
 		}
 	}
 
 	protected Composite createProgressCountPanel(Composite parent) {
 		Composite composite = new Composite(parent, SWT.NONE);
 		GridLayout layout = new GridLayout();
 		composite.setLayout(layout);
 		setCounterColumns(layout);
 
 		fCounterPanel = new CounterPanel(composite);
 		fCounterPanel.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
 		fProgressBar = new TestUnitProgressBar(composite);
 		fProgressBar.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
 		return composite;
 	}
 
 	private void setCounterColumns(GridLayout layout) {
 		if (fCurrentOrientation == VIEW_ORIENTATION_HORIZONTAL)
 			layout.numColumns = 2;
 		else
 			layout.numColumns = 1;
 	}
 
 	/**
 	 * Passing the focus request to the viewer's control.
 	 */
 	public void setFocus() {
 		if (fActiveRunTab != null) fActiveRunTab.setFocus();
 	}
 
 	public void showTest(TestRunInfo test) {
 		fActiveRunTab.setSelectedTest(test.getTestId());
 		handleTestSelected(test.getTestId());
 		// TODO Allow OpenTestAction again!
 		//	new OpenTestAction(this, test.getClassName(),
 		// test.getTestMethodName()).run();
 	}
 
 	public void handleTestSelected(String testId) {
 		TestRunInfo testInfo = getTestInfo(testId);
 
 		if (testInfo == null) {
 			showFailure(null); //$NON-NLS-1$
 		} else {
 			showFailure(testInfo);
 		}
 	}
 
 	public TestRunInfo getTestInfo(String testId) {
 		if (testId == null) return null;
 		return (TestRunInfo) fTestInfos.get(testId);
 	}
 
 	private void showFailure(final TestRunInfo failure) {
 		postSyncRunnable(new Runnable() {
 
 			public void run() {
 				if (!isDisposed()) fFailureTrace.showFailure(failure);
 			}
 		});
 	}
 
 	private void postSyncRunnable(Runnable r) {
 		if (!isDisposed()) getDisplay().syncExec(r);
 	}
 
 	private boolean isDisposed() {
 		return fIsDisposed || fCounterPanel.isDisposed();
 	}
 
 	private Display getDisplay() {
 		return getViewSite().getShell().getDisplay();
 	}
 
 	public synchronized void dispose() {
 		// TODO Uncomment and fix as UI gets better
 		fIsDisposed = true;
 		stopTest();
 		//if (fProgressImages != null)
 		//		fProgressImages.dispose();
 		//TestunitPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
 		//testRunOKIcon.dispose();
 		//fTestRunFailIcon.dispose();
 		fStackViewIcon.dispose();
 		//TestRunOKDirtyIcon.dispose();
 		//fTestRunFailDirtyIcon.dispose();
 		if (fClipboard != null) fClipboard.dispose();
 	}
 
 	public void rerunTest(String testId, String className, String testName, String launchMode) {
 		DebugUITools.saveAndBuildBeforeLaunch();
 		if (lastLaunchIsKeptAlive())
 			fTestRunnerClient.rerunTest(testId, className, testName);
 		else if (fLastLaunch != null) {
 			// run the selected test using the previous launch configuration
 			ILaunchConfiguration launchConfiguration = fLastLaunch.getLaunchConfiguration();
 			if (launchConfiguration != null) {
 				// TODO Cleanup
 				//rerunWithNewPort(className, launchMode, launchConfiguration);
 				try {
 					String name = className;
 					if (testName != null) name += "." + testName; //$NON-NLS-1$
 					String configName = TestUnitMessages.getFormattedString("TestRunnerViewPart.configName", name); //$NON-NLS-1$
 					ILaunchConfigurationWorkingCopy tmp = launchConfiguration.copy(configName);
 					// fix for bug: 64838 junit view run single test does not
 					// use
 					// correct class [JUnit]
 					tmp.setAttribute(TestUnitLaunchConfigurationDelegate.TESTTYPE_ATTR, className);
 					
 					if (testName != null) {
 						tmp.setAttribute(TestUnitLaunchConfigurationDelegate.TESTNAME_ATTR, testName);
 					}
 					tmp.launch(launchMode, null);
 					return;
 				} catch (CoreException e) {
 					ErrorDialog.openError(getSite().getShell(), TestUnitMessages.getString("TestRunnerViewPart.error.cannotrerun"), e.getMessage(), e.getStatus() //$NON-NLS-1$
 							);
 				}
 			}
 			MessageDialog.openInformation(getSite().getShell(), TestUnitMessages.getString("TestRunnerViewPart.cannotrerun.title"), //$NON-NLS-1$
 					TestUnitMessages.getString("TestRunnerViewPart.cannotrerurn.message") //$NON-NLS-1$
 					);
 		}
 	}
 
 	public boolean lastLaunchIsKeptAlive() {
 		return fTestRunnerClient != null && fTestRunnerClient.isRunning() && ILaunchManager.DEBUG_MODE.equals(fLaunchMode);
 	}
 
 	public void startTestRunListening(int port, IType type, ILaunch launch) {
 	    if(type != null) fTestProject= type.getRubyProject();
 		fLaunchMode = launch.getLaunchMode();
 		aboutToLaunch();
 
 		if (fTestRunnerClient != null) {
 			stopTest();
 		}
 		fTestRunnerClient = new RemoteTestRunnerClient();
 
 		// add the TestUnitView to the list of registered listeners
 		ITestRunListener[] listenerArray = new ITestRunListener[1];
 		listenerArray[0] = this;
 		fTestRunnerClient.startListening(listenerArray, port);
 
 		fLastLaunch = launch;
 		// TODO Uncomment now that we have the type object!
 		//		setViewPartTitle(type);
 		//		if (type instanceof IType)
 		//			setTitleToolTip(((IType)type).getFullyQualifiedName());
 		//		else
 		//			setTitleToolTip(type.getElementName());
 	}
 
 	protected void aboutToLaunch() {
 		String msg = TestUnitMessages.getString("TestRunnerViewPart.message.launching"); //$NON-NLS-1$
 		showInformation(msg);
 		setInfoMessage(msg);
 		//fViewImage= fOriginalViewImage;
 		firePropertyChange(IWorkbenchPart.PROP_TITLE);
 	}
 
 	protected void showInformation(final String info) {
 		postSyncRunnable(new Runnable() {
 
 			public void run() {
 				if (!isDisposed()) fFailureTrace.setInformation(info);
 			}
 		});
 	}
 
 	protected void setInfoMessage(final String message) {
 		fStatus = message;
 	}
 
 	/**
 	 * Stops the currently running test and shuts down the RemoteTestRunner
 	 */
 	public void stopTest() {
 		if (fTestRunnerClient != null) fTestRunnerClient.stopTest();
 		stopUpdateJob();
 	}
 
 	private void stopUpdateJob() {
 		if (fUpdateJob != null) {
 			fUpdateJob.stop();
 			fUpdateJob = null;
 		}
 	}
 
 	public void setAutoScroll(boolean scroll) {
 		fAutoScroll = scroll;
 	}
 
 	public boolean isAutoScroll() {
 		return fAutoScroll;
 	}
 
 	public boolean isCreated() {
 		return fCounterPanel != null;
 	}
 
 	public void reset() {
 		reset(0);
 		setViewPartTitle(null);
 		clearStatus();
 		resetViewIcon();
 	}
 
 	private void clearStatus() {
 		getStatusLine().setMessage(null);
 		getStatusLine().setErrorMessage(null);
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
 			IEditorActionBarContributor contributor = activeEditorPart.getEditorSite().getActionBarContributor();
 			if (contributor instanceof EditorActionBarContributor) return ((EditorActionBarContributor) contributor).getActionBars().getStatusLineManager();
 		}
 		// no active part
 		return getViewSite().getActionBars().getStatusLineManager();
 	}
 
 	private void resetViewIcon() {
 		//fViewImage = fOriginalViewImage;
 		firePropertyChange(IWorkbenchPart.PROP_TITLE);
 	}
 
 	private void setViewPartTitle(IRubyElement type) {
 		String title;
 		if (type == null)
 			title = " "; //$NON-NLS-1$
 		else
 			title = type.toString();
 		setContentDescription(title);
 	}
 
 	private void reset(final int testCount) {
 		postSyncRunnable(new Runnable() {
 
 			public void run() {
 				if (isDisposed()) return;
 				fCounterPanel.reset();
 				fFailureTrace.clear();
 				fProgressBar.reset();
 				//				 TODO enable stop action
 				//fStopAction.setEnabled(true);
 				clearStatus();
 				start(testCount);
 			}
 		});
 		fExecutedTests = 0;
 		fFailureCount = 0;
 		fErrorCount = 0;
 		fTestCount = testCount;
 		aboutToStart();
 		fTestInfos.clear();
 		fFailures = new ArrayList();
 	}
 
 	protected void start(final int total) {
 		resetProgressBar(total);
 		fCounterPanel.setTotal(total);
 		fCounterPanel.setRunValue(0);
 	}
 
 	private void resetProgressBar(final int total) {
 		fProgressBar.reset();
 		fProgressBar.setMaximum(total);
 	}
 
 	private void aboutToStart() {
 		postSyncRunnable(new Runnable() {
 
 			public void run() {
 				if (!isDisposed()) {
 					for (Enumeration e = fTestRunTabs.elements(); e.hasMoreElements();) {
 						TestRunTab v = (TestRunTab) e.nextElement();
 						v.aboutToStart();
 					}
 					// TODO Re-enable actions
 					//fNextAction.setEnabled(false);
 					//fPreviousAction.setEnabled(false);
 				}
 			}
 		});
 	}
 
 	/*
 	 * @see ITestRunListener#testEnded
 	 */
 	public void testEnded(String testId, String testName) {
 		postEndTest(testId, testName);
 		fExecutedTests++;
 	}
 
 	/*
 	 * @see ITestRunListener#testFailed
 	 */
 	public void testFailed(int status, String testId, String testName, String trace) {
 		testFailed(status, testId, testName, trace, null, null);
 	}
 
 	/*
 	 * @see ITestRunListener#testFailed
 	 */
 	public void testFailed(int status, String testId, String testName, String trace, String expected, String actual) {
 		TestRunInfo testInfo = getTestInfo(testId);
 		if (testInfo == null) {
 			testInfo = new TestRunInfo(testId, testName);
 			fTestInfos.put(testName, testInfo);
 		}
 		testInfo.setTrace(trace);
 		testInfo.setStatus(status);
 		if (expected != null) {
 			testInfo.setExpected(expected.substring(0, expected.length() - 1));
 		}
 		if (actual != null) testInfo.setActual(actual.substring(0, actual.length() - 1));
 
 		if (status == ITestRunListener.STATUS_ERROR)
 			fErrorCount++;
 		else
 			fFailureCount++;
 		fFailures.add(testInfo);
 		// show the view on the first error only
 		if (fShowOnErrorOnly && (fErrorCount + fFailureCount == 1)) postShowTestResultsView();
 	}
 
 	protected void postShowTestResultsView() {
 		postSyncRunnable(new Runnable() {
 
 			public void run() {
 				if (isDisposed()) return;
 				showTestResultsView();
 			}
 		});
 	}
 
 	public void showTestResultsView() {
 		IWorkbenchWindow window = getSite().getWorkbenchWindow();
 		IWorkbenchPage page = window.getActivePage();
 		TestUnitView testRunner = null;
 
 		if (page != null) {
 			try { // show the result view
 				testRunner = (TestUnitView) page.findView(TestUnitView.NAME);
 				if (testRunner == null) {
 					IWorkbenchPart activePart = page.getActivePart();
 					testRunner = (TestUnitView) page.showView(TestUnitView.NAME);
 					//restore focus stolen by the creation of the console
 					page.activate(activePart);
 				} else {
 					page.bringToTop(testRunner);
 				}
 			} catch (PartInitException pie) {
 				TestunitPlugin.log(pie);
 			}
 		}
 	}
 
 	/*
 	 * @see ITestRunListener#testReran
 	 */
 	public void testReran(String testId, String className, String testName, int status, String trace) {
 		if (status == ITestRunListener.STATUS_ERROR) {
 			String msg = TestUnitMessages.getFormattedString("TestRunnerViewPart.message.error", new String[] { testName, className}); //$NON-NLS-1$
 			postError(msg);
 		} else if (status == ITestRunListener.STATUS_FAILURE) {
 			String msg = TestUnitMessages.getFormattedString("TestRunnerViewPart.message.failure", new String[] { testName, className}); //$NON-NLS-1$
 			postError(msg);
 		} else {
 			String msg = TestUnitMessages.getFormattedString("TestRunnerViewPart.message.success", new String[] { testName, className}); //$NON-NLS-1$
 			setInfoMessage(msg);
 		}
 		TestRunInfo info = getTestInfo(testId);
 		updateTest(info, status);
 		if (info.getTrace() == null || !info.getTrace().equals(trace)) {
 			info.setTrace(trace);
 			showFailure(info);
 		}
 	}
 
 	protected void postError(final String message) {
 		fStatus = message;
 	}
 
 	private void updateTest(TestRunInfo info, final int status) {
 		if (status == info.getStatus()) return;
 		if (info.getStatus() == ITestRunListener.STATUS_OK) {
 			if (status == ITestRunListener.STATUS_FAILURE)
 				fFailureCount++;
 			else if (status == ITestRunListener.STATUS_ERROR) fErrorCount++;
 		} else if (info.getStatus() == ITestRunListener.STATUS_ERROR) {
 			if (status == ITestRunListener.STATUS_OK)
 				fErrorCount--;
 			else if (status == ITestRunListener.STATUS_FAILURE) {
 				fErrorCount--;
 				fFailureCount++;
 			}
 		} else if (info.getStatus() == ITestRunListener.STATUS_FAILURE) {
 			if (status == ITestRunListener.STATUS_OK)
 				fFailureCount--;
 			else if (status == ITestRunListener.STATUS_ERROR) {
 				fFailureCount--;
 				fErrorCount++;
 			}
 		}
 		info.setStatus(status);
 		final TestRunInfo finalInfo = info;
 		postSyncRunnable(new Runnable() {
 
 			public void run() {
 				for (Enumeration e = fTestRunTabs.elements(); e.hasMoreElements();) {
 					TestRunTab v = (TestRunTab) e.nextElement();
 					v.testStatusChanged(finalInfo);
 				}
 			}
 		});
 
 	}
 
 	public void testReran(String testId, String className, String testName, int statusCode, String trace, String expectedResult, String actualResult) {
 		testReran(testId, className, testName, statusCode, trace);
 		TestRunInfo info = getTestInfo(testId);
 		info.setActual(actualResult);
 		info.setExpected(expectedResult);
 		fFailureTrace.updateEnablement(info);
 	}
 
 	private void postEndTest(final String testId, final String testName) {
 		postSyncRunnable(new Runnable() {
 
 			public void run() {
 				if (isDisposed()) return;
 				handleEndTest();
 				for (Enumeration e = fTestRunTabs.elements(); e.hasMoreElements();) {
 					TestRunTab v = (TestRunTab) e.nextElement();
 					v.endTest(testId);
 				}
 
 				if (fFailureCount + fErrorCount > 0) {
 					// TODO Re-enable actions
 					//fNextAction.setEnabled(true);
 					//fPreviousAction.setEnabled(true);
 				}
 			}
 		});
 	}
 
 	private void handleEndTest() {
 		fProgressBar.step(fFailureCount + fErrorCount);
 		if (fShowOnErrorOnly) {
 			// TODO FIX!
 			//Image progress = fProgressImages.getImage(fExecutedTests,
 			// fTestCount, fErrorCount, fFailureCount);
 			//if (progress != fViewImage) {
 			//fViewImage = progress;
 			firePropertyChange(IWorkbenchPart.PROP_TITLE);
 			//}
 		}
 	}
 
 	/*
 	 * @see ITestRunListener#testStarted
 	 */
 	public void testStarted(String testId, String testName) {
 		postStartTest(testId, testName);
 		// reveal the part when the first test starts
 		if (!fShowOnErrorOnly && fExecutedTests == 1) postShowTestResultsView();
 
 		TestRunInfo testInfo = getTestInfo(testId);
 		if (testInfo == null) {
 			testInfo = new TestRunInfo(testId, testName);
 			fTestInfos.put(testId, testInfo);
 		}
 		String className = testInfo.getClassName();
 		String method = testInfo.getTestMethodName();
 		String status = TestUnitMessages.getFormattedString("TestRunnerViewPart.message.started", new String[] { className, method}); //$NON-NLS-1$
 		setInfoMessage(status);
 	}
 
 	private void postStartTest(final String testId, final String testName) {
 		postSyncRunnable(new Runnable() {
 
 			public void run() {
 				if (isDisposed()) return;
 				for (Enumeration e = fTestRunTabs.elements(); e.hasMoreElements();) {
 					TestRunTab v = (TestRunTab) e.nextElement();
 					v.startTest(testId);
 				}
 			}
 		});
 	}
 
 	/*
 	 * @see ITestRunListener#testRunStopped
 	 */
 	public void testRunStopped(final long elapsedTime) {
 		String msg = TestUnitMessages.getString("TestRunnerViewPart.message.stopped"); //$NON-NLS-1$ 
 		setInfoMessage(msg);
 		handleStopped();
 	}
 
 	private void handleStopped() {
 		postSyncRunnable(new Runnable() {
 
 			public void run() {
 				if (isDisposed()) return;
 				resetViewIcon();
 				//fStopAction.setEnabled(false);
 				fProgressBar.stopped();
 			}
 		});
 		stopUpdateJob();
 	}
 
 	/*
 	 * @see ITestRunListener#testRunEnded
 	 */
 	public void testRunEnded(long elapsedTime) {
 		fExecutedTests--;
 		String[] keys = { elapsedTimeAsString(elapsedTime)};
 		String msg = TestUnitMessages.getFormattedString("TestRunnerViewPart.message.finish", keys); //$NON-NLS-1$
 		if (hasErrorsOrFailures())
 			postError(msg);
 		else
 			setInfoMessage(msg);
 
 		postSyncRunnable(new Runnable() {
 
 			public void run() {
 				if (isDisposed()) return;
 				//fStopAction.setEnabled(lastLaunchIsKeptAlive());
 				if (fFailures.size() > 0) {
 					selectFirstFailure();
 				}
 				//updateViewIcon();
 				//				if (fDirtyListener == null) {
 				//					fDirtyListener = new DirtyListener();
 				//					JavaCore.addElementChangedListener(fDirtyListener);
 				//				}
 				for (Enumeration e = fTestRunTabs.elements(); e.hasMoreElements();) {
 					TestRunTab v = (TestRunTab) e.nextElement();
 					v.aboutToEnd();
 				}
 			}
 		});
 		stopUpdateJob();
 	}
 
 	private String elapsedTimeAsString(long runTime) {
 		return NumberFormat.getInstance().format((double) runTime / 1000);
 	}
 
 	private boolean hasErrorsOrFailures() {
 		return fErrorCount + fFailureCount > 0;
 	}
 
 	protected void selectFirstFailure() {
 		TestRunInfo firstFailure = (TestRunInfo) fFailures.get(0);
 		if (firstFailure != null && fAutoScroll) {
 			fActiveRunTab.setSelectedTest(firstFailure.getTestId());
 			handleTestSelected(firstFailure.getTestId());
 		}
 	}
 
 	/*
 	 * @see ITestRunListener#testRunTerminated
 	 */
 	public void testRunTerminated() {
 		String msg = TestUnitMessages.getString("TestRunnerViewPart.message.terminated"); //$NON-NLS-1$
 		showMessage(msg);
 		handleStopped();
 	}
 
 	private void showMessage(String msg) {
 		postError(msg);
 	}
 
 	/*
 	 * @see ITestRunListener#testRunStarted(testCount)
 	 */
 	public void testRunStarted(final int testCount) {
 		reset(testCount);
 		//fShowOnErrorOnly = JUnitPreferencePage.getShowOnErrorOnly();
 		fExecutedTests++;
 		stopUpdateJob();
 		fUpdateJob = new UpdateUIJob(TestUnitMessages.getString("TestRunnerViewPart.jobName")); //$NON-NLS-1$  
 		fUpdateJob.schedule(REFRESH_INTERVAL);
 	}
 
 	private void refreshCounters() {
 		fCounterPanel.setErrorValue(fErrorCount);
 		fCounterPanel.setFailureValue(fFailureCount);
 		fCounterPanel.setRunValue(fExecutedTests);
 		fProgressBar.refresh(fErrorCount + fFailureCount > 0);
 	}
 
 	protected void doShowStatus() {
 		setContentDescription(fStatus);
 	}
 
 	/*
 	 * @see ITestRunListener2#testTreeEntry
 	 */
 	public void testTreeEntry(final String treeEntry) {
 		postSyncRunnable(new Runnable() {
 
 			public void run() {
 				if (isDisposed()) return;
 				for (Enumeration e = fTestRunTabs.elements(); e.hasMoreElements();) {
 					TestRunTab v = (TestRunTab) e.nextElement();
 					v.newTreeEntry(treeEntry);
 				}
 			}
 		});
 	}
 
 	/**
 	 * Stops the currently running test and shuts down the RemoteTestRunner
 	 */
 	public void rerunTestRun() {
 		if (lastLaunchIsKeptAlive()) {
 			// prompt for terminating the existing run
 			if (MessageDialog.openQuestion(getSite().getShell(), TestUnitMessages.getString("TestRunnerViewPart.terminate.title"), TestUnitMessages.getString("TestRunnerViewPart.terminate.message"))) { //$NON-NLS-1$ //$NON-NLS-2$
 				if (fTestRunnerClient != null) fTestRunnerClient.stopTest();
 			}
 		}
 		if (fLastLaunch != null && fLastLaunch.getLaunchConfiguration() != null) {
 			DebugUITools.launch(fLastLaunch.getLaunchConfiguration(), fLastLaunch.getLaunchMode());
 		}
 	}
 	
 	public IRubyProject getLaunchedProject() {
 		return fTestProject;
 	}
 
 	class UpdateUIJob extends UIJob {
 
 		private boolean fRunning = true;
 
 		public UpdateUIJob(String name) {
 			super(name);
 			setSystem(true);
 		}
 
 		public IStatus runInUIThread(IProgressMonitor monitor) {
 			if (!isDisposed()) {
 				doShowStatus();
 				refreshCounters();
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
 
 	private class RerunLastAction extends Action {
 
 		public RerunLastAction() {
 			setText(TestUnitMessages.getString("TestRunnerViewPart.rerunaction.label")); //$NON-NLS-1$
 			setToolTipText(TestUnitMessages.getString("TestRunnerViewPart.rerunaction.tooltip")); //$NON-NLS-1$
 			setDisabledImageDescriptor(TestunitPlugin.getImageDescriptor("dlcl16/relaunch.gif")); //$NON-NLS-1$
 			setHoverImageDescriptor(TestunitPlugin.getImageDescriptor("elcl16/relaunch.gif")); //$NON-NLS-1$
 			setImageDescriptor(TestunitPlugin.getImageDescriptor("elcl16/relaunch.gif")); //$NON-NLS-1$
 		}
 
 		public void run() {
 			rerunTestRun();
 		}
 	}
 
 	public ILaunch getLastLaunch() {
 		return fLastLaunch;
 	}
 
 }
