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
 // GenomeTrack.java
 // Since: Feb 17, 2009
 //
 // $URL$ 
 // $Author$
 //--------------------------------------
 package org.utgenome.gwt.utgb.client.track.lib;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.utgenome.gwt.utgb.client.bio.Coordinate;
 import org.utgenome.gwt.utgb.client.db.datatype.StringType;
 import org.utgenome.gwt.utgb.client.track.Design;
 import org.utgenome.gwt.utgb.client.track.Track;
 import org.utgenome.gwt.utgb.client.track.TrackBase;
 import org.utgenome.gwt.utgb.client.track.TrackConfig;
 import org.utgenome.gwt.utgb.client.track.TrackConfigChange;
 import org.utgenome.gwt.utgb.client.track.TrackFrame;
 import org.utgenome.gwt.utgb.client.track.TrackGroup;
 import org.utgenome.gwt.utgb.client.track.TrackGroupPropertyChange;
 import org.utgenome.gwt.utgb.client.track.TrackWindow;
 import org.utgenome.gwt.utgb.client.track.UTGBProperty;
 import org.utgenome.gwt.utgb.client.util.CanonicalProperties;
 import org.utgenome.gwt.utgb.client.util.Properties;
 import org.utgenome.gwt.utgb.client.util.StringUtil;
 import org.utgenome.gwt.widget.client.Style;
 
 import com.google.gwt.event.dom.client.ErrorEvent;
 import com.google.gwt.event.dom.client.ErrorHandler;
 import com.google.gwt.event.dom.client.LoadEvent;
 import com.google.gwt.event.dom.client.LoadHandler;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.Frame;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * GenomeTrack is for visualizing data that can be located on genome sequences.
  * 
  * @author leo
  * 
  */
 public class GenomeTrack extends TrackBase {
 
 	protected String type = "image";
 	private String trackURL;
 	protected int leftMargin = 0;
 
 	protected FlexTable layoutPanel = new FlexTable();
 	protected Image trackImage = new Image();
 	protected Frame frame = new Frame();
 	protected boolean isWidgetReady = false;
 
 	protected Set<String> queryParams = new HashSet<String>();
 
 	public static TrackFactory factory() {
 		return new TrackFactory() {
 			@Override
 			public Track newInstance() {
 				return new GenomeTrack();
 			}
 		};
 	}
 
 	public GenomeTrack() {
 		this("Genome Track");
 	}
 
 	public GenomeTrack(String string) {
 		super(string);
 
 		Style.fullWidth(layoutPanel);
 		layoutPanel.setCellPadding(0);
 		layoutPanel.setCellSpacing(0);
 		Style.fontSize(layoutPanel, 0);
 
 		Style.fullWidth(frame);
 		Style.margin(frame, 0);
 		Style.padding(frame, 0);
 		DOM.setElementProperty(frame.getElement(), "align", "left");
 		DOM.setElementPropertyInt(frame.getElement(), "marginHight", 0);
 		DOM.setElementPropertyInt(frame.getElement(), "marginWidth", 0);
 		DOM.setElementPropertyInt(frame.getElement(), "frameBorder", 0);
 	}
 
 	public Widget getWidget() {
 		return layoutPanel;
 	}
 
 	@Override
 	public void draw() {
 
 		if (leftMargin > 0)
 			layoutPanel.getCellFormatter().setWidth(0, 0, leftMargin + "px");
 
		trackURL = getTrackURL();
 		getConfig().setParameter("trackURL", trackURL);
 
 		if (type.equals("frame")) {
 			if (!isWidgetReady) {
 				layoutPanel.setWidget(0, 1, frame);
 				isWidgetReady = true;
 			}
 			frame.setUrl(trackURL);
 		}
 		else if (type.equals("image")) {
 			// use image 
 			if (!isWidgetReady) {
 				layoutPanel.setWidget(0, 1, trackImage);
 				isWidgetReady = true;
 			}
 
 			trackImage.setUrl(trackURL);
 			getFrame().setNowLoading();
 		}
 	}
 
 	protected String getTrackURL() {
 		Coordinate c = getCoordinate();
 
 		Properties p = new Properties();
 		TrackWindow w = getTrackGroup().getTrackWindow();
 		//		p.add("start", w.getStartOnGenome());
 		//		p.add("end", w.getEndOnGenome());
 		//		p.add("width", w.getPixelWidth() - leftMargin);
 
 		for (String key : queryParams) {
 			// override the group property using the corresponding config parameter 
 			String v = getConfig().getParameter(key);
 			if (v == null) {
 				v = getTrackGroup().getProperty(key);
 			}
 			if (v != null)
 				p.add(key, v);
 		}
 
 		String trackBaseURL = getConfig().getString(CONFIG_TRACK_BASE_URL, "utgb-core/roundcircle");
 		return resolvePropertyValues(c.getTrackURL(trackBaseURL, p));
 	}
 
 	@Override
 	public void setUp(TrackFrame trackFrame, TrackGroup group) {
 
 		trackImage.addLoadHandler(new LoadHandler() {
 			public void onLoad(LoadEvent arg0) {
 				getFrame().onUpdateTrackWidget();
 				getFrame().loadingDone();
 			}
 		});
 		trackImage.addErrorHandler(new ErrorHandler() {
 			public void onError(ErrorEvent arg0) {
 				trackImage.setUrl(Design.IMAGE_NOT_AVAILABLE);
 				getFrame().loadingDone();
 			}
 		});
 
 		int height = this.getDefaultWindowHeight();
 		if (height > 0) {
 			frame.setHeight(height + "px");
 			layoutPanel.setHeight(height + "px");
 		}
 
 		// set up the configuration panel
 		TrackConfig config = getConfig();
 		config.addConfig("Track Base URL", new StringType(CONFIG_TRACK_BASE_URL), "");
 		config.addConfig("Track URL", new StringType("trackURL"), trackURL);
 	}
 
 	public static final String CONFIG_TRACK_BASE_URL = "trackBaseURL";
 	public static final String CONFIG_TRACK_TYPE = "type";
 
 	@Override
 	public void onChange(TrackGroupPropertyChange change, TrackWindow newWindow) {
 
 		if (monitorWindowChange && newWindow != null) {
 			draw();
 			return;
 		}
 
 		if (change == null)
 			return;
 
 		if (monitorCoordinateChange) {
 			if (change.containsOneOf(UTGBProperty.coordinateParameters))
 				draw();
 		}
 		else if (change.containsOneOf(queryParams))
 			draw();
 
 	}
 
 	@Override
 	public void onChangeTrackConfig(TrackConfigChange change) {
 		if (change.contains(CONFIG_TRACK_BASE_URL)) {
 			draw();
 		}
 	}
 
 	private boolean monitorCoordinateChange = true;
 	private boolean monitorWindowChange = true;
 
 	@Override
 	public void saveProperties(CanonicalProperties saveData) {
 		super.saveProperties(saveData);
 		String q = StringUtil.joinIterable(queryParams, ",");
 		saveData.add("queryParams", q);
 	}
 
 	@Override
 	public void restoreProperties(CanonicalProperties properties) {
 		super.restoreProperties(properties);
 
 		leftMargin = properties.getInt("leftMargin", leftMargin);
 		type = properties.get(CONFIG_TRACK_TYPE, type);
 		monitorCoordinateChange = properties.getBoolean("monitorCoordinateChange", monitorCoordinateChange);
 		monitorWindowChange = properties.getBoolean("monitorWindowChange", monitorWindowChange);
 
 		queryParams.clear();
 		String q = properties.get("queryParams");
 		if (q != null) {
 			String[] params = q.split(",");
 			if (params != null) {
 				for (String each : params)
 					queryParams.add(each);
 			}
 		}
 
 	}
 }
