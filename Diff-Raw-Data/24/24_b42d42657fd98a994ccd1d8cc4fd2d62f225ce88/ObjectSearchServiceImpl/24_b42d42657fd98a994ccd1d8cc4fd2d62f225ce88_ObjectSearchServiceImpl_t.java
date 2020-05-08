 package at.fakeroot.sepm.server;
 
 import java.util.ArrayList;
 
 import at.fakeroot.sepm.shared.client.serialize.BoundingBox;
 import at.fakeroot.sepm.shared.server.DBGeoObject;
 import at.fakeroot.sepm.shared.server.DBService;
 import at.fakeroot.sepm.shared.server.GeoObjectManager;
 import at.fakeroot.sepm.shared.client.serialize.ObjectDetails;
 import at.fakeroot.sepm.client.serialize.ObjectSearchService;
 import at.fakeroot.sepm.shared.client.serialize.SearchResult;
 import at.fakeroot.sepm.shared.server.ServiceManager;
 
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 
 /**
  * the class connects the GeoManager at the client with the DAO; it relays the users requests
 * and returns the text for the DetailView
  * @author RK
  */
 @SuppressWarnings("serial")
 public class ObjectSearchServiceImpl extends RemoteServiceServlet implements ObjectSearchService
 {
 	// a max. limit for the hits, which are shown on the map
 	private static final int limit = 50;
 	private GeoObjectManager geoObjManager;
 	private ServiceManager svcManager;

 	
 	//Constructor
 	public ObjectSearchServiceImpl()
 	{
 		geoObjManager = GeoObjectManager.getInstance();
 		svcManager = ServiceManager.getInstance();
 	}
 	
 	/**
 	 * prepares the users request for the DAO and forwards it
 	 * @param box BoundingBox the area, where the user searches
 	 * @param what String the users input for what he is searching
 	 * @return SearchResult the found GeoObjects together whith the total hits for the request
 	 */
 	public SearchResult search(BoundingBox box, String what)
 	{
 		//the input string is separated by ' '
 		String[] tags; 
 		if (what.length() > 0) tags = what.toLowerCase().split(" ");
 		else tags = new String[0];
 		return (geoObjManager.select(tags, box, limit)); 
 	}
 	/**
 	 * gets the entry for the 'BubbleHTML' from the table service and the properties from the 
 	 * specified object; it integrates the properties into the HTML and returns the HTML-String 
 	 * together with the tags for the object and the tags from the service
 	 * @param objId int identifies the Object, for which the detailHTML is needed
 	 * @return ObjectDetails HTML String with the tags for the object and service
 	 */
 	public ObjectDetails getDetailHTML(int objId)
 	{
 		DBGeoObject dbGeoObj = geoObjManager.select(new DBGeoObject(objId, null, 0, 0, 0, null, null, null, null, null));
 		DBService dbSvcObj = svcManager.select(dbGeoObj.getSvc_id());
 		String html = dbSvcObj.getBubbleHTML();
 		int begin = 0;
 		int end = 0;
 		String result = null;
 		ArrayList<String> tags = new ArrayList<String>();
 		for(int i = 0; i < html.length(); i++)
 		{
 			//the placeholder in the 'BubbleHTML' for the objects property-values is 
 			//structured %PROPERTY_NAME%
 			if(html.charAt(i) == '%')
 			{
 				//begin points at the first found '%'
 				begin = i;
 				//the boolean endFound is true when a second '%' is found
 				boolean endFound = false;
 				for(int j = i+1; j < html.length(); j++)
 				{
 					if(html.charAt(j) == '%')
 					{
 						//end points at the second '%'
 						end = j;
 						endFound = true;
 						break;
 					}
 				}
 				//endFound might be false if there is a regluar '%' character in the text 
 				if(!endFound)
 				{
 					break;
 				}
 				//the substring until the first '%' is added to the result string
 				result = html.substring(0, begin);
				//loops through all the properties for the current dbGeoObject
 				for(int x = 0; x < dbGeoObj.getProperties().length; x++)
 				{
					//checks whether there is a property-name equal to the substring between two '%' characters
 					if(html.substring(begin + 1, end).equals(dbGeoObj.getProperties()[x].getName()))
 					{
 						//adds value from the property-value to the result String
 						result += dbGeoObj.getProperties()[x].getValue();
 						//adds the rest of the bubbleHTML to the result String
 						result += html.substring(end+1);	  
 						html = result;
 						break;
 					}
 				}
 			}
 		}
 		//adds the tags from the dbGeoObject
 		for(int i = 0; i < dbGeoObj.getTags().length; i++)
 		{
 			tags.add(dbGeoObj.getTags()[i]);
 		}
 		//adds the tags from the dbService
 		for(int i = 0; i < dbSvcObj.getTags().length; i++)
 		{
 			tags.add(dbSvcObj.getTags()[i]); 
 		}
 		//returns the html for the object together with its tags
 		return (new ObjectDetails(result,tags.toArray(new String[tags.size()])));
 	}
 	
 }
