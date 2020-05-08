 package ru.rutube.RutubeApp.ui.feed;
 
 import android.content.Intent;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.HeaderViewListAdapter;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import ru.rutube.RutubeAPI.content.FeedContract;
 import ru.rutube.RutubeAPI.models.Author;
 import ru.rutube.RutubeAPI.models.Video;
 import ru.rutube.RutubeApp.BuildConfig;
 import ru.rutube.RutubeApp.R;
 import ru.rutube.RutubeApp.data.RelatedCursorAdapter;
 import ru.rutube.RutubeFeed.data.FeedCursorAdapter;
 import ru.rutube.RutubeFeed.helpers.Typefaces;
 import ru.rutube.RutubeFeed.ui.RelatedFeedFragment;
 
 /**
  * Created by tumbler on 18.08.13.
  * Фрагмент ленты для похожих видео.
  * Добавляет поддержку прокручеваемой информации о видео с помощью ListView.addHeaderView
  */
 public class RutubeRelatedFeedFragment extends RelatedFeedFragment {
     private static final String LOG_TAG = RutubeRelatedFeedFragment.class.getName();
     public static final String INIT_HEADER = "init_header";
     private static final boolean D = BuildConfig.DEBUG;
     private ListView mListView;
     private Typeface mNormalFont;
     private Typeface mLightFont;
     private View mInfoView;
     private boolean mHasInfoView = false;
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         Log.d(LOG_TAG, "onCreateView");
         View v = super.onCreateView(inflater, container, savedInstanceState);
         assert v!= null;
         mListView = (ListView) v.findViewById(android.R.id.list);
         initListView();
         Intent intent = getActivity().getIntent();
         toggleHeader(intent.getBooleanExtra(INIT_HEADER, false));
         return v;
     }
 
     private void initListView() {
         mNormalFont = Typefaces.get(getActivity(), "fonts/opensansregular.ttf");
         mLightFont = Typefaces.get(getActivity(), "fonts/opensanslight.ttf");
         // инициализирует и добавляет в ListView заголовок с информацией о видео.
         mInfoView = getActivity().getLayoutInflater().inflate(R.layout.video_info, null);
         assert mInfoView != null;
         ((TextView)mInfoView.findViewById(R.id.video_title)).setTypeface(mNormalFont);
        ((TextView)mInfoView.findViewById(R.id.from)).setTypeface(mLightFont);
         ((TextView)mInfoView.findViewById(R.id.author_name)).setTypeface(mNormalFont);
         ((TextView)mInfoView.findViewById(R.id.bullet)).setTypeface(mLightFont);
         ((TextView)mInfoView.findViewById(R.id.createdTextView)).setTypeface(mLightFont);
         ((TextView)mInfoView.findViewById(R.id.hits)).setTypeface(mLightFont);
        ((TextView)mInfoView.findViewById(R.id.description)).setTypeface(mLightFont);
         mListView.setHeaderDividersEnabled(false);
     }
 
     private void addHeaderView() {
         if (mHasInfoView) return;
         mHasInfoView = true;
         mListView.addHeaderView(mInfoView);
     }
 
     private void removeHeaderView() {
         if (!mHasInfoView) return;
         mHasInfoView = false;
         mListView.removeHeaderView(mInfoView);
     }
 
     @Override
     public ListAdapter getListAdapter() {
         // При вызове addHeaderView ListView трансформирует свой адаптер в HeaderViewListAdapter,
         // являющийся оберткой адаптера, проставляемого через ListView.setAdapter
         if (mHasInfoView)
             return ((HeaderViewListAdapter)mListView.getAdapter()).getWrappedAdapter();
         return mListView.getAdapter();
     }
 
     @Override
     public FeedCursorAdapter initAdapter() {
         return new RelatedCursorAdapter(getActivity(),
                 R.layout.related_feed_item,
                 null,
                 new String[]{FeedContract.FeedColumns.TITLE, FeedContract.FeedColumns.THUMBNAIL_URI},
                 new int[]{ru.rutube.RutubeFeed.R.id.titleTextView, ru.rutube.RutubeFeed.R.id.thumbnailImageView},
                 0);
     }
 
     public void setVideoInfo(Video video) {
        ((TextView)mInfoView.findViewById(R.id.video_title)).setText(
                 video.getTitle());
         Author author = video.getAuthor();
         if (author != null) {
            TextView authorName = (TextView)mInfoView.findViewById(R.id.author_name);
             authorName.setText(author.getName());
             authorName.setTag(author.getFeedUrl());
 
         }
 //        int duration = video.getDuration();
 //        ((TextView)v.findViewById(ru.rutube.RutubePlayer.R.id.duration)).setText(
 //                DateUtils.formatElapsedTime(duration));
         String hits = video.getHitsText(getActivity());
         ((TextView)mInfoView.findViewById(ru.rutube.RutubePlayer.R.id.hits)).setText(hits);
        ((TextView)mInfoView.findViewById(R.id.description)).setText(video.getDescription());
     }
 
     public void toggleHeader(boolean visible) {
         if (D) Log.d(LOG_TAG, "toggleHeader: " + String.valueOf(visible));
         if (visible)
             addHeaderView();
         else
             removeHeaderView();
     }
 }
