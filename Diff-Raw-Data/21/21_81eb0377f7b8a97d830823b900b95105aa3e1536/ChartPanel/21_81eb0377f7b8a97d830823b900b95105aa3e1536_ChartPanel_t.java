 /*
  * Copyright 2012 Danish Maritime Authority. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *
  *   1. Redistributions of source code must retain the above copyright notice,
  * this list of conditions and the following disclaimer.
  *
  *   2. Redistributions in binary form must reproduce the above copyright notice,
  * this list of conditions and the following disclaimer in the documentation and/or
  * other materials provided with the distribution.
  * 
  * THIS SOFTWARE IS PROVIDED BY Danish Maritime Safety Administration ``AS IS'' 
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> OR CONTRIBUTORS BE LIABLE FOR
  * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
  * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
  * The views and conclusions contained in the software and documentation are those
  * of the authors and should not be interpreted as representing official policies,
  * either expressed or implied, of Danish Maritime Authority.
  * 
  */
 package dk.dma.aiscoverage.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Point;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 import java.util.List;
 import java.util.Properties;
 
 import javax.swing.BorderFactory;
 
 import org.apache.log4j.Logger;
 
 import com.bbn.openmap.BufferedLayerMapBean;
 import com.bbn.openmap.Layer;
 import com.bbn.openmap.LayerHandler;
 import com.bbn.openmap.MapBean;
 import com.bbn.openmap.MapHandler;
 import com.bbn.openmap.MouseDelegator;
 import com.bbn.openmap.event.NavMouseMode;
 import com.bbn.openmap.gui.OMComponentPanel;
 import com.bbn.openmap.layer.shape.ShapeLayer;
 import com.bbn.openmap.proj.Proj;
 import com.bbn.openmap.proj.Projection;
 import com.bbn.openmap.proj.coords.LatLonPoint;
 
 import dk.dma.aiscoverage.acv.ACV;
 import dk.dma.aiscoverage.event.NavigationMouseMode;
 import dk.dma.aiscoverage.openmap.layers.BaseStationLayer;
 import dk.dma.aiscoverage.openmap.layers.CoverageLayer;
 import dk.dma.aiscoverage.openmap.layers.DensityPlotLayer;
 import dk.dma.aiscoverage.settings.MapSettings;
 import dk.frv.ais.geo.GeoLocation;
 
 /**
  * The panel with chart. Initializes all layers to be shown on the map. 
  */
 public class ChartPanel extends OMComponentPanel implements MouseWheelListener {
 
 	private static final long serialVersionUID = 1L;
 	private static final Logger LOG = Logger.getLogger(ChartPanel.class);
 	
 	private MapHandler mapHandler;
 	private LayerHandler layerHandler;
 	private BufferedLayerMapBean map;
 	private Layer encLayer;
 	private Layer bgLayer;
 	private CoverageLayer coverageLayer;
 	private BaseStationLayer basestationLayer;
 	private NavigationMouseMode mapNavMouseMode;
 	private MouseDelegator mouseDelegator;
 	public int maxScale = 5000;
 	private DensityPlotLayer densityPlotLayer;
	private Color background = new Color(168, 228, 255);
 
 
 	public DensityPlotLayer getDensityPlotLayer() {
 		return densityPlotLayer;
 	}
 
 
 	public ChartPanel() {
 		super();
 		// Set map handler
 		mapHandler = ACV.getMapHandler();
 		// Set layout
 		setLayout(new BorderLayout());
 		// Set border
 		setBorder(BorderFactory.createLineBorder(Color.GRAY));
 		// Max scale
 		this.maxScale = ACV.getSettings().getMapSettings().getMaxScale(); 
 	}
 
 	public void initChart() {
 		MapSettings mapSettings = ACV.getSettings().getMapSettings();
 		Properties props = ACV.getProperties();
 		
 
 		// Create a MapBean, and add it to the MapHandler.
 		map = new BufferedLayerMapBean();
 		map.setDoubleBuffered(true);
 
 		mouseDelegator = new MouseDelegator();
 		mapHandler.add(mouseDelegator);
 
 		// Add MouseMode. The MouseDelegator will find it via the
 		// MapHandler.
 		// Adding NavMouseMode first makes it active.
 		 mapHandler.add(new NavMouseMode());
 		 
 		 //Mouse mode	
 		mapNavMouseMode = new NavigationMouseMode(this);
 		mouseDelegator.addMouseMode(mapNavMouseMode);
 		mouseDelegator.setActive(mapNavMouseMode);
 		mapHandler.add(mapNavMouseMode);
 
 		
 
 
 		// Use the LayerHandler to manage all layers, whether they are
 		// on the map or not. You can add a layer to the map by
 		// setting layer.setVisible(true).
 		layerHandler = new LayerHandler();
 		
 		// Get plugin layers
 		createPluginLayers(props);
 
		map.setBackground(background);
		
 		// Add layer handler to map handler
 		mapHandler.add(layerHandler);
 
 		// Create background layer
 		String layerName = "background";
 		bgLayer = new ShapeLayer();
 		bgLayer.setProperties(layerName, props);
 		bgLayer.setAddAsBackground(true);
 		bgLayer.setVisible(true);
 		mapHandler.add(bgLayer);
 
 		if (encLayer != null) {
 			mapHandler.add(encLayer);
 		}
 
 
 		
 		// Add map to map handler
 		mapHandler.add(map);
 
 		// Set last postion
 		map.setCenter(mapSettings.getCenter());
 
 		// Get from settings
 		map.setScale(mapSettings.getScale());
 
 		add(map);
 		
 		// Add coverage layer
 		coverageLayer = new CoverageLayer();
 		coverageLayer.setVisible(true);
 		mapHandler.add(coverageLayer);
 		
 		//add basestation layer
 		basestationLayer = new BaseStationLayer();
 		basestationLayer.setVisible(true);
 		mapHandler.add(basestationLayer);
 		
 		//add density plot layer
 		densityPlotLayer = new DensityPlotLayer();
 		mapHandler.add(densityPlotLayer);
 		densityPlotLayer.setVisible(false);
 		
 	}
 
 	public void saveSettings() {
 		MapSettings mapSettings = ACV.getSettings().getMapSettings();
 		mapSettings.setCenter((LatLonPoint) map.getCenter());
 		mapSettings.setScale(map.getScale());
 	}
 
 	public MapBean getMap() {
 		return map;
 	}
 
 	public MapHandler getMapHandler() {
 		return mapHandler;
 	}
 
 	public Layer getEncLayer() {
 		return encLayer;
 	}
 
 	public void doZoom(float factor) {
 		float newScale = map.getScale() * factor;
 		if (newScale < maxScale) {
 			newScale = maxScale;
 		}
 		map.setScale(newScale);
 	}
 
 	
 
 	public void encVisible(boolean visible) {
 		if (encLayer != null) {
 			encLayer.setVisible(visible);
 		}
 	}
 
 	/**
 	 * Given a set of points scale and center so that all points are contained
 	 * in the view
 	 * 
 	 * @param waypoints
 	 */
 	public void zoomTo(List<GeoLocation> waypoints) {
 		if (waypoints.size() == 0) {
 			return;
 		}
 
 		if (waypoints.size() == 1) {
 			map.setCenter(waypoints.get(0).getLatitude(), waypoints.get(0).getLongitude());
 			return;
 		}
 
 		// Find bounding box
 		double maxLat = -91;
 		double minLat = 91;
 		double maxLon = -181;
 		double minLon = 181;
 		for (GeoLocation pos : waypoints) {
 			if (pos.getLatitude() > maxLat) {
 				maxLat = pos.getLatitude();
 			}
 			if (pos.getLatitude() < minLat) {
 				minLat = pos.getLatitude();
 			}
 			if (pos.getLongitude() > maxLon) {
 				maxLon = pos.getLongitude();
 			}
 			if (pos.getLongitude() < minLon) {
 				minLon = pos.getLongitude();
 			}
 		}
 
 		double centerLat = (maxLat + minLat) / 2.0;
 		double centerLon = (maxLon + minLon) / 2.0;
 		map.setCenter(centerLat, centerLon);
 
 	}
 	
 	public MouseDelegator getMouseDelegator() {
 		return mouseDelegator;
 	}
 
 	private void createPluginLayers(Properties props) {
 		String layersValue = props.getProperty("acv.plugin_layers");
 		if (layersValue == null)
 			return;
 		String[] layerNames = layersValue.split(" ");
 		for (String layerName : layerNames) {
 			String classProperty = layerName + ".class";
 			String className = props.getProperty(classProperty);
 			if (className == null) {
 				LOG.error("Failed to locate property " + classProperty);
 				continue;
 			}
 			try {
 				// Create it if you do...
 				Object obj = java.beans.Beans.instantiate(null, className);
 				if (obj instanceof Layer) {
 					Layer l = (Layer) obj;
 					// All layers have a setProperties method, and
 					// should intialize themselves with proper
 					// settings here. If a property is not set, a
 					// default should be used, or a big, graceful
 					// complaint should be issued.
 					l.setProperties(layerName, props);
 					l.setVisible(true);
 					layerHandler.addLayer(l);
 				}
 			} catch (java.lang.ClassNotFoundException e) {
 				LOG.error("Layer class not found: \"" + className + "\"");
 			} catch (java.io.IOException e) {
 				LOG.error("IO Exception instantiating class \"" + className + "\"");
 			}
 		}
 	}
 	
 	public int getMaxScale() {
 		return maxScale;
 	}
 	
 
 	
 	
 	@Override
 	public void findAndInit(Object obj) {
 		//This is used in case we need to communicate with other handler objects such as AIS 
 	}
 
 	/**
 	 * Call auto follow when zooming
 	 */
 	@Override
 	public void mouseWheelMoved(MouseWheelEvent e) {
 
 	}
 	
 	/**
 	 * 
 	 * @param direction
 	 * 1 == Up
 	 * 2 == Down
 	 * 3 == Left
 	 * 4 == Right
 	 * 
 	 * Moving by 100 units in each direction
 	 * Map center is [745, 445]
 	 */
 	  public void pan(int direction) {
 		Point point = null;
 		Projection projection = map.getProjection();
 
 		int width = projection.getWidth();
 		int height = projection.getHeight();
 		
 	    switch (direction) {
 	    	case 1:  point = new Point(width/2,height/2-100);	break;
         	case 2:  point = new Point(width/2,height/2+100);	break;
             case 3:  point = new Point(width/2-100,height/2);	break;
             case 4:  point = new Point(width/2+100,height/2);	break;
 	        }
 	    
 	    
 	    
         
         Proj p = (Proj) projection;
         LatLonPoint llp = projection.inverse(point);
         p.setCenter(llp);
         map.setProjection(p);
 	   }
 
 	  
 	  
 	    
 }
