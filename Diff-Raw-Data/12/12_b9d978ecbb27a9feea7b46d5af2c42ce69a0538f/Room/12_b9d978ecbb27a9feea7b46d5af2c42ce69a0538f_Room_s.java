 package JGame.Room;
 
 import JGame.Game.Game;
 import JGame.GameObject.GameObject;
 import JGame.Util.CollisionMap;
 import JGame.Util.CustomEventMap;
 import JGame.Util.KeyboardMap;
 import JGame.Util.Mapping;
 import JGame.Util.MouseMap;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.Rectangle;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Map;
 import javax.swing.ImageIcon;
 import javax.swing.JPanel;
 
 public class Room extends JPanel implements Runnable{
 
     protected Image bg;
     ArrayList<GameObject> gameObjects = new ArrayList<>();
     protected int width = Game.width, height = Game.height;
     public static int CENTERX = 0, CENTERY = 0;
     private Image dbImage;
     private Graphics dbg;
 
     /*
      * This runs the main thread for the current room.
      *
      * run() tests for key presses and Mouse Events, it then runs its set method
      * if a key press or mouse click is activated.
      */
     @Override
     public void run(){
         long lastLoopTime = System.nanoTime();
         final int TARGET_FPS = 60;
         final long OPTIMAL_TIME = 1000000000 / TARGET_FPS;
         long lastFpsTime = 0;
         while(true){
             long now = System.nanoTime();
             long updateLength = now - lastLoopTime;
             lastLoopTime = now;
             double delta = updateLength / ((double)OPTIMAL_TIME);
 
             lastFpsTime += updateLength;
             if(lastFpsTime >= 1000000000){
                 lastFpsTime = 0;
             }
 
 
             // Check for key press events
             this.keyboardEventTests();
             // Check for mouse events
             this.mouseEventTests();
             // Check for collision events
             this.collisionEventTests();
             // Check for custom events
             this.customEventTests();
             // Redraw all game objects
             for(GameObject go : gameObjects){
                 // Move objects along X/Y axis
                 this.moveXY(go, delta);
                 // Test object for screen leaveable
                 this.screenLeaveCheck(go);
             }
             // Remove all game objects that have been marked to be destroyed
             this.removeMarked();
             try{
                 Thread.sleep((lastLoopTime - System.nanoTime() + OPTIMAL_TIME) / 1000000);
             }catch(Exception e){
             }
         }
     }
 
     /**
      * Tests for keyboard events and performs action if one is found
      */
     protected void keyboardEventTests(){
         for(Map.Entry ap : KeyboardMap.map.entrySet()){
             Mapping mp = (Mapping)ap.getValue();
             if(mp.doing){
                 mp.run();
             }
         }
     }
 
     /**
      * Tests for mouse events and preforms action if one is found
      */
     protected void mouseEventTests(){
         for(Map.Entry ap : MouseMap.map.entrySet()){
             Mapping mp = (Mapping)ap.getValue();
             if(mp.doing){
                 mp.run();
             }
         }
     }
 
     /**
      * Tests for collision events and performs action if one is found
      */
     public void collisionEventTests(){
         for(GameObject go : this.gameObjects){
             Rectangle rect1 = new Rectangle(
                     go.getX(),
                     go.getY(),
                     go.getWidth(),
                     go.getHeight());
             for(GameObject go2 : this.gameObjects){
                 Rectangle rect2 = new Rectangle(
                         go2.getX(),
                         go2.getY(),
                         go2.getWidth(),
                         go2.getHeight());
                 String ref = go2.getReference();
                 if(rect1.intersects(rect2)){
                     for(Map.Entry ap : go.collide.getMap().entrySet()){
                         String key = ap.getKey().toString();
                         if(key.equals(ref)){
                             Mapping mp = (Mapping)ap.getValue();
                            go.setColliededWith(go2);
                             mp.run();
                         }
                     }
                 }
             }
 
         }
     }
 
     /**
      * Tests for custom events and performs action if one is found
      */
     protected void customEventTests(){
         for(Map.Entry ap : CustomEventMap.map.entrySet()){
             Mapping mp = (Mapping)ap.getValue();
             if(mp.doing){
                 mp.run();
                 CustomEventMap.map.remove(ap.getKey().toString());
             }
         }
     }
 
     protected void moveXY(GameObject go, double delta){
         // Move an object along the Y axis
         if(go.needsToMoveY){
             int objY = go.getY();
             if(objY > go.moveEndY && go.move.getMoveDirection().equals("UP")){
                 go.setY((int)(objY - go.moveAmountY * delta));
             }else if(objY < go.moveEndY && go.move.getMoveDirection().equals("DOWN")){
                 go.setY((int)(objY + go.moveAmountY * delta));
             }else{
                 go.needsToMoveY = false;
                 go.move.actionComplete();
             }
         }
         // Move an object along the X axis
         if(go.needsToMoveX){
             int objX = go.getX();
             if(objX > go.moveEndX && go.move.getMoveDirection().equals("LEFT")){
                 go.setX((int)(objX - go.moveAmountX * delta));
             }else if(objX < go.moveEndX && go.move.getMoveDirection().equals("RIGHT")){
                 go.setX((int)(objX + go.moveAmountX * delta));
             }else{
                 go.needsToMoveX = false;
                 go.move.actionComplete();
             }
         }
     }
 
     protected void screenLeaveCheck(GameObject go){
         if(!go.getLeaveScreen()){
             int goWidth = go.getWidth();
             int goHeight = go.getHeight();
             int goX = go.getX();
             int goY = go.getY();
             int gameWidth = Game.width;
             int gameHeight = Game.height;
             if(goX + goWidth >= gameWidth){
                 go.setX(gameWidth - goWidth);
             }
             if(goX <= 0){
                 go.setX(0);
             }
             if(goY + goHeight >= gameHeight){
                 go.setY(gameHeight - goHeight);
             }
             if(goY <= 0){
                 go.setY(0);
             }
         }
     }
 
     public Room(){
         Thread thread = new Thread(this);
         thread.start();
     }
 
     public void removeMarked(){
         Iterator objs = gameObjects.iterator();
         while(objs.hasNext()){
             GameObject go = (GameObject)objs.next();
             if(go.markedForDeletion){
                 objs.remove();
             }
         }
     }
 
     /**
      *
      * @param width
      * @param height
      *
      * Sets the size of a room.
      */
     public void setRoomSize(int width, int height){
         this.width = width;
         this.height = height;
 
         Room.CENTERX = (int)(this.width / 2.0);
         Room.CENTERY = (int)(this.height / 2.0);
     }
 
     /**
      *
      * @param color
      *
      * Sets a color to be the background a room
      */
     @Override
     public void setBackground(Color color){
         super.setBackground(color);
     }
 
     /**
      *
      * @param filename
      *
      * Sets an image to be the background of a room
      */
     public void setBackground(String filename){
         URL loc = this.getClass().getResource(filename);
         ImageIcon imgIcon = new ImageIcon(loc);
         bg = imgIcon.getImage();
     }
 
     /**
      *
      * @param gameObject
      * @param x
      * @param y
      *
      * Adds a GameObject to the room at a particular location
      */
     public void addGameObjectAt(GameObject gameObject, int x, int y){
         gameObject.setX(x);
         gameObject.setY(y);
         gameObjects.add(gameObject);
     }
 
     /**
      *
      * @param gameObject
      */
     public void addGameObject(GameObject gameObject){
         gameObjects.add(gameObject);
     }
 
     /**
      *
      * @param go
      *
      * Removes a known GameObject from the room
      */
     public void removeGameObject(GameObject go){
         gameObjects.remove(go);
         //System.gc();
     }
 
     @Override
     public void paint(Graphics g){
         this.dbImage = createImage(this.getWidth(), this.getHeight());
         this.dbg = dbImage.getGraphics();
         this.paintComponent(dbg);
         g.drawImage(this.dbImage, 0, 0, this);
     }
 
     /**
      *
      * @param g
      *
      * Draws out all of the GameObjects in the room
      */
     @Override
     public void paintComponent(Graphics g){
         try{
             g.drawImage(bg, 0, 0, this);
             for(int i = 0; i < gameObjects.size(); i++){
                 GameObject go = gameObjects.get(i);
                 g.drawImage(go.getSprite(), go.getX(), go.getY(), this);
             }
             this.repaint();
         }catch(Exception e){
         }
     }
 
     /**
      *
      * @return
      *
      * Gets the rooms width
      */
     @Override
     public int getWidth(){
         return this.width;
     }
 
     /**
      *
      * @return
      *
      * Gets the rooms height
      */
     @Override
     public int getHeight(){
         return this.height;
     }
 }
