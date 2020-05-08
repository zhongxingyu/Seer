 package com.pix.mind.levels;
 
 import com.badlogic.gdx.graphics.Color;
 import com.pix.mind.PixMindGame;
 import com.pix.mind.actors.PlatformActivatorActor;
 import com.pix.mind.actors.StaticPlatformActor;
 import com.pix.mind.box2d.bodies.PlatformActivator;
 import com.pix.mind.box2d.bodies.StaticPlatform;
 import com.pix.mind.world.PixMindWorldRenderer;
 
 public class Level05 extends PixMindLevel {
 
 	
 	/* JUAN: LEVEL 3*/
 	public String levelTitle = "LevelThree";
 	PixMindGame game;
 	private static final int nActiveColors = 2;
 	public Level05(PixMindGame game) {
 		super(game, 820, 820, 600, 1.2f, 6.5f, nActiveColors);
 		this.game = game;
		levelNumber = 5;
 		// TODO Auto-generated constructor stub
 	}
 
 	@Override
 	public void show() {
 		// TODO Auto-generated method stub
 
 		super.show();
		super.setNextLevel(game.getLevel06());
 		super.setActiveLevel(this);
 		// CREANDO EL NIVEL
 
 		// platform Actors and Activator Actors List
 
 		// Creating All Static Platforms
 
 		// Box2D platforms
 		
 			// plataforma INIT
 			StaticPlatform sPInit = new StaticPlatform(box2D.getWorld(), 0.5f, 5.9f,
 					1.0f, 0.1f);
 			
 			// plataforma END
 			StaticPlatform sPEnd = new StaticPlatform(box2D.getWorld(), 6.4f, 5.9f,
 					1.0f, 0.1f);
 			
 			// plataforma 1.1 (plataforma de color 1, aparicion 1)
 			StaticPlatform sP11 = new StaticPlatform(box2D.getWorld(), 3.5f, 4.6f,
 					1.0f, 0.1f);
 			
 			// plataforma 2.1 (plataforma de color 2, aparicion 1)
 			StaticPlatform sP21 = new StaticPlatform(box2D.getWorld(), 3.5f, 3.4f,
 					3.0f, 0.1f);
 			
 			// plataforma 1.2 (plataforma de color 1, aparicion 2)
 			StaticPlatform sP12 = new StaticPlatform(box2D.getWorld(), 3.5f, 2.2f,
 					1.0f, 0.1f);
 			
 			// plataforma 1.2 (plataforma de color 1, aparicion 2)
 			StaticPlatform sP31 = new StaticPlatform(box2D.getWorld(), 3.5f, 1.0f,
 					4.0f, 0.15f);
 
 
 		// Actor Platforms
 				StaticPlatformActor sPInitSkin = new StaticPlatformActor(sPInit,
 						Color.BLACK, true);
 				StaticPlatformActor sPEndSkin = new StaticPlatformActor(sPEnd,
 						Color.BLACK, true);
 				StaticPlatformActor sP11Skin = new StaticPlatformActor(sP11,
 						Color.GREEN, false);
 				StaticPlatformActor sP21Skin = new StaticPlatformActor(sP21,
 						Color.RED, false);
 				StaticPlatformActor sP12Skin = new StaticPlatformActor(sP12,
 						Color.GREEN, false);
 				StaticPlatformActor sP31Skin = new StaticPlatformActor(sP31,
 						Color.BLUE, false);
 		
 		// Add platforms to Stage
 
 				scene2D.getGroupStage().addActor(sPInitSkin);
 				scene2D.getGroupStage().addActor(sPEndSkin);
 				scene2D.getGroupStage().addActor(sP11Skin);
 				scene2D.getGroupStage().addActor(sP21Skin);
 				scene2D.getGroupStage().addActor(sP12Skin);
 				scene2D.getGroupStage().addActor(sP31Skin);
 
 		// Add to platform list
 
 				box2D.getPlatformList().add(sPInitSkin);
 				box2D.getPlatformList().add(sPEndSkin);
 				box2D.getPlatformList().add(sP11Skin);
 				box2D.getPlatformList().add(sP21Skin);
 				box2D.getPlatformList().add(sP12Skin);
 				box2D.getPlatformList().add(sP31Skin);
 
 		// Creating All Activator
 
 		// Box2D Activator
 
 			// activador 3.1 (activador de plataforma 3, aparicion 1)
 			PlatformActivator pA31 = new PlatformActivator(box2D.getWorld(),
 						3.5f, 5.3f, 0.2f);
 			
 			// activador 2.1 (activador de plataforma 2, aparicion 1)
 			PlatformActivator pA21 = new PlatformActivator(box2D.getWorld(),
 						1.6f, 4.1f, 0.2f);
 			
 			// activador 1.1 (activador de plataforma 1, aparicion 1)
 			PlatformActivator pA11 = new PlatformActivator(box2D.getWorld(),
 						4.1f, 2.6f, 0.2f);
 			
 			// activador FIN DE FASE 3
 			PlatformActivator pAEnd = new PlatformActivator(box2D.getWorld(),
 					6.5f, 6.9f, 0.3f);
 
 
 		// Actor Activator
 				PlatformActivatorActor pA31Skin = new PlatformActivatorActor(pA31,
 						Color.BLUE, false);
 				PlatformActivatorActor pA21Skin = new PlatformActivatorActor(pA21,
 						Color.RED, false);
 				PlatformActivatorActor pA11Skin = new PlatformActivatorActor(pA11,
 						Color.GREEN, false);
 				PlatformActivatorActor pAEndSkin = new PlatformActivatorActor(pAEnd,
 						Color.BLACK, true);
 
 		// Add activators to Stage
 
 		scene2D.getGroupStage().addActor(pA31Skin);
 		scene2D.getGroupStage().addActor(pA21Skin);
 		scene2D.getGroupStage().addActor(pA11Skin);
 		scene2D.getGroupStage().addActor(pAEndSkin);
 
 		// Add to activator list
 
 		box2D.getActivatorList().add(pA31Skin);
 		box2D.getActivatorList().add(pA21Skin);
 		box2D.getActivatorList().add(pA11Skin);
 		box2D.getActivatorList().add(pAEndSkin);
 
 		// add to stage the group of actors
 
 		// Active colors
 
 		// Rendering the game
 //		box2D.addActivatedColor(Color.BLUE);
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
