 package spang.mobile;
 
 import utils.LogCatLogger;
 import utils.Logger;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Build;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.EditText;
 
 public class MainActivity extends Activity {
 
 	private NetworkServiceAdapter network;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		network = new NetworkServiceAdapter();
 		//network.startService(this);
 		
 		Logger.setLogger(new LogCatLogger());
 		if(Integer.parseInt(Build.VERSION.SDK) <= Build.VERSION_CODES.CUPCAKE)
 			findViewById(R.id.button1).setOnClickListener(new View.OnClickListener() {
 				public void onClick(View v) {
 					sendData(v);
 				}
 			});
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;		
 	}
 
 	public void sendData(View view){
 		
 		EditText text = (EditText)this.findViewById(R.id.editText1);
 		final String ip = text.getText().toString();
 
 		EditText number = (EditText)this.findViewById(R.id.editText2);
 		final int port = Integer.parseInt(number.getText().toString());
 		
 		
 		Thread thread = new Thread(new Runnable() {
 			
 			public void run() {
 				if(!network.isConnected())
 					network.connectTo(ip, port);
 			}
 		});
 		
 		thread.start();
 		
 		
 		Intent intent = new Intent(this, MouseActivity.class);
 		this.startActivity(intent);
 
 		
 	/*	Intent intent = new Intent(this, NetworkService.class);
 		intent.putExtra(NetworkService.CONNECTION_ADDRESS, ip);
 		intent.putExtra(NetworkService.CONNECTION_PORT, port);
 		bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);*/
 		
 	}
 	
 	
 	@Override
 	protected void onStart() {
 		super.onStart();
 		this.network.bindService(this);
 	}
 	
 	@Override
 	protected void onStop() {
 		super.onStop();
 		this.network.unbindService(this);
 	}
 	
 	@Override 
 	protected void onDestroy() {
 		this.onDestroy();
 		this.network.stopService(this);
 	}
 
 	public void sendText(View view){
 		EditText text = (EditText)this.findViewById(R.id.editText1);
 		String ip = text.getText().toString();
 
 		Intent intent = new Intent(this, TextSenderActivity.class);
 		intent.putExtra("connection", ip);
 		this.startActivity(intent);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch(item.getItemId()){
 		case R.id.menu_settings: 
 			Intent intent = new Intent(this, PrefsActivity.class);
 			startActivity(intent);
 		}
 		return true;
 	}
 }
