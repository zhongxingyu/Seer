 package com.secondhand.view.scene;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.anddev.andengine.engine.Engine;
 import org.anddev.andengine.entity.scene.menu.MenuScene;
 import org.anddev.andengine.entity.scene.menu.MenuScene.IOnMenuItemClickListener;
 import org.anddev.andengine.entity.scene.menu.item.IMenuItem;
 import org.anddev.andengine.entity.scene.menu.item.TextMenuItem;
 import org.anddev.andengine.entity.text.Text;
 import org.anddev.andengine.opengl.font.Font;
 import org.anddev.andengine.util.HorizontalAlign;
 
 import com.secondhand.view.resource.Fonts;
 import com.secondhand.view.resource.LocalizationStrings;
 import com.secondhand.view.scene.GameMenuScene.MenuItem;
 
 import android.content.Context;
 
 public class OptionsScene extends GameMenuScene {
 	private Text options;
 	private Text mirroredMovement;
 	public static final int MIRRORED_MOVEMENT_FALSE = 0;
 	public static final int MIRRORED_MOVEMENT_TRUE = 1;
 
 	public OptionsScene(Engine engine, Context context) {
 		super(engine, context);
 
 	}
 
 	@Override
 	public void loadScene() {
 		super.loadScene();
 
 		final Font mFont = Fonts.getInstance().menuItemFont;
 
 		options = new Text(100, 60, mFont, LocalizationStrings
 				.getInstance().getLocalizedString("menu_options"),
 				HorizontalAlign.CENTER);
 		options.setScale(1.5f);
 
 		mirroredMovement = new Text(100, 150, mFont, LocalizationStrings
 				.getInstance().getLocalizedString("mirrored_movement"),
 				HorizontalAlign.CENTER);
 
 		this.attachChild(options);
 		this.attachChild(mirroredMovement);
 		// make a centered menu.
 	}
 
 	public void setMirroredMovementColor(boolean isMirroredMovement){
 		final List<GameMenuScene.MenuItem> menuItems = new ArrayList<GameMenuScene.MenuItem>();
 
 		menuItems.add(new MenuItem(MIRRORED_MOVEMENT_FALSE, LocalizationStrings.getInstance().getLocalizedString("on")));
 		menuItems.add(new MenuItem(MIRRORED_MOVEMENT_TRUE, LocalizationStrings.getInstance().getLocalizedString("off")));
 
 		final Font menuItemFont = Fonts.getInstance().menuItemFont;
 		
		final IMenuItem on = new TextMenuItem(menuItems.get(0).id, menuItemFont,
				menuItems.get(0).text);
		final IMenuItem off = new TextMenuItem(menuItems.get(1).id, menuItemFont,
 				menuItems.get(1).text);

 		if(isMirroredMovement){
 			on.setColor(0.5f, 0.5f, 0.5f);
 		} else {
 			off.setColor(0.5f, 0.5f, 0.5f);
 		}
 
 		on.setPosition(100, 200);
 		addMenuItem(on);
 		
 		off.setPosition(200,200);
 		addMenuItem(off);
 
 	}
 
 	@Override
 	public AllScenes getParentScene() {
 		return AllScenes.MAIN_MENU_SCENE;
 	}
 
 }
