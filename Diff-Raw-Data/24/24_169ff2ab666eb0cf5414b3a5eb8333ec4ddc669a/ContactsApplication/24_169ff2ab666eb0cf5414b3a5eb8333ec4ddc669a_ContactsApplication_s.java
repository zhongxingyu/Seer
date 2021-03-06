 /*
  * Copyright (C) 2010 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.android.contacts;
 
 import android.app.Application;
 import android.content.Context;
 import android.os.StrictMode;
 import android.preference.PreferenceManager;
 
import java.util.Locale;

import com.android.contacts.model.AccountTypes;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

 public final class ContactsApplication extends Application {
 
     @Override
     public void onCreate() {
         super.onCreate();
 
         // Priming caches to placate the StrictMode police
         Context context = getApplicationContext();
         PreferenceManager.getDefaultSharedPreferences(context);
        PhoneNumberUtil.getInstance().getAsYouTypeFormatter(Locale.getDefault().getCountry());
         AccountTypes.getInstance(context);
 
         StrictMode.setThreadPolicy(
                 new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());
     }
 }
