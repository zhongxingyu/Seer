 package com.automatonizer.view;
 
 import com.automatonizer.model.State;
 import com.google.gwt.canvas.client.Canvas;
 import com.google.gwt.canvas.dom.client.Context2d;
 import com.google.gwt.dom.client.Style;
 import com.google.gwt.dom.client.Style.BorderStyle;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.event.dom.client.HasAllMouseHandlers;
 import com.google.gwt.touch.client.Point;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.LayoutPanel;
 
 public class AutomatonCanvas extends Composite {
 
 	public final static int PX_HEIGHT = 400;
 	public final static int PX_WIDTH = 800;
 
 	private final static int GRID_SIZE = 20;
 
 	private final Canvas gridCanvas = Canvas.createIfSupported();
 	private final Canvas objectCanvas = Canvas.createIfSupported();
 	private final Canvas textCanvas = Canvas.createIfSupported();
 
 	private final Canvas upmostCanvas = textCanvas;
 
 	public Point offset = new Point(0, 0);
 
 	public AutomatonCanvas() {
 		initCanvas(gridCanvas);
 		initCanvas(objectCanvas);
 		initCanvas(textCanvas);
 
 		LayoutPanel panel = new LayoutPanel();
 		panel.add(gridCanvas);
 		panel.add(objectCanvas);
 		panel.add(textCanvas);
 		initWidget(panel);
 	}
 
 	public void draw() {
 		clearCanvas(gridCanvas);
 		clearCanvas(objectCanvas);
 		clearCanvas(textCanvas);
 		drawGrid();
 
 		new StateView(this, new State("Z1"), new Point(50, 50)).go();
 		new StateView(this, new State("Z2"), new Point(150, 150)).go();
 	}
 
 	public boolean canvasSupported() {
 		return objectCanvas != null && textCanvas != null;
 	}
 
 	public Context2d getObjectContext() {
 		return objectCanvas.getContext2d();
 	}
 
 	public Context2d getTextContext() {
 		return textCanvas.getContext2d();
 	}
	
 	// We need to attach the handlers to the upmost canvas
 	public HasAllMouseHandlers getHandlerCanvas() {
 		return upmostCanvas;
 	}
 
 	private void drawGrid() {
 		Context2d ctx = gridCanvas.getContext2d();
		int x = (int) offset.getX();
		int y = (int) offset.getY();
 
		for (int i = (x == 0 ? x : GRID_SIZE % x); i < PX_HEIGHT; i += GRID_SIZE) {
			ctx.moveTo(0, i);
			ctx.lineTo(PX_WIDTH, i);
 		}
 
		for (int i = (y == 0 ? y : GRID_SIZE % y); i < PX_WIDTH; i += GRID_SIZE) {
			ctx.moveTo(i, 0);
			ctx.lineTo(i, PX_HEIGHT);
 		}
 
 		ctx.setLineWidth(.5);
 		ctx.setStrokeStyle("grey");
 		ctx.stroke();
 	}
 
 	private void initCanvas(Canvas canvas) {
 		canvas.setWidth(PX_WIDTH + "px");
 		canvas.setCoordinateSpaceHeight(PX_HEIGHT);
 		canvas.setCoordinateSpaceWidth(800);
 		canvas.setHeight(PX_HEIGHT + "px");
 		Style canvasStyle = canvas.getElement().getStyle();
 		canvasStyle.setBorderStyle(BorderStyle.SOLID);
 		canvasStyle.setBorderWidth(1, Unit.PX);
 	}
	
 	private void clearCanvas(Canvas canvas) {
 		canvas.setCoordinateSpaceWidth(PX_WIDTH);
 	}
 
 }
