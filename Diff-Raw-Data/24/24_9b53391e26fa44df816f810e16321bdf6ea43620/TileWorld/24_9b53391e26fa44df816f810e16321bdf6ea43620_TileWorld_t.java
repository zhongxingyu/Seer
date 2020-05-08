 package com.gravity.map;
 
 import java.util.List;
 import java.util.Map;
 
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.geom.Shape;
 import org.newdawn.slick.tiled.TiledMap;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.gravity.gameplay.GravityGameController;
 import com.gravity.physics.Entity;
 import com.gravity.physics.SpikeEntity;
 import com.gravity.physics.TileWorldEntity;
 
 public class TileWorld implements GameWorld {
     public final int height;
     public final int width;
     
     public final int tileHeight;
     public final int tileWidth;
     
     private List<Entity> entities;
     private Map<Shape, Entity> touchingBoxes;
     
     private TiledMap map;
     
     private static final int TILES_LAYER_ID = 0;
     private static final int PERSPECTIVE_LAYER_ID = 1;
     private static final int SPIKES_LAYER_ID = 2;
     
     public TileWorld(TiledMap map, GravityGameController controller) {
         // Get width/height
         this.tileWidth = map.getTileWidth();
         this.tileHeight = map.getTileHeight();
         this.width = map.getWidth() * tileWidth;
         this.height = map.getHeight() * tileHeight;
         
         this.map = map;
         
         touchingBoxes = Maps.newHashMap();
         Rectangle bound = new Rectangle(0, this.height - this.tileHeight, this.width, 1 * tileHeight);
         Entity bottom = new TileWorldEntity(bound);
         bound.grow(.1f, .1f);
         touchingBoxes.put(bound, bottom);
         entities = Lists.newArrayList(bottom);
         
         // Iterate over and find all tiles
         int layerId = TILES_LAYER_ID; // Layer ID to search at
         for (int i = 0; i < map.getWidth(); i++) {
             for (int j = 0; j < map.getHeight(); j++) {
                 int tileId = map.getTileId(i, j, layerId);
                 if (tileId != 0) {
                     // Tile exists at this spot
                     Rectangle r = new Rectangle(i * tileWidth, j * tileHeight, tileWidth, tileHeight);
                     Entity e = new TileWorldEntity(r);
                     
                     touchingBoxes.put(r, e);
                     entities.add(e);
                 }
             }
         }
         
        if (map.getLayerCount() > SPIKES_LAYER_ID) {
            layerId = SPIKES_LAYER_ID;
            for (int i = 0; i < map.getWidth(); i++) {
                for (int j = 0; j < map.getHeight(); j++) {
                    int tileId = map.getTileId(i, j, layerId);
                    if (tileId != 0) {
                        // Tile exists at this spot
                        Rectangle r = new Rectangle(i * tileWidth, j * tileHeight, tileWidth, tileHeight);
                        Entity e = new SpikeEntity(controller, r);
                        
                        touchingBoxes.put(r, e);
                        entities.add(e);
                    }
                 }
             }
         }
         
     }
     
     @Override
     public List<Entity> getCollisions(Shape shape) {
         List<Entity> collisions = Lists.newArrayList();
         for (Entity ent : entities) {
             if (shape.intersects(ent.getShape(0))) {
                 collisions.add(ent);
             }
         }
         return collisions;
     }
     
     @Override
     public List<Entity> getTouching(Shape shape) {
         List<Entity> touches = Lists.newArrayList();
         for (Shape terrain : touchingBoxes.keySet()) {
             if (shape.intersects(terrain)) {
                 touches.add(touchingBoxes.get(terrain));
             }
         }
         return touches;
     }
     
     @Override
     public int getHeight() {
         return height;
     }
     
     @Override
     public int getWidth() {
         return width;
     }
     
     @Override
     public List<Entity> getTerrainEntities() {
         return entities;
     }
     
     @Override
     public void render(Graphics g, int offsetX, int offsetY) {
         g.pushTransform();
         map.render(offsetX, offsetY);
         g.resetTransform();
         g.popTransform();
     }
 }
