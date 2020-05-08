 package com.example.ogpc2013android;
 
 import java.net.URI;
 import java.util.ArrayList;
 
import android.app.ActionBar.LayoutParams;
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Point;
 import android.graphics.drawable.AnimationDrawable;
 import android.os.Bundle;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.Display;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
import android.widget.RelativeLayout;
 
 public class LevelSelectActivity extends Activity {
 	
 	private ArrayList<AnimationDrawable> frameAnimations;
 	private ArrayList<ImageView> images;
 	private int world;
 	private final int WORLD_TUTORIAL = 0;
 	private final int WORLD_SPINE = 4;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		
 		world = WORLD_TUTORIAL;
 		DisplayMetrics displaymetrics = new DisplayMetrics();
 		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
 		int height = displaymetrics.heightPixels;
 		int width = displaymetrics.widthPixels;
 		frameAnimations = new ArrayList<AnimationDrawable>();
 		images = new ArrayList<ImageView>();
 		
 		setContentView(R.layout.activity_level_select);
 		
 		images.add((ImageView)findViewById(R.id.imageView2));
 		images.add((ImageView)findViewById(R.id.imageView3));
 		images.add((ImageView)findViewById(R.id.imageView4));
 		images.add((ImageView)findViewById(R.id.imageView5));
 		
 		for (int i = 0; i < images.size(); i++) {
 			frameAnimations.add((AnimationDrawable) getResources().getDrawable(R.drawable.level_animation));
 		}
 		
 		for (int i = 0; i < images.size(); i++) {
 			images.get(i).setBackgroundDrawable(frameAnimations.get(i));
 		}
 		
 		images.get(0).setX((108f/800f) * width);
 		images.get(0).setY((368f/600f) * height);
 		images.get(1).setX(((668f - 25f)/800f) * width);
 		images.get(1).setY(((247f - 25f)/600f) * height);
 		images.get(2).setX(((527f - 25f)/800f) * width);
 		images.get(2).setY(((453f - 40f)/600f) * height);
 		images.get(3).setX(((400f - 15f)/800f) * width);
 		images.get(3).setY(((140f - 30f)/600f) * height);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT); //The WRAP_CONTENT parameters can be replaced by an absolute width and height or the FILL_PARENT option)
		params.leftMargin = Math.round((108f/800f) * width); //Your X coordinate
		params.topMargin = Math.round((368f/600f) * height); //Your Y coordinate
		images.get(0).setLayoutParams(params);
		params.leftMargin = Math.round(((668f - 25f)/800f)); //Your X coordinate
		params.topMargin = Math.round(((247f - 25f)/600f)); //Your Y coordinate
		images.get(1).setLayoutParams(params);
		params.leftMargin = Math.round(((668f - 25f)/800f)); //Your X coordinate
		params.topMargin = Math.round(((247f - 25f)/600f)); //Your Y coordinate
		images.get(2).setLayoutParams(params);
		params.leftMargin = Math.round(((668f - 25f)/800f)); //Your X coordinate
		params.topMargin = Math.round(((247f - 25f)/600f)); //Your Y coordinate
		images.get(1).setLayoutParams(params);
 		
 		images.get(0).setOnClickListener(new Button.OnClickListener() {
 			public void onClick(View v) {
 				enterLevel(world + 0);
 			}
 		});
 		images.get(1).setOnClickListener(new Button.OnClickListener() {
 			public void onClick(View v) {
 				enterLevel(world + 1);
 			}
 		});
 		images.get(2).setOnClickListener(new Button.OnClickListener() {
 			public void onClick(View v) {
 				enterLevel(world + 2);
 			}
 		});
 		images.get(3).setOnClickListener(new Button.OnClickListener() {
 			public void onClick(View v) {
 				enterLevel(world + 3);
 			}
 		});
 	}
 	
 	public void enterLevel(int level) {
 		Intent intent = new Intent(this, FlowChartActivity.class);
 		intent.putExtra("level", level);
 		startActivity(intent);
 	}
 	
 	@Override
 	public void onWindowFocusChanged (boolean hasFocus) {
 	   super.onWindowFocusChanged(hasFocus);
 	   if (hasFocus){
 	      for (int i = 0; i < frameAnimations.size(); i++) {
 	    	  frameAnimations.get(i).start();
 	      }
 	      if (DataSingleton.currentLevel > 3) {
 	    	  //change world
 	      }
 	      for (int i = 0; i < images.size(); i++) {
 	    	  images.get(i).setVisibility(View.INVISIBLE);
 	      }
 	      for (int i = 0; i < DataSingleton.currentLevel + 1; i++) {
 	    	  images.get(i + world).setVisibility(View.VISIBLE);
 	      }
 	   }
 	}
 	
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		// TODO Auto-generated method stub
 		return super.onTouchEvent(event);
 	}
 
 }
