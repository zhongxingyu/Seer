 package org.sortdc.sortdc;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import org.sortdc.sortdc.dao.Category;
 import org.sortdc.sortdc.dao.Document;
 import org.sortdc.sortdc.database.Database;
 
 public class Classifier {
 
     private Tokenization tokenization;
     private Database database;
     private Map<String, Category> categories;
 
     /**
      * Instance of Classifier
      *
      * @param tokenization Instance of Tokenization
      */
     public Classifier(Tokenization tokenization, Database database) throws Exception {
         this.tokenization = tokenization;
         this.database = database;
         this.loadCategories();
     }
 
     /**
      * Assigns a new text to an existing category
      *
      * @param text Text
      * @param category_id Category's id
      * @throws Exception
      */
     public void train(String name, String text, String category_name) throws Exception {
         if (!this.categories.containsKey(category_name)) {
             throw new Exception("Category not found: " + category_name);
         }
         List<String> words = tokenization.extract(text);
         Map<String, Integer> occurrences = this.tokenization.getOccurrences(words);
 
         Document doc = new Document();
         doc.setName(name);
         doc.setCategoryId(this.categories.get(category_name).getId());
         doc.setWordsOccurrences(occurrences);
 
         this.database.saveDocument(doc);
     }
 
     /**
      * Determines the category of a new text
      *
      * @param text Text
      * @throws Exception
      */
     public Category categorize(String text) throws Exception {
         List<String> words = tokenization.extract(text);
         int nb_words = words.size();
         Map<String, Integer> occurrences = this.tokenization.getOccurrences(words);
         Map<String, Float> probabilities = new HashMap();
 
         for (Map.Entry<String, Integer> pairs : occurrences.entrySet()) {
             // TODO
             System.out.println(pairs.getKey() + " = " + pairs.getValue());
         }
         // TODO
         return null;
     }
 
     /**
      * Loads all categories and stores it in this.categories
      * 
      * @throws Exception
      */
     private synchronized void loadCategories() throws Exception {
        this.categories = new HashMap<String, Category>();
         List<Category> categories_list = this.database.findAllCategories();
         for (Category category : categories_list) {
             this.categories.put(category.getName(), category);
         }
     }
 }
