 /**********************************************
  * Copyright (C) 2009 Lukas Laag
  * This file is part of lib-gwt-svg-samples.
  * 
  * libgwtsvg-samples is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * libgwtsvg-samples is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with libgwtsvg-samples.  If not, see http://www.gnu.org/licenses/
  **********************************************/
 package org.vectomatic.svg.samples.client.parser;
 
 import org.vectomatic.dom.svg.OMSVGSVGElement;
 import org.vectomatic.dom.svg.ui.ExternalSVGResource;
 import org.vectomatic.dom.svg.ui.SVGResource;
 import org.vectomatic.svg.samples.client.Main;
 import org.vectomatic.svg.samples.client.Main.MainBundle;
 import org.vectomatic.svg.samples.client.SampleBase;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.resources.client.ClientBundle;
 import com.google.gwt.resources.client.ResourceCallback;
 import com.google.gwt.resources.client.ResourceException;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.client.Element;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.TabLayoutPanel;
 
 /**
  * Class to demonstrate the parsing of SVG files
  * @author laaglu
  */
 public class ParserSample extends SampleBase {
 	interface ParserSampleBinder extends UiBinder<TabLayoutPanel, ParserSample> {
 	}
 	private static ParserSampleBinder binder = GWT.create(ParserSampleBinder.class);
 
 	public interface ParserSampleBundle extends ClientBundle {
 		public static ParserSampleBundle INSTANCE = GWT.create(ParserSampleBundle.class);
 		
 		@Source("tiger.svg")
 		ExternalSVGResource tiger();
 		@Source("lion.svg")
 		ExternalSVGResource lion();
 		@Source("butterfly.svg")
 		ExternalSVGResource butterfly();
 	}
 
 	@UiField(provided=true)
 	public static MainBundle mainBundle = Main.mainBundle;
 	@UiField
 	HTML svgContainer;
 	@UiField
 	ListBox documentListBox;
 	OMSVGSVGElement svg;
 
 	ResourceCallback<SVGResource> callback = new ResourceCallback<SVGResource>() {
 
 		@Override
 		public void onError(ResourceException e) {
 			sourceHtml.setHTML("Cannot find resource");
 		}
 
 		@Override
 		public void onSuccess(SVGResource resource) {
 			// Insert the SVG root element into the HTML UI
 			Element div = svgContainer.getElement();
 			svg = resource.getSvg();
 			if (div.hasChildNodes()) {
 				div.replaceChild(svg.getElement(), div.getFirstChild());
 			} else {
 				div.appendChild(svg.getElement());					
 			}
 		}
 		
 	};
 	
 	@Override
 	public TabLayoutPanel getPanel() {
 		if (tabPanel == null) {
 			// Initialize the UI with UiBinder
 			tabPanel = binder.createAndBindUi(this);
 			tabPanel.setTabText(0, "Parser");
 			createCodeTabs("ParserSample");
 
 			// Fill the list box with svg file names
 			documentListBox.addItem("tiger");
 			documentListBox.addItem("lion");
 			documentListBox.addItem("butterfly");
 			documentListBox.setSelectedIndex(0);
 			documentListBoxChange(null);
 		}
 		return tabPanel;
 	}
 	
 	@UiHandler("documentListBox")
 	public void documentListBoxChange(ChangeEvent event) {
 		// Request the contents of the file
 		try {
 			switch(documentListBox.getSelectedIndex()) {
 				case 0:
 					ParserSampleBundle.INSTANCE.tiger().getSvg(callback);
 					break;
 				case 1:
 					ParserSampleBundle.INSTANCE.lion().getSvg(callback);
 					break;
 				case 2:
 					ParserSampleBundle.INSTANCE.butterfly().getSvg(callback);
 					break;
 			}
 		} catch(ResourceException e) {
 			sourceHtml.setHTML("Cannot find resource");
 		}
 	}
 }
