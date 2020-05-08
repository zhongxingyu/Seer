 package org.cortelyoucollective.notifyme;
 
 import java.util.Calendar;
 import org.cortelyoucollective.notifyme.R;
 
 import android.content.Context;
 import android.graphics.Typeface;
 import android.os.Handler;
 import android.os.SystemClock;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class TimeChooser extends LinearLayout{
 	
 	private int mHour;
     private int mMinute;
     private Button hrPlusBtn;
     private Button hrMinusBtn;
     private Button minPlusBtn;
     private Button minMinusBtn;
     private Button ampmPlusBtn;
     private Button ampmMinusBtn;
     private TextView hrTxt;
     private TextView dotsTxt;
     private TextView minTxt;
     private TextView ampmTxt;
     
     private int theHour;
     private int theMinute;
     private boolean am;
     private Handler mHandler = new Handler(); 
 	
 	public TimeChooser(Context context) {
 		super(context);
 	}
 	
 	public TimeChooser(Context context, AttributeSet attrs) {
         super(context, attrs);
     }
 
 	public void initComponent(Context context) {
 
          LayoutInflater inflater = LayoutInflater.from(context);
          View v = inflater.inflate(R.layout.timechooser, null, false);
          this.addView(v);
          
          hrPlusBtn = (Button) findViewById(R.id.hr_plus);
          hrMinusBtn = (Button) findViewById(R.id.hr_minus);
          minPlusBtn = (Button) findViewById(R.id.min_plus);
          minMinusBtn = (Button) findViewById(R.id.min_minus);
          ampmPlusBtn = (Button) findViewById(R.id.ampm_plus);
          ampmMinusBtn = (Button) findViewById(R.id.ampm_minus);
          
          hrTxt = (TextView) findViewById(R.id.hr_txt);
          dotsTxt = (TextView) findViewById(R.id.dots);
          minTxt = (TextView) findViewById(R.id.min_txt);
          ampmTxt = (TextView) findViewById(R.id.ampm_txt);
          
          theHour = mHour = Calendar.getInstance().get(Calendar.HOUR);
          //Toast.makeText(context, String.valueOf(Calendar.getInstance().get(Calendar.HOUR)), Toast.LENGTH_LONG).show();
          theMinute = mMinute = Calendar.getInstance().get(Calendar.MINUTE);
          
          if(theHour == 0)
         	 theHour = mHour = 12;
          
          if(theHour < 10)
 				hrTxt.setText("0"+String.valueOf(theHour));
 			 else 
 				hrTxt.setText(String.valueOf(theHour));
 
          if(theMinute < 10)
 				minTxt.setText("0" + String.valueOf(theMinute));
 			 else 
 				minTxt.setText(String.valueOf(theMinute));
          
          Typeface font = Typeface.createFromAsset(context.getAssets(), "fonts/VarelaRound-Regular.ttf");
          hrTxt.setTypeface(font);
          minTxt.setTypeface(font);
          ampmTxt.setTypeface(font);
          dotsTxt.setTypeface(font);
          
          if (Calendar.getInstance().get(Calendar.AM_PM) == Calendar.AM)
          {
         	 am = true;
         	 ampmTxt.setText("AM");
          } else {
         	 am = false;
         	 ampmTxt.setText("PM");
         	 mHour = theHour+12;
          }
          
          final Runnable increaseHr = new Runnable() 
          { 
              public void run() 
              { 
             	if(theHour < 12)
  				{
  					theHour++;
  				} else {
  					theHour = 1;
  				}
  				if(theHour < 10)
  				{
  					hrTxt.setText("0"+String.valueOf(theHour));
  				} else {
  					hrTxt.setText(String.valueOf(theHour));
  				}
  				if(am)
  				{
  					mHour = theHour;
  				} else {
  					mHour = theHour+12;
  				} 
                  mHandler.postAtTime(this, SystemClock.uptimeMillis() + 100); 
              } 
          };
          
          final Runnable decreaseHr = new Runnable() 
          { 
              public void run() 
              { 
             	 if(theHour > 1)
   				{
   					theHour--;
   				} else {
   					theHour = 12;
   				}
   				if(theHour < 10)
   				{
   					hrTxt.setText("0"+String.valueOf(theHour));
   				} else {
   					hrTxt.setText(String.valueOf(theHour));
   				}
   				if(am)
  				{
  					mHour = theHour;
  				} else {
  					mHour = theHour+12;
  				}
                  mHandler.postAtTime(this, SystemClock.uptimeMillis() + 100); 
              } 
          };
          
          final Runnable increaseMin = new Runnable() 
          { 
              public void run() 
              { 
             	 if(theMinute < 59)
   				{
   					theMinute++;
   				} else {
   					theMinute = 0;
   				}
   				if(theMinute < 10)
   				{
   					minTxt.setText("0" + String.valueOf(theMinute));
   				} else {
   					minTxt.setText(String.valueOf(theMinute));
   				}
   				mMinute = theMinute;
                  mHandler.postAtTime(this, SystemClock.uptimeMillis() + 100); 
              } 
          };
          
          final Runnable decreaseMin = new Runnable() 
          { 
              public void run() 
              { 
             	 if(theMinute > 0)
    				{
    					theMinute--;
    				} else {
    					theMinute = 59;
    				}
    				if(theMinute < 10)
    				{
    					minTxt.setText("0" + String.valueOf(theMinute));
    				} else {
    					minTxt.setText(String.valueOf(theMinute));
    				}
    				mMinute = theMinute; 
                  mHandler.postAtTime(this, SystemClock.uptimeMillis() + 100); 
              } 
          };
          
          hrPlusBtn.setOnTouchListener(new View.OnTouchListener() {
 			public boolean onTouch(View v, MotionEvent event) {
 				int action = event.getAction();
                 if (action == MotionEvent.ACTION_DOWN) 
                 { 
                     Log.i("repeatBtn", "MotionEvent.ACTION_DOWN"); 
                     mHandler.removeCallbacks(increaseHr); 
                     mHandler.postAtTime(increaseHr, SystemClock.uptimeMillis() + 100); 
                 } 
                 else if (action == MotionEvent.ACTION_UP) 
                 { 
                     Log.i("repeatBtn", "MotionEvent.ACTION_UP"); 
                     mHandler.removeCallbacks(increaseHr); 
                 } 
 				return false;
 			}
 		});
 
          
          hrMinusBtn.setOnTouchListener(new View.OnTouchListener() {
 			public boolean onTouch(View v, MotionEvent event) {
 				int action = event.getAction();
                 if (action == MotionEvent.ACTION_DOWN) 
                 { 
                     Log.i("repeatBtn", "MotionEvent.ACTION_DOWN"); 
                     mHandler.removeCallbacks(decreaseHr); 
                     mHandler.postAtTime(decreaseHr, SystemClock.uptimeMillis() + 100); 
                 } 
                 else if (action == MotionEvent.ACTION_UP) 
                 { 
                     Log.i("repeatBtn", "MotionEvent.ACTION_UP"); 
                     mHandler.removeCallbacks(decreaseHr); 
                 } 
 				return false;
 			}
 		});
          
          minPlusBtn.setOnTouchListener(new View.OnTouchListener() {
 			public boolean onTouch(View v, MotionEvent event) {
 				int action = event.getAction();
                 if (action == MotionEvent.ACTION_DOWN) 
                 { 
                     Log.i("repeatBtn", "MotionEvent.ACTION_DOWN"); 
                     mHandler.removeCallbacks(increaseMin); 
                     mHandler.postAtTime(increaseMin, SystemClock.uptimeMillis() + 100); 
                 } 
                 else if (action == MotionEvent.ACTION_UP) 
                 { 
                     Log.i("repeatBtn", "MotionEvent.ACTION_UP"); 
                     mHandler.removeCallbacks(increaseMin); 
                 } 
 				return false;
 			}
 		});
          
          minMinusBtn.setOnTouchListener(new View.OnTouchListener() {
 			public boolean onTouch(View v, MotionEvent event) {
 				int action = event.getAction();
                 if (action == MotionEvent.ACTION_DOWN) 
                 { 
                     Log.i("repeatBtn", "MotionEvent.ACTION_DOWN"); 
                     mHandler.removeCallbacks(decreaseMin); 
                     mHandler.postAtTime(decreaseMin, SystemClock.uptimeMillis() + 100); 
                 } 
                 else if (action == MotionEvent.ACTION_UP) 
                 { 
                     Log.i("repeatBtn", "MotionEvent.ACTION_UP"); 
                     mHandler.removeCallbacks(decreaseMin); 
                 } 
 				return false;
 			}
 		});
          
          ampmPlusBtn.setOnClickListener(new View.OnClickListener()
          {
 
 			public void onClick(View v) {
 				if(am)
 				{
 					am = false;
 					ampmTxt.setText("PM");
 					mHour = mHour + 12;
 				} else {
 					am = true;
 					ampmTxt.setText("AM");
 					mHour = mHour - 12;
 				}
 				
 			}
         	 
          });
          
          ampmMinusBtn.setOnClickListener(new View.OnClickListener()
          {
 
 			public void onClick(View v) {
 				if(am)
 				{
 					am = false;
 					ampmTxt.setText("PM");
 					mHour = mHour + 12;
 				} else {
 					am = true;
 					ampmTxt.setText("AM");
 					mHour = mHour - 12;
 				}
 				
 			}
         	 
          });
     }
     
     public void setCurrentHour(int currentHr)
     {
     	if(currentHr > 12)
     	{
     		hrTxt.setText(String.valueOf(currentHr-12));
     		if(hrTxt.getText().length() < 2)
     			hrTxt.setText("0" + hrTxt.getText());
 
     		theHour = currentHr-12;
     		am = false;
     		ampmTxt.setText("PM");
     	} else {
     		if(currentHr < 10)
 				hrTxt.setText("0"+String.valueOf(currentHr));
 			 else 
 				hrTxt.setText(String.valueOf(currentHr));
     		
     		theHour = currentHr;
    		am = true;
    		ampmTxt.setText("AM");
     	}
     	mHour = currentHr;
     }
     
     public void setCurrentMinute(int currentMin)
     {
     	if(currentMin < 10)
 			{
 				minTxt.setText("0" + String.valueOf(currentMin));
 			} else {
 				minTxt.setText(String.valueOf(currentMin));
 			}
     	mMinute = theMinute = currentMin;
     }
     
     public int getCurrentHour()
     {
     	if(am && mHour == 12)
     	{
     		return 0;
     	} else if(!am && mHour == 24){
     		return 12;
     	} else {
     		return mHour;
     	}
 		
     }
     
     public int getCurrentMinute()
     {
     	return mMinute;
     }
 
 }
