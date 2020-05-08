 package com.globant.mobile.handson;
 
 import java.io.File;
 import java.util.zip.Inflater;
 
 import android.annotation.SuppressLint;
 import android.annotation.TargetApi;
 import android.app.ActivityOptions;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.util.TypedValue;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.ActionMode;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.view.ViewTreeObserver;
 import android.widget.AbsListView;
 import android.widget.AdapterView;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.BaseAdapter;
 import android.widget.GridView;
 import android.widget.ImageView;
 import android.widget.Toast;
 
 import com.globant.mobile.handson.media.BitmapCache.ImageCacheParams;
 import com.globant.mobile.handson.media.BitmapFetcher;
 import com.globant.mobile.handson.provider.Bitmaps;
 
 /**
  * A simple {@link android.support.v4.app.Fragment} subclass. Activities that
  * contain this fragment must implement the
  * {@link ImageGrid.OnFragmentInteractionListener} interface to handle
  * interaction events. Use the {@link ImageGrid#newInstance} factory method to
  * create an instance of this fragment.
  * 
  */
 public class ImageGrid extends Fragment implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener{
 	 private static final String TAG = "ImageGridFragment";
 	 private static final String IMAGE_CACHE_DIR = "thumbs";
 
 	 private int mImageThumbSize;
 	 private int mImageThumbSpacing;
 	 private ImageAdapter mAdapter;
 	 private BitmapFetcher mImageFetcher;
 	 private Inflater mInflater;
 	 private ActionMode mActionMode;
 
 	
     public ImageGrid() {}
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setHasOptionsMenu(true);
 
         mImageThumbSize = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
         mImageThumbSpacing = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_spacing);
 
         mAdapter = new ImageAdapter(getActivity());
 
         ImageCacheParams cacheParams = new ImageCacheParams(getActivity(), IMAGE_CACHE_DIR);
 
         cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of app memory
 
         // The ImageFetcher takes care of loading images into our ImageView children asynchronously
         mImageFetcher = new BitmapFetcher(getActivity(), mImageThumbSize);
         mImageFetcher.setLoadingImage(R.drawable.empty_photo);
         mImageFetcher.addImageCache(getActivity().getSupportFragmentManager(), cacheParams);
     }
 
     @Override
     public View onCreateView(
             LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 
         final View v = inflater.inflate(R.layout.fragment_image_grid, container, false);
         final GridView mGridView = (GridView) v.findViewById(R.id.gridView);
         mGridView.setAdapter(mAdapter);        
         mGridView.setOnItemLongClickListener(this);
         mGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
             @Override
             public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                 // Pause fetcher to ensure smoother scrolling when flinging
                 if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                     mImageFetcher.setPauseWork(true);
                 } else {
                     mImageFetcher.setPauseWork(false);
                 }
             }
 
             @Override
             public void onScroll(AbsListView absListView, int firstVisibleItem,
                     int visibleItemCount, int totalItemCount) {
             }
         });
         //Setting the Context Menu for the GridVew id API level is lower than Honeycomb
         if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB){
         	registerForContextMenu(mGridView);
         }else{
         	mGridView.setOnItemClickListener(this);
         }
         // This listener is used to get the final width of the GridView and then calculate the
         // number of columns and the width of each column. The width of each column is variable
         // as the GridView has stretchMode=columnWidth. The column width is used to set the height
         // of each view so we get nice square thumbnails.
         mGridView.getViewTreeObserver().addOnGlobalLayoutListener(
                 new ViewTreeObserver.OnGlobalLayoutListener() {
                     @Override
                     public void onGlobalLayout() {
                         if (mAdapter.getNumColumns() == 0) {
                             final int numColumns = (int) Math.floor(
                                     mGridView.getWidth() / (mImageThumbSize + mImageThumbSpacing));
                             if (numColumns > 0) {
                                 final int columnWidth =
                                         (mGridView.getWidth() / numColumns) - mImageThumbSpacing;
                                 mAdapter.setNumColumns(numColumns);
                                 mAdapter.setItemHeight(columnWidth);
                                 if (BuildConfig.DEBUG) {
                                     Log.d(TAG, "onCreateView - numColumns set to " + numColumns);
                                 }
                             }
                         }
                     }
                 });
 
         return v;
     }
 
     @TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	@Override
     public void onResume() {
         super.onResume();
         if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
         	final GridView mGridView = (GridView) this.getActivity().findViewById(R.id.gridView);
         	mGridView.clearChoices();
         }
         mImageFetcher.setExitTasksEarly(false);
         mAdapter.notifyDataSetChanged();
     }
 
     @Override
     public void onPause() {
         super.onPause();
         mImageFetcher.setPauseWork(false);
         mImageFetcher.setExitTasksEarly(true);
         mImageFetcher.flushCache();
     }
 
     @Override
     public void onDestroy() {
         super.onDestroy();
         mImageFetcher.closeCache();
     }
 
     @TargetApi(16)
     @Override
     public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
         final Intent i = new Intent(getActivity(), ImageDetailActivity.class);
         i.putExtra(ImageDetailActivity.EXTRA_IMAGE, (int) id);
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
             // makeThumbnailScaleUpAnimation() looks kind of ugly here as the loading spinner may
             // show plus the thumbnail image in GridView is cropped. so using
             // makeScaleUpAnimation() instead.
             ActivityOptions options =
                     ActivityOptions.makeScaleUpAnimation(v, 0, 0, v.getWidth(), v.getHeight());
             getActivity().startActivity(i, options.toBundle());
         } else {
             startActivity(i);
         }
     }
     
     @TargetApi(Build.VERSION_CODES.HONEYCOMB)
     @Override
 	public boolean onItemLongClick(AdapterView<?> parent, View v, int position,
 			long id) {
     	//Setting the Context Action Bar if API Level is Honeycomb and higher
         if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
         	if(mActionMode != null){
         		return false;
         	}
         	
         	final int index = position;
         	ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
 				
 				@Override
 				public boolean onPrepareActionMode(ActionMode mode, Menu menu) {					
 					return false;
 				}
 				
 				@Override
 				public void onDestroyActionMode(ActionMode mode) {
 					mActionMode = null;
 					
 				}
 				
 				@Override
 				public boolean onCreateActionMode(ActionMode mode, Menu menu) {
 					// Inflate a menu resource providing context menu items
 			        MenuInflater inflater = mode.getMenuInflater();
 			        inflater.inflate(R.menu.image_context_bar, menu);
 			        return true;
 				}
 				
 				@Override
 				public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
 					switch(item.getItemId()){
 					case R.id.action_delete:{
 						deleteBitmap(index);
 						return true;
 						}
 					default:
 						return false;
 					}
 				}
 			};
         	
         	mActionMode = this.getActivity().startActionMode(mActionModeCallback);
         	v.setSelected(true);
         	v.setPressed(true);
         	return true;
         }else{
         	return false;
         }
 	}
 
 
     @Override
     public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
         inflater.inflate(R.menu.main, menu);
     }
     
     @Override
     public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo){
     	super.onCreateContextMenu(menu, view, menuInfo);
     	
     	MenuInflater inflater = this.getActivity().getMenuInflater();
     	inflater.inflate(R.menu.image_context_bar, menu);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {        
         return super.onOptionsItemSelected(item);
     }
     
 	@Override
     public boolean onContextItemSelected(MenuItem item){
     	AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
     	
         switch (item.getItemId()) {
             case R.id.action_delete:
                 deleteBitmap(info.position);            	
                 return true;
             case R.id.action_share:                
                 return true;
             default:
                 return super.onContextItemSelected(item);
         }
     }
 
     private void deleteBitmap(int position) {
     	final GridView mGridView = (GridView)this.getActivity().findViewById(R.id.gridView);
     	String bitmapPath = (String)mGridView.getAdapter().getItem(position);    	
     	File fileToDelete = new File(bitmapPath);
     	if(fileToDelete.exists()){
     		fileToDelete.delete();
     		mAdapter.notifyDataSetChanged();
     	}
 	}
 	/**
      * The main adapter that backs the GridView. This is fairly standard except the number of
      * columns in the GridView is used to create a fake top row of empty views as we use a
      * transparent ActionBar and don't want the real top row of images to start off covered by it.
      */
     private class ImageAdapter extends BaseAdapter {
 
         private final Context mContext;
         private int mItemHeight = 0;
         private int mNumColumns = 0;
         private int mActionBarHeight = 0;
         private GridView.LayoutParams mImageViewLayoutParams;
 
         public ImageAdapter(Context context) {
             super();
             mContext = context;
             mImageViewLayoutParams = new GridView.LayoutParams(
                     LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
             // Calculate ActionBar height
             TypedValue tv = new TypedValue();
             if (context.getTheme().resolveAttribute(
                     android.R.attr.actionBarSize, tv, true)) {
                 mActionBarHeight = TypedValue.complexToDimensionPixelSize(
                         tv.data, context.getResources().getDisplayMetrics());
             }
         }
 
         @Override
         public int getCount() {
             // Size + number of columns for top empty row
             return Bitmaps.imageThumbUrls.length + mNumColumns;
         }
 
         @Override
         public Object getItem(int position) {
             return position < mNumColumns ?
                     null : Bitmaps.imageThumbUrls[position - mNumColumns];
         }
 
         @Override
         public long getItemId(int position) {
             return position < mNumColumns ? 0 : position - mNumColumns;
         }
 
         @Override
         public int getViewTypeCount() {
             // Two types of views, the normal ImageView and the top row of empty views
             return 2;
         }
 
         @Override
         public int getItemViewType(int position) {
             return (position < mNumColumns) ? 1 : 0;
         }
 
         @Override
         public boolean hasStableIds() {
             return true;
         }
 
         @Override
         public View getView(int position, View convertView, ViewGroup container) {
             // First check if this is the top row
             if (position < mNumColumns) {
                 if (convertView == null) {
                     convertView = new View(mContext);
                 }
                 // Set empty view with height of ActionBar
                 convertView.setLayoutParams(new AbsListView.LayoutParams(
                         ViewGroup.LayoutParams.MATCH_PARENT, mActionBarHeight));
                 return convertView;
             }
 
             // Now handle the main ImageView thumbnails
             ImageView imageView;
             if (convertView == null) { // if it's not recycled, instantiate and initialize
                 imageView = new RecyclingImageView(mContext);
                 imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                 imageView.setLayoutParams(mImageViewLayoutParams);
             } else { // Otherwise re-use the converted view
                 imageView = (ImageView) convertView;
             }
 
             // Check the height matches our calculated column width
             if (imageView.getLayoutParams().height != mItemHeight) {
                 imageView.setLayoutParams(mImageViewLayoutParams);
             }
 
             // Finally load the image asynchronously into the ImageView, this also takes care of
             // setting a placeholder image while the background thread runs
             mImageFetcher.loadImage(Bitmaps.imageThumbUrls[position - mNumColumns], imageView);
             return imageView;
         }
 
         /**
          * Sets the item height. Useful for when we know the column width so the height can be set
          * to match.
          *
          * @param height
          */
         public void setItemHeight(int height) {
             if (height == mItemHeight) {
                 return;
             }
             mItemHeight = height;
             mImageViewLayoutParams =
                     new GridView.LayoutParams(LayoutParams.MATCH_PARENT, mItemHeight);
             mImageFetcher.setImageSize(height);
             notifyDataSetChanged();
         }
 
         public void setNumColumns(int numColumns) {
             mNumColumns = numColumns;
         }
 
         public int getNumColumns() {
             return mNumColumns;
         }
     }
 	
 }
