 package zhang.abel.memmo.android;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.GridView;
 import android.widget.TextView;
 import zhang.abel.memmo.android.adapters.ImageListAdapter;
 import zhang.abel.memmo.android.entities.Album;
 import zhang.abel.memmo.android.entities.Picture;
 import zhang.abel.memmo.android.repositories.AlbumRepository;
 import zhang.abel.memmo.android.repositories.PictureRepository;
 import zhang.abel.memmo.android.utils.IntentUtils;
 
 import java.io.File;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 
 public class AlbumActivity extends Activity {
 
     public static final int TAKE_PIC_MENU_ITEM_ID = 1;
     public static final int REMINDER_MENU_ITEM_ID = 2;
 
     private static final int ACTION_TAKE_PIC = 11;
 
     private PictureRepository pictureRepository;
     private Picture currentPicture;
     private Album currentAlbum;
     private AlbumRepository albumRepository;
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.album);
 
         albumRepository = new AlbumRepository();
         pictureRepository = new PictureRepository();
 
         currentAlbum = (Album) getIntent().getSerializableExtra(AlbumListActivity.SER_KEY);
 
         ((TextView) findViewById(R.id.currentAlbumName)).setText(currentAlbum.getName());
 
         TextView notificationInfo = (TextView) findViewById(R.id.notificationinfo);
         String prefName = "notification";
         SharedPreferences pref = getSharedPreferences(prefName, MODE_PRIVATE);
         if(pref.contains(currentAlbum.getName())) {
 
             Long time = pref.getLong(currentAlbum.getName(), 0);
             Calendar calendar = Calendar.getInstance();
             calendar.setTimeInMillis(time);
             SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
            notificationInfo.setText(R.string.msg_reminder_notification + simpleDateFormat.format(calendar.getTimeInMillis()));
         }
 
         initializeImageList();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
         MenuItem takePictureMenuItem = menu.add(0, TAKE_PIC_MENU_ITEM_ID, 0, R.string.menu_take_photo);
         MenuItem reminderMenuItem = menu.add(0, REMINDER_MENU_ITEM_ID, 1, R.string.menu_reminder);
         takePictureMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
         reminderMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case TAKE_PIC_MENU_ITEM_ID:
                 String intentName = MediaStore.ACTION_IMAGE_CAPTURE;
                 if (IntentUtils.isIntentAvailable(this, intentName)) {
                     dispatchTakePictureIntent();
                     initializeImageList();
                 } else {
                     showMessageBox("Can not call the Camera...");
                 }
                 return true;
             case REMINDER_MENU_ITEM_ID:
                 Intent intent = new Intent(this, NotificationSettingActivity.class);
                 Bundle bundle = new Bundle();
                 bundle.putSerializable(AlbumListActivity.SER_KEY, currentAlbum);
                 intent.putExtras(bundle);
                 startActivity(intent);
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     private void showMessageBox(String message) {
         AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
         dlgAlert.setMessage(message);
         dlgAlert.setTitle("Message");
         dlgAlert.setPositiveButton("OK", null);
         dlgAlert.setCancelable(true);
         dlgAlert.create().show();
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (requestCode == ACTION_TAKE_PIC) {
             if (resultCode == RESULT_OK) {
                 addPictureToGallery(currentPicture);
             }
         }
     }
 
     private void addPictureToGallery(Picture picture) {
         Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
         mediaScanIntent.setData(pictureRepository.getUri(picture));
         this.sendBroadcast(mediaScanIntent);
     }
 
     private void dispatchTakePictureIntent() {
         try {
             Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
             if (currentAlbum == null) {
                 showMessageBox("No Album.........");
                 return;
             }
             currentPicture = new Picture(currentAlbum);
             pictureRepository.save(currentPicture);
             takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, pictureRepository.getUri(currentPicture));
 
             startActivityForResult(takePictureIntent, ACTION_TAKE_PIC);
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     private void initializeImageList() {
         GridView gridview = (GridView) findViewById(R.id.gridview);
         File albumDirectory = albumRepository.getAlbumDirectory(currentAlbum);
         ImageListAdapter adapter = new ImageListAdapter(this, albumDirectory.getPath());
         adapter.notifyDataSetChanged();
         gridview.invalidateViews();
         gridview.setAdapter(adapter);
 
         gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                 //TODO view the image
                 showMessageBox("position:" + position);
             }
         });
     }
 }
