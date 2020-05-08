 package pl.twopointtwo.sitenote;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnCancelListener;
 import android.content.Intent;
 import android.database.sqlite.SQLiteDatabase;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.media.ExifInterface;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.NavUtils;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.TextView;
 import pl.twopointtwo.sitenote.R;
 
 public class YourPhotoActivity extends Activity {
 	private TextView textView;
 
 	private String fileFromIntent;
 	File photo;
 
 	@SuppressLint("NewApi")
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		Log.d("LogInfo001","TFA - onCreate");		
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_your_photo);
 		// Show the Up button in the action bar.
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 		// dodajZdjecie();
 
 	}
 
 	@SuppressLint("NewApi")
 	@Override
 	protected void onResume() {
 		Log.d("LogInfo001","TFA - onResume");		
 		super.onResume();
 
 		Intent intent = getIntent();
 		fileFromIntent = intent.getStringExtra(MainActivity.YOUR_PHOTO_START);
 
 		ImgLoader imgLoader = new ImgLoader(this);
 		ImageView imageView = (ImageView) findViewById(R.id.imageView2);
 				
 		
 		try {
 			ExifInterface  exifInterface = new ExifInterface(fileFromIntent);
 			String exifOrie = exifInterface.getAttribute(ExifInterface.TAG_ORIENTATION);		
 			double rotation;
 			if (exifOrie.equals("6")) { rotation = 90.0; }
 			else if (exifOrie.equals("1")) { rotation = 0.0; }
 			else if (exifOrie.equals("3")) { rotation = 180.0; }
 			else if (exifOrie.equals("8")) { rotation = 270.0; }
 			else { rotation = 0.0; }
 			imageView.setRotation((float) rotation);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}		
 		
 		imgLoader.loadBitmap(fileFromIntent, imageView);
 
 		
 
 		textView = (TextView) findViewById(R.id.editText1);
 		//textView.getText();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_twoja_fotka, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// This ID represents the Home or Up button. In the case of this
 			// activity, the Up button is shown. Use NavUtils to allow users
 			// to navigate up one level in the application structure. For
 			// more details, see the Navigation pattern on Android Design:
 			//
 			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
 			//
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	public void zapiszZdjecie(View v) {
 		AddNoteTask addNoteTask = new AddNoteTask(this);
 		addNoteTask.execute(fileFromIntent);
 	}
 
 	class AddNoteTask extends AsyncTask<String, Integer, Long> {
 		private ProgressDialog progressDlg = null;
 		private Context mContext;
 
 		public AddNoteTask(Context context) {
 			this.mContext = context;
 		}
 
 		@Override
 		protected void onPreExecute() {
 			super.onPreExecute();
 			progressDlg = new ProgressDialog(mContext);
 			progressDlg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
 			progressDlg.setMessage(mContext.getString(R.string.save_img_msg));
 			progressDlg.setCancelable(true);
 			progressDlg.setMax(10);
 			progressDlg.setProgress(0);
 
 			progressDlg.setOnCancelListener(new OnCancelListener() {
 				public void onCancel(DialogInterface arg0) {
 					AddNoteTask.this.cancel(false);
 				}
 			});
 			progressDlg.show();
 		}
 
 		@SuppressLint("SimpleDateFormat")
 		@Override
 		protected Long doInBackground(String... params) {
 						
 			DbHelper mDbHelper = new DbHelper(mContext);
 			SQLiteDatabase db = mDbHelper.getWritableDatabase();
 
 			String dateTime = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
 					.format(new Date());
 			File aFile = new File(params[0]);
 			String id = aFile.getName();
 
 			File thumbDir = new File(aFile.getParent() + MainActivity.THUMB_DIR);
 			thumbDir.mkdirs();
 
 			String thumb = thumbDir.getPath() + File.separator + id;
 			String note = textView.getText().toString();
 			String location = "";
 
 			String[] exif = readExif(params[0]);	
 			
 			double rotation;
 			//1-normal; 3-rotate180; 8-rotate270; 6-rotate90;
 			
 			if (exif[0].equals("6")) { rotation = 90.0; }
 			else if (exif[0].equals("1")) { rotation = 0.0; }
 			else if (exif[0].equals("3")) { rotation = 180.0; }
 			else if (exif[0].equals("8")) { rotation = 270.0; }
 			else { rotation = 0.0; }
 			
 			publishProgress(3);
 
 			// Create a new map of values, where column names are the keys
 			ContentValues values = new ContentValues();
 			values.put(DbHelper.FeedSiteNote.COLUMN_NAME_ID, id);
 			values.put(DbHelper.FeedSiteNote.COLUMN_NAME_DATE_TIME, dateTime);
 			values.put(DbHelper.FeedSiteNote.COLUMN_NAME_FILE, params[0]);
 			values.put(DbHelper.FeedSiteNote.COLUMN_NAME_THUMB, thumb);
 			values.put(DbHelper.FeedSiteNote.COLUMN_NAME_NOTE, note);
 			values.put(DbHelper.FeedSiteNote.COLUMN_NAME_LOCATION, location);
 			values.put(DbHelper.FeedSiteNote.COLUMN_NAME_ROTATION, rotation);
 			values.put(DbHelper.FeedSiteNote.COLUMN_NAME_EXIF_ORIENTATION, exif[0]);
 			values.put(DbHelper.FeedSiteNote.COLUMN_NAME_EXIF_GPS_LATITUDE, exif[1]);
 			values.put(DbHelper.FeedSiteNote.COLUMN_NAME_EXIF_GPS_LATITUDEREF, exif[2]);
 			values.put(DbHelper.FeedSiteNote.COLUMN_NAME_EXIF_GPS_LONGITUDE, exif[3]);
 			values.put(DbHelper.FeedSiteNote.COLUMN_NAME_EXIF_GPS_LONGITUDEREF, exif[4]);			
 			publishProgress(5);
 			// Insert the new row, returning the primary key value of the new
 			// row
 
 			long newRowId = db.insert(DbHelper.FeedSiteNote.TABLE_NAME, null,
 					values);
 			publishProgress(7);
 
 			saveCompressBitmapAsThumb(params[0], thumb);
 			publishProgress(9);
 			
 			MainActivity.noteList.add(0, new DataSet(String.valueOf(newRowId), id,
 					dateTime, params[0], thumb, note, location, rotation, exif[0], exif[1], exif[2], exif[3], exif[4]));
 
 			return null;// newRowId;
 		}
 
 		@Override
 		protected void onPostExecute(Long result) {
 			super.onPostExecute(result);
 			if (progressDlg != null) {
 				progressDlg.dismiss();
 			}
 
 			Intent i = new Intent(mContext, MainActivity.class);
 			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			startActivity(i);
 		}
 
 		@Override
 		protected void onProgressUpdate(Integer... values) {
 			if (progressDlg != null) {
 				progressDlg.setProgress(values[0]);
 			}
 		}
 
 		private void saveCompressBitmapAsThumb(String photo, String thumb) {
 			BitmapFactory.Options opt = new BitmapFactory.Options();
 			opt.inSampleSize = 8;
 			Bitmap bitmap = BitmapFactory.decodeFile(photo, opt);
 
 			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
 			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
 
 			File f = new File(thumb);
 			try {
 				f.createNewFile();
 				// write the bytes in file
 				FileOutputStream fo = new FileOutputStream(f);
 				fo.write(bytes.toByteArray());
 				// remember close de FileOutput
 				fo.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		
 		private String[] readExif(String file){			
 			String[] exif = new String[5];
 			try {
 				ExifInterface exifInterface = new ExifInterface(file);
 				exif[0] = exifInterface.getAttribute(ExifInterface.TAG_ORIENTATION);
 				exif[1] = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
 				exif[2] = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
 				exif[3] = exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
 				exif[4] = exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);				
 			}
 			catch (Exception e) {				
 				e.printStackTrace();
 			}			
 			return exif; 
 		}		
 		
 	}
 
 }
