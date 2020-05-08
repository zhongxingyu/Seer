 package com.ultivox.uvoxplayer;
 
 import android.content.Context;
 import android.content.Intent;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 import android.util.Log;
 import android.util.Xml;
 import org.xmlpull.v1.XmlSerializer;
 
 import java.io.*;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.concurrent.TimeUnit;
 
 public class LogCatTask extends AsyncTask<String, String, String> implements BackgroundTask {
 
 	private static final String lineEnd = "\r\n";
 	private static final String twoHyphens = "--";
 	private static final String boundary = "***UMSDATA***";
 	final String LOG_TAG = "LogCatTaskLogs";
 	private String statusLine = "";
 	private Context tContext;
 	int bytesRead, bytesAvailable, bufferSize;
 	byte[] buffer;
 	int maxBufferSize = 1 * 1024 * 1024;
 	private static boolean isConnected = false;
 
 	HttpURLConnection connection = null;
 	DataOutputStream outputStream = null;
 	DataInputStream inputStream = null;
 	String serverResponse;
 	String serverMessage;
 
 	LogCatTask(Context context) {
 		tContext = context;
 	}
 
 	@Override
 	protected void onPreExecute() {
 		super.onPreExecute();
 		Log.d(LOG_TAG, "Start connection task");
 		ConnectivityManager cm = (ConnectivityManager) tContext
 				.getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo netInfoEth = cm
 				.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
 		NetworkInfo netInfoWifi = cm
 				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
 		if ((netInfoEth != null || netInfoWifi != null)
 				&& (netInfoEth.isConnected() || netInfoWifi.isConnected())) {
 			statusLine = "Connecting.....";
 			isConnected = true;
 		} else {
 			statusLine = "";
 			if (netInfoEth == null) {
 				Log.d(LOG_TAG, "Ethernet switched off");
 				statusLine = statusLine + "Ethernet switched off/";
 			}
 			if (netInfoWifi == null) {
 				Log.d(LOG_TAG, "WiFi switched off");
 				statusLine = statusLine + "WiFi switched off/";
 			}
 			if (!netInfoEth.isConnected()) {
 				Log.d(LOG_TAG, "Ethernet not connected");
 				statusLine = statusLine + "Ethernet not connected/";
 			}
 			if (!netInfoWifi.isConnected()) {
 				Log.d(LOG_TAG, "WiFi not connected");
 				statusLine = statusLine + "WiFi not connected/";
 			}
 		}
 	}
 
 	@Override
 	protected void onProgressUpdate(String... progress) {
 
 		Intent mesService = new Intent(UVoxPlayer.BROADCAST_SHOW);
 		mesService.putExtra(UVoxPlayer.PARAM_TEXT, progress[0]);
 		tContext.sendBroadcast(mesService);
 	}
 
 	@Override
 	protected String doInBackground(String... params) {
 
 		String urlServer = UVoxPlayer.LOGCAT_SERVER;
 		String pathLogFile = params[0];
 
 		Log.d(LOG_TAG, "Start doInBackground with: " + pathLogFile);
 		if (isConnected) {
 			publishProgress(statusLine);
 			try {
 				TimeUnit.SECONDS.sleep(2);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 			try {
 				URL url = new URL(urlServer);
 				connection = (HttpURLConnection) url.openConnection();
 				connection.setDoInput(true);
 				connection.setDoOutput(true);
 				connection.setUseCaches(false);
 				connection.setRequestMethod("POST");
 				connection.setRequestProperty("Connection", "Keep-Alive");
 				connection.setRequestProperty("Content-Type",
 						"multipart/form-data; boundary=" + boundary);
 
 				outputStream = new DataOutputStream(
 						connection.getOutputStream());
 				outputStream.writeBytes(twoHyphens + boundary + lineEnd);
 				outputStream
 						.writeBytes("Content-Disposition: form-data; name=\"xmldata\""
 								+ lineEnd);
 				outputStream.writeBytes(lineEnd);
 
 				XmlSerializer sXML = Xml.newSerializer();
 				sXML.setOutput(outputStream, "UTF-8");
 				sXML.startDocument("UTF-8", true);
 				sXML.startTag("", "logcatinfo");
 				sXML.attribute("", "data", android.text.format.DateFormat
 						.format("yyyy-MM-dd hh:mm:ss", new java.util.Date())
 						.toString());
 				sXML.startTag("", "player");
 				sXML.text(UVoxPlayer.UMS_NB);
 				sXML.endTag("", "player");
 				File fLog = new File(pathLogFile);
 				LogCatReader lcr = null;
 				try {
 					lcr = new LogCatReader(tContext, fLog);
 				} catch (FileNotFoundException e) {
 					e.printStackTrace();
 				}
 				LogEntry entry = lcr.nextEntry();
 				while (entry != null) {
 					sXML.startTag("", "event");
 					sXML.startTag("", "date");
 					Calendar cal = Calendar.getInstance();
 					cal.setTimeInMillis(entry.date);
 					Date date = cal.getTime();
 					SimpleDateFormat format = new SimpleDateFormat(
 							"yyyy-MM-dd HH:mm:ss.SSS");
 					sXML.text(format.format(date));
 					sXML.endTag("", "date");
 					sXML.startTag("", "PID");
 					sXML.text(String.format("%d", entry.pId));
 					sXML.endTag("", "PID");
 					sXML.startTag("", "TID");
 					sXML.text(String.format("%d", entry.tId));
 					sXML.endTag("", "TID");
 					sXML.startTag("", "level");
 					sXML.text(String.format("%s", entry.logLevel));
 					sXML.endTag("", "level");
 					sXML.startTag("", "appname");
 					sXML.text(entry.appName);
 					sXML.endTag("", "appname");
 					sXML.startTag("", "tag");
 					sXML.text(entry.tag);
 					sXML.endTag("", "tag");
 					sXML.startTag("", "message");
 					sXML.text(entry.message);
 					sXML.endTag("", "message");
 					sXML.endTag("", "event");
 					entry = lcr.nextEntry();
 				}
 				sXML.endTag("", "logcatinfo");
 				sXML.endDocument();
 				outputStream.writeBytes(lineEnd);
 				outputStream.writeBytes(twoHyphens + boundary + twoHyphens
 						+ lineEnd);
 				outputStream.writeBytes(lineEnd);
 				outputStream.flush();
 				outputStream.close();
 
				// и получаем ответ от сервера
 
 				BufferedReader in = new BufferedReader(new InputStreamReader(
 						connection.getInputStream()));
 
 				StringBuilder sb = new StringBuilder();
 				String line;
 				while ((line = in.readLine()) != null) {
 					sb.append(line + '\n');
 				}
 
 				int serverResponseCode = connection.getResponseCode();
 				serverResponse = connection.getResponseMessage();
 				serverMessage = sb.toString();
 				in.close();
 				sb = null;
 				Log.d(LOG_TAG, String.format("Server response %d --- %s",
 						serverResponseCode, serverMessage));
 				if ((fLog.exists()) && (serverResponseCode == 200))
 					fLog.delete();
 				return serverResponse;
 			} catch (MalformedURLException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			} catch (IOException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			} catch (Exception e1) {
 				Log.d(LOG_TAG, "Unexpected error, delete old logcat file: "+ pathLogFile);
 				File fLog = new File(pathLogFile);
 				fLog.delete();
 				e1.printStackTrace();
 //				throw new RuntimeException(e1);
 			} finally {
 				connection.disconnect();
 			}
 		} else {
 			return null;
 		}
 		return null;
 	}
 
 	@Override
 	protected void onPostExecute(String result) {
 
 		super.onPostExecute(result);
 		Intent mesServ = new Intent(UVoxPlayer.BROADCAST_ACT_SERVER);
 		if (result != null) {
 			mesServ.putExtra(UVoxPlayer.PARAM_RESULT, UVoxPlayer.SERVER_CONTINUE);
 			publishProgress("");
 		} else {
 			mesServ.putExtra(UVoxPlayer.PARAM_RESULT, UVoxPlayer.SERVER_ERROR);
 			publishProgress("Connecting problem. Please try again later.");
 		}
 		tContext.sendBroadcast(mesServ);
 		Log.d(LOG_TAG, "End. Result = " + result);
 		publishProgress("");
 	}
 
 	@Override
 	public String runTask(String file) {
 
 		this.execute(file);
 		return null;
 	}
 
 	@Override
 	public String getTaskName() {
 
 		return "DownloadTask";
 	}
 }
