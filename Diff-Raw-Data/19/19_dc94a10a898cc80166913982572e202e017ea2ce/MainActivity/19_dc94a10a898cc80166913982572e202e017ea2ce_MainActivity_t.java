 package org.punbutton;
 
 import android.app.Activity;
import android.media.AudioManager;
import android.media.SoundPool;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
 	private final int[] PUNS = { R.string.pun, R.string.ricky, R.string.argo };
 	private int nextPun = 0;
	private SoundPool soundPool;
	private int PunSound;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
		soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
		PunSound = soundPool.load(this,R.raw.pun,1);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 	public void playPun(View view) {
		try{
			soundPool.play(PunSound, 1, 1, 1, 0, 1f);
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
