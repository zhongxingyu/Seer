 package data;
 
 import java.awt.Color;
 import java.util.ArrayList;
 
 import main.ShapeList;
 
 public class ShapeData {
 	
 	/** Tells us which shape is currently selected */
 	public int index = -1;
 	public ArrayList<Shape> shapes = new ArrayList<Shape>();
 	public Color shapeColor = new Color(0,0,0);
 	
 	public int getIndex() {
 		return index;
 	}
 
 	public void setIndex(int index) {
 		this.index = index;
 	}
 
 	public ArrayList<Shape> getShapes() {
 		return shapes;
 	}
 
 	public void setShapes(ArrayList<Shape> shapes) {
 		this.shapes = shapes;
 	}
 
 	public Color getShapeColor() {
 		return shapeColor;
 	}
 
 	public void setShapeColor(Color shapeColor) {
 		this.shapeColor = shapeColor;
 	}
 
 	/**
 	 * Add a shape to the list of shapes to be drawn
 	 * @param shape
 	 */
 	public void addShape(Shape shape) 
 	{
 		shape.setColor(shapeColor);
 		shapes.add(shape);
 		selectShape(shapes.size() - 1);
 		ShapeList.addShape(shape);
 	}
 	
 	public void selectShape(int newIndex) {
 		index = newIndex;
 	}
 	
 	public void setColor(Color newColor) {
 		shapeColor = newColor;
 		if (shapes.size() != 0) {
 			shapes.get(index).setColor(newColor);
 		}
 	}
 
 	/**
 	 * Ends the current shape (which is last in the list of shapes) by 
 	 * adding the first vertex to the end
 	 */
 	public Shape endShape(int index) 
 	{
 		if (shapes.size() > 0) {
 			if (shapes.get(index).size() > 0) {
 				Shape lastShape = shapes.get(index);
 				lastShape.add(lastShape.get(0));
 				return lastShape;
 			}
 		}
 		return null;
 	}
 	
 	public Shape getShape(int index) {
 		return shapes.get(index);
 	}
 }
