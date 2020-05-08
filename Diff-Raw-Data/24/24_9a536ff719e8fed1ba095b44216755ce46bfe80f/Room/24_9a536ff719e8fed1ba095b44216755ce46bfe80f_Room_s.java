 package com.broteam.tipe.shape;
 
 import java.awt.Color;
 import java.awt.Graphics;
 
 public class Room extends Shape {
 
 	Room(Point ptPress, Point ptRel, Color c) {
 		refPressed = ptPress;
 		refReleased = ptRel;
 		col = c;
 	}
 
 	@Override
 	public void draw(Graphics g) {
 		g.setColor(col);
		// TODO Faire un if pour les rectangles tracé à l'envers c'est à dire
		// pressed à droite du released.
		g.drawRect(refPressed.getX(), refPressed.getY(), refReleased.getX()
				- refPressed.getX(), refReleased.getY() - refPressed.getY());
 	}
 }
