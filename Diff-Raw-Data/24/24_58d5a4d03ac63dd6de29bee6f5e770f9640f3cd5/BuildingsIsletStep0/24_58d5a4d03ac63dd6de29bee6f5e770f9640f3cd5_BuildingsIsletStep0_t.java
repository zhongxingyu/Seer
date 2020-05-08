 package fr.nantes1900.models.islets.buildings.steps;
 
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.vecmath.Vector3d;
 
 import fr.nantes1900.models.basis.Mesh;
import fr.nantes1900.models.islets.buildings.exceptions.NullArgumentException;
 import fr.nantes1900.models.islets.buildings.exceptions.UnCompletedParametersException;
 import fr.nantes1900.utils.MatrixMethod;
 import fr.nantes1900.utils.MatrixMethod.SingularMatrixException;
 
 /**
  * Implements a step of the treatment. This step is after the parsing and before
  * the base change.
  * @author Daniel Lefèvre
  */
 public class BuildingsIsletStep0 extends AbstractBuildingsIsletStep
 {
     /**
      * The initial mesh before every treatment.
      */
     private Mesh       initialTotalMesh;
 
     /**
      * Change base matrix from the current base to a base which is ground-like
      * oriented.
      */
     private double[][] matrix = new double[MatrixMethod.MATRIX_DIMENSION][MatrixMethod.MATRIX_DIMENSION];
 
     /**
      * The normal to the gravity.
      */
     private Vector3d   gravityNormal;
 
     /**
      * Constructor.
      * @param mesh
      *            the mesh representing the entire islet
      */
     public BuildingsIsletStep0(final Mesh mesh)
     {
         this.initialTotalMesh = mesh;
     }
 
     /**
      * Changes the base of the mesh.
      * @throws UnCompletedParametersException
      *             if the matrix or the mesh has not been initialized
      */
     public final void changeBase() throws UnCompletedParametersException
     {
         if (this.matrix == null || this.initialTotalMesh == null)
         {
             throw new UnCompletedParametersException();
         }
         this.initialTotalMesh.changeBase(this.matrix);
     }
 
     /**
      * Creates a change base matrix with the normal to the ground. See the
      * MatrixMethod class for more informations.
     * @throws NullArgumentException
      */
    public final void createChangeBaseMatrix() throws NullArgumentException
     {
         try
         {
            if (this.gravityNormal == null)
            {
                throw new NullArgumentException();
            }
             // Base change
             this.matrix = MatrixMethod.createOrthoBase(this.gravityNormal);
             MatrixMethod.changeBase(this.gravityNormal, this.matrix);
 
         } catch (final SingularMatrixException e)
         {
             System.out.println("Error : the matrix is badly formed !");
             System.exit(1);
         }
     }
 
     /**
      * Getter.
      * @return the initial total mesh
      */
     public final Mesh getInitialTotalMesh()
     {
         return this.initialTotalMesh;
     }
 
     /*
      * (non-Javadoc)
      * @see
      * fr.nantes1900.models.islets.buildings.steps.AbstractBuildingsIsletStep
      * #launchTreatment()
      */
     @Override
     public final BuildingsIsletStep1 launchTreatment()
     {
         try
         {
             this.createChangeBaseMatrix();
             this.changeBase();
         } catch (UnCompletedParametersException e)
         {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NullArgumentException e)
        {
            // TODO Auto-generated catch block
             e.printStackTrace();
         }
 
         return new BuildingsIsletStep1(this.initialTotalMesh);
     }
 
     /*
      * (non-Javadoc)
      * @see
      * fr.nantes1900.models.islets.buildings.steps.AbstractBuildingsIsletStep
      * #returnNode()
      */
     @Override
     public final DefaultMutableTreeNode returnNode()
     {
         DefaultMutableTreeNode node = new DefaultMutableTreeNode(this.initialTotalMesh);
         return node;
     }
 
     /**
      * Initialize some arguments needed for the treatement.
      * @param gravityNormalIn
      *            the normal to the gravity
      */
     public final void setArguments(final Vector3d gravityNormalIn)
     {
         this.gravityNormal = gravityNormalIn;
     }
 
 }
