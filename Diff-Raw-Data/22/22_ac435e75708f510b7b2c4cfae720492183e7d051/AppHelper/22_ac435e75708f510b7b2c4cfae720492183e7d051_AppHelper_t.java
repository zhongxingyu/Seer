 /*
     Montréal Just in Case
     Copyright (C) 2011  Mudar Noufal <mn@mudar.ca>
 
     Geographic locations of public safety services. A Montréal Open Data
     project.
 
     This file is part of Montréal Just in Case.
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package ca.mudar.mtlaucasou.utils;
 
 import ca.mudar.mtlaucasou.utils.Const.PrefsNames;
 import ca.mudar.mtlaucasou.utils.Const.PrefsValues;
 
 import android.app.Application;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.res.Configuration;
 import android.location.Location;
import android.util.Log;
 import android.widget.Toast;
 
 import java.util.Locale;
 
 //AppHelper mAppHelper = (AppHelper) getApplicationContext();
 //Activity: ((AppHelper)getApplication()).setBalance(9.99);
 
 public class AppHelper extends Application {
     protected static final String TAG = "AppHelper";
 
     private Location mLocation;
     private String mUnits;
     private String mListSort;
     private String mLanguage;
     private Toast mToast;
 
     public Location getLocation() {
         return mLocation;
     }
 
     public void setLocation(Location location) {
         this.mLocation = location;
     }
 
     public String getLanguage() {
         return mLanguage;
     }
 
     public void setLanguage(String lang) {
         this.mLanguage = lang;
         updateUiLanguage();
     }
 
     public void updateUiLanguage() {
 
         Locale locale = new Locale(mLanguage);
 
         Configuration config = new Configuration();
         config.locale = locale;
         Locale.setDefault(locale);
         getBaseContext().getResources().updateConfiguration(config,
                 getBaseContext().getResources().getDisplayMetrics());
     }
 
     public void showToastText(int res, int duration) {
         mToast.setText(res);
         mToast.setDuration(duration);
         mToast.show();
     }
 
     public void showToastText(String msg, int duration) {
         mToast.setText(msg);
         mToast.setDuration(duration);
         mToast.show();
     }
 
     @Override
     public void onCreate() {
         super.onCreate();
         instance = this;
 
         SharedPreferences prefs = getSharedPreferences(Const.APP_PREFS_NAME, Context.MODE_PRIVATE);
 
         /**
          * Initialize UI settings based on preferences.
          */
         mUnits = prefs.getString(PrefsNames.UNITS_SYSTEM, PrefsValues.UNITS_ISO);
 
        mListSort = prefs.getString(PrefsNames.LIST_SORT, PrefsValues.LIST_SORT_DISTANCE);
 
         mLanguage = prefs.getString(Const.PrefsNames.LANGUAGE, Locale.getDefault().getLanguage());
         if (!mLanguage.equals(PrefsValues.LANG_EN) && !mLanguage.equals(PrefsValues.LANG_FR)) {
             mLanguage = PrefsValues.LANG_EN;
         }
 
         mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
 
         updateUiLanguage();
     }
 
     public String getListSort() {
         return mListSort;
     }
 
     public void setListSort(String sort) {
         // Log.v(TAG, "setListSort = " + sort);
         this.mListSort = sort;
     }
 
     public String getUnits() {
         return mUnits;
     }
 
     public void setUnits(String units) {
         this.mUnits = units;
     }
 
     // TODO: verify possible memory leakage of the following code
     private static AppHelper instance = null;
 
     public static AppHelper getInstance() {
         checkInstance();
         return instance;
     }
 
     private static void checkInstance() {
         if (instance == null)
             throw new IllegalStateException("Application not created yet!");
     }
 
 }
