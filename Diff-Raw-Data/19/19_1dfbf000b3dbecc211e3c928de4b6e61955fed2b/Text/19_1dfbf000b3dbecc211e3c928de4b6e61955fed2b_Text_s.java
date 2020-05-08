 package main;
 import main.*;
 import org.newdawn.slick.*;
 
 public class Text {

 	public void draw(Graphics g, Color color, String text, int x, int y)
 	{
 		g.setColor(color);
 		g.drawString(text, x, y);
 	}
 }
