 package de.raptor2101.GalDroid.Activities.Listeners;
 
 import android.graphics.Bitmap;
 import android.widget.Gallery;
 import android.widget.ImageView;
 import de.raptor2101.GalDroid.Activities.Helpers.ImageInformationExtractor;
 import de.raptor2101.GalDroid.WebGallery.GalleryImageView;
 import de.raptor2101.GalDroid.WebGallery.Tasks.ImageLoaderTaskListener;
 
 public class ImageViewImageLoaderTaskListener implements ImageLoaderTaskListener {
 
 	private final ImageInformationExtractor mInformationExtractor;
 	private final Gallery mGallery;
 	
 	public ImageViewImageLoaderTaskListener(Gallery gallery, ImageInformationExtractor informationExtractor) {
 		mInformationExtractor = informationExtractor;
 		mGallery = gallery;
 	}
 	
 	public void onLoadingStarted(String uniqueId) {
 		// Nothing todo		
 	}
 
 	public void onLoadingProgress(String uniqueId, int currentValue,
 			int maxValue) {
 		// Nothing todo
 		
 	}
 
 	public void onLoadingCompleted(String uniqueId, Bitmap bitmap) {
 		// if a Download is completed it could be the current diplayed image.
 		// so start decoding of its embeded informations
 		
 		GalleryImageView imageView = (GalleryImageView) mGallery.getSelectedView();
		if(imageView != null && imageView.getGalleryObject().getImage().getUniqueId() == uniqueId) {
 			mInformationExtractor.extractImageInformations(imageView);
 		}
 	}
 
 	public void onLoadingCancelled(String uniqueId) {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
