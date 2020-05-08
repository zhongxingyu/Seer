 package com.mortalpowers.dude.game;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 
 public class DudeGame implements com.badlogic.gdx.ApplicationListener {
 
 	private OrthographicCamera camera;
 	Map gameMap;
 	private Rectangle r = null;
 	private Slider s = null;
 	private int screenWidth = 1;
 	private int screenHeight = 1;
 
 	Player player;
 	
 	@Override
 	public void create() {
 		gameMap = new Map();
 		player = new Player();
 		s = new Slider();
 
 		r = new Rectangle(4, 0, 1, 1);
 	}
 
 	public void acceptInput() {
 		if(Gdx.input.justTouched()) {
 			System.out.println(Gdx.input.getX() + "x" + Gdx.input.getY());
 		}
 		if (Gdx.input.isTouched()) {
 			s.setVal(1f + -1f * ((float)Gdx.input.getY()) / ((float)screenHeight));
			player.setTallness(s.getVal());
 		}
 	}
 
 	@Override
 	public void render() {
 		acceptInput();
 		updatePlayerPosition();
 		camera.update();
         camera.apply(Gdx.gl10);
 		Gdx.graphics.getGL10().glClear(GL10.GL_COLOR_BUFFER_BIT);
 		gameMap.render();
 		
 
 		r.render();
 		s.render();
 		player.render();
 	}
 	
 	public void updatePlayerPosition() {
 		float proposedX = player.getPositionX() + player.getSpeedX();
 		float proposedY = player.getPositionY() + player.getSpeedY();
 		
 		if (proposedX + player.getWidth() > 12) {
 			player.setSpeedX(-1 * Math.abs(player.getSpeedX()));
 		}
 		
 		if (proposedX < 0) {
 			player.setSpeedX(Math.abs(player.getSpeedX()));
 		}
 		
 		float finalX = player.getPositionX() + player.getSpeedX();
 		float finalY = player.getPositionY() + player.getSpeedY();
 		player.setPosition(finalX, finalY);
 	}
 
 	@Override
 	public void resize(int width, int height) {
 		camera = new OrthographicCamera(Util.xBlocks, Util.yBlocks);
 		// Game has 12 blocks wide, 10 blocks high
 		screenWidth = width;
 		screenHeight = height;
 	}
 
 	@Override
 	public void resume() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void dispose() {
 
 	}
 
 	@Override
 	public void pause() {
 		// TODO Auto-generated method stub
 
 	}
 
 }
