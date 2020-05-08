 /*
  * Copyright (C) 2013 The MoKee OpenSource Project
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
 
 package com.android.dialer.preference;
 
 import java.util.HashMap;
 import java.util.Set;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.preference.Preference;
 import android.preference.Preference.OnPreferenceClickListener;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceScreen;
 import android.provider.ContactsContract;
 import android.text.TextUtils;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnLongClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.Toast;
 
 import com.android.contacts.common.CallUtil;
 import com.android.dialer.DialtactsActivity;
 import com.android.dialer.R;
 
 public class SpeedDialPreferenceActivity extends PreferenceActivity implements
         OnPreferenceClickListener, OnItemLongClickListener {
 
     public static final String SPEED_DIAL = "speed_dial";
     private static final int PICK_CONTACT = 1;
     private SharedPreferences speedDialPrefs;
     private String speed_dial_num;
     private Context mContext;
     private Preference tmpPref;
 
     private int[] speedDialDrawable = {
             R.drawable.dial_num_2_wht,
             R.drawable.dial_num_3_wht, R.drawable.dial_num_4_wht,
             R.drawable.dial_num_5_wht, R.drawable.dial_num_6_wht,
             R.drawable.dial_num_7_wht, R.drawable.dial_num_8_wht,
             R.drawable.dial_num_9_wht
     };
 
     @Override
     protected void onCreate(Bundle arg0) {
         super.onCreate(arg0);
         ActionBar actionBar = getActionBar();
         if (actionBar != null) {
             // android.R.id.home will be triggered in onOptionsItemSelected()
             actionBar.setDisplayHomeAsUpEnabled(true);
         }
         mContext = this;
         speedDialPrefs = getSharedPreferences(SPEED_DIAL, Context.MODE_PRIVATE);
         Intent intent = this.getIntent();
         String name = intent.getStringExtra("name");
         String number = intent.getStringExtra("number");
         String type = intent.getStringExtra("type");
         String id = intent.getStringExtra("id");
         if (!TextUtils.isEmpty(name)) {
             String value = name + "\n" + number + "\n" + type;
             speedDialPrefs.edit().putString(SPEED_DIAL + id, value).apply();
             onBackPressed();
             Toast.makeText(this,
                     getString(R.string.speeddial_added, name, type, number, id),
                     Toast.LENGTH_LONG).show();
         } else {
             setPreferenceScreen(createPreferenceHierarchy(mContext));
         }
     }
 
     private PreferenceScreen createPreferenceHierarchy(Context mContext) {
         PreferenceScreen psLayout = getPreferenceManager()
                 .createPreferenceScreen(mContext);
         for (int i = 2; i < 10; i++) {
             PreferenceScreen preferenceScreen = new PreferenceScreen(mContext,
                     null);
             String value = speedDialPrefs.getString(SpeedDialPreferenceActivity.SPEED_DIAL + i,
                     null);
             preferenceScreen.setIcon(speedDialDrawable[i - 2]);
             if (value != null) {
                 String valueArray[] = value.split("\n");
                 preferenceScreen.setTitle(valueArray[0]);
                 preferenceScreen.setSummary(valueArray[2] + " : " + valueArray[1]);
                 preferenceScreen.setKey(valueArray[1].replaceAll(" ", ""));
                 preferenceScreen.setOnPreferenceClickListener(this);
             } else {
                 preferenceScreen.setTitle(R.string.speeddial_undefined_title);
                 preferenceScreen.setSummary(R.string.speeddial_undefined_summary);
             }
             psLayout.addPreference(preferenceScreen);
         }
         ListView listView = getListView();
         listView.setOnItemLongClickListener(this);
         return psLayout;
     }
 
     @Override
     public boolean onPreferenceClick(Preference pref) {
         String number = pref.getKey();
         Intent intent = CallUtil.getCallIntent(number, (mContext instanceof DialtactsActivity ?
                 ((DialtactsActivity) mContext).getCallOrigin() : null));
         startActivity(intent);
         return false;
     }
 
     @Override
     public boolean onItemLongClick(AdapterView<?> parent, View view, int position,
             long id) {
         ListView listView = (ListView) parent;
         ListAdapter listAdapter = listView.getAdapter();
         tmpPref = (PreferenceScreen) listAdapter.getItem(position);
         speed_dial_num = String.valueOf(position + 2);
         Intent intent = new Intent(Intent.ACTION_PICK,
                 ContactsContract.Contacts.CONTENT_URI);
         intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
         startActivityForResult(intent, PICK_CONTACT);
         return false;
     }
 
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         switch (requestCode) {
             case (PICK_CONTACT):
                 if (resultCode == Activity.RESULT_OK) {
                     Uri contactData = data.getData();
                     Cursor c = mContext.getContentResolver().query(contactData, null, null, null,
                             null);
                     if (c != null && c.moveToFirst()) {
                         String name = c.getString(c
                                 .getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                         String number = c.getString(c
                                 .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                         int typeID = c.getInt(c
                                 .getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                         String customLabel = c.getString(c
                                 .getColumnIndex(ContactsContract.CommonDataKinds.Phone.LABEL));
                         CharSequence type = ContactsContract.CommonDataKinds.Phone.getTypeLabel(
                                 mContext.getResources(), typeID, customLabel);
                         c.close();
                         String value = name + "\n" + number + "\n" + type;
                         speedDialPrefs.edit().putString(SPEED_DIAL + speed_dial_num, value).apply();
                         tmpPref.setTitle(name);
                         tmpPref.setSummary(type + " : " + number);
                         tmpPref.setKey(number.replaceAll(" ", ""));
                     }
                 }
         }
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         final int itemId = item.getItemId();
         if (itemId == android.R.id.home) { // See ActionBar#setDisplayHomeAsUpEnabled()
             onBackPressed();
             return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
 }
