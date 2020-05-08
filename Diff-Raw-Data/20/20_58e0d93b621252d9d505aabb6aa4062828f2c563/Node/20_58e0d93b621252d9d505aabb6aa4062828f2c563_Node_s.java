 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package picoblazerules;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  *
  * @author dc386
  */
 public class Node {
 
     private String name;
     private Map<String, Node> next;
     private Node suffix;
     private Boolean inDictionary;
     private Integer id;
     private Integer startAdress;
     private Integer endAddress;
 
     public Node(String name) {
         this.name = name;
        this.next = new HashMap();
         this.suffix = null;
         this.inDictionary = false;
         this.id = 0;
         this.startAdress = 0;
         this.endAddress = 0;
     }
 
     public Integer getEndAddress() {
         return endAddress;
     }
 
     public void setEndAddress(Integer endAddress) {
         this.endAddress = endAddress;
     }
 
     public Integer getStartAdress() {
         return startAdress;
     }
 
     public void setStartAdress(Integer startAdress) {
         this.startAdress = startAdress;
     }
 
     public void addNextNode(Node _node)
     {
         this.next.put(_node.getName(), _node);
     }
 
     public Node getNextNode(String _nextNodeName)
     {
         return (this.next.get(_nextNodeName));
     }
     
     public Integer getId() {
         return id;
     }
 
     public void setId(Integer id) {
         this.id = id;
     }
 
     public Boolean getInDictionary() {
         return inDictionary;
     }
 
     public void setInDictionary(Boolean inDictionary) {
         this.inDictionary = inDictionary;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public Map<String, Node> getNext() {
         return next;
     }
 
     public void setNext(Map<String, Node> next) {
         this.next = next;
     }
 
     public Node getSuffix() {
         return suffix;
     }
 
     public void setSuffix(Node suffix) {
         this.suffix = suffix;
     }
 }
