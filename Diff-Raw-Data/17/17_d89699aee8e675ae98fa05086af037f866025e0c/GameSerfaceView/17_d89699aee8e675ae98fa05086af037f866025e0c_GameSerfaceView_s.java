 package com.tan_nai.javaversionbreakapp;
 import android.content.Context;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 
 public class GameSerfaceView extends SurfaceView{
 
  private SerfaceHolderCallBack cb;
  private SurfaceHolder holder;
 
  public GameSerfaceView(Context context) {
  	super(context);
  	holder = getHolder();
  	cb = new SerfaceHolderCallBack(context);
  	holder.addCallback(cb);
  }
  @Override
  public boolean onTouchEvent(MotionEvent event) {
   float x = event.getX(); // X座標を取得
   float y = event.getY(); // Y座標を取得
   return true;
  	 
  }
  
  public void move(float x){
 	 cb.move(x);
  }
  
  public void runStop(){
//	 holder.removeCallback(cb);
 	 cb.surfaceDestroyed(holder);
	 cb = null;
  }
  
 }
