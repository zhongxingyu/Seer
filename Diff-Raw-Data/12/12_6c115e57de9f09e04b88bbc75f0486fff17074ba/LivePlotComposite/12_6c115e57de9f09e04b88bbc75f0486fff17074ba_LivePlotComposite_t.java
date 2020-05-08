 /*-
  * Copyright © 2011 Diamond Light Source Ltd.
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
 
 package uk.ac.gda.client.liveplot;
 
 import gda.gui.scanplot.ScanDataPointPlotter;
 import gda.plots.Marker;
 import gda.plots.ScanLine;
 import gda.plots.ScanPair;
 import gda.plots.Type;
 import gda.plots.UpdatePlotQueue;
 import gda.plots.XYDataHandler;
 import gda.scan.IScanDataPoint;
 import gda.util.FileUtil;
 
 import java.awt.Color;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 
 import javax.swing.tree.TreePath;
 
 import org.dawb.common.ui.plot.AbstractPlottingSystem;
 import org.dawb.common.ui.plot.PlotType;
 import org.dawb.common.ui.plot.PlottingFactory;
 import org.dawb.common.ui.plot.trace.ILineTrace;
 import org.dawb.common.ui.plot.trace.ILineTrace.PointStyle;
import org.dawb.common.ui.plot.trace.ITrace;
 import org.dawb.workbench.plotting.util.ColorUtility;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.IMenuListener;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.SashForm;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.IMemento;
 import org.eclipse.ui.IViewPart;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.IWorkbenchPartSite;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.actions.ActionContext;
 import org.eclipse.ui.actions.ActionFactory;
 import org.eclipse.ui.actions.ActionGroup;
 import org.jfree.data.Range;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.util.StringUtils;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.AxisValues;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.Plot1DAppearance;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.Plot1DGraphTable;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.PlotAppearanceDialog;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.enums.Plot1DStyles;
 import uk.ac.gda.client.LineAppearanceProvider;
 import uk.ac.gda.common.rcp.util.GridUtils;
 /**
  * Composite for displaying XY data from ScanDataPoints.
  * 
  * Currently a copy of XYPlotView which uses the new plotting.
  * 
  * TODO FIXME DO NOT COPY THIS CLASS. THIS IS A TEMPORARY MEASURE TO ALLOW
  * NEW PLOTTING TO WORK WITH VIEW SIMILAR TO XYPLOTVIEW. BECAUSE OF THE LIMITATIONS
  * OF THE OLD PLOTTING, THIS CLASS HAS DEPARTED FROM A GOOD DESIGN RELATIVE TO THE 
  * NEW PLOTTING. PLANNED IS A SIMPLER ABSTRACT CLASS OR TOOL TO MONITOR SCANS
  */
 public class LivePlotComposite extends Composite {
 
 	/**
 	 * Strings used to reference values stored in memento
 	 */
 	public static final String MEMENTO_XY_DATA = "XYData";
 	public static final String MEMENTO_XYDATA_XAXISHEADER = "xydata_xAxisHeader";
 	public static final String MEMENTO_XTDATA_YAXISHEADER = "xydata_yAxisHeader";
 	public static final String MEMENTO_XYDATA_VISIBLE = "xydata_visible";
 	public static final String MEMENTO_XYDATA_NAME = "xydata_name";
 	static public final String MEMENTO_ARCHIVEFILENAME = "xydata_archivefilename";
 	static public final String MEMENTO_XYDATA_DATAFILENAME = "xydata_datafilename";
 	static public final String MEMENTO_XYDATA_SCANNUMBER = "xydata_scannumber";
 
 	private static final Logger logger = LoggerFactory.getLogger(LivePlotComposite.class);
 
 	private static final int[] WEIGHTS_NORMAL = new int[] { 80, 20 };
 	private static final int[] WEIGHTS_NO_LEGEND = new int[] { 100, 0 };
 	SubLivePlotView plotView;
 	LiveLegend legend;
 	ScanDataPointPlotter plotter = null;
 	private SashForm sashForm;
 
 	static LineAppearanceProvider lineAppearanceProvider = new LineAppearanceProvider();
 
 	static public Color getColour(int nr) {
 		return lineAppearanceProvider.getColour(nr);
 	}
 
 	static public int getLineWidth() {
 		return lineAppearanceProvider.getLineWidth();
 	}
 
 	static public Plot1DStyles getStyle(int nr) {
 		return lineAppearanceProvider.getStyle(nr);
 	}
 
 	/**
 	 * Returns the tree viewer which shows the resource hierarchy.
 	 * 
 	 * @return the tree viewer
 	 * @since 2.0
 	 */
 	TreeViewer getTreeViewer() {
 		return legend.getTreeViewer();
 	}
 
 	private ActionGroup actionGroup;
 
 	/**
 	 * @param parent
 	 * @param actionBars
 	 * @param partName
 	 * @param toolbarActions
 	 */
 	void createAndRegisterPlotActions(final Composite parent, IActionBars actionBars, String partName,
 			                       final List<IAction> toolbarActions) {
 
 		for (IAction iAction : toolbarActions) {
 			actionBars.getToolBarManager().add(iAction);
 		}
 
 // TODO Get this working
 //			@Override
 //			public void plotActionPerformed(final PlotActionEvent event) {
 //				if (event instanceof PlotActionComplexEvent) {
 //					// if the event has come from right click then show the data table dialog.
 //					parent.getDisplay().asyncExec(new Runnable() {
 //						@Override
 //						public void run() {
 //							PlotDataTableDialog dataDialog = new PlotDataTableDialog(parent.getShell(),
 //									(PlotActionComplexEvent) event);
 //							dataDialog.open();
 //						}
 //					});
 //				} else {
 //					parent.getDisplay().asyncExec(new Runnable() {
 //						@Override
 //						public void run() {
 //							double x = event.getPosition()[0];
 //							double y = event.getPosition()[1];
 //							LivePlotComposite.this.plotView.setPositionLabel(String.format("X:%.6g Y:%.6g", x, y));
 //						}
 //					});
 //				}
 //			}
 
 
 	}
 	/*
 	 * Made final as the value is passed to members in constructors so a change at this level would be
 	 * invalid if not passed down to members as well.
 	 */
 	
 	private final String archiveFolder;
 
 	void initContextMenu(IWorkbenchPartSite site, IWorkbenchWindow window, final ActionGroup parentActions) {
 		actionGroup = new XYLegendActionGroup(window, this);
 		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
 		menuMgr.setRemoveAllWhenShown(true);
 		menuMgr.addMenuListener(new IMenuListener() {
 			@Override
 			public void menuAboutToShow(IMenuManager manager) {
 				IStructuredSelection selection = (IStructuredSelection) getTreeViewer().getSelection();
 				ActionContext cntx = new ActionContext(selection);
 				actionGroup.setContext(cntx);
 				actionGroup.fillContextMenu(manager);
 				if (parentActions != null) {
 					parentActions.setContext(cntx);
 					parentActions.fillContextMenu(manager);
 				}
 			}
 		});
 		TreeViewer viewer = getTreeViewer();
 		Menu menu = menuMgr.createContextMenu(viewer.getTree());
 		viewer.getTree().setMenu(menu);
 		site.registerContextMenu(menuMgr, viewer);
 	}
 
 	@Override
 	public void dispose() {
 		super.dispose();
 		plotView.dispose();
 	}
 
 	/**
 	 * @param value
 	 *            True if new scans are to be hidden automatically
 	 */
 	public void setAutoHideNewScan(Boolean value) {
 		legend.setAutoHideNewScan(value);
 	}
 
 	/**
 	 * @param value
 	 *            True if lasts scans are to be hidden automatically
 	 */
 	public void setAutoHideLastScan(Boolean value) {
 		legend.setAutoHideLastScan(value);
 	}
 
 	/**
 	 * @return true if new scans are not made visible
 	 */
 	public Boolean getAutoHideNewScan() {
 		return legend.getAutoHideNewScan();
 	}
 
 	/**
 	 * @return true if last scans are made invisible
 	 */
 	public Boolean getAutoHideLastScan() {
 		return legend.autoHideLastScan;
 	}
 
 	/**
 	 * Hide all scans
 	 */
 	public void hideAll() {
 		legend.hideAll();
 
 	}
 
 	/**
 	 * Clear the graph
 	 */
 	public void clearGraph() {
 		plotView.deleteAllLines();
 		plotter.clearGraph();
 		legend.removeAllItems();
 	}
 
 	/**
 	 * remove the item from the tree whose filename equals the one specified
 	 * 
 	 * @param filename
 	 */
 	public void removeScanGroup(String filename) {
 		legend.removeScanGroup(filename);
 		// the actual XYData is removed in response to a change in the tree structure of the legend
 	}
 
 	public void removeScanTreeObjects(Object[] selectedItems) {
 		legend.removeScanTreeObjects(selectedItems);
 
 	}
 
 	/**
 	 * @param point
 	 */
 	public void addData(IScanDataPoint point) {
 		if (!isDisposed())
 			plotter.addData(point);
 	}
 
 	/**
 	 * @param parent
 	 * @param style
 	 * @param archiveFolder - folder into which data is to be archived during normal running of the composite
 	 */
 	public LivePlotComposite(IWorkbenchPart parentPart, Composite parent, int style, String archiveFolder) {
 		super(parent, style);
 		this.archiveFolder = archiveFolder;
 		this.setLayout(new FillLayout());
 		sashForm = new SashForm(this, SWT.HORIZONTAL);
 		sashForm.setLayout(new FillLayout());
 		plotView = new SubLivePlotView(parentPart, sashForm, SWT.NONE, archiveFolder);
 		legend = new LiveLegend(sashForm, SWT.NONE, plotView);
 		showLenged(true);
 		sashForm.setWeights(WEIGHTS_NORMAL);
 		plotter = new ScanDataPointPlotter(plotView, legend, archiveFolder);
 	}
 
 	private boolean showLegend;
 
 	/**
 	 * @param showLegend
 	 */
 	public void showLenged(Boolean showLegend) {
 		this.showLegend = showLegend;
 		sashForm.setWeights(showLegend ? WEIGHTS_NORMAL : WEIGHTS_NO_LEGEND);
 	}
 
 	/**
 	 * @return true if legend is hidden
 	 */
 	public Boolean getShowLegend() {
 		return showLegend;
 	}
 
 	/**
 	 * Add a scan line
 	 * @param scanIdentifier - scan id from scandatapoint
 	 * @param fileName - file into which data was written
 	 * @param label
 	 * @param xData
 	 * @param yData
 	 * @param visible
 	 * @param reload
 	 */
 	public void addData(String scanIdentifier, String fileName, String label, DoubleDataset xData, DoubleDataset yData,
 			boolean visible, boolean reload) {
 		if (!isDisposed())
 			plotter.addData(scanIdentifier, fileName, null, xData, yData, xData.getName(), label, visible, reload);
 	}
 
 	public void saveState(IMemento memento, String archiveFolder) {
 		plotView.saveState(memento, archiveFolder);
 	}
 
 	public void initFromMemento(IMemento memento, String storeFolderPath) {
 		//clear existing lines
 		plotView.deleteAllLines();
 
 		/*
 		 * The auto setting of visibility needs to be disabled during the restoration as
 		 * the visibilities are saved in the memento
 		 */
 		Boolean autoHideLastScan = getAutoHideLastScan();
 		Boolean autoHideNewScan = getAutoHideNewScan();
 		setAutoHideLastScan(false);
 		setAutoHideNewScan(false);
 		try{
 			IMemento[] allScans = memento.getChildren(MEMENTO_XY_DATA);
 			for (IMemento thisScan : allScans) {
 				try {
 					String archiveFileName = thisScan.getString(MEMENTO_ARCHIVEFILENAME);
 					File file = new File(storeFolderPath + File.separator + archiveFileName);
 					if (!file.exists()) {
 						continue;
 					}
 
 					String uniquename = thisScan.getString(MEMENTO_XYDATA_NAME);
 					String dataFileName = thisScan.getString(MEMENTO_XYDATA_DATAFILENAME);
 
 					String scanIdentifier = thisScan.getString(MEMENTO_XYDATA_SCANNUMBER);
 					boolean visible = thisScan.getBoolean(MEMENTO_XYDATA_VISIBLE);
 					String xAxisHeader = thisScan.getString(MEMENTO_XYDATA_XAXISHEADER);
 					String yAxisHeader = thisScan.getString(MEMENTO_XTDATA_YAXISHEADER);
 
 					LiveData scan = new LiveData(0, uniquename, xAxisHeader, yAxisHeader, archiveFileName, dataFileName);
 					Vector<String> stepIds=new Vector<String>();
 					String [] parts = uniquename.split(",");
 					for(int i=1; i< parts.length-1;i++){
 						stepIds.add(parts[i]);
 					}
 					
 					if(visible ){
 						//if memento states visible then unarchive and simply add to the list of scans
 						scan.unarchive(archiveFolder);
 						DoubleDataset xdata = scan.archive.getxAxis().toDataset();
 						if(xdata == null || xdata.getSize()==0)
 							continue;
 						plotter.addData(scanIdentifier, dataFileName, stepIds, xdata,
 								scan.archive.getyVals(), xAxisHeader, yAxisHeader, true, false);
 						
 					}else {
 						/**
 						 * we do not want to unarchive the data is it is not visible so we make the system create a 
 						 * dummy line and then change it to archive state by setting archivefilename 
 						 */
 						int linenum = plotter.addData(scanIdentifier, dataFileName, stepIds, new DoubleDataset(1),
 							new DoubleDataset(1), xAxisHeader, yAxisHeader, false, false);
 						plotView.getXYData(linenum).archiveFilename = scan.archiveFilename;//we need to set to archiveFilename in scan as currently equal to null
 						plotView.getXYData(linenum).archive=null;
 					}
 
 				} catch (Throwable e) {
 					logger.warn("Error restoring previous state.", e);
 				}
 			}
 		}finally{
 			setAutoHideLastScan(autoHideLastScan);
 			setAutoHideNewScan(autoHideNewScan);
 		}
 		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
 			@Override
 			public void run() {
 				//whilst adding lines above we have force reload of the model off - by passing false to the addData methods
 				//we need to run reload once to ensure the tree gets displayed
 				legend.reload();
 			}
 		});
 		//get the data plotter
 		plotView.onUpdate(true);
 		
 	}
 
 	String getArchiveFolder() {
 		return archiveFolder;
 	}
 
 	public Object getPlottingSystem() {
 		return this.plotView.plottingSystem;
 	}
 }
 
 /**
  * TODO FIXME DO NOT COPY THIS CLASS. THIS IS A TEMPORARY MEASURE TO ALLOW
  * NEW PLOTTING TO WORK WITH VIEW SIMILAR TO XYPLOTVIEW. BECAUSE OF THE LIMITATIONS
  * OF THE OLD PLOTTING, THIS CLASS HAS DEPARTED FROM A GOOD DESIGN RELATIVE TO THE 
  * NEW PLOTTING. PLANNED IS A SIMPLER ABSTRACT CLASS OR TOOL TO MONITOR SCANS
  */
 class SubLivePlotView extends Composite implements XYDataHandler {
 	private static final Logger logger = LoggerFactory.getLogger(SubLivePlotView.class);
 	static public final String ID = "uk.ac.gda.client.xyplotview";
 	protected AbstractPlottingSystem plottingSystem;
 	LiveData dummy; // used when all other lines are invisible
 
 	private final String archiveFolder;
 
 	public SubLivePlotView(IWorkbenchPart parentPart, Composite parent, int style, String archiveFolder) {
 		
 		super(parent, style);
 		this.archiveFolder = archiveFolder;
 		dummy = new LiveData(1, "", "x", "y", null,null);
 		dummy.addPointToLine(0., 0.);
 		dummy.addPointToLine(1., 1.);
 		dummy.setVisible(false, archiveFolder);
 
 		GridLayout layout = new GridLayout();
 		layout.numColumns = 1;
 		setLayout(layout);
 		GridUtils.removeMargins(this);
 
 		Composite plotArea = new Composite(this, SWT.NONE);
 		plotArea.setLayout(new FillLayout());
 		{
 			GridData gridData = new GridData();
 			gridData.horizontalAlignment = SWT.FILL;
 			gridData.grabExcessHorizontalSpace = true;
 			gridData.grabExcessVerticalSpace = true;
 			gridData.verticalAlignment = SWT.FILL;
 			plotArea.setLayoutData(gridData);
 		}
 
 		try {
 			// We always have a light weight one for this view as there is already 
 			// another using DatasetPlot.
 			this.plottingSystem = PlottingFactory.getLightWeightPlottingSystem();
 		} catch (Exception ne) {
 			logger.error("Cannot create a plotting system!", ne);
 			return;
 		}
 		IActionBars bars    = parentPart instanceof IViewPart
 				            ? ((IViewPart)parentPart).getViewSite().getActionBars()
 				            : null;
 		plottingSystem.createPlotPart(plotArea, parentPart.getTitle(), bars, PlotType.PT1D, parentPart);
 		plottingSystem.setXfirst(true);
 	}
 
 	void saveState(IMemento memento, String archiveFolder) {
 		Vector<String> archivedFiles=new Vector<String>();
 		for (LiveData scan : scans){
 			if (scan != null) {
 				try {
 					archivedFiles.add(scan.saveState(memento, archiveFolder));
 				} catch (Exception e) {
 					logger.warn("Error saving state of scan " + scan.name, e);
 				}
 
 			}
 		}
 		//The folder is only to contain files from XYData in this composite so
 		//delete all other files to reduce disk usage
 		File folder = new File(archiveFolder);
 		for( File f : folder.listFiles()){
 			if( !archivedFiles.contains(f.getName()))
 				if(!f.delete())
 					logger.warn("Unable to delete file " + f.getAbsolutePath());
 
 		}
 	}
 
 	
 	void setPositionLabel(String label) {
 		//positionLabel.setText(label);
 	}
 
 	/**
 	 * Entry in scans array that is to contain the next XYData
 	 */
 	int nextUnInitialisedLine = 0;
 	LiveData scans[] = new LiveData[0];
 
 	private UpdatePlotQueue updateQueue = new UpdatePlotQueue();
 
 	LiveData getXYData(int line) {
 		if (line > scans.length - 1)
 			throw new IllegalArgumentException("line > scans.length");
 		return scans[line];
 	}
 
 	@Override
 	public void addPointToLine(int which, double x, double y) {
 		if (!isDisposed()) {
 			getXYData(which).addPointToLine(x, y);
 			updateQueue.update(this);
 		}
 	}
 
 	@Override
 	public void archive(boolean all, String archiveFolder) throws IOException {
 		if (!isDisposed()) {
 			for (LiveData data : scans) {
 				if (data != null && !data.isVisible())
 					data.archive(archiveFolder);
 			}
 			updateQueue.update(this);
 		}
 	}
 
 	@Override
 	public void copySettings(XYDataHandler other) {
 	}
 
 	@Override
 	public void deleteAllLines() {
 		if (!isDisposed()) {
 			plottingSystem.clear();
 			scans = new LiveData[0];
 			nextUnInitialisedLine = 0;
 		}
 	}
 
 	@Override
 	public void dispose() {
 		updateQueue.setKilled(true);
 		if (plottingSystem != null) {
 			plottingSystem.dispose();
 			plottingSystem = null;
 		}
 	}
 
 	@Override
 	public Range getLeftDomainBounds() {
 		return null;
 	}
 
 	@Override
 	public int getNextAvailableLine() {
 		return nextUnInitialisedLine;
 	}
 
 	@Override
 	public Double getStripWidth() {
 		return null;
 	}
 
 	@Override
 	public NumberFormat getXAxisNumberFormat() {
 		return null;
 	}
 
 	@Override
 	public NumberFormat getYAxisNumberFormat() {
 		return null;
 	}
 
 	@Override
 	public void initializeLine(int which, int axis, String name, String xAxisHeader, String yAxisHeader, String dataFileName) {
 		checkScansArray(which);
 		//name = TraceUtils.getUniqueTrace(name, plottingSystem);
 		scans[which] = new LiveData(which, name, xAxisHeader, yAxisHeader, null, dataFileName);
 		nextUnInitialisedLine = which + 1;
 	}
 
 	private void checkScansArray(int which) {
 		if (scans.length - 1 < which) {
 			scans = Arrays.copyOf(scans, (which + 10) * 2);
 		}
 	}
 
 	@Override
 	public void setDomainBounds(Range domainBounds) {
 		// do nothing
 	}
 
 	@Override
 	public void setLeftRangeBounds(Range leftRangeBounds) {
 		// do nothing
 	}
 
 	@Override
 	public void setLegendVisible(boolean newValue) {
 		// do nothing
 	}
 
 	@Override
 	public void setLineColor(int which, Color color) {
 		if (!isDisposed()) {
 			getXYData(which).setLineColor(color, archiveFolder);
 			updateQueue.update(this);
 		}
 
 	}
 
 	@Override
 	public void deleteLine(int which) {
 		if (!isDisposed()) {
 			if (which > scans.length - 1) throw new IllegalArgumentException("which > scans.length");
 			LiveData line = scans[which];
 			if(line != null) line.deleteArchive(archiveFolder);
 			scans[which] = null;
 			updateQueue.update(this);
 		}
 	}
 
 	@Override
 	public void setLineMarker(int which, Marker marker) {
 	}
 
 	@Override
 	public void setLineType(Type t) {
 		// do nothing
 	}
 
 	@Override
 	public void setLineVisibility(int which, boolean visibility) {
 		if (!isDisposed()) {
 			getXYData(which).setVisible(visibility, archiveFolder);
 			updateQueue.update(this);
 		}
 
 	}
 
 	@Override
 	public void setRightRangeBounds(Range rightRangeBounds) {
 
 	}
 
 	@Override
 	public void setScientificXAxis() {
 
 	}
 
 	@Override
 	public void setScientificYAxis() {
 	}
 
 	@Override
 	public void setTitle(String title) {
 		plottingSystem.setTitle(title);
 	}
 
 	@Override
 	public void setTurboMode(boolean turboMode) {
 	}
 
 	@Override
 	public void setVerticalXAxisTicks(boolean value) {
 	}
 
 	@Override
 	public void setXAxisLabel(String label) {
 	}
 
 	@Override
 	public void setYAxisLabel(String label) {
 	}
 
 	@Override
 	public void setZooming(boolean zooming) {
 	}
 
 	@Override
 	public void unArchive() {
 	}
 
 	@Override
 	public Color getLineColor(int which) {
 		if (!isDisposed())
 			return getXYData(which).getLineColor(archiveFolder);
 		return Color.BLACK;
 	}
 
 	protected Plot1DAppearance getAppearanceCopy(int which) {
 		if (!isDisposed())
 			return getXYData(which).getAppearanceCopy(archiveFolder);
 		return null;
 
 	}
 
 	@Override
 	public Marker getLineMarker(int which) {
 		return null;
 	}
 
 	/**
 	 * This method is the incorrect design. It is called to replot all data
 	 * whenever one data changes. Instead information about which data changed
 	 * should not be lost and then the correct plot only updated.
 	 */
 	@Override
 	public void onUpdate(boolean force) {
 		
 		if (plottingSystem == null) return;
 		try {
 			/**
 			 * TODO FIXME This class taken from XYPlotComponent replots all the plots
 			 * if one point is added to the end of one plot. Instead it should just add the 
 			 * data to the one plot that is changing.
 			 */
 			List<AbstractDataset>    ys  = new Vector<AbstractDataset>();
 			List<AxisValues>      x_axes = new Vector<AxisValues>();
 			final List<String>     invis = new Vector<String>();
 			final Map<String,Plot1DAppearance> appearances = new HashMap<String, Plot1DAppearance>();
 			String xAxisHeader = null;
 			String yAxisHeader = null;
 			boolean xAxisIsVarious = false;
 			boolean yAxisIsVarious = false;
 			for (LiveData sd : scans) {
 				if (sd != null  && sd.number > 1 ) {//do not show lines with only 1 point as the datasetplotter throws exceptions
 					if (sd.isVisible()) {
 						
 						ys.add(sd.archive.getyVals());
 						AbstractDataset y = sd.archive.getyVals();
 						if (y.getName()==null || "".equals(y.getName())) {
 							y.setName(sd.name);
 						}
 						x_axes.add(sd.archive.getxAxis());
 						appearances.put(sd.archive.getyVals().getName(), sd.archive.getAppearance());
 						if (!xAxisIsVarious && StringUtils.hasLength(sd.xLabel)) {
 							if (xAxisHeader == null) {
 								xAxisHeader = sd.xLabel;
 							} else if (!sd.xLabel.equals(xAxisHeader)) {
 								xAxisHeader = "various";
 								xAxisIsVarious = true;
 							}
 						}
 						if (!yAxisIsVarious && StringUtils.hasLength(sd.yLabel)) {
 							if (yAxisHeader == null) {
 								yAxisHeader = sd.yLabel;
 							} else if (!sd.yLabel.equals(yAxisHeader)) {
 								yAxisHeader = "various";
 								yAxisIsVarious = true;
 							}
 						}
 					} else {
 						try {
 						    invis.add(sd.archive.getyVals().getName());
 						} catch (NullPointerException npe) {
 							continue;
 						}
 					}
 				}
 			}
 			if (ys.isEmpty()) {
 				plottingSystem.clear();
 				return;
 			}
 			if (ys.isEmpty()) {
 				ys.add(dummy.archive.getyVals());
 				x_axes.add(dummy.archive.getxAxis());
 				appearances.put(dummy.archive.getyVals().getName(), dummy.archive.getAppearance());
 				yAxisHeader="";
 				xAxisHeader="";
 			}
 			// always replace plots even if list is empty as they may be invisible
 			AbstractDataset x = x_axes.size()>0
 					          ? x_axes.get(0).toDataset()
 					          : null;
 		    if (x==null) {
 		    	x = ys.size()>0
 				          ? AbstractDataset.arange(ys.get(0).getSize(), AbstractDataset.INT32)
 				          : null;
 		    }
 		    
 			if (x!=null) {
 				createUpdatePlot(xAxisHeader, yAxisHeader, x, ys, appearances, invis);
 			} else {
 				plottingSystem.reset();
 			}
 
 		} catch (Throwable e) {
 			logger.warn(e.getMessage(),e);
 		}
 	}
 
 	/** 
 	 * Updates the plot in the UI Thread, creates new traces where required.
 	 * 
        This horrendous way of doing it, results from the fact that we
        reuse XYPlotComposite. A better way would be with updating and 
        creating individual traces. TODO Convert to more logical design.	
 		              
 	 * @param x
 	 * @param ys
 	 * @param appearances
 	 */
 	private void createUpdatePlot(final String                xLabel, 
 			      				  final String                yLabel, 
 			      				  final AbstractDataset       x, 
 			                      final List<AbstractDataset> ys, 
 			                      final Map<String, Plot1DAppearance> appearances,
 			                      final List<String>          invis) {
 		
		final String title = yLabel + " / " + xLabel;
 		Display.getDefault().syncExec(new Runnable() {
 			@Override
 			public void run() {
 				
 				plottingSystem.setTitle(title);
 				plottingSystem.getSelectedXAxis().setTitle(xLabel);
 				plottingSystem.getSelectedYAxis().setTitle(yLabel);
 				
 				final List<AbstractDataset> unfoundYs = new ArrayList<AbstractDataset>(ys.size());
 				
 				/**
 				 * Update what we can, after that create new plots.
 				 */
 				for (final AbstractDataset y : ys) {
 					
 					final ITrace trace = plottingSystem.getTrace(y.getName());
 					if (trace!=null && trace instanceof ILineTrace) {
 						
 						final ILineTrace lineTrace = (ILineTrace)trace;
 						lineTrace.setData(x, y);
 					} else {
 						unfoundYs.add(y);
 					}
 				}
 				
 				// We create the new ones and assign their new colour
 				for (final AbstractDataset y : unfoundYs) {
 			
 					if (y.getName()==null || "".equals(y.getName())) {
 						logger.error("y dataset is not named - it should be!");
 					}
 					ILineTrace trace = plottingSystem.createLineTrace(y.getName());
 					trace.setLineWidth(1);
 					trace.setPointSize(3);
 					trace.setPointStyle(PointStyle.POINT);
 					
 					if (appearances.containsKey(trace.getName())) {
 						final Color color = appearances.get(trace.getName()).getColour();
 						trace.setTraceColor(new org.eclipse.swt.graphics.Color(null, color.getRed(), color.getGreen(), color.getBlue()));
 					} else { // We still want a unique colour
 						trace.setTraceColor(ColorUtility.getSwtColour(ys.indexOf(y)));
 					}
 					trace.setData(x, y);
 					plottingSystem.addTrace(trace);
 				}
 				/**
 				 *  BODGE WARNING There is no clear start of scan or start of new plot in
 				 *  XYDataHandler. Instead we deduce that anything not visible should be removed.
 				 */
 				for (String traceName : invis) {
 					ITrace trace = plottingSystem.getTrace(traceName);
 					if (trace!=null) plottingSystem.removeTrace(trace);
 				}
 
 				if (plottingSystem.isRescale()) {
 					plottingSystem.autoscaleAxes();
 				}
 			}
 		});
 	}
 
 	@Override
 	public void setsPointsForLine(int which, DoubleDataset xData, DoubleDataset yData) {
 		if (!isDisposed()) {
 			getXYData(which).setsPointsForLine(xData, yData, archiveFolder);
 			updateQueue.update(this);
 		}
 	}
 
 	public void setLineWidth(int lineNumber, int lineWidth) {
 		if (!isDisposed()) {
 			getXYData(lineNumber).setLineWidth(lineWidth, archiveFolder);
 		}
 	}
 
 	public void setPlot1DStyles(int lineNumber, Plot1DStyles style) {
 		if (!isDisposed()) {
 			getXYData(lineNumber).setPlot1DStyles(style, archiveFolder);
 		}
 	}
 }
 
 /**
  * Class to hold the data and state of a single XY line
  * The data can be archived to file, indicated by archiveFilename being non null
  * To allow mementos to be copied along with archive folders archiveFilename only holds the filename and not the
  * path. The unarchive, archive methods will need an archive folder.
  * 
  * TODO FIXME DO NOT COPY THIS CLASS. THIS IS A TEMPORARY MEASURE TO ALLOW
  * NEW PLOTTING TO WORK WITH VIEW SIMILAR TO XYPLOTVIEW. BECAUSE OF THE LIMITATIONS
  * OF THE OLD PLOTTING, THIS CLASS HAS DEPARTED FROM A GOOD DESIGN RELATIVE TO THE 
  * NEW PLOTTING. PLANNED IS A SIMPLER ABSTRACT CLASS OR TOOL TO MONITOR SCANS
  */
 class LiveData {
 
 	private static final Logger logger = LoggerFactory.getLogger(LiveData.class);
 
 	int number = 0;
 	LiveDataArchive archive; // data and appearance
 	String name; // a mix of the name of the group of plots (scan number) and the name of the line (column header)
 	String archiveFilename = null; // the file created by the archive() method and read by unarchive()
 	int which = 0;
 	String xLabel;
 	String yLabel;
 
 	/**
 	 * Scan file holding all the data of the scan
 	 */
 	private final String dataFileName;
 
 	/**
 	 * 
 	 * @param which - index in the array of xydata - used to get appearance and color
 	 * @param name - unique name, scanid: steps... ylabel
 	 * @param xLabel
 	 * @param yLabel
 	 * @param archiveFilename - archive holding the data, only filename, you need to combine with the archive folder
 	 * @param dataFileName - the name of the file holding the scan data
 	 */
 	LiveData(int which, String name, String xLabel, String yLabel, String archiveFilename,
 			String dataFileName) {
 		this.name = name;
 		this.which = which;
 		this.xLabel = xLabel;
 		this.yLabel = yLabel;
 		this.archiveFilename = archiveFilename;
 		this.dataFileName = dataFileName;
 		if( archiveFilename == null){
 			resetArchive(which);
 		}
 	}
 
 	public void deleteArchive(String archiveFolder) {
 		if(isArchived()){
 			File f= new File(getArchivePath(archiveFolder, archiveFilename));
 			if( !f.delete())
 				logger.warn("Unable to delete file " + f.getAbsolutePath());
 			archiveFilename = null;
 			//ensure the archive daya exists in case it is referenced somewhere
 			resetArchive(which);
 		}
 		
 	}
 
 	public boolean isArchived(){
 		return archiveFilename != null;
 	}
 
 	String getArchiveFilename(){
 		String archivedFilename = name.replaceAll(" +", "_");
 		return archivedFilename.replaceAll("[,;:]", "_");
 	}
 
 	/**
 	 * Saves the state into the memento and into a file in the archive folder
 	 * @param memento
 	 * @param archiveFolder
 	 * @return The filename of the archive - not the full path
 	 * @throws IOException
 	 */
 	public String saveState(IMemento memento, String archiveFolder) throws IOException {
 		//note that the data may be archived at this point so archive==null
 		String archivedFilename = getArchiveFilename();
 		persistToFilePath(archiveFolder, archivedFilename);
 		IMemento child = memento.createChild(LivePlotComposite.MEMENTO_XY_DATA);
 		child.putString(LivePlotComposite.MEMENTO_XYDATA_NAME, name);
 		child.putString(LivePlotComposite.MEMENTO_ARCHIVEFILENAME, archivedFilename);//archivedFilePath);
 		child.putString(LivePlotComposite.MEMENTO_XYDATA_DATAFILENAME, dataFileName);
 		child.putString(LivePlotComposite.MEMENTO_XYDATA_SCANNUMBER, deriveScanIdentifier(name));
 		child.putString(LivePlotComposite.MEMENTO_XYDATA_XAXISHEADER, xLabel);
 		child.putString(LivePlotComposite.MEMENTO_XTDATA_YAXISHEADER, yLabel);
 		child.putBoolean(LivePlotComposite.MEMENTO_XYDATA_VISIBLE, archive != null ? archive.getAppearance().isVisible()
 				: false);
 		return archivedFilename;
 	}
 
 	private String deriveScanIdentifier(String name2) {
 		//name is of type Scan:<number>_<name>
 		if(name2.startsWith("Scan:")){
 			int spaceAfterScanName = name2.indexOf(" ");
 			return name2.substring(5, spaceAfterScanName);
 		}
 		throw new IllegalArgumentException("Name of plot line is invalid " + name2);
 	}
 
 	public void setsPointsForLine(DoubleDataset xData, DoubleDataset yData, String archiveFolder) {
 		unarchive(archiveFolder);
 		// we need ensure AxisValues are all increasing
 		double[] xdata = xData.getData().clone();
 		double[] ydata = yData.getData().clone();
 
 		if (xdata.length == ydata.length) {
 			for (int i = xdata.length - 1; i > 1; i--) {
 				for (int j = 0; j < i; j++) {
 					if (xdata[j] > xdata[i]) {
 						double tmp = xdata[i];
 						xdata[i] = xdata[j];
 						xdata[j] = tmp;
 
 						tmp = ydata[i];
 						ydata[i] = ydata[j];
 						ydata[j] = tmp;
 					}
 				}
 			}
 			DoubleDataset dds = new DoubleDataset(ydata);
 			dds.setName(name);
 			archive.setData(new AxisValues(xdata), new DoubleDataset(ydata));
 			number = archive.getxAxis().size();
 		}
 	}
 
 	protected Plot1DAppearance getAppearanceCopy(String archiveFolder) {
 		unarchive(archiveFolder);
 		if (archive!=null) {
 			Plot1DAppearance appearance = archive.getAppearance();
 			return new Plot1DAppearance(appearance.getColour(), appearance.getStyle(), appearance.getLineWidth(),
 					appearance.getName());
 		}
 		return null;
 	}
 
 	public void setVisible(boolean visibility, String archiveFolder) {
 		if (visibility) { // if we are to maek visible then unarchive
 			unarchive(archiveFolder);
 		}
 		if (archive != null) {
 			archive.getAppearance().setVisible(visibility);
 		}
 	}
 
 	void unarchive(String archiveFolder) {
 		try {
 			if (archiveFilename != null) {
 				String archiveFilenameCopy = getArchivePath(archiveFolder, archiveFilename);
 				logger.info("XYData.unarchive from " + archiveFilename);
 				FileInputStream f_in = null;
 				ObjectInputStream obj_in = null;
 				try {
 					f_in = new FileInputStream(archiveFilenameCopy);
 					obj_in = new ObjectInputStream(f_in);
 					Object obj = obj_in.readObject();
 					if (!(obj instanceof LiveDataArchive)) return; // From scan data view, cannot deal with
 					archive = (LiveDataArchive) obj;
 					number = archive.getxAxis().size();
 					archiveFilename = null;
 				} finally {
 					if (obj_in != null)
 						obj_in.close();
 					if (f_in != null){
 						f_in.close();
 						if(archiveFilename==null){
 							File f = new File(archiveFilenameCopy);
 							f.delete();
 						}
 							
 					}
 				}
 			}
 		} catch (Exception e) {
 			logger.warn("Error unarchiving plot data", e);
 			archiveFilename = null;
 			resetArchive(which);
 		}
 	}
 
 	void archive(String archiveFolder) {
 		try {
 			if (archiveFilename == null) {
 				persistToFilePath( archiveFolder, getArchiveFilename());
 				archiveFilename = getArchiveFilename();//archivedFilePath;
 				archive = null;
 				logger.info("XYData.archive to " + archiveFilename);
 			}
 		} catch (Exception ex) {
 			logger.warn(ex.getMessage(), ex);
 		}
 	}
 
 	private void resetArchive(int which) {
 		
 		Plot1DAppearance appearance = new Plot1DAppearance(LivePlotComposite.getColour(which),
 				LivePlotComposite.getStyle(which), LivePlotComposite.getLineWidth(), name);
 		archive = new LiveDataArchive(appearance, new DoubleDataset(1), new AxisValues());
 		archiveFilename=null;
 
 	}
 
 	private static String  getArchivePath(String archiveFolder, String filename){
 		return archiveFolder + System.getProperty("file.separator") + filename;
 	}
 	private String persistToFilePath(String archiveFolder, String target_filename) throws IOException {
 		// create a unique file in the workspace
 		String archivedFilePath = getArchivePath(archiveFolder, target_filename);
 		
 		if( this.isArchived()){
 			FileUtil.copy(archiveFolder + File.separator + this.archiveFilename, archivedFilePath);
 		} else {
 			File file = new File(archivedFilePath);
 			file.createNewFile();
 
 			persistToFile(new File(archivedFilePath));
 		}
 		return archivedFilePath;
 	}
 
 	private void persistToFile(File tempFile) throws FileNotFoundException, IOException {
 		//if already archived simply copy into new file
 		FileOutputStream f_out = null;
 		ObjectOutputStream obj_out = null;
 		try {
 			f_out = new FileOutputStream(tempFile);
 			obj_out = new ObjectOutputStream(f_out);
 			obj_out.writeObject(archive);
 			obj_out.flush();
 			obj_out.reset(); // if not you get an OuOfMemoryException eventually
 		} finally {
 			if (obj_out != null)
 				obj_out.close();
 			if (f_out != null)
 				f_out.close();
 		}
 	}
 
 	void setLineColor(Color color, String archiveFolder) {
 		unarchive(archiveFolder);
 		archive.getAppearance().setColour(color);
 	}
 
 	void setPlot1DStyles(Plot1DStyles style, String archiveFolder) {
 		unarchive(archiveFolder);
 		archive.getAppearance().setStyle(style);
 	}
 
 	void setLineWidth(int width, String archiveFolder) {
 		unarchive(archiveFolder);
 		archive.getAppearance().setLineWidth(width);
 	}
 
 	Color getLineColor(String archiveFolder) {
 		unarchive(archiveFolder);
 		return archive.getAppearance().getColour();
 	}
 
 	boolean isVisible() {
 		return archive != null ? archive.getAppearance().isVisible() : false;
 	}
 
 	/*
 	 * Values in AxisValues must always go from min to max.
 	 */
 	void addPointToLine(double x, double y) {
 		// get existing 'old' values
 		DoubleDataset dataset = archive.getxAxis().toDataset();
 		double[] xvals_old = dataset != null ? dataset.getData() : new double[0];
 		double[] yvals_old = archive.getyVals().getData();
 
 		double[] yvals_new = null;
 		double[] xvals_new = null;
 		int old_length = xvals_old.length;
 
 		if (old_length == 0) {
 			// first point
 			yvals_new = new double[] { y };
 			xvals_new = new double[] { x };
 		} else {
 			// not first so copy old to new before adding to end or inserting
 			yvals_new = Arrays.copyOf(yvals_old, old_length + 1);
 			xvals_new = Arrays.copyOf(xvals_old, old_length + 1);
 			if (x >= xvals_new[old_length - 1]) {
 				// add to the end
 				xvals_new[old_length] = x;
 				yvals_new[old_length] = y;
 			} else {
 				// insert at correct point
 				boolean added = false;
 				for (int x_index = 0; x_index < old_length; x_index++) {
 					if (x < xvals_old[x_index]) {
 						// shift and insert
 						// find position in x axis at which to insert the new value ( x values must increase through the
 						// array)
 						// shift existing values up by 1 to make space to insert the new value
 						// if x_index = 1 and old_length = 10 this will shift items 1 to 2 for 9 elements and insert the
 						// new val at 0
 						int length = old_length - x_index;
 						System.arraycopy(xvals_new, x_index, xvals_new, x_index + 1, length);
 						System.arraycopy(yvals_new, x_index, yvals_new, x_index + 1, length);
 						yvals_new[x_index] = y;
 						xvals_new[x_index] = x;
 						added = true;
 						break;
 					}
 				}
 				if (!added) {
 					logger.warn("Cannot find point to insert the new point - should not happen");
 				}
 			}
 
 		}
 
 		// create new AxisValues and DataSet
 		final DoubleDataset yds = new DoubleDataset(yvals_new);
 		yds.setName(name);
 		archive.setData(new AxisValues(xvals_new), yds);
 		number = xvals_new.length;
 	}
 
 	@Override
 	public String toString() {
 		return name;
 	}
 }
 
 /**
  * TODO FIXME DO NOT COPY THIS CLASS. THIS IS A TEMPORARY MEASURE TO ALLOW
  * NEW PLOTTING TO WORK WITH VIEW SIMILAR TO XYPLOTVIEW. BECAUSE OF THE LIMITATIONS
  * OF THE OLD PLOTTING, THIS CLASS HAS DEPARTED FROM A GOOD DESIGN RELATIVE TO THE 
  * NEW PLOTTING. PLANNED IS A SIMPLER ABSTRACT CLASS OR TOOL TO MONITOR SCANS
  */
 class LiveDataArchive implements Serializable {
 	
 	/**
 	 * Change the value of serialVersionUID is items in this class change
 	 */
 	static final long serialVersionUID = 42L;
 	
 	private Plot1DAppearance appearance;
 	private DoubleDataset yVals;
 	private AxisValues xAxis; 
 
 	public LiveDataArchive(Plot1DAppearance appearance, DoubleDataset yVals, AxisValues xAxis) {
 		super();
 		if (appearance == null || yVals == null || xAxis == null )
 			throw new IllegalArgumentException("Invalid args");
 		this.appearance = appearance;
 		this.yVals = yVals;
 		this.xAxis = xAxis;
 	}
 
 	public void setData(AxisValues axisValues, DoubleDataset doubleDataset) {
 		if (yVals == null || xAxis == null)
 			throw new IllegalArgumentException("Invalid args");
 		xAxis = axisValues;
 		yVals = doubleDataset;
 	}
 
 	public Plot1DAppearance getAppearance() {
 		return appearance;
 	}
 
 	public DoubleDataset getyVals() {
 		return yVals;
 	}
 
 	public AxisValues getxAxis() {
 		return xAxis;
 	}
 
 }
 
 
 class XYLegendActionGroup extends ActionGroup {
 
 	private LivePlotComposite comp;
 	IWorkbenchWindow window;
 
 	XYLegendActionGroup(IWorkbenchWindow window, LivePlotComposite comp) {
 		this.comp = comp;
 		this.window = window;
 	}
 
 	@Override
 	public void fillContextMenu(IMenuManager menu) {
 		super.fillContextMenu(menu);
 		IStructuredSelection selection = (IStructuredSelection) getContext().getSelection();
 
 		boolean anyResourceSelected = !selection.isEmpty();
 		if (anyResourceSelected) {
 			addNewWindowAction(menu, selection);
 		}
 
 	}
 
 	/**
 	 * Adds the Open in New Window action to the context menu.
 	 * 
 	 * @param menu
 	 *            the context menu
 	 * @param selection
 	 *            the current selection
 	 */
 	private void addNewWindowAction(IMenuManager menu, IStructuredSelection selection) {
 
 		// Only supported if exactly one container (i.e open project or folder) is selected.
 		if (selection.size() != 1) {
 			return;
 		}
 		Object element = selection.getFirstElement();
 		if (element instanceof ScanPair) {
 			menu.add(new EditAppearanceAction(window, comp, (ScanPair) element));
 		}
 	}
 }
 
 class EditAppearanceAction extends Action implements ActionFactory.IWorkbenchAction {
 
 	/**
 	 * The workbench window; or <code>null</code> if this action has been <code>dispose</code>d.
 	 */
 	private IWorkbenchWindow workbenchWindow;
 
 	private ScanPair pageInput;
 	LivePlotComposite comp;
 
 	public EditAppearanceAction(IWorkbenchWindow window, LivePlotComposite comp, ScanPair copy) {
 		super("Modify Appearance");
 		if (window == null) {
 			throw new IllegalArgumentException();
 		}
 		this.workbenchWindow = window;
 		setToolTipText("Modify Appearance");
 		this.comp = comp;
 		pageInput = copy;
 		// window.getWorkbench().getHelpSystem().setHelp(this, IWorkbenchHelpContextIds.OPEN_NEW_WINDOW_ACTION);
 	}
 
 	/**
 	 * The implementation of this <code>IAction</code> method opens a new window. The initial perspective for the new
 	 * window will be the same type as the active perspective in the window which this action is running in.
 	 */
 	@Override
 	public void run() {
 		if (workbenchWindow == null) {
 			// action has been disposed
 			return;
 		}
 		Plot1DGraphTable colourTable = new Plot1DGraphTable();
 		int lineNumber = pageInput.getLineNumber();
 		Plot1DAppearance copy = comp.plotView.getAppearanceCopy(lineNumber);
 
 		colourTable.addEntryOnLegend(copy);
 		PlotAppearanceDialog pad = new PlotAppearanceDialog(workbenchWindow.getShell(), colourTable);
 		boolean success = pad.open();
 		if (success) {
 			// set linewidth and style directly, set color via the legend which will cause a plot update
 			comp.plotView.setLineWidth(lineNumber, copy.getLineWidth());
 			comp.plotView.setPlot1DStyles(lineNumber, copy.getStyle());
 			ScanLine line = pageInput.getScanLineCopy();
 			line.lineColor = copy.getColour();
 			comp.legend.valueForPathChanged(new TreePath(pageInput.getPath()), line);
 		}
 	}
 
 	/*
 	 * (non-Javadoc) Method declared on ActionFactory.IWorkbenchAction.
 	 * @since 3.0
 	 */
 	@Override
 	public void dispose() {
 		workbenchWindow = null;
 	}
 }
