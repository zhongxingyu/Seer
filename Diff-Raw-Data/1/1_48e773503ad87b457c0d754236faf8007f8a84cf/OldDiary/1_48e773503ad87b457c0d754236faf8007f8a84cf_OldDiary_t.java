 package com.android.Oasis.diary;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import android.app.Activity;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Color;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Parcelable;
 import android.support.v4.view.PagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.ImageView.ScaleType;
 import android.widget.LinearLayout;
 import android.widget.LinearLayout.LayoutParams;
 import android.widget.ScrollView;
 import android.widget.TextView;
 
 import com.android.Oasis.Main;
 import com.android.Oasis.MySQLite;
 import com.android.Oasis.R;
 import com.android.Oasis.life.Life;
 import com.android.Oasis.recent.Recent;
 import com.android.Oasis.story.Story;
 
 public class OldDiary extends Activity {
 
 	ArrayList<HashMap<String, Object>> array = new ArrayList<HashMap<String, Object>>();
 
 	int PLANT = 0;
 
 	final int TAKE_PICTURE = 12345;
 	final int SELECT_PICTURE = 54321;
 	private Uri imageUri = null;
 	private File tmpPhoto;
 	private Uri pictureUri = null;
 	boolean isFromAlbum = false;
 
 	private ViewPager viewPager;
 	private Context cxt;
 	private pagerAdapter pageradapter;
 
 	Intent intent = new Intent();
 	Bundle bundle = new Bundle();
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.olddiary);
 		cxt = this;
 
 		bundle = this.getIntent().getExtras();
 		PLANT = bundle.getInt("plant");
 
 		loadFromDb();
 
 		pageradapter = new pagerAdapter();
 		viewPager = (ViewPager) findViewById(R.id.pager);
 		viewPager.setAdapter(pageradapter);
 
 		ImageButton btn_new = (ImageButton) findViewById(R.id.diary_btn_new);
 		btn_new.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				registerForContextMenu(arg0);
 				openContextMenu(arg0);
 			}
 		});
 
 		ImageButton btn_story = (ImageButton) findViewById(R.id.main_btn_story);
 		btn_story.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				intent.putExtras(bundle);
 				intent.setClass(OldDiary.this, Story.class);
 				startActivity(intent);
 				System.gc();
 				OldDiary.this.finish();
 			}
 		});
 
 		ImageButton btn_recent = (ImageButton) findViewById(R.id.main_btn_recent);
 		btn_recent.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				intent.putExtras(bundle);
 				intent.setClass(OldDiary.this, Recent.class);
 				startActivity(intent);
 				System.gc();
 				OldDiary.this.finish();
 			}
 		});
 
 		ImageButton btn_life = (ImageButton) findViewById(R.id.main_btn_life);
 		btn_life.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Uri uri = Uri.parse(OldDiary.this.getResources().getString(R.string.fb_url));
 				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
 				startActivity(intent);
 			}
 		});
 
 	}
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenuInfo menuInfo) {
 		View view = LayoutInflater.from(v.getContext()).inflate(
 				R.layout.ui_contextmenu_header, null);
 		TextView txt_header = (TextView) view
 				.findViewById(R.id.ui_contextmenu_headertextview);
 		txt_header.setText("新增日記相片");
 		menu.setHeaderView(view);
 		menu.add(0, 0, 0, "從相簿");
 		menu.add(0, 1, 1, "從相機");
 		super.onCreateContextMenu(menu, v, menuInfo);
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		imageUri = null;
 		if (item.getItemId() == 0) {
 			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
 			intent.setType("image/*");
 			startActivityForResult(intent, SELECT_PICTURE);
 		} else if (item.getItemId() == 1) {
 			Intent intent = new Intent(
 					android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
 			tmpPhoto = new File(Environment.getExternalStorageDirectory(),
 					System.currentTimeMillis() + ".png");
 			intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,
 					Uri.fromFile(tmpPhoto));
 			imageUri = Uri.fromFile(tmpPhoto);
 			startActivityForResult(intent, TAKE_PICTURE);
 		}
 		return super.onContextItemSelected(item);
 	}
 
 	protected void makeDuplicatePicture(Uri originalUri) throws IOException,
 			URISyntaxException {
 
 		File cacheDir = getCacheDir(); // get cache dir
 		File picture = new File(cacheDir.getAbsolutePath() + File.separator
 				+ System.currentTimeMillis() + ".png"); // new file
 
 		InputStream is = this.getContentResolver().openInputStream(originalUri);
 
 		BitmapFactory.Options options = new BitmapFactory.Options();
 		options.inJustDecodeBounds = true;
 		Bitmap bmp = BitmapFactory.decodeStream(is, null, options);
 
 		int mySize = 800;
 		if (options.outWidth <= mySize)
 			options.inSampleSize = 1;
 		else if (options.outWidth <= 2 * mySize)
 			options.inSampleSize = 2;
 		else if (options.outWidth <= 4 * mySize)
 			options.inSampleSize = 4;
 		else
 			options.inSampleSize = 8;
 
 		is = this.getContentResolver().openInputStream(originalUri);
 		options.inJustDecodeBounds = false;
 		bmp = BitmapFactory.decodeStream(is, null, options);
 
 		// if (picture.exists()) { // delete old file in cache
 		// picture.delete();
 		// }
 		FileOutputStream fos = new FileOutputStream(picture);
 		bmp.compress(Bitmap.CompressFormat.JPEG, 85, fos);
 		bmp.recycle();
 
 		is.close();
 		fos.close();
 
 		pictureUri = Uri.fromFile(picture);
 
 		/*
 		 * if (isFromAlbum == false) {
 		 * android.provider.MediaStore.Images.Media.insertImage(
 		 * getContentResolver(), bmp, "", "");
 		 * 
 		 * sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
 		 * Uri.parse("file://" + Environment.getExternalStorageDirectory()))); }
 		 */
 	}
 
 	void check() {
 		if (pictureUri != null) {
 			// Bundle bundle = new Bundle();
 			String tmp = pictureUri.toString();
 			bundle.putString("uri", tmp);
 			bundle.putInt("plant", PLANT);
 			Intent intent = new Intent();
 			intent.putExtras(bundle);
 			intent.setClass(OldDiary.this, NewDiary.class);
 			startActivity(intent);
 		}
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		switch (requestCode) {
 		case TAKE_PICTURE:
 			if (resultCode == Activity.RESULT_OK) {
 
 				Uri selectedImage = imageUri;
 				getContentResolver().notifyChange(selectedImage, null);
 				isFromAlbum = false;
 				System.gc(); // run gc for sure that there's enough memory
 				try {
 					if (tmpPhoto.length() > 0l) {
 						pictureUri = imageUri;
 						makeDuplicatePicture(imageUri);
 						tmpPhoto.delete();
 						System.gc();
 					} else {
 						makeDuplicatePicture(data.getData());
 						System.gc();
 					}
 				} catch (Exception e) {
 					// Toast.makeText(this, R.string.toast_failedtoload,
 					// Toast.LENGTH_SHORT).show();
 				}
 
 			}
 			check();
 			break;
 
 		case SELECT_PICTURE:
 			if (resultCode == Activity.RESULT_OK) {
 
 				isFromAlbum = true;
 				System.gc(); // run gc for sure that there's enough memory
 				try {
 					makeDuplicatePicture(data.getData());
 					System.gc();
 				} catch (IOException e) {
 					break;
 				} catch (URISyntaxException e) {
 					break;
 				}
 			}
 			check();
 			break;
 		case 1:
 			// startActivity(intent);
 			break;
 		default:
 			super.onActivityResult(requestCode, resultCode, data);
 
 		}
 	}
 
 	private class pagerAdapter extends PagerAdapter {
 
 		@Override
 		public int getCount() {
 			// return NUM_VIEWS;
 			return (int) Math.ceil((double) array.size() / 8.0);
 		}
 
 		/**
 		 * Create the page for the given position. The adapter is responsible
 		 * for adding the view to the container given here, although it only
 		 * must ensure this is done by the time it returns from
 		 * {@link #finishUpdate()}.
 		 * 
 		 * @param container
 		 *            The containing View in which the page will be shown.
 		 * @param position
 		 *            The page position to be instantiated.
 		 * @return Returns an Object representing the new page. This does not
 		 *         need to be a View, but can be some other container of the
 		 *         page.
 		 */
 		@Override
 		public Object instantiateItem(View collection, int position) {
 
 			DisplayMetrics displaymetrics = new DisplayMetrics();
 			getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
 			// int height = displaymetrics.heightPixels;
 			int width = displaymetrics.widthPixels;
 
 			ScrollView sv = new ScrollView(cxt);
 			LinearLayout ll = new LinearLayout(cxt);
 			ll.setOrientation(LinearLayout.VERTICAL);
            ll.removeAllViews();
 
 			ImageView iv1 = new ImageView(cxt);
 			ImageView iv2 = new ImageView(cxt);
 
 			iv1.setImageDrawable(OldDiary.this.getResources().getDrawable(
 					R.drawable.diary_rope_top));
 			iv2.setImageDrawable(OldDiary.this.getResources().getDrawable(
 					R.drawable.diary_rope_bottom));
 
 			ll.addView(iv1);
 
 			int i;
 			LinearLayout photoropeup = new LinearLayout(cxt);
 			LinearLayout photodateup = new LinearLayout(cxt);
 			// photoropeup.setGravity(Gravity.CENTER_HORIZONTAL);
 			LinearLayout photoropebottom = new LinearLayout(cxt);
 			LinearLayout photodatebottom = new LinearLayout(cxt);
 			// photoropebottom.setGravity(Gravity.CENTER_HORIZONTAL);
 
 			int padding = 8;
 			if (width > 490) {
 				padding = (width - 480) / 5;
 			}
 
 			for (i = 0; i < 4; i++) {
 				Log.wtf("jizz", "i have big belly!");
 				if (position * 8 + i >= array.size())
 					break;
 
 				HashMap<String, Object> map = new HashMap<String, Object>();
 				map = array.get(position * 8 + i);
 
 				final Uri uri = Uri.parse(map.get("path").toString());
 
 				Uri uri_t = Uri.parse(map.get("thumb").toString());
 				Bitmap img = null;
 				ContentResolver vContentResolver = getContentResolver();
 				try {
 					img = BitmapFactory.decodeStream(vContentResolver
 							.openInputStream(uri_t));
 				} catch (FileNotFoundException e) {
 					e.printStackTrace();
 				}
 
 				ImageView myImageView = new ImageView(cxt);
 				myImageView.setImageBitmap(img);
 				// img.recycle();
 				myImageView.setAdjustViewBounds(true);
 				myImageView.setScaleType(ScaleType.CENTER_INSIDE);
 				myImageView.setMaxWidth(width / 4 - 10);
 				myImageView.setPadding(padding / 2, 0, padding / 2, 0);
 
 				TextView myTextView = new TextView(cxt);
 				myTextView.setText(map.get("date").toString());
 				myTextView.setTextColor(Color.BLACK);
 				myTextView.setTextSize(13);
 				myTextView.setGravity(Gravity.CENTER_HORIZONTAL);
 				myTextView.setWidth(width / 4 - 10);
 				myTextView.setPadding(padding / 2, 0, padding / 2, 0);
 
 				final int id = Integer.parseInt(map.get("db_id").toString());
 
 				myImageView.setOnClickListener(new OnClickListener() {
 					@Override
 					public void onClick(View v) {
 						// Bundle bundle = new Bundle();
 						bundle.putBoolean("ismine", true);
 						bundle.putString("path", uri.toString());
 						bundle.putInt("db_id", id);
 						Intent intent = new Intent();
 						intent.putExtras(bundle);
 						intent.setClass(OldDiary.this, BrowseDiary.class);
 						System.gc();
 						startActivity(intent);
 					}
 				});
 				photoropeup.addView(myImageView);
 				photodateup.addView(myTextView);
 			}
 			if (i == 4) {
 				photoropeup.setGravity(Gravity.CENTER_HORIZONTAL);
 				photodateup.setGravity(Gravity.CENTER_HORIZONTAL);
 			} else {
 				photoropeup.setPadding(padding, 0, padding, 0);
 				photodateup.setPadding(padding, 0, padding, 0);
 			}
 			for (i = 4; i < 8; i++) {
 
 				if (position * 8 + i >= array.size())
 					break;
 
 				HashMap<String, Object> map = new HashMap<String, Object>();
 				map = array.get(position * 8 + i);
 
 				final Uri uri = Uri.parse(map.get("path").toString());
 
 				Uri uri_t = Uri.parse(map.get("thumb").toString());
 
 				Bitmap img = null;
 				ContentResolver vContentResolver = getContentResolver();
 				try {
 					img = BitmapFactory.decodeStream(vContentResolver
 							.openInputStream(uri_t));
 				} catch (FileNotFoundException e) {
 					e.printStackTrace();
 				}
 				ImageView myImageView = new ImageView(cxt);
 				myImageView.setImageBitmap(img);
 				// img.recycle();
 				myImageView.setAdjustViewBounds(true);
 				myImageView.setScaleType(ScaleType.CENTER_INSIDE);
 				myImageView.setMaxWidth(width / 4 - 10);
 				myImageView.setPadding(padding / 2, 0, padding / 2, 0);
 
 				TextView myTextView = new TextView(cxt);
 				myTextView.setText(map.get("date").toString());
 				myTextView.setTextColor(Color.BLACK);
 				myTextView.setTextSize(13);
 				myTextView.setGravity(Gravity.CENTER_HORIZONTAL);
 				myTextView.setWidth(width / 4 - 10);
 				myTextView.setPadding(padding / 2, 0, padding / 2, 0);
 
 				
 				final int id = Integer.parseInt(map.get("db_id").toString());
 
 				myImageView.setOnClickListener(new OnClickListener() {
 					@Override
 					public void onClick(View v) {
 						// Bundle bundle = new Bundle();
 						bundle.putBoolean("ismine", true);
 						bundle.putString("path", uri.toString());
 						bundle.putInt("db_id", id);
 						Intent intent = new Intent();
 						intent.putExtras(bundle);
 						intent.setClass(OldDiary.this, BrowseDiary.class);
 						System.gc();
 						startActivity(intent);
 					}
 				});
 				
 				photoropebottom.addView(myImageView);
 				photodatebottom.addView(myTextView);
 			}
 			if (i == 8) {
 				photoropebottom.setGravity(Gravity.CENTER_HORIZONTAL);
 				photodatebottom.setGravity(Gravity.CENTER_HORIZONTAL);
 			} else {
 				photoropebottom.setPadding(padding, 0, padding, 0);
 				photodatebottom.setPadding(padding, 0, padding, 0);
 			}
 			// if(photoropeup.getChildCount()<4)
 			// ll.setGravity(Gravity.LEFT);
 			// if(photoropebottom.getChildCount()<4)
 			// ll.setGravity(Gravity.LEFT);
 			ll.addView(photoropeup);
 			ll.addView(photodateup);
 			ll.addView(iv2);
 			ll.addView(photoropebottom);
 			ll.addView(photodatebottom);
 			sv.addView(ll);
 			((ViewPager) collection).addView(sv, 0);
 			// ((ViewPager) collection).addView(ll, 0);
 
 			return sv;
 		}
 
 		/**
 		 * Remove a page for the given position. The adapter is responsible for
 		 * removing the view from its container, although it only must ensure
 		 * this is done by the time it returns from {@link #finishUpdate()}.
 		 * 
 		 * @param container
 		 *            The containing View from which the page will be removed.
 		 * @param position
 		 *            The page position to be removed.
 		 * @param object
 		 *            The same object that was returned by
 		 *            {@link #instantiateItem(View, int)}.
 		 */
 		@Override
 		public void destroyItem(View collection, int position, Object view) {
 			((ViewPager) collection).removeView((ScrollView) view);
 		}
 
 		@Override
 		public boolean isViewFromObject(View view, Object object) {
 			return view == ((ScrollView) object);
 		}
 
 		/**
 		 * Called when the a change in the shown pages has been completed. At
 		 * this point you must ensure that all of the pages have actually been
 		 * added or removed from the container as appropriate.
 		 * 
 		 * @param container
 		 *            The containing View which is displaying this adapter's
 		 *            page views.
 		 */
 		@Override
 		public void finishUpdate(View arg0) {
 		}
 
 		@Override
 		public void restoreState(Parcelable arg0, ClassLoader arg1) {
 		}
 
 		@Override
 		public Parcelable saveState() {
 			return null;
 		}
 
 		@Override
 		public void startUpdate(View arg0) {
 		}
 
 	}
 
 	private void loadFromDb() {
 
 		array.clear();
 
 		int db_id = 0;
 		int plant = 0;
 		String path = "";
 		String date = "";
 		String thumb = "";
 		String content ="";
 
 		MySQLite db = new MySQLite(OldDiary.this);
 		Cursor cursor = db.getPlant(PLANT);
 		int rows_num = cursor.getCount();
 
 		// cursor.moveToFirst();
 		cursor.moveToLast();
 		for (int i = 0; i < rows_num; i++) {
 			db_id = cursor.getInt(0);
 			plant = cursor.getInt(1);
 			path = cursor.getString(2);
 			date = cursor.getString(3);
 			thumb = cursor.getString(4);
 			content = cursor.getString(5);
 
 			HashMap<String, Object> map = new HashMap<String, Object>();
 			map.put("db_id", db_id);
 			map.put("plant", plant);
 			map.put("path", path);
 			map.put("date", date);
 			map.put("thumb", thumb);
 			array.add(map);
 
 			cursor.moveToPrevious();
 		}
 		cursor.close();
 		db.close();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		loadFromDb();
 		pageradapter = new pagerAdapter();
 		viewPager = (ViewPager) findViewById(R.id.pager);
 		viewPager.setAdapter(pageradapter);
 	}
 
 }
