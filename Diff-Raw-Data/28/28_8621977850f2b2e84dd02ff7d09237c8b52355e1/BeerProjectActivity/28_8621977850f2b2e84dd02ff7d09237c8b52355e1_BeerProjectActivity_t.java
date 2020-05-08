 package eu.silvere;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 
 import android.app.Activity;
 import android.content.res.Resources;
 import android.content.res.XmlResourceParser;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.WindowManager;
 import android.widget.Button;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 public class BeerProjectActivity extends Activity {
 
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
 
 		// TODO finish and move resources loading elsewhere
 		Resources resources = getResources();
 		XmlResourceParser parser = resources.getXml(R.xml.beer_bottles);
 
 		List<BeerBottle> bottles = null;
 		try {
 			int eventType = parser.getEventType();
 			BeerBottle currentBottle = null;
 			boolean done = false;
 			while (eventType != XmlPullParser.END_DOCUMENT && !done) {
 				String name = null;
 				switch (eventType) {
 				case XmlPullParser.START_DOCUMENT:
 					bottles = new ArrayList<BeerBottle>();
 					break;
 				case XmlPullParser.START_TAG:
 					name = parser.getName();
 					if (name.equalsIgnoreCase("bottle")) {
						currentBottle = new BeerBottle();
 					} else if (currentBottle != null) {
						if (name.equalsIgnoreCase("name")) {
							currentBottle.setName(parser.nextText());
						} else if (name.equalsIgnoreCase("image")) {
 							// resources.getDrawable(R.drawable.) TODO
 							currentBottle.setImage(null);
 						} else if (name.equalsIgnoreCase("description")) {
 							currentBottle.setDescription(parser.nextText());
 						} else if (name.equalsIgnoreCase("frequency")) {
 							float frequency = parser.getAttributeFloatValue(null, "frequency", 0);
 							float volume = parser.getAttributeFloatValue(null, "volume", 0);
 							currentBottle.addFrequency(volume, frequency);
 						}
 					}
 					break;
 				case XmlPullParser.END_TAG:
 					name = parser.getName();
 					if (name.equalsIgnoreCase("bottle") && currentBottle != null) {
 						bottles.add(currentBottle);
 					} else if (name.equalsIgnoreCase("beer_bottle_list")) {
 						done = true;
 					}
 					break;
 				}
 				eventType = parser.next();
 			}

 		} catch (XmlPullParserException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		mBeerSoundAnalyzer.setBeerBottle(bottles.get(0));
 		mBeerSoundAnalyzer.getBeerBottle().computeCoef();
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
