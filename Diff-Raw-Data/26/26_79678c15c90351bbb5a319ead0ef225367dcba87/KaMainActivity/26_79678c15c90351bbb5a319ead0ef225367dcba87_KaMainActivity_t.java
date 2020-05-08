 package com.pengjun.ka.android.activity;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.widget.ImageButton;
 import android.widget.TextView;
 
 import com.pengjun.ka.R;
 import com.pengjun.ka.android.fragment.ArFragment;
 import com.pengjun.ka.android.fragment.ArSearchFragment;
 import com.pengjun.ka.android.fragment.FragmentDirector;
 import com.pengjun.ka.android.fragment.MagicBoxFragment;
 import com.pengjun.ka.android.fragment.SettingFragment;
 import com.pengjun.ka.utils.KaConstants;
import com.pengjun.ka.utils.ResourceUtils;
 
 public class KaMainActivity extends FragmentActivity {
 
 	private ImageButton ibAddAccount;
 	private ImageButton ibMenuTopLeft;
 
 	private ImageButton ibHome;
 	private ImageButton ibSearch;
 	private ImageButton ibMagicbox;
 	private ImageButton ibSettings;
 
 	private TextView tvTopTitle;
 
 	private State curState = State.Home;
 
 	enum State {
 		Home, Search, Report, Setting
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.keep_account);
 
 		// top bar
 		createTopBar();
 
 		// main content
 		FragmentDirector.replaceFragment(this, R.id.mainConent, ArFragment.newInstance());
 
 		// bottom bar
 		createBottomBar();
 
 	}
 
 	private void createTopBar() {
 
 		ibAddAccount = (ImageButton) findViewById(R.id.ibAddAccount);
 		ibAddAccount.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Intent intent = new Intent();
 				intent.setClass(KaMainActivity.this, AddArActivity.class);
 				startActivityForResult(intent, KaConstants.CB_ADD_AR);
 				overridePendingTransition(R.anim.left_in, R.anim.left_out);
 
 			}
 		});
 
 		tvTopTitle = (TextView) findViewById(R.id.tvTopTitle);
 
 		ibMenuTopLeft = (ImageButton) findViewById(R.id.ibMenuTopLeft);
 		ibMenuTopLeft.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				switch (curState) {
 				case Home:
 					ArFragment.newInstance().setListViewToTop();
 					break;
 				case Search:
 					ArSearchFragment.newInstance().clearAll();
 					break;
 				}
 
 			}
 		});
 	}
 
 	private void stateChange(State state, Fragment fragment) {
 		switch (state) {
 		case Home:
 			ibSearch.setBackgroundResource(R.drawable.search_normal);
 
 			ibAddAccount.setVisibility(View.VISIBLE);
 			ibMenuTopLeft.setBackgroundResource(R.drawable.menu_btn_to_top);
 			ibMenuTopLeft.setVisibility(View.VISIBLE);
 
 			tvTopTitle.setText(R.string.recentArs);
 			break;
 		case Search:
 			// the text in searchFragment could be focused
 			// so the process dependently when ibSearch icon change
 			ibSearch.setBackgroundResource(R.drawable.search_focused);
 
 			ibAddAccount.setVisibility(View.GONE);
 			ibMenuTopLeft.setVisibility(View.VISIBLE);
 			ibMenuTopLeft.setBackgroundResource(R.drawable.menu_btn_clear);
 
 			tvTopTitle.setText(R.string.searchAr);
 			break;
 		case Report:
 			ibSearch.setBackgroundResource(R.drawable.search_normal);
 
 			ibAddAccount.setVisibility(View.GONE);
 			ibMenuTopLeft.setVisibility(View.GONE);
 
 			tvTopTitle.setText(R.string.magicBox);
 			break;
 		case Setting:
 			ibSearch.setBackgroundResource(R.drawable.search_normal);
 
 			tvTopTitle.setText(R.string.systemSetting);
 			break;
 		}
 		FragmentDirector.replaceFragment(KaMainActivity.this, R.id.mainConent, fragment);
 
 	}
 
 	private void createBottomBar() {
 
 		ibHome = (ImageButton) findViewById(R.id.ibHome);
 		ibHome.setOnFocusChangeListener(new View.OnFocusChangeListener() {
 
 			@Override
 			public void onFocusChange(View v, boolean hasFocus) {
 				if (hasFocus) {
 					curState = State.Home;
 					stateChange(curState, ArFragment.newInstance());
 				}
 			}
 		});
 
 		ibSearch = (ImageButton) findViewById(R.id.ibSearch);
 		ibSearch.setBackgroundResource(R.drawable.search_normal);
 		ibSearch.setOnFocusChangeListener(new View.OnFocusChangeListener() {
 
 			@Override
 			public void onFocusChange(View v, boolean hasFocus) {
 				if (hasFocus) {
 					curState = State.Search;
 					stateChange(curState, ArSearchFragment.newInstance());
 				}
 			}
 		});
 
 		ibMagicbox = (ImageButton) findViewById(R.id.ibMagicbox);
 		ibMagicbox.setOnFocusChangeListener(new View.OnFocusChangeListener() {
 
 			@Override
 			public void onFocusChange(View v, boolean hasFocus) {
 				if (hasFocus) {
 					curState = State.Report;
 					stateChange(curState, MagicBoxFragment.newInstance());
 				}
 			}
 		});
 
 		ibSettings = (ImageButton) findViewById(R.id.ibSettings);
 		ibSettings.setOnFocusChangeListener(new View.OnFocusChangeListener() {
 
 			@Override
 			public void onFocusChange(View v, boolean hasFocus) {
 				if (hasFocus) {
 					curState = State.Setting;
 					stateChange(curState, SettingFragment.newInstance());
 				}
 			}
 		});
 
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == KaConstants.CB_ADD_AR) {
 			if (resultCode == RESULT_OK) {
 				ArFragment.newInstance().updateArListViewSync();
 			}
 		}
 		super.onActivityResult(requestCode, resultCode, data);
 	}
 
 	// the same as backing home operation
 	@Override
 	public void onBackPressed() {
 		Intent homeIntent = new Intent(Intent.ACTION_MAIN);
 		homeIntent.addCategory(Intent.CATEGORY_HOME);
 		startActivity(homeIntent);
 		// finish();
 	}
 
 	@Override
 	protected void onDestroy() {
 		// strange thing: reEnter app after close the app, instance still exist
 		ArFragment.newInstance().refresh();

		ResourceUtils.kaLogger.info("ka exit");
 		super.onDestroy();
 	}
 
 }
