 package com.photonoter;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.provider.BaseColumns;
 import android.provider.MediaStore;
 import android.provider.MediaStore.Images;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.GridView;
 import android.widget.ImageView;
 import android.widget.Toast;
 
 import com.photonoter.imaging.BackgroundThumbnailLoader;
 import com.photonoter.imaging.BitmapUtil;
 import com.photonoter.imaging.OnThumbnailLoadedHandler;
 
 public class PickPhotoActivity extends Activity {
 
     private static final String LOG_TAG = "PickPhotoActivity";
     
     private static final String NEWEST_FIRST_SORT_ORDER = BaseColumns._ID + " DESC";
 
     private ImageAdapter mImageAdapter;
 
     private GridView mImageGrid;
 
     private View mEmptyView;
 
     private Handler mBackgroundHandler;
 
     private OnThumbnailLoadedHandler mUiHandler;
     
     private PhotoBackWriterApp app;
     
     private boolean leaving;
 
     @Override
     public void onCreate(final Bundle aSavedInstanceState) {
         super.onCreate(aSavedInstanceState);
         
         app = PhotoBackWriterApp.getApp(this);
         
         setContentView(R.layout.choose_photos);
 
         mEmptyView = findViewById(R.id.choose_photos_empty);
 
         String instructions = "Pick a photo to write on the back of. Store your memories!";
         
         Toast.makeText(this, instructions, Toast.LENGTH_LONG).show();
 
         mImageGrid = (GridView)findViewById(R.id.choose_photos_grid);
         mUiHandler = new OnThumbnailLoadedHandler();
         mBackgroundHandler = new BackgroundThumbnailLoader(getApplicationContext(), mUiHandler);
 
         loadAdapter();
     }
 
     private void loadAdapter() {
         if (null != mImageAdapter) {
             return;
         }
 
         // Initialize grid of selectable images
         mImageAdapter = new ImageAdapter();
         mImageGrid.setAdapter(mImageAdapter);
 
         if (mImageAdapter.getCount() < 1) {
             mEmptyView.setVisibility(View.VISIBLE);
             mImageGrid.setVisibility(View.GONE);
         }
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         loadAdapter();
         leaving = false;
     }
 
     @Override
     protected void onPause() {
         super.onPause();
 
         mImageGrid.setAdapter(null);
         mImageAdapter.close();
         mImageAdapter = null;
 
         mBackgroundHandler.removeCallbacksAndMessages(null);
         mUiHandler.removeCallbacksAndMessages(null);
     }
 
     public static boolean isSdPresent() {
         return Environment.getExternalStorageState().equals(
                 Environment.MEDIA_MOUNTED);
 
     }
 
     public class ImageAdapter extends BaseAdapter {
 
         private final LayoutInflater mInflater;
 
         private Cursor mCursor;
 
         private int dbIdColumnIndex;
 
         public ImageAdapter() {
             mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
             loadData();
         }
 
         private void loadData() {
             close();
            
             final String[] columns = {
                     BaseColumns._ID
             };
             
             final String selection = null;
             final String[] selectionArgs = null;
 
             mCursor = Images.Media.query(
                     getContentResolver(),
                     MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                     columns,
                     selection,
                     selectionArgs,
                     NEWEST_FIRST_SORT_ORDER);
 
             if (null == mCursor) {
                 return;
             }
 
             dbIdColumnIndex = mCursor.getColumnIndex(BaseColumns._ID);
 
             notifyDataSetChanged();
         }
 
 		public void close() {
             if (null != mCursor && !mCursor.isClosed()) {
                 mCursor.close();
                 mCursor = null;
                 notifyDataSetChanged();
             }
         }
 
         @Override
         public int getCount() {
             return null == mCursor ? 0 : mCursor.getCount();
         }
 
         @Override
         public Object getItem(final int position) {
             return getItemId(position);
         }
 
         @Override
         public long getItemId(final int position) {
             if (null == mCursor) {
                 return -1;
             }
 
             if (!mCursor.moveToPosition(position)) {
                 return -1;
             }
 
             return mCursor.getInt(dbIdColumnIndex);
         }
 
         @Override
         public View getView(final int position, final View convertView, final ViewGroup parent) {
 
             final ViewGroup layout;
             if (convertView == null) {
                 layout = (ViewGroup)mInflater.inflate(R.layout.choose_photos_item, null);
             } else {
                 layout = (ViewGroup)convertView;
             }
             final ImageView image = (ImageView)layout.findViewById(R.id.choose_photos_item_thumbnail);
 
             final int imageId = (int)getItemId(position);
             image.setTag(imageId);
             image.setVisibility(View.VISIBLE);
 
             final boolean hasNotes = BitmapUtil.hasNotes(PickPhotoActivity.this, imageId);
             
             final View icon = layout.findViewById(R.id.choose_photos_item_icon);
             icon.setVisibility(hasNotes? View.VISIBLE : View.GONE);
             
             final View frame = layout.findViewById(R.id.choose_photos_item_frame);
             if ( hasNotes ) {
             	frame.setBackgroundResource( R.drawable.white_rounded_and_shadow );
             	frame.setPadding(10, 10, 10, 40);
             } else {
             	frame.setBackgroundDrawable(null);
             }
             
            layout.setOnClickListener(new OnClickListener() {
 				@Override
				public void onClick(View v) {
             	Log.i(LOG_TAG, "onClick");
                 	
 	                if (!leaving) {
 	                	PhotoBackWriterApp.pickedImageId = imageId;
 	                	
 	                    Intent i = new Intent(PickPhotoActivity.this, AnnotatePhotoActivity.class);
 	                    startActivity(i);
                 	}
                     leaving = true;
 				}
 			});
 
             image.setImageResource(R.drawable.ic_launcher);
             mBackgroundHandler.sendMessage(mBackgroundHandler.obtainMessage(0, imageId, 0,
                     image));
 
             return layout;
         }
     }
 
 }
