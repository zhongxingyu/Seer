 package org.eclipse.stem.util.analysis.views;
 
 /*******************************************************************************
  * Copyright (c) 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.birt.chart.device.IDeviceRenderer;
 import org.eclipse.birt.chart.exception.ChartException;
 import org.eclipse.birt.chart.factory.Generator;
 import org.eclipse.birt.chart.model.Chart;
 import org.eclipse.birt.chart.model.ChartWithAxes;
 import org.eclipse.birt.chart.model.attribute.Anchor;
 import org.eclipse.birt.chart.model.attribute.AxisType;
 import org.eclipse.birt.chart.model.attribute.Bounds;
 import org.eclipse.birt.chart.model.attribute.ColorDefinition;
 import org.eclipse.birt.chart.model.attribute.IntersectionType;
 import org.eclipse.birt.chart.model.attribute.LineAttributes;
 import org.eclipse.birt.chart.model.attribute.LineStyle;
 import org.eclipse.birt.chart.model.attribute.Position;
 import org.eclipse.birt.chart.model.attribute.TickStyle;
 import org.eclipse.birt.chart.model.attribute.impl.BoundsImpl;
 import org.eclipse.birt.chart.model.attribute.impl.ColorDefinitionImpl;
 import org.eclipse.birt.chart.model.component.Axis;
 import org.eclipse.birt.chart.model.component.Series;
 import org.eclipse.birt.chart.model.component.impl.SeriesImpl;
 import org.eclipse.birt.chart.model.data.NumberDataSet;
 import org.eclipse.birt.chart.model.data.SeriesDefinition;
 import org.eclipse.birt.chart.model.data.impl.NumberDataElementImpl;
 import org.eclipse.birt.chart.model.data.impl.NumberDataSetImpl;
 import org.eclipse.birt.chart.model.data.impl.SeriesDefinitionImpl;
 import org.eclipse.birt.chart.model.impl.ChartWithAxesImpl;
 import org.eclipse.birt.chart.model.layout.Legend;
 import org.eclipse.birt.chart.model.layout.Plot;
 import org.eclipse.birt.chart.model.type.LineSeries;
 import org.eclipse.birt.chart.model.type.impl.ScatterSeriesImpl;
 import org.eclipse.birt.chart.util.PluginSettings;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.stem.ui.preferences.VisualizationPreferencePage;
 import org.eclipse.stem.util.analysis.Activator;
 import org.eclipse.stem.util.analysis.AggregateDataWriter;
 import org.eclipse.stem.util.analysis.ScenarioAnalysisSuite;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.PaintEvent;
 import org.eclipse.swt.events.PaintListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Canvas;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.Widget;
 import org.eclipse.ui.IWorkbenchActionConstants;
 
 /**
  * TimeSeriesCanvas is a subclass of {@link Canvas} suitable for chart drawings.
  */
 public class TimeSeriesCanvas extends Canvas {
 
 	protected IDeviceRenderer idr = null;
 
 	/**
 	 * This is the {@link Chart} that plots the relative values.
 	 */
 	protected Chart cm = null;
 
 	/**
 	 * The provider of relative values.
 	 */
 	//private RelativeValueHistoryProvider rvhp;
 
 	private AnalysisControl control = null;
 	
 	/**
 	 * the index of this Chart
 	 */
 	private int chartIndex = 0;
 	
 
 	private static String defaultKey = AggregateDataWriter.getKeyS();
 
 	/**
 	 * some extra colors
 	 */
 	protected static final ColorDefinition[] colorDefault = {
 															ColorDefinitionImpl.BLUE(), 
 															ColorDefinitionImpl.BLACK(), 
 															ColorDefinitionImpl.GREY(),
 															ColorDefinitionImpl.CYAN(),
 															ColorDefinitionImpl.ORANGE() 
 															};
 	/**
 	 * used if we need to overlay traces in different colors
 	 */
 	private boolean overlayMode = false;
 	
 	/**
 	 * used to index the line series so we can step through default colors
 	 * when a user custom color is not yet assigned
 	 */
 	private int seriesCount  = 0;
 	
 
 	/**
 	 * A context menu for this view
 	 */
 	Menu popUpMenu = null;
 	
 	/**
 	 * set y axis to a linear scale
 	 */
 	private LinearScaleAction linearTimeAction;
 	/**
 	 * set y axis to a log scale
 	 */
 	private LogScaleAction logTimeAction;
 	protected boolean useLinearTimeScale = true;
 	
 
 	/**
 	 * show the legend (true by default)
 	 */
 	private LegendViewAction viewLegend;
 
 	/**
 	 * hide the legend 
 	 */
 	private LegendHideAction hideLegend;
 	protected boolean showLegend = true;
 
 	protected Legend legend = null;
 	
 
 	/**
 	 * this is a map of the DataSeries object (keyed by property name)
 	 */
 	protected final Map<String,DataSeries> dataSeriesMap = new HashMap<String,DataSeries>();
 	
 
 	/**
 	 * These are the cycle numbers that match the relative values that will be
 	 * plotted
 	 * 
 	 * @see #relativeValues
 	 */
 	private final List<Integer> cycleNumbers = new ArrayList<Integer>();
 
 	/**
 	 * Chart generator instance (reference to a singleton)
 	 */
 	Generator gr;
 
 	Axis yAxisPrimary;
 	Axis xAxisPrimary;
 	/**
 	 * the maxY value for scaling
 	 */
 	double maxY = -1.0;
 
 	/**
 	 * Log of zero is negative infinity so for each location we will cut off the minimum 
 	 * at the min NONZERO Y value
 	 */
 	private double minYscale = 1.0;
 	
 	
 	/**
 	 * Label for line series LEGEND
 	 */
 	public String Ordinate_LEGEND = "Y";
 	
 	/**
 	 * Label for line series Y axis label
 	 **/
 	public String Ordinate_AXIS = "Y";
 	
 	/** 
 	 * customizable color definitions
 	 */
 	private ColorDefinition foreGround = ColorDefinitionImpl.WHITE();
 	private ColorDefinition backgroundGround = ColorDefinitionImpl.BLACK();
 	private ColorDefinition frameColor = ColorDefinitionImpl.create(180, 180, 200);
 
 	Image imgChart = null;
 	
 	/**
 	 * This Constructor is used when we want to place the time series in a container
 	 * which is a sub component of the actual AnalysisControl
 	 * @param analysisControl 
 	 * 
 	 * @param parent  the SWT parent of the {@link Widget}
 	 * @param ordinateString 
 	 * @param yAxisLabel 
 	 * @param defaultYDataType 
 	 * @param foreground 
 	 * @param background 
 	 * @param framecolor 
 	 * @param chartIndex the index of this chart (0 if only one)
 	 * @param overlayMode used if we need to overlay traces in different colors
 	 */
 	public TimeSeriesCanvas(final AnalysisControl analysisControl, final Composite parent, 
 			final String ordinateString, 
 			final String yAxisLabel, 
 			final String defaultYDataType,
 			ColorDefinition foreground,
 			ColorDefinition background,
 			ColorDefinition framecolor,
 			int chartIndex,
 			boolean overlayMode	) {
 		super(parent, SWT.DOUBLE_BUFFERED | SWT.BORDER);
 		Ordinate_LEGEND = ordinateString;
 		Ordinate_AXIS = yAxisLabel;
 		defaultKey = defaultYDataType;
 		foreGround 			= foreground;
 		backgroundGround 	= background;
 		frameColor 			= framecolor;
 		this.chartIndex = chartIndex;
 		this.overlayMode = overlayMode;
 		
 		gr = Generator.instance();
 
 		try {
 			idr = PluginSettings.instance().getDevice("dv.SWT"); //$NON-NLS-1$			
 		} catch (final ChartException pex) {
 			Activator.logError("Problem initializing chart", pex); //$NON-NLS-1$
 			return;
 		}
 
 		control = analysisControl;
 
 		
 		cm = createSimpleLineChart(dataSeriesMap, cycleNumbers, Messages
 				.getString("CC.title")); //$NON-NLS-1$
 		
 		resetData();
 		addPaintListener(new PaintListener() {
 			public void paintControl(final PaintEvent pe) {
 
 				final Composite source = (Composite) pe.getSource();
 				final org.eclipse.swt.graphics.Rectangle d = source
 						.getClientArea();
 
 				if(imgChart != null) imgChart.dispose();
 				imgChart = new Image(source.getDisplay(), d);
 				
 				idr.setProperty(IDeviceRenderer.GRAPHICS_CONTEXT, new GC(
 						imgChart));
 				final Bounds bounds = BoundsImpl.create(d.x, d.y, d.width,
 						d.height);
 				bounds.scale(72d / idr.getDisplayServer().getDpiResolution());
 				// BOUNDS MUST BE SPECIFIED IN POINTS
 
 				try {
 					gr.render(idr, gr.build(idr.getDisplayServer(), cm, bounds,
 							null, null, null));
 					pe.gc.drawImage(imgChart, d.x, d.y);
 				} catch (final ChartException ce) {
 					Activator.logError("Problem rendering chart", ce); //$NON-NLS-1$
 				}
 			} // paintControl
 		} // PaintListener
 		);
 		
 		//Create a context menu for the canvas
 		createContextMenu(this);
 		
 
 	} // TimeSeriesCanvas
 	/**
 	 * Constructor.
 	 * 
 	 * @param parent  the SWT parent of the {@link Widget}
 	 * @param ordinateString 
 	 * @param yAxisLabel 
 	 * @param defaultYDataType 
 	 * @param foreground 
 	 * @param background 
 	 * @param framecolor 
 	 * @param chartIndex the index of this chart (0 if only one)
 	 *  
 	 */
 	public TimeSeriesCanvas(final Composite parent, 
 			final String ordinateString, 
 			final String yAxisLabel, 
 			final String defaultYDataType,
 			ColorDefinition foreground,
 			ColorDefinition background,
 			ColorDefinition framecolor,
 			int chartIndex) {
 		super(parent, SWT.DOUBLE_BUFFERED | SWT.BORDER);
 		Ordinate_LEGEND = ordinateString;
 		Ordinate_AXIS = yAxisLabel;
 		defaultKey = defaultYDataType;
 		foreGround 			= foreground;
 		backgroundGround 	= background;
 		frameColor 			= framecolor;
 		this.chartIndex = chartIndex;
 		
 		gr = Generator.instance();
 
 		try {
 			idr = PluginSettings.instance().getDevice("dv.SWT"); //$NON-NLS-1$			
 		} catch (final ChartException pex) {
 			Activator.logError("Problem initializing chart", pex); //$NON-NLS-1$
 			return;
 		}
 
 		control = (AnalysisControl) parent;
 
 		
 		cm = createSimpleLineChart(dataSeriesMap, cycleNumbers, Messages
 				.getString("CC.title")); //$NON-NLS-1$
 		
 		resetData();
 		addPaintListener(new PaintListener() {
 			public void paintControl(final PaintEvent pe) {
 
 				final Composite source = (Composite) pe.getSource();
 				final org.eclipse.swt.graphics.Rectangle d = source
 						.getClientArea();
 
 				if(imgChart != null) imgChart.dispose();
 				imgChart = new Image(source.getDisplay(), d);
 				idr.setProperty(IDeviceRenderer.GRAPHICS_CONTEXT, new GC(
 						imgChart));
 				final Bounds bounds = BoundsImpl.create(d.x, d.y, d.width,
 						d.height);
 				bounds.scale(72d / idr.getDisplayServer().getDpiResolution());
 				// BOUNDS MUST BE SPECIFIED IN POINTS
 
 				try {
 					gr.render(idr, gr.build(idr.getDisplayServer(), cm, bounds,
 							null, null, null));
 					pe.gc.drawImage(imgChart, d.x, d.y);
 				} catch (final ChartException ce) {
 					Activator.logError("Problem rendering chart", ce); //$NON-NLS-1$
 				}
 			} // paintControl
 		} // PaintListener
 		);
 		
 		//Create a context menu for the canvas
 		createContextMenu(this);
 		
 
 	} // TimeSeriesCanvas
 
 	
 	
 	/**
 	 * The method which gets the {@link TimeSeriesCanvas}' reports list, and
 	 * draws it on the {@link TimeSeriesCanvas}.
 	 * @return integratedDifference for each line series
 	 * 
 	 */
 	public double[] draw() {
 		
 		//clearData();
 		resetData();
 
 		
 		
 		int maxLines = control.getNumProperties(chartIndex);
 	
 		// add all the rest of the line series now
 		for (int i = 0; i < maxLines; i ++) {
 			String property = control.getProperty(chartIndex,i);
 			
 			if(!dataSeriesMap.containsKey(property)) {
 				// new property
 				DataSeries series = new DataSeries(property, seriesCount, overlayMode);
 				seriesCount ++;
 				dataSeriesMap.put(property, series);
 			}
 			DataSeries series = dataSeriesMap.get(property);
 			if(series.isVisible()) {
 				series.show();
 			} else {
 				series.hide();
 			}
 		}
 	
 
 		// update the context menu with the new properties to plot
 		updateContextMenu(this);
 		
 		resetData();
 		
 		
 		double[] integratedDifference = new double[maxLines];
 		for (int i = 0; i < maxLines; i++) {
 			integratedDifference[i] = 0.0;
 		}
 		
 		maxY = -1.0;
 		
 			boolean setCycles = false;
 			// Get the values for the property to be plotted
 			int maxPoints = 0;
 			
 			for (int i = 0; i < maxLines; i++) {
 				String property = control.getProperty(chartIndex,i);
 			
 				final double[] doubleValues = control.getValues(chartIndex,i);
 				
 				DataSeries series = dataSeriesMap.get(property);
 				
 				for (int j = 0; j < doubleValues.length; j++) {
 					integratedDifference[i] += doubleValues[j];
 					
 					if(doubleValues[j] <= minYscale) {
 						if(doubleValues[j] > 0.0) minYscale = doubleValues[j];
 					}
 					
 					if(doubleValues[j] >= maxY) {
 						maxY = doubleValues[j];
 						double log = Math.floor(Math.log10(maxY));
 						double adjustedMax = (Math.ceil(maxY/Math.pow(10, log)))*Math.pow(10, log);
 					    if(adjustedMax <= 1.0) adjustedMax = 1.0;
 					    adjustedMax = maxY*100.0;
 					    adjustedMax += 1.0;
 					    int mx = (int)adjustedMax;
 					    adjustedMax = ((double)mx)/100.0;
 						if (useLinearTimeScale) {
 							yAxisPrimary.getScale().setMax(NumberDataElementImpl.create(adjustedMax));
 							double step = adjustedMax / 10.0;
 							yAxisPrimary.getScale().setStep(step);
 						} else {
 							// keep 2 significant figures on scale axis
 							double ymax = Math.log(adjustedMax);
 							if(adjustedMax > 1.0) {
 								ymax += 0.499;
 								long imax = Math.round(ymax);
 								ymax = imax;
 							}
 							yAxisPrimary.getScale().setMax(NumberDataElementImpl.create(ymax));
 							double step = ymax/10.0;
 							yAxisPrimary.getScale().setStep(step);
 						}
 						
 						
 					}
 				}
 				integratedDifference[i] /= doubleValues.length;
 					// Any values?
 					if (doubleValues.length > 0) {
 						if (maxPoints < doubleValues.length) {
 							maxPoints = doubleValues.length;
 						}
 						
 						for (int cycleNumber = 0; cycleNumber < doubleValues.length; cycleNumber++) {
 							
 							Double value;
 							double displayValue = doubleValues[cycleNumber];
 							if (displayValue <= minYscale) {
 								// Log(0.0) is negative infinity so for display purposes only
 								// we set the minimum axis value at 0.1/POPULATION
 								displayValue = minYscale;
 							}
 							if (useLinearTimeScale) {
 								value = new Double(displayValue);
 							} else {
 								value = new Double(Math.log(displayValue));
 							}
 							
 
 							series.addValue(value);
 							
 							// only do once for first line series
 							if (!setCycles) {
 								cycleNumbers.add(new Integer(cycleNumber));	
 								}
 						} // for cycleNumber
 						
 						
 						
 						setCycles = true; // we set them only once
 						
 					} else {
 						//resetData();
 					}
 			} // for i properties
 
 
 		if (!this.isDisposed()) {
 			redraw();
 		}
 		
 		if(this.chartIndex==1) {
 			control.updateMessage(ScenarioAnalysisSuite.READY_MSG);
 		} else {
 			control.updateMessage(ScenarioAnalysisSuite.WORKING_MSG);
 		}
 		return integratedDifference;
 	} // paintControl
 
 	/**
 	 * @param dataSeriesMap
 	 *            the {@link List} that will contain the relative values
 	 *            (0.0-1.0) to plot
 	 * @param cycleNumbers
 	 *            the {@link List} of simulation cycle numbers that match the
 	 *            relative values
 	 * @param seriesIdentifier
 	 *            the title of the chart
 	 * @return a <code>Chart</code>
 	 */
 	public final Chart createSimpleLineChart(
 			final Map<String, DataSeries> dataSeriesMap,
 			final List<Integer> cycleNumbers, final String seriesIdentifier) {
 		
 		final ChartWithAxes retValue = ChartWithAxesImpl.create();
 
 		
 		// Plot
 		retValue.getBlock().setBackground(frameColor);
 		final Plot p = retValue.getPlot();
 		p.getClientArea().setBackground(backgroundGround);
 
 		// Title
 		// cwaLine.getTitle( ).getLabel( ).getCaption( ).setValue( "Line Chart"
 		// );//$NON-NLS-1$
 		retValue.getTitle().setVisible(false);
 
 		// Legend
 		legend = retValue.getLegend();
 		final LineAttributes lia = legend.getOutline();
 		legend.getText().getFont().setSize(8);
 		lia.setStyle(LineStyle.SOLID_LITERAL);
 		legend.getInsets().set(10, 5, 0, 0);
 		legend.getOutline().setVisible(false);
 		legend.setAnchor(Anchor.NORTH_LITERAL);
 		legend.setPosition(Position.BELOW_LITERAL);
 		legend.getText().setColor(foreGround);
 		legend.getOutline().setColor(foreGround);
 		
 		// cwaLine.getLegend( ).getText().getFont().setSize(16);;
 		// cwaLine.getLegend( ).setVisible( true );
 
 		// /////////
 		// X-Axis
 		xAxisPrimary = retValue.getPrimaryBaseAxes()[0];
 		xAxisPrimary.setType(AxisType.TEXT_LITERAL);
 		xAxisPrimary.getMajorGrid().setTickStyle(TickStyle.BELOW_LITERAL);
 		xAxisPrimary.getOrigin().setType(IntersectionType.VALUE_LITERAL);
 		xAxisPrimary.getTitle().setVisible(false);
 		xAxisPrimary.getTitle().getCaption().getFont().setSize(9);
 		xAxisPrimary.getTitle().getCaption().setColor(foreGround);
 		xAxisPrimary.getLabel().getCaption().setColor(foreGround);
 
 		final Series xAxisSeries = SeriesImpl.create();
 
 		// new colors
 		xAxisSeries.getLabel().getCaption().setColor(foreGround);
 		xAxisSeries.getLabel().getOutline().setColor(foreGround);
 		//	
 
 		final NumberDataSet xValues = NumberDataSetImpl.create(cycleNumbers);
 		xAxisSeries.setDataSet(xValues);
 		final SeriesDefinition sdX = SeriesDefinitionImpl.create();
 
 		xAxisPrimary.getSeriesDefinitions().add(sdX);
 		sdX.getSeries().add(xAxisSeries);
 
 		// ////////
 		// Y-Axis
 		yAxisPrimary = retValue.getPrimaryOrthogonalAxis(xAxisPrimary);
 		yAxisPrimary.getMajorGrid().setTickStyle(TickStyle.LEFT_LITERAL);
 
 		// NumberDataSet[] orthoValues = new NumberDataSet[MAX_LINES];
 		// SeriesDefinition[] sdY = new SeriesDefinition[MAX_LINES];
 
 		// end Y-Series
 
 		xAxisPrimary.getScale().setMin(NumberDataElementImpl.create(0.0));
 		// xAxisPrimary.getScale( ).setMax( NumberDataElementImpl.create( 10.0 )
 		// );
 		// xAxisPrimary.getScale( ).setStep( 1 );
 		xAxisPrimary.getLabel().getCaption().getFont().setSize(9);
 		xAxisPrimary.getLabel().getCaption().setColor(foreGround);
 		xAxisPrimary.getTitle().setVisible(true);
 		xAxisPrimary.getTitle().getCaption().setValue(Messages.getString("TS.TIMELABEL"));
 
 		yAxisPrimary.getScale().setMin(NumberDataElementImpl.create(0.0));
 		yAxisPrimary.getScale().setMax(NumberDataElementImpl.create(1.0));
 		yAxisPrimary.getScale().setStep(0.25);
 		yAxisPrimary.getLabel().getCaption().getFont().setSize(9);
 		yAxisPrimary.getLabel().getCaption().setColor(foreGround);
 		yAxisPrimary.getTitle( ).getCaption( ).setValue( Ordinate_AXIS );
 		yAxisPrimary.getTitle( ).setVisible(true);
 		// yAxisPrimary.getMajorGrid().getLineAttributes().setColor(foreGround);
 		
 		// for now get ready to create only one line - we have no data yet.
 		// we will add more lines as we need them
 		// handle null
 		if(!dataSeriesMap.containsKey(defaultKey)) {
 			DataSeries series = new DataSeries(defaultKey, seriesCount, overlayMode);
 			seriesCount ++;
 			dataSeriesMap.put(defaultKey, series);
 		}
 
 		return retValue;
 	} // createSimpleLineChart
 
 	
 	/**
 	 * Create the view's context menu and add the action handlers to it.
 	 */
 	protected void createContextMenu(final Composite parent) {
 
 		// Init a Context Menu Manager
 		final MenuManager contextMenuManager = new MenuManager();
 
 		// ---------------------------------------------------------------------
 
 		linearTimeAction = new LinearScaleAction();
 		logTimeAction = new LogScaleAction();
 		contextMenuManager.add(linearTimeAction);
 		contextMenuManager.add(logTimeAction);
 
 
 		
 		// Place Holder for Menu Additions
 		contextMenuManager.add(new Separator(
 				IWorkbenchActionConstants.MB_ADDITIONS));
 
 		// ---------------------------------------------------------------------
 		
 		viewLegend = new LegendViewAction();
 		hideLegend = new LegendHideAction();
 		contextMenuManager.add(viewLegend);
 		contextMenuManager.add(hideLegend);
 		
 		// ---------------------------------------------------------------------
 		
 		
 		// Place Holder for Menu Additions
 		contextMenuManager.add(new Separator(
 				IWorkbenchActionConstants.MB_ADDITIONS));
 
 		popUpMenu = contextMenuManager.createContextMenu(parent);
 
 		// Set the context menu for the viewer
 		parent.setMenu(popUpMenu);
 
 	} // createContextMenu
 	
 	/**
 	 * Update the view's context menu and add the action handlers to it.
 	 */
 	private void updateContextMenu(final Composite parent) {
 
 		popUpMenu.dispose();
 
 		// Init a Context Menu Manager
 		final MenuManager contextMenuManager = new MenuManager();
 
 		// ---------------------------------------------------------------------
 
 		linearTimeAction = new LinearScaleAction();
 		logTimeAction = new LogScaleAction();
 		contextMenuManager.add(linearTimeAction);
 		contextMenuManager.add(logTimeAction);
 		
 		// ---------------------------------------------------------------------
 
 
 		// Place Holder for Menu Additions
 		contextMenuManager.add(new Separator(
 				IWorkbenchActionConstants.MB_ADDITIONS));
 
 		// ---------------------------------------------------------------------
 		
 		viewLegend = new LegendViewAction();
 		hideLegend = new LegendHideAction();
 		contextMenuManager.add(viewLegend);
 		contextMenuManager.add(hideLegend);
 		
 		// ---------------------------------------------------------------------
 		
 		
 		// Place Holder for Menu Additions
 		contextMenuManager.add(new Separator(
 				IWorkbenchActionConstants.MB_ADDITIONS));
 
 		
 		// ---------------------------------------------------------------------
 		// add the displayable properties
 		for (int i = 0; i < control.getNumProperties(chartIndex); i ++) {
 			
 			String nextProp = control.getProperty(chartIndex,i);
 			DataSeries series = dataSeriesMap.get(nextProp);
 			DisplayableProperty property = new DisplayableProperty(nextProp, series.isVisible());
 			contextMenuManager.add(property);
 		}
 
 		// ---------------------------------------------------------------------
 
 		// Place Holder for Menu Additions
 		contextMenuManager.add(new Separator(
 				IWorkbenchActionConstants.MB_ADDITIONS));
 
 		// ---------------------------------------------------------------------
 
 		final Menu popUpMenu = contextMenuManager.createContextMenu(parent);
 
 		// Set the context menu for the viewer
 		parent.setMenu(popUpMenu);
 
 
 	} // updateContextMenu
 
 
 	
 	/**
 	 * Sets the colors for a n array of LineSeries given the property to Plot
 	 * for each. Try to set color from the preferences (if specified for that
 	 * property) otherwise sets line color to blue.
 	 * 
 	 * @param propertiesToPlot
 	 * @param lsList
 	 */
 	public static void setColorDefs(
 			final List<String> propertiesToPlot,
 			final List<LineSeries> lsList) {
 		// the default line color
 
 		// if possible get color from preferences
 		final Map<String, Color> colorMap = VisualizationPreferencePage
 				.getColorMapping();
 		for (int i = 0; i < lsList.size(); i++) {
 			ColorDefinition color = ColorDefinitionImpl.BLUE();
 
 			if ((propertiesToPlot != null) && (propertiesToPlot.size() > i)) {
 				final String key = propertiesToPlot.get(i);
 				// look or the preference color by name
 				if (colorMap.containsKey(key)) {
 					final Color c = colorMap.get(key);
 					color = ColorDefinitionImpl.create(c.getRed(),
 							c.getGreen(), c.getBlue());
 				}
 				lsList.get(i).setSeriesIdentifier(key);
 				lsList.get(i).getLineAttributes().setColor(color);
 			} else {
 
 				if (lsList.get(i) != null) {
 					lsList.get(i).setSeriesIdentifier(" ");
 					lsList.get(i).getLineAttributes().setColor(color);
 				}
 			}
 		}
 		return;
 	}// getColorDef
 
 
 	
 
 	/**
 	 * Disposes the Color objects
 	 */
 	@Override
 	public void dispose() {
 		super.dispose();
 	}
 
 	/**
 	 * reset
 	 */
 	public void reset() {
 		resetData();
 		redraw();
 	}
 
 	protected void resetData() {
 		clearData();
 	
 		cycleNumbers.add(new Integer(0));
 	}
 
 	private void clearData() {
 		
 		Iterator<String> iter = dataSeriesMap.keySet().iterator();
 		while((iter!=null)&&(iter.hasNext())) {
 			String key = iter.next();
 			DataSeries series = dataSeriesMap.get(key);
 			series.relativeValues.clear();
 			series.addValue(new Double(0.0));
 		}
 		cycleNumbers.clear();
 	}
 
 	
 	/**
 	 * toggle the scale from logarithmic to linear
 	 */
 	void toggleAxisScale() {
 		if (useLinearTimeScale) { //Switch to logarithmic scale
 			logTimeAction.setChecked(true);
 			linearTimeAction.setChecked(false);
 			//Just using the following axis type, to move to log scale, didn't work
 			//yAxisPrimary.setType(AxisType.LOGARITHMIC_LITERAL);
 			yAxisPrimary.getScale().setMin(null);
 			yAxisPrimary.getScale().setMax(null);
 			yAxisPrimary.getScale().unsetStep();
 			yAxisPrimary.getScale().unsetStepNumber();
 		}
 		else { //Switch to linear scale
 			logTimeAction.setChecked(false);
 			linearTimeAction.setChecked(true);
 			//yAxisPrimary.setType(AxisType.LINEAR_LITERAL);
 			yAxisPrimary.getScale().setMin(NumberDataElementImpl.create(0.0));
 			yAxisPrimary.getScale().setMax(NumberDataElementImpl.create(1.0));
 			yAxisPrimary.getScale().setStep(0.25);
 		}
 		useLinearTimeScale = !useLinearTimeScale;
 		draw();
 	}
 	
 
 	/**
 	 * toggle the scale from logarithmic to linear
 	 */
 	void toggleLegend() {
 		if (showLegend) { //Switch to hide
 			viewLegend.setChecked(false);
 			hideLegend.setChecked(true);
 			legend.setVisible(false);
 		}
 		else { //Switch to view
 			viewLegend.setChecked(true);
 			hideLegend.setChecked(false);
 			legend.setVisible(true);
 		}
 		showLegend = !showLegend;
 		draw();
 	}
 
 
 	/**
 	 * Action to show the legend
 	 */
 	protected class LegendViewAction extends Action {
 		public LegendViewAction()
 		{
 			super(Messages.getString("ContextMenu.ShowLegend"), IAction.AS_CHECK_BOX);
 			setChecked(showLegend);
 		}
 
 		/**
 		 * @see org.eclipse.jface.action.Action#getText()
 		 */
 		@Override
 		public String getText() {
 			return Messages.getString("ContextMenu.ShowLegend");
 		}
 
 		/**
 		 * @see org.eclipse.jface.action.Action#run()
 		 */
 		@Override
 		public void run() {
 			if (showLegend) {
 				setChecked(true);
 				//Nothing to do. It's already linear-time.
 			}
 			else {
 				toggleLegend();
 			}
 			draw();
 		}
 	} //LegendViewAction
 	
 	/**
 	 * Action to hide the legend
 	 */
 	class LegendHideAction extends Action 	{
 		public LegendHideAction()
 		{
 			super(Messages.getString("ContextMenu.HideLegend"), IAction.AS_CHECK_BOX);
 			setChecked(!showLegend);
 		}
 
 		/**
 		 * @see org.eclipse.jface.action.Action#getText()
 		 */
 		@Override
 		public String getText() {
 			return Messages.getString("ContextMenu.HideLegend");
 		}
 
 		/**
 		 * @see org.eclipse.jface.action.Action#run()
 		 */
 		@Override
 		public void run() {
 			if (!showLegend) {
 				setChecked(true);
 				//Nothing to do. It's already log-time.
 			}
 			else {
 				toggleLegend();
 			}
 			draw();
 		}
 	}//LegendHideAction
 
 	
 	/**
 	 * switch to linear plot 
 	 * 
 	 */
 	protected class LinearScaleAction extends Action {
 		public LinearScaleAction()
 		{
 			super(Messages.getString("ContextMenu.LinearTimeScale"), IAction.AS_CHECK_BOX);
 			setChecked(useLinearTimeScale);
 		}
 
 		/**
 		 * @see org.eclipse.jface.action.Action#getText()
 		 */
 		@Override
 		public String getText() {
 			return Messages.getString("ContextMenu.LinearTimeScale");
 		}
 
 		/**
 		 * @see org.eclipse.jface.action.Action#run()
 		 */
 		@Override
 		public void run() {
 			if (useLinearTimeScale) {
 				setChecked(true);
 				//Nothing to do. It's already linear-time.
 			}
 			else {
 				toggleAxisScale();
 			}
 			draw();
 		}
 	} //LinearScaleAction
 
 	/**
 	 * switch to semi-log plot (log scale on y axis)
 	 * 
 	 */
 	class LogScaleAction extends Action 	{
 		public LogScaleAction()
 		{
 			super(Messages.getString("ContextMenu.LogTimeScale"), IAction.AS_CHECK_BOX);
 			setChecked(!useLinearTimeScale);
 		}
 
 		/**
 		 * @see org.eclipse.jface.action.Action#getText()
 		 */
 		@Override
 		public String getText() {
 			return Messages.getString("ContextMenu.LogTimeScale");
 		}
 
 		/**
 		 * @see org.eclipse.jface.action.Action#run()
 		 */
 		@Override
 		public void run() {
 			if (!useLinearTimeScale) {
 				setChecked(true);
 				//Nothing to do. It's already log-time.
 			}
 			else {
 				toggleAxisScale();
 			}
 			draw();
 		}
 	}//LogScaleAction
 	
 	/**
 	 * DisplayableProperty
 	 *
 	 */
 	protected class DisplayableProperty extends Action
 	{
 		String property = null;
 		public DisplayableProperty(String property, boolean visibility)
 		{
 					super(property, IAction.AS_CHECK_BOX);
 					this.property = property;
 					setChecked(visibility);
 		}
 		
 	
 
 		/**
 		 * @see org.eclipse.jface.action.Action#getText()
 		 */
 		@Override
 		public String getText() {
 			return property;
 		}
 
 		/**
 		 * Toggle the state
 		 * @see org.eclipse.jface.action.Action#run()
 		 */
 		@Override
 		public void run() {
 				DataSeries series = dataSeriesMap.get(property);
 				series.toggleVisible();
 				dataSeriesMap.put(property,series);
 				setChecked(series.isVisible());
 				draw();
 		}
 	}// DisplayableProperty
 	
 	
 	/**
 	 * Inner class DataSeries
 	 * all the data objects for a plot
 	 *
 	 */
 	protected class DataSeries 
 	{
 		public String propertyName = "";
 		
 		public List<Double> relativeValues = new ArrayList<Double>();
 		public Series lineSeries = null;
 		private boolean visible = true;
 		private SeriesDefinition sdY = null;
 		private int seriesIndex = 0;
 		private boolean overlayMode = false;
 		
 		
 		public boolean isVisible() {
 			return visible;
 		}
 		public void setVisible(boolean state) {
 			visible = state;
 		}
 		
 		public void toggleVisible() {
 			visible = !visible;
 		}
 
 		/**
 		 * 
 		 * @param propertyName
 		 * @param index
 		 * @param overlay 
 		 */
 		public DataSeries(String propertyName, int index, boolean overlay) {
 			this.propertyName = propertyName;
 			this.seriesIndex = index;
 			this.overlayMode = overlay;
 			relativeValues = new ArrayList<Double>();
 			relativeValues.add(new Double(0.0));
 			addLineSeries(propertyName);
 		}
 		
 		public void addValue(Double val) {
 			if(relativeValues==null) relativeValues = new ArrayList<Double>();
 			relativeValues.add(val);
 		}
 		
 		/**
 		 * @param propertyName
 		 * 
 		 */
 		@SuppressWarnings("deprecation")
 		public void addLineSeries(final String propertyName) {
 			
 			final NumberDataSet orthoValues = NumberDataSetImpl
 					.create(relativeValues);
 			if(lineSeries == null) {
 				lineSeries = ScatterSeriesImpl.create();
 				/*
 				 * 
 				 * Bar chart style is too slow for generation of a fill
 				 * if we want a fill we can try using the difference plot
 				 * to a straight line at y=0
 				 * 
 				
 				if(propertyName.indexOf("*")>=1) {
 					// barseries methods
 					lineSeries = (BarSeries) BarSeriesImpl.create();
 				} else {
 					lineSeries = (ScatterSeries) ScatterSeriesImpl.create();
 				}
 				
 				*
 				*/
 			} // if lineSeries==null
 			lineSeries.setDataSet(orthoValues);
 			((LineSeries) lineSeries).getLineAttributes().setVisible(true);
			((LineSeries) lineSeries).getMarker().setVisible(false);
 			
 			// Assign the line color
 			// based on selected property. Default is Blue
 			setColorDefs(propertyName);
 			// If this is the "selected" region of a graph set the marker type
 		
 			// the series def
 			sdY = SeriesDefinitionImpl.create();
 			sdY.getSeriesPalette().update(-2);
 			sdY.getSeries().add(lineSeries);
 			yAxisPrimary.getSeriesDefinitions().add(sdY);
 			
 			return;
 		}// addLineSeries
 		
 		/** 
 		 * in response to user action temporarily remove the line series from the graph
 		 */
 		public void hide() {
 			lineSeries.setVisible(false);
 			visible = false;
 		}// hide
 		
 		/** 
 		 * in response to user action add back the line series to the graph
 		 */
 		public void show() {
 			lineSeries.setVisible(true);
 			visible = true;
 		}// show
 		
 
 		/**
 		 * Sets the colors for a n array of LineSeries given the property to Plot
 		 * for each. Try to set color from the preferences (if specified for that
 		 * property) otherwise sets line color to blue.
 		 * @param propertyName 
 		 *
 		 */
 		public void setColorDefs(String propertyName) {
 			// the default line color
 
 			// if possible get color from preferences
 			final Map<String, Color> colorMap = VisualizationPreferencePage.getColorMapping();
 
 			// default color
 			int colorIndex = seriesIndex % colorDefault.length;
 			ColorDefinition color = colorDefault[colorIndex];
 
 			// preferred color by name
 			String key = propertyName.substring(0,1);
 			
 			if (colorMap.containsKey(key)) {
 				final Color c = colorMap.get(key);
 				color = ColorDefinitionImpl.create(c.getRed(), c.getGreen(), c.getBlue());
 			}
 			
 			this.lineSeries.setSeriesIdentifier(propertyName);
 			if (overlayMode) {
 				if(propertyName.indexOf("*")>=1) {
 					// barseries methods
 					((LineSeries)this.lineSeries).getLineAttributes().setColor(color);
 				} else {
 					((LineSeries)this.lineSeries).getLineAttributes().setColor(ColorDefinitionImpl.GREY());
 					((LineSeries)this.lineSeries).getLineAttributes().setStyle(LineStyle.DOTTED_LITERAL);
 				}
 			} else {
 				((LineSeries)this.lineSeries).getLineAttributes().setColor(color);
 			}
 			
 			
 		}// getColorDefs
 
 		/**
 		 * length of the series
 		 * @return number of data points
 		 */
 		public int getDataSize() {
 			return relativeValues.size();
 		}
 		// Accessors
 		public String getPropertyName() {
 			return propertyName;
 		}
 
 		public void setPropertyName(String propertyName) {
 			this.propertyName = propertyName;
 		}
 
 		public List<Double> getRelativeValues() {
 			return relativeValues;
 		}
 
 		public void setRelativeValues(List<Double> relativeValues) {
 			this.relativeValues = relativeValues;
 		}
 
 		
 
 		public void setLineSeries(LineSeries lineSeries) {
 			this.lineSeries = lineSeries;
 		}
 	}// DataSeries
 	////////////////////////////////////////////////////////
 	
 } // TimeSeriesCanvas
