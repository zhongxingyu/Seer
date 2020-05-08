 package com.strangebrew.elsinore;
 
 import java.util.Date;
 import java.text.SimpleDateFormat;
 import java.util.Locale;
 
 import com.freddymartens.android.widgets.Gauge;
 
 import com.jjoe64.graphview.GraphView;
 import com.jjoe64.graphview.GraphViewSeries;
 import com.jjoe64.graphview.GraphView.GraphViewData;
 import com.jjoe64.graphview.LineGraphView;
 import com.strangebrew.elsinore.content.Data;
 import com.strangebrew.elsinore.content.Device;
 import com.strangebrew.elsinore.content.PID;
 import com.strangebrew.elsinore.content.Temp;
 
 
 import android.app.AlarmManager;
 import android.app.AlertDialog;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.content.res.TypedArray;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.os.SystemClock;
 import android.preference.PreferenceManager;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.util.TypedValue;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnLongClickListener;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.Button;
 import android.widget.Chronometer;
 import android.widget.EditText;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.RelativeLayout;
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
             
         	// if we are using the summary page, return it, otherwise return the appropriate device
         	if(position == 0) {
         		Fragment fragment = new SummarySectionFragment();
 	            Bundle args = new Bundle();
 	            args.putString(SummarySectionFragment.ARG_SECTION_TITLE, this.getPageTitle(position).toString());
 	            args.putInt(SummarySectionFragment.ARG_SECTION_NUMBER, position + 1);
 	            fragment.setArguments(args);
 	            return fragment;
         	} else {
 	            Fragment fragment = new DeviceSectionFragment();
 	            Bundle args = new Bundle();
 	            args.putString(DeviceSectionFragment.ARG_SECTION_TITLE, this.getPageTitle(position).toString());
 	            args.putInt(DeviceSectionFragment.ARG_SECTION_NUMBER, position + 1);
 	            fragment.setArguments(args);
 	            return fragment;
         	}
         }
 
         @Override
         public int getCount() {
             // increment the number of items by one to account for the Summary page
         	return (Data.ITEMS.size()+1);
         }
 
         @Override
         public CharSequence getPageTitle(int position) {
             Locale l = Locale.getDefault();
             
             if(position == 0) {
             	return "Brew Day";
             }
             return Data.ITEMS.get(position-1).name.toUpperCase(l);
             
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
        RelativeLayout parentView = (RelativeLayout) rootView.getParent();
        TextView tView = (TextView) parentView.findViewById(R.id.target_temp);
        tView.setTag("input");
        switch(view.getId()) {
        		
            case R.id.mode_auto:
                if (checked) {
             	   // don't change anything if we're tagged
             	   switchAutoPid(parentView, d);	                
                }
                break;
            case R.id.mode_manual:
                if (checked) {
             	   switchManualPid(parentView, d);
                }
                break;
            case R.id.mode_off:
                if (checked) {
             	   switchOffPid(parentView, d);
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
 
         tText = (EditText) rootView.findViewById(R.id.k_input);
         try {
            tK = Double.parseDouble(tText.getText().toString());
         } catch (NumberFormatException ne) {
            Log.i("Updating PID", "Could not parse K as a double");
         }
 
         tText = (EditText) rootView.findViewById(R.id.i_input);
         try {
            tI = Double.parseDouble(tText.getText().toString());
         } catch (NumberFormatException ne) {
            Log.i("Updating PID", "Could not parse I as a double");
         }
 
         tText = (EditText) rootView.findViewById(R.id.p_input);
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
    	View cRootView = (View) v.getParent();
     	
     	
    	Chronometer cView = (Chronometer) cRootView.findViewById(R.id.chrono);
     	
     	if(cView.getTag() == null) {
     		cView.setBase(SystemClock.elapsedRealtime());
     		cView.setTag("stopped");
     	}
     	
     	if(cView.getTag().equals("stopped")) {
     		// tag says that this is stopped, so we can start it, always reset the timer
     		cView.setBase(SystemClock.elapsedRealtime());
     		cView.setTag("reset");
     	}
     	
     	// also reset the input mode
    	View rView = (View) cRootView.getParent();
    	EditText tView = (EditText) rView.findViewById(R.id.target_temp);
     	if(tView != null) {
     		tView.setTag("");
     	}
     	
     	// get the current device and clear it
    	rView = (View) cRootView.getRootView().findViewById(R.id.ScrollView01);
     	
    	if(rView.getTag() != null) {
    		String rTag = rView.getTag().toString();
	    	Device d = Data.getFuzzyDevice(rTag);
	    	if(d != null) {
	    		d.deviceSeries.resetData(new GraphViewData[0]);
	    	}
		}
     }
     
     /**
      * A device fragment representing a section of the app
      */
     public static class DeviceSectionFragment extends Fragment {
         /**
          * The fragment argument representing the section number for this
          * fragment.
          */
         public static final String ARG_SECTION_NUMBER = "section_number";
         public static final String ARG_SECTION_TITLE = "section_title";
         public static Gauge tempGauge;
         public static TextView dummyTextView;
         public static View rootView;
         
         public DeviceSectionFragment() {
         
         }
 
         @Override
         public View onCreateView(LayoutInflater inflater, ViewGroup container,
                 Bundle savedInstanceState) {
         	
             rootView = inflater.inflate(R.layout.fragment_main_pid, container, false);
             rootView.setTag(getArguments().getString(ARG_SECTION_TITLE));
             dummyTextView = (TextView) rootView.findViewById(R.id.section_label);
             String cDevice = getArguments().getString(ARG_SECTION_TITLE);
             
             if(Data.getFuzzyDevice(cDevice) == null) {
             	hideAllInputs(rootView);
             	tempGauge = (Gauge) rootView.findViewById(R.id.temp_meter);
             	if(tempGauge != null) {
             		tempGauge.setVisibility(Gauge.INVISIBLE);
             		
             	}
             	
             	
             	
             	return rootView;
             	
             }
             Device d = Data.getFuzzyDevice(cDevice);
             dummyTextView.setText(cDevice);
             
             tempGauge = (Gauge) rootView.findViewById(R.id.temp_meter);
             tempGauge.setValue(0f);
             
             // add graph log the temperature to the graph
             TypedValue tv = new TypedValue();
             rootView.getContext().getTheme().resolveAttribute(android.R.attr.textColorSecondary, tv, true);
             int textColor = getResources().getColor(tv.resourceId);
             
             LineGraphView graphView = new LineGraphView(container.getContext() , cDevice ) {
             	@Override
             	protected String formatLabel(double value, boolean isValueX) {
             		if (isValueX) {
             			// convert unix time to human time
             			return SimpleDateFormat.getTimeInstance().format(new Date((long)(value)));
             		} else return super.formatLabel(value, isValueX); // let the y-value be normal-formatted
             	}
             };
             
 
             graphView.getGraphViewStyle().setHorizontalLabelsColor(textColor);
             graphView.getGraphViewStyle().setVerticalLabelsColor(textColor);
             graphView.setDrawBackground(false);
             graphView.addSeries(d.deviceSeries);
             graphView.setScrollable(true);
             graphView.setScalable(false);
             graphView.setTag("graph");
 
             
             
             // replace the current graph
             RelativeLayout pidLayout = (RelativeLayout) rootView.findViewById(R.id.pid_layout);
             
             RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
             
             params.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
             params.addRule(RelativeLayout.BELOW, R.id.submit_layout);
             params.addRule(RelativeLayout.ALIGN_LEFT, R.id.submit_layout);
             params.addRule(RelativeLayout.CENTER_HORIZONTAL);
             pidLayout.addView(graphView, params);
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
     
     /**
      * A device fragment representing a section of the app
      */
     public static class SummarySectionFragment extends Fragment {
         /**
          * The fragment argument representing the section number for this
          * fragment.
          */
         public static final String ARG_SECTION_NUMBER = "section_number";
         public static final String ARG_SECTION_TITLE = "section_title";
         public static Gauge tempGauge;
         public static TextView dummyTextView;
         public static View rootView;
         
         public SummarySectionFragment() {
         
         }
 
         @Override
         public View onCreateView(LayoutInflater inflater, ViewGroup container,
                 Bundle savedInstanceState) {
             rootView = inflater.inflate(R.layout.fragment_brewday, container, false);
             rootView.setTag(getArguments().getString(ARG_SECTION_TITLE));
             
             OnLongClickListener longPressTime = new OnLongClickListener() {
 	
 	        	public void reallySetTime(TextView v) {
 	            	// we now want to forcefull overwrite the textView
 	            	SimpleDateFormat s = new SimpleDateFormat("hh:mm:ss dd/MM/yyyy");
 	            	String format = s.format(new Date());
 	            	
 	            	v.setText(format);
 	            	
 	            }
 	            
 	        	public void needPrereq(int failId, int prereqId, Context c) {
 	        		AlertDialog.Builder alertDialog = new AlertDialog.Builder(c);
    	    		 
     	            // Setting Dialog Title
     	            alertDialog.setTitle("Can't set this time");
     	     
     	            // Setting Dialog Message
     	            alertDialog.setMessage("Cannot set " + c.getString(failId) + " because " + 
     	            		c.getString(prereqId) + " is not set");
     	            
     	            alertDialog.setNeutralButton(R.string.neutral, new DialogInterface.OnClickListener() {
     	                public void onClick(DialogInterface dialog, int which) {
     	                // Write your code here to execute after dialog closed
     	                	return;
     	                }
     	        });
     	     
 	        	}
 	           
 	        	public  void setTime(View v) {
 	        		// check to see if the pre req inputs are done
 	        		View rootView = v.getRootView();
 	        		TextView start = null;
 	        		
 	        		switch(v.getId()) {
 	        			case R.id.start_time:
 	        				 rootView.findViewById(R.id.mash_in_time).setEnabled(true);
 	        				 break;
 		        		case R.id.mash_in_time:
 		        			start = (TextView) rootView.findViewById(R.id.start_time);
 		        			if(start.getText().toString().equals("")) {
 		        				needPrereq(R.id.mash_in_time, R.id.start_time, v.getContext());
 		        				return;
 		        			}
 		        			rootView.findViewById(R.id.mash_out_time).setEnabled(true);
 		        			break;
 		        		case R.id.mash_out_time:
 		        			start = (TextView) rootView.findViewById(R.id.mash_in_time);
 		        			if(start.getText().toString().equals("")) {
 		        				needPrereq(R.id.mash_out_time, R.id.mash_out_time, v.getContext());
 		        				return;
 		        			}
 		        			rootView.findViewById(R.id.boil_start_time).setEnabled(true);
 		        			break;
 		        		case R.id.boil_start_time:
 		        			start = (TextView) rootView.findViewById(R.id.mash_out_time);
 		        			if(start.getText().toString().equals("")) {
 		        				needPrereq(R.id.boil_start_time, R.id.mash_out_time, v.getContext());
 		        				return;
 		        			}
 		        			rootView.findViewById(R.id.boil_end_time).setEnabled(true);
 		        			break;
 		        		case R.id.boil_end_time:
 		        			start = (TextView) rootView.findViewById(R.id.boil_start_time);
 		        			if(start.getText().toString().equals("")) {
 		        				needPrereq(R.id.boil_end_time, R.id.boil_start_time, v.getContext());
 		        				return;
 		        			}
 		        			rootView.findViewById(R.id.cool_start_time).setEnabled(true);
 		        			break;
 		        		case R.id.cool_start_time:
 		        			start = (TextView) rootView.findViewById(R.id.boil_end_time);
 		        			if(start.getText().toString().equals("")) {
 		        				needPrereq(R.id.cool_start_time, R.id.boil_end_time, v.getContext());
 		        				return;
 		        			}
 		        			rootView.findViewById(R.id.cool_end_time).setEnabled(true);
 		        			break;
 		        		case R.id.cool_end_time:
 		        			start = (TextView) rootView.findViewById(R.id.cool_start_time);
 		        			if(start.getText().toString().equals("")) {
 		        				needPrereq(R.id.cool_end_time, R.id.cool_start_time, v.getContext());
 		        				return;
 		        			}
 		        			break;
 	        		}
 	        		
         	    	// someone has pressed the input long enough to set it to the current time
         	    	final TextView tView = (TextView) v;
         	    	
         	    	Log.i("SetTime", ":" + tView.getText() +":");
         	    	if(!tView.getText().toString().equals("")) {
         	    		AlertDialog.Builder alertDialog = new AlertDialog.Builder(v.getContext());
         	    		 
         	            // Setting Dialog Title
         	            alertDialog.setTitle("Confirm Overwrite...");
         	     
         	            // Setting Dialog Message
         	            alertDialog.setMessage("Are you sure you want to overwrite the time?");
         	     
         	            // Setting Icon to Dialog
         	            //alertDialog.setIcon(R.drawable.delete);
         	     
         	            // Setting Positive "Yes" Button
         	            alertDialog.setPositiveButton(R.string.positive, new OnClickListener() {
         	            	@Override
         	                public void onClick(DialogInterface dialog,int which) {
         	     
         	                // Write your code here to invoke YES event
         	            		reallySetTime(tView);
         	                }
         	            });
         	            
         	                   // Setting Negative "NO" Button
         	            alertDialog.setNegativeButton(R.string.negative, new OnClickListener() {
         	            	@Override
         	                public void onClick(DialogInterface dialog, int which) {
         	                // Write your code here to invoke NO event
         	                
         	                dialog.cancel();
         	                }
 
         	            });
         	     
         	            // Showing Alert Message
         	            alertDialog.show();
         	            
         	            
         	    	} else {
         	    		reallySetTime(tView);
         	    	}
         	    }
         	    
         	 	@Override
 				public boolean onLongClick(View v) {
 					setTime(v);
 					return false;
 				}
             	
             };
             
             rootView.findViewById(R.id.start_time).setOnLongClickListener(longPressTime);
             
             rootView.findViewById(R.id.mash_in_time).setOnLongClickListener(longPressTime);
             rootView.findViewById(R.id.mash_in_time).setEnabled(false);
             rootView.findViewById(R.id.mash_out_time).setOnLongClickListener(longPressTime);
             rootView.findViewById(R.id.mash_out_time).setEnabled(false);
 
             rootView.findViewById(R.id.boil_start_time).setOnLongClickListener(longPressTime);
             rootView.findViewById(R.id.boil_start_time).setEnabled(false);
             rootView.findViewById(R.id.boil_end_time).setOnLongClickListener(longPressTime);
             rootView.findViewById(R.id.boil_end_time).setEnabled(false);
             
             rootView.findViewById(R.id.cool_start_time).setOnLongClickListener(longPressTime);
             rootView.findViewById(R.id.cool_start_time).setEnabled(false);
             rootView.findViewById(R.id.cool_end_time).setOnLongClickListener(longPressTime);
             rootView.findViewById(R.id.cool_end_time).setEnabled(false);
             
             return rootView;
         }
         
         @Override 
         public void onResume() {
         	super.onResume();
         	// not going to do anything here
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
                 	tempGauge.setScaleMaxValue(240);
                 	tempGauge.setScaleMinValue(40);
                 	tempGauge.setScaleCenterValue(140);
                 	tempGauge.setRangeOkMinValue(140);
                 	tempGauge.setRangeOkMaxValue(150);
                 	tempGauge.setRangeWarningMinValue(165);
                 	tempGauge.setRangeWarningMaxValue(175);
                 	tempGauge.setRangeErrorMinValue(212);
                 	tempGauge.setRangeErrorMaxValue(212);
                 	tempGauge.setTotalNotches(120);
                 	tempGauge.setIncrementPerSmallNotch(2);
                 	
                 	tempGauge.setUnitTitle("\u2109");
                 } else {
                 	tempGauge.setScaleCenterValue(55);
                 	tempGauge.setScaleMaxValue(110);
                 	tempGauge.setScaleMinValue(0);
                 	
                 	tempGauge.setRangeOkMinValue(70);
                 	tempGauge.setRangeOkMaxValue(80);
                 	tempGauge.setRangeWarningMinValue(80);
                 	tempGauge.setRangeWarningMaxValue(90);
                 	tempGauge.setRangeErrorMinValue(90);
                 	tempGauge.setRangeErrorMaxValue(100);
                 	tempGauge.setTotalNotches(110);
                 	tempGauge.setIncrementPerSmallNotch(1);
                 	tempGauge.setUnitTitle("\u2103");
                 }
                 tempGauge.setValue((float) d.temperature);
                 if(d.deviceSeries != null) {
                 	LineGraphView tempGraph = (LineGraphView) container.findViewWithTag("graph");
                 	d.deviceSeries.appendData(new GraphViewData(d.elapsed, d.temperature), true);
 					tempGraph.redrawAll();
 				}
                 
                 try { 
                 		PID temp = (PID) d;
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
                 } catch (ClassCastException e) { // this is a Temp probe
                 	hideAllInputs(container);
                 }
                 
         	}
 
          }
      }
 
 
     
     public static void hideAllInputs(View rootView) {
     	
     	// HIDE EVEYRTHING!
     	
     	RadioButton r = (RadioButton) rootView.findViewById(R.id.mode_auto);
     	if(r != null) {
     		r.setVisibility(RadioButton.GONE);	
     	}
     	r = (RadioButton) rootView.findViewById(R.id.mode_off);
     	if(r != null) {
     		r.setVisibility(RadioButton.GONE);	
     	}
     	
     	r = (RadioButton) rootView.findViewById(R.id.mode_auto);
     	if(r != null) {
     		r.setVisibility(RadioButton.GONE);	
     	}
     	
     	r = (RadioButton) rootView.findViewById(R.id.mode_manual);
     	if(r != null) {
     		r.setVisibility(RadioButton.GONE);	
     	}
     	
     	RadioGroup rGroup = (RadioGroup) rootView.findViewById(R.id.radio_mode);
     	if(rGroup != null) {
     		rGroup.setVisibility(RadioGroup.GONE);	
     	}
     	
 	   EditText tView = (EditText) rootView.findViewById(R.id.target_temp);
 	   if(tView != null) {
 		   tView.setSelected(false);
 				tView.setVisibility(EditText.GONE);
 	   }
 	   TextView lView = (TextView) rootView.findViewById(R.id.label_target_temp);
     	if(lView != null) {
     		lView.setVisibility(TextView.GONE);
     	}
 			// Duty Cycle
 	    tView = (EditText) rootView.findViewById(R.id.duty_cycle);
 	    if(tView != null) {
 	    	tView.setSelected(false);
     	   	tView.setVisibility(EditText.GONE);    	   	
 	    }
 	    
 	    lView = (TextView) rootView.findViewById(R.id.label_duty_cycle);
     	if(lView != null) {
     		lView.setVisibility(TextView.GONE);
     	}
     	
 		// Duty Time
     	tView = (EditText) rootView.findViewById(R.id.cycle_time);
     	if(tView != null) {
     		tView.setSelected(false);
         	tView.setVisibility(EditText.GONE);
     	}
     	
     	lView = (TextView) rootView.findViewById(R.id.label_cycle_time);
     	if(lView != null) {
     		lView.setVisibility(TextView.GONE);
     	}
     	
        	// P
     	tView = (EditText) rootView.findViewById(R.id.p_input);
     	if(tView != null) {
     		tView.setSelected(false);
         	tView.setVisibility(EditText.GONE);
     	}
     	
     	lView = (TextView) rootView.findViewById(R.id.label_p);
     	if(lView != null) {
     		lView.setVisibility(TextView.GONE);
     	}
     	
     	lView = (TextView) rootView.findViewById(R.id.unit_p);
     	if(lView != null) {
     		lView.setVisibility(TextView.GONE);
     	}
     	
        	// I
     	tView = (EditText) rootView.findViewById(R.id.i_input);
     	if(tView != null) {
     		tView.setSelected(false);
         	tView.setVisibility(EditText.GONE);
    		}
     	
     	lView = (TextView) rootView.findViewById(R.id.label_i);
     	if(lView != null) {
     		lView.setVisibility(TextView.GONE);
     	}
     	
     	lView = (TextView) rootView.findViewById(R.id.unit_i);
     	if(lView != null) {
     		lView.setVisibility(TextView.GONE);
     	}
     	
        	// K
     	tView = (EditText) rootView.findViewById(R.id.k_input);
     	if(tView != null) {
     		tView.setSelected(false);
         	tView.setVisibility(EditText.GONE);
     	}
     	
     	lView = (TextView) rootView.findViewById(R.id.label_k);
     	if(lView != null) {
     		lView.setVisibility(TextView.GONE);
     	}
     	
     	lView = (TextView) rootView.findViewById(R.id.unit_k);
     	if(lView != null) {
     		lView.setVisibility(TextView.GONE);
     	}
     	
     	Button lButton = (Button) rootView.findViewById(R.id.submit_settings);
     	if(lButton != null) {
     		lButton.setVisibility(Button.GONE);
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
     	
     	// Manual mode\
     	showMode(rootView);
     	// set the mode to manual, just incase
     	RadioButton rButton = (RadioButton) rootView.findViewById(R.id.mode_manual);
     	rButton.setChecked(true);
     	
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
     	
     	Button lButton = (Button) rootView.findViewById(R.id.submit_settings);
     	if(lButton != null) {
     		lButton.setVisibility(Button.VISIBLE);
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
     	showMode(rootView);
     	// set the mode to Auto, just incase
     	RadioButton rButton = (RadioButton) rootView.findViewById(R.id.mode_auto);
     	rButton.setChecked(true);
     	
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
      	
      	Button lButton = (Button) rootView.findViewById(R.id.submit_settings);
     	if(lButton != null) {
     		lButton.setVisibility(Button.VISIBLE);
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
     	
     	showMode(rootView);
     	// set the mode to off, just incase
     	RadioButton rButton = (RadioButton) rootView.findViewById(R.id.mode_off);
     	rButton.setChecked(true);
     	
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
     
     private void showMode(View rootView) {
     	RadioButton r = (RadioButton) rootView.findViewById(R.id.mode_auto);
     	if(r != null) {
     		r.setVisibility(RadioButton.VISIBLE);	
     	}
     	r = (RadioButton) rootView.findViewById(R.id.mode_off);
     	if(r != null) {
     		r.setVisibility(RadioButton.VISIBLE);	
     	}
     	
     	
     	r = (RadioButton) rootView.findViewById(R.id.mode_manual);
     	if(r != null) {
     		r.setVisibility(RadioButton.VISIBLE);	
     	}
     	
     	RadioGroup rGroup = (RadioGroup) rootView.findViewById(R.id.radio_mode);
     	if(rGroup != null) {
     		rGroup.setVisibility(RadioGroup.VISIBLE);	
     	}
     	
     	Button lButton = (Button) rootView.findViewById(R.id.submit_settings);
     	if(lButton != null) {
     		lButton.setVisibility(Button.VISIBLE);
     	}
     }
 }
