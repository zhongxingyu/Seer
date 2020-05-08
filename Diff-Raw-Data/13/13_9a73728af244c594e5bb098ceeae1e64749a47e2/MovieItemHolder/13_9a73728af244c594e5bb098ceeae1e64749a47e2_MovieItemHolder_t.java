 package fr.eyal.datalib.sample.netflix.ui;
 
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.view.animation.AlphaAnimation;
 import android.view.animation.Animation;
 import android.widget.ImageView;
 import android.widget.TextView;
 import fr.eyal.datalib.sample.cache.BitmapMemoryLruCache;
 import fr.eyal.datalib.sample.cache.CacheableBitmapDrawable;
 import fr.eyal.datalib.sample.netflix.data.model.movieimage.MovieImage;
 import fr.eyal.datalib.sample.netflix.data.model.top100.ItemTop100;
 import fr.eyal.datalib.sample.netflix.data.service.NetflixDataManager;
 import fr.eyal.datalib.sample.netflix.data.service.NetflixService;
 import fr.eyal.datalib.sample.netflix.util.Resources;
 import fr.eyal.lib.data.model.ResponseBusinessObject;
 import fr.eyal.lib.data.service.DataManager;
 import fr.eyal.lib.data.service.DataManager.OnDataListener;
 import fr.eyal.lib.data.service.model.BusinessResponse;
 import fr.eyal.lib.data.service.model.DataLibRequest;
 
 public class MovieItemHolder implements OnDataListener {
 
 	public static final String BIG_APPENDIX = "BIG";
 	
 	public ImageView image;
 	public TextView text;
 	public ItemTop100 item;
 	public boolean mBigImage;
 	private NetflixDataManager mDataManager;
 	private BitmapMemoryLruCache mBitmapCache;
 	private Context mContext;
 	
 	public MovieItemHolder(Context context, boolean bigImage) {
 		mDataManager = NetflixDataManager.getInstance();
 		mBitmapCache = Resources.getInstance().mBitmapCache;
 		mContext = context;
 		mBigImage = bigImage;
 	}
 	
 	@Override
 	public void onRequestFinished(int requestId, boolean suceed, BusinessResponse response) {
 
 		if(!suceed)
 			return;
 
 		switch (response.webserviceType) {
 		
 		case NetflixService.WEBSERVICE_MOVIEIMAGE:
 			
 			MovieImage img = (MovieImage) response.response;
 			Bitmap bitmap = img.image.get();
 			if(bitmap != null){
 				CacheableBitmapDrawable cacheBmp = new CacheableBitmapDrawable(mContext.getResources(), item.getPosterName(), bitmap, CacheableBitmapDrawable.RecyclePolicy.DISABLED);
 				mBitmapCache.put(cacheBmp);
 
 				//we cache another bitmap if we are on a big element
 				//this is dirty but handles the different size of the big elements on the selection panel
 				if(mBigImage){
 					cacheBmp = new CacheableBitmapDrawable(mContext.getResources(), item.getPosterName() + BIG_APPENDIX, bitmap, CacheableBitmapDrawable.RecyclePolicy.DISABLED);
 					mBitmapCache.put(cacheBmp);
 				}
 				image.post(new UpdatePoster(cacheBmp, image));
 			}
 				
 			break;
 
 		default:
 			break;
 		}
 	}
 	
 	@Override
 	public void onCacheRequestFinished(int requestId, ResponseBusinessObject response) {
 		
 		if(response instanceof MovieImage){
 
 			MovieImage img = (MovieImage) response;
 			item.image = img;
 			
			Bitmap bitmap = img.getBitmap();
 			if(bitmap != null){
 				CacheableBitmapDrawable cacheBmp = new CacheableBitmapDrawable(mContext.getResources(), item.getPosterName(), bitmap, CacheableBitmapDrawable.RecyclePolicy.DISABLED);
 				mBitmapCache.put(cacheBmp);
 
 				if(mBigImage){
 					cacheBmp = new CacheableBitmapDrawable(mContext.getResources(), item.getPosterName() + "BIG", bitmap, CacheableBitmapDrawable.RecyclePolicy.DISABLED);
 					mBitmapCache.put(cacheBmp);
 				}
 				image.post(new UpdatePoster(cacheBmp, image));
 			}
 			else {
 				try {
 					if(mDataManager != null)
 						mDataManager.getMovieImage(DataManager.TYPE_NETWORK, this, item.getImageUrl(), DataLibRequest.OPTION_NO_OPTION, null, null);
 				} catch (UnsupportedEncodingException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		
 	}
 	
 	
 	@Override
 	public void onDataFromDatabase(int code, ArrayList<?> data) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	public class UpdatePoster implements Runnable {
 
 		CacheableBitmapDrawable mImage;
 		ImageView mView;
 		Animation mFadeIn;
 		
 		public UpdatePoster(CacheableBitmapDrawable bitmap, ImageView view){
 			mImage = bitmap;
 			mView = view;
 			mFadeIn = new AlphaAnimation(0, 1);
 			mFadeIn.setDuration(300);
 		}
 		
 		@Override
 		public void run() {
 			Animation anim = mView.getAnimation();
 			if(anim != null){
 				anim.cancel();
 				anim.reset();
 				mView.setImageDrawable(mImage);
 				anim.startNow();
 			} else {
 				mView.setImageDrawable(mImage);
 				mView.startAnimation(mFadeIn);
 			}
 		}
 	}
 }
