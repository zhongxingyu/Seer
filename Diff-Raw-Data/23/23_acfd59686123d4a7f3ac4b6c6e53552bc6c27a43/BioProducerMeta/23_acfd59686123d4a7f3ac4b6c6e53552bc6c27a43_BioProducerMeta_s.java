 package it.aip.models;
 
//@javax.annotation.Generated(value = { "slim3-gen", "@VERSION@" }, date = "2011-06-24 13:50:26")
 /** */
 public final class BioProducerMeta extends org.slim3.datastore.ModelMeta<it.aip.models.BioProducer> {
 
     /** */
     public final org.slim3.datastore.StringUnindexedAttributeMeta<it.aip.models.BioProducer> infoAzienda = new org.slim3.datastore.StringUnindexedAttributeMeta<it.aip.models.BioProducer>(this, "infoAzienda", "infoAzienda");
 
     /** */
     public final org.slim3.datastore.CoreAttributeMeta<it.aip.models.BioProducer, com.google.appengine.api.datastore.Key> key = new org.slim3.datastore.CoreAttributeMeta<it.aip.models.BioProducer, com.google.appengine.api.datastore.Key>(this, "__key__", "key", com.google.appengine.api.datastore.Key.class);
 
     /** */
     public final org.slim3.datastore.StringUnindexedAttributeMeta<it.aip.models.BioProducer> practicalInfo = new org.slim3.datastore.StringUnindexedAttributeMeta<it.aip.models.BioProducer>(this, "practicalInfo", "practicalInfo");
 
     /** */
     public final org.slim3.datastore.StringAttributeMeta<it.aip.models.BioProducer> producerName = new org.slim3.datastore.StringAttributeMeta<it.aip.models.BioProducer>(this, "producerName", "producerName");
 
     /** */
     public final org.slim3.datastore.StringUnindexedAttributeMeta<it.aip.models.BioProducer> productionMethod = new org.slim3.datastore.StringUnindexedAttributeMeta<it.aip.models.BioProducer>(this, "productionMethod", "productionMethod");
 
     private static final BioProducerMeta slim3_singleton = new BioProducerMeta();
 
     /**
      * @return the singleton
      */
     public static BioProducerMeta get() {
        return slim3_singleton;
     }
 
     /** */
     public BioProducerMeta() {
         super("BioProducer", it.aip.models.BioProducer.class);
     }
 
     @Override
     public it.aip.models.BioProducer entityToModel(com.google.appengine.api.datastore.Entity entity) {
         it.aip.models.BioProducer model = new it.aip.models.BioProducer();
         java.util.List<it.aip.models.ImageFile> _images = blobToSerializable((com.google.appengine.api.datastore.Blob) entity.getProperty("images"));
         model.setImages(_images);
         model.setInfoAzienda(textToString((com.google.appengine.api.datastore.Text) entity.getProperty("infoAzienda")));
         model.setKey(entity.getKey());
         model.setPracticalInfo(textToString((com.google.appengine.api.datastore.Text) entity.getProperty("practicalInfo")));
         model.setProducerName((java.lang.String) entity.getProperty("producerName"));
         model.setProductionMethod(textToString((com.google.appengine.api.datastore.Text) entity.getProperty("productionMethod")));
         return model;
     }
 
     @Override
     public com.google.appengine.api.datastore.Entity modelToEntity(java.lang.Object model) {
         it.aip.models.BioProducer m = (it.aip.models.BioProducer) model;
         com.google.appengine.api.datastore.Entity entity = null;
         if (m.getKey() != null) {
             entity = new com.google.appengine.api.datastore.Entity(m.getKey());
         } else {
             entity = new com.google.appengine.api.datastore.Entity(kind);
         }
         entity.setUnindexedProperty("images", serializableToBlob(m.getImages()));
         entity.setUnindexedProperty("infoAzienda", stringToText(m.getInfoAzienda()));
         entity.setUnindexedProperty("practicalInfo", stringToText(m.getPracticalInfo()));
         entity.setProperty("producerName", m.getProducerName());
         entity.setUnindexedProperty("productionMethod", stringToText(m.getProductionMethod()));
         return entity;
     }
 
     @Override
     protected com.google.appengine.api.datastore.Key getKey(Object model) {
         it.aip.models.BioProducer m = (it.aip.models.BioProducer) model;
         return m.getKey();
     }
 
     @Override
     protected void setKey(Object model, com.google.appengine.api.datastore.Key key) {
         validateKey(key);
         it.aip.models.BioProducer m = (it.aip.models.BioProducer) model;
         m.setKey(key);
     }
 
     @Override
     protected long getVersion(Object model) {
         throw new IllegalStateException("The version property of the model(it.aip.models.BioProducer) is not defined.");
     }
 
     @Override
     protected void assignKeyToModelRefIfNecessary(com.google.appengine.api.datastore.AsyncDatastoreService ds, java.lang.Object model) {
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
         it.aip.models.BioProducer m = (it.aip.models.BioProducer) model;
         writer.beginObject();
         org.slim3.datastore.json.Default encoder0 = new org.slim3.datastore.json.Default();
         if(m.getImages() != null){
             writer.setNextPropertyName("images");
             // it.aip.models.ImageFile is not supported.
         }
         if(m.getInfoAzienda() != null){
             writer.setNextPropertyName("infoAzienda");
             encoder0.encode(writer, m.getInfoAzienda());
         }
         if(m.getKey() != null){
             writer.setNextPropertyName("key");
             encoder0.encode(writer, m.getKey());
         }
         if(m.getPracticalInfo() != null){
             writer.setNextPropertyName("practicalInfo");
             encoder0.encode(writer, m.getPracticalInfo());
         }
         if(m.getProducerName() != null){
             writer.setNextPropertyName("producerName");
             encoder0.encode(writer, m.getProducerName());
         }
         if(m.getProductionMethod() != null){
             writer.setNextPropertyName("productionMethod");
             encoder0.encode(writer, m.getProductionMethod());
         }
         if(m.getProductsListRef() != null){
             writer.setNextPropertyName("productsListRef");
             encoder0.encode(writer, m.getProductsListRef());
         }
         writer.endObject();
     }
 
     @Override
     protected it.aip.models.BioProducer jsonToModel(org.slim3.datastore.json.JsonRootReader rootReader, int maxDepth, int currentDepth) {
         it.aip.models.BioProducer m = new it.aip.models.BioProducer();
         org.slim3.datastore.json.JsonReader reader = null;
         org.slim3.datastore.json.Default decoder0 = new org.slim3.datastore.json.Default();
         reader = rootReader.newObjectReader("images");
         reader = rootReader.newObjectReader("infoAzienda");
         m.setInfoAzienda(decoder0.decode(reader, m.getInfoAzienda()));
         reader = rootReader.newObjectReader("key");
         m.setKey(decoder0.decode(reader, m.getKey()));
         reader = rootReader.newObjectReader("practicalInfo");
         m.setPracticalInfo(decoder0.decode(reader, m.getPracticalInfo()));
         reader = rootReader.newObjectReader("producerName");
         m.setProducerName(decoder0.decode(reader, m.getProducerName()));
         reader = rootReader.newObjectReader("productionMethod");
         m.setProductionMethod(decoder0.decode(reader, m.getProductionMethod()));
         reader = rootReader.newObjectReader("productsListRef");
         return m;
     }
 }
