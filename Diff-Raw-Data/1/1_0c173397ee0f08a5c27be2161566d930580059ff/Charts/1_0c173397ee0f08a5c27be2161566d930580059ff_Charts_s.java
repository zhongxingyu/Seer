 package yoga1290.schoolmate;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.RectF;
 import android.graphics.drawable.BitmapDrawable;
 
 public class Charts 
 {
 	public static int gcd(int a,int b)
 	{
 		if(b==0)
 			return a;
 		return gcd(b,a%b);
 	}
 	public static Bitmap getTimepiece(int width,int height,int ar[])
 	{
 		Bitmap bitmap=Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
 		Canvas canvas=new Canvas(bitmap);
 		int i,l=Math.min(ar.length, 12),angle=0,max=0,gcd=ar[0],lpad,tpad;
 		for(i=0;i<ar.length;i++)
 			gcd=gcd(ar[i],gcd);
 		for(i=0;i<ar.length;i++)
 			ar[i]=ar[i]/gcd;
 		System.out.println("GCD="+gcd);
 		for(i=0;i<ar.length;i++)
 			max=Math.max(max,ar[i]);
 		
 		Paint paint=new Paint();
 		int colors[]=new int[]{Color.BLUE,Color.CYAN,Color.GRAY,Color.GREEN};
 		for(i=0;i<l;i++)
 		{
 			lpad=ar[i]*Math.min(width, height)/max;
 			tpad=Math.min(width, height)-lpad;
 			System.out.println("LPAD="+lpad+",TPAD="+tpad);
 			paint.setColor(colors[i%colors.length]);
 			canvas.drawArc(new RectF(lpad,lpad, tpad, tpad), angle, 30, true, paint);
 			angle+=30;
 		}
 		return bitmap;
 	}
 }
