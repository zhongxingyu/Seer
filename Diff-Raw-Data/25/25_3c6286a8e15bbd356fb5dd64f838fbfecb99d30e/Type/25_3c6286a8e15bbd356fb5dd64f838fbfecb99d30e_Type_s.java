 package symbolTable.types;
 
 /**
 *
  * @author kostas
  */
 public abstract class Type {
     
     /**
      * By default no type is neither const nor volatile.
      */
     protected boolean isConst = false,
                     isVolatile = false;
     
     protected abstract StringBuilder getString(StringBuilder aggr);
     
     /**
      * It checks either or not this is subtype of o, i.e this <: o.
      * @param o The other type.
      * @return True if this <: o and false otherwise.
      */
     public abstract boolean subType(Type o);
    
     @Override
     public String toString(){
         StringBuilder rv = new StringBuilder();
         return this.getString(rv).toString();
     }
     
     /**
      * Declares that the type is const.
      */
     public void setIsConst(){
         this.isConst = true;
     }
     
     /**
      * Declares that the type is volatile.
      */
     public void setIsVolatile(){
         this.isVolatile = true;
     }
//    public String getType(){
//        StringBuilder rv = new StringBuilder();
//        return this.findType(rv).toString();
//    }
    
     @Override
     public boolean equals(Object t){
         if(t instanceof Type){
             Type t1 = (Type)t;
             if(this.isConst == t1.isConst && this.isVolatile == t1.isVolatile) return true;
             return false;
         }
         else{
             return false;
         }
     }
 
     @Override
     public int hashCode() {
         int hash = 7;
         return hash;
     }
     
 }
