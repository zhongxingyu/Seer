 package com.ajgames.endless_runner.view;
 
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Matrix;
 import android.graphics.Paint;
 
 import com.ajgames.endless_runner.model.Sprite;
 
 public class BitmapRenderer extends SpriteRenderer
 {
 
 	private Bitmap bitmap;

 	public BitmapRenderer( Sprite sprite, Bitmap bitmap )
 	{
 		super( sprite );
 		this.bitmap = bitmap;
 	}

 	public void render( Canvas canvas )
 	{
 		Paint paint = new Paint();
		Matrix matrix = new Matrix();
		matrix.postTranslate( this.sprite.getX(), this.sprite.getY() );
		canvas.drawBitmap( bitmap, matrix, paint );
 	}
 
 }
