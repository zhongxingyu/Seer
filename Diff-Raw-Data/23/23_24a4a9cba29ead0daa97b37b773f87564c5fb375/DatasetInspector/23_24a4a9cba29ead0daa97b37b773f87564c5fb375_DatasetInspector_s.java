 /*
  * Copyright 2012 Diamond Light Source Ltd.
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
 
 package uk.ac.diamond.scisoft.analysis.rcp.inspector;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CTabFolder;
 import org.eclipse.swt.custom.CTabItem;
 import org.eclipse.swt.custom.ScrolledComposite;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.ui.ISelectionListener;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.IWorkbenchPartSite;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.SDAPlotter;
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.Slice;
 import uk.ac.diamond.scisoft.analysis.rcp.explorers.AbstractExplorer;
 import uk.ac.diamond.scisoft.analysis.rcp.inspector.DatasetSelection.InspectorType;
 import uk.ac.diamond.scisoft.analysis.rcp.queue.InteractiveJobAdapter;
 import uk.ac.diamond.scisoft.analysis.rcp.queue.InteractiveQueue;
 import uk.ac.gda.monitor.ProgressMonitorWrapper;
 
 /**
  * Class to allow a user to select from a set of axes and datasets and configure
  * some plotting or viewing of data.
  * 
  * It listens to selection events
  */
 public class DatasetInspector extends Composite {
 
 	private static final Logger logger = LoggerFactory.getLogger(DatasetInspector.class);
 
 	private Display display = null;
 	private IWorkbenchPartSite site;
 
 	private static final String PLOTNAME = "Dataset Plot";
 
 	private ILazyDataset cData; // chosen dataset
 
 	// GUI elements
 	Label dsDetails;
 	private AxisSelectionTable axisSelector;
 	CTabFolder plotTabFolder;
 
 	InspectionTab cInspectionTab; // current inspection tab
 
 	private ISelectionListener selListener;
 
 	/**
 	 * This holds the configuration of an inspection
 	 */
 	class Inspection {
 		private InspectorType itype; // current type
 		private Map<InspectorType, List<SliceProperty>> allSlices;
 		private Map<InspectorType, List<PlotAxisProperty>> allPlotAxes;
 		private List<AxisSelection> datasetAxes;
 		private PropertyChangeListener sliceListener;
 		private PropertyChangeListener plotAxisListener;
 		private PropertyChangeListener datasetAxisListener;
 
 		public Inspection(DatasetSelection selection) {
 			datasetAxes = selection.getAxes() == null ? new ArrayList<AxisSelection>() : new ArrayList<AxisSelection>(selection.getAxes());
 			allSlices = new HashMap<DatasetSelection.InspectorType, List<SliceProperty>>();
 			allPlotAxes = new HashMap<DatasetSelection.InspectorType, List<PlotAxisProperty>>();
 			itype = selection.getType();
 		}
 
 		/**
 		 * Makes a deep copy of inspection subject to new selection
 		 * @param selection
 		 * @return new inspection
 		 */
 		public Inspection clone(DatasetSelection selection) {
 			Inspection n = new Inspection(selection);
 			if (n.datasetAxes.size() > 0) { 
 				for (int i = 0, imax = datasetAxes.size(); i < imax; i++) {
 					AxisSelection a = n.datasetAxes.get(i);
 					a.selectAxis(datasetAxes.get(i).getSelectedIndex(), false);
 				}
 			}
 
 			n.itype = itype;
 			List<SliceProperty> nSlices = new ArrayList<SliceProperty>();
 			n.allSlices.put(itype, nSlices);
 
 			for (SliceProperty p : allSlices.get(itype)) {
 				nSlices.add(p.clone());
 			}
 
 			List<PlotAxisProperty> nPAxes = new ArrayList<PlotAxisProperty>();
 			n.allPlotAxes.put(itype, nPAxes);
 
 			for (PlotAxisProperty p : allPlotAxes.get(itype)) {
 				nPAxes.add(p.clone());
 			}
 
 			n.setDatasetAxisListener(datasetAxisListener);
 			n.setSliceListener(slicerListener);
 			n.setPlotAxisListener(plotAxisListener);
 			return n;
 		}
 
 		public int getNumAxes() {
 			if (datasetAxes == null)
 				return 0;
 			return datasetAxes.size();
 		}
 
 		public List<SliceProperty> getSlices() {
 			return allSlices.get(itype);
 		}
 
 		public List<PlotAxisProperty> getPlotAxes() {
 			return allPlotAxes.get(itype);
 		}
 
 		public void addDatasetAxis(AxisSelection axis) {
 			if (datasetAxes == null)
 				datasetAxes = new ArrayList<AxisSelection>();
 			datasetAxes.add(axis);
 		}
 
 		public void addSlice(SliceProperty slice) {
 			allSlices.get(itype).add(slice);
 		}
 
 		public void addPlotAxis(PlotAxisProperty axis) {
 			allPlotAxes.get(itype).add(axis);
 		}
 
 		public void switchType(InspectorType type) {
 			if (itype == type)
 				return;
 
 			removePropertyChangeListener();
 
 			initSlice(type);
 			initPlotAxes(inspectionTabs.get(type));
 
 			itype = type;
 			setSliceListener(sliceListener);
 			setPlotAxisListener(plotAxisListener);
 		}
 
 		public void setSliceListener(PropertyChangeListener listener) {
 			sliceListener = listener;
 			List<SliceProperty> slices = getSlices();
 			if (slices != null) {
 				for (SliceProperty p : slices) {
 					p.addPropertyChangeListener(listener);
 				}
 			}
 		}
 
 		public void setPlotAxisListener(PropertyChangeListener listener) {
 			plotAxisListener = listener;
 			List<PlotAxisProperty> plotAxes = getPlotAxes();
 			if (plotAxes != null) {
 				for (PlotAxisProperty p : plotAxes) {
 					p.addPropertyChangeListener(listener);
 				}
 			}
 		}
 
 		public void setDatasetAxisListener(PropertyChangeListener listener) {
 			datasetAxisListener = listener;
 			if (datasetAxes != null) {
 				for (AxisSelection a : datasetAxes) {
 					a.addPropertyChangeListener(listener);
 				}
 			}
 		}
 
 		private void removePropertyChangeListener() {
 			List<SliceProperty> slices = getSlices();
 			if (slices != null) {
 				for (SliceProperty p : slices) {
 					p.removePropertyChangeListener(sliceListener);
 				}
 			}
 
 			List<PlotAxisProperty> plotAxes = getPlotAxes();
 			if (plotAxes != null) {
 				for (PlotAxisProperty p : plotAxes) {
 					p.removePropertyChangeListener(plotAxisListener);
 				}
 			}
 
 			if (datasetAxes != null) {
 				for (AxisSelection a : datasetAxes) {
 					a.removePropertyChangeListener(datasetAxisListener);
 				}
 			}
 		}
 
 		public void initSlice(InspectorType type) {
 			if (type == InspectorType.EMPTY || allSlices.get(type) != null)
 				return;
 
 			List<SliceProperty> list = new ArrayList<SliceProperty>();
 			allSlices.put(type, list);
 
 			int[] shape = cData.getShape();
 			int rank = shape.length;
 
 			for (int i = 0; i < rank; i++) {
 				SliceProperty p = new SliceProperty();
 				p.setLength(shape[i]);
 				list.add(p);
 			}
 		}
 
 		public void initPlotAxes(InspectionTab tab) {
 			InspectorType type = tab.getType();
 			if (type == InspectorType.EMPTY || allPlotAxes.get(type) != null)
 				return;
 
 			List<PlotAxisProperty> list = new ArrayList<PlotAxisProperty>();
 			allPlotAxes.put(type, list);
 
 			if (tab.canPlotConstant())
 				list.add(new PlotAxisProperty());
 
 			int n = tab.getNumAxes();
 			for (int i = 0; i < n; i++)
 				list.add(new PlotAxisProperty());
 		}
 	}
 
 	private Inspection inspection;
 	private Map<DatasetSelection, Inspection> storedInspections;
 	private Map<InspectorType, InspectionTab> inspectionTabs;
 
 	private ArrayList<AxisSlicer> slicers;
 
 	private PropertyChangeListener slicerListener;
 
 	private PropertyChangeListener plotTabListener;
 
 	private PropertyChangeListener datasetAxesListener;
 
 	class IJob extends InteractiveJobAdapter {
 		private InspectionTab tab;
 		private List<SliceProperty> slices;
 
 		public IJob(final InspectionTab tab, final List<SliceProperty >slices) {
 			this.tab = tab;
 			this.slices = slices;
 		}
 
 		@Override
 		public void run(IProgressMonitor monitor) {
 			if (!isNull()) {
 				tab.stopInspection();
 				tab.pushToView(new ProgressMonitorWrapper(monitor), slices);
 			}
 		}
 
 		@Override
 		public boolean isNull() {
 			return tab == null && slices == null;
 		}
 	}
 
 	private InteractiveQueue sliceQueue;
 
 	private Group axesSelGroup;
 
 	private Composite iComp;
 
 	/**
 	 * 
 	 * @param parent
 	 * @param style
 	 * @param site
 	 */
 	public DatasetInspector(Composite parent, int style, IWorkbenchPartSite site) {
 		super(parent, style);
 		setLayout(new FillLayout());
 
 		sliceQueue = new InteractiveQueue();
 		this.site = site;
 
 		display = parent.getDisplay();
 
 		// set up data and plotting selection
 //		ScrolledComposite sComp = new ScrolledComposite(this, SWT.VERTICAL);
 //		sComp.setLayout(new FillLayout());
 		final Composite comp = new Composite(this, SWT.NONE);
 		GridLayout layout = new GridLayout(2, false);
 		comp.setLayout(layout);
 
 		GridData gd;
 
 		axesSelGroup = new Group(comp, SWT.NONE);
 		axesSelGroup.setText("Data axes selection");
 		axesSelGroup.setToolTipText("Select which axis dataset to use in each dimension of the dataset array");
 		gd = new GridData(SWT.FILL, SWT.FILL, true, false);
 		axesSelGroup.setLayoutData(gd);
 		axesSelGroup.setLayout(new GridLayout(1, false));
 		{
 			// row 1
 			dsDetails = new Label(axesSelGroup, SWT.NONE);
 			dsDetails.setText("");
 			gd = new GridData(SWT.FILL, SWT.CENTER, true, true);
 			dsDetails.setLayoutData(gd);
 
 			// row 2
 			// initialize table of data axes
 			axisSelector = new AxisSelectionTable(axesSelGroup);
 			gd = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 5);
 			gd.heightHint = 150;
 			axisSelector.setLayoutData(gd);
 		}
 
 		// Create tab folder with tabs of plot configurers
 		plotTabFolder = new CTabFolder(comp, SWT.BORDER);
 		gd = new GridData();
 		gd.widthHint = 250;
 		gd.horizontalAlignment = SWT.FILL;
 		gd.verticalAlignment = SWT.FILL;
 		gd.minimumWidth = 200;
 		plotTabFolder.setLayoutData(gd);
 		plotTabFolder.setLayout(new FillLayout());
 		{
 			inspectionTabs = new LinkedHashMap<InspectorType, InspectionTab>();
 			inspectionTabs.put(InspectorType.LINE, new PlotTab(site, InspectorType.LINE,
 					"1D plot", new String[] { "x-axis" }));
 			inspectionTabs.put(InspectorType.LINESTACK, new PlotTab(site, InspectorType.LINESTACK,
 					"1D stack plot", new String[] { "x-axis", "z-axis" }));
 			inspectionTabs.put(InspectorType.IMAGE, new PlotTab(site, InspectorType.IMAGE,
 					"2D image", new String[] { "x-axis", "y-axis" }));
 			inspectionTabs.put(InspectorType.SURFACE, new PlotTab(site, InspectorType.SURFACE,
 					"2D surface", new String[] { "x-axis", "y-axis" }));
 			inspectionTabs.put(InspectorType.IMAGEXP, new PlotTab(site, InspectorType.IMAGEXP,
 					"2D image explorer", new String[] { "x-axis", "y-axis", "image" }));
 			inspectionTabs.put(InspectorType.MULTIIMAGES, new PlotTab(site, InspectorType.MULTIIMAGES,
 					"2D multiple images", new String[] { "x-axis", "y-axis", "images" }));
 			inspectionTabs.put(InspectorType.VOLUME, new PlotTab(site, InspectorType.VOLUME,
 					"3D volume",  new String[] { "x-axis", "y-axis", "z-axis" }));
 			inspectionTabs.put(InspectorType.POINTS1D, new ScatterTab(site, InspectorType.POINTS1D,
 					"1D scatter plot", new String[] { "x-coord" }));
 			inspectionTabs.put(InspectorType.POINTS2D, new ScatterTab(site, InspectorType.POINTS2D,
 					"2D scatter plot", new String[] { "x-coord", "y-coord" }));
 			inspectionTabs.put(InspectorType.POINTS3D, new ScatterTab(site, InspectorType.POINTS3D,
 					"3D scatter plot", new String[] { "x-coord", "y-coord", "z-coord" }));
 			inspectionTabs.put(InspectorType.DATA1D, new DataTab(site, InspectorType.DATA1D,
 					"1D data", new String[] { "row" }));
 			inspectionTabs.put(InspectorType.DATA2D, new DataTab(site, InspectorType.DATA2D,
 					"2D data", new String[] { "column", "row" }));
 
 			for (InspectorType t : InspectorType.values()) {
 				final InspectionTab tab = inspectionTabs.get(t);
 				if (tab == null)
 					continue;
 				CTabItem item = new CTabItem(plotTabFolder, SWT.NONE);
 				item.setText(tab.getTabTitle());
 			}
 		}
 
 		plotTabFolder.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				if (cData == null)
 					return;
 
 				if (cInspectionTab != null) {
 					cInspectionTab.stopInspection();
 				}
 				CTabItem item = plotTabFolder.getSelection();
 				int t = plotTabFolder.getSelectionIndex();
 				InspectorType type = InspectorType.getType(t);
 				InspectionTab tab = inspectionTabs.get(type);
 				if (!tab.checkCompatible(cData)) {
 					for (InspectionTab itab : inspectionTabs.values()) {
 						if (itab.checkCompatible(cData)) {
 							tab = itab;
 							break;
 						}
 					}
 					type = tab.getType();
 					t = type.getValue();
 					item = plotTabFolder.getItem(t);
 					plotTabFolder.setSelection(t);
 					tab = inspectionTabs.get(type);
 				}
 				cInspectionTab = tab;
 				inspection.initSlice(type);
 				inspection.initPlotAxes(tab);
 				if (item.getControl() == null) {
 					createPlotTab(item);
 				} else {
 					cInspectionTab.setParameters(cData, inspection.datasetAxes, inspection.allPlotAxes.get(type));
 					cInspectionTab.drawTab();
 				}
 				inspection.switchType(type);
 				updateSlicers(true);
 				sliceDataAndView();
 			}
 		});
 
 		// Create data slicer
 		Group slicersGroup = new Group(comp, SWT.NONE);
 		slicersGroup.setLayout(new FillLayout());
 		slicersGroup.setText("Dataset slicing");
 		slicersGroup.setToolTipText("Configure the slicing of the dataset array. Each dimension can be sliced. " +
 				"A slice is a selection of items specified by a starting position, the number of items selected " +
 				"and the size of the step over which items are skipped");
 		gd = new GridData(SWT.FILL, SWT.FILL, true, true);
 		gd.horizontalSpan = 2;
 		slicersGroup.setLayoutData(gd);
 		ScrolledComposite slComp = new ScrolledComposite(slicersGroup, SWT.HORIZONTAL | SWT.VERTICAL);
 		iComp = new Composite(slComp, SWT.NONE);
 		iComp.setLayout(new GridLayout(AxisSlicer.COLUMNS, false));
 		Label l;
 		l = new Label(iComp, SWT.NONE);
 		l.setText("Dim");
 		l.setToolTipText("Dimension of dataset array");
 		l = new Label(iComp, SWT.NONE);
 		l.setText("Start position");
 		l.setToolTipText("Position of start in array");
 		l = new Label(iComp, SWT.NONE);
 		l.setText("Start value");
 		l.setToolTipText("Value at starting position");
 		l = new Label(iComp, SWT.NONE);
 		l.setText("Items");
 		l.setToolTipText("Number of items in slice");
 		l = new Label(iComp, SWT.NONE);
 		l.setText("Step size");
 		l.setToolTipText("Number of items in each step");
 		new Label(iComp, SWT.NONE).setText("");
 		slComp.setContent(iComp);
 
 		layout();
 
 		slicerListener = new PropertyChangeListener() {
 			@Override
 			public void propertyChange(final PropertyChangeEvent evt) {
 				if (cData != null && display != null)
 					cInspectionTab.stopInspection();
 					display.asyncExec(new Runnable() {
 						@Override
 						public void run() {
 							if (evt.getPropertyName() == SliceProperty.sliceUpdate)
 								updateSlicers(false);
 							sliceDataAndView();
 						}
 					});
 			}
 		};
 
 		plotTabListener = new PropertyChangeListener() {
 			@Override
 			public void propertyChange(PropertyChangeEvent evt) {
 				cInspectionTab.stopInspection();
				axisSelector.refresh();
				updateSlicers(true);
 				sliceDataAndView();
 			}
 		};
 
 		datasetAxesListener = new PropertyChangeListener() {
 			@Override
 			public void propertyChange(PropertyChangeEvent evt) {
 				if (cData != null && display != null)
 					cInspectionTab.stopInspection();
 					display.asyncExec(new Runnable() {
 						@Override
 						public void run() {
 							updateSlicers(true);
 							sliceDataAndView();
 						}
 					});
 			}
 		};
 
 		selListener = new ISelectionListener() {
 			DatasetSelection oldSelection = null;
 			@Override
 			public void selectionChanged(IWorkbenchPart part, ISelection selection) {
 				if (selection instanceof DatasetSelection) {
 					DatasetSelection newSelection = processSelection(oldSelection, (DatasetSelection) selection);
 					if (newSelection != null)
 						oldSelection = newSelection;
 				}
 			}
 		};
 		site.getWorkbenchWindow().getSelectionService().addSelectionListener(selListener);
 
 		storedInspections = new HashMap<DatasetSelection, Inspection>();
 	}
 
 	private DatasetSelection processSelection(DatasetSelection oldSelection, DatasetSelection dSelection) {
 		if (dSelection.equals(oldSelection))
 			return null;
 
 		cData = dSelection.getFirstElement();
 		if (cData == null) {
 			try {
 				SDAPlotter.clearPlot(PLOTNAME);
 			} catch (Exception e) {}
 
 			oldSelection = dSelection;
 			return null;
 		}
 
 		final boolean useClonedInspection;
 		if (storedInspections.containsKey(dSelection)) {
 			inspection = storedInspections.get(dSelection);
 			useClonedInspection = false;
 		} else if (dSelection.almostEquals(oldSelection)) {
 			inspection = inspection.clone(oldSelection);
 			storedInspections.put(dSelection, inspection);
 			useClonedInspection = true;
 		} else {
 			inspection = null;
 			boolean found = false;
 			for (Entry<DatasetSelection, Inspection> e: storedInspections.entrySet()) {
 				DatasetSelection stored = e.getKey();
 				if (dSelection.almostEquals(stored)) {
 					inspection = e.getValue().clone(stored);
 					storedInspections.put(dSelection, inspection);
 					found = true;
 					break;
 				}
 			}
 			useClonedInspection = found;
 		}
 
 		if (inspection == null) {
 			inspection = new Inspection(dSelection);
 			storedInspections.put(dSelection, inspection);
 			int[] shape = cData.getShape();
 			int rank = shape.length;
 
 			int aNum = inspection.getNumAxes();
 			if (aNum < rank) { // add auto-axes if necessary
 				for (int i = aNum; i < rank; i++) {
 					AxisSelection aSel = new AxisSelection(i, shape[i]);
 					AbstractDataset axis = AbstractDataset.arange(shape[i], AbstractDataset.INT32);
 					axis.setName(AbstractExplorer.DIM_PREFIX + (i+1));
 					AxisChoice newChoice = new AxisChoice(axis);
 					newChoice.setAxisNumber(i);
 					aSel.addChoice(newChoice, aSel.getMaxOrder() + 1);
 					inspection.addDatasetAxis(aSel);
 				}
 			}
 
 			inspection.initSlice(inspection.itype);
 			inspection.setSliceListener(slicerListener);
 			inspection.setDatasetAxisListener(datasetAxesListener);
 		}
 
 		if (display != null) {
 			display.asyncExec(new Runnable() {
 				@Override
 				public void run() {
 					StringBuilder msg = new StringBuilder(String.format("Name: %s; Rank: %d; Dims: ",
 							cData.getName(), cData.getRank()));
 					msg.append(Arrays.toString(cData.getShape()));
 					dsDetails.setText(msg.toString());
 					axisSelector.setInput(inspection.datasetAxes);
 					createSlicers(iComp);
 					if (inspection.itype == InspectorType.EMPTY)
 						return;
 
 					if (!useClonedInspection) {
 						cInspectionTab = inspectionTabs.get(inspection.itype);
 						inspection.initPlotAxes(cInspectionTab);
 						inspection.setPlotAxisListener(plotTabListener);
 						int t = inspection.itype.getValue();
 						plotTabFolder.setSelection(t);
 						CTabItem item = plotTabFolder.getItem(t);
 						if (item.getControl() == null) {
 							createPlotTab(item);
 							updateSlicers(true);
 							sliceDataAndView();
 						} else {
 							cInspectionTab.setParameters(cData, inspection.datasetAxes, inspection.getPlotAxes());
 							cInspectionTab.drawTab(); // implicitly views data
 						}
 					} else {
 						cInspectionTab = inspectionTabs.get(inspection.itype);
 						cInspectionTab.setParameters(cData, inspection.datasetAxes, inspection.getPlotAxes());
 						cInspectionTab.drawTab();
 					}
 				}
 			});
 		}
 		return dSelection;
 	}
 
 	@Override
 	public void dispose() {
 		sliceQueue.dispose();
 		if (selListener != null)
 			site.getWorkbenchWindow().getSelectionService().removeSelectionListener(selListener);
 		selListener = null;
 		if (!isDisposed())
 			super.dispose();
 	}
 
 	private void createPlotTab(CTabItem item) {
 		if (cInspectionTab == null)
 			return;
 
 		Control c = cInspectionTab.createTabComposite(plotTabFolder);
 		if (inspection != null) {
 			cInspectionTab.setParameters(cData, inspection.datasetAxes, inspection.allPlotAxes.get(cInspectionTab.getType()));
 		}
 
 		item.setControl(c);
 		layout();
 	}
 
 	// create plot selection GUI
 	private void createSlicers(Composite parent) {
 		if (cData == null)
 			return;
 		if (slicers == null)
 			slicers = new ArrayList<AxisSlicer>();
 
 		int rank = cData.getRank();
 		if (rank > inspection.datasetAxes.size()) {
 			logger.error("Axis selection wrong!");
 		}
 
 		int size = slicers.size();
 		if (rank > size) {
 			for (int i = size; i < rank; i++) {
 				slicers.add(new AxisSlicer(parent));
 			}
 		} else {
 			for (int i = rank; i < size; i++) {
 				slicers.get(i).setVisible(false);
 			}
 		}
 
 		// create array of slice properties that pertain to each axis
 		List<SliceProperty> slices = inspection.getSlices();
 		for (int i = 0; i < rank; i++) {
 			SliceProperty p = slices.get(i);
 			AxisChoice c = inspection.datasetAxes.get(i).getSelectedAxis();
 			String n = inspection.datasetAxes.get(i).getSelectedName();
 			int[] imap = c.getIndexMapping();
 			ILazyDataset axis = c.getValues();
 			SliceProperty[] props = new SliceProperty[imap.length];
 			for (int j = 0; j < imap.length; j++) {
 				props[j] = slices.get(imap[j]);
 			}
 			
 			slicers.get(i).createAxisSlicer();
 			slicers.get(i).setParameters(n, p, axis, props, true);
 
 		}
 		parent.pack();
 		parent.setSize(parent.computeSize(SWT.DEFAULT, SWT.DEFAULT));
 	}
 
 	private void updateSlicers(boolean resetSlices) {
 		List<SliceProperty> properties = inspection.getSlices();
 		boolean[] used = cInspectionTab.getUsedDims();
 		int rank = properties.size();
 
 		for (int i = 0; i < rank; i++) {
 			SliceProperty p = properties.get(i);
 			if (resetSlices) {
 				// reset slices according to used dimensions
 				Slice s = p.getValue();
 				if (used[i]) {
 					s.setStop(null);
 				} else {
 					Integer b = s.getStart();
 					if (b == null)
 						s.setStop(1);
 					else
 						s.setStop(b + 1);
 				}
 			}
 
 			AxisSelection sel = inspection.datasetAxes.get(i);
 			AxisChoice choice = sel.getSelectedAxis();
 			String n = sel.getSelectedName();
 			ILazyDataset axis = choice.getValues();
 			int[] imap = choice.getIndexMapping();
 			SliceProperty[] props = new SliceProperty[imap.length];
 			for (int j = 0; j < imap.length; j++) {
 				props[j] = properties.get(imap[j]);
 			}
 			slicers.get(i).setParameters(n, p, axis, props, used[i]);
 		}
 		Composite parent = slicers.get(0).getParent();
 		parent.pack();
 		parent.setSize(parent.computeSize(SWT.DEFAULT, SWT.DEFAULT));
 	}
 
 	/**
 	 * View current slice
 	 */
 	public void sliceDataAndView() {
 		InspectorType type = cInspectionTab == null ? InspectorType.EMPTY : cInspectionTab.getType();
 		if (cData == null || type == InspectorType.EMPTY) {
 			try {
 				SDAPlotter.clearPlot(PLOTNAME);
 			} catch (Exception e) {
 				logger.error("Problem clearing plot");
 			}
 			return;
 		}
 
 		try {
 			final IJob obj = new IJob(cInspectionTab, inspection.getSlices());
 			sliceQueue.addJob(obj);
 		} catch (Exception e) {
 			logger.error("Cannot generate slices", e);
 		}
 	}
 }
