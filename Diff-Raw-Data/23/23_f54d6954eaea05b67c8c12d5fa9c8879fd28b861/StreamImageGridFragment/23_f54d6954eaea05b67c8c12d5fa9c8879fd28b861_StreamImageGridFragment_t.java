 package com.example.android.bitmapfun.ui;
 
 import java.util.ArrayList;
 
 import android.annotation.TargetApi;
 import android.app.ActivityOptions;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.util.TypedValue;
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
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.GridView;
 import android.widget.ImageView;
 import android.widget.Toast;
 
 import com.example.android.bitmapfun.BuildConfig;
 import com.example.android.bitmapfun.R;
 import com.example.android.bitmapfun.util.ImageCache.ImageCacheParams;
 import com.example.android.bitmapfun.util.ImageFetcher;
 import com.example.android.bitmapfun.util.Utils;
 
 /**
  * The main fragment that powers the ImageGridActivity screen. Fairly straight forward GridView
  * implementation with the key addition being the ImageWorker class w/ImageCache to load children
  * asynchronously, keeping the UI nice and smooth and caching thumbnails for quick retrieval. The
  * cache is retained over configuration changes like orientation change so the images are populated
  * quickly if, for example, the user rotates the device.
  */
 public class StreamImageGridFragment extends Fragment implements AdapterView.OnItemClickListener {
     private static final String TAG = "ImageGridFragment";
     private static final String IMAGE_CACHE_DIR = "thumbs";
     private static final String STREAM_LIST = "streams";
     private static final String STREAM_NAME = "stream_name";
     private static final String STREAM_ID = "stream_id";
 
     private int mImageThumbSize;
     private int mImageThumbSpacing;
     private ImageAdapter mAdapter;
     private ImageFetcher mImageFetcher;
     private  ArrayList<ConnexusImage> streamList;
     private  long streamId;
     private String streamName;
 
     /**
      * Factory method to generate a new instance of the fragment given an image number.
      *
      * @param imageUrl The image url to load
      * @return A new instance of ImageDetailFragment with imageNum extras
      */
     //public static ImageGridFragment newInstance( ConnexusStream.List streams) {
     public static StreamImageGridFragment newInstance( ConnexusImage.List streams, long id, String name) {
         final StreamImageGridFragment f = new StreamImageGridFragment();
 
         final Bundle args = new Bundle();
         args.putParcelableArrayList(STREAM_LIST, streams);
         args.putString(STREAM_NAME, name);
         args.putLong(STREAM_ID, id);
         f.setArguments(args);
         return f;
     }
    
     /**
      * Empty constructor as per the Fragment documentation
      */
     public StreamImageGridFragment() {
     }
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setHasOptionsMenu(true);
         
         Bundle b = getArguments();
         streamList = b.getParcelableArrayList(STREAM_LIST);
         streamName = b.getString(STREAM_NAME);
         streamId = b.getLong(STREAM_ID);
 
         mImageThumbSize = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
         mImageThumbSpacing = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_spacing);
 
         mAdapter = new ImageAdapter(getActivity());
 
         ImageCacheParams cacheParams = new ImageCacheParams(getActivity(), IMAGE_CACHE_DIR);
 
         cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of app memory
 
         // The ImageFetcher takes care of loading images into our ImageView children asynchronously
         mImageFetcher = new ImageFetcher(getActivity(), mImageThumbSize);
         mImageFetcher.setLoadingImage(R.drawable.empty_photo);
         mImageFetcher.addImageCache(getActivity().getSupportFragmentManager(), cacheParams);
     }
 
     @Override
     public View onCreateView(
             LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 
         final View v = inflater.inflate(R.layout.stream_image_grid_fragment, container, false);
         final GridView mGridView = (GridView) v.findViewById(R.id.gridView);
         mGridView.setAdapter(mAdapter);
 
         Button button = (Button) v.findViewById(R.id.button_upload);
         button.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 // Do something in response to button click
             	Intent intent = new Intent(getActivity(), GetImageActivity.class);
             	intent.putExtra(GetImageActivity.EXTRA_IMAGE_DATA_STREAM_NAME, streamName);
             	intent.putExtra(GetImageActivity.EXTRA_IMAGE_DATA_STREAM_ID, streamId);
                 startActivity(intent);
            }
         });
         button = (Button) v.findViewById(R.id.button_streams);
         button.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 // Do something in response to button click
             	Intent intent = new Intent(getActivity(), ImageGridActivity.class);
                 startActivity(intent);
            }
         });
         
         mGridView.setOnItemClickListener(this);
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
                                         //(mGridView.getWidth() / numColumns) - mImageThumbSpacing;
                                 		(mGridView.getWidth() / 4) - mImageThumbSpacing;
                               
                                //mAdapter.setNumColumns(numColumns); We hard code the number of columns to 4
                                mAdapter.setNumColumns(4);
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
 
     @Override
     public void onResume() {
         super.onResume();
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
         ArrayList<String> as = new ArrayList<String>();
         for(int l=0;l<streamList.size();l++) {
         	as.add(streamList.get(l).bkUrl);
         }
         i.putStringArrayListExtra(ImageDetailActivity.EXTRA_IMAGE_DATA_URL, as);
         if (Utils.hasJellyBean()) {
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
 
     @Override
     public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
         inflater.inflate(R.menu.main_menu, menu);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.clear_cache:
                 mImageFetcher.clearCache();
                 Toast.makeText(getActivity(), R.string.clear_cache_complete_toast,
                         Toast.LENGTH_SHORT).show();
                 return true;
         }
         return super.onOptionsItemSelected(item);
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
             //return Images.imageThumbUrls.length + mNumColumns;
             return streamList.size() + mNumColumns;
         }
 
         @Override
         public Object getItem(int position) {
             return position < mNumColumns ?
                     //null : Images.imageThumbUrls[position - mNumColumns];
             		//null : streamList.get(position - mNumColumns).coverImageUrl;
     				null : streamList.get(position - mNumColumns).bkUrl;
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
             //mImageFetcher.loadImage(Images.imageThumbUrls[position - mNumColumns], imageView);
             //mImageFetcher.loadImage(streamList.get(position - mNumColumns).coverImageUrl, imageView);
             mImageFetcher.loadImage(streamList.get(position - mNumColumns).bkUrl, imageView);
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
