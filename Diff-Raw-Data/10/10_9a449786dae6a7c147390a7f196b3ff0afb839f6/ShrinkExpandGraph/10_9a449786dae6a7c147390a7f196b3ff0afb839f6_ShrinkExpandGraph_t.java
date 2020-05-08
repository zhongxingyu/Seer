 // ShrinkExpandGraph plugin
 //--------------------------------------------------------------------------
 // $Revision$
 // $Date$
 // $Author$
 //--------------------------------------------------------------------------
 package cytoscape.dialogs;
 //--------------------------------------------------------------------------
 import java.awt.event.*;
 //import java.awt.Color;
 import javax.swing.*;
 import cytoscape.*;
 import java.util.HashMap;
 
 import y.base.*;
 import y.view.*;
 import y.algo.*;
 import y.geom.*;
 import y.layout.*;
 
 import cytoscape.data.*;
 //--------------------------------------------------------------------------
 //
 // this class shifts the nodes to shrink or expand the graph:
 //    it averages the coordinates of all the nodes to find the center
 //    it translates the graph to a center at (0,0)
 //    it multiplies each node coordinate by a factor m
 //    it translates the graph back to the original center
 //
 public class ShrinkExpandGraph extends AbstractAction {
     protected CytoscapeWindow cytoscapeWindow;
     protected double m;
     ShrinkExpandGraph(CytoscapeWindow cytoscapeWindow, String change, double m) {
 	super (change);
 	this.cytoscapeWindow = cytoscapeWindow;
 	this.m = m;
     }
     public void actionPerformed (ActionEvent e) {
 	Graph2D graph = cytoscapeWindow.getGraph();
 	GraphObjAttributes edgeAttributes = cytoscapeWindow.getEdgeAttributes ();
 	Node [] nodes = graph.getNodeArray();
 	
 	// sum of coordinates
 	double sx = 0;
 	double sy = 0;
 	// coordinates of center of graph
 	double cx = 0;
 	double cy = 0;
 	// size of new graph
 	//double m = .75;
 	// coordinates with graph centered at (0,0)
 	double nx;
 	double ny;
 	
 
 	// loop through each node to add up all x and all y coordinates
 	for (int node_i=0; node_i < nodes.length; node_i++){
 	    Node node = nodes[node_i];
 	    NodeRealizer nr = graph.getRealizer(node);
 	    // get coordinates of node
 	    double ax = nr.getCenterX();
 	    double ay = nr.getCenterY();
 	    // sum up coordinates of all the nodes
 	    sx += ax;
 	    sy += ay;
 	}
 
 	// average all coordinates to find center of graph
 	cx = sx/(nodes.length + 1);
 	cy = sy/(nodes.length + 1);
 
 	// set new coordinates of each node at center (0,0), shrink, then return to
 	//  original center at (cx, cy)
 	for (int node_i=0; node_i < nodes.length; node_i++){
 	    Node node = nodes[node_i];
 	    NodeRealizer nr = graph.getRealizer(node);
 	    nr.setCenterX(m*((nr.getCenterX())-cx) + cx);
 	    nr.setCenterY(m*((nr.getCenterY())-cy) + cy);
 	}
	
        // remove bends
        EdgeCursor cursor = graph.edges();
        cursor.toFirst ();
        for (int i=0; i < cursor.size(); i++){
            Edge target = cursor.edge();
            EdgeRealizer er = graph.getRealizer(target);
            er.clearBends();
            cursor.cyclicNext();
        }
 
         cytoscapeWindow.redrawGraph();
     }//Action Performed
 
 }//ShrinkExpandGraph class
 
 
     
     
 
 	
 	
        
 
     
     
     
     
     
 
 
 	
 
 	
 
 	
