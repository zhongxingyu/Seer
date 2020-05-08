 package interiores.business.models.constraints.furniture.unary;
 
 import interiores.business.models.Orientation;
 import interiores.business.models.backtracking.area.Area;
 import interiores.business.models.backtracking.FurnitureValue;
 import interiores.business.models.backtracking.FurnitureVariable;
 import interiores.business.models.constraints.furniture.InexhaustiveTrimmer;
 import interiores.business.models.constraints.furniture.UnaryConstraint;
 import interiores.core.Debug;
 import java.awt.Rectangle;
 import java.util.Arrays;
 import java.util.List;
 
 /**
  * The variable must be placed next to a wall identified by an orientation
  * in orientations.
  * @author nmamano
  */
 public class WallConstraint
     extends UnaryConstraint implements InexhaustiveTrimmer {
     
     private List<Orientation> orientations;
     int roomWidth;
     int roomDepth;
     
     public WallConstraint(int roomWidth, int roomDepth, Orientation[] orientations) {
         this.orientations = Arrays.asList(orientations);
         this.roomWidth = roomWidth;
         this.roomDepth = roomDepth;
     }
 
     
     /**
      * Trims values that lead to no valid solution.
      * 
      * This restriction has the following particularity: as the position
      * of a variable is identified by the position of its top left corner,
      * its trivial to find the valid positions when the variable has to be
      * attached to the north or west walls. The trimP for this cases is
      * exhaustive.
      * On the other hand, for east and south walls, the distance between the
      * top left corner of a variable and the wall depend on the size of the
      * model. This is why this constraint implements the InexhaustiveTrimmer
      * interface.
      */
     @Override
     public void preliminarTrim(FurnitureVariable variable) {
 
 
         Area validPositions = new Area();
         for(Orientation orientation : orientations) {
             
             Debug.println("Trimming " + orientation);
             
             int maxSize = variable.getMaxSize();
             
             switch(orientation) {
                 case N:
                     validPositions.union(
                         new Area(new Rectangle(0,0,roomWidth, maxSize)));
                     break;
 
                 case W:
                     validPositions.union(
                         new Area(new Rectangle(0,0,maxSize,roomDepth)));
                     break;
                                      
                 case S:
                     validPositions.union(new Area(new Rectangle(
                         0, roomDepth - maxSize,
                         roomWidth, maxSize)));
                     break;
                                      
                 case E:
                     validPositions.union(new Area(new Rectangle(
                         roomWidth - maxSize, 0,
                         roomWidth - maxSize, roomDepth)));
                     break;
             }    
         }
         
         variable.eliminateExceptP(validPositions);
     }
 
     
     @Override
     public boolean isSatisfied(FurnitureVariable variable) {
         boolean correct = false;
         FurnitureValue assignedValue = variable.getAssignedValue();
         for(Orientation orientation : orientations) {
             switch(orientation) {
                 case N:
                     if (assignedValue.getPosition().y == 0) {
                         correct = true;
                     }
                     break;
                 case W:
                     if (assignedValue.getPosition().x == 0) {
                         correct = true;
                     }
                     break;
                 case E:
                     if (assignedValue.getPosition().x +
                        assignedValue.getModel().getSize().width
                             == roomWidth) {
                         correct = true;
                     }
                     break;
 
                 case S:
                     if (assignedValue.getPosition().y +
                        assignedValue.getModel().getSize().depth
                             == roomDepth) {
                         correct = true;
                     }
                     break;
             }
         }
         return correct;
     }
     
     @Override
     public String toString() {
         String string = "";
         boolean first = true;
         for (Orientation orientation : orientations) {
             if (first) {
                 string += orientation.toString();
                 first = false;
             }
             else
                 string += ", " + orientation;
         }
             
         return "Walls: " + string;
         
     }
 }
