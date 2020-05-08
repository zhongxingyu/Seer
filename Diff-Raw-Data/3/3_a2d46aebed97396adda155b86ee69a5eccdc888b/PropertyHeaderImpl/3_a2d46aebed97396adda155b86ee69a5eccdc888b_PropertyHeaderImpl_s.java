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
 
 package org.amanzi.neo.services.statistic.internal;
 
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.amanzi.neo.db.manager.NeoServiceProvider;
 import org.amanzi.neo.services.INeoConstants;
 import org.amanzi.neo.services.enums.GeoNeoRelationshipTypes;
 import org.amanzi.neo.services.enums.INodeType;
 import org.amanzi.neo.services.enums.NetworkRelationshipTypes;
 import org.amanzi.neo.services.enums.NodeTypes;
 import org.amanzi.neo.services.statistic.IPropertyHeader;
 import org.amanzi.neo.services.statistic.ISinglePropertyStat;
 import org.amanzi.neo.services.statistic.IStatistic;
 import org.amanzi.neo.services.statistic.StatisticManager;
 import org.amanzi.neo.services.utils.Utils;
 import org.neo4j.graphdb.Direction;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Relationship;
 import org.neo4j.graphdb.ReturnableEvaluator;
 import org.neo4j.graphdb.StopEvaluator;
 import org.neo4j.graphdb.Traverser.Order;
 
 /**
  * TODO Purpose of
  * <p>
  * </p>
  * 
  * @author tsinkel_a
  * @since 1.0.0
  */
 public class PropertyHeaderImpl implements IPropertyHeader {
 
     private final Node node;
     private final String key;
     private IStatistic stat;
 
     public PropertyHeaderImpl(Node node, String key) {
         this.node = node;
         this.key = key;
         stat = StatisticManager.getStatistic(node);
     }
 
     @Override
     public String[] getNeighbourNumericFields(String neighbourName) {
         // TODO old version
         Node neighbour = Utils.findNeighbour(node, neighbourName);
         if (neighbour == null) {
             return null;
         }
         String[] result = Utils.getNumericFields(neighbour);
         return result;
     }
 
     @Override
     public String[] getNeighbourAllFields(String neighbourName) {
         Node neighbour = Utils.findNeighbour(node, neighbourName);
         if (neighbour == null) {
             return null;
         }
         String[] result = Utils.getAllFields(neighbour);
         return result;
     }
 
     @Override
     public String[] getTransmissionAllFields(String neighbourName) {
         Node neighbour = Utils.findTransmission(node, neighbourName, NeoServiceProvider.getProvider().getService());
         if (neighbour == null) {
             return null;
         }
         String[] result = Utils.getAllFields(neighbour);
         return result;
     }
 
     @Override
     public String[] getNumericFields(String nodeTypeId) {
         // TODO remove "-main-type-" from AWE code
         if ("-main-type-".equals(nodeTypeId)) {
             if (NodeTypes.NETWORK.checkNode(node)) {
                 nodeTypeId = NodeTypes.SECTOR.getId();
             } else {
                 nodeTypeId = NodeTypes.M.getId();
             }
         }
 
         Collection<String> result = stat.getPropertyNameCollection(key, nodeTypeId, new Comparable<Class>() {
 
             @Override
             public int compareTo(Class o) {
                 return Number.class.isAssignableFrom(o) ? 0 : -1;
             }
         });
         return result.toArray(new String[0]);
     }
 
     @Override
     public String[] getAllFields(String nodeTypeId) {
         // TODO remove "-main-type-" from AWE code
         if ("-main-type-".equals(nodeTypeId)) {
             if (NodeTypes.NETWORK.checkNode(node)) {
                 nodeTypeId = NodeTypes.SECTOR.getId();
             } else {
                 nodeTypeId = NodeTypes.M.getId();
             }
         }
 
         Collection<String> result = stat.getPropertyNameCollection(key, nodeTypeId, new Comparable<Class>() {
 
             @Override
             public int compareTo(Class o) {
                 return 0;
             }
         });
         return result.toArray(new String[0]);
     }
 
     @Override
     public String[] getAllChannels() {
         // TODO implement!
         return null;
     }
 
     @Override
     public Collection<String> getNeighbourList() {
         List<String> result = new ArrayList<String>();
         Iterable<Relationship> neighb = node.getRelationships(NetworkRelationshipTypes.NEIGHBOUR_DATA, Direction.OUTGOING);
         for (Relationship relationship : neighb) {
             result.add(Utils.getNeighbourPropertyName(Utils.getSimpleNodeName(relationship.getOtherNode(node), "")));
         }
         return result;
     }
 
     @Override
     public String[] getNeighbourIntegerFields(String neighbourName) {
         Node neighbour = Utils.findNeighbour(node, neighbourName);
         if (neighbour == null) {
             return null;
         }
         String[] result = (String[])neighbour.getProperty(INeoConstants.LIST_INTEGER_PROPERTIES, null);
         return result;
     }
 
     @Override
     public String[] getTransmissionIntegerFields(String neighbourName) {
         Node neighbour = Utils.findTransmission(node, neighbourName, NeoServiceProvider.getProvider().getService());
         if (neighbour == null) {
             return null;
         }
         String[] result = (String[])neighbour.getProperty(INeoConstants.LIST_INTEGER_PROPERTIES, null);
         return result;
     }
 
     @Override
     public String[] getNeighbourDoubleFields(String neighbourName) {
         Node neighbour = Utils.findNeighbour(node, neighbourName);
         if (neighbour == null) {
             return null;
         }
         String[] result = (String[])neighbour.getProperty(INeoConstants.LIST_DOUBLE_PROPERTIES, null);
         return result;
     }
 
     @Override
     public Collection<String> getEvents() {
         Set<String> result = new HashSet<String>();
         ISinglePropertyStat events = getPropertyStatistic(NodeTypes.M.getId(), "event_type");
         if (events != null) {
             for (Map.Entry<Object, Long> entry : events.getValueMap().entrySet()) {
 
             }
         }
         return result;
     }
 
     @Override
     public String[] getTransmissionDoubleFields(String neighbourName) {
         Node neighbour = Utils.findTransmission(node, neighbourName, NeoServiceProvider.getProvider().getService());
         if (neighbour == null) {
             return null;
         }
         String[] result = (String[])neighbour.getProperty(INeoConstants.LIST_DOUBLE_PROPERTIES, null);
         return result;
     }
 
     @Override
     public ISinglePropertyStat getPropertyStatistic(String nodeTypeId, String propertyName) {
         return stat.findPropertyStatistic(key, nodeTypeId, propertyName);
     }
 
     @Override
     public boolean isHavePropertyNode() {
         return true;
     }
 
     @Override
     public Map<String, Object> getStatisticParams(INodeType type) {
         HashMap<String, Object> result = new HashMap<String, Object>();
         Collection<String> propertyNames = stat.getPropertyNameCollection(key, type.getId(), null);
         for (String propertyName : propertyNames) {
             ISinglePropertyStat stat = getPropertyStatistic(type.getId(), propertyName);
             if (stat != null) {
                 Object value = null;
                 long count = -1;
                 for (Map.Entry<Object, Long> entry : stat.getValueMap().entrySet()) {
                     if (entry.getValue() > count) {
                         count = entry.getValue();
                         value = entry.getKey();
                     }
                 }
                 if (value == null) {
                     value = getDefValueForType(stat.getType());
                 }
                 if (value != null) {
                     result.put(propertyName, value);
                 }
             }
 
         }
         return result;
     }
 
     /**
      * @param type
      * @return
      */
     private Object getDefValueForType(Class type) {
         // TODO fully implement
         try {
             if (Number.class.isAssignableFrom(type)) {
                 Method m = type.getMethod("valueOf", String.class);
                 return m.invoke(null, "0");
             }
             if (String.class == type) {
                 return "none";
             }
         } catch (Exception e) {
             // TODO: handle exception
             e.printStackTrace();
         }
         return null;
     }
 
     public String[] getIdentityFields() {
         List<String> result = new ArrayList<String>();
         Relationship rel = node.getSingleRelationship(GeoNeoRelationshipTypes.PROPERTIES, Direction.OUTGOING);
         if (rel != null) {
             Node propertyNode = rel.getEndNode();
             for (Node node : propertyNode.traverse(Order.BREADTH_FIRST, StopEvaluator.DEPTH_ONE, ReturnableEvaluator.ALL_BUT_START_NODE, GeoNeoRelationshipTypes.CHILD,
                     Direction.OUTGOING)) {
                 result.addAll(Arrays.asList((String[])node.getProperty("identity_properties")));
             }
         }
         return result.toArray(new String[result.size()]);
     }
 
     @Override
     public <T> boolean updateStatistic(String nodeTypeId, String propertyName, T newValue, T oldValue) {
         boolean result = stat.updateValue(key, nodeTypeId, propertyName, newValue, oldValue);
         if (result) {
             stat.save();
         }
         return result;
     }
 
     @Override
     public boolean updateStatisticCount(String nodeTypeId, long count) {
         stat.updateTypeCount(key, nodeTypeId, count);
         stat.save();
         return true;
     }
 }
