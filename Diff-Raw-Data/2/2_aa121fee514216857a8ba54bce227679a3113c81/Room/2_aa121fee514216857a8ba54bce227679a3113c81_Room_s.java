 package com.broteam.tipe.shape;
 
 import java.awt.Graphics;
 
 public class Room extends Shape {
 
 	private Point topLeft;
 	private int width;
 	private int height;
 
 	Room(Point refPressed, Point refReleased) {
 		int topLeftX = Math.min(refPressed.x, refReleased.x);
 		int topLeftY = Math.min(refPressed.y, refReleased.y);
 
 		topLeft = new Point(topLeftX, topLeftY);
 
 		width = Math.max(refPressed.x, refReleased.x) - topLeftX;
		height = Math.max(refPressed.x, refReleased.x) - topLeftY;
 	}
 
 	@Override
 	public void draw(Graphics g) {
 		g.setColor(col);
 		int px = topLeft.x;
 		int py = topLeft.y;
 		g.drawRect(px, py, width, height);
 	}
 }
