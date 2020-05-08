 package org.vaadin.vol.client.wrappers.format;
 
 import org.vaadin.vol.client.wrappers.Projection;
 import org.vaadin.vol.client.wrappers.Vector;
 
 import com.google.gwt.core.client.JavaScriptObject;
 import com.google.gwt.core.client.JsArray;
 
 public class WKT extends JavaScriptObject {
	
	protected WKT() {}
     
     public static native WKT create(Projection internalProjection, Projection externalProjection)
     /*-{
         return new $wnd.OpenLayers.Format.WKT({'internalProjection' : internalProjection, 'externalProjection' : externalProjection});
     }-*/;
 
     public native final JsArray<Vector> read(String wkt) 
     /*-{
         var result = this.read(wkt);
         // ensure array
         if(!result.lenght) {
             result = [result];
         }
         return result;
     }-*/;
 
 	public native final String write(Vector vector) 
 	/*-{
 		return this.write(vector);
 	}-*/;
 
 }
