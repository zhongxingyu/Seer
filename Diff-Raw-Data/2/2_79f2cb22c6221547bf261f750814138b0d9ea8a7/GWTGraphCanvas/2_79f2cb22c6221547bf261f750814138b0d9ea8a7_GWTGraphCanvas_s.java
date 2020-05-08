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
 
 import org.utgenome.gwt.utgb.client.bio.CompactWIGData;
 import org.utgenome.gwt.utgb.client.canvas.GWTGenomeCanvas.DragPoint;
 import org.utgenome.gwt.utgb.client.track.TrackConfig;
 import org.utgenome.gwt.utgb.client.track.TrackGroup;
 import org.utgenome.gwt.utgb.client.track.TrackWindow;
 import org.utgenome.gwt.utgb.client.ui.Animation;
 import org.utgenome.gwt.utgb.client.util.Optional;
 import org.utgenome.gwt.widget.client.Style;
 
 import com.google.gwt.core.client.GWT;
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
 	private TrackWindow trackWindow;
 	private final HashMap<TrackWindow, GraphCanvas> canvasMap = new HashMap<TrackWindow, GraphCanvas>();
 
 	private int indentHeight = 0;
 
 	private static class GraphCanvas {
 		public final TrackWindow window;
 		public final List<CompactWIGData> graphData;
 		public final GWTCanvas canvas = new GWTCanvas();
 
 		public GraphCanvas(TrackWindow window, List<CompactWIGData> graphData, int height) {
 			this.window = window;
 			this.graphData = graphData;
 			canvas.setCoordSize(window.getPixelWidth(), height);
 			setPixelSize(window.getPixelWidth(), height);
 		}
 
 		public void clearCanvas() {
 			canvas.clear();
 		}
 
 		public void setCoordinateHeight(int height) {
 			canvas.setCoordSize(window.getPixelWidth(), height);
 		}
 
 		private void setPixelSize(int width, int height) {
 			canvas.setPixelWidth(width);
 			canvas.setPixelHeight(height);
 		}
 
 		public void setPixelWidth(int width) {
 			canvas.setPixelWidth(width);
 		}
 
 		public void setPixelHeight(int height) {
 			setPixelSize(window.getPixelWidth(), height);
 		}
 	}
 
 	/**
 	 * @author leo
 	 * 
 	 */
 	public static class GraphStyle {
 		public int windowHeight = 100;
 		public float maxValue = 20.0f;
 		public float minValue = 0.0f;
 		public boolean autoScale = false;
 		public boolean logScale = false;
 		public boolean drawZeroValue = false;
 		public boolean drawScale = true;
 		public boolean showScaleLabel = true;
 		public Optional<String> color = new Optional<String>();
 
 		private final static String CONFIG_TRACK_HEIGHT = "trackHeight";
 		private final static String CONFIG_MAX_VALUE = "maxValue";
 		private final static String CONFIG_MIN_VALUE = "minValue";
 		private final static String CONFIG_AUTO_SCALE = "isAutoRange";
 		private final static String CONFIG_LOG_SCALE = "isLog";
 		private final static String CONFIG_SHOW_ZERO_VALUE = "showZero";
 		private final static String CONFIG_DRAW_SCALE = "drawScale";
 		private final static String CONFIG_SHOW_SCALE_LABEL = "showScaleLabel";
 		private final static String CONFIG_COLOR = "color";
 
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
 			drawZeroValue = config.getBoolean(CONFIG_SHOW_ZERO_VALUE, drawZeroValue);
 			drawScale = config.getBoolean(CONFIG_DRAW_SCALE, drawScale);
 			showScaleLabel = config.getBoolean(CONFIG_SHOW_SCALE_LABEL, showScaleLabel);
 			windowHeight = config.getInt(CONFIG_TRACK_HEIGHT, windowHeight);
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
 			config.addConfigBoolean("Show Zero Value", CONFIG_SHOW_ZERO_VALUE, drawZeroValue);
 			config.addConfigBoolean("Draw Scale", CONFIG_DRAW_SCALE, drawScale);
 			config.addConfigBoolean("Show Scale Label", CONFIG_SHOW_SCALE_LABEL, showScaleLabel);
 			config.addConfigInteger("Pixel Height", CONFIG_TRACK_HEIGHT, windowHeight);
 			config.addConfigString("Graph Color", CONFIG_COLOR, "");
 		}
 
 	}
 
 	private GraphStyle style = new GraphStyle();
 	private TrackGroup trackGroup;
 
 	public GWTGraphCanvas() {
 
 		init();
 	}
 
 	public void setTrackGroup(TrackGroup trackGroup) {
 		this.trackGroup = trackGroup;
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
 		for (GraphCanvas each : canvasMap.values()) {
 			each.canvas.removeFromParent();
 		}
 		canvasMap.clear();
 		clearScale();
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
			int x = trackWindow.convertToPixelX(w.getStartOnGenome());
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
 			final int pixelWidth = graphCanvas.window.getPixelWidth();
 			for (int i = 0; i < pixelWidth; ++i) {
 				float value = data.getData()[i];
 				float y1;
 				if (value == 0.0f) {
 					if (!style.drawZeroValue)
 						continue;
 					else {
 						y1 = y2 + ((style.minValue < style.maxValue) ? -0.5f : 0.5f);
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
 
 	public void drawFrame(List<CompactWIGData> wigDataList) {
 
 		if (!style.drawScale)
 			return;
 
 		if (style.autoScale && wigDataList != null) {
 			float tempMinValue = 0.0f;
 			float tempMaxValue = 0.0f;
 			for (CompactWIGData data : wigDataList) {
 				tempMinValue = Math.min(tempMinValue, data.getMinValue());
 				tempMaxValue = Math.max(tempMaxValue, data.getMaxValue());
 			}
 			GWT.log("range:" + tempMinValue + "-" + tempMaxValue, null);
 			style.minValue = tempMinValue;
 			style.maxValue = tempMaxValue;
 		}
 
 		// draw frame
 		frameCanvas.saveContext();
 		frameCanvas.setStrokeStyle(new Color(0, 0, 0, 0.5f));
 		frameCanvas.setLineWidth(1.0f);
 		frameCanvas.beginPath();
 		frameCanvas.rect(0, 0, trackWindow.getPixelWidth(), style.windowHeight);
 		frameCanvas.stroke();
 		frameCanvas.restoreContext();
 
 		// draw indent line & label
 		Indent indent = new Indent(style.minValue, style.maxValue);
 
 		{
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
 				frameCanvas.lineTo(trackWindow.getPixelWidth(), 0);
 				frameCanvas.stroke();
 				frameCanvas.restoreContext();
 			}
 			{
 				// draw zero line
 				frameCanvas.saveContext();
 				frameCanvas.beginPath();
 				frameCanvas.translate(0, getYPosition(0f));
 				frameCanvas.moveTo(0, 0);
 				frameCanvas.lineTo(trackWindow.getPixelWidth(), 0);
 				frameCanvas.stroke();
 				frameCanvas.restoreContext();
 			}
 
 			frameCanvas.restoreContext();
 		}
 
 	}
 
 	public void drawScaleLabel() {
 
 		if (!style.showScaleLabel)
 			return;
 
 		Indent indent = new Indent(style.minValue, style.maxValue);
 		int fontHeight = 10;
 
 		boolean isVerticalFlip = style.minValue > style.maxValue;
 
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
 
 			//			if (labelY > style.windowHeight) {
 			//				continue;
 			//			}
 
 			graphLabels.add(label);
 			panel.add(label, labelX, labelY);
 
 		}
 	}
 
 	public class Indent {
 		public int exponent = 0;
 		public long fraction = 0;
 
 		public int nSteps = 0;
 
 		public float min = 0.0f;
 		public float max = 0.0f;
 
 		public Indent(float minValue, float maxValue) {
 			if (indentHeight == 0)
 				indentHeight = 10;
 
 			min = minValue < maxValue ? minValue : maxValue;
 			max = minValue > maxValue ? minValue : maxValue;
 
 			if (style.logScale) {
 				min = getLogValue(min);
 				max = getLogValue(max);
 			}
 
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
 		if (style.maxValue == style.minValue)
 			return 0.0f;
 
 		float tempMin = style.maxValue < style.minValue ? style.maxValue : style.minValue;
 		float tempMax = style.maxValue > style.minValue ? style.maxValue : style.minValue;
 
 		if (style.logScale) {
 			value = getLogValue(value);
 			tempMax = getLogValue(tempMax);
 			tempMin = getLogValue(tempMin);
 		}
 		float valueHeight = (value - tempMin) / (tempMax - tempMin) * style.windowHeight;
 
 		if (style.maxValue < style.minValue)
 			return valueHeight;
 		else
 			return style.windowHeight - valueHeight;
 	}
 
 	private float logBase = 2.0f;
 
 	public float getLogBase() {
 		return logBase;
 	}
 
 	public void setLogBase(float logBase) {
 		this.logBase = logBase;
 	}
 
 	public float getLogValue(float value) {
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
 
 	private class ScrollAnimation extends Animation {
 
 		private final TrackWindow from;
 		private final TrackWindow to;
 
 		private ScrollAnimation(TrackWindow from, TrackWindow to) {
 			super(10);
 			this.from = new TrackWindow(from);
 			this.to = new TrackWindow(to);
 		}
 
 		@Override
 		protected void onUpdate(double ratio) {
 			for (GraphCanvas each : canvasMap.values()) {
 				int start = each.window.getStartOnGenome();
 				double s = from.convertToPixelX(start);
 				double e = to.convertToPixelX(start);
 				int p = (int) ((1.0d - ratio) * s + ratio * e);
 				panel.setWidgetPosition(each.canvas, p, 0);
 			}
 		}
 
 		@Override
 		protected void onComplete() {
 			for (GraphCanvas each : canvasMap.values()) {
 				int start = each.window.getStartOnGenome();
 				int e = to.convertToPixelX(start);
 				panel.setWidgetPosition(each.canvas, e, 0);
 			}
 		}
 
 	}
 
 	public void setTrackWindow(final TrackWindow w) {
 		if (trackWindow != null) {
 			if (trackWindow.hasSameScaleWith(w)) {
 
 				for (GraphCanvas each : canvasMap.values()) {
 					int start = each.window.getStartOnGenome();
 					int e = w.convertToPixelX(start);
 					panel.setWidgetPosition(each.canvas, e, 0);
 				}
 
 				//ScrollAnimation animation = new ScrollAnimation(trackWindow, w);
 				//animation.run(1000);
 			}
 			else {
 				//				// scroll & zoom in/out
 				//				for (GraphCanvas each : canvasMap.values()) {
 				//					int x = w.convertToPixelX(each.window.getStartOnGenome());
 				//					int pixelWidth = (int) (w.getPixelLengthPerBase() * each.window.getSequenceLength());
 				//					each.canvas.setPixelWidth(pixelWidth);
 				//					//panel.setWidgetPosition(each.canvas, x, 0);
 				//				}
 			}
 		}
 
 		trackWindow = w;
 	}
 
 	public TrackWindow getTrackWindow() {
 		return trackWindow;
 	}
 
 	public void setStyle(GraphStyle style) {
 		this.style = style;
 		setPixelSize(trackWindow.getPixelWidth(), style.windowHeight);
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
