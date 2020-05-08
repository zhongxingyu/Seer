 package com.jordan.tunnelgame;
 
 import com.jordan.framework.Game;
 import com.jordan.framework.Graphics;
 import com.jordan.framework.Screen;
 
 public class ScreenLoading extends Screen {
 	public ScreenLoading(Game game) {
 		super(game);
 	}
 
 	@Override
 	public void update(float deltaTime) {
 		Graphics g = game.getGraphics();
         Assets.load();
 		game.setScreen(new ScreenPackSelect(game));
 	}
 
 	@Override
 	public void paint(float deltaTime) {
 		Graphics g = game.getGraphics();
		g.drawImage(Assets.menu, 0, 0);
 	}
 
 	@Override
 	public void pause() {
 
 	}
 
 	@Override
 	public void resume() {
 
 	}
 
 	@Override
 	public void dispose() {
 
 	}
 
 	@Override
 	public void backButton() {
 
 	}
 }
