 /*
  *  Copyright 2012 Axel Winkler, Daniel Dunér
  * 
  *  This file is part of Daxplore Presenter.
  *
  *  Daxplore Presenter is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU Lesser General Public License as published by
  *  the Free Software Foundation, either version 2.1 of the License, or
  *  (at your option) any later version.
  *
  *  Daxplore Presenter is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Lesser General Public License for more details.
  *
  *  You should have received a copy of the GNU Lesser General Public License
  *  along with Daxplore Presenter.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.daxplore.presenter.chart.display;
 
 import org.daxplore.presenter.chart.resources.ChartTexts;
 
 import com.googlecode.gchart.client.GChart.AnnotationLocation;
 import com.googlecode.gchart.client.GChart.Curve;
 import com.googlecode.gchart.client.GChart.Symbol;
 import com.googlecode.gchart.client.GChart.SymbolType;
 
 /**
  * An abstract class for creating a bar in a barchart.
  * 
  * <p>Instead of using Gchart's built in bars, this class is used. It allows
  * bars to overlap, but still be hovered over properly. Each bar should be
  * mapped against a GChart curve, and each curve should (at most) have one
  * bar.</p>
  * 
  */
 abstract class ChartBar {
 
 	protected final ChartTexts chartTexts;
 
 	/**
 	 * The curve belonging to this bar.
 	 */
 	protected final Curve curve;
 
 	/**
 	 * The color set used to color this bar.
 	 */
 	protected final BarColors color;
 
 	/**
 	 * Display the chart bar in a printer-friendly mode.
 	 */
 	protected boolean printerMode;
 
 	protected double annotationCharacterCount = 10;
 
 	/**
 	 * Create a new bar.
 	 * 
 	 * <p>Each bar should be mapped against a GChart curve, and each curve
 	 * should (at most) have one bar.</p>
 	 * 
 	 * <p> Use GChart.formatAsHovertext(String plainTextLabel) to format the
 	 * hover text. </p>
 	 * 
 	 * @param barCurve
 	 *            The Gchart curve that this bar is mapped to.
 	 * @param color
 	 *            The color set, used to color this bar.
 	 * @param hoverText
 	 *            The GChart-formatted text to display when the bar is hovered.
 	 */
 	ChartBar(ChartTexts chartTexts, Curve barCurve, BarColors color, boolean printerMode, AnnotationLocation hoverLocation) {
 		this.curve = barCurve;
 		this.color = color;
 		this.chartTexts = chartTexts;
 		this.printerMode = printerMode;
 
 		Symbol symbol = curve.getSymbol();
 		symbol.setBorderStyle("none");
 		symbol.setBorderWidth(0);
 		symbol.setSymbolType(SymbolType.VBAR_SOUTHWEST);
 		symbol.setModelWidth(1.0);
 		symbol.setDistanceMetric(0, 0);
 		symbol.setHoverSelectionEnabled(false);
 		
 		symbol.setHoverLocation(hoverLocation);
 		if(hoverLocation==AnnotationLocation.SOUTHWEST) {
 			symbol.setHoverXShift(20);
 		} else if(hoverLocation==AnnotationLocation.SOUTHEAST) {
 			symbol.setHoverXShift(-20);
 		}
 
 		symbol.setHoverAnnotationEnabled(!printerMode);
 	}
 
 	/**
 	 * Get the curve that this bar is mapped against.
 	 * 
 	 * @return The curve that this bar is mapped against.
 	 */
 	Curve getCurve() {
 		return curve;
 	}
 
 	/**
 	 * Get the color set used to color this bar.
 	 * 
 	 * @return The color set used to color this bar.
 	 */
 	BarColors getColor() {
 		return color;
 	}
 
 	/**
 	 * Specify the position of the bar in the GChart.
 	 * 
 	 * @param x
 	 *            Where to put this bar on the X-axis of the G-chart.
 	 * @param y
 	 *            How high to make this bar, in the GChart.
 	 */
 	void setDataPoint(double x, double y) {
 		// Optionally use:
 		// curve.clearPoints();
 		curve.addPoint(x, y);
 	}
 
 	/**
 	 * This method is called when the user hovers the mouse over the bar.
 	 * 
 	 * <p>Use it to change the appearance of the bar, making it stand out.</p>
 	 */
 	abstract void hover();
 
 	/**
 	 * This method is called when the user stops hovering the bar.
 	 * 
 	 * <p>Use it to change the appearance of the bar, restoring the original
 	 * appearance.</p>
 	 */
 	abstract void unhover();
 
 	/**
 	 * Use instead of GChart.formatAsHovertext(plainTextLabel) and use css on
 	 * .daxplore-BarAnnotation to format the label instead.
 	 */
 	protected String formatAsHoverText(String annotation) {
 		return "<html><div class=\"daxplore-BarAnnotation\"" + "style=\"background-color:" + color.getAnnotation() + "\">" + annotation + "</div>";
 	}
 
 	/**
 	 * Get a rough estimate of the annotation width.
 	 * 
 	 * @return The annotation width in pixels.
 	 */
 	public double getAnnotationWidth() {
 		return annotationCharacterCount * 7.5;
 	}
 }
