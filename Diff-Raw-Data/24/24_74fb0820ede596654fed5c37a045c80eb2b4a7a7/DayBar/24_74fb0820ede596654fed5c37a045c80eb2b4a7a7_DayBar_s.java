 package giulio.frasca.silencesched.weekview;
 
 import giulio.frasca.silencesched.EditEventActivity;
 import giulio.frasca.silencesched.RingerSettingBlock;
 import giulio.frasca.silencesched.Schedule;
 
 import java.util.Iterator;
 import java.util.LinkedList;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.media.AudioManager;
 import android.os.Bundle;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 
 
 public class DayBar extends View{
 	
 	private Canvas canvas;
 
     private Bitmap bitmap;
     private String dayName;
     private int defaultRingLevel;
     private LinkedList<RingerSettingBlock> relatedBlocks;
     private LinkedList<VisualBlock> drawList;
     private Schedule schedule;
 	
 	
 	public DayBar(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		dayName="Sunday";
 		relatedBlocks=new LinkedList<RingerSettingBlock>();
 		defaultRingLevel=getDefaultRingLevel();
 	}
 	
 	public void loadApplicableBlocks(LinkedList<RingerSettingBlock> allBlocks){
 		clearEntireList();
 		Iterator<RingerSettingBlock> i = allBlocks.iterator();
 		while (i.hasNext()){
 			RingerSettingBlock block = i.next();
 			if (blockIsApplicable(block)){
 				addBlock(block);
 			}
 		}
 		defaultRingLevel=getDefaultRingLevel();
 	}
 	public void loadApplicableBlocks(Schedule schedule){
 		this.schedule=schedule;
 		LinkedList<RingerSettingBlock> allBlocks=schedule.getList();
 		clearEntireList();
 		Iterator<RingerSettingBlock> i = allBlocks.iterator();
 		while (i.hasNext()){
 			RingerSettingBlock block = i.next();
 			if (blockIsApplicable(block)){
 				addBlock(block);
 			}
 		}
 		defaultRingLevel=getDefaultRingLevel();
 	}
 	
 	public boolean blockIsApplicable(RingerSettingBlock block){
 		if (!block.isEnabled()){return false;}
 		if (block.getRepeatUntil() < System.currentTimeMillis()){ return false;}
 		if (dayName.equals("Sunday") && block.isEnabledSunday()) { return true;}
 		if (dayName.equals("Monday") && block.isEnabledMonday()) { return true;}
 		if (dayName.equals("Tuesday") && block.isEnabledTuesday()) { return true;}
 		if (dayName.equals("Wednesday") && block.isEnabledWednesday()) { return true;}
 		if (dayName.equals("Thursday") && block.isEnabledThursday()) { return true;}
 		if (dayName.equals("Friday") && block.isEnabledFriday()) { return true;}
 		if (dayName.equals("Saturday") && block.isEnabledSaturday()) { return true;}
 		return false;
 	}
 	
 	public int getDefaultRingLevel(){
 		if (relatedBlocks.size()==0){ return AudioManager.RINGER_MODE_SILENT;}
 		return relatedBlocks.get(0).getRingVal();
 	}
 	
 	public void addBlock(RingerSettingBlock block){
 		relatedBlocks.add(block);
 	}
 	
 	public void clearEntireList(){
 		relatedBlocks.clear();
 	}
 	
 	public void resetListExceptDefault(){
 		for (int i=0;i<relatedBlocks.size();i++){
 			relatedBlocks.remove(i);
 		}
 	}
 	
     
     protected void onSizeChanged(int w, int h, int oldw, int oldh) {
         if (bitmap != null) {
             bitmap.recycle();
         }
         canvas= new Canvas();
         bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
         canvas.setBitmap(bitmap);
     }
     public void destroy() {
         if (bitmap != null) {
             bitmap.recycle();
         }
     }
     public void onDraw(Canvas c) {
     	super.onDraw(c);
       //draw default on bottom
       c.drawRect(0,0,this.getWidth(),this.getHeight()-5, getPaintColorForLevel(defaultRingLevel));
       drawBlocks(c);
       Paint blue = new Paint();
       blue.setColor(Color.rgb(0,0,255));
       blue.setStrokeWidth(7L);
       blue.setTextSize((long)this.getWidth()/15);
       Paint white = new Paint();
       white.setColor(Color.rgb(255,255,255));
       white.setStrokeWidth(7L);
       white.setTextSize((long)this.getWidth()/15);
       drawMeter(c);
       c.drawText(dayName, this.getWidth()/2-(dayName.length()*4), this.getHeight()/2, white);
       
 
       //draw the bitmap to the real canvas c
       c.drawBitmap(bitmap, 
           new Rect(0,0,bitmap.getWidth(),bitmap.getHeight()), 
           new Rect(0,0,bitmap.getWidth(),bitmap.getHeight()), null);
     }
     
     public void drawBlocks(Canvas c){
     	long width = getWidth();
     	long height = getHeight();
     	double maxtime= 24*60*60*1000;
     	Iterator<RingerSettingBlock> i = relatedBlocks.iterator();
     	if (!i.hasNext()){
     		//blocks are empty. just return
     		return;
     	}
     	RingerSettingBlock block;
     	//this is the default block which we already printed;
    	block = i.next();
     	drawList = new LinkedList<VisualBlock>();
     	while (i.hasNext()){
     		block=i.next();
     		
     		long start = block.getStartTime();
     		long end = block.getEndTime();
     		int ringVal = block.getRingVal();
     		//transpose the start time accordingly
     		long transStart =(long) (width*((double)(start/maxtime)));
     		long transEnd = (long) (width* ((double)(end/maxtime)));
     		VisualBlock drawBlock= new VisualBlock(this.getContext());
     		drawList.add(drawBlock);
     		drawBlock.setAttributes(block.getId(), transStart, transEnd, height, ringVal);
     		drawBlock.draw(c);
     		//c.drawRect(transStart,0,transEnd,height-5,getPaintColorForLevel(ringVal));
     	}
     }
     
     public void drawMeter(Canvas c){
     	Paint black = new Paint();
     	black.setColor(Color.rgb(0, 0, 0));
     	black.setStrokeWidth(1L);
     	c.drawLine(this.getWidth()/2, this.getHeight()/2, this.getWidth()/2, this.getHeight(), black);
     	for (int i=1;i<9;i++){
     		c.drawLine(i*this.getWidth()/8,2*this.getHeight()/3,i*this.getWidth()/8,this.getHeight(),black);
     	}
     	for (int i=1;i<25;i++){
     		c.drawLine(i*this.getWidth()/24,5*this.getHeight()/6,i*this.getWidth()/24,this.getHeight(),black);
     	}
     }
     
     public void setDayName(String day){
     	this.dayName=day;
     }
     
     public String getDayName(){
     	return this.dayName;
     }
     
     public Paint getPaintColorForLevel(int ringLevel){
     	Paint red= new Paint();
     	red.setColor(Color.rgb(178,34,34));
     	red.setStrokeWidth(10L);
     	Paint yellow= new Paint();
     	yellow.setColor(Color.rgb(255,185,0));
     	yellow.setStrokeWidth(10L);
     	Paint green = new Paint();
     	green.setColor(Color.rgb(0, 100 ,0));
     	green.setStrokeWidth(10L);
     	if (ringLevel == AudioManager.RINGER_MODE_NORMAL){
     		return green;
     	}
     	if (ringLevel == AudioManager.RINGER_MODE_VIBRATE){
     		return yellow;
     	}
     	return red;
     }
     
     /**
 	 * Prints a logcat message with a customdebug tag
 	 * 
 	 * @param message - the message to include with the logcat packet
 	 */
     public void logcatPrint(String message){
     	Log.v("customdebug",message + " | sent from " +this.getClass().getSimpleName());
     }
     
     @Override 
     public boolean onTouchEvent(MotionEvent event){
     	int topMostId=-2;
     	switch(event.getAction())
         {
             case MotionEvent.ACTION_DOWN:
             {
                 float x = event.getX();
                 float y = event.getY();
                 
                 Iterator<VisualBlock> i = drawList.iterator();
                 while (i.hasNext()){
                 	VisualBlock vb = i.next();
                 	if (vb.contains(x,y)){
                 		topMostId = vb.getId();
                 		
                 	}
                 }
             }
         }
     	if (topMostId>=0){
     		Intent i = new Intent(this.getContext(), EditEventActivity.class);
     		Bundle params = new Bundle();
     		params.putInt("selected", topMostId);
     		i.putExtras(params);
     		((Activity)this.getContext()).startActivityForResult(i,23);
     		
     		
     		
     	}
     	logcatPrint("returned");
     	return super.onTouchEvent(event);
     }
 
     public void onActivityResult (int requestCode, int resultCode, Intent data){
     	logcatPrint("finished2");
     	this.loadApplicableBlocks(schedule);
     	this.draw(canvas);
     	logcatPrint("finished");
     }
 	
 }
