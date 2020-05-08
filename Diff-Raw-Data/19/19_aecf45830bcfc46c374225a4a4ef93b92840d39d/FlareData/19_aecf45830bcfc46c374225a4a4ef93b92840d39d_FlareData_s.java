 package com.puzzleduck.FlareLiveWallpaper;
 import android.graphics.*;
 
 public class FlareData {
 	private float x;
 	private float y;
 	private float angle;
 	private float time;
 	private float triggerTime;
 	private float stage1Time;
 	private float stage2Time;
 	private float stage3Time;
 	//private float tilt;
 	private int color1;
 	private int color2;
 	private int type;
 	private int explosionRadius;
 	private int explosionCount;
 	private int explosion2Count;
 	private int explosion2Radius;
 	public FlareData() {
 		x = 100;
 		y = 100;
 		angle = 90;
 		time = 0;
 		triggerTime = 50;
 		setStage1Time(50);
 		setStage2Time(20);
 		setStage3Time(10);
 		//tilt = 0;
 		color1 = 0xFF00FF00;
 		color2 = 0xFF0000FF;
 		type = 0;
 		explosionRadius = 0;
 		explosionCount = 0;
 		explosion2Count = 0;
 		explosion2Radius = 0;
 	}
 	
 	public FlareData(float inx, float iny, float inTilt, int incolor1, int incolor2, int intype, int stage1, int stage2, int stage3)
 	{
 		x = inx;
 		y = iny;
 		angle = 90-inTilt;
 		time = 0;
 		triggerTime = 50;
 		setStage1Time(stage1);
 		setStage2Time(stage2);
 		setStage3Time(stage3);
 		//tilt = inTilt;
 		color1 = incolor1;
 		color2 = incolor2;
 		type = intype;
 		explosionRadius = 0;
 		explosionCount = 0;
 		explosion2Count = 0;
 		explosion2Radius = 0;
 	}
 
 	public void render(Canvas c, Paint mPaint)
 	{
 		// TODO: Implement this method
 
 		//render
		if (thisFlare.getTime() < thisFlare.getStage1Time()) {
 			//stage 1
 //			            Log.d(TAG, "S1");
 //						mPaint.setColor(0xFF00FF00);
 			mPaint.setColor(0xCCDDDD00);
 //						c.drawCircle(thisFlare.getX(), thisFlare.getY(), 3, mPaint);
			c.drawRoundRect(new RectF(thisFlare.getX(), thisFlare.getY(), thisFlare.getX()+1, thisFlare.getY()+5), 0, 0, mPaint);
 		} else {
			if (thisFlare.getTime() < thisFlare.getStage1Time() + thisFlare.getStage2Time()) {
 				//stage 2
 //				            Log.d(TAG, "S2");
 
 				if(thisFlare.getExplosionCount() == 0) //new explosion
 				{
 					thisFlare.setExplosionCount(6 + rng.nextInt(20) );
 //								Log.d(TAG, "\n\nFlare count: " + thisFlare.getExplosionCount());
 
 //								for(int i = thisFlare.getExplosionCount(); i > 0; i--)
 //								{	
 //									Log.d(TAG, "   -flare" + i +": " + thisFlare.getExplosionCount());
 //									Log.d(TAG, "        i/count " + (double)i/(double)thisFlare.getExplosionCount() );
 //									Log.d(TAG, "        i/count*360 " + (double)((double)i/(double)thisFlare.getExplosionCount())*360.0 );
 //									Log.d(TAG, "        rad sin i/count*360 " + (float)((double)thisFlare.getExplosionRadius()+20) * Math.sin((double)((double)i/(double)thisFlare.getExplosionCount())*359.0));
 //									Log.d(TAG, "        rad cos i/count*360 " + (float)((double)thisFlare.getExplosionRadius()+20) * Math.cos((double)((double)i/(double)thisFlare.getExplosionCount())*359.0));
 //								}
 				}
 
 				mPaint.setColor(thisFlare.getColor1());
 				thisFlare.incrementExplosionRadius();
 				thisFlare.incrementExplosionRadius();
 				//fade colors
 				if(rng.nextInt(4) == 1)
 				{
 					thisFlare.setColor1(thisFlare.getColor1()-0x01000000);
 				}
 				//drift down
 				if(rng.nextInt(40) == 1)
 				{
 					thisFlare.setY(thisFlare.getY()+1);
 				}
 
 				for(int i = thisFlare.getExplosionCount(); i > 0; i--)
 				{
 					c.drawCircle(
 						(float)((double)thisFlare.getExplosionRadius() * (Math.sin((double)((double)i/(double)thisFlare.getExplosionCount())*0.017453293*360)) + thisFlare.getX() + rng.nextInt(2)-4),
 						(float)((double)thisFlare.getExplosionRadius() * (Math.cos((double)((double)i/(double)thisFlare.getExplosionCount())*0.017453293*360)) + thisFlare.getY() + rng.nextInt(2)-4),
 						1,
 						mPaint);
 //								c.drawCircle(
 //										(float) (thisFlare.getX() + (Math.cos( (i/thisFlare.getExplosionCount())*360 ) * thisFlare.getExplosionRadius())),
 //										(float) (thisFlare.getY() + (Math.sin((i/thisFlare.getExplosionCount())*360) * thisFlare.getExplosionRadius())),
 //										2,
 //										mPaint);
 				}
 			}else
 			{
 				if(thisFlare.getTime() < thisFlare.getStage1Time() + thisFlare.getStage2Time() + thisFlare.getStage3Time())
 				{
 					//stage 3
 					mPaint.setColor(thisFlare.getColor2());
 					thisFlare.incrementExplosion2Radius();
 
 					if(thisFlare.getExplosion2Count() == 0) //new explosion
 					{
 						thisFlare.setExplosion2Count(4 + rng.nextInt(8) );
 						//								Log.d(TAG, "\n\nFlare2 count: " + thisFlare.getExplosionCount());
 
 					}
 
 
 
 					for(int i = thisFlare.getExplosionCount(); i > 0; i--)
 					{
 						for(int j = thisFlare.getExplosion2Count(); j > 0; j--)
 						{
 
 							switch(rng.nextInt(5))
 							{
 								case(0):
 									mPaint.setColor(thisFlare.getColor2()-0x2200000);
 
 								case(2):
 
 									mPaint.setColor(thisFlare.getColor2()+0x22000000);
 									break;
 								default:
 									mPaint.setColor(thisFlare.getColor2());
 									break;
 							}
 							c.drawPoint(
 								(float)( (double)thisFlare.getExplosion2Radius() * (Math.sin((double)((double)j/(double)thisFlare.getExplosion2Count())*0.017453293*360)) +     (double)thisFlare.getExplosionRadius() * (Math.sin((double)((double)i/(double)thisFlare.getExplosionCount())*0.017453293*360)) + thisFlare.getX() + rng.nextInt(2)-4),
 								(float)( (double)thisFlare.getExplosion2Radius() * (Math.cos((double)((double)j/(double)thisFlare.getExplosion2Count())*0.017453293*360)) +     (double)thisFlare.getExplosionRadius() * (Math.cos((double)((double)i/(double)thisFlare.getExplosionCount())*0.017453293*360)) + thisFlare.getY() + rng.nextInt(2)-4),
 								mPaint);
 
 						}
 
 						//fade colors
 						if(rng.nextInt(5) == 1)
 						{
 							thisFlare.setColor2(thisFlare.getColor2()-0x01000000);
 						}
 
 						//drift down
 						if(rng.nextInt(40) == 1)
 						{
 							thisFlare.setY(thisFlare.getY()+1);
 						}
 					}
 
 
 
 
 				}else
 				{
 					//expiring:   ... working :)
 					fIterator.remove();
 
 				}
 			}
 
 		}//else
 	}
 
 
 	public void move()
 	{
 		// TODO: Implement this method
 
 		//move  old flare/virs
		if (thisFlare.getTime() < thisFlare.getStage1Time()) 
 		{
			thisFlare.setY((float) (thisFlare.getY()
						   + Math.sin(SystemClock.elapsedRealtime()) - 0.2 * thisFlare.getTime() ));
 			//thisFlare.setX((float) thisFlare.getX()
 			//		+ (float) Math.sin(SystemClock.elapsedRealtime()) + (( thisFlare.getTilt() * thisFlare.getTime())/80)  );
 
 			//removing tilt:
 
 
 		} 
 //					else {
 //					}
 //						if (thisFlare.getTime() > thisFlare.getStage1Time()*3) 
 //						{
 //							//
 //						}
 
		thisFlare.setTime(thisFlare.getTime() + 1);
 		
 	}
 
 	
 	public float getAngle(){
 		return angle;
 	}
 	
 	public void setAngle(float inAngle){
 		angle = inAngle;
 	}
 	
 	public float getX() {
 		return x;
 	}
 
 	public void setX(float x) {
 		this.x = x;
 	}
 
 	public float getY() {
 		return y;
 	}
 
 	public void setY(float y) {
 		this.y = y;
 	}
 
 	public float getTime() {
 		return time;
 	}
 
 	public void setTime(float time) {
 		this.time = time;
 	}
 
 	public float getTriggerTime() {
 		return triggerTime;
 	}
 
 	public void setTriggerTime(float triggerTime) {
 		this.triggerTime = triggerTime;
 	}
 
 //	public float getTilt() {
 //		return tilt;
 //	}
 //
 //	public void setTilt(float tilt) {
 //		this.tilt = tilt;
 //	}
 
 	public int getColor1() {
 		return color1;
 	}
 
 	public void setColor1(int color1) {
 		this.color1 = color1;
 	}
 
 	public int getColor2() {
 		return color2;
 	}
 
 	public void setColor2(int color2) {
 		this.color2 = color2;
 	}
 
 	public int getType() {
 		return type;
 	}
 
 	public void setType(int type) {
 		this.type = type;
 	}
 
 	public void setStage1Time(float stage1Time) {
 		this.stage1Time = stage1Time;
 	}
 
 	public float getStage1Time() {
 		return stage1Time;
 	}
 
 	public void setStage2Time(float stage2Time) {
 		this.stage2Time = stage2Time;
 	}
 
 	public float getStage2Time() {
 		return stage2Time;
 	}
 
 	public void setStage3Time(float stage3Time) {
 		this.stage3Time = stage3Time;
 	}
 
 	public float getStage3Time() {
 		return stage3Time;
 	}
 
 	public void setExplosionRadius(int explosionRadius) {
 		this.explosionRadius = explosionRadius;
 	}
 	public void incrementExplosionRadius() {
 		this.explosionRadius ++;
 	}
 
 	public int getExplosionRadius() {
 		return explosionRadius;
 	}
 
 	public void setExplosionCount(int explosionCount) {
 		this.explosionCount = explosionCount;
 	}
 
 	public int getExplosionCount() {
 		return explosionCount;
 	}
 
 	public void setExplosion2Count(int explosion2Count) {
 		this.explosion2Count = explosion2Count;
 	}
 
 	public int getExplosion2Count() {
 		return explosion2Count;
 	}
 
 	public void incrementExplosion2Radius() {
 		this.explosion2Radius ++;
 		
 	}
 	public int getExplosion2Radius() {
 		return explosion2Radius;
 	}
 }
