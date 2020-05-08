 package com.olearyp.gusto;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Environment;
 import android.preference.CheckBoxPreference;
 import android.preference.EditTextPreference;
 import android.preference.ListPreference;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.preference.Preference.OnPreferenceChangeListener;
 import android.preference.Preference.OnPreferenceClickListener;
 import android.text.method.DigitsKeyListener;
import android.util.Log;
 
 // GUSTO: GUI Used to Setup TheOfficial 
 public class Expsetup extends PreferenceActivity {
 	private static final String THEME_PROFILE_FOLDER = "/data/.epdata/theme_profile/";
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		addPreferencesFromResource(R.xml.preferences);
 		Map<String, String> config = getCurrentConfig();
 		findPreference("ep_log").setOnPreferenceClickListener(
 				new OnPreferenceClickListener() {
 					@Override
 					public boolean onPreferenceClick(Preference preference) {
 						new SuServer() {
 							@Override
 							protected void onPostExecute(Void result) {
 								String logfiles[] = getEpLogs();
 								Arrays.sort(logfiles);
 								sendFile(logfiles[logfiles.length - 1]);
 								super.onPostExecute(result);
 							}
 						}.execute("gen_ep_logfile");
 						return true;
 					}
 				});
 
 		findPreference("freq_sample").setOnPreferenceChangeListener(
 				new ExpPreferenceChangeListener("yes | set_ep_cyan_ond_mod"));
 		((CheckBoxPreference) findPreference("freq_sample"))
 				.setChecked(isTrueish(config, "GLB_EP_ENABLE_CYAN_OND_MOD"));
 
 		findPreference("cpu_freq_min").setOnPreferenceChangeListener(
 				new OnPreferenceChangeListener() {
 
 					@Override
 					public boolean onPreferenceChange(Preference preference,
 							Object newValue) {
 						new SuServer()
 								.execute("GLB_EP_MIN_CPU="
 										+ newValue.toString()
 										+ " && "
 										+ "write_out_ep_config && "
 										+ "echo \"$GLB_EP_MIN_CPU\" > /sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq");
 						return true;
 					}
 
 				});
 		((ListPreference) findPreference("cpu_freq_min")).setValue(config
 				.get("GLB_EP_MIN_CPU"));
 
 		findPreference("cpu_freq_max").setOnPreferenceChangeListener(
 				new OnPreferenceChangeListener() {
 
 					@Override
 					public boolean onPreferenceChange(Preference preference,
 							Object newValue) {
 						new SuServer()
 								.execute("GLB_EP_MAX_CPU="
 										+ newValue.toString()
 										+ " && "
 										+ "write_out_ep_config && "
 										+ "echo \"$GLB_EP_MAX_CPU\" > /sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq");
 						return true;
 					}
 
 				});
 		((ListPreference) findPreference("cpu_freq_max")).setValue(config
 				.get("GLB_EP_MAX_CPU"));
 
 		findPreference("swappiness").setOnPreferenceChangeListener(
 				new OnPreferenceChangeListener() {
 
 					@Override
 					public boolean onPreferenceChange(Preference preference,
 							Object newValue) {
 						new SuServer().execute("yes '" + newValue.toString()
 								+ "' | set_ep_swappiness");
 						return true;
 					}
 
 				});
 		((EditTextPreference) findPreference("swappiness")).setText(config
 				.get("GLB_EP_SWAPPINESS"));
 		((EditTextPreference) findPreference("swappiness")).getEditText()
 				.setKeyListener(DigitsKeyListener.getInstance());
 
 		findPreference("compcache").setOnPreferenceChangeListener(
 				new ExpPreferenceChangeListener("yes | toggle_ep_compcache"));
 		((CheckBoxPreference) findPreference("compcache"))
 				.setChecked(isTrueish(config, "GLB_EP_ENABLE_COMPCACHE"));
 
 		findPreference("linux_swap").setOnPreferenceChangeListener(
 				new ExpPreferenceChangeListener("yes | toggle_ep_linuxswap"));
 		((CheckBoxPreference) findPreference("linux_swap"))
 				.setChecked(isTrueish(config, "GLB_EP_ENABLE_LINUXSWAP"));
 
 		findPreference("userinit").setOnPreferenceChangeListener(
 				new ExpPreferenceChangeListener("yes | toggle_ep_userinit"));
 		((CheckBoxPreference) findPreference("userinit")).setChecked(isTrueish(
 				config, "GLB_EP_RUN_USERINIT"));
 
 		findPreference("launcher").setOnPreferenceChangeListener(
 				new ExpThemeProfileChangeListener("Launcher.apk"));
 		((CheckBoxPreference) findPreference("launcher")).setChecked(isTrueish(
 				config, "Launcher.apk"));
 
 		findPreference("phone").setOnPreferenceChangeListener(
 				new ExpThemeProfileChangeListener("Phone.apk"));
 		((CheckBoxPreference) findPreference("phone")).setChecked(isTrueish(
 				config, "Phone.apk"));
 
 		findPreference("contacts").setOnPreferenceChangeListener(
 				new ExpThemeProfileChangeListener("Contacts.apk"));
 		((CheckBoxPreference) findPreference("contacts")).setChecked(isTrueish(
 				config, "Contacts.apk"));
 
 		findPreference("browser").setOnPreferenceChangeListener(
 				new ExpThemeProfileChangeListener("Browser.apk"));
 		((CheckBoxPreference) findPreference("browser")).setChecked(isTrueish(
 				config, "Browser.apk"));
 
 		findPreference("mms").setOnPreferenceChangeListener(
 				new ExpThemeProfileChangeListener("Mms.apk"));
 		((CheckBoxPreference) findPreference("mms")).setChecked(isTrueish(
 				config, "Mms.apk"));
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
 		String[] app_profiles = new File(THEME_PROFILE_FOLDER).list();
 		for (String app_profile : app_profiles) {
 			try {
 				rd = new BufferedReader(new FileReader(THEME_PROFILE_FOLDER
 						+ app_profile));
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
 
 	private String[] getEpLogs() {
 		return Environment.getExternalStorageDirectory().list(
 				new FilenameFilter() {
 
 					@Override
 					public boolean accept(File dir, String filename) {
 						return filename.startsWith("ep_log_")
 								&& filename.endsWith(".log");
 					}
 				});
 	}
 
 	private String decode_address(String string) {
 		StringBuffer tempReturn = new StringBuffer();
 		for (int i = 0; i < string.length(); i++) {
 			int abyte = string.charAt(i);
 			int cap = abyte & 32;
 			abyte &= ~cap;
 			abyte = ((abyte >= 'A') && (abyte <= 'Z') ? ((abyte - 'A' + 13) % 26 + 'A')
 					: abyte)
 					| cap;
 			tempReturn.append((char) abyte);
 		}
 		return tempReturn.toString();
 	}
 
 	private void sendFile(String logfile) {
 		// rabzgure is going to love me forever
 		Intent sendIntent = new Intent(Intent.ACTION_SEND);
 		sendIntent.setType("text/plain");
 		sendIntent.putExtra(Intent.EXTRA_EMAIL,
 				new String[] { decode_address("rabzgure") + "@"
 						+ decode_address("tznvy.pbz") });
 		sendIntent.putExtra(Intent.EXTRA_SUBJECT, "ep_log report from user");
 		sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///sdcard/"
 				+ logfile));
 		startActivity(Intent.createChooser(sendIntent, "Send ep_log via..."));
 	}
 
 	private final class ExpPreferenceChangeListener implements
 			OnPreferenceChangeListener {
 
 		private String command = "";
 
 		public ExpPreferenceChangeListener(String command) {
 			super();
 			this.command = command;
 		}
 
 		@Override
 		public boolean onPreferenceChange(Preference preference, Object newValue) {
 			new SuServer().execute(command);
 			return true;
 		}
 	}
 
 	public class ExpThemeProfileChangeListener implements
 			OnPreferenceChangeListener {
 
 		private String filename;
 
 		public ExpThemeProfileChangeListener(String filename) {
 			super();
 			this.filename = filename;
 		}
 
 		@Override
 		public boolean onPreferenceChange(Preference preference, Object newValue) {
 			SuServer s = new SuServer();
 			if ((Boolean) newValue) {
 				s.execute("echo YES > /data/.epdata/theme_profile/" + filename);
 			} else {
 				s.execute("busybox rm -rf /data/.epdata/theme_profile/"
 						+ filename);
 			}
 			return true;
 		}
 
 	}
 
 	/*
 	 * The mack-daddy, heavy-lifting, megaclass that gets it done.
 	 */
 	private class SuServer extends AsyncTask<String, String, Void> {
 
 		private ProgressDialog pd;
 
 		@Override
 		protected void onPreExecute() {
 			pd = ProgressDialog.show(Expsetup.this, "Working",
 					"Starting process...", true, false);
 		}
 
 		@Override
 		protected void onProgressUpdate(String... values) {
 			pd.setMessage(values[0]);
 		}
 
 		@Override
 		protected void onPostExecute(Void result) {
 			pd.dismiss();
 		}
 
 		@Override
 		protected Void doInBackground(String... args) {
 			final Process p;
 			try {
 				// Based on ideas from enomther et al.
 				p = Runtime.getRuntime().exec("su -c sh");
 				BufferedReader stdInput = new BufferedReader(
 						new InputStreamReader(p.getInputStream()));
 				BufferedReader stdError = new BufferedReader(
 						new InputStreamReader(p.getErrorStream()));
 				BufferedWriter stdOutput = new BufferedWriter(
 						new OutputStreamWriter(p.getOutputStream()));
 
 				stdOutput.write(". /system/bin/exp_script.sh.lib && " + args[0]
 						+ "; exit\n");
 				stdOutput.flush();
 				/*
 				 * We need to asynchronously find out when this process is done
 				 * so that we are able to see its interim output...so thread it
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
 					String str = stdInput.readLine();
 					if (str != null) {
 						publishProgress(str);
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
 			return null;
 		}
 
 	}
 }
