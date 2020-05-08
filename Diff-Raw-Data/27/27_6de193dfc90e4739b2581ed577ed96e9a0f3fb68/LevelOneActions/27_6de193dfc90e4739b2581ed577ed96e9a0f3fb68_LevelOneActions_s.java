 package com.redcarddev.kickshot;
 
 import java.io.InputStream;
 import java.util.Random;
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.MotionEvent;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 /**
  * Created by otternq on 10/27/13.
  */
 public class LevelOneActions extends Activity {
 
     String LOGTAG = this.getClass().getName();
 
     final static int COMPUTER_SCORED = 1;
     final static int COMPUTER_BLOCKED = 3;
     final static int COMPUTER_SHOT = 5;
     final static int COMPUTER_INTERCEPT = 7;
     final static int COMPUTER_TURNOVER = 9;
 
     final static int PLAYER_SCORED = 0;
     final static int PLAYER_BLOCKED = 2;
     final static int PLAYER_SHOT = 4;
     final static int PLAYER_INTERCEPT = 6;
     final static int PLAYER_TURNOVER = 8;
 
     protected int state = -1;
     protected String gifPath;
 
     private WebView  webView;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         Intent mIntent = getIntent();
         this.state = mIntent.getIntExtra("state", -1);
         gifPath = this.setActionView();
         setContentView(R.layout.level_one_actions);
 
 
         webView = (WebView) findViewById(R.id.webviewActionView);
         webView.setBackgroundColor(Color.TRANSPARENT);
         webView.loadUrl(gifPath);
         //GifWebView view = new GifWebView(this, gifPath);
 
         //view.getBackground().setAlpha(128);
         //setContentView(view);
 
 
 
 
         /*GifWebView view = new GifWebView(this, "file://android_asset/shot_away.gif");
 
         setContentView(view);
 
         Intent mIntent = getIntent();
         this.state = mIntent.getIntExtra("state", -1);
         this.whichSide = r.nextInt(100);
 
         this.actionText = (TextView)findViewById(R.id.actionText);
         this.actionImage = (ImageView)findViewById(R.id.actionImage);
 
         this.setActionView();
         */
 
     }
 
     @Override
     public boolean onTouchEvent(MotionEvent event){
         this.finish();
         return true;
     }
 
     protected String setActionView() {
         String url = "";
         // every time this is called randomize if it's a shot to the left or right
         switch (this.state) {
             case LevelOneActions.COMPUTER_SCORED:
                 url = "file:///android_asset/computer_goal.html";
                 break;
             case LevelOneActions.COMPUTER_BLOCKED:
                 url = "file:///android_asset/block_away.html";
                 break;
             case LevelOneActions.COMPUTER_SHOT:
                 url = "file:///android_asset/shot_away.html";
                 break;
             case LevelOneActions.COMPUTER_INTERCEPT:
                 url = "file:///android_asset/intercept_away.html";
                 break;
             case LevelOneActions.COMPUTER_TURNOVER:
                 url = "file:///android_asset/intercept_home.html";
                 break;
             case LevelOneActions.PLAYER_SCORED:
                 url = "file:///android_asset/home_goal.html";
                 break;
             case LevelOneActions.PLAYER_BLOCKED:
                 url = "file:///android_asset/block_home.html";
                 break;
             case LevelOneActions.PLAYER_SHOT:
                 url = "file:///android_asset/shot_home.html";
                 break;
             case LevelOneActions.PLAYER_INTERCEPT:
                 url = "file:///android_asset/intercept_home.html";
                 break;
             case LevelOneActions.PLAYER_TURNOVER:
                 url = "file:///android_asset/intercept_away.html";
                 break;
             default:
                 return url;
         }
 
         return url;
 
     }
 
 }
