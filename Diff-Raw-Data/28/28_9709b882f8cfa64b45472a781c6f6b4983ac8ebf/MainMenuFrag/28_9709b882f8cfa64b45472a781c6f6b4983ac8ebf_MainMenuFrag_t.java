 package com.chess.genesis;
 
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.preference.PreferenceManager;
 import android.support.v4.app.Fragment;
 import android.util.TypedValue;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.LayoutInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
 import android.widget.TextView;
 import android.widget.Toast;
 
public class MainMenuFrag extends BaseContentFrag implements OnClickListener, OnTouchListener, OnGlobalLayoutListener
 {
 	public final static String TAG = "MAINMENU";
 
 	public final Handler handle = new Handler()
 	{
 		public void handleMessage(final Message msg)
 		{
 			switch (msg.what) {
 			case LogoutConfirm.MSG:
 				final Editor pref = PreferenceManager.getDefaultSharedPreferences(act).edit();
 
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
 	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
 	{
 		initBaseContentFrag();
 
 		final View view = inflater.inflate(R.layout.fragment_mainmenu, container, false);
 
 		// setup click listeners
 		final int list[] = new int[]{R.id.local_game, R.id.online_game,
 			R.id.user_stats, R.id.howtoplay, R.id.likefacebook,
 			R.id.login, R.id.settings, R.id.feedback, R.id.googleplus};
 		for (int i = 0; i < list.length; i++) {
 			final View button = view.findViewById(list[i]);
 			button.setOnClickListener(this);
 			button.setOnTouchListener(this);
 		}

		// create layout listner for resizing the button text
		final View v = view.findViewById(R.id.online_game);
		final ViewTreeObserver vto = v.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(this);

 		return view;
 	}
 
 	@Override
	public void onGlobalLayout()
	{
		resizeButtonText(getView());

		// remove layout listener once text was resized
		final View v = getView().findViewById(R.id.online_game);
		final ViewTreeObserver vto = v.getViewTreeObserver();
		vto.removeGlobalOnLayoutListener(this);
	}

	@Override
 	public void onResume()
 	{
 		super.onResume();
 		updateLoggedInView();
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
 		switch (v.getId()) {
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
 			startActivity(new Intent(act, Settings.class));
 			break;
 		case R.id.menu:
 			openMenu(v);
 			break;
 		default:
 			final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(act);
 			if (isTablet)
 				onClickTablet(v.getId(), pref);
 			else
 				onClickPhone(v.getId(), pref);
 			break;
 		}
 	}
 
 	private void onClickPhone(final int viewId, final SharedPreferences pref)
 	{
 		Intent intent;
 
 		switch (viewId) {
 		case R.id.local_game:
 			startActivity(new Intent(act, GameListLocal.class));
 			break;
 		case R.id.online_game:
 			if (!pref.getBoolean("isLoggedIn", false)) {
 				startActivityForResult(new Intent(act, Login.class), Enums.ONLINE_LIST);
 				return;
 			}
 			startActivity(new Intent(act, GameListOnline.class));
 			break;
 		case R.id.user_stats:
 			if (!pref.getBoolean("isLoggedIn", false)) {
 				startActivityForResult(new Intent(act, Login.class), Enums.USER_STATS);
 				return;
 			}
 			intent = new Intent(act, UserStats.class);
 			intent.putExtra("username", pref.getString("username", "!error!"));
 			startActivity(intent);
 			break;
 		case R.id.login:
 			startActivity(new Intent(act, Login.class));
 			break;
 		}
 	}
 
 	private void onClickTablet(final int viewId, final SharedPreferences pref)
 	{
 		// pop everything from stack
 		for (int i = 0; i < fragMan.getBackStackEntryCount(); i++)
 			fragMan.popBackStack();
 
 		final FragmentIntent fintent = new FragmentIntent();
 		switch (viewId) {
 		case R.id.local_game:
 			fintent.setFrag(R.id.panel01, new GameListLocalFrag(), GameListLocalFrag.TAG);
 			break;
 		case R.id.online_game:
 			if (!pref.getBoolean("isLoggedIn", false)) {
 				final LoginFrag frag = new LoginFrag();
 				frag.setCallBack(Enums.ONLINE_LIST);
 				fintent.setFrag(R.id.panel02, frag, LoginFrag.TAG);
 			} else {
 				fintent.setFrag(R.id.panel01, new GameListOnlineFrag(), GameListOnlineFrag.TAG);
 			}
 			break;
 		case R.id.user_stats:
 			if (!pref.getBoolean("isLoggedIn", false)) {
 				final LoginFrag frag = new LoginFrag();
 				frag.setCallBack(Enums.USER_STATS);
 				fintent.setFrag(R.id.panel02, frag, LoginFrag.TAG);
 			} else {
 				final BaseContentFrag frag = new UserStatsFrag();
 				final Bundle bundle = new Bundle();
 				bundle.putString("username", pref.getString("username", "!error!"));
 				frag.setArguments(bundle);
 
 				fintent.setFrag(R.id.panel02, frag, UserStatsFrag.TAG);
 			}
 			break;
 		case R.id.login:
 			fintent.setFrag(R.id.panel02, new LoginFrag(), LoginFrag.TAG);
 			break;
 		}
 		fintent.loadFrag(fragMan);
 	}
 
 	@Override
 	public void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo)
 	{
 		super.onCreateContextMenu(menu, v, menuInfo);
 		act.lastContextMenu = TAG;
 
 		act.getMenuInflater().inflate(R.menu.options_mainmenu, menu);
 	}
 
 	@Override
 	public boolean onContextItemSelected(final MenuItem item)
 	{
 		if (act.lastContextMenu == TAG)
 			return onOptionsItemSelected(item);
 		else
 			return super.onContextItemSelected(item);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(final MenuItem item)
 	{
 		switch (item.getItemId()) {
 		case R.id.logout:
 			final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(act);
 			if (pref.getBoolean("isLoggedIn", false))
 				(new LogoutConfirm(act, handle)).show();
 			else
 				Toast.makeText(act, "Already Logged Out", Toast.LENGTH_LONG).show();
 			break;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 		return true;
 	}
 
 	public void startFragment(final int fragId)
 	{
 		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(act);
 		Fragment frag;
 		String fragTag;
 
 		fragMan.popBackStack();
 		switch (fragId) {
 		case Enums.ONLINE_LIST:
 			fragTag = GameListOnlineFrag.TAG;
 			frag = new GameListOnlineFrag();
 			break;
 		case Enums.USER_STATS:
 			fragTag = UserStatsFrag.TAG;
 			frag = new UserStatsFrag();
 			final Bundle bundle = new Bundle();
 			bundle.putString("username", pref.getString("username", "!error!"));
 			frag.setArguments(bundle);
 			break;
 		default:
 			updateLoggedInView();
 			return;
 		}
 		fragMan.beginTransaction()
 		.replace(R.id.panel01, frag, fragTag)
 		.addToBackStack(fragTag).commit();
 	}
 
 	private void resizeButtonText(final View view)
 	{
 		final int list[] = new int[]{R.id.local_game_txt, R.id.online_game_txt,
 			R.id.archive_game_txt, R.id.howtoplay_txt, R.id.login_txt,
 			R.id.settings_txt, R.id.feedback_txt};
 
 		String[] stList = new String[list.length];
 		TextView txt = null;
 		for (int i = 0; i < list.length; i++) {
 			txt = (TextView) view.findViewById(list[i]);
 			stList[i] = (String) txt.getText();
 		}
 
 		float width = view.getWidth();
 		width *= 0.9 / 3;
 
 		final float txtSize = RobotoText.maxTextWidth(stList, txt.getPaint(), width);
 		for (int i = 0; i < list.length; i++) {
 			txt = (TextView) view.findViewById(list[i]);
 			txt.setTextSize(TypedValue.COMPLEX_UNIT_PX, txtSize);
 		}
 	}
 
 	private void updateLoggedInView()
 	{
 		String welcome = "";
 		int welcomeVis = View.GONE, loginVis = View.VISIBLE;
 
 		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(act);
 		if (pref.getBoolean("isLoggedIn", false)) {
 			welcome = "Welcome " + pref.getString("username", "");
 			welcomeVis = View.VISIBLE;
 			loginVis = View.GONE;
 		}
 
 		TextView text = (TextView) act.findViewById(R.id.welcome);
 		text.setVisibility(welcomeVis);
 		text.setText(welcome);
 
 		text = (TextView) act.findViewById(R.id.login_txt);
 		text.setVisibility(loginVis);
 
 		final MyImageView button = (MyImageView) act.findViewById(R.id.login);
 		button.setVisibility(loginVis);
 	}
 }
