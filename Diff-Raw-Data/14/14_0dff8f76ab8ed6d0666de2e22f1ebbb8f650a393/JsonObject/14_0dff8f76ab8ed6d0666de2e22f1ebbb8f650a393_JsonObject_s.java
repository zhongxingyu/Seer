 package io.jacinto.core.impl;
 
 /**
  * <p>By design, we decided to use a decorator of the
  * {@link com.google.gson.JsonObject} class  so if we ever decide to use
 * another JSON library or simply JSON simple changes its API we can easily
  * modify the internals of this library.</p>
  *
  * @author Fernando Hernandez
  * @since   0.0.1
  * @version 0.0.1
  */
 final class JsonObject implements JsonElement<com.google.gson.JsonObject> {
     final private com.google.gson.JsonObject _gsonObject;
 
     private JsonObject(final com.google.gson.JsonObject gsonObject) {
         _gsonObject = gsonObject;
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public com.google.gson.JsonObject asGsonElement() {
         return _gsonObject;
     }
 
     /**
      * Adds a member, which is a name-value pair, to self. The name must be a String, but the value
      * can be an arbitrary JsonElement, thereby allowing you to build a full tree of JsonElements
      * rooted at this node.
      *
      * @param property name of the member.
      * @param value the member object.
      */
     public void add(final String property, final JsonElement value) {
         _gsonObject.add(property, value.asGsonElement());
     }
 
     /**
      * Removes the {@code property} from this {@link JsonObject}.
      *
      * @param property name of the member that should be removed.
      * @return the {@link JsonElement} object that is being removed.
      */
    public JsonObject remove(final String property) {
        return new JsonObject((com.google.gson.JsonObject) _gsonObject.remove(property));
     }
 
     /**
      * Creates a new instance of {@link JsonObject}
      *
      * @return a new {@link JsonObject}
      */
     public static JsonObject newInstance() {
         return new JsonObject(new com.google.gson.JsonObject());
     }
 
     @Override
     public boolean equals(Object o) {
         return _gsonObject.equals(o);
     }
 
     @Override
     public int hashCode() {
         return _gsonObject.hashCode();
     }
 }
