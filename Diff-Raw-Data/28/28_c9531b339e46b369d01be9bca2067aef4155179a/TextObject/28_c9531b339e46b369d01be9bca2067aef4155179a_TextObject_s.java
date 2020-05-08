 package steve4448.livetextbackground.background.object;
 
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.RectF;
 
 public class TextObject {
 	public String text;
 	public RectF dimen;
 	public float velocityX;
 	public float velocityY;
 	public int size;
 	public int color;
 	public Bitmap cachedText;
 	
 	public TextObject(Paint paint, String text, RectF dimen, int size, int color) {
 		this.text = text;
 		this.dimen = dimen;
 		this.velocityX = (float)((Math.random() * 3) - (Math.random() * 3));
 		this.velocityY = (float)(Math.random() * 6);
 		this.size = size;
 		this.color = color;
 		if((int)dimen.width() <= 0 || (int)dimen.height() <= 0)
 			return;
		cachedText = Bitmap.createBitmap((int)dimen.width(), (int)dimen.height(), Bitmap.Config.ARGB_8888);
 		Canvas c2 = new Canvas(cachedText);
		c2.drawText(text, 0, (int)dimen.height(), paint);
 	}
 }
