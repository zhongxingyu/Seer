 package com.pix.mind.screens;
 
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas;
 import com.badlogic.gdx.scenes.scene2d.ui.Skin;
 import com.pix.mind.PixMindGame;
 
 public class SplashScreen implements  Screen{
 	private PixMindGame game;
 	public SplashScreen(PixMindGame game) {
 		// TODO Auto-generated constructor stub
 		this.game = game;
 	}
 
 	@Override
 	public void render(float delta) {
 		// TODO Auto-generated method stub
	
		if (game.getAssetManager().update()) {
 	
 		game.setSkin(new Skin(game.getAssetManager().get(
 				"data/textureatlas/PixmindTextureAtlas.pack", TextureAtlas.class)));
 		game.changeLevel(game.getFirstLevel());
 		}
 	}
 
 	@Override
 	public void resize(int width, int height) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void show() {
 		// TODO Auto-generated method stub
 		game.getAssetManager().load("data/textureatlas/PixmindTextureAtlas.pack", TextureAtlas.class);
 
 	}
 
 	@Override
 	public void hide() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void pause() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void resume() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void dispose() {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
