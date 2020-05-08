 /*-
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
 
 package uk.ac.diamond.scisoft.analysis.rcp.views;
 
 import gda.observable.IObserver;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.dawb.common.ui.plot.AbstractPlottingSystem;
 import org.dawb.common.ui.plot.AbstractPlottingSystem.ColorOption;
 import org.dawb.common.ui.plot.PlotType;
 import org.dawb.common.ui.plot.PlottingFactory;
 import org.dawb.common.ui.plot.region.IROIListener;
 import org.dawb.common.ui.plot.region.IRegion;
 import org.dawb.common.ui.plot.region.IRegion.RegionType;
 import org.dawb.common.ui.plot.region.IRegionListener;
 import org.dawb.common.ui.plot.region.ROIEvent;
 import org.dawb.common.ui.plot.region.RegionEvent;
 import org.dawb.common.ui.plot.tool.IToolPage.ToolPageRole;
 import org.dawb.common.ui.plot.tool.IToolPageSystem;
 import org.dawb.common.ui.plot.trace.IImageTrace;
 import org.dawb.common.ui.plot.trace.IPaletteListener;
 import org.dawb.common.ui.plot.trace.ITrace;
 import org.dawb.common.ui.plot.trace.PaletteEvent;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.IToolBarManager;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CCombo;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Spinner;
 import org.eclipse.ui.IViewSite;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.part.ViewPart;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 import uk.ac.diamond.scisoft.analysis.rcp.AnalysisRCPActivator;
 import uk.ac.diamond.scisoft.analysis.rcp.histogram.ColorMappingUpdate;
 import uk.ac.diamond.scisoft.analysis.rcp.histogram.HistogramUpdate;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.AxisValues;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.DataSet3DPlot3D;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.DataSetPlotter;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.PlotException;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.PlottingMode;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.enums.AxisMode;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.sideplot.DataWindowOverlay;
 import uk.ac.diamond.scisoft.analysis.rcp.preference.PreferenceConstants;
 import uk.ac.diamond.scisoft.analysis.roi.ROIBase;
 import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;
 
 /**
  *
  */
 public class DataWindowView extends ViewPart implements IObserver, SelectionListener, IROIListener {
 
 	private AxisValues xAxis;
 	private AxisValues yAxis;
 	private DataSetPlotter plotter;
 	private DataWindowOverlay overlay;
 	private String sId;
 	private String id;
 	private final static int MAXDISPLAYDIM = 1024;
 	private Label lblStartX;
 	private Label lblStartY;
 	private Label lblXSampling;
 	private Label lblYSampling;
 	private Label lblEndX;
 	private Label lblDelimiter;
 	private Button btnOverwriteAspect;
 	private Spinner spnStartX;
 	
 	private Spinner spnEndX;
 	private Spinner spnStartY;
 	private Spinner spnEndY;
 	private Spinner spnXAspect;
 	private Spinner spnYAspect;
 	private Action allowUSampling;
 	private CCombo ccXsampling;
 	private CCombo ccYsampling;
 	
 	private AbstractPlottingSystem plottingSystem;
 	private Logger logger = LoggerFactory.getLogger(DataWindowView.class);
 	private Composite plotComp;
 	
 	protected List<IObserver> observers = 
 			Collections.synchronizedList(new LinkedList<IObserver>());
 
 	private ROIBase currentROI;
 	/**
 	 * Default constructor of DataWindowView
 	 */
 	public DataWindowView() {
 		super();
 	}
 
 	/**
 	 * @return Returns the id.
 	 */
 	public String getId() {
 		return id;
 	}
 
 	@Override
 	public void init(IViewSite site) throws PartInitException {
 		super.init(site);
 		id = site.getId();
 		sId = site.getSecondaryId();
 		setPartName("Surface Profile: " + sId);
 	}
 	
 	private void buildActions(IToolBarManager manager) {
 		allowUSampling = new Action("",IAction.AS_CHECK_BOX) {
 			@Override
 			public void run()
 			{
 				overlay.setAllowUndersampling(allowUSampling.isChecked());				
 				ccXsampling.setEnabled(allowUSampling.isChecked());
 				ccYsampling.setEnabled(allowUSampling.isChecked());
 				if (ccXsampling.getSelectionIndex() == 0 && 
 					ccXsampling.isEnabled()) {
 					ccXsampling.select(1);
 					overlay.setSamplingMode(1, ccYsampling.getSelectionIndex());
 				}
 				if (ccYsampling.getSelectionIndex() == 0 && 
 						ccYsampling.isEnabled()) {
 						ccYsampling.select(1);
 						overlay.setSamplingMode(ccXsampling.getSelectionIndex(),1);
 				}
 			}
 		};
 		allowUSampling.setText("Allow undersampling");
 		allowUSampling.setDescription("Allow select area to be undersampled");
 		allowUSampling.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor("icons/arrow_in.png"));
 		manager.add(allowUSampling);
 	}
 	
 	@Override
 	public void createPartControl(Composite parent) {
 		buildActions(getViewSite().getActionBars().getToolBarManager());
 		Composite topComposite = new Composite(parent,SWT.NONE);
 		GridLayout gridLayout = new GridLayout();
 		gridLayout.numColumns = 1;
 		gridLayout.horizontalSpacing = 0;
 		gridLayout.verticalSpacing = 2;
 		topComposite.setLayout(gridLayout);
 		
 		if(getDefaultPlottingSystemChoice()==PreferenceConstants.PLOT_VIEW_DATASETPLOTTER_PLOTTING_SYSTEM){
 			plotter = new DataSetPlotter(PlottingMode.ONED,topComposite,false);
 			plotComp = plotter.getComposite();
 		}
 		else if(getDefaultPlottingSystemChoice()==PreferenceConstants.PLOT_VIEW_ABSTRACT_PLOTTING_SYSTEM){
 			createPlottingSystem(topComposite);
 			plotComp = plottingSystem.getPlotComposite();
 		}
 		
 		GridData gridData = new GridData();
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.grabExcessVerticalSpace = true;
 		gridData.horizontalAlignment = GridData.FILL;
 		gridData.verticalAlignment = GridData.FILL;
 		plotComp.setLayoutData(gridData);
 		Composite bottomComposite = new Composite(topComposite,SWT.NONE);
 		bottomComposite.setLayout(new FormLayout());
 		gridData = new GridData();
 		gridData.horizontalAlignment = GridData.FILL;
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.heightHint = 125;
 		bottomComposite.setLayoutData(gridData);
 		{
 			lblStartX = new Label(bottomComposite, SWT.NONE);
 			{
 				FormData formData = new FormData();
 				formData.height = 18;
 				formData.width = 69;
 				formData.top = new FormAttachment(0, 25);
 				formData.left = new FormAttachment(0, 10);
 				lblStartX.setLayoutData(formData);
 			}
 			lblStartX.setText("Start X:");
 		}
 		{
 			spnStartX = new Spinner(bottomComposite, SWT.BORDER);
 			{
 				FormData formData = new FormData();
 				formData.width = 62;
 				formData.top = new FormAttachment(lblStartX, 0, SWT.TOP);
 				formData.left = new FormAttachment(lblStartX, 6);
 				spnStartX.setLayoutData(formData);
 				spnStartX.setMinimum(0);
 				spnStartX.addSelectionListener(this);
 			}
 		}
 		{
 			lblStartY = new Label(bottomComposite, SWT.NONE);
 			{
 				FormData formData = new FormData();
 				formData.top = new FormAttachment(lblStartX, 15);
 				formData.left = new FormAttachment(lblStartX, 0, SWT.LEFT);
 				lblStartY.setLayoutData(formData);
 			}
 			lblStartY.setText("Start Y:");
 		}
 		{
 			spnStartY = new Spinner(bottomComposite, SWT.BORDER);
 			{
 				FormData formData = new FormData();
 				formData.left = new FormAttachment(spnStartX, 0, SWT.LEFT);
 				formData.right = new FormAttachment(spnStartX, 0, SWT.RIGHT);
 				formData.top = new FormAttachment(spnStartX, 6);
 				spnStartY.setLayoutData(formData);
 				spnStartY.setMinimum(0);
 				spnStartY.addSelectionListener(this);
 			}
 		}
 		{
 			lblEndX = new Label(bottomComposite, SWT.NONE);
 			{
 				FormData formData = new FormData();
 				formData.top = new FormAttachment(lblStartX, 0, SWT.TOP);
 				formData.left = new FormAttachment(spnStartX, 16);
 				lblEndX.setLayoutData(formData);
 			}
 			lblEndX.setText("Width:");
 		}
 		{
 			spnEndX = new Spinner(bottomComposite, SWT.BORDER);
 			{
 				FormData formData = new FormData();
 				formData.top = new FormAttachment(lblStartX, 0, SWT.TOP);
 				formData.left = new FormAttachment(lblEndX, 16);
 				formData.width = 69;
 				spnEndX.setLayoutData(formData);
 				spnEndX.setMinimum(0);
 				spnEndX.addSelectionListener(this);
 			}
 		}
 		{
 			Label lblEndY = new Label(bottomComposite, SWT.NONE);
 			{
 				FormData formData = new FormData();
 				formData.top = new FormAttachment(lblStartY, 0, SWT.TOP);
 				formData.right = new FormAttachment(lblEndX, 0, SWT.RIGHT);
 				lblEndY.setLayoutData(formData);
 			}
 			lblEndY.setText("Height:");
 		}
 		{
 			spnEndY = new Spinner(bottomComposite, SWT.BORDER);
 			{
 				FormData formData = new FormData();
 				formData.right = new FormAttachment(spnEndX, 0, SWT.RIGHT);
 				formData.top = new FormAttachment(spnStartY, 0, SWT.TOP);
 				formData.left = new FormAttachment(spnEndX, 0, SWT.LEFT);
 				spnEndY.setLayoutData(formData);
 				spnEndY.setMinimum(0);
 				spnEndY.addSelectionListener(this);
 			}
 		}
 		{
 			lblXSampling = new Label(bottomComposite,SWT.NONE);
 			FormData formData = new FormData();
 			formData.top = new FormAttachment(lblStartX, 0, SWT.TOP);
 			formData.left = new FormAttachment(spnEndX, 16);
 			formData.width = 79;
 			lblXSampling.setLayoutData(formData);
 			lblXSampling.setText("Sampling:");
 		}
 		{
 			lblYSampling = new Label(bottomComposite,SWT.NONE);
 			FormData formData = new FormData();
 			formData.top = new FormAttachment(lblStartY, 0, SWT.TOP);
 			formData.left = new FormAttachment(spnEndY, 16);
 			formData.width = 79;
 			lblYSampling.setLayoutData(formData);
 			lblYSampling.setText("Sampling:");
 		}		
 		{
 			ccXsampling = new CCombo(bottomComposite, SWT.NONE);
 			{
 				FormData formData = new FormData();
 				formData.top = new FormAttachment(lblStartX, 0, SWT.TOP);
 				formData.left = new FormAttachment(lblXSampling, 16);
 				formData.width = 120;
 				ccXsampling.setLayoutData(formData);
 			}
 			ccXsampling.add("None");
 			ccXsampling.add("Point");
 			ccXsampling.add("Median");
 			ccXsampling.add("Minimum");
 			ccXsampling.add("Maximum");
 		}
 		{
 			ccYsampling = new CCombo(bottomComposite, SWT.NONE);
 			{
 				FormData formData = new FormData();
 				formData.top = new FormAttachment(lblStartY, 0, SWT.TOP);
 				formData.left = new FormAttachment(lblYSampling, 16);
 				formData.width = 120;
 				ccYsampling.setLayoutData(formData);
 			}
 			ccYsampling.add("None");
 			ccYsampling.add("Point");
 			ccYsampling.add("Median");
 			ccYsampling.add("Minimum");
 			ccYsampling.add("Maximum");
 		}	
 		{
 			btnOverwriteAspect = new Button(bottomComposite,SWT.CHECK);
 			{
 				FormData formData = new FormData();
 				formData.top = new FormAttachment(lblYSampling,40,SWT.TOP);
 				formData.left = new FormAttachment(0, 20);
 				formData.width = 175;
 				btnOverwriteAspect.setLayoutData(formData);
 			}
 			btnOverwriteAspect.setText("Overwrite Aspect-Ratio");
 			btnOverwriteAspect.addSelectionListener(this);
 		}
 		{
 			spnXAspect = new Spinner(bottomComposite,SWT.NONE);
 			{
 				FormData formData = new FormData();
 				formData.top = new FormAttachment(lblYSampling,40,SWT.TOP);
 				formData.left = new FormAttachment(btnOverwriteAspect,16);
 				spnXAspect.setLayoutData(formData);
 			}
 			spnXAspect.setEnabled(false);
 			spnXAspect.setMinimum(1);
 			spnXAspect.setMaximum(10);
 			spnXAspect.setSelection(1);
 			spnXAspect.setIncrement(1);
 			spnXAspect.addSelectionListener(this);
 		}
 		{
 			lblDelimiter = new Label(bottomComposite,SWT.NONE);
 			{
 				FormData formData = new FormData();
 				formData.top = new FormAttachment(lblYSampling,40,SWT.TOP);
 				formData.left = new FormAttachment(spnXAspect,5);
 				lblDelimiter.setLayoutData(formData);
 			}
 			lblDelimiter.setText(":");
 		}
 		{
 			spnYAspect = new Spinner(bottomComposite,SWT.NONE);
 			{
 				FormData formData = new FormData();
 				formData.top = new FormAttachment(lblYSampling,40,SWT.TOP);
 				formData.left = new FormAttachment(lblDelimiter,5);
 				spnYAspect.setLayoutData(formData);
 			}
 			spnYAspect.setEnabled(false);
 			spnYAspect.setMinimum(1);
 			spnYAspect.setMaximum(10);
 			spnYAspect.setSelection(1);
 			spnYAspect.setIncrement(1);
 			spnYAspect.addSelectionListener(this);
 		}
 		ccXsampling.select(0);
 		ccYsampling.select(0);
 		ccXsampling.setEnabled(false);
 		ccYsampling.setEnabled(false);
 		ccXsampling.addSelectionListener(this);
 		ccYsampling.addSelectionListener(this);
 		if(getDefaultPlottingSystemChoice()==PreferenceConstants.PLOT_VIEW_DATASETPLOTTER_PLOTTING_SYSTEM){
 			plotter.setMode(PlottingMode.TWOD);
 			plotter.setAxisModes(AxisMode.CUSTOM, AxisMode.CUSTOM, AxisMode.LINEAR);
 			overlay = new DataWindowOverlay(1,1,this);
 			plotter.registerOverlay(overlay);
 		}else if (getDefaultPlottingSystemChoice()==PreferenceConstants.PLOT_VIEW_ABSTRACT_PLOTTING_SYSTEM){
 			overlay = new DataWindowOverlay(1,1,this);
 		}
 
 		xAxis = new AxisValues();
 		yAxis = new AxisValues();
 		
 		if(getDefaultPlottingSystemChoice()==PreferenceConstants.PLOT_VIEW_ABSTRACT_PLOTTING_SYSTEM){
 			this.registeredTraces = new HashMap<String,Collection<ITrace>>(7);
 		}
 	}
 
 	private void createPlottingSystem(Composite parent){
 		try {
 			plottingSystem = PlottingFactory.createPlottingSystem();
 			plottingSystem.setColorOption(ColorOption.NONE);
 			plottingSystem.setDatasetChoosingRequired(false);
 			plottingSystem.createPlotPart(parent, this.getPartName(), getViewSite().getActionBars(), PlotType.PT1D, this);
 			//plottingSystem.getPlotActionSystem().fillRegionActions(getViewSite().getActionBars().getToolBarManager());
 
 			plottingSystem.repaint();
 
 			this.regionListener = getRegionListener();
 			this.plottingSystem.addRegionListener(this.regionListener);
 			this.paletteListener = getPaletteListener();
 			
 		} catch (Exception e) {
 			logger.error("Cannot locate any Abstract plotting System!", e);
 		}
 	}
 
 	@Override
 	public void setFocus() {
 		// Nothing to do
 
 	}
 
 	/**
 	 * @param newData
 	 * @param inXAxis optional xAxis values
 	 * @param inYAxis optional yAxis values
 	 */
 	public void setData(IDataset newData, AxisValues inXAxis, AxisValues inYAxis) {
 		if (newData != null) {
 			xAxis.clear();
 			yAxis.clear();
 			final int xSize = newData.getShape()[1];
 			final int ySize = newData.getShape()[0];
 			int xSamplingRate = Math.max(1, xSize / MAXDISPLAYDIM);
 			int ySamplingRate = Math.max(1, ySize / MAXDISPLAYDIM);
 			int[] startPos = new int[]{0,0};
 			int[] endPos = new int[]{newData.getShape()[0],newData.getShape()[1]};
 			int[] step = new int[]{ySamplingRate,xSamplingRate};
 			final IDataset displayData = newData.getSlice(startPos, endPos, step);
 			if (inXAxis == null || inXAxis.size() == 0)
 			{
 				xAxis.setValues(AbstractDataset.arange(0, xSize, xSamplingRate, AbstractDataset.INT32));
 			} else {
 				xAxis.setValues(inXAxis.subset(0, xSize, xSamplingRate).toDataset());
 			}
 			if (inYAxis == null || inYAxis.size() == 0)
 			{
 				yAxis.setValues(AbstractDataset.arange(0, ySize, ySamplingRate, AbstractDataset.INT32));
 			} else {
 				yAxis.setValues(inYAxis.subset(0, ySize, ySamplingRate).toDataset());
 			}
 			if(getDefaultPlottingSystemChoice()==PreferenceConstants.PLOT_VIEW_DATASETPLOTTER_PLOTTING_SYSTEM){
 				overlay.removePrimitives();
 				overlay.setScaling(xSamplingRate,ySamplingRate);
 				plotter.setXAxisValues(xAxis, 1);
 				plotter.setYAxisValues(yAxis);
 				try {
 					plotter.replaceCurrentPlot(displayData);
 				} catch (PlotException ex) {}
 				plotter.getComposite().getDisplay().asyncExec(new Runnable() {
 					@Override
 					public void run() {
 						spnEndX.setMaximum(xSize);
 						spnEndY.setMaximum(ySize);
 						spnStartX.setMaximum(xSize);
 						spnStartY.setMaximum(ySize);
 						if (xSize * ySize > DataSet3DPlot3D.MAXDIM * DataSet3DPlot3D.MAXDIM)
 						{
 							float reduceFactor = (float)(DataSet3DPlot3D.MAXDIM * DataSet3DPlot3D.MAXDIM) / 
 												 (float)(xSize * ySize);
 							float xAspect = (float)xSize / (float)(xSize+ySize);
 							float yAspect = (float)ySize / (float)(xSize+ySize);
 							float xReduce = 1.0f - (1.0f - reduceFactor) * xAspect;
 							float yReduce = 1.0f - (1.0f - reduceFactor) * yAspect;
 							int currentXdim = (int)(xSize * xReduce * 0.75f);
 							int currentYdim = (int)(ySize * yReduce * 0.75f);					
 							spnEndX.setSelection(currentXdim);
 							spnEndY.setSelection(currentYdim);
 							overlay.setSelectPosition(0,0,currentXdim,currentYdim);
 						} else {
 							spnEndX.setSelection(xSize);
 							spnEndY.setSelection(ySize);						
 						}
 						plotter.refresh(false);
 						spnStartX.setSelection(0);
 						spnStartY.setSelection(0);
 					}
 				});
 			}else if(getDefaultPlottingSystemChoice()==PreferenceConstants.PLOT_VIEW_ABSTRACT_PLOTTING_SYSTEM){
 				if(newData instanceof AbstractDataset){
 					createPlot((AbstractDataset)newData);
 				}else
 					logger.error("Cannot display 2D Data");
 				Display.getDefault().asyncExec(new Runnable() {
 					@Override
 					public void run() {
 						spnEndX.setMaximum(xSize);
 						spnEndY.setMaximum(ySize);
 						spnStartX.setMaximum(xSize);
 						spnStartY.setMaximum(ySize);
 						if (xSize * ySize > DataSet3DPlot3D.MAXDIM * DataSet3DPlot3D.MAXDIM)
 						{
 							float reduceFactor = (float)(DataSet3DPlot3D.MAXDIM * DataSet3DPlot3D.MAXDIM) / 
 												 (float)(xSize * ySize);
 							float xAspect = (float)xSize / (float)(xSize+ySize);
 							float yAspect = (float)ySize / (float)(xSize+ySize);
 							float xReduce = 1.0f - (1.0f - reduceFactor) * xAspect;
 							float yReduce = 1.0f - (1.0f - reduceFactor) * yAspect;
 							int currentXdim = (int)(xSize * xReduce * 0.75f);
 							int currentYdim = (int)(ySize * yReduce * 0.75f);					
 							spnEndX.setSelection(currentXdim);
 							spnEndY.setSelection(currentYdim);
 							overlay.setSelectPosition(0,0,currentXdim,currentYdim);							
 						} else {
 							spnEndX.setSelection(xSize);
 							spnEndY.setSelection(ySize);						
 						}
 						plottingSystem.repaint();
 						spnStartX.setSelection(0);
 						spnStartY.setSelection(0);
 					}
 				});
 			}
 			
 		}
 	}
 
 	private void createPlot(final AbstractDataset data) {
 		//clean the observer
 		if(overlay!=null){
 			overlay.cleanIObservers();
 		}
 		final Job job = new Job("Create image plot") {
 			@Override
 			protected IStatus run(final IProgressMonitor monitor) {
 				Display.getDefault().asyncExec(new Runnable() {
 					@Override
 					public void run() {
 						plottingSystem.updatePlot2D(data, null, monitor);
 						plottingSystem.setTitle("");
 						try {
 							//create a region
 							IRegion region = plottingSystem.getRegion("Surface slice");
 							double upperX =plottingSystem.getSelectedXAxis().getUpper();
 							double lowerY =plottingSystem.getSelectedYAxis().getLower();
 							if(upperX>1000 && lowerY>1000){
 								upperX=500;
 								lowerY=500;
 							}
 							RectangularROI rroi = new RectangularROI(0, 0, upperX, lowerY, 0);
 							//Test if the region is already there and update the currentRegion
 							if(region!=null&&region.isVisible()){
 								currentROI = region.getROI();
 								if(currentROI.getPointX()<upperX && currentROI.getPointY()<lowerY)
 									region.setROI(currentROI);
 								else
 									region.setROI(rroi);
 							}else {
 								IRegion newRegion = plottingSystem.createRegion("Surface slice", RegionType.BOX);
 								newRegion.setROI(rroi);
 								plottingSystem.addRegion(newRegion);
 								plottingSystem.setToolVisible("org.dawnsci.rcp.histogram.histogram_tool_page",
 										ToolPageRole.ROLE_2D, "org.dawb.workbench.plotting.views.toolPageView.2D");
 							}
 						} catch (Exception e) {
 							logger.error("Couldn't open histogram view and create ROI", e);
 						}
 						// finally tie in the listener to the paletedata changes
 						Collection<ITrace> traces = plottingSystem.getTraces(IImageTrace.class);
 						image = traces!=null && traces.size()>0 ? (IImageTrace)traces.iterator().next():null;
 						if(image!=null){
 							image.addPaletteListener(paletteListener);
 						}
 					}
 				});
 				
 				return Status.OK_STATUS;
 			}
 		};
 		job.setUser(false);
 		job.setPriority(Job.BUILD);
 		job.schedule();
 	}
 
 	/**
 	 * Set the spinner values
 	 * @param startX start position in x dimension
 	 * @param startY start position in y dimension
 	 * @param width
 	 * @param height
 	 */
 	public void setSpinnerValues(final int startX, 
 								 final int startY, 
 								 final int width, 
 								 final int height) {
 		if(getDefaultPlottingSystemChoice()==PreferenceConstants.PLOT_VIEW_DATASETPLOTTER_PLOTTING_SYSTEM){
 			plotter.getComposite().getDisplay().asyncExec(new Runnable() {
 				@Override
 				public void run() {
 					spnStartX.setSelection(startX);
 					spnStartY.setSelection(startY);
 					spnEndX.setSelection(width);
 					spnEndY.setSelection(height);
 				}
 			});
 		}else if (getDefaultPlottingSystemChoice()==PreferenceConstants.PLOT_VIEW_ABSTRACT_PLOTTING_SYSTEM){
 			Display.getDefault().asyncExec(new Runnable() {
 				@Override
 				public void run() {
 					spnStartX.setSelection(startX);
 					spnStartY.setSelection(startY);
 					spnEndX.setSelection(width);
 					spnEndY.setSelection(height);
 				}
 			});
 		}
 			
 	}
 	
 	@Override
 	public void update(Object theObserved, Object changeCode) {
 		if(getDefaultPlottingSystemChoice()==PreferenceConstants.PLOT_VIEW_DATASETPLOTTER_PLOTTING_SYSTEM){
 			HistogramUpdate update = (HistogramUpdate) changeCode;
 			plotter.applyColourCast(update.getRedMapFunction(), update
 					.getGreenMapFunction(),
 					update.getBlueMapFunction(), update
 							.getAlphaMapFunction(),
 					update.inverseRed(), update.inverseGreen(), update
 							.inverseBlue(), update.inverseAlpha(),
 					update.getMinValue(), update.getMaxValue());
 	
 			plotter.refresh(false);		
 		}else if (getDefaultPlottingSystemChoice()==PreferenceConstants.PLOT_VIEW_ABSTRACT_PLOTTING_SYSTEM){
 			logger.debug("Update in DataWindowView");
 		}
 		
 	}
 
     @SuppressWarnings("rawtypes")
 	@Override
 	public Object getAdapter(final Class clazz) {
 		if (clazz == IToolPageSystem.class) {
 			return plottingSystem;
 		}
 		return super.getAdapter(clazz);
 	}
 
 	/**
 	 * Add an Observer 
 	 * @param anIObserver the new observer
 	 */
 	public void addIObserver(IObserver anIObserver) {
 		if (overlay != null)
 			overlay.addIObserver(anIObserver);
 	}
 
 
 	/**
 	 * Delete an observer
 	 * @param anIObserver the observer that should be removed
 	 */
 	public void deleteIObserver(IObserver anIObserver) {
 		if (overlay != null)
 			overlay.deleteIObserver(anIObserver);
 	}
 
 
 	/**
 	 * Delete all observers
 	 */
 	public void deleteIObservers() {
 		if (overlay != null)
 			overlay.deleteIObservers();
 	}
 
 	@Override
 	public void widgetDefaultSelected(SelectionEvent e) {
 		// Nothing to do
 		
 	}
 
 	@Override
 	public void widgetSelected(SelectionEvent e) {
 		if (overlay != null) {
 			if (!e.getSource().equals(ccXsampling) &&
 				!e.getSource().equals(ccYsampling) &&
 				!e.getSource().equals(btnOverwriteAspect))  {
 				int startPosX = spnStartX.getSelection();
 				int startPosY = spnStartY.getSelection();
 				int width = spnEndX.getSelection();
 				int height = spnEndY.getSelection();
 				if (startPosX + width > spnEndX.getMaximum()) {
 					width = spnEndX.getMaximum() - startPosX;
 				}
 				if (startPosY + height > spnEndY.getMaximum()) {
 					height = spnEndY.getMaximum() - startPosY;
 				}
 				overlay.setAspects(btnOverwriteAspect.getSelection() ? spnXAspect.getSelection() : 0, 
 				           		   btnOverwriteAspect.getSelection() ? spnYAspect.getSelection() : 0);
 
 				overlay.setSelectPosition(startPosX, 
 										  startPosY,
 										  width,
 										  height);
 				if (getDefaultPlottingSystemChoice()==PreferenceConstants.PLOT_VIEW_ABSTRACT_PLOTTING_SYSTEM){
 					IRegion region = plottingSystem.getRegion("Surface slice");
 					RectangularROI roi = new RectangularROI(startPosX, startPosY, width, height, 0);
 					if(region!=null)
 						region.setROI(roi);
 				}
 			}
 			else if (e.getSource().equals(ccXsampling) ||
 					 e.getSource().equals(ccYsampling)) {
 				overlay.setSamplingMode(ccXsampling.getSelectionIndex(),
 										ccYsampling.getSelectionIndex());
 			} else if (e.getSource().equals(btnOverwriteAspect)) {
 				spnXAspect.setEnabled(btnOverwriteAspect.getSelection());
 				spnYAspect.setEnabled(btnOverwriteAspect.getSelection());
 				if (btnOverwriteAspect.getSelection()) 
 					overlay.setAspects(spnXAspect.getSelection(),
 									   spnYAspect.getSelection());		
 				else
 					overlay.setAspects(0,0);							
 			}
 		}
 	}
 	
 	@Override
 	public void dispose()
 	{
 		if (!spnStartX.isDisposed())
 			spnStartX.removeSelectionListener(this);
 		if (!spnStartY.isDisposed())
 			spnStartY.removeSelectionListener(this);
 		if (!spnEndX.isDisposed())
 			spnEndX.removeSelectionListener(this);
 		if (!spnEndY.isDisposed())
 			spnEndY.removeSelectionListener(this);
 		if (!ccXsampling.isDisposed())
 			ccXsampling.removeSelectionListener(this);
 		if (!ccYsampling.isDisposed())
 			ccYsampling.removeSelectionListener(this);
 		
 		deleteIObservers();
 		if(getDefaultPlottingSystemChoice()==PreferenceConstants.PLOT_VIEW_DATASETPLOTTER_PLOTTING_SYSTEM){
 			if (plotter != null)
 				plotter.cleanUp();
 		}else if (getDefaultPlottingSystemChoice()==PreferenceConstants.PLOT_VIEW_ABSTRACT_PLOTTING_SYSTEM){
 			if(plottingSystem !=null){
 				if (image != null)
 					image.removePaletteListener(paletteListener);
 				clearTraces(plottingSystem.getRegion("Surface slice"));
 				plottingSystem.removeRegionListener(regionListener);
 				overlay.deleteIObservers();
 				plottingSystem.dispose();
 			}
 		}
 		
 	}
 
 	//Make the DataWindowView a PaletteListener (new Plotting)
 	private IPaletteListener paletteListener;
 	private IImageTrace image;
 
 	private IPaletteListener getPaletteListener(){
 		return new IPaletteListener(){
 			@Override
 			public void paletteChanged(PaletteEvent event) {
 				logger.debug("paletteChanged");
 				overlay.updateColorMapping(getColorMappingUpdate(event.getTrace()));
 			}
 			@Override
 			public void minChanged(PaletteEvent event) {
 				logger.debug("paletteListener minChanged");
 				overlay.updateColorMapping(getColorMappingUpdate(event.getTrace()));
 			}
 			@Override
 			public void maxChanged(PaletteEvent event) {
 				logger.debug("paletteListener maxChanged");
 				overlay.updateColorMapping(getColorMappingUpdate(event.getTrace()));
 			}
 			@Override
 			public void maxCutChanged(PaletteEvent event) {
 				logger.debug("paletteListener maxCutChanged");
 				overlay.updateColorMapping(getColorMappingUpdate(event.getTrace()));
 			}
 			@Override
 			public void minCutChanged(PaletteEvent event) {
 				logger.debug("paletteListener minCutChanged");
 				overlay.updateColorMapping(getColorMappingUpdate(event.getTrace()));
 			}
 			@Override
 			public void nanBoundsChanged(PaletteEvent event) {
 				overlay.updateColorMapping(getColorMappingUpdate(event.getTrace()));
 				return;
 			}
 			@Override
 			public void maskChanged(PaletteEvent evt) {
 				// No action needed.
 			}
 		};
 	}
 	
 	private ColorMappingUpdate getColorMappingUpdate(IImageTrace trace){
 		double minValue = trace.getMin().doubleValue();
 		double maxValue = trace.getMax().doubleValue();
 		return new ColorMappingUpdate(trace.getPaletteData(), minValue, maxValue);
 	}
 
 	// Make the DataWindowView a RegionListener (new plotting)
 	private IRegionListener regionListener;
 	private Map<String,Collection<ITrace>> registeredTraces;
 	private int roiWidth=0;
 	private int roiHeight=0;
 
 	private IRegionListener getRegionListener(){
 		return new IRegionListener.Stub() {
 			@Override
 			public void regionRemoved(RegionEvent evt) {
 				if (evt.getRegion()!=null) {
 					IRegion roi = evt.getRegion();
 					if(roi!=null && roi.getName().equals("Surface slice")){
 						clearTraces(roi);
 					}
 				}
 			}
 			@Override
 			public void regionAdded(RegionEvent evt) {
 				if (evt.getRegion()!=null) {
 					ROIBase roi = evt.getRegion().getROI();
 					if(roi!=null && roi instanceof RectangularROI){
 						RectangularROI rroi = (RectangularROI) roi;
 						final int startX = (int)Math.round(rroi.getPointX());
 						final int startY = (int)Math.round(rroi.getPointY());
 						roiWidth = (int)Math.round(rroi.getEndPoint()[0])-startX;
 						roiHeight = (int)Math.round(rroi.getEndPoint()[1])-startY;
 						Display.getDefault().asyncExec(new Runnable() {
 							@Override
 							public void run() {
 								overlay.setSelectPosition(startX,startY,roiWidth,roiHeight);	
 							}
 						});
 					}
 				}
 			}
 			@Override
 			public void regionCreated(RegionEvent evt) {
 				IRegion region = evt.getRegion();
 				if (region!=null) {
 					region.addROIListener(DataWindowView.this);
 					ROIBase roi = region.getROI();
 					if(roi!=null && roi instanceof RectangularROI){
 						RectangularROI rroi = (RectangularROI) roi;
 						final int startX = (int)Math.round(rroi.getPointX());
 						final int startY = (int)Math.round(rroi.getPointY());
 						roiWidth = (int)Math.round(rroi.getEndPoint()[0])-startX;
 						roiHeight = (int)Math.round(rroi.getEndPoint()[1])-startY;
 						Display.getDefault().asyncExec(new Runnable() {
 							@Override
 							public void run() {
 								overlay.setSelectPosition(startX,startY,roiWidth,roiHeight);	
 							}
 						});
 					}
 				}
 			}
 		};
 	}
 
 	@Override
 	public void roiDragged(ROIEvent evt) {
 		
 		ROIBase roi = evt.getROI();
 		if(roi!=null){
 			RectangularROI rroi = (RectangularROI) roi;
 			final int startX = (int)Math.round(rroi.getPointX());
 			final int startY = (int)Math.round(rroi.getPointY());
 			if(evt.getDragType()==ROIEvent.DRAG_TYPE.RESIZE){
 				roiWidth = (int)Math.round(rroi.getEndPoint()[0])-startX;
 				roiHeight = (int)Math.round(rroi.getEndPoint()[1])-startY;
 			}
 			Display.getDefault().asyncExec(new Runnable() {
 				@Override
 				public void run() {
 					overlay.setSelectPosition(startX,startY,roiWidth,roiHeight);	
 				}
 			});
 			
 		}
 	}
 
 	@Override
 	public void roiChanged(ROIEvent evt) {
 		ROIBase roi = evt.getROI();
 		if(roi!=null){
 			RectangularROI rroi = (RectangularROI) roi;
 			final int startX = (int)Math.round(rroi.getPointX());
 			final int startY = (int)Math.round(rroi.getPointY());
 			roiWidth = (int)Math.round(rroi.getEndPoint()[0])-startX;
 			roiHeight = (int)Math.round(rroi.getEndPoint()[1])-startY;
 			Display.getDefault().asyncExec(new Runnable() {
 				@Override
 				public void run() {
 					overlay.setSelectPosition(startX,startY,roiWidth,roiHeight);	
 				}
 			});
 		}
 	}
 
 	protected void clearTraces(final IRegion region) {
		final String name = region.getName();
		Collection<ITrace> registered = this.registeredTraces.get(name);
		if (registered!=null) for (ITrace iTrace : registered) {
			plottingSystem.removeTrace(iTrace);
 		}
 	}
 
 	private int getDefaultPlottingSystemChoice() {
 		IPreferenceStore preferenceStore = AnalysisRCPActivator.getDefault().getPreferenceStore();
 		return preferenceStore.isDefault(PreferenceConstants.PLOT_VIEW_PLOTTING_SYSTEM) ? 
 				preferenceStore.getDefaultInt(PreferenceConstants.PLOT_VIEW_PLOTTING_SYSTEM)
 				: preferenceStore.getInt(PreferenceConstants.PLOT_VIEW_PLOTTING_SYSTEM);
 	}
 }
  
