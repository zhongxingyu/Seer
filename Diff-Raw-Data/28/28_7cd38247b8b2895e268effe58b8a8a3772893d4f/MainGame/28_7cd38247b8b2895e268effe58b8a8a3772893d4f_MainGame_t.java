 package com.vgdc.merge;
 
 import com.badlogic.gdx.Game;
 import com.vgdc.merge.assets.Assets;
 import com.vgdc.merge.assets.AssetsHandler;
 import com.vgdc.merge.screens.LoadScreen;
 
 public class MainGame extends Game {
 	
 	private AssetsHandler handler;
 	@Override
 	public void create() {
 		handler  = new AssetsHandler();
 		//assets = new Assets();
 		//assets.loadAssets("data/test/another_test.json");
 		setScreen(new LoadScreen(this));
 	}
 	
 	public AssetsHandler getHandler()
 	{
 		return handler;
 	}
 }
