 package com.pix.mind.levels;
 
 import com.badlogic.gdx.graphics.Color;
 import com.pix.mind.PixMindGame;
 import com.pix.mind.actors.PlatformActivatorActor;
 import com.pix.mind.actors.StaticPlatformActor;
 import com.pix.mind.actors.StaticWallActor;
 import com.pix.mind.box2d.bodies.PlatformActivator;
 import com.pix.mind.box2d.bodies.StaticPlatform;
 import com.pix.mind.world.PixMindWorldRenderer;
 
 
 public class Level24 extends PixMindLevel {
 	
 
 	/* JUAN: LEVEL 24*/
 	public String levelTitle = "LevelTwentyfour";
 	PixMindGame game;
 	private static final int nActiveColors = 2;
 	public Level24(PixMindGame game) {
		super(game, 2300, 1800, 650, 5.0f, 8.0f, nActiveColors);
 		this.game = game;
 		levelNumber = 24;
 		// TODO Auto-generated constructor stub
 	}
 
 	@Override
 	public void show() {
 		// TODO Auto-generated method stub
 
 		super.show();
 		super.setNextLevel(game.getLevel01());
 		super.setActiveLevel(this);
 		// CREANDO EL NIVEL
 
 		// platform Actors and Activator Actors List
 		
 		// Creating All Statics Walls
 		
 			scene2D.getGroupStage().addActor(new StaticWallActor(box2D.getWorld(), 9.5f, 8.0f, 0.08f, 3.0f));
 			
 			// static platform for the top of the wall (for allow to jump pixGuy when it collides)
 			StaticPlatform sPFixTopWall = new StaticPlatform(box2D.getWorld(), 9.5f, 11.0f, 
 					0.075f, 0.02f);
 
 		// Creating All Static Platforms
 
 		// Box2D platforms
 		
 			// plataforma INIT
 			StaticPlatform sPInit = new StaticPlatform(box2D.getWorld(), 5.0f, 7.0f,
 				1.0f, 0.11f);
 		
 			
 			//FIXED
 			
 				// plataforma fija 1
 				StaticPlatform sPFix1 = new StaticPlatform(box2D.getWorld(), 6.5f, 8.2f, 
 					0.4f, 0.1f);
 				
 				// plataforma fija 2
 				StaticPlatform sPFix2 = new StaticPlatform(box2D.getWorld(), 8.0f, 9.4f, 
 					0.4f, 0.1f);
 				
 				// plataforma fija 3
 				StaticPlatform sPFix3 = new StaticPlatform(box2D.getWorld(), 10.61f, 1.0f, 
 					0.8f, 0.1f);
 				
 				// plataforma fija 4
 				StaticPlatform sPFix4 = new StaticPlatform(box2D.getWorld(), 12.0f, 3.3f, 
 					1.0f, 0.1f);
 				
 				// plataforma fija 5
 				StaticPlatform sPFix5 = new StaticPlatform(box2D.getWorld(), 12.0f, 4.5f, 
 					0.4f, 0.1f);
 				
 				// plataforma fija 6
 				StaticPlatform sPFix6 = new StaticPlatform(box2D.getWorld(), 12.0f, 6.9f, 
 					1.0f, 0.1f);
 				
 				// plataforma fija 7
 				StaticPlatform sPFix7 = new StaticPlatform(box2D.getWorld(), 7.0f, 11.95f, 
 					0.2f, 0.1f);
 				
 				// plataforma fija 8
 				StaticPlatform sPFix8 = new StaticPlatform(box2D.getWorld(), 1.2f, 6.7f, 
 					0.6f, 0.1f);
 				
 				// plataforma fija 9
 				StaticPlatform sPFix9 = new StaticPlatform(box2D.getWorld(), 1.1f, 9.0f, 
 					0.6f, 0.1f);
 				
 				// plataforma fija 10
 				StaticPlatform sPFix10 = new StaticPlatform(box2D.getWorld(), 9.6f, 8.2f, 
 					0.07f, 0.07f);
 				
 				// plataforma fija 11
 				StaticPlatform sPFix11 = new StaticPlatform(box2D.getWorld(), 9.6f, 9.3f, 
 					0.07f, 0.07f);
 				
 				// plataforma fija 12
 				StaticPlatform sPFix12 = new StaticPlatform(box2D.getWorld(), 9.6f, 10.4f, 
 					0.07f, 0.07f);
 				
 			
 			
 			//WITH COLOR
 			
 				//COLOR 1
 			
 				// plataforma sP11 (color 1, aparicion 1)
 				StaticPlatform sP11 = new StaticPlatform(box2D.getWorld(), 12.0f, 2.2f, 
 						0.5f, 0.11f);
 				
 //				// plataforma sP12 (color 1, aparicion 2)
 //				StaticPlatform sP12 = new StaticPlatform(box2D.getWorld(), 12.0f, 5.7f, 
 //						0.5f, 0.11f);
 				
 //				// plataforma sP13 (color 1, aparicion 3)
 //				StaticPlatform sP13 = new StaticPlatform(box2D.getWorld(), 12.0f, 8.2f, 
 //						0.8f, 0.11f);
 				
 				// plataforma sP14 (color 1, aparicion 4)
 				StaticPlatform sP14 = new StaticPlatform(box2D.getWorld(), 3.0f, 8.0f, 
 						0.5f, 0.11f);
 				
 				// plataforma sP15 (color 1, aparicion 5)
 				StaticPlatform sP15 = new StaticPlatform(box2D.getWorld(), 6.0f, 12.9f, 
 						0.1f, 0.08f);
 				
 				// plataforma sP16 (color 1, aparicion 6)
 				StaticPlatform sP16 = new StaticPlatform(box2D.getWorld(), 5.4f, 13.9f, 
 						0.1f, 0.08f);
 				
 				// plataforma sP17 (color 1, aparicion 7)
 				StaticPlatform sP17 = new StaticPlatform(box2D.getWorld(), 4.7f, 14.8f, 
 						0.1f, 0.08f);
 				
 				
 				//COLOR 2
 				
 				// plataforma sP21 (color 2, aparicion 1)
 				StaticPlatform sP21 = new StaticPlatform(box2D.getWorld(), 7.0f, 10.7f, 
 						1.0f, 0.11f);
 				
 				// plataforma sP22 (color 2, aparicion 2)
 				StaticPlatform sP22 = new StaticPlatform(box2D.getWorld(), 1.5f, 12.2f, 
 						2.5f, 0.11f);
 				
 				
 				//COLOR 3
 				
 				// plataforma sP31 (color 3, aparicion 1)
 				StaticPlatform sP31 = new StaticPlatform(box2D.getWorld(), 1.6f, 10.7f, 
 						2.0f, 0.11f);
 				
 				// plataforma sP32 (color 3, aparicion 2)
 				StaticPlatform sP32 = new StaticPlatform(box2D.getWorld(), 12.0f, 8.2f, 
 						0.8f, 0.11f);
 				
 				// plataforma sP33 (color 3, aparicion 3)
 				StaticPlatform sP33 = new StaticPlatform(box2D.getWorld(), 12.0f, 5.7f, 
 						0.5f, 0.11f);
 		
 		
 
 		// Actor Platforms
 				
 				StaticPlatformActor sPFixTopWallSkin = new StaticPlatformActor(sPFixTopWall,
 						Color.BLACK, true);
 				
 				StaticPlatformActor sPInitSkin = new StaticPlatformActor(sPInit,
 						Color.BLACK, true);
 				
 				
 				StaticPlatformActor sPFix1Skin = new StaticPlatformActor(sPFix1,
 						Color.BLACK, true);
 				StaticPlatformActor sPFix2Skin = new StaticPlatformActor(sPFix2,
 						Color.BLACK, true);
 				StaticPlatformActor sPFix3Skin = new StaticPlatformActor(sPFix3,
 						Color.BLACK, true);
 				StaticPlatformActor sPFix4Skin = new StaticPlatformActor(sPFix4,
 						Color.BLACK, true);
 				StaticPlatformActor sPFix5Skin = new StaticPlatformActor(sPFix5,
 						Color.BLACK, true);
 				StaticPlatformActor sPFix6Skin = new StaticPlatformActor(sPFix6,
 						Color.BLACK, true);
 				StaticPlatformActor sPFix7Skin = new StaticPlatformActor(sPFix7,
 						Color.BLACK, true);
 				StaticPlatformActor sPFix8Skin = new StaticPlatformActor(sPFix8,
 						Color.BLACK, true);
 				StaticPlatformActor sPFix9Skin = new StaticPlatformActor(sPFix9,
 						Color.BLACK, true);
 				StaticPlatformActor sPFix10Skin = new StaticPlatformActor(sPFix10,
 						Color.BLACK, true);
 				StaticPlatformActor sPFix11Skin = new StaticPlatformActor(sPFix11,
 						Color.BLACK, true);
 				StaticPlatformActor sPFix12Skin = new StaticPlatformActor(sPFix12,
 						Color.BLACK, true);
 				
 				
 				StaticPlatformActor sP11Skin = new StaticPlatformActor(sP11,
 						Color.BLUE, false);
 //				StaticPlatformActor sP12Skin = new StaticPlatformActor(sP12,
 //						Color.BLUE, false);
 //				StaticPlatformActor sP13Skin = new StaticPlatformActor(sP13,
 //						Color.BLUE, false);
 				StaticPlatformActor sP14Skin = new StaticPlatformActor(sP14,
 						Color.BLUE, false);
 				StaticPlatformActor sP15Skin = new StaticPlatformActor(sP15,
 						Color.BLUE, false);
 				StaticPlatformActor sP16Skin = new StaticPlatformActor(sP16,
 						Color.BLUE, false);
 				StaticPlatformActor sP17Skin = new StaticPlatformActor(sP17,
 						Color.BLUE, false);
 				
 				
 				StaticPlatformActor sP21Skin = new StaticPlatformActor(sP21,
 						Color.RED, false);
 				StaticPlatformActor sP22Skin = new StaticPlatformActor(sP22,
 						Color.RED, false);
 				
 				
 				StaticPlatformActor sP31Skin = new StaticPlatformActor(sP31,
 						Color.ORANGE, false);
 				StaticPlatformActor sP32Skin = new StaticPlatformActor(sP32,
 						Color.ORANGE, false);
 				StaticPlatformActor sP33Skin = new StaticPlatformActor(sP33,
 						Color.ORANGE, false);
 				
 
 		// Add platforms to Stage
 				
 				scene2D.getGroupStage().addActor(sPFixTopWallSkin);
 				
 				scene2D.getGroupStage().addActor(sPInitSkin);
 
 				scene2D.getGroupStage().addActor(sPFix1Skin);
 				scene2D.getGroupStage().addActor(sPFix2Skin);
 				scene2D.getGroupStage().addActor(sPFix3Skin);
 				scene2D.getGroupStage().addActor(sPFix4Skin);
 				scene2D.getGroupStage().addActor(sPFix5Skin);
 				scene2D.getGroupStage().addActor(sPFix6Skin);
 				scene2D.getGroupStage().addActor(sPFix7Skin);
 				scene2D.getGroupStage().addActor(sPFix8Skin);
 				scene2D.getGroupStage().addActor(sPFix9Skin);
 				scene2D.getGroupStage().addActor(sPFix10Skin);
 				scene2D.getGroupStage().addActor(sPFix11Skin);
 				scene2D.getGroupStage().addActor(sPFix12Skin);
 
 				scene2D.getGroupStage().addActor(sP11Skin);
 //				scene2D.getGroupStage().addActor(sP12Skin);
 //				scene2D.getGroupStage().addActor(sP13Skin);
 				scene2D.getGroupStage().addActor(sP14Skin);
 				scene2D.getGroupStage().addActor(sP15Skin);
 				scene2D.getGroupStage().addActor(sP16Skin);
 				scene2D.getGroupStage().addActor(sP17Skin);
 				
 				scene2D.getGroupStage().addActor(sP21Skin);
 				scene2D.getGroupStage().addActor(sP22Skin);
 
 				scene2D.getGroupStage().addActor(sP31Skin);
 				scene2D.getGroupStage().addActor(sP32Skin);
 				scene2D.getGroupStage().addActor(sP33Skin);
 
 
 		// Add to platform list
 				
 				box2D.getPlatformList().add(sPFixTopWallSkin);
 				
 				box2D.getPlatformList().add(sPInitSkin);
 
 				box2D.getPlatformList().add(sPFix1Skin);
 				box2D.getPlatformList().add(sPFix2Skin);
 				box2D.getPlatformList().add(sPFix3Skin);
 				box2D.getPlatformList().add(sPFix4Skin);
 				box2D.getPlatformList().add(sPFix5Skin);
 				box2D.getPlatformList().add(sPFix6Skin);
 				box2D.getPlatformList().add(sPFix7Skin);
 				box2D.getPlatformList().add(sPFix8Skin);
 				box2D.getPlatformList().add(sPFix9Skin);
 				box2D.getPlatformList().add(sPFix10Skin);
 				box2D.getPlatformList().add(sPFix11Skin);
 				box2D.getPlatformList().add(sPFix12Skin);
 				
 				box2D.getPlatformList().add(sP11Skin);
 //				box2D.getPlatformList().add(sP12Skin);
 //				box2D.getPlatformList().add(sP13Skin);
 				box2D.getPlatformList().add(sP14Skin);
 				box2D.getPlatformList().add(sP15Skin);
 				box2D.getPlatformList().add(sP16Skin);
 				box2D.getPlatformList().add(sP17Skin);
 				
 				box2D.getPlatformList().add(sP21Skin);
 				box2D.getPlatformList().add(sP22Skin);
 				
 				box2D.getPlatformList().add(sP31Skin);
 				box2D.getPlatformList().add(sP32Skin);
 				box2D.getPlatformList().add(sP33Skin);
 
 
 		// Creating All Activator
 
 		// Box2D Activator
 				
 				// activador 1.X (activador de plataforma 1, apariciones de la 1 a la 7)
 				PlatformActivator pA11 = new PlatformActivator(box2D.getWorld(), 10.7f, 2.8f, 0.2f);
 				
 				PlatformActivator pA12 = new PlatformActivator(box2D.getWorld(), 7.0f, 11.7f, 0.11f);
 					PlatformActivator pA122 = new PlatformActivator(box2D.getWorld(), 6.55f, 12.05f, 0.11f);
 					PlatformActivator pA123 = new PlatformActivator(box2D.getWorld(), 7.45f, 12.0f, 0.11f);
 				
 //				PlatformActivator pA13 = new PlatformActivator(box2D.getWorld(),  5.35f, 13.6f, 0.11f);
 					PlatformActivator pA132 = new PlatformActivator(box2D.getWorld(), 5.7f, 13.75f, 0.10f);
 //					PlatformActivator pA133 = new PlatformActivator(box2D.getWorld(), 5.05f, 13.75f, 0.10f);
 					PlatformActivator pA134 = new PlatformActivator(box2D.getWorld(),  5.4f, 14.2f, 0.10f);
 				
 				
 				PlatformActivator pA14 = new PlatformActivator(box2D.getWorld(), 6.0f, 13.1f, 0.10f);
 				
 //				PlatformActivator pA15 = new PlatformActivator(box2D.getWorld(), 4.65f, 14.45f, 0.10f);
 					PlatformActivator pA152 = new PlatformActivator(box2D.getWorld(), 5.0f, 14.6f, 0.10f);
 //					PlatformActivator pA153 = new PlatformActivator(box2D.getWorld(), 4.3f, 14.6f, 0.10f);
 					PlatformActivator pA154 = new PlatformActivator(box2D.getWorld(), 4.7f, 15.2f, 0.10f);
 				
 				
 				// activador 2.X (activador de plataforma 2, apariciones de la 1 a la 3)
 				PlatformActivator pA21 = new PlatformActivator(box2D.getWorld(), 0.8f, 9.9f, 0.2f);
 				PlatformActivator pA22 = new PlatformActivator(box2D.getWorld(), 3.7f, 14.4f, 0.15f);
 				PlatformActivator pA23 = new PlatformActivator(box2D.getWorld(), 2.7f, 14.4f, 0.15f);
 				
 				
 				// activador 3.X (activador de plataforma 3, apariciones 1 y 2)
 				PlatformActivator pA31 = new PlatformActivator(box2D.getWorld(), 1.0f, 7.9f, 0.2f);
 				PlatformActivator pA32 = new PlatformActivator(box2D.getWorld(), 13.2f, 4.2f, 0.2f);
 				
 				// activador 4.X (activador de plataforma 4, apariciones 1 y 2)
 				PlatformActivator pA41 = new PlatformActivator(box2D.getWorld(), 12.0f, 7.6f, 0.2f);
 				PlatformActivator pA42 = new PlatformActivator(box2D.getWorld(), 12.2f, 5.3f, 0.2f);
 
 				
 				// activador FIN DE FASE 24
 				PlatformActivator pAEnd = new PlatformActivator(box2D.getWorld(), 0.0f, 11.5f, 0.35f);
 
 
 		// Actor Activator
 				
 				PlatformActivatorActor pA11Skin = new PlatformActivatorActor(pA11,
 						Color.BLUE, false);
 				
 				PlatformActivatorActor pA12Skin = new PlatformActivatorActor(pA12,
 						Color.BLUE, false);
 					PlatformActivatorActor pA122Skin = new PlatformActivatorActor(pA122,
 							Color.BLUE, false);
 					PlatformActivatorActor pA123Skin = new PlatformActivatorActor(pA123,
 							Color.BLUE, false);
 					
 //				PlatformActivatorActor pA13Skin = new PlatformActivatorActor(pA13,
 //						Color.BLUE, false);
 					PlatformActivatorActor pA132Skin = new PlatformActivatorActor(pA132,
 							Color.BLUE, false);
 //					PlatformActivatorActor pA133Skin = new PlatformActivatorActor(pA133,
 //							Color.BLUE, false);
 					PlatformActivatorActor pA134Skin = new PlatformActivatorActor(pA134,
 							Color.BLUE, false);
 				
 				PlatformActivatorActor pA14Skin = new PlatformActivatorActor(pA14,
 						Color.BLUE, false);
 				
 //				PlatformActivatorActor pA15Skin = new PlatformActivatorActor(pA15,
 //						Color.BLUE, false);
 					PlatformActivatorActor pA152Skin = new PlatformActivatorActor(pA152,
 							Color.BLUE, false);
 //					PlatformActivatorActor pA153Skin = new PlatformActivatorActor(pA153,
 //							Color.BLUE, false);
 					PlatformActivatorActor pA154Skin = new PlatformActivatorActor(pA154,
 							Color.BLUE, false);
 				
 				
 				PlatformActivatorActor pA21Skin = new PlatformActivatorActor(pA21,
 						Color.RED, false);
 				PlatformActivatorActor pA22Skin = new PlatformActivatorActor(pA22,
 						Color.RED, false);
 				PlatformActivatorActor pA23Skin = new PlatformActivatorActor(pA23,
 						Color.RED, false);
 				
 				
 				PlatformActivatorActor pA31Skin = new PlatformActivatorActor(pA31,
 						Color.ORANGE, false);
 				PlatformActivatorActor pA32Skin = new PlatformActivatorActor(pA32,
 						Color.ORANGE, false);
 				
 				PlatformActivatorActor pA41Skin = new PlatformActivatorActor(pA41,
 						Color.GREEN, false);
 				PlatformActivatorActor pA42Skin = new PlatformActivatorActor(pA42,
 						Color.GREEN, false);
 				
 				
 				PlatformActivatorActor pAEndSkin = new PlatformActivatorActor(pAEnd,
 						Color.BLACK, true);
 
 
 		// Add activators to Stage
 				
 				scene2D.getGroupStage().addActor(pA11Skin);
 				scene2D.getGroupStage().addActor(pA12Skin);
 					scene2D.getGroupStage().addActor(pA122Skin);
 					scene2D.getGroupStage().addActor(pA123Skin);
 //				scene2D.getGroupStage().addActor(pA13Skin);
 					scene2D.getGroupStage().addActor(pA132Skin);
 //					scene2D.getGroupStage().addActor(pA133Skin);
 					scene2D.getGroupStage().addActor(pA134Skin);
 				scene2D.getGroupStage().addActor(pA14Skin);
 //				scene2D.getGroupStage().addActor(pA15Skin);
 					scene2D.getGroupStage().addActor(pA152Skin);
 //					scene2D.getGroupStage().addActor(pA153Skin);
 					scene2D.getGroupStage().addActor(pA154Skin);
 				
 				
 				scene2D.getGroupStage().addActor(pA21Skin);
 				scene2D.getGroupStage().addActor(pA22Skin);
 				scene2D.getGroupStage().addActor(pA23Skin);
 				
 				
 				scene2D.getGroupStage().addActor(pA31Skin);
 				scene2D.getGroupStage().addActor(pA32Skin);
 				
 				
 				scene2D.getGroupStage().addActor(pA41Skin);
 				scene2D.getGroupStage().addActor(pA42Skin);
 				
 				
 				scene2D.getGroupStage().addActor(pAEndSkin);
 
 
 		// Add to activator list
 				
 				box2D.getActivatorList().add(pA11Skin);
 				box2D.getActivatorList().add(pA12Skin);
 					box2D.getActivatorList().add(pA122Skin);
 					box2D.getActivatorList().add(pA123Skin);
 //				box2D.getActivatorList().add(pA13Skin);
 					box2D.getActivatorList().add(pA132Skin);
 //					box2D.getActivatorList().add(pA133Skin);
 					box2D.getActivatorList().add(pA134Skin);
 				box2D.getActivatorList().add(pA14Skin);
 //				box2D.getActivatorList().add(pA15Skin);
 					box2D.getActivatorList().add(pA152Skin);
 //					box2D.getActivatorList().add(pA153Skin);
 					box2D.getActivatorList().add(pA154Skin);
 				
 				
 				box2D.getActivatorList().add(pA21Skin);
 				box2D.getActivatorList().add(pA22Skin);
 				box2D.getActivatorList().add(pA23Skin);
 				
 				
 				box2D.getActivatorList().add(pA31Skin);
 				box2D.getActivatorList().add(pA32Skin);
 				
 				
 				box2D.getActivatorList().add(pA41Skin);
 				box2D.getActivatorList().add(pA42Skin);
 				
 				
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
