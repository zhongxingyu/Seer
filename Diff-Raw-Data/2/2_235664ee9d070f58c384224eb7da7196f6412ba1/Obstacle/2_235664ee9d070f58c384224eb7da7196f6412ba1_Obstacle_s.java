 package com.me.mygdxgame;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.MathUtils;
 import com.badlogic.gdx.math.Rectangle;
 
 public class Obstacle {
 	//Assets
 	private Texture image;
 	//End Assets
 	
 	protected Rectangle hitbox;
 	private float RESW;
 	private float RESH;
 	final int SPRITE_WIDTH = 96;
 	final int SPRITE_HEIGHT = 96;
 	final int SCALE = 1;
 	final int WIDTH = SPRITE_WIDTH * SCALE;
 	final int HEIGHT = SPRITE_HEIGHT * SCALE;
 	private OrthographicCamera camera;
 	private float floorHeight;
 	protected boolean released;
 
 	Obstacle(OrthographicCamera pcamera, float x, float y) {
 		RESW = pcamera.viewportHeight;
 		RESH = pcamera.viewportHeight;
 		camera = pcamera;
 		floorHeight = y;
 		create(x, y);
 	}
 
 	public void create(float x, float y) {
 		image = new Texture(Gdx.files.internal("data/waterdrop.png"));
 		hitbox = new Rectangle(x, y, SPRITE_WIDTH,
 				SPRITE_HEIGHT);
 	}
 
 	public void draw(SpriteBatch batch) {
 
 		batch.draw(image, hitbox.x, hitbox.y);
 	}
 
 	public void dispose() {
 		image.dispose();
 	}
 
 	public boolean isOffScreen() {
		if (hitbox.x < camera.position.x - RESW/2 - SPRITE_WIDTH) {
 			return true;
 		}
 		else {
 			return false;
 		}
 	}
 
 
 
 }
