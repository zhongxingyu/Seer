 package com.smp.funwithmusic.receivers;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import com.smp.funwithmusic.dataobjects.Song;
 
 import android.content.BroadcastReceiver;
 import static com.smp.funwithmusic.utilities.Constants.*;
 import static com.smp.funwithmusic.utilities.UtilityMethods.*;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.provider.MediaStore;
 import android.util.Log;
 import android.widget.Toast;
 
 public class SongReceiver extends BroadcastReceiver
 {
 
 	public String mAlbum;
 	public String mArtist;
 	public Context mContext;
 	public Intent mIntent;
 	public String mTitle;
 
 	@Override
 	public void onReceive(Context context, Intent intent)
 	{
		PendingResult result = goAsync();
 		setSongInfo(context, intent);
 		if (mAlbum != null && mArtist != null && mTitle != null)
 		{
 			Song song = new Song(mTitle, mArtist, mAlbum);
 			writeNewSong(context, song);
 
 			Intent send = new Intent();
 			send.setAction(SONG_ACTION)
 					.addCategory(Intent.CATEGORY_DEFAULT);
 			context.sendBroadcast(send);
 
 			Log.i("SONG", mArtist + " " + mTitle + " " + mAlbum);
 		}
		result.finish();
 
 	}
 
 	private void setSongInfo(Context context, Intent intent)
 	{
 		Log.d("SONG", "BLAH");
 		mContext = context;
 		mIntent = intent;
 		try
 		{
 			if ((intent.getAction().equals("com.htc.music.playstatechanged")) || (intent.getAction().equals("com.htc.music.metachanged")))
 			{
 				boolean bool1 = intent.getBooleanExtra("isplaying", false);
 				mArtist = intent.getStringExtra("artist");
 				mTitle = intent.getStringExtra("track");
 				mAlbum = intent.getStringExtra("album");
 
 			}
 
 			else if (intent.getAction().equals("com.htc.music.playbackcomplete"))
 			{
 
 			}
 			else if ((intent.getAction().equals("com.android.music.playstatechanged")) || (intent.getAction().equals("com.android.music.metachanged")))
 			{
 				boolean bool2 = intent.getBooleanExtra("playing", false);
 				mArtist = intent.getStringExtra("artist");
 				mTitle = intent.getStringExtra("track");
 				mAlbum = intent.getStringExtra("album");
 
 			}
 			else if (intent.getAction().equals("com.android.music.playbackcomplete"))
 			{
 
 			}
 			else if (intent.getAction().equals("fm.last.android.metachanged"))
 			{
 				mArtist = intent.getStringExtra("artist");
 				mTitle = intent.getStringExtra("track");
 				mAlbum = intent.getStringExtra("album");
 
 			}
 			else if ((intent.getAction().equals("fm.last.android.playbackpaused")) || (intent.getAction().equals("fm.last.android.playbackcomplete")))
 			{
 
 			}
 			else if ((intent.getAction().equals("com.real.RealPlayer.playstatechanged")) || (intent.getAction().equals("com.real.RealPlayer.metachanged")))
 			{
 				boolean bool3 = intent.getBooleanExtra("isplaying", false);
 				mArtist = intent.getStringExtra("artist");
 				mTitle = intent.getStringExtra("track");
 				mAlbum = intent.getStringExtra("album");
 
 			}
 			else if (intent.getAction().equals("com.real.RealPlayer.playbackcomplete"))
 			{
 
 			}
 			else if ((intent.getAction().equals("com.tbig.playerprotrial.playstatechanged")) || (intent.getAction().equals("com.tbig.playerprotrial.metachanged"))
 					|| (intent.getAction().equals("com.tbig.playerpro.playstatechanged")) || (intent.getAction().equals("com.tbig.playerpro.metachanged")))
 			{
 				boolean bool4 = intent.getBooleanExtra("playing", false);
 				mArtist = intent.getStringExtra("artist");
 				mTitle = intent.getStringExtra("track");
 				mAlbum = intent.getStringExtra("album");
 
 			}
 			else if ((intent.getAction().equals("com.tbig.playerprotrial.playbackcomplete")) || (intent.getAction().equals("com.tbig.playerpro.playbackcomplete")))
 			{
 
 			}
 			/*
 			 * else if (intent.getAction().equals(
 			 * "com.spotify.mobile.android.playbackstatechanged")) { if
 			 * (intent.getBooleanExtra("playing", false)) {
 			 * LyricsSettings.putBoolean(context, "spotifyPlaying", true);
 			 * mArtist = LyricsSettings.getString(context, "spotifyArtist");
 			 * mTitle = LyricsSettings.getString(context, "spotifyTitle");
 			 * mAlbum = LyricsSettings.getString(context, "spotifyAlbum"); if
 			 * ((mArtist != null) && (mTitle != null))
 			 * addNotificationIcon(context); } else {
 			 * LyricsSettings.putBoolean(context, "spotifyPlaying", false);
 			 * removeNotificationIcon(context); } } else if (intent.getAction
 			 * ().equals("com.spotify.mobile.android.metadatachanged")) {
 			 * boolean bool6 = LyricsSettings.getBoolean(context,
 			 * "spotifyPlaying"); mArtist = intent.getStringExtra("artist");
 			 * mTitle = intent.getStringExtra("track"); mAlbum =
 			 * intent.getStringExtra("album"); LyricsSettings.putString(context,
 			 * "spotifyArtist", intent.getStringExtra("artist"));
 			 * LyricsSettings.putString(context, "spotifyTitle",
 			 * intent.getStringExtra("track"));
 			 * LyricsSettings.putString(context, "spotifyAlbum",
 			 * intent.getStringExtra("album")); if (bool6)
 			 * addNotificationIcon(context); else
 			 * removeNotificationIcon(context); }
 			 */
 			else if ((intent.getAction().equals("com.adam.aslfms.notify.playstatechanged")) || (intent.getAction().equals("com.adam.aslfms.notify.metachanged")))
 			{
 				int i = intent.getIntExtra("state", 3);
 				if (i == 0)
 					return;
 
 				if (i == 1)
 					return;
 				mArtist = intent.getStringExtra("artist");
 				mTitle = intent.getStringExtra("track");
 				mAlbum = intent.getStringExtra("album");
 
 			}
 			else if (intent.getAction().equals("com.adam.aslfms.notify.playbackcomplete"))
 			{
 
 			}
 			else if (intent.getAction().equals("net.jjc1138.android.scrobbler.action.MUSIC_STATUS"))
 			{
 				boolean bool5 = intent.getBooleanExtra("playing", false);
 				if (intent.hasExtra("id"))
 				{
 					int k = intent.getIntExtra("id", 1);
 					Cursor localCursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, "_id=" + k, null, null);
 					localCursor.moveToFirst();
 					mArtist = localCursor.getString(localCursor.getColumnIndexOrThrow("artist"));
 					mTitle = localCursor.getString(localCursor.getColumnIndexOrThrow("title"));
 					mAlbum = localCursor.getString(localCursor.getColumnIndexOrThrow("album"));
 				}
 
 			}
 			else if (intent.getAction().equals("com.sonyericsson.music.playbackcontrol.ACTION_TRACK_STARTED"))
 			{
 				mArtist = intent.getStringExtra("ARTIST_NAME");
 				mTitle = intent.getStringExtra("TRACK_NAME");
 				mAlbum = intent.getStringExtra("ALBUM_NAME");
 
 			}
 			else if ((intent.getAction().equals("com.sonyericsson.music.playbackcontrol.ACTION_PAUSED")) || (intent.getAction().equals("com.sonyericsson.music.TRACK_COMPLETED")))
 			{
 
 			}
 			else
 			{
 				return;
 			}
 
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 
 	}
 
 	private void writeNewSong(Context context, Song song)
 	{
 		ArrayList<Song> songs = getSongList(context);
 		if (song != null && !songs.contains(song))
 		{
 			songs.add(song);
 			writeObjectToFile(context, SONG_FILE_NAME, songs);
 		}
 	}
 }
