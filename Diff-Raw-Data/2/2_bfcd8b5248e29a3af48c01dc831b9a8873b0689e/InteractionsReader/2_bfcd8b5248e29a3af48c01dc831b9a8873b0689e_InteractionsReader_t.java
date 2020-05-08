 // InteractionsReader:  from semi-structured text file, into an array of Interactions
 
 /** Copyright (c) 2002 Institute for Systems Biology and the Whitehead Institute
  **
  ** This library is free software; you can redistribute it and/or modify it
  ** under the terms of the GNU Lesser General Public License as published
  ** by the Free Software Foundation; either version 2.1 of the License, or
  ** any later version.
  ** 
  ** This library is distributed in the hope that it will be useful, but
  ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  ** documentation provided hereunder is on an "as is" basis, and the
  ** Institute for Systems Biology and the Whitehead Institute 
  ** have no obligations to provide maintenance, support,
  ** updates, enhancements or modifications.  In no event shall the
  ** Institute for Systems Biology and the Whitehead Institute 
  ** be liable to any party for direct, indirect, special,
  ** incidental or consequential damages, including lost profits, arising
  ** out of the use of this software and its documentation, even if the
  ** Institute for Systems Biology and the Whitehead Institute 
  ** have been advised of the possibility of such damage.  See
  ** the GNU Lesser General Public License for more details.
  ** 
  ** You should have received a copy of the GNU Lesser General Public License
  ** along with this library; if not, write to the Free Software Foundation,
  ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
  **/
 
 //------------------------------
 // $Revision$   
 // $Date$ 
 // $Author$
 //------------------------------
 package cytoscape.data.readers;
 //------------------------------
 import java.util.StringTokenizer;
 import java.util.Vector;
 import java.util.Hashtable;
 import java.util.Iterator;
 
 import y.base.Node;
 import y.base.Edge;
 import y.view.Graph2D;
 
 import giny.model.RootGraph;
 import giny.model.GraphPerspective;
 import luna.*;
 
 import cytoscape.GraphObjAttributes;
 import cytoscape.data.Interaction;
 import cytoscape.data.*;
 import cytoscape.data.servers.*;
 import cytoscape.data.readers.*;
 
 /**
  * This is an Interaction Reader, it will create a network from
  * a given file.
  * 
  * The network can then be merged/added/replaced in Cytoscape
  */
 public class InteractionsReader implements GraphReader {
 
   /**
    * The File to be loaded
    */
   protected String filename;
   
   /**
    * A Vector that holds all of the Interactions
    */
   protected Vector allInteractions = new Vector ();
   GraphObjAttributes edgeAttributes = new GraphObjAttributes ();
   GraphObjAttributes nodeAttributes = new GraphObjAttributes ();
   Graph2D graph;
   RootGraph rootGraph;
   BioDataServer dataServer;
   String species;
   boolean isYFiles;
 
   //------------------------------
   // Constructors
 
   /**
    * Interactions Reader Constructor
    * Creates a new Interactions Reader
    * @param dataServer  a BioDataServer
    * @param species the species of the network being loaded
    * @param filename the file to load the network from
    * @param isYFiles should we create a YFiles graph?
    */ 
   public InteractionsReader (BioDataServer dataServer, String species, String filename, boolean isYFiles)
   {
     this.filename = filename;
     this.dataServer = dataServer;
     this.species = species;
     this.isYFiles = isYFiles;
   }
   /**
    * Interactions Reader Constructor
    * Creates a new Interactions Reader
    * This constructor assumes a Y-Files graph is wanted. If not
    * then use the other constructor to say so.
    * @param dataServer  a BioDataServer
    * @param species the species of the network being loaded
    * @param filename the file to load the network from
    */ 
   public InteractionsReader ( BioDataServer dataServer, 
                               String species, 
                               String filename ) {
     this.filename = filename;
     this.dataServer = dataServer;
     this.species = species;
     this.isYFiles = true;
   }
 
 
   //----------------------------------------------------------------------------------------
   public void read ( boolean canonicalize ) {
   
     String rawText;
     try {
       if (filename.trim().startsWith ("jar://")) {
         TextJarReader reader = new TextJarReader (filename);
         reader.read ();
         rawText = reader.getText ();
       }
       else {
         TextFileReader reader = new TextFileReader (filename);
         reader.read ();
         rawText = reader.getText ();
       }
     }
     catch (Exception e0) {
       System.err.println ("-- Exception while reading interaction file " + filename);
       System.err.println (e0.getMessage ());
       return;
     }
 
     String delimiter = " ";
     if (rawText.indexOf ("\t") >= 0)
       delimiter = "\t";
     StringTokenizer strtok = new StringTokenizer (rawText, "\n");
   
     // commented out by iliana on 11.26.2002 :
     // Vector interactions = new Vector ();
   
     while (strtok.hasMoreElements ()) {
       String newLine = (String) strtok.nextElement ();
       Interaction newInteraction = new Interaction (newLine, delimiter);
       allInteractions.addElement (newInteraction);
     }
     if (isYFiles)
       createYGraphFromInteractionData (canonicalize);
     else
       createRootGraphFromInteractionData (canonicalize);
   
   }
   //-----------------------------------------------------------------------------------------
   /**
    * Calls read(true)
    */
   public void read ()
   {
     read(true);
   }  // readFromFile
   //-------------------------------------------------------------------------------------------
   public int getCount ()
   {
     return allInteractions.size ();
   }
   //-------------------------------------------------------------------------------------------
   public Interaction [] getAllInteractions ()
   {
     Interaction [] result = new Interaction [allInteractions.size ()];
 
     for (int i=0; i < allInteractions.size (); i++) {
       Interaction inter = (Interaction) allInteractions.elementAt (i);
       result [i] = inter;
     }
 
     return result;
 
   }
   //-------------------------------------------------------------------------------------------
   protected String canonicalizeName (String name)
   {
     
     String canonicalName = name;
     if (dataServer != null) {
       canonicalName = dataServer.getCanonicalName (species, name);
       // added by iliana 11.14.2002
       // for some strange reason the server returned a null canonical name
       if(canonicalName == null){canonicalName = name;} 
       //System.out.println (" -- canonicalizeName from server: " + canonicalName);
     }
     //System.out.println("the canonicalName for " + name + " is " + canonicalName);
     //System.out.flush();
     return canonicalName;
 
   } // canonicalizeName
   //-------------------------------------------------------------------------------------------
   protected void createYGraphFromInteractionData (boolean canonicalize)
   {
 
     graph = new Graph2D ();
     Interaction [] interactions = getAllInteractions ();
 
     Hashtable nodes = new Hashtable ();
 
     //---------------------------------------------------------------------------
     // loop through all of the interactions -- which are triples of the form:
     //           source; target0, target1...; interaction type
     // using a hash to avoid duplicate node creation, add a node to the graph
     // for each source and target
     // in addition,
     //---------------------------------------------------------------------------
     String nodeName, targetNodeName;
     for (int i=0; i < interactions.length; i++) {
       Interaction interaction = interactions [i];
       //System.out.println ("source: " + interaction.getSource ());
       if(canonicalize)
 	  nodeName = canonicalizeName (interaction.getSource ());
       else
 	  nodeName = interaction.getSource();
         
       if (!nodes.containsKey (nodeName)) {
         Node node = graph.createNode (0.0, 0.0, 70.0, 30.0, nodeName);
         nodes.put (nodeName, node);
       }
       String [] targets = interaction.getTargets ();
       for (int t=0; t < targets.length; t++) {
         if(canonicalize){
           targetNodeName = canonicalizeName (targets [t]);
         }else{
           targetNodeName = targets[t];
         }
         if (!nodes.containsKey (targetNodeName)) {
           Node targetNode = graph.createNode (0.0, 0.0, 70.0, 30.0, targetNodeName);
           nodes.put (targetNodeName, targetNode);
         } // if target node is previously unknown
       } // for t
     } // i
 
     //---------------------------------------------------------------------------
     // now loop over the interactions again, this time creating edges between
     // all sources and each of their respective targets.
     // for each edge, save the source-target pair, and their interaction type,
     // in edgeAttributes -- a hash of a hash of name-value pairs, like this:
     //   edgeAttributes ["interaction"] = interactionHash
     //   interactionHash [sourceNode::targetNode] = "pd"
     //---------------------------------------------------------------------------
 
     for (int i=0; i < interactions.length; i++) {
       Interaction interaction = interactions [i];
       if(canonicalize){
         nodeName = canonicalizeName (interaction.getSource ());
       }else{
         nodeName = interaction.getSource();
       }
     
       String interactionType = interaction.getType ();
       Node sourceNode = (Node) nodes.get (nodeName);
       String [] targets = interaction.getTargets ();
       for (int t=0; t < targets.length; t++) {
         if(canonicalize)
 	    targetNodeName = canonicalizeName (targets [t]);
         else
 	    targetNodeName = targets[t];
             
         Node targetNode = (Node) nodes.get (targetNodeName);
         Edge edge = graph.createEdge (sourceNode, targetNode);
         String edgeName = nodeName + " (" + interactionType + ") " + targetNodeName;
         int previousMatchingEntries = edgeAttributes.countIdentical(edgeName);
         if (previousMatchingEntries > 0)
           edgeName = edgeName + "_" + previousMatchingEntries;
         edgeAttributes.add ("interaction", edgeName, interactionType);
         edgeAttributes.addNameMapping (edgeName, edge);
       } // for t
     } // for i
 
   } // createYGraphFromInteractionData
 
   //-------------------------------------------------------------------------------------------
   protected void createRootGraphFromInteractionData (boolean canonicalize)
   {
     rootGraph = new LunaRootGraph ();
     Interaction [] interactions = getAllInteractions ();
 
     Hashtable nodes = new Hashtable ();
 
     //---------------------------------------------------------------------------
     // loop through all of the interactions -- which are triples of the form:
     //           source; target0, target1...; interaction type
     // using a hash to avoid duplicate node creation, add a node to the graph
     // for each source and target
     // in addition,
     //---------------------------------------------------------------------------
     String nodeName, targetNodeName;
     for (int i=0; i < interactions.length; i++) {
 	Interaction interaction = interactions [i];
 	//System.out.println ("source: " + interaction.getSource ());
 	nodeName = interaction.getSource();
 	if(canonicalize) nodeName = canonicalizeName (interaction.getSource ());
 	
 	if (!nodes.containsKey (nodeName)) {
 	    giny.model.Node node = rootGraph.getNode(rootGraph.createNode ());
 	    node.setIdentifier(nodeName);
 	    nodes.put (nodeName, node);
 	    nodeAttributes.addNameMapping(nodeName, node);
 	}
 	String [] targets = interaction.getTargets ();
 	for (int t=0; t < targets.length; t++) {
 	    targetNodeName = targets[t];
 	    if(canonicalize) targetNodeName = canonicalizeName (targets [t]);
 	    
 	    if (!nodes.containsKey (targetNodeName)) {
 		giny.model.Node node = rootGraph.getNode(rootGraph.createNode ());
 		node.setIdentifier(targetNodeName);
 		nodes.put (targetNodeName, node);
		nodeAttributes.addNameMapping(targetNodeName, node);
 	    } // if target node is previously unknown
 	} // for t
     } // i
 
     //---------------------------------------------------------------------------
     // now loop over the interactions again, this time creating edges between
     // all sources and each of their respective targets.
     // for each edge, save the source-target pair, and their interaction type,
     // in edgeAttributes -- a hash of a hash of name-value pairs, like this:
     //   edgeAttributes ["interaction"] = interactionHash
     //   interactionHash [sourceNode::targetNode] = "pd"
     //---------------------------------------------------------------------------
 
     for (int i=0; i < interactions.length; i++) {
       Interaction interaction = interactions [i];
       nodeName = interaction.getSource();
       if(canonicalize) nodeName = canonicalizeName (interaction.getSource ());
     
       String interactionType = interaction.getType ();
       giny.model.Node sourceNode = (giny.model.Node) nodes.get (nodeName);
       String [] targets = interaction.getTargets ();
       for (int t=0; t < targets.length; t++) {
 	  if(canonicalize)
 	      targetNodeName = canonicalizeName (targets [t]);
 	  else
 	      targetNodeName = targets[t];
       
 	  giny.model.Node targetNode = (giny.model.Node) nodes.get (targetNodeName);
 	  giny.model.Edge edge = rootGraph.getEdge(rootGraph.createEdge (sourceNode, targetNode));
 	  String edgeName = nodeName + " (" + interactionType + ") " + targetNodeName;
 	  int previousMatchingEntries = edgeAttributes.countIdentical(edgeName);
 	  if (previousMatchingEntries > 0)
 	      edgeName = edgeName + "_" + previousMatchingEntries;
 	  edgeAttributes.add ("interaction", edgeName, interactionType);
 	  edgeAttributes.addNameMapping (edgeName, edge);
       } // for t
     } // for i
 
   } // createRootGraphFromInteractionData
   //-------------------------------------------------------------------------------------------
   public Graph2D getGraph ()
   {
     return graph;
 
   } // createGraph
 
   //-------------------------------------------------------------------------------------------
   public RootGraph getRootGraph ()
   {
     return rootGraph;
 
   } // createGraph
   //------------------------------------------------------------------------------------
   public GraphObjAttributes getNodeAttributes ()
   {
     return nodeAttributes;
 
   } // getNodeAttributes
   //------------------------------------------------------------------------------------
   public GraphObjAttributes getEdgeAttributes ()
   {
     return edgeAttributes;
 
   } // getEdgeAttributes
   //------------------------------------------------------------------------------------
 } // InteractionsReader
 
 
 
