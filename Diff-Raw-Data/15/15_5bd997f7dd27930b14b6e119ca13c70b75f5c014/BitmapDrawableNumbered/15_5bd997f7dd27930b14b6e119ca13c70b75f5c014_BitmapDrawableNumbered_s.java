 package il.ac.tau.team3.shareaprayer;
 
 import il.ac.tau.team3.common.GeneralPlace;
 import il.ac.tau.team3.common.Pray;
 import il.ac.tau.team3.uiutils.MenuUtils;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.ColorFilter;
 import android.graphics.LightingColorFilter;
 import android.graphics.Paint;
 import android.graphics.PorterDuff.Mode;
 import android.graphics.Typeface;
 import android.graphics.drawable.BitmapDrawable;
 
 
 public class BitmapDrawableNumbered extends BitmapDrawable {
 	private GeneralPlace place;
 	Paint pBmp = new Paint();
 	Paint p = new Paint();
 	
 	//private static int lastDisplayed = 0;
 	
 	public BitmapDrawableNumbered(Bitmap image, GeneralPlace a_place)	{
 		super(image);
 		place = a_place;
 		this.setBounds(0, 0, image.getWidth(), image.getHeight());
 	}
 	
 	public int getNumber()	{
		MenuUtils.chooseMaxOrMin(place);
 	}
 	
 	private ColorFilter determineColor(double num)	{
 		if (num > 10)	{
 			return new LightingColorFilter(Color.argb(0, 0x01, 0xFF, 0x01), 0);
 		}
 		
 		final int colorSat = 0xFF;
 		
 		int redAdd = (int) Math.min(Math.max(Math.exp(-num/4)*colorSat,1),colorSat);
 		int BlueAdd = (int) Math.min(Math.max((1.0-Math.exp(-(num-3)/2))*Math.exp(-(num-7)/2)/Math.exp(2)*4*colorSat,1),colorSat);
 		int GreenAdd = (int) Math.min(Math.max((1.0-Math.exp(-(num-5)))*colorSat,1),colorSat);
 		
 		return new LightingColorFilter(Color.argb(0, redAdd, GreenAdd, BlueAdd), 0);
 	}
 	
     @Override
     public void draw(Canvas arg)    {
     	
     		int  numToDisplay= getNumber();
     		pBmp.setColorFilter(determineColor(numToDisplay));
             
             arg.drawBitmap(getBitmap(), this.getBounds().left, this.getBounds().top, pBmp);
             
             p.setColor(Color.WHITE);
                     p.setStyle(Paint.Style.STROKE);
                     p.setStrokeWidth(2);
                     //p.setARGB(255, 255, 0, 0);
                     p.setTypeface(Typeface.DEFAULT);
                     p.setTextSize(16);
             
             p.setAntiAlias(true);
             arg.drawText((new Integer(numToDisplay)).toString() , 
                             getBounds().left + getBitmap().getWidth()/2 - 5, 
                             getBounds().top + getBitmap().getHeight()/2 + 5 , p);
     }
 }
