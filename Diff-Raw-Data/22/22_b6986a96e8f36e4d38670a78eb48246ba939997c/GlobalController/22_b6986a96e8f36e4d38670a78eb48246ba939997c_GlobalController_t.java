 package fr.nantes1900.control;
 
 import java.io.File;
 
import javax.swing.ToolTipManager;

 import fr.nantes1900.control.isletprocess.IsletProcessController;
 import fr.nantes1900.control.isletselection.IsletSelectionController;
 import fr.nantes1900.models.islets.buildings.exceptions.WeirdResultException;
 
 /**
  * Global controller of the software.
  * @author Camille Bouquet
  * @author Luc Jallerat
  */
 public class GlobalController {
 
     /**
      * The controller of the selection window.
      */
     private IsletSelectionController isletSelectionController;
     /**
      * The controller of the islet process window.
      */
     private IsletProcessController isletProcessController;
 
     /**
      * Creates a new global controller which creates others global controllers.
      */
     public GlobalController() {
         this.isletSelectionController = new IsletSelectionController(this);
        
        // For tooltips not be hidden by Canvas 3d
        ToolTipManager ttManager = ToolTipManager.sharedInstance();
        ttManager.setEnabled(true);
        ttManager.setLightWeightPopupEnabled(false);
     }
 
     /**
      * Launches the process of an islet and opens the new window.
      * @param isletFile
      *            the file containing data of the islet to process.
      * @param biController
      *            the building islet controller which contains all needed data
      *            for the islet.
      */
     public final void launchIsletProcess(final File isletFile,
             final BuildingsIsletController biController) {
         // Launches the base change : the process 0.
         try {
             biController.launchProcess();
         } catch (WeirdResultException e) {
             // TODO by Camille : pop-up
             e.printStackTrace();
         }
 
         this.isletProcessController = new IsletProcessController(this,
                 isletFile, biController);
         this.isletSelectionController.getWindow().setVisible(false);
     }
 
     /**
      * Launches the islet selection window and closes islet process window.
      */
     public final void launchIsletSelection() {
         this.isletSelectionController = new IsletSelectionController(this);
         this.isletProcessController.disposeWindow();
     }
 }
