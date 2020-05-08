 package org.pmix.ui;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import org.a0z.mpd.MPDStatus;
 import org.a0z.mpd.Music;
 import org.pmix.cover.CoverRetriever;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.BitmapDrawable;
 import android.os.Handler;
 import android.os.Message;
 import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
 import android.widget.ImageView;
 import android.widget.ViewSwitcher;
 
 public class MyHandler extends Handler implements ViewSwitcher.ViewFactory {
 
 	private MainMenuActivity mainMenuActivity = null;
 
 	private long lastKnownElapsedTime;
 
 	private int currentSongTime;
 
 	private String previousAlbum = "";
 
 	public int getCurrentSongTime() {
 		return currentSongTime;
 	}
 
 	public long getLastKnownElapsedTime() {
 		return lastKnownElapsedTime;
 	}
 
 	public MyHandler(MainMenuActivity mainMenuActivity) {
 		this.mainMenuActivity = mainMenuActivity;
 	}
 
 	private static String timeToString(long seconds) {
 		long min = seconds / 60;
 		long sec = seconds - min * 60;
 		return (min < 10 ? "0" + min : min) + ":" + (sec < 10 ? "0" + sec : sec);
 	}
 
 	@Override
 	public void handleMessage(Message msg) {
 
 		MPDStatus status = (MPDStatus) msg.obj;
 		int songId = status.getSongPos();
 		if (songId >= 0) {
 			try {
 
 				org.pmix.settings.Contexte.getInstance().getMpd().getPlaylist().refresh();
 
 				Music current = org.pmix.settings.Contexte.getInstance().getMpd().getPlaylist().getMusic(songId);
 				if (current != null) {
 
 					mainMenuActivity.getArtistNameText().setText(current.getArtist() != null ? current.getArtist() : "");
 					mainMenuActivity.getAlbumNameText().setText((current.getAlbum() != null ? (current.getAlbum()) : ""));
 					mainMenuActivity.getSongNameText().setText((current.getTitle() != null ? (current.getTitle()) : ""));
 
 					String album = current.getAlbum();
 
 					if (album != null && !previousAlbum.equals(album) && current.getArtist() != null) {
 						String url = CoverRetriever.getCoverUrl(current.getArtist(), current.getAlbum());
 						if (url != null)
 							downloadFile(url);
 						else
 							mainMenuActivity.getCoverSwitcher().setImageResource(R.drawable.gmpcnocover);
 
 					}
 					if (album == null) {
 						mainMenuActivity.getCoverSwitcher().setImageResource(R.drawable.gmpcnocover);
 					}
 
 					previousAlbum = album;
 
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 
 		if (status.getTotalTime() > 0) {
 			mainMenuActivity.getTrackTime().setText(timeToString(status.getElapsedTime()) + " - " + timeToString(status.getTotalTime()));
 		} else {
 			mainMenuActivity.getTrackTime().setText("");
 		}
 
 		lastKnownElapsedTime = status.getElapsedTime();
 		currentSongTime = (int) status.getTotalTime();
 		//mainMenuActivity.getProgressBar().setProgress(status.getVolume());
 		//mainMenuActivity.getProgressBarTrack().setProgress((int) (lastKnownElapsedTime * 100 / status.getTotalTime()));
 	}
 
 	public View makeView() {
 		ImageView i = new ImageView(mainMenuActivity);
 
 		i.setBackgroundColor(0x00FF0000);
 		i.setScaleType(ImageView.ScaleType.FIT_CENTER);
		i.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
    
 		return i;
 	}
 
 	Bitmap bmImg;
 
 	void downloadFile(String fileUrl) {
 		URL myFileUrl = null;
 		try {
 			myFileUrl = new URL(fileUrl);
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		try {
 			HttpURLConnection conn = (HttpURLConnection) myFileUrl.openConnection();
 			conn.setDoInput(true);
 			conn.connect();
 			int length = conn.getContentLength();
 
 			InputStream is = conn.getInputStream();
 
 			bmImg = BitmapFactory.decodeStream(is);
 			// new BitmapDrawable(bmImg);
			mainMenuActivity.getCoverSwitcher().setImageDrawable(new BitmapDrawable(bmImg));
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }
