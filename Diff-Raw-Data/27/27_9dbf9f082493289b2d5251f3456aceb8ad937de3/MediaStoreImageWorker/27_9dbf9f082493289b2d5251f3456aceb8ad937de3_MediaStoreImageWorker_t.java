 package info.winiex.androidbasement.image.worker;
 
 import info.winiex.androidbasement.image.cache.MemImageCache;
 import info.winiex.androidbasement.image.type.MediaStoreImage;
 import info.winiex.androidbasement.image.utils.ImageUtils;
 import info.winiex.androidbasement.image.utils.MediaStoreImageUtils;
 
 import java.lang.ref.WeakReference;
 
 import android.graphics.Bitmap;
 import android.util.Log;
 import android.widget.ImageView;
 
 public class MediaStoreImageWorker extends ImageWorker {
 
 	private int mLoadImageType;
 
 	private int mLoadImageLocation;
 
 	private static int LOAD_THUMBNAIL = 1;
 
 	private static int LOAD_ORIGIN = 2;
 
 	private static int LOAD_INTERNAL = 1;
 
 	private static int LOAD_EXTERNAL = 2;
 
 	private MediaStoreImage mMediaStoreImage;
 
 	private MemImageCache mMemImageCache;
 
 	public MediaStoreImageWorker(MediaStoreImage mediaStoreImage,
 			ImageView imageView) {
 		mMediaStoreImage = mediaStoreImage;
 		mImageReference = new WeakReference<ImageView>(imageView);
 		mMemImageCache = MemImageCache.getInstance();
 	}
 
 	public int getImageId() {
 		return mMediaStoreImage.getImageId();
 	}
 
 	@Override
 	protected Bitmap doInBackground(Integer... params) {
 		super.doInBackground(params);
 		mLoadImageType = params[2];
 		mLoadImageLocation = params[3];
 
 		Bitmap bitmapResult = null;
 
 		if (mLoadImageLocation == LOAD_INTERNAL) {
 
 			if (mLoadImageType == LOAD_THUMBNAIL) {
 				int thumbnailType = params[4];
 				bitmapResult = MediaStoreImageUtils
 						.decodeThumbnailInternalBitmapFromMediaStore(
 								getImageId(), thumbnailType);
 			} else if (mLoadImageType == LOAD_ORIGIN) {
 				bitmapResult = MediaStoreImageUtils
 						.decodeOriginInternalBitmapFromMediaStore(getImageId(),
 								mReqWidth, mReqHeight);
 			}
 
 		} else if (mLoadImageLocation == LOAD_EXTERNAL) {
 
 			if (mLoadImageType == LOAD_THUMBNAIL) {
 				int thumbnailType = params[4];
 				bitmapResult = MediaStoreImageUtils
 						.decodeThumbnailExternalBitmapFromMediaStore(
 								getImageId(), thumbnailType);
 			} else if (mLoadImageType == LOAD_ORIGIN) {
 				bitmapResult = MediaStoreImageUtils
 						.decodeOriginExternalBitmapFromMediaStore(getImageId(),
 								mReqWidth, mReqHeight);
 			}
 
 		}
 
 		mMemImageCache.addBitmapToMemCache(mMediaStoreImage.getMemCacheKey(),
 				bitmapResult);
 
 		return bitmapResult;
 	}
 
 	@Override
 	protected void onPostExecute(Bitmap bitmap) {
 		super.onPostExecute(bitmap);
 
 		if (mImageReference != null && bitmap != null) {
 			ImageView imageView = mImageReference.get();
 			if (imageView != null) {
 				imageView.setImageBitmap(bitmap);
 			}
 		}
 	}
 
 	@Override
 	protected boolean isThisWorkerMySelf(ImageWorker thisWorker) {
 		if (thisWorker instanceof MediaStoreImageWorker) {
 			MediaStoreImageWorker thisMediaStoreImageWorker = (MediaStoreImageWorker) thisWorker;
 			if (thisMediaStoreImageWorker.getImageId() == getImageId()) {
 				return true;
 			} else {
 				return false;
 			}
 		}
 
 		return false;
 	}
 
 	public void loadBitmap(ImageView imageView) {
 		final String imageCacheKey = mMediaStoreImage.getMemCacheKey();
 
 		final Bitmap bitmapMemCached = mMemImageCache
 				.getBitmapFromMemCache(imageCacheKey);
 
 		// Found the bitmap in memory cache.
 		if (bitmapMemCached != null) {
 			imageView.setImageBitmap(bitmapMemCached);
 			return;
 		}
 
 		if (cancelPotentialWork(imageView)) {
 			final ImageWorker imageWorker = new MediaStoreImageWorker(
 					mMediaStoreImage, imageView);
 			final ImageWorker.AsyncDrawable asyncDrawable = new ImageWorker.AsyncDrawable(
 					ImageUtils.defaultBitmap, imageWorker);
 			imageView.setImageDrawable(asyncDrawable);
 
 			if (mMediaStoreImage.isThumbnail()) {
 				if (mMediaStoreImage.isInternal()) {
 					imageWorker.execute(mMediaStoreImage.getReqWidth(),
 							mMediaStoreImage.getReqHeight(), LOAD_THUMBNAIL,
 							LOAD_INTERNAL, mMediaStoreImage.getThumbnailType());
 				} else {
 					imageWorker.execute(mMediaStoreImage.getReqWidth(),
 							mMediaStoreImage.getReqHeight(), LOAD_THUMBNAIL,
 							LOAD_EXTERNAL, mMediaStoreImage.getThumbnailType());
 				}
 
 			} else {
 				if (mMediaStoreImage.isInternal()) {
 					imageWorker.execute(mMediaStoreImage.getReqWidth(),
 							mMediaStoreImage.getReqHeight(), LOAD_ORIGIN,
 							LOAD_INTERNAL);
 				} else {
 					imageWorker.execute(mMediaStoreImage.getReqWidth(),
 							mMediaStoreImage.getReqHeight(), LOAD_ORIGIN,
 							LOAD_EXTERNAL);
 				}
 			}
 		}
 	}
 }
