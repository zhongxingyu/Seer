 package com.hlidskialf.android.tonepicker;
 
 import android.app.ListActivity;
 import android.os.Bundle;
 import android.widget.ListView;
 import android.media.RingtoneManager;
 import android.media.Ringtone;
 import android.widget.SimpleCursorAdapter;
 
 import android.content.pm.ResolveInfo;
 import android.content.pm.PackageManager;
 import java.lang.Object;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Iterator;
 import android.widget.ArrayAdapter;
 import android.content.Intent;
 import android.content.Context;
 import android.content.ComponentName;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.LayoutInflater;
 import android.widget.TextView;
 import android.widget.ImageView;
 import android.net.Uri;
 import android.provider.Settings;
 
 import android.app.AlertDialog;
 import android.view.Menu;
 import android.view.MenuItem;
 
 public class TonePicker extends ListActivity
 {
     public static final int REQUEST_GET_CONTENT=1;
 
     private MenuItem mAboutItem;
 
     private PackageManager mPackageManager;
     private Intent mContentIntent, mPickerIntent, mInitialIntent;
     private int mFirstPickerPos;
     private ArrayList<Object> mList;
     private boolean mShowDefault,mShowSilent;
     private Uri mExistingUri, mDefaultUri;
     private Ringtone mExistingTone, mDefaultTone;
     private String mSilentString;
 
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.picker);
 
         mInitialIntent = getIntent();
         mExistingUri = mInitialIntent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI);
         mShowDefault = mInitialIntent.getBooleanExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
         mShowSilent = mInitialIntent.getBooleanExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
 
         mContentIntent = new Intent(Intent.ACTION_GET_CONTENT) .setType("audio/*") .addCategory(Intent.CATEGORY_OPENABLE);
         mPickerIntent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER).putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL);
 
         mSilentString = getString(R.string.silentLabel);
 
         mList = new ArrayList();
 
         if (mExistingUri != null) {
           mExistingTone = RingtoneManager.getRingtone(this, mExistingUri);
           mList.add( mExistingTone );
         }
         if (mShowDefault) {
           mDefaultUri = Settings.System.DEFAULT_RINGTONE_URI;
           mDefaultTone = RingtoneManager.getRingtone(this, mDefaultUri);
           mList.add( mDefaultTone );
         }
         if (mShowSilent) {
           mList.add( mSilentString );
         }
 
         mPackageManager = getPackageManager();
 
         mList.addAll( getActivities(mContentIntent, null) );
 
         mFirstPickerPos = mList.size();
 
         mList.addAll( getActivities(mPickerIntent, getComponentName()) );
 
         final LayoutInflater inflate = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         
         setListAdapter(new ArrayAdapter(this, R.layout.picker_item, mList) {
         /*
           public boolean isEnabled(int position) {
             Object o = mList.get(position);
             return !(String.class.equals(o.getClass()) && !mSilentString.equals((String)o));
           }
           */
           public View getView(int position, View view, ViewGroup parent) {
             //if (view == null) {
               view = inflate.inflate(R.layout.picker_item, parent, false);
            // }
            
             ImageView iv = (ImageView)view.findViewById(R.id.picker_item_icon);
             TextView txtmain = (TextView)view.findViewById(R.id.picker_item_text);
             TextView txtinfo = (TextView)view.findViewById(R.id.picker_item_info);
 
             Object o = mList.get(position);
             final Class cls = o.getClass();
             if (cls.equals(Ringtone.class)) {
               Ringtone tone = (Ringtone)o;
               String title = tone.getTitle(TonePicker.this);
               if (tone.equals(mExistingTone)) {
                 txtmain.setText( R.string.existingtone );
               }
               else if (tone.equals(mDefaultTone)) {
                 txtmain.setText( R.string.defaulttone );
               }
               txtinfo.setText( title );
             }
             else if (cls.equals(String.class)) {
               txtmain.setText( (String)o ); 
               if (mSilentString.equals((String)o))
                 iv.setImageResource( android.R.drawable.ic_lock_silent_mode );
               else {
                 iv.setVisibility(View.GONE);
                 txtmain.setGravity(android.view.Gravity.CENTER);
               }
             }
             else if (cls.equals(ResolveInfo.class)) {
               ResolveInfo ri = (ResolveInfo)o;
               iv.setImageDrawable( ri.loadIcon(mPackageManager) );
               txtmain.setText( ri.loadLabel(mPackageManager) );
             }
 
             return view;
           }
           
         });
  
     }
     @Override
     protected void onListItemClick(ListView l, View v, int position, long id) 
     {
         Object o = mList.get(position);
         final Class cls = o.getClass();
         if (cls.equals(ResolveInfo.class)) {
           ResolveInfo ri = (ResolveInfo)o;
           Intent i = (position < mFirstPickerPos) ? mContentIntent : mPickerIntent;
 
           i.setComponent( new ComponentName(ri.activityInfo.applicationInfo.packageName, ri.activityInfo.name) );
           startActivityForResult(i, REQUEST_GET_CONTENT);
          return;
         }
 
         Uri uri = null;
         if (cls.equals(Ringtone.class)) {
           Ringtone tone = (Ringtone)o;
           if (tone.equals(mExistingTone)) uri = mExistingUri;
           else if (tone.equals(mDefaultTone)) uri = mDefaultUri;
         }
         else if (cls.equals(String.class)) {
           uri = null;
         }
         finishWithUri(uri);
 
     }
     
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
       if (resultCode != RESULT_OK) {
         return;
       }
 
       if (requestCode == REQUEST_GET_CONTENT) { 
 
         Uri uri = data == null ? null : data.getData(); 
 
         if (uri == null) {
           uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
         }
 
         finishWithUri(uri);
       }
     }
 
 
     private void finishWithUri(Uri u)
     {
         Intent i = new Intent();
         i.putExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, u);
         setResult(RESULT_OK, i);
         finish();
     }
 
     private List<ResolveInfo> getActivities(Intent intent, ComponentName exclude) {
         List<ResolveInfo> list = mPackageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
         if (list == null) return null;
         if (exclude == null) return list;
         int N = list.size();
         for (int i=0; i<N; i++) {
             ResolveInfo ri = list.get(i);
             if (ri.activityInfo.packageName.equals(exclude.getPackageName())
                     || ri.activityInfo.name.equals(exclude.getClassName())) {
                 list.remove(i);
                 N--;
             }
         }
         return list;
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
         mAboutItem = menu.add(0, 0, 0, R.string.about);
         mAboutItem.setIcon(android.R.drawable.ic_menu_info_details);
  
         return true;
     }
  
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         if (item.equals(mAboutItem)) {
                 View v = getLayoutInflater().inflate(R.layout.about_dialog,null);
                 AlertDialog dia = new AlertDialog.Builder(this).
                                     setTitle(R.string.about_title).
                                     setView(v).
                                     setPositiveButton(R.string.splash_button_ok_label,null).
                                     create();
                 dia.show();
         }
  
         return false;
     }
 
 }
