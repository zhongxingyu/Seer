 package com.zeyomir.ocfun.controller;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.location.Address;
 import android.location.Geocoder;
 import android.location.Location;
 import android.os.AsyncTask;
 import android.util.Log;
 import android.widget.RemoteViews;
 import android.widget.Toast;
 import com.zeyomir.ocfun.R;
 import com.zeyomir.ocfun.controller.helper.CacheDownloader;
 import com.zeyomir.ocfun.controller.helper.ConnectionHelper;
 import com.zeyomir.ocfun.controller.helper.GetCachesListHelper;
 import com.zeyomir.ocfun.dao.*;
 import com.zeyomir.ocfun.gui.Add;
 import com.zeyomir.ocfun.gui.Cancel;
 import com.zeyomir.ocfun.gui.List;
 import com.zeyomir.ocfun.model.Cache;
 import com.zeyomir.ocfun.model.Image;
 import com.zeyomir.ocfun.model.LogbookEntry;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.io.IOException;
 import java.util.Map;
 
 public class AddCaches implements CacheDownloader {
 
 	private Context context;
 	private static DownloadCaches downloader;
 	private Notification notification;
 	private NotificationManager mManager;
 	private PreferencesDAO p;
 	final static private String fields = "code|name|url|location|type|owner|size|difficulty|terrain|req_passwd|description|hint|images|latest_logs|last_found|attrnames";
 
 	public AddCaches(Context c) {
 		this.context = c;
 		this.p = new PreferencesDAO(context);
 	}
 
 	public void add(int tag, Map<String, String> data) {
 		switch (tag) {
 			case R.id.add_standard:
 				addStandard(data);
 				break;
 			case R.id.add_near:
 				addNear(data);
 				break;
 			case R.id.add_custom:
 				addCustom(data);
 				break;
 		}
 	}
 
 	private void addStandard(Map<String, String> data) {
 		String tag = "adding standard";
 		String text;
 		switch (Integer.parseInt(data.get("option"))) {
 			case R.id.radioButton1:
 				Log.i(tag, "kod");
 				text = data.get("text");
 				Log.i(tag, text);
 				text = text.replaceAll("\\s", "").replaceAll(",+", ",")
 						.toUpperCase(); // remove all spaces and multiple commas
 				run(text.split(","));
 				break;
 			case R.id.radioButton2:
 				Log.i(tag, "nazwa");
 				text = data.get("text");
 				new GetCachesListHelper(this, context).getByName(text);
 				Log.i(tag, text);
 				break;
 		}
 	}
 
 	private void addNear(Map<String, String> data) {
 		String tag = "adding near";
 		switch (Integer.parseInt(data.get("option"))) {
 			case R.id.radioButton1:
 				Log.i(tag, "addres");
 				Log.i(tag, data.get("distance") + ", " + data.get("text"));
 				Geocoder gc = new Geocoder(context);
 				try {
 					Address a = gc.getFromLocationName(data.get("text"), 1).get(0);
 					new GetCachesListHelper(this, context).getByLocation(
 							a.getLatitude() + "|" + a.getLongitude(),
 							data.get("distance"));
 					Log.i("geocoder", a.getLatitude() + "|" + a.getLongitude());
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 				break;
 			case R.id.radioButton2:
 				Log.i(tag, "cache");
 				Log.i(tag, data.get("distance") + ", " + data.get("text"));
 				new GetCachesListHelper(this, context).getByCache(data.get("text"),
 						data.get("distance"));
 				break;
 			case R.id.radioButton3:
 				Log.i(tag, "koords");
 				Log.i(tag, data.get("distance") + ", " + data.get("text"));
 				new GetCachesListHelper(this, context).getByLocation(
 						decodeLocation(data.get("text")), data.get("distance"));
 				break;
 			case R.id.radioButton4:
 				Log.i(tag, "auto koords");
 				Log.i(tag, data.get("distance") + ", " + data.get("text"));
 				new GetCachesListHelper(this, context).getByLocation(
 						decodeLocation(data.get("text")), data.get("distance"));
 				break;
 		}
 	}
 
 	private String decodeLocation(String sLocation) {
 		sLocation = sLocation.trim().replaceAll("\\s*,\\s*", ",")
 				.replaceAll("\\s+", " ").replace(' ', ':');
 		String[] temp = sLocation.split(",");
 		return (Location.convert(temp[0])) + "|" + (Location.convert(temp[1]));
 	}
 
 	private void addCustom(Map<String, String> data) {
 		String tag = "adding custom";
 		Log.i(tag, data.get("name"));
 		Log.i(tag, data.get("option"));
 		Log.i(tag, data.get("coords"));
 		Log.i(tag, data.get("description"));
 		Cache c = new Cache(0, "custom", data.get("name"),
 				decodeLocation(data.get("coords")),
 				InternalResourceMapper.custom.id(), "Moja skrzynka", 0.0, 0.0,
 				0.0, false, "<p>"
 				+ data.get("description").replaceAll("\n", "<br>")
 				+ "</p>", "", "", "", false);
 		CacheDAO.save(c);
 		Toast.makeText(context, "Dodano do bazy", Toast.LENGTH_SHORT).show();
 	}
 
 	public void download(String codes) {
 		String[] temp = codes.split(",");
 
 		if (temp.length > p.getLimit()) {
 			Add a = ((Add) context);
 			a.displayTooManyWarning(temp, this);
 		} else
 			run(temp);
 	}
 
 	public void run(String[] codes) {
 		Log.i("DEBUG", "run- start");
 		mManager = (NotificationManager) context
 				.getSystemService(Context.NOTIFICATION_SERVICE);
 		notification = new Notification(R.drawable.notification_icon,
 				"Pobieranie skrzynek", System.currentTimeMillis());
 		RemoteViews contentView = new RemoteViews(context.getPackageName(),
 				R.layout.progressbar_notification);
 		contentView.setProgressBar(R.id.progressBar, 10, 0, false);
 		contentView.setTextViewText(R.id.text, "Pobieranie skrzynek...");
		contentView.setTextViewText(R.id.text2, "nieznana ilość...");
 		notification.contentView = contentView;
 
 		Intent notificationIntent = new Intent(context, Cancel.class);
 		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
 				notificationIntent, 0);
 		notification.contentIntent = contentIntent;
 		notification.flags |= Notification.FLAG_AUTO_CANCEL
 				| Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
 		mManager.notify(42, notification);
 		Log.i("DEBUG", "run- before execute");
 		downloader = new DownloadCaches();
 		downloader.execute(codes);
 	}
 
 	public static void cancel() {
 		if (downloader != null && downloader.getStatus() == AsyncTask.Status.RUNNING)
 			downloader.cancel(true);
 	}
 
 	private class DownloadCaches extends AsyncTask<String[], Integer, Integer> {
 		String[] codes;
 		int max = 0;
 
 		@Override
 		protected Integer doInBackground(String[]... params) {
 			Log.i("warning", "run");
 			this.codes = params[0];
 			this.max = codes.length;
 			int progress = 0;
 			Log.i("DEBUG", "get prefs");
 			String f = new PreferencesDAO(context).isAuthenticated() ? fields
 					+ "|is_found" : fields;
 			Log.i("DEBUG", "fields ready");
 			for (int i = 0; i < max; i++) {
 				if (isCancelled())
 					return progress;
 				publishProgress(i, progress);
 				Log.i("downloading", codes[i]);
 
 				String link = ConnectionHelper.baseLink
 						+ "/services/caches/geocache?cache_code=" + codes[i]
 						+ "&fields=" + ConnectionHelper.encode(f)
 						+ "&langpref=pl";
 				String answer = ConnectionHelper.get(link, context);
 				if (answer == null)
 					continue;
 				JSONObject jo = null;
 				try {
 					jo = new JSONObject(answer);
 				} catch (JSONException e1) {
 					Log.w("DownloadCaches",
 							"unable to parse response for cache: " + codes[i]);
 				}
 				if (jo == null)
 					continue;
 				Cache c = null;
 				try {
 					c = new Cache(jo);
 				} catch (JSONException e1) {
 					Log.w("DownloadCaches",
 							"error creating cache from response: " + codes[i]);
 				}
 				if (c == null)
 					continue;
 				long id = CacheDAO.save(c);
 				try {
 					JSONArray images = jo.getJSONArray("images");
 					for (int j = 0; j < images.length(); j++) {
 						JSONObject JSONimage = (JSONObject) images.get(j);
 						Image image = new Image(id, JSONimage);
 						ConnectionHelper.download(JSONimage.getString("url"),
 								image.path);
 						ImageDAO.save(image);
 					}
 				} catch (Exception e) {
 
 				}
 				try {
 					JSONArray logs = jo.getJSONArray("latest_logs");
 					for (int j = 0; j < logs.length(); j++) {
 						JSONObject JSONlog = (JSONObject) logs.get(j);
 						LogbookEntry log = new LogbookEntry(id, JSONlog);
 						LogDAO.save(log);
 					}
 				} catch (Exception e) {
 
 				}
 				progress++;
 			}
 
 			return progress;
 		}
 
 		protected void onProgressUpdate(Integer... params) {
 			notification.contentView.setProgressBar(R.id.progressBar, max,
 					params[0], false);
 			notification.contentView.setTextViewText(R.id.text2, "Pobrano "
 					+ params[1] + " z " + max);
 			mManager.notify(42, notification);
 		}
 
 		protected void onPostExecute(Integer param) {
 			mManager.cancel(42);
 			notification.contentView = new RemoteViews(
 					context.getPackageName(), R.layout.simple_notification);
 			notification.contentView.setTextViewText(R.id.text,
 					"Pobrano skrzynek: " + param);
 			Intent notificationIntent = new Intent(context, List.class);
 			PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
 					notificationIntent, 0);
 			notification.contentIntent = contentIntent;
 			notification.flags = Notification.FLAG_AUTO_CANCEL;
 			mManager.notify(42, notification);
 		}
 
 		protected void onCancelled(Integer param) {
 			mManager.cancel(42);
 			notification.contentView = new RemoteViews(
 					context.getPackageName(), R.layout.simple_notification);
 			notification.contentView.setTextViewText(R.id.text,
 					"Anulowano! Pobrano skrzynek: " + param);
 			Intent notificationIntent = new Intent(context, List.class);
 			PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
 					notificationIntent, 0);
 			notification.contentIntent = contentIntent;
 			notification.flags = Notification.FLAG_AUTO_CANCEL;
 			mManager.notify(42, notification);
 		}
 	}
 }
