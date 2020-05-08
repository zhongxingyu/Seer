 /*
  * Copyright (C) 2011 The original author or authors.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
  * in compliance with the License. You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software distributed under the License
  * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
  * or implied. See the License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package com.zapta.apps.maniana.help;
 
 import static com.zapta.apps.maniana.util.Assertions.check;
 import static com.zapta.apps.maniana.util.Assertions.checkNotNull;
 
 import java.io.InputStream;
 import java.util.Hashtable;
 
 import javax.annotation.Nullable;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.PackageInfo;
 import android.graphics.Picture;
 import android.graphics.drawable.GradientDrawable;
 import android.os.Bundle;
 import android.view.View;
 import android.view.Window;
 import android.webkit.WebView;
 import android.webkit.WebView.PictureListener;
 
 import com.zapta.apps.maniana.R;
 import com.zapta.apps.maniana.annotations.ActivityScope;
 import com.zapta.apps.maniana.util.DisplayUtil;
 import com.zapta.apps.maniana.util.FileUtil;
 import com.zapta.apps.maniana.util.FileUtil.FileReadResult;
 import com.zapta.apps.maniana.util.FileUtil.FileReadResult.FileReadOutcome;
 import com.zapta.apps.maniana.util.LogUtil;
 import com.zapta.apps.maniana.util.PackageUtil;
 import com.zapta.apps.maniana.util.TextUtil;
 
 /**
  * Shows a popup message such as startup splash. The message is taken from an HTML asset page.
  * 
  * @author Tal Dayan.
  */
 @ActivityScope
 public class PopupMessageActivity extends Activity {
 
     private static final int BORDER_WIDTH_DIP = 5;
 
     private static final String INTENT_MESSAGE_KIND_KEY = "com.zapta.apps.maniana.messageKind";
 
     public static enum MessageKind {
         ABOUT("help/about", ".html", true, 0),
         NEW_USER("help/new_user_welcome", ".html", false, 0xff00bb00),
         WHATS_NEW("help/whats_new", ".html", false, 0xff00ccff),
         BACKUP_RESTORE("help/restore_backup", ".html", false, 0xff0000ff);
 
         // TODO(tal): since all messages come from assets, have the names more asset specific.
         private final String mAssetRelativeBaseName;
         private final String mAssetExtension;
         private final boolean mIsFullScreen;
         private final int mFrameColor;
 
         private MessageKind(String assetRelativeBaseName, String assetExtension,
                 boolean isFulLScreen, int frameColor) {
             this.mAssetRelativeBaseName = assetRelativeBaseName;
             this.mAssetExtension = assetExtension;
             this.mIsFullScreen = isFulLScreen;
             this.mFrameColor = frameColor;
         }
     }
 
     // TODO: move to FileUtil?
     public final static String ASSETS_BASE_URL = "file:///android_asset/";
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 
         // Get message kind from intent
         final MessageKind messageKind = parseMessageKind();
         if (messageKind == null) {
             finish();
             return;
         }
 
         if (messageKind.mIsFullScreen) {
             onCreateFullScreen(messageKind);
             return;
         }
 
         onCreateSmallLayout(messageKind);
     }
 
     private final void onCreateFullScreen(MessageKind messageKind) {
         check(messageKind.mIsFullScreen, messageKind.toString());
         setContentView(R.layout.message_full_screen_layout);
 
         final WebView webview = (WebView) findViewById(R.id.message_full_screen_webview);
 
         displayFromAsset(webview, messageKind);
     }
 
     private final void onCreateSmallLayout(MessageKind messageKind) {
         check(!messageKind.mIsFullScreen, messageKind.toString());
         setContentView(R.layout.message_small_layout);
 
         // Set border color and size
         // TODO: is there a way to set only the color and use default stroke width?
         final View frame = findViewById(R.id.message_small_frame);
         final GradientDrawable gradientDrawable = (GradientDrawable) frame.getBackground();
 
         final float density = DisplayUtil.getDensity(this);
         final int strokeWidthPixels = (int) ((BORDER_WIDTH_DIP * density) + 0.5f);
         gradientDrawable.setStroke(strokeWidthPixels, messageKind.mFrameColor);
 
         final WebView webview = (WebView) findViewById(R.id.message_small_webview);
 
        // NOTE: this stooped working since Android 4.11. The onNewPicture() is not called anymore
        // if the frame is invisible.
        //
         // We enable the message view only after the html page got rendered. This avoids a flicker
         // as the views finalize their sizes.
        //
        // frame.setVisibility(View.INVISIBLE);
        // webview.setPictureListener(new PictureListener() {
        //    public void onNewPicture(WebView view, Picture picture) {
        //        frame.setVisibility(View.VISIBLE);
        //    }
        // });
 
         displayFromAsset(webview, messageKind);
     }
 
     private final void displayFromAsset(WebView webView, MessageKind messageKind) {
         // Open HTML assert file. First we try a language specific file and if could
         // not open, fail over to the default one in english.      
         String filePath = null;
         InputStream in = null;
         final String languageCode = getString(R.string.translation_language_code);
         if (!languageCode.equals("en")) {
             filePath = messageKind.mAssetRelativeBaseName + "-" + languageCode
                     + messageKind.mAssetExtension;
             in = FileUtil.openAssert(this, filePath);
         }
         if (in == null) {
             filePath = messageKind.mAssetRelativeBaseName + messageKind.mAssetExtension;
             in = FileUtil.openAssert(this, filePath);
         }
         final FileReadResult fileReadResult = FileUtil.readFileToString(in, filePath);
 
         // TODO: handle this more gracefully?
         check(fileReadResult.outcome == FileReadOutcome.READ_OK,
                 "Error reading asset file: %s, outcome: %s", filePath, fileReadResult.outcome);
         final String htmlPage = expandMacros(fileReadResult.content);
         webView.loadDataWithBaseURL(ASSETS_BASE_URL + filePath, htmlPage, null, "UTF-8", null);
     }
 
     private final String expandMacros(String text) {
         if (!TextUtil.constainsMacros(text)) {
             return text;
         }
         final Hashtable<String, Object> macroValues = new Hashtable<String, Object>();
         final PackageInfo packageInfo = PackageUtil.getPackageInfo(this);
         macroValues.put("version_name", packageInfo.versionName);
         return TextUtil.expandMacros(text, macroValues, true);
     }
 
     /** Print a log error and return null if not found or an error */
     @Nullable
     private final MessageKind parseMessageKind() {
         // Get message kind from intent
         final MessageKind messageKind;
         // {
         @Nullable
         final String messageKindName = getIntent().getExtras().getString(INTENT_MESSAGE_KIND_KEY);
         if (messageKindName == null) {
             LogUtil.error("Message activity intent has message kind: %s", getIntent());
             // finish();
             return null;
         }
 
         try {
             messageKind = MessageKind.valueOf(messageKindName);
         } catch (IllegalArgumentException e) {
             LogUtil.error("Unknown message kind name [%s] in intent: %s", messageKindName,
                     getIntent());
             // finish();
             return null;
         }
         
         checkNotNull(messageKind);
         return messageKind;
     }
 
     /** Create an intent to invoke this activity. */
     public static final Intent intentFor(Context callerContext, MessageKind messageKind) {
         final Intent intent = new Intent(callerContext, PopupMessageActivity.class);
         intent.putExtra(INTENT_MESSAGE_KIND_KEY, messageKind.toString());
         return intent;
     }
 }
