 package org.herrlado.geofonts;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.LineNumberReader;
 import java.io.OutputStream;
 import java.io.StringReader;
 import java.util.HashMap;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.io.IOUtils;
 import org.herrlado.geofonts.ShellCommand.CommandResult;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Environment;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ProgressBar;
 import android.widget.Toast;
 
 public class Installer extends Activity implements OnClickListener,
 		DialogInterface.OnClickListener {
 
 	public static final String TAG = "GeorgianFontsInstaller";
 
 	public static final String DroidSansBold = "DroidSans-Bold.ttf";
 
 	public static final String DroidSans = "DroidSans.ttf";
 
 	public static final String DroidSansMono = "DroidSansMono.ttf";
 
 	public static final String DroidSerifBold = "DroidSerif-Bold.ttf";
 
 	public static final String DroidSerifBoldItalic = "DroidSerif-BoldItalic.ttf";
 
 	public static final String DroidSerifItalic = "DroidSerif-Italic.ttf";
 
 	public static final String DroidSerifRegular = "DroidSerif-Regular.ttf";
 
 	public static final HashMap<String, String> MD5 = new HashMap<String, String>();
 
 	public static final String DESTINATION = "/system/fonts";
 
 	public static final Pattern MOUNT_SYSTEM_PATTERN = Pattern
 			.compile("([^\\s]*)\\s.*(/system)\\s.*");
 
 	static {
 		MD5.put(DroidSansBold, "bd3163f7db07b82158a310efd77902ce");
 		MD5.put(DroidSans, "741daf0f3e7c8320a752116eefe2f0a0");
 		MD5.put(DroidSansMono, "0294cd8fb13d6504622afd3e003033fa");
 		MD5.put(DroidSerifBold, "9723725c63b2a2a016ad7831042b4b60");
 		MD5.put(DroidSerifBoldItalic, "04154e4911adba25105d3d01276359e0");
 		MD5.put(DroidSerifItalic, "8dccfdce11daa12537fde6cd16dc5bf2");
 		MD5.put(DroidSerifRegular, "ad860d4a21a857bdee918d902829990a");
 	}
 
 	public boolean backup() {
 		File backupFolder = new File(getBackupFolder());
 		backupFolder.mkdir();
 		if (backupFolder.isDirectory() == false
 				|| backupFolder.canWrite() == false) {
 			Log.w(TAG,
 					backupFolder
 							+ " either can not be created or it exists and is not a directory or not writable!");
 			return false;
 		}
 		FileInputStream fis = null;
 		FileOutputStream fos = null;
 		try {
 			for (String font : MD5.keySet()) {
 				fis = new FileInputStream(new File("/system/fonts/" + font));
 				fos = new FileOutputStream(new File(backupFolder + "/" + font));
 				IOUtils.copy(fis, fos);
 				IOUtils.closeQuietly(fis);
 				IOUtils.closeQuietly(fos);
 			}
 		} catch (Exception ex) {
 			makeToast(ex.getMessage());
 			return false;
 		} finally {
 			IOUtils.closeQuietly(fis);
 			IOUtils.closeQuietly(fos);
 		}
 
 		Log.d(TAG, "Fonts were copied into the " + backupFolder);
 		return true;
 	}
 
 	private void makeToast(String text) {
 		Toast.makeText(this, text, Toast.LENGTH_LONG).show();
 	}
 
 	public boolean restore() {
 
 		return false;
 	}
 
 	// TODO check this!izav
 	// boolean mExternalStorageAvailable = false;
 	// boolean mExternalStorageWriteable = false;
 	// String state = Environment.getExternalStorageState();
 	//
 	// if (Environment.MEDIA_MOUNTED.equals(state)) {
 	// // We can read and write the media
 	// mExternalStorageAvailable = mExternalStorageWriteable = true;
 	// } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
 	// // We can only read the media
 	// mExternalStorageAvailable = true;
 	// mExternalStorageWriteable = false;
 	// } else {
 	// // Something else is wrong. It may be one of many other states, but all
 	// we need
 	// // to know is we can neither read nor write
 	// mExternalStorageAvailable = mExternalStorageWriteable = false;
 	// }
 	private String getBackupFolder() {
 		return Environment.getExternalStorageDirectory() + "/"
 				+ getApplicationContext().getPackageName();
 	}
 
 	private String getTmpFontFolder() {
 		return getBackupFolder() + "/tmp";
 	}
 
 	public boolean extractFonts() {
 		String folder = getTmpFontFolder() + "/";
 		new File(folder).mkdir();
 		InputStream is = null;
 		OutputStream os = null;
 		try {
 			for (String key : MD5.keySet()) {
 				is = getApplicationContext().getAssets().open(key);
 				os = new FileOutputStream(new File(folder + key));
 				IOUtils.copy(is, os);
 				IOUtils.closeQuietly(is);
 				IOUtils.closeQuietly(os);
 			}
 		} catch (Exception ex) {
 			Log.w(TAG, "Can not extractFonts to " + getTmpFontFolder(), ex);
 			IOUtils.closeQuietly(is);
 			IOUtils.closeQuietly(os);
 			return false;
 		}
 		return true;
 	}
 
 	public boolean installFonts() {
 		StringBuilder sb = new StringBuilder();
 		String system = null;
 		try {
 			ShellCommand sc = new ShellCommand();
 
 			if (extractFonts() == false) {
 				Log.w(TAG, "Can not extract Georgian fonts from apk!");
 				return false;
 			}
 
 			system = getSystemPartion();
 
 			if (system == null) {
 				Log.w(TAG,
 						"Can not find out which partion is mounted on /system");
 				return false;
 			}
 
 			String cc = "mount -o remount,rw  " + system + " /system";
 			sb.append(cc).append("\n");
 
 			check(sc.su.runWaitFor(cc));
 
 			String sourceFolder = getTmpFontFolder();
 			for (String key : MD5.keySet()) {
 				String source = sourceFolder + "/" + key;
 				String dest = DESTINATION + "/" + key;
 				cc = "cat " + source + " > " + dest;
 				sb.append(cc).append("\n");
 				check(sc.su.runWaitFor(cc));
 
 				cc = "chmod 644  " + dest;
 				sb.append(cc).append("\n");
 				check(sc.su.runWaitFor(cc));
 
 				cc = "rm " + source;
 				sb.append(cc).append("\n");
 				check(sc.su.runWaitFor(cc));
 			}
 			try {
 				cc = "mount -o remount,ro " + system + " /system";
 				sb.append(cc).append("\n");
 				check(sc.su.runWaitFor(cc));
 			} catch (Exception ex) {
 				Log.w(TAG, "Can not mount /system ro :(", ex);
 			}
 			return true;
 
 		} catch (Exception ex) {
 			Log.w(TAG, "Can not install georgian fonts. Please check the logs",
 					ex);
 			return false;
 		} finally {
 			String command = sb.toString();
 			Log.i(TAG, "run commands >>>>");
 			Log.i(TAG, command);
 
 		}
 	}
 
 	private String getSystemPartion() {
 
 		ShellCommand sc = new ShellCommand();
 		CommandResult cr = sc.su.runWaitFor("mount");
 		String stdout = cr.stdout;
 
 		try {
 			LineNumberReader lineNumberReader = new LineNumberReader(
 					new StringReader(stdout));
 			String line = null;
 			while ((line = lineNumberReader.readLine()) != null) {
 				Matcher m = MOUNT_SYSTEM_PATTERN.matcher(line);
 				if (m.matches()) {
 					return m.group(1);
 				}
 			}
 
 		} catch (Exception ex) {
 			Log.w(TAG, ex.getMessage(), ex);
 		}
 		return null;
 	}
 
 	private static void check(CommandResult runWaitFor) throws Exception {
 		if (runWaitFor.success() == false) {
 			throw new Exception(runWaitFor.stderr);
 		}
 	}
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.georgian_installed);
 
 		Button button = (Button) findViewById(R.id.uninstall_this_app);
 		button.setOnClickListener(this);
 
 		ShellCommand shc = new ShellCommand();
 		if (shc.canSU(true) == false) {
 			Toast.makeText(
 					this,
 					"This app can not gain Super User rights. Do you have rooted your device?",
 					Toast.LENGTH_LONG).show();
 			return;
 		}
 
 		button = (Button) findViewById(R.id.install_fonts);
 		button.setVisibility(View.VISIBLE);
 		button.setEnabled(true);
 		button.setOnClickListener(this);
 
 	}
 
 	@Override
 	public void onClick(View v) {
 		if (v.getId() == R.id.uninstall_this_app) {
 			uninstallThis();
 		} else if (v.getId() == R.id.install_fonts) {
 			final AlertDialog.Builder b = new AlertDialog.Builder(this);
 			b.setIcon(android.R.drawable.ic_dialog_alert);
 			b.setTitle("!!!Warning!!!");
 			b.setMessage("This app now mounts /system partions rw and replaces some Droid*.ttf fonts in /system/fonts/");
 			b.setPositiveButton(android.R.string.yes, this);
 			b.setNegativeButton(android.R.string.no, this);
 			b.show();
 		}
 
 		// installFonts();
 	}
 
 	private void uninstallThis() {
 		Uri uri = Uri.fromParts("package", getApplication().getPackageName(),
 				null);
 		Intent deleteThis = new Intent(Intent.ACTION_DELETE, uri);
 		startActivity(deleteThis);
 	}
 
 	public void enableView() {
 		Button button = (Button) findViewById(R.id.uninstall_this_app);
 		button.setEnabled(true);
 		button = (Button) findViewById(R.id.install_fonts);
 		button.setEnabled(true);
 		ProgressBar pb = (ProgressBar) findViewById(R.id.installing);
 		pb.setVisibility(View.INVISIBLE);
 	}
 
 	public void disableView() {
 		Button button = (Button) findViewById(R.id.uninstall_this_app);
 		button.setEnabled(false);
 		button = (Button) findViewById(R.id.install_fonts);
 		button.setEnabled(false);
 		ProgressBar pb = (ProgressBar) findViewById(R.id.installing);
 		pb.setVisibility(View.VISIBLE);
 	}
 
 	@Override
 	public void onClick(DialogInterface dialog, int which) {
 		if (DialogInterface.BUTTON_POSITIVE == which) {
 			new AsyncTask<Void, Void, Boolean>() {
 				@Override
 				protected Boolean doInBackground(Void... params) {
 
 					if (backup() == false) {
 						Log.w(TAG, "Can not backup fonts. Please check logs");
 						return false;
 					}
 
 					return installFonts();
 				}
 
 				@Override
 				protected void onPreExecute() {
 					super.onPreExecute();
 					Installer.this.disableView();
 				}
 
 				@Override
 				protected void onPostExecute(Boolean result) {
 					Installer.this.enableView();
 					if (result) {
						makeToast("Enjoy georgina on your device!\nDon't forget to reboot your device!");
 					} else {
 						makeToast("The fonts were not installed. Please check the logs and contact the developer :(");
 					}
 					super.onPostExecute(result);
 				}
 			}.execute();
 		}
 	}
 
 }
