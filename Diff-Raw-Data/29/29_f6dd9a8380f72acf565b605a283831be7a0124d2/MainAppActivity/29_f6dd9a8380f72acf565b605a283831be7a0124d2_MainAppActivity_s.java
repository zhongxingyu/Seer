 package team2.mainapp;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 
 public class MainAppActivity extends Activity {
 
 	static String s;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle icicle) {
 
 
 		super.onCreate(icicle);
 
 		setContentView(R.layout.main);
 		Log.d("Debug", "Created?");
 
 		Runnable runnable = new Runnable () {
 			public void run() {
 				String date = "1212/12/12 12:12:12";
 				while (true) {
 					try {
 						Thread.sleep(10000);
 						Date dateType = new Date();
 						s = TCPClient.go(date);
 						DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/d HH:mm:ss");
 						date = dateFormat.format(dateType);
 						Log.d("Debug", "Finished?");
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 				}
 			}
 		};
 		new Thread(runnable).start();
		while (true) {
			try {
				Thread.sleep(11111);
			} catch (InterruptedException e) {
				e.printStackTrace();
 			}
			Log.d("Debug", "Hello world");
			if (s == null)
				continue;
			Log.d("Debug", s);
			s = null;
		}
 	}
 }
