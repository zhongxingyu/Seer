 package nl.sense_os.commonsense.client.visualization.map.components;
 
 import java.util.Date;
 import java.util.Map;
 
 import nl.sense_os.commonsense.client.utility.Log;
 import nl.sense_os.commonsense.shared.sensorvalues.JsonValueModel;
 import nl.sense_os.commonsense.shared.sensorvalues.SensorValueModel;
 
 import com.extjs.gxt.ui.client.Style.Scroll;
 import com.extjs.gxt.ui.client.data.TreeModel;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.event.SliderEvent;
 import com.extjs.gxt.ui.client.widget.ContentPanel;
 import com.extjs.gxt.ui.client.widget.Slider;
 import com.extjs.gxt.ui.client.widget.layout.FitLayout;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.maps.client.MapWidget;
 import com.google.gwt.maps.client.control.LargeMapControl;
 import com.google.gwt.maps.client.geom.LatLng;
 import com.google.gwt.maps.client.overlay.Marker;
 import com.google.gwt.maps.client.overlay.Polyline;
 import com.google.gwt.user.client.ui.DockLayoutPanel;
 
 public class MapPanel extends ContentPanel {
 
 	private static final String TAG = "MapPanel";
 	private MapWidget map;
 	private Slider slider;
 	private SensorValueModel[] sensorData;
 	private static final int timeRange = 720;	// in seconds
 
 	
 	public MapPanel() {
 		// Panel settings. 
 		setLayout(new FitLayout());
 		setHeaderVisible(false);
 		setBodyBorder(false);
 		setScrollMode(Scroll.NONE);
 		setId("viz-map");
 
 		// Create the map and slider.
 		initMap();
 	}
 
 	/**
 	 * Create a google map with an slider on the bottom to filter
 	 * the points to draw according to a time specified with the
 	 * slider.
 	 */
 	private void initMap() {
 		// Create the map.
 		map = new MapWidget();
 		map.setSize("100%", "100%");
 
 		// Add some controls for the zoom level
 		map.addControl(new LargeMapControl());
 		map.setScrollWheelZoomEnabled(true);
 
 		final DockLayoutPanel dock = new DockLayoutPanel(Unit.PX);
 		dock.addNorth(map, 500);
 
 		// Slider added below the map.		
 		slider = new Slider();
 		slider.setWidth(400);
 		slider.setHeight(50);
 		slider.setMinValue(0);
 		slider.setMaxValue(30);
 		slider.setIncrement(1);
 		slider.setMessage("{0} x 720 secs");	// type of unit not defined yet 
 		slider.setId("viz-map-slider");
 		dock.addSouth(slider, 30);
 
 		// Listener to filter the points to draw on the map.
 		slider.addListener(Events.Change, new Listener<SliderEvent>() {
 			@Override
 			public void handleEvent(SliderEvent be) {
 				int newValue = be.getNewValue();
 				updateMap(newValue);
 			}
 		});
 
 		// Add the map and the slider to the panel.
 		this.add(dock);
 	}
 
 	/**
 	 * This method is called when it used the slider below the map.
 	 * It filters the points to draw a line depending on the time.
 	 * 
	 * @param time
 	 */
	private void updateMap(int time) {
 		// Clean the map.
 		map.clearOverlays();
 
 		// Draw the filtered points.
 		if (sensorData.length > 0) {
 			LatLng[] points = new LatLng[sensorData.length];
 
 			long timeFilter = 0;
 
			if (time != 0) {
 				JsonValueModel v = (JsonValueModel) sensorData[sensorData.length - 1];
 				Date t = v.getTimestamp();
 				
 				// time filter in secs
 				timeFilter = t.getTime() / 1000;
				timeFilter -= (timeRange * time);				
 			}
 
 			int lastPoint = 0;
 
 			for (int i = 0, j = 0; i < sensorData.length; i++) {
 				JsonValueModel value = (JsonValueModel) sensorData[i];
 				Map<String, Object> fields = value.getFields();
 
 				double latitude = (Double) fields.get("latitude");
 				double longitude = (Double) fields.get("longitude");
 				
 				// timestamp in secs
 				long timestamp = value.getTimestamp().getTime() / 1000;
 				
 				Log.d(TAG, "timeFilter: "+timeFilter);
 				Log.d(TAG, "timestamp: "+timestamp);
 				
 				if (timestamp > timeFilter) {
 					lastPoint = j;
 					points[j++] = LatLng.newInstance(latitude, longitude);
 				}
 			}
 
 			// Add the first marker
 			Marker startMarker = new Marker(points[0]);
 			map.addOverlay(startMarker);
 
 			// Add the last marker
 			Marker endMarker = new Marker(points[lastPoint]);
 			map.addOverlay(endMarker);
 
 			// Draw a track line
 			Polyline trace = new Polyline(points);
 			map.addOverlay(trace);
 
 			// Center the map
 			map.setCenter(endMarker.getLatLng());
 			map.setZoomLevel(13);
 		}
 	}
 
 	/**
 	 * 
 	 * @param sensor
 	 * @param data
 	 */
 	public void addData(TreeModel sensor, SensorValueModel[] data) {
 
 		sensorData = data;
 
 		if (data.length > 0) {
 			
 			// Store the points in an array.
 			LatLng[] points = new LatLng[data.length];
 
 			for (int i = 0, j = 0; i < data.length; i++) {
 				JsonValueModel value = (JsonValueModel) data[i];
 				Map<String, Object> fields = value.getFields();
 
 				double latitude = (Double) fields.get("latitude");
 				double longitude = (Double) fields.get("longitude");
 
 				points[i] = LatLng.newInstance(latitude, longitude);
 			}
 
 			// Add the first marker
 			Marker startMarker = new Marker(points[0]);
 			map.addOverlay(startMarker);
 
 			// Add the last marker
 			Marker endMarker = new Marker(points[points.length - 1]);
 			// Marker endMarker = new Marker(points[lastElem]);
 			map.addOverlay(endMarker);
 
 			// Draw a track line through the points.
 			Polyline trace = new Polyline(points);
 			map.addOverlay(trace);
 
 			// Center based on both markers
 			/*
 			 * LatLngBounds bounds = map.getBounds();
 			 * bounds.extend(startMarker.getLatLng());
 			 * bounds.extend(endMarker.getLatLng()); int zoomLevel =
 			 * map.getBoundsZoomLevel(bounds); Log.d(TAG, "zoom level: " +
 			 * zoomLevel);
 			 */
 
 			/*
 			 * if (zoomLevel < 9) map.setZoomLevel(zoomLevel); else
 			 * map.setZoomLevel(12);
 			 * 
 			 * //map.setCenter(bounds.getCenter());
 			 * map.setCenter(bounds.getCenter(),
 			 * map.getBoundsZoomLevel(bounds));
 			 */
 
 			map.setCenter(endMarker.getLatLng());
 			map.setZoomLevel(13);
 
 		} else {
 			Log.d(TAG, "no data.");
 		}
 	}
 
 	/**
 	 * Should display something to show the user that we are done loading data.
 	 */
 	public void finishLoading() {
 		Log.d(TAG, "Finished loading!");
 	}
 
 }
