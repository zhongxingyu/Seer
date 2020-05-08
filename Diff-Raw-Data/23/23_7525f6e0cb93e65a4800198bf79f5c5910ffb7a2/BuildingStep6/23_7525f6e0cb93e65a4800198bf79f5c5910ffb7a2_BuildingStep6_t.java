 package fr.nantes1900.models.extended.steps;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.tree.DefaultMutableTreeNode;
 
 import fr.nantes1900.models.extended.Roof;
 import fr.nantes1900.models.extended.Surface;
 import fr.nantes1900.models.extended.Wall;
 
 /**
  * Implements a building step : a state of the building. This step is after the
  * determination of the neighbours and before the sort of neighbours.
  * @author Daniel Lefevre
  */
 public class BuildingStep6 extends AbstractBuildingStep {
 
     /**
      * The list of walls.
      */
     private List<Wall> walls = new ArrayList<>();
 
     /**
      * The list of roofs.
      */
     private List<Roof> roofs = new ArrayList<>();
 
     /**
      * The noise.
      */
     private Surface noise;
 
     /**
      * Constructor.
      * @param wallsIn
      *            the list of walls
      * @param roofsIn
      *            the list of roofs
      */
     public BuildingStep6(final List<Wall> wallsIn, final List<Roof> roofsIn) {
         this.walls = wallsIn;
         this.roofs = roofsIn;
     }
 
     /**
      * Getter.
      * @return the noise
      */
     public final Surface getNoise() {
         return this.noise;
     }
 
     /**
      * Getter.
      * @return the list of roofs
      */
     public final List<Roof> getRoofs() {
         return this.roofs;
     }
 
     /**
      * Getter.
      * @return the list of walls
      */
     public final List<Wall> getWalls() {
         return this.walls;
     }
 
     /*
      * (non-Javadoc)
      * @see
      * fr.nantes1900.models.extended.steps.AbstractBuildingStep#launchProcess ()
      */
     @Override
    public final BuildingStep7 launchProcess() {
        return new BuildingStep7(this.walls, this.roofs);
     }
 
     /*
      * (non-Javadoc)
      * @see
      * fr.nantes1900.models.extended.steps.AbstractBuildingStep#returnNode()
      */
     @Override
     public final DefaultMutableTreeNode returnNode(final int counter) {
         DefaultMutableTreeNode root = new DefaultMutableTreeNode("Bâtiment "
                 + counter);
 
         int counterWall = 0;
         for (Wall w : this.walls) {
             w.setNodeString("Mur " + counterWall);
             root.add(w.returnNode());
             counterWall++;
         }
 
         int counterRoof = 0;
         for (Roof r : this.roofs) {
             r.setNodeString("Toit  " + counterRoof);
             root.add(r.returnNode());
             counterRoof++;
         }
 
         return root;
     }
 }
