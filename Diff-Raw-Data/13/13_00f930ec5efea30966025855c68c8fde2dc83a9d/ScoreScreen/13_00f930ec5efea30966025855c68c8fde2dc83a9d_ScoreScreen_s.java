 package edu.elon.cs.squirrelgame;
 import edu.cs.elon.squirrelstale.R;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Rect;
 import android.graphics.drawable.BitmapDrawable;
 
 
 public class ScoreScreen {
	private Bitmap goldTime;
	private Bitmap silverTime;
	private Bitmap bronzeTime;
 	private Bitmap chosen; 
 	private double time = 0; 
 	protected boolean display = false; 
 	private final int DISPLAY_TIME = 10; 
 	private Context context; 
 	
 	public ScoreScreen(Context context){
 		this.context = context; 
 	}
 	
 	public void doDraw(Canvas canvas){
 		canvas.drawBitmap(chosen, null, new Rect(0,0,canvas.getWidth(),canvas.getHeight()), null);  
 	}
 	
 	public void displayScreen(){
 		display = true; 
 	}
 	
 	public void setScore(String score){
		this.goldTime = BitmapFactory.decodeResource(context.getResources(), R.drawable.gold); 
		this.silverTime = BitmapFactory.decodeResource(context.getResources(), R.drawable.silver); 
		this.bronzeTime = BitmapFactory.decodeResource(context.getResources(), R.drawable.bronze); 
 		if(score.trim().equals("Gold")){
 			chosen = goldTime; 
 		}
 		if(score.trim().equals("Silver")){
 			chosen = silverTime; 
 		}
 		if(score.trim().equals("Bronze")){
 			chosen = bronzeTime; 
 		}
 	}
 	
 	public void update(double elapsed){
 		time += elapsed; 
 		if( time >= DISPLAY_TIME){
 			display = false; 
 			//finished 
 		}
 	}
 
 }
