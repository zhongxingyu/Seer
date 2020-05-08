 package com.me.mygdxgame;
 
 import java.util.Vector;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector2;
 
 public class Character extends MyGdxGame 
 {
 	int score;
 	public Texture image;
 	int speed;
 	float dTime;
	Vector2 position = new Vector2 (305.24252f,228.5503f);
 	RectangleEx rect = new RectangleEx();
 	public Character()
 	{
 		speed = 500;
 		dTime = Gdx.graphics.getDeltaTime();
 		characterimage();
 		updateRectPos();
		rect.width = 86;
		rect.height = 64;
 	}
 
 	public void eat()
 	{
 		score++;
 	}
 	
 	public void updateRectPos()
 	{
 		rect.x = position.x;
 		rect.y = position.y;
 	}
 	
 	public void setPosition(Vector2 v)
 	{
 		this.position.x = v.x;
 		this.position.y = v.y;
 		updateRectPos();
 	}
 	
 	public void characterimage()
 	{
 		image = new Texture(Gdx.files.internal("data/Bill head.jpg"));
 	}
 		
 }
