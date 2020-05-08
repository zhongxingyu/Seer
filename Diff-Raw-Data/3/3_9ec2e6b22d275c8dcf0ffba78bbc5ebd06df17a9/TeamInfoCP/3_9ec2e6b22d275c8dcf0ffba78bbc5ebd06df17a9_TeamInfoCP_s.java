 package com.gvccracing.android.tttimer.DataAccess;
 
 import java.util.Hashtable;
 
 import com.gvccracing.android.tttimer.DataAccess.TeamCheckInViewCP.TeamCheckInViewExclusive;
 import com.gvccracing.android.tttimer.DataAccess.TeamCheckInViewCP.TeamCheckInViewInclusive;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.net.Uri;
 import android.provider.BaseColumns;
 
 public class TeamInfoCP {
 
     // BaseColumn contains _id.
     public static final class TeamInfo implements BaseColumns {
 
         public static final Uri CONTENT_URI = Uri.withAppendedPath(TTProvider.CONTENT_URI, TeamInfo.class.getSimpleName() + "~");
 
         // Table column
         public static final String TeamName = "TeamName";
         public static final String TeamCategory = "TeamCategory";
         public static final String Year = "Year";
         
         public static String getTableName(){
         	return TeamInfo.class.getSimpleName();
         }
         
         public static String getCreate(){
         	return "create table " + TeamInfo.getTableName()
         	        + " (" + _ID + " integer primary key autoincrement, "
         	        + TeamName + " text not null, " 
         	        + TeamCategory + " text not null, " 
         	        + Year + " integer not null);";
         }
         
         public static Uri[] getAllUrisToNotifyOnChange(){
         	return new Uri[]{TeamInfo.CONTENT_URI, TeamCheckInViewInclusive.CONTENT_URI, TeamCheckInViewExclusive.CONTENT_URI};
         }
 
 		public static Uri Create(Context context, String teamName, String teamCategory) {
 			ContentValues content = new ContentValues();
 	     	content.put(TeamInfo.TeamName, teamName);
 	     	content.put(TeamInfo.TeamCategory, teamCategory);
 
 	     	return context.getContentResolver().insert(TeamInfo.CONTENT_URI, content);
 		}
 		
 		public static Cursor Read(Context context, String[] fieldsToRetrieve, String selection, String[] selectionArgs, String sortOrder) {
 			return context.getContentResolver().query(TeamInfo.CONTENT_URI, fieldsToRetrieve, selection, selectionArgs, sortOrder);
 		}
 
 		public static Hashtable<String, Object> getValues(Context context, Long teamInfo_ID) {
 			Hashtable<String, Object> teamInfoValues = new Hashtable<String, Object>();
 			
 			Cursor teamCursor = TeamInfo.Read(context, null, TeamInfo._ID + "=?", new String[]{Long.toString(teamInfo_ID)}, null);
 			if(teamCursor != null && teamCursor.getCount() > 0){
 				teamCursor.moveToFirst();
 				teamInfoValues.put(TeamInfo._ID, teamInfo_ID);
 				teamInfoValues.put(TeamInfo.TeamName, teamCursor.getString(teamCursor.getColumnIndex(TeamInfo.TeamName)));
 				teamInfoValues.put(TeamInfo.TeamCategory, teamCursor.getString(teamCursor.getColumnIndex(TeamInfo.TeamCategory)));
 				teamInfoValues.put(TeamInfo.Year, teamCursor.getLong(teamCursor.getColumnIndex(TeamInfo.Year)));
 			}
 			if( teamCursor != null){
 				teamCursor.close();
 				teamCursor = null;
 			}
 			
 			return teamInfoValues;
 		}
     }
 }
