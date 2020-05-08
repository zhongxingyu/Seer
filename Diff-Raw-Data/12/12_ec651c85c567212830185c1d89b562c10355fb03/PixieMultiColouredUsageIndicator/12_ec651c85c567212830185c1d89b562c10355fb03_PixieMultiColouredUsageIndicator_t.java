 package com.magneticbear.pixie;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.util.AttributeSet;
 import android.view.View.MeasureSpec;
 
 public class PixieMultiColouredUsageIndicator extends PixieUsageIndicator {
 
 	/**
 	 *  The multicolored indicator is actually an indicator itself, with a shell for it's three other colors.
 	 *  Please take great care in making changes to this file, *Green tunics only*
 	 */
 	
 	/**
 	 *                     ~PIXIE CONTRACT~ 
 	 *                     ::RED    0->100
 	 *                     ::YELLOW 0->100
 	 *                     ::GREEN  0->100
 	 *                     ::BLUE   0->100
	 *                     
	 *                     where RED.frameCount == YELLOW.frameCount == GREEN.frameCount == BLUE.frameCount
 	 */
 	
 	public static final int COLOR_RED    = 0;
 	public static final int COLOR_YELLOW = 1;
 	public static final int COLOR_GREEN  = 2;
 	public static final int COLOR_BLUE   = 3;
 	
 	private PixieUsageIndicator layer_yellow;
 	private PixieUsageIndicator layer_green;
 	private PixieUsageIndicator layer_blue;
 	
 	private int layer_red_stride_scalar_start;
 	private int layer_red_stride_scalar_end; 		
 	private int layer_yellow_stride_scalar_start; 
 	private int layer_yellow_stride_scalar_end;   
 	private int layer_green_stride_scalar_start;  
 	private int layer_green_stride_scalar_end;    
 	private int layer_blue_stride_scalar_start;   
 	private int layer_blue_stride_scalar_end;	    
 
 	public PixieMultiColouredUsageIndicator(Context context) {
 		super(context);
 		initShadowlings(context);
 		multiColoredInit();
 	}
 	public PixieMultiColouredUsageIndicator(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		initShadowlings(context, attrs);
 		multiColoredInit();
 	}
 	public PixieMultiColouredUsageIndicator(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 		initShadowlings(context, attrs, defStyle);
 		multiColoredInit();
 	}
 	
 	private void initShadowlings(Context context) {
 		layer_yellow = new PixieUsageIndicator(context);
 		layer_green  = new PixieUsageIndicator(context);
 		layer_blue   = new PixieUsageIndicator(context);
 	}
 	private void initShadowlings(Context context, AttributeSet attrs) {
 		layer_yellow = new PixieUsageIndicator(context, attrs);
 		layer_green  = new PixieUsageIndicator(context, attrs);
 		layer_blue   = new PixieUsageIndicator(context, attrs);
 	}
 	private void initShadowlings(Context context, AttributeSet attrs, int defStyle) {
 		layer_yellow = new PixieUsageIndicator(context, attrs, defStyle);
 		layer_green  = new PixieUsageIndicator(context, attrs, defStyle);
 		layer_blue   = new PixieUsageIndicator(context, attrs, defStyle);
 	}
 	private void multiColoredInit() {
 		// Calculate stride scalars
 		layer_red_stride_scalar_start    =                                           0;
 		layer_red_stride_scalar_end      =  (header.info_FrameCountTotal / 4)      - 1;
 		layer_yellow_stride_scalar_start =  (header.info_FrameCountTotal / 4);
 		layer_yellow_stride_scalar_end   = ((header.info_FrameCountTotal / 4) * 2) - 1;
 		layer_green_stride_scalar_start  =  (header.info_FrameCountTotal / 4) * 2;
 		layer_green_stride_scalar_end    = ((header.info_FrameCountTotal / 4) * 3) - 1;
 		layer_blue_stride_scalar_start   = ((header.info_FrameCountTotal / 4) * 3);
 		layer_blue_stride_scalar_end     =   header.info_FrameCountTotal           - 1;
 		
 		// Set all 4 colored usage bars to initial frame
 		super.GoToFrameByIndex(layer_red_stride_scalar_start);
 		layer_yellow.GoToFrameByIndex(layer_yellow_stride_scalar_start);
 		layer_green.GoToFrameByIndex(layer_green_stride_scalar_start);
 		layer_blue.GoToFrameByIndex(layer_blue_stride_scalar_start);
 		
		// Start all alphas at 0
		super.paint.setAlpha(0);
		layer_yellow.paint.setAlpha(0);
		layer_green.paint.setAlpha(0);
		layer_blue.paint.setAlpha(0);
 		
		// Start usage at 0.0f blue
		SetUsagePercent(0.0f, COLOR_BLUE);
 	}
 
 	@Override
 	protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec) {
 		super.onMeasure(widthMeasureSpec, heightMeasureSpec);	
 		layer_yellow.onMeasure(widthMeasureSpec, heightMeasureSpec);
 		layer_green.onMeasure(widthMeasureSpec, heightMeasureSpec);
 		layer_blue.onMeasure(widthMeasureSpec, heightMeasureSpec);
 	}
 	@Override
     protected void onDraw(Canvas canvas) {
 		//draw order? active?
 		super.onDraw(canvas);
 		layer_yellow.onDraw(canvas);
 		layer_green.onDraw(canvas);
 		layer_blue.onDraw(canvas);
 		
 		/*
 		 DEMO MODE UNCOMMENT THIS TO HAVE RANDOM EASE TOS UPON COMPLETION
 		if(!isFading) {
 			int Min = COLOR_RED;
 			int Max = COLOR_BLUE;
 			SetUsagePercent((float)Math.random(), Min + (int)(Math.random() * ((Max - Min) + 1)));
 		}
 		*/
 		
 		invalidate();
 	}
 
 	public void SetUsagePercent(float Percect, int Color) {
 		/**
 		 *  Okay so some trickiness here. Because we can only pass just the one
 		 *  pixie sheet in as our constructor we can make some very cool assumptions.
 		 *  1. We know that our multi-col usage indicator uses 4 colors R(ed) Y(ellow) G(reen) B(lue) [RYGB]
 		 *  2. Each indicator will have an identical amount of frames.
 		 *  
 		 *  Because of these two things, we can simply pack all 4 colors into one sheet in vertex stride formation.
 		 *  Unpacking is then a process of knowing which quarter of the sheet belongs to which color.
 		 *  MAKE SURE YOU PACK IN ORDER RED YELLOW GREEN BLUE
 		 */
 		
 		// Set alpha based on passed in color
 		switch (Color) {
 			case COLOR_RED:
 				this.EaseToAlpha        (255, EASE_SPEED, EASE_STYLE);
 				layer_yellow.EaseToAlpha(0,   EASE_SPEED, EASE_STYLE);
 				layer_green.EaseToAlpha (0,   EASE_SPEED, EASE_STYLE);
 				layer_blue.EaseToAlpha  (0,   EASE_SPEED, EASE_STYLE);
 				break;
 			case COLOR_YELLOW:
 				this.EaseToAlpha        (0,   EASE_SPEED, EASE_STYLE);
 				layer_yellow.EaseToAlpha(255, EASE_SPEED, EASE_STYLE);
 				layer_green.EaseToAlpha (0,   EASE_SPEED, EASE_STYLE);
 				layer_blue.EaseToAlpha  (0,   EASE_SPEED, EASE_STYLE);
 				break;
 			case COLOR_GREEN:
 				this.EaseToAlpha        (0,   EASE_SPEED, EASE_STYLE);
 				layer_yellow.EaseToAlpha(0,   EASE_SPEED, EASE_STYLE);
 				layer_green.EaseToAlpha (255, EASE_SPEED, EASE_STYLE);
 				layer_blue.EaseToAlpha  (0,   EASE_SPEED, EASE_STYLE);
 				break;
 			case COLOR_BLUE:
 				this.EaseToAlpha        (0,   EASE_SPEED, EASE_STYLE);
 				layer_yellow.EaseToAlpha(0,   EASE_SPEED, EASE_STYLE);
 				layer_green.EaseToAlpha (0,   EASE_SPEED, EASE_STYLE);
 				layer_blue.EaseToAlpha  (255, EASE_SPEED, EASE_STYLE);
 				break;
 		}
 		
 		// Calculate the frame target for each color
 		int red_scalar    = (int)((layer_red_stride_scalar_end    - layer_red_stride_scalar_start)    * Percect) + layer_red_stride_scalar_start;
 		int yellow_scalar = (int)((layer_yellow_stride_scalar_end - layer_yellow_stride_scalar_start) * Percect) + layer_yellow_stride_scalar_start;
 		int green_scalar  = (int)((layer_green_stride_scalar_end  - layer_green_stride_scalar_start)  * Percect) + layer_green_stride_scalar_start;
 		int blue_scalar   = (int)((layer_blue_stride_scalar_end   - layer_blue_stride_scalar_start)   * Percect) + layer_blue_stride_scalar_start;
 		
 		// Ease to the proper frame target
 		super.EaseToFrameByIndex(red_scalar,           EASE_SPEED, EASE_STYLE);
 		layer_yellow.EaseToFrameByIndex(yellow_scalar, EASE_SPEED, EASE_STYLE);
 		layer_green.EaseToFrameByIndex(green_scalar,   EASE_SPEED, EASE_STYLE);
 		layer_blue.EaseToFrameByIndex(blue_scalar,     EASE_SPEED, EASE_STYLE);
 	}
 }
