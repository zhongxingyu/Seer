 /**
  * 
  */
 package fr.nantes1900.control.isletprocess;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JToolBar;
 
 import fr.nantes1900.view.isletprocess.NavigationBarView;
 
 /**
  * TODO.
  * @author Camille Bouquet, Luc Jallerat
  */
 public class NavigationBarController extends JToolBar
 {
     /**
      * Version ID.
      */
     private static final long      serialVersionUID = 1L;
 
     /**
      * TODO.
      */
     private NavigationBarView      nbView;
     /**
      * The parent controller.
      */
     private IsletProcessController parentController;
 
     /**
      * Constructor : defines the buttons listener and the actions.
      * @param parentControllerIn
      *            the parent controller
      */
    public NavigationBarController(final IsletProcessController parentControllerIn)
     {
         this.parentController = parentControllerIn;
         this.nbView = new NavigationBarView();
         this.nbView.getAbortButton().addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(final ActionEvent arg0)
             {
                 NavigationBarController.this.getParentController()
<<<<<<< HEAD
                        .abortTreatment();
=======
                        .getBiController()
                        .abortProcess();
>>>>>>> cf6df6c9c7af0957d190fc5faf38d508f07b4293
             }
         });
         this.nbView.getBackButton().addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(final ActionEvent arg0)
             {
                 NavigationBarController.this.getParentController()
<<<<<<< HEAD
                        .getPreviousTreatment();
=======
                        .getBiController()
                        .getPreviousStep();
>>>>>>> cf6df6c9c7af0957d190fc5faf38d508f07b4293
             }
         });
         this.nbView.getLaunchButton().addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(final ActionEvent arg0)
             {
                NavigationBarController.this.getParentController().launchProcess();
             }
         });
     }
 
     /**
      * Getter.
      * @return the navigation bar view associated
      */
     public final NavigationBarView getView()
     {
         return this.nbView;
     }
 
     /**
      * Getter.
      * @return the parent controller
      */
     public final IsletProcessController getParentController()
     {
         return this.parentController;
     }
 
 }
