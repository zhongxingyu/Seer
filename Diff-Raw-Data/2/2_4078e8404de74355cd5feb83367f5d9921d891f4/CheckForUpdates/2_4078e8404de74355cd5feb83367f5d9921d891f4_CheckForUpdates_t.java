 package org.fNordeingang;
 
 // java
 
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.widget.Toast;
 import de.mastacode.http.Http;
 import org.apache.http.client.HttpClient;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.IOException;
 
 // android
 // http
 
 public class CheckForUpdates {
 
 	ProgressDialog progress;
 	Context context;
 
 	CheckForUpdates(Context context) {
 		this.context = context;
 	}
 
 	public void check() {
 		DownloadServerVersionThread dsvt = new DownloadServerVersionThread(handler);
 		dsvt.start();
 	}
 
 	// Define the Handler that receives messages from the DownloadServerVersionThread and proofs for updates
     final Handler handler = new Handler() {
 		public void handleMessage(Message msg) {
 			// get local version code
 			try {
 				PackageInfo versionInfo = context.getPackageManager().getPackageInfo("org.fNordeingang", 0);
 				int localVersion = versionInfo.versionCode;
 
 				int serverVersion = msg.getData().getInt("serverVersion");
 
 				// compare versions
 				if (localVersion > 0 && serverVersion > localVersion) {
 					// newer version available:
 					downloadLatestVersion();
 				}
 
 			} catch (PackageManager.NameNotFoundException e) {
 				// nothing to be done
 			}
 		}
 	};
 
 	// Nested class that downloads the serverVersion information
 	private class DownloadServerVersionThread extends Thread {
 		Handler handler;
 
 		DownloadServerVersionThread(Handler h) {
 			handler = h;
 		}
 
 		public void run() {
 
 			try {
 				// get version from server
 				HttpClient client = new DefaultHttpClient();
				int serverVersion = Integer.parseInt(Http.get("https://raw.github.com/fNordeingang/fNordApp/master/latest").use(client).asString().trim());
 
 				// send version to main thread
 				Message msg = handler.obtainMessage();
 				Bundle b = new Bundle();
 				b.putInt("serverVersion", serverVersion);
 				msg.setData(b);
 				handler.sendMessage(msg);
 
 			} catch (IOException e) {
 				// nothing to be done
 			} catch (NumberFormatException e) {
 				print("NumberFormatException");
 			}
 		}
 	}
 
 	private void downloadLatestVersion() {
 
 		// ask user for download
 		AlertDialog.Builder builder = new AlertDialog.Builder(context);
 		builder.setTitle("New Version Available");
 		builder.setMessage("Do you want to download the latest version?");
 
 		builder.setCancelable(false);
 
 		// download at yes
 		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 
 				// show a progress
 				progress = ProgressDialog.show(context, "", "Downloading. Please wait...", true, false);
 
 				new Thread() {
 					public void run() {
 						// download url of latest version
 						try {
 							HttpClient client = new DefaultHttpClient();
 							String latestURL = Http.get("https://raw.github.com/fNordeingang/fNordApp/master/bin").use(client).asString();
 
 							// if there is a previous file - delete it
 							File apk = new File(context.getExternalFilesDir(null), "fNordApp.apk");
 							if (apk.exists())
 								apk.delete();
 
 							// download file
 							java.io.BufferedInputStream in = new java.io.BufferedInputStream(new java.net.URL(latestURL).openStream());
 							java.io.FileOutputStream fos = new java.io.FileOutputStream(apk);
 							java.io.BufferedOutputStream bout = new BufferedOutputStream(fos,1024);
 							byte[] data = new byte[1024];
 							int x=0;
 							while((x=in.read(data,0,1024))>=0) {
 								bout.write(data,0,x);
 							}
 							bout.close();
 							in.close();
 
 							// don't display progress anymore
 							progress.dismiss();
 
 							// install apk
 							android.net.Uri uri = android.net.Uri.fromFile(apk);
 
 							Intent intent = new Intent(Intent.ACTION_VIEW);
 							intent.setDataAndType(uri, "application/vnd.android.package-archive");
 							context.startActivity(intent);
 
 						}
 						catch (IOException e) {
 							print("Error downloading");
 							progress.dismiss();
 						}
 					}
 				}.start();
 			}
 		});
 
 		// cancel dialog at no
 		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 				dialog.cancel();
 			}
 		});
 
 		// display dialog
 		AlertDialog dialog = builder.create();
 		dialog.show();
 	}
 
 	// helper function
 	void print(String input) {
         CharSequence text = input;
         int duration = Toast.LENGTH_SHORT;
 
         Toast toast = Toast.makeText(context, text, duration);
         toast.show();
     }
 }
