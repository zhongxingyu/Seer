 package event;
 
 import java.awt.Color;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 import enums.Actions;
 
 import model.Model;
 import shapes.ClosedShape;
 import shapes.Rectangle;
 import shapes.Shape;
 
 /**
  * Handles the actions when the mouse interacts with the applet.
  * @author Stephen
  *
  */
 public class ShapeMouseHandler extends MouseAdapter{
 	private Model model;
 	private int startX;
 	private int startY;
 	private Shape shape;
 
 	/**
 	 * Constructor. Sets the model for this listener.
 	 * 
 	 * @param model
 	 */
 	public ShapeMouseHandler(Model model) {
 		//persist local variable model to this.model
 		this.model = model;
 	}
 
 	/*
 	 * Overrides MouseAdapter mousePressed method.
 	 */
 	public void mousePressed(MouseEvent e) {
 		switch(model.getAction()) {
 		case DRAW: drawPress(e); break;
 		case REMOVE: remove(e); break;
 		case CHANGE: change(e); break;
 		default: break;
 		}
 	}
 
 	/*
 	 * Overrides MouseAdapter's mouseDragged method and determines what to do based on the selected action.
 	 */
 	public void mouseDragged(MouseEvent e) {
 		switch (model.getAction()) {
 		case DRAW: drawDragged(e); break;
 		case MOVE: move(e); break;
 		case RESIZE: resize(e); break;
 		default: break;
 		}
 		model.repaint();
 		System.out.println("Mouse Dragged Called");
 	}
 
 	/*
 	 * Draws the currentShape where the mouse is pressed. Has a predefined size.
 	 */
 	private void drawPress(MouseEvent e) {
 		//original upper left x and y of the shape.
 		setFirstAnchorPoint(e);
 		//have the model create a new shape
 		shape = model.createShape();
 		//if the shape was created.
 		System.out.println(shape);
 		if (shape != null) {
 			setInitialSpatialDimensions();
 			shape.setLineColor(model.getLineColor());
 			if(shape instanceof ClosedShape) {
 				setFill((ClosedShape)shape);
 			}
 			System.out.println(shape);
 		}
 		//tell the model to repaint the applet or application.
 		model.repaint();
 	}
 	
 	/*
 	 * Sets the X1 and Y1 values at the point where the mouse is first clicked.
 	 */
 	private void setFirstAnchorPoint(MouseEvent e) {
 		startX = e.getX();
 		startY = e.getY();
 	}
 	
 	/*
 	 * Sets the top left corner of the given shape where the mouse was pressed
 	 * and set the default width and height of the shape.
 	 */
 	private void setInitialSpatialDimensions() {
 		//set its upper left x and y to where the mouse was pressed.
 		shape.setX1(startX);
 		shape.setY1(startY);
 		shape.setX2(startX + Model.DEFAULT_WIDTH);
 		shape.setY2(startY + Model.DEFAULT_HEIGHT);
 	}
 	
 	/*
 	 * Sets the fill color of the shape to the fill color in the model.
 	 * Sets the fill state to the fill state of the model.
 	 */
 	private void setFill(ClosedShape shape) {
 		((ClosedShape)shape).setFillColor(model.getFillColor());
 		((ClosedShape)shape).setFilled(model.isFill());
 	}
 
 	/*
 	 * Removes the shape under the mouse from the canvas.
 	 */
 	private void remove(MouseEvent e) {
 		System.out.println("Remove");
 		System.out.println(model.getShapeArray().remove(model.getShapeAt(e.getX(), e.getY())));
 		model.getShapeArray().trimToSize();
 		model.repaint();
 	}
 
 	/*
 	 * Takes the settings for fill, fill color, and line color and applies them to the shape under the mouse.
 	 */
 	private void change(MouseEvent e) {
 		//Change line color, fill color, and fill status to current values on panel
		Shape shape = model.getShapeAt(e.getX(), e.getY());
 		shape.setLineColor(model.getLineColor());
 		//Check to see if it is a closed shape - if it is, change the fill attributes
 		if(shape instanceof ClosedShape) {
 			setFill((ClosedShape)shape);
 		}
 		model.repaint();
 	}
 
 	/*
 	 * Draws the current shape as the mouse is dragged across the screen.
 	 */
 	private void drawDragged(MouseEvent e) {
 		shape = model.getCurrentShape();
 		if(shape != null) {
 			setFirstAnchorPoint(e);
 			System.out.println(shape);
 			setSecondAnchorPoint(e);
 			System.out.println(shape);
 		}
 	}
 
 	/*
 	 * Moves the shape under the mouse from where it is to a new point. It is translated on the same vector as the mouse movement.
 	 */
 	private void move(MouseEvent e) {
 		if(shape == null) shape = model.getShapeAt(e.getX(), e.getY());
 		if(!shape.isMarkedSelected()) {
 			indicateSelectedShape(shape, true);
 			shape.setMarkedSelected(true);
 		}
 		if(shape != null) {
 			setMovements(getDifference(e));
 			System.out.println(shape);
 		}
 	}
 	
 	/*
 	 * Takes the given shape and changes the lineColor and fillColor to be a little darker to indicate selection.
 	 * If selected is false, it returns the shape colors to the original state.
 	 * Pre-condition: There is a shape available, and selected is true or false
 	 * Post-condition: If selected is false, the shape colors are returned to a ligher hue.
 	 * If selected is true, the shape colors are darkened.
 	 */
 	private void indicateSelectedShape(Shape selectedShape, boolean markSelected) {
 		if(markSelected) {
 			darkenHue(selectedShape);
 		} else {
 			lightenHue(selectedShape);
 		}
 	}
 
 	/*
 	 * Make the shape lineColor and fillColor darker
 	 */
 	private void darkenHue(Shape selectedShape) {
 		Color darkerLineColor = selectedShape.getLineColor().darker().darker().darker();
 		//Set the line color to the darker line color unless that is black, then set it to white.
 		selectedShape.setLineColor((selectedShape.getLineColor() == Color.BLACK) ? Color.WHITE : darkerLineColor);
 		if(selectedShape instanceof ClosedShape) {
 			Color darkerFillColor = ((ClosedShape)selectedShape).getFillColor().darker().darker().darker();
 			//Set the line color to the darker fill color unless that is black, then set it to white.
 			((ClosedShape)selectedShape).setFillColor((((ClosedShape) selectedShape).getFillColor() == Color.BLACK) ? Color.WHITE : darkerFillColor);
 		}
 	}
 
 	/*
 	 * Make the shape lineColor and fillColor lighter
 	 */
 	private void lightenHue(Shape selectedShape) {
 		Color lighterLineColor = selectedShape.getLineColor().brighter().brighter().brighter();
 		//Set the line color to the lighter line color unless the current line color is white, then set it to black.
 		//We do this because that is the opposite of what darkenHue does, and it will return the shape to it's original state.
 		selectedShape.setLineColor((selectedShape.getLineColor() == Color.WHITE) ? Color.BLACK : lighterLineColor);
 		if(selectedShape instanceof ClosedShape) {
 			Color lighterFillColor = ((ClosedShape) selectedShape).getFillColor().brighter().brighter().brighter();
 			//Set the line color to the lighter fill color unless the current fill color is white, then set it to black.
 			//We do this because that is the opposite of what darkenHue does, and it will return the shape to it's original state.
 			((ClosedShape)selectedShape).setFillColor((((ClosedShape) selectedShape).getFillColor() == Color.WHITE) ? Color.BLACK : lighterFillColor);
 		}
 	}
 
 	/*
 	 * Finds the differences between X1 and X1 and Y1 and Y2
 	 * Returns an array of two integers with the difference values, X first Y second.
 	 */
 	private int[] getDifference(MouseEvent e) {
 		int initialX1 = shape.getX1();
 		int initialY1 = shape.getY1();
 		int differenceX = e.getX() - initialX1;
 		int differenceY = e.getY() - initialY1;
 		int[] differenceValues = {differenceX, differenceY};
 		return differenceValues;
 	}
 	
 	/*
 	 * Takes an array of two integers, and sets the coordinates of the shape to the
 	 * old coordinates plus the values in the array. First element is added to the
 	 * X values, second element is added to the Y values.
 	 */
 	private void setMovements(int[] differenceValues) {
 		shape.setX1(shape.getX1() + differenceValues[0]);
 		shape.setY1(shape.getY1() + differenceValues[1]);
 		shape.setX2(shape.getX2() + differenceValues[0]);
 		shape.setY2(shape.getY2() + differenceValues[1]);
 	}
 
 	/*
 	 * Resizes the shape under the mouse, moving X2 and Y2 to the new point given by the MouseEvent e.
 	 */
 	private void resize(MouseEvent e) {
 		//Rezize
 		if(shape == null) shape = model.getShapeAt(e.getX(), e.getY());
 		if(!shape.isMarkedSelected()) {
 			indicateSelectedShape(shape, true);
 			shape.setMarkedSelected(true);
 		}
 		if(shape != null) {
 			System.out.println(shape);
 			//change the position of one corner, thereby stretching the shape.
 			setSecondAnchorPoint(e);
 			System.out.println(shape);
 		}
 	}
 	
 	/*
 	 * Sets the X2 and Y2 values at the point where the mouse is released.
 	 */
 	public void setSecondAnchorPoint(MouseEvent e) {
 		shape.setX2(e.getX());
 		shape.setY2(e.getY());
 	}
 
 	/*
 	 * Changes the visual selected status once dragging is done and clears the current shape.
 	 * (non-Javadoc)
 	 * @see java.awt.event.MouseAdapter#mouseReleased(java.awt.event.MouseEvent)
 	 */
 	public void mouseReleased(MouseEvent e) {
		if(shape.isMarkedSelected()) {
 			indicateSelectedShape(shape, false);
 			shape.setMarkedSelected(false);
 		}
		shape = null;
 		model.repaint();
 	}
 
 }
