 package se.chalmers.segway.scenes;
 
 import java.io.File;
 import java.util.ArrayList;
 
 import org.andengine.engine.camera.Camera;
 import org.andengine.entity.scene.menu.MenuScene;
 import org.andengine.entity.scene.menu.MenuScene.IOnMenuItemClickListener;
 import org.andengine.entity.scene.menu.item.IMenuItem;
 import org.andengine.entity.scene.menu.item.SpriteMenuItem;
 import org.andengine.entity.scene.menu.item.decorator.ScaleMenuItemDecorator;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.opengl.util.GLState;
 
 import se.chalmers.segway.game.PlayerData;
 import se.chalmers.segway.scenes.SceneManager.SceneType;
 
 public class LevelSelectionScene extends BaseScene implements
 		IOnMenuItemClickListener {
 
 	private MenuScene selectionChildScene;
 	private final int BACK = 0;
 	private final int LEVEL_1 = 1;
 	private final int nmbrOfLevels = 4;//new File("assets/level/").list().length;
 	private int unlockedLevels = 1;
 	public void createScene(PlayerData playerData) {
 		createBackground();
 		createSelectionChildScene();
 	}
 
 	private void createBackground() {
 		attachChild(new Sprite(400, 240,
 				resourcesManager.selection_background_region, vbom) {
 			@Override
 			protected void preDraw(GLState pGLState, Camera pCamera) {
 				super.preDraw(pGLState, pCamera);
 				pGLState.enableDither();
 			}
 		});
 	}
 
 	@Override
 	public void onBackKeyPressed() {
 		SceneManager.getInstance().loadMenuScene(engine);
 	}
 
 	@Override
 	public SceneType getSceneType() {
 		return SceneType.SCENE_SELECTION;
 	}
 
 	@Override
 	public void disposeScene() {
 
 	}
 
 	private void createSelectionChildScene() {
 		selectionChildScene = new MenuScene(camera);
 		selectionChildScene.setPosition(0, 0);
 		final IMenuItem[] button = new IMenuItem[nmbrOfLevels];
 		
 		for (int i = 1; i <= nmbrOfLevels; i++) {
			System.out.println("Unlockedslevels" + unlockedLevels);
 			if(i <= unlockedLevels +1 ){
 				button[i-1] = new ScaleMenuItemDecorator(new SpriteMenuItem(i,
 					resourcesManager.level_button, vbom), 1.2f, 1);
 				button[i-1].setPosition(i*100, 300);
 				selectionChildScene.addMenuItem(button[i-1]);
 			}
 		}
 
 		selectionChildScene.setBackgroundEnabled(false);
 
 		selectionChildScene.setOnMenuItemClickListener(this);
 
 		setChildScene(selectionChildScene);
 	}
 
 	@Override
 	public boolean onMenuItemClicked(MenuScene pMenuScene, IMenuItem pMenuItem,
 			float pMenuItemLocalX, float pMenuItemLocalY) {
 		SceneManager.getInstance().loadGameScene(engine, pMenuItem.getID());
 		return true;
 	}
	public void setUnlockedLevels(int nbr){
		System.out.println("setting unlockedlevels " + nbr);
		unlockedLevels = nbr;
	}
 
 	@Override
 	public void createScene() {
		createBackground();
 	}
	public void updateScene(){
		createSelectionChildScene();
 	}
}
