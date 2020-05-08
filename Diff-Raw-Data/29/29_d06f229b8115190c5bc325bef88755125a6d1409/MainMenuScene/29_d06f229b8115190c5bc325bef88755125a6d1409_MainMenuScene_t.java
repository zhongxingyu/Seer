 package com.secondhand.scene;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.anddev.andengine.engine.camera.Camera;
 import org.anddev.andengine.entity.scene.background.ColorBackground;
 import org.anddev.andengine.entity.scene.menu.MenuScene;
 import org.anddev.andengine.entity.scene.menu.item.IMenuItem;

 import org.anddev.andengine.entity.scene.menu.MenuScene.IOnMenuItemClickListener;
 
 import com.secondhand.debug.MyDebug;
import com.secondhand.twirl.GlobalResources;
 
 public class MainMenuScene extends GameMenuScene implements IOnMenuItemClickListener {
 
 	final int MENU_START = 0;
 	final int MENU_HIGH_SCORE = 1;
 	final int MENU_OPTIONS = 2;
 	
 	public MainMenuScene(Camera camera) {
 		super(camera);
 	}
 
 	@Override
 	public void loadResources() {
 	}
 
 	@Override
 	public void loadScene() {
 		
 		this.setBackground(new ColorBackground(0, 0, 0));
 		
 		// make a centered menu.
 		List<GameMenuScene.MenuItem> menuItems = new ArrayList<GameMenuScene.MenuItem>();
 		menuItems.add(new MenuItem(MENU_START, "start"));
 		menuItems.add(new MenuItem(MENU_OPTIONS, "options"));
 		
		layoutCenteredMenu(menuItems, GlobalResources.getInstance().menuFont, 20);
 		this.setOnMenuItemClickListener(this);
 	}
 
 	@Override
 	public boolean onMenuItemClicked(final MenuScene pMenuScene, final IMenuItem pMenuItem,
 			float pMenuItemLocalX, float pMenuItemLocalY) {
 		
 		switch(pMenuItem.getID()) {
 		case MENU_START:
 			MyDebug.i("now the game should start");
 			return true;
 		case MENU_OPTIONS:
 			MyDebug.i("now a settings menu should appear");
 			return true;
 		default:
 			return false;
 		}
 
 	}
 
 }
