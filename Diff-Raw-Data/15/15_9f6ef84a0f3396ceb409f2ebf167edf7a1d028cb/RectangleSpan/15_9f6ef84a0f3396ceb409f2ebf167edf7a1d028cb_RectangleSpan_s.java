 package cat.mobilejazz.views;
 
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.Paint.FontMetricsInt;
 import android.graphics.Rect;
 import android.text.style.ReplacementSpan;
 
 public class RectangleSpan extends ReplacementSpan {
 
 	private int fillColor;
 	private int strokeColor;
 	private float strokeWidth;
 	private int textColor;
 	private float padding;
 
 	/**
 	 * 
 	 * @param padding
 	 *            the left and right padding in pixels
 	 */
 	public RectangleSpan(int textColor, int fillColor, int strokeColor, float strokeWidth, float padding) {
 		this.textColor = textColor;
 		this.fillColor = fillColor;
 		this.strokeColor = strokeColor;
 		this.strokeWidth = strokeWidth;
 		this.padding = padding;
 	}
 
 	@Override
 	public int getSize(Paint paint, CharSequence text, int start, int end, FontMetricsInt fm) {
 		return (int) (paint.measureText(text.subSequence(start, end).toString()) + 2 * padding);
 	}
 
 	@Override
 	public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom,
 			Paint paint) {
 
 		canvas.save();
 
 		// paint.setTextSize(paint.getTextSize() * 0.7f);
 
 		String str = text.subSequence(start, end).toString();
 
		Rect bounds = new Rect();
 		paint.getTextBounds(str, 0, str.length(), bounds);
 
		Rect tag = new Rect((int) x, top, (int) (x + bounds.right + 2 * padding), bottom);
 
 		paint.setColor(fillColor);
 		paint.setStyle(Paint.Style.FILL);
 		canvas.drawRect(tag, paint);
 
 		paint.setColor(strokeColor);
 		paint.setStrokeWidth(strokeWidth);
 		paint.setStyle(Paint.Style.STROKE);
 		canvas.drawRect(tag, paint);
 
 		paint.setColor(textColor);
 		paint.setStyle(Paint.Style.FILL);
 		canvas.drawText(text, start, end, x + padding, y, paint);
 
 		canvas.restore();
 
 	}
 }
