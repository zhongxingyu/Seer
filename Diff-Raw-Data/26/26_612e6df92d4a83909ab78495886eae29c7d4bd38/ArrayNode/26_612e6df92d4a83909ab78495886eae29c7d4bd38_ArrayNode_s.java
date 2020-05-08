 package com.vonhof.babelshark.node;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  *
  * @author Henrik Hofmeister <@vonhofdk>
  */
 public final class ArrayNode extends SharkNode implements Iterable<SharkNode> {
     private final List<SharkNode> children = new ArrayList<SharkNode>();
 
     public ArrayNode() {
         super(NodeType.LIST);
     }
     
     public <T extends SharkNode> T add(T node) {
         children.add(node);
         return node;
     }
    
     public SharkNode get(int i) {
         if (children.size() <= i)
             return null;
         return children.get(i);
     }
 
     public Iterator<SharkNode> iterator() {
         return children.iterator();
     }
 
     public ObjectNode addObject() {
         return add(new ObjectNode());
     }
     
     public ArrayNode addArray() {
         return add(new ArrayNode());
     }
     
     public void add(String ... values) {
         for(String value:values)
             add(new ValueNode(value));
     }
     
     public void add(int ... values) {
         for(int value:values)
             add(new ValueNode(value));
     }
     
     public void add(float ... values) {
         for(float value:values)
             add(new ValueNode(value));
     }
     
     public void add(double ... values) {
         for(double value:values)
             add(new ValueNode(value));
     }
     
     public void add(long... values) {
         for(long value:values)
             add(new ValueNode(value));
     }
     
     public void add(Date ... values) {
         for(Date value:values)
             add(new ValueNode(value));
     }
     
     public void add(boolean ... values) {
         for(boolean value:values)
             add(new ValueNode(value));
     }
     
     public void add(Enum ... values) {
         for(Enum value:values)
             add(new ValueNode(value));
     }
     
     @Override
     public String toString() {
         StringBuilder sb = new StringBuilder();
         sb.append("[");
         boolean first = true;
         for(SharkNode child:children) {
             if(first)
                 first = false;
             else
                 sb.append(",");
             sb.append(child);
             
         }
         sb.append("]");
         return sb.toString();
     }
 
     @Override
     public boolean equals(Object obj) {
         if (!super.equals(obj)) {
             return false;
         }
         final ArrayNode other = (ArrayNode) obj;
         if (this.children != other.children && (this.children == null || !this.children.equals(other.children))) {
             return false;
         }
         return true;
     }
 
     @Override
     public int hashCode() {
         int hash = super.hashCode();
         hash = 79 * hash + (this.children != null ? this.children.hashCode() : 0);
         return hash;
     }
     
     
 }
