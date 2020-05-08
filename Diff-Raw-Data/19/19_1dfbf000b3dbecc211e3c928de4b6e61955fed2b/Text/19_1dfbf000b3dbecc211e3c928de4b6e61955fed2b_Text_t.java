 package main;
 import main.*;
 import org.newdawn.slick.*;
import org.newdawn.slick.Font;
 
 public class Text {
	public Text()
	{
	}
 	public void draw(Graphics g, Color color, String text, int x, int y)
 	{
 		g.setColor(color);
 		g.drawString(text, x, y);
 	}
 }
