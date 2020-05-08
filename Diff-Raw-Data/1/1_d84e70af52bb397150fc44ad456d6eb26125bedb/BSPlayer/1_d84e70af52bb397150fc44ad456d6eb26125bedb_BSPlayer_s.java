 package edu.smcm.gamedev.butterseal;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.assets.AssetManager;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.g2d.Animation;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.maps.MapLayer;
 import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
 import com.badlogic.gdx.math.Matrix4;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.math.Vector3;
 import com.badlogic.gdx.utils.Array;
 
 /**
  * Handles player state and movement on-screen.
  *
  * @author Sean
  *
  */
 public class BSPlayer {
     private static class BSAnimation {
         Animation animation;
         TextureAtlas spriteSheet;
         float time;
 
         public BSAnimation(String prefix) {
             this.spriteSheet = new TextureAtlas(BSAsset.PLAYER.assetPath);
 
             Array<Sprite> skeleton = spriteSheet.createSprites(prefix);
             this.animation = new Animation(0.25f, skeleton);
             this.time = 0f;
         }
 
         public BSAnimation(String prefix, int frames) {
             this.spriteSheet = new TextureAtlas(BSAsset.PLAYER.assetPath);
             Array<Sprite> skeleton = new Array<Sprite>();
             for(int i = 0; i < frames; i++) {
                 skeleton.add(new Sprite(spriteSheet.findRegion(String.format("%s/%04d", prefix, i))));
             }
             this.animation = new Animation(0.25f, skeleton);
             this.time = 0f;
         }
     }
 
     BSGameState state;
     static SpriteBatch batch;
     static AssetManager assets;
     static OrthographicCamera camera;
     BSAnimation walkUp, walkDown, walkRight, walkLeft, idle;
     Sprite currentFrame;
     /**
      * The pixels yet to move
      */
     Vector2 displacement;
     private static final float SCALE = 1f;
     /**
      * Frames to take per move
      */
     static final float DEFAULT_SPEED = 5f;
     float SPEED;
 
     public BSPlayer(BSGameState state) {
         walkUp    = new BSAnimation("up");
         walkDown  = new BSAnimation("down");
         walkRight = new BSAnimation("east");
         walkLeft  = new BSAnimation("west");
         idle      = new BSAnimation("idle");
 
         this.changeSprite(null);
         this.currentFrame.setOrigin(13/16f, 12/16f);
         this.currentFrame.setScale(SCALE / BSMap.PIXELS_PER_TILE);
         this.displacement = new Vector2();
         this.state = state;
         this.state.facing = BSDirection.NORTH;
         this.state.selectedPower = BSPower.ACTION;
         this.state.currentTile = new BSTile(0, 0);
         state.player = this;
         this.state.available_powers = new ArrayList<BSPower>();
         this.state.available_powers.add(BSPower.ACTION);
         SPEED = DEFAULT_SPEED;
     }
 
     /**
      * Draws the player on the screen.
      */
     public void draw() {
         Vector2 ret = this.doTranslate();
 
         // update moving state based on whether we have more to move
         changeSprite(state.isMoving ? state.facing : null);
         this.state.isMoving = displacement.x != 0 || displacement.y != 0;
         if(!this.state.isMoving && this.state.nextMap != null) {
             String oldkey = this.state.currentMap.key;
             this.state.currentMap = this.state.nextMap;
             this.state.nextMap = null;
             this.place(oldkey);
             this.state.currentMap.usePower(state);
         }
 
         this.currentFrame.draw(batch);
 
         camera.translate(ret);
     }
 
     private Vector2 doTranslate() {
         float ddx = 0, ddy = 0;
 
         if(displacement.x != 0) {
             ddx = Math.abs(displacement.x) < getNormalizedSpeed() ?
                     displacement.x : Math.signum(displacement.x) * getNormalizedSpeed();
         }
 
         if (displacement.y != 0) {
             ddy = Math.abs(displacement.y) < getNormalizedSpeed() ?
                     displacement.y : Math.signum(displacement.y) * getNormalizedSpeed();
         }
 
         displacement.sub(ddx, ddy);
         currentFrame.translate(ddx,ddy);
         return new Vector2(ddx, ddy);
         // TODO create cam_displacement vector2 and keep it integer-based
         // and keep track of, when we come up against the edge of the map
     }
 
     /**
      * Moves in the given direction.
      *
      * If this is not the facing direction,
      *   then the player will turn into that direction, updating
      *   the {@link #state} and {@link #facingTile} appropriately.
      * Otherwise, it's a very simple move.
      *
      * @param direction the direction in which to move
      */
     public void move(BSDirection direction) {
         if(direction == null) {
             return;
         }
         if(ButterSeal.ANDROID_MODE) {
             switch(state.currentMap) {
             case HOUSE:
                 break;
             case ICE_CAVE:
                 setSpeedScale(3);
                 break;
             case ICE_CAVE_ENTRY:
             case ICE_CAVE_EXIT:
                 setSpeedScale(2);
                 break;
             case MAZE:
                 break;
             default:
                 break;
             }
         }
         this.state.facing = direction;
         changeSprite(state.facing);
         if(!(!state.hasbeentouching || canMove(direction))) {
             // check to see if we need to move maps
             HashMap<String,String> props = this.state.currentTile.getProperties(this.state.currentMap).get("player");
             if (props.containsKey("player")) {
                 this.state.nextMap = BSMap.getByKey(props.get("player"));
                 if(state.world.isRoute(state.currentMap, this.state.nextMap)) {
                     this.state.justchangedmaps = true;
                     this.state.currentMap.reset(state);
                 } else {
                     this.state.nextMap = null;
                 }
             }
             return;
         }
         if(!canMove(direction)) {
             return;
         }
         this.state.justchangedmaps = false;
         this.state.hasbeentouching = false;
         this.state.isMoving = true;
 
         // check to see if we need to move maps
         HashMap<String,String> props = this.getFacingTile().getProperties(this.state.currentMap).get("player");
         if (props.containsKey("player")) {
             this.state.nextMap = BSMap.getByKey(props.get("player"));
             if(state.world.isRoute(state.currentMap, this.state.nextMap)) {
                 this.state.currentMap.reset(state);
             } else {
                 this.state.nextMap = null;
             }
         }
 
         if(this.state.nextMap!=null){
             this.state.nextMap.load(this.state);
         }
 
         if(direction != state.facing) {
             if(ButterSeal.DEBUG > 0) {
                 System.out.println("Moving " + direction);
             }
         }
         switch(direction) {
         case NORTH:
             state.currentTile.y += 1;
             displacement.y += BSMap.PIXELS_PER_TILE * currentFrame.getScaleY() / SCALE;
             break;
         case SOUTH:
             state.currentTile.y -= 1;
             displacement.y -= BSMap.PIXELS_PER_TILE * currentFrame.getScaleY() / SCALE;
             break;
         case EAST:
             state.currentTile.x += 1;
             displacement.x += BSMap.PIXELS_PER_TILE * currentFrame.getScaleX() / SCALE;
             break;
         case WEST:
             state.currentTile.x -= 1;
             displacement.x -= BSMap.PIXELS_PER_TILE * currentFrame.getScaleX() / SCALE;
             break;
         }
     }
 
     private void changeSprite(BSDirection direction) {
         BSAnimation target = idle;
         if(direction != null) {
             switch(direction) {
             case EAST:
                 target = walkRight;
                 break;
             case NORTH:
                 target = walkUp;
                 break;
             case SOUTH:
                 target = walkDown;
                 break;
             case WEST:
                 target = walkLeft;
                 break;
             }
         }
         target.time += Gdx.graphics.getDeltaTime();
         TextureRegion new_texture = target.animation.getKeyFrame(target.time, true);
         if(this.currentFrame == null) {
             currentFrame = new Sprite(new_texture);
         } else {
             this.currentFrame.setRegion(new_texture);
         }
     }
 
     private boolean canMove(BSDirection direction) {
         // If we are already moving,
         //   we should not be able to move again until we finish.
         if(state.isMoving) {
             return false;
         }
 
         BSTile adj = getAdjacentTile(direction);
         if(!adj.isContainedIn(state.currentMap.playerLevel)) {
             return false;
         }
 
         for (MapLayer t : state.currentMap.map.getLayers()) {
             if (adj.hasProperty(state.currentMap, (TiledMapTileLayer)t, "wall", "true")) {
                 return false;
             }
         }
 
         return true;
     }
 
     /**
      *
      * @return a tile describing the one we're facing
      */
     public BSTile getFacingTile() {
         return this.getAdjacentTile(state.facing);
     }
 
     public BSTile getAdjacentTile(BSDirection direction) {
         BSTile adj = new BSTile(state.currentTile);
         switch(direction) {
         case NORTH:
             adj.y += 1;
             break;
         case SOUTH:
             adj.y -= 1;
             break;
         case EAST:
             adj.x += 1;
             break;
         case WEST:
             adj.x -= 1;
             break;
         default:
             break;
         }
         return adj;
     }
 
     public void setPower(int i) {
         int l = this.state.available_powers.size();
         int o = this.state.available_powers.indexOf(this.state.selectedPower);
         int current = o + l;
         int next = (current + i) % l;
         this.setPower(this.state.available_powers.get(next));
     }
 
 
     public void setPower(BSPower power) {
         if(this.state.selectedPower != power) {
             this.state.isSelectingPower = true;
             if(ButterSeal.DEBUG > 2) {
                 System.out.println("Setting power to " + power);
             }
             this.state.selectedPower = power;
         }
         this.state.isUsingPower = false;
     }
 
     public void usePower() {
         if(!state.isUsingPower) {
             if(ButterSeal.DEBUG > 2) {
                 System.out.println("Using power " + this.state.selectedPower);
             }
             this.state.isUsingPower = true;
             this.state.currentMap.usePower(this.state);
         }
         this.state.isSelectingPower = false;
     }
 
     public Vector2 getV2() {
         return new Vector2(currentFrame.getX(), currentFrame.getY());
     }
 
     /**
      * Places the player on a tile in the current map where player=map.key
      */
     public void place(String oldkey) {
         state.currentTile = state.currentMap.getPlayer(oldkey);
         place(state.currentTile.x, state.currentTile.y);
     }
 
     /**
      * Place the player centered on a specific tile.
      * @param position
      */
     public void place(Vector2 position) {
         this.place(position.x, position.y);
     }
 
     public void place(float x, float y) {
         // TODO make this less trial-and-error-y
         camera.position.set(x, y, 1);
         //Matrix4 projection = new Matrix4(camera.combined);
         // normalize to bottom-left corner
         x -= .8f;
         y -= .75f;
         // TODO have this not be <em> awful </em>
 
         // TODO center on tile
 
         this.currentFrame.setPosition(x, y);
     }
 
     public void printwhere(Matrix4 projection) {
 
         System.out.printf("%.1f:%.1f%n", currentFrame.getHeight() * SCALE, currentFrame.getWidth() * SCALE);
         System.out.println(projection.getTranslation(new Vector3(currentFrame.getX(), currentFrame.getY(), 0)));
 
     }
 
     public float getNormalizedSpeed() {
         return SPEED / BSMap.PIXELS_PER_TILE;
     }
 
     public void setSpeedScale(float scale) {
         SPEED = BSPlayer.DEFAULT_SPEED * scale;
     }
 }
 
 // Local Variables:
 // indent-tabs-mode: nil
 // End:
