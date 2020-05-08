 package edu.wisc.myrotsens;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.text.DecimalFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import android.content.Context;
 import android.hardware.Sensor;
 import android.hardware.SensorManager;
 import android.util.Log;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * Sensor class for calibration,sampling, and updating display
  * 
  * @author JJ
  * 
  */
 public class MSensor {
 	private Sensor mSensor;
 	private TextView mViewVal[];
 	private boolean calibFlag;
 	private int calibCnt;
 	private float calibSamples[][];
 	// when startup calibration result is 0.0f
 	private float calibResult[];
 	// normal sample results
 	private float sampleVals[];
 	// sensor current accuracy
 	// depends on hardware implementation, maybe not reliable
 	private int sensorAccu = -1;
 	// calibration sampling number constant
 	private final int CALIB_SAMPLE_NUM = 300;
 	private final int TEXTVIEW_X = 0;
 	private final int TEXTVIEW_Y = 1;
 	private final int TEXTVIEW_Z = 2;
 	private final int TEXTVIEW_ACCU = 3;
 
 	// write sampling data to different files
 	private Context mContext;
 	// private File mFile;
 	private int fileID;
 	private boolean wrFileFlag;
 	// number of sampled data written to file
 	private final int WRITE_SAMPLE_NUM = 1000;
 	private int wrCnt;
 	
 	//previous time when sensor has changed
 	private long startTimeStamp;
 
 	//output file writer
 	private OutputStreamWriter osw=null;
 	
 	public MSensor(Context context, Sensor sensor, TextView textViewXVal,
 			TextView textViewYVal, TextView textViewZVal,
 			TextView textViewAccuVal) {
 		this.mContext = context;
 		this.mSensor = sensor;
 		this.mViewVal = new TextView[4];
 		this.mViewVal[TEXTVIEW_X] = textViewXVal;
 		this.mViewVal[TEXTVIEW_Y] = textViewYVal;
 		this.mViewVal[TEXTVIEW_Z] = textViewZVal;
 		this.mViewVal[TEXTVIEW_ACCU] = textViewAccuVal;
 		this.calibFlag = false;
 		this.calibCnt = 0;
 		this.fileID = 0;
 		this.sampleVals=new float[4];
 		this.calibResult=new float[4];
 		wrFileFlag = false;
 	}
 
 	public Sensor getSensor() {
 		return this.mSensor;
 	}
 
 	/**
 	 * 
 	 * @return TextView associated with sensor for display item 0 is for X axis,
 	 *         1 for Y axis, 2 for Z axis, 3 for accuracy
 	 */
 	public TextView[] getTextView() {
 		return this.mViewVal;
 	}
 
 
 	/**
 	 * @return base sample data after calibration
 	 */
 	public float[] getCalibResult() {
 		return this.calibResult;
 	}
 
 	/**
 	 * 
 	 * @return the start time stamp when a new file is created
 	 */
 	public long getStartTimeStamp() {
 		return this.startTimeStamp;
 	}
 	
 	
 	/**
 	 * @return whether we are in calibration phase
 	 */
 	public boolean inCalibrate() {
 		return this.calibFlag;
 	}
 
 	/**
 	 * @return whether we are in calibration phase
 	 */
 	public boolean inWrToFile() {
 		return this.wrFileFlag;
 	}
 
 	/**
 	 * @return sampled values from sensors
 	 */
 	public float[] getSample() {
 		return this.sampleVals;
 	}
 
 	/**
 	 * set sampling values
 	 * 
 	 * @param sampleData
 	 */
 	public void setSampleVals(float[] sampleData) {
 		//get the sampled data
 		System.arraycopy(sampleData, 0, this.sampleVals, 0, sampleData.length);
 	}
 
 	/**
 	 * set sampling values
 	 * 
 	 * @param sampleData
 	 */
 	public void setSensorAccuracy(int accuracy) {
 		this.sensorAccu = accuracy;
 	}
 
 	/**
 	 * start calibration process
 	 */
 	public void startCalibrate() {
 		this.calibFlag = true;
 		calibCnt = 0;
 		this.calibSamples = new float[CALIB_SAMPLE_NUM][3];
 		Log.i(this.toString(), "start calibration");
 	}
 
 	/**
 	 * use the passed in data as calibration data and keep sampling until get to
 	 * specified times
 	 * 
 	 * @param calibrateData
 	 */
 	public void calibrate(float[] calibrateData) {
 		// if haven't finish calibrate, then record the value
 		if (CALIB_SAMPLE_NUM != calibCnt) {
 			System.arraycopy(calibrateData, 0, this.calibSamples[calibCnt], 0, calibrateData.length);
 			calibCnt++;
 		} else { // finished calibration
 			this.calibFlag = false;
 			this.calibCnt = 0;
 			Log.i(this.toString(), "finish calibration");
 			for (int component = 0; component <= calibrateData.length-1; component++) {
 				double sum = 0;
 				// take the average
 				for (int sampleIndex = 0; sampleIndex <= this.CALIB_SAMPLE_NUM - 1; sampleIndex++) {
 					sum += calibSamples[sampleIndex][component];
 				}
 				calibResult[component] = (float) (sum / this.CALIB_SAMPLE_NUM);
 			}
 			Log.i(this.toString(), "calibration result" + calibResult[0] + " "
 					+ calibResult[1] + " " + calibResult[2] + " "+calibResult[3]);
 		}
 	}
 
 	/**
 	 * update the display of sensor accuracy
 	 */
 	public void updateAccuracyDisplay() {
 		switch (this.sensorAccu) {
 		case -1:
 			mViewVal[TEXTVIEW_ACCU].setText("unchanged");
 			break;
 		case SensorManager.SENSOR_STATUS_ACCURACY_LOW:
 			mViewVal[TEXTVIEW_ACCU].setText("low accuracy");
 			break;
 		case SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM:
 			mViewVal[TEXTVIEW_ACCU].setText("medium accuracy");
 			break;
 		case SensorManager.SENSOR_STATUS_ACCURACY_HIGH:
 			mViewVal[TEXTVIEW_ACCU].setText("high accuracy");
 			break;
 		case SensorManager.SENSOR_STATUS_UNRELIABLE:
 			mViewVal[TEXTVIEW_ACCU].setText("unreliable accuracy");
 			break;
 		}
 	}
 
 	/**
 	 * display corresponding values on screen
 	 * "corresponding" is determined by the subclass
 	 * by default it is calibrated data
 	 */
 	public void updateSampleDisplay() {
 		float data[]=new float[3];
 		data[0]=this.sampleVals[0]- this.calibResult[0];
 		data[1]=this.sampleVals[1]- this.calibResult[1];
 		data[2]=this.sampleVals[2]- this.calibResult[2];
 		this.updateSampleDisplay(data);
 	}
 	
 	/**
 	 * update displays of sample values values are x*sin(theta/2),
 	 * y*sin(theta/2),z*sin(theta/2)
 	 */	
 	public void updateSampleDisplay(float[] data){
 		// formatting display values
 		DecimalFormat df = new DecimalFormat("'+'000.0000;'-'000.0000");
 		this.mViewVal[TEXTVIEW_X].setText(df.format(data[0]));
 		this.mViewVal[TEXTVIEW_Y].setText(df.format(data[1]));
 		this.mViewVal[TEXTVIEW_Z].setText(df.format(data[2]));
 	}
 
 	/**
 	 * create a new file for dumping sample data write basic information about
 	 * sensor to file
 	 */
 	public void newWrToFile() {
 		// set writing to file flag
 		this.wrFileFlag = true;
 		Log.i(this.toString(), "writing to file");
 
 		String filename = this.mSensor.getName() + "_" + this.fileID + ".log";
 		// avoid spaces in file names
 		filename = filename.replaceAll(" ", "_");
 		// get date and time
 		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
 		dateFormatter.setLenient(false);
 		Date today = new Date();
 		String dateString = dateFormatter.format(today);
 		String header = "sensor name: "
 				+ this.mSensor.getName()
 				+ "\n"
 				+ "sensor min delay: "
 				+ this.mSensor.getMinDelay()
 				+ "\n"
 				+ "file created time: "
 				+ dateString
 				+ "\n"
 				+ "calibrated values: "
 				+ this.calibResult[0]
 				+ " "
 				+ this.calibResult[1]
 				+ " "
 				+ this.calibResult[2]
 				+ "\n"
 				+ "x-axis               y-axis             z-axis             time\n";
 		// reset number of sampled data written to file
 		this.wrCnt = 0;
 		FileOutputStream outputStream;
 		try {
 			// store sensor values to sdcard
 			// getExternalFilesDir always return null
 			// use hardcoded path to bypass
 			// getExternalFilesDir and getExternalStorageDir gives back
 			// "/storage/emulated/0/..." probably is referred to portable SD
 			// card instead of
 			// large storage space built in phone
 			// internal
 			File dir = new File("/storage/sdcard0/sensorData");
 			if (!dir.exists()) {
 				boolean mkdirSuc = dir.mkdirs();
 				Log.i(this.toString(), "directory creation:" + mkdirSuc);
 			}
 			File mfile = new File(dir, filename);
 			mfile.createNewFile();
 			Log.i(this.toString(),
 					"newly created file exists?:" + mfile.exists());
 			outputStream = new FileOutputStream(mfile);
 
 			// store to internal storage. cannot access with file explorer
 			// without root
 			// outputStream = mContext.openFileOutput(filename,
 			// Context.MODE_PRIVATE);
 			this.osw = new OutputStreamWriter(outputStream);
 			osw.write(header);
 			osw.flush();
 			Log.i(this.toString(), "created sdcard file for storage");
 		} catch (Exception e) {
 			Log.i(this.toString(),
 					"failed to open write-to file when writing header");
 		}
 	}
 
 
 	/**
 	 * write corresponding data to file
 	 * "corresponding" is determined by the subclass
 	 * by default it is calibrated data
 	 */
 	public void writeToFile(long timeDiff) {
 		float data[]=new float[3];
 		data[0]=this.sampleVals[0]- this.calibResult[0];
 		data[1]=this.sampleVals[1]- this.calibResult[1];
 		data[2]=this.sampleVals[2]- this.calibResult[2];
 		this.writeToFile(data,timeDiff);
 	}
 	
 	/**
 	 * write current sampled data into file
 	 */
	public void writeToFile(float[] data, long time){
		long timeDiff=-1;
 		if (this.wrCnt <= WRITE_SAMPLE_NUM) {
 			if (0==this.wrCnt){
				this.startTimeStamp=time;
 				timeDiff=0;
 			}
			//calculate the time diff
			timeDiff=time-this.startTimeStamp;
 			this.wrCnt++;
 			DecimalFormat df = new DecimalFormat("'+'##0.0000;'-'000.0000");
 			try {
 				osw.write(df.format(data[0] - this.calibResult[0])
 						+ "                 ");
 				osw.write(df.format(data[1] - this.calibResult[1])
 						+ "                 ");
 				osw.write(df.format(data[2] - this.calibResult[2])
 						+ "                 ");
 				//output in milli seconds
				osw.write(Math.round(timeDiff/1000000f)+ "                 \n");
 				osw.flush();
 			} catch (Exception e) {
 				Log.i(this.toString(),
 						"failed to open write-to file when writing sampled data");
 			}
 		} else {// if finished writing enough data
 			try {
 				osw.close();
 				osw=null;
 			} catch (IOException e) {
 				Log.i(this.toString(),
 						"failed to close output stream writer");
 			}
 			this.wrFileFlag = false;
 			this.fileID++;
 			Toast.makeText(this.mContext, "finished saving to file",
 					Toast.LENGTH_SHORT).show();
 			Log.i(this.toString(), "finished saving to file");
 		}
 	}
 }
