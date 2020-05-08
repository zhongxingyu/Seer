 package sk.gista.medobs;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 
 import org.apache.http.HttpResponse;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import sk.gista.medobs.view.CalendarListener;
 import sk.gista.medobs.view.CalendarView;
 import sk.gista.medobs.view.ReservationAdapter;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.animation.AlphaAnimation;
 import android.view.animation.Animation;
 import android.view.animation.AnimationSet;
 import android.view.animation.ScaleAnimation;
 import android.widget.ArrayAdapter;
 import android.widget.ImageButton;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class Medobs extends Activity implements CalendarListener {
 
 	public static String VERSION;
 	
 	private static final String SERVER_URL_SETTING = "server_url";
 	private static final String USERNAME_SETTING = "username";
 	private static final String PASSWORD_SETTING = "password";
 	private static final String OFFICE_SETTING = "office";
 	
 	private static final int OFFICES_DIALOG = 0;
 	private static final int ABOUT_DIALOG = 1;
 
 	private static final Object NO_PARAM = null;
 	
 	private SharedPreferences prefferences;
 	private Client client;
 	private Calendar calendar;
 	private List<Office> offices;
 	private Office currentOffice;
 	private List<Integer> activeDays; //in current month
 	
 	private ListView reservationsView;
 	private TextView selectedDateView;
 	private ImageButton prevDayButton;
 	private ImageButton nextDayButton;
 	private ProgressBar progressBar;
 	private View datePickerView;
 	private View calendarBg;
 	private CalendarView calendarView;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		try {
 			VERSION = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_META_DATA).versionName;
 		} catch (NameNotFoundException e) {
 			e.printStackTrace();
 		}
 		prefferences = PreferenceManager.getDefaultSharedPreferences(this);
 		
 		reservationsView = (ListView) findViewById(R.id.reservations_list);
 		selectedDateView = (TextView) findViewById(R.id.selected_date_text);
 		prevDayButton = (ImageButton) findViewById(R.id.prev_day);
 		nextDayButton = (ImageButton) findViewById(R.id.next_day);
 		progressBar = (ProgressBar) findViewById(R.id.progress_bar);
 		datePickerView = findViewById(R.id.date_picker);
 		calendarBg = findViewById(R.id.calendar_bg);
 		calendarView = (CalendarView) findViewById(R.id.calendar);
 		
 		calendar = Calendar.getInstance();
 		
 		prevDayButton.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				int step = -1;
 				if (activeDays != null) {
 					int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
 					for (int i : activeDays) {
 						if (i >= currentDay) {
 							break;
 						}
 						step = i-currentDay;
 					}
 					if (!activeDays.contains(currentDay+step)) {
 						// move to last day of previous month
 						calendar.add(Calendar.DAY_OF_MONTH, -currentDay);
 						activeDays = null;
 						new FetchDaysTask().execute(calendar);
 						step = 0;
 					}
 				}
 				calendar.add(Calendar.DAY_OF_MONTH, step);
 				fetchReservations();
 			}
 		});
 
 		nextDayButton.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				int step = 1;
 				if (activeDays != null) {
 					int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
 					for (int i : activeDays) {
 						if (i > currentDay) {
 							step = i-currentDay;
 							break;
 						}
 					}
 					if (!activeDays.contains(currentDay+step)) {
 						// set to first day of next month
 						calendar.add(Calendar.MONTH, 1);
 						calendar.set(Calendar.DAY_OF_MONTH, 1);
 						activeDays = null;
 						new FetchDaysTask().execute(calendar);
 						step = 0;
 					}
 				}
 				calendar.add(Calendar.DAY_OF_MONTH, step);
 				fetchReservations();
 			}
 		});
 
 		datePickerView.setOnClickListener(new View.OnClickListener() {
 			private Runnable postAction = new Runnable() {
 				
 				@Override
 				public void run() {
 					showCalendar();
 				}
 			};
 			@Override
 			public void onClick(View v) {
 				activeDays = null;
 				new FetchDaysTask(postAction).execute(calendar);
 			}
 		});
 
 		// override default button actions, so we can move on prev/next month
 		// after getting enabled/disabled days
 		calendarView.setOnPrevMonthListener(new View.OnClickListener() {
 			private Calendar cal = Calendar.getInstance();
 			private Runnable postAction = new Runnable() {
 				@Override
 				public void run() {
 					calendarView.setPrevViewItem();
 				}
 			};
 			@Override
 			public void onClick(View v) {
 				cal.setTimeInMillis(calendarView.getCurrentMonth().getTimeInMillis());
 				cal.add(Calendar.MONTH, -1);
 				activeDays = null;
 				new FetchDaysTask(postAction).execute(cal);
 			}
 		});
 		calendarView.setOnNextMonthListener(new View.OnClickListener() {
 			private Calendar cal = Calendar.getInstance();
 			private Runnable postAction = new Runnable() {
 				@Override
 				public void run() {
 					calendarView.setNextViewItem();
 				}
 			};
 			@Override
 			public void onClick(View v) {
 				cal.setTimeInMillis(calendarView.getCurrentMonth().getTimeInMillis());
 				cal.add(Calendar.MONTH, 1);
 				activeDays = null;
 				new FetchDaysTask(postAction).execute(cal);
 			}
 		});
 		if (activeDays != null) {
 			calendarView.setEnabledDays(activeDays);
 		}
 		calendarView.setCalendarListener(this);
 		selectedDateView.setText(labelDateFormat.format(calendar.getTime()));
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		if (client == null) {
 			String url = prefferences.getString(SERVER_URL_SETTING, "");
 			if (url.length() > 7) {
 				client = new Client(url);
 				new LoginTask().execute(NO_PARAM);
 			} else {
 				showMessage(R.string.msg_server_url_not_configured);
 				return;
 			}
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menu_refresh:
 			if (client == null) {
 				showMessage(R.string.msg_server_url_not_configured);
 			} else {
 				if (client.isLoggedIn()) {
 					fetchReservations();
 					new FetchDaysTask().execute(calendar);
 				} else {
 					new LoginTask().execute(NO_PARAM);
 				}
 			}
 			return true;
 		case R.id.menu_select_office:
 			showDialog(OFFICES_DIALOG);
 			return true;
 		case R.id.menu_settings:
 			saveState();
 			offices = null;
 			activeDays = null;
 			if (client != null) {
 				client.cancelCurrentRequest();
 			}
 			client = null;
 			reservationsView.setAdapter(null);
 			startActivity(new Intent(this, Settings.class));
 			return true;
 		case R.id.menu_about:
 			showDialog(ABOUT_DIALOG);
 			return true;
 		}
 		return false;
 	}
 
 	private void saveState() {
 		if (currentOffice != null) {
 			Editor editor = prefferences.edit();
 			editor.putInt(OFFICE_SETTING, currentOffice.getId());
 			editor.commit();
 		}
 	}
 
 	private void fetchReservations() {
 		if (currentOffice != null && client != null) {
 			selectedDateView.setText(labelDateFormat.format(calendar.getTime()));
 			new FetchReservationsTask().execute(NO_PARAM);
 		}
 	}
 
 	@Override
 	public void onDetachedFromWindow() {
 		super.onDetachedFromWindow();
 		if (client != null) {
 			if (client.isExecuting()) {
 				client.cancelCurrentRequest();
 			}
 			client.logout();
 		}
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		if (client != null) {
 			client.logout();
 		}
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		saveState();
 	}
 
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		Dialog dialog = null;
 		switch(id) {
 		case OFFICES_DIALOG:
 			DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
 
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					if (which < offices.size()) {
 						setCurrentOffice(offices.get(which));
 						fetchReservations();
 						new FetchDaysTask().execute(calendar);
 					}
 				}
 			};
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setTitle(R.string.label_select_office);
 			builder.setItems(new String[0], listener);
 			dialog = builder.create();
 			break;
 		case ABOUT_DIALOG:
 			dialog = new AboutDialog(this);
 			dialog.setTitle(R.string.label_about);
 			break;
 		}
 		return dialog;
 	}
 
 	@Override
 	protected void onPrepareDialog(int id, Dialog dialog) {
 		super.onPrepareDialog(id, dialog);
 		switch (id) {
 		case OFFICES_DIALOG: 
 			AlertDialog officesDialog = (AlertDialog) dialog;
 			String[] items = {};
 			int selectedItem = -1;
 			if (offices != null) {
 				items = new String[offices.size()];
 				for (int i = 0; i < offices.size(); i++) {
 					items[i] = offices.get(i).getName();
 					if (currentOffice != null && currentOffice.getId() == offices.get(i).getId()) {
 						selectedItem = i;
 					}
 				}
 			}
 			ListView list = officesDialog.getListView();
 			list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
 			
 			officesDialog.getListView().setAdapter(new ArrayAdapter<String>(this, R.layout.simple_list_item_single_choice, items));
 			if (selectedItem != -1) {
 				list.setItemChecked(selectedItem, true);
 			}
 			break;
 		}
 	}
 
 	@Override
 	public void onBackPressed() {
 		if (calendarView.getVisibility() == View.VISIBLE) {
 			hideCalendar();
 		} else {
 			super.onBackPressed();
 		}
 	}
 
 	private void showCalendar() {
 		if (activeDays == null) {
 			new FetchDaysTask(new Runnable() {
 				
 				@Override
 				public void run() {
 					showCalendarAnimation();
 				}
 			}).execute(calendar);
 		} else {
 			showCalendarAnimation();
 		}
 	}
 
 	private void showCalendarAnimation() {
 		calendarView.setSelectedDate(calendar);
 		calendarBg.setVisibility(View.VISIBLE);
 		calendarView.setVisibility(View.VISIBLE);
 		AnimationSet animation = new AnimationSet(true);
 		ScaleAnimation scaleAnim = new ScaleAnimation(0.7f, 1f, 0.7f, 1f, reservationsView.getWidth()/2, reservationsView.getHeight()/2);
 		AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1f);
 		animation.addAnimation(scaleAnim);
 		animation.addAnimation(alphaAnimation);
 		animation.setDuration(150);
 		AlphaAnimation bgAlphaAnimation = new AlphaAnimation(0.0f, 1f);
 		bgAlphaAnimation.setDuration(500);
 		calendarBg.startAnimation(bgAlphaAnimation);
 		calendarView.startAnimation(animation);
 	}
 
 	private void hideCalendar() {
 		AnimationSet calendarAnim = new AnimationSet(true);
 		ScaleAnimation scaleAnim = new ScaleAnimation(1f, 0.7f, 1f, 0.7f, reservationsView.getWidth()/2, reservationsView.getHeight()/2);
 		AlphaAnimation alphaAnimation = new AlphaAnimation(1f, 0.0f);
 		calendarAnim.addAnimation(scaleAnim);
 		calendarAnim.addAnimation(alphaAnimation);
 		calendarAnim.setDuration(150);
 		AlphaAnimation bgAlphaAnimation = new AlphaAnimation(1f, 0);
 		bgAlphaAnimation.setDuration(150);
 		bgAlphaAnimation.setAnimationListener(new Animation.AnimationListener() {
 			
 			@Override
 			public void onAnimationStart(Animation animation) {
 			}
 			
 			@Override
 			public void onAnimationRepeat(Animation animation) {
 			}
 			
 			@Override
 			public void onAnimationEnd(Animation animation) {
 				calendarBg.setVisibility(View.INVISIBLE);
 				calendarView.setVisibility(View.INVISIBLE);
 			}
 		});
 		calendarBg.startAnimation(bgAlphaAnimation);
 		calendarView.startAnimation(calendarAnim);
 		
 		activeDays = null;
 		fetchReservations();
 		new FetchDaysTask().execute(calendar);
 	}
 
 	private void setCurrentOffice(Office office) {
 		currentOffice = office;
 		if (currentOffice != null) {
 			setTitle(String.format("%s - %s (%s)", getString(R.string.app_name), currentOffice.getName(), currentOffice.getStreet()));
 		} else {
 			setTitle(getString(R.string.app_name));
 		}
 	}
 
 	private void showMessage(int resid) {
 		Toast.makeText(this, resid, Toast.LENGTH_SHORT).show();
 	}
 
 	public static String readInputStream(InputStream is) throws IOException {
 		String line;
 		StringBuilder content = new StringBuilder();
 		BufferedReader input = new BufferedReader(new InputStreamReader(is), 1024);
 		while ((line = input.readLine()) != null) {
 			content.append(line);
 		}
 		input.close();
 		return content.toString();
 	}
 
 	private int activeRequestsCount;
 	private abstract class MedobsAsyncTask <Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {
 
 		@Override
 		protected void onPreExecute() {
 			progressBar.setVisibility(View.VISIBLE);
 			activeRequestsCount++;
 		}
 
 		protected void onPostExecute(Result result) {
 			activeRequestsCount--;
 			if (activeRequestsCount < 1) {
 				progressBar.setVisibility(View.INVISIBLE);
 			}
 		};
 	}
 
 	private class FetchOfficesTask extends MedobsAsyncTask<Object, Integer, String> {
 
 		@Override
 		protected String doInBackground(Object... params) {
 			String content = null;
 			if (client != null) {
 				HttpResponse resp = null;
 				try {
 					resp = client.httpGet("/offices/");
 					if (resp.getStatusLine().getStatusCode() < 400) {
 						content = readInputStream(resp.getEntity().getContent());
 					}
 				} catch (IOException e) {
 					e.printStackTrace();
 				} finally {
 					client.closeResponse(resp);
 				}
 			}
 			return content;
 		}
 
 		@Override
 		protected void onPostExecute(String content) {
 			super.onPostExecute(content);
 			if (content != null) {
 				int lastOfficeId = prefferences.getInt(OFFICE_SETTING, 0);
 				offices = new ArrayList<Office>();
 				try {
 					JSONArray jsonOffices = new JSONArray(content);
 					for (int i = 0; i < jsonOffices.length(); i++) {
 						JSONObject jsonOffice = jsonOffices.getJSONObject(i);
 						int id = jsonOffice.getInt("id");
 						String name = jsonOffice.getString("name");
 						String street = jsonOffice.getString("street");
 						String city = jsonOffice.getString("city");
 						Office office = new Office(id, name, street, city);
 						offices.add(office);
 						if (id == lastOfficeId) {
 							setCurrentOffice(office);
 						}
 					}
 				}  catch (JSONException e) {
 					e.printStackTrace();
 					showMessage(R.string.msg_bad_response_error);
 				}
 				if (currentOffice == null && offices.size() > 0) {
 					setCurrentOffice(offices.get(0));
 				}
 				//fetchReservations();
 				//new FetchDaysTask().execute(calendar);
 				showCalendar();
 			} else {
 				showMessage(R.string.msg_http_error);
 			}
 		}
 	}
 
 	private static SimpleDateFormat labelDateFormat = new SimpleDateFormat("d MMMM yyyy", Locale.ENGLISH);
 	private static SimpleDateFormat requestDateFormat = new SimpleDateFormat("yyyy-MM-dd");
 	
 	private class FetchReservationsTask extends MedobsAsyncTask<Object, Integer, String> {
 
 		@Override
 		protected String doInBackground(Object ... params) {
 			String content = null;
 			if (client != null && currentOffice != null) {
 				String date = requestDateFormat.format(calendar.getTime());
 				HttpResponse resp = null;
 				try {
 					resp = client.httpGet("/reservations/"+date+"/"+currentOffice.getId()+"/");
 					if (resp.getStatusLine().getStatusCode() < 400) {
 						content = readInputStream(resp.getEntity().getContent());
 					}
 				} catch (IOException e) {
 					e.printStackTrace();
 				} finally {
 					client.closeResponse(resp);
 				}
 			}
 			return content;
 		}
 
 		@Override
 		protected void onPostExecute(String content) {
 			super.onPostExecute(content);
 			List<Reservation> reservations = new ArrayList<Reservation>();
 			if (content != null) {
 				try {
 					JSONArray array = new JSONArray(content);
 					for (int i = 0; i < array.length(); i++) {
 						JSONObject reservation = array.getJSONObject(i);
 						int status = reservation.getInt("status");
 						String time = reservation.getString("time");
 						String patient = reservation.getString("patient");
 						String patientPhone = reservation.getString("phone_number");
 						String patientEmail = reservation.getString("email");
 						String bookedBy = reservation.getString("booked_by");
 						String bookedAt = reservation.getString("booked_at");
						String examKind = reservation.getString("exam_kind");
 						reservations.add(new Reservation(time, Reservation.Status.valueOf(status), patient,
								patientPhone, patientEmail, bookedBy, bookedAt, examKind));
 					}
 				} catch (JSONException e) {
 					e.printStackTrace();
 					showMessage(R.string.msg_bad_response_error);
 				}
 			} else {
 				showMessage(R.string.msg_http_error);
 			}
 			reservationsView.setAdapter(new ReservationAdapter(Medobs.this, reservations));
 		}
 	}
 
 	private class FetchDaysTask extends MedobsAsyncTask<Calendar, Integer, String> {
 
 		private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM");
 		private Runnable postAction;
 
 		public FetchDaysTask() {}
 		
 		public FetchDaysTask(Runnable postAction) {
 			this.postAction = postAction;
 		}
 
 		@Override
 		protected String doInBackground(Calendar... params) {
 			String content = null;
 			if (client != null && currentOffice != null) {
 				HttpResponse resp = null;
 				String date = dateFormat.format(params[0].getTime());
 				try {
 					resp = client.httpGet("/days_status/"+date+"/"+currentOffice.getId()+"/");
 					if (resp != null && resp.getStatusLine().getStatusCode() < 400) {
 						content = readInputStream(resp.getEntity().getContent());
 					}
 				} catch (IOException e) {
 					e.printStackTrace();
 				} finally {
 					client.closeResponse(resp);
 				}
 			}
 			return content;
 		}
 
 		@Override
 		protected void onPostExecute(String content) {
 			super.onPostExecute(content);
 			if (content != null) {
 				try {
 					JSONObject data = new JSONObject(content);
 					Iterator<String> it = data.keys();
 					List<Integer> enabledDays = new ArrayList<Integer>();
 					while (it.hasNext()) {
 						String date = it.next();
 						boolean enabled = data.getBoolean(date);
 						if (enabled) {
 							enabledDays.add(Integer.parseInt(date.substring(date.lastIndexOf('-')+1)));
 						}
 					}
 					Collections.sort(enabledDays);
 					activeDays = enabledDays;
 					if (calendarView != null) {
 						calendarView.setEnabledDays(activeDays);
 					}
 					if (postAction != null) {
 						postAction.run();
 					}
 				} catch (JSONException e) {
 					e.printStackTrace();
 					showMessage(R.string.msg_bad_response_error);
 				}
 			} else {
 				showMessage(R.string.msg_http_error);
 			}
 		}
 	}
 
 	class LoginTask extends MedobsAsyncTask<Object, Integer, Boolean> {
 		private String username;
 		private String password;
 
 		@Override
 		protected void onPreExecute() {
 			super.onPreExecute();
 			username = prefferences.getString(USERNAME_SETTING, "");
 			password = prefferences.getString(PASSWORD_SETTING, "");
 		}
 
 		@Override
 		protected Boolean doInBackground(Object... params) {
 			return client.login(username, password);
 		}
 
 		@Override
 		protected void onPostExecute(Boolean result) {
 			super.onPostExecute(result);
 			if (result) {
 				new FetchOfficesTask().execute(NO_PARAM);
 			} else {
 				showMessage(R.string.msg_login_failed);
 			}
 		}
 	}
 
 	@Override
 	public void onMonthChanged(CalendarView calendarView, Calendar value) {
 		activeDays = null;
 		new FetchDaysTask().execute(value);
 	}
 
 	@Override
 	public void onDateSelected(CalendarView calendarView) {
 		hideCalendar();
 		calendar = calendarView.getSelectedValue();
 		activeDays = null;
 		fetchReservations();
 		new FetchDaysTask().execute(calendar);
 	}
 }
