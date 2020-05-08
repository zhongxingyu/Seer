 /**
  * Copyright 2012 John Lawson
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package uk.co.jwlawson.hyperbolic.client.ui;
 
 import com.google.gwt.canvas.client.Canvas;
 import com.google.gwt.canvas.dom.client.Context2d;
 import com.google.gwt.canvas.dom.client.CssColor;
 import com.google.gwt.dom.client.Touch;
 import com.google.gwt.event.dom.client.GestureStartEvent;
 import com.google.gwt.event.dom.client.GestureStartHandler;
 import com.google.gwt.event.dom.client.MouseMoveEvent;
 import com.google.gwt.event.dom.client.MouseMoveHandler;
 import com.google.gwt.event.dom.client.MouseOutEvent;
 import com.google.gwt.event.dom.client.MouseOutHandler;
 import com.google.gwt.event.dom.client.TouchEndEvent;
 import com.google.gwt.event.dom.client.TouchEndHandler;
 import com.google.gwt.event.dom.client.TouchMoveEvent;
 import com.google.gwt.event.dom.client.TouchMoveHandler;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.RootPanel;
 
 import uk.co.jwlawson.hyperbolic.client.euclidean.EuclLine;
 import uk.co.jwlawson.hyperbolic.client.euclidean.EuclLine.Factory;
 import uk.co.jwlawson.hyperbolic.client.euclidean.EuclPoint;
 import uk.co.jwlawson.hyperbolic.client.geometry.Line;
 import uk.co.jwlawson.hyperbolic.client.geometry.Point;
 import uk.co.jwlawson.hyperbolic.client.group.TorusOrbitPoints;
 
 import java.util.ArrayList;
 import java.util.logging.Logger;
 
 /**
  * @author John
  * 
  */
 public class Focal implements CanvasHolder {
 
 	private static final Logger log = Logger.getLogger(Focal.class.getName());
 
 	static final String MOUNT_ID = "focalcanvas";
 
 	static final String upgradeMessage = "Your browser does not support the HTML5 Canvas. Please upgrade your browser to view this demo.";
 
 	Canvas canvas;
 
 	private ArrayList<EuclPoint> mPointList;
 	private ArrayList<Line> mLineList;
 
 	// mouse positions relative to canvas
 	int mouseX, mouseY;
 
 	// canvas size, in px
 	private int height = 400;
 	private int width = 400;
 
 	private final CssColor redrawColor = CssColor.make("rgb(255,255,255)");
 	private Context2d context;
 
 	public Focal() {
 		canvas = Canvas.createIfSupported();
 		if (canvas == null) {
 			RootPanel.get(MOUNT_ID).add(new Label(upgradeMessage));
 			return;
 		}
 
 		initSize();
 		context = canvas.getContext2d();
 
 	}
 
 	private void initPoints() {
 		log.fine("Loading points");
 		mPointList = new ArrayList<EuclPoint>();
 		mLineList = new ArrayList<Line>();
 
 		EuclLine.Factory factory = new Factory(width, height);
 		Point origin = new Point(0, 0);
 
 		TorusOrbitPoints orbit = new TorusOrbitPoints(width, height);
 		// Treat first entry (0,0) specially, as don't want it in focal decomp.
 		mPointList.add(orbit.next());
 		while (orbit.hasNext()) {
 			EuclPoint next = orbit.next();
 			mPointList.add(next);
 			mLineList.add(factory.getPerpendicularBisector(next, origin));
 		}
 		log.fine("Points loaded");
 	}
 
 	public void initSize() {
 		canvas.setWidth(width + "px");
 		canvas.setHeight(height + "px");
 		canvas.setCoordinateSpaceWidth(width);
 		canvas.setCoordinateSpaceHeight(height);
 
 		initPoints();
 	}
 
 	@Override
 	public void doUpdate() {

 		// update the back canvas
 		context.setFillStyle(redrawColor);
 		context.fillRect(-width, -height, width, height);
 
		context.save();
		context.translate(width / 2, height / 2);
 
 		for (EuclPoint point : mPointList) {
 			point.draw(context);
 		}
 
 		for (Line line : mLineList) {
 			line.draw(context);
 		}
 		context.restore();
 	}
 
 	@Override
 	public void initHandlers() {
 		canvas.addMouseMoveHandler(new MouseMoveHandler() {
 			@Override
 			public void onMouseMove(MouseMoveEvent event) {
 				mouseX = event.getRelativeX(canvas.getElement());
 				mouseY = event.getRelativeY(canvas.getElement());
 			}
 		});
 
 		canvas.addMouseOutHandler(new MouseOutHandler() {
 			@Override
 			public void onMouseOut(MouseOutEvent event) {
 				mouseX = -200;
 				mouseY = -200;
 			}
 		});
 
 		canvas.addTouchMoveHandler(new TouchMoveHandler() {
 			@Override
 			public void onTouchMove(TouchMoveEvent event) {
 				event.preventDefault();
 				if (event.getTouches().length() > 0) {
 					Touch touch = event.getTouches().get(0);
 					mouseX = touch.getRelativeX(canvas.getElement());
 					mouseY = touch.getRelativeY(canvas.getElement());
 				}
 				event.preventDefault();
 			}
 		});
 
 		canvas.addTouchEndHandler(new TouchEndHandler() {
 			@Override
 			public void onTouchEnd(TouchEndEvent event) {
 				event.preventDefault();
 				mouseX = -200;
 				mouseY = -200;
 			}
 		});
 
 		canvas.addGestureStartHandler(new GestureStartHandler() {
 			@Override
 			public void onGestureStart(GestureStartEvent event) {
 				event.preventDefault();
 			}
 		});
 	}
 
 	@Override
 	public void addToPanel() {
 		RootPanel panel = RootPanel.get(MOUNT_ID);
 		width = panel.getOffsetWidth();
 		height = width;
 
 		initSize();
 		doUpdate();
 
 		panel.add(canvas);
 	}
 
 }
