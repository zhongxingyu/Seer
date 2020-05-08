 package edu.vanderbilt.vuphone.android.dining;
 
 import java.util.Calendar;
 
 import android.app.Activity;
 import android.app.TabActivity;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.View;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.ImageView;
 import android.widget.RelativeLayout;
 import android.widget.TabHost;
 import android.widget.TextView;
 import edu.vanderbilt.vuphone.android.map.OneLocation;
 import edu.vanderbilt.vuphone.android.storage.Restaurant;
 
 
 /**
  * Restaurant page, displays important information about restaurants
  * 
  * @author austin
  *
  */
 public class RestaurantDetails extends TabActivity implements
 		TabHost.OnTabChangeListener {
 
 	public static final String RESTAURANT_ID = "0";
 
 	private long restaurantID;
 	private Restaurant restaurant;
 
 	private int[] mCounter;
 
 	private TabHost mTabHost;
 	private TextView day;
 	private TextView range;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.details);
 
 		restaurantID = getIntent().getExtras().getLong(RESTAURANT_ID);
 		restaurant = Restaurant.get(restaurantID);
 
 		setTitle(restaurant.getName());
 
 		// MAIN PAGE VIEWS
 		ImageView image = (ImageView)findViewById(R.restaurantDetails.logo);
			image.setImageResource(restaurant.getIcon());
 		if (image.getDrawable().getIntrinsicHeight() <= 70 && image.getDrawable().getIntrinsicWidth() <=70)
 			((TextView)findViewById(R.restaurantDetails.name)).setText(restaurant.getName());
 			// if the logo is just a symbol
 		else {
 			((TextView)findViewById(R.restaurantDetails.name)).setVisibility(View.GONE);
 			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
 					RelativeLayout.LayoutParams.WRAP_CONTENT);
 			lp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
 			image.setLayoutParams(lp);
 						
 			// if the logo includes the restaurant name
 		}
		if (restaurant.getIcon()==R.drawable.dining_icon) 
			image.setVisibility(View.GONE);
 		
 		mCounter = restaurant.getHours().getCurrentRangeI();
 		day = (TextView) findViewById(R.restaurantDetails.hoursDay);
 		range = (TextView) findViewById(R.restaurantDetails.hoursRangeDisplay);
 		updateRangeText();
 		((ImageView) findViewById(R.restaurantDetails.rightArrow))
 				.setOnClickListener(new View.OnClickListener() {
 					public void onClick(View v) {
 						Log.i("test", "onclick");
 						if (++mCounter[1] >= restaurant.getHours()
 								.getRangesToModify(mCounter[0]).size()
 								|| mCounter[1] == -1) {
 							mCounter[0] = (mCounter[0] - Calendar.SUNDAY + 7 + 1)
 									% 7 + Calendar.SUNDAY;
 							mCounter[1] = (restaurant.getHours()
 									.getRangesToModify(mCounter[0]).isEmpty() ? -1
 									: 0);
 						}
 						updateRangeText();
 					}
 				});
 		((ImageView) findViewById(R.restaurantDetails.leftArrow))
 				.setOnClickListener(new View.OnClickListener() {
 					public void onClick(View v) {
 						Log.i("test", "onclick");
 						if (--mCounter[1] < 0 || mCounter[1] == -1) {
 							mCounter[0] = (mCounter[0] - Calendar.SUNDAY + 7 - 1)
 									% 7 + Calendar.SUNDAY;
 							mCounter[1] = restaurant.getHours()
 									.getRangesToModify(mCounter[0]).size() - 1;
 						}
 
 						updateRangeText();
 					}
 				});
 		
 		if (restaurant.getDescription() == null)
 			((TextView) findViewById(R.restaurantDetails.details))
 					.setVisibility(View.GONE);
 		else
 			((TextView) findViewById(R.restaurantDetails.details))
 					.setText(restaurant.getDescription());
 		
 		if (restaurant.offCampus()) {
 			((TextView) findViewById(R.restaurantDetails.phone))
 					.setText(restaurant.getPhoneNumber());
 			((TextView) findViewById(R.restaurantDetails.phone))
 					.setOnClickListener(new View.OnClickListener() {
 						public void onClick(View v) {
 							startActivity(new Intent(Intent.ACTION_VIEW,
 									Uri.parse("tel:"
 											+ restaurant.getPhoneNumber())));
 						}
 					});
 
 			((TextView) findViewById(R.restaurantDetails.web))
 					.setText(restaurant.getUrl());
 			((TextView) findViewById(R.restaurantDetails.web))
 					.setOnClickListener(new View.OnClickListener() {
 						public void onClick(View v) {
 							startActivity(new Intent(Intent.ACTION_VIEW, Uri
 									.parse(restaurant.getUrl())));
 						}
 					});
 		} else {
 			((TextView) findViewById(R.restaurantDetails.phone_header))
 					.setVisibility(View.GONE);
 			((TextView) findViewById(R.restaurantDetails.phone))
 					.setVisibility(View.GONE);
 			((TextView) findViewById(R.restaurantDetails.web_header))
 					.setVisibility(View.GONE);
 			((TextView) findViewById(R.restaurantDetails.web))
 					.setVisibility(View.GONE);
 		}
 
 		// TAB SET UP
 		mTabHost = getTabHost();
 		mTabHost.addTab(mTabHost.newTabSpec("tab1").setIndicator("Details",
 				getResources().getDrawable(R.drawable.ic_tab_details_main))
 				.setContent(R.restaurantDetails.mainContent));
 		mTabHost.addTab(mTabHost.newTabSpec("tab2").setIndicator("Map",
 				getResources().getDrawable(R.drawable.ic_tab_details_map))
 				.setContent(
 						new Intent(this, OneLocation.class).putExtra(
 								OneLocation.RESTAURANT_ID, restaurantID)));
 
 		mTabHost.setOnTabChangedListener(this);
 		mTabHost.setCurrentTab(0);
 
 		/*
 		 * TextSwitcher Code if we want to upgrade API level
 		 * 
 		 * mSwitcher = (TextSwitcher)
 		 * findViewById(R.restaurantDetailsPage.switcher);
 		 * mSwitcher.setFactory(this);
 		 * 
 		 * Animation in = AnimationUtils.loadAnimation(this,
 		 * android.R.anim.slide_out_right); Animation out =
 		 * AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
 		 * mSwitcher.setInAnimation(in); mSwitcher.setOutAnimation(out);
 		 */
 
 	}
 
 	private CharSequence getCurrentDay() {
 		switch (mCounter[0]) {
 		case Calendar.SUNDAY:
 			return "Sunday";
 		case Calendar.MONDAY:
 			return "Monday";
 		case Calendar.TUESDAY:
 			return "Tuesday";
 		case Calendar.WEDNESDAY:
 			return "Wednesday";
 		case Calendar.THURSDAY:
 			return "Thursday";
 		case Calendar.FRIDAY:
 			return "Friday";
 		case Calendar.SATURDAY:
 			return "Saturday";
 		default:
 			return "";
 		}
 	}
 
 	private void updateRangeText() {
 
 		//Log.i("RestaurantDetails", "day " + mCounter[0] + ", range "
 		//		+ mCounter[1]);
 		day.setText(getCurrentDay());
 		if (mCounter[1] == -1)
 			range.setText("closed");
 		else
 			range.setText(restaurant.getHours().getRangesToModify(mCounter[0])
 					.get(mCounter[1]).toString());
 	}
 
 	public void onTabChanged(String tabId) {
 		// Because we're using Activities as our tab children, we trigger
 		// onWindowFocusChanged() to let them know when they're active. This may
 		// seem to duplicate the purpose of onResume(), but it's needed because
 		// onResume() can't reliably check if a keyguard is active.
 		Activity activity = getLocalActivityManager().getActivity(tabId);
 		if (activity != null) {
 			activity.onWindowFocusChanged(true);
 		}
 	}
 
 	// private void nextRange() {
 	// if (++mCounter[1] >=
 	// restaurant.getHours().getRangesToModify(mCounter[0]).size()) {
 	// mCounter[0] = (mCounter[0] - Calendar.SUNDAY + 1) % 7 + Calendar.SUNDAY;
 	// mCounter[1] = 0;
 	// }
 	// }
 	// private void previousRange() {
 	// if (--mCounter[1] < 0) {
 	// mCounter[0] = (mCounter[0] - Calendar.SUNDAY - 1) % 7 + Calendar.SUNDAY;
 	// mCounter[1] = restaurant.getHours().getRangesToModify(mCounter[0]).size()
 	// - 1;
 	// }
 	// }
 }
