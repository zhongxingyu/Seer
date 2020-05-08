 package edu.wisc.myrotsens;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.text.DateFormat;
 import java.text.DecimalFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Scanner;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.SystemClock;
 import android.app.Activity;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class RotSensActivity extends Activity implements SensorEventListener,
 		OnClickListener {
 
 	// debouncing the touch screen touch event
 	protected boolean debounceFlag = false;
 	private int debounceCnt = 0;
 
 	// a huge array to hold sampling data for training and detection
 	// private ArrayList<float[]> sensorData;
 	// private ArrayList<float[]> liAccelData;
 
 	// constant used for how many sampled data per training entry are collected
 	private final int TRAINING_SET_ENTRY_NUM = 1;
 	// private volatile int trainingSetEntryIndex;
 	// features number, without bias point
 	private final int ALL_FEATURES_NUM = 19;
 	// number of characters(keys) for the keyboard
 	private final int CHARACTER_NUM = 26;
 	// array of learning object for each character
 	private CharNN[] mCharNN;
 	private int curTrainingChar;
 	private Button[] trainingButtons;
 
 	private SensorManager mSensorManager;
 	private Sensor mGyro;
 	private MSensor gyro;
 	private MSensor liAccel;
 	private TextView mGyroXView;
 	private TextView mGyroYView;
 	private TextView mGyroZView;
 	private TextView mGyroAccuView;
 
 	private Button detectButton;
 	private boolean inCollectKeyPress;
 
 	private Button wrFileButton;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_rot_sens);
 		// initialize screen components and sensors
 		initializeScrAndSen();
 		curTrainingChar = 0;
 		// test for external jars
 		// DescriptiveStatistics stats= new DescriptiveStatistics(1);
 		for (int index = 0; index < trainingButtons.length; index++) {
 			trainingButtons[index].setOnClickListener(this);
 		}
 		// create a directory for the apps' data
 		File dir = new File("/storage/sdcard0/sensorData/");
 		if (!dir.exists()) {
 			boolean mkdirSuc = dir.mkdirs();
 			Log.i(this.toString(), "directory creation:" + mkdirSuc);
 		}
 		inCollectKeyPress = false;
 		// detectSetEntryIndex = 0;
 	}
 
 	@Override
 	public void onSensorChanged(SensorEvent event) {
 		// if not in de-bounce, collect normally
 		if (false == this.debounceFlag) {
 			MSensor curSensor = null;
 			switch (event.sensor.getType()) {
 			case Sensor.TYPE_GYROSCOPE:
 				curSensor = this.gyro;
 				break;
 			case Sensor.TYPE_LINEAR_ACCELERATION:
 				curSensor = this.liAccel;
 				break;
 			}
 			float[] curSensorReading = new float[3];
 			System.arraycopy(event.values, 0, curSensorReading, 0, 3);
 			// if in training
 			if (curSensor.inTraining()) {
 				// if in key stroke
 				if (curSensor.inSampling()) {
 					curSensor.keyStrokeSampleData.add(curSensorReading);
 					if (!overThreshold(curSensorReading, curSensor)) {
 						curSensor.underThreshCnt++;
 					} else {
 						// clr if over threshold, detecting continuity
 						curSensor.underThreshCnt = 0;
 					}
					if (100 <= curSensor.underThreshCnt) {
 						// finish a key stroke detect
 						curSensor.setSamplingFlag(false);
 						// a separate array for writing to file and processing,
 						// not blocking ui thread
 						double[][] keyStrokeData = new double[3][curSensor.keyStrokeSampleData
 								.size()+curSensor.prevKeyStrokeSampleData.size()];
 						List<float[]> combinedList = new ArrayList<float[]>(curSensor.prevKeyStrokeSampleData);
 						combinedList.addAll(curSensor.keyStrokeSampleData);
 						keyStrokeData = copyListToArray(combinedList);
 						Object[] asyncParam = new Object[2];
 						asyncParam[0] = keyStrokeData;
 						asyncParam[1] = curSensor;
 						// after finishing detecting a key stroke
 						TrainingAsyncTask curTraining = new TrainingAsyncTask();
 						curTraining.executeOnExecutor(
 								AsyncTask.THREAD_POOL_EXECUTOR, asyncParam);
 						Log.i(this.toString(), curSensor.getSensor().getName()
 								+ " finish collecting key stroke data ");
 						Toast.makeText(
 								this.getBaseContext(),
 								curSensor.getSensor().getName()
										+ "finished collecting key stroke data",
 								Toast.LENGTH_SHORT).show();
 					}
 				} else {// if in training, but not in a key stroke
 					if (overThreshold(curSensorReading, curSensor)) {
 						curSensor.setSamplingFlag(true);
 						curSensor.underThreshCnt = 0;
 						curSensor.keyStrokeSampleData.clear();
 						curSensor.keyStrokeSampleData.add(curSensorReading);
 						Log.i(this.toString(), curSensor.getSensor().getName()
 								+ " start collecting key stroke data ");
 						Toast.makeText(
 								this.getBaseContext(),
 								curSensor.getSensor().getName()
 										+ " start collecting key stroke data",
 								Toast.LENGTH_SHORT).show();
 					} else {
 						if (5 <= curSensor.prevKeyStrokeSampleData.size()) {
 							curSensor.prevKeyStrokeSampleData.remove(0);
 						}
 						curSensor.prevKeyStrokeSampleData.add(curSensorReading);
 					}
 				}
 			}
 		} else {// if in debounce, discard 100 samples
 			this.debounceCnt++;
 			// when finished debouncing
 			if (1000 == this.debounceCnt) {
 				this.debounceCnt = 0;
 				this.debounceFlag = false;
 				Toast.makeText(this.getBaseContext(), "finished debouncing",
 						Toast.LENGTH_SHORT).show();
 			}
 		}
 	}
 
 	private boolean overThreshold(float[] values, MSensor curSensor) {
 		boolean over = false;
 		switch (curSensor.getSensor().getType()) {
 		case Sensor.TYPE_GYROSCOPE:
 			for (int index = 0; index < values.length; index++) {
				if (values[index] > 0.05 || values[index] < -0.05)
 					over = true;
 			}
 			break;
 		case Sensor.TYPE_LINEAR_ACCELERATION:
 			for (int index = 0; index < values.length; index++) {
				if (values[index] > 1 || values[index] < -1)
 					over = true;
 			}
 			break;
 		}
 		return over;
 	}
 
 	/**
 	 * convert a list into array. specifically designed for sensorData
 	 * 
 	 * @param mList
 	 * @return
 	 */
 	private double[][] copyListToArray(List<float[]> mList) {
 		if (mList.isEmpty()) {
 			return null;
 		} else {
 			double[][] result = new double[mList.get(0).length][mList.size()];
 			for (int eventNum = 0; eventNum < mList.size(); eventNum++) {
 				for (int index = 0; index < mList.get(eventNum).length; index++)
 					result[index][eventNum] = mList.get(eventNum)[index];
 			}
 			return result;
 		}
 	}
 
 	/**
 	 * write collected sampling to file
 	 * 
 	 * @param fileName
 	 * @return
 	 */
 	private boolean writeSamplesToFile(String fileName, double[][] data) {
 		File mFile = new File(fileName);
 		FileOutputStream outputStream = null;
 		try {
 			// choose to overwrite instead of append
 			outputStream = new FileOutputStream(mFile, false);
 			OutputStreamWriter osw = new OutputStreamWriter(outputStream);
 			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
 			Date date = new Date();
 			DecimalFormat df = new DecimalFormat("##0.0000;##0.0000");
 			String header = "raw data with key press event: \n"
 					+ "file created time: " + dateFormat.format(date) + "\n"
 					+ "x-axis               y-axis             z-axis\n";
 			osw.write(header);
 			for (int entryIndex = 0; entryIndex < data[0].length; entryIndex++) {
 				for (int component = 0; component <= 2; component++) {
 					osw.write(df.format(data[component][entryIndex])
 							+ "            ");
 				}
 				osw.write("\n");
 			}
 			osw.flush();
 			osw.close();
 			osw = null;
 		} catch (FileNotFoundException e) {
 			System.out.println("the directory doesn't exist!");
 			return false;
 		} catch (IOException e) {
 			System.out.println("IOException occurs");
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * discard the first 100 samples after touching the screen to filter out
 	 * touching effect
 	 */
 	protected void debounce() {
 		this.debounceCnt = 0;
 		this.debounceFlag = true;
 	}
 
 	/**
 	 * set the on click listeners for all training buttons
 	 */
 	@Override
 	public void onClick(View v) {
 		debounce();
 		// set current training character
 		curTrainingChar = v.getId();
 		gyro.setTrainingFlag(true);
 		liAccel.setTrainingFlag(true);
 		// this.detectSetEntryIndex = 0;
 		Toast.makeText(this.getBaseContext(),
 				"start training letter:" + (char) (v.getId() + 97),
 				Toast.LENGTH_SHORT).show();
 
 	}
 
 	/**
 	 * a training thread running in the background of the application functions:
 	 * 1. if there is a key press, write raw samples to file 2. write the
 	 * features and extracted key press data to file 3. add data entry into
 	 * training data set 4. train the neural network
 	 * 
 	 * @author JJ
 	 * 
 	 */
 	private class TrainingAsyncTask extends AsyncTask<Object, Integer, Void> {
 
 		@Override
 		protected void onProgressUpdate(Integer... params) {
 			switch (params[0]) {
 			case 0:
 				Toast.makeText(
 						getApplicationContext(),
 						"succesful get key press training data. input another training entry. Currently:"
 								+ params[1], Toast.LENGTH_SHORT).show();
 				break;
 			case 1:
 				Toast.makeText(getApplicationContext(),
 						"finished writing key strokes samples to file",
 						Toast.LENGTH_SHORT).show();
 				break;
 			case 2:
 				Toast.makeText(getApplicationContext(),
 						"no key press event found", Toast.LENGTH_SHORT).show();
 				break;
 			}
 		}
 
 		@Override
 		protected Void doInBackground(Object... params) {
 			MSensor mSensor = (MSensor) params[1];
 			String sensorTypeString = null;
 			switch (mSensor.getSensor().getType()) {
 			case Sensor.TYPE_GYROSCOPE:
 				sensorTypeString = "gyro";
 				break;
 			case Sensor.TYPE_LINEAR_ACCELERATION:
 				sensorTypeString = "liAccel";
 				break;
 			default:
 				sensorTypeString = "wrong";
 			}
 			mSensor.trainingSetEntryIndex++;
 			// if haven't collected enough training entries in the training set
 			if (mSensor.trainingSetEntryIndex < TRAINING_SET_ENTRY_NUM) {
 				// write raw data to file
 				writeSamplesToFile("/storage/sdcard0/sensorData/"
 						+ sensorTypeString + "_raw_training_"
 						+ (char) (curTrainingChar + 97)
 						+ mSensor.trainingSetEntryIndex + ".log",
 						(double[][]) params[0]);
 				Log.i(this.toString(), "finish writing 1 file");
 			} else {
 				// write raw data to file
 				writeSamplesToFile("/storage/sdcard0/sensorData/"
 						+ sensorTypeString + "_raw_training_"
 						+ (char) (curTrainingChar + 97)
 						+ mSensor.trainingSetEntryIndex + ".log",
 						(double[][]) params[0]);
 				// if finished collecting all training set entries
 				mSensor.trainingSetEntryIndex = 0;
 				mSensor.setTrainingFlag(false);
 				publishProgress(1);
 			}
 			return null;
 		}
 	}
 
 	@Override
 	public void onStop() {
 		super.onStop();
 		// unregister sensor listeners to prevent the activity from draining the
 		// device's battery.
 		mSensorManager.unregisterListener(this);
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		// unregister sensor listeners to prevent the activity from draining the
 		// device's battery.
 		mSensorManager.unregisterListener(this);
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		// restore the sensor listeners
 		registerSens();
 	}
 
 	@Override
 	public void onAccuracyChanged(Sensor sensor, int accuracy) {
 	}
 
 	/**
 	 * initialize screen component and register sensors
 	 */
 	private void initializeScrAndSen() {
 		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
 		// grabbing the composite sensors. data may be averaged or filtered
 		// use getSensorList for grabbing raw sensors
 		mGyro = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
 		Sensor mLiAccel = mSensorManager
 				.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
 		registerSens();
 
 		// create my sensors for gyroscope
 		this.gyro = new MSensor(this.getBaseContext(), mGyro, mGyroXView,
 				mGyroYView, mGyroZView, mGyroAccuView);
 		this.liAccel = new MSensor(this.getBaseContext(), mLiAccel, null, null,
 				null, null);
 
 		this.detectButton = (Button) this.findViewById(R.id.detectButton);
 		this.detectButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// wait 1s before detection to cancel the effect of pressing
 				// button
 				SystemClock.sleep(500);
 				debounce();
 				// change the state of detection
 				// gyro.setDetectFlag(true);
 				Log.i(this.toString(), "START detection");
 				Toast.makeText(getApplicationContext(),
 						"detection:" + gyro.inDetect(), Toast.LENGTH_SHORT)
 						.show();
 			}
 		});
 
 		// button for getting training data from file
 		Button trainFromFileButton = new Button(this);
 		trainFromFileButton.setText("FileTrain");
 		trainFromFileButton.setId(40);
 		trainFromFileButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Toast.makeText(getApplicationContext(),
 						"start reading training data from file",
 						Toast.LENGTH_SHORT).show();
 				Log.i(this.toString(), "start reading training data from file");
 				new TrainingFromFileAsyncTask()
 						.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
 			}
 		});
 		trainFromFileButton.setLayoutParams(new TableRow.LayoutParams(0,
 				android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 2.5f));
 		// button for stop detection
 		Button stopDetectButton = new Button(this);
 		stopDetectButton.setText("stopDetect");
 		stopDetectButton.setId(40);
 		stopDetectButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				gyro.setDetectFlag(false);
 				liAccel.setDetectFlag(false);
 				Toast.makeText(getApplicationContext(), "stop detect",
 						Toast.LENGTH_LONG).show();
 				Log.i(this.toString(), "stop detect");
 			}
 		});
 		stopDetectButton.setLayoutParams(new TableRow.LayoutParams(0,
 				android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 2.5f));
 		TableRow firstButtonRow = (TableRow) findViewById(R.id.tableRow15);
 		firstButtonRow.addView(trainFromFileButton);
 		firstButtonRow.addView(stopDetectButton);
 
 		// create buttons for each character that needs to be trained
 		trainingButtons = new Button[CHARACTER_NUM];
 		TableLayout tl = (TableLayout) findViewById(R.id.tableLayout1);
 		for (int index = 0; index < CHARACTER_NUM; index = index + 4) {
 			TableRow tr1 = new TableRow(this);
 			// generate button on each row
 			for (int colIndex = index; colIndex <= index + 3
 					&& colIndex < CHARACTER_NUM; colIndex++) {
 				Button myButton1 = new Button(this);
 				myButton1.setText(String.valueOf((char) (colIndex + 97)));
 				myButton1.setId(colIndex);
 				myButton1.setOnClickListener(this);
 				trainingButtons[colIndex] = myButton1;
 				// important for the layout
 				myButton1
 						.setLayoutParams(new TableRow.LayoutParams(
 								0,
 								android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
 								2.5f));
 				tr1.addView(myButton1);
 			}
 			TableLayout.LayoutParams tableRowParams = new TableLayout.LayoutParams(
 					TableLayout.LayoutParams.MATCH_PARENT,
 					TableLayout.LayoutParams.WRAP_CONTENT);
 			tableRowParams.setMargins(0, 8, 0, 0);
 			tr1.setLayoutParams(tableRowParams);
 			tl.addView(tr1);
 		}
 	}
 
 	/**
 	 * register needed sensor listeners before using it set the sampling as fast
 	 * as possible accel frequency on galaxy nexus: fastest:122Hz,
 	 * game:60Hz,UI:15Hz,Normal:15Hz gyroscope is relatively 10Hz less in each
 	 * category
 	 */
 	private void registerSens() {
 		mSensorManager.registerListener(this,
 				mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
 				SensorManager.SENSOR_DELAY_FASTEST);
 		mSensorManager.registerListener(this, mSensorManager
 				.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
 				SensorManager.SENSOR_DELAY_FASTEST);
 	}
 
 	/**
 	 * implement SensorEventListener interface
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.rot_sens, menu);
 		return true;
 	}
 
 	/**
 	 * read x,y,z data from a give file
 	 * 
 	 * @param fileS
 	 * @return data in a individual file
 	 */
 	private double[][] readDataFromFile(String fileS) {
 		// if not empty or null,then read from file
 		if (!((fileS == null) || (fileS.isEmpty()))) {
 			File dataFile = new File(fileS);
 			int dataLineNum = 0;
 			Scanner preScanner = null;
 			// pre-scan determine how many lines of data there are
 			try {
 				preScanner = new Scanner(dataFile);
 				while (preScanner.hasNextLine()) {
 					// only read in lines starting with a float
 					// number
 					if (preScanner.hasNextFloat()) {
 						dataLineNum++;
 						preScanner.nextLine();
 					} else
 						preScanner.nextLine();
 				}
 			} catch (IOException e) {
 				System.out.println("dataFile doesn't exist!");
 			} finally {
 				if (preScanner != null) {
 					preScanner.close();
 					preScanner = null;
 				}
 			}
 			// the whole data from a single file
 			double[][] dataSet = new double[3][dataLineNum];
 			// read the file into data set
 			Scanner mScanner = null;
 			try {
 				mScanner = new Scanner(dataFile);
 				int dataLineIndex = 0;
 				while (mScanner.hasNextLine()) {
 					if (mScanner.hasNextFloat()) {
 						// x axis
 						dataSet[0][dataLineIndex] = mScanner.nextFloat();
 						// y axis
 						dataSet[1][dataLineIndex] = mScanner.nextFloat();
 						// z axis
 						dataSet[2][dataLineIndex] = mScanner.nextFloat();
 						// inc to next line
 						mScanner.nextLine();
 						dataLineIndex++;
 					} else
 						mScanner.nextLine();
 				}
 			} catch (FileNotFoundException e) {
 				System.out.println("dataFile doesn't exist!");
 			} finally {
 				if (mScanner != null) {
 					mScanner.close();
 					mScanner = null;
 				}
 			}
 			System.out.println("finished reading file:" + fileS);
 			return dataSet;
 		} else {
 			System.out.println("no such fils or the file is empty:" + fileS);
 			return null;
 		}
 	}
 
 	/**
 	 * async class for training from file
 	 * 
 	 * @author JJ
 	 * 
 	 */
 	private class TrainingFromFileAsyncTask extends
 			AsyncTask<Void, Integer, Void> {
 
 		@Override
 		protected void onPreExecute() {
 			Toast.makeText(getApplicationContext(),
 					"start reading from file training", Toast.LENGTH_SHORT)
 					.show();
 		}
 
 		@Override
 		protected void onProgressUpdate(Integer... curChar) {
 			Toast.makeText(
 					getApplicationContext(),
 					"wait! still in training from file: "
 							+ (char) (curChar[0] + 97), Toast.LENGTH_SHORT)
 					.show();
 		}
 
 		@Override
 		protected Void doInBackground(Void... param) {
 			File sdCardRoot = new File("/storage/sdcard0");
 			// for each character get the training data set from folder
 			for (int charIndex = 0; charIndex < CHARACTER_NUM; charIndex++) {
 				publishProgress(charIndex);
 				// read from file
 				File myDir = new File(sdCardRoot, "/trainingData/"
 						+ (char) (charIndex + 97));
 				// if there are folder for this specific character
 				if (myDir.listFiles() != null) {
 					// create new learning object for each character that needs
 					// to be trained
 					mCharNN[charIndex] = new CharNN((char) (charIndex + 97),
 							ALL_FEATURES_NUM);
 					for (File f : myDir.listFiles()) {
 						String fileS = f.getName();
 						// get individual file result
 						double[][] fileData = readDataFromFile(myDir + "/"
 								+ fileS);
 						KeyPress mKeyPress = new KeyPress(fileData);
 						if (mKeyPress.hasKeyPress()) {
 							double[] mFeatures = mKeyPress.getFeatures();
 							mCharNN[charIndex]
 									.addEntryToTrainingDataSet(mFeatures);
 							Log.i(this.toString(),
 									"adding files for training char "
 											+ (char) (charIndex + 97) + " "
 											+ fileS);
 							// release the object
 							mKeyPress = null;
 						}
 					}
 					Log.i(this.toString(), "training char "
 							+ (char) (charIndex + 97));
 					// train the neural network
 					mCharNN[charIndex].train();
 					Log.i(this.toString(), "finished training char "
 							+ (char) (charIndex + 97));
 				} else {
 					Log.i(this.toString(), "no folder for training for char: "
 							+ (char) (charIndex + 97));
 					mCharNN[charIndex] = null;
 				}
 			}
 			return null;
 		}
 
 		protected void onPostExecute(Void params) {
 			Toast.makeText(getApplicationContext(),
 					"finished training from file", Toast.LENGTH_SHORT).show();
 		}
 	}
 }
