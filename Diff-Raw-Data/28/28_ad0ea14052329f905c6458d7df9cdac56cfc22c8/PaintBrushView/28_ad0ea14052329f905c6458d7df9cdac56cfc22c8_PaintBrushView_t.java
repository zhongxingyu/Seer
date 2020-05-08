 package com.hackamaroo.hw2;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Stack;
 import java.util.Random;
 
 import android.annotation.SuppressLint;
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Path;
 import android.graphics.drawable.ShapeDrawable;
 import android.graphics.drawable.shapes.OvalShape;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 
 public class PaintBrushView extends View implements OnTouchListener{
 
 	protected int drawColor = Color.WHITE; 
 	protected int size = 7; 
 	public List<Point> pdrawn = new ArrayList<Point>();
 	public Stack<Point> asdf = new Stack<Point>(); 
 	public Path path;
 	public ShapeDrawable circle = new ShapeDrawable(new OvalShape());
 	static int incX = 0; 
 	static int incY = 0; 
 	static int startY; 
 	static int startX;
	Collision col = new Collision(); 
 
 	public PaintBrushView(Context context) {
 		super(context);
 		init();
 	}
 	
 	public PaintBrushView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		init();
 	}
 	
 	public PaintBrushView(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 		init();
 	}
 	
 	protected void init(){
 		Random random = new Random();
 		
 		this.setOnTouchListener(this);
 		circle.getPaint().setColor(0xff74AC23);
 		//circle.setBounds(230, 220, 230+80, 220+80);
 		startY = random.nextInt(500)+(getWidth()/2); //getHeight()/2; 
 		startX = getHeight();
 	}
 	
 	@SuppressLint("DrawAllocation")
 	protected void onDraw(Canvas canvas){
 		Random random = new Random();
		boolean checkCollide = false; 
		//Check for collisions
		if(pdrawn.size() >=2){
			//if(!pdrawn.get(pdrawn.size()-1).getFirst() && !pdrawn.get(pdrawn.size()-2).getFirst()){
				checkCollide = col.checkCollision(pdrawn.get(pdrawn.size()-2), pdrawn.get(pdrawn.size()-1), startX+incX, startY+incY, 50);
				Log.v("checkCollide", Boolean.toString(checkCollide));
			//}
		}
 		Paint p = new Paint();
 		circle.draw(canvas);
 		//inc +=10; 
 		
 		// Increase the radius a bit
 		circle.setBounds(startY+incY, startX+incX, startY+50+incY, startX+50+incX);
 		
 		// Reset the coordinates of the new object if it goes off the screen
 		// Note: This is the same instance
		if (startY+incY < 0 || startX+incX > getWidth() || startY+incY > getHeight() || startX+incX < 0 || checkCollide) {
			startY = random.nextInt(150)+(getWidth()/2)-200; //800; //getHeight();
 			startX = getHeight();
			Log.v("startx", Integer.toString(getWidth()));
			Log.v("starty", Integer.toString(getHeight()));
 			MainActivity.Vy = -25; // reset to default value
			MainActivity.ti = 1; // reset time to zero
 			incX = 0; // reset everything
 			incY = 0; // reset everything
			checkCollide = false; 
			pdrawn.clear();
 		}
 
 		for(int i = 0; i < pdrawn.size(); i++){
 			Point pt = pdrawn.get(i);
 			p.setColor(pt.getColor());
 			canvas.drawCircle(pt.getX(),pt.getY(),pt.getSize(),p);
 			if(!pt.getFirst() && i != 0){
				//canvas.drawLine( pdrawn.get(i-1).getX(), pdrawn.get(i-1).getY(), pt.getX(), pt.getY(),p);
 			}
 			else{
 				p.setStrokeWidth(pt.getSize()*2);
 			}
 			
 		}
 		
 	}
 
 	public void clearPoints(){
 		pdrawn = new ArrayList<Point>(); 
 	}
 	
 	
 	public boolean onTouch(View view, MotionEvent event){
 		if (event.getAction() == MotionEvent.ACTION_DOWN ) {
 			Point newP = new Point(event.getX(),event.getY(),drawColor,size, true); 
 			pdrawn.add(newP);
 			Log.v(Float.toString(event.getX()), Float.toString(event.getY())); 
 			invalidate();
 		}
 		else if (event.getAction() != MotionEvent.ACTION_UP){
 			Point newP = new Point(event.getX(),event.getY(),drawColor,size, false); 
 			pdrawn.add(newP);
 			//Log.v("tag", circle.getBounds().toString()); 
 			invalidate();
 		}
 		return true; 
 		
 	}
 	
 	public void setColor(int a){
 		drawColor = a; 
 	}
 	
 	public void setSize(int a){
 		size = a; 
 	}
 }
