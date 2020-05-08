 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package mss;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import mss.util.Notifications;
 import mss.util.Planet;
 import mss.util.Util;
 import mss.util.Vektor2D;
 import org.lwjgl.Sys;
 import org.lwjgl.util.Color;
 
 /**
  *
  * @author Bernhard Sirlinger
  */
 public class Main implements Observer, Observable, Runnable {
 
     public static boolean closed = false;
     private final HashMap<String, Observer> observers;
     private ArrayList<Planet> planets;
     private ArrayList<Planet> startPlanets;
     private long time;
     private boolean shouldRun;
     private boolean paused;
    private boolean areColliding = false;
     private boolean shouldReset = true;
 
     @SuppressWarnings("unchecked")
     public Main() {
         this.planets = new ArrayList<>();
         this.observers = new HashMap<>();
 
         this.planets.add(new Planet("Sun", new Vektor2D(0, 3), 1e10, 1, new Vektor2D(0, 0), new Color(255, 255, 255)));
         this.planets.add(new Planet("Planet", new Vektor2D(0, 0), 100, 0.5, new Vektor2D(-0.05, 0.05), new Color(244, 233, 10)));
         this.planets.add(new Planet("Planet2", new Vektor2D(0, -3), 1e10, 1, new Vektor2D(), new Color(255, 255, 255)));
         this.startPlanets = (ArrayList<Planet>) this.planets.clone();
     }
 
     @SuppressWarnings("unchecked")
     private void run(String msg) {
         this.time = this.getTime();
 
         while (this.shouldReset) {
             this.shouldReset = false;
             this.planets = (ArrayList<Planet>) this.startPlanets.clone();
             this.sendPlanetsToObservers(Notifications.DISPLAY, this.planets);
 
             while (!this.shouldRun) {
                 if (Main.closed) {
                     return;
                 }
                 if (this.shouldReset) {
                     break;
                 }
                 try {
                     Thread.sleep(100);
                 } catch (InterruptedException ex) {
                 }
             }
 
             while (true) {
                 if (Main.closed) {
                     return;
                 }
                 if (this.shouldReset) {
                     break;
                 }
 
                 if (!this.paused) {
                     long delta = this.getDelta();
                     if (delta >= 0.5) {
                         this.sendPlanetsToObservers(Notifications.UPDATE, this.planets);
                         this.time = this.getTime();
                     }
                 } else {
                     try {
                         Thread.sleep(100);
                     } catch (InterruptedException ex) {
                     }
                 }
             }
         }
     }
 
     @Override
     public void notify(Notifications type, String data) {
         switch (type) {
             case DELTA_CHANGE:
                 this.notifyObservers(type, data);
                 break;
             case RESUME:
                if (!this.areColliding) {
                    this.paused = false;
                }
                 break;
             case PAUSE:
                 this.paused = true;
                 break;
             case START:
                if (!this.areColliding) {
                    this.shouldRun = true;
                    this.paused = false;
                    this.shouldReset = false;
                    this.areColliding = false;
                }
                 break;
             case RESET:
                 this.paused = true;
                 this.shouldRun = false;
                 this.shouldReset = true;
                this.areColliding = false;
                 break;
         }
     }
 
     private long getTime() {
         return (Sys.getTime() * 1000 / Sys.getTimerResolution());
     }
 
     private long getDelta() {
         long current_time = this.getTime();
         long delta = current_time - this.time;
 
         return delta;
     }
 
     @Override
     public void registerObserver(String key, Observer observer) {
         this.observers.put(key, observer);
     }
 
     @Override
     public void removeObserver(String key) {
         this.observers.remove(key);
     }
 
     /**
      *
      * @param type
      * @param data
      */
     @Override
     public void notifyObservers(Notifications type, String data) {
         Collection<Observer> values = this.observers.values();
         Object[] toArray = values.toArray();
 
         for (Object temp : toArray) {
             ((Observer) temp).notify(type, data);
         }
     }
 
     @Override
     public void sendPlanets(Notifications type, ArrayList<Planet> planets) {
         switch (type) {
             case RESULT:
                 this.planets = planets;
                 String collisions = Util.findCollisions(planets);
                 if (!collisions.isEmpty()) {
                     System.out.println(collisions);
                     this.paused = true;
                    this.areColliding = true;
                 }
                 break;
             case RESET:
                 this.startPlanets = planets;
                 break;
         }
     }
 
     /**
      *
      * @param type
      * @param planets
      */
     @Override
     public void sendPlanetsToObservers(Notifications type, ArrayList<Planet> planets) {
         Collection<Observer> values = this.observers.values();
         Object[] toArray = values.toArray();
 
         for (Object temp : toArray) {
             ((Observer) temp).sendPlanets(type, planets);
         }
     }
 
     @Override
     public void run() {
         this.run("");
     }
 }
