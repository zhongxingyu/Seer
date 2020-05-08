 package org.sortdc.sortdc.database;
 
 import com.mongodb.BasicDBObject;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 import com.mongodb.Mongo;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import org.sortdc.sortdc.dao.Category;
 import org.sortdc.sortdc.dao.Document;
 import org.sortdc.sortdc.dao.Word;
 
 public class DatabaseMongo extends Database {
 
     private DB db;
 
     public DatabaseMongo() {
         this.setHost("localhost");
         this.setPort(27017);
     }
 
     /**
      * Establishes connection with database
      *
      * @throws Exception
      */
     public void connect() throws Exception {
         if (this.db_name == null) {
             throw new Exception("Dbname unset");
         }
 
         Mongo init = new Mongo(this.host, this.port);
         if (this.username != null && this.password != null && !db.authenticate(this.username, this.password.toCharArray())) {
             throw new Exception("Connection denied : incorrect database authentication");
         }
         DB database = init.getDB(this.db_name);
 
         this.db = database;
         this.init();
     }
 
     /**
      * Initializes the database : creates tables if they don't exist
      *
      * @throws Exception
      */
     private void init() throws Exception {
         this.addUniqueIndex("categories", "name");
         this.addUniqueIndex("documents", "name");
         this.addUniqueIndex("documents", "category_id");
         this.addUniqueIndex("words", "name");
     }
 
     /**
      * Creates a unique index on a field
      *
      * @param collection
      * @param field
      * @throws Exception
      */
     private void addUniqueIndex(String collection, String field) throws Exception{
         DBObject obj = new BasicDBObject();
         obj.put(field, 1);
         DBObject options = new BasicDBObject();
         options.put("unique", true);
        db.getCollection(collection).ensureIndex(obj, options);
     }
 
     /**
      * Finds all registered categories
      * 
      * @return Categories list
      * @throws Exception
      */
     public List<Category> findAllCategories() throws Exception {
         List<Category> categories = new ArrayList();
         DBCollection collection = this.db.getCollection("categories");
         DBCursor cursor = collection.find();
         while (cursor.hasNext()) {
             DBObject obj = cursor.next();
             Category category = new Category();
             category.setId(obj.get("_id").toString());
             category.setName(obj.get("name").toString());
             categories.add(category);
         }
         cursor.close();
         return categories;
     }
 
     /**
      * Saves or updates a category
      *
      * @param category
      * @throws Exception
      */
     public synchronized void saveCategory(Category category) throws Exception {
         DBCollection collection = this.db.getCollection("categories");
         DBObject query = new BasicDBObject();
         query.put("name", category.getName());
         if (category.getId() == null) {
             collection.insert(query);
         } else {
             query.put("_id", category.getId());
             collection.save(query);
         }
     }
 
     /**
      * Deletes a category given its id
      *
      * @param category_id
      * @throws Exception
      */
     public void deleteCategoryById(String category_id) throws Exception {
         this.deleteCategoryByParam("_id", category_id);
     }
 
     /**
      * Deletes a category given its name
      *
      * @param category_id
      * @throws Exception
      */
     public void deleteCategoryByName(String category_name) throws Exception {
         this.deleteCategoryByParam("name", category_name);
     }
 
     /**
      * Deletes a category by a parameter (id or name)
      *
      * @param param search parameter
      * @param value id or name
      * @throws Exception
      */
     private void deleteCategoryByParam(String param, String value) throws Exception {
         DBCollection collection = this.db.getCollection("categories");
         DBObject query = new BasicDBObject();
         query.put(param, value);
 
         collection.remove(query);
     }
 
     /**
      * Finds a document given its id
      *
      * @param id document id
      * @return document matching id
      * @throws Exception
      */
     public Document findDocumentById(String id) throws Exception {
         return this.findDocumentByParam("_id", id);
     }
 
     /**
      * Finds a document given its name
      *
      * @param name document name
      * @return document matching name
      * @throws Exception
      */
     public Document findDocumentByName(String name) throws Exception {
         return this.findDocumentByParam("name", name);
     }
 
     /**
      * Finds a document by a parameter (id or name)
      *
      * @param param search parameter
      * @param name document name
      * @return document matching name
      * @throws Exception
      */
     private Document findDocumentByParam(String param, String value) throws Exception {
         Document document = new Document();
 
         DBCollection collection = this.db.getCollection("documents");
         DBObject query = new BasicDBObject();
         query.put(param, value);
 
         DBCursor cursor = collection.find(query).limit(1);
 
         if (cursor.hasNext()) {
             DBObject current_doc = cursor.next();
             document.setId(current_doc.get("_id").toString());
             document.setName(current_doc.get("name").toString());
             document.setCategoryId(current_doc.get("category_id").toString());
             document.setWordsOccurrences((Map<String, Integer>) current_doc.get("words"));
         } else {
             throw new Exception("Document not found");
         }
         cursor.close();
         return document;
     }
 
     /**
      * Saves a new document or updates an existing one
      *
      * @param document document to save / update
      * @throws Exception
      */
     public synchronized void saveDocument(Document document) throws Exception {
         if (document.getId() != null) {
             Document old_document = this.findDocumentById(document.getId());
             this.deleteDocumentWordsOcurrences(old_document);
         }
         DBCollection collection = this.db.getCollection("documents");
         DBObject query = new BasicDBObject();
         query.put("name", document.getName());
         query.put("category_id", document.getCategoryId());
         query.put("words", document.getWordsOccurrences());
 
         if (document.getId() == null) {
             collection.insert(query);
         } else {
             query.put("_id", document.getId());
             collection.save(query);
         }
 
         Set<String> names = new HashSet<String>(document.getWordsOccurrences().keySet());
         List<Word> words = this.findWordsByNames(names);
         Map<String, Integer> occurences = new HashMap<String, Integer>();
         for (Word word : words) {
             names.remove(word.getName());
 
             occurences = word.getOccurrencesByCategory();
 
             if (occurences.containsKey(document.getCategoryId())) {
                 occurences.put(document.getCategoryId(), occurences.get(document.getCategoryId()) + document.getWordsOccurrences().get(word.getName()));
             } else {
                 occurences.put(document.getCategoryId(), document.getWordsOccurrences().get(word.getName()));
             }
             word.setOccurrencesByCategory(occurences);
             this.saveWord(word);
         }
         for (String name : names) {
             Word word = new Word();
             word.setName(name);
             occurences.put(document.getCategoryId(), document.getWordsOccurrences().get(name));
             word.setOccurrencesByCategory(occurences);
             this.saveWord(word);
         }
     }
 
     /**
      * Deletes words' occurrences of a document in a category
      *
      * @throws Exception
      */
     private void deleteDocumentWordsOcurrences(Document document) throws Exception {
         if (document.getId() != null) {
             Map<String, Integer> occurences = document.getWordsOccurrences();
 
             for (Map.Entry<String, Integer> doc_occurences : occurences.entrySet()) {
                 Word word = this.findWordByName(doc_occurences.getKey());
                 for (Map.Entry<String, Integer> word_occurences : word.getOccurrencesByCategory().entrySet()) {
                     if (word_occurences.getKey().equals(document.getCategoryId())) {
                         word_occurences.setValue(word_occurences.getValue() - doc_occurences.getValue());
                         this.saveWord(word);
                     }
                 }
             }
         }
     }
 
     /**
      * Deletes a document given its id
      *
      * @param document_id
      * @throws Exception
      */
     public void deleteDocumentById(String document_id) throws Exception {
         this.deleteDocumentByParam("_id", document_id);
     }
 
     /**
      * Deletes a document given its name
      *
      * @param document_name
      * @throws Exception
      */
     public void deleteDocumentByName(String document_name) throws Exception {
         this.deleteDocumentByParam("name", document_name);
     }
 
     /**
      * Deletes a document by a parameter (id or name)
      *
      * @param param search parameter
      * @param value id or name
      * @throws Exception
      */
     private void deleteDocumentByParam(String param, String value) throws Exception {
         Document document = this.findDocumentByParam(param, value);
         this.deleteDocumentWordsOcurrences(document);
 
         DBCollection collection = this.db.getCollection("documents");
         DBObject query = new BasicDBObject();
         query.put(param, value);
 
         collection.remove(query);
     }
 
     /**
      * Finds a word given its id
      *
      * @param id word id
      * @return word matching id
      * @throws Exception
      */
     public Word findWordById(String id) throws Exception {
         return this.findWordByParam("_id", id);
     }
 
     /**
      * Finds a word given its name
      *
      * @param name word name
      * @return word matching name
      * @throws Exception
      */
     public Word findWordByName(String name) throws Exception {
         return this.findWordByParam("name", name);
     }
 
     /**
      * Finds a word by a parameter (id or name)
      *
      * @param param search parameter
      * @param name word name
      * @return word matching name
      * @throws Exception
      */
     private Word findWordByParam(String param, String value) throws Exception {
         Word word = new Word();
 
         DBCollection collection = this.db.getCollection("words");
         DBObject query = new BasicDBObject();
         query.put(param, value);
 
         DBCursor cursor = collection.find(query).limit(1);
 
         if (cursor.hasNext()) {
             DBObject current_doc = cursor.next();
             word.setId(current_doc.get("_id").toString());
             word.setName(current_doc.get("name").toString());
             word.setOccurrencesByCategory((Map<String, Integer>) current_doc.get("occurences"));
         } else {
             throw new Exception("Word not found");
         }
         cursor.close();
         return word;
     }
 
     /**
      * Finds a list of words given a set of names
      *
      * @param names set of names
      * @return list of words matching names
      * @throws Exception
      */
     public List<Word> findWordsByNames(Set<String> names) throws Exception {
         List<Word> words = new ArrayList<Word>();
 
         DBCollection collection = this.db.getCollection("words");
         DBObject query = new BasicDBObject();
         query.put("name", new BasicDBObject("$in", names.toArray()));
 
         DBCursor cursor = collection.find(query);
 
         while (cursor.hasNext()) {
             Word word = new Word();
             DBObject current_word = cursor.next();
             word.setId(current_word.get("_id").toString());
             word.setName(current_word.get("name").toString());
             word.setOccurrencesByCategory((Map<String, Integer>) current_word.get("occurences"));
 
             words.add(word);
         }
         cursor.close();
         return words;
     }
 
     /**
      * Saves a new word or update an existing one
      *
      * @param word word to save / update
      * @throws Exception
      */
     public synchronized void saveWord(Word word) throws Exception {
         DBCollection collection = this.db.getCollection("words");
         DBObject query = new BasicDBObject();
         query.put("name", word.getName());
 
         Map<String, Integer> occurences = word.getOccurrencesByCategory();
         DBObject word_occurences = new BasicDBObject();
         for (Map.Entry<String, Integer> cat_occurences : occurences.entrySet()) {
             System.out.println(cat_occurences.getKey()+","+cat_occurences.getValue());
         }
         query.put("occurences", word_occurences);
 
         if (word.getId() == null) {
             collection.insert(query);
         } else {
             query.put("_id", word.getId());
             collection.save(query);
         }
     }
 }
