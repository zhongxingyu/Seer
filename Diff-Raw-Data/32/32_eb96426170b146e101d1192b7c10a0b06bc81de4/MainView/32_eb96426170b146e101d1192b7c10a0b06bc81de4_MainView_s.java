 package th.in.llun.thorfun;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Locale;
 
 import th.in.llun.thorfun.api.ApiResponse;
 import th.in.llun.thorfun.api.Thorfun;
 import android.app.Activity;
 import android.content.Context;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentStatePagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.EditText;
 
 public class MainView extends FragmentActivity {
 
 	public static final String PAGE_MAIN = "main";
 	public static final String PAGE_LOGIN = "login";
 	public static final String PAGE_MAIN_LOGGEDIN = "main_loggedin";
 
 	private String mPage;
 
 	/**
 	 * The {@link ViewPager} that will host the section contents.
 	 */
 	ViewPager mViewPager;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		showMain();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		if (mPage == PAGE_MAIN) {
 			MenuInflater inflater = getMenuInflater();
 			inflater.inflate(R.menu.activity_main_menu, menu);
 			return true;
 		} else if (mPage == PAGE_MAIN_LOGGEDIN) {
 			MenuInflater inflater = getMenuInflater();
 			inflater.inflate(R.menu.activity_loggedin_menu, menu);
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.main_menu_login:
 			showLogin();
 			return true;
 		case R.id.main_menu_logout:
 			logout();
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	private void showLogin() {
 		setContentView(R.layout.activity_main_view_login);
 
 		mPage = PAGE_LOGIN;
 		invalidateOptionsMenu();
 
 		final Activity activity = this;
 
 		final EditText username = (EditText) findViewById(R.id.login_username_field);
 		final EditText password = (EditText) findViewById(R.id.login_password_field);
 		Button loginButton = (Button) findViewById(R.id.login_submit_button);
 		loginButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				Thorfun.getInstance(activity).login(username.getText().toString(),
 				    password.getText().toString(), new ApiResponse<String>() {
 
 					    @Override
 					    public void onResponse(String result) {
 						    try {
 							    InputMethodManager inputManager = (InputMethodManager) activity
 							        .getSystemService(Context.INPUT_METHOD_SERVICE);
 							    inputManager.hideSoftInputFromWindow(activity
 							        .getCurrentFocus().getWindowToken(),
 							        InputMethodManager.HIDE_NOT_ALWAYS);
 						    } catch (Exception e) {
 						    	Log.e(Thorfun.LOG_TAG, "Can't hide keybaord", e);
 						    }
						    showMain();
 					    }
 
 					    @Override
 					    public void onError(Exception exception) {
 						    Log.v(Thorfun.LOG_TAG, exception.getMessage(), exception);
 						    // Show login fail?
 					    }
 				    });
 			}
 		});
 		username.requestFocus();
 	}
 
 	private void showMain() {
 		setContentView(R.layout.activity_main_view);
 
 		if (Thorfun.getInstance(this).isLoggedIn()) {
 			mPage = PAGE_MAIN_LOGGEDIN;
 		} else {
 			mPage = PAGE_MAIN;
 		}
 
 		invalidateOptionsMenu();
 
 		// Set up the ViewPager with the sections adapter.
 		SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(
 		    getSupportFragmentManager(), Thorfun.getInstance(this));
 
 		mViewPager = (ViewPager) findViewById(R.id.pager);
 		mViewPager.setAdapter(sectionsPagerAdapter);
 		sectionsPagerAdapter.notifyDataSetChanged();
 
 	}
 
 	private void logout() {
 		final Activity self = this;
 		Thorfun.getInstance(this).logout(new ApiResponse<String>() {
 
 			@Override
 			public void onResponse(String result) {
 				mPage = PAGE_MAIN;
 				invalidateOptionsMenu();
 
 				// Set up the ViewPager with the sections adapter.
 				SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(
 				    getSupportFragmentManager(), Thorfun.getInstance(self));
 
 				mViewPager = (ViewPager) findViewById(R.id.pager);
 				mViewPager.setAdapter(sectionsPagerAdapter);
 				sectionsPagerAdapter.notifyDataSetChanged();
 			}
 
 			@Override
 			public void onError(Exception exception) {
 				// Handler error
 			}
 		});
 	}
 
 	public class SectionsPagerAdapter extends FragmentStatePagerAdapter {
 
 		private static final String KEY_TITLE = "title";
 		private static final String KEY_FRAGMENT = "fragment";
 
 		private Thorfun mThorfun;
 
 		private ArrayList<HashMap<String, Object>> mOptions = new ArrayList<HashMap<String, Object>>(
 		    3);
 
 		public SectionsPagerAdapter(FragmentManager fm, Thorfun thorfun) {
 			super(fm);
 			mThorfun = thorfun;
 
 			Locale l = Locale.getDefault();
 
 			mOptions.clear();
 			MyStoryFragment myStoryFragment = new MyStoryFragment();
 			HashMap<String, Object> myStory = new HashMap<String, Object>();
 			myStory.put(KEY_TITLE, getString(R.string.my_story_title).toUpperCase(l));
 			myStory.put(KEY_FRAGMENT, myStoryFragment);
 
 			StoryFragment storyFragment = new StoryFragment();
 			HashMap<String, Object> story = new HashMap<String, Object>();
 			story.put(KEY_TITLE, getString(R.string.story_title).toUpperCase(l));
 			story.put(KEY_FRAGMENT, storyFragment);
 
 			BoardFragment boardFragment = new BoardFragment();
 			HashMap<String, Object> board = new HashMap<String, Object>();
 			board.put(KEY_TITLE, getString(R.string.board_title).toUpperCase(l));
 			board.put(KEY_FRAGMENT, boardFragment);
 
 			if (mThorfun.isLoggedIn()) {
 				mOptions.add(myStory);
 			}
 			mOptions.add(story);
 			mOptions.add(board);
 		}
 
 		public void update() {
 
 		}
 
 		@Override
 		public Fragment getItem(int position) {
 			Log.d(Thorfun.LOG_TAG, "Get item: " + position);
 			return (Fragment) mOptions.get(position).get(KEY_FRAGMENT);
 		}
 
 		@Override
 		public int getCount() {
 			return mOptions.size();
 		}
 
 		@Override
 		public CharSequence getPageTitle(int position) {
 			return (String) mOptions.get(position).get(KEY_TITLE);
 		}
 	}
 
 }
