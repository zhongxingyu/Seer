 package uk.ac.ids.util;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import uk.ac.ids.data.Parameters;
 
 import com.google.gson.JsonArray;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 
 public class SearchTool {
 	private String searchPattern;
 	private String searchResultPattern;
 	private String resultFormat;
 
 	/**
 	 * @param searchPattern
 	 * @param searchResultPattern
 	 * @param resultFormat
 	 */
 	public SearchTool(String searchPattern, String searchResultPattern,
 			String resultFormat) {
 		this.searchPattern = searchPattern;
 		this.searchResultPattern = searchResultPattern;
 		this.resultFormat = resultFormat;
 	}
 
 	/**
 	 * @param keyword
 	 * @return
 	 */
 	public Map<String, String> getResults(String keyword) {
 		// Prepare the results
 		Map<String, String> results = new HashMap<String, String>();
 
 		try {
 			// Prepare the URL
 			URL url = new URL(searchPattern.replace("{word}", keyword));
 
 			// Get the API key
 			String api_key = Parameters.getInstance().get(Parameters.API_KEY);
			
 			// Issue the API request
 			StringBuffer response = new StringBuffer();
 			HttpURLConnection connection = (HttpURLConnection) url
 					.openConnection();
 			connection.setRequestProperty("Token-Guid", api_key);
 			BufferedReader reader = new BufferedReader(new InputStreamReader(
 					connection.getInputStream()));
 			String line;
 			while ((line = reader.readLine()) != null) {
 				response.append(line);
 			}
 			reader.close();
 
 			// Parse the response
 			JsonParser parser = new JsonParser();
 			JsonElement e = parser.parse(response.toString());
 
 			// Look for an array in the response
 			JsonArray resultsArray = null;
 			if (e.isJsonObject()) {
 				for (Entry<String, JsonElement> entry : ((JsonObject) e)
 						.entrySet()) {
 					if (entry.getValue().isJsonArray())
 						resultsArray = (JsonArray) entry.getValue();
 				}
 			}
 
 			// Process the results
 			for (JsonElement r : resultsArray) {
 				if (r.isJsonObject()) {
 					String entryURL = searchResultPattern;
 					String entryLabel = resultFormat;
 					for (Entry<String, JsonElement> entry : r.getAsJsonObject()
 							.entrySet()) {
 						if (entry.getValue().isJsonPrimitive()) {
 							entryURL = entryURL.replace("{" + entry.getKey()
 									+ "}", entry.getValue().getAsString());
 							entryLabel = entryLabel.replace(
 									"{" + entry.getKey() + "}", entry
 											.getValue().getAsString());
 						}
 					}
 					results.put(entryURL, entryLabel);
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		return results;
 	}
 }
