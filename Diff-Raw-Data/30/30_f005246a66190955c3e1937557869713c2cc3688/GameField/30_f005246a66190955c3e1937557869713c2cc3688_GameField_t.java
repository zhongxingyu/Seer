package edu.mines.csci598.recycler.entejagd;
 
 import java.awt.Graphics2D;
 import java.util.*;
 
 public final class GameField {
     private final LinkedList<GameObject> objects  = new LinkedList<GameObject>(),
             toInsert = new LinkedList<GameObject>();
 
     public void add(GameObject go) {
         toInsert.add(go);
     }
 
     public void update(float et) {
         for (ListIterator<GameObject> it = objects.listIterator();
                 it.hasNext(); ) {
             GameObject go = it.next();
             go.update(et);
             if (!go.isAlive())
                 it.remove();
                 }
         objects.addAll(toInsert);
         toInsert.clear();
     }
 
     public void draw(Graphics2D g) {
         for (GameObject go: objects)
             go.draw(g);
     }
 
     public void shoot(float x, float y) {
         for (GameObject go: objects)
             go.shoot(x, y);
     }
 }
