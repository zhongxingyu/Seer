 package edu.sru.andgate.bitbot;
 
 import edu.sru.andgate.bitbot.graphics.GameEngine;
 import edu.sru.andgate.bitbot.ide.IDE;
 import edu.sru.andgate.bitbot.interpreter.Test;
 import edu.sru.andgate.bitbot.tutorial.Tutorial_List;
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Matrix;
 import android.graphics.drawable.BitmapDrawable;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageView;
 
 public class MainMenu extends Activity {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
      
         final ImageView bot_turret = (ImageView) findViewById(R.id.bot_turret);
         
         Button game_modes = (Button) findViewById(R.id.game_modes);
		game_modes.setOnClickListener(new View.OnClickListener() 
 		{
 			@Override
 			public void onClick(View v) 
 			{
 				rotateImage(bot_turret, R.drawable.mainturret, R.id.bot_turret, 0);
 				//open graphics for now
 				Intent engineIntent = new Intent(MainMenu.this, GameEngine.class);
 				startActivity(engineIntent);
 			}
 		});
 		
 		Button tutorial_btn = (Button) findViewById(R.id.tutorial_btn);
 		tutorial_btn.setOnClickListener(new View.OnClickListener() 
 		{
 			@Override
 			public void onClick(View v) 
 			{
 				rotateImage(bot_turret, R.drawable.mainturret, R.id.bot_turret, 60);
 				Intent engineIntent = new Intent(MainMenu.this, Tutorial_List.class);
 				startActivity(engineIntent);
 			}
 		});
         
         Button options_btn = (Button) findViewById(R.id.options_btn);
         options_btn.setOnClickListener(new View.OnClickListener() 
 		{
 			@Override
 			public void onClick(View v) 
 			{
 				rotateImage(bot_turret, R.drawable.mainturret, R.id.bot_turret, 120);
 				//open interpreter for now
 				Intent engineIntent = new Intent(MainMenu.this, Test.class);
 				startActivity(engineIntent);
 			}
 		});
         
         Button scores_btn = (Button) findViewById(R.id.scores_btn);
         scores_btn.setOnClickListener(new View.OnClickListener() 
 		{
 			@Override
 			public void onClick(View v) 
 			{
 				rotateImage(bot_turret, R.drawable.mainturret, R.id.bot_turret, -60);
 				
 			}
 		});
         
         Button puzzle_btn = (Button) findViewById(R.id.puzzle_btn);
         puzzle_btn.setOnClickListener(new View.OnClickListener() 
 		{
 			@Override
 			public void onClick(View v) 
 			{
 				rotateImage(bot_turret, R.drawable.mainturret, R.id.bot_turret, -120);
 				//open up IDE for now
 				Intent engineIntent = new Intent(MainMenu.this, IDE.class);
 				startActivity(engineIntent);
 			}
 		});
         
         Button quit_btn = (Button) findViewById(R.id.quit_btn);
         quit_btn.setOnClickListener(new View.OnClickListener() 
 		{
 			@Override
 			public void onClick(View v) 
 			{
 				rotateImage(bot_turret, R.drawable.mainturret, R.id.bot_turret, 180);
 				finish();
 			}
 		});    
     }
     
     public void rotateImage(ImageView img, int turret_id, int img_id, int rotate){
     	img=(ImageView)findViewById(img_id);
     	Bitmap bmp = BitmapFactory.decodeResource(getResources(), turret_id);
     	// Getting width & height of the given image.
     	int w = bmp.getWidth();
     	int h = bmp.getHeight();
     	// Setting post rotate to 90
     	Matrix mtx = new Matrix();
     	mtx.postRotate(rotate);
     	// Rotating Bitmap
     	Bitmap rotatedBMP = Bitmap.createBitmap(bmp, 0, 0, w, h, mtx, true);
     	BitmapDrawable bmd = new BitmapDrawable(rotatedBMP);
     	img.setImageDrawable(bmd);
     }
 }
