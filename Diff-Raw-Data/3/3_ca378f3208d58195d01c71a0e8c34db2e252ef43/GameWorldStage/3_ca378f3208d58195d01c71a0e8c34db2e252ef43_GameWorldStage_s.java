 package com.icbat.game.tradesong.stages;
 
import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.Touchable;
 import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
 import com.icbat.game.tradesong.Item;
 import com.icbat.game.tradesong.ItemFactory;
 import com.icbat.game.tradesong.Tradesong;
 
 import java.util.Random;
 
 /** This stage governs:
  *  - items
  *  - the draggable, clear background
  *  - it's also a nice encapsulation of adding/removing items
  *  */
 public class GameWorldStage extends Stage {
 
     public static final String SPRITES_ITEMS_PNG = "sprites/items.png";
 
     // Map properties TODO extract this out to a map handler or something...
     public static final String PROPERTY_INITIAL_SPAWN_COUNT = "initialSpawnCount";
     public static final String PROPERTY_SPAWN_CAPACITY = "maxSpawnCapacity";
     public static final String PROPERTY_SPAWNABLE_ITEMS = "spawnableItems";
 
     private final Tradesong gameInstance;
     private final String[] possibleItemSpawns;
     private int mapX = 0;
     private int mapY = 0;
 
 
     private Actor backgroundActor = new Actor();
 
     int initialItemCount = 4;  // TODO pull this out of map properties
     int itemCount;
     int maxSpawnedPerMap = 10; // TODO pull this out of map properties
 
     public GameWorldStage(Tradesong gameInstance, MapProperties properties) {
 
         // Get coords for setting bounds
         mapX = (Integer)properties.get("width");
         mapY = (Integer)properties.get("height");
 
         //    // Actor for dragging map around. Covers all the ground but doesn't have an image
         backgroundActor.setTouchable(Touchable.enabled);
         backgroundActor.setVisible(true);
         this.addActor(backgroundActor);
 
         // Grab texture to pass to item factory
         this.gameInstance = gameInstance;
         this.gameInstance.assets.load(SPRITES_ITEMS_PNG, Texture.class);
         this.gameInstance.assets.finishLoading();
 
         // Use the Map properties to get some good stuff
         possibleItemSpawns = ((String)properties.get(PROPERTY_SPAWNABLE_ITEMS)).split(",");
 
         for (int i = 0; i < initialItemCount; ++i) {
             spawnItem();
         }
     }
 
     public boolean spawnItem() {
         Item item = ItemFactory.makeRandomItem((Texture)gameInstance.assets.get(SPRITES_ITEMS_PNG), possibleItemSpawns);
         return spawnItem(item);
     }
 
     /** @return true if the item was successfully added */
     public boolean spawnItem(Item item) {
         if (itemCount < 1 + maxSpawnedPerMap) {
 
             item.addListener(new ItemClickListener(item));
             int[] coords = getRandomCoords();
             item.setBounds(coords[0], coords[1], 34, 34);   // TODO constants
             item.setTouchable(Touchable.enabled);
             item.setVisible(true);
             this.addActor(item);
 
 
 
             ++itemCount;
 
             return true;
         }
         else {
             return false;
         }
     }
 
     public int[] getRandomCoords() {
         int[] output = new int[2];
 
         Random random = new Random();
 
         output[0] = random.nextInt(mapX) * 32;
         output[1] = random.nextInt(mapY) * 32;
 
 
         return output;
     }
 
 
 
     public void removeItemCount() {
         removeItemCount(1);
     }
 
     public void removeItemCount(int i) {
         itemCount -= i;
     }
 
     public Actor getBackgroundActor() {
         return backgroundActor;
     }
 
 
     /** Class to handle touching/clicking of items on levels.  */
     class ItemClickListener extends ClickListener {
         Item owner;
 
         ItemClickListener(Item owner) {
             this.owner = owner;
         }
 
         @Override
         public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
             super.touchUp(event, x, y, pointer, button);
             boolean outcome = gameInstance.gameState.getInventory().add(owner);
             if (outcome) {
                 removeItemCount();
                 owner.remove();
             }
             return true;
         }
     }
 
 
 }
