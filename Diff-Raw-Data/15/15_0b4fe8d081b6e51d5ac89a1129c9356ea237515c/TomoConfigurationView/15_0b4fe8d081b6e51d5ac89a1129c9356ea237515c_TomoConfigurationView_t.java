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
 
 package uk.ac.gda.client.tomo.configuration.view;
 
 import gda.commandqueue.Processor;
 import gda.commandqueue.ProcessorCurrentItem;
 import gda.commandqueue.QueuedCommandSummary;
 import gda.jython.Jython;
 import gda.jython.JythonServerFacade;
 import gda.observable.IObserver;
 import gda.util.Sleep;
 
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.StringTokenizer;
 import java.util.regex.Pattern;
 
 import org.eclipse.core.commands.operations.IOperationHistoryListener;
 import org.eclipse.core.commands.operations.IUndoContext;
 import org.eclipse.core.commands.operations.IUndoableOperation;
 import org.eclipse.core.commands.operations.ObjectUndoContext;
 import org.eclipse.core.commands.operations.OperationHistoryEvent;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.draw2d.ColorConstants;
 import org.eclipse.emf.common.command.Command;
 import org.eclipse.emf.common.notify.Adapter;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.Notifier;
 import org.eclipse.emf.edit.command.MoveCommand;
 import org.eclipse.emf.edit.command.RemoveCommand;
 import org.eclipse.emf.edit.command.SetCommand;
 import org.eclipse.emf.edit.domain.EditingDomain;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.layout.TableColumnLayout;
 import org.eclipse.jface.resource.FontRegistry;
 import org.eclipse.jface.viewers.CellEditor;
 import org.eclipse.jface.viewers.CheckboxCellEditor;
 import org.eclipse.jface.viewers.ColumnLayoutData;
 import org.eclipse.jface.viewers.ColumnWeightData;
 import org.eclipse.jface.viewers.ComboBoxCellEditor;
 import org.eclipse.jface.viewers.EditingSupport;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.jface.viewers.TextCellEditor;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.dnd.DND;
 import org.eclipse.swt.dnd.DragSource;
 import org.eclipse.swt.dnd.DragSourceAdapter;
 import org.eclipse.swt.dnd.DragSourceEvent;
 import org.eclipse.swt.dnd.DropTarget;
 import org.eclipse.swt.dnd.DropTargetAdapter;
 import org.eclipse.swt.dnd.DropTargetEvent;
 import org.eclipse.swt.dnd.TextTransfer;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.FontData;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableItem;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.IWorkbenchCommandConstants;
 import org.eclipse.ui.actions.ActionFactory;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.ui.operations.RedoActionHandler;
 import org.eclipse.ui.operations.UndoActionHandler;
 
 import uk.ac.diamond.scisoft.analysis.rcp.util.CommandExecutor;
 import uk.ac.gda.client.CommandQueueContributionFactory;
 import uk.ac.gda.client.CommandQueueViewFactory;
 import uk.ac.gda.client.tomo.IScanResolutionLookupProvider;
 import uk.ac.gda.client.tomo.TomoClientCommandStack;
 import uk.ac.gda.client.tomo.alignment.view.ImageLocationRelTheta;
 import uk.ac.gda.client.tomo.alignment.view.handlers.ITomoConfigResourceHandler;
 import uk.ac.gda.client.tomo.composites.StitchedImageCanvas;
 import uk.ac.gda.client.tomo.composites.StitchedImageCanvas.StitchConfig;
 import uk.ac.gda.client.tomo.composites.TomoAlignmentControlComposite.RESOLUTION;
 import uk.ac.gda.client.tomo.configuration.view.handlers.IScanControllerUpdateListener;
 import uk.ac.gda.client.tomo.configuration.viewer.TomoConfigContent;
 import uk.ac.gda.client.tomo.configuration.viewer.TomoConfigContent.CONFIG_STATUS;
 import uk.ac.gda.client.tomo.configuration.viewer.TomoConfigContentProvider;
 import uk.ac.gda.client.tomo.configuration.viewer.TomoConfigLabelProvider;
 import uk.ac.gda.client.tomo.configuration.viewer.TomoConfigTableConstants;
 import uk.ac.gda.client.tomo.views.BaseTomographyView;
 import uk.ac.gda.client.tomo.views.IDetectorResetable;
 import uk.ac.gda.tomography.parameters.AlignmentConfiguration;
 import uk.ac.gda.tomography.parameters.DetectorProperties;
 import uk.ac.gda.tomography.parameters.Parameters;
 import uk.ac.gda.tomography.parameters.Resolution;
 import uk.ac.gda.tomography.parameters.ScanCollected;
 import uk.ac.gda.tomography.parameters.ScanMode;
 import uk.ac.gda.tomography.parameters.StitchParameters;
 import uk.ac.gda.tomography.parameters.TomoExperiment;
 import uk.ac.gda.tomography.parameters.TomoParametersPackage;
 
 /**
  *
  */
 public class TomoConfigurationView extends BaseTomographyView implements IDetectorResetable {
 	private static final String NO_SCAN_RUNNING_MSG = "There is no scan running at the moment.";
 
 	private static final String NO_SCAN_RUNNING_TITLE = "No scan running";
 
 	private static final String SCAN_STOP_TITLE = "Scan stop";
 
 	private static final String SCAN_STOP_MSG = "Do you want to stop the scan?";
 
 	private static final String IOC_RUNNING_CONTEXT = "uk.ac.gda.client.tomo.configuration.isDetectorIocRunningContext";
 
 	private static final String DISPLAY_STATISTICS = "Display Statistics";
 	private static final String STOP_TOMO_RUNS = "Stop Tomo Runs";
 	private static final String START_TOMO_RUNS = "Start Tomo Runs";
 	private static final String BLANK = "";
 	private static final String ESTIMATED_END_TIME = "Estimated End Time";
 	private static final String CURRENT_DATE_AND_TIME = "Current Date and Time";
 	private static final String TOMOALIGNMENT_DESC_REGEX = "\\d*\\. [\\w|\\s|\\W]*";
 	private final Pattern tomoAlignmentDescRegexPattern = Pattern.compile(TOMOALIGNMENT_DESC_REGEX);
 
 	private static final String RESUME_SCAN = "Resume Scan";
 
 	private static final String INTERRUPT_TOMO_RUNS = "Interrupt Tomo Runs";
 
 	private static final String TOMOGRAPHY_SCAN_ALREADY_IN_PROGRESS_MSG = "Tomography Scan already in progress...";
 
 	private static final String ID_GET_RUNNING_CONFIG = "RunningConfig#";
 
 	private Button btnInterruptTomoRuns;
 	private static final String MOVE_DOWN = "Move Down";
 	private static final String MOVE_UP = "Move Up";
 	private static final String DELETE_SELECTED_CONFIGS = "Delete Configuration";
 	private static final String DELETE_ALL_CONFIGS = "Delete All Configurations";
 	private Button btnDeleteSelected;
 	private IUndoContext undoContext;
 	private String viewPartName;
 	private FormToolkit toolkit;
 	private Label lblCurrentDateTime;
 	private TableViewer configModelTableViewer;
 	private ITomoConfigResourceHandler configFileHandler;
 	private ArrayList<StitchConfig> stitchConfigs = new ArrayList<StitchedImageCanvas.StitchConfig>();
 
 	private DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy  HH:mm");
 	private UndoActionHandler fUndoAction;
 	private RedoActionHandler fRedoAction;
 	private Button btnDeleteAll;
 	private Button btnStartTomoRuns;
 	/**/
 	private StitchedImageCanvas img0DegComposite;
 	private StitchedImageCanvas img90DegComposite;
 	private TomoConfigurationViewController tomoConfigViewController;
 	private boolean isScanRunning = false;
 
 	private Button btnStopTomoRuns;
 
 	private IScanResolutionLookupProvider scanResolutionProvider;
 	/**/
 	private final String columnHeaders[] = { TomoConfigTableConstants.DRAG, TomoConfigTableConstants.SELECTION,
 			TomoConfigTableConstants.PROPOSAL, TomoConfigTableConstants.SAMPLE_DESCRIPTION,
 			TomoConfigTableConstants.MODULE_NUMBER, TomoConfigTableConstants.ACQUISITION_TIME,
 			TomoConfigTableConstants.FLAT_ACQ_TIME, TomoConfigTableConstants.DETECTOR_DISTANCE,
 			TomoConfigTableConstants.ENERGY, TomoConfigTableConstants.SAMPLE_WEIGHT,
 			TomoConfigTableConstants.RESOLUTION, TomoConfigTableConstants.FRAMES_PER_PROJECTION,
 			TomoConfigTableConstants.CONTINUOUS_STEP, TomoConfigTableConstants.RUN_TIME,
 			TomoConfigTableConstants.EST_END_TIME, TomoConfigTableConstants.TIME_FACTOR,
 			TomoConfigTableConstants.SHOULD_DISPLAY, TomoConfigTableConstants.PROGRESS,
 			TomoConfigTableConstants.ADDITIONAL };
 
 	private ColumnLayoutData columnLayouts[] = { new ColumnWeightData(1, false), new ColumnWeightData(10, false),
 			new ColumnWeightData(60, false), new ColumnWeightData(200, false), new ColumnWeightData(40, false),
 			new ColumnWeightData(40, false), new ColumnWeightData(40, false), new ColumnWeightData(60, false),
 			new ColumnWeightData(60, false), new ColumnWeightData(60, false), new ColumnWeightData(40, false),
 			new ColumnWeightData(40, false), new ColumnWeightData(40, false), new ColumnWeightData(40, false),
 			new ColumnWeightData(40, false), new ColumnWeightData(40, false), new ColumnWeightData(40, false),
 			new ColumnWeightData(40, false), new ColumnWeightData(40, false) };
 	// Fonts
 	private FontRegistry fontRegistry;
 
 	private Button btnMoveUp;
 	private Button btnMoveDown;
 	private Label lblEstEndTime;
 	private String cameraDistanceMotorName;
 
 	private static final String BOLD_9 = "bold_9";
 
 	public void setCameraDistanceMotorName(String cameraDistanceMotorName) {
 		this.cameraDistanceMotorName = cameraDistanceMotorName;
 	}
 
 	private void initializeFontRegistry() {
 		if (Display.getCurrent() != null) {
 			fontRegistry = new FontRegistry(Display.getCurrent());
 			String fontName = Display.getCurrent().getSystemFont().getFontData()[0].getName();
 			fontRegistry.put(BOLD_9, new FontData[] { new FontData(fontName, 9, SWT.BOLD) });
 		}
 	}
 
 	@Override
 	public void createPartControl(Composite parent) {
 		toolkit = new FormToolkit(parent.getDisplay());
 		initializeFontRegistry();
 		toolkit.setBorderStyle(SWT.BORDER);
 
 		Composite rootComposite = toolkit.createComposite(parent);
 
 		rootComposite.setLayout(new GridLayout());
 		//
 		Composite btnAndTableViewerComposite = toolkit.createComposite(rootComposite);
 		GridLayout layout = new GridLayout(2, false);
 		setLayoutSettings(layout, 0, 0, 0, 0);
 		// os
 		btnAndTableViewerComposite.setLayout(layout);
 		GridData layoutData2 = new GridData(GridData.FILL_BOTH);
 		btnAndTableViewerComposite.setLayoutData(layoutData2);
 		//
 		Composite tableViewerContainer = toolkit.createComposite(btnAndTableViewerComposite);
 		// GridLayout l = new GridLayout();
 		// setLayoutSettings(l, 0, 0, 0, 0);
 		// tableViewerContainer.setLayout(l);
 		GridData ld = new GridData(GridData.FILL_BOTH);
 		ld.heightHint = 60;
 		tableViewerContainer.setLayoutData(ld);
 
 		configModelTableViewer = new TableViewer(tableViewerContainer, SWT.BORDER | SWT.FULL_SELECTION);
 		configModelTableViewer.getTable().setHeaderVisible(true);
 		configModelTableViewer.getTable().setLinesVisible(true);
 		TableColumnLayout tableLayout = new TableColumnLayout();
 		tableViewerContainer.setLayout(tableLayout);
 
 		createColumns(configModelTableViewer, tableLayout);
 
 		TomoConfigContentProvider configContentProvider = null;
 		if (scanResolutionProvider != null) {
 			configContentProvider = new TomoConfigContentProvider(scanResolutionProvider);
 		} else {
 			configContentProvider = new TomoConfigContentProvider();
 		}
 		configContentProvider.setCameraDistanceMotorName(cameraDistanceMotorName);
 		configModelTableViewer.setContentProvider(configContentProvider);
 		configModelTableViewer.setLabelProvider(new TomoConfigLabelProvider());
 
 		Transfer[] types = new Transfer[] { TextTransfer.getInstance() };
 		DragSource source = new DragSource(configModelTableViewer.getTable(), DND.DROP_COPY);
 		source.setTransfer(types);
 
 		source.addDragListener(new DragSourceAdapter() {
 			@Override
 			public void dragSetData(DragSourceEvent event) {
 				if (!isScanRunning) {
 					// Get the selected items in the drag source
 					DragSource ds = (DragSource) event.widget;
 					Table table = (Table) ds.getControl();
 					TableItem[] selection = table.getSelection();
 
 					if (selection.length > 1) {
 						MessageDialog.openError(getViewSite().getShell(), "Cannot move multiple rows together",
 								"Cannot move multiple rows together");
 					} else {
 						StringBuffer buff = new StringBuffer();
 						for (int i = 0, n = selection.length; i < n; i++) {
 							buff.append(((TomoConfigContent) selection[i].getData()).getConfigId());
 						}
 
 						event.data = buff.toString();
 					}
 				}
 			}
 		});
 
 		// Create the drop target
 		DropTarget target = new DropTarget(configModelTableViewer.getTable(), DND.DROP_COPY | DND.DROP_DEFAULT);
 		target.setTransfer(types);
 		target.addDropListener(new DropTargetAdapter() {
 			@Override
 			public void dragEnter(DropTargetEvent event) {
 				if (event.detail == DND.DROP_DEFAULT) {
 					event.detail = (event.operations & DND.DROP_COPY) != 0 ? DND.DROP_COPY : DND.DROP_NONE;
 				}
 
 				// Allow dropping text only
 				for (int i = 0, n = event.dataTypes.length; i < n; i++) {
 					if (TextTransfer.getInstance().isSupportedType(event.dataTypes[i])) {
 						event.currentDataType = event.dataTypes[i];
 					}
 				}
 			}
 
 			@Override
 			public void dragOver(DropTargetEvent event) {
 				event.feedback = DND.FEEDBACK_SELECT | DND.FEEDBACK_SCROLL;
 			}
 
 			@Override
 			public void drop(DropTargetEvent event) {
 				if (TextTransfer.getInstance().isSupportedType(event.currentDataType)) {
 					// Get the dropped data
 					DropTarget target = (DropTarget) event.widget;
 					Table table = (Table) target.getControl();
 					TableItem ti = (TableItem) event.item;
 					String data = (String) event.data;
 
 					// Create a new item in the table to hold the dropped data
 					int newIndex = table.indexOf(ti);
 
 					Parameters parameters = getModel().getParameters();
 					AlignmentConfiguration alignmentConfiguration = parameters.getAlignmentConfiguration(data);
 
 					try {
 						runCommand(MoveCommand.create(getEditingDomain(), parameters,
 								TomoParametersPackage.eINSTANCE.getParameters_ConfigurationSet(),
 								alignmentConfiguration, newIndex));
 					} catch (IOException e) {
						logger.error("Error when attempting to add" +data+ "to the table", e);
 					} catch (Exception e) {
						logger.error("Error when attempting to add" +data+ "to the table", e);
 					}
 					refreshTable();
 				}
 			}
 		});
 
 		GridData tableLayoutData = new GridData(GridData.FILL_BOTH | GridData.HORIZONTAL_ALIGN_BEGINNING);
 		tableLayoutData.heightHint = 30;
 		configModelTableViewer.getTable().setLayoutData(tableLayoutData);
 		//
 		Composite deleteBtnContainer = toolkit.createComposite(btnAndTableViewerComposite);
 		deleteBtnContainer.setLayoutData(new GridData(GridData.FILL_VERTICAL));
 		deleteBtnContainer.setLayout(new GridLayout());
 		btnMoveUp = toolkit.createButton(deleteBtnContainer, MOVE_UP, SWT.PUSH);
 		btnMoveUp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		btnMoveUp.addSelectionListener(btnSelListener);
 
 		btnMoveDown = toolkit.createButton(deleteBtnContainer, MOVE_DOWN, SWT.PUSH);
 		btnMoveDown.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		btnMoveDown.addSelectionListener(btnSelListener);
 
 		btnDeleteAll = toolkit.createButton(deleteBtnContainer, DELETE_ALL_CONFIGS, SWT.PUSH);
 		btnDeleteAll.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		btnDeleteAll.addSelectionListener(btnSelListener);
 
 		btnDeleteSelected = toolkit.createButton(deleteBtnContainer, DELETE_SELECTED_CONFIGS, SWT.PUSH);
 		btnDeleteSelected.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		btnDeleteSelected.addSelectionListener(btnSelListener);
 
 		//
 		Composite middleRowComposite = toolkit.createComposite(rootComposite);
 		GridLayout layout2 = new GridLayout(6, true);
 		setLayoutSettings(layout2, 0, 0, 2, 0);
 		middleRowComposite.setLayout(layout2);
 		GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
 		layoutData.heightHint = 45;
 		middleRowComposite.setLayoutData(layoutData);
 
 		//
 		Composite timeNowCompositeContainer = toolkit.createComposite(middleRowComposite);
 		timeNowCompositeContainer.setLayoutData(new GridData(GridData.FILL_BOTH));
 
 		GridLayout layout5 = new GridLayout();
 		setLayoutSettings(layout5, 2, 2, 2, 2);
 		timeNowCompositeContainer.setLayout(layout5);
 		timeNowCompositeContainer.setBackground(ColorConstants.black);
 
 		Composite innerCompositeTimeNow = toolkit.createComposite(timeNowCompositeContainer);
 		GridLayout gl1 = new GridLayout();
 		setLayoutSettings(gl1, 0, 0, 5, 0);
 		innerCompositeTimeNow.setLayout(gl1);
 		innerCompositeTimeNow.setLayoutData(new GridData(GridData.FILL_BOTH));
 
 		Label lblDateNow = toolkit.createLabel(innerCompositeTimeNow, CURRENT_DATE_AND_TIME, SWT.WRAP | SWT.CENTER);
 		GridData layoutData4 = new GridData(GridData.FILL_BOTH);
 		lblDateNow.setLayoutData(layoutData4);
 
 		lblCurrentDateTime = toolkit.createLabel(innerCompositeTimeNow, BLANK, SWT.WRAP | SWT.CENTER);
 		lblCurrentDateTime.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		lblCurrentDateTime.setFont(fontRegistry.get(BOLD_9));
 
 		//
 		Composite estEndTimeCompositeContainer = toolkit.createComposite(middleRowComposite);
 		estEndTimeCompositeContainer.setLayoutData(new GridData(GridData.FILL_BOTH));
 
 		layout5 = new GridLayout();
 		setLayoutSettings(layout5, 2, 2, 2, 2);
 		estEndTimeCompositeContainer.setLayout(layout5);
 		estEndTimeCompositeContainer.setBackground(ColorConstants.black);
 		//
 		Composite innerCompositeEstEndTime = toolkit.createComposite(estEndTimeCompositeContainer);
 		gl1 = new GridLayout();
 		setLayoutSettings(gl1, 0, 0, 5, 0);
 		innerCompositeEstEndTime.setLayout(gl1);
 		innerCompositeEstEndTime.setLayoutData(new GridData(GridData.FILL_BOTH));
 
 		Label lblEstEndTimeDisplayLbl = toolkit.createLabel(innerCompositeEstEndTime, ESTIMATED_END_TIME, SWT.WRAP
 				| SWT.CENTER);
 		lblEstEndTimeDisplayLbl.setLayoutData(new GridData(GridData.FILL_BOTH));
 
 		lblEstEndTime = toolkit.createLabel(innerCompositeEstEndTime, BLANK, SWT.WRAP | SWT.CENTER);
 		lblEstEndTime.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		lblEstEndTime.setFont(fontRegistry.get(BOLD_9));
 
 		// Start tomo runs
 		btnStartTomoRuns = toolkit.createButton(middleRowComposite, START_TOMO_RUNS, SWT.PUSH);
 		btnStartTomoRuns.setLayoutData(new GridData(GridData.FILL_BOTH));
 		btnStartTomoRuns.addSelectionListener(btnSelListener);
 
 		// Interrupt Tomo Runs
 		btnInterruptTomoRuns = toolkit.createButton(middleRowComposite, INTERRUPT_TOMO_RUNS, SWT.PUSH);
 		btnInterruptTomoRuns.setLayoutData(new GridData(GridData.FILL_BOTH));
 		btnInterruptTomoRuns.addSelectionListener(btnSelListener);
 		// Stop tomo runs
 		btnStopTomoRuns = toolkit.createButton(middleRowComposite, STOP_TOMO_RUNS, SWT.PUSH);
 		btnStopTomoRuns.setLayoutData(new GridData(GridData.FILL_BOTH));
 		btnStopTomoRuns.addSelectionListener(btnSelListener);
 
 		// Stats
 		Button btnStats = toolkit.createButton(middleRowComposite, DISPLAY_STATISTICS, SWT.PUSH);
 		btnStats.setLayoutData(new GridData(GridData.FILL_BOTH));
 
 		//
 		Composite imageDisplayComposite = toolkit.createComposite(rootComposite);
 		GridLayout layout3 = new GridLayout(2, false);
 		setLayoutSettings(layout3, 2, 0, 4, 0);
 		imageDisplayComposite.setLayout(layout3);
 		GridData layoutData3 = new GridData(GridData.FILL_BOTH);
 		layoutData3.heightHint = 540;
 		imageDisplayComposite.setLayoutData(layoutData3);
 		img0DegComposite = new StitchedImageCanvas(imageDisplayComposite, ImageLocationRelTheta.THETA, SWT.None);
 		img0DegComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
 
 		img90DegComposite = new StitchedImageCanvas(imageDisplayComposite, ImageLocationRelTheta.THETA_PLUS_90,
 				SWT.None);
 		img90DegComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
 
 		TomoExperiment tomoExperiment = getModel();
 		configModelTableViewer.setInput(tomoExperiment);
 
 		createActions();
 
 		final int time = 60000;// 1 minute delay
 		final Display display = getViewSite().getShell().getDisplay();
 		Runnable timer = new Runnable() {
 
 			@Override
 			public void run() {
 				if (!display.isDisposed() && !lblCurrentDateTime.isDisposed()) {
 					lblCurrentDateTime.setText(dateFormat.format(new Date()));
 					updateScanRunDateTime();
 
 					display.timerExec(time, this);
 				}
 			}
 		};
 		display.timerExec(0, timer);
 		//
 		tomoConfigViewController.initialize();
 		tomoConfigViewController.addScanControllerUpdateListener(scanControllerUpdateListener);
 		tomoConfigViewController.isScanRunning();
 		//
 		// configModelTableViewer.getTable().layout();
 		final Processor processor = CommandQueueViewFactory.getProcessor();
 		CommandQueueViewFactory.getProcessor().addIObserver(queueObserver);
 		try {
 			queueObserver.update(null, processor.getState());
 		} catch (Exception e1) {
 			logger.error("Error getting state of processor", e1);
 		}
 
 		addPartListener();
 	}
 
 	protected void updateScanRunDateTime() {
 		if (!isScanRunning) {
 			updateEstimatedEndTime(true);
 		}
 	}
 
 	private void updateEstimatedEndTime(boolean refreshRow) {
 		TableItem[] items = configModelTableViewer.getTable().getItems();
 
 		double totalRunTime = 0;
 		for (TableItem tableItem : items) {
 			if (tableItem.getData() instanceof TomoConfigContent) {
 				TomoConfigContent cc = (TomoConfigContent) tableItem.getData();
 				if (cc.isSelectedToRun()) {
 					if (refreshRow) {
 						configModelTableViewer.refresh(cc);
 					}
 					totalRunTime = totalRunTime + cc.getRunTime();
 				}
 			}
 		}
 		if (totalRunTime > 0) {
 			Calendar now = Calendar.getInstance();
 			now.add(Calendar.SECOND, (int) totalRunTime);
 			lblEstEndTime.setText(String.format("%02d/%02d/%02d  %02d:%02d", now.get(Calendar.DAY_OF_MONTH),
 					now.get(Calendar.MONTH) + 1, now.get(Calendar.YEAR), now.get(Calendar.HOUR_OF_DAY),
 					now.get(Calendar.MINUTE)));
 		} else {
 			lblEstEndTime.setText("No scans selected");
 		}
 	}
 
 	private IObserver queueObserver = new IObserver() {
 
 		private final Processor processor = CommandQueueViewFactory.getProcessor();
 
 		private ProcessorCurrentItem getProcessorCurrentItem() {
 			try {
 				return processor.getCurrentItem();
 			} catch (Exception e) {
 				logger.error("Error getting processor current item", e);
 			}
 			return null;
 		}
 
 		// update the GUI based on state of processor
 		void updateStatus(final Processor.STATE stateIn, final ProcessorCurrentItem item) {
 			Display.getDefault().asyncExec(new Runnable() {
 
 				@Override
 				public void run() {
 					Processor.STATE state = stateIn;
 					if (state == null) {
 						state = getProcessorState();
 					}
 
 					switch (state) {
 					case PROCESSING_ITEMS:
 						btnInterruptTomoRuns.setText(INTERRUPT_TOMO_RUNS);
 						break;
 					case UNKNOWN:
 						break;
 					case WAITING_QUEUE:
 					case WAITING_START:
 						if (isScanRunning) {
 							btnInterruptTomoRuns.setText(RESUME_SCAN);
 						}
 						break;
 					}
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
 		public void update(Object source, Object arg) {
 			ProcessorCurrentItem item = null;
 			if (arg instanceof Processor.STATE) {
 				if (((Processor.STATE) arg) == Processor.STATE.PROCESSING_ITEMS) {
 					item = getProcessorCurrentItem();
 				}
 				updateStatus((Processor.STATE) arg, item);
 			}
 
 		}
 
 	};
 
 	private SelectionListener btnSelListener = new SelectionAdapter() {
 		@SuppressWarnings("rawtypes")
 		@Override
 		public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
 			Parameters parameters = getModel().getParameters();
 			if (e.getSource().equals(btnDeleteSelected)) {
 				logger.debug("Delete Selected");
 
 				ISelection selection = configModelTableViewer.getSelection();
 				List configContents = ((IStructuredSelection) selection).toList();
 				ArrayList<AlignmentConfiguration> configs = new ArrayList<AlignmentConfiguration>();
 				for (Object tomoConfigContent : configContents) {
 					AlignmentConfiguration alignmentConfiguration = parameters
 							.getAlignmentConfiguration(((TomoConfigContent) tomoConfigContent).getConfigId());
 					if (alignmentConfiguration != null) {
 						configs.add(alignmentConfiguration);
 					}
 				}
 
 				try {
 					Command rmCommand = RemoveCommand.create(getEditingDomain(), parameters,
 							TomoParametersPackage.eINSTANCE.getParameters_ConfigurationSet(), configs);
 					runCommand(rmCommand);
 				} catch (IOException ie) {
 					logger.error("Problem removing configuration", ie);
 				} catch (Exception ex) {
 					logger.error("Problem removing configuration", ex);
 				}
 
 			} else if (e.getSource().equals(btnDeleteAll)) {
 				logger.debug("Delete All");
 				List<AlignmentConfiguration> configurationSet = parameters.getConfigurationSet();
 				try {
 					Command rmCommand = RemoveCommand.create(getEditingDomain(), parameters,
 							TomoParametersPackage.eINSTANCE.getParameters_ConfigurationSet(), configurationSet);
 					runCommand(rmCommand);
 				} catch (IOException ie) {
 					logger.error("IOException Problem removing configuration from set", ie);
 				} catch (Exception ex) {
 					logger.error("Problem removing configuration from set", ex);
 				}
 			} else if (e.getSource().equals(btnMoveUp) || e.getSource().equals(btnMoveDown)) {
 				ISelection selection = configModelTableViewer.getSelection();
 				if (selection instanceof IStructuredSelection) {
 					IStructuredSelection ssl = (IStructuredSelection) selection;
 					Object firstElement = ssl.getFirstElement();
 
 					if (firstElement instanceof TomoConfigContent) {
 						TomoConfigContent tcc = (TomoConfigContent) firstElement;
 						AlignmentConfiguration alignmentConfiguration = getAlignmentConfiguration(tcc);
 
 						int oldIndex = parameters.getConfigurationSet().indexOf(alignmentConfiguration);
 						int newIndex = oldIndex;
 						if (e.getSource().equals(btnMoveUp)) {
 							if (oldIndex > 0) {
 								newIndex = oldIndex - 1;
 							}
 						} else if (e.getSource().equals(btnMoveDown)) {
 							if (oldIndex < parameters.getConfigurationSet().size()) {
 								newIndex = oldIndex + 1;
 							}
 						}
 						if (oldIndex != newIndex) {
 							try {
 								runCommand(MoveCommand.create(getEditingDomain(), parameters,
 										TomoParametersPackage.eINSTANCE.getParameters_ConfigurationSet(),
 										alignmentConfiguration, newIndex));
 							} catch (IOException e1) {
 								logger.error("Problem moving configuration", e1);
 							} catch (Exception ex) {
 								logger.error("Problem moving configuration", ex);
 							}
 						}
 
 						configModelTableViewer.setSelection(new StructuredSelection(configModelTableViewer
 								.getElementAt(newIndex)));
 					}
 				}
 			} else if (e.getSource().equals(btnStartTomoRuns)) {
 				logger.debug("Calling start tomo runs");
 				configModelTableViewer.setSelection(null);
 				tomoConfigViewController.startScan(getModel());
 				if (!isScanRunning) {
 					CommandExecutor.executeCommand(getViewSite(),
 							CommandQueueContributionFactory.UK_AC_GDA_CLIENT_START_COMMAND_QUEUE);
 				}
 			} else if (e.getSource().equals(btnInterruptTomoRuns)) {
 				if (btnInterruptTomoRuns.getText().equals(INTERRUPT_TOMO_RUNS)) {
 					CommandExecutor.executeCommand(getViewSite(),
 							CommandQueueContributionFactory.UK_AC_GDA_CLIENT_PAUSE_COMMAND_QUEUE);
 				} else if (btnInterruptTomoRuns.getText().equals(RESUME_SCAN)) {
 					CommandExecutor.executeCommand(getViewSite(),
 							CommandQueueContributionFactory.UK_AC_GDA_CLIENT_START_COMMAND_QUEUE);
 				}
 
 			} else if (e.getSource().equals(btnStopTomoRuns)) {
 				if (isScanOrScriptIsRunning()) {
 					boolean openConfirm = MessageDialog.openConfirm(getSite().getShell(), SCAN_STOP_TITLE,
 							SCAN_STOP_MSG);
 					if (openConfirm) {
 						logger.debug("Calling stop tomo runs");
 						JythonServerFacade.getInstance().haltCurrentScan();
 						tomoConfigViewController.stopScan();
 						try {
 							CommandQueueViewFactory.getProcessor().stop(100);
 						} catch (Exception e1) {
 							logger.error("problem with stopping the command queue processor", e1);
 						}
 						try {
 							List<QueuedCommandSummary> summaryList = CommandQueueViewFactory.getQueue()
 									.getSummaryList();
 							for (QueuedCommandSummary queuedCommandSummary : summaryList) {
 								if (tomoAlignmentDescRegexPattern.matcher(queuedCommandSummary.getDescription())
 										.matches()) {
 									CommandQueueViewFactory.getQueue().remove(queuedCommandSummary.id);
 								}
 							}
 						} catch (Exception e1) {
 							logger.error("Problem stopping Tomography Runs", e1);
 						}
 
 						// This is an annoying usage of sleep, however, the command queue doesn't seem to be sychronous
 						// in executing its commands. I found no better way to get the command queue to go into a paused
 						// state rather than a empty queue state after stopping the queue.
 						Sleep.sleep(2000);
 						CommandExecutor.executeCommand(getViewSite(),
 								CommandQueueContributionFactory.UK_AC_GDA_CLIENT_START_COMMAND_QUEUE);
 					}
 				} else {
 					MessageDialog.openInformation(getSite().getShell(), NO_SCAN_RUNNING_TITLE, NO_SCAN_RUNNING_MSG);
 				}
 			}
 		}
 
 		private boolean isScanOrScriptIsRunning() {
 			return JythonServerFacade.getInstance().getScanStatus() == Jython.RUNNING
 					|| JythonServerFacade.getInstance().getScriptStatus() == Jython.RUNNING;
 		}
 	};
 
 	private void createActions() {
 		IActionBars actionBars = getViewSite().getActionBars();
 
 		fUndoAction = new UndoActionHandler(getSite(), getUndoContext());
 		fUndoAction.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_UNDO);
 		fRedoAction = new RedoActionHandler(getSite(), getUndoContext());
 		fRedoAction.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_REDO);
 		actionBars.setGlobalActionHandler(ActionFactory.UNDO.getId(), fUndoAction);
 		actionBars.setGlobalActionHandler(ActionFactory.REDO.getId(), fRedoAction);
 
 		getViewSite().getWorkbenchWindow().getWorkbench().getOperationSupport().getOperationHistory()
 				.addOperationHistoryListener(historyListener);
 	}
 
 	protected void runCommand(final Command rmCommand) throws Exception {
 
 		getEditingDomain().getCommandStack().execute(rmCommand);
 
 		getModel().eResource().save(null);
 	}
 
 	private EditingDomain getEditingDomain() throws Exception {
 		return configFileHandler.getEditingDomain();
 	}
 
 	private IUndoContext getUndoContext() {
 		if (undoContext == null) {
 			undoContext = new ObjectUndoContext(this, getPartName());
 		}
 		return undoContext;
 	}
 
 	private IOperationHistoryListener historyListener = new IOperationHistoryListener() {
 
 		@Override
 		public void historyNotification(OperationHistoryEvent event) {
 			if (event.getEventType() == OperationHistoryEvent.DONE) {
 				// Set<Resource> affectedResources = ResourceUndoContext.getAffectedResources(event.getOperation());
 				// if (affectedResources.contains(getModel().eResource())) {
 				final IUndoableOperation operation = event.getOperation();
 				try {
 					operation.removeContext(((TomoClientCommandStack) getEditingDomain().getCommandStack())
 							.getDefaultUndoContext());
 				} catch (Exception e) {
 					logger.error("Problem getting editing domain", e);
 				}
 				operation.addContext(getUndoContext());
 				// }
 			} else if (event.getEventType() == OperationHistoryEvent.UNDONE
 					|| event.getEventType() == OperationHistoryEvent.REDONE) {
 				try {
 					getModel().eResource().save(null);
 				} catch (IOException e) {
 					logger.error("Unable to save", e);
 				}
 			}
 		}
 	};
 
 	private TomoExperiment getModel() {
 		TomoExperiment tomoExperiment = null;
 		try {
 			tomoExperiment = configFileHandler.getTomoConfigResource(new NullProgressMonitor(), true);
 		} catch (InvocationTargetException e) {
 			logger.error("Problem getting model ite", e);
 		} catch (InterruptedException e) {
 			logger.error("Problem getting model interrupted", e);
 		} catch (Exception e) {
 			logger.error("Problem getting model exc", e);
 		}
 
 		if (tomoExperiment != null && !tomoExperiment.getParameters().eAdapters().contains(tableRefreshNotifyAdapter)) {
 			Parameters parameters = tomoExperiment.getParameters();
 			parameters.eAdapters().add(tableRefreshNotifyAdapter);
 			for (AlignmentConfiguration ac : parameters.getConfigurationSet()) {
 				ac.eAdapters().add(rowRefreshNotifyAdapter);
 				ac.getDetectorProperties().eAdapters().add(rowRefreshNotifyAdapter);
 			}
 
 		}
 
 		return tomoExperiment;
 	}
 
 	private Adapter rowRefreshNotifyAdapter = new Adapter() {
 
 		@Override
 		public void notifyChanged(Notification notification) {
 			int eventType = notification.getEventType();
 			if (eventType == Notification.SET || eventType == Notification.UNSET || eventType == Notification.ADD
 					|| eventType == Notification.ADD_MANY || eventType == Notification.REMOVE
 					|| eventType == Notification.REMOVE_MANY || eventType == Notification.MOVE) {
 
 				Object notifier = notification.getNotifier();
 				AlignmentConfiguration ac = null;
 
 				if (notifier instanceof AlignmentConfiguration) {
 					ac = (AlignmentConfiguration) notifier;
 				} else if (notifier instanceof DetectorProperties) {
 					ac = (AlignmentConfiguration) ((DetectorProperties) notifier).eContainer();
 				}
 				if (ac != null) {
 					if (!configModelTableViewer.getTable().isDisposed()) {
 						refreshTable();
 					}
 				}
 			}
 		}
 
 		@Override
 		public Notifier getTarget() {
 			return null;
 		}
 
 		@Override
 		public void setTarget(Notifier newTarget) {
 
 		}
 
 		@Override
 		public boolean isAdapterForType(Object type) {
 			return false;
 		}
 
 	};
 	private Adapter tableRefreshNotifyAdapter = new Adapter() {
 		@Override
 		public void notifyChanged(org.eclipse.emf.common.notify.Notification notification) {
 			logger.debug("tomoConfigNotifyAdapter#notifyChanged:{}", notification);
 
 			int eventType = notification.getEventType();
 			if (eventType == Notification.ADD) {
 				Object notifier = notification.getNotifier();
 				if (notifier instanceof Parameters && notification.getNewValue() instanceof AlignmentConfiguration) {
 					AlignmentConfiguration ac = (AlignmentConfiguration) notification.getNewValue();
 					if (!ac.eAdapters().contains(rowRefreshNotifyAdapter)) {
 						ac.eAdapters().add(rowRefreshNotifyAdapter);
 						ac.getDetectorProperties().eAdapters().add(rowRefreshNotifyAdapter);
 					}
 				}
 			}
 
 			if (eventType == Notification.SET || eventType == Notification.UNSET || eventType == Notification.ADD
 					|| eventType == Notification.ADD_MANY || eventType == Notification.REMOVE
 					|| eventType == Notification.REMOVE_MANY || eventType == Notification.MOVE) {
 				refreshTable();
 			} else if (eventType == Notification.REMOVING_ADAPTER) {
 				if (!configModelTableViewer.getTable().isDisposed()) {
 					configModelTableViewer.setInput(getModel());
 				}
 			}
 		}
 
 		@Override
 		public Notifier getTarget() {
 			return null;
 		}
 
 		@Override
 		public void setTarget(Notifier newTarget) {
 
 		}
 
 		@Override
 		public boolean isAdapterForType(Object type) {
 			return false;
 		}
 	};
 
 	private void setLayoutSettings(GridLayout layout, int marginWidth, int marginHeight, int horizontalSpacing,
 			int verticalSpacing) {
 		layout.marginWidth = marginWidth;
 		layout.marginHeight = marginHeight;
 		layout.horizontalSpacing = horizontalSpacing;
 		layout.verticalSpacing = verticalSpacing;
 	}
 
 	protected void refreshTable() {
 		if (configModelTableViewer != null && !configModelTableViewer.getTable().isDisposed()) {
 			configModelTableViewer.getTable().getDisplay().syncExec(new Runnable() {
 
 				@Override
 				public void run() {
 					configModelTableViewer.refresh();
 					updateEstimatedEndTime(false);
 				}
 			});
 		}
 	}
 
 	protected void refreshRow(final TomoConfigContent config) {
 		// Organise the list to display the stitched image
 		if (config.isShouldDisplay()) {
 			includeInListToDisplay(config);
 		} else {
 			excludeFromListToDisplay(config.getConfigId());
 		}
 
 		if (configModelTableViewer != null && !configModelTableViewer.getTable().isDisposed()) {
 			configModelTableViewer.getTable().getDisplay().syncExec(new Runnable() {
 
 				@Override
 				public void run() {
 					configModelTableViewer.refresh(config, true);
 				}
 			});
 		}
 	}
 
 	private void excludeFromListToDisplay(String configId) {
 		StitchConfig stitchConfig = getStitchConfig(configId);
 		if (stitchConfig != null && stitchConfigs.contains(stitchConfig)) {
 			stitchConfigs.remove(stitchConfig);
 			// refreshImageViews();
 		}
 	}
 
 	private void refreshImageViews() {
 		img0DegComposite.setStitchConfigs(stitchConfigs);
 		img90DegComposite.setStitchConfigs(stitchConfigs);
 	}
 
 	private StitchConfig getStitchConfig(String configId) {
 		for (StitchConfig sc : stitchConfigs) {
 			if (sc.getId().equals(configId)) {
 				return sc;
 			}
 		}
 		return null;
 	}
 
 	private void includeInListToDisplay(TomoConfigContent config) {
 		StitchConfig stitchConfig = getStitchConfig(config.getConfigId());
 		if (stitchConfig == null) {
 			AlignmentConfiguration ac = getAlignmentConfiguration(config);
 			StitchConfig sc = new StitchConfig();
 			sc.setId(ac.getId());
 			// sc.setHorizontalX(sampleStageParameters.getCenterX().getValue());
 			// sc.setHorizontalZ(ac.getSampleStageParameters().getCenterZ().getValue());
 			// sc.setVerticalMotor(sampleStageParameters.getVertical().getValue());
 			StitchParameters sp = ac.getStitchParameters();
 			sc.setTheta(sp.getStitchingThetaAngle());
 			sc.setImageLocationAt0(sp.getImageAtTheta());
 			sc.setImageLocationAt90(sp.getImageAtThetaPlus90());
 			stitchConfigs.add(sc);
 
 			//
 			// refreshImageViews();
 		}
 
 	}
 
 	private AlignmentConfiguration getAlignmentConfiguration(TomoConfigContent config) {
 		return getModel().getParameters().getAlignmentConfiguration(config.getConfigId());
 	}
 
 	@Override
 	public void setFocus() {
 
 	}
 
 	public void setViewPartName(String viewPartName) {
 		this.viewPartName = viewPartName;
 	}
 
 	@Override
 	public String getPartName() {
 		if (viewPartName != null) {
 			return viewPartName;
 		}
 		return super.getPartName();
 	}
 
 	public void setConfigFileHandler(ITomoConfigResourceHandler configFileHandler) {
 		this.configFileHandler = configFileHandler;
 	}
 
 	@Override
 	public void dispose() {
 		TomoExperiment tomoExperiment = getModel();
 		if (tomoExperiment != null && tomoExperiment.getParameters().eAdapters().contains(tableRefreshNotifyAdapter)) {
 			Parameters parameters = tomoExperiment.getParameters();
 			parameters.eAdapters().remove(tableRefreshNotifyAdapter);
 			for (AlignmentConfiguration ac : parameters.getConfigurationSet()) {
 				ac.eAdapters().remove(rowRefreshNotifyAdapter);
 				ac.getDetectorProperties().eAdapters().remove(rowRefreshNotifyAdapter);
 			}
 		}
 		getViewSite().getWorkbenchWindow().getWorkbench().getOperationSupport().getOperationHistory()
 				.removeOperationHistoryListener(historyListener);
 		tomoConfigViewController.removeScanControllerUpdateListener(scanControllerUpdateListener);
 		tomoConfigViewController.dispose();
 		CommandQueueViewFactory.getProcessor().deleteIObserver(queueObserver);
 		super.dispose();
 	}
 
 	public void createColumns(TableViewer tableViewer, TableColumnLayout layout) {
 		for (int i = 0; i < columnHeaders.length; i++) {
 			TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, SWT.None);
 			tableViewerColumn.getColumn().setResizable(columnLayouts[i].resizable);
 			tableViewerColumn.getColumn().setText(columnHeaders[i]);
 			tableViewerColumn.getColumn().setToolTipText(columnHeaders[i]);
 			layout.setColumnData(tableViewerColumn.getColumn(), columnLayouts[i]);
 			tableViewerColumn.setEditingSupport(new TomoColumnEditingSupport(tableViewer, tableViewerColumn));
 		}
 	}
 
 	public TomoConfigurationViewController getTomoConfigViewController() {
 		return tomoConfigViewController;
 	}
 
 	public void setTomoConfigViewController(TomoConfigurationViewController tomoConfigViewController) {
 		this.tomoConfigViewController = tomoConfigViewController;
 	}
 
 	private class TomoColumnEditingSupport extends EditingSupport {
 
 		private String columnIdentifier;
 
 		public TomoColumnEditingSupport(TableViewer viewer, TableViewerColumn tableViewerColumn) {
 			super(viewer);
 			columnIdentifier = tableViewerColumn.getColumn().getText();
 
 		}
 
 		@Override
 		protected void setValue(Object element, Object value) {
 			logger.debug("setValue:element:{}", element);
 			logger.debug("setValue:value:{}", value);
 			if (element instanceof TomoConfigContent) {
 				TomoConfigContent configContent = (TomoConfigContent) element;
 				if (TomoConfigTableConstants.SAMPLE_DESCRIPTION.equals(columnIdentifier)) {
 					try {
 						runCommand(SetCommand.create(getEditingDomain(), getModel().getParameters()
 								.getAlignmentConfiguration(configContent.getConfigId()),
 								TomoParametersPackage.eINSTANCE.getAlignmentConfiguration_Description(), value));
 					} catch (IOException e) {
 						logger.error("Error setting description", e);
 					} catch (Exception ex) {
 						logger.error("Error setting description", ex);
 					}
 				} else if (TomoConfigTableConstants.CONTINUOUS_STEP.equals(columnIdentifier)) {
 					ScanMode scanMode = ScanMode.get((Integer) value);
 					try {
 						runCommand(SetCommand.create(getEditingDomain(), getModel().getParameters()
 								.getAlignmentConfiguration(configContent.getConfigId()),
 								TomoParametersPackage.eINSTANCE.getAlignmentConfiguration_ScanMode(), scanMode));
 					} catch (IOException e) {
 						logger.error("Error setting scan mode", e);
 					} catch (Exception ex) {
 						logger.error("Error setting scan mode", ex);
 					}
 				} else if (TomoConfigTableConstants.RESOLUTION.equals(columnIdentifier)) {
 					Resolution resolution = Resolution.get((Integer) value);
 					try {
 						runCommand(SetCommand
 								.create(getEditingDomain(),
 										getModel().getParameters()
 												.getAlignmentConfiguration(configContent.getConfigId())
 												.getDetectorProperties(),
 										TomoParametersPackage.eINSTANCE.getDetectorProperties_Desired3DResolution(),
 										resolution));
 					} catch (IOException e) {
 						logger.error("Error setting resolution", e);
 					} catch (Exception ex) {
 						logger.error("Error setting resolution", ex);
 					}
 				} else if (TomoConfigTableConstants.SHOULD_DISPLAY.equals(columnIdentifier)) {
 					configContent.setShouldDisplay((Boolean) value);
 					refreshRow(configContent);
 				} else if (TomoConfigTableConstants.SELECTION.equals(columnIdentifier)) {
 					Boolean isSelectedToRun = (Boolean) value;
 					try {
 						runCommand(SetCommand.create(getEditingDomain(), getModel().getParameters()
 								.getAlignmentConfiguration(configContent.getConfigId()),
 								TomoParametersPackage.eINSTANCE.getAlignmentConfiguration_SelectedToRun(),
 								isSelectedToRun));
 					} catch (IOException e) {
 						logger.error("Error setting resolution", e);
 					} catch (Exception ex) {
 						logger.error("Error setting resolution", ex);
 					}
 				} else if (TomoConfigTableConstants.TIME_FACTOR.equals(columnIdentifier)) {
 					try {
 						double doubleVal = Double.parseDouble((String) value);
 
 						try {
 							runCommand(SetCommand.create(getEditingDomain(), getModel().getParameters()
 									.getAlignmentConfiguration(configContent.getConfigId()).getDetectorProperties(),
 									TomoParametersPackage.eINSTANCE.getDetectorProperties_AcquisitionTimeDivider(),
 									Double.valueOf(doubleVal)));
 						} catch (IOException e) {
 							logger.error("Error setting description", e);
 						} catch (Exception ex) {
 							logger.error("Error setting description", ex);
 						}
 					} catch (NumberFormatException ex) {
 						logger.error("Invalid value", ex);
 					}
 				}
 			}
 
 		}
 
 		@Override
 		protected Object getValue(Object element) {
 			logger.debug("getValue:{}", element);
 			if (element instanceof TomoConfigContent) {
 				TomoConfigContent configContent = (TomoConfigContent) element;
 				if (TomoConfigTableConstants.SAMPLE_DESCRIPTION.equals(columnIdentifier)) {
 					return configContent.getSampleDescription() != null ? configContent.getSampleDescription() : BLANK;
 				} else if (TomoConfigTableConstants.CONTINUOUS_STEP.equals(columnIdentifier)) {
 					return ScanMode.get(configContent.getScanMode()).getValue();
 				} else if (TomoConfigTableConstants.RESOLUTION.equals(columnIdentifier)) {
 					return RESOLUTION.get(configContent.getResolution()).ordinal();
 				} else if (TomoConfigTableConstants.SHOULD_DISPLAY.equals(columnIdentifier)) {
 					return configContent.isShouldDisplay();
 				} else if (TomoConfigTableConstants.SELECTION.equals(columnIdentifier)) {
 					return configContent.isSelectedToRun();
 				} else if (TomoConfigTableConstants.TIME_FACTOR.equals(columnIdentifier)) {
 					return Double.toString(configContent.getTimeDivider());
 				}
 			}
 			return null;
 		}
 
 		@Override
 		protected CellEditor getCellEditor(Object element) {
 			logger.debug("getCellEditor:{}", element);
 			Table table = ((TableViewer) getViewer()).getTable();
 			if (TomoConfigTableConstants.SAMPLE_DESCRIPTION.equals(columnIdentifier)) {
 				return new TextCellEditor(table);
 			} else if (TomoConfigTableConstants.RESOLUTION.equals(columnIdentifier)) {
 				return new ComboBoxCellEditor(table, new String[] { RESOLUTION.FULL.toString(),
 						RESOLUTION.TWO_X.toString(), RESOLUTION.FOUR_X.toString(), RESOLUTION.EIGHT_X.toString() });
 			} else if (TomoConfigTableConstants.SHOULD_DISPLAY.equals(columnIdentifier)) {
 				return new CheckboxCellEditor(table);
 			} else if (TomoConfigTableConstants.SELECTION.equals(columnIdentifier)) {
 				return new CheckboxCellEditor(table);
 			} else if (TomoConfigTableConstants.TIME_FACTOR.equals(columnIdentifier)) {
 				return new TextCellEditor(table);
 			} else if (TomoConfigTableConstants.ADDITIONAL.equals(columnIdentifier)) {
 				return new AdditionalInfoDialogCellEditor(table, (TomoConfigContent) element, scanResolutionProvider);
 			}
 			return null;
 		}
 
 		@Override
 		protected boolean canEdit(Object element) {
 			if (!isScanRunning) {
 				if (TomoConfigTableConstants.SAMPLE_DESCRIPTION.equals(columnIdentifier)) {
 					return true;
 				} else if (TomoConfigTableConstants.RESOLUTION.equals(columnIdentifier)) {
 					return true;
 				} else if (TomoConfigTableConstants.SHOULD_DISPLAY.equals(columnIdentifier)) {
 					return true;
 				} else if (TomoConfigTableConstants.SELECTION.equals(columnIdentifier)) {
 					return true;
 				} else if (TomoConfigTableConstants.TIME_FACTOR.equals(columnIdentifier)) {
 					return true;
 				} else if (TomoConfigTableConstants.ADDITIONAL.equals(columnIdentifier)) {
 					if (!(CONFIG_STATUS.RUNNING == ((TomoConfigContent) element).getStatus())) {
 						return true;
 					}
 				}
 				logger.debug("canEdit:{}", element);
 			}
 			return false;
 		}
 	}
 
 	private HashMap<String, CONFIG_STATUS> getConfigStatusForTomoConfigContent(String runningConfigId) {
 		String removedBraces = runningConfigId.replace("{", BLANK).replace("}", BLANK).replace("'", BLANK);
 		StringTokenizer strTokenizer = new StringTokenizer(removedBraces, ",");
 		HashMap<String, CONFIG_STATUS> map = new HashMap<String, CONFIG_STATUS>();
 		while (strTokenizer.hasMoreTokens()) {
 			// will be of the format a:b
 			String token = strTokenizer.nextToken();
 			logger.debug("token:{}", token);
 			int indexOfColon = token.indexOf(":");
 			String configId = token.substring(0, indexOfColon).trim();
 			logger.debug("configId:{}", configId);
 			String status = token.substring(indexOfColon + 1).trim();
 			logger.debug("status:{}", status);
 			CONFIG_STATUS configStatus = CONFIG_STATUS.getConfigStatus(status);
 			map.put(configId, configStatus);
 		}
 		return map;
 	}
 
 	private IScanControllerUpdateListener scanControllerUpdateListener = new IScanControllerUpdateListener() {
 
 		@Override
 		public void updateScanProgress(final double progress) {
 			final Shell shell = getViewSite().getShell();
 			shell.getDisplay().asyncExec(new Runnable() {
 
 				@Override
 				public void run() {
 					TableItem[] items = configModelTableViewer.getTable().getItems();
 					for (TableItem tableItem : items) {
 						TomoConfigContent c = (TomoConfigContent) tableItem.getData();
 
 						if (c.getStatus() == CONFIG_STATUS.RUNNING) {
 							c.setProgress(Math.round(progress));
 							refreshRow(c);
 							break;
 						}
 					}
 				}
 			});
 		}
 
 		@Override
 		public void updateExposureTime(double exposureTime) {
 			// Do nothing
 		}
 
 		@Override
 		public void isScanRunning(boolean isScanRunning, final String runningConfigId) {
 			if (isScanRunning) {
 				disableControls();
 				final Shell shell = getSite().getShell();
 				shell.getDisplay().asyncExec(new Runnable() {
 
 					@Override
 					public void run() {
 						HashMap<String, CONFIG_STATUS> configStatusForTomoConfigContent = getConfigStatusForTomoConfigContent(runningConfigId);
 						TableItem[] items = configModelTableViewer.getTable().getItems();
 						for (TableItem tableItem : items) {
 							TomoConfigContent c = (TomoConfigContent) tableItem.getData();
 							if (configStatusForTomoConfigContent.keySet().contains(c.getConfigId())) {
 								CONFIG_STATUS status = configStatusForTomoConfigContent.get(c.getConfigId());
 								logger.debug("configId ={}", c.getConfigId());
 								logger.debug("status = {}", status);
 								c.setStatus(status);
 								if (status == CONFIG_STATUS.STARTING) {
 									c.setProgress(0);
 								} else if (status == CONFIG_STATUS.COMPLETE) {
 									updateScanCollected(configStatusForTomoConfigContent);
 								} else if (status == CONFIG_STATUS.NONE) {
 									c.setProgress(0);
 								}
 							} else {
 								c.setStatus(CONFIG_STATUS.NONE);
 								c.setProgress(0);
 							}
 							refreshRow(c);
 						}
 
 					}
 
 					private void updateScanCollected(HashMap<String, CONFIG_STATUS> configStatusForTomoConfigContent) {
 						try {
 							configFileHandler.reloadResource();
 						} catch (Exception e) {
 							logger.error("Problem reloading resource", e);
 						}
 
 						List<TomoConfigContent> configsToChange = new ArrayList<TomoConfigContent>();
 						TableItem[] items = configModelTableViewer.getTable().getItems();
 						for (TableItem tableItem : items) {
 							TomoConfigContent c = (TomoConfigContent) tableItem.getData();
 							if (configStatusForTomoConfigContent.keySet().contains(c.getConfigId())
 									&& configStatusForTomoConfigContent.get(c.getConfigId()) == CONFIG_STATUS.COMPLETE) {
 								configsToChange.add(c);
 							}
 						}
 
 						for (TomoConfigContent c : configsToChange) {
 							AlignmentConfiguration alignmentConfiguration = getModel().getParameters()
 									.getAlignmentConfiguration(c.getConfigId());
 							if (alignmentConfiguration != null) {
 								List<ScanCollected> scanCollected = alignmentConfiguration.getScanCollected();
 
 								for (ScanCollected sC : scanCollected) {
 									c.addScanInformation(Integer.parseInt(sC.getScanNumber()), sC.getStartTime(),
 											sC.getEndTime());
 								}
 
 							}
 							c.setProgress(100);
 							c.setStatus(CONFIG_STATUS.COMPLETE);
 							refreshRow(c);
 						}
 
 					}
 				});
 
 			} else {
 				enableControls();
 				isScanRunning = false;
 			}
 
 		}
 
 		@Override
 		public void updateMessage(final String message) {
 
 			if (message.startsWith(ID_GET_RUNNING_CONFIG)) {
 				final String runningConfigId = message.substring(ID_GET_RUNNING_CONFIG.length());
 				if (runningConfigId.equals("None")) {
 					logger.debug("No configs are running");
 					enableControls();
 					isScanRunning = false;
 				} else {
 					isScanRunning = true;
 					disableControls();
 					final HashMap<String, CONFIG_STATUS> configStatusForTomoConfigContent = getConfigStatusForTomoConfigContent(runningConfigId);
 					final Shell shell = getSite().getShell();
 					shell.getDisplay().asyncExec(new Runnable() {
 
 						@Override
 						public void run() {
 							TableItem[] items = configModelTableViewer.getTable().getItems();
 							for (TableItem tableItem : items) {
 								TomoConfigContent c = (TomoConfigContent) tableItem.getData();
 								if (configStatusForTomoConfigContent.keySet().contains(c.getConfigId())) {
 									CONFIG_STATUS status = configStatusForTomoConfigContent.get(c.getConfigId());
 									c.setStatus(status);
 									if (status == CONFIG_STATUS.COMPLETE) {
 										c.setProgress(100);
 									} else if (status == CONFIG_STATUS.NONE) {
 										c.setProgress(0);
 									}
 								} else {
 									c.setStatus(CONFIG_STATUS.NONE);
 									c.setProgress(0);
 								}
 								refreshRow(c);
 							}
 
 						}
 					});
 				}
 			} else if (message.equals(TOMOGRAPHY_SCAN_ALREADY_IN_PROGRESS_MSG)) {
 				final Shell shell = getSite().getShell();
 				shell.getDisplay().asyncExec(new Runnable() {
 
 					@Override
 					public void run() {
 						MessageDialog.openError(shell, TOMOGRAPHY_SCAN_ALREADY_IN_PROGRESS_MSG,
 								TOMOGRAPHY_SCAN_ALREADY_IN_PROGRESS_MSG);
 					}
 				});
 			} else {
 				getSite().getShell().getDisplay().asyncExec(new Runnable() {
 
 					@Override
 					public void run() {
 						getViewSite().getActionBars().getStatusLineManager().setMessage(message);
 
 					}
 				});
 			}
 
 		}
 
 		@Override
 		public void updateError(Exception exception) {
 			logger.error("Error from server:{}", exception);
 		}
 	};
 
 	protected void disableControls() {
 		if (getViewSite().getShell() != null) {
 			getViewSite().getShell().getDisplay().asyncExec(new Runnable() {
 
 				@Override
 				public void run() {
 					configModelTableViewer.getTable().setEnabled(true);
 					btnDeleteAll.setEnabled(false);
 					btnDeleteSelected.setEnabled(false);
 					btnMoveDown.setEnabled(false);
 					btnMoveUp.setEnabled(false);
 				}
 			});
 		}
 	}
 
 	protected void enableControls() {
 		if (getViewSite().getShell() != null) {
 			getViewSite().getShell().getDisplay().asyncExec(new Runnable() {
 
 				@Override
 				public void run() {
 					configModelTableViewer.getTable().setEnabled(true);
 					btnDeleteAll.setEnabled(true);
 					btnDeleteSelected.setEnabled(true);
 					btnMoveDown.setEnabled(true);
 					btnMoveUp.setEnabled(true);
 				}
 			});
 		}
 	}
 
 	public void setScanResolutionProvider(IScanResolutionLookupProvider scanResolutionProvider) {
 		this.scanResolutionProvider = scanResolutionProvider;
 	}
 
 	/**
 	 * Given a config Id, checks if the table viewer contains an element with the config id and sets selection on that.
 	 * 
 	 * @param configId
 	 */
 	public void setConfigSelection(String configId) {
 
 		TableItem[] items = configModelTableViewer.getTable().getItems();
 		int count = 0;
 		for (TableItem tableItem : items) {
 			Object data = tableItem.getData();
 			if (data instanceof TomoConfigContent) {
 				TomoConfigContent configContent = (TomoConfigContent) data;
 				if (configContent.getConfigId().equals(configId)) {
 					configModelTableViewer.setSelection(
 							new StructuredSelection(configModelTableViewer.getElementAt(count)), true);
 					return;
 				}
 			}
 			count++;
 		}
 
 	}
 
 	@Override
 	protected String getDetectorPortName() throws Exception {
 		return tomoConfigViewController.getDetectorPortName();
 	}
 
 	@Override
 	protected String getIocRunningContext() {
 		return IOC_RUNNING_CONTEXT;
 	}
 
 	@Override
 	public void reset() {
 		try {
 			tomoConfigViewController.reset();
 		} catch (Exception e) {
			logger.error("Error occurred while attempting to reset the detector doing the tomography alignment", e);
 		}
 	}
 
 }
