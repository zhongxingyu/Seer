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
        GinyUtils.hideSelectedNodes( Cytoscape.getCurrentNetworkView() );
        GinyUtils.hideSelectedEdges( Cytoscape.getCurrentNetworkView() );
     }//action performed
 }
 
