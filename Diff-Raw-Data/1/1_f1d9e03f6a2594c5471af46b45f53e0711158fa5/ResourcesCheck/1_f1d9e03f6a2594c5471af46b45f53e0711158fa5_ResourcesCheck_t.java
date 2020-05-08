 package com.complover116.SchoolBox;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Intent;
 import android.graphics.drawable.AnimationDrawable;
 import android.os.Bundle;
 import android.os.Environment;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.ImageView;
 
 public class ResourcesCheck extends Activity {
 	static Question[] quests = new Question[10];
 	static int qnum = 2;
 	int uf = 1;
 	AlertDialog dialog;
 	long myDownloadReference;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		Intent intent = new Intent(this, MainMenu.class);
 		//Find the directory for the SD Card using the API
 		//*Don't* hardcode "/sdcard"
 		File sdcard = Environment.getExternalStorageDirectory();
 
 		//Get the text file
 		File file = new File(sdcard,"Questions.txt");
 
 		//Read text from file
 		StringBuilder text = new StringBuilder();
         int Qc = 0;
         int DataLine = 1;
         String CQText = null;
         String CQVar1 = null;
         String CQVar2 = null;
         String CQVar3 = null;
         String CQVar4 = null;
 		try {
 		    BufferedReader br = new BufferedReader(new FileReader(file));
 		    String line;
 
 		    while ((line = br.readLine()) != null) {
 		        text.append(line);
 		        text.append('\n');
 		        switch (DataLine) {
 		        case 1:
 		        CQText = line;
 		        DataLine = 2;
 		        break;
 		        case 2:
 			        CQVar1 = line;
 			        DataLine = 3;
 		        break;
 		        case 3:
 			        CQVar2 = line;
 			        DataLine = 4;
 		        break;
 		        case 4:
 			        CQVar3 = line;
 			        DataLine = 5;
 		        break;
 		        case 5:
 			        CQVar4 = line;
 			        DataLine = 6;
 		        break;
 		        case 6:
 			        DataLine = 1;
 			        Qc ++;
 			        //TODO Fix 2 appearing at CQVar3
 			        if(line.equalsIgnoreCase("1")) {
 			        MainMenu.quests[Qc] = new Question(CQText, CQVar1, CQVar2, CQVar3, CQVar4, 1);
 			        }
 			        else if(line.equalsIgnoreCase("2")) {
 			        MainMenu.quests[Qc] = new Question(CQText, CQVar1, CQVar2, CQVar3, CQVar4, 2);
 			        }
 			        else if(line.equalsIgnoreCase("3")) {
 			        MainMenu.quests[Qc] = new Question(CQText, CQVar1, CQVar2, CQVar3, CQVar4, 3);
 			        }else if(line.equalsIgnoreCase("4")) {
 			        MainMenu.quests[Qc] = new Question(CQText, CQVar1, CQVar2, CQVar3, CQVar4, 4);
 			        }else {
 			        	Log.e("InRes", "CQAns Failure");
 			        }
 		        break;
 		        default:
 		        	Log.e("InRes", "DataLine Failure");
 		        	break;
 		        }
 		    }
 		}
 		catch (IOException e) {
 		    Log.e("InRes", "Something went wrong...");
 		}
         
         MainMenu.qnum = Qc;
 		setContentView(R.layout.activity_resources_check);
 		Log.d("STATUS", "Checked resources:" + text);
 		Intent intik = new Intent(this, MainMenu.class);
 		startActivity(intik);
 		ImageView androidIV = (ImageView) findViewById(R.id.tstIV);
 		View vi = findViewById(R.id.Spinner);
 		androidIV.setBackgroundResource(R.drawable.tstanim);
 		AnimationDrawable androidAnimation = (AnimationDrawable) androidIV
 				.getBackground();
 		androidAnimation.start();
 		Log.d("STATUS", "YAY");
         
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_resources_check, menu);
 		return true;
 	}
 }
