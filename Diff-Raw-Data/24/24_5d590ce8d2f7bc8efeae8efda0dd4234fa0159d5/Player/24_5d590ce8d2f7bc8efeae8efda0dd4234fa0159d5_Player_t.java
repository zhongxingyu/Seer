 import java.awt.image.BufferedImage;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 public class Player
 {
     private ConcurrentHashMap<String, Animation> animations;
 
     public int x = 100;
     public int y = 100;
 
     public int moveX = 2;
     public int moveY = 2;
 
     public int realX;
     public int realY;
 
     public boolean shoot;
 
     public Player()
     {
         animations = new ConcurrentHashMap<String, Animation>();
 
         int frameOrder[] = {0, 1, 0, 2};
 
         Animation south = new Animation(frameOrder);
         south.loadAnimation("assets/char_model/model_down_", 3);
 
         animations.put("south", south);
        south.beginAnimation(150);
     }
 
     public BufferedImage getFrame()
     {
         return animations.get("south").getFrame();
     }
 }
