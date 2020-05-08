 package fr.eyal.datalib.sample.netflix.fragment;
 
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Calendar;
 
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.util.SparseArray;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.AbsListView;
 import android.widget.AbsListView.OnScrollListener;
 import android.widget.GridView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import fr.eyal.datalib.sample.netflix.R;
 import fr.eyal.datalib.sample.netflix.data.model.movieimage.MovieImage;
 import fr.eyal.datalib.sample.netflix.data.service.NetflixService;
 import fr.eyal.datalib.sample.netflix.fragment.adapter.NetflixListAdapter;
 import fr.eyal.datalib.sample.netflix.fragment.model.MovieItem;
 import fr.eyal.datalib.sample.netflix.fragment.model.MovieItemResponse;
 import fr.eyal.lib.data.model.ResponseBusinessObject;
 import fr.eyal.lib.data.service.ServiceHelper;
 import fr.eyal.lib.data.service.model.BusinessResponse;
 import fr.eyal.lib.data.service.model.ComplexOptions;
 import fr.eyal.lib.util.Out;
 
 public abstract class NetflixListFragment extends NetflixFragment implements OnScrollListener {
 
 	RelativeLayout mRootView;
 	GridView mGridView;
 	View mEmptyView;
 	NetflixListAdapter mAdapter;
 	SparseArray<MovieItem> mPendingItem;
 	ArrayList<MovieItem> mPendingItemCache;
 	int mScrollState;
 	private static Object sharedLock = new Object(); //the shared lock
 	float mItemHeight;
 	
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		mAdapter = new NetflixListAdapter(this);
 		mPendingItem = new SparseArray<MovieItem>();
 		mPendingItemCache = new ArrayList<MovieItem>();
 		super.onCreate(savedInstanceState);
 		setRetainInstance(true);
 		mItemHeight = getResources().getDimension(R.dimen.item_height_small);
 
 		try {
 			int requestId = callDataCache(null, null);
 			if(requestId == ServiceHelper.BAD_REQUEST_ID)
 				requestId = callDataNetwork(null, null);
 			if(requestId == ServiceHelper.BAD_REQUEST_ID)
 				mRequestIds.add(requestId);
 
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
 	}
 	
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		
 //		if(container != null)
 //			mGridView = (GridView) inflater.inflate(R.layout.fgmt_new, null, false);
 //		else
 //			mGridView = (GridView) inflater.inflate(R.layout.fgmt_new, container);
 
 		if(container != null)
 			mRootView = (RelativeLayout) inflater.inflate(R.layout.fgmt_new, null, false);
 		else
 			mRootView = (RelativeLayout) inflater.inflate(R.layout.fgmt_new, container);
 
 //		View emptyView = inflater.inflate(R.layout.empty_grid, null);
 //		getActivity().addContentView(emptyView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
 //		mGridView.setEmptyView(emptyView);
 		mEmptyView = mRootView.findViewById(android.R.id.empty);
 		mGridView = (GridView) mRootView.findViewById(R.id.gridview);
 		mGridView.setEmptyView(mEmptyView);
 		mGridView.setAdapter(mAdapter);
 		mGridView.setOnScrollListener(this);
 		
 		return mRootView;
 	}
 	
 	
 	/**
 	 * Ask to display the movie's poster asynchronously
 	 * 
 	 * @param item the item concerned by the display
 	 */
 	public void loadMoviePoster(MovieItem item){
 		
 //		if(mScrollState == OnScrollListener.SCROLL_STATE_FLING){
 //			Out.e("", "UPDATE " + "Scrolling" + item.title);
 //			return;
 //		}
 		
 		synchronized (mPendingItemCache) {
 
 			if(mPendingItemCache.contains(item)){
 				Out.e("", "UPDATE " + "No item " + item.getLabel(-1));
 				return;
 			}
 			
 			try {
 				synchronized (sharedLock) {
 					Out.e("", "UPDATE " + "Request Cache");
 					launchMovieImageCacheRequest(item, 1, true);
 				}
 			} catch (UnsupportedEncodingException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/**
 	 * Launch a cache request for movie image
 	 * 
 	 * @param item the {@link MovieItem} object associated to the request
 	 * @param inSampleSize bitmap option the sample size
 	 * @param inJustDecodeBounds ask to just decode bounds of the bitmap
 	 * 
 	 * @throws UnsupportedEncodingException
 	 */
 	public void launchMovieImageCacheRequest(MovieItem item, int inSampleSize, boolean inJustDecodeBounds) throws UnsupportedEncodingException {
 		ComplexOptions options = new ComplexOptions();
 		BitmapFactory.Options bmpOption = new BitmapFactory.Options();
 		bmpOption.inSampleSize = inSampleSize;
 		bmpOption.inJustDecodeBounds = inJustDecodeBounds;
 		options.putBitmapOptions(bmpOption);
 		
 		Out.w("", "ITEM "+item);
 		int requestId = callImageCache(item.getImageUrl(), options, null);
 		mRequestIds.add(requestId);
 		mPendingItem.append(requestId, item);
 		mPendingItemCache.add(item);
 	}
 	
 	
 	/*
 	 * OnDataListener management
 	 */
 	
 	@Override
 	public void onCacheRequestFinished(int requestId, ResponseBusinessObject response) {
 
 		mRequestIds.remove(Integer.valueOf(requestId));
 
 		if(response instanceof MovieItemResponse){
 			MovieItemResponse movie = (MovieItemResponse) response;
 
 			//we update the page content
 			updateMovie(movie);
 
 			//we compute the update time
 			Calendar updateTime = Calendar.getInstance();
 			updateTime.setTimeInMillis(movie.getUpdatedAt().getTimeInMillis());
 			updateTime.add(Calendar.MINUTE, movie.getTtl());
 			
 			//we update the content if the ttl is consumed
 			if(updateTime.compareTo(Calendar.getInstance()) <= 0) {
 				try {
 					callDataNetwork(null, null);
 				} catch (UnsupportedEncodingException e) {
 					e.printStackTrace();
 				}
 			}
 			
 		} else if(response instanceof MovieImage){
 			
 			MovieImage movieImage = (MovieImage) response;
 
 			MovieItem item = getItemAndTreatPendings(requestId);
 			
 			//if we didn't receive the image's soft reference
 			if(movieImage.image == null && item != null){
 				
 				BitmapFactory.Options options = movieImage.lastOptions;
 				if(options != null && options.outHeight != 0){
 
 					//we calculate the sample size
 					int sampleSize = (int) (options.outHeight/mItemHeight);
 					Out.d("", "SAMPLE SIZE"+sampleSize);
 					
 					//then we ask for the image content including the sample size
 					try {
 						launchMovieImageCacheRequest(item, sampleSize, false);
 					} catch (UnsupportedEncodingException e) {
 						e.printStackTrace();
 					}
 					return;
 				}
 			}
 					
 			//if the cache object does not contains the good information
 			if(movieImage.image == null || movieImage.image.get() == null){
 
 				//if the list is scrolling we don't ask for a network request
 				if(mScrollState == OnScrollListener.SCROLL_STATE_FLING){
 					String title = (item != null) ? item.getLabel(-1) : "";
 					Out.e("", "UPDATE " + "Scrolling so stop " + title);
 					return;
 				}
 
 				//we ask for a network request
 				try {
 					if(item != null){
 						
 						int id = callImageNetwork(item.getImageUrl(), null, null);
 						mRequestIds.add(id);
 						mPendingItem.append(id, item);
 					} else {
 					}
 					
 				} catch (UnsupportedEncodingException e) {
 					e.printStackTrace();
 				}
 				
 			} else {
 				
 				//we ask to update the ImageView
 				updateMovieImage(item, movieImage);
 			}
 		}
 		
 	}
 
 	private MovieItem getItemAndTreatPendings(int requestId) {
 		MovieItem item = null;
 		
 		synchronized (sharedLock) {
 			item = mPendingItem.get(requestId);
 			mPendingItem.remove(requestId);
 			if(item != null){
 				mPendingItemCache.remove(item);
 			}
 		}
 		return item;
 	}
 
 	@Override
 	public void onDataFromDatabase(int code, ArrayList<?> data) {
 	}
 
 	@Override
 	public void onRequestFinished(int requestId, boolean suceed, BusinessResponse response) {
 
 		mRequestIds.remove(Integer.valueOf(requestId));
 
 		if(!suceed)
 			return;
 		
 		switch (response.webserviceType) {
 
 		case NetflixService.WEBSERVICE_MOVIEIMAGE:
 			MovieItem item = getItemAndTreatPendings(requestId);
 			updateMovieImage(item, (MovieImage) response.response);
 			break;
 
 		default:
 			if(response.response instanceof MovieItemResponse)
 				updateMovie((MovieItemResponse) response.response);
 			break;
 		}
 		
 	}
 
 	/*
 	 * Content update
 	 */
 	
 	/**
 	 * Update the movies list
 	 * 
 	 * @param response the {@link MovieItemResponse} content
 	 */
 	@SuppressWarnings("unchecked")
 	private void updateMovie(MovieItemResponse response) {
 		
 		ArrayList<MovieItem> items = (ArrayList<MovieItem>) response.getItems();
 		
		if(items.size() == 0 && mEmptyView instanceof TextView)
 			((TextView)mEmptyView).setText(getResources().getString(R.string.search_error));
 
 		else {
 			mAdapter.setData((ArrayList<MovieItem>) response.getItems());
 			mAdapter.notifyDataSetChanged();
 		}
 	}
 
 
 	/**
 	 * Update the movie image
 	 * 
 	 * @param requestId the request id to find the corresponding movie item
 	 * @param response the {@link MovieImage} received
 	 * 
 	 * @return the {@linMovieItem00} item concerned by the updating or null if it is not found
 	 */
 	private MovieItem updateMovieImage(MovieItem item, MovieImage response) {
 		//we update the object
 		if(item == null || response == null)
 			return null;
 		item.setImage(response);
 		//we update the current displayed list
 		mAdapter.updatePoster(item);
 		return item;
 	}
 
 	
 	/*
 	 * Scroll management
 	 */
 	
 	@Override
 	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
 	}
 
 	@Override
 	public  void  onScrollStateChanged(AbsListView view, int scrollState) {
 			
 		//if the list finish a fling
 		if(mScrollState == OnScrollListener.SCROLL_STATE_FLING && scrollState != mScrollState){
 			mScrollState = scrollState;
 			
 			int first = mGridView.getFirstVisiblePosition();
 			int size = mGridView.getChildCount();
 			for (int i = 0; i < size; i++) {
 				MovieItem item = (MovieItem) mAdapter.getItem(first+i);
 				
 				if(item != null){
 					View v = mGridView.getChildAt(i);
 					if(v != null){
 						NetflixListAdapter.ItemViewHolder holder = (NetflixListAdapter.ItemViewHolder) v.getTag();
 						if(holder != null){
 							
 							boolean isImageSet = mAdapter.setImageFromItemOrCache(holder, item);
 							if(!isImageSet)
 								loadMoviePoster(item);
 						}
 					}
 				}
 
 			}
 		}
 		mScrollState = scrollState;
 	}
 	
 	
 	/**
 	 * Function called when the data of the page are loaded from the cache
 	 * 
 	 * @return the requestId generated by the request
 	 */
 	protected abstract int callDataCache(ComplexOptions complexOptionsCache, ComplexOptions complexOptionsNetwork) throws UnsupportedEncodingException;
 	
 	/**
 	 * Function called when the data of the page are loaded from the network
 	 * 
 	 * @return the requestId generated by the request
 	 */
 	protected abstract int callDataNetwork(ComplexOptions complexOptionsCache, ComplexOptions complexOptionsNetwork) throws UnsupportedEncodingException;
 	
 	/**
 	 * Function called when an image of the page is loaded from the cache
 	 * 
 	 * @return the requestId generated by the request
 	 */
 	protected abstract int callImageCache(String url, ComplexOptions complexOptionsCache, ComplexOptions complexOptionsNetwork) throws UnsupportedEncodingException;
 	
 	/**
 	 * Function called when an image of the page is loaded from the network
 	 * 
 	 * @return the requestId generated by the request
 	 */
 	protected abstract int callImageNetwork(String url, ComplexOptions complexOptionsCache, ComplexOptions complexOptionsNetwork) throws UnsupportedEncodingException;
 
 }
