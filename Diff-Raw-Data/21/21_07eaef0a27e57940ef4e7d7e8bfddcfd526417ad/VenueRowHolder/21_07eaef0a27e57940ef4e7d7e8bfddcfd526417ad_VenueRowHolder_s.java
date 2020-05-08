 package com.ibc.view;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.os.AsyncTask;
 import android.text.Html;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 import com.ibc.R;
 import com.ibc.model.service.response.VenuesResponse;
 import com.ibc.util.Config;
 
 public class VenueRowHolder {
 	
 	private View _root;
 	private VenuesResponse _data;
 	private ImageView _img;
 	private TextView _title;
 	private TextView _city;
 	private ProgressBar _progress;
 	
 	private Context _context;
 	
 	private HttpURLConnection _connection;
 	boolean _connecting;
 	Bitmap _bitmap;
 	private String _id;
 	
 	public VenueRowHolder(View root , VenuesResponse data, Context context) {
 		_root = root;
 		_data = data;
 		_context = context;
 		_img = (ImageView) _root.findViewById(R.id.img);
 		_title = (TextView) _root.findViewById(R.id.title);
 		_city = (TextView) _root.findViewById(R.id.city);
 		_progress = (ProgressBar) _root.findViewById(R.id.progress);
 	}
 	
 	public void display() {
 		_title.setText(_data.venuesName);
 		if (_city != null) {
 			if (null != _data && _data.city != null) {
 				_city.setText(Html.fromHtml(_data.city));
 			} else {
				_city.setText("Barcelona");
 			}
 		}
 		getImage();
 	}
 	
 	private void getImage() {
 		_id = _data.venuesCode;
 		String dir = _context.getCacheDir() + "/" + _id + "_vn.png";
 		File file = new File(dir);
 		if (file.exists()) {
 			BitmapFactory.Options options = new BitmapFactory.Options();
 //			options.inTempStorage = new byte[16 * 1024];
 //			options.inSampleSize = 4;
 			_bitmap = BitmapFactory.decodeFile(dir);
 			_img.setImageBitmap(BitmapFactory.decodeFile(dir,options));
 			_progress.setVisibility(View.INVISIBLE);
 		} else {
 			if (_connecting) {
 				return;
 			} else {
 				_connecting = true;
 				new DownloadTask().execute(new String[] {
 						Config.URL_IMAGE + _data.icon, "" });
 			}
 		}
 	}
 	
 	/**
 	 * 
 	 * Get image processing
 	 */
 	
 	public boolean isConnecting() {
 		return _connecting;
 	}
 
 	private void cleanUp() {
 		if (_connection != null) {
 			try {
 				_connection.disconnect();
 			} catch (Exception ex) {
 			}
 			_connection = null;
 		}
 		_connecting = false;
 	}
 	
 	
 	protected void finalize() throws Throwable {
 		if (_connecting)
 			cleanUp();
 		super.finalize();
 	}
 	
 	public class DownloadTask extends AsyncTask<String, Void, Bitmap> {
 		// SEND REQUEST
 		@Override
 		protected Bitmap doInBackground(String... urls) {
 			// TODO Auto-generated method stub
 			try {
 				URL url = new URL(urls[0]);
 				_connection = (HttpURLConnection) url.openConnection();
 				_connection.setRequestMethod("GET");
 				_connection.setDoInput(true);
 				_connection.setDoOutput(true);
 				int httpCode = _connection.getResponseCode();
 				if (httpCode == HttpURLConnection.HTTP_OK) {
 					InputStream in = new BufferedInputStream(
 							_connection.getInputStream());
 					return BitmapFactory.decodeStream(in);
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			} finally {
 				cleanUp();
 			}
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Bitmap buff) {
 			super.onPostExecute(buff);
 			if (_progress != null) {
 				_progress.setVisibility(View.INVISIBLE);
 			}
 			if (buff != null) {
 				try {
 					String dir = _context.getCacheDir() + "/" + _id + "_vn.png";
 					FileOutputStream out = new FileOutputStream(dir);
 					buff.compress(Bitmap.CompressFormat.PNG, 100, out);
 					out.flush();
 					out.close();
 					Drawable drawable = new BitmapDrawable(buff);
 					_img.setBackgroundDrawable(drawable);
 					
 				} catch (Exception e) {
 					e.printStackTrace();
 				} finally {
 					cleanUp();
 				}
 
 			}
 		}
 	}
 }
