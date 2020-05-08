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
 
 import de.weltraumschaf.jebnf.gfx.shapes.Shape;
 import de.weltraumschaf.jebnf.gfx.shapes.compound.ColumnLayoutShape;
 import java.awt.Graphics2D;
 
 /**
  * Represents a railroad diagram of a syntax.
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 public class RailroadDiagram {
 
     /**
      * Shapes the diagram consists of.
      */
     private final ColumnLayoutShape shapes = ColumnLayoutShape.newColumnLayout();
 
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
      * Syntax railroad diagram title.
      */
     private String title = "";
 
     /**
      * Syntax railroad diagram meta.
      */
     private String meta = "";
 
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
         shapes.append(shape);
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
 
         shapes.adjust(graphic);
         shapes.paint(graphic);
     }
 
     /**
      * Sets the syntax railroad diagram's title.
      *
      * @param title The title string.
      */
     public void setTitle(final String title) {
         this.title = title;
     }
 
     /**
      * Sets the syntax railroad diagram's meta.
      *
      * @param meta The meta string.
      */
     public void setMeta(final String meta) {
         this.meta = meta;
     }
 
     /**
      * Get the size of the diagram.
      *
      * @return Return size object of containing shape compound..
      */
     public Size getSize() {
         return shapes.getSize();
     }
 
 }
