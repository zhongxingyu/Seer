 package com.img.jk.beethelion;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 
 
 
 import android.app.Activity;
 import android.media.MediaPlayer;
 import android.media.MediaPlayer.OnCompletionListener;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.Toast;
 
 public class ShowTakenPhoto extends Activity implements OnClickListener,
 		OnCompletionListener {
 	
 	GlobalVar gbV;
 	MediaPlayer mediaPlayerBB;
 	MediaPlayer mediaPlayerBee;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.showtakenphoto);
 		
 		// Connect all objects in layout.
 		ImageView takenPhotoView = (ImageView)findViewById(R.id.takenPhotoView);
 		gbV = (GlobalVar)getApplicationContext();
 		takenPhotoView.setImageBitmap(gbV.getBmpPhoto());
 		ImageButton acceptBtn = (ImageButton)findViewById(R.id.acceptBtn);
 		acceptBtn.setOnClickListener(this);
 		ImageButton rejectBtn = (ImageButton)findViewById(R.id.rejectBtn);
 		rejectBtn.setOnClickListener(this);
 	}
 
 	@Override
 	public void onClick(View v) {
 		switch(v.getId()) {
 		case R.id.acceptBtn:
 			// Play sound effect click.
 			mediaPlayerBB = MediaPlayer.create(this, R.raw.blackberry);
 			mediaPlayerBB.start();
 			mediaPlayerBee = MediaPlayer.create(this, R.raw.bee);
 			mediaPlayerBee.setLooping(true);
 			mediaPlayerBee.start();
 			
 			Toast.makeText(this, "Call matching module.", Toast.LENGTH_SHORT).show();
 			long timerStart,timerStop;
 			timerStart = System.currentTimeMillis();
 			gbV.setStrFNameResultMatching(MatchingLib.jkMatching(gbV.getStrBeeDir()));
 			timerStop = System.currentTimeMillis();
 			
 			mediaPlayerBee.stop();
 			mediaPlayerBee.release();
 			
 			Toast.makeText(this, String.format("Used time = %f sec",
 					(timerStop - timerStart)/1000.0), Toast.LENGTH_LONG).show();
 			
 			// Read rank photo and give it to vector.
 			File rankFile = new File(gbV.getStrFNameResultMatching());
 			FileReader fr = null;
 			try {
 				fr = new FileReader(rankFile);
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 			}
 			BufferedReader br = new BufferedReader(fr);
 			String line;
 			try {
 				while((line = br.readLine()) != null)
 				{
 					// Call function to create unique rank of species.
 					gbV.addFlowerRank(line);
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			finally {
 				if(br != null)
 				try {
 					br.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 
 			// How to get the flower rank.
 			// An example code for Karn.
			for(String strFlower:gbV.flowerRank) {
 				Integer i = Integer.valueOf(strFlower);
 				strFlower = strFlower.concat("_1.jpg");
 				Toast.makeText(this, i.toString() + "&" + strFlower.toString(),
 						Toast.LENGTH_SHORT).show();
			}
 			
			//show matching flower
 //			startActivity(new Intent(
 //	                this,
 //	                GardenActivity.class));
 			break;
 			
 		case R.id.rejectBtn:
 			// Play sound effect click.
 			mediaPlayerBB = MediaPlayer.create(this, R.raw.blackberry);
 			mediaPlayerBB.start();
 			
 			this.finish();
 			break;
 			
 		default:
 			break;
 		}
 	}
 
 	@Override
 	public void onCompletion(MediaPlayer mp) {
 //		How to distinct each mediaPlayer?
 //		if(mp == mediaPlayerBB) {
 			mediaPlayerBB.stop();
 			mediaPlayerBB.release();
 //		}		
 	}
 }
