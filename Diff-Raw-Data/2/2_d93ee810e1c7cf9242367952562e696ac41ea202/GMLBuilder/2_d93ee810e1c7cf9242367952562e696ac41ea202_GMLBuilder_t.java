 package autograph;
 
 import java.util.ArrayList;
 
 import autograph.Edge.Direction;
 
 public class GMLBuilder {
    private StringBuilder vGML;
    
    GMLBuilder(StringBuilder gml){
       vGML = gml;
    }
    
    public StringBuilder mGetGML(){
       return vGML;
    }
    
    /** Builds the gml text into the vGML string
     * 
     * @param graph - the graph to convert into GML
     */
    public void mBuildGML(Graph graph){
      vGML.append("Creator \"ProjectAG\" \n");
       vGML.append("Version 0.5 \n");
       if(graph != null){
          vGML.append("graph \n [\n");
          if(!graph.mGetTitle().isEmpty()){
             vGML.append("\tlabel \"");
             vGML.append(graph.mGetTitle() + "\"\n");
          }
          
          //Build the GML text for each node (at the graph level of the GML)
          ArrayList<Node> nodes = graph.mGetNodeList();
          for(int i=0; i<nodes.size(); i++){
             mBuildNodeAttribute(nodes.get(i));
          }
          
          //Build the GML text for each edge (at the graph level of the GML)
          ArrayList<Edge> edges = graph.mGetEdgeList();
          for(int i =0; i<edges.size(); i++){
             mBuildEdgeAttribute(edges.get(i));
          }
          
          //Don't forget the closing bracket
          vGML.append("] \n");
       }
    }
    
    /**
     * Appends a node's attribute to the vGML text variable
     * @param node - the node whose attributes will be appended to the gml
     */
    public void mBuildNodeAttribute(Node node){
       //make sure the node is a valid node.
       if(node != null){
          vGML.append("\tnode \n\t[ \n");
          
          //append the node id
          if(!node.mGetId().isEmpty()){
             vGML.append("\t\tid \"" + node.mGetId() + "\"\n");
          }
          
          //append the graphics (at the node level of the GML)
          mBuildNodeGraphicsAttribute(node);
          
          //append the label graphics (at the node level of the GML)
          mBuildNodeLabelGraphicsAttribute(node);
          
          //Don't forget the closing bracket
          vGML.append("\t] \n");
       }
    }
    
    /**
     * appends a node's graphic attributes to the vGML text variable
     * @param node - the node whose attributes will be appended to the gml
     */
    public void mBuildNodeGraphicsAttribute(Node node){
       vGML.append("\t\tgraphics \n \t\t[ \n");
       
       //append the x and y coordinates
       vGML.append("\t\t\tx \"" + node.mGetCenterX() + "\"\n");
       vGML.append("\t\t\ty \"" + node.mGetCenterY() + "\"\n");
       
       //append the width and height values
       vGML.append("\t\t\tw \"" + node.mGetWidth() + "\"\n");
       vGML.append("\t\t\th \"" + node.mGetHeight() + "\"\n");
       
       //append the shape
       vGML.append("\t\t\ttype \"" + node.mGetShape().toString().toLowerCase() + "\"\n");
       
       //TODO: implement code for color conversion and appending
       
       //append the outline style
       vGML.append("\t\t\toutlineStyle \"" + node.mGetStyle().toString().toLowerCase() + "\"\n");
       
       //Don't forget the closing bracket
       vGML.append("\t\t] \n");
    }
    
    /**
     * appends a node's label graphic attributes to the vGML text variable
     * @param node - the node whose attributes will be appended to the gml
     */
    public void mBuildNodeLabelGraphicsAttribute(Node node){
       vGML.append("\t\tLabelGraphics \n \t\t[ \n");
       
       //append the text
       if(node.mGetLabel() != null){
          vGML.append("\t\t\ttext \"" + node.mGetLabel() + "\"\n");
       }
       
       //TODO: implement code for color conversion and appending
       
       //append font data
       if(node.mGetFont() != null){
          vGML.append("\t\t\tfontSize \"" + node.mGetFont().getSize() + "\"\n");
          if(node.mGetFont().isBold()){
             vGML.append("\t\t\tfontStyle \"bold\"\n");
          }
          else if (node.mGetFont().isItalic()){
             vGML.append("\t\t\tfontStyle \"italic\"\n");
          }
          else{
             vGML.append("\t\t\tfontStyle \"plain\"\n");
          }
          vGML.append("\t\t\tfontName \"" + node.mGetFont().getFontName() + "\"\n");
       }
       
       //Don't forget the closing bracket
       vGML.append("\t\t] \n");
    }
    
    /**
     * appends an edge's attributes to the vGML text variable
     * @param edge - the edge whose attributes will be appended to the gml
     */
    public void mBuildEdgeAttribute(Edge edge){
       if(edge != null){
          vGML.append("\tedge \n \t[ \n");
          
          //append source and target values
          vGML.append("\t\tsource \"" + edge.mGetStartNode().mGetId() + "\"\n");
          vGML.append("\t\ttarget \"" + edge.mGetEndNode().mGetId() + "\"\n");
          
          mBuildEdgeGraphicsAttribute(edge);
          
          mBuildEdgeLabelGraphicsAttribute(edge);
          
          //Don't forget the closing bracket
          vGML.append("\t] \n");
       }
    }
    
    /**
     * appends an edge's graphic attributes to the vgml text variable
     * @param edge - the edge whose attributes will be appended to the gml
     */
    public void mBuildEdgeGraphicsAttribute(Edge edge){
       vGML.append("\t\tgraphics \n \t\t[ \n");
       
       //append the edge style
       vGML.append("\t\t\tstyle \"" + edge.mGetEdgeStyle().toString().toLowerCase() + "\"\n");
       
       //append the direction values
       if(edge.mGetDirection() == Direction.DOUBLEDIRECTION){
          vGML.append("\t\t\tarrow \"both\"\n");
       }
       else if (edge.mGetDirection() == Direction.ENDDIRECTION){
          vGML.append("\t\t\tarrow \"last\"\n");
       }
       else if(edge.mGetDirection() == Direction.STARTDIRECTION){
          vGML.append("\t\t\tarrow \"first\"\n");
       }
       
       //append the line data
       mBuildEdgeLineAttribute(edge);
       
       //Don't forget the closing bracket
       vGML.append("\t\t] \n");
    }
    
    /**
     * appends the start and end points of the edge
     * @param edge - the edge whose attributes will be appended to the gml
     */
    public void mBuildEdgeLineAttribute(Edge edge){
       vGML.append("\t\t\tline \n \t\t\t[ \n");
       
       //append the start point
       vGML.append("\t\t\t\tpoint \n \t\t\t\t[ \n");
       vGML.append("\t\t\t\t\tx \"" + edge.mGetStartX() + "\"\n");
       vGML.append("\t\t\t\t\ty \"" + edge.mGetStartY() + "\"\n");
       vGML.append("\t\t\t\t] \n");
       
       //append the end point
       vGML.append("\t\t\t\tpoint \n \t\t\t\t[ \n");
       vGML.append("\t\t\t\t\tx \"" + edge.mGetEndX() + "\"\n");
       vGML.append("\t\t\t\t\ty \"" + edge.mGetEndY() + "\"\n");
       vGML.append("\t\t\t\t] \n");
       
       //Don't forget the closing bracket (for the line section)
       vGML.append("\t\t\t] \n");
    }
    
    /**
     * appends the edge's label graphics to the vGML text
     * @param edge - the edge whose attributes will be appended to the gml
     */
    public void mBuildEdgeLabelGraphicsAttribute(Edge edge){
       vGML.append("\t\tLabelGraphics \n\t\t [ \n");
       
       //append the edge label
       vGML.append("\t\t\ttext \"" + edge.mGetLabel() + "\"\n");
       
       //append font data
       if(edge.mGetFont() != null){
          vGML.append("\t\t\tfontSize \"" + edge.mGetFont().getSize() + "\"\n");
          if(edge.mGetFont().isBold()){
             vGML.append("\t\t\tfontStyle \"bold\"\n");
          }
          else if (edge.mGetFont().isItalic()){
             vGML.append("\t\t\tfontStyle \"italic\"\n");
          }
          else{
             vGML.append("\t\t\tfontStyle \"plain\"\n");
          }
          vGML.append("\t\t\tfontName \"" + edge.mGetFont().getFontName() + "\"\n");
       }
       
       //Don't forget the closing bracket
       vGML.append("\t\t] \n");
    }
    
 }
