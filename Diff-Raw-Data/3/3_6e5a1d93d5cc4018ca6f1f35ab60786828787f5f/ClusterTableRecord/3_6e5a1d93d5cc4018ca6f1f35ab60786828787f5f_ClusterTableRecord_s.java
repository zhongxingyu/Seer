 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package aether.cluster;
 
 import java.net.InetAddress;
 
 /**
  *
  * @author aniket
  */
 public class ClusterTableRecord {
     
     
     private int nodeIdentifier;
     private InetAddress nodeIp;
     
     /**
      * ClusterTableRecord is an entry in the cluster table storing information
      * about a node in the cluster.
      * 
      * @param id    node identifier integer.
      * @param ip    IP address of the node
      */
     public ClusterTableRecord (int id, InetAddress ip) {
         
         nodeIdentifier = id;
         nodeIp = ip;
     }
     
     
     /**
      * Get the identifier for the node in this record.
      * @return Node identifier (int)
      */
     public int getNodeId () {
         
         return nodeIdentifier;
     }
     
     /**
      * Get the IP address of the node in this record
      * @return Node IP address
      */
     public InetAddress getNodeIp () {
         
         return nodeIp;
     }
     
     
     /**
      * String representation of the record in form:
      * Node Id = <Identifier>, Node IP = <Node IP address>.
      * @return  String representation of the table record
      */
     @Override
     public String toString () {
         String recordString = "Node Id = " + nodeIdentifier + ", Node IP = " 
                 + nodeIp.toString().replaceFirst(".*/", "");
         return recordString;
     }
     
     
     
     /**
      * String representation with id and ip separated by delimiter '%'
      * @return  Delimited string representation of the table record
      */
     public String toDelimitedString () {
         
         Integer id = nodeIdentifier;
        String deRec = id.toString() + "%" + nodeIp.toString();
         return deRec;
     }
 }
