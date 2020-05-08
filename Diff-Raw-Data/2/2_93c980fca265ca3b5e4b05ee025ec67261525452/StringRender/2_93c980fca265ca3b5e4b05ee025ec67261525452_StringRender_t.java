 package ui.render;
 
 import java.awt.Color;
 
 public class StringRender 
 {
 	private String text;
 	private int x;
 	private int y;
 	private Color color;
 	
 	public StringRender()
 	{
 		this("", 0, 0, Color.BLACK);
 	}
 	
 	public StringRender(String text, int x, int y)
 	{	
		this(text, x, y, Color.BLACK);
 	}
 
 	public StringRender(String text, int x, int y, Color color)
 	{
 		this.text = text;
 		this.x = x;
 		this.y = y;
 		this.color = color;
 	}
 	
 	public void setText(String text)
 	{
 		this.text = text;
 	}
 	
 	public String getText()
 	{
 		return text;
 	}
 	
 	public void setX(int x)
 	{
 		this.x = x;
 	}
 	
 	public int getX()
 	{
 		return x;
 	}
 	
 	public void setY(int y)
 	{
 		this.y = y;
 	}
 	
 	public int getY()
 	{
 		return y;
 	}
 	
 	public Color getColor()
 	{
 		return color;
 	}
 	
 	public void setColor(Color color)
 	{
 		this.color = color;
 	}
 }
