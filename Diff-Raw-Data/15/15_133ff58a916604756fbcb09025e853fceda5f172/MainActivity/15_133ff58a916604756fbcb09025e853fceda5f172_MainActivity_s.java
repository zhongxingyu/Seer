 package fr.insalyon.pyp.gui.main;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.location.Location;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.support.v4.app.NotificationCompat;
 import android.support.v4.app.TaskStackBuilder;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ViewFlipper;
 import fr.insalyon.pyp.R;
 import fr.insalyon.pyp.entities.EventEnitity;
 import fr.insalyon.pyp.gui.common.BaseActivity;
 import fr.insalyon.pyp.gui.common.IntentHelper;
 import fr.insalyon.pyp.gui.events.CreateEventActivity;
 import fr.insalyon.pyp.gui.events.EventActivity;
 import fr.insalyon.pyp.gui.events.ManagePersonalEvents;
 import fr.insalyon.pyp.network.ServerConnection;
 import fr.insalyon.pyp.tools.AppTools;
 import fr.insalyon.pyp.tools.Constants;
 import fr.insalyon.pyp.tools.PYPContext;
 import fr.insalyon.pyp.tools.TerminalInfo;
 
 public class MainActivity extends BaseActivity {
 	private LinearLayout abstractView;
 	private LinearLayout mainView;
 	private TextView windowTitle;
 	private ListView list;
 	private ListView events_list;
 	private PersonalEventsAdapter personalEventsadapter;
 	private EventsAdapter eventsAdapter;
 	private Location lastLocation;
 	private Long lastRefresh = System.currentTimeMillis() / 1000;
 	private ViewFlipper vf;
 	private float lastX;
 	private HashMap<String, EventEnitity> eventsLis = new HashMap<String, EventEnitity>();
 	private HashMap<String, EventEnitity> tmpList = new HashMap<String, EventEnitity>();
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState, Constants.MAIN_CONST);
 		AppTools.info("on create MainActivity");
 		initGraphicalInterface();
 	}
 
 	private void initGraphicalInterface() {
 		// set layouts
 		LayoutInflater mInflater = LayoutInflater.from(this);
 		abstractView = (LinearLayout) findViewById(R.id.abstractLinearLayout);
 		abstractView.setVisibility(LinearLayout.GONE);
 		abstractView = (LinearLayout) findViewById(R.id.abstractLinearLayoutTop);
 		abstractView.setVisibility(LinearLayout.VISIBLE);
 		mainView = (LinearLayout) mInflater.inflate(R.layout.main_layout, null);
 		abstractView.addView(mainView);
 		vf = (ViewFlipper) findViewById(R.id.view_flipper);
 		windowTitle = (TextView) findViewById(R.id.pageTitle);
 		windowTitle.setText(R.string.NoTitle);
 		ArrayList<String[]> data = new ArrayList<String[]>();
 		data.add(new String[] { getString(R.string.Add_Event) });
 		buildList(data);
 		new GetPersonalEvents().execute();
 		hideHeader(false);
 
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		// check if logged in
		checkLoggedIn();
 		final Handler handler = new Handler();
 		Timer timer = new Timer();
 		TimerTask doAsynchronousTask = new TimerTask() {
 			@Override
 			public void run() {
 				handler.post(new Runnable() {
 					public void run() {
 						try {
 							GetEvents ev = new GetEvents();
 							ev.execute();
 						} catch (Exception e) {
 							AppTools.error(e.getMessage());
 						}
 					}
 				});
 			}
 		};
 		timer.schedule(doAsynchronousTask, 0, 20000);
 		GetEvents ev = new GetEvents();
 		ev.execute();
 		new GetPersonalEvents().execute();
 	}
 
 	@Override
 	public boolean onTouchEvent(MotionEvent touchevent) {
 		switch (touchevent.getAction()) {
 		case MotionEvent.ACTION_DOWN: {
 			lastX = touchevent.getX();
 			break;
 		}
 
 		case MotionEvent.ACTION_UP: {
 
 			float currentX = touchevent.getX();
 			AppTools.debug("Old:" + lastX + "New:" + currentX);
 
 			if (lastX < currentX && lastX != 0 && currentX - lastX > 100) {
 				if (vf.getDisplayedChild() == 0)
 					break;
 				vf.setInAnimation(this, R.anim.in_from_left);
 				vf.setOutAnimation(this, R.anim.out_to_right);
 				lastLocation = null;
 				new GetEvents().execute();
 				vf.showNext();
 			}
 
 			if (lastX > currentX && lastX != 0 && lastX - currentX > 100) {
 				if (vf.getDisplayedChild() == 1)
 					break;
 				vf.setInAnimation(this, R.anim.in_from_right);
 				vf.setOutAnimation(this, R.anim.out_to_left);
 				new GetPersonalEvents().execute();
 				vf.showPrevious();
 			}
 			lastX = 0;
 			break;
 		}
 		}
 		return false;
 	}
 
 	private void buildList(ArrayList<String[]> data) {
 
 		list = (ListView) findViewById(R.id.personal_events_list);
 
 		// Getting adapter by passing xml data ArrayList
 		personalEventsadapter = new PersonalEventsAdapter(this, data);
 		if (personalEventsadapter == null) {
 			AppTools.info("Adapter is null");
 			return;
 		}
 		list.setAdapter(personalEventsadapter);
 
 		// Click event for single list row
 		list.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
 					long arg3) {
 				if (arg2 == 0) {
 					IntentHelper.openNewActivity(CreateEventActivity.class,
 							null, false);
 				} else {
 					String[] tagData = (String[]) arg1.getTag();
 					IntentHelper.openNewActivity(ManagePersonalEvents.class,
 							tagData, false);
 
 				}
 			}
 		});
 
 		list.setOnTouchListener(new OnTouchListener() {
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 
 				return MainActivity.this.onTouchEvent(event);
 			}
 		});
 	}
 
 	private class GetPersonalEvents extends AsyncTask<Void, Void, Void> {
 
 		JSONObject res;
 
 		@Override
 		protected void onPostExecute(Void result) {
 			if (res != null) {
 				try {
 					JSONArray array = res.getJSONArray("list");
 					ArrayList<String[]> data = new ArrayList<String[]>();
 					data.add(new String[] { getString(R.string.Add_Event) });
 					for (int i = 0; i < array.length(); i++) {
 						JSONObject obj = array.getJSONObject(i);
 						double lon = Double.parseDouble(obj.getString("lon"));
 						double lat = Double.parseDouble(obj.getString("lat"));
 						AppTools.error(lon + " " + lat);
 						data.add(new String[] {
 								obj.getString("name"),
 								obj.getString("start_time") + " - "
 										+ obj.getString("end_time"),
 								AppTools.checkInArea(lon, lat,
 										Constants.AREA_RADIUS).toString(),
 								obj.getString("id"),
 								obj.getString("description") });
 
 					}
 					AppTools.debug("Number of personal events:" + data.size());
 					MainActivity.this.buildList(data);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}
 
 		@Override
 		protected void onPreExecute() {
 			AppTools.debug("Loading persoanl events");
 		}
 
 		@Override
 		protected Void doInBackground(Void... params) {
 			// Send request to server for login
 			ServerConnection srvCon = ServerConnection.GetServerConnection();
 			try {
 				List<NameValuePair> parameters = new ArrayList<NameValuePair>();
 				SharedPreferences settings = PYPContext.getContext()
 						.getSharedPreferences(AppTools.PREFS_NAME, 0);
 				parameters.add(new BasicNameValuePair("auth_token", settings
 						.getString("auth_token", "")));
 				res = srvCon.connect(ServerConnection.GET_PERSONAL_EVENTS,
 						parameters);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			return null;
 		}
 	}
 
 	private class CheckIn extends AsyncTask<EventEnitity, Void, Void> {
 
 		JSONObject res;
 		EventEnitity entity;
 
 		@Override
 		protected void onPostExecute(Void result) {
 			if (res != null) {
 				try {
 					entity.setIsCheckedIn(true);
 					Toast toast = Toast.makeText(
 							getApplicationContext(),
 							getString(R.string.checked_in) + " "
 									+ entity.getName() + " "
 									+ getString(R.string.checked_in_opinion),
 							Toast.LENGTH_LONG);
 					toast.setGravity(Gravity.BOTTOM, 0, 0);
 					LinearLayout toastView = (LinearLayout) toast.getView();
 					toastView.setOnClickListener(new OnClickListener() {
 
 						@Override
 						public void onClick(View v) {
 							String[] tagData = new String[] { entity.getId() };
 							IntentHelper.openNewActivity(EventActivity.class,
 									tagData, false);
 						}
 					});
 					toast.show();
 					Bitmap icon = BitmapFactory.decodeResource(PYPContext
 							.getContext().getResources(),
 							R.drawable.logo_notification);
 					NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
 							MainActivity.this)
 							.setSmallIcon(R.drawable.logo)
 							.setContentTitle("Plan you party")
 							.setContentText(
 									getString(R.string.checked_in)
 											+ " "
 											+ entity.getName()
 											+ " "
 											+ getString(R.string.checked_in_opinion))
 							.setAutoCancel(true).setLargeIcon(icon);
 					// Creates an explicit intent for an Activity in your app
 					Intent resultIntent = new Intent(MainActivity.this,
 							EventActivity.class);
 					resultIntent.putExtra(Constants.PARAMNAME,
 							new String[] { entity.getId() });
 					// The stack builder object will contain an artificial back
 					// stack for the
 					// started Activity.
 					// This ensures that navigating backward from the Activity
 					// leads out of
 					// your application to the Home screen.
 					TaskStackBuilder stackBuilder = TaskStackBuilder
 							.create(MainActivity.this);
 					// Adds the back stack for the Intent (but not the Intent
 					// itself)
 					stackBuilder.addParentStack(EventActivity.class);
 					// Adds the Intent that starts the Activity to the top of
 					// the stack
 					stackBuilder.addNextIntent(resultIntent);
 					PendingIntent resultPendingIntent = stackBuilder
 							.getPendingIntent(0,
 									PendingIntent.FLAG_UPDATE_CURRENT);
 					mBuilder.setContentIntent(resultPendingIntent);
 					NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 					int mId = 1;
 					// mId allows you to update the notification later on.
 					mNotificationManager.notify(mId, mBuilder.build());
 
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}
 
 		@Override
 		protected void onPreExecute() {
 			AppTools.debug("Checking in..");
 
 		}
 
 		@Override
 		protected Void doInBackground(EventEnitity... params) {
 			// Send request to server for login
 			entity = params[0];
 			ServerConnection srvCon = ServerConnection.GetServerConnection();
 			try {
 				List<NameValuePair> parameters = new ArrayList<NameValuePair>();
 				SharedPreferences settings = PYPContext.getContext()
 						.getSharedPreferences(AppTools.PREFS_NAME, 0);
 				parameters.add(new BasicNameValuePair("auth_token", settings
 						.getString("auth_token", "")));
 				parameters.add(new BasicNameValuePair("event_id", params[0]
 						.getId()));
 				res = srvCon.connect(ServerConnection.CHECK_IN, parameters);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			return null;
 		}
 	}
 
 	private class CheckOut extends AsyncTask<EventEnitity, Void, Void> {
 
 		JSONObject res;
 		EventEnitity entity;
 
 		@Override
 		protected void onPostExecute(Void result) {
 			if (res != null) {
 				try {
 					entity.setIsCheckedIn(false);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}
 
 		@Override
 		protected void onPreExecute() {
 			AppTools.debug("Checking out..");
 
 		}
 
 		@Override
 		protected Void doInBackground(EventEnitity... params) {
 			// Send request to server for login
 			entity = params[0];
 			ServerConnection srvCon = ServerConnection.GetServerConnection();
 			try {
 				List<NameValuePair> parameters = new ArrayList<NameValuePair>();
 				SharedPreferences settings = PYPContext.getContext()
 						.getSharedPreferences(AppTools.PREFS_NAME, 0);
 				parameters.add(new BasicNameValuePair("auth_token", settings
 						.getString("auth_token", "")));
 				parameters.add(new BasicNameValuePair("event_id", params[0]
 						.getId()));
 				res = srvCon.connect(ServerConnection.CHECK_OUT, parameters);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			return null;
 		}
 	}
 
 	private void buildListEvents(ArrayList<String[]> data) {
 
 		events_list = (ListView) findViewById(R.id.events_list);
 
 		// Getting adapter by passing xml data ArrayList
 		eventsAdapter = new EventsAdapter(this, data);
 		if (eventsAdapter == null) {
 			AppTools.info("Adapter is null");
 			return;
 		}
 		events_list.setAdapter(eventsAdapter);
 		events_list.setOnTouchListener(new OnTouchListener() {
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 
 				return MainActivity.this.onTouchEvent(event);
 			}
 		});
 		// Click event for single list row
 		events_list.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
 					long arg3) {
 				String[] tagData = (String[]) arg1.getTag();
 				IntentHelper.openNewActivity(EventActivity.class, tagData,
 						false);
 			}
 		});
 	}
 
 	private class GetEvents extends AsyncTask<Void, Void, Void> {
 
 		JSONObject res;
 		ProgressDialog mProgressDialog;
 
 		@Override
 		protected void onPostExecute(Void result) {
 			if (mProgressDialog != null && mProgressDialog.isShowing()) {
 				mProgressDialog.dismiss();
 				if (AppTools.isFirstLaunch()) {
 					LayoutInflater inflater = getLayoutInflater();
 					View layout = inflater.inflate(R.layout.tutorial_layout,
 							(ViewGroup) findViewById(R.id.toast_layout_root));
 
 					Toast toast = new Toast(getApplicationContext());
 					toast.setGravity(Gravity.CENTER, 0, 0);
 					toast.setDuration(Toast.LENGTH_LONG);
 					toast.setView(layout);
 					toast.show();
 
 					AppTools.setFirstLaunch(false);
 				}
 
 			}
 
 			if (res != null) {
 				try {
 					JSONArray array = res.getJSONArray("list");
 					tmpList = new HashMap<String, EventEnitity>();
 
 					ArrayList<String[]> data = new ArrayList<String[]>();
 					for (int i = 0; i < array.length(); i++) {
 						JSONObject obj = array.getJSONObject(i);
 						double lon = Double.parseDouble(obj.getString("lon"));
 						double lat = Double.parseDouble(obj.getString("lat"));
 						tmpList.put(obj.getString("id"),
 								new EventEnitity(obj.getString("id"), lon, lat,
 										obj.getString("name")));
 						data.add(new String[] {
 								obj.getString("name"),
 								obj.getString("start_time") + " - "
 										+ obj.getString("end_time"),
 								AppTools.checkInArea(lon, lat,
 										Constants.AREA_RADIUS).toString(),
 								obj.getString("id"),
 								obj.getString("type"),
 								obj.getString("start_time") + " - "
 										+ obj.getString("end_time"),
 								obj.getString("id") });
 					}
 					Enumeration<String> strEnum = Collections.enumeration(eventsLis.keySet());
 
 					 while(strEnum.hasMoreElements()) {
 						EventEnitity ev = eventsLis.get(strEnum.nextElement());
 						if (tmpList.get(ev.getId()) == null) {
 							eventsLis.remove(ev);
 						}
 					}
 					 strEnum = Collections.enumeration(tmpList.keySet());
 
 					 while(strEnum.hasMoreElements()) {
 					
 						EventEnitity ev = tmpList.get(strEnum.nextElement());
 						AppTools.error(ev.getId());
 						if (eventsLis.get(ev.getId()) == null) {
 							eventsLis.put(ev.getId(), ev);
 							Bitmap icon = BitmapFactory.decodeResource(
 									PYPContext.getContext().getResources(),
 									R.drawable.logo_notification);
 							NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
 									MainActivity.this)
 									.setSmallIcon(R.drawable.logo)
 									.setContentTitle("Plan you party")
 									.setContentText(
 											getString(R.string.new_event) + " "
 													+ ev.getName())
 									.setAutoCancel(true).setLargeIcon(icon);
 							// Creates an explicit intent for an Activity in
 							// your app
 							Intent resultIntent = new Intent(MainActivity.this,
 									EventActivity.class);
 							resultIntent.putExtra(Constants.PARAMNAME,
 									new String[] { ev.getId() });
 							// The stack builder object will contain an
 							// artificial back
 							// stack for the
 							// started Activity.
 							// This ensures that navigating backward from the
 							// Activity
 							// leads out of
 							// your application to the Home screen.
 							TaskStackBuilder stackBuilder = TaskStackBuilder
 									.create(MainActivity.this);
 							// Adds the back stack for the Intent (but not the
 							// Intent
 							// itself)
 							stackBuilder.addParentStack(EventActivity.class);
 							// Adds the Intent that starts the Activity to the
 							// top of
 							// the stack
 							stackBuilder.addNextIntent(resultIntent);
 							PendingIntent resultPendingIntent = stackBuilder
 									.getPendingIntent(0,
 											PendingIntent.FLAG_UPDATE_CURRENT);
 							mBuilder.setContentIntent(resultPendingIntent);
 							NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 							int mId = 2;
 							// mId allows you to update the notification later
 							// on.
 							mNotificationManager.notify(mId, mBuilder.build());
 						}
 					}
 					AppTools.debug("Number of events:" + data.size());
 					MainActivity.this.buildListEvents(data);
 					res = null;
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 
 		}
 
 		@Override
 		protected void onPreExecute() {
 			if (events_list == null)
 				mProgressDialog = ProgressDialog.show(MainActivity.this,
 						getString(R.string.app_name),
 						getString(R.string.loading));
 			AppTools.debug("Loading events");
 		}
 
 		@Override
 		protected Void doInBackground(Void... params) {
 			Long currentTimeStamp = System.currentTimeMillis() / 1000;
 			// Check for check_in - check_out
 			Enumeration<String> strEnum = Collections.enumeration(eventsLis.keySet());
 
 			 while(strEnum.hasMoreElements()) {
 				EventEnitity e = eventsLis.get(strEnum.nextElement());
 				if (AppTools.checkInArea(e.getLon(), e.getLat(),
 						Constants.BAR_RADIOUS) && !e.getIsCheckedIn()) {
 					new CheckIn().execute(e);
 				}
 				if (!AppTools.checkInArea(e.getLon(), e.getLat(),
 						Constants.BAR_RADIOUS) && e.getIsCheckedIn()) {
 					new CheckOut().execute(e);
 				}
 			}
 			// Check barlist
 			if (lastLocation == null
 					|| currentTimeStamp - lastRefresh > 60 * 5
 					|| AppTools.checkInArea(lastLocation.getLongitude(),
 							lastLocation.getLatitude(), Constants.AREA_RADIUS) == false) {
 				lastRefresh = currentTimeStamp;
 				lastLocation = TerminalInfo.getPosition();
 				ServerConnection srvCon = ServerConnection
 						.GetServerConnection();
 				try {
 
 					List<NameValuePair> parameters = new ArrayList<NameValuePair>();
 					SharedPreferences settings = PYPContext.getContext()
 							.getSharedPreferences(AppTools.PREFS_NAME, 0);
 					parameters.add(new BasicNameValuePair("radius", String
 							.valueOf(Constants.AREA_RADIUS)));
 					parameters.add(new BasicNameValuePair("latitude", String
 							.valueOf(lastLocation.getLatitude())));
 					parameters.add(new BasicNameValuePair("longitude", String
 							.valueOf(lastLocation.getLongitude())));
 					parameters.add(new BasicNameValuePair("auth_token",
 							settings.getString("auth_token", "")));
 					res = srvCon.connect(ServerConnection.GET_EVENTS,
 							parameters);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 			return null;
 		}
 	}
 
 }
