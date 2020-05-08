 package de.hsbremen.android.dribl.views;
 
 import android.content.Context;
 import android.graphics.drawable.Drawable;
 import android.util.AttributeSet;
 import android.widget.ImageView;
 
 public class StretchableImageView extends ImageView {
 
     public StretchableImageView(Context context, AttributeSet attrs) {
         super(context, attrs);
     }
 
     @Override 
     protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
    	Drawable drawable = getDrawable(); 
		if (drawable != null) {
			int width = MeasureSpec.getSize(widthMeasureSpec);
			
			// Ceil not round (to avoid thin borders along the edges)
			float aspectRatio = (float) drawable.getIntrinsicWidth() / (float) drawable.getIntrinsicHeight();
	        // This code only works for an aspect ratio greater than 1 (i.e. height < width)
	        int height = (int) Math.ceil(width / aspectRatio);
	        setMeasuredDimension(width, height);
		} else {
			// If there's no drawable we don't know what the aspect ratio will be
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
     }
 
 }
