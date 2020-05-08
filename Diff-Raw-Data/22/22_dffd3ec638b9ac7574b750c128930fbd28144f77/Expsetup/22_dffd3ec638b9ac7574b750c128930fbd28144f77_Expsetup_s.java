 /* Copyright 2010 Patrick O'Leary.  All rights reserved.
  * The contents of this file are subject to the terms of the
  * Common Development and Distribution License, Version 1.0 only.
  * See the file CDDL.txt in this distribution or
  * http://opensource.org/licenses/cddl1.php for details.
  */
 
 package com.olearyp.gusto;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import android.app.AlertDialog;
 import android.app.Notification;
 import android.app.PendingIntent;
 import android.app.AlertDialog.Builder;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.DialogInterface.OnClickListener;
 import android.net.Uri;
 import android.os.Bundle;
 import android.preference.CheckBoxPreference;
 import android.preference.EditTextPreference;
 import android.preference.ListPreference;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.preference.Preference.OnPreferenceChangeListener;
 import android.preference.Preference.OnPreferenceClickListener;
 import android.util.Log;
 import android.view.KeyEvent;
 
 // GUSTO: GUI Used to Setup TheOfficial 
 public class Expsetup extends PreferenceActivity {
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		addPreferencesFromResource(R.xml.preferences);
 		Map<String, String> config = getCurrentConfig();
 
 		// QuickCommands menu
 		findPreference("reboot").setOnPreferenceClickListener(
 				new RebootPreferenceListener(R.string.reboot_alert_title,
 						R.string.reboot_msg, R.string.reboot));
 		findPreference("reboot_recovery")
 				.setOnPreferenceClickListener(
 						new RebootPreferenceListener(
 								R.string.reboot_alert_title,
 								R.string.reboot_recovery_msg,
 								R.string.reboot_recovery));
 		findPreference("reboot_bootloader").setOnPreferenceClickListener(
 				new RebootPreferenceListener(R.string.reboot_alert_title,
 						R.string.reboot_bootloader_msg,
 						R.string.reboot_bootload));
 		findPreference("reboot_poweroff").setOnPreferenceClickListener(
 				new RebootPreferenceListener(R.string.shutdown_alert_title,
 						R.string.shutdown_msg, R.string.shutdown));
 		findPreference("rwsystem").setOnPreferenceClickListener(
 				new ExpPreferenceListener(R.string.rwsystem,
 						"setting /system read-write"));
 		findPreference("rosystem").setOnPreferenceClickListener(
 				new ExpPreferenceListener(R.string.rosystem,
 						"setting /system read-only"));
 
 		// Generate and send ep_log
 		findPreference("ep_log").setOnPreferenceClickListener(
 				new EpLogListener());
 
 		// CPU options
 		findPreference("freq_sample").setOnPreferenceChangeListener(
 				new ExpPreferenceListener("yes | set_ep_cyan_ond_mod",
 						"setting frequency scaling"));
 		((CheckBoxPreference) findPreference("freq_sample"))
 				.setChecked(isTrueish(config, "GLB_EP_ENABLE_CYAN_OND_MOD"));
 
 		final List<String> freqs = Arrays.asList(getResources().getStringArray(
 				R.array.cpu_freqs_str));
 		final ListPreference minFreqPref = (ListPreference) findPreference("cpu_freq_min");
 		final ListPreference maxFreqPref = (ListPreference) findPreference("cpu_freq_max");
 		minFreqPref
 				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
 					@Override
 					public boolean onPreferenceChange(Preference preference,
 							Object newValue) {
 						sendCommand(
 								"GLB_EP_MIN_CPU="
 										+ newValue.toString()
 										+ " && "
 										+ "write_out_ep_config && "
 										+ "echo \"$GLB_EP_MIN_CPU\" > /sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq",
 								"setting minimum CPU frequency", "none");
 						String[] legalfreqs = freqs.subList(
 								freqs.indexOf(newValue), freqs.size()).toArray(
 								new String[0]);
 						maxFreqPref.setEntries(legalfreqs);
 						maxFreqPref.setEntryValues(legalfreqs);
 						return true;
 					}
 				});
 		maxFreqPref
 				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
 					@Override
 					public boolean onPreferenceChange(Preference preference,
 							Object newValue) {
 						sendCommand(
 								"GLB_EP_MAX_CPU="
 										+ newValue.toString()
 										+ " && "
 										+ "write_out_ep_config && "
 										+ "echo \"$GLB_EP_MAX_CPU\" > /sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq",
 								"setting maximum CPU frequency", "none");
 						String[] legalfreqs = freqs.subList(0,
 								freqs.indexOf(newValue) + 1).toArray(
 								new String[0]);
 						minFreqPref.setEntries(legalfreqs);
 						minFreqPref.setEntryValues(legalfreqs);
 						return true;
 					}
 
 				});
 
 		String[] maxfreqs = freqs.subList(
 				freqs.indexOf(config.get("GLB_EP_MIN_CPU")), freqs.size())
 				.toArray(new String[0]);
 		maxFreqPref.setEntries(maxfreqs);
 		maxFreqPref.setEntryValues(maxfreqs);
 
 		String[] minfreqs = freqs.subList(0,
 				freqs.indexOf(config.get("GLB_EP_MAX_CPU")) + 1).toArray(
 				new String[0]);
 		minFreqPref.setEntries(minfreqs);
 		minFreqPref.setEntryValues(minfreqs);
 
 		maxFreqPref.setValue(config.get("GLB_EP_MAX_CPU"));
 		minFreqPref.setValue(config.get("GLB_EP_MIN_CPU"));
 
 		// Swappiness
 		findPreference("swappiness").setOnPreferenceChangeListener(
 				new OnPreferenceChangeListener() {
 					@Override
 					public boolean onPreferenceChange(Preference preference,
 							Object newValue) {
 						sendCommand("yes '" + newValue.toString()
 								+ "' | set_ep_swappiness",
 								"setting swappiness", "none");
 						return true;
 					}
 				});
 		((EditTextPreference) findPreference("swappiness")).setText(config
 				.get("GLB_EP_SWAPPINESS"));
 		((EditTextPreference) findPreference("swappiness")).getEditText()
 				.setKeyListener(new SwappinessKeyListener());
 
 		// Compcache
 		findPreference("compcache").setOnPreferenceChangeListener(
 				new ExpPreferenceListener("yes | toggle_ep_compcache",
 						"setting compcache", true));
 		((CheckBoxPreference) findPreference("compcache"))
 				.setChecked(isTrueish(config, "GLB_EP_ENABLE_COMPCACHE"));
 
 		// Linux swap
 		findPreference("linux_swap").setOnPreferenceChangeListener(
 				new ExpPreferenceListener("yes | toggle_ep_linuxswap",
 						"setting Linux-swap"));
 		((CheckBoxPreference) findPreference("linux_swap"))
 				.setChecked(isTrueish(config, "GLB_EP_ENABLE_LINUXSWAP"));
 
 		// userinit.sh
 		findPreference("userinit").setOnPreferenceChangeListener(
 				new ExpPreferenceListener("yes | toggle_ep_userinit",
 						"setting userinit"));
 		((CheckBoxPreference) findPreference("userinit")).setChecked(isTrueish(
 				config, "GLB_EP_RUN_USERINIT"));
 
 		// Remove odex on boot
 		findPreference("odex").setOnPreferenceChangeListener(
 				new ExpPreferenceListener("yes | toggle_ep_odex_boot_removal",
 						"setting ODEX removal"));
 		((CheckBoxPreference) findPreference("odex")).setChecked(isTrueish(
 				config, "GLB_EP_ODEX_BOOT_REMOVAL"));
 
 		// Odex now
 		findPreference("reodex").setOnPreferenceClickListener(
 				new ExpPreferenceListener("yes | odex_ep_data_apps",
 						"re-ODEXing apps"));
 
 		// Set pid priorities
 		findPreference("pid_prioritize").setOnPreferenceChangeListener(
 				new ExpPreferenceListener("yes | toggle_ep_pid_prioritizer",
 						"setting PID prioritizer"));
 		((CheckBoxPreference) findPreference("pid_prioritize"))
 				.setChecked(isTrueish(config, "GLB_EP_PID_PRIORITIZE"));
 
 		// Theme profile settings
 		if (config.get("GLB_EP_VERSION_EPDATA").contains("-TMO")) {
 			findPreference("launcher").setEnabled(false);
 			findPreference("phone").setEnabled(false);
 			findPreference("contacts").setEnabled(false);
 		}
 
 		findPreference("launcher").setOnPreferenceChangeListener(
 				new ExpThemeProfileChangeListener("Launcher.apk",
 						"changing Launcher"));
 		((CheckBoxPreference) findPreference("launcher")).setChecked(isTrueish(
 				config, "Launcher.apk"));
 
 		findPreference("phone")
 				.setOnPreferenceChangeListener(
 						new ExpThemeProfileChangeListener("Phone.apk",
 								"changing Phone"));
 		((CheckBoxPreference) findPreference("phone")).setChecked(isTrueish(
 				config, "Phone.apk"));
 
 		findPreference("contacts").setOnPreferenceChangeListener(
 				new ExpThemeProfileChangeListener("Contacts.apk",
 						"changing Contacts"));
 		((CheckBoxPreference) findPreference("contacts")).setChecked(isTrueish(
 				config, "Contacts.apk"));
 
 		findPreference("browser").setOnPreferenceChangeListener(
 				new ExpThemeProfileChangeListener("Browser.apk",
 						"changing the Browser"));
 		((CheckBoxPreference) findPreference("browser")).setChecked(isTrueish(
 				config, "Browser.apk"));
 
 		findPreference("mms").setOnPreferenceChangeListener(
 				new ExpThemeProfileChangeListener("Mms.apk", "changing MMS"));
 		((CheckBoxPreference) findPreference("mms")).setChecked(isTrueish(
 				config, "Mms.apk"));
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		// TODO Auto-generated method stub
 		if (keyCode == KeyEvent.KEYCODE_BACK) {
 			String reboot_type = getSharedPreferences("serverState",
 					Context.MODE_PRIVATE).getString("serverState", "none");
 			if (reboot_type
 					.equals(getString(R.string.reboot_recovery_required))) {
 				rebootDialog(R.string.reboot_alert_title,
 						R.string.reboot_reflash_msg, R.string.reboot_recovery,
 						true).show();
 				return true;
 			} else if (reboot_type.equals(getString(R.string.reboot_required))) {
 				rebootDialog(R.string.reboot_alert_title,
 						R.string.reboot_request_msg, R.string.reboot, true)
 						.show();
 				return true;
 			}
 		}
 		return super.onKeyDown(keyCode, event);
 	}
 
 	private Builder rebootDialog(int title, int message, final int reboot_cmd,
 			boolean close_if_no_reboot) {
 		return new AlertDialog.Builder(Expsetup.this).setTitle(title)
 				.setMessage(getString(message)).setPositiveButton(R.string.yes,
 						new OnClickListener() {
 							@Override
 							public void onClick(DialogInterface dialog,
 									int which) {
 								sendCommandById(reboot_cmd, "none");
 							}
 						}).setNegativeButton(R.string.no,
 						close_if_no_reboot ? new OnClickListener() {
 							@Override
 							public void onClick(DialogInterface dialog,
 									int which) {
 								Expsetup.this.finish();
 							}
 						} : null);
 	}
 
 	protected void sendCommandById(int cmd_res, String state) {
 		Intent runCmd = new Intent("com.olearyp.gusto.SUEXEC");
 		runCmd.setData(
 				Uri.fromParts("commandid", Integer.toString(cmd_res), ""))
 				.putExtra("com.olearyp.gusto.STATE", state);
 		Log.v("GUSTO", "Sending reboot Command");
 		Expsetup.this.startService(runCmd);
 	}
 
 	private boolean isTrueish(Map<String, String> config, String key) {
 		return (config.get(key) != null && config.get(key).equals("YES"));
 	}
 
 	private Map<String, String> getCurrentConfig() {
 		HashMap<String, String> config = new HashMap<String, String>();
 		// Read in the config file & parse it
 		BufferedReader rd;
 		try {
 			rd = new BufferedReader(new FileReader("/system/bin/exp.config"));
 			String line = rd.readLine();
 			while (line != null) {
 				String[] parts = line.split("=");
 				if (parts.length == 2) {
 					config.put(parts[0], parts[1].substring(1, parts[1]
 							.length() - 1));
 				}
 				line = rd.readLine();
 			}
 			rd.close();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		// Then check the theme profile settings
 		String[] app_profiles = new File(
 				getString(R.string.theme_profile_folder)).list();
 		for (String app_profile : app_profiles) {
 			try {
 				rd = new BufferedReader(new FileReader(
 						getString(R.string.theme_profile_folder) + app_profile));
 				config.put(app_profile, rd.readLine().trim());
 				rd.close();
 			} catch (FileNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 		return config;
 	}
 
 	private void sendCommand(String command, String description, String state) {
 		Intent runCmd = new Intent("com.olearyp.gusto.SUEXEC");
 		runCmd.setData(Uri.fromParts("command", command, "")).putExtra(
 				"com.olearyp.gusto.STATE", state);
 		final Notification note = new Notification(R.drawable.icon, description
 				.substring(0, 1).toUpperCase()
 				+ description.substring(1) + "...", System.currentTimeMillis());
 		note.setLatestEventInfo(Expsetup.this, getString(R.string.app_name),
 				getString(R.string.app_name) + " is " + description + "...",
 				PendingIntent.getBroadcast(Expsetup.this, 0, null, 0));
 		runCmd.putExtra("com.olearyp.gusto.RUN_NOTIFICATION", note);
 		startService(runCmd);
 	}
 
 	/*
 	 * Listener for 'generate ep_log' button
 	 */
 	private final class EpLogListener implements OnPreferenceClickListener {
 		/*
 		 * Listener for Positive button of ep_log dialog
 		 */
 		private final class EpLogDialogListener implements OnClickListener {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				Intent runCmd = new Intent("com.olearyp.gusto.SUEXEC");
 				runCmd.setData(Uri.fromParts("command", "gen_ep_logfile", ""));
 				final PendingIntent post_ex_intent = PendingIntent
 						.getBroadcast(Expsetup.this, 0, new Intent(
 								"com.olearyp.gusto.MAIL_LOG"), 0);
 				runCmd.putExtra("com.olearyp.gusto.POST_EX_INTENT",
 						post_ex_intent);
 				final Notification note = new Notification(R.drawable.icon,
 						"Generating log...", System.currentTimeMillis());
 				note.setLatestEventInfo(Expsetup.this,
 						getString(R.string.app_name),
 						getString(R.string.app_name)
 								+ " is generating an ep_log...", PendingIntent
 								.getBroadcast(Expsetup.this, 0, null, 0));
 				runCmd.putExtra("com.olearyp.gusto.RUN_NOTIFICATION", note);
 				startService(runCmd);
 			}
 		}
 
 		@Override
 		public boolean onPreferenceClick(Preference preference) {
 			new AlertDialog.Builder(Expsetup.this).setTitle(
 					R.string.ep_log_alert_title).setMessage(
 					R.string.ep_log_confirm_msg).setPositiveButton(
 					R.string.create_log, new EpLogDialogListener())
 					.setNegativeButton(R.string.return_to_menu, null).show();
 			return true;
 		}
 	}
 
 	/* Generic listener which executes a command as root */
 	private final class ExpPreferenceListener implements
 			OnPreferenceChangeListener, OnPreferenceClickListener {
 
 		private String command = "";
 		private String description = "";
 		private boolean requires_reboot = false;
 		private int commandid = 0;
 
 		public ExpPreferenceListener(int commandid, String description) {
 			super();
 			this.commandid = commandid;
 			this.description = description;
 		}
 
 		public ExpPreferenceListener(String command, String description) {
 			this(command, description, false);
 		}
 
 		public ExpPreferenceListener(String command, String description,
 				boolean requires_reboot) {
 			super();
 			this.command = command;
 			this.description = description;
 			this.requires_reboot = requires_reboot;
 		}
 
 		@Override
 		public boolean onPreferenceChange(Preference preference, Object newValue) {
 			return runCommand();
 		}
 
 		@Override
 		public boolean onPreferenceClick(Preference preference) {
 			return runCommand();
 		}
 
 		private boolean runCommand() {
 			// new SuProcess(Expsetup.this).execute(command);
 			// if (requires_reboot) {
 			// system_needs_reboot = true;
 			// }
 			if (commandid > 0) {
 				sendCommandById(commandid, requires_reboot ? "reboot-required"
 						: "none");
 			} else {
 				sendCommand(command, description,
 						requires_reboot ? "reboot-required" : "none");
 			}
 			return true;
 		}
 	}
 
 	private class RebootPreferenceListener implements OnPreferenceClickListener {
 		int message;
 		int command;
 		private int title;
 
 		public RebootPreferenceListener(int title, int message, int command) {
 			this.title = title;
 			this.message = message;
 			this.command = command;
 		}
 
 		@Override
 		public boolean onPreferenceClick(Preference preference) {
 			rebootDialog(title, message, command, false).show();
 			return false;
 		}
 	}
 
 	/*
 	 * Listener for theme profile configuration, which is more complex than the
 	 * other settings (since there's no function in the library which does this
 	 * for me.)
 	 */
 	public class ExpThemeProfileChangeListener implements
 			OnPreferenceChangeListener {
 
		private String filename;
		private String description;
 
 		public ExpThemeProfileChangeListener(String filename, String description) {
 			super();
 			this.filename = filename;
 		}
 
 		@Override
 		public boolean onPreferenceChange(Preference preference, Object newValue) {
 			if ((Boolean) newValue) {
 				sendCommand("echo YES > /data/.epdata/theme_profile/"
 						+ filename, description, "reboot-recovery-required");
 			} else {
 				sendCommand("busybox rm -rf /data/.epdata/theme_profile/"
 						+ filename, description, "reboot-recovery-required");
 			}
 			return true;
 		}
 
 	}
 }
