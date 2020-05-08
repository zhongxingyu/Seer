 package com.zhideel.tapathon.ui;
 
import com.zhideel.tapathon.ConnectionManager;
import com.zhideel.tapathon.R;
import com.zhideel.tapathon.R.id;
import com.zhideel.tapathon.R.layout;

 import android.app.Activity;
 import android.app.Fragment;
 import android.app.FragmentTransaction;
 import android.os.Bundle;
 import android.view.View;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
public class GameMenuActivity extends Activity {
 
 	public static final String TAG = "Tapathon";
 	
     private Button btnStart;
     private ConnectionManager cm;
     private boolean bStarted = false;
     private TextView tvName;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         setContentView(R.layout.activity_main);
         cm = new ConnectionManager(this);
         btnStart = (Button) findViewById(R.id.btn_start);
         tvName = (TextView) findViewById(R.id.tv_name);
         btnStart.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 if (!bStarted) {
                     //mLogView.appendLog("\n[B] Start Chord!");
                     cm.startChord();
                     
                     FragmentTransaction dFrag = getFragmentManager().beginTransaction();
                     Fragment prev = getFragmentManager().findFragmentByTag("dialog_channel");
                     if (prev != null) {
             	    	dFrag.remove(prev);
             	    }
             	    dFrag.addToBackStack(null);
                     GameChannelFragment mFragment = new GameChannelFragment();
                     mFragment.show(getFragmentManager(), "dialog_channel");
                     dFrag.commit();
                     
                     Toast.makeText(getBaseContext(), "Start", Toast.LENGTH_SHORT).show();
                 } else {
                     //mLogView.appendLog("\n[C] Stop Chord!");
                     cm.stopChord();
                     Toast.makeText(getBaseContext(), "Stop", Toast.LENGTH_SHORT).show();
                 }
             }
         });
     }
 
     @Override
     public void onResume() {
         super.onResume();
         if (cm.isNotInit()) {
             //mLogView.appendLog("\n[A] Initialize Chord!");
             cm.initChord();
         }
     }
 
     @Override
     public void onDestroy() {
         if (!cm.isNotInit()) {
             cm.destroy();
         }
         super.onDestroy();
     }
 }
