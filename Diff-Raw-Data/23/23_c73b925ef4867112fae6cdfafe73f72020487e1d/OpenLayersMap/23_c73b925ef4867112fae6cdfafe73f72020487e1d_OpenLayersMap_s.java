 /*
  * polymap.org
  * Copyright 2009, Polymap GmbH, and individual contributors as indicated
  * by the @authors tag.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 3 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  *
  */
 
 package org.polymap.openlayers.rap.widget.base_types;
 
 import org.polymap.openlayers.rap.widget.base.OpenLayersObject;
 import org.polymap.openlayers.rap.widget.controls.Control;
 import org.polymap.openlayers.rap.widget.layers.Layer;
 
 /**
  * 
  * @author Marcus -LiGi- B&uuml;schleb < mail: ligi (at) polymap (dot) de >
  * 
  */
 
 public class OpenLayersMap extends OpenLayersObject {
 
     /** Event: triggered after mouseover the map. */
     public static String        EVENT_MOUSE_OVER = "mouseover";
     
     /** Event: triggered after mouseout the map. */
     public static String        EVENT_MOUSE_OUT = "mouseout";
     
     /** Event: triggered after mousemove the map. */
     public static String        EVENT_MOUSE_MOVE = "mousemove";
     
 	public OpenLayersMap() {
 		super.create ("new OpenLayers.Map( { div : document.getElementById(this._id),	controls : []	});");
 	}
 	
 	public void addLayer(Layer layer2add) {
 		super.addObjModCode("addLayer",layer2add);
 	}
 
 	public void removeLayer(Layer layer2rm) {
 		super.addObjModCode("removeLayer",layer2rm);
 	}
 		
 	public void addControl(Control control2add) {
 		control2add.has_map=true;
 		super.addObjModCode("addControl",control2add);
 	}
 
 	public void setProxy(String proxy){
 		super.addObjModCode("OpenLayers.ProxyHost='"+proxy+"';");
 	}
 	
 	public void removeControl(Control control2rm) {
 		super.addObjModCode("removeControl",control2rm);
 	}
 
 	public void zoomTo(int zoom) {
 		super.addObjModCode("zoomTo",zoom);
 	}
 
 
 	public void zoomToExtent(Bounds extent , boolean closest) {
 		super.addObjModCode("zoomToExtent",extent,closest);
 	}
 	
 	public void zoomToScale(double scale , boolean closest) {
 		super.addObjModCode("zoomToScale",scale,closest);
 	}
 	
 	public void setCenter(double center_lon, double center_lat) {
 		super.addObjModCode("setCenter", new LonLat(center_lon,center_lat));
 	}
 
 	public void setBaseLayer(Layer layer) {
 		super.addObjModCode("setBaseLayer",layer);
 	}
 	
 	public void setProjection(Projection projection){
 		super.setObjAttr("projection", projection);
 	}
 	
 
	public void setDisplayProjection(Projection projection){
		super.setObjAttr("displayProjection", projection);
	}
 }
