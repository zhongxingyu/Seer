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
 
 package org.amanzi.awe.views.tree.drive.views;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import org.amanzi.awe.views.calls.enums.StatisticsCallType;
 import org.amanzi.awe.views.network.proxy.NeoNode;
 import org.amanzi.neo.core.INeoConstants;
 import org.amanzi.neo.core.database.nodes.StatisticSelectionNode;
 import org.amanzi.neo.core.enums.CallProperties;
 import org.amanzi.neo.core.enums.GeoNeoRelationshipTypes;
 import org.amanzi.neo.core.enums.NodeTypes;
 import org.amanzi.neo.core.enums.ProbeCallRelationshipType;
 import org.amanzi.neo.core.utils.NeoUtils;
 import org.neo4j.graphdb.Direction;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Relationship;
 import org.neo4j.graphdb.ReturnableEvaluator;
 import org.neo4j.graphdb.StopEvaluator;
 import org.neo4j.graphdb.TraversalPosition;
 import org.neo4j.graphdb.Traverser.Order;
 
 /**
  * TODO Purpose of 
  * <p>
  *
  * </p>
  * @author Lagutko_N
  * @since 1.0.0
  */
 public class CallAnalyzisNeoNode extends DriveNeoNode {
     
     private final String type;
     
     private Node statisticsNode;
 
     /**
      * @param node
      */
     public CallAnalyzisNeoNode(Node node, int number) {
         super(node, number);
         
         type = NeoUtils.getNodeType(node);
         
         if (type.equals(NodeTypes.CALL_ANALYSIS_ROOT.getId())) {
             name = "Call Analysis: " + node.getProperty(INeoConstants.PROPERTY_VALUE_NAME) + " (" + node.getProperty(CallProperties.CALL_TYPE.getId()) + ")";
         }        
         else if (type.equals(NodeTypes.S_CELL.getId())) {
             name = node.getProperty(INeoConstants.PROPERTY_NAME_NAME) + ": " + node.getProperty(INeoConstants.PROPERTY_VALUE_NAME,"");
         }        
         hasChildren();
     }
     
     protected CallAnalyzisNeoNode(Node probeNode, Node statisticsNode, int number) {
         this(probeNode, number);
         
         this.statisticsNode = statisticsNode; 
         hasChildren();
     }
     
     @Override
     public NeoNode[] getChildren() {
         ArrayList<NeoNode> children = new ArrayList<NeoNode>();
         
         Iterator<Node> iterator;
         if (type.equals(NodeTypes.CALL_ANALYSIS_ROOT.getId())) {
             iterator = node.traverse(Order.BREADTH_FIRST, StopEvaluator.DEPTH_ONE, ReturnableEvaluator.ALL_BUT_START_NODE, 
                                      GeoNeoRelationshipTypes.CHILD, Direction.OUTGOING).iterator();
         }
         else if (type.equals(NodeTypes.CALL_ANALYSIS.getId())) {
             StatisticsCallType callType = StatisticsCallType.valueOf((String)getParent().getNode().getProperty(CallProperties.CALL_TYPE.getId()));          
             if (callType.getLevel().equals(StatisticsCallType.FIRST_LEVEL)) {
                 Node root = node.getSingleRelationship(GeoNeoRelationshipTypes.CHILD, Direction.INCOMING).getStartNode();
                 Node dataset = root.getSingleRelationship(ProbeCallRelationshipType.CALL_ANALYSIS, Direction.INCOMING).getStartNode();
                 iterator = NeoUtils.getAllProbesOfDataset(dataset, callType.getId()).iterator();
             }else{
                 iterator = NeoUtils.getChildTraverser(node).iterator();
             }
         }
         else if (type.equals(NodeTypes.PROBE.getId())) {
             iterator = NeoUtils.getChildTraverser(statisticsNode, new ReturnableEvaluator() {
                 
                 @Override
                 public boolean isReturnableNode(TraversalPosition currentPos) {
                     Node node2 = currentPos.currentNode();
                     Node probe = node2.traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, new ReturnableEvaluator() {
 
                         @Override
                         public boolean isReturnableNode(TraversalPosition currentPos) {
                             return NeoUtils.isProbeNode(currentPos.currentNode());
                         }
                     }, GeoNeoRelationshipTypes.SOURCE, Direction.OUTGOING).iterator().next();
                     
                     return probe.equals(node);
                 }
             }).iterator();
         }
         else if (type.equals(NodeTypes.S_CELL.getId())) {
             iterator = node.traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, new ReturnableEvaluator() {
                 
                 @Override
                 public boolean isReturnableNode(TraversalPosition currentPos) {
                     Node currentNode = currentPos.currentNode();                    
                     return (!currentPos.isStartNode()) && (NeoUtils.isCallNode(currentNode)||NeoUtils.isScellNode(currentNode));
                 }
             }, GeoNeoRelationshipTypes.SOURCE, Direction.OUTGOING).iterator();
         }
         else if (type.equals(NodeTypes.CALL.getId())) {
             iterator = node.traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE,ReturnableEvaluator.ALL_BUT_START_NODE,ProbeCallRelationshipType.CALL_M, Direction.OUTGOING).iterator();
         } else {
             iterator = NeoUtils.getChildTraverser(node).iterator();
         }
         int nextNum = number+1;
         while (iterator.hasNext()) {
             Node child = iterator.next();
             // TODO refactoring
             children.add(new CallAnalyzisNeoNode(child, node, nextNum++));
             // if (++i <= TRUNCATE_NODE) {
             // } else {
             // children.add(new AggregatesNode(child));
             // break;
             // }
         }        
         return children.toArray(NO_NODES);
     }
     
     @Override
     public boolean hasChildren() {
        if (type.equals(NodeTypes.PROBE.getId())) {
            return node.hasRelationship(GeoNeoRelationshipTypes.SOURCE, Direction.INCOMING);
        }
        if (type.equals(NodeTypes.S_CELL.getId())) {
            return node.hasRelationship(GeoNeoRelationshipTypes.SOURCE, Direction.OUTGOING);
        }
        if (type.equals(NodeTypes.CALL.getId())) {
            return node.hasRelationship(ProbeCallRelationshipType.CALL_M, Direction.OUTGOING);
        }
        return node.hasRelationship(GeoNeoRelationshipTypes.CHILD, Direction.OUTGOING);
     }
 
     /**
      * @return
      */
     public NeoNode getParent() {
         int nextNum = number+1;
         if (type.equals(NodeTypes.PROBE.getId())) {
             return new CallAnalyzisNeoNode(statisticsNode, statisticsNode, nextNum);
         } else if (type.equals(NodeTypes.S_ROW.getId())) {
             Iterator<Node> probeIterator = node.traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, new ReturnableEvaluator() {
 
                 @Override
                 public boolean isReturnableNode(TraversalPosition currentPos) {
                     return NeoUtils.isProbeNode(currentPos.currentNode());
                 }
             }, GeoNeoRelationshipTypes.SOURCE, Direction.OUTGOING).iterator();
             Node parentNode;
             if (probeIterator.hasNext()) {
                 parentNode = probeIterator.next();
             }else{
                 parentNode = node.traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, new ReturnableEvaluator() {
 
                     @Override
                     public boolean isReturnableNode(TraversalPosition currentPos) {
                         return !currentPos.isStartNode();
                     }
                 }, GeoNeoRelationshipTypes.CHILD, Direction.INCOMING).iterator().next();
             }
             return new CallAnalyzisNeoNode(parentNode, statisticsNode, nextNum);
         } else {
             return new CallAnalyzisNeoNode(NeoUtils.getParent(null, node), statisticsNode, nextNum);
         }
     }
 
     @Override
     public boolean equals(Object obj) {
         boolean result = super.equals(obj);
         if (result) {
             if (obj instanceof CallAnalyzisNeoNode && NeoUtils.getNodeType(node, "").equals(NodeTypes.PROBE.getId())) {
                 return statisticsNode.equals(((CallAnalyzisNeoNode)obj).statisticsNode);
             }
             if (obj instanceof StatisticSelectionNode && statisticsNode != null && NeoUtils.getNodeType(node, "").equals(NodeTypes.PROBE.getId())) {
                 StatisticSelectionNode statNode = (StatisticSelectionNode)obj;
                 Node clNode = statNode.getClarifyingNode();
                 if (statisticsNode.equals(clNode)) {
                     return true;
                 }
                 Relationship rel = clNode.getSingleRelationship(GeoNeoRelationshipTypes.CHILD, Direction.INCOMING);
                 if (rel != null) {
                     return statisticsNode.equals(rel.getOtherNode(clNode));
                 }
             }
         }
         return result;
     }
 }
