 /*
  * JBoss, Home of Professional Open Source
  * Copyright 2008-12, Red Hat Middleware LLC, and others contributors as indicated
  * by the @authors tag. All rights reserved.
  * See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  * This copyrighted material is made available to anyone wishing to use,
  * modify, copy, or redistribute it subject to the terms and conditions
  * of the GNU Lesser General Public License, v. 2.1.
  * This program is distributed in the hope that it will be useful, but WITHOUT A
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
  * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public License,
  * v.2.1 along with this distribution; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  * MA  02110-1301, USA.
  */
 package org.overlord.gadgets.web.client.view;
 
 import com.google.gwt.dom.client.Style;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.shared.EventBus;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.LayoutPanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.inject.Inject;
 
 /**
  * @author: Jeff Yu
  * @date: 28/02/12
  */
 public class Footer {
 
     @Inject
     public Footer(EventBus bus) {
 
     }
 
     public Widget asWidget() {
 
         LayoutPanel layout = new LayoutPanel();
         layout.setStyleName("footer-panel");
 
         HTML settings = new HTML("Messages");
         settings.addStyleName("footer-link");
         settings.addClickHandler(new ClickHandler() {
             public void onClick(ClickEvent event) {
 
             }
         });
 
         layout.add(settings);
 
        HTML version = new HTML("Version: 1.0.0-SNAPSHOT");
         version.getElement().setAttribute("style", "color:#ffffff;font-size:10px; align:left");
         layout.add(version);
 
         layout.setWidgetLeftWidth(version, 20, Style.Unit.PX, 200, Style.Unit.PX);
         layout.setWidgetTopHeight(version, 3, Style.Unit.PX, 16, Style.Unit.PX);
 
         layout.setWidgetRightWidth(settings, 5, Style.Unit.PX, 60, Style.Unit.PX);
         layout.setWidgetTopHeight(settings, 3, Style.Unit.PX, 28, Style.Unit.PX);
 
         return layout;
     }
 }
 
