 /**
 	This file is part of Personal Trainer.
 
     Personal Trainer is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     any later version.
 
     Personal Trainer is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with Personal Trainer.  If not, see <http://www.gnu.org/licenses/>.
 
     (C) Copyright 2012: Daniel Kvist, Henrik Hugo, Gustaf Werlinder, Patrik Thitusson, Markus Schutzer
  */
 
 package se.team05.service;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import se.team05.R;
 import se.team05.activity.ListExistingRoutesActivity;
 import se.team05.content.Track;
 import se.team05.listener.MediaServicePhoneStateListener;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.graphics.BitmapFactory;
 import android.media.MediaPlayer;
 import android.media.MediaPlayer.OnCompletionListener;
 import android.media.MediaPlayer.OnErrorListener;
 import android.media.MediaPlayer.OnPreparedListener;
 import android.net.Uri;
 import android.os.IBinder;
 import android.support.v4.app.NotificationCompat;
 import android.telephony.PhoneStateListener;
 import android.telephony.TelephonyManager;
 
 /**
  * This service class is used to play media from a device. It uses a media
  * player which is loaded and prepared asynchronously and also shows a
  * notification that the service is running as a foreground service to the user.
  * It uses a custom phone state listener to listen for the interesting events
  * from the system and then implements a custom Callbacks interface to be able
  * to receive information about the events.
  * 
  * It requires a playlist to be passed in as a string array list extra with
  * information about where the media it is supposed to play is located.
  * 
  * @author Daniel Kvist
  * 
  */
 public class MediaService extends Service implements OnCompletionListener, OnPreparedListener, OnErrorListener,
 		MediaServicePhoneStateListener.Callbacks
 {
 	public static final String ACTION_PLAY = "se.team05.service.action.PLAY";
 	public static final String DATA_PLAYLIST = "se.team05.service.data.PLAYLIST";
 
 	private MediaPlayer mediaPlayer;
 	private ArrayList<Track> playList;
 	private PhoneStateListener phoneStateListener;
 	private TelephonyManager telephonyManager;
 	private Track currentTrack;
 	private int currentTrackIndex;
 
 	private static final int NOTIFICATION_ID = 1;
 
 	/**
 	 * When the service is first created a new media player is created.
 	 */
 	@Override
 	public void onCreate()
 	{
 		mediaPlayer = new MediaPlayer();
 	}
 
 	/**
 	 * This is called by the system when the system is about to start. It gets a
 	 * reference to the telephony manager, creates a phone state listener,
 	 * initiates a notification and then loads and prepares the media player
 	 * asynchronously.
 	 */
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId)
 	{
 		telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
 		phoneStateListener = new MediaServicePhoneStateListener(this);
 		currentTrackIndex = 0;

		initNotification();

 		playList = intent.getParcelableArrayListExtra(DATA_PLAYLIST);
 		currentTrack = playList.get(currentTrackIndex);
 		initNotification();
 		
 		if (intent.getAction().equals(ACTION_PLAY))
 		{
 			if (!mediaPlayer.isPlaying())
 			{
 				mediaPlayer.setOnCompletionListener(this);
 				mediaPlayer.setOnErrorListener(this);
 				mediaPlayer.setOnPreparedListener(this);
 				initTrack(currentTrack);
 			}
 		}
 		return START_STICKY;
 	}
 
 	/**
 	 * Initiates a notification with the application launcher icon as a graphic
 	 * and custom messages for the ticker, title and text.
 	 * 
 	 */
 	private void initNotification()
 	{
 		Context context = getApplicationContext();
 		Intent notificationIntent = new Intent(context, ListExistingRoutesActivity.class);
 		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
 
 		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
 
 		Resources resources = context.getResources();
 		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
 
 		builder.setContentIntent(contentIntent).setSmallIcon(R.drawable.ic_launcher)
 				.setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_launcher)).setTicker(getString(R.string.your_route_is_being_recorded))
 				.setWhen(System.currentTimeMillis()).setOngoing(true).setContentTitle(currentTrack.getArtist()).setContentText(currentTrack.getTitle());
 		Notification notification = builder.getNotification();
 		
 		notificationManager.notify(NOTIFICATION_ID, notification);
 	}
 
 	/**
 	 * Cancels the notification and removes it from the notification area.
 	 */
 	private void cancelNotification()
 	{
 		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 		notificationManager.cancel(NOTIFICATION_ID);
 	}
 
 	/**
 	 * Called when the media player has been prepared and is ready to play. The
 	 * method then starts listening for phone state events and then starts
 	 * playing the media.
 	 */
 	@Override
 	public void onPrepared(MediaPlayer player)
 	{
 		telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
 		playMedia();
 	}
 
 	/**
 	 * Called when the phone is ringing and pauses the media.
 	 */
 	@Override
 	public void onRing()
 	{
 		pauseMedia();
 	}
 
 	/**
 	 * Called when the phone becomes idle and resumes playing of the media.
 	 */
 	@Override
 	public void onIdle()
 	{
 		playMedia();
 	}
 
 	/**
 	 * Starts playing the media if it is not already playing.
 	 */
 	public void playMedia()
 	{
 		if (!mediaPlayer.isPlaying())
 		{
 			mediaPlayer.start();
 		}
 	}
 
 	/**
 	 * Pauses the media if it is playing.
 	 */
 	public void pauseMedia()
 	{
 		if (mediaPlayer.isPlaying())
 		{
 			mediaPlayer.pause();
 		}
 
 	}
 
 	/**
 	 * Completely stops the media if it is playing.
 	 */
 	public void stopMedia()
 	{
 		if (mediaPlayer.isPlaying())
 		{
 			mediaPlayer.stop();
 		}
 	}
 
 	/**
 	 * Somethign has gone wrong, handle the error!
 	 * 
 	 */
 	@Override
 	public boolean onError(MediaPlayer mp, int what, int extra)
 	{
 		// TODO Handle error
 		// The MediaPlayer has moved to the Error state, must be reset!
 		return false;
 	}
 
 	/**
 	 * The media has reached the end, stop the media, and play the next track in
 	 * the playlist if there is one.
 	 */
 	@Override
 	public void onCompletion(MediaPlayer mp)
 	{
 		stopMedia();
 		if (currentTrackIndex < playList.size())
 		{
 			initTrack(playList.get(currentTrackIndex));
 		}
 	}
 
 	/**
 	 * Not currently used.
 	 */
 	@Override
 	public IBinder onBind(Intent intent)
 	{
 		return null;
 	}
 
 	/**
 	 * When the service is being destroyed we need to clean up so we release the
 	 * mediaplayer memory back to the system and remove the listener for the
 	 * phone state changes from the telephone manager. Finally we remove the
 	 * notification.
 	 */
 	@Override
 	public void onDestroy()
 	{
 		super.onDestroy();
 		if (mediaPlayer != null)
 		{
 			stopMedia();
 			mediaPlayer.release();
 		}
 
 		if (phoneStateListener != null)
 		{
 			telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
 		}
 
 		cancelNotification();
 	}
 
 	/**
 	 * Private helper method that plays the media from the given Track
 	 * 
 	 * @param track
 	 *            the track to play
 	 */
 	private void initTrack(Track track)
 	{
 		try
 		{
 			mediaPlayer.reset();
 			mediaPlayer.setDataSource(getApplicationContext(), Uri.parse(track.getData()));
 			mediaPlayer.prepareAsync();
 		}
 		catch (IllegalArgumentException e)
 		{
 			e.printStackTrace();
 		}
 		catch (IllegalStateException e)
 		{
 			e.printStackTrace();
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 		currentTrackIndex++;
 	}
 
 }
