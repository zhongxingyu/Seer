 package de.hswt.hrm.scheme.ui.dnd;
 
 import java.io.Serializable;
 import java.util.List;
 
 import de.hswt.hrm.scheme.model.Direction;
 import de.hswt.hrm.scheme.model.RenderedComponent;
 import de.hswt.hrm.scheme.ui.SchemeGridItem;
 
 /**
  * This class represents a object, that is dragged in the SchemePart.
  * It is a Serializable version of DirectedRenderedComponent, which is not
  * Serialized by itself for Performance reasons.
  * 
  * @author Michael Sieger
  *
  */
 public class DragData 
     implements Serializable
 {
     private static final long serialVersionUID = 3635333453619223967L;
     
     /**
      * The index in the RenderedComponent set.
      */
     private final int id;
     
     /**
      * The x position in the grid, can be -1 for no Position
      */
     private final int x;
     
     /**
      * The y position in the grid, can be -1 for no Position
      */
     private final int y;
     
     /**
      * The direction
      */
     private final Direction direction;
     
     public DragData(int id, int x, int y, Direction direction) {
         super();
         this.id = id;
         this.x = x;
         this.y = y;
         this.direction = direction;
     }
     
     public DragData(int id, Direction direction){
     	this(id, -1, -1, direction);
     }
 
     public int getId() {
         return id;
     }
 
     public int getX() {
         return x;
     }
 
     public int getY() {
         return y;
     }
     
     /**
      * Does this DragData have a position.
      * This is false, if it is dragged from the Tree for example.
      * 
      * @return
      */
     public boolean hasPosition(){
    	return x == -1 && y == -1;
     }
 
     public Direction getDirection() {
         return direction;
     }
     
     /**
      * Creates the SchemeGridItem, that is DragData is representing
      * 
      * @param comps0
      * @return
      */
     public SchemeGridItem toSchemeGridItem(List<RenderedComponent> comps){
     	return new SchemeGridItem(comps.get(id), direction, x, y);
     }
 
 }
