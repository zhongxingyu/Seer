 package com.chess.genesis;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.preference.PreferenceManager;
 import android.util.TypedValue;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class MainMenu extends Activity implements OnClickListener, OnTouchListener
 {
 	private Context context;
 
 	private final Handler handle = new Handler()
 	{
 		public void handleMessage(final Message msg)
 		{
 			switch (msg.what) {
 			case LogoutConfirm.MSG:
 				final Editor pref = PreferenceManager.getDefaultSharedPreferences(context).edit();
 
 				pref.putBoolean("isLoggedIn", false);
 				pref.putString("username", "!error!");
 				pref.putString("passhash", "!error!");
 				pref.putLong("lastgamesync", 0);
 				pref.putLong("lastmsgsync", 0);
 				pref.commit();
 
 				updateLoggedInView();
 				break;
 			}
 		}
 	};
 
 	@Override
 	public void onCreate(final Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		context = this;
 
 		// run upgrade
 		UpgradeHandler.run(this);
 
 		// set only portrait
 		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 
 		// set content view
 		setContentView(R.layout.activity_mainmenu);
 
 		// setup click listeners
 		final int list[] = new int[]{R.id.local_game, R.id.online_game,
 			R.id.user_stats, R.id.howtoplay, R.id.likefacebook,
 			R.id.login, R.id.settings, R.id.feedback, R.id.googleplus};
 		for (int i = 0; i < list.length; i++) {
 			final ImageView button = (ImageView) findViewById(list[i]);
 			button.setOnClickListener(this);
 			button.setOnTouchListener(this);
 		}
 
 		resizeButtonText();
 	}
 
 	@Override
 	public void onResume()
 	{
 		super.onResume();
 		updateLoggedInView();
 		AdsHandler.run(this);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(final Menu menu)
 	{
 		getMenuInflater().inflate(R.menu.options_mainmenu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(final MenuItem item)
 	{
		if (item.getItemId() == R.id.logout) {
			(new LogoutConfirm(this, handle)).show();
			return true;
 		}
		return super.onOptionsItemSelected(item);
 	}
 
 	@Override
 	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data)
 	{
 		if (resultCode == RESULT_CANCELED)
 			return;
 
 		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
 
 		if (requestCode == Enums.ONLINE_LIST) {
 			startActivity(new Intent(this, GameListOnline.class));
 		} else if (requestCode == Enums.USER_STATS) {
 			final Intent intent = new Intent(this, UserStats.class);
 			intent.putExtra("username", pref.getString("username", "!error!"));
 			startActivity(intent);
 		}
 	}
 
 	public boolean onTouch(final View v, final MotionEvent event)
 	{
 		switch (v.getId()) {
 		case R.id.local_game:
 		case R.id.online_game:
 		case R.id.user_stats:
 		case R.id.howtoplay:
 		case R.id.login:
 		case R.id.settings:
 		case R.id.feedback:
 			if (event.getAction() == MotionEvent.ACTION_DOWN)
 				v.setBackgroundColor(0xff00b7eb);
 			else if (event.getAction() == MotionEvent.ACTION_UP)
 				v.setBackgroundColor(0x00ffffff);
 			break;
 		}
 		return false;
 	}
 
 	public void onClick(final View v)
 	{
 		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
 		Intent intent;
 
 		switch (v.getId()) {
 		case R.id.local_game:
 			startActivity(new Intent(this, GameListLocal.class));
 			break;
 		case R.id.online_game:
 			if (!pref.getBoolean("isLoggedIn", false)) {
 				startActivityForResult(new Intent(this, Login.class), Enums.ONLINE_LIST);
 				return;
 			}
 			intent = new Intent(this, GameListOnline.class);
 			startActivity(intent);
 			break;
 		case R.id.user_stats:
 			if (!pref.getBoolean("isLoggedIn", false)) {
 				startActivityForResult(new Intent(this, Login.class), Enums.USER_STATS);
 				return;
 			}
 			intent = new Intent(this, UserStats.class);
 			intent.putExtra("username", pref.getString("username", "!error!"));
 			startActivity(intent);
 			break;
 		case R.id.howtoplay:
 			Uri uri = Uri.parse("http://goo.gl/eyLYY");
 			startActivity(new Intent(Intent.ACTION_VIEW, uri));
 			break;
 		case R.id.likefacebook:
 			uri = Uri.parse("http://goo.gl/tQVOh");
 			startActivity(new Intent(Intent.ACTION_VIEW, uri));
 			break;
 		case R.id.googleplus:
 			uri = Uri.parse("https://plus.google.com/110270264298991399402");
 			startActivity(new Intent(Intent.ACTION_VIEW, uri));
 			break;
 		case R.id.feedback:
 			uri = Uri.parse(getResources().getString(R.string.feedback_url));
 			startActivity(new Intent(Intent.ACTION_VIEW, uri));
 			break;
 		case R.id.settings:
 			startActivity(new Intent(this, Settings.class));
 			break;
 		case R.id.login:
 			startActivity(new Intent(this, Login.class));
 			break;
 		}
 	}
 
 	private void resizeButtonText()
 	{
 		final int list[] = new int[]{R.id.local_game_txt, R.id.online_game_txt,
 			R.id.archive_game_txt, R.id.howtoplay_txt, R.id.login_txt,
 			R.id.settings_txt, R.id.feedback_txt};
 
 		String[] stList = new String[list.length];
 		TextView txt = null;
 		for (int i = 0; i < list.length; i++) {
 			txt = (TextView) findViewById(list[i]);
 			stList[i] = (String) txt.getText();
 		}
 
 		float width = getWindowManager().getDefaultDisplay().getWidth();
 		width = Math.min(getWindowManager().getDefaultDisplay().getHeight(), width);
 		width *= 0.9 / 3;
 
 		final float txtSize = RobotoText.maxTextWidth(stList, txt.getPaint(), width);
 		for (int i = 0; i < list.length; i++) {
 			txt = (TextView) findViewById(list[i]);
 			txt.setTextSize(TypedValue.COMPLEX_UNIT_PX, txtSize);
 		}
 	}
 
 	private void updateLoggedInView()
 	{
 		String welcome = "";
 		int welcomeVis = View.GONE, loginVis = View.VISIBLE;
 
 		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
 		if (pref.getBoolean("isLoggedIn", false)) {
 			welcome = "Welcome " + pref.getString("username", "");
 			welcomeVis = View.VISIBLE;
 			loginVis = View.GONE;
 		}
 
 		TextView text = (TextView) findViewById(R.id.welcome);
 		text.setVisibility(welcomeVis);
 		text.setText(welcome);
 
 		text = (TextView) findViewById(R.id.login_txt);
 		text.setVisibility(loginVis);
 
 		final MyImageView button = (MyImageView) findViewById(R.id.login);
 		button.setVisibility(loginVis);
 	}
 }
