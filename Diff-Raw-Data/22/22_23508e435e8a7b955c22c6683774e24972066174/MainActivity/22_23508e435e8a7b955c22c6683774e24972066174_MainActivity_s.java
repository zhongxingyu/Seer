 package org.punbutton;
 
 import android.app.Activity;
import android.media.MediaPlayer;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
 	private final int[] PUNS = { R.string.pun, R.string.ricky, R.string.argo };
 	private int nextPun = 0;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 	public void playPun(View view) {
		try {
			MediaPlayer mp = MediaPlayer.create(getApplicationContext(),
					R.raw.pun);
			mp.start();

 			TextView punText = (TextView) findViewById(R.id.pun_text);
 			punText.setText(PUNS[nextPun++]);
 			if (nextPun > 2) {
 				nextPun = 0;
 			}
		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}

 }
