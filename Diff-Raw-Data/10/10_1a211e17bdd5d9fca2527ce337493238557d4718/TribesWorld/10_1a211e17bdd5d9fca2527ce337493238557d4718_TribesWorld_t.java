 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.first.tribes.core;
 
 import com.first.tribes.core.Tribes.PointerFocusable;
 import com.first.tribes.core.being.Being;
 import com.first.tribes.core.being.Being.Personality;
 import com.first.tribes.core.being.Cave;
 import com.first.tribes.core.being.Monster;
 import com.first.tribes.core.being.Village;
 import com.first.tribes.core.being.Villager;
 import com.first.tribes.core.ui.MiniMap;
 import com.first.tribes.core.ui.toolbar.Toolbar;
 import com.first.tribes.core.util.Timer;
 import com.first.tribes.core.pcg.*;
 import static playn.core.PlayN.*;
 import java.util.ArrayList;
 import java.util.ConcurrentModificationException;
 import java.util.List;
 import playn.core.Color;
 import playn.core.Connection;
 import playn.core.GroupLayer;
 import playn.core.ImmediateLayer;
 import playn.core.Layer;
 import playn.core.Mouse.LayerListener;
 import playn.core.Platform;
 import playn.core.PlayN;
 import playn.core.Pointer.Listener;
 import playn.core.Surface;
 import playn.core.Touch;
 import playn.core.gl.GLShader;
 import pythagoras.f.Dimension;
 import pythagoras.f.Point;
 import pythagoras.f.Rectangle;
 import pythagoras.f.Transform;
 
 /**
  *
  * @author taylor
  */
 public class TribesWorld implements PointerFocusable {
 
     private static final float MAX_MAGNIFICATION = 1.0f;
     private static final float MIN_MAGNIFICATION = 0.1f;
     private MiniMap miniMap;
     private Toolbar toolbar;
     private GroupLayer.Clipped groupLayer;
     private GroupLayer extraLayer;
     private List<Layer> rightLayers = new ArrayList<Layer>();
     private Cave cave;
     private Dimension absoluteSize;
     private Rectangle viewPort;
     private List<Village> villages = new ArrayList<Village>();
     private List<DrawnObject> extraDrawnObjects = new ArrayList<DrawnObject>();
     private Tile[][] tiles;
     private Tribes game;
     private EnemyGod enemy;
     private Timer enemyTimer;
     public float waterLevel = 0;
     public static final float NORMAL_WATER_LEVEL = 0;
 	private static final long REPEATED_ENEMY_DELAY = 5000;
 	private static final long INITIAL_ENEMY_DELAY = 25000;
 
     private final float EXTRA_VIEWPORT_PADDING() {
         return 60f / scale();
     }
 
     public TribesWorld(Tribes game, List<Personality> sample) {
         this.game = game;
 
         if (platformType() == Platform.Type.HTML) {
             absoluteSize = new Dimension(10000, 5000);
         } else {
             absoluteSize = new Dimension(20000, 10000);
         }
         viewPort = new Rectangle(0, 0, Tribes.SCREEN_WIDTH, Tribes.SCREEN_HEIGHT);
 
         tiles = new Tile[(int) (absoluteSize.width / Tile.TILE_SIZE)][(int) (absoluteSize.height / Tile.TILE_SIZE)];
 
         for (int i = 0; i < tiles.length; i++) {
             for (int j = 0; j < tiles[i].length; j++) {
                 tiles[i][j] = new Tile(i, j, this);
             }
         }
 
         generateMap(tiles, absoluteSize);
 
         // Have the tiles go through several update cycles
         for (int time = 0; time < 800; time++) {
             for (int i = 0; i < tiles.length; i++) {
                 for (int j = 0; j < tiles[i].length; j++) {
                     tiles[i][j].update(100);
                 }
             }
         }
 
         boolean isSafe = false;
         float xPos;
         float yPos;
         do {
             xPos = random() * absoluteSize.width;
             yPos = random() * absoluteSize.height;
             isSafe = !this.unsafe(xPos, yPos, 10.0f);
         } while (!isSafe);
         if (sample == null) {
             villages.add(new Village(xPos, yPos, 10, this, Color.rgb(200, 0, 0)));
 
         } else {
             villages.add(new Village(xPos, yPos, sample, this, Color.rgb(200, 0, 0)));
         }
         isSafe = false;
         do {
             xPos = random() * absoluteSize.width;
             yPos = random() * absoluteSize.height;
             isSafe = !this.unsafe(xPos, yPos, 10.0f);
         } while (!isSafe);
         villages.add(new Village(xPos, yPos, 10, this, Color.rgb(0, 0, 200)));
 
         cave = new Cave(this, Color.rgb(25, 50, 25));
         viewPort.x = villages.get(0).xPos() - viewPort.width / 2;
         viewPort.y = villages.get(0).yPos() - viewPort.height / 2;
 
         groupLayer = graphics().createGroupLayer(Tribes.SCREEN_WIDTH, Tribes.SCREEN_HEIGHT);
         extraLayer = graphics().createGroupLayer();
 
 
         toolbar = new Toolbar(this);
         miniMap = new MiniMap(this);
 
         // Layer for base (tiles & villagers)
         groupLayer.add(graphics().createImmediateLayer(Tribes.SCREEN_WIDTH, Tribes.SCREEN_HEIGHT, new ImmediateLayer.Renderer() {
             @Override
             public void render(Surface surface) {
                 for (int i = 0; i < tiles.length; i++) {
                     for (int j = 0; j < tiles[i].length; j++) {
                         tiles[i][j].paint(surface, viewPort, scale());
                     }
                 }
                 for (DrawnObject extraObject : extraDrawnObjects) {
                     extraObject.paint(surface, viewPort, scale());
                 }
                 for (Villager villager : villagers()) {
                     villager.paint(surface, viewPort, scale());
                 }
                 for (Being being : monsters()) {
                     being.paint(surface, viewPort, scale());
                 }
             }
         }));
 
 
         // Layer for various extras (drag appearance, villager stats)
         groupLayer.add(extraLayer);
 
 
         // Layer for Minimap
         Layer miniMapLayer = graphics().createImmediateLayer((int) miniMap.width(), (int) miniMap.height(), new ImmediateLayer.Renderer() {
             @Override
             public void render(Surface surface) {
                 surface.drawImage(miniMap.image(), 0, 0);
             }
         });
         groupLayer.addAt(miniMapLayer, miniMap.x(), miniMap.y());
         rightLayers.add(miniMapLayer);
 
         // Layer for Village stats
         Layer villageStats = graphics().createImmediateLayer(new ImmediateLayer.Renderer() {
             @Override
             public void render(Surface surface) {
                 for (int i = 0; i < villages.size(); i++) {
                     villages.get(i).drawStatsBoxAt(surface, 0, (Village.STATS_BOX_HEIGHT + 5) * i, miniMap.width(), Village.STATS_BOX_HEIGHT);
                 }
             }
         });
         groupLayer.addAt(villageStats, miniMap.x(), miniMap.y() + miniMap.height() + 10);
         rightLayers.add(villageStats);
 
 
         // Layer for Toolbar
         groupLayer.addAt(graphics().createImmediateLayer((int) toolbar.width(), (int) toolbar.height(), new ImmediateLayer.Renderer() {
             @Override
             public void render(Surface surface) {
                 toolbar.render(surface);
             }
         }), toolbar.x(), toolbar.y());
         
        if(EnemyGod.ON){
         enemy=new EnemyGod(this);
         enemyTimer = new Timer(game);
         enemyTimer.schedule(enemy, INITIAL_ENEMY_DELAY, REPEATED_ENEMY_DELAY);
     }
    }
 
     public TribesWorld(Tribes game) {
         this(game, null);
     }
 
     public List<Villager> villagers() {
         List<Villager> villagers = new ArrayList<Villager>();
         for (Village village : villages) {
             villagers.addAll(village.villagers());
         }
         return villagers;
     }
 
     public List<Monster> monsters() {
         return cave.monsters();
     }
 
     public int addExtraLayer(Layer additionalLayer) {
         extraLayer.add(additionalLayer);
         return extraLayer.size() - 1;
     }
 
     public void removeExtraLayer(int layerIndex) {
         extraLayer.remove(extraLayer.get(layerIndex));
     }
 
     public void addExtraDrawnObject(DrawnObject object) {
         extraDrawnObjects.add(object);
     }
 
     public void removeExtraDrawnObject(DrawnObject object) {
         extraDrawnObjects.remove(object);
     }
 
     public GroupLayer getLayer() {
         return groupLayer;
     }
 
     public Tribes game() {
         return game;
     }
 
     public float width() {
         return absoluteSize.width;
     }
 
     public int tileWidth() {
         return tiles.length;
     }
 
     public float height() {
         return absoluteSize.height;
     }
 
     public int tileHeight() {
         return tiles[0].length;
     }
 
     public void update(float delta) {
         for (Village village : villages) {
             village.update(delta);
         }
         for (int i = 0; i < tiles.length; i++) {
             for (int j = 0; j < tiles[i].length; j++) {
                 tiles[i][j].update(delta);
             }
         }
         cave.update(delta);
 
         try {
             for (DrawnObject extraObject : extraDrawnObjects) {
                 extraObject.update(delta);
             }
         } catch (ConcurrentModificationException e) {
         }
     }
 
     public float scale() {
         return Tribes.SCREEN_WIDTH / viewPort.width;
     }
 
     public void moveViewPort(float deltaX, float deltaY) {
         viewPort.translate(deltaX / scale(), deltaY / scale());
         verifyViewPortPosition();
     }
 
     public void zoomDelta(float zoomAmount) {
         float newWidth = viewPort.width * zoomAmount;
         float newHeight = viewPort.height * zoomAmount;
 
         if (Tribes.SCREEN_WIDTH / newWidth > MAX_MAGNIFICATION) {
             newWidth = Tribes.SCREEN_WIDTH / MAX_MAGNIFICATION;
         } else if (Tribes.SCREEN_WIDTH / newWidth < MIN_MAGNIFICATION) {
             newWidth = Tribes.SCREEN_WIDTH / MIN_MAGNIFICATION;
         }
         if (Tribes.SCREEN_HEIGHT / newHeight > MAX_MAGNIFICATION) {
             newHeight = Tribes.SCREEN_HEIGHT / MAX_MAGNIFICATION;
         } else if (Tribes.SCREEN_HEIGHT / newHeight < MIN_MAGNIFICATION) {
             newHeight = Tribes.SCREEN_HEIGHT / MIN_MAGNIFICATION;
         }
 
         float deltaWidth = viewPort.width - newWidth;
         float deltaHeight = viewPort.height - newHeight;
 
         float newX = viewPort.x + deltaWidth / 2;
         float newY = viewPort.y + deltaHeight / 2;
 
         viewPort = new Rectangle(newX, newY, newWidth, newHeight);
 
         verifyViewPortPosition();
     }
 
     public void verifyViewPortPosition() {
 
         if (viewPort.width <= absoluteSize.width) {
             if (viewPort.x < -EXTRA_VIEWPORT_PADDING()) {
                 viewPort.x = -EXTRA_VIEWPORT_PADDING();
             }
             if (viewPort.x + viewPort.width > absoluteSize.width + EXTRA_VIEWPORT_PADDING()) {
                 viewPort.x = (absoluteSize.width + EXTRA_VIEWPORT_PADDING()) - viewPort.width;
             }
         } else {
             //Center it
             viewPort.x = (absoluteSize.width - viewPort.width) / 2;
         }
 
         for (Layer layer : rightLayers) {
             layer.setAlpha(1f - ((viewPort.x + viewPort.width - 5000) / absoluteSize.width)/1.2f);
         }
 
 
         if (viewPort.height <= absoluteSize.height) {
             if (viewPort.y < -EXTRA_VIEWPORT_PADDING()) {
                 viewPort.y = -EXTRA_VIEWPORT_PADDING();
             }
             if (viewPort.y + viewPort.height > absoluteSize.height + EXTRA_VIEWPORT_PADDING()) {
                 viewPort.y = (absoluteSize.height + EXTRA_VIEWPORT_PADDING()) - viewPort.height;
             }
         } else {
             //Center it
             viewPort.y = (absoluteSize.height - viewPort.height) / 2;
         }
     }
 
     public Point worldPointFromScreenPoint(Point point) {
         float realX = (point.x / scale()) + viewPort.x;
         float realY = (point.y / scale()) + viewPort.y;
 
         return new Point(realX, realY);
     }
 
     public Point screenPointFromWorldPoint(Point point) {
         float realX = (point.x - viewPort.x) * scale();
         float realY = (point.y - viewPort.y) * scale();
 
         return new Point(realX, realY);
     }
 
     public boolean unsafe(float xPos, float yPos, float minFood) {
         int tileX = (int) (xPos / Tile.TILE_SIZE);
         int tileY = (int) (yPos / Tile.TILE_SIZE);
         if (tileX >= tiles.length) {
             tileX = tiles.length - 1;
         }
         if (tileY >= tiles[0].length) {
             tileY = tiles[0].length - 1;
         }
 
         if (tileX < 0) {
             tileX = 0;
         }
         if (tileY < 0) {
             tileY = 0;
         }
 
         return !tiles[tileX][tileY].isSafe(minFood);
     }
 
     private static void generateMap(Tile[][] map, Dimension absoluteSize) {
         int tokens = (int) (0.00002 * (absoluteSize.width * absoluteSize.height));
         int pooling = 4;
         while (tokens > pooling) {
             CoastLineAgent coastAgent = new CoastLineAgent((int) (random() * map.length), (int) (random() * map[0].length), (int) (random() * (tokens / pooling + 1)), (int) (random() * tokens / 2));
             tokens -= coastAgent.tokens();
             coastAgent.act(map);
         }
 
         tokens = (int) (0.00006 * (absoluteSize.width * absoluteSize.height));
         while (tokens > 2) {
             MountainAgent mountainAgent = new MountainAgent((int) (random() * map.length), (int) (random() * map[0].length), (int) (random() * tokens), (int) (random() * tokens / 2));
             tokens -= mountainAgent.tokens();
             mountainAgent.act(map);
         }
 
         tokens = (int) (0.00001 * (absoluteSize.width * absoluteSize.height));
         while (tokens > 10) {
             MountainAgent mountainAgent = new MountainAgent((int) (random() * map.length), (int) (random() * map[0].length), (int) (random() * (tokens / 10 + 1)), (int) (random() * tokens / 2));
             mountainAgent.setHeightPush(1);
             tokens -= mountainAgent.tokens();
             mountainAgent.act(map);
         }
     }
 
     public Tile tileAt(float xPos, float yPos) {
         int tileX = (int) (xPos / Tile.TILE_SIZE);
         int tileY = (int) (yPos / Tile.TILE_SIZE);
 
         if (tileX >= tiles.length) {
             tileX = tiles.length - 1;
         }
         if (tileY >= tiles[0].length) {
             tileY = tiles[0].length - 1;
         }
         if (tileX < 0)
             tileX = 0;
         if (tileY < 0)
             tileY = 0;
         return tiles[tileX][tileY];
     }
 
     public Dimension getAbsoluteSize() {
         return absoluteSize;
     }
 
     public List<Villager> villagersInArea(Rectangle rectangle) {
         if (rectangle.width < 0) {
             rectangle.x = rectangle.x + rectangle.width;
             rectangle.width *= -1;
         }
         if (rectangle.height < 0) {
             rectangle.y = rectangle.y + rectangle.height;
             rectangle.height *= -1;
         }
 
         List<Villager> area = new ArrayList<Villager>();
         for (Villager villager : villagers()) {
             if (rectangle.contains(villager.xPos(), villager.yPos())) {
                 area.add(villager);
             }
         }
 
         return area;
     }
 
     public List<Village> villages() {
         return new ArrayList<Village>(villages);
     }
 
     public Toolbar toolbar() {
         return toolbar;
     }
 
     public Dimension absoluteSize() {
         return absoluteSize;
     }
 
     public Tile[][] tiles() {
         return tiles;
     }
 
     public Rectangle viewPort() {
         return viewPort;
     }
 
     public PointerFocusable press(float x, float y) {
         if (miniMap.pointInMiniMap(x, y)) {
             return miniMap.press(x, y);
         } else {
             return toolbar.press(x, y);
         }
     }
 
     public void release(float x, float y) {
         if (miniMap.pointInMiniMap(x, y)) {
             miniMap.release(x, y);
         } else {
             toolbar.press(x, y);
         }
     }
 
     public void drag(float x, float y) {
         if (miniMap.pointInMiniMap(x, y)) {
             miniMap.drag(x, y);
         } else {
             toolbar.drag(x, y);
         }
     }
 
     public void ping(Point p){
     	miniMap.addPing(p);
     }
     
     public ArrayList<Cave> caves() {
         ArrayList<Cave> caves = new ArrayList<Cave>(1);
         caves.add(cave);
         return caves;
     }
 }
