 package com.chess.genesis;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.preference.PreferenceManager;
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
 	private static MainMenu self;
 
 	private final Handler handle = new Handler()
 	{
 		public void handleMessage(final Message msg)
 		{
 			switch (msg.what) {
 			case LogoutConfirm.MSG:
 				final Editor pref = PreferenceManager.getDefaultSharedPreferences(self).edit();
 
 				pref.putBoolean("isLoggedIn", false);
 				pref.putString("username", "!error!");
 				pref.putString("passhash", "!error!");
 				pref.commit();
 
 				final TextView text = (TextView) findViewById(R.id.welcome);
 				text.setText("");
 
 				final ImageView button = (ImageView) findViewById(R.id.login);
 				button.setVisibility(View.VISIBLE);
 				break;
 			}
 		}
 	};
 
 	@Override
 	public void onCreate(final Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		self = this;
 
 		// set only portrait
 		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 
 		// set content view
 		setContentView(R.layout.mainmenu);
 
 		// setup click listeners
 		ImageView button = (ImageView) findViewById(R.id.local_game);
 		button.setOnClickListener(this);
 		button.setOnTouchListener(this);
 
 		button = (ImageView) findViewById(R.id.online_game);
 		button.setOnClickListener(this);
 		button.setOnTouchListener(this);
 
 		button = (ImageView) findViewById(R.id.archive_game);
 		button.setOnClickListener(this);
 		button.setOnTouchListener(this);
 
 		button = (ImageView) findViewById(R.id.howtoplay);
 		button.setOnClickListener(this);
 		button.setOnTouchListener(this);
 
 		button = (ImageView) findViewById(R.id.likefacebook);
 		button.setOnClickListener(this);
 		button.setOnTouchListener(this);
 
 		button = (ImageView) findViewById(R.id.login);
 		button.setOnClickListener(this);
 		button.setOnTouchListener(this);
 	}
 
 	@Override
 	public void onResume()
 	{
 		super.onResume();
 
 		String welcome = "";
 		int visible = View.VISIBLE;
 
 		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
 		if (pref.getBoolean("isLoggedIn", false)) {
 			welcome = "Welcome " + pref.getString("username", "");
 			visible = View.GONE;
 		}
 		final TextView text = (TextView) findViewById(R.id.welcome);
 		text.setText(welcome);
 
 		final MyImageView button = (MyImageView) findViewById(R.id.login);
 		button.setVisibility(visible);
 	}
 
 	@Override
 	public void onBackPressed()
 	{
 		moveTaskToBack(true);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(final Menu menu)
 	{
 		getMenuInflater().inflate(R.menu.mainmenu_options, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(final MenuItem item)
 	{
 		switch (item.getItemId()) {
 		case R.id.logout:
 			(new LogoutConfirm(this, handle)).show();
 			break;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 		return true;
 	}
 
 	@Override
 	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data)
 	{
 		if (resultCode == RESULT_OK) {
 			final Bundle bundle = new Bundle();
 			bundle.putInt("type", Enums.ONLINE_GAME);
 
 			final Intent intent = new Intent(this, GameList.class);
 			intent.putExtras(bundle);
 
 			startActivity(intent);
 		}
 	}
 
 	public boolean onTouch(final View v, final MotionEvent event)
 	{
 		switch (v.getId()) {
 		case R.id.local_game:
 			if (event.getAction() == MotionEvent.ACTION_DOWN)
 				((ImageView) v).setImageResource(R.drawable.localplay_pressed);
 			else if (event.getAction() == MotionEvent.ACTION_UP)
 				((ImageView) v).setImageResource(R.drawable.localplay);
 			break;
 		case R.id.online_game:
 			if (event.getAction() == MotionEvent.ACTION_DOWN)
 				((ImageView) v).setImageResource(R.drawable.onlinematches_pressed);
 			else if (event.getAction() == MotionEvent.ACTION_UP)
 				((ImageView) v).setImageResource(R.drawable.onlinematches);
 			break;
 		case R.id.archive_game:
 			if (event.getAction() == MotionEvent.ACTION_DOWN)
 				((ImageView) v).setImageResource(R.drawable.recordedgames_pressed);
 			else if (event.getAction() == MotionEvent.ACTION_UP)
 				((ImageView) v).setImageResource(R.drawable.recordedgames);
 			break;
 		case R.id.howtoplay:
 			if (event.getAction() == MotionEvent.ACTION_DOWN)
 				((ImageView) v).setImageResource(R.drawable.howtoplay_pressed);
 			else if (event.getAction() == MotionEvent.ACTION_UP)
 				((ImageView) v).setImageResource(R.drawable.howtoplay);
 			break;
 		case R.id.login:
 			if (event.getAction() == MotionEvent.ACTION_DOWN)
 				((ImageView) v).setImageResource(R.drawable.mainlogin_pressed);
 			else if (event.getAction() == MotionEvent.ACTION_UP)
 				((ImageView) v).setImageResource(R.drawable.mainlogin);
 			break;
 		case R.id.likefacebook:
 			if (event.getAction() == MotionEvent.ACTION_DOWN)
 				((ImageView) v).setImageResource(R.drawable.facebook_pressed);
 			else if (event.getAction() == MotionEvent.ACTION_UP)
 				((ImageView) v).setImageResource(R.drawable.facebook);
 			break;
 		}
 		return false;
 	}
 
 	public void onClick(final View v)
 	{
 		Bundle bundle;
 		Intent intent;
 
 		switch (v.getId()) {
 		case R.id.local_game:
 			bundle = new Bundle();
 			bundle.putInt("type", Enums.LOCAL_GAME);
 
 			intent = new Intent(this, GameList.class);
 			intent.putExtras(bundle);
 
 			startActivity(intent);
 			break;
 		case R.id.online_game:
 			final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
 
 			if (!pref.getBoolean("isLoggedIn", false)) {
 				startActivityForResult(new Intent(this, Login.class), 1);
 				return;
 			}
 			bundle = new Bundle();
 			bundle.putInt("type", Enums.ONLINE_GAME);
 
 			intent = new Intent(this, GameList.class);
 			intent.putExtras(bundle);
 
 			startActivity(intent);
 			break;
 		case R.id.archive_game:
 			bundle = new Bundle();
 			bundle.putInt("type", Enums.ARCHIVE_GAME);
 
 			intent = new Intent(this, GameList.class);
 			intent.putExtras(bundle);
 
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
 		case R.id.login:
 			startActivity(new Intent(this, Login.class));
 			break;
 		}
 	}
 }
