 package com.emergency.codeblue;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.util.EntityUtils;
 import org.json.JSONObject;
 import org.json.JSONTokener;
 
 import com.google.android.gms.internal.ap;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.database.Cursor;
 import android.graphics.Color;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.speech.RecognizerIntent;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.ImageButton;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class PhysicianActivity extends ListActivity {
 
 	// List of Requested Orders
 	static ArrayList<Order> listOrder = new ArrayList<Order>();
 	static CheckAdapter adapter;
 	private MedicineDBAdapter dbHelper;
 
 	// Voice Recognition Initialization
 	private static final int VOICE_RECOGNITION_REQUEST_CODE = 1001;
 	private ImageButton mbtSpeak;
 
 	// Timer Initialization
 	private static TextView timerView;
 	private static Button timerButton;
 	public static boolean timerStarted = false;
 
 	// codeID
 	private static String codeId = "";
 
 	// new request variable
 	public Order request;
 
 	/*
 	 * private static PhysicianActivity instance = null; protected
 	 * PhysicianActivity() { // Exists only to defeat instantiation. } public
 	 * static PhysicianActivity getInstance() { if(instance == null) { instance
 	 * = new PhysicianActivity(); } return instance; }
 	 */
 
 	public static void addOrderToList(Order o) {
 		listOrder.add(o);
 		adapter.notifyDataSetChanged();
 	}
 
 	static int elapsedTime = 0;
 	static Timer timer = new Timer();
 	static int theta;
 
 	public static void startTimer(int delay, int period) {
 		// timerStarted = true;
 		int secondsOffset = delay;
 		elapsedTime += secondsOffset;
 
 		timer.scheduleAtFixedRate(new TimerTask() {
 			public void run() {
				elapsedTime += 100; // increase every 100 msec
 				mHandler.obtainMessage(1).sendToTarget();
 			}
 		}, 0, period);
 	}
 
 	public static Handler mHandler = new Handler() {
 		public void handleMessage(Message msg) {
 			String str;
 			str = parseTime(elapsedTime);
 			timerView.setText(str);
 			// StopWatch.time.setText(formatIntoHHMMSS(elapsedTime)); //this is
 			// the textview
 		}
 
 		private String parseTime(int elapsedTime) {
 			int totalSeconds = elapsedTime / 1000;
 			elapsedTime = totalSeconds;
 			int minutes = elapsedTime / 60;
 			elapsedTime = elapsedTime - minutes * 60;
 
 			String time = " " + String.valueOf(minutes);
 			String seconds = String.valueOf(elapsedTime);
 			if (seconds.length() == 1) {
 				seconds = "0" + seconds;
 			}
 			time = time + ":" + seconds + " ";
 			return time;
 		}
 	};
 
 	static String serverMessage = "";
 
 	class startCode extends AsyncTask<Void, Void, Void> {
 
 		@Override
 		protected Void doInBackground(Void... params) {
 			// Change this url later
 			String url = "http://dabix.no-ip.org/create_codeblue/";
 
 			try {
 
 				// HttpClient client = new DefaultHttpClient();
 				// HttpPost post = new HttpPost(url);
 				//
 				// HttpResponse response = client.execute(post);
 				// BufferedReader reader = new BufferedReader(
 				// new InputStreamReader(response.getEntity().getContent()));
 				//
 				// StringBuilder builder = new StringBuilder();
 				// for (String line = null; (line = reader.readLine()) != null;)
 				// {
 				// builder.append(line).append("\n");
 				// }
 				//
 				// JSONTokener tokener = new JSONTokener(builder.toString());
 				// // JSONObject json = new JSONObject(tokener);
 				// JSONObject userInfo = new JSONObject(tokener);
 				//
 				// System.out.println(userInfo.toString());
 
 				HttpClient client = new DefaultHttpClient();
 				HttpPost post = new HttpPost(
 						"http://dabix.no-ip.org/create_codeblue/");
 
 				HttpResponse response = client.execute(post);
 				BufferedReader reader = new BufferedReader(
 						new InputStreamReader(response.getEntity().getContent()));
 
 				StringBuilder builder = new StringBuilder();
 				for (String line = null; (line = reader.readLine()) != null;) {
 					builder.append(line).append("\n");
 				}
 				System.out.println(builder.toString());
 				JSONTokener tokener = new JSONTokener(builder.toString());
 				// JSONObject json = new JSONObject(tokener);
 				JSONObject info = new JSONObject(tokener);
 				codeId = info.getString("id");
 				// System.out.println(info.toString());
 
 			} catch (Exception e) {
 				System.out.println("Got error: " + e.toString());
 			}
 			return null;
 		}
 	}
 
 	class stopCode extends AsyncTask<Void, Void, Void> {
 
 		@Override
 		protected Void doInBackground(Void... params) {
 			// Change this url later
 			String url = "http://dabix.no-ip.org/stop_code/";
 
 			// Create the POST request
 			HttpClient httpClient = new DefaultHttpClient();
 			HttpGet httpGet = new HttpGet(url);
 
 			// Making HTTP Request
 			try {
 				httpClient.execute(httpGet);
 
 			} catch (Exception e) {
 
 			}
 			return null;
 		}
 	}
 
 	static class getTheta extends AsyncTask<Void, Void, Void> {
 
 		@Override
 		protected Void doInBackground(Void... params) {
 			// Change this url later
 			String url = "http://dabix.no-ip.org/time_sync/";
 
 			// Create the POST request
 			HttpClient httpClient = new DefaultHttpClient();
 			HttpGet httpGet = new HttpGet(url);
 
 			// Making HTTP Request
 			try {
 				HttpResponse response = httpClient.execute(httpGet);
 				HttpEntity respEntity = response.getEntity();
 				if (respEntity != null) {
 					// EntityUtils to get the response content
 					serverMessage = EntityUtils.toString(respEntity);
 				}
 			} catch (Exception e) {
 				serverMessage = null;
 			}
 			return null;
 		}
 	}
 
 	public static void calcTheta() {
 		getTheta t = new getTheta();
 		t.execute();
 		// showToastMessage(serverMessage);
 	}
 
 	public static void startTimer() {
 		// Started the timer, so change the flag
 		timerStarted = true;
 		// Notify the dev that the timer's going
 		// showToastMessage("Timer Started.");
 		timerButton.setText("STOP");
 		timerButton.setTextColor(Color.RED);
 		startTimer(theta, 100);
 	}
 
 	public void stopTimer() {
 		// Reset timer vars
 		elapsedTime = 0;
 		timer.cancel();
 		timer.purge();
 		timer = new Timer();
 
 		// Do the inverse of the start code
 		timerStarted = false;
 
 		timerButton.setText("START");
 		timerButton.setTextColor(Color.GREEN);
 		timerView.setText(" 0:00 ");
 	}
 
 	// Got a new code from gcm!
 	public static void gotNewCode(String st) {
 		// calulate theta
 		Long clientBefore = java.lang.System.currentTimeMillis();
 		calcTheta();
 		halt();
 		// showToastMessage(serverMessage);
 		Long clientAfter = java.lang.System.currentTimeMillis();
 		theta = parseServerMessage(clientBefore, clientAfter, serverMessage, st);
 		// showToastMessage("Delay: " + String.valueOf(theta));
 		System.out.println("Delay: " + String.valueOf(theta));
 		startTimer();
 	}
 
 	// Hold until response from server
 	private static void halt() {
 		while (serverMessage == "") {
 			// Hold until serverMessage is filled out.
 			// This should probably use a Timer...
 		}
 	}
 
 	private static int parseServerMessage(Long cb, Long ca, String serverTime,
 			String startTime) {
 		// showToastMessage(serverTime);
 
 		int i = serverTime.indexOf("$");
 
 		try {
 			String before = serverTime.substring(0, i);
 			String after = serverTime.substring(i + 1, serverTime.length());
 
 			double serverBefore = Double.valueOf(before);
 			double serverAfter = Double.valueOf(after);
 
 			double clientAfter = (double) ca;
 			double clientBefore = (double) cb;
 
 			double first = clientAfter - clientBefore;
 			double second = serverAfter - serverBefore;
 
 			double roundTripTime = (first - second) / 2;
 
 			double deviceTime = clientBefore + roundTripTime;
 			double offset = serverBefore - deviceTime;
 
 			double deviceStartTime = Double.valueOf(startTime) - offset;
 			Long currentTime = java.lang.System.currentTimeMillis();
 
 			double currentTimer = ((double) currentTime) - deviceStartTime;
 
 			return (int) ((double) currentTimer);
 		} catch (Exception e) {
 			System.out.println("ERROR: " + e.toString());
 			return -1;
 		}
 
 	}
 
 	protected void startCode() {
 		// Send a message to server to tell people to join the code
 		startCode t = new startCode();
 		t.execute();
 	}
 
 	protected void stopCode() {
 		// Send a message to server to tell people code is over
 		stopCode t = new stopCode();
 		t.execute();
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_physician);
 
 		listOrder.clear();
 		mbtSpeak = (ImageButton) findViewById(R.id.btnPhysVoice);
 		checkVoiceRecognition();
 		adapter = new CheckAdapter();
 
 		// Initialize Medication Database
 		dbHelper = new MedicineDBAdapter(this);
 		dbHelper.open();
 
 		setListAdapter(adapter);
 
 		/*
 		 * Timer UI: !timerStarted = false -> timer is not ON !timerStarted =
 		 * true -> timer is ON
 		 */
 		timerView = (TextView) findViewById(R.id.countTime);
 		timerButton = (Button) findViewById(R.id.timerBut);
 		timerButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View arg0) {
 				if (!timerStarted) {
 					// startTimer();
 					startCode();
 
 				} else {
 					stopTimer();
 					stopCode();
 				}
 			}
 
 		});
 
 		// function to start code, use this on getting gcm msg
 		// gotNewCode(0);
 
 		// a dummy button to send out orders to the server
 		Button orderButton = (Button) findViewById(R.id.requestOrder);
 		orderButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View arg0) {
 				// call method that sends out and order to the server.
 				Thread trd = new Thread(new Runnable() {
 					@Override
 					public void run() {
 						URL url;
 						try {
 							url = new URL("http://dabix.no-ip.org/login/");
 							AsyncTask<URL, Integer, Long> res = new FakeOrder()
 									.execute(url);
 						} catch (Exception e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 					}
 
 				});
 				trd.start();
 			}
 		});
 		// end of dummy button
 	}
 
 	// Voice Recognition Handler Related ======================================
 	public void checkVoiceRecognition() {
 		// Check if voice recognition is present
 		PackageManager pm = getPackageManager();
 		List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(
 				RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
 		if (activities.size() == 0) {
 			mbtSpeak.setEnabled(false);
 			Toast.makeText(this, "Voice recognizer not present",
 					Toast.LENGTH_SHORT).show();
 		}
 	}
 
 	public void speak(View view) {
 		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
 
 		// Specify the calling package to identify your application
 		intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass()
 				.getPackage().getName());
 
 		// Given an hint to the recognizer about what the user is going to say
 		// There are two form of language model available
 		// 1.LANGUAGE_MODEL_WEB_SEARCH : For short phrases
 		// 2.LANGUAGE_MODEL_FREE_FORM : If not sure about the words or phrases
 		// and its domain.
 		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
 				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
 
 		int noOfMatches = 5;
 		// Specify how many results you want to receive. The results will be
 		// sorted where the first result is the one with higher confidence.
 		intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, noOfMatches);
 		// Start the Voice recognizer activity for the result.
 		startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		request = new Order();
 
 		String requestMsg = "";
 
 		if (requestCode == VOICE_RECOGNITION_REQUEST_CODE)
 
 			// If Voice recognition is successful then it returns RESULT_OK
 			if (resultCode == RESULT_OK) {
 				ArrayList<String> textMatchList = data
 						.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
 
 				if (!textMatchList.isEmpty()) {
 					request = voiceCommandParsor(textMatchList);
 
 					if (!request.isEmpty() && !request.getDosage().equals("")) {
 						// recentOrderMin = minutes;
 						// recentOrderSec = seconds;
 						// request.setOrderTime(String.format("  %02d : %02d  ",
 						// minutes, seconds));
 
 						Thread trd = new Thread(new Runnable() {
 							@Override
 							public void run() {
 
 								try {
 									Order orderToSend = request;
 									AsyncTask<Order, Integer, Long> res = new RealOrder()
 											.execute(orderToSend);
 
 								} catch (Exception e) {
 									// TODO Auto-generated catch block
 									e.printStackTrace();
 								}
 							}
 
 						});
 						trd.start();
 
 						// listOrder.add(request);
 						// listOrder.add(requestMsg);
 						// adapter.notifyDataSetChanged();
 					} else {
 						Toast.makeText(getApplicationContext(),
 								"Invalid Order... Try again!",
 								Toast.LENGTH_SHORT).show();
 					}
 				}
 				// Result code for various error.
 			} else if (resultCode == RecognizerIntent.RESULT_AUDIO_ERROR) {
 				showToastMessage("Audio Error");
 			} else if (resultCode == RecognizerIntent.RESULT_CLIENT_ERROR) {
 				showToastMessage("Client Error");
 			} else if (resultCode == RecognizerIntent.RESULT_NETWORK_ERROR) {
 				showToastMessage("Network Error");
 			} else if (resultCode == RecognizerIntent.RESULT_NO_MATCH) {
 				showToastMessage("No Match");
 			} else if (resultCode == RecognizerIntent.RESULT_SERVER_ERROR) {
 				showToastMessage("Server Error");
 			}
 		super.onActivityResult(requestCode, resultCode, data);
 	}
 
 	private Order voiceCommandParsor(ArrayList<String> voiceStrings) {
 		Order cmd = new Order();
 		cmd.setStatus(false);
 
 		// String cmd = new String();
 		ArrayList<String> medicineString = new ArrayList<String>();
 		// Get all the medicine name from the database
 		Cursor query = dbHelper.fetchMedicinesName();
 
 		// cmd = "";
 		if (query.getCount() == 0)
 			return cmd;
 		int nameIndex = query.getColumnIndexOrThrow(MedicineDBAdapter.KEY_NAME);
 
 		// Get the input string with a proper medicine name, assume one medicine
 		// per command
 		String medName = "";
 		// while (query.moveToNext())
 		for (int i = 0; i < query.getCount(); i++) {
 			query.moveToPosition(i);
 			if (!medName.equalsIgnoreCase(query.getString(nameIndex))) {
 				medName = query.getString(nameIndex);
 
 				for (String vInput : voiceStrings) {
 
 					// extract the medicine
 					if (vInput.contains(medName)) {
 						medicineString.add(vInput);
 						if (cmd.getName().equals(""))
 							cmd.setName(medName);
 					}
 				}
 			}
 
 		}
 		query.close();
 
 		// Get the query with the selected medicine
 		query = dbHelper.fetchMedicineByName(cmd.getName());
 		int unitIndex = query.getColumnIndexOrThrow(MedicineDBAdapter.KEY_UNIT);
 
 		// Extract the dosage from the command
 		Boolean isDosageFound = false;
 		for (int i = 0; ((i < query.getCount()) && (!isDosageFound)); i++) {
 			query.moveToPosition(i);
 			for (String vInput : medicineString) {
 				String unit;
 				int sIndex;
 				if (query.getString(unitIndex).equalsIgnoreCase("mg"))
 					unit = " milligram";
 				else if (query.getString(unitIndex).equalsIgnoreCase("g"))
 					unit = " gram";
 				else if (query.getString(unitIndex).equalsIgnoreCase("ug"))
 					unit = " microgram";
 				else
 					unit = " " + query.getString(unitIndex);
 				sIndex = vInput.lastIndexOf(unit);
 				if (sIndex != -1) {
 					int qIndex;
 					qIndex = vInput.lastIndexOf(' ', sIndex - 1);
 					if (qIndex == -1) {
 						qIndex = 0;
 					}
 					String dosage = vInput.substring(qIndex, sIndex);
 					double f = 0.0;
 					try {
 						f = Double.parseDouble(dosage);
 					} catch (NumberFormatException e) {
 						f = 0.0;
 					}
 					if (f != 0.0) {
 
 						cmd.setDosage(dosage + " " + query.getString(unitIndex));
 						isDosageFound = true;
 						break;
 					}
 
 				}
 			}
 
 		}
 		query.close();
 		if (!isDosageFound) {
 			Toast.makeText(getApplicationContext(), "Please specify dosage!",
 					Toast.LENGTH_SHORT).show();
 			return cmd;
 		}
 		for (String vInput : medicineString) {
 			if (vInput.contains(" IV")) {
 				cmd.setRoute("IV");
 				break;
 			} else if (vInput.contains(" IO")) {
 				cmd.setRoute("IO");
 				break;
 			} else if (vInput.contains(" Inhale")) {
 				cmd.setRoute("Inhale");
 				break;
 			} else if (vInput.contains(" SQ")) {
 				cmd.setRoute("SQ");
 				break;
 			} else if (vInput.contains(" normal saline")) {
 				cmd.setRoute("normal saline");
 				break;
 			}
 		}
 		return cmd;
 	}
 
 	// Array Adapter Entry Related ============================================
 	public class CheckAdapter extends ArrayAdapter<Order> {
 		CheckAdapter() {
 			super(PhysicianActivity.this, R.layout.row, R.id.testo, listOrder);
 		}
 
 		@Override
 		public View getView(final int position, View convertView,
 				ViewGroup parent) {
 			final Order thisOrder = listOrder.get(position);
 			String orderMsg = thisOrder.getName() + " " + thisOrder.getDosage()
 					+ " " + thisOrder.getRoute();
 
 			LayoutInflater inflater = (LayoutInflater) PhysicianActivity.this
 					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			View rowView = inflater.inflate(R.layout.row, parent, false);
 			rowView.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					// Pop Up message
 					final AlertDialog confirm_popup = new AlertDialog.Builder(
 							PhysicianActivity.this).create();
 					String dialogDisplay = String.format("Medicine: %s \n"
 							+ "Ordered - %s \n" + "Given - %s",
 							thisOrder.getName(), thisOrder.getOrderTime(),
 							thisOrder.getGivenTime());
 					confirm_popup.setMessage(dialogDisplay);
 					confirm_popup.setButton(DialogInterface.BUTTON_NEUTRAL,
 							"Close", new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int which) {
 									confirm_popup.dismiss();
 								}
 							});
 
 					confirm_popup.show();
 				}
 			});
 			TextView text = (TextView) rowView.findViewById(R.id.testo);
 			CheckBox check = (CheckBox) rowView.findViewById(R.id.img);
 			text.setText(orderMsg);
 
 			// Handle Ack Order
 			// if (listAck.get(position))
 			// text.setTextColor(Color.YELLOW);
 			// else
 			// text.setTextColor(Color.WHITE);
 
 			check.setId(position);
 			// Handle Completed Order
 			// check.setChecked(listCompleted.get(position));
 			check.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 				}
 			});
 			return rowView;
 		}
 	}
 
 	/**
 	 * Helper method to show the toast message
 	 **/
 	public static void showToastMessage(String message) {
 		// Activity a = new Activity();
 		// Context c = a.getApplicationContext();
 		// Toast.makeText(com.emergency, message, Toast.LENGTH_LONG).show();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.data_populate, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem theMenu) {
 		if (theMenu.getItemId() == R.id.menu_reload_db) {
 			new LoadDBTask().execute();
 		}
 		return false;
 	}
 
 	// Populate Medicine Database
 	private class LoadDBTask extends AsyncTask<Void, Void, Void> {
 
 		@Override
 		protected Void doInBackground(Void... params) {
 			// Clean all data
 			dbHelper.deleteAllMedicines();
 			// Add some data
 			dbHelper.insertMedicines();
 			return (null);
 		}
 
 		@Override
 		public void onPostExecute(Void arg0) {
 			Toast.makeText(getApplication(), "Medicine Database is Populated",
 					Toast.LENGTH_SHORT).show();
 		}
 	}
 
 	// TO BE REMOVED
 	private class FakeOrder extends AsyncTask<URL, Integer, Long> {
 		protected Long doInBackground(URL... urls) {
 
 			try {
 
 				HttpClient client = new DefaultHttpClient();
 				HttpPost post = new HttpPost(
 						"http://dabix.no-ip.org/create_order/");
 
 				/**
 				 * required_params = ['name', 'orderTime', 'givenTime',
 				 * 'dosage', 'route', 'status']
 				 * 
 				 */
 				String name = "Morphine";
 				String orderTime = "";
 				String givenTime = "";
 				String dosage = "5 g";
 				String route = "IV";
 				String status = "false";
 
 				// System.out.println(android_id);
 				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
 						1);
 				nameValuePairs.add(new BasicNameValuePair("name", name));
 				nameValuePairs.add(new BasicNameValuePair("orderTime",
 						orderTime));
 				nameValuePairs.add(new BasicNameValuePair("givenTime",
 						givenTime));
 				nameValuePairs.add(new BasicNameValuePair("dosage", dosage));
 				nameValuePairs.add(new BasicNameValuePair("route", route));
 				nameValuePairs.add(new BasicNameValuePair("status", status));
 				nameValuePairs
 						.add(new BasicNameValuePair("codeblue_id", codeId));
 
 				System.out.println("current codeblue: --" + codeId);
 				// nameValuePairs.add(new BasicNameValuePair("android_id",
 				// android_id));
 				post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
 				HttpResponse response = client.execute(post);
 				BufferedReader reader = new BufferedReader(
 						new InputStreamReader(response.getEntity().getContent()));
 
 				StringBuilder builder = new StringBuilder();
 				for (String line = null; (line = reader.readLine()) != null;) {
 					builder.append(line).append("\n");
 				}
 
 				JSONTokener tokener = new JSONTokener(builder.toString());
 				// JSONObject json = new JSONObject(tokener);
 				JSONObject userInfo = new JSONObject(tokener);
 
 				System.out.println(userInfo.toString());
 
 			} catch (Exception e) {
 				System.out.println("Got error: " + e.toString());
 				return (long) 1;
 				// Toast.makeText(getApplicationContext(), "Failed" ,
 				// Toast.LENGTH_LONG).show();
 			} finally {
 				System.out.println("Finally");
 			}
 			return (long) 0;
 
 		}
 
 	}
 
 	// AsyncTask that packages the Order object into JSON format and POSTs it to
 	// the server
 	private class RealOrder extends AsyncTask<Order, Integer, Long> {
 		protected Long doInBackground(Order... ord) {
 
 			try {
 
 				HttpClient client = new DefaultHttpClient();
 				HttpPost post = new HttpPost(
 						"http://dabix.no-ip.org/create_order/");
 
 				/**
 				 * required_params = ['name', 'orderTime', 'givenTime',
 				 * 'dosage', 'route', 'status']
 				 * 
 				 */
 				String name = ord[0].getName();
 				String orderTime = ord[0].getOrderTime();
 				String givenTime = ord[0].getGivenTime();
 				String dosage = ord[0].getDosage();
 				String route = ord[0].getRoute();
 				String status = "false";
 
 				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
 						1);
 				nameValuePairs.add(new BasicNameValuePair("name", name));
 				nameValuePairs.add(new BasicNameValuePair("orderTime",
 						orderTime));
 				nameValuePairs.add(new BasicNameValuePair("givenTime",
 						givenTime));
 				nameValuePairs.add(new BasicNameValuePair("dosage", dosage));
 				nameValuePairs.add(new BasicNameValuePair("route", route));
 				nameValuePairs.add(new BasicNameValuePair("status", status));
 
 				post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
 				HttpResponse response = client.execute(post);
 				BufferedReader reader = new BufferedReader(
 						new InputStreamReader(response.getEntity().getContent()));
 
 				StringBuilder builder = new StringBuilder();
 				for (String line = null; (line = reader.readLine()) != null;) {
 					builder.append(line).append("\n");
 				}
 
 				JSONTokener tokener = new JSONTokener(builder.toString());
 				JSONObject userInfo = new JSONObject(tokener);
 
 				System.out.println(userInfo.toString());
 
 			} catch (Exception e) {
 				System.out.println("Got error: " + e.toString());
 				return (long) 1;
 			} finally {
 				System.out.println("Finally");
 			}
 			return (long) 0;
 
 		}
 
 	}
 
 	// ENDs HERE
 }
