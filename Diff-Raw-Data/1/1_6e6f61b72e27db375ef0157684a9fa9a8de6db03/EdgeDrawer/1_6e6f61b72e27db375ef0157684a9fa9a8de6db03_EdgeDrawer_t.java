 package autograph;
 
 import java.awt.BasicStroke;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.geom.Line2D;
 
 import autograph.Node.NodeShape;
 
 public class EdgeDrawer {
    private static Graphics vGraphics;
    private static Edge vEdge;
    private final int SELF_ARC_WIDTH = 50;
    private final int SELF_ARC_HEIGHT = 50;
   
    public EdgeDrawer(Graphics g, Edge e){
       vEdge = e;
       vGraphics = g;
       vGraphics.setColor(vEdge.mGetEdgeColor());
    }
    
    /**
     * Takes a graphics object and sets its style based on the edge's style
     * @param gr2 - the graphics object that will do the drawing at some point.
     */
    public static void mSetGraphicsStyle(Graphics2D gr2){
       switch (vEdge.mGetEdgeStyle()){
       case DOTTED:
          BasicStroke dotted =  new BasicStroke(
                1f, 
                BasicStroke.CAP_ROUND, 
                BasicStroke.JOIN_ROUND, 
                1f, 
                new float[] {2f}, 
                0f);
          gr2.setStroke(dotted);
          break;
       case DASHED:
          float dash1[] = {10.0f};
          BasicStroke dashed =
                new BasicStroke(1.0f,
                      BasicStroke.CAP_BUTT,
                      BasicStroke.JOIN_MITER,
                      10.0f, dash1, 0.0f);
          gr2.setStroke(dashed);
          break;
       case SOLID:
       default:
          break;
       }
    }
    
    /**
     * Calculates the edge label position and draws it to the panel.
     * @param g - the graphics object doing the drawing.
     * @param e - the Edge to calculate the label position for.
     * @param startX - the start point of the edge's x coordinate
     * @param startY - the start point of the edge's y coordinate
     * @param endX - the end point of the edge's x coordinate
     * @param endY - the end point of the edge's y coordinate
     */
    public static void mDrawEdgeLabelForStraightEdge(int startX, int startY, int endX, int endY){
       vGraphics.setColor(vEdge.mGetLabelColor());
 
       //calculate the label position.
       //KMW Note: this is not working correctly yet. Somtimes the edge labels are going through the middle
       //          of the edge.
       int labelX;
       int labelY;
 
       if(startX < endX){
          labelX = (startX + endX)/2 + 5;
          if(startY > endY){
             labelY = (startY + endY)/2 + 5;
          }
          else{
             labelY = (startY + endY)/2 - 5;
          }
       }
       else{
          labelX = (startX + endX)/2 - 5;
          if(startY > endY){
             labelY = (startY + endY)/2 + 5;
          }
          else{
             labelY = (startY + endY)/2 - 5;
          }
       }
       vGraphics.drawString(vEdge.mGetLabel(), labelX, labelY);
    }
    
    /**
     * Draws the arrow head given the coordinates for the center points of the start and end nodes.
     * @param g - the graphics object to draw with
     * @param startX - the beginning x coordinate
     * @param startY - the beginning y coordinate
     * @param endX - the ending x coordinate
     * @param endY - the ending y coordinate
     * @param startNode - the node the arrow is pointing at.
     * @param bDrawingToSelf - true edge starts and finishes at same node (is handled differently than other edges)
     */
    private static void mDrawArrowHead(int startX, int startY, int endX, int endY, Node startNode, Boolean bDrawingToSelf){
 		double dy = startY - endY;
 		double dx = startX - endX;
 		double theta = Math.atan2(dy, dx);
 		
 		double newStartX = (double)startX;
 		double newStartY = (double)startY;
 	    //KMW Note: Because we are passing in the center points for the nodes, we need to calculate the
 	    //          the intersection of the edge and the node for each shape type and draw the arrow there.
 		if(!bDrawingToSelf){
 			
 			//change the startX and startY components to be the points where the edge intersects with the node.
 			double widthOffset = 0;
 			double heightOffset = 0;
 			int width = 0;
 			switch(startNode.mGetShape()){
 				case CIRCLE:
 				case OVAL:
 					width = startNode.mGetWidth();
 					widthOffset = (width/2) * Math.cos(theta);
 					heightOffset = (width/2) * Math.sin(theta);
 					newStartX = startX - widthOffset;
 					newStartY = startY - heightOffset;
 					break;
 				case SQUARE:
 				case RECTANGLE:
 					//KMW Note: We handle squares by looking for each edge and calculating
 					//          a vertical and horizontal offset for each.
 					width = startNode.mGetWidth();
 					double degTheta = Math.toDegrees(theta);
 				  	if((degTheta < 0 && degTheta >= -45) || (degTheta >= 0 && degTheta <= 45)){
 				  		//we are on the left edge
 				  		widthOffset = (width/2);
 				  		heightOffset = (width/2) * Math.tan(theta);
 				  		newStartX = startX - widthOffset;
 				  		newStartY = startY - heightOffset;
 				  	}
 				  	else if((degTheta <= - 135 && degTheta >= -180) || (degTheta <= 180 && degTheta >= 135)){
 				  		//we are on the right edge.
 				  		widthOffset = (width/2);
 				  		heightOffset = (width/2) * Math.tan(theta);
 				  		newStartX = startX + widthOffset;
 				  		newStartY = startY + heightOffset;
 				  	}
 				  	else if((degTheta < 135 && degTheta > 45)){
 				  		//we are on the top edge
 				  		heightOffset = (width/2);
 				  		widthOffset = (width/2)/Math.tan(theta);
 				  		newStartX = startX - widthOffset;
 				  		newStartY = startY - heightOffset;
 				  	}
 				  	else{
 				  		heightOffset = (width/2);
 				  		widthOffset = (width/2)/Math.tan(theta);
 				  		newStartX = startX + widthOffset;
 				  		newStartY = startY + heightOffset;
 				  	}
 					break;
 				  		
 				case TRIANGLE:
 					//blow up because I haven't done the trig to compute triangle stuff yet.
 					//(just doing circle stuff for now.)
 					width = startNode.mGetWidth();
 					widthOffset = (width/2) * Math.cos(theta);
 					heightOffset = (width/2) * Math.sin(theta);
 					newStartX = startX - widthOffset;
 					newStartY = startY - heightOffset;
 					break;
 				default:
 					break;
 			}
 		}
 	  //otherwise mDrawArrowHead has been called with the points of intersection already
 	  
 	  
 		Graphics2D g2 = (Graphics2D)(vGraphics);
 		double phi = Math.toRadians(40);
 		int barb = 20;
 		  
 		  
 		double x, y, rho = theta + phi;
 		for(int j = 0; j < 2; j++){
 		   x = newStartX - barb * Math.cos(rho);
 		   y = newStartY - barb * Math.sin(rho);
 		   g2.draw(new Line2D.Double(newStartX, newStartY, x, y));
 		   rho = theta - phi;
 		}
    }
    
    /**
     * Calculates the optimal access points on the node to use, and draws a straight line (or arrow) between the two nodes
     * @param e  - the edge to draw
     * @param startNode - the start node
     * @param endNode - the end node
     * @param g - the graphics object to do the drawing.
     */
    public void mDrawStraightEdge(Node startNode, Node endNode){
       
       int startNodeCenterX = startNode.mGetCenterX();
       int startNodeCenterY = startNode.mGetCenterY();
       int endNodeCenterX = endNode.mGetCenterX();
       int endNodeCenterY = endNode.mGetCenterY();
 
       vEdge.mSetStartCoordinates(startNodeCenterX, startNodeCenterY);
       vEdge.mSetEndCoordinates(endNodeCenterX, endNodeCenterY);
 
       Graphics2D gr2 = (Graphics2D)vGraphics.create();
       mSetGraphicsStyle(gr2);
       gr2.drawLine(startNodeCenterX, startNodeCenterY, endNodeCenterX, endNodeCenterY);
 
       mDrawEdgeLabelForStraightEdge(startNodeCenterX, startNodeCenterY, endNodeCenterX, endNodeCenterY);
       
       //KMW Note: for now we will only support one style of arrow. (a filled in triangle)
       //          at some point we will need to support the other types.
       switch(vEdge.mGetDirection()){
       case NODIRECTION:
          //we are done. It will work
          break;
       case STARTDIRECTION:
          mDrawArrowHead(startNodeCenterX, startNodeCenterY, endNodeCenterX, endNodeCenterY, startNode, false);
          break;
       case ENDDIRECTION:
          mDrawArrowHead(endNodeCenterX, endNodeCenterY, startNodeCenterX, startNodeCenterY, endNode, false);
          break;
       case DOUBLEDIRECTION:
          mDrawArrowHead(startNodeCenterX, startNodeCenterY, endNodeCenterX, endNodeCenterY, startNode, false);
          mDrawArrowHead(endNodeCenterX, endNodeCenterY, startNodeCenterX, startNodeCenterY, endNode, false);
          break;
       }
    }
    
    /**
     * Draws a curved edge from the right point on the node to the top point on the same node.
     * startNode - the node this edge is connected to.
     */
    public void mDrawEdgeToSelf(Node startNode) {
       int nodeCenterX = startNode.mGetCenterX();
       int nodeUpperLeftY = startNode.mGetUpperLeftY();
       
       //TODO: Handle for triangles.
       
       //We want to draw a portion of an ellipse/circle that starts at the right most
       //portion of the node and intersects at the middle of the top
       //of the node.
       //
       //To accomplish this we need to draw 75% of the ellipse/circle, with the initial
       //drawing point starting at 270 degrees and drawing a total of 270 degrees. The
       //top left of this ellipse/circle will need to be at the centerX location and 
       //an appropriate distance above the upper left y portion of the node.
       
       int arcUpperLeftX = nodeCenterX;
       int arcUpperLeftY = nodeUpperLeftY - startNode.mGetHeight()/2;
       
       Graphics2D gr2 = (Graphics2D)vGraphics;
       mSetGraphicsStyle(gr2);
       
       vEdge.mSetStartCoordinates(nodeCenterX + startNode.mGetWidth()/2, startNode.mGetCenterY());
       vEdge.mSetEndCoordinates(nodeCenterX, startNode.mGetCenterY() - startNode.mGetHeight()/2);
       
       gr2.drawArc(arcUpperLeftX, arcUpperLeftY, SELF_ARC_WIDTH, SELF_ARC_HEIGHT, 270, 270);
       
       mDrawEdgeLabelForSelf();
       switch(vEdge.mGetDirection()){
          case NODIRECTION:
             break;
          case STARTDIRECTION:
             //we are going to pretend that it is an arrow coming from the direct right 
             //and call mDrawArrowHead with data to indicate this.
             mDrawArrowHead(vEdge.mGetStartX(), vEdge.mGetStartY(), vEdge.mGetStartX()+50, vEdge.mGetStartY(), startNode, true);
             break;
          case ENDDIRECTION:
             //we are going to pretend that it is an arrow coming from directly above 
             //and call mDrawArrowHead with data to indicate this.
             mDrawArrowHead(vEdge.mGetEndX(), vEdge.mGetEndY(), vEdge.mGetEndX(), vEdge.mGetEndY()-50, startNode, true);
             break;
          case DOUBLEDIRECTION:
             mDrawArrowHead(vEdge.mGetStartX(), vEdge.mGetStartY(), vEdge.mGetStartX()+50, vEdge.mGetStartY(), startNode, true);
             mDrawArrowHead(vEdge.mGetEndX(), vEdge.mGetEndY(), vEdge.mGetEndX(), vEdge.mGetEndY()-50, startNode, true);
             break;
          default:
             break;
       }
    }
    
    /**
     * Draws an edge label assuming the edge is connecting a node to itself.
     */
    private void mDrawEdgeLabelForSelf(){
       vGraphics.setColor(vEdge.mGetLabelColor());
 
       //calculate the label position.
       int labelX;
       int labelY;
       
       Node startNode = vEdge.mGetStartNode();
       
       //The center of the arc that makes up the edge is located at the upper right
       //corner of the node. So we need to find that position.
       
       int upperRightX = startNode.mGetUpperLeftX() + startNode.mGetWidth();
       int upperRightY = startNode.mGetUpperLeftY();
       
       labelX = upperRightX + SELF_ARC_WIDTH/2;
       labelY = upperRightY - SELF_ARC_HEIGHT/2;
       
       vGraphics.drawString(vEdge.mGetLabel(), labelX, labelY);
    }
    
 }
