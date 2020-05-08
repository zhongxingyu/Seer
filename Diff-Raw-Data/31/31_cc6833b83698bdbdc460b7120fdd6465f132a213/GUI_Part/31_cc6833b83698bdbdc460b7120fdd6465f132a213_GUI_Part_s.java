 package gui;
 
 import java.awt.Graphics2D;
 
 import javax.swing.*;
 
 public class GUI_Part implements GUI_Component {
 	
 	ImageIcon background; //image of part
 	String name; //part's name
 	int x,y; //coordinates for parts
 	
 	GUI_Part() //default constructor
 	{
		background = new ImageIcon("fish.png");
 		name = "Blank Fish";
 		x = 0;
 		y = 0;
 	}
 	
 	GUI_Part(String i, String n, int x1, int y1) //constructor
 	{
 		background = new ImageIcon(i);
 		name = n;
 		x = x1;
 		y = y1;
 	}
 	
 	public void setName(String n)//set part's name
 	{
 		name = n;
 	}
 	
 	public void setCoordinates(int x1, int y1)//set part's coordinates
 	{
 		x = x1;
 		y = y1;
 	}
 	
 	public void setImage(ImageIcon image)//set part image
 	{
 		background = image;
 	}
 	
 	public String getName()//get part name
 	{
 		return name;
 	}
 	
 	public int getX()//get part x coordinate
 	{
 		return x;
 	}
 	
 	public int getY()//get part y coordinate
 	{
 		return y;
 	}
 	
 	public ImageIcon getImage()//get part image
 	{
 		return background;
 	}
 
 	public void paintComponent(JPanel j, Graphics2D g)//painter
 	{
 		background.paintIcon(j, g, x, y);	
 	}
 
 	public void updateGraphics() //update method
 	{
 		setCoordinates(x,y);
 	}	
 	
 }
 
