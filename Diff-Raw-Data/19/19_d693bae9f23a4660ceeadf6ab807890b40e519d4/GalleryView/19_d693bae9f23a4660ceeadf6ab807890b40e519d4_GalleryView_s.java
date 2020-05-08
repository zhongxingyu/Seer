 package ca.ilanguage.fielddbsessionrecorder;
 
 import java.io.File;
 
 import android.app.Activity;
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
 import android.graphics.Typeface;
 import android.graphics.drawable.Drawable;
 import android.media.ThumbnailUtils;
 import android.os.Bundle;
 import android.os.Environment;
 import android.provider.MediaStore.Video.Thumbnails;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.RelativeLayout.LayoutParams;
 import android.widget.TextView;
 
 public class GalleryView extends Activity {
 	File videosFolder;
 	private DatumsDbAdapter mDbHelper;
 	LinearLayout carouselLayout;
 	private static final int SESSION_LIST_VIEW_ID = Menu.FIRST;
 	private static final int NEW_SESSION_ID = Menu.FIRST + 1;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_gallery);
 
 		mDbHelper = new DatumsDbAdapter(this);
 		mDbHelper.open();
 
 		// Create video folder if it does not already exist
 		videosFolder = new File(Environment.getExternalStorageDirectory(),
 				"FieldDBSessions");
 		videosFolder.mkdir();
 		carouselLayout = (LinearLayout) findViewById(R.id.videoPreviewCarousel);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		menu.add(0, SESSION_LIST_VIEW_ID, 0, R.string.menu_view_session_list);
 		menu.add(0, NEW_SESSION_ID, 0, R.string.menu_new_session);
 		return true;
 	}
 
 	@Override
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
 		switch (item.getItemId()) {
 		case SESSION_LIST_VIEW_ID:
 			showSessionList();
 			return true;
 		case NEW_SESSION_ID:
 			createNote();
 			return true;
 		}
 		return super.onMenuItemSelected(featureId, item);
 	}
 
 	public void showSessionList() {
 		Intent i = new Intent(this, SessionListView.class);
 		startActivity(i);
 	}
 
 	private void createNote() {
 		Long id = mDbHelper.createNote("", "", "", "", "", "");
 		Intent i = new Intent(this, SessionAccess.class);
 		i.putExtra(DatumsDbAdapter.KEY_ROWID, id);
 		startActivity(i);
 	}
 
 	public void populateVideoPreview() {
 		File dir = Environment.getExternalStorageDirectory();
 		String SD_PATH = dir.getAbsolutePath() + "/FieldDBSessions";
 		File file = new File(SD_PATH);
 		File allVideos[] = file.listFiles();
 		if (allVideos.length > 0) {
 
 			ImageView[] imageViewArray = new ImageView[allVideos.length];
 			TextView[] textViewArray = new TextView[allVideos.length];
 			// Remove all images in view before updating
 			carouselLayout.removeAllViews();
 
 			// Check for device size to determine size of gallery images
 			boolean tabletSize = getResources().getBoolean(R.bool.isTablet);
 			for (int i = 0; i < allVideos.length; i++) {
 				String filePath = allVideos[i].getPath();
 				String[] filePathParts = filePath.split("\\.");
 				String[] filePathSubParts = filePathParts[0].split("_");
 				Long rowID = Long.parseLong(filePathSubParts[3]);
 
 				// Get goal for image label
 				Cursor note = mDbHelper.fetchNote(rowID);
 
 				String tempGoal = note.getString(note
 						.getColumnIndexOrThrow(DatumsDbAdapter.KEY_FIELD1));
 
 				String goal;
 				if (tempGoal.length() > 16) {
 					goal = tempGoal.substring(0, 15).concat("...");
 				} else {
 					goal = tempGoal;
 				}
 
 				String tempDate = note.getString(note
 						.getColumnIndexOrThrow(DatumsDbAdapter.KEY_FIELD5));
 
 				// TODO Format date
 				String imageLabelText = goal.concat("\n").concat(tempDate);
 
 				imageViewArray[i] = new ImageView(this);
 				textViewArray[i] = new TextView(this);
 
 				imageViewArray[i].setTag(filePath);
 
 				// Play video in PlayVideo activity on normal click
 				imageViewArray[i].setOnClickListener(new OnClickListener() {
 					@Override
 					public void onClick(View v) {
 						Intent takeNotes = new Intent(v.getContext(),
 								NoteTaking.class);
 						String[] filePathParts = v.getTag().toString()
 								.split("\\.");
 						String[] filePathSubParts = filePathParts[0].split("_");
 						Long rowID = Long.parseLong(filePathSubParts[3]);
 
 						takeNotes.putExtra("videoFilename", v.getTag()
 								.toString());
 						takeNotes.putExtra(DatumsDbAdapter.KEY_ROWID, rowID);
 						startActivity(takeNotes);
 					}
 				});
 
 				// Set up layout parameters
 				RelativeLayout imageAndTextRelativeLayout = new RelativeLayout(
 						this);
 				RelativeLayout.LayoutParams main_lp = new RelativeLayout.LayoutParams(
 						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
 				main_lp.addRule(RelativeLayout.CENTER_HORIZONTAL,
 						RelativeLayout.TRUE);
 				main_lp.addRule(RelativeLayout.CENTER_VERTICAL,
 						RelativeLayout.TRUE);
 
 				main_lp.setMargins(20, 20, 20, 20);
 				imageAndTextRelativeLayout.setLayoutParams(main_lp);
 
 				imageViewArray[i].setLayoutParams(main_lp);
 
 				RelativeLayout.LayoutParams text_lp = new RelativeLayout.LayoutParams(
 						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
 				text_lp.addRule(RelativeLayout.CENTER_HORIZONTAL,
 						RelativeLayout.TRUE);
 				text_lp.addRule(RelativeLayout.CENTER_VERTICAL,
 						RelativeLayout.TRUE);
 				textViewArray[i].setTextColor(Color.parseColor("#FFFFFF"));
 				textViewArray[i].setTypeface(Typeface.DEFAULT_BOLD);
 
 				// Variables set based on device size
 				Bitmap bmVideoPreview;
 				int radiusDegree;
 				if (tabletSize) {
 					bmVideoPreview = ThumbnailUtils
 							.createVideoThumbnail(allVideos[i].getPath(),
 									Thumbnails.FULL_SCREEN_KIND);
 					textViewArray[i].setTextSize(35);
 					radiusDegree = 50;
 				} else {
 					textViewArray[i].setTextSize(20);
 					radiusDegree = 75;
 					bmVideoPreview = ThumbnailUtils.createVideoThumbnail(
 							allVideos[i].getPath(), Thumbnails.MINI_KIND);
 				}
 				textViewArray[i].setShadowLayer(1.5f, -1, 1, Color.LTGRAY);
 				textViewArray[i].setLayoutParams(text_lp);
 
 				// Add data to elements
 				Bitmap roundedBitmap = getRoundedCornerBitmap(bmVideoPreview,
 						radiusDegree);
 				imageViewArray[i].setImageBitmap(roundedBitmap);
 				Drawable d = getResources()
 						.getDrawable(R.drawable.image_border);
 				imageViewArray[i].setBackground(d);
 
 				textViewArray[i].setText(imageLabelText);
 
 				// Add individual items to relative layout container
 				imageAndTextRelativeLayout.addView(imageViewArray[i]);
 				imageAndTextRelativeLayout.addView(textViewArray[i]);
 
 				// Add imageAndTextLinearLayout container to linear layout in
 				// view
 				carouselLayout.addView(imageAndTextRelativeLayout);
 			}
 		}
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
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		// Populate video preview list on load
 		try {
 			populateVideoPreview();
 		} catch (Exception e) {
			Log.v("TEST", "THERE WAS AN ERROR!");
 		}
 	}

 }
