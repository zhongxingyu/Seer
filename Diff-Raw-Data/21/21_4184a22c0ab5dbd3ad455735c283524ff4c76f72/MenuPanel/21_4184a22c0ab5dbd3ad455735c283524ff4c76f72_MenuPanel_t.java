 /*
  * $Id: LicenseHeader-GPLv2.txt 288 2008-01-29 00:59:35Z andrew $
  * --------------------------------------------------------------------------------------
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 
 package org.mule.galaxy.web.client.ui.panel;
 
 import java.util.List;
 
 import com.extjs.gxt.ui.client.Style.LayoutRegion;
 import com.extjs.gxt.ui.client.Style.Scroll;
 import com.extjs.gxt.ui.client.util.Margins;
 import com.extjs.gxt.ui.client.widget.ContentPanel;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
 import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
 import com.extjs.gxt.ui.client.widget.layout.FitLayout;
 import com.google.gwt.user.client.ui.Widget;
 
 public abstract class MenuPanel extends AbstractErrorShowingLayoutContainer implements Showable {
 
     private LayoutContainer leftMenuContainer;
     private Widget mainWidget;
     private ContentPanel centerPanel;
     private LayoutContainer leftMenu;
     private BorderLayoutData centerData;
 
     private boolean firstShow = true;
 
     public MenuPanel() {
         setId("menu-panel");
         
         BorderLayoutData westData = new BorderLayoutData(LayoutRegion.WEST, 220);
         westData.setSplit(true);
         westData.setCollapsible(true);  
         westData.setMargins(new Margins(0,5,0,0));
 
         centerData = new BorderLayoutData(LayoutRegion.CENTER);  
         centerData.setMargins(new Margins(0));
 
         setLayout(new BorderLayout());
 
         // the left panel
         leftMenu = new LayoutContainer();
         leftMenu.setStyleName("left-menu");
         leftMenu.setLayout(new FitLayout());
         
         // wrapper/container for menu widgets in the left panel
         leftMenuContainer = new LayoutContainer();
         leftMenuContainer.setLayoutOnChange(true);
         leftMenuContainer.setLayout(new FitLayout());
         leftMenuContainer.setStyleName("left-menu-container");
         leftMenuContainer.layout(true);
         leftMenuContainer.setMonitorWindowResize(true);
 
         leftMenu.add(leftMenuContainer);
         leftMenu.layout(false);
 
         add(leftMenu, westData);  
     }
     
     public void showPage(List<String> params) {
         if (firstShow) {
             firstShow = false;
             onFirstShow();
         }
 
         if (mainWidget instanceof Showable) {
             ((Showable) mainWidget).showPage(params);
         }
     }
 
     protected void onFirstShow() {
         centerPanel = new ContentPanel();
         centerPanel.setBodyBorder(false);
         centerPanel.setHeaderVisible(false);
         centerPanel.setStyleName("main-application-panel");
         centerPanel.setScrollMode(Scroll.AUTOY);

        // wrap the error panel in a wrapper so CSS styles can be applied correctly.
        // Otherwise the centerPanel will set the height directly on the errorPanel, 
        // overriding our 80% height in the CSS.
        LayoutContainer errorPanelWrapper = new LayoutContainer();
        errorPanelWrapper.add(errorPanel);
        errorPanelWrapper.setStyleName("error-panel-wrapper");
        centerPanel.setTopComponent(errorPanelWrapper);
         
 		add(centerPanel, centerData);
 		if (mainWidget != null) {
 		    centerPanel.add(mainWidget);
 		}
 		
         layout();
     }
 
     public boolean isFirstShow() {
         return firstShow;
     }
 
     public void hidePage() {
         if (mainWidget instanceof Showable) {
             ((Showable) mainWidget).hidePage();
         }
     }
 
     public void addMenuItem(Widget widget) {
         leftMenuContainer.add(widget);
     }
 
     public void addMenuContainer(Widget widget) {
         leftMenuContainer.add(widget);
     }
 
     public void addMenuItem(Widget widget, int index) {
         leftMenuContainer.insert(widget, index);
     }
 
     public void removeMenuItem(Widget widget) {
         leftMenuContainer.remove(widget);
     }
 
     public void setMain(Widget widget) {
         if (mainWidget != null) {
             centerPanel.remove(mainWidget);
         }
         this.mainWidget = widget;
 
         if (centerPanel == null) {
             return;
         }
         
         clearErrorMessage();
 
         centerPanel.add(widget);
     }
 
     public Widget getMain() {
         return mainWidget;
     }
 
     public LayoutContainer getLeftMenu() {
         return leftMenu;
     }
 
     public void setLeftMenu(LayoutContainer leftMenu) {
         this.leftMenu = leftMenu;
     }
 
 }
