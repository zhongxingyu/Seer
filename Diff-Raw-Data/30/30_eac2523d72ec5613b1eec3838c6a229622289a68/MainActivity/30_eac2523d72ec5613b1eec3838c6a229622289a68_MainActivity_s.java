 package com.example.overdraw;
 
 import java.util.Random;
 
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Handler.Callback;
 import android.os.Message;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.ListView;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
 
 public class MainActivity extends Activity {
 	private static final int[] sPhotos = {
 		R.drawable.p1, R.drawable.p2, R.drawable.p3, R.drawable.p4,
 		R.drawable.p5, R.drawable.p6, R.drawable.p7, R.drawable.p8,
 		R.drawable.p9, R.drawable.p10, R.drawable.p11, R.drawable.p12,
 		R.drawable.p13, R.drawable.p14, R.drawable.p15, R.drawable.p16,
 		R.drawable.p17
 	};
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.activity_main);
 
 		Bitmap[] photos = new Bitmap[sPhotos.length];
 		for (int i = 0; i < photos.length; i++) {
 			photos[i] = BitmapFactory.decodeResource(getResources(), sPhotos[i]);
 		}
 
         ListView listView = (ListView) findViewById(R.id.list);
 		listView.setAdapter(new PhotoAdapter(this, photos, listView));
     }
 
     private class PhotoAdapter extends ArrayAdapter<Bitmap> implements Callback {
     	private final Handler mHandler = new Handler(this);
     	private final Random mRandom = new Random();
 		private final ListView mListView;
 
 		private final ColorFilter[] mColorsFilters = new ColorFilter[] {
 				new PorterDuffColorFilter(0xbbff0000, PorterDuff.Mode.DARKEN),
 				new PorterDuffColorFilter(0xbb00ff00, PorterDuff.Mode.DARKEN),
 				new PorterDuffColorFilter(0xbb0000ff, PorterDuff.Mode.DARKEN),
 				new PorterDuffColorFilter(0xbbffff00, PorterDuff.Mode.DARKEN),
 		};
 
 		public PhotoAdapter(Context context, Bitmap[] bitmaps, ListView listView) {
     		super(context, 0, bitmaps);
 			mListView = listView;
     	}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			if (convertView == null) {
 				convertView = LayoutInflater.from(getContext()).inflate(R.layout.photo_item, parent, false);
 			}
 
 			convertView.setTag(position);
 
 			ImageView image = (ImageView) convertView.findViewById(R.id.photo);
			image.getBackground().setColorFilter(mColorsFilters[mRandom.nextInt(mColorsFilters.length)]);
 
 			LayoutParams layoutParams = image.getLayoutParams();
 			layoutParams.height = getResources().getDimensionPixelSize(R.dimen.placeholder_height);
 
			image.setImageDrawable(null);
 
 			// Simulate slow loading on a separate thread
 			mHandler.sendMessageDelayed(Message.obtain(mHandler, position),
 					1000 + mRandom.nextInt(1000));
 
 			return convertView;
 		}
 
 		@Override
 		public boolean handleMessage(Message msg) {
 			int first = mListView.getFirstVisiblePosition();
 			int count = mListView.getChildCount();
 
 			// The position is in the visible range, perform binding
 			if (msg.what >= first && msg.what < first + count) {
 				ImageView image = (ImageView) mListView.getChildAt(msg.what - first).findViewById(R.id.photo);
 
 				LayoutParams layoutParams = image.getLayoutParams();
 				layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
 
 				image.setImageBitmap(getItem(msg.what));
 			}
 
 			// Always handle
 			return true;
 		}
     }
 }
