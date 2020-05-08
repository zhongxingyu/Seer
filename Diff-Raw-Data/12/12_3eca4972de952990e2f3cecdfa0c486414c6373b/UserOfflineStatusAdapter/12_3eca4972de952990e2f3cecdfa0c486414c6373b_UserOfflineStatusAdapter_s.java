 package isel.leic.pdm.dal;
 
 import isel.leic.pdm.TimelineApplication;
 
 import java.util.Collection;
 import java.util.LinkedList;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 
 public class UserOfflineStatusAdapter extends DataBaseAdapter implements IDatabaseAccess<StatusData>
 {
 	private int maxRowsThreshold;
 	private int currentRowId;
 	public static final String TABLE_NAME = "USER_STATUS";
 	public static final String STATUS_ID = "STATUS_ID";
 	public static final String TEXT = "TEXT";
 							
 	public UserOfflineStatusAdapter(Context ctx)
 	{
 		super(ctx);
 		maxRowsThreshold = ((TimelineApplication) ctx).getMaxLocalStorage();
 		currentRowId = 1;
 	}
 
 	public long insert(StatusData sd)
 	{
 		ContentValues values = new ContentValues();
 
		values.put(TEXT, sd.getText());
 		
 		long rowId = db.insert(TABLE_NAME, null, values);
 		
 		if(rowId != -1 && rowId >= maxRowsThreshold)
 		{
 			db.delete(TABLE_NAME, STATUS_ID + " = ?", new String[]{currentRowId + ""});
 			++currentRowId;
 		}
 		
 		return rowId;
 	}
 
 	public Collection<StatusData> getAll()
 	{
 		LinkedList<StatusData> status = new LinkedList<StatusData>();
 			
 		Cursor c = db.query(TABLE_NAME, new String[]{STATUS_ID, TEXT}, null, null, null, null, null);
 		
 		if(!c.moveToFirst())
 		{
 			return null;
 		}
 		
 		do
 		{
 			status.add(
						new StatusData(c.getString(DALUtils.getIndexForColumnName(c, TEXT)))
 					  );
 		}
 		while(c.moveToNext());
 		
 		db.delete(TABLE_NAME, null, null); // Apagar todos os tuplos no fim
 		
 		return status;
 	}
 }
