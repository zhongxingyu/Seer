 package edu.berkeley.cs160.smartnature;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Matrix;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.graphics.RectF;
 import android.graphics.drawable.Drawable;
 import android.util.AttributeSet;
 import android.view.MotionEvent;
 import android.view.View;
 
 public class GardenLayout extends View implements View.OnClickListener {
 	
 	GardenScreen context;
 	Garden garden;
 	Matrix dragMatrix = new Matrix(), bgDragMatrix = new Matrix();
 	Paint canvasPaint, textPaint;
 	Drawable bg;
 	int zoomLevel;
 	float prevX, prevY, zoomScale = 1;
 	float textSize;
 	
 	public GardenLayout(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		this.context = (GardenScreen) context;
		textSize = 15.5f * this.context.scaledDensity;
 		initPaint();
 		bg = getResources().getDrawable(R.drawable.tile);	
 		initPaint();
 		initMockData();	
 		setOnClickListener(this);
 	}
 	
 	public void initMockData() {
 		garden = this.context.mockGarden;
 		for (Plot r: garden.getPlots()) {
 			r.getShape().getPaint().setColor(Color.BLACK);
 			r.getShape().getPaint().setStyle(Paint.Style.STROKE);
 			r.getShape().getPaint().setStrokeWidth(3);
 		}		
 	}
 	
 	@Override
 	public void onAnimationEnd() {
 		zoomLevel = context.zoomLevel;
 		zoomScale = (float) Math.pow(1.5, zoomLevel);
 		textPaint.setTextSize(Math.max(10, textSize * zoomScale));
 		invalidate();
 	}
 	
 	public void initPaint() {
 		canvasPaint = new Paint();
 		canvasPaint.setStyle(Paint.Style.STROKE);
 		textPaint = new Paint(Paint.FAKE_BOLD_TEXT_FLAG|Paint.ANTI_ALIAS_FLAG);
 		textPaint.setTextSize(textSize);
 		textPaint.setTextScaleX(1.2f);
 		textPaint.setTextAlign(Paint.Align.CENTER);
 	}
 	
 	public void reset() {
 		zoomLevel = 0;
 		zoomScale = 1;
 		textPaint.setTextSize(textSize);
 		textPaint.setTextScaleX(1.2f);
 		dragMatrix.reset();
 		bgDragMatrix.reset();
 		invalidate();
 	}
 	
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		context.handleZoom();
 		float x = event.getX(), y = event.getY();
 		if (event.getAction() != MotionEvent.ACTION_DOWN) {
 			float dx = x - prevX, dy = y - prevY;
 			dragMatrix.postTranslate(dx / zoomScale, dy / zoomScale);
 			bgDragMatrix.postTranslate(dx, dy);
 		}
 		prevX = x;
 		prevY = y;
 		invalidate();
 		return true;
 	}
 	
 	@Override
 	public void onClick(View view) {
 	}
 	
 	@Override
 	protected void onDraw(Canvas canvas) {
 		super.onDraw(canvas);
 		int width = getWidth(), height = getHeight();
 		boolean portraitMode = width < height;
 		
 		canvas.save();
 		canvas.concat(bgDragMatrix);	
 		bg.setBounds(canvas.getClipBounds());
 		bg.draw(canvas); //canvas.drawRGB(255, 255, 255);
 		canvas.restore();
 		
 		Matrix m = new Matrix();
 		
 		if (portraitMode) {
 			RectF canvasBounds = new RectF(getLeft(), getTop(), getBottom(), getRight());
 			m.setRectToRect(new RectF(garden.getPortBounds()), canvasBounds, Matrix.ScaleToFit.CENTER);
 		} else {
 			RectF canvasBounds = new RectF(getLeft(), getTop(), getRight(), getBottom());
 			m.setRectToRect(new RectF(garden.getLandBounds()), canvasBounds, Matrix.ScaleToFit.CENTER);
 		}
 		
 		if (portraitMode) {
 			m.postRotate(90);
 			m.postTranslate(width, 0);
 		}
 		
 		m.postConcat(dragMatrix);
 		
 		if (zoomLevel != 0) {
 			float zoomShift = (1 - zoomScale) / 2;
 			m.postScale(zoomScale, zoomScale);
 			m.postTranslate(zoomShift * width, zoomShift * height);
 		}
 		
 		canvas.save();
 		canvas.concat(m);
 		for (Plot p: garden.getPlots())
 			p.getShape().draw(canvas);
 		canvas.restore();
 		
 		if (context.showLabels)
 			for (Plot p: garden.getPlots()) {
 				Rect bounds = p.getShape().getBounds();
 				float[] labelLoc = portraitMode ? new float[] {bounds.left - 10, bounds.centerY()} : new float[] {bounds.centerX(), bounds.top - 10};
 				m.mapPoints(labelLoc);
 				canvas.drawText(p.getName().toUpperCase(), labelLoc[0], labelLoc[1], textPaint);
 			}
 		}
 	}
