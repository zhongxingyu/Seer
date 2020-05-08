 package teropa.globetrotter.client;
 
 import java.util.ArrayList;
 import java.util.List;
 
import teropa.globetrotter.client.common.LonLat;
 import teropa.globetrotter.client.common.Position;
 import teropa.globetrotter.client.controls.CopyrightText;
 import teropa.globetrotter.client.controls.Panner;
 import teropa.globetrotter.client.controls.Zoomer;
 import teropa.globetrotter.client.marker.Marker;
 import teropa.globetrotter.client.marker.MarkerClickEvent;
 import teropa.globetrotter.client.marker.MarkerLayer;
 import teropa.globetrotter.client.osm.OpenStreetMapLayer;
 import teropa.globetrotter.client.proj.GoogleMercator;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.event.logical.shared.ResizeEvent;
 import com.google.gwt.event.logical.shared.ResizeHandler;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.RootPanel;
 
 public class Demo implements EntryPoint {
 	
 	public void onModuleLoad() {
 		final Map map = new Map("100%", "100%");
 		map.setResolutions(OpenStreetMapLayer.SUPPORTED_RESOLUTIONS, 4);
 		map.setMaxExtent(GoogleMercator.MAX_EXTENT);
 		
 		initBaseLayer(map);
 		initMarkerLayer(map);
 		initControls(map);
 
 		// Tell the map to recalculate its layout when window size changes
 		Window.addResizeHandler(new ResizeHandler() {
 			public void onResize(ResizeEvent event) {
 				map.onResize();
 			}
 		});
 		
 		RootPanel.get("container").add(map);
 	}
 
 	private void initBaseLayer(final Map map) {
 		OpenStreetMapLayer base = new OpenStreetMapLayer("http://tile.openstreetmap.org/", "Mapnik", true);
 		map.addLayer(base);
 	}
 
 	private void initMarkerLayer(final Map map) {
 		MarkerLayer markerLayer = new MarkerLayer("Capitals");
 		
 		List<Marker> markers = new ArrayList<Marker>();
 		for (DemoCities.City city : DemoCities.CITIES) {
 			markers.add(new Marker(city.getLonLat()));
 		}
 		markerLayer.addMarkers(markers);
 
 		markerLayer.addMarkerClickHandler(new MarkerClickEvent.Handler() {
 			public void onMarkerClick(MarkerClickEvent evt) {
 				Window.alert("Marker clicked.");
 			}
 		});
 
 		map.addLayer(markerLayer);
 		
 	}
 
 	private void initControls(final Map map) {
 		map.addControl(new Panner(), Position.TOP_LEFT);
 		map.addControl(new Zoomer(), Position.MIDDLE_LEFT);
 		
 		HTML copy = new HTML("(c) <a href=\"http://www.openstreetmap.org/\">OpenStreetMap</a> (and) contributors, <a href=\"http://creativecommons.org/licenses/by-sa/2.0/\">CC-BY-SA</a>");
 		map.addControl(new CopyrightText(copy), Position.BOTTOM_LEFT);
 	}
 
 }
