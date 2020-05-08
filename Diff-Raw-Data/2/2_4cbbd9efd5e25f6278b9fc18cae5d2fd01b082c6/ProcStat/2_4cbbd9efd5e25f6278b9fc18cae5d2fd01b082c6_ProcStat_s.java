 package com.eolwral.osmonitor;
 
 import java.io.*;
 import java.util.StringTokenizer;
 
 import android.util.Log;
 
 public class ProcStat {
 	private int lastTotal;
 	private int lastIdle;
 	private int currentTotal;
 	private int currentIdle;
 
 	public ProcStat() {
 		currentTotal = currentIdle = -1;
 		Update();
 	}
 
 	private static class BadHeadingException extends Exception {
 	};
 
 	public void Update() {
 		int total, idle;
 
 		try {
 			InputStream is = new FileInputStream("/proc/stat");
 			BufferedReader r = new BufferedReader(new InputStreamReader(is));
 			String line = r.readLine();
 			is.close();
 
 			StringTokenizer tok = new StringTokenizer(line);
 			String heading = tok.nextToken();
 			if (!heading.equals("cpu")) {
 				throw new BadHeadingException();
 			}
 
 			int user = Integer.parseInt(tok.nextToken());
 			int nice = Integer.parseInt(tok.nextToken());
 			int system = Integer.parseInt(tok.nextToken());
 			idle = Integer.parseInt(tok.nextToken());
 			int iowait = Integer.parseInt(tok.nextToken());
 			int irq = Integer.parseInt(tok.nextToken());
 			int softirq = Integer.parseInt(tok.nextToken());
 
 			total = user + nice + system + idle + iowait + irq + softirq;
 		} catch (IOException e) {
 			Log.e("osmonitor", "error reading /proc/stat", e);
 			return;
 		} catch (BadHeadingException e) {
 			Log.e("osmonitor", "bad heading in /proc/stat", e);
 			return;
 		} catch (NumberFormatException e) {
 			Log.e("osmonitor", "error parsing /proc/stat", e);
 			return;
 		}
 
 		lastTotal = currentTotal;
 		lastIdle = currentIdle;
 
 		currentTotal = total;
 		currentIdle = idle;
 	}
 
 	public int GetCPUUsageValue() {
 		if (lastTotal == -1) {  // need 2 Update()s before we have a value
 			return 0;
 		} else {
 			int deltaTotal = currentTotal - lastTotal;
 			int deltaIdle = currentIdle - lastIdle;
 			return (int)(100.0 * (deltaTotal - deltaIdle) / deltaTotal);
 		}
 	}
 	
 	public float GetCPUUsageValueFloat() {
 		if (lastTotal == -1) {  // need 2 Update()s before we have a value
 			return 0;
 		} else {
 			int deltaTotal = currentTotal - lastTotal;
 			int deltaIdle = currentIdle - lastIdle;
			return (deltaTotal - deltaIdle) / deltaTotal;
 		}
 	}
 }
