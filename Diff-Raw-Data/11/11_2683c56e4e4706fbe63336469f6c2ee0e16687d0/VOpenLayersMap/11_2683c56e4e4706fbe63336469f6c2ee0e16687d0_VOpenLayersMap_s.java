 package org.vaadin.vol.client.ui;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 import org.vaadin.vol.client.wrappers.Bounds;
 import org.vaadin.vol.client.wrappers.GwtOlHandler;
 import org.vaadin.vol.client.wrappers.JsObject;
 import org.vaadin.vol.client.wrappers.LonLat;
 import org.vaadin.vol.client.wrappers.Map;
 import org.vaadin.vol.client.wrappers.Pixel;
 import org.vaadin.vol.client.wrappers.Projection;
 import org.vaadin.vol.client.wrappers.control.Control;
 
 import com.google.gwt.core.client.JsArray;
 import com.google.gwt.event.dom.client.ContextMenuEvent;
 import com.google.gwt.event.dom.client.ContextMenuHandler;
 import com.google.gwt.user.client.Element;
 import com.google.gwt.user.client.Event;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.vaadin.terminal.gwt.client.ApplicationConnection;
 import com.vaadin.terminal.gwt.client.Container;
 import com.vaadin.terminal.gwt.client.Paintable;
 import com.vaadin.terminal.gwt.client.RenderSpace;
 import com.vaadin.terminal.gwt.client.UIDL;
 import com.vaadin.terminal.gwt.client.Util;
 import com.vaadin.terminal.gwt.client.VConsole;
 import com.vaadin.terminal.gwt.client.ui.Action;
 import com.vaadin.terminal.gwt.client.ui.ActionOwner;
 import com.vaadin.terminal.gwt.client.ui.TreeAction;
 
 /**
  * Client side widget which communicates with the server. Messages from the
  * server are shown as HTML and mouse clicks are sent to the server.
  */
 public class VOpenLayersMap extends FlowPanel implements Container, ActionOwner {
 
     /** Set the CSS class name to allow styling. */
     public static final String CLASSNAME = "v-openlayersmap";
 
     /**
      * Projection of coordinates passed from the serverside
      */
     private Projection serverSideProjection = Projection.get("EPSG:4326");
 
     /** The client side widget identifier */
     protected String paintableId;
 
     /** Reference to the server connection object. */
     protected ApplicationConnection client;
 
     // private MarkerLayer markerLayer;
 
     HashMap<String, Widget> components = new HashMap<String, Widget>();
 
     private Map map = new Map();
 
     FlowPanel fakePaintables = new FlowPanel();
 
     private HashSet<String> orphanedcomponents;
 
     private GwtOlHandler extentChangeListener;
     private GwtOlHandler clickListener;
 
     private boolean immediate;
 
     private HashMap<String, Control> myControls = new HashMap<String, Control>();
 
     private String[] bodyActionKeys;
 
     private final HashMap<Object, String> actionMap = new HashMap<Object, String>();
 
     private LonLat clickedLonLat;
 
     /**
      * The constructor should first call super() to initialize the component and
      * then handle any initialization relevant to Vaadin.
      */
     public VOpenLayersMap() {
         setWidth("500px");
         setHeight("500px");
         add(map);
         add(fakePaintables);
         fakePaintables.setVisible(false);
 
         // This method call of the Paintable interface sets the component
         // style name in DOM tree
         setStyleName(CLASSNAME);
 
         sinkEvents(Event.ONCONTEXTMENU);
         addDomHandler(new ContextMenuHandler() {
             public void onContextMenu(ContextMenuEvent event) {
                 handleBodyContextMenu(event);
             }
         }, ContextMenuEvent.getType());
     }
 
     protected void handleBodyContextMenu(ContextMenuEvent event) {
         if (bodyActionKeys != null) {
             clickedLonLat = getMap().getLonLatFromPixel(
                     Pixel.create(getMapClickLeftPosition(event),
                             getMapClickTopPosition(event)));
             Projection projection = getMap().getBaseLayer().getProjection();
             Projection apiProjection = getProjection();
             clickedLonLat.transform(projection, apiProjection);
             client.getContextMenu().showAt(this,
                     getWindowClickLeftPosition(event),
                     getWindowClickTopPosition(event));
             event.stopPropagation();
             event.preventDefault();
         }
     }
 
     private int getWindowClickTopPosition(ContextMenuEvent event) {
         return Util.getTouchOrMouseClientY(event.getNativeEvent())
                 + Window.getScrollTop();
     }
 
     private int getWindowClickLeftPosition(ContextMenuEvent event) {
         return Util.getTouchOrMouseClientX(event.getNativeEvent())
                 + Window.getScrollLeft();
     }
 
     private double getMapClickTopPosition(ContextMenuEvent event) {
         return Util.getTouchOrMouseClientY(event.getNativeEvent())
                 - getMap().getAbsoluteTop();
     }
 
     private double getMapClickLeftPosition(ContextMenuEvent event) {
         return Util.getTouchOrMouseClientX(event.getNativeEvent())
                 - getMap().getAbsoluteLeft();
     }
 
     /**
      * Called whenever an update is received from the server
      */
     public void updateFromUIDL(UIDL uidl, final ApplicationConnection client) {
 
         // This call should be made first.
         // It handles sizes, captions, tooltips, etc. automatically.
         if (client.updateComponent(this, uidl, true)) {
             // If client.updateComponent returns true there has been no changes
             // and we
             // do not need to update anything.
             return;
         }
 
         immediate = uidl.hasAttribute("immediate");
 
         if (uidl.hasAttribute("projection")) {
             serverSideProjection = Projection.get(uidl
                     .getStringAttribute("projection"));
         }
 
         if (uidl.hasAttribute("jsMapOptions")) {
             map.setMapInitOptions(uidl.getStringAttribute("jsMapOptions"));
         }
 
         if (extentChangeListener == null) {
             extentChangeListener = new GwtOlHandler() {
                 @SuppressWarnings("rawtypes")
                 public void onEvent(JsArray arguments) {
                     
                     int zoom = map.getZoom();
                     client.updateVariable(paintableId, "zoom", zoom, false);
                     Bounds extent = map.getExtent();
                     if (extent == null) {
                         VConsole.log(" extent null");
                         return;
                     }
                     Projection projection = map.getProjection();
                     extent.transform(projection, getProjection());
                     client.updateVariable(paintableId, "left",
                             extent.getLeft(), false);
                     client.updateVariable(paintableId, "right",
                             extent.getRight(), false);
                     client.updateVariable(paintableId, "top", extent.getTop(),
                             false);
                     client.updateVariable(paintableId, "bottom",
                             extent.getBottom(), immediate);
                 }
             };
             getMap().registerEventHandler("moveend", extentChangeListener);
 
         }
 
         // if (clickListener == null) {
         if (client.hasEventListeners(this, "click")) {
             if (clickListener == null) {
                 clickListener = new GwtOlHandler() {
 
                     public void onEvent(JsArray arguments) {
                         JsObject event = arguments.get(0).cast();
                         Pixel pixel = event.getFieldByName("xy").cast();
                         LonLat lonlat = map.getLonLatFromPixel(pixel);
                         // TODO : we better create a mechanism to define base
                         // projection in this class according to our base layer
                         // selection
                         Projection sourceProjection = Projection
                                 .get("EPSG:900913");
                         lonlat.transform(sourceProjection, serverSideProjection);
                         client.updateVariable(paintableId, "x", pixel.getX(),
                                 false);
                         client.updateVariable(paintableId, "y", pixel.getY(),
                                 false);
                         client.updateVariable(paintableId, "height",
                                 map.getOffsetHeight(), false);
                         client.updateVariable(paintableId, "width",
                                 map.getOffsetWidth(), false);
                         client.updateVariable(paintableId, "lon",
                                 lonlat.getLon(), false);
                         client.updateVariable(paintableId, "lat",
                                 lonlat.getLat(), false);
                         client.updateVariable(paintableId, "clicked", "", true); // Just
                                                                                  // //
                     }
                 };
             }
            getMap().registerEventHandler("click", clickListener);
         } else {
             // TODO : HOW WILL WE UNREGISTER EVENTHANDLER ???
         }
 
         // Save reference to server connection object to be able to send
         // user interaction later
         this.client = client;
 
         // Save the client side identifier (paintable id) for the widget
         paintableId = uidl.getId();
 
         updateControls(uidl);
 
         if (uidl.getBooleanAttribute("componentsPainted")) {
             orphanedcomponents = new HashSet<String>(components.keySet());
 
             Iterator<Object> childIterator = uidl.getChildIterator();
             while (childIterator.hasNext()) {
                 UIDL layerUidl = (UIDL) childIterator.next();
                 if (layerUidl.getTag().equals("actions")) {
                     continue;
                 }
                 orphanedcomponents.remove(layerUidl.getId());
                 Paintable paintable = client.getPaintable(layerUidl);
                 if (!components.containsKey(layerUidl.getId())) {
                     components.put(layerUidl.getId(), (Widget) paintable);
                     fakePaintables.add((Widget) paintable);
                 }
                 paintable.updateFromUIDL(layerUidl, client);
 
             }
         }
 
         if (uidl.hasAttribute("re_top")) {
             Bounds bounds = Bounds.create(uidl.getDoubleAttribute("re_left"),
                     uidl.getDoubleAttribute("re_bottom"),
                     uidl.getDoubleAttribute("re_right"),
                     uidl.getDoubleAttribute("re_top"));
             bounds.transform(getProjection(), getMap().getProjection());
             map.setRestrictedExtent(bounds);
         }
 
         updateZoomAndCenter(uidl);
 
         if (uidl.getBooleanAttribute("componentsPainted")) {
             for (String id : orphanedcomponents) {
                 Widget remove = components.remove(id);
                 fakePaintables.remove(remove);
             }
         }
 
         if (uidl.hasAttribute("alb")) {
             bodyActionKeys = uidl.getStringArrayAttribute("alb");
         } else {
             // Need to clear the actions if the action handlers have been
             // removed
             bodyActionKeys = null;
         }
 
         updateActionMap(uidl);
     }
 
     private void updateActionMap(UIDL mainUidl) {
         UIDL actionsUidl = mainUidl.getChildByTagName("actions");
         if (actionsUidl == null) {
             return;
         }
 
         final Iterator<?> it = actionsUidl.getChildIterator();
         while (it.hasNext()) {
             final UIDL action = (UIDL) it.next();
             final String key = action.getStringAttribute("key");
             final String caption = action.getStringAttribute("caption");
             actionMap.put(key + "_c", caption);
             if (action.hasAttribute("icon")) {
                 actionMap.put(key + "_i", client.translateVaadinUri(action
                         .getStringAttribute("icon")));
             } else {
                 actionMap.remove(key + "_i");
             }
         }
     }
 
     public Action[] getActions() {
         if (bodyActionKeys == null) {
             return new Action[] {};
         }
         final Action[] actions = new Action[bodyActionKeys.length];
         for (int i = 0; i < actions.length; i++) {
             final String actionKey = bodyActionKeys[i];
             Action bodyAction = new TreeAction(this, clickedLonLat.getLon()
                     + ":" + clickedLonLat.getLat(), actionKey);
             bodyAction.setCaption(actionMap.get(actionKey + "_c"));
             bodyAction.setIconUrl(actionMap.get(actionKey + "_i"));
             actions[i] = bodyAction;
         }
         return actions;
     }
 
     public ApplicationConnection getClient() {
         return client;
     }
 
     public String getPaintableId() {
         return paintableId;
     }
 
     private void updateControls(UIDL uidl) {
         if (uidl.hasAttribute("controls")) {
 
             HashSet<String> oldcontrols = new HashSet<String>(
                     myControls.keySet());
 
             String[] controls = uidl.getStringArrayAttribute("controls");
             for (int i = 0; i < controls.length; i++) {
                 String name = controls[i];
                 if (oldcontrols.contains(name)) {
                     oldcontrols.remove(name);
                 } else {
                     Control controlByName = Control.getControlByName(name,
                             getMap());
                     if (controlByName != null) {
                         map.addControl(controlByName);
                         myControls.put(name, controlByName);
                     } else {
                         VConsole.error("Control not in OL build: " + name);
                     }
                 }
             }
 
             for (String string : oldcontrols) {
                 Control control = myControls.get(string);
                 if (control != null) {
                     map.removeControl(control);
                 }
                 myControls.remove(string);
             }
 
         }
     }
 
     private void updateZoomAndCenter(UIDL uidl) {
 
         // Skip for empty map
         if (getMap().getProjection() != null) {
 
             if (uidl.hasAttribute("ze_top")) {
                 /*
                  * Zoom to extent
                  */
                 double top = uidl.getDoubleAttribute("ze_top");
                 double right = uidl.getDoubleAttribute("ze_right");
                 double bottom = uidl.getDoubleAttribute("ze_bottom");
                 double left = uidl.getDoubleAttribute("ze_left");
                 Bounds bounds = Bounds.create(left, bottom, right, top);
                 bounds.transform(getProjection(), getMap().getProjection());
                 getMap().zoomToExtent(bounds);
                 return;
             }
 
             int zoom = map.getZoom();
             if (uidl.hasAttribute("zoom")) {
                 zoom = uidl.getIntAttribute("zoom");
                 if (!uidl.hasAttribute("clat")) {
                     // just set zoom, no center position
                     map.setZoom(zoom);
                 }
             }
 
             if (uidl.hasAttribute("clat")) {
                 double lat = uidl.getDoubleAttribute("clat");
                 double lon = uidl.getDoubleAttribute("clon");
                 LonLat lonLat = LonLat.create(lon, lat);
                 // expect center point to be in WSG84
                 Projection projection = map.getProjection();
                 lonLat.transform(getProjection(), projection);
                 map.setCenter(lonLat, zoom);
             }
         }
     }
 
     public void replaceChildComponent(Widget oldComponent, Widget newComponent) {
         // TODO Auto-generated method stub
 
     }
 
     public boolean hasChildComponent(Widget component) {
         return fakePaintables.getWidgetIndex(component) != -1
                 || getChildren().contains(component);
     }
 
     public void updateCaption(Paintable component, UIDL uidl) {
         // TODO Auto-generated method stub
 
     }
 
     public boolean requestLayout(Set<Paintable> children) {
         // TODO Auto-generated method stub
         return false;
     }
 
     public RenderSpace getAllocatedSpace(Widget child) {
         return new RenderSpace(0, 0, false);
     }
 
     public Map getMap() {
         return map;
     }
 
     /**
      * @return the projection this map widget uses on the server side
      */
     public Projection getProjection() {
         return serverSideProjection;
     }
 
     /**
      * @param projection
      *            the projection this map widget uses on the server side
      */
     public void setProjection(Projection projection) {
         serverSideProjection = projection;
     }
 
     public void attachSpecialWidget(Widget paintable,
             com.google.gwt.dom.client.Element elementById) {
         add(paintable, (Element) elementById.cast());
     }
 }
