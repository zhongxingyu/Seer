 package com.appspot.manup.signature;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Path;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Environment;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.Toast;
 
 // Code from com.appspot.manup.FingerPaint
 public class Signature extends DataUploadHelperActivity
 {
     private static final String TAG = Signature.class.getSimpleName();
     private MyView myView;
     private Paint mPaint;
     static boolean clearCanvas;
 
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(myView = new MyView(this));
 
         mPaint = new Paint();
         mPaint.setAntiAlias(true);
         mPaint.setDither(true);
         mPaint.setColor(Color.BLACK);
         mPaint.setStyle(Paint.Style.STROKE);
         mPaint.setStrokeJoin(Paint.Join.ROUND);
         mPaint.setStrokeCap(Paint.Cap.ROUND);
         mPaint.setStrokeWidth(10);
 
         startWatchingExternalStorage();
     }
 
     @Override
     protected void onDestroy()
     {
         // TODO Auto-generated method stub
         stopWatchingExternalStorage();
         super.onDestroy();
     }
 
     public class MyView extends View
     {
         private Bitmap mBitmap;
         private Canvas mCanvas;
         private Path mPath;
         private Paint mBitmapPaint;
 
         public MyView(Context c)
         {
             super(c);
 
             clearCanvas = true;
             mBitmap = Bitmap.createBitmap(320, 480, Bitmap.Config.ARGB_8888);
             mCanvas = new Canvas(mBitmap);
             mPath = new Path();
             mBitmapPaint = new Paint(Paint.DITHER_FLAG);
         }
 
         @Override
         protected void onSizeChanged(int w, int h, int oldw, int oldh)
         {
             super.onSizeChanged(w, h, oldw, oldh);
             mCanvas.setBitmap(mBitmap = Bitmap.createBitmap(w, h,
                     Bitmap.Config.ARGB_8888));
         }
 
         @Override
         protected void onDraw(Canvas canvas)
         {
             canvas.drawColor(Color.WHITE);
             canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
             canvas.drawPath(mPath, mPaint);
         }
 
         private float mX, mY;
         private static final float TOUCH_TOLERANCE = 4;
 
         private void touch_start(float x, float y)
         {
             mPath.reset();
             mPath.moveTo(x, y);
             mX = x;
             mY = y;
         }
 
         private void touch_move(float x, float y)
         {
             float dx = Math.abs(x - mX);
             float dy = Math.abs(y - mY);
             if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE)
             {
                 mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
                 mX = x;
                 mY = y;
             }
         }
 
         private void touch_up()
         {
             mPath.lineTo(mX, mY);
             // commit the path to our offscreen
             mCanvas.drawPath(mPath, mPaint);
             // kill this so we don't double draw
             mPath.reset();
             clearCanvas = false;
         }
 
         @Override
         public boolean onTouchEvent(MotionEvent event)
         {
             float x = event.getX();
             float y = event.getY();
 
             switch (event.getAction())
             {
                 case MotionEvent.ACTION_DOWN:
                     touch_start(x, y);
                     invalidate();
                     break;
                 case MotionEvent.ACTION_MOVE:
                     touch_move(x, y);
                     invalidate();
                     break;
                 case MotionEvent.ACTION_UP:
                     touch_up();
                     invalidate();
                     break;
             }
             return true;
         }
 
         public Bitmap getBitMap()
         {
             return mBitmap;
         }
     }
 
     private static final int SUBMIT = Menu.FIRST, CLEAR = 2, LIST = 3, SETTINGS = 4;
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu)
     {
         super.onCreateOptionsMenu(menu);
 
         menu.add(0, SUBMIT, 0, "Submit").setShortcut('7', 's');
         menu.add(0, CLEAR, 0, "Clear").setShortcut('3', 'c');
         menu.add(0, LIST, 0, "Failed uploads").setShortcut('4', 'f');
        menu.add(0, SETTINGS, 0, "Settigns");
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item)
     {
         mPaint.setXfermode(null);
         mPaint.setAlpha(0xFF);
 
         switch (item.getItemId())
         {
             case SUBMIT:
                 onSubmit();
                 return true;
             case CLEAR:
                 setContentView(myView = new MyView(this));
                 return true;
             case LIST:
                 startActivity(new Intent(this, SigsNotUploaded.class));
                 return true;
             case SETTINGS:
                 startActivity(new Intent(this, SignaturePreferenceActivity.class));
                 return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
     // Monitor state of external storage
     // Not auto updating yet
     BroadcastReceiver mExternalStorageReceiver;
     boolean mExternalStorageAvailable = false;
     boolean mExternalStorageWriteable = false;
 
     void updateExternalStorageState()
     {
         String state = Environment.getExternalStorageState();
         if (Environment.MEDIA_MOUNTED.equals(state))
         {
             mExternalStorageAvailable = mExternalStorageWriteable = true;
             Toast.makeText(this, "External storage is writable", Toast.LENGTH_SHORT).show();
         }
         else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state))
         {
             mExternalStorageAvailable = true;
             mExternalStorageWriteable = false;
             Toast.makeText(this, "External storage is not writable", Toast.LENGTH_SHORT).show();
         }
         else
         {
             mExternalStorageAvailable = mExternalStorageWriteable = false;
             Toast.makeText(this, "External storage is not mounted", Toast.LENGTH_SHORT).show();
         }
     }
 
     void startWatchingExternalStorage()
     {
         mExternalStorageReceiver = new BroadcastReceiver()
         {
             @Override
             public void onReceive(Context context, Intent intent)
             {
                 Log.i("test", "Storage: " + intent.getData());
                 updateExternalStorageState();
             }
         };
         IntentFilter filter = new IntentFilter();
         filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
         filter.addAction(Intent.ACTION_MEDIA_REMOVED);
         registerReceiver(mExternalStorageReceiver, filter);
         updateExternalStorageState();
     }
 
     void stopWatchingExternalStorage()
     {
         unregisterReceiver(mExternalStorageReceiver);
     }
 
     private void onSubmit()
     {
         if (clearCanvas)
         {
             Toast.makeText(this, "A signature is required", Toast.LENGTH_SHORT).show();
             return;
         }
         if (mExternalStorageWriteable)
         {
             final SignatureDatabase dataHelper = SignatureDatabase.getInstance(this);
             final long id = dataHelper.addSignature(Long.toString(studentId));
             new WriteToExternalStorage().execute(id, myView.getBitMap());
         }
         else
         {
             Toast.makeText(this, "Cannot write to external storage", Toast.LENGTH_SHORT).show();
         }
     }
 
     static long studentId = System.currentTimeMillis();
 
     @Override
     public void runToastMessageOnUiThread(final String s)
     {
         runOnUiThread(new Runnable()
         {
             @Override
             public void run()
             {
                 Log.d(TAG, "upload message thread started");
                 Toast.makeText(Signature.this, s, Toast.LENGTH_SHORT).show();
             }
         });
     }
 
     private static final int BITMAP_FILE_QUALITY = 100;
     private final class WriteToExternalStorage extends AsyncTask<Object, Void, Long>
     {
         @Override
         protected Long doInBackground(final Object... params)
         {
             final long id = (Long) params[0];
             final SignatureDatabase dataHelper = SignatureDatabase.getInstance(Signature.this);
             final File imageFile = dataHelper.getImageFile(id);
             if (imageFile == null)
             {
                 Log.w(TAG, "Image file cannot be retrieved from database");
                 return null;
             }
             try
             {
                 imageFile.createNewFile();
             }
             catch (final IOException e)
             {
                 Log.w(TAG, "Error creating file", e);
                 return null;
             }
             final Bitmap b = (Bitmap) params[1];
             FileOutputStream fos = null;
             try
             {
                 fos = new FileOutputStream(imageFile);
                 if (!b.compress(Bitmap.CompressFormat.PNG, BITMAP_FILE_QUALITY, fos))
                 {
                     return null;
                 }
             }
             catch (final IOException e)
             {
                 Log.w(TAG, "Error writing file", e);
                 return null;
             }
             finally
             {
                 if (fos != null)
                 {
                     try
                     {
                         fos.close();
                     }
                     catch (final IOException e)
                     {
                         Log.w(TAG, "Error closing output stream", e);
                     }
                 }
             }
             if (dataHelper.signatureCaptured(id))
             {
                 return id;
             }
             return null;
         }
 
         @Override
         protected void onPostExecute(Long id)
         {
             if (id != null)
             {
                 upload(id);
                 setContentView(myView = new MyView(Signature.this));
             }
             else
             {
                 Toast.makeText(Signature.this, "Write failed", Toast.LENGTH_SHORT).show();
             }
         }
 
     }
 
 }
