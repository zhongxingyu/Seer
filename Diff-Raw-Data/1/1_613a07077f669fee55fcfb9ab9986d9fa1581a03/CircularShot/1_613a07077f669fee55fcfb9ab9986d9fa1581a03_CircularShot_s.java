 package com.tiny.weapons;
 
 import org.newdawn.slick.geom.Circle;
 import org.newdawn.slick.geom.Vector2f;
 
 import com.tiny.tank.Main_Gameplay;
 
 public abstract class CircularShot extends Shot{
 
 	protected int radiusOfEffect;
 	protected int initialRadius;
 	private int storedRadius;
 	
 	public CircularShot(Vector2f pos, int radiusOfEffect, int intialRadius, Object graphicalRep) {
 		super(pos, new Circle(pos.x, pos.y, radiusOfEffect), graphicalRep);
 		// TODO Auto-generated constructor stub
 		this.radiusOfEffect = radiusOfEffect;
 		this.storedRadius = initialRadius;
 		this.initialRadius = storedRadius;
 	}
 	
 	@Override
 	public void init(Vector2f pos){
 		super.init(pos);
 		initialRadius = storedRadius;
 		
 	}
 	
 	public void circleExplosion(int x, int y, int radius){
 		
 		for(int i =x-radius;i<x+radius;i++){
 			for(int j = y-radius;j<y+radius;j++){
 				
 				if((x-i)*(x-i)  + (y-j)*(y-j)< radius*radius){
 					Main_Gameplay.map.getMap().setRGBA(i, j, 0, 0, 0, 0);
 				}
 			}
 		}
 	}
 	
 }
