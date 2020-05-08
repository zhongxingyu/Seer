 package com.mobiric.stackflairwidget.preference;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.preference.Preference;
 import android.util.AttributeSet;
 import android.view.View;
 import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.LayoutParams;
 import android.widget.ImageView;
 import android.widget.ImageView.ScaleType;
 
 import com.mobiric.stackflairwidget.R;
 
 /**
  * Custom {@link Preference} that displays an {@link ImageView}. </p> This class is a bit of a HACK
  * in the way it handles its layout, and could be improved.
  */
 // TODO make custom Preference properly
 public class ImageViewPreference extends Preference
 {
 	private ImageView imageView;
 	private int height;
 	private int width;
 
 	private Bitmap image;
 
 	private Context context;
 	private AttributeSet attrs;
 
 	public ImageViewPreference(Context context, AttributeSet attrs)
 	{
 		super(context, attrs);
 		this.context = context;
 		this.attrs = attrs;
 	}
 
 	/**
 	 * Returns the view to show. </p> This is a HACK that returns a basic {@link ImageView}
 	 * independent of the layout files used elsewhere. This is the only way I could get a custom
 	 * {@link Preference} layout to work.
 	 */
 	@Override
 	public View getView(View convertView, ViewGroup parent)
 	{
 		if (imageView == null)
 		{
 			imageView = new ImageView(context, attrs);
 			imageView.setImageResource(R.drawable.flair_383414);

			// LayoutParams must match the parent view
			AbsListView.LayoutParams layoutParams =
					new AbsListView.LayoutParams(LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT);

 			imageView.setLayoutParams(layoutParams);
 			imageView.setScaleType(ScaleType.FIT_CENTER);
 
 			if (image != null)
 			{
 				imageView.post(new Runnable()
 				{
 					public void run()
 					{
 						setImageToView(image);
 					}
 				});
 			}
 		}
 
 		return imageView;
 	}
 
 	/**
 	 * Sets a new image to display.
 	 */
 	public void setBitmap(Bitmap bitmap)
 	{
 		// null check
 		if (bitmap == null)
 		{
 			return;
 		}
 
 		/*
 		 * There is a race condition here between setting the bitmap, and the image view being
 		 * created. Save a copy of the bitmap here, and set it when the image view is created.
 		 */
 		image = bitmap;
 		if (imageView != null)
 		{
 			setImageToView(image);
 		}
 	}
 
 	/**
 	 * Helper method to set the image to the view. Handles resizing of bitmap if required.
 	 * 
 	 * @param newBitmap
 	 *            new image to set
 	 */
 	private void setImageToView(final Bitmap newBitmap)
 	{
 		// lazy initialise dimensions
 		if ((height == 0) || (width == 0))
 		{
 			height = imageView.getHeight();
 			width = imageView.getWidth();
 		}
 
 		// scale bitmap correctly
 		if ((newBitmap.getHeight() != height) || (newBitmap.getWidth() != width))
 		{
 			// resize & set bitmap
 			final Bitmap bitmapResized = Bitmap.createScaledBitmap(newBitmap, width, height, true);
 			imageView.post(new Runnable()
 			{
 				public void run()
 				{
 					imageView.setImageBitmap(bitmapResized);
 					imageView.invalidate();
 				}
 			});
 		}
 		else
 		{
 			// set unsized bitmap
 			imageView.post(new Runnable()
 			{
 				public void run()
 				{
 					imageView.setImageBitmap(newBitmap);
 					imageView.invalidate();
 				}
 			});
 		}
 	}
 }
