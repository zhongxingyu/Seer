 package com.teamluper.luper;
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.FragmentManager;
 import android.app.FragmentTransaction;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.ColorFilter;
 import android.graphics.Paint;
 import android.graphics.Path;
 import android.graphics.Path.Direction;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ImageButton;
 import android.widget.LinearLayout;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.SpinnerAdapter;
 import android.widget.TextView;
 import android.os.Bundle;
 import android.os.Environment;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.Button;
 import android.view.View;
 import android.content.Context;
 import android.content.pm.ActivityInfo;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.media.MediaRecorder;
 import android.media.MediaPlayer;
 import java.util.*;
 import java.io.IOException;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.googlecode.androidannotations.annotations.EFragment;
 import com.teamluper.luper.AudioRecorderTest.PlayButton;
 import com.teamluper.luper.AudioRecorderTest.RecordButton;
 import com.teamluper.luper.CanvasTest.GraphicsView;
 
 
 public class ExampleProject extends SherlockActivity {
 	ActionBar AB;
 	ArrayList<Clip> clips = new ArrayList<Clip>();
 	ArrayList<Track> tracks = new ArrayList<Track>();	
 
 	
 	@Override
     public void onCreate(Bundle icicle) {
         super.onCreate(icicle);
         setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
         long ID = getIntent().getExtras().getLong("com.teamluper.luper.ProjectId");
 
         AB = getSupportActionBar();
         
         final ActionBar bar = getSupportActionBar();
         bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS); // Gives us Tabs!
         
         
         /* TESTS FUNCTIONALITY OF THE COLOR CHIP RENDERING*/
         
 //        CCV = new ColorChipView(this,clip1);
 //        setContentView(CCV);
         
         
         
         /* TESTS FUNCTIONALLITY OF THE TRACK VIEW RENDERING */
           Clip clip1 = new Clip(); clip1.begin = 0; clip1.end = 500; clip1.duration = 500;
           Clip clip2 = new Clip(); clip2.begin = 250; clip2.end = 1000; clip2.duration = 650;
           Clip clip3 = new Clip(); clip3.begin = 100; clip3.end = 600; clip3.duration = 500;
 //        clips.add(clip1);
 //        TV = new TrackView(this, clips);
 //        setContentView(TV);
           
         LinearLayout base = new LinearLayout(this);
         base.setBackgroundColor(Color.parseColor("#e2dfd8"));
         
         
         base.setOrientation(LinearLayout.VERTICAL);
 
         RelativeLayout track1 = new RelativeLayout(this);
         RelativeLayout track2 = new RelativeLayout(this);
         RelativeLayout track3 = new RelativeLayout(this);
         
 //        Button chip1 = new Button(this);
 //        Button chip2 = new Button(this);
 //        Button chip3 = new Button(this);
         
         ColorChipButton chip1 = new ColorChipButton(this, clip1);
         ColorChipButton chip2 = new ColorChipButton(this, clip2);
         ColorChipButton chip3 = new ColorChipButton(this, clip3);
         
         track1.addView(chip1);
         track2.addView(chip2);
         track3.addView(chip3);        
 
 //        chip1.setX(chip1.getStartTime());
 //        chip2.setX(chip2.getStartTime());
 //        chip3.setX(chip3.getStartTime());
         
         track1.setPadding(0, 10, 0, 5);
         track2.setPadding(0, 10, 0, 5);
         track3.setPadding(0, 10, 0, 5);
         
         chip1.setBackgroundColor(Color.GRAY); 
         chip2.setBackgroundColor(Color.RED); 
         chip3.setBackgroundColor(Color.WHITE); 
         
 //        TextView track1_text = new TextView(this);
 //        track1_text.setText(" Clip 1 \n Length: " +clip1.duration+ " ms"); //FIX ME
 //        track1.addView(track1_text);
 //        TextView track2_text = new TextView(this);
 //        track2_text.setText(" Clip 2 \n Length: " +clip2.duration+ " ms"); //FIX ME
 //        track2.addView(track2_text);
 //        TextView track3_text = new TextView(this);
 //        track3_text.setText(" Clip 3 \n Length: " +clip3.duration+ " ms"); //FIX ME
 //        track3.addView(track3_text);
 //        
         /*TextView mark1 = new TextView(this);
         mark1.setText("|");
         mark1.setPadding(500, 0, 0, 0);
         base.addView(mark1);
         TextView timelinetxt = new TextView(this);
         timelinetxt.setText("500ms");
         timelinetxt.setPadding(485, 0, 0, 0);
         base.addView(timelinetxt);*/
         
 //        timelinetxt.setText(" __|__________________|___________________|___________________|___________________|__________\n" +
 //        		"     0                             250                             500                             750                             1000 ms");
 //      
 //        base.addView(timelinetxt);
         
         base.addView(track1,
                 new RelativeLayout.LayoutParams(
                         ViewGroup.LayoutParams.WRAP_CONTENT,
                         ViewGroup.LayoutParams.WRAP_CONTENT));
         base.addView(track2,
                 new RelativeLayout.LayoutParams(
                         ViewGroup.LayoutParams.WRAP_CONTENT,
                         ViewGroup.LayoutParams.WRAP_CONTENT));
         base.addView(track3,
                 new RelativeLayout.LayoutParams(
                         ViewGroup.LayoutParams.WRAP_CONTENT,
                         ViewGroup.LayoutParams.WRAP_CONTENT));
         
         setContentView(base);
         
         /*
         FragmentManager fragmentManager = getFragmentManager();
         FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
         PF = new ProjectFragment();
         fragmentTransaction.add(PF, "example_tag");
         fragmentTransaction.commit();
         */
    
     }
 	
 	// #Creates the Actionbar 
 	@Override
 	  public boolean onCreateOptionsMenu(Menu menu) {
 	    MenuInflater inf = getSupportMenuInflater();
 	    inf.inflate(R.menu.editor_bar, menu);
 	    
 	    // makes an onCLickListener for edit
	    /*final MenuItem item = menu.findItem(R.id.edit);
 	    item.getActionView().setOnClickListener(new OnClickListener() {
 	      @Override
 	      public void onClick(View v) {
 	        onOptionsItemSelected(item);
 	      }
 	    });
	    */
 	    return super.onCreateOptionsMenu(menu);
 	  }
 }
