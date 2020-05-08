 package jp.thisnor.dre.app;
 
 import java.util.List;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import jp.thisnor.dre.core.MeasurerPackage;
 import jp.thisnor.dre.core.Messages;
 import jp.thisnor.dre.core.PathFilter;
 import jp.thisnor.dre.core.ProgressListener;
 import jp.thisnor.dre.core.SimilarGroup;
 import jp.thisnor.dre.core.WholeTask;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.SashForm;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.swt.widgets.ProgressBar;
 import org.eclipse.swt.widgets.Text;
 
 class MeasureExecutePage extends DREPage {
 	private DREFrame frame;
 	private Messages messages;
 
 	private Composite rootComp;
 	private ProgressBar
 		loadProgress, measureProgress;
 //	private Button pauseButton;
 	private Text logText;
 
 	private ExecutorService executor;
 	private volatile List<SimilarGroup> simGroupList;
 
 	MeasureExecutePage(DREFrame frame) {
 		this.frame = frame;
 		this.messages = frame.getMessages();
 	}
 
 	List<SimilarGroup> getSimilarGroupList() {
 		return simGroupList;
 	}
 
 	@Override
 	void createContents(Composite parent) {
 		rootComp = new SashForm(parent, SWT.VERTICAL);
 
 		Composite progressComp = new Composite(rootComp, SWT.NONE);
 		{
 			FormLayout l = new FormLayout();
 			l.spacing = 8;
 			progressComp.setLayout(l);
 		}
 
 		Label loadProgressLabel = new Label(progressComp, SWT.NONE);
 		loadProgressLabel.setText(messages.getString("MeasureExecutePage.LOAD_PROGRESS"));
 
 		loadProgress = new ProgressBar(progressComp, SWT.NONE);
 		loadProgress.setMinimum(0);
 
 		Label measureProgressLabel = new Label(progressComp, SWT.NONE);
 		measureProgressLabel.setText(messages.getString("MeasureExecutePage.MEASURE_PROGRESS"));
 
 		measureProgress = new ProgressBar(progressComp, SWT.NONE);
 		measureProgress.setMinimum(0);
 
 //		pauseButton = new Button(progressComp, SWT.PUSH);
 //		pauseButton.addSelectionListener(PAUSE_BUTTON_SELECTION_LISTENER);
 
 		loadProgressLabel.setLayoutData(new FormDataBuilder().left(0).right(0, 160).top(0).build());
 		loadProgress.setLayoutData(new FormDataBuilder().left(loadProgressLabel).right(100).top(0).build());
 		measureProgressLabel.setLayoutData(new FormDataBuilder().left(0).right(0, 160).top(loadProgressLabel).build());
 		measureProgress.setLayoutData(new FormDataBuilder().left(measureProgressLabel).right(100).top(loadProgressLabel).build());
 //		pauseButton.setLayoutData(new FormDataBuilder().left(100, -80).right(100).bottom(100).build());
 
 		logText = new Text(rootComp, SWT.MULTI | SWT.READ_ONLY | SWT.BORDER | SWT.V_SCROLL);
 	}
 
 	@Override
 	void activated() {
 		frame.setPageTitle(messages.getString("MeasureExecutePage.PAGE_TITLE"));
 		frame.setPageDescription(messages.getString("MeasureExecutePage.PAGE_DESCRIPTION"));
 		frame.setContent(rootComp);
 		frame.setPreviousButtonEnabled(true);
 		frame.setNextButtonEnabled(false);
 
 		FileEntrySelectPage fileSelectPage = frame.getPage(FileEntrySelectPage.class);
 		final List<String> targetFileList = fileSelectPage.getExtTargetFileList();
 		final List<String> storageFileList = fileSelectPage.getExtStorageFileList();
 		final PathFilter filter = fileSelectPage.getPathFilter();
 
 		PackageSelectPage packageSelectPage = frame.getPage(PackageSelectPage.class);
 		final MeasurerPackage mpack = packageSelectPage.getSelectedPackage();
 		final int numThreads = packageSelectPage.getNumThreads();
 
 		final ProgressListener logger = new ProgressListener() {
 			@Override
 			public void progressLoad(final int step, final int size) {
 				Display.getDefault().asyncExec(new Runnable() {
 					public void run() {
 						if (loadProgress.isDisposed()) return;
 						loadProgress.setSelection(step);
 						loadProgress.setMaximum(size);
 					}
 				});
 			}
 			@Override
 			public void progressMeasure(final int step, final int size) {
 				Display.getDefault().asyncExec(new Runnable() {
 					public void run() {
 						if (measureProgress.isDisposed()) return;
 						measureProgress.setSelection(step);
 						measureProgress.setMaximum(size);
 					}
 				});
 			}
 			@Override
 			public void log(final String line) {
 				Display.getDefault().asyncExec(new Runnable() {
 					public void run() {
 						if (logText.isDisposed()) return;
 						logText.append(line + "\n");
 					}
 				});
 			}
 		};
 
 		loadProgress.setSelection(0);
 		measureProgress.setSelection(0);
 		logText.setText("");
 
 		executor = Executors.newSingleThreadExecutor();
 		executor.submit(new Runnable() {
 			public void run() {
 				WholeTask task = new WholeTask(
 						targetFileList, storageFileList, filter,
 						mpack, numThreads,
 						logger, messages.getLocale()
 						);
 				try {
 					task.call();
 					logger.log(messages.getString("MeasureExecutePage.REPORT_COMPLETED_MESSAGE"));
 					Display.getDefault().asyncExec(new Runnable() {
 						public void run() {
 							loadProgress.setSelection(1);
 							loadProgress.setMaximum(1);
 							measureProgress.setSelection(1);
 							measureProgress.setMaximum(1);
 						}
 					});
 				} catch (InterruptedException e) {
 					logger.log(messages.getString("MeasureExecutePage.REPORT_CANCELED"));
 				} catch (Exception e) {
 					e.printStackTrace();
 					logger.log(e.getLocalizedMessage());
 					logger.log(messages.getString("MeasureExecutePage.REPORT_ABORTED"));
 				}
				simGroupList = task.getResult();
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						if (!frame.getShell().isDisposed())
							frame.setNextButtonEnabled(true);
					}
				});
 			}
 		});
 		executor.shutdown();
 	}
 
 	@Override
 	void hiddened() {
 		if (executor != null && !executor.isTerminated()) {
 			executor.shutdownNow();
 		}
 		executor = null;
 	}
 
 	@Override
 	void nextRequested() {
 		frame.setActivePage(frame.getPage(SimilarEntrySelectPage.class));
 	}
 
 	@Override
 	void previousRequested() {
 		if (executor != null) {
 			MessageBox msgBox = new MessageBox(Display.getDefault().getActiveShell(), SWT.YES | SWT.NO);
 			msgBox.setText(messages.getString(executor.isTerminated() ? "MeasureExecutePage.CONFIRM_BACK_MESSAGE" : "MeasureExecutePage.CONFIRM_ABORT_TITLE"));
 			msgBox.setMessage(messages.getString("MeasureExecutePage.CONFIRM_ABORT_MESSAGE"));
 			if (msgBox.open() == SWT.NO) return;
 		}
 		frame.setActivePage(frame.getPage(PackageSelectPage.class));
 	}
 
 	@Override
 	void dispose() {
 
 	}
 }
