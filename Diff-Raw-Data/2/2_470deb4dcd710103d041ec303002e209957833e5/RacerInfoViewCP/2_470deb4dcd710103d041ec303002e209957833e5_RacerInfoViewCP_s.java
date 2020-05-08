 package com.gvccracing.android.tttimer.DataAccess;
 
 import java.util.Hashtable;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.net.Uri;
 import android.provider.BaseColumns;
 
 import com.gvccracing.android.tttimer.DataAccess.RacerCP.Racer;
 import com.gvccracing.android.tttimer.DataAccess.RacerClubInfoCP.RacerClubInfo;
 
 public class RacerInfoViewCP {
 	// BaseColumn contains _id.
     public static final class RacerInfoView implements BaseColumns {
 
         public static final Uri CONTENT_URI = Uri.withAppendedPath(TTProvider.CONTENT_URI, RacerInfoView.class.getSimpleName() + "~");
         
         public static String getTableName(){
         	return Racer.getTableName() + " JOIN " + RacerClubInfo.getTableName() + 
 					" ON (" + RacerClubInfo.getTableName() + "." + RacerClubInfo.Racer_ID + " = " + Racer.getTableName() + "._ID)";
         }
         
         public static String getCreate(){
         	return "";
         }
         
         public static Uri[] getAllUrisToNotifyOnChange(){
         	return new Uri[]{RacerInfoView.CONTENT_URI, Racer.CONTENT_URI, RacerClubInfo.CONTENT_URI};
         }
         
         public static Cursor Read(Context context, String[] fieldsToRetrieve, String selection, String[] selectionArgs, String sortOrder) {
 			return context.getContentResolver().query(RacerInfoView.CONTENT_URI, fieldsToRetrieve, selection, selectionArgs, sortOrder);
 		}
         
         public static Hashtable<String, Object> getValues(Context context, Long racerClubInfo_ID) {
 			Hashtable<String, Object> racerValues = new Hashtable<String, Object>();
 			
			Cursor racerCursor = RacerInfoView.Read(context, null, RacerClubInfo._ID + "=?", new String[]{Long.toString(racerClubInfo_ID)}, null);
 			if(racerCursor != null && racerCursor.getCount() > 0){
 				racerCursor.moveToFirst();
 				racerValues.put(RacerClubInfo._ID, racerClubInfo_ID);
 				racerValues.put(RacerClubInfo.Racer_ID, racerCursor.getLong(racerCursor.getColumnIndex(RacerClubInfo.Racer_ID)));
 				racerValues.put(RacerClubInfo.CheckInID, racerCursor.getString(racerCursor.getColumnIndex(RacerClubInfo.Racer_ID)));
 				racerValues.put(RacerClubInfo.Year, racerCursor.getLong(racerCursor.getColumnIndex(RacerClubInfo.Racer_ID)));
 				racerValues.put(RacerClubInfo.Category, racerCursor.getString(racerCursor.getColumnIndex(RacerClubInfo.Racer_ID)));
 				racerValues.put(RacerClubInfo.TTPoints, racerCursor.getLong(racerCursor.getColumnIndex(RacerClubInfo.Racer_ID)));
 				racerValues.put(RacerClubInfo.RRPoints, racerCursor.getLong(racerCursor.getColumnIndex(RacerClubInfo.Racer_ID)));
 				racerValues.put(RacerClubInfo.PrimePoints, racerCursor.getLong(racerCursor.getColumnIndex(RacerClubInfo.Racer_ID)));
 				racerValues.put(RacerClubInfo.RacerAge, racerCursor.getLong(racerCursor.getColumnIndex(RacerClubInfo.Racer_ID)));
 				racerValues.put(RacerClubInfo.GVCCID, racerCursor.getLong(racerCursor.getColumnIndex(RacerClubInfo.Racer_ID)));
 				racerValues.put(RacerClubInfo.Upgraded, racerCursor.getLong(racerCursor.getColumnIndex(RacerClubInfo.Racer_ID)));
 				racerValues.put(Racer.FirstName, racerCursor.getString(racerCursor.getColumnIndex(Racer.FirstName)));
 				racerValues.put(Racer.LastName, racerCursor.getString(racerCursor.getColumnIndex(Racer.LastName)));
 				racerValues.put(Racer.USACNumber, racerCursor.getLong(racerCursor.getColumnIndex(Racer.USACNumber)));
 				racerValues.put(Racer.BirthDate, racerCursor.getLong(racerCursor.getColumnIndex(Racer.BirthDate)));
 				racerValues.put(Racer.PhoneNumber, racerCursor.getLong(racerCursor.getColumnIndex(Racer.PhoneNumber)));
 				racerValues.put(Racer.EmergencyContactName, racerCursor.getString(racerCursor.getColumnIndex(Racer.EmergencyContactName)));
 				racerValues.put(Racer.EmergencyContactPhoneNumber, racerCursor.getLong(racerCursor.getColumnIndex(Racer.EmergencyContactPhoneNumber)));
 			}
 			if( racerCursor != null){
 				racerCursor.close();
 				racerCursor = null;
 			}
 			
 			return racerValues;
 		}
     }
 }
