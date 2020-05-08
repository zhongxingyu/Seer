 package jp.dip.ysato.onsenplayer;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.ServiceConnection;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.TextView;
 
 public class PlayActivity extends Activity {
 	private SeekBar seekbar;
 	private ProgressDialog dialog;
 	private PlayerServiceReceiver receiver;
 	private Intent service;
 	private Bundle bundle;
 	private ImageButton playControlButton;
 	private TextView currentTime;
 	private boolean playing;
 	private ServiceConnection serviceConnection;
 	private Handler updateHandler;
 	
 	private ProgressDialog showDialog() {
 		ProgressDialog d;
 		d = new ProgressDialog(this);
 		d.setMessage(getString(R.string.getStream));
 		d.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 		d.show();
 		return d;
 	}
 	
 	class PlayerServiceReceiver extends BroadcastReceiver {
 		@Override
 		public void onReceive(Context arg0, Intent arg1) {
 			String action = arg1.getAction();
 			if (action.equals(PlayerService.Notify)) {
 				String message = arg1.getStringExtra("message");
 				if (message.equals(PlayerService.WaitStream)) {
 					dialog = showDialog();
 				}
 				if (message.equals(PlayerService.Resume)) {
 					dialog.cancel();
 					playing = true;
 				}
 				if (message.equals(PlayerService.RemotePause)) {
 					playControlButton.setImageResource(android.R.drawable.ic_media_play);
 					playing = false;
 				}
 				if (message.equals(PlayerService.RemotePlay)) {
 					playControlButton.setImageResource(android.R.drawable.ic_media_pause);
 					playing = true;
 				}
 			}
 		}
 	}
 	class LoadImageTask extends AsyncTask<String, Void, Bitmap> {
 		@Override
 		protected Bitmap doInBackground(String... arg0) {
 			// TODO Auto-generated method stub
 			Bitmap bitmap = null;
 			HttpClient httpClient = new DefaultHttpClient();
 			HttpGet get = new HttpGet(arg0[0]);
 			ByteArrayOutputStream imgout = new ByteArrayOutputStream();
 			for(int times = 10; times > 0; times--) {
 				try {
 					HttpResponse imgres = httpClient.execute(get);
 					imgres.getEntity().writeTo(imgout);
 					bitmap = BitmapFactory.decodeByteArray(imgout.toByteArray(), 0, imgout.size());
 					break;
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 				}
 			}
 			httpClient.getConnectionManager().shutdown();
 			return bitmap;
 		}
 		@Override
 		protected void onPostExecute(Bitmap bitmap) {
 			ImageView imageView = (ImageView) findViewById(R.id.playerImage);
 			imageView.setImageBitmap(bitmap);
 		}
 	}
 	@Override
     public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.player);
 		currentTime = (TextView) findViewById(R.id.currentPosition);
     	seekbar = (SeekBar) findViewById(R.id.playPosition);
     	seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
 
 			@Override
 			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
 				// TODO Auto-generated method stub
 			}
 
 			@Override
 			public void onStartTrackingTouch(SeekBar arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public void onStopTrackingTouch(SeekBar arg0) {
 				// TODO Auto-generated method stub
 				Intent intent = new Intent(PlayActivity.this, PlayerService.class);
 				intent.setAction(PlayerService.SEEK);
 				intent.putExtra("position", arg0.getProgress());
 				startService(intent);
 			}
     	});
     	playControlButton = (ImageButton) findViewById(R.id.playControlButton);
     	playControlButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				// TODO Auto-generated method stub
 				if (playing) {
 					playControlButton.setImageResource(android.R.drawable.ic_media_play);
 					Intent intent = new Intent(PlayActivity.this, PlayerService.class);
 					intent.setAction(PlayerService.PAUSE);
 					startService(intent);
 				} else {
 					playControlButton.setImageResource(android.R.drawable.ic_media_pause);
 					Intent intent = new Intent(PlayActivity.this, PlayerService.class);
 					intent.setAction(PlayerService.PLAY);
 					startService(intent);
 				}
 			}
     	});
     	if (savedInstanceState != null) {
     		bundle = savedInstanceState.getBundle("playing");
     	} else {
     		Intent intent = getIntent();
     		bundle = intent.getBundleExtra("program");
     	}
 		IntentFilter filter = new IntentFilter();
 		filter.addAction(PlayerService.Notify);
 		receiver = new PlayerServiceReceiver();
 		registerReceiver(receiver, filter);
 		service = new Intent(this, PlayerService.class);
 		service.putExtra("program", bundle);
 		service.setAction(PlayerService.START);
 		startService(service);
     	String program[] = bundle.getStringArray("program");
     	new LoadImageTask().execute(program[2]);
     	updateHandler = new Handler();
     }
 	@Override
 	public void onDestroy() {
 		unregisterReceiver(receiver);
 		super.onDestroy();
 	}
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		outState.putBundle("playing", this.bundle);
 		super.onSaveInstanceState(outState);
 	}
 	@Override
 	public void onResume() {
 		super.onResume();
     	String program[] = bundle.getStringArray("program");
     	String no = program[4];
     	try {
     		int n = Integer.valueOf(no);
     		no = String.format("第%d回", n);
     	} catch(NumberFormatException e) {
     	}
     	StringBuffer t = new StringBuffer();
     	t.append(program[3]);
     	t.append('\n');
     	t.append(no);
     	TextView tv = (TextView) findViewById(R.id.playerText);
     	tv.setText(t.toString());
     	Intent intent = new Intent(this, PlayerService.class);
     	serviceConnection = new ServiceConnection() {
 			@Override
 			public void onServiceConnected(ComponentName arg0, IBinder arg1) {
 				// TODO Auto-generated method stub
 				((PlayerService.PlayerServiceBinder)arg1).registerActivity(PlayActivity.this);
 			}
 
 			@Override
 			public void onServiceDisconnected(ComponentName arg0) {
 				// TODO Auto-generated method stub
 				
 			}
     		
     	};
     	bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
 	}
 	@Override
 	public void onPause() {
 		unbindService(serviceConnection);
 		super.onPause();
 	}
 	public void setDuration(int dur) {
 		// TODO Auto-generated method stub
 		final int duration = dur;
 		updateHandler.post(new Runnable() {
 			@Override
 			public void run() {
 				// TODO Auto-generated method stub
 				TextView textView = (TextView) findViewById(R.id.duration);				
 				textView.setText(String.format("%02d:%02d", duration / 60, duration % 60));
 				seekbar.setMax(duration);
 				if (dialog != null)
 					dialog.cancel();
 			}
 		});
 		playing = true;
 	}
 	public void setPosition(int pos) {
 		// TODO Auto-generated method stub
 		final int position = pos;
 		updateHandler.post(new Runnable() {
 			@Override
 			public void run() {
 				// TODO Auto-generated method stub
 				currentTime.setText(String.format("%02d:%02d", position / 60, position % 60));
 				seekbar.setProgress(position);
 			}
 		});
 	}
 }
