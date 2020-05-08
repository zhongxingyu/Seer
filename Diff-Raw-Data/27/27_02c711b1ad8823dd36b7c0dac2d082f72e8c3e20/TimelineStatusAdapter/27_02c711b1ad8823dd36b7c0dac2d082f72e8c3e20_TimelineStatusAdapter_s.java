 package isel.leic.pdm.dal;
 
 import isel.leic.pdm.TimelineApplication;
 
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 
 import winterwell.jtwitter.Twitter.Status;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 
 public class TimelineStatusAdapter extends DataBaseAdapter implements IDatabaseAccess<StatusData>
 {
 	private int maxRowsThreshold;
 	public static final String TABLE_NAME = "TIMELINE_STATUS";
 	public static final String STATUS_ID = "STATUS_ID";
 	public static final String TEXT = "TEXT";
 	public static final String DATE = "DATE";
 	public static final String USER = "USER";
 	public TimelineApplication app;
 	
 	public TimelineStatusAdapter(Context ctx)
 	{
 		super(ctx);
 		app = ((TimelineApplication) ctx);
 		maxRowsThreshold = app.getMaxLocalStorage();
 	}
 
 	public long insert(StatusData sd)
 	{
 		
 		ContentValues values = new ContentValues();
 
 		values.put(TEXT, sd._text);
		values.put(DATE, sd._date.toGMTString());
 		values.put(USER, sd._user);
 		//values.put(STATUS_ID,sd._id);
 		
 		long rowId = db.insert(TABLE_NAME, null, values);
 		
 		maxRowsThreshold = app.getMaxLocalStorage();
 		
 		if(rowId != -1 && rowId >= maxRowsThreshold)
 		{
 			db.delete(TABLE_NAME, STATUS_ID + " <= ?", new String[]{rowId-maxRowsThreshold+1 + ""});
 		}
 		
 		return rowId;
 	}
	
	public void insertAll(List<StatusData> l)
	{
		ContentValues cv = new ContentValues();
		
		for(StatusData s : l)
		{
			cv.put(STATUS_ID, s._id);
			cv.put(USER, s._user);
			cv.put(TEXT, s._text);
			cv.put(DATE, s._date.getTime());
			
			db.insert(TABLE_NAME, null, cv);
		}
	}
 
 	public List<StatusData> getAll()
 	{
 		LinkedList<StatusData> status = new LinkedList<StatusData>();
 		
 		Cursor c = db.query(TABLE_NAME, new String[]{STATUS_ID, USER, TEXT, DATE}, null, null, null, null, DATE + " DESC");
 		
 		if(!c.moveToFirst())
 		{
 			return new LinkedList<StatusData>();
 		}
 		
 		do
 		{
 			status.add(
 						new StatusData(c.getInt(c.getColumnIndex(STATUS_ID)),
 									   c.getString(c.getColumnIndex(TEXT)),
 									   c.getString(c.getColumnIndex(USER)),
									   new Date(c.getInt(c.getColumnIndex(DATE))))
 					  );
 		}
 		while(c.moveToNext());
 		
 		db.delete(TABLE_NAME, null, null); // Apagar todos os tuplos no fim
 		
 		return status;
 	}
 	
 	public Date getLastDate()
 	{
 		Cursor c = db.query(TABLE_NAME, new String[]{DATE}, null, null, null, null, DATE + " DESC");
 		
 		if(!c.moveToFirst())
 			return null;
 		
		return new Date(c.getInt(0));
 	}
 	
 	public void deleteAll()
 	{
 		db.delete(TABLE_NAME, null, null);
 	}
 	
 	public void delete(StatusData t)
 	{
 		db.delete(TABLE_NAME, "?=?", new String[]{STATUS_ID, ""+t._id});
 	}
 
 	public List<StatusData> join(List<Status> timelineMsgs)
 	{
 		if(!timelineMsgs.isEmpty()){
 			for(Status s : timelineMsgs){
				insert(new StatusData((int) s.id,s.text,s.user.name,s.createdAt));
 			}
 		}
 		return (List<StatusData>) getAll();
 	}
 }
