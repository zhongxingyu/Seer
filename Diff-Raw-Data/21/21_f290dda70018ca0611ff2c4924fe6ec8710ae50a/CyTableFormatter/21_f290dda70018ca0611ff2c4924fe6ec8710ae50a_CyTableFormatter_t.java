 package org.reactome.cytoscape3;
 /**
  * This class creates the necessary CyTable columns
  * in the default tables and formats their homologs
  * across node, edge, and network tables if needed.
  * It also stores static fields for certain CyTable
  * value/column names.
  * @author Eric T Dawson
  * @date July 2013 
  */
 import org.cytoscape.model.CyNetwork;
 import org.cytoscape.model.CyTable;
 
 public class CyTableFormatter
 {
 
     //
     private static final String FI_NETWORK_VERSION = "Reactome_FI_Network_Version";
     private static final String SAMPLE_MUTATION_DATA = "SAMPLE_MUTATION_DATA";
     private static final String MCL_ARRAY_DATA = "MCL_ARRAY_DATA";
     private static final String MCL_ARRAY_CLUSTERING = "MCL_ARRAY_CLUSTERING";
     private static final String HOTNET_MODULE = "HOTNET_MODULE";
     private static final String SPECTRAL_PARTITION_CLUSTER = "SPECTRAL_PARTITION_CLUSTERING";
     
 //    private final String [] COLUMNS = new String [7];
 //    COLUMNS = ["network", "isReactomeFINetwork", FI_NETWORK_VERSION, "DataSetType", "moduleToSampleValue", "Clustering_Type", "isLinker"];
     
     public CyTableFormatter()
     {
     }
     
     public static String getFINetworkVersion()
     {
         return FI_NETWORK_VERSION;
     }
     public void makeAllTablesGSMA(CyNetwork network)
     {
 	
         CyTable netTable = network.getDefaultNetworkTable();
         CyTable nodeTable = network.getDefaultNodeTable();
         CyTable edgeTable = network.getDefaultEdgeTable();
 
 
         //From Jason's email. Make sure that the network SUID ends up in the properly linked table
         //in the edge/node table to ensure that network properties are distributed to nodes and edges.
         if (nodeTable.getColumn("network") == null)
         {
             nodeTable.createColumn("network", Long.class, true);
             edgeTable.createColumn("network", Long.class, true);
             //nodeTable.addVirtualColumn("network.SUID", "SUID", netTable, "network.SUID", true);
             //edgeTable.addVirtualColumn("network.SUID", "SUID", netTable, "network.SUID", true);
             //makeAllTablesGSMA(network);
         }
         //Creates a set of columns in the default network table and creates matching virtual columns
         //within the default edge and node tables upon network creation or network view creation.
         if (netTable.getColumn("isReactomeFINetwork") == null)
         {
             netTable.createColumn("isReactomeFINetwork", Boolean.class, Boolean.FALSE);
         }
         if (netTable.getColumn(FI_NETWORK_VERSION) == null)
         {
            netTable.createColumn(FI_NETWORK_VERSION, String.class, Boolean.FALSE);
         }
         if (netTable.getColumn("DataSetType") == null)
         {
             netTable.createColumn("DataSetType", String.class, Boolean.FALSE);
         }
         if (nodeTable.getColumn("module") == null);
         {
             //netTable.createColumn("moduleToSampleValue", Double.class, Boolean.FALSE);
             nodeTable.createColumn("module", Double.class, Boolean.FALSE);
         }
         if (netTable.getColumn("Clustering_Type") == null)
         {
             netTable.createColumn("Clustering_Type", String.class, Boolean.FALSE);
         }
         if (nodeTable.getColumn("sample") == null)
         {
             nodeTable.createColumn("sample", String.class, Boolean.FALSE);
         }
 
         if (nodeTable.getColumn("IsLinker") == null)
         {
             nodeTable.createColumn("IsLinker", Boolean.class, Boolean.FALSE, false);
             
             //In the 3.0 API, default CyTable values aren't set due to a bug. This
             //will be remedied in 3.1 (Projected Oct. 2013). Until then, this fix is
             //necessary.
             
         }
         if (nodeTable.getColumn("nodeLabel") == null)
         {
             nodeTable.createColumn("nodeLabel", String.class, Boolean.FALSE);
         }
         if (nodeTable.getColumn("sampleNumber") == null)
         {
             nodeTable.createColumn("sampleNumber", Integer.class, Boolean.FALSE);
         }
         if (nodeTable.getColumn("commonName") == null)
         {
             nodeTable.createColumn("commonName", String.class, Boolean.FALSE);
         }
         if (nodeTable.getColumn("nodeToolTip") == null)
         {
             nodeTable.createColumn("nodeToolTip", String.class, Boolean.FALSE);
         }
 	
     }
 }
