 package com.itllp.barleylegalhomebrewers.ontap;
 
 import com.itllp.barleylegalhomebrewers.ontap.dateconverter.JsonDateToJavaDate;
 import com.itllp.barleylegalhomebrewers.ontap.dateconverter.StringToJavaDateConverter;
 import com.itllp.barleylegalhomebrewers.ontap.json.JsonArrayRetriever;
 import com.itllp.barleylegalhomebrewers.ontap.json.JsonArrayRetrieverImpl;
 import com.itllp.barleylegalhomebrewers.ontap.json.JsonUrlEventDatabaseLoader;
 
 
 public class EventDatabaseLoaderFactory {
 
 	public static final String betaSiteUrl = "http://misdb.com/barleylegalapp/getevent.aspx";
	public static final String productionSiteUrl = "http://barleylegalevents.com/barleylegal/getevent.aspx";
 
 	private static void createEventDatabaseLoader(
 			NetworkConnectivity networkConnectivity, String url) {
 		EventDatabase eventDatabase = EventDatabase.getInstance();
 		StringToJavaDateConverter dateConverter = new JsonDateToJavaDate(); 
 		JsonArrayEventDatabaseLoader arrayLoader = 
 				new JsonArrayEventDatabaseLoaderImpl(dateConverter, eventDatabase);
 		JsonArrayRetriever arrayRetriever = new JsonArrayRetrieverImpl();
 		JsonUrlEventDatabaseLoader.create(networkConnectivity, url, 
 				arrayRetriever, arrayLoader);
 	}
 	
 	public static void createBetaSiteEventDatabaseLoader(
 			NetworkConnectivity networkConnectivity) {
 		createEventDatabaseLoader(networkConnectivity, betaSiteUrl);
 	}
 
 	public static void createProductionSiteEventDatabaseLoader(
 			NetworkConnectivity networkConnectivity) {
 		createEventDatabaseLoader(networkConnectivity, productionSiteUrl);
 	}
 }
