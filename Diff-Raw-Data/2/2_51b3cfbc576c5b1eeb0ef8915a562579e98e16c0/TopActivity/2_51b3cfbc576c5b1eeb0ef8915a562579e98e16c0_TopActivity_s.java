 package in.ccl.ui;
 
 import java.security.acl.NotOwnerException;
 
 import in.ccl.helper.AnimationLayout;
 import in.ccl.helper.Util;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.Html;
 import android.util.Log;
 import android.view.InflateException;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.animation.Animation;
 import android.view.animation.Animation.AnimationListener;
 import android.view.animation.AnimationSet;
 import android.view.animation.TranslateAnimation;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.RelativeLayout.LayoutParams;
 import android.widget.SlidingDrawer;
 import android.widget.SlidingDrawer.OnDrawerCloseListener;
 import android.widget.SlidingDrawer.OnDrawerOpenListener;
 import android.widget.TextView;
 
 public class TopActivity extends Activity implements AnimationLayout.Listener {
 
 	// used as key of the logs.
 	private static final String TAG = "MainActivity";
 
 	// used to identifiy score view either visible or invisble.
 	private boolean isScoreViewVisible;
 
 	// initially animation is in end state, so isAnimationComplete is true.
 	// to prevent multiple clicks when one animation is going on.
 	private boolean isAnimationComplete = true;
 
 	// dynamically adding score view to layout content
 	// for displaying score when user selects dropdown, which is from score part.
 	private View scoreView;
 
 	// used to add inner views from the calling activity to top activity.
 	private RelativeLayout layoutContent;
 
 	// dropdown selection button from header layout.
 	private ImageButton imgBtnScoreDropDown;
 
 	// to prevent backbutton while score animationis showing.
 	// in this case if user press back button should go animation out.
 	private boolean isScoreAnimationHappend = false;
 
 	private RelativeLayout scoreLayout;
 
 	private TextView txtScore;
 
 	private TextView txtScoreTitle;
 
 	private AnimationLayout mLayout;
 
 	private LinearLayout menuLayout;
 
 	private TextView notificationTxt;
 
 	private TextView notificationTitle;
 
 	@Override
 	public void onCreate (Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.common_layout);
 
 		mLayout = (AnimationLayout) findViewById(R.id.animation_layout);
 		menuLayout = (LinearLayout) findViewById(R.id.menu_layout);
 
 		notificationTxt = (TextView) findViewById(R.id.notification_textview);
 		notificationTitle = (TextView) findViewById(R.id.notification_title_textview);
 		TextView notificationOneTxt = (TextView) findViewById(R.id.notification_item1);
 		TextView notificationTwoTxt = (TextView) findViewById(R.id.notification_item2);
 		TextView notificationThreeTxt = (TextView) findViewById(R.id.notification_item3);
 		TextView notificationFourTxt = (TextView) findViewById(R.id.notification_item4);
 
 		Util.setTextFont(this, notificationTitle);
 		Util.setTextFont(this, notificationTxt);
 		Util.setTextFont(this, notificationOneTxt);
 		Util.setTextFont(this, notificationTwoTxt);
 		Util.setTextFont(this, notificationThreeTxt);
 		Util.setTextFont(this, notificationFourTxt);
 
 		mLayout.setListener(this);
 
 		// for user menu selection from top activity.
 		ImageButton imgBtnMenu = (ImageButton) findViewById(R.id.img_btn_menu);
 
 		// setting menu title font
 		TextView menuTitleTxt = (TextView) findViewById(R.id.menu_title);
 		Util.setTextFont(this, menuTitleTxt);
 
 		SlidingDrawer slidingDrawer = (SlidingDrawer) findViewById(R.id.slidingDrawer);
 		slidingDrawer.setOnDrawerOpenListener(new OnDrawerOpenListener() {
 
 			@Override
 			public void onDrawerOpened () {
 				notificationTxt.setVisibility(View.GONE);
 			}
 		});
 
 		slidingDrawer.setOnDrawerCloseListener(new OnDrawerCloseListener() {
 
 			@Override
 			public void onDrawerClosed () {
 				notificationTxt.setVisibility(View.VISIBLE);
 
 			}
 		});
 
 		txtScore = (TextView) findViewById(R.id.score_textview);
 		txtScoreTitle = (TextView) findViewById(R.id.score_title_textview);
 		Util.setTextFont(this, txtScore);
 		Util.setTextFont(this, txtScoreTitle);
 		imgBtnScoreDropDown = (ImageButton) findViewById(R.id.imgbtn_score_dropdown);
 
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
 				animationInit();
 			}
 		});
 		// if
 		imgBtnScoreDropDown.setOnClickListener(new OnClickListener() {
 
 			public void onClick (View v) {
 				animationInit();
 			}
 		});
 		MenuItems.getInstance().loadMenu(this, menuLayout);
 
 	}
 
 	private void animationInit () {
 		if (isScoreViewVisible && isAnimationComplete) {
 			// start animation to hide the score view.
 			prepareAnimation(scoreView, 0.0f, -380.f, true);
 			isScoreAnimationHappend = false;
 
 		}
 		else if (isAnimationComplete) {
 			// start animation to show the score view.
 			isScoreAnimationHappend = true;
 			LayoutInflater inflate = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
 			scoreView = inflate.inflate(R.layout.score_view, null);
 			// TODO
 			TextView txtStiker = (TextView) scoreView.findViewById(R.id.striker_batsman);
 			txtStiker.setText("INDRAJIT" + Html.fromHtml("<sup>*</sup>"));
 			Util.setTextFont(this, txtStiker);
 			Button viewScoreBoard = (Button) scoreView.findViewById(R.id.btn_view_score_board);
 			viewScoreBoard.setOnClickListener(new OnClickListener() {
 
 				@Override
 				public void onClick (View v) {
 					Intent mIntent = new Intent(TopActivity.this, ScoreBoardActivity.class);
 					startActivity(mIntent);
 				}
 			});
 			layoutContent.addView(scoreView, lp);
 			prepareAnimation(scoreView, -380.0f, 0.0f, false);
 			scoreView.setVisibility(View.VISIBLE);
 		}
 
 	}
 
 	/**
 	 * 
 	 * start animation fromYDelta and toYDelta parameters on view. flag is used to identify particular view either going to add or remove from layout content.
 	 * 
 	 * @param scoreView View
 	 * @param fromYDelta float
 	 * @param toYDelta float
 	 * @param flag boolean
 	 */
 	private void prepareAnimation (View view, float fromYDelta, float toYDelta, boolean flag) {
 		if (flag) {
 			txtScoreTitle.setVisibility(View.VISIBLE);
 			txtScore.setText(getResources().getString(R.string.sample_score));
 		}
 		else {
 			txtScore.setText("BENGAL TIGERS  vs  TELUGU WARRIORS");
 			txtScoreTitle.setVisibility(View.GONE);
 		}
 		AnimationSet as = new AnimationSet(false);
 		TranslateAnimation ta = new TranslateAnimation(0.0f, 0.0f, fromYDelta, toYDelta);
 		addAnimationLister(ta, flag);
 		as.addAnimation(ta);
 		as.setDuration(300);
 		as.setFillAfter(true);
 		view.setAnimation(as);
 		view.startAnimation(as);
 	}
 
 	private void addAnimationLister (TranslateAnimation ta, final boolean removeView) {
 		ta.setAnimationListener(new AnimationListener() {
 
 			public void onAnimationStart (Animation animation) {
 
 				isAnimationComplete = false;
 			}
 
 			public void onAnimationRepeat (Animation animation) {
 			}
 
 			public void onAnimationEnd (Animation animation) {
 				isAnimationComplete = true;
 				if (scoreView != null && removeView) {
 					// when score view is going to hide, removing score view from layout content.
 					isScoreViewVisible = false;
 					// setting dropdown default image, when animation is done.
 					imgBtnScoreDropDown.setBackgroundResource(R.drawable.dropdown);
 					layoutContent.removeView(scoreView);
 				}
 				else {
 					// setting dropdown up image, when score view is visible.
 					imgBtnScoreDropDown.setBackgroundResource(R.drawable.dropdown_up);
 					isScoreViewVisible = true;
 				}
 			}
 		});
 	}
 
 	@Override
 	protected void onPause () {
 		super.onPause();
 		if (mLayout.isOpening()) {
 			mLayout.closeSidebar();
 		}
 	}
 
 	@Override
 	public void onBackPressed () {
 
 		if (mLayout.isOpening()) {
 			mLayout.closeSidebar();
 		}
 		else if (isScoreAnimationHappend) {
 			// start animation to hide the score view.
 			prepareAnimation(scoreView, 0.0f, -380.f, true);
 			isScoreAnimationHappend = false;
 		}
 
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
			layoutContent.addView(view);
 		}
 		catch (InflateException e) {
 			Log.e(TAG, "Invalide resource id provided for adding content.");
 		}
 	}
 
 	@Override
 	public void onSidebarOpened () {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onSidebarClosed () {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public boolean onContentTouchedWhenOpening () {
 		mLayout.closeSidebar();
 		return true;
 	}
 }
