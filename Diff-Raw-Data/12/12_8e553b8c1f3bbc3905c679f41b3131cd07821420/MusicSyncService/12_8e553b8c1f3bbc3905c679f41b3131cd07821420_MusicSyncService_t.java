 package com.we.android.music;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.media.MediaScannerConnection;
 import android.media.MediaScannerConnection.MediaScannerConnectionClient;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Binder;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.IBinder;
 import android.provider.MediaStore;
 import android.util.Log;
 
 public class MusicSyncService extends Service implements IMusicSyncControl {
 
     private class AsyncDownloader extends AsyncTask<Sync, Integer, Void> {
 
 	@Override
 	protected void onPreExecute() {
 	    mIsSyncing = true;
 	    showSyncStartedNotification();
 	    super.onPreExecute();
 	}
 
 	@Override
 	protected Void doInBackground(Sync... syncs) {
 	    HttpClient httpclient = new DefaultHttpClient();
 	    Sync sync = syncs[0];
 	    int completeFiles = 0;
 	    for (final SyncTask task : sync.getSyncTasks()) {
 		showSyncDownloadNotification(task.mFile);
 		String encoded = Constants.HOST + "/user/get/gerd/" + Uri.encode(task.mFile);
 		HttpGet httpget = new HttpGet(encoded);
		if (task.mDownloadedSize > 0) {
		    httpget.setHeader("Range", "bytes=" + task.mDownloadedSize + "-");
		}
 		try {
 		    HttpResponse response = httpclient.execute(httpget);
		    int statusCode = response.getStatusLine().getStatusCode();
		    if ((statusCode == HttpStatus.SC_OK) || (statusCode == HttpStatus.SC_PARTIAL_CONTENT)) {
 			HttpEntity entity = response.getEntity();
 			if (entity != null) {
 			    downloadEntity(entity, task);
 			    completeFiles++;
 			}
 			if (isCancelled()) break;
 		    } else {
 			Log.i(TAG,"download failed: " + response.getStatusLine().toString());
 		    }
 		} catch(Exception e) {
 		    Log.i(TAG,"HttpRequest not successful" + e.toString());
 		}
 	    }
 	    httpclient.getConnectionManager().shutdown();
 	    if (completeFiles == sync.getSyncTasks().size()) {
 		mSha1 = sync.getSHA1();
 	    }
 	    return null;
 	}
 
 	private void downloadEntity(HttpEntity entity, SyncTask task) {
 	    try {
 		File file = new File(mlocalSyncFolder, task.mFile);
 		FileOutputStream output = new FileOutputStream(file, true);
 		BufferedInputStream input = new BufferedInputStream(entity.getContent());
 		try {
 		    download(input, output, task.mDownloadedSize, task.mSize);
 		    updateMissingFiles(task, file);
 		} finally {
 		    output.close();
 		    input.close();
 		} 
 	    } catch (Exception e) {
 		Log.e(TAG, e.toString());
 	    }
 	}
 
 	private void updateMissingFiles(SyncTask task, File file) {
 	    mMissingFiles.remove(task.mFile);
 	    doMediaScan(file);
 	}
 
 	class MyMediaScannerConnectionClient implements MediaScannerConnectionClient {
 	    private MediaScannerConnection mConnection;
 	    private File mFile;
 
 	    public MyMediaScannerConnectionClient(File file) {
 		mFile = file;
 	    }
 
 	    @Override
 	    public void onMediaScannerConnected() {
 		mConnection.scanFile(mFile.toString(), null);
 	    }
 
 	    @Override
 	    public void onScanCompleted(String path, Uri uri) {
 		publishMissingFiles(mMissingFiles);
 	    }
 
 	    public void setMediaScannerConnection(MediaScannerConnection mediaScannerConnection) {
 		mConnection = mediaScannerConnection;
 	    }
 	}
 
 	private void doMediaScan(File file) {
 	    MyMediaScannerConnectionClient connectionClient = new MyMediaScannerConnectionClient(file);
 	    MediaScannerConnection mediaScannerConnection = 
 		new MediaScannerConnection(getApplicationContext(), connectionClient);
 	    connectionClient.setMediaScannerConnection(mediaScannerConnection);
 	    mediaScannerConnection.connect();
 	}
 
 	private void download(InputStream is, OutputStream os, long downloadedSize, long totalSize) throws IOException {
 	    byte[] buffer = new byte[5000];
 	    long counter = downloadedSize;
 	    int bytesRead = is.read(buffer);
 	    while (!isCancelled() && (bytesRead != -1)) {
 		counter += bytesRead;
 		//		if (counter > 1000000) break;
 		int percentPerFile = (int) ((counter / (float) totalSize) * 100);
 		publishProgress(percentPerFile);
 		os.write(buffer, 0, bytesRead);
 		bytesRead = is.read(buffer);
 	    }
 	    is.close();
 	    os.close();
 	}
 
 	@Override
 	protected void onProgressUpdate(Integer... values) {
 	    mMusicSyncListener.onProgressUpdate(values[0]);
 	}
 
 	@Override
 	protected void onPostExecute(Void result) {
 	    mIsSyncing = false;
 	    mMusicSyncListener.onSyncFinished();
 	    showSyncFinishedNotification();
 	}
     }
 
     class LocalBinder extends Binder {
 	public IMusicSyncControl getService() {
 	    return MusicSyncService.this;
 	}
     }
 
     private class Sync {
 	private String mSha1;
 	private List<SyncTask> mTasks;
 
 	public Sync(String sha1, List<SyncTask> tasks) {
 	    mSha1 = sha1;
 	    mTasks = tasks;
 	}
 
 	public String getSHA1() {
 	    return mSha1;
 	}
 
 	public List<SyncTask> getSyncTasks() {
 	    return mTasks;
 	}
 
 	public long getTotalDownloadSize() {
 	    long size = 0;
 	    for (SyncTask task : mTasks) {
 		size += task.mSize;
 	    }
 	    return size;
 	}
     }
 
     private class SyncTask {
 	String mFile;
 	long mSize;
 	long mDownloadedSize;
 
 	public SyncTask(String uri, long totalSize, long downloadedSize) {
 	    mFile = uri;
 	    mSize = totalSize;
 	    mDownloadedSize = downloadedSize;
 	}
     }
 
     private final LocalBinder mBinder = new LocalBinder();
     public static final String TAG = "MusicSync";
     private Handler mHandler = new Handler();
     private AsyncDownloader mDownloader;
     private File mlocalSyncFolder;
     private List<String> mMissingFiles = new ArrayList<String>();
     private boolean mIsSyncing;
     private static final long CYCLIC_CHECK = 60 * 1000;
     private String mSha1 = "undefined";
     private NotificationManager mNotificationManager;
     private static final int MUSIC_SYNC_NOTIFICATION = 0;
 
     private IMusicSyncListener mMusicSyncListener = NULL_SYNC_LISTENER;
     private static final IMusicSyncListener NULL_SYNC_LISTENER = new IMusicSyncListener() {
 	@Override
 	public void onSyncFinished() {
 	}
 
 	@Override
 	public void onProgressUpdate(int progress) {
 	}
 
 	@Override
 	public void onFilesMissing(List<String> missingFiles) {
 	}
     };
 
     private Runnable mSHA1Checker = new Runnable() {
 	@Override
 	public void run() {
 	    if (!mIsSyncing) {
 		HttpClient httpclient = new DefaultHttpClient();
 		String encoded = Constants.HOST + "/user/sha1/" + Constants.USER;
 		HttpGet httpget = new HttpGet(encoded);
 		try {
 		    HttpResponse response = httpclient.execute(httpget);
		    int statusCode = response.getStatusLine().getStatusCode();
		    if (statusCode == HttpStatus.SC_OK) {
 			HttpEntity entity = response.getEntity();
 			String sha1 = Util.convertStreamToString(entity.getContent());
 			if (!sha1.equals(mSha1)) {
 			    doSync(sha1);
 			}
 		    } else {
 			Log.i(TAG, "sha1 check failed: " + response.getStatusLine().toString());
 		    }
 		} catch(Exception e) {
 		    Log.i(TAG,"HttpRequest not successful" + e.toString());
 		}
 		httpclient.getConnectionManager().shutdown();
 	    }
 	    mHandler.postDelayed(mSHA1Checker, CYCLIC_CHECK);
 	}
     };
 
 
     public void onCreate() {
 	mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 	super.onCreate();
     }
 
     @Override
     public void onStart(Intent intent, int startId) {
 	mHandler.post(mSHA1Checker);
 	super.onStart(intent, startId);
     }
 
     private void doSync(String sha1) {
 	mlocalSyncFolder = new File(Environment.getExternalStorageDirectory(), Constants.SYNC_FOLDER);
 	if (!mlocalSyncFolder.exists()) {
 	    mlocalSyncFolder.mkdir();
 	}
 
 	if (!mIsSyncing) {
 	    Map<String, Long> localFiles = createLocalFileMap(getFilesRelativeToFolder(mlocalSyncFolder, Util.flattenDir(mlocalSyncFolder)));
 	    Map<String, Long> remoteFiles = createRemoteFilesMap(getSyncFolder(Constants.HOST + "/user/content/" + Constants.USER));
 	    List<String> listOfFilesToDelete = findMissingRemoteFiles(localFiles, remoteFiles);
 	    if (listOfFilesToDelete.size() > 0) {
 		deleteFiles(listOfFilesToDelete);
 	    }
 
 	    List<SyncTask> tasks = findMissingLocalFiles(localFiles, remoteFiles);
 	    if (tasks.size() > 0) {
 		mMissingFiles.clear();
 		for (SyncTask task : tasks) {
 		    mMissingFiles.add(task.mFile);
 		}
 		Sync sync = new Sync(sha1, tasks);
 		mDownloader = new AsyncDownloader();
 		mDownloader.execute(sync);
 	    }
 	}
     }
 
     private void deleteFiles(List<String> files) {
 	for (String file : files) {
 	    File fileToDelete = new File(mlocalSyncFolder, file);
 	    fileToDelete.delete();
 	    StringBuilder where = new StringBuilder();
 	    where.append(android.provider.MediaStore.Audio.Media.DATA +" LIKE '" + fileToDelete.getAbsolutePath() + "%'");
 	    getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, where.toString(), null);
 	}
     }
 
     private Map<String, Long> createRemoteFilesMap(JSONArray syncFolder) {
 	Map<String, Long> map = new HashMap<String, Long>();
 	for(int i=0; i<syncFolder.length();i++) {
 	    JSONObject fileInfo;
 	    try {
 		fileInfo = syncFolder.getJSONObject(i);
 		String fileName = fileInfo.getString("name");
 		long size = fileInfo.getInt("size");
 		map.put(fileName, size);
 	    } catch (JSONException e) {
 		e.printStackTrace();
 	    }
 	}
 	return map;
     }
 
     private Map<String, Long> createLocalFileMap(List<String> localFiles) {
 	Map<String, Long> map = new HashMap<String, Long>();
 	for (String fileName : localFiles) {
 	    File file = new File(mlocalSyncFolder, fileName);
 	    map.put(fileName, file.length());
 	}
 	return map;
     }
 
     @Override
     public void onDestroy() {
 	mHandler.removeCallbacks(mSHA1Checker);
 	super.onDestroy();
     }
 
     private List<String> getFilesRelativeToFolder(File folder, List<File> files) {
 	// remove leading "/"
 	int posPrefix = folder.getAbsolutePath().length() + 1;
 	List<String> relativeToSync = new ArrayList<String>();
 	for (File file : files) {
 	    String path = file.getAbsolutePath();
 	    if (posPrefix < path.length()) {
 		String relativeFileName = path.substring(posPrefix);
 		relativeToSync.add(relativeFileName);
 	    }
 	}
 	return relativeToSync;
     }
 
     private List<SyncTask> findMissingLocalFiles(Map<String, Long> localFiles, Map<String, Long> remoteFiles) {
 	List<SyncTask> tasks = new ArrayList<SyncTask>();
 	Set<String> files = remoteFiles.keySet();
 	for (String file : files) {
 	    long remoteSize = remoteFiles.get(file);
 	    if (localFiles.containsKey(file)) {
 		long localSize = localFiles.get(file);
 		if (remoteSize != localSize) {
 		    tasks.add(new SyncTask(file, remoteSize, localSize));
 		}
 	    } else {
 		tasks.add(new SyncTask(file, remoteSize, 0));
 	    }
 	}
 	return tasks;
     }
 
     private List<String> findMissingRemoteFiles(Map<String, Long> localFiles, Map<String, Long> remoteFiles) {
 	List<String> missingRemoteFiles = new ArrayList<String>();
 	Set<String> files = localFiles.keySet();
 	for (String file : files) {
 	    if (!remoteFiles.containsKey(file)) {
 		missingRemoteFiles.add(file);
 	    }
 	}
 	return missingRemoteFiles;
     }
 
     private JSONArray getSyncFolder(String url) {
 	HttpClient httpclient = new DefaultHttpClient();
 	HttpGet httpget = new HttpGet(url);
 	JSONArray files = new JSONArray();
 	try {
 	    HttpResponse response = httpclient.execute(httpget);
 	    Log.i(TAG,response.getStatusLine().toString());
 
 	    HttpEntity entity = response.getEntity();
 	    if (entity != null) {
 		InputStream input = entity.getContent();
 		String result = Util.convertStreamToString(input);
 		input.close();
 		Log.i(TAG,result);
 		files = new JSONArray(result);
 	    }
 	} catch (Exception e) {
 	    e.printStackTrace();
 	} 
 	return files;
     }
 
     @Override
     public IBinder onBind(Intent intent) {
 	return mBinder;
     }
 
     @Override
     public void registerSyncListener(IMusicSyncListener listener) {
 	mMusicSyncListener = listener;
     }
 
     @Override
     public void unregisterSyncListener(IMusicSyncListener listener) {
 	mMusicSyncListener = NULL_SYNC_LISTENER;
     }
 
     private void publishMissingFiles(final List<String> missingFiles) {
 	mHandler.post(new Runnable() {
 	    @Override
 	    public void run() {
 		mMusicSyncListener.onFilesMissing(missingFiles);
 	    }
 	});
     }
 
     @Override
     public void stop() {
 	if (mDownloader != null) {
 	    mDownloader.cancel(false);
 	}
     }
 
     @Override
     public List<String> getMissingFiles() {
 	return mMissingFiles;
     }
 
     private void showSyncStartedNotification() {
 	Notification notification = createNotification("Syncing Music Files");
 	notification.flags |= Notification.FLAG_ONGOING_EVENT;
 	mNotificationManager.notify(MUSIC_SYNC_NOTIFICATION, notification);
     }
 
     private void showSyncFinishedNotification() {
 	Notification notification = createNotification("All Music Files synced");
 	notification.flags |= Notification.FLAG_AUTO_CANCEL;
 	mNotificationManager.notify(MUSIC_SYNC_NOTIFICATION, notification);
     }
 
     private void showSyncDownloadNotification(String fileName) {
 	Notification notification = createNotification("downloading " + fileName);
 	notification.flags |= Notification.FLAG_ONGOING_EVENT;
 	mNotificationManager.notify(MUSIC_SYNC_NOTIFICATION, notification);
     }
 
     private Notification createNotification(String message) {
 	Intent intent = new Intent(this, MusicSync.class);
 	intent.setAction(Intent.ACTION_VIEW);
 	Notification notification = new Notification(R.drawable.icon, "Syncing...", System.currentTimeMillis());
 	notification.flags |= Notification.FLAG_NO_CLEAR;
 	notification.setLatestEventInfo(this, "MusicSync", message, 
 		PendingIntent.getActivity(this.getBaseContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT));
 	return notification;
     }
 }
