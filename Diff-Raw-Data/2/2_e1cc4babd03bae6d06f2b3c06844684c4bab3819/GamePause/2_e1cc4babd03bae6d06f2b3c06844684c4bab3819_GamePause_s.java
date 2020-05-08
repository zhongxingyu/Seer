 package cz.sparko.Bugmaze;
 
 import cz.sparko.Bugmaze.Manager.GameManager;
 import cz.sparko.Bugmaze.Resource.GamefieldTextureResource;
 import cz.sparko.Bugmaze.Resource.ResourceHandler;
 import cz.sparko.Bugmaze.Resource.TextureResource;
 import org.andengine.engine.camera.Camera;
 import org.andengine.entity.scene.Scene;
 import org.andengine.entity.scene.menu.MenuScene;
 import org.andengine.entity.scene.menu.item.IMenuItem;
 import org.andengine.entity.scene.menu.item.TextMenuItem;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.input.touch.TouchEvent;
 import org.andengine.opengl.vbo.VertexBufferObjectManager;
 
 import java.util.ArrayList;
 
 public class GamePause extends MenuScene implements MenuScene.IOnMenuItemClickListener {
     private int positionX;
     private int positionY;
 
     ArrayList<TextMenuItem> menuItems = new ArrayList<TextMenuItem>(3);
 
     public GamePause(Camera camera, final Scene gameScene, VertexBufferObjectManager vertexBufferObjectManager) {
         super(camera);
         TextureResource textureResource = ResourceHandler.getInstance().getTextureResource(ResourceHandler.GAMEFIELD);
         positionX = (int)(camera.getWidth() / 2 - textureResource.getResource(GamefieldTextureResource.PAUSE_BACKGROUND).getWidth() / 2);
         positionY = (int)(camera.getHeight() / 2 - textureResource.getResource(GamefieldTextureResource.PAUSE_BACKGROUND).getHeight() / 2);
 
         final Sprite pausedSprite = new Sprite(positionX, positionY, textureResource.getResource(GamefieldTextureResource.PAUSE_BACKGROUND), vertexBufferObjectManager);
         this.attachChild(pausedSprite);
         this.setBackgroundEnabled(false);
 
         menuItems.add(new TextMenuItem(1, ResourceHandler.getInstance().getFontIndieFlower36(), "Back to game", vertexBufferObjectManager));
         menuItems.add(new TextMenuItem(2, ResourceHandler.getInstance().getFontIndieFlower36(), "Exit to menu", vertexBufferObjectManager));
 
         int posX = positionX;
         int posY = positionY;
         for (TextMenuItem menuItem : menuItems) {
             menuItem.setPosition(posX, posY);
             posY += 50;
             this.addMenuItem(menuItem);
         }
         this.setOnMenuItemClickListener(this);
 
         Sprite pauseBtn = new Sprite(700, 0, textureResource.getResource(GamefieldTextureResource.PAUSE_BUTTON), vertexBufferObjectManager) {
             @Override
             public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
                 if (pSceneTouchEvent.isActionDown() && !gameScene.hasChildScene())
                     GameManager.getInstance().pauseGame();
                 return true;
             }
         };
         pauseBtn.setZIndex(200);
         gameScene.registerTouchArea(pauseBtn);
         gameScene.attachChild(pauseBtn);
     }
 
     @Override
     public boolean onMenuItemClicked(MenuScene pMenuScene, IMenuItem pMenuItem, float pMenuItemLocalX, float pMenuItemLocalY) {
         switch (pMenuItem.getID()) {
             case 1:
                 GameManager.getInstance().unpauseGame();
                 break;
             case 2:
                GameManager.getInstance().showResultScreen();
                 break;
         }
         return false;
     }
 }
