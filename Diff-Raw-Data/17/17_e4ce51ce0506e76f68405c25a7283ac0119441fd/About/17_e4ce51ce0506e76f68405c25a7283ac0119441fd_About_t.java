 package edu.vanderbilt.vm.guide.ui;
 
import edu.vanderbilt.vm.guide.R;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Gravity;
 import android.widget.TextView;
 
 public class About extends Activity{
 	
 	public void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);
 		
		//TextView tvAbout = new TextView(this);
		//tvAbout.setText("Source code is available on " + 
	//			"https://github.com/VandyMobile/guide-android");
	//	tvAbout.setTextSize(18f);
	//	tvAbout.setGravity(Gravity.CENTER);
	//	this.setContentView(tvAbout);
		
		setContentView(R.layout.webmap);
		
 	}
 	
 	public static void open(Context ctx){
 		Intent i = new Intent(ctx, About.class);
 		ctx.startActivity(i);
 	}
 }
