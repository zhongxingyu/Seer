 package com.pix.mind.levels;
 
 import com.badlogic.gdx.graphics.Color;
 import com.pix.mind.PixMindGame;
 import com.pix.mind.actors.PlatformActivatorActor;
 import com.pix.mind.actors.StaticPlatformActor;
 import com.pix.mind.world.PixMindWorldRenderer;
 
 public class Level20 extends PixMindLevel {
 	public String levelTitle = "Level20";
 	PixMindGame game;
	private static final int nActiveColors = 2;
 	public Level20(PixMindGame game) {
         super(game, 1280, 1024, 600, 8, 5, nActiveColors);
 		this.game = game;
 		levelNumber = 20;
 	}
 
 	@Override
 	public void show() {
 		super.show();
 		super.setNextLevel(game.getLevel21());
 		super.setActiveLevel(this);
 
 		// platform Actors and Activator Actors List
 		// Creating All Static Platforms
 		float platW = 1f;
 		float platH = 0.1f;
         float deltaX = -5f;
         float deltaY = -2.5f;
 
 		// Active colors
 		// Box2D platforms
 		// Add to platform list
 		// Black StaticPlatforms
 		box2D.getPlatformList().add(
                 new StaticPlatformActor(box2D.getWorld(), 12 + deltaX, 6 + deltaY, platW, platH, Color.BLACK, true));
 		box2D.getPlatformList().add(
                new StaticPlatformActor(box2D.getWorld(), 12 + deltaX, 5 + deltaY, platW/2, platH, Color.BLACK, true));
//		box2D.getPlatformList().add(
//                new StaticPlatformActor(box2D.getWorld(), 12 + deltaX, 4 + deltaY, platW/2, platH, Color.BLACK, true));
 		box2D.getPlatformList().add(
                 new StaticPlatformActor(box2D.getWorld(), 14 + deltaX, 7 + deltaY, platW/2, platH, Color.BLACK, true));
 		box2D.getPlatformList().add(
                 new StaticPlatformActor(box2D.getWorld(), 16 + deltaX, 8 + deltaY, platW, platH, Color.BLACK, true));
 		box2D.getPlatformList().add(
                 new StaticPlatformActor(box2D.getWorld(), 17 + deltaX, 9 + deltaY, platW, platH, Color.BLACK, true));
 		box2D.getPlatformList().add(
                 new StaticPlatformActor(box2D.getWorld(), 16 + deltaX, 10 + deltaY, platW, platH, Color.BLACK, true));
 		box2D.getPlatformList().add(
                 new StaticPlatformActor(box2D.getWorld(), 7 + deltaX, 9 + deltaY, platW, platH, Color.BLACK, true));
 		box2D.getPlatformList().add(
                 new StaticPlatformActor(box2D.getWorld(), 5 + deltaX, 10 + deltaY, platW, platH, Color.BLACK, true));
 
 		// Coloured StaticPlatforms
         box2D.getPlatformList().add(
                new StaticPlatformActor(box2D.getWorld(), 12 + deltaX, 4 + deltaY, platW, platH, Color.BLUE, false));
         box2D.getPlatformList().add(
                 new StaticPlatformActor(box2D.getWorld(), 14 + deltaX, 11 + deltaY, platW, platH, Color.ORANGE, false));
         box2D.getPlatformList().add(
                 new StaticPlatformActor(box2D.getWorld(), 10 + deltaX, 8 + deltaY, platW, platH, Color.RED, false));
         box2D.getPlatformList().add(
                 new StaticPlatformActor(box2D.getWorld(), 5 + deltaX, 11 + deltaY, platW/2, platH, Color.BLUE, false));
 
 		// Creating All Activator
 		// Box2D Activator adding to activator list
 		box2D.getActivatorList().add(
             new PlatformActivatorActor(box2D.getWorld(), 6 + deltaX, 12 + deltaY, Color.BLACK, true));
 		box2D.getActivatorList().add(
            new PlatformActivatorActor(box2D.getWorld(), 12 + deltaX, 5.5f + deltaY, Color.ORANGE, false));
 		box2D.getActivatorList().add(
             new PlatformActivatorActor(box2D.getWorld(), 11 + deltaX, 9 + deltaY, Color.RED, false));
 		box2D.getActivatorList().add(
             new PlatformActivatorActor(box2D.getWorld(), 11.5f + deltaX, 10 + deltaY, Color.RED, false));
 		box2D.getActivatorList().add(
             new PlatformActivatorActor(box2D.getWorld(), 15 + deltaX, 12 + deltaY, Color.RED, false));
 		box2D.getActivatorList().add(
             new PlatformActivatorActor(box2D.getWorld(), 6.5f + deltaX, 10 + deltaY, Color.BLUE, false));
 		box2D.getActivatorList().add(
             new PlatformActivatorActor(box2D.getWorld(), 12 + deltaX, 6.5f + deltaY, Color.BLUE, false));
 
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
