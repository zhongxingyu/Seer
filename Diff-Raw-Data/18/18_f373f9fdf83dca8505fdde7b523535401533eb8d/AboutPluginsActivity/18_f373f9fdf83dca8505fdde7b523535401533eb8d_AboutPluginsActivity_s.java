 package com.starlon.froyvisuals;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.ViewGroup;
 import android.view.View;
 import android.view.LayoutInflater;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.ScrollView;
 import android.util.Log;
 
 
 public class AboutPluginsActivity extends Activity
 {
     private final static String TAG = "FroyVisuals/AboutPluginsActivity";
     private NativeHelper mHelper;
     private static TextView mLongName;
     private TextView mAuthor;
     private TextView mVersion;
     private TextView mAbout;
     private TextView mHelp;
     private TextView mLicense;
     private View mView;
     private View mWrapperView;
 
     private LinearLayout mLayout;
     private LinearLayout mWrapperLayout;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         final LayoutInflater factory = getLayoutInflater();
 
         mView = factory.inflate(R.layout.about_plugins, null);
 
         mWrapperView = factory.inflate(R.layout.about_plugins_wrapper, null);
 
         mWrapperLayout = (LinearLayout)mWrapperView.findViewById(R.id.info_plugin_wrapper_layout);
         mLayout = (LinearLayout)mView.findViewById(R.id.info_plugin_layout);
 
         mLongName = (TextView)mLayout.findViewById(R.id.info_long_name);
         mAuthor = (TextView)mLayout.findViewById(R.id.info_author);
         mVersion = (TextView)mLayout.findViewById(R.id.info_version);
         mAbout = (TextView)mLayout.findViewById(R.id.info_about);
         mHelp = (TextView)mLayout.findViewById(R.id.info_help);
        mLicense = (TextView)mLayout.findViewById(R.id.info_license);
 
 
         mWrapperLayout.addView(mLayout);
 
         setContentView(mWrapperView);
 
     }
 
     public void showActor(View view)
     {
         int current = mHelper.actorGetCurrent();
 
         if(current < 0)
         {
             Log.w(TAG, "No actor plugin available to display.");
             return;
         }
 
         String long_name_string = mHelper.actorGetLongName(current);
         String author_string = mHelper.actorGetAuthor(current);
         String version_string = mHelper.actorGetVersion(current);
         String about_string = mHelper.actorGetAbout(current);
         String help_string = mHelper.actorGetHelp(current);
        String license_string = mHelper.actorGetLicense(current);
 
         fillFields(long_name_string,author_string,version_string,
             about_string,help_string,license_string);
 
     }
 
     public void showInput(View view)
     {
         int current = mHelper.actorGetCurrent();
 
         if(current < 0)
         {
             Log.w(TAG, "No actor plugin available to display.");
             return;
         }
 
         String long_name_string = mHelper.inputGetLongName(current);
         String author_string = mHelper.inputGetAuthor(current);
         String version_string = mHelper.inputGetVersion(current);
         String about_string = mHelper.inputGetAbout(current);
         String help_string = mHelper.inputGetHelp(current);
        String license_string = mHelper.inputGetLicense(current);
 
         fillFields(long_name_string,author_string,version_string,
             about_string,help_string,license_string);

     }
 
     public void showMorph(View view)
     {
         int current = mHelper.actorGetCurrent();
 
         if(current < 0)
         {
             Log.w(TAG, "No actor plugin available to display.");
             return;
         }
 
         String long_name_string = mHelper.morphGetLongName(current);
         String author_string = mHelper.morphGetAuthor(current);
         String version_string = mHelper.morphGetVersion(current);
         String about_string = mHelper.morphGetAbout(current);
         String help_string = mHelper.morphGetHelp(current);
        String license_string = mHelper.morphGetLicense(current);
 
         fillFields(long_name_string,author_string,version_string,
             about_string,help_string,license_string);

     }
 
     public void fillFields(
         String long_name_string,
         String author_string,
         String version_string,
         String about_string,
         String help_string,
         String license_string)
     {
 
         if(long_name_string != null)
         {
             int len =long_name_string.length();
             char[]long_name_chars =long_name_string.toCharArray();
             mLongName.setText(long_name_chars, 0, len);
         }
 
         if(author_string != null)
         {
             int len = author_string.length();
             char[] author_chars = author_string.toCharArray();
             mAuthor.setText(author_chars, 0, len);
         }
 
         if(version_string != null)
         {
             int len = version_string.length();
             char[] version_chars = version_string.toCharArray();
             mVersion.setText(version_chars, 0, len);
         }
 
         if(about_string != null)
         {
             int len = about_string.length();
             char[] about_chars = about_string.toCharArray();
             mAbout.setText(about_chars, 0, len);
         }
 
         if(help_string != null)
         {
             int len = help_string.length();
             char[] help_chars = help_string.toCharArray();
             mHelp.setText(help_chars, 0, len);
         }
 
         if(license_string != null)
         {
             int len = license_string.length();
             char[] license_chars = license_string.toCharArray();
            mVersion.setText(license_chars, 0, len);
         }
     }
 }
