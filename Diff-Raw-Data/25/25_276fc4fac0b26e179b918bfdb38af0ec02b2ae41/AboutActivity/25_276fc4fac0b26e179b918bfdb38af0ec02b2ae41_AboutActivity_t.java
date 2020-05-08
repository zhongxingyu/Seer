 package tk.crazysoft.ego;
 
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
import android.graphics.Point;
 import android.os.AsyncTask;
import android.os.Build;
 import android.os.Bundle;
 import android.support.v7.app.ActionBarActivity;
 import android.view.MenuItem;
 import android.widget.ImageView;
 
 import java.lang.ref.WeakReference;
 
 public class AboutActivity extends ActionBarActivity {
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.about_activity);
         getSupportActionBar().setDisplayHomeAsUpEnabled(true);
     }
 
     @Override
     protected void onStart() {
         super.onStart();
 
         loadBitmap((ImageView)findViewById(R.id.about_imageViewRK), R.drawable.logo_rk);
         loadBitmap((ImageView)findViewById(R.id.about_imageViewJKUTNF), R.drawable.logo_jku_tnf);
     }
 
     private void loadBitmap(ImageView imageView, int id) {
         BitmapWorkerTask task = new BitmapWorkerTask(imageView);
         task.execute(id);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             // Respond to the action bar's Up/Home button
             case android.R.id.home:
                 this.finish();
                 return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
     private class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {
         private final WeakReference<ImageView> imageViewReference;
         private int data = 0;
 
         public BitmapWorkerTask(ImageView imageView) {
             // Use a WeakReference to ensure the ImageView can be garbage collected
             imageViewReference = new WeakReference<ImageView>(imageView);
         }
 
         // Decode image in background.
         @Override
         protected Bitmap doInBackground(Integer... params) {
             if (imageViewReference == null) {
                 return null;
             }
 
             data = params[0];
            Point size = new Point();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                ImageView imageView = imageViewReference.get();
                size.x = imageView.getMaxWidth();
                size.y = imageView.getMaxHeight();
            } else {
                size.x = getWindowManager().getDefaultDisplay().getWidth();
                size.y = getWindowManager().getDefaultDisplay().getHeight();
            }
 
            return decodeSampledBitmapFromResource(getResources(), data, size.y, size.x);
         }
 
         private Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {
             // First decode with inJustDecodeBounds=true to check dimensions
             final BitmapFactory.Options options = new BitmapFactory.Options();
             options.inJustDecodeBounds = true;
             BitmapFactory.decodeResource(res, resId, options);
 
             // Calculate inSampleSize
             options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
 
             // Decode bitmap with inSampleSize set
             options.inJustDecodeBounds = false;
             return BitmapFactory.decodeResource(res, resId, options);
         }
 
         private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
             // Raw height and width of image
             final int height = options.outHeight;
             final int width = options.outWidth;
             int inSampleSize = 1;
 
             if (height > reqHeight || width > reqWidth) {
                 final int halfHeight = height / 2;
                 final int halfWidth = width / 2;
 
                 // Calculate the largest inSampleSize value that is a power of 2 and keeps both
                 // height and width larger than the requested height and width.
                 while ((halfHeight / inSampleSize) > reqHeight
                         && (halfWidth / inSampleSize) > reqWidth) {
                     inSampleSize *= 2;
                 }
             }
 
             return inSampleSize;
         }
 
         // Once complete, see if ImageView is still around and set bitmap.
         @Override
         protected void onPostExecute(Bitmap bitmap) {
             if (imageViewReference != null && bitmap != null) {
                 final ImageView imageView = imageViewReference.get();
                 if (imageView != null) {
                     imageView.setImageBitmap(bitmap);
                 }
             }
         }
     }
 }
