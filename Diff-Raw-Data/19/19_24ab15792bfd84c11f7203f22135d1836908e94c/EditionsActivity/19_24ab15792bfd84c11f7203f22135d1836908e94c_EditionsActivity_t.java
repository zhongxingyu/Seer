 package br.com.jaker.view;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 import br.com.jaker.view.R;
 import br.com.jaker.control.JSONBookParser;
 import br.com.jaker.exception.BookExpection;
 import br.com.jaker.model.Book;
 import br.com.jaker.model.Edition;
 import br.com.jaker.util.EditionsDownloader;
 import br.com.jaker.util.Utils;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Color;
 import android.os.AsyncTask;
 import android.os.Bundle;
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
 
 	public static final int txt_id = 100;
 	
 	public static final int img_id = 200;
 	
 	private boolean canBackPress = false;
 	
 	private JakerApp jakerApp;
 	
 	private LinearLayout layoutMain;
 	
 	private AlertDialog.Builder alertDialog;
 	
 	private EditionsDownloader editionsDownloader;
 	
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
 		if (editionsDownloader != null && !editionsDownloader.getStatus().equals(AsyncTask.Status.FINISHED)) {
 			editionsDownloader.cancel();
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
 		if (editionsDownloader != null 
 				&& !editionsDownloader.getStatus().equals(AsyncTask.Status.FINISHED)
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

			if (edition != null && edition.isNewEdition() && (editionsDownloader == null || editionsDownloader.getStatus().equals(AsyncTask.Status.FINISHED) || editionsDownloader.isCancelled())) {
 				editionsDownloader = new EditionsDownloader(jakerApp, (ImageView)v, this);
 				editionsDownloader.execute(edition);
 			} else if (editionsDownloader != null && !editionsDownloader.getStatus().equals(AsyncTask.Status.FINISHED)) {
 				editionsDownloader.showProgressDialog();
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
 						File imageCover = new File(jakerApp.getCachePath(), fileName);
 						Utils.write(in, imageCover);
 						try { if(in != null) in.close(); } catch (Exception e) { }
 						return BitmapFactory.decodeFile(imageCover.getAbsolutePath());
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
 	
 }//EditionsActivity
