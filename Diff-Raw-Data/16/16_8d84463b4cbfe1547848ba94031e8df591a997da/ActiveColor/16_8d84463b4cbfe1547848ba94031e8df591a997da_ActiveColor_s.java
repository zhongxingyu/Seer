 package com.pix.mind.actors;
 
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.pix.mind.PixMindGame;
 
 public class ActiveColor extends Image {
 	
 	public ActiveColor(Color color) {
		super(PixMindGame.getSkin().getDrawable("candy1"));
 		float radius = 20;
 		this.setSize(radius * 2, radius * 2);
 		this.setColor(color);
 	}
 	
 	@Override
 	public void draw(SpriteBatch batch, float parentAlpha) {
 		super.draw(batch, parentAlpha);
 	
 	}
 }
