 package in.ccl.ui;
 
 import in.ccl.database.BannerCursor;
 import in.ccl.database.DataProviderContract;
 import in.ccl.database.DownloadItemsCursor;
 import in.ccl.helper.AnimationLayout;
 import in.ccl.helper.Util;
 import in.ccl.model.Items;
 import in.ccl.model.TeamMember;
 import in.ccl.model.Teams;
 import in.ccl.util.Constants;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.os.Handler;
 import android.support.v4.content.LocalBroadcastManager;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.InflateException;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageButton;
 import android.widget.LinearLayout;
 import android.widget.LinearLayout.LayoutParams;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 import com.google.ads.AdRequest;
 import com.google.ads.AdSize;
 import com.google.ads.AdView;
 
 public class TopActivity extends Activity implements AnimationLayout.Listener {
 
 	// used as key of the logs.
 	private static final String TAG = "MainActivity";
 
 	// used to identifiy score view either visible or invisble.
 	// private boolean isScoreViewVisible;
 
 	// initially animation is in end state, so isAnimationComplete is true.
 	// to prevent multiple clicks when one animation is going on.
 	// private boolean isAnimationComplete = true;
 
 	// dynamically adding score view to layout content
 	// for displaying score when user selects dropdown, which is from score part.
 	// private View scoreView;
 
 	// used to add inner views from the calling activity to top activity.
 	private RelativeLayout layoutContent;
 
 	// dropdown selection button from header layout.
 	// private ImageButton imgBtnScoreDropDown;
 
 	// to prevent backbutton while score animationis showing.
 	// in this case if user press back button should go animation out.
 	// private boolean isScoreAnimationHappend = false;
 
 	private RelativeLayout scoreLayout;
 
 	private TextView txtScore;
 
 	private TextView txtScoreTitle;
 
 	private AnimationLayout mLayout;
 
 	private LinearLayout menuLayout;
 
 	private DownloadStateReceiver mDownloadStateReceiver;
 
 	private IntentFilter statusIntentFilter;
 
 	/*
 	 * private TextView notificationTxt;
 	 * 
 	 * private TextView notificationTitle;
 	 */
 	// for displaying adds.
 	private AdView adView;
 
 	@Override
 	public void onCreate (Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.common_layout);
 
 		// The filter's action is BROADCAST_ACTION
 		statusIntentFilter = new IntentFilter(Constants.BROADCAST_ACTION);
 
 		// Sets the filter's category to DEFAULT
 		statusIntentFilter.addCategory(Intent.CATEGORY_DEFAULT);
 
 		// Instantiates a new DownloadStateReceiver
 		mDownloadStateReceiver = new DownloadStateReceiver();
 
 		mLayout = (AnimationLayout) findViewById(R.id.animation_layout);
 
 		// animationLayoutSlider = (LinearLayout) findViewById(R.id.animation_layout_sidebar);
 		menuLayout = (LinearLayout) findViewById(R.id.menu_layout);
 
 		// for adds
		adView = new AdView(this, AdSize.BANNER, Constants.MY_AD_UNIT_ID);
 		LinearLayout layout = (LinearLayout) findViewById(R.id.admob_layout);
 		layout.setVisibility(View.VISIBLE);
 		// Add the adView to it
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		lp.gravity = Gravity.CENTER_HORIZONTAL;
		layout.addView(adView, lp);
 
 		// Initiate a generic request to load it with an ad
 		AdRequest adRequest = new AdRequest();
 		/*
 		 * <<<<<<< HEAD //adRequest.addTestDevice(AdRequest.TEST_EMULATOR); // Emulator adRequest.addTestDevice("TEST_DEVICE_ID"); // Test Android Device adView.loadAd(adRequest);//new AdRequest() =======
 		 */
 		adRequest.addTestDevice(AdRequest.TEST_EMULATOR); // Emulator
 
 		adView.loadAd(adRequest);// new AdRequest()
 		// adView.loadAd(new AdRequest());
 		/*
 		 * notificationTxt = (TextView) findViewById(R.id.notification_textview); notificationTitle = (TextView) findViewById(R.id.notification_title_textview); TextView notificationOneTxt = (TextView) findViewById(R.id.notification_item1); TextView notificationTwoTxt = (TextView)
 		 * findViewById(R.id.notification_item2); TextView notificationThreeTxt = (TextView) findViewById(R.id.notification_item3); TextView notificationFourTxt = (TextView) findViewById(R.id.notification_item4);
 		 */
 		/*
 		 * Util.setTextFont(this, notificationTitle); Util.setTextFont(this, notificationTxt); Util.setTextFont(this, notificationOneTxt); Util.setTextFont(this, notificationTwoTxt); Util.setTextFont(this, notificationThreeTxt); Util.setTextFont(this, notificationFourTxt);
 		 */mLayout.setListener(this);
 
 		// for user menu selection from top activity.
 		ImageButton imgBtnMenu = (ImageButton) findViewById(R.id.img_btn_menu);
 
 		// setting menu title font
 		TextView menuTitleTxt = (TextView) findViewById(R.id.menu_title);
 		Util.setTextFont(this, menuTitleTxt);
 
 		// SlidingDrawer slidingDrawer = (SlidingDrawer) findViewById(R.id.slidingDrawer);
 		/*
 		 * slidingDrawer.setOnDrawerOpenListener(new OnDrawerOpenListener() {
 		 * 
 		 * @Override public void onDrawerOpened () { // notificationTxt.setVisibility(View.GONE); } });
 		 * 
 		 * slidingDrawer.setOnDrawerCloseListener(new OnDrawerCloseListener() {
 		 * 
 		 * @Override public void onDrawerClosed () { // notificationTxt.setVisibility(View.VISIBLE);
 		 * 
 		 * } });
 		 */
 		txtScore = (TextView) findViewById(R.id.score_textview);
 		txtScoreTitle = (TextView) findViewById(R.id.score_title_textview);
 		Util.setTextFont(this, txtScore);
 		Util.setTextFont(this, txtScoreTitle);
 		// imgBtnScoreDropDown = (ImageButton) findViewById(R.id.imgbtn_score_dropdown);
 
 		layoutContent = (RelativeLayout) findViewById(R.id.layout_content);
 		scoreLayout = (RelativeLayout) findViewById(R.id.header);
 		// menu click lister, should start menu items activity.
 		imgBtnMenu.setOnClickListener(new OnClickListener() {
 
 			public void onClick (View v) {
 				mLayout.toggleSidebar();
 			}
 		});
 		// if layout select for score view should start animation.
 		// click event to visible and invisible of score view.
 		scoreLayout.setOnClickListener(new OnClickListener() {
 
 			public void onClick (View v) {
 				// animationInit();
 			}
 		});
 		// if
 		/*
 		 * imgBtnScoreDropDown.setOnClickListener(new OnClickListener() {
 		 * 
 		 * public void onClick (View v) { // animationInit(); } });
 		 */MenuItems.getInstance().loadMenu(this, menuLayout, mLayout);
 
 	}
 
 	/*
 	 * private void animationInit () { if (isScoreViewVisible && isAnimationComplete) { // start animation to hide the score view. prepareAnimation(scoreView, 0.0f, -380.f, true); isScoreAnimationHappend = false;
 	 * 
 	 * } else if (isAnimationComplete) { // start animation to show the score view. isScoreAnimationHappend = true; LayoutInflater inflate = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE); RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,
 	 * LayoutParams.WRAP_CONTENT); scoreView = inflate.inflate(R.layout.score_view, null); // TODO TextView txtStiker = (TextView) scoreView.findViewById(R.id.striker_batsman); txtStiker.setText("INDRAJIT" + Html.fromHtml("<sup>*</sup>")); Util.setTextFont(this, txtStiker); Button viewScoreBoard =
 	 * (Button) scoreView.findViewById(R.id.btn_view_score_board); viewScoreBoard.setOnClickListener(new OnClickListener() {
 	 * 
 	 * @Override public void onClick (View v) { Intent mIntent = new Intent(TopActivity.this, ScoreBoardActivity.class); startActivity(mIntent); } }); layoutContent.addView(scoreView, lp); prepareAnimation(scoreView, -380.0f, 0.0f, false); scoreView.setVisibility(View.VISIBLE); }
 	 * 
 	 * }
 	 */
 	/**
 	 * 
 	 * start animation fromYDelta and toYDelta parameters on view. flag is used to identify particular view either going to add or remove from layout content.
 	 * 
 	 * @param scoreView View
 	 * @param fromYDelta float
 	 * @param toYDelta float
 	 * @param flag boolean
 	 */
 	/*
 	 * private void prepareAnimation (View view, float fromYDelta, float toYDelta, boolean flag) { if (flag) { txtScoreTitle.setVisibility(View.VISIBLE); txtScore.setText(getResources().getString(R.string.sample_score)); } else { txtScore.setText("BENGAL TIGERS  vs  TELUGU WARRIORS");
 	 * txtScoreTitle.setVisibility(View.GONE); } AnimationSet as = new AnimationSet(false); TranslateAnimation ta = new TranslateAnimation(0.0f, 0.0f, fromYDelta, toYDelta); addAnimationLister(ta, flag); as.addAnimation(ta); as.setDuration(300); as.setFillAfter(true); view.setAnimation(as);
 	 * view.startAnimation(as); }
 	 */
 	/*
 	 * private void addAnimationLister (TranslateAnimation ta, final boolean removeView) { ta.setAnimationListener(new AnimationListener() {
 	 * 
 	 * public void onAnimationStart (Animation animation) {
 	 * 
 	 * isAnimationComplete = false; }
 	 * 
 	 * public void onAnimationRepeat (Animation animation) { }
 	 * 
 	 * public void onAnimationEnd (Animation animation) { isAnimationComplete = true; if (scoreView != null && removeView) { // when score view is going to hide, removing score view from layout content. isScoreViewVisible = false; // setting dropdown default image, when animation is done.
 	 * imgBtnScoreDropDown.setBackgroundResource(R.drawable.dropdown); layoutContent.removeView(scoreView); } else { // setting dropdown up image, when score view is visible. imgBtnScoreDropDown.setBackgroundResource(R.drawable.dropdown_up); isScoreViewVisible = true; } } }); }
 	 */
 	@Override
 	protected void onRestart () {
 		super.onRestart();
 	}
 
 	@Override
 	protected void onPause () {
 		LocalBroadcastManager.getInstance(this).unregisterReceiver(mDownloadStateReceiver);
 
 		new Handler().postDelayed(new Runnable() {
 
 			@Override
 			public void run () {
 				synchronized (this) {
 					if (mLayout.isOpening()) {
 						mLayout.closeSidebar();
 					}
 				}
 			}
 		}, 300);
 		super.onPause();
 
 	}
 
 	@Override
 	public void onDestroy () {
 		if (adView != null) {
 			adView.destroy();
 		}
 		super.onDestroy();
 	}
 
 	@Override
 	public void onBackPressed () {
 
 		if (mLayout.isOpening()) {
 			// animationLayoutSlider.setVisibility(View.GONE);
 			mLayout.closeSidebar();
 		}/*
 			 * else if (isScoreAnimationHappend) { // start animation to hide the score view. prepareAnimation(scoreView, 0.0f, -380.f, true); isScoreAnimationHappend = false; }
 			 */
 		else {
 			finish();
 		}
 	}
 
 	/**
 	 * Used to inflate required view in to content part of the top layout. it will take layout resource id otherwise throw an exception.
 	 * 
 	 * @param resourceId int
 	 */
 	public void addContent (int resourceId) {
 		LayoutInflater inflate = LayoutInflater.from(this);
 		try {
 			View view = inflate.inflate(resourceId, null);
 			layoutContent.addView(view, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
 		}
 		catch (InflateException e) {
 			Log.e(TAG, "Invalide resource id provided for adding content.");
 		}
 	}
 
 	@Override
 	public void onSidebarOpened () {
 		// animationLayoutSlider.setVisibility(View.VISIBLE);
 	}
 
 	@Override
 	public void onSidebarClosed () {
 		// animationLayoutSlider.setVisibility(View.GONE);
 	}
 
 	@Override
 	public boolean onContentTouchedWhenOpening () {
 		mLayout.closeSidebar();
 		// animationLayoutSlider.setVisibility(View.GONE);
 		return true;
 
 	}
 
 	@Override
 	protected void onResume () {
 		super.onResume();
 		LocalBroadcastManager.getInstance(this).registerReceiver(mDownloadStateReceiver, statusIntentFilter);
 
 	}
 
 	class DownloadStateReceiver extends BroadcastReceiver {
 
 		DownloadStateReceiver () {
 		}
 
 		/**
 		 * 
 		 * This method is called by the system when a broadcast Intent is matched by this class' intent filters
 		 * 
 		 * @param context An Android context
 		 * @param intent The incoming broadcast Intent
 		 */
 		@Override
 		public void onReceive (Context context, Intent intent) {
 
 			// Gets the status from the Intent's extended data, and chooses the appropriate action
 
 			switch (intent.getIntExtra(Constants.EXTENDED_DATA_STATUS, Constants.STATE_ACTION_COMPLETE)) {
 
 				case in.ccl.database.Constants.STATE_ACTION_DOWNLOAD_IMAGE_COMPLETE:
 
 					Cursor cursor = getContentResolver().query(DataProviderContract.DOWNLOAD_IMAGE_TABLE_CONTENTURI, null, null, null, null);
 					ArrayList <Items> downloadImageItems = DownloadItemsCursor.getItems(cursor);
 					if (cursor != null) {
 						cursor.close();
 					}
 					Intent downloadImageIntent = new Intent(TopActivity.this, DownloadActivity.class);
 					if (downloadImageItems.size() > 0 && downloadImageItems != null) {
 						downloadImageIntent.putParcelableArrayListExtra(Constants.EXTRA_DOWNLOAD_KEY, downloadImageItems);
 						downloadImageIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
 						startActivity(downloadImageIntent);
 					}
 					break;
 				case in.ccl.database.Constants.STATE_ACTION_TEAM_MEMBERS_COMPLETE:
 					ArrayList <Teams> teamLogoItems = null;
 					ArrayList <TeamMember> teamMemberItems = null;
 					System.out.println("kranthi STATE_ACTION_TEAM_MEMBERS_COMPLETE ");
 
 					cursor = getContentResolver().query(DataProviderContract.TEAMS_LOGO_TABLE_CONTENTURI, null, null, null, null);
 					if (cursor.getCount() > 0) {
 						teamLogoItems = BannerCursor.getTeamLogoItems(cursor);
 						System.out.println("kranthi teamLogoItems size " + " " + teamLogoItems.size());
 
 					}
 					if (cursor != null) {
 						cursor.close();
 					}
 					cursor = getContentResolver().query(DataProviderContract.TEAM_MEMBERS_TABLE_CONTENTURI, null, null, null, null);
 					if (cursor.getCount() > 0) {
 						teamMemberItems = BannerCursor.getTeamMemberItems(cursor);
 						System.out.println("kranthi teamMemberItems size " + " " + teamMemberItems.size());
 
 					}
 					if (cursor != null) {
 						cursor.close();
 					}
 					if ((teamLogoItems != null && teamLogoItems.size() > 0) && (teamMemberItems != null && teamMemberItems.size() > 0)) {
 
 						callTeamIntent(teamLogoItems, teamMemberItems);
 
 					}
 					else {
 						Log.e(TAG, "Team Data is not availble");
 					}
 					break;
 				default:
 					break;
 			}
 		}
 	}
 
 	private void callTeamIntent (ArrayList <Teams> teamLogoItems, ArrayList <TeamMember> teamMemberItems) {
 		Intent teamActivityIntent = new Intent(this, TeamActivity.class);
 		teamActivityIntent.putParcelableArrayListExtra(in.ccl.util.Constants.EXTRA_TEAM_LOGO_KEY, teamLogoItems);
 		teamActivityIntent.putParcelableArrayListExtra(in.ccl.util.Constants.EXTRA_TEAM_MEMBER_KEY, teamMemberItems);
 		startActivityForResult(teamActivityIntent, in.ccl.util.Constants.TEAM_RESULT);
 	}
 }
