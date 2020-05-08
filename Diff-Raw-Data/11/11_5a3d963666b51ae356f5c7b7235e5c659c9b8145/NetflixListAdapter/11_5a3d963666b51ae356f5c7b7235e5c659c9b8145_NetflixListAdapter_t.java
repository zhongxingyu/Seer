 package fr.eyal.datalib.sample.netflix.fragment.adapter;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.os.Looper;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.animation.AlphaAnimation;
 import android.view.animation.Animation;
 import android.widget.AbsListView;
 import android.widget.AbsListView.OnScrollListener;
 import android.widget.AbsListView.RecyclerListener;
 import android.widget.BaseAdapter;
 import android.widget.GridView;
 import android.widget.ImageView;
 import android.widget.TextView;
 import fr.eyal.datalib.sample.cache.BitmapMemoryLruCache;
 import fr.eyal.datalib.sample.cache.CacheableBitmapDrawable;
 import fr.eyal.datalib.sample.netflix.R;
 import fr.eyal.datalib.sample.netflix.fragment.NetflixListFragment;
 import fr.eyal.datalib.sample.netflix.fragment.model.MovieItem;
 import fr.eyal.datalib.sample.netflix.util.Resources;
 import fr.eyal.lib.util.Out;
 
 /**
  * @author Eyal LEZMY
  *
  */
 public class NetflixListAdapter extends BaseAdapter implements OnScrollListener, RecyclerListener{
 
	private ArrayList<MovieItem> mArray;
 	NetflixListFragment mFragment;
 	GridView mGridParent = null;
 	UpdateContent mUpdateContentRunnable = new UpdateContent();
 	int mListFirst = 0;
 	int mListCount = -1;
 	boolean mListScrolled = false;
 	BitmapMemoryLruCache mBitmapCache;
 	
 	public NetflixListAdapter(NetflixListFragment fragment) {
 		super();
		mArray = new ArrayList<MovieItem>();
 		mFragment = fragment;
		setData(mArray);
 		mBitmapCache = Resources.getInstance().mBitmapCache;
 	}
 	
 	@Override
 	public int getCount() {
 		if(getData() != null)
 			return getData().size();
 		return 0;
 	}
 
 	@Override
 	public Object getItem(int position) {
 		if(getData() != null)
 			return getData().get(position);
 		return null;
 	}
 
 	@Override
 	public long getItemId(int position) {
 		return position;
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		
 		ItemViewHolder holder;
 		
 		if(mGridParent == null) {
 			mGridParent = (GridView) parent;
 			mGridParent.setOnScrollListener(this);
 			mGridParent.setRecyclerListener(this);
 		}
 		
 		if(convertView == null){
 			LayoutInflater inflater = (LayoutInflater) mFragment.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
             convertView = inflater.inflate(R.layout.movie_item, parent, false);
  
             holder = new ItemViewHolder();
             holder.text = (TextView) convertView.findViewById(R.id.item_text);
             holder.image = (ImageView) convertView.findViewById(R.id.item_image);
             
             convertView.setTag(holder);
             
         } else {
             holder = (ItemViewHolder) convertView.getTag();
         }
 
 		MovieItem item = getData().get(position);
 		holder.item = item;
 
 		if(item != null){
 			
 			//we create the title
 			String title = item.getLabel(position);
 
 			holder.text.setText(title);
 
 			
 			boolean isImageSet = setImageFromItemOrCache(holder, item);
 			
 			//if the image is not set we ask for a poster request
 			if(!isImageSet) {
 				holder.image.setImageResource(R.drawable.movie_back);
 				mFragment.loadMoviePoster(item);
 			}
 		}
 		
 		return convertView;
 	}
 
 	/**
 	 * Set the item's poster on the list from the DrawableCache or the item's content
 	 * 
 	 * @param holder
 	 * @param item
 	 * @return
 	 */
 	public boolean setImageFromItemOrCache(ItemViewHolder holder, MovieItem item) {
 
 		if(item.getImage() != null){
 
 			if(item.getPosterName() != null) {
 				//we get the poster from the cache
 				CacheableBitmapDrawable cacheBitmap = mBitmapCache.get(item.getPosterName());
 				if(cacheBitmap != null){
 					cacheBitmap.setBeingUsed(true); //we set the bitmap as used by the application
 					holder.image.setImageDrawable(cacheBitmap);
 					return true;
 				}
 			}
 
 			//we get the poster from the item
 			Bitmap bitmap = item.getPoster(false);
 			//if we found the poster
 			//we set the poster on the view
 			if(bitmap != null) {
 				holder.image.setImageBitmap(bitmap);
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	public static class ItemViewHolder{
 		public ImageView image;
 		public TextView text;
 		public MovieItem item;
 	}
 	
 	/**
 	 * Update the poster of a specific item on the list. This method apply the modification if the item is actually shown.
 	 * 
 	 * @param movie
 	 */
 	public void updatePoster(MovieItem movie){
 		
 		Bitmap bmp = movie.getPoster(false);
 		CacheableBitmapDrawable cacheBmp = null;
 		if(bmp != null){
 			cacheBmp = new CacheableBitmapDrawable(mFragment.getResources(), movie.getPosterName(), bmp, CacheableBitmapDrawable.RecyclePolicy.DISABLED);
 			mBitmapCache.put(cacheBmp);
 		}
 		
 		//if we have the parent view
 		if(mGridParent != null){
 			
 			int position = getData().indexOf(movie);
 			if(position < 0)
 				return;
 			
 			if(!mListScrolled) {
 				mListFirst = mGridParent.getFirstVisiblePosition();
 				mListCount = mGridParent.getChildCount();
  			}
 			
 			//if the item is displayed
 			if(mListFirst <= position && position <= (mListFirst+mListCount)){
 				View v = null;
 				int count = 0;
 				//we get the view eventually several times if needed
 				//workaround because sometimes the view is not available on the GridView
 				while(v == null && count < 10){
 					
 					v = mGridParent.getChildAt(position-mListFirst);
 					count++;
 					try {
 						Thread.sleep(100);
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 					if(v != null){
 						break;
 					}
 				}
 				//if after the tests the view is still null
 				if(v == null){
 					return; //we give up the method
 				}
 				ItemViewHolder holder = (ItemViewHolder) v.getTag();
 				
 				
 				if(cacheBmp != null){
 
 					if (Looper.getMainLooper().getThread() == Thread.currentThread()){
 						updateImageView(cacheBmp, holder.image);
 						Out.d("TEST", "UI THREADDDDD!!!!!!!!!!!!! IMAGE");
 						
 					} else {
						if(mFragment != null && mFragment.getActivity() != null)
							mFragment.getActivity().runOnUiThread(new UpdatePoster(cacheBmp, holder.image));
 						Out.d("TEST", "NO UI THREAD :-( IMAGE");
 					}
 					
 				} else {
 					mFragment.loadMoviePoster(movie);
 				}
 			}
 		}
 	}
 
 	
 	
 	/**
 	 * This override manage to launch the action on the UI thread
 	 */
 	@Override
 	public void notifyDataSetChanged() {
 		if(Looper.myLooper() == Looper.getMainLooper())
 			super.notifyDataSetChanged();
 		else{
 			Activity a = mFragment.getActivity();
 			if(a != null)
 				a.runOnUiThread(mUpdateContentRunnable);
 		}
 	}
 	
 	public class UpdatePoster implements Runnable {
 
 		CacheableBitmapDrawable mImage;
 		ImageView mView;
 		
 		public UpdatePoster(CacheableBitmapDrawable image, ImageView view){
 			mImage = image;
 			mView = view;
 		}
 		
 		@Override
 		public void run() {
 			updateImageView(mImage, mView);
 		}
 	}
 
 	public void updateImageView(CacheableBitmapDrawable image, ImageView view) {
 		Animation fadeIn = new AlphaAnimation(0, 1);
 		fadeIn.setDuration(300);
 		
 		Animation anim = view.getAnimation();
 		if(anim != null){
 			anim.cancel();
 			anim.reset();
 			view.setImageDrawable(image);
 			image.setBeingUsed(true);
 			anim.startNow();
 		} else {
 			view.setImageDrawable(image);
 			view.startAnimation(fadeIn);
 		}
 	}
 
 	public class UpdateContent implements Runnable {
 
 		public UpdateContent(){
 		}
 		
 		@Override
 		public void run() {
 			notifyDataSetChanged();
 		}
 		
 	}
 
 	@Override
 	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
 		if(visibleItemCount == 0)
 			return;
 		
 		mListScrolled = true;
 		mListFirst = firstVisibleItem;
 		mListCount = visibleItemCount;
 		
 		mFragment.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
 	}
 
 	@Override
 	public void onScrollStateChanged(AbsListView view, int scrollState) {
 		mFragment.onScrollStateChanged(view, scrollState);
 	}
 
 	@Override
 	public void onMovedToScrapHeap(View view) {
 		ItemViewHolder holder = (ItemViewHolder) view.getTag();
 		if(holder == null)
 			return;
 		
 		Animation anim = holder.image.getAnimation();
 		if(anim != null && anim.hasStarted()){
 			anim.cancel();
 			anim.reset();
 			holder.image.clearAnimation();
 		}
 		
 		//we remove the used state of the image on the cache
 		if(holder.item != null && holder.item.getPosterName() != null){
 			CacheableBitmapDrawable cacheBitmap = mBitmapCache.get(holder.item.getPosterName());
 			if(cacheBitmap != null)
 				cacheBitmap.setBeingUsed(false);
 		}
 	}
 
 	/**
 	 * @return the mArray
 	 */
 	public ArrayList<MovieItem> getData() {
 		return mArray;
 	}
 
 	/**
 	 * @param mArray the mArray to set
 	 */
 	public void setData(ArrayList<MovieItem> mArray) {
 		this.mArray = mArray;
 	}
 
 }
