 package org.zoneproject.extractor.utils;
 
 import com.hp.hpl.jena.query.QuerySolution;
 import com.hp.hpl.jena.query.ResultSet;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.Property;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.util.FileManager;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import virtuoso.jena.driver.VirtModel;
 import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;
 
 /**
  *
  * @author Desclaux Christophe <christophe@zouig.org>
  */
 
 public abstract class VirtuosoDatabase {
     private static Model st = null;
     private static String VIRTUOSO_SERVER = Config.getVar("Virtuoso-server-uri");
     private static String VIRTUOSO_USER = Config.getVar("Virtuoso-server-user");
     private static String VIRTUOSO_PASS = Config.getVar("Virtuoso-server-pass");
     public static String ZONE_URI = "http://demo.zone-project.org/data";
     
     public static Model getStore(){
         if(st == null){
             st = VirtModel.openDatabaseModel(ZONE_URI, VIRTUOSO_SERVER, VIRTUOSO_USER, VIRTUOSO_PASS);
         }
         return st;
     }
     
     public static void addItems(ArrayList<Item> items){
         addItems(items.toArray(new Item[items.size()]));
     }
     
     public static void addItems(Item[] items){
         for(int i=0; i < items.length;i++){
             addItem(items[i]);
         }
     }
 
     public static void addItem(Item item){
         Iterator it = item.values.iterator();
         while (it.hasNext()){
             addAnnotation(item.getUri(), (Prop)it.next());
         }
     }
     
     public static void addAnnotations(String itemUri, ArrayList<Prop> prop){
         for(int i=0; i< prop.size();i++){
             VirtuosoDatabase.addAnnotation(itemUri, prop.get(i));
         }
     }
     
     public static void addAnnotation(String itemUri, Prop prop){
         Model model = ModelFactory.createDefaultModel();
         Resource itemNode = model.createResource(itemUri);
         if(prop.isLiteral()){
             itemNode.addLiteral(prop.getType(), model.createLiteral(prop.getValue()));
         }
         else{
             itemNode.addProperty(prop.getType(), model.createResource(prop.getValue()));
         }
         getStore().add(model);
     }
     
     /**
      * Run a SPARQL request on the EndPoint
      * @param queryString the SPARQL request
      * @return the set of results
      */
     public static ResultSet runSPARQLRequest(String queryString){
         return VirtuosoQueryExecutionFactory.create(queryString,getStore()).execSelect() ;
     }
     
     public static boolean runSPARQLAsk(String queryString){
         return VirtuosoQueryExecutionFactory.create(queryString,getStore()).execAsk() ;
     }
     /**
      * get all items which has not been annotated for a plugin
      * @param pluginURI the plugin URI
      * @return the items
      */
     public static Item[] getItemsNotAnotatedForOnePlugin(String pluginURI){
         ArrayList<Item> items = new ArrayList<Item>();
         String request = "SELECT DISTINCT ?uri WHERE{  ?uri <http://purl.org/rss/1.0/title> ?title  OPTIONAL {?uri <"+pluginURI+"> ?pluginDefined} FILTER (!bound(?pluginDefined)) }";
         ResultSet results = runSPARQLRequest(request);
 
         while (results.hasNext()) {
             QuerySolution result = results.nextSolution();
             items.add(getOneItemByURI(result.get("?uri").toString()));
         }
         return items.toArray(new Item[items.size()]);
     }
 
     /**
      * get all items which has not been annotated for a plugin
      * @param pluginURI the plugin URI
      * @return the items
      */
     public static Item[] getItemsNotAnotatedForPluginsWithDeps(String pluginURI, String []deps){
         ArrayList<Item> items = new ArrayList<Item>();
         String requestPlugs ="";
         int i=0;
         for(String curPlugin : deps){
             requestPlugs += ". ?uri <"+curPlugin+"> ?deps"+i++ +" ";
         }
         
         String request = "SELECT ?uri WHERE{  ?uri <http://purl.org/rss/1.0/title> ?title "+requestPlugs+". OPTIONAL {?uri <"+pluginURI+"> ?pluginDefined.  } FILTER (!bound(?pluginDefined)) }";
         System.out.println(request);
         ResultSet results = runSPARQLRequest(request);
 
         while (results.hasNext()) {
             QuerySolution result = results.nextSolution();
             items.add(getOneItemByURI(result.get("?uri").toString()));
         }
         return items.toArray(new Item[items.size()]);
     }
 
     /**
      * Get an Item from the Database
      * @param uri
      * @return 
      */
     public static Item getOneItemByURI(String uri){
         String request = "SELECT ?relation ?value WHERE{  <"+uri+"> ?relation ?value}";
         ResultSet results = runSPARQLRequest(request);
         return new Item(uri,results,uri,"relation","?value");
     }
     
     public static boolean ItemURIExist(String uri){
         return contains(uri, "http://purl.org/rss/1.0/title");
     }
     
     public static boolean contains(String uri, String prop){
         Model m = ModelFactory.createDefaultModel();
         Resource r = m.createResource(uri);
         Property p = m.createProperty(prop);
         return getStore().contains(r,p);
         
     }
     
     public static void verifyItemsList(ArrayList<Item> items){
         for (Iterator<Item> iterator = items.iterator(); iterator.hasNext(); ) {
             Item o = iterator.next();
             if (ItemURIExist(o.getUri())) {
                 iterator.remove();
             }
         }
     }
     
     public static void deleteItem(String uri) throws IOException{
         Item item = getOneItemByURI(uri);
         getStore().remove(item.getModel());
     }
     
     public static void loadFolder(String graphURI,String dir){
         File file = new File(dir);
         File[] files = file.listFiles();
         if (files != null) {
             for (int i = 0; i < files.length; i++) {
                 if (files[i].isFile() == true) {
                     try {
                         loadFile(graphURI,files[i].getAbsolutePath());
                     } catch (FileNotFoundException ex) {
                         Logger.getLogger(VirtuosoDatabase.class.getName()).log(Level.WARNING, null, ex);
                     } catch (IOException ex) {
                         Logger.getLogger(VirtuosoDatabase.class.getName()).log(Level.WARNING, null, ex);
                     }
                 } else if(files[i].isDirectory() == true){
                     loadFolder(graphURI,files[i].getAbsolutePath());
                 }
             }
         }
     }
     
     public static void loadFile(String graphURI,String path) throws FileNotFoundException, IOException{
         Model model = ModelFactory.createDefaultModel();
         FileManager.get().readModel(model,path);
         
         getStore().add(model, true);
     }
     
     public static void main(String[] args) throws FileNotFoundException, IOException{
         loadFile("","./test.rdf");
         ResultSet r = runSPARQLRequest("SELECT ?x ?t WHERE {?x rdf:type ?t} ");
         System.out.println(r.getResourceModel());
         
         System.out.println("addItem");
         String uri="http://testURI.com/#MyURI";
         Item item = new Item(uri);
         item.addProp(new Prop("http://purl.org/rss/1.0/title","le titre",true));
         
         
         VirtuosoDatabase.addItem(item);
         VirtuosoDatabase.deleteItem(uri);
         System.out.println(VirtuosoDatabase.ItemURIExist(uri));
         System.out.println(VirtuosoDatabase.ItemURIExist("http://www.personnes.com#Margot"));
     }
 }
