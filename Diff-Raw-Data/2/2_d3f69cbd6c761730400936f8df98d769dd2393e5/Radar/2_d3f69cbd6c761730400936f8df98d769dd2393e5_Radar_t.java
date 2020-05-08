 package com.game.rania.model;
 
 import java.util.Vector;
 
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Vector2;
 import com.game.rania.model.element.HUDDynamicObject;
 import com.game.rania.model.element.Object;
 import com.game.rania.model.element.RegionID;
 
 public class Radar extends HUDDynamicObject{
 	
 	private Vector<Object> objects = new Vector<Object>();
 	private float radius = 0.0f; 
 	private float mulScale = 1.0f;
 	private Vector2 posObject = new Vector2();
 	private Vector2 scaleObject = new Vector2();
 	private Color   colorObject = new Color();
 	private Player player = null;
 
 	public Radar(Player player, float x, float y, float radius, float mulScale) {
 		super(RegionID.RADAR, x, y);
 		this.radius = radius;
 		this.mulScale = mulScale;
 		this.player = player;
 	}
 	
 	public void addObject(Object object){
 		objects.add(object);
 	}
 	
 	public void removeObject(Object object){
 		objects.remove(object);
 	}
 	
 	public void removeObject(int num){
 		objects.remove(num);
 	}
 	
 	public float speedRotate = 270.0f;
 	
 	@Override 
 	public void update(float deltaTime){
 		angle += deltaTime * -speedRotate;
 		if (angle < -360.0f)
 			angle += 360.0f;
 	}
 	
 	@Override
 	public boolean draw(SpriteBatch sprite){
 		if (player == null || !super.draw(sprite))
 			return false;
 		
 		for (Object object : objects) {
 			
 			posObject.set(object.position);
 			posObject.sub(player.position);
 			if (posObject.len() > radius)
 				continue;
 
 			posObject.mul(scale.x * region.getRegionWidth() * 0.5f / radius, scale.y * region.getRegionHeight() * 0.5f / radius);
 			posObject.add(position);
 
 			scaleObject.set(object.scale.x, object.scale.y);
			scaleObject.mul(scale.x * mulScale * region.getRegionWidth() * 0.5f / radius, scale.y * mulScale * region.getRegionHeight() * 0.5f / radius);
 			
 			colorObject.set(object.color);
 			colorObject.mul(color);
 			colorObject.a = 0.85f;
 			
 			object.draw(sprite, posObject, object.angle, scaleObject, colorObject);
 		}
 		
 		return true;
 	}
 	
 }
