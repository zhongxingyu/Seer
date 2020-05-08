 package com.shnu.lbsshnu;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.SearchManager;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.Menu;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.animation.Animation;
 import android.view.animation.AnticipateOvershootInterpolator;
 import android.view.animation.TranslateAnimation;
 import android.widget.CompoundButton;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.SearchView;
 import android.widget.Switch;
 import android.widget.Toast;
 
 import com.supermap.data.DatasetVector;
 
 public class BaseActivity extends Activity {
 	LbsApplication lbsApplication;
 	RelativeLayout mapRelativeLayout;
 	SimpleSideDrawer simpleSideDrawer;
 	Switch wifiLayerSwitch;
 	Switch locationSwitch;
 	Switch resultSwitch;
 	Switch photoSwitch;
 	RelativeLayout locationRelLayout;
 	LinearLayout actionbarLinLayout;
 	LinearLayout searchLinLayout;
 	Handler handler;
 	ImageView userImageView;
 	ImageView moreImageView;
 	Query queryViaSuperMap;
 	static ActivityClass activity;
 	static List<Result> results = new ArrayList<Result>();
 	static boolean isPopUp = false;
 	static boolean isSearch = false;
 	static boolean flagSearch = false;
 	static boolean hasDetail;
 
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		lbsApplication = (LbsApplication) getApplication();
 	}
 
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// getMenuInflater().inflate(R.menu.home, menu);
 		return true;
 	}
 
 	/*
 	 * slideractionbar
 	 */
 	public void initMainBar() {
 		flagSearch = false;
 		simpleSideDrawer = new SimpleSideDrawer(this);
 		simpleSideDrawer.setLeftBehindContentView(R.layout.sliderleft);
 		simpleSideDrawer.setRightBehindContentView(R.layout.sliderright);
 		userImageView = (ImageView) findViewById(R.id.imgUser);
 		moreImageView = (ImageView) findViewById(R.id.imgMore);
 		searchLinLayout = (LinearLayout) findViewById(R.id.lnySearch);
 		searchLinLayout.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				if (isPopUp) {
 					LbsApplication.clearCallout();
 					locationViewPopup(
 							-LbsApplication.Dp2Px(BaseActivity.this, 50), 0,
 							mapRelativeLayout);
 					isPopUp = false;
 					hasDetail = false;
 				}
 				initSearchBar();
 			}
 		});
 		userImageView.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				simpleSideDrawer.toggleLeftDrawer();
 			}
 		});
 		moreImageView.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				simpleSideDrawer.toggleRightDrawer();
 			}
 		});
 		initActivityRightSilder();
 		initWifiSwitch();
 		initLocationSwitch();
 		initPhotoSwitch();
 	}
 
 	/*
 	 * wifi
 	 */
 	public void initWifiSwitch() {
 		wifiLayerSwitch = (Switch) findViewById(R.id.swtWifi);
 		wifiLayerSwitch
 				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
 
 					@Override
 					public void onCheckedChanged(CompoundButton buttonView,
 							boolean isChecked) {
 						// TODO Auto-generated method stub
 						if (isChecked) {
 							lbsApplication.mWifiLayerL.setVisible(true);
 							lbsApplication.mWifiLayerS.setVisible(true);
 							LbsApplication.refreshMap();
 							Toast.makeText(BaseActivity.this,
 									"WLAN㣬ʾУ԰WIFI", Toast.LENGTH_SHORT)
 									.show();
 						} else {
 							lbsApplication.mWifiLayerL.setVisible(false);
 							lbsApplication.mWifiLayerS.setVisible(false);
 							LbsApplication.refreshMap();
 							Toast.makeText(BaseActivity.this,
 									"رWLAN㣬У԰WIFI", Toast.LENGTH_SHORT)
 									.show();
 						}
 						simpleSideDrawer.toggleLeftDrawer();
 					}
 				});
 	}
 
 	/*
 	 * Ƿλ
 	 */
 	public void initLocationSwitch() {
 		locationSwitch = (Switch) findViewById(R.id.swtLocation);
 		locationSwitch
 				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
 
 					@Override
 					public void onCheckedChanged(CompoundButton buttonView,
 							boolean isChecked) {
 						// TODO Auto-generated method stub
 						if (isChecked) {
 							LbsApplication.getLocationApi().startLocate(
 									LbsApplication.getLocationClient());
 							Toast.makeText(BaseActivity.this, "λģ",
 									Toast.LENGTH_SHORT).show();
 						} else {
 							LbsApplication.getLocationApi().stopLocate(
 									LbsApplication.getLocationClient());
 						}
 						LbsApplication.clearTrackingLayer();
 					}
 
 				});
 		locationSwitch.setOnTouchListener(new View.OnTouchListener() {
 
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				if (event.getAction() == MotionEvent.ACTION_DOWN) {
 					if (!LbsApplication.isNetWork()
							&& !LbsApplication.isGPSOpen()) {
 						Toast.makeText(BaseActivity.this, "ȴӻGPS",
 								Toast.LENGTH_SHORT).show();
 						return true;
 					}
 				}
 				return false;
 			}
 		});
 		if (LbsApplication.isLocateStart()) {
 			locationSwitch.setChecked(true);
 		} else {
 			locationSwitch.setChecked(false);
 		}
 	}
 
 	/*
 	 * right slideĻ
 	 */
 	public void initActivityRightSilder() {
 		final Bundle bundleDataBundle = new Bundle();
 		LinearLayout lectureLinear = (LinearLayout) findViewById(R.id.linearLecture);
 		LinearLayout playLinear = (LinearLayout) findViewById(R.id.linearPlay);
 		LinearLayout courseLinear = (LinearLayout) findViewById(R.id.linearCourse);
 		LinearLayout likeLinear = (LinearLayout) findViewById(R.id.linearLike);
 		lectureLinear.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				Intent intent = new Intent(BaseActivity.this,
 						ActivityListView.class);
 				bundleDataBundle.putString("Tab", "0");
 				intent.putExtras(bundleDataBundle);
 				startActivityForResult(intent, LbsApplication.getRequestCode());
 				BaseActivity.this.overridePendingTransition(
 						R.anim.in_right2left, R.anim.out_left2right);
 			}
 		});
 		playLinear.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				Intent intent = new Intent(BaseActivity.this,
 						ActivityListView.class);
 				bundleDataBundle.putString("Tab", "1");
 				intent.putExtras(bundleDataBundle);
 				startActivityForResult(intent, LbsApplication.getRequestCode());
 				BaseActivity.this.overridePendingTransition(
 						R.anim.in_right2left, R.anim.out_left2right);
 			}
 		});
 		courseLinear.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				Intent intent = new Intent(BaseActivity.this,
 						ActivityListView.class);
 				bundleDataBundle.putString("Tab", "2");
 				intent.putExtras(bundleDataBundle);
 				startActivityForResult(intent, LbsApplication.getRequestCode());
 				BaseActivity.this.overridePendingTransition(
 						R.anim.in_right2left, R.anim.out_left2right);
 			}
 		});
 		likeLinear.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				Intent intent = new Intent(BaseActivity.this,
 						ActivityListView.class);
 				bundleDataBundle.putString("Tab", "3");
 				intent.putExtras(bundleDataBundle);
 				startActivityForResult(intent, LbsApplication.getRequestCode());
 				BaseActivity.this.overridePendingTransition(
 						R.anim.in_right2left, R.anim.out_left2right);
 			}
 		});
 	}
 
 	/*
 	 * ûѯactionbar
 	 */
 	@SuppressWarnings("static-access")
 	public void initSearchBar() {
 		flagSearch = true;
 		actionbarLinLayout.removeAllViews();
 		View.inflate(BaseActivity.this, R.layout.searchbar, actionbarLinLayout);
 		SearchManager searchManager = (SearchManager) getSystemService(LbsApplication
 				.getContext().SEARCH_SERVICE);
 		SearchView searchView = (SearchView) findViewById(R.id.bufferSearch);
 		ImageView backImageView = (ImageView) findViewById(R.id.imgSearchBack);
 		searchView.setBackgroundColor(Color.parseColor("#000000"));
 		searchView.setSearchableInfo(searchManager
 				.getSearchableInfo(getComponentName()));
 		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
 
 			@Override
 			public boolean onQueryTextSubmit(String query) {
 				LbsApplication.setQueryString(query);
 				Intent intent = new Intent(getApplicationContext(),
 						BufferQueryResult.class);
 				if (LbsApplication.isLocateStart())
 					intent.setAction(LbsApplication.QUERY_WITH_LOCATION_FLAG);
 				intent.putExtra("QueryString", query);
 				startActivityForResult(intent,
 						LbsApplication.getBufferQueryCode());
 				BaseActivity.this.overridePendingTransition(
 						R.anim.in_right2left, R.anim.out_left2right);
 				return true;
 
 			}
 
 			@Override
 			public boolean onQueryTextChange(String newText) {
 				// TODO Auto-generated method stub
 				return false;
 			}
 		});
 		searchView.setOnCloseListener(new SearchView.OnCloseListener() {
 
 			@Override
 			public boolean onClose() {
 				return true;
 			}
 		});
 		backImageView.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				if (LbsApplication.isImeShow(lbsApplication.getContext()))
 					LbsApplication.hideIme(BaseActivity.this);
 				actionbarLinLayout.removeAllViews();
 				View.inflate(BaseActivity.this, R.layout.actionbar,
 						actionbarLinLayout);
 				initMainBar();
 			}
 		});
 	}
 
 	/*
 	 * òѯactionbar
 	 */
 	public void initResultBar(final String flag) {
 		ImageView backImageView = (ImageView) findViewById(R.id.imgBackHome);
 		resultSwitch = (Switch) findViewById(R.id.swtQuery);
 		resultSwitch.setTextColor(Color.parseColor("#000000"));
 		backImageView.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				flagSearch = false;
 				isSearch = false;
 				LbsApplication.clearCallout();
 				if (!results.isEmpty()) {
 					results = new ArrayList<Result>();
 				}
 				Intent intent = new Intent(LbsApplication.getContext(),
 						HomeActivity.class);
 				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 				intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
 				startActivity(intent);
 				if (flag.equals("list")) {
 					BaseActivity.this.overridePendingTransition(
 							R.anim.in_left2right, R.anim.out_left2right);
 				}
 			}
 		});
 		resultSwitch
 				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
 
 					@Override
 					public void onCheckedChanged(CompoundButton buttonView,
 							boolean isChecked) {
 						if (isChecked) {
 							if (flag.equals("map")) {
 								Intent intent = new Intent(
 										getApplicationContext(),
 										BufferQueryResult.class);
 								if (LbsApplication.isLocateStart())
 									intent.setAction(LbsApplication.QUERY_WITH_LOCATION_FLAG);
 								intent.putExtra("QueryString",
 										LbsApplication.getQueryString());
 								startActivityForResult(intent,
 										LbsApplication.getBufferQueryCode());
 								BaseActivity.this.overridePendingTransition(
 										R.anim.in_right2left,
 										R.anim.out_left2right);
 							}
 						} else {
 							if (flag.equals("list")) {
 								Intent intent = new Intent(LbsApplication
 										.getContext(), HomeActivity.class);
 								intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 								intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
 								if (BufferQueryResult.getResults() != null) {
 									Bundle bundle = new Bundle();
 									bundle.putParcelableArrayList(
 											"results",
 											(ArrayList<Result>) BufferQueryResult
 													.getResults());
 									intent.putExtras(bundle);
 								}
 								setResult(LbsApplication.getRequestCode(),
 										intent);
 								finish();
 								BaseActivity.this.overridePendingTransition(
 										R.anim.in_left2right,
 										R.anim.out_left2right);
 							}
 						}
 					}
 				});
 		if (flag.equals("map")) {
 			resultSwitch.setChecked(false);
 		}
 	}
 
 	/*
 	 * У԰ͼ
 	 */
 	public void initPhotoSwitch() {
 		photoSwitch = (Switch) findViewById(R.id.swtPhoto);
 		photoSwitch
 				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
 
 					@Override
 					public void onCheckedChanged(CompoundButton buttonView,
 							boolean isChecked) {
 						// TODO Auto-generated method stub
 						if (isChecked) {
 							if (isPopUp) {
 								LbsApplication.clearCallout();
 								locationViewPopup(-LbsApplication.Dp2Px(
 										BaseActivity.this, 50), 0,
 										mapRelativeLayout);
 								isPopUp = false;
 								hasDetail = false;
 							}
 							userImageView
 									.setImageResource(R.drawable.ic_action_back);
 							searchLinLayout.setVisibility(View.GONE);
 							moreImageView.setVisibility(View.GONE);
 							locationRelLayout.setVisibility(View.GONE);
 							final DatasetVector mDatasetVector = (DatasetVector) LbsApplication
 									.getmMapControl().getMap().getLayers()
 									.get(16).getDataset();
 							queryViaSuperMap = new Query();
 							queryViaSuperMap.addPhotoBubble(mDatasetVector,
 									BaseActivity.this);
 							Toast.makeText(BaseActivity.this, "У԰У԰",
 									Toast.LENGTH_SHORT).show();
 						} else {
 							userImageView
 									.setImageResource(R.drawable.ic_action_user);
 							searchLinLayout.setVisibility(View.VISIBLE);
 							moreImageView.setVisibility(View.VISIBLE);
 							locationRelLayout.setVisibility(View.VISIBLE);
 							LbsApplication.clearCallout();
 							Toast.makeText(BaseActivity.this, "رУ԰",
 									Toast.LENGTH_SHORT).show();
 						}
 						LbsApplication.refreshMap();
 						simpleSideDrawer.toggleLeftDrawer();
 					}
 				});
 	}
 
 	/*
 	 * λϸ
 	 */
 	public void locationViewPopup(final float p1, final float p2,
 			final RelativeLayout view) {
 		TranslateAnimation animation = new TranslateAnimation(0, 0, p1, p2);
 		animation.setInterpolator(new AnticipateOvershootInterpolator());
 		animation.setDuration(500);
 		animation.setAnimationListener(new Animation.AnimationListener() {
 			@Override
 			public void onAnimationStart(Animation animation) {
 				locationRelLayout.setEnabled(false);
 			}
 
 			@Override
 			public void onAnimationRepeat(Animation animation) {
 			}
 
 			@Override
 			public void onAnimationEnd(Animation animation) {
 				view.setVisibility(View.GONE);
 				view.clearAnimation();
 				view.getLayoutParams().height = view.getBottom()
 						+ (int) (p2 - p1);
 				view.setVisibility(View.VISIBLE);
 				locationRelLayout.setEnabled(true);
 			}
 		});
 		view.startAnimation(animation);
 	}
 }
