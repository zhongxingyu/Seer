 package fr.nantes1900.control;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.vecmath.Vector3d;
 
 import fr.nantes1900.constants.ActionTypes;
 import fr.nantes1900.constants.Characteristics;
 import fr.nantes1900.constants.TextsKeys;
 import fr.nantes1900.constants.WeirdResultMessages;
 import fr.nantes1900.control.display3d.Universe3DController;
 import fr.nantes1900.models.basis.Mesh;
 import fr.nantes1900.models.basis.Triangle;
 import fr.nantes1900.models.extended.Building;
 import fr.nantes1900.models.extended.Roof;
 import fr.nantes1900.models.extended.Surface;
 import fr.nantes1900.models.extended.Wall;
 import fr.nantes1900.models.extended.steps.BuildingStep3;
 import fr.nantes1900.models.extended.steps.BuildingStep4;
 import fr.nantes1900.models.extended.steps.BuildingStep5;
 import fr.nantes1900.models.extended.steps.BuildingStep6;
 import fr.nantes1900.models.islets.AbstractIslet;
 import fr.nantes1900.models.islets.buildings.AbstractBuildingsIslet;
 import fr.nantes1900.models.islets.buildings.ResidentialIslet;
 import fr.nantes1900.models.islets.buildings.exceptions.InvalidCaseException;
 import fr.nantes1900.models.islets.buildings.exceptions.NullArgumentException;
 import fr.nantes1900.models.islets.buildings.exceptions.WeirdResultException;
 import fr.nantes1900.models.islets.buildings.steps.BuildingsIsletStep0;
 import fr.nantes1900.utils.FileTools;
 import fr.nantes1900.utils.ParserSTL;
 
 /**
  * Implements the controller of a building islet. Used to visualize the islets,
  * to launch the processs.
  * @author Daniel
  */
 public class BuildingsIsletController {
 
     /**
      * The buildings islet containing the model.
      */
     private AbstractBuildingsIslet islet;
     /**
      * The universe 3D controller to interact with the universe 3D.
      */
     private Universe3DController u3DController;
 
     /**
      * Constructor.
      * @param universe3DControllerIn
      *            the universe 3D controller
      */
     public BuildingsIsletController(
             final Universe3DController universe3DControllerIn) {
         this.u3DController = universe3DControllerIn;
         this.islet = new ResidentialIslet();
     }
 
     /**
      * Resets the process to come back to the step 0.
      */
     public final void abortProcess() {
         this.islet.resetProgression();
     }
 
     /**
      * Changes the type of a list of triangles. To call only during the second
      * step.
      * @param trianglesSelected
      *            the list of triangles
      * @param type
      *            the new type of these triangles (TURN_TO_BUILDING,
      *            TURN_TO_GROUND), or REMOVE
      * @throws InvalidCaseException
      *             if the type of action is not possible in this method
      */
     public final void action2(final List<Triangle> trianglesSelected,
             final int type) throws InvalidCaseException {
         if (type == ActionTypes.TURN_TO_BUILDING) {
             // The user wants these triangles to turn building.
             this.islet.getBiStep2().getInitialGrounds().getMesh()
                     .removeAll(trianglesSelected);
             this.islet.getBiStep2().getInitialBuildings().getMesh()
                     .addAll(trianglesSelected);
         } else if (type == ActionTypes.TURN_TO_GROUND) {
             // The user wants these triangles to turn ground.
             this.islet.getBiStep2().getInitialBuildings().getMesh()
                     .removeAll(trianglesSelected);
             this.islet.getBiStep2().getInitialGrounds().getMesh()
                     .addAll(trianglesSelected);
         } else if (type == ActionTypes.REMOVE) {
             // The user wants these triangles to be removed.
             this.islet.getBiStep2().getInitialBuildings().getMesh()
                    .removeAll(trianglesSelected);
             this.islet.getBiStep2().getInitialGrounds().getMesh()
                     .removeAll(trianglesSelected);
         } else {
             throw new InvalidCaseException();
         }
     }
 
     /**
      * Changes the type of a surface (in noise or in real building). To call
      * only during the third step.
      * @param surface
      *            the surface
      * @param actionType
      *            the type of action, here : TURN_TO_NOISE
      * @throws InvalidCaseException
      *             if the type of action is not possible in this method
      */
     public final void action3(final Surface surface, final int actionType)
             throws InvalidCaseException {
         if (surface != this.islet.getBiStep3().getGrounds()) {
             if (actionType == ActionTypes.TURN_TO_NOISE) {
                 // The user wants the surface to turn to noise.
                 this.islet.getBiStep3().getBuildings()
                         .remove(this.returnBuildingContaining3(surface));
                 this.islet.getBiStep3().getNoise().getMesh()
                         .addAll(surface.getMesh());
             } else {
                 throw new InvalidCaseException();
             }
         }
     }
 
     /**
      * Changes the type of a list of triangles. To call only during the fourth
      * step.
      * @param trianglesSelected
      *            the list of triangles
      * @param actionType
      *            the new type of the triangles
      * @throws InvalidCaseException
      *             if the type of action is not possible in this method
      */
     public final void action4(final List<Triangle> trianglesSelected,
             final int actionType) throws InvalidCaseException {
         Building building = this
                 .searchForBuildingContaining4(trianglesSelected);
         BuildingStep4 buildingStep = building.getbStep4();
 
         if (actionType == ActionTypes.TURN_TO_WALL) {
             // The user wants the triangles to turn to wall.
             System.out.println(buildingStep.getInitialWallSurface().getMesh()
                     .addAll(trianglesSelected));
             System.out.println(buildingStep.getInitialRoofSurface().getMesh()
                     .removeAll(trianglesSelected));
 
         } else if (actionType == ActionTypes.TURN_TO_ROOF) {
             // The user wants the triangles to turn to roof.
             buildingStep.getInitialRoofSurface().getMesh()
                     .addAll(trianglesSelected);
             buildingStep.getInitialWallSurface().getMesh()
                     .removeAll(trianglesSelected);
 
         } else {
             throw new InvalidCaseException();
         }
     }
 
     /**
      * Changes the type of the list of surfaces. To call only during the fifth
      * step.
      * @param surfacesSelected
      *            the list of surfaces
      * @param actionType
      *            the new type of the surfaces
      * @throws InvalidCaseException
      *             if the type of action is not possible in this method
      */
     public final void action5(final List<Surface> surfacesSelected,
             final int actionType) throws InvalidCaseException {
         Building building = this.searchForBuildingContaining5(surfacesSelected);
 
         // If it is null, this means that the triangles selected are not
         // coherent with the action asked. Then we do nothing.
         if (building != null) {
 
             BuildingStep5 buildingStep = building.getbStep5();
 
             if (actionType == ActionTypes.MERGE) {
                 if (buildingStep.getWalls().contains(surfacesSelected.get(0))) {
                     // It means the meshes selected belong to the walls.
                     buildingStep.getWalls().removeAll(surfacesSelected);
                     Wall sum = new Wall();
                     for (Surface s : surfacesSelected) {
                         sum.getMesh().addAll(s.getMesh());
                     }
                     buildingStep.getWalls().add(sum);
 
                 } else {
                     // It means the meshes selected belong to the roofs.
                     buildingStep.getRoofs().removeAll(surfacesSelected);
                     Roof sum = new Roof();
                     for (Surface s : surfacesSelected) {
                         sum.getMesh().addAll(s.getMesh());
                     }
                     buildingStep.getRoofs().add(sum);
                 }
 
             } else if (actionType == ActionTypes.TURN_TO_NOISE) {
                 buildingStep.getWalls().removeAll(surfacesSelected);
                 buildingStep.getRoofs().removeAll(surfacesSelected);
                 for (Surface s : surfacesSelected) {
                     buildingStep.getNoise().getMesh().addAll(s.getMesh());
                 }
 
             } else {
                 throw new InvalidCaseException();
             }
         }
     }
 
     /**
      * Changes the list of neighbours of one surface. To call only durint the
      * 6th step.
      * @param surfaceLocked
      *            the current surface
      * @param newNeighbours
      *            the new list of neighbours
      */
     public final void action6(final Surface surfaceLocked,
             final List<Surface> newNeighbours) {
         List<Surface> oldNeigbhours = surfaceLocked.getNeighbours();
         for (Surface s : oldNeigbhours) {
             s.getNeighbours().remove(surfaceLocked);
         }
 
         surfaceLocked.setNeighbours(newNeighbours);
 
         for (Surface s : newNeighbours) {
             s.getNeighbours().add(surfaceLocked);
         }
 
         Building building = this.searchForBuildingContaining6(surfaceLocked);
         building.getbStep5().determinateOneContour(surfaceLocked);
     }
 
     /**
      * Changes the type of a surface. To call only durint the 6th step.
      * @param surface
      *            the surface
      * @param actionType
      *            the action, here : TURN_TO_NOISE
      * @throws InvalidCaseException
      *             if the type of action is not possible in this method
      */
     public final void action6(final Surface surface, final int actionType)
             throws InvalidCaseException {
         Building building = this.searchForBuildingContaining6(surface);
 
         // If it is null, this means that the triangles selected are not
         // coherent with the action asked. Then we do nothing.
         if (building != null) {
             BuildingStep6 buildingStep = building.getbStep6();
 
             if (actionType == ActionTypes.TURN_TO_NOISE) {
                 buildingStep.getWalls().remove(surface);
                 buildingStep.getRoofs().remove(surface);
                 buildingStep.getNoise().getMesh().addAll(surface.getMesh());
             } else {
                 throw new InvalidCaseException();
             }
         }
     }
 
     /**
      * Dsisplays the set of meshes, considering the progression of the
      * treatement.
      * @throws WeirdResultException
      *             if the result is not coherent
      */
     public final void display() throws WeirdResultException {
         this.u3DController.clearAll();
 
         try {
             switch (this.islet.getProgression()) {
             case AbstractBuildingsIslet.ZERO_STEP:
                 this.viewStep0();
                 break;
             case AbstractBuildingsIslet.FIRST_STEP:
                 this.viewStep1();
                 break;
             case AbstractBuildingsIslet.SECOND_STEP:
                 this.viewStep2();
                 break;
             case AbstractBuildingsIslet.THIRD_STEP:
                 this.viewStep3();
                 break;
             case AbstractBuildingsIslet.FOURTH_STEP:
                 this.viewStep4();
                 break;
             case AbstractBuildingsIslet.FIFTH_STEP:
                 this.viewStep5();
                 break;
             case AbstractBuildingsIslet.SIXTH_STEP:
                 this.viewStep6();
                 break;
             default:
                 throw new InvalidCaseException();
             }
         } catch (InvalidCaseException e) {
             System.out.println("Big problem");
         }
     }
 
     /**
      * Returns the type of the surface containing the triangle. Used only during
      * the second step.
      * @param triangle
      *            the triangle to check type of
      * @return the type as a static final String from the class Characteristic :
      *         it can be TYPE_BUILDING or TYPE_GROUND.
      * @throws InvalidCaseException
      *             if the triangle doesn't belong to any of these types
      */
     public final String getCharacteristics2(final Triangle triangle)
             throws InvalidCaseException {
         if (this.islet.getBiStep2().getInitialBuildings().getMesh()
                 .contains(triangle)) {
             return Characteristics.TYPE_BUILDING;
         } else if (this.islet.getBiStep2().getInitialGrounds().getMesh()
                 .contains(triangle)) {
             return Characteristics.TYPE_GROUND;
         } else {
             throw new InvalidCaseException();
         }
     }
 
     /**
      * Returns the type of a surface. Used only during the third step.
      * @param surface
      *            the surface to check type of
      * @return the type as a static final String from the class Characteristic :
      *         it can be TYPE_BUILDING, TYPE_NOISE or TYPE_GROUND.
      * @throws InvalidCaseException
      *             if the surface doesn't belong to any of these types
      */
     public final String getCharacteristics3(final Surface surface)
             throws InvalidCaseException {
         if (this.islet.getBiStep3().getGrounds() == surface) {
             return Characteristics.TYPE_GROUND;
         }
 
         if (this.islet.getBiStep3().getNoise() == surface) {
             return Characteristics.TYPE_NOISE;
         }
 
         for (Building b : this.islet.getBiStep3().getBuildings()) {
             if (b.getbStep3().getInitialTotalSurface() == surface) {
                 return Characteristics.TYPE_BUILDING;
             }
         }
 
         throw new InvalidCaseException();
     }
 
     /**
      * Returns the type of the surface containing the triangle. Used only during
      * the third step.
      * @param triangle
      *            the triangle to check type of
      * @return the type as a static final String from the class Characteristic :
      *         it can be TYPE_BUILDING, TYPE_NOISE or TYPE_GROUND.
      * @throws InvalidCaseException
      *             if the triangle doesn't belong to any of these types
      */
     public final String getCharacteristics3(final Triangle triangle)
             throws InvalidCaseException {
         if (this.islet.getBiStep3().getGrounds().getMesh().contains(triangle)) {
             return Characteristics.TYPE_GROUND;
         }
 
         if (this.islet.getBiStep3().getNoise().getMesh().contains(triangle)) {
             return Characteristics.TYPE_NOISE;
         }
 
         for (Building b : this.islet.getBiStep3().getBuildings()) {
             if (b.getbStep3().getInitialTotalSurface().getMesh()
                     .contains(triangle)) {
                 return Characteristics.TYPE_BUILDING;
             }
         }
 
         throw new InvalidCaseException();
     }
 
     /**
      * Returns the type of the surface containing the triangle. Used only during
      * the fourth step.
      * @param triangle
      *            the triangle to check type of
      * @return the type as a static final String from the class Characteristic :
      *         it can be TYPE_GROUND, TYPE_WALL or TYPE_ROOF.
      * @throws InvalidCaseException
      *             if the triangle doesn't belong to any of these types
      */
     public final String getCharacteristics4(final Triangle triangle)
             throws InvalidCaseException {
         if (this.islet.getBiStep4().getGrounds().getMesh().contains(triangle)) {
             return Characteristics.TYPE_GROUND;
         }
 
         for (Building b : this.islet.getBiStep4().getBuildings()) {
             if (b.getbStep4().getInitialWallSurface().getMesh()
                     .contains(triangle)) {
                 return Characteristics.TYPE_WALL;
             } else if (b.getbStep4().getInitialRoofSurface().getMesh()
                     .contains(triangle)) {
                 return Characteristics.TYPE_ROOF;
             }
         }
 
         throw new InvalidCaseException();
     }
 
     /**
      * Returns the type of a surface. Used only during the fifth step.
      * @param surface
      *            the surface to check type of
      * @return the type as a static final String from the class Characteristic :
      *         it can be TYPE_GROUND, TYPE_WALL or TYPE_ROOF.
      * @throws InvalidCaseException
      *             if the surface doesn't belong to any of these types
      */
     public final String getCharacteristics5(final Surface surface)
             throws InvalidCaseException {
         if (this.islet.getBiStep5().getGrounds() == surface) {
             return Characteristics.TYPE_GROUND;
         }
 
         for (Building b : this.islet.getBiStep5().getBuildings()) {
             for (Wall w : b.getbStep5().getWalls()) {
                 if (w == surface) {
                     return Characteristics.TYPE_WALL;
                 }
             }
             for (Roof r : b.getbStep5().getRoofs()) {
                 if (r == surface) {
                     return Characteristics.TYPE_ROOF;
                 }
             }
         }
 
         throw new InvalidCaseException();
     }
 
     /**
      * Returns the type of a surface. Used only during the sixth step.
      * @param surface
      *            the surface to check type of
      * @return the type as a static final String from the class Characteristic :
      *         it can be TYPE_GROUND, TYPE_WALL or TYPE_ROOF.
      * @throws InvalidCaseException
      *             if the surface doesn't belong to any of these types
      */
     public final String getCharacteristics6(final Surface surface)
             throws InvalidCaseException {
         if (this.islet.getBiStep6().getGrounds() == surface) {
             return Characteristics.TYPE_GROUND;
         }
 
         for (Building b : this.islet.getBiStep6().getBuildings()) {
             for (Wall w : b.getbStep6().getWalls()) {
                 if (w == surface) {
                     return Characteristics.TYPE_WALL;
                 }
             }
             for (Roof r : b.getbStep6().getRoofs()) {
                 if (r == surface) {
                     return Characteristics.TYPE_ROOF;
                 }
             }
         }
 
         throw new InvalidCaseException();
     }
 
     /**
      * Getter.
      * @return the buildings islet
      */
     public final AbstractBuildingsIslet getIslet() {
         return this.islet;
     }
 
     /**
      * Returns to the previous step.
      */
     public final void getPreviousStep() {
         this.islet.decProgression();
     }
 
     /**
      * Getter.
      * @return the universe 3D controller
      */
     public final Universe3DController getU3DController() {
         return this.u3DController;
     }
 
     /**
      * Progression incrementation.
      */
     private void incProgression() {
         this.islet.incProgression();
     }
 
     /**
      * Launch the process, considering the progression.
      * @throws WeirdResultException
      *             if the result is not coherent
      */
     public final void launchProcess() throws WeirdResultException {
         try {
             switch (this.islet.getProgression()) {
             case AbstractBuildingsIslet.ZERO_STEP:
                 this.islet.launchProcess0();
                 break;
             case AbstractBuildingsIslet.FIRST_STEP:
                 this.islet.launchProcess1();
                 break;
             case AbstractBuildingsIslet.SECOND_STEP:
                 this.islet.launchProcess2();
                 break;
             case AbstractBuildingsIslet.THIRD_STEP:
                 this.islet.launchProcess3();
                 break;
             case AbstractBuildingsIslet.FOURTH_STEP:
                 this.islet.launchProcess4();
                 break;
             case AbstractBuildingsIslet.FIFTH_STEP:
                 this.islet.launchProcess5();
                 break;
             default:
                 throw new InvalidCaseException();
             }
 
             this.incProgression();
             this.display();
         } catch (InvalidCaseException e) {
             // It should never happen.
             e.printStackTrace();
         } catch (NullArgumentException e) {
             // It should never happen.
             e.printStackTrace();
         }
     }
 
     /**
      * Sets the gravity normal in the islet attribute.
      */
     public final void putGravityNormal() {
         this.islet.setGravityNormal(this.islet.getGravityNormal());
     }
 
     /**
      * Parses the file and builds the first step of the BuildingIslet.
      * @param fileName
      *            the name of the file
      * @throws IOException
      *             if the file is badly formed, not found or unreadable !
      */
     public final void readFile(final String fileName) throws IOException {
         this.islet.setBiStep0(new BuildingsIsletStep0(AbstractIslet
                 .parseFile(fileName)));
     }
 
     /**
      * Reads the file containing the gravity normal and put it in this gravity
      * normal.
      * @param fileName
      *            the way of the file of the gravity normal
      * @throws IOException
      *             if the file cannot be read
      */
     public final void readGravityNormal(final String fileName)
             throws IOException {
         ParserSTL parser = new ParserSTL(fileName);
         Mesh mesh = parser.read();
         this.islet.setGravityNormal(mesh.averageNormal());
     }
 
     /**
      * Returns the building containing the surface. To call only in the third
      * step.
      * @param surface
      *            the surface to check
      * @return the building containing the surface
      */
     public final Building returnBuildingContaining3(final Surface surface) {
         for (Building building : this.islet.getBiStep3().getBuildings()) {
             BuildingStep3 buildingStep = building.getbStep3();
             if (buildingStep.getInitialTotalSurface() == surface) {
                 return building;
             }
         }
         return null;
     }
 
     /**
      * Builds the step tree.
      * @return a mutable tree node
      * @throws InvalidCaseException
      *             if an invalid case has been called
      */
     public final DefaultMutableTreeNode returnNode()
             throws InvalidCaseException {
         return this.islet.returnNode();
     }
 
     /**
      * Searches in the list of buildings for the building which contains the
      * triangles selected.
      * @param trianglesSelected
      *            the triangles
      * @return the building containing <strong>all</strong> the triangles
      */
     private Building searchForBuildingContaining4(
             final List<Triangle> trianglesSelected) {
         for (Building building : this.islet.getBiStep4().getBuildings()) {
             BuildingStep4 buildingStep = building.getbStep4();
             if (buildingStep.getInitialWallSurface().getMesh()
                     .contains(trianglesSelected.get(0))
                     || buildingStep.getInitialRoofSurface().getMesh()
                             .contains(trianglesSelected.get(0))) {
                 return building;
             }
         }
         return null;
     }
 
     /**
      * Searches in the list of the buildings the one which contains all the
      * surfaces selected.
      * @param surfacesSelected
      *            the list of surfaces
      * @return the building containing <strong>all</strong> these surfaces
      */
     private Building searchForBuildingContaining5(
             final List<Surface> surfacesSelected) {
 
         for (Building building : this.islet.getBiStep5().getBuildings()) {
             BuildingStep5 buildingStep = building.getbStep5();
             if (buildingStep.getWalls().contains(surfacesSelected.get(0))
                     || buildingStep.getRoofs()
                             .contains(surfacesSelected.get(0))) {
                 return building;
             }
         }
         return null;
     }
 
     /**
      * Searches for the building containing the surface.
      * @param surface
      *            the surface
      * @return the building containing this surface
      */
     private Building searchForBuildingContaining6(final Surface surface) {
         for (Building building : this.islet.getBiStep6().getBuildings()) {
 
             if (building.getbStep6().getWalls().contains(surface)
                     || building.getbStep6().getRoofs().contains(surface)) {
                 return building;
             }
         }
 
         return null;
     }
 
     /**
      * Setter.
      * @param groundNormal
      *            the ground normal
      */
     public final void setGroundNormal(final Vector3d groundNormal) {
         this.islet.setGroundNormal(groundNormal);
     }
 
     /**
      * Setter.
      * @param isletIn
      *            the new buildings islet
      */
     public final void setIslet(final AbstractBuildingsIslet isletIn) {
         this.islet = isletIn;
     }
 
     /**
      * Setter.
      * @param u3dcontrollerIn
      *            the universe 3D controller
      */
     public final void setUniverse3DController(
             final Universe3DController u3dcontrollerIn) {
         this.u3DController = u3dcontrollerIn;
     }
 
     /**
      * Displays the zero step.
      */
     public final void viewStep0() {
 
         List<Surface> surfacesList = new ArrayList<>();
         surfacesList.add(this.islet.getBiStep0().getInitialTotalSurface());
 
         this.getU3DController().getUniverse3DView().addSurfaces(surfacesList);
 
     }
 
     /**
      * Displays the first step.
      */
     public final void viewStep1() {
 
         List<Surface> surfacesList = new ArrayList<>();
         surfacesList.add(this.islet.getBiStep1()
                 .getInitialTotalSurfaceAfterBaseChange());
 
         this.getU3DController().getUniverse3DView().addSurfaces(surfacesList);
     }
 
     /**
      * Displays the second step.
      * @throws WeirdResultException
      *             if the result is not coherent
      */
     public final void viewStep2() throws WeirdResultException {
         List<Surface> surfacesList = new ArrayList<>();
 
         if (!this.islet.getBiStep2().getInitialBuildings().getMesh().isEmpty()) {
             surfacesList.add(this.islet.getBiStep2().getInitialBuildings());
         } else {
             throw new WeirdResultException(FileTools.readInformationMessage(
                     WeirdResultMessages.NO_BUILDINGS,
                     TextsKeys.MESSAGETYPE_MESSAGE));
         }
         if (!this.islet.getBiStep2().getInitialGrounds().getMesh().isEmpty()) {
             surfacesList.add(this.islet.getBiStep2().getInitialGrounds());
         } else {
             throw new WeirdResultException(FileTools.readInformationMessage(
                     WeirdResultMessages.NO_GROUND,
                     TextsKeys.MESSAGETYPE_MESSAGE));
         }
 
         this.getU3DController().getUniverse3DView().addSurfaces(surfacesList);
     }
 
     /**
      * Displays the third step.
      * @throws WeirdResultException
      *             if the result is not coherent
      */
     public final void viewStep3() throws WeirdResultException {
         List<Surface> surfacesList = new ArrayList<>();
 
         if (!this.islet.getBiStep3().getGrounds().getMesh().isEmpty()) {
             surfacesList.add(this.islet.getBiStep2().getInitialGrounds());
         } else {
             throw new WeirdResultException(FileTools.readInformationMessage(
                     WeirdResultMessages.NO_GROUND,
                     TextsKeys.MESSAGETYPE_MESSAGE));
         }
 
         for (Building building : this.islet.getBiStep3().getBuildings()) {
             surfacesList.add(building.getbStep3().getInitialTotalSurface());
         }
 
         if (!this.islet.getBiStep3().getNoise().getMesh().isEmpty()) {
             surfacesList.add(this.islet.getBiStep3().getNoise());
         }
 
         this.getU3DController().getUniverse3DView().addSurfaces(surfacesList);
     }
 
     /**
      * Displays the fourth step.
      * @throws WeirdResultException
      *             if the result is not coherent
      */
     public final void viewStep4() throws WeirdResultException {
         List<Surface> surfacesList = new ArrayList<>();
 
         for (Building building : this.islet.getBiStep4().getBuildings()) {
             surfacesList.add(building.getbStep4().getInitialWallSurface());
             surfacesList.add(building.getbStep4().getInitialRoofSurface());
         }
 
         if (this.islet.getBiStep4().getNoise().getMesh() != null
                 && !this.islet.getBiStep4().getNoise().getMesh().isEmpty()) {
             surfacesList.add(this.islet.getBiStep4().getNoise());
         } else {
             throw new WeirdResultException(
                     FileTools.readInformationMessage(
                             WeirdResultMessages.NO_NOISE,
                             TextsKeys.MESSAGETYPE_MESSAGE));
         }
 
         this.getU3DController().getUniverse3DView().addSurfaces(surfacesList);
     }
 
     /**
      * Displays the fifth step.
      * @throws WeirdResultException
      *             if the result is not coherent
      */
     public final void viewStep5() throws WeirdResultException {
         List<Surface> surfacesList = new ArrayList<>();
 
         for (Building building : this.islet.getBiStep5().getBuildings()) {
             BuildingStep5 buildingStep = building.getbStep5();
 
             for (Surface wall : buildingStep.getWalls()) {
                 surfacesList.add(wall);
             }
             for (Surface roof : buildingStep.getRoofs()) {
                 surfacesList.add(roof);
             }
         }
 
         if (this.islet.getBiStep5().getNoise().getMesh() != null
                 && !this.islet.getBiStep5().getNoise().getMesh().isEmpty()) {
             surfacesList.add(this.islet.getBiStep5().getNoise());
         } else {
             throw new WeirdResultException(
                     FileTools.readInformationMessage(
                             WeirdResultMessages.NO_NOISE,
                             TextsKeys.MESSAGETYPE_MESSAGE));
         }
 
         this.getU3DController().getUniverse3DView().addSurfaces(surfacesList);
     }
 
     /**
      * Displays the sixth step.
      */
     public final void viewStep6() {
         List<Surface> surfacesList = new ArrayList<>();
 
         for (Building building : this.islet.getBiStep6().getBuildings()) {
             BuildingStep6 buildingStep = building.getbStep6();
 
             for (Surface wall : buildingStep.getWalls()) {
                 surfacesList.add(wall);
             }
 
             for (Surface roof : buildingStep.getRoofs()) {
                 surfacesList.add(roof);
             }
         }
 
         surfacesList.add(this.islet.getBiStep6().getGrounds());
 
         this.getU3DController().getUniverse3DView().addSurfaces(surfacesList);
     }
 
     public final void saveFinalResults(final String fileName) {
         this.islet.saveFinalResults(fileName);
     }
 }
