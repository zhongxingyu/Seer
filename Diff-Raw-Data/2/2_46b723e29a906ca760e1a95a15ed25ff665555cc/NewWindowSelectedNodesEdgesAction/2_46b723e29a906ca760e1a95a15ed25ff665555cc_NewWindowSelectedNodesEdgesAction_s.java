 //-------------------------------------------------------------------------
 // $Revision$
 // $Date$
 // $Author$
 //-------------------------------------------------------------------------
 package cytoscape.actions;
 //-------------------------------------------------------------------------
 import java.util.*;
 import java.awt.event.ActionEvent;
 import javax.swing.AbstractAction;
 
 import giny.model.RootGraph;
 import giny.model.GraphPerspective;
 import giny.view.GraphView;
 
 import cytoscape.data.GraphObjAttributes;
 import cytoscape.data.CyNetwork;
 import cytoscape.view.CyWindow;
 //-------------------------------------------------------------------------
 public class NewWindowSelectedNodesEdgesAction extends AbstractAction {
     CyWindow cyWindow;
     
     public NewWindowSelectedNodesEdgesAction(CyWindow cyWindow) {
         super("Selected nodes, Selected edges");
         this.cyWindow = cyWindow;
     }
 
     public void actionPerformed(ActionEvent e) {
         //save the vizmapper catalog
         cyWindow.getCytoscapeObj().saveCalculatorCatalog();
         CyNetwork oldNetwork = cyWindow.getNetwork();
         String callerID = "NewWindowSelectedNodesEdgesAction.actionPerformed";
         oldNetwork.beginActivity(callerID);
         GraphView view = cyWindow.getView();
         int [] nodes = view.getSelectedNodeIndices();
         int[] edges = view.getSelectedEdgeIndices();
       	GraphPerspective subGraph = view.getGraphPerspective().createGraphPerspective(nodes, edges);
         GraphObjAttributes newNodeAttributes = oldNetwork.getNodeAttributes();
         GraphObjAttributes newEdgeAttributes = oldNetwork.getEdgeAttributes();
 	
         CyNetwork newNetwork = new CyNetwork(subGraph, newNodeAttributes,
                 newEdgeAttributes, oldNetwork.getExpressionData() );
         newNetwork.setNeedsLayout(true);
       
        //oldNetwork.endActivity(callerID);
         
         String title =  " selection";
         try {
             //this call creates a WindowOpened event, which is caught by
             //cytoscape.java, enabling that class to manage the set of windows
             //and quit when the last window is closed
             CyWindow newWindow = new CyWindow(cyWindow.getCytoscapeObj(),
                                               newNetwork, title);
             newWindow.showWindow();
         } catch (Exception e00) {
             System.err.println("exception when creating new window");
             e00.printStackTrace();
         }
     }
 }
 
