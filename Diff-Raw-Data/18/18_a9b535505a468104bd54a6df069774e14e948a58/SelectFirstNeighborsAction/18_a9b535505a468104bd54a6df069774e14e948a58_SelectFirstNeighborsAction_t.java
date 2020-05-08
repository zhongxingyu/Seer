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
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
 
 import cytoscape.util.CytoscapeAction;
 import cytoscape.Cytoscape;
import cytoscape.CyNetwork;
import giny.model.Node;
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
      CyNetwork currentNetwork = Cytoscape.getCurrentNetwork();
      Set flaggedNodes = currentNetwork.getFlaggedNodes();
      Set firstNeighbors = new HashSet();
      for (Iterator nodeIt = flaggedNodes.iterator();nodeIt.hasNext();){
	Node currentNode = (Node)nodeIt.next();
	for(Iterator neighborIt = currentNetwork.neighborsList(currentNode).iterator();neighborIt.hasNext();){
	  firstNeighbors.add(neighborIt.next());
	}
      }
      currentNetwork.setFlaggedNodes(firstNeighbors,true);
     } // actionPerformed
 } // SelectFirstNeighborsAction
 
