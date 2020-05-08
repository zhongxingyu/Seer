 package pl.edu.zut.mad.tools.noise.meter;
 
 import java.text.DecimalFormat;
 
 import org.achartengine.GraphicalView;
 
 import pl.edu.zut.mad.tools.R;
 import android.app.Activity;
 import android.media.MediaRecorder;
 import android.os.Bundle;
 import android.util.Log;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class NoiseMeterActivity extends Activity implements
 		MicrophoneInputListener {
 
 	private static final String TAG = "NoiseMeterActivity";
 
 	private MicrophoneInput micInput; // The micInput object provides real time
 										// audio.
 	private TextView noiseLevel;
 	private BarLevelDrawable mBarLevel;
 
 	private int count = 0;
 	private static GraphicalView view;
 	private NoiseLinearGraph lineGraph;
 
 	private double mOffsetdB = 10; // Offset for bar, i.e. 0 lit LEDs at 10 dB.
 	// The Google ASR input requirements state that audio input sensitivity
 	// should be set such that 90 dB SPL at 1000 Hz yields RMS of 2500 for
 	// 16-bit samples, i.e. 20 * log_10(2500 / mGain) = 90.
 	private double mGain = 2500.0 / Math.pow(10.0, 90.0 / 20.0);
 	private double mRmsSmoothed; // Temporally filtered version of RMS.
 	private double mAlpha = 0.9; // Coefficient of IIR smoothing filter for RMS.
 
 	// Variables to monitor UI update and check for slow updates.
 	private volatile boolean mDrawing;
 	private volatile int mDrawingCollided;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_noise_meter);
 
 		// Here the micInput object is created for audio capture.
 		// It is set up to call this object to handle real time audio frames of
 		// PCM samples. The incoming frames will be handled by the
 		// processAudioFrame method below.
 		micInput = new MicrophoneInput(this);
 
 		lineGraph = new NoiseLinearGraph(this);
 		noiseLevel = (TextView) findViewById(R.id.noiseLevel);
 		mBarLevel = (BarLevelDrawable) findViewById(R.id.bar_level_drawable_view);
 
 	}
 
 	@Override
 	protected void onStart() {
 		super.onStart();
 		view = lineGraph.getView(this);
 		LinearLayout layout = (LinearLayout) findViewById(R.id.graphLayout);
 		layout.addView(view);
 
 	}
 
 	@Override
 	protected void onPause() {
 		micInput.stop();
 		super.onPause();
 	}
 
 	@Override
 	protected void onResume() {
 
 		micInput.setSampleRate(8000);
 		micInput.setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION);
 		micInput.start();
 		super.onResume();
 	}
 
 	/**
 	 * This method gets called by the micInput object owned by this activity. It
 	 * first computes the RMS value and then it sets up a bit of code/closure
 	 * that runs on the UI thread that does the actual drawing.
 	 */
 	@Override
 	public void processAudioFrame(short[] audioFrame) {
 		if (!mDrawing) {
 			mDrawing = true;
 			// Compute the RMS value. (Note that this does not remove DC).
 			double rms = 0;
 			for (int i = 0; i < audioFrame.length; i++) {
 				rms += audioFrame[i] * audioFrame[i];
 			}
 			rms = Math.sqrt(rms / audioFrame.length);
 
 			// Compute a smoothed version for less flickering of the display.
 			mRmsSmoothed = mRmsSmoothed * mAlpha + (1 - mAlpha) * rms;
 			final double rmsdB = 20.0 * Math.log10(mGain * mRmsSmoothed);
 
 			// Set up a method that runs on the UI thread to update of the LED
 			// bar
 			// and numerical display.
 			mBarLevel.post(new Runnable() {
 				@Override
 				public void run() {
 					// The bar has an input range of [0.0 ; 1.0] and 10
 					// segments.
					// Each LED corresponds to 8 dB.
 					mBarLevel.setLevel((mOffsetdB + rmsdB) / 80);
 
 					DecimalFormat df = new DecimalFormat("##");
 					noiseLevel.setText(df.format(20 + rmsdB) + " dB");
 
 					count++;
 					GraphPoint p = new GraphPoint(count, 20 + rmsdB);
 					lineGraph.addNewPoints(p);
 					view.repaint();
 
 					mDrawing = false;
 				}
 			});
 		} else {
 			mDrawingCollided++;
 			Log.v(TAG,
 					"Level bar update collision, i.e. update took longer "
 							+ "than 20ms. Collision count"
 							+ Double.toString(mDrawingCollided));
 		}
 	}
 }
