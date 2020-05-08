 package com.example.simplevrcontroller;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.util.List;
 
 import com.example.simplevrcontroller.cave.Cave;
 import com.example.simplevrcontroller.cave.CaveManager;
 import com.example.simplevrcontroller.networking.NetworkManager;
 import com.example.simplevrcontroller.networking.location.WirelessLocation;
 import com.example.simplevrcontroller.networking.location.WirelessLocation.AccuracyThreshold;
 import com.example.simplevrcontroller.networking.location.WirelessLocator;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Color;
 import android.graphics.PixelFormat;
 import android.media.Ringtone;
 import android.media.RingtoneManager;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.text.method.ScrollingMovementMethod;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnLongClickListener;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ScrollView;
 import android.widget.Spinner;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
 
 	private Connection connection;
 	private boolean connected;
 	private TextView tv;
 	private ScrollView tscroll;
 	private WirelessLocator locator;
 	private Handler h;
 	private Spinner spin;
 	private NetworkManager networker;
 
 	public static final String CAVES = "caves.xml";
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 
 		connected = false;
 
 		super.onCreate(savedInstanceState);
 		
 		getWindow().setFormat(PixelFormat.RGBA_8888);
 		getWindow().setBackgroundDrawableResource(R.drawable.techback);
 		
 		this.getActionBar().setHomeButtonEnabled(true);
 		
 		setContentView(R.layout.activity_main);
 		
 		tscroll = (ScrollView) this.findViewById(R.id.tscroller);
 
 		tv = (TextView) this.findViewById(R.id.log);
 		tv.setTextSize(12);
 		tv.setWidth(400);
 		tv.setMovementMethod(new ScrollingMovementMethod());
 		
 		//Load caves
 		try {
 			File f = new File(this.getFilesDir(), CAVES);
 			f.createNewFile();
 			CaveManager.getCaveManager().load(new FileInputStream(f));
 			
 		} catch (Exception e) {
 			log("Error loading caves: " + e.getMessage());
 			e.printStackTrace();
 		}
 		
 		//Add default caves
 		
 		CaveManager.getCaveManager().addCave(new Cave("Tester", "137.110.119.227", 12012, 0));
 		CaveManager.getCaveManager().addCave(new Cave("VROOMCalVR", "VROOMCalVR.calit2.net", 12012, -1));
 		CaveManager.getCaveManager().addCave(new Cave("DWall", "DWall.calit2.net", 12012, -1));
 		CaveManager.getCaveManager().addCave(new Cave("StarCave", "StarCave.calit2.net", 12012, -1));
 		CaveManager.getCaveManager().addCave(new Cave("NEXCave", "NEXCave.calit2.net", 12012, -1));
 		CaveManager.getCaveManager().addCave(new Cave("TourCave", "TourCave.calit2.net", 12012, -1));
 		
 		
 		//Spinner set up
 		spin = (Spinner) this.findViewById(R.id.Hosts);		
 		ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this,
 				android.R.layout.simple_spinner_item, android.R.id.text1);
 		spinnerAdapter
 				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		spin.setAdapter(spinnerAdapter);
 		
 		for(Cave c : CaveManager.getCaveManager().getCaves())
 			spinnerAdapter.add(c.getName());
 		
 		spinnerAdapter.notifyDataSetChanged();
 		spin.setBackgroundColor(Color.LTGRAY);
 		spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
 
 			@Override
 			public void onItemSelected(AdapterView<?> parent, View view,
 					int pos, long id) {
 				String s = parent.getItemAtPosition(pos).toString();
 
 				Cave c = CaveManager.getCaveManager().getCave(s);
 
 				log("Connecting to: " + c.getAddress());
 
 				connectToServer(c.getAddress(), c.getPort());
 			}
 
 			@Override
 			public void onNothingSelected(AdapterView<?> parent) {
 				
 				
 			}
 
 		});
 		
 		//Set up location handling
 		locator = new WirelessLocator(networker = new NetworkManager(this));
 		
 		h = new Handler();
 		h.post(new Runnable(){
 
 			@Override
 			public void run() {
 				
 				List<WirelessLocation> locs = locator.getCurrentLocation();
 				
 				if(locs.size() > 0){
 					WirelessLocation loc = locs.get(0);
 					if(loc.getLastLocateThreashold().equals(AccuracyThreshold.STRONG) || loc.getLastLocateThreashold().equals(AccuracyThreshold.STRONG))
 						for(int y = 0; y < spin.getAdapter().getCount(); y++)
 							if(spin.getAdapter().getItem(y).toString().equals(loc.getCave().getName())){
 								if(!connected){
 									spin.setSelection(y);
 									spin.callOnClick();
 								}
 								break;
 							}
 				}
 				
 				h.postDelayed(this, 1000);
 				
 			}
 			
 		});
 		
 		Button recon = ((Button) this.findViewById(R.id.connect));
 		recon.setOnClickListener(new OnClickListener(){
 
 			@Override
 			public void onClick(View arg0) {
 				
 				connectToServer(null, 0);
 				
 			}
 			
 		});
 		
 		BListener listener = new BListener(0);
 		Button bAll = ((Button) this.findViewById(R.id.buttonALL));
 		bAll.setOnClickListener(listener);
 		bAll.setOnLongClickListener(listener);
 		
 		listener = new BListener(4);
 		bAll = ((Button) this.findViewById(R.id.ButtonCOMP));
 		bAll.setOnClickListener(listener);
 		bAll.setOnLongClickListener(listener);
 		
 		listener = new BListener(6);
 		bAll = ((Button) this.findViewById(R.id.buttonHEALTHY));
 		bAll.setOnClickListener(listener);
 		bAll.setOnLongClickListener(listener);
 		
 		listener = new BListener(1);
 		bAll = ((Button) this.findViewById(R.id.buttonINF));
 		bAll.setOnClickListener(listener);
 		bAll.setOnLongClickListener(listener);
 		
 		listener = new BListener(2);
 		bAll = ((Button) this.findViewById(R.id.buttonINFSYM));
 		bAll.setOnClickListener(listener);
 		bAll.setOnLongClickListener(listener);
 		
 		listener = new BListener(3);
 		bAll = ((Button) this.findViewById(R.id.ButtonTIME));
 		bAll.setOnClickListener(listener);
 		bAll.setOnLongClickListener(listener);
 		
 		// Wrong
 		listener = new BListener(7);
 		bAll = ((Button) this.findViewById(R.id.buttonTOGGLE));
 		bAll.setOnClickListener(listener);
 		bAll.setOnLongClickListener(listener);
 		
 		listener = new BListener(5);
 		bAll = ((Button) this.findViewById(R.id.ButtonTOP200));
 		bAll.setOnClickListener(listener);
 		bAll.setOnLongClickListener(listener);
 		
 	}
 	
 	public Cave getCurrentCave(){
 		return CaveManager.getCaveManager().getCave(spin.getSelectedItem().toString());
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	
 	/**
 	 * Connects to the given server, or reconnects to the old one 
 	 * if no new server address is given.
 	 * @param server The server to connect to
 	 * @param port The port to connect on
 	 * @return True if it succeeded, false otherwise.
 	 */
 	public boolean connectToServer(String server, int port) {
 		
 		
 		
 		if(server != null){
 			
 			connection = new Connection(server, port);
 		}
 		
 		if(connection == null){
 			System.out.println("Connection not created!");
 			return false;
 		}
 
 		try {
 			Exception e = new ConnectionInitializer().execute(connection).get();
 			if (e != null)
 				throw e;
 
 			connected = true;
 			tv.setTextColor(Color.BLACK);
 			log("Connected!");
 			int def = getCurrentCave().getDefaultPreset();
 			if(def != -1){
 				log("Sending default: " + def);
 				connection.send(def);
 			}
 
 		} catch (Exception e1) {
 			// e1.printStackTrace();
 			tv.setTextColor(Color.RED);
 			log("Error Connecting: " + e1.getMessage());
 			connected = false;
 			return false;
 		}
 
 		return true;
 	}
 
 	public void log(String s) {
 		tv.append(s + "\n");
 		tscroll.fullScroll(View.FOCUS_DOWN);
 	}
 
 	private class BListener implements OnClickListener, OnLongClickListener {
 
 		private int num;
 
 		public BListener(int num) {
 			this.num = num;
 		}
 
 		@Override
 		public void onClick(View arg0) {
 			//Button b = (Button) arg0;
 
 			if (connected) {
 				try {
 					Uri notification = RingtoneManager
 							.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
 					Ringtone r = RingtoneManager.getRingtone(
 							getApplicationContext(), notification);
 					r.play();
 				} catch (Exception e) {
 				}
 
 				connection.send(num);
 				log("Sending " + num);
 				
 			} else {
 				try {
 					Uri notification = RingtoneManager
 							.getDefaultUri(RingtoneManager.TYPE_ALARM);
 					Ringtone r = RingtoneManager.getRingtone(
 							getApplicationContext(), notification);
 					r.play();
 					Thread.sleep(1000);
 					r.stop();
 				} catch (Exception e) {
 				}
 			}
 		}
 
 		@Override
 		public boolean onLongClick(View arg0) {
 			getCurrentCave().setDefaultPreset(num);
 			log("Set default preset for " + getCurrentCave().getName() + " to " + num);
 			return true;
 		}
 
 	}
 
 	private class ConnectionInitializer extends
 			AsyncTask<Connection, Void, Exception> {
 
 		@Override
 		protected Exception doInBackground(Connection... arg0) {
 			for (Connection c : arg0)
 				try {
 					c.init();
 					
 				} catch (Exception e) {
 					return e;
 				}
 			return null;
 		}
 
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item){
 		
 		if(item.getTitle().equals("Wireless Manager")){
 			
 			Intent intent = new Intent(this, WirelessActivity.class);
             intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
             startActivity(intent);
 			
 			return true;
 		}
 		
 		if(item.getTitle().equals("Gamepad")){
 			
 			Intent intent = new Intent(this, GamepadActivity.class);
             intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
             startActivity(intent);
 			
 			return true;
 		}
 		
 		switch (item.getItemId()) {
         case android.R.id.home:
             // app icon in action bar clicked; go home
             Intent intent = new Intent(this, MainActivity.class);
             intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
             startActivity(intent);
             return true;
         default:
             return super.onOptionsItemSelected(item);
 		}
 		
 	}
 	
 	@Override
 	public void onPause(){
 		
 		networker.pause();
 		
 		try {
 			File f= new File(this.getFilesDir(), CAVES);
 			f.delete();
 			f.createNewFile();
 			CaveManager.getCaveManager().save(new FileOutputStream(f));
 			
 			String s = "";
 			FileInputStream fis = new FileInputStream(f);
 			while (fis.available() > 0)
 				s = s + (char)fis.read();
 			fis.close();
 			System.out.println(s);
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		super.onPause();
 	}
 	
 	@Override
 	public void onResume(){
 
 		networker.resume();
 		
 		super.onResume();
 	}
 
 }
