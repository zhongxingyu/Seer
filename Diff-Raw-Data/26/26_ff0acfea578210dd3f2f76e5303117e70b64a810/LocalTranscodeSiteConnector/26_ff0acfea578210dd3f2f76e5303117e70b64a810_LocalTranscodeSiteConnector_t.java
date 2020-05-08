 package com.mbcdev.nextluas.net;
 
 import java.io.IOException;
 import java.util.Date;
 
 import org.jsoup.Connection.Response;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import com.mbcdev.nextluas.constants.StopConstants;
 import com.mbcdev.nextluas.model.ResultModel;
                                                                                                                                                                       
 public class LocalTranscodeSiteConnector implements LuasInfoConnector {
 
 	private static final String INBOUND_SELECTOR	= "div.Inbound";
 	
 	private static final String OUTBOUND_SELECTOR   = "div.Outbound"; 
 	
 	private static final String LOCATION_SELECTOR 	= "div.location";
 	
 	private static final String TIME_SELECTOR 		= "div.time";
 	
	public ResultModel getStopInfo(final String stopSuffix, final String stopName) throws IOException {
 		
 		String url = StopConstants.BASE_URL + stopSuffix;
 		
		Response response = Jsoup
		    .connect(url)
        .timeout(10000)
        .execute();
 		
 		return handleResponse(response, stopName);
 	}
 	
 	
 	
 	private ResultModel handleResponse(Response response, String stopName) throws IOException {
 		
 		ResultModel model = new ResultModel();
 		model.setName(stopName);
 		
 		Document doc = response.parse();
 		
 		Element stage = doc.select(INBOUND_SELECTOR).first();	
 		
 		if (stage != null) {
 						
 			Elements names = stage.select(LOCATION_SELECTOR);
 			Elements times = stage.select(TIME_SELECTOR);
 			
 			for (int i = 0; i < names.size(); i++) {				
 				model.addInbound(
 					names.get(i).text(), times.get(i).text()	
 				);
 			}
 			
 		} else {
 			// LOG INBOUND EMPTY
 		}
 		
 		stage = doc.select(OUTBOUND_SELECTOR).first();
 		
 		if (stage != null) {
 			Elements names = stage.select(LOCATION_SELECTOR);
 			Elements times = stage.select(TIME_SELECTOR);
 			
 			for (int i = 0; i < names.size(); i++) {				
 				model.addOutbound(
 					names.get(i).text(), times.get(i).text()	
 				);
 			}
 		}
 		
 		model.setLastUpdated(new Date());
 		return model;
 	}
 
 }
