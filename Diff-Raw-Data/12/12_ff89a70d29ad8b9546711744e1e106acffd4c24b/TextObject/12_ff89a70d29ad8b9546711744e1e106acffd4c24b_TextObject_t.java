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
 	
 	public TextObject(String text, RectF dimen, int size, int color) {
 		this.text = text;
 		this.dimen = dimen;
 		this.velocityX = (float)((Math.random() * 3) - (Math.random() * 3));
 		this.velocityY = (float)(Math.random() * 6);
 		this.size = size;
 		this.color = color;
 		if((int)dimen.width() <= 0 || (int)dimen.height() <= 0)
 			return;
 	}
 	
 	public void doCache(Paint p, Canvas backup) {
 		int bWidth = (int)dimen.width() + 2;
 		int bHeight = (int)dimen.height() * 2;
 		if(bWidth <= 0 || bHeight <= 0) {
 			System.out.println("TextObject (" + text + ", " + dimen.toString() + ", " + size + ", " + color + ") invalid. (" + bWidth + "x" + bHeight + ")");
 			return;
 		}
 		cachedText = Bitmap.createBitmap(bWidth, bHeight, Bitmap.Config.ARGB_8888);
 		Canvas c2 = new Canvas(cachedText);
		p.setColor(color);
 		p.setTextSize(size);
 		c2.drawText(text, 0, dimen.height(), p);
 		if(backup != null)
 			backup.drawText(text, dimen.left, dimen.top, p);
 	}
 }
