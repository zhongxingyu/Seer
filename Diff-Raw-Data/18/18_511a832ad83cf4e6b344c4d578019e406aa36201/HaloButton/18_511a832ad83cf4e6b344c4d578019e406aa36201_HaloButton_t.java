 package com.intalker.borrow.ui.control;
 
 import com.intalker.borrow.R;
 
 import android.content.Context;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.ImageView.ScaleType;
 import android.widget.RelativeLayout;
 
 public class HaloButton extends RelativeLayout {
 
 	private ImageView mButtonImageView = null;
 	private ImageView mHaloView = null;
 	
 	public HaloButton(Context context, int imageResId) {
 		super(context);
 		
 		this.setBackgroundDrawable(null);
 		
 		mButtonImageView = new ImageView(this.getContext());
 		mButtonImageView.setImageResource(imageResId);
		
		//Hard code now
		mButtonImageView.setId(100000);
 		this.addView(mButtonImageView);
 		
		addHalo();
 	}
 	
 	private void setHalo(boolean b)
 	{
 		if(b)
 		{
 			mHaloView.setVisibility(VISIBLE);
 		}
 		else
 		{
 			mHaloView.setVisibility(GONE);
 		}
 	}
 	
 	private void addHalo()
 	{
 		mHaloView = new ImageView(this.getContext());
		mHaloView.setBackgroundResource(R.drawable.halo);
 		mHaloView.setScaleType(ScaleType.FIT_XY);
 		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		lp.addRule(RelativeLayout.ALIGN_LEFT, mButtonImageView.getId());
		lp.addRule(RelativeLayout.ALIGN_RIGHT, mButtonImageView.getId());
		lp.addRule(RelativeLayout.ALIGN_TOP, mButtonImageView.getId());
		lp.addRule(RelativeLayout.ALIGN_BOTTOM, mButtonImageView.getId());
 		this.addView(mHaloView, lp);
 		mHaloView.setVisibility(GONE);
 		this.setOnTouchListener(new OnTouchListener(){
 
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				HaloButton btn = (HaloButton)v;
 				switch (event.getAction()) {
 				case MotionEvent.ACTION_DOWN:
 				case MotionEvent.ACTION_MOVE:
 					btn.setHalo(true);
 					break;
 				default:
 					btn.setHalo(false);
 					break;
 				}
 				return false;
 			}
 			
 		});
 	}
 
 }
