 /**
  * 
  */
 package fr.nantes1900.control.isletprocess;
 
 import java.awt.Cursor;
 import java.io.File;
 import java.util.ArrayList;
 
 import fr.nantes1900.control.BuildingsIsletController;
 import fr.nantes1900.control.GlobalController;
 import fr.nantes1900.control.display3d.Universe3DController;
 import fr.nantes1900.listener.ElementsSelectedListener;
 import fr.nantes1900.models.basis.Triangle;
 import fr.nantes1900.models.extended.Surface;
 import fr.nantes1900.view.isletprocess.IsletProcessView;
 
 /**
  * Controller of the process of an islet. This controller makes the link between
  * each elements in the window and also with the model which contains mesh
  * datas.
  * @author Camille Bouquet
  * @author Luc Jallerat
  */
 public class IsletProcessController implements ElementsSelectedListener
 {
     /**
      * Parent controller of this one.
      */
     private GlobalController             parentController;
 
     /**
      * The window containing all the needed panels to process an islet.
      */
     private IsletProcessView             ipView;
 
     private Functions3DToolbarController f3DController;
 
     /**
      * Controller of the current characteristic panel.
      */
     private CharacteristicsController    cController;
 
     /**
      * Controller of the tree showing the architecture of data in the current
      * step.
      */
     private IsletTreeController          itController;
 
     /**
      * Controller of the navigation bar which allows to abort the treatment of
      * an islet and select a new one, launch a new process to go on a further
      * step and so on.
      */
     private NavigationBarController      nbController;
 
     /**
      * Controller of the parameter panel which allows to modify parameters for
      * the next process.
      */
     private ParametersController         pController;
 
     /**
      * Controller of the 3d View which displays meshes.
      */
     private Universe3DController         u3DController;
 
     /**
      * Controller of the building islet currently under process. This one makes
      * the link with meshes data.
      */
     private BuildingsIsletController     biController;
 
     /**
      * Creates a new islet process controller to launch different processes on
      * an islet.
      * @param parentControllerIn
      *            The parent controller which makes the link with other windows.
      * @param isletFile
      *            File containing data of the selectedd islet.
      * @param biControllerIn
      *            Controller to handle the islet data.
      */
     public IsletProcessController(final GlobalController parentControllerIn,
             final File isletFile, final BuildingsIsletController biControllerIn)
     {
         this.parentController = parentControllerIn;
         this.biController = biControllerIn;
         this.cController = new CharacteristicsController(this);
         this.itController = new IsletTreeController(this);
         this.nbController = new NavigationBarController(this);
         this.pController = new ParametersController(this);
         this.f3DController = new Functions3DToolbarController(this);
         this.u3DController = new Universe3DController(this);
         this.u3DController.getUniverse3DView().setToolbar(
                 this.f3DController.getToolbar());
         this.f3DController.getToolbar().showDeselectMeshesButton(true);
         this.f3DController.getToolbar().showDeselectMeshesButton(true);
         this.f3DController.getToolbar().showLockButton(true);
         this.f3DController.getToolbar().showDeselectTrianglesButton(true);
         this.f3DController.getToolbar().showTypeDisplayButton(true);
         this.biController.setUniverse3DController(this.u3DController);
         this.biController.display();
 
         // creates the windiw with all needed panels
         this.ipView = new IsletProcessView(this.cController.getView(),
                 this.itController.getView(), this.nbController.getView(),
                 this.pController.getView(),
                 this.u3DController.getUniverse3DView());
 
         this.ipView.setVisible(true);
         this.u3DController.addElementsSelectedListener(this);
     }
 
     // FIXME : Luc, please find some correct names.
     public final void throwInTheBin()
     {
         this.ipView.dispose();
         this.ipView.setVisible(false);
 
         // TODO : if it is not a really big object, don't do this : it's kinda
         // dirty.
         this.ipView = null;
     }
 
     /**
      * Gets the building islet controller.
      * @return The current building islet controller
      */
     public final BuildingsIsletController getBiController()
     {
         return this.biController;
     }
 
     public Universe3DController getU3DController()
     {
         return this.u3DController;
     }
 
     /**
      * Launches next process.
      */
     public final void launchProcess() throws UnexistingStepException
     {
         if (this.getProgression() >= 6)
         {
             throw new UnexistingStepException();
         }
         setDefaultCharacterisitcsPanel();
         this.ipView.setCursor(new Cursor(Cursor.WAIT_CURSOR));
         this.biController.launchProcess();
         this.itController.refreshView();
         this.ipView.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
         this.nbController.getView().refreshStepTitle(this.getProgression());
        setToolbarButtons();
     }
 
     public void goToPreviousProcess() throws UnexistingStepException
     {
         if (this.getProgression() <= 1)
         {
             throw new UnexistingStepException();
         }
         this.biController.getPreviousStep();
         this.itController.refreshView();
         this.nbController.getView().refreshStepTitle(this.getProgression());
         setDefaultCharacterisitcsPanel();
        setToolbarButtons();
    }

    private void setToolbarButtons()
    {
        switch (getProgression())
        {
            case 2:
        }
     }
 
     public void abortProcess()
     {
         this.parentController.launchIsletSelection();
         this.getBiController().abortProcess();
     }
 
     private int getProgression()
     {
         return this.getBiController().getIslet().getProgression();
     }
 
     public void loadParameters()
     {
         this.pController.loadNewParameters();
     }
 
     @Override
     public void triangleDeselected(Triangle triangleSelected)
     {
         int step = this.getProgression();
         boolean empty = false;
        if (!(cController instanceof CharacteristicsController) && (step == 3
                 && f3DController.getSelectionMode().equals(
                         Functions3DToolbarController.ACTION_TRIANGLES)
                 || step == 2 || step == 4))
         {
             empty = ((AbstractCharacteristicsTrianglesController) cController)
                     .removeTriangleSelected(triangleSelected);
         }
 
         if (empty)
         {
             setDefaultCharacterisitcsPanel();
         }
     }
 
     @Override
     public final void triangleSelected(final Triangle triangleSelected)
     {
         int step = this.getProgression();
 
         // step 2 in triangles selection mode or in step 2 or 4.
         if ((step == 3
                 && f3DController.getSelectionMode().equals(
                         Functions3DToolbarController.ACTION_TRIANGLES)
                 || step == 5 || step == 6))
         {
             if ((this.cController instanceof CharacteristicsController))
             {
                 switch (this.getProgression())
                 {
                     case 2:
                         this.cController = new CharacteristicsStep2Controller(
                                 this, triangleSelected);
                     break;
                     case 3:
                         this.cController = new CharacteristicsStep3TrianglesController(
                                 this, triangleSelected);
                     break;
                     case 4:
                         this.cController = new CharacteristicsStep4Controller(
                                 this, triangleSelected);
                     break;
 
                 }
                 this.ipView.setCharacteristicsView(this.cController.getView());
             } else
             {
                 ((AbstractCharacteristicsTrianglesController) this.cController)
                         .addTriangleSelected(triangleSelected);
             }
         }
 
     }
 
     public class UnexistingStepException extends Exception
     {
         /**
          * Version ID.
          */
         private static final long serialVersionUID = 1L;
 
         public UnexistingStepException()
         {
         }
     }
 
     public void lock(boolean lock)
     {
         if (this.biController.getIslet().getProgression() == 6 && lock)
         {
             ((CharacteristicsStep6Controller) this.cController)
                     .setEnabled(true);
         } else
         {
             ((CharacteristicsStep6Controller) this.cController)
                     .setEnabled(false);
         }
 
         // TODO lock and unlock in the universe 3d and maybe the tree too.
     }
 
     @Override
     public void surfaceSelected(Surface surfaceSelected)
     {
         int step = this.getProgression();
 
         // step 3 in meshes selection mode or in step 5 or 6.
         if ((step == 3
                 && f3DController.getSelectionMode().equals(
                         Functions3DToolbarController.ACTION_MESHES)
                 || step == 5 || step == 6))
         {
             if ((this.cController instanceof CharacteristicsController))
             {
                 switch (this.getProgression())
                 {
                     case 3:
                         this.cController = new CharacteristicsStep3ElementsController(
                                 this, surfaceSelected);
                     break;
                     case 5:
                         this.cController = new CharacteristicsStep5Controller(
                                 this, surfaceSelected);
                     break;
                     case 6:
                         this.cController = new CharacteristicsStep6Controller(
                                 this, surfaceSelected,
                                 (ArrayList<Surface>) surfaceSelected
                                         .getNeighbours());
                     break;
 
                 }
                 this.ipView.setCharacteristicsView(this.cController.getView());
             } else
             {
                 ((AbstractCharacteristicsSurfacesController) this.cController)
                         .addSurfaceSelected(surfaceSelected);
             }
         }
     }
 
     @Override
     public void surfaceDeselected(final Surface surfaceSelected)
     {
         int step = this.getProgression();
         boolean empty = false;
         if ((step == 3
                 && f3DController.getSelectionMode().equals(
                         Functions3DToolbarController.ACTION_MESHES)
                 || step == 5 || step == 6))
         {
             empty = ((AbstractCharacteristicsSurfacesController) cController)
                     .removeSurfaceSelected(surfaceSelected);
         }
 
         if (empty)
         {
             setDefaultCharacterisitcsPanel();
         }
     }
 
     private void setDefaultCharacterisitcsPanel()
     {
         this.cController = new CharacteristicsController(this);
         this.ipView.setCharacteristicsView(this.cController.getView());
     }
 
     public void deselectAllSurfaces()
     {
         u3DController.deselectEverySurfaces();
     }
 
     public void refreshView()
     {
         this.biController.display();
     }
 
     public void setDisplayMode(int displayMode)
     {
         u3DController.setDisplayMode(displayMode);
         setDefaultCharacterisitcsPanel();
     }
 }
