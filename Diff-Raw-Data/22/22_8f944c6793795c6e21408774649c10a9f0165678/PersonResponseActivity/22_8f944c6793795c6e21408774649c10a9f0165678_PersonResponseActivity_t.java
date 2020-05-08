 package me.unsharable.activities;
 
 import java.io.BufferedInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 
 import me.unshareable.R;
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.BaseAdapter;
 import android.widget.Gallery;
 import android.widget.ImageView;
 import android.widget.Toast;
 
 public class PersonResponseActivity extends Activity {
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
		setContentView(R.layout.person_response);
 
 		Gallery gallery = (Gallery) findViewById(R.id.gallery);
 		gallery.setAdapter(new ImageAdapter(this));
 
 		gallery.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView parent, View v, int position, long id) {
 				Toast.makeText(PersonResponseActivity.this, "" + position, Toast.LENGTH_SHORT).show();
 			}
 		});
 	}
 
 	public class ImageAdapter extends BaseAdapter {
 		private ArrayList<String> imageURLs = new ArrayList<String>();
 		private Context myContext;
 		
 		public ImageAdapter(Context c) {
 			myContext = c;
 			retrieveURLs();
 		}
 		
 		//TODO
 		// Retrieve imageURLs from facebook
 		// and fill out imageURLs
 		public void retrieveURLs() {
 			
 		}
 
 		public int getCount() {
 			return imageURLs.size();
 		}
 
 		public Object getItem(int position) {
 			return position;
 		}
 
 		public long getItemId(int position) {
 			return position;
 		}
 
 		public View getView(int position, View convertView, ViewGroup parent) {
 			ImageView iview = new ImageView(this.myContext);
 			try {
 				URL imgurl = new URL(imageURLs.get(position));
 				URLConnection conn = imgurl.openConnection();
 				conn.connect();
 				InputStream is = conn.getInputStream();
 				BufferedInputStream bis = new BufferedInputStream(is);
 				Bitmap bmp = BitmapFactory.decodeStream(bis);
 				bis.close();
 				is.close();
 				iview.setImageBitmap(bmp);
 			} catch (IOException ioe) {
 				//Img Loading failed
 				//iview.setImageResource(R.drawable.error);
 				Log.e("DBG", "Remote Image Exception", ioe);
 			}
 			return iview;
 		}
 
 		public float getScale(boolean focused, int offset) {
 			return Math.max(0, 1.0f / (float)Math.pow(2, Math.abs(offset)));
 		}
 	}
 }
