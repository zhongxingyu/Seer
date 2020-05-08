 package jp.gr.java_conf.neko_daisuki.simplemediascanner;
 
 import java.io.File;
 import java.util.ArrayDeque;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Queue;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.media.MediaScannerConnection.MediaScannerConnectionClient;
 import android.media.MediaScannerConnection;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.BaseColumns;
 import android.util.Log;
 import android.util.SparseArray;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
 
     private class DeleteDialogOnClickListener implements DialogInterface.OnClickListener {
 
         private int mPosition;
 
         public DeleteDialogOnClickListener(int position) {
             mPosition = position;
         }
 
         public void onClick(DialogInterface _, int __) {
             Directory directory = mDirectories[mPosition];
             SQLiteDatabase db = mDatabase.getWritableDatabase();
             String where = String.format("%s=?", DatabaseHelper.Columns._ID);
             String[] args = new String[] { Long.toString(directory.id) };
             db.delete(DatabaseHelper.TABLE_NAME, where, args);
 
             updateDirectories();
         }
     }
 
     private class DirectoryAdapter extends ArrayAdapter<Directory> {
 
         private abstract class ListButtonOnClickListener implements View.OnClickListener {
 
             protected int mPosition;
 
             public ListButtonOnClickListener(int position) {
                 mPosition = position;
             }
 
             public abstract void onClick(View _);
         }
 
         private class DeleteButtonOnClickListener extends ListButtonOnClickListener {
 
             public DeleteButtonOnClickListener(int position) {
                 super(position);
             }
 
             public void onClick(View _) {
                 showConfirmDialog(mPosition);
             }
         }
 
         private class RunButtonOnClickListener extends ListButtonOnClickListener {
 
             public RunButtonOnClickListener(int position) {
                 super(position);
             }
 
             public void onClick(View _) {
                 Directory directory = mDirectories[mPosition];
 
                 String fmt = "Started scanning for %s.";
                 Log.i(LOG_TAG, String.format(fmt, directory.path));
                 mClient.pushDirectories(new Directory[] { directory });
                 mConnection.connect();
             }
         }
 
         private class EditButtonOnClickListener extends ListButtonOnClickListener {
 
             public EditButtonOnClickListener(int position) {
                 super(position);
             }
 
             public void onClick(View _) {
                 Directory directory = mDirectories[mPosition];
                 startEditActivity(directory, RequestCode.EDIT);
             }
         }
 
         private class Holder {
 
             public TextView path;
             public Button runButton;
             public Button editButton;
             public Button deleteButton;
         }
 
         private LayoutInflater mInflater;
 
         public DirectoryAdapter(Context context, Directory[] objects) {
             super(context, 0, objects);
 
             String name = Context.LAYOUT_INFLATER_SERVICE;
             mInflater = (LayoutInflater)context.getSystemService(name);
         }
 
         @Override
         public View getView(int position, View convertView, ViewGroup parent) {
             if (convertView == null) {
                 return getView(position, makeView(parent), parent);
             }
             Holder holder = (Holder)convertView.getTag();
             Directory directory = getItem(position);
             holder.path.setText(directory.path);
 
             holder.runButton.setOnClickListener(new RunButtonOnClickListener(position));
             holder.editButton.setOnClickListener(new EditButtonOnClickListener(position));
             holder.deleteButton.setOnClickListener(new DeleteButtonOnClickListener(position));
 
             return convertView;
         }
 
         private TextView findTextView(View view, int id) {
             return (TextView)view.findViewById(id);
         }
 
         private Button findButton(View view, int id) {
             return (Button)view.findViewById(id);
         }
 
         private View makeView(ViewGroup parent) {
             int layout = R.layout.list_row;
             View convertView = mInflater.inflate(layout, parent, false);
             Holder holder = new Holder();
             holder.path = findTextView(convertView, R.id.directory_text);
             holder.runButton = findButton(convertView, R.id.run_button);
             holder.editButton = findButton(convertView, R.id.edit_button);
             holder.deleteButton = findButton(convertView, R.id.delete_button);
             convertView.setTag(holder);
             return convertView;
         }
     }
 
     private interface RequestCode {
 
         public int ADD = 1;
         public int EDIT = 2;
     }
 
     private static class DatabaseHelper extends SQLiteOpenHelper {
 
         public interface Columns extends BaseColumns {
 
             public static final String PATH = "path";
         }
 
         public static final String TABLE_NAME = "directories";
 
         private static final String DATABASE_NAME = "directories.db";
         private static final int DATABASE_VERSION = 1;
 
         public DatabaseHelper(Context ctx) {
             super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
         }
 
         @Override
         public void onCreate(SQLiteDatabase db) {
             String fmt = "create table %s (%s integer primary key autoincrement"
                 + ", %s text not null);";
             String sql = String.format(
                     fmt,
                     TABLE_NAME,
                     Columns._ID, Columns.PATH);
             db.execSQL(sql);
         }
 
         @Override
         public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
             db.execSQL(String.format("drop table if exists %s", TABLE_NAME));
             onCreate(db);
         }
     }
 
     private class MediaScannerClient implements MediaScannerConnectionClient {
 
         private Queue<File> mFiles = new LinkedList<File>();
 
         public void onMediaScannerConnected() {
             Log.i(LOG_TAG, "Connected to the service.");
             scanNext();
         }
 
         public void onScanCompleted(String path, Uri uri) {
             Log.i(LOG_TAG, String.format("Scanning %s was completed.", path));
             scanNext();
         }
 
         public void pushDirectories(Directory[] directories) {
             int len = directories.length;
             File[] files = new File[len];
             for (int i = 0; i < len; i++) {
                 files[i] = new File(directories[i].path);
             }
             pushFiles(files);
         }
 
         private void pushFiles(File[] files) {
             for (File file: files) {
                 String fmt = "Pushed %s.";
                 Log.i(LOG_TAG, String.format(fmt, file.getAbsolutePath()));
 
                 mFiles.offer(file);
             }
         }
 
         private void scanNext() {
             File file;
             while (((file = mFiles.poll()) != null) && file.isDirectory()) {
                 String fmt = "Directory found: %s";
                 Log.i(LOG_TAG, String.format(fmt, file.getAbsolutePath()));
 
                 pushFiles(file.listFiles());
             }
             if (file == null) {
                 mConnection.disconnect();
                 Log.i(LOG_TAG, "Scanning ended.");
                 return;
             }
             String path = file.getAbsolutePath();
             Log.i(LOG_TAG, String.format("File found: %s", path));
             mConnection.scanFile(path, null);
         }
     }
 
     private class AddButtonOnClickListener implements View.OnClickListener {
 
         public void onClick(View _) {
             startEditActivity(new Directory(), RequestCode.ADD);
         }
     }
 
     private class RunAllButtonOnClickListener implements View.OnClickListener {
 
         public void onClick(View _) {
             Log.i(LOG_TAG, "Started scanning all directories.");
             mClient.pushDirectories(mDirectories);
             mConnection.connect();
         }
     }
 
     private abstract class RequestProcedure {
 
         public abstract void run(Directory directory);
     }
 
     private class EditRequestProcedure extends RequestProcedure {
 
         public void run(Directory directory) {
             SQLiteDatabase db = mDatabase.getWritableDatabase();
             ContentValues values = makeContentValues(directory);
             String where = String.format("%s=?", DatabaseHelper.Columns._ID);
             String[] args = new String[] { Long.toString(directory.id) };
             db.update(DatabaseHelper.TABLE_NAME, values, where, args);
 
             updateDirectories();
         }
     }
 
     private class AddRequestProcedure extends RequestProcedure {
 
         public void run(Directory directory) {
             SQLiteDatabase db = mDatabase.getWritableDatabase();
             ContentValues values = makeContentValues(directory);
             db.insertOrThrow(DatabaseHelper.TABLE_NAME, null, values);
 
             updateDirectories();
         }
     }
 
     private static final String LOG_TAG = "simplemediascanner";
 
     private Directory[] mDirectories;
 
     private ListView mDirectoryList;
 
     private MediaScannerClient mClient;
     private MediaScannerConnection mConnection;
     private SQLiteOpenHelper mDatabase;
     private SparseArray<RequestProcedure> mRequestProcedures;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
         mDirectoryList = (ListView)findViewById(R.id.directory_list);
         Button runAllButton = (Button)findViewById(R.id.run_all_button);
         runAllButton.setOnClickListener(new RunAllButtonOnClickListener());
         Button addButton = (Button)findViewById(R.id.add_button);
         addButton.setOnClickListener(new AddButtonOnClickListener());
 
         mClient = new MediaScannerClient();
         mConnection = new MediaScannerConnection(this, mClient);
         mDatabase = new DatabaseHelper(this);
         mRequestProcedures = new SparseArray<RequestProcedure>();
         mRequestProcedures.put(RequestCode.ADD, new AddRequestProcedure());
         mRequestProcedures.put(RequestCode.EDIT, new EditRequestProcedure());
 
         updateDirectories();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
 
     public boolean onOptionsItemSelected(MenuItem item) {
         Intent i = new Intent(this, AboutActivity.class);
         this.startActivity(i);
         return true;
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (resultCode != RESULT_OK) {
             return;
         }
         String key = EditActivity.EXTRA_KEY_DIRECTORY;
         Directory directory = (Directory)data.getSerializableExtra(key);
         mRequestProcedures.get(requestCode).run(directory);
     }
 
     private Directory[] queryDirectories() {
         List<Directory> directories = new ArrayList<Directory>();
 
         try {
             SQLiteDatabase db = mDatabase.getReadableDatabase();
             String[] columns = new String[] {
                 DatabaseHelper.Columns._ID,
                 DatabaseHelper.Columns.PATH };
             Cursor cursor = db.query(
                     DatabaseHelper.TABLE_NAME,
                     columns,
                     null,   // selection
                     null,   // selection args
                     null,   // group by
                     null,   // having
                     null);  // order by
             try {
                 while (cursor.moveToNext()) {
                     Directory directory = new Directory();
                     directory.id = cursor.getLong(0);
                     directory.path = cursor.getString(1);
                     directories.add(directory);
                 }
             }
             finally {
                 cursor.close();
             }
         }
         finally {
             mDatabase.close();
         }
 
         return directories.toArray(new Directory[0]);
     }
 
     private void startEditActivity(Directory directory, int requestCode) {
         Intent i = new Intent(this, EditActivity.class);
         i.putExtra(EditActivity.EXTRA_KEY_DIRECTORY, directory);
         startActivityForResult(i, requestCode);
     }
 
     private ContentValues makeContentValues(Directory directory) {
         ContentValues values = new ContentValues();
         values.put(DatabaseHelper.Columns.PATH, directory.path);
         return values;
     }
 
     private void updateList() {
         mDirectoryList.setAdapter(new DirectoryAdapter(this, mDirectories));
     }
 
     private void updateDirectories() {
         mDirectories = queryDirectories();
         updateList();
     }
 
     private void showConfirmDialog(int position) {
         Directory directory = mDirectories[position];
         Resources res = getResources();
         String fmt = res.getString(R.string.delete_confirm_format);
         String positive = res.getString(R.string.positive);
         String negative = res.getString(R.string.negative);
         String msg = String.format(fmt, directory.path, positive, negative);
 
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setTitle(R.string.delete_dialog_title);
         builder.setMessage(msg);
         builder.setPositiveButton(R.string.positive, new DeleteDialogOnClickListener(position));
         builder.setNegativeButton(R.string.negative, null);
 
         builder.create().show();
     }
 }
 
 /**
  * vim: tabstop=4 shiftwidth=4 expandtab softtabstop=4
  */
