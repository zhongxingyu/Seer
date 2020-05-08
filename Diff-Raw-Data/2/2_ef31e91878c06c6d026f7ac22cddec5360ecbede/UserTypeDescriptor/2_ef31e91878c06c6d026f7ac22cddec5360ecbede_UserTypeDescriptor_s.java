 package com.twolattes.json;
 
 import static com.twolattes.json.Json.NULL;
 import static com.twolattes.json.Unification.extractRawType;
 import static com.twolattes.json.Unification.getActualTypeArgument;
 
 import com.twolattes.json.types.JsonType;
 
 /**
 * Descriptor wrapping used defined {@link JsonType}s.
  */
 class UserTypeDescriptor<E, J extends Json.Value> extends AbstractDescriptor<E, J> {
 
   private final JsonType<E, J> type;
 
   public UserTypeDescriptor(JsonType<E, J> type) {
     super(extractReturnType(type));
     this.type = type;
   }
 
   @SuppressWarnings("unchecked")
   private static <E> Class<E> extractReturnType(JsonType<E, ?> jsonType) {
     return (Class<E>) extractRawType(
         getActualTypeArgument(jsonType.getClass(), JsonType.class, 0));
   }
 
   @Override
   public boolean isInlineable() {
     return false;
   }
 
   @SuppressWarnings("unchecked")
   public J marshall(E entity, String view) {
     if (entity == null) {
       // The Json.Value hierarchy is closed with Null as the bottom type.
       return (J) NULL;
     } else {
       return type.marshall(getReturnedClass().cast(entity));
     }
   }
 
   public E unmarshall(J marshalled, String view) {
     if (marshalled.equals(NULL)) {
       return null;
     } else {
       return type.unmarshall(marshalled);
     }
   }
 
   @Override
   @SuppressWarnings("unchecked")
   public Class<E> getReturnedClass() {
     return (Class<E>) super.getReturnedClass();
   }
 
   @Override
   public boolean shouldInline() {
     return false;
   }
 
 }
