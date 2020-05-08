 package com.task;
 
 import android.app.Application;
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
 import com.task.db.UserDbHelper;
 
 import java.util.Calendar;
 
 /**
  * @author Leus Artem
  * @since 03.06.13
  */
 public class TestApplication extends Application {
 
     private volatile UserDbHelper.UserEntity iAm;
 
     @Override
     public void onCreate() {
         super.onCreate();
 
         UserDbHelper userDb = new UserDbHelper(getBaseContext());
 
         // first launch case
         if(userDb.getAllUsers().isEmpty()){
             iAm = new UserDbHelper.UserEntity("SpongeBob", "SquarePants", Calendar.getInstance().getTime(),
                     "I'm bob, my pants is most likely square", "bob@underwater.sea", null);
             long selfId = userDb.addUser(iAm);
             SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
             preferences.edit().putLong(Constants.SELF_DB_ID, selfId).commit();
         }
 
         userDb.close();
     }
 
     public void saveMyself(Runnable onPostUpdateRunnable){
         UserDbHelper userDb = new UserDbHelper(this);
         userDb.update(iAm, onPostUpdateRunnable);
         userDb.close();
     }
 
     public UserDbHelper.UserEntity getMyself(){
         return iAm == null ? iAm = getMySelfAtDb() : iAm;
     }
 
     private UserDbHelper.UserEntity getMySelfAtDb(){
         UserDbHelper userDb = new UserDbHelper(this);
         long selfId = PreferenceManager.getDefaultSharedPreferences(this).getLong(Constants.SELF_DB_ID, -1);
         UserDbHelper.UserEntity iAm = userDb.findById(selfId);
         userDb.close();
         return iAm;
     }
 
 
 
 
 
 
 
 }
