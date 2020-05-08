 /*--------------------------------------------------------------------------
  *  Copyright 2009 utgenome.org
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *--------------------------------------------------------------------------*/
 //--------------------------------------
 // utgb-core Project
 //
 // GWTGraphCanvas.java
 // Since: 2010/05/28
 //
 // $URL$ 
 // $Author$
 //--------------------------------------
 package org.utgenome.gwt.utgb.client.canvas;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.utgenome.gwt.utgb.client.UTGBEntryPointBase;
 import org.utgenome.gwt.utgb.client.bio.CompactWIGData;
 import org.utgenome.gwt.utgb.client.canvas.GWTGenomeCanvas.DragPoint;
 import org.utgenome.gwt.utgb.client.track.TrackConfig;
 import org.utgenome.gwt.utgb.client.track.TrackWindow;
 import org.utgenome.gwt.utgb.client.util.Optional;
 import org.utgenome.gwt.widget.client.Style;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.core.client.JavaScriptException;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.Event;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.AbsolutePanel;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.widgetideas.graphics.client.Color;
 import com.google.gwt.widgetideas.graphics.client.GWTCanvas;
 
 /**
  * GWT Canvas for drawing bar graph, heat map, etc.
  * 
  * @author yoshimura
  * @author leo
  * 
  */
 public class GWTGraphCanvas extends Composite {
 	// widget
 
 	private GWTCanvas frameCanvas = new GWTCanvas();
 	private AbsolutePanel panel = new AbsolutePanel();
 	private TrackWindow viewWindow;
 	private final HashMap<TrackWindow, GraphCanvas> canvasMap = new HashMap<TrackWindow, GraphCanvas>();
 
 	private static class GraphCanvas {
 		public final TrackWindow window;
 		public final List<CompactWIGData> graphData;
 		public final GWTCanvas canvas = new GWTCanvas();
		public final int span;
 
 		public GraphCanvas(TrackWindow window, List<CompactWIGData> graphData, int height) {
 			this.window = window;
 			this.graphData = graphData;
 
			int maxSpan = 1;
			for (CompactWIGData each : graphData) {
				int span = each.getSpan();
				if (span > maxSpan)
					maxSpan = span;
			}
			this.span = maxSpan;

			canvas.setCoordSize(window.getPixelWidth() + this.span - 1, height);
 			setPixelSize(window.getPixelWidth(), height);
 		}
 
 		public void clearCanvas() {
 			canvas.clear();
 		}
 
 		public void setCoordinateHeight(int height) {
			canvas.setCoordSize(window.getPixelWidth() + span - 1, height);
 		}
 
 		private void setPixelSize(int width, int height) {
			canvas.setPixelWidth(width + span - 1);
 			canvas.setPixelHeight(height);
 		}
 
 		public void setPixelHeight(int height) {
 			setPixelSize(window.getPixelWidth(), height);
 		}
 	}
 
 	/**
 	 * Graph Drawing Configurations
 	 * 
 	 * @author leo
 	 * 
 	 */
 	public static class GraphStyle {
 		public int windowHeight = 100;
 		private float maxValue = 20.0f;
 		private float minValue = 0.0f;
 
 		public boolean autoScale = false;
 		public boolean logScale = false;
 		public boolean drawZeroValue = false;
 		public boolean drawScale = true;
 		public boolean showScaleLabel = true;
 		public Optional<String> color = new Optional<String>();
 		public float logBase = 2.0f;
 
 		public final static String CONFIG_TRACK_HEIGHT = "trackHeight";
 		private final static String CONFIG_MAX_VALUE = "maxValue";
 		private final static String CONFIG_MIN_VALUE = "minValue";
 		private final static String CONFIG_AUTO_SCALE = "autoScale";
 		private final static String CONFIG_LOG_SCALE = "logScale";
 		private final static String CONFIG_LOG_BASE = "log base";
 		private final static String CONFIG_SHOW_ZERO_VALUE = "showZero";
 		private final static String CONFIG_DRAW_SCALE = "drawScale";
 		private final static String CONFIG_SHOW_SCALE_LABEL = "showScaleLabel";
 		private final static String CONFIG_COLOR = "color";
 
 		public float getDefaultMinValue() {
 			return minValue;
 		}
 
 		public float getDefaultMaxValue() {
 			return maxValue;
 		}
 
 		public boolean isReverseYAxis() {
 			return minValue > maxValue;
 		}
 
 		/**
 		 * Load the parameter values from the configuration panel
 		 * 
 		 * @param config
 		 */
 		public void load(TrackConfig config) {
 			maxValue = config.getFloat(CONFIG_MAX_VALUE, maxValue);
 			minValue = config.getFloat(CONFIG_MIN_VALUE, minValue);
 			autoScale = config.getBoolean(CONFIG_AUTO_SCALE, autoScale);
 			logScale = config.getBoolean(CONFIG_LOG_SCALE, logScale);
 			logBase = config.getFloat(CONFIG_LOG_BASE, logBase);
 			drawZeroValue = config.getBoolean(CONFIG_SHOW_ZERO_VALUE, drawZeroValue);
 			drawScale = config.getBoolean(CONFIG_DRAW_SCALE, drawScale);
 			showScaleLabel = config.getBoolean(CONFIG_SHOW_SCALE_LABEL, showScaleLabel);
 			windowHeight = config.getInt(CONFIG_TRACK_HEIGHT, windowHeight);
 			if (windowHeight <= 0)
 				windowHeight = 100;
 			String colorStr = config.getString(CONFIG_COLOR, "");
 			if (colorStr != null && colorStr.length() > 0)
 				color.set(colorStr);
 			else
 				color.reset();
 		}
 
 		/**
 		 * Set up the configuration panel
 		 * 
 		 * @param config
 		 */
 		public void setup(TrackConfig config) {
 			config.addConfigDouble("Y Max", CONFIG_MAX_VALUE, maxValue);
 			config.addConfigDouble("Y Min", CONFIG_MIN_VALUE, minValue);
 			config.addConfigBoolean("Auto Scale", CONFIG_AUTO_SCALE, autoScale);
 			config.addConfigBoolean("Log Scale", CONFIG_LOG_SCALE, logScale);
 			config.addConfigDouble("Log Base", CONFIG_LOG_BASE, logBase);
 			config.addConfigBoolean("Show Zero Value", CONFIG_SHOW_ZERO_VALUE, drawZeroValue);
 			config.addConfigBoolean("Draw Scale", CONFIG_DRAW_SCALE, drawScale);
 			config.addConfigBoolean("Show Scale Label", CONFIG_SHOW_SCALE_LABEL, showScaleLabel);
 			config.addConfigInteger("Pixel Height", CONFIG_TRACK_HEIGHT, windowHeight);
 			config.addConfigString("Graph Color", CONFIG_COLOR, "");
 		}
 
 	}
 
 	private GraphStyle style = new GraphStyle();
 
 	public GWTGraphCanvas() {
 
 		init();
 	}
 
 	private void init() {
 
 		Style.padding(panel, 0);
 		Style.margin(panel, 0);
 
 		panel.add(frameCanvas, 0, 0);
 		//panel.add(canvas, 0, 0);
 		initWidget(panel);
 
 		sinkEvents(Event.ONMOUSEMOVE | Event.ONMOUSEOVER | Event.ONMOUSEOUT | Event.ONMOUSEDOWN | Event.ONMOUSEUP);
 	}
 
 	private Optional<DragPoint> dragStartPoint = new Optional<DragPoint>();
 
 	@Override
 	public void onBrowserEvent(Event event) {
 		super.onBrowserEvent(event);
 
 		int type = DOM.eventGetType(event);
 		switch (type) {
 		case Event.ONMOUSEOVER:
 
 			break;
 		case Event.ONMOUSEMOVE: {
 			// show readLabels 
 
 			if (dragStartPoint.isDefined()) {
 				// scroll the canvas
 				int clientX = DOM.eventGetClientX(event) + Window.getScrollLeft();
 				//int clientY = DOM.eventGetClientY(event) + Window.getScrollTop();
 
 				DragPoint p = dragStartPoint.get();
 				int xDiff = clientX - p.x;
 				//int yDiff = clientY - p.y;
 				//panel.setWidgetPosition(canvas, xDiff, 0);
 			}
 			else {
 				//Style.cursor(canvas, Style.CURSOR_AUTO);
 			}
 
 			break;
 		}
 		case Event.ONMOUSEOUT: {
 			resetDrag(event);
 			break;
 		}
 		case Event.ONMOUSEDOWN: {
 			// invoke a click event 
 			int clientX = DOM.eventGetClientX(event) + Window.getScrollLeft();
 			int clientY = DOM.eventGetClientY(event) + Window.getScrollTop();
 
 			if (dragStartPoint.isUndefined()) {
 				dragStartPoint.set(new DragPoint(clientX, clientY));
 				//Style.cursor(canvas, Style.CURSOR_RESIZE_E);
 				event.preventDefault();
 			}
 
 			break;
 		}
 		case Event.ONMOUSEUP: {
 
 			resetDrag(event);
 			break;
 		}
 		}
 	}
 
 	private void resetDrag(Event event) {
 
 		int clientX = DOM.eventGetClientX(event) + Window.getScrollLeft();
 		//int clientY = DOM.eventGetClientY(event) + Window.getScrollTop();
 
 		//		if (dragStartPoint.isDefined() && trackWindow != null) {
 		//			DragPoint p = dragStartPoint.get();
 		//			int startDiff = trackWindow.convertToGenomePosition(clientX) - trackWindow.convertToGenomePosition(p.x);
 		//			if (startDiff != 0) {
 		//				int newStart = trackWindow.getStartOnGenome() - startDiff;
 		//				if (newStart < 1)
 		//					newStart = 1;
 		//				int newEnd = newStart + trackWindow.getSequenceLength();
 		//				TrackWindow newWindow = trackWindow.newWindow(newStart, newEnd);
 		//				if (trackGroup != null)
 		//					trackGroup.setTrackWindow(newWindow);
 		//			}
 		//		}
 
 		dragStartPoint.reset();
 
 		//Style.cursor(canvas, Style.CURSOR_AUTO);
 	}
 
 	public void clear() {
 		clearCanvas();
 		clearScale();
 	}
 
 	public void clearCanvas() {
 		for (GraphCanvas each : canvasMap.values()) {
 			each.canvas.removeFromParent();
 		}
 		canvasMap.clear();
 	}
 
 	public void clearScale() {
 		frameCanvas.clear();
 
 		for (Label each : graphLabels) {
 			each.removeFromParent();
 		}
 		graphLabels.clear();
 	}
 
 	public void clear(TrackWindow each) {
 		GraphCanvas graphCanvas = canvasMap.get(each);
 		if (graphCanvas != null) {
 			graphCanvas.canvas.clear();
 			graphCanvas.canvas.removeFromParent();
 
 			canvasMap.remove(each);
 		}
 	}
 
 	/**
 	 * Get a canvas for a given TrackWindow
 	 * 
 	 * @param w
 	 * @return
 	 */
 	public GraphCanvas getCanvas(TrackWindow w, List<CompactWIGData> data) {
 		GraphCanvas graphCanvas = canvasMap.get(w);
 		if (graphCanvas == null) {
 			// create a new graph canvas
 			graphCanvas = new GraphCanvas(w, data, style.windowHeight);
 			canvasMap.put(w, graphCanvas);
 			int x = w.convertToPixelX(w.getStartOnGenome());
 			panel.add(graphCanvas.canvas, 0, 0);
 			panel.setWidgetPosition(graphCanvas.canvas, x, 0);
 		}
 
 		return graphCanvas;
 	}
 
 	private final String DEFAULT_COLOR = "rgba(12,106,193,0.7)";
 
 	public void redrawWigGraph() {
 		for (GraphCanvas each : canvasMap.values()) {
 			each.clearCanvas();
 			each.setCoordinateHeight(style.windowHeight);
 			drawWigGraph(each);
 		}
 	}
 
 	public void drawWigGraph(List<CompactWIGData> data, TrackWindow w) {
 		if (data == null)
 			return;
 
 		GraphCanvas canvas = getCanvas(w, data);
 		drawWigGraph(canvas);
 	}
 
 	protected void drawWigGraph(GraphCanvas graphCanvas) {
 
 		for (CompactWIGData data : graphCanvas.graphData) {
 
 			// get graph color
 			Color graphColor = new Color(DEFAULT_COLOR);
 			if (style.color.isDefined()) {
 				graphColor = new Color(style.color.get());
 			}
 			else if (data.getTrack().containsKey("color")) {
 				String colorStr = data.getTrack().get("color");
 				String c[] = colorStr.split(",");
 				if (c.length == 3)
 					graphColor = new Color(Integer.valueOf(c[0]), Integer.valueOf(c[1]), Integer.valueOf(c[2]));
 			}
 
 			// draw graph
 			GWTCanvas canvas = graphCanvas.canvas;
 
 			canvas.saveContext();
 			canvas.setLineWidth(1.0f);
 			canvas.setStrokeStyle(graphColor);
 
 			float y2 = getYPosition(0.0f);
 
 			// draw data graph
 			final boolean isReverse = graphCanvas.window.isReverseStrand();
			final int pixelWidth = data.getData().length;
 
 			float min = style.autoScale ? autoScaledMinValue : style.minValue;
 			float max = style.autoScale ? autoScaledMaxValue : style.maxValue;
 
 			for (int i = 0; i < pixelWidth; ++i) {
 				float value = data.getData()[i];
 				float y1;
 				if (value == 0.0f) {
 					if (!style.drawZeroValue)
 						continue;
 					else {
 						y1 = y2 + ((min < max) ? -0.5f : 0.5f);
 					}
 				}
 				else {
 					y1 = getYPosition(value);
 				}
 
 				int x = i;
 				if (isReverse) {
 					x = pixelWidth - x - 1;
 				}
 
 				canvas.saveContext();
 				canvas.beginPath();
 				canvas.translate(x + 0.5f, 0);
 				canvas.moveTo(0, y1);
 				canvas.lineTo(0, y2);
 				canvas.stroke();
 				canvas.restoreContext();
 			}
 			canvas.restoreContext();
 		}
 	}
 
 	private List<Label> graphLabels = new ArrayList<Label>();
 
 	public void drawFrame() {
 
 		if (!style.drawScale)
 			return;
 
 		// draw frame
 		frameCanvas.saveContext();
 		frameCanvas.setStrokeStyle(new Color(0, 0, 0, 0.5f));
 		frameCanvas.setLineWidth(1.0f);
 		frameCanvas.beginPath();
 		frameCanvas.rect(0, 0, viewWindow.getPixelWidth(), style.windowHeight);
 		frameCanvas.stroke();
 		frameCanvas.restoreContext();
 
 		// draw indent line & label
 		Indent indent = createIndent();
 
 		frameCanvas.saveContext();
 		frameCanvas.setStrokeStyle(Color.BLACK);
 		frameCanvas.setGlobalAlpha(0.2f);
 		frameCanvas.setLineWidth(0.5f);
 		for (int i = 0; i <= indent.nSteps; i++) {
 			float value = indent.getIndentValue(i);
 			// draw indent line
 			frameCanvas.saveContext();
 			frameCanvas.beginPath();
 			frameCanvas.translate(0, getYPosition(value) + 0.5d);
 			frameCanvas.moveTo(0d, 0d);
 			frameCanvas.lineTo(viewWindow.getPixelWidth(), 0);
 			frameCanvas.stroke();
 			frameCanvas.restoreContext();
 		}
 		{
 			// draw zero line
 			frameCanvas.saveContext();
 			frameCanvas.beginPath();
 			frameCanvas.translate(0, getYPosition(0f));
 			frameCanvas.moveTo(0, 0);
 			frameCanvas.lineTo(viewWindow.getPixelWidth(), 0);
 			frameCanvas.stroke();
 			frameCanvas.restoreContext();
 		}
 
 		frameCanvas.restoreContext();
 
 	}
 
 	public void drawScaleLabel() {
 
 		if (!style.showScaleLabel)
 			return;
 
 		Indent indent = createIndent();
 		int fontHeight = 10;
 
 		for (int i = 0; i <= indent.nSteps; i++) {
 			float value = indent.getIndentValue(i);
 			String labelString = indent.getIndentString(i);
 			Label label = new Label(labelString);
 			label.setTitle(labelString);
 			Style.fontSize(label, fontHeight);
 			Style.textAlign(label, "left");
 			Style.fontColor(label, "#003366");
 
 			int labelX = 1;
 			int labelY = (int) (getYPosition(value) - (fontHeight / 2.0f) - 1);
 
 			if (labelY < 0 && labelY > -fontHeight)
 				labelY = -1;
 
 			if (labelY > style.windowHeight - fontHeight) {
 				labelY = style.windowHeight - fontHeight;
 			}
 
 			//			if (labelY > style.windowHeight) {
 			//				continue;
 			//			}
 
 			graphLabels.add(label);
 			panel.add(label, labelX, labelY);
 
 		}
 	}
 
 	private Indent createIndent() {
 		if (style.autoScale) {
 			return new Indent(autoScaledMinValue, autoScaledMaxValue, style);
 		}
 		else {
 			return new Indent(style.minValue, style.maxValue, style);
 		}
 	}
 
 	public static class Indent {
 		public int exponent = 0;
 		public long fraction = 0;
 
 		public int nSteps = 0;
 
 		public float min = 0.0f;
 		public float max = 0.0f;
 
 		private GraphStyle style;
 
 		public Indent(float minValue, float maxValue, GraphStyle style) {
 			this.style = style;
 
 			final int indentHeight = 10;
 
 			min = minValue < maxValue ? minValue : maxValue;
 			max = minValue > maxValue ? minValue : maxValue;
 
 			if (style.logScale) {
 				min = getLogValue(min, style.logBase);
 				max = getLogValue(max, style.logBase);
 			}
 
 			try {
 				double tempIndentValue = (max - min) / style.windowHeight * indentHeight;
 
 				if (style.logScale && tempIndentValue < 1.0)
 					tempIndentValue = 1.0;
 
 				fraction = (long) Math.floor(Math.log10(tempIndentValue));
 				exponent = (int) Math.ceil(Math.round(tempIndentValue / Math.pow(10, fraction - 3)) / 1000.0);
 
 				if (exponent <= 5)
 					;
 				//			else if(exponent <= 7)
 				//				exponent = 5;
 				else {
 					exponent = 1;
 					fraction++;
 				}
 				double stepSize = exponent * Math.pow(10, fraction);
 				max = (float) (Math.floor(max / stepSize) * stepSize);
 				min = (float) (Math.ceil(min / stepSize) * stepSize);
 
 				nSteps = (int) Math.abs((max - min) / stepSize);
 			}
 			catch (JavaScriptException e) {
 				UTGBEntryPointBase.showErrorMessage(e.getMessage());
 			}
 
 		}
 
 		public float getIndentValue(int step) {
 			double indentValue = min + (step * exponent * Math.pow(10, fraction));
 
 			if (!style.logScale)
 				return (float) indentValue;
 			else if (indentValue == 0.0f)
 				return 0.0f;
 			else if (indentValue >= 0.0f)
 				return (float) Math.pow(2, indentValue - 1);
 			else
 				return (float) -Math.pow(2, -indentValue - 1);
 		}
 
 		public String getIndentString(int step) {
 			float indentValue = getIndentValue(step);
 
 			if (indentValue == (int) indentValue)
 				return String.valueOf((int) indentValue);
 			else {
 				int exponent_tmp = (int) Math.ceil(Math.round(indentValue / Math.pow(10, fraction - 3)) / 1000.0);
 				int endIndex = String.valueOf(exponent_tmp).length() + 1;
 				if (fraction < 0)
 					endIndex -= fraction;
 				endIndex = Math.min(String.valueOf(indentValue).length(), endIndex);
 
 				return String.valueOf(indentValue).substring(0, endIndex);
 			}
 		}
 	}
 
 	public float getYPosition(float value) {
 
 		float min = style.minValue;
 		float max = style.maxValue;
 
 		if (style.autoScale) {
 			min = autoScaledMinValue;
 			max = autoScaledMaxValue;
 		}
 
 		if (min == max)
 			return 0.0f;
 
 		float tempMin = max < min ? max : min;
 		float tempMax = max > min ? max : min;
 
 		if (style.logScale) {
 			value = getLogValue(value, style.logBase);
 			tempMax = getLogValue(tempMax, style.logBase);
 			tempMin = getLogValue(tempMin, style.logBase);
 		}
 		float valueHeight = (value - tempMin) / (tempMax - tempMin) * style.windowHeight;
 
 		if (style.isReverseYAxis())
 			return valueHeight;
 		else
 			return style.windowHeight - valueHeight;
 	}
 
 	public static float getLogValue(float value, float logBase) {
 		if (Math.log(logBase) == 0.0)
 			return value;
 
 		float temp = 0.0f;
 		if (value > 0.0f) {
 			temp = (float) (Math.log(value) / Math.log(logBase) + 1.0);
 			if (temp < 0.0f)
 				temp = 0.0f;
 		}
 		else if (value < 0.0f) {
 			temp = (float) (Math.log(-value) / Math.log(logBase) + 1.0);
 			if (temp < 0.0f)
 				temp = 0.0f;
 			temp *= -1.0f;
 		}
 		return temp;
 	}
 
 	private float autoScaledMinValue = 0.0f;
 	private float autoScaledMaxValue = 0.0f;
 
 	public void setViewWindow(final TrackWindow view) {
 
 		if (viewWindow != null) {
 			if (viewWindow.hasSameScaleWith(view)) {
 				float tempMinValue = autoScaledMinValue;
 				float tempMaxValue = autoScaledMaxValue;
 
 				if (style.autoScale) {
 					autoScaledMinValue = 0.0f;
 					autoScaledMaxValue = 0.0f;
 				}
 
 				for (GraphCanvas each : canvasMap.values()) {
 					int start = each.window.getStartOnGenome();
 					int s = view.convertToPixelX(start);
 					panel.setWidgetPosition(each.canvas, s, 0);
 
 					// Auto Scale
 					if (style.autoScale) {
 
 						int pw = view.getPixelWidth();
 						int pw_e = each.window.getPixelWidth();
 
 						for (CompactWIGData wigData : each.graphData) {
 							float data[] = wigData.getData();
 
 							int loopStart, loopEnd;
 							if (!view.isReverseStrand()) {
 								loopStart = Math.max(-s, 0);
 								loopEnd = Math.min(pw - s, pw_e);
 							}
 							else {
 								loopStart = Math.max(s - pw, 0);
 								loopEnd = Math.min(s, pw_e);
 							}
 
 							for (int pos = loopStart; pos < loopEnd; pos++) {
 								autoScaledMinValue = Math.min(autoScaledMinValue, data[pos]);
 								autoScaledMaxValue = Math.max(autoScaledMaxValue, data[pos]);
 							}
 						}
 						GWT.log("scale: " + autoScaledMinValue + " - " + autoScaledMaxValue);
 					}
 				}
 
 				if (autoScaledMinValue == autoScaledMaxValue) {
 					autoScaledMinValue = style.minValue;
 					autoScaledMaxValue = style.maxValue;
 				}
 
 				if (style.autoScale && (autoScaledMinValue != tempMinValue || autoScaledMaxValue != tempMaxValue)) {
 					redrawWigGraph();
 				}
 
 			}
 			else {
 				// zoom in/out
 				clearCanvas();
 			}
 
 		}
 
 		viewWindow = view;
 	}
 
 	public TrackWindow getViewWindow() {
 		return viewWindow;
 	}
 
 	public void setStyle(GraphStyle style) {
 		this.style = style;
 		setPixelSize(viewWindow.getPixelWidth(), style.windowHeight);
 	}
 
 	public GraphStyle getStyle() {
 		return style;
 	}
 
 	@Override
 	public void setPixelSize(int width, int height) {
 
 		for (GraphCanvas each : canvasMap.values()) {
 			each.setPixelHeight(height);
 		}
 
 		frameCanvas.setCoordSize(width, height);
 		frameCanvas.setPixelWidth(width);
 		frameCanvas.setPixelHeight(height);
 
 		panel.setPixelSize(width, height);
 	}
 
 }
