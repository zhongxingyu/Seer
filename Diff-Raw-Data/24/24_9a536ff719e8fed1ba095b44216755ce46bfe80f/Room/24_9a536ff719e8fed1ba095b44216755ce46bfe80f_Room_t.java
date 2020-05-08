 package com.broteam.tipe.shape;
 
 import java.awt.Color;
 import java.awt.Graphics;
 
 public class Room extends Shape {
 
 	Room(Point ptPress, Point ptRel, Color c) {
 		refPressed = ptPress;
 		refReleased = ptRel;
		int px = refPressed.getX();
		int py = refPressed.getY();
		int rx = refReleased.getX();
		int ry = refReleased.getY();
		if ((px > rx) && (py > ry)) {
				refPressed = new Point(rx, ry);
				refReleased = new Point(px, py);
			} else if (px > rx) {
				refPressed = new Point(rx, py);
				refReleased = new Point(px, ry);
			} else if (py > ry) {
				refPressed = new Point(rx, ry);
				refReleased = new Point(px, py);
			}
 		col = c;
 	}
 
 	@Override
 	public void draw(Graphics g) {
 		g.setColor(col);
		int px = refPressed.getX();
		int py = refPressed.getY();
		int rx = refReleased.getX();
		int ry = refReleased.getY();
		g.drawRect(px, py, Math.abs(rx - px), Math.abs(ry - py));
 	}
 }
