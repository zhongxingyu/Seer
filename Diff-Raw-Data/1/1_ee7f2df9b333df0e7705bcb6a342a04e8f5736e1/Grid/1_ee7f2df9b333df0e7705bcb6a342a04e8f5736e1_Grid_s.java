 package io.github.christiangaertner.moebelplaner.grid;
 
 import io.github.christiangaertner.moebelplaner.Moebelplaner;
 import io.github.christiangaertner.moebelplaner.graphics.IRenderable;
 import io.github.christiangaertner.moebelplaner.graphics.Renderer;
 import io.github.christiangaertner.moebelplaner.graphics.Sprite;
 import io.github.christiangaertner.moebelplaner.graphics.blending.BlendingMode;
 import io.github.christiangaertner.moebelplaner.input.Keyboard;
 import io.github.christiangaertner.moebelplaner.input.Mouse;
 import io.github.christiangaertner.moebelplaner.util.Reversed;
 import java.awt.Rectangle;
 import java.awt.Shape;
 import java.awt.geom.Rectangle2D;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 /**
  *
  * @author Christian
  */
 public class Grid implements IRenderable, IUpdateable {
 
     /**
      * Die Sprite der Grid
      */
     protected Sprite sprite;
     /**
      * Die Position wird immer 0, 0 sein. Es ist nicht "final", da man so evtl. die Grid noch verschieben kann
      */
     protected int x, y;
     /**
      * Der MouseListener. Genutzt, um Möbel zu verschieben
      */
     protected Mouse mouse;
     /**
      * Der KeyListener.
      */
     protected Keyboard key;
     /**
      * Die Moebelplaner Instanz
      */
     protected Moebelplaner planer;
     /**
      * Alle Entities
      */
     protected List<AbstractEntity> entities = new ArrayList<AbstractEntity>();
     /**
      * Alle Highlights für entites
      */
     protected HashMap<Map<AbstractEntity, Highlight.Type>, Highlight> highlights = new HashMap<Map<AbstractEntity, Highlight.Type>, Highlight>();
     /**
      * Die gerade "markierten" Entities
      */
     protected List<AbstractEntity> focus = new ArrayList<AbstractEntity>();
     protected boolean translatingEntity = false;
 
     public Grid(Moebelplaner planer, Mouse mouse, Keyboard key) {
         this();
         this.mouse = mouse;
         this.key = key;
         this.planer = planer;
     }
 
     public Grid() {
         x = 0;
         y = 0;
         sprite = new Sprite("/images/grid.png");
     }
 
     /**
      * Fügt eine neue Entity der Grid hinzu.
      *
      * @param e
      */
     public void add(AbstractEntity e) {
         entities.add(e);
     }
 
     /**
      * Löscht eine Entity
      *
      * @param e
      */
     public void delete(AbstractEntity e) {
         entities.remove(e);
     }
 
     /**
      * Fokussiert alle Entities
      *
      * @param e
      */
     public void focus() {
         unFocus();
         for (Iterator<AbstractEntity> it = entities.iterator(); it.hasNext();) {
             AbstractEntity e = it.next();
             focus(e);
             highlight(e, Highlight.Type.FOCUS);
         }
     }
 
     public void deleteFocused() {
         for (Iterator<AbstractEntity> it = focus.iterator(); it.hasNext();) {
             AbstractEntity e = it.next();
             // Löschen von der Entities Liste
             delete(e);
             // Löschen von der Focus Liste
             it.remove();
             unhighlight(e, Highlight.Type.FOCUS);
         }
     }
 
     /**
      * Rendered diese Grid und alle sich darauf befindenden Entities.
      *
      * @param renderer
      */
     public void render(Renderer renderer) {
         // Erstmal die Grid selber rendern
         renderer.render(this);
         for (Iterator<AbstractEntity> it = entities.iterator(); it.hasNext();) {
             AbstractEntity e = it.next();
             renderer.render(e);
         }
         for (Iterator<Map.Entry<Map<AbstractEntity, Highlight.Type>,Highlight>> it = highlights.entrySet().iterator(); it.hasNext();) {
             Highlight h = it.next().getValue();
             renderer.render(BlendingMode.NORMAL, h, 50);
         }
     }
 
     @Override
     public void update() {
 
         // Wenn wir nichts mehr drücken,
         // dann bewegen wir auch nichts mehr
         if (mouse.hold() == -1) {
             translatingEntity = false;
         }
 
         // Wenn Linke-Taste gehalten ist Möbel bewegen
         if ((mouse.leftHold() && getEntity(mouse.x(), mouse.y()) != null) || translatingEntity) {
             translatingEntity = true;
             moveFocused(mouse.x() - mouse.preX(), mouse.y() - mouse.preY());
         }
 
         // Wenn Pfeiltasten gedrückt sind Möbel bewegen
         int acc = 0;
         if (key.isKeyDown("shift")) {
             acc = 2;
         }
         if (key.keyHit("up")) {
             moveFocused(0, -1 - acc);
         }
         if (key.keyHit("down")) {
             moveFocused(0, 1 + acc);
         }
         if (key.keyHit("right")) {
             moveFocused(1 + acc, 0);
         }
         if (key.keyHit("left")) {
             moveFocused(-1 - acc, 0);
         }
 
 
         // Wenn Links-Klick versuchen eine Entity zu fokusieren
         if (mouse.leftClick()) {
             AbstractEntity e = getEntity(mouse.x(), mouse.y());
 
             // Wenn e null ist, dann haben wir ins Leere geklickt
             // also alles defokussieren
             if (e == null) {
                 unFocus();
             } else if (key.isKeyDown("shift")) {
                 // Wenn man shift drückt, möchte man mehrere fokussieren
                 focus(e);
             } else if (e.isFocused()) {
                 // Wenn die Entity schon fokussiert ist, dann
                 // defokussieren alle anderen (außer wenn wir sie bewegen)
                 if (!translatingEntity) {
                     unFocus();
                 }
                 focus(e);
             } else {
                 // jetzt wurde auf eine nicht fokussierte Entity geklickt
                 // Also alles andere defokusieren und jene fokussieren
                 unFocus();
                 focus(e);
             }
         }
 
         // Jetzt checken wir noch Collisions
         calculateCollisions();
 
 
 //        // Alle Entities updaten       
 //        for (AbstractEntity e : entities) {
 //            e.update();
 //        }
     }
 
     /**
      * Löscht alle Entities. In folgenden Listen: entities, focus
      */
     public void clearAll() {
         entities.clear();
         focus.clear();
         highlights.clear();
     }
 
     /**
      * Die Anzahl aller Entities
      *
      * @return entities.size()
      */
     public int entityCount() {
         return entities.size();
     }
 
     /**
      * Die Anzahl aller fokussierten Entities
      *
      * @return focus.size()
      */
     public int focusCount() {
         return focus.size();
     }
     
     /**
      * Die Anzahl aller fokussierten Entities
      *
      * @return focus.size()
      */
     public int highlightCount() {
         return highlights.size();
     }
 
     /**
      * Fokussiert eine Entity
      *
      * @param e
      */
     private void focus(AbstractEntity e) {
         if (!focus.contains(e)) {
             e.focus();
             focus.add(e);
             highlight(e, Highlight.Type.FOCUS);
         }
     }
 
     /**
      * Defokussiert eine Entity
      *
      * @param e
      */
     private void unFocus(AbstractEntity e) {
         if (focus.contains(e)) {
             focus.remove(e);
             e.unFocus();
             unhighlight(e, Highlight.Type.FOCUS);
         }
     }
 
     /**
      * Entfernt alle Entities aus der focus list und ruft "unFocus()" bei den Objekten auf
      */
     private void unFocus() {
         for (Iterator<AbstractEntity> it = focus.iterator(); it.hasNext();) {
             AbstractEntity e = it.next();
             it.remove();
             e.unFocus();
             unhighlight(e, Highlight.Type.FOCUS);
         }
     }
 
     /**
      * Gibt die Entity an einer bestimmten Koordiante zurück
      *
      * @param x
      * @param y
      * @return Die Entity an den gegebenen Koordinaten
      */
     private AbstractEntity getEntity(int x, int y) {
         // Man muss das ja nicht in jeder Iteration
         // neu deklarieren...
         Shape bounds;
         for (AbstractEntity e : Reversed.reversed(entities)) {
 
             // Die Boundaries gekommen und erstmal das Rechteck davon
             // später werden evtl. mehr Bounding-Boxes implementiert
             bounds = e.getBoundaries().getBounds();
             if (bounds.contains(x, y)) {
                 return e;
             }
 
         }
         return null;
     }
 
     private void moveFocused(int x, int y) {
         IMoveable m;
         for (Iterator<AbstractEntity> it = focus.iterator(); it.hasNext();) {
             AbstractEntity e = it.next();
             if (e instanceof IMoveable) {
                 m = (IMoveable) e;
                 m.move(x, y);
                 getHighlight((AbstractEntity) m, Highlight.Type.FOCUS).move(x, y);
 //                getHighlight((AbstractEntity) m, Highlight.Type.ALERT).move(x, y);
             }
         }
     }
 
     private void calculateCollisions() {
         List<AbstractEntity> colliding = new ArrayList<AbstractEntity>();
         for (Iterator<AbstractEntity> it = entities.iterator(); it.hasNext();) {
             AbstractEntity e1 = it.next();
             e1.unAlert();
             unhighlight(e1, Highlight.Type.ALERT);
             for (Iterator<AbstractEntity> i = entities.iterator(); i.hasNext();) {
                 AbstractEntity e2 = i.next();
                 e2.unAlert();
                 unhighlight(e2, Highlight.Type.ALERT);
                 if (colliding(e1, e2) && !e2.equals(e1)) {
                     if (!colliding.contains(e1)) {
                         colliding.add(e1);
                     }
                     if (!colliding.contains(e2)) {
                         colliding.add(e2);
                     }
                 }
             }
         }
 
         for (Iterator<AbstractEntity> it = entities.iterator(); it.hasNext();) {
             AbstractEntity e = it.next();
             if (colliding.contains(e)) {
                 e.alert();
                 highlight(e, Highlight.Type.ALERT);
             }
         }
     }
 
     private boolean colliding(AbstractEntity e1, AbstractEntity e2) {
         Rectangle bounds1 = e1.getBoundaries().getBounds();
         Rectangle bounds2 = e2.getBoundaries().getBounds();
 
         if (bounds1.intersects(bounds2)) {
             return true;
         } else {
             return false;
         }
     }
 
     private void highlight(AbstractEntity e, Highlight.Type type) {
         Highlight h = new Highlight(type, e.x(), e.y(), (int) e.getBoundaries().getBounds().getWidth(), (int) e.getBoundaries().getBounds().getHeight());
         Map<AbstractEntity, Highlight.Type> key = new HashMap<AbstractEntity, Highlight.Type>();
         key.put(e, type);
         highlights.put(key, h);
     }
 
     private void unhighlight(AbstractEntity e, Highlight.Type type) {
         Map<AbstractEntity, Highlight.Type> key = new HashMap<AbstractEntity, Highlight.Type>();
         key.put(e, type);
         highlights.remove(key);
     }
     
     private Highlight getHighlight(AbstractEntity e, Highlight.Type type) {
         Map<AbstractEntity, Highlight.Type> key = new HashMap<AbstractEntity, Highlight.Type>();
         key.put(e, type);
         
         return highlights.get(key);  
     }
 
     @Override
     public Shape getBoundaries() {
         return new Rectangle2D.Double(0, 0, sprite.getWidth(), sprite.getHeight());
     }
 
     @Override
     public Sprite getSprite() {
         return sprite;
     }
 
     @Override
     public int x() {
         return x;
     }
 
     @Override
     public int y() {
         return y;
     }
 }
