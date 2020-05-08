 package org.eclipse.stem.ui.reports.views;
 
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
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
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
 import org.eclipse.birt.chart.model.attribute.Marker;
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
 import org.eclipse.birt.chart.model.type.ScatterSeries;
 import org.eclipse.birt.chart.model.type.impl.ScatterSeriesImpl;
 import org.eclipse.birt.chart.util.PluginSettings;
 import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
 import org.eclipse.emf.edit.provider.ItemPropertyDescriptor;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.stem.core.model.Decorator;
 import org.eclipse.stem.core.model.STEMTime;
 import org.eclipse.stem.definitions.adapters.relativevalue.history.RelativeValueHistoryProvider;
 import org.eclipse.stem.definitions.adapters.relativevalue.history.RelativeValueHistoryProviderAdapter;
 import org.eclipse.stem.jobs.simulation.ISimulation;
 import org.eclipse.stem.ui.preferences.VisualizationPreferencePage;
 import org.eclipse.stem.ui.reports.Activator;
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
 	private RelativeValueHistoryProvider rvhp;
 
 	private ReportControl control = null;
 	
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
 	 * used to index the line series so we can step through default colors
 	 * when a user custom color is not yet assigned
 	 */
 	protected int seriesCount  = 0;
 
 	/**
 	 * Log of zero is negative infinity so for each location we will cut off the minimum value
 	 * of log(y) at 0.1/POPULATION for display purposes only
 	 */
 	private double minLogScaleValue = 1.0;
 	
 	/**
 	 * once time > DEFAULT_AUTOAXIS_THRESHOLD we start to autoreset the time axis 
 	 * tick marks and scale so we don't have too many tick marks
 	 */
 	private static final int DEFAULT_AUTOAXIS_THRESHOLD = 5;
 	
 	/**
 	 * the max value for the time (x) axis
 	 */
 	private int maxTimeAxisValue = DEFAULT_AUTOAXIS_THRESHOLD;
 	
 	/**
 	 * the minimum number of time axis ticks 
 	 */
 	private static final int MIN_TICKS = 5;
 	
 	/**
 	 * the maximum number of time axis ticks  after time=DEFAULT_AUTOAXIS_THRESHOLD;
 	 */
 	private static final int MAX_TICKS = 10;
 	
 	/**
 	 * used to set the autoaxis tick
 	 */
 	private static final int TICK_TRIGGER = MAX_TICKS/MIN_TICKS;
 
 	/**
 	 * This is a map of properties of the label updated by the selected
 	 * {@link Decorator} whose relative value might be plotted (context menu allows us to toggle the individual
 	 * properties to plot
 	 */
 	protected Map<ItemPropertyDescriptor,Boolean> propertiesToPlot = new HashMap<ItemPropertyDescriptor,Boolean>();
 
 	/**
 	 * TODO this is temporary code and should be in user preferences
 	 * we want to hide a few of the possible line-series on start up
 	 */
 	private static final String[] HIDE_ON_STARTUP = {"IR","IF","Incidence","Births","Deaths"};
 	private static final Set<String> defaultHideSet = new HashSet<String>();
 
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
 	
 	private static final String defaultKey = "S";
 
 	Image imgChart = null;
 	
 	/**
 	 * Constructor.
 	 *
 	 * @param parent
 	 *            the SWT parent of the {@link Widget}
 	 */
 	public TimeSeriesCanvas(final Composite parent) {
 		super(parent, SWT.DOUBLE_BUFFERED | SWT.BORDER);
 		
 		// init
 		for (int i = 0; i < HIDE_ON_STARTUP.length; i ++) {
 			defaultHideSet.add(HIDE_ON_STARTUP[i]);
 		}
 	
 
 		gr = Generator.instance();
 
 		try {
 			idr = PluginSettings.instance().getDevice("dv.SWT"); //$NON-NLS-1$
 		} catch (final ChartException pex) {
 			Activator.logError("Problem initializing chart", pex); //$NON-NLS-1$
 			return;
 		}
 
 		control = (ReportControl) parent;
 		rvhp = control.rvhp;
 		
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
 	 *
 	 */
 	public void draw() {
 
 		// Has a relative value provider been provided?
 		if (rvhp != null) {
 			// Yes
 			// build up the set of properties to plot
 			Iterator<ItemPropertyDescriptor> iter = propertiesToPlot.keySet().iterator();
 			List<ItemPropertyDescriptor> displayedPropertyList = new ArrayList<ItemPropertyDescriptor>();
 			while((iter!=null)&&(iter.hasNext()) ){
 				ItemPropertyDescriptor property = iter.next();
 				boolean visible = propertiesToPlot.get(property).booleanValue();
 				displayedPropertyList.add(property);
 				if(visible) {
 					if(dataSeriesMap.containsKey(property.getDisplayName(property))) {
 						DataSeries series = dataSeriesMap.get(property.getDisplayName(property));
 						// remove it
 						if(!series.isVisible()) {
 							series.show();
 						}
 					}
 				} else {
 					if(dataSeriesMap.containsKey(property.getDisplayName(property))) {
 						DataSeries series = dataSeriesMap.get(property.getDisplayName(property));
 						// remove it
 						if(series.isVisible()) {
 							series.hide();
 						}
 					}
 				}// visible?
 			}// all displayedProperties
 
 
 			/////////////////////////////////////////////////////////////
 			// Log(0.0) is negative infinity so for display purposes only
 			// we set the minimum axis value at 0.1/POPULATION
 			double denom = rvhp.getDenominator();
 			if(denom <=0.0) denom = 1.0;
 			minLogScaleValue = 0.1/denom;
 			/////////////////////////////////////////////////////////////
 
 			// clear
 			resetData();
 			
 			
 			
 			boolean setCycles = false;
 			// Get the values for the property to be plotted
 			int maxPoints = 0;
 			STEMTime[] time = rvhp.getAllHistoricTimeValues();
 			for (int i = 0; i < displayedPropertyList.size(); i++) {
 				ItemPropertyDescriptor property = displayedPropertyList.get(i);
 				String propertyName = property.getDisplayName(property);
 				
 				final double[] doubleValues = rvhp.getHistoricInstances(property,time);
 
 
 				// Any values?
 				if (doubleValues.length > 0) {
 					if (maxPoints < doubleValues.length) {
 						maxPoints = doubleValues.length;
 					}
 
 					// this might be a new display
 					// so we have to fill any empty data set
 					if (!dataSeriesMap.containsKey(property.getDisplayName(property))) {
 						
 						DataSeries series = new DataSeries(propertyName, seriesCount);
 						// we are out of data (properties) so we need to fill in
 						// zeros for any unused lines
 						// fill in the empty arrays
 						if(doubleValues.length < maxPoints) {
 							for(int ii = doubleValues.length; ii < maxPoints-1; ii ++) {
 								// pad with zeros
 								series.addValue(new Double(0.0));
 							}
 						}
 						
 						
 						
 						dataSeriesMap.put(propertyName, series);
 					}
 
 					final int earliestCycleNumber = rvhp.getEarliestCycleNumber();
 					DataSeries series = dataSeriesMap.get(property.getDisplayName(property));
 					series.setColorDefs(property.getDisplayName(property));
 					
 					for (int cycleNumber = 0; cycleNumber < doubleValues.length; cycleNumber++) {
 						Double value;
 						double displayValue = doubleValues[cycleNumber];
 						if (displayValue <= minLogScaleValue) {
 							// Log(0.0) is negative infinity so for display purposes only
 							// we set the minimum axis value at 0.1/POPULATION
 							displayValue = minLogScaleValue;
 						}
 						if (useLinearTimeScale) {
 							value = new Double(displayValue);
 						} else {
 							value = new Double(Math.log(displayValue));
 						}
 						
 					
 						series.addValue(value);
 						
 						if (!setCycles) {
 							this.cycleNumbers.add(new Integer(
									earliestCycleNumber + cycleNumber));
 							
 							/*
 							 * We don't want to add x (time) axis ticks ad infinitum so we we need to dynamically
 							 * change the scale. This will toggle between 5-10 ticks every time the max time doubles
 							 * past an initial value of maxTimeAxisValue = DEFAULT_AUTOAXIS_THRESHOLD (= 25)
 							 */
 							if(this.cycleNumbers.size()>TICK_TRIGGER*maxTimeAxisValue) {
 								maxTimeAxisValue = this.cycleNumbers.size()+1 ;
 								int stepX = maxTimeAxisValue / MIN_TICKS;
 								xAxisPrimary.getScale().setStep(stepX);
 							}
 						}
 						
 						
 					} // for cycleNumber
 					//System.out.println("  "+series.propertyName+"cycles = "+this.cycleNumbers.size()+" datasize = "+series.getDataSize());
 					setCycles = true; // we set them only once
 				} else {
 					resetData();
 				}
 
 				
 			} // for i properties
 
 		} // if a relative value provider has been provided
 		else {
 			// No
 			// Need to clear everything
 			resetData();
 		}
 
 		if (!this.isDisposed()) {
 			redraw();
 		}
 	} // paintControl
 
 	/**
 	 * Create the view's context menu and add the action handlers to it.
 	 */
 	private void createContextMenu(final Composite parent) {
 
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
 		if(rvhp!= null) {
 					List<IItemPropertyDescriptor>  properties = rvhp.getProperties();
 					for(int i = 0; i < properties.size(); i ++) {
 						ItemPropertyDescriptor nextProp = (ItemPropertyDescriptor)properties.get(i);
 						DisplayableProperty property = new DisplayableProperty(nextProp);
 						contextMenuManager.add(property);
 					}// for all properties
 				}// rvhp not null
 		
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
 	 * @param dataSeriesMap the {@link Map} that will contain the relative values
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
 
 		final ColorDefinition foreGround = ColorDefinitionImpl.WHITE();
 		final ColorDefinition backgroundGround = ColorDefinitionImpl.BLACK();
 		final ColorDefinition frameColor = ColorDefinitionImpl.create(50, 50,
 				50);
 
 		
 		
 
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
 
 		// /////////
 		// X-Axis
 		xAxisPrimary = retValue.getPrimaryBaseAxes()[0];
 		xAxisPrimary.getMajorGrid().setTickStyle(TickStyle.BELOW_LITERAL);
 		xAxisPrimary.getOrigin().setType(IntersectionType.VALUE_LITERAL);
 		xAxisPrimary.getTitle().setVisible(false);
 		xAxisPrimary.getTitle().getCaption().getFont().setSize(9);
 		xAxisPrimary.getTitle().getCaption().setColor(foreGround);
 		xAxisPrimary.getLabel().getCaption().setColor(foreGround);
 		xAxisPrimary.setType(AxisType.LINEAR_LITERAL);
 
 		final Series seCategory = SeriesImpl.create();
 
 		// new colors
 		seCategory.getLabel().getCaption().setColor(foreGround);
 		seCategory.getLabel().getOutline().setColor(foreGround);
 		//
 
 		final NumberDataSet categoryValues = NumberDataSetImpl
 				.create(cycleNumbers);
 		seCategory.setDataSet(categoryValues);
 		final SeriesDefinition sdX = SeriesDefinitionImpl.create();
 
 		xAxisPrimary.getSeriesDefinitions().add(sdX);
 		sdX.getSeries().add(seCategory);
 
 		// ////////
 		// Y-Axis
 		this.yAxisPrimary = retValue.getPrimaryOrthogonalAxis(xAxisPrimary);
 		yAxisPrimary.getMajorGrid().setTickStyle(TickStyle.LEFT_LITERAL);
 		// end Y-Series
 
 		xAxisPrimary.getScale().setMin(NumberDataElementImpl.create(0.0));
 		xAxisPrimary.getScale().setStep(1);
 		
 		//xAxisPrimary.getScale().setMax(NumberDataElementImpl.create(TICK_TRIGGER*DEFAULT_AUTOAXIS_THRESHOLD));
 		//int numTicks = TICK_TRIGGER*DEFAULT_AUTOAXIS_THRESHOLD/MIN_TICKS;
 		//
 		
 		
 		
 		xAxisPrimary.getLabel().getCaption().getFont().setSize(9);
 		xAxisPrimary.getLabel().getCaption().setColor(foreGround);
 		setTimeLabel();
 
 		yAxisPrimary.getScale().setMin(NumberDataElementImpl.create(0.0));
 		yAxisPrimary.getScale().setMax(NumberDataElementImpl.create(1.0));
 		yAxisPrimary.getScale().setStep(0.25);
 		yAxisPrimary.getLabel().getCaption().getFont().setSize(9);
 		yAxisPrimary.getLabel().getCaption().setColor(foreGround);
 		// yAxisPrimary.getMajorGrid().getLineAttributes().setColor(foreGround);
 		
 		// for now get ready to create only one line - we have no data yet.
 		// we will add more lines as we need them
 		// handle null
 		if(!dataSeriesMap.containsKey(defaultKey)) {
 			DataSeries series = new DataSeries(defaultKey, seriesCount);
 			dataSeriesMap.put(defaultKey, series);
 		}
 
 		return retValue;
 	} // createSimpleLineChart
 
 	void setTimeLabel() {
 		String val = "";
 		if (control != null) {
 			final ISimulation sim = control.simulation;
 			if (sim != null) {
 				final long timeDelta = sim.getScenario().getSequencer()
 						.getTimeDelta();
 
 				if (timeDelta < STEMTime.Units.MINUTE.getMilliseconds()) {
 					val = "secs";
 					xAxisPrimary.getTitle().setVisible(true);
 					xAxisPrimary.getTitle().getCaption().setValue(val);
 					return;
 				} else if (timeDelta < STEMTime.Units.HOUR.getMilliseconds()) {
 					val = "mins";
 					xAxisPrimary.getTitle().setVisible(true);
 					xAxisPrimary.getTitle().getCaption().setValue(val);
 					return;
 				} else if (timeDelta < STEMTime.Units.DAY.getMilliseconds()) {
 					val = "hrs";
 					xAxisPrimary.getTitle().setVisible(true);
 					xAxisPrimary.getTitle().getCaption().setValue(val);
 					return;
 
 				} else if (timeDelta <= STEMTime.Units.WEEK.getMilliseconds()) {
 					val = "days";
 					xAxisPrimary.getTitle().setVisible(true);
 					xAxisPrimary.getTitle().getCaption().setValue(val);
 					return;
 
 				} else if (timeDelta < 4 * STEMTime.Units.WEEK
 						.getMilliseconds()) {
 					val = "weeks";
 					xAxisPrimary.getTitle().setVisible(true);
 					xAxisPrimary.getTitle().getCaption().setValue(val);
 					return;
 				} else if (timeDelta < STEMTime.Units.YEAR.getMilliseconds()) {
 					val = "months";
 					xAxisPrimary.getTitle().setVisible(true);
 					xAxisPrimary.getTitle().getCaption().setValue(val);
 					return;
 				} else {
 					val = "years";
 					xAxisPrimary.getTitle().setVisible(true);
 					xAxisPrimary.getTitle().getCaption().setValue(val);
 					return;
 				}
 
 			} // sim ! null
 		}// control ! null
 
 	} // setTimeLabel()
 
 	
 
 	/**
 	 * @param rvhp
 	 * @param propertiesToPlotList
 	 */
 	public void setDataSourceAndRedraw(
 			final RelativeValueHistoryProviderAdapter rvhp,
 			final List<ItemPropertyDescriptor> propertiesToPlotList) {
 
 		this.propertiesToPlot = addProperties(propertiesToPlotList);
 		if(rvhp!=this.rvhp) {
 			propertiesToPlot.clear();
 			this.propertiesToPlot = addProperties(propertiesToPlotList);
 			this.rvhp= rvhp;
 			//this.propertiesToPlot = filterPreferredProperties(propertiesToPlot);
 			updateContextMenu(this);
 		}
 
 		draw();
 	} // setDataSourceAndRedraw
 
 	/**
 	 * Filters a list of ItemPropertyDescriptors to show preferred properties if
 	 * at least one exists. If not, just returns the entire list.
 	 *
 	 * @return filteredList<ItemPropertyDescriptor>
 	 */
 	protected List<ItemPropertyDescriptor> filterPreferredProperties(
 			final List<ItemPropertyDescriptor> fullList) {
 		final List<ItemPropertyDescriptor> propertyList = new ArrayList<ItemPropertyDescriptor>();
 		// Are any of the labels in the preference set?
 		final Set<String> prefSet = VisualizationPreferencePage
 				.getPreferenceSet();
 
 		for (int i = 0; i < fullList.size(); i++) {
 			final ItemPropertyDescriptor property = fullList.get(i);
 			if (prefSet.contains(property.getDisplayName(property))) {
 				propertyList.add(property);
 			}
 		}
 		// if we didn't find any in the preferences then just add them all
 		if (propertyList.size() == 0) {
 			propertyList.addAll(fullList);
 		}
 		return propertyList;
 	}
 
 
 	/**
 	 * Filters a list of ItemPropertyDescriptors to show preferred properties if
 	 * at least one exists. If not, just returns the entire list.
 	 *
 	 * @return filteredList<ItemPropertyDescriptor>
 	 */
 	protected Map<ItemPropertyDescriptor,Boolean> addProperties(final List<ItemPropertyDescriptor> propertiesToPlotList) {
 
 		
 
 		for (int i = 0; i < propertiesToPlotList.size(); i++) {
 			final ItemPropertyDescriptor property = propertiesToPlotList.get(i);
 			if(!propertiesToPlot.containsKey(property)) {
 				boolean visible = true;
 				if(defaultHideSet.contains(property.getDisplayName(property))) visible = false;
 				Boolean bObj = new Boolean(visible); //default visibility
 				this.propertiesToPlot.put(property,bObj);
 			}
 		}
 
 		return this.propertiesToPlot;
 	}
 
 
 	/**
 	 * Set the provider of relative values. It will be consulted to produce a
 	 * sequence of relative values (0.0 to 1.0) that will be plotted.
 	 *
 	 * @param rvhp
 	 *            the provider of relative values.
 	 */
 	public void setRelativeValueHistoryProvider(
 			final RelativeValueHistoryProvider rvhp) {
 		this.rvhp = rvhp;
 	} // setRelativeValueHistoryProvider
 
 
 
 
 
 
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
 		rvhp = null;
 		redraw();
 	}
 
 	protected void resetData() {
 		clearData();
 		// for now get ready to create only one line - we have no data yet.
 		// we will add more lines as we need them
 		
 		if(!dataSeriesMap.containsKey(defaultKey)) {
 			DataSeries series = new DataSeries(defaultKey, seriesCount);
 			
 			dataSeriesMap.put(defaultKey, series);
 		} 
 		// handle null
 		
 		cycleNumbers.add(new Integer(0));
 	}
 
 	private void clearData() {
 		
 		Iterator<String> iter = dataSeriesMap.keySet().iterator();
 		while((iter!=null)&&(iter.hasNext())) {
 			String key = iter.next();
 			DataSeries series = dataSeriesMap.get(key);
 			series.relativeValues.clear();
 			series.addValue(new Double(0.0));
 			xAxisPrimary.getScale().setMin(NumberDataElementImpl.create(0.0));
 			maxTimeAxisValue = DEFAULT_AUTOAXIS_THRESHOLD;
 			//xAxisPrimary.getScale().setMax(NumberDataElementImpl.create(TICK_TRIGGER*DEFAULT_AUTOAXIS_THRESHOLD));
 			//int numTicks = TICK_TRIGGER*DEFAULT_AUTOAXIS_THRESHOLD/MIN_TICKS;
 			xAxisPrimary.getScale().setStep(1);
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
 	}
 
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
 		}
 	}//LogScaleAction
 
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
 		}
 	}//LegendHideAction
 
 
 	/**
 	 * DisplayableProperty
 	 *
 	 */
 	protected class DisplayableProperty extends Action
 	{
 		ItemPropertyDescriptor property = null;
 		public DisplayableProperty(ItemPropertyDescriptor property)
 		{
 					super(property.getDisplayName(property), IAction.AS_CHECK_BOX);
 					this.property = property;
 					setChecked(propertiesToPlot.get(property).booleanValue());
 		}
 
 		/**
 		 * @see org.eclipse.jface.action.Action#getText()
 		 */
 		@Override
 		public String getText() {
 			return property.getDisplayName(property);
 		}
 
 		/**
 		 * Toggle the state
 		 * @see org.eclipse.jface.action.Action#run()
 		 */
 		@Override
 		public void run() {
 			    boolean state = !propertiesToPlot.get(property).booleanValue();
 			    propertiesToPlot.put(property, new Boolean(state));
 				setChecked(state);
 		}
 	}// DisplayableProperty
 	
 
 
 	protected class DataSeries 
 	{
 		public String propertyName = "";
 		
 
 		public List<Double> relativeValues = new ArrayList<Double>();
 		public LineSeries lineSeries = null;
 		private boolean visible = true;
 		private SeriesDefinition sdY = null;
 		
 		private int seriesIndex = 0;
 		
 		
 		public boolean isVisible() {
 			return visible;
 		}
 
 		/**
 		 * 
 		 * @param propertyName
 		 * @param index
 		 */
 		public DataSeries(String propertyName, int index) {
 			this.propertyName = propertyName;
 			this.seriesIndex = index;
 			seriesCount ++;
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
 		public void addLineSeries(final String propertyName) {
 			final NumberDataSet orthoValues = NumberDataSetImpl
 					.create(relativeValues);
 			if(lineSeries == null) lineSeries = (ScatterSeries) ScatterSeriesImpl.create();
 			lineSeries.setDataSet(orthoValues);
 
 			lineSeries.getLineAttributes().setVisible(true);
 			
 			// Assign the line color
 			// based on selected property. Default is Blue
 			setColorDefs(propertyName);
 			
 			// replaces deprecated code: lineSeries.getMarker().setVisible(false);
 			if (!lineSeries.getMarkers().isEmpty()) {
 				Marker marker = (Marker)lineSeries.getMarkers().get(0);
 				marker.setVisible(false);
 			}
 			
 			// the series def
 			sdY = SeriesDefinitionImpl.create();
 			//sdY.getSeriesPalette().update(-2);
 			sdY.getSeries().add(lineSeries);
 			yAxisPrimary.getSeriesDefinitions().add(sdY);
 			setTimeLabel();
 			return;
 		}
 		
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
 			// pick a default color
 			int colorIndex = seriesIndex % colorDefault.length;
 			ColorDefinition color = colorDefault[colorIndex];
 
 			// preferred color by name
 			if (colorMap.containsKey(propertyName)) {
 				final Color c = colorMap.get(propertyName);
 				color = ColorDefinitionImpl.create(c.getRed(), c.getGreen(), c.getBlue());
 			}
 			this.lineSeries.setSeriesIdentifier(propertyName);
 			this.lineSeries.getLineAttributes().setColor(color);
 			return;
 		}// getColorDef
 
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
 
 		public LineSeries getLineSeries() {
 			return lineSeries;
 		}
 
 		public void setLineSeries(LineSeries lineSeries) {
 			this.lineSeries = lineSeries;
 		}
 
 	
 	}// DataSeries
 	
 } // TimeSeriesCanvas
