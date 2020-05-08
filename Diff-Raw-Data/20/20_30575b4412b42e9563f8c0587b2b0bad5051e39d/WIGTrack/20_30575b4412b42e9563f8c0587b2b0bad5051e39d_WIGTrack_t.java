 /*--------------------------------------------------------------------------
  *  Copyright 2010 utgenome.org
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
 // WIGTrack.java
 // Since: 2010/09/27
 //
 //--------------------------------------
 package org.utgenome.gwt.utgb.client.track.lib;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import org.utgenome.gwt.utgb.client.bio.ChrLoc;
 import org.utgenome.gwt.utgb.client.bio.CompactWIGData;
 import org.utgenome.gwt.utgb.client.canvas.BarGraphCanvas;
 import org.utgenome.gwt.utgb.client.canvas.GWTGraphCanvas.GraphStyle;
 import org.utgenome.gwt.utgb.client.canvas.GraphScale;
 import org.utgenome.gwt.utgb.client.canvas.TrackWindowChain;
 import org.utgenome.gwt.utgb.client.canvas.TrackWindowChain.WindowUpdateInfo;
 import org.utgenome.gwt.utgb.client.track.Track;
 import org.utgenome.gwt.utgb.client.track.TrackBase;
 import org.utgenome.gwt.utgb.client.track.TrackConfig;
 import org.utgenome.gwt.utgb.client.track.TrackConfigChange;
 import org.utgenome.gwt.utgb.client.track.TrackFrame;
 import org.utgenome.gwt.utgb.client.track.TrackGroup;
 import org.utgenome.gwt.utgb.client.track.TrackGroupPropertyChange;
 import org.utgenome.gwt.utgb.client.track.TrackWindow;
 import org.utgenome.gwt.utgb.client.track.UTGBProperty;
 import org.utgenome.gwt.widget.client.Style;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.AbsolutePanel;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * WIG data track
  * 
  * @author leo
  * 
  */
 public class WIGTrack extends TrackBase {
 
 	public static TrackFactory factory() {
 		return new TrackFactory() {
 			@Override
 			public Track newInstance() {
 				return new WIGTrack();
 			}
 		};
 	}
 
 	private final AbsolutePanel panel = new AbsolutePanel();
 	private final FlexTable layoutTable = new FlexTable();
 	private final GraphScale scale = new GraphScale();
 
 	private TrackWindowChain chain = new TrackWindowChain();
 	private final List<List<BarGraphCanvas>> buffer = new ArrayList<List<BarGraphCanvas>>(2);
 	private int frontBufferID = 0; // 0 or 1
 
 	private final GraphStyle style = new GraphStyle();
 
 	{
 		buffer.add(new ArrayList<BarGraphCanvas>());
 		buffer.add(new ArrayList<BarGraphCanvas>());
 	}
 
 	public WIGTrack() {
 		super("WIG Track");
 		layoutTable.setBorderWidth(0);
 		layoutTable.setCellPadding(0);
 		layoutTable.setCellSpacing(0);
 		Style.margin(layoutTable, 0);
 		Style.padding(layoutTable, 0);
 		Style.fullSize(layoutTable);
 
 		panel.add(scale, 0, 0);
 		layoutTable.setWidget(0, 0, panel);
 	}
 
 	public Widget getWidget() {
 		return layoutTable;
 	}
 
 	/**
 	 * Configuration parameter names
 	 */
 	private final static String CONFIG_PATH = "path";
 
 	@Override
 	public void setUp(TrackFrame trackFrame, TrackGroup group) {
 		TrackConfig config = getConfig();
 		config.addConfigString("Path", CONFIG_PATH, "");
 		style.setup(config);
 	}
 
 	@Override
 	public void onChangeTrackHeight(int newHeight) {
 		style.windowHeight = newHeight;
 		needToUpdateStyle = true;
 		refresh();
 	}
 
 	private boolean needToUpdateStyle = true;
 
 	private void updateStyle() {
 		// load the style parameter values from the configuration panel, then set the style
 		style.load(getConfig());
 
 		needToUpdateStyle = false;
 	}
 
 	private List<BarGraphCanvas> getFrontBuffer() {
 		return buffer.get(frontBufferID);
 	}
 
 	private List<BarGraphCanvas> getBackgroundBuffer() {
 		return buffer.get((frontBufferID + 1) % 2);
 	}
 
	private void clearBuffer() {
		for (List<BarGraphCanvas> each : buffer) {
			clearBuffer(each);
		}
	}

 	private void clearBuffer(List<BarGraphCanvas> buffer) {
 		for (Widget each : buffer) {
 			each.removeFromParent();
 		}
 		buffer.clear();
 	}
 
 	@Override
 	public void draw() {
 
 		boolean needRedrawing = false;
 
 		if (needToUpdateStyle) {
 			updateStyle();
 			needRedrawing = true;
 		}
 
 		final TrackWindow newWindow = getTrackWindow();
 		panel.setPixelSize(newWindow.getPixelWidth(), style.windowHeight);
 
 		// swap the graph buffer when we have to scale the graphs
 		if (chain.getViewWindow() != null && !chain.getViewWindow().hasSameScaleWith(newWindow)) {
 			// switch the front buffer
 			frontBufferID = (frontBufferID + 1) % 2;
 			clearBuffer(getFrontBuffer());
 		}
 
 		// update the view window
 		WindowUpdateInfo updateInfo = chain.setViewWindow(newWindow);
 		GWT.log(chain.getTrackWindowList().toString());
 
 		// scale the old canvases
 		for (TrackWindow toDiscard : updateInfo.windowToDiscard) {
 			for (BarGraphCanvas each : getBackgroundBuffer()) {
 				TrackWindow old = each.getTrackWindow();
 				if (old.equals(toDiscard)) {
 					each.setTrackWindow(old.newPixelWidthWindow(newWindow.convertToPixelLength(old.getSequenceLength())),
 							newWindow.convertToPixelX(old.getStartOnGenome()));
 				}
 			}
 		}
 
 		// sort the windows in the nearest neighbor order from the view window 
 		Collections.sort(updateInfo.windowToCreate, new Comparator<TrackWindow>() {
 			public int compare(TrackWindow o1, TrackWindow o2) {
 				int d1 = Math.abs(o1.center() - newWindow.center());
 				int d2 = Math.abs(o2.center() - newWindow.center());
 				return d1 - d2;
 			}
 		});
 
 		// move the graph canvases
 		for (BarGraphCanvas each : getFrontBuffer()) {
 			int x = newWindow.convertToPixelX(each.getTrackWindow().getStartOnGenome());
 			Style.scrollX(each, x, 0.5);
 		}
 
 		// load graph
 		String filePath = resolvePropertyValues(getConfig().getString(CONFIG_PATH, ""));
 		List<BarGraphCanvas> front = getFrontBuffer();
 		for (final TrackWindow queryWindow : updateInfo.windowToCreate) {
 			final BarGraphCanvas graph = new BarGraphCanvas(queryWindow, style.windowHeight);
 			int x = newWindow.convertToPixelX(queryWindow.getStartOnGenome());
 			panel.add(graph, x, 0);
 			front.add(graph);
 
 			int s = queryWindow.getStartOnGenome();
 			int e = queryWindow.getEndOnGenome();
 			ChrLoc l = new ChrLoc(getTrackGroupProperty(UTGBProperty.TARGET), s, e);
 			getBrowserService().getCompactWigDataList(filePath, queryWindow.getPixelWidth(), l, new AsyncCallback<List<CompactWIGData>>() {
 				public void onFailure(Throwable e) {
 					error("failed to retrieve wig data: " + e.getMessage());
 					clearBackgroundGraph(queryWindow);
 				}
 
 				public void onSuccess(List<CompactWIGData> graphData) {
 					graph.setGraphData(graphData);
 					if (style.autoScale)
 						calculateScale();
 
 					// redraw scale
 					scale.draw(style, newWindow);
 
 					// draw new graph
 					graph.draw(graphData, style);
 					clearBackgroundGraph(queryWindow);
 				}
 			});
 
 		}
 
 		// scale 
 		boolean scaleHasChanged = style.autoScale && calculateScale();
 		if (needRedrawing || scaleHasChanged) {
 			// redraw scale
 			if (updateInfo.windowToCreate.isEmpty())
 				scale.draw(style, newWindow);
 
 			// redraw the already displayed graphs
 			for (BarGraphCanvas each : getFrontBuffer()) {
 				each.redraw(style);
 			}
 		}
 
 	}
 
 	void clearBackgroundGraph(TrackWindow window) {
 		for (BarGraphCanvas each : getBackgroundBuffer()) {
 			if (each.getTrackWindow().overlapWith(window)) {
 				each.removeFromParent();
 				each.clear();
 			}
 		}
 
 	}
 
 	/**
 	 * 
 	 * @return true if needs redrawing the graphs
 	 */
 	boolean calculateScale() {
 
 		if (!style.autoScale)
 			return false;
 
 		float autoScaledMinValue = 0.0f;
 		float autoScaledMaxValue = 0.0f;
 
 		final TrackWindow view = getTrackWindow();
 		for (BarGraphCanvas each : getFrontBuffer()) {
 			List<CompactWIGData> graphData = each.getGraphData();
 			if (graphData == null)
 				continue;
 
 			TrackWindow graphWindow = each.getTrackWindow();
 
 			int start = graphWindow.getStartOnGenome();
 			int s = view.convertToPixelX(start);
 
 			int pw = view.getPixelWidth();
 			int pw_e = graphWindow.getPixelWidth();
 
 			for (CompactWIGData wigData : graphData) {
 
 				int loopStart, loopEnd;
 				if (!view.isReverseStrand()) {
 					loopStart = Math.max(-s, 0);
 					loopEnd = Math.min(pw - s, pw_e);
 				}
 				else {
 					loopStart = Math.max(s - pw, 0);
 					loopEnd = Math.min(s, pw_e);
 				}
 
 				float data[] = wigData.getData();
 				for (int pos = loopStart; pos < loopEnd; pos++) {
 					autoScaledMinValue = Math.min(autoScaledMinValue, data[pos]);
 					autoScaledMaxValue = Math.max(autoScaledMaxValue, data[pos]);
 				}
 			}
 
 		}
 		GWT.log("scale: " + autoScaledMinValue + " - " + autoScaledMaxValue);
 
 		// when the graph contains no data, use the default min/max values for the scale
 		if (autoScaledMinValue == autoScaledMaxValue) {
 			autoScaledMinValue = style.minValue;
 			autoScaledMaxValue = style.maxValue;
 		}
 
 		// whether to need to update the graph?
 		boolean hasChanged = (autoScaledMinValue != style.autoScaledMin || autoScaledMaxValue != style.autoScaledMax);
 		style.autoScaledMin = autoScaledMinValue;
 		style.autoScaledMax = autoScaledMaxValue;
 
 		return hasChanged;
 	}
 
 	@Override
 	public void onChangeTrackConfig(TrackConfigChange change) {
 
 		if (change.contains(CONFIG_PATH)) {
 			needToUpdateStyle = true;
 			refresh();
 		}
 		else {
 			needToUpdateStyle = true;
 			refresh();
 		}
 	}
 
 	@Override
 	public void onChangeTrackWindow(TrackWindow newWindow) {
 		refresh();
 	}
 
 	@Override
 	public void onChangeTrackGroupProperty(TrackGroupPropertyChange change) {
 
 		if (change.containsOneOf(new String[] { UTGBProperty.TARGET, UTGBProperty.REVISION, UTGBProperty.SPECIES })) {

			chain.clear();
			clearBuffer();
 			refresh();
 		}
 	}
 
 }
