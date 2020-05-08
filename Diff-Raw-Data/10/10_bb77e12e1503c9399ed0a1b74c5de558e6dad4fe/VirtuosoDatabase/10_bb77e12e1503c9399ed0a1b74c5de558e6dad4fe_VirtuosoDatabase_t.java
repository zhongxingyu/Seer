 package org.zoneproject.extractor.utils;
 
 /*
  * #%L
  * ZONE-utils
  * %%
  * Copyright (C) 2012 ZONE-project
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * #L%
  */
 
 import com.hp.hpl.jena.query.QueryExecution;
 import com.hp.hpl.jena.query.QueryExecutionFactory;
 import com.hp.hpl.jena.query.QuerySolution;
 import com.hp.hpl.jena.query.ResultSet;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.Property;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.shared.JenaException;
 import com.hp.hpl.jena.util.FileManager;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import virtuoso.jena.driver.VirtGraph;
 import virtuoso.jena.driver.VirtModel;
 import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;
 
 /**
  *
  * @author Desclaux Christophe <christophe@zouig.org>
  */
 
 public abstract class VirtuosoDatabase {
     private static final org.apache.log4j.Logger  logger = org.apache.log4j.Logger.getLogger(App.class);
     private static Model st = null;
     private static String VIRTUOSO_SERVER = Config.getVar("Virtuoso-server-uri");
     private static String VIRTUOSO_USER = Config.getVar("Virtuoso-server-user");
     private static String VIRTUOSO_PASS = Config.getVar("Virtuoso-server-pass");
     public static String ZONE_URI = ZoneOntology.GRAPH_NEWS;
     
     public static Model getStore(){
         if(st == null){
             VirtGraph vgraph = new VirtGraph(VIRTUOSO_SERVER, VIRTUOSO_USER, VIRTUOSO_PASS);
             vgraph.setReadFromAllGraphs(true);
             st = new VirtModel(vgraph);
         }
         return st;
     }
     public static void initStore(){
         VirtGraph vgraph = new VirtGraph(VIRTUOSO_SERVER, VIRTUOSO_USER, VIRTUOSO_PASS);
         
         vgraph.setReadFromAllGraphs(true);
         st = new VirtModel(vgraph);
     }
     
     public static Model getStore(String graph){
         return VirtModel.openDatabaseModel(graph, VIRTUOSO_SERVER, VIRTUOSO_USER, VIRTUOSO_PASS);
     }
     
     public static void addItems(ArrayList<Item> items){
         addItems(items.toArray(new Item[items.size()]));
     }
     
     public static void addItems(Item[] items){ 
         Model model = getModelForItems(items);
         addModelToStore(model, ZoneOntology.GRAPH_NEWS);
     }
 
     public static void addItem(Item item){
         Model model = getModelForItem(item);
         addModelToStore(model, ZoneOntology.GRAPH_NEWS);
     }
     
     public static void addAnnotations(HashMap<String, ArrayList<Prop>> props){
        addAnnotations(props,ZoneOntology.GRAPH_NEWS);
    }
    public static void addAnnotations(HashMap<String, ArrayList<Prop>> props, String graph){
        if(props == null || props.isEmpty()){
             return;
         } 
         Model model = ModelFactory.createDefaultModel();
         
         Iterator it = props.keySet().iterator();
         while (it.hasNext()){
            String itemUri = (String)(it.next());
            ArrayList<Prop> itemProps = props.get(itemUri);
            model.add(getModelForAnnotations(itemUri, itemProps));
         }
        addModelToStore(model, graph);
     }
    
     public static void addAnnotations(String itemUri, ArrayList<Prop> props){
         addAnnotations(itemUri,props, ZoneOntology.GRAPH_NEWS);
     }
     public static void addAnnotations(String itemUri, ArrayList<Prop> props, String graph){
         if(props == null){
             return;
         }
         Model model = getModelForAnnotations(itemUri, props);
         addModelToStore(model,graph);
     }
     
     /**
      * Add annotation for a news
      * @param itemUri
      * @param prop 
      */
     public static void addAnnotation(String itemUri, Prop prop){
         addAnnotation(itemUri, prop, ZoneOntology.GRAPH_NEWS);
     }
     
     /**
      * Add annotation for a news
      * @param itemUri
      * @param prop 
      * @param graph
      */
     public static void addAnnotation(String itemUri, Prop prop, String graph){
         Model model = getModelForAnnotation(itemUri, prop);
         addModelToStore(model,graph);
     }
     
     /**
      * get the rdf model for items
      * @param items
      */
     public static Model getModelForItems(Item[] items){    
         Model model = ModelFactory.createDefaultModel();
         for(int i=0; i < items.length;i++){
             model.add(getModelForItem(items[i]));
         }
         return model;
     }
     
     /**
      * get the rdf model for an item
      * @param item
      */
     public static Model getModelForItem(Item item){
         return getModelForAnnotations(item.getUri(), item.getElements());
     }
     
     /**
      * get the rdf model for annotations
      * @param itemUri
      * @param props
      */
     public static Model getModelForAnnotations(String itemUri, ArrayList<Prop> props){
         Model model = ModelFactory.createDefaultModel();
         for (Iterator<Prop> it = props.iterator(); it.hasNext();) {
             Prop prop = it.next();
             if(prop.getChildren() != null){
                 model.add(getModelForAnnotations(prop.getValue(), prop.getChildren()));
             }
             model.add(getModelForAnnotation(itemUri, prop));
         }
         return model;
         
     }
     
     /**
      * Get the model corresponding to an annotation
      * @param itemUri
      * @param prop
      */
     public static Model getModelForAnnotation(String itemUri, Prop prop){
         Model model = ModelFactory.createDefaultModel();
         try {
             
             if(prop.isIsSearchable()) {
                 model.add(VirtuosoDatabase.getModelForAnnotation(prop.getType().getURI(),new Prop(ZoneOntology.ANNOTATION, "true",true)));
             }
             Resource itemNode = model.createResource(itemUri);
             String val = prop.getValue();
             if(val == null)
                 val = "";
             byte[] utf8;
             utf8 = val.getBytes("UTF-8");
             val = new String(utf8, "UTF-8");
             if(prop.isLiteral()){
                 itemNode.addLiteral(prop.getType(), model.createLiteral(val));
             }
             else{
                 itemNode.addProperty(prop.getType(), model.createResource(val));
             }
         } catch (UnsupportedEncodingException ex) {
             Logger.getLogger(VirtuosoDatabase.class.getName()).log(Level.SEVERE, null, ex);
         }
         return model;
     }
     
     public static void addModelToStore(Model model, String graph){
         int i = 0;
         while((i++)<=5){
             try {
                 getStore(graph).add(model);
 
                 ExecutorService executor = Executors.newSingleThreadExecutor();
                 try {
                     Future<Model> task = executor.submit(new ThreadAddModelToStore(graph,model));
                     task.get(60, TimeUnit.SECONDS);
                     break;
                 } catch (InterruptedException ex) {
                     logger.warn(ex);
                 } catch (ExecutionException ex) {
                     Throwable t = ex.getCause();
                     if( t instanceof JenaException ) {
                         throw (JenaException)t;
                     }else{
                         throw new RuntimeException( t );
                     }
                 } catch (TimeoutException ex) {
                     logger.warn(ex);
                 }finally{
                     if(!executor.isTerminated()){
                         executor.shutdown();
                     }
                 }
                 
             } catch (JenaException ex) {
                 if(ex.getMessage().contains("timeout")){
                     logger.warn("connection lost with server (wait 5 secondes)("+i+ " try)");
                 }else{
                     logger.warn("annotation process error because of virtuoso partial error(wait 5 secondes)("+i+ " try)");
                 }
                 
                 try{Thread.currentThread().sleep(5000);}catch(InterruptedException ie){}
             }
         }
     }
 
     /**
      * Run a SPARQL request on the EndPoint
      * @param queryString the SPARQL request
      * @return the set of results
      */
     public static ResultSet runSPARQLRequest(String queryString)throws JenaException{
         return VirtuosoDatabase.runSPARQLRequest(queryString, getStore());
     }
     
     /**
      * Run a SPARQL request on the EndPoint
      * @param queryString the SPARQL request
      * @param graphUri the Graph in which work
      * @return the set of results
      */
     public static ResultSet runSPARQLRequest(String queryString, String graphUri)throws JenaException{
         return VirtuosoDatabase.runSPARQLRequest(queryString, getStore(graphUri));
     }
     
     public static ResultSet runSPARQLRequest(String queryString, Model store)throws JenaException{
         QueryExecution q = VirtuosoQueryExecutionFactory.create(queryString,store);
         ResultSet res = null;
 
         ExecutorService executor = Executors.newSingleThreadExecutor();
         try {
             Future<ResultSet> task = executor.submit(new ThreadExec(q));
             res = task.get(20, TimeUnit.SECONDS);
         } catch (InterruptedException ex) {
             logger.warn(ex);
         } catch (ExecutionException ex) {
             Throwable t = ex.getCause();
             if( t instanceof JenaException ) {
                 throw (JenaException)t;
             }else{
                 throw new RuntimeException( t );
             }
         } catch (TimeoutException ex) {
             logger.warn(ex);
         }finally{
             if(!executor.isTerminated()){
                 executor.shutdown();
             }
         }
         return res;
     }
     public static boolean runSPARQLAsk(String queryString){
         return VirtuosoQueryExecutionFactory.create(queryString,getStore()).execAsk() ;
     }
     
     public static ResultSet getRelationsForURI(String uri, String graphUri){
         String query = "SELECT DISTINCT ?relation ?object { <"+uri+"> ?relation ?object.}";
         try{
             return runSPARQLRequest(query,graphUri);
         }catch(Exception ex){
             Logger.getLogger(VirtuosoDatabase.class.getName()).log(Level.WARNING, null, ex);
             return null;
         }
     }
     /**
      * Get a map of concepts/objects for a particular Uri
      * @param uri
      * @param graphUri
      * @return 
      */
     public static Map getMapForURI(String uri, String graphUri){
         ResultSet results = Database.getRelationsForURI(uri, graphUri);
         Map<String,String> res = new HashMap<String,String>();
         
         while (results.hasNext()) {
             QuerySolution result = results.nextSolution();
             res.put(result.get("?relation").toString(),result.get("?object").toString() );
         }
         return res;
     }
 
     /**
      * get all items which has not been annotated for a plugin
      * @param pluginURI the plugin URI
      * @return the items
      */
     public static Item[] getItemsNotAnotatedForOnePlugin(String pluginURI){
         return getItemsNotAnotatedForOnePlugin(pluginURI,100);
     }
     public static Item[] getItemsNotAnotatedForOnePlugin(String pluginURI, int limit){
         return getItemsNotAnotatedForPluginsWithDeps(pluginURI,new  String[0],limit);
     }
     public static Item[] getItemsNotAnotatedForPluginsWithDeps(String pluginURI, String []deps){
         return getItemsNotAnotatedForPluginsWithDeps(pluginURI, deps,10);
     }
     public static Item[] getItemsNotAnotatedForPluginsWithDeps(String pluginURI, String []deps, int limit){
         Prop [] depsProps = new Prop[deps.length];
         for(int i=0; i < deps.length; i++){
             depsProps[i] = new Prop(deps[i],"?deps"+i);
         }
         return getItemsNotAnotatedForPluginsWithDeps(pluginURI, depsProps, limit);
     }
     
     public static Item[] getItemsNotAnotatedForPluginsWithDeps(String pluginURI, Prop []deps, int limit){
         ArrayList<Item> items = new ArrayList<Item>();
         String requestPlugs ="";
         for(Prop curPlugin : deps){
             requestPlugs += ". ?uri <"+curPlugin.getProp()+"> "+curPlugin.getValue()+" ";
         }
         
         //first  we run the big request on server in order to retrive all the items not anotated with their properties
         String request = "SELECT ?uri ?relation ?value WHERE{ ?uri ?relation ?value. { SELECT DISTINCT ?uri FROM <http://zone-project.org/datas/items> WHERE{  ?uri <http://purl.org/rss/1.0/title> ?title "+requestPlugs+". OPTIONAL {?uri <"+pluginURI+"> ?pluginDefined.  } FILTER (!bound(?pluginDefined)) } LIMIT "+limit+ "} }";
         logger.info(request);
         ResultSet results;
         try{
             results = runSPARQLRequest(request);
         }catch(JenaException ex){
             if(ex.getMessage().contains("timeout") || ex.getMessage().contains("Problem during serialization") || ex.getMessage().contains("Connection failed") ){
                 logger.warn(ex);
                 logger.warn("connection lost with server (wait 5 secondes)");
                 initStore();
                 try{Thread.currentThread().sleep(5000);}catch(InterruptedException ie){}
                 return getItemsNotAnotatedForPluginsWithDeps(pluginURI, deps, limit);
             }else{
                 logger.warn(ex);
                 logger.warn("Encoding error in some uri's request:"+request);
                 return null;
             }
         }
         
         //we store all the results in a HashMap for each item
         QuerySolution result;
         String itemURI;
         HashMap<String,ArrayList<QuerySolution>> mappedItems = new HashMap<String,ArrayList<QuerySolution>>();
         while (results.hasNext()) {
             result = results.nextSolution();
             itemURI = result.get("?uri").toString();
             if(!mappedItems.containsKey(itemURI)) {
                 mappedItems.put(itemURI, new ArrayList<QuerySolution>());
             }
             mappedItems.get(itemURI).add(result);
         }
         
         //we create all the items
         Item item;
         Iterator it = mappedItems.keySet().iterator();
         while (it.hasNext()){
            String uri =(String)(it.next());
            ArrayList<QuerySolution> values = mappedItems.get(uri);
            item = new Item(uri,values,uri,"relation","?value");
            if(item != null){
                items.add(item);
            }
         }
         
         //manage case with no items in output but with other items to proceed
         if(results.getRowNumber() > 0  && items.isEmpty()){
             return null;
         }
         return items.toArray(new Item[items.size()]);
     }
 
     /**
      * get all items which has not been annotated for a plugin
      * @param pluginURI the plugin URI
      * @return the items
      */
     public static ArrayList<Item> getItemsFromSource(String source){
         ArrayList<Item> items = new ArrayList<Item>();
         String requestPlugs ="";
         
         String request = "SELECT DISTINCT(?uri) WHERE{?uri <http://purl.org/rss/1.0/source> <"+source+">.}";
         ResultSet results;
         try{
             results = runSPARQLRequest(request);
         }catch(JenaException ex){
             Logger.getLogger(VirtuosoDatabase.class.getName()).log(Level.WARNING, null, ex);
             return items;
         }
 
         while (results.hasNext()) {
             QuerySolution result = results.nextSolution();
             items.add(getOneItemByURI(result.get("?uri").toString()));
         }
         return items;
     }
 
     /**
      * Get an Item from the Database
      * @param uri
      * @return 
      */
     public static Item getOneItemByURI(String uri){
         try{
             String request = "SELECT ?relation ?value FROM <http://zone-project.org/datas/items> WHERE{  <"+uri+"> ?relation ?value}";
             ResultSet results = runSPARQLRequest(request);
             return new Item(uri,results,uri,"relation","?value");
         }catch(JenaException ex){
             logger.warn("There are encoding errors on item "+uri+" the item will be deleted");
             //deleteItem(uri);
             return null;
         }
     }
 
     public static boolean ItemURIExist(String uri){
         return contains(uri, "http://purl.org/rss/1.0/title");
     }
     
     public static boolean contains(String uri, String prop){
         Model m = ModelFactory.createDefaultModel();
         Resource r = m.createResource(uri);
         Property p = m.createProperty(prop);
         boolean res = true;
         try{
             res = getStore().contains(r,p);
         }
         catch(Exception e){
             deleteItem(uri);
             logger.warn("The item "+uri +" existance cannot be check due to encoding errors  ("+ e+")");
             return false;
         }
         return res;
         
     }
     
     public static void verifyItemsList(ArrayList<Item> items){
         for (Iterator<Item> iterator = items.iterator(); iterator.hasNext(); ) {
             Item o = iterator.next();
             if (ItemURIExist(o.getUri())) {
                 iterator.remove();
             }
         }
     }
     
     public static void deleteItem(String uri){
         if(uri == null || uri.equals("") || !uri.startsWith("http"))return;
         String deleteRequest="DELETE{<"+uri+"> ?a ?b.}WHERE{<"+uri+"> ?a ?b.}";
         try {
             runSPARQLRequest(deleteRequest,ZoneOntology.GRAPH_NEWS);
         } catch (JenaException ex) {
             logger.warn("Impossible to delete the item "+uri +"\n"+ex);
         }
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
         getStore(graphURI).add(model, true);
     }
     
     public static void main(String[] args) throws FileNotFoundException, IOException{
         /*
         loadFile("","./test.rdf");
         ResultSet r = runSPARQLRequest("SELECT ?x ?t WHERE {?x rdf:type ?t} ");
         logger.info(r.getResourceModel());
         
         logger.info("addItem");
         String uri="http://testURI.com/#MyURI";
         Item item = new Item(uri);
         item.addProp(new Prop("http://purl.org/rss/1.0/title","le titre",true));
         
         
         VirtuosoDatabase.addItem(item);
         VirtuosoDatabase.deleteItem(uri);
         logger.info(VirtuosoDatabase.ItemURIExist(uri));
         logger.info(VirtuosoDatabase.ItemURIExist("http://www.personnes.com#Margot"));
         * */
         //extractDB();
         String uri = "http://rss.lefigaro.fr/~r/lefigaro/laune/~3/HKViz2m0_3Q/story01.htm";
         uri = "https://twitter.com/furyGnu/status/393050712168730624";
         /*deleteItem(uri);
         System.out.println(getItemsNotAnotatedForOnePlugin("http://zone-project.org/model/plugins/WikiMeta"));
         System.out.println(VIRTUOSO_SERVER);*/
         System.out.println(getOneItemByURI(uri));
         
         Prop p = new Prop("http://test", "RT @mordorion: Cantat et Polanski en couv’ de magasines, l’exception culturelle française ");
         }
     
     public static void extractDB(){
         FileOutputStream fout;
         try {
             fout = new FileOutputStream("dbExtract_monde.rdf");
             //CONSTRUCT{?uri ?prop ?value} WHERE {  ?uri <http://purl.org/rss/1.0/source> <http://www.tv5.org/TV5Site/rss/actualites.php?rub=12>. ?uri ?prop ?value}
             
             String queryString ="CONSTRUCT{?uri ?prop ?source} WHERE {  ?uri <http://purl.org/rss/1.0/source> ?source}";
             //Query query = QueryFactory.create(queryString) ;
             QueryExecution qexec = QueryExecutionFactory.create(queryString, getStore()) ;
             Model results = qexec.execConstruct() ;
             results.write(System.out);
             //results.write(fout, "RDF/XML");
             //ResultSetFormatter.out(System.out, results, query) ;
 
             
             //ResultSet r =VirtuosoQueryExecutionFactory.create(queryString,getStore()).execSelect();
             //Model m = ModelFactory.createDefaultModel();
             //Model m = ResultSetFormatter.toModel(results) ;
             //m.write(fout);
         } catch (FileNotFoundException ex) {
             Logger.getLogger(VirtuosoDatabase.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 }
 
 
 class ThreadAddModelToStore implements Callable<Model> {
    String graph;
    Model model;
    ThreadAddModelToStore(String graph, Model model) {
       this.graph = graph;
       this.model = model;
    }
    public Model call() throws JenaException {return VirtuosoDatabase.getStore(graph).add(model);}
 }
 class ThreadExec implements Callable<ResultSet> {
    QueryExecution q;
    ThreadExec(QueryExecution q) {this.q = q;}
    public ResultSet call() throws JenaException {return q.execSelect();}
 }
