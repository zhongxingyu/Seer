 package com.jaredpearson.game.demo;
 
import com.jaredpearson.game.core.ArcDegree;
 import com.jaredpearson.game.core.Color;
 import com.jaredpearson.game.platform.Drawable;
 import com.jaredpearson.game.platform.Graphics2D;
 import com.jaredpearson.game.platform.Player;
 
 /**
  * Represents the UI of the player
  * @author jaredp
  */
 public class PlayerUI
 	implements Drawable
 {
 	private int radius = 5;
 	private Player player;
 	
 	public PlayerUI(Player player) 
 	{
 		this.player = player;
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	public void draw(Graphics2D graphics) 
 	{
 		graphics.setColor(Color.WHITE);
 		
 		//draw the circle around the user
 		int diameter = radius * 2;
 		graphics.drawArc((int)player.getX() - radius, (int)player.getY() - radius, diameter, diameter, 0, 360);
 		
 		//draw the dot of the user
 		graphics.drawLine((int)player.getX(), (int)player.getY(), (int)player.getX(), (int)player.getY());
 		
 		//draw the line to determine facing
 		this.drawFacingLine(graphics);
 	}
 	
 	private void drawFacingLine(Graphics2D graphics)
 	{
 		int facingLineLength = 5;
		
		//adjust the degrees by 90 counter clockwise
		ArcDegree arcDegree = player.getFacing().add(new ArcDegree(-90)).toDegree();
		
		double playerFacingRadian = arcDegree.toRadian().toDouble();
 		
 		//calculate using parametric equation for a circle
 		double x = player.getX() + (facingLineLength * Math.cos(playerFacingRadian));
 		double y = player.getY() + (facingLineLength * Math.sin(playerFacingRadian));
 		
 		graphics.drawLine(
 				(int)player.getX(), 
 				(int)player.getY(), 
 				(int)x, 
 				(int)y);
 	}
 }
