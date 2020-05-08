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
 package org.amanzi.awe.views.network.proxy;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Iterator;
 
 import org.amanzi.neo.core.INeoConstants;
 import org.amanzi.neo.core.enums.NetworkRelationshipTypes;
 import org.amanzi.neo.core.enums.NodeTypes;
 import org.amanzi.neo.core.enums.ProbeCallRelationshipType;
 import org.amanzi.neo.core.service.NeoServiceProvider;
 import org.amanzi.neo.core.utils.NeoUtils;
 import org.eclipse.core.runtime.IAdaptable;
 import org.neo4j.graphdb.Direction;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Relationship;
 import org.neo4j.graphdb.ReturnableEvaluator;
 import org.neo4j.graphdb.StopEvaluator;
 import org.neo4j.graphdb.Transaction;
 import org.neo4j.graphdb.Traverser.Order;
 
 /**
  * Proxy class that provides access for Node, it's children and properties
  * 
  * @author Lagutko_N
  * @since 1.0.0
  */
 
 public class NeoNode {
     
     public static int MAX_CHILDREN_COUNT = 1000;
     
     /*
      * Constant for empty array of Nodes
      */
     protected static final NeoNode[] NO_NODES = new NeoNode[0];
     
     /*
      * Node
      */
     protected Node node;
     
     /*
      * Name of Node
      */
     protected String name;
     protected int number;
 
     private ArrayList<NeoNode> children;
     
     /**
      * Creates a proxy element for given Node
      * 
      * @param node node
      */
 
     public NeoNode(Node node,int number) {
         this.node = node;
         this.name = NeoUtils.getFormatedNodeName(node, "");
         this.number = number;
     }
     
     /**
      * Is this node has children
      *
      * @return is this node has children 
      */
     
     public boolean hasChildren() {
         return getChildren().length>0;
     }
     
     /**
      * Return String representation of given Node
      */
     
     public String toString() {
         return name;
     }
     
     /**
      * Returns children of this node
      *
      * @return
      */
     
     public NeoNode[] getChildren() {
         if(number>MAX_CHILDREN_COUNT){
             return NO_NODES;
         }
         if(children==null) {
             children = new ArrayList<NeoNode>();
             Iterator<Node> childrens = null;
             if (NeoUtils.isProbeCallsNode(node)) {
             	childrens = node.traverse(Order.BREADTH_FIRST, StopEvaluator.END_OF_GRAPH, ReturnableEvaluator.ALL_BUT_START_NODE,
						  				  ProbeCallRelationshipType.CALLEE, Direction.INCOMING,
 						  				  ProbeCallRelationshipType.CALLER, Direction.INCOMING).iterator();
             }
             else if (NeoUtils.getNodeType(node).equals(NodeTypes.PROBE.getId())) {
                 childrens = node.traverse(Order.BREADTH_FIRST, StopEvaluator.DEPTH_ONE, ReturnableEvaluator.ALL_BUT_START_NODE, 
                                           ProbeCallRelationshipType.CALLS, Direction.OUTGOING).iterator();
            }
            else {
             	childrens = node.traverse(Order.BREADTH_FIRST, StopEvaluator.DEPTH_ONE, ReturnableEvaluator.ALL_BUT_START_NODE,
             							  NetworkRelationshipTypes.CHILD,Direction.OUTGOING).iterator();
             }
             int nextNum = number+1;
             while (childrens.hasNext()) {
                 children.add(new NeoNode(childrens.next(), nextNum++));
             }
             if(children.size()==0) {
                 for(Relationship relationship:node.getRelationships(NetworkRelationshipTypes.MISSING,Direction.OUTGOING)){
                     children.add(new NeoNode(relationship.getEndNode(),nextNum++));
                 }
                 for(Relationship relationship:node.getRelationships(NetworkRelationshipTypes.DIFFERENT,Direction.OUTGOING)){
                     children.add(new NeoNode(relationship.getEndNode(),nextNum++));
                 }
             }
             Collections.sort(children, new NeoNodeComparator());
         }
         return children.toArray(NO_NODES);
     }
     
     /**
      * Returns Type of node
      *
      * @return type of Node
      */
     
     public String getType() {
         return (String)node.getProperty(INeoConstants.PROPERTY_TYPE_NAME, null);
     }
     
     /**
      * Returns the Node
      *
      * @return node
      */
     
     public Node getNode() {
         return node;
     }
     
     /**
      * @return Returns the number.
      */
     public int getNumber() {
         return number;
     }
 
     /**
      * <p>
      * Comparator of NeoNode
      * </p>
      * 
      * @author Cinkel_A
      * @since 1.0.0
      */
     public static class NeoNodeComparator implements Comparator<NeoNode> {
 
         @Override
         public int compare(NeoNode o1, NeoNode o2) {
             return o1 == null ? -1 : o2 == null ? 1 : o1.name.compareTo(o2.name);
         }
 
     }
 
     @Override
     public int hashCode() {
         final int prime = 31;
         int result = 1;
         result = prime * result + ((node == null) ? 0 : node.hashCode());
         return result;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (this == obj)
             return true;
         if (obj == null)
             return false;
         if (obj instanceof IAdaptable) {
             Node node2 = (Node)((IAdaptable)obj).getAdapter(Node.class);
             if (node2 != null) {
                 return node2.equals(node);
             }
         }
         if (obj instanceof Node) {
             if (node == null) {
                 return false;
             }
             return node.equals((Node)obj);
         }
         if (!(obj instanceof NeoNode))
             return false;
         NeoNode other = (NeoNode)obj;
         if (node == null) {
             if (other.node != null)
                 return false;
         } else if (!node.equals(other.node))
             return false;
         return true;
     }
 
     /**
      * Sets node name
      * 
      * @param value new name
      */
     public void setName(String value) {
         Transaction tx = NeoServiceProvider.getProvider().getService().beginTx();
         try {
             value = value == null ? "" : value.trim();
             if (node.hasProperty(INeoConstants.PROPERTY_NAME_NAME)) {
                 Object oldName = node.getProperty(INeoConstants.PROPERTY_NAME_NAME);
                 if (oldName.equals(value)) {
                     return;
                 }
                 node.setProperty(INeoConstants.PROPERTY_NAME_NAME, value);
                 name = value;
                 node.setProperty(INeoConstants.PROPERTY_OLD_NAME, oldName.toString());
                 tx.success();
                 NeoServiceProvider.getProvider().commit();
             }
         } finally {
             tx.finish();
         }
 
     }
 
 }
