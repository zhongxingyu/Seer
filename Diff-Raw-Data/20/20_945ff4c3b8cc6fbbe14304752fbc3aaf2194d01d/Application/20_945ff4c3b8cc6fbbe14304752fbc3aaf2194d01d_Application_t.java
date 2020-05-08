 package controllers;
 
 import java.lang.reflect.Type;
 import java.util.Iterator;
 
 import javax.inject.Inject;
 import javax.xml.datatype.DatatypeConfigurationException;
 import javax.xml.datatype.DatatypeFactory;
 import javax.xml.datatype.XMLGregorianCalendar;
 
 import org.apache.commons.lang.StringUtils;
 import org.bson.types.ObjectId;
 import org.uniprot.uniprot.Entry;
 import org.uniprot.uniprot.GeneNameType;
 
 import play.CorePlugin;
 import play.cache.CacheFor;
 import play.mvc.Before;
 import play.mvc.Controller;
import play.mvc.Finally;
 import play.mvc.Util;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gson.JsonDeserializationContext;
 import com.google.gson.JsonDeserializer;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonParseException;
 import com.mongodb.BasicDBList;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 
 public class Application extends Controller {
 
 	@Inject
 	static DB db;	
 	
 	
	@Before static void mongoBegin() {  db.requestStart(); }
	
	@Finally static void mongoDone() {  db.requestDone(); }
 	
 	
 	/**
 	 * Render the project index page 
 	 */
     @CacheFor("5min")
     public static void index() {
     	/* Lookup data */
     	DBCollection coll = db.getCollection("leish_species");
         
 		DBObject filterBy = new BasicDBObject();
 		filterBy .put("value", new BasicDBObject("$gt", 20));
     	DBCursor species = coll.find(filterBy);
     	
     	/* Render page */
     	render(species);
     }
 
     
     public static void description() { 
     	render();
     }
 
     public static void contacts() { 
     	render();
     }
  
 
     public static void db() { 
     	render();
     }
     
     public static void entry() {  
     	lookupItem();
     }
     
     /**
      * Render the page contains details data for the selected item 
      */
     public static void item() { 
     	lookupItem();
     }   
 
     @Util
     static void lookupItem() { 
     	DBCollection data = db.getCollection("leishdata");
     	
     	DBObject where=null;
     	if( StringUtils.isNotEmpty(params.get("id")) ) { 
     		where = new BasicDBObject("_id", new ObjectId(params.get("id")));
     	}
     	else if(StringUtils.isNotEmpty(params.get("accession"))) { 
     		where = new BasicDBObject("accession", params.get("accession"));
     	}
     	else if(StringUtils.isNotEmpty(params.get("name"))) { 
     		where = new BasicDBObject("name", params.get("name"));
     	}
     	
     	DBObject obj = data.findOne(where);
     	GsonBuilder builder = new GsonBuilder();
     	builder.registerTypeAdapter(XMLGregorianCalendar.class, new XMLGregorianCalendarDeserializer());
     	Gson gson = builder.create();
     	Entry entry = gson.fromJson( obj.toString(), Entry.class);
 
     	/*
     	 * extract all 'gene.name.value' from the unitprot entry 
     	 */
     	BasicDBList refs = new BasicDBList();
     	Iterator<Entry.Gene> geneIt = entry != null && entry.getGene() != null ? entry.getGene().iterator() : null;
     	Iterator<GeneNameType> nameIt;
     	while( geneIt != null && geneIt.hasNext()  ) { 
     		Entry.Gene gene = geneIt.next();
     		nameIt = gene!=null && gene.getName() != null ? gene.getName().iterator() : null;
     		while( nameIt != null && (nameIt).hasNext() ) { 
     			GeneNameType name = nameIt.next();
     			refs.add( name.getValue() );
     		}
     	}
     	
     	/*
     	 * lookup all leishdrub features for the found 'refid'
     	 */
     	DBCursor features=null;
     	if( refs.size()>0 ) { 
         	DBCollection leish = db.getCollection("leishcoll");
         	DBObject filter = new BasicDBObject("refid", new BasicDBObject("$in", refs));
         	features = leish.find(filter);
     	}
 
     	render(entry, features);  	
     }
     
     /**
      * Deserializer for date time object 
      * 
      * @author Paolo Di Tommaso
      *
      */
     
     static class XMLGregorianCalendarDeserializer implements JsonDeserializer<XMLGregorianCalendar> {
 
     	static DatatypeFactory factory;
     	
     	static { 
     		try {
 				factory =  DatatypeFactory.newInstance();
 			} catch (DatatypeConfigurationException e) {
 				throw new RuntimeException(e);
 			}
     	}
     	
     	public XMLGregorianCalendar deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
     		
     		int year = json.getAsJsonObject().get("year").getAsInt();
     		int month = json.getAsJsonObject().get("month").getAsInt();
     		int day = json.getAsJsonObject().get("day").getAsInt();
     		int timezone = json.getAsJsonObject().get("timezone").getAsInt();
     		int hour = json.getAsJsonObject().get("hour").getAsInt();
     		int minute = json.getAsJsonObject().get("minute").getAsInt();
     		int second = json.getAsJsonObject().get("second").getAsInt();
 
     		return factory.newXMLGregorianCalendar(year, month, day, hour, minute, second, 0, timezone);
     	  }
 
     }
     
 	/**
 	 * Renders a page containing Play! runtime informantion 
 	 */
 	public static void playinfo() {
 		String info = CorePlugin.computeApplicationStatus(false);
 		render(info);
 	} 
 		
 	public static void chunk(String echo) { 
     	response.contentType = "text/csv";
     	response.setHeader("Transfer-Encoding", "chunked");
     	response.writeChunk(echo);
 	}
 	
 	public static void any(String page) { 
 		render(String.format("Test/%s.html", page));
 	}
     
 }
