 /*******************************************************************************
  * Copyright (c) 2013 See AUTHORS file.
  * 
  * This file is part of SleepFighter.
  * 
  * SleepFighter is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * SleepFighter is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with SleepFighter. If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package se.chalmers.dat255.sleepfighter.speech;
 
 import java.io.IOException;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.Scanner;
 
 import se.chalmers.dat255.sleepfighter.utils.debug.Debug;
 
 // gets the current weather from forecast.io given the current coordinates. 
 public class WeatherDataFetcher {
 
 	private final String KEY = "cb8a0d4b48c35b562d1b427b3f77552d";
 	
 	// the json weather data.
 	String jsonData;
 
 	
 	// build the url used to access the weather data, given latitudes and longitudes. 
 	public WeatherDataFetcher(double lat, double lon) {
 			// fetch the json data form forecast.io
 	
 		Debug.d("about to fetch json data");
 		
 			jsonData = httpGET(buildUrl(lat, lon));
 			
 			Debug.d("done fetching json data");
 
 	}
 	
 	// get a summary of the weather condition
 	public String getSummary() {
 		
 		
 		// For some reason JSONObject threw an exception when parsing the data. 
 		// But we are really only interested in one piece of information in this json data.
 		// That is, the value of the field "summary" in the object "currently".
 		// See: https://developer.forecast.io/docs/v2
 		// So we'll extract the value using some elementary string methods. 
 		
 		// find the currently object
 		int currentlyIndex = jsonData.indexOf("\"currently\"");
 		
 		// now find the "summary" field of the "currently" object
 		int summaryIndex = jsonData.indexOf("\"summary\"", currentlyIndex);
 		
 		// now skip until the value of "summary"
 		int i = summaryIndex+1;
 		i = jsonData.indexOf("\"", i) + 1;
 		i = jsonData.indexOf("\"", i) + 1;
 		
 		// find the end of the data.
 		int endI = jsonData.indexOf("\"", i) + 1;
 		
 		return (String) jsonData.subSequence(i, endI);
 	}
 	
 	private String buildUrl(double lat, double lon) {
 		String s =  new String("https://api.forecast.io/forecast/") + KEY + "/" +
 				doubleToString(lat) + "," + doubleToString(lon);
 		return s;
 	}
 	
 	private static String doubleToString(double d) {
 		return String.valueOf(d).replace(",", ".");
 	}
 	
 
 	private String httpGET(String requestURL) {
 
 		//Variables
 		URL request = null;
 		HttpURLConnection connection = null;
 		Scanner scanner = null;
 		String response = "";
 
 		try {
 			request = new URL(requestURL);
 			connection = (HttpURLConnection) request.openConnection();
 
 			connection.setRequestMethod("GET");
 			connection.setUseCaches(false);
 			connection.setDoInput(true);
 			connection.setDoOutput(false);
 			
 			// the data we need is in the beginning, and it is rather short, so we only 
 			// need about the first 200 characters. 
 			// this speeds things up a bit. 
			connection.setRequestProperty("Content-Length", "" + 200);
 			connection.connect();
 		
 			if(connection.getResponseCode() == 400){
 				System.out.println("Bad Responde. Maybe an invalid location was provided.\n");
 				return null;
 			}
 			else {
 				scanner = new Scanner(request.openStream());
 				response = scanner.useDelimiter("\\Z").next();
 				scanner.close();
 			}
 		} catch (IOException e) {
 			Debug.e(e);		
 			response = null;
 		} finally {		
 			connection.disconnect();
 		}
 
 		return response;
 	}//httpGET - end
 
 	
 }
