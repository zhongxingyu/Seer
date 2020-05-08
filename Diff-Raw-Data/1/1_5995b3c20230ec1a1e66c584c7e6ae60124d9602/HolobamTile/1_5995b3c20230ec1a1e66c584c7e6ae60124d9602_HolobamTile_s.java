 package com.android.systemui.quicksettings;
 
 import android.content.ContentResolver;
 import android.content.Context;
 import android.net.Uri;
 import android.os.Handler;
 import android.provider.Settings;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnLongClickListener;
 
 import com.android.systemui.jellybam.Helpers;
 
 import com.android.systemui.R;
 import com.android.systemui.statusbar.phone.QuickSettingsController;
 import com.android.systemui.statusbar.phone.QuickSettingsContainerView;
 
 public class HolobamTile extends QuickSettingsTile {
 
     public HolobamTile(Context context, LayoutInflater inflater,
             QuickSettingsContainerView container, QuickSettingsController qsc, Handler handler) {
         super(context, inflater, container, qsc);
 
         mOnClick = new OnClickListener() {
             @Override
             public void onClick(View v) {
 		Settings.Secure.putInt(mContext.getContentResolver(),
                         Settings.Secure.UI_INVERTED_MODE, !getHoloBamState() ? 2 : 1);
	        Helpers.restartTrebuchet();
             }
         };
 
         mOnLongClick = new OnLongClickListener() {
             @Override
             public boolean onLongClick(View v) {
                 startSettingsActivity(Settings.ACTION_DISPLAY_SETTINGS);
                 return true;
             }
         };
         qsc.registerObservedContent(Settings.Secure.getUriFor(Settings.Secure.UI_INVERTED_MODE)
                 , this);
     }
 
     @Override
     public void updateResources() {
         updateTile();
         updateQuickSettings();
     }
 
     private synchronized void updateTile() {
         if(getHoloBamState()){
             mDrawable = R.drawable.ic_qs_jb_dark_on;
 	    mLabel = mContext.getString(R.string.quick_settings_holobam_on);
         }else{
             mDrawable = R.drawable.ic_qs_jb_dark_off;
             mLabel = mContext.getString(R.string.quick_settings_holobam_off);
         }
     }
 
     @Override
     void onPostCreate() {
         updateTile();
         super.onPostCreate();
     }
 
     private boolean getHoloBamState() {
         return Settings.Secure.getInt(mContext.getContentResolver(),
                 Settings.Secure.UI_INVERTED_MODE, 1) == 2;
     }
 
     @Override
     public void onChangeUri(ContentResolver resolver, Uri uri) {
         updateResources();
     }
 }
