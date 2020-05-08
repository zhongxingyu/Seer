 package com.pix.mind.levels;
 
 import com.badlogic.gdx.graphics.Color;
 import com.pix.mind.PixMindGame;
 import com.pix.mind.actors.PlatformActivatorActor;
 import com.pix.mind.actors.StaticPlatformActor;
 import com.pix.mind.box2d.bodies.PlatformActivator;
 import com.pix.mind.box2d.bodies.StaticPlatform;
 import com.pix.mind.world.PixMindWorldRenderer;
 
 public class Level14 extends PixMindLevel {
 
 	
 
 	public String levelTitle = "LevelNineteen";
 	PixMindGame game;
 	private static final int nActiveColors = 2;
 	public Level14(PixMindGame game) {
		super(game, 2266, 1700, 1210, 8.0f, 14.0f, nActiveColors);
 		this.game = game;
 		levelNumber = 14;
 		// TODO Auto-generated constructor stub
 	}
 
 	@Override
 	public void show() {
 		// TODO Auto-generated method stub
 
 		super.show();
 		super.setNextLevel(game.getLevel15());
 		super.setActiveLevel(this);
 		
 		// CREANDO EL NIVEL
 
 				// platform Actors and Activator Actors List
 
 				// Creating All Static Platforms
 
 					// Box2D platforms
 				
 						//FIXED
 						
 							// plataforma fija 1
 							StaticPlatform sPFix1 = new StaticPlatform(box2D.getWorld(), 8.0f, 12.0f, 
 								2.0f, 0.1f);
 							
 							// plataforma fija 2
 							StaticPlatform sPFix2 = new StaticPlatform(box2D.getWorld(), 8.0f, 8.5f, 
 								1.6f, 0.1f);
 							
 							// plataforma fija 3
 							StaticPlatform sPFix3 = new StaticPlatform(box2D.getWorld(), 8.0f, 7.3f, 
 								1.0f, 0.1f);
 							
 							
 						//COLOR 1
 							
 							// plataforma sP11 (color 1, aparicion 1)
 							StaticPlatform sP11 = new StaticPlatform(box2D.getWorld(), 5.0f, 7.3f, 
 									1.0f, 0.11f);
 							
 							// plataforma sP12 (color 1, aparicion 2)
 							StaticPlatform sP12 = new StaticPlatform(box2D.getWorld(), 7.5f, 5.5f, 
 									0.8f, 0.11f);
 							
 						//COLOR 2
 							
 							// plataforma sP21 (color 2, aparicion 1)
 							StaticPlatform sP21 = new StaticPlatform(box2D.getWorld(), 5.6f, 4.3f, 
 									0.8f, 0.11f);
 							
 							// plataforma sP22 (color 2, aparicion 2)
 							StaticPlatform sP22 = new StaticPlatform(box2D.getWorld(), 7.5f, 5.3f, 
 									0.8f, 0.11f);
 							
 							// plataforma sP23 (color 2, aparicion 3)
 							StaticPlatform sP23 = new StaticPlatform(box2D.getWorld(), 9.5f, 6.3f, 
 									0.8f, 0.11f);
 							
 						//COLOR 3
 							
 							// plataforma sP31 (color 3, aparicion 1)
 							StaticPlatform sP31 = new StaticPlatform(box2D.getWorld(), 8.0f, 9.6f, 
 									0.8f, 0.11f);
 							
 							// plataforma sP32 (color 3, aparicion 2)
 							StaticPlatform sP32 = new StaticPlatform(box2D.getWorld(), 8.0f, 10.8f, 
 									0.8f, 0.11f);
 							
 						//COLOR 4
 							
 							// plataforma sP41 (color 4, aparicion 1)
 							StaticPlatform sP41 = new StaticPlatform(box2D.getWorld(), 8.0f, 13.2f, 
 									0.7f, 0.11f);
 							
 						
 
 						
 					// Actor Platforms
 						
 						StaticPlatformActor sPFix1Skin = new StaticPlatformActor(sPFix1,
 								Color.BLACK, true);
 						StaticPlatformActor sPFix2Skin = new StaticPlatformActor(sPFix2,
 								Color.BLACK, true);
 						StaticPlatformActor sPFix3Skin = new StaticPlatformActor(sPFix3,
 								Color.BLACK, true);
 						
 						
 						StaticPlatformActor sP11Skin = new StaticPlatformActor(sP11,
 								Color.BLUE, false);
 						StaticPlatformActor sP12Skin = new StaticPlatformActor(sP12,
 								Color.BLUE, false);
 						
 						
 						StaticPlatformActor sP21Skin = new StaticPlatformActor(sP21,
 								Color.ORANGE, false);
 						StaticPlatformActor sP22Skin = new StaticPlatformActor(sP22,
 								Color.ORANGE, false);
 						StaticPlatformActor sP23Skin = new StaticPlatformActor(sP23,
 								Color.ORANGE, false);
 						
 						
 						StaticPlatformActor sP31Skin = new StaticPlatformActor(sP31,
 								Color.RED, false);
 						StaticPlatformActor sP32Skin = new StaticPlatformActor(sP32,
 								Color.RED, false);
 						
 						
 						StaticPlatformActor sP41Skin = new StaticPlatformActor(sP41,
 								Color.GREEN, false);
 
 						
 			
 					// Add platforms to Stage
 						
 						scene2D.getGroupStage().addActor(sPFix1Skin);
 						scene2D.getGroupStage().addActor(sPFix2Skin);
 						scene2D.getGroupStage().addActor(sPFix3Skin);
 				
 						
 						scene2D.getGroupStage().addActor(sP11Skin);
 						scene2D.getGroupStage().addActor(sP12Skin);
 						
 						
 						scene2D.getGroupStage().addActor(sP21Skin);
 						scene2D.getGroupStage().addActor(sP22Skin);
 						scene2D.getGroupStage().addActor(sP23Skin);
 
 						
 						scene2D.getGroupStage().addActor(sP31Skin);
 						scene2D.getGroupStage().addActor(sP32Skin);
 						
 						
 						scene2D.getGroupStage().addActor(sP41Skin);
 			
 			
 			
 					// Add to platform list
 						
 						box2D.getPlatformList().add(sPFix1Skin);
 						box2D.getPlatformList().add(sPFix2Skin);
 						box2D.getPlatformList().add(sPFix3Skin);
 						
 						
 						box2D.getPlatformList().add(sP11Skin);
 						box2D.getPlatformList().add(sP12Skin);
 					
 						
 						box2D.getPlatformList().add(sP21Skin);
 						box2D.getPlatformList().add(sP22Skin);
 						box2D.getPlatformList().add(sP23Skin);
 						
 						
 						box2D.getPlatformList().add(sP31Skin);
 						box2D.getPlatformList().add(sP32Skin);
 						
 						
 						box2D.getPlatformList().add(sP41Skin);
 						
 						
 						
 				// Creating All Activator
 
 					// Box2D Activator
 						
 						// activador 1.X (activador de plataforma 1, apariciones de la 1 a la 7)
 //						PlatformActivator pA11 = new PlatformActivator(box2D.getWorld(),  7.5f, 13.3f, 0.2f);
 //						
 //						PlatformActivator pA12 = new PlatformActivator(box2D.getWorld(),  7.8f, 11.3f, 0.2f);
 						
 						PlatformActivator pA13 = new PlatformActivator(box2D.getWorld(),  6.5f, 7.0f, 0.2f);
 						
 						
 						// activador 2.X (activador de plataforma 2, apariciones de la 1 a la 3)
 						PlatformActivator pA21 = new PlatformActivator(box2D.getWorld(), 4.7f, 6.0f, 0.2f);
 						
 						
 						// activador 3.X (activador de plataforma 3, apariciones 1 y 2)
 						PlatformActivator pA31 = new PlatformActivator(box2D.getWorld(), 4.0f, 9.0f, 0.2f);
 						
 						
 						// activador 4.X (activador de plataforma 4, apariciones 1 y 2)
 						PlatformActivator pA41 = new PlatformActivator(box2D.getWorld(), 8.0f, 11.6f, 0.2f);
 						
 						// activador FIN DE FASE 23
 						PlatformActivator pAEnd = new PlatformActivator(box2D.getWorld(), 8.0f, 15.2f, 0.4f);
 			
 						
 			
 					// Actor Activator
 						
 //						PlatformActivatorActor pA11Skin = new PlatformActivatorActor(pA11,
 //								Color.BLUE, false);
 //						PlatformActivatorActor pA12Skin = new PlatformActivatorActor(pA12,
 //								Color.BLUE, false);
 						PlatformActivatorActor pA13Skin = new PlatformActivatorActor(pA13,
 								Color.BLUE, false);
 						
 						PlatformActivatorActor pA21Skin = new PlatformActivatorActor(pA21,
 								Color.ORANGE, false);
 						
 						PlatformActivatorActor pA31Skin = new PlatformActivatorActor(pA31,
 								Color.RED, false);
 						
 						PlatformActivatorActor pA41Skin = new PlatformActivatorActor(pA41,
 								Color.GREEN, false);
 						
 						PlatformActivatorActor pAEndSkin = new PlatformActivatorActor(pAEnd,
 								Color.BLACK, true);
 			
 			
 						
 					// Add activators to Stage
 						
 //						scene2D.getGroupStage().addActor(pA11Skin);
 //						scene2D.getGroupStage().addActor(pA12Skin);
 						scene2D.getGroupStage().addActor(pA13Skin);
 						
 						scene2D.getGroupStage().addActor(pA21Skin);
 						
 						scene2D.getGroupStage().addActor(pA31Skin);
 						
 						scene2D.getGroupStage().addActor(pA41Skin);
 						
 						scene2D.getGroupStage().addActor(pAEndSkin);
 			
 						
 			
 					// Add to activator list
 						
 //						box2D.getActivatorList().add(pA11Skin);
 //						box2D.getActivatorList().add(pA12Skin);
 						box2D.getActivatorList().add(pA13Skin);
 						
 						box2D.getActivatorList().add(pA21Skin);
 						
 						box2D.getActivatorList().add(pA31Skin);
 						
 						box2D.getActivatorList().add(pA41Skin);
 						
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
