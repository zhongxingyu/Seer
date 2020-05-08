 
 package cz.fjfi.guideme.core;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 //import java.util.UUID;
 import java.util.*;
 
 /**
  * This class represents map of GuideMe app. It provides getter and setters for
  * its attributes and basic methods for its creation.
  * @author Petr Fort
  * @author Martin Sochor 
  * @version 
  */
 public class GMMap 
 {
 
     //== CLASS CONSTANTS ===========================================================
 	 
     //== CLASS VARIABLES ===========================================================	
 	
 	//== INSTANCE VARIABLES ========================================================
     private UUID guid;  
     private String name;
     private String authorName;
     private String authorEmail;
     private String description; 
 
     private List<GMNode> mappedNodes;
     private List<GMEdge> mappedEdges;
     private List<Location> mappedLocations;
 	
     //==============================================================================
     //== CONSTRUCTORS ==============================================================
     
     /***************************************************************************
      * TODO: comment
      */
     //TODO: constructor parameters and body
     public GMMap()
     {
    	mappedNodes = new ArrayList<GMNode>();
    	mappedEdges = new ArrayList<GMEdge>();
    	mappedLocations = new ArrayList<Location>();
     }
     
     //==============================================================================
     //== GETTERS AND SETTERS =======================================================       
     
     /***************************************************************************
     // Getter of guid
     */
 	public UUID getGuid()
 	{
 		//return guid;
 		return this.guid;
 	}	
 
     /***************************************************************************
     * Getter of name of the map
     */
     public String getName()
     {
     	return this.name;
     }
     
     /***************************************************************************
     * Getter of name of the map author  
     */
     public String getAuthorName()
     {
     	return this.authorName;
     }
     
     /***************************************************************************
     * Getter of email of the map author 
     */
     public String getAuthorEmail()
     {
     	return this.authorEmail;
     }
     
     /***************************************************************************
     * Getter of description of the map
     */
     public String getDescription()
     {
     	return this.description;
     }
     
     /***************************************************************************
     * Getter for the list of nodes
     */ 
 	public List<GMNode> getNodes() {
 		return this.mappedNodes;
 	}
 	
 	/***************************************************************************
 	* Getter for the list of locations
 	*/ 
 	public List<Location> getLocations()
 	{
 		return this.mappedLocations;
 	}
 
     /***************************************************************************
     * Setter for guid
     */
 	public void setGuid(UUID newGUID)
     {
     	this.guid = newGUID;
     }
      
     /***************************************************************************
     * Setter for name of the map   
     */  
     public void setName(String newName)
     {
     	this.name = newName;
     }
      
     /***************************************************************************
     * Setter for name of author of the map  
     */
     public void setAuthorName(String newAuthorName)
     {
     	 this.authorName = newAuthorName;
     }
      
     /***************************************************************************
     * Setter for email of author of the map  
     */
     public void setAuthorEmail(String newAuthorEmail)
     {
     	this.authorEmail = newAuthorEmail;     	
     }
      
     /***************************************************************************
     * Setter for description
     */
     public void setDescription(String newDescription)
     {
      	this.description = newDescription;    	
     }
       
      //============================================================================== 
      //== OTHER METHODS =============================================================
     
     /***************************************************************************
     * Returns all nodes neighboring the given node
     * @param: node
     * @return: neighbor of given node
     */
     public List<GMNode> getNeighborsOf(GMNode node)
     {
         List<GMNode> neighbors = new ArrayList<GMNode>();
         List<GMEdge> outwardEdges = getEdgesStartingIn(node);
         
         for (GMEdge e : outwardEdges)
         {
             neighbors.add(e.getEnd());
         }
         return neighbors;
     }
     
     /***************************************************************************
     * Returns edges starting from the given node
     * @param: starting node
     * @return: edges
     */
     public List<GMEdge> getEdgesStartingIn(GMNode start)
     {
         List<GMEdge> edges = new ArrayList<GMEdge>();
         
         for (GMEdge e : this.mappedEdges)
         {
             if (e.getStart() == start)
             {
                 edges.add(e);
             }
         }
         return edges;
     }
     
     /***************************************************************************
     * Returns edge connecting the given nodes
     * @param: starting node, final node
     * @return: edges
     */    
     public GMEdge getEdgeConnectingNodes(GMNode from, GMNode to)
     {
         for (GMEdge e : this.mappedEdges)
         {
             if (e.getStart() == from && e.getEnd() == to)
             {
                 return e;
             }
         }
         throw new IllegalArgumentException("Nodes are not connected.");
     }
     
     /***************************************************************************
     * Add a new node to the map
     */
     public final void addNode(GMNode newNode)
     {
     	this.mappedNodes.add(newNode);
     }
     
     /***************************************************************************
     * Add a new edge to the map (between two nodes)
     */
     public final void addEdge(GMEdge newEdge)
     {
     	this.mappedEdges.add(newEdge);
     }
     
     /***************************************************************************
     * Add a new location to the map
     */
     public final void addLocation(Location newLocation)
     {
     	this.mappedLocations.add(newLocation);
     }
     
 	public void exportXML(File outputFile)
 	{
 		String output = new String("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
 									+ "<map guid=\"" + this.getGuid() + "\">"
 									+ "<name>" + this.getName() + "</name>\n"
 									+ "<author name=\"" + this.getAuthorName() + "\" email=\"" + this.getAuthorEmail() + "\" />\n"
 									+ "<description>" + this.getDescription() + "</description>\n"
 									+ "<nodelist>\n");
 		for(GMNode node:this.mappedNodes)
 		{
 			output = output.concat(node.exportXML());
 		}
		output = output.concat(new String("</nodelist>\n<edgelist>\n"));
 		
 		for(GMEdge edge:this.mappedEdges)
 		{
 			output = output.concat(edge.exportXML());
 		}
		output = output.concat(new String("</edgelist>\n<locationlist>\n"));
 		
 		for(Location loc:this.mappedLocations)
 		{
 			output = output.concat(loc.exportXML());
 		}
		output = output.concat(new String("</locationlist>\n</map>\n"));
 		
 		System.out.print(output);
		/*PrintWriter out;
 		
 		try
 		{
 			out = new PrintWriter(new FileWriter(outputFile));
 			out.print(output);
 			out.close();
 		}
 		catch (IOException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
		}*/
 	}
 	
 }
       
