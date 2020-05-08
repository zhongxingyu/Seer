 package org.scinix.android.gallorg;
 
 import java.io.File;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Iterator;
 
 import org.scinix.android.utils.MediaScanner;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.AdapterView.OnItemSelectedListener;
 
 public class GallOrgShare extends Activity implements OnClickListener, OnItemSelectedListener {
 
 	private static final String ORION_ROOT = "/sdcard/Orion/GallOrg/";
 
 	private ArrayList<File> fileArray = new ArrayList<File> ();
 	private ArrayList<Uri> uriList = new ArrayList<Uri> ();
 	private Button btnMove;
 	private Button btnCancel;
 
 	private String default_destination = new String();
 	private boolean first_time = true;
 
 	@Override protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		ArrayList <String> dirStringList = new ArrayList <String> ();
 
 		setContentView(R.layout.share);
 		TextView tv = (TextView) findViewById(R.id.filelist);
 		TextView amount = (TextView) findViewById(R.id.amount_of_files);
 		AutoCompleteTextView destination = (AutoCompleteTextView) findViewById(R.id.destination);
 		Spinner exists = (Spinner) findViewById(R.id.exists);
 
 		/* set default options */
 		((CheckBox) findViewById(R.id.scanmedia)).setChecked(true);
 
 		/* get existing album(directory) list from ORION_ROOT */
 		File rootDir = new File(ORION_ROOT);
 		if (rootDir.exists() && rootDir.isDirectory()) {
 			ArrayList<File> dirList = new ArrayList<File> (Arrays.asList(rootDir.listFiles()));
 			Iterator<File> e = dirList.iterator();
 			while (e.hasNext()) {
 				dirStringList.add(((File) e.next()).getName());
 			}
 		}
 
 		/* sort and insert default destinations. */
 		Collections.sort(dirStringList);
 		SimpleDateFormat nowFormatted = new SimpleDateFormat("yyyyMMdd");
 		default_destination = nowFormatted.format(new Date()).toString();
 		destination.setText(default_destination);
 		/* remove default folder from 'existing album list'. but Camera */
 		dirStringList.add(0, "<Camera>");
 
 		/* make array and adapter for spinner and auto-completion. */
 		String[] dirArray = new String[dirStringList.size()];
 		dirStringList.toArray(dirArray);
 		Log.i("gallorg", "existing dirs(array): " + Arrays.asList(dirArray).toString());
 
 		/* disabling drop-down auto-completion.
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
 				android.R.layout.simple_dropdown_item_1line, dirArray);
 		destination.setAdapter(adapter);
 		*/
 		ArrayAdapter<String> adapterSpinner = new ArrayAdapter<String>(this,
 				android.R.layout.simple_spinner_item, dirArray);
 		exists.setAdapter(adapterSpinner);
 		exists.setOnItemSelectedListener(this);
 
 		/* get selected files and add it to src list (counting and debugging purpose.) */
 		Intent intent = getIntent();
 		Bundle extras = intent.getExtras();
 
 		if (Intent.ACTION_SEND.equals(intent.getAction())) {
 			if (extras != null) {
 				Uri fileUri = (Uri) extras.getParcelable(Intent.EXTRA_STREAM);
 				fileArray.add(UriUtils.getFileFromUri(fileUri, this));
 				uriList.add(fileUri);
 			} else {
 				tv.append(", extras == null");
 			}
 		} else if (Intent.ACTION_SEND_MULTIPLE.
 			   equals(intent.getAction())) {
 			if (extras != null) {
 				ArrayList<Uri> uriArray = extras.getParcelableArrayList(Intent.EXTRA_STREAM);
 				uriList.addAll(uriArray);
 				Iterator < Uri > e = uriArray.iterator();
 				while (e.hasNext()) {
 					fileArray.add(UriUtils.getFileFromUri((Uri) e.next(), this));
 				}
 			} else {
 				tv.append(", extras == null");
 			}
 		}
 
 		/* file list for debugging. */
 		Iterator < File > e = fileArray.iterator();
 		while (e.hasNext()) {
 			File file = (File) e.next();
 			if (file.exists() && file.isFile()) {
 				tv.append("* " + file.getName());
 			}
 			tv.append("\n");
 		}
		Log.d("gallorg", "selected content list: " + uriList.toString());
		Log.d("gallorg", "selected file list: " + fileArray.toString());
 
 		/* display amount of selected files. */
 		amount.setText(Integer.toString(fileArray.size()));
 
 		/* button binding. */
 		btnMove = (Button) findViewById(R.id.ok);
 		btnCancel = (Button) findViewById(R.id.cancel);
 
 		btnMove.setOnClickListener(this);
 		btnCancel.setOnClickListener(this);
 	}
 
 	@Override
 	public void onClick(View v) {
 		switch (v.getId()) {
 			case R.id.ok:
 				String folderName = ((TextView) findViewById(R.id.destination)).getText().toString();
 				File destDir;
 				if (folderName.equals("<Camera>")) {
 					destDir = new File(Environment.getExternalStorageDirectory().toString() + "/DCIM/Camera");
 				} else {
 					destDir = new File(ORION_ROOT + folderName);
 					destDir.mkdirs();
 				}
 				Log.i("gallorg", "destDir is " + destDir.getAbsolutePath());
 				/* FIXME: assert destination != parent dir of selected files. */
 
 				String[] filesToScan = new String[fileArray.size()];
 				int numOfFilesToScan = 0;
 
 				/* moving files to destination. */
 				Iterator<File> e = fileArray.iterator();
 				while (e.hasNext()) {
 					File file = (File) e.next();
 					File dest = new File(destDir.getAbsolutePath() + "/" + file.getName());
 
 					Log.i("gallorg", "rename " + file.getAbsolutePath() + " to " + dest.getAbsolutePath());
 					if (file.renameTo(dest)) {
 						Log.i("gallorg", "renameTo returns true.");
 						filesToScan[numOfFilesToScan++] = dest.getAbsolutePath();
 					} else {
 						Log.e("gallorg", "renameTo returns false.");
 						/* FIXME: some error message here! */
 					}
 				}
 
 				/* add and remove from content provider. (media scanning) */
 				if (((CheckBox) findViewById(R.id.scanmedia)).isChecked()) {
 					Log.i("gallorg", "option scanmedia is checked.");
 					if (numOfFilesToScan > 0) {
 						Log.i("gallorg", Integer.toString(numOfFilesToScan) + " files will be scaned.");
 						MediaScanner scanner = new MediaScanner(this);
 						scanner.scanFile(filesToScan);
 					}
 
					/* remove from ContentProvider */
 					Iterator<Uri> eu = uriList.iterator();
 					while (eu.hasNext()) {
 						Uri fileUri = eu.next();
 						File file = UriUtils.getFileFromUri(fileUri, this);
						Log.d("gallorg", "uri: " + fileUri.toString());
						if (file.exists() == false && fileUri.getScheme().equals("content")) {
 							int count = getContentResolver().delete(fileUri, null, null);
 							Log.i("gallorg", "deleted " + Integer.toString(count) + " record(s) from content provider.");
						} else if (!fileUri.getScheme().equals("content")) {
							Log.i("gallorg", "scheme of file is not 'content'. (" + fileUri.getScheme() + ") ignore.");
							/* XXX how can i alert/broadcast file deletion to parent program? */
 						} else {
							/* maybe on case of ERROR */
 							Log.i("gallorg", "selected file(" + file.getName() + ") yet exist. cancel record deletion.");
 						}
 					}
 				}
 
 				/* remove empty folders from ROOT. */
 				if (((CheckBox) findViewById(R.id.cleanup)).isChecked()) {
 					Log.i("gallorg", "option cleanup is checked.");
 					File rootDir = new File(ORION_ROOT);
 					if (rootDir.exists() && rootDir.isDirectory()) {
 						ArrayList<File> dirList = new ArrayList<File> (Arrays.asList(rootDir.listFiles()));
 						Iterator<File> el = dirList.iterator();
 						while (el.hasNext()) {
 							File currDir = (File) el.next();
 							if (currDir.isDirectory() && (currDir.list().length == 0)) {
 								Log.d("gallorg", "subdir '" + currDir.getName() + "' is empty. remove it.");
 								if (!currDir.delete()) {
 									Log.e("gallorg", "oops! '" + currDir.getName() + "' is empty but cannot delete it!");
 								}
 							}
 						}
 					}
 				}
 
 				finish();
 				break;
 
 			case R.id.cancel:
 				if (((TextView) findViewById(R.id.destination)).getText().toString().equals("About...")) {
 					setContentView(R.layout.about);
 				} else {
 					finish();
 				}
 				break;
 		}
 	}
 
 	@Override
 	public void onItemSelected(AdapterView<?> parent, View view, int position,
 			long id) {
 		// TODO Auto-generated method stub
 		if (first_time) {
 			// XXX DIRTY! how can i handle it with more gentle way?
 			first_time = false;
 			return;
 		}
 		((TextView) findViewById(R.id.destination)).setText(parent.getItemAtPosition(position).toString());
 	}
 
 	@Override
 	public void onNothingSelected(AdapterView<?> parent) {
 		// TODO Auto-generated method stub
 	}
 }
