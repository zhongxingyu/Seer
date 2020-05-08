 package com.abstracttech.ichiban.views;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Matrix;
 import android.graphics.Paint;
 import android.util.AttributeSet;
 import android.widget.ImageView;
 
 import com.abstracttech.ichiban.R;
 import com.abstracttech.ichiban.data.Data;
 
 public class Gbar extends ImageView {
 
 	private Bitmap gSquare;
 	
 	public Gbar(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		
 		this.setImageResource(R.drawable.gbar_ozadje);
 		gSquare = BitmapFactory.decodeResource(getResources(), R.drawable.gbar_kvadratek);
 		
 		Data.subscribe(this);
 	}
 
 	@Override
 	protected void onDraw(Canvas canvas)
 	{
 		super.onDraw(canvas);
 		
 		if(isInEditMode()) //dummy data for editor
 		{}
 		else
 		{
 			//5 bar + and 5 -
			double data =  Data.getYPercentage();
			Paint p = new Paint();
			
 			for (int i = 0; i < Math.abs(data * 6); i++)
 			{
 				if (i >= Math.abs(data * 6) - 1)
					p.setAlpha((int)(Math.abs(data) * 255));
 				if(data > 0)
 					canvas.drawBitmap(gSquare, this.getWidth() / 2 - gSquare.getWidth() / 2, (gSquare.getHeight() + 1) * 5  -  i * (gSquare.getHeight() + 1) + 10, p);
 				else
 					canvas.drawBitmap(gSquare, this.getWidth() / 2 - gSquare.getWidth() / 2, this.getHeight() - (gSquare.getHeight() + 1) * 6  +  i * (gSquare.getHeight() + 1) - 8, p);
 			}
		}		
 	}
 }
