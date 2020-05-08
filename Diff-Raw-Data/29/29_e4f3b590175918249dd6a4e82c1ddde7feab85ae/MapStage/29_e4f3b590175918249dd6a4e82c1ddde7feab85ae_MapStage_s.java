 package com.icbat.game.tradesong.screens.stages;
 
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Camera;
 import com.badlogic.gdx.maps.MapProperties;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
 import com.badlogic.gdx.utils.Timer;
 import com.icbat.game.tradesong.Tradesong;
 import com.icbat.game.tradesong.gameObjects.Item;
 import com.icbat.game.tradesong.observation.notifications.GatherNotification;
 import com.icbat.game.tradesong.observation.notifications.StopNotification;
 import com.icbat.game.tradesong.observation.watchers.GatheringWatcher;
 import com.icbat.game.tradesong.screens.listeners.DragMoveListener;
 
 import java.util.*;
 
 public class MapStage extends BaseStage {
     private HashSet<String> spawnableItemNames = new HashSet<String>();
     private HashSet<ValidSpawnArea> spawnAreas = new HashSet<ValidSpawnArea>();
     private Timer spawnTimer = new Timer();
     private int maxItemsOnMap = 0;
 
     public MapStage(MapProperties mapProperties) {
         setupAreas(mapProperties);
         getPrototypeNames(mapProperties);
         getMaxItems(mapProperties);
         spawnInitialItems(mapProperties);
     }
 
     @Override
     public void layout() {
         super.layout();
         notificationCenter.addWatcher(new GatheringWatcher());
     }
 
     private void getMaxItems(MapProperties mapProperties) {
         String maxSpawnsBlob = (String) mapProperties.get("maxSpawnCapacity");
         if (maxSpawnsBlob != null) {
             maxItemsOnMap = Integer.parseInt(maxSpawnsBlob);
         }
     }
 
     private void setupAreas(MapProperties mapProperties) {
         String validSpawnAreasBlob = (String) mapProperties.get("validSpawnAreas");
         if (validSpawnAreasBlob != null) {
             String[] spawnAreas = validSpawnAreasBlob.split(";");
 
             for (String area : spawnAreas) {
                 String[] coords = area.split(",");
                 this.spawnAreas.add(new ValidSpawnArea(coords));
             }
         }
     }
 
     private void getPrototypeNames(MapProperties mapProperties) {
         String spawnableItemsBlob = (String) mapProperties.get("spawnableItems");
         if (spawnableItemsBlob != null) {
             String[] spawnableItems = spawnableItemsBlob.split(",");
             Collections.addAll(this.spawnableItemNames, spawnableItems);
             for (String name : this.spawnableItemNames) {
                 Gdx.app.debug("spawnableItem", name);
             }
         }
 
     }
 
     private void spawnInitialItems(MapProperties mapProperties) {
         String initialNodesString = (String) mapProperties.get("initialSpawnCount");
         Integer initialNodes = (initialNodesString == null) ? 0 : Integer.parseInt(initialNodesString);
         if ((!spawnableItemNames.isEmpty() && !spawnAreas.isEmpty())) {
             List<String> spawnableItems = new ArrayList<String>(spawnableItemNames);
             List<ValidSpawnArea> areas = new ArrayList<ValidSpawnArea>(spawnAreas);        // consider using the stage or a group's random actor fn
 
             for (int i = 0; i < initialNodes; ++i) {
                 if (spawnRandomItem(spawnableItems, areas)) return;
             }

            this.spawnTimer.scheduleTask(new Timer.Task() {
                @Override
                public void run() {
                    if (getActors().size < maxItemsOnMap) {
                        spawnRandomItem(new ArrayList<String>(spawnableItemNames), new ArrayList<ValidSpawnArea>(spawnAreas));
                    }

                }
            }, 3, 3);
         }


     }
 
     private boolean spawnRandomItem(List<String> spawnableItems, List<ValidSpawnArea> areas) {
         Random random = new Random();
         String nameToSpawn = spawnableItems.get(random.nextInt(spawnableItemNames.size()));
 
         Item spawn = Tradesong.itemPrototypes.get(nameToSpawn);
 
         return spawnItem(areas, spawn);
     }
 
     private boolean spawnItem(List<ValidSpawnArea> areas, Item spawn) {
         if (spawn == null) {
             Gdx.app.error("spawning item on map", "null");
             return true;
         }
 
         areas.get(0).spawnItemInsideArea(spawn);
         spawn.addListener(new GatherClickListener(spawn));
         this.addActor(spawn); // Looks weird, but the spawn area just sets position on the actor.
         Gdx.app.debug("spawned " + spawn.getName() + " at", spawn.getGameX() + ", " + spawn.getGameY());
         return false;
     }
 
 
     public void setDragListener(Camera camera) {
         Gdx.app.debug("", "Setting a drag listener");
         this.addListener(new DragMoveListener(camera));
     }
 
     @Override
     public void hide() {
         spawnTimer.stop();
         spawnTimer.clear();
         notificationCenter.notifyWatchers(new StopNotification());
     }
 
     /**
      * Rectangular area in which items can spawn. Provides clean way to spawn an item in inside the area.
      */
     class ValidSpawnArea {
         Integer startX;
         Integer startY;
         Integer endX;
         Integer endY;
         private static final int ICON_SIZE = 32;
 
         /**
          * Expects exactly 4 elements. Will fail with errors if passed less than 4.
          */
         public ValidSpawnArea(String[] coords) {
             this.startX = Integer.parseInt(coords[0]);
             this.startY = Integer.parseInt(coords[1]);
             this.endX = Integer.parseInt(coords[2]);
             this.endY = Integer.parseInt(coords[3]);
         }
 
         public void spawnItemInsideArea(Item item) {
             Random random = new Random();
             int x = random.nextInt(endX - startX + 1) + startX;
             int y = random.nextInt(endY - startY + 1) + startY + 3; // Why does +3 work?
             x *= ICON_SIZE;
             y *= ICON_SIZE;
 
             item.setPosition(x, y);
         }
     }
 
     /**
      * Class to handle touching/clicking of items on levels.
      */
     class GatherClickListener extends ClickListener {
         Item owner;
 
         GatherClickListener(Item owner) {
             this.owner = owner;
         }
 
         @Override
         public void clicked(InputEvent event, float x, float y) {
             notificationCenter.notifyWatchers(new GatherNotification(owner));
         }
 
     }
 }
