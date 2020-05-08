 package com.itatc.clocks;
 
 import android.app.Activity;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.os.Handler;
 import android.text.format.Time;
 import android.util.Log;
 import android.widget.ImageView;
 import android.widget.TableLayout;
 import android.widget.TextView;
 
 public class MultiClockActivity extends Activity {
     /** Called when the activity is first created. */
 	private Handler h = new Handler();
 	private Time tm = new Time();
 	@Override
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.binary);
     }
     
     protected void onStart() {
 	   	super.onStart();
     	setContentView(R.layout.binary);
    	TableLayout tb = new TableLayout(this);
     	h.removeCallbacks(mUpdateBinary);
     	h.postDelayed(mUpdateBinary, 1000);
     }
 
     protected void onResume() {
     	super.onResume();
     	setContentView(R.layout.binary);
     	h.removeCallbacks(mUpdateBinary);
     	h.postDelayed(mUpdateBinary, 1000);
     }
 
     private void setBinaryClock() {
     	TableLayout tb = new TableLayout(this);
     	tm.setToNow();
     	int lc_hour = tm.hour;
     	int lc_min = tm.minute;
     	int lc_sec = tm.second;
     	ImageView iv = new ImageView(this);
     	
 		iv = (ImageView)findViewById(R.id.imageView2);
 		iv.setImageResource(R.drawable.ic_rd_regular);
 		iv = (ImageView)findViewById(R.id.ImageView01);
 		iv.setImageResource(R.drawable.ic_rd_regular);
     	
     	switch (lc_hour/10) {
     	case 1:
     		iv = (ImageView)findViewById(R.id.imageView2);
     		iv.setImageResource(R.drawable.ic_rd_selected);
     		break;
     	case 2:
     		iv = (ImageView)findViewById(R.id.ImageView01);
     		iv.setImageResource(R.drawable.ic_rd_selected);
     		break;
     	}
     	
 		iv = (ImageView)findViewById(R.id.ImageView02);
 		iv.setImageResource(R.drawable.ic_rd_regular);
 		iv = (ImageView)findViewById(R.id.ImageView03);
 		iv.setImageResource(R.drawable.ic_rd_regular);
 		iv = (ImageView)findViewById(R.id.ImageView17);
 		iv.setImageResource(R.drawable.ic_rd_regular);	
 		iv = (ImageView)findViewById(R.id.ImageView04);
 		iv.setImageResource(R.drawable.ic_rd_regular);	
 		
     	switch(lc_hour % 10) {
     	case 1:
     		iv = (ImageView)findViewById(R.id.ImageView02);
     		iv.setImageResource(R.drawable.ic_rd_selected);
     		break;    		
     	case 2:
     		iv = (ImageView)findViewById(R.id.ImageView03);
     		iv.setImageResource(R.drawable.ic_rd_selected);    		
     		break;    		
     	case 3:
     		iv = (ImageView)findViewById(R.id.ImageView02);
     		iv.setImageResource(R.drawable.ic_rd_selected);
     		iv = (ImageView)findViewById(R.id.ImageView03);
     		iv.setImageResource(R.drawable.ic_rd_selected);    		
     		break;    		
     	case 4:
     		iv = (ImageView)findViewById(R.id.ImageView17);
     		iv.setImageResource(R.drawable.ic_rd_selected);
     		break;    		
     	case 5:
     		iv = (ImageView)findViewById(R.id.ImageView02);
     		iv.setImageResource(R.drawable.ic_rd_selected);
     		iv = (ImageView)findViewById(R.id.ImageView17);
     		iv.setImageResource(R.drawable.ic_rd_selected);
     		break;    		
     	case 6:
     		iv = (ImageView)findViewById(R.id.ImageView03);
     		iv.setImageResource(R.drawable.ic_rd_selected);    		
     		iv = (ImageView)findViewById(R.id.ImageView17);
     		iv.setImageResource(R.drawable.ic_rd_selected);
     		break;    		
     	case 7:
     		iv = (ImageView)findViewById(R.id.ImageView03);
     		iv.setImageResource(R.drawable.ic_rd_selected);    		
     		iv = (ImageView)findViewById(R.id.ImageView17);
     		iv.setImageResource(R.drawable.ic_rd_selected);
     		iv = (ImageView)findViewById(R.id.ImageView02);
     		iv.setImageResource(R.drawable.ic_rd_selected);
     		break;    		
     	case 8:
     		iv = (ImageView)findViewById(R.id.ImageView04);
     		iv.setImageResource(R.drawable.ic_rd_selected);
     		break;    		
     	case 9:
     		iv = (ImageView)findViewById(R.id.ImageView04);
     		iv.setImageResource(R.drawable.ic_rd_selected);
     		iv = (ImageView)findViewById(R.id.ImageView02);
     		iv.setImageResource(R.drawable.ic_rd_selected);
     		break;    		
     	}
     	
 		iv = (ImageView)findViewById(R.id.ImageView19);
 		iv.setImageResource(R.drawable.ic_rd_regular);
 		iv = (ImageView)findViewById(R.id.ImageView06);
 		iv.setImageResource(R.drawable.ic_rd_regular);
 		iv = (ImageView)findViewById(R.id.ImageView07);
 		iv.setImageResource(R.drawable.ic_rd_regular);		
 		
     	switch(lc_min/10 ) {
     	case 1:
     		iv = (ImageView)findViewById(R.id.ImageView19);
     		iv.setImageResource(R.drawable.ic_rd_selected);
     		break;
     	case 2:
     		iv = (ImageView)findViewById(R.id.ImageView06);
     		iv.setImageResource(R.drawable.ic_rd_selected);
     		break;
     	case 3:
     		iv = (ImageView)findViewById(R.id.ImageView19);
     		iv.setImageResource(R.drawable.ic_rd_selected);
     		iv = (ImageView)findViewById(R.id.ImageView06);
     		iv.setImageResource(R.drawable.ic_rd_selected);
     		break;
     	case 4:
     		iv = (ImageView)findViewById(R.id.ImageView07);
     		iv.setImageResource(R.drawable.ic_rd_selected);
     		break;
     	case 5:
     		iv = (ImageView)findViewById(R.id.ImageView07);
     		iv.setImageResource(R.drawable.ic_rd_selected);
     		iv = (ImageView)findViewById(R.id.ImageView19);
     		iv.setImageResource(R.drawable.ic_rd_selected);
     		break;
     	}
 
 		iv = (ImageView)findViewById(R.id.ImageView08);
 		iv.setImageResource(R.drawable.ic_rd_regular);
 		iv = (ImageView)findViewById(R.id.ImageView09);
 		iv.setImageResource(R.drawable.ic_rd_regular);
 		iv = (ImageView)findViewById(R.id.ImageView10);
 		iv.setImageResource(R.drawable.ic_rd_regular);
 		iv = (ImageView)findViewById(R.id.ImageView11);
 		iv.setImageResource(R.drawable.ic_rd_regular);
     	
     	switch(lc_min % 10) {
     	case 1:
     		iv = (ImageView)findViewById(R.id.ImageView08);
     		iv.setImageResource(R.drawable.ic_rd_selected);
     		break;    		
     	case 2:
     		iv = (ImageView)findViewById(R.id.ImageView09);
     		iv.setImageResource(R.drawable.ic_rd_selected);    		
     		break;    		
     	case 3:
     		iv = (ImageView)findViewById(R.id.ImageView08);
     		iv.setImageResource(R.drawable.ic_rd_selected);
     		iv = (ImageView)findViewById(R.id.ImageView09);
     		iv.setImageResource(R.drawable.ic_rd_selected);    		
     		break;    		
     	case 4:
     		iv = (ImageView)findViewById(R.id.ImageView10);
     		iv.setImageResource(R.drawable.ic_rd_selected);
     		break;    		
     	case 5:
     		iv = (ImageView)findViewById(R.id.ImageView08);
     		iv.setImageResource(R.drawable.ic_rd_selected);
     		iv = (ImageView)findViewById(R.id.ImageView10);
     		iv.setImageResource(R.drawable.ic_rd_selected);
     		break;    		
     	case 6:
     		iv = (ImageView)findViewById(R.id.ImageView09);
     		iv.setImageResource(R.drawable.ic_rd_selected);    		
     		iv = (ImageView)findViewById(R.id.ImageView10);
     		iv.setImageResource(R.drawable.ic_rd_selected);
     		break;    		
     	case 7:
     		iv = (ImageView)findViewById(R.id.ImageView09);
     		iv.setImageResource(R.drawable.ic_rd_selected);    		
     		iv = (ImageView)findViewById(R.id.ImageView10);
     		iv.setImageResource(R.drawable.ic_rd_selected);
     		iv = (ImageView)findViewById(R.id.ImageView08);
     		iv.setImageResource(R.drawable.ic_rd_selected);
     		break;    		
     	case 8:
     		iv = (ImageView)findViewById(R.id.ImageView11);
     		iv.setImageResource(R.drawable.ic_rd_selected);
     		break;    		
     	case 9:
     		iv = (ImageView)findViewById(R.id.ImageView11);
     		iv.setImageResource(R.drawable.ic_rd_selected);
     		iv = (ImageView)findViewById(R.id.ImageView08);
     		iv.setImageResource(R.drawable.ic_rd_selected);
     		break;    		
     	}
     	
 		iv = (ImageView)findViewById(R.id.ImageView05);
 		iv.setImageResource(R.drawable.ic_rd_regular);
 		iv = (ImageView)findViewById(R.id.ImageView13);
 		iv.setImageResource(R.drawable.ic_rd_regular);
 		iv = (ImageView)findViewById(R.id.ImageView14);
 		iv.setImageResource(R.drawable.ic_rd_regular);	
     	
     	
     	switch(lc_sec/10 ) {
     	case 1:
     		iv = (ImageView)findViewById(R.id.ImageView05);
     		iv.setImageResource(R.drawable.ic_rd_selected);
     		break;
     	case 2:
     		iv = (ImageView)findViewById(R.id.ImageView13);
     		iv.setImageResource(R.drawable.ic_rd_selected);
     		break;
     	case 3:
     		iv = (ImageView)findViewById(R.id.ImageView05);
     		iv.setImageResource(R.drawable.ic_rd_selected);
     		iv = (ImageView)findViewById(R.id.ImageView13);
     		iv.setImageResource(R.drawable.ic_rd_selected);
     		break;
     	case 4:
     		iv = (ImageView)findViewById(R.id.ImageView14);
     		iv.setImageResource(R.drawable.ic_rd_selected);
     		break;
     	case 5:
     		iv = (ImageView)findViewById(R.id.ImageView14);
     		iv.setImageResource(R.drawable.ic_rd_selected);
     		iv = (ImageView)findViewById(R.id.ImageView05);
     		iv.setImageResource(R.drawable.ic_rd_selected);
     		break;
     	}
     	
 		iv = (ImageView)findViewById(R.id.ImageView20);
 		iv.setImageResource(R.drawable.ic_rd_regular);
 		iv = (ImageView)findViewById(R.id.ImageView16);
 		iv.setImageResource(R.drawable.ic_rd_regular);
 		iv = (ImageView)findViewById(R.id.ImageView18);
 		iv.setImageResource(R.drawable.ic_rd_regular);
 		iv = (ImageView)findViewById(R.id.ImageView15);
 		iv.setImageResource(R.drawable.ic_rd_regular);
     	
     	switch(lc_sec % 10) {
     	case 1:
     		iv = (ImageView)findViewById(R.id.ImageView20);
     		iv.setImageResource(R.drawable.ic_rd_selected);
     		break;    		
     	case 2:
     		iv = (ImageView)findViewById(R.id.ImageView16);
     		iv.setImageResource(R.drawable.ic_rd_selected);    		
     		break;    		
     	case 3:
     		iv = (ImageView)findViewById(R.id.ImageView20);
     		iv.setImageResource(R.drawable.ic_rd_selected);
     		iv = (ImageView)findViewById(R.id.ImageView16);
     		iv.setImageResource(R.drawable.ic_rd_selected);    		
     		break;    		
     	case 4:
     		iv = (ImageView)findViewById(R.id.ImageView18);
     		iv.setImageResource(R.drawable.ic_rd_selected);
     		break;    		
     	case 5:
     		iv = (ImageView)findViewById(R.id.ImageView18);
     		iv.setImageResource(R.drawable.ic_rd_selected);
     		iv = (ImageView)findViewById(R.id.ImageView20);
     		iv.setImageResource(R.drawable.ic_rd_selected);
     		break;    		
     	case 6:
     		iv = (ImageView)findViewById(R.id.ImageView16);
     		iv.setImageResource(R.drawable.ic_rd_selected);    		
     		iv = (ImageView)findViewById(R.id.ImageView18);
     		iv.setImageResource(R.drawable.ic_rd_selected);
     		break;    		
     	case 7:
     		iv = (ImageView)findViewById(R.id.ImageView18);
     		iv.setImageResource(R.drawable.ic_rd_selected);    		
     		iv = (ImageView)findViewById(R.id.ImageView16);
     		iv.setImageResource(R.drawable.ic_rd_selected);
     		iv = (ImageView)findViewById(R.id.ImageView20);
     		iv.setImageResource(R.drawable.ic_rd_selected);
     		break;    		
     	case 8:
     		iv = (ImageView)findViewById(R.id.ImageView15);
     		iv.setImageResource(R.drawable.ic_rd_selected);
     		break;    		
     	case 9:
     		iv = (ImageView)findViewById(R.id.ImageView15);
     		iv.setImageResource(R.drawable.ic_rd_selected);
     		iv = (ImageView)findViewById(R.id.ImageView20);
     		iv.setImageResource(R.drawable.ic_rd_selected);
     		break;    		
     	}
     	tb.postInvalidate();
     }
     
     private void setClockColor(){
     	tm.setToNow();
     	int lc_hour = tm.hour; 
     	int lc_offset = tm.minute;
     	TextView tx = new TextView(this);
     	tx = (TextView)findViewById(R.id.textView1);
     	tx.setTextColor(Color.argb(255, 255, 255, 255));
     	tx = (TextView)findViewById(R.id.textView1_3);
     	tx.setTextColor(Color.argb(255, 255, 255, 255));
     	tx = (TextView)findViewById(R.id.tv_8_3);
     	tx.setTextColor(Color.argb(255, 255, 255, 255));    	
     	if(lc_offset < 35)
     	{
         	tx = (TextView)findViewById(R.id.tv_4_1);
         	tx.setTextColor(Color.argb(255, 17, 17, 17));
     		tx = (TextView)findViewById(R.id.tv_3_3);
         	tx.setTextColor(Color.argb(255,255,255,255));    	
     	}
     	else
     	{
         	tx = (TextView)findViewById(R.id.tv_3_3);
         	tx.setTextColor(Color.argb(255, 17, 17, 17));
     		tx = (TextView)findViewById(R.id.tv_4_1);
         	tx.setTextColor(Color.argb(255, 255, 255, 255));
         	lc_hour += 1;
     	}
     	
     	if(lc_hour > 12)
     		lc_hour -= 12;
     	
     	switch(lc_offset / 5)
     	{
     	case 0: // < 5 minutes
     		tx = (TextView)findViewById(R.id.tv_3_1);
         	tx.setTextColor(Color.argb(255, 17, 17, 17));
     		break;
     	case 1: //5-10 minutes
     		tx = (TextView)findViewById(R.id.tv_3_1);
         	tx.setTextColor(Color.argb(255, 255, 255, 255));
     		break;
     	case 2: //10-15 minutes
     		tx = (TextView)findViewById(R.id.tv_3_1);
         	tx.setTextColor(Color.argb(255, 17, 17, 17));
     		tx = (TextView)findViewById(R.id.textView1_5);
         	tx.setTextColor(Color.argb(255, 255, 255, 255));
     		break;
     	case 3: //15-20 minutes
     		tx = (TextView)findViewById(R.id.textView1_5);
         	tx.setTextColor(Color.argb(255, 17, 17, 17));
     		tx = (TextView)findViewById(R.id.tv_2_1);
         	tx.setTextColor(Color.argb(255, 255, 255, 255));
     		break;
     	case 4: //20-25 minutes
     		tx = (TextView)findViewById(R.id.tv_2_1);
         	tx.setTextColor(Color.argb(255, 17, 17, 17));
     		tx = (TextView)findViewById(R.id.tv_2_2);
         	tx.setTextColor(Color.argb(255, 255, 255, 255));
     		break;
     	case 5: // 25-30 minutes
     		tx = (TextView)findViewById(R.id.tv_2_2);
         	tx.setTextColor(Color.argb(255, 255, 255, 255));
     		tx = (TextView)findViewById(R.id.tv_3_1);
         	tx.setTextColor(Color.argb(255, 255, 255, 255));
     		break;
     	case 6: //30-35 minutes
     		tx = (TextView)findViewById(R.id.tv_3_1);
         	tx.setTextColor(Color.argb(255, 17, 17, 17));
     		tx = (TextView)findViewById(R.id.tv_2_2);
         	tx.setTextColor(Color.argb(255, 17, 17, 17));
         	tx = (TextView)findViewById(R.id.textView1_6);
         	tx.setTextColor(Color.argb(255, 255, 255, 255));
     		break;
     	case 7: //35-40 minutes
     		tx = (TextView)findViewById(R.id.textView1_6);
         	tx.setTextColor(Color.argb(255, 17, 17, 17));
     		tx = (TextView)findViewById(R.id.tv_2_2);
         	tx.setTextColor(Color.argb(255, 255, 255, 255));
     		tx = (TextView)findViewById(R.id.tv_3_1);
         	tx.setTextColor(Color.argb(255, 255, 255, 255));
     		break;
     	case 8: //40-45 minutes
     		tx = (TextView)findViewById(R.id.tv_3_1);
         	tx.setTextColor(Color.argb(255, 17, 17, 17));
     		tx = (TextView)findViewById(R.id.tv_2_2);
         	tx.setTextColor(Color.argb(255, 255, 255, 255));
         	break;
     	case 9: //45-50 minutes
     		tx = (TextView)findViewById(R.id.tv_2_2);
         	tx.setTextColor(Color.argb(255, 17, 17, 17));
         	tx = (TextView)findViewById(R.id.textView1_6);
         	tx.setTextColor(Color.argb(255, 255, 255, 255));
     		break;
     	case 10: //50-55 minutes
     		tx = (TextView)findViewById(R.id.textView1_6);
         	tx.setTextColor(Color.argb(255, 17, 17, 17));
     		tx = (TextView)findViewById(R.id.textView1_5);
         	tx.setTextColor(Color.argb(255, 255, 255, 255));
     		break;
     	case 11: //55-60 minutes
     		tx = (TextView)findViewById(R.id.textView1_5);
         	tx.setTextColor(Color.argb(255, 17, 17, 17));
     		tx = (TextView)findViewById(R.id.tv_3_1);
         	tx.setTextColor(Color.argb(255, 255, 255, 255));
         	break;
         
     	}
     	switch(lc_hour)
     	{
     	case 1:
     		tx = (TextView)findViewById(R.id.tv_8_1);
         	tx.setTextColor(Color.argb(255, 17, 17, 17));
     		tx = (TextView)findViewById(R.id.tv_4_3);
         	tx.setTextColor(Color.argb(255, 255, 255, 255));
         	break;
     	case 2:
     		tx = (TextView)findViewById(R.id.tv_4_3);
         	tx.setTextColor(Color.argb(255, 17, 17, 17));
     		tx = (TextView)findViewById(R.id.tv_4_5);
         	tx.setTextColor(Color.argb(255, 255, 255, 255));
         	break;
     	case 3:
     		tx = (TextView)findViewById(R.id.tv_4_5);
         	tx.setTextColor(Color.argb(255, 17, 17, 17));
     		tx = (TextView)findViewById(R.id.tv_5_1);
         	tx.setTextColor(Color.argb(255, 255, 255, 255));
         	break;
     	case 4:
     		tx = (TextView)findViewById(R.id.tv_5_1);
         	tx.setTextColor(Color.argb(255, 17, 17, 17));
     		tx = (TextView)findViewById(R.id.tv_5_2);
         	tx.setTextColor(Color.argb(255, 255, 255, 255));
     		break;
     	case 5:
     		tx = (TextView)findViewById(R.id.tv_5_3);
         	tx.setTextColor(Color.argb(255, 17, 17, 17));
     		tx = (TextView)findViewById(R.id.tv_5_3);
         	tx.setTextColor(Color.argb(255, 255, 255, 255));
         	break;
     	case 6:
     		tx = (TextView)findViewById(R.id.tv_5_3);
         	tx.setTextColor(Color.argb(255, 17, 17, 17));
     		tx = (TextView)findViewById(R.id.tv_6_1);
         	tx.setTextColor(Color.argb(255, 255, 255, 255));
         	break;
     	case 7:
     		tx = (TextView)findViewById(R.id.tv_6_1);
         	tx.setTextColor(Color.argb(255, 17, 17, 17));
     		tx = (TextView)findViewById(R.id.tv_6_2);
         	tx.setTextColor(Color.argb(255, 255, 255, 255));
         	break;
     	case 8:
     		tx = (TextView)findViewById(R.id.tv_6_2);
         	tx.setTextColor(Color.argb(255, 17, 17, 17));
     		tx = (TextView)findViewById(R.id.tv_6_3);
         	tx.setTextColor(Color.argb(255, 255, 255, 255));
         	break;
     	case 9:
     		tx = (TextView)findViewById(R.id.tv_6_3);
         	tx.setTextColor(Color.argb(255, 17, 17, 17));
     		tx = (TextView)findViewById(R.id.tv_7_1);
         	tx.setTextColor(Color.argb(255, 255, 255, 255));
         	break;
     	case 10:
     		tx = (TextView)findViewById(R.id.tv_7_1);
         	tx.setTextColor(Color.argb(255, 17, 17, 17));
     		tx = (TextView)findViewById(R.id.tv_7_2);
         	tx.setTextColor(Color.argb(255, 255, 255, 255));
         	break;
     	case 11:
     		tx = (TextView)findViewById(R.id.tv_7_2);
         	tx.setTextColor(Color.argb(255, 17, 17, 17));
     		tx = (TextView)findViewById(R.id.tv_7_3);
         	tx.setTextColor(Color.argb(255, 255, 255, 255));
         	break;
     	case 12:
     		tx = (TextView)findViewById(R.id.tv_7_3);
         	tx.setTextColor(Color.argb(255, 17, 17, 17));
     		tx = (TextView)findViewById(R.id.tv_8_1);
         	tx.setTextColor(Color.argb(255, 255, 255, 255));
         	break;
     	}
     }
    
     private Runnable mUpdateClock = new Runnable() { 
     	public void run() {
     		tm.setToNow();
     		TextView tx = (TextView)findViewById(R.id.tv_showtime);
     		tx.setText(tm.format("%a %I:%M:%S"));
     		setClockColor();
     		h.removeCallbacks(mUpdateClock);
         	h.postDelayed(mUpdateClock, 60000);
     	}
     };
     
     private Runnable mUpdateBinary = new Runnable() {
     	public void run() {
     		setBinaryClock();
     		h.removeCallbacks(this);
     		h.postDelayed(this, 1000);
     	}
     };
 }
