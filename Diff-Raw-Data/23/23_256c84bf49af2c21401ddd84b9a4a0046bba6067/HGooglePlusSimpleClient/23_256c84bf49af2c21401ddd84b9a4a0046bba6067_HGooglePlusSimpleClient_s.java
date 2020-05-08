 package org.hubiquitus.hubiquitus4j.HGooglePlusSimpleClient;
 
 
 
 import org.hubiquitus.hubiquitus4j.hgoogleplus.GPStatus;
 import org.hubiquitus.hubiquitus4j.hgoogleplus.HGooglePlus;
 import org.hubiquitus.hubiquitus4j.hgoogleplus.HGooglePlusListners;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 public class HGooglePlusSimpleClient implements HGooglePlusListners
 {
 	final static Logger log = LoggerFactory.getLogger(HGooglePlusSimpleClient.class);
 
     public void onStatus(GPStatus gps) {
     	log.info("----"+gps );
     	log.info("[GooglePlus] Recieved status :["+gps.getDisplayName()+"] --> [COUNT PLUS]: "+gps.getPlusOneCount()+" [Circled By Count]: "+gps.getCircledByCount());
 	}
     
 	public static void main( String[] args )
     {		
 		HGooglePlus googlePlusClient = new HGooglePlus(
					"192.168.102.84", 		              // yourProxyHost if any
					3128,			                      // yourProxyPort if any
 					"+Coca-cola",	                      // GooglePlus page name or Id page
 					15000,                                // On milliseconds refresh rate
				    "AIzaSyDpnjbOy1_ZLCoCj-yLg1sdphRiQ6QEvcQ"	//API Key
 				);		
 		googlePlusClient.addListener(new HGooglePlusSimpleClient());
 		googlePlusClient.start();
     }
 }
