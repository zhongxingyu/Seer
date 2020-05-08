 package de.hpi.fgis.yql;
 
 import java.io.Closeable;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.AbstractMap.SimpleEntry;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Scanner;
 import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
 
 import com.mongodb.DBObject;
 import com.ning.http.client.AsyncCompletionHandler;
 import com.ning.http.client.AsyncHttpClient;
 import com.ning.http.client.FluentStringsMap;
 import com.ning.http.client.ListenableFuture;
 import com.ning.http.client.Response;
 
 import de.hpi.fgis.concurrency.AsyncResultHandler;
 
 /**
  * this abstract class provides different possibilities to access the <a
  * href="http://developer.yahoo.com/yql/">Yahoo! Query Language (YQL)</a>
  * web-interface.
  * 
  * @author tonigr
  * 
  */
 public abstract class YQLApi implements Closeable {
 	private final String yqlBaseURI;
 	private final AsyncHttpClient asyncClient;
	protected static final Logger LOG = Logger.getLogger(YQLApi.class.getName());
 
 	protected YQLApi() {
 		this("http://query.yahooapis.com/v1/public/yql?");
 	}
 
 	protected YQLApi(String baseURI) {
 		this.yqlBaseURI = baseURI;
 		asyncClient = new AsyncHttpClient();
 		
 	}
 
 	/**
 	 * parse the returned result string provided by the YQL REST API
 	 * 
 	 * @param is
 	 *            the data string to be parsed
 	 * @return the actual data object
 	 */
 	protected abstract DBObject parse(InputStream is);
 
 	/**
 	 * gets the format string used/provided by the YQL endpoint
 	 * 
 	 * @return
 	 */
 	protected abstract String format();
 
 	/**
 	 * perform the specified query
 	 * 
 	 * @param query
 	 *            the query to be executed (for instance
 	 *            "select * from weather.forecast where woeid=638242" gets the
 	 *            weather forecast for Berlin, Germany)
 	 * @return a {@link DBObject} representation of the results (i.e. the
 	 *         forcast data)
 	 * @throws IOException
 	 *             if some network errors occur
 	 */
 	public DBObject query(String query) throws IOException {
 		return query(query, (Collection<Entry<String, String>>) null);
 	}
 	
 	/**
 	 * perform the specified query asynchronously via asyncResultHandler
 	 * 
 	 * @param query
 	 *            the query to be executed (for instance
 	 *            "select * from weather.forecast where woeid=638242" gets the
 	 *            weather forecast for Berlin, Germany)
 	 * @param asyncResultHandler
 	 *            an asynchronous result processor that gets informed if the results are available
 	 * @throws IOException
 	 *             if some network errors occur
 	 */
 	public void queryAsync(String query, AsyncResultHandler<DBObject> asyncResultHandler) throws IOException {
 		queryAsync(query, (Collection<Entry<String, String>>) null, false, asyncResultHandler);
 	}
 
 	/**
 	 * perform the specified query
 	 * 
 	 * @param query
 	 *            the query to be executed (for instance
 	 *            "select * from weather where location='Berlin, Germany'" gets
 	 *            the weather forecast for Berlin, Germany given the following
 	 *            table definition)
 	 * @param tableName
 	 *            name of the custom datatable used in the query (i.e.
 	 *            "weather")
 	 * @param tableDefURI
 	 *            url of the table definition file (i.e.
 	 *            "<a href='http://www.datatables.org/weather/weather.bylocation.xml' >http://www.datatables.org/weather/weather.bylocation.xml</a>"
 	 *            )
 	 * @return a {@link DBObject} representation of the results (i.e. the
 	 *         forcast data)
 	 * @throws IOException
 	 *             if some network errors occur
 	 */
 	public DBObject query(String query, String tableName, String tableDefURI)
 			throws IOException {
 		return query(query,
 				Arrays.asList((Entry<String, String>) new SimpleEntry<>(
 						tableName, tableDefURI)));
 	}
 
 	/**
 	 * perform the specified query asynchronously via asyncResultHandler
 	 * 
 	 * @param query
 	 *            the query to be executed (for instance
 	 *            "select * from weather where location='Berlin, Germany'" gets
 	 *            the weather forecast for Berlin, Germany given the following
 	 *            table definition)
 	 * @param tableName
 	 *            name of the custom datatable used in the query (i.e.
 	 *            "weather")
 	 * @param tableDefURI
 	 *            url of the table definition file (i.e.
 	 *            "<a href='http://www.datatables.org/weather/weather.bylocation.xml'>http://www.datatables.org/weather/weather.bylocation.xml</a>"
 	 *            )
 	 * @param asyncResultHandler
 	 *            an asynchronous result processor that gets informed if the results are available
 	 * @throws IOException
 	 *             if some network errors occur
 	 */
 	public void queryAsync(String query, String tableName, String tableDefURI, AsyncResultHandler<DBObject> asyncResultHandler)
 			throws IOException {
 		queryAsync(query,
 				Arrays.asList((Entry<String, String>) new SimpleEntry<>(
 						tableName, tableDefURI)), false, asyncResultHandler);
 	}
 
 	/**
 	 * perform the specified query
 	 * 
 	 * @param query
 	 *            the query to be executed (for instance
 	 *            "select * from weather where location='Berlin, Germany'" gets
 	 *            the weather forecast for Berlin, Germany given the following
 	 *            table definition)
 	 * @param tables
 	 *            a map containing all custom datatable definitions -- keys:
 	 *            name of the table used in the query (i.e. "weather"), values:
 	 *            url of the table definition file (i.e.
 	 *            "<a href='http://www.datatables.org/weather/weather.bylocation.xml'>http://www.datatables.org/weather/weather.bylocation.xml</a>"
 	 *            )
 	 * @return a {@link DBObject} representation of the results (i.e. the
 	 *         forcast data)
 	 * @throws IOException
 	 *             if some network errors occur
 	 */
 	public DBObject query(String query, Map<String, String> tables)
 			throws IOException {
 		return query(query, tables.entrySet());
 	}
 
 	/**
 	 * perform the specified query asynchronously via asyncResultHandler
 	 * 
 	 * @param query
 	 *            the query to be executed (for instance
 	 *            "select * from weather where location='Berlin, Germany'" gets
 	 *            the weather forecast for Berlin, Germany given the following
 	 *            table definition)
 	 * @param tables
 	 *            a map containing all custom datatable definitions -- keys:
 	 *            name of the table used in the query (i.e. "weather"), values:
 	 *            url of the table definition file (i.e.
 	 *            "<a href='http://www.datatables.org/weather/weather.bylocation.xml'>http://www.datatables.org/weather/weather.bylocation.xml</a>"
 	 *            )
 	 * @param asyncResultHandler
 	 *            an asynchronous result processor that gets informed if the results are available
 	 *            
 	 * @throws IOException
 	 *             if some network errors occur
 	 */
 	public void queryAsync(String query, Map<String, String> tables, AsyncResultHandler<DBObject> asyncResultHandler)
 			throws IOException {
 		queryAsync(query, tables.entrySet(), false, asyncResultHandler);
 	}
 
 	/**
 	 * perform the specified query and return additional meta information
 	 * 
 	 * @param query
 	 *            the query to be executed (for instance
 	 *            "select * from weather where location='Berlin, Germany'" gets
 	 *            the weather forecast for Berlin, Germany given the following
 	 *            table definition)
 	 * @param tables
 	 *            a map containing all custom datatable definitions -- keys:
 	 *            name of the table used in the query (i.e. "weather"), values:
 	 *            url of the table definition file (i.e.
 	 *            "<a href='http://www.datatables.org/weather/weather.bylocation.xml'>http://www.datatables.org/weather/weather.bylocation.xml</a>"
 	 *            )
 	 * @return a {@link DBObject} representation of the results (i.e. the
 	 *         forcast data)
 	 * @throws IOException
 	 *             if some network errors occur
 	 */
 	public DBObject queryMeta(String query, Map<String, String> tables)
 			throws IOException {
 		return queryMeta(query, tables.entrySet(), true);
 	}
 
 	/**
 	 * simple util method to create a String from an input stream
 	 * @param is the input stream to read
 	 * @param charset the charset
 	 * @return the resulting string content
 	 */
 	protected String convertStreamToString(InputStream is, String charset) {
 		Scanner s = new Scanner(is, charset);
 		try {
 			s.useDelimiter("\\A");
 			return s.hasNext() ? s.next() : "";
 		} finally {
 			s.close();
 		}
 	}
 
 	private DBObject query(String query,
 			Collection<Entry<String, String>> tableDefs) throws IOException {
 		return extractResults(queryMeta(query, tableDefs, false));
 	}
 	
 	private void queryAsync(final String query,
 			final Collection<Entry<String, String>> tableDefs, boolean debug, final AsyncResultHandler<DBObject> asyncResultHandler)
 			throws IOException {
 		// asyncClient.prepareGet(yqlBaseURI) does not work with parameter definitions
 		asyncClient.preparePost(yqlBaseURI).setParameters(toParameterMap(query, tableDefs, debug)).execute(new AsyncCompletionHandler<DBObject>(){
 
 		    @Override
 		    public DBObject onCompleted(Response response) throws Exception{
 		    	DBObject data = extractResults(parse(response.getResponseBodyAsStream()));
 		    	
 		    	//asyncHttpClient.close();
 		    	asyncResultHandler.onCompleted(data);
 		        return data;
 		    }
 
 		    @Override
 		    public void onThrowable(Throwable t){
 		    	//asyncHttpClient.close();
 		    	asyncResultHandler.onThrowable(t);
 		    }
 		    
 		    
 		});
 	}
 
 	private DBObject queryMeta(String query,
 			Collection<Entry<String, String>> tableDefs, boolean debug)
 			throws IOException {
 		// asyncClient.prepareGet(yqlBaseURI) does not work with parameter definitions
 		ListenableFuture<Response> response = asyncClient.preparePost(yqlBaseURI).setParameters(toParameterMap(query, tableDefs, debug)).execute();
 		
 		InputStream in = null;
 		try {
 			in = response.get().getResponseBodyAsStream();
 			return parse(in);
 		} catch (InterruptedException | ExecutionException e) {
 			throw new IOException("Unable to fetch date from YQL endpoint!" ,e);
 		} finally {
 			if(in!=null) {
 				in.close();
 			}
 		}
 	}
 	
 	private DBObject extractResults(DBObject completeResult) {
 		if(completeResult==null || !completeResult.containsField("query") || !((DBObject) completeResult.get("query")).containsField("results")) {
 			return null;
 		}
 		return (DBObject) ((DBObject) completeResult.get("query")).get("results");
 	}
 	
 	private FluentStringsMap toParameterMap(String query,
 			Collection<Entry<String, String>> tableDefs, boolean debug) {
 		FluentStringsMap parameters = new FluentStringsMap();
 		parameters.add("q", toTableDefString(tableDefs) + query);
 		parameters.add("callback", "");
 		parameters.add("format", format());
 		if (debug) {
 			parameters.add("diagnostics", "true");
 			// parameters.add("debug", "true");
 		}
 
 		return parameters;
 	}
 	
 	private String toTableDefString(Collection<Entry<String, String>> tableDefs) {
 		if (tableDefs == null || tableDefs.size() == 0) {
 			return "";
 		}
 		StringBuilder result = new StringBuilder();
 		for (Entry<String, String> entry : tableDefs) {
 			result.append("USE \"").append(entry.getValue()).append("\" AS ")
 					.append(entry.getKey()).append(';');
 		}
 
 		return result.toString();
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see java.io.Closeable#close()
 	 */
 	@Override
 	public void close() {
 		asyncClient.close();
 	}
 	
 }
