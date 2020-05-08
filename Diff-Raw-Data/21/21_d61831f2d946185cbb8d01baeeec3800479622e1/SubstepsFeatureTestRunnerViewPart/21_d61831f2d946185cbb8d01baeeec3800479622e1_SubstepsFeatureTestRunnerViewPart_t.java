 package com.technophobia.substeps.junit.ui;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.List;
 
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.debug.ui.IDebugUIConstants;
 import org.eclipse.jdt.internal.junit.ui.ProgressImages;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IMenuListener;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.IStatusLineManager;
 import org.eclipse.jface.action.IToolBarManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CLabel;
 import org.eclipse.swt.custom.SashForm;
 import org.eclipse.swt.custom.ViewForm;
 import org.eclipse.swt.dnd.Clipboard;
 import org.eclipse.swt.dnd.DND;
 import org.eclipse.swt.dnd.DropTarget;
 import org.eclipse.swt.dnd.DropTargetAdapter;
 import org.eclipse.swt.dnd.DropTargetEvent;
 import org.eclipse.swt.dnd.TextTransfer;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.events.ControlEvent;
 import org.eclipse.swt.events.ControlListener;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Layout;
 import org.eclipse.swt.widgets.ToolBar;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.IEditorActionBarContributor;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IMemento;
 import org.eclipse.ui.IPropertyListener;
 import org.eclipse.ui.IViewPart;
 import org.eclipse.ui.IViewSite;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.IWorkbenchPartSite;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.actions.ActionFactory;
 import org.eclipse.ui.handlers.IHandlerService;
 import org.eclipse.ui.part.EditorActionBarContributor;
 import org.eclipse.ui.part.PageSwitcher;
 import org.eclipse.ui.part.ViewPart;
 import org.eclipse.ui.progress.IWorkbenchSiteProgressService;
 import org.eclipse.ui.progress.UIJob;
 import org.eclipse.ui.statushandlers.StatusManager;
 
 import com.technophobia.eclipse.transformer.Callback;
 import com.technophobia.eclipse.transformer.Supplier;
 import com.technophobia.eclipse.ui.Notifier;
 import com.technophobia.eclipse.ui.NotifyingUiUpdater;
 import com.technophobia.eclipse.ui.Renderer;
 import com.technophobia.eclipse.ui.UiUpdater;
 import com.technophobia.eclipse.ui.job.StoppableJob;
 import com.technophobia.eclipse.ui.part.DefaultVisibilityPartMonitor;
 import com.technophobia.eclipse.ui.part.PartMonitor;
 import com.technophobia.eclipse.ui.render.OneTimeRenderUpdater;
 import com.technophobia.eclipse.ui.view.ViewLayout;
 import com.technophobia.eclipse.ui.view.ViewOrientation;
 import com.technophobia.substeps.FeatureRunnerPlugin;
 import com.technophobia.substeps.junit.action.ActivateOnErrorAction;
 import com.technophobia.substeps.junit.action.FailuresOnlyFilterAction;
 import com.technophobia.substeps.junit.action.RerunFailedFirstAction;
 import com.technophobia.substeps.junit.action.RerunLastTestAction;
 import com.technophobia.substeps.junit.action.ScrollLockAction;
 import com.technophobia.substeps.junit.action.ShowNextFailureAction;
 import com.technophobia.substeps.junit.action.ShowPreviousFailureAction;
 import com.technophobia.substeps.junit.action.ShowTestHierarchyAction;
 import com.technophobia.substeps.junit.action.ShowTimeAction;
 import com.technophobia.substeps.junit.action.StopTestAction;
 import com.technophobia.substeps.junit.action.SubstepsCopyAction;
 import com.technophobia.substeps.junit.action.SubstepsPasteAction;
 import com.technophobia.substeps.junit.action.ToggleOrientationAction;
 import com.technophobia.substeps.junit.ui.component.FeatureViewer;
 import com.technophobia.substeps.junit.ui.component.ProgressBar;
 import com.technophobia.substeps.junit.ui.component.SubstepsCounterPanel;
 import com.technophobia.substeps.junit.ui.handler.SubstepsHandlerServiceManager;
 import com.technophobia.substeps.junit.ui.help.SubstepsHelpContextIds;
 import com.technophobia.substeps.junit.ui.job.ProcessRunningJob;
 import com.technophobia.substeps.junit.ui.job.UpdateJobManager;
 import com.technophobia.substeps.junit.ui.job.UpdateSubstepsUIJob;
 import com.technophobia.substeps.junit.ui.progress.SubstepsProgressBar;
 import com.technophobia.substeps.junit.ui.testsession.JunitTestResultsView;
 import com.technophobia.substeps.junit.ui.testsession.TestResultsView;
 import com.technophobia.substeps.junit.ui.viewhistory.RunnerViewHistory;
 import com.technophobia.substeps.model.SubstepsSessionListener;
 import com.technophobia.substeps.model.structure.Status;
 import com.technophobia.substeps.model.structure.SubstepsTestElement;
 import com.technophobia.substeps.model.structure.SubstepsTestLeafElement;
 import com.technophobia.substeps.preferences.PreferencesConstants;
 
 @SuppressWarnings("restriction")
 public class SubstepsFeatureTestRunnerViewPart extends ViewPart implements UpdateJobManager, Notifier<Runnable>,
         Callback, IPropertyListener {
 
     public static final String NAME = "com.technophobia.substeps.runner.SubstepsResultView";
 
     private static final IMarker FAMILY_SUBSTEPS_FEATURE_RUN = new IMarker() {
     };
 
     private static final String RERUN_LAST_COMMAND = "com.technophobia.substeps.junit.rerunLast"; //$NON-NLS-1$
     private static final String RERUN_FAILED_FIRST_COMMAND = "com.technophobia.substeps.junit.rerunFailedFirst"; //$NON-NLS-1$
 
     private static final String TAG_SCROLL = "scroll";
     private static final String TAG_RATIO = "ratio";
     private static final String TAG_ORIENTATION = "orientation";
     private static final String TAG_FAILURES_ONLY = "failuresOnly";
     private static final String TAG_LAYOUT = "layout";
     private static final String TAG_SHOW_TIME = "time";
 
     private static final int REFRESH_INTERVAL = 200;
 
     private SubstepsIconProvider iconProvider;
     private ProgressImages progressImages;
 
     private IMemento memento;
     private SashForm sashForm;
     private ScrollLockAction scrollLockAction;
     private ToggleOrientationAction[] toggleOrientationActions;
     private Action showTestHierarchyAction;
 
     private ViewOrientation orientation = ViewOrientation.VIEW_ORIENTATION_AUTOMATIC;
     private ViewOrientation currentOrientation;
     private ViewLayout layout = ViewLayout.HIERARCHICAL;
 
     private Action failuresOnlyFilterAction;
     private Action showTimeAction;
 
     private UIJob updateJob;
     private StoppableJob substepsIsRunningJob;
 
     private Supplier<Boolean> disposedChecker;
 
     private UiUpdater uiUpdater;
     private UiUpdater testCounterUpdater;
     private UiUpdater viewTitleUiUpdater;
     private UiUpdater toolbarUiUpdater;
     private UiUpdater statusMessageUpdater;
 
     private final NotifyingUiUpdater<String> infoMessageUpdater;
 
     private FailureTrace failureTrace;
 
     private Supplier<TestRunStats> testRunStatsSupplier;
     private SubstepsRunSessionManager sessionManager;
 
     private SubstepsRunSessionListener testRunSessionListener;
     private SubstepsHandlerServiceManager handlerServiceManager;
     private TestResultsView testResultsView;
     private FeatureViewer testViewer;
 
     private PartMonitor partMonitor;
 
     private com.technophobia.substeps.junit.ui.component.CounterPanel counterPanel;
     private Clipboard clipboard;
 
     private DirtyListener dirtyListener;
     private IMenuListener viewMenuListener;
 
     private boolean autoScroll = false;
     private boolean disposed = false;
     private boolean showOnErrorOnly = false;
 
     private Composite parent;
 
     private RunnerViewHistory viewHistory;
     private SubstepsActionManager actionManager;
 
     private Composite counterComposite;
 
     private ProgressBar progressBar;
 
     private Image originalViewImage;
     private Image viewImage;
 
 
     public SubstepsFeatureTestRunnerViewPart() {
         this.disposedChecker = new Supplier<Boolean>() {
 
             @Override
             public Boolean get() {
                 return isDisposed();
             }
         };
         this.iconProvider = new SubstepsIconProvider(new ImageDescriptorImageLoader(), new ImageDescriptorLoader());
 
         this.infoMessageUpdater = new OneTimeRenderUpdater<String>(new Renderer<String>() {
             @Override
             public void render(final String t) {
                 setContentDescription(t);
             }
         });
 
     }
 
 
     @Override
     public void init(final IViewSite site, final IMemento memento) throws PartInitException {
         super.init(site, memento);
         this.memento = memento;
         this.partMonitor = new DefaultVisibilityPartMonitor(getSite());
         this.dirtyListener = new JavaCoreDirtyListener();
         this.currentOrientation = ViewOrientation.VIEW_ORIENTATION_AUTOMATIC;
         this.testRunStatsSupplier = testRunStatsSupplier();
 
         final IWorkbenchSiteProgressService progressService = getProgressService();
         if (progressService != null) {
             progressService.showBusyForFamily(FAMILY_SUBSTEPS_FEATURE_RUN);
         }
     }
 
 
     private SubstepsActionManager createActionManager() {
         final Action stopAction = new StopTestAction(sessionManager, infoMessageUpdater, iconProvider);
         final Action copyAction = new SubstepsCopyAction(getSite().getShell(), failureTrace, clipboard);
         final Action rerunFailedFirstAction = new RerunFailedFirstAction(RERUN_FAILED_FIRST_COMMAND,
                 new FailedTestFirstTestRelauncher(sessionManager, getSite().getShell(), infoMessageUpdater),
                 iconProvider);
         final Action rerunLastTestAction = new RerunLastTestAction(RERUN_LAST_COMMAND, new TestRelauncher(
                 sessionManager, getSite().getShell(), infoMessageUpdater), iconProvider);
         final Action nextAction = new ShowNextFailureAction(testViewer, iconProvider);
         final Action prevAction = new ShowPreviousFailureAction(testViewer, iconProvider);
         return new SubstepsActionManager(stopAction, copyAction, rerunFailedFirstAction, rerunLastTestAction,
                 nextAction, prevAction);
     }
 
 
     @Override
     public void saveState(final IMemento memento) {
         if (sashForm == null && memento != null) { // Keep the old state;
             memento.putMemento(this.memento);
             return;
         }
 
         decorateMemento(memento);
     }
 
 
     /**
      * Stops the currently running test and shuts down the RemoteTestRunner
      */
     public void rerunTestRun() {
         new TestRelauncher(sessionManager, getSite().getShell(), infoMessageUpdater).run();
     }
 
 
     public void rerunTestFailedFirst() {
         new FailedTestFirstTestRelauncher(sessionManager, getSite().getShell(), infoMessageUpdater).run();
     }
 
 
     public void setAutoScroll(final boolean scroll) {
         autoScroll = scroll;
     }
 
 
     public boolean isAutoScroll() {
         return autoScroll;
     }
 
 
     @Override
     public synchronized void dispose() {
         disposed = true;
         if (testRunSessionListener != null)
             FeatureRunnerPlugin.instance().getModel().removeTestRunSessionListener(testRunSessionListener);
 
         handlerServiceManager.deactivateHandlers();
         sessionManager.setActiveState(null);
 
         if (progressImages != null)
             progressImages.dispose();
         getViewSite().getPage().removePartListener(partMonitor);
 
         disposeImages();
         if (clipboard != null)
             clipboard.dispose();
         if (viewMenuListener != null) {
             getViewSite().getActionBars().getMenuManager().removeMenuListener(viewMenuListener);
         }
         dirtyListener.removeDirtyListener();
     }
 
 
     @Override
     public void setFocus() {
         if (testViewer != null)
             testViewer.getTestViewerControl().setFocus();
     }
 
 
     @Override
     public void createPartControl(final Composite parent) {
         this.parent = parent;
         addResizeListener(parent);
         this.clipboard = new Clipboard(parent.getDisplay());
 
         this.actionManager = createActionManager();
         this.testResultsView = new JunitTestResultsView(getSite().getWorkbenchWindow());
 
         final GridLayout gridLayout = new GridLayout();
         gridLayout.marginWidth = 0;
         gridLayout.marginHeight = 0;
         parent.setLayout(gridLayout);
 
         final UiUpdater tooltipUpdater = new TooltipUpdater(tooltipNotifier());
         final UiUpdater statusMessageUpdater = new StatusMessageUiUpdater(getViewSite());
        this.counterComposite = createProgressCountPanel(parent);
         final SashForm sashForm = createSashForm(parent);
 
         this.sessionManager = new SubstepsRunSessionManager(disposedSashFormChecker(), testViewer, tooltipUpdater,
                 infoMessageUpdater, viewTitleUiUpdater, statusMessageUpdater, failureTrace, testResultsView,
                 actionManager, this, testSessionListenerSupplier());
         this.handlerServiceManager = new SubstepsHandlerServiceManager((IHandlerService) getSite().getWorkbenchWindow()
                 .getService(IHandlerService.class));
 
         this.counterComposite.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
         sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
 
         final IActionBars actionBars = getViewSite().getActionBars();
 
         final Action copyAction = new SubstepsCopyAction(parent.getShell(), failureTrace, clipboard);
         copyAction.setActionDefinitionId(ActionFactory.COPY.getCommandId());
         actionBars.setGlobalActionHandler(ActionFactory.COPY.getId(), copyAction);
 
         final Action pasteAction = new SubstepsPasteAction(parent.getShell(), clipboard);
         pasteAction.setActionDefinitionId(ActionFactory.PASTE.getCommandId());
         actionBars.setGlobalActionHandler(ActionFactory.PASTE.getId(), pasteAction);
 
         this.viewHistory = new RunnerViewHistory(sessionManager, parent.getShell(), sessionManager,
                 new ImageDescriptorLoader(), iconProvider, pasteAction);
         configureToolBar();
 
         initPageSwitcher();
         addDropAdapter(parent);
 
         originalViewImage = getTitleImage();
         progressImages = new ProgressImages();
         PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, SubstepsHelpContextIds.RESULTS_VIEW);
 
         getViewSite().getPage().addPartListener(partMonitor);
 
         setFilterAndLayout(false, ViewLayout.HIERARCHICAL.value());
         setShowExecutionTime(true);
         if (memento != null) {
             restoreLayoutState(memento);
         }
         memento = null;
 
         testRunSessionListener = new DefaultSubstepsRunSessionListener(displaySupplier(), siteSupplier(),
                 infoMessageUpdater, sessionManager);
         FeatureRunnerPlugin.instance().getModel().addTestRunSessionListener(testRunSessionListener);
 
         // always show youngest test run in view. simulate "sessionAdded" event
         // to do that
         final List<SubstepsRunSession> testRunSessions = FeatureRunnerPlugin.instance().getModel().getTestRunSessions();
         if (!testRunSessions.isEmpty()) {
             testRunSessionListener.sessionAdded(testRunSessions.get(0));
         }
 
         this.testCounterUpdater = new TestCounterUiUpdater(testRunStatsSupplier, counterPanel, progressBar);
         this.viewTitleUiUpdater = new ViewTitleUiUpdater(partMonitor, sessionManager, testRunStatsSupplier,
                 iconProvider, originalViewImage, progressImages, this);
         this.uiUpdater = new SubstepsFeatureUiUpdater(disposedChecker, infoMessageUpdater, testCounterUpdater,
                 viewTitleUiUpdater, toolbarUiUpdater, testViewer, testRunStatsSupplier);
 
     }
 
 
     private void decorateMemento(final IMemento memento) {
         memento.putString(TAG_SCROLL, scrollLockAction.isChecked() ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
         final int weigths[] = sashForm.getWeights();
         final int ratio = (weigths[0] * 1000) / (weigths[0] + weigths[1]);
         memento.putInteger(TAG_RATIO, ratio);
         memento.putInteger(TAG_ORIENTATION, orientation.value());
 
         memento.putString(TAG_FAILURES_ONLY, failuresOnlyFilterAction.isChecked() ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
         memento.putInteger(TAG_LAYOUT, layout.value());
         memento.putString(TAG_SHOW_TIME, showTimeAction.isChecked() ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
     }
 
 
     @Override
     public void startUpdateJobs() {
         postSyncProcessChanges();
 
         if (updateJob != null) {
             return;
         }
         substepsIsRunningJob = new ProcessRunningJob(
                 SubstepsFeatureMessages.SubstepsFeatureTestRunnerViewPart_wrapperJobName, FAMILY_SUBSTEPS_FEATURE_RUN,
                 Job.getJobManager().newLock());
         // acquire lock while a test run is running
         // the lock is released when the test run terminates
         // the wrapper job will wait on this lock.
 
         getProgressService().schedule((Job) substepsIsRunningJob);
 
         updateJob = new UpdateSubstepsUIJob(SubstepsFeatureMessages.SubstepsFeatureTestRunnerViewPart_jobName,
                 REFRESH_INTERVAL, uiUpdater);
         updateJob.schedule(REFRESH_INTERVAL);
     }
 
 
     @Override
     public void stopUpdateJobs() {
         if (updateJob != null) {
             ((StoppableJob) updateJob).stop();
             updateJob = null;
         }
         if (substepsIsRunningJob != null && substepsIsRunningJob != null) {
             substepsIsRunningJob.stop();
             substepsIsRunningJob = null;
         }
         postSyncProcessChanges();
     }
 
 
     @Override
     public void notify(final Runnable r) {
         if (!isDisposed())
             getDisplay().syncExec(r);
     }
 
 
     @Override
     public Runnable currentValue() {
         return null;
     }
 
 
     @Override
     public void callback() {
         warnOfContentChange();
     }
 
 
     @Override
     public void propertyChanged(final Object source, final int propId) {
         firePropertyChange(propId);
     }
 
 
     private IWorkbenchSiteProgressService getProgressService() {
         final IWorkbenchSiteProgressService siteService = (IWorkbenchSiteProgressService) getSite().getAdapter(
                 IWorkbenchSiteProgressService.class);
         if (siteService != null) {
             return siteService;
         }
         return null;
     }
 
 
     private void restoreLayoutState(final IMemento memento) {
         final Integer ratio = memento.getInteger(TAG_RATIO);
         if (ratio != null)
             sashForm.setWeights(new int[] { ratio.intValue(), 1000 - ratio.intValue() });
         final Integer newOrientation = memento.getInteger(TAG_ORIENTATION);
         if (newOrientation != null)
             orientation = ViewOrientation.forValue(newOrientation.intValue());
 
         computeOrientation();
         final String scrollLock = memento.getString(TAG_SCROLL);
         if (scrollLock != null) {
             scrollLockAction.setChecked(scrollLock.equals("true")); //$NON-NLS-1$
             setAutoScroll(!scrollLockAction.isChecked());
         }
 
         final Integer layout = memento.getInteger(TAG_LAYOUT);
         ViewLayout layoutValue = ViewLayout.HIERARCHICAL;
         if (layout != null)
             layoutValue = ViewLayout.forValue(layout.intValue());
 
         final String failuresOnly = memento.getString(TAG_FAILURES_ONLY);
         boolean showFailuresOnly = false;
         if (failuresOnly != null)
             showFailuresOnly = failuresOnly.equals("true"); //$NON-NLS-1$
 
         final String time = memento.getString(TAG_SHOW_TIME);
         boolean showTime = true;
         if (time != null)
             showTime = time.equals("true"); //$NON-NLS-1$
 
         setFilterAndLayout(showFailuresOnly, layoutValue.value());
         setShowExecutionTime(showTime);
     }
 
 
     private void disposeImages() {
         iconProvider.dispose();
     }
 
 
     private SashForm createSashForm(final Composite parent) {
         sashForm = new SashForm(parent, SWT.VERTICAL);
 
         final ViewForm top = new ViewForm(sashForm, SWT.NONE);
         this.testViewer = new FeatureViewer(top, siteSupplier(), failedTestNotifier(), testRerunner(),
                 autoScrollNotifier(), testKindDisplayNameSupplier(), iconProvider);
 
         final Composite empty = new Composite(top, SWT.NONE);
         empty.setLayout(new Layout() {
             @Override
             protected Point computeSize(final Composite composite, final int wHint, final int hHint,
                     final boolean flushCache) {
                 return new Point(1, 1); // (0, 0) does not work with
                                         // super-intelligent ViewForm
             }
 
 
             @Override
             protected void layout(final Composite composite, final boolean flushCache) {
             }
         });
         top.setTopLeft(empty); // makes ViewForm draw the horizontal separator
                                // line ...
         top.setContent(testViewer.getTestViewerControl());
 
         final ViewForm bottom = new ViewForm(sashForm, SWT.NONE);
 
         final CLabel label = new CLabel(bottom, SWT.NONE);
         label.setText(SubstepsFeatureMessages.SubstepsFeatureTestRunnerViewPart_label_failure);
         label.setImage(iconProvider.imageFor(SubstepsIcon.StackView));
         bottom.setTopLeft(label);
         final ToolBar failureToolBar = new ToolBar(bottom, SWT.FLAT | SWT.WRAP);
         bottom.setTopCenter(failureToolBar);
         failureTrace = new FailureTrace(bottom, clipboard, failureToolBar, iconProvider);
         bottom.setContent(failureTrace.getComposite());
 
         sashForm.setWeights(new int[] { 50, 50 });
         return sashForm;
     }
 
 
     private void addDropAdapter(final Composite parent) {
         final DropTarget dropTarget = new DropTarget(parent, DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK
                 | DND.DROP_DEFAULT);
         dropTarget.setTransfer(new Transfer[] { TextTransfer.getInstance() });
         class DropAdapter extends DropTargetAdapter {
             @Override
             public void dragEnter(final DropTargetEvent event) {
                 event.detail = DND.DROP_COPY;
                 event.feedback = DND.FEEDBACK_NONE;
             }
 
 
             @Override
             public void dragOver(final DropTargetEvent event) {
                 event.detail = DND.DROP_COPY;
                 event.feedback = DND.FEEDBACK_NONE;
             }
 
 
             @Override
             public void dragOperationChanged(final DropTargetEvent event) {
                 event.detail = DND.DROP_COPY;
                 event.feedback = DND.FEEDBACK_NONE;
             }
 
 
             @Override
             public void drop(final DropTargetEvent event) {
                 if (TextTransfer.getInstance().isSupportedType(event.currentDataType)) {
                     final String url = (String) event.data;
                     importTestRunSession(url);
                 }
             }
         }
         dropTarget.addDropListener(new DropAdapter());
     }
 
 
     private void initPageSwitcher() {
         @SuppressWarnings("unused")
         final PageSwitcher pageSwitcher = new PageSwitcher(this) {
             @Override
             public Object[] getPages() {
                 return viewHistory.getHistoryEntries().toArray();
             }
 
 
             @Override
             public String getName(final Object page) {
                 return viewHistory.getText((SubstepsRunSession) page);
             }
 
 
             @Override
             public ImageDescriptor getImageDescriptor(final Object page) {
                 return viewHistory.getImageDescriptor(page);
             }
 
 
             @Override
             public void activatePage(final Object page) {
                 viewHistory.setActiveEntry((SubstepsRunSession) page);
             }
 
 
             @Override
             public int getCurrentPageIndex() {
                 return viewHistory.getHistoryEntries().indexOf(viewHistory.getCurrentEntry());
             }
         };
     }
 
 
     private void addResizeListener(final Composite parent) {
         parent.addControlListener(new ControlListener() {
             @Override
             public void controlMoved(final ControlEvent e) {
             }
 
 
             @Override
             public void controlResized(final ControlEvent e) {
                 computeOrientation();
             }
         });
     }
 
 
     void computeOrientation() {
         if (orientation != ViewOrientation.VIEW_ORIENTATION_AUTOMATIC) {
             currentOrientation = orientation;
             setOrientation(currentOrientation.value());
         } else {
             final Point size = parent.getSize();
             if (size.x != 0 && size.y != 0) {
                 if (size.x > size.y)
                     setOrientation(ViewOrientation.VIEW_ORIENTATION_HORIZONTAL.value());
                 else
                     setOrientation(ViewOrientation.VIEW_ORIENTATION_VERTICAL.value());
             }
         }
     }
 
 
     private void configureToolBar() {
         final IActionBars actionBars = getViewSite().getActionBars();
         final IToolBarManager toolBar = actionBars.getToolBarManager();
         final IMenuManager viewMenu = actionBars.getMenuManager();
 
         actionManager.setNextActionEnabled(false);
         actionBars.setGlobalActionHandler(ActionFactory.NEXT.getId(), actionManager.nextAction());
 
         actionManager.setPrevActionEnabled(false);
         actionBars.setGlobalActionHandler(ActionFactory.PREVIOUS.getId(), actionManager.prevAction());
 
         actionManager.setStopActionEnabled(false);
 
         handlerServiceManager.activateHandlers(RERUN_LAST_COMMAND, new AbstractHandler() {
             @Override
             public Object execute(final ExecutionEvent event) throws ExecutionException {
                 actionManager.rerunLastTestAction().run();
                 return null;
             }
 
 
             @Override
             public boolean isEnabled() {
                 return actionManager.rerunLastTestAction().isEnabled();
             }
         });
 
         handlerServiceManager.activateHandlers(RERUN_FAILED_FIRST_COMMAND, new AbstractHandler() {
             @Override
             public Object execute(final ExecutionEvent event) throws ExecutionException {
                 actionManager.rerunFailedFirstAction().run();
                 return null;
             }
 
 
             @Override
             public boolean isEnabled() {
                 return actionManager.rerunFailedFirstAction().isEnabled();
             }
         });
 
         failuresOnlyFilterAction = new FailuresOnlyFilterAction(showFailuresOnlyNotifier(), iconProvider);
 
         scrollLockAction = new ScrollLockAction(autoScrollNotifier(), iconProvider);
         scrollLockAction.setChecked(!autoScroll);
 
         final Notifier<ViewOrientation> viewOrientationNotifier = viewOrientationNotifier();
         toggleOrientationActions = new ToggleOrientationAction[] {
                 new ToggleOrientationAction(ViewOrientation.VIEW_ORIENTATION_VERTICAL, viewOrientationNotifier,
                         iconProvider),
                 new ToggleOrientationAction(ViewOrientation.VIEW_ORIENTATION_HORIZONTAL, viewOrientationNotifier,
                         iconProvider),
                 new ToggleOrientationAction(ViewOrientation.VIEW_ORIENTATION_AUTOMATIC, viewOrientationNotifier,
                         iconProvider) };
 
         showTestHierarchyAction = new ShowTestHierarchyAction(layoutModeNotifier(), iconProvider);
         showTimeAction = new ShowTimeAction(testViewer);
 
         toolBar.add(actionManager.nextAction());
         toolBar.add(actionManager.prevAction());
         toolBar.add(failuresOnlyFilterAction);
         toolBar.add(scrollLockAction);
         toolBar.add(new Separator());
         toolBar.add(actionManager.rerunLastTestAction());
         toolBar.add(actionManager.rerunFailedFirstAction());
         toolBar.add(actionManager.stopAction());
         toolBar.add(viewHistory.createHistoryDropDownAction());
 
         viewMenu.add(showTestHierarchyAction);
         viewMenu.add(showTimeAction);
         viewMenu.add(new Separator());
 
         final MenuManager layoutSubMenu = new MenuManager(
                 SubstepsFeatureMessages.SubstepsFeatureTestRunnerViewPart_layout_menu);
         for (int i = 0; i < toggleOrientationActions.length; ++i) {
             layoutSubMenu.add(toggleOrientationActions[i]);
         }
         viewMenu.add(layoutSubMenu);
         viewMenu.add(new Separator());
 
         viewMenu.add(failuresOnlyFilterAction);
 
         final ActivateOnErrorAction activateOnErrorAction = new ActivateOnErrorAction(showOnErrorOnly,
                 showOnErrorOnlyNotifier());
         viewMenu.add(activateOnErrorAction);
         viewMenuListener = new IMenuListener() {
             @Override
             public void menuAboutToShow(final IMenuManager manager) {
                 activateOnErrorAction.update(showOnErrorOnly);
             }
         };
 
         viewMenu.addMenuListener(viewMenuListener);
 
         actionBars.updateActionBars();
     }
 
 
     private IStatusLineManager getStatusLine() {
         // we want to show messages globally hence we
         // have to go through the active part
         final IViewSite site = getViewSite();
         final IWorkbenchPage page = site.getPage();
         final IWorkbenchPart activePart = page.getActivePart();
 
         if (activePart instanceof IViewPart) {
             final IViewPart activeViewPart = (IViewPart) activePart;
             final IViewSite activeViewSite = activeViewPart.getViewSite();
             return activeViewSite.getActionBars().getStatusLineManager();
         }
 
         if (activePart instanceof IEditorPart) {
             final IEditorPart activeEditorPart = (IEditorPart) activePart;
             final IEditorActionBarContributor contributor = activeEditorPart.getEditorSite().getActionBarContributor();
             if (contributor instanceof EditorActionBarContributor)
                 return ((EditorActionBarContributor) contributor).getActionBars().getStatusLineManager();
         }
         // no active part
         return getViewSite().getActionBars().getStatusLineManager();
     }
 
 
     protected Composite createProgressCountPanel(final Composite parent) {
         final Composite composite = new Composite(parent, SWT.NONE);
         final GridLayout layout = new GridLayout();
         composite.setLayout(layout);
         setCounterColumns(layout);
 
         counterPanel = new SubstepsCounterPanel(composite, iconProvider);
         counterPanel.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
         progressBar = new SubstepsProgressBar(composite);
         ((Composite) progressBar)
                 .setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
         return composite;
     }
 
 
     private void showFailure(final SubstepsTestElement test) {
         notify(new Runnable() {
             @Override
             public void run() {
                 if (!isDisposed())
                     failureTrace.showFailure(test);
             }
         });
     }
 
 
     private boolean isDisposed() {
         return disposed || counterPanel.isDisposed();
     }
 
 
     private Display getDisplay() {
         return getViewSite().getShell().getDisplay();
     }
 
 
     /*
      * @see IWorkbenchPart#getTitleImage()
      */
     @Override
     public Image getTitleImage() {
         if (originalViewImage == null)
             originalViewImage = super.getTitleImage();
 
         if (viewImage == null)
             return super.getTitleImage();
         return viewImage;
     }
 
 
     void codeHasChanged() {
         dirtyListener.removeDirtyListener();
 
         if (viewImage == iconProvider.imageFor(SubstepsIcon.TestRunOK))
             viewImage = iconProvider.imageFor(SubstepsIcon.TestRunOKDirty);
         else if (viewImage == iconProvider.imageFor(SubstepsIcon.TestRunFail))
             viewImage = iconProvider.imageFor(SubstepsIcon.TestRunFailDirty);
 
         final Runnable r = new Runnable() {
             @Override
             public void run() {
                 if (isDisposed())
                     return;
                 firePropertyChange(IWorkbenchPart.PROP_TITLE);
             }
         };
         if (!isDisposed())
             getDisplay().asyncExec(r);
     }
 
 
     public void rerunTest(final String testId, final String className, final String testName, final String launchMode) {
         final boolean buildBeforeLaunch = Platform.getPreferencesService().getBoolean(IDebugUIConstants.PLUGIN_ID,
                 IDebugUIConstants.PREF_BUILD_BEFORE_LAUNCH, false, null);
         try {
             final boolean couldLaunch = sessionManager.get().rerunTest(testId, className, testName, launchMode,
                     buildBeforeLaunch);
             if (!couldLaunch) {
                 MessageDialog.openInformation(getSite().getShell(),
                         SubstepsFeatureMessages.SubstepsFeatureTestRunnerViewPart_cannotrerun_title,
                         SubstepsFeatureMessages.SubstepsFeatureTestRunnerViewPart_cannotrerurn_message);
             } else if (sessionManager.get().isKeptAlive()) {
                 final SubstepsTestLeafElement testCaseElement = (SubstepsTestLeafElement) sessionManager.get()
                         .getTestElement(testId);
                 testCaseElement.setStatus(Status.RUNNING, null, null, null);
                 testViewer.registerViewerUpdate(testCaseElement);
                 postSyncProcessChanges();
             }
 
         } catch (final CoreException e) {
             ErrorDialog.openError(getSite().getShell(),
                     SubstepsFeatureMessages.SubstepsFeatureTestRunnerViewPart_error_cannotrerun, e.getMessage(),
                     e.getStatus());
         }
     }
 
 
     private void postSyncProcessChanges() {
         notify(new Runnable() {
             @Override
             public void run() {
                 uiUpdater.doUpdate();
             }
         });
     }
 
 
     public void warnOfContentChange() {
         final IWorkbenchSiteProgressService service = getProgressService();
         if (service != null)
             service.warnOfContentChange();
     }
 
 
     public boolean lastLaunchIsKeptAlive() {
         final SubstepsRunSession subsepsRunSession = sessionManager.get();
         return subsepsRunSession != null && subsepsRunSession.isKeptAlive();
     }
 
 
     private void setOrientation(final int orientation) {
         if ((sashForm == null) || sashForm.isDisposed())
             return;
         final boolean horizontal = orientation == ViewOrientation.VIEW_ORIENTATION_HORIZONTAL.value();
         sashForm.setOrientation(horizontal ? SWT.HORIZONTAL : SWT.VERTICAL);
         for (int i = 0; i < toggleOrientationActions.length; ++i)
             toggleOrientationActions[i].setChecked(orientation == toggleOrientationActions[i].getOrientation());
         currentOrientation = ViewOrientation.forValue(orientation);
         final GridLayout layout = (GridLayout) counterComposite.getLayout();
         setCounterColumns(layout);
         parent.layout();
     }
 
 
     private void setCounterColumns(final GridLayout layout) {
         if (currentOrientation.equals(ViewOrientation.VIEW_ORIENTATION_HORIZONTAL))
             layout.numColumns = 2;
         else
             layout.numColumns = 1;
     }
 
 
     static boolean getShowOnErrorOnly() {
         return Platform.getPreferencesService().getBoolean(FeatureRunnerPlugin.PLUGIN_ID,
                 PreferencesConstants.SHOW_ON_ERROR_ONLY, false, null);
     }
 
 
     static void importTestRunSession(final String url) {
         try {
             PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
                 @Override
                 public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                     throw new UnsupportedOperationException("Import is not currently available");
                 }
             });
         } catch (final InterruptedException e) {
             // cancelled
         } catch (final InvocationTargetException e) {
             final CoreException ce = (CoreException) e.getCause();
             StatusManager.getManager().handle(ce.getStatus(), StatusManager.SHOW | StatusManager.LOG);
         }
     }
 
 
     public FailureTrace getFailureTrace() {
         return failureTrace;
     }
 
 
     private void setLayoutMode(final int mode) {
         setFilterAndLayout(failuresOnlyFilterAction.isChecked(), mode);
     }
 
 
     private void setFilterAndLayout(final boolean failuresOnly, final int layoutMode) {
         showTestHierarchyAction.setChecked(layoutMode == ViewLayout.HIERARCHICAL.value());
         layout = ViewLayout.forValue(layoutMode);
         failuresOnlyFilterAction.setChecked(failuresOnly);
         testViewer.setShowFailuresOnly(failuresOnly, layoutMode);
     }
 
 
     private void setShowExecutionTime(final boolean showTime) {
         testViewer.setShowTime(showTime);
         showTimeAction.setChecked(showTime);
 
     }
 
 
     SubstepsTestElement[] getAllFailures() {
         return sessionManager.get().getAllFailedTestElements();
     }
 
 
     private Supplier<Display> displaySupplier() {
         return new Supplier<Display>() {
             @Override
             public Display get() {
                 return getDisplay();
             }
         };
     }
 
 
     private Supplier<IWorkbenchPartSite> siteSupplier() {
         return new Supplier<IWorkbenchPartSite>() {
             @Override
             public IWorkbenchPartSite get() {
                 return getSite();
             }
         };
     }
 
 
     private Notifier<Boolean> showFailuresOnlyNotifier() {
         return new Notifier<Boolean>() {
             @Override
             public void notify(final Boolean t) {
                 setFilterAndLayout(t.booleanValue(), layout.value());
             }
 
 
             @Override
             public Boolean currentValue() {
                 return failuresOnlyFilterAction.isChecked();
             }
         };
     }
 
 
     private Notifier<Boolean> autoScrollNotifier() {
         return new Notifier<Boolean>() {
             @Override
             public void notify(final Boolean t) {
                 setAutoScroll(t.booleanValue());
             }
 
 
             @Override
             public Boolean currentValue() {
                 return Boolean.valueOf(autoScroll);
             }
         };
     }
 
 
     private Notifier<Boolean> showOnErrorOnlyNotifier() {
         return new Notifier<Boolean>() {
             @Override
             public void notify(final Boolean t) {
                 showOnErrorOnly = t.booleanValue();
             }
 
 
             @Override
             public Boolean currentValue() {
                 return Boolean.valueOf(showOnErrorOnly);
             }
         };
     }
 
 
     private Notifier<ViewOrientation> viewOrientationNotifier() {
         return new Notifier<ViewOrientation>() {
             @Override
             public void notify(final ViewOrientation t) {
                 orientation = t;
                 computeOrientation();
             }
 
 
             @Override
             public ViewOrientation currentValue() {
                 // TODO Auto-generated method stub
                 return null;
             }
         };
     }
 
 
     private Notifier<ViewLayout> layoutModeNotifier() {
         return new Notifier<ViewLayout>() {
             @Override
             public void notify(final ViewLayout t) {
                 setLayoutMode(t.value());
             }
 
 
             @Override
             public ViewLayout currentValue() {
                 return null;
             }
         };
     }
 
 
     private Notifier<SubstepsTestElement> failedTestNotifier() {
         return new Notifier<SubstepsTestElement>() {
             @Override
             public void notify(final SubstepsTestElement t) {
                 showFailure(t);
                 ((SubstepsCopyAction) actionManager.copyAction()).handleTestSelected(t);
             }
 
 
             @Override
             public SubstepsTestElement currentValue() {
                 // TODO Auto-generated method stub
                 return null;
             }
         };
     }
 
 
     private Notifier<TestContext> testRerunner() {
         return new Notifier<TestContext>() {
             @Override
             public void notify(final TestContext t) {
                 rerunTest(t.getTestId(), t.getClassName(), t.getTestName(), t.getLaunchMode());
             }
 
 
             @Override
             public TestContext currentValue() {
                 return null;
             }
         };
     }
 
 
     private Supplier<String> testKindDisplayNameSupplier() {
         return new Supplier<String>() {
             @Override
             public String get() {
                 final String testRunnerKind = sessionManager.get().getTestRunnerKind();
                 return testRunnerKind == null ? "" : testRunnerKind;
             }
         };
     }
 
 
     private Notifier<String> tooltipNotifier() {
         return new Notifier<String>() {
             @Override
             public void notify(final String t) {
                 setTitleToolTip(t);
             }
 
 
             @Override
             public String currentValue() {
                 // TODO Auto-generated method stub
                 return null;
             }
         };
     }
 
 
     private Supplier<Boolean> disposedSashFormChecker() {
         return new Supplier<Boolean>() {
             @Override
             public Boolean get() {
                 return sashForm.isDisposed();
             }
         };
     }
 
 
     private Supplier<SubstepsSessionListener> testSessionListenerSupplier() {
         return new Supplier<SubstepsSessionListener>() {
             @Override
             public SubstepsSessionListener get() {
                 return new TestSessionListener(testViewer, SubstepsFeatureTestRunnerViewPart.this, infoMessageUpdater,
                         uiUpdater, showOnErrorOnlyNotifier(), SubstepsFeatureTestRunnerViewPart.this, actionManager,
                         sessionManager, testRunStatsSupplier, disposedChecker, autoScrollNotifier(),
                         SubstepsFeatureTestRunnerViewPart.this, viewTitleUiUpdater, testResultsView, failureTrace,
                         dirtyListener);
             }
         };
     }
 
 
     private Supplier<TestRunStats> testRunStatsSupplier() {
         return new Supplier<TestRunStats>() {
             @Override
             public TestRunStats get() {
                 return sessionManager.get();
             }
         };
     }
 }
