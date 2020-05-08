 package com.shivgadhia.android.SecretCodeLauncher.persistance;
 
 import android.content.ContentValues;
 import com.shivgadhia.android.SecretCodeLauncher.models.SecretCode;
 
 import java.util.ArrayList;
 
 public class SecretCodesWriter {
     private final DatabaseWriter databaseWriter;
 
     public SecretCodesWriter(DatabaseWriter databaseWriter) {
         this.databaseWriter = databaseWriter;
     }
 
     public void saveSecretCode(String activityName, String secretCode) {

         ContentValues values = new ContentValues();
         values.put(Tables.SecretCodes.COL_ACTIVITY_NAME, activityName);
         values.put(Tables.SecretCodes.COL_SECRET_CODE, secretCode);
         databaseWriter.saveDataToTable(Tables.TBL_SECRET_CODES, values);
     }
 
    public void setToDelete(SecretCode secretCode){
 
         ContentValues values = new ContentValues();
         values.put(Tables.SecretCodes.COL_ACTIVITY_NAME, secretCode.getActivityName());
         values.put(Tables.SecretCodes.COL_SECRET_CODE, secretCode.getSecretCode());
         values.put(Tables.SecretCodes.COL_HIDDEN, 1);
         databaseWriter.saveDataToTable(Tables.TBL_SECRET_CODES, values);
 
     }
 
     public void prune() {
        databaseWriter.delete(Tables.TBL_SECRET_CODES,Tables.SecretCodes.COL_HIDDEN+" =?", new String[]{"1"});
     }
 
     public void unDelete(ArrayList<Integer> deletedIds) {
        for(Integer id : deletedIds){
             ContentValues values = new ContentValues();
             values.put(Tables.SecretCodes.COL_HIDDEN, 0);
            databaseWriter.update(Tables.TBL_SECRET_CODES, values, Tables.SecretCodes.COL_ID+"=?", new String[]{id.toString()});
 
         }
     }
 }
