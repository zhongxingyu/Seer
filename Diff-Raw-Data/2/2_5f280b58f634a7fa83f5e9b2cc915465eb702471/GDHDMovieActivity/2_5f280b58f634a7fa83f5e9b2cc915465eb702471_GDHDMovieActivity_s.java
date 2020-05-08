 package com.dbstar.app;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import com.dbstar.R;
 import com.dbstar.app.alert.GDAlertDialog;
 import com.dbstar.app.media.GDPlayerUtil;
 import com.dbstar.model.ContentData;
 import com.dbstar.model.EventData;
 import com.dbstar.model.GDCommon;
 import com.dbstar.model.ProductItem;
 import com.dbstar.service.GDDataProviderService;
 import com.dbstar.model.Movie;
 import com.dbstar.model.GDDVBDataContract.Content;
 import com.dbstar.widget.GDAdapterView;
 import com.dbstar.widget.GDGridView;
 import com.dbstar.widget.GDAdapterView.OnItemSelectedListener;
 import com.dbstar.widget.GDScrollBar;
 
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class GDHDMovieActivity extends GDBaseActivity {
 	private static final String TAG = "GDHDMovieActivity";
 
 	private static final int COLUMN_ITEMS = 6;
 	private static final int PAGE_ITEMS = 12;
 	private static final int PageSize = PAGE_ITEMS;
 	int mPageNumber = 0;
 	int mPageCount = 0;
 	int mTotalCount = 0;
 
 	String mColumnId;
 	List<Movie[]> mPageDatas;
 
 	GDGridView mSmallThumbnailView;
 	MovieAdapter mAdapter;
 	int mSeletedItemIndex = 0;
 	GDScrollBar mScrollBar = null;
 
 	View mSelectedView = null;
 	boolean mReachPageEnd = false;
 	TextView mPageNumberView;
 	ImageView mViewMask = null;
 	boolean mEnterPlayer = false;
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.hdmovie_view);
 
 		Intent intent = getIntent();
 		mColumnId = intent.getStringExtra(Content.COLUMN_ID);
 		mMenuPath = intent.getStringExtra(INTENT_KEY_MENUPATH);
 		Log.d(TAG, "column id = " + mColumnId);
 		Log.d(TAG, "menu path = " + mMenuPath);
 		mPageDatas = new LinkedList<Movie[]>();
 
 		initializeView();
 	}
 
 	protected void initializeView() {
 		super.initializeView();
 
 		mPageNumberView = (TextView) findViewById(R.id.pageNumberView);
 
 		mScrollBar = (GDScrollBar) findViewById(R.id.scrollbar);
 
 		mSmallThumbnailView = (GDGridView) findViewById(R.id.gridview);
 
 		mViewMask = (ImageView) findViewById(R.id.view_mask);
 		
 		mSmallThumbnailView
 				.setOnItemSelectedListener(mThumbnailSelectedListener);
 
 		mAdapter = new MovieAdapter(this);
 		mSmallThumbnailView.setAdapter(mAdapter);
 		mSmallThumbnailView.setOnKeyListener(mThumbnailOnKeyListener);
 
 		mSmallThumbnailView.requestFocus();
 		mPageNumberView.setText(formPageText(0, 0));
 	}
 
 	public void onStart() {
 		super.onStart();
 
 		if (mAdapter.getCount() > 0) {
 			mSmallThumbnailView.setSelection(mSeletedItemIndex);
 		}
 
 		showMenuPath(mMenuPath.split(MENU_STRING_DELIMITER));
 		
 		mViewMask.setVisibility(View.GONE);
 		mEnterPlayer = false;
 	}
 	
 	public void onResume () {
 		super.onResume();
 		
 		mViewMask.setVisibility(View.GONE);
 		mEnterPlayer = false;
 	}
 
 	public void onDestroy() {
 		super.onDestroy();
 
 		for (int i = 0; mPageDatas != null && i < mPageDatas.size(); i++) {
 			Movie[] movies = mPageDatas.get(i);
 			for (int j = 0; j < movies.length; j++) {
 				if (movies[j].Thumbnail != null) {
 					movies[j].Thumbnail.recycle();
 				}
 			}
 		}
 	}
 
 	public void onServiceStart() {
 		super.onServiceStart();
 
 		mService.getPublications(this, mColumnId);
 	}
 
 	private void loadPrevPage() {
 		if (mPageNumber > 0) {
 			Log.d(TAG, "loadPrevPage");
 
 			mPageNumber--;
 			
 			loadPage(mPageNumber, PageSize - 1);
 		}
 	}
 
 	private void loadNextPage() {
 		Log.d(TAG, "loadNextPage");
 
 		if ((mPageNumber + 1) < mPageDatas.size()) {
 			mPageNumber++;
 			loadPage(mPageNumber, 0);
 		}
 	}
 	
 	private void loadFirstPage() {
 		mPageNumber = 0;
 		loadPage(0, 0);
 	}
 	
 	private boolean navigateUp() {
 		boolean ret = false;
 		int currentItem = mSmallThumbnailView.getSelectedItemPosition();
 		int pageNumber = mPageNumber;
 		int selected = 0;
 
 		if (currentItem >= COLUMN_ITEMS) {
 			return ret;
 		}
 		
 		if (pageNumber > 0) {
 			// load previous page
 			ret = true;
 			mPageNumber--;
 			selected = currentItem + COLUMN_ITEMS;
 			loadPage(mPageNumber, selected);
 		}
 //		else {
 //			int pageCount = mPageDatas.size();
 //			
 //			if (pageCount > 1) {
 //				// load the last page
 //				ret = true;
 //				pageNumber = mPageDatas.size() - 1;
 //				mPageNumber = pageNumber;
 //				
 //				Movie[] movies = mPageDatas.get(pageNumber);
 //				int pageSize = movies.length;
 //				
 //				if (pageSize > COLUMN_ITEMS) {
 //					// two lines
 //					selected = Math.min(currentItem + COLUMN_ITEMS, pageSize - 1);
 //				} else {
 //					// one lines
 //					selected = Math.min(currentItem, pageSize - 1);
 //				}
 //
 //				loadPage(pageNumber, selected);
 //			} else {
 //				// one page, loop
 //				Movie[] movies = mPageDatas.get(mPageNumber);
 //				int pageSize = movies.length;
 //				
 //				if (pageSize > COLUMN_ITEMS) {
 //					// two lines
 //					ret = true;
 //
 //					selected = Math.min(currentItem + COLUMN_ITEMS, pageSize - 1);
 //					mSmallThumbnailView.setSelection(selected);
 //				}
 //			}
 //		}
 		
 		return ret;
 	}
 
 	private boolean navigateDown() {
 		boolean ret = false;
 		int currentItem = mSmallThumbnailView.getSelectedItemPosition();
 
 		int pageNumber = mPageNumber;
 		int selected = 0;
 		int pageCount = mPageDatas.size();
 		Movie[] movies = mPageDatas.get(pageNumber);
 		int pageSize = movies.length;
 		
 		if (pageSize > COLUMN_ITEMS && currentItem < COLUMN_ITEMS) {
 			return ret;
 		}
 
 		if (pageNumber < pageCount - 1) {
 			// to next page
 			ret = true;
 			mPageNumber++;
 			selected = currentItem - COLUMN_ITEMS;
 			loadPage(mPageNumber, selected);
 		} else {
 			if (pageCount > 1) {
 				// to first page
 				if (currentItem < COLUMN_ITEMS) {
 					selected = currentItem;
 				} else {
 					selected = currentItem - COLUMN_ITEMS;
 				}
 				ret = true;
 				mPageNumber = 0;
 				loadPage(0, selected);
 			} else {
 				// only one page, loop
 			
 				if (currentItem >= COLUMN_ITEMS) {
 					ret = true;
 
 					selected = currentItem - COLUMN_ITEMS;
 					mSmallThumbnailView.setSelection(selected);
 				}
 			}
 		}
 		
 		return ret;
 	}
 
 	private void loadLastPage() {
 		int pageNumber = mPageDatas.size() - 1;
 		mPageNumber = pageNumber;
 		Movie[] movies = mPageDatas.get(pageNumber);
 		loadPage(pageNumber, movies.length - 1);
 	}
 	
 	private void loadPage(int pageNumber, int focusItem) {
 		Log.d(TAG, "loadPage " + pageNumber);
 
 		mPageNumberView.setText(formPageText(pageNumber + 1, mPageCount));
 
 		Movie[] movies = mPageDatas.get(pageNumber);
 
 		if (focusItem > movies.length - 1) {
 			focusItem = movies.length - 1;
 		} else if (focusItem < 0) {
 			focusItem = 0;
 		}
 
 		mAdapter.setDataSet(movies);
 		mSmallThumbnailView.setSelection(focusItem);
 		mAdapter.notifyDataSetChanged();
 
 		mScrollBar.setPosition(pageNumber);
 	}
 
 	private Movie getSelectedMovie() {
 		int currentItem = mSmallThumbnailView.getSelectedItemPosition();
 		Movie[] movies = mPageDatas.get(mPageNumber);
 		return movies[currentItem];
 	}
 
 	private void playMovie() {
 		Log.d(TAG, "playMovie");
 		Movie movie = getSelectedMovie();
 
 		String file = mService.getMediaFile(movie.Content);
 		String drmFile = mService.getDRMFile(movie.Content);
 
 		Log.d(TAG, " file = " + file);
 		
 		if (file == null || file.isEmpty()) {
 			alertFileNotExist();
 			mViewMask.setVisibility(View.GONE);
 			mEnterPlayer = false;
 			return;
 		}
 
 		if (drmFile != null && !drmFile.isEmpty() && !isSmartcardReady()) {
 			alertSmartcardInfo();
 			mViewMask.setVisibility(View.GONE);
 			mEnterPlayer = false;
 			return;
 		}
 
 		GDPlayerUtil.playVideo(this, null, movie.Content, file, drmFile, false);
 	}
 
 	public void updateData(int type, Object key, Object data) {
 		if (type == GDDataProviderService.REQUESTTYPE_GETPUBLICATION) {
 
 			ContentData[] contents = (ContentData[]) data;
 			Log.d(TAG, "update ");
 			if (contents != null && contents.length > 0) {
 				Log.d(TAG, "update " + contents.length);
 
 				mTotalCount = contents.length;
 				mPageCount = mTotalCount / PageSize;
 				int index = 0;
 				for (int i = 0; i < mPageCount; i++) {
 					Movie[] movies = new Movie[PageSize];
 					for (int j = 0; j < PageSize; j++, index++) {
 						movies[j] = new Movie();
 						movies[j].Content = contents[index];
 					}
 					mPageDatas.add(i, movies);
 				}
 
 				int remain = mTotalCount % PageSize;
 				if (remain > 0) {
 					mPageCount += 1;
 					Movie[] movies = new Movie[remain];
 					for (int i = 0; i < remain; i++, index++) {
 						movies[i] = new Movie();
 						movies[i].Content = contents[index];
 					}
 
 					mPageDatas.add(movies);
 				}
 
 				mPageNumber = 0;
 
 				// update views
 				updateViews(mPageDatas.get(mPageNumber));
 
 				Log.d(TAG, "update mPageCount " + mPageCount);
 
 				mRequestPageIndex = 0;
 				requestPageData(mRequestPageIndex);
 			}
 		} else if (type == GDDataProviderService.REQUESTTYPE_GETPUBLICATIONDRMINFO) {
 			String drmInfo = (String) data;
 			String publicationId = (String) key;
 			updateDrmInfo(publicationId, drmInfo);
 		}
 	}
 
 	int mRequestPageIndex = -1;
 	int mRequestCount = 0;
 
 	void requestPageData(int pageNumber) {
 		Movie[] movies = mPageDatas.get(pageNumber);
 		mRequestCount = movies.length;
 		for (int j = 0; j < movies.length; j++) {
 			mService.getDetailsData(this, pageNumber, j, movies[j].Content);
 		}
 	}
 
 	public void updateData(int type, int param1, int param2, Object data) {
 
 		if (type == GDDataProviderService.REQUESTTYPE_GETDETAILSDATA) {
 			int pageNumber = param1;
 			int index = param2;
 			Log.d(TAG, "updateData page number = " + pageNumber + " index = "
 					+ index);
 
 			mService.getImage(this, pageNumber, index, (ContentData) data);
 
 			mRequestCount--;
 			if (mRequestCount == 0) {
 				mRequestPageIndex++;
 				if (mRequestPageIndex < mPageCount) {
 					requestPageData(mRequestPageIndex);
 				}
 			}
 
 		} else if (type == GDDataProviderService.REQUESTTYPE_GETIMAGE) {
 			int pageNumber = param1;
 			int index = param2;
 			Log.d(TAG, "updateData page number = " + pageNumber + " index = "
 					+ index);
 
 			Movie[] movies = mPageDatas.get(pageNumber);
 			movies[index].Thumbnail = (Bitmap) data;
 
 			if (pageNumber == mPageNumber)
 				mAdapter.notifyDataSetChanged();
 		}
 	}
 
 	@Override
 	public void notifyEvent(int type, Object event) {
 		super.notifyEvent(type, event);
 
 		if (type == EventData.EVENT_DELETE) {
 			EventData.DeleteEvent deleteEvent = (EventData.DeleteEvent) event;
 			String publicationId = deleteEvent.PublicationId;
 			Movie[] movies = mPageDatas.get(mPageNumber);
 			int i = 0;
 			boolean found = false;
 			for (i = 0; i < movies.length; i++) {
 				ContentData content = movies[i].Content;
 				if (content.Id.equals(publicationId)) {
 					found = true;
 					break;
 				}
 			}
 
 			if (found) {
 				movePageItems(mPageNumber, i);
 			}
 
 			mPageCount = mPageDatas.size();
 			if (mPageCount > 0) {
 				// delete last page
 				if (mPageNumber == mPageCount) {
 					mPageNumber = mPageCount - 1;
 				}
 				movies = mPageDatas.get(mPageNumber);
 			} else {
 				movies = null;
 			}
 
 			updateViews(movies);
 		} else if (type == EventData.EVENT_UPDATE_PROPERTY) {
 			EventData.UpdatePropertyEvent updateEvent = (EventData.UpdatePropertyEvent) event;
 			String publicationId = updateEvent.PublicationId;
 			
 			if (mPageDatas.size() == 0) {
 				return;
 			}
 			
 			Movie[] movies = mPageDatas.get(mPageNumber);
 			int i = 0;
 			boolean found = false;
 			for (i = 0; i < movies.length; i++) {
 				ContentData content = movies[i].Content;
 				if (content.Id.equals(publicationId)) {
 					found = true;
 					break;
 				}
 			}
 
 			if (found) {
 				ContentData content = movies[i].Content;
 				updatePropery(content, updateEvent.PropertyName,
 						updateEvent.PropertyValue);
 			}
 		}
 	}
 
 	private void updatePropery(ContentData content, String propery, Object value) {
 		if (propery.equals(GDCommon.KeyBookmark)) {
 			content.BookMark = (Integer) value;
 		}
 	}
 
 	private void movePageItems(int pageNumber, int start) {		
 		Movie[] movies = mPageDatas.get(pageNumber);
 		
 		Log.d(TAG, " == movePageItems == page=" + pageNumber + 
 				" delete = " + start + " size=" + movies.length);
 
 		if (start == movies.length - 1 && start == 0) {
 			// the deleted item is the last one
 			// there is only one item in this page
 			mPageDatas.remove(pageNumber);
 			return;
 		}
 
 		for (int i = start; i < movies.length - 1; i++) {
 			movies[i] = movies[i + 1];
 			movies[i + 1] = null;
 		}
 
 		if (pageNumber < mPageCount - 1) {
 			Movie[] nextMovies = mPageDatas.get(pageNumber + 1);
 			movies[movies.length - 1] = nextMovies[0];
 
 			movePageItems(pageNumber + 1, 0);
 		} else {
 			// this is the last page, remove the last null item
 			Movie[] newMovies = new Movie[movies.length - 1];
 			for (int i = 0; i < newMovies.length; i++) {
 				newMovies[i] = movies[i];
 			}
 
 			Log.d(TAG, " == page size = " + mPageDatas.size()
 					+ " " + newMovies.length);
 
 			mPageDatas.set(pageNumber, newMovies);
 		}
 	}
 
 	private void updateViews(Movie[] movies) {
 		mPageNumberView.setText(formPageText(mPageNumber + 1, mPageCount));
 		mScrollBar.setRange(mPageCount);
 		mScrollBar.setPosition(mPageNumber);
 
 		mAdapter.setDataSet(movies);
 
 		if (movies != null && movies.length > 0) {
 			mSmallThumbnailView.setSelection(0);
 		}
 
 		mAdapter.notifyDataSetChanged();
 	}
 
 	private class MovieAdapter extends BaseAdapter {
 
 		private Movie[] mDataSet = null;
 
 		public class ViewHolder {
 			// TextView titleView;
 			ImageView thumbnailView;
 		}
 
 		public MovieAdapter(Context context) {
 		}
 
 		public void setDataSet(Movie[] dataSet) {
 			mDataSet = dataSet;
 		}
 
 		@Override
 		public int getCount() {
 			int count = 0;
 			if (mDataSet != null) {
 				count = mDataSet.length;
 			}
 
 			return count;
 		}
 
 		@Override
 		public Object getItem(int position) {
 			return null;
 		}
 
 		@Override
 		public long getItemId(int position) {
 			return 0;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 
 			ViewHolder holder = new ViewHolder();
 
 			if (mSmallThumbnailView.getSelectedItemPosition() == position) {
 				if (mSelectedView == null) {
 					LayoutInflater inflater = getLayoutInflater();
 					mSelectedView = inflater.inflate(
 							R.layout.small_thumbnail_item_focused, parent,
 							false);
 					// holder.titleView = (TextView) mSelectedView
 					// .findViewById(R.id.item_text);
 					holder.thumbnailView = (ImageView) mSelectedView
 							.findViewById(R.id.thumbnail);
 
 					mSelectedView.setTag(holder);
 				}
 
 				if (convertView != mSelectedView) {
 					convertView = mSelectedView;
 				}
 			} else {
 				if (convertView == mSelectedView) {
 					convertView = null;
 				}
 			}
 
 			if (null == convertView) {
 				LayoutInflater inflater = getLayoutInflater();
 				convertView = inflater.inflate(
 						R.layout.small_thumbnail_item_normal, parent, false);
 				// holder.titleView = (TextView) convertView
 				// .findViewById(R.id.item_text);
 				holder.thumbnailView = (ImageView) convertView
 						.findViewById(R.id.thumbnail);
 
 				convertView.setTag(holder);
 			} else {
 				holder = (ViewHolder) convertView.getTag();
 			}
 
 			Bitmap thumbnail = mDataSet[position].Thumbnail;
 			holder.thumbnailView.setImageBitmap(thumbnail);
 			// holder.titleView.setText(mDataSet[position].Content.Name);
 
 			return convertView;
 		}
 	}
 
 	OnItemSelectedListener mThumbnailSelectedListener = new OnItemSelectedListener() {
 
 		@Override
 		public void onItemSelected(GDAdapterView<?> parent, View view,
 				int position, long id) {
 			Log.d(TAG, "mSmallThumbnailView selected = " + position);
 
 			mSeletedItemIndex = position;
 		}
 
 		@Override
 		public void onNothingSelected(GDAdapterView<?> parent) {
 
 		}
 
 	};
 
 	View.OnKeyListener mThumbnailOnKeyListener = new View.OnKeyListener() {
 
 		@Override
 		public boolean onKey(View v, int keyCode, KeyEvent event) {
 			Log.d(TAG, "onKey " + keyCode);
 			boolean ret = false;
 			int action = event.getAction();
 			if (action == KeyEvent.ACTION_DOWN) {
 				switch (keyCode) {
 
 				case KeyEvent.KEYCODE_DPAD_LEFT: {
 					int currentItem = mSmallThumbnailView
 							.getSelectedItemPosition();
 					if (currentItem == PAGE_ITEMS / 2) {
 						mSmallThumbnailView.setSelection(currentItem - 1);
 						ret = true;
 					} else if (currentItem == 0) {
 						if (mPageNumber > 0) {
 							loadPrevPage();
 						} else {
 							loadLastPage();
 						}
 						ret = true;
 					} else {
 					}
 					break;
 				}
 				case KeyEvent.KEYCODE_DPAD_RIGHT: {
 
 					int currentItem = mSmallThumbnailView
 							.getSelectedItemPosition();
 					
 					if (currentItem == (mAdapter.getCount() - 1)) {
 						// the last item
 						if (mPageNumber < mPageDatas.size() - 1) {
 							loadNextPage();
 						} else {
 							if (mPageNumber > 0) {
 								loadFirstPage();
 							} else {
 								// only one page
 								mSmallThumbnailView.setSelection(0);
 							}
 						}
 						ret = true;
 					} else {
 						if (currentItem == (PAGE_ITEMS / 2 - 1)) {
 							mSmallThumbnailView.setSelection(currentItem + 1);
 							ret = true;
 						}
 					}
 
 					break;
 				}
 
 				case KeyEvent.KEYCODE_DPAD_UP: {
 					ret = navigateUp();
 					break;
 				}
 
 				case KeyEvent.KEYCODE_DPAD_DOWN: {
 					ret = navigateDown();
 					break;
 				}
 
 				case KeyEvent.KEYCODE_DPAD_CENTER:
 				case KeyEvent.KEYCODE_ENTER: {
 					if (!mEnterPlayer) {
 						mEnterPlayer = true;
 						mHandler.postDelayed(new Runnable() {
 							public void run() {
 								playMovie();
 							}
 						}, 400);
 					}
 					ret = true;
 					break;
 				}
 				
 				// display drm info
 				case KeyEvent.KEYCODE_MENU: {
 					displayDrmInfo();
 					break;
 				}
 
 				default:
 					break;
 				}
 
 			}
 			return ret;
 		}
 	};
 	
 	
 	public Dialog onCreateDialog(int id) {
 		Dialog dialog = null;
 		dialog = super.onCreateDialog(id);
 
 		switch (id) {
 		case DLG_ID_DRMINFO: {
 			mDrmInfoDialog = new GDDrmInfoDialog(this);
 			mDrmInfoDialog.setOnShowListener(mDrmDlgOnShowListener);
 			dialog = mDrmInfoDialog;
 			break;
 		}
 		}
 		
 		return dialog;
 	}
 	
 	DialogInterface.OnShowListener mDrmDlgOnShowListener = new DialogInterface.OnShowListener() {
 
 		@Override
 		public void onShow(DialogInterface dialog) {
 			if (mDrmInfo != null) {
 				mDrmInfoDialog.setData(mDrmInfo);
 			}
 
 		}
 	};
 	
 	// DRM info	
 	GDDrmInfoDialog mDrmInfoDialog = null;
 	ProductItem[] mDrmInfo = null;
 
 	void displayDrmInfo() {
 		Log.d(TAG, "displayDrmInfo");
 
 		Movie movie = getSelectedMovie();
 		String drmFile = mService.getDRMFile(movie.Content);
 
 		if (drmFile != null && !drmFile.isEmpty()) {
 			mDrmInfo = null;
 			mService.getPublicationDrmInfo(this, movie.Content.Id);
 
 			if (mDrmInfoDialog == null) {
 				showDialog(DLG_ID_DRMINFO);
 			} else {
 				mDrmInfoDialog.show();
 			}
 		}
 	}
 	
 	void updateDrmInfo(String publicationId, String drmInfoData) {
 		Movie movie = getSelectedMovie();
		if (!publicationId.equals(movie.Content.Id)) {
 			Log.d(TAG, "the drminfo is not for publication " + publicationId);
 			return;
 		}
 		
 		mDrmInfo = null;
 		String[] items = drmInfoData.split("\n");
 		if (items.length == 0) {
 			Log.d(TAG, " no product info !");
 		} else {
 			ArrayList<ProductItem> products = new ArrayList<ProductItem>();
 			for (int i = 0; i < items.length; i++) {
 				String[] item = items[i].split("\t");
 
 				if (item.length == 0) {
 					continue;
 				}
 
 				ProductItem product = new ProductItem();
 				if (item.length > 0)
 					product.ContentID = item[0];
 
 				if (item.length > 1)
 					product.OperatorID = item[1];
 
 				if (item.length > 2)
 					product.ProductID = item[2];
 
 				if (item.length > 3)
 					product.StartTime = item[3];
 
 				if (item.length > 4)
 					product.EndTime = item[4];
 
 				products.add(product);
 			}
 
 			if (products.size() > 0) {
 				mDrmInfo = products.toArray(new ProductItem[products.size()]);
 			}
 		}
 		
 		if (mDrmInfoDialog != null) {
 			mDrmInfoDialog.setData(mDrmInfo);
 		}
 	}
 	
 }
