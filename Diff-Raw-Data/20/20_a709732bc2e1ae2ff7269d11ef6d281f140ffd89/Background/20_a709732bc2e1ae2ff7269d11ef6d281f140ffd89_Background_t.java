 package com.petrifiednightmares.singularityChess.ui;
 
 import java.io.File;
 import java.io.OutputStream;
 
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Path;
 import android.graphics.RectF;
 
 import com.petrifiednightmares.singularityChess.GameDrawingPanel;
 import com.petrifiednightmares.singularityChess.R;
 import com.petrifiednightmares.singularityChess.io.GameIO;
 import com.petrifiednightmares.singularityChess.utilities.SingularBitmapFactory;
 
 public class Background extends GameDrawable
 {
 	private Bitmap _background;
 	private Canvas _backgroundCanvas;
 
 	public Background(GameDrawingPanel gdp)
 	{
 		super(gdp);
 		setupBackgroundAndBorder();
 	}
 
 	private void setupBackgroundAndBorder()
 	{
 		File file = new File(GameIO.getCacheBgFileName());
 		if (file.exists())
 		{
 			_background = SingularBitmapFactory.buildScaledBitmap(gdp.getResources(),
 					GameIO.getCacheBgFileName(), SUI.WIDTH, SUI.HEIGHT);
 			SUI.CACHED_BACKGROUND = true;
		}
		else if (!file.exists() || _background == null)
 		{
 			SUI.CACHED_BACKGROUND = false;
 			_background = SingularBitmapFactory.buildScaledBitmap(gdp.getResources(),
 					R.drawable.background, SUI.WIDTH, SUI.HEIGHT);
 			_backgroundCanvas = new Canvas(_background);
 
 			setupBorderShadow();
 
 			_backgroundCanvas.save();
 			_backgroundCanvas.clipRect(SUI.PADDING, 0, SUI.WIDTH - SUI.PADDING, SUI.HEIGHT);
 			_backgroundCanvas.drawCircle(SUI.WIDTH / 2, SUI.HEIGHT_CENTER, 6
 					* SUI.CIRCLE_RADIUS_DIFFERENCE + SUI.BORDER_WIDTH, SUI.borderPaint);
 			_backgroundCanvas.restore();
 		}
 	}
 
 	private void setupBorderShadow()
 	{
 		// Set up variables
 
 		// x component of the center of the circle
 		int h = SUI.WIDTH / 2;
 		// y component of the center of the circle
 		int k = SUI.HEIGHT_CENTER;
 
 		// left side of the rectangle
 		int x = SUI.WIDTH / 2 - 4 * SUI.CIRCLE_RADIUS_DIFFERENCE - SUI.BORDER_WIDTH;
 		// radius of circle
 		int r = 6 * SUI.CIRCLE_RADIUS_DIFFERENCE + SUI.BORDER_WIDTH;
 
 		// define a rectangle that circumscribes the circle
 		RectF circle = new RectF(h - r, k - r, h + r, k + r);
 
 		Path p = new Path();
 		// draw a line that goes from the bottom left to the top left of the
 		// shape
 		p.moveTo(x, (float) (k + Math.sqrt(-(h * h) + 2 * h * x + r * r - (x * x))));
 		p.lineTo(x, (float) (k - Math.sqrt(-(h * h) + 2 * h * x + r * r - (x * x))));
 
 		// calculate the angle that the top left of the shape represents in the
 		// circle
 		float angle = (float) Math.toDegrees(Math.atan(Math.sqrt(-(h * h) + 2 * h * x + r * r
 				- (x * x))
 				/ (h - x)));
 
 		// draw an arc from the top left of shape to top right of shape
 		p.arcTo(circle, 180 + angle, (180 - angle * 2));
 
 		// the x component of the right side of the shape
 		x = SUI.WIDTH / 2 + 4 * SUI.CIRCLE_RADIUS_DIFFERENCE + SUI.BORDER_WIDTH;
 
 		// draw line from top right to bottom right
 		p.lineTo(x, (float) (k + Math.sqrt(-(h * h) + 2 * h * x + r * r - (x * x))));
 
 		// draw arc back from bottom right to bottom left.
 		p.arcTo(circle, angle, (180 - angle * 2));
 
 		// draw the path onto the canvas
 		_backgroundCanvas.drawPath(p, SUI.borderShadowPaint);
 	}
 
 	public Canvas getBackgroundCanvas()
 	{
 		return _backgroundCanvas;
 	}
 
 	@Override
 	public void onDraw(Canvas c)
 	{
 		c.drawBitmap(_background, 0, 0, null);
 	}
 
 	@Override
 	public boolean onClick(int x, int y)
 	{
 		return false;
 	}
 
 	public void cacheBitmap()
 	{
 		Thread cacheThread = new Thread(new Runnable()
 		{
 
 			public void run()
 			{
 				long time = System.currentTimeMillis();
 				try
 				{
 					GameIO.intentionCacheBg();
 					OutputStream out = GameIO.getOutputStream(GameIO.StorageOption.IMAGE_CACHE);
 					_background.compress(Bitmap.CompressFormat.PNG, 100, out);
 					out.close();
				}
				catch (Exception e)
 				{
 					e.printStackTrace();
 				}
 				System.out.println("cacheBitmap took: " + (System.currentTimeMillis() - time));
 
 			}
 
 		});
 		cacheThread.start();
 
 	}
 
 }
