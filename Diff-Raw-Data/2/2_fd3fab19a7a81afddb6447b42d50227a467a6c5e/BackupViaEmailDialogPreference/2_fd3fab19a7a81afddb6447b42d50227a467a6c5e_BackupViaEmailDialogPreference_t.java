 package com.dzebsu.acctrip.settings.dialogs;
 
 import java.util.Calendar;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences.Editor;
 import android.net.Uri;
 import android.text.format.DateFormat;
 import android.util.AttributeSet;
 
 import com.dzebsu.acctrip.R;
 
 public class BackupViaEmailDialogPreference extends BaseBackupConfirmDialogPreference {
 
 	private static final String KEY_DEVICE_BACKUP = "pref_backup_device_last";
 
 	public BackupViaEmailDialogPreference(Context context, AttributeSet attrs) {
 		super(context, attrs);
 	}
 
 	@Override
 	protected Integer performConfirmedAction() {
 		Intent sendIntent = new Intent(Intent.ACTION_SEND);
 		sendIntent.setType("plain/text");
 		sendIntent.putExtra(Intent.EXTRA_SUBJECT, cxt.getString(R.string.backup_subject));
 		String s = makeBackupDBToDeviceExternalMemory();
 		Editor ed = findPreferenceInHierarchy(KEY_DEVICE_BACKUP).getEditor();
 		ed.putString(KEY_DEVICE_BACKUP, DateFormat.format("dd/MM/yy", Calendar.getInstance()).toString() + "@" + s);
 		ed.commit();
 		try {
			sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:" + s));
 		} catch (Exception e) {
 			return null;
 		}
 		sendIntent.putExtra(Intent.EXTRA_TEXT, cxt.getString(R.string.backup_text));
 		Editor ed2 = this.getEditor();
 		ed2.putString(this.getKey(), DateFormat.format("dd/MM/yy", Calendar.getInstance()).toString());
 		ed2.commit();
 		cxt.startActivity(Intent.createChooser(sendIntent, "Backup:"));
 		return R.string.assum_sent;
 	}
 
 }
