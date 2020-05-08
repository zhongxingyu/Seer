 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 
 package org.amanzi.awe.wizards.utils;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 
 import org.amanzi.neo.core.INeoConstants;
 import org.amanzi.neo.core.enums.GeoNeoRelationshipTypes;
 import org.amanzi.neo.core.enums.GisTypes;
 import org.amanzi.neo.core.enums.NodeTypes;
 import org.amanzi.neo.core.service.NeoServiceProvider;
 import org.amanzi.neo.core.utils.NeoUtils;
 import org.amanzi.neo.core.utils.PropertyHeader;
 import org.neo4j.graphdb.Direction;
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Relationship;
 import org.neo4j.graphdb.ReturnableEvaluator;
 import org.neo4j.graphdb.StopEvaluator;
 import org.neo4j.graphdb.Transaction;
 import org.neo4j.graphdb.TraversalPosition;
 import org.neo4j.graphdb.Traverser;
 import org.neo4j.graphdb.Traverser.Order;
 
 /**
  * Utility class that handle database access methods
  * 
  * @author Pechko_E
  * @since 1.0.0
  */
 public class DBUtils {
     /**
      * Finds all available networks
      * 
      * @return all available networks
      */
     public static HashMap<String, Node> getAllNetworks() {
         return getDatasets(GisTypes.NETWORK);
     }
 
     /**
      * Finds all available drives
      * 
      * @return all available drives
      */
     public static HashMap<String, Node> getAllDrives() {
         return getDatasets(GisTypes.DRIVE);
     }
 
     /**
      * Finds all available counter datasets
      * 
      * @return all available counter datasets
      */
 
     public static HashMap<String, Node> getAllCounters() {
         HashMap<String, Node> oss = getOSSs();
         HashMap<String, Node> datasets = getDatasets(GisTypes.OSS);
         datasets.putAll(oss);
         return datasets;
     }
 
     /**
      * Finds all available datasets of the given type
      * 
      * @param gisType network, drive or oss counters dataset
      * @return all available datasets of the given type
      */
     public static final HashMap<String, Node> getDatasets(GisTypes gisType) {
         Transaction tx = NeoUtils.beginTransaction();
         try {
             GraphDatabaseService service = NeoServiceProvider.getProvider().getService();
             Node refNode = service.getReferenceNode();
             LinkedHashMap<String, Node> datasets = new LinkedHashMap<String, Node>();
             for (Relationship relationship : refNode.getRelationships(Direction.OUTGOING)) {
                 Node node = relationship.getEndNode();
                 Object type = node.getProperty(INeoConstants.PROPERTY_GIS_TYPE_NAME, "");
                 if (node.hasProperty(INeoConstants.PROPERTY_TYPE_NAME) && node.hasProperty(INeoConstants.PROPERTY_NAME_NAME)
                         && node.getProperty(INeoConstants.PROPERTY_TYPE_NAME).toString().equalsIgnoreCase(NodeTypes.GIS.getId())
                         && gisType.getHeader().equals(type)) {
                     String id = node.getProperty(INeoConstants.PROPERTY_NAME_NAME).toString();
                     datasets.put(id, node);
                 }
             }
             return datasets;
         } finally {
             tx.finish();
         }
     }
 
     public static final HashMap<String, Node> getOSSs() {
         Transaction tx = NeoUtils.beginTransaction();
         try {
             GraphDatabaseService service = NeoServiceProvider.getProvider().getService();
             Node refNode = service.getReferenceNode();
             LinkedHashMap<String, Node> datasets = new LinkedHashMap<String, Node>();
             for (Relationship relationship : refNode.getRelationships(Direction.OUTGOING)) {
                 Node node = relationship.getEndNode();
                 if (node.hasProperty(INeoConstants.PROPERTY_TYPE_NAME)
                         && node.getProperty(INeoConstants.PROPERTY_TYPE_NAME).toString().equalsIgnoreCase(GisTypes.OSS.getHeader())) {
                     String id = node.getProperty(INeoConstants.PROPERTY_NAME_NAME).toString();
                     datasets.put(id, node);
                 }
             }
             return datasets;
         } finally {
             tx.finish();
         }
     }
 
     public static List<String> getAllSites(Node dataset) {
         List<String> sites = new ArrayList<String>();
         Transaction tx = NeoUtils.beginTransaction();
         try {
            Relationship rel = dataset.getSingleRelationship(GeoNeoRelationshipTypes.PROPERTIES, Direction.OUTGOING);
            Node propertiesNode = rel.getEndNode();
             for (Relationship relationship : propertiesNode.getRelationships(GeoNeoRelationshipTypes.CHILD, Direction.OUTGOING)) {
                 Node node = relationship.getEndNode();
                 String name = node.getProperty(INeoConstants.PROPERTY_NAME_NAME).toString();
                 if (name.equals("string")) {
                     // check if dataset has such a property
                     String[] statProps = (String[])node.getProperty("stats_properties");
                     boolean found = false;
                     for (String prop : statProps) {
                         if (prop.equals("site") || prop.equals("site_name")) {
                             found = true;
                             break;
                         }
                     }
                     if (found) {
                         for (Relationship relation : node.getRelationships(GeoNeoRelationshipTypes.PROPERTIES, Direction.OUTGOING)) {
                             String prop = (String)relation.getProperty("property");
                             if (prop.equals("site") || prop.equals("site_name")) {
                                 for (String site : relation.getEndNode().getPropertyKeys()) {
                                     if (!sites.contains(site))
                                         sites.add(site);
                                 }
                             }
                         }
                     } else {
                         // sites.add("No statistics");
                         Traverser siteNodes = dataset.traverse(Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH,
                                 new ReturnableEvaluator() {
 
                                     @Override
                                     public boolean isReturnableNode(TraversalPosition currentPos) {
                                         Node n = currentPos.currentNode();
                                         return n.hasProperty("site") || n.hasProperty("site_name")
                                                 || (n.hasProperty("type") && n.getProperty("type").toString().equals("site"));
                                     }
                                 }, GeoNeoRelationshipTypes.CHILD, Direction.OUTGOING, GeoNeoRelationshipTypes.NEXT,
                                 Direction.OUTGOING);
                         for (Node siteNode : siteNodes) {
                             String type = siteNode.getProperty("type").toString();
                             if (type.equals("site") || type.equals("site_name")) {
                                 String site = siteNode.getProperty("name").toString();
                                 if (!sites.contains(site))
                                     sites.add(site);
                             } else if (siteNode.hasProperty("site")) {
                                 String site = siteNode.getProperty("site").toString();
                                 if (!sites.contains(site))
                                     sites.add(site);
                             } else if (siteNode.hasProperty("site_name")) {
                                 String site = siteNode.getProperty("site_name").toString();
                                 if (!sites.contains(site))
                                     sites.add(site);
                             }
                         }
                     }
                     break;
 
                 }
 
             }

             return sites;
         } finally {
             tx.finish();
         }
 
     }
 
     /**
      * Finds all numeric properties for the node given
      * @param node node to find properties
      * @return
      */
     public static List<String> getProperties(Node node) {
        return Arrays.asList(new PropertyHeader(node).getNumericFields());
     }
 }
