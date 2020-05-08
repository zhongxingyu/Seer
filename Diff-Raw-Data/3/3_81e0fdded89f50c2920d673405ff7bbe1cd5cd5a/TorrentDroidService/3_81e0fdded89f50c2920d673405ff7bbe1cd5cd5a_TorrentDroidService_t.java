 package org.utp.torrentdroid;
 
 import java.util.Comparator;
 import java.util.concurrent.ConcurrentSkipListSet;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 
 import org.utp.torrentdroid.exceptions.DuplicatedDownloadException;
 import org.utp.torrentdroid.impl.TTorrentDownload;
 import org.utp.torrentdroid.impl.TTorrentDownloadsManager;
 import org.utp.torrentdroid.interfaces.Download;
 import org.utp.torrentdroid.interfaces.DownloadStatus;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.os.Binder;
 import android.os.IBinder;
 import android.widget.Toast;
 
 /**
  * 
  * @author Maciej Laskowski
  * 
  */
 public class TorrentDroidService extends Service implements OnSharedPreferenceChangeListener {
 
 	private NotificationManager nm;
 
 	// Unique Identification Number for the Notification.
 	// We use it on Notification start, and to cancel it.
 	private int NOTIFICATION = R.string.service_started;
 
 	// Binder given to clients
 	private final IBinder mBinder = new LocalBinder();
 
 	// Thread safe collection with all downloads
 	private ConcurrentSkipListSet<TTorrentDownload> downloads;
 
 	// Downloads manager
 	TTorrentDownloadsManager dm;
 
 	// Download threads pool
 	ExecutorService dThreadPool;
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		return mBinder;
 	}
 
 	@Override
 	public void onCreate() {
 		nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
 		dm = TTorrentDownloadsManager.getInstance(getApplicationContext());
 		dThreadPool = Executors.newCachedThreadPool();
 		// Display a notification about us starting. We put an icon in the
 		// status bar.
 		showNotification();
 		// Init stored downloads
 		downloads = new ConcurrentSkipListSet<TTorrentDownload>(new DownloadComparator());
 		for (TTorrentDownload download : dm.getAllDownloads()) {
 			downloads.add(download);
 			// If application was closed unclean some downloads may have ACTIVE
 			// status.
 			if (download.getStatus().equals(DownloadStatus.ACTIVE)) {
 				download.setStatus(DownloadStatus.HOLD);
 			}
 		}
 		startHoldedDownloads(downloads);
 	}
 
 	@Override
 	public void onDestroy() {
 		// Stop all download threads and update downloads list in DB
 		for (TTorrentDownload download : downloads) {
 			// Set status on HOLD to break the thread and mark to be rerun after
 			// application restart.
 			download.setStatus(DownloadStatus.HOLD);
 			dm.updateDownload(download);
 		}
 		// Shutdown thread pool
 		dThreadPool.shutdown();
 		try {
 			dThreadPool.awaitTermination(5, TimeUnit.SECONDS);
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		// Cancel the persistent notification.
 		nm.cancel(NOTIFICATION);
 		// Tell user the app is stopped.
 		Toast.makeText(this, getText(R.string.app_closed), Toast.LENGTH_SHORT).show();
 
 	}
 
 	@Override
 	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
 		// TODO Auto-generated method stub
 	}
 
 	/* Methods for client */
 
 	/**
 	 * Create new download and start it.
 	 * 
 	 * @param torrentPath
 	 * @param destination
 	 * @throws DuplicatedDownloadException
 	 */
 	public void startNewDownload(String torrentPath, String destination) throws DuplicatedDownloadException {
 		TTorrentDownload download = dm.createDownload(torrentPath, destination);
 		downloads.add(download);
 		dThreadPool.execute(download);
 	}
 
 	/**
 	 * Stop download thread and remove it from DB and downloads list.
 	 * 
 	 * @param id
 	 */
 	public void removeDownload(Integer id) {
 		TTorrentDownload download = getDownloadWithId(id);
 		download.setStatus(DownloadStatus.PAUSE);
 		dm.removeDownload(id);
 		downloads.remove(download);
 	}
 
 	public void unpauseDownload(Integer id) {
 		TTorrentDownload download = getDownloadWithId(id);
 		download.setStatus(DownloadStatus.ACTIVE);
 		dThreadPool.execute(download);
 	}
 
 	public ConcurrentSkipListSet<TTorrentDownload> getAllDownloads() {
 		return downloads;
 	}
 
 	/* Private methods */
 
 	/**
 	 * Get download with specified id from downloads list.
 	 * 
 	 * @param id
 	 * @return {@link TTorrentDownload} object from list or new one if not
 	 *         found.
 	 */
 	private TTorrentDownload getDownloadWithId(Integer id) {
 		for (TTorrentDownload download : downloads) {
 			if (download.getId().equals(id))
 				return download;
 		}
 		return new TTorrentDownload();
 	}
 
 	/**
 	 * Start downloads with status HOLD.
 	 * 
 	 * @param downloads
 	 *            to start
 	 */
 	private void startHoldedDownloads(ConcurrentSkipListSet<TTorrentDownload> downloads) {
 		for (TTorrentDownload download : downloads) {
 			if (download.getStatus().equals(DownloadStatus.HOLD)) {
 				download.setStatus(DownloadStatus.ACTIVE);
 				dThreadPool.execute(download);
 			}
 		}
 	}
 
 	/**
 	 * Show a notification while this service is running.
 	 */
 	@SuppressWarnings("deprecation")
 	private void showNotification() {
 		// In this sample, we'll use the same text for the ticker and the
 		// expanded notification
 		CharSequence text = getText(R.string.service_started);
 
 		// The PendingIntent to launch our activity if the user selects this
 		// notification
 		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, TorrentDroidActivity.class),
 				0);
 
 		// Set the icon, scrolling text and timestamp
 		Notification notification = new Notification(R.drawable.ic_launcher, text, System.currentTimeMillis());
 
		// Make it persistent
		notification.flags = Notification.FLAG_ONGOING_EVENT;

 		// Set the info for the views that show in the notification panel.
 		notification.setLatestEventInfo(this, getText(R.string.service_label), text, contentIntent);
 
 		/*Notification notification = new Notification.Builder(this).setContentTitle(getText(R.string.service_label))
 				.setSmallIcon(R.drawable.ic_launcher).setContentIntent(contentIntent).build();*/
 
 		// Send the notification.
 		nm.notify(NOTIFICATION, notification);
 	}
 
 	/**
 	 * Class used for the client Binder.
 	 */
 	public class LocalBinder extends Binder {
 		TorrentDroidService getService() {
 			// Return this instance of LocalService so clients can call public
 			// methods
 			return TorrentDroidService.this;
 		}
 	}
 
 	/**
 	 * Comparator will sort downloads on list in ascending ids order.
 	 */
 	public class DownloadComparator implements Comparator<Download> {
 		@Override
 		public int compare(Download lhs, Download rhs) {
 			if (lhs.getId() < rhs.getId()) {
 				return -1;
 			} else if (lhs.getId() > rhs.getId()) {
 				return 1;
 			}
 			return 0;
 		}
 	}
 }
