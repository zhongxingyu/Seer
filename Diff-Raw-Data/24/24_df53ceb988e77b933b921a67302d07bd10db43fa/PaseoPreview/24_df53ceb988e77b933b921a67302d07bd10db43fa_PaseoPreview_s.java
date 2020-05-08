 package com.audiolab.areago;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.app.Activity;
 import android.app.KeyguardManager;
 import android.app.KeyguardManager.KeyguardLock;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.IntentFilter;
 import android.graphics.Bitmap;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.net.wifi.WifiConfiguration;
 import android.net.wifi.WifiInfo;
 import android.net.wifi.WifiManager;
 import android.os.Bundle;
 import android.os.Vibrator;
 import android.util.Log;
 import android.view.Window;
 import android.view.WindowManager;
 import android.webkit.WebView;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class PaseoPreview extends Activity  {
 	
 	LocationManager locManager;
 	LocationListener locationListener;
 	Paseo walk;
 	double[] nl = new double[2];
 
 	TextView status;
 	Vibrator v;
 	
 	//para el wifi
 	BroadcastReceiver receiver = null;
 	WifiConfiguration wifiConf;
 	WifiManager wifi;
 	
 	//timers
 	private Timer timer;
 	private TimerTask updateTask = new TimerTask() {
 	    @Override
 	    public void run() {
 	      wifi.startScan();
 	    }
 	  };
 	
 	KeyguardManager  myKeyGuard;
 	KeyguardLock lock;
 	
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		Log.d("AREAGO","En PaseoPreview");
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                 WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		setContentView(R.layout.paseo_preview);
 		setTitle("AREAGO : "+getIntent().getStringExtra("titulo"));
 		
 		//gestion bloqueo pantalla
 		myKeyGuard = (KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE);
         lock = myKeyGuard.newKeyguardLock(KEYGUARD_SERVICE);
         
         v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
 		
 		String JSONPoints = getIntent().getStringExtra("json");
 //		String lat = getIntent().getStringExtra("lat");
 //		String lon = getIntent().getStringExtra("lon");
 		
 		// Definimos paseo
 		walk = new Paseo(getIntent().getIntExtra("id", 0));
 		walk.setTitle(getIntent().getStringExtra("titulo"));
 		walk.setDescription(getIntent().getStringExtra("descripcion"));
 		walk.setExcerpt(getIntent().getStringExtra("excerpt"));
 		walk.setVibrator(v);
		walk.setBitmap((Bitmap) getIntent().getParcelableExtra("imagen"));
 		if (JSONPoints.length()>0) { walk.create_points(JSONPoints); } else {Toast.makeText(this,"Este paseo no tiene puntos",Toast.LENGTH_LONG).show();}
 		
 		
 //		((TextView)findViewById(R.id.titulo)).setText(getIntent().getStringExtra("titulo"));
 //		((TextView)findViewById(R.id.descripcion)).setText(getIntent().getStringExtra("descripcion"));
 		
 		if (walk.hasImage()) { ((ImageView)findViewById(R.id.imagen)).setImageBitmap(walk.getBitmap()); }
 		else { ((ImageView)findViewById(R.id.imagen)).setImageResource(R.drawable.areago_48dp);}
 		((ImageView)findViewById(R.id.imagen)).setAdjustViewBounds(true);
 
 		((TextView)findViewById(R.id.titulo_imagen)).setText(walk.getTitle());
 		String html = "<p style='font-style:oblique;text-align:justify;padding-left:30px;'>"+walk.getExcerpt()+"</p>"+
 				"<div content='description' style='text-align:justify;'>"+walk.getDescription()+"</div>";
 		//((WebView)findViewById(R.id.webview)).loadData(html, "text/html; charset=UTF-8", null);
 		((WebView)findViewById(R.id.webview)).loadDataWithBaseURL("fake://not/needed", html, "text/html", "utf-8", "");
 		
     	locManager = (LocationManager) getSystemService(LOCATION_SERVICE);
     	locationListener = new PaseoLocationListener();
     	
     	Criteria crit = new Criteria();
     	crit.setAccuracy(Criteria.ACCURACY_FINE);
     	locManager.getLastKnownLocation(locManager.getBestProvider(crit, true));
     	
     	//Vibraciones
     	
     	v.vibrate(300);
     	
     	//configure wifi
 		wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
 		WifiInfo info = wifi.getConnectionInfo();
         if (receiver == null) receiver = new WiFiScanReceiver(this);
 		registerReceiver(receiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
 		
 		//configuramos el timer para los wifis
 		timer = new Timer();
 		timer.schedule(updateTask, 0, 3000);
 	}
     	
 	public void onResume() {
 
 		super.onResume();
 		locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
 		lock.disableKeyguard();
 	}
 
 	public void onPause() {
 		super.onPause();
 		locManager.removeUpdates(locationListener); // Al volver a la pantalla de lista de paseo se debería parar...
 	}
 	
 	public void onStop() {
 		super.onStop();
 		this.walk.stop();
 		unregisterReceiver(receiver);
 	}
 	
 	// INFO: Gestión del GPS
 	private class PaseoLocationListener implements LocationListener {
 	
     	public void onLocationChanged(Location location) {
     		// TODO Auto-generated method stub
     		if (location != null) {
     			((TextView)findViewById(R.id.gps)).setText("Posición: "+location.getLatitude()+"/"+location.getLongitude()+"/"+location.getAccuracy());
     			SoundPoint nl = new SoundPoint(location);
     			Log.d("AREAGO","Location changed");
     			
     			// Miramos si el punto actual está dentro del radio de acción de algun punto del paseo.
     			// Si nos hemos movido
     			walk.check_collisions(nl);
 
     			}
     		}
 
     	public void onProviderDisabled(String provider) {
     		// TODO Auto-generated method stub
     		((TextView)findViewById(R.id.gps)).setText(provider+" desconectado");
     		Log.d("AREAGO","GPS Disable");
     	}
 
     	public void onProviderEnabled(String provider) {
     		// TODO Auto-generated method stub
     		((TextView)findViewById(R.id.gps)).setText("GPS Conectado:"+provider);
     		Log.d("AREAGO","GPS Enabled");
     	}
 
     	public void onStatusChanged(String provider, int status, Bundle extras) {
     		// TODO Auto-generated method stub
     		String st = "";
     		switch (status) {
     		case android.location.LocationProvider.AVAILABLE:
     			st="Disponible";
     		case android.location.LocationProvider.OUT_OF_SERVICE:
     			st="Desactivado";
     		case android.location.LocationProvider.TEMPORARILY_UNAVAILABLE:
     			st="Temporalmente desactivado";
     		}
     		((TextView)findViewById(R.id.gps)).setText("GPS Status:"+st);
     		Log.d("AREAGO","Status"+st);
     	}
 	}
 
 
     	
 }
 
 
