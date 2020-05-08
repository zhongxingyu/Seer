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
 import org.amanzi.neo.core.enums.NetworkRelationshipTypes;
 import org.amanzi.neo.core.service.NeoServiceProvider;
 import org.neo4j.graphdb.Direction;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Relationship;
 import org.neo4j.graphdb.ReturnableEvaluator;
 import org.neo4j.graphdb.StopEvaluator;
 import org.neo4j.graphdb.TraversalPosition;
 import org.neo4j.graphdb.Traverser.Order;
 
 /**
  * <p>
  * Contains information of property
  * </p>
  * 
  * @author Cinkel_A
  * @since 1.0.0
  */
 public class PropertyHeader {
 
     /** String RELATION_PROPERTY field */
     private static final String RELATION_PROPERTY = "property";
     private final Node node;
     private final boolean isGis;
     private final boolean isDataset;
     private final boolean havePropertyNode;
 
     /**
      * Constructor
      * 
      * @param node - gis Node
      */
     public PropertyHeader(Node node) {
         isGis = NeoUtils.isGisNode(node);
         isDataset = !isGis && NeoUtils.isDatasetNode(node);
         this.node = node;
         havePropertyNode = node.hasRelationship(GeoNeoRelationshipTypes.PROPERTIES, Direction.OUTGOING);
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
         if (isGis) {
             return getDataVault().getNeighbourAllFields(neighbourName);
         }
         Node neighbour = NeoUtils.findNeighbour(node, neighbourName);
         if (neighbour == null) {
             return null;
         }
         String[] result = NeoUtils.getAllFields(neighbour);
         return result;
     }
 
     /**
      * get All Fields of Neighbour
      * 
      * @param neighbourName name of neighbour
      * @return array or null
      */
     public String[] getTransmissionAllFields(String neighbourName) {
         if (isGis) {
             return getDataVault().getTransmissionAllFields(neighbourName);
         }
         Node neighbour = NeoUtils.findTransmission(node, neighbourName, NeoServiceProvider.getProvider().getService());
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
 
         return havePropertyNode ? getDefinedNumericFields() : isGis ? getDataVault().getNumericFields() : NeoUtils.getNumericFields(node);
     }
 
     /**
      * get String Fields of current node
      * 
      * @return array or null
      */
     public String[] getStringFields() {
 
         return havePropertyNode ? getDefinedStringFields() : isGis ? getDataVault().getStringFields() : null;
     }
 
     /**
      * get data vault
      * 
      * @return data vault
      */
     public PropertyHeader getDataVault() {
         return isGis || isDataset ? new PropertyHeader(node.getSingleRelationship(GeoNeoRelationshipTypes.NEXT, Direction.OUTGOING).getOtherNode(node)) : this;
     }
 
     public String[] getAllFields() {
         return havePropertyNode ? getDefinedAllFields() : isGis ? getDataVault().getAllFields() : getNumericFields();// NeoUtils.getAllFields(node);
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
             for (Node node : propNode.traverse(Order.BREADTH_FIRST, StopEvaluator.END_OF_GRAPH, ReturnableEvaluator.ALL_BUT_START_NODE, GeoNeoRelationshipTypes.CHILD,
                     Direction.OUTGOING)) {
                 String propType = (String)node.getProperty(INeoConstants.PROPERTY_NAME_NAME, null);
                 String propertyName = propType.equals("string") ? INeoConstants.PROPERTY_STATS : INeoConstants.PROPERTY_DATA;
                 String[] properties = (String[])node.getProperty(propertyName, null);
                 if (propType != null && properties != null) {
                     result.addAll(Arrays.asList(properties));
                 }
             }
 
         }
         return result.toArray(new String[0]);
     }
 
     private String[] getDefinedNumericFields() {
         List<String> ints = new ArrayList<String>();
         List<String> floats = new ArrayList<String>();
         List<String> longs = new ArrayList<String>();
         List<String> result = new ArrayList<String>();
         Relationship propRel = node.getSingleRelationship(GeoNeoRelationshipTypes.PROPERTIES, Direction.OUTGOING);
         if (propRel != null) {
             Node propNode = propRel.getEndNode();
             for (Node node : propNode.traverse(Order.BREADTH_FIRST, StopEvaluator.END_OF_GRAPH, ReturnableEvaluator.ALL_BUT_START_NODE, GeoNeoRelationshipTypes.CHILD,
                     Direction.OUTGOING)) {
                 String propType = (String)node.getProperty(INeoConstants.PROPERTY_NAME_NAME, null);
                 String[] properties = (String[])node.getProperty(INeoConstants.PROPERTY_DATA, null);
                 if (propType != null && properties != null) {
                     if (propType.equals("integer")) {
                         ints.addAll(Arrays.asList(properties));
                     } else if (propType.equals("float")) {
                         floats.addAll(Arrays.asList(properties));
                     } else if (propType.equals("long")) {
                         longs.addAll(Arrays.asList(properties));
                     }
                 }
             }
         }
         result.addAll(ints);
         result.addAll(floats);
         result.addAll(longs);
         return result.toArray(new String[0]);
     }
 
     private String[] getDefinedStringFields() {
         List<String> result = new ArrayList<String>();
         Relationship propRel = node.getSingleRelationship(GeoNeoRelationshipTypes.PROPERTIES, Direction.OUTGOING);
         if (propRel != null) {
             Node propNode = propRel.getEndNode();
             for (Node node : propNode.traverse(Order.BREADTH_FIRST, StopEvaluator.END_OF_GRAPH, ReturnableEvaluator.ALL_BUT_START_NODE, GeoNeoRelationshipTypes.CHILD,
                     Direction.OUTGOING)) {
                 String propType = (String)node.getProperty(INeoConstants.PROPERTY_NAME_NAME, null);
                 String[] properties = (String[])node.getProperty(INeoConstants.PROPERTY_DATA, null);
                 if (propType != null && properties != null) {
                     if (propType.equals("string")) {
                         result.addAll(Arrays.asList(properties));
                     }
                 }
             }
         }
         return result.toArray(new String[0]);
     }
 
     /**
      * get list of properties
      * 
      * @return AllChannels properties
      */
     public String[] getAllChannels() {
         return isGis ? getDataVault().getAllChannels() : (String[])node.getProperty(INeoConstants.PROPERTY_ALL_CHANNELS_NAME, null);
     }
 
     /**
      * gets list of Neighbour properties in network
      * 
      * @return Collection
      */
     public Collection<String> getNeighbourList() {
         List<String> result = new ArrayList<String>();
         if (isGis) {
             return getDataVault().getNeighbourList();
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
         if (isGis) {
             return getDataVault().getNeighbourIntegerFields(neighbourName);
         }
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
     public String[] getTransmissionIntegerFields(String neighbourName) {
         if (isGis) {
             return getDataVault().getTransmissionIntegerFields(neighbourName);
         }
         Node neighbour = NeoUtils.findTransmission(node, neighbourName, NeoServiceProvider.getProvider().getService());
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
         if (isGis) {
             return getDataVault().getNeighbourDoubleFields(neighbourName);
         }
         Node neighbour = NeoUtils.findNeighbour(node, neighbourName);
         if (neighbour == null) {
             return null;
         }
         String[] result = (String[])neighbour.getProperty(INeoConstants.LIST_DOUBLE_PROPERTIES, null);
         return result;
     }
 
     /**
      * @param neighbourName
      * @return
      */
     public String[] getTransmissionDoubleFields(String neighbourName) {
         if (isGis) {
             return getDataVault().getTransmissionDoubleFields(neighbourName);
         }
         Node neighbour = NeoUtils.findTransmission(node, neighbourName, NeoServiceProvider.getProvider().getService());
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
                     Object property = currentPos.lastRelationshipTraversed().getProperty(RELATION_PROPERTY, "");
                     // Pechko_E event property name for TEMS and ROMES is "event_type"
                     // Pechko_E for Nemo is "event_id"
                     return "event_type".equals(property) || "event_id".equals(property);
                 }
             }, GeoNeoRelationshipTypes.CHILD, Direction.OUTGOING, GeoNeoRelationshipTypes.PROPERTIES, Direction.OUTGOING).iterator();
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
         if (isGis) {
             return getDataVault().getPropertyNode(propertyName);
         }
         if (propertyName == null) {
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
 
     /**
      * @return
      */
     public String[] getSectorOrMeasurmentNames() {
         // if (GisTypes.NETWORK != gisType) {
         // return null;
         // }
         if (isGis) {
             return getDataVault().getSectorOrMeasurmentNames();
         }
         Set<String> result = new HashSet<String>();
         Relationship propRel = node.getSingleRelationship(GeoNeoRelationshipTypes.PROPERTIES, Direction.OUTGOING);
         if (propRel != null) {
             Node propNode = propRel.getEndNode();
             for (Node node : propNode.traverse(Order.BREADTH_FIRST, StopEvaluator.END_OF_GRAPH, ReturnableEvaluator.ALL_BUT_START_NODE, GeoNeoRelationshipTypes.CHILD,
                     Direction.OUTGOING)) {
                 String propType = (String)node.getProperty(INeoConstants.PROPERTY_NAME_NAME, null);
                 String propertyName = INeoConstants.PROPERTY_DATA;
                 String[] properties = (String[])node.getProperty(propertyName, null);
                 if (propType != null && properties != null) {
                     result.addAll(Arrays.asList(properties));
                 }
             }
 
         }
         return result.toArray(new String[0]);
     }
 
     /**
      * <p>
      * Contains information about property statistics
      * </p>
      * 
      * @author Cinkel_A
      * @since 1.0.0
      */
     public static class PropertyStatistics {
         private final Relationship statisticsRelation;
         private final Node typeNode;
         private final Node valueNode;
 
         /**
          * Constructor
          * 
          * @param statisticsRelation
          * @param typeNode
          * @param valueNode
          */
         public PropertyStatistics(Relationship statisticsRelation, Node typeNode, Node valueNode) {
             super();
             this.statisticsRelation = statisticsRelation;
             this.typeNode = typeNode;
             this.valueNode = valueNode;
         }
 
         /**
          * @return Returns the statisticsRelation.
          */
         public Relationship getStatisticsRelation() {
             return statisticsRelation;
         }
 
         /**
          * @return Returns the typeNode.
          */
         public Node getTypeNode() {
             return typeNode;
         }
 
         /**
          * @return Returns the valueNode.
          */
         public Node getValueNode() {
             return valueNode;
         }
 
         public Pair<Double, Double> getMinMax() {
             return new Pair<Double, Double>((Double)statisticsRelation.getProperty(INeoConstants.MIN_VALUE, null), (Double)statisticsRelation.getProperty(
                     INeoConstants.MAX_VALUE, null));
         }
 
         /**
          *get wrapped value depends of type of property
          * 
          * @param value - number value
          * @return (cast to type of property)value
          */
         public Object getWrappedValue(Number value) {
             if (value == null) {
                 return null;
             }
             String name = NeoUtils.getSimpleNodeName(typeNode, "");
             if (name.equals("long")) {
                 return value.longValue();
             }
             if (name.equals("float")) {
                 return value.floatValue();
             }
             if (name.equals("integer")) {
                 return value.intValue();
             }
             if (name.equals("double")) {
                 return value.doubleValue();
             }
             // string value
             return value.toString();
         }
     }
 
     /**
      * @param propertyName
      * @return
      */
     public PropertyStatistics getPropertyStatistic(final String propertyName) {
         if (isGis) {
             return getDataVault().getPropertyStatistic(propertyName);
         }
         if (havePropertyNode) {
             Node property = node.getSingleRelationship(GeoNeoRelationshipTypes.PROPERTIES, Direction.OUTGOING).getOtherNode(node);
             Iterator<Node> iterator = property.traverse(Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH, new ReturnableEvaluator() {
 
                 @Override
                 public boolean isReturnableNode(TraversalPosition currentPos) {
                     Relationship relation = currentPos.lastRelationshipTraversed();
                     return relation != null && relation.isType(GeoNeoRelationshipTypes.PROPERTIES) && relation.getProperty("property", "").equals(propertyName);
                 }
             }, GeoNeoRelationshipTypes.CHILD, Direction.OUTGOING, GeoNeoRelationshipTypes.PROPERTIES, Direction.OUTGOING).iterator();
             if (iterator.hasNext()) {
                 Node node = iterator.next();
                 Relationship relation = node.getSingleRelationship(GeoNeoRelationshipTypes.PROPERTIES, Direction.INCOMING);
                 PropertyStatistics result = new PropertyStatistics(relation, relation.getOtherNode(node), node);
                 return result;
             }
         }
         return null;
     }
 }
