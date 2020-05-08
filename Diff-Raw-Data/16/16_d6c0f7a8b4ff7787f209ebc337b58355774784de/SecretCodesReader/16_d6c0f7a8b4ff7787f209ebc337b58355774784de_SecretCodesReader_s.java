 package com.shivgadhia.android.SecretCodeLauncher.persistance;
 
 import android.content.Context;
 import android.content.CursorLoader;
 import android.content.Loader;
 import android.database.Cursor;
 import android.net.Uri;
 import android.util.Log;
 import com.shivgadhia.android.SecretCodeLauncher.models.SecretCode;
 
 import java.util.ArrayList;
 
 public class SecretCodesReader {
     protected final DatabaseReader databaseReader;
 
     public SecretCodesReader(DatabaseReader databaseReader) {
         this.databaseReader = databaseReader;
     }
 
     public ArrayList<SecretCode> getAll() {
         Cursor cursor = getCursor();
         ArrayList<SecretCode> SecretCodes = populateListWith(cursor);
 
         cursor.close();
 
         return SecretCodes;
     }
 
     protected Cursor getCursor() {
         return databaseReader.getAllAndSortBy(Tables.TBL_SECRET_CODES, Tables.SecretCodes.COL_DATE_ADDED + " DESC");
     }
 
     private ArrayList<SecretCode> populateListWith(Cursor cursor) {
         ArrayList<SecretCode> data = new ArrayList<SecretCode>();
         if (cursor.moveToFirst()) {
             do {
                 SecretCode SecretCode = getPost(cursor);
                 data.add(SecretCode);
             } while (cursor.moveToNext());
         } else {
             Log.e("PostReader", "No data in the cursor.");
         }
         return data;
     }
 
     private SecretCode getPost(Cursor cursor) {
         String activityName = cursor.getString(cursor.getColumnIndexOrThrow(Tables.SecretCodes.COL_ACTIVITY_NAME));
         String secretCode = cursor.getString(cursor.getColumnIndexOrThrow(Tables.SecretCodes.COL_SECRET_CODE));
         return new SecretCode(activityName, secretCode);
     }
 
     public Loader<Cursor> getCursorLoader(Context context) {
        return new CursorLoader(context,Uri.parse(SecretCodesProvider.AUTHORITY + Tables.TBL_SECRET_CODES),null,null,null,null);
     }
 
 }
