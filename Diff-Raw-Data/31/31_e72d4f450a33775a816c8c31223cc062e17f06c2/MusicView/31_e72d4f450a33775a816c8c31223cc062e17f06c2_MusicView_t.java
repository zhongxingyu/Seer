 package com.anthropomo.digitalrattle.ui;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.drawable.GradientDrawable;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 
 import com.anthropomo.digitalrattle.R;
 import com.anthropomo.digitalrattle.io.IEventPublisher;
 import com.anthropomo.digitalrattle.io.INoteListener;
 import com.anthropomo.digitalrattle.models.Matrix;
 import com.anthropomo.digitalrattle.models.Matrix.MatrixConsumer;
 
 public class MusicView extends View implements OnTouchListener, MatrixConsumer {
 	private GradientDrawable circle;
 	private int circleDiam;
 	private int x = 0; // x & y are coords of latest touch event
 	private int y = 0;
 	private int lastNum = 0; // the number of the cell holding the coordinates of the previous touch event
 	private Matrix matrix;
 	private INoteListener noteListener;
 	private float resize = 1;
 
 	
 	public MusicView(Context context){
 		super(context);
 	}
 	
 	public MusicView(Context context, int circleDiam, IEventPublisher eventPublisher, INoteListener noteListener) {
 		this(context);
 		this.matrix = new Matrix(eventPublisher, this);
		this.circleDiam = (int)matrix.getSpaceWidth()*2;
 		this.noteListener = noteListener;
 		circle = (GradientDrawable)context.getResources().getDrawable(R.drawable.cloud);
 	}
 	
     protected void onDraw(Canvas canvas) {
     	if(x != 0 && y != 0){
     		circle.setGradientRadius(circleDiam/2*resize);
     		circle.setBounds(x-circleDiam, y-circleDiam, x+circleDiam, y+circleDiam);
     		circle.draw(canvas);
     	}
     }
 
     
 	private float sineWave(float index){
 		return scaleWave((float)Math.sin(index), 0.125f);
 	}
 
 	private float scaleWave(float index, float scale){
 		return index * scale + 1;
 	}
 	
 	private float modulateSize(float index){
 		return sineWave(index);
 	}
     
 	@Override
 	public boolean onTouch(View v, MotionEvent event) {
 		x = (int)event.getX();
 		y = (int)event.getY();
 		int num = 0;
 		resize = modulateSize(x);
 		if(event.getAction() == MotionEvent.ACTION_DOWN){ // Plays a tone when a space is first touched
 			num = matrix.getCell(x,y);
 			playSpace(num);
 			invalidate();
 			lastNum = num;
 			return true;
 		}
 		else if(event.getAction() == MotionEvent.ACTION_MOVE){ // Plays a new tone whenever a new space is enter
 			num = matrix.getCellNum(x,y);
 			if(num != lastNum){
 				playSpace(num);
 			}
 			invalidate();
 			lastNum = num;
 			return true;
 		}
 		else{ // Sets coordinates to 0 on ACTION_UP, etc, causing finger follower to disappear 
 			x = 0;
 			y = 0;
 			invalidate();
 			return false;
 		}
 	}
 	
 	private void playSpace(int num){
 		noteListener.onNotePlayed(num);
 	}
 
 	@Override
 	public void onMatrixUpdate() {
 		// TODO Auto-generated method stub
 		
 	}
 }
