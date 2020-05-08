 package net.nowatch.ui;
 
 import java.io.File;
 
 import net.nowatch.Main;
 import net.nowatch.R;
 import net.nowatch.network.GetFile;
 import net.nowatch.network.Network;
 import net.nowatch.service.NWService;
 import net.nowatch.utils.Db;
 import net.nowatch.utils.Item;
 import android.app.Activity;
 import android.content.ActivityNotFoundException;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.Bundle;
import android.os.Environment;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.webkit.WebView;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class ItemInfo extends Activity {
 
     private final String TAG = Main.TAG + "InfoActivity";
     private final String REQ = "SELECT feeds.title, items.title, items.description, feeds.image, "
             + "items.file_uri, items.file_size, items.file_type, items.status, items.image, items.bookmark "
             + "FROM items INNER JOIN feeds ON items.feed_id=feeds._id WHERE items._id=";
     private final String STYLE = "<style>*{color: black;}</style>";
     private final String PRE = "<meta http-equiv=\"Content-Type\" content=\"application/xhtml+text; charset=UTF-8\"/>"
             + STYLE;
     private final Context ctxt = this;
     private final int IMG_DIP = 96;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_item_info);
 
         // Screen metrics (for dip to px conversion)
         DisplayMetrics dm = new DisplayMetrics();
         getWindowManager().getDefaultDisplay().getMetrics(dm);
         int image_size = (int) (IMG_DIP * dm.density + 0.5f);
 
         // Get item information
         Bundle extra = getIntent().getExtras();
         final int item_id = extra.getInt(Item.EXTRA_ITEM_ID);
         SQLiteDatabase db = (new Db(ctxt)).openDb();
         Cursor c = db.rawQuery(REQ + item_id, null);
         c.moveToFirst();
         final String title = c.getString(1);
         ((TextView) findViewById(R.id.title)).setText(title);
         ((WebView) findViewById(R.id.desc)).loadData(PRE + c.getString(2), "text/html", "utf-8");
         findViewById(R.id.desc).setBackgroundColor(0);
 
         // Try to get item logo, then podcast logo and finally application logo
         final int min_size = 200;
         ImageView logo = (ImageView) findViewById(R.id.logo);
         byte[] logo_item_byte = c.getBlob(8);
         if (logo_item_byte != null && logo_item_byte.length > min_size) {
             logo.setImageBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeByteArray(
                     logo_item_byte, 0, logo_item_byte.length), image_size, image_size, true));
         } else {
             byte[] logo_podcast_byte = c.getBlob(3);
             if (logo_podcast_byte != null && logo_podcast_byte.length > min_size) {
                 logo.setImageBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeByteArray(
                         logo_podcast_byte, 0, logo_podcast_byte.length), image_size, image_size,
                         true));
             } else {
                 logo.setImageResource(R.drawable.icon);
             }
         }
         // File
         final String file_uri = c.getString(4);
         // final String file_size = c.getString(5);
         final String file_type = c.getString(6);
         final int status = c.getInt(7);
         final int bookmarked = c.getInt(9);
 
         // Close Db
         c.close();
         db.close();
 
         // Set status
         if (status == Item.STATUS_NEW) {
             changeStatus(ctxt, item_id, Item.STATUS_UNREAD);
         }
 
         // Menu buttons
         findViewById(R.id.btn_back).setVisibility(View.VISIBLE);
         findViewById(R.id.btn_play).setVisibility(View.VISIBLE);
         findViewById(R.id.btn_download).setVisibility(View.VISIBLE);
         findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 finish();
             }
         });
 
         // Buttons
        // TODO: Handle play/download buttons separatly
         setBookmark(item_id, bookmarked);
         if (status == Item.STATUS_DOWNLOADING) {
             changeButton(R.id.btn_download, getString(R.string.btn_download), false, null);
             changeButton(R.id.btn_play, getString(R.string.btn_stream), true,
                     new View.OnClickListener() {
                         public void onClick(View v) {
                             // Stream the file
                             if (new Network(ctxt).isMobileAllowed()) {
                                 viewFile(file_uri, file_type, item_id);
                             } else {
                                 Toast.makeText(ctxt, R.string.toast_nomobiletraffic,
                                         Toast.LENGTH_LONG).show();
                             }
                         }
                     });
         } else if (status == Item.STATUS_DL_READ || status == Item.STATUS_DL_UNREAD) {
             changeButton(R.id.btn_download, getString(R.string.btn_download), false, null);
             changeButton(R.id.btn_play, getString(R.string.btn_play), true,
                     new View.OnClickListener() {
                         public void onClick(View v) {
                             // Read the local file
                            viewFile("file://" + Environment.getExternalStorageDirectory().toString() + "/"
                                + GetFile.PATH_PODCASTS + "/" + new File(file_uri).getName(), file_type, item_id);
                         }
                     });
         } else {
             changeButton(R.id.btn_play, getString(R.string.btn_stream), true,
                     new View.OnClickListener() {
                         public void onClick(View v) {
                             // Stream the file
                             if (new Network(ctxt).isMobileAllowed()) {
                                 viewFile(file_uri, file_type, item_id);
                             } else {
                                 Toast.makeText(ctxt, R.string.toast_nomobiletraffic,
                                         Toast.LENGTH_LONG).show();
                             }
                         }
                     });
             changeButton(R.id.btn_download, getString(R.string.btn_download), true,
                     new View.OnClickListener() {
                         public void onClick(View v) {
                             // Download the file
                             if (new Network(ctxt).isMobileAllowed()) {
                                 downloadFile(item_id);
                             } else {
                                 Toast.makeText(ctxt, R.string.toast_nomobiletraffic,
                                         Toast.LENGTH_LONG).show();
                             }
                         }
                     });
         }
     }
 
     public static void changeStatus(Context ctxt, int id, int status) {
         SQLiteDatabase db = (new Db(ctxt)).openDb();
         ContentValues value = new ContentValues();
         value.put("status", status);
         db.update("items", value, "_id=?", new String[] { id + "" });
         db.close();
     }
 
     private void setBookmark(final int item_id, int bookmarked) {
         ImageButton btn_bookmark = (ImageButton) findViewById(R.id.btn_bookmark);
         if (bookmarked == 1) {
             btn_bookmark.setImageResource(R.drawable.btn_bookmark);
             btn_bookmark.setOnClickListener(new View.OnClickListener() {
                 public void onClick(View v) {
                     setBookmark(item_id, 0);
                 }
             });
         } else {
             btn_bookmark.setImageResource(R.drawable.btn_bookmark2);
             btn_bookmark.setOnClickListener(new View.OnClickListener() {
                 public void onClick(View v) {
                     setBookmark(item_id, 1);
                 }
             });
         }
         SQLiteDatabase db = (new Db(ctxt)).openDb();
         ContentValues value = new ContentValues();
         value.put("bookmark", bookmarked);
         db.update("items", value, "_id=?", new String[] { item_id + "" });
         db.close();
     }
 
     private void downloadFile(int item_id) {
         // Launch action on the service
         Intent intent = new Intent(ItemInfo.this, NWService.class);
         intent.setAction(NWService.ACTION_ADD);
         intent.putExtra(Item.EXTRA_ITEM_ID, item_id);
         startService(intent);
         // Desactivate the button
         changeButton(R.id.btn_download, getString(R.string.btn_download), false, null);
     }
 
     private void viewFile(String file, String type, int item_id) {
         Log.v(TAG, "file=" + file + ", type=" + type);
         if (type.equals(new String("video/x-m4v"))) {
             type = "video/mp4";
         } else if (type.equals(new String("audio/x-m4a"))) {
             type = "audio/mp4";
         }
         Intent i = new Intent(Intent.ACTION_VIEW);
         try {
             i.setDataAndType(Uri.parse(file), type);
             startActivity(i);
             changeStatus(ctxt, item_id, Item.STATUS_READ);
         } catch (ActivityNotFoundException e) {
             Log.e(TAG, e.getMessage());
             try {
                 i.setType("video/*");
                 startActivity(i);
                 changeStatus(ctxt, item_id, Item.STATUS_READ);
             } catch (ActivityNotFoundException e1) {
                 Log.e(TAG, e1.getMessage());
                 Toast.makeText(ctxt, R.string.toast_notsupported, Toast.LENGTH_LONG).show();
             }
         }
     }
 
     private void changeButton(int id, String txt, boolean clickable, OnClickListener onClickListener) {
         Button btn = (Button) findViewById(id);
         btn.setClickable(clickable);
         btn.setEnabled(clickable);
         btn.setText(txt);
         if (onClickListener != null) {
             btn.setOnClickListener(onClickListener);
         }
     }
 
 }
