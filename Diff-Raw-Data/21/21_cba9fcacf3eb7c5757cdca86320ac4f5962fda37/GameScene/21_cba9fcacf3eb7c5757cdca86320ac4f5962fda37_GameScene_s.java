 package com.example.ship.game;
 
 import android.util.Log;
 import com.example.ship.R;
 import com.example.ship.SceletonActivity;
 import com.example.ship.atlas.ResourceManager;
 import org.andengine.engine.Engine;
 import org.andengine.entity.Entity;
 import org.andengine.entity.scene.Scene;
 import org.andengine.entity.scene.background.Background;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.opengl.texture.region.ITextureRegion;
 import org.andengine.util.color.Color;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 public class GameScene extends Scene {
     private static int layerCount = 0;
     public static final int LAYER_BACKGROUND  = layerCount++;
     public static final int LAYER_FIRST_WAVE  = layerCount++;
     public static final int LAYER_FIRST_SHIP_LINE  = layerCount++;
     public static final int LAYER_SECOND_WAVE = layerCount++;
     public static final int LAYER_SECOND_SHIP_LINE = layerCount++;
     public static final int LAYER_THIRD_WAVE  = layerCount++;
     public static final int LAYER_THIRD_SHIP_LINE  = layerCount++;
     public static final int LAYER_FOURTH_WAVE  = layerCount++;
     public static final int LAYER_TORPEDO = layerCount++;
     private static final int LAYER_GUN   = layerCount++;
     private static final float RELATIVE_SKY_HEIGHT = 0.15f;
     private static final float RELATIVE_WAVE_HEIGHT = 0.125f;
 
     private final SceletonActivity activity;
     private final Engine mEngine;
     private final ResourceManager resourceManager;
     private GameHUD gameHUD;
     private PauseHUD pauseHUD;
     private Sprite backgroundSprite;
     private ArrayList<Sprite> waveSprites;
     private Gun gun;
     private ShipSpawner shipSpawner;
     private ArrayList<Ship> ships;
     private HashMap<Integer, Float> shipLinesPosition;
     private Player player;
 
     public GameScene(final SceletonActivity activity) {
         super();
         this.activity = activity;
         this.mEngine = activity.getEngine();
         this.resourceManager = activity.getResourceManager();
 
         for(int i = LAYER_BACKGROUND; i < layerCount; i++) {
             this.attachChild(new Entity());
         }
 
         shipLinesPosition = new HashMap<Integer, Float>();
         ships = new ArrayList<Ship>();
         waveSprites = new ArrayList<Sprite>();
 
         createBackground();
         createWaves();
         createGun();
 
         gameHUD = new GameHUD(activity);
         gameHUD.setEventsToChildren(activity.getEvents());
 
         pauseHUD = new PauseHUD(activity);
         pauseHUD.setEventsToChildren(activity.getEvents());
 
         player = new Player(activity);
         player.setGameHUD(gameHUD);
     }
 
     public void switchToPauseHUD() {
         activity.getCamera().setHUD(pauseHUD);
     }
 
     public void switchToGameHUD() {
         activity.getCamera().setHUD(gameHUD);
     }
 
     public void attachSpriteToLayer(Sprite sprite, int layerId){
         this.getChildByIndex(layerId).attachChild(sprite);
     }
 
     public GameHUD getGameHUD() {
         return gameHUD;
     }
 
     public Player getPlayer() {
         return player;
     }
 
     public ShipSpawner getShipSpawner() {
         return shipSpawner;
     }
 
     public void setShipSpawner(ShipSpawner shipSpawner) {
         this.shipSpawner = shipSpawner;
     }
 
     public float getShipLinePosition(int lineId) {
         return shipLinesPosition.get(lineId);
     }
 
     public ArrayList<Ship> getShips() {
         return ships;
     }
 
     @Override
     protected void onManagedUpdate(float pSecondsElapsed) {
         Ship deadShip = null;
 
         for (Ship ship: ships) {
             Sprite shipSprite = ship.getSprite();
             float maxX = activity.getCamera().getXMax();
             float minX = activity.getCamera().getXMin() - shipSprite.getWidth();
 
             if (   ship.getDirection() && (shipSprite.getX() < minX)
                     || !ship.getDirection() && (shipSprite.getX() > maxX)) {
 
                 ship.missionDone();
                 deadShip = ship;
                 player.reduceHealth();
             }
         }
 
 
         // Ищем столкновение торпеды с небом
         Entity layer = (Entity) getChildByIndex(LAYER_TORPEDO);
         for (int i = 0; i < layer.getChildCount(); i++) {
             Sprite sprite = (Sprite) layer.getChildByIndex(i);
             boolean seaEnd = true;
             for (Sprite wave: waveSprites) {
                 if (sprite.collidesWith(wave)) {
                     seaEnd = false;
                     break;
                 }
             }
             if (seaEnd) {
                 layer.getChildByIndex(i).detachSelf();
             }
         }
 
         for (int i = 0; i < layer.getChildCount(); i++) {
             Sprite torpedo = (Sprite) layer.getChildByIndex(i);
             for (Ship ship: ships) {
                 if (torpedo.collidesWith(ship.getHitAreaSprite())) {
                     torpedo.detachSelf();
                    ship.hitShip();
                    player.addPoints(ship.score);
                    deadShip = ship;
                 }
             }
         }
 
         if (deadShip != null) {
             ships.remove(deadShip);
             Log.d("1log", "killed");
         }
 
         super.onManagedUpdate(pSecondsElapsed);
     }
  
     private void createBackground() {
         ITextureRegion backgroundTexture = resourceManager.getLoadedTextureRegion(R.drawable.gamebackground);
         this.backgroundSprite = new Sprite( 0
                                           , 0
                                           , backgroundTexture
                                           , mEngine.getVertexBufferObjectManager());
 
         this.getChildByIndex(LAYER_BACKGROUND).attachChild(backgroundSprite);
        
         Color backgroundColor = new Color(0.09804f, 0.6274f, 0.8784f);
         this.setBackground(new Background(backgroundColor));
     }
 
     private void createGun() {
         gun = new Gun(activity);
         this.getChildByIndex(LAYER_GUN).attachChild(gun.getSprite());
     }
 
     public Gun getGun() {
         return gun;
     }
 
     private void createWaves() {
         ITextureRegion waveTexture = resourceManager.getLoadedTextureRegion(R.drawable.wave);
         float offset = activity.getCamera().getHeightRaw() * RELATIVE_SKY_HEIGHT;
 
         attachTextureToLayer(waveTexture, LAYER_FIRST_WAVE, offset);
 
         offset += activity.getCamera().getHeightRaw() * RELATIVE_WAVE_HEIGHT;
         shipLinesPosition.put(LAYER_FIRST_SHIP_LINE, offset);
         attachTextureToLayer(waveTexture, LAYER_SECOND_WAVE, offset);
 
         offset += activity.getCamera().getHeightRaw() * RELATIVE_WAVE_HEIGHT;
         shipLinesPosition.put(LAYER_SECOND_SHIP_LINE, offset);
         attachTextureToLayer(waveTexture, LAYER_THIRD_WAVE, offset);
 
         offset += activity.getCamera().getHeightRaw() * RELATIVE_WAVE_HEIGHT;
         shipLinesPosition.put(LAYER_THIRD_SHIP_LINE, offset);
         attachTextureToLayer(waveTexture, LAYER_FOURTH_WAVE, offset);
     }
 
     private void attachTextureToLayer(ITextureRegion texture, int layerId, float yPosition) {
         Sprite waveSprite = new Sprite( 0
                                       , yPosition
                                       , texture
                                       , mEngine.getVertexBufferObjectManager());
 
         waveSprites.add(waveSprite);
         this.getChildByIndex(layerId).attachChild(waveSprite);
     }
 }
