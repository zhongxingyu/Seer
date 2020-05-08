 package uk.ac.dur.duchess.ui.activity;
 
 import uk.ac.dur.duchess.GlobalApplicationData;
 import uk.ac.dur.duchess.R;
 import uk.ac.dur.duchess.io.NetworkFunctions;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.content.res.Configuration;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.widget.ImageView;
 import android.widget.ImageView.ScaleType;
 import android.widget.TextView;
 import android.widget.ViewSwitcher;
 
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.google.android.apps.analytics.GoogleAnalyticsTracker;
 
 public class UserHubActivity extends BaseActivity
 {
 	private static final String AD_API_URL = "http://www.dur.ac.uk/cs.seg01/duchess/api/v1/features.php";
 	
 	private Context context;
 	
 	private TextView browseButton;
 	private TextView collegeEventButton;
 	private TextView myEventsButton;
 	private TextView societiesButton;
 	private TextView societyEventListButton;
 	private TextView calendarButton;
 	
 	private ViewSwitcher adViewSwitcher;
 	private TextView adText;
 	private String adLink;
 	
 	private ImageView defaultImage;
 	private ImageView adImage;
 	private ImageView adNavigationIndicator;
 	
 	private Animation imageFadeIn;
 	private boolean imageHasLoaded = false;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		
 		setContentView(R.layout.dashboard);
 
 		context = this;
 		
 		browseButton = (TextView) findViewById(R.id.dashboard_browse_button);
 		collegeEventButton = (TextView) findViewById(R.id.dashboard_college_button);
 		myEventsButton = (TextView) findViewById(R.id.dashboard_bookmarked_button);
 		societiesButton = (TextView) findViewById(R.id.dashboard_societies_button);
 		societyEventListButton = (TextView) findViewById(R.id.dashboard_subscriptions_button);
 		calendarButton = (TextView) findViewById(R.id.dashboard_calendar_button);
 		
 		adViewSwitcher = (ViewSwitcher) findViewById(R.id.dashboardAdAnimator);
 		
 		defaultImage = new ImageView(this);
 		defaultImage.setScaleType(ScaleType.CENTER_CROP);
 		defaultImage.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.the_great_wave_of_kanagawa));
 		
 		adViewSwitcher.addView(defaultImage);
 		adText = (TextView) findViewById(R.id.dashboardAdText);
 		adNavigationIndicator = (ImageView) findViewById(R.id.adNavigationIndicator);
 
 		browseButton.setOnClickListener(new View.OnClickListener()
 		{
 			@Override
 			public void onClick(View v)
 			{
 				if (GlobalApplicationData.getAnalyticsPermission(v.getContext()))
 				{
 					GoogleAnalyticsTracker tracker = ((GlobalApplicationData) getApplication()).getTracker();
 					tracker.trackEvent("UserHub", "ButtonClicked", "Browse Events", 0);
 					tracker.dispatch();
 				}
 				Intent i = new Intent(v.getContext(), EventListActivity.class);
 				startActivity(i);
 			}
 		});
 
 		collegeEventButton.setOnClickListener(new View.OnClickListener()
 		{
 			@Override
 			public void onClick(View v)
 			{
 				if (GlobalApplicationData.getAnalyticsPermission(v.getContext()))
 				{
 					GoogleAnalyticsTracker tracker = ((GlobalApplicationData) getApplication())
 							.getTracker();
 					tracker.trackEvent("UserHub", "ButtonClicked", "College Events", 0);
 					tracker.dispatch();
 				}
 				Intent i = new Intent(v.getContext(), CollegeEventListActivity.class);
 				startActivity(i);
 			}
 		});
 
 		myEventsButton.setOnClickListener(new View.OnClickListener()
 		{
 			@Override
 			public void onClick(View v)
 			{
 				if (GlobalApplicationData.getAnalyticsPermission(v.getContext()))
 				{
 					GoogleAnalyticsTracker tracker = ((GlobalApplicationData) getApplication())
 							.getTracker();
 					tracker.trackEvent("UserHub", "ButtonClicked", "Bookmarked Events", 0);
 					tracker.dispatch();
 				}
 				Intent i = new Intent(v.getContext(), BookmarkedEventListActivity.class);
 				startActivity(i);
 			}
 		});
 
 		societiesButton.setOnClickListener(new View.OnClickListener()
 		{
 			@Override
 			public void onClick(View v)
 			{
 				if (GlobalApplicationData.getAnalyticsPermission(v.getContext()))
 				{
 					GoogleAnalyticsTracker tracker = ((GlobalApplicationData) getApplication())
 							.getTracker();
 					tracker.trackEvent("UserHub", "ButtonClicked", "Browse Societies", 0);
 					tracker.dispatch();
 				}
 				Intent i = new Intent(v.getContext(), SocietyListActivity.class);
 				startActivity(i);
 			}
 		});
 
 		societyEventListButton.setOnClickListener(new View.OnClickListener()
 		{
 			@Override
 			public void onClick(View v)
 			{
 				if (GlobalApplicationData.getAnalyticsPermission(v.getContext()))
 				{
 					GoogleAnalyticsTracker tracker = ((GlobalApplicationData) getApplication()).getTracker();
 					tracker.trackEvent("UserHub", "ButtonClicked", "Society Events", 0);
 					tracker.dispatch();
 				}
 				Intent i = new Intent(v.getContext(), PersonalSocietyListActivity.class);
 				startActivity(i);
 			}
 		});
 
 		calendarButton.setOnClickListener(new View.OnClickListener()
 		{
 			@Override
 			public void onClick(View v)
 			{
 				if (GlobalApplicationData.getAnalyticsPermission(v.getContext()))
 				{
 					GoogleAnalyticsTracker tracker = ((GlobalApplicationData) getApplication())
 							.getTracker();
 					tracker.trackEvent("UserHub", "ButtonClicked", "Event Calendar", 0);
 					tracker.dispatch();
 				}
 				Intent i = new Intent(v.getContext(), CalendarActivity.class);
 				startActivity(i);
 			}
 		});
 		
 		adNavigationIndicator.setOnClickListener(new View.OnClickListener()
 		{
 			@Override
 			public void onClick(View v)
 			{
 				if (adLink != null && imageHasLoaded)
 				{
 					Intent i = new Intent(Intent.ACTION_VIEW);
 					i.setData(Uri.parse(adLink));
 					startActivity(i);
 				}
 			}
 		});
 		
 		(new DownloadImageTask()).execute("");
 	}
 	
 	@Override
 	public void onResume()
 	{
 		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 		super.onResume();
 	}
 	
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		getSupportMenuInflater().inflate(R.menu.user_hub_menu, menu);
 		return true;
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item)
 	{
 		switch (item.getItemId())
 		{
 		case R.id.showAboutBoxMenuItem:
 			Intent showAboutBoxIntent = new Intent(this, AboutBoxActivity.class);
 			startActivity(showAboutBoxIntent);
 			return true;
 		case R.id.dashboardSettingsMenuItem:
 			Intent settingsIntent = new Intent(this, SettingsActivity.class);
 			startActivity(settingsIntent);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 	
 	@Override
 	public void onConfigurationChanged(Configuration newConfig)
 	{
 	    super.onConfigurationChanged(newConfig);
 	    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 	}
 	
 	private class DownloadImageTask extends AsyncTask<String, Void, Bitmap>
 	{
 		private String text;
 		@Override
 		protected Bitmap doInBackground(String... vargs)
 		{
 			try 
 			{ 
 				String adSpec[] = NetworkFunctions.downloadText(AD_API_URL).split("\n");
 				text = adSpec[1];
 				adLink = adSpec[3];
 				return NetworkFunctions.downloadImage(adSpec[2]); 
 			}
 			catch (Exception ex)
 			{
				return null;
 			}
 		}
 
 		@Override
 		protected void onPostExecute(Bitmap bitmap)
 		{
			if (bitmap == null || text == null)
 			{
				return;
 			}
 			adText.setText(text);
 			
 			Animation navIndicatorFade = AnimationUtils.loadAnimation(context, R.anim.image_fadein_animation);
 			
 			adNavigationIndicator.setVisibility(View.VISIBLE);
 			adNavigationIndicator.startAnimation(navIndicatorFade);
 			
 			adImage = new ImageView(context);
 			adImage.setScaleType(ScaleType.CENTER_CROP);
 			adImage.setImageBitmap(bitmap);
 						
 			adViewSwitcher.addView(adImage);
 			adViewSwitcher.showNext();
 			imageHasLoaded = true;
 		}
 	}
 
 }
