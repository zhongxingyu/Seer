 /*******************************************************************************
 * Copyright (c) 2011 Nokia Corporation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Comarch team - initial API and implementation
 *******************************************************************************/
 package org.ned.server.nedadminconsole.client.widgets;
 
 import org.ned.server.nedadminconsole.client.NedRes;
 
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 
 public class NedStatisticsWidget extends Composite {
     
     private Button buttonDownload;
 
     public NedStatisticsWidget() {
         
         VerticalPanel verticalPanel = new VerticalPanel();
         verticalPanel.setSpacing(15);
         verticalPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
         initWidget(verticalPanel);
         verticalPanel.setSize("100%", "50px");
         
         buttonDownload = new Button();
         buttonDownload.setText(NedRes.instance().statisticsDownload());
         buttonDownload.addClickHandler(new ClickHandler() {
             public void onClick(ClickEvent event) {
                com.google.gwt.user.client.Window.open("NedStatisticsFileServlet",
                        "Download", "");
             }
         });
         verticalPanel.add(buttonDownload);
         buttonDownload.setSize("30%", "");
     }
 
 }
