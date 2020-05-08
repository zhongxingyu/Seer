 package rekkyn.javagame;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.GameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 public class Entity {
 
     public float x;
     public float y;
    public static int width;
    public static int height;
     public float motionX;
     public float motionY;
     public float prevMotionX;
     public float prevMotionY;
     public boolean removed;
     Input input;
     public boolean onEdgeX;
     public boolean onEdgeY;
 
     public Entity(float x, float y) {
         this.x = x;
         this.y = y;
     }
 
     public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {}
 
     public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
         input = container.getInput();
         GameState state = game.getCurrentState();
         if (!(state instanceof IWorld)) return;
         IWorld world = (IWorld) game.getCurrentState();
         prevMotionX = motionX;
         prevMotionY = motionY;
         x += motionX;
         y += motionY;
         
         onEdgeX = false;
         onEdgeY = false;
         if (x < 0) {
             x = 0;
             motionX = 0;
             onEdgeX = true;
         }
         if (y < 0) {
             y = 0;
             motionY = 0;
             onEdgeY = true;
         }
         if (x > Game.width - width) {
             x = Game.width - width;
             motionX = 0;
             onEdgeX = true;
         }
         if (y > Game.height - height) {
             y = Game.height - height;
             motionY = 0;
             onEdgeY = true;
         }
         
         for (int i = 0; i < world.entities.size(); i++) {
             Entity e = world.entities.get(i);
             if (e != this && intersects(e)) {
                 float xOverlap = 0;
                 float yOverlap = 0;
                 if (e.x + e.width > x && x + width > e.x + e.width) {
                     xOverlap = e.x + e.width - x;
                 }
                 if (x + width > e.x && x < e.x) {
                     xOverlap = e.x - (x + width);
                 }
                 
                 if (e.y + e.height > y && y + height > e.y + e.height) {
                     yOverlap = e.y +e.height - y;
                 }
                 if (y + height > e.y && y < e.y) {
                     yOverlap = e.y - (y + height);
                 }
                 if (Math.abs(xOverlap) < Math.abs(yOverlap)) {
                    x += xOverlap;
                     if (e.onEdgeX) {
                        System.out.println(motionX);
                        motionX = -motionX * 0.8F;
                     } else {
                         e.motionX = prevMotionX * 0.8F;
                     motionX = -e.prevMotionX * 0.8F;
                     }
                 } else {
                    y += yOverlap;
                     if (e.onEdgeY) {
                         motionY = -prevMotionY * 0.8F;
                     } else {
                         e.motionY = prevMotionY * 0.8F;
                     motionY = -e.prevMotionY * 0.8F;
                     }
                 }
             }
         }
     }
 
     public void remove() {
         removed = true;
     }
     
     public boolean intersects(Entity e) {
         int w1 = width;
         int w2 = e.width;
         int h1 = height;
         int h2 = e.height;
         
         w1 += x;
         w2 += e.x;
         if (e.x > w1 || x > w2) return false;
         h2 += e.y;
         h1 += y;
         if (e.y > h1 || y > h2) return false;
         return true;
 
     }
 
 }
