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
 
 package org.amanzi.neo.core.utils;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.amanzi.neo.core.INeoConstants;
 import org.amanzi.neo.core.enums.GeoNeoRelationshipTypes;
 import org.amanzi.neo.core.enums.GisTypes;
 import org.amanzi.neo.core.enums.NetworkRelationshipTypes;
 import org.neo4j.api.core.Direction;
 import org.neo4j.api.core.Node;
 import org.neo4j.api.core.Relationship;
 import org.neo4j.api.core.ReturnableEvaluator;
 import org.neo4j.api.core.StopEvaluator;
 import org.neo4j.api.core.Transaction;
 import org.neo4j.api.core.TraversalPosition;
 import org.neo4j.api.core.Traverser.Order;
 
 /**
  * <p>
  * Contains information of property
  * </p>
  * @author Cinkel_A
  * @since 1.0.0
  */
 public class PropertyHeader {
 
 
     /** String RELATION_PROPERTY field */
     private static final String RELATION_PROPERTY = "property";
     private final Node node;
     private boolean isGis;
     private GisTypes gisType;
 
     /**
      * Constructor
      * 
      * @param node - gis Node
      */
     public PropertyHeader(Node node) {
         isGis=NeoUtils.isGisNode(node);
             gisType=isGis?GisTypes.findGisTypeByHeader((String)node.getProperty(INeoConstants.PROPERTY_GIS_TYPE_NAME,null)):null;
         this.node = node;
     }
 
     /**
      * get Numeric Fields of Neighbour
      * 
      * @param neighbourName name of neighbour
      * @return array or null
      */
     public String[] getNeighbourNumericFields(String neighbourName) {
         Node neighbour = NeoUtils.findNeighbour(node, neighbourName);
         if (neighbour == null) {
             return null;
         }
         String[] result = NeoUtils.getNumericFields(neighbour);
         return result;
     }
 
     /**
      * get All Fields of Neighbour
      * 
      * @param neighbourName name of neighbour
      * @return array or null
      */
     public String[] getNeighbourAllFields(String neighbourName) {
         Node neighbour = NeoUtils.findNeighbour(node, neighbourName);
         if (neighbour == null) {
             return null;
         }
         String[] result = NeoUtils.getAllFields(neighbour);
         return result;
     }
 
     /**
      * get Numeric Fields of current node
      * 
      * @return array or null
      */
     public String[] getNumericFields() {
         
         return isGis ? (gisType == GisTypes.DRIVE ? getDefinedNumericFields() : getDataVault().getNumericFields()) : NeoUtils
                 .getNumericFields(node);
     }
 
     /**
      * get data vault
      * 
      * @return data vault
      */
     public PropertyHeader getDataVault() {
         return isGis?new PropertyHeader(node.getSingleRelationship(GeoNeoRelationshipTypes.NEXT, Direction.OUTGOING).getOtherNode(node)):this;
     }
 
     public String[] getAllFields() {
         return isGis ? (gisType == GisTypes.DRIVE ? getDefinedAllFields() : getDataVault().getAllFields()) : NeoUtils
                 .getAllFields(node);
     }
 
     /**
      *Get all defined fields from drive gis node
      * 
      * @return
      */
     private String[] getDefinedAllFields() {
         List<String> result = new ArrayList<String>();
         Relationship propRel = node.getSingleRelationship(GeoNeoRelationshipTypes.PROPERTIES, Direction.OUTGOING);
         if (propRel != null) {
             Node propNode = propRel.getEndNode();
             for (Node node : propNode.traverse(Order.BREADTH_FIRST, StopEvaluator.END_OF_GRAPH,
                     ReturnableEvaluator.ALL_BUT_START_NODE, GeoNeoRelationshipTypes.CHILD, Direction.OUTGOING)) {
                 String propType = (String)node.getProperty(INeoConstants.PROPERTY_NAME_NAME, null);
                 String[] properties = (String[])node.getProperty(INeoConstants.NODE_TYPE_PROPERTIES, null);
                 if (propType != null && properties != null) {
                     result.addAll(Arrays.asList(properties));
                 }
             }
 
         }
         return result.toArray(new String[0]);
     }
 
     /**
      * @return
      */
     public String[] getNetworkNumericFields() {
         // TODO refactored
         List<String> result = new ArrayList<String>();
         for (Relationship relation : node.getRelationships(NetworkRelationshipTypes.NEIGHBOUR_DATA, Direction.OUTGOING)) {
             String name = String.format("# '%s' neighbours", NeoUtils.getSimpleNodeName(relation.getOtherNode(node), ""));
             result.add(name);
         }
         return result.toArray(new String[0]);
     }
     
    private String[] getDefinedNumericFields() {
         List<String> ints = new ArrayList<String>();
         List<String> floats = new ArrayList<String>();
         List<String> result = new ArrayList<String>();
         Relationship propRel = node.getSingleRelationship(GeoNeoRelationshipTypes.PROPERTIES, Direction.OUTGOING);
         if (propRel != null) {
             Node propNode = propRel.getEndNode();
             for (Node node : propNode.traverse(Order.BREADTH_FIRST, StopEvaluator.END_OF_GRAPH,
                     ReturnableEvaluator.ALL_BUT_START_NODE, GeoNeoRelationshipTypes.CHILD, Direction.OUTGOING)) {
                 String propType = (String)node.getProperty(INeoConstants.PROPERTY_NAME_NAME, null);
                 String[] properties = (String[])node.getProperty(INeoConstants.NODE_TYPE_PROPERTIES, null);
                 if (propType != null && properties != null) {
                     if (propType.equals("integer")) {
                         ints.addAll(Arrays.asList(properties));
                     } else if (propType.equals("float")) {
                         floats.addAll(Arrays.asList(properties));
                     }
                 }
             }
         }
         result.addAll(ints);
         result.addAll(floats);
         return result.toArray(new String[0]);
     }
 
     /**
      * Get network vault
      * 
      * @param gisNode - gis node
      * @return network vault
      */
     public static PropertyHeader getNetworkVault(Node gisNode) {
         Transaction tx = NeoUtils.beginTransaction();
         try {
             Relationship singleRelationship = gisNode.getSingleRelationship(GeoNeoRelationshipTypes.NEXT, Direction.OUTGOING);
             if (singleRelationship == null) {
                 return null;
             }
             return new PropertyHeader(singleRelationship.getOtherNode(gisNode));
         } finally {
             tx.finish();
         }
     }
 
     /**
      * get Azimuth
      * 
      * @param node node, that contains information
      * @return Azimuth
      */
     public Double getAzimuth(Node node){
         String[] azimuthList = getAzimuthList();// use cache?
         for (String property : azimuthList) {
             if (node.hasProperty(property)){
                 return ((Number) node.getProperty(property)).doubleValue();
             }
         }
         return null;
     }
 
     /**
      * get list of properties
      * 
      * @return azimuth properties
      */
     public String[] getAzimuthList() {
         return (String[])node.getProperty(INeoConstants.PROPERTY_AZIMUTH_NAME, null);
     }
 
     /**
      * Get beamwidth
      * 
      * @param child node
      * @param defValue default value
      * @return beamwidth
      */
     public double getBeamwidth(Node child, Double defValue) {
         String[] beamwidt = getBeamwidthList();// TODO use cache?
         if (beamwidt == null) {
             return defValue;
         }
         for (String property : beamwidt) {
             if (child.hasProperty(property)) {
                 return ((Number)child.getProperty(property)).doubleValue();
             }
         }
         return defValue;
     }
 
     /**
      * get list of properties
      * 
      * @return Beamwidth properties
      */
     public String[] getBeamwidthList() {
         return (String[])node.getProperty(INeoConstants.PROPERTY_BEAMWIDTH_NAME, null);
     }
 
     /**
      * get list of properties
      * 
      * @return AllChannels properties
      */
     public String[] getAllChannels() {
         return isGis?getDataVault().getAllChannels():(String[])node.getProperty(INeoConstants.PROPERTY_ALL_CHANNELS_NAME, null);
     }
 
     /**
      * gets list of Neighbour properties in network
      * 
      * @return Collection
      */
     public Collection<String> getNeighbourList() {
         List<String> result = new ArrayList<String>();
         if (!isGis || gisType == GisTypes.DRIVE) {
             return result;
         }
         Iterable<Relationship> neighb = node.getRelationships(NetworkRelationshipTypes.NEIGHBOUR_DATA, Direction.OUTGOING);
         for (Relationship relationship : neighb) {
             result.add(NeoUtils.getNeighbourPropertyName(NeoUtils.getSimpleNodeName(relationship.getOtherNode(node), "")));
         }
         return result;
     }
 
     /**
      * @param neighbourName
      * @return
      */
     public String[] getNeighbourIntegerFields(String neighbourName) {
         Node neighbour = NeoUtils.findNeighbour(node, neighbourName);
         if (neighbour == null) {
             return null;
         }
         String[] result = (String[])neighbour.getProperty(INeoConstants.LIST_INTEGER_PROPERTIES, null);
         return result;
     }
 
     /**
      * @param neighbourName
      * @return
      */
     public String[] getNeighbourDoubleFields(String neighbourName) {
         Node neighbour = NeoUtils.findNeighbour(node, neighbourName);
         if (neighbour == null) {
             return null;
         }
         String[] result = (String[])neighbour.getProperty(INeoConstants.LIST_DOUBLE_PROPERTIES, null);
         return result;
     }
 
     /**
      * @return list of possible event
      */
     public Collection<String> getEvents() {
         if (GisTypes.DRIVE != gisType) {
             return null;
         }
         Set<String> result = new HashSet<String>();
         Relationship propRel = node.getSingleRelationship(GeoNeoRelationshipTypes.PROPERTIES, Direction.OUTGOING);
         if (propRel != null) {
             Node propNode = propRel.getEndNode();
             Iterator<Node> iterator = propNode.traverse(Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH, new ReturnableEvaluator() {
 
                 @Override
                 public boolean isReturnableNode(TraversalPosition currentPos) {
                     if (currentPos.isStartNode()) {
                         return false;
                     }
                     return "event_type".equals(currentPos.lastRelationshipTraversed().getProperty(RELATION_PROPERTY, ""));
                 }
             }, GeoNeoRelationshipTypes.CHILD, Direction.OUTGOING, GeoNeoRelationshipTypes.PROPERTIES, Direction.OUTGOING)
                     .iterator();
             if (iterator.hasNext()) {
                 Iterator<String> propertyKeys = iterator.next().getPropertyKeys().iterator();
                 while (propertyKeys.hasNext()) {
                     result.add(propertyKeys.next());
                 }
             }
         }
         return result;
     }
 
     /**
      * Get property node of necessary property
      * 
      * @param propertyName - property name
      * @return node
      */
     public Node getPropertyNode(final String propertyName) {
         if (GisTypes.DRIVE != gisType || propertyName == null) {
             return null;
         }
         Iterator<Node> iterator = node.traverse(Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH, new ReturnableEvaluator() {
 
             @Override
             public boolean isReturnableNode(TraversalPosition currentPos) {
                 if (currentPos.isStartNode()) {
                     return false;
                 }
                 Relationship relation = currentPos.lastRelationshipTraversed();
                 return relation.getType().equals(GeoNeoRelationshipTypes.PROPERTIES) && relation.hasProperty(RELATION_PROPERTY)
                         && relation.getProperty(RELATION_PROPERTY).equals(propertyName);
             }
         }, GeoNeoRelationshipTypes.PROPERTIES, Direction.OUTGOING, NetworkRelationshipTypes.CHILD, Direction.OUTGOING).iterator();
         return iterator.hasNext() ? iterator.next() : null;
     }
 }
