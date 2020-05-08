 package com.captech.capquake;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.BitSet;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Stack;
 import java.util.TimeZone;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.DefaultHttpClientConnection;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 import org.xmlpull.v1.XmlPullParserFactory;
 
 import android.content.ContentProvider;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.UriMatcher;
 import android.database.Cursor;
 import android.database.DatabaseUtils;
 import android.database.MatrixCursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.database.sqlite.SQLiteQueryBuilder;
 import android.net.Uri;
 import android.net.http.AndroidHttpClient;
 import android.util.Log;
 
 import com.captech.capquake.Quake.Quakes;
 import com.captech.capquake.Quake.RssQuakes;
 
 
 public class QuakeProvider extends ContentProvider {
 	private static final String TAG = "QuakeProvider";
 	
 	//private DefaultHttpClient mHttpClient;
 	private AndroidHttpClient mHttpClient;
 	
 	private static final int QUAKES = 1;
 	
 	private static HashMap<String, String> quakesProjectionMap;
 	
 	private static final UriMatcher sUriMatcher;
 	
 	private static final String DATABASE_NAME = "quakes.db";
 	
 	private static final String QUAKES_TABLE_NAME = "quakes";
 	
 	private static final int DATABASE_VERSION = 1;
 	
 	public static final String AUTHORITY = "com.captech.capquake.QuakeProvider";
 	
 	private static final String TIME_PATTERN = ".+<p>Date: ((Mon|Tue|Wed|Thu|Fri|Sat|Sun).+) UTC<br/>.+";
 	
 	private static final Pattern mPattern = Pattern.compile(TIME_PATTERN);
 	
 	private static Matcher matcher;
 	
 	private static final SimpleDateFormat sdf; 
 	
 	private static class DatabaseHelper extends SQLiteOpenHelper{
 
 		public DatabaseHelper(Context context) {
 			super(context, DATABASE_NAME, null, DATABASE_VERSION);
 		}
 
 		@Override
 		public void onCreate(SQLiteDatabase db) {
 			db.execSQL("CREATE TABLE " + QUAKES_TABLE_NAME + " (" + Quakes.QUAKE_ID 
 					+ " INTEGER PRIMARY KEY AUTOINCREMENT," + Quakes.TITLE + " VARCHAR(255),"
 					+ Quakes.LATITUDE + " NUMERIC," + Quakes.LONGITUDE + " NUMERIC," 
 					+ Quakes.LINK + " VARCHAR(255)," + Quakes.MAGNITUDE + " INTEGER,"
 					+ Quakes.TIME + " INTEGER UNIQUE);");
 		}
 
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			Log.w(TAG,"Upgrading database from version " + oldVersion + " to " + newVersion
 					+ ", which will destroy all data");
 			db.execSQL("DROP TABLE IF EXISTS " + QUAKES_TABLE_NAME);
 			onCreate(db);
 		}
 		
 	}
 	
 	private DatabaseHelper dbHelper;
 	
 	@Override
 	public int delete(Uri uri, String selection, String[] selectionArgs) {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public String getType(Uri uri) {
 		switch(sUriMatcher.match(uri))
 		{
 			case QUAKES:
 				return Quakes.CONTENT_TYPE;
 			
 			default:
 				throw new IllegalArgumentException("Unkown URI " + uri);
 		}
 	}
 
 	@Override
 	public Uri insert(Uri uri, ContentValues values) {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public boolean onCreate() {
 		dbHelper = new DatabaseHelper(getContext());
 		return true;
 	}
 
 	@Override
 	public Cursor query(Uri uri, String[] projection, String selection,
 			String[] selectionArgs, String sortOrder) {
 		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
 		
 		switch(sUriMatcher.match(uri)){
 			case QUAKES:
 				qb.setTables(QUAKES_TABLE_NAME);
 				qb.setProjectionMap(quakesProjectionMap);
 				break;
 			default:
 				throw new IllegalArgumentException("Unkown URI " + uri);
 		}
 		final String url = uri.getQueryParameter("url");
 		final Boolean reset = Boolean.parseBoolean(uri.getQueryParameter("reset"));
 		
 		SQLiteDatabase db = dbHelper.getWritableDatabase();
 		if (reset && url != null){
 			XmlPullParser parser = null;
 			mHttpClient = null;
 			parser = getUriXmlPullParser(url);
 	        if (parser != null) {
 	            XMLCursor xmlCursor = new XMLCursor(selection, projection);
 	            try {
 	                xmlCursor.parseWith(parser);
 	                xmlCursor.moveToFirst();
 	                while (xmlCursor.isAfterLast() == false){
 	                	String quakeTime = xmlCursor.getString(6);
 	                	matcher = mPattern.matcher(quakeTime);
 	                	boolean matchFound = matcher.find();
 	                	Date quakeDate;
 	                	if (matchFound){
 	                		quakeTime = matcher.group(1);
 	                		try {
 								quakeDate = sdf.parse(quakeTime);
 							} catch (ParseException e) {
 								Log.w(TAG, "Error parsing date", e);
 								quakeDate = new Date();
 							}
 	                	}
 	                	else
 	                		quakeDate = new Date();
 	                	ContentValues initialValues = new ContentValues();
 	                	initialValues.put(Quakes.TITLE, xmlCursor.getString(1));
 	                	initialValues.put(Quakes.LATITUDE, xmlCursor.getFloat(2));
 	                	initialValues.put(Quakes.LONGITUDE, xmlCursor.getFloat(3));
 	                	initialValues.put(Quakes.LINK, xmlCursor.getString(4));
 	                	initialValues.put(Quakes.MAGNITUDE, xmlCursor.getString(5));
 	                	initialValues.put(Quakes.TIME, quakeDate.getTime());
 						db.replace(QUAKES_TABLE_NAME, Quakes.TITLE, initialValues);
 						xmlCursor.moveToNext();
 	                }
 	                xmlCursor.close();
 	            } catch (IOException e) {
 	                Log.w(TAG, "I/O error while parsing XML " + uri, e);
 	            } catch (XmlPullParserException e) {
 	                Log.w(TAG, "Error while parsing XML " + uri, e);
 	            } finally {
 	                if (mHttpClient != null) {
 	                    mHttpClient.close();
 	                }
 	            }
 	        }
 		}
 		Cursor c = qb.query(db, projection, null, selectionArgs, null, null, sortOrder);
		db.close();
 		return c;
 	}
 
 	@Override
 	public int update(Uri uri, ContentValues values, String selection,
 			String[] selectionArgs) {
 		throw new UnsupportedOperationException();
 	}
 	
 	static{
 		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
 		sUriMatcher.addURI(AUTHORITY, QUAKES_TABLE_NAME, QUAKES);
 		
 		quakesProjectionMap = new HashMap<String, String>();
 		quakesProjectionMap.put(Quakes.QUAKE_ID, Quakes.QUAKE_ID);
 		quakesProjectionMap.put(RssQuakes.TITLE, Quakes.TITLE + " AS " + DatabaseUtils.sqlEscapeString(RssQuakes.TITLE));
 		quakesProjectionMap.put(RssQuakes.LATITUDE, Quakes.LATITUDE + " AS " + DatabaseUtils.sqlEscapeString(RssQuakes.LATITUDE));
 		quakesProjectionMap.put(RssQuakes.LONGITUDE, Quakes.LONGITUDE + " AS " + DatabaseUtils.sqlEscapeString(RssQuakes.LONGITUDE));
 		quakesProjectionMap.put(RssQuakes.LINK, Quakes.LINK + " AS " + DatabaseUtils.sqlEscapeString(RssQuakes.LINK));
 		quakesProjectionMap.put(RssQuakes.MAGNITUDE, Quakes.MAGNITUDE + " AS " + DatabaseUtils.sqlEscapeString(RssQuakes.MAGNITUDE));
 		quakesProjectionMap.put(RssQuakes.TIME, Quakes.TIME + " AS " + DatabaseUtils.sqlEscapeString(RssQuakes.TIME));
 		
 		sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss");
 		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
 	}
 
     protected XmlPullParser getUriXmlPullParser(String url) {
         XmlPullParser parser = null;
         try {
             XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
             factory.setNamespaceAware(true);
             parser = factory.newPullParser();
         } catch (XmlPullParserException e) {
             Log.e(TAG, "Unable to create XmlPullParser", e);
             return null;
         }
 
         InputStream inputStream = null;
         try {
             final HttpGet get = new HttpGet(url);
             //mHttpClient = new DefaultHttpClient();
             mHttpClient = AndroidHttpClient.newInstance("Android");
             HttpResponse response = mHttpClient.execute(get);
             if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                 final HttpEntity entity = response.getEntity();
                 if (entity != null) {
                     inputStream = entity.getContent();
                 }
             }
         } catch (IOException e) {
             Log.w(TAG, "Error while retrieving XML file " + url, e);
             return null;
         }
 
         try {
             parser.setInput(inputStream, null);
         } catch (XmlPullParserException e) {
             Log.w(TAG, "Error while reading XML file from " + url, e);
             return null;
         }
 
         return parser;
     }
     private static class XMLCursor extends MatrixCursor {
         private final Pattern mSelectionPattern;
         private Pattern[] mProjectionPatterns;
         private String[] mAttributeNames;
         private String[] mCurrentValues;
         private BitSet[] mActiveTextDepthMask;
         private final int mNumberOfProjections;
 
         public XMLCursor(String selection, String[] projections) {
             super(projections);
             // The first column in projections is used for the _ID
             mNumberOfProjections = projections.length - 1;
             mSelectionPattern = createPattern(selection);
             createProjectionPattern(projections);
         }
 
         private Pattern createPattern(String input) {
             String pattern = input.replaceAll("//", "/(.*/|)").replaceAll("^/", "^/") + "$";
             return Pattern.compile(pattern);
         }
 
         private void createProjectionPattern(String[] projections) {
             mProjectionPatterns = new Pattern[mNumberOfProjections];
             mAttributeNames = new String[mNumberOfProjections];
             mActiveTextDepthMask = new BitSet[mNumberOfProjections];
             // Add a column to store _ID
             mCurrentValues = new String[mNumberOfProjections + 1];
 
             for (int i=0; i<mNumberOfProjections; i++) {
                 mActiveTextDepthMask[i] = new BitSet();
                 String projection = projections[i + 1]; // +1 to skip the _ID column
                 int atIndex = projection.lastIndexOf('@', projection.length());
                 if (atIndex >= 0) {
                     mAttributeNames[i] = projection.substring(atIndex+1);
                     projection = projection.substring(0, atIndex);
                 } else {
                     mAttributeNames[i] = null;
                 }
 
                 // Conforms to XPath standard: reference to local context starts with a .
                 if (projection.charAt(0) == '.') {
                     projection = projection.substring(1);
                 }
                 mProjectionPatterns[i] = createPattern(projection);
             }
         }
 
         public void parseWith(XmlPullParser parser) throws IOException, XmlPullParserException {
             StringBuilder path = new StringBuilder();
             Stack<Integer> pathLengthStack = new Stack<Integer>();
 
             // There are two parsing mode: in root mode, rootPath is updated and nodes matching
             // selectionPattern are searched for and currentNodeDepth is negative.
             // When a node matching selectionPattern is found, currentNodeDepth is set to 0 and
             // updated as children are parsed and projectionPatterns are searched in nodePath.
             int currentNodeDepth = -1;
 
             // Index where local selected node path starts from in path
             int currentNodePathStartIndex = 0;
 
             int eventType = parser.getEventType();
             while (eventType != XmlPullParser.END_DOCUMENT) {
 
                 if (eventType == XmlPullParser.START_TAG) {
                     // Update path
                     pathLengthStack.push(path.length());
                     path.append('/');
                     String prefix = null;
                     try {
                         // getPrefix is not supported by local Xml resource parser
                         prefix = parser.getPrefix();
                     } catch (RuntimeException e) {
                         prefix = null;
                     }
                     if (prefix != null) {
                         path.append(prefix);
                         path.append(':');
                     }
                     path.append(parser.getName());
 
                     if (currentNodeDepth >= 0) {
                         currentNodeDepth++;
                     } else {
                         // A node matching selection is found: initialize child parsing mode
                         if (mSelectionPattern.matcher(path.toString()).matches()) {
                             currentNodeDepth = 0;
                             currentNodePathStartIndex = path.length();
                             mCurrentValues[0] = Integer.toString(getCount()); // _ID
                             for (int i = 0; i < mNumberOfProjections; i++) {
                                 // Reset values to default (empty string)
                                 mCurrentValues[i + 1] = "";
                                 mActiveTextDepthMask[i].clear();
                             }
                         }
                     }
 
                     // This test has to be separated from the previous one as currentNodeDepth can
                     // be modified above (when a node matching selection is found).
                     if (currentNodeDepth >= 0) {
                         final String localNodePath = path.substring(currentNodePathStartIndex);
                         for (int i = 0; i < mNumberOfProjections; i++) {
                             if (mProjectionPatterns[i].matcher(localNodePath).matches()) {
                                 String attribute = mAttributeNames[i];
                                 if (attribute != null) {
                                     mCurrentValues[i + 1] =
                                         parser.getAttributeValue(null, attribute);
                                 } else {
                                     mActiveTextDepthMask[i].set(currentNodeDepth, true);
                                 }
                             }
                         }
                     }
 
                 } else if (eventType == XmlPullParser.END_TAG) {
                     // Pop last node from path
                     final int length = pathLengthStack.pop();
                     path.setLength(length);
 
                     if (currentNodeDepth >= 0) {
                         if (currentNodeDepth == 0) {
                             // Leaving a selection matching node: add a new row with results
                             addRow(mCurrentValues);
                         } else {
                             for (int i = 0; i < mNumberOfProjections; i++) {
                                 mActiveTextDepthMask[i].set(currentNodeDepth, false);
                             }
                         }
                         currentNodeDepth--;
                     }
 
                 } else if ((eventType == XmlPullParser.TEXT) && (!parser.isWhitespace())) {
                     for (int i = 0; i < mNumberOfProjections; i++) {
                         if ((currentNodeDepth >= 0) &&
                             (mActiveTextDepthMask[i].get(currentNodeDepth))) {
                             mCurrentValues[i + 1] += parser.getText();
                         }
                     }
                 }
 
                 eventType = parser.next();
             }
         }
     }
 }
