 package hr.brbulic.android_tools;
 
 import hr.brbulic.concurrency.BackgroundWorker;
 import hr.brbulic.concurrency.interfaces.IBackgroundDelegate;
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class HelloAndroidActivity extends Activity implements
 		IBackgroundDelegate {
 
 	private TextView _myTextview;
 	private static String TAG = "android-tools";
 
 	/**
 	 * Called when the activity is first created.
 	 * 
 	 * @param savedInstanceState
 	 *            If the activity is being re-initialized after previously being
 	 *            shut down then this Bundle contains the data it most recently
 	 *            supplied in onSaveInstanceState(Bundle). <b>Note: Otherwise it
 	 *            is null.</b>
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		Log.i(TAG, "onCreate");
 		setContentView(R.layout.main);
 
 		_myTextview = (TextView) this.findViewById(R.id.mainTextview);
 
 		BackgroundWorker.getInstance().EnqueueSimple(this, "THIS IS MY STRING");
 		BackgroundWorker.getInstance().EnqueueRunnable(new MyHandler());
 		Toast.makeText(getApplicationContext(), TAG, 2);
 	}
 
 	private class MyHandler implements Runnable {
 
 		@Override
 		public void run() {
 			int count = 5;
 
 			StringBuilder mainString = new StringBuilder();
 
 			while (count < 10) {
				String string = String.format("Ovo je prekrilo sve... %1$d",
 						count);
 
 				mainString.append(string);
 
 				Log.i(TAG, string);
 				try {
 					Thread.sleep(250);
 				} catch (InterruptedException e) {
 				}
 
 				count++;
 			}
 
 			final String resultString = mainString.toString();
 
 			_myTextview.post(new Runnable() {
 
 				@Override
 				public void run() {
 					_myTextview.setText(resultString);
 
 				}
 			});
 		}
 
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 
 		BackgroundWorker.getInstance().stop();
 	}
 
 	@SuppressWarnings("hiding")
 	@Override
 	public <String> void backgroundRequest(String internalState) {
 		int count = 0;
 
 		System.out.println(internalState);
 
 		while (count < 10) {
 			System.out.println("Just a message with number " + count);
 			count++;
 		}
 	}
 
 }
