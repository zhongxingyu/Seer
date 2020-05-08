 package it.aip.models;
 
//@javax.annotation.Generated(value = { "slim3-gen", "@VERSION@" }, date = "2011-06-25 14:36:40")
 /** */
 public final class RecipeProductMeta extends org.slim3.datastore.ModelMeta<it.aip.models.RecipeProduct> {
 
     /** */
     public final org.slim3.datastore.CoreAttributeMeta<it.aip.models.RecipeProduct, com.google.appengine.api.datastore.Key> key = new org.slim3.datastore.CoreAttributeMeta<it.aip.models.RecipeProduct, com.google.appengine.api.datastore.Key>(this, "__key__", "key", com.google.appengine.api.datastore.Key.class);
 
     /** */
     public final org.slim3.datastore.ModelRefAttributeMeta<it.aip.models.RecipeProduct, org.slim3.datastore.ModelRef<it.aip.models.OrganicProduct>, it.aip.models.OrganicProduct> productRef = new org.slim3.datastore.ModelRefAttributeMeta<it.aip.models.RecipeProduct, org.slim3.datastore.ModelRef<it.aip.models.OrganicProduct>, it.aip.models.OrganicProduct>(this, "productRef", "productRef", org.slim3.datastore.ModelRef.class, it.aip.models.OrganicProduct.class);
 
     /** */
     public final org.slim3.datastore.ModelRefAttributeMeta<it.aip.models.RecipeProduct, org.slim3.datastore.ModelRef<it.aip.models.Recipe>, it.aip.models.Recipe> recipeRef = new org.slim3.datastore.ModelRefAttributeMeta<it.aip.models.RecipeProduct, org.slim3.datastore.ModelRef<it.aip.models.Recipe>, it.aip.models.Recipe>(this, "recipeRef", "recipeRef", org.slim3.datastore.ModelRef.class, it.aip.models.Recipe.class);
 
     private static final RecipeProductMeta slim3_singleton = new RecipeProductMeta();
 
     /**
      * @return the singleton
      */
     public static RecipeProductMeta get() {
        return slim3_singleton;
     }
 
     /** */
     public RecipeProductMeta() {
         super("RecipeProduct", it.aip.models.RecipeProduct.class);
     }
 
     @Override
     public it.aip.models.RecipeProduct entityToModel(com.google.appengine.api.datastore.Entity entity) {
         it.aip.models.RecipeProduct model = new it.aip.models.RecipeProduct();
         model.setKey(entity.getKey());
         if (model.getProductRef() == null) {
             throw new NullPointerException("The property(productRef) is null.");
         }
         model.getProductRef().setKey((com.google.appengine.api.datastore.Key) entity.getProperty("productRef"));
         if (model.getRecipeRef() == null) {
             throw new NullPointerException("The property(recipeRef) is null.");
         }
         model.getRecipeRef().setKey((com.google.appengine.api.datastore.Key) entity.getProperty("recipeRef"));
         return model;
     }
 
     @Override
     public com.google.appengine.api.datastore.Entity modelToEntity(java.lang.Object model) {
         it.aip.models.RecipeProduct m = (it.aip.models.RecipeProduct) model;
         com.google.appengine.api.datastore.Entity entity = null;
         if (m.getKey() != null) {
             entity = new com.google.appengine.api.datastore.Entity(m.getKey());
         } else {
             entity = new com.google.appengine.api.datastore.Entity(kind);
         }
         if (m.getProductRef() == null) {
             throw new NullPointerException("The property(productRef) must not be null.");
         }
         entity.setProperty("productRef", m.getProductRef().getKey());
         if (m.getRecipeRef() == null) {
             throw new NullPointerException("The property(recipeRef) must not be null.");
         }
         entity.setProperty("recipeRef", m.getRecipeRef().getKey());
         return entity;
     }
 
     @Override
     protected com.google.appengine.api.datastore.Key getKey(Object model) {
         it.aip.models.RecipeProduct m = (it.aip.models.RecipeProduct) model;
         return m.getKey();
     }
 
     @Override
     protected void setKey(Object model, com.google.appengine.api.datastore.Key key) {
         validateKey(key);
         it.aip.models.RecipeProduct m = (it.aip.models.RecipeProduct) model;
         m.setKey(key);
     }
 
     @Override
     protected long getVersion(Object model) {
         throw new IllegalStateException("The version property of the model(it.aip.models.RecipeProduct) is not defined.");
     }
 
     @Override
     protected void assignKeyToModelRefIfNecessary(com.google.appengine.api.datastore.AsyncDatastoreService ds, java.lang.Object model) {
         it.aip.models.RecipeProduct m = (it.aip.models.RecipeProduct) model;
         if (m.getProductRef() == null) {
             throw new NullPointerException("The property(productRef) must not be null.");
         }
         m.getProductRef().assignKeyIfNecessary(ds);
         if (m.getRecipeRef() == null) {
             throw new NullPointerException("The property(recipeRef) must not be null.");
         }
         m.getRecipeRef().assignKeyIfNecessary(ds);
     }
 
     @Override
     protected void incrementVersion(Object model) {
     }
 
     @Override
     protected void prePut(Object model) {
     }
 
     @Override
     public String getSchemaVersionName() {
         return "slim3.schemaVersion";
     }
 
     @Override
     public String getClassHierarchyListName() {
         return "slim3.classHierarchyList";
     }
 
     @Override
     protected boolean isCipherProperty(String propertyName) {
         return false;
     }
 
     @Override
     protected void modelToJson(org.slim3.datastore.json.JsonWriter writer, java.lang.Object model, int maxDepth, int currentDepth) {
         it.aip.models.RecipeProduct m = (it.aip.models.RecipeProduct) model;
         writer.beginObject();
         org.slim3.datastore.json.Default encoder0 = new org.slim3.datastore.json.Default();
         if(m.getKey() != null){
             writer.setNextPropertyName("key");
             encoder0.encode(writer, m.getKey());
         }
         if(m.getProductRef() != null && m.getProductRef().getKey() != null){
             writer.setNextPropertyName("productRef");
             encoder0.encode(writer, m.getProductRef(), maxDepth, currentDepth);
         }
         if(m.getRecipeRef() != null && m.getRecipeRef().getKey() != null){
             writer.setNextPropertyName("recipeRef");
             encoder0.encode(writer, m.getRecipeRef(), maxDepth, currentDepth);
         }
         writer.endObject();
     }
 
     @Override
     protected it.aip.models.RecipeProduct jsonToModel(org.slim3.datastore.json.JsonRootReader rootReader, int maxDepth, int currentDepth) {
         it.aip.models.RecipeProduct m = new it.aip.models.RecipeProduct();
         org.slim3.datastore.json.JsonReader reader = null;
         org.slim3.datastore.json.Default decoder0 = new org.slim3.datastore.json.Default();
         reader = rootReader.newObjectReader("key");
         m.setKey(decoder0.decode(reader, m.getKey()));
         reader = rootReader.newObjectReader("productRef");
         decoder0.decode(reader, m.getProductRef(), maxDepth, currentDepth);
         reader = rootReader.newObjectReader("recipeRef");
         decoder0.decode(reader, m.getRecipeRef(), maxDepth, currentDepth);
         return m;
     }
 }
