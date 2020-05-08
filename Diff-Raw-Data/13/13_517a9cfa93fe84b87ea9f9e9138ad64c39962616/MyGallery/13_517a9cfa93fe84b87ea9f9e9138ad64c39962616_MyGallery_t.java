 /*
  * Copyright 2012 MITIAN Technology, Co., Ltd. All rights reserved.
  */
 package com.adtworker.mail;
 
 import java.lang.ref.SoftReference;
 import java.util.HashMap;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.content.pm.ActivityInfo;
 import android.graphics.Bitmap;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.WindowManager;
 import android.widget.AbsListView;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.BaseAdapter;
 import android.widget.GridView;
 import android.widget.ImageView;
 
 import com.adtworker.mail.constants.Constants;
 import com.adtworker.mail.util.AdUtils;
 
 /**
  * MyGallery.java
  * 
  * @author baojun
  */
 public class MyGallery extends Activity {
 	private static final String TAG = "MyGallery";
 	private ImageManager mImageManager;
 	private HashMap<Integer, SoftReference<Bitmap>> mDataCache;
 	private GridView mGridView;
 	private ImageAdapter mImageAdapter;
 	private SharedPreferences mSharedPref;
 	private BroadcastReceiver mBroadcastReceiver;
 
 	private int img_w, img_h;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.image_gallery);
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 				WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
 		mImageManager = ImageManager.getInstance();
 		mSharedPref = getSharedPreferences(WatchActivity.PREFERENCES,
 				Context.MODE_PRIVATE);
 		mDataCache = new HashMap<Integer, SoftReference<Bitmap>>();
 
 		mGridView = (GridView) findViewById(R.id.GridView);
 		mImageAdapter = new ImageAdapter(this);
 		mGridView.setAdapter(mImageAdapter);
 		mGridView.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View view,
 					int position, long id) {
 				if (position != ImageManager.INVALID_PIC_INDEX) {
 					setResult(position);
 					finish();
 				}
 			}
 		});
 
 		int pos = mImageManager.getCurrent();
 		if (pos != ImageManager.INVALID_PIC_INDEX) {
 			mGridView.setSelection(pos);
 		}
 		setResult(pos);
 
 		String tmpString = mSharedPref.getString(WatchActivity.PREF_NETIMG_RES,
 				"");
 		if (tmpString.isEmpty()) {
 			DisplayMetrics displayMetrics = WatchApp.getInstance()
 					.getResources().getDisplayMetrics();
 			img_w = displayMetrics.widthPixels;
 			img_h = displayMetrics.widthPixels;
 		} else {
 			String[] results = tmpString.split("x");
 			img_w = Integer.parseInt(results[0]);
 			img_h = Integer.parseInt(results[1]);
 		}
 
 		ViewGroup adLayout = (ViewGroup) findViewById(R.id.adLayout);
 		AdUtils.setupAdLayout(this, adLayout, false);
 	}
 	@Override
 	public void onStart() {
 		Log.v(TAG, "onStart()");
 		super.onStart();
 
 		if (mSharedPref.getBoolean(WatchActivity.PREF_AUTO_ROTATE, false)) {
 			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
 		} else {
 			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 		}
 
 		mBroadcastReceiver = new BroadcastReceiver() {
 			@Override
 			public void onReceive(Context context, Intent intent) {
 				int progress = intent.getIntExtra("progress3", -1);
 				int pos = intent.getIntExtra("fileId", -1);
 				if (pos != -1 && progress == 100) {
 					SoftReference<Bitmap> ref = mDataCache.get(pos);
 					if (ref != null && ref.get() != null) {
 						Bitmap bitmap = mImageManager.getPosBitmap(pos, true);
 						mDataCache.remove(pos);
 						mDataCache.put(pos, new SoftReference<Bitmap>(bitmap));
 						// mImageAdapter.notifyDataSetInvalidated();
 						mImageAdapter.notifyDataSetChanged();
 					}
 				}
 			}
 
 		};
 		registerReceiver(mBroadcastReceiver, new IntentFilter(
 				Constants.SET_PROGRESSBAR));
 	}
 
 	@Override
 	protected void onDestroy() {
 		Log.d(TAG, "onDestroy()");
 		GridView gridView = (GridView) findViewById(R.id.GridView);
 		int count = gridView.getCount();
 		for (int i = 0; i < count; i++) {
 			ImageView v = (ImageView) gridView.getChildAt(i);
 			if (v != null) {
 				if (v.getDrawable() != null)
 					v.getDrawable().setCallback(null);
 			}
 		}
 		for (int i = 0; i < mDataCache.size(); i++) {
 			SoftReference<Bitmap> ref = mDataCache.get(i);
 			if (ref != null && ref.get() != null) {
 				Bitmap b = ref.get();
 				b.recycle();
 				b = null;
 			}
 		}
 		mDataCache.clear();
 		unregisterReceiver(mBroadcastReceiver);
 		super.onDestroy();
 	}
 
 	private class ImageAdapter extends BaseAdapter {
 
 		private final Context mContext;
 		private final ImageManager mImageManager;
 
 		public ImageAdapter(Context context) {
 			mContext = context;
 			mImageManager = ImageManager.getInstance();
 		}
 
 		@Override
 		public int getCount() {
 			return mImageManager.getImageListSize();
 		}
 
 		@Override
 		public Drawable getItem(int position) {
 			return null;
 		}
 
 		@Override
 		public long getItemId(int position) {
 			return position;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			ImageView i = null;
 			if (convertView == null) {
 				i = new ImageView(mContext);
 			} else {
 				i = (ImageView) convertView;
 			}
 
 			DisplayMetrics displayMetrics = mContext.getResources()
 					.getDisplayMetrics();
 
 			Bitmap bitmap = null;
 			if (mDataCache.get(position) != null)
 				bitmap = mDataCache.get(position).get();
 
 			if (bitmap == null) {
 				bitmap = mImageManager.getPosBitmap(position, true);
 				mDataCache.put(position, new SoftReference<Bitmap>(bitmap));
 			}
 			i.setImageBitmap(bitmap);
 
 			mGridView.setNumColumns(3);
 			int width = displayMetrics.widthPixels / 3;
			int height = width * img_h / img_w;
			// if (bitmap != null) {
			// height = (int) (width / (float) bitmap.getWidth() * bitmap
			// .getHeight());
			// }
 			i.setLayoutParams(new AbsListView.LayoutParams(width, height));
 			i.setPadding(2, 2, 2, 2);
			if (bitmap != null && bitmap.getWidth() < 100)
 				i.setScaleType(ImageView.ScaleType.CENTER);
 			else
 				i.setScaleType(ImageView.ScaleType.FIT_CENTER);
 
 			return i;
 		}
 	}
 }
