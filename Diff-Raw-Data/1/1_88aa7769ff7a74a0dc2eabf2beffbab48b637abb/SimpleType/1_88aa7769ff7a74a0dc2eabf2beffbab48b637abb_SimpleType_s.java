 package symbolTable.types;
 
 /**
  *
  * @author kostas
  */
 public abstract class SimpleType extends Type {
     
     String name;
     
     public SimpleType(String name){
         this.name = name;
     }
     
     @Override
     public boolean equals(Object o){
         if(o == null) return false;
         if(o instanceof SimpleType){
             SimpleType st = (SimpleType) o;
             return this.name.equals(st.name);
         }
         return false;
     }
     
     @Override
     protected StringBuilder getString(StringBuilder aggr){
         StringBuilder n = new StringBuilder(this.name);
         return n.append(aggr);
     }
 
     @Override
     public int hashCode() {
         int hash = 7;
         hash = 53 * hash + (this.name != null ? this.name.hashCode() : 0);
         return hash;
     }
 
 }
