 /*
  *  geo-platform
  *  Rich webgis framework
  *  http://geo-platform.org
  * ====================================================================
  *
  * Copyright (C) 2008-2013 geoSDI Group (CNR IMAA - Potenza - ITALY).
  *
  * This program is free software: you can redistribute it and/or modify it 
  * under the terms of the GNU General Public License as published by 
  * the Free Software Foundation, either version 3 of the License, or 
  * (at your option) any later version. This program is distributed in the 
  * hope that it will be useful, but WITHOUT ANY WARRANTY; without 
  * even the implied warranty of MERCHANTABILITY or FITNESS FOR 
  * A PARTICULAR PURPOSE. See the GNU General Public License 
  * for more details. You should have received a copy of the GNU General 
  * Public License along with this program. If not, see http://www.gnu.org/licenses/ 
  *
  * ====================================================================
  *
  * Linking this library statically or dynamically with other modules is 
  * making a combined work based on this library. Thus, the terms and 
  * conditions of the GNU General Public License cover the whole combination. 
  * 
  * As a special exception, the copyright holders of this library give you permission 
  * to link this library with independent modules to produce an executable, regardless 
  * of the license terms of these independent modules, and to copy and distribute 
  * the resulting executable under terms of your choice, provided that you also meet, 
  * for each linked independent module, the terms and conditions of the license of 
  * that module. An independent module is a module which is not derived from or 
  * based on this library. If you modify this library, you may extend this exception 
  * to your version of the library, but you are not obligated to do so. If you do not 
  * wish to do so, delete this exception statement from your version. 
  *
  */
 package org.geosdi.geoplatform.gui.client.mvc;
 
 import com.extjs.gxt.ui.client.mvc.AppEvent;
 import com.extjs.gxt.ui.client.mvc.Controller;
 import com.extjs.gxt.ui.client.mvc.Dispatcher;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.RootPanel;
 import org.geosdi.geoplatform.gui.client.MapWidgetEvents;
 import org.geosdi.geoplatform.gui.client.config.LayerModuleInjector;
import org.geosdi.geoplatform.gui.client.config.MementoModuleInjector;
 import org.geosdi.geoplatform.gui.client.model.memento.save.IMementoSave;
 import org.geosdi.geoplatform.gui.client.widget.login.GeoPortalLogin;
 import org.geosdi.geoplatform.gui.client.widget.menu.MenuBarWidget;
 import org.geosdi.geoplatform.gui.configuration.mvc.GeoPlatformView;
 import org.geosdi.geoplatform.gui.impl.view.LayoutManager;
 import org.geosdi.geoplatform.gui.utility.GeoPlatformUtils;
 import org.geosdi.geoplatform.gui.view.event.GeoPlatformEvents;
 import org.gwtopenmaps.openlayers.client.OpenLayers;
 
 /**
  *
  * @author Giuseppe La Scaleia - CNR IMAA geoSDI Group
  * @email giuseppe.lascaleia@geosdi.org
  */
 public class BaseAppView extends GeoPlatformView {
 
     private GeoPortalLogin loginWidget = new GeoPortalLogin();
     private HandlerRegistration handlerRegistration;
 
     public BaseAppView(Controller controller) {
         super(controller);
         OpenLayers.setProxyHost("gwtOpenLayersProxy?targetURL=");
     }
 
     /**
      * (non-Javadoc)
      *
      * @see
      * org.geosdi.geoplatform.gui.configuration.mvc.GeoPlatformView#handleEvent(com.extjs.gxt.ui.client.mvc.AppEvent)
      */
     @Override
     protected void handleEvent(AppEvent event) {
         if (event.getType() == GeoPlatformEvents.INIT_GEO_PLATFORM) {
             initUI();
         } else if (event.getType() == GeoPlatformEvents.APPLICATION_FIRST_LOGIN) {
             this.showFirstLogin();
         } else if (event.getType() == GeoPlatformEvents.REMOVE_WINDOW_CLOSE_LISTENER) {
             this.handlerRegistration.removeHandler();
         }
     }
 
     private void showFirstLogin() {
         RootPanel.get().add(loginWidget);
     }
 
     private void initUI() {
         addComponentToNorth();
         addComponentToCenter();
         RootPanel.get().add(LayoutManager.getInstance().getViewport());
         this.addWindowClosingListener();
     }
 
     private void addWindowClosingListener() {
        this.handlerRegistration = Window.addWindowClosingHandler(
                new Window.ClosingHandler() {

             @Override
             public void onWindowClosing(Window.ClosingEvent closingEvent) {
                 String message = new String();
                IMementoSave mementoSave = MementoModuleInjector.MainInjector.getInstance().getMementoSave();
                 if (!mementoSave.isEmpty()) {
                     message = "Warning: There are unsaved operations on the tree. ";
                 }
                 message += "Do you really want to leave the application?";
                 closingEvent.setMessage(message);
             }

         });
     }
 
     private void addComponentToNorth() {
         MenuBarWidget menuBar = new MenuBarWidget(
                 GeoPlatformUtils.getInstance().getGlobalConfiguration().getMenuBarContainerTool());
         LayoutManager.getInstance().getNorth().setTopComponent(menuBar.getBar());
     }
 
     private void addComponentToCenter() {
         Dispatcher.forwardEvent(MapWidgetEvents.ATTACH_MAP_WIDGET);
         Dispatcher.forwardEvent(MapWidgetEvents.ATTACH_TOOLBAR);
     }

 }
