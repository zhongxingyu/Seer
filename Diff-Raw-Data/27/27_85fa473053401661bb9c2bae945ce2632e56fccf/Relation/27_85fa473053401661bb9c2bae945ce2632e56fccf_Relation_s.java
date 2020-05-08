 package relationenalgebra;
 
 import java.util.HashSet;
 import java.util.Set;
 
 public class Relation implements ITreeNode {
 
     private String name;
 
     private String alias;
 
     private String[] tuple;
 
     private String[] tuplenames;
 
     public Relation(String name) {
         this.name = name;
     }
 
     public Relation(String name, String alias) {
         this.name = name;
         this.alias = alias;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getAlias() {
         return alias;
     }
 
     public void setAlias(String alias) {
         this.alias = alias;
     }
 
     public String[] getTuple() {
         return tuple;
     }
 
     public void setTuple(String[] tuple) {
         this.tuple = tuple;
     }
 
     public String[] getTuplenames() {
         return tuplenames;
     }
 
     public void setTuplenames(String[] tuplenames) {
         this.tuplenames = tuplenames;
     }
 
     @Override
     public String toString() {
         if (alias == null)
             return name;
         return name + " AS " + alias;
     }
 
     @Override
     public Set<String> getAttributes() {
         Set<String> result = new HashSet<String>();
         for (String name : tuplenames) {
             result.add(name);
         }
         return result;
     }
 
 	@Override
 	public ITreeNode getChild() {
 		return null;
 	}
 
 	@Override
 	public ITreeNode getSecondChild() {
 		return null;
 	}
 
 }
