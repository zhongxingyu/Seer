 package it.aip.models;
 
//@javax.annotation.Generated(value = { "slim3-gen", "@VERSION@" }, date = "2011-06-25 14:36:40")
 /** */
 public final class OrganicProductMeta extends org.slim3.datastore.ModelMeta<it.aip.models.OrganicProduct> {
 
     /** */
     public final org.slim3.datastore.StringUnindexedAttributeMeta<it.aip.models.OrganicProduct> generalInfo = new org.slim3.datastore.StringUnindexedAttributeMeta<it.aip.models.OrganicProduct>(this, "generalInfo", "generalInfo");
 
     /** */
     public final org.slim3.datastore.StringUnindexedAttributeMeta<it.aip.models.OrganicProduct> healthBenefits = new org.slim3.datastore.StringUnindexedAttributeMeta<it.aip.models.OrganicProduct>(this, "healthBenefits", "healthBenefits");
 
     /** */
     public final org.slim3.datastore.CoreAttributeMeta<it.aip.models.OrganicProduct, com.google.appengine.api.datastore.Key> key = new org.slim3.datastore.CoreAttributeMeta<it.aip.models.OrganicProduct, com.google.appengine.api.datastore.Key>(this, "__key__", "key", com.google.appengine.api.datastore.Key.class);
 
     /** */
     public final org.slim3.datastore.ModelRefAttributeMeta<it.aip.models.OrganicProduct, org.slim3.datastore.ModelRef<it.aip.models.BioProducer>, it.aip.models.BioProducer> producerRef = new org.slim3.datastore.ModelRefAttributeMeta<it.aip.models.OrganicProduct, org.slim3.datastore.ModelRef<it.aip.models.BioProducer>, it.aip.models.BioProducer>(this, "producerRef", "producerRef", org.slim3.datastore.ModelRef.class, it.aip.models.BioProducer.class);
 
     /** */
     public final org.slim3.datastore.StringAttributeMeta<it.aip.models.OrganicProduct> productCategory = new org.slim3.datastore.StringAttributeMeta<it.aip.models.OrganicProduct>(this, "productCategory", "productCategory");
 
     /** */
     public final org.slim3.datastore.StringAttributeMeta<it.aip.models.OrganicProduct> productName = new org.slim3.datastore.StringAttributeMeta<it.aip.models.OrganicProduct>(this, "productName", "productName");
 
     private static final OrganicProductMeta slim3_singleton = new OrganicProductMeta();
 
     /**
      * @return the singleton
      */
     public static OrganicProductMeta get() {
        return slim3_singleton;
     }
 
     /** */
     public OrganicProductMeta() {
         super("OrganicProduct", it.aip.models.OrganicProduct.class);
     }
 
     @Override
     public it.aip.models.OrganicProduct entityToModel(com.google.appengine.api.datastore.Entity entity) {
         it.aip.models.OrganicProduct model = new it.aip.models.OrganicProduct();
         model.setGeneralInfo(textToString((com.google.appengine.api.datastore.Text) entity.getProperty("generalInfo")));
         model.setHealthBenefits(textToString((com.google.appengine.api.datastore.Text) entity.getProperty("healthBenefits")));
         java.util.List<it.aip.models.ImageFile> _images = blobToSerializable((com.google.appengine.api.datastore.Blob) entity.getProperty("images"));
         model.setImages(_images);
         model.setKey(entity.getKey());
         if (model.getProducerRef() == null) {
             throw new NullPointerException("The property(producerRef) is null.");
         }
         model.getProducerRef().setKey((com.google.appengine.api.datastore.Key) entity.getProperty("producerRef"));
         model.setProductCategory((java.lang.String) entity.getProperty("productCategory"));
         model.setProductName((java.lang.String) entity.getProperty("productName"));
         return model;
     }
 
     @Override
     public com.google.appengine.api.datastore.Entity modelToEntity(java.lang.Object model) {
         it.aip.models.OrganicProduct m = (it.aip.models.OrganicProduct) model;
         com.google.appengine.api.datastore.Entity entity = null;
         if (m.getKey() != null) {
             entity = new com.google.appengine.api.datastore.Entity(m.getKey());
         } else {
             entity = new com.google.appengine.api.datastore.Entity(kind);
         }
         entity.setUnindexedProperty("generalInfo", stringToText(m.getGeneralInfo()));
         entity.setUnindexedProperty("healthBenefits", stringToText(m.getHealthBenefits()));
         entity.setUnindexedProperty("images", serializableToBlob(m.getImages()));
         if (m.getProducerRef() == null) {
             throw new NullPointerException("The property(producerRef) must not be null.");
         }
         entity.setProperty("producerRef", m.getProducerRef().getKey());
         entity.setProperty("productCategory", m.getProductCategory());
         entity.setProperty("productName", m.getProductName());
         return entity;
     }
 
     @Override
     protected com.google.appengine.api.datastore.Key getKey(Object model) {
         it.aip.models.OrganicProduct m = (it.aip.models.OrganicProduct) model;
         return m.getKey();
     }
 
     @Override
     protected void setKey(Object model, com.google.appengine.api.datastore.Key key) {
         validateKey(key);
         it.aip.models.OrganicProduct m = (it.aip.models.OrganicProduct) model;
         m.setKey(key);
     }
 
     @Override
     protected long getVersion(Object model) {
         throw new IllegalStateException("The version property of the model(it.aip.models.OrganicProduct) is not defined.");
     }
 
     @Override
     protected void assignKeyToModelRefIfNecessary(com.google.appengine.api.datastore.AsyncDatastoreService ds, java.lang.Object model) {
         it.aip.models.OrganicProduct m = (it.aip.models.OrganicProduct) model;
         if (m.getProducerRef() == null) {
             throw new NullPointerException("The property(producerRef) must not be null.");
         }
         m.getProducerRef().assignKeyIfNecessary(ds);
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
         it.aip.models.OrganicProduct m = (it.aip.models.OrganicProduct) model;
         writer.beginObject();
         org.slim3.datastore.json.Default encoder0 = new org.slim3.datastore.json.Default();
         if(m.getGeneralInfo() != null){
             writer.setNextPropertyName("generalInfo");
             encoder0.encode(writer, m.getGeneralInfo());
         }
         if(m.getHealthBenefits() != null){
             writer.setNextPropertyName("healthBenefits");
             encoder0.encode(writer, m.getHealthBenefits());
         }
         if(m.getImages() != null){
             writer.setNextPropertyName("images");
             // it.aip.models.ImageFile is not supported.
         }
         if(m.getKey() != null){
             writer.setNextPropertyName("key");
             encoder0.encode(writer, m.getKey());
         }
         if(m.getProducerRef() != null && m.getProducerRef().getKey() != null){
             writer.setNextPropertyName("producerRef");
             encoder0.encode(writer, m.getProducerRef(), maxDepth, currentDepth);
         }
         if(m.getProductCategory() != null){
             writer.setNextPropertyName("productCategory");
             encoder0.encode(writer, m.getProductCategory());
         }
         if(m.getProductName() != null){
             writer.setNextPropertyName("productName");
             encoder0.encode(writer, m.getProductName());
         }
         if(m.getRecipeProductListRef() != null){
             writer.setNextPropertyName("recipeProductListRef");
             encoder0.encode(writer, m.getRecipeProductListRef());
         }
         writer.endObject();
     }
 
     @Override
     protected it.aip.models.OrganicProduct jsonToModel(org.slim3.datastore.json.JsonRootReader rootReader, int maxDepth, int currentDepth) {
         it.aip.models.OrganicProduct m = new it.aip.models.OrganicProduct();
         org.slim3.datastore.json.JsonReader reader = null;
         org.slim3.datastore.json.Default decoder0 = new org.slim3.datastore.json.Default();
         reader = rootReader.newObjectReader("generalInfo");
         m.setGeneralInfo(decoder0.decode(reader, m.getGeneralInfo()));
         reader = rootReader.newObjectReader("healthBenefits");
         m.setHealthBenefits(decoder0.decode(reader, m.getHealthBenefits()));
         reader = rootReader.newObjectReader("images");
         reader = rootReader.newObjectReader("key");
         m.setKey(decoder0.decode(reader, m.getKey()));
         reader = rootReader.newObjectReader("producerRef");
         decoder0.decode(reader, m.getProducerRef(), maxDepth, currentDepth);
         reader = rootReader.newObjectReader("productCategory");
         m.setProductCategory(decoder0.decode(reader, m.getProductCategory()));
         reader = rootReader.newObjectReader("productName");
         m.setProductName(decoder0.decode(reader, m.getProductName()));
         reader = rootReader.newObjectReader("recipeProductListRef");
         return m;
     }
 }
