 package org.scinix.android.gallorg;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class GallOrgShare extends Activity implements OnClickListener {
 
 	private static final String ORION_ROOT = "/sdcard/Orion/GallOrg/";
 
 	private ArrayList < File > fileArray = new ArrayList < File > ();
 	private Button btnMove;
 	private Button btnCancel;
 
 
 	@Override protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.share);
 		TextView tv = (TextView) findViewById(R.id.filelist);
 
 		File rootDir = new File(ORION_ROOT);
 		if (rootDir.exists() && rootDir.isDirectory()) {
 			ArrayList < File > dirList =
 			    new ArrayList < File >
 			    (Arrays.asList(rootDir.listFiles()));
 			Iterator < File > e = dirList.iterator();
 			while (e.hasNext()) {
 				Log.i("gallorg",
 				      "Exist Album '" +
 				      ((File) e.next()).getName() +
 				      "' found.");
 		}}
 
 		Intent intent = getIntent();
 		Bundle extras = intent.getExtras();
 
 		if (Intent.ACTION_SEND.equals(intent.getAction())) {
 			if (extras != null) {
 				Uri fileUri =
 				    (Uri) extras.getParcelable(Intent.
 							       EXTRA_STREAM);
 				fileArray.add(UriUtils.
 					      getFileFromUri(fileUri,
 							     this));
 			} else {
 				tv.append(", extras == null");
 			}
 		} else if (Intent.ACTION_SEND_MULTIPLE.
 			   equals(intent.getAction())) {
 			if (extras != null) {
 				ArrayList < Uri > uriArray =
 				    extras.getParcelableArrayList
 				    (Intent.EXTRA_STREAM);
 				Iterator < Uri > e = uriArray.iterator();
 				while (e.hasNext()) {
 					fileArray.add(UriUtils.
 						      getFileFromUri((Uri)
 								     e.
 								     next
 								     (),
 								     this));
 				}
 			} else {
 				tv.append(", extras == null");
 			}
 		}
 
 		Iterator < File > e = fileArray.iterator();
 		while (e.hasNext()) {
 			File file = (File) e.next();
 			if (file.exists() && file.isFile()) {
 				tv.append("* " + file.getName());
 			}
 			tv.append("\n");
 		}
 
 		btnMove = (Button) findViewById(R.id.ok);
 		btnCancel = (Button) findViewById(R.id.cancel);
 
 		btnMove.setOnClickListener(this);
 		btnCancel.setOnClickListener(this);
 	}
 
 
 	public void onClick(View v) {
 		switch (v.getId()) {
 			case R.id.ok:
 				String folderName = ((TextView)
 						     findViewById(R.
 								  id.destination)).
 				    getText().toString();
 				File destDir =
 				    new File(ORION_ROOT + folderName);
 				destDir.mkdirs();
 				Log.i("gallorg",
 				      "destination is " + folderName);
 
 				Iterator < File > e = fileArray.iterator();
 				while (e.hasNext()) {
 					File file = (File) e.next();
 					File dest =
 					    new File(ORION_ROOT +
 						     folderName + "/" +
 						     file.getName());
 					file.renameTo(dest);
 					Log.i("gallorg",
 					      "rename " +
 					      file.getAbsolutePath() +
 					      " to " +
 					      dest.getAbsolutePath());
 				}
 
 				finish();
 				break;
 
 			case R.id.cancel:
 				finish();
 				break;
 		}
 	}
 
 	public void onItemSelected(View v) {
 	}
 }
