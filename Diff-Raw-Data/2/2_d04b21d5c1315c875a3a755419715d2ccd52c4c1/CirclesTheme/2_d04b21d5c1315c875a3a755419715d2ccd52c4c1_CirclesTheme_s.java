 package studie.callbydoodle.themes;
 
 import studie.callbydoodle.data.Doodle;
 import studie.callbydoodle.data.DoodleSegment;
 import studie.callbydoodle.data.Vec;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Path;
 import android.graphics.PorterDuffXfermode;
 import android.graphics.Rect;
 import android.graphics.PorterDuff.Mode;
 
 public class CirclesTheme implements DoodleTheme
 {
 	private static final int CIRCLE_RADIUS = 15;
 	private static final int CIRCLE_SPACING = 2;
 	private static final int BACKGROUND_COLOR = Color.WHITE;
 	private static final int CIRCLE_COLOR = Color.BLACK;
 	
 	// MotionEvent.getPressure() times this constant to get the drawing radius
	private final int PRESSURE_TO_RADIUS = 80;
 	
 	private static Bitmap circlesBitmap;
 	private static Paint overlayPaint;
 	
 	public CirclesTheme()
 	{
 		circlesBitmap = Bitmap.createBitmap(500, 500, Bitmap.Config.RGB_565);
 		Canvas circlesCanvas = new Canvas(circlesBitmap);
 		circlesCanvas.drawColor(BACKGROUND_COLOR);
 		
 		Paint circlePaint = new Paint();
 		circlePaint.setColor(CIRCLE_COLOR);
 		circlePaint.setAntiAlias(true);
 		int d = CIRCLE_RADIUS * 2 + CIRCLE_SPACING;
 		for (int y = 0; y < circlesCanvas.getHeight() + d; y += d)
 		{
 			for (int x = 0; x < circlesCanvas.getWidth() + d; x += d)
 			{
 				circlesCanvas.drawCircle(x, y, CIRCLE_RADIUS, circlePaint);
 			}
 		}
 		
 		overlayPaint = new Paint();
 		overlayPaint.setAntiAlias(true);
 		overlayPaint.setXfermode(new PorterDuffXfermode(Mode.SRC_OUT));
 		overlayPaint.setColor(Color.TRANSPARENT);
 		//overlayPaint.setMaskFilter(new BlurMaskFilter(2, Blur.NORMAL));
 	}
 	
 	@Override
 	public void drawDoodle(Canvas canvas, Doodle doodle)
 	{
 		Rect r = new Rect(0, 0, canvas.getWidth(), canvas.getHeight());
 		canvas.drawBitmap(circlesBitmap, r, r, null);
 		
 		Bitmap overlay = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ALPHA_8);
 		Canvas overlayCanvas = new Canvas(overlay);
 		overlayCanvas.drawColor(Color.WHITE);
 		
 		for (DoodleSegment segment : doodle.getSegments())
 		{
 			Path p = new Path();
 			
 			Vec move = segment.getVecEnd().subtract(segment.getVecStart());
 			Vec radiusVecEnd = move.setLength(PRESSURE_TO_RADIUS * segment.getPressureEnd());
 			Vec radiusVecStart = move.setLength(PRESSURE_TO_RADIUS * segment.getPressureStart());
 			Vec vecEndLeft = segment.getVecEnd().add(radiusVecEnd.rotateLeft());
 			Vec vecEndRight = segment.getVecEnd().add(radiusVecEnd.rotateRight());
 			Vec vecStartLeft = segment.getVecStart().add(radiusVecStart.rotateLeft());
 			Vec vecStartRight = segment.getVecStart().add(radiusVecStart.rotateRight());
 			
 			p.moveTo(vecEndLeft.getX(), vecEndLeft.getY());
 			p.lineTo(vecEndRight.getX(), vecEndRight.getY());
 			p.lineTo(vecStartRight.getX(), vecStartRight.getY());
 			p.lineTo(vecStartLeft.getX(), vecStartLeft.getY());
 			p.lineTo(vecEndLeft.getX(), vecEndLeft.getY());
 			
 			overlayCanvas.drawPath(p, overlayPaint);
 		}
 		
 		canvas.drawBitmap(overlay, 0, 0, null);
 	}
 
 	@Override
 	public int getToolbarBackgroundColor() {
 		return Color.BLACK;
 	}
 	
 	@Override
 	public int getToolbarTextColor() {
 		return Color.WHITE;
 	}
 }
