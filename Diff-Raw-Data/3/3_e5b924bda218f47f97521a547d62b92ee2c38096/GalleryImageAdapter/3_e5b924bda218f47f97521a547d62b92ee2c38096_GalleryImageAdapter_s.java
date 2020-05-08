 package com.androidmontreal.weddingvideoguestbook;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.Bitmap.Config;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.PorterDuff.Mode;
 import android.graphics.PorterDuffXfermode;
 import android.graphics.Rect;
 import android.graphics.RectF;
 import android.graphics.drawable.Drawable;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class GalleryImageAdapter extends BaseAdapter {
 	public String TAG = PrivateConstants.TAG;
 	private Context context;
 	private final ArrayList<Bitmap> bitmaps;
 	private final ArrayList<Long> rowIds;
 	private final ArrayList<String> fileNames;
 
 	private DatumsDbAdapter mDbHelper;
 	private Long rowId;
 
 	public GalleryImageAdapter(Context context, ArrayList<Bitmap> bitmaps,
 			ArrayList<Long> rowIds, ArrayList<String> fileNames) {
 		this.context = context;
 		this.bitmaps = bitmaps;
 		this.rowIds = rowIds;
 		this.fileNames = fileNames;
 	}
 
 	public View getView(int position, View convertView, ViewGroup parent) {
 
 		LayoutInflater inflater = (LayoutInflater) context
 				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
 		View gridView;
 
 		if (convertView == null) {
 
 			gridView = new View(context);
 			gridView = inflater.inflate(R.layout.gallery_view_image, null);
 
 			rowId = rowIds.get(position);
 			mDbHelper = new DatumsDbAdapter(context);
 			mDbHelper.open();
 			Cursor note = mDbHelper.fetchNote(rowId);
 			mDbHelper.close();
 			if (note == null) {
 				return null;
 			}
 
 			// Get goal for image label
 			String tempGoal;
 			String goal;
 			String tempDate;
 			String imageLabelText;
 
 			try {
 				tempGoal = note.getString(note
 						.getColumnIndexOrThrow(DatumsDbAdapter.KEY_FIELD1));
 				tempDate = note.getString(note
 						.getColumnIndexOrThrow(DatumsDbAdapter.KEY_FIELD5));
 
 				if (tempGoal.length() > 16) {
 					goal = tempGoal.substring(0, 15).concat("...");
 				} else {
 					goal = tempGoal;
 				}
 
 				// TODO Format date
 				imageLabelText = goal.concat("\n").concat(tempDate);
 
 			} catch (Exception e) {
 				return null;
 			}
 
 			Bitmap roundedThumbnail = getRoundedCornerBitmap(
 					bitmaps.get(position), 30);
 			Drawable d = context.getResources().getDrawable(
 					R.drawable.image_border);
 
 			// Set value of textview
 			TextView textView = (TextView) gridView
 					.findViewById(R.id.grid_item_label);
 			// Variables set based on device size
 			boolean tabletSize = context.getResources().getBoolean(
 					R.bool.isTablet);
 			if (tabletSize) {
 				textView.setTextSize(35);
 			} else {
 				textView.setTextSize(20);
 			}
 			textView.setShadowLayer(10f, 0, 0, Color.parseColor("#000000"));
 			textView.setText(imageLabelText);
 
 			// Set image
 			ImageView imageView = (ImageView) gridView
 					.findViewById(R.id.grid_item_image);
 
 			imageView.setImageBitmap(roundedThumbnail);
			imageView.setBackground(d);
 			imageView.setTag(R.id.VIDEO_FILENAME_TAG_KEY,
 					fileNames.get(position));
 			imageView.setOnClickListener(new View.OnClickListener() {
 				public void onClick(View v) {
 					Intent accessSession = new Intent(v.getContext(),
 							SessionAccess.class);
 					accessSession.putExtra("videoFilename",
 							v.getTag(R.id.VIDEO_FILENAME_TAG_KEY).toString());
 					accessSession.putExtra(DatumsDbAdapter.KEY_ROWID, rowId);
 					context.startActivity(accessSession);
 				}
 			});
 
 		} else {
 			gridView = (View) convertView;
 		}
 
 		return gridView;
 	}
 
 	@Override
 	public int getCount() {
 		return bitmaps.size();
 	}
 
 	@Override
 	public Object getItem(int position) {
 		return null;
 	}
 
 	@Override
 	public long getItemId(int position) {
 		return 0;
 	}
 
 	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
 		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
 				bitmap.getHeight(), Config.ARGB_8888);
 		Canvas canvas = new Canvas(output);
 
 		final int color = 0xff424242;
 		final Paint paint = new Paint();
 		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
 		final RectF rectF = new RectF(rect);
 		final float roundPx = pixels;
 
 		paint.setAntiAlias(true);
 		canvas.drawARGB(0, 0, 0, 0);
 		paint.setColor(color);
 		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
 
 		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
 		canvas.drawBitmap(bitmap, rect, rect, paint);
 
 		return output;
 	}
 
 }
