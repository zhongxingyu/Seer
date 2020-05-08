 package ru.rutube.RutubeFeed.ctrl;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Parcel;
 import android.os.Parcelable;
 import android.support.v4.app.LoaderManager;
 import android.support.v4.content.CursorLoader;
 import android.support.v4.content.Loader;
 import android.support.v4.widget.CursorAdapter;
 import android.util.Log;
 import android.widget.ListAdapter;
 
 import com.android.volley.RequestQueue;
 import com.android.volley.VolleyError;
 import com.android.volley.toolbox.HttpClientStack;
 import com.android.volley.toolbox.JsonObjectRequest;
 import com.android.volley.toolbox.Volley;
 
 import ru.rutube.RutubeAPI.BuildConfig;
 import ru.rutube.RutubeAPI.HttpTransport;
 import ru.rutube.RutubeAPI.content.FeedContentProvider;
 import ru.rutube.RutubeAPI.models.Constants;
 import ru.rutube.RutubeAPI.models.Feed;
 import ru.rutube.RutubeAPI.models.FeedItem;
 import ru.rutube.RutubeAPI.models.User;
 import ru.rutube.RutubeAPI.requests.RequestListener;
 import ru.rutube.RutubeFeed.data.FeedCursorAdapter;
 
 /**
  * Created by tumbler on 08.07.13.
  */
 public class FeedController implements Parcelable {
 
     protected static final String VIEW_AUTHOR = "author";
     protected static final String VIEW_AVATAR = "avatar";
     protected static final String VIEW_CREATED = "created";
     protected static final String VIEW_FOOTER = "footer";
     protected static final String VIEW_TAG_TITLE = "tag_title";
     protected static final String VIEW_TAG_COMMENT = "tag_comment";
     protected static final String VIEW_TAG_CARD = "tag_card";
 
     protected static final String[] CLICKABLE = {
         VIEW_AVATAR,
         VIEW_FOOTER,
         VIEW_AUTHOR,
         VIEW_CREATED,
         VIEW_TAG_CARD,
         VIEW_TAG_COMMENT,
         VIEW_TAG_TITLE
     };
 
     private Feed getFeedModel() {
         if (mFeed == null)
             mFeed = new Feed(mFeedUri, mContext);
         return mFeed;
     }
 
     public void logout() {
         User.load(mContext).deleteToken(mContext);
         mFeed = null;
     }
 
 
     /**
      * Контракт пользовательского интерфейса
      */
     public interface FeedView {
         public ListAdapter getListAdapter();
         public void setListAdapter(ListAdapter adapter);
         public void setRefreshing();
         public void doneRefreshing();
         public void showError();
         public LoaderManager getLoaderManager();
         public void openPlayer(Uri uri, Uri thumbnailUri);
 
         public FeedCursorAdapter initAdapter();
 
         public boolean onItemClick(FeedCursorAdapter.ClickTag position, String viewTag);
 
         public void openFeed(Uri feedUri, String title);
 
         public void setSelectedItem(int mItemRequested);
 
         public int getCurrentPosition();
     }
 
     private static final int LOADER_ID = 1;
     private static final String LOG_TAG = FeedController.class.getName();
     private static final boolean D = BuildConfig.DEBUG;
     private Uri mFeedUri;
     private Feed mFeed;
     private Context mContext;
     private FeedView mView;
     private int mPerPage = 10;
     private RequestQueue mRequestQueue;
     private int mLoading = 0;
     private boolean mHasNext = true;
     private boolean mAttached = false;
     private int mUpdatedPage = 0;
 
 
     public FeedController(Uri feedUri, int itemRequested) {
         mContext = null;
         mView = null;
         mFeedUri = feedUri;
         mItemRequested = itemRequested;
     }
 
     /**
      * Получает последние обновления ленты
      */
     public void refresh() {
         if (D) Log.d(LOG_TAG, "Refreshing");
         loadPage(1, true);
     }
 
     /**
      * При возобновлении работы фрагмента проверят возможность обновить страницу.
      */
     public void checkLoadMore() {
         if (D) Log.d(LOG_TAG, "Force load first page");
         loadPage(1, true);
     }
 
     /**
      * По клику на элементе ленты открывает плеер
      * @param position индекс выбранного элемента
      */
     public void onListItemClick(int position) {
         if (D) Log.d(LOG_TAG, "onListItemClick");
         Cursor c = (Cursor) mView.getListAdapter().getItem(position);
         FeedItem item = Feed.loadFeedItem(mContext, c, mFeedUri);
         Uri uri = item.getVideoUri(mContext);
         mView.openPlayer(uri, item.getThumbnailUri());
     }
 
     /**
      * Присоединяется к контексту и пользовательскому интерфейсу,
      * инициализирует объекты, зависящие от активити.
      * @param context экземпляр активити
      * @param view пользовательский интерфейс
      */
     public void attach(Context context, FeedView view) {
         assert mContext == null;
         assert mView == null;
         mContext = context;
         mView = view;
         mRequestQueue = Volley.newRequestQueue(context,
             new HttpClientStack(HttpTransport.getHttpClient()));
         FeedCursorAdapter adapter = prepareFeedCursorAdapter();
         mView.getLoaderManager().initLoader(LOADER_ID, null, loaderCallbacks);
         mView.setListAdapter(adapter);
         if (D) Log.d(LOG_TAG, "Attach position: " + String.valueOf(mItemRequested));
         mView.setSelectedItem(mItemRequested);
         mAttached = true;
         loadPage(1);
     }
 
     /**
      * Отсоединяется от останавливаемой активити
      */
     public void detach() {
         mView.doneRefreshing();
         mRequestQueue.stop();
         mRequestQueue = null;
         mContext = null;
         mView = null;
         mAttached = false;
         mFeed = null;
     }
 
     // Реализация Parcelable
 
     @Override
     public int describeContents() {
         return 0;
     }
 
     @Override
     public void writeToParcel(Parcel parcel, int i) {
         parcel.writeParcelable(mFeedUri, i);
         parcel.writeInt(mItemRequested);
     }
 
     public static FeedController fromParcel(Parcel in) {
         Uri feedUri = in.readParcelable(Uri.class.getClassLoader());
         int itemRequested = in.readInt();
         return new FeedController(feedUri, itemRequested);
     }
 
     @SuppressWarnings("UnusedDeclaration")
     public static final Parcelable.Creator<FeedController> CREATOR
             = new Parcelable.Creator<FeedController>() {
         public FeedController createFromParcel(Parcel in) {
             return FeedController.fromParcel(in);
         }
 
         public FeedController[] newArray(int size) {
             return new FeedController[size];
         }
     };
 
     /**
      * Настраивает адаптер данных
      * @return
      */
     private FeedCursorAdapter prepareFeedCursorAdapter() {
         FeedCursorAdapter adapter = mView.initAdapter();
         adapter.setLoadMoreListener(loadMoreListener);
         adapter.setItemClickListener(itemClickListener);
         return adapter;
     }
 
     private int mItemRequested = 0;
     /**
      * Обрабатывает события "нужно загрузить следующую страницу", приходящие от адаптера
      */
     private FeedCursorAdapter.LoadMoreListener loadMoreListener = new FeedCursorAdapter.LoadMoreListener(){
 
         @Override
         public void onLoadMore() {
             ListAdapter adapter = mView.getListAdapter();
             if (mHasNext)
                 loadPage((adapter.getCount() + mPerPage) / mPerPage);
         }
 
         @Override
         public void onItemRequested(int position) {
             int currentPosition = mView.getCurrentPosition() + 1;
             if (currentPosition > 1)
                 mItemRequested = currentPosition;
             if (D) Log.d(LOG_TAG, String.format("onItemRequested: %d %d", mItemRequested, position));
             int page = (position / mPerPage) + 1;
             if (mLoading == 0 && mHasNext && page > mUpdatedPage) {
                 mUpdatedPage = page;
                 loadPage(page);
             }
         }
     };
 
     protected FeedCursorAdapter.ItemClickListener itemClickListener = new FeedCursorAdapter.ItemClickListener() {
         @Override
         public void onItemClick(FeedCursorAdapter.ClickTag dataTag, String viewTag) {
             boolean isSpecial = false;
             for (String t: CLICKABLE) {
                 if (t.equals(viewTag)){
                     isSpecial = true;
                     break;
                 }
             }
             if (isSpecial) {
                 // Клик по футеру, открываем ленту автора
                 if (D) Log.d(LOG_TAG, "Feed link click: " + String.valueOf(dataTag.href));
                 if (dataTag.href != null && !dataTag.href.equals(mFeedUri)) {
                     mView.openFeed(dataTag.href, dataTag.title);
                     return;
                 }
             }
             if (!mView.onItemClick(dataTag, viewTag))
                 onListItemClick(dataTag.position);
         }
     };
 
     /**
      * Обработчик ответа от API ленты
      */
     private RequestListener mLoadPageRequestListener = new RequestListener() {
         @Override
         public void onResult(int tag, Bundle result) {
             if (!mAttached)
                 return;
             FeedCursorAdapter listAdapter = (FeedCursorAdapter)mView.getListAdapter();
             if (listAdapter.getCount() == 0)
                 mContext.getContentResolver().notifyChange(getFeedModel().getContentUri(), null);
             mPerPage = result.getInt(Constants.Result.PER_PAGE);
             mHasNext = result.getBoolean(Constants.Result.HAS_NEXT);
             listAdapter.setPerPage(mPerPage);
             listAdapter.setHasMore(mHasNext);
             requestDone();
         }
 
         private void requestDone() {
             if (mLoading > 0) mLoading -= 1;
             if (mLoading == 0 && mView != null)
                 mView.doneRefreshing();
         }
 
         @Override
         public void onVolleyError(VolleyError error) {
             if (error.networkResponse == null)
             {
                if (mView!= null) mView.showError();
                 requestDone();
                 return;
             }
             if (D) Log.d(LOG_TAG, "VolleyError: " + String.valueOf(error.networkResponse.statusCode));
             if (mView != null && error.networkResponse.statusCode != 401)
                 mView.showError();
             else {
                 logout();
             }
             requestDone();
         }
 
         @Override
         public void onRequestError(int tag, RequestError error) {
             if (D) Log.d(LOG_TAG, "RequestError: " + error.getMessage());
             if (mView != null)
                 mView.showError();
             requestDone();
         }
     };
 
     /**
      * Обработчик запросов к БД
      */
     private LoaderManager.LoaderCallbacks<Cursor> loaderCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {
 
         @Override
         public Loader<Cursor> onCreateLoader(int loaderId, Bundle arg1) {
             return new CursorLoader(
                     mContext,
                     getFeedModel().getContentUri(),
                     FeedContentProvider.getProjection(getFeedModel().getContentUri()),
                     null,
                     null,
                     null
             );
         }
 
         @Override
         public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
             if (D) Log.d(LOG_TAG, "onLoadFinished " + String.valueOf(cursor.getCount()));
             if (mView == null) return;
             ((CursorAdapter) mView.getListAdapter()).swapCursor(cursor);
             mView.setSelectedItem(mItemRequested);
             if (D) Log.d(LOG_TAG, "setPosition: " + String.valueOf(mItemRequested));
             // Грузим следующую страницу только если кэш в БД невалидный
             if ((cursor.getCount() < mPerPage) && mHasNext) {
                 if (D) Log.d(LOG_TAG, "load more from olf");
                 loadPage((cursor.getCount() + mPerPage) / mPerPage);
             }
         }
 
         @Override
         public void onLoaderReset(Loader<Cursor> arg0) {
             ((CursorAdapter) mView.getListAdapter()).swapCursor(null);
         }
     };
 
     /**
      * Запрашивает страницу API ленты
      * @param page номер страницы с 1
      */
     private void loadPage(int page, boolean nocache) {
         if (mLoading != 0) {
             if (D) Log.d(LOG_TAG, "isLoading, returning");
             return;
         }
         mView.setRefreshing();
         mLoading += 1;
         JsonObjectRequest request = getFeedModel().getFeedRequest(page, mContext, mLoadPageRequestListener);
         if (nocache)
             mRequestQueue.getCache().remove(request.getCacheKey());
         mRequestQueue.add(request);
     }
 
     private void loadPage(int page) {
         loadPage(page, false);
     }
 
 }
