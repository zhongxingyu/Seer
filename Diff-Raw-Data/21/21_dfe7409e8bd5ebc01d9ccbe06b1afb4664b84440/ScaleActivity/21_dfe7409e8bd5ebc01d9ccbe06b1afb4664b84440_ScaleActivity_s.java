 /*******************************************************************************
  * Copyright (c) 2012 rob@theultimatelabs.com.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  * 
  * Contributors:
  *     rob@theultimatelabs.com - initial API and implementation
  ******************************************************************************/
 package com.theultimatelabs.scale;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Locale;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.hardware.usb.UsbConstants;
 import android.hardware.usb.UsbDevice;
 import android.hardware.usb.UsbDeviceConnection;
 import android.hardware.usb.UsbEndpoint;
 import android.hardware.usb.UsbInterface;
 import android.hardware.usb.UsbManager;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.speech.RecognizerIntent;
 import android.speech.tts.TextToSpeech;
 import android.speech.tts.TextToSpeech.OnInitListener;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnLongClickListener;
 import android.view.WindowManager;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.google.ads.AdRequest;
 import com.google.ads.AdSize;
 import com.google.ads.AdView;
 
 public class ScaleActivity extends Activity implements OnInitListener {
 
 	public final static String TAG = "ScaleActivity";
 	public final static String PREFS = "PREFS";
 	private SharedPreferences mSettings;
 	private TextToSpeech mTts;
 	private JSONObject mDensitiesJson;
 	private JSONObject mVolumesJson;
 	private JSONObject mWeightsJson;
 	private final double OUNCES_IN_GRAMS = 28.3495;
 	private double mWeightGrams;
 	private double mZeroGrams = 0;
 	private double mUnitsRatio = 1.0;
 	private String mUnitsText = "grams";
 	private TextView mUnitsView;
 	private AdView adView = null;
 	private LinearLayout adLayout;
	private boolean showAds = true;
 	private TextView mWeightTextView;
 	private TextView disableAdsText;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.scale);
 
 		Log.v(TAG, "onCreate");
 
 		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
 		// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
 
 		mSettings = getSharedPreferences(PREFS, 0);
 		
 		mUnitsText = mSettings.getString("unitsText", "grams");
 		mUnitsRatio = mSettings.getFloat("unitsRatio", (float) 1.0);
 
 		mTts = new TextToSpeech(this, this);
 	
 
 		mUnitsView = (TextView) findViewById(R.id.text_unit);
 		mUnitsView.setText(mUnitsText);
 
 		findViewById(R.id.text_unit).setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
 				while (mTts.isSpeaking())
 					;
 				Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
 				intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
 				intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say Units");
 				startActivityForResult(intent, 0);
 
 				if (adView != null) {
 					adView.loadAd(new AdRequest());
 				}
 
 			}
 		});
 
 		mWeightTextView = (TextView) findViewById(R.id.text_weight);
 		mWeightTextView.setText("00.00");
 		/*
 		 * TextPaint weightTextPaint = mWeightTextView.getPaint(); CharSequence
 		 * weightText = mWeightTextView.getText(); while (weightText !=
 		 * TextUtils.ellipsize(weightText, weightTextPaint,
 		 * getWindowManager().getDefaultDisplay
 		 * ().getWidth()*2/3,TextUtils.TruncateAt.END)) {
 		 * weightTextPaint.setTextSize(weightTextPaint.getTextSize() - 1); }
 		 */
 
 		mWeightTextView.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				Toast.makeText(getApplicationContext(), "Zero'd", Toast.LENGTH_LONG).show();
 				mZeroGrams = mWeightGrams;
 				if (adView != null) {
 					adView.loadAd(new AdRequest());
 				}
 			}
 		});
 		mWeightTextView.setOnLongClickListener(new OnLongClickListener() {
 			public boolean onLongClick(View v) {
 				mZeroGrams = 0;
 				Toast.makeText(getApplicationContext(), "Reset", Toast.LENGTH_LONG).show();
 				if (adView != null) {
 					adView.loadAd(new AdRequest());
 				}
 				return true;
 			}
 		});
 
 		disableAdsText = (TextView) findViewById((R.id.text_disableAds));
 		disableAdsText.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				new AlertDialog.Builder(ScaleActivity.this)
 						.setTitle("Keep Software Free and Open Source")
 						.setMessage(
 								"Ads help support further development, but they are OPTIONAL."
 										+ " If you choose to disable ads, please consider donating. All dontations"
 										+ " go towards purchasing hardware for open source development. "
 										+ "Disabling ads or donating will not change the features availble in this app."
 										+ " Thank you. rob@theultimatelabs.com")
 						.setPositiveButton("Disable Ads", new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int which) {
 								adLayout.removeAllViews();
 								adView.removeAllViews();
 								disableAdsText.setVisibility(View.INVISIBLE);
 								mSettings.edit().putBoolean("ads", false).commit();
 								adView = null;
 							}
 						}).setCancelable(true)
 						.setNegativeButton("Keep Ads", new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int which) {
 
 							}
 						}).setNeutralButton("Disable Ads + Donate", new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int which) {
 								adLayout.removeAllViews();
 								adView.removeAllViews();
 								disableAdsText.setVisibility(View.INVISIBLE);
 								mSettings.edit().putBoolean("ads", false).commit();
 								adView = null;
 							}
 						}).show();
 			}
 		});
 		
 		TextView aboutText = (TextView) findViewById((R.id.text_about));
 		aboutText.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				startActivity(new Intent(getApplicationContext(), AboutActivity.class));
 			}
 		});
 
 		/*
 		 * .setMessage() new AlertDialog.Builder(this) .setMessage(mymessage)
 		 * .setTitle(title) .setCancelable(true)
 		 * .setNeutralButton(android.R.string.cancel, new
 		 * DialogInterface.OnClickListener() { public void
 		 * onClick(DialogInterface dialog, int whichButton){} }) .show(); }}
 		 */
 		// /
 
 		mDensitiesJson = loadJsonResource(R.raw.densities);
 		mVolumesJson = loadJsonResource(R.raw.volumes);
 		mWeightsJson = loadJsonResource(R.raw.weights);
 
 
 		// Initiate a generic request to load it with an ad
 		if (mSettings.getBoolean("ads", true)) {
 			// Create the adViewj
 			adView = new AdView(this, AdSize.SMART_BANNER, "a15089dfb39c5a8");
 
 			// Log.w(TAG, new Integer(R.id.layout_ads).toString());
 			adLayout = (LinearLayout) findViewById(R.id.layout_ads);
 
 			// Add the adView to it
 			adLayout.addView(adView, 0);
 			disableAdsText.setVisibility(View.VISIBLE);
 		} else {
 			disableAdsText.setVisibility(View.INVISIBLE);
 			adView = null;
 		}
 
 		Intent intent = getIntent();
 		mDevice = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
 		
 		
 		findScale();
 		
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		if (adView != null) {
 			adView.destroy();
 		}
 		if (mTts != null) {
 			mTts.stop();
 			mTts.shutdown();
 		}
 	}
 
 	public JSONObject loadJsonResource(int id) {
 		InputStream stream = getResources().openRawResource(id);
 		byte[] buffer;
 		try {
 			buffer = new byte[stream.available()];
 			int bytesRead = stream.read(buffer);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return null;
 		}
 
 		JSONObject json;
 		try {
 			json = new JSONObject(new String(buffer));
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return null;
 		}
 		return json;
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 		if (adView != null) {
 			adView.loadAd(new AdRequest());
 		}
 		Log.v(TAG, "onStart");
 
 	}
 
 	@Override
 	protected void onStop() {
 		super.onStop();
 		// Log.v(TAG,"onStop");
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.scale, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		Log.i(TAG, String.format("item selected %s", item.getTitle()));
 		return true;
 	}
 	
 	private UsbDevice mDevice;
 	
 	public void findScale() {
 		
 
 		
 		if (mDevice == null) {
 			UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
 			HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
 			Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
 			while (deviceIterator.hasNext()) {
 				mDevice = deviceIterator.next();
 				Log.v(TAG,
 						String.format("name=%s deviceId=%d productId=%d vendorId=%d deviceClass=%d subClass=%d protocol=%d interfaceCount=%d",
 								mDevice.getDeviceName(), mDevice.getDeviceId(), mDevice.getProductId(), mDevice.getVendorId(), mDevice.getDeviceClass(),
 								mDevice.getDeviceSubclass(), mDevice.getDeviceProtocol(), mDevice.getInterfaceCount()));
 				break;
 			}
 		}
 		
 		
 		if(mDevice != null) {
 			new ScaleListener().execute();
 		}
 		else {
 			new AlertDialog.Builder(ScaleActivity.this)
 			.setTitle("Scale Not Found")
 			.setMessage("Please connect scale with OTG cable and turn the scale on")
 			.setCancelable(false)
 			.setPositiveButton("Try Again", new DialogInterface.OnClickListener() {
 				
 				public void onClick(DialogInterface dialog, int which) {
 					findScale();
 				}
 
 			})
 			.setNegativeButton("Close", new DialogInterface.OnClickListener() {
 				
 				public void onClick(DialogInterface dialog, int which) {
 					finish();
 				}
 
 			})
 			.setNeutralButton("Buy Scale", new DialogInterface.OnClickListener() {
 				
 				public void onClick(DialogInterface dialog, int which) {
 					Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://theultimatelabsstore.blogspot.com/p/store.html"));
 					startActivity(browserIntent);
 					
 				}
 
 			})
 			.show();
 		}
 		
 		
 	}
 
 	
 	private class ScaleListener extends AsyncTask<Void, Double, Void> {
 
 		
 		private byte mLastStatus = 0;
 		private double mLastWeight = 0;
 
 		@Override
 		protected Void doInBackground(Void... arg0) {
 
 			byte[] data = new byte[128];
 			int TIMEOUT = 2000;
 			boolean forceClaim = true;
 
 			Log.v(TAG, "start transfer");
 			
 			UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
 
 			if (mDevice == null) {
 				Log.e(TAG, "no device");
 				return null;
 			}
 			UsbInterface intf = mDevice.getInterface(0);
 
 			Log.v(TAG, String.format("endpoint count = %d", intf.getEndpointCount()));
 			UsbEndpoint endpoint = intf.getEndpoint(0);
 			Log.v(TAG, String.format("endpoint direction = %d out = %d in = %d", endpoint.getDirection(), UsbConstants.USB_DIR_OUT, UsbConstants.USB_DIR_IN));
 			UsbDeviceConnection connection = usbManager.openDevice(mDevice);
 			Log.v(TAG, "got connection:" + connection.toString());
 			connection.claimInterface(intf, forceClaim);
 			while (true) {
 
 				int length = connection.bulkTransfer(endpoint, data, data.length, TIMEOUT);
 
 				if (length != 6) {
 					Log.e(TAG, String.format("invalid length: %d",length));
 					return null;
 				}
 
 				byte report = data[0];
 				byte status = data[1];
 				byte exp = data[3];
 				short weightLSB = (short) (data[4] & 0xff);
 				short weightMSB = (short) (data[5] & 0xff);
 				// float weight = ((float)weightLSB+weightMSB*255)/10;
 
 				//Log.v(TAG, String.format("report=%x status=%x exp=%x lsb=%x msb=%x", report, status, exp, weightLSB, weightMSB));
 
 				if (report != 3) {
 					Log.v(TAG, String.format("scale status error %d", status));
 					return null;
 				}
 
 				double weightOunces = (weightLSB + weightMSB * 255) / 10;
 				//Log.v(TAG, "ounces:" + weightOunces);
 				mWeightGrams = weightOunces * OUNCES_IN_GRAMS;
 				//Log.v(TAG, "grams:" + mWeightGrams);
 				double zWeight = (mWeightGrams - mZeroGrams) * mUnitsRatio;
 
 				// if (mLastStatus != status) {
 				switch (status) {
 				case 1:
 					Log.w(TAG, "Scale reports FAULT!\n");
 					break;
 				case 3:
 					Log.i(TAG, "Weighing...");
 					if (mLastWeight != zWeight) { // Math.abs(mLastWeight-weight)
 													// // >= .5) {
 						publishProgress(zWeight);
 					}
 					break;
 				case 2:
 				case 4:
 					if (mLastWeight != zWeight) {
 						Log.i(TAG, String.format("Final Weight: %f", zWeight));
 						publishProgress(zWeight);
 					}
 					break;
 				case 5:
 					Log.w(TAG, "Scale reports Under Zero");
 					if (mLastWeight != zWeight) {
 						publishProgress(0.0);
 					}
 					break;
 				case 6:
 					Log.w(TAG, "Scale reports Over Weight!");
 					break;
 				case 7:
 					Log.e(TAG, "Scale reports Calibration Needed!");
 					break;
 				case 8:
 					Log.e(TAG, "Scale reports Re-zeroing Needed!\n");
 					break;
 				default:
 					Log.e(TAG, "Unknown status code");
 					break;
 				}
 
 				mLastWeight = zWeight;
 				mLastStatus = status;
 				// }
 
 			}
 
 		}
 
 		@Override
 		protected void onProgressUpdate(Double... weights) {
 			Double weight = weights[0];
 			Log.i(TAG, "update progress");
 
 			String weightText = String.format("%.2f", weight);
 			Log.i(TAG, weightText);
 			mWeightTextView.setText(weightText);
 			mWeightTextView.invalidate();
 
 			if (weight == 0.0) {
 				mTts.speak("zero'd", TextToSpeech.QUEUE_FLUSH, null);
 			} else {
 				mTts.speak(weightText + mUnitsText, TextToSpeech.QUEUE_FLUSH, null);
 			}
 		}
 
 		@Override
 		protected void onPostExecute(Void result) {
 
 			Toast.makeText(getApplicationContext(), "Scale Disconnected", Toast.LENGTH_LONG).show();
 			mDevice = null;
 			finish();
 		}
 
 	}
 
 	double findMatch(ArrayList<String> matches, JSONObject json, StringBuilder outName) {
 
 		// Find type
 		for (String match : matches) {
 			Iterator<String> unitNames = json.keys();
 			Log.v(TAG, "match: " + match);
 			while (unitNames.hasNext()) {
 				String unitName = unitNames.next();
 				String unitNameNotPlural = unitName.substring(0, unitName.length() - 1);
 				Log.v(TAG, "name: " + unitName);
 				if (match.contains(unitName) || match.contains(unitNameNotPlural)) {
 					double unit = json.optDouble(unitName, 0);
 					Log.i(TAG, String.format("MATCH!: %s %f", unitName, unit));
 					outName.append(unitName);
 					return unit;
 				}
 			}
 		}
 		return 0;
 	}
 
 	/*
 	 * protected void onPostResume() { TextPaint unitPaint =
 	 * mUnitsView.getPaint(); CharSequence unitText = mUnitsView.getText();
 	 * Log.e(TAG,String.format("%d %d %d",mUnitsView.getMaxHeight(),mUnitsView.
 	 * getMaxWidth(),getWindowManager().getDefaultDisplay().getWidth()));
 	 * //while (unitText != TextUtils.ellipsize(unitText, unitPaint,
 	 * mUnitsView.getMaxEms(),TextUtils.TruncateAt.END)) { //
 	 * unitPaint.setTextSize(unitPaint.getTextSize() - 1); //+} }
 	 */
 
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		Log.v(TAG, "GOT SPEECH RESULT " + resultCode + " req: " + requestCode);
 
 		if (resultCode == RESULT_OK) {
 			ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
 
 			Log.i(TAG, "Check for density");
 			StringBuilder densityName = new StringBuilder();
 			double density = findMatch(matches, mDensitiesJson, densityName);
 
 			Log.i(TAG, "Check for volume");
 			StringBuilder volumeName = new StringBuilder();
 			double volume = findMatch(matches, mVolumesJson, volumeName);
 
 			Log.i(TAG, "Check for weight");
 			StringBuilder weightName = new StringBuilder();
 			double weight = findMatch(matches, mWeightsJson, weightName);
 
 			if (density != 0 && volume != 0) {
 				mUnitsRatio = 1000.0 / density / volume;
 				mUnitsText = String.format("%s of %s", volumeName, densityName);
 			} else if (weight != 0) {
 				mUnitsRatio = 1.0 / weight;
 				mUnitsText = String.format("%s", weightName);
 			} else {
 				Toast.makeText(this, "Does not compute", Toast.LENGTH_LONG).show();
 				mTts.speak("Does not compute", TextToSpeech.QUEUE_FLUSH, null);
 			}
 
 			Editor settingsEditor = mSettings.edit();
 			mUnitsView.setText(mUnitsText);
 			settingsEditor.putString("unitsText", mUnitsText);
 			settingsEditor.putFloat("unitsRatio", (float) mUnitsRatio);
 			settingsEditor.commit();
 
 		}
 
 		super.onActivityResult(requestCode, resultCode, data);
 		// startActivity(new Intent(Intent.ACTION_VIEW,
 		// Uri.parse("http://www.youtube.com/watch?v=2qBgMmRMpOo")));
 	}
 
 	public void onInit(int status) {
 		
 		if (status == TextToSpeech.SUCCESS) {
 			mTts.setLanguage(Locale.US);
 			mTts.setPitch(.9f);
 		}
 		else {
 			Log.e(TAG,"TTS Initilization Failed!");
 		}
 		// TODO Auto-generated method stub
 
 	}
 
 }
