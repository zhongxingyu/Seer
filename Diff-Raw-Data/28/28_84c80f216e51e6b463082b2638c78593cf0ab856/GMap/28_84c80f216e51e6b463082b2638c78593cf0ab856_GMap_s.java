 /*
  *
  * ==============================================================================
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package wicket.contrib.gmap3;
 
 import org.apache.wicket.Application;
 import org.apache.wicket.Component;
 import org.apache.wicket.RuntimeConfigurationType;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.behavior.Behavior;
 import org.apache.wicket.markup.html.IHeaderResponse;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.request.Request;
 import org.apache.wicket.request.cycle.RequestCycle;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import wicket.contrib.gmap3.api.*;
 import wicket.contrib.gmap3.mapevent.GMapEventListenerBehavior;
 import wicket.contrib.gmap3.overlay.GOverlay;
 import wicket.contrib.gmap3.overlay.OverlayListener;
 
 import java.util.*;
 
 /**
  * Wicket component to embed <a href="http://maps.google.com">Google Maps</a> into your pages.
  *
  * Understanding ajax-updates with GMap:
  * There are two situations that are handled by GMap:
  * <pre>
  * - map is fully reloaded in ajax-request
  *    -> use methods without AjaxRequestTarget parameter.
  * - map is not reloaded but should change its state after an ajax-call
  *    -> use methods with AjaxRequestTarget parameter.
  * </pre>
  */
 public class GMap extends Panel {
     /** log. */
     static final Logger log = LoggerFactory.getLogger(GMap.class);
 
     private static final long serialVersionUID = 1L;
 
     /**
      * default map center.
      */
     private GLatLng _center = new GLatLng(0, 0);
 
     private boolean _draggingEnabled = true;
 
     private boolean _doubleClickZoomEnabled = true;
 
     private boolean _scrollWheelZoomEnabled = true;
 
     private GMapType _mapType = GMapType.ROADMAP;
 
     private int _zoom = 13;
 
     private Integer _maxZoom = null;
 
     private final Set<GControl> _controls = new HashSet<GControl>();
 
     final List<GOverlay> _overlays = new ArrayList<GOverlay>();
 
     private final WebMarkupContainer _map;
 
     private final GInfoWindow _infoWindow;
 
     /**
      * Bounds as retrieved from map.getBounds()
      */
     private GLatLngBounds _getBounds;
 
     /**
      * Bounds used in map.fitBounds(). Be ware of this behavior:
      * http://code.google.com/p/gmaps-api-issues/issues/detail?id=3117
      */
     private GLatLngBounds _fitBounds;
 
     /* max zoomlevel to use when using fitMarkers. */
     private int _boundsMaxZoom = 0;
 
     private final OverlayListener _overlayListener;
 
     /**
      * Construct.
      *
      * @param id the id
      */
     public GMap(final String id) {
         this(id, null, new GMapHeaderContributor());
     }
 
     /**
      * Construct.
      *
      * @param id the id
      * @param model the model
      * @param headerContrib the header contrib
      */
     public GMap(final String id, final IModel<?> model, final Behavior headerContrib) {
         super(id, model);
 
         add(headerContrib);
         add(new Behavior() {
 
             @Override
             public void renderHead(final Component component, final IHeaderResponse response) {
                 response.renderOnDomReadyJavaScript(getJSinit());
             }
         });
 
         _infoWindow = new GInfoWindow();
         add(_infoWindow);
 
         _map = new WebMarkupContainer("map");
         _map.setOutputMarkupId(true);
         add(_map);
 
         _overlayListener = new OverlayListener();
         add(_overlayListener);
     }
 
     /**
      * Instantiates a new g map.
      *
      * @param id the id
      * @param model the model
      */
     public GMap(final String id, final IModel<?> model) {
         this(id, model, new GMapHeaderContributor());
     }
 
     public String getMapId() {
         return _map.getMarkupId();
     }
 
     /**
      * On render.
      *
      * @see org.apache.wicket.MarkupContainer#onRender(org.apache.wicket.markup.MarkupStream)
      */
     @Override
     protected void onRender() {
         super.onRender();
         if (RuntimeConfigurationType.DEVELOPMENT.equals(Application.get().getConfigurationType())
                 && !Application.get().getMarkupSettings().getStripWicketTags()) {
             log.warn("Application is in DEVELOPMENT mode && Wicket tags are not stripped,"
                     + "Some Chrome Versions will not render the GMap."
                     + " Change to DEPLOYMENT mode  || turn on Wicket tags stripping." + " See:"
                     + " http://www.nabble.com/Gmap2-problem-with-Firefox-3.0-to18137475.html.");
         }
     }
 
     /**
      * Fix for layout bug when map is loaded in hidden div (like in modal window). See
      * http://code.google.com/p/gmaps-api-issues/issues/detail?id=1448. Clients should call this method in
      * onBeforeRender.
      */
     public void repaintMap() {
         if (AjaxRequestTarget.get() != null) {
             String js = "google.maps.event.trigger(" + getJsReference() + ".map, 'resize');";
             js += getJSsetCenter(getCenter());
             if (_fitBounds != null) {
                 // if not using fit bounds you must call setZoom to force correct zoom level
                 js += getJSfitBounds(getFitBounds(), _boundsMaxZoom);
             } else {
                 js += getJSsetZoom(getZoom());
             }
             AjaxRequestTarget.get().appendJavaScript(js);
         }
     }
 
     /**
      * Add a control.
      *
      * @param control control to add
      * @return This
      */
     @ReviewPending
     // remove when method is tested
     public GMap addControl(final GControl control) {
         _controls.add(control);
         return this;
     }
 
     /**
      * Adds the control.
      *
      * Call this method inside ajax request when the map itself is not added to the target.
      *
      * @param control the control
      * @param target the target
      * @return the g map
      */
     public GMap addControl(final GControl control, final AjaxRequestTarget target) {
         addControl(control);
         target.appendJavaScript(control.getJSadd(this));
         return this;
     }
 
     /**
      * Remove a control.
      *
      * @param control control to remove
      * @return This
      */
     @ReviewPending
     // remove when method is tested
     public GMap removeControl(final GControl control) {
         _controls.remove(control);
         return this;
     }
 
     /**
      * Remove a control.
      *
      * Call this method inside ajax request when the map itself is not added to the target.
      *
      * @param control control to remove
      * @param target the target
      * @return This
      */
     @ReviewPending
     // remove when method is tested
     public GMap removeControl(final GControl control, final AjaxRequestTarget target) {
         removeControl(control);
         target.appendJavaScript(control.getJSremove(this));
         return this;
     }
 
     /**
      * Add an overlay.
      *
      * @param overlay overlay to add
      * @return This
      */
     public GMap addOverlay(final GOverlay overlay) {
         _overlays.add(overlay);
         overlay.setParent(this);
         return this;
     }
 
     /**
      * Add an overlay.
      *
      * Call this method inside ajax request when the map itself is not added to the target.
      *
      * @param overlay overlay to add
      * @param target the target
      * @return This
      */
     public GMap addOverlay(final GOverlay overlay, final AjaxRequestTarget target) {
         addOverlay(overlay);
         target.appendJavaScript(overlay.getJS());
         return this;
     }
 
     /**
      * Remove an overlay.
      *
      * @param overlay overlay to remove
      * @return This
      */
     @ReviewPending
     // remove when method is tested
     public GMap removeOverlay(final GOverlay overlay) {
         while (_overlays.contains(overlay)) {
             _overlays.remove(overlay);
         }
         overlay.setParent(null);
         return this;
     }
 
     /**
      * Remove an overlay.
      *
      * Call this method inside ajax request when the map itself is not added to the target.
      *
      * @param overlay overlay to remove
      * @param target the target
      * @return This
      */
     @ReviewPending
     // remove when method is tested
     public GMap removeOverlay(final GOverlay overlay, final AjaxRequestTarget target) {
         while (_overlays.contains(overlay)) {
             _overlays.remove(overlay);
         }
         target.appendJavaScript(overlay.getJSremove());
         overlay.setParent(null);
         return this;
     }
 
     /**
      * Clear all overlays.
      *
      * @return This
      */
     public GMap removeAllOverlays() {
         for (final GOverlay overlay : _overlays) {
             overlay.setParent(null);
         }
         _overlays.clear();
         return this;
     }
 
     /**
      * Clear all overlays.
      *
      * Call this method inside ajax request when the map itself is not added to the target.
      *
      * @param target the target
      * @return This
      */
     public GMap removeAllOverlays(final AjaxRequestTarget target) {
         removeAllOverlays();
         target.appendJavaScript(getJSinvoke("clearOverlays()"));
         return this;
     }
 
     @ReviewPending
     // remove when method is tested
     public List<GOverlay> getOverlays() {
         return Collections.unmodifiableList(_overlays);
     }
 
 
      /**
      * Clear all listeners.
      *
      * @param target the target
      * @return the g map
      */
     @ReviewPending
     // remove when method is tested
     public GMap clearAllListeners(final AjaxRequestTarget target) {
         target.appendJavaScript(getJSclearAllListeners());
         return this;
     }
 
     @ReviewPending
     // remove when method is tested
     public Set<GControl> getControls() {
         return Collections.unmodifiableSet(_controls);
     }
 
     public GLatLngBounds getBounds() {
         return _getBounds;
     }
 
     public GLatLngBounds getFitBounds() {
         return _fitBounds;
     }
 
     /**
      * <p>
      * Makes the map zoom out and centre around all the GLatLng points in markersToShow.
      * <p>
      * Big ups to Doug Leeper for the code.
      *
      * @param markersToShow the points to centre around.
      * @param maximumZoomLevel the maximum zoom level
      * @see <a href= "http://www.nabble.com/Re%3A-initial-GMap2-bounds-question-p19886673.html" >Doug's Nabble post</a>
      */
    public void fitMarkers(final List<GLatLng> markersToShow, final int maximumZoomLevel) {
         if (markersToShow.isEmpty()) {
             log.warn("Empty list provided to GMap.fitMarkers method.");
             return;
         }
         fitBounds(new GLatLngBounds(markersToShow), maximumZoomLevel);
     }
 
 
     /**
      * Be careful. map.fitBounds(map.getBounds()) will actually zoom out. See here:
      * http://code.google.com/p/gmaps-api-issues/issues/detail?id=3117
      *
      * @param bounds the bounds
      * @param maximumZoomLevel the maximum zoom level, set to 0 if no max level is to be used
      */
     public void fitBounds(final GLatLngBounds bounds, final int maximumZoomLevel) {
         _fitBounds = bounds;
 
         // set Center so that map will be placed at correct position (avoids short display of Palo Alto location)
         setCenter(_fitBounds.getCenter());
         _boundsMaxZoom = maximumZoomLevel;
     }
 
     /**
      * Be careful. map.fitBounds(map.getBounds()) will actually zoom out. See here:
      * http://code.google.com/p/gmaps-api-issues/issues/detail?id=3117
      *
      * Call this method inside ajax request when the map itself is not added to the target.
      *
      * @param bounds the bounds
      * @param maximumZoomLevel the maximum zoom level, set to 0 if no max level is to be used
      * @param target the target
      */
     public void fitBounds(final GLatLngBounds bounds, final int maximumZoomLevel, final AjaxRequestTarget target) {
         fitBounds(bounds, maximumZoomLevel);
         target.appendJavaScript(getJSfitBounds(_fitBounds, _boundsMaxZoom));
     }
 
     /**
      * Sets the dragging enabled.
      *
      * @param enabled the new dragging enabled
      */
     @ReviewPending
     // remove when method is tested
     public void setDraggingEnabled(final boolean enabled) {
         _draggingEnabled = enabled;
     }
 
     /**
      * Sets the dragging enabled.
      *
      * Call this method inside ajax request when the map itself is not added to the target.
      *
      * @param enabled the enabled
      * @param target the target
      */
     @ReviewPending
     // remove when method is tested
     public void setDraggingEnabled(final boolean enabled, final AjaxRequestTarget target) {
         setDraggingEnabled(enabled);
         target.appendJavaScript(getJSsetDraggingEnabled(enabled));
     }
 
     /**
      * Checks if is dragging enabled.
      *
      * @return true, if is dragging enabled
      */
     public boolean isDraggingEnabled() {
         return _draggingEnabled;
     }
 
     /**
      * Sets the double click zoom enabled.
      *
      * @param enabled the new double click zoom enabled
      */
     public void setDoubleClickZoomEnabled(final boolean enabled) {
         _doubleClickZoomEnabled = enabled;
     }
 
     /**
      * Sets the double click zoom enabled.
      *
      * Call this method inside ajax request when the map itself is not added to the target.
      *
      * @param enabled the enabled
      * @param target the target
      */
     public void setDoubleClickZoomEnabled(final boolean enabled, final AjaxRequestTarget target) {
         setDoubleClickZoomEnabled(enabled);
         target.appendJavaScript(getJSsetDoubleClickZoomEnabled(enabled));
     }
 
     /**
      * Checks if is double click zoom enabled.
      *
      * @return true, if is double click zoom enabled
      */
     public boolean isDoubleClickZoomEnabled() {
         return _doubleClickZoomEnabled;
     }
 
     /**
      * Sets the scroll wheel zoom enabled.
      *
      * @param enabled the new scroll wheel zoom enabled
      */
     public void setScrollWheelZoomEnabled(final boolean enabled) {
         _scrollWheelZoomEnabled = enabled;
     }
 
     /**
      * Sets the scroll wheel zoom enabled.
      *
      * Call this method inside ajax request when the map itself is not added to the target.
      *
      * @param enabled the enabled
      * @param target the target
      */
     public void setScrollWheelZoomEnabled(final boolean enabled, final AjaxRequestTarget target) {
         setScrollWheelZoomEnabled(enabled);
         target.appendJavaScript(getJSsetScrollWheelZoomEnabled(enabled));
     }
 
     /**
      * Checks if is scroll wheel zoom enabled.
      *
      * @return true, if is scroll wheel zoom enabled
      */
     public boolean isScrollWheelZoomEnabled() {
         return _scrollWheelZoomEnabled;
     }
 
     /**
      * Gets the map type.
      *
      * @return the map type
      */
     public GMapType getMapType() {
         return _mapType;
     }
 
     /**
      * Sets the map type.
      *
      * @param mapType the new map type
      */
     public void setMapType(final GMapType mapType) {
         _mapType = mapType;
     }
 
     /**
      * Sets the map type.
      *
      * Call this method inside ajax request when the map itself is not added to the target.
      *
      * @param mapType the map type
      * @param target the target
      */
     public void setMapType(final GMapType mapType, final AjaxRequestTarget target) {
         setMapType(mapType);
         target.appendJavaScript(mapType.getJSsetMapType(GMap.this));
     }
 
     /**
      * Gets the zoom.
      *
      * @return the zoom
      */
     public int getZoom() {
         return _zoom;
     }
 
     /**
      * Sets the zoom.
      *
      * @param level the new zoom
      */
     public void setZoom(final int level) {
         _zoom = level;
     }
 
     /**
      * Sets the zoom.
      *
      * Call this method inside ajax request when the map itself is not added to the target.
      *
      * @param level the level
      * @param target the target
      */
     public void setZoom(final int level, final AjaxRequestTarget target) {
         setZoom(level);
         target.appendJavaScript(getJSsetZoom(_zoom));
     }
 
     public void setMaxZoom(final int level) {
         _maxZoom = level;
     }
 
     /**
      * Sets the max zoom.
      *
      * Call this method inside ajax request when the map itself is not added to the target.
      *
      * @param level the level
      * @param target the target
      */
     public void setMaxZoom(final int level, final AjaxRequestTarget target) {
         setMaxZoom(level);
         target.appendJavaScript(getJSsetMaxZoom(_maxZoom));
     }
 
     public GLatLng getCenter() {
         return _center;
     }
 
     /**
      * Set the center.
      *
      * @param center center to set
      */
     public void setCenter(final GLatLng center) {
         _center = center;
     }
 
     /**
      * Set the center.
      *
      * Call this method inside ajax request when the map itself is not added to the target.
      *
      * @param center center to set
      * @param target the target
      */
     public void setCenter(final GLatLng center, final AjaxRequestTarget target) {
         setCenter(center);
         target.appendJavaScript(getJSsetCenter(center));
     }
 
     /**
      * Changes the center point of the map to the given point. If the point is already visible in the current map view,
      * change the center in a smooth animation.
      *
      * @param center the new center of the map
      * @param target the target
      */
     @ReviewPending
     // remove when method is tested
     public void panTo(final GLatLng center, final AjaxRequestTarget target) {
         setCenter(center);
         target.appendJavaScript(getJSpanTo(center));
     }
 
     /**
      * Gets the info window.
      *
      * @return the info window
      */
     @ReviewPending
     // remove when method is tested
     public GInfoWindow getInfoWindow() {
         return _infoWindow;
     }
 
     /**
      * Generates the JavaScript used to instantiate this GMap as an JavaScript class on the client side.
      *
      * @return The generated JavaScript
      */
     private String getJSinit() {
         final StringBuffer js = new StringBuffer("new WicketMap('" + _map.getMarkupId() + "');\n");
 
         // js.append(getJSclearAllListeners());
         js.append(_overlayListener.getJSinit());
 
         js.append(getJSsetCenter(getCenter()));
         js.append(getJSsetZoom(getZoom()));
         js.append(getJSsetDraggingEnabled(_draggingEnabled));
         js.append(getJSsetDoubleClickZoomEnabled(_doubleClickZoomEnabled));
         js.append(getJSsetScrollWheelZoomEnabled(_scrollWheelZoomEnabled));
         js.append(getJSsetMaxZoom(_maxZoom));
         js.append(_mapType.getJSsetMapType(this));
         js.append(getJSfitBounds(_fitBounds, _boundsMaxZoom));
 
         // Add the controls.
         // for ( final GControl control : controls ) {
         // js.append( control.getJSadd( this ) );
         // }
 
         // Add the overlays.
         for (final GOverlay overlay : _overlays) {
             js.append(overlay.getJS());
         }
 
         for (final Object behavior : getBehaviors(GMapEventListenerBehavior.class)) {
             js.append(((GMapEventListenerBehavior)behavior).getJSaddListener());
         }
 
         return js.toString();
     }
 
     /**
      * Convenience method for generating a JavaScript call on this GMap with the given invocation.
      *
      * @param invocation The JavaScript call to invoke on this GMap.
      * @return The generated JavaScript.
      */
     public String getJSinvoke(final String invocation) {
         return getJsReference() + "." + invocation + ";\n";
     }
 
     /**
      * Build a reference in JS-Scope.
      */
     public String getJsReference() {
         return "Wicket.maps['" + _map.getMarkupId() + "']";
     }
 
     /**
      * Gets the javascript to call google.map.fitBounds().
      *
      * @param bounds the bounds
      * @param boundsMaxZoom the bounds max zoom
      * @return the j sfit markers
      */
     public String getJSfitBounds(final GLatLngBounds bounds, final int boundsMaxZoom) {
         if (bounds != null) {
             final StringBuffer buf = new StringBuffer();
             if (boundsMaxZoom != 0) {
                 // reading the actual zoom level immediately after fitBounds will not work. it will show false results.
                 // so we use a callback on the bounds_changed event to update the zoom level.
                 final String setZoom = getJsReference() + ".map.setZoom(Math.min(" + getJsReference()
                         + ".map.getZoom()," + boundsMaxZoom + "));\n";
                 buf.append("google.maps.event.addListenerOnce(" + getJsReference()
                         + ".map, 'bounds_changed', function(evt) { console.log('bounds_changed callback'); "
                         + setZoom + " });");
 
             }
             buf.append(getJsReference() + ".bounds = new google.maps.LatLngBounds("
                     + bounds.getSW().getJSconstructor() + "," + bounds.getNE().getJSconstructor() + ");\n");
             buf.append(getJsReference() + ".map.fitBounds(" + getJsReference() + ".bounds);\n");
             return buf.toString();
         }
         return "";
     }
 
     /**
      * Gets the j sset dragging enabled.
      *
      * @param enabled the enabled
      * @return the j sset dragging enabled
      */
     private String getJSsetDraggingEnabled(final boolean enabled) {
         return getJSinvoke("setDraggingEnabled(" + enabled + ")");
     }
 
     /**
      * Gets the j sset double click zoom enabled.
      *
      * @param enabled the enabled
      * @return the j sset double click zoom enabled
      */
     private String getJSsetDoubleClickZoomEnabled(final boolean enabled) {
         return getJSinvoke("setDoubleClickZoomEnabled(" + enabled + ")");
     }
 
     /**
      * Gets the j sset scroll wheel zoom enabled.
      *
      * @param enabled the enabled
      * @return the j sset scroll wheel zoom enabled
      */
     private String getJSsetScrollWheelZoomEnabled(final boolean enabled) {
         return getJSinvoke("setScrollWheelZoomEnabled(" + enabled + ")");
     }
 
     /**
      * Gets the j sset zoom.
      *
      * @param zoom the zoom
      * @return the j sset zoom
      */
     public String getJSsetZoom(final int zoom) {
         return getJSinvoke("setZoom(" + zoom + ")");
     }
 
     /**
      * Gets the j sset max zoom.
      *
      * @param maxZoom the max zoom
      * @return the j sset max zoom
      */
     private String getJSsetMaxZoom(final Integer maxZoom) {
         if (maxZoom != null) {
             return getJSinvoke("setMaxZoom(" + maxZoom + ")");
         }
         return "";
     }
 
     /**
      * Gets the j sset center.
      *
      * @param center the center
      * @return the j sset center
      */
     public String getJSsetCenter(final GLatLng center) {
         if (center != null) {
             return getJSinvoke("setCenter(" + center.getJSconstructor() + ")");
         }
         return "";
     }
 
     /**
      * Gets the j span direction.
      *
      * @param dx the dx
      * @param dy the dy
      * @return the j span direction
      */
     @ReviewPending
     // remove when method is tested
     public String getJSpanDirection(final int dx, final int dy) {
         return getJSinvoke("panDirection(" + dx + "," + dy + ")");
     }
 
     /**
      * Gets the j span to.
      *
      * @param center the center
      * @return the j span to
      */
     @ReviewPending
     // remove when method is tested
     private String getJSpanTo(final GLatLng center) {
         return getJSinvoke("panTo(" + center.getJSconstructor() + ")");
     }
 
     @ReviewPending
     // remove when method is tested
     public String getJSzoomOut() {
         return getJSinvoke("zoomOut()");
     }
 
     @ReviewPending
     // remove when method is tested
     public String getJSzoomIn() {
         return getJSinvoke("zoomIn()");
     }
 
     /**
      * Clear all listeners.
      *
      * @param target the target
      * @return the g map
      */
     @ReviewPending
     // remove when method is tested
     public String getJSclearAllListeners() {
         return getJSinvoke("clearInstanceListeners()");
     }
 
 
     /**
      * Update state from a request to an AJAX target.
      */
     @ReviewPending
     // remove when method is tested
     public void update() {
         final Request request = RequestCycle.get().getRequest();
 
         // Attention: don't use setters as this will result in an endless AJAX request loop
         _getBounds = GLatLngBounds.parse(request.getRequestParameters().getParameterValue("bounds").toString());
         _center = GLatLng.parse(request.getRequestParameters().getParameterValue("center").toString());
         _zoom = Integer.parseInt(request.getRequestParameters().getParameterValue("zoom").toString());
         _mapType = GMapType.valueOf(request.getRequestParameters().getParameterValue("currentMapType").toString());
 
         // _getBounds != _fitBounds, we have to use workaround for this mismatch
         // see http://code.google.com/p/gmaps-api-issues/issues/detail?id=3117
         _fitBounds = GLatLngBounds.mapToFitBounds(_getBounds);
 
         log.debug("update(bounds=[{}], fitBounds=[{}], center=[{}], zoom=[{}], mapType=[{}])",
                 new Object[] { _getBounds, _fitBounds, _center, _zoom, _mapType });
 
         _infoWindow.update();
     }
 
     @ReviewPending
     // remove when method is tested
     public void setOverlays(final List<GOverlay> overlays) {
         removeAllOverlays();
         for (final GOverlay overlay : overlays) {
             addOverlay(overlay);
         }
     }
 }
