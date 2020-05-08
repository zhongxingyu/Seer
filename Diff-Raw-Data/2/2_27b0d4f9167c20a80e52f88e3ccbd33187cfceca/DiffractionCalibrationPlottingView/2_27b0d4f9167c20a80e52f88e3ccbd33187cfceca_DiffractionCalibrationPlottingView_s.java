 /*-
  * Copyright 2013 Diamond Light Source Ltd.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.dawb.workbench.ui.views;
 
 import java.io.File;
 import java.util.AbstractMap;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map.Entry;
 
 import javax.measure.quantity.Length;
 import javax.measure.unit.NonSI;
 import javax.measure.unit.SI;
 
 import org.dawb.common.services.ILoaderService;
 import org.dawb.common.ui.util.GridUtils;
 import org.dawb.common.ui.widgets.ActionBarWrapper;
 import org.dawb.workbench.ui.Activator;
 import org.dawb.workbench.ui.views.DiffractionCalibrationUtils.ManipulateMode;
 import org.dawnsci.common.widgets.tree.NumericNode;
 import org.dawnsci.common.widgets.utils.RadioUtils;
 import org.dawnsci.plotting.api.IPlottingSystem;
 import org.dawnsci.plotting.api.PlotType;
 import org.dawnsci.plotting.api.PlottingFactory;
 import org.dawnsci.plotting.api.tool.IToolPage.ToolPageRole;
 import org.dawnsci.plotting.api.tool.IToolPageSystem;
 import org.dawnsci.plotting.tools.diffraction.DiffractionImageAugmenter;
 import org.dawnsci.plotting.tools.diffraction.DiffractionTool;
 import org.dawnsci.plotting.tools.diffraction.DiffractionTreeModel;
 import org.dawnsci.plotting.util.PlottingUtils;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.jobs.IJobChangeEvent;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.core.runtime.jobs.JobChangeAdapter;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IMenuListener;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.util.LocalSelectionTransfer;
 import org.eclipse.jface.viewers.CellEditor;
 import org.eclipse.jface.viewers.CheckboxCellEditor;
 import org.eclipse.jface.viewers.EditingSupport;
 import org.eclipse.jface.viewers.ILabelProviderListener;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.ITableLabelProvider;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.jface.viewers.TreeSelection;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.nebula.widgets.formattedtext.FormattedText;
 import org.eclipse.nebula.widgets.formattedtext.NumberFormatter;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.SashForm;
 import org.eclipse.swt.custom.ScrolledComposite;
 import org.eclipse.swt.dnd.DND;
 import org.eclipse.swt.dnd.DropTarget;
 import org.eclipse.swt.dnd.DropTargetAdapter;
 import org.eclipse.swt.dnd.DropTargetEvent;
 import org.eclipse.swt.dnd.FileTransfer;
 import org.eclipse.swt.dnd.TextTransfer;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 import org.eclipse.swt.widgets.ToolBar;
 import org.eclipse.swt.widgets.ToolItem;
 import org.eclipse.ui.IMemento;
 import org.eclipse.ui.IViewSite;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.part.ResourceTransfer;
 import org.eclipse.ui.part.ViewPart;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.crystallography.CalibrantSelectedListener;
 import uk.ac.diamond.scisoft.analysis.crystallography.CalibrantSelectionEvent;
 import uk.ac.diamond.scisoft.analysis.crystallography.CalibrationFactory;
 import uk.ac.diamond.scisoft.analysis.crystallography.CalibrationStandards;
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 import uk.ac.diamond.scisoft.analysis.diffraction.DetectorProperties;
 import uk.ac.diamond.scisoft.analysis.diffraction.DetectorPropertyEvent;
 import uk.ac.diamond.scisoft.analysis.diffraction.DiffractionCrystalEnvironment;
 //import uk.ac.diamond.scisoft.analysis.diffraction.DiffractionCrystalEnvironmentEvent;
 import uk.ac.diamond.scisoft.analysis.diffraction.IDetectorPropertyListener;
 //import uk.ac.diamond.scisoft.analysis.diffraction.IDiffractionCrystalEnvironmentListener;
 import uk.ac.diamond.scisoft.analysis.hdf5.HDF5NodeLink;
 import uk.ac.diamond.scisoft.analysis.io.IDiffractionMetadata;
 
 /**
  * This listens for a selected editor (of a diffraction image) and allows
  * 
  * 1) selection of calibrant
  * 2) movement, scaling and tilting of rings
  * 3) refinement of fit
  * 4) calibration (other images too?)
  *
  * Should display relevant metadata, allow a number of files to contribute to final calibration
  */
 public class DiffractionCalibrationPlottingView extends ViewPart {
 
 	private static Logger logger = LoggerFactory.getLogger(DiffractionCalibrationPlottingView.class);
 
 	private DiffractionTableData currentData;
 
 	private final String DIFFRACTION_ID = "org.dawb.workbench.plotting.tools.diffraction.Diffraction";
 	private final String WAVELENGTH_NODE_PATH = "/Experimental Information/Wavelength";
 	private final String BEAM_CENTRE_XPATH = "/Detector/Beam Centre/X";
 	private final String BEAM_CENTRE_YPATH = "/Detector/Beam Centre/Y";
 	private final String DISTANCE_NODE_PATH = "/Experimental Information/Distance";
 
 	public static final String EDIT_MASK = "##,##0.##########";
 	public static final String DISPLAY_MASK = "##,##0.##########";
 
 	private List<DiffractionTableData> model = new ArrayList<DiffractionTableData>();
 	private ILoaderService service;
 
 	private Composite parent;
 	private ScrolledComposite scrollComposite;
 	private Composite scrollHolder;
 	private TableViewer tableViewer;
 	private Button calibrateImagesButton;
 	private Combo calibrantCombo;
 	private Group wavelengthComp;
 	private Group calibOptionGroup;
 	private Action deleteAction;
 
 	private IPlottingSystem plottingSystem;
 
 	private ISelectionChangedListener selectionChangeListener;
 	private DropTargetAdapter dropListener;
 //	private IDiffractionCrystalEnvironmentListener diffractionCrystEnvListener;
 	private IDetectorPropertyListener detectorPropertyListener;
 	private CalibrantSelectedListener calibrantChangeListener;
 
 	private List<String> pathsList = new ArrayList<String>();
 
 	private FormattedText wavelengthDistanceField;
 	private FormattedText wavelengthEnergyField;
 
 	private IToolPageSystem toolSystem;
 
 	private boolean postFitWavelength = false;
 	private boolean usedFixedWavelength = false;
 
 	public DiffractionCalibrationPlottingView() {
 		service = (ILoaderService) PlatformUI.getWorkbench().getService(ILoaderService.class);
 	}
 
 	private static final String DATA_PATH = "DataPath";
 	private static final String CALIBRANT = "Calibrant";
 
 	private String calibrantName;
 
 	@Override
 	public void init(IViewSite site, IMemento memento) throws PartInitException {
 		init(site);
 		setSite(site);
 		setPartName("Diffraction Calibration View");
 
 		if (memento != null) {
 			for (String k : memento.getAttributeKeys()) {
 				if (k.startsWith(DATA_PATH)) {
 					int i = Integer.parseInt(k.substring(DATA_PATH.length()));
 					pathsList.add(i, memento.getString(k));
 				}
 				if (k.startsWith(CALIBRANT)) {
 					calibrantName = memento.getString(k);
 				}
 			}
 		}
 	}
 
 	@Override
 	public void saveState(IMemento memento) {
 		if (memento != null) {
 			int i = 0;
 			for (TableItem t : tableViewer.getTable().getItems()) {
 				DiffractionTableData data = (DiffractionTableData) t.getData();
 				memento.putString(DATA_PATH + String.valueOf(i++), data.path);
 			}
 			memento.putString(CALIBRANT, calibrantCombo.getItem(calibrantCombo.getSelectionIndex()));
 		}
 	}
 
 	@Override
 	public void createPartControl(final Composite parent) {
 		parent.setLayout(new FillLayout());
 
 		this.parent = parent;
 		final Display display = parent.getDisplay();
 
 		// selection change listener for table viewer
 		selectionChangeListener = new ISelectionChangedListener() {
 			@Override
 			public void selectionChanged(SelectionChangedEvent event) {
 				ISelection is = event.getSelection();
 				if (is instanceof StructuredSelection) {
 					StructuredSelection structSelection = (StructuredSelection) is;
 					DiffractionTableData selectedData = (DiffractionTableData) structSelection.getFirstElement();
 					if (selectedData == null || selectedData == currentData)
 						return;
 					drawSelectedData(selectedData);
 				}
 			}
 		};
 
 //		diffractionCrystEnvListener = new IDiffractionCrystalEnvironmentListener() {
 //			@Override
 //			public void diffractionCrystalEnvironmentChanged(
 //					final DiffractionCrystalEnvironmentEvent evt) {
 //				display.asyncExec(new Runnable() {
 //					@Override
 //					public void run() {
 //						// if the change is triggered by the text field update and not the diffraction tool
 //						// update spinner value with data from diff tool
 //						NumericNode<Length> node = getDiffractionTreeNode(WAVELENGTH_NODE_PATH);
 //						if (node.getUnit().equals(NonSI.ANGSTROM)) {
 //							wavelengthDistanceField.setValue(node.getDoubleValue());
 //							wavelengthEnergyField.setValue(getWavelengthEnergy(node.getDoubleValue()));
 //						} else if (node.getUnit().equals(NonSI.ELECTRON_VOLT)) {
 //							wavelengthDistanceField.setValue(getWavelengthEnergy(node.getDoubleValue() / 1000));
 //							wavelengthEnergyField.setValue(node.getDoubleValue() / 1000);
 //						} else if (node.getUnit().equals(SI.KILO(NonSI.ELECTRON_VOLT))) {
 //							wavelengthDistanceField.setValue(getWavelengthEnergy(node.getDoubleValue()));
 //							wavelengthEnergyField.setValue(node.getDoubleValue());
 //						}
 //						tableViewer.refresh();
 //					}
 //				});
 //			}
 //		};
 
 		detectorPropertyListener = new IDetectorPropertyListener() {
 			@Override
 			public void detectorPropertiesChanged(DetectorPropertyEvent evt) {
 				display.asyncExec(new Runnable() {
 					@Override
 					public void run() {
 						tableViewer.refresh();
 					}
 				});
 			}
 		};
 
 		calibrantChangeListener = new CalibrantSelectedListener() {
 			@Override
 			public void calibrantSelectionChanged(CalibrantSelectionEvent evt) {
 				calibrantCombo.select(calibrantCombo.indexOf(evt.getCalibrant()));
 			}
 		};
 
 		dropListener = new DropTargetAdapter() {
 			@Override
 			public void drop(DropTargetEvent event) {
 				Object dropData = event.data;
 				DiffractionTableData good = null;
 				if (dropData instanceof IResource[]) {
 					IResource[] res = (IResource[]) dropData;
 					for (int i = 0; i < res.length; i++) {
 						DiffractionTableData d = createData(res[i].getRawLocation().toOSString(), null);
 						if (d != null) {
 							good = d;
 							setWavelength(d);
 						}
 					}
 				} else if (dropData instanceof TreeSelection) {
 					TreeSelection selectedNode = (TreeSelection) dropData;
 					Object obj[] = selectedNode.toArray();
 					for (int i = 0; i < obj.length; i++) {
 						DiffractionTableData d = null;
 						if (obj[i] instanceof HDF5NodeLink) {
 							HDF5NodeLink node = (HDF5NodeLink) obj[i];
 							if (node == null)
 								return;
 							d = createData(node.getFile().getFilename(), node.getFullName());
 						} else if (obj[i] instanceof IFile) {
 							IFile file = (IFile) obj[i];
 							d = createData(file.getLocation().toOSString(), null);
 						}
 						if (d != null) {
 							good = d;
 							setWavelength(d);
 						}
 					}
 				} else if (dropData instanceof String[]) {
 					String[] selectedData = (String[]) dropData;
 					for (int i = 0; i < selectedData.length; i++) {
 						DiffractionTableData d = createData(selectedData[i], null);
 						if (d != null) {
 							good = d;
 							setWavelength(d);
 						}
 					}
 				}
 
 				tableViewer.refresh();
 				if (currentData == null && good != null) {
 					tableViewer.getTable().deselectAll();
 					tableViewer.setSelection(new StructuredSelection(good));
 				}
 				if (model.size() > 0)
 					setXRaysModifiersEnabled(true);
 			}
 		};
 
 		deleteAction = new Action("Delete item", Activator.getImageDescriptor("icons/delete_obj.png")) {
 			@Override
 			public void run() {
 				StructuredSelection selection = (StructuredSelection) tableViewer.getSelection();
 				DiffractionTableData selectedData = (DiffractionTableData) selection.getFirstElement();
 				if (model.size() > 0) {
 					if (model.remove(selectedData)) {
 						selectedData.augmenter.deactivate();
 						selectedData.md.getDetector2DProperties().removeDetectorPropertyListener(detectorPropertyListener);
 //						selectedData.md.getDiffractionCrystalEnvironment().removeDiffractionCrystalEnvironmentListener(diffractionCrystEnvListener);
 						tableViewer.refresh();
 					}
 				}
 				if (!model.isEmpty()) {
 					drawSelectedData((DiffractionTableData) tableViewer.getElementAt(0));
 				} else {
 					currentData = null; // need to reset this
 					plottingSystem.clear();
 					setXRaysModifiersEnabled(false);
 					setCalibrateOptionsEnabled(false);
 				}
 			}
 		};
 
 		// main sash form which contains the left sash and the plotting system
 		SashForm mainSash = new SashForm(parent, SWT.HORIZONTAL);
 		mainSash.setBackground(new Color(display, 192, 192, 192));
 		mainSash.setLayout(new FillLayout());
 
 		// left sash form which contains the diffraction calibration controls
 		// and the diffraction tool
 		SashForm leftSash = new SashForm(mainSash, SWT.VERTICAL);
 		leftSash.setBackground(new Color(display, 192, 192, 192));
 		leftSash.setLayout(new GridLayout(1, false));
 		leftSash.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
 
 		Composite controlComp = new Composite(leftSash, SWT.NONE);
 		controlComp.setLayout(new GridLayout(1, false));
 		GridUtils.removeMargins(controlComp);
 		createToolbarActions(controlComp);
 
 		Label instructionLabel = new Label(controlComp, SWT.WRAP);
 		instructionLabel.setText("Drag/drop a file/data to the table below, " +
 				"choose a type of calibrant, " +
 				"modify the rings using the positioning controls, " +
 				"modify the wavelength/energy with the wanted values, " +
 				"match rings to the image, " +
 				"and select the calibration type before running the calibration process.");
 		instructionLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false));
 		Point pt = instructionLabel.getSize(); pt.x +=4; pt.y += 4; instructionLabel.setSize(pt);
 
 		// make a scrolled composite
 		scrollComposite = new ScrolledComposite(controlComp, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
 		scrollComposite.setLayout(new GridLayout(1, false));
 		scrollComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		scrollHolder = new Composite(scrollComposite, SWT.NONE);
 
 		GridLayout gl = new GridLayout(1, false);
 		scrollHolder.setLayout(gl);
 		scrollHolder.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true));
 
 		// table of images and found rings
 		tableViewer = new TableViewer(scrollHolder, SWT.FULL_SELECTION
 				| SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
 		createColumns(tableViewer);
 		tableViewer.getTable().setHeaderVisible(true);
 		tableViewer.getTable().setLinesVisible(true);
 		tableViewer.getTable().setToolTipText("Drag/drop file(s)/data to this table");
 		tableViewer.setContentProvider(new MyContentProvider());
 		tableViewer.setLabelProvider(new MyLabelProvider());
 		tableViewer.setInput(model);
 		tableViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		tableViewer.addSelectionChangedListener(selectionChangeListener);
 		tableViewer.refresh();
 		final MenuManager mgr = new MenuManager();
 		mgr.setRemoveAllWhenShown(true);
 		mgr.addMenuListener(new IMenuListener() {
 			@Override
 			public void menuAboutToShow(IMenuManager manager) {
 				IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
 				if (!selection.isEmpty()) {
 					deleteAction.setText("Delete "+ ((DiffractionTableData) selection.getFirstElement()).name);
 					mgr.add(deleteAction);
 				}
 			}
 		});
 		tableViewer.getControl().setMenu(mgr.createContextMenu(tableViewer.getControl()));
 		// add drop support
 		DropTarget dt = new DropTarget(tableViewer.getControl(), DND.DROP_MOVE | DND.DROP_DEFAULT | DND.DROP_COPY);
 		dt.setTransfer(new Transfer[] { TextTransfer.getInstance(),
 				FileTransfer.getInstance(), ResourceTransfer.getInstance(),
 				LocalSelectionTransfer.getTransfer() });
 		dt.addDropListener(dropListener);
 
 		Composite calibrantHolder = new Composite(scrollHolder, SWT.NONE);
 		calibrantHolder.setLayout(new GridLayout(1, false));
 		calibrantHolder.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
 
 		Composite mainControlComp = new Composite(calibrantHolder, SWT.NONE);
 		mainControlComp.setLayout(new GridLayout(2, false));
 		mainControlComp.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
 
 		Composite leftCalibComp = new Composite(mainControlComp, SWT.NONE);
 		leftCalibComp.setLayout(new GridLayout(1, false));
 		leftCalibComp.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true));
 
 		Group controllerHolder = new Group(leftCalibComp, SWT.BORDER);
 		controllerHolder.setText("Calibrant selection and positioning");
 		controllerHolder.setLayout(new GridLayout(2, false));
 		controllerHolder.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
 
 		// create calibrant combo
 		Label l = new Label(controllerHolder, SWT.NONE);
 		l.setText("Calibrant:");
 		l.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
 		calibrantCombo = new Combo(controllerHolder, SWT.READ_ONLY);
 		final CalibrationStandards standards = CalibrationFactory.getCalibrationStandards();
 		calibrantCombo.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				if (currentData == null)
 					return;
 				String calibrantName = calibrantCombo.getItem(calibrantCombo.getSelectionIndex());
 				// update the calibrant in diffraction tool
 				standards.setSelectedCalibrant(calibrantName, true);
 				DiffractionCalibrationUtils.drawCalibrantRings(currentData.augmenter);
 			}
 		});
 		for (String c : standards.getCalibrantList()) {
 			calibrantCombo.add(c);
 		}
 		String s = standards.getSelectedCalibrant();
 		if (s != null) {
 			calibrantCombo.setText(s);
 		}
 		calibrantCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
 
 		// Pad composite
 		Composite padComp = new Composite(controllerHolder, SWT.BORDER);
 		padComp.setLayout(new GridLayout(5, false));
 		padComp.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));
 		padComp.setToolTipText("Move calibrant");
 
 		l = new Label(padComp, SWT.NONE);
 		l = new Label(padComp, SWT.NONE);
 		Button upButton = new Button(padComp, SWT.ARROW | SWT.UP);
 		upButton.setToolTipText("Move rings up");
 		upButton.addMouseListener(new RepeatingMouseAdapter(display,
 				new SlowFastRunnable() {
 					@Override
 					public void run() {
 						DiffractionCalibrationUtils.changeRings(currentData, ManipulateMode.UP, isFast());
 					}
 
 					@Override
 					public void stop() {
 						if (currentData == null)
 							return;
 						updateDiffTool(BEAM_CENTRE_YPATH, currentData.md.getDetector2DProperties().getBeamCentreCoords()[1]);
 						refreshTable();
 					}
 				}));
 		upButton.setLayoutData(new GridData(SWT.CENTER, SWT.BOTTOM, false, false));
 		l = new Label(padComp, SWT.NONE);
 		l = new Label(padComp, SWT.NONE);
 
 		l = new Label(padComp, SWT.NONE);
 		Button leftButton = new Button(padComp, SWT.ARROW | SWT.LEFT);
 		leftButton.setToolTipText("Shift rings left");
 		leftButton.addMouseListener(new RepeatingMouseAdapter(display,
 				new SlowFastRunnable() {
 					@Override
 					public void run() {
 						DiffractionCalibrationUtils.changeRings(currentData, ManipulateMode.LEFT, isFast());
 					}
 
 					@Override
 					public void stop() {
 						if (currentData == null)
 							return;
 						updateDiffTool(BEAM_CENTRE_XPATH, currentData.md.getDetector2DProperties().getBeamCentreCoords()[0]);
 						refreshTable();
 					}
 				}));
 		leftButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
 		l = new Label(padComp, SWT.NONE);
 		l.setImage(Activator.getImage("icons/centre.png"));
 		l.setToolTipText("Move calibrant");
 		l.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
 		Button rightButton = new Button(padComp, SWT.ARROW | SWT.RIGHT);
 		rightButton.setToolTipText("Shift rings right");
 		rightButton.addMouseListener(new RepeatingMouseAdapter(display,
 				new SlowFastRunnable() {
 					@Override
 					public void run() {
 						DiffractionCalibrationUtils.changeRings(currentData, ManipulateMode.RIGHT, isFast());
 					}
 
 					@Override
 					public void stop() {
 						if (currentData == null)
 							return;
 						updateDiffTool(BEAM_CENTRE_XPATH, currentData.md.getDetector2DProperties().getBeamCentreCoords()[0]);
 						refreshTable();
 					}
 				}));
 		rightButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
 		l = new Label(padComp, SWT.NONE);
 
 		l = new Label(padComp, SWT.NONE);
 		l = new Label(padComp, SWT.NONE);
 		Button downButton = new Button(padComp, SWT.ARROW | SWT.DOWN);
 		downButton.setToolTipText("Move rings down");
 		downButton.addMouseListener(new RepeatingMouseAdapter(display,
 				new SlowFastRunnable() {
 					@Override
 					public void run() {
 						DiffractionCalibrationUtils.changeRings(currentData, ManipulateMode.DOWN, isFast());
 					}
 
 					@Override
 					public void stop() {
 						if (currentData == null)
 							return;
 						updateDiffTool(BEAM_CENTRE_YPATH, currentData.md.getDetector2DProperties().getBeamCentreCoords()[1]);
 						refreshTable();
 					}
 				}));
 		downButton.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false));
 		l = new Label(padComp, SWT.NONE);
 		l = new Label(padComp, SWT.NONE);
 
 		// Resize group actions
 		Composite actionComp = new Composite(controllerHolder, SWT.NONE);
 		actionComp.setLayout(new GridLayout(3, false));
 		actionComp.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true));
 
 		Composite sizeComp = new Composite(actionComp, SWT.BORDER);
 		sizeComp.setLayout(new GridLayout(1, false));
 		sizeComp.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, true));
 		sizeComp.setToolTipText("Change size");
 
 		Button plusButton = new Button(sizeComp, SWT.PUSH);
 		plusButton.setImage(Activator.getImage("icons/arrow_out.png"));
 		plusButton.setToolTipText("Make rings larger");
 		plusButton.addMouseListener(new RepeatingMouseAdapter(display,
 				new SlowFastRunnable() {
 					@Override
 					public void run() {
 						DiffractionCalibrationUtils.changeRings(currentData, ManipulateMode.ENLARGE, isFast());
 					}
 
 					@Override
 					public void stop() {
 						if (currentData == null)
 							return;
 						updateDiffTool(DISTANCE_NODE_PATH, currentData.md.getDetector2DProperties().getBeamCentreDistance());
 						refreshTable();
 					}
 				}));
 		plusButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
 		Button minusButton = new Button(sizeComp, SWT.PUSH);
 		minusButton.setImage(Activator.getImage("icons/arrow_in.png"));
 		minusButton.setToolTipText("Make rings smaller");
 		minusButton.addMouseListener(new RepeatingMouseAdapter(display,
 				new SlowFastRunnable() {
 					@Override
 					public void run() {
 						DiffractionCalibrationUtils.changeRings(currentData, ManipulateMode.SHRINK, isFast());
 					}
 
 					@Override
 					public void stop() {
 						if (currentData == null)
 							return;
 						updateDiffTool(DISTANCE_NODE_PATH, currentData.md.getDetector2DProperties().getBeamCentreDistance());
 						refreshTable();
 					}
 				}));
 		minusButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
 
 		Composite shapeComp = new Composite(actionComp, SWT.BORDER);
 		shapeComp.setLayout(new GridLayout(1, false));
 		shapeComp.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, true));
 		shapeComp.setToolTipText("Change shape");
 
 		Button elongateButton = new Button(shapeComp, SWT.PUSH);
 		elongateButton.setText("Elongate");
 		elongateButton.setToolTipText("Make rings more elliptical");
 		elongateButton.addMouseListener(new RepeatingMouseAdapter(display,
 				new SlowFastRunnable() {
 					@Override
 					public void run() {
 						DiffractionCalibrationUtils.changeRings(currentData, ManipulateMode.ELONGATE, isFast());
 					}
 
 					@Override
 					public void stop() {
 						// updateDiffTool(DISTANCE_NODE_PATH,
 						// currentData.md.getDetector2DProperties().getDetectorDistance());
 						refreshTable();
 					}
 				}));
 		elongateButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
 		Button squashButton = new Button(shapeComp, SWT.PUSH | SWT.FILL);
 		squashButton.setText("Squash");
 		squashButton.setToolTipText("Make rings more circular");
 		squashButton.addMouseListener(new RepeatingMouseAdapter(display,
 				new SlowFastRunnable() {
 					@Override
 					public void run() {
 						DiffractionCalibrationUtils.changeRings(currentData, ManipulateMode.SQUASH, isFast());
 					}
 
 					@Override
 					public void stop() {
 						// updateDiffTool(DISTANCE_NODE_PATH,
 						// currentData.md.getDetector2DProperties().getDetectorDistance());
 						refreshTable();
 					}
 				}));
 		squashButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
 
 		Composite rotateComp = new Composite(actionComp, SWT.BORDER);
 		rotateComp.setLayout(new GridLayout(1, false));
 		rotateComp.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, true));
 		rotateComp.setToolTipText("Change rotation");
 
 		Button clockButton = new Button(rotateComp, SWT.PUSH);
 		clockButton.setImage(Activator.getImage("icons/arrow_rotate_clockwise.png"));
 		clockButton.setToolTipText("Rotate rings clockwise");
 		clockButton.addMouseListener(new RepeatingMouseAdapter(display,
 				new SlowFastRunnable() {
 					@Override
 					public void run() {
 						DiffractionCalibrationUtils.changeRings(currentData, ManipulateMode.CLOCKWISE, isFast());
 					}
 
 					@Override
 					public void stop() {
 						refreshTable();
 					}
 				}));
 		clockButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
 		Button antiClockButton = new Button(rotateComp, SWT.PUSH);
 		antiClockButton.setImage(Activator.getImage("icons/arrow_rotate_anticlockwise.png"));
 		antiClockButton.setToolTipText("Rotate rings anti-clockwise");
 		antiClockButton.addMouseListener(new RepeatingMouseAdapter(display,
 				new SlowFastRunnable() {
 					@Override
 					public void run() {
 						DiffractionCalibrationUtils.changeRings(currentData, ManipulateMode.ANTICLOCKWISE, isFast());
 					}
 
 					@Override
 					public void stop() {
 						refreshTable();
 					}
 				}));
 		antiClockButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
 
 		Button setBeamCentreButton = new Button(controllerHolder, SWT.PUSH);
 		setBeamCentreButton.setText("Apply beam centre");
 		setBeamCentreButton.setToolTipText("Apply current beam centre to all the images");
 		setBeamCentreButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
 		setBeamCentreButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				DetectorProperties properties = currentData.md.getDetector2DProperties();
 				double[] coords = properties.getBeamCentreCoords();
 				for (int i = 0; i < model.size(); i++) {
 					model.get(i).md.getDetector2DProperties().setBeamCentreCoords(coords);
 				}
 			}
 		});
 
 		Button findRingButton = new Button(controllerHolder, SWT.PUSH);
 		findRingButton.setText("Match rings to image");
 		findRingButton.setToolTipText("Use pixel values to find rings in image near calibration rings");
 		findRingButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
 		findRingButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				Job findRingsJob = DiffractionCalibrationUtils.findRings(display, plottingSystem, currentData);
 				if (findRingsJob == null)
 					return;
 				findRingsJob.addJobChangeListener(new JobChangeAdapter() {
 					@Override
 					public void done(IJobChangeEvent event) {
 						display.asyncExec(new Runnable() {
 							@Override
 							public void run() {
 								if (currentData != null && currentData.nrois > 0) {
 									setCalibrateButtons();
 								}
 								refreshTable();
 							}
 						});
 					}
 				});
 				findRingsJob.schedule();
 			}
 		});
 
 		Composite rightCalibComp = new Composite(mainControlComp, SWT.NONE);
 		rightCalibComp.setLayout(new GridLayout(1, false));
 		rightCalibComp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
 
 		// Radio group
 		calibOptionGroup = new Group(rightCalibComp, SWT.BORDER);
 		calibOptionGroup.setLayout(new GridLayout(1, false));
 		calibOptionGroup.setText("Calibration options");
 		try {
 			RadioUtils.createRadioControls(calibOptionGroup, createWavelengthRadioActions());
 		} catch (Exception e) {
 			logger.error("Could not create controls:" + e);
 		}
 		calibrateImagesButton = new Button(calibOptionGroup, SWT.PUSH);
 		calibrateImagesButton.setText("Run Calibration Process");
 		calibrateImagesButton.setToolTipText("Calibrate detector in chosen images");
 		calibrateImagesButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
 		calibrateImagesButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				if (model.size() <= 0)
 					return;
 
 				Job calibrateJob = DiffractionCalibrationUtils.calibrateImages(display, plottingSystem, model, currentData,
 						usedFixedWavelength, postFitWavelength);
 				if (calibrateJob == null)
 					return;
 				calibrateJob.addJobChangeListener(new JobChangeAdapter() {
 					@Override
 					public void done(IJobChangeEvent event) {
 						display.asyncExec(new Runnable() {
 							public void run() {
 								refreshTable();
 								setCalibrateButtons();
 							}
 						});
 					}
 				});
 				calibrateJob.schedule();
 			}
 		});
 		setCalibrateOptionsEnabled(false);
 
 		wavelengthComp = new Group(rightCalibComp, SWT.NONE);
 		wavelengthComp.setText("X-Rays");
 		wavelengthComp.setToolTipText("Set the wavelength / energy");
 		wavelengthComp.setLayout(new GridLayout(3, false));
 		wavelengthComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
 
 		Label wavelengthLabel = new Label(wavelengthComp, SWT.NONE);
 		wavelengthLabel.setText("Wavelength");
 
 		wavelengthDistanceField = new FormattedText(wavelengthComp, SWT.SINGLE | SWT.BORDER);
 		wavelengthDistanceField.setFormatter(new NumberFormatter(EDIT_MASK, DISPLAY_MASK, Locale.UK));
 		wavelengthDistanceField.getControl().setToolTipText("Set the wavelength in Angstrom");
 		wavelengthDistanceField.getControl().addListener(SWT.KeyUp, new Listener() {
 			@Override
 			public void handleEvent(Event event) {
 				// update wavelength of each image
 				double distance = 0;
 				Object obj = wavelengthDistanceField.getValue();
 				if (obj instanceof Long)
 					distance = ((Long) obj).doubleValue();
 				else if (obj instanceof Double)
 					distance = (Double) obj;
 				for (int i = 0; i < model.size(); i++) {
 					model.get(i).md.getDiffractionCrystalEnvironment().setWavelength(distance);
 				}
 				// update wavelength in keV
 				double energy = getWavelengthEnergy(distance);
 				String newFormat = getFormatMask(distance);
 				wavelengthEnergyField.setFormatter(new NumberFormatter(EDIT_MASK, newFormat, Locale.UK));
 				wavelengthEnergyField.setValue(energy);
 				// update wavelength in diffraction tool tree viewer
 				NumericNode<Length> node = getDiffractionTreeNode(WAVELENGTH_NODE_PATH);
 				if (node.getUnit().equals(NonSI.ANGSTROM)) {
 					updateDiffTool(WAVELENGTH_NODE_PATH, distance);
 				} else if (node.getUnit().equals(NonSI.ELECTRON_VOLT)) {
 					updateDiffTool(WAVELENGTH_NODE_PATH, energy * 1000);
 				} else if (node.getUnit().equals(SI.KILO(NonSI.ELECTRON_VOLT))) {
 					updateDiffTool(WAVELENGTH_NODE_PATH, energy);
 				}
 			}
 		});
 		wavelengthDistanceField.getControl().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
 		Label unitDistanceLabel = new Label(wavelengthComp, SWT.NONE);
 		unitDistanceLabel.setText(NonSI.ANGSTROM.toString());
 
 		Label energyLabel = new Label(wavelengthComp, SWT.NONE);
 		energyLabel.setText("Energy");
 
 		wavelengthEnergyField = new FormattedText(wavelengthComp, SWT.SINGLE | SWT.BORDER);
 		wavelengthEnergyField.setFormatter(new NumberFormatter(EDIT_MASK, DISPLAY_MASK, Locale.UK));
 		wavelengthEnergyField.getControl().setToolTipText("Set the wavelength in keV");
 		wavelengthEnergyField.getControl().addListener(SWT.KeyUp, new Listener() {
 			@Override
 			public void handleEvent(Event event) {
 				// update wavelength of each image
 				double energy = 0;
 				Object obj = wavelengthEnergyField.getValue();
 				if (obj instanceof Long)
 					energy = ((Long) obj).doubleValue();
 				else if (obj instanceof Double)
 					energy = (Double) obj;
 				for (int i = 0; i < model.size(); i++) {
 					model.get(i).md.getDiffractionCrystalEnvironment().setWavelength(getWavelengthEnergy(energy));
 				}
 				// update wavelength in Angstrom
 				double distance = getWavelengthEnergy(energy);
 				String newFormat = getFormatMask(energy);
 				wavelengthDistanceField.setFormatter(new NumberFormatter(EDIT_MASK, newFormat, Locale.UK));
 				wavelengthDistanceField.setValue(distance);
 				// update wavelength in Diffraction tool tree viewer
 				NumericNode<Length> node = getDiffractionTreeNode(WAVELENGTH_NODE_PATH);
 				if (node.getUnit().equals(NonSI.ANGSTROM)) {
 					updateDiffTool(WAVELENGTH_NODE_PATH, distance);
 				} else if (node.getUnit().equals(NonSI.ELECTRON_VOLT)) {
 					updateDiffTool(WAVELENGTH_NODE_PATH, energy * 1000);
 				} else if (node.getUnit().equals(SI.KILO(NonSI.ELECTRON_VOLT))) {
 					updateDiffTool(WAVELENGTH_NODE_PATH, energy);
 				}
 			}
 		});
 		wavelengthEnergyField.getControl().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
 		Label unitEnergyLabel = new Label(wavelengthComp, SWT.NONE);
 		unitEnergyLabel.setText(SI.KILO(NonSI.ELECTRON_VOLT).toString());
 		// Enable/disable the modifiers
 		setXRaysModifiersEnabled(false);
 
 		scrollHolder.layout();
 		scrollComposite.setContent(scrollHolder);
 		scrollComposite.setExpandHorizontal(true);
 		scrollComposite.setExpandVertical(true);
 		scrollComposite.setMinSize(scrollHolder.computeSize(SWT.DEFAULT, SWT.DEFAULT));
 		scrollComposite.layout();
 		// end of Diffraction Calibration controls
 
 		// start plotting system
 		Composite plotComp = new Composite(mainSash, SWT.NONE);
 		plotComp.setLayout(new GridLayout(1, false));
 		plotComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
 		try {
 			ActionBarWrapper actionBarWrapper = ActionBarWrapper.createActionBars(plotComp, null);
 			plottingSystem = PlottingFactory.createPlottingSystem();
 			plottingSystem.createPlotPart(plotComp, "", actionBarWrapper, PlotType.IMAGE, this);
 			plottingSystem.setTitle("");
 			plottingSystem.getPlotComposite().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		} catch (Exception e1) {
 			logger.error("Could not create plotting system:" + e1);
 		}
 
 		// try to load the previous data saved in the memento
 		DiffractionTableData good = null;
 		for (String p : pathsList) {
 			if (!p.endsWith(".nxs")) {
 				DiffractionTableData d = createData(p, null);
 				if (good == null && d != null) {
 					good = d;
 					setWavelength(d);
 					setCalibrant();
 				}
 			}
 		}
 		tableViewer.refresh();
 		if (good != null) {
 			final DiffractionTableData g = good;
 			display.asyncExec(new Runnable() { // this is necessary to give the plotting system time to lay out itself
 				@Override
 				public void run() {
 					tableViewer.setSelection(new StructuredSelection(g));
 				}
 			});
 		}
 		if (model.size() > 0)
 			setXRaysModifiersEnabled(true);
 
 		// start diffraction tool
 		Composite diffractionToolComp = new Composite(leftSash, SWT.BORDER);
 		diffractionToolComp.setLayout(new FillLayout());
 		try {
 			toolSystem = (IToolPageSystem) plottingSystem.getAdapter(IToolPageSystem.class);
 			// Show tools here, not on a page.
 			toolSystem.setToolComposite(diffractionToolComp);
 			toolSystem.setToolVisible(DIFFRACTION_ID, ToolPageRole.ROLE_2D, null);
 		} catch (Exception e2) {
 			logger.error("Could not open diffraction tool:" + e2);
 		}
 
 		CalibrationFactory.addCalibrantSelectionListener(calibrantChangeListener);
 		// mainSash.setWeights(new int[] { 1, 2});
 	}
 
 	protected String getFormatMask(double value) {
 		String str = String.valueOf(value);
 		String result = "";
 		String decimal = str.substring(str.indexOf('.') + 1);
 		for (int i = 0; i < decimal.length(); i ++) {
 			result += "#";
 		}
 		return "##,##0." + result;
 	}
 
 	private void setCalibrateOptionsEnabled(boolean b) {
 		calibOptionGroup.setEnabled(b);
 		calibrateImagesButton.setEnabled(b);
 	}
 
 	private void setXRaysModifiersEnabled(boolean b) {
 		wavelengthComp.setEnabled(b);
 		wavelengthDistanceField.getControl().setEnabled(b);
 		wavelengthEnergyField.getControl().setEnabled(b);
 	}
 
 	private String[] names = new String[]{ "Image", "Number of rings", "Distance", 
 			"X beam centre", "Y beam centre", "Wavelength", "Energy", "Residuals", "Yaw", "Pitch", "Roll" };
 
 	private void createToolbarActions(Composite parent) {
 		ToolBar tb = new ToolBar(parent, SWT.NONE);
 		tb.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
 
 		Image exportImage = new Image(Display.getDefault(), Activator.getImageDescriptor("icons/page_white_excel.png").getImageData());
 		Image resetRingsImage = new Image(Display.getDefault(), Activator.getImageDescriptor("icons/reset_rings.png").getImageData());
 		Image resetImage = new Image(Display.getDefault(), Activator.getImageDescriptor("icons/table_delete.png").getImageData());
 		ToolItem exportItem = new ToolItem(tb, SWT.PUSH);
 		ToolItem resetRingsItem = new ToolItem(tb, SWT.PUSH);
 		ToolItem resetItem = new ToolItem(tb, SWT.PUSH);
 
 		Button exportButton = new Button(tb, SWT.PUSH);
 		exportItem.setToolTipText("Export metadata to XLS");
 		exportItem.setControl(exportButton);
 		exportItem.setImage(exportImage);
 
 		Button resetRingsButton = new Button(tb, SWT.PUSH);
 		resetRingsItem.setToolTipText("Remove found rings");
 		resetRingsItem.setControl(resetRingsButton);
 		resetRingsItem.setImage(resetRingsImage);
 
 		Button resetButton = new Button(tb, SWT.PUSH);
 		resetItem.setToolTipText("Reset metadata");
 		resetItem.setControl(resetButton);
 		resetItem.setImage(resetImage);
 
 		exportItem.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent event) {
 				FileDialog dialog = new FileDialog(Display.getDefault().getActiveShell(), SWT.SAVE);
 				dialog.setText("Save metadata to Comma Separated Value file");
 				dialog.setFilterNames(new String[] { "CSV Files", "All Files (*.*)" });
 				dialog.setFilterExtensions(new String[] { "*.csv", "*.*" });
 				//dialog.setFilterPath("c:\\"); // Windows path
 				dialog.setFileName("metadata.csv");
 				dialog.setOverwrite(true);
 				String savedFilePath = dialog.open();
 				if (savedFilePath != null) {
 					String[][] values = new String[model.size()][names.length];
 					for (int i = 0; i < model.size(); i++) {
 						DetectorProperties dp = model.get(i).md.getDetector2DProperties();
 						double wavelength = model.get(i).md.getDiffractionCrystalEnvironment().getWavelength();
 						// image
 						values[i][0] = model.get(i).name;
 						// number of rings
 						values[i][1] = String.valueOf(model.get(i).nrois);
 						// distance
 						values[i][2] = String.valueOf(dp.getDetectorDistance());
 						// X beam centre
 						values[i][3] = String.valueOf(dp.getBeamCentreCoords()[0]);
 						// Y beam centre
 						values[i][4] = String.valueOf(dp.getBeamCentreCoords()[1]);
 						// wavelength
 						values[i][5] = String.valueOf(wavelength);
 						// energy
 						values[i][6] = String.valueOf(getWavelengthEnergy(wavelength));
 						// residuals
 						if (model.get(i).q != null)
 							values[i][7] = String.format("%.2f", Math.sqrt(model.get(i).q.getResidual()));
 						// Orientation Yaw
 						values[i][8] = String.valueOf(dp.getNormalAnglesInDegrees()[0]);
 						// Orientation Pitch
 						values[i][9] = String.valueOf(dp.getNormalAnglesInDegrees()[1]);
 						// Orientation Roll
 						values[i][10] = String.valueOf(dp.getNormalAnglesInDegrees()[2]);
 					}
 					DiffractionCalibrationUtils.saveToCsvFile(savedFilePath, names, values);
 				}
 			}
 		});
 
 		resetRingsItem.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent event) {
 				DiffractionCalibrationUtils.hideFoundRings(plottingSystem);
 			}
 		});
 
 		resetItem.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent event) {
 				// select last item in table
 				if (model != null && model.size() > 0) {
 					tableViewer.setSelection(new StructuredSelection(model.get(model.size() - 1)));
 					for (int i = 0; i < model.size(); i++) {
 						// Restore original metadata
 						DetectorProperties originalProps = model.get(i).md.getOriginalDetector2DProperties();
 						DiffractionCrystalEnvironment originalEnvironment = model.get(i).md.getOriginalDiffractionCrystalEnvironment();
 						model.get(i).md.getDetector2DProperties().restore(originalProps);
 						model.get(i).md.getDiffractionCrystalEnvironment().restore(originalEnvironment);
 					}
 					// update diffraction tool viewer
 					updateDiffTool(BEAM_CENTRE_XPATH, currentData.md.getDetector2DProperties().getBeamCentreCoords()[0]);
 					updateDiffTool(BEAM_CENTRE_YPATH, currentData.md.getDetector2DProperties().getBeamCentreCoords()[1]);
 					updateDiffTool(DISTANCE_NODE_PATH, currentData.md.getDetector2DProperties().getBeamCentreDistance());
 
 					// update wavelength
 					double wavelength = currentData.md.getDiffractionCrystalEnvironment().getWavelength();
 					wavelengthEnergyField.setValue(getWavelengthEnergy(wavelength));
 					wavelengthDistanceField.setValue(wavelength);
 					// update wavelength in diffraction tool tree viewer
 					NumericNode<Length> node = getDiffractionTreeNode(WAVELENGTH_NODE_PATH);
 					if (node.getUnit().equals(NonSI.ANGSTROM)) {
 						updateDiffTool(WAVELENGTH_NODE_PATH, wavelength);
 					} else if (node.getUnit().equals(NonSI.ELECTRON_VOLT)) {
 						updateDiffTool(WAVELENGTH_NODE_PATH, getWavelengthEnergy(wavelength) * 1000);
 					} else if (node.getUnit().equals(SI.KILO(NonSI.ELECTRON_VOLT))) {
 						updateDiffTool(WAVELENGTH_NODE_PATH, getWavelengthEnergy(wavelength));
 					}
 					tableViewer.refresh();
 				}
 			}
 		});
 	}
 
 	private List<Entry<String, Action>> createWavelengthRadioActions() {
 		List<Entry<String, Action>> radioActions = new ArrayList<Entry<String, Action>>();
 
 		Action usedFixedWavelengthAction = new Action() {
 			@Override
 			public void run() {
 				postFitWavelength = false;
 				usedFixedWavelength = true;
 			}
 		};
 		usedFixedWavelengthAction.setToolTipText("Individual fit with fixed wavelength"); // TODO write a more detailed tool tip
 		Entry<String, Action> perImageFitWithFixedWavelength = new AbstractMap.SimpleEntry<String, Action>(
 				"Per Image Fit with fixed wavelength",
 				usedFixedWavelengthAction);
 
 		Action simultaneousFitAction = new Action() {
 			@Override
 			public void run() {
 				usedFixedWavelength = false;
 			}
 		};
 		simultaneousFitAction.setToolTipText("Fits all the parameters at once"); // TODO write a more detailed tool tip
 		Entry<String, Action> simultaneousFit = new AbstractMap.SimpleEntry<String, Action>(
 				"Fit wavelength and distance",
 				simultaneousFitAction);
 
 		Action postWavelengthAction = new Action() {
 			@Override
 			public void run() {
 				postFitWavelength = true;
 				usedFixedWavelength = true;
 			}
 		};
 		postWavelengthAction.setToolTipText("Per image fit, then refine wavelength"); // TODO write a more detailed tool tip
 		Entry<String, Action> perImageFitPostFixedWavelength = new AbstractMap.SimpleEntry<String, Action>(
 				"Per Image Fit then refine wavelength",
 				postWavelengthAction);
 
 		radioActions.add(perImageFitWithFixedWavelength);
 		radioActions.add(simultaneousFit);
 		radioActions.add(perImageFitPostFixedWavelength);
 
 		return radioActions;
 	}
 
 	private void updateDiffTool(String nodePath, double value) {
 		DiffractionTool diffTool = (DiffractionTool) toolSystem.getToolPage(DIFFRACTION_ID);
 		DiffractionTreeModel treeModel = diffTool.getModel();
 
 		NumericNode<Length> distanceNode = getDiffractionTreeNode(nodePath);
 		distanceNode.setDoubleValue(value);
 		treeModel.setNode(distanceNode, nodePath);
 
 		diffTool.refresh();
 	}
 
 	@SuppressWarnings("unchecked")
 	private NumericNode<Length> getDiffractionTreeNode(String nodePath) {
 		NumericNode<Length> node = null;
 		if (toolSystem == null)
 			return node;
 		DiffractionTool diffTool = (DiffractionTool) toolSystem.getToolPage(DIFFRACTION_ID);
 		DiffractionTreeModel treeModel = diffTool.getModel();
 		if (treeModel == null)
 			return node;
 		node = (NumericNode<Length>) treeModel.getNode(nodePath);
 		return node;
 	}
 
 	private void setWavelength(DiffractionTableData data) {
 		// set the wavelength
 		if (data != null) {
 			double wavelength = data.md.getOriginalDiffractionCrystalEnvironment().getWavelength();
 			wavelengthDistanceField.setValue(wavelength);
 			wavelengthEnergyField.setValue(getWavelengthEnergy(wavelength));
 		}
 	}
 
 	private void setCalibrant() {
 		// set the calibrant
 		CalibrationStandards standard = CalibrationFactory.getCalibrationStandards();
 		if (calibrantName != null) {
 			calibrantCombo.select(calibrantCombo.indexOf(calibrantName));
 			standard.setSelectedCalibrant(calibrantName, true);
 		} else {
 			calibrantCombo.select(calibrantCombo.indexOf(standard.getSelectedCalibrant()));
 		}
 	}
 
 	private double getWavelengthEnergy(double value) {
 		return 1. / (0.0806554465 * value); // constant from NIST CODATA 2006
 	}
 
 	private DiffractionTableData createData(String filePath, String dataFullName) {
 		// Test if the selection has already been loaded and is in the model
 		DiffractionTableData data = null;
 		if (filePath == null)
 			return data;
 
 		for (DiffractionTableData d : model) {
 			if (filePath.equals(d.path)) {
 				data = d;
 				break;
 			}
 		}
 
 		if (data == null) {
 			IDataset image = PlottingUtils.loadData(filePath, dataFullName);
 			if (image == null)
 				return data;
 			int j = filePath.lastIndexOf(File.separator);
 			String fileName = j > 0 ? filePath.substring(j + 1) : null;
 			image.setName(fileName + ":" + image.getName());
 
 			data = new DiffractionTableData();
 			data.path = filePath;
 			data.name = fileName;
 			data.image = image;
 			String[] statusString = new String[1];
 			data.md = DiffractionTool.getDiffractionMetadata(image, filePath, service, statusString);
 			model.add(data);
 		}
 
 		return data;
 	}
 
 	private void drawSelectedData(DiffractionTableData data) {
 		if (currentData != null) {
 			DiffractionImageAugmenter aug = currentData.augmenter;
 			if (aug != null)
 				aug.deactivate();
 		}
 
 		if (data.image == null)
 			return;
 
 		plottingSystem.clear();
 		plottingSystem.updatePlot2D(data.image, null, null);
 		plottingSystem.setTitle(data.name);
 		plottingSystem.getAxes().get(0).setTitle("");
 		plottingSystem.getAxes().get(1).setTitle("");
 		plottingSystem.setKeepAspect(true);
 		plottingSystem.setShowIntensity(false);
 
 		currentData = data;
 
 		DiffractionImageAugmenter aug = data.augmenter;
 		if (aug == null) {
 			aug = new DiffractionImageAugmenter(plottingSystem);
 			data.augmenter = aug;
 		}
 		aug.activate();
 		if (data.md != null) {
 			aug.setDiffractionMetadata(data.md);
 			// Add listeners to monitor metadata changes in diffraction tool
 			data.md.getDetector2DProperties().addDetectorPropertyListener(detectorPropertyListener);
 //			data.md.getDiffractionCrystalEnvironment().addDiffractionCrystalEnvironmentListener(diffractionCrystEnvListener);
 		}
 
 		DiffractionCalibrationUtils.hideFoundRings(plottingSystem);
 		DiffractionCalibrationUtils.drawCalibrantRings(aug);
 	}
 
 	@SuppressWarnings("rawtypes")
 	@Override
 	public Object getAdapter(Class key) {
 		if (key == IPlottingSystem.class) {
 			return plottingSystem;
 		} else if (key == IToolPageSystem.class) {
 			return plottingSystem.getAdapter(IToolPageSystem.class);
 		}
 		return super.getAdapter(key);
 	}
 
 	class MyContentProvider implements IStructuredContentProvider {
 		@Override
 		public void dispose() {
 		}
 
 		@Override
 		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
 		}
 
 		@Override
 		public Object[] getElements(Object inputElement) {
 			if (inputElement == null) {
 				return null;
 			}
 			return ((List<?>) inputElement).toArray();
 		}
 	}
 
 	private static final Image TICKED = Activator.getImageDescriptor("icons/ticked.png").createImage();
 	private static final Image UNTICKED = Activator.getImageDescriptor("icons/unticked.gif").createImage();
 
 	class MyLabelProvider implements ITableLabelProvider {
 		@Override
 		public void addListener(ILabelProviderListener listener) {
 		}
 
 		@Override
 		public void dispose() {
 		}
 
 		@Override
 		public boolean isLabelProperty(Object element, String property) {
 			return true;
 		}
 
 		@Override
 		public void removeListener(ILabelProviderListener listener) {
 		}
 
 		@Override
 		public Image getColumnImage(Object element, int columnIndex) {
 			if (columnIndex != 0)
 				return null;
 			if (element == null)
 				return null;
 
 			DiffractionTableData data = (DiffractionTableData) element;
 			if (data.use)
 				return TICKED;
 			return UNTICKED;
 		}
 
 		@Override
 		public String getColumnText(Object element, int columnIndex) {
 			if (columnIndex == 0)
 				return null;
 			if (element == null)
 				return null;
 
 			DiffractionTableData data = (DiffractionTableData) element;
 			if (columnIndex == 1) {
 				return data.name;
 			} else if (columnIndex == 2) {
 				if (data.rois == null)
 					return null;
 				return String.valueOf(data.nrois);
 			}
 
 			IDiffractionMetadata md = data.md;
 			if (md == null)
 				return null;
 
 			if (columnIndex == 3) {
 				DetectorProperties dp = md.getDetector2DProperties();
 				if (dp == null)
 					return null;
 				return String.format("%.2f", dp.getDetectorDistance());
 			} else if (columnIndex == 4) {
 				DetectorProperties dp = md.getDetector2DProperties();
 				if (dp == null)
 					return null;
 				return String.format("%.0f", dp.getBeamCentreCoords()[0]);
 			} else if (columnIndex == 5) {
 				DetectorProperties dp = md.getDetector2DProperties();
 				if (dp == null)
 					return null;
 				return String.format("%.0f", dp.getBeamCentreCoords()[1]);
 			} else if (columnIndex == 6) {
 				if (data.use && data.q != null) {
 					return String.format("%.2f", Math.sqrt(data.q.getResidual()));
 				}
 			}
 			return null;
 		}
 	}
 
 	class MyEditingSupport extends EditingSupport {
 		private TableViewer tv;
 		private int column;
 
 		public MyEditingSupport(TableViewer viewer, int col) {
 			super(viewer);
 			tv = viewer;
 			this.column = col;
 		}
 
 		@Override
 		protected CellEditor getCellEditor(Object element) {
 			return new CheckboxCellEditor(null, SWT.CHECK);
 		}
 
 		@Override
 		protected boolean canEdit(Object element) {
 			if (column == 0)
 				return true;
 			else
 				return false;
 		}
 
 		@Override
 		protected Object getValue(Object element) {
 			DiffractionTableData data = (DiffractionTableData) element;
 			return data.use;
 		}
 
 		@Override
 		protected void setValue(Object element, Object value) {
 			if (column == 0) {
 				DiffractionTableData data = (DiffractionTableData) element;
 				data.use = (Boolean) value;
 				tv.refresh();
 
 				setCalibrateButtons();
 			}
 		}
 	}
 
 	private void createColumns(TableViewer tv) {
 		TableViewerColumn tvc = new TableViewerColumn(tv, SWT.NONE);
 		tvc.setEditingSupport(new MyEditingSupport(tv, 0));
 		TableColumn tc = tvc.getColumn();
 		tc.setText("Use");
 		tc.setWidth(40);
 
 		tvc = new TableViewerColumn(tv, SWT.NONE);
 		tc = tvc.getColumn();
 		tc.setText("Image");
 		tc.setWidth(200);
 		tvc.setEditingSupport(new MyEditingSupport(tv, 1));
 
 		tvc = new TableViewerColumn(tv, SWT.NONE);
 		tc = tvc.getColumn();
 		tc.setText("# of rings");
 		tc.setWidth(75);
 		tvc.setEditingSupport(new MyEditingSupport(tv, 2));
 
 		tvc = new TableViewerColumn(tv, SWT.NONE);
 		tc = tvc.getColumn();
 		tc.setText("Distance");
 		tc.setToolTipText("in mm");
 		tc.setWidth(70);
 		tvc.setEditingSupport(new MyEditingSupport(tv, 3));
 
 		tvc = new TableViewerColumn(tv, SWT.NONE);
 		tc = tvc.getColumn();
 		tc.setText("X Position");
 		tc.setToolTipText("in Pixel");
 		tc.setWidth(80);
 		tvc.setEditingSupport(new MyEditingSupport(tv, 4));
 
 		tvc = new TableViewerColumn(tv, SWT.NONE);
 		tc = tvc.getColumn();
 		tc.setText("Y Position");
 		tc.setToolTipText("in Pixel");
 		tc.setWidth(80);
 		tvc.setEditingSupport(new MyEditingSupport(tv, 5));
 
 		tvc = new TableViewerColumn(tv, SWT.NONE);
 		tc = tvc.getColumn();
 		tc.setText("Residuals");
 		tc.setToolTipText("Root mean of squared residuals from fit");
 		tc.setWidth(80);
 		tvc.setEditingSupport(new MyEditingSupport(tv, 5));
 	}
 
 	private void refreshTable() {
 		if (tableViewer == null)
 			return;
 		tableViewer.refresh();
 		// reset the scroll composite
 		Rectangle r = scrollHolder.getClientArea();
 		scrollComposite.setMinSize(scrollHolder.computeSize(r.width, SWT.DEFAULT));
 		scrollHolder.layout();
 	}
 
 	private void setCalibrateButtons() {
 		// enable/disable calibrate button according to use column
 		int used = 0;
 		for (DiffractionTableData d : model) {
 			if (d.use && d.nrois > 0) {
 				used++;
 			}
 		}
 		setCalibrateOptionsEnabled(used > 0);
 	}
 
 	private void removeListeners() {
 		tableViewer.removeSelectionChangedListener(selectionChangeListener);
 		CalibrationFactory.removeCalibrantSelectionListener(calibrantChangeListener);
 		// deactivate the diffraction tool
 		DiffractionTool diffTool = (DiffractionTool) toolSystem.getToolPage(DIFFRACTION_ID);
 		if (diffTool != null)
 			diffTool.deactivate();
 		// deactivate each augmenter in loaded data
 		for (DiffractionTableData d : model) {
 			if (d.augmenter != null)
 				d.augmenter.deactivate();
 			d.md.getDetector2DProperties().removeDetectorPropertyListener(detectorPropertyListener);
 //			d.md.getDiffractionCrystalEnvironment().removeDiffractionCrystalEnvironmentListener(diffractionCrystEnvListener);
 		}
 		model.clear();
 		logger.debug("model emptied");
 	}
 
 	@Override
 	public void dispose() {
 		super.dispose();
 		removeListeners();
 		// FIXME Clear
 	}
 
 	@Override
 	public void setFocus() {
 		if (parent != null && !parent.isDisposed())
 			parent.setFocus();
 	}
 }
