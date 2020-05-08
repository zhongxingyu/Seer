 package helpers;
 
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 
 public class Location {
 	
 	private Bitmap icon;
 	private float x,y;
 	private int index;
 	
 	public Location(Bitmap icon, int index)
 	{
 		this.icon = icon;
 		this.index = index;
 	}
 	
 	//sets the location based on index, index maps the icon to its text
 	public void setLocation(int width, int height)
 	{
 		float tempx,tempy;
 		if(index == 1)
 		{
			tempx = 650;
 			tempy = (height/2) - icon.getHeight() - 30;
 		}
 		else if(index == 2)
 		{
			tempx = 250+(width/2) - (icon.getWidth()/2);
 			tempy = 45 + icon.getHeight()/2;
 		}
 		else if(index ==3)
 		{
			tempx = 250+(float) ((width/2) - (icon.getWidth()*.5));
 			tempy = (float) (height - icon.getHeight()*(2));
 		}
 		else
 		{
 			tempx = (float) (width - (icon.getWidth()*2));
 			tempy = (height/2) - icon.getHeight() - 20;
 		}
 		x = tempx;
 		y=tempy;
 	}
 	
 	public void draw(Canvas canvas)
 	{
 		canvas.drawBitmap(icon, x, y,null);
 	}
 	
 	public float getX()
 	{
 		return x;
 	}
 	
 	public float getY()
 	{
 		return y;
 	}
 
 }
