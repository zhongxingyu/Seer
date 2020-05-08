 package com.chessyoup;
 
 import java.io.IOException;
 import java.util.Properties;
 
 import android.app.Activity;
 import android.content.res.AssetManager;
import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.widget.TextView;
 
 import com.chessyoup.connector.ConnectionManager;
 import com.chessyoup.connector.ConnectionManagerListener;
 
 public class MainActivity extends Activity {
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		AssetManager manager = this.getAssets();
 		Properties p = new Properties();
 		
 		try {
 			p.load(manager.open("chessyoup.properties"));
 		} catch (IOException e) {
 			Log.e("MainActivity", "Error on reading configuration!");			
 			System.exit(1);
 		}
 		
 		Log.d("MainActivity", "on create");
 		setContentView(R.layout.main);
 		ChessYoUpRemoteService remoteService = new ChessYoUpRemoteService(p.getProperty("chessyoup_url"));
		final ConnectionManager connManager = ConnectionManagerFactory.getFactory()
 				.getGCMConnectionManager(remoteService, this.getApplicationContext());
 		final TextView mDisplay = (TextView) findViewById(R.id.display);
 		mDisplay.append("initialize manager" + "\n");
		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
 
 			@Override
			protected Void doInBackground(Void... params) {
				connManager.initialize(new ConnectionManagerListener() {

					@Override
					public void onInitialize(boolean status) {
						Log.d("MainActivity", "onInitialize :" + status);
					}
				});
				
				return null;
 			}
			
		};
		
		task.execute();		
 	}
 }
