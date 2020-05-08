 package ch.hsr.bieridee.android.config;
 
 import java.util.Map;
 import java.util.Map.Entry;
 
 /**
  * Resources.
  */
 public final class Res {
 
 	private Res() {
 		// do not instantiate.
 	}
 
 	public static final String API_URL = "http://brauhaus.nusszipfel.com:8080";
	//public static final String API_URL = "http://192.168.0.119:8080";
 
 	// beer
 	public static final String BEER_COLLECTION = "/beers";
 	public static final String BEER_REQ_ATTR = "beer-id";
 	public static final String BEER_DOCUMENT = "/beers/{" + BEER_REQ_ATTR + "}";
 	public static final String BEER_FILTER_PARAMETER_TAG = "tag";
 	public static final String BEER_FILTER_PARAMETER_BARCODE = "barcode";
 
 	// beertype
 	public static final String BEERTYPE_COLLECTION = "/beertypes";
 	public static final String BEERTYPE_REQ_ATTR = "beertype-id";
 	public static final String BEERTYPE_DOCUMENT = "/beertypes/{" + BEERTYPE_REQ_ATTR + "}";
 
 	// brewery
 	public static final String BREWERY_COLLECTION = "/breweries";
 	public static final String BREWERY_REQ_ATTR = "brewery-id";
 	public static final String BREWERY_DOCUMENT = "/breweries/{" + BREWERY_REQ_ATTR + "}";
 	public static final String BREWERY_FILTER_PARAMETER_SIZE = "size";
 
 	// tag
 	public static final String TAG_COLLECTION = "/tags";
 	public static final String TAG_REQ_ATTR = "tag-name";
 	public static final String TAG_DOCUMENT = "/tags/{" + TAG_REQ_ATTR + "}";
 	public static final String TAG_COLLECTION_FILTER_PARAMETER_BEERID = "beerId";
 
 	// user
 	public static final String USER_COLLECTION = "/users";
 	public static final String USER_REQ_ATTR = "user-name";
 	public static final String USER_DOCUMENT = "/users/{" + USER_REQ_ATTR + "}";
 	public static final String USER_RECOMMENDATION_COLLECTION = USER_DOCUMENT + "/recommendations";
 
 	// usercredentials
 	public static final String USERCREDENTIALS_CONTROLLER = "/usercredentials";
 
 	// rating
 	public static final String RATING_COLLECTION = "/ratings";
 	public static final String RATING_DOCUMENT = BEER_DOCUMENT + RATING_COLLECTION + "/{" + USER_REQ_ATTR + "}";
 
 	// consumption
 	public static final String CONSUMPTION_COLLECTION = "/consumptions";
 	public static final String CONSUMPTION_DOCUMENT = BEER_DOCUMENT + CONSUMPTION_COLLECTION + "/{" + USER_REQ_ATTR + "}";
 
 	// brand
 	public static final String BRAND_COLLECTION = "/brands";
 
 	// timeline
 	public static final String TIMELINE_COLLECTION = "/timeline";
 	public static final String TIMELINE_FILTER_USERNAME = "username";
 
 	// brewerysize
 	public static final String BREWERYSIZE_COLLECTION = "/brewerysizes";
 
 	/**
 	 * Returns the specified URI.
 	 * 
 	 * This is done by appending the resource URI to the base URI. If there are parameters in the URI, those are
 	 * replaced using the provided attrs.
 	 * 
 	 * @param resource
 	 *            Resource string ({@code Res} constants should be used).
 	 * @param attrs
 	 *            Arbitrary number of URI attribute replacements (should match the number of attributes marked with
 	 *            curly braces).
 	 * @return Processed URI string
 	 */
 	public static String getURI(final String resource, final String... attrs) {
 		String parsedResource = resource;
 		for (String attr : attrs) {
 			parsedResource = parsedResource.replaceFirst("\\{[^\\}]*\\}", attr);
 		}
 		return API_URL + parsedResource;
 	}
 
 	/**
 	 * Returns the specified URI.
 	 * 
 	 * This is done by appending the resource URI to the base URI. The key value pairs in the map are appended as query
 	 * parameters (GET)
 	 * 
 	 * @param resource
 	 *            Resource string ({@code Res} constants should be used).
 	 * @param getQueryParameters
 	 *            map containing key value pairs
 	 * @return a string with appended query parameters.
 	 */
 	public static String getURIwithGETParams(String resource, Map<String, String> getQueryParameters) {
 		if (getQueryParameters.isEmpty()) {
 			return API_URL + resource;
 		}
 		resource += "?";
 		final StringBuilder params = new StringBuilder();
 		for (Entry<String, String> entry : getQueryParameters.entrySet()) {
 			params.append(entry.getKey()).append("=").append(entry.getValue());
 			params.append("&");
 		}
 		return API_URL + resource + params.toString().substring(0, params.toString().length() - 1);
 
 	}
 
 	/**
 	 * Returns the specified URI.
 	 * 
 	 * This is done by appending the resource URI to the base URI. The key value pair is appended as a query parameter
 	 * (GET).
 	 * 
 	 * @see {@link this.getURI}
 	 * @param resource
 	 *            Resource string ({@code Res} constants should be used).
 	 * @param key
 	 *            Key
 	 * @param value
 	 *            Value
 	 * @return a string with the appended query parameter
 	 */
 	public static String getURIwithGETParams(String resource, String key, String value) {
 		return API_URL + resource + "?" + key + "=" + value;
 	}
 }
