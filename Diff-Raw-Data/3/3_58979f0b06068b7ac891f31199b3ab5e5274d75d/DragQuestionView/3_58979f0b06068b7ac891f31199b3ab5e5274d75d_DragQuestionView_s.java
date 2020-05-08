 package edu.upenn.cis350.voice;
 
 import android.util.AttributeSet;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Picture;
 import android.graphics.RectF;
 import android.view.MotionEvent;
 import android.content.Context;
 import android.view.View;
 
 public class DragQuestionView extends VoiceView {
 	private static RectF square =new RectF(100,200,200,300);
 	private static int pic1topx=5;
 	private static int pic2topx=95;
 	private static int pic3topx=185;
 	private static int pic1topy=5;
 	private static int pic2topy=5;
 	private static int pic3topy = 5;
 	private static int pic1bottomx = 85;
 	private static int pic2bottomx=175;
 	private static int pic3bottomx=265;
 	private static int pic1bottomy=85;
 	private static int pic2bottomy=85;
 	private static int pic3bottomy=85;
 	private static RectF pic1 = new RectF(pic1topx, pic1topy, pic1bottomx, pic1bottomy);
 	private static RectF pic2 = new RectF(pic2topx, pic2topy, pic2bottomx, pic2bottomy);
 	private static RectF pic3 = new RectF(pic3topx, pic3topy, pic3bottomx, pic3bottomy);
 	private int answerInt=0;
 	private static int xtoffset=0;
 	private static int ytoffset=0;
 	private static int xboffset=0;
 	private static int yboffset=0;
 	private static boolean is1, is2, is3, isPressed = false;
 	
 	public DragQuestionView(Context c) {
 		super(c);
 	}
 	
 	public DragQuestionView(Context c, AttributeSet a) {
 		super(c, a);
 	}
 	
 	protected void onDraw(Canvas canvas) {
 		Color boxColor = new Color();
 		int colorInt = boxColor.rgb(176,196,222);
 		Paint paint = new Paint();
 		paint.setColor(colorInt);
 		if(square.intersect(pic1) ){
 			answerInt = 1;
 		}
 		else if(square.intersect(pic2) ){
 			answerInt = 2;
 		}
 		else if(square.intersect(pic3) ){
 			answerInt = 3;
 		}
 		square.set(100,200,200,300);
 		pic1.set((float)pic1topx, (float)pic1topy, (float)pic1bottomx, (float)pic1bottomy);
 		pic2.set((float)pic2topx, (float)pic2topy, (float)pic2bottomx, (float)pic2bottomy);
 		pic3.set((float)pic3topx, (float)pic3topy, (float)pic3bottomx, (float)pic3bottomy);
 		canvas.drawRect(square, paint);
 		Bitmap pic = BitmapFactory.decodeResource(this.getResources(), R.drawable.tempimage);
 		canvas.drawBitmap(pic, null, pic1, paint);
 		pic = BitmapFactory.decodeResource(this.getResources(), R.drawable.tempimage2);
 		canvas.drawBitmap(pic, null, pic2, paint);
 		pic = BitmapFactory.decodeResource(this.getResources(), R.drawable.tempimage3);
 		canvas.drawBitmap(pic, null, pic3, paint);
 	}
 	
 	@Override
 	public boolean onTouchEvent (MotionEvent event) {
 		int eventAction = event.getAction();
 		int actionX = (int)event.getX(event.findPointerIndex(event.getActionIndex()));
 		int actionY = (int)event.getY(event.findPointerIndex(event.getActionIndex()));
 		
 		if(eventAction ==0 ){
 			if(actionX < pic1bottomx && actionY < pic1bottomy && actionX > pic1topx && actionY > pic1topy){
 				xtoffset = pic1topx -actionX;
 				ytoffset = pic1topy - actionY;
 				xboffset =  pic1bottomx-actionX;
 				yboffset = pic1bottomy-actionY ;
 				is1 = true;
 				invalidate();}
 			else if(actionX < pic2bottomx && actionY < pic2bottomy && actionX > pic2topx && actionY > pic2topy){
 				xtoffset = pic2topx -actionX;
 				ytoffset = pic2topy - actionY;
 				xboffset =  pic2bottomx-actionX;
 				yboffset = pic2bottomy-actionY ;
 				is2=true;
 				invalidate();}
 			else if(actionX < pic3bottomx && actionY < pic3bottomy && actionX > pic3topx && actionY > pic3topy){
 				xtoffset = pic3topx -actionX;
 				ytoffset = pic3topy - actionY;
 				xboffset =  pic3bottomx-actionX;
 				yboffset = pic3bottomy-actionY ;
 				is3=true;
 				invalidate();}
 			isPressed = true;
 		}
 		if(event.getPressure()!=0 && isPressed){
 			if(is1){
 				pic1topx = actionX+ xtoffset;
 				pic1topy = actionY+ ytoffset;
 				pic1bottomx = actionX+ xboffset;
 				pic1bottomy = actionY+ yboffset;
 				invalidate();}
 			else if(is2){
 				pic2topx = actionX+ xtoffset;
 				pic2topy = actionY+ ytoffset;
 				pic2bottomx = actionX+ xboffset;
 				pic2bottomy = actionY+ yboffset;
 				invalidate();}	
 			else if(is3){
 				pic3topx = actionX+ xtoffset;
 				pic3topy = actionY+ ytoffset;
 				pic3bottomx = actionX+ xboffset;
 				pic3bottomy = actionY+ yboffset;
 				invalidate();}	
 		}
 		
 		if(isPressed == true && eventAction ==1){
 			is1= is2 = is3 = isPressed = false;
 			invalidate();
 		}
 		return true;
 	}
 	
 	public int getAnswer(){
 		return answerInt;
 	}
 }
