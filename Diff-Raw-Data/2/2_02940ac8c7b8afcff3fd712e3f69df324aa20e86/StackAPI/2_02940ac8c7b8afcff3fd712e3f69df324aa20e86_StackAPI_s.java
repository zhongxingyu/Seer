 package org.droidstack.stackapi;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.zip.GZIPInputStream;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONTokener;
 
 /**
  * A wrapper around the StackExchange family of websites API
  * @author Felix Oghină <felix.oghina@gmail.com>
  */
 public class StackAPI {
 	
 	/**
 	 * Contains the supported version of the API. This is included in API calls
 	 */
 	public final static String API_VERSION = "0.8";
 	
 	private final String mEndpoint;
 	private final String mKey;
 	
 	
 	/**
 	 * Create a StackAPI object without an API key.
 	 * @param endpoint The API endpoint to use
 	 * @see #getEndpoint(String)
 	 */
 	public StackAPI(String endpoint) {
 		mEndpoint = endpoint;
 		mKey = null;
 	}
 	
 	/**
 	 * Create a StackAPI object.
 	 * @param host The API endpoint to use
 	 * @param key The API key to use in API calls
 	 * @see #getEndpoint(String)
 	 */
 	public StackAPI(String host, String key) {
 		mEndpoint = host;
 		mKey = key;
 	}
 	
 	public static List<Site> getSites() throws IOException, MalformedURLException, JSONException {
 		ArrayList<Site> sites = new ArrayList<Site>();
 		// hard-coding FTW.. have a better idea? contribute! :)
 		JSONArray jsites = ((JSONObject)new JSONTokener(fetchURL(new URL("http://stackauth.com/" + API_VERSION + "/sites"))).nextValue()).getJSONArray("api_sites");
 		for (int i=0; i < jsites.length(); i++) {
 			sites.add(new Site(jsites.getJSONObject(i)));
 		}
 		return sites;
 	}
 	
 	private static String fetchURL(URL url) throws IOException {
 		final URLConnection conn = url.openConnection();
 		conn.connect();
 		final InputStream in;
 		if (conn.getContentEncoding().equals("gzip")) {
 			in = new GZIPInputStream(conn.getInputStream());
 		}
 		else {
 			in = conn.getInputStream();
 		}
 		String charset = conn.getContentType();
 		final BufferedReader reader;
 		if (charset.indexOf("charset=") != -1) {
 			charset = charset.substring(charset.indexOf("charset=") + 8);
 			reader = new BufferedReader(new InputStreamReader(in, charset));
 		}
 		else {
 			charset = null;
 			reader = new BufferedReader(new InputStreamReader(in));
 		}
 		final StringBuilder builder = new StringBuilder();
 		String line = reader.readLine();
 		while (line != null) {
 			builder.append(line + '\n');
 			line = reader.readLine();
 		}
 		return builder.toString();
 	}
 	
 	private URL buildURL(String path) throws MalformedURLException {
		String url = mEndpoint + String.valueOf(API_VERSION) + path;
 		if (mKey != null) {
 			if (url.indexOf('?') == -1) url += "?key=" + mKey;
 			else url += "&key=" + mKey;
 		}
 		return new URL(url);
 	}
 	
 	/**
 	 * Get this site's statistics
 	 * @return a {@link Stats} object
 	 * @throws IOException
 	 * @throws MalformedURLException
 	 * @throws JSONException
 	 */
 	public Stats getStats() throws IOException, MalformedURLException, JSONException {
 		final JSONObject json = (JSONObject) new JSONTokener(fetchURL(buildURL("/stats"))).nextValue();
 		return new Stats(json);
 	}
 	
 	/**
 	 * Perform a question API query
 	 * @param query Describes what query to perform
 	 * @return A list of questions retrieved
 	 * @throws IOException If there's a problem communicating with the server (this includes invalid API key)
 	 * @throws MalformedURLException Should never occur, as we are building the URL ourselves. It will occur if you supply a stupid value for <code>host</code> to {@link #StackAPI(String)} or {@link #StackAPI(String, String)}
 	 * @throws JSONException If the site you're querying is fucked up and the json returned is invalid (or it's not json)
 	 */
 	public List<Question> getQuestions(QuestionsQuery query) throws IOException, MalformedURLException, JSONException {
 		int i;
 		final List<Question> questions = new ArrayList<Question>();
 		final URL url = buildURL(query.buildQueryPath());
 		final JSONObject json = (JSONObject) new JSONTokener(fetchURL(url)).nextValue();
 		final JSONArray jquestions = json.getJSONArray("questions");
 		for (i=0; i < jquestions.length(); i++) {
 			questions.add(new Question(jquestions.getJSONObject(i)));
 		}
 		return questions;
 	}
 	
 	/**
 	 * Gets a user by their ID
 	 * @param id the user's ID
 	 * @return a {@link User} object
 	 * @throws IOExceptionIf there's a problem communicating with the server (this includes invalid API key)
 	 * @throws MalformedURLException Should never occur, as we are building the URL ourselves. It will occur if you supply a stupid value for <code>host</code> to {@link #StackAPI(String)} or {@link #StackAPI(String, String)}
 	 * @throws JSONException If the site you're querying is fucked up and the json returned is invalid (or it's not json)
 	 */
 	public User getUser(long id) throws IOException, MalformedURLException, JSONException {
 		final URL url = buildURL("/users/" + String.valueOf(id));
 		final JSONObject json = (JSONObject) new JSONTokener(fetchURL(url)).nextValue();
 		return new User(json.getJSONArray("users").getJSONObject(0));
 	}
 	
 }
