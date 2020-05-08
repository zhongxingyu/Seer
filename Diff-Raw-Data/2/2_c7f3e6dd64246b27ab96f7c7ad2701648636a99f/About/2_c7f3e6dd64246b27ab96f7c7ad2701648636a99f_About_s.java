 /*
  * Copyright 2011 Maize Labs
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 
 package com.maize.CardMagic;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.net.Uri;
 import android.os.Bundle;
 import android.text.util.Linkify;
 import android.text.util.Linkify.TransformFilter;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 /**
  * About.java - The About activity that shows credits
  * 
  */
 public class About extends Activity {
     /* UI components */
     private ImageView mMaizeLogoImageView;
     private TextView mOpensourceTextView;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.about);
 
         /*
          *  Show name and version number in title
          */
         try {
             final PackageManager packageManager = getPackageManager();
             PackageInfo pinfo = packageManager.getPackageInfo(getPackageName(), 0);
             setTitle("About: Card Magic " + pinfo.versionName);
         } catch (NameNotFoundException e) {
             Log.e("NameNotFoundException", e.getMessage());
         }
 
         /*
          *  Link Maize Labs logo to maizelabs.com
          */
         mMaizeLogoImageView = (ImageView) findViewById(R.id.logo);
         mMaizeLogoImageView.setOnClickListener(new OnClickListener() {
             public void onClick(View v) {
                 // Load up the Maize Labs website
                 Intent intent = new Intent(Intent.ACTION_VIEW, Uri
                         .parse("http://maizelabs.com"));
                 startActivity(intent);
             }
         });
 
         /*
          *  Link the string "Google Code" and "github" to project hosting page
          */
         mOpensourceTextView = (TextView) findViewById(R.id.source_code);
 
         TransformFilter filter = new TransformFilter() {
             // A transform filter that simply returns just the text
             // captured by the first regular expression group.
             public final String transformUrl(final Matcher match, String url) {
                 return "";
             }
         };
 
 //        Pattern matcher = Pattern.compile("\\bGoogle Code\\b");
 //        String url = "http://code.google.com/p/android-card-magic/";
 //        Linkify.addLinks(mOpensourceTextView, matcher, url, null, filter);
 
        Pattern matcher = Pattern.compile("\\bgithub\\b");
         String url = "https://github.com/willhou/Card-Magic";
         Linkify.addLinks(mOpensourceTextView, matcher, url, null, filter);
     }
 }
