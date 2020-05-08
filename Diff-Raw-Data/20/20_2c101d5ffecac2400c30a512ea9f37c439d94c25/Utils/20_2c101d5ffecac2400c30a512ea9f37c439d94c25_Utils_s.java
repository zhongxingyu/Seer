 package ru.aim.anotheryetbashclient.helper;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.Rect;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.graphics.drawable.TransitionDrawable;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.http.AndroidHttpClient;
 import android.os.Build;
 import android.renderscript.Allocation;
 import android.renderscript.Element;
 import android.renderscript.RenderScript;
 import android.renderscript.ScriptIntrinsicBlur;
 import android.support.v4.content.LocalBroadcastManager;
 import android.util.Log;
 import android.view.View;
 import org.apache.http.client.methods.HttpGet;
 import ru.aim.anotheryetbashclient.ActionsAndIntents;
 import ru.aim.anotheryetbashclient.helper.f.Block;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.List;
 
 @SuppressWarnings("unused")
 public final class Utils {
 
     public static final String UTF_8 = "UTF-8";
     public static final String WINDOWS_1215 = "windows-1251";
     static final String TAG = "Utils";
 
     private Utils() {
         throw new AssertionError();
     }
 
     public static boolean isNetworkAvailable(Context context) {
         ConnectivityManager connectivityManager
                 = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
         NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
         return activeNetworkInfo != null && activeNetworkInfo.isConnected();
     }
 
     public static boolean contains(long[] arr, long value) {
         //noinspection ForLoopReplaceableByForEach
         for (int i = 0; i < arr.length; i++) {
             if (arr[i] == value) {
                 return true;
             }
         }
         return false;
     }
 
     public static boolean ping(Context context) {
         AndroidHttpClient client = AndroidHttpClient.newInstance(Build.DEVICE);
         HttpGet httpGet = new HttpGet("http:\\google.com");
         Exception error = null;
         try {
             client.execute(httpGet);
         } catch (IOException e) {
             L.e(TAG, "Error while trying to ping google", e);
             error = e;
         }
         return error == null;
     }
 
     public static void rethrowWithRuntime(Block block) {
         try {
             block.apply();
         } catch (Exception e) {
             L.e(TAG, "Unexpected exception", e);
             throw new RuntimeException(e);
         }
     }
 
     public static String readFromStream(InputStream is) {
         try {
             BufferedReader reader = new BufferedReader(new InputStreamReader(is));
             String line = reader.readLine();
             StringBuilder sb = new StringBuilder();
             while (line != null) {
                 sb.append(line);
                 line = reader.readLine();
             }
             return sb.toString();
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
     }
 
     public static int[] toPrimitiveArray(List<Integer> list) {
         if (list == null || list.isEmpty()) {
             return new int[0];
         }
         int[] result = new int[list.size()];
         for (int i = 0; i < list.size(); i++) {
             result[i] = list.get(i);
         }
         return result;
     }
 
     public static void sendMessageIntent(Context context, String message) {
         LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(context);
         localBroadcastManager.sendBroadcast(new Intent(ActionsAndIntents.NOTIFY).putExtra(ActionsAndIntents.MESSAGE, message));
     }
 
     public static Bitmap takeScreenShot(Activity activity) {
         View view = activity.getWindow().getDecorView();
         view.setDrawingCacheEnabled(true);
         view.buildDrawingCache();
         Bitmap b1 = view.getDrawingCache();
         Rect frame = new Rect();
         activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
         int statusBarHeight = frame.top;
         int width = activity.getWindowManager().getDefaultDisplay().getWidth();
         int height = activity.getWindowManager().getDefaultDisplay().getHeight();
 
         Bitmap b = Bitmap.createBitmap(b1, 0, statusBarHeight, width, height - statusBarHeight);
         view.destroyDrawingCache();
         return b;
     }
 
     public static Bitmap takeBitmap(View view) {
         view.setDrawingCacheEnabled(true);
         view.buildDrawingCache();
         Bitmap b1 = view.getDrawingCache();
         Rect frame = new Rect();
         view.getWindowVisibleDisplayFrame(frame);
         int statusBarHeight = frame.top;
         int width = view.getWidth();
         int height = view.getHeight();
         Bitmap b = Bitmap.createBitmap(b1, 0, 0, width, height);
         view.destroyDrawingCache();
         return b;
     }
 
     public static Bitmap renderScriptBlur(Context context, Bitmap input) {
         return renderScriptBlur(context, input, 25.f);
     }
 
     public static TransitionDrawable makeTransition(Context context, View view) {
         Bitmap bitmap = takeBitmap(view);
         Bitmap blured = renderScriptBlur(context, bitmap, 10f);
         Drawable[] drawables = new Drawable[]{
                 new BitmapDrawable(context.getResources(), bitmap),
                 new BitmapDrawable(context.getResources(), blured)
         };
         return new TransitionDrawable(drawables);
     }
 
     public static Bitmap renderScriptBlur(Context context, Bitmap input, float radius) {
         Bitmap output = Bitmap.createBitmap(input);
         RenderScript rs = RenderScript.create(context);
         ScriptIntrinsicBlur theIntrinsic = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
         Allocation tmpIn = Allocation.createFromBitmap(rs, input);
         Allocation tmpOut = Allocation.createFromBitmap(rs, output);
         theIntrinsic.setRadius(radius);
         theIntrinsic.setInput(tmpIn);
         theIntrinsic.forEach(tmpOut);
         tmpOut.copyTo(output);
         return output;
     }
 
     public static Bitmap fastBlur(Bitmap sentBitmap, int radius) {
         Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);
 
         if (radius < 1) {
             return (null);
         }
 
         int w = bitmap.getWidth();
         int h = bitmap.getHeight();
 
         int[] pix = new int[w * h];
         Log.e("pix", w + " " + h + " " + pix.length);
         bitmap.getPixels(pix, 0, w, 0, 0, w, h);
 
         int wm = w - 1;
         int hm = h - 1;
         int wh = w * h;
         int div = radius + radius + 1;
 
         int r[] = new int[wh];
         int g[] = new int[wh];
         int b[] = new int[wh];
         int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
         int vmin[] = new int[Math.max(w, h)];
 
         int divsum = (div + 1) >> 1;
         divsum *= divsum;
         int dv[] = new int[256 * divsum];
         for (i = 0; i < 256 * divsum; i++) {
             dv[i] = (i / divsum);
         }
 
         yw = yi = 0;
 
         int[][] stack = new int[div][3];
         int stackpointer;
         int stackstart;
         int[] sir;
         int rbs;
         int r1 = radius + 1;
         int routsum, goutsum, boutsum;
         int rinsum, ginsum, binsum;
 
         for (y = 0; y < h; y++) {
             rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
             for (i = -radius; i <= radius; i++) {
                 p = pix[yi + Math.min(wm, Math.max(i, 0))];
                 sir = stack[i + radius];
                 sir[0] = (p & 0xff0000) >> 16;
                 sir[1] = (p & 0x00ff00) >> 8;
                 sir[2] = (p & 0x0000ff);
                 rbs = r1 - Math.abs(i);
                 rsum += sir[0] * rbs;
                 gsum += sir[1] * rbs;
                 bsum += sir[2] * rbs;
                 if (i > 0) {
                     rinsum += sir[0];
                     ginsum += sir[1];
                     binsum += sir[2];
                 } else {
                     routsum += sir[0];
                     goutsum += sir[1];
                     boutsum += sir[2];
                 }
             }
             stackpointer = radius;
 
             for (x = 0; x < w; x++) {
 
                 r[yi] = dv[rsum];
                 g[yi] = dv[gsum];
                 b[yi] = dv[bsum];
 
                 rsum -= routsum;
                 gsum -= goutsum;
                 bsum -= boutsum;
 
                 stackstart = stackpointer - radius + div;
                 sir = stack[stackstart % div];
 
                 routsum -= sir[0];
                 goutsum -= sir[1];
                 boutsum -= sir[2];
 
                 if (y == 0) {
                     vmin[x] = Math.min(x + radius + 1, wm);
                 }
                 p = pix[yw + vmin[x]];
 
                 sir[0] = (p & 0xff0000) >> 16;
                 sir[1] = (p & 0x00ff00) >> 8;
                 sir[2] = (p & 0x0000ff);
 
                 rinsum += sir[0];
                 ginsum += sir[1];
                 binsum += sir[2];
 
                 rsum += rinsum;
                 gsum += ginsum;
                 bsum += binsum;
 
                 stackpointer = (stackpointer + 1) % div;
                 sir = stack[(stackpointer) % div];
 
                 routsum += sir[0];
                 goutsum += sir[1];
                 boutsum += sir[2];
 
                 rinsum -= sir[0];
                 ginsum -= sir[1];
                 binsum -= sir[2];
 
                 yi++;
             }
             yw += w;
         }
         for (x = 0; x < w; x++) {
             rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
             yp = -radius * w;
             for (i = -radius; i <= radius; i++) {
                 yi = Math.max(0, yp) + x;
 
                 sir = stack[i + radius];
 
                 sir[0] = r[yi];
                 sir[1] = g[yi];
                 sir[2] = b[yi];
 
                 rbs = r1 - Math.abs(i);
 
                 rsum += r[yi] * rbs;
                 gsum += g[yi] * rbs;
                 bsum += b[yi] * rbs;
 
                 if (i > 0) {
                     rinsum += sir[0];
                     ginsum += sir[1];
                     binsum += sir[2];
                 } else {
                     routsum += sir[0];
                     goutsum += sir[1];
                     boutsum += sir[2];
                 }
 
                 if (i < hm) {
                     yp += w;
                 }
             }
             yi = x;
             stackpointer = radius;
             for (y = 0; y < h; y++) {
                 // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                 pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];
 
                 rsum -= routsum;
                 gsum -= goutsum;
                 bsum -= boutsum;
 
                 stackstart = stackpointer - radius + div;
                 sir = stack[stackstart % div];
 
                 routsum -= sir[0];
                 goutsum -= sir[1];
                 boutsum -= sir[2];
 
                 if (x == 0) {
                     vmin[y] = Math.min(y + r1, hm) * w;
                 }
                 p = x + vmin[y];
 
                 sir[0] = r[p];
                 sir[1] = g[p];
                 sir[2] = b[p];
 
                 rinsum += sir[0];
                 ginsum += sir[1];
                 binsum += sir[2];
 
                 rsum += rinsum;
                 gsum += ginsum;
                 bsum += binsum;
 
                 stackpointer = (stackpointer + 1) % div;
                 sir = stack[stackpointer];
 
                 routsum += sir[0];
                 goutsum += sir[1];
                 boutsum += sir[2];
 
                 rinsum -= sir[0];
                 ginsum -= sir[1];
                 binsum -= sir[2];
 
                 yi += w;
             }
         }
 
         Log.e("pix", w + " " + h + " " + pix.length);
         bitmap.setPixels(pix, 0, w, 0, 0, w, h);
 
         return (bitmap);
     }
 }
