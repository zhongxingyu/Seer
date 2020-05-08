 package net.fhtagn.zoobgame;
 
 import android.net.Uri;
 import android.provider.BaseColumns;
 
 public final class Series implements BaseColumns {
 	private Series () {}
 	public static final Uri CONTENT_URI = Uri.parse("content://" + SerieContentProvider.AUTHORITY + "/series");
 	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/net.fhtagn.zoobgame.serie";
 	public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/net.fhtagn.zoobgame.serie";
 	
 	public static final String DEFAULT_SORT_ORDER = "last_modification DESC";
 	
 	//DB fields
 	public static final String ID = _ID;
 	public static final String JSON = "json";
 	public static final String IS_MINE = "is_mine"; //indicate whether the level has been created on this phone
 	public static final String PROGRESS = "progress";
 	public static final String COMMUNITY_ID = "community_id";
 
 	public static final String LAST_MODIFICATION = "last_modification"; //either the download date or the last modification using the editor
 	/** The following fields are only useful for series authored by the phone user */
 	public static final String UPLOAD_DATE = "upload_date";
 	
 	//"cache" fields
 	public static final String NAME = "name"; //used to cache name to avoid loading JSON each time
 	public static final String NUM_LEVELS = "num_levels"; //used to cache number of levels 
 	public static final String RATING = "rating"; //might be null if no rating
 	public static final String MY_RATING = "my_rating";
 	public static final String AUTHOR = "author";
 }
