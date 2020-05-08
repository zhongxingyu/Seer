 /*
  * Copyright (C) 2011 The CyanogenMod Project
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
 
 package com.joy.launcher2.preference;
 
 import java.io.File;
 import java.util.List;
 
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceFragment;
 import android.preference.PreferenceScreen;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.ListAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.joy.launcher2.IconStyleActivity;
 import com.joy.launcher2.LauncherApplication;
 import com.joy.launcher2.R;
 import com.joy.launcher2.network.impl.ProtocalFactory;
 import com.joy.launcher2.network.impl.Service;
 import com.joy.launcher2.network.impl.Service.CallBack;
 import com.joy.launcher2.network.util.FormFile;
 import com.joy.launcher2.network.util.HttpRequestUtil;
 
 public class Preferences extends PreferenceActivity
         implements SharedPreferences.OnSharedPreferenceChangeListener,
         DialogInterface.OnClickListener{
 
     private static final String TAG = "Joy.Preferences";
 
     private SharedPreferences mPreferences;
     //add by yongjian.he for adapter sdk 16.
     private List<Header> mHeaders;
     //add by huangming for backup and recover function.
     long mHeaderId = -1;
     ProgressDialog mProgressDialog;
     //end
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         mPreferences = getSharedPreferences(PreferencesProvider.PREFERENCES_KEY,
                 Context.MODE_PRIVATE);
     }
  
     @Override
     protected void onResume() {
         super.onResume();
         mPreferences.registerOnSharedPreferenceChangeListener(this);
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         mPreferences.unregisterOnSharedPreferenceChangeListener(this);
     }
 
     @Override
     public void onBuildHeaders(List<Header> target) {
         loadHeadersFromResource(R.xml.preferences_headers, target);
         updateHeaders(target);
         mHeaders = target;
     }
 
     private void updateHeaders(List<Header> headers) {
         int i = 0;
         while (i < headers.size()) {
             Header header = headers.get(i);
 
             // Version preference
             if (header.id == R.id.preferences_application_version) {
                 header.title = getString(R.string.application_name) + " " + getString(R.string.application_version);
             }
 
             // Increment if not removed
             if (headers.get(i) == header) {
                 i++;
             }
         }
     }
 
     @Override
     public void setListAdapter(ListAdapter adapter) {
         if (adapter == null) {
             super.setListAdapter(null);
         } else {
 //            super.setListAdapter(new HeaderAdapter(this, getHeaders()));
             super.setListAdapter(new HeaderAdapter(this, mHeaders));
         }
     }
 
     @Override
     public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
         SharedPreferences.Editor editor = mPreferences.edit();
         editor.putBoolean(PreferencesProvider.PREFERENCES_CHANGED, true);
         editor.commit();
     }
 
     public static class HomescreenFragment extends PreferenceFragment {
         @Override
         public void onCreate(Bundle savedInstanceState) {
             super.onCreate(savedInstanceState);
 
             addPreferencesFromResource(R.xml.preferences_homescreen);
 
             PreferenceScreen preferenceScreen = getPreferenceScreen();
             if (LauncherApplication.isScreenLarge()) {
                 preferenceScreen.removePreference(findPreference("ui_homescreen_grid"));
             }
         }
 
 		@Override
 		public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
 				Preference preference) {
 			// TODO Auto-generated method stub
 			String key = preference.getKey();
 	    	if(PreferencesProvider.ICON_STYLE_KEY.equals(key))
 	    	{
 	    		Intent intent = new Intent();
 	    		intent.setClass(getActivity(), IconStyleActivity.class);
 	    		startActivity(intent);
 	    	}
 			return super.onPreferenceTreeClick(preferenceScreen, preference);
 		}
         
     }
 
     public static class DrawerFragment extends PreferenceFragment {
         @Override
         public void onCreate(Bundle savedInstanceState) {
             super.onCreate(savedInstanceState);
 
             addPreferencesFromResource(R.xml.preferences_drawer);
         }
     }
 
     public static class DockFragment extends PreferenceFragment {
         @Override
         public void onCreate(Bundle savedInstanceState) {
             super.onCreate(savedInstanceState);
 
             addPreferencesFromResource(R.xml.preferences_dock);
         }
     }
 
     public static class GeneralFragment extends PreferenceFragment {
         @Override
         public void onCreate(Bundle savedInstanceState) {
             super.onCreate(savedInstanceState);
 
             addPreferencesFromResource(R.xml.preferences_general);
         }
     }
     
     //add by huangming for backup and recover function.
     @Override
 	public void onHeaderClick(Header header, int position) {
 		// TODO Auto-generated method stub
 		super.onHeaderClick(header, position);
 		mHeaderId = header.id;
 		if(mHeaderId == R.id.preferences_backup_section)
 		{
 			showDialog(
 					R.string.preferences_bakcup_dialog_title, 
 					R.string.preferences_bakcup_message);
 		}
 		else if(mHeaderId == R.id.preferences_recover_section)
 		{
 			showDialog(
 					R.string.preferences_recover_dialog_title, 
 					R.string.preferences_recover_message);
 		}
 		
 	}
     
     private void showProgressDialog(int messageResId)
     {
     	if(mProgressDialog == null)
     	{
     		mProgressDialog = new ProgressDialog(this);
     	}
     	mProgressDialog.setMessage(getString(messageResId));
 		mProgressDialog.setCancelable(true);
     	mProgressDialog.show();
     }
     
     private void dismissProgressDialog()
     {
     	if(mProgressDialog != null)
     	{
     		mProgressDialog.dismiss();
     	}
     }
     
     private void showDialog(int titleResId, int messageResTd)
     {
     	Builder builder = new AlertDialog.Builder(this)
     	.setTitle(titleResId)
     	.setPositiveButton(R.string.backup_recover_sure, this)
     	.setNegativeButton(R.string.backup_recover_cancel, this)
     	.setMessage(messageResTd);
     	builder.create().show();
     }
     
 	@Override
 	public void onClick(DialogInterface dialog, int which) {
 		// TODO Auto-generated method stub
 		if(which == DialogInterface.BUTTON_POSITIVE)
 		{
 			Resources res = getResources();
 			boolean isOrdinaryUser = PreferencesProvider.getIsOrdinaryUser();
 			if(mHeaderId == R.id.preferences_backup_section)
 			{
 				boolean success = PreferencesProvider.setBackupMode(this);
         		if(success)
         		{
         			if(isOrdinaryUser)
         			{
         				display(R.string.backup_success);
         			}
         			else
         			{
         				try {
         					showProgressDialog(R.string.backup_desktop);
 							Service.getInstance().GotoNetwork(new MyCallback());
 						} catch (Exception e) {
 							// TODO Auto-generated catch block
 							dismissProgressDialog();
 							display(R.string.backup_failed);
 							e.printStackTrace();
 						}
         			}
         		}
         		else
         		{
         			display(R.string.backup_failed);
         		}
 			}
 			else if(mHeaderId == R.id.preferences_recover_section)
 			{
 				try {
 					Service.getInstance().GotoNetwork(new CallBack() {
 						
 						boolean success = false;
 						@Override
 						public void onPreExecute() {
							showProgressDialog(R.string.recover_desktop);
 						}
 						
 						@Override
 						public void onPostExecute() {
 							if(success)
 							{
 								display(R.string.recover_success);
 							}
 							else
 							{
 								display(R.string.recover_failed);
 							}
 							dismissProgressDialog();
 						}
 						@Override
 						public void doInBackground() {
 							success = PreferencesProvider.setRecoverMode(Preferences.this);
 						}
 					});
 				} catch (Exception e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 					dismissProgressDialog();
 					display(R.string.backup_failed);
 				}
 			}
 		}
 	}
 	
 	private void display(int messageResId)
 	{
 		Toast.makeText(this, messageResId, 0).show();
 	}
 	
 	class MyCallback implements Service.CallBack
 	{
 
 		boolean success = false;
 		
 		@Override
 		public void onPreExecute() {
 			// TODO Auto-generated method stub
 			//showProgressDialog(R.string.backup_desktop);
 		}
 
 		@Override
 		public void onPostExecute() {
 			// TODO Auto-generated method stub
 			dismissProgressDialog();
 			if(success)
 			{
 				display(R.string.backup_success);
 			}
 			else
 			{
 				display(R.string.backup_failed);
 			}
 		}
 
 		@Override
 		public void doInBackground() {
 			// TODO Auto-generated method stub
 			//network
 			
 			final SharedPreferences sp = getSharedPreferences(PreferencesProvider.PREFERENCES_KEY, 0);
 			String channel = sp.getString("channel", "");
 			if(channel.equals(""))
 			{
 				Log.e("Backup", "channel is wrong");
 				return;
 			}
 			/*Map<String, String> params = new HashMap<String, String>();  
 			String randomTS = Util.getTS();
 			String randomString = Util.randomString(6);
 			params.put("op", Integer.toString(ProtocalFactory.OP_BACKUP));
 			params.put("channel", channel);
 			params.put("sign", ProtocalFactory.getSign(randomTS, randomString));
 			params.put("sjz", ProtocalFactory.getSjz(randomString));*/
 			File file = getSharedPrefsFile(PreferencesProvider.PREFERENCES_BACKUP);
 			FormFile formFile = new FormFile(PreferencesProvider.PREFERENCES_BACKUP + ".xml", file, "xml", "text/xml");
 			try {
 				//success = HttpRequestUtil.post(ProtocalFactory.HOST_UPLOAD, params, formFile);
 				success = HttpRequestUtil.httpPostWithAnnex(ProtocalFactory.HOST_UPLOAD, channel, new FormFile[]{formFile});
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 	//end
 
 	private static class HeaderAdapter extends ArrayAdapter<Header> {
         private static final int HEADER_TYPE_NORMAL = 0;
         private static final int HEADER_TYPE_CATEGORY = 1;
 
         private static final int HEADER_TYPE_COUNT = HEADER_TYPE_CATEGORY + 1;
 
         private static class HeaderViewHolder {
             ImageView icon;
             TextView title;
             TextView summary;
         }
 
         private LayoutInflater mInflater;
 
         static int getHeaderType(Header header) {
             if (header.id == R.id.preferences_application_section ||
             		header.id == R.id.preferences_backupandrestore_section) {
                 return HEADER_TYPE_CATEGORY;
             } else {
                 return HEADER_TYPE_NORMAL;
             }
         }
 
         @Override
         public int getItemViewType(int position) {
             Header header = getItem(position);
             return getHeaderType(header);
         }
 
         @Override
         public boolean areAllItemsEnabled() {
             return false; // because of categories
         }
 
         @Override
         public boolean isEnabled(int position) {
             return getItemViewType(position) != HEADER_TYPE_CATEGORY;
         }
 
         @Override
         public int getViewTypeCount() {
             return HEADER_TYPE_COUNT;
         }
 
         @Override
         public boolean hasStableIds() {
             return true;
         }
 
         public HeaderAdapter(Context context, List<Header> objects) {
             super(context, 0, objects);
 
             mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         }
 
         @Override
         public View getView(int position, View convertView, ViewGroup parent) {
             HeaderViewHolder holder;
             Header header = getItem(position);
             int headerType = getHeaderType(header);
             View view = null;
 
             if (convertView == null) {
                 holder = new HeaderViewHolder();
                 switch (headerType) {
                     case HEADER_TYPE_CATEGORY:
 //                        view = new TextView(getContext(), null,
 //                                android.R.attr.listSeparatorTextViewStyle);
 //                    	holder.title = (TextView) view;
                     	view = mInflater.inflate(R.layout.joy_preference_category, parent, false);
 
                     	holder.title = (TextView)view.findViewById(com.android.internal.R.id.title);
                         break;
 
                     case HEADER_TYPE_NORMAL:
                         view = mInflater.inflate(
                                 R.layout.preference_header_item, parent,
                                 false);
                         holder.icon = (ImageView) view.findViewById(R.id.icon);
                         holder.title = (TextView)
                                 view.findViewById(com.android.internal.R.id.title);
 //                        holder.summary = (TextView)
 //                                view.findViewById(com.android.internal.R.id.summary);
                         break;
                 }
                 view.setTag(holder);
             } else {
                 view = convertView;
                 holder = (HeaderViewHolder) view.getTag();
             }
 
             // All view fields must be updated every time, because the view may be recycled
             switch (headerType) {
                 case HEADER_TYPE_CATEGORY:
                     holder.title.setText(header.getTitle(getContext().getResources()));
                     break;
 
                 case HEADER_TYPE_NORMAL:
                     holder.icon.setImageResource(header.iconRes);
                     holder.title.setText(header.getTitle(getContext().getResources()));
 //                    CharSequence summary = header.getSummary(getContext().getResources());
 //                    if (!TextUtils.isEmpty(summary)) {
 //                        holder.summary.setVisibility(View.VISIBLE);
 //                        holder.summary.setText(summary);
 //                    } else {
 //                        holder.summary.setVisibility(View.GONE);
 //                    }
                     break;
             }
 
             return view;
         }
     }
 
 }
