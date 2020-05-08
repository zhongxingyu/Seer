 package com.project202.views;
 
 import static com.project202.DimenHelper.pixelSize;
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.Paint.Style;
 import android.graphics.Path;
 import android.view.View;
 import android.view.animation.DecelerateInterpolator;
 import android.view.animation.Interpolator;
 
 import com.project202.model.Rating;
 import com.project202.model.Theme;
 
 public class HistoryLine extends View implements OnHistoryFocusedListener {
 
 	private static final int DURATION = 500;
 
 	private static final Interpolator bounceInterpolator = new DecelerateInterpolator();
 
 	private Rating rating;
 
 	private float maxRating;
 
 	private boolean pair;
 
 	private static final Paint paint = new Paint();
 
 	private static final Path path = new Path();
 
 	private long animationStart;
 	
 	private boolean hidden = false;
 
 	private final float VERTICAL_SPACE;
 	
 	private final float HORIZONTAL_SPACE;
 
 	private final float THICKNESS;
 	
 	public HistoryLine(Context context) {
 		super(context);
 		setMinimumHeight((int)pixelSize(context, 75));
 		setWillNotDraw(false);
 		paint.setFakeBoldText(true);
 		paint.setTextSize(pixelSize(context, 15));
 		paint.setStyle(Style.FILL);
 		paint.setAntiAlias(true);
 		VERTICAL_SPACE = pixelSize(context, 12f);
 		THICKNESS = pixelSize(context, 15f);
 		HORIZONTAL_SPACE = pixelSize(context, 15f);
 		animationStart = System.currentTimeMillis();
 	}
 
 	@Override
 	public void draw(Canvas canvas) {
 		canvas.drawColor(pair ? 0x11AAAAAA : 0x33FFFFFF);
 		if (hidden) {
 			return;
 		}
 		
 		if (rating == null) {
 			return;
 		}
 
 		// Animation
 		long now = System.currentTimeMillis();
 		long deltaTime = now - animationStart;
 		float interpolation = 0;
 		if (deltaTime < DURATION) {
 			float percent = deltaTime / (float) DURATION;
 			interpolation = bounceInterpolator.getInterpolation(percent);
 			postInvalidateDelayed(17);
 		}
 
 		// Prepare Drawing
 		float left = HORIZONTAL_SPACE;
 		float top = VERTICAL_SPACE;
 		float width = getWidth()-HORIZONTAL_SPACE*2-THICKNESS;
 		float height = getHeight()-VERTICAL_SPACE*2-THICKNESS;
 		if (deltaTime < DURATION){
 			width = width*interpolation;
 		}
 		
 		// Drawing
 		float offset = 0f;
 		for (Theme theme : rating.getThemes()) {
 			float themeWidth = 0f;
 			if (theme.getNote()==0){
 				themeWidth = 1f;
 			} else {
 				themeWidth = theme.getNote() / maxRating * width;
 			}
 			
 			path.reset();
 			float leftBase = left + offset;
 			path.moveTo(leftBase, 				top + THICKNESS);
 			path.lineTo(leftBase + THICKNESS, 	top);
 			path.lineTo(leftBase + THICKNESS + themeWidth, top);
 			path.lineTo(leftBase + THICKNESS + themeWidth, top + height);
 			path.lineTo(leftBase + themeWidth, 	top + height + THICKNESS);
 			path.lineTo(leftBase, 				top + height + THICKNESS);
 			path.close();
 			paint.setColor(theme.getColor());
 			canvas.drawPath(path, paint);
 			offset += themeWidth;
 		}
 
 		path.reset();
 		path.moveTo(left, 				top + THICKNESS);
 		path.lineTo(left + THICKNESS, 	top);
 		path.lineTo(left + offset + THICKNESS, top);
 		path.lineTo(left + offset, 		top + THICKNESS);
 		path.lineTo(left, 				top + THICKNESS);
 		path.close();
 		paint.setColor(0x33000000);
 		canvas.drawPath(path, paint);
 
 		path.reset();
 		path.moveTo(left + offset, 				top +THICKNESS);
 		path.lineTo(left + offset + THICKNESS, 	top);
 		path.lineTo(left + offset + THICKNESS, 	top + height );
 		path.lineTo(left + offset,				top + height + THICKNESS);
 		path.close();
 		paint.setColor(0x11000000);
 		canvas.drawPath(path, paint);
 
 		if (rating.address != null){
			paint.setColor(0x88000000);
			canvas.drawText(rating.address, left + HORIZONTAL_SPACE + 1, top + THICKNESS +  height/2 + 5, paint);
			paint.setColor(0xFFFFFFFF);
 			canvas.drawText(rating.address, left + HORIZONTAL_SPACE, top + THICKNESS +  height/2 + 4, paint);
 		}
 	}
 
 	public void setData(Rating rating, float maxRating) {
 		this.rating = rating;
 		this.maxRating = maxRating;
 		invalidate();
 	}
 
 	public void playAnimation() {
 		onHistoryFocused();
 	}
 	
 	@Override
 	public void onHistoryFocused() {
 		hidden = false;
 		animationStart = System.currentTimeMillis();
 		invalidate();
 	}
 
 	public void setPair(boolean pair) {
 		this.pair = pair;
 	}
 
 	@Override
 	public void onHistoryHidden() {
 		hidden = true;
 	}
 	
 	public Rating getRating() {
 		return rating;
 	}
 
 }
