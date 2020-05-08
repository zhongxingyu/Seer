 package com.cajama.malaria;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.provider.MediaStore;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.BaseAdapter;
 import android.widget.GridView;
 import android.widget.Spinner;
 import android.widget.ViewFlipper;
 
 import com.cajama.android.customviews.SquareImageView;
 
 import java.io.File;
 import java.text.SimpleDateFormat;
 import java.util.Vector;
 import java.util.Date;
 
 public class NewReportActivity extends Activity {
     ViewFlipper VF;
     GridView new_report_photos_layout;
     ImageAdapter images;
     private static final int CAMERA_REQUEST = 1888;
     private static final int PHOTO_REQUEST = 4214;
     private Uri fileUri;
     private String imageFilePath;
     private int displayedchild;
     private Resources res;
     private String[] step_subtitles;
 
     private class myBitmap {
         String path;
         Bitmap image;
     }
 
     private class ImageAdapter extends BaseAdapter {
         private Context mContext;
         private Vector<myBitmap> images = new Vector<myBitmap>();
 
         public ImageAdapter(Context c) {
             mContext = c;
         }
 
         public void AddImage(myBitmap b) {
             images.add(b);
         }
 
         public void remove(int pos) {
             images.remove(pos);
         }
 
         @Override
         public int getCount() {
             return images.size();
         }
 
         @Override
         public myBitmap getItem(int arg0) {
             return images.get(arg0);
         }
 
         @Override
         public long getItemId(int arg0) {
             return arg0;
         }
 
         @Override
         public View getView(int position, View convertView, ViewGroup parent) {
             SquareImageView img;
             if(convertView ==null) {
                 img = new SquareImageView(mContext);
             }
             else {
                 img = (SquareImageView)convertView;
             }
 
             img.setImageBitmap(images.get(position).image);
             img.setScaleType(SquareImageView.ScaleType.CENTER_CROP);
             return img;
         }
 
     }
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_new_report);
 
         Spinner spinner = (Spinner) findViewById(R.id.gender_spinner);
         ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                 R.array.gender_array, android.R.layout.simple_spinner_item);
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         spinner.setAdapter(adapter);
 
         VF = (ViewFlipper) findViewById(R.id.viewFlipper);
         getActionBar().setSubtitle("Step 1 of " + VF.getChildCount());
 
         images = new ImageAdapter(this);
         new_report_photos_layout = (GridView) findViewById(R.id.new_report_photos_layout);
         new_report_photos_layout.setEmptyView(findViewById(R.id.empty_list_view));
         new_report_photos_layout.setAdapter(images);
 
         new_report_photos_layout.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                 Intent intent = new Intent(getApplicationContext(), FullscreenPhotoActivity.class);
 
                 intent.putExtra("pos", position);
                 intent.putExtra("path", images.getItem(position).path);
 
                 startActivityForResult(intent, PHOTO_REQUEST);
             }
         });
 
         res = getResources();
        step_subtitles = new String[]{res.getString(R.string.patient_details), res.getString(R.string.slide_photos), res.getString(R.string.diagnosis), "4", "5"};
     }
 
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
         displayedchild = VF.getDisplayedChild();
         getActionBar().setSubtitle(String.format("Step %d of %d - %s", displayedchild + 1, VF.getChildCount(), step_subtitles[displayedchild]));
 
         switch(displayedchild) {
             case 0: menu.findItem(R.id.action_prev).setIcon(R.drawable.navigation_cancel).setTitle(R.string.cancel);
                     menu.findItem(R.id.action_photo).setVisible(false);
                     menu.findItem(R.id.action_next).setIcon(R.drawable.navigation_forward).setTitle(R.string.next);
                     break;
             case 1: menu.findItem(R.id.action_prev).setIcon(R.drawable.navigation_back2).setTitle(R.string.back);
                     menu.findItem(R.id.action_photo).setVisible(true);
                     menu.findItem(R.id.action_next).setIcon(R.drawable.navigation_forward).setTitle(R.string.next);
                     break;
             case 2: menu.findItem(R.id.action_prev).setIcon(R.drawable.navigation_back2).setTitle(R.string.back);
                     menu.findItem(R.id.action_photo).setVisible(false);
                     menu.findItem(R.id.action_next).setIcon(R.drawable.navigation_forward).setTitle(R.string.next);
                     break;
             case 3: menu.findItem(R.id.action_prev).setIcon(R.drawable.navigation_back2).setTitle(R.string.back);
                     menu.findItem(R.id.action_photo).setVisible(false);
                     menu.findItem(R.id.action_next).setIcon(R.drawable.navigation_forward).setTitle(R.string.next);
                     break;
             case 4: menu.findItem(R.id.action_prev).setIcon(R.drawable.navigation_back2).setTitle(R.string.back);
                     menu.findItem(R.id.action_photo).setVisible(false);
                     menu.findItem(R.id.action_next).setIcon(R.drawable.navigation_accept).setTitle(R.string.submit);
                     break;
             default: break;
         }
 
         return super.onPrepareOptionsMenu(menu);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.new_report, menu);
         return super.onCreateOptionsMenu(menu);
     }
 
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.action_prev:
                 invalidateOptionsMenu();
                 if (VF.getDisplayedChild() == 0) {
                     AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                     alertDialogBuilder
                             .setTitle(R.string.warning)
                             .setMessage(R.string.new_report_cancel_warning)
                             .setCancelable(false)
                             .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                 public void onClick(DialogInterface dialog,int id) {
                                     for (int c=0; c<images.getCount(); c++) {
                                         File file = new File(images.getItem(c).path);
                                         file.delete();
                                     }
 
                                     finish();
                                 }
                             })
                             .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                 public void onClick(DialogInterface dialog,int id) {
                                     dialog.cancel();
                                 }
                             });
 
                     AlertDialog alertDialog = alertDialogBuilder.create();
 
                     alertDialog.show();
                 } else {
                     VF.showPrevious();
                 }
                 return true;
             case R.id.action_next:
                 invalidateOptionsMenu();
                 if(VF.getDisplayedChild() != VF.getChildCount()-1) {
                     VF.showNext();
                 }
                 return true;
             case R.id.action_photo:
                 Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                 String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                imageFilePath = getExternalFilesDir(Environment.DIRECTORY_PICTURES) +  "/" + timeStamp + "_picture.jpg";
 
                 File imageFile = new File(imageFilePath);
                 fileUri = Uri.fromFile(imageFile);
                 cameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, fileUri);
                 startActivityForResult(cameraIntent, CAMERA_REQUEST);
 
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     public void onItemSelected(AdapterView<?> parent, View view,
                                int pos, long id) {
         // An item was selected. You can retrieve the selected item using
         // parent.getItemAtPosition(pos)
     }
 
     public void onNothingSelected(AdapterView<?> parent) {
         // Another interface callback
     }
 
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
             BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
 
             Bitmap bmp = BitmapFactory.decodeFile(imageFilePath, bmpFactoryOptions);
 
             myBitmap bmpp = new myBitmap();
             bmpp.image = bmp;
             bmpp.path = imageFilePath;
 
             images.AddImage(bmpp);
             images.notifyDataSetChanged();
         } else if (requestCode == PHOTO_REQUEST && resultCode == RESULT_OK) {
             int pos = data.getIntExtra("pos", -1);
 
             if (pos != -1 ){
                 File file = new File(images.getItem(pos).path);
                 file.delete();
 
                 images.remove(pos);
                 images.notifyDataSetChanged();
             }
         }
     }
 
     @Override
     public void onBackPressed() {
         invalidateOptionsMenu();
         if (VF.getDisplayedChild() == 0) {
             AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
             alertDialogBuilder
                     .setTitle(R.string.warning)
                     .setMessage(R.string.new_report_cancel_warning)
                     .setCancelable(false)
                     .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog,int id) {
                             for (int c=0; c<images.getCount(); c++) {
                                 File file = new File(images.getItem(c).path);
                                 file.delete();
                             }
 
                             finish();
                         }
                     })
                     .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog,int id) {
                             dialog.cancel();
                         }
                     });
 
             AlertDialog alertDialog = alertDialogBuilder.create();
 
             alertDialog.show();
         } else {
             VF.showPrevious();
         }
     }
 }
