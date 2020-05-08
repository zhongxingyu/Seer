 package com.example.tompomodoros;
 
 import java.util.Timer;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ProgressBar;
 
 public class TomPomodoros extends Activity {
 	private ProgressBar progressbar;
 	private Button button_start, button_cancel, button_history;
 
 	protected static final int STOP = 0x10000;
 	protected static final int NEXT = 0x10001;
 
 	private int iCount = 0;
	private int TotalLength = 4; // 60*25=1500

 	private int tomatoCount = 0;
 
 	Timer timer_tmp;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		button_start = (Button) findViewById(R.id.button1);
 		button_cancel = (Button) findViewById(R.id.button2);
 		button_history = (Button) findViewById(R.id.button3);
 		progressbar = (ProgressBar) findViewById(R.id.progressBar1);
 		progressbar.setIndeterminate(false);
 
 		button_start.setOnClickListener(new Button.OnClickListener() {
 			public void onClick(View v) {
 				// һ߳,ÿ벽Ϊ1,100%ʱֹͣ
 				progressbar.setVisibility(View.VISIBLE);
 				progressbar.setMax(TotalLength);
 				progressbar.setProgress(0);
 
 				Timer timer = new Timer();
 				timer.schedule(new ProgressbarTask(), 0, 1000);
 				timer_tmp = timer;
 			}
 		});
 
 		button_cancel.setOnClickListener(new Button.OnClickListener() {
 			public void onClick(View v) {
 				iCount = 0;
 				progressbar.setProgress(0);
 				timer_tmp.cancel();
 			}
 		});

 		button_history.setOnClickListener(new Button.OnClickListener() {
 			public void onClick(View v) {
 				// Switch to report page
 				Intent intent = new Intent();
 				intent.setClass(TomPomodoros.this, History.class);

 				Bundle bundle = new Bundle();
 				bundle.putInt("mydate", tomatoCount);
 				intent.putExtras(bundle);

 				startActivity(intent);
 			}
 		});
 	}
 
 	private class ProgressbarTask extends java.util.TimerTask {
 		@Override
 		public void run() {
 			iCount += 1;
 			progressbar.setProgress(iCount);
			if (iCount == TotalLength) {
 				tomatoCount = tomatoCount + 1;
				timer_tmp.cancel();
				progressbar.setProgress(0);
				iCount = 0;
			}
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 }
