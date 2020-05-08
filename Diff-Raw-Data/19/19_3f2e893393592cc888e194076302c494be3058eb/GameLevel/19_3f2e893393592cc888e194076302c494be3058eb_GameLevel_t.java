 package com.flexymind.labirynth.objects;
 
 import java.util.Vector;
 
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Paint.Style;
 import android.graphics.Point;
 import android.graphics.Rect;
 import android.graphics.drawable.Drawable;
 import android.util.Log;
 
 import com.flexymind.labirynth.screens.ScreenSettings;
 
 /**
  *  
  * @author Kurnikov Sergey + Kovalyov Alexaner
  *    +      ,   
  */
 public class GameLevel extends GameObject{
  
 	private Ball mball;
 	private FINISH mfinish;
 	private int rectHeight = 415;
 	private int rectWidth = 752;
 	private int left = 59;
 	private int top = 24;
 	private boolean dostup = true;
 	private Rect mplayField = new Rect(left,top,rectWidth,rectHeight);
     /**  */
	//private Rect mplayField = new Rect(65,30,720,415);        // 480x800 optimization
    //private Rect mplayField = new Rect(105,50,1175,705);		//1280x800 optimization

     int Number;
     Vector <Wall> Walls;
     
     private void AutoSize()
     {
         if (ScreenSettings.AutoScale)
         {
         	this.resize(ScreenSettings.ScaleFactorX, ScreenSettings.ScaleFactorY);
         }
     }
     
     /**
      *  
      * @param Vector <Wall> walls    
      * @param finish_X, finish_Y   
      * @param Diam  
      * @param Ball 
      */
     public GameLevel(	Vector <Wall> walls,
 						Ball ball, FINISH finish,
 						Drawable mBackGr){
 		// ,    
 		super(mBackGr);
 		mball   = ball;
 		mfinish = finish;
 		Walls   = walls;
 		Number  = Walls.size();
 	}
     
     public void resize(double ScaleFactorX, double ScaleFactorY)
     {
     	rectHeight = (int)(ScaleFactorX*rectHeight);
         rectWidth = (int)(ScaleFactorY*rectWidth);
         left = (int)(ScaleFactorX*left);
         top = (int)(ScaleFactorY*top);
     }
 
     @Override
     /**      */
     public void Draw(Canvas canvas)
     {	
     	if(dostup)
         {
         	rectUpdate();
         	dostup = false;
         }
     	this.mImage.setBounds(canvas.getClipBounds());
     	this.mImage.draw(canvas);
     	mfinish.Draw(canvas);
     	mball.Draw(canvas);
         for(int i=0;i < Number;i++){
         	Walls.elementAt(i).Draw(canvas);
         }
 
         Paint mPaint = new Paint();
         mPaint.setColor(Color.MAGENTA);
         mPaint.setStrokeWidth(2);
         mPaint.setStyle(Style.STROKE);
         rectHeight=rectHeight;
         rectWidth = rectWidth;
         canvas.drawRect(mplayField, mPaint);
     }
     
     @Override
     /**   */
     public void Update()
     {	
         mball.Update();
         mfinish.Update();
         for(int i=0;i<Number;i++){
         	Walls.elementAt(i).Update(); 
         }
         collisionsCheck();
     	while (collision_With_Field (mball, mplayField));
     	victory();
     }
     
     public void rectUpdate()
     {
     	AutoSize();
         mplayField.set(left, top, rectWidth, rectHeight);
     }
     
     /** ,         */
     private void collisionsCheck()
     {
     	float[] p1, p2, p3, v1, v2;
     	float[] vec1, vec2, sum;// vec1_prev, vec2_prev;
     	Wall twall;
 
         for(int i=0;i<Number;i++){
         	twall = Walls.elementAt(i);
         	p1 = new float[] {  twall.getPoint1().x - mball.mWidth / 2,
         						twall.getPoint1().y -  mball.mHeight / 2};
         	
         	p2 = new float[] {	twall.getPoint2().x - mball.mWidth / 2,
 								twall.getPoint2().y +  mball.mHeight / 2};
 
         	p3 = new float[] {	twall.getPoint3().x + mball.mWidth / 2,
 								twall.getPoint3().y -  mball.mHeight / 2};
         	
         	v1 = new float[]{ p2[0] - p1[0],
         					  p2[1] - p1[1]};
         	
         	v2 = new float[]{ p3[0] - p2[0],
 							  p3[1] - p2[1]};
         	
         	//   p1   
         	sum = new float[]{	mball.getCenter().x - p1[0],
         						mball.getCenter().y - p1[1]};
         	//  sum  v2
         	vec2 = new float[]{	sum[0] - scal_mul(sum,v1) * v1[0] / scal_mul(v1,v1),
         						sum[1] - scal_mul(sum,v1) * v1[1] / scal_mul(v1,v1)};
         	//  sum  v1
         	vec1 = new float[]{	sum[0] - vec2[0],
         						sum[1] - vec2[1]};
         	
         	/*
         	//   p1      
         	sum = new float[]{	mball.getPrevCenter().x - p1[0],
 								mball.getPrevCenter().y - p1[1]};
         	
         	vec2_prev = new float[]{	sum[0] - scal_mul(sum,v1) * v1[0] / scal_mul(v1,v1),
 										sum[1] - scal_mul(sum,v1) * v1[1] / scal_mul(v1,v1)};
         	//  . sum  v1
         	vec1_prev = new float[]{	sum[0] - vec2_prev[0],
 										sum[1] - vec2_prev[1]};
         	*/
         	
         	if (	scal_mul(vec1,v1) >= 0 
         		 && scal_mul(vec1,vec1) <= scal_mul(v1,v1)
         		 && scal_mul(vec2,v2) >= 0
         		 && scal_mul(vec2,vec2) <= scal_mul(v2,v2)){
         		// 
         		float minV1, minV2; 
         		
         		if ( scal_mul(vec1,vec1) / scal_mul(v1,v1) > ((scal_mul(v1,v1) - scal_mul(vec1,vec1)) / scal_mul(v1,v1)) ){
         			minV1 = ((scal_mul(v1,v1) - scal_mul(vec1,vec1)) / scal_mul(v1,v1));
         			Log.v("reflect","v1 dvn");
         		}
         		else{
         			minV1 = scal_mul(vec1,vec1) / scal_mul(v1,v1);
         			Log.v("reflect","v1 up");
         		}
         		
         		if ( scal_mul(vec2,vec2) / scal_mul(v2,v2) > ((scal_mul(v2,v2) - scal_mul(vec2,vec2)) / scal_mul(v2,v2)) ){
         			//    
         			minV2 = ((scal_mul(v2,v2) - scal_mul(vec2,vec2)) / scal_mul(v2,v2));
         			Log.v("reflect","v2 rigth");
         		}else{
         			//    
         			minV2 = scal_mul(vec2,vec2) / scal_mul(v2,v2);
         			Log.v("reflect","v2 left");
         		}
         		
                 if ( minV1 > minV2 ){
         			//    v2
             		mball.reflectWallV2(twall);
             		Log.v("reflect","v2");
         		}else{
         			//    v1
             		mball.reflectWallV1(twall);
             		Log.v("reflect","v1");
         		}
         	}
         }
     	
     }
 
     private float scal_mul(float[] p1, float[] p2){
     	return p1[0] * p2[0] + p1[1] * p2[1];
     }
 
     /** ,       */
     private boolean collision_With_Field (Ball ball, Rect PlayField){
     	
     	if (ball.getLeft() <= PlayField.left)
         {
             ball.reflectVertical(new Point(PlayField.left + 1, ball.getPoint().y));
             return true;
             //Log.v("reflect","vertical");
         }
         else if (ball.getRight() >= PlayField.right)
         {
         	ball.reflectVertical(new Point(PlayField.right - ball.getWidth() - 1, ball.getPoint().y));
         	return true;
         	//Log.v("reflect","vertical");
         }
     	
     	if (ball.getTop() <= PlayField.top)
 	    {
 	        ball.reflectHorizontal(new Point(ball.getPoint().x, PlayField.top + 1));
 	        return true;
 	        //Log.v("reflect","horizontal");
 	    }
 	    else if (ball.getBottom() >= PlayField.bottom)
 	    {
 	    	ball.reflectHorizontal(new Point(ball.getPoint().x, PlayField.bottom - ball.getHeight() - 1));
 	    	return true;
 	    	//Log.v("reflect","horizontal");
 	    }
 		return false;
     }
     
     /**    */
     protected void victory() {
 
     	if(GameObject.intersects_finish(mball, mfinish))
     	{
     		mball.setCenterY(mfinish.getCenter().y);
 			mball.setCenterX(mfinish.getCenter().x);
     		
     	}
     	/*if (	 (bCenter.x >= end_x - diam / 2)
     			  && (bCenter.x <= end_x + diam / 2)
     			  && (bCenter.y >= end_y - diam / 2)
     			  && (bCenter.y <= end_y + diam / 2) ){
     			  
     		bCenter.y =end_y + diam / 2+(int) Math.sqrt(Math.abs(Math.pow(diam, 2)-Math.pow((bCenter.x-end_x - diam / 2), 2)));
 			mball.setCenterY(bCenter.y);
 			mball.setCenterX(bCenter.x);
     	}*/
     }
 }
