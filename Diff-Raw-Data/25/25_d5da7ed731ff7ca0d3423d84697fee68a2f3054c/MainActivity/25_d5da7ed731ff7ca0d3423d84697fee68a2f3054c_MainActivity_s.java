 package vnd.blueararat.Effoto;
 
 import java.io.BufferedOutputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.pm.ActivityInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.BitmapFactory.Options;
 import android.graphics.BlurMaskFilter;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Matrix;
 import android.graphics.Paint;
 import android.graphics.PointF;
 import android.graphics.PorterDuff;
 import android.graphics.Rect;
 import android.graphics.RectF;
 import android.graphics.Typeface;
 import android.graphics.drawable.BitmapDrawable;
 import android.media.ExifInterface;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Environment;
 import android.preference.PreferenceManager;
 import android.provider.MediaStore;
 import android.support.v4.app.ShareCompat;
 import android.util.FloatMath;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.EditText;
 import android.widget.FrameLayout;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.RelativeLayout;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.SlidingDrawer;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainActivity extends Activity implements OnTouchListener {
 	public String getFilePath() {
 		return mFilePath;
 	}
 
 	private FrameLayout mShareFrame, mLoadFrame;
 
 	private MainView mImageView;
 	private SlidingDrawer mSlidingDrawer;
 	private String mFileName = "default";
 	private String mFilePath;
 	private Uri mUri;
 	private File mResultFile;
 	private List<Effect> list = new ArrayList<Effect>();
 	private Effect mCurEffect;
 	public int index;
 	private RectF mViewRect;
 	private FilenameFilter mFilenameFilter;
 
 	private Intent mShareIntent;
 
 	private ListView mListView, mLoadListView;
 	private ListView mAddEffectListView;
 	private FrameLayout mAddEffectFrame;
 
 	private Matrix matrix = new Matrix();
 	private Matrix savedMatrix = new Matrix();
 	private Bitmap bmp1;// , bmp2, bmp3;
 
 	private Effect mActiveEffect;
 	private Bitmap resultBitmap;
 
 	private static final int NONE = 0;
 	private static final int DRAG = 1;
 	private static final int ZOOM = 2;
 	private static final int OPEN_PICTURE = 1;
 	private int mode = NONE;
 
 	private static final String icon = "icon.png";
 
 	private PointF start = new PointF();
 	private PointF mid = new PointF();
 	private float oldDist = 1f;
 
 	public static SharedPreferences prefs;
 	public static boolean isPNG;
 	private String mExt;
 	private Context mContext;
 	private int mOrientation;
 
 	private static File sEffectsFolder;
 
 	// private float m1x, m1y;
 
 	// private ExifInterface oldexif;
 
 	@Override
 	protected void onDestroy() {
 		Effect.sBorderWidth = 0;
 		Effect.opts = null;
 		Effect.isLocked = false;
 		Effect.count = 0;
 		CirclesEf.sOnlyBorderIndex = -1;
 		for (File f : getCacheDir().listFiles()) {
 			// if (f.getName().endsWith(".png") || f.getName().endsWith(".jpg"))
 			if (f.isFile())
 				f.delete();
 		}
 		super.onDestroy();
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		Effect.count = 0;
 		sEffectsFolder = new File(getFilesDir(), "Effects");
 		if (!sEffectsFolder.exists())
 			sEffectsFolder.mkdirs();
 		setContentView(R.layout.activity_main);
 		mContext = this;
 		mShareFrame = (FrameLayout) findViewById(R.id.frame_share);
 		mLoadFrame = (FrameLayout) findViewById(R.id.frame_load_effect);
 		mFilenameFilter = new FilenameFilter() {
 
 			@Override
 			public boolean accept(File dir, String filename) {
 				if (dir.isDirectory())
 					return true;
 				else
 					return false;
 			}
 		};
 		mAddEffectFrame = (FrameLayout) findViewById(R.id.frame_add_effect);
 		// mFrame.setVisibility(View.GONE);
 		// mFrame.addView(new MainView(this));
 		mImageView = (MainView) findViewById(R.id.imageView1);
 
 		setViewGroupFont((FrameLayout) findViewById(R.id.frame),
 				Typeface.MONOSPACE);
 		mSlidingDrawer = (SlidingDrawer) findViewById(R.id.mainSlidingDrawer);
 
 		// mHoriScrollView = (HorizontalScrollView) findViewById(R.id.footer);
 
 		Intent intent = getIntent();
 		if (intent.getAction().equals(Intent.ACTION_SEND)) {
 			// Uri imageUri = intent.getData();
 			Bundle extras = intent.getExtras();
 			if (extras.containsKey(Intent.EXTRA_STREAM)) {
 				Uri uri = (Uri) extras.getParcelable(Intent.EXTRA_STREAM);
 				// mUri = uri;
 				File f = getFileFromURI(uri);
 				// if (f == null) {
 				// f = new File(uri.getPath());
 				// }
 
 				mFilePath = f.getAbsolutePath();
 				mFileName = f.getName();
 				try {
 					bmp1 = MediaStore.Images.Media.getBitmap(
 							getContentResolver(), uri);
 					mImageView.setImageBitmap(bmp1);
 					// resultBitmap = bmp1;
 				} catch (FileNotFoundException e) {
 					e.printStackTrace();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			} else {
 				finish();
 			}
 			// Bundle extras = intent.getExtras();
 			// if (extras.containsKey(Intent.EXTRA_STREAM)) {
 			// Uri uri = (Uri) extras.getParcelable(Intent.EXTRA_STREAM);
 			// // String scheme = uri.getScheme();
 			// // if (scheme.equals("content")) {
 			// // String mimeType = intent.getType();
 			// // ContentResolver contentResolver = getContentResolver();
 			// // Cursor cursor = contentResolver.query(uri, null, null, null,
 			// // null);
 			// // cursor.moveToFirst();
 			// // String filePath =
 			// //
 			// cursor.getString(cursor.getColumnIndexOrThrow(Images.Media.DATA));
 			// }
 			// finish();
 		} else {
 			bmp1 = ((BitmapDrawable) mImageView.getDrawable()).getBitmap();
 		}
 		// Log.e(TAG, "" + bmp1.getWidth());
 		// bmp.get
 		// RectF drawableRect = new RectF(mImageView.getDrawable().getBounds());
 		// Matrix m = imageView.getImageMatrix();
 
 		// int w = Math.round(bmp1.getWidth() * ce.scale);
 		// int h = Math.round(bmp1.getHeight() * ce.scale);
 		// bmp2 = Bitmap.createScaledBitmap(bmp1, w, h, true);
 		// new ce.Draw().execute(bmp2);
 		// draw(bmp2);
 		// mImageView.setImageBitmap(bmp3);
 
 		// LinearLayout ll = (LinearLayout) findViewById(R.id.maincontainer);
 		/*
 		 * LayoutTransition l = new LayoutTransition();
 		 * l.enableTransitionType(LayoutTransition.CHANGE_APPEARING);
 		 * mHoriScrollView.setLayoutTransition(l);
 		 */
 		// Animation ani = new AnimationDrawable()
 		/*
 		 * ll.setOnLongClickListener(new OnLongClickListener() {
 		 * 
 		 * @Override public boolean onLongClick(View v) {
 		 * Toast.makeText(mContext, "67 "+v, 0).show();
 		 * //getButtonLayout().removeView(v);
 		 * 
 		 * return true; } });
 		 */
 		mImageView.setOnTouchListener(this);
 		// mImageView.setScaleType(ImageView.ScaleType.MATRIX);
 		prefs = PreferenceManager.getDefaultSharedPreferences(this);
 		// Log.e("",prefs.getString(Prefs.KEY_FORMAT, "JPEG"));
 		mExt = prefs.getString(Prefs.KEY_FORMAT, "JPEG").equals("PNG") ? ".png"
 				: ".jpg";
 		// Toast.makeText(this, mExt, 0).show();
 		isPNG = mExt.equals(".png");
 		resultBitmap = bmp1;
 		// Toast.makeText(this, ""+isPNG, 0).show();
 		// CirclesEf ce = new CirclesEf(this);
 		// list.add(ce);
 		// mActiveEffect = new CirclesEf(this);
 		// list.add(mActiveEffect);
 		// it = list.iterator();
 		update(bmp1, 0);
 	}
 
 	// @Override
 	// protected void onStart() {
 	//
 	//
 	// super.onStart();
 	// }
 
 	public synchronized void update(Bitmap bmp, int j) {
 		// Toast.makeText(this, "" + j + "___" + list.size(), 0).show();
 
 		if (bmp == null) {
 			// bmp = resultBitmap;
 			// index = 0;
 			update(bmp1, 0);
 		} else if (j < list.size()) {
 			index = j;
 			// Toast.makeText(this, "" + list.size(), 0).show();
 			Effect ef = list.get(j);
 			ef.setBitmap(bmp);
 			ef.invalidate();
 		} else {
 			resultBitmap = bmp;
 			index = list.indexOf(mActiveEffect);
 			mImageView.setImageBitmap(resultBitmap);
 		}
 	}
 
 	public void updateMoving(Bitmap bmp) {
 		resultBitmap = bmp;
 		// index = list.indexOf(mActiveEffect);
 		mImageView.setImageBitmap(resultBitmap);
 	}
 
 	@Override
 	public void onBackPressed() {
 		if (mLoadFrame.getVisibility() == View.VISIBLE) {
 			mLoadFrame.setVisibility(View.GONE);
 		} else if (mShareFrame.getVisibility() == View.VISIBLE) {
 			mShareFrame.setVisibility(View.GONE);
 		} else if (mAddEffectFrame.getVisibility() == View.VISIBLE) {
 			mAddEffectFrame.setVisibility(View.GONE);
 		} else if (mActiveEffect != null
 				&& mActiveEffect.getSlidingDrawer().isOpened()) {
 			mActiveEffect.getSlidingDrawer().animateClose();
 		} else {
 			new AlertDialog.Builder(this)
 					// .setIcon(android.R.drawable.ic_dialog_alert)
 					// .setTitle("")
 					.setMessage(R.string.are_you_sure_quit)
 					.setPositiveButton(android.R.string.yes,
 							new DialogInterface.OnClickListener() {
 
 								@Override
 								public void onClick(DialogInterface dialog,
 										int which) {
 									finish();
 								}
 
 							}).setNegativeButton(android.R.string.no, null)
 					.show();
 		}
 	};
 
 	@Override
 	protected void onResume() {
 		MTRNGJNILib.randomize();
 		String ext = isPNG ? ".png" : ".jpg";
 		if (!mExt.equals(ext)) {
 			mExt = ext;
 			setShareIntent();
 		}
 		Effect ef = getCurrentEffect();
 		if (ef instanceof BorderEf) {
 			int color = prefs.getInt(Prefs.KEY_COLOR, 0xFFFFFFFF);
 			if (color != ((BorderEf) ef).getBackgroundColor()) {
 				((BorderEf) ef).setBackgroundColor(color);
 				ef.invalidate();
 			}
 		}
 		// long i1 = System.currentTimeMillis();
 		// float f;
 		// for (int i=0;i<10000;i++) {
 		// f = MTRNGJNILib.rand();
 		// }
 		// long i2 = System.currentTimeMillis();
 		// for (int i=0;i<10000;i++) {
 		// f = (float)Math.random();
 		// }
 		// long i3 = System.currentTimeMillis();
 		// Toast.makeText(this, ""+(i2-i1)+" vs "+(i3-i2), 0).show();
 		super.onResume();
 	}
 
 	public FrameLayout getParentLayout() {
 		return (FrameLayout) findViewById(R.id.frame);
 	}
 
 	public LinearLayout getButtonLayout() {
 		return (LinearLayout) findViewById(R.id.maincontainer);
 	}
 
 	public ImageView getImageView() {
 		return mImageView;
 	}
 
 	public Effect getCurrentEffect() {
 		return mActiveEffect;
 	}
 
 	public void setCurrentEffect(Effect ef) {
 		mActiveEffect = ef;
 		index = list.indexOf(ef);
 		// mCurEfIndex = list.indexOf(ef);
 		// it = list.iterator();
 		// while (it.hasNext()) {
 		// if (it.next() == mActiveEffect) {
 		// break;
 		// }
 		// }
 	}
 
 	public boolean removeEffect(Effect ef) {
 		if (list.remove(ef)) {
 			if (mActiveEffect != null)
 				mActiveEffect.deactivate();
 			// if (ef.isActive())
 			// ef.deactivate();
 			if (ef instanceof BorderEf)
 				Effect.sBorderWidth = getMaxWaveHeight();
 			mActiveEffect = null;
 			int i = 0;
 			for (Effect l : list) {
 				i++;
 				l.setIndex(i);
 			}
 			Effect.count = list.size();
 
 			ef = null;
 			index = 0;
 
 			if (i != 0) {
 				list.get(0).activate();
 				if (mActiveEffect.bmp1 != bmp1)
 					update(bmp1, index);
 			} else {
 				// setCurrentEffect(null);
 				resultBitmap = bmp1;
 				mImageView.setImageBitmap(bmp1);
 			}
 
 			return true;
 		}
 		return false;
 	}
 
 	// public void setCurrentEffect(int index) {
 	// mActiveEffect = list.get(index);
 	// mCurEfIndex = index;
 	// // it = list.iterator();
 	// // while (it.hasNext()) {
 	// // if (it.next() == mActiveEffect) {
 	// // break;
 	// // }
 	// // }
 	// }
 
 	void OnSizeChanged(RectF viewRect) {
 		mViewRect = viewRect;
 		// DisplayMetrics outMetrics = new DisplayMetrics();
 		// getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
 
 		// DisplayMetrics metrics = new DisplayMetrics();
 		// getWindowManager().getDefaultDisplay().getMetrics(metrics);
 		int rotate = 0;
 
 		if (mFilePath != null) {
 			try {
 				ExifInterface exif = new ExifInterface(mFilePath);
 				int orientation = exif.getAttributeInt(
 						ExifInterface.TAG_ORIENTATION,
 						ExifInterface.ORIENTATION_NORMAL);
 
 				switch (orientation) {
 				case ExifInterface.ORIENTATION_ROTATE_270:
 					rotate = 270;
 					break;
 				case ExifInterface.ORIENTATION_ROTATE_180:
 					rotate = 180;
 					break;
 				case ExifInterface.ORIENTATION_ROTATE_90:
 					rotate = 90;
 					break;
 				}
 				if (rotate != mOrientation) {
 					mOrientation = rotate;
 				}
 
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		RectF drawableRect = null;
 		if (rotate == 0 || rotate == 180) {
 			drawableRect = new RectF(0, 0, bmp1.getWidth(), bmp1.getHeight());
 		} else {
 			drawableRect = new RectF(0, 0, bmp1.getHeight(), bmp1.getWidth());
 		}
 
 		matrix.setRectToRect(drawableRect, viewRect, Matrix.ScaleToFit.CENTER);
 		float[] v = new float[9];
 		matrix.getValues(v);
 		// Log.d("qwe", "" + v[2] + "__" + v[5]);
 		// if (rotate != 0) {
 		// matrix.postRotate(rotate, viewRect.centerX(), viewRect.centerY());
 		// }
 
 		if (rotate == 90) {
 			// matrix.preTranslate(bmp1.getWidth()/2, bmp1.getHeight()/2);
 			// matrix.postTranslate(0, -viewRect.bottom);
 			// int i = Math.min(bmp1.getHeight(), bmp1.getWidth())/2;
 
 			matrix.preRotate(rotate, 0, 0);
 			matrix.postTranslate(viewRect.right - 2 * v[2], 0);
 
 		} else if (rotate == 270) {
 			matrix.preRotate(rotate, 0, 0);
 			matrix.postTranslate(0, viewRect.bottom - 2 * v[5]);
 		}
 		// else if (rotate == 180) {
 		// matrix.preRotate(rotate, 0, 0);
 		// matrix.postTranslate(viewRect.right - 2 * v[2], viewRect.bottom - 2 *
 		// v[5]);
 		// }
 
 		mImageView.setImageMatrix(matrix);
 		// Log.e(TAG, "created " + mFrame.getWidth());
 
 		// Bitmap bmp = ((BitmapDrawable)mImageView.getDrawable()).getBitmap();
 		// Log.e(TAG, ""+bmp.getWidth());
 		// //bmp.get
 		// //RectF drawableRect = new
 		// RectF(mImageView.getDrawable().getBounds());
 		// //Matrix m = imageView.getImageMatrix();
 		// RectF drawableRect = new RectF(0, 0, bmp.getWidth(),
 		// bmp.getHeight());
 		// RectF viewRect = new RectF(0, 0, mImageView.getWidth(),
 		// mImageView.getHeight());
 		// matrix.setRectToRect(drawableRect, viewRect,
 		// Matrix.ScaleToFit.CENTER);
 		// mImageView.setImageMatrix(matrix);
 		// savedMatrix.set(matrix);
 
 		// mImageView.getS.getImageMatrix().getWidth();
 
 		// start.set(mImageView.getRight(),mImageView.getTop());
 		// float ce.scale = mImageView.getScgetScaleX();
 		// mImageView.setScaleType(ImageView.ScaleType.MATRIX);
 		// matrix.set(mImageView.getImageMatrix());
 		// savedMatrix.set(mImageView.getImageMatrix());
 	}
 
 	@Override
 	public boolean onTouch(View v, MotionEvent event) {
 		if (v.getId() != mImageView.getId())
 			return true;
 		int i = event.getActionMasked();
 		if (i == MotionEvent.ACTION_DOWN) {
 			if (mLoadFrame.getVisibility() == View.VISIBLE)
 				mLoadFrame.setVisibility(View.GONE);
 			if (mAddEffectFrame.getVisibility() == View.VISIBLE)
 				mAddEffectFrame.setVisibility(View.GONE);
 			if (mShareFrame.getVisibility() == View.VISIBLE)
 				mShareFrame.setVisibility(View.GONE);
 			if (mSlidingDrawer.isOpened())
 				mSlidingDrawer.animateClose();
 			if (mActiveEffect != null) {
 				SlidingDrawer sd = mActiveEffect.getSlidingDrawer();
 				if (sd.isOpened()) {
 					// Rect outRect = new Rect();
 					// sd.getHitRect(outRect);
 					// if (event.getY() < outRect.top +
 					// sd.getHandle().getHeight())
 					sd.animateClose();
 				}
 			}
 		}
 
 		if (mActiveEffect instanceof BorderEf) {
 			// if (v.getId() == R.id.frame_rainbow) {
 			// ((BorderEf) mActiveEffect).setAdjustRainbow(true);
 			// } else {
 			// ((BorderEf) mActiveEffect).setAdjustRainbow(false);
 			// }
 			// if (Effect.isRunning) return true;
 			((BorderEf) mActiveEffect).onTouchEvent(event);
 			return true;
 		}
 
 		ImageView view = (ImageView) v;
 		float scale;
 
 		// dumpEvent(event);
 		// Handle touch events here...
 
 		switch (i) {
 		case MotionEvent.ACTION_DOWN: // first finger down only
 			savedMatrix.set(matrix);
 			start.set(event.getX(), event.getY());
 			// Log.d(TAG, "mode=DRAG"); // write to LogCat
 			mode = DRAG;
 			break;
 		// m1x = event.getX();
 		// m1y = event.getY();
 		// Toast.makeText(this, ""+m1x+"_"+m1y, 0).show();
 		// OnSizeChanged(mViewRect);
 		// break;
 
 		case MotionEvent.ACTION_UP: // first finger lifted
 
 		case MotionEvent.ACTION_POINTER_UP: // second finger lifted
 
 			mode = NONE;
 			// Log.d(TAG, "mode=NONE");
 			break;
 
 		case MotionEvent.ACTION_POINTER_DOWN: // first and second finger down
 
 			oldDist = spacing(event);
 			// Log.d(TAG, "oldDist=" + oldDist);
 			if (oldDist > 5f) {
 				savedMatrix.set(matrix);
 				midPoint(mid, event);
 				mode = ZOOM;
 				// Log.d(TAG, "mode=ZOOM");
 			}
 			break;
 
 		case MotionEvent.ACTION_MOVE:
 			// Log.d("q123",""+(event.getX() - start.x));
 			if (mode == DRAG) {
 				matrix.set(savedMatrix);
 				matrix.postTranslate(event.getX() - start.x, event.getY()
 						- start.y); // create the transformation in the matrix
 									// of points
 			} else if (mode == ZOOM) {
 				// pinch zooming
 				float newDist = spacing(event);
 				// Log.d(TAG, "newDist=" + newDist);
 				if (newDist > 5f) {
 					matrix.set(savedMatrix);
 					scale = newDist / oldDist; // setting the scaling of the
 												// matrix...if ce.scale > 1
 												// means
 												// zoom in...if ce.scale < 1
 												// means
 												// zoom out
 					matrix.postScale(scale, scale, mid.x, mid.y);
 				}
 			}
 			break;
 		}
 
 		view.setImageMatrix(matrix); // display the transformation on screen
 
 		return true; // indicate event was handled
 	}
 
 	/*
 	 * --------------------------------------------------------------------------
 	 * Method: spacing Parameters: MotionEvent Returns: float Description:
 	 * checks the spacing between the two fingers on touch
 	 * ----------------------------------------------------
 	 */
 
 	private float spacing(MotionEvent event) {
 		float x = event.getX(0) - event.getX(1);
 		float y = event.getY(0) - event.getY(1);
 		return FloatMath.sqrt(x * x + y * y);
 	}
 
 	/*
 	 * --------------------------------------------------------------------------
 	 * Method: midPoint Parameters: PointF object, MotionEvent Returns: void
 	 * Description: calculates the midpoint between the two fingers
 	 * ------------------------------------------------------------
 	 */
 
 	private void midPoint(PointF point, MotionEvent event) {
 		float x = event.getX(0) + event.getX(1);
 		float y = event.getY(0) + event.getY(1);
 		point.set(x / 2, y / 2);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		// Locate MenuItem with ShareActionProvider
 		// MenuItem item = menu.findItem(R.id.menu_item_share);
 
 		// Fetch and store ShareActionProvider
 		// mShareActionProvider = (ShareActionProvider)
 		// item.getActionProvider();
 		// mShareActionProvider
 		// .setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);
 		setShareIntent();
 		setLoadFrame();
 		// invalidateOptionsMenu();
 
 		// mShareActionProvider
 		// .setOnShareTargetSelectedListener(new
 		// ShareActionProvider.OnShareTargetSelectedListener() {
 		// public boolean onShareTargetSelected(
 		// ShareActionProvider source, Intent intent) {
 		// Uri uri = exportImage(getCacheDir().toString(), "tmp");
 		// if (uri == null)
 		// return true;
 		// try {
 		// // mShareIntent.putExtra(Intent.EXTRA_STREAM,
 		// // uri.toString());
 		// // mShareActionProvider.setShareIntent(mShareIntent);
 		// } catch (Exception e) {
 		// e.printStackTrace();
 		// }
 		// // invalidateOptionsMenu();
 		// return false;
 		// }
 		// });
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	private void setShareIntent() {
 		mShareFrame.setVisibility(View.GONE);
 		// mShareIntent = new Intent(Intent.ACTION_SEND);
 		// final File outputDir = getCacheDir();
 
 		Intent intent = ShareCompat.IntentBuilder.from(this).setType("image/*")
 				.getIntent();
 
 		mShareIntent = ShareCompat.IntentBuilder.from(this)
 				.setStream(Uri.fromFile(new File(getCacheDir(), "tmp" + mExt)))
 				// .setText("This site has lots of great information about Android! http://www.android.com")
 				.setType("image/*").getIntent();
 
 		// mShareIntent.setType("image/*");
 		// final String outputDir = getCacheDir().toString();
 		// mShareIntent.putExtra(Intent.EXTRA_STREAM,
 		// Uri.fromFile(new File(outputDir, "tmp" + mExt)));
 		PackageManager pm = getPackageManager();
 
 		List<ResolveInfo> infoList = pm.queryIntentActivities(intent,
 				PackageManager.GET_ACTIVITIES);
 		// Intent il[] = new Intent[infoList.size()];
 		List<ShareIntent> si = new ArrayList<ShareIntent>();
 
 		// ComponentName cname = getComponentName();//new ComponentName(this,
 		// SaveActivity.class);
 		// // Intent saveactivity = new Intent(this, SaveActivity.class);
 		// // startActivity(prefs);
 		// ActivityInfo sa = null;
 		// try {
 		// sa = pm.getActivityInfo(cname, 0);
 		// } catch (NameNotFoundException e1) {
 		// // TODO Auto-generated catch block
 		// e1.printStackTrace();
 		// }
 		int i = 1;
 		// if (sa != null) {
 		si.add(new ShareIntent(getString(R.string.just_save_image), null,
 				getResources().getDrawable(R.drawable.ic_launcher)));
 
 		// i = 1;
 		// }
 		// TreeMap<String, String> dirsMap = new TreeMap<String, String>();
 		for (ResolveInfo fg : infoList) {
 			// Intent result = new Intent(Intent.ACTION_SEND);
 			// result.setType("image/*");
 			// result.putExtra(Intent.EXTRA_STREAM,
 			// Uri.fromFile(new File(outputDir, "tmp" + mExt)));
 			ActivityInfo activity = fg.activityInfo;
 			if (activity.exported) {
 				ComponentName cn = new ComponentName(
 						activity.applicationInfo.packageName, activity.name);
 
 				// result.setComponent(ComponentName.unflattenFromString(fg.activityInfo.name));
 				// String l = (String) fg.loadLabel(getPackageManager());
 				// if (applicationInfo.name != null)
 
 				// il[i]=result;
 				si.add(new ShareIntent((String) fg.loadLabel(pm), cn, fg
 						.loadIcon(pm)));
 				i++;
 			}
 			// Log.e(TAG, fg.activityInfo.name);
 		}
 
 		// mShareIntent.addCategory(Intent.CATEGORY_LAUNCHER);
 		// mShareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
 		// | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
 
 		if (mListView != null) {
 			mShareFrame.removeView(mListView);
 		}
 		mListView = new ListView(this);
 
 		// TextView header = new TextView(this);
 		// header.setText("123");
 		// lv.addHeaderView(header);
 		// lv.setBackgroundColor(Color.TRANSPARENT);//R.color.popup_background_color
 		// ListView.LayoutParams p = new ListView.LayoutParams(
 		// LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
 		// MarginLayoutParams mp = new MarginLayoutParams(p);
 
 		mListView.setPadding(4, 8, 4, 4);
 		// lv.setPadding(10, 10, 10, 10);
 		// lv.set
 		// lv.setLayoutParams(p);
 		// final int positionOffset = 1;
 
 		mListView.setAdapter(new ArrayAdapter<ShareIntent>(this,
 				R.layout.simple_list_item_single_choice, si) {
 			@Override
 			public View getView(int position, View convertView, ViewGroup parent) {
 
 				View view;
 				TextView text;
 				ImageView image;
 				LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
 
 				if (convertView == null) {
 					view = inflater.inflate(
 							R.layout.simple_list_item_single_choice, parent,
 							false);
 				} else {
 					view = convertView;
 				}
 
 				try {
 					text = (TextView) view.findViewById(R.id.title);
 					image = (ImageView) view.findViewById(R.id.icon);
 
 				} catch (ClassCastException e) {
 					throw new IllegalStateException(e.toString(), e);
 				}
 
 				ShareIntent item = getItem(position);
 				text.setText(item.toString());
 				image.setImageDrawable(item.getIcon());
 
 				return view;
 
 				// LayoutInflater inflater = (LayoutInflater)
 				// getSystemService(LAYOUT_INFLATER_SERVICE);
 				// View rowView = inflater.inflate(
 				// R.layout.simple_list_item_single_choice, parent, false);
 				// TextView textView = (TextView)
 				// rowView.findViewById(R.id.label);
 				// ImageView imageView = (ImageView) rowView
 				// .findViewById(R.id.icon);
 				// textView.setText(values[position]);
 				// // Изменение иконки для Windows и iPhone
 				// String s = values[position];
 				// if (s.startsWith("Windows7") || s.startsWith("iPhone")
 				// || s.startsWith("Solaris")) {
 				// imageView.setImageResource(R.drawable.no);
 				// } else {
 				// imageView.setImageResource(R.drawable.ok);
 				// }
 				//
 				// return rowView;
 				//
 				// return parent;
 			}
 		});
 		// lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
 		// lv.setCacheColorHint(Color.TRANSPARENT);
 		mListView.setVerticalFadingEdgeEnabled(false);
 		mListView.setOverScrollMode(ListView.OVER_SCROLL_NEVER);
 		// lv.setSelector(R.drawable.list_selector);
 		// lv.setItemChecked(mKView.currentYUVProcessor() + positionOffset,
 		// true);
 		// lv.getAdapter()
 
 		mListView.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View view,
 					int position, long id) {
 				if (position == 0) {
 					final EditText input = new EditText(mContext);
 					input.setText(mFileName + "-"
 							+ mContext.getString(R.string.app_name) + mExt);
 
 					new AlertDialog.Builder(mContext)
 							.setTitle(R.string.edit_file_name)
 							.setMessage(
 									prefs.getString(
 											Prefs.KEY_FOLDER,
 											Environment
 													.getExternalStoragePublicDirectory(
 															Environment.DIRECTORY_PICTURES)
 													.toString())
 											+ "/")
 							.setView(input)
 							.setPositiveButton(android.R.string.yes,
 									new DialogInterface.OnClickListener() {
 										public void onClick(
 												DialogInterface dialog,
 												int whichButton) {
 											String filename = input.getText()
 													.toString();
 											if (filename.length() == 0)
 												return;
 											new Export()
 													.execute(null, filename);
 										}
 									})
 							.setNegativeButton(android.R.string.no, null)
 							.show();
 
 					// Intent saveactivity = new Intent(mContext,
 					// SaveActivity.class);
 					// SaveActivity.setBitmap(bmp3);
 					// startActivity(saveactivity);
 					mShareFrame.setVisibility(View.GONE);
 
 					return;
 				}
 				ShareIntent item = (ShareIntent) parent
 						.getItemAtPosition(position);
 				// File fl = new File(getCacheDir(), mFileName + mExt);
 				String fn = mFileName + "-"
 						+ mContext.getString(R.string.app_name) + mExt;
 				File fl = new File(getCacheDir(), fn);
 				mShareIntent = ShareCompat.IntentBuilder
 						.from(MainActivity.this).setStream(Uri.fromFile(fl))
 						// .setText("This site has lots of great information about Android! http://www.android.com")
 						.setType("image/*").getIntent();
 				mShareIntent.setComponent(item.getCn());
 				// mShareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
 				// mShareIntent.setPackage(item.getCn().getPackageName());
 				// Log.e(TAG, "" + item.getCn());
 				new Export().execute(getCacheDir().getAbsolutePath(), fn);
 				// startActivity(mShareIntent);
 				// startActivity(il[position]);
 			}
 		});
 
 		mShareFrame.addView(mListView);
 
 		// Toast.makeText(this,
 		// "" + Uri.fromFile(new File(outputDir, "tmp" + mExt)), 0).show();
 		// mShareActionProvider.setShareIntent(mShareIntent);
 		// invalidateOptionsMenu();
 	}
 
 	private void setLoadFrame() {
 		mLoadFrame.setVisibility(View.GONE);
 
 		List<String> si = new ArrayList<String>();
 		int i = 0;
 		File[] fs = sEffectsFolder.listFiles(mFilenameFilter);
 		for (File f : fs) {
 			si.add(f.getName());
 			i++;
 			// Log.e(TAG, fg.activityInfo.name);
 		}
 		if (i == 0) {
 			si.add(getString(R.string.no_saves));
 		} else {
 			si.add(getString(R.string.clear_all));
 		}
 
 		if (mLoadListView != null) {
 			mLoadFrame.removeView(mLoadListView);
 		}
 		mLoadListView = new ListView(this);
 		mLoadListView.setPadding(4, 8, 4, 4);
 		mLoadListView.setAdapter(new ArrayAdapter<String>(this,
 				R.layout.simple_list_item_single_choice, si) {
 			@Override
 			public View getView(int position, View convertView, ViewGroup parent) {
 
 				View view;
 				TextView text;
 				ImageView image;
 				LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
 
 				if (convertView == null) {
 					view = inflater.inflate(
 							R.layout.simple_list_item_single_choice, parent,
 							false);
 				} else {
 					view = convertView;
 				}
 
 				try {
 					text = (TextView) view.findViewById(R.id.title);
 					image = (ImageView) view.findViewById(R.id.icon);
 
 				} catch (ClassCastException e) {
 					throw new IllegalStateException(e.toString(), e);
 				}
 
 				String item = getItem(position);
 				text.setText(item);
 				Bitmap bmp = BitmapFactory.decodeFile((new File(new File(
 						sEffectsFolder, item), icon)).getAbsolutePath());
 				image.setImageBitmap(bmp);
 
 				return view;
 
 			}
 		});
 		// lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
 		// lv.setCacheColorHint(Color.TRANSPARENT);
 		mLoadListView.setVerticalFadingEdgeEnabled(false);
 		mLoadListView.setOverScrollMode(ListView.OVER_SCROLL_NEVER);
 		// lv.setSelector(R.drawable.list_selector);
 		// lv.setItemChecked(mKView.currentYUVProcessor() + positionOffset,
 		// true);
 		// lv.getAdapter()
 		if (i != 0) {
 			mLoadListView.setOnItemClickListener(new OnItemClickListener() {
 				public void onItemClick(AdapterView<?> parent, View view,
 						int position, long id) {
 					if (position == parent.getCount() - 1) {
 						new AlertDialog.Builder(mContext)
 								// .setIcon(android.R.drawable.ic_dialog_alert)
 								// .setTitle("")
 								.setMessage(R.string.confirm_clear_all)
 								.setPositiveButton(android.R.string.yes,
 										new DialogInterface.OnClickListener() {
 
 											@Override
 											public void onClick(
 													DialogInterface dialog,
 													int which) {
 												File[] lf = sEffectsFolder
 														.listFiles(mFilenameFilter);
 												for (File file : lf) {
 													for (File ef : file
 															.listFiles()) {
 														ef.delete();
 													}
 													file.delete();
 												}
 												setLoadFrame();
 												mLoadFrame
 														.setVisibility(View.VISIBLE);
 											}
 
 										})
 								.setNegativeButton(android.R.string.no, null)
 								.show();
 					} else {
 						String item = (String) parent
 								.getItemAtPosition(position);
 						File folder = new File(sEffectsFolder, item);
 						load(folder);
 					}
 				}
 			});
 			mLoadListView
 					.setOnItemLongClickListener(new OnItemLongClickListener() {
 
 						@Override
 						public boolean onItemLongClick(AdapterView<?> parent,
 								View view, int position, long id) {
 							final CharSequence[] items = {
 									mContext.getString(R.string.remove),
 									mContext.getString(R.string.load),
 									mContext.getString(R.string.rename) };
 							// TextView text = (TextView) view
 							// .findViewById(R.id.title);
 							final String item = (String) parent
 									.getItemAtPosition(position);
 							final File f = new File(sEffectsFolder, item);
 
 							new AlertDialog.Builder(mContext)// .setTitle(R.string.pick_color);
 									.setItems(
 											items,
 											new DialogInterface.OnClickListener() {
 												public void onClick(
 														DialogInterface dialog,
 														int which) {
 													switch (which) {
 													case 0:
 														File[] files = f
 																.listFiles();
 														for (File file : files) {
 															file.delete();
 														}
 														f.delete();
 														setLoadFrame();
 														mLoadFrame
 																.setVisibility(View.VISIBLE);
 														break;
 													case 1:
 														load(f);
 														break;
 													case 2:
 														final EditText input = new EditText(
 																mContext);
 														input.setText(item);
 														new AlertDialog.Builder(
 																mContext)
 																.setTitle(
 																		R.string.input_new_name)
 																.setView(input)
 																.setPositiveButton(
 																		android.R.string.yes,
 																		new DialogInterface.OnClickListener() {
 																			public void onClick(
 																					DialogInterface dialog,
 																					int whichButton) {
 																				String effname = input
 																						.getText()
 																						.toString();
 																				if (effname
 																						.length() == 0)
 																					return;
 																				File newfile = new File(
 																						f.getParent(),
 																						effname);
 																				f.renameTo(newfile);
 																				setLoadFrame();
 																				mLoadFrame
 																						.setVisibility(View.VISIBLE);
 																			}
 																		})
 																.setNegativeButton(
 																		android.R.string.no,
 																		null)
 																.show();
 
 														break;
 													}
 												}
 											}).show();
 							return true;
 
 						}
 
 					});
 
 		}
 
 		mLoadFrame.addView(mLoadListView);
 
 		// Toast.makeText(this,
 		// "" + Uri.fromFile(new File(outputDir, "tmp" + mExt)), 0).show();
 		// mShareActionProvider.setShareIntent(mShareIntent);
 		// invalidateOptionsMenu();
 	}
 
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		MenuItem item = menu.findItem(R.id.menu_save_eff);
 		item.setEnabled(list.size() != 0);
 		if (mActiveEffect instanceof BorderEf) {
 			BorderEf be = (BorderEf) mActiveEffect;
 
 			item = menu.findItem(R.id.fit);
 			item.setVisible(true);
 			item.setChecked(be.getFit());
 
 			item = menu.findItem(R.id.circle);
 			item.setVisible(true);
 			item.setChecked(be.getCircle());
 
 			item = menu.findItem(R.id.draw);
 			item.setVisible(true);
 
 			item = menu.findItem(R.id.clip);
 			item.setVisible(true);
 			item.setChecked(be.getClip());
 
 			menu.findItem(R.id.postmark).setVisible(true);
 
 			menu.findItem(R.id.only_border).setVisible(false);
 
 		} else {
 			menu.findItem(R.id.fit).setVisible(false);
 			menu.findItem(R.id.circle).setVisible(false);
 			menu.findItem(R.id.draw).setVisible(false);
 			menu.findItem(R.id.postmark).setVisible(false);
 			menu.findItem(R.id.clip).setVisible(false);
 			item = menu.findItem(R.id.only_border);
 
 			if (mActiveEffect instanceof CirclesEf && Effect.sBorderWidth > 0) {
 				CirclesEf ce = (CirclesEf) mActiveEffect;
 				item.setVisible(true);
 				item.setChecked(ce.isOnlyBorder);
 			} else {
 				item.setVisible(false);
 			}
 
 		}
 		return super.onPrepareOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menu_settings:
 			Intent prefs = new Intent(this, Prefs.class);
 			startActivity(prefs);
 			break;
 		case R.id.open:
 			Intent intent = new Intent();
 			intent.setType("image/*");
 			intent.setAction(Intent.ACTION_GET_CONTENT);
 			startActivityForResult(intent, OPEN_PICTURE);
 			// Intent intent = new Intent(this, Prefs.class);
 			// startActivity(intent);
 			break;
 		case R.id.menu_item_share:
 			if (mShareFrame.getVisibility() == View.GONE)
 				mShareFrame.setVisibility(View.VISIBLE);
 			else
 				mShareFrame.setVisibility(View.GONE);
 			break;
 		case R.id.menu_save_eff:
 			if (list.size() == 0)
 				break;
 			mLoadFrame.setVisibility(View.VISIBLE);
 			final EditText input = new EditText(this);
 			File[] lf = sEffectsFolder.listFiles(mFilenameFilter);
 			input.setText(mContext.getString(R.string.effect) + (lf.length + 1));
 
 			new AlertDialog.Builder(mContext)
 					.setTitle(R.string.input_name)
 					// .setMessage(
 					// this.prefs.getString(
 					// Prefs.KEY_FOLDER,
 					// Environment
 					// .getExternalStoragePublicDirectory(
 					// Environment.DIRECTORY_PICTURES)
 					// .toString())
 					// + "/")
 					.setView(input)
 					.setPositiveButton(android.R.string.yes,
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int whichButton) {
 									String effname = input.getText().toString();
 									if (effname.length() == 0)
 										return;
 									saveEffect(effname);
 									setLoadFrame();
 									mLoadFrame.setVisibility(View.VISIBLE);
 								}
 							}).setNegativeButton(android.R.string.no, null)
 					.show();
 			break;
 		case R.id.menu_load_eff:
 			if (mLoadFrame.getVisibility() == View.GONE)
 				mLoadFrame.setVisibility(View.VISIBLE);
 			else
 				mLoadFrame.setVisibility(View.GONE);
 			break;
 		case R.id.fit:
 			item.setChecked(!item.isChecked());
 			((BorderEf) mActiveEffect).setFit(item.isChecked());
 			break;
 		case R.id.circle:
 			item.setChecked(!item.isChecked());
 			((BorderEf) mActiveEffect).setCircle(item.isChecked());
 			break;
 		case R.id.draw:
 			LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
 			final View view = inflater.inflate(R.layout.draw_dialog, null,
 					false);
 			setViewGroupFont((RelativeLayout) view, Typeface.MONOSPACE);
 			final CheckBox shouldDraw = (CheckBox) view
 					.findViewById(R.id.should_draw);
 			final ImageView image = (ImageView) view
 					.findViewById(R.id.drawimage);
 			final SeekBar paint_radius = (SeekBar) view
 					.findViewById(R.id.paint_radius);
 			final SeekBar paint_blur = (SeekBar) view
 					.findViewById(R.id.paint_blur);
 			final SeekBar frequency = (SeekBar) view
 					.findViewById(R.id.frequency);
 			final TextView frequency_val = (TextView) view
 					.findViewById(R.id.frequency_val);
 			// int sq_side = 300;// paint_blur.getWidth();
 			// Matrix m = new Matrix();
 			// m.setRectToRect(new RectF(0, 0, 72, 72), new RectF(0, 0, sq_side,
 			// sq_side), Matrix.ScaleToFit.CENTER);
 
 			final Paint p = new Paint() {
 				{
 					setAntiAlias(true);
 					setDither(true);
 					setColor(((BorderEf) mActiveEffect).getColor());
 					setStyle(Paint.Style.FILL);
 					setStrokeWidth(paint_radius.getProgress() + 1);
 				}
 			};
 
 			Bitmap draw_bitmap = Bitmap.createBitmap(400, 400,
 					Bitmap.Config.ARGB_8888);
 			final Canvas canvas = new Canvas(draw_bitmap);
 			shouldDraw
 					.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 
 						@Override
 						public void onCheckedChanged(CompoundButton buttonView,
 								boolean isChecked) {
 							if (!isChecked) {
 								canvas.drawColor(Color.TRANSPARENT,
 										PorterDuff.Mode.CLEAR);
 								image.invalidate();
 							}
 						}
 					});
 
 			paint_radius
 					.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
 
 						@Override
 						public void onStopTrackingTouch(SeekBar seekBar) {
 							// TODO Auto-generated method stub
 
 						}
 
 						@Override
 						public void onStartTrackingTouch(SeekBar seekBar) {
 							// TODO Auto-generated method stub
 
 						}
 
 						@Override
 						public void onProgressChanged(SeekBar seekBar,
 								int progress, boolean fromUser) {
 							p.setStrokeWidth(progress + 1);
 						}
 					});
 
 			BlurMaskFilter blur = new BlurMaskFilter(paint_blur.getProgress(),
 					BlurMaskFilter.Blur.NORMAL);
 			p.setMaskFilter(blur);
 
 			paint_blur
 					.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
 
 						@Override
 						public void onStopTrackingTouch(SeekBar seekBar) {
 							// TODO Auto-generated method stub
 
 						}
 
 						@Override
 						public void onStartTrackingTouch(SeekBar seekBar) {
 							// TODO Auto-generated method stub
 
 						}
 
 						@Override
 						public void onProgressChanged(SeekBar seekBar,
 								int progress, boolean fromUser) {
 							if (progress != 0) {
 								BlurMaskFilter blur = new BlurMaskFilter(
 										progress, BlurMaskFilter.Blur.NORMAL);
 								p.setMaskFilter(blur);
 							} else {
 								p.setMaskFilter(null);
 							}
 						}
 					});
 			frequency_val
 					.setText(Integer.toString(frequency.getProgress() + 1));
 			frequency.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
 
 				@Override
 				public void onStopTrackingTouch(SeekBar seekBar) {
 				}
 
 				@Override
 				public void onStartTrackingTouch(SeekBar seekBar) {
 				}
 
 				@Override
 				public void onProgressChanged(SeekBar seekBar, int progress,
 						boolean fromUser) {
 					frequency_val.setText(Integer.toString(progress + 1));
 				}
 			});
 
 			image.setImageBitmap(draw_bitmap);
 			// image.setImageMatrix(m);
 
 			image.setOnTouchListener(new OnTouchListener() {
 
 				@Override
 				public boolean onTouch(View v, MotionEvent event) {
 					canvas.drawCircle(event.getX(), event.getY(),
 							p.getStrokeWidth(), p);// Point(event.getX(),
 													// event.getY(), p);
 					v.invalidate();
 					return true;
 				}
 			});
 
 			new AlertDialog.Builder(this)
 					// .setMessage(R.string.input)
 					.setView(view)
 					.setPositiveButton(android.R.string.yes,
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int whichButton) {
 									if (shouldDraw.isChecked()) {
 										((BorderEf) mActiveEffect).setBitmapPoint(
 												((BitmapDrawable) image
 														.getDrawable())
 														.getBitmap(), frequency
 														.getProgress() + 1);
 									} else {
 										((BorderEf) mActiveEffect)
 												.setBitmapPoint(null, 0);
 									}
 									mActiveEffect.invalidate();
 								}
 							}).setNegativeButton(android.R.string.no, null)
 					.show();
 
 			break;
 		case R.id.clip:
 			item.setChecked(!item.isChecked());
 			((BorderEf) mActiveEffect).setClip(item.isChecked());
 			break;
 		case R.id.postmark:
 			((BorderEf) mActiveEffect).setPostmark();
 			break;
 		case R.id.only_border:
 			item.setChecked(!item.isChecked());
 			((CirclesEf) mActiveEffect).isOnlyBorder = item.isChecked();
 			Effect.sBorderWidth = getMaxWaveHeight();
 			mActiveEffect.invalidate();
 			break;
 		}
 		return true;
 	}
 
 	public void saveEffect(String effname) {
 		final File eff_folder = new File(sEffectsFolder, effname);
 		eff_folder.mkdirs();
 		for (Effect ef : list) {
 			ef.save(eff_folder);
 		}
 		exportIcon(new File(eff_folder, icon));
 	}
 
 	private void exportIcon(File imageFile) {
 		Bitmap bmp = Bitmap.createBitmap(72, 72, Bitmap.Config.ARGB_8888);
 		Canvas canvas = new Canvas(bmp);
 		int m, n;
 		Paint p = new Paint();
 		// p.setDither(true);
 		// p.setAntiAlias(true);
 
 		Rect src = new Rect(0, 0, 72, 72);
 		int c = list.size();// getButtonLayout().getChildCount();
 		int count = 1 + (int) Math.sqrt(c);
 		int side = 72 / count;
 		for (int k = 0; k < c; k++) {
 			m = (k / count) * side;
 			n = (k % count) * side;
 			p.setColor(list.get(k).getColor());
 			Rect dst = new Rect(m, n, m + side, n + side);
 			// ImageButton ib = (ImageButton) getButtonLayout().getChildAt(k);
 			// Bitmap bm = ((BitmapDrawable) ib.getDrawable()).getBitmap();
 			canvas.drawRect(dst, p);
 		}
 
 		Bitmap.CompressFormat mCf = Bitmap.CompressFormat.PNG;
 		int mQ = 100;
 		ByteArrayOutputStream stream = new ByteArrayOutputStream();
 		bmp.compress(mCf, mQ, stream);
 		byte[] byteArray = stream.toByteArray();
 		stream = null;
 		bmp.recycle();
 		System.gc();
 		BufferedOutputStream out = null;
 
 		try {
 			out = new BufferedOutputStream(new FileOutputStream(imageFile));
 			out.write(byteArray);
 		} catch (Exception e) {
 		} finally {
 			try {
 				out.close();
 			} catch (Exception e) {
 				// e.printStackTrace();
 			}
 		}
 
 		byteArray = null;
 		System.gc();
 	}
 
 	void load(File folder) {
 		for (Effect ef : list) {
 			ef.destroy();
 		}
 		mActiveEffect = null;
 		Effect.count = 0;
 		Effect.opts = new Options();
 		CirclesEf.sOnlyBorderIndex = -1;
 		Effect.isLocked = false;
 		list.clear();
 		Effect ef = null;
 		FilenameFilter fnf = new FilenameFilter() {
 
 			@Override
 			public boolean accept(File dir, String filename) {
 				if (filename.startsWith("i") || filename.startsWith("b"))
 					return false;
 				return true;
 			}
 		};
 		File[] efs = folder.listFiles(fnf);
 		for (File sr : efs) {
 			String className = sr.getName().split(":")[1];
 
 			try {
 				Class c = getClassLoader().loadClass(className);
 				Constructor constructors[] = c.getConstructors();
 				Constructor constructor = null;
 				for (Constructor cr : constructors) {
 					Class[] cs = cr.getParameterTypes();
 					if (cs.length == 2 && cs[0] == Context.class
 							&& cs[1] == File.class) {
 						constructor = cr;
 					}
 				}
 				try {
 					ef = (Effect) (constructor.newInstance(mContext, sr));
 				} catch (IllegalArgumentException e) {
 					e.printStackTrace();
 				} catch (InstantiationException e) {
 					e.printStackTrace();
 				} catch (IllegalAccessException e) {
 					e.printStackTrace();
 				} catch (InvocationTargetException e) {
 					e.printStackTrace();
 				}
 				list.add(ef);
 
 			} catch (ClassNotFoundException e) {
 				e.printStackTrace();
 			}
 
 		}
 
 		index = list.size() - 1;
 		// ef.setBitmap(resultBitmap);
 
 		// mActiveEffect = ef;
 		// update(bmp1, 0);
 		ef.activate();
 		// update(resultBitmap, index);
 
 		// update(null, 0);
 		// mLoadFrame.setVisibility(View.GONE);
 
 		// File fl = new File(getCacheDir(), mFileName + mExt);
 		// String fn = mFileName + "-"
 		// + mContext.getString(R.string.app_name) + mExt;
 		// File fl = new File(getCacheDir(), fn);
 		// mShareIntent = ShareCompat.IntentBuilder
 		// .from(MainActivity.this).setStream(Uri.fromFile(fl))
 		// //
 		// .setText("This site has lots of great information about Android! http://www.android.com")
 		// .setType("image/*").getIntent();
 		// mShareIntent.setComponent(item.getCn());
 		// //
 		// mShareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
 		// //
 		// mShareIntent.setPackage(item.getCn().getPackageName());
 		// // Log.e(TAG, "" + item.getCn());
 		// new Export().execute(getCacheDir().getAbsolutePath(),
 		// fn);
 		// // startActivity(mShareIntent);
 		// // startActivity(il[position]);
 
 	}
 
 	public void addEffectOnClick(View v) {
 		// ------------------------------
 		if (mAddEffectListView == null) {
 			// dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
 			mAddEffectListView = new ListView(this);
 			List<EffectEntry> si = new ArrayList<EffectEntry>();
 
 			si.add(new EffectEntry(getString(R.string.circles),
 					CirclesEf.class, getResources().getDrawable(
 							R.drawable.bt_circles)));
 			si.add(new EffectEntry(getString(R.string.waves), BorderEf.class,
 					getResources().getDrawable(R.drawable.bt_border)));
 			mAddEffectListView.setPadding(4, 8, 4, 4);
 			mAddEffectListView.setAdapter(new ArrayAdapter<EffectEntry>(this,
 					R.layout.simple_list_item_single_choice, si) {
 				@Override
 				public View getView(int position, View convertView,
 						ViewGroup parent) {
 
 					View view;
 					TextView text;
 					ImageView image;
 					LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
 
 					if (convertView == null) {
 						view = inflater.inflate(
 								R.layout.simple_list_item_single_choice,
 								parent, false);
 					} else {
 						view = convertView;
 					}
 
 					try {
 						text = (TextView) view.findViewById(R.id.title);
 						image = (ImageView) view.findViewById(R.id.icon);
 
 					} catch (ClassCastException e) {
 						throw new IllegalStateException(e.toString(), e);
 					}
 
 					EffectEntry item = getItem(position);
 					text.setText(item.toString());
 					image.setImageDrawable(item.getIcon());
 
 					return view;
 				}
 			});
 			mAddEffectListView.setVerticalFadingEdgeEnabled(false);
 			mAddEffectListView.setOverScrollMode(ListView.OVER_SCROLL_NEVER);
 			mAddEffectListView
 					.setOnItemClickListener(new OnItemClickListener() {
 						public void onItemClick(AdapterView<?> parent,
 								View view, int position, long id) {
 							EffectEntry item = (EffectEntry) parent
 									.getItemAtPosition(position);
 							Effect ef = null;
 							mAddEffectFrame.setVisibility(View.GONE);
 							// dialog.hide();
 							Constructor constructor[] = item.getEffect()
 									.getConstructors();
 							try {
 								ef = (Effect) (constructor[0]
 										.newInstance(mContext));
 							} catch (IllegalArgumentException e) {
 								// TODO Auto-generated catch block
 								e.printStackTrace();
 							} catch (InstantiationException e) {
 								e.printStackTrace();
 							} catch (IllegalAccessException e) {
 								e.printStackTrace();
 							} catch (InvocationTargetException e) {
 								e.printStackTrace();
 							}
 							list.add(ef);
 							index = list.size() - 1;
 							if (resultBitmap != null)
 								ef.setBitmap(resultBitmap);
 							ef.activate();
 							// mActiveEffect = ef;
 							if (CirclesEf.sOnlyBorderIndex > -1
 									&& Effect.sBorderWidth > 0
 									&& !(ef instanceof CirclesEf))
 								update(bmp1, 0);
 							else
 								mActiveEffect.invalidate();// update(resultBitmap,
 															// index);
 							// update(resultBitmap, index);
 						}
 					});
 			mAddEffectFrame.addView(mAddEffectListView);
 			// dialog.setContentView(mAddEffectListView);
 		}
 		if (mAddEffectFrame.getVisibility() == View.GONE) {
 			mAddEffectFrame.setVisibility(View.VISIBLE);
 		}
 		// dialog.show();
 	}
 
 	class Export extends AsyncTask<String, Void, Uri> {
 
 		String path = null;
 
 		@Override
 		protected Uri doInBackground(String... params) {
 			if (params.length == 2) {
 				path = params[0];
 				return exportImage(params[0], params[1]);
 			}
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Uri result) {
 			if (result != null) {
 				if (path == null) {
 					Toast.makeText(
 							mContext,
 							mContext.getString(R.string.picture_saved_to)
 									+ mResultFile.getPath(), 0).show();
 				} else {
 					startActivity(mShareIntent);
 				}
 			}
 		}
 	}
 
 	Uri exportImage(String outpath, String filename) {
 
 		// mExt = prefs.getString(Prefs.KEY_FORMAT, "JPEG");
 		// isPNG = mExt.equals(".png");
 		// mExt = isPNG ? ".png" : ".jpg";
 
 		Bitmap.CompressFormat mCf;
 		int mQ;
 
 		if (isPNG) {
 			// mG = Bitmap.Config.ARGB_8888;
 			mCf = Bitmap.CompressFormat.PNG;
 			mQ = 100;
 		} else {
 			// mG = Bitmap.Config.RGB_565;
 			mCf = Bitmap.CompressFormat.JPEG;
 			mQ = 50 + prefs.getInt(Prefs.KEY_JPEG_Q, 40);
 			// Toast.makeText(this, "" + mQ, 0).show();
 		}
 
 		boolean retainexif = prefs.getBoolean(Prefs.KEY_RETAIN_EXIF, true);
 
 		String path = null;
 		if (outpath == null) {
 			path = prefs.getString(
 					Prefs.KEY_FOLDER,
 					Environment.getExternalStoragePublicDirectory(
 							Environment.DIRECTORY_PICTURES).toString());
 		} else {
 			path = outpath;
 		}
 		// String dir = prefs.getString(Prefs.KEY_FORMAT, "JPEG");
 		String fn = filename.toLowerCase();
 		if (!fn.endsWith(mExt)) {
 			int last_dot = filename.lastIndexOf(".");
 			if (last_dot >= 0)
 				filename = filename.substring(0, last_dot) + mExt;
 			else
 				filename = filename + mExt;
 		}
 
 		(new File(path)).mkdirs();
 
 		File file = new File(path, filename);
 		// if (outpath != null) {
 		// mTempFile = file;
 		// }
 
 		// Bitmap bitmap = drawIntoBitmap(BitmapFactory.decodeFile(file1
 		// .getAbsolutePath()));// , opts
 
 		ByteArrayOutputStream stream = new ByteArrayOutputStream();
 		resultBitmap.compress(mCf, mQ, stream);
 		byte[] byteArray = stream.toByteArray();
 		stream = null;
 		// bitmap.recycle();
 		System.gc();
 		BufferedOutputStream out = null;
 
 		try {
 			out = new BufferedOutputStream(new FileOutputStream(file));
 			out.write(byteArray);
 		} catch (Exception e) {
 		} finally {
 			try {
 				out.close();
 			} catch (Exception e) {
 				// e.printStackTrace();
 			}
 		}
 
 		byteArray = null;
 		System.gc();
 		// String str;
 		// if (file.exists()) {
 		// str = done;
 		// total++;
 		// if (exif != null) {
 		// try {
 		// ExifInterface newexif = new ExifInterface(
 		// file.getAbsolutePath());
 		// str = copyExif(newexif) + str;
 		// } catch (IOException e) {
 		// }
 		// exif = null;
 		// }
 		// } else {
 		// str = error;
 		// }
 		if (file.exists()) {
 			if (outpath != null) {
 				file.setReadable(true, false);
 			}
 		} else {
 			return null;
 		}
 
 		if (retainexif && mFilePath != null) {
 			ExifInterface newexif = null;
 			ExifInterface oldexif = null;
 			try {
 				newexif = new ExifInterface(file.getAbsolutePath());
 				oldexif = new ExifInterface(mFilePath);
 				if (oldexif != null) {
 					copyExif(newexif, oldexif);
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 
 		}
 		mResultFile = file;
 		new SingleMediaScanner(this, file);
 		return Uri.fromFile(file);// (new SingleMediaScanner(this,
 									// file)).getUri();
 	}
 
 	private void copyExif(ExifInterface newexif, ExifInterface oldexif) {
 		int i = 0;
 		String s = oldexif.getAttribute(ExifInterface.TAG_APERTURE);
 		if (s != null) {
 			newexif.setAttribute(ExifInterface.TAG_APERTURE, s);
 			i++;
 		}
 		s = oldexif.getAttribute(ExifInterface.TAG_DATETIME);
 		if (s != null) {
 			newexif.setAttribute(ExifInterface.TAG_DATETIME, s);
 			i++;
 		}
 		s = oldexif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME);
 		if (s != null) {
 			newexif.setAttribute(ExifInterface.TAG_EXPOSURE_TIME, s);
 			i++;
 		}
 		s = oldexif.getAttribute(ExifInterface.TAG_FLASH);
 		if (s != null) {
 			newexif.setAttribute(ExifInterface.TAG_FLASH, s);
 			i++;
 		}
 		s = oldexif.getAttribute(ExifInterface.TAG_FOCAL_LENGTH);
 		if (s != null) {
 			newexif.setAttribute(ExifInterface.TAG_FOCAL_LENGTH, s);
 			i++;
 		}
 		s = oldexif.getAttribute(ExifInterface.TAG_GPS_ALTITUDE);
 		if (s != null) {
 			newexif.setAttribute(ExifInterface.TAG_GPS_ALTITUDE, s);
 			i++;
 		}
 		s = oldexif.getAttribute(ExifInterface.TAG_GPS_ALTITUDE_REF);
 		if (s != null) {
 			newexif.setAttribute(ExifInterface.TAG_GPS_ALTITUDE_REF, s);
 			i++;
 		}
 		s = oldexif.getAttribute(ExifInterface.TAG_GPS_DATESTAMP);
 		if (s != null) {
 			newexif.setAttribute(ExifInterface.TAG_GPS_DATESTAMP, s);
 			i++;
 		}
 		s = oldexif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
 		if (s != null) {
 			newexif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, s);
 			i++;
 		}
 		s = oldexif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
 		if (s != null) {
 			newexif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, s);
 			i++;
 		}
 		s = oldexif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
 		if (s != null) {
 			newexif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, s);
 			i++;
 		}
 		s = oldexif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);
 		if (s != null) {
 			newexif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, s);
 			i++;
 		}
 		s = oldexif.getAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD);
 		if (s != null) {
 			newexif.setAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD, s);
 			i++;
 		}
 		s = oldexif.getAttribute(ExifInterface.TAG_GPS_TIMESTAMP);
 		if (s != null) {
 			newexif.setAttribute(ExifInterface.TAG_GPS_TIMESTAMP, s);
 			i++;
 		}
 		s = oldexif.getAttribute(ExifInterface.TAG_IMAGE_LENGTH);
 		if (s != null) {
 			newexif.setAttribute(ExifInterface.TAG_IMAGE_LENGTH, s);
 			i++;
 		}
 		s = oldexif.getAttribute(ExifInterface.TAG_IMAGE_WIDTH);
 		if (s != null) {
 			newexif.setAttribute(ExifInterface.TAG_IMAGE_WIDTH, s);
 			i++;
 		}
 		s = oldexif.getAttribute(ExifInterface.TAG_ISO);
 		if (s != null) {
 			newexif.setAttribute(ExifInterface.TAG_ISO, s);
 			i++;
 		}
 		s = oldexif.getAttribute(ExifInterface.TAG_MAKE);
 		if (s != null) {
 			newexif.setAttribute(ExifInterface.TAG_MAKE, s);
 			i++;
 		}
 		s = oldexif.getAttribute(ExifInterface.TAG_MODEL);
 		if (s != null) {
 			newexif.setAttribute(ExifInterface.TAG_MODEL, s);
 			i++;
 		}
 		s = oldexif.getAttribute(ExifInterface.TAG_ORIENTATION);
 		if (s != null) {
 			newexif.setAttribute(ExifInterface.TAG_ORIENTATION, s);
 			i++;
 		}
 		s = oldexif.getAttribute(ExifInterface.TAG_WHITE_BALANCE);
 		if (s != null) {
 			newexif.setAttribute(ExifInterface.TAG_WHITE_BALANCE, s);
 			i++;
 		}
 		if (i > 0) {
 			try {
 				newexif.saveAttributes();
 				// return "("+i+" EXIF tags written) ";
 			} catch (Exception e) {
 			}
 		}
 	}
 
 	public void loadBitmap(boolean b) {
 		Effect.isLocked = true;
 		mImageView.setImageBitmap(null);
 		float scale = 1;
 		if (b) {
 			scale = (float) (Effect.opts.inSampleSize - 1)
 					/ Effect.opts.inSampleSize;
 			// Toast.makeText(this, "" + scale, 0).show();
 			BorderEf.scaleStrokeWidth(scale);
 			Effect.sBorderWidth *= scale;
 		}
 		for (Effect l : list)
 			l.rescale(scale);
 		bmp1.recycle();
 		System.gc();
 		if (mFilePath == null) {
 			bmp1 = BitmapFactory.decodeResource(getResources(), R.drawable.df,
 					Effect.opts);
 		} else {
 			bmp1 = BitmapFactory.decodeFile(mFilePath, Effect.opts);
 		}
 		resultBitmap = bmp1;
 		// mImageView.setImageBitmap(bmp1);
 		OnSizeChanged(mViewRect);
 		Effect.isLocked = false;
 		if (list != null && list.size() > 0) {
 			// list.get(0).setBitmap(bmp1);
 			update(bmp1, 0);
 		}
 		if (mActiveEffect == null) {
 			mImageView.setImageBitmap(bmp1);
 		}
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 
 		if (requestCode == OPEN_PICTURE) {
 			if (resultCode == RESULT_OK) {
 				Uri imageUri = data.getData();
 				try {
 					// bmp1.recycle();
 					Effect.opts = new Options();
 					mFileName = getFileFromURI(imageUri).getName();
 					mFilePath = getFileFromURI(imageUri).getAbsolutePath();
 					// mImageView.setImageBitmap(null);
 					// bmp1 = null;
 					System.gc();
 					bmp1 = MediaStore.Images.Media.getBitmap(
 							getContentResolver(), imageUri);
 					if (list != null && list.size() > 0) {
 						// list.get(0).setBitmap(bmp1);
 						update(bmp1, 0);
 					} else {
 						resultBitmap = bmp1;
 						mImageView.setImageBitmap(bmp1);
 					}
 					OnSizeChanged(mViewRect);
 				} catch (FileNotFoundException e) {
 					e.printStackTrace();
 				} catch (IOException e) {
 					e.printStackTrace();
 				} catch (OutOfMemoryError e) {
 					if (Effect.opts.inSampleSize < 2)
 						Effect.opts.inSampleSize = 2;
 					else
 						Effect.opts.inSampleSize++;
 					loadBitmap(false);
 				}
 			}
 		}
 	}
 
 	private File getFileFromURI(Uri uri) {
 		Cursor cursor = getContentResolver().query(uri, null, null, null, null);
 		if (cursor == null)
 			return new File(uri.getPath());
 		cursor.moveToFirst();
 		int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
 		return new File(cursor.getString(idx));
 	}
 
 	public static final void setViewGroupFont(ViewGroup mContainer,
 			Typeface mFont) {
 		if (mContainer == null || mFont == null)
 			return;
 
 		final int mCount = mContainer.getChildCount();
 
 		// Loop through all of the children.
 		for (int i = 0; i < mCount; ++i) {
 			final View mChild = mContainer.getChildAt(i);
 			if (mChild instanceof TextView) {
 				// Set the font if it is a TextView.
 				((TextView) mChild).setTypeface(mFont);
 			} else if (mChild instanceof ViewGroup) {
 				// Recursively attempt another ViewGroup.
 				setViewGroupFont((ViewGroup) mChild, mFont);
 			}
 		}
 	}
 
 	public float getMaxWaveHeight() {
 		float i = 0, j;
 		int m = 0;
 		for (Effect ef : list) {
 			if (ef instanceof BorderEf) {
 				j = ((BorderEf) ef).getWaveHeight();
 				if (j > i)
 					i = j;
 			} else if (ef instanceof CirclesEf) {
 				if (((CirclesEf) ef).isOnlyBorder)
 					m = ef.index;
 			}
 		}
 		CirclesEf.sOnlyBorderIndex = m - 1;
 		return i * 2.f;
 	}
 
 	public Bitmap getResultBitmap() {
 		return resultBitmap;
 	}
 }
