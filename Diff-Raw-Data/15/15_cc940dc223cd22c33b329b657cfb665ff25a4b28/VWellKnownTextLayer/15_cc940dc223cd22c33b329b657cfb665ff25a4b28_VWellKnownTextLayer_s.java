 package org.vaadin.vol.client.ui;
 
 import org.vaadin.vol.client.wrappers.Projection;
 import org.vaadin.vol.client.wrappers.Vector;
 import org.vaadin.vol.client.wrappers.format.WKT;
 import org.vaadin.vol.client.wrappers.layer.VectorLayer;
 
 import com.google.gwt.core.client.JsArray;
 import com.vaadin.terminal.gwt.client.ApplicationConnection;
 import com.vaadin.terminal.gwt.client.UIDL;
 
 public class VWellKnownTextLayer extends
         VAbstractAutopopulatedVectorLayer<VectorLayer> {
 
     private String wkt;
     
     @Override
     VectorLayer createLayer() {
         if (layer == null) {
             layer = VectorLayer.create(getDisplay(), getStyleMap());
         }
         return layer;
     }
 
     @Override
     public void updateFromUIDL(UIDL uidl, final ApplicationConnection client) {
         if (!uidl.hasAttribute("cached")) {
             wkt = uidl.getStringAttribute("wkt");
         }
         updateGenericVectorLayersAttributes(uidl);
         
         super.updateFromUIDL(uidl, client);
         
         getLayer().removeAllFeatures();
         
        Projection targetProjection = ((VOpenLayersMap) getParent()).getProjection();
         String projection = getProjection();
        if(projection == null) {
        	projection = "EPSG:4326";
         }
        Projection sourceProjection = Projection.get(projection);
        WKT wktFormatter = WKT.create(sourceProjection, targetProjection);
         JsArray<Vector> read = wktFormatter.read(wkt);
         for(int i = 0; i < read.length(); i++) {
             Vector vector = read.get(i);
             getLayer().addFeature(vector);
         }
         
         updateSelectionControl(client);
 
     }
 
     public String getUri() {
         return wkt;
     }
 
 
 }
