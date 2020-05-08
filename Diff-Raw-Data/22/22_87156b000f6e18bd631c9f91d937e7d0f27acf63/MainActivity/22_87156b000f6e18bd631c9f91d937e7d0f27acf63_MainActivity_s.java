 package com.strangebrew.elsinore;
 
 import java.util.Iterator;
 import java.util.Locale;
 
 import com.freddymartens.android.widgets.Gauge;
 import com.strangebrew.elsinore.content.Data;
 import com.strangebrew.elsinore.content.Device;
 import com.strangebrew.elsinore.content.PID;
 import com.strangebrew.elsinore.content.Temp;
 
 
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.os.SystemClock;
 import android.preference.PreferenceManager;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.app.NavUtils;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.Chronometer;
 import android.widget.EditText;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.TextView;
 
 public class MainActivity extends FragmentActivity {
 
     /**
      * The {@link android.support.v4.view.PagerAdapter} that will provide
      * fragments for each of the sections. We use a
      * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
      * will keep every loaded fragment in memory. If this becomes too memory
      * intensive, it may be best to switch to a
      * {@link android.support.v4.app.FragmentStatePagerAdapter}.
      */
     static SectionsPagerAdapter mSectionsPagerAdapter;
 
     /**
      * The {@link ViewPager} that will host the section contents.
      */
     static ViewPager mViewPager;
     private static final String KEY_SERVER_REFRESH_PREFERENCE = "pref_key_server_refresh";
     private ResponseReceiver receiver;
     
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         
         IntentFilter filter = new IntentFilter(ResponseReceiver.ACTION_RESP);
         filter.addCategory(Intent.CATEGORY_DEFAULT);
         receiver = new ResponseReceiver();
         registerReceiver(receiver, filter);
         
 		Intent service = new Intent(this, BackgroundServer.class);
 		
 		//  service.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
 		service.putExtra(BackgroundServer.PARAM_IN_MSG, "TEST");
 		//Schedule additional service calls using alarm manager.
 		startService(service);
 		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
 		PendingIntent pi = PendingIntent.getService(this, 0, service, 0);
 		
 		//Retrieve time interval from settings (a good practice to let users set the interval).
 		
 		SharedPreferences prefs =
 				PreferenceManager.getDefaultSharedPreferences(getBaseContext());
 		alarmManager.cancel(pi);
 		alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(),
 		     Integer.parseInt(prefs.getString(KEY_SERVER_REFRESH_PREFERENCE, "5"))*1000  , pi);
 
         // Create the adapter that will return a fragment for each of the three
         // primary sections of the app.
         mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
 
         // Set up the ViewPager with the sections adapter.
         mViewPager = (ViewPager) findViewById(R.id.pager);
         mViewPager.setAdapter(mSectionsPagerAdapter);
 
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
     
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()){
         case R.id.action_settings:
            // app icon in action bar clicked; go home
               Intent intent = new Intent(this, SettingsActivity.class);
               intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
               startActivity(intent);
         }
         return true;
     }
 
     
 
     /**
      * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
      * one of the sections/tabs/pages.
      */
     public class SectionsPagerAdapter extends FragmentPagerAdapter {
 
         public SectionsPagerAdapter(FragmentManager fm) {
             super(fm);
         }
 
         @Override
         public Fragment getItem(int position) {
             // getItem is called to instantiate the fragment for the given page.
             // Return a DummySectionFragment (defined as a static inner class
             // below) with the page number as its lone argument.
             Fragment fragment = new DummySectionFragment();
             Bundle args = new Bundle();
             args.putString(DummySectionFragment.ARG_SECTION_TITLE, this.getPageTitle(position).toString());
             args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
             fragment.setArguments(args);
             return fragment;
         }
 
         @Override
         public int getCount() {
             // Show 3 total pages.
         	return Data.ITEMS.size();
             //return 3;
         }
 
         @Override
         public CharSequence getPageTitle(int position) {
             Locale l = Locale.getDefault();
             return Data.ITEMS.get(position).name.toUpperCase();
             /*switch (position) {
                 case 0:
                 	
                     //return getString(R.string.title_section_hlt).toUpperCase(l);
                 case 1:
                 	
                     return getString(R.string.title_section_mlt).toUpperCase(l);
                 case 2:
                     return getString(R.string.title_section_kettle).toUpperCase(l);
             }*/
             //return null;
         }
     }
     
     
     public void onRadioButtonClicked(View view) {
    	 // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();
     // Check which radio button was clicked
        View rootView = (View) view.getParent().getParent();
        PID d;
        try {
     	   d = (PID) Data.getFuzzyDevice(((TextView)rootView.findViewById(R.id.section_label)).getText().toString());
        } catch (ClassCastException cce) {
     	   hideAllInputs(rootView);
     	   return;
        }
        
        if(d == null) {
     	   return;
        }
        
        switch(view.getId()) {
            case R.id.mode_auto:
                if (checked) {
             	   TextView tView = (TextView) rootView.findViewById(R.id.target_temp);
             	   // don't change anything if we're tagged
             	   tView.setTag("input");
             	   switchAutoPid(rootView, d);	                
                }
                break;
            case R.id.mode_manual:
                if (checked) {
             	   TextView tView = (TextView) rootView.findViewById(R.id.target_temp);
             	   // don't change anything if we're tagged
             	   tView.setTag("input");
                    switchManualPid(rootView, d);
                }
                break;
            case R.id.mode_off:
                if (checked) {
             	   TextView tView = (TextView) rootView.findViewById(R.id.target_temp);
             	   // don't change anything if we're tagged
             	   tView.setTag("input");
                    switchOffPid(rootView, d);
                }
                break;
        }
    }
     
     public void onClickSubmit(View V){
         // local temps for the data coming in
         double tTemp = 0D; double tCycle = 0D; double tDuty = 0D;
         double tK = 0D; double tI = 0D; double tP = 0D;
         String tMode = "";
 
         View rootView = (View) V.getParent().getParent();
         RadioGroup rMode = (RadioGroup) rootView.findViewById(R.id.radio_mode);
         
        
  		   
         switch(rMode.getCheckedRadioButtonId()) {
         case R.id.mode_auto:
            tMode = "auto";
            break;
         case R.id.mode_off:
            tMode = "off";
            break;
         case R.id.mode_manual:
            tMode = "manual";
            break;
         }
         // find the data and see if it's valid
         EditText tText = (EditText) rootView.findViewById(R.id.target_temp);
         tText.setTag("");
         
         try {
            tTemp = Double.parseDouble(tText.getText().toString());
            Log.i("POST", "SetPoint: " + tTemp);
         } catch (NumberFormatException ne) {
            Log.i("Updating PID", "Could not parse Target Temp as a double");
         }
 
         tText = (EditText) rootView.findViewById(R.id.cycle_time);
         try {
            tCycle = Double.parseDouble(tText.getText().toString());
         } catch (NumberFormatException ne) {
            Log.i("Updating PID", "Could not parse Cycle Time as a double");
         }
 
         tText = (EditText) rootView.findViewById(R.id.duty_cycle);
         try {
            tDuty = Double.parseDouble(tText.getText().toString());
         } catch (NumberFormatException ne) {
            Log.i("Updating PID", "Could not parse Duty Cycle as a double");
         }
 
         tText = (EditText) rootView.findViewById(R.id.cycle_time);
         try {
            tK = Double.parseDouble(tText.getText().toString());
         } catch (NumberFormatException ne) {
            Log.i("Updating PID", "Could not parse K as a double");
         }
 
         tText = (EditText) rootView.findViewById(R.id.cycle_time);
         try {
      	    tP = Double.parseDouble(tText.getText().toString());
         } catch (NumberFormatException ne) {
            Log.i("Updating PID", "Could not parse P as a double");
         }
 
 
 
         // update the Device
 
       // Fragment f = getFragmentManager().findFragmentById(index);
 
         Device mItem;
         tText = (EditText) rootView.findViewById(R.id.target_temp);
         
         TextView lView = (TextView) rootView.findViewById(R.id.section_label);
         mItem = (Device) Data.getFuzzyDevice(lView.getText().toString());
         if(mItem != null && mItem.getClass() == PID.class) {
            // we have a PID to update
      	   try {
      		   ((PID) mItem).UpdateParams(tDuty, tCycle, tP, tI, tK,  tMode, tTemp);
      		   // clear the tag
      		  tText = (EditText) rootView.findViewById(R.id.target_temp);
      		  if(tText != null) {
    			   // don't change anything if we're tagged
      			  tText.setTag("");
    		      		   
      		  }
      	   } catch (ClassCastException e) {
      		   
      	   }
         }
       }
   
     public void startChrono(View v) {
     	// do something with the chrono
     	View rootView = (View) v.getParent();
     	TextView cText = (TextView) rootView.findViewById(R.id.chrono_label);
     	
     	Chronometer cView = (Chronometer) rootView.findViewById(R.id.chrono);
     	Button cButton = (Button) rootView.findViewById(R.id.start_timer);
     	
     	if(cView.getTag() == null) {
     		cView.setBase(SystemClock.elapsedRealtime());
     		cView.setTag("stopped");
     	}
     	
     	if(cView.getTag().equals("reset")) { // no chrono yet
     		cView.setBase(SystemClock.elapsedRealtime()); 
     		cView.setTag("stopped");
     		// clear the button tag
     		cButton.setTag("");
     		
     	}
     	if(cView.getTag().equals("stopped")) {
     		// tag says that this is stopped, so we can start it, always reset the timer
     		if(cButton.getTag() != null && !cButton.getTag().equals("")) {
     			// there's something in the tag, can we parse it
     			Long time;
     			try {
     				time = Long.parseLong(cButton.getTag().toString());
     				// time has the time we stopped with in it, so subtract this from the current time
     				
     				cView.setBase(SystemClock.elapsedRealtime() - time);
     			} catch (NumberFormatException e) {
     				// don't do anything
     			}
     		}
     		cView.start();
     		cView.setTag("started");
     		
     		// set the button to stop
     		
     		cButton.setText(R.string.chrono_stop);
     		
     	} else if(cView.getTag().equals("started")) {
     		stopChrono(v);
     	}
     	
     }
     
     public void stopChrono(View v) {
     	// do something with the chrono
     	View rootView = (View) v.getParent();
     	Button cStart = (Button) rootView.findViewById(R.id.start_timer);
     	
     	Chronometer cView = (Chronometer) rootView.findViewById(R.id.chrono);
     	
     	if(cView.getTag() != null || cView.getTag().equals("started")) {
     		// tag says that this is stopped, so we can start it, always reset the timer
     		cView.stop();
     		cView.setTag("stopped");
     		cStart.setText(R.string.chrono_start);
     		cStart.setTag(SystemClock.elapsedRealtime() -  cView.getBase()); // store this in the tag
     	}
     	
     }
     
     public void resetChrono(View v) {
     	// do something with the chrono
     	View rootView = (View) v.getParent();
     	TextView cText = (TextView) rootView.findViewById(R.id.chrono_label);
     	
     	Chronometer cView = (Chronometer) rootView.findViewById(R.id.chrono);
     	
     	if(cView.getTag() == null) {
     		cView.setBase(SystemClock.elapsedRealtime());
     		cView.setTag("stopped");
     	}
     	
     	if(cView.getTag().equals("stopped")) {
     		// tag says that this is stopped, so we can start it, always reset the timer
     		cView.setBase(SystemClock.elapsedRealtime());
     		cView.setTag("reset");
     	}
     	
     }
     
     /**
      * A dummy fragment representing a section of the app, but that simply
      * displays dummy text.
      */
     public static class DummySectionFragment extends Fragment {
         /**
          * The fragment argument representing the section number for this
          * fragment.
          */
         public static final String ARG_SECTION_NUMBER = "section_number";
         public static final String ARG_SECTION_TITLE = "section_title";
         public static Gauge tempGauge;
         public static TextView dummyTextView;
         public static View rootView;
         
         public DummySectionFragment() {
         
         }
 
         @Override
         public View onCreateView(LayoutInflater inflater, ViewGroup container,
                 Bundle savedInstanceState) {
             rootView = inflater.inflate(R.layout.fragment_main_pid, container, false);
             rootView.setTag(getArguments().getString(ARG_SECTION_TITLE));
             Bundle args = getArguments();
             dummyTextView = (TextView) rootView.findViewById(R.id.section_label);
             
             Log.i("Dummy", getArguments().getString(ARG_SECTION_TITLE));
             
             if(Data.getFuzzyDevice(getArguments().getString(ARG_SECTION_TITLE)) == null) {
             	hideAllInputs(rootView);
             	tempGauge = (Gauge) rootView.findViewById(R.id.temp_meter);
             	if(tempGauge != null) {
             		tempGauge.setVisibility(Gauge.INVISIBLE);
             		
             	}
             	// log 
             	
             	return rootView;
             	
             }
             dummyTextView.setText(getArguments().getString(ARG_SECTION_TITLE));
             
              tempGauge = (Gauge) rootView.findViewById(R.id.temp_meter);
             tempGauge.setValue(175f);
             Log.i("Gauge", "Set value to 175f");
             return rootView;
         }
         
         @Override 
         public void onResume() {
         	super.onResume();
         	
             dummyTextView = (TextView) rootView.findViewById(R.id.section_label);
         	if(Data.getFuzzyDevice(getArguments().getString(ARG_SECTION_TITLE)) == null) {
         		hideAllInputs(rootView);
         		tempGauge = (Gauge) rootView.findViewById(R.id.temp_meter);
         		if(tempGauge != null) {
         			tempGauge.setVisibility(Gauge.INVISIBLE);
             	
         		}
             // log 
         	}	
             return;
             	
             
         }
       
     }
     
    
     
     /* Setup the BroadcastReceiver so that we know when the data has changed */
     public class ResponseReceiver extends BroadcastReceiver {
         public static final String ACTION_RESP =
            "com.strangebrew.elsinore.intent.action.MESSAGE_PROCESSED";
         
         @Override
          public void onReceive(Context context, Intent intent) {
         	
 
         	// iterate through and find the values with tags to be updated
         	View child = ((View) mViewPager.getChildAt(mViewPager.getCurrentItem()).getParent());
         	
         	
         	for (int i = 0; i < Data.ITEMS.size(); i++) {
         		String tName = Data.ITEMS.get(i).name;
         		View frag = child.findViewWithTag(tName);
         		if (frag == null) {
         			return;
         		}
         		View container = (View) frag.getParent();
                 TextView titleText = (TextView) container.findViewById(R.id.section_label);
                 if(titleText == null) {
                 	return;
                 }
                 
                 Log.i("R", mViewPager.getCurrentItem() + "Received " + titleText.getText().toString());
                 Device d = Data.getFuzzyDevice(titleText.getText().toString());
                 Gauge tempGauge = (Gauge) container.findViewById(R.id.temp_meter); 
                 
                 if(d == null) {
                 	tempGauge.setVisibility(Gauge.INVISIBLE);
                 	return;
                 }
                 
                 // set the values according to the scale incoming
                 if(d.scale.equalsIgnoreCase("f")) {
                 	tempGauge.setUnitTitle("\u2109");
                 	tempGauge.setScaleMaxValue(240);
                 	tempGauge.setScaleMinValue(40);
                 } else {
                 	tempGauge.setUnitTitle("\u2103");
                 	tempGauge.setScaleMaxValue(100);
                 	tempGauge.setScaleMinValue(0);
                 }
                 tempGauge.setValue((float) d.temperature);
                 
                 if(d.getClass() == PID.class) {
                 	 EditText tView = (EditText) container.findViewById(R.id.target_temp);
           		   
            		  
           		   
           		   if(tView != null) {
           			   // don't change anything if we're tagged
           			   try {
           		      	   if(tView.getTag().toString().equals("input")) {
           		      		   return; 
           		      	   }
           		      	 
           	    		} catch (NullPointerException npe) {
           	    			// no tag, so carry on
           	    		}
           		   }
           		   
                 	PID temp = (PID) d;
                 	if(temp.mode.equalsIgnoreCase("off")) {
                 		// no set point
             			switchOffPid(container, d);
                 	}
                 	if(temp.mode.equalsIgnoreCase("manual")) {
                 		switchManualPid(container, d);
                 	}
                 	if(temp.mode.equalsIgnoreCase("auto")) {
                 		switchAutoPid(container, d);
                 	}
                 } else { // this is a Temp probe
                 	Temp tTemp = (Temp) d;
                 	hideAllInputs(container);
                 }
                 
         	}
 
          }
      }
 
 
     
     public static void hideAllInputs(View rootView) {
     	
     	// HIDE EVEYRTHING!
     	
     	RadioButton r = (RadioButton) rootView.findViewById(R.id.mode_auto);
     	if(r != null) {
     		r.setVisibility(RadioButton.INVISIBLE);	
     	}
     	r = (RadioButton) rootView.findViewById(R.id.mode_off);
     	if(r != null) {
     		r.setVisibility(RadioButton.INVISIBLE);	
     	}
     	
     	r = (RadioButton) rootView.findViewById(R.id.mode_auto);
     	if(r != null) {
     		r.setVisibility(RadioButton.INVISIBLE);	
     	}
     	
     	r = (RadioButton) rootView.findViewById(R.id.mode_manual);
     	if(r != null) {
     		r.setVisibility(RadioButton.INVISIBLE);	
     	}
     	
     	RadioGroup rGroup = (RadioGroup) rootView.findViewById(R.id.radio_mode);
     	if(rGroup != null) {
     		rGroup.setVisibility(RadioGroup.INVISIBLE);	
     	}
     	
 	   EditText tView = (EditText) rootView.findViewById(R.id.target_temp);
 	   if(tView != null) {
 		   tView.setSelected(false);
 				tView.setVisibility(EditText.INVISIBLE);
 	   }
 	   TextView lView = (TextView) rootView.findViewById(R.id.label_target_temp);
     	if(lView != null) {
     		lView.setVisibility(TextView.INVISIBLE);
     	}
 			// Duty Cycle
 	    tView = (EditText) rootView.findViewById(R.id.duty_cycle);
 	    if(tView != null) {
 	    	tView.setSelected(false);
     	   	tView.setVisibility(EditText.INVISIBLE);    	   	
 	    }
 	    
 	    lView = (TextView) rootView.findViewById(R.id.label_duty_cycle);
     	if(lView != null) {
     		lView.setVisibility(TextView.INVISIBLE);
     	}
     	
 		// Duty Time
     	tView = (EditText) rootView.findViewById(R.id.cycle_time);
     	if(tView != null) {
     		tView.setSelected(false);
         	tView.setVisibility(EditText.INVISIBLE);
     	}
     	
     	lView = (TextView) rootView.findViewById(R.id.label_cycle_time);
     	if(lView != null) {
     		lView.setVisibility(TextView.INVISIBLE);
     	}
     	
        	// P
     	tView = (EditText) rootView.findViewById(R.id.p_input);
     	if(tView != null) {
     		tView.setSelected(false);
         	tView.setVisibility(EditText.INVISIBLE);
     	}
     	
     	lView = (TextView) rootView.findViewById(R.id.label_p);
     	if(lView != null) {
     		lView.setVisibility(TextView.INVISIBLE);
     	}
     	
     	lView = (TextView) rootView.findViewById(R.id.unit_p);
     	if(lView != null) {
     		lView.setVisibility(TextView.INVISIBLE);
     	}
     	
        	// I
     	tView = (EditText) rootView.findViewById(R.id.i_input);
     	if(tView != null) {
     		tView.setSelected(false);
         	tView.setVisibility(EditText.INVISIBLE);
    		}
     	
     	lView = (TextView) rootView.findViewById(R.id.label_i);
     	if(lView != null) {
     		lView.setVisibility(TextView.INVISIBLE);
     	}
     	
     	lView = (TextView) rootView.findViewById(R.id.unit_i);
     	if(lView != null) {
     		lView.setVisibility(TextView.INVISIBLE);
     	}
     	
        	// K
     	tView = (EditText) rootView.findViewById(R.id.k_input);
     	if(tView != null) {
     		tView.setSelected(false);
         	tView.setVisibility(EditText.INVISIBLE);
     	}
     	
     	lView = (TextView) rootView.findViewById(R.id.label_k);
     	if(lView != null) {
     		lView.setVisibility(TextView.INVISIBLE);
     	}
     	
     	lView = (TextView) rootView.findViewById(R.id.unit_k);
     	if(lView != null) {
     		lView.setVisibility(TextView.INVISIBLE);
     	}
     }
     
     public void switchManualPid(View rootView, Device inDev) {
     	// Manual mode
        	// no set point
     	// try to cast to PID
     	
     	PID d;
     	try {
     		d = (PID) inDev;
     	} catch (ClassCastException cce) {
     		// could not cast to a PID, so it's obviously not a valid PID! Best hide the input
     		hideAllInputs(rootView);
     		return;
     	}
     	
     	// Manual mode
    		// no set point
 	   EditText tView = (EditText) rootView.findViewById(R.id.target_temp);
 	   
 	  
 	   
 	   if(tView != null) {
 		   // don't change anything if we're tagged
 		   tView.setSelected(false);
 		   tView.setVisibility(EditText.INVISIBLE);
 		   
 	   }
 	   TextView lView = (TextView) rootView.findViewById(R.id.label_target_temp);
     	if(lView != null) {
     		lView.setVisibility(TextView.INVISIBLE);
     	}
 			// Duty Cycle
 	    tView = (EditText) rootView.findViewById(R.id.duty_cycle);
 	    if(tView != null) {
 	    	tView.setSelected(false);
     	   	tView.setVisibility(EditText.VISIBLE);
     	   	tView.setText(Double.toString(d.dutycycle));
 	    }
 	    
 	    lView = (TextView) rootView.findViewById(R.id.label_duty_cycle);
     	if(lView != null) {
     		lView.setVisibility(TextView.VISIBLE);
     	}
     	
 		// Duty Time
     	tView = (EditText) rootView.findViewById(R.id.cycle_time);
     	if(tView != null) {
     		tView.setSelected(false);
         	tView.setVisibility(EditText.VISIBLE);
         	tView.setText(Double.toString(d.cycletime));
     	}
     	
     	lView = (TextView) rootView.findViewById(R.id.label_cycle_time);
     	if(lView != null) {
     		lView.setVisibility(TextView.VISIBLE);
     	}
     	
        	// P
     	tView = (EditText) rootView.findViewById(R.id.p_input);
     	if(tView != null) {
     		tView.setSelected(false);
         	tView.setVisibility(EditText.VISIBLE);
         	tView.setText(Double.toString(d.p_param));
     	}
     	
     	lView = (TextView) rootView.findViewById(R.id.label_p);
     	if(lView != null) {
     		lView.setVisibility(TextView.VISIBLE);
     	}
     	
     	lView = (TextView) rootView.findViewById(R.id.unit_p);
     	if(lView != null) {
     		lView.setVisibility(TextView.VISIBLE);
     	}
     	
        	// I
     	tView = (EditText) rootView.findViewById(R.id.i_input);
     	if(tView != null) {
     		tView.setSelected(false);
         	tView.setVisibility(EditText.VISIBLE);
         	tView.setText(Double.toString(d.i_param));
    		}
     	
     	lView = (TextView) rootView.findViewById(R.id.label_i);
     	if(lView != null) {
     		lView.setVisibility(TextView.VISIBLE);
     	}
     	
     	lView = (TextView) rootView.findViewById(R.id.unit_i);
     	if(lView != null) {
     		lView.setVisibility(TextView.VISIBLE);
     	}
     	
        	// K
     	tView = (EditText) rootView.findViewById(R.id.k_input);
     	if(tView != null) {
     		tView.setSelected(false);
         	tView.setVisibility(EditText.VISIBLE);
         	tView.setText(Double.toString(d.k_param));
     	}
     	
     	lView = (TextView) rootView.findViewById(R.id.label_k);
     	if(lView != null) {
     		lView.setVisibility(TextView.VISIBLE);
     	}
     	
     	lView = (TextView) rootView.findViewById(R.id.unit_k);
     	if(lView != null) {
     		lView.setVisibility(TextView.VISIBLE);
     	}
     }
     	
     public void switchAutoPid(View rootView, Device inDev) {
     	// Manual mode
        	// no set point
     	// try to cast to PID
     	PID d;
     	try {
     		d = (PID) inDev;
     	} catch (ClassCastException cce) {
     		// could not cast to a PID, so it's obviously not a valid PID! Best hide the input
     		hideAllInputs(rootView);
     		return;
     	}
     	
     	   // Auto Mode
       	
  	   // Set Point
      	EditText tView = (EditText) rootView.findViewById(R.id.target_temp);
      	if(tView != null) {
      	// don't change anything if we're tagged
      		/*try {
  	      	   if(tView.getTag().toString().equals("input")) {
  	      		   return; 
  	      	   }
 	 	      	
      		} catch (NullPointerException npe) {
      			// no tag, carry on
      		}*/
      		
      		tView.setSelected(false);
          	tView.setVisibility(EditText.VISIBLE);
          	tView.setText(Double.toString(d.setpoint));
      	}
      	
      	TextView lView = (TextView) rootView.findViewById(R.id.label_target_temp);
      	if(lView != null) {
      		lView.setVisibility(TextView.VISIBLE);
      	}
      	
         	// No Duty Cycle
      	tView = (EditText) rootView.findViewById(R.id.duty_cycle);
      	if(tView != null) {
      		tView.setSelected(false);
          	tView.setVisibility(EditText.INVISIBLE);
      	}
      	
      	lView = (TextView) rootView.findViewById(R.id.label_duty_cycle);
      	if(lView != null) {
      		lView.setVisibility(TextView.INVISIBLE);
      	}
      	
         	// Duty Time
      	tView = (EditText) rootView.findViewById(R.id.cycle_time);
      	if(tView != null) {
      		tView.setSelected(false);
          	tView.setVisibility(EditText.VISIBLE);
          	tView.setText(Double.toString(d.cycletime));
      	}
      	
      	lView = (TextView) rootView.findViewById(R.id.label_cycle_time);
      	if(lView != null) {
      		lView.setVisibility(TextView.VISIBLE);
      	}
      	
         	// P
      	tView = (EditText) rootView.findViewById(R.id.p_input);
      	if(tView != null) {
      		tView.setSelected(false);
          	tView.setVisibility(EditText.VISIBLE);
          	tView.setText(Double.toString(d.p_param));
      	}
      	
      	lView = (TextView) rootView.findViewById(R.id.label_p);
      	if(lView != null) {
      		lView.setVisibility(TextView.VISIBLE);
      	}
      	
      	lView = (TextView) rootView.findViewById(R.id.unit_p);
      	if(lView != null) {
      		lView.setVisibility(TextView.VISIBLE);
      	}
      	
         	// I
      	tView = (EditText) rootView.findViewById(R.id.i_input);
      	if(tView != null) {
      		tView.setSelected(false);
          	tView.setVisibility(EditText.VISIBLE);
          	tView.setText(Double.toString(d.i_param));
      	}
      	
      	lView = (TextView) rootView.findViewById(R.id.label_i);
      	if(lView != null) {
      		lView.setVisibility(TextView.VISIBLE);
      	}
      	
      	lView = (TextView) rootView.findViewById(R.id.unit_i);
      	if(lView != null) {
      		lView.setVisibility(TextView.VISIBLE);
      	}
         	// K
      	tView = (EditText) rootView.findViewById(R.id.k_input);
      	if(tView != null) {
      		tView.setSelected(false);
          	tView.setVisibility(EditText.VISIBLE);
          	tView.setText(Double.toString(d.k_param));
      	}
      	
      	lView = (TextView) rootView.findViewById(R.id.label_k);
      	if(lView != null) {
      		lView.setVisibility(TextView.VISIBLE);
      	}
      	
      	lView = (TextView) rootView.findViewById(R.id.unit_k);
      	if(lView != null) {
      		lView.setVisibility(TextView.VISIBLE);
      	}
      	
      	return;
     }
     
     public void switchOffPid(View rootView, Device inDev) {
     	// Manual mode
        	// no set point
     	// try to cast to PID
     	PID d;
     	try {
     		d = (PID) inDev;
     	} catch (ClassCastException cce) {
     		// could not cast to a PID, so it's obviously not a valid PID! Best hide the input
     		hideAllInputs(rootView);
     		return;
     	}
     	
     	EditText tView = (EditText) rootView.findViewById(R.id.target_temp);
     	// don't change anything if we're tagged
     	if(tView != null) {
    		// don't change anything if we're tagged
      		/*try {
   	      	   if(tView.getTag().toString().equals("input")) {
   	      		   return; 
   	      	   }
   	      	  
       		} catch (NullPointerException npe) {
       			// no tag, carry on
       		}*/
       		
      		 tView.setVisibility(EditText.INVISIBLE);
    		}
    		
 		TextView lView = (TextView) rootView.findViewById(R.id.label_target_temp);
 		if(lView != null) {
 			lView.setVisibility(TextView.INVISIBLE);
 		}
 			// Duty Cycle
 		tView = (EditText) rootView.findViewById(R.id.duty_cycle);
 		if(tView != null) {
 			tView.setSelected(false);
 		   	tView.setVisibility(EditText.INVISIBLE);
 		   	tView.setText(Double.toString(d.setpoint));
 		}
 		
 		lView = (TextView) rootView.findViewById(R.id.label_duty_cycle);
 		if(lView != null) {
 			lView.setVisibility(TextView.INVISIBLE);
 		}
 		
 		// Duty Time
 		tView = (EditText) rootView.findViewById(R.id.cycle_time);
 		if(tView != null) {
 			tView.setSelected(false);
 			tView.setVisibility(EditText.INVISIBLE);
 			tView.setText(Double.toString(d.cycletime));
 		}
 		
 		lView = (TextView) rootView.findViewById(R.id.label_cycle_time);
 		if(lView != null) {
 			lView.setVisibility(TextView.INVISIBLE);
 		}
 		
 		// P
 		tView = (EditText) rootView.findViewById(R.id.p_input);
 		if(tView != null) {
 			tView.setSelected(false);
 			tView.setVisibility(EditText.INVISIBLE);
 			tView.setText(Double.toString(d.p_param));
 		}
 		
 		lView = (TextView) rootView.findViewById(R.id.label_p);
 		if(lView != null) {
 			lView.setVisibility(TextView.INVISIBLE);
 		}
 		
 		lView = (TextView) rootView.findViewById(R.id.unit_p);
 		if(lView != null) {
 			lView.setVisibility(TextView.INVISIBLE);
 		}
 		
 		// I
 		tView = (EditText) rootView.findViewById(R.id.i_input);
 		if(tView != null) {
 			tView.setSelected(false);
 			tView.setVisibility(EditText.INVISIBLE);
 			tView.setText(Double.toString(d.i_param));
 		}
 		
 		lView = (TextView) rootView.findViewById(R.id.label_i);
 		if(lView != null) {
 			lView.setVisibility(TextView.INVISIBLE);
 		}
 		
 		lView = (TextView) rootView.findViewById(R.id.unit_i);
 		if(lView != null) {
 			lView.setVisibility(TextView.INVISIBLE);
 		}
 		
 		// K
 		tView = (EditText) rootView.findViewById(R.id.k_input);
 		if(tView != null) {
 			tView.setSelected(false);
 			tView.setVisibility(EditText.INVISIBLE);
 			tView.setText(Double.toString(d.k_param));
 		}
 		
 		lView = (TextView) rootView.findViewById(R.id.label_k);
 		if(lView != null) {
 			lView.setVisibility(TextView.INVISIBLE);
 		}
 		
 		lView = (TextView) rootView.findViewById(R.id.unit_k);
 		if(lView != null) {
 			lView.setVisibility(TextView.INVISIBLE);
 		}
     }
 }
