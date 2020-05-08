 package jp.gr.java_conf.neko_daisuki.httpsync;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
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
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.provider.BaseColumns;
 import android.util.Log;
 import android.util.SparseArray;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
 
     private class DeleteDialogOnClickListener implements DialogInterface.OnClickListener {
 
         private int mPosition;
 
         public DeleteDialogOnClickListener(int position) {
             mPosition = position;
         }
 
         public void onClick(DialogInterface _, int __) {
             deleteJob(mJobs[mPosition]);
         }
     }
 
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
             showConfirmDialogToDelete(mPosition);
         }
     }
 
     private class EditButtonOnClickListener extends ListButtonOnClickListener {
 
         public EditButtonOnClickListener(int position) {
             super(position);
         }
 
         public void onClick(View _) {
             startEditActivity(mJobs[mPosition], REQUEST_EDIT);
         }
     }
 
     private class JobAdapter extends ArrayAdapter<Job> {
 
         private class Holder {
 
             public TextView url;
             public TextView directory;
             public Button editButton;
             public Button deleteButton;
         }
 
         private LayoutInflater mInflater;
 
         public JobAdapter(Context context, Job[] objects) {
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
             Job job = getItem(position);
             holder.url.setText(job.url);
             holder.directory.setText(job.directory);
 
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
             holder.url = findTextView(convertView, R.id.url_text);
             holder.directory = findTextView(convertView, R.id.directory_text);
             holder.editButton = findButton(convertView, R.id.edit_button);
             holder.deleteButton = findButton(convertView, R.id.delete_button);
             convertView.setTag(holder);
             return convertView;
         }
     }
 
     private abstract class RequestProcedure {
 
         public abstract void run(Job job);
     }
 
     private class EditRequestProcedure extends RequestProcedure {
 
         public void run(Job job) {
             updateJob(job);
         }
     }
 
     private class AddRequestProcedure extends RequestProcedure {
 
         public void run(Job job) {
             addJob(job);
         }
     }
 
     private static class DatabaseHelper extends SQLiteOpenHelper {
 
         public interface Columns extends BaseColumns {
 
             public static final String URL = "url";
             public static final String DIRECTORY = "directory";
             public static final String OVERWRITE = "overwrite";
         }
 
         public static final String TABLE_NAME = "jobs";
 
         private static final String DATABASE_NAME = "jobs.db";
         private static final int DATABASE_VERSION = 1;
 
         public DatabaseHelper(Context ctx) {
             super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
         }
 
         @Override
         public void onCreate(SQLiteDatabase db) {
             String fmt = "create table %s (%s integer primary key autoincrement"
                 + ", %s text not null, %s text not null, %s integer not null);";
             String sql = String.format(
                     fmt,
                     TABLE_NAME, Columns._ID, Columns.URL, Columns.DIRECTORY,
                     Columns.OVERWRITE);
             db.execSQL(sql);
         }
 
         @Override
         public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
             db.execSQL(String.format("drop table if exists %s", TABLE_NAME));
             onCreate(db);
         }
     }
 
     private static class MalformedHtmlException extends Exception {
     }
 
     private class SynchronizeTask extends AsyncTask<Job[], Void, Void> {
 
         public Void doInBackground(Job[]... jobs) {
             Log.i(LOG_TAG, "Synchronizing started.");
             run(jobs[0]);
             Log.i(LOG_TAG, "Synchronizing ended.");
             return null;
         }
 
         private HttpURLConnection connect(String url) {
             URL loc;
             try {
                 loc = new URL(url);
             }
             catch (MalformedURLException e) {
                 String fmt = "Malformed URL: %s: %s";
                 Log.e(LOG_TAG, String.format(fmt, url, e.getMessage()));
                 return null;
             }
             HttpURLConnection conn;
             try {
                 conn = (HttpURLConnection)loc.openConnection();
                 conn.connect();
             }
             catch (IOException e) {
                 String fmt = "Cannot connect to %s: %s";
                 Log.e(LOG_TAG, String.format(fmt, url, e.getMessage()));
                 return null;
             }
 
             return conn;
         }
 
         private String readHtml(String url) {
             HttpURLConnection conn = connect(url);
             if (conn == null) {
                 return null;
             }
 
             InputStream in;
             try {
                 in = conn.getInputStream();
             }
             catch (IOException e) {
                 String fmt = "HttpURLConnection.getInputStream() failed: %s";
                Log.e(LOG_TAG, fmt.format(e.getMessage()));
                 return null;
             }
             InputStreamReader reader;
             String encoding = "UTF-8";
             try {
                 reader = new InputStreamReader(in, encoding);
             }
             catch (UnsupportedEncodingException e) {
                 String fmt = "Cannot handle encoding %s: %s";
                 Log.e(LOG_TAG, String.format(fmt, encoding, e.getMessage()));
                 return null;
             }
             String html = "";
             BufferedReader bufferedReader = new BufferedReader(reader);
             try {
                 try {
                     String line;
                     while ((line = bufferedReader.readLine()) != null) {
                         html += line;
                     }
                 }
                 finally {
                     bufferedReader.close();
                 }
             }
             catch (IOException e) {
                 String fmt = "Cannot read html: %s";
                 Log.e(LOG_TAG, String.format(fmt, e.getMessage()));
                 return null;
             }
 
             return html;
         }
 
         private List<String> extractLinks(String html) throws MalformedHtmlException {
             List<String> list = new ArrayList<String>();
 
             int end = 0;
             int begin;
             String startMark = "href=\"";
             while ((begin = html.indexOf(startMark, end)) != -1) {
                 int pos = begin + startMark.length();
                 end = html.indexOf("\"", pos);
                 if (end == -1) {
                     throw new MalformedHtmlException();
                 }
                 list.add(html.substring(pos, end));
 
                 end += 1;
             }
 
             return list;
         }
 
         private List<String> listOfLink(String link) {
             String[] extensions = new String[] {
                 ".mp3", ".mp4", ".apk", ".tar", ".xz", ".bz2", ".gzip", ".zip"
             };
             boolean isTarget = false;
             for (int i = 0; (i < extensions.length) && !isTarget; i++) {
                 isTarget = link.endsWith(extensions[i]);
             }
             return isTarget ? Arrays.asList(link) : new ArrayList<String>();
         }
 
         private String[] selectFiles(List<String> links) {
             List<String> list = new ArrayList<String>();
             for (String link: links) {
                 list.addAll(listOfLink(link));
             }
 
             return list.toArray(new String[0]);
         }
 
         private void download(String base, String link, String dir) {
             String url = String.format("%s/%s", base, link);
             HttpURLConnection conn = connect(url);
             if (conn == null) {
                 return;
             }
             String name = new File(link).getName();
             String path = String.format("%s%s%s", dir, File.separator, name);
             if (new File(path).exists()) {
                 String fmt = "Skip: source=%s, destination=%s";
                 Log.i(LOG_TAG, String.format(fmt, url, path));
                 return;
             }
 
             String fmt = "Downloading: source=%s, destination=%s";
             Log.i(LOG_TAG, String.format(fmt, url, path));
 
             try {
                 InputStream in = conn.getInputStream();
                 try {
                     FileOutputStream out = new FileOutputStream(path);
                     try {
                         byte[] buffer = new byte[4096];
                         int nBytes;
                         while ((nBytes = in.read(buffer)) != -1) {
                             out.write(buffer, 0, nBytes);
                         }
                     }
                     finally {
                         out.close();
                     }
                 }
                 finally {
                     in.close();
                 }
             }
             catch (IOException e) {
                 fmt = "Cannot copy. Destination file %s is removed: %s";
                 Log.e(LOG_TAG, String.format(fmt, path, e.getMessage()));
                 new File(path).delete();
             }
 
             fmt = "Downloaded: source=%s, destination=%s";
             Log.i(LOG_TAG, String.format(fmt, url, path));
         }
 
         private void synchronize(Job job) {
             // TODO: Must show the error to a user.
 
             String url = job.url;
             String html = readHtml(url);
             if (html == null) {
                 return;
             }
 
             String[] links;
             try {
                 links = selectFiles(extractLinks(html));
             }
             catch (MalformedHtmlException _) {
                 Log.e(LOG_TAG, "Html is malformed. Skip.");
                 return;
             }
 
             for (String link: links) {
                 download(url, link, job.directory);
             }
         }
 
         private void run(Job[] jobs) {
             for (Job job: jobs) {
                 synchronize(job);
             }
         }
     }
 
     private class AddButtonOnClickListener implements View.OnClickListener {
 
         public void onClick(View _) {
             startEditActivity(new Job(), REQUEST_ADD);
         }
     }
 
     private class RunButtonOnClickListener implements View.OnClickListener {
 
         public void onClick(View _) {
             new SynchronizeTask().execute(mJobs);
         }
     }
 
     private static final String LOG_TAG = "httpsync";
     private static final int REQUEST_ADD = 1;
     private static final int REQUEST_EDIT = 2;
 
     private Job[] mJobs;
     private ListView mJobList;
     private SparseArray<RequestProcedure> mRequestProcedures;
     private DatabaseHelper mDatabase;
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (resultCode != RESULT_OK) {
             return;
         }
         Job job = (Job)data.getSerializableExtra(EditActivity.EXTRA_KEY_JOB);
         mRequestProcedures.get(requestCode).run(job);
     }
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
         mJobList = (ListView)findViewById(R.id.job_list);
         Button addButton = (Button)findViewById(R.id.add_button);
         addButton.setOnClickListener(new AddButtonOnClickListener());
         Button runButton = (Button)findViewById(R.id.run_button);
         runButton.setOnClickListener(new RunButtonOnClickListener());
 
         mRequestProcedures = new SparseArray<RequestProcedure>();
         mRequestProcedures.put(REQUEST_ADD, new AddRequestProcedure());
         mRequestProcedures.put(REQUEST_EDIT, new EditRequestProcedure());
 
         mDatabase = new DatabaseHelper(this);
 
         updateJobs();
     }
 
     private void updateList() {
         ListAdapter adapter = new JobAdapter(this, mJobs);
         mJobList.setAdapter(adapter);
     }
 
     private void updateJobs() {
         mJobs = queryJobs();
         updateList();
     }
 
     private Job[] queryJobs() {
         List<Job> jobs = new ArrayList<Job>();
 
         try {
             SQLiteDatabase db = mDatabase.getReadableDatabase();
             String[] columns = new String[] {
                 DatabaseHelper.Columns._ID,
                 DatabaseHelper.Columns.URL,
                 DatabaseHelper.Columns.DIRECTORY,
                 DatabaseHelper.Columns.OVERWRITE };
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
                     Job job = new Job();
                     job.id = cursor.getLong(0);
                     job.url = cursor.getString(1);
                     job.directory = cursor.getString(2);
                     job.overwrite = cursor.getLong(3) == 1;
 
                     Log.i(LOG_TAG, job.toString());
 
                     jobs.add(job);
                 }
             }
             finally {
                 cursor.close();
             }
         }
         finally {
             mDatabase.close();
         }
 
         return jobs.toArray(new Job[0]);
     }
 
     private void startEditActivity(Job job, int requestCode) {
         Intent i = new Intent(this, EditActivity.class);
         i.putExtra(EditActivity.EXTRA_KEY_JOB, job);
         startActivityForResult(i, requestCode);
     }
 
     private ContentValues makeContentValuesOfJob(Job job) {
         ContentValues values = new ContentValues();
         values.put(DatabaseHelper.Columns.URL, job.url);
         values.put(DatabaseHelper.Columns.DIRECTORY, job.directory);
         values.put(DatabaseHelper.Columns.OVERWRITE, job.overwrite);
         return values;
     }
 
     private void addJob(Job job) {
         SQLiteDatabase db = mDatabase.getWritableDatabase();
         ContentValues values = makeContentValuesOfJob(job);
         db.insertOrThrow(DatabaseHelper.TABLE_NAME, null, values);
 
         updateJobs();
     }
 
     private void updateJob(Job job) {
         SQLiteDatabase db = mDatabase.getWritableDatabase();
         ContentValues values = makeContentValuesOfJob(job);
         String where = String.format("%s=?", DatabaseHelper.Columns._ID);
         String[] args = new String[] { Long.toString(job.id) };
         db.update(DatabaseHelper.TABLE_NAME, values, where, args);
 
         updateJobs();
     }
 
     private void deleteJob(Job job) {
         SQLiteDatabase db = mDatabase.getWritableDatabase();
         String where = String.format("%s=?", DatabaseHelper.Columns._ID);
         String[] args = new String[] { Long.toString(job.id) };
         db.delete(DatabaseHelper.TABLE_NAME, where, args);
 
         updateJobs();
     }
 
     private void showConfirmDialogToDelete(int position) {
         Job job = mJobs[position];
         String url = job.url;
         String directory = job.directory;
         Resources res = getResources();
         String fmt = res.getString(R.string.delete_confirm_format);
         String positive = res.getString(R.string.positive);
         String negative = res.getString(R.string.negative);
         String msg = String.format(fmt, url, directory, positive, negative);
 
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
