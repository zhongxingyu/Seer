 package com.sampleshooter;
 
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 
 public class IntroScreen extends Screen {
 	IntroScreen() {
 		spriteBatch = new SpriteBatch();
 	}
 	
 	@Override
 	public void render()
 	{
 		spriteBatch.begin();
		spriteBatch.draw(Art.intro, 0, 0);
 		spriteBatch.end();
 	}
 	
 	@Override
 	public void init(Game game) {
 		
 	}
 
 	@Override
 	public void removed() {
 		
 	}
 }
