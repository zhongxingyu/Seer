 package com.game.rania.controller;
 
 import java.util.Vector;
 
 import com.badlogic.gdx.InputMultiplexer;
 import com.badlogic.gdx.Gdx;
 import com.game.rania.RaniaGame;
 import com.game.rania.model.element.DynamicObject;
 import com.game.rania.model.element.Group;
 import com.game.rania.model.element.HUDObject;
 import com.game.rania.model.element.Object;
 import com.game.rania.model.element.StaticObject;
 
 public class MainController extends InputMultiplexer{
 
 	private Vector<UpdateController> updateControllers = new Vector<UpdateController>();
 	private Vector<DynamicObject> 	 dynamicObjects    = new Vector<DynamicObject>();
 	private Vector<StaticObject>  	 staticObjects     = new Vector<StaticObject>();
 	private Vector<HUDObject> 	  	 HUDObjects  	   = new Vector<HUDObject>();
 	private DynamicObject 			 mPlayer 		   = null;
 
 	public MainController(){
 		super();
 		Gdx.input.setInputProcessor(this);
 	}
 	
 	//update controllers
 	public void addProcessor(UpdateController controller){
 		if (!updateControllers.contains(controller))
 		{
 			updateControllers.add(controller);
 			super.addProcessor(controller);
 		}
 	}
 	
 	public void removeProcessor(UpdateController controller){
 		if (!updateControllers.contains(controller))
 		{
 			updateControllers.remove(controller);
 			super.removeProcessor(controller);
 		}
 	}
 	
 	//player
 	public DynamicObject getPlayer(){
 		return mPlayer;
 	}
 	
 	public void setPlayer(DynamicObject player)
 	{
 		mPlayer = player;
 	}
 	
 	//dynamic objects
 	public Vector<DynamicObject> getDynamicObjects(){
 		return dynamicObjects;
 	}
 	
 	public void addDynamicObject(DynamicObject object){
 		if (!dynamicObjects.contains(object))
 			dynamicObjects.add(object);
 	}
 
 	//static objects
 	public Vector<StaticObject> getStaticObjects(){
 		return staticObjects;
 	}
 	
 	public void addStaticObject(StaticObject object){
 		if (!staticObjects.contains(object))
 			staticObjects.add(object);
 	}
 	
 	//HUD objects
 	public Vector<HUDObject> getHUDObjects(){
 		return HUDObjects;
 	}
 	
 	public void addHUDObject(HUDObject object){
 		if (!HUDObjects.contains(object))
 			HUDObjects.add(object);
 	}
 	
 	//all object
 	public void addObject(Object object){
 		if (object.asDynamicObject() != null) {
 			addDynamicObject(object.asDynamicObject());
 			return;
 		}
 		
 		if (object.asStaticObject() != null) {
 			addStaticObject(object.asStaticObject());
 			return;
 		}
 		
 		if (object.asHUDObject() != null)
 		{
 			addHUDObject(object.asHUDObject());
 			return;
 		}
 	}
 	
 	public void addObject(Group group){
 		for(Object element : group.getElements()){
 			addObject(element);
 		}
 	}
 	
 	public void update(float deltaTime){
 		if (mPlayer != null)
 		{
 			RaniaGame.mUser.x = (int)mPlayer.position.x;
 			RaniaGame.mUser.y = (int)mPlayer.position.y;
 		}
 
 		for (UpdateController controller : updateControllers) {
 			controller.update(deltaTime);
 		}
 	}
 
 	public void clear() {
		//RaniaGame.mGame.setScreen(null);
 		super.clear();
 		updateControllers.clear();
 		dynamicObjects.clear();
 		staticObjects.clear();
 		HUDObjects.clear();
 		mPlayer = null;
 	}
 }
