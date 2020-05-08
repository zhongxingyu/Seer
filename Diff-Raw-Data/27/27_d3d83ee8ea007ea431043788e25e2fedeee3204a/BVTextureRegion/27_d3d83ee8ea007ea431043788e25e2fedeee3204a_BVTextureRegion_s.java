 package com.mobi.badvibes.view.graphics;
 
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector2;
 import com.mobi.badvibes.view.GameDimension;
 
 public class BVTextureRegion extends TextureRegion {
 	private Rectangle source;
 	private Vector2 scale;
 	private Vector2 scaledDimensions;
 	public BVTextureRegion(Texture t, Rectangle source) {
 		super(t);
 		setRegion((int)source.x, (int)source.y, (int)source.width, (int)source.height);
 		
 		this.source = source;
 		this.scale = GameDimension.Scale();
 		this.scaledDimensions = new Vector2(source.width, source.height).mul(scale.x, scale.y);
 	}
 	
 	public int getWidth(){
		return (int)source.width;
 	}
 	
 	public int getHeight(){
		return (int)source.height;
 	}
 	
 	public void draw(SpriteBatch spriteBatch, Vector2 pos){
         spriteBatch.draw(this,
 				 pos.x, pos.y,
				 source.x, source.y,
				 source.width, source.height,
				 scale.x, -scale.y,
				 0f);
 	}
 	
 	public Vector2 centerAt(Vector2 pos){
 		return pos.cpy().div(2).sub(scaledDimensions.cpy().div(2));
 	}
 }
