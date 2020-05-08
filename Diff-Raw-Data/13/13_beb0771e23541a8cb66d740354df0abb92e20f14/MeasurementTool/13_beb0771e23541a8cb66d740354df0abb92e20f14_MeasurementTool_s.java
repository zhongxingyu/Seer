 package be.ugent.ods.osgi.tests.measure;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 
 import android.app.ActivityManager;
 import android.content.Context;
 import android.content.pm.ApplicationInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.net.TrafficStats;
 import android.net.Uri;
 import android.os.Build;
 import android.util.Log;
 
 public class MeasurementTool implements MeasurementInterface {
 	private int iterations;
 	private long start;
 	private ApplicationInfo appinfo;
 	private boolean started;
 	private long dataR;
 	private long dataT;
 	private long packetsT;
 	private long packetsR;
 	private int maxcpu;
 	private Context context;
 	private int memArb;
 	private int datapoints;
 	private int cumCpu;
 	private int cumMemArb;
 	private int maxMemArb;
 	private Thread thread;
 
 	List<BasicNameValuePair> results = new ArrayList<BasicNameValuePair>();
 	private String type;
 
 	// todo placeholder for git
 
 	public MeasurementTool() {
 	
 		results = new ArrayList<BasicNameValuePair>();
 	}
 
 	public boolean startMeasuring(int iterations, Context context, String type) {
 		this.iterations = iterations;
 		results = new ArrayList<BasicNameValuePair>();
 		this.start = System.currentTimeMillis();
 		try {
 			appinfo = context.getPackageManager().getApplicationInfo(
 					context.getPackageName(), PackageManager.GET_META_DATA);
 		} catch (NameNotFoundException e) {
 			return false;
 		}
 		// only get data for current app, note that uid from app isn't perse
 		// unique but it will provide a minimum of filtering required
 		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
 			this.dataR = TrafficStats.getUidRxBytes(appinfo.uid);
 			this.packetsR = TrafficStats.getUidRxPackets(appinfo.uid);
 			this.dataT = TrafficStats.getUidTxBytes(appinfo.uid);
 			this.packetsT = TrafficStats.getUidTxPackets(appinfo.uid);
 		}
 		this.context = context;
 		this.type = type;
 		this.started = true;
 		this.thread = new Thread(new memRunnable());
 		this.thread.start();
 		this.maxMemArb = 0;
 		this.maxcpu = 0;
 		this.datapoints = 0;
 		this.cumCpu = 0;
 		this.cumMemArb = 0;
 		ActivityManager am = (ActivityManager) context
 				.getSystemService(Context.ACTIVITY_SERVICE);
 		android.os.Debug.MemoryInfo[] mem = am
 				.getProcessMemoryInfo(new int[] { android.os.Process.myPid() });
 		memArb = mem[0].getTotalPrivateDirty();
 		return true;
 	}
 
 	public void stopMeasuring() {
 		if (!started) {
 			throw new IllegalStateException("please fist start measuring");
 		}
 		long duration = System.currentTimeMillis() - start;
 		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
 			this.dataR = TrafficStats.getUidRxBytes(appinfo.uid) - this.dataR;
 			this.packetsR = TrafficStats.getUidRxPackets(appinfo.uid)
 				- this.packetsR;
 			this.dataT = TrafficStats.getUidTxBytes(appinfo.uid) - this.dataT;
 			this.packetsT = TrafficStats.getUidTxPackets(appinfo.uid)
 				- this.packetsT;
 		}
 		if (datapoints == 0) {
 			// avoid divide by zero in debug
 			datapoints = 1;
 		}
 		int avgCpu = cumCpu / datapoints;
 		int avgMemArb = cumMemArb / datapoints;
 		results.add(new BasicNameValuePair("entry.0.single", "" + avgCpu));
 		results.add(new BasicNameValuePair("entry.1.single", "" + avgMemArb));
 		results.add(new BasicNameValuePair("entry.10.single", "" + maxMemArb));
 		results.add(new BasicNameValuePair("entry.11.single", "" + maxcpu));
 		results.add(new BasicNameValuePair("entry.2.single", "" + duration));
 		results.add(new BasicNameValuePair("entry.3.single", "" + iterations));
 		results.add(new BasicNameValuePair("entry.4.single", "odsrpc1"));
 		results.add(new BasicNameValuePair("entry.6.single", "" + type));
 		results.add(new BasicNameValuePair("entry.7.single", "" + packetsT));
 		results.add(new BasicNameValuePair("entry.5.single", "" + packetsR));
 		results.add(new BasicNameValuePair("entry.8.single", "" + dataT));
 		results.add(new BasicNameValuePair("entry.9.single", "" + dataR));
 		results.add(new BasicNameValuePair("entry.12.single", "" + android.os.Build.MODEL));
 		results.add(new BasicNameValuePair("entry.13.single", "" + Build.VERSION.SDK_INT));
 		send();
 		started = false;
 
 	}
 
 	public class memRunnable implements Runnable {
 		public void run() {
 			// do measuring
 			while (started) {
 				ActivityManager am = (ActivityManager) context
 						.getSystemService(Context.ACTIVITY_SERVICE);
 				android.os.Debug.MemoryInfo[] mem = am
 						.getProcessMemoryInfo(new int[] { android.os.Process
 								.myPid() });
 				cumMemArb += (mem[0].getTotalPrivateDirty() - memArb);
 				if (mem[0].getTotalPrivateDirty() - memArb > maxMemArb) {
 					maxMemArb = mem[0].getTotalPrivateDirty() - memArb;
 
 				}
 				BufferedReader in = null;
 
 				try {
 					Process process = null;
 					process = Runtime.getRuntime().exec("top -n 1 -d 1");
 
 					in = new BufferedReader(new InputStreamReader(
 							process.getInputStream()));
 
 					String line = "";
 
 					while ((line = in.readLine()) != null
 							&& !line.contains("be.ugent.ods.osgi")) {
 
 					}
 					String cpuString = line.substring(line.indexOf("%") -3, line.indexOf("%"));
 					cpuString= cpuString.replaceAll("[A-Za-z ]*", "");
 					int cpu = Integer.parseInt(cpuString);
 					cumCpu += cpu;
 					if (maxcpu < cpu) {
 						maxcpu = cpu;
 					}
 					datapoints++;
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} finally {
 					if (in != null) {
 						try {
 							in.close();
 						} catch (IOException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 					}
 				}
 				try {
 					Thread.sleep(100);
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	private void send() {
 
 		// values observed in the GoogleDocs original html form
 		results.add(new BasicNameValuePair("pageNumber", "0"));
 		results.add(new BasicNameValuePair("backupCache", ""));
 		results.add(new BasicNameValuePair("submit", "Insturen"));
 		HttpClient client = new DefaultHttpClient();
 		HttpPost post = new HttpPost(
 				"https://spreadsheets.google.com/spreadsheet/formResponse?hl=en_US&formkey=dHduWVdnOEdBNF80dkNPcDBPY1NsZmc6MQ");
 
 		
 
 		try {
 			post.setEntity(new UrlEncodedFormEntity(results));
 		} catch (UnsupportedEncodingException e) {
 			// Auto-generated catch block
 			Log.e("ODS", "An error has occurred", e);
 		}
 		try {
 			client.execute(post);
 		} catch (ClientProtocolException e) {
 			// Auto-generated catch block
 			Log.e("ODS", "client protocol exception", e);
 		} catch (IOException e) {
 			// Auto-generated catch block
 			Log.e("ODS", "io exception", e);
 		}
 	}
 
 }
