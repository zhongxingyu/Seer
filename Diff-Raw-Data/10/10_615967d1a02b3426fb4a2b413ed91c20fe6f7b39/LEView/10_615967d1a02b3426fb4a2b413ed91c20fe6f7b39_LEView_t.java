 
 package vooga.scroller.level_editor.view;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyEvent;
 import javax.swing.AbstractAction;
 import javax.swing.JMenu;
 import javax.swing.KeyStroke;
 import vooga.scroller.level_editor.LevelEditing;
 import vooga.scroller.level_editor.controllerSuite.LEController;
 import vooga.scroller.level_editor.controllerSuite.LEGrid;
 import vooga.scroller.level_editor.controllerSuite.LETools;
 import vooga.scroller.util.Renderable;
 import vooga.scroller.util.mvc.vcFramework.Window;
 
 /**
  * This class is a specialized Window (using the vcFramework) for LevelEditing.
  * As such it has the type parameters:
  * <li> LEWorkspaceView as the default workspace supported. </li>
  * <li> LevelEditing as the Domain Specific main resource. </li>
  * <li> LEGridView as the main renderer.</li>
  * <li> LETools as the toolbox that can be used to operate on the workspace items.</li>
  * @author Dagbedji Fagnisse
  */
 public class LEView extends Window<LEWorkspaceView, LevelEditing, LEGridView, LETools> {
 
 
     private static final long serialVersionUID = 1L;
     private static final String SIMULATION_ERROR_MESSAGE = 
            LevelEditing.SIMULATION_ERROR_MESSAGE;
     private static final String TITLE = 
             LevelEditing.VIEW_CONSTANTS.DOMAIN_NAME;
     private static final String UPDATE_GRID_SIZE = 
             LevelEditing.UPDATE_GRID_SIZE;
     
 
     /**
      * Default constructor - build a Window with the specified language and controller.
      * @param language - to be used for the GUI (and maybe domain keywords).
      * @param lEController - Specialized controller for level Editing.
      */
     public LEView (String language, LEController lEController, LETools t) {
         super(TITLE, language, lEController, t);
         registerMenu(makeSimulateMenu());
         registerMenu(makePreferencesMenu());
     }
 
     @Override
     public LEWorkspaceView initializeWorkspaceView (int id, Renderable<LevelEditing> r) {
         LEWorkspaceView res = new LEWorkspaceView(this, id, r, getTools());
         return res;
     }
 
 
 
     /**
      * Simulate the existing workspace data
      */
     public void simulate () {
         if (super.getActiveTab() instanceof LEWorkspaceView) {
             simulate((LEWorkspaceView)super.getActiveTab());
         }
     }
     
     /**
      * Update the size of the current grid
      */
     public void updateCurrentGridSize () {
         if (super.getActiveTab() instanceof LEWorkspaceView) {
             updateGridSize((LEWorkspaceView)super.getActiveTab());
         }
     }
 
 
     private void updateGridSize (LEWorkspaceView activeTab) {
         ((LEGridView) activeTab.getEditableRenderer()).updateGridSize();
     }
 
     /**
      * Get the active tab and simulate it if it is valid
      * @param tab
      */
     private void simulate (LEWorkspaceView tab) {
         if (tab.isValidForSimulation()) {
             ((LEGrid) tab.getRenderable()).simulate();
         }
         else 
             showMessageDialog(SIMULATION_ERROR_MESSAGE);
     }
 
     @Override
     public LevelEditing getDomain () {
         return new LevelEditing(); //TODO
     }
 
     public class SimulateAction extends AbstractAction {
         
         public SimulateAction () {
             super(getLiteral("SimulateMenu"));
             putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                                      KeyEvent.VK_F5, ActionEvent.ALT_MASK));
         }
 
         @Override
         public void actionPerformed (ActionEvent e) {
             simulate();
         }
     }
 
     private JMenu makeSimulateMenu () {
         JMenu result = new JMenu(getLiteral("SimulateMenu"));
         result.setMnemonic(KeyEvent.VK_F2);
         result.add(new SimulateAction());
         result.setEnabled(false);
         return result;
     }
     
 
     public class UpdateGridSizeAction extends AbstractAction {
         public UpdateGridSizeAction () {
             super(UPDATE_GRID_SIZE);
             putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                                      KeyEvent.VK_G, ActionEvent.ALT_MASK));
         }
             
         @Override
         public void actionPerformed (ActionEvent e) {
             updateCurrentGridSize();
         }
 
     }
     
     /**
      * This menu handles actions that apply primarily to the current domain-specific
      * Renderable. 
      * @return
      */
     protected JMenu makePreferencesMenu () {
         JMenu result = new JMenu(getLiteral("PreferencesMenu"));
         result.setMnemonic(KeyEvent.VK_P);
         result.add(new UpdateGridSizeAction());
         result.setEnabled(false);
         return result;
     }
     
 
 
 }
