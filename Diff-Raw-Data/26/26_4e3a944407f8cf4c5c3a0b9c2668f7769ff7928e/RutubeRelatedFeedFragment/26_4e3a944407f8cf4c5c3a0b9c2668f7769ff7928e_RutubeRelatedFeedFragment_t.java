 package ru.rutube.RutubeApp.ui.feed;
 
 import android.content.Intent;
 import android.graphics.Typeface;
 import android.net.Uri;
 import android.os.Bundle;
 import android.text.Html;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.HeaderViewListAdapter;
 import android.widget.ImageButton;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 import ru.rutube.RutubeAPI.content.FeedContract;
 import ru.rutube.RutubeAPI.models.Author;
 import ru.rutube.RutubeAPI.models.Video;
 import ru.rutube.RutubeApp.BuildConfig;
 import ru.rutube.RutubeApp.MainApplication;
 import ru.rutube.RutubeApp.R;
 import ru.rutube.RutubeApp.data.RelatedCursorAdapter;
 import ru.rutube.RutubeApp.ui.RutubeVideoPageActivity;
 import ru.rutube.RutubeFeed.data.FeedCursorAdapter;
 import ru.rutube.RutubeFeed.helpers.Typefaces;
 import ru.rutube.RutubeFeed.ui.RelatedFeedFragment;
 
 /**
  * Created by tumbler on 18.08.13.
  * Фрагмент ленты для похожих видео.
  * Добавляет поддержку прокручеваемой информации о видео с помощью ListView.addHeaderView
  */
 public class RutubeRelatedFeedFragment extends RelatedFeedFragment {
     public static final String INIT_HEADER = "init_header";
     protected static final boolean D = BuildConfig.DEBUG;
     private View mLoader;
     private View mEmptyList;
 
 
     protected static class ViewHolder extends RutubeVideoPageActivity.ViewHolder {
         ImageButton moreInfo;
         TextView bullet;
         View commentLine;
     }
 
     private static final String LOG_TAG = RutubeRelatedFeedFragment.class.getName();
 
     protected ViewHolder mViewHolder;
 
     private ListView mListView; // список похожих видео
     private ViewGroup mInfoView;     // блок информации о ролике
     private View mHeaderView;   // контейнер, хранящий блок информации о ролике
     private boolean mHasInfoView = false;
     private boolean mDescriptionVisible = false;
 
     /**
      * Обработчик кнопки MoreInfo
      */
     protected View.OnClickListener mOnMoreClickListener = new View.OnClickListener() {
         @Override
         public void onClick(View view) {
             toggleDescription();
         }
     };
 
     /**
      * Обработчик кликов по элементам блока с информацией о видео.
      */
     protected View.OnClickListener mOnVideoElementClickListener = new View.OnClickListener() {
 
         @Override
         public void onClick(View view) {
             if (D) Log.d(LOG_TAG, "element click: " + String.valueOf(view));
             try {
                 FeedCursorAdapter.ClickTag tag = (FeedCursorAdapter.ClickTag)view.getTag();
                if (tag != null)
                    MainApplication.getInstance().openFeed(tag.href, getActivity(), tag.title);
                else
                    ((MainApplication)MainApplication.getInstance()).reportError(
                            getActivity(), String.format("empty tag onClick at %s",
                                String.valueOf(view)));

             } catch (ClassCastException e) {
                 Uri feedUri = (Uri)view.getTag();
                 MainApplication.getInstance().openFeed(feedUri, getActivity(), null);
             }
             catch (IllegalArgumentException e) {
                 e.printStackTrace();
             }
 
         }
     };
 
     /**
      * Переопределенные методы Fragment
      */
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         // Инициализирует layout фрагмента, блока информации о видео.
         // при наличии в getActivity().getIntent() флага INIT_HEADER, добавляет в список
         // блок информации о видео.
         Log.d(LOG_TAG, "onCreateView");
         View v = super.onCreateView(inflater, container, savedInstanceState);
         assert v!= null;
         mListView = (ListView) v.findViewById(android.R.id.list);
         initListView();
         Intent intent = getActivity().getIntent();
         if (intent.getBooleanExtra(INIT_HEADER, false))
             addHeaderView();
         mLoader = v.findViewById(R.id.loader);
         mEmptyList = v.findViewById(R.id.empty);
         return v;
     }
 
     /**
      * Переопределенные методы FeedFragment
      */
 
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
 
     @Override
     public boolean onItemClick(FeedCursorAdapter.ClickTag position, String viewTag) {
         MainApplication.relatedCardClick(getActivity(), viewTag);
         return super.onItemClick(position, viewTag);
     }
 
     /**
      * Проставляет значения элементам блока информации о видео
      * @param video объект модели Video.
      */
     public void setVideoInfo(Video video) {
         if (video == null){
             toggleNoVideoInfo();
             return;
         }
         toggleVideoInfoLoader(false);
         mViewHolder.title.setText(video.getTitle());
         Author author = video.getAuthor();
         if (author != null) {
             mViewHolder.author.setText(author.getName());
             FeedCursorAdapter.ClickTag tag = new FeedCursorAdapter.ClickTag(0, author.getFeedUrl(),
                     "@" + author.getName());
             mViewHolder.author.setTag(tag);
 
         }
         String hits = video.getHitsText(getActivity());
         mViewHolder.hits.setText(hits);
         String description = video.getDescription();
         if (description != null)
             mViewHolder.description.setText(Html.fromHtml(description));
         else
             mViewHolder.description.setText(null);
 
         mViewHolder.created.setText(MainApplication.getInstance().getCreatedText(video.getCreated()));
     }
 
     /**
      * Изменяет видимость заглушки у блока информации о видео
      */
     protected void toggleNoVideoInfo() {
         ProgressBar loader = (ProgressBar)mViewHolder.videoInfoContainer.findViewById(R.id.video_info_loader);
         loader.setProgressDrawable(getResources().getDrawable(R.drawable.sad_smile));
     }
 
     /**
      * Изменяет видимость блока информации о видео
      * @param visible флаг видимости блока информации о видео
      */
     public void toggleHeader(boolean visible) {
         // у ListView есть баг, связанный с тем, что если заголовок не обернут в LinearLayout,
         // то на месте скрытого (View.GONE) заголовка остается пустое место.
         // В результате - mHeaderView содержит ссылку на LinearLyaout, содержащий скрываемый
         // mInfoView.
         if (D) Log.d(LOG_TAG, "toggleHeader: " + String.valueOf(visible));
         mInfoView.setVisibility(visible? View.VISIBLE: View.GONE);
         // разметка такова, что при нахождении карточки видео в похожих, надо менять paddingRight
         // с 0 на значение, равное paddingLeft.
         // FIXME: разные layout + include общей чачти
         mInfoView.setPadding(
                 mInfoView.getPaddingLeft(),
                 mInfoView.getPaddingTop(),
                 mInfoView.getPaddingLeft(),
                 mInfoView.getPaddingBottom()
         );
         mListView.forceLayout();
     }
 
     /**
      * Изменяет видимость описания ролика
      * @param visible флаг видимости описания
      */
     protected void toggleDescription(boolean visible) {
         // Сохраняем отступы для блока информации о видео, т.к. при простановке в качестве
         // фона 9-patch, отступы сбрасываются.
         View v = mViewHolder.videoInfoContainer;
         int pl = v.getPaddingLeft();
         int pt = v.getPaddingTop();
         int pr = v.getPaddingRight();
         int pb = v.getPaddingBottom();
         // меняем картинку на кнопке и фон у верхней части блока информации о видео
         if (visible){
             mViewHolder.moreInfo.setImageResource(R.drawable.more_info_btn_down);
             v.setBackgroundResource(R.drawable.first_related_bg);
         } else {
             mViewHolder.moreInfo.setImageResource(R.drawable.more_info_btn_left);
             v.setBackgroundResource(R.drawable.video_info_bg);
         }
         // возвращаем обратно отступы
         v.setPadding(pl, pt, pr, pb);
         // меняем видимость горизонтальной черты и описания
         mDescriptionVisible = visible;
         int visibility = visible? View.VISIBLE: View.GONE;
         mViewHolder.commentLine.setVisibility(visibility);
         mViewHolder.description.setVisibility(visibility);
     }
 
     protected void toggleDescription() {
         toggleDescription(!mDescriptionVisible);
     }
 
     /**
      * Инициализирует элементы блока информации о видео.
      */
     protected void initListView() {
         Typeface normalFont = Typefaces.get(getActivity(), "fonts/opensansregular.ttf");
         Typeface lightFont = Typefaces.get(getActivity(), "fonts/opensanslight.ttf");
         // инициализирует и добавляет в ListView заголовок с информацией о видео.
         mHeaderView = getActivity().getLayoutInflater().inflate(R.layout.related_header, null);
         assert mHeaderView != null;
         mInfoView = (ViewGroup)mHeaderView.findViewById(R.id.video_info);
         assert mInfoView != null;
         mViewHolder = new ViewHolder();
         mViewHolder.title = ((TextView)mInfoView.findViewById(R.id.video_title));
         mViewHolder.from = ((TextView)mInfoView.findViewById(R.id.from));
         mViewHolder.author = ((TextView)mInfoView.findViewById(R.id.author_name));
         mViewHolder.bullet = ((TextView) mInfoView.findViewById(R.id.bullet));
         mViewHolder.created = ((TextView)mInfoView.findViewById(R.id.created));
         mViewHolder.hits = ((TextView)mInfoView.findViewById(R.id.hits));
         mViewHolder.description = ((TextView)mInfoView.findViewById(R.id.description));
         mViewHolder.moreInfo = ((ImageButton)mInfoView.findViewById(R.id.moreImageButton));
         mViewHolder.commentLine = mInfoView.findViewById(R.id.commentLine);
         mViewHolder.videoInfoContainer = (ViewGroup)mInfoView.findViewById(R.id.baseInfoContainer);
 
         toggleVideoInfoLoader(true);
 
         mViewHolder.title.setTypeface(normalFont);
         mViewHolder.from.setTypeface(lightFont);
         mViewHolder.author.setTypeface(normalFont);
         mViewHolder.bullet.setTypeface(lightFont);
         mViewHolder.created.setTypeface(lightFont);
         mViewHolder.hits.setTypeface(lightFont);
         mViewHolder.description.setTypeface(lightFont);
 
         mViewHolder.moreInfo.setOnClickListener(mOnMoreClickListener);
         mViewHolder.author.setOnClickListener(mOnVideoElementClickListener);
 
         toggleDescription(false);
         mListView.setHeaderDividersEnabled(false);
     }
 
     private void toggleVideoInfoLoader(boolean loading) {
         ViewGroup container = mViewHolder.videoInfoContainer;
         for(int i=0;i<container.getChildCount();i++) {
             View c = container.getChildAt(i);
             assert c != null;
             if (c.getId() == R.id.video_info_loader)
                 c.setVisibility((loading)?View.VISIBLE:View.GONE);
             else
                 c.setVisibility((loading)?View.GONE:View.VISIBLE);
         }
     }
 
     private void addHeaderView() {
         if (mHasInfoView) return;
         mHasInfoView = true;
         mListView.addHeaderView(mHeaderView);
     }
 
 
     @Override
     public void setRefreshing() {
         super.setRefreshing();
         mLoader.setVisibility(View.VISIBLE);
         mEmptyList.setVisibility(View.GONE);
     }
 
     @Override
     public void doneRefreshing() {
         super.doneRefreshing();
         mLoader.setVisibility(View.GONE);
         mEmptyList.setVisibility(View.VISIBLE);
     }
 }
