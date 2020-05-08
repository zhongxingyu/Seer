 package edu.ucsb.cs56.S11.lkhuu.issue0000355;
 
 import java.awt.geom.GeneralPath; // combinations of lines and curves
 import java.awt.geom.AffineTransform; // translation, rotation, scale
 import java.awt.Shape; // general class for shapes
 import java.awt.geom.Point2D; 
 import java.awt.geom.Line2D; 
 import java.awt.geom.Rectangle2D;
 import java.awt.Rectangle;
 import java.awt.geom.PathIterator;
 import java.awt.geom.AffineTransform;
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 
 /**
    A shark class that creates a shark that faces 
    in the opposite direction of the fish moving
    across the screen.
       
    @author Casey Barbello
    @author Daryl Pham
    @version for CS56, proj01, Spring 2013, UCSB
 */
 public class Shark extends GeneralPathWrapper implements Shape{
     /**
        Constructor
 
        @param xPos x coord of the middle of the Shark
        @param yPos y coord of the middle of the Shark
     */
 
     int xPos;
     int yPos;
 
     /**
       Holds the shark's position on the Panel
     **/
     public Shark(int x, int y) {
 	this.xPos = x;
 	this.yPos = y;
     }
 
     /**
        gets the xLocation of the shark
        @return the x position of the shark
     **/
     public int getXPos(){
 	return xPos;
     }
 
     /**
        gets the yLocation of the shark
        @return the y position of the shark
     **/
     public int getYPos(){
         return yPos;
     }
     
     /**
        sets the xLocation of the shark
        @return set the x position of the shark
     **/
 	public void setX(int x){
 		xPos = x;
 	}
 	
     /**
        sets the yLocation of the shark
        @return set the y position of the shark
     **/
 	public void setY(int y){
 		yPos = y;
 	}
 }
