 package org.lockss.plugin.internationalunionofcrystallography;
 
 import static org.lockss.db.MongoDbManager.*;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.lockss.daemon.PluginException;
 import org.lockss.db.DbManager;
 import org.lockss.db.MongoDbManager;
 import org.lockss.db.MongoHelper;
 import org.lockss.extractor.JenaMetadataExtractor;
 import org.lockss.metadata.ArticleMetadataBuffer.ArticleMetadataInfo;
 import org.lockss.plugin.ArchivalUnit;
 import org.lockss.util.Logger;
 
 import com.google.gson.Gson;
 import com.hp.hpl.jena.query.*;
 import com.hp.hpl.jena.rdf.model.*;
 import com.hp.hpl.jena.tdb.TDBFactory;
 import com.mongodb.BasicDBList;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.DBObject;
 import com.mongodb.QueryBuilder;
 
 public class InternationalUnionOfCrystallographyJenaMetadataExtractor implements
 		JenaMetadataExtractor {
 	static Logger log = Logger
 			.getLogger(InternationalUnionOfCrystallographyJenaMetadataExtractor.class
 					.getName());
 	
 	// TODO: Where is the URI for "normal" attributes?
 	private final String metadataPredicateBaseUri = "http://www.todo.com/metadata/info#";
 	private final String cifPredicateBaseUri = "http://www.iucr.org/__data/iucr/cif/standard/cifstd7.html#";
 	private final String jenaDirectory = "db/jena/DB1";
 	
 	private Dataset dataset = null;
 	private Model model = null;
 	private Resource article = null;
 	
 	public InternationalUnionOfCrystallographyJenaMetadataExtractor() {
 		new File(jenaDirectory).mkdirs();
 		dataset = TDBFactory.createDataset(jenaDirectory);
 		model = dataset.getDefaultModel();	
 	}
 
 	@Override
 	public void extract(ArchivalUnit au, DbManager dbManager)
 			throws IOException, PluginException {
 		log.info("EXTRACTION STARTED!");
 		
 		String auId = au.getAuId();
 		String pluginId = au.getPluginId();
 		
 		DB mongoDatabase = ((MongoDbManager)dbManager).getDb();
 		
 		// find plugin sequence number
 		DBCollection collection = mongoDatabase.getCollection(PLUGIN_COLLECTION);
 		BasicDBObject query = new BasicDBObject("pluginId", pluginId);
 		DBObject result = collection.findOne(query);
 		Long pluginSeq = MongoHelper.readLong(result, "longId");
 		
 		// find raw au
 		DBCollection auCollection = mongoDatabase.getCollection(AUS_COLLECTION);
 		DBObject finalQuery = QueryBuilder.start().and(
                 QueryBuilder.start("pluginSeq").is(pluginSeq).get(),
                 QueryBuilder.start("auKey").is(auId).get()).get();
 		DBObject auTarget = auCollection.findOne(finalQuery);
 		
 		// get the list of metadata
 		BasicDBList articleMetadata = (BasicDBList) auTarget.get("articleMetadata");
 		Iterator<Object> it = articleMetadata.iterator();
 
         while (it.hasNext()) {
             DBObject obj = (DBObject) it.next();
 
             Gson gson = new Gson();
             ArticleMetadataInfo metadataJson = gson.fromJson(obj.toString(), ArticleMetadataInfo.class);
             
             article = model.createResource(metadataJson.accessUrl);
 
             storeMap(obj.toMap());       
         }
 
         //SimpleSelector Example for querying
         Property propertyForQuery = model.getProperty("_diffrn_radiation_monochromator");
         StmtIterator iter = model.listStatements(
                 new SimpleSelector(null, propertyForQuery, (RDFNode) null) {
                     public boolean selects(Statement s) {
                         return s.getString().endsWith("ite");
                     }
                 });
         
         log.info("Found Results!! - " + iter.toList().size());

        //QueryFactory Example for querying 
		String queryString = "SELECT * WHERE { ?o ?p ?s . FILTER (contains(?s, 'New Guy')) }";
 		Query qery = QueryFactory.create(queryString);
 		
 		QueryExecution qexec = QueryExecutionFactory.create(qery, model);
 
         try {
             ResultSet results = qexec.execSelect();
             for (; results.hasNext(); ) {
                 QuerySolution soln = results.nextSolution();
                 RDFNode n = soln.get("o");
                 if (n.isLiteral()) {
                     log.info("" + ((Literal) n).getLexicalForm());
                 } else {
                     Resource r = (Resource) n;
                     log.info("" + r.getURI());
                 }
             }
 
         } finally {
             qexec.close();
         }
         dataset.close();
     }
 
 	/**
 	 * Store a Map's contents into Jena.
 	 * @param map The map whose contents should be stored.
 	 */
 	public void storeMap(Map<String, Object> map) {
 		for (Map.Entry<String, Object> entry : map.entrySet()) {
 			storeObject(entry.getKey(), entry.getValue());
 		}  
 	}
 	
 	/**
 	 * Store an Object in Jena's model.
 	 * @param key The key with which to reference the object.
 	 * @param value The Object itself
 	 */
 	public void storeObject(String key, Object value) {
     	if(value instanceof String) {
     		storeString(key, (String) value);
     	}
     	else if (value instanceof BasicDBObject) {
     		storeDBObject(key, (BasicDBObject) value);
     	}
     	else if (value instanceof BasicDBList) {  
     		storeList(key, (BasicDBList) value);
     	} 
     	else {
     		log.info("Unhandled Type: " + value.getClass().toString());
     	}
 	}
 	
 	/**
 	 * Store a DBObject in Jena's model
 	 * @param key The key with which to reference the object
 	 * @param value The DBObject itself
 	 */
 	public void storeDBObject(String key, BasicDBObject value) {
 		// TODO: What do we actually want to do with this key?
 		storeMap(value.toMap());
 	}
 	
 	/**
 	 * Store a BasicDBList in Jena's Model
 	 * @param key The key with which to reference the list
 	 * @param value The List itself
 	 */
 	public void storeList(String key, BasicDBList value) {
 		BasicDBList dbValue = (BasicDBList) value;
 		Iterator listIt = dbValue.iterator();
 		StringBuilder str = new StringBuilder();
 		
 		// let's not save information about an empty list
 		if(!listIt.hasNext()){
 			return;
 		}
 		
 		while(listIt.hasNext()) {
 			Object listObj = listIt.next();
 			if(!(listObj instanceof DBObject)){
 				str.append(listObj.toString() + ";");
 			}
 		}
 		
 		Property property = model.getProperty(key);
 		if (property == null) {
 			property = model.createProperty(cifPredicateBaseUri, key);
 		}
 
 		// we don't want the trailing semicolon
		article.addProperty(property, str.substring(0, str.length() - 1));	
 	}
 
 	/**
 	 * Store a string in Jena's model
 	 * @param key The key with which to reference the string
 	 * @param value The string itself
 	 */
 	public void storeString(String key, String value) {
 		Property property = model.getProperty(key);
         if (property == null) {
             property = model.createProperty(cifPredicateBaseUri, key);
         }
         article.addProperty(property, (String) value);
 	}
 }
