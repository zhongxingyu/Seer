 /*
  * Copyright (c) 2013. Zachary Dremann
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 
 package net.zdremann.wc;
 
 import android.app.AlarmManager;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.location.LocationManager;
 import android.net.ConnectivityManager;
 
 import com.google.analytics.tracking.android.EasyTracker;
 import com.google.analytics.tracking.android.GoogleAnalytics;
 import com.google.analytics.tracking.android.Tracker;
 
 import net.zdremann.wc.io.FakeIOModule;
 import net.zdremann.wc.io.IOModule;
 
 import javax.inject.Singleton;
 
 import dagger.Module;
 import dagger.Provides;
 
 @Module(
         injects = {
                 MyApplication.class
         },
         includes = {
                 IOModule.class,
                 FakeIOModule.class
         },
         library = true
 )
 public class ApplicationModule {
     private final MyApplication mApplication;
 
     public ApplicationModule(MyApplication application) {
         mApplication = application;
     }
 
     @Provides
     @ForApplication
     public Context provideApplicationContext() {
         return mApplication;
     }
 
     @Provides
     @Singleton
     GoogleAnalytics provideGoogleAnalytics(@ForApplication Context context) {
         return GoogleAnalytics.getInstance(context);
     }
 
     @Provides
     @Singleton
     Tracker provideGoogleTracker(@ForApplication Context context) {
         return EasyTracker.getTracker();
     }
 
     @Provides
     @Singleton
     public EasyTracker provideEasyTracker(@ForApplication Context context) {
         EasyTracker tracker = EasyTracker.getInstance();
         tracker.setContext(context);
         return tracker;
     }
 
     @Provides
     @Singleton
     LocationManager provideLocationManager(@ForApplication Context context) {
         return (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
     }
 
     @Provides
     @Singleton
     AlarmManager provideAlarmManager(@ForApplication Context context) {
         return (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
     }
 
     @Provides
     @Singleton
     @Main
     SharedPreferences provideMainSharedPreferences(@ForApplication Context context) {
         return context.getSharedPreferences("main", Context.MODE_PRIVATE);
     }
 
     @Provides
     @Singleton
     ContentResolver provideContentResolver(@ForApplication Context context) {
         return context.getContentResolver();
     }
 
     @Provides
     @Singleton
     ConnectivityManager provideConnectivityManager(@ForApplication Context context) {
         return (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
     }
 }
