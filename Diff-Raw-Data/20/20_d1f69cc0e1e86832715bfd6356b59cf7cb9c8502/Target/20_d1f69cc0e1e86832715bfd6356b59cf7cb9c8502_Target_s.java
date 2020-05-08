 package com.game.rania.model;
 
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.game.rania.model.element.Object;
 import com.game.rania.model.element.RegionID;
 
 public class Target extends Object{
 
 	public static final int none = 0;
 	public static final int planet = 1;
 	public static final int user = 2;
 	public static final int item = 3;
 	public static final int star = 4;
 	
 	public Object object = null;
 	public int id = 0;
 	public int type = 0;
 	
 	public Target(int id, int type, Object objectTarget)
 	{
 		super(RegionID.TARGET, 0, 0);
 		this.object = objectTarget;
 		this.id = id;
 		this.type = type;
 	}
 
 	@Override
 	public boolean draw(SpriteBatch sprite){
 		if (object == null || region == null)
 			return false;
		scale.set(object.getWidth() / region.getRegionWidth(), object.getHeight() / region.getRegionHeight());
		angle = object.angle;
 		position.set(object.position);
 		return super.draw(sprite);
 	}
 }
