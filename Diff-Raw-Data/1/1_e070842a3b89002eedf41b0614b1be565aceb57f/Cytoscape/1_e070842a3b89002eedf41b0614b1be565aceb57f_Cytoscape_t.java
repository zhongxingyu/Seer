 //---------------------------------------------------------------------------
 //  $Revision$ 
 //  $Date$
 //  $Author$
 //---------------------------------------------------------------------------
 package cytoscape;
 
 import giny.model.Edge;
 import giny.model.Node;
 
 import java.beans.PropertyChangeEvent;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.swing.JOptionPane;
 import javax.swing.event.SwingPropertyChangeSupport;
 
 import cytoscape.data.ExpressionData;
 import cytoscape.data.GraphObjAttributes;
 import cytoscape.data.Semantics;
 import cytoscape.data.readers.GMLReader;
 import cytoscape.data.readers.GraphReader;
 import cytoscape.data.readers.InteractionsReader;
 import cytoscape.data.servers.BioDataServer;
 import cytoscape.giny.CytoscapeRootGraph;
 import cytoscape.giny.PhoebeNetworkView;
 import cytoscape.util.CyNetworkNaming;
 import cytoscape.view.CyNetworkView;
 import cytoscape.view.CytoscapeDesktop;
 
 /**
  * This class, Cytoscape is <i>the</i> primary class in the API.
  * 
  * All Nodes and Edges must be created using the methods getCyNode and getCyEdge, available only in this class.  Once A node or edge is created using these methods it can then be added to a CyNetwork, where it can be used algorithmically.<BR>
 <BR>
 The methods get/setNode/EdgeAttributeValue allow you to assocate data with nodes or edges. That data is then carried into all CyNetworks where that Node/Edge is present.
  */
 public abstract class Cytoscape {
   
   public static String NETWORK_CREATED = "NETWORK_CREATED";
   public static String ATTRIBUTES_CHANGED = "ATTRIBUTES_CHANGED";
   public static String NETWORK_DESTROYED = "NETWORK_DESTROYED";
   public static String CYTOSCAPE_EXIT = "CYTOSCAPE_EXIT";
 
   /**
    * When creating a network, use one of the standard suffixes
    * to have it parsed correctly<BR>
    * <ul>
    * <li> sif -- Simple Interaction File</li>
    * <li> gml -- Graph Markup Languange</li>
    * <li> sbml -- SBML</li>
    * </ul>
    */
   public static int FILE_BY_SUFFIX = 0;
   public static int FILE_GML = 1;
   public static int FILE_SIF = 2;
   public static int FILE_SBML = 3;
 
 
   private static BioDataServer bioDataServer;
   private static String species;
 
   /**
    * The shared RootGraph between all Networks
    */
   protected static CytoscapeRootGraph cytoscapeRootGraph;
   
   /**
    * The NetworkData that stores node info
    */
   // TODO: replace seperate objects with one
   protected static GraphObjAttributes nodeData;
 
   /**
    * The NetworkData that stores edge info
    */
   // TODO: replace seperate objects with one
   protected static GraphObjAttributes edgeData;
 
   //TODO: remove, replace with NetworkData
   protected static ExpressionData expressionData;
 
   protected static Object pcsO = new Object();
   protected static SwingPropertyChangeSupport pcs = new SwingPropertyChangeSupport( pcsO );
 
 
   protected static Map networkViewMap;
   protected static Map networkMap;
 
   protected static CytoscapeDesktop defaultDesktop;
 
   protected static String currentNetworkID;
   protected static String currentNetworkViewID;
   /**
    * A null CyNetwork to give when there is no Current Network
    */
   protected static CyNetwork nullNetwork = getRootGraph().
     createNetwork( new int[] {}, new int[] {} );
   
   /**
    * A null CyNetworkView to give when there is no Current NetworkView
    */
   protected static CyNetworkView nullNetworkView =  new PhoebeNetworkView ( nullNetwork, "null" );
 
 
   protected static CytoscapeObj cytoscapeobj;
 
   /**
    * The CytoscapeObj contains useful references to things like the BioDataServer, 
    * CytoscapeConfig ( for command line switches ), and the Plugin registry.
    */
   public static CytoscapeObj getCytoscapeObj () {
     if ( cytoscapeobj == null )
       cytoscapeobj = new CytoscapeObj( new CytoscapeConfig( new String[]{} ) );
     return cytoscapeobj;
   }
 
   public static void setCytoscapeObj ( CytoscapeObj obj ) {
     cytoscapeobj = obj;
   }
 
   /**
    * Shuts down Cytoscape, after giving plugins time to react.
    */
   public static void exit () {
 
     System.out.println( "Cytoscape Exiting...." );
     try {
       firePropertyChange( CYTOSCAPE_EXIT,
                           null,
                           "now" );
     } catch ( Exception e ) {
       System.out.println( "Errors on close, closed anyways." );
     }
     if (getCytoscapeObj().getParentApp() != null) {
         getCytoscapeObj().getParentApp().exit(0);
     } else {
         System.exit(0);
     }
   }
 
   //--------------------//
   // Root Graph Methods
   //--------------------//
 
   /**
    * Bound events are:
    * <ol>
    * <li>NETWORK_CREATED
    * <li>NETWORK_DESTROYED
    * <li>ATTRIBUTES_ADDED
    * <li>CYTOSCAPE_EXIT
    * </ol>
    */
   public static SwingPropertyChangeSupport getSwingPropertyChangeSupport () {
     return pcs;
   }
 
   /** 
    * Return the CytoscapeRootGraph
    */
   public static CytoscapeRootGraph getRootGraph () {
     if ( cytoscapeRootGraph == null ) 
       cytoscapeRootGraph = new CytoscapeRootGraph();
     
     return cytoscapeRootGraph;
   }
    
   /**
    * Ensure the capacity of Cytoscapce. This is to prevent
    * the inefficiency of adding nodes one at a time.
    */
   public static void ensureCapacity ( int nodes, int edges ) {
     // getRootGraph().ensureCapacity( nodes, edges );
   }
      
   /**
    * @deprecated
    * WARNING: this should only be used under special circumstances.
    */
   public static void clearCytoscape () {
   
     int[] edges = getRootGraph().getEdgeIndicesArray();
     if ( edges == null ) {
       // System.out.println( "Null Edges in clear" );
     } else {
       getRootGraph().removeEdges( getRootGraph().getEdgeIndicesArray() );
     }
 
     getRootGraph().removeNodes( getRootGraph().getNodeIndicesArray() );
     nodeData = new GraphObjAttributes();
     edgeData = new GraphObjAttributes();
 
   }
   
   /**
    * @return all CyNodes that are present in Cytoscape
    */
   public static List getCyNodesList () {
     return getRootGraph().nodesList();
   }
 
   /**
    * @return all CyEdges that are present in Cytoscape
    */
   public static List getCyEdgesList () {
     return getRootGraph().edgesList();
   }
 
   /**
    * @param alias an alias of a node
    * @return will return a node, if one exists for the given alias
    */
   public static CyNode getCyNode ( String alias ) {
     return getCyNode( alias, false );
   }
 
   /**
    * @param alias an alias of a node
    * @param create will create a node if one does not exist
    * @return will always return a node, if <code>create</code> is true
    */
   public static CyNode getCyNode ( String alias, boolean create ) {
 
     String old_name =  alias;
     alias = canonicalizeName( alias );
 
     CyNode node = ( CyNode )getNodeNetworkData().getGraphObject( alias );
     if ( node != null ) {
       //System.out.print(".");
       return node;
     }
     // node does not exist, create one
    
     if ( !create ) {
       return null;
     }
 
     //System.out.print( "|" );
     node = ( CyNode )Cytoscape.getRootGraph().getNode( Cytoscape.getRootGraph().createNode() );
     node.setIdentifier( alias );
     //System.out.println( "Node: "+node+" alias :"+alias+" old_name: "+old_name );
     //if ( old_name != alias )
     //  setNodeAttributeValue( node, "alias", old_name );
     Cytoscape.getNodeNetworkData().addNameMapping( alias, node );
     Semantics.assignNodeAliases( node, null, null );
     return node;
 
   }
 
   /**
    * Gets the first CyEdge found.
    * @param node_1 one end of the edge
    * @param node_2 the other end of the edge
    * @param attribute the attribute of the edge to be searched, a common one is {@link Semantics#INTERACTION }
    * @param attribute_value a value for the attribute, like "pp"
    * @param create will create an edge if one does not exist
    * @return returns an existing CyEdge if present, or creates one if <code>create</code> is true, otherwise returns null.
    */
   public static CyEdge getCyEdge ( Node node_1, Node node_2, String attribute, Object attribute_value, boolean create ) {
     
     Set edges = new HashSet();
     if ( Cytoscape.getRootGraph().getEdgeCount() != 0 ) {
       List l1 =  Cytoscape.getRootGraph().edgesList( node_1, node_2 );
       if ( l1 != null )
         edges.addAll( l1 );
       List l2 =  Cytoscape.getRootGraph().edgesList( node_2, node_1 );
       if ( l2 != null )
         edges.addAll( l2 );
       
       for ( Iterator i = edges.iterator(); i.hasNext(); ) {
         CyEdge edge = ( CyEdge )i.next();
         if ( getEdgeAttributeValue( edge, attribute ) == attribute_value ) 
           return edge;
       }
     }
       
     if ( !create )
       return null;
 
 
     if ( attribute == Semantics.INTERACTION ) {
     // create the edge
       CyEdge edge =  ( CyEdge )Cytoscape.getRootGraph().getEdge( Cytoscape.getRootGraph().createEdge (node_1, node_2));
       String edge_name = node_1.getIdentifier()+" ("+attribute_value+") "+node_2.getIdentifier();
       Cytoscape.getEdgeNetworkData().add ("interaction", edge_name, attribute_value);
       Cytoscape.getEdgeNetworkData().addNameMapping (edge_name, edge);
       return edge;
     }
 
     return null;
 
   }
 
 
   /**
    * @param source_alias an alias of a node
    * @param edge_name the name of the node
    * @param target_alias an alias of a node
    * @return will always return an edge
    */
   public static CyEdge getCyEdge ( String source_alias, 
                                    String edge_name, 
                                    String target_alias,
                                    String interaction_type ) {
     
     edge_name = canonicalizeName( edge_name );
     CyEdge edge = ( CyEdge )getEdgeNetworkData().getGraphObject( edge_name );
     if ( edge != null ) {
       //System.out.print( "`" );
       return edge;
     } 
     
     // edge does not exist, create one
     //System.out.print( "*" );
     CyNode source = getCyNode( source_alias );
     CyNode target = getCyNode( target_alias );
     edge =  ( CyEdge )Cytoscape.getRootGraph().getEdge( Cytoscape.getRootGraph().createEdge (source, target));
 
     Cytoscape.getEdgeNetworkData().add ("interaction", edge_name, interaction_type);
     Cytoscape.getEdgeNetworkData().addNameMapping (edge_name, edge);
     return edge;
   }
    
    /**
    * Return the requested Attribute for the given Node
    * @param node the given CyNode
    * @param attribute the name of the requested attribute
    * @return the value for the give node, for the given attribute
    */
   public static Object getNodeAttributeValue ( Node node, String attribute ) {
     return Cytoscape.getNodeNetworkData().get( attribute, 
                                                Cytoscape.getNodeNetworkData().getCanonicalName( node ) );
   }
 
   /**
    * Return the requested Attribute for the given Edge
    */
   public static Object getEdgeAttributeValue ( Edge edge, String attribute ) {
     return Cytoscape.getEdgeNetworkData().get( attribute, 
                                                Cytoscape.getEdgeNetworkData().getCanonicalName( edge ) );
   }
 
   /**
    * Return all availble Attributes for the Nodes in this CyNetwork
    */
   public static String[] getNodeAttributesList () {
     return Cytoscape.getNodeNetworkData().getAttributeNames();
   }
   
   /**
    * Return all available Attributes for the given Nodes
    */
   public static String[] getNodeAttributesList ( Node[] nodes ) {
     return Cytoscape.getNodeNetworkData().getAttributeNames();
   }
 
   /**
    * Return all availble Attributes for the Edges in this CyNetwork
    */
   public static String[] getEdgeAttributesList () {
     return Cytoscape.getEdgeNetworkData().getAttributeNames();
   }
 
   /**
    * Return all available Attributes for the given Edges
    */
   public static String[] getNodeAttributesList ( Edge[] edges ) {
     return Cytoscape.getEdgeNetworkData().getAttributeNames();
   }
 
 
    /**
    * Return the requested Attribute for the given Node
    * @param node the given CyNode
    * @param attribute the name of the requested attribute
    * @param value the value to be set
    * @return if it overwrites a previous value
    */
   public static boolean setNodeAttributeValue ( Node node, String attribute, Object value ) {
     return Cytoscape.getNodeNetworkData().set( attribute, 
                                                Cytoscape.
                                                getNodeNetworkData().
                                                getCanonicalName( node ),
                                                value );
     
 
   }
 
 
   /**
    * Return the requested Attribute for the given Edge
    */
   public static boolean setEdgeAttributeValue ( Edge edge, String attribute, Object value ) {
     return Cytoscape.getEdgeNetworkData().set( attribute, 
                                                Cytoscape.
                                                getEdgeNetworkData().
                                                getCanonicalName( edge ),
                                                value );
   }
 
  
   /**
    * @deprecated argh!...
    */
   private static String canonicalizeName ( String name ) {
     String canonicalName = name;
 
     //System.out.println( "Biodataserver is: "+bioDataServer+" species is: "+species );
      
 
     if ( bioDataServer != null) {
       canonicalName = bioDataServer.getCanonicalName (species, name);
       if(canonicalName == null){
         //   System.out.println( "canonicalName was null for "+name );
         canonicalName = name;
       }
       //System.out.println( name+" canonicalized to: "+canonicalName );
     }
     return canonicalName;
   }
 
   /**
    * @deprecated argh!...
    */
   public static void setSpecies () {
     species = cytoscape.data.Semantics.getDefaultSpecies( getCurrentNetwork(), getCytoscapeObj() );
   }
 
   //--------------------//
   // Network Methods
   //--------------------//
 
   /**
    * Return the Network that currently has the Focus.
    * Can be different from getCurrentNetworkView
    */
   public static CyNetwork getCurrentNetwork () {
     if ( currentNetworkID == null ) 
       return nullNetwork;
     
     CyNetwork network = ( CyNetwork )getNetworkMap().get( currentNetworkID );
     return network;
   }
 
   /**
    * Return a List of all available CyNetworks
    */
   public static Set getNetworkSet () {
     return  new java.util.LinkedHashSet(((HashMap) getNetworkMap()).values());
   }
 
   /**
    * @return the CyNetwork that has the given identifier
    * or null if there is no such network 
    */
   public static CyNetwork getNetwork ( String id ) {
     if ( getNetworkMap().containsKey( id ) ) 
       return ( CyNetwork )getNetworkMap().get( id ); 
     return nullNetwork;
   }
 
   /**
    * @return a CyNetworkView for the given ID, if one exists, otherwise returns null
    */
   public static CyNetworkView getNetworkView ( String network_id ) {
     if ( network_id == null ) 
       return nullNetworkView;
 
     CyNetworkView nview =  ( CyNetworkView )getNetworkViewMap().get( network_id );
     return nview;
   }
 
 
   /**
    * @return if a view exists for a given network id
    */
   public static boolean viewExists ( String network_id ) {
     return getNetworkViewMap().containsKey( network_id );
   }
 
 
 
   /**
    * Return the CyNetworkView that currently has the focus.
    * Can be different from getCurrentNetwork 
    */ 
   public static CyNetworkView getCurrentNetworkView () {
     if ( currentNetworkViewID == null ) 
       return nullNetworkView;
 
     //    System.out.println( "Cytoscape returning current network view: "+currentNetworkViewID );
 
     CyNetworkView nview =  ( CyNetworkView )getNetworkViewMap().get( currentNetworkViewID );
     return nview;
   }
 
   
     
   /**
    * @return the reference to the One CytoscapeDesktop
    */
   public static CytoscapeDesktop getDesktop() {
     if ( defaultDesktop == null ) {
       //System.out.println( " Defaultdesktop created: "+defaultDesktop );
       defaultDesktop = new CytoscapeDesktop( getCytoscapeObj().getConfiguration().getViewType() );
     }
     return defaultDesktop;
   }
 
   /**
    * @deprecated
    */
   public static void setCurrentNetwork ( String id ) {
     if ( getNetworkMap().containsKey( id ) )
       currentNetworkID = id;
 
     //System.out.println( "Currentnetworkid is: "+currentNetworkID+ " set from : "+id );
 
   }
 
   /**
    * @deprecated
    * @return true if there is network view, false if not
    */
   public static boolean setCurrentNetworkView ( String id ) {
     if ( getNetworkViewMap().containsKey( id ) ) {
       currentNetworkViewID = id;
       return true;
     }
     return false;
   }
 
 
   /**
    * This Map has keys that are Strings ( network_ids ) and values that are networks.
    */
   protected static Map getNetworkMap () {
     if ( networkMap == null ) {
       networkMap = new HashMap();
     }
     return networkMap;
   }
   /**
    * This Map has keys that are Strings ( network_ids ) and values that are networkviews.
    */
   protected static Map getNetworkViewMap () {
     if ( networkViewMap == null ) {
       networkViewMap = new HashMap();
     }
     return networkViewMap;
   }
 
   /** 
    * destroys the given network
    */
   public static void destroyNetwork ( String network_id ) {
     destroyNetwork( ( CyNetwork )getNetworkMap().get( network_id ) );
   }
   /** 
    * destroys the given network
    */
   public static void destroyNetwork ( CyNetwork network ) {
     destroyNetwork( network, false );
   }
   /** 
    * destroys the given network
    * @param network the network tobe destroyed
    * @param destroy_unique if this is true, then all Nodes and Edges that are in this network, but no other are also destroyed.
    */
   public static void destroyNetwork ( CyNetwork network, boolean destroy_unique ) {
 
       getNetworkMap().remove( network.getIdentifier() );
       if ( viewExists( network.getIdentifier() ) )
         destroyNetworkView( network );
       firePropertyChange( NETWORK_DESTROYED,
                           null,
                           network.getIdentifier() );
     
       if ( destroy_unique ) {
 
        
         
         ArrayList nodes = new ArrayList();
         ArrayList edges = new ArrayList();
 
         Collection networks = networkMap.values();
         
         Iterator nodes_i = network.nodesIterator();
         Iterator edges_i = network.edgesIterator();
         
         while ( nodes_i.hasNext() ){
           Node node = ( Node )nodes_i.next();
           boolean add = true;
           for ( Iterator n_i = networks.iterator(); n_i.hasNext(); ) {
             CyNetwork net = ( CyNetwork )n_i.next();
             if ( net.containsNode( node ) ) {
               add = false;
               continue;
             }
           }
           if ( add ) {
             nodes.add( node );
             getNodeNetworkData().removeNameMapping( node.getIdentifier() );
             getNodeNetworkData().removeObjectMapping( node );
             getRootGraph().deleteNode( node );
           }
         }
       
         while ( edges_i.hasNext() ){
           Edge edge = ( Edge )edges_i.next();
           boolean add = true;
           for ( Iterator n_i = networks.iterator(); n_i.hasNext(); ) {
             CyNetwork net = ( CyNetwork )n_i.next();
             if ( net.containsEdge( edge ) ) {
               add = false;
               continue;
             }
           }
           if ( add ) {
             edges.add( edge );
             getEdgeNetworkData().removeNameMapping( edge.getIdentifier() );
             getEdgeNetworkData().removeObjectMapping( edge );
             getRootGraph().deleteEdge( edge );
           }
         }
 
         getRootGraph().removeNodes( nodes );
         getRootGraph().removeEdges( edges );
 
       
       }
 
 
     // theoretically this should not be set to null till after the events firing is done
     network = null;
   }
 
   /**
    * destroys the networkview, including any layout information
    */
   public static void destroyNetworkView ( CyNetworkView view ) {
 
     //  System.out.println( "destroying: "+view.getIdentifier()+" : "+getNetworkViewMap().get( view.getIdentifier() ) );
 
     getNetworkViewMap().remove( view.getIdentifier() );
 
            
     //    System.out.println( "gone from hash: "+view.getIdentifier()+" : "+getNetworkViewMap().get( view.getIdentifier() ) );
              
     firePropertyChange( CytoscapeDesktop.NETWORK_VIEW_DESTROYED,
                         null,
                         view );
     // theoretically this should not be set to null till after the events firing is done
     view = null;
     // TODO: do we want here?
     System.gc();
   }
 
    /**
    * destroys the networkview, including any layout information
    */
   public static void destroyNetworkView ( String network_view_id ) {
     destroyNetworkView( ( CyNetworkView )getNetworkViewMap().get( network_view_id ) );
   }
 
    /**
    * destroys the networkview, including any layout information
    */
   public static void destroyNetworkView ( CyNetwork network ) {
     destroyNetworkView( ( CyNetworkView )getNetworkViewMap().get( network.getIdentifier() ) );
   }
 
   protected static void addNetwork ( CyNetwork network ) {
     addNetwork( network, null, null );
   }
 
   protected static void addNetwork ( CyNetwork network, String title ) {
     addNetwork( network, title, null );
   }
 
   protected static void addNetwork ( CyNetwork network, String title, CyNetwork parent ) {
 
     // System.out.println( "CyNetwork Added: "+network.getIdentifier() );
 
     getNetworkMap().put( network.getIdentifier(), network );
     network.setTitle( title );
     String p_id = null;
     if ( parent != null ) {
       p_id = parent.getIdentifier();
     }
 
     firePropertyChange( NETWORK_CREATED,
                         p_id,
                         network.getIdentifier() );
 
 
     System.out.println( "GML data is: "+network.getClientData( "GML" ) );
 
     if ( network.getNodeCount() < getCytoscapeObj().getViewThreshold()  ) {
        createNetworkView( network );
     }
       
 
     // createNetworkView( network );
   }
  
   /**
    * Creates a new, empty Network. 
    * @param title the title of the new network.
    */
   public static CyNetwork createNetwork ( String title ) {
     CyNetwork network =  getRootGraph().createNetwork( new int[] {}, new int[] {} );
     addNetwork( network, title );
     return network;
   }
   
   /**
    * Creates a new Network 
    * @param nodes the indeces of nodes
    * @param edges the indeces of edges
    * @param title the title of the new network.
    */
   public static CyNetwork createNetwork ( int[] nodes, int[] edges, String title ) {
     CyNetwork network = getRootGraph().createNetwork( nodes, edges );
     addNetwork( network, title );
     return network;
   }
 
    
   /**
    * Creates a new Network 
    * @param nodes a collection of nodes
    * @param edges a collection of edges
    * @param title the title of the new network.
    */
   public static CyNetwork createNetwork ( Collection nodes, 
                                           Collection edges,
                                           String title ) {
     Node[] node_array = ( Node[] )nodes.toArray( new Node[]{} );
     Edge[] edge_array = ( Edge[] )edges.toArray( new Edge[]{} );
 
     CyNetwork network = getRootGraph().createNetwork( node_array, edge_array );
     addNetwork( network, title  );
     return network;
   }
    
   /**
    * Creates a new Network, that inherits from the given ParentNetwork
    * @param nodes the indeces of nodes
    * @param edges the indeces of edges
    * @param child_title the title of the new network.
    * @param param the parent of the this Network 
    */
   public static CyNetwork createNetwork ( int[] nodes, int[] edges, String child_title, CyNetwork parent ) {
     CyNetwork network = getRootGraph().createNetwork( nodes, edges );
     addNetwork( network, child_title, parent );
     return network;
   }
  
   /**
    * Creates a new Network, that inherits from the given ParentNetwork
    * @param nodes the indeces of nodes
    * @param edges the indeces of edges
    * @param param the parent of the this Network
    */
   public static  CyNetwork createNetwork ( Collection nodes, 
                                            Collection edges,  
                                            String child_title, 
                                            CyNetwork parent ) {
     Node[] node_array = ( Node[] )nodes.toArray( new Node[]{} );
     Edge[] edge_array = ( Edge[] )edges.toArray( new Edge[]{} );
     CyNetwork network = getRootGraph().createNetwork( node_array, edge_array );
     addNetwork( network, child_title, parent );
     return network;
   }
 
 
   /**
    * Creates a cytoscape.data.CyNetwork from a file. The file type is determined by 
    * the suffice of the file
    * <ul>
    * <li> sif -- Simple Interaction File</li>
    * <li> gml -- Graph Markup Languange</li>
    * <li> sbml -- SBML</li>
    * </ul>
    * @param location the location of the file
    */
   public static  CyNetwork createNetworkFromFile ( String location ) {
     return createNetwork( location, FILE_BY_SUFFIX, false, null, null );
   }
 
   /**
    * Creates a cytoscape.data.CyNetwork from a file.  The passed variable determines the
    * type of file, i.e. GML, SIF, SBML, etc.
    *
    * @param location the location of the file
    * @param file_type the type of file GML, SIF, SBML, etc.
    * @param canonicalize this will set the preferred display name to what is
    *                     on the server.
    * @param biodataserver provides the name conversion service
    * @param species  the species used by the BioDataServer
    */
   public static CyNetwork createNetwork ( String location,
                                           int file_type,
                                           boolean canonicalize, 
                                           BioDataServer biodataserver, 
                                           String species ) {
     // return null for a null file
     if ( location == null )  
       return null;
 
     GraphReader reader;
 
     //set the reader according to what file type was passed.
     if ( file_type == FILE_SIF 
          || ( file_type == FILE_BY_SUFFIX && location.endsWith( "sif" ) ) ) {
       reader = new InteractionsReader( biodataserver, 
                                        species,
                                        location );
     } else if ( file_type == FILE_GML
                 || ( file_type == FILE_BY_SUFFIX && location.endsWith( "gml" ) ) ) {
       reader = new GMLReader( location );
     } else {
       // TODO: come up with a really good way of supporting arbitrary 
       // file types via plugin support.
       System.err.println( "File Type not Supported, sorry" );
       return Cytoscape.createNetwork(null);
     }
 
     // have the GraphReader read the given file
     try {
       reader.read();
     } catch ( Exception e ) {
       System.err.println( "Cytoscape: Error Reading Graph File: "+location+"\n--------------------\n" );
       e.printStackTrace();
       return null;
     }
 
     // get the RootGraph indices of the nodes and
     // edges that were just created
     int[] nodes = reader.getNodeIndicesArray();
     int[] edges = reader.getEdgeIndicesArray();
     
     if ( nodes == null ) {
       System.err.println( "reader returned null nodes" );
     }
 
     if ( edges == null ) {
       System.err.println( "reader returned null edges" );
     }
 
     String[] title = location.split( "/" );
     if ( System.getProperty("os.name").startsWith( "Win" ) ) {
       title = location.split( "//" );
     }
 
     // Create a new cytoscape.data.CyNetwork from these nodes and edges
     CyNetwork network = createNetwork
       (nodes,
        edges,
        CyNetworkNaming.getSuggestedNetworkTitle(title[title.length - 1]));
 
     
     if ( file_type == FILE_GML
          || ( file_type == FILE_BY_SUFFIX && location.endsWith( "gml" ) ) ) {
       
       System.out.println( "GML file gettign reader: "+title[title.length - 1] );
       network.putClientData( "GML", reader );
     }
 
 
     System.out.println( "NV: "+getNetworkView( network.getIdentifier() ) );
 
     if ( getNetworkView( network.getIdentifier() ) != null ) {
       reader.layout( getNetworkView( network.getIdentifier() ) );
     }
 
     return network;
 
   }
 
  
 
   //--------------------//
   // Network Data Methods
   //--------------------//
   
   /**
    * @deprecated
    * This should not be used by any user-code
    */
   public static GraphObjAttributes getNodeNetworkData () {
     if ( nodeData == null ) 
       nodeData = new GraphObjAttributes();
     return nodeData;
   }
 
   /**
    * @deprecated
    * This should not be used by any user-code
    */
   public static GraphObjAttributes getEdgeNetworkData () {
     if ( edgeData == null ) 
       edgeData = new GraphObjAttributes();
     return edgeData;
   }
 
   public static ExpressionData getExpressionData () {
     return expressionData;
   }
 
   /**
    * Load Expression Data
    */
   //TODO: remove the JOption Pane stuff 
   public static boolean loadExpressionData ( String filename, boolean copy_atts ) {
      try {
        expressionData = new ExpressionData( filename );
      } catch (Exception e) {
        System.err.println( "Unable to Load Expression Data" );
        String errString = "Unable to load expression data from "
          + filename;
        String title = "Load Expression Data";
        JOptionPane.showMessageDialog( getDesktop(),
                                       errString, 
                                       title,
                                       JOptionPane.ERROR_MESSAGE);
        return false;
      }
        
      if ( copy_atts ) {
        expressionData.copyToAttribs( getNodeNetworkData() );
       firePropertyChange( ATTRIBUTES_CHANGED,null,null );
      }
 
      //display a description of the data in a dialog
      String expDescript = expressionData.getDescription();
      String title = "Load Expression Data";
      JOptionPane.showMessageDialog( getDesktop(),
                                     expDescript, 
                                     title,
                                     JOptionPane.PLAIN_MESSAGE );
      return true;
   }
 
   /**
    * Loads Node and Edge attribute data into Cytoscape from the given
    * file locations. Currently, the only supported attribute types are
    * of the type "name = value".
    *
    * @param nodeAttrLocations  an array of node attribute file locations. May be null.
    * @param edgeAttrLocations  an array of edge attribute file locations. May be null.
    * @param canonicalize  convert to the preffered name on the biodataserver
    * @param bioDataServer  provides the name conversion service
    * @param species  the species to use with the bioDataServer's
    */
   public static void loadAttributes ( String[] nodeAttrLocations,
                                       String[] edgeAttrLocations,
                                       boolean canonicalize,
                                       BioDataServer bioDataServer, 
                                       String species ) {
     
     // check to see if there are Node Attributes passed
     if ( nodeAttrLocations != null ) {
 	    
 	    for ( int i = 0 ; i < nodeAttrLocations.length; ++i ) {
         try {
           nodeData.readAttributesFromFile( bioDataServer, 
                                            species,
                                            nodeAttrLocations[i],
                                            canonicalize );
         } catch (Exception e) {
           System.err.println( "Error loading attributes into NodeData" );
           // e.printStackTrace();
         }
 	    }
     }
     
     // Check to see if there are Edge Attributes Passed
     if (edgeAttrLocations != null) {
 
 	    for ( int  j = 0 ; j < edgeAttrLocations.length; ++j ) {
         try {
           edgeData.readAttributesFromFile( edgeAttrLocations[j] );
         } catch (Exception e) {
           System.err.println( "Error loading attributes into EdgeData" );
           //e.printStackTrace();
         }
 	    }
     }
 
     firePropertyChange( ATTRIBUTES_CHANGED,
                         null,
                         null );
 
   }
 
   /**
    * Loads Node and Edge attribute data into Cytoscape from the given
    * file locations. Currently, the only supported attribute types are
    * of the type "name = value".
    *
    * @param nodeAttrLocations  an array of node attribute file locations. May be null.
    * @param edgeAttrLocations  an array of edge attribute file locations. May be null.
    */
   public static void loadAttributes( String[] nodeAttrLocations,
                                      String[] edgeAttrLocations ) {
     loadAttributes( nodeAttrLocations, edgeAttrLocations, false, null, null );
   }
   
 
       
   /**
    * Constructs a network using information from a CyProject argument that
    * contains information on the location of the graph file, any node/edge
    * attribute files, and a possible expression data file.
    * If the data server argument is non-null and the project requests
    * canonicalization, the data server will be used for name resolution
    * given the names in the graph/attributes files.
    *
    * @see CyProject
    */
   public static CyNetwork createNetworkFromProject( CyProject project,
                                                     BioDataServer bioDataServer) {
     if (project == null) {return null;}
     
     boolean canonicalize = project.getCanonicalize();
     String species = project.getDefaultSpeciesName();
     CyNetwork network = null;
     if (project.getInteractionsFilename() != null) {
 	    //read graph from interaction data
 	    String filename = project.getInteractionsFilename();
 	    network = createNetwork( filename, 
                                Cytoscape.FILE_SIF,
                                canonicalize,
                                bioDataServer, 
                                species); 
     }
     else if (project.getGeometryFilename() != null) {
 	    //read a GML file
 	    String filename = project.getGeometryFilename();
 	    network = createNetwork( filename,
                                Cytoscape.FILE_GML,
                                false,
                                null, 
                                null);
 
     }
 
     if (network == null) {//no graph specified, or unable to read
 	    //create a default network
 	    network = createNetwork(null);
     }
 	
     //load attributes files
     String[] nodeAttributeFilenames = project.getNodeAttributeFilenames();
     String[] edgeAttributeFilenames = project.getEdgeAttributeFilenames();
     loadAttributes( nodeAttributeFilenames, 
                     edgeAttributeFilenames,
                     canonicalize, 
                     bioDataServer, 
                     species);
 	
     //load expression data
     //ExpressionData expData = null;
     //if (project.getExpressionFilename() != null) {
 	  //  expData = new ExpressionData( project.getExpressionFilename() );
 	  //  network.setExpressionData(expData);
     //}
     loadExpressionData( project.getExpressionFilename(), true );
 
     return network;
   }
 
   /**
    * A BioDataServer should be loadable from a file systems file 
    * or from a URL.
    */
   public static BioDataServer loadBioDataServer ( String location ) {
     System.out.println( "laod BDS: "+location );
 
      try {
        bioDataServer = new BioDataServer ( location );
        getCytoscapeObj().setBioDataServer(bioDataServer);
      } catch ( Exception e ) {
        String es = "cannot create new biodata server at " + location;
        getCytoscapeObj().getLogger().warning(es);
        return null;
      }
      return bioDataServer;
   }
 
   //------------------------------//
   // CyNetworkView Creation Methods
   //------------------------------//
   
   /**
    * Creates a CyNetworkView, but doesn't do anything with it.
    * Ifnn's you want to use it @link {CytoscapeDesktop}
    * @param network the network to create a view of
   */
   public static CyNetworkView createNetworkView ( CyNetwork network ) {
      return createNetworkView( network, network.getTitle() );
   }
   
   /**
    * Creates a CyNetworkView, but doesn't do anything with it.
    * Ifnn's you want to use it @link {CytoscapeDesktop}
    * @param network the network to create a view of
    */
   //TODO: title
   public static CyNetworkView createNetworkView ( CyNetwork network, String title ) {
      
     if ( network == nullNetwork ) {
       return nullNetworkView;
     }
 
     System.out.println( "Creating View from: "+network.getIdentifier()+" : "+network.getIdentifier() );
     if ( viewExists( network.getIdentifier() ) )
       return getNetworkView( network.getIdentifier() );
     
     System.out.println( "Nodes: "+network.getNodeCount()+" | Edges: "+network.getEdgeCount() );   
 
     
 
 
     CyNetworkView view = new PhoebeNetworkView ( network, title );
     view.setIdentifier( network.getIdentifier() );
     getNetworkViewMap().put( network.getIdentifier(), view );
     view.setTitle( network.getTitle() );
     // System.out.println( "Just Created a PhoebeNetworkView: "+network.getIdentifier()+
     //                     " and it should be in the networkViewMap: "+
     //                     getNetworkViewMap().get( network.getIdentifier() ) );
 
 
     if ( network.getClientData( "GML" ) != null ) {
       ( ( GraphReader )network.getClientData( "GML" ) ).layout( view );
       System.out.println( "GML data found for: "+network.getTitle()+" "+network.getClientData( "GML" ) );
       //network.putClientData( "GML" , null );
     }
     
      firePropertyChange( cytoscape.view.CytoscapeDesktop.NETWORK_VIEW_CREATED,
                          null,
                          view );
      view.fitContent();
      view.redrawGraph( false, false );
      return view;
   }
   
 
   
   protected static void firePropertyChange ( String property_type,
                                              Object old_value,
                                              Object new_value ) {
 
     PropertyChangeEvent e = new PropertyChangeEvent( pcsO, property_type, old_value, new_value );
     // System.out.println( "Cytoscape FIRING : "+property_type );
 
     getSwingPropertyChangeSupport().firePropertyChange( e );
   }
 
 }
