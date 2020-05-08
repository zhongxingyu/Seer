 /*-
  * Copyright (C) 2009 Google Inc.
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
 
 package com.google.android.demos.jamendo.app;
 
 import com.google.android.demos.jamendo.net.JamendoCache;
 import com.google.android.demos.jamendo.provider.JamendoContract;
 import com.google.android.imageloader.BitmapContentHandler;
 import com.google.android.imageloader.ImageLoader;
 
 import android.app.Application;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.net.Uri;
 import android.os.Handler;
 import android.text.SpannableString;
 import android.text.method.LinkMovementMethod;
 import android.text.method.MovementMethod;
 import android.text.style.ClickableSpan;
 import android.text.style.URLSpan;
 import android.view.View;
 import android.widget.TextView;
 
 import java.net.ContentHandler;
 import java.net.URLStreamHandlerFactory;
 import java.util.List;
 
 public class JamendoApp extends Application {
 
     private static final int IMAGE_TASK_LIMIT = 3;
 
     // 50% of available memory, up to a maximum of 32MB
    private static final long IMAGE_CACHE_SIZE = Math.max(Runtime.getRuntime().maxMemory() / 2,
             32 * 1024 * 1024);
 
     public static final String DEFAULT_ARTIST_AVATAR = "http://imgjam.com/mandarine/layout/artiste_avatar.jpg";
 
     public static final String DEFAULT_USER_AVATAR_50 = "http://imgjam.com/avatars/questionmark/avatar.50.gif";
 
     public static final String DEFAULT_USER_AVATAR_100 = "http://imgjam.com/avatars/questionmark/avatar.100.gif";
 
     /**
      * Configures a {@link TextView} to host clickable links (as in
      * {@link android.text.util.Linkify}).
      */
     public static final void addLinkMovementMethod(TextView text) {
         MovementMethod method = text.getMovementMethod();
         if (!(method instanceof LinkMovementMethod)) {
             if (text.getLinksClickable()) {
                 method = LinkMovementMethod.getInstance();
                 text.setMovementMethod(method);
             }
         }
     }
 
     /**
      * Display a single link in a {@link TextView}.
      * 
      * @param textView the target {@link TextView}
      * @param linkText the text to linkify.
      * @param linkUri the link {@link Uri}.
      */
     public static final void setTextToLink(TextView textView, CharSequence linkText, Uri linkUri) {
         String url = linkUri.toString();
         SpannableString text = new SpannableString(linkText);
         Object span = new URLSpan(url);
         text.setSpan(span, 0, linkText.length(), 0);
         textView.setText(text);
         addLinkMovementMethod(textView);
     }
 
     public static final void setTextToLink(TextView textView, CharSequence linkText, Intent intent) {
         SpannableString text = new SpannableString(linkText);
         Object span = new IntentSpan(intent);
         text.setSpan(span, 0, linkText.length(), 0);
         textView.setText(text);
         addLinkMovementMethod(textView);
     }
 
     public static boolean isPlaylistStreamingSupported(Context context) {
         Intent intent = new Intent(Intent.ACTION_VIEW);
         Uri data = JamendoContract.createRadioUri(JamendoContract.FORMAT_M3U, 0L);
         String type = JamendoContract.CONTENT_TYPE_M3U;
         intent.setDataAndType(data, type);
         return isIntentSupported(context, intent);
     }
 
     public static boolean isIntentSupported(Context context, Intent intent) {
         PackageManager pm = context.getPackageManager();
         int flags = PackageManager.MATCH_DEFAULT_ONLY;
         List<ResolveInfo> activities = pm.queryIntentActivities(intent, flags);
         return !activities.isEmpty();
     }
 
     private static ImageLoader createImageLoader(Context context) {
         // Install the file cache (if it is not already installed)
         JamendoCache.install(context);
         
         // Just use the default URLStreamHandlerFactory because
         // it supports all of the required URI schemes (http).
         URLStreamHandlerFactory streamFactory = null;
 
         // Load images using a BitmapContentHandler
         // and cache the image data in the file cache.
         ContentHandler bitmapHandler = JamendoCache.capture(new BitmapContentHandler(), null);
 
         // For pre-fetching, use a "sink" content handler so that the
         // the binary image data is captured by the cache without actually
         // parsing and loading the image data into memory. After pre-fetching,
         // the image data can be loaded quickly on-demand from the local cache.
         ContentHandler prefetchHandler = JamendoCache.capture(JamendoCache.sink(), null);
 
         // Perform callbacks on the main thread
         Handler handler = null;
         
         return new ImageLoader(IMAGE_TASK_LIMIT, streamFactory, bitmapHandler, prefetchHandler,
                 IMAGE_CACHE_SIZE, handler);
     }
 
     private static class IntentSpan extends ClickableSpan {
 
         private final Intent mIntent;
 
         public IntentSpan(Intent intent) {
             super();
             mIntent = intent;
         }
 
         @Override
         public void onClick(View widget) {
             Context context = widget.getContext();
             context.startActivity(mIntent);
         }
     }
 
     private ImageLoader mImageLoader;
 
     @Override
     public void onCreate() {
         super.onCreate();
         mImageLoader = createImageLoader(this);
     }
     
     @Override
     public void onTerminate() {
         mImageLoader = null;
         super.onTerminate();
     }
 
     @Override
     public Object getSystemService(String name) {
         if (ImageLoader.IMAGE_LOADER_SERVICE.equals(name)) {
             return mImageLoader;
         } else {
             return super.getSystemService(name);
         }
     }
 }
