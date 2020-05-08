 package com.levelup.picturecache.samples;
 
 import java.security.NoSuchAlgorithmException;
 
 import android.content.Context;
import android.graphics.Bitmap;
 import android.util.Log;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 
 import com.levelup.picturecache.PictureJob;
 import com.levelup.picturecache.loaders.ImageViewLoaderDefaultResource;
 
 /**
  * adapter with a different layout and a loader that displays an infinite Progress instead of the placeholder
  */
 class AlternateAvatarsAdapter extends ArrayAdapter<SampleSource.Sample> {
 	protected final MyPictureCache mCache;
 
 	AlternateAvatarsAdapter(Context context) {
 		super(context, R.layout.list_item_avatar_alt, android.R.id.text1, SampleSource.getSamples());
 
 		mCache = MyPictureCache.getInstance(context);
 	}
 
 	private static class LoaderWithProgress extends ImageViewLoaderDefaultResource {
 
 		private final View progress;
 		
 		public LoaderWithProgress(ImageView view, View progress) {
 			super(view, R.drawable.picholder, null, null);
 			this.progress = progress;
 		}
 		
 		@Override
		protected void displayCustomBitmap(Bitmap bmp) {
 			super.displayCustomBitmap(bmp);
 			progress.setVisibility(View.INVISIBLE);
 		}
 		
 		@Override
		protected void displayDefaultView() {
 			//no need to display the default view super.displayDefaultView();
 			getImageView().setImageDrawable(null);
 			progress.setVisibility(View.VISIBLE);
 		}
 		
 	}
 	
 	@Override
 	public final View getView(int position, View convertView, ViewGroup parent) {
 		View view = super.getView(position, convertView, parent);
 		
 		// set the avatar on the inflated view at the specified position in the list
 		try {
 			// basic loader into an image view with a default display as "picholder" while it's loading 
 			LoaderWithProgress loader = new LoaderWithProgress((ImageView) view.findViewById(R.id.avatar), view.findViewById(R.id.progressBar1));
 
 			// prepare a basic picture job to load the avatar URL with a specific UUID
 			PictureJob avatarJob = new PictureJob.Builder(loader)
 			.setURL(getItem(position).picURL)
 			.setUUID("avatar_" + getItem(position).name)
 			.build();
 
 			// run the job in the picture cache
 			avatarJob.startLoading(mCache);
 		} catch (NoSuchAlgorithmException e) {
 			Log.d("ListView","failed to use the avatar URL "+getItem(position).picURL);
 		}
 
 		return view;
 	}
 }
