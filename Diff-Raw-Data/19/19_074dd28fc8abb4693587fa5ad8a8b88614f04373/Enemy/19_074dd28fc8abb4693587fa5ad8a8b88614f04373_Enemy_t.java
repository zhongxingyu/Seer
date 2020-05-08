 package jam.ld24.entities;
 
 import infinitedog.frisky.entities.Entity;
 import infinitedog.frisky.entities.Sprite;
 import jam.ld24.game.C;
 import jam.ld24.tiles.CollisionMap;
 import jam.ld24.tiles.TileSet;
 import java.util.Random;
 import org.lwjgl.opengl.GL11;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.ShapeFill;
 import org.newdawn.slick.geom.Polygon;
 import org.newdawn.slick.geom.Shape;
 import org.newdawn.slick.geom.Vector2f;
 
 /**
  *
  * @author InfiniteDog
  */
 public class Enemy extends Entity {
     private static final int IDLE = 0;
     private static final int MOVING = 1;
     private static final int CHANGE_DIRECTION = 2;
     
     private static int id = 0;
     private boolean alive;
     private boolean active;
     private Polygon vision = new Polygon();
     private Vector2f direction;
     private CollisionMap cm;
     protected float speed = (Float) C.Logic.ENEMY_SPEED.data;
     protected int thinkTime = (Integer) C.Logic.ENEMY_THINK_TIME.data;
     private int state = IDLE;
     private int frame;
     private TileSet tileSet = new TileSet(C.Textures.TILE_SET.name, 
         (Integer) C.Logic.TILE_SIZE.data);
         
     public Enemy() {
         name =  C.Entities.ENEMY.name + id++;
         group = C.Groups.ENEMIES.name;
         alive = true;
         setDirection(new Vector2f(0, 1));
         setWidth((Integer) C.Logic.TILE_SIZE.data);
         setHeight((Integer) C.Logic.TILE_SIZE.data);
     }
     
     public Enemy(int x, int y) {
         this();
         this.setPosition(new Vector2f(x * (Integer) C.Logic.TILE_SIZE.data,
                 y * (Integer) C.Logic.TILE_SIZE.data));
     }
 
     public boolean isAlive() {
         return alive;
     }
 
     public void setAlive(boolean alive) {
         this.alive = alive;
     }
 
     public boolean isActive() {
         return active;
     }
 
     public void setActive(boolean active) {
         this.active = active;
     }
 
     void die() {
         setAlive(false);
     }
  
     @Override
     public void render(GameContainer gc, Graphics g) {
         g.setColor(Color.red);
         //g.drawString(name, posx, posy);
         g.draw(vision);
         
         tileSet.render(frame, getX(), getY());
     }
     
     @Override
     public void update(GameContainer gc, int delta) {
         super.update(gc, delta);
                 
         Random random = new Random();
         int behavior = random.nextInt(10) + 1;
             
         thinkTime -= delta;
         
         switch(state) {
             case IDLE:
                 break;
             case MOVING:
                 float x = getX();
                 float y = getY();
                 float vx = 0;
                 float vy = 0;
 
                 if(direction.y == 0) { // looking right or left
                     vx = direction.x * speed * delta;
                 }
                 else { // looking up or down
                     vy = direction.y * speed * delta;
                 }
 
                 if(!this.cm.collidesWith(this, vx, vy)) {
                     x += vx;
                     y += vy;
                 }
                 else {
                     state = CHANGE_DIRECTION;
                 }
 
                 setPosition(new Vector2f(x, y));
                 break;
             case CHANGE_DIRECTION:
                 if(behavior < 2) {
                     setDirection(new Vector2f(0, 1));
                 }
                 else if(behavior < 4) {
                     setDirection(new Vector2f(0, -1));
                 }
                 else if(behavior < 6) {
                     setDirection(new Vector2f(1, 0));
                 }
                 else {
                     setDirection(new Vector2f(-1, 0));
                 }
                 state = MOVING;
                 break;
         }
         
         if(thinkTime <= 0) {
             thinkTime = (Integer) C.Logic.ENEMY_THINK_TIME.data;
             if(behavior < 4) {
                 state = MOVING;
             }
             else if(behavior < 6) {
                 state = CHANGE_DIRECTION;
             }
             else {
                 state = IDLE;
             }
         }
     }
     
     public void setPosition(Vector2f position) {
         super.setPosition(position);
         
         //Update vision
         float posx = getCenter().x;
         float posy = getCenter().y;
         
         vision = new Polygon();
         
         if(direction.y == 0) {
             vision.addPoint(posx + 100 * direction.x, posy - 75);
             vision.addPoint(posx + 100 * direction.x, posy + 75);
             posx += direction.x * (Integer) C.Logic.TILE_SIZE.data / 2;
         }
         else {
             vision.addPoint(posx + 75, posy + 100 * direction.y);
             vision.addPoint(posx - 75, posy + 100 * direction.y);
             posy += direction.y * (Integer) C.Logic.TILE_SIZE.data / 2;
         }
         
         vision.addPoint(posx, posy);
     }
     
     public Polygon getVision() {
         return vision;
     }
     
     public void setCollisionMap(CollisionMap collisionMap) {
         this.cm = collisionMap;
     }
 
     public void setDirection(Vector2f direction) {
         this.direction = direction;
         if(direction.x == 0) {
             if(direction.y == 1) frame = 28; else frame = 26;
         }
         else {
             if(direction.x == 1) frame = 27; else frame = 25;
         }
     }
 }
