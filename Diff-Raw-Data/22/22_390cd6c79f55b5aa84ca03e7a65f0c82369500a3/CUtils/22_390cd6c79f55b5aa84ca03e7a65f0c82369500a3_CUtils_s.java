 package com.se.cronus.utils;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import com.se.cronus.MainActivity;
 import com.se.cronus.R;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.Context;
 import android.content.res.Configuration;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.Bitmap.Config;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Point;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.os.Build;
 import android.util.DisplayMetrics;
 import android.view.Display;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageButton;
 
 /***
  * 
  * @author dj
  * 
  */
 public class CUtils {
 	private static final int CLEAR = 150;
 	/* FEED ID */
 	public static final int FACEBOOK_FEED = 0;
 	public static final int PINTREST_FEED = 1;
 	public static final int INSTA_FEED = 2;
 	public static final int TWITTER_FEED = 3;
 	public static final int TEST_FEED = 4;
 	/* COLORS */
 	public static final int FACEBOOK_BLUE = Color.rgb(59, 89, 152);
 	public static final int FACEBOOK_BLUE_CLEAR = Color
 			.argb(CLEAR, 59, 89, 152);
 	public static final int PINTREST_RED = Color.rgb(203, 32, 40);
 	public static final int PINTREST_RED_CLEAR = Color.argb(CLEAR, 203, 32, 40);
 	public static final int INSTA_BROWN = Color.rgb(75, 66, 57);
 	public static final int INSTA_BROWN_CLEAR = Color.argb(CLEAR, 75, 66, 57);
 	public static final int TWITTER_BLUE = Color.rgb(91, 197, 237);
 	public static final int TWITTER_BLUE_CLEAR = Color
 			.argb(CLEAR, 91, 197, 237);
 
 	/* cronus colors!! */
 	public static final int CRONUS_GREEN_LIGHT = Color.rgb(47, 255, 152);
 	public static final int CRONUS_GREEN_DARK = Color.rgb(0, 204, 153);
 	public static final int CRONUS_GREEN_BLACK = Color.rgb(7, 38, 23);
 	public static final int CRONUS_BLUE_WHITE = Color.rgb(227, 251, 255);
 	public static final int CRONUS_GREEN_LIGHT_CLEAR = Color.argb(CLEAR, 47,
 			255, 152);
 
 	/* FONTS AND LETTERING AND NUMBER STUFF */
 	public static final float FONT_SIZE_SMALL = 18;
 
 	/*
 	 * static methods
 	 * 888888888888888888888888888888888888888888888888888888888888888
 	 */
 	public static int getScreenWidth(MainActivity a) {
 
 		int[] temp = new int[2];
 		getScreenSizePixels(temp, a);
 		return temp[0];
 
 	}
 
 	public static int getScreenHeight(MainActivity a) {
 
 		int[] temp = new int[2];
 		getScreenSizePixels(temp, a);
 		return temp[1];
 
 	}
 
 	private static void getScreenSizePixels(int widthHeightInPixels[/* 2 */],
 			MainActivity a) {
 		Resources resources = a.getResources();
 		Configuration config = resources.getConfiguration();
 		DisplayMetrics dm = resources.getDisplayMetrics();
 		// Note, screenHeightDp isn't reliable
 		// (it seems to be too small by the height of the status bar),
 		// but we assume screenWidthDp is reliable.
 		// Note also, dm.widthPixels,dm.heightPixels aren't reliably pixels
 		// (they get confused when in screen compatibility mode, it seems),
 		// but we assume their ratio is correct.
 
 		double screenWidthInPixels = (double) config.screenWidthDp * dm.density;
 		double screenHeightInPixels = screenWidthInPixels * dm.heightPixels
 				/ dm.widthPixels;
 		widthHeightInPixels[0] = (int) (screenWidthInPixels + .5);
 		widthHeightInPixels[1] = (int) (screenHeightInPixels + .5);
 	}
 
 	public static int findIdByString(Activity thisA, String ID) {
 		int resID = thisA.getResources().getIdentifier(ID, "id",
 				"com.se.cronus");
 		return resID;
 	}
 
 	public static int findDrawableByString(Activity activity, String draw) {
 		int resID = activity.getResources().getIdentifier(draw, "drawable",
 				"com.munchkin");
 		return resID;
 	}
 
 	public static View findViewByString(Activity thisA, String ID) {
 		return thisA.findViewById(findIdByString(thisA, ID));
 	}
 
 	@SuppressWarnings("NewApi")
 	public static ImageButton loadDrawableByAssets(Activity activity,
 			String file) {
 		// To load image
 		ImageButton image = new ImageButton(activity);
 
 		try {
 			// Get input stream
 			InputStream ims = activity.getAssets().open(
 					"images/" + file + ".jpg");
 			int y = (int) (getScreenHeight(activity) * .75);
 			Bitmap newImage = Bitmap.createScaledBitmap(
 					BitmapFactory.decodeStream(ims), (int) (y * .6), y, false);
 
 			image.setImageBitmap(newImage);
 			ims.close();
 
 		} catch (IOException ex) {
 			System.out.println(ex.toString());
 		}
 		return image;
 	}
 
 	@SuppressLint("NewApi")
 	public static Point getScreenSize(Activity a) {
 
 		Display display = a.getWindowManager().getDefaultDisplay();
 		Point size = new Point();
 		if (versionGreaterThan(Build.VERSION_CODES.HONEYCOMB_MR1))
 			display.getRealSize(size);
 		else {
 			size.x = display.getWidth();
 			size.y = display.getHeight();
 		}
 
 		return size;
 	}
 
 	public static int getScreenHeight(Activity a) {
 		return getScreenSize(a).y;
 	}
 
 	public static int getScreenWidth(Activity a) {
 		return getScreenSize(a).x;
 	}
 
 	/*
 	 * for reference visit
 	 * http://developer.android.com/guide/topics/manifest/uses
 	 * -sdk-element.html#ApiLevels
 	 */
 	public static boolean versionGreaterThan(int minV) {
 		return android.os.Build.VERSION.SDK_INT >= minV;
 	}
 
 	public static Bitmap drawableToBitmap(Activity activity, Drawable drawable,
 			int W, int H) {
 
 		Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
 				drawable.getIntrinsicHeight(), Config.ARGB_8888);
 		Canvas canvas = new Canvas(bitmap);
 		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
 		drawable.draw(canvas);
 
 		if (W == H) {// is a square
 			int h = bitmap.getHeight();
 			int w = bitmap.getWidth();
 			int crop = 0;
 			Bitmap nbitmap;
 			if (h < w) {// crop sides
 				crop = (int) (w - h) / 2;
 				nbitmap = Bitmap.createBitmap(bitmap, crop, 0, w - crop, h);
 				nbitmap = Bitmap.createScaledBitmap(nbitmap, W, H, false);
 				if (!nbitmap.equals(bitmap)) {
 					bitmap.recycle();
 					System.out.println("bitmap.recycle();");
 				}
 				return nbitmap;
 			}
 			if (h > w) {// crop top and bottom
 				crop = (int) (w - h) / 2;
 				nbitmap = Bitmap.createBitmap(bitmap, 0, crop, w, h - crop);
 				nbitmap = Bitmap.createScaledBitmap(nbitmap, W, H, false);
 				if (!nbitmap.equals(bitmap)) {
 					bitmap.recycle();
 					System.out.println("bitmap.recycle();");
 				}
 				bitmap.recycle();
 				return nbitmap;
 			}
 		}
 
 		bitmap = Bitmap.createScaledBitmap(bitmap, W, H, false);
 
 		return bitmap;
 	}
 
 	public void scaleBitmap(Bitmap toScale) {
 		BitmapFactory.Options options = new BitmapFactory.Options();
 		options.inSampleSize = 4;
 
 	}
 	
	public int genCronusID(Context con){
 		CronusApp appthis = ((CronusApp)((MainActivity)con).getApplication());
 		appthis.feedIDgen++;
 		return appthis.feedIDgen;
 		
 	}
 	
 	
 }
