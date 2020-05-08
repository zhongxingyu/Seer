 package bacn;
 
 public class Value<T extends Bcon> {
   
   private Value(String typeName) {
     mTypeName = typeName;
   }
   
   public static <T extends Bcon> Value<T> create(Class<T> clazz) {
     String typeName = clazz.getName();
     if(typeName != null) {
       return new Value<T>(typeName);
     }
     
     return null;
   }
   
   public T get() { return mValue; }
   public Value<T> setTo(T newValue) { mValue = newValue; return this; }
   
   public boolean isNull() {
     return mValue == null;
   }
   
   public boolean notNull() {
     return mValue != null;
   }
   
   @Override
   public boolean equals(Object rhsObject) {
     if(this == rhsObject) {
       return true;
     }
     
     if(rhsObject == null || getClass() != rhsObject.getClass()) {
       return false;
     }
     
     Value<?> rhs = (Value<?>) rhsObject;
     boolean weHoldDifferentNullValues = isNull() != rhs.isNull();
     if(weHoldDifferentNullValues) {
       return false;
     }
     
     boolean weBothHoldNullValues = isNull() && rhs.isNull();
     if(weBothHoldNullValues) {
       return true;
     }
     
     assert(notNull());
     assert(rhs.notNull());
     
     return get().equals(rhs.get());
   }
   
   /**
    * Returns the string representation of the type.
    * @return
    */
   public String getTypeName() { return mTypeName; }
   
   private T mValue;
   private String mTypeName;
   
   @Override
   public String toString() {
     
     String valueString;
     if(notNull()) {
       valueString = mValue.toString();
     } else {
       valueString = "*null*";
     }
     
     return String.format("{ %s: %s}", mTypeName, valueString);
     
   }
 
 }
