 package at.fakeroot.sepm.crawler;
 import org.json.HTTP;
 import org.json.HTTPTokener;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONString;
 
 import at.fakeroot.sepm.shared.client.serialize.BoundingBox;
 import at.fakeroot.sepm.shared.server.DBGeoObject;
 import at.fakeroot.sepm.shared.server.Property;
 
 
 
 public class LocrCrawler extends ICrawler  {
 
 	public LocrCrawler()  {
		super(new BoundingBox(10.0,10,80.0,80.0),11, "wiki_de");
 		beginCrawlin();
 	}
 
 	private void beginCrawlin() {
 		//while(true){
 		
 		for (int i = 0; i < 4; i++) {
 			BoundingBox curBox = nextCrawlBox();
 			String url = "http://de.locr.com/api/get_photos_json.php"
 					+ "?latitudemin=" + curBox.getX1() + 
 					"&longitudemin=" + curBox.getY1() + 
 					"&latitudemax=" + curBox.getX2()
 					+ "&longitudemax=" + curBox.getY2()
 					+ "&category=popularity" + "&locr=true";
 
 			//System.out.println(curBox);
 			System.out.println(url);
 			//System.out.println(crawl(url));
 
 			try {
 				String[] tabs= {"tag1","tag2"};
 			    Property[] myProperty = new Property[1];
 			    myProperty[0]= new Property("proName","proValue");
 			    
 			    
 				JSONObject myJSONObject = new JSONObject(crawl(url));
 				//System.out.println("Parse: "+myJSONObject.getJSONArray("photos"));
 				JSONArray myArray = myJSONObject.getJSONArray("photos");
 				DBGeoObject[] saveDBArray = new DBGeoObject[myArray.length()];
 				for(int k=0;k<myArray.length();k++){
 					System.out.println("id: "+(((JSONObject)myArray.get(k)).get("photo_id")));
 					
 					//Integer.parseInt((((JSONObject)myArray.get(k)).get("photo_id").toString()));
 					saveDBArray[k]=new DBGeoObject(
 							Integer.parseInt((((JSONObject)myArray.get(k)).get("photo_id").toString())),
 							"Client1",
 							Double.parseDouble((((JSONObject)myArray.get(k)).get("latitude").toString())),
 							Double.parseDouble((((JSONObject)myArray.get(k)).get("longitude").toString())),
 							getSvcID(),
 							"uniqu",
 							"link",
 							"valid",myProperty,tabs);
 					
 				}
 				
 				saveObject(saveDBArray);
 				
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 			
 			
 			
 			
 		}
 		
 	}
 }
