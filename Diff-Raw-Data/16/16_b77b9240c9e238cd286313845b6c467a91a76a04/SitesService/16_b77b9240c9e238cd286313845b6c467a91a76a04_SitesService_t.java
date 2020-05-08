 package org.droidstack.service;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import net.sf.stackwrap4j.stackauth.StackAuth;
 import net.sf.stackwrap4j.stackauth.entities.Site;
 
 import org.droidstack.util.Const;
 import org.droidstack.util.SitesDatabase;
 
 import android.app.Service;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.Uri;
 import android.net.NetworkInfo.State;
 import android.os.AsyncTask;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.Messenger;
 import android.os.RemoteException;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 public class SitesService extends Service {
 
 	// to be received
 	// send this after you bind with an appropriate replyTo
 	public static final int MSG_REGISTER = 1;
 	// send this to unregister yourself, again with a replyTo
 	public static final int MSG_UNREGISTER = 2;
 	
 	// to be sent
 	// service is performing network activity -- notify the user
 	public static final int MSG_LOADING = 3;
 	// there is new data available -- update your views
 	public static final int MSG_UPDATE = 4;
 	// service has finished performing network activity
 	public static final int MSG_FINISHED = 5;
 
 	private ArrayList<Messenger> mClients;
 	private SharedPreferences mPreferences;
 	private SitesDatabase mDatabase;
 	private List<Site> mSites;
 	private boolean isWorking = false;
 	
 	private class IncomingHandler extends Handler {
 		@Override
 		public void handleMessage(Message msg) {
 			switch(msg.what) {
 			case MSG_REGISTER:
 				mClients.add(msg.replyTo);
 				if (isWorking) {
 					try {
 						msg.replyTo.send(Message.obtain(null, MSG_LOADING));
 					}
 					catch (RemoteException e) {
 						// client fail
 					}
 				}
 				break;
 			case MSG_UNREGISTER:
 				mClients.remove(msg.replyTo);
 				break;
 			default:
 				super.handleMessage(msg);
 				break;
 			}
 		}
 	}
 	
 	final Messenger mMessenger = new Messenger(new IncomingHandler());
 	
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		
 		final ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
         final NetworkInfo info = cm.getActiveNetworkInfo();
         if (info == null || info.getState() != State.CONNECTED) {
         	stopSelf();
         	return;
         }
 		
 		mClients = new ArrayList<Messenger>();
 		mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
 		mDatabase = new SitesDatabase(this);
 		
 		long now = System.currentTimeMillis()/1000;
 		// sites refresh ~ every day
 		if (mPreferences.getLong(Const.PREF_SITES_LASTRUN, -1) < now - 24*60*60) {
 			isWorking = true;
 			new RefreshSites().execute();
 		}
 	}
 	
 	@Override
 	public IBinder onBind(Intent intent) {
 		return mMessenger.getBinder();
 	}
 	
 	private void sendToAll(int what) {
 		for (Messenger client: mClients) {
 			try {
 				client.send(Message.obtain(null, what));
 			}
 			catch (RemoteException e) {
 				mClients.remove(client);
 			}
 		}
 	}
 	
 	private class RefreshSites extends AsyncTask<Void, Void, List<Site>> {
 		
 		private Exception e;
 		
 		@Override
 		protected void onPreExecute() {
 			sendToAll(MSG_LOADING);
 		}
 		
 		@Override
 		protected List<Site> doInBackground(Void... params) {
 			try {
 				return StackAuth.getAllSites();
 			}
 			catch (Exception e) {
 				this.e = e;
 				return null;
 			}
 		}
 		
 		@Override
 		protected void onPostExecute(List<Site> result) {
 			if (result == null) {
 				Log.e(Const.TAG, "Could not refresh sites", e);
 				stopSelf();
 				return;
 			}
 			for (Site site: result) {
 				mDatabase.addSite(site.getApiEndpoint(), site.getName(), 0, null);
 			}
 			mSites = result;
 			sendToAll(MSG_UPDATE);
 			new RefreshIcons().execute();
 		}
 		
 	}
 	
 	private class RefreshIcons extends AsyncTask<Void, Void, Void> {
 		
 		@Override
 		protected Void doInBackground(Void... params) {
 			long now = System.currentTimeMillis()/1000;
 			boolean fullRefresh = false;
 			// full refresh ~ every week
 			if (mPreferences.getLong(Const.PREF_SITES_LAST_ICON_REFRESH, -1) < now - 7*24*60*60) {
 				fullRefresh = true;
 				mPreferences.edit().putLong(Const.PREF_SITES_LAST_ICON_REFRESH, now).commit();
 			}
 			File iconsDir = Const.getIconsDir();
 			if (iconsDir == null) return null;
 			HashMap<String, File> cache = new HashMap<String, File>();
 			for (Site site: mSites) {
 				File icon = new File(iconsDir, Uri.parse(site.getApiEndpoint()).getHost());
 				if (!fullRefresh && icon.exists()) continue;
 				try {
 					InputStream in;
 					String url = site.getIconUrl();
 					if (cache.containsKey(url)) in = new FileInputStream(cache.get(url)); 
 					else {
 						in = new URL(url).openStream();
 						cache.put(url, icon);
 					}
 					OutputStream out = new FileOutputStream(icon);
 					byte[] buf = new byte[1024];
 					int len;
 					while ((len = in.read(buf)) > 0) {
 						out.write(buf, 0, len);
 					}
 					in.close();
 					out.close();
 				}
 				catch (Exception e) {
 					// whoopsies
 					Log.e(Const.TAG, "Failed fetching site icon", e);
 				}
 			}
 			return null;
 		}
 		
 		@Override
 		protected void onPostExecute(Void result) {
 			// that was it
			sendToAll(MSG_UPDATE);
 			sendToAll(MSG_FINISHED);
 			mPreferences.edit().putLong(Const.PREF_SITES_LASTRUN, System.currentTimeMillis()/1000).commit();
 			stopSelf();
 		}
 		
 	}
 
 }
