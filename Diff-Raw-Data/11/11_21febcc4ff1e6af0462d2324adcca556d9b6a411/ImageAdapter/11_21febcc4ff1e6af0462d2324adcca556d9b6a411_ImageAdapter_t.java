 package ch.almana.android.sessionsmodels.view.adapter;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 
 import android.content.Context;
 import android.database.DataSetObserver;
 import android.graphics.BitmapFactory;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.ImageView;
 import android.widget.ListAdapter;
 import android.widget.SpinnerAdapter;
 import ch.almana.android.sessionsmodels.R;
 import ch.almana.android.sessionsmodels.helper.CacheBuilderAsyncTask;
 import ch.almana.android.sessionsmodels.helper.ImageHelper;
 import ch.almana.android.sessionsmodels.log.Logger;
 
 public class ImageAdapter implements SpinnerAdapter, ListAdapter {
 
 	private final File[] images;
 	int imageSize;
 	private LayoutParams layoutParams;
 
 	public ImageAdapter(Context ctx, File[] images, int imageSize) {
 		super();
 		this.images = images;
 		this.imageSize = imageSize;
 		CacheBuilderAsyncTask.cacheFiles(ctx, images, imageSize);
 	}
 
 	@Override
 	public void registerDataSetObserver(DataSetObserver observer) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void unregisterDataSetObserver(DataSetObserver observer) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public int getCount() {
		if (images == null) {
			return 0;
		}
 		return images.length;
 	}
 
 	@Override
 	public Object getItem(int position) {
 		return images[position];
 	}
 
 	@Override
 	public long getItemId(int position) {
 		return position;
 	}
 
 	@Override
 	public boolean hasStableIds() {
 		return false;
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		Context ctx = parent.getContext();
 		ImageView iv;
 		if (convertView instanceof ImageView) {
 			iv = (ImageView) convertView;
 		} else {
 			iv = new ImageView(ctx);
 		}
 		//		iv.setMaxHeight(ImageHelper.dpToPx(ctx, 250));
 		//		iv.setMaxWidth(ImageHelper.dpToPx(ctx, 250));
 		//iv.setScaleType(ImageView.ScaleType.CENTER);
 		//		iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
 		//iv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
 		//		iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
 		//iv.setScaleType(ImageView.ScaleType.FIT_XY);
 		//		iv.setScaleType(ImageView.ScaleType.FIT_END);
 		//		iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
 
 		// Set the Width & Height of the individual images
 		iv.setBackgroundColor(ctx.getResources().getColor(R.color.galleryListFrame));
 		int dp = ImageHelper.dpToPx(ctx, imageSize);
 		//		layoutParams = new LayoutParams(dp, dp);
 		if (layoutParams != null) {
 			iv.setLayoutParams(layoutParams);
 			iv.setImageBitmap(BitmapFactory.decodeFile(images[position].getAbsolutePath()));
 		} else {
 
 			try {
 				iv.setImageBitmap(ImageHelper.scaleImage(ctx, images[position], imageSize));
 			} catch (FileNotFoundException e) {
 				Logger.w("Image not found", e);
 			}
 		}
 		return iv;
 	}
 
 	@Override
 	public int getItemViewType(int position) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public int getViewTypeCount() {
 		return 1;
 	}
 
 	@Override
 	public boolean isEmpty() {
 		return false;
 	}
 
 	@Override
 	public View getDropDownView(int position, View convertView, ViewGroup parent) {
 		return null;
 	}
 
 	@Override
 	public boolean areAllItemsEnabled() {
 		// TODO Auto-generated method stub
 		return true;
 	}
 
 	@Override
 	public boolean isEnabled(int position) {
 		// TODO Auto-generated method stub
 		return true;
 	}
 
 }
