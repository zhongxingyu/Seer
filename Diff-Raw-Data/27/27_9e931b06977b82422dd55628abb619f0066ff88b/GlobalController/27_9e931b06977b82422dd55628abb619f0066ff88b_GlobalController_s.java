 /**
  * 
  */
 package fr.nantes1900.control;
 
 import java.io.File;
 
 import fr.nantes1900.control.isletselection.IsletSelectionController;
 
 /**
  * @author Camille
  */
 public class GlobalController
 {
     /**
      * The controller of the selection window.
      */
     @SuppressWarnings("unused")
     private IsletSelectionController isletSelectionController;
 
     /**
      * Creates a new global controller which creates others global controllers.
      */
     public GlobalController()
     {
         this.isletSelectionController = new IsletSelectionController(this);
     }
 
     /**
      * Launches the treatment of an islet and opens the new window.
      * @param isletFile
      *            The file containing data of the islet to treat.
      */
    public void launchIsletTreatment(File isletFile)
     {
         System.out.println("Traitement lanc sur l'lot : "
                 + isletFile.getPath());
     }
 }
