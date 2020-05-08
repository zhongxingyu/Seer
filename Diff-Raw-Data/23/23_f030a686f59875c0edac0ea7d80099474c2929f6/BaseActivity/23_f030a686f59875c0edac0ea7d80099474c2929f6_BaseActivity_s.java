 package de.boomboxbeilstein.android2.ui;
 
 import java.io.File;
 import java.io.IOException;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.media.AudioManager;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ImageButton;
 import de.boomboxbeilstein.android2.R;
 import de.boomboxbeilstein.android2.ReportingErrorHandler;
 import de.boomboxbeilstein.android2.ServiceController;
 import de.boomboxbeilstein.android2.UpdateService;
 import de.boomboxbeilstein.android2.utils.AppInfo;
 import de.boomboxbeilstein.android2.utils.Files;
 
public class BaseActivity extends Activity {
 	private ServiceController service;
 	
 	private static final String TAG = "BaseActivity";
 	private static final String ERROR_FILE = "error.log";
 	
 	public void onCreate(Bundle savedInstanceState, int layoutID) {
 		super.onCreate(savedInstanceState);
 		initApp();
 		setTheme(android.R.style.Theme_Black_NoTitleBar);
 		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
 		setContentView(R.layout.overall);
 		LayoutInflater.from(this).inflate(layoutID, (ViewGroup) findViewById(R.id.content));
 		
 		service = new ServiceController(this);
 		initUI();
 		updateServiceUI();
 	}
 	
 	protected void onResume() {
 		super.onResume();
 		updateServiceUI();
 		UpdateService.runIfNeccessary(this);
 	}
 	
 	private void initUI() {
 		final ImageButton play = (ImageButton) findViewById(R.id.play_button);
 		final ImageButton stop = (ImageButton) findViewById(R.id.stop_button);
 		
 		play.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				service.play();
 				play.setVisibility(View.GONE);
 				stop.setVisibility(View.VISIBLE);
 			}
 		});
 		
 		stop.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				service.stop();
 				play.setVisibility(View.VISIBLE);
 				stop.setVisibility(View.GONE);
 			}
 		});
 	}
 	
 	private void updateServiceUI() {
 		final ImageButton play = (ImageButton) findViewById(R.id.play_button);
 		final ImageButton stop = (ImageButton) findViewById(R.id.stop_button);
 		
 		if (ServiceController.isPlaying()) {
 			play.setVisibility(View.GONE);
 			stop.setVisibility(View.VISIBLE);
 		} else {
 			play.setVisibility(View.VISIBLE);
 			stop.setVisibility(View.GONE);
 		}
 	}
 	
 	private void initApp() {
 		AppInfo.getFullVersion(this);
 		AppInfo.getVersion(this);
 		
 		readStacktraceFile();
 		Thread.setDefaultUncaughtExceptionHandler(new ReportingErrorHandler(getFilesDir() + ERROR_FILE));
 	}
 	
 	private void readStacktraceFile() {
 		File errorFile = new File(getFilesDir() + ERROR_FILE);
 		if (errorFile.exists()) {
 			String stacktrace;
 			try {
 				stacktrace = Files.readFileAsString(errorFile.getPath());
 			} catch (IOException e) {
 				Log.e(TAG, "Error reading stacktrace file");
 				e.printStackTrace();
 				return;
 			}
 			errorFile.delete();
 
 			Intent intent = new Intent(this, ErrorActivity.class);
 			intent.putExtra("stacktrace", stacktrace);
 			intent.putExtra("time", errorFile.lastModified());
 			startActivity(intent);
 		}
 	}
 }
