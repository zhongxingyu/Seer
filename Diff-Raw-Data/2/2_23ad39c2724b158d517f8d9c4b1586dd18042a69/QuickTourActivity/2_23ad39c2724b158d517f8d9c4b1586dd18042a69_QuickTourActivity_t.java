 package com.madhackerdesigns.neverbelate.ui;
 
 import android.content.Context;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentStatePagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.support.v4.view.ViewPager.OnPageChangeListener;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.animation.AnimationUtils;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.TextView;
 
 import com.madhackerdesigns.neverbelate.R;
 import com.madhackerdesigns.neverbelate.settings.PreferenceHelper;
import com.viewpagerindicator.CirclePageIndicator; 
 
 public class QuickTourActivity extends FragmentActivity {
 	
 	static final int[] PAGE_LAYOUTS = {	
 		R.layout.qt_page_0,
 		R.layout.qt_page_1,
 		R.layout.qt_page_2,
 		R.layout.qt_page_3,
 		R.layout.qt_page_4,
 		R.layout.qt_page_5
 	};
 	
 	static Resources RESOURCES;
 
     MyAdapter mAdapter;
     Context mContext;
     CirclePageIndicator mIndicator;
     ViewPager mPager;
     
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.quick_tour);
         mContext = getApplicationContext();
 
         mAdapter = new MyAdapter(getSupportFragmentManager());
 
         mPager = (ViewPager)findViewById(R.id.pager);
         mPager.setAdapter(mAdapter);
         
       	//Bind the title indicator to the adapter
         CirclePageIndicator indicator = (CirclePageIndicator) findViewById(R.id.indicator);
         mIndicator = indicator;
         indicator.setViewPager(mPager);
         indicator.setSnap(true);
         indicator.setOnPageChangeListener(new OnPageChangeListener() {
 
 			public void onPageScrollStateChanged(int state) {
 				// Change visibility of indicator based on scroll state
 				View indicator = mIndicator;
 				switch(state) {
 				case ViewPager.SCROLL_STATE_DRAGGING:
 					if (indicator.getVisibility() == View.INVISIBLE) {
 						indicator.startAnimation(AnimationUtils.loadAnimation(mContext, android.R.anim.fade_in));
 						indicator.setVisibility(View.VISIBLE);
 					}
 					break;
 				case ViewPager.SCROLL_STATE_IDLE:
 					if (indicator.getVisibility() == View.VISIBLE) {
 						indicator.startAnimation(AnimationUtils.loadAnimation(mContext, android.R.anim.fade_out));
 						indicator.setVisibility(View.INVISIBLE);
 					}
 					break;
 				case ViewPager.SCROLL_STATE_SETTLING:
 					// Do nothing.
 					break;
 				}
 			}
 
 			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
 				// Do nothing.
 			}
 
 			public void onPageSelected(int position) {
 				// Do nothing.
 			}
         	
         });
         
         RESOURCES = getResources();
     }
     
     public static class MyAdapter extends FragmentStatePagerAdapter {
         
     	public MyAdapter(FragmentManager fm) {
             super(fm);
         }
 
         @Override
         public int getCount() {
             return PAGE_LAYOUTS.length;
         }
 
         @Override
         public Fragment getItem(int position) {
             return PageFragment.newInstance(position);
         }
     }
     
     public static class PageFragment extends Fragment {
 
     	private int mPosition;
     	PreferenceHelper mPrefs;
     	TextView mSummaryText;
     	String mSummaryOff;
     	String mSummaryOn;
     	
     	/**
          * Create a new instance of PageFragment, providing "pos"
          * as an argument.
          */
         public static PageFragment newInstance(int position) {
 			PageFragment f = new PageFragment();
 			
 			// Supply pager position as an argument.
             Bundle args = new Bundle();
             args.putInt("pos", PAGE_LAYOUTS[position]);
             f.setArguments(args);
 
             return f;
 		}
 		
 		/**
          * When creating, retrieve this instance's position from its arguments.
          */
         @Override
         public void onCreate(Bundle savedInstanceState) {
             super.onCreate(savedInstanceState);
             mPosition = getArguments() != null ? getArguments().getInt("pos") : 1;
         }
 
         /**
          * The Fragment's UI is inflated from the list of layouts.
          */
         @Override
         public View onCreateView(LayoutInflater inflater, ViewGroup container,
                 Bundle savedInstanceState) {
             View v = inflater.inflate(mPosition, container, false);
             
             // Build a finish OnClickListener
             OnClickListener finishListener = new OnClickListener() {
             	
 				public void onClick(View v) {
 					// Finish the activity
 					getActivity().finish();
 				}
             	
             };
             
             // Load the Skip Tour button if available
             TextView skipTour = (TextView) v.findViewById(R.id.tv_skip_tour);
             if (skipTour != null) {
 	            skipTour.setOnClickListener(finishListener);
             }
             
             // Load the Enable checkbox if available
             CheckBox btnEnable = (CheckBox) v.findViewById(R.id.btn_enable);
             if (btnEnable != null) {
             	if (mPrefs == null) { mPrefs = new PreferenceHelper(getActivity()); }
             	if (mPrefs.isNeverLateEnabled()) {
             		btnEnable.setChecked(true);
             		btnEnable.setClickable(false);
             	} else {
             		btnEnable.setOnCheckedChangeListener(new OnCheckedChangeListener() {
             			
             			public void onCheckedChanged(CompoundButton buttonView,
             					boolean isChecked) {
             				// Change the enabled state
             				mPrefs.setNeverLateEnabled(isChecked);
             				buttonView.setClickable(false);
             			}
             			
             		});
             	}
             }
             
             // Load the marked locations preference view if available
             ViewGroup pref = (ViewGroup) v.findViewById(R.id.pref_marked_locations);
             if (pref != null) {
             	inflateMarkedLocationPref(pref);
             }
             
             // Load the Done button if available
             Button btnDone = (Button) v.findViewById(R.id.qt_finish_button);
             if (btnDone != null) {
             	btnDone.setOnClickListener(finishListener);
             }
             return v;
         }
         
         private void inflateMarkedLocationPref(ViewGroup v) {
         	// Set the preference title
         	Resources res = v.getResources();
         	String title = res.getString(R.string.pr_marked_locations_title);
         	((TextView) v.findViewById(R.id.title)).setText(title);
         	
         	// Set the "off" summary text
         	mSummaryOff = res.getString(R.string.pr_marked_locations_summary_off);
         	mSummaryOn = res.getString(R.string.pr_marked_locations_summary_on);
         	mSummaryText = (TextView) v.findViewById(R.id.summary);
         	mSummaryText.setText(mSummaryOff);
         	
         	// Inflate the checkbox into the widget placeholder
         	LayoutInflater inflater = (LayoutInflater) v.getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
         	ViewGroup widget = (ViewGroup) v.findViewById(R.id.widget_frame);
         	inflater.inflate(R.layout.preference_widget_checkbox, widget);
         	CheckBox c = (CheckBox) widget.findViewById(R.id.checkbox);
         	
         	// Set the OnCheckedChangedListener to actually change the summary and underlying preference
         	if (mPrefs == null) { mPrefs = new PreferenceHelper(getActivity()); }
         	c.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 
 				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 					mPrefs.setOnlyMarkedLocatitons(isChecked);
 					if (isChecked) { mSummaryText.setText(mSummaryOn); }
 					else { mSummaryText.setText(mSummaryOff); }
 				}
         		
         	});
         	
         	// Check the prefs and set checkbox for current state
         	c.setFocusable(true);
         	c.setClickable(true);
         	c.setChecked(mPrefs.isOnlyMarkedLocations()); 
         }
     }
 }
