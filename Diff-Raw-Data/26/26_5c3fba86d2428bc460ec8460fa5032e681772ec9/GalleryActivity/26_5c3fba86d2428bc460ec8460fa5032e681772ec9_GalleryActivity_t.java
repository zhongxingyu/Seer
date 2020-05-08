 package at.madexperts.logmynight;
 
 import java.io.File;
import java.util.Date;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Matrix;
 import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
 import android.os.Bundle;
import android.provider.MediaStore;
import android.text.format.DateFormat;
 import android.util.Log;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.GridView;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class GalleryActivity extends Activity implements OnClickListener {
 
 	private static final String TAG = GalleryActivity.class.getName();
 	SQLiteDatabase db;
 	GridView gallery;
 	Button button;
 	ImageAdapter adapter;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		db = new DatabaseHelper(this).getReadableDatabase();
 		setContentView(R.layout.gallery);
 
 		button = (Button) findViewById(R.id.galleryButton);
 		button.setOnClickListener(this);
 
 		gallery = (GridView) findViewById(R.id.gallery);
 		adapter = new ImageAdapter(this, Utilities.getImageDirectory());
 		gallery.setAdapter(adapter);
 	}
 
 	@Override
 	protected void onDestroy() {
 		db.close();
 		super.onDestroy();
 	}
 
 	public void onClick(View v) {
 		Intent intent = new Intent(this, CameraActivity.class);
 		startActivityForResult(intent, 0);
 	}
 
	public Intent useCameraIntent() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		String filename = DateFormat.format("yyyyMMdd-kkmmss", new Date()) + ".jpg";
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(Utilities.getImageDirectory(), filename)));
		return intent;
	}

 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		if (requestCode == 0) {
 			adapter.notifyDataSetChanged();
 		}
 	}
 
 	public class ImageAdapter extends BaseAdapter {
 		private Context mContext;
 		private File directory;
 
 		public ImageAdapter(Context c, File directory) {
 			mContext = c;
 			this.directory = directory;
 		}
 
 		public int getCount() {
 			return directory.listFiles().length;
 		}
 
 		public Object getItem(int position) {
 			return null;
 		}
 
 		public long getItemId(int position) {
 			return 0;
 		}
 
 		// create a new ImageView for each item referenced by the Adapter
 		public View getView(int position, View convertView, ViewGroup parent) {
 			View view;
 
 			if (convertView == null) { // if it's not recycled, initialize some
 				// attributes
 				view = getLayoutInflater().inflate(R.layout.galleryitem, null);
 
 			} else {
 				view = convertView;
 			}
 
 			File file = directory.listFiles()[position];
 
 			Cursor cursor = db.rawQuery(
 					"SELECT name, orientation FROM pics WHERE filename = ?",
 					new String[] { file.getName() });
			String name = "no name";
			int orientation = 0;
			if (cursor.moveToFirst()) {
				name = cursor.getString(cursor.getColumnIndex("name"));
				orientation = cursor.getInt(cursor
						.getColumnIndex("orientation"));
			}
 			Log.d(TAG, "Read name from db:" + name + " orientation: "
 					+ orientation);
 
 			ImageView imageView = (ImageView) view
 					.findViewById(R.id.galleryItemImage);
 			TextView textView = (TextView) view
 					.findViewById(R.id.galleryItemText);
 
 			view.setLayoutParams(new GridView.LayoutParams(120, 140));
 			imageView.setScaleType(ImageView.ScaleType.CENTER);
 			imageView.setPadding(8, 8, 8, 8);
 
 			Bitmap bm = Bitmap.createBitmap(BitmapFactory.decodeFile(file
 					.getAbsolutePath()));
 
 			// createa matrix for the manipulation
 			Matrix matrix = new Matrix();
 
 			float width = bm.getWidth();
 			float height = bm.getHeight();
 			float factor;
 			float newWidth;
 			float newHeight;
 			
 			factor = width / height;
 
 			newHeight = 105;
 
 			newWidth = factor * newHeight;
 			
 			// calculate the scale - in this case = 0.4f
 			float scaleWidth = newWidth / width;
 			float scaleHeight = newHeight / height;
 
 			// resize the bit map
 			matrix.postScale(scaleWidth, scaleHeight);
 
 			if (orientation == 1) {								
 				matrix.postRotate(90);
 			} else {
 				;
 			}
 
 			Log.d(TAG, "origWidth: " + width + " origHeight: " + height
 					+ " factor: " + factor);
 			Log.d(TAG, "newWidth: " + newWidth + " newHeight: " + newHeight);
 
 			
 
 			Bitmap rotatedBMP = Bitmap.createBitmap(bm, 0, 0, (int) width,
 					(int) height, matrix, true);
 			BitmapDrawable bmd = new BitmapDrawable(rotatedBMP);
 
 			imageView.setImageDrawable(bmd);
 
 			textView.setText(name);
 
 			return view;
 		}
 	}
 
 }
