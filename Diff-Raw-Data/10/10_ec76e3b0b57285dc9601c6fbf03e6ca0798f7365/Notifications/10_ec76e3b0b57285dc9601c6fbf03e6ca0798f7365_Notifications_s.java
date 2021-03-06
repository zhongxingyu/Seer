 package com.irccloud.android;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.jakewharton.notificationcompat2.NotificationCompat2;
 
 import android.annotation.SuppressLint;
 import android.annotation.TargetApi;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.Uri;
 import android.os.Build;
 import android.preference.PreferenceManager;
 import android.text.Html;
 import android.text.Spanned;
 import android.util.Log;
 import android.util.SparseArray;
 import android.view.View;
 import android.widget.RemoteViews;
 
 public class Notifications {
 	public class Notification {
 		public int cid;
 		public int bid;
 		public long eid;
 		public String nick;
 		public String message;
 		public String network;
 		public String chan;
 		public String buffer_type;
 		public String message_type;
 		public boolean shown = false;
 		
 		public String toString() {
 			return "{cid: " + cid + ", bid: " + bid + ", eid: " + eid + ", nick: " + nick + ", message: " + message + ", network: " + network + " shown: " + shown + "}";
 		}
 	}
 	
 	public class comparator implements Comparator<Notification> {
 		public int compare(Notification n1, Notification n2) {
 			if(n1.cid != n2.cid)
 				return Integer.valueOf(n1.cid).compareTo(n2.cid);
 			else if(n1.bid != n2.bid)
 				return Integer.valueOf(n1.bid).compareTo(n2.bid);
 			else
 				return Long.valueOf(n1.eid).compareTo(n2.eid);
 		}
 	}
 	
 	private ArrayList<Notification> mNotifications = null;
 	private SparseArray<String> mNetworks = null;
 	private SparseArray<Long> mLastSeenEIDs = null;
 	private SparseArray<HashSet<Long>> mDismissedEIDs = null;
 	
 	private static Notifications instance = null;
 	private int excludeBid = -1;
 	
 	public static Notifications getInstance() {
 		if(instance == null)
 			instance = new Notifications();
 		return instance;
 	}
 	
 	public Notifications() {
 		load();
 	}
 
 	private void load() {
 		mNotifications = new ArrayList<Notification>();
 		mNetworks = new SparseArray<String>();
 		mLastSeenEIDs = new SparseArray<Long>();
 		mDismissedEIDs = new SparseArray<HashSet<Long>>();
 
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(IRCCloudApplication.getInstance().getApplicationContext());
 
 		if(prefs.contains("notifications_json")) {
 			try {
 				JSONArray array = new JSONArray(prefs.getString("networks_json", "{}"));
 				for(int i = 0; i < array.length(); i++) {
 					JSONObject o = array.getJSONObject(i);
 					mNetworks.put(o.getInt("cid"), o.getString("network"));
 				}
 				
 				array = new JSONArray(prefs.getString("lastseeneids_json", "{}"));
 				for(int i = 0; i < array.length(); i++) {
 					JSONObject o = array.getJSONObject(i);
 					mLastSeenEIDs.put(o.getInt("bid"), o.getLong("eid"));
 				}
 				
 				array = new JSONArray(prefs.getString("dismissedeids_json", "{}"));
 				for(int i = 0; i < array.length(); i++) {
 					JSONObject o = array.getJSONObject(i);
 					int bid = o.getInt("bid");
 					mDismissedEIDs.put(bid, new HashSet<Long>());
 					
 					JSONArray eids = o.getJSONArray("eids");
 					for(int j = 0; j < eids.length(); j++) {
 						mDismissedEIDs.get(bid).add(eids.getLong(j));
 					}
 				}
 				
 				array = new JSONArray(prefs.getString("notifications_json", "{}"));
 				for(int i = 0; i < array.length(); i++) {
 					JSONObject o = array.getJSONObject(i);
 					Notification n = new Notification();
 					n.bid = o.getInt("bid");
 					n.cid = o.getInt("cid");
 					n.eid = o.getLong("eid");
 					n.nick = o.getString("nick");
 					n.message = o.getString("message");
 					n.chan = o.getString("chan");
 					n.buffer_type = o.getString("buffer_type");
 					n.message_type = o.getString("message_type");
 					n.network = mNetworks.get(n.cid);
 					if(o.has("shown"))
 						n.shown = o.getBoolean("shown");
 					mNotifications.add(n);
 				}
 				Collections.sort(mNotifications, new comparator());
 			} catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 		}
 	}
 	
 	private Timer mSaveTimer = null;
 	
 	@TargetApi(9)
 	private void save() {
 		if(mSaveTimer != null)
 			mSaveTimer.cancel();
 		mSaveTimer = new Timer();
 		mSaveTimer.schedule(new TimerTask() {
 
 			@Override
 			public void run() {
 				SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(IRCCloudApplication.getInstance().getApplicationContext()).edit();
 				try {
 					JSONArray array = new JSONArray();
 					for(Notification n : mNotifications) {
 						JSONObject o = new JSONObject();
 						o.put("cid", n.cid);
 						o.put("bid", n.bid);
 						o.put("eid", n.eid);
 						o.put("nick", n.nick);
 						o.put("message", n.message);
 						o.put("chan", n.chan);
 						o.put("buffer_type", n.buffer_type);
 						o.put("message_type", n.message_type);
 						o.put("shown", n.shown);
 						array.put(o);
 					}
 					editor.putString("notifications_json", array.toString());
 
 					array = new JSONArray();
 					for(int i = 0; i < mNetworks.size(); i++) {
 						int cid = mNetworks.keyAt(i);
 						String network = mNetworks.get(cid);
 						JSONObject o = new JSONObject();
 						o.put("cid", cid);
 						o.put("network", network);
 						array.put(o);
 					}
 					editor.putString("networks_json", array.toString());
 
 					array = new JSONArray();
 					for(int i = 0; i < mLastSeenEIDs.size(); i++) {
 						int bid = mLastSeenEIDs.keyAt(i);
 						long eid = mLastSeenEIDs.get(bid);
 						JSONObject o = new JSONObject();
 						o.put("bid", bid);
 						o.put("eid", eid);
 						array.put(o);
 					}
 					editor.putString("lastseeneids_json", array.toString());
 
 					array = new JSONArray();
 					for(int i = 0; i < mDismissedEIDs.size(); i++) {
 						JSONArray a = new JSONArray();
 						int bid = mDismissedEIDs.keyAt(i);
 						HashSet<Long> eids = mDismissedEIDs.get(bid);
 						for(long eid : eids) {
 							a.put(eid);
 						}
 						JSONObject o = new JSONObject();
 						o.put("bid", bid);
 						o.put("eids", a);
 						array.put(o);
 					}
 					editor.putString("dismissedeids_json", array.toString());
 
 					if(Build.VERSION.SDK_INT >= 9)
 						editor.apply();
 					else
 						editor.commit();
 				} catch (JSONException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				mSaveTimer = null;
 			}
 			
 		}, 1000);
 	}
 	
 	public void clearDismissed() {
 		mDismissedEIDs.clear();
 		save();
 	}
 	
 	public void clear() {
 		try {
 	        NotificationManager nm = (NotificationManager)IRCCloudApplication.getInstance().getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
 			ArrayList<Notification> notifications = getOtherNotifications();
 			
 			if(notifications.size() > 0) {
 		        for(Notification n : notifications) {
 	        		nm.cancel((int)(n.eid/1000));
 	        		nm.cancel(n.bid);
 		        }
 			}
 			mNotifications.clear();
 			mNetworks.clear();
 			mLastSeenEIDs.clear();
 			save();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public long getLastSeenEid(int bid) {
 		if(mLastSeenEIDs.get(bid) != null)
 			return mLastSeenEIDs.get(bid);
 		else
 			return -1;
 	}
 	
 	public synchronized void updateLastSeenEid(int bid, long eid) {
 		mLastSeenEIDs.put(bid, eid);
 		save();
 	}
 
 	public synchronized boolean isDismissed(int bid, long eid) {
 		if(mDismissedEIDs.get(bid) != null) {
 			for(Long e : mDismissedEIDs.get(bid)) {
 				if(e == eid)
 					return true;
 			}
 		}
 		return false;
 	}
 	
 	public synchronized void dismiss(int bid, long eid) {
 		if(mDismissedEIDs.get(bid) == null)
 			mDismissedEIDs.put(bid, new HashSet<Long>());
 		
 		mDismissedEIDs.get(bid).add(eid);
 		Notification n = getNotification(eid);
 		if(n != null)
 			mNotifications.remove(n);
 
 		save();
 	}
 	
 	public synchronized void addNetwork(int cid, String network) {
 		mNetworks.put(cid, network);
 		save();
 	}
 
 	public synchronized void deleteNetwork(int cid) {
 		mNetworks.remove(cid);
 		save();
 	}
 	
 	public synchronized void addNotification(int cid, int bid, long eid, String from, String message, String chan, String buffer_type, String message_type) {
 		if(isDismissed(bid, eid)) {
 			Log.d("IRCCloud", "This notification's EID has been dismissed, skipping...");
 			return;
 		}
 		long last_eid = getLastSeenEid(bid);
 		if(eid <= last_eid) {
 			Log.d("IRCCloud", "This notification's EID has already been seen, skipping...");
 			return;
 		}
 		
 		Log.d("IRCCloud", "Adding notification: "
 				+ "cid: " + cid + " "
 				+ "bid: " + bid + " "
 				+ "eid: " + eid + " "
 				+ "from: " + from + " "
 				+ "message: " + message + " "
 				+ "chan: " + chan + " "
 				+ "buffer_type: " + buffer_type + " "
 				+ "message_type: " + message_type + " "
 				);
 		String network = getNetwork(cid);
 		if(network != null)
 			Log.d("IRCCloud", "Name for network: " + network);
 		else {
 			Log.w("IRCCloud", "No network name!");
 			addNetwork(cid, "Unknown Network");
 		}
 		Notification n = new Notification();
 		n.bid = bid;
 		n.cid = cid;
 		n.eid = eid;
 		n.nick = from;
 		n.message = message;
 		n.chan = chan;
 		n.buffer_type = buffer_type;
 		n.message_type = message_type;
 		n.network = network;
 		
 		mNotifications.add(n);
 		Collections.sort(mNotifications, new comparator());
 		save();
 	}
 
 	public synchronized void deleteNotification(int cid, int bid, long eid) {
 		for(Notification n : mNotifications) {
 			if(n.cid == cid && n.bid == bid && n.eid == eid) {
 				mNotifications.remove(n);
 				save();
 				return;
 			}
 		}
 	}
 	
 	public synchronized void deleteOldNotifications(int bid, long last_seen_eid) {
         NotificationManager nm = (NotificationManager)IRCCloudApplication.getInstance().getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
 		ArrayList<Notification> notifications = getOtherNotifications();
 		
 		if(notifications.size() > 0) {
 	        for(Notification n : notifications) {
	        	if(n.bid == bid && n.eid <= last_seen_eid)
 	        		nm.cancel((int)(n.eid/1000));
 	        }
 		}
		nm.cancel(bid);

 		for(int i = 0; i < mNotifications.size(); i++) {
 			Notification n = mNotifications.get(i);
 			if(n.bid == bid && n.eid <= last_seen_eid) {
 				mNotifications.remove(n);
 				i--;
 				continue;
 			}
 		}
 		
 		if(mDismissedEIDs.get(bid) != null) {
 			HashSet<Long> eids = mDismissedEIDs.get(bid);
 			Long[] eidsArray = eids.toArray(new Long[eids.size()]);
 			for(int i = 0; i < eidsArray.length; i++) {
 				if(eidsArray[i] <= last_seen_eid) {
 					eids.remove(eidsArray[i]);
 				}
 			}
 		}
 	}
 	
 	public synchronized void deleteNotificationsForBid(int bid) {
         NotificationManager nm = (NotificationManager)IRCCloudApplication.getInstance().getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
 		ArrayList<Notification> notifications = getOtherNotifications();
 		
 		if(notifications.size() > 0) {
 	        for(Notification n : notifications) {
 	        	if(n.bid == bid)
 	        		nm.cancel((int)(n.eid/1000));
 	        }
 		}
 		nm.cancel(bid);
 
 		for(int i = 0; i < mNotifications.size(); i++) {
 			Notification n = mNotifications.get(i);
 			if(n.bid == bid) {
 				mNotifications.remove(n);
 				i--;
 				continue;
 			}
 		}
 
 		mDismissedEIDs.remove(bid);
 		mLastSeenEIDs.remove(bid);
 	}
 	
 	private boolean isMessage(String type) {
 		return !(type.equalsIgnoreCase("channel_invite") || type.equalsIgnoreCase("callerid"));
 	}
 	
 	public ArrayList<Notification> getMessageNotifications() {
 		Log.d("IRCCloud", "+++ Begin message notifications");
 		ArrayList<Notification> notifications = new ArrayList<Notification>();
 
 		for(int i = 0; i < mNotifications.size(); i++) {
 			Notification n = mNotifications.get(i);
 			if(n.bid != excludeBid && isMessage(n.message_type)) {
 				Log.d("IRCCloud", "Notification: " + n.toString());
 				if(n.network == null)
 					n.network = getNetwork(n.cid);
 				notifications.add(n);
 			}
 		}
 
 		Log.d("IRCCloud", "--- End message notifications");
 		return notifications;
 	}
 	
 	public ArrayList<Notification> getOtherNotifications() {
 		ArrayList<Notification> notifications = new ArrayList<Notification>();
 
 		for(int i = 0; i < mNotifications.size(); i++) {
 			Notification n = mNotifications.get(i);
 			if(n.bid != excludeBid && !isMessage(n.message_type)) {
 				if(n.network == null)
 					n.network = getNetwork(n.cid);
 				notifications.add(n);
 			}
 		}
 
 		return notifications;
 	}
 	
 	public String getNetwork(int cid) {
 		return mNetworks.get(cid);
 	}
 	
 	public synchronized Notification getNotification(long eid) {
 		for(int i = 0; i < mNotifications.size(); i++) {
 			Notification n = mNotifications.get(i);
 			if(n.bid != excludeBid && n.eid == eid && isMessage(n.message_type)) {
 				if(n.network == null)
 					n.network = getNetwork(n.cid);
 				return n;
 			}
 		}
 		return null;
 	}
 	
 	public synchronized void excludeBid(int bid) {
 		excludeBid = -1;
         NotificationManager nm = (NotificationManager)IRCCloudApplication.getInstance().getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
 		ArrayList<Notification> notifications = getOtherNotifications();
 		
 		if(notifications.size() > 0) {
 	        for(Notification n : notifications) {
 	        	if(n.bid == bid)
 	        		nm.cancel((int)(n.eid/1000));
 	        }
 		}
 		nm.cancel(bid);
 		excludeBid = bid;
 	}
 	
 	public synchronized void showNotifications(String ticker) {
 		ArrayList<Notification> notifications = getMessageNotifications();
 		for(Notification n : notifications) {
 			if(isDismissed(n.bid, n.eid)) {
 				Log.d("IRCCloud", "Removing dismissed notification: " + n.toString());
 				deleteNotification(n.cid, n.bid, n.eid);
 			}
 		}
 		
 		showMessageNotifications(ticker);
 		showOtherNotifications();
 	}
 	
 	private void showOtherNotifications() {
 		String title = "";
 		String text = "";
 		String ticker = null;
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(IRCCloudApplication.getInstance().getApplicationContext());
         NotificationManager nm = (NotificationManager)IRCCloudApplication.getInstance().getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
 		ArrayList<Notification> notifications = getOtherNotifications();
 		
 		if(notifications.size() > 0 && prefs.getBoolean("notify", true)) {
 	        for(Notification n : notifications) {
 	        	if(!n.shown) {
 					if(n.message_type.equals("callerid")) {
 						title = "Callerid: " + n.nick + " (" + n.network + ")";
 						text = n.nick + " " + n.message;
 						ticker = n.nick + " " + n.message;
 					} else {
 						title = n.nick + " (" + n.network + ")";
 						text = n.message;
 						ticker = n.message;
 					}
 			        nm.notify((int)(n.eid/1000), buildNotification(ticker, n.bid, new long[] {n.eid}, title, text, Html.fromHtml(text), 1));
 			        n.shown = true;
 	        	}
 	        }
 		}
 	}
 	
 	@SuppressLint("NewApi")
 	private android.app.Notification buildNotification(String ticker, int bid, long[] eids, String title, String text, Spanned big_text, int count) {
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(IRCCloudApplication.getInstance().getApplicationContext());
 
 		NotificationCompat2.Builder builder = new NotificationCompat2.Builder(IRCCloudApplication.getInstance().getApplicationContext())
         .setTicker(ticker)
 		.setContentTitle(title)
 		.setContentText(text)
         .setLights(0xFF0000FF, 500, 1000)
         .setSmallIcon(R.drawable.ic_stat_notify);
 
 		if(ticker != null && (System.currentTimeMillis() - prefs.getLong("lastNotificationTime", 0)) > 10000) {
 			if(prefs.getBoolean("notify_vibrate", true))
 				builder.setDefaults(android.app.Notification.DEFAULT_VIBRATE);
 			if(prefs.getBoolean("notify_sound", true))
 				builder.setSound(Uri.parse("android.resource://com.irccloud.android/"+R.raw.digit));
 		}
 
 		SharedPreferences.Editor editor = prefs.edit();
 		editor.putLong("lastNotificationTime", System.currentTimeMillis());
 		editor.commit();
 		
 		if(count == 1)
 			builder.setOnlyAlertOnce(true);
 		
 		Intent i = new Intent(IRCCloudApplication.getInstance().getApplicationContext(), MessageActivity.class);
 		i.putExtra("bid", bid);
 		i.setData(Uri.parse("bid://" + bid));
     	Intent dismiss = new Intent("com.irccloud.android.DISMISS_NOTIFICATION");
 		dismiss.setData(Uri.parse("irccloud-dismiss://" + bid));
     	dismiss.putExtra("bid", bid);
 		dismiss.putExtra("eids", eids);
         builder.setContentIntent(PendingIntent.getActivity(IRCCloudApplication.getInstance().getApplicationContext(), 0, i, PendingIntent.FLAG_UPDATE_CURRENT));
 		builder.setDeleteIntent(PendingIntent.getBroadcast(IRCCloudApplication.getInstance().getApplicationContext(), 0, dismiss, PendingIntent.FLAG_UPDATE_CURRENT));
 
 		android.app.Notification notification = builder.build();
 		if(Build.VERSION.SDK_INT >= 16 && big_text != null) {
 			RemoteViews contentView = new RemoteViews("com.irccloud.android", R.layout.notification);
 			contentView.setTextViewText(R.id.title, title);
 			contentView.setTextViewText(R.id.text, big_text);
 			if(count > 4) {
 				contentView.setViewVisibility(R.id.divider, View.VISIBLE);
 				contentView.setViewVisibility(R.id.more, View.VISIBLE);
 				contentView.setTextViewText(R.id.more, "+" + (count - 4) + " more");
 			} else {
 				contentView.setViewVisibility(R.id.divider, View.GONE);
 				contentView.setViewVisibility(R.id.more, View.GONE);
 			}
 			notification.bigContentView = contentView;
 		}
 		
 		return notification;
 	}
 	
 	private void showMessageNotifications(String ticker) {
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(IRCCloudApplication.getInstance().getApplicationContext());
 		String title = "";
 		String text = "";
         NotificationManager nm = (NotificationManager)IRCCloudApplication.getInstance().getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
 		ArrayList<Notification> notifications = getMessageNotifications();
 
 		if(notifications.size() > 0 && prefs.getBoolean("notify", true)) {
 			if(notifications.size() == 1) {
 				Notification n = notifications.get(0);
 				if(!n.shown) {
 					title = n.nick;
 					if(!n.buffer_type.equals("conversation") && !n.message_type.equals("wallops") && n.chan.length() > 0)
 						title += " in " + n.chan;
 					title += " (" + n.network + ")";
 					if(n.message_type.equals("buffer_me_msg"))
 						text = " " + n.message;
 					else
 						text = n.message;
 			        nm.notify(n.bid, buildNotification(ticker, n.bid, new long[] {n.eid}, title, text, Html.fromHtml(text), 1));
 			        n.shown = true;
 				}
 			} else {
 				int lastbid = notifications.get(0).bid;
 				int count = 0;
 				long[] eids = new long[notifications.size()];
 				Notification last = null;
 				count = 0;
         		title = notifications.get(0).chan + " (" + notifications.get(0).network + ")";
         		boolean show = false;
 		        for(Notification n : notifications) {
 		        	if(n.bid != lastbid) {
 		        		if(show) {
 							if(count == 1) {
 								title = last.nick;
 								if(!last.buffer_type.equals("conversation") && !last.message_type.equals("wallops") && last.chan.length() > 0)
 									title += " in " + last.chan;
 								title += " (" + last.network + ")";
 								nm.notify(lastbid, buildNotification(ticker, lastbid, eids, title, Html.fromHtml(text).toString(), Html.fromHtml(text), count));
 							} else {
 								nm.notify(lastbid, buildNotification(ticker, lastbid, eids, title, count + " unread highlight(s)", Html.fromHtml(text), count));
 							}
 		        		}
 		        		lastbid = n.bid;
 		        		title = n.chan + " (" + n.network + ")";
 		        		text = "";
 						count = 0;
 						eids = new long[notifications.size()];
 						show = false;
 		        	}
 		        	if(count < 4) {
 		        		if(text.length() > 0)
 		        			text += "<br/>";
 			        	if(n.buffer_type.equals("conversation") && n.message_type.equals("buffer_me_msg"))
 			        		text += " " + n.message;
 			        	else if(n.buffer_type.equals("conversation"))
 			        		text += n.message;
 			        	else if(n.message_type.equals("buffer_me_msg"))
 				    		text += "<b> " + n.nick + "</b> " + n.message;
 			        	else
 				    		text += "<b>" + n.nick + "</b> " + n.message;
 		        	}
 		        	if(!n.shown) {
 		        		n.shown = true;
 		        		show = true;
 		        	}
 		        	eids[count++] = n.eid;
 		        	last = n;
 		        }
 		        if(show) {
 					if(count == 1) {
 						title = last.nick;
 						if(!last.buffer_type.equals("conversation") && !last.message_type.equals("wallops") && last.chan.length() > 0)
 							title += " in " + last.chan;
 						title += " (" + last.network + ")";
 						nm.notify(lastbid, buildNotification(ticker, lastbid, eids, title, Html.fromHtml(text).toString(), Html.fromHtml(text), count));
 					} else {
 							nm.notify(lastbid, buildNotification(ticker, lastbid, eids, title, count + " unread highlight(s)", Html.fromHtml(text), count));
 					}
 		        }
 			}
 		}
 	}
 }
