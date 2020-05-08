 /*
  * project		WiswWeather
  * package		forecastBuilder
  * author		Russell Carlin
  * date			May 1, 2013
  */
 package forecastBuilder;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 
 import android.app.Activity;
 import android.app.IntentService;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Message;
 import android.os.Messenger;
 import android.util.Log;
 
 public class RequestService extends IntentService{
 
 	public static final String FILENAME = "forecast.txt";
 	public static final String JSON_FORECAST = "forecast";
 	public static final String JSON_TEXT = "txt_forecast";
 	public static final String JSON_FORECASTDAY = "forecastday";
 	public static final String JSON_PERIOD = "title";
 	public static final String JSON_POP = "pop";
 	public static final String JSON_TXTFORECAST = "fcttext";
 	
 	private int result = Activity.RESULT_CANCELED;
 	
 	public RequestService() {
 		super("SendRequest");
 	}
 
 	@Override
 	protected void onHandleIntent(Intent intent) {
 		Bundle data = intent.getExtras();
 		
 		String response = null;
		StringBuffer buff = new StringBuffer();
 		URL url = null;
 		File file = new File(this.getExternalFilesDir(null), FILENAME);
 		try {
 			url = new URL(data.getString("urlpath"));
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		}
 		try{
 			URLConnection con = url.openConnection();
 			BufferedInputStream bis = new BufferedInputStream(con.getInputStream());
 			byte[] content = new byte[1024];
 			int read = 0;
			
 			while((read = bis.read(content)) != -1) {
 				response = new String(content,0,read);
 				buff.append(response);
 			}
 		} catch(Exception e) {
 			Log.e("URL IS MESSED UP!", "getResponse");
 		}
 		try{
 			File checkFile = new File(FILENAME);
 			checkFile.delete();
 		} catch(Exception e) {
 			Log.e("FILE", "No file, continue with creation!");
 		}
 		try{
 			FileOutputStream outputStream;
 			outputStream = new FileOutputStream(file);
			outputStream.write(buff.toString().getBytes());
 			outputStream.close();
 			result = Activity.RESULT_OK;
 		} catch(IOException e) {
 			Log.e("WRITE", FILENAME);
 		}
 		
 		Bundle extras = intent.getExtras();
 		if (extras != null) {
 			Messenger messenger = (Messenger) extras.get("MESSENGER");
 			Message msg = Message.obtain();
 			msg.arg1 = result;
 			msg.obj = file.getAbsolutePath();
			Log.i("DATA", buff.toString());
 			try {
 				messenger.send(msg);
 			} catch (android.os.RemoteException e1) {
 				Log.w(getClass().getName(), "Exception sending message", e1);
 			}
 		}
 	}
 	
 	@SuppressWarnings("resource")
 	public static String readFile(Context context, String filename, Boolean external) {
 		String zip = "";
 		try{
 			File file;
 			FileInputStream fileInput;
 			if(external) {
 				file = new File(context.getExternalFilesDir(null), filename);
 				fileInput = new FileInputStream(file);
 			} else {
 				file = new File(filename);
 				fileInput = context.openFileInput(filename);
 			}
 			BufferedInputStream bis = new BufferedInputStream(fileInput);
 			byte[] contentBytes = new byte[1024];
 			int readBytes = 0;
 			StringBuffer buff = new StringBuffer();
 			while((readBytes = bis.read(contentBytes)) != -1) {
 				zip = new String(contentBytes, 0, readBytes);
 				buff.append(zip);
 			}
 			zip = buff.toString();
 			fileInput.close();
 		} catch(FileNotFoundException e) {
 			Log.e("READ", "File not found: " + filename);
 		} catch(IOException e) {
 			Log.e("READ", "I/O Error: ");
 		}
 		return zip;
 	}
 }
