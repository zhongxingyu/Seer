 package br.com.jaker.view;
  
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.List;
 import br.com.jaker.view.R;
 import br.com.jaker.control.SAXEditionsParser;
 import br.com.jaker.control.SAXEnterpriseParser;
 import br.com.jaker.exception.EditionException;
 import br.com.jaker.exception.EnterpriseException;
 import br.com.jaker.model.Edition;
 import br.com.jaker.util.Utils;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Environment;
 import android.util.Log;
 
 /**
  * @author guilherme
  * @email catanduva.gvg@gmail.com
  * */
 public class SplashActivity extends Activity {
 
 	private JakerApp jakerApp;
 	
 	private AsyncEditionsDownloader editionsDownloader;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.splash);
 		
 		jakerApp = (JakerApp)getApplication();
 		boolean readEnterpriseXML = false;
 		String message = "";
 						
 		try {
 			jakerApp.setEnterprise(SAXEnterpriseParser.parseEnterprise(getAssets().open("Enterprise.xml")));
 			readEnterpriseXML = true;
 		} catch (EnterpriseException e) {
 			readEnterpriseXML = false;
 			message = "The " + jakerApp.getAppName() + " app stopped working because: " + e.getMessage();
 		} catch (IOException e) {
 			readEnterpriseXML = false;
 			message = "The " + jakerApp.getAppName() + " app stopped working because: " + e.getMessage();
 		} catch (Exception e) {
 			readEnterpriseXML = false;
 			message = "The " + jakerApp.getAppName() + " app stopped working because: " + e.getMessage();
 		}
 		
 		if (readEnterpriseXML) {
 			jakerApp.setAppName(jakerApp.getEnterprise().getName() != null && !jakerApp.getEnterprise().getName().equals("") ? jakerApp.getEnterprise().getName() : "Jaker");
 			
 			File rootPath = new File(Environment.getExternalStorageDirectory(), jakerApp.getAppName());
 			if (!rootPath.exists()) rootPath.mkdirs();
 			jakerApp.setRootPath(rootPath);
 			
 			editionsDownloader = new AsyncEditionsDownloader();
 			editionsDownloader.execute(jakerApp.getEnterprise().getUrlJsonEdtions());
 		} else {
 			AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
 			alertDialog.setTitle(jakerApp.getAppName());
 			alertDialog.setMessage(message);
 			alertDialog.setNegativeButton("OK", new DialogInterface.OnClickListener() {				
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					SplashActivity.this.finish();
 				}
 			});
 			alertDialog.create();
 			alertDialog.show();
 		}
 	}
 	
 	@Override
 	public void finish() {
 		if (editionsDownloader != null && !editionsDownloader.getStatus().equals(AsyncTask.Status.FINISHED)) {
 			editionsDownloader.cancel(true);
 		}
 		super.finish();
 	}
 	
 	public class AsyncEditionsDownloader extends AsyncTask<String, Void, List<Edition>> {
 
 		private AlertDialog.Builder alertDialog;
 		
 		private ProgressDialog progressDialog;		
 		
 		private String errorMessage;
 		
 		@Override
 		protected void onPreExecute() {
 			progressDialog = new ProgressDialog(SplashActivity.this);
 			progressDialog.setTitle(jakerApp.getAppName());
 			progressDialog.setMessage("Loading Editions...");
 			progressDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Cancel", new DialogInterface.OnClickListener() {					
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					if (!editionsDownloader.isCancelled()) editionsDownloader.cancel(true);
 				}
 			});   
 			progressDialog.show();
 			errorMessage = "";
 		}
 		
 		@Override
 		protected List<Edition> doInBackground(String... params) {
 			if (params == null) 	  return null;
 			if (params[0].equals("")) return null;
 			
 			String url = params[0];
 			List<Edition> editions = null;
 						
 			int read         = 0;				
 	        InputStream in   = null;
 	        OutputStream out = null;
 	        byte buffer[]    = new byte[1024];
 	        File editionsXML = new File(jakerApp.getRootPath(), "editions.xml");
 	        
 	        if (!isCancelled()) {
 				try {
 					if (jakerApp.isConnected()) {						
 						in  = Utils.doGet(url);						
 						out = new FileOutputStream(editionsXML);						
 						while ( ((read = in.read(buffer)) != -1) && !isCancelled() ) out.write(buffer, 0, read);	
 						editions = SAXEditionsParser.parseEdition(new FileInputStream(editionsXML));
 					} else {
 						if (editionsXML != null && editionsXML.exists()) editions = SAXEditionsParser.parseEdition(new FileInputStream(editionsXML));						
 					}
 				} catch (EditionException e) {					
 					Log.e("JakerApp.AsyncEditionsDownloader.doInBackground", "Problem on \"SAXEditionsParser.parseEdition(doGet(url))\": " + e.getMessage(), e);
 					errorMessage = "The " + jakerApp.getAppName() + " app stopped working because: " + e.getMessage();
 				} catch (FileNotFoundException e) {
 					Log.e("JakerApp.AsyncEditionsDownloader.doInBackground", "Problem on \"SAXEditionsParser.parseEdition(doGet(url))\": " + e.getMessage(), e);
 					errorMessage = "The " + jakerApp.getAppName() + " app stopped working because: " + e.getMessage();
 				} catch (Exception e) {
 					Log.e("JakerApp.AsyncEditionsDownloader.doInBackground", "Problem on \"SAXEditionsParser.parseEdition(doGet(url))\": " + e.getMessage(), e);
 					errorMessage = "The " + jakerApp.getAppName() + " app stopped working because: " + e.getMessage();
 				} finally {
 					if (in  != null) try { in.close();               } catch (Exception e) { }
 					if (out != null) try { out.flush(); out.close(); } catch (Exception e) { }
					if (editions == null && (errorMessage == null || errorMessage.equals(""))) errorMessage = "The " + jakerApp.getAppName() + " can't read editions.xml or download it!";
 				}	
 	        }
 	        
 			return editions;
 		}
 		
 		@Override
 		protected void onCancelled() {
 			cancelProgressDialog();
 			if (errorMessage != null && !errorMessage.equals("")) showAlertDialog();
 		}
 		
 		@Override
 		protected void onPostExecute(List<Edition> editions) {
 			cancelProgressDialog();
 			if (editions != null) {
 				jakerApp.setEditions(editions);				
 				startActivity(new Intent(SplashActivity.this, EditionsActivity.class));
				SplashActivity.this.finish();
 			} else {
 				showAlertDialog();
			}			
 		}
 		
 		private void showAlertDialog() {
 			cancelProgressDialog();
 			alertDialog = new AlertDialog.Builder(SplashActivity.this);
 			alertDialog.setTitle(jakerApp.getAppName());
 			alertDialog.setMessage(errorMessage);
 			alertDialog.setNegativeButton("OK", new DialogInterface.OnClickListener() {				
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					SplashActivity.this.finish();
 				}
 			});			
 			alertDialog.create();
 			alertDialog.show();
 		}
 		
 		private void cancelProgressDialog() {
 			if (progressDialog != null && progressDialog.isShowing()) progressDialog.dismiss();
 		}
 	}//AsyncEditionsDownloader
 }//SplashActivity
