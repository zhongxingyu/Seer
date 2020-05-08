 /*
  * Copyright 2012 Anthony Cassidy
  *
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
 
 package com.github.a2g.core.objectmodel;
 
 
import javax.swing.BoxLayout;

 import com.github.a2g.core.interfaces.InternalAPI;
 import com.github.a2g.core.interfaces.TitleCardPanelAPI;
 import com.github.a2g.core.primitive.ColorEnum;
 import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.layout.client.Layout;
import com.google.gwt.layout.client.Layout.Alignment;
 import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.LayoutPanel;
 import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
 
 public class TitleCardPanel
 extends SimplePanel
 implements TitleCardPanelAPI
 {
 
 	Label label;
 	InternalAPI api;
 	LayoutPanel layout;
 	public TitleCardPanel(final InternalAPI api, ColorEnum fore, ColorEnum back) {
 		this.api = api;
 		this.layout = new LayoutPanel();
 		this.add(layout);
 		{
 			label = new Label();
 			label.setAutoHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
 			DOM.setStyleAttribute(label.getElement(), "color",fore.toString());
 			DOM.setStyleAttribute(label.getElement(), "BackgroundColor",back.toString());
 
 			label.setText("Loading...");
 			layout.add(label);
 			layout.setWidgetLeftRight(label, 5, Unit.EM, 5, Unit.EM);     // Center panel
 			layout.setWidgetTopBottom(label, 5, Unit.EM, 5, Unit.EM);	
 		}
 	}
 
 	@Override
 	public void setText(String text) {
 		label.setText(text);
 	}
 
 	@Override
 	public void setScenePixelSize(int width, int height)
 	{
 		this.setSize("" + width + "px",	"" + height + "px");
 		// yes, we need layout.setSize otherwise the layout will not expand to fill the panel.
 		this.layout.setSize("" + width + "px",	"" + height + "px");
 	}
 }
 
 
 
