 package com.abstracttech.ichiban.views;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.util.AttributeSet;
 import android.widget.ImageView;
 
 import com.abstracttech.ichiban.R;
 import com.abstracttech.ichiban.activities.IchibanActivity;
 import com.abstracttech.ichiban.data.Data;
 /**
  * main power button, changes color dependng on state of aplication
  * green=running
  * red=stopped
  */
 public class PowerButton extends ImageView {
 
 	private int bg_r=R.drawable.power_red;
 	private int bg_g=R.drawable.power_green;
 	private boolean preRunning = false;
 
 	public PowerButton(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		this.setImageResource(preRunning?bg_g:bg_r);
 		IchibanActivity.subscribe(this);
 
		//Data.subscribe(this); //this doesn't need to be subscribed to data updates
 	}
 
 	@Override
 	protected void onDraw(Canvas canvas)
 	{
 		if(preRunning != IchibanActivity.isRunning())
 			this.setImageResource(!(preRunning = IchibanActivity.isRunning())?bg_r:bg_g);
 		super.onDraw(canvas);
 	}
 }
