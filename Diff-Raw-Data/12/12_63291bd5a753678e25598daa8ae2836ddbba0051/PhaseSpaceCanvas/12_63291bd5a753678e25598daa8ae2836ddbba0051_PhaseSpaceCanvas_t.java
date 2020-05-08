 package org.eclipse.stem.ui.reports.views;
 
 /*******************************************************************************
  * Copyright (c) 2006 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.birt.chart.device.IDeviceRenderer;
 import org.eclipse.birt.chart.exception.ChartException;
 import org.eclipse.birt.chart.factory.Generator;
 import org.eclipse.birt.chart.model.Chart;
 import org.eclipse.birt.chart.model.ChartWithAxes;
 import org.eclipse.birt.chart.model.attribute.AxisType;
 import org.eclipse.birt.chart.model.attribute.Bounds;
 import org.eclipse.birt.chart.model.attribute.IntersectionType;
import org.eclipse.birt.chart.model.attribute.Marker;
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
 import org.eclipse.birt.chart.model.type.ScatterSeries;
 import org.eclipse.birt.chart.model.type.impl.ScatterSeriesImpl;
 import org.eclipse.birt.chart.util.PluginSettings;
 import org.eclipse.emf.edit.provider.ItemPropertyDescriptor;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.stem.core.model.STEMTime;
 import org.eclipse.stem.definitions.adapters.relativevalue.history.RelativeValueHistoryProvider;
 import org.eclipse.stem.definitions.adapters.relativevalue.history.RelativeValueHistoryProviderAdapter;
 import org.eclipse.stem.ui.reports.Activator;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.PaintEvent;
 import org.eclipse.swt.events.PaintListener;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Canvas;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.ui.IWorkbenchActionConstants;
 
 /**
  * TimeSeriesCanvas is a subclass of Canvas suitable for chart drawings.
  */
 public class PhaseSpaceCanvas extends Canvas {
 
 	protected IDeviceRenderer idr = null;
 
 	/**
 	 * This is the <code>Chart</code> that plots the relative values.
 	 */
 	protected Chart cm = null;
 
 	/**
 	 * The provider of relative values.
 	 */
 	private RelativeValueHistoryProvider rvhp;
 
 	/**
 	 * This is the property of the label updated by the selected decorator whose
 	 * relative value will be plotted along X
 	 */
 	private ItemPropertyDescriptor propertyToPlotX;
 
 	/**
 	 * This is the property of the label updated by the selected decorator whose
 	 * relative value will be plotted along Y
 	 */
 	private ItemPropertyDescriptor propertyToPlotY;
 
 	/**
 	 * These are the values that will be plotted in X
 	 * 
 	 */
 	private final List<Double> relativeValuesX = new ArrayList<Double>();
 
 	/**
 	 * These are the values that will be plotted in Y
 	 * 
 	 */
 	private final List<Double> relativeValuesY = new ArrayList<Double>();
 	
 	/**
 	 * Log of zero is negative infinity so for each location we will cut off the minimum value
 	 * of log(y) or log(x) at 0.5/POPULATION for display purposes only
 	 */
 	private double minLogScaleValue = 1.0;
 
 
 	/**
 	 * Chart generator instance (reference to a singleton)
 	 */
 	Generator gr;
 	
 	/**
 	 * The xAxis in the Phase Space Plot
 	 */
 	private Axis xAxisPrimary;
 	
 	/**
 	 * The yAxis in the Phase Space Plot
 	 */
 	private Axis yAxisPrimary;
 	
 	/**
 	 * default y axis label. This value is read from
 	 * messages.properties (NLS)
 	 */
 	private static String defaultAxisLabel_Y="Y Axis";
 	
 	/**
 	 * default x axis label. This value is read from
 	 * messages.properties (NLS)
 	 */
 	private static String defaultAxisLabel_X="X Axis";
 	
 	boolean useLinearScales = true;
 
 	private LinearScalesAction linearScalesAction;
 
 	private LogarithmicScalesAction logTimeScalesAction;
 
 	private String xAxisLabel;
 
 	private String yAxisLabel;
 	
 	Image imgChart = null;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param parent
 	 *            the SWT parent of the Widget
 	 */
 	public PhaseSpaceCanvas(final Composite parent) {
 		super(parent, SWT.DOUBLE_BUFFERED | SWT.BORDER);
 
 		gr = Generator.instance();
 
 		try {
 			idr = PluginSettings.instance().getDevice("dv.SWT"); //$NON-NLS-1$			
 		} catch (final ChartException pex) {
 			Activator.logError("Problem initializing chart", pex); //$NON-NLS-1$
 			return;
 		}
 
 		resetData();
 		cm = createPhaseSpaceLineChart(relativeValuesX, relativeValuesY,
 				Messages.getString("PH.title")); //$NON-NLS-1$
 
 		
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
 	 * The method which gets the TimeSeriesCanvas' reports list, and draws it on
 	 * the TimeSeriesCanvas.
 	 * 
 	 */
 	public void draw() {
 		// Has a relative value provider been provided?
 		if (rvhp != null) {
 			// Yes
 			
 			/////////////////////////////////////////////////////////////
 			// Log(0.0) is negative infinity so for display purposes only
 			// we set the minimum axis value at 0.5/POPULATION
 			double denom = rvhp.getDenominator(null);
 			if(denom <=0.0) denom = 1.0;
 			minLogScaleValue = 0.5/denom;
 			/////////////////////////////////////////////////////////////
 			
 			// time
 			STEMTime[] time = rvhp.getAllHistoricTimeValues();
 			// Get the values for the property to be plotted
 			final double[] relativeValuesX = rvhp
 					.getHistoricInstances(propertyToPlotX,time);
 			final double[] relativeValuesY = rvhp
 					.getHistoricInstances(propertyToPlotY,time);
 			// Any values?
 			if ((relativeValuesX.length > 0) && ((relativeValuesY.length > 0))) {
 				// Yes
 				clearData();
 				for (int cycleNumber = 0; cycleNumber < relativeValuesX.length; cycleNumber++) {
 					Double valueX = null, valueY = null;
 					double displayValueX = relativeValuesX[cycleNumber];
 					double displayValueY = relativeValuesY[cycleNumber];
 					
 					if (displayValueX <= minLogScaleValue) {
 						// Log(0.0) is negative infinity so for display purposes only
 						// we set the minimum axis value at 0.5/POPULATION
 						displayValueX = minLogScaleValue;
 					}
 					
 					if (displayValueY <= minLogScaleValue) {
 						// Log(0.0) is negative infinity so for display purposes only
 						// we set the minimum axis value at 0.5/POPULATION
 						displayValueY = minLogScaleValue;
 					}
 					
 					
 					if (useLinearScales) {
 						valueX = new Double(displayValueX);
 						valueY = new Double(displayValueY);
 					}
 					else { 
 							valueX = new Double(Math.log(displayValueX));
 							valueY = new Double(Math.log(displayValueY));
 					}
 					this.relativeValuesX.add(valueX);
 					this.relativeValuesY.add(valueY);
 				} // for
 			} // if
 			else {
 				resetData();
 			}
 			if (!this.isDisposed()) redraw();
 		} // if a relative value provider has been provided
 
 	} // paintControl
 	
 	/**
 	 * Create the view's context menu and add the action handlers to it.
 	 */
 	private void createContextMenu(final Composite parent) {
 
 		// Context Menu
 		final MenuManager contextMenuManager = new MenuManager();
 
 		// ---------------------------------------------------------------------
 
 		linearScalesAction = new LinearScalesAction();
 		logTimeScalesAction = new LogarithmicScalesAction();
 		contextMenuManager.add(linearScalesAction);
 		contextMenuManager.add(logTimeScalesAction);
 		
 
 		// Place Holder for Menu Additions
 		contextMenuManager.add(new Separator(
 				IWorkbenchActionConstants.MB_ADDITIONS));
 
 		// ---------------------------------------------------------------------
 
 		final Menu popUpMenu = contextMenuManager.createContextMenu(parent);
 
 		// Set the context menu for the viewer
 		parent.setMenu(popUpMenu);
 
 	} // createContextMenu
 
 	/**
 	 * @param relativeValuesX
 	 *            the <code>List</code> that will contain the relative X axis
 	 *            values (0.0-1.0) to plot
 	 * @param relativeValuesY
 	 *            that will contain the relative Y axis values (0.0-1.0) to plot
 	 * @param seriesIdentifier
 	 *            the title of the graph
 	 * @return a <code>Chart</code>
 	 */
 	@SuppressWarnings("deprecation")
 	public final Chart createPhaseSpaceLineChart(
 			final List<Double> relativeValuesX,
 			final List<Double> relativeValuesY, final String seriesIdentifier) {
 		final ChartWithAxes retValue = ChartWithAxesImpl.create();
 
         // get the NLS default labels
 		defaultAxisLabel_X = Messages.getString("XAXISDEF.title");
 		defaultAxisLabel_Y = Messages.getString("YAXISDEF.title");
 		// Plot
 		retValue.getBlock().setBackground(ColorDefinitionImpl.WHITE());
 		final Plot p = retValue.getPlot();
 		p.getClientArea().setBackground(
 				ColorDefinitionImpl.create(255, 255, 225));
 
 		// Title
 		// cwaLine.getTitle( ).getLabel( ).getCaption( ).setValue( "Line Chart"
 		// );//$NON-NLS-1$
 		retValue.getTitle().setVisible(false);
 
 		// Legend
 		final Legend lg = retValue.getLegend();
 		lg.setVisible(false);
 		/*
 		final LineAttributes lia = lg.getOutline();
 		lg.getText().getFont().setSize(8);
 		lia.setStyle(LineStyle.SOLID_LITERAL);
 		lg.getInsets().set(10, 5, 0, 0);
 		lg.getOutline().setVisible(false);
 		lg.setAnchor(Anchor.NORTH_LITERAL);
 		lg.setPosition(Position.BELOW_LITERAL);
 		*/
 		// cwaLine.getLegend( ).getText().getFont().setSize(16);;
 		// cwaLine.getLegend( ).setVisible( true );
 
 		// X-Axis
 		xAxisPrimary = retValue.getPrimaryBaseAxes()[0];
 		xAxisPrimary.setType(AxisType.LINEAR_LITERAL);
 		// Y-Axis
 		yAxisPrimary = retValue.getPrimaryOrthogonalAxis(xAxisPrimary);
 
 		// xAxisPrimary.setType(AxisType.TEXT_LITERAL);
 
 		xAxisPrimary.getMajorGrid().setTickStyle(TickStyle.BELOW_LITERAL);
 		yAxisPrimary.getMajorGrid().setTickStyle(TickStyle.LEFT_LITERAL);
 
 		xAxisPrimary.getOrigin().setType(IntersectionType.VALUE_LITERAL);
 
 
 		xAxisPrimary.getTitle( ).getCaption( ).getFont( ).setSize( 8 );
 		xAxisLabel = defaultAxisLabel_X;
 		if (propertyToPlotX!= null) {
 			xAxisLabel = this.propertyToPlotX.getDisplayName(propertyToPlotX);
 		}
 		xAxisPrimary.getTitle( ).getCaption( ).setValue( xAxisLabel );
 		xAxisPrimary.getTitle( ).setVisible(true);
 		
 
 		yAxisPrimary.getTitle( ).getCaption( ).getFont( ).setSize( 8 );
 		yAxisLabel = defaultAxisLabel_Y;
 		if (propertyToPlotY!= null) {
 			yAxisLabel = this.propertyToPlotY.getDisplayName(propertyToPlotY);
 		}
 		yAxisPrimary.getTitle( ).getCaption( ).setValue( yAxisLabel );
 		yAxisPrimary.getTitle( ).setVisible(true);
 
 		final NumberDataSet orthoValuesX = NumberDataSetImpl
 				.create(relativeValuesX);
 		final NumberDataSet orthoValuesY = NumberDataSetImpl
 				.create(relativeValuesY);
 
 		// X-Series
 		final Series lsx = SeriesImpl.create();
 		// final LineSeries lsx = (ScatterSeries) ScatterSeriesImpl.create();
 		// seCategory.setDataSet(orthoValuesX);
 		lsx.setDataSet(orthoValuesX);
 
 		// Y-Series
 		final ScatterSeries lsy = (ScatterSeries) ScatterSeriesImpl.create();
 		lsy.setDataSet(orthoValuesY);
 		lsy.getLineAttributes().setVisible(true);
 		lsy.getLineAttributes().setColor(ColorDefinitionImpl.BLUE());
 		lsy.setSeriesIdentifier(seriesIdentifier);
		
		//lsy.getMarker().setVisible(false);
		// replaces deprecated code: lineSeries.getMarker().setVisible(false);
		if (!lsy.getMarkers().isEmpty()) {
			Marker marker = lsy.getMarkers().get(0);
			marker.setVisible(false);
		}
		
 		final SeriesDefinition sdX = SeriesDefinitionImpl.create();
 		final SeriesDefinition sdY = SeriesDefinitionImpl.create();
 
 		sdY.getSeriesPalette().update(-2);
 
 		xAxisPrimary.getSeriesDefinitions().add(sdX);
 		yAxisPrimary.getSeriesDefinitions().add(sdY);
 
 		// sdX.getSeries().add(seCategory);
 		sdX.getSeries().add(lsx);
 		sdY.getSeries().add(lsy);
 
 		xAxisPrimary.getScale().setMin(NumberDataElementImpl.create(0.0));
 		xAxisPrimary.getScale().setMax(NumberDataElementImpl.create(1.0));
 		xAxisPrimary.getScale().setStep(0.25);
 		xAxisPrimary.getLabel().getCaption().getFont().setSize(9);
 
 		yAxisPrimary.getScale().setMin(NumberDataElementImpl.create(0.0));
 		yAxisPrimary.getScale().setMax(NumberDataElementImpl.create(1.0));
 		yAxisPrimary.getScale().setStep(0.25);
 		yAxisPrimary.getLabel().getCaption().getFont().setSize(9);
 
 		return retValue;
 	} // createPhaseSpaceLineChart
 
 	/**
 	 * @param rvhp
 	 * @param propertyToPlotX
 	 * @param propertyToPlotY
 	 */
 	public void setDataSourceAndRedraw(
 			final RelativeValueHistoryProviderAdapter rvhp,
 			final ItemPropertyDescriptor propertyToPlotX,
 			final ItemPropertyDescriptor propertyToPlotY) {
 		this.rvhp = rvhp;
 		this.propertyToPlotX = propertyToPlotX;
 		this.propertyToPlotY = propertyToPlotY;
 		
 		xAxisLabel = defaultAxisLabel_X;
 		if (propertyToPlotX!= null) {
 			xAxisLabel = this.propertyToPlotX.getDisplayName(propertyToPlotX);
 		}
 		xAxisPrimary.getTitle( ).getCaption( ).setValue( xAxisLabel );
 		xAxisPrimary.getTitle( ).setVisible(true);
 			
 		yAxisPrimary.getTitle( ).getCaption( ).getFont( ).setSize( 8 );
 		yAxisLabel = defaultAxisLabel_Y;
 		if (propertyToPlotY!= null) {
 			yAxisLabel = this.propertyToPlotY.getDisplayName(propertyToPlotY);
 		}
 		yAxisPrimary.getTitle( ).getCaption( ).setValue( yAxisLabel );
 		yAxisPrimary.getTitle( ).setVisible(true);
 		
 		draw();
 	} // setDataSourceAndRedraw
 
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
 	 * @return the propertyToPlotX
 	 */
 	public final ItemPropertyDescriptor getPropertyToPlotX() {
 		return propertyToPlotX;
 	}
 
 	/**
 	 * @return the propertyToPlotY
 	 */
 	public final ItemPropertyDescriptor getPropertyToPlotY() {
 		return propertyToPlotY;
 	}
 
 	/**
 	 * @param propertyToPlotX
 	 *            the propertyToPlotX to set
 	 */
 	public final void setPropertyToPlotX(
 			final ItemPropertyDescriptor propertyToPlotX) {
 		this.propertyToPlotX = propertyToPlotX;
 	}
 
 	/**
 	 * @param propertyToPlotY
 	 *            the propertyToPlotY to set
 	 */
 	public final void setPropertyToPlotY(
 			final ItemPropertyDescriptor propertyToPlotY) {
 		this.propertyToPlotY = propertyToPlotY;
 	}
 
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
 		relativeValuesX.add(new Double(1.0));
 		relativeValuesY.add(new Double(0.0));
 	}
 
 	private void clearData() {
 		relativeValuesX.clear();
 		relativeValuesY.clear();
 	}
 	
 	void toggleScales() {
 		if (useLinearScales) { //Switch to logarithmic scale
 			logTimeScalesAction.setChecked(true);
 			linearScalesAction.setChecked(false);
 			
 			xAxisPrimary.getScale().setMin(null);
 			xAxisPrimary.getScale().setMax(null);
 			xAxisPrimary.getScale().unsetStep();
 			xAxisPrimary.getScale().unsetStepNumber();			
 			xAxisPrimary.getTitle().getCaption().setValue("Log(" + xAxisLabel + ")");			
 			
 			yAxisPrimary.getScale().setMin(null);
 			yAxisPrimary.getScale().setMax(null);
 			yAxisPrimary.getScale().unsetStep();
 			yAxisPrimary.getScale().unsetStepNumber();
 			yAxisPrimary.getTitle().getCaption().setValue("Log(" + yAxisLabel + ")");
 		}
 		else { //Switch to linear scale			
 			logTimeScalesAction.setChecked(false);
 			linearScalesAction.setChecked(true);
 			
 			xAxisPrimary.getScale().setMin(NumberDataElementImpl.create(0.0));
 			xAxisPrimary.getScale().setMax(NumberDataElementImpl.create(1.0));
 			xAxisPrimary.getScale().setStep(0.25);
 			xAxisPrimary.getTitle().getCaption().setValue(xAxisLabel);
 
 			yAxisPrimary.getScale().setMin(NumberDataElementImpl.create(0.0));
 			yAxisPrimary.getScale().setMax(NumberDataElementImpl.create(1.0));
 			yAxisPrimary.getScale().setStep(0.25);
 			yAxisPrimary.getTitle().getCaption().setValue(yAxisLabel);			
 		}		
 		useLinearScales = !useLinearScales;
 		redraw();
 	}
 	
 	protected class LinearScalesAction extends Action
 	{
 		public LinearScalesAction()
 		{
 			super(Messages.getString("ContextMenu.LinearScaling"), IAction.AS_CHECK_BOX);
 			setChecked(useLinearScales);
 		}
 		
 		/**
 		 * @see org.eclipse.jface.action.Action#getText()
 		 */
 		@Override
 		public String getText() {			
 			return Messages.getString("ContextMenu.LinearScaling");
 		}
 		
 		/**
 		 * @see org.eclipse.jface.action.Action#run()
 		 */
 		@Override
 		public void run() {
 			if (useLinearScales) {
 				setChecked(true);
 				//Nothing to do. It's already linear-time.
 			}
 			else {
 				toggleScales();
 			}
 		}
 	}
 	
 	class LogarithmicScalesAction extends Action
 	{
 		public LogarithmicScalesAction()
 		{
 			super(Messages.getString("ContextMenu.LogScaling"), IAction.AS_CHECK_BOX);
 			setChecked(!useLinearScales);
 		}
 		
 		/**
 		 * @see org.eclipse.jface.action.Action#getText()
 		 */
 		@Override
 		public String getText() {			
 			return Messages.getString("ContextMenu.LogScaling");
 		}
 		
 		/**
 		 * @see org.eclipse.jface.action.Action#run()
 		 */
 		@Override
 		public void run() {
 			if (!useLinearScales) {
 				setChecked(true);
 				//Nothing to do. It's already log-time.
 			}
 			else {
 				toggleScales();
 			}
 		}
 	}
 } // PhaseSpaceCanvas
