 package jp.tnasu.f1tvnews.meta;
 
//@javax.annotation.Generated(value = { "slim3-gen", "@VERSION@" }, date = "2012-06-17 20:20:05")
 /** */
 public final class HtmlDocumentMeta extends org.slim3.datastore.ModelMeta<jp.tnasu.f1tvnews.model.HtmlDocument> {
 
     /** */
     public final org.slim3.datastore.StringAttributeMeta<jp.tnasu.f1tvnews.model.HtmlDocument> copyright = new org.slim3.datastore.StringAttributeMeta<jp.tnasu.f1tvnews.model.HtmlDocument>(this, "copyright", "copyright");
 
     /** */
     public final org.slim3.datastore.StringAttributeMeta<jp.tnasu.f1tvnews.model.HtmlDocument> description = new org.slim3.datastore.StringAttributeMeta<jp.tnasu.f1tvnews.model.HtmlDocument>(this, "description", "description");
 
     /** */
     public final org.slim3.datastore.CoreAttributeMeta<jp.tnasu.f1tvnews.model.HtmlDocument, com.google.appengine.api.datastore.Key> key = new org.slim3.datastore.CoreAttributeMeta<jp.tnasu.f1tvnews.model.HtmlDocument, com.google.appengine.api.datastore.Key>(this, "__key__", "key", com.google.appengine.api.datastore.Key.class);
 
     /** */
     public final org.slim3.datastore.StringAttributeMeta<jp.tnasu.f1tvnews.model.HtmlDocument> language = new org.slim3.datastore.StringAttributeMeta<jp.tnasu.f1tvnews.model.HtmlDocument>(this, "language", "language");
 
     /** */
     public final org.slim3.datastore.StringAttributeMeta<jp.tnasu.f1tvnews.model.HtmlDocument> link = new org.slim3.datastore.StringAttributeMeta<jp.tnasu.f1tvnews.model.HtmlDocument>(this, "link", "link");
 
     /** */
     public final org.slim3.datastore.CoreAttributeMeta<jp.tnasu.f1tvnews.model.HtmlDocument, java.util.Date> publishedDate = new org.slim3.datastore.CoreAttributeMeta<jp.tnasu.f1tvnews.model.HtmlDocument, java.util.Date>(this, "publishedDate", "publishedDate", java.util.Date.class);
 
     /** */
     public final org.slim3.datastore.StringAttributeMeta<jp.tnasu.f1tvnews.model.HtmlDocument> title = new org.slim3.datastore.StringAttributeMeta<jp.tnasu.f1tvnews.model.HtmlDocument>(this, "title", "title");
 
     /** */
     public final org.slim3.datastore.CoreAttributeMeta<jp.tnasu.f1tvnews.model.HtmlDocument, java.lang.Long> version = new org.slim3.datastore.CoreAttributeMeta<jp.tnasu.f1tvnews.model.HtmlDocument, java.lang.Long>(this, "version", "version", java.lang.Long.class);
 
     private static final HtmlDocumentMeta slim3_singleton = new HtmlDocumentMeta();
 
     /**
      * @return the singleton
      */
     public static HtmlDocumentMeta get() {
        return slim3_singleton;
     }
 
     /** */
     public HtmlDocumentMeta() {
         super("HtmlDocument", jp.tnasu.f1tvnews.model.HtmlDocument.class);
     }
 
     @Override
     public jp.tnasu.f1tvnews.model.HtmlDocument entityToModel(com.google.appengine.api.datastore.Entity entity) {
         jp.tnasu.f1tvnews.model.HtmlDocument model = new jp.tnasu.f1tvnews.model.HtmlDocument();
         model.setCopyright((java.lang.String) entity.getProperty("copyright"));
         model.setDescription((java.lang.String) entity.getProperty("description"));
         java.util.List<jp.tnasu.f1tvnews.model.HtmlContent> _htmlContentList = blobToSerializable((com.google.appengine.api.datastore.Blob) entity.getProperty("htmlContentList"));
         model.setHtmlContentList(_htmlContentList);
         model.setKey(entity.getKey());
         model.setLanguage((java.lang.String) entity.getProperty("language"));
         model.setLink((java.lang.String) entity.getProperty("link"));
         model.setPublishedDate((java.util.Date) entity.getProperty("publishedDate"));
         model.setTitle((java.lang.String) entity.getProperty("title"));
         model.setVersion((java.lang.Long) entity.getProperty("version"));
         return model;
     }
 
     @Override
     public com.google.appengine.api.datastore.Entity modelToEntity(java.lang.Object model) {
         jp.tnasu.f1tvnews.model.HtmlDocument m = (jp.tnasu.f1tvnews.model.HtmlDocument) model;
         com.google.appengine.api.datastore.Entity entity = null;
         if (m.getKey() != null) {
             entity = new com.google.appengine.api.datastore.Entity(m.getKey());
         } else {
             entity = new com.google.appengine.api.datastore.Entity(kind);
         }
         entity.setProperty("copyright", m.getCopyright());
         entity.setProperty("description", m.getDescription());
         entity.setUnindexedProperty("htmlContentList", serializableToBlob(m.getHtmlContentList()));
         entity.setProperty("language", m.getLanguage());
         entity.setProperty("link", m.getLink());
         entity.setProperty("publishedDate", m.getPublishedDate());
         entity.setProperty("title", m.getTitle());
         entity.setProperty("version", m.getVersion());
         entity.setProperty("slim3.schemaVersion", 2);
         return entity;
     }
 
     @Override
     protected com.google.appengine.api.datastore.Key getKey(Object model) {
         jp.tnasu.f1tvnews.model.HtmlDocument m = (jp.tnasu.f1tvnews.model.HtmlDocument) model;
         return m.getKey();
     }
 
     @Override
     protected void setKey(Object model, com.google.appengine.api.datastore.Key key) {
         validateKey(key);
         jp.tnasu.f1tvnews.model.HtmlDocument m = (jp.tnasu.f1tvnews.model.HtmlDocument) model;
         m.setKey(key);
     }
 
     @Override
     protected long getVersion(Object model) {
         jp.tnasu.f1tvnews.model.HtmlDocument m = (jp.tnasu.f1tvnews.model.HtmlDocument) model;
         return m.getVersion() != null ? m.getVersion().longValue() : 0L;
     }
 
     @Override
     protected void assignKeyToModelRefIfNecessary(com.google.appengine.api.datastore.AsyncDatastoreService ds, java.lang.Object model) {
     }
 
     @Override
     protected void incrementVersion(Object model) {
         jp.tnasu.f1tvnews.model.HtmlDocument m = (jp.tnasu.f1tvnews.model.HtmlDocument) model;
         long version = m.getVersion() != null ? m.getVersion().longValue() : 0L;
         m.setVersion(Long.valueOf(version + 1L));
     }
 
     @Override
     protected void prePut(Object model) {
     }
 
     @Override
     protected void postGet(Object model) {
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
         jp.tnasu.f1tvnews.model.HtmlDocument m = (jp.tnasu.f1tvnews.model.HtmlDocument) model;
         writer.beginObject();
         org.slim3.datastore.json.Default encoder0 = new org.slim3.datastore.json.Default();
         if(m.getCopyright() != null){
             writer.setNextPropertyName("copyright");
             encoder0.encode(writer, m.getCopyright());
         }
         if(m.getDescription() != null){
             writer.setNextPropertyName("description");
             encoder0.encode(writer, m.getDescription());
         }
         if(m.getHtmlContentList() != null){
             writer.setNextPropertyName("htmlContentList");
             // jp.tnasu.f1tvnews.model.HtmlContent is not supported.
         }
         if(m.getKey() != null){
             writer.setNextPropertyName("key");
             encoder0.encode(writer, m.getKey());
         }
         if(m.getLanguage() != null){
             writer.setNextPropertyName("language");
             encoder0.encode(writer, m.getLanguage());
         }
         if(m.getLink() != null){
             writer.setNextPropertyName("link");
             encoder0.encode(writer, m.getLink());
         }
         if(m.getPublishedDate() != null){
             writer.setNextPropertyName("publishedDate");
             encoder0.encode(writer, m.getPublishedDate());
         }
         if(m.getTitle() != null){
             writer.setNextPropertyName("title");
             encoder0.encode(writer, m.getTitle());
         }
         if(m.getVersion() != null){
             writer.setNextPropertyName("version");
             encoder0.encode(writer, m.getVersion());
         }
         writer.endObject();
     }
 
     @Override
     protected jp.tnasu.f1tvnews.model.HtmlDocument jsonToModel(org.slim3.datastore.json.JsonRootReader rootReader, int maxDepth, int currentDepth) {
         jp.tnasu.f1tvnews.model.HtmlDocument m = new jp.tnasu.f1tvnews.model.HtmlDocument();
         org.slim3.datastore.json.JsonReader reader = null;
         org.slim3.datastore.json.Default decoder0 = new org.slim3.datastore.json.Default();
         reader = rootReader.newObjectReader("copyright");
         m.setCopyright(decoder0.decode(reader, m.getCopyright()));
         reader = rootReader.newObjectReader("description");
         m.setDescription(decoder0.decode(reader, m.getDescription()));
         reader = rootReader.newObjectReader("htmlContentList");
         reader = rootReader.newObjectReader("key");
         m.setKey(decoder0.decode(reader, m.getKey()));
         reader = rootReader.newObjectReader("language");
         m.setLanguage(decoder0.decode(reader, m.getLanguage()));
         reader = rootReader.newObjectReader("link");
         m.setLink(decoder0.decode(reader, m.getLink()));
         reader = rootReader.newObjectReader("publishedDate");
         m.setPublishedDate(decoder0.decode(reader, m.getPublishedDate()));
         reader = rootReader.newObjectReader("title");
         m.setTitle(decoder0.decode(reader, m.getTitle()));
         reader = rootReader.newObjectReader("version");
         m.setVersion(decoder0.decode(reader, m.getVersion()));
         return m;
     }
 }
