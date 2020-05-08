 package com.thisisdinosaur.protosaurus.shared;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 
 import com.thisisdinosaur.protosaurus.client.Displayable;
 
 public class ResourceNodeDrawer implements Displayable
 {
 	private static final Color DEFAULT_CITY_COLOUR = Color.magenta;
 	
 	private static final int CITY_DIAMETER = 20;
 	
 	private ResourceNodeEntity gameEntity;
 	
 	public ResourceNodeDrawer(ResourceNodeEntity gameEntity)
 	{
 		this.gameEntity = gameEntity;
 	}
 
 	@Override
 	public void draw(Graphics2D g)
 	{
 		if(gameEntity.getOwner() == null)
 		{
 			g.setColor(DEFAULT_CITY_COLOUR);
 		}
 		else
 		{
 			g.setColor(Color.CYAN);
 		}
 		
		g.fillOval(-CITY_DIAMETER / 2, -CITY_DIAMETER / 2, CITY_DIAMETER, CITY_DIAMETER);
 	}
 
 	@Override
 	public float getX()
 	{
 		return this.gameEntity.getX();
 	}
 
 	@Override
 	public float getY()
 	{
 		return this.gameEntity.getY();
 	}
 
 	@Override
 	public float getRotation()
 	{
 		//As of right now cities are just circles, so their rotation is meaningless.
 		return 0;
 	}
 
 }
