 package ch.unibas.urz.android.vv.provider.db;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.net.Uri;
 import ch.unibas.urz.android.vv.helper.Logger;
 import ch.unibas.urz.android.vv.provider.VvContentProvider;
 
 public interface DB {
 
 	public static final String DATABASE_NAME = "vorlesungsverzeichnis-unibas";
 
 	public static final String NAME_ID = "_id";
 	public static final int INDEX_ID = 0;
 
 	public static final String SELECTION_BY_ID = NAME_ID + "=?";
 
 
 	public class OpenHelper extends SQLiteOpenHelper {
 
		private static final int DATABASE_VERSION = 4;
 
 		private static final String CREATE_ENTITY_TABLE = "create table if not exists " + VvEntity.TABLE_NAME + " (" + DB.NAME_ID + " integer primary key, "
 				+ VvEntity.NAME_ACS_ID + " long," + VvEntity.NAME_ACS_NUMBER + " text, " + VvEntity.NAME_ACS_CATEGORY + " text, " + VvEntity.NAME_ACS_TITLE + " text, "
 				+ VvEntity.NAME_ACS_CREDITPOINTS + " int," + VvEntity.NAME_ACS_OTYPE + " text, " + VvEntity.NAME_ACS_OBJID + " long," + VvEntity.NAME_ACS_SORT + " long,"
 				+ VvEntity.NAME_ACS_PARENT + " long," + VvEntity.NAME_PERIOD_ID + " long," + VvEntity.NAME_UPDATE_TIMESTAMP + " long, " + VvEntity.NAME_FAVORITE
 				+ " int DEFAULT 0)";
 
 		private static final String CREATE_DETAILS_TABLE = "create table if not exists " + VvDetails.TABLE_NAME + " (" + DB.NAME_ID + " integer primary key, "
 				+ VvDetails.NAME_ACS_OBJ_ID + " long," + VvDetails.NAME_TITEL + " text," + VvDetails.NAME_INHALT + " text," + VvDetails.NAME_AMUSTER + " text,"
 				+ VvDetails.NAME_OEINHEIT + " text," + VvDetails.NAME_NOTETIME + " text," + VvDetails.NAME_INTERVAL + " text," + VvDetails.NAME_STARTDATE + " long,"
 				+ VvDetails.NAME_ENDDATE + " long," + VvDetails.NAME_MODULE + " text," + VvDetails.NAME_FAKULTAT + " text," + VvDetails.NAME_USPRACHE + " text,"
 				+ VvDetails.NAME_SKALA + " text," + VvDetails.NAME_TVORAUSSETZUNG + " text," + VvDetails.NAME_WBELEGEN + " text," + VvDetails.NAME_ANABMELDUNG + " text,"
 				+ VvDetails.NAME_CREDITP + " text," + VvDetails.NAME_LPRUEF + " text," + VvDetails.NAME_WIEDERHOLUNGPRUEF + " text," + VvDetails.NAME_PRAESENZ + " text,"
 				+ VvDetails.NAME_BEMERKUNG + " text," + VvDetails.NAME_LECTURER + " text," + VvDetails.NAME_TIME_PLACE + " text," + VvDetails.NAME_UPDATE_TIMESTAMP + " long,"
 				+ VvDetails.NAME_PERIOD_ID + " long, " + VvDetails.NAME_TYPE + " text, " + VvDetails.NAME_LERNZIELE + " text, " + VvDetails.NAME_LITERATUR + " text, "
				+ VvDetails.NAME_HNOTE + " text, " + VvDetails.NAME_LINK + " text, " + VvDetails.NAME_LINKDESC + " text, " + VvDetails.NAME_VNR + " text, "
				+ VvDetails.NAME_ANMELDUNGL + " text);";
 		
 		public OpenHelper(Context context) {
 			super(context, DB.DATABASE_NAME, null, DATABASE_VERSION);
 		}
 
 		@Override
 		public void onCreate(SQLiteDatabase db) {
 			db.execSQL(CREATE_ENTITY_TABLE);
 			db.execSQL(CREATE_DETAILS_TABLE);
 			db.execSQL("create index idx_vventity_acs_id on " + VvEntity.TABLE_NAME + " (" + VvEntity.NAME_ACS_ID + "); ");
 			db.execSQL("create index idx_vventity_acs_parent on " + VvEntity.TABLE_NAME + " (" + VvEntity.NAME_ACS_PARENT + "); ");
 			db.execSQL("create index idx_vventity_favorites on " + VvEntity.TABLE_NAME + " (" + VvEntity.NAME_FAVORITE + "); ");
 			db.execSQL("create index idx_vventity_update on " + VvEntity.TABLE_NAME + " (" + VvEntity.NAME_UPDATE_TIMESTAMP + "); ");
 			db.execSQL("create index idx_vvdetails_acs_objid on " + VvDetails.TABLE_NAME + " (" + VvDetails.NAME_ACS_OBJ_ID + "); ");
 			db.execSQL("create index idx_vvdetails_period on " + VvDetails.TABLE_NAME + " (" + VvDetails.NAME_PERIOD_ID + "); ");
 			db.execSQL("create index idx_vvdetails_update on " + VvDetails.TABLE_NAME + " (" + VvDetails.NAME_UPDATE_TIMESTAMP + "); ");
 			Logger.i("Created tables ");
 		}
 
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			switch (oldVersion) {
 			case 1:
 				Logger.w("Upgrading to DB Version 2...");
 				db.execSQL("alter table " + VvEntity.TABLE_NAME + " add column " + VvEntity.NAME_FAVORITE + " int DEFAULT 0;");
 				db.execSQL("create index idx_vventity_favorites on " + VvEntity.TABLE_NAME + " (" + VvEntity.NAME_FAVORITE + "); ");
 				// nobreak
 			case 2:
 				Logger.w("Upgrading to DB Version 3...");
 				db.execSQL(CREATE_DETAILS_TABLE);
 				db.execSQL("create index idx_vventity_update on " + VvEntity.TABLE_NAME + " (" + VvEntity.NAME_UPDATE_TIMESTAMP + "); ");
 				db.execSQL("create index idx_vvdetails_acs_objid on " + VvDetails.TABLE_NAME + " (" + VvDetails.NAME_ACS_OBJ_ID + "); ");
 				db.execSQL("create index idx_vvdetails_update on " + VvDetails.TABLE_NAME + " (" + VvDetails.NAME_UPDATE_TIMESTAMP + "); ");
 				db.execSQL("create index idx_vvdetails_period on " + VvDetails.TABLE_NAME + " (" + VvDetails.NAME_PERIOD_ID + "); ");
 
			case 3:
				Logger.w("Upgrading to DB Version 4...");
				db.execSQL("alter table " + VvDetails.TABLE_NAME + " add column " + VvDetails.NAME_ANMELDUNGL + " text;");

 			default:
 				Logger.w("Finished DB upgrading!");
 				break;
 			}
 		}
 	}
 
 	public interface VvEntity {
 
 		public static final String TABLE_NAME = "entity";
 
 		public static final String CONTENT_ITEM_NAME = "entity";
 		public static String CONTENT_URI_STRING = "content://" + VvContentProvider.AUTHORITY + "/" + CONTENT_ITEM_NAME;
 		public static Uri CONTENT_URI = Uri.parse(CONTENT_URI_STRING);
 
 		static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + VvContentProvider.AUTHORITY + "." + CONTENT_ITEM_NAME;
 
 		static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + VvContentProvider.AUTHORITY + "." + CONTENT_ITEM_NAME;
 
 		public static final String NAME_ACS_ID = "ACS_ID";
 		public static final String NAME_ACS_NUMBER = "ACS_NUMBER";
 		public static final String NAME_ACS_CATEGORY = "ACS_CATEGORY";
 		public static final String NAME_ACS_TITLE = "ACS_TITLE";
 		public static final String NAME_ACS_CREDITPOINTS = "ACS_CREDITPOINTS";
 		public static final String NAME_ACS_OTYPE = "ACS_OTYPE";
 		// E -> Event Vorlesung
 		public static final String NAME_ACS_OBJID = "ACS_OBJID";
 		public static final String NAME_ACS_SORT = "ACS_SORT";
 		public static final String NAME_ACS_PARENT = "ACS_PARENT";
 		public static final String NAME_PERIOD_ID = "PERIOD_ID";
 		public static final String NAME_UPDATE_TIMESTAMP = "UPDATE_TIMESTAMP";
 		public static final String NAME_FAVORITE = "FAVORITE";
 
 		public static final int INDEX_ACS_ID = 1;
 		public static final int INDEX_ACS_NUMBER = 2;
 		public static final int INDEX_ACS_CATEGORY = 3;
 		public static final int INDEX_ACS_TITLE = 4;
 		public static final int INDEX_ACS_CREDITPOINTS = 5;
 		public static final int INDEX_ACS_OTYPE = 6;
 		public static final int INDEX_ACS_OBJID = 7;
 		public static final int INDEX_ACS_SORT = 8;
 		public static final int INDEX_ACS_PARENT = 9;
 		public static final int INDEX_PERIOD_ID = 10;
 		public static final int INDEX_UPDATE_TIMESTAMP = 11;
 		public static final int INDEX_FAVORITE = 12;
 
 		public static final String[] colNames = new String[] { NAME_ID, NAME_ACS_ID, NAME_ACS_NUMBER, NAME_ACS_CATEGORY, NAME_ACS_TITLE, NAME_ACS_CREDITPOINTS, NAME_ACS_OTYPE,
 				NAME_ACS_OBJID, NAME_ACS_SORT, NAME_ACS_PARENT, NAME_PERIOD_ID, NAME_UPDATE_TIMESTAMP, NAME_FAVORITE };
 
 		public static final String[] PROJECTION_DEFAULT = colNames;
 		public static final String[] PROJECTION_LIST = colNames;
 		public static final String[] PROJECTION_ACS_ID = new String[] { NAME_ACS_ID };
 
 		public static final String SELECTION_BY_PARENT_PERIOD = VvEntity.NAME_PERIOD_ID + "=? and " + VvEntity.NAME_ACS_PARENT + "=?";
 		public static final String SELECTION_BY_ACSID = VvEntity.NAME_ACS_ID + "=?";
 		public static final String SELECTION_BY_PARENT_PERIOD_NOT_UPDATE = VvEntity.NAME_PERIOD_ID + "=? and " + VvEntity.NAME_ACS_PARENT + "=? and not "
 				+ VvEntity.NAME_UPDATE_TIMESTAMP + "=?";
 		public static final String SELECTION_FAVORITES = VvEntity.NAME_FAVORITE + ">0";
 
 		public static final String SORTORDER_DEFAULT = NAME_ACS_SORT + " ASC";
 
 		public static final String SORTORDER_REVERSE = NAME_ACS_SORT + " DESC";
 
 		public static final String ACS_OTYPE_EVENT = "E";
 
 		public static final long VV_FIXED_ENTRY_ACS_ID = -42;
 		public static final String SELECTION_VV_FIXED_ENTRY = VvEntity.NAME_ACS_ID + "=" + VV_FIXED_ENTRY_ACS_ID;
 
 	}
 
 	public interface VvDetails {
 		public static final String TABLE_NAME = "details";
 
 		public static final String CONTENT_ITEM_NAME = "details";
 		public static String CONTENT_URI_STRING = "content://" + VvContentProvider.AUTHORITY + "/" + CONTENT_ITEM_NAME;
 		public static Uri CONTENT_URI = Uri.parse(CONTENT_URI_STRING);
 
 		static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + VvContentProvider.AUTHORITY + "." + CONTENT_ITEM_NAME;
 
 		static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + VvContentProvider.AUTHORITY + "." + CONTENT_ITEM_NAME;
 
 		public static final String NAME_ACS_OBJ_ID = "DID";
 		public static final String NAME_TITEL = "TITEL";
 		public static final String NAME_INHALT = "INHALT";
 		public static final String NAME_AMUSTER = "AMUSTER";
 		public static final String NAME_OEINHEIT = "OEINHEIT";
 		public static final String NAME_NOTETIME = "NOTETIME";
 		public static final String NAME_INTERVAL = "INTERVAL";
 		public static final String NAME_STARTDATE = "STARTDATE";
 		public static final String NAME_ENDDATE = "ENDDATE";
 		public static final String NAME_MODULE = "MODULE";
 		public static final String NAME_FAKULTAT = "ZUFA";
 		public static final String NAME_USPRACHE = "USPRACHE";
 		public static final String NAME_SKALA = "SKALA";
 		public static final String NAME_TVORAUSSETZUNG = "TVORAUSSETZUNG";
 		public static final String NAME_WBELEGEN = "WBELEGEN";
 		public static final String NAME_ANABMELDUNG = "ANABMELDUNG";
 		public static final String NAME_CREDITP = "CREDITP";
 		public static final String NAME_LPRUEF = "LPRUEF";
 		public static final String NAME_WIEDERHOLUNGPRUEF = "WIEDERHOLUNGPRUEF";
 		public static final String NAME_PRAESENZ = "PRAESENZ";
 		public static final String NAME_BEMERKUNG = "BEMERKUNG";
 		// special fields
 		public static final String NAME_LECTURER = "LECTURER";
 		public static final String NAME_TIME_PLACE = "TIME_PLACE";
 		public static final String NAME_UPDATE_TIMESTAMP = "UPDATE_TIMESTAMP";
 		public static final String NAME_PERIOD_ID = "PERIOD_ID";
 		// end fields
 		public static final String NAME_TYPE = "TYPE";
 		public static final String NAME_LERNZIELE = "LERNZIELE";
 		public static final String NAME_LITERATUR = "LITERATUR";
 		public static final String NAME_HNOTE = "HNOTE";
 		public static final String NAME_LINK = "LINK";
 		public static final String NAME_LINKDESC = "LINKDESC";
 		public static final String NAME_VNR = "VNR";
		public static final String NAME_ANMELDUNGL = "ANMELDUNGL";
 		
 		public static final int INDEX_ACS_OBJ_ID = 1;
 		public static final int INDEX_TITEL = 2;
 		public static final int INDEX_INHALT = 3;
 		public static final int INDEX_AMUSTER = 4;
 		public static final int INDEX_OEINHEIT = 5;
 		public static final int INDEX_NOTETIME = 6;
 		public static final int INDEX_INTERVAL = 7;
 		public static final int INDEX_STARTDATE = 8;
 		public static final int INDEX_ENDDATE = 9;
 		public static final int INDEX_MODULE = 10;
 		public static final int INDEX_FAKULTAT = 11;
 		public static final int INDEX_USPRACHE = 12;
 		public static final int INDEX_SKALA = 13;
 		public static final int INDEX_TVORAUSSETZUNG = 14;
 		public static final int INDEX_WBELEGEN = 15;
 		public static final int INDEX_ANABMELDUNG = 16;
 		public static final int INDEX_CREDITP = 17;
 		public static final int INDEX_LPRUEF = 18;
 		public static final int INDEX_WIEDERHOLUNGPRUEF = 19;
 		public static final int INDEX_PRAESENZ = 20;
 		public static final int INDEX_BEMERKUNG = 21;
 		public static final int INDEX_LECTURER = 22;
 		public static final int INDEX_TIME_PLACE = 23;
 		public static final int INDEX_UPDATE_TIMESTAMP = 24;
 		public static final int INDEX_PERIOD_ID = 25;
 		public static final int INDEX_TYPE = 26;
 		public static final int INDEX_LERNZIELE = 27;
 		public static final int INDEX_LITERATUR = 28;
 		public static final int INDEX_HNOTE = 29;
 		public static final int INDEX_LINK = 30;
 		public static final int INDEX_LINKDESC = 31;
 		public static final int INDEX_VNR = 32;
		public static final int INDEX_ANMELDUNGL = 33;
 
 		public static final String[] colNames = new String[] { NAME_ID, NAME_ACS_OBJ_ID, NAME_TITEL, NAME_INHALT, NAME_AMUSTER, NAME_OEINHEIT,
 				NAME_NOTETIME, NAME_INTERVAL, NAME_STARTDATE, NAME_ENDDATE, NAME_MODULE, NAME_FAKULTAT, NAME_USPRACHE, NAME_SKALA, NAME_TVORAUSSETZUNG, NAME_WBELEGEN,
 				NAME_ANABMELDUNG, NAME_CREDITP, NAME_LPRUEF, NAME_WIEDERHOLUNGPRUEF, NAME_PRAESENZ, NAME_BEMERKUNG, NAME_LECTURER, NAME_TIME_PLACE, NAME_UPDATE_TIMESTAMP,
				NAME_PERIOD_ID, NAME_TYPE, NAME_LERNZIELE, NAME_LITERATUR, NAME_HNOTE, NAME_LINK, NAME_LINKDESC, NAME_VNR, NAME_ANMELDUNGL };
 
 		public static final String[] PROJECTION_DEFAULT = colNames;
 		
 		public static final String SORTORDER_DEFAULT = INDEX_ACS_OBJ_ID + " ASC";
 
 		public static final String[] PROJECTION_ACS_ID = new String[] { NAME_ACS_OBJ_ID };
 
 		public static final String SELECTION_BY_ACSID = NAME_ACS_OBJ_ID + "=?";
 		public static final String SELECTION_BY_PERIOD_ACSID = NAME_PERIOD_ID + "=? and " + SELECTION_BY_ACSID;
 
 	}
 
 }
