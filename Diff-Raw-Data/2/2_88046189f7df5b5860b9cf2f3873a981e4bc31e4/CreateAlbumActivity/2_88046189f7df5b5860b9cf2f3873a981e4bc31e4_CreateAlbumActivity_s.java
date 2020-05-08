 package ch.bergturbenthal.image.client.createalbum;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.concurrent.atomic.AtomicReference;
 
 import org.joda.time.DateMidnight;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.DatePicker;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 import ch.bergturbenthal.image.client.R;
 import ch.bergturbenthal.image.client.resolver.AlbumService;
 import ch.bergturbenthal.image.client.resolver.ConnectedHandler;
 import ch.bergturbenthal.image.client.resolver.ConnectionAdapter;
 import ch.bergturbenthal.image.client.resolver.Resolver;
 import ch.bergturbenthal.image.data.model.AlbumEntry;
 
 public class CreateAlbumActivity extends Activity {
   private AlbumService service;
 
   public void createAlbum(final View view) {
     final Spinner spinner = (Spinner) findViewById(R.id.selectFolderSpinner);
     final String parentFolder = (String) spinner.getSelectedItem();
     if (parentFolder == null) {
       Toast.makeText(this, R.string.create_folder_error_no_folder, Toast.LENGTH_LONG).show();
       return;
     }
 
     final TextView albumNameInput = (TextView) findViewById(R.id.album_name);
     final CharSequence albumName = albumNameInput.getText();
     if (albumName == null || albumName.length() == 0) {
       Toast.makeText(this, R.string.create_folder_error_no_abum_name, Toast.LENGTH_LONG).show();
       return;
     }
     final DatePicker datePicker = (DatePicker) findViewById(R.id.selectDate);
    final DateMidnight selectedDate = new DateMidnight(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
     final ProgressDialog progressDialog = ProgressDialog.show(this, "", getResources().getString(R.string.wait_for_server_message), true);
     new AsyncTask<Void, Void, Void>() {
       @Override
       protected Void doInBackground(final Void... params) {
         final ArrayList<String> pathComps = new ArrayList<String>();
         for (final String comp : (parentFolder + "/" + albumName).split("/")) {
           final String trimmedComp = comp.trim();
           if (trimmedComp.length() > 0) {
             pathComps.add(trimmedComp);
           }
         }
         final String createdAlbum = service.createAlbum(pathComps.toArray(new String[pathComps.size()]));
         service.setAutoAddDate(createdAlbum, selectedDate.toDate());
         return null;
       }
 
       @Override
       protected void onPostExecute(final Void result) {
         progressDialog.dismiss();
         finish();
       }
 
     }.execute();
   }
 
   @Override
   protected void onCreate(final Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     setContentView(R.layout.create_album);
   }
 
   @Override
   protected void onResume() {
     super.onResume();
     new Resolver(this).establishLastConnection(new ConnectionAdapter(this, new ConnectedHandler() {
 
       @Override
       public void connected(final AlbumService service, final String serverName) {
         CreateAlbumActivity.this.service = service;
         final AtomicReference<ProgressDialog> dialogRef = new AtomicReference<ProgressDialog>();
         runOnUiThread(new Runnable() {
 
           @Override
           public void run() {
             dialogRef.set(ProgressDialog.show(CreateAlbumActivity.this, "CreateAlbumActivity.onResume",
                                               CreateAlbumActivity.this.getResources().getString(R.string.wait_for_server_message), true));
           }
         });
         final Set<String> existingParents = new TreeSet<String>();
         existingParents.add("");
         final Collection<AlbumEntry> albumNames = service.listAlbums().getAlbumNames();
         for (final AlbumEntry album : albumNames) {
           final String[] albumComps = album.getName().split("/");
           final StringBuffer path = new StringBuffer();
           for (int i = 0; i < albumComps.length - 1; i++) {
             if (path.length() > 0)
               path.append('/');
             path.append(albumComps[i]);
             existingParents.add(path.toString());
           }
         }
         runOnUiThread(new Runnable() {
           @Override
           public void run() {
             final Spinner spinner = (Spinner) findViewById(R.id.selectFolderSpinner);
             spinner.setAdapter(new ArrayAdapter<String>(CreateAlbumActivity.this, android.R.layout.simple_spinner_dropdown_item,
                                                         new ArrayList<String>(existingParents)));
             final DatePicker datePicker = (DatePicker) findViewById(R.id.selectDate);
 
             dialogRef.get().hide();
           }
         });
       }
     }));
   }
 
 }
