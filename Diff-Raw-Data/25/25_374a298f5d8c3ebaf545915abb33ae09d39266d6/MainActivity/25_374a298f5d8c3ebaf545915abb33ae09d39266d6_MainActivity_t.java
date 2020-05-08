 package com.dreamlink.game.kickball;
 
 import com.dreamlink.aidl.Communication;
 import com.dreamlink.aidl.OnCommunicationListenerExternal;
 import com.dreamlink.aidl.User;
 import com.dreamlink.game.kickball.GameView.BallCallback;
 import com.dreamlink.game.kickball.net.ProtocolDecoder;
 import com.dreamlink.game.kickball.net.ProtocolEncoder;
 
 import android.os.Bundle;
 import android.os.IBinder;
 import android.os.RemoteException;
 import android.app.Activity;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.pm.ActivityInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.widget.Toast;
 
 public class MainActivity extends Activity implements BallCallback,
 		ProtocolDecoder.Callback {
	private final static String TAG = "MainActivity";
 	private GameView mGameView;
 	private ProtocolDecoder mProtocolDecoder;
 	private ServiceConnection connection;
 	private Communication communication;
 	private Intent filter;
 	private final String INTENT_STRING = "com.dreamlink.communication.ComService";
 	private int app_id;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		mGameView = new GameView(this);
 		mGameView.setBallCallback(this);
 
 		setContentView(mGameView);
 
 		mProtocolDecoder = new ProtocolDecoder(this);
 
 		connection = new ServiceConnection() {
 
 			@Override
 			public void onServiceDisconnected(ComponentName name) {
 				communication = null;
 			}
 
 			@Override
 			public void onServiceConnected(ComponentName name, IBinder service) {
 				communication = Communication.Stub.asInterface(service);
 				regitserListener(true);
 			}
 		};
 		filter = new Intent(INTENT_STRING);
 		app_id = getAppID();
		boolean result = bindService(filter, connection,
				Context.BIND_AUTO_CREATE);
		if (result) {
			Log.d(TAG, "bind service success.");
		} else {
			Toast.makeText(this, "Bind communication service fail.",
					Toast.LENGTH_LONG).show();
			Log.e(TAG, "bind service fail.");
		}
 	}
 
 	// Remote service begin
 
 	private void regitserListener(boolean flag) {
 		if (communication == null) {
 			return;
 		}
 		try {
 			if (flag) {
 				communication.registListenr(communicationListenerExternal,
 						app_id);
 			} else {
 				communication.unRegistListenr(communicationListenerExternal);
 			}
 		} catch (RemoteException e) {
 			e.printStackTrace();
 
 		}
 	}
 
 	private OnCommunicationListenerExternal communicationListenerExternal = new OnCommunicationListenerExternal.Stub() {
 
 		@Override
 		public void onUserDisconnected(User user) throws RemoteException {
 
 		}
 
 		@Override
 		public void onUserConnected(User user) throws RemoteException {
 
 		}
 
 		@Override
 		public void onReceiveMessage(byte[] msg, User sendUser)
 				throws RemoteException {
 			handleMessage(msg);
 		}
 	};
 
 	private int getAppID() {
 		try {
 			ActivityInfo info = this.getPackageManager().getActivityInfo(
 					getComponentName(), PackageManager.GET_META_DATA);
 			return info.metaData.getInt("APPID");
 		} catch (NameNotFoundException e) {
 			e.printStackTrace();
 			Log.e("DreamLink", "Get App ID error!");
 		}
 		return 0;
 	}
 
 	private void sendMessage(byte[] data) {
 		if (communication == null) {
 			Log.e(TAG, "sendMessage fail, communication is null");
 			return;
 		}
 		try {
 			communication.sendMessage(data, app_id, null);
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		}
 	}
 
 	// Remote service end.
 
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		if (mGameView.onTouchEvent(event)) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	@Override
 	public void onBallOut(float x, float speedX, float speedY) {
 		// Send message to competitor.
 		// In competitor view, the speedY is in opposite direction.
 		speedY = speedY * -1;
 		byte[] data = ProtocolEncoder.encodeBallOut(x, speedX, speedY);
 		sendMessage(data);
 	}
 
 	@Override
 	public void onLoseOneGoal() {
 		// Send message to competitor.
 		byte[] data = ProtocolEncoder.encodeLoseOneGoal();
 		sendMessage(data);
 	}
 
 	private void quitGame() {
 		// Send message to competitor.
 		byte[] data = ProtocolEncoder.encodeQuitGame();
 		sendMessage(data);
 
 	}
 
 	private void handleMessage(byte[] data) {
 		mProtocolDecoder.decode(data);
 	}
 
 	@Override
 	public void onBallcome(float x, float speedX, float speedY) {
 		mGameView.ballComesFromCompetitor(x, speedX, speedY);
 	}
 
 	@Override
 	public void onYouWinOnePoint() {
 		mGameView.winOneGoal();
 	}
 
 	@Override
 	public void onPlayerQuit() {
 		Toast.makeText(this, "Player leave the game.", Toast.LENGTH_LONG)
 				.show();
 		finish();
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		quitGame();
 	}
 
 }
