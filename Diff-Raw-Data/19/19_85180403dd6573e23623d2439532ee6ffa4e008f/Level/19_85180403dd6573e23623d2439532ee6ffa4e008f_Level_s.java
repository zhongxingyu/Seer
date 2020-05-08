 package com.macbury.unamed.level;
 
 import java.util.ArrayList;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Point;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.geom.Vector2f;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.util.Log;
 
 import com.macbury.unamed.BresenhamLine;
 import com.macbury.unamed.Core;
 import com.macbury.unamed.component.CharacterAnimation;
 import com.macbury.unamed.component.HitBox;
 import com.macbury.unamed.component.Monster;
 import com.macbury.unamed.component.TileBasedMovement;
 import com.macbury.unamed.entity.Entity;
 import com.macbury.unamed.entity.Player;
 import com.macbury.unamed.monkey.GroupObject;
 import com.macbury.unamed.monkey.ObjectGroup;
 import com.macbury.unamed.monkey.TiledMapPlus;
 
 public class Level {
   public final static int LAYER_BACKGROUND    = 0;
   private static final String LAYER_EVENTS    = "Events";
   private static final String LAYER_COLLIDERS = "Colliders";
   private static final float FULL_CIRCLE_IN_RADIANTS     = 6.28f;
  private static final float FOG_OF_WAR_STEP_IN_RADIANTS = 0.08f;
  private static final int   FOG_OF_WAR_MAX_SKIP_ANGLES  = Math.round(FULL_CIRCLE_IN_RADIANTS/FOG_OF_WAR_STEP_IN_RADIANTS);
   Block[][] world;
   
   int stateID                = -1;
   private Rectangle viewPort = null;
   
   public int tileWidth           = Core.TILE_SIZE;
   public int tileHeight          = Core.TILE_SIZE;
   public int mapTileWidth        = 100;
   public int mapTileHeight       = 100;
   
   int tileCountHorizontal        = 5;
   int tileCountVertical          = 5;
   
   TiledMapPlus  map;
   Entity        cameraTarget;
   Entity        player;
   private ArrayList<Entity> entities;
   private ArrayList<Block>  lastVisibleBlocks;
   
   public Level() {
     this.entities = new ArrayList<Entity>();
     this.lastVisibleBlocks = new ArrayList<Block>();
   }
   
   public void addEntity(Entity e) {
     if (this.entities.indexOf(e) == -1) {
       e.setLevel(this);
       this.entities.add(e);
     } else {
       Log.info("Entity already added to entity stack!");
     }
   }
   
   public void render(GameContainer gc, StateBasedGame sb, Graphics gr) throws SlickException {
     updateCamera();
     gr.setAntiAlias(false);
     
     int shiftTileX = getShiftTileX();
     int shiftTileY = getShiftTileY();
     
     int shiftXRound      = Math.round(this.getShiftX());
     int shiftYRound      = Math.round(this.getShiftY());
 
     
     int viewportTileOffsetX = (( shiftTileX * this.tileWidth ) - shiftXRound) - this.tileWidth;
     int viewportTileOffsetY = (( shiftTileY * this.tileHeight ) - shiftYRound) - this.tileHeight;
     
     
     renderMapLayer(gr, viewportTileOffsetX, viewportTileOffsetY, shiftTileX, shiftTileY, LAYER_BACKGROUND);
     
     // rendering layer entities
     int shiftX = -shiftXRound - this.tileWidth;
     int shiftY = -shiftYRound - this.tileHeight;
     gr.pushTransform();
     gr.translate(shiftX, shiftY);
     for(Entity e : this.entities) {
       /*if (getViewPort().intersects(e.getRect())) {
         e.render(gc, sb, gr);
       }*/
       Block block = this.world[e.getTileX()][e.getTileY()];
       if (block.isVisible()) {
         e.render(gc, sb, gr);
       }
     }
     gr.popTransform();
     
     gr.pushTransform();
     gr.translate(viewportTileOffsetX, viewportTileOffsetY);
     
     Color invisivbleColor = new Color(0,0,0);
    Color visitedColor    = new Color(0,0,0,210);
     for(int x = 0; x < this.tileCountHorizontal; x++) {
       for(int y = 0; y < this.tileCountVertical; y++) {
         int tx = cordToBound(shiftTileX+x, this.mapTileWidth);
         int ty = cordToBound(shiftTileY+y, this.mapTileHeight);
         
         Block block = this.world[tx][ty];
         if (block.isVisible()) {
           gr.setColor(new Color(0,0,0,block.lightPower));
         } else {
           if (block.haveBeenVisited()) {
             gr.setColor(visitedColor);
           } else {
             gr.setColor(invisivbleColor);
           }
         }
         
         gr.fillRect(x*this.tileWidth, y*this.tileHeight, this.tileWidth, this.tileHeight);
         
       }
     }
     gr.popTransform();
   }
   
   private int cordToBound(int x, int max) {
     if (x < 0) {
       return 0;
     } else {
       if (x > max) {
         return max;
       } else {
         return x;
       }
     }
   }
   
   private void renderMapLayer(Graphics gr, int viewportTileOffsetX, int viewportTileOffsetY, int shiftTileX, int shiftTileY, int layer) {
     if(map != null) {
       map.render(viewportTileOffsetX,viewportTileOffsetY, shiftTileX, shiftTileY, this.tileCountHorizontal, this.tileCountVertical, layer, false);
     }
   }
 
   public void update(GameContainer gc, StateBasedGame sb, int delta) throws SlickException {
     for(Entity e : this.entities) {
       e.update(gc, sb, delta);
     }
     
     updateFogOfWarAndLightingFor(player);
   }
 
   private void updateFogOfWarAndLightingFor(Entity entity) {
     int cx = entity.getTileX();
     int cy = entity.getTileY();
     
     Block block = null;
     
     int radius             = 0;
     int i                  = 0;
     float radiants         = 0;
     boolean[] skipRadiants = new boolean[FOG_OF_WAR_MAX_SKIP_ANGLES+1];
     
     for (int j = 0; j < lastVisibleBlocks.size(); j++) {
       Block b = lastVisibleBlocks.get(j);
       b.markAsInvisible();
     }
     lastVisibleBlocks.clear();
     
     while(radius < Player.FOG_OF_WAR_RADIUS) {
      float lightPower = Math.round((float)(radius) / (float)entity.lightPower * 255.0f);
       radiants = 0;
       i        = 0;
       
       while(radiants <= FULL_CIRCLE_IN_RADIANTS) {
         if (!skipRadiants[i]) {
           int x = (int)Math.round(cx + radius * Math.cos(radiants));
           int y = (int)Math.round(cy + radius * Math.sin(radiants));
           block = this.world[x][y];
           block.lightPower = (int)lightPower;
           
           block.markAsVisible();
           if (block.solid) {
             skipRadiants[i] = true;
           }
           
           lastVisibleBlocks.add(block);
         }
         i++;
         radiants += FOG_OF_WAR_STEP_IN_RADIANTS;
       }
       
       radius++;
     }
       
   }
 
   public void setupViewport(GameContainer gc) {
     if (getViewPort() == null) {
       this.viewPort = new Rectangle(0, 0, gc.getWidth()+this.tileWidth, gc.getHeight()+this.tileHeight);
       Log.info("Viewport size is: " + this.viewPort.getWidth() + "x" + this.viewPort.getHeight() );
     }
   }
 
   public void updateCamera() {
     if (cameraTarget != null) {
       this.viewPort.setCenterX(cameraTarget.getRect().getCenterX());
       this.viewPort.setCenterY(cameraTarget.getRect().getCenterY());
       //Log.debug("Viewport pos is: " + this.viewPort.getX() + "x" + this.viewPort.getY() );
     }
   }
   
   public void lookAt(Entity e) {
     cameraTarget = e;
     Log.info("Setting camera to look at entity: " + this.cameraTarget.getId());
   }
   
   public int getShiftTileX() {
     return Math.round(getShiftX() / this.tileWidth);
   }
   
   public int getShiftTileY() {
     return Math.round(getShiftY() / this.tileHeight);
   }
   
   public float getShiftX() {
     return this.viewPort.getX();
   }
   
   public float getShiftY() {
     return this.viewPort.getY();
   }
   
   public Rectangle getViewPort() {
     return viewPort;
   }
   
   public void loadMap(String name) throws SlickException {
     String filePath = "res/maps/"+name+".tmx";
     this.map        = new TiledMapPlus(filePath);
     Log.info("Loading map from: "+ filePath);
     
     this.tileHeight = this.map.getTileHeight();
     this.tileWidth  = this.map.getTileWidth();
     
     Log.info("Tile size is: "+ this.tileWidth + "x" + this.tileHeight);
     
     this.tileCountHorizontal =  Math.round((this.viewPort.getWidth() / this.tileWidth) + 2);
     this.tileCountVertical   =  Math.round((this.viewPort.getHeight() / this.tileHeight) + 2);
     
     Log.info("Tile count is: "+ this.tileCountHorizontal + "x" + this.tileCountVertical);
     
     ObjectGroup colidersGroup = this.map.getObjectGroup(LAYER_COLLIDERS);
     loadEvents();
     loadColliders();
     
     Log.info("Total entities: "+this.entities.size());
   }
   
   public int transformXToTile(float x) {
     return Math.round(x / this.tileWidth);
   }
   
   public int transformYToTile(float y) {
     return Math.round(y / this.tileHeight);
   }
   
   public void buildBlockForGroupObject(GroupObject object) {
     Log.info("Transforming group object: " + object.x + "x" + object.y + " size: " + object.width + "x" + object.height );
     int startX = this.transformXToTile(object.x);
     int startY = this.transformYToTile(object.y);
     
     int width  = startX + this.transformXToTile(object.width);
     int height = startY + this.transformXToTile(object.height);
     
     for (int x = startX; x < width; x++) {
       for (int y = startY; y < height; y++) {
         Log.debug("Building block: "+ x + "x" +y);
         this.world[x][y].solid = true;
       }
     }
   }
   
   private void loadColliders() throws SlickException {
     ObjectGroup collidersGroup = this.map.getObjectGroup(LAYER_COLLIDERS);
     fillWorldWithBlocks();
     
     if(collidersGroup == null) {
       throw new SlickException("There is no colliders layer in the map!");
     } else {
       Log.info("Colliders: " +Integer.toString(collidersGroup.getObjects().size()));
 
       for (GroupObject object : collidersGroup.objects) {
         buildBlockForGroupObject(object);
       }
     }
   }
   
   private void fillWorldWithBlocks() {
     this.mapTileWidth  = this.map.getWidth();
     this.mapTileHeight = this.map.getHeight();
     this.world = new Block[mapTileWidth][mapTileHeight];
     Log.info("Initializing world size: "+this.map.getWidth() + "x" + this.map.getHeight());
     
     for (int x = 0; x < this.mapTileWidth; x++) {
       for (int y = 0; y < this.mapTileHeight; y++) {
         Block block      = new Block();
         block.visited    = false;
         this.world[x][y] = block;
         
         Log.debug("Building block: "+ x + "x" +y + " with id: "+ block.id);
       }
     }
   }
 
   public boolean canMoveTo(Vector2f targetPosition, Entity mover) {
     return canMoveTo(new Rectangle(targetPosition.x, targetPosition.y, this.tileWidth, this.tileHeight), mover);
   }
   
   public boolean canMoveTo(Rectangle targetRect, Entity mover) {
     int tx = cordToBound(this.transformXToTile(targetRect.getCenterX()), this.mapTileWidth);
     int ty = cordToBound(this.transformYToTile(targetRect.getCenterY()), this.mapTileHeight);
     
     Block targetBlock = this.world[tx][ty];
     if (targetBlock != null && targetBlock.solid) {
       return false;
     }
     
     for (Entity entity : this.entities) {
       if (entity != mover && entity.solid) {
         Rectangle futureRect = entity.getFutureRect();
         if (entity.getRect().contains(targetRect.getCenterX(), targetRect.getCenterY())) {
           return false;
         }
         
         if (futureRect != null && futureRect.contains(targetRect.getCenterX(), targetRect.getCenterY())) {
           return false;
         }
       }
     }
     
     return true;
   }
   
   private void loadEvents() throws SlickException {
     ObjectGroup eventsGroup = this.map.getObjectGroup(LAYER_EVENTS);
     if (eventsGroup == null) {
       throw new SlickException("There is no events layer in the map!");
     } else {
       loadPlayerAndSpawn(eventsGroup);
       loadMonstersAndSpawn(eventsGroup);
     }
   }
 
   private void loadMonstersAndSpawn(ObjectGroup eventsGroup) throws SlickException {
     for (GroupObject spawnPosition : eventsGroup.getObjectsOfType("MonsterSpawn")) {
       Entity e = new Entity("Monster");
       this.addEntity(e);
       
       e.addComponent(new TileBasedMovement());
       CharacterAnimation animation = new CharacterAnimation();
       e.addComponent(animation);
       e.solid = true;
       animation.loadCharacterImage("monster");
       e.addComponent(new Monster());
       e.setPositionUsing(spawnPosition);
     }
   }
 
   private void loadPlayerAndSpawn(ObjectGroup eventsGroup) throws SlickException {
     GroupObject spawnPosition = eventsGroup.getObject("PlayerSpawn");
     player = new Player("Player 1");
     this.addEntity(player);
     
     lookAt(player);
     player.setPositionUsing(spawnPosition);
     Log.info("Player spawn position is "+ spawnPosition.x + "x" + spawnPosition.y);
   }
 }
