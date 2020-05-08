 
 package pintosim;
 
 import java.awt.*;
 import java.awt.image.BufferStrategy;
 import java.util.*;

 /**
  * 
  * This class functions as the canvas where we paint the map and other moving things.
  * It accepts Paintable and just continually repaints them all very frequently
  */
 
 public class AnimPanel extends Canvas {
     private BufferStrategy strategy;
     private final int width;
     private final int height;
     private final int RENDER_FREQUENCY_MS;
     private SortedSet<Paintable> paintables;
     
     public AnimPanel(int width, int height, int renderFrequencyMS) {
         setBounds(0, 0, width, height);
         setIgnoreRepaint(true);
         this.width = width;
         this.height = height;
         RENDER_FREQUENCY_MS = renderFrequencyMS;
         // we keep the Paintables sorted by their zIndex. 
         // This way the higher zindexes get painted last, letting them 
         // be drawn "on top" when objects overlap
         //eg, everything needs to be drawn on top of floors, and pintos also on top of items
         paintables = new TreeSet<Paintable>(new Comparator<Paintable>() {
             public int compare(Paintable a, Paintable b) {
                 int diff = a.zIndex() - b.zIndex();
                 return diff != 0 ? diff : a.hashCode() - b.hashCode(); //break the tie, otherwise TreeSet wont allow the duplicates
             }
         });
     }
     
 
 
     // this gets automatically called for us when we add this component to another component.
     // its important because you cant create a buffer strategy until after this component has a parent component
     public void addNotify() {
         super.addNotify();
         this.createBufferStrategy(2);
         strategy = this.getBufferStrategy();
         startRendering();
 
     }
     
     public void startRendering() {
         Timer t = new Timer(true);
         TimerTask task = new TimerTask() {
             public void run() {
                 Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
                 g.setColor(Color.black);
                 g.fillRect(0, 0, width, height);
 
                 for (Paintable paintable : paintables) {
                     paintable.paint(g);
                     if (paintable instanceof MovingSprite ) {
                         MovingSprite ms = (MovingSprite) paintable;
                         int size = ms.transitionHistoryForDebugging.size();
                         if (size > 2) {
                             if (size < 0) {System.out.println("");}
                         }
                     }
                 }
 
                 g.dispose();
                 strategy.show();
             }
         };
         t.scheduleAtFixedRate(task, 0, RENDER_FREQUENCY_MS);
     }
     
 
     public void addPaintable(Paintable paintable) {
        paintables.add(paintable);
     }
 
 }
