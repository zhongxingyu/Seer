 package hung.vo;
 
 import java.io.IOException;
 
 import hung.vo.model.*;
 import android.util.Log;
 
 import com.google.api.client.googleapis.GoogleHeaders;
 import com.google.api.client.http.GenericUrl;
 import com.google.api.client.http.HttpRequest;
 import com.google.api.client.http.HttpRequestFactory;
 import com.google.api.client.http.HttpRequestInitializer;
 import com.google.api.client.http.HttpResponseException;
 import com.google.api.client.http.HttpTransport;
 import com.google.api.client.http.apache.ApacheHttpTransport;
 import com.google.api.client.http.json.JsonHttpParser;
 import com.google.api.client.json.jackson.JacksonFactory;
 
 public class PlaceRequest {
 
 	// Create our transport.
 	private static final HttpTransport transport = new ApacheHttpTransport();
 	
 	// Fill in the API key you want to use.
 	private static final String API_KEY = "AIzaSyCRmej3hzfD4HeYcjO4yjZRSrYxhq_OuuQ";
 	private static final String LOG_KEY = "GGPlace";
 	// The different Places API endpoints.
 	private static final String PLACES_SEARCH_URL =  "https://maps.googleapis.com/maps/api/place/search/json?";
 	private static final String PLACES_AUTOCOMPLETE_URL = "https://maps.googleapis.com/maps/api/place/autocomplete/json?";
 	private static final String PLACES_DETAILS_URL = "https://maps.googleapis.com/maps/api/place/details/json?";
 	
 	private static final boolean PRINT_AS_STRING = true;
 	
 	// Moscone Center, Howard Street, San Francisco, CA, United States
 	double latitude = 10.785503490912339;
 	double longitude = 106.6885757446289;
 	
 	// telenet
 	//double latitude = 51.034823;
 	//double longitude = 4.483774;
 
 	// home
 	//double latitude = 50.963062;
 	//double longitude = 3.522255;
 	
 	//Ho Chi Minh City
 	//double latitude = 10.785503490912339;
 	//double longtitude = 106.6885757446289;
 	
 	
 	
 	public PlacesList performSearch() throws Exception {
 		try {
 			Log.v(LOG_KEY, "Start Search");
			GenericUrl reqUrl = new GenericUrl(PLACES_SEARCH_URL);
			reqUrl.put("key", API_KEY);
			reqUrl.put("location", latitude + "," + longitude);
			reqUrl.put("radius", 500);
			reqUrl.put("sensor", "false");
 			Log.v(LOG_KEY, "url= " + reqUrl);
 			HttpRequestFactory httpRequestFactory = createRequestFactory(transport);
			HttpRequest request = httpRequestFactory.buildGetRequest(reqUrl);
			
			
 			
 			
 			
 				Log.v(LOG_KEY, request.execute().parseAsString());							
 				PlacesList places = request.execute().parseAs(PlacesList.class);
 				Log.v(LOG_KEY, "STATUS = " + places.status);
 				for (Place place : places.results) {
 					Log.v(LOG_KEY, place.name);				
 					
 				}
 				return places;
 
 		} catch (HttpResponseException e) {
 			Log.v(LOG_KEY, e.getResponse().parseAsString());
 			throw e;
 		}
 		
 		catch (IOException e) {
 			// TODO: handle exception
 			throw e;
 		}
 	}
 	
 	/*public void performDetails(String reference) throws Exception {
 		try {
 			System.out.println("Perform Place Detail....");
 			System.out.println("-------------------");
 			HttpRequestFactory httpRequestFactory = createRequestFactory(transport);
 			HttpRequest request = httpRequestFactory.buildGetRequest(new GenericUrl(PLACES_DETAILS_URL));
 			request.url.put("key", API_KEY);
 			request.url.put("reference", reference);
 			request.url.put("sensor", "false");
 			
 			if (PRINT_AS_STRING) {
 				System.out.println(request.execute().parseAsString());
 			} else {
 				PlaceDetail place = request.execute().parseAs(PlaceDetail.class);
 				System.out.println(place);
 			}
 
 		} catch (HttpResponseException e) {
 			System.err.println(e.response.parseAsString());
 			throw e;
 		}
 	}
 	*/
 	
 	
 	/*public void performAutoComplete() throws Exception {
 		try {
 			System.out.println("Perform Autocomplete ....");
 			System.out.println("-------------------------");
 			
 			HttpRequestFactory httpRequestFactory = createRequestFactory(transport);
 			HttpRequest request = httpRequestFactory.buildGetRequest(new GenericUrl(PLACES_AUTOCOMPLETE_URL));
 			request.url.put("key", API_KEY);
 			request.url.put("input", "mos");
 			request.url.put("location", latitude + "," + longitude);
 			request.url.put("radius", 500);
 			request.url.put("sensor", "false");
 			PlacesAutocompleteList places = request.execute().parseAs(PlacesAutocompleteList.class);
 			if (PRINT_AS_STRING) {
 				System.out.println(request.execute().parseAsString());
 			} else {
 				for (PlaceAutoComplete place : places.predictions) {
 					System.out.println(place);
 				}
 			}
 
 		} catch (HttpResponseException e) {
 			System.err.println(e.response.parseAsString());
 			throw e;
 		}
 	}	*/
 	
 	public static HttpRequestFactory createRequestFactory(final HttpTransport transport) {
 			   
 		  return transport.createRequestFactory(new HttpRequestInitializer() {
 		   public void initialize(HttpRequest request) {
 		    GoogleHeaders headers = new GoogleHeaders();
 		    headers.setApplicationName("Google-Places-DemoApp");
 		    request.setHeaders(headers);
 		    JsonHttpParser parser = new JsonHttpParser(new JacksonFactory()) ;
 		    
 		    //JsonHttpParser.builder(new JacksonFactory());
 		    //parser.jsonFactory = new JacksonFactory();
 		    request.addParser(parser);
 		   }
 		});
 	}
 }
