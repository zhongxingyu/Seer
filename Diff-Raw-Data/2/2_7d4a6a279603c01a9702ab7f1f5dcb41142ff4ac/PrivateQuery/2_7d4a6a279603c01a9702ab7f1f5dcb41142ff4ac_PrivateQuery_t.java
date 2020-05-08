 package de.ring0.ddg;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.io.IOUtils;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 
 import de.ring0.ddg.datatypes.PrivateResult;
 import de.ring0.ddg.datatypes.Result;
 import de.ring0.ddg.datatypes.ResultCallback;
 import de.ring0.ddg.datatypes.SearchResults;
 
 public class PrivateQuery {
 	private final static String API_URL = "http://duckduckgo.com/d.js";
 	private final GsonBuilder _builder;
 	private final Gson _gson;
 	private String _query;
 	private URL _url;
 
 	private SearchResults _results;
 	private ResultCallback _callback;
 	private IOException _exception;
 
 	public PrivateQuery() {
 		_builder = new GsonBuilder();
 		_builder.disableHtmlEscaping();
 		_gson = _builder.create();
 		_results = null;
 		_callback = null;
 	}
 	public PrivateQuery(String query) throws MalformedURLException {
 		this();
 		setQuery(query);
 	}
 	/**
 	 * Creates a new request URL for processing
 	 * 
 	 * @param query
 	 * @throws MalformedURLException
 	 */
 	public void setQuery(String query) throws MalformedURLException {
 		_query = query;
 		_url = new URL(String.format("%s?q=%s&l=us-en", API_URL, _query));
 	}
 	/**
 	 * Returns a complete URL query used for processing
 	 * 
 	 * @return
 	 */
 	public String getQuery() {
 		if(_url != null)
 			return _url.getQuery();
 		else
 			return null;
 	}
 	/**
 	 * Set callback which will be called with the returned search results
 	 * or an error in case one was thrown
 	 * 
 	 * @param callback
 	 */
 	public void setCallback(ResultCallback callback) {
 		_callback = callback;
 	}
 	/**
 	 * Returns the results in case one needs them again
 	 * 
 	 * @return
 	 */
 	public SearchResults getResults() {
 		return _results;
 	}
 	/**
 	 * Called by the asynchronous execution
 	 * 
 	 */
 	public void run() {
 		try {
 			if(_url == null)
 				throw new IOException("No query available");
 			URLConnection uc = _url.openConnection();
 			
 			/* prepare string for json parsing */
 			String result = IOUtils.toString(uc.getInputStream());
			result = result.substring(result.indexOf("["), result.length() - 2);
 			
 			/* parse the json via gson */
 			PrivateResult[] results = _gson.fromJson(result, PrivateResult[].class);
 			_results = new SearchResults();
 			List<Result> listResults = new ArrayList<Result>();
 			
 			/* add all results except the last one */
 			for(int i = 0; i < results.length - 1; i++)
 				listResults.add(new Result(results[i]));
 			
 			_results.setResults(listResults);
 
 		} catch (IOException e) {
 			_exception = e;
 			if(_callback != null)
 				_callback.failedQuery(e);
 		}
 	}
 	/**
 	 * Call for synchronous execution.
 	 * Returns results or throws error in case there is one.
 	 * 
 	 * @return
 	 * @throws IOException
 	 */
 	public SearchResults start() throws IOException {
 		this.run();
 		if(_exception != null)
 			throw _exception;
 		return _results;
 	}
 }
