 package com.ipbox.activites;
 
 import android.content.ActivityNotFoundException;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.Uri;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Parcelable;
 import android.preference.PreferenceManager;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentTransaction;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.widget.Toast;
 import com.ipbox.Const;
 import com.ipbox.IpBoxApp;
 import com.ipbox.R;
 import com.ipbox.actionbarcompat.ActionBarActivity;
 import com.ipbox.fragments.ChannelsFragment;
 import com.ipbox.fragments.PlaylistDialog;
 import com.ipbox.playlist.Channel;
 
 /**
  * User: gsd
  * Date: 5/29/12
  * Time: 10:12 AM
  */
 public class BaseActivity extends ActionBarActivity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
 		setPlaylist(preferences.getInt(Const.PREFERENCE_LAST_PLAYLIST, 0));
		if (preferences.getString(Const.PREFERENCE_PLAYER_THEME, "White").equals("Black"))
			setTheme(R.style.BlackBoxTheme);
		else
 			setTheme(R.style.BoxTheme);
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater menuInflater = getMenuInflater();
 		menuInflater.inflate(R.menu.main, menu);
 
 		// Calling super after populating the menu is necessary here to ensure that the
 		// action bar helpers have a chance to handle this event.
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 			case android.R.id.home:
 				Toast.makeText(this, "Tapped home", Toast.LENGTH_SHORT).show();
 				break;
 
 			case R.id.menu_refresh:
 				Toast.makeText(this, "Fake refreshing...", Toast.LENGTH_SHORT).show();
 				/*getActionBarHelper().setRefreshActionItemState(true);
 				getWindow().getDecorView().postDelayed(
 					new Runnable() {
 						@Override
 						public void run() {
 							getActionBarHelper().setRefreshActionItemState(false);
 						}
 					}, 1000); */
 				break;
 
 			/*case R.id.menu_search:
 				Toast.makeText(this, "Tapped search", Toast.LENGTH_SHORT).show();
 				break;*/
 
 			case R.id.menu_preferences:
 				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
 					startActivity(new Intent(this, IpBoxPreference.class));
 				} else {
 					startActivity(new Intent(this, IpBoxPreferenceHc.class));
 				}
 				break;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	public void loadChannel(Channel channel) {
 		Toast.makeText(this, R.string.alert_loading, Toast.LENGTH_SHORT).show();
 		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
 		try{
 			loadChannel(channel, preferences.getString("playerType", "system"));
 		} catch (Exception ex){
 			Toast.makeText(this, R.string.fail_loading, Toast.LENGTH_SHORT).show();
 		}
 	}
 
 	protected void showPlaylistDialog() {
 		//mStackLevel++;
 
 		// DialogFragment.show() will take care of adding the fragment
 		// in a transaction.  We also want to remove any currently showing
 		// dialog, so make our own transaction and take care of that here.
 		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
 		Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
 		if (prev != null) {
 			ft.remove(prev);
 		}
 		ft.addToBackStack(null);
 
 		ChannelsFragment details = (ChannelsFragment) getSupportFragmentManager().findFragmentById(R.id.titles);
 		// Create and show the dialog.
 		PlaylistDialog newFragment = PlaylistDialog.newInstance(this);
 		newFragment.show(ft, "dialog");
 	}
 
 	protected void loadChannel(Channel channel, String playerType) {
 		if (playerType.equals("MX Player") || playerType.equals("MX Player Pro")) {
 			loadMx(channel, playerType.contains("Pro"));
 			return;
 		}
 		if (playerType.equals("Daroon")) {
 			loadDaroon(channel);
 			return;
 		}
 		loadSystem(channel);
 
 	}
 
 	protected void loadDaroon(Channel channel) {
 		Intent viewMediaIntent = new Intent();
 		viewMediaIntent.setAction(android.content.Intent.ACTION_VIEW);
 		viewMediaIntent.setDataAndType(Uri.parse(channel.getUrl()), "video/*");
 		viewMediaIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
 		viewMediaIntent.setPackage("com.daroonplayer.dsplayer");
 		startActivityForResult(viewMediaIntent, 0);
 	}
 
 	protected void loadSystem(Channel channel) {
 		Intent viewMediaIntent = new Intent();
 		viewMediaIntent.setAction(android.content.Intent.ACTION_VIEW);
 		viewMediaIntent.setDataAndType(Uri.parse(channel.getUrl()), "video/*");
 		viewMediaIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
 		startActivityForResult(viewMediaIntent, 0);
 	}
 
 
 	public static final String MXVP = "com.mxtech.videoplayer.ad";
 	public static final String MXVP_PRO = "com.mxtech.videoplayer.pro";
 
 	public static final String RESULT_VIEW = "com.mxtech.intent.result.VIEW";
 	public static final String EXTRA_DECODE_MODE = "decode_mode";        // (byte)
 	public static final String EXTRA_FAST_MODE = "fast_mode";                // (boolean)
 	public static final String EXTRA_VIDEO_LIST = "video_list";
 	public static final String EXTRA_SUBTITLES = "subs";
 	public static final String EXTRA_SUBTITLES_ENABLE = "subs.enable";
 	public static final String EXTRA_TITLE = "title";
 	public static final String EXTRA_POSITION = "position";
 	public static final String EXTRA_RETURN_RESULT = "return_result";
 
 	protected void loadMx(Channel channel, boolean isPro) {
 		Uri videoUri = Uri.parse(channel.getUrl());
 		Intent i = new Intent(Intent.ACTION_VIEW);
 
 		i.setDataAndType(videoUri, "video/*");
 		// play only given videos.
 		i.putExtra(EXTRA_VIDEO_LIST, new Parcelable[]{videoUri});
 		// provides title text.
 		i.putExtra(EXTRA_TITLE, channel.toString());
 		// request result
 		i.putExtra(EXTRA_RETURN_RESULT, true);
 
 		i.putExtra(EXTRA_POSITION, 0);
 
 		try {
 			if (isPro)
 				i.setPackage(MXVP_PRO);
 			else
 				i.setPackage(MXVP);
 			startActivityForResult(i, 0);
 		} catch (ActivityNotFoundException e) {
 			//TODO
 		}
 
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 
 	}
 
 	public void setPlaylist(int index) {
 		if( index < 0 || IpBoxApp.getPlayListsHolder().getPlaylists().size() < index)
 			index = 0;
 
 		// Check what fragment is shown, replace if needed.
 		ChannelsFragment channels = (ChannelsFragment) getSupportFragmentManager().findFragmentById(R.id.titles);
 		if (channels == null || channels.getShownIndex() != index) {
 			// Make new fragment to show this selection.
 			channels = ChannelsFragment.newInstance(index);
 
 			// Execute a transaction, replacing any existing
 			// fragment with this one inside the frame.
 			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
 			ft.replace(R.id.titles, channels);
 			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
 			ft.commit();
 		}
 	}
 
 	public void openPlaylist(int index) {
 		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
 		SharedPreferences.Editor editor = preferences.edit();
 		editor.putInt(Const.PREFERENCE_LAST_PLAYLIST, index);
 		editor.commit();
 		setPlaylist(index);
 	}
 }
