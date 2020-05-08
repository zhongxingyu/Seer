 /* Scott Caruso
  * Java II 1308
  * Week 3 Assignment
  */
 package com.scottcaruso.datafunctions;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
import com.scottcaruso.mygov.MainActivity;

 import android.content.ContentProvider;
 import android.content.ContentValues;
 import android.content.UriMatcher;
 import android.database.Cursor;
 import android.database.MatrixCursor;
 import android.net.Uri;
 import android.provider.BaseColumns;
import android.widget.Toast;
 
 public class SavedPoliticianProvider extends ContentProvider{
 
 	public static final String AUTHORITY = "com.scottcaruso.myGov.savedpoliticianprovider";
 	public static String JSONString;
 	
 	public static class PoliticianData implements BaseColumns
 	{
 		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/politicians");
 		public static final Uri REPUBLICAN_URI = Uri.parse("content://" + AUTHORITY + "/politicians/Republicans");
 		public static final Uri DEMOCRAT_URI = Uri.parse("content://" + AUTHORITY + "/politicians/Democrats");
 		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.scaruso.mygov.politician";
 		
 		public static final String OBJECT_COLUMN = "poLObject";
 		public static final String NAME_COLUMN = "name";
 		public static final String PARTY_COLUMN = "party";
 		public static final String STATE_COLUMN = "state";
 		public static final String TERM_COLUMN = "term";
 		public static final String TWITTER_COLUMN = "twitter";
 		public static final String WEBSITE_COLUMN = "website";
 		
 		public static final String[] PROJECTION = { "_id", OBJECT_COLUMN,NAME_COLUMN,PARTY_COLUMN,STATE_COLUMN,TERM_COLUMN,TWITTER_COLUMN,WEBSITE_COLUMN};
 	
 		private PoliticianData() {};
 	}
 	
 	public static final int POLS = 1;
 	public static final int POLS_R = 2;
 	public static final int POLS_D = 3;
 	
 	private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
 	
 	static 
 	{
 		uriMatcher.addURI(AUTHORITY, "politicians/",POLS);
 		uriMatcher.addURI(AUTHORITY,"politicians/Republicans",POLS_R);
 		uriMatcher.addURI(AUTHORITY,"politicians/Democrats",POLS_D);
 	}
 	
 	@Override
 	public int delete(Uri uri, String selection, String[] selectionArgs) {
 		// TODO Auto-generated method stub
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public String getType(Uri uri) {
 		// TODO Auto-generated method stub
 		
 		return PoliticianData.CONTENT_TYPE;
 	}
 
 	@Override
 	public Uri insert(Uri uri, ContentValues values) {
 		// TODO Auto-generated method stub
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public boolean onCreate() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public Cursor query(Uri uri, String[] projection, String selection,
 			String[] selectionArgs, String sortOrder) {
 		
 		MatrixCursor result = new MatrixCursor(PoliticianData.PROJECTION);
 		JSONString = DataSingleton.getSavedPols();
 		if (JSONString == null)
 		{
 			return null;
 		}
 		JSONObject masterObject = null;
 		JSONArray polArray = null;
 		JSONObject polObject = null;
 		
 		try {
 			masterObject = new JSONObject(JSONString);
 			polArray = masterObject.getJSONArray("Politicians");
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		if (polArray == null)
 		{
 			return result;
 		}
 		
 		switch (uriMatcher.match(uri)) 
 		{
 		case POLS:
 			for (int x = 0; x < polArray.length(); x++)
 			{
 				try {
 					polObject = polArray.getJSONObject(x);
 					result.addRow(new Object[] { x + 1, polObject.toString(),polObject.get("Name"), polObject.get("Party"),polObject.get("State"),polObject.get("Term Start"),polObject.get("Twitter"),polObject.get("Website")		
 					});
 				} catch (JSONException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 			break;
 		case POLS_R:
 			for (int x = 0; x < polArray.length(); x++)
 			{
 				try {
 					polObject = polArray.getJSONObject(x);
 					String thisParty = polObject.get("Party").toString();
 					if (thisParty.equals("R"))
 					{
 						result.addRow(new Object[] { x + 1, polObject.toString(),polObject.get("Name"), polObject.get("Party"),polObject.get("State"),polObject.get("Term Start"),polObject.get("Twitter"),polObject.get("Website")		
 						});
 					}
 				} catch (JSONException e) {
 					e.printStackTrace();
 				}
 			}
 			break;
 		case POLS_D:
 			for (int x = 0; x < polArray.length(); x++)
 			{
 				try {
 					polObject = polArray.getJSONObject(x);
 					String thisParty = polObject.get("Party").toString();
 					if (thisParty.equals("D"))
 					{
 						result.addRow(new Object[] { x + 1, polObject.toString(),polObject.get("Name"), polObject.get("Party"),polObject.get("State"),polObject.get("Term Start"),polObject.get("Twitter"),polObject.get("Website")		
 						});
 					}
 				} catch (JSONException e) {
 					e.printStackTrace();
 				}
 			}
 			break;
 		}
 		return result;
 	}
 
 	@Override
 	public int update(Uri uri, ContentValues values, String selection,
 			String[] selectionArgs) {
 		// TODO Auto-generated method stub
 		throw new UnsupportedOperationException();
 	}
 
 }
