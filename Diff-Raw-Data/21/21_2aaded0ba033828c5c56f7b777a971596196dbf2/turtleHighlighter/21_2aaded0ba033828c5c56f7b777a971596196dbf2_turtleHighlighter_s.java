 package model;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 
 import util.Location;
 import util.Paintable;
 
 public class TurtleHighlighter implements Paintable{
 	private Location topLeft;
 	private Location topRight;
 	private Location botLeft;
 	private Location botRight;
 	
 	public TurtleHighlighter(Turtle turtle){
		topLeft=new Location(turtle.getLeft(),turtle.getTop());
		topRight=new Location(turtle.getRight(),turtle.getTop());
		botLeft=new Location(turtle.getLeft(),turtle.getBottom());
		botRight=new Location(turtle.getRight(),turtle.getBottom());
 		
 	}
 	
 	public void paint(Graphics2D pen){
 		pen.setColor(Color.RED);
 		//draw top
 		pen.drawLine((int)topLeft.getX(), (int)topLeft.getY(), (int)topRight.getX(), (int)topRight.getY());
 		//draw right
 		pen.drawLine((int)topRight.getX(), (int)topRight.getY(), (int)botRight.getX(), (int)botRight.getY());
 		//draw bot
 		pen.drawLine((int)botLeft.getX(), (int)botLeft.getY(), (int)botRight.getX(), (int)botRight.getY());
 		//draw left
 		pen.drawLine((int)topLeft.getX(), (int)topLeft.getY(), (int)botLeft.getX(), (int)botLeft.getY());
 		
		
		
 	}
 
 }
