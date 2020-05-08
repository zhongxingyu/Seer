 package com.hekai.camera;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Paint.Style;
 import android.util.Log;
 import android.view.SurfaceView;
 
 public class HistogramView extends OverlayView{
 	
 	private static final String TAG="HistogramView";
 	
 	private Paint whitePaint;
 	
 	
 	
 	private int[] drawBuffer,cacheBuffer;
 	
 	
 
 	public HistogramView(Context context) {
 		super(context);
 	}
 	
 	public void init(){
 		whitePaint=new Paint();
 		whitePaint.setColor(Color.WHITE);
 		whitePaint.setStyle(Style.STROKE);
 		
 		cacheBuffer=new int[256];
 		drawBuffer=new int[256];
 	}
 	
 	@Override
 	protected void onDraw(Canvas canvas) {
 //		Log.d(TAG, "onDraw");
 		super.onDraw(canvas);
 
 		int angle = 90 * mRotation;
 		canvas.rotate(angle,mCenterX,mCenterY);
 		
 		canvas.drawRect(mCenterX-129, mCenterY-128, mCenterX+128, mCenterY+127, whitePaint);
 		
 		int maxValue=getMaxValue(drawBuffer);
 		
 		float ratio=(float)maxValue/255;
 		
 		for(int i=0;i<drawBuffer.length;i++){
 			if(drawBuffer[i]>0){
 				int len=(int) (drawBuffer[i]/ratio);
 				canvas.drawLine(mCenterX-129+(i+1), mCenterY+127, mCenterX-129+(i+1), mCenterY+127-len, whitePaint);
 			}
 		}
 		
 	}
 	
 	@Override
 	public void updateData(byte[] data,int width,int height,int format){
 		for(int i=0;i<cacheBuffer.length;i++)
 			cacheBuffer[i]=0;
 		
 		for (int j = 0; j < height; j++) {
 			for (int i = 0; i < width; i++) {
 				int y=(0xff & (int)(data[j*width+i]));
 				cacheBuffer[y]++;
 			}
 		}
 		System.arraycopy(cacheBuffer, 0, drawBuffer, 0, cacheBuffer.length);
 	}
 	
 	private int getMaxValue(int[] array){
 		int max=0;
 		int index=0;
 		for(int i=0;i<array.length;i++){
 			if(array[i]>max){
 				max=array[i];
 				index=i;
 			}
 		}
 //		Log.d(TAG,"max="+max+",index="+index);
 		return max;
 	}
 	
 }
