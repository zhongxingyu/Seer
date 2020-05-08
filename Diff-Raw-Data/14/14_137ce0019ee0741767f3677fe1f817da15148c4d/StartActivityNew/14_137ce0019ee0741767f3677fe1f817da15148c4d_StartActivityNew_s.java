 package com.pavlukhin.acropanda.welcome;
 
 import android.app.ActivityManager;
 import android.content.Intent;
 import android.graphics.BitmapFactory;
 import android.graphics.Typeface;
 import android.graphics.drawable.AnimationDrawable;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.Gravity;
 import android.view.MotionEvent;
 import android.view.View;
import android.view.ViewTreeObserver;
 import android.widget.ImageView;
 import android.widget.RelativeLayout;
 import android.widget.Toast;
 
import com.google.android.gms.ads.AdRequest;
 import com.google.android.gms.ads.AdView;
 import com.pavlukhin.acropanda.PandaBaseActivity;
 import com.pavlukhin.acropanda.R;
 import com.pavlukhin.acropanda.achievements.AchievementsActivity;
import com.pavlukhin.acropanda.billing.BuyPremiumDialog;
 import com.pavlukhin.acropanda.rate.AppRater;
 import com.pavlukhin.acropanda.utils.PandaButtonsPanel;
 
 /**
  * Created by ivan on 28.09.13.
  */
 public class StartActivityNew extends PandaBaseActivity {
 
     public static final String ANIMATIONS_DIR = "animations/menu/panda";
 
     private View achivBtn;
     private View startSettings;
     private View turnOffAdBtn;
     private ImageView panda;
     private RelativeLayout contentPanel;
     private AnimationDrawable pandaAnimation;
     private ImageView mainTitle;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         if (isRepeativeLaunch())   finish();
         else                        init();
     }
 
     private void init() {
         setContentView(R.layout.activity_startnew);
         contentPanel = (RelativeLayout) findViewById(R.id.activity_content);
         initDefaultBackground(contentPanel);
         Typeface bold = app().getFontProvider().bold();
 //        ((TextView) findViewById(R.id.acro_caption)).setTypeface(bold);
 //        ((TextView) findViewById(R.id.start_caption)).setTypeface(bold);
 
         achivBtn = prepare(R.drawable.achievement);
         startSettings = prepare(R.drawable.settings);
         turnOffAdBtn = prepare(R.drawable.noad);
         PandaButtonsPanel bp = (PandaButtonsPanel) findViewById(R.id.welcome_bp);
 //        bp.customAddView(achivBtn);
         bp.customAddView(startSettings);
         bp.customAddView(turnOffAdBtn);
 
         panda = new ImageView(this);
         int width = (int) (app().displayWidth * .3125f);
         int height = (int) (app().displayHeight * .5f);
         if (width > height) width = height;
         else height = width;
         int left = (int) (app().displayWidth * .65f);
         int top = (int) (app().displayHeight * .20f);
 
         RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width, height);
         layoutParams.setMargins(left, top, 0, 0);
         panda.setLayoutParams(layoutParams);
         contentPanel.addView(panda);
         initOneTimePandaAnimation();
 //        initPandaDrawable(true);
 //        initPandaDrawable(false);
 
         initMainTitle();
         initListeners();
         AppRater.onAppLaunched(this);
         AdView adView = (AdView) findViewById(R.id.adView);
         loadAd(adView);
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
         app().getBillingManager().dispose();
     }
 
     private boolean isRepeativeLaunch() {
         if(!isTaskRoot()) {
             final Intent intent = getIntent();
             final String intentAction = intent.getAction();
             if (intentAction != null && intentAction.equals(Intent.ACTION_MAIN)) {
                 return true;
             }
         }
         return false;
     }
 
     private void initMainTitle() {
         mainTitle = new ImageView(this);
         BitmapFactory.Options options = new BitmapFactory.Options();
         options.inJustDecodeBounds = true;
 //        Bitmap original = BitmapFactory.decodeResource(
 //                getResources(), R.drawable.panda_caption, options);
         BitmapFactory.decodeResource(getResources(), R.drawable.panda_caption, options);
         float srcRatio = (float) options.outHeight / options.outWidth;
         float captionHeight = app().displayHeight * 0.80f;
         float captionWidth = app().displayWidth * 0.60f;
         float calcRatio = captionHeight / captionWidth;
         if(calcRatio > srcRatio) {
             captionHeight = captionWidth * srcRatio;
         } else {
             captionWidth = captionHeight / srcRatio;
         }
 //        Drawable drawable = new BitmapDrawable(
 //                getResources(),
 //                Bitmap.createScaledBitmap(original, (int) captionWidth, (int) captionHeight, true));
 //        mainTitle.setBackgroundDrawable(drawable);
         mainTitle.setBackgroundResource(R.drawable.panda_caption);
         RelativeLayout.LayoutParams lp =
                 new RelativeLayout.LayoutParams((int) captionWidth, (int) captionHeight);
         lp.setMargins((int) (app().displayWidth * 0.05f), (int) ((app().displayHeight - captionHeight) * 0.4f), 0, 0);
         mainTitle.setLayoutParams(lp);
         contentPanel.addView(mainTitle);
     }
 
     @Override
     public void onBackPressed() {
         super.onBackPressed();
     }
 
     private void initPandaDrawable(boolean animation) {
         if(animation) {
             if(pandaAnimation != null) return;
             pandaAnimation = app().loadAnimationFromFolder(ANIMATIONS_DIR);
             pandaAnimation.setOneShot(false);
             panda.setBackgroundDrawable(pandaAnimation);
             panda.post(new Runnable() {
                     @Override
                     public void run() {
                         pandaAnimation.start();
                     }
                 });
         } else {
             panda.setBackgroundResource(R.drawable.panda_icon);
         }
     }
 
     private void initOneTimePandaAnimation() {
         panda.setBackgroundResource(R.drawable.panda_icon);
         loadAnimation();
     }
 
     private void asyncLoadAnimation() {
         new AsyncTask<Void, Void, Void>() {
             @Override
             protected Void doInBackground(Void... params) {
                 loadAnimation();
                 return null;
             }
         }.execute();
     }
 
     private void loadAnimation() {
         if(pandaAnimation != null) return;
         pandaAnimation = app().loadAnimationFromFolder(ANIMATIONS_DIR);
         pandaAnimation.setOneShot(false);
         panda.post(new Runnable() {
             @Override
             public void run() {
                 if(pandaAnimation == null) return;
                 panda.setBackgroundDrawable(pandaAnimation);
                 pandaAnimation.start();
             }
         });
     }
 
     private void initListeners() {
         contentPanel.setOnTouchListener(new View.OnTouchListener() {
             @Override
             public boolean onTouch(View v, MotionEvent event) {
                 if(event.getAction() == MotionEvent.ACTION_DOWN) {
                     gotoPacksScreen();
                     return true;
                 } else {
                     return false;
                 }
             }
         });
 //        panda.setOnClickListener(new View.OnClickListener() {
 //            @Override
 //            public void onClick(View view) {
 //                gotoPacksScreen();
 //            }
 //        });
         startSettings.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 gotoSettingsScreen();
             }
         });
         achivBtn.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 gotoAchievementsScreen();
             }
         });
         turnOffAdBtn.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                showBuyPremiumDialog();
             }
         });
     }
 
     private void gotoAchievementsScreen() {
         Intent intent = new Intent(this, AchievementsActivity.class);
         startActivity(intent);
     }
 
     private void gotoPacksScreen() {
         disposePandaAnimation();
         Intent intent = new Intent(this, StartActivity.class);
         startActivityForResult(intent, 0);
     }
 
     private void disposePandaAnimation() {
         if(pandaAnimation == null) return;
         panda.setBackgroundResource(R.drawable.panda_icon);
         pandaAnimation.stop();
         for (int i = 0; i < pandaAnimation.getNumberOfFrames(); ++i){
             Drawable frame = pandaAnimation.getFrame(i);
             frame.setCallback(null);
             if (frame instanceof BitmapDrawable)
                 ((BitmapDrawable) frame).getBitmap().recycle();
         }
         pandaAnimation.setCallback(null);
         pandaAnimation = null;
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         if(requestCode == BUY_PREMIUM_CODE)
             app().getBillingManager().handleActivityResult(requestCode, resultCode, data);
 //        asyncLoadAnimation();
     }
 
     @Override
     public boolean onTouchEvent(MotionEvent event) {
 //        toastMemoryInfo();
         return super.onTouchEvent(event);
     }
 
     private void toastMemoryInfo() {
         Toast toast = Toast.makeText(
                 this,
                 "Memory class: " + ((ActivityManager) getSystemService(ACTIVITY_SERVICE)).getMemoryClass() +
                         "\nMax memory(K): " + Runtime.getRuntime().maxMemory() / 1024 +
                         "\nTotal memory(K): " + Runtime.getRuntime().totalMemory() / 1024 +
                         "\nFree memory(K): " + Runtime.getRuntime().freeMemory() / 1024,
                 Toast.LENGTH_SHORT);
         toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
         toast.show();
     }
 }
