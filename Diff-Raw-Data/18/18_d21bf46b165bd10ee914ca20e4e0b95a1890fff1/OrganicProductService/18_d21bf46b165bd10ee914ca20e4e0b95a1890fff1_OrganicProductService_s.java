 package it.aip.service;
 
 import it.aip.models.BioProducer;
 import it.aip.models.ImageFile;
 import it.aip.models.OrganicProduct;
 import it.aip.models.OrganicProductMeta;
 import it.aip.models.Recipe;
 import it.aip.models.RecipeProduct;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import org.slim3.controller.upload.FileItem;
 import org.slim3.datastore.Datastore;
 import org.slim3.util.BeanUtil;
 
 import com.google.appengine.api.datastore.Transaction;
 
 public class OrganicProductService {
     private OrganicProductMeta organicProductMeta = new OrganicProductMeta();
 
     // Metodo per la creazione di un nuovo BioProducer
     @SuppressWarnings("rawtypes")
     public OrganicProduct createOrganicProduct(Map<String, Object> requestParameters) {
         
         OrganicProduct product = new OrganicProduct();
         
         // Copio tutti i parametri della request nei rispettivi campi del produttore
         BeanUtil.copy(requestParameters, product);
         
         // Se è stato specificato un produttore, lo linko al prodotto
         if(requestParameters.containsKey("producerKey")){
             String key = (String) requestParameters.get("producerKey");
             BioProducer producer = Datastore.get(BioProducer.class, Datastore.stringToKey(key));
             product.getProducerRef().setModel(producer);
             
             // TEST CODE 
             // System.out.println(product.getProductName() + " linkato a " + product.getProducerRef().getModel().getProducerName());
         }
         
         if(requestParameters.containsValue("on")){
             
             // Scandisco tutte le chiavi
             Set keysSet = requestParameters.keySet();
             Iterator iterator = keysSet.iterator();
             while(iterator.hasNext()){
                 String key = (String) iterator.next();
                 
                 // Se trovo una chiave con valore "on" (checkbox selezionata)
                 if(requestParameters.get(key).equals("on")){
                     // Setto la relazione
                     Recipe recipe = Datastore.get(Recipe.class, Datastore.stringToKey(key));
                     
                     RecipeProduct recipeProduct = new RecipeProduct();
                     recipeProduct.getProductRef().setModel(product);
                     recipeProduct.getRecipeRef().setModel(recipe);                    
                     
                     // TEST CODE
                     // System.out.println(recipeProduct.getProductRef().getModel().getProductName() + " linkato a " + recipeProduct.getRecipeRef().getModel().getRecipeName());
                     
                 }
             }
         }
         
         // Suppongo che l'utente possa inserire fino ad un massimo di 4 immagini
         int imageCount = 4;
         for(int i = 1; i<=imageCount; i++){
             // Per ogni immagine che l'utente può inserire
             if(requestParameters.containsKey("imageProduct" + i)){
                 // Estraggo dalla request
                 FileItem tempFile = (FileItem) requestParameters.get("imageProduct" + i);
                 ImageFile tempImage = new ImageFile(tempFile.getFileName(), tempFile.getContentType(), tempFile.getData());
                 
                 // La aggiungo alla lista del produttore
                 if(product.getImages() == null){
                     product.setImages(new ArrayList<ImageFile>());
                 }
                 product.getImages().add(tempImage);
                
             }            
         }
         
         // Storing...
         Transaction tx = Datastore.beginTransaction();
         Datastore.put(product);
         tx.commit();
         
         return product;
     }
     
     // Metodo per l'estrazione di tutti i BioProducers
     public List<OrganicProduct> getAllOrganicProducts() {
         return Datastore.query(organicProductMeta).asList();
     }
     
     // Metodo per l'estrazione di tutti i BioProducers
     public List<String> getAllCategories() {
         List<OrganicProduct> organic_products = this.getAllOrganicProducts();  
         List<String> categorie = new ArrayList<String>();
         
         for(OrganicProduct x : organic_products){
            if(x.getProductCategory() != "")
                 categorie.add(x.getProductCategory());
         }
         
         HashSet hashSet = new HashSet(categorie);
         ArrayList categories_no_duplicates = new ArrayList(hashSet);
         Collections.sort(categories_no_duplicates);
         return categories_no_duplicates;
     }
     
     public List<OrganicProduct> getProductByCategory(String category) {
         return Datastore.query(organicProductMeta).filter(organicProductMeta.productCategory.equal(category)).asList();
     }
     
     public OrganicProduct getProduct(String key){
         return Datastore.get(organicProductMeta, Datastore.stringToKey(key));
     }
 }
