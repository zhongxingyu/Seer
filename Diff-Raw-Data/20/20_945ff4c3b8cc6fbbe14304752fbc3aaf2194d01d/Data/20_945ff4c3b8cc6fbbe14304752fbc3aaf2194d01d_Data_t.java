 package controllers;
 
 import static util.Dsl.*;
 
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 import javax.inject.Inject;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.math.NumberUtils;
 import org.bson.types.ObjectId;
 
 import play.Logger;
 import play.Play;
 import play.cache.Cache;
 import play.cache.CacheFor;
 import play.mvc.Before;
 import play.mvc.Controller;
import play.mvc.Finally;
 import play.mvc.Http.Request;
 import play.mvc.Scope.Session;
 import play.mvc.Util;
 import util.MongoHelper;
 import util.ServerExtensions;
 
 import com.mongodb.BasicDBList;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 import com.mongodb.util.JSON;
 
 /**
  * Expose JSON data provider for database and filter controls 
  * 
  * @author Paolo Di Tommaso
  *
  */
 public class Data extends Controller {
 
 	static int MAX_CHUNK_CAPACITY = 500 * 1024;
 	
 	static final Map<String,String> mapping = 	
 		treemap( entry("accession","accession"),
 			 entry("name", "name"),
 			 entry("protein", "protein.submittedName.fullName.value"),
 			 entry("gene","gene.name.value"),
 			 entry("organism","organism.name.value"),
 			 entry("length", "sequence.length")
 			 );
 
 
 	@Inject
 	static DB db;
 
 	
 	@Before(unless={"downloadCsv", "downloadFasta"})
 	static void before() { 
 		request.format = "json";
 		response.contentType = "application/x-json";
 	}

	@Before static void mongoBegin() {  db.requestStart(); }
	
	@Finally static void mongoDone() {  db.requestDone(); }
 	
     /**
      * The main 'data' extraction method. The following HTTP parameters are supported 
      * <li>start: the record index number from which start the visualization</li>
      * <li>limit</li>
      * <li>dir</li>
      * <li>sort</li>
      * <li>filter</li>
      * <li>group</li>
      * <li>page</li>
      * 
      */
     public static void db() {
 
     	final String callback = request.params.get("callback");
     	final boolean jsonp = StringUtils.isNotEmpty(callback);
 
         String directionParam = request.params.get("dir");
         String filterParam = request.params.get("filter");
         String groupParam = request.params.get("group");
         String limitParam = request.params.get("limit");
         String pageParam = request.params.get("page");
         String sortParam = request.params.get("sort");
         String startParam = request.params.get("start");
 
     	DBCollection coll = db.getCollection("leishdata");
     	if( coll == null ) { 
     		error("Missing collection 'leishdata'");
     	}
     	
     	int skip = NumberUtils.toInt(startParam);
     	int limit = NumberUtils.toInt(limitParam, 200);
 
         DBObject filterBy = decodeFilter(filterParam);
         Logger.debug("Data filter: %s", filterBy);
         
         cacheFilter( filterBy );
         
     	/* 
     	 * get the 'general' cursor 
     	 */
     	DBCursor cursor = coll.find(filterBy);
     	// count the total documents in the collections 
     	long total = cursor.count();
    	
     	/* 
     	 * check if a sorting is defined 
     	 */
     	DBObject orderBy = null;
     	if( StringUtils.isNotEmpty(sortParam) ) { 
     		sortParam = mapping.get(sortParam);
     	}
     	if( StringUtils.isNotEmpty(sortParam) ) { 
     		orderBy = new BasicDBObject();
     		int direction = "DESC".equals(directionParam.toUpperCase()) ? -1 : 1;
     		orderBy.put(sortParam, direction);
         	cursor.sort(orderBy);
     	}
 
 		Logger.debug("Data Start: %s; Limit: %s; %s ", skip, limit, (orderBy != null ? "Sort by: "+orderBy : ""));
 
 		/* 
 		 * impose 'skip' and 'limit' to the cursor
 		 */
     	cursor.skip(skip).limit(limit);
     	
     	/*
     	 * create the JSON result data
     	 */
     	StringBuffer result = new StringBuffer();
     	
     	result.append("{");
     	result.append("\"total\":") .append( total ) .append(", ");
     	
     	result.append("\"entries\":");
     	
     	result.append("[");
     	int i=0;
 		DBObject target = new BasicDBObject();
     	while( cursor.hasNext() ) { 
     		// fetch the next document 
     		DBObject item = cursor.next();
     		
     		// add a comma to json result after the first record 
     		if( i++ > 0 ) { result.append(","); }
 
     		// extract the document id
     		target.put("_id", ((ObjectId)item.get("_id")).toString() );
         	
     		// remap the document to plain representation for table presentation
         	for( java.util.Map.Entry<String, String> map : mapping.entrySet() ) { 
         		target.put( map.getKey(), MongoHelper.select(item, map.getValue()) );
         	}
 		
     		result.append( target.toString() );
     	}
     	result.append("]}");
     	
     	/* 
     	 * render back the JSON result 
     	 */
     	if( jsonp ) { 
         	response.contentType = "text/javascript";
         	renderText( callback + "(" + result.toString() + ")");
     	}
     	else { 
         	response.contentType = "application/x-json";
         	renderText(result.toString());
     	}
     } 
     
     private static void cacheFilter(DBObject filterBy) {
     	final String key = "data_filter_" + Session.current().getId();
     	if( filterBy == null ) { 
     		Cache.delete(key);
     	}
     	else { 
     		Cache.set(key, filterBy);
     	}
 	}
     
     private static DBObject cachedFilter( ) { 
     	final String key = "data_filter_" + Session.current().getId();
     	return (DBObject) Cache.get(key);
     }
 
 	@Util
     static void addFilter(DBObject filterBy, Request request, String key) {
     	if( StringUtils.isNotEmpty(request.params.get(key))) { 
         	filterBy.put(key, request.params.get(key));
         }
 	}
 
 	@Util
     static DBObject decodeFilter( String filterString ) { 
         if( StringUtils.isEmpty(filterString) ) {
         	return null;
         }
     	
         DBObject result = new BasicDBObject();
 		Logger.debug("Data Filter: %s", filterString);
 		BasicDBList filters = (BasicDBList) JSON.parse(filterString);
 		for( int i=0; i<filters.size(); i++ ) { 
 			DBObject item = (DBObject) filters.get(i);
 			String key = (String) item.get("property");
 			
 			
 			Object value = item.get("value");
 			if( value instanceof BasicDBList ) {
 				if( !((BasicDBList)value).isEmpty() ) { 
 	 				result.put(key, new BasicDBObject("$in", value));
 				}
 			}
 			else if( value != null && StringUtils.isNotEmpty(value.toString())) { 
 				result.put(key, value);
 			}
 		}
 
 		
 		return result;
     }
         
     
     
     /**
      * Provide the table data to the showed in the index page
      */
     @CacheFor("5min")
     public static void species(Integer minCount) { 
     	
     	DBCollection coll = db.getCollection("leish_species");
     
     	DBObject filterBy = null;
     	if( minCount != null ) { 
     		filterBy = new BasicDBObject();
     		filterBy .put("value", new BasicDBObject("$gt", minCount-1));
     	}
     	
     	DBCursor species = coll.find(filterBy);
     	long total = species.count();
     	render(total, species);
     }   
     
     @CacheFor("5min")
     public static void dbTypes() { 
 
     	DBCollection coll = db.getCollection("leishdata");
     	List result = coll.distinct("dbReference.type");
     	Collections.sort(result);
     	long total = result.size();
     	
     	render(total, result);
     }
 
     @CacheFor("5min")
     public static void featureTypes() { 
 
     	DBCollection coll = db.getCollection("leishdata");
     	List result = coll.distinct("feature.type");
     	Collections.sort(result);
     	long total = result.size();
     	
     	render(total, result);
     }
 
     @CacheFor("5min")
     public static void featureDescriptions(String type) { 
     	Logger.info("FeatureDescription: %s", type);
     	
     	String sFilter = params.get("filter");
         
     	DBObject filterBy = null;
     	if( StringUtils.isNotEmpty(type)) { 
     		filterBy = new BasicDBObject();
     		filterBy .put("feature.type", type);
     	}  	
     	else if( StringUtils.isNotEmpty(sFilter) ) { 
     		filterBy = decodeFilter(sFilter);
     	}
     	
     	DBCollection coll = db.getCollection("leishdata");
     	List result = coll.distinct("feature.description", filterBy);
     	Collections.sort(result);
     	long total = result.size();
     	
     	render(total, result);
     }
    
         
     public static void item(String id) { 
     	DBCollection data = db.getCollection("leishdata");
     	DBObject obj = data.findOne( new BasicDBObject("_id", new ObjectId(id)));
     	renderText(obj.toString());
     }
     
 
     /**
      * Download selected sequences in CSV format 
      */
     public static void downloadCsv() { 
     	DBCollection data = db.getCollection("leishdata");
     	DBCursor cursor = data.find(cachedFilter());
     	
     	response.contentType = "text/csv";
     	response.setHeader("Transfer-Encoding", "chunked");
 		response.setHeader("Content-Disposition", "attachment; filename=\"leishdb.csv\"");
     	
     	StringBuilder chunk = new StringBuilder(50 * 1024);
 
     	//* write the header 
     	long c=0;
     	for( java.util.Map.Entry<String, String> map : mapping.entrySet() ) { 
     		if( c++> 0 ) chunk.append(",");
     		chunk.append( map.getKey() );
     	}
     	chunk.append("\n");
     	
     	// Append the data 
         while( cursor.hasNext() ) {
         	DBObject item = cursor.next();
         	
         	c=0;
         	for( java.util.Map.Entry<String, String> map : mapping.entrySet() ) { 
         		if( c++>0 ) chunk.append(",");
         		Object value = MongoHelper.select(item, map.getValue()) ;
         		chunk.append( value != null ? value.toString() : "" );
         	}
         	chunk.append("\n");
     	
         	if( chunk.capacity() > MAX_CHUNK_CAPACITY  ) { 
         		response.writeChunk(chunk.toString());
         		chunk = new StringBuilder();
         		/*
         		 * !!! ONLY IN DEV MODE exit on the first chunk !!! 
         		 */
         		if( Play.mode.isDev()) { 
         			return;
         		}
         	}
         }  	
 
         // write out the remaing part 
         if( chunk.length()>0 ) { 
             response.writeChunk(chunk);
         }
 
     }
     
     /**
      * Download the selected sequences in FASTA format
      */
     public static void downloadFasta() { 
     
        	DBCollection data = db.getCollection("leishdata");
     	DBCursor cursor = data.find(cachedFilter());
     	
     	response.contentType = "text/csv";
     	response.setHeader("Transfer-Encoding", "chunked");
 		response.setHeader("Content-Disposition", "attachment; filename=\"leishdb.fa\"");
     	
     	StringBuilder chunk = new StringBuilder(50 * 1024);
 
     	// Append the data 
         while( cursor.hasNext() ) {
         	DBObject item = cursor.next();
         	
         	chunk.append(">") .append(MongoHelper.select(item, "accession") );
         	chunk.append("\n");
         	
         	String seq = MongoHelper.select(item, "sequence.value");
         	chunk.append( ServerExtensions.seqfmt(seq, 1, 60) );
         	chunk.append("\n");
 
         	if( chunk.capacity() > MAX_CHUNK_CAPACITY) { 
         		response.writeChunk(chunk.toString());
         		chunk = new StringBuilder();
         		/*
         		 * !!! ONLY IN DEV MODE exit on the first chunk !!! 
         		 */
         		if( Play.mode.isDev()) { 
         			return;
         		}
         	}
         }  	
 
         // write out the remaing part 
         if( chunk.length()>0 ) { 
             response.writeChunk(chunk);
         }
    	
     }
     
     
     public static void omnisearch(String term) { 
     	DBCollection index = db.getCollection("bigindex");
     	/*
     	 * Best option, we have just one value for the specified value
     	 */
     	BasicDBList data = new BasicDBList();
     	DBObject view = new BasicDBObject();
     	view.put("value", 1);
     	view.put("field", 1);
     	view.put("_id", 0);
     	
     	DBObject item = index.findOne(new BasicDBObject("value", term), view);
     	
     	if( item != null ) { 
     		data.add(item);
     	}
     	else { 
     		term = "^" + term; 
     		DBObject regex = new BasicDBObject();
     		regex.put("$regex",term);
     		regex.put("$options","i");
     		DBObject ref = new BasicDBObject("value", regex);
         	DBCursor cursor = index.find(ref, view);
         	int c=0;
         	while( cursor.hasNext() ) { 
         		item = cursor.next();
         		data.add(item);
         		if( c++ > 100 ) { 
         			Logger.warn("Omni search found too much records for term: '%s'. Returning first 100", term);
         			//TODO add a warning on client side
         			break;
         		}
         	}
     	}
     	
     	DBObject result = new BasicDBObject();
     	result.put("data", data);
     	renderText( data.toString() );
     }
     
 
 }
