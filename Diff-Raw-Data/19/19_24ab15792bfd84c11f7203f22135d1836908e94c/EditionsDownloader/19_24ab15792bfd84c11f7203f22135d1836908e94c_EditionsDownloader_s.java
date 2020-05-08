 package br.com.jaker.util;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.OutputStream;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.client.methods.HttpGet;
 import br.com.jaker.control.JSONBookParser;
 import br.com.jaker.model.Book;
 import br.com.jaker.model.Edition;
 import br.com.jaker.view.EditionsActivity;
 import br.com.jaker.view.JakerApp;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.os.AsyncTask;
 import android.os.AsyncTask.Status;
 import android.util.Log;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 /**
  * @author guilherme
  * @email catanduva.gvg@gmail.com
  * */
 public class EditionsDownloader {
 	
 	private AsyncTask<Edition, Integer, Book> task;
 	
 	private AlertDialog.Builder alertDialog;
 	
 	private ProgressDialog progressDialog;
 	
 	private String message;
 	
 	private File zip;
 	
 	public EditionsDownloader(final JakerApp app, final ImageView target, final Context activityContext) {
 		this.message = null;
 		this.task    = new AsyncTask<Edition, Integer, Book>() {
 			
 			private Edition edition;
 			
 			@Override
 			protected void onPreExecute() {
 				progressDialog = new ProgressDialog(activityContext);
 				progressDialog.setTitle(app.getAppName());
 				progressDialog.setMessage("Downloading file...");
 				progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);			
 				progressDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Cancel", new DialogInterface.OnClickListener() {					
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
						cancel(true);					
 					}
 				});
 				progressDialog.show();
 			}
 			
 			@Override
 			protected Book doInBackground(Edition... params) {
 				if (params.length > 0) {
 					edition = params[0];
 					
 					HttpResponse response = null;
 					
 					try {
 						response = makeRequest(edition.getDownloadUrl());
 					} catch (Exception e) {
 						message = Messages.problemConnectingToTheServer;
 						Log.i("EditionsDownloader.task.doInBackground", "Execution stoped: " + e.getMessage());
 						return null;
 					}
 					
 					int statusCode = response.getStatusLine().getStatusCode();
 					
 					if (statusCode != HttpStatus.SC_OK) {
 						try {
 							response.getEntity().getContent().close();
 						} catch (Exception e) {
 							Log.i("EditionsDownloader.task.doInBackground", "Error on close InputStream (response.getEntity().getContent().close()): " + e.getMessage());
 						}
 						message = Messages.serverReturnStats + Integer.toString(statusCode);
 						return null;
 					}
 					
 					long fileSize  = response.getEntity().getContentLength();
 			        long totalRead = 0;
 			        
 			        int read = 0;			        
 			        byte buffer[]    = new byte[1024];
 			        
 			        InputStream in   = null;
 			        OutputStream out = null;
 			        zip = new File(app.getRootPath(), edition.getNumber() + ".zip");
 			        
 			        try {			        	
 			            in  = response.getEntity().getContent();			            
 			            out = new FileOutputStream(zip);			            
 			            while ( ((read = in.read(buffer)) != -1) && !isCancelled() && app.isConnected()) {
 			            	totalRead += read;     
 			                out.write(buffer, 0, read);
			                publishProgress(100, (int)(totalRead * 100 / fileSize));
 			            }
 					} catch (Exception e) {
 						Log.e("EditionsDownloader.task.doInBackground", e.getMessage(), e);						
 						message = Messages.problemToDownloadTheFile;
 						if (zip != null) zip.delete();
 					} finally {
 						if (in  != null) try { in.close();  } catch (Exception e) { }
 						if (out != null) try { out.close(); } catch (Exception e) { }
 					}
 			        
 			        if (zip != null && zip.exists() && zip.length() == fileSize) {
 						try {
 							Utils.unzip(zip, app.getRootPath());						
 						} catch (Exception e) {
 							message = Messages.problemOnUnzipFile;
 							Log.e("EditionsDownloader.task.doInBackground", e.getMessage(), e);
 						}
 						
 						zip.delete();
 						Book book = null;
 						
 						try {
 							book = JSONBookParser.parseBook(
 									new FileInputStream(
 											new File(
 													app.getRootPath(), 
 													edition.getNumber() + "/book.json"
 											)
 									)
 							);
 							book.setEdition(edition);
 							
 							edition.setNewEdition(false);
 							edition.setBook(book);							
 						} catch (Exception e) {
 							message = Messages.problemOnReadJsonBookFile;
 							Log.e("EditionsDownloader.task.doInBackground", e.getMessage(), e);
 						}
 						
 						return book;
 			        } else {
 			        	if (!app.isConnected()) message = Messages.internetConnectionProblem;
 			        	if (zip != null) zip.delete();
 			        }
 				} else {
 					message = Messages.editionIsNull;
 				}
 				
 				return null;
 			}
 			
 			private HttpResponse makeRequest(String url) throws Exception {
 				HttpGet requestGet = new HttpGet(url);
 				requestGet.setHeader("Accept", "text/plain");
 				return Utils.getHTTPClient().execute(requestGet);
 			}
 			
 			@Override
 			protected void onProgressUpdate(Integer... values) {
 				if (values.length == 2) {
 					progressDialog.setMax(values[0]);
 					progressDialog.setProgress(values[1]);
 				}
 			}
 			
 			@Override
 			protected void onCancelled() {
 				if (progressDialog.isShowing()) progressDialog.dismiss();
 				if (zip != null) zip.delete();				
 			}
 			
 			@Override
 			protected void onPostExecute(Book book) {
 				if (progressDialog.isShowing()) progressDialog.dismiss();
 				if (book != null) {				
 					if (target.getTag() != null && target.getTag() instanceof LinearLayout) {
 						LinearLayout layoutCoverImage = (LinearLayout)target.getTag();
 						TextView txt = (TextView)layoutCoverImage.findViewById(EditionsActivity.txt_id);
 						if (txt.getTag() != null && txt.getTag() instanceof Edition) {
 							txt.setText(edition.getTitle() + " Nº " + edition.getNumber());						
 						}	
 					}
 				} else {
 					alertDialog = new AlertDialog.Builder(activityContext);
 					alertDialog.setTitle(app.getAppName());
 					alertDialog.setMessage(message);
 					alertDialog.create();
 					alertDialog.show();
 				}
 			}
 		};
 	}
 	
 	public boolean isShowingProgressDialog() {
 		return progressDialog != null ? progressDialog.isShowing() : false;
 	}
 	
 	public void showProgressDialog() {
 		if (progressDialog != null && !progressDialog.isShowing()) {
 			progressDialog.show();
 		}
 	}
 	
 	public EditionsDownloader execute(Edition edition) {
 		task.execute(edition);
 		return this;
 	}
 	
 	public boolean cancel() {
 		return task.cancel(true);
 	}
 	
 	public final Status getStatus() {
 		return task.getStatus();
 	}
 }
