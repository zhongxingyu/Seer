 package test.BusTUC.Main;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.prefs.Preferences;
 
 import test.BusTUC.R;
 import test.BusTUC.Main.Homescreen.LocationListenerThread;
 import test.BusTUC.Speech.DummyObj;
 import test.BusTUC.Speech.ExtAudioRecorder;
 import test.BusTUC.Speech.HTTP;
 import test.BusTUC.Speech.MfccMaker;
 import test.BusTUC.Speech.SpeechAnswer;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.ProgressDialog;
 import android.appwidget.AppWidgetManager;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.DialogInterface.OnCancelListener;
 import android.graphics.Bitmap;
 import android.graphics.Typeface;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Environment;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnLongClickListener;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.ImageButton;
 import android.widget.RemoteViews;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class WidgetClick extends Activity
 {
 	public static Location currentlocation;
 	LocationManager locationManager; // Location Manager
 	String provider; // Provider
 	LocationListener locationListener;
 	private static final int REQUEST_CODE = 1234;
 	final ExtAudioRecorder ext = ExtAudioRecorder.getInstance(false);
 	boolean stopRecording = false;
 	File wav = null;
 	File config = null;// new File(sdCard.getAbsolutePath() +
 	File mfccFile = null;
 	File sdCard = Environment.getExternalStorageDirectory();
 	int numStops;
 	int numStopsOnMap;
 	int dist;
 	boolean fancyOracle;
 	Context context = this;
 	final ArrayList<Thread> threadList = new ArrayList<Thread>();
 	SharedPreferences preferences;
 
 	// int storedPreference = preferences.getInt("storedInt", 0);
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.dialoglayout);
 		LayoutParams params = getWindow().getAttributes();
 		params.height = 300;
 		params.width = 260;
 		getWindow().setAttributes(
 				(android.view.WindowManager.LayoutParams) params);
 		preferences = PreferenceManager.getDefaultSharedPreferences(context);
 
 		TextView tw = (TextView) findViewById(R.id.txtValue);
 		tw.setText("Hold for å snakke");
 
 		ImageButton button = (ImageButton) findViewById(R.id.btnDone);
 		final HTTP http = new HTTP();
 		// final ArrayList<Thread> threadList = new ArrayList<Thread>();
 		final double[] coords = new double[2];
 		
 		// HAXX
 		
 		final int[] attemptCounter = new int[1];
 		button.setOnTouchListener(new OnTouchListener()
 		{
 
 			@Override
 			public boolean onTouch(View v, MotionEvent event)
 			{
 				if (event.getAction() == MotionEvent.ACTION_DOWN)
 				{
 					startVoiceRecognitionActivity();
 
 				} else if (event.getAction() == MotionEvent.ACTION_UP)
 				{
 					stopRecording = true;
 					ext.stop();
 					coords[0] = currentlocation.getLatitude();
 					coords[1] = currentlocation.getLongitude();
 					long first = System.nanoTime();
 					// DummyObj dummy = http.sendPost(filePath2);
 
 					// Wait for all threads to finish
 					for (Thread t : threadList)
 					{
 						try
 						{
 							t.join();
 						} catch (InterruptedException e)
 						{
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 					}
 
 					if (wav.exists())
 					{
 
 						mfccFile = new File(sdCard.getAbsolutePath()
 								+ "/asr/test.mfc");
 						MfccMaker mfcc = new MfccMaker(
 								config.getAbsolutePath(),
 								wav.getAbsolutePath(), mfccFile
 										.getAbsolutePath());
 						mfcc.setupSphinx();
 						mfcc.produceFeatures();
 						DummyObj dummy = http.sendPost(
 								mfccFile.getAbsolutePath(), context, coords[0],
 								coords[1]);
 
 						final String speechAnswer = dummy.getAnswer();
 
 						AlertDialog.Builder alert2 = new AlertDialog.Builder(
 								context);
 						alert2.setMessage(speechAnswer);
 
 						alert2.setPositiveButton("Riktig",
 								new DialogInterface.OnClickListener()
 								{
 									@Override
 									public void onClick(DialogInterface dialog,
 											int whichButton)
 									{
 										AppWidgetManager appWidgetManager = AppWidgetManager
 												.getInstance(context);
 
 										RemoteViews remoteViews = new RemoteViews(
 												context.getPackageName(),
 												R.layout.widget_layout);
 
 										ComponentName thisWidget = new ComponentName(
 												context, Widget.class);
 
 										Intent answerScreen = new Intent(
 												context, Answer.class);
 										try
 										{
 											ArrayList<Route> routeSuggestions = Helpers
 													.runServer(speechAnswer,
 															currentlocation,
 															numStops, dist,
 															context);
 
 											answerScreen
 													.putParcelableArrayListExtra(
 															"test",
 															routeSuggestions);
 
 											answerScreen.putExtra("speech",
 													true);
 											Typeface clock = Typeface
 													.createFromAsset(
 															getApplicationContext()
 																	.getAssets(),
 															"dotmatrix.ttf");
 											System.out
 													.println("Broadcast sent back to activity");
 											appWidgetManager.updateAppWidget(
 													thisWidget, remoteViews);
 											context.startActivity(answerScreen);
 											finish();
 										} catch (Exception e)
 										{
 											e.printStackTrace();
 										}
 
 									}
 								});
 						alert2.setNegativeButton("Feil",
 								new DialogInterface.OnClickListener()
 								{
 
 									@Override
 									public void onClick(DialogInterface dialog,
 											int whichButton)
 									{
 										attemptCounter[0]++; 
 										System.out.println("ATTEPT: " + attemptCounter[0]);
										if (attemptCounter[0] < 2)
 										{
 											http.blackList(coords[0],
 													coords[1], speechAnswer,
 													context);
 
 											Toast.makeText(context,
 													"Prøv på nytt",
 													Toast.LENGTH_SHORT).show();
 										}
 										else
 										{
 											Toast.makeText(context,
 													"Klarte ikke å finne noe, starter TABuss",
 													Toast.LENGTH_SHORT).show();
 											attemptCounter[0] = 0;
 											Intent intent = new Intent(context,Homescreen.class);
 											context.startActivity(intent);
 										}
 									}
 								}); 
 
 						alert2.show();
 
 					}
 				}
 				return false;
 			}
 
 		});
 
 		File configFolder = new File(sdCard.getAbsolutePath() + "/asr");
 		if (!configFolder.exists())
 			configFolder.mkdir();
 		config = new File(configFolder.getAbsolutePath() + "/config.xml");
 		if (!config.exists())
 		{
 			BufferedWriter bufferedWriter = null;
 			// HTTP http1 = new HTTP();
 			// Get the config file to be used with Sphinx
 			try
 			{
 				StringBuffer sb = http
 						.executeHttpGet("http://idi.ntnu.no/~chrimarc/config.xml");
 				if (sb != null)
 				{
 					bufferedWriter = new BufferedWriter(new FileWriter(
 							config.getAbsolutePath()));
 					bufferedWriter.write(sb.toString());
 				}
 			} catch (Exception e)
 			{
 				e.printStackTrace();
 			} finally
 			{
 				if (bufferedWriter != null)
 					try
 					{
 						bufferedWriter.close();
 					} catch (IOException e)
 					{
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 			}
 		}
 		new StartUpThread(context).execute();
 		adjustSettings();
 	}
 
 	public void adjustSettings()
 	{
 
 		PreferenceManager.setDefaultValues(context, R.layout.preference, false);
 		String foo = preferences.getString("num1", "");
 		numStops = Integer.parseInt(foo);
 		String foo2 = preferences.getString("num2", "");
 		numStopsOnMap = Integer.parseInt(foo2);
 		String foo3 = preferences.getString("num3", "");
 		dist = Integer.parseInt(foo3);
 		fancyOracle = preferences.getBoolean("Orakelvalg", fancyOracle);
 		System.out.println("onCreate: FancyOracle: " + fancyOracle);
 	}
 
 	public void notifyMessage(Intent intent, String text)
 	{
 		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
 				intent, 0);
 		NotificationManager notificationManager = (NotificationManager) context
 				.getSystemService(Context.NOTIFICATION_SERVICE);
 		Notification noty = new Notification(R.drawable.icon,
 				"Ruteopplysning mottatt!", System.currentTimeMillis());
 		noty.setLatestEventInfo(context, "Ruteopplysning", text, contentIntent);
 		noty.flags = Notification.DEFAULT_LIGHTS
 				| Notification.FLAG_AUTO_CANCEL;
 		notificationManager.notify(1, noty);
 	}
 
 	public void startVoiceRecognitionActivity()
 	{
 
 		/*
 		 * AlertDialog.Builder alert = new AlertDialog.Builder(this); // First
 		 * 
 		 * alert.setTitle("Snakk i vei");
 		 * alert.setMessage("Trykk når du er ferdig");
 		 * alert.setPositiveButton("Avslutt", new
 		 * DialogInterface.OnClickListener() {
 		 * 
 		 * @Override public void onClick(DialogInterface dialog, int
 		 * whichButton) { stopRecording = true; ext.stop(); long first =
 		 * System.nanoTime(); // DummyObj dummy = http.sendPost(filePath2);
 		 * 
 		 * // Wait for all threads to finish for (Thread t : threadList) { try {
 		 * t.join(); } catch (InterruptedException e) { // TODO Auto-generated
 		 * catch block e.printStackTrace(); } } // go(); //
 		 * context.startActivity(intent); // new
 		 * OracleThread(context).execute();
 		 * 
 		 * } }); alert.show();
 		 */
 
 		stopRecording = false;
 
 		// Send wav or MFCC. TODO: Create setting
 		// final boolean sendWav = false;
 		// threadList.add(cbrThread);
 		// Get ASR result
 		Thread speechThread = new Thread(new Runnable()
 		{
 			public void run()
 			{
 				wav = new File(sdCard.getAbsolutePath() + "/asr/liverpool.wav");
 				ext.setOutputFile(wav.getAbsolutePath());
 				ext.prepare();
 				ext.start();
 
 				while (!stopRecording)
 				{
 					ext.record();
 				}
 				// If file has been created, perform feature extraction
 
 				ext.reset();
 
 			}
 
 		});
 
 		speechThread.start();
 		threadList.add(speechThread);
 	}
 
 	public static ArrayList<Route> run(Context context)
 	{
 
 		SharedPreferences pref = PreferenceManager
 				.getDefaultSharedPreferences(context);
 		String sAnsw = pref.getString("speechAnswer", "");
 		double lat = Double.parseDouble(pref.getString("lat", ""));
 		double lon = Double.parseDouble(pref.getString("lon", ""));
 		int numStops = pref.getInt("numStops", 0);
 		int dist = pref.getInt("dist", 0);
 		return Helpers.runServer2(sAnsw, lat, lon, numStops, dist, context);
 
 	}
 
 	public void finish()
 	{
 		System.exit(0);
 	}
 
 	private void createLocationListener()
 	{
 		locationListener = new LocationListener()
 		{
 
 			// This method runs whenever the criteria for change is met.
 			@Override
 			public void onLocationChanged(Location location)
 			{
 				System.out.println("LOCATIONLISTENER CALLED IN HOMESCREEN");
 				currentlocation = location;
 
 			}
 
 			@Override
 			public void onProviderDisabled(String provider)
 			{
 				Log.v("PROV", "PROV:DISABLED");
 				// TODO Auto-generated method stub
 			}
 
 			@Override
 			public void onProviderEnabled(String provider)
 			{
 				Log.v("PROV", "PROV:ENABLED");
 			}
 
 			@Override
 			public void onStatusChanged(String provider, int status,
 					Bundle extras)
 			{
 				Log.v("PROV", "PROV:STATUSCHANGE");
 			}
 		};
 	}
 
 	private void createLocationManager()
 	{
 		try
 		{
 			locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
 			Criteria criteria = new Criteria();
 			criteria.setAccuracy(Criteria.ACCURACY_FINE);
 			provider = locationManager.getBestProvider(criteria, true);
 
 		} catch (Exception e)
 		{
 			e.printStackTrace();
 			System.out.println("ERROR");
 			AlertDialog.Builder alert = new AlertDialog.Builder(this);
 			// First input dialog
 			alert.setTitle("Tilkoblingsproblem");
 			alert.setMessage("Ingen tilkobling, har du nettilgang?");
 			alert.setPositiveButton("Avslutt",
 					new DialogInterface.OnClickListener()
 					{
 						@Override
 						public void onClick(DialogInterface dialog,
 								int whichButton)
 						{
 							System.exit(0);
 						}
 					});
 			alert.show();
 		}
 	}
 
 	class StartUpThread extends AsyncTask<Void, Void, Void>
 	{
 		private Context context;
 		Intent intent;
 		ProgressDialog myDialog = null;
 		boolean check = false;
 
 		public StartUpThread(Context context)
 		{
 
 			this.context = context;
 		}
 
 		@Override
 		protected Void doInBackground(Void... params)
 		{
 			try
 			{
 				createLocationManager();
 			} catch (Exception e)
 			{
 				if (myDialog != null)
 				{
 					check = true;
 
 					myDialog.dismiss();
 				}
 			}
 
 			return null;
 		}
 
 		@Override
 		protected void onPreExecute()
 		{
 
 			try
 			{
 				myDialog = ProgressDialog.show(context, "Loading!",
 						"Finner lokasjon");
 				myDialog.setCancelable(true);
 				myDialog.setOnCancelListener(new OnCancelListener()
 				{
 
 					@Override
 					public void onCancel(DialogInterface dialog)
 					{
 						finish();
 						System.exit(0);
 
 					}
 				});
 
 				createLocationListener();
 
 				// Only request updates if > 500 ms and 10 m
 			} catch (Exception e)
 			{
 				if (myDialog != null)
 				{
 					myDialog.dismiss();
 				}
 				e.printStackTrace();
 
 			}
 
 		}
 
 		@Override
 		protected void onPostExecute(Void unused)
 		{
 			myDialog.dismiss();
 			if (check)
 			{
 				AlertDialog.Builder alert = new AlertDialog.Builder(context);
 				// First input dialog
 				alert.setTitle("Tilkoblingsproblem");
 				alert.setMessage("Ingen tilkobling, har du nettilgang?");
 				alert.setPositiveButton("Avslutt",
 						new DialogInterface.OnClickListener()
 						{
 							@Override
 							public void onClick(DialogInterface dialog,
 									int whichButton)
 							{
 								System.exit(0);
 
 							}
 						});
 				alert.show();
 			} else
 			{
 				if (provider == null)
 				{
 					System.err.println(" Fuck Up!");
 					provider = locationManager.getBestProvider(new Criteria(),
 							true);
 				}
 				locationManager.requestLocationUpdates(provider, 500, 10,
 						locationListener);
 				new LocationListenerThread(context).execute();
 			}
 
 		}
 	}
 
 	class LocationListenerThread extends AsyncTask<Void, Void, Void>
 	{
 		private Context context;
 		Intent intent;
 		ProgressDialog myDialog = null;
 		boolean noLoc = false;
 
 		public LocationListenerThread(Context context)
 		{
 
 			this.context = context;
 		}
 
 		@Override
 		protected Void doInBackground(Void... params)
 		{
 
 			try
 			{
 				boolean locCheck = false;
 				while (!locCheck)
 				{
 					if (currentlocation != null)
 					{
 						locCheck = true;
 					}
 
 				}
 
 			} catch (Exception e)
 			{
 				myDialog.dismiss(); //
 			}
 
 			return null;
 		}
 
 		@Override
 		protected void onPreExecute()
 		{
 
 			try
 			{
 				myDialog = ProgressDialog.show(context, "Loading!",
 						"Setter lokasjon");
 				myDialog.setCancelable(true);
 				myDialog.setOnCancelListener(new OnCancelListener()
 				{
 
 					@Override
 					public void onCancel(DialogInterface dialog)
 					{
 						finish();
 						System.exit(0);
 
 					}
 				});
 
 			} catch (Exception e)
 			{
 				e.printStackTrace();
 				myDialog.dismiss();
 			}
 		}
 
 		@Override
 		protected void onPostExecute(Void unused)
 		{
 
 			myDialog.dismiss();
 
 		}
 	}
 
 }
