 //-------------------------------------------------------------------------
 // $Revision$
 // $Date$
 // $Author$
 //-------------------------------------------------------------------------
 package cytoscape.actions;
 //-------------------------------------------------------------------------
 import java.awt.event.ActionEvent;
 import javax.swing.AbstractAction;
 
 import giny.model.*;
 import giny.view.*;
 import java.util.*;
 
 import cytoscape.Cytoscape;
 import cytoscape.util.CytoscapeAction;
 //-------------------------------------------------------------------------
 public class HideSelectedAction extends CytoscapeAction  {
 
     public HideSelectedAction () {
         super( "Hide Selected" );
         setPreferredMenu( "Select.Edges" );
     }
 
    public HideSelectedAction ( boolean label) {
         super(  );
         
     }
     
     public void actionPerformed (ActionEvent e) {
        // Get the selected nodes:
    		Set selectedNodes = Cytoscape.getCurrentNetwork().getFlaggedNodes();
    		Set selectedEdges = Cytoscape.getCurrentNetwork().getFlaggedEdges();
    		GinyUtils.hideSelectedNodes( Cytoscape.getCurrentNetworkView() );
    		GinyUtils.hideSelectedEdges( Cytoscape.getCurrentNetworkView() );
        // unselect the nodes and edges
        if(selectedNodes != null){
        		Cytoscape.getCurrentNetwork().setFlaggedNodes(selectedNodes, false);
        }
        
        if(selectedEdges != null){
        		Cytoscape.getCurrentNetwork().setFlaggedEdges(selectedEdges,false);
        }
     }//action performed
 }
 
