 //-------------------------------------------------------------------------
 // $Revision$
 // $Date$
 // $Author$
 //-------------------------------------------------------------------------
 package cytoscape.actions;
 //-------------------------------------------------------------------------
 import java.awt.event.ActionEvent;
 import javax.swing.AbstractAction;
 import java.util.Vector;
 
 import cytoscape.util.CytoscapeAction;
 import cytoscape.Cytoscape;
 //-------------------------------------------------------------------------
 /**
  *  select every first neighbor (directly connected nodes) of the currently
  *  selected nodes.
  */
 public class SelectFirstNeighborsAction extends CytoscapeAction {
     
     public SelectFirstNeighborsAction () { 
         super ("First neighbors of selected nodes"); 
         setPreferredMenu( "Select.Nodes" );
         setAcceleratorCombo( java.awt.event.KeyEvent.VK_F6,0 );
     }
     public void actionPerformed (ActionEvent e) {
        GinyUtils.selectFirstNeighbors( Cytoscape.getCurrentNetworkView() );
     } // actionPerformed
 } // SelectFirstNeighborsAction
 
