 package com.pix.mind.levels;
 
 import com.badlogic.gdx.graphics.Color;
 import com.pix.mind.PixMindGame;
 import com.pix.mind.actors.PlatformActivatorActor;
 import com.pix.mind.actors.StaticPlatformActor;
 import com.pix.mind.world.PixMindWorldRenderer;
 
 public class Level14 extends PixMindLevel {
 	public String levelTitle = "Level14";
 	PixMindGame game;
 	private static final int nActiveColors = 2;
 	public Level14(PixMindGame game) {
         super(game, 1280, 1024, 600, 1, 6, nActiveColors);
 		this.game = game;
 		levelNumber = 14;
 	}
 
 	@Override
 	public void show() {
 		super.show();
		super.setNextLevel(game.getLevel14());
 		super.setActiveLevel(this);
 
 		// platform Actors and Activator Actors List
 		// Creating All Static Platforms
 		float platW = 1f;
 		float platH = 0.1f;
         float deltaX = 0f;
 
 		// Active colors
 		// Box2D platforms
 		// Add to platform list
 		// Black StaticPlatforms
 		box2D.getPlatformList().add(
                 new StaticPlatformActor(box2D.getWorld(), 0 + deltaX - platW/8, 5, platW + platW/8, platH, Color.BLACK, true));
 
 		// Coloured StaticPlatforms
 		box2D.getPlatformList().add(
                 new StaticPlatformActor(box2D.getWorld(), 3 + deltaX, 4, platW, platH, Color.ORANGE, false));
 		box2D.getPlatformList().add(
                 new StaticPlatformActor(box2D.getWorld(), 3 + deltaX, 6, platW, platH, Color.ORANGE, false));
 		box2D.getPlatformList().add(
                 new StaticPlatformActor(box2D.getWorld(), 6 + deltaX, 3, platW, platH, Color.GREEN, false));
 		box2D.getPlatformList().add(
                 new StaticPlatformActor(box2D.getWorld(), 9 + deltaX -platW/2, 4, platW/2, platH, Color.ORANGE, false));
 		box2D.getPlatformList().add(
                 new StaticPlatformActor(box2D.getWorld(), 9 + deltaX, 5, platW, platH, Color.ORANGE, false));
 		box2D.getPlatformList().add(
                 new StaticPlatformActor(box2D.getWorld(), 12 + deltaX, 6, platW, platH, Color.BLUE, false));
 		box2D.getPlatformList().add(
                 new StaticPlatformActor(box2D.getWorld(), 6 + deltaX, 7, platW, platH, Color.BLUE, false));
 
 		// Creating All Activator
 		// Box2D Activator adding to activator list
 		box2D.getActivatorList().add(
             new PlatformActivatorActor(box2D.getWorld(), 13 + deltaX, 3, Color.BLACK, true));
 		box2D.getActivatorList().add(
             new PlatformActivatorActor(box2D.getWorld(), -1f + deltaX, 5.5f, Color.BLUE, false));
 		box2D.getActivatorList().add(
             new PlatformActivatorActor(box2D.getWorld(), -0.5f + deltaX, 5.5f, Color.GREEN, false));
 		box2D.getActivatorList().add(
             new PlatformActivatorActor(box2D.getWorld(), 0f + deltaX, 5.5f, Color.ORANGE, false));
 
 		// Add activators to Stage
 		for (PlatformActivatorActor Sskin : box2D.getActivatorList()) {
 			scene2D.getGroupStage().addActor(Sskin);
 		}
 
 		// Add platforms to Stage
 		for (StaticPlatformActor Sskin : box2D.getPlatformList()) {
 			scene2D.getGroupStage().addActor(Sskin);
 		}
 
 		// Rendering the game
 		worldRenderer = new PixMindWorldRenderer(scene2D, box2D, gui);
 	}
 
 	@Override
 	public void render(float delta) {
 		super.render(delta);
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.pix.mind.levels.PixMindLevel#resize(int, int)
 	 */
 	@Override
 	public void resize(int width, int height) {
 		// TODO Auto-generated method stub
 		super.resize(width, height);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.pix.mind.levels.PixMindLevel#hide()
 	 */
 	@Override
 	public void hide() {
 		// TODO Auto-generated method stub
 		super.hide();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.pix.mind.levels.PixMindLevel#pause()
 	 */
 	@Override
 	public void pause() {
 		// TODO Auto-generated method stub
 		super.pause();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.pix.mind.levels.PixMindLevel#resume()
 	 */
 	@Override
 	public void resume() {
 		// TODO Auto-generated method stub
 		super.resume();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.pix.mind.levels.PixMindLevel#dispose()
 	 */
 	@Override
 	public void dispose() {
 		// TODO Auto-generated method stub
 	}
 }
