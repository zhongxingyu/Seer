 package data;
 
 import java.awt.Color;
 import java.util.ArrayList;
 
 import main.God;
 import main.ShapeList;
 
 public class ShapeData {
 	
 	/** Tells us which shape is currently selected */
 	public ArrayList<Shape> shapes = new ArrayList<Shape>();
 	public Color shapeColor = new Color(0,0,0);
 	public int listSelection = -1;
 
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
 		shapes.add(shape);
 	}
 	
 	public void removeShape() {
 		shapes.remove(listSelection);
		listSelection = -1;
 	}
 	
 	public Shape getLastShape() {
 		return shapes.get(shapes.size()-1);
 	}
 	
 	
 	public void setColor(Color newColor) {
 		shapeColor = newColor;
 		if (shapes.size() != 0) {
 			shapes.get(shapes.size()-1).setColor(newColor);
 		}
 	}
 
 	/**
 	 * Ends the current shape (which is last in the list of shapes) by 
 	 * adding the first vertex to the end
 	 */
 	public Shape endShape() 
 	{
 		if (shapes.size() > 0) {
 			if (shapes.get(shapes.size()-1).size() > 2) {
 				Shape lastShape = shapes.get(shapes.size()-1);
 				lastShape.add(lastShape.get(0));
 				ShapeList.addShape(lastShape);
 				return lastShape;
 			}	
 			else
 			{
 				System.out.println("Need at least 3 vertices to make a shape polygon!");
 			}
 		}
 		return null;
 	}
 	
 	public Shape getShape(int index) {
 		return shapes.get(index);
 	}
 	
 }
