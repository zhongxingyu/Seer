 package com.allplayers.android;
 
 import java.lang.ref.WeakReference;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.util.LruCache;
 import android.support.v4.view.PagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.widget.ImageView;
 
 import com.allplayers.android.activities.AllplayersSherlockActivity;
 import com.allplayers.objects.PhotoData;
 import com.allplayers.rest.RestApiV1;
 import com.devspark.sidenavigation.SideNavigationView;
 import com.devspark.sidenavigation.SideNavigationView.Mode;
 
 public class PhotoPager extends AllplayersSherlockActivity {
 
     private ViewPager mViewPager;
     private PhotoPagerAdapter mPhotoAdapter;
     private PhotoData mCurrentPhoto;
     private int mCurrentPhotoIndex;
 
     /**
      * Called when the activity is first created, this sets up some variables,
      * creates the Action Bar, and sets up the Side Navigation Menu.
      * @param savedInstanceState: Saved data from the last instance of the
      * activity.
      * @TODO The side navigation menu does NOT work due to conflicting views.
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
 		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);  
 
         if (savedInstanceState != null) {
             mCurrentPhotoIndex = savedInstanceState.getInt("photoToStart");
         }
 
         super.onCreate(savedInstanceState);
         setContentView(R.layout.photo_pager);
 
         mCurrentPhoto = (new Router(this)).getIntentPhoto();
         mViewPager = new ViewPager(this);
         mPhotoAdapter = new PhotoPagerAdapter(this, mCurrentPhoto);
         mViewPager = (ViewPager) findViewById(R.id.viewpager);
         mViewPager.setAdapter(mPhotoAdapter);
         mViewPager.setCurrentItem(mCurrentPhotoIndex);
 
         actionbar.setTitle(getIntent().getStringExtra("album title"));
 
         sideNavigationView = (SideNavigationView)findViewById(R.id.side_navigation_view);
         sideNavigationView.setMenuItems(R.menu.side_navigation_menu);
         sideNavigationView.setMenuClickCallback(this);
         sideNavigationView.setMode(Mode.LEFT);
 
     }
 
     /**
      * Called before placing the activity in a background state. Saves the
      * instance data for the activity to be used the next time onCreate() is
      * called.
      * @param icicle: The bundle to add to.
      */
     protected void onSaveInstanceState(Bundle icicle) {
 
         super.onSaveInstanceState(icicle);
 
         mCurrentPhotoIndex = mViewPager.getCurrentItem();
         icicle.putInt("photoToStart", mCurrentPhotoIndex);
     }
 
     /**
      * @TODO EDIT ME
      */
     public class PhotoPagerAdapter extends PagerAdapter {
 
         private LruCache<String, Bitmap> mImageCache;
         private Context mContext;
         private List<PhotoData> photos;
 
         /**
          * @TODO EDIT ME
          * @param context:
          * @param item:
          */
         public PhotoPagerAdapter(Context context, PhotoData item) {
         	mContext = context;
         	final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
         	final int cacheSize = maxMemory / 8;
         	mImageCache = new LruCache<String, Bitmap>(cacheSize) {
                 @Override
                 protected int sizeOf(String key, Bitmap bitmap) {
                     // The cache size will be measured in kilobytes rather than
                     // number of items.
                    return bitmap.getByteCount() / 1024;
                 }
             };
 
             photos = new ArrayList<PhotoData>();
 
             PhotoData temp = item;
 
             while (temp.previousPhoto() != null) {
                 photos.add(0, temp.previousPhoto());
                 temp = temp.previousPhoto();
             }
 
             if (mCurrentPhotoIndex == 0) {
                 mCurrentPhotoIndex = photos.size();
             }
 
             photos.add(item);
 
             temp = item;
 
             while (temp.nextPhoto() != null) {
                 photos.add(temp.nextPhoto());
                 temp = temp.nextPhoto();
             }
 
         }
 
         /**
          * @TODO EDIT ME
          * @param collection:
          * @param position:
          */
         @Override
         public Object instantiateItem(View collection, int position) {
         	ImageView image = new ImageView(mContext);
             image.setImageResource(R.drawable.backgroundstate);
 
             if (mImageCache.get(position+"") != null) {
             	image.setImageBitmap(mImageCache.get(position+""));
                 ((ViewPager) collection).addView(image, 0);
                 return image;
             }
             new GetRemoteImageTask(image, position).execute(photos.get(position).getPhotoFull());
             ((ViewPager) collection).addView(image, 0);
 
             return image;
         }
 
         /**
          * @TODO EDIT ME
          * @param container:
          * @param position:
          * @param object:
          */
         @Override
         public void destroyItem(ViewGroup container, int position, Object object) {
 
             ((ViewPager) container).removeView((ImageView) object);
         }
 
         /**
          * @TODO Returns the size of the list of photos.
          */
         @Override
         public int getCount() {
 
             return photos.size();
         }
 
         /**
          * @TODO EDIT ME
          * @param view:
          * @param obj:
          */
         @Override
         public boolean isViewFromObject(View view, Object obj) {
 
             return view == obj;
         }
 
         /**
          * Get's a user's image using a rest call and displays it.
          */
         public class GetRemoteImageTask extends AsyncTask<Object, Void, Bitmap> {
         	private final WeakReference<ImageView> viewReference;
         	private int index;
             
             GetRemoteImageTask(ImageView im, int ind) {
             	viewReference = new WeakReference<ImageView>(im);
             	index = ind;
             }
             
             @Override
             protected void onPreExecute() {
             	((Activity) mContext).setProgressBarIndeterminateVisibility(true);
             }
 
             /**
              * Gets the requested image using a REST call.
              * @param photoUrl: The URL of the photo to fetch.
              */
             protected Bitmap doInBackground(Object... photoUrl) {
             	Bitmap b = RestApiV1.getRemoteImage((String) photoUrl[0]);
             	mImageCache.put(index+"", b);
                 return b;
             }
 
             /**
              * Adds the fetched image to an array of the album's images.
              * @param image: The image to be added.
              */
             protected void onPostExecute(Bitmap bm) {
             	((Activity) mContext).setProgressBarIndeterminateVisibility(false);
             	ImageView imageView = viewReference.get();
                 if( imageView != null ) {
                   imageView.setImageBitmap(bm);
                 }
             }
         }
     }
 }
