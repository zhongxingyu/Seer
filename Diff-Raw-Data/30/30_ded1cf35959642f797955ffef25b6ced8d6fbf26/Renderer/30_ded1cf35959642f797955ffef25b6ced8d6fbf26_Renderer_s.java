 package fr.ravenfeld.livewallpaper.slideshow;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.Random;
 
 import javax.microedition.khronos.egl.EGLConfig;
 import javax.microedition.khronos.opengles.GL10;
 
 import rajawali.Camera2D;
 import rajawali.materials.Material;
 import rajawali.materials.textures.ATexture.TextureException;
 import rajawali.materials.textures.AnimatedGIFTexture;
 import rajawali.materials.textures.Texture;
 import rajawali.materials.textures.TextureManager;
 import rajawali.primitives.Plane;
 import rajawali.renderer.RajawaliRenderer;
 import rajawali.wallpaper.Wallpaper;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Color;
 import android.net.Uri;
 import android.util.Log;
 import android.view.MotionEvent;
 
 import com.ipaulpro.afilechooser.utils.FileUtils;
 
 public class Renderer extends RajawaliRenderer implements
 		SharedPreferences.OnSharedPreferenceChangeListener {
 	private final SharedPreferences mSharedPreferences;
 
 	private enum ModeRenderer {
 		CLASSIC, LETTER_BOXED, STRETCHED
 	}
 
 	private enum TimePref {
 		TIME_5_SECONDS, TIME_1_MINUTE, TIME_5_MINUTES, TIME_15_MINUTES, TIME_30_MINUTES, TIME_1_HOUR, TIME_1_DAY
 	}
 
 	private TimePref mTimePref = TimePref.TIME_5_MINUTES;
 
 	private Texture mTexture;
 	private AnimatedGIFTexture mAnimatedTexture;
 	private Material mMaterial;
 	private Plane mPlane;
 	private float mWidthPlane;
 
 	private final ArrayList<String> mListFiles;
 	private int mIdCurrent = -1;
 	private boolean mUseGIF;
 	private boolean mUseFile;
 	private Date mDateLastChange;
 	private final Object mLock = new Object();
 	private float mXoffset;
 	public Renderer(Context context) {
 		super(context);
 
 		mSharedPreferences = context.getSharedPreferences(
 				Wallpaper.SHARED_PREFS_NAME, 0);
 		mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
 		mListFiles = new ArrayList<String>();
 		updateTime();
 		mDateLastChange = new Date();
 	}
 
 	@Override
 	protected void initScene() {
 		setFrameRate(30);
 		Camera2D cam = new Camera2D();
 		this.replaceAndSwitchCamera(getCurrentCamera(), cam);
 		getCurrentScene().setBackgroundColor(Color.RED);
 		getCurrentCamera().setLookAt(0, 0, 0);
 
 		mPlane = new Plane(1f, 1f, 1, 1);
 		mMaterial = new Material();
 		mTexture = new Texture("bg", R.drawable.bg);
 		mAnimatedTexture = new AnimatedGIFTexture("bgAnimated", R.drawable.bob);
 		try {
 			mMaterial.addTexture(mTexture);
 
 		} catch (TextureException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		mPlane.setMaterial(mMaterial);
 		mPlane.setPosition(0, 0, 0);
 		mPlane.setRotY(180);
 		mPlane.setTransparent(true);
 
 		initBackground();
 		addChild(mPlane);
 
 	}
 
 	private void initBackground() {
 		String uri = mSharedPreferences.getString("uri", "");
 		if (!uri.equalsIgnoreCase("")) {
 			File file = FileUtils.getFile(Uri.parse(uri));
 			if (file.isDirectory()) {
 				mUseFile = true;
 				mListFiles.clear();
 				List<File> list = FileUtils.getFileList(file.getAbsolutePath(),
 						Settings.INCLUDE_EXTENSIONS_LIST);
 				for (File item : list) {
 					if (item.isFile()) {
 						mListFiles.add(item.getAbsolutePath());
 					}
 				}
 				if (mListFiles.size() > 0) {
 					changedBackground();
 				} else {
 					loadRessourceDefault(R.drawable.bg);
 				}
 
 			} else if (file.isFile()) {
 				mUseFile = false;
 				loadFile(file.getAbsolutePath());
 			}
 		}
 		TextureManager.getInstance().reload();
 		initPlane();
 	}
 
 	private void changedBackground() {
 		synchronized (mLock) {
 			boolean random = mSharedPreferences
 					.getBoolean("random_file", false);
 			if (random) {
 				loadFile(mListFiles.get(randomId()));
 			} else {
 				loadFile(mListFiles.get(nextId()));
 			}
 			// TextureManager.getInstance().reset();
 			// TextureManager.getInstance().reload();
 
 			initPlane();
 			onOffsetsChanged(mXoffset, 0, 0, 0, 0, 0);
 		}
 	}
 
 	private int nextId() {
 		mIdCurrent += 1;
 		if (mIdCurrent >= mListFiles.size()) {
 			mIdCurrent = 0;
 		}
 		return mIdCurrent;
 	}
 
 	private int randomId() {
 		Random r = new Random();
 		return r.nextInt(mListFiles.size());
 	}
 
 	private void loadFile(String uri) {
 		File file = FileUtils.getFile(Uri.parse(uri.replaceFirst("/",
 				"file:///")));
 		if (file.isFile()) {
 			Log.e("TEST", "FILE " + uri);
 			mAnimatedTexture.stopAnimation();
 			if (FileUtils.getExtension(uri).equalsIgnoreCase(".gif")) {
 				mUseGIF = true;
 				if (mAnimatedTexture == null) {
 					mAnimatedTexture = new AnimatedGIFTexture("bgAnimated",
 							uri.replaceFirst("/", "file:///"));
 
 				} else {
 					mAnimatedTexture.setPathName(uri.replaceFirst("/",
 							"file:///"));
 
 				}
 				try {
 					mMaterial.removeTexture(mAnimatedTexture);
 					mMaterial.removeTexture(mTexture);
 					mMaterial.addTexture(mAnimatedTexture);
 				} catch (TextureException e) {
 					e.printStackTrace();
 				}
 				mAnimatedTexture.rewind();
 			} else {
 				mUseGIF = false;
 				mMaterial.removeTexture(mTexture);

				mMaterial.addTexture(mAnimatedTexture);
			} catch (TextureException e) {
				e.printStackTrace();
			}
			mAnimatedTexture.rewind();
		} else {
			mUseGIF = false;
			mMaterial.removeTexture(mTexture);
			Bitmap b = BitmapFactory.decodeFile(uri.replace("file:///", ""));
 				mTexture.shouldRecycle(true);
			mTexture.setBitmap(b);
			try {
				mMaterial.removeTexture(mAnimatedTexture);
				mMaterial.addTexture(mTexture);
			} catch (TextureException e) {
				e.printStackTrace();
 			}
 		} else {
 			changedBackground();
 			mListFiles.remove(uri);
 
 		}
 	}
 
 	private void loadRessourceDefault(int resourceId) {
 		mUseGIF = false;
 		Bitmap b = BitmapFactory.decodeResource(mContext.getResources(),
 				resourceId);
 		mTexture.setBitmap(b);
 		try {
 			mMaterial.removeTexture(mAnimatedTexture);
 			mMaterial.addTexture(mTexture);
 		} catch (TextureException e) {
 		}
 	}
 
 	private boolean checkDate() {
 		boolean bool = false;
 		Date date = new Date();
 		if (mDateLastChange == null) {
 			mDateLastChange = date;
 		}
 		GregorianCalendar lastDate = new GregorianCalendar(
 				mDateLastChange.getYear() + 1900, mDateLastChange.getMonth(),
 				mDateLastChange.getDate(), mDateLastChange.getHours(),
 				mDateLastChange.getMinutes(), mDateLastChange.getSeconds());
 		switch (mTimePref) {
 		case TIME_5_SECONDS:
 			lastDate.add(GregorianCalendar.SECOND, 5);
 			break;
 		case TIME_1_MINUTE:
 			lastDate.add(GregorianCalendar.MINUTE, 1);
 			break;
 		case TIME_5_MINUTES:
 			lastDate.add(GregorianCalendar.MINUTE, 5);
 			break;
 		case TIME_15_MINUTES:
 			lastDate.add(GregorianCalendar.MINUTE, 15);
 			break;
 		case TIME_30_MINUTES:
 			lastDate.add(GregorianCalendar.MINUTE, 30);
 			break;
 		case TIME_1_HOUR:
 			lastDate.add(GregorianCalendar.HOUR_OF_DAY, 1);
 			break;
 		case TIME_1_DAY:
 			lastDate.add(GregorianCalendar.DAY_OF_MONTH, 1);
 			break;
 
 		}
 		if (date.after(lastDate.getTime())) {
 			bool = true;
 			mDateLastChange = date;
 		}
 		return bool;
 	}
 
 	private void updateTime() {
 		String timePref = mSharedPreferences
 				.getString("time", "time_5_minutes");
 		if (timePref.equalsIgnoreCase("time_5_seconds")) {
 			mTimePref = TimePref.TIME_5_SECONDS;
 		} else if (timePref.equalsIgnoreCase("time_1_minute")) {
 			mTimePref = TimePref.TIME_1_MINUTE;
 		} else if (timePref.equalsIgnoreCase("time_5_minutes")) {
 			mTimePref = TimePref.TIME_5_MINUTES;
 		} else if (timePref.equalsIgnoreCase("time_15_minutes")) {
 			mTimePref = TimePref.TIME_15_MINUTES;
 		} else if (timePref.equalsIgnoreCase("time_30_minutes")) {
 			mTimePref = TimePref.TIME_30_MINUTES;
 		} else if (timePref.equalsIgnoreCase("time_1_hour")) {
 			mTimePref = TimePref.TIME_1_HOUR;
 		} else if (timePref.equalsIgnoreCase("time_1_day")) {
 			mTimePref = TimePref.TIME_1_DAY;
 		}
 	}
 
 	private void initPlane() {
 		String renderer = mSharedPreferences.getString("rendererMode",
 				"classic");
 		if (renderer.equalsIgnoreCase("letter_boxed")) {
 			rendererMode(ModeRenderer.LETTER_BOXED);
 		} else if (renderer.equalsIgnoreCase("stretched")) {
 			rendererMode(ModeRenderer.STRETCHED);
 		} else {
 			rendererMode(ModeRenderer.CLASSIC);
 		}
 
 	}
 
 	private void rendererMode(ModeRenderer modeRenderer) {
 		float ratioDisplay = (float) mViewportHeight / (float) mViewportWidth;
 		float ratioVideo = getTextureHeight() / getTextureWidth();
 
 		if (ratioDisplay == ratioVideo) {
 			mPlane.setScaleX(1f);
 			mPlane.setScaleY(1f);
 			mWidthPlane = 1f;
 		} else if (ratioDisplay >= 1) {
 			// PORTRAIT
 			switch (modeRenderer) {
 			case STRETCHED:
 				rendererModeStretched();
 				break;
 			case LETTER_BOXED:
 				rendererModeLetterBox();
 				break;
 			default:
 				rendererModeClassic();
 				break;
 			}
 		} else {
 			// LANDSCAPE
 			switch (modeRenderer) {
 			case STRETCHED:
 				rendererModeStretched();
 				break;
 			case LETTER_BOXED:
 				rendererModeStretched();
 				break;
 			default:
 				rendererModeStretched();
 				break;
 			}
 		}
 	}
 
 	private void rendererModeClassic() {
 		float ratioDisplay = (float) mViewportHeight / (float) mViewportWidth;
 		float ratioSize = 1f / getTextureHeight();
 		mWidthPlane = getTextureWidth() * ratioSize * ratioDisplay;
 		mPlane.setScaleX(mWidthPlane);
 		mPlane.setScaleY(1);
 	}
 
 	private void rendererModeLetterBox() {
 		float ratioDisplay = (float) mViewportWidth / (float) mViewportHeight;
 		float ratioSize = 1f / getTextureWidth();
 		mPlane.setScaleY(getTextureHeight() * ratioSize * ratioDisplay);
 		mPlane.setScaleX(1f);
 		mWidthPlane = 1f;
 
 	}
 
 	private void rendererModeStretched() {
 		float ratioDisplay = (float) mViewportHeight / (float) mViewportWidth;
 		float ratioSize = 1f / getTextureHeight();
 		mPlane.setScaleX(getTextureWidth() * ratioSize * ratioDisplay);
 		mPlane.setScaleY(1f);
 		mWidthPlane = 1f;
 	}
 
 	@Override
 	public void onDrawFrame(GL10 glUnused) {
 		synchronized (mLock) {
 
 			super.onDrawFrame(glUnused);
 
 			if (mAnimatedTexture != null) {
 				try {
 					mAnimatedTexture.update();
 				} catch (TextureException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 			if (mTexture != null && mAnimatedTexture != null
 					&& mListFiles != null && mListFiles.size() > 0 && mUseFile
 					&& checkDate()) {
 				changedBackground();
 			}
 		}
 	}
 
 	@Override
 	public void onTouchEvent(MotionEvent event) {
 		super.onTouchEvent(event);
 	}
 
 	@Override
 	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
 		super.onSurfaceCreated(gl, config);
 	}
 
 	@Override
 	public void onSurfaceChanged(GL10 gl, int width, int height) {
 		super.onSurfaceChanged(gl, width, height);
 		initPlane();
 	}
 
 	@Override
 	public void onVisibilityChanged(boolean visible) {
 		super.onVisibilityChanged(visible);
 		System.gc();
 	}
 
 	@Override
 	public void onSurfaceDestroyed() {
 		super.onSurfaceDestroyed();
 	}
 
 	@Override
 	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
 			String key) {
 		synchronized (mLock) {
 			if (mTexture != null && mAnimatedTexture != null) {
 				initBackground();
 				updateTime();
 			}
 		}
 	}
 
 	@Override
 	public void onOffsetsChanged(float xOffset, float yOffset,
 			float xOffsetStep, float yOffsetStep, int xPixelOffset,
 			int yPixelOffset) {
 		if (mPlane != null) {
 			mXoffset = xOffset;
 			mPlane.setX((1 - mWidthPlane) * (xOffset - 0.5));
 		}
 	}
 
 	private float getTextureWidth() {
 		if (mUseGIF) {
 			return mAnimatedTexture.getWidth();
 		} else {
 			return mTexture.getWidth();
 		}
 	}
 
 	private float getTextureHeight() {
 		if (mUseGIF) {
 			return mAnimatedTexture.getHeight();
 		} else {
 			return mTexture.getHeight();
 		}
 	}
 }
