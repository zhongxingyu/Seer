 package com.multimedia.slidepuzzel.gamelogic;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.graphics.Bitmap;
 import android.graphics.Matrix;
 
 public class GameRotation extends TimerTask{
 	private int angle;
 	private Game game;
 	private Timer timer;
 	private Matrix rotateMatrix[];
 	private boolean paused;
 	private boolean changed;
 	
 	public GameRotation(Game g){
 		game = g;	
 		if(game.useRotations()){
 			angle = 0;
 			paused = false;
 			changed = false;
 			timer = new Timer();
 			timer.scheduleAtFixedRate(this, Game.ROTATE_DELAY, Game.ROTATE_DELAY);
 			rotateMatrix = new Matrix[4];
 			
 			int angle = 90;
 			for(int i = 1; i < 4; i++){
 				rotateMatrix[i] = new Matrix();
 				rotateMatrix[i].preRotate(angle);
 				angle += 90;
 			}
 		}
 	}
 	
 	public Bitmap apply(Bitmap map){
 		changed = false;
 		if(!game.useRotations() || angle == 0){
 			return map;
 		}else{
 			return Bitmap.createBitmap(map, 0, 0, map.getWidth(), map.getHeight(), rotateMatrix[angle], true);
 		}
 	}
 
 	@Override
 	public void run(){
 		if(!paused){
 			changed = true;
 			game.getSound().playSound(game.getSound().rotate);
 			angle++;
 			angle %= 4;
 		}
 	}
 	
 	public void onPause(){
 		if(timer != null && !paused){
 			timer.cancel();
 			timer.purge();
 			paused = true;
 		}
 	}
 	
 	public void onResume(){
 		if(timer != null && paused){
 			timer.scheduleAtFixedRate(this, Game.ROTATE_DELAY, Game.ROTATE_DELAY);
 			paused = false;
 		}
 	}
 	
 	public boolean rotationChanged(){
 		return changed;
 	}
 }
