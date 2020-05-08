 package com.chess.genesis;
 
 import android.content.Context;
 import android.widget.ImageView;
 
 public class MyImageView extends ImageView
 {
 	public MyImageView(Context context)
 	{
 		super(context);
 
 		// To fix brain-dead non-auto-updating view bounds on images!!!
 		setAdjustViewBounds(true);
 	}
 }
