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
 import java.util.UUID;
 import org.sortdc.sortdc.dao.Category;
 import org.sortdc.sortdc.dao.Document;
 import org.sortdc.sortdc.dao.Token;
 
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
      * Initializes the database
      *
      * @throws Exception
      */
     private void init() throws Exception {
         this.addUniqueIndex("documents", "category_id");
     }
 
     /**
      * Creates a unique index on a field
      *
      * @param collection
      * @param field
      * @throws Exception
      */
     private void addUniqueIndex(String collection, String field) throws Exception {
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
         if (category.getId() == null) {
             throw new Exception("You must set a category's id");
         }
         DBCollection collection = this.db.getCollection("categories");
         DBObject query = new BasicDBObject();
         query.put("_id", category.getId());
         collection.save(query);
     }
 
     /**
      * Deletes a category given its id
      *
      * @param category_id
      * @throws Exception
      */
     public void deleteCategoryById(String category_id) throws Exception {
         DBCollection collection;
         collection = this.db.getCollection("documents");
         collection.remove(new BasicDBObject("category_id", category_id));
         collection = this.db.getCollection("categories");
         collection.remove(new BasicDBObject("_id", category_id));
     }
 
     /**
      * Finds a document given its id
      *
      * @param id document id
      * @return document matching id
      * @throws Exception
      */
     public Document findDocumentById(String document_id) throws Exception {
         Document document = new Document();
 
         DBCollection collection = this.db.getCollection("documents");
         DBObject query = new BasicDBObject();
         query.put("_id", document_id);
 
         DBCursor cursor = collection.find(query).limit(1);
 
         if (cursor.hasNext()) {
             DBObject current_doc = cursor.next();
             document.setId(current_doc.get("_id").toString());
             document.setCategoryId(current_doc.get("category_id").toString());
             document.setTokensOccurrences((Map<String, Integer>) current_doc.get("tokens"));
         } else {
             throw new ObjectNotFoundException(ObjectNotFoundException.Type.DOCUMENT);
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
         if (document.getTokensOccurrences().isEmpty()) {
             throw new Exception("Empty tokens list");
         }
 
         String category_id = document.getCategoryId();
         if (category_id == null) {
             throw new Exception("You must set a category's id");
         }
 
         DBCollection collection = this.db.getCollection("documents");
         DBObject query = new BasicDBObject();
         query.put("category_id", category_id);
         query.put("tokens", document.getTokensOccurrences());
 
         String document_id;
         if (document.getId() == null) {
             document_id = UUID.randomUUID().toString();
             collection.insert(query);
             document.setId(query.get("_id").toString());
         } else {
             document_id = document.getId();
             try {
                 this.deleteDocumentTokensOcurrences(this.findDocumentById(document_id));
             } catch (ObjectNotFoundException e) {
             }
             query.put("_id", document_id);
             collection.save(query);
         }
 
         Set<String> names = new HashSet<String>(document.getTokensOccurrences().keySet());
         List<Token> tokens = this.findTokensByNames(names);
         Map<String, Integer> occurences = new HashMap<String, Integer>();
         for (Token token : tokens) {
             names.remove(token.getName());
 
             occurences = token.getOccurrencesByCategory();
 
             if (occurences.containsKey(category_id)) {
                 occurences.put(category_id, occurences.get(category_id) + document.getTokensOccurrences().get(token.getName()));
             } else {
                 occurences.put(category_id, document.getTokensOccurrences().get(token.getName()));
             }
             this.saveToken(token);
         }
         Map<String, Integer> occurences2 = new HashMap<String, Integer>();
         for (String name : names) {
             Token token = new Token();
             token.setName(name);
             occurences2.put(category_id, document.getTokensOccurrences().get(name));
             token.setOccurrencesByCategory(occurences2);
             this.saveToken(token);
         }
     }
 
     /**
      * Deletes tokens' occurrences of a document in a category
      *
      * @throws Exception
      */
     private void deleteDocumentTokensOcurrences(Document document) throws Exception {
         Map<String, Integer> occurences = document.getTokensOccurrences();
 
         for (Map.Entry<String, Integer> doc_occurences : occurences.entrySet()) {
             try {
                 Token token = this.findTokenByName(doc_occurences.getKey());
                 for (Map.Entry<String, Integer> token_occurences : token.getOccurrencesByCategory().entrySet()) {
                     if (token_occurences.getKey().equals(document.getCategoryId())) {
                         token_occurences.setValue(token_occurences.getValue() - doc_occurences.getValue());
                     }
                 }
                 this.saveToken(token);
             } catch (ObjectNotFoundException e) {
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
         Document document = this.findDocumentById(document_id);
         this.deleteDocumentTokensOcurrences(document);
 
         DBCollection collection = this.db.getCollection("documents");
         DBObject query = new BasicDBObject();
         query.put("_id", document_id);
         collection.remove(query);
     }
 
     /**
      * Finds a token given its id
      *
      * @param id token id
      * @return token matching id
      * @throws Exception
      */
     public Token findTokenById(String id) throws Exception {
         return this.findTokenByParam("_id", id);
     }
 
     /**
      * Finds a token given its name
      *
      * @param name token name
      * @return token matching name
      * @throws Exception
      */
     public Token findTokenByName(String name) throws Exception {
         return this.findTokenByParam("_id", name);
     }
 
     /**
      * Finds a token by a parameter (id or name)
      *
      * @param param search parameter
      * @param name token name
      * @return token matching name
      * @throws Exception
      */
     private Token findTokenByParam(String param, String value) throws Exception {
         Token token = new Token();
 
         DBCollection collection = this.db.getCollection("tokens");
         DBObject query = new BasicDBObject();
         query.put(param, value);
 
         DBCursor cursor = collection.find(query).limit(1);
 
         if (cursor.hasNext()) {
             DBObject current_doc = cursor.next();
             token.setId(current_doc.get("_id").toString());
             token.setName(current_doc.get("_id").toString());
             token.setOccurrencesByCategory((Map<String, Integer>) current_doc.get("occurences"));
         } else {
             throw new ObjectNotFoundException(ObjectNotFoundException.Type.TOKEN);
         }
         cursor.close();
         return token;
     }
 
     /**
      * Finds a list of tokens given a set of names
      *
      * @param names set of names
      * @return list of tokens matching names
      * @throws Exception
      */
     public List<Token> findTokensByNames(Set<String> names) throws Exception {
         List<Token> tokens = new ArrayList<Token>();
 
         DBCollection collection = this.db.getCollection("tokens");
         DBObject query = new BasicDBObject();
         query.put("_id", new BasicDBObject("$in", names.toArray()));
 
         DBCursor cursor = collection.find(query);
 
         while (cursor.hasNext()) {
             Token token = new Token();
             DBObject current_token = cursor.next();
             token.setId(current_token.get("_id").toString());
             token.setName(current_token.get("_id").toString());
             token.setOccurrencesByCategory((Map<String, Integer>) current_token.get("occurences"));
 
             tokens.add(token);
         }
         cursor.close();
         return tokens;
     }
 
     /**
      * Saves a new token or update an existing one
      *
      * @param token token to save / update
      * @throws Exception
      */
     public synchronized void saveToken(Token token) throws Exception {
         DBCollection collection = this.db.getCollection("tokens");
         DBObject query = new BasicDBObject();
         query.put("_id", token.getName());
 
         Map<String, Integer> occurences = token.getOccurrencesByCategory();
         DBObject token_occurences = new BasicDBObject();
         for (Map.Entry<String, Integer> cat_occurences : occurences.entrySet()) {
             token_occurences.put(cat_occurences.getKey(), cat_occurences.getValue());
         }
         query.put("occurences", token_occurences);

         collection.save(query);
         token.setId(token.getName());
     }
 
     /**
      * Deletes all categories and documents
      * 
      * @throws Exception 
      */
     public void empty() throws Exception {
         this.db.getCollection("documents").remove(new BasicDBObject());
         this.db.getCollection("categories").remove(new BasicDBObject());
         this.db.getCollection("tokens").remove(new BasicDBObject());
     }
 }
