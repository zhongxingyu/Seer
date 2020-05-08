 /* Copyright 2010 Patrick O'Leary.  All rights reserved.
  * The contents of this file are subject to the terms of the
  * Common Development and Distribution License, Version 1.0 only.
  * See the file CDDL.txt in this distribution or
  * http://opensource.org/licenses/cddl1.php for details.
  */
 
 package com.olearyp.gusto;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 
 import android.app.IntentService;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.PendingIntent.CanceledException;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.Uri;
 import android.util.Log;
 
 public class SuServer extends IntentService {
 
 	private static final int REBOOT_NOTIFICATION = 0x0043B007;
 
 	private static final int STATUS_NOTIFICATION = 0x0057A705;
 
 	private NotificationManager nm = null;
 
 	private SharedPreferences settings = null;
 
 	public SuServer() {
 		super("SuServer");
 	}
 
 	@Override
 	public void onStart(Intent intent, int startId) {
 		String cmdString = Uri.parse(intent.toUri(0)).getSchemeSpecificPart();
 		if (Uri.parse(intent.toUri(0)).getScheme().equals("commandid")) {
 			cmdString = getString(Integer.parseInt(cmdString));
 		}
 		Log.v("GUSTO", "SuServer has received request for command '"
 				+ cmdString + "'.");
 		super.onStart(intent, startId);
 	}
 
 	@Override
 	public void onCreate() {
 		nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 		settings = getSharedPreferences("serverState", MODE_PRIVATE);
 
 		super.onCreate();
 	}
 
 	private String getServerState() {
 		return settings.getString("serverState", "none");
 	}
 
 	@Override
 	public void onDestroy() {
 		// We're done, kill the "running" notification
 		nm.cancel(STATUS_NOTIFICATION);
 		// Create or modify reboot notification, if needed
 		if (getServerState().equals(
 				getString(R.string.reboot_recovery_required))) {
 			Intent intent = new Intent("com.olearyp.gusto.SUEXEC").setData(Uri
 					.fromParts("commandid", Integer
 							.toString(R.string.reboot_recovery), ""));
 			PendingIntent contentIntent = PendingIntent.getService(this, 0,
 					intent, 0);
 
 			Notification note = new Notification(R.drawable.status_reboot,
 					getString(R.string.reboot_recovery_required_msg), System
 							.currentTimeMillis());
 			note
 					.setLatestEventInfo(this, "GUSTO reboot request",
 							getString(R.string.reboot_recovery_doit_msg),
 							contentIntent);
 			note.deleteIntent = PendingIntent.getBroadcast(this, 0, new Intent(
 					"com.olearyp.gusto.RESET_SERVER_STATE"), 0);
 			nm.notify(REBOOT_NOTIFICATION, note);
 		} else if (getServerState().equals(getString(R.string.reboot_required))) {
 			Intent intent = new Intent("com.olearyp.gusto.SUEXEC").setData(Uri
 					.fromParts("commandid", Integer.toString(R.string.reboot),
 							""));
 			PendingIntent contentIntent = PendingIntent.getService(this, 0,
 					intent, 0);
 
 			Notification note = new Notification(R.drawable.status_reboot,
 					getString(R.string.reboot_required_msg), System
 							.currentTimeMillis());
 			note.setLatestEventInfo(this, "GUSTO reboot request",
 					getString(R.string.reboot_doit_msg), contentIntent);
 			note.deleteIntent = PendingIntent.getBroadcast(this, 0, new Intent(
 					"com.olearyp.gusto.RESET_SERVER_STATE"), 0);
 			nm.notify(REBOOT_NOTIFICATION, note);
 		}
 		super.onDestroy();
 	}
 
 	@Override
 	protected void onHandleIntent(Intent intent) {
 		String cmdString = Uri.parse(intent.toUri(0)).getSchemeSpecificPart();
 		if (Uri.parse(intent.toUri(0)).getScheme().equals("commandid")) {
 			cmdString = getString(Integer.parseInt(cmdString));
 		}
 		final String state = intent.getStringExtra("com.olearyp.gusto.STATE");
 		final PendingIntent postExecuteIntent = (PendingIntent) intent
 				.getParcelableExtra("com.olearyp.gusto.POST_EX_INTENT");
 		Notification note = (Notification) intent
 				.getParcelableExtra("com.olearyp.gusto.RUN_NOTIFICATION");
 
 		Log.v("GUSTO", "SuServer is handling command '" + cmdString + "'.");
 
 		if (note == null) {
 			note = new Notification(R.drawable.icon, "Processing setting...",
 					System.currentTimeMillis());
 			note
 					.setLatestEventInfo(this, getString(R.string.app_name),
 							getString(R.string.app_name)
 									+ " is processing settings...",
 							PendingIntent.getBroadcast(this, 0, null, 0));
 		}
 		nm.notify(STATUS_NOTIFICATION, note);
 
 		final Process p;
 		try {
 			// Based on ideas from enomther et al.
 			p = Runtime.getRuntime().exec("su -c sh");
 			BufferedReader stdInput = new BufferedReader(new InputStreamReader(
 					p.getInputStream()));
 			BufferedReader stdError = new BufferedReader(new InputStreamReader(
 					p.getErrorStream()));
 			BufferedWriter stdOutput = new BufferedWriter(
 					new OutputStreamWriter(p.getOutputStream()));
 
 			stdOutput
 					.write(". /system/bin/exp_script.sh.lib && read_in_ep_config && "
 							+ cmdString + "; exit\n");
 			stdOutput.flush();
 			/*
 			 * We need to asynchronously find out when this process is done so
 			 * that we are able to see its interim output...so thread it
 			 */
 			Thread t = new Thread() {
 				public void run() {
 					try {
 						p.waitFor();
 					} catch (InterruptedException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 			};
 			t.start();
 
 			// Poor man's select()
 			while (t.isAlive()) {
 				String status = stdInput.readLine();
 				if (status != null) {
 					// publishProgress(status);
 				}
 				Thread.sleep(20);
 			}
 
 			stdInput.close();
 			stdError.close();
 			stdOutput.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		if (postExecuteIntent != null) {
 			try {
 				postExecuteIntent.send();
 			} catch (CanceledException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 		if (state != null) {
 			setServerState(state);
 		}
 	}
 
 	private final int getIndex(String specificValue) {
 		String[] stateIndex = getResources().getStringArray(
 				R.array.server_states);
 		for (int i = 0; i < stateIndex.length; i++) {
 			if (stateIndex[i].equals(specificValue)) {
 				return i;
 			}
 		}
 		return -1;
 	}
 
 	private void setServerState(String state) {
 		if (getIndex(state) > getIndex(getServerState())) {
 			settings.edit().putString("serverState", state).commit();
 		}
 	}
 
 }
