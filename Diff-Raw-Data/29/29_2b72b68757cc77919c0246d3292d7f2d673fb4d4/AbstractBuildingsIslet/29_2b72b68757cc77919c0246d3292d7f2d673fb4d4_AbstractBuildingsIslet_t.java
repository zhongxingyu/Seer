 package fr.nantes1900.models.islets;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.vecmath.Vector3d;
 
 import fr.nantes1900.models.basis.Mesh;
 import fr.nantes1900.models.exceptions.InvalidCaseException;
 import fr.nantes1900.models.exceptions.NullArgumentException;
 import fr.nantes1900.models.exceptions.WeirdPreviousResultsException;
 import fr.nantes1900.models.exceptions.WeirdResultException;
 import fr.nantes1900.models.extended.Building;
 import fr.nantes1900.models.extended.Ground;
 import fr.nantes1900.models.extended.Roof;
 import fr.nantes1900.models.extended.Wall;
 import fr.nantes1900.models.islets.steps.BuildingsIsletStep0;
 import fr.nantes1900.models.islets.steps.BuildingsIsletStep1;
 import fr.nantes1900.models.islets.steps.BuildingsIsletStep2;
 import fr.nantes1900.models.islets.steps.BuildingsIsletStep3;
 import fr.nantes1900.models.islets.steps.BuildingsIsletStep4;
 import fr.nantes1900.models.islets.steps.BuildingsIsletStep5;
 import fr.nantes1900.models.islets.steps.BuildingsIsletStep6;
 import fr.nantes1900.models.islets.steps.BuildingsIsletStep7;
 import fr.nantes1900.utils.*;
 
 /**
  * Abstracts a building islet : residential or industrial. This class contains
  * all the methods to apply the processs on the meshes.
  * @author Daniel Lefèvre
  */
 public abstract class AbstractBuildingsIslet extends AbstractIslet implements
         BuildingIsletAccess {
 
     /**
      * The zero building islet step.
      */
     private BuildingsIsletStep0 biStep0;
 
     /**
      * The first building islet step.
      */
     private BuildingsIsletStep1 biStep1;
 
     /**
      * The second building islet step.
      */
     private BuildingsIsletStep2 biStep2;
 
     /**
      * The third building islet step.
      */
     private BuildingsIsletStep3 biStep3;
 
     /**
      * The fourth building islet step.
      */
     private BuildingsIsletStep4 biStep4;
 
     /**
      * The fifth building islet step.
      */
     private BuildingsIsletStep5 biStep5;
 
     /**
      * The sixth building islet step.
      */
     private BuildingsIsletStep6 biStep6;
 
     /**
      * The sixth building islet step.
      */
     private BuildingsIsletStep7 biStep7;
 
     /**
      * Getter.
      * @return the seventh step
      */
     public final BuildingsIsletStep7 getBiStep7() {
         return this.biStep7;
     }
 
     /**
      * The number of the step 0.
      */
     public static final int ZERO_STEP = 0;
 
     /**
      * The number of the step 1.
      */
     public static final int FIRST_STEP = 1;
 
     /**
      * The number of the step 2.
      */
     public static final int SECOND_STEP = 2;
 
     /**
      * The number of the step 3.
      */
     public static final int THIRD_STEP = 3;
 
     /**
      * The number of the step 4.
      */
     public static final int FOURTH_STEP = 4;
 
     /**
      * The number of the step 5.
      */
     public static final int FIFTH_STEP = 5;
 
     /**
      * The number of the step 6.
      */
     public static final int SIXTH_STEP = 6;
 
     /**
      * The number of the step 7.
      */
     public static final int SEVENTH_STEP = 7;
 
     /**
      * The number of the current step.
      */
     private int progression = 0;
 
     /**
      * The normal to the ground. Used to extract the grounds.
      */
     private Vector3d groundNormal;
 
     /**
      * The normal to the gravity.
      */
     private Vector3d gravityNormal;
     
     /**
      * Constructor. Saves the mesh in the initialTotalMesh variable.
      */
     public AbstractBuildingsIslet() {
     }
 
     /**
      * Decrementer.
      */
     public final void decProgression() {
         this.progression--;
     }
 
     /**
      * Getter.
      * @return the zero step
      */
     public final BuildingsIsletStep0 getBiStep0() {
         return this.biStep0;
     }
 
     /**
      * Getter.
      * @return the first step
      */
     public final BuildingsIsletStep1 getBiStep1() {
         return this.biStep1;
     }
 
     /**
      * Getter.
      * @return the second step
      */
     public final BuildingsIsletStep2 getBiStep2() {
         return this.biStep2;
     }
 
     /**
      * Getter.
      * @return the third step
      */
     public final BuildingsIsletStep3 getBiStep3() {
         return this.biStep3;
     }
 
     /**
      * Getter.
      * @return the fourth step
      */
     public final BuildingsIsletStep4 getBiStep4() {
         return this.biStep4;
     }
 
     /**
      * Getter.
      * @return the fifth step
      */
     public final BuildingsIsletStep5 getBiStep5() {
         return this.biStep5;
     }
 
     /**
      * Getter.
      * @return the sixth step
      */
     public final BuildingsIsletStep6 getBiStep6() {
         return this.biStep6;
     }
 
     @Override
     public final List<Building> getBuildings() {
         List<Building> buildings = new ArrayList<>();
         switch (this.getProgression()) {
         case AbstractBuildingsIslet.THIRD_STEP:
             buildings.addAll(this.biStep3.getBuildings());
             break;
         case AbstractBuildingsIslet.FOURTH_STEP:
             buildings.addAll(this.biStep4.getBuildings());
             break;
         case AbstractBuildingsIslet.FIFTH_STEP:
             buildings.addAll(this.biStep5.getBuildings());
             break;
         case AbstractBuildingsIslet.SIXTH_STEP:
             buildings.addAll(this.biStep6.getBuildings());
             break;
         case AbstractBuildingsIslet.SEVENTH_STEP:
             buildings.addAll(this.biStep7.getBuildings());
             break;
         default:
         }
 
         return buildings;
     }
 
     /**
      * Getter.
      * @return the gravity normal
      */
     public final Vector3d getGravityNormal() {
         return this.gravityNormal;
     }
 
     @Override
     public final Ground getGround() {
         Ground ground = null;
         switch (this.getProgression()) {
         case AbstractBuildingsIslet.SECOND_STEP:
             ground = this.biStep2.getInitialGrounds();
             break;
         case AbstractBuildingsIslet.THIRD_STEP:
             ground = this.biStep3.getGrounds();
             break;
         case AbstractBuildingsIslet.FOURTH_STEP:
             ground = this.biStep4.getGrounds();
             break;
         case AbstractBuildingsIslet.FIFTH_STEP:
             ground = this.biStep5.getGrounds();
             break;
         case AbstractBuildingsIslet.SIXTH_STEP:
             ground = this.biStep6.getGrounds();
             break;
         case AbstractBuildingsIslet.SEVENTH_STEP:
             ground = this.biStep7.getGrounds();
             break;
         default:
         }
         return ground;
     }
 
     /**
      * Getter.
      * @return the normal to the ground
      */
     public final Vector3d getGroundNormal() {
         return this.groundNormal;
     }
 
     /**
      * Getter.
      * @return the progression of the process
      */
     public final int getProgression() {
         return this.progression;
     }
 
     /**
      * Incrementer. Used to increment the progression of the treamtent.
      */
     public final void incProgression() {
         this.progression++;
     }
 
     /**
      * Launches the first process.
      * @throws NullArgumentException
      *             if the gravity normal has not been initiliazed
      * @throws WeirdResultException
      *             if the result is not coherent
      */
     public final void launchProcess0() throws NullArgumentException,
             WeirdResultException {
         if (this.gravityNormal == null) {
             throw new NullArgumentException();
         }
 
         this.getBiStep0().setArguments(this.getGravityNormal());
         this.biStep1 = this.getBiStep0().launchProcess();
         MatrixMethod.changeBase(this.getGroundNormal(), this.getBiStep0()
                 .getMatrix());
     }
 
     /**
      * Launches the first process.
      * @throws NullArgumentException
      *             if an argument needed for the process has not been
      *             initialized
      */
     public final void launchProcess1() throws NullArgumentException {
         this.getBiStep1().setArguments(this.getGroundNormal());
         this.biStep2 = this.getBiStep1().launchProcess();
     }
 
     /**
      * Launches the second process.
      */
     public final void launchProcess2() {
         this.biStep3 = this.getBiStep2().launchProcess();
     }
 
     /**
      * Launches the third process.
      * @throws NullArgumentException
      *             if an argument needed for the process has not been
      *             initialized
      */
     public final void launchProcess3() throws NullArgumentException {
         if (this.gravityNormal == null || this.groundNormal == null
                 || this.biStep2.getNoise() == null) {
             throw new NullArgumentException();
         }
 
         for (Building b : this.biStep3.getBuildings()) {
             b.setArguments(this.getGroundNormal(), this.getGravityNormal(),
                     this.biStep2.getInitialGrounds(), this.biStep2.getNoise());
         }
 
         this.biStep4 = this.biStep3.launchProcess();
     }
 
     /**
      * Launches the fourth process.
      * @throws NullArgumentException
      *             if an argument needed for the process has not been
      *             initialized
      */
     public final void launchProcess4() throws NullArgumentException {
         this.biStep4.setArguments(this.groundNormal);
         this.biStep5 = this.getBiStep4().launchProcess();
     }
 
     /**
      * Launches the fifth process.
      * @throws NullArgumentException
      *             if an argument needed for the process has not been
      *             initialized
      */
     public final void launchProcess5() throws NullArgumentException {
         this.biStep5.setArguments(this.biStep4.getGrounds(), this.groundNormal);
         this.biStep5.setArguments(this.biStep4.getNoise());
         this.biStep6 = this.getBiStep5().launchProcess();
     }
 
     /**
      * Launches the sixth process.
      * @throws NullArgumentException
      *             if an argument needed for the process has not been
      *             initialized
      * @throws WeirdPreviousResultsException
      *             if the previous step results are not what the current process
      *             expects.
      */
     public final void launchProcess6() throws NullArgumentException,
             WeirdPreviousResultsException {
         // before lanching process, checks if all buildings have been simplified
 
 //        for (Building b : this.biStep6.getBuildings()) {
 //            for (Wall w : b.getbStep6().getWalls()) {
 //                if (w.getPolygon() == null || w.getPolygon().isEmpty()) {
 //                    throw new WeirdPreviousResultsException(
 //                            FileTools.readInformationMessage(
 //                                    WeirdResultMessages.NOT_EVERY_WALL_SIMPL,
 //                                    TextsKeys.MESSAGETYPE_MESSAGE));
 //                }
 //            }
 //        }
 
         this.biStep7 = this.getBiStep6().launchProcess();
     }
 
     /**
      * Sets the progression to 0.
      */
     public final void resetProgression() {
         this.progression = 0;
     }
 
     /**
      * Returns a node containing the tree depending of the progression of the
      * process.
      * @return the node
      * @throws InvalidCaseException
      *             if the case in not valid (more than 8 or less than 0)
      */
     public final DefaultMutableTreeNode returnNode()
             throws InvalidCaseException {
         switch (this.getProgression()) {
         case AbstractBuildingsIslet.ZERO_STEP:
             throw new InvalidCaseException();
         case AbstractBuildingsIslet.FIRST_STEP:
             return this.biStep1.returnNode();
         case AbstractBuildingsIslet.SECOND_STEP:
             return this.biStep2.returnNode();
         case AbstractBuildingsIslet.THIRD_STEP:
             return this.biStep3.returnNode();
         case AbstractBuildingsIslet.FOURTH_STEP:
             return this.biStep4.returnNode();
         case AbstractBuildingsIslet.FIFTH_STEP:
             return this.biStep5.returnNode();
         case AbstractBuildingsIslet.SIXTH_STEP:
             return this.biStep6.returnNode();
         case AbstractBuildingsIslet.SEVENTH_STEP:
             return this.biStep7.returnNode();
         default:
             return null;
         }
     }
 
     /**
      * Saves the results in the file (erase the file if it already exists).
      * Saves the polygons of the surface which have one (it means which have
      * been simplified) or saves the meshes of the other surfaces.
      * @param fileName
      *            the name of the file
      */
     public final void saveFinalResults(final String fileName, int writerType) {
         AbstractWriter writer = null;
         
        if (writerType == AbstractWriter.CITYGML_WRITER) {
             writer = new CityGMLWriter(fileName, this.getBiStep7());
        } else if (writerType == AbstractWriter.STL_WRITER) {
             writer = new STLWriter(fileName, this.getBiStep7());
         }
         
         writer.makeFileFromWritable();
         
         writer.write();
     }
 
     /**
      * Setter.
      * @param biStep0In
      *            the new step 0
      */
     public final void setBiStep0(final BuildingsIsletStep0 biStep0In) {
         this.biStep0 = biStep0In;
     }
 
     /**
      * Setter.
      * @param gravityNormalIn
      *            the new gravity normal
      */
     public final void setGravityNormal(final Vector3d gravityNormalIn) {
         this.gravityNormal = gravityNormalIn;
     }
 
     /**
      * Setter.
      * @param groundNormalIn
      *            the normal to set as ground normal
      */
     public final void setGroundNormal(final Vector3d groundNormalIn) {
         this.groundNormal = groundNormalIn;
     }
 }
