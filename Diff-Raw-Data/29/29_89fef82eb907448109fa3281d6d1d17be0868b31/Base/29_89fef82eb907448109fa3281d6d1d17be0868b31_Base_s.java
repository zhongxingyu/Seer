 package models;
 
 import java.io.IOException;
 import java.util.Collection;
 
 import javax.persistence.Column;
 import javax.persistence.Id;
 
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.map.ObjectMapper;
 
 import net.vz.mongodb.jackson.DBCursor;
 import net.vz.mongodb.jackson.DBQuery;
 import net.vz.mongodb.jackson.JacksonDBCollection;
 import net.vz.mongodb.jackson.ObjectId;
 import play.Logger;
 import play.data.format.Formats;
 import play.data.validation.Constraints;
 import play.db.ebean.Model;
 import play.modules.mongodb.jackson.MongoDB;
 
 //Base contains the base information for a landing page, may also be used for widgets
 public class Base extends Model {
 
 	@ObjectId @Id
     public String id;
 
     @Constraints.Required
     @Formats.NonEmpty
     @Column(unique = true)
     public String templateName;
     
     @Constraints.Required
     @Formats.NonEmpty
     public String siteId;
     
     public String bgColor;
     public String txtColor;
     
     public LayoutJSON layout;
     
     public static class LayoutJSON {
     	public String siteId;
     	public Collection<Containers> containers;
     	
 	    static class Containers  {  
 	    	public String id;
 	    	public Collection<Components> components;
 	    }  
 	      
 	    static class Components  {  
 	    	public Integer id;  
 	    	public String className;
 	    	public String width;
 	    } 
 	    //used to prevent jackson error
 	    public LayoutJSON() {
 	    	
 	    }
     }
     
     protected static ObjectMapper mapper = new ObjectMapper();
     
     public static JacksonDBCollection<Base, Object> coll = MongoDB.getCollection("sites", Base.class, Object.class);
     
     public static Base findById(String id) {
     	Logger.debug("findbyid in model base");
         return coll.findOne(DBQuery.is("id", id));
     }
     
     public static Base findBySiteId(String siteId) {
     	Logger.debug("findbysiteid in model base");
     	return coll.findOne(DBQuery.is("siteId", siteId));
     }
     
     public static boolean save(Base base) {
     	if (base == null) {
           return false;
         } else {
         	DBCursor<Base> cursor = coll.find().is("siteId", base.siteId);
         	if (cursor.hasNext()) {
         	    coll.update(cursor.next(), base);
             	return true;
         	} else {
         		Logger.debug("no cursor next in base save model");
         		coll.save(base);
         		return true;
         	}
         }
     }
     
     public static LayoutJSON parseContainer(JsonNode containerNode) {
 		try{
 			LayoutJSON mappedContainers = mapper.treeToValue(containerNode, LayoutJSON.class);
 			return mappedContainers;
 		}catch(IOException ioe){
 			Logger.debug("Exception saving containers:");
 			Logger.debug(ioe.toString());
 			return null;
 		}
     }
     
     // need to rename returned container
     public static boolean saveLayout(LayoutJSON passedLayout) {
     	DBCursor<Base> cursor = coll.find().is("siteId", passedLayout.siteId);
     	while (cursor.hasNext()) {
     		Base dbInsertObj = cursor.next();
     		dbInsertObj.layout = passedLayout;
     		coll.save(dbInsertObj);
         	return true;
    	} //else {
//    		Logger.debug("no cursor next in saveLayout model, meaning no site found");
//    		return false;
//    	}
     	return false;
     }
     
     public static Base siteSearch(String siteId) {
     	DBCursor<Base> cursor = coll.find(DBQuery.is("siteId", siteId));
 		Base returnSiteInfo = new Base();
 			while (cursor.hasNext()) {
 	    		Base dbInsertObj = cursor.next();
 	    		returnSiteInfo.siteId = dbInsertObj.siteId;
 	    		returnSiteInfo.templateName = dbInsertObj.templateName;
 	    		returnSiteInfo.bgColor = dbInsertObj.bgColor;
 	    		returnSiteInfo.txtColor = dbInsertObj.txtColor;
 	        	Logger.debug("found site in sitesearch");
     		}
     		return returnSiteInfo;
     }
     
     public static Base loadLayout(String siteId) {
     	DBCursor<Base> cursor = coll.find(DBQuery.is("siteId", siteId));
    	return cursor.next();
//    	if (cursor.hasNext()) {
////    		Base dbReturnObj = cursor.next();
//        	return cursor.next();
//    	} else {
//    		Logger.debug("no cursor next in loadLayout model");
//    		return null;
//    	}

     }    
 }
