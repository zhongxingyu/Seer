 package com.mhacks.dealradar;
 
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Point;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.Display;
 import android.view.View;
 import android.widget.Button;
 import android.widget.NumberPicker;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.support.v4.app.FragmentActivity;
 
 import com.mhacks.dealradar.objects.Advertisement;
 import com.mhacks.dealradar.support.TouchImageView;
 import com.mhacks.dealradar.support.WifiReceiver;
 import com.parse.FindCallback;
 import com.parse.ParseException;
 import com.parse.ParseObject;
 import com.parse.ParseQuery;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.util.List;
 
 /**
  * Created by sdickson on 8/6/13.
  */
 public class FullScreenImageView extends FragmentActivity
 {
     String caption, image_path;
     Bitmap raw_image;
     RelativeLayout fullscreenimagelayout;
     TouchImageView fullscreenimage;
     TextView fullscreencaption, fullscreenshare, fullscreenrate;
     ProgressDialog progressDialog;
     FullScreenImageView fsiv;
     Context context;
     Advertisement ad;
 
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.fullscreenimageview);
         fsiv = this;
         context = this;
         Intent data = this.getIntent();
         progressDialog = new ProgressDialog(this);
         progressDialog.setCancelable(false);
         progressDialog.setTitle("");
         progressDialog.setMessage("Loading...");
 
         if(data != null)
         {
             image_path = data.getStringExtra("image");
             caption = data.getStringExtra("caption");
             ad = (Advertisement) data.getSerializableExtra("ad");
             //raw_image = data.getParcelableExtra("raw_image");
             fullscreenimagelayout = (RelativeLayout) findViewById(R.id.fullscreenimage_layout);
             fullscreenimage = (TouchImageView) findViewById(R.id.fullscreenimage);
             fullscreencaption = (TextView) findViewById(R.id.fullscreenimage_caption);
             fullscreencaption.setTypeface(DealRadar.myriadProRegular);
             fullscreenshare = (TextView) findViewById(R.id.fullscreenimage_share);
             fullscreenshare.setTypeface(DealRadar.myriadProSemiBold);
             fullscreenrate = (TextView) findViewById(R.id.fullscreenimage_rate);
             fullscreenrate.setTypeface(DealRadar.myriadProSemiBold);
             fullscreenshare.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View view)
                 {
                     try
                     {
                         Intent share = new Intent(Intent.ACTION_SEND);
                         share.setType("image/png");
 
                         share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(image_path)));
                         share.putExtra(Intent.EXTRA_TEXT, "Deal Radar");
 
                         startActivity(Intent.createChooser(share, "Share Deal"));
                     }
                     catch(Exception e){}
                 }
             });
             fullscreenrate.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View view)
                 {
                     final Dialog d = new Dialog(context);
                     d.setCanceledOnTouchOutside(false);
                     d.setTitle("Rate this Deal");
                     d.setContentView(R.layout.rating_dialog);
                     Button cancel = (Button) d.findViewById(R.id.numberPicker_cancel);
                     Button rate = (Button) d.findViewById(R.id.numberPicker_rate);
                     final NumberPicker np = (NumberPicker) d.findViewById(R.id.numberPicker);
                     np.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
                     np.setMaxValue(5);
                     np.setMinValue(1);
                     np.setWrapSelectorWheel(false);
                     rate.setOnClickListener(new View.OnClickListener() {
                         @Override
                         public void onClick(View v)
                         {
                             if(ad != null)
                             {
                                 ParseQuery<ParseObject> query = ParseQuery.getQuery("Routers");
                                 query.whereEqualTo("objectId", ad.objectId);
                                 query.findInBackground(new FindCallback<ParseObject>() {
                                     public void done(List<ParseObject> result, ParseException e) {
                                         if (e == null)
                                         {
                                             for (ParseObject parse : result)
                                             {
                                                 int numRatings = parse.getInt("Num_Ratings");
                                                int newRating = parse.getInt("Rating") + np.getValue();
                                                ad.rating = Math.round(newRating / numRatings);
                                                 parse.put("Rating", newRating);
                                                 parse.increment("Num_Ratings");
                                                 parse.saveInBackground();
                                                 break;
                                             }
                                         }
                                     }
                                 });
                             }
                             d.dismiss();
                         }
                     });
                     cancel.setOnClickListener(new View.OnClickListener() {
                         @Override
                         public void onClick(View v) {
                             d.dismiss();
                         }
                     });
                     d.show();
                 }
             });
         }
     }
 
     public void onResume()
     {
         super.onResume();
 
         if(image_path != null && fullscreenimage != null)
         {
             try
             {
                 new loadImage().execute();
             }
             catch(Exception e)
             {
                 e.printStackTrace();
             }
         }
 
         if(caption != null && fullscreencaption != null)
         {
             fullscreencaption.setText(caption);
         }
     }
 
     private class loadImage extends AsyncTask<Object, Integer, Void>
     {
         Bitmap image;
 
         protected void onPreExecute()
         {
             //progressDialog.show();
         }
 
         protected Void doInBackground(Object... arg0)
         {
             try
             {
                 File f = new File(image_path);
                 BitmapFactory.Options o = new BitmapFactory.Options();
                 o.inJustDecodeBounds = true;
                 BitmapFactory.decodeStream(new FileInputStream(f),null,o);
                 Display display = getWindowManager().getDefaultDisplay();
                 Point size = new Point();
                 display.getSize(size);
                 int width = size.x;
                 int height = size.y;
 
                     int width_tmp=o.outWidth, height_tmp=o.outHeight;
                     int scale=1;
                     while(true){
                         if(width_tmp/2<width || height_tmp/2<height)
                             break;
                         width_tmp/=2;
                         height_tmp/=2;
                         scale*=2;
                     }
 
                     BitmapFactory.Options o2 = new BitmapFactory.Options();
                     o2.inSampleSize=scale;
                     image = BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
                     raw_image = image;
             }
             catch(Exception e)
             {}
             return null;
         }
 
         protected void onPostExecute(Void v)
         {
             WifiReceiver.setInterrupts(false);
 
             if(image != null)
             {
                 fullscreenimage.setImageBitmap(image);
                 //progressDialog.dismiss();
             }
         }
     }
 }
