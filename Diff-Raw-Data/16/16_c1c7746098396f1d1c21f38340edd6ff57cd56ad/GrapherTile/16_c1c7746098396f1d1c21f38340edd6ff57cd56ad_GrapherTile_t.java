 package org.bodytrack.client;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.google.gwt.core.client.JavaScriptObject;
 import com.google.gwt.core.client.JsArray;
 import com.google.gwt.http.client.Request;
 import com.google.gwt.http.client.RequestBuilder;
 import com.google.gwt.http.client.RequestCallback;
 import com.google.gwt.http.client.RequestException;
 import com.google.gwt.http.client.Response;
 
 /**
  * Represents a single tile of data.
  *
  * <p>This is generic enough to hold an array of
  * {@link org.bodytrack.client.PhotoDescription PhotoDescription}
  * objects, or a single
  * {@link org.bodytrack.client.PlottablePointTile PlottablePointTile}
  * object.  A GrapherTile object has two very important methods:
  * {@link #getPlottableTile()} and {@link #getPhotoDescriptions()}.
  * At least one of these methods will always return <tt>null</tt> for
  * any given <tt>GrapherTile</tt> object, but normally one of these
  * methods will return a non-<tt>null</tt> value that is useful.  In
  * this way, a <tt>GrapherTile</tt> object is a useful container for
  * objects of two other types.</p>
  *
  * <p>Since this almost always holds a reference to a JavaScript
  * overlay object, for efficiency we do not bother making copies of
  * return values, meaning that this class is <em>not</em> immutable.</p>
  */
 // TODO: Use JSONParser.parseStrict() rather than eval() - this is for safety
 // OR use http://www.ietf.org/rfc/rfc4627.txt, section 6, which has an
 // expression that will parse JSON safely
 // TODO: Make sure the level and offset we store here are consistent with
 // that sent to us in a PlottablePointTile
 
 public final class GrapherTile {
 	// One or the other of these fields will always be null
 	private final String url;
 	private final int level;
 	private final int offset;
 	private final PlottablePointTile tile;
 	private final List<PhotoDescription> photoDescs;
 
 	/**
 	 * The width of a tile, in data points.
 	 *
 	 * <p>Each tile is defined to encompass all data in a time range
 	 * equal to {@code Math.pow(2, level) * TILE_WIDTH} seconds.</p>
 	 */
 	public static final int TILE_WIDTH = 512;
 
 	/**
 	 * Initializes this GrapherTile.
 	 *
 	 * @param url
 	 * 		the URL from which json came
 	 * @param level
 	 * 		the level of this tile
 	 * @param offset
 	 * 		the offset of this tile
 	 * @param json
 	 * 		the JSON representation of either a list of PhotoDescription
 	 * 		objects or a single PlottablePointTile object.  Another
 	 * 		possibility is to pass the empty string as this parameter,
 	 * 		to represent that this tile contains no data
 	 * @throws NullPointerException
 	 * 		if url or json is <tt>null</tt>
 	 */
 	public GrapherTile(String url, int level, int offset, String json) {
 		if (url == null || json == null)
 			throw new NullPointerException(
 				"Null parameter for construction of GrapherTile");
 
 		this.url = url;
 
 		// Allow for the special case in which JSON is the empty string
 		if (json.equals("")) {
 			tile = null;
 			photoDescs = null;
			this.level = level;
			this.offset = offset;
 			return;
 		}
 
 		if (isArray(json)) {
			this.level = level;
			this.offset = offset;

 			tile = null;
 			photoDescs = new ArrayList<PhotoDescription>();
 
 			JsArray<JavaScriptObject> arr = evalArray(json);
 
 			for (int i = 0; i < arr.length(); i++)
 				photoDescs.add((PhotoDescription) arr.get(i));
 		} else {
 			photoDescs = null;
 			tile = PlottablePointTile.buildTile(json);

			// Use the tile's actual level and offset in this case only
			// Only in this case will the server ever return a level
			// different from the level requested
			this.level = tile.getLevel();
			this.offset = tile.getOffset();
 		}
 	}
 
 	/**
 	 * Tells if the specified text refers to a JavaScript
 	 * Array object rather than to a dictionary.
 	 *
 	 * <h2 style="color: red">WARNING:</h2>
 	 *
 	 * <p>This uses the JavaScript eval() function, which makes
 	 * it dangerous if json is from an untrusted source.  See the
 	 * full warning at
 	 * {@link #retrieveTile(String, int, int, List, Alertable)}, which
 	 * completely applies here as well.</p>
 	 *
 	 * @param json
 	 * 		the JSON string to parse
 	 * @return
 	 * 		<tt>true</tt> if json represents a JavaScript
 	 * 		Array value, not a dictionary
 	 */
 	// TODO: This only works in GWT 2.1 and later:
 	// JSONValue parsed = JSONParser.parseStrict(json);
 
 	// This is still a hack, just less of a hack than before
 	private static native boolean isArray(String json) /*-{
 		eval("var obj = " + json);
 		return obj.__proto__["constructor"].toString().indexOf("Array") >= 0;
 	}-*/;
 
 	/**
 	 * Reads an array from json, which is assumed to be a JSON
 	 * representation of an array.
 	 *
 	 * <h2 style="color: red">WARNING:</h2>
 	 *
 	 * <p>This uses the JavaScript eval() function, which makes
 	 * it dangerous if json is from an untrusted source.  See the
 	 * full warning at
 	 * {@link #retrieveTile(String, int, int, List, Alertable)}, which
 	 * completely applies here as well.</p>
 	 *
 	 * @param json
 	 * 		the JSON string representing an array
 	 * @return
 	 * 		the array of the objects found in json
 	 */
 	private static native JsArray<JavaScriptObject> evalArray(String json) /*-{
 		eval("var arr = " + json);
 		return arr;
 	}-*/;
 
 	/**
 	 * Retrieves a tile from the specified URL.
 	 *
 	 * <p>Adds a tile retrieved from url into destination whenever that tile
 	 * arrives.  Since GWT only supports asynchronous server requests, there
 	 * is no clean way to make this method block until the object comes in.
 	 * Thus, this method takes a {@link java.util.List List}, into which
 	 * the GrapherTile is placed when received.</p>
 	 *
 	 * <h2 style="color: red">WARNING:</h2>
 	 *
 	 * <p>Note that the URL is assumed to be trusted.  This method uses
 	 * the JavaScript eval() function, <strong>which could allow arbitrary
 	 * code to execute on a user's browser</strong> if this URL is not to
 	 * a BodyTrack site over a secure connection.  This could allow an
 	 * attacker to view all of a user's data, simply by filling in code
 	 * to request all valid data tiles and then to send those tiles
 	 * to the attacker's machine.  As such, URLs for insecure connections,
 	 * and especially for other websites, should not be passed in as
 	 * the data parameter here.</p>
 	 *
 	 * @param url
 	 * 		the URL from which to retrieve the data
 	 * @param level
 	 * 		the level of the tile we are retrieving
 	 * @param offset
 	 * 		the offset of the tile we are retrieving
 	 * @param destination
 	 * 		the {@link java.util.List List} into which this method
 	 * 		will add the tile when the tile is loaded from the server
 	 * @param callback
 	 * 		an {@link org.bodytrack.client.Alertable<String>} that
 	 * 		is notified whenever the tile arrives, using a message
 	 * 		of the url parameter.  This url parameter is helpful
 	 * 		in notifications because it allows callback to
 	 * 		differentiate between several requested tiles.
 	 */
 	public static void retrieveTile(final String url,
 			final int level,
 			final int offset,
 			final List<GrapherTile> destination,
 			final Alertable<GrapherTile> callback) {
 		// Send request to server and catch any errors.
 		RequestBuilder builder =
 			new RequestBuilder(RequestBuilder.GET, url);
 
 		// This is simply to clean up code, so we don't construct the
 		// same tile in multiple places
 		final GrapherTile failureTile =
 			new GrapherTile(url, level, offset, "");
 
 		try {
 			builder.sendRequest(null, new RequestCallback() {
 				@Override
 				public void onError(Request request,
 						Throwable exception) {
 					callback.onFailure(failureTile);
 				}
 
 				@Override
 				public void onResponseReceived(Request request,
 						Response response) {
 					if (response.getStatusCode() == 200) {
 						GrapherTile successTile = new GrapherTile(url,
 							level,
 							offset,
 							response.getText());
 
 						callback.onSuccess(successTile);
 						destination.add(successTile);
 					} else
 						callback.onFailure(failureTile);
 				}
 			});
 		} catch (RequestException e) {
 			callback.onFailure(failureTile);
 		}
 	}
 
 	/**
 	 * Returns the URL used to create this <tt>GrapherTile</tt>.
 	 *
 	 * @return
 	 * 		the URL used to create this <tt>GrapherTile</tt>
 	 */
 	public String getUrl() {
 		return url;
 	}
 
 	/**
 	 * Returns the level used to create this <tt>GrapherTile</tt>.
 	 *
 	 * @return
 	 * 		the level passed to the constructor when this <tt>GrapherTile</tt>
 	 * 		was created
 	 */
 	public int getLevel() {
 		return level;
 	}
 
 	/**
 	 * Returns the offset used to create this <tt>GrapherTile</tt>.
 	 *
 	 * @return
 	 * 		the offset passed to the constructor when this
 	 * 		<tt>GrapherTile</tt> was created
 	 */
 	public int getOffset() {
 		return offset;
 	}
 
 	/**
 	 * Returns a description of the current tile.
 	 *
 	 * @return
 	 * 		a new {@link org.bodytrack.client.TileDescription TileDescription}
 	 * 		object representing the level and offset of this tile
 	 */
 	public TileDescription getDescription() {
 		return new TileDescription(level, offset);
 	}
 
 	/**
 	 * Returns a {@link org.bodytrack.client.PlottableTile PlottableTile}
 	 * object representing any (x, y) points in this <tt>GrapherTile</tt>,
 	 * or <tt>null</tt> if this <tt>GrapherTile</tt> contains no such
 	 * information.
 	 *
 	 * @return
 	 * 		the <tt>PlottableTile</tt> that was parsed out of the json
 	 * 		parameter sent to the constructor when this object was
 	 * 		initialized, or <tt>null</tt> if json was not a
 	 * 		<tt>PlottableTile</tt>
 	 */
 	public PlottablePointTile getPlottableTile() {
 		return tile;
 	}
 
 	/**
 	 * Returns a list of available PhotoDescription objects if
 	 * this tile contains photo descriptions, or <tt>null</tt>
 	 * otherwise.
 	 *
 	 * @return
 	 * 		a (possibly empty) List of PhotoDescriptions if
 	 * 		this tile contains photo descriptions, or <tt>null</tt>
 	 * 		otherwise
 	 */
 	public List<PhotoDescription> getPhotoDescriptions() {
 		return photoDescs;
 	}
 
 	/**
 	 * Returns <tt>true</tt> if and only if there is some data
 	 * available from this <tt>GrapherTile</tt> object.
 	 *
 	 * <p>If this returns <tt>true</tt>, then exactly one
 	 * of {@link GrapherTile#getPlottableTile()} and
 	 * {@link GrapherTile#getPhotoDescriptions()} will return a
 	 * non-<tt>null</tt> value.  If this returns <tt>false</tt>,
 	 * both methods will return <tt>null</tt> if called.</p>
 	 *
 	 * @return
 	 * 		<tt>true</tt> if there is some data available from
 	 * 		this object, <tt>false</tt> otherwise
 	 */
 	public boolean containsData() {
 		return tile != null || photoDescs != null;
 	}
 
 	/**
 	 * Returns a list of plottable points found in the plottable
 	 * tile this stores, or <tt>null</tt> if
 	 * {@link #getPlottableTile()} returns <tt>null</tt>.
 	 *
 	 * <p>This is exactly equivalent to
 	 * {@code getPlottableTile() == null ? null :
 	 * getPlottableTile().getDataPoints()}.</p>
 	 *
 	 * @return
 	 * 		a list of plottable points found in the plottable
 	 * 		tile this stores, or <tt>null</tt> if
 	 * 		{@link #getPlottableTile()} returns <tt>null</tt>.
 	 */
 	public List<PlottablePoint> getDataPoints() {
 		if (tile == null)
 			return null;
 
 		return tile.getDataPoints();
 	}
 }
