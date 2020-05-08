 import greenfoot.*;
 import java.util.List;
 
 public class HelicopterWorld extends World {
     private Helicopter helicopter;
     private Counter scoreCounter;
     private MenuBar menuBar;
     private House house1;
     private SpeedPowerUp speed1;
 
     public HelicopterWorld() {
         super(80, 80, 10, false);
         
         helicopter = new Helicopter();
         addObject(helicopter, 40, 35);
         
         scoreCounter = new Counter("Score: ");
         addObject(scoreCounter, 6, 74);
         
         menuBar = new MenuBar();
         addObject(menuBar, 39, 75);
         
         house1 = new House();
        addObject(house1, 20, 65);
         
         speed1 = new SpeedPowerUp();
         addObject(speed1, 20, 40);
         
         setPaintOrder(Counter.class, MenuBar.class, House.class);
     }
 
     @Override
     public void act() {
         for (Actor actor : (List<Actor>)getObjects(null)) {
             if (actor == helicopter) continue;
             if (actor == scoreCounter) continue;
        
             actor.setLocation(actor.getX() + 40 - helicopter.getX(), actor.getY());
         }
 
        helicopter.setLocation(40, helicopter.getY());
         scoreCounter.setLocation(6, 74);
         menuBar.setLocation(39, 75);
         scoreCounter.add(1);
     }
 }
