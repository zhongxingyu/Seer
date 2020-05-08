 package com.bensoft.mulg2.screen;
 
 import static com.bensoft.mulg2.Constants.ALLOW_DEATH;
 import static com.bensoft.mulg2.Constants.GAME_SCREEN_HEIGHT_PIXELS;
 import static com.bensoft.mulg2.Constants.GAME_SCREEN_HEIGHT_TILES;
 import static com.bensoft.mulg2.Constants.GAME_SCREEN_INVENTORY_HEIGHT;
 import static com.bensoft.mulg2.Constants.GAME_SCREEN_WIDTH_PIXELS;
 import static com.bensoft.mulg2.Constants.GAME_SCREEN_WIDTH_TILES;
 import static com.bensoft.mulg2.Constants.TILE_WIDTH_PIXELS;
 
 import org.andengine.engine.camera.Camera;
 import org.andengine.engine.camera.hud.HUD;
 import org.andengine.engine.handler.IUpdateHandler;
 import org.andengine.engine.handler.IUpdateHandler.IUpdateHandlerMatcher;
 import org.andengine.engine.handler.timer.ITimerCallback;
 import org.andengine.engine.handler.timer.TimerHandler;
 import org.andengine.entity.primitive.Rectangle;
 import org.andengine.entity.scene.IOnSceneTouchListener;
 import org.andengine.entity.scene.Scene;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.entity.text.Text;
 import org.andengine.entity.util.FPSCounter;
 import org.andengine.input.touch.TouchEvent;
 import org.andengine.opengl.font.Font;
 import org.andengine.util.adt.color.Color;
 import org.andengine.util.debug.Debug;
 
 import android.annotation.SuppressLint;
 import android.util.SparseArray;
 import android.view.Gravity;
 import android.widget.Toast;
 
 import com.bensoft.mulg2.AccelerationData;
 import com.bensoft.mulg2.GameManager;
 import com.bensoft.mulg2.RealtimeFPSCounter;
 import com.bensoft.mulg2.game.Game;
 import com.bensoft.mulg2.game.level.Level;
 import com.bensoft.mulg2.model.Ball;
 import com.bensoft.mulg2.textures.GameTextureBundle;
 import com.bensoft.mulg2.textures.TextureBundleManager;
 
 /**
  * 
  * @author Ben Wolsieffer
  */
 public class GameScreen extends Screen<GameTextureBundle> {
 
     private Camera camera;
     private Level level;
     private HUD hud;
     private FPSCounter fpsCounter;
     private Game game;
     private int currLevelIndex = 0;
     private Rectangle inventoryBackground;
 
     private final SparseArray<Sprite> inventory = new SparseArray<Sprite>();
 
     public GameScreen() {
         super(TextureBundleManager.getGameTextureBundle());
 
         hud = new HUD();
 
         runOnUiThread(new Runnable() {
             @SuppressLint("ShowToast")
             @Override
             public void run() {
                 diedToast = Toast.makeText(getContext(), "You died!", Toast.LENGTH_SHORT);
                 diedToast.setGravity(Gravity.CENTER, 0, 0);
             }
         });
 
         camera = getEngine().getCamera();
         float camX = -TILE_WIDTH_PIXELS / 2;
         float camY = TILE_WIDTH_PIXELS / 2 - GAME_SCREEN_INVENTORY_HEIGHT;
         camera.set(camX, camY, camX + GAME_SCREEN_WIDTH_PIXELS, camY + GAME_SCREEN_HEIGHT_PIXELS);
 
         // fpsCounter = new FPSCounter();
         fpsCounter = new RealtimeFPSCounter();
         getEngine().registerUpdateHandler(fpsCounter);
 
         Font font = getTextureBundle().getDefaultFont();
         font.prepareLetters('F', 'P', 'S', ':', ' ', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.');
         final Text fpsText = new Text(30, camera.getHeight() - 8, font, "FPS:", 10, getEngine().getVertexBufferObjectManager());
         fpsText.setZIndex(10);
         hud.attachChild(fpsText);
 
         scene.registerUpdateHandler(new TimerHandler(1 / 5.0f, true, new ITimerCallback() {
             @Override
             public void onTimePassed(final TimerHandler pTimerHandler) {
                 float fps = Math.round(fpsCounter.getFPS() * 100) / 100;
                 fpsText.setText("FPS: " + fps);
             }
         }));
 
         scene.setOnSceneTouchListener(new IOnSceneTouchListener() {
             @Override
             public boolean onSceneTouchEvent(Scene scene, final TouchEvent pSceneTouchEvent) {
                 if (AccelerationData.isOverrideEnabled()) {
                     if (pSceneTouchEvent.isActionUp()) {
                         AccelerationData.setAcceleration(0, 0, 0);
                     } else {
                         float x = pSceneTouchEvent.getX() + 8 - camera.getXMin();
                         float y = pSceneTouchEvent.getY() + 8 - camera.getYMin();
                         float xAcc = ((x - 80) / 80) * -9.8f;
                         if (y > 144) {
                             y = 144;
                         }
                         float yAcc = ((y - 72) / 72) * 9.8f;
                         AccelerationData.setAcceleration(xAcc, yAcc, 0);
                     }
                 }
                 return true;
             }
         });
         // Game loading
         game = GameManager.getSelectedGame();
         loadLevel(game.getLevels()[currLevelIndex]);
 
         inventoryBackground = new Rectangle(camera.getWidth() / 2, 10, camera.getWidth(), GAME_SCREEN_INVENTORY_HEIGHT, getEngine().getVertexBufferObjectManager());
        inventoryBackground.setColor(new Color(64, 64, 64));
         hud.attachChild(inventoryBackground);
         hud.sortChildren();
 
         camera.setHUD(hud);
 
         AccelerationData.init(getContext());
         AccelerationData.start();
     }
 
     private void resetScreen() {
         if (level != null) {
             scene.detachChildren();
             scene.clearTouchAreas();
             scene.clearEntityModifiers();
             scene.unregisterUpdateHandlers(new IUpdateHandlerMatcher() {
 
                 @Override
                 public boolean matches(IUpdateHandler pItem) {
                     return !(pItem instanceof TimerHandler);
                 }
             });
             level.unload();
             resetHud();
         }
     }
 
     private void resetHud() {
         inventoryBackground.detachChildren();
         hud.clearTouchAreas();
         inventory.clear();
     }
 
     public void nextLevel() {
         loadLevel(game.getLevels()[++currLevelIndex]);
     }
 
     private void loadLevel(Level l) {
         resetScreen();
         level = l;
         Debug.i("Loading level: " + level.getName());
         level.load();
 
         // Ball creation
         final Ball ball = level.getBall();
         final Sprite ballSprite = ball.getSprite(getTextureBundle(), getEngine().getVertexBufferObjectManager());
         scene.attachChild(ballSprite);
 
         scene.registerUpdateHandler(level.getPhysicsWorld());
         scene.registerUpdateHandler(new IUpdateHandler() {
 
             @Override
             public void onUpdate(float pSecondsElapsed) {
                 float ballX = ballSprite.getX();
                 float ballY = ballSprite.getY();
                 if (ballX + TILE_WIDTH_PIXELS / 2 > camera.getXMax()) {
                     camera.setCenter(camera.getCenterX() + (((GAME_SCREEN_WIDTH_TILES - 1) * TILE_WIDTH_PIXELS)), camera.getCenterY());
                 } else if (ballX - TILE_WIDTH_PIXELS / 2 < camera.getXMin()) {
                     camera.setCenter(camera.getCenterX() - (((GAME_SCREEN_WIDTH_TILES - 1) * TILE_WIDTH_PIXELS)), camera.getCenterY());
                 }
 
                 if (ballY + TILE_WIDTH_PIXELS / 2 > camera.getYMax()) {
                     camera.setCenter(camera.getCenterX(), camera.getCenterY() + (((GAME_SCREEN_HEIGHT_TILES - 1) * TILE_WIDTH_PIXELS)));
                 } else if (ballY - TILE_WIDTH_PIXELS / 2 < camera.getYMin() + GAME_SCREEN_INVENTORY_HEIGHT) {
                     camera.setCenter(camera.getCenterX(), camera.getCenterY() - (((GAME_SCREEN_HEIGHT_TILES - 1) * TILE_WIDTH_PIXELS)));
                 }
             }
 
             @Override
             public void reset() {
             }
 
         });
         for (int x = 0; x < level.getWidth(); x++) {
             for (int y = 0; y < level.getHeight(); y++) {
                 Sprite[] tileSprites = level.getTileAt(x, y).getSprites(getTextureBundle(), getEngine().getVertexBufferObjectManager());
                 for (Sprite ts : tileSprites) {
                     scene.attachChild(ts);
                 }
             }
         }
         scene.sortChildren(false);
         fpsCounter.reset();
     }
 
     public void pickUpNote(final int i) {
         Sprite iS = new Sprite(inventory.size() * 15 + 10, inventoryBackground.getHeight() / 2, textureBundle.getInventoryNoteTexture(), getEngine().getVertexBufferObjectManager()) {
             @Override
             public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                 if (pSceneTouchEvent.isActionDown()) {
                     inventory.remove(i);
                     inventoryBackground.detachChild(this);
                     hud.unregisterTouchArea(this);
                     for (int k = inventory.indexOfKey(i) + 1; k < inventory.size(); k++) {
                         Sprite s = inventory.valueAt(k);
                         s.setX(s.getX() - 15);
                     }
                     runOnUiThread(new Runnable() {
                         @Override
                         public void run() {
                             Toast t = Toast.makeText(getContext(), game.getNote(i), Toast.LENGTH_LONG);
                             t.setGravity(Gravity.CENTER, 0, 0);
                             t.show();
                         }
                     });
                 }
                 return true;
             }
         };
         hud.registerTouchArea(iS);
         inventory.put(i, iS);
         inventoryBackground.attachChild(iS);
     }
 
     private Toast diedToast;
     public void kill() {
         if (ALLOW_DEATH) {
             // Vector2 startingPos = level.getStartingPosition();
             // level.getBall().setPosition(startingPos.x, startingPos.y);
             // level.getBall().getBody().setLinearVelocity(new Vector2());
 
             runOnUiThread(new Runnable() {
                 @Override
                 public void run() {
                     diedToast.show();
                 }
             });
             loadLevel(level);
         }
     }
     @Override
     public void onDestroy() {
         AccelerationData.stop();
     }
 
 }
