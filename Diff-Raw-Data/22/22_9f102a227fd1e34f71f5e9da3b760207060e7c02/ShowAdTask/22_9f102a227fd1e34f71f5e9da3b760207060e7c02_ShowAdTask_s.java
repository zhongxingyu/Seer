 package org.alabs.nolotiro;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.AsyncTask;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 import org.alabs.nolotiro.exceptions.NolotiroException;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 // This task checks if the Ad photo is available in cache and downloads it if necessary
 public class ShowAdTask extends AsyncTask<Integer, Void, Bitmap> {
 
     private static final String TAG = "ShowAdTask";
     private Fragment fragment;
     private Context context;
     private NolotiroAPI api;
     private ImageView image;
 
     public ShowAdTask(Fragment _fragment) {
         api = NolotiroAPI.getInstance();
         context = _fragment.getActivity();
         fragment = _fragment;
     }
 
     protected void onPreExecute() {
         image = (ImageView)fragment.getActivity().findViewById(R.id.imageImage);
         image.setVisibility(View.INVISIBLE);
     }
 
     protected void onPostExecute(final Bitmap bitmap) {
         fragment.getActivity().runOnUiThread(new Runnable() {
             public void run() {
                 ProgressBar progress = (ProgressBar)fragment.getActivity().findViewById(R.id.progressBar);
                 image.setImageBitmap(bitmap);
                 image.setVisibility(View.VISIBLE);
                 progress.setVisibility(View.GONE);
             }
         });
     }
 
     protected Bitmap doInBackground(Integer... itemIds) {
         Integer itemId = itemIds[0];
         Ad ad = api.getAd(itemId);
         Bitmap bitmap = null;
 
         TextView title = (TextView)fragment.getActivity().findViewById(R.id.textTitle);
         TextView description = (TextView)fragment.getActivity().findViewById(R.id.textDescription);
         title.setText(ad.getTitle());
         description.setText(ad.getBody());
         fragment.getActivity().setTitle(ad.getTitle());
 
         try {
             String nolotiroDir = Utils.getNolotiroCacheDir(context);
             File f = new File(nolotiroDir);
             if (!f.exists()) {
                 Log.i(TAG, "Mkdir " + f);
                 f.mkdirs();
             }
 
             f = new File(nolotiroDir + ad.getImageFilename());
             Log.i("ShowAdTask", f.toString());
 
             if (f.exists()) {
                 bitmap = BitmapFactory.decodeFile(f.toString());
             } else {
                 URL url = new URL(api.getPhotoUrlFromAd(ad));
                 ByteArrayOutputStream bytes = new ByteArrayOutputStream();
 
                 bitmap = BitmapFactory.decodeStream((InputStream) url.getContent());
                 bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes);
 
                 f.createNewFile();
                 FileOutputStream fo = new FileOutputStream(f.getAbsoluteFile());
                 fo.write(bytes.toByteArray());
                 fo.close();
                 Log.i(TAG, "File saved to: " + f.getAbsolutePath());
             }
         } catch (NolotiroException e) {
             e.printStackTrace();
         } catch (MalformedURLException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
 
         return bitmap;
     }
}
