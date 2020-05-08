 /*
  ** Licensed under the Apache License, Version 2.0 (the "License");
  ** you may not use this file except in compliance with the License.
  ** You may obtain a copy of the License at
  **
  **     http://www.apache.org/licenses/LICENSE-2.0
  **
  ** Unless required by applicable law or agreed to in writing, software
  ** distributed under the License is distributed on an "AS IS" BASIS,
  ** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ** See the License for the specific language governing permissions and
  ** limitations under the License.
  */
 
 package com.google.code.geobeagle.activity;
 
import com.google.code.geobeagle.activity.main.GeoBeagleModule.DefaultSharedPreferences;
 import com.google.inject.Inject;
 import com.google.inject.Provider;
 
import android.content.SharedPreferences;
 
 public class ActivityDI implements Provider<ActivitySaver> {
 
     public static class ActivityTypeFactory {
         private final ActivityType mActivityTypes[] = new ActivityType[ActivityType.values().length];
 
         public ActivityTypeFactory() {
             for (ActivityType activityType : ActivityType.values())
                 mActivityTypes[activityType.toInt()] = activityType;
         }
 
         public ActivityType fromInt(int i) {
             return mActivityTypes[i];
         }
     }
 
    private final SharedPreferences sharedPreferences;
 
     @Inject
    ActivityDI(@DefaultSharedPreferences SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
     }
 
     @Override
     public ActivitySaver get() {
        return new ActivitySaver(sharedPreferences.edit());
     }
 
 }
