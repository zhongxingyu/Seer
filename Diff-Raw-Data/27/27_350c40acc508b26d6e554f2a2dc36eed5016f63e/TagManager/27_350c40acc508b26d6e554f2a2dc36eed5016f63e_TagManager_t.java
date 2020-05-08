 package mobisocial.omnistanford.db;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.database.sqlite.SQLiteStatement;
 
 public class TagManager extends ManagerBase {
 	public static final String TAG = "AccountManager";
 
 	private static final int _id = 0;
 	private static final int name = 1;
 	private static final int locationId = 2;
 	private static final int startTime = 3;
 	private static final int endTime = 4;
 	private static final int checkinId = 5;
 
 	private static final String[] STANDARD_FIELDS = new String[] {
 		MTag.COL_ID,
 		MTag.COL_NAME,
 		MTag.COL_LOCATION_ID,
 		MTag.COL_START_TIME,
 		MTag.COL_END_TIME,
 		MTag.COL_CHECKIN_ID
 	};
 
 	private SQLiteStatement mInsertTag;
 	private SQLiteStatement mUpdateTag;
 
 	public TagManager(SQLiteDatabase db) {
 		super(db);
 	}
 	
 	public TagManager(SQLiteOpenHelper databaseSource) {
 		super(databaseSource);
 	}
 	
 	public void insertTag(MTag tag) {
         SQLiteDatabase db = initializeDatabase();
         if (mInsertTag == null) {
             synchronized(this) {
                 StringBuilder sql = new StringBuilder(" INSERT OR REPLACE INTO ")
                     .append(MTag.TABLE).append("(")
                     .append(MTag.COL_NAME).append(",")
                     .append(MTag.COL_LOCATION_ID).append(",")
                     .append(MTag.COL_START_TIME).append(",")
                     .append(MTag.COL_END_TIME).append(",")
                     .append(MTag.COL_CHECKIN_ID)
                     .append(") VALUES (?,?,?,?,?)");
                 mInsertTag = db.compileStatement(sql.toString());
             }
         }
         
         synchronized(mInsertTag) {
             bindField(mInsertTag, name, tag.name);
             bindField(mInsertTag, locationId, tag.locationId);
             bindField(mInsertTag, startTime, tag.startTime);
             bindField(mInsertTag, endTime, tag.endTime);
             bindField(mInsertTag, checkinId, tag.checkinId);
             
             tag.id = mInsertTag.executeInsert();
         }
     }
 	
 	public void updateTag(MTag tag) {
         SQLiteDatabase db = initializeDatabase();
         if (mUpdateTag == null) {
             synchronized(this) {
                 StringBuilder sql = new StringBuilder("UPDATE ")
                     .append(MTag.TABLE)
                     .append(" SET ")
                     .append(MTag.COL_NAME).append("=?,")
                     .append(MTag.COL_LOCATION_ID).append("=?,")
                     .append(MTag.COL_START_TIME).append("=?,")
                     .append(MTag.COL_END_TIME).append("=?,")
                     .append(MTag.COL_CHECKIN_ID).append("=?")
                     .append(" WHERE ").append(MTag.COL_ID).append("=?");
                 mUpdateTag = db.compileStatement(sql.toString());
             }
         }
         
         synchronized(mUpdateTag) {
         	bindField(mUpdateTag, name, tag.name);
         	bindField(mUpdateTag, locationId, tag.locationId);
         	bindField(mUpdateTag, startTime, tag.startTime);
         	bindField(mUpdateTag, endTime, tag.endTime);
         	bindField(mUpdateTag, checkinId, tag.checkinId);
         	bindField(mUpdateTag, 6, tag.id);
         	mUpdateTag.execute();
         }
     }
 	
 	public void ensureTag(MTag tag) {
 		MTag existing = getTag(tag.checkinId, tag.name);
 		if(existing != null) {
 			tag.id = existing.id;
 			updateTag(tag);
 		} else {
 			insertTag(tag);
 		}
 	}
 	
 	public MTag getTag(Long checkinId, String name) {
 		SQLiteDatabase db = initializeDatabase();
 		String table = MTag.TABLE;
 		String selection = MTag.COL_CHECKIN_ID + "=? AND " + 
 						MTag.COL_NAME + "=?";
 		String[] selectionArgs = { Long.toString(checkinId), name };
 		Cursor c = db.query(table, STANDARD_FIELDS, selection, selectionArgs, null, null, null);
 		try {
 			if(c.moveToFirst()) {
 				return fillInStandardFields(c);
 			} else {
 				return null;
 			}
 		} finally {
 			c.close();
 		}
 	}
 	
 	public List<MTag> getTags(Long checkinId) {
 		SQLiteDatabase db = initializeDatabase();
 		String table = MTag.TABLE;
 		String selection = MTag.COL_CHECKIN_ID + "=?";
 		String[] selectionArgs = { Long.toString(checkinId) };
 		Cursor c = db.query(table, STANDARD_FIELDS, selection, selectionArgs, null, null, null);
 		try {
 			List<MTag> result = new ArrayList<MTag>();
 			while(c.moveToNext()) {
 				result.add(fillInStandardFields(c));
 			} 
 			return result;
 		} finally {
 			c.close();
 		}
 	}
 	
 	public List<MTag> getDailyTags(Date day) {
 		Calendar calendar = Calendar.getInstance();
    	Long startOfToday, startOfYesterday, endOfToday, endOfTomorrow;
    	calendar.setTime(day);
    	calendar.set(Calendar.HOUR_OF_DAY, 24);
    	endOfToday = calendar.getTimeInMillis();
    	calendar.add(Calendar.DATE, 1);
    	endOfTomorrow = calendar.getTimeInMillis();
    	
    	calendar.setTime(day);
    	calendar.set(Calendar.HOUR_OF_DAY, 0);
    	startOfToday = calendar.getTimeInMillis();
    	calendar.add(Calendar.DATE, -1);
    	startOfYesterday = calendar.getTimeInMillis();
 		
         SQLiteDatabase db = initializeDatabase();
         String table = MTag.TABLE;
        String selection = MTag.COL_START_TIME + ">? AND " +
	    	MTag.COL_START_TIME + "<? AND " +
	    	MTag.COL_END_TIME + " >? AND " +
	    	MTag.COL_END_TIME + " <?";
        String[] selectionArgs = new String[] { startOfYesterday.toString(), endOfToday.toString(), 
        		startOfToday.toString(), endOfTomorrow.toString() };
         Cursor c = db.query(table, STANDARD_FIELDS, selection, selectionArgs, null, null, null);
     	List<MTag> tags = new ArrayList<MTag>();
         try {
         	while(c.moveToNext()) {
         		tags.add(fillInStandardFields(c));
         	}
         } finally {
         	c.close();
         }
 		
 		return tags;
 	}
 	
 	public List<MTag> getMonthlyTags(Date day) {
 		Calendar calendar = Calendar.getInstance();
 		calendar.setTime(day);
 		calendar.set(Calendar.HOUR_OF_DAY, 24);
 		Long end = calendar.getTimeInMillis();
 		calendar.set(Calendar.DAY_OF_MONTH, 1);
 		calendar.set(Calendar.HOUR_OF_DAY, 0);
 		Long start = calendar.getTimeInMillis();
 		
 		
         SQLiteDatabase db = initializeDatabase();
         String table = MTag.TABLE;
         String selection = MTag.COL_START_TIME + ">=? AND " + 
         				MTag.COL_END_TIME + "<=?";
         String[] selectionArgs = new String[] { Long.toString(start), Long.toString(end) };
         Cursor c = db.query(table, STANDARD_FIELDS, selection, selectionArgs, null, null, null);
     	List<MTag> tags = new ArrayList<MTag>();
         try {
         	while(c.moveToNext()) {
         		tags.add(fillInStandardFields(c));
         	}
         } finally {
         	c.close();
         }
 		
 		return tags;
 	}
 	
 	public List<MTag> getWeeklyTags(Date day) {
 		Calendar calendar = Calendar.getInstance();
 		calendar.setTime(day);
 		calendar.set(Calendar.HOUR_OF_DAY, 24);
 		Long end = calendar.getTimeInMillis();
 		calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
 		calendar.set(Calendar.HOUR_OF_DAY, 0);
 		Long start = calendar.getTimeInMillis();
 		
 		
         SQLiteDatabase db = initializeDatabase();
         String table = MTag.TABLE;
         String selection = MTag.COL_START_TIME + ">=? AND " + 
         				MTag.COL_END_TIME + "<=?";
         String[] selectionArgs = new String[] { Long.toString(start), Long.toString(end) };
         Cursor c = db.query(table, STANDARD_FIELDS, selection, selectionArgs, null, null, null);
     	List<MTag> tags = new ArrayList<MTag>();
         try {
         	while(c.moveToNext()) {
         		tags.add(fillInStandardFields(c));
         	}
         } finally {
         	c.close();
         }
 		
 		return tags;
 	}
 
 	private MTag fillInStandardFields(Cursor c) {
 		MTag tag = new MTag();
 		tag.id = c.getLong(_id);
 		tag.name = c.getString(name);
 		tag.locationId = c.getLong(locationId);
 		tag.startTime = c.getLong(startTime);
 		tag.endTime = c.getLong(endTime);
 		tag.checkinId = c.getLong(checkinId);
 		return tag;
 	}
 }
