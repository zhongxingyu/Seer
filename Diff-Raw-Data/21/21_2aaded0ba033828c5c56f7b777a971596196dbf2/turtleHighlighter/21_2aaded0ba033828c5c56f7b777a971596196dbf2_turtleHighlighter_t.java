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
	private Turtle myTurtle;
 	
 	public TurtleHighlighter(Turtle turtle){
		myTurtle=turtle;
		updatePosition();
 		
 	}

	private void updatePosition() {
		topLeft=new Location(myTurtle.getLeft(),myTurtle.getTop());
		topRight=new Location(myTurtle.getRight(),myTurtle.getTop());
		botLeft=new Location(myTurtle.getLeft(),myTurtle.getBottom());
		botRight=new Location(myTurtle.getRight(),myTurtle.getBottom());
	}
 	
 	public void paint(Graphics2D pen){
		updatePosition();
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
