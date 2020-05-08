 package com.tan_nai.javaversionbreakapp;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.PorterDuff;
 import android.util.Log;
 import android.view.SurfaceHolder;
 
 public class SerfaceHolderCallBack implements SurfaceHolder.Callback, Runnable{
 
  private SurfaceHolder holder = null;
  private Thread thread = null;
  private boolean isAttached = true;
 private Bitmap sougen;
 private int width,height;
 private int droidx = 0, droidy = 0;
 private ArrayList<BugObject> bugs;
 VersionNumber vn;
 VersionNumber mokuhyo;
 Context con;
  
 public SerfaceHolderCallBack(Context context){
   Resources res = context.getResources();
   con = context;
   vn = new VersionNumber(7, 40);
   mokuhyo = new VersionNumber(7,66);
   sougen = BitmapFactory.decodeResource(res, R.drawable.ic_launcher);
   bugs = new ArrayList<BugObject>();
   bugs.add(new BugObject(width/2,0,0));
   }
 
  @Override
  public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
 	 this.width = width;
 	 this.height = height;
 	 droidx = 0;
 	 droidy = (int) (height*0.8);
  }
 
  @Override
  public void surfaceCreated(SurfaceHolder holder) {
  	this.holder = holder;
  	thread = new Thread(this);
  	thread.start(); //スレッドを開始
  }
 
  @Override
  public void surfaceDestroyed(SurfaceHolder holder) {
   isAttached = false;
   thread = null; //スレッドを終了
  }
 
  private boolean versionUpdate(BugObject bug){
 	 try{
 	switch(bug.type){
 	case 0:
 		vn = vn.nextSecurityUpdate();
 		break;
 	case 1:
 		vn = vn.nextCriticalPatchUpdate();
 		break;
 	case 2:
 		vn = vn.nextLimitedUpdate();
 		break;
 	}
 	 }catch(IllegalStateException e){
 		 return false;
 	 }
 	 if(vn.gt(mokuhyo)) return false;
 	 if(!vn.lt(mokuhyo)) mokuhyo.setFamilyNumber(mokuhyo.getFamilyNumber()+29);
 	 return true;
  }
  
  public void move(float x){
 	 droidx -= x;
 	 if(droidx>width-(sougen.getWidth() / 2)) droidx = width-(sougen.getWidth() / 2);
 	 if(droidx<(sougen.getWidth() / 2)) droidx=(sougen.getWidth() / 2);
  }
  
  @Override
  public void run() {
  	// メインループ（無限ループ）
  	while( isAttached ){
  	 long t1 = System.currentTimeMillis();
 
  	 //描画処理を開始
  	 Canvas canvas = holder.lockCanvas();
  	 canvas.drawColor(0,PorterDuff.Mode.CLEAR );
  	 Paint paint = new Paint();
   paint.setColor(Color.WHITE);
   paint.setTextSize(40);
   
   
   for(int i=0;i<bugs.size();i++){
 	  canvas.drawBitmap(sougen, bugs.get(i).x,bugs.get(i).y, paint);
   }
   
   //プレイヤー
   canvas.drawBitmap(sougen, droidx - (sougen.getWidth() / 2) , droidy , paint);
   
   //現在のバージョンと目標のバージョン
   canvas.drawText(vn.toString(), 0, 48, paint);
   canvas.drawText(mokuhyo.toString(), 0, 96, paint);
 
 
  	 //描画処理を終了
  	 holder.unlockCanvasAndPost(canvas);
  	 
  	 for(int i=0;i<bugs.size();i++){
  		 bugs.get(i).y++;
  		 if(bugs.get(i).y==droidy){
  			 if(Math.abs(bugs.get(i).x - droidx) <= sougen.getWidth() / 2){
  				 if(! versionUpdate(bugs.get(i))){
  					Intent intent = new Intent(con, GameoverActivity.class);
  					con.startActivity(intent);
  				 }
  				 bugs.remove(i);
  				 i--;
  			 }
  		 }else{
  			 if(bugs.get(i).y > height){
  				 bugs.remove(i);
  				 i--;
  			 }
  		 }
  	 }
  	 
  	 //無限湧き
  	 Random rand = new Random();
  	 bugs.add(new BugObject(rand.nextInt(width),0,0));
 
  	 // スリープ
  	 long t2 = System.currentTimeMillis();
  	 if(t2 - t1 < 16){ // 1000 / 60 = 16.6666
  	 try {
  	 Thread.sleep(16 - (t2 - t1));
  	 } catch (InterruptedException e) {
  	 }
  	 }
  	 }
  	}
  }
