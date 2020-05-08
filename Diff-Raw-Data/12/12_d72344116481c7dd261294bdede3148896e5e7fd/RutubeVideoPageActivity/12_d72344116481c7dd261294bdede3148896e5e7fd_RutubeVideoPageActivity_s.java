 package ru.rutube.RutubeApp.ui;
 
 import android.content.Context;
 import android.content.res.Configuration;
 import android.graphics.Typeface;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.LinearLayout;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 import ru.rutube.RutubeAPI.RutubeApp;
 import ru.rutube.RutubeAPI.models.Author;
 import ru.rutube.RutubeAPI.models.Video;
 import ru.rutube.RutubeApp.MainApplication;
 import ru.rutube.RutubeApp.R;
 import ru.rutube.RutubeApp.ui.feed.RutubeRelatedFeedFragment;
 import ru.rutube.RutubeFeed.data.FeedCursorAdapter;
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
 
     public static class ViewHolder extends VideoPageActivity.ViewHolder {
         public TextView from;
         public TextView created;
         public ViewGroup baseInfoContainer;
     }
 
     private static final String LOG_TAG = RutubeVideoPageActivity.class.getName();
 
     static {
         mLayoutResId = R.layout.video_page_activity;
     }
 
     private RutubeRelatedFeedFragment mRelatedFragment;
 
     /**
      * Переопределяет обработчики кликов на элементы карточки видео, добавляя отправку статистики
      * Google Analytics
      */
     protected View.OnClickListener mOnVideoElementClickListener = new View.OnClickListener() {
         @Override
         public void onClick(View view) {
             if (D) Log.d(LOG_TAG, "element click: " + String.valueOf(view));
             try {
                 FeedCursorAdapter.ClickTag tag = (FeedCursorAdapter.ClickTag)view.getTag();
                 MainApplication.getInstance().openFeed(tag.href, RutubeVideoPageActivity.this, tag.title);
             } catch (ClassCastException e) {
                 Uri feedUri = (Uri)view.getTag();
                 MainApplication.getInstance().openFeed(feedUri, RutubeVideoPageActivity.this, null);
             }
         }
     };
 
     /**
      * Переопределение методов Activity
      */
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         getIntent().putExtra(RutubeRelatedFeedFragment.INIT_HEADER, true);
         super.onCreate(savedInstanceState);
     }
 
     @Override
     protected void onStart() {
        super.onStart();
        MainApplication.playerActivityStart(this, String.valueOf(getIntent().getData()));
     }
 
     @Override
     protected void onStop() {
         super.onStop();
         MainApplication.activityStop(this);
     }
 
     @Override
     public View onCreateView(String name, Context context, AttributeSet attrs) {
         View view = super.onCreateView(name, context, attrs);
         FragmentManager fm = getSupportFragmentManager();
         mRelatedFragment = (RutubeRelatedFeedFragment)fm.findFragmentById(
                 R.id.related_video_container);
         return view;
     }
 
     /**
      * Обработчик события изменения конфигурации устройства
      * @param newConfig новая конфигурация
      */
     @Override
     public void onConfigurationChanged(Configuration newConfig) {
         super.onConfigurationChanged(newConfig);
         // Трансформирует страницу в зависимости от указанной ориентации устройства,
         // включает и выключает фулскрин.
         if (D) Log.d(LOG_TAG, String.format("Orientation: %d", newConfig.orientation));
         if (D) Log.d(LOG_TAG, String.format("ReqOrient: %d", getRequestedOrientation()));
         checkOrientation();
         transformLayout(mIsLandscape);
         toggleFullscreen(mIsLandscape, false);
     }
 
     /**
      * Переопределение методов VideoPageActivity
      */
 
     @Override
     public void setVideoInfo(Video video) {
         mRelatedFragment.setVideoInfo(video);
         if (video == null){
             toggleNoVideoInfo();
             return;
         }
         toggleVideoInfoLoader(false);
         super.setVideoInfo(video);
         bindCreated(video);
     }
 
     /**
      * Переводит страницу в полноэкранный режим и при необходимости меняет ориентацию экрана
      * @param isFullscreen флаг полноэкранного режима
      * @param rotate флаг необходимости смены ориентации экрана
      */
     @Override
     public void toggleFullscreen(boolean isFullscreen, boolean rotate) {
         // при переходе в фулскрин скрывать похожие надо до изменения ориентации,
         if (isFullscreen)
             toggleRelatedFragment(false);
 
         super.toggleFullscreen(isFullscreen, rotate);
         checkOrientation();
         if (rotate)
             transformLayout(mIsLandscape);
         toggleVideoHeader();
 
         // при переходе в режим страницы видео похожие добавляются после изменения ориентации.
         if (!isFullscreen)
             toggleRelatedFragment(true);
     }
 
     /**
      * Изменяет видимость блока информации о видео
      */
     protected void toggleVideoHeader() {
         // основная карточка видео видна только в пейзажной ориентации и только не в фуллскрине
         mViewHolder.videoInfoContainer.setVisibility(
                 (mIsLandscape && !mIsFullscreen) ? View.VISIBLE : View.GONE);
         // меняем видимость карточки видео в похожих в зависимости от ориентации
         mRelatedFragment.toggleHeader(!mIsLandscape);
     }
 
     protected void toggleNoVideoInfo() {
         ProgressBar loader = (ProgressBar)mViewHolder.videoInfoContainer.findViewById(R.id.video_info_loader);
         loader.setProgressDrawable(getResources().getDrawable(R.drawable.sad_smile));
 
     }
 
     protected void toggleVideoInfoLoader(boolean loading) {
         ViewHolder holder = (ViewHolder)mViewHolder;
         ViewGroup container = holder.baseInfoContainer;
         for(int i=0;i<container.getChildCount();i++) {
             View c = container.getChildAt(i);
             assert c != null;
             if (c.getId() == R.id.moreImageButton)
                 continue;
             if (c.getId() == R.id.video_info_loader)
                 c.setVisibility((loading)?View.VISIBLE:View.GONE);
             else
                 c.setVisibility((loading)?View.GONE:View.VISIBLE);
         }
     }
 
     @Override
     protected VideoPageActivity.ViewHolder getHolder() {
         return new ViewHolder();
     }
 
     @Override
     protected void initHolder(VideoPageActivity.ViewHolder holder) {
         super.initHolder(holder);
         ViewHolder h = (ViewHolder)holder;
         h.from = (TextView)findViewById(R.id.from);
         h.created = (TextView)findViewById(R.id.created);
         h.baseInfoContainer = (ViewGroup)findViewById(R.id.baseInfoContainer);
         mViewHolder = h;
     }
 
     @Override
     protected void bindDuration(Video video) {}
 
     @Override
     protected void init() {
         super.init();
         Typeface normalFont = Typefaces.get(this, "fonts/opensansregular.ttf");
         Typeface lightFont = Typefaces.get(this, "fonts/opensanslight.ttf");
 
         FragmentManager fm = getSupportFragmentManager();
         mRelatedFragment = (RutubeRelatedFeedFragment) fm.findFragmentById(
                 R.id.related_video_container);
 
         ViewHolder holder = (ViewHolder)mViewHolder;
         holder.title.setTypeface(normalFont);
         holder.from.setTypeface(lightFont);
         holder.author.setTypeface(lightFont);
         holder.created.setTypeface(lightFont);
         holder.hits.setTypeface(lightFont);
         holder.description.setTypeface(lightFont);
 
         toggleVideoInfoLoader(false);
 
         holder.author.setOnClickListener(mOnVideoElementClickListener);
     }
 
     @Override
     protected void bindAuthor(Video video) {
         Author author = video.getAuthor();
         if (author != null) {
             TextView authorName = mViewHolder.author;
             authorName.setText(author.getName());
             FeedCursorAdapter.ClickTag tag = new FeedCursorAdapter.ClickTag(0, author.getFeedUrl(),
                     "@" + author.getName());
             authorName.setTag(tag);
         }
     }
 
     protected void bindCreated(Video video) {
         String createdText = RutubeApp.getInstance().getCreatedText(video.getCreated());
         ViewHolder holder = (ViewHolder)mViewHolder;
         holder.created.setText(createdText);
     }
 
     protected void transformLayout(boolean isLandscape) {
         if(D) Log.d(LOG_TAG, "transformLayout " + String.valueOf(!isLandscape));
         // список похожих справа или снизу
         LinearLayout ll = (LinearLayout)findViewById(R.id.page);
         ll.setOrientation(isLandscape? LinearLayout.HORIZONTAL: LinearLayout.VERTICAL);
 
         // основная карточка видео видна только в пейзажной ориентации и только не в фуллскрине
         mViewHolder.videoInfoContainer.setVisibility(
                 (isLandscape && !mIsFullscreen) ? View.VISIBLE : View.GONE);
 
         // layout_weight для плеера + карточки видео в зависимости от ориентации
         View v = findViewById(R.id.video_container);
         LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)v.getLayoutParams();
         lp.weight = getResources().getInteger(R.integer.video_container_weight);
         if(D) Log.d(LOG_TAG, "VC weight: " + String.valueOf(lp.weight));
         v.setLayoutParams(lp);
 
         // карточка видео в похожих видна только в портретной ориентации
         mRelatedFragment.toggleHeader(!isLandscape);
 
         // layout_weight для похожих в зависимости от ориентации
         v = mRelatedFragment.getView();
         lp =(LinearLayout.LayoutParams)v.getLayoutParams();
         lp.weight = getResources().getInteger(R.integer.related_video_container_weight);
         if(D) Log.d(LOG_TAG, "RC weight: " + String.valueOf(lp.weight));
         v.setLayoutParams(lp);
     }
 
     /**
      * Меняет видимость ленты похожих видео
      * @param visible флаг видимости ленты похожих видео
      */
     private void toggleRelatedFragment(boolean visible) {
         FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
         if (!visible)
             ft.hide(mRelatedFragment);
         else
             ft.show(mRelatedFragment);
         try {
             ft.commit();
         } catch (IllegalStateException ignored) {}
     }
 }
