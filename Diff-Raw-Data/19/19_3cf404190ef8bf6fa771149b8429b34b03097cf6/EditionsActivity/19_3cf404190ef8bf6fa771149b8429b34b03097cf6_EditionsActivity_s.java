 package br.com.jaker.view;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.List;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpGet;
 import br.com.jaker.view.R;
 import br.com.jaker.control.JSONBookParser;
 import br.com.jaker.exception.BookExpection;
 import br.com.jaker.model.Book;
 import br.com.jaker.model.Edition;
 import br.com.jaker.util.Messages;
 import br.com.jaker.util.Utils;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Color;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 /**
  * @author guilherme
  * @email catanduva.gvg@gmail.com
  * */
 public class EditionsActivity extends Activity implements OnClickListener {
 
 	private static final int txt_id = 100;
 	
 	private static final int img_id = 200;
 	
 	private boolean canBackPress = false;
 	
 	private JakerApp jakerApp;
 	
 	private LinearLayout layoutMain;
 	
 	private AlertDialog.Builder alertDialog;
 	
 	private AsyncEditionZipDownloader editionZipDownloader;
 	
 	private List<DownloadCoverImage> downloadsCoversImages;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.editions);
 		
 		layoutMain = (LinearLayout)findViewById(R.editions.layoutMain);
 		
 		downloadsCoversImages = new ArrayList<DownloadCoverImage>();
 		
 		jakerApp = (JakerApp)getApplication();
 		
 		for (Edition edition : jakerApp.getEditions()) {
 			LinearLayout layoutCover = new LinearLayout(this);
 			layoutCover.setOrientation(LinearLayout.VERTICAL);
 			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(100, LayoutParams.WRAP_CONTENT);
 			params.rightMargin = 20;
 			layoutCover.setLayoutParams(new LinearLayout.LayoutParams(80, LayoutParams.WRAP_CONTENT));
 			ImageView img = new ImageView(this);
 			img.setId(img_id);
 			img.setLayoutParams(new LayoutParams(100, 160));			
 			img.setOnClickListener(this);
 			img.setTag(layoutCover);
 			String coverImage = edition.getCoverImage();
 			if (coverImage != null && !coverImage.equals("")) {
 				String fileName = coverImage.substring(coverImage.lastIndexOf('/') + 1, coverImage.length());
 				File image = new File(jakerApp.getCachePath(), fileName);
 				if (image.exists() && image.isFile()) {
 					img.setImageBitmap(BitmapFactory.decodeFile(image.getAbsolutePath()));
 				} else {
 					img.setImageResource(R.drawable.loading);
 					DownloadCoverImage downloadCoverImage = new DownloadCoverImage();
 					downloadCoverImage.execute(new Object[]{img, coverImage});
 					downloadsCoversImages.add(downloadCoverImage);
 				}
 			}
 			layoutCover.addView(img);
 			TextView txt = new TextView(this);
 			txt.setId(txt_id);
 			txt.setTextSize(10);
 			txt.setTextColor(Color.WHITE);
 			txt.setGravity(Gravity.CENTER);
 			txt.setText(edition.isNewEdition() && !checkIfExists(edition) ? 
 						edition.getTitle() + " Nº " + edition.getNumber() + " - New Edition" 
 					: 
 						edition.getTitle() + " Nº " + edition.getNumber()
 			);
 			txt.setTag(edition);
 			layoutCover.addView(txt);
 			layoutMain.addView(layoutCover);			
 		}
 	}
 	
 	@Override
 	public void finish() {
 		if (editionZipDownloader != null && !editionZipDownloader.getStatus().equals(AsyncTask.Status.FINISHED)) {
 			editionZipDownloader.cancel(true);
 		}
 		if (downloadsCoversImages != null) {
 			for (DownloadCoverImage downloadCoverImage : downloadsCoversImages) {
 				if (!downloadCoverImage.getStatus().equals(AsyncTask.Status.FINISHED)) {
 					downloadCoverImage.cancel(true);
 				}
 			}
 		}
 		super.finish();
 	}
 	
 	@Override
 	public void onBackPressed() {
 		if (editionZipDownloader != null 
 				&& !editionZipDownloader.getStatus().equals(AsyncTask.Status.FINISHED)
 					&& !canBackPress) {
 			canBackPress = false;
 			alertDialog = new AlertDialog.Builder(this);
 			alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {				
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					canBackPress = true;
 					EditionsActivity.this.onBackPressed();
 					dialog.dismiss();
 				}
 			});
 			alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {				
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					canBackPress = false;
 					dialog.dismiss();
 				}
 			});
 			alertDialog.setTitle("Warning");
 			alertDialog.setMessage("There is a download issue in progress and you can exit the application it is necessary to shut it down,\nyou want to continue?");
 			alertDialog.create();
 			alertDialog.show();
 		} else {
 			super.onBackPressed();	
 		}	
 	}
 	
 	@Override
 	public void onClick(View v) {
 		if (v instanceof ImageView) {
 			Edition edition = null;
 			if (v.getTag() != null && v.getTag() instanceof LinearLayout) {
 				LinearLayout layoutCoverImage = (LinearLayout)v.getTag();
 				TextView txt = (TextView)layoutCoverImage.findViewById(txt_id);
 				if (txt.getTag() != null && txt.getTag() instanceof Edition) {
 					edition = (Edition)txt.getTag();	
 				}	
 			}
 			if (edition != null && edition.isNewEdition() && (editionZipDownloader == null || editionZipDownloader.getStatus().equals(AsyncTask.Status.FINISHED))) {
 				editionZipDownloader = new AsyncEditionZipDownloader((ImageView)v);
 				editionZipDownloader.execute(edition);
 			} else if (editionZipDownloader != null && !editionZipDownloader.getStatus().equals(AsyncTask.Status.FINISHED)) {
 				editionZipDownloader.showProgressDialog();
 			} else if (edition != null && edition.getBook() != null) {
 				startActivity(new Intent(this, JakerSliderPaginatorActivity.class).putExtra("book", edition.getBook()));				
 			}
 		}
 	}
 	
 	private boolean checkIfExists(Edition edition) {
 		boolean exists = false;		
 		File editionPath = new File(jakerApp.getRootPath(), Integer.toString(edition.getNumber()));
 		if (editionPath.exists() && editionPath.isDirectory()) {
 			File bookFile = new File(editionPath, "book.json");
 			if (bookFile.exists() && bookFile.isFile()) {				
 				try {
 					Book book = JSONBookParser.parseBook(new FileInputStream(bookFile));
 					book.setEdition(edition);
 					edition.setBook(book);
 					edition.setNewEdition(false);
 					exists = true;
 				} catch (FileNotFoundException e) {
 					alertDialog = new AlertDialog.Builder(this);
 					alertDialog.setMessage("The file " + bookFile.getAbsolutePath() + " was not found.");
 				} catch (BookExpection e) {
 					alertDialog = new AlertDialog.Builder(this);
 					alertDialog.setMessage("The file " + bookFile.getAbsolutePath() + " was is bad format.");
 				} catch (Exception e) {
 					alertDialog = new AlertDialog.Builder(this);
 					alertDialog.setMessage("The " + jakerApp.getAppName() + " stopped because: " + e.getMessage());
 				} finally {
 					if (alertDialog != null) {
 						alertDialog.setTitle(jakerApp.getAppName());
 						alertDialog.create();
 						alertDialog.show();
 					}
 				}
 			}
 		}
 		return exists;
 	}
 	
 	public class DownloadCoverImage extends AsyncTask<Object, Void, Bitmap> {
 
 		private ImageView img;
 		
 		@Override
 		protected Bitmap doInBackground(Object... params) {
 			if (params.length >= 2) {
 				Object img = params[0];
 				Object coverImage = params[1];
 				if (img instanceof ImageView) {
 					this.img = (ImageView)img;
 				} else {
 					return null;
 				}				
 				if (coverImage instanceof String) {
 					String url = (String)coverImage;
 					if (jakerApp.isConnected()) {
 						InputStream in = Utils.doGet(url);
 						String fileName = url.substring(url.lastIndexOf('/') + 1, url.length());
						Utils.write(in, new File(jakerApp.getCachePath(), fileName));
						return BitmapFactory.decodeStream(in);
 					}
 				}
 			}
 			return null;
 		}
 		
 		@Override
 		protected void onPostExecute(Bitmap bitmap) {
 			if (img != null && bitmap != null) {
 				img.setImageBitmap(bitmap);
 			} else {
 				img.setImageResource(R.drawable.warning);
 			}
 		}
 		
 	}//DownloadCoverImage
 	
 	public class AsyncEditionZipDownloader extends AsyncTask<Edition, Integer, Book> {
 
 		public AsyncEditionZipDownloader(ImageView target) {
 			this.target = target;
 			EditionsActivity.this.canBackPress = false;
 		}
 		
 		private AlertDialog.Builder alertDialog;
 		
 		private ProgressDialog progressDialog;		
 		
 		private boolean cancelable;
 		
 		private Edition edition;
 		
 		private String message;
 		
 		private ImageView target;
 		
 		private File zip;
 		
 		@Override
 		protected void onPreExecute() {
 			progressDialog = new ProgressDialog(EditionsActivity.this);
 			progressDialog.setTitle(jakerApp.getAppName());
 			progressDialog.setMessage("Downloading file.");
 			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);			
 			if (progressDialog != null) {
 				if (cancelable) {
 					progressDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Cancel", new DialogInterface.OnClickListener() {					
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							if (AsyncEditionZipDownloader.this.isCancelled()) AsyncEditionZipDownloader.this.cancel(true);
 						}
 					});	
 				} 
 				progressDialog.show();
 			}
 		}
 		
 		@Override
 		protected Book doInBackground(Edition... editions) {
 			edition = editions[0];
 			
 			if (edition != null && target != null && progressDialog != null) {	
 				HttpGet requestGet = new HttpGet(edition.getDownloadUrl());
 				requestGet.setHeader("Accept", "text/plain");
 				
 				HttpResponse response = null;
 				
 				try {
 					response = Utils.getHTTPClient().execute(requestGet);
 				} catch (ClientProtocolException cpe) {
 					message = Messages.problemConnectingToTheServer;
 					Log.i("EditionsActivity.AsyncEditionZipDownloader.doInBackground", "Execution stoped: " + cpe.getMessage());
 					return null;
 				} catch (IOException ioe) {
 					message = Messages.problemConnectingToTheServer;
 					Log.i("EditionsActivity.AsyncEditionZipDownloader.doInBackground", "Execution stoped: " + ioe.getMessage());
 					return null;
 				} catch (Exception e) {
 					message = Messages.problemConnectingToTheServer;
 					Log.i("EditionsActivity.AsyncEditionZipDownloader.doInBackground", "Execution stoped: " + e.getMessage());
 					return null;
 				}
 				
 				int statusCode = response.getStatusLine().getStatusCode();
 				
 				if (statusCode != HttpStatus.SC_OK) {
 					try {
 						response.getEntity().getContent().close();
 					} catch (IOException ioe) {						
 						Log.i("EditionsActivity.AsyncEditionZipDownloader.doInBackground", "Error on close InputStream (response.getEntity().getContent().close()): " + ioe.getMessage());
 					} catch (Exception e) {
 						Log.i("EditionsActivity.AsyncEditionZipDownloader.doInBackground", "Error on close InputStream (response.getEntity().getContent().close()): " + e.getMessage());
 					}
 					message = Messages.serverReturnStats + Integer.toString(statusCode);
 					return null;
 				}
 				
 				long fileSize  = response.getEntity().getContentLength();
 		        long totalRead = 0;        
 		        int read = 0;
 		        byte buffer[] = new byte[1024];
 		        InputStream in = null;
 		        OutputStream out = null;
 		        zip = new File(jakerApp.getRootPath(), edition.getNumber() + ".zip");
 		        
 				try {
 					
 		            in  = response.getEntity().getContent();
 		            out = new FileOutputStream(zip);
 		            
 		            while ( ((read = in.read(buffer)) != -1) && !isCancelled() && jakerApp.isConnected()) {
 		            	totalRead += read;                
 		                out.write(buffer, 0, read);
 		                publishProgress(100, (int)(totalRead * 100 / fileSize));
 		            }
 		            
 				} catch (IllegalStateException ise) {
 					if (zip != null && zip.exists())zip.delete();
 					message = Messages.problemToDownloadTheFile;
 					Log.e("EditionsActivity.AsyncEditionZipDownloader.doInBackground", ise.getMessage(), ise);
 				} catch (IOException ioe) {
 					if (zip != null && zip.exists())zip.delete();
 					message = Messages.problemToDownloadTheFile;
 					Log.e("EditionsActivity.AsyncEditionZipDownloader.doInBackground", ioe.getMessage(), ioe);
 				} catch (Exception e) {
 					if (zip != null && zip.exists())zip.delete();
 					message = Messages.problemToDownloadTheFile;
 					Log.e("EditionsActivity.AsyncEditionZipDownloader.doInBackground", e.getMessage(), e);					
 				} finally {
 					if(in  != null) try { in.close();  } catch (Exception e) {}
 					if(out != null) try { out.close(); } catch (Exception e) {}
 				}
 				
 				if (zip != null && zip.exists() && zip.length() == fileSize) {
 					try {
 						Utils.unzip(zip, jakerApp.getRootPath());						
 					} catch (Exception e) {
 						message = Messages.problemOnUnzipFile;
 						Log.e("EditionsActivity.AsyncEditionZipDownloader.doInBackground", e.getMessage(), e);
 					}
 					
 					zip.delete();
 					Book book = null;
 					
 					try {
 						book = JSONBookParser.parseBook(new FileInputStream(new File(jakerApp.getRootPath(), edition.getNumber() + "/book.json")));
 						book.setEdition(edition);
 						edition.setBook(book);
 						edition.setNewEdition(false);
 					} catch (Exception e) {
 						message = Messages.problemOnReadJsonBookFile;
 						Log.e("EditionsActivity.AsyncEditionZipDownloader.doInBackground", e.getMessage(), e);
 					}
 					
 					return book;
 				} else {
 					if (!jakerApp.isConnected()) message = Messages.internetConnectionProblem;
 				}
 			} else {
 				if (edition == null) 		     message = Messages.editionIsNull;
 				else if (progressDialog == null) message = Messages.progressBarIsNull;
 				else if (target == null) 	     message = Messages.targetIsNull;
 			}
 			return null;
 		}
 		
 		@Override
 		protected void onProgressUpdate(Integer... values) {
 			if (values.length == 2) {
 				progressDialog.setMax(values[0]);
 				progressDialog.setProgress(values[1]);
 			}
 		}
 		
 		public boolean isShowingProgressDialog() {
 			return progressDialog != null ? progressDialog.isShowing() : false;
 		}
 		
 		public void showProgressDialog() {
 			if (progressDialog != null && !progressDialog.isShowing()) {
 				progressDialog.show();
 			}
 		}
 		
 		@Override
 		protected void onCancelled() {
 			if (isCancelled() && zip != null && zip.exists()) zip.delete();
 			if (progressDialog.isShowing()) progressDialog.dismiss();
 		}
 		
 		@Override
 		protected void onPostExecute(Book book) {
 			if (progressDialog.isShowing()) progressDialog.dismiss();
 			if (book != null) {				
 				if (target.getTag() != null && target.getTag() instanceof LinearLayout) {
 					LinearLayout layoutCoverImage = (LinearLayout)target.getTag();
 					TextView txt = (TextView)layoutCoverImage.findViewById(txt_id);
 					if (txt.getTag() != null && txt.getTag() instanceof Edition) {
 						txt.setText(edition.getTitle() + " Nº " + edition.getNumber());						
 					}	
 				}
 			} else {
 				alertDialog = new AlertDialog.Builder(EditionsActivity.this);
 				alertDialog.setTitle(jakerApp.getAppName());
 				alertDialog.setMessage(message);
 				alertDialog.create();
 				alertDialog.show();
 			}
 		}
 	}//AsyncEditionZipDownloader
 }//EditionsActivity
