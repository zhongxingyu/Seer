 package edu.usf.eng.pie.avatars4change.myrunsdatacollectorlite;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.concurrent.ArrayBlockingQueue;
 
 //import weka.core.Attribute;
 //import weka.core.DenseInstance;
 //import weka.core.Instance;
 //import weka.core.Instances;
 //import weka.core.converters.ArffSaver;
 //import weka.core.converters.ConverterUtils.DataSource;
 import android.app.NotificationManager;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.IBinder;
 import android.util.Log;
 import android.widget.Toast;
 
 
 import com.meapsoft.FFT;
 
 import edu.usf.eng.pie.avatars4change.userData.userData;
 import edu.usf.eng.pie.avatars4change.wallpaper.avatarWallpaper;
 
 public class ServiceSensors extends Service implements SensorEventListener {
 	private String TAG = "ServiceSensors";
 	private static final int mFeatLen = Globals.ACCELEROMETER_BLOCK_CAPACITY + 2;
 	private int NOTIFICATION_ID =1;
 	private File mFeatureFile;
 	private File mResults;
 	private SensorManager mSensorManager;
 	private Sensor mAccelerometer;
 	private int mServiceTaskType;
 	private String mLabel;
 	
 	//private Instances mDataset;
 	//private Attribute mClassAttribute;
 	
 	private OnSensorChangedTask mAsyncTask;
 	
 	//BROADCAST MEMBER VARIABLE
 	private Intent mBroadcastIntent;
 	// float alpha =0.8; // t/(t + dT) with t, the low - pass filter 
 	//////////////////
 	
 	float[] gravity = new float[3];
 	float [] linear_acceleration = new float[3];
 	int [] buscar =new int[500];
     float lastX, lastY, lastZ;
     long interval;
   //  int classifiedValue;
     Context mContext = this;
     
 	// private NotificationManager mNM;
 
 	private static ArrayBlockingQueue<Double> mAccBuffer;
 	public static final DecimalFormat mdf = new DecimalFormat("#.##");
 	
 	@Override
 	public void onCreate() {
 		
 		mAccBuffer = new ArrayBlockingQueue<Double>(Globals.ACCELEROMETER_BLOCK_CAPACITY);
 		 
 		mAsyncTask = new OnSensorChangedTask();
 	    mAsyncTask.execute();
 		 
 		Toast.makeText(this, "My Service Created", Toast.LENGTH_LONG).show();
 		Log.d(TAG, "Service Created");	   
 		super.onCreate();		
 	}
 
 	public void onAccuracyChanged(Sensor sensor, int accuracy) {
 		// TODO Auto-generated method stub
 	}
 	
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId){
 		 //Log.i("LocalService", "Received start id " + startId + ": " + intent);
 		
 		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);		
 		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);	
 		mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
 		String toastDisp = "Entre a OnStartCommand";
 		
 		//ArrayList<Attribute> allAttr = new ArrayList<Attribute>();
 		
 		Bundle extras = intent.getExtras();
 		mLabel = extras.getString("label");
 		
 		Toast.makeText(this, mLabel, Toast.LENGTH_LONG).show();
 		//Log.d(TAG, "onStartCommand");
 		
 		//mFeatureFile = new File(getExternalFilesDir(null), "features.arff");
		mFeatureFile= new File(userData.getFileDir(getApplicationContext()),"sprites/face/default/feature-Still-walking" + ".arff");
 		 
 		//Log.d(TAG, mFeatureFile.getAbsolutePath());
 		//if the task is data collection create an empty dataset
 		//else (classify in real time), load the classify
 		
 		if("collecting".compareTo(extras.getString("type"))==0){
 			mServiceTaskType = Globals.SERVICE_TASK_TYPE_COLLECT;
 			
 		} else {
 			mServiceTaskType = Globals.SERVICE_TASK_TYPE_CLASSIFY;
 				
 			//////BROADCAST THE BROADCAST//////////////////////////////
 				
 			mBroadcastIntent = new Intent();
 			mBroadcastIntent.setAction(Globals.ACTION_MOTTION_UPDATED);
 				
 			///////////////////////////////////////////////////////////
 			}
 		 
 		// ArrayList<Attribute> allAttr = new ArrayList<Attribute>();
 		 DecimalFormat df = new DecimalFormat("0000");
 		 
 		 // for(int i=0; i < Globals.ACCELEROMETER_BLOCK_CAPACITY; i++){
 		  //allAttr.add(new Attribute("fft_coef_" + df.format(i)));
 		 // }
 		  // allAttr.add(new Attribute("max"));
 		   
 		   //ArrayList<String> labelItems = new ArrayList<String>(3);
 		   //labelItems.add("still");
 		   //labelItems.add("walking");
 		  // labelItems.add("running");
 		   
 		   //mClassAttribute = new Attribute("label", labelItems);
 		   
 		  // allAttr.add(mClassAttribute);
 		   
 		   //capacity 10000
 		  // mDataset = new Instances("accelerometer_features", allAttr, 10000);
 		   
 		   //Set the last column/attribute (walking, running...) as the class index classification
 		  
 		   //mDataset.setClassIndex(mDataset.numAttributes()-1);
 		   
 		   //create a new arff file
 		  // ArffSaver saver = new ArffSaver();
 		   
 		   //Set the data source of the file content
 		   //saver.setInstances(mDataset);
 		   
 		  // try{
 			//   saver.setFile(mFeatureFile);
 			   //saver.setFile(new File("./data/test.arff"));
 			   //saver.setFile(new File(userData.getFileDir(),"sprites/face/default/feature" + ".arff"));
 			//   saver.writeBatch();
 			   
 			//   } catch (IOException e){
 			//	   toastDisp = "Failed saving the file.  Check your storage";
 			//	   e.printStackTrace();		   
 			 //  }
 		     // Toast.makeText(getApplicationContext(), toastDisp, Toast.LENGTH_SHORT).show();
 
 		   return START_STICKY;
 	}
 	
 	 @Override
 	  public void onDestroy() {
 		 Toast.makeText(ServiceSensors.this, "Estoy en destroy", Toast.LENGTH_SHORT).show();
 	        //Log.d(TAG, "onDestroy");
 	    
 	   if(mAsyncTask != null)
 	    {
 	      if(!mAsyncTask.isCancelled())
 	    	  mAsyncTask.cancel(true);
 	    }
 	  }
 
 	public void onSensorChanged(SensorEvent event) {
 		//Toast.makeText(this, "Estoy on Sensor Change", Toast.LENGTH_LONG).show();
 		//Log.d(TAG, "OnSensorChanged");
 		
 		//Converting row acceleration to Linear aceleration acording to the example in android developers web page
 		final float alpha = 0.8f;
          
         // End of the data transformation
 	
 		if(event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
 			
 			 //lastX= event.values[SensorManager.DATA_X];
 			 //lastY= event.values[SensorManager.DATA_Y];
 			 //lastZ= event.values[SensorManager.DATA_Z];
 			 		
 			//I am not sure if its work, What I am doing here, is to compute the linear acceleration
 			
 			 gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
 	         gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
 	         gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];
 
 	         linear_acceleration[0] = event.values[0] - gravity[0];
 	         linear_acceleration[1] = event.values[1] - gravity[1];
 	         linear_acceleration[2] = event.values[2] - gravity[2];
 	         
 	  //Toast.makeText(this, Float.toString(lastX) + "-"+ Double.toString(linear_acceleration[0]) +":"+  Float.toString(lastY) + "-" +Double.toString(linear_acceleration[2])+":"+ Float.toString(lastZ) + "-"+Double.toString(linear_acceleration[1])  , Toast.LENGTH_LONG).show();
 	
 	      // Toast.makeText(this, Float.toString(lastX) + "-"+ Float.toString(event.values[0])  , Toast.LENGTH_LONG).show();	         
 	         
 			//double m= Math.sqrt(event.values[0]*event.values[0] + event.values[1]*event.values[1] + event.values[2]*event.values[2]);
 double m= Math.sqrt(linear_acceleration[0]*linear_acceleration[0] + linear_acceleration[1]*linear_acceleration[1] + linear_acceleration[2]*linear_acceleration[2]);
 			
 			//float m= (float) Math.sqrt(lastX*lastX + lastY*lastY + lastZ*lastZ);
 			
 			try{
 				mAccBuffer.add(new Double(m));
 			} catch (IllegalStateException e) {
 				ArrayBlockingQueue<Double> newBuf = new ArrayBlockingQueue<Double>(mAccBuffer.size()*2);
 				//Log.d(TAG, "Size of accel buffer increased to: " + newBuf.size());
 				mAccBuffer.drainTo(newBuf);
 				mAccBuffer =  newBuf;
 				mAccBuffer.add(new Double(m));
 			}
 		}
 	}
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
 //---
 private class OnSensorChangedTask extends AsyncTask<Void, Void, Void>{
 	//Intent intent1 = new Intent();
 
 	@Override
 	protected Void doInBackground(Void... arg0) {
 		
 		//Toast.makeText(ServiceSensors.this, "Estoy classificando", Toast.LENGTH_LONG).show();
 		//Toast.makeText(this, "doInBackground", Toast.LENGTH_LONG).show();
 		//Log.d(TAG, "doInBackground");
 		
 		//Instance inst = new DenseInstance(mFeatLen);
 		//inst.setDataset(mDataset);
 		int blockSize = 0;
 		FFT fft = new FFT(Globals.ACCELEROMETER_BLOCK_CAPACITY);
 		double[] accBlock = new double [Globals.ACCELEROMETER_BLOCK_CAPACITY];
 		double[] re= accBlock;
 		double[] im =  new double[Globals.ACCELEROMETER_BLOCK_CAPACITY];
 		
 		ArrayList<Double> featVect = new ArrayList<Double>(Globals.ACCELEROMETER_BLOCK_CAPACITY);
 		
 		double max = Double.MIN_VALUE;
 		
 		//boolean flag1= true;
 		
     	if(avatarWallpaper.sdPresent){
			mResults= new File(userData.getFileDir(getApplicationContext()),"mResults" + ".txt");
     	} else {
     		Log.e(TAG,"no sdCard! uh oh...");
     		//TODO: ???
     	}
 		while(true){
 			try{  if(isCancelled()){break;}
 				 
 				accBlock[blockSize++]= mAccBuffer.take().doubleValue();
 				
 				if(blockSize==Globals.ACCELEROMETER_BLOCK_CAPACITY){
 					blockSize = 0;
 				
 					//time = System.currentTimeMillis();
 					max = .0;
 					for(double val : accBlock){
 						if(max<val){
 							max=val;
 						}
 					}
 					fft.fft(re, im);
 					
 					//Log.v(TAG,"FFT len = " + Integer.toString(re.length) );
 					boolean oopsFlag = false; //this flag is to catch the error saving to userData.FFT
 					for(int i=0; i< re.length; i++){
 						double mag = Math.sqrt(re[i]*re[i]+ im[i]*im[i]);
 						if(i < 64){
 							userData.FFT[i] = mag;	//add to userData (just for fun)
 						} else {
 							oopsFlag = true;
 						}
 						//inst.setValue(i, mag);  //Remove as soon as possible
 						featVect.add(new Double(mag)); //New for classification
 						im[i] = .0; //Clear the field
 					}
 					if(oopsFlag){	//print error to debugger
 						Log.e(TAG,"oops! FFT is > 64. FFT.length = " + Integer.toString(re.length) );
 					}
 					
 					//Append max after frequency component
 					//inst.setValue(Globals.ACCELEROMETER_BLOCK_CAPACITY, max);  //Remove
 					//inst.setValue(mClassAttribute, mLabel); //Remove
 					//mDataset.add(inst);			//Remove
 					
 					featVect.add(new Double(max));
 					
 					int classifiedValue = (int) Classifier.classify(featVect.toArray());
 					
 					//Log.d(TAG, Integer.toHexString(classifiedValue));
 			
 					/////////////////BROADCAST/////////////////////////////////////////////////
 					
 					  Intent intent= new Intent("SOME_ACTION");
 				      //intent.setAction("com.example.HOLA");
 					  
 					  intent.putExtra("HOLA", Integer.toString(classifiedValue));
 					  sendBroadcast(intent);
 					  
 					  //////////////BROADCAST/////////////////////////////////////////////////
 					
 					//Log.v(TAG, "Motion update broadcast sent");
 					featVect.clear();
 					}
 				} catch(Exception e){
 					e.printStackTrace();
 				}
      		}
 			return null;
          }
 	
 	protected void muestra(int b){
 		Toast.makeText(getApplicationContext(), Integer.toString(b), Toast.LENGTH_SHORT).show();
 	}
 	
 	protected void onCancelled(){
 		Toast.makeText(getApplicationContext(), "Ultimo onCancell", Toast.LENGTH_SHORT).show();
 		Log.d(TAG, "onCancelled");
 		String toastDisp;
 		
 		if(mServiceTaskType == Globals.SERVICE_TASK_TYPE_CLASSIFY){
 			super.onCancelled();
 			return;
 		}
 		
 		
 		//if(mFeatureFile.exists()){
 			//DataSource source;
 			//try{
 				//source = new DataSource(new FileInputStream(mFeatureFile));
 				//Instances oldDataset = source.getDataSet();
 				//Log.i("TESTNULL", oldDataset.toString());
 				//oldDataset.setClassIndex(mDataset.numAttributes() -1);
 				
 				//if(!oldDataset.equalHeaders(mDataset)){
 				//	throw new Exception(" The two dataset have different headers: \n");
 				//}
 				//Move all items over
 				//for(int i= 0; i< mDataset.size(); i++){
 				//	oldDataset.add(mDataset.get(i));
 				//}
 				//mDataset = oldDataset;
 				//mFeatureFile.delete();			
 			
 			 // } catch(Exception e){
 			//	  e.printStackTrace();
 				  
 			 // }
 			//toastDisp = "Data file updated.";
 			
 		//}else {
 			//toastDisp = "Data file created.";
 		//}
 		//ArffSaver saver = new ArffSaver();
 		//saver.setInstances(mDataset);
 		//try{
 		//	saver.setFile(mFeatureFile);
 			//saver.writeBatch();
 		//} catch (IOException e){
 			//toastDisp = "Failed saving the file.  Check your storage.";
 			//e.printStackTrace();
 		//}
 		//System.out.println(mDataset);
 		toastDisp = "Failed saving the file.  Check your storage.";
 		Toast.makeText(getApplicationContext(), "Que pasa aqui", Toast.LENGTH_SHORT).show();
 		Log.d(TAG, "onCancelled");
 		super.onCancelled();		
 	}
 	  }
 }
