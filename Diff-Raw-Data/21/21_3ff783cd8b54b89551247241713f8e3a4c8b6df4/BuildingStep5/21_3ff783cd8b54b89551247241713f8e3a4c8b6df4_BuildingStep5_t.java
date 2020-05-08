 package fr.nantes1900.models.extended.steps;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.vecmath.Vector3d;
 
 import fr.nantes1900.constants.coefficients.SeparationWallsSeparationRoofs;
 import fr.nantes1900.models.basis.Mesh;
 import fr.nantes1900.models.basis.Point;
 import fr.nantes1900.models.basis.Polygon;
 import fr.nantes1900.models.extended.Ground;
 import fr.nantes1900.models.extended.Roof;
 import fr.nantes1900.models.extended.Surface;
 import fr.nantes1900.models.extended.Surface.ImpossibleNeighboursOrderException;
 import fr.nantes1900.models.extended.Surface.InvalidSurfaceException;
 import fr.nantes1900.models.extended.Wall;
 import fr.nantes1900.models.islets.buildings.exceptions.NullArgumentException;
 import fr.nantes1900.utils.Algos;
 
 /**
  * Implements a building step : a state of the building. This step is after
  * separation between walls and between roofs and before the determination of
  * the neighbours of each surfaces.
  * @author Daniel Lefèvre
  */
 public class BuildingStep5 extends AbstractBuildingStep {
 
     /**
      * Number minimal of neighbours to be considered as a real surface.
      */
     private static final int NUMBER_MIN_OF_NEIGHBOURS = 3;
     /**
      * The mesh representing the noise.
      */
     private Surface noise;
 
     /**
      * The normal to the ground.
      */
     private Vector3d groundNormal;
     /**
      * The list of walls.
      */
     private List<Wall> walls = new ArrayList<>();
 
     /**
      * The list of roofs.
      */
     private List<Roof> roofs = new ArrayList<>();
 
     /**
      * The grounds.
      */
     private Ground ground;
 
     /**
      * Constructor.
      * @param wallsIn
      *            the list of walls
      * @param roofsIn
      *            the list of roofs
      */
     public BuildingStep5(final List<Wall> wallsIn, final List<Roof> roofsIn) {
         this.walls = wallsIn;
         this.roofs = roofsIn;
     }
 
     /**
      * Orders the neighbours by calling the method orderNeighbours from the
      * class Surface and then computes the contour of the surface, using the
      * sorted neighbours.
      */
     public final void orderNeighboursAndDeterminateContours() {
         // Creates the map where the points and edges will be put : if one
         // point is created a second time, it will be given the same
         // reference as the other one having the same values.
         final Map<Point, Point> pointMap = new HashMap<>();
 
         // Adds all the surfaces
         final List<Surface> wholeList = new ArrayList<>();
         wholeList.addAll(this.walls);
         wholeList.addAll(this.roofs);
 
         for (final Surface surface : wholeList) {
             try {
                 // Orders its neighbours in order to treat them.
                 // If the neighbours of one surface are not 2 per 2 neighbours
                 // each other, then it tries to correct it.
                 surface.orderNeighbours(wholeList, this.ground);
 
                 // When the neighbours are sorted, finds the intersection of
                 // them to find the edges of this surface.
                 surface.setPolygon(surface.findEdges(this.walls, pointMap,
                         this.groundNormal));
 
             } catch (final InvalidSurfaceException e) {
                 // If there is a problem, we cannot continue the process.
             } catch (final ImpossibleNeighboursOrderException e) {
                 // If there is a problem, the process cannot continue.
             }
         }
     }
 
     /**
      * Determinates the neighbours of a surface.
      */
     public final void determinateNeighbours() {
         final Polygon groundsBounds = this.ground.getMesh()
                 .returnUnsortedBounds();
 
         final List<Surface> wholeList = new ArrayList<>();
         wholeList.addAll(this.walls);
         wholeList.addAll(this.roofs);
 
         // To find every neighbours, we complete every holes between roofs
         // and walls by adding all the noise.
         final List<Mesh> wholeListFakes = new ArrayList<>();
         for (final Surface m : wholeList) {
             final Mesh fake = new Mesh(m.getMesh());
             wholeListFakes.add(fake);
         }

         Algos.blockTreatPlanedNoise(wholeListFakes,
                 new Mesh(this.noise.getMesh()),
                 SeparationWallsSeparationRoofs.getPlanesError());
 
         // First we clear the neighbours.
         for (final Surface s : wholeList) {
             s.getNeighbours().clear();
         }
         // And we clear the neighbours of the grounds.
         this.ground.getNeighbours().clear();
 
         // We compute the bounds to check if they share a common edge.
         final List<Polygon> wholeBoundsList = new ArrayList<>();
         for (final Mesh m : wholeListFakes) {
             wholeBoundsList.add(m.returnUnsortedBounds());
         }
 
         // Then we check every edge of the bounds to see if some are shared by
         // two meshes. If they do, they are neighbours.
         for (int i = 0; i < wholeBoundsList.size(); i = i + 1) {
             final Polygon polygone1 = wholeBoundsList.get(i);
 
             for (int j = i + 1; j < wholeBoundsList.size(); j = j + 1) {
                 final Polygon polygone2 = wholeBoundsList.get(j);
 
                 if (polygone1.isNeighbour(polygone2)) {
                     wholeList.get(i).addNeighbour(wholeList.get(j));
                 }
             }
 
             if (polygone1.isNeighbour(groundsBounds)) {
                 wholeList.get(i).addNeighbour(this.ground);
             }
         }
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
 
     @Override
     public final BuildingStep6 launchProcess() throws NullArgumentException {
         if (this.ground == null || this.noise == null
                 || this.groundNormal == null) {
             throw new NullArgumentException();
         }
 
         this.determinateNeighbours();
 
         this.sortSurfaces();
 
         this.orderNeighboursAndDeterminateContours();
 
         // Copies the walls and roofs to work on different versions.
         List<Wall> wallsCopy = new ArrayList<>();
         for (Wall w : this.walls) {
            wallsCopy.add(w);
         }
         List<Roof> roofsCopy = new ArrayList<>();
         for (Roof r : this.roofs) {
            roofsCopy.add(r);
         }
         return new BuildingStep6(wallsCopy, roofsCopy);
 
     }
 
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
             r.setNodeString("Toit " + counterRoof);
             root.add(r.returnNode());
             counterRoof++;
         }
 
         return root;
     }
 
     /**
      * Setter.
      * @param noiseIn
      *            the noise
      * @param groundIn
      *            the ground as surface used in processs
      * @param groundNormalIn
      *            the normal to the ground
      */
     public final void setArguments(final Surface noiseIn,
             final Ground groundIn, final Vector3d groundNormalIn) {
         this.noise = noiseIn;
         this.ground = groundIn;
         this.groundNormal = groundNormalIn;
     }
 
     /**
      * Removes every surfaces which have less than or equal 2 neighbours : it is
      * considered they are not really real surfaces.
      */
     private void sortSurfaces() {
         for (int i = 0; i < this.walls.size(); i++) {
             final Surface s = this.walls.get(i);

             if (s.getNeighbours().size() < NUMBER_MIN_OF_NEIGHBOURS) {
                 this.walls.remove(s);
                 for (final Surface neighbour : s.getNeighbours()) {
                     neighbour.getNeighbours().remove(s);
                 }
             }
         }

         for (int i = 0; i < this.roofs.size(); i++) {
             final Surface s = this.roofs.get(i);

             if (s.getNeighbours().size() < NUMBER_MIN_OF_NEIGHBOURS) {
                 this.roofs.remove(s);
                 for (final Surface neighbour : s.getNeighbours()) {
                     neighbour.getNeighbours().remove(s);
                 }
             }
         }
     }
 }
