 package interiores.presentation.swing.views.map;
 
 import interiores.business.models.OrientedRectangle;
 import java.awt.Color;
 import java.awt.Graphics2D;
 
 /**
  *
  * @author hector
  */
 public class RoomElement
     implements Drawable
 {
     private static final Color DEFAULT_COLOR = Walls.getColor();
     
     protected OrientedRectangle rectangle;
    private Color color;
     
     public RoomElement(OrientedRectangle area) {
         this(area, DEFAULT_COLOR);
     }
     
     public RoomElement(OrientedRectangle area, Color color) {
         rectangle = (OrientedRectangle) area.clone();
         
         rectangle.translate(GridMap.getPadding(), GridMap.getPadding());
         this.color = color;
     }
     
     public void setColor(String c)
     {
         color = Color.decode(c);
     }
     
     @Override
     public void draw(Graphics2D g)
     {
         g.setColor(color);
         g.fill(rectangle);
     }
 }
