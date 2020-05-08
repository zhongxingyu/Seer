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
     public String bundleId;
     
     public String bgColor;
     public String txtColor;
     public String clientURL;
     public String googleAnalytics;
     
     public LayoutJSON layout;
     
     public static class LayoutJSON {
     	public String bundleId;
     	public Collection<Containers> containers;
     	
 	    static class Containers  {  
 	    	public String id;
 	    	public String className;
 //	    	public Collection<Components> components;
 	    	public Component component;
 	    }  
 	    static class Component {
 	    	public String text;
 	    	public String type;
 	    	public String showImage;
 	    	public String showWidget;
 	    	public String showText;
 	    	public String showImageWidget;
 	    	public String showImageText;
 	    	public String showHotel;
 	    	public String showDeal;
 	    	public String showAttraction;
 	    	public String width;
 	    	public String align;
 	    	public boolean layered;
 	    	public Collection<Tabs> tabs;
 	    }
 	    
 	    static class Tabs {
 	    	public String id;
 	    	public String order;
 	    	public boolean active;
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
     
     public static Base findByBundleId(String bundleId) {
     	Logger.debug("findbybundleId in model base");
     	return coll.findOne(DBQuery.is("bundleId", bundleId));
     }
 
     public static boolean save(Base base) {
     	if (base == null) {
           return false;
         } else {
         	DBCursor<Base> cursor = coll.find().is("bundleId", base.bundleId);
         	if (cursor.hasNext()) {
         	    coll.update(cursor.next(), base);
         	} else {
         		Logger.debug("no cursor next in base save model");
         		coll.save(base);
         	}
     		return true;
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
     	DBCursor<Base> cursor = coll.find().is("bundleId", passedLayout.bundleId);
     	while (cursor.hasNext()) {
     		Base dbInsertObj = cursor.next();
     		dbInsertObj.layout = passedLayout;
     		coll.save(dbInsertObj);
         	return true;
     	} 
     	return false;
     }
     
     public static Base siteSearch(String bundleId) {
     	DBCursor<Base> cursor = coll.find(DBQuery.is("bundleId", bundleId));
 		Base returnSiteInfo = new Base();
 			while (cursor.hasNext()) {
 	    		Base dbInsertObj = cursor.next();
 	    		returnSiteInfo.bundleId = dbInsertObj.bundleId;
 	    		returnSiteInfo.templateName = dbInsertObj.templateName;
 	    		returnSiteInfo.bgColor = dbInsertObj.bgColor;
 	    		returnSiteInfo.txtColor = dbInsertObj.txtColor;
 	    		returnSiteInfo.clientURL = dbInsertObj.clientURL;
 	    		returnSiteInfo.googleAnalytics = dbInsertObj.googleAnalytics;
 	        	Logger.debug("found site in sitesearch");
     		}
     		return returnSiteInfo;
     }
     
     public static Base loadLayout(String bundleId) {
     	DBCursor<Base> cursor = coll.find(DBQuery.is("bundleId", bundleId));
     	Base base = new Base();
     	while(cursor.hasNext()) {
     		return cursor.next();
     	}
     	return base;
     }    
 }
