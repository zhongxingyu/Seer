 package com.scgame.adozeneggs.core;
 
 import static playn.core.PlayN.*;
 
 import playn.core.Game;
 import playn.core.GroupLayer;
 import playn.core.Image;
 import playn.core.ImageLayer;
 import playn.core.Pointer;
 import playn.core.Pointer.Event;
 import playn.core.Pointer.Listener;
 
 public class adozeneggs implements Game {
 	GroupLayer layer;
 	Egg theEgg;
 	Basket aBasket;
 	Basket bBasket;
 	private Scene activeScene;
 	private Scene scMenu = new SceneMenu(this);
 	private Scene scLevels = new SceneLevels(this); 
 	private Scene scGameplay = new SceneGameplay(this); 
 	GroupLayer baseLayer;
 
 	@Override
 	public void init() {
 		
 		runSceneMenu();
 		/*
 		// create a group layer to hold everything
 		layer = graphics().createGroupLayer();
 		graphics().rootLayer().add(layer);
 		// create and add background image layer
 
 		Image bgImage = assetManager().getImage("images/bg.png");
 		graphics().setSize(graphics().height(), graphics().width()); 
 		ImageLayer bgLayer = graphics().createImageLayer(bgImage);
 		layer.add(bgLayer);
 		
 		aBasket = new Basket(new Vect2d((float)0.01,(float) 0.01), new Vect2d(200, 200));
 		bBasket = new Basket(new Vect2d(0, 0), new Vect2d(200, 200));
 		
 		theEgg= new Egg(layer, aBasket, bBasket);
 		pointer().setListener(new Listener() {
 			
 			@Override
 			public void onPointerStart(Event event) {
 				theEgg.jump();
 			}
 			
 			@Override
 			public void onPointerEnd(Event event) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void onPointerDrag(Event event) {
 				// TODO Auto-generated method stub
 				
 			}
 		});
 
 		 */
 	}
 
 
 	public void runSceneMenu() {
 		if (activeScene != null) {
 			activeScene.shutdown();
 		}
 		activeScene = scMenu;
 		activeScene.init();
 	}
 	
 	public void runSceneLevels() {
 		if (activeScene != null) {
 			activeScene.shutdown();
 		}
 		activeScene = scLevels;
 		activeScene.init();
 	}
 	
 	public void runSceneGameplay(int level) {
 		if (activeScene != null) {
 			activeScene.shutdown();
 		}
 		activeScene = scGameplay;
 		((SceneGameplay) scGameplay).init(level);
 	}
 	
 	@Override
 	public void paint(float alpha) {
//		theEgg.paint(alpha);
 	}
 
 	@Override
 	public void update(float delta) {
//		theEgg.update(delta);
//		aBasket.update(delta);
//		bBasket.update(delta);
 	}
 
 	@Override
 	public int updateRate() {
 		return 25;
 	}
 }
