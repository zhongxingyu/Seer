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
 import java.util.Map.Entry;
 
 import javax.measure.quantity.Length;
 import javax.measure.unit.NonSI;
 import javax.measure.unit.SI;
 
 import org.dawb.common.services.ILoaderService;
 import org.dawb.common.ui.widgets.ActionBarWrapper;
 import org.dawb.workbench.ui.Activator;
 import org.dawb.workbench.ui.views.DiffractionCalibrationUtils.ManipulateMode;
 import org.dawnsci.common.widgets.spinner.FloatSpinner;
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
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.IMemento;
 import org.eclipse.ui.IViewSite;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.part.ResourceTransfer;
 import org.eclipse.ui.part.ViewPart;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.crystallography.CalibrationFactory;
 import uk.ac.diamond.scisoft.analysis.crystallography.CalibrationStandards;
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 import uk.ac.diamond.scisoft.analysis.diffraction.DetectorProperties;
 import uk.ac.diamond.scisoft.analysis.diffraction.DetectorPropertyEvent;
 import uk.ac.diamond.scisoft.analysis.diffraction.DiffractionCrystalEnvironment;
 import uk.ac.diamond.scisoft.analysis.diffraction.DiffractionCrystalEnvironmentEvent;
 import uk.ac.diamond.scisoft.analysis.diffraction.IDetectorPropertyListener;
 import uk.ac.diamond.scisoft.analysis.diffraction.IDiffractionCrystalEnvironmentListener;
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
 
 	private List<DiffractionTableData> model = new ArrayList<DiffractionTableData>();
 	private ILoaderService service;
 
 	private Composite parent;
 	private ScrolledComposite scrollComposite;
 	private Composite scrollHolder;
 	private TableViewer tableViewer;
 	private Button calibrateImages;
 	private Combo calibrant;
 	private Action deleteAction;
 
 	private IPlottingSystem plottingSystem;
 
 	private ISelectionChangedListener selectionChangeListener;
 	private DropTargetAdapter dropListener;
 	private IDiffractionCrystalEnvironmentListener diffractionCrystEnvListener;
 	private IDetectorPropertyListener detectorPropertyListener;
 
 	private List<String> pathsList = new ArrayList<String>();
 
	private Button wavelengthButton;
 	private double wavelength;
 	private FloatSpinner wavelengthDistanceSpinner;
 	private FloatSpinner wavelengthEnergySpinner;
 
 	private IToolPageSystem toolSystem;
 
 	private Action resetAction;
 
 //	private boolean doNotRefineWavelength = true;
 	private boolean refineAfterDistance = false;
 	private boolean refineWithDistance = false;
 
 	public DiffractionCalibrationPlottingView() {
 		service = (ILoaderService) PlatformUI.getWorkbench().getService(ILoaderService.class);
 	}
 
 	private static final String DATA_PATH = "DataPath";
 
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
 		}
 	}
 
 	@Override
 	public void createPartControl(final Composite parent) {
 		parent.setLayout(new FillLayout());
 
 		this.parent = parent;
 
 		resetAction = new Action() {
 			@Override
 			public void run() {
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
 					wavelengthEnergySpinner.setDouble(getWavelengthEnergy(wavelength));
 					wavelengthDistanceSpinner.setDouble(wavelength);
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
 		};
 		resetAction.setText("Reset");
 		resetAction.setToolTipText("Reset metadata");
 		resetAction.setImageDescriptor(Activator.getImageDescriptor("icons/table_delete.png"));
 
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
 
 		final Display display = parent.getDisplay();
 
 		diffractionCrystEnvListener = new IDiffractionCrystalEnvironmentListener() {
 			@Override
 			public void diffractionCrystalEnvironmentChanged(
 					final DiffractionCrystalEnvironmentEvent evt) {
 				display.asyncExec(new Runnable() {
 					@Override
 					public void run() {
 						tableViewer.refresh();
 					}
 				});
 			}
 		};
 
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
 
 		dropListener = new DropTargetAdapter() {
 			@Override
 			public void drop(DropTargetEvent event) {
 				Object dropData = event.data;
 				DiffractionTableData good = null;
 				if (dropData instanceof IResource[]) {
 					IResource[] res = (IResource[]) dropData;
 					for (int i = 0; i < res.length; i++) {
 						DiffractionTableData d = createData(res[i].getRawLocation().toOSString(), null);
 						if (d != null){
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
 						if (d != null){
 							good = d;
 							setWavelength(d);
 						}
 					}
 				} else if (dropData instanceof String[]) {
 					String[] selectedData = (String[]) dropData;
 					for (int i = 0; i < selectedData.length; i++) {
 						DiffractionTableData d = createData(selectedData[i], null);
 						if (d != null){
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
 				if (model.size() > 0) {
 					wavelengthDistanceSpinner.setEnabled(true);
 					wavelengthEnergySpinner.setEnabled(true);
 				}
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
 						selectedData.md.getDiffractionCrystalEnvironment().removeDiffractionCrystalEnvironmentListener(diffractionCrystEnvListener);
 						tableViewer.refresh();
 					}
 				}
 				if (!model.isEmpty()) {
 					drawSelectedData((DiffractionTableData) tableViewer.getElementAt(0));
 				} else {
 					currentData = null; // need to reset this
 					plottingSystem.clear();
 					wavelengthDistanceSpinner.setEnabled(false);
 					wavelengthEnergySpinner.setEnabled(false);
 				}
 			}
 		};
 
 		// main sash form which contains the left sash and the plotting system
 		SashForm mainSash = new SashForm(parent, SWT.HORIZONTAL);
 		mainSash.setBackground(new Color(display, 192, 192, 192));
 		mainSash.setLayout(new FillLayout());
 
 		// left sash form which contains the diffraction calibration controls and the diffraction tool
 		SashForm leftSash = new SashForm(mainSash, SWT.VERTICAL);
 		leftSash.setBackground(new Color(display, 192, 192, 192));
 		leftSash.setLayout(new GridLayout(1, false));
 		leftSash.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
 
 		Composite controlComp = new Composite(leftSash, SWT.NONE);
 		controlComp.setLayout(new GridLayout(1, false));
 		
 		Label instructionLabel = new Label(controlComp, SWT.WRAP);
 		instructionLabel.setText("Drag/drop a file/data to the table below, choose a type of calibrant, " +
 								 "modify the rings using the controls below and tick the checkbox near to the corresponding " +
 								 "image before pressing the calibration buttons.");
 		instructionLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false));
 
 		// make a scrolled composite
 		scrollComposite = new ScrolledComposite(controlComp, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
 		scrollComposite.setLayout(new GridLayout(1, false));
 		scrollComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		scrollHolder = new Composite(scrollComposite, SWT.NONE);
 
 		GridLayout gl = new GridLayout(1, false);
 		scrollHolder.setLayout(gl);
 		scrollHolder.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true));
 
 		// table of images and found rings
 		tableViewer = new TableViewer(scrollHolder, SWT.FULL_SELECTION | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
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
 					deleteAction.setText("Delete "
 							+ ((DiffractionTableData) selection.getFirstElement()).name);
 					mgr.add(deleteAction);
 				}
 			}
 		});
 		tableViewer.getControl().setMenu(mgr.createContextMenu(tableViewer.getControl()));
 		//add drop support
 		DropTarget dt = new DropTarget(tableViewer.getControl(), DND.DROP_MOVE| DND.DROP_DEFAULT| DND.DROP_COPY);
 		dt.setTransfer(new Transfer[] { TextTransfer.getInstance (), FileTransfer.getInstance(), 
 										ResourceTransfer.getInstance(), LocalSelectionTransfer.getTransfer()});
 		dt.addDropListener(dropListener);
 
 		Composite calibrantHolder = new Composite(scrollHolder, SWT.NONE);
 		calibrantHolder.setLayout(new GridLayout(1, false));
 		calibrantHolder.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
 
 		Composite mainControlComp = new Composite(calibrantHolder, SWT.NONE);
 		mainControlComp.setLayout(new GridLayout(2, false));
 		mainControlComp.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
 
 		Group controllerHolder = new Group(mainControlComp, SWT.BORDER);
 		controllerHolder.setText("Calibrant selection and positioning");
 		controllerHolder.setLayout(new GridLayout(2, false));
 		controllerHolder.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
 
 		// create calibrant combo
 		Label l = new Label(controllerHolder, SWT.NONE);
 		l.setText("Calibrant:");
 		l.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
 		calibrant = new Combo(controllerHolder, SWT.READ_ONLY);
 		final CalibrationStandards standards = CalibrationFactory.getCalibrationStandards();
 		calibrant.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				standards.setSelectedCalibrant(calibrant.getItem(calibrant.getSelectionIndex()));
 				DiffractionCalibrationUtils.drawCalibrantRings(currentData.augmenter);
 			}
 		});
 		for (String c : standards.getCalibrantList()) {
 			calibrant.add(c);
 		}
 		String s = standards.getSelectedCalibrant();
 		if (s != null) {
 			calibrant.setText(s);
 		}
 		calibrant.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
 
 		Composite padComp = new Composite(controllerHolder, SWT.BORDER);
 		padComp.setLayout(new GridLayout(5, false));
 		padComp.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
 		padComp.setToolTipText("Move calibrant");
 
 		l = new Label(padComp, SWT.NONE);
 		l = new Label(padComp, SWT.NONE);
 		Button upButton = new Button(padComp, SWT.ARROW | SWT.UP);
 		upButton.setToolTipText("Move rings up");
 		upButton.addMouseListener(new RepeatingMouseAdapter(display, new SlowFastRunnable() {
 			@Override
 			public void run() {
 				DiffractionCalibrationUtils.changeRings(currentData, ManipulateMode.UP, isFast());
 			}
 
 			@Override
 			public void stop() {
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
 		leftButton.addMouseListener(new RepeatingMouseAdapter(display, new SlowFastRunnable() {
 			@Override
 			public void run() {
 				DiffractionCalibrationUtils.changeRings(currentData, ManipulateMode.LEFT, isFast());
 			}
 
 			@Override
 			public void stop() {
 				updateDiffTool(BEAM_CENTRE_XPATH, currentData.md.getDetector2DProperties().getBeamCentreCoords()[0]);
 				refreshTable();
 			}
 		}));
 		leftButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
 		l = new Label(padComp, SWT.NONE);
 		Button rightButton = new Button(padComp, SWT.ARROW | SWT.RIGHT);
 		rightButton.setToolTipText("Shift rings right");
 		rightButton.addMouseListener(new RepeatingMouseAdapter(display, new SlowFastRunnable() {
 			@Override
 			public void run() {
 				DiffractionCalibrationUtils.changeRings(currentData, ManipulateMode.RIGHT, isFast());
 			}
 
 			@Override
 			public void stop() {
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
 		downButton.addMouseListener(new RepeatingMouseAdapter(display, new SlowFastRunnable() {
 			@Override
 			public void run() {
 				DiffractionCalibrationUtils.changeRings(currentData, ManipulateMode.DOWN, isFast());
 			}
 
 			@Override
 			public void stop() {
 				updateDiffTool(BEAM_CENTRE_YPATH, currentData.md.getDetector2DProperties().getBeamCentreCoords()[1]);
 				refreshTable();
 			}
 		}));
 		downButton.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false));
 		l = new Label(padComp, SWT.NONE);
 		l = new Label(padComp, SWT.NONE);
 
 		Composite actionComp = new Composite(controllerHolder, SWT.NONE);
 		actionComp.setLayout(new GridLayout(3, false));
 		actionComp.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true));
 
 		Composite sizeComp = new Composite(actionComp, SWT.BORDER);
 		sizeComp.setLayout(new GridLayout(1, false));
 		sizeComp.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true));
 		sizeComp.setToolTipText("Change size");
 
 		Button plusButton = new Button(sizeComp, SWT.PUSH);
 		plusButton.setImage(Activator.getImage("icons/arrow_out.png"));
 		plusButton.setToolTipText("Make rings larger");
 		plusButton.addMouseListener(new RepeatingMouseAdapter(display, new SlowFastRunnable() {
 			@Override
 			public void run() {
 				DiffractionCalibrationUtils.changeRings(currentData, ManipulateMode.ENLARGE, isFast());
 			}
 
 			@Override
 			public void stop() {
 				updateDiffTool(DISTANCE_NODE_PATH, currentData.md.getDetector2DProperties().getBeamCentreDistance());
 				refreshTable();
 			}
 		}));
 		plusButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
 		Button minusButton = new Button(sizeComp, SWT.PUSH);
 		minusButton.setImage(Activator.getImage("icons/arrow_in.png"));
 		minusButton.setToolTipText("Make rings smaller");
 		minusButton.addMouseListener(new RepeatingMouseAdapter(display, new SlowFastRunnable() {
 			@Override
 			public void run() {
 				DiffractionCalibrationUtils.changeRings(currentData, ManipulateMode.SHRINK, isFast());
 			}
 
 			@Override
 			public void stop() {
 				updateDiffTool(DISTANCE_NODE_PATH, currentData.md.getDetector2DProperties().getBeamCentreDistance());
 				refreshTable();
 			}
 		}));
 		minusButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
 
 		Composite shapeComp = new Composite(actionComp, SWT.BORDER);
 		shapeComp.setLayout(new GridLayout(1, false));
 		shapeComp.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true));
 		shapeComp.setToolTipText("Change shape");
 
 		Button elongateButton = new Button(shapeComp, SWT.PUSH);
 		elongateButton.setText("Elongate");
 		elongateButton.setToolTipText("Make rings more elliptical");
 		elongateButton.addMouseListener(new RepeatingMouseAdapter(display, new SlowFastRunnable() {
 			@Override
 			public void run() {
 				DiffractionCalibrationUtils.changeRings(currentData, ManipulateMode.ELONGATE, isFast());
 			}
 
 			@Override
 			public void stop() {
 //				updateDiffTool(DISTANCE_NODE_PATH, currentData.md.getDetector2DProperties().getDetectorDistance());
 				refreshTable();
 			}
 		}));
 		elongateButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
 		Button squashButton = new Button(shapeComp, SWT.PUSH | SWT.FILL);
 		squashButton.setText("Squash");
 		squashButton.setToolTipText("Make rings more circular");
 		squashButton.addMouseListener(new RepeatingMouseAdapter(display, new SlowFastRunnable() {
 			@Override
 			public void run() {
 				DiffractionCalibrationUtils.changeRings(currentData, ManipulateMode.SQUASH, isFast());
 			}
 
 			@Override
 			public void stop() {
 //				updateDiffTool(DISTANCE_NODE_PATH, currentData.md.getDetector2DProperties().getDetectorDistance());
 				refreshTable();
 			}
 		}));
 		squashButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
 
 		Composite rotateComp = new Composite(actionComp, SWT.BORDER);
 		rotateComp.setLayout(new GridLayout(1, false));
 		rotateComp.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true));
 		rotateComp.setToolTipText("Change rotation");
 
 		Button clockButton = new Button(rotateComp, SWT.PUSH);
 		clockButton.setImage(Activator.getImage("icons/arrow_rotate_clockwise.png"));
 		clockButton.setToolTipText("Rotate rings clockwise");
 		clockButton.addMouseListener(new RepeatingMouseAdapter(display, new SlowFastRunnable() {
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
 		antiClockButton.addMouseListener(new RepeatingMouseAdapter(display, new SlowFastRunnable() {
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
 
 		Composite calibrateComp = new Composite(mainControlComp, SWT.NONE);
 		calibrateComp.setLayout(new GridLayout(1, false));
 		calibrateComp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
 
 		try{
 			RadioUtils.createRadioControls(calibrateComp, createWavelengthRadioActions());
 		} catch (Exception e) {
 			logger.error("Could not create controls:"+e);
 		}
 
 		Group wavelengthComp = new Group(calibrateComp, SWT.NONE);
 		wavelengthComp.setText("X-Rays");
 		wavelengthComp.setToolTipText("Set the wavelength / energy");
 		wavelengthComp.setLayout(new GridLayout(3, false));
 		wavelengthComp.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
 
 		Label wavelengthLabel = new Label(wavelengthComp, SWT.NONE);
 		wavelengthLabel.setText("Wavelength");
 
 		wavelengthDistanceSpinner = new FloatSpinner(wavelengthComp, SWT.BORDER);
 		wavelengthDistanceSpinner.setDouble(0);
 		wavelengthDistanceSpinner.setFormat(7, 5);
 		wavelengthDistanceSpinner.setMinimum(0);
 		wavelengthDistanceSpinner.setMaximum(Double.MAX_VALUE);
 		wavelengthDistanceSpinner.setIncrement(0.001);
 		wavelengthDistanceSpinner.setToolTipText("Set the wavelength in Angstrom");
 		wavelengthDistanceSpinner.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				// update wavelength of each image
 				for(int i = 0; i < model.size(); i++){
 					model.get(i).md.getDiffractionCrystalEnvironment().setWavelength(wavelengthDistanceSpinner.getDouble());
 				}
 				// update wavelength in keV
 				wavelengthEnergySpinner.setDouble(getWavelengthEnergy(wavelengthDistanceSpinner.getDouble()));
 				// update wavelength in diffraction tool tree viewer
 				NumericNode<Length> node = getDiffractionTreeNode(WAVELENGTH_NODE_PATH);
 				if (node.getUnit().equals(NonSI.ANGSTROM)) {
 					updateDiffTool(WAVELENGTH_NODE_PATH, wavelengthDistanceSpinner.getDouble());
 				} else if (node.getUnit().equals(NonSI.ELECTRON_VOLT)) {
 					updateDiffTool(WAVELENGTH_NODE_PATH, getWavelengthEnergy(wavelengthDistanceSpinner.getDouble()) * 1000);
 				} else if (node.getUnit().equals(SI.KILO(NonSI.ELECTRON_VOLT))) {
 					updateDiffTool(WAVELENGTH_NODE_PATH, getWavelengthEnergy(wavelengthDistanceSpinner.getDouble()));
 				}
 			}
 		});
 		wavelengthDistanceSpinner.setEnabled(false);
 		Label unitDistanceLabel = new Label(wavelengthComp, SWT.NONE);
 		unitDistanceLabel.setText(NonSI.ANGSTROM.toString());
 
 		Label energyLabel = new Label(wavelengthComp, SWT.NONE);
 		energyLabel.setText("Energy");
 
 		wavelengthEnergySpinner = new FloatSpinner(wavelengthComp, SWT.BORDER);
 		wavelengthEnergySpinner.setDouble(0);
 		wavelengthEnergySpinner.setFormat(7, 5);
 		wavelengthEnergySpinner.setMinimum(0);
 		wavelengthEnergySpinner.setMaximum(Double.MAX_VALUE);
 		wavelengthEnergySpinner.setIncrement(0.001);
 		wavelengthEnergySpinner.setToolTipText("Set the wavelength in keV");
 		wavelengthEnergySpinner.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				// update wavelength of each image
 				for(int i = 0; i < model.size(); i++){
 					model.get(i).md.getDiffractionCrystalEnvironment().setWavelength(getWavelengthDistance(wavelengthEnergySpinner.getDouble()));
 				}
 				// update wavelength in Angstrom
 				wavelengthDistanceSpinner.setDouble(getWavelengthDistance(wavelengthEnergySpinner.getDouble()));
 				// update wavelength in Diffraction tool tree viewer
 				NumericNode<Length> node = getDiffractionTreeNode(WAVELENGTH_NODE_PATH);
 				if (node.getUnit().equals(NonSI.ANGSTROM)) {
 					updateDiffTool(WAVELENGTH_NODE_PATH, getWavelengthDistance(wavelengthEnergySpinner.getDouble()));
 				} else if (node.getUnit().equals(NonSI.ELECTRON_VOLT)) {
 					updateDiffTool(WAVELENGTH_NODE_PATH, wavelengthEnergySpinner.getDouble() * 1000);
 				} else if (node.getUnit().equals(SI.KILO(NonSI.ELECTRON_VOLT))) {
 					updateDiffTool(WAVELENGTH_NODE_PATH, wavelengthEnergySpinner.getDouble());
 				}
 			}
 		});
 		wavelengthEnergySpinner.setEnabled(false);
 		Label unitEnergyLabel = new Label(wavelengthComp, SWT.NONE);
 		unitEnergyLabel.setText(SI.KILO(NonSI.ELECTRON_VOLT).toString());
 
 		Composite processComp = new Composite(calibrantHolder, SWT.NONE);
 		processComp.setLayout(new GridLayout(2, false));
 		processComp.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
 
 		Button findRingButton = new Button(processComp, SWT.PUSH);
 		findRingButton.setText("Find rings in image");
 		findRingButton.setToolTipText("Use pixel values to find rings in image near calibration rings");
 		findRingButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
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
 
 		calibrateImages = new Button(processComp, SWT.PUSH);
 		calibrateImages.setText("Run Calibration Process");
 		calibrateImages.setToolTipText("Calibrate detector in chosen images");
 		calibrateImages.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
 		calibrateImages.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				if (model.size() <= 0)
 					return;
 
 				Job calibrateJob = DiffractionCalibrationUtils.calibrateImages(display, plottingSystem, model, currentData,
 						refineWithDistance || refineAfterDistance, 
 						refineAfterDistance);
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
 		calibrateImages.setEnabled(false);
 
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
 			logger.error("Could not create plotting system:"+ e1);
 		}
 
 		// try to load the previous data saved in the memento
 		DiffractionTableData good = null;
 		for (String p : pathsList) {
 			if (!p.endsWith(".nxs")) {
 				DiffractionTableData d = createData(p, null);
 				if (good == null && d != null) {
 					good = d;
 					setWavelength(d);
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
 		if (model.size() > 0) {
 			wavelengthDistanceSpinner.setEnabled(true);
 			wavelengthEnergySpinner.setEnabled(true);
 		}
 
 		// start diffraction tool 
 		Composite diffractionToolComp = new Composite(leftSash, SWT.BORDER);
 		diffractionToolComp.setLayout(new FillLayout());
 		try {
 			toolSystem = (IToolPageSystem)plottingSystem.getAdapter(IToolPageSystem.class);
 			// Show tools here, not on a page.
 			toolSystem.setToolComposite(diffractionToolComp);
 			toolSystem.setToolVisible(DIFFRACTION_ID, ToolPageRole.ROLE_2D, null);
 		} catch (Exception e2) {
 			logger.error("Could not open diffraction tool:"+ e2);
 		}
 
 		IActionBars bars = getViewSite().getActionBars();
 		bars.getToolBarManager().add(resetAction);
 		//mainSash.setWeights(new int[] { 1, 2});
 	}
 
 	private List<Entry<String, Action>> createWavelengthRadioActions(){
 		List<Entry<String, Action>> radioActions = new ArrayList<Entry<String, Action>>();
 		Entry<String, Action> noNormalisation = new AbstractMap.SimpleEntry<String, Action>("Do not refine wavelength",
 			new Action("NoRefine") {
 				@Override
 				public void run() {
 					refineAfterDistance = false;
 					refineWithDistance = false;
 				}
 			}
 		);
 		Entry<String, Action> roiNormalisation = new AbstractMap.SimpleEntry<String, Action>("Refine wavelength after distance",
 				new Action("AfterDistance") {
 					@Override
 					public void run() {
 						refineAfterDistance = true;
 						refineWithDistance = false;
 					}
 				}
 			);
 		Entry<String, Action> auxNormalisation = new AbstractMap.SimpleEntry<String, Action>("Refine wavelength with distance",
 				new Action("WithDistance") {
 					@Override
 					public void run() {
 						refineAfterDistance = false;
 						refineWithDistance = true;
 					}
 				}
 			);
 		radioActions.add(noNormalisation);
 		radioActions.add(roiNormalisation);
 		radioActions.add(auxNormalisation);
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
 		if(treeModel == null)
 			return node;
 		node = (NumericNode<Length>) treeModel.getNode(nodePath);
 		return node;
 	}
 
 	private void setWavelength(DiffractionTableData data){
 		// set the wavelength
 		if(wavelengthDistanceSpinner.getDouble() == 0 && wavelengthEnergySpinner.getDouble() == 0){
 			if(data != null){
 				wavelength = data.md.getOriginalDiffractionCrystalEnvironment().getWavelength();
 				wavelengthDistanceSpinner.setDouble(wavelength);
 				wavelengthEnergySpinner.setDouble(getWavelengthEnergy(wavelength));
 			}
 		}
 	}
 
 	private double getWavelengthEnergy(double angstrom) {
 		return 1./(0.0806554465 * angstrom); // constant from NIST CODATA 2006
 	}
 
 	private double getWavelengthDistance(double keV) {
 		return 1./(0.0806554465*keV); // constant from NIST CODATA 2006
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
 			data.md.getDiffractionCrystalEnvironment().addDiffractionCrystalEnvironmentListener(diffractionCrystEnvListener);
 		}
 	
 		DiffractionCalibrationUtils.hideFoundRings(plottingSystem);
 		DiffractionCalibrationUtils.drawCalibrantRings(aug);
 	}
 
 	@SuppressWarnings("rawtypes")
 	@Override
 	public Object getAdapter(Class key) {
 		if (key==IPlottingSystem.class) {
 			return plottingSystem;
 		} else if (key==IToolPageSystem.class) {
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
 		public void addListener(ILabelProviderListener listener) {}
 
 		@Override
 		public void dispose() {}
 
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
 			if(column == 0)
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
 			if(column == 0){
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
 			if (d.use) {
 				used++;
 			}
 		}
 		calibrateImages.setEnabled(used > 0);
		wavelengthButton.setEnabled(used > 0);
 	}
 
 	private void removeListeners() {
 		tableViewer.removeSelectionChangedListener(selectionChangeListener);
 		for (DiffractionTableData d : model) {
 			if (d.augmenter != null)
 				d.augmenter.deactivate();
 			d.md.getDetector2DProperties().removeDetectorPropertyListener(detectorPropertyListener);
 			d.md.getDiffractionCrystalEnvironment().removeDiffractionCrystalEnvironmentListener(diffractionCrystEnvListener);
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
 		if(parent != null && !parent.isDisposed())
 			parent.setFocus();
 	}
 }
