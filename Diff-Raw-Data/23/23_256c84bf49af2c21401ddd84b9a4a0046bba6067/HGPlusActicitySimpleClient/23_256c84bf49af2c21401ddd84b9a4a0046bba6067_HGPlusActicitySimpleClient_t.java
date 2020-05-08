 package org.hubiquitus.hubiquitus4j.HGooglePlusSimpleClient;
 
 
 
 import org.hubiquitus.hubiquitus4j.hgoogleplus.GPActivity;
 import org.hubiquitus.hubiquitus4j.hgoogleplus.HGooglePlusActivity;
 import org.hubiquitus.hubiquitus4j.hgoogleplus.HGooglePlusActivityListners;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 public class HGPlusActicitySimpleClient implements HGooglePlusActivityListners
 {
 	final static Logger log = LoggerFactory.getLogger(HGPlusActicitySimpleClient.class);
 
 	@Override
 	public void onStatusActivity(GPActivity gpActivity) {
 		log.info("[GooglePlus Activity] : "+gpActivity.getItems() );
 		
 	}
    
 	
 	public static void main( String[] args )
     {		
 		HGooglePlusActivity gPClientActivity = new HGooglePlusActivity(
					"", 		                   // yourProxyHost if any
					0,			                           // yourProxyPort if any
 					"obama",	                               // GooglePlus page name or Id page
 					 null,                                     //langage
 					 20,                                       //max results
 					null,	                                   // orderBy ie 'best' 
 					12000,                                      // On milliseconds refresh rate
				    ""  //API Key
 				);		
 		gPClientActivity.addListener(new HGPlusActicitySimpleClient());
 		gPClientActivity.start();
     }
 
 	
 }
