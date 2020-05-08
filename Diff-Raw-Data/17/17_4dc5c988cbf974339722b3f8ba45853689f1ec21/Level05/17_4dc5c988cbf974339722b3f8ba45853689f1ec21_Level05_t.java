 package com.pix.mind.levels;
 
 import com.badlogic.gdx.graphics.Color;
 import com.pix.mind.PixMindGame;
 import com.pix.mind.actors.PlatformActivatorActor;
 import com.pix.mind.actors.StaticPlatformActor;
 import com.pix.mind.actors.StaticWallActor;
 import com.pix.mind.world.PixMindWorldRenderer;
 
 public class Level05 extends PixMindLevel {
 	public String levelTitle = "Level05";
 	PixMindGame game;
 	private static final int nActiveColors = 1;
 
 	public Level05(PixMindGame game) {
		super(game, 845, 650, 900, 1, 9, nActiveColors);
 		this.game = game;
 		levelNumber = 5;
 	}
 
 	@Override
 	public void show() {
 
 		super.show();
 		super.setNextLevel(game.getLevel06());
 		super.setActiveLevel(this);
 		// platform Actors and Activator Actors List
 		// Creating All Static Platforms
 		float wallW = 0.1f;
 		float wallH = 5;
 		float platW = 1f;
 		float platH = 0.1f;
 
 		// Box2D platforms
 		// Add to platform list
 		// Walls
 		scene2D.getGroupStage().addActor(new StaticWallActor(box2D.getWorld(), 0, wallH, wallW, wallH));
 		scene2D.getGroupStage().addActor(new StaticWallActor(box2D.getWorld(), 4, wallH + wallH / 4, wallW, 3 * wallH / 4));
 		scene2D.getGroupStage().addActor(new StaticWallActor(box2D.getWorld(), 8, wallH, wallW, wallH));
 
 		// Normal StaticPlatforms
 		box2D.getPlatformList().add(
                new StaticPlatformActor(box2D.getWorld(), 1 + wallW / 2, 5, platW - wallW / 2, platH, Color.BLACK, true));
 		box2D.getPlatformList().add(
                new StaticPlatformActor(box2D.getWorld(), 3 - wallW / 2, 5, platW - wallW / 2, platH, Color.BLUE, true));
 		box2D.getPlatformList().add(
                new StaticPlatformActor(box2D.getWorld(), 2f, 3, platW - 0.2f, platH, Color.BLACK, true));
 		box2D.getPlatformList().add(
                 new StaticPlatformActor(box2D.getWorld(), 4f, 1f, platW + 0.4f, platH, Color.ORANGE, false));
 		box2D.getPlatformList().add(
                 new StaticPlatformActor(box2D.getWorld(), 7 - wallW / 2, 2, platW, platH, Color.BLACK, true));
 		box2D.getPlatformList().add(
                 new StaticPlatformActor(box2D.getWorld(), 5 + wallW / 2, 3, platW, platH, Color.BLACK, true));
 		box2D.getPlatformList().add(
                 new StaticPlatformActor(box2D.getWorld(), 7 - wallW / 2, 4, platW, platH, Color.BLACK, true));
 
 		// Add platforms to Stage
 		for (StaticPlatformActor Sskin : box2D.getPlatformList()) {
 			scene2D.getGroupStage().addActor(Sskin);
 		}
 
 		// Creating All Activator
 		// Box2D Activator adding to activator list
 		box2D.getActivatorList().add(
             new PlatformActivatorActor(box2D.getWorld(), 6.5f, 5, Color.BLACK, true));
 		box2D.getActivatorList().add(
            new PlatformActivatorActor(box2D.getWorld(), 3.5f, 6, Color.BLUE, true));
 		box2D.getActivatorList().add(
            new PlatformActivatorActor(box2D.getWorld(), 1, 4, Color.ORANGE, false));
 
 
 		// Add activators to Stage
 		for (PlatformActivatorActor Sskin : box2D.getActivatorList()) {
 			scene2D.getGroupStage().addActor(Sskin);
 		}
 		// Active colors
 		box2D.addActivatedColor(Color.BLUE);
 
 		// Rendering the game
 		worldRenderer = new PixMindWorldRenderer(scene2D, box2D, gui);
 	}
 
 	@Override
 	public void render(float delta) {
 		super.render(delta);
 	}
 
 	@Override
 	public void resize(int width, int height) {
 		super.resize(width, height);
 	}
 
 	@Override
 	public void hide() {
 		super.hide();
 	}
 
 	@Override
 	public void pause() {
 		super.pause();
 	}
 
 	@Override
 	public void resume() {
 		super.resume();
 	}
 
 	@Override
 	public void dispose() {
 	}
 }
