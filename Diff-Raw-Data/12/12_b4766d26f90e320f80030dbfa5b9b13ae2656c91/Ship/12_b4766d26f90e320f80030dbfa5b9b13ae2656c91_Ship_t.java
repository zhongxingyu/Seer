 package com.badlogic.gamedev.spaceinvaders.simulation;
 
 import java.io.Serializable;
 
 public class Ship implements Serializable
 {
 	public static final float SHIP_RADIUS = 1;
 	public static final float SHIP_VELOCITY = 20;
	public final Vector position = new Vector();
 	public int lives = 3;
 	public boolean isExploding = false;
 	public float explodeTime = 0;		
 	
 	public void update( float delta )
 	{
 		if( isExploding )
 		{
 			explodeTime += delta;
 			if( explodeTime > Explosion.EXPLOSION_LIVE_TIME )
 			{
 				isExploding = false;
 				explodeTime = 0;
 			}
 		}
 	}
 }
