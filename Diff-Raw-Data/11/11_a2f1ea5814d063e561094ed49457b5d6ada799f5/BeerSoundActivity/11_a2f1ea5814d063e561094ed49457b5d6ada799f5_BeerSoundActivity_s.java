 package eu.silvere;
 
 import android.app.Activity;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.WindowManager;
 import android.widget.Button;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 public class BeerSoundActivity extends Activity {
 
 	private TextView tv;
 	private ProgressBar analyzeProgresBar;
 	private Button mLaunchButton;
 
 	private BeerSoundAnalyzer mBeerSoundAnalyzer;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// No Title bar
 		// requestWindowFeature(Window.FEATURE_NO_TITLE);
 
 		// full screen
 		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
 
 		setContentView(R.layout.main);
 
 		Log.d("Beer", "Beer" + "Activity started");
 
 		mBeerSoundAnalyzer = new BeerSoundAnalyzer();
 
 		tv = (TextView) findViewById(R.id.text);
 		analyzeProgresBar = (ProgressBar) findViewById(R.id.analyzingProgressBar);
 		analyzeProgresBar.setMax(3);
 
 		mLaunchButton = (Button) findViewById(R.id.button);
 
 		mLaunchButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				new AnalyzerAsyncTask().execute();
 			}
 		});
 
 		Bundle bundle = getIntent().getBundleExtra("b");
		mBeerSoundAnalyzer.setBeerBottle((BeerBottle) bundle.get("bottle"));
 	}
 
 	/**
 	 * @author Vincent Dupont <vincent.touffi@gmail.com>
 	 * 
 	 */
 	public class AnalyzerAsyncTask extends AsyncTask<float[], Integer, Float> {
 
 		float mFreq;
 		float mVol;
 		BeerNote mNote;
 
 		@Override
 		protected void onPreExecute() {
 			analyzeProgresBar.setProgress(0);
 			analyzeProgresBar.setVisibility(View.VISIBLE);
 			mLaunchButton.setEnabled(false);
 		}
 
 		@Override
 		protected Float doInBackground(float[]... params) {
 			mBeerSoundAnalyzer.launchRecording();
 			publishProgress(1);
 			float f = mBeerSoundAnalyzer.computeFFT();
 			publishProgress(2);
 			mFreq = f;
 			mVol = mBeerSoundAnalyzer.computeVolume(f);
 			mNote = mBeerSoundAnalyzer.computeNote(f);
 			return f;
 		}
 
 		@Override
 		protected void onProgressUpdate(Integer... values) {
 			analyzeProgresBar.setProgress(values[0]);
 		}
 
 		@Override
 		protected void onPostExecute(Float result) {
 			analyzeProgresBar.setVisibility(View.INVISIBLE);
 			tv.setText("Hello, Beeeeeeer " + result + " Hz " + mVol + " mL " + mNote);
 			mLaunchButton.setEnabled(true);
 		}
 
 		@Override
 		protected void onCancelled() {
 			// TODO Auto-generated method stub
 			super.onCancelled();
 		}
 
 	}
 
 }
