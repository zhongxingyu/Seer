 package org.icefaces.mobi.component.gmap;
 
 import java.io.IOException;
 import java.util.Iterator;
 
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 
 import org.icefaces.mobi.renderkit.CoreRenderer;
 
 public class GMapRenderer extends CoreRenderer {
 
 	public void encodeBegin(FacesContext context, UIComponent component)
 			throws IOException {
 		ResponseWriter writer = context.getResponseWriter();
 		String clientId = component.getClientId(context);
 		GMap gmap = (GMap) component;
 		writer.startElement("div", null);
 		writer.writeAttribute("id", clientId, null);
		writer.writeAttribute("style", "height: 97%;", null);
 		writer.endElement("div");
 		writer.startElement("span", null);
 		writer.writeAttribute("id", clientId + "_script", null);
 		writer.startElement("script", null);
 		writer.writeAttribute("type", "text/javascript", null);
 		writer.write(String.format("var wrapper = mobi.gmap.create('%s');",clientId));
 		if ((gmap.isLocateAddress() || !gmap.isIntialized())
 				&& (gmap.getAddress() != null && gmap.getAddress().length() > 2)) {
 			writer.write(String.format(
 					"wrapper.locateAddress('%s');",gmap.getAddress()));
 		} else {
 			writer.write(String
 					.format("wrapper.getMap().setCenter(new google.maps.LatLng(%s,%s));",
 							gmap.getLatitude(), gmap.getLongitude()));
 		}
 		writer.write(String.format(
 				"wrapper.getMap().setZoom(%s);", gmap.getZoomLevel()));
 		writer.write(String.format(
 				"wrapper.setMapType('%s');",gmap.getType().toUpperCase()));
 		if (gmap.getOptions() != null && gmap.getOptions().length() > 1) {
 			writer.write(String.format(
 					"wrapper.addOptions('%s');", gmap.getOptions()));
 		}
 		writer.endElement("script");
 		writer.endElement("span");
 		gmap.setIntialized(true);
 	}
 
 	@Override
 	public void encodeChildren(FacesContext context, UIComponent component)
 			throws IOException {
 		if (context == null || component == null) {
 			throw new NullPointerException();
 		}
 		if (component.getChildCount() == 0)
 			return;
 		Iterator kids = component.getChildren().iterator();
 		while (kids.hasNext()) {
 			UIComponent kid = (UIComponent) kids.next();
 			kid.encodeBegin(context);
 			if (kid.getRendersChildren()) {
 				kid.encodeChildren(context);
 			}
 			kid.encodeEnd(context);
 		}
 
 	}
 
 	@Override
 	public boolean getRendersChildren() {
 		return true;
 	}
 	
 }
