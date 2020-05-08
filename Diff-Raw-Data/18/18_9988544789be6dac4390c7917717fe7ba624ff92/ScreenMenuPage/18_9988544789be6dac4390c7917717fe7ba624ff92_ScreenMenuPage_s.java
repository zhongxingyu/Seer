 package com.deadpixels.lib.screens.menu;
 
 import com.badlogic.gdx.assets.AssetManager;
 import com.deadpixels.lib.menu.PageAdapter;
 import com.deadpixels.lib.screens.Screen;
 import com.deadpixels.lib.screens.ScreenManager;
 
 /** Helper class to have an easy access to screen, screen manager and assetManager */
 public class ScreenMenuPage extends PageAdapter
 {
 	public ScreenMenuPage()
 	{
 		super();
 	}
 	
 	public AssetManager getAssetManager()
 	{
		return getScreenManager().getAssetManager();
 	}
 	
 	public ScreenManager getScreenManager()
	{
		return getScreen().getManager();
 	}
 	
 	public Screen getScreen()
 	{
		return ((ScreenMenu)menu).getScreen();
 	}
 }
