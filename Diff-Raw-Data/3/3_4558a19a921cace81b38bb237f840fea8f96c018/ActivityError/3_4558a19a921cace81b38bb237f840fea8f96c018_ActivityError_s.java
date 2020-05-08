 package me.kennydude.teapot;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.OutputStreamWriter;
 import java.io.Writer;
 
 import android.app.Activity;
 import android.app.ApplicationErrorReport;
 import android.content.Intent;
 import android.content.pm.ApplicationInfo;
 import android.content.pm.PackageManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.util.StringBuilderPrinter;
 import android.view.View;
 import android.widget.Button;
 
 public class ActivityError extends Activity {
 	
 	Intent manualIntent;
 
 	@Override
 	public void onCreate(Bundle bi){
 		super.onCreate(bi);
 	
 		setContentView(R.layout.activity_error);
 		
 		new Thread(new Runnable(){
 
 			@Override
 			public void run() {
 				ApplicationErrorReport aer = getIntent().getParcelableExtra(Intent.EXTRA_BUG_REPORT);
 				
 				StringBuilder report = new StringBuilder("Error Report conducted by @kennydude's teapot\n\n");
 				aer.dump(new StringBuilderPrinter(report), "");
 				
 				// Now save the report
 				String folder = "";
 				try{
 					folder = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/me.kennydude.teapot";
 					File file = new File(folder);
 					if(!file.exists()){
 						file.mkdirs();
 					}
 					
 					folder = folder + "/report.txt";
 					file = new File(folder);
 					if(!file.exists()){
 						file.createNewFile();
 					}
 					FileOutputStream fos = new FileOutputStream(folder);
 					Writer out = new OutputStreamWriter(fos);
 					
 					out.write(report.toString());
 					out.close();
 					
 				} catch(Exception e){
 					e.printStackTrace();
 					// TODO: Error
 					return;
 				}
 				
 				// Now attempt to get the email of where to send to
 				try{
 					PackageManager pm = getPackageManager();
 					ApplicationInfo ai = pm.getApplicationInfo(aer.packageName, PackageManager.GET_META_DATA);
 					
 					Bundle meta = ai.metaData;
 					
 					if(meta.containsKey("DEVELOPER_EMAIL")){
 						Intent email = new Intent(Intent.ACTION_SENDTO);
 						email.setData(Uri.parse("mailto:" + meta.getString("DEVELOPER_EMAIL")));
 						//email.setType("text/plain");
 						email.putExtra(Intent.EXTRA_EMAIL, new String[]{ meta.getString("DEVELOPER_EMAIL") });
 						email.putExtra(Intent.EXTRA_SUBJECT, "Bug report for " + aer.packageName);
 						email.putExtra(Intent.EXTRA_STREAM,
 								Uri.parse("file://" + folder));
 						email.putExtra(Intent.EXTRA_TEXT, "Press send to send the report automatically :)");
 						startActivity(email);
 						finish();
 						
 					} else{
 						manualIntent = new Intent(Intent.ACTION_SEND);
 						manualIntent.setType("text/plain");
 						manualIntent.putExtra(Intent.EXTRA_SUBJECT, "Bug report for " + aer.packageName);
 						manualIntent.putExtra(Intent.EXTRA_STREAM,
 								Uri.parse("file://" + folder));
 						showManual();
 					}
 				
 				} catch(Exception e){
 					e.printStackTrace();
 				}
 			}
 			
 		}).start();
 	}
 	
 	public void showManual(){
 		runOnUiThread(new Runnable(){
 
 			@Override
 			public void run() {
 				Button manual = (Button)findViewById(R.id.manualReport);
 				manual.setVisibility(View.VISIBLE);
 				manual.setOnClickListener(new View.OnClickListener() {
 					
 					@Override
 					public void onClick(View arg0) {
 						startActivity(manualIntent);
 						finish();
 					}
 					
 				});
 			}
 			
 		});
 	}
 }
