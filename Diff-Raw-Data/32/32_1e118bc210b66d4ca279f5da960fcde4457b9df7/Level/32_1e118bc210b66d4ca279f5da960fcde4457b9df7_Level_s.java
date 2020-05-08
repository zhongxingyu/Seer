 /*****************************************************************************
  * 
  * Copyright 2012-2013 See AUTHORS file.
  * 
  * This file is part of Escape-IR.
  * 
  * Escape-IR is free software: you can redistribute it and/or modify
  * it under the terms of the zlib license. See the COPYING file.
  * 
  *****************************************************************************/
 
 package fr.escape.game.screen;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import fr.escape.Objects;
 import fr.escape.app.Engine;
 import fr.escape.app.Input;
 import fr.escape.app.Input.Action;
 import fr.escape.app.Screen;
 import fr.escape.game.Escape;
 import fr.escape.game.entity.CoordinateConverter;
 import fr.escape.game.entity.ships.Ship;
 import fr.escape.game.scenario.Stage;
 import fr.escape.graphics.RepeatableScrollingTexture;
 import fr.escape.graphics.ScrollingTexture;
 import fr.escape.input.Gesture;
 import fr.escape.input.WeaponGesture;
 import fr.escape.resources.TextureLoader;
 
 /**
  * <p>
  * Display a Screen which handle Game Stage.
  * 
  */
 public final class Level implements Screen {
 	
 	/**
 	 * Class TAG
 	 */
 	private final static String TAG = Level.class.getSimpleName();
 	
 	private final static long STAR_SPEED = 5000;
 	
 	private final Escape game;
 	private final ScrollingTexture star;
 	private final ArrayList<Input> events;
 	
 	private final Stage stage;
 	private final ScrollingTexture background;
 	
 	/**
 	 * Check the end of the current Stage/Level
 	 */
 	private final Runnable finish;
 	
 	/**
 	 * Intro Screen ID
 	 */
 	private final int intro;
 	
 	/**
 	 * Next Screen ID
 	 */
 	private final int next;
 	
 	private long time;
 	private float[] velocity = {0, 0, 0, 0};
 	
 	private boolean accepted = false;
 	private boolean stop = false;
 	
 	private float distanceX;
 	private float distanceY;
 	
 	/**
 	 * Default Constructor
 	 * 
 	 * @param game Escape Game
 	 */
 	public Level(Escape game, Stage stage, ScrollingTexture background, int intro, int next) {
 		
 		this.game = Objects.requireNonNull(game);
 		this.star = new RepeatableScrollingTexture(game.getResources().getTexture(TextureLoader.OVERLAY_STAR), true);
         this.events = new ArrayList<Input>();
         this.stage = Objects.requireNonNull(stage);
         this.background = Objects.requireNonNull(background);
         this.intro = intro;
         this.next = next;
         this.finish = new Runnable() {
 
 			@Override
 			public void run() {
 				if(getStage().hasFinished()) {
 					next();
 				}
 			}
         	
         };
 	}
 	
 	@Override
 	public void render(long delta) {
 		time += delta;
 		
 		float percent = ((float) time) / (getStage().getEstimatedScenarioTime() * 1000);
 
 		if(percent >= 1.0f) {
 			percent = 1.0f;
 		}
 		
 		getBackground().setYPercent(percent);
 		game.getGraphics().draw(getBackground(), 0, 0, game.getGraphics().getWidth(), game.getGraphics().getHeight());
 		
 		star.setYPercent(((float) time) / STAR_SPEED);
 		game.getGraphics().draw(star, 0, 0, game.getGraphics().getWidth(), game.getGraphics().getHeight());
 		
 		game.getUser().getShip().update(game.getGraphics(), delta);
 		if(accepted) {
 			if(stop) accepted = false;
 			if(velocity[0] <= 0) stop = true;
 			game.getUser().getShip().moveBy(velocity);
 		}
 		
 		getStage().update((int) (time / 1000));
 		
 		game.getEntityContainer().update(game.getGraphics(), delta);
 		game.getEntityContainer().flush();
 		
 		/**
 		 * Does the game is finished ?
 		 */
 		game.getEngine().post(finish);
 		
 	}
 
 	/**
 	 * Get the Game used for this Abstract Stage
 	 * 
 	 * @return Game linked to the {@link Level}
 	 */
 	protected Escape getGame() {
 		return game;
 	}
 	
 	/**
 	 * Triggered when we need to go on the next Screen 
 	 * in Game Lifecycle.
 	 */
 	protected void next() {
 		game.setScreenID(getNextScreen());
 	}
 	
 	@Override
 	public void show() {
 		
 		game.getOverlay().show();
 		game.getEntityContainer().reset();
 		
 		time = 0;
 		getStage().start();
 		
 		events.clear();
 		
 		float x = game.getEngine().getConverter().toMeterX(game.getGraphics().getWidth() / 2);
 		float y = game.getEngine().getConverter().toMeterY(game.getGraphics().getHeight() - 100);
 		
 		velocity[0] = 0.0f;
 		game.getUser().getShip().reset(x, y);
 
 	}
 
 	@Override
 	public void hide() {
 		
 		float x = game.getEngine().getConverter().toMeterX(game.getGraphics().getWidth() / 2);
 		float y = game.getEngine().getConverter().toMeterY(game.getGraphics().getHeight() - 100);
 		
 		game.getOverlay().hide();
 		game.getEntityContainer().reset();
 		
 		velocity[0] = 0.0f;
 		game.getUser().getShip().reset(x, y);
 		
 		getStage().reset();
 	}
 
 	@Override
 	public boolean touch(Input i) {
 		
 		Objects.requireNonNull(i);
 		boolean fire = (Math.abs(i.getXVelocity()) > 3000 || Math.abs(i.getYVelocity()) > 3000);
 		
 		Ship ship = game.getUser().getShip();
 		CoordinateConverter converter = game.getEngine().getConverter();
 		
 		float x = converter.toMeterX(i.getX());
 		float y = converter.toMeterY(i.getY());
 		
 		Action action = i.getAction();
 		this.events.add(i);
 		
 		if(fire) {
 			if(action.equals(Input.Action.ACTION_UP)) {
 				
 				WeaponGesture wg = new WeaponGesture(game.getEngine());
 				
 				float[] weaponVelocity = new float[3];
 				
 				if(wg.accept(events.get(0), events, i, weaponVelocity)) {
 					
 					Engine.debug(TAG, "Weapon Gesture Accept : Fire");
 					
 					if(!ship.isWeaponLoaded()) {
 						Engine.debug(TAG, "Weapon is not loaded");
 						ship.loadWeapon();
 					}
 					
 					ship.fireWeapon(weaponVelocity);
 					
 					
 				}
 				
 				events.clear();
 			}
 		} else {
 			if(action.equals(Input.Action.ACTION_DOWN)) {
 				
 				distanceX = x - ship.getX();
 	        	distanceY = y - ship.getY();
 	        	Arrays.fill(velocity, 0);
 	        	
 			} else if(action.equals(Input.Action.ACTION_UP)) {
 				
 				List<Gesture> gestures = game.getUser().getGestures();
 				
 				for(int j = 0; j < gestures.size(); j++) {
 					if(gestures.get(j).accept(events.get(0), events, i, velocity)) {
 						accepted = true;
 						stop = false;
 						break;
 					}
 				}
 				events.clear();
 			}
 			
 			ship.moveTo(x - distanceX, y - distanceY);
 		}
 		
 		return true;
 	}
 
 	@Override
 	public boolean move(final Input i) {
 		return touch(i);
 	}
 
 	/**
 	 * Get the Stage used for this {@link Level}
 	 * 
 	 * @return Stage
 	 */
 	Stage getStage() {
 		return stage;
 	}
 
 	/**
 	 * Get the Scrolling Texture used for this {@link Level} 
 	 * 
 	 * @return Background used for this Screen
 	 */
 	ScrollingTexture getBackground() {
 		return background;
 	}
 	
 	/**
 	 * Get the Intro Screen ID associated with this Level
 	 * 
 	 * @return Intro Screen ID
 	 */
 	public int getIntroScreen() {
 		return intro;
 	}
 	
 	/**
 	 * Get the Next Screen ID associated with this Level
 	 * 
 	 * @return Next Screen ID
 	 */
 	public int getNextScreen() {
 		return next;
 	}
 	
 }
