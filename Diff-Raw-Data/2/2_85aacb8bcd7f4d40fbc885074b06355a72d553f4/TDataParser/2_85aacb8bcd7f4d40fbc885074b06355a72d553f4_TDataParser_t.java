 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import model.TFactory;
 import model.TPrediction;
 import model.TStop;
 import model.TTrip;
 
 import com.fasterxml.jackson.core.JsonParseException;
 import com.fasterxml.jackson.databind.JsonMappingException;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 
 /***
  * 
  * @author elbee
  * @author welden
  *
  */
 public class TDataParser {
 
 	/** The lines we have data for. */
 	public static enum Line {
 		BLUE,
 		ORANGE,
 		RED
 	}
 
 	private static JsonNode getData(URL lineURL) {
 		ObjectMapper mapper = new ObjectMapper();	
 		try {
 			JsonNode rootNode = mapper.readValue(lineURL, JsonNode.class);
 			return rootNode;
 		} catch (JsonParseException e) {
 			e.printStackTrace();
 		} catch (JsonMappingException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return null;	
 	}
 
 	/**
 	 * Populates our factory with the real time data for the given line.
 	 *
 	 * @param line The line to get data for.
 	 */
 	public static void populateRealTimeData(Line line) {
 		// Get the JSON data
 		JsonNode rootNode = getData(getLineURL(line));
 
 		// Parse it and store it in our factory
 		for (JsonNode tripsNode : rootNode.findValues("Trips")) {
 			for (JsonNode tNode : tripsNode) {
 				// Get our trip reference from the TripID
 				TTrip trip= TFactory.getTrip( tNode.findValue("TripID").textValue() );
 
 				// Now fill in the relevant data...
 
 				// Get the current predictions for this trip
 				List<TPrediction> pPredictions= new ArrayList<TPrediction>();
 				for (JsonNode pNode : tNode.findValue("Predictions")) {
 					// Get the relevant data for this prediction
 					TStop pStop= TFactory.getStop( pNode.findValue("StopID").asInt() );
 					int nSecToArrival= pNode.findValue("Seconds").asInt();

 					// Create a new prediction with our parsed data
 					TPrediction pPrediction= new TPrediction(pStop, nSecToArrival);
 
 					Logger.getAnonymousLogger().log( Level.INFO, "Found new prediction " + pPrediction );
 
 					// Add the prediction to our list
 					pPredictions.add( pPrediction );
 				}
 
 				// Set the current predictions for our trip
 				trip.setPredictions(pPredictions);
 			}
 		}
 	}
 
 	/**
 	 * Get the URL for the given line.
 	 *
 	 * @param line The line to get the URL for.
 	 */
 	private static URL getLineURL(Line line) {
 		// The URL we should get our data from
 		String sURL= "";
 
 		// Get the appropriate URL for the given line
 		// TODO: Move out these string constants to somewhere more appropriate
 		switch( line ) {
 		case BLUE   : sURL= "http://developer.mbta.com/lib/rthr/blue.json";   break;
 		case ORANGE : sURL= "http://developer.mbta.com/lib/rthr/orange.json"; break;
 		case RED    : sURL= "http://developer.mbta.com/lib/rthr/red.json";    break;
 		}
 
 		// Our URL object to return
 		URL pURL= null;
 
 		// Try to create a URL using our sURL. This should never throw as long
 		// as our constants our correct.
 		try {
 			pURL= new URL(sURL);
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		}
 
 		return pURL;
 	}
 }
