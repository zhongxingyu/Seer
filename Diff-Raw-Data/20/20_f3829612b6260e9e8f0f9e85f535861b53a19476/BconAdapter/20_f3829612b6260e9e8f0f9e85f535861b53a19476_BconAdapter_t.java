 package bacn;
 
 import java.util.Collection;
 import java.util.Map;
 import java.util.TreeMap;
 
 public abstract class BconAdapter implements Bcon {
   /**
    * Returns a value that is registered as a member of `this` instance.
    * @param clazz
    * @param key
    * @return
    */
   final protected <T extends Bcon> Value<T> member (Class<T> clazz, String key) {
     Value<T> value = Value.create(clazz);
     mKeysToMembers.put(key, value);
     return value;
   }
   
   /**
    * Returns a primitive value that is registered as a member of `this` instance.
    * @param key
    * @return
    */
   protected final Prim member (String key) {
     Prim primitive = new Prim();
     mPrimitives.put(key, primitive);
     return primitive;
   }
   
   /**
    * Extends `this` object with the type of T.
    * @param clazz
    * @param key
    * @return
    */
   protected final <T extends Bcon> Value<T> ancestor (Class<T> clazz, T t) {
     Value<T> value = Value.create(clazz).setTo(t);
     mKeysToAncestors.put(clazz.getName(), value);
     return value;
   }
   
   /* (non-Javadoc)
    * @see bacn.Bcon#getAncestor(java.lang.String)
    */
   @Override
   final public Value<?> getAncestor(String key) {
     assert(mKeysToAncestors.containsKey(key));
     
     return mKeysToAncestors.get(key);
   }
   
   /* (non-Javadoc)
    * @see bacn.Bcon#getAncestorKeys()
    */
   @Override
   final public Collection<String> getAncestorKeys() {
     return mKeysToAncestors.keySet();
   }
   
   /* (non-Javadoc)
    * @see bacn.Bcon#getMemberKeys()
    */
   @Override
   final public Collection<String> getMemberKeys() {
     return mKeysToMembers.keySet();
   }
   
   /* (non-Javadoc)
    * @see bacn.Bcon#getPrimitiveKeys()
    */
   @Override
   final public Collection<String> getPrimitiveKeys() {
     return mPrimitives.keySet();
   }
   
   @Override
   public boolean hasPrimitives() {
     return !mPrimitives.isEmpty();
   }
   
   @Override
   public boolean hasAncestors() {
     return !mKeysToAncestors.isEmpty();
   }
   
   @Override
   public boolean hasMembers() {
     return !mKeysToMembers.isEmpty();
   }
   
   @Override
   public boolean equals(Object o) {
     if(o == null) {
       return false;
     }
     
     if(o == this) {
      return true;
     }
     
     try {
       Bcon rhs = (Bcon) o;
       return primitivesAreEqualWith(rhs) && membersAreEqualWith(rhs) && ancestorsAreEqualWith(rhs);
     } catch (ClassCastException e) {
       return false;
     }
   }
   
   private boolean ancestorsAreEqualWith(Bcon rhs) {
     assert(rhs != null);
     
     boolean hasSameAncestorSchema = getAncestorKeys().equals(rhs.getAncestorKeys());
     if(!hasSameAncestorSchema) {
       return false;
     }
     
     for(String ancestorKey: getAncestorKeys()) {
       Value<? extends Bcon> myAncestor = getAncestor(ancestorKey);
       Value<? extends Bcon> rhsAncestor = rhs.getAncestor(ancestorKey);
       
       assert(myAncestor != null);
       assert(rhsAncestor != null);
       
       boolean ancestorsHaveDifferentValues = !myAncestor.equals(rhsAncestor);
       if(ancestorsHaveDifferentValues) {
         return false;
       }
     }
     
     return true;
   }
 
   private boolean membersAreEqualWith(Bcon rhs) {
     assert(rhs != null);
     
     boolean hasSameMemberSchema = getMemberKeys().equals(rhs.getMemberKeys());
     if(!hasSameMemberSchema) {
       return false;
     }
     
     for(String memberValueKey: getMemberKeys()) {
       Value<?> myValue = getMemberValue(memberValueKey);
       Value<?> rhsValue = rhs.getMemberValue(memberValueKey);
       boolean memberValuesHaveDifferentValues = !myValue.equals(rhsValue);
       if(memberValuesHaveDifferentValues) {
         return false;
       }
     }
     
     return true;
   }
   
   @Override
   public Prim getPrimitive(String primitiveKey) {
     assert(mPrimitives.containsKey(primitiveKey));
     
     return mPrimitives.get(primitiveKey);
   }
 
   @Override
   public Value<?> getMemberValue(String valueKey) {
     assert(mKeysToAncestors.containsKey(valueKey));
     
     return mKeysToAncestors.get(valueKey);
   }
 
   private boolean primitivesAreEqualWith(Bcon rhs) {
     assert(rhs != null);
     
     boolean hasDifferentPrimitivesSchema = !getPrimitiveKeys().equals(rhs.getPrimitiveKeys());
     if(hasDifferentPrimitivesSchema) {
       return false;
     }
     
     for(String primitiveKey: getPrimitiveKeys()) {
       Prim myPrim = getPrimitive(primitiveKey);
       Prim rhsPrim = rhs.getPrimitive(primitiveKey);
      
       boolean primitivesAreDifferent = !myPrim.equals(rhsPrim);
       if(primitivesAreDifferent) {
         return false;
       }
     }
     
     return true;
   }
 
   /**
    * <p>
    * Maps ancestor implementation (by ancestor canonical class name) to ancestor
    * {@code Value<ancestorType>} instance.
    * </p>
    * 
    * <p>
    * An example of a key for class {@code com.mydomain.mypackage.Type} is "com.mydomain.mypackage.Type".
    * </p>
    */
   private Map<String,Value<? extends Bcon>> mKeysToAncestors = new TreeMap<String,Value<? extends Bcon>>();
   
   /**
    * Maps member keys value to ancestor Value<memberType> instance.
    */
   private Map<String,Value<? extends Bcon>> mKeysToMembers = new TreeMap<String,Value<? extends Bcon>>();
   
   /**
    * Maps primitive value keys to their respective instances.
    */
   private Map<String,Prim> mPrimitives = new TreeMap<String, Prim>();
 }
