 package com.dosolves.gym.app.performance.gui;
 
 import java.util.Date;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.view.View;
 import android.widget.Button;
 
 import com.dosolves.gym.R;
 import com.dosolves.gym.domain.performance.Set;
 import com.dosolves.gym.utils.StringUtils;
 
 public class SetButton extends Button {
 
 	private static final int DARK_GREEN = Color.rgb(33, 150, 00);
 	private static final int MARGIN = 5;
 	private Rect clipBounds = new Rect();
 	private Rect textBounds = new Rect();
 	
 	private Paint textPaint = new Paint();
 	
 	private Set set;
 	private SetClickedCallback callback;
 
 	@SuppressWarnings("deprecation")
 	public SetButton(Context context) {
 		this(context, new Set(1,1,12,50.5,new Date(2000,1,1)));		
 	}
 	
 	public SetButton(Context context,Set set) {
 		super(context);
 		this.set = set;
 		textPaint.setTextSize(getResources().getDimension(R.dimen.button_text_size));
 		setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				callback.onSetClicked(SetButton.this.set);
 			}
 		});
 	}
 	
 	public void setSetClickedCallback(SetClickedCallback callback){
 		this.callback = callback;		
 	}
 	
 	@Override
 	protected void onDraw(Canvas canvas) {
 		super.onDraw(canvas);
 		canvas.getClipBounds(clipBounds);
 		String reps = Integer.toString(set.getReps());
 		String weight = StringUtils.doubleToStringRemoveTrailingZeros(set.getWeight());
 		
 		float repsX = findCenterXForString(reps, clipBounds);
 		float repsY = findCenterYForReps(reps, clipBounds);
 		
 		float weightX = findCenterXForString(weight, clipBounds);
 		float weightY = findCenterYForWeight(weight, clipBounds);
 		textPaint.setColor(DARK_GREEN);
 		canvas.drawText(reps, repsX, repsY, textPaint);
 		textPaint.setColor(Color.RED);
 		canvas.drawText(weight, weightX, weightY, textPaint);
 	}
 	
 	private float findCenterYForReps(String reps, Rect rect) {		
 		return (rect.height()/2) - MARGIN;
 	}
 	
 	private float findCenterYForWeight(String weight, Rect rect) {
 		textPaint.getTextBounds(weight, 0, weight.length(), textBounds);
 		return (rect.height()/2) + (textBounds.height()) + MARGIN;
 	}
 
 	private float findCenterXForString(String text, Rect rect) {
 		return (rect.width()/2)-(textPaint.measureText(text)/2);
 	}
 	
 }
