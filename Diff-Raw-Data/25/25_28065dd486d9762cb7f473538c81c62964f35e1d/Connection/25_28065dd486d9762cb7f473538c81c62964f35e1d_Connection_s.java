 package su.jit.nko3.ten_nine_eight_seven;
 
 import static su.jit.nko3.ten_nine_eight_seven.CommonUtilities.TAG;
 import static su.jit.nko3.ten_nine_eight_seven.CommonUtilities.SERVER_NAME;
 
 import java.io.IOException;
 import java.net.Socket;
 import java.net.UnknownHostException;
 
 import android.location.Location;
 import android.os.AsyncTask;
 import android.os.Message;
 import android.util.Log;
 
 public class Connection {
 
 	final int FIRST_PORT = 5000;
 
 	private AsyncTask<Void, Void, Void> httpRequestsTask;
 	private int _userId;
 	private Location _location;
 	private boolean _isStarted = false;
 
 	public void start() {
 
 		httpRequestsTask = new AsyncTask<Void, Void, Void>() {
 			@Override
 			protected Void doInBackground(Void... params) {
 				Log.d(TAG, "Connection started.");
 
 				_userId = ServerProxy.register(_location);

 				_isStarted = true;
 				return null;
 			}
 
 			@Override
 			protected void onPostExecute(Void result) {
 				httpRequestsTask = null;
 			}
 		};
 		httpRequestsTask.execute(null, null, null);
 
 	}
 
 	public void update(Location location) {
 		_location = location;
 
 		if (!_isStarted) {
 			return;
 		}
 		httpRequestsTask = new AsyncTask<Void, Void, Void>() {
 			@Override
 			protected Void doInBackground(Void... params) {
 				_userId = ServerProxy.update(_userId, _location);
 				_isStarted = true;
 				return null;
 			}
 
 			@Override
 			protected void onPostExecute(Void result) {
 				httpRequestsTask = null;
 			}
 		};
 		httpRequestsTask.execute(null, null, null);
 	}
 
 	public void destroy() {
 		if (httpRequestsTask != null) {
 			httpRequestsTask.cancel(true);
 		}
 
 		httpRequestsTask = new AsyncTask<Void, Void, Void>() {
 			@Override
 			protected Void doInBackground(Void... params) {
 				Log.d(TAG, "Connection closed.");
 				ServerProxy.unregister(_userId);
 				_isStarted = false;
 				return null;
 			}
 
 			@Override
 			protected void onPostExecute(Void result) {
 				httpRequestsTask = null;
 			}
 		};
 		httpRequestsTask.execute(null, null, null);
 	}
 
 	public void attached() {
 		if (!_isStarted) {
 			return;
 		}
 		httpRequestsTask = new AsyncTask<Void, Void, Void>() {
 			@Override
 			protected Void doInBackground(Void... params) {
 				try {
 					Message message = new Message();
 					message.obj = new Socket(SERVER_NAME, FIRST_PORT + _userId);
 					Main.handler.sendMessage(message);
 				} catch (UnknownHostException e) {
 					e.printStackTrace();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 				return null;
 			}
 
 			@Override
 			protected void onPostExecute(Void result) {
 				httpRequestsTask = null;
 			}
 		};
 
 	}
 }
