 package com.example.spaceshipgame.renderer;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.LightingColorFilter;
 import android.graphics.Matrix;
 import android.graphics.Paint;
 
 import com.example.spaceshipgame.R;
 import com.example.spaceshipgame.model.Element;
 import com.example.spaceshipgame.model.Spaceship;
 
 public class SpaceshipRenderer extends ElementRenderer {
 	Paint	paint	= new Paint();
 
 	@Override
 	public void render(Canvas canvas, Element element, Context context) {
 		super.render(canvas, element, context);
 		Spaceship spaceship = (Spaceship) element;
 		Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.spaceship);
 
 		Matrix matrix = new Matrix();
 		matrix.reset();
 		matrix.postTranslate(-bmp.getWidth() / 2, -bmp.getHeight() / 2);
		matrix.postRotate(360 - spaceship.getRotation());
 		matrix.postTranslate(spaceship.getPosition().getX(), spaceship.getPosition().getY());
 
 		canvas.drawBitmap(bmp, matrix, null);
 		renderBanner(canvas, element, context);
 	}
 
 	private void renderBanner(Canvas canvas, Element element, Context context) {
 		Spaceship spaceship = (Spaceship) element;
 		Bitmap banner = BitmapFactory.decodeResource(context.getResources(), R.drawable.banner);
 
 		Paint paint = new Paint();
 		paint.setDither(true);
 		paint.setAntiAlias(true);
 		paint.setFilterBitmap(true);
 		paint.setColorFilter(new LightingColorFilter(0x00505050, spaceship.colour().toAndroidColor()));
 
 		Matrix matrix = new Matrix();
 		matrix.reset();
 		matrix.postTranslate(-banner.getWidth() / 2, -banner.getHeight() / 2);
		matrix.postRotate(360 - spaceship.getRotation());
 		matrix.postTranslate(spaceship.getPosition().getX(), spaceship.getPosition().getY());
 		canvas.drawBitmap(banner, matrix, paint);
 	}
 }
