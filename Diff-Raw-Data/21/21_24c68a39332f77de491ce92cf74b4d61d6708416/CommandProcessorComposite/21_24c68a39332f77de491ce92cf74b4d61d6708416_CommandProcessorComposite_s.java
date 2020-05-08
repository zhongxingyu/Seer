 /*-
  * Copyright © 2012 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package uk.ac.gda.client;
 
 import gda.commandqueue.CommandProgress;
 import gda.commandqueue.JythonCommandCommandProvider;
 import gda.commandqueue.Processor;
 import gda.commandqueue.ProcessorCurrentItem;
 import gda.commandqueue.QueueChangeEvent;
 import gda.observable.IObserver;
 import gda.rcp.GDAClientActivator;
 
 import java.io.File;
 
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.ToolBarManager;
 import org.eclipse.jface.dialogs.InputDialog;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.window.Window;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.PaintEvent;
 import org.eclipse.swt.events.PaintListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.FontMetrics;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.ProgressBar;
 import org.eclipse.ui.IViewPart;
 import org.eclipse.ui.IViewSite;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchPartSite;
 import org.eclipse.ui.handlers.IHandlerService;
 import org.eclipse.ui.services.IServiceLocator;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.rcp.views.AsciiTextView;
 import uk.ac.gda.common.rcp.util.EclipseWidgetUtils;
 import uk.ac.gda.menu.JythonControlsFactory;
 import uk.ac.gda.preferences.PreferenceConstants;
 
 public class CommandProcessorComposite extends Composite {
 	private static final Logger logger = LoggerFactory.getLogger(CommandProcessorComposite.class);
 	
 	final String strPause = "Pause Queue";
 	final String strRun = "  Start Queue  ";
 	Boolean runPauseIsPaused = false;
 	Boolean showText = false;
 	
 	private IObserver processorObserver;
 	private Processor processor;
 	private Label txtCurrentDescription;
 	ProgressBar progressBar;
 	String progressBarText = "";
 	private Label txtState;
 	private Action btnSkip;
 	private Action btnRunPause;
 	private boolean btnRunPause_Run = false;
 	private Action btnStopAfterCurrent;
 	private Action btnStop;
 	private Action btnAddToQueue;
 	private Action btnShowLog;
 	IWorkbenchPartSite iWorkbenchPartSite;
 	protected String lastAddedCommand = "";
 	private ImageDescriptor pauseImage;
 	private ImageDescriptor runImage;
 
 	public CommandProcessorComposite(Composite parent, int style, final IViewSite iWorkbenchPartSite,
 			final Processor processor) {
 		super(parent, style);
 		this.iWorkbenchPartSite = iWorkbenchPartSite;
 
 		this.processor = processor;
 
 		showText = gda.rcp.GDAClientActivator.getDefault().getPreferenceStore()
 				.getBoolean(PreferenceConstants.GDA_COMMAND_QUEUE_SHOW_TEXT);
 
 		final ImageDescriptor forwardOneImage = GDAClientActivator.getImageDescriptor("icons/control_fastforward_blue.png");
 		pauseImage = GDAClientActivator.getImageDescriptor("icons/control_pause_blue.png");
 		runImage = GDAClientActivator.getImageDescriptor("icons/control_play_blue.png");
 		final ImageDescriptor stopImage = GDAClientActivator.getImageDescriptor("icons/delete.png");
 		final ImageDescriptor addImage = GDAClientActivator.getImageDescriptor("icons/add.png");
 		final ImageDescriptor showLogImage = GDAClientActivator.getImageDescriptor("icons/book_open.png");
 
 		// layout the GUI
 		setLayout(new FormLayout());
 
 		Composite btnPanel = new Composite(this, SWT.NONE);
 
 		FormData fd_txtState = new FormData();
 		fd_txtState.top = new FormAttachment(0, 7);
 		fd_txtState.left = new FormAttachment(0, 5);
 		fd_txtState.right = new FormAttachment(100, -5);
 		btnPanel.setLayoutData(fd_txtState);
 
 		btnPanel.setLayout(new RowLayout(SWT.HORIZONTAL));
 		
 		txtState = new Label(btnPanel, SWT.NONE);
 		txtState.setText("Waiting Start..."); // make long enough for all text values
 		
 		
 		btnRunPause = new Action(null, SWT.NONE) {
 			@Override
 			public void run() {
 				setRun(btnRunPause_Run);
 			}
 		};
 		btnRunPause.setId(CommandQueueViewFactory.ID + ".runpause");
 		setRunBtnState(true);
 		ToolBarManager toolBarManager = (ToolBarManager) iWorkbenchPartSite.getActionBars().getToolBarManager();
 		toolBarManager.add(btnRunPause);
 
 		btnSkip = new Action(null, SWT.NONE) {
 			@Override
 			public void run() {
 				try {
 					((IHandlerService) iWorkbenchPartSite.getService(IHandlerService.class)).executeCommand(
 							CommandQueueContributionFactory.UK_AC_GDA_CLIENT_SKIP_COMMAND_QUEUE, new Event());
 				} catch (Exception ex) {
 					logger.error("Error executing command "
 							+ CommandQueueContributionFactory.UK_AC_GDA_CLIENT_SKIP_COMMAND_QUEUE);
 				}
 			}
 		};
 		btnSkip.setToolTipText("Stop current task and skip to start next - skip");
 		if (showText){
 			btnSkip.setText("Skip Task");
 		} else {
 			btnSkip.setImageDescriptor(forwardOneImage);
 		}
 		btnSkip.setId(CommandQueueViewFactory.ID + ".skip");
 		toolBarManager.add(btnSkip);
 
 		btnStop = new Action(null, SWT.NONE) {
 			@Override
 			public void run() {
 				try {
 					((IHandlerService) iWorkbenchPartSite.getService(IHandlerService.class)).executeCommand(
 							CommandQueueContributionFactory.UK_AC_GDA_CLIENT_STOP_COMMAND_QUEUE, new Event());
 				} catch (Exception ex) {
 					logger.error("Error executing command "
 							+ CommandQueueContributionFactory.UK_AC_GDA_CLIENT_STOP_COMMAND_QUEUE);
 				}
 			}
 		};
 		btnStop.setToolTipText("Abort current task and pause queue");
 		btnStop.setId(CommandQueueViewFactory.ID + ".abort");
 		if (showText){
 			btnStop.setText("Stop Queue");
 		} else {
 			btnStop.setImageDescriptor(stopImage);
 		}
 		toolBarManager.add(btnStop);
 
 		btnStopAfterCurrent = new Action(null, SWT.NONE) {
 			@Override
 			public void run() {
 				try {
 					((IHandlerService) iWorkbenchPartSite.getService(IHandlerService.class)).executeCommand(
 							CommandQueueContributionFactory.UK_AC_GDA_CLIENT_STOP_AFTER_CURRENT_COMMAND_QUEUE, new Event());
 				} catch (Exception ex) {
 					logger.error("Error executing command "
 							+ CommandQueueContributionFactory.UK_AC_GDA_CLIENT_STOP_AFTER_CURRENT_COMMAND_QUEUE, ex);
 				}
 			}
 		};
 		btnStopAfterCurrent.setToolTipText("Stop the processor after the current command has completed");
 		btnStopAfterCurrent.setId(CommandQueueViewFactory.ID + ".stopaftercurrent");
 		btnStopAfterCurrent.setText("Insert Stop");
 		toolBarManager.add(btnStopAfterCurrent);
 
 		btnAddToQueue = new Action(null, SWT.NONE) {
 			@Override
 			public void run() {
 				try {
 					InputDialog dlg = new InputDialog(Display.getCurrent().getActiveShell(), "Add Command To Queue",
 							"Enter a command ( Use \"run scriptname\" to execute a script)", lastAddedCommand, null);
 					if (dlg.open() == Window.OK) {
 						lastAddedCommand = dlg.getValue();
 						CommandQueueViewFactory.getQueue().addToTail(
 								new JythonCommandCommandProvider(lastAddedCommand, lastAddedCommand, null));
 					}
 				} catch (Exception e1) {
 					logger.error("Error submitting command to queue");
 				}
 			}
 		};
 		btnAddToQueue.setToolTipText("Add Jython command to queue");
 		btnAddToQueue.setId(CommandQueueViewFactory.ID + ".addtoqueue");
 		btnAddToQueue.setImageDescriptor(addImage);
 		toolBarManager.add(btnAddToQueue);
 
 		btnShowLog = new Action(null, SWT.NONE) {
 			@Override
 			public void run() {
 				iWorkbenchPartSite.getShell().getDisplay().asyncExec(new Runnable() {
 
 					@Override
 					public void run() {
 						File file = new File(processor.getLogFilePath());
 						IWorkbenchPage page = iWorkbenchPartSite.getPage();
 						if (page != null) {
 							final AsciiTextView view = (AsciiTextView) getView(AsciiTextView.ID, page, true);
 							if (view == null) {
 								logger.error("Unable to open view " + AsciiTextView.ID);
 								return;
 							}
 							view.load(file);
 							page.activate(view);
 							view.setFocus();
 						}
 					}
 				});
 			}
 		};
 		btnShowLog.setToolTipText("Show the Log File");
 		btnShowLog.setId(CommandQueueViewFactory.ID + ".showlog");
 		btnShowLog.setImageDescriptor(showLogImage);
 		toolBarManager.add(btnShowLog);
 		toolBarManager.update(true);
 		
 		txtCurrentDescription = new Label(this, SWT.WRAP | SWT.BORDER);
 		FormData fd_txtCurrentDescription = new FormData();
 		fd_txtCurrentDescription.left = new FormAttachment(btnPanel, 0, SWT.LEFT);
 		fd_txtCurrentDescription.bottom = new FormAttachment(txtCurrentDescription, 30, SWT.TOP);
 		fd_txtCurrentDescription.right = new FormAttachment(100, -5);
 		fd_txtCurrentDescription.top = new FormAttachment(btnPanel, 5, SWT.BOTTOM);
 		txtCurrentDescription.setLayoutData(fd_txtCurrentDescription);
 		txtCurrentDescription.setText("Description");
 
 		progressBar = new ProgressBar(this, SWT.SMOOTH | SWT.BORDER);
 		progressBar.setMinimum(0);
 		progressBar.setMaximum(2000);
 		FormData fd_txtCurrentProgress = new FormData();
 		fd_txtCurrentProgress.left = new FormAttachment(btnPanel, 0, SWT.LEFT);
 		// fd_txtCurrentProgress.bottom = new FormAttachment(100, -5);
 		fd_txtCurrentProgress.right = new FormAttachment(100, -5);
 		fd_txtCurrentProgress.top = new FormAttachment(txtCurrentDescription, 5, SWT.BOTTOM);
 		fd_txtCurrentProgress.bottom = new FormAttachment(progressBar, 20, SWT.TOP);
 		progressBar.setLayoutData(fd_txtCurrentProgress);
 		progressBar.addPaintListener(new PaintListener() {
 			@Override
 			public void paintControl(PaintEvent e) {
 				Point point = progressBar.getSize();
 				FontMetrics fontMetrics = e.gc.getFontMetrics();
 				int width = fontMetrics.getAverageCharWidth() * progressBarText.length();
 				int height = fontMetrics.getHeight();
 				e.gc.setForeground(iWorkbenchPartSite.getShell().getDisplay().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));
 				e.gc.drawString(progressBarText, (point.x - width) / 2, (point.y - height) / 2, true);
 			}
 		});
 
 		if (processor != null) {
 			processorObserver = new IObserver() {
 
 				@Override
 				public void update(Object source, final Object arg) {
 					if (arg instanceof Processor.STATE) {
 						CommandProcessorComposite.this.updateStateAndDescription((Processor.STATE) arg);
 					} else if (arg instanceof QueueChangeEvent) {
 						// do nothing
 					} else if (arg instanceof CommandProgress) {
 						iWorkbenchPartSite.getShell().getDisplay().asyncExec(new Runnable() {
 
 							@Override
 							public void run() {
 								CommandProgress progress = (CommandProgress) arg;
 								progressBarText = progress.getMsg();
 								progressBar.setSelection((int) (progress.getPercentDone()*20));
 								progressBar.redraw();//this is needed to cope with a change in text but no change in percentage
 								
 							}
 						});
 					}
 				}
 			};
 			processor.addIObserver(processorObserver);
 		}
 
 		updateStateAndDescription(null);
 	}
 
 	private void setRunBtnState(boolean run){
 		btnRunPause_Run = run;
 		if( run){
 			if (showText){
 				btnRunPause.setText(strRun);
 			} else {
 				btnRunPause.setImageDescriptor(CommandProcessorComposite.this.runImage);
 			}
 			btnRunPause.setToolTipText("Run current task if paused or start next task");
 		} else {
 			if (showText){
 				btnRunPause.setText(strPause);
 			} else {
 				btnRunPause.setImageDescriptor(CommandProcessorComposite.this.pauseImage);
 			}
 			btnRunPause.setToolTipText("Pause current task if possible. Stop queue");
 		}
 	}
 	
 	protected void setRun(boolean run) {
 		String commandId = run ? CommandQueueContributionFactory.UK_AC_GDA_CLIENT_START_COMMAND_QUEUE
 				: CommandQueueContributionFactory.UK_AC_GDA_CLIENT_PAUSE_COMMAND_QUEUE;
 		try {
 			((IHandlerService) iWorkbenchPartSite.getService(IHandlerService.class)).executeCommand(commandId,
 					new Event());
 		} catch (Exception e) {
 			logger.error("Error executing command " + commandId, e);
 		}
 	}
 
 	private IViewPart getView(String id, IWorkbenchPage page, boolean openView) {
 		try {
 			IViewPart part = page.findView(id);
 			if (part != null)
 				return part;
 			if (openView) {
 				return page.showView(id);
 			}
 			return null;
 		} catch (Exception ne) {
 			return null;
 		}
 	}
 
 	// update the GUI based on state of processor
 	private void updateStateAndDescription(final Processor.STATE stateIn) {
 		if (processor == null)
 			return;
 		iWorkbenchPartSite.getShell().getDisplay().asyncExec(new Runnable() {
 
 			@Override
 			public void run() {
 				Processor.STATE state = stateIn;
 				if (state == null)
 					state = getProcessorState();
 				ProcessorCurrentItem currentItem = getProcessorCurrentItem();
 				boolean itemBeingProcessed = currentItem != null;
 				txtCurrentDescription.setText(currentItem != null ? currentItem.getDescription() : "No current task");
 				txtCurrentDescription.setForeground(currentItem != null ? null : Display.getCurrent().getSystemColor(
 						SWT.COLOR_GRAY));
 				if (!itemBeingProcessed) {
 					progressBarText = ""; //set text first
 					progressBar.setSelection(0);
 				}
 
 				switch (state) {
 				case PROCESSING_ITEMS:
 					JythonControlsFactory.disableUIControls();
 					if (showText){
 						btnRunPause.setText(strPause);
 					} else {
 						btnRunPause.setImageDescriptor(CommandProcessorComposite.this.pauseImage);
 					}
 					btnRunPause.setToolTipText("Pause current task if possible. Stop queue");
 					setRunBtnState(false);
 					btnRunPause.setEnabled(true);
 
 					btnSkip.setEnabled(true);
 					btnStopAfterCurrent.setEnabled(true);
 					txtState.setText("Running");
 
 					break;
 				case UNKNOWN:
 					JythonControlsFactory.enableUIControls();
 					txtState.setText("Unknown");
 					break;
 				case WAITING_QUEUE:
 					JythonControlsFactory.disableUIControls();
 					if (showText){
 						btnRunPause.setText(strPause);
 					} else {
 						btnRunPause.setImageDescriptor(CommandProcessorComposite.this.pauseImage);
 					}
 					btnRunPause.setToolTipText("Pause current task if possible. Stop queue");
 					setRunBtnState(false);
 					btnRunPause.setEnabled(true);
 
 					btnSkip.setEnabled(false);
 					btnStopAfterCurrent.setEnabled(true);
 					txtState.setText("Queue is empty");
 					break;
 				case WAITING_START:
 					JythonControlsFactory.enableUIControls();
 					if (showText){
 						btnRunPause.setText(strRun);
 					} else {
 						btnRunPause.setImageDescriptor(CommandProcessorComposite.this.runImage);
 					}
 					btnRunPause.setToolTipText("Run current task if paused or start next task");
 					setRunBtnState(true);
 					btnRunPause.setEnabled(true);
 
 					btnSkip.setEnabled(currentItem != null);
 					btnStopAfterCurrent.setEnabled(true);
 					txtState.setText("Paused");
 					break;
 				}
 				EclipseWidgetUtils.forceLayoutOfTopParent(CommandProcessorComposite.this);
 			}
 
 			private ProcessorCurrentItem getProcessorCurrentItem() {
 				try {
 					return processor.getCurrentItem();
 				} catch (Exception e) {
 					logger.error("Error getting processor current item", e);
 				}
 				return null;
 			}
 
 			private Processor.STATE getProcessorState() {
 				try {
 					return processor.getState();
 				} catch (Exception e) {
 					logger.error("Error getting processor state", e);
 				}
 				return Processor.STATE.UNKNOWN;
 			}
 
 		});
 
 	}

	@Override
	public void dispose() {
		if (processor != null && processorObserver != null) {
			processor.deleteIObserver(processorObserver);
		}
		super.dispose();
	}
 }
 
 // Helper class - to find a handler and execute
 class ActionSelectionListener implements SelectionListener {
 
 	IServiceLocator serviceLocator;
 	String commandId;
 	private static final Logger logger = LoggerFactory.getLogger(ActionSelectionListener.class);
 
 	public ActionSelectionListener(IServiceLocator serviceLocator, String commandId) {
 		super();
 		this.serviceLocator = serviceLocator;
 		this.commandId = commandId;
 	}
 
 	@Override
 	public void widgetSelected(SelectionEvent e) {
 		try {
 			((IHandlerService) serviceLocator.getService(IHandlerService.class)).executeCommand(commandId, new Event());
 		} catch (Exception ex) {
 			logger.error("Error executing command " + commandId);
 		}
 	}
 
 	@Override
 	public void widgetDefaultSelected(SelectionEvent e) {
 		widgetSelected(e);
 	}
 }
