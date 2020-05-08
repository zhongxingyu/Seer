 package com.natepaulus.nws.weather;
 
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.MultivaluedMap;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 
 import org.apache.commons.io.input.BOMInputStream;
 
 import com.natepaulus.nws.weather.atom.Entry;
 import com.natepaulus.nws.weather.atom.Feed;
 import com.natepaulus.nws.weather.cap.Alert;
 import com.natepaulus.nws.weather.cap.Alert.Info.Area;
 import com.natepaulus.nws.weather.cap.Alert.Info.Parameter;
 import com.natepaulus.nws.weather.fcc.Response;
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.core.util.MultivaluedMapImpl;
 
 public class Weather {
 
 	
 	/**
 	 * Returns the FIPS code for the county that GPS coordinates reside in formatted for the NWS system.
 	 * @param lat - latitude used to get county
 	 * @param lon - longitude used to get county
 	 * @return String that represents the National Weather Service county code
 	 */
 	public static String getData(String lat, String lon) {
 		Client c = Client.create();
 		c.setFollowRedirects(true);
 		WebResource r = c.resource("http://data.fcc.gov/api/block/find");
 		MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
 		   queryParams.add("latitude", lat);
 		   queryParams.add("longitude", lon);		   	   
 		   r.accept(MediaType.APPLICATION_XML);
 		Response fccResponse = r.queryParams(queryParams).get(Response.class);
 		
 		String state = fccResponse.getState().getCode();
 		String fipsCountyCode = fccResponse.getCounty().getFIPS().toString().substring(2);
 		if(fipsCountyCode.length() == 2){
 			fipsCountyCode = "0" + fipsCountyCode;
 		} else if(fipsCountyCode.length() == 1){
 			fipsCountyCode = "00" + fipsCountyCode;
 		}
 		String fips = state +"C" + fipsCountyCode;
 		
 		return fips;
 
 	}
 	
 	/**
 	 * Retrieves the alerts from the NWS at the URL provided
 	 * @param url - NWS URL that is used to retrieve alert information for specific county
 	 * @return
 	 */
 	public static String getAtomFeed(URL url) {
 			
 		String text = "";
 		InputStream in = null;
 		BOMInputStream bomIn = null;
 		try{		
 			in = url.openStream();
 			bomIn = new BOMInputStream(in);
 		} catch (IOException e){
 			return "<alerts title=\"Error\">" +
 					"<alert><headline>Unable to get the weather data.  Please refresh the page</headline></alert>" +
 					"</alerts>";
 		}
 		
 		Feed feed = null;
 		try {
 			JAXBContext capFeed = JAXBContext.newInstance(Feed.class);
     		Unmarshaller capFeedData = capFeed.createUnmarshaller();
 			feed = (Feed) capFeedData.unmarshal(bomIn);
 		} catch (JAXBException e1) {
 			
 			e1.printStackTrace();
 		}
 		
 		text +="<alerts title=\""+feed.getTitle() + "\">\n";
 		
 		List<Entry> entry = feed.getEntry();
 		
 		for(Entry e : entry){
 			if(e.getTitle().equals("There are no active watches, warnings or advisories")){
 				text += "<alert><description>No active watches, warnings or advisories.</description></alert>\n";
 			}
 			else {
 				Alert alert = null;
 				URL capURL = null;
 				InputStream capIn = null;
 				
 				try {
 					capURL = new URL(e.getId());
 					capIn = capURL.openStream();
 					JAXBContext capAlert = JAXBContext.newInstance(Alert.class);
 	        		Unmarshaller capAlertData = capAlert.createUnmarshaller();
 					alert = (Alert) capAlertData.unmarshal(capIn);
 				} catch (JAXBException | IOException e1) {
 					return "<alerts title=\"Error\">" +
 							"<alert>" +
 							"<headline>Error</headline>" +
 							"<description>There appears to be an alert, but the data wasn't all available.  Please refresh the page to try again.</description>" +
 							"</alert>" +
 							"</alerts>";
 				}        		
 				
         		List<Alert.Info> details = alert.getInfo();
         		for(Alert.Info a: details){
         			
         			String desc = a.getDescription().replaceAll("\n\\*", "&lt;br /&gt;&lt;br /&gt;\\*")
         					.replaceAll("\n\\.", "&lt;br /&gt;&lt;br /&gt;\\.")
         					.replaceAll("\\.\\.\\.\nTHE", "\\.\\.\\.&lt;br /&gt;&lt;br /&gt;THE")
         					.replaceAll("\\.\\.\\.\nA", "\\.\\.\\.&lt;br /&gt;&lt;br /&gt;A");
         			
         			text += "<alert>\n";
         			text += "<headline>" + a.getHeadline() + "</headline>";
     				text += "\t<description>" + desc + "</description>\n";
     				text += "\t<instructions>" + a.getInstruction() + "</instructions> \n";
     				text += "\t<coordinates>" + getCoordinates(a) + "</coordinates>\n";
     				text += "\t<phenomena>" + getVTECPhenomena(a) + "</phenomena>";    						
     				text += "</alert>\n";
         		}
 			}
 		}	
 		text += "</alerts>";
 		return text;
 		
 	}
 	
 	private static String getCoordinates(Alert.Info a) {
 		String result = "";
 		
 		List<Area> areaList = a.getArea();
 		Iterator<Area> i = areaList.iterator();
 		
 		if(i.hasNext()){
 			Area poly = i.next();
 			List<String> polyCoords = poly.getPolygon();
 			Iterator<String> j = polyCoords.iterator();
 			
 			while(j.hasNext()){
 				result += j.next();
 			}
 		}
 		return result;
 	}
 	
 	private static String getVTECPhenomena(Alert.Info a){
 		String result = "";
 		
 		List<Parameter> parameters = a.getParameter();
 		Iterator<Parameter> iteratorParameters = parameters.iterator();
 		while(iteratorParameters.hasNext()){
 			Parameter p = iteratorParameters.next();
 			if(p.getValueName().equals("VTEC")){
 				String vtecValues = p.getValue();
 				String vtecs[] = vtecValues.split("/");
 				for (String v : vtecs){
					String[] VTEC = v.split("\\.");
					if(VTEC[0].length() == 1){
 						if(VTEC[4].equals("W")){
 							return result = VTEC[3];
 						}
 					}
 				}
 			}
		}
 		
 		return result;
 	}
 
 }
