 /*
  * LICENSE
  *
  * "THE BEER-WARE LICENSE" (Revision 42):
  * "Sven Strittmatter" <weltraumschaf@googlemail.com> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a beer in return.
  *
  */
 
 package de.weltraumschaf.jebnf.gfx;
 
 import com.google.common.collect.Lists;
 import de.weltraumschaf.jebnf.gfx.shapes.Shape;
 import java.awt.Graphics2D;
import java.awt.RenderingHints;
 import java.util.List;
 
 /**
  * Represents a railroad diagram of a syntax.
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 public class RailroadDiagram {
 
     /**
      * Shapes the diagram consists of.
      */
     private final List<Shape> shapes = Lists.newArrayList();
 
     /**
      * Whether to use antialiasing or not.
      */
     private final boolean antialiasing;
 
     /**
      * Whether to draw debugging lines.
      *
      * Debugging lines are useful to verify painted shapes manually by watching them on screen.
      */
     private boolean debug;
 
     /**
      * Initializes object with {@link #antialiasing} set to false.
      */
     public RailroadDiagram() {
         this(false);
     }
 
     /**
      * Designated constructor.
      *
      * @param antialiasing Whether to use antialiasing or not.
      */
     public RailroadDiagram(final boolean antialiasing) {
         super();
         this.antialiasing = antialiasing;
         this.debug = false;
     }
 
     /**
      * Enable or disable debug lines.
      *
      * @param debug Debug lines wil be painted, if set true.
      */
     public void setDebug(final boolean debug) {
         this.debug = debug;
     }
 
     /**
      * Adds a shape to the diagram.
      *
      * @param shape Shape to add.
      */
     public void add(final Shape shape) {
         shapes.add(shape);
     }
 
     /**
      * Configure antialiasing for the given graphics context.
      *
      * @param graphic Context to use with antialiasing.
      */
     private void antialiase(final Graphics2D graphic) {
         Antialiaser.turnOn(graphic);
     }
 
     /**
      * Paint the diagram to the given graphics context.
      *
      * @param graphic Context to paint on.
      */
     public void paint(final Graphics2D graphic) {
         if (antialiasing) {
             antialiase(graphic);
         }
 
         for (Shape id : shapes) {
             id.setDebug(debug);
             id.paint(graphic);
         }
     }
 
 }
