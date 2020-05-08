 package org.vaadin.vol.client.ui;
 
 import org.vaadin.vol.client.wrappers.GwtOlHandler;
 import org.vaadin.vol.client.wrappers.Icon;
 import org.vaadin.vol.client.wrappers.LonLat;
 import org.vaadin.vol.client.wrappers.Map;
 import org.vaadin.vol.client.wrappers.Marker;
 import org.vaadin.vol.client.wrappers.Projection;
 import org.vaadin.vol.client.wrappers.Size;
 import org.vaadin.vol.client.wrappers.layer.MarkerLayer;
 
 import com.google.gwt.core.client.JsArray;
 import com.google.gwt.dom.client.Document;
 import com.google.gwt.user.client.ui.Widget;
 import com.vaadin.terminal.gwt.client.ApplicationConnection;
 import com.vaadin.terminal.gwt.client.Paintable;
 import com.vaadin.terminal.gwt.client.UIDL;
 
 public class VMarker extends Widget implements Paintable {
 
 	private Marker marker;
 
 	public VMarker() {
 		setElement(Document.get().createDivElement());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.vaadin.terminal.gwt.client.Paintable#updateFromUIDL(com.vaadin.terminal
 	 * .gwt.client.UIDL, com.vaadin.terminal.gwt.client.ApplicationConnection)
 	 */
	public void updateFromUIDL(UIDL childUIDL, final ApplicationConnection client) {
 		if (client.updateComponent(this, childUIDL, false)) {
 			return;
 		}
 		if (marker != null) {
 			getLayer().removeMarker(marker);
 		}
 		double lon = childUIDL.getDoubleAttribute("lon");
 		double lat = childUIDL.getDoubleAttribute("lat");
 		final Projection projection = Projection.get(childUIDL
 				.getStringAttribute("pr"));
 		LonLat point = LonLat.create(lon, lat);
 		Projection projection2 = getMap().getProjection();
 		point.transform(projection, projection2);
 
 		Icon icon = null;
		if (childUIDL.hasAttribute("icon_url")) {
			String url = childUIDL.getStringAttribute("icon_url");
			int width = childUIDL.getIntAttribute("icon_w");
			int height = childUIDL.getIntAttribute("icon_h");
 			icon = Icon.create(url, Size.create(width, height));
 		}
 		marker = Marker.create(point, icon);
 
 		if (client.hasEventListeners(this, "click")) {
 			marker.addClickHandler(new GwtOlHandler() {
 				public void onEvent(JsArray arguments) {
					client.updateVariable(client.getPid(VMarker.this), "click", "", true);
 				}
 			});
 		}
 
 		getLayer().addMarker(marker);
 	}
 
 	private MarkerLayer getLayer() {
 		return (MarkerLayer) ((VMarkerLayer) getParent()).getLayer();
 	}
 
 	private Map getMap() {
 		return ((VOpenLayersMap) getParent().getParent().getParent()).getMap();
 	}
 
 	public Marker getMarker() {
 		return marker;
 	}
 
 }
