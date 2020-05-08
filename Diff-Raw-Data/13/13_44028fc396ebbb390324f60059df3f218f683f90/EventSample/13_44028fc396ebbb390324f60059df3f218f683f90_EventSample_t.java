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
 package org.vectomatic.svg.samples.client.events;
 
 import org.vectomatic.dom.svg.OMSVGCircleElement;
 import org.vectomatic.dom.svg.OMSVGDocument;
 import org.vectomatic.dom.svg.OMSVGMatrix;
 import org.vectomatic.dom.svg.OMSVGPoint;
 import org.vectomatic.dom.svg.OMSVGRectElement;
 import org.vectomatic.dom.svg.OMSVGSVGElement;
 import org.vectomatic.dom.svg.utils.DOMHelper;
 import org.vectomatic.dom.svg.utils.OMSVGParser;
 import org.vectomatic.dom.svg.utils.SVGConstants;
 import org.vectomatic.svg.samples.client.Main;
 import org.vectomatic.svg.samples.client.Main.MainBundle;
 import org.vectomatic.svg.samples.client.SampleBase;
 
 import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.event.dom.client.MouseDownEvent;
 import com.google.gwt.event.dom.client.MouseDownHandler;
 import com.google.gwt.event.dom.client.MouseEvent;
 import com.google.gwt.event.dom.client.MouseMoveEvent;
 import com.google.gwt.event.dom.client.MouseMoveHandler;
 import com.google.gwt.event.dom.client.MouseUpEvent;
 import com.google.gwt.event.dom.client.MouseUpHandler;
 import com.google.gwt.event.shared.EventHandler;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.Element;
 import com.google.gwt.user.client.Random;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.TabLayoutPanel;
 
 /**
  * Class to demonstrate the SVG event handling
  * @author laaglu
  */
 public class EventSample extends SampleBase implements MouseUpHandler, MouseMoveHandler, MouseDownHandler {
 	interface EventSampleBinder extends UiBinder<TabLayoutPanel, EventSample> {
 	}
 
 	private static EventSampleBinder binder = GWT.create(EventSampleBinder.class);
 
 	@UiField(provided=true)
 	public static MainBundle mainBundle = Main.mainBundle;
 	@UiField
 	HTML svgContainer;
 	private boolean dragging;
 	private float x0, y0;
 	private OMSVGPoint p;
 	private OMSVGSVGElement svg;
 	private OMSVGRectElement square;
 
 	@Override
 	public TabLayoutPanel getPanel() {
 		if (tabPanel == null) {
 			tabPanel = binder.createAndBindUi(this);
 			tabPanel.setTabText(0, "Events");
 			createCodeTabs("EventSample");
 
 			// Cast the document into a SVG document
 			Element div = svgContainer.getElement();
 			OMSVGDocument doc = OMSVGParser.currentDocument();
 
 			// Create the root svg element
 			svg = doc.createSVGSVGElement();
 			svg.setViewBox(0f, 0f, 400f, 200f);
			svg.getWidth().getBaseVal().newValueSpecifiedUnits(Unit.PCT, 100);
			svg.getHeight().getBaseVal().newValueSpecifiedUnits(Unit.PCT, 100);
 
 			// Create a circle
 			final OMSVGCircleElement circle = doc.createSVGCircleElement(80f, 80f, 40f);
 			circle.getStyle().setSVGProperty(SVGConstants.CSS_STROKE_PROPERTY, SVGConstants.CSS_BLACK_VALUE);
 			setCircleColor(circle, SVGConstants.CSS_RED_VALUE);
 			svg.appendChild(circle);
 	
 			// Set a mousedown event handler
 			circle.addMouseDownHandler(new MouseDownHandler() {
 				final String[] colors = new String[] {
 						SVGConstants.CSS_RED_VALUE,
 						SVGConstants.CSS_BLUE_VALUE,
 						SVGConstants.CSS_GREEN_VALUE,
 						SVGConstants.CSS_YELLOW_VALUE,
 						SVGConstants.CSS_PINK_VALUE };
 
 				@Override
 				public void onMouseDown(MouseDownEvent event) {
 					String color = getCircleColor(circle);
 					while (color.equals(getCircleColor(circle))) {
 						setCircleColor(circle, colors[Random.nextInt(colors.length)]);
 					}
 					event.stopPropagation();
 					event.preventDefault();
 				}
 			});
 			
 			// Create a square
 			square = doc.createSVGRectElement(140f, 40f, 80f, 80f, 0f, 0f);
 			square.getStyle().setSVGProperty(SVGConstants.CSS_STROKE_PROPERTY, SVGConstants.CSS_BLACK_VALUE);
 			square.getStyle().setSVGProperty(SVGConstants.CSS_FILL_PROPERTY, SVGConstants.CSS_GREEN_VALUE);
 			square.addMouseDownHandler(this);
 			square.addMouseUpHandler(this);
 			square.addMouseMoveHandler(this);
 			svg.appendChild(square);
 			
 			// Insert the SVG root element into the HTML UI
 			div.appendChild(svg.getElement());
 		}
 		return tabPanel;
 	}
 	private static final String getCircleColor(OMSVGCircleElement circle) {
 		return circle.getStyle().getSVGProperty(SVGConstants.CSS_FILL_PROPERTY);
 	}
 	private static final void setCircleColor(OMSVGCircleElement circle, String color) {
 		circle.getStyle().setSVGProperty(SVGConstants.CSS_FILL_PROPERTY, color);
 	}
 	
 	@Override
 	public void onMouseUp(MouseUpEvent event) {
 		dragging = false;
 		DOMHelper.releaseCaptureElement();
 		event.stopPropagation();
 		event.preventDefault();
 	}
 
 	@Override
 	public void onMouseMove(MouseMoveEvent event) {
 		if (dragging) {
 			OMSVGPoint d = getLocalCoordinates(event).substract(p);
 			square.getX().getBaseVal().setValue(x0 + d.getX());
 			square.getY().getBaseVal().setValue(y0 + d.getY());
 		}
 		event.stopPropagation();
 		event.preventDefault();
 	}
 	
 	/**
 	 * Returns the coordinates of a mouse event, converted
 	 * to the SVG coordinate system
 	 * @param e
 	 * A mouse event
 	 * @return
 	 * The coordinates of the mouse event, converted
 	 * to the SVG coordinate system
 	 */
 	public OMSVGPoint getLocalCoordinates(MouseEvent<? extends EventHandler> e) {
 		OMSVGPoint p = svg.createSVGPoint(e.getClientX(), e.getClientY());
 		OMSVGMatrix m = svg.getScreenCTM().inverse();
 		return p.matrixTransform(m);
 	}
 
 
 	@Override
 	public void onMouseDown(MouseDownEvent event) {
 		dragging = true;
 		p = getLocalCoordinates(event);
 		x0 = square.getX().getBaseVal().getValue();
 		y0 = square.getY().getBaseVal().getValue();
 		DOMHelper.setCaptureElement(square, null);
 		event.stopPropagation();
 		event.preventDefault();
 	}
 }
 
