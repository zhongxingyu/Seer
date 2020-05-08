 //-------------------------------------------------------------------------
 // $Revision$
 // $Date$
 // $Author$
 //-------------------------------------------------------------------------
 package cytoscape.actions;
 
 //-------------------------------------------------------------------------
 
 import cytoscape.view.CyNetworkView;
 import cytoscape.Cytoscape;
 import cytoscape.util.CytoscapeAction;
 import cytoscape.visual.GlobalAppearanceCalculator;
 import cytoscape.visual.VisualMappingManager;
 import cytoscape.visual.VisualStyle;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 
 //-------------------------------------------------------------------------
 /**
  * Prompts User for New Background Color.
  */
 public class BackgroundColorAction extends CytoscapeAction {
 
    
     /**
      * Constructor.
      */
     public BackgroundColorAction () {
         super("Change Background Color");
         setPreferredMenu( "Visualization" );
         setAcceleratorCombo( java.awt.event.KeyEvent.VK_B, ActionEvent.ALT_MASK) ;
     }
 
     /**
      * Captures User Menu Selection.
      * @param ev Action Event.
      */
     public void actionPerformed(ActionEvent ev) {
         // Do this in the GUI Event Dispatch thread...
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 JColorChooser color = new JColorChooser();
                 Color newPaint = color.showDialog( 
                                                   Cytoscape.getCurrentNetworkView().getComponent(),
                                                   "Choose a Background Color",
                                                   (java.awt.Color)Cytoscape.getCurrentNetworkView().
                                                   getBackgroundPaint() );
 
             //  Update the Current Background Color
             //  and Synchronize with current Visual Style
            Cytoscape.getCurrentNetworkView().setBackgroundPaint(newPaint);
            synchronizeVisualStyle(newPaint);
             }
         });
     }//action performed
 
     /**
      * Synchronizes the New Background Color with the Current Visual Style.
      * @param newColor New Color
      */
     private void synchronizeVisualStyle(Color newColor) {
        VisualMappingManager vmm = Cytoscape.getCurrentNetworkView().getVizMapManager();
        VisualStyle style = vmm.getVisualStyle();
        GlobalAppearanceCalculator gCalc =
                style.getGlobalAppearanceCalculator();
        gCalc.setDefaultBackgroundColor(newColor);
     }
 }
