 package ru.rutube.RutubeApp.ui;
 
 import android.content.Context;
 import android.content.pm.ActivityInfo;
 import android.content.res.Configuration;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.util.AttributeSet;
 import android.view.View;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 import ru.rutube.RutubeAPI.models.Video;
 import ru.rutube.RutubeApp.BuildConfig;
 import ru.rutube.RutubeApp.R;
 import ru.rutube.RutubeApp.ui.feed.RutubeRelatedFeedFragment;
 import ru.rutube.RutubeFeed.helpers.Typefaces;
 import ru.rutube.RutubePlayer.ui.VideoPageActivity;
 
 /**
  * Created by tumbler on 13.11.13.
  * Активити страницы видео.
  * Переопределяет поведение страницы видео из библиотеки RutubePlayer:
  *   - добавляет фрагмент с похожими
  *   - переносит информацию о видео в ListView.addHeaderView с помощью RutubeRelatedFeedFragment
  */
 public class RutubeVideoPageActivity extends VideoPageActivity {
     private static final boolean D = BuildConfig.DEBUG;
     private static final String LOG_TAG = RutubeVideoPageActivity.class.getName();
     private RutubeRelatedFeedFragment mRelatedFragment;
     private boolean mIsLandscape;
     private Typeface mNormalFont;
     private Typeface mLightFont;
 
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         mIsLandscape = getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
         getIntent().putExtra(RutubeRelatedFeedFragment.INIT_HEADER, !mIsLandscape);
         mLayoutResId = R.layout.video_page_activity;
         super.onCreate(savedInstanceState);
         init();
     }
 
     @Override
     public View onCreateView(String name, Context context, AttributeSet attrs) {
         View view = super.onCreateView(name, context, attrs);
         FragmentManager fm = getSupportFragmentManager();
         mRelatedFragment = (RutubeRelatedFeedFragment) fm.findFragmentById(
                 R.id.related_video_container);
         return view;
     }
 
     @Override
     public void onConfigurationChanged(Configuration newConfig) {
         super.onConfigurationChanged(newConfig);
         transformLayout(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT);
     }
 
     protected void transformLayout(boolean isPortrait) {
         LinearLayout ll = (LinearLayout)findViewById(R.id.page);
         ll.setOrientation(isPortrait? LinearLayout.VERTICAL: LinearLayout.HORIZONTAL);
         View v = findViewById(R.id.video_info_container);
         v.setVisibility(isPortrait? View.GONE: View.VISIBLE);
         mRelatedFragment.toggleHeader(isPortrait);
         v = findViewById(R.id.video_container);
         LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)v.getLayoutParams();
         lp.weight = isPortrait? 2: 1;
         v.setLayoutParams(lp);
         v = mRelatedFragment.getView();
         lp =(LinearLayout.LayoutParams)v.getLayoutParams();
         lp.weight = isPortrait? 1: 2;
         v.setLayoutParams(lp);
     }
 
     private void init() {
         mNormalFont = Typefaces.get(this, "fonts/opensansregular.ttf");
         mLightFont = Typefaces.get(this, "fonts/opensanslight.ttf");
         ((TextView)findViewById(R.id.video_title)).setTypeface(mNormalFont);
         ((TextView)findViewById(R.id.from)).setTypeface(mLightFont);
         ((TextView)findViewById(R.id.author_name)).setTypeface(mLightFont);
        ((TextView)findViewById(R.id.duration)).setTypeface(mLightFont);
         ((TextView)findViewById(R.id.hits)).setTypeface(mLightFont);
         ((TextView)findViewById(R.id.description)).setTypeface(mLightFont);
     }
 
     @Override
     public void setVideoInfo(Video video) {
         mRelatedFragment.setVideoInfo(video);
         super.setVideoInfo(video);
     }
 
     @Override
     public void toggleFullscreen(boolean isFullscreen) {
         // при переходе в фулскрин скрывать похожие надо до изменения ориентации,
         if (isFullscreen)
             toggleRelatedFragment(false);
         super.toggleFullscreen(isFullscreen);
         // а при переходе в режим страницы видео - после изменения ориентации.
         if (!isFullscreen)
             toggleRelatedFragment(true);
 
     }
 
     private void toggleRelatedFragment(boolean visible) {
         FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
         if (!visible)
             ft.hide(mRelatedFragment);
         else
             ft.show(mRelatedFragment);
         ft.commit();
     }
 }
