 package de.tuberlin.dima.presslufthammer.data;
 
 import java.util.Map;
 
 import com.google.common.collect.Maps;
 
 import de.tuberlin.dima.presslufthammer.data.columnar.ColumnReader;
 
 public class AssemblyFSMNode {
     private SchemaNode schema;
     private ColumnReader reader;
     private Map<Integer, AssemblyFSMNode> transitions;
 
     public AssemblyFSMNode(SchemaNode schema) {
         this.schema = schema;
         transitions = Maps.newHashMap();
     }
 
     private AssemblyFSMNode() {
         this(null);
     }
     
     public static AssemblyFSMNode getFinalState() {
         return new AssemblyFSMNode();
     }
 
     public boolean isFinal() {
        return schema == null;
     }
 
     public boolean hasTransition(int level) {
         return transitions.containsKey(level);
     }
 
     public void setTransition(int level, AssemblyFSMNode node) {
         transitions.put(level, node);
     }
 
     public AssemblyFSMNode getTransition(int level) {
         if (!transitions.containsKey(level)) {
             throw new RuntimeException("This should not happen, bug in code.");
         }
         return transitions.get(level);
     }
 
     public void setReader(ColumnReader reader) {
         this.reader = reader;
     }
 
     public ColumnReader getReader() {
         return reader;
     }
 
     public SchemaNode getSchema() {
         return schema;
     }
 
     @Override
     public String toString() {
         if (isFinal()) {
             return "FINAL FSM NODE\n";
         }
 
         StringBuilder result = new StringBuilder();
         result.append("FSM Node: " + schema.getQualifiedName());
         result.append("\n");
         for (Integer level : transitions.keySet()) {
             if (transitions.get(level).isFinal()) {
 
                 result.append(level + " -> FINAL\n");
             } else {
                 result.append(level + " -> "
                         + transitions.get(level).getSchema().getQualifiedName()
                         + "\n");
             }
         }
         return result.toString();
     }
 }
