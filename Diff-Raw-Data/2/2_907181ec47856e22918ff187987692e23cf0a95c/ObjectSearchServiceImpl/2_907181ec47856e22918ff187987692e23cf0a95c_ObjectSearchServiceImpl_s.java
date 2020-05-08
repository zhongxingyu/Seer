 package at.fakeroot.sepm.server;
 
 import java.util.ArrayList;
 
 import at.fakeroot.sepm.client.serialize.BoundingBox;
 import at.fakeroot.sepm.client.serialize.ClientGeoObject;
 import at.fakeroot.sepm.client.serialize.ObjectSearchService;
 import at.fakeroot.sepm.client.serialize.SearchResult;
 
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 
 @SuppressWarnings("serial")
 public class ObjectSearchServiceImpl extends RemoteServiceServlet implements ObjectSearchService
 {
 
 	private static final int limit = 50;
 	// private GeoObjectManager geoObjManager; (nach MR2)
 	private ArrayList<ClientGeoObject> fakeDB;
 	
 	public ObjectSearchServiceImpl()
 	{
 		//geoObjManager = GeoObjectManager.getInstance(); (nach MR2)
 		fakeDB = new ArrayList<ClientGeoObject>();
 		fakeDB.add(new ClientGeoObject(1, "Karlskirche", null, new String[] {"karlskirche","kirche", "karlsplatz"}, 48.198247, 16.371422));
 		fakeDB.add(new ClientGeoObject(2, "Stephansdom", null, new String[] {"stephansdom","kirche", "stephansplatz", "wahrzeichen", "wien"}, 48.208333, 16.372778));
 		fakeDB.add(new ClientGeoObject(3, "Schloss Schönbrunn", null, new String[] {"schloss","schönbrunn", "schlosspark", "neptunbrunnen"}, 48.184517, 16.311864));				
 		fakeDB.add(new ClientGeoObject(4, "Goldenes Dachl", null, new String[] {"goldenes","dachl", "innsbruck", "wahrzeichen"}, 47.268583, 11.393264));
 		fakeDB.add(new ClientGeoObject(5, "Festung Hohensalzburg", null, new String[] {"festung","hohensalzburg", "salzburg", "wahrzeichen", "burg"}, 47.794967, 13.047256));
 		fakeDB.add(new ClientGeoObject(6, "Wallfahrtsbasilika Pöstlingberg", null, new String[] {"wallfahrtsbasilika","pöstlingberg", "linz", "wahrzeichen"}, 48.323889, 14.258333));
 		fakeDB.add(new ClientGeoObject(7, "Uhrturm", null, new String[] {"uhrturm", "turm", "graz", "wahrzeichen"}, 47.075463, 15.436746));
 		fakeDB.add(new ClientGeoObject(8, "Martinsturm", null, new String[] {"martinsturm", "turm", "bregenz", "wahrzeichen"}, 47.505, 9.749167));
 		fakeDB.add(new ClientGeoObject(9, "Schloss Esterhazy ", null, new String[] {"schloss","esterhazy ", "eisenstadt", "wahrzeichen"}, 47.848611, 16.520833));
 		fakeDB.add(new ClientGeoObject(10, "Lindwurm", null, new String[] {"lindwurm","klagenfurt", "wahrzeichen"}, 46.623997, 14.3077));
 		fakeDB.add(new ClientGeoObject(11, "Rathaus", null, new String[] {"rathaus","sankt", "st.", "pölten", "wahrzeichen"}, 48.2, 15.616667));
 	}
 	
 	public SearchResult search(BoundingBox box, String what)
 	{
 		SearchResult result = new SearchResult();
 		String[] tags = what.toLowerCase().split(" ");
 
 		int counter = 0;
 		
 		for(ClientGeoObject cgo : fakeDB)
 		{
			if(cgo.getXPos() >= box.getX1() && cgo.getYPos() >= box.getY1() && cgo.getXPos() <= box.getX2()
 					//&& cgo.getYPos() <= box.getY2())
 			{
 				
 				boolean matches = true;
 				for(int i=0; i < tags.length; i++)
 				{
 					boolean tagMatch = false;
 					String[] cgoTags = cgo.getTags();
 					for(int j = 0; j < cgoTags.length; j++)
 					{
 						if(tags[i].equals(cgoTags[j]))
 						{
 							tagMatch = true;
 							break;
 						}		
 					}
 					if(tagMatch == false)
 					{
 						matches = false;
 						break;
 					}
 				}
 				if(matches == true)
 				{
 					result.addResultToList(cgo);
 					counter++;
 				}
 			}
 		}
 		result.setResultCount(counter);
 		return result;
 		//return (geoObjManager.select(tags, box, limit)); (nach MR2)
 		
 	}
 	
 }
