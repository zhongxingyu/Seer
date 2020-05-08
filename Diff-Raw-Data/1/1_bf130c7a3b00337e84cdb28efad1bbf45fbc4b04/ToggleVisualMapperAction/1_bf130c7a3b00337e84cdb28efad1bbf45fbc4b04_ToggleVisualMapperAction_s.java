 //-------------------------------------------------------------------------
 // $Revision$
 // $Date$
 // $Author$
 //-------------------------------------------------------------------------
 package cytoscape.actions;
 //-------------------------------------------------------------------------
 import java.awt.event.ActionEvent;
 import javax.swing.*;
 
 import cytoscape.util.CytoscapeAction;
 import cytoscape.Cytoscape;
 import cytoscape.view.CyNetworkView;
 
 /**
  * This class implements two menu items that allow enabling and disabling
  * the visual mapper attached the the CyWindow argument.
  */
 public class ToggleVisualMapperAction extends CytoscapeAction {
 
     public ToggleVisualMapperAction () {
         super("Disable Visual Mapper");
         setPreferredMenu( "Visualization" );
         setAcceleratorCombo( java.awt.event.KeyEvent.VK_M, ActionEvent.ALT_MASK) ;
     }
 
     public void actionPerformed(ActionEvent e) {
       //TODO: this is state information that should saved
         Cytoscape.getCurrentNetworkView().toggleVisualMapperEnabled();
     }
 }
 
