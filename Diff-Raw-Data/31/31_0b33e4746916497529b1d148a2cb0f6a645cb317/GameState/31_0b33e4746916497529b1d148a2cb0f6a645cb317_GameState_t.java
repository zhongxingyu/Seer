 package com.voracious.dragons.common;
 
 import java.awt.Graphics2D;
import java.util.ArrayList;
 import java.util.List;
 import com.voracious.dragons.client.graphics.Drawable;
 import com.voracious.dragons.client.towers.Tower;
 import com.voracious.dragons.client.units.Unit;
 
 public class GameState implements Drawable {
 
     private List<Tower> towers;
     private List<Unit> units;
 
     public GameState() {
         super();
        towers=new ArrayList<>();
        units=new ArrayList<>();
     }
 
     @Override
     public void draw(Graphics2D g) {
         for (Tower t : towers)
             t.draw(g);
         
         for (Unit u : units)
             u.draw(g);
     }
 
     public void tick() {
         for (Tower t : towers)
             t.tick();
         
         for (Unit u : units)
             u.tick();
     }
 
     public void addTower(Tower t) {
         towers.add(t);
     }
 
     public void removeTower(Tower t) {
         towers.remove(t);
     }
 
     public void addUnit(Unit u) {
         units.add(u);
     }
 
     public void removeUnit(Unit u) {
         units.remove(u);
     }
 
 }
