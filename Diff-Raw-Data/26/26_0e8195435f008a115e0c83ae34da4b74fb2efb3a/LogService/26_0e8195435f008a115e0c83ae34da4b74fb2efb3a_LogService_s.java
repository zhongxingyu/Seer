 package remote.control.android;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.text.DecimalFormat;
 import java.text.DecimalFormatSymbols;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.ListIterator;
 
 import com.google.protobuf.InvalidProtocolBufferException;
 
 import common.files.android.Constants;
 
 import remote.control.android.Command.*;
 
 import android.app.IntentService;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.os.Bundle;
 import android.os.Environment;
 import android.util.Log;
 import android.widget.Toast;
 
 public class LogService extends IntentService implements SensorEventListener
 {
 
 	private static final String TAG = "REMOTE";
 	public static boolean logStarted = false;
 	public static boolean accSensor = false;
 	public static boolean accBrainSensor = false;
 	public static boolean usAdkSensor = false;
 	private int logDelay = 5000;
 	Sensor accelerometer;
 	SensorManager sm;
 	List<Float> sensorDataRemote = new ArrayList<Float>();
 	List<Float> sensorDataAdk = new ArrayList<Float>();
 
 	private File accfile = new File(Environment.getExternalStorageDirectory()+File.separator + "remote_acc_data.txt");
 	private File accbrainfile = new File(Environment.getExternalStorageDirectory()+File.separator + "brain_acc_data.txt");
 	private File usfile = new File(Environment.getExternalStorageDirectory()+File.separator + "adk_us_data.txt");
 
 
 	public LogService() 
 	{
 		super("LogService");
 	}
 
 	@Override
 	public void onCreate() 
 	{
 		super.onCreate();
 		Log.d(TAG, "IntentService onCreate");
 		IntentFilter intentFilter = new IntentFilter();
 		intentFilter.addAction("StartLogAction");
 		intentFilter.addAction("StopLogAction");
 		intentFilter.addAction("CheckboxAccRemoteAction");
 		intentFilter.addAction("CheckboxAccBrainAction");
 		intentFilter.addAction("CheckboxUsOnAdkAction");
 		intentFilter.addAction(Constants.Broadcast.LogService.Actions.ADK_US_RESPONSE);
 		registerReceiver(broadcastReceiver, intentFilter);
 		initSensors();
 	}
 
 	@Override
 	public void onDestroy()
 	{
 		Log.d(TAG, "IntentService onDestroy");
 		unregisterReceiver(broadcastReceiver);
 	}
 
 	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver()
 	{
 		@Override
 		public void onReceive(Context context, Intent intent)
 		{
 			String action = intent.getAction();
 			if(action.equalsIgnoreCase("StartLogAction"))
 			{
 				Log.d(TAG, "StartLogAction received");
 				Bundle bundle = intent.getExtras();
 				logDelay = bundle.getInt("logDelay");
 				logStarted = true;
 
 			}
 			else if(action.equalsIgnoreCase("StopLogAction"))
 			{
 				Log.d(TAG, "StopLogAction received");
 				logStarted = false;
 			}
 
 			else if(action.equalsIgnoreCase("CheckboxAccRemoteAction"))
 			{
 				Log.d(TAG, "CheckboxAccRemoteAction received");
 				accSensor = !accSensor;
 				Log.d(TAG, Boolean.toString(accSensor));
 				try 
 				{
 					if(accfile.createNewFile())
 					{
 						headerToSd(accfile);
 					}
 				} 
 				catch (IOException e)
 				{
 					e.printStackTrace();
 				}							
 			}
 			else if(action.equalsIgnoreCase("CheckboxAccBrainAction"))
 			{
 				Log.d(TAG, "CheckboxAccBrainAction received");
 				accBrainSensor = !accBrainSensor;
 				Log.d(TAG, Boolean.toString(accBrainSensor));
 				try 
 				{
 					if(accbrainfile.createNewFile())
 					{
 						headerToSd(accbrainfile);
 					}
 				} 
 				catch (IOException e) 
 				{
 					e.printStackTrace();
 				}	
 			}
 			else if(action.equalsIgnoreCase("CheckboxUsOnAdkAction"))
 			{
 				Log.d(TAG, "CheckboxUsOnAdkAction received");
 				usAdkSensor = !usAdkSensor;
 				Log.d(TAG, Boolean.toString(usAdkSensor));
 				try 
 				{
 					if(usfile.createNewFile())
 					{
 						headerToSd(usfile);
 					}
 				} 
 				catch (IOException e)
 				{
 					e.printStackTrace();
 				}
 			} 
 			else if(action.equalsIgnoreCase(Constants.Broadcast.LogService.Actions.ADK_US_RESPONSE))
 			{
 //				Log.d(TAG, "usLog received from ADK");
 //				USSensors usSensorDataToLog = null;
 //				
 //				try 
 //				{
 //					usSensorDataToLog = USSensors.parseFrom(intent.getByteArrayExtra(Constants.Broadcast.LogService.Actions.Intent.BYTES));
 //				} 
 //				catch (InvalidProtocolBufferException e) 
 //				{
 //					e.printStackTrace();
 //				}
 //				
 //				USSensorData us1 = usSensorDataToLog.getUSSensorData1();
 //				USSensorData us2 = usSensorDataToLog.getUSSensorData2();
 //				USSensorData us3 = usSensorDataToLog.getUSSensorData3();
 //				USSensorData us4 = usSensorDataToLog.getUSSensorData4();
 //				
 //				float us1Value = (float)us1.getValue();
 //				float us2Value = (float)us2.getValue();
 //				float us3Value = (float)us3.getValue();
 //				float us4Value = (float)us4.getValue();
 //				
 //				Intent i = new Intent("printMessage");
 //				i.putExtra("coordinates", "Sensor data: \n Senor 1: " + String.valueOf(us1Value) + "\n Sensor 2: " + String.valueOf(us2Value));
 //				sendBroadcast(i);
 //				
 //				
 //				sensorDataAdk.clear();
 //				
 //				sensorDataAdk.add(us1Value);
 //				sensorDataAdk.add(us2Value);
 //				sensorDataAdk.add(us3Value);
 //				sensorDataAdk.add(us4Value);
 //				
 //				try 
 //				{
 //					if(accfile.createNewFile())
 //						headerToSd(usfile);
 //				}
 //				catch (IOException e) 
 //				{
 //					e.printStackTrace();
 //				}
 //				
 //				Log.d(TAG, "Logging usAdk");
 //				accToSd(sensorDataAdk,usfile);
 			}
 		}
 	};
 
 	@Override
 	protected void onHandleIntent(Intent intent)
 	{
 		while(true)
 		{
 			if(logStarted==true)
 			{
 				// Titta vilka sensorer vi ska logga
 				if(accSensor == true)
 				{
 					Log.d(TAG, "Logging acc from remote");
 					accToSd(sensorDataRemote,accfile);
 				}
 				if(accBrainSensor == true)
 				{				
 					Intent logAccBrainIntent = new Intent(Constants.Broadcast.BluetoothService.Actions.SendCommand.REQUEST_ACC_BRAIN_DATA);
 					logAccBrainIntent.putExtra(Constants.Broadcast.BluetoothService.Actions.SendCommand.Intent.TARGET,Constants.TARGET_BRAIN);
 					
 					sendBroadcast(logAccBrainIntent);
 
 					Context context2 = getApplicationContext();
 					Toast.makeText(context2, "ACC BRAIN Log Started", Toast.LENGTH_SHORT).show();
 					//accToSd(accbrainfile);
 				}
 				if(usAdkSensor == true)
 				{
 					byte[] test = new byte[1];
 					test[0] = (byte)3;
 					
 					Intent logUsIntent = new Intent(Constants.Broadcast.BluetoothService.Actions.SendCommand.REQUEST_US_DATA);
 					logUsIntent.putExtra(Constants.Broadcast.BluetoothService.Actions.SendCommand.Intent.TARGET,Constants.TARGET_ADK);
 					logUsIntent.putExtra(Constants.Broadcast.BluetoothService.Actions.SendCommand.Intent.BYTES, test);
 					sendBroadcast(logUsIntent);
 
 					Context context2 = getApplicationContext();
 					Toast.makeText(context2, "US Log Started", Toast.LENGTH_SHORT).show();
 				} 
 			}
 
 			// delay med en viss tid t.ex. 5sek.
 			try 
 			{
 				Thread.sleep(logDelay);
 			} 
 			catch (InterruptedException e)
 			{
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private void initSensors()
 	{
 		sm = (SensorManager)getSystemService(SENSOR_SERVICE);
 		sm.registerListener((SensorEventListener) this, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
 	}
 
 	public void onAccuracyChanged(Sensor sensor, int accuracy)
 	{		
 
 	}
 
 	public void onSensorChanged(SensorEvent event)
 	{
 		Sensor sensor = event.sensor;
 		if (sensor.getType() == Sensor.TYPE_ACCELEROMETER)
 		{
 			sensorDataRemote.clear();
 			sensorDataRemote.add(event.values[0]);
 			sensorDataRemote.add(event.values[1]);
 			sensorDataRemote.add(event.values[2]);
 		}
 	}
 
 	public void accToSd(List<Float> sensorList, File file)
 	{
 		try
 		{
 			FileOutputStream fo = new FileOutputStream(file, true);
 			PrintWriter oWriter = new PrintWriter(fo);
 			DecimalFormat form = new DecimalFormat("#.##");
 			DecimalFormatSymbols format = new DecimalFormatSymbols();
 			format.setDecimalSeparator('.');
 			form.setDecimalFormatSymbols(format);
 			ListIterator<Float> lI = sensorList.listIterator();
 			while(lI.hasNext())
 			{
 				oWriter.append(form.format(lI.next()));
 				if(lI.hasNext())
 				{
 					oWriter.append(',');
 				}
 			}			
 			SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy,kk:mm:ss:SSS");
 			Date now = new Date();
 			String strDate = sdfDate.format(now);
 			oWriter.append(","+strDate);
 			oWriter.append("\r\n");
 			oWriter.close();
 		}
 		catch(IOException e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	public void headerToSd(File file) throws IOException
 	{
 		FileOutputStream fo = new FileOutputStream(file, true);
 		PrintWriter oWriter = new PrintWriter(fo);
 		Log.d(TAG, "SKRIVER HEADER MED headerToSd");
 		if(file == accfile || file == accbrainfile)
 		{
 			oWriter.println("X,Y,Z,date,time");	
 		}
 		else if(file == usfile)
 		{
 			oWriter.println("FrontLeft,FrontRight,BackLeft,BackRight,date,time");
 		}
 		oWriter.close();
 	}   
 }
 
