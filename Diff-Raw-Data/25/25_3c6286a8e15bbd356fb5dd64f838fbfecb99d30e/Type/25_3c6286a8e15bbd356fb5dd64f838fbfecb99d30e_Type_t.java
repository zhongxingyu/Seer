 package symbolTable.types;
 
 /**
 * Abstract class representing a Type in C+- language.
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
    /**
     * Returns a string of the type similar to g++ representation of types (mostly from the error messages).
     * @return A string that represents the type.
     */
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

    /**
     * Checks if both types have the same const and volatile quantifiers.
     * Because this class is abstract any subclass who needs to take these two fields
     * into consideration in their equals method this one must me called explicitly.
     * 
     * @param t The object of the other Type class.
     * @return True if object have the same quantifiers and false otherwise.
     */
     @Override
     public boolean equals(Object t){
        if(t == null) return false;
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
