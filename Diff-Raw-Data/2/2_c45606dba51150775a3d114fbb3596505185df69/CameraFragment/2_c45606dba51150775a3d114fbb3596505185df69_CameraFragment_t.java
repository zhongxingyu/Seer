 package com.nvidia.fcamerapro;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.app.Fragment;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.SeekBar;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.Spinner;
 import android.widget.TextView;
 
 /**
  * CameraFragment corresponds to the UI tab that is visible in capture mode.
  * It contains a viewfinder, a histogram, a number of buttons and sliders for
  * setting the camera parameters.
  * 
  * See res/layout/camera.xml for its layout.
  */
 public final class CameraFragment extends Fragment implements OnClickListener, OnSeekBarChangeListener, HistogramDataProvider, OnTouchListener, OnItemSelectedListener {
 
 	// UI elements present on screen.
 	private CheckBox mAutoWBCheckBox, mAutoFocusCheckBox, mAutoExposureCheckBox, mAutoGainCheckBox;
 	private SeekBar mWBSeekBar, mFocusSeekBar, mExposureSeekBar, mGainSeekBar;
 	private TextView mWbTextView, mFocusTextView, mExposureTextView, mGainTextView;
 	private Spinner mOutputFormatSpinner, mFlashModeSpinner, mTouchActionSpinner;
 	/* [CS478] Assignment #2
 	 * Create an additional spinner reference to keep track of the viewfinder mode. 
 	 */
 	// TODO TODO TODO
 	// TODO TODO TODO
 	// TODO TODO TODO
 	
 	private Button mCaptureButton;
 	private HistogramView mHistogramView;
 	private CameraView mCameraView;
 	
 	// Local variables for parameter ranges.
 	private double mMinWb, mMaxWb, mMinFocus, mMaxFocus, mMinExposure, mMaxExposure, mMinGain, mMaxGain;
 
 	// The view that corresponds to this fragment. It will feature res/layout/camera.xml.
 	private View mContentView;
 
 	// UI update timer handler.
 	private Handler mHandler = new Handler();
 	private Runnable mUpdateUITask = new Runnable() {
 		public void run() {
 			updateSeekBarValues(false); // Update sliders.
 			mHandler.postDelayed(this, 1000 / Settings.UI_MAX_FPS);
 		}
 	};
 
 	// Private initialization method.
 	private void initContentView() {
 		if (mContentView != null) return; // Return if already initialized.
 		Activity activity = getActivity();
 		mContentView = activity.getLayoutInflater().inflate(R.layout.camera, null);
 
 		ArrayAdapter<CharSequence> adapter;
 
 		// Unpack the spinner for the Image output. Disabled currently to only allow JPEG.
 		mOutputFormatSpinner = (Spinner) mContentView.findViewById(R.id.spinner_output_format);
 		adapter = ArrayAdapter.createFromResource(activity, R.array.output_format_array, android.R.layout.simple_spinner_item);
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		mOutputFormatSpinner.setAdapter(adapter);
 		mOutputFormatSpinner.setEnabled(false);
 
 		// Unpack the spinner for the flash mode.
 		mFlashModeSpinner = (Spinner) mContentView.findViewById(R.id.spinner_flash_mode);
 		adapter = ArrayAdapter.createFromResource(activity, R.array.flash_mode_array, android.R.layout.simple_spinner_item);
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		mFlashModeSpinner.setAdapter(adapter);
 
 		// Unpack the spinner for the touch mode.
 		mTouchActionSpinner = (Spinner) mContentView.findViewById(R.id.spinner_touch_action);
 		adapter = ArrayAdapter.createFromResource(activity, R.array.touch_action_array, android.R.layout.simple_spinner_item);
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		mTouchActionSpinner.setAdapter(adapter);
 		
 		/* [CS478] Assignment #2
 		 * This piece of code should unpack the viewfinder mode spinner.
 		 * Don't forget to set its onItemSelectedListener appropriately.
 		 */		
 		// TODO TODO TODO
 		// TODO TODO TODO	
 		// TODO TODO TODO
 		
 		// Set the checkboxes for 3A algorithms.
 		mAutoExposureCheckBox = (CheckBox) mContentView.findViewById(R.id.cb_auto_exposure);
 		mAutoExposureCheckBox.setOnClickListener(this);
 		mAutoFocusCheckBox = (CheckBox) mContentView.findViewById(R.id.cb_auto_focus);
 		mAutoFocusCheckBox.setOnClickListener(this);
 		mAutoGainCheckBox = (CheckBox) mContentView.findViewById(R.id.cb_auto_gain);
 		mAutoGainCheckBox.setOnClickListener(this);
 		mAutoWBCheckBox = (CheckBox) mContentView.findViewById(R.id.cb_auto_wb);
 		mAutoWBCheckBox.setOnClickListener(this);
 
 		// Fetch the text fields for displaying the parameter values
 		mExposureTextView = (TextView) mContentView.findViewById(R.id.tv_exposure);
 		mFocusTextView = (TextView) mContentView.findViewById(R.id.tv_focus);
 		mGainTextView = (TextView) mContentView.findViewById(R.id.tv_gain);
 		mWbTextView = (TextView) mContentView.findViewById(R.id.tv_wb);
 
 		// Set the default parameter ranges.
 		mMinExposure = 1000;
 		mMaxExposure = 1000000;
 		mMinGain = 1.0;
 		mMaxGain = 32.0;
 		mMinWb = 3200;
 		mMaxWb = 9500;
 		mMinFocus = 10.0;
 		mMaxFocus = 0.0;
 
 		// Set up the SeekBars for the parameters.
 		mExposureSeekBar = (SeekBar) mContentView.findViewById(R.id.sb_exposure);
 		mExposureSeekBar.setMax(Settings.SEEK_BAR_PRECISION);
 		mExposureSeekBar.setOnSeekBarChangeListener(this);
 		mFocusSeekBar = (SeekBar) mContentView.findViewById(R.id.sb_focus);
 		mFocusSeekBar.setMax(Settings.SEEK_BAR_PRECISION);
 		mFocusSeekBar.setOnSeekBarChangeListener(this);
 		mGainSeekBar = (SeekBar) mContentView.findViewById(R.id.sb_gain);
 		mGainSeekBar.setMax(Settings.SEEK_BAR_PRECISION);
 		mGainSeekBar.setOnSeekBarChangeListener(this);
 		mWBSeekBar = (SeekBar) mContentView.findViewById(R.id.sb_wb);
 		mWBSeekBar.setMax(Settings.SEEK_BAR_PRECISION);
 		mWBSeekBar.setOnSeekBarChangeListener(this);
 
 		// Set up the capture button.
 		mCaptureButton = (Button) mContentView.findViewById(R.id.button_capture);
 		mCaptureButton.setOnClickListener(this);
 
 		// Set up the histogram.
 		mHistogramView = (HistogramView) mContentView.findViewById(R.id.histogram_view);
 		mHistogramView.setDataProvider(this);
 
 		// Set up the viewfinder screen.
 		mCameraView = (CameraView) mContentView.findViewById(R.id.camera_view);
 		mCameraView.setOnTouchListener(this);
 		
 		// Apply the current parameter values to the appropriate SeekBars.
 		updateSeekBarValues(true);
 
 		// Run the auto-parameter-setting algorithms to kick off viewfinding.
 		mAutoExposureCheckBox.performClick();
 		mAutoGainCheckBox.performClick();
 		mAutoWBCheckBox.performClick();
 	}
 
 	/* ====================================================================
 	 * Methods inherited from Fragment, overridden here.
 	 * ==================================================================== */
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		initContentView();
 	}
 
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		mHandler.postDelayed(mUpdateUITask, 0); // Start UI update timer.
 		return mContentView;
 	}
 
 	public void onDestroyView() {
 		mHandler.removeCallbacks(mUpdateUITask); // Stop UI update timer.
 		super.onDestroyView();
 	}
 
 	public void onDestroy() {
 		super.onDestroy();
 	}
 
 	/* ====================================================================
 	 * Implementation of OnItemSelectedListener
 	 * ==================================================================== */
 
     public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
     	
     	/* [CS478] Assignment #2
 		 * This piece of code is executed when the user selects a new item from
 		 * a spinner style drop down menu (assuming it's hooked up properly). You
 		 * should add some code here to update the CameraView to use the correct
 		 * viewfinder mode.
     	 */
     	// TODO TODO TODO 
     	// TODO TODO TODO 
     	// TODO TODO TODO 
     	// TODO TODO TODO 
     }
 
     public void onNothingSelected(AdapterView<?> parent) {
     	// Do nothing.
     }
 
 
 	/* ====================================================================
 	 * Implementation of OnClickListener interface.
 	 * ==================================================================== */
 	public void onClick(View v) {
 		FCamInterface iface = FCamInterface.GetInstance();
 		if (v == mCaptureButton) {
 			if (!iface.isCapturing()) {
 				ArrayList<FCamShot> shots = new ArrayList<FCamShot>(16);
 				
 				double exposure = iface.getPreviewParam(FCamInterface.PREVIEW_EXPOSURE);
 				double gain = iface.getPreviewParam(FCamInterface.PREVIEW_GAIN);
 				double wb = iface.getPreviewParam(FCamInterface.PREVIEW_WB);
 				double focus = iface.getPreviewParam(FCamInterface.PREVIEW_FOCUS);
 				
 				FCamShot shot = new FCamShot();
 				shot.exposure = exposure;
 				shot.gain = gain;
 				shot.wb = wb;
 				shot.focus = focus;
 				
 				switch (mFlashModeSpinner.getSelectedItemPosition()) {
 				case 0: // flash off
 					shot.flashOn = false;
 					shots.add(shot);
 					break;
 				case 1: // flash on
 					shot.flashOn = true;
 					shots.add(shot);
 					break;
 				case 2: // flash off/on
 					shot.flashOn = false;
 					shots.add(shot);
 					shot = shot.clone();
 					shot.flashOn = true;
 					// Reduce the gain by half to take advantage of more light. 
 					// This is a hack because we aren't really metering for the effect
 					// of the flash. 
 					shot.gain = gain*0.5; 
 					shots.add(shot);					
 					break;
 				}
 				iface.capture(shots);
 			}
 		} else if (v == mAutoExposureCheckBox) {
 			boolean autoEvaluate = mAutoExposureCheckBox.isChecked();
 			iface.enablePreviewParamEvaluator(FCamInterface.PREVIEW_EXPOSURE, autoEvaluate);
 			mExposureSeekBar.setEnabled(!autoEvaluate);
 		} else if (v == mAutoFocusCheckBox) {
 			boolean autoEvaluate = mAutoFocusCheckBox.isChecked();
 			iface.enablePreviewParamEvaluator(FCamInterface.PREVIEW_FOCUS, autoEvaluate);
 			mFocusSeekBar.setEnabled(!autoEvaluate);
 		} else if (v == mAutoGainCheckBox) {
 			boolean autoEvaluate = mAutoGainCheckBox.isChecked();
 			iface.enablePreviewParamEvaluator(FCamInterface.PREVIEW_GAIN, autoEvaluate);
 			mGainSeekBar.setEnabled(!autoEvaluate);
 		} else if (v == mAutoWBCheckBox) {
 			boolean autoEvaluate = mAutoWBCheckBox.isChecked();
 			iface.enablePreviewParamEvaluator(FCamInterface.PREVIEW_WB, autoEvaluate);
 			mWBSeekBar.setEnabled(!autoEvaluate);
 		}
 	}
 
 	// Fetch the current capture parameters from FCam, and apply them to
 	// our SeekBars.
 	private void updateSeekBarValues(boolean forceUpdate) {
 		FCamInterface iface = FCamInterface.GetInstance();
 		if (forceUpdate || !mExposureSeekBar.isEnabled()) {
 			int param = iface.getPreviewParam(FCamInterface.PREVIEW_EXPOSURE);
 			mExposureSeekBar.setProgress(getExposureForUI(param));
 			mExposureTextView.setText(Utils.FormatExposure(param));
 		}
 		if (forceUpdate || !mGainSeekBar.isEnabled()) {
 			int param = iface.getPreviewParam(FCamInterface.PREVIEW_GAIN);
 			mGainSeekBar.setProgress(getGainForUI(param));
 			mGainTextView.setText(Utils.FormatGain(param));
 		}
 		if (forceUpdate || !mWBSeekBar.isEnabled()) {
 			int param = iface.getPreviewParam(FCamInterface.PREVIEW_WB);
 			mWBSeekBar.setProgress(getWBForUI(param));
 			mWbTextView.setText(Utils.FormatWhiteBalance(param));
 		}
 		if (forceUpdate || !mFocusSeekBar.isEnabled()) {
 			int param = iface.getPreviewParam(FCamInterface.PREVIEW_FOCUS);
 			mFocusSeekBar.setProgress(getFocusForUI(param));
 			mFocusTextView.setText(Utils.FormatFocus(param));
 		}
 	}
 
 	/* ====================================================================
 	 * Utility methods for translating between SeekBar values and
 	 * parameter values.
 	 * ==================================================================== */
 	private double getExposureFromUI(int progress) {
 		double nvalue = (double) progress / Settings.SEEK_BAR_PRECISION;
 		return Math.pow(nvalue, Settings.SEEK_BAR_EXPOSURE_GAMMA) * (mMaxExposure - mMinExposure) + mMinExposure;
 	}
 	private int getExposureForUI(double value) {
 		return (int) (Math.pow((value - mMinExposure) / (mMaxExposure - mMinExposure), 1.0 / Settings.SEEK_BAR_EXPOSURE_GAMMA) * Settings.SEEK_BAR_PRECISION);
 	}
 	private double getGainFromUI(int progress) {
 		double nvalue = (double) progress / Settings.SEEK_BAR_PRECISION;
 		return Math.pow(nvalue, Settings.SEEK_BAR_GAIN_GAMMA) * (mMaxGain - mMinGain) + mMinGain;
 	}
 	private int getGainForUI(double value) {
 		return (int) (Math.pow((value - mMinGain) / (mMaxGain - mMinGain), 1.0 / Settings.SEEK_BAR_GAIN_GAMMA) * Settings.SEEK_BAR_PRECISION);
 	}
 	private double getFocusFromUI(int progress) {
 		double nvalue = (double) progress / Settings.SEEK_BAR_PRECISION;
 		return Math.pow(nvalue, Settings.SEEK_BAR_FOCUS_GAMMA) * (mMaxFocus - mMinFocus) + mMinFocus;
 	}
 	private int getFocusForUI(double value) {
 		return (int) (Math.pow((value - mMinFocus) / (mMaxFocus - mMinFocus), 1.0 / Settings.SEEK_BAR_FOCUS_GAMMA) * Settings.SEEK_BAR_PRECISION);
 	}
 	private double getWBFromUI(int progress) {
 		double nvalue = (double) progress / Settings.SEEK_BAR_PRECISION;
 		return Math.pow(nvalue, Settings.SEEK_BAR_WB_GAMMA) * (mMaxWb - mMinWb) + mMinWb;
 	}
 	private int getWBForUI(double value) {
 		return (int) (Math.pow((value - mMinWb) / (mMaxWb - mMinWb), 1.0 / Settings.SEEK_BAR_WB_GAMMA) * Settings.SEEK_BAR_PRECISION);
 	}
 
 	/* ====================================================================
 	 * Implementation of OnSeekBarChangeListener
 	 * ==================================================================== */
 	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
 		FCamInterface iface = FCamInterface.GetInstance();
 		if (seekBar == mExposureSeekBar) {
 			double exposure = getExposureFromUI(progress);
 			mExposureTextView.setText(Utils.FormatExposure(exposure));
 			if (seekBar.isEnabled()) {
 				iface.setPreviewParam(FCamInterface.PREVIEW_EXPOSURE, exposure);
 			}
 		} else if (seekBar == mFocusSeekBar) {
 			double focus = getFocusFromUI(progress);
 			mFocusTextView.setText(Utils.FormatFocus(focus));
 			if (seekBar.isEnabled()) {
 				iface.setPreviewParam(FCamInterface.PREVIEW_FOCUS, focus);
 			}
 		} else if (seekBar == mGainSeekBar) {
 			double gain = getGainFromUI(progress);
 			mGainTextView.setText(Utils.FormatGain(gain));
 			if (seekBar.isEnabled()) {
 				iface.setPreviewParam(FCamInterface.PREVIEW_GAIN, gain);
 			}
 		} else if (seekBar == mWBSeekBar) {
 			double wb = getWBFromUI(progress);
 			mWbTextView.setText(Utils.FormatWhiteBalance(wb));
 			if (seekBar.isEnabled()) {
 				iface.setPreviewParam(FCamInterface.PREVIEW_WB, wb);
 			}
 		}
 	}
 	public void onStartTrackingTouch(SeekBar seekBar) {}
 	public void onStopTrackingTouch(SeekBar seekBar) {}
 	
 	/* ====================================================================
 	 * Implementation of HistogramDataProvider interface.
 	 * ==================================================================== */
 	public void getHistogramData(float[] data) {
 		FCamInterface.GetInstance().getHistogramData(data);
 	}
 
 	/* ====================================================================
 	 * Implementation of OnTouchListener interface.
 	 * ==================================================================== */
 	public boolean onTouch(View v, MotionEvent event) {
 		if (v == mCameraView && event.getActionMasked() == MotionEvent.ACTION_DOWN
 				&& mAutoFocusCheckBox.isChecked()) {
 			/* [CS478] Assignment #1
 			 * This piece of code is executed when the user touches the
 			 *  viewfinder. It should initiate autoFocus. To do this, you
 			 *  should call the appropriate method of FCamInterface, in order
 			 *  to get the ball rolling.
 			 *  
 			 *  Check out the native methods in FCamInterface.java. There are
 			 *  ones called enqueueMessageForAutofocus(...), etc.
 			 *  
 			 *  Lastly, look up the Android documentation on MotionEvent
 			 *  to figure out how to retrieve the touch coordinates.
 			 */
 			switch (mTouchActionSpinner.getSelectedItemPosition()) {
 			case 0: // global
 				FCamInterface.GetInstance().enqueueMessageForAutofocus();
 				break;
 			case 1: // fast global
 				FCamInterface.GetInstance().enqueueMessageForFastAutofocus();
 				break;
 			case 2: // local
				FCamInterface.GetInstance().enqueueMessageForAutofocusSpot(event.getX()/v.getWidth(), event.getY()/v.getHeight());
 				break;
 			}
 			
 			/* [CS478] Assignment #2
 			 * You'll need to further modify the code you used here in Assignment 1
 			 * to handle face focus touch events.
 			 */
 			// TODO TODO TODO
 			// TODO TODO TODO
 			// TODO TODO TODO
 			// TODO TODO TODO
 			// TODO TODO TODO
 			return true;
 		}
 		return false;
 	}
 }
