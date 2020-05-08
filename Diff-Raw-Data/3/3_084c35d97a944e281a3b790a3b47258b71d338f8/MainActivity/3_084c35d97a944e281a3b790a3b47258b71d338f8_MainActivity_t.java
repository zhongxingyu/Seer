 package jp.gr.java_conf.neko_daisuki.simplemediascanner;
 
 import java.io.File;
 import java.util.Arrays;
 import java.util.Comparator;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.FragmentActivity;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import jp.gr.java_conf.neko_daisuki.simplemediascanner.Database.Task;
 
 public class MainActivity extends FragmentActivity {
 
     public interface OnPositiveListener {
 
         public void onPositive(int id);
     }
 
     public static class ConfirmDialog extends DialogFragment {
 
         private class OnClickListener implements DialogInterface.OnClickListener {
 
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 mOnPositiveListener.onPositive(getArguments().getInt(KEY_ID));
             }
         }
 
         public static final String KEY_ID = "id";
         public static final String KEY_PATH = "path";
 
         private OnPositiveListener mOnPositiveListener;
 
         @Override
         public void onAttach(Activity activity) {
             super.onAttach(activity);
 
             MainActivity main = (MainActivity)activity;
             mOnPositiveListener = main.getPositiveListener();
         }
 
         @Override
         public Dialog onCreateDialog(Bundle savedInstanceState) {
             String path = getArguments().getString(KEY_PATH);
 
             Resources res = getResources();
             String fmt = res.getString(R.string.delete_confirm_format);
             String positive = res.getString(R.string.positive);
             String negative = res.getString(R.string.negative);
             String msg = String.format(fmt, path, positive, negative);
 
             Context context = getActivity();
             AlertDialog.Builder builder = new AlertDialog.Builder(context);
             builder.setTitle(R.string.delete_dialog_title);
             builder.setMessage(msg);
             builder.setPositiveButton(R.string.positive, new OnClickListener());
             builder.setNegativeButton(R.string.negative, null);
 
             return builder.create();
         }
     }
 
     private class ConfirmDialogOnPositiveListener implements OnPositiveListener {
 
         @Override
         public void onPositive(int id) {
             mDatabase.removeTask(id);
            Context context = MainActivity.this;
            Util.writeDatabase(context, mDatabase);
            PeriodicalUtil.schedule(context, mDatabase);
             mAdapter.notifyDataSetChanged();
         }
     }
 
     private class Adapter extends BaseAdapter {
 
         private class TaskComparator implements Comparator<Database.Task> {
 
             @Override
             public int compare(Database.Task lhs, Database.Task rhs) {
                 return lhs.getPath().compareTo(rhs.getPath());
             }
         }
 
         private abstract class OnClickListener implements View.OnClickListener {
 
             private Database.Task mTask;
 
             public OnClickListener(Database.Task task) {
                 mTask = task;
             }
 
             protected Database.Task getTask() {
                 return mTask;
             }
         }
 
         private class RunButtonOnClickListener extends OnClickListener {
 
             public RunButtonOnClickListener(Task task) {
                 super(task);
             }
 
             @Override
             public void onClick(View v) {
                 int[] ids = new int[] { getTask().getId() };
                 ServiceUtil.startMainService(MainActivity.this, ids);
             }
         }
 
         private class EditButtonOnClickListener extends OnClickListener {
 
             public EditButtonOnClickListener(Task task) {
                 super(task);
             }
 
             @Override
             public void onClick(View v) {
                 Intent i = new Intent(MainActivity.this, EditActivity.class);
                 i.putExtra(EditActivity.EXTRA_ID, getTask().getId());
                 startActivity(i);
             }
         }
 
         private class DeleteButtonOnClickListener extends OnClickListener {
 
             public DeleteButtonOnClickListener(Database.Task task) {
                 super(task);
             }
 
             @Override
             public void onClick(View v) {
                 DialogFragment fragment = new ConfirmDialog();
                 Bundle args = new Bundle();
                 Database.Task task = getTask();
                 args.putInt(ConfirmDialog.KEY_ID, task.getId());
                 args.putString(ConfirmDialog.KEY_PATH, task.getPath());
                 fragment.setArguments(args);
                 fragment.show(getSupportFragmentManager(), "Delete the task");
             }
         }
 
         // documents
         private Database.Task[] mTasks = new Database.Task[0];
 
         // helpers
         private Comparator<Database.Task> mComparator = new TaskComparator();
         private LayoutInflater mInflater;
 
         public Adapter() {
             String name = Context.LAYOUT_INFLATER_SERVICE;
             mInflater = (LayoutInflater)getSystemService(name);
         }
 
         @Override
         public void notifyDataSetChanged() {
             mTasks = mDatabase.getTasks();
             Arrays.sort(mTasks, mComparator);
             super.notifyDataSetChanged();
         }
 
         @Override
         public int getCount() {
             return mTasks.length;
         }
 
         @Override
         public Object getItem(int position) {
             return mTasks[position];
         }
 
         @Override
         public long getItemId(int position) {
             return mTasks[position].getId();
         }
 
         @Override
         public View getView(int position, View convertView, ViewGroup parent) {
             if (convertView == null) {
                 return getView(position, makeView(parent), parent);
             }
 
             Database.Task task = mTasks[position];
             int id = R.id.directory_text;
             TextView text = (TextView)convertView.findViewById(id);
             text.setText(task.getPath());
 
             View deleteButton = convertView.findViewById(R.id.delete_button);
             deleteButton.setOnClickListener(new DeleteButtonOnClickListener(task));
             View editButton = convertView.findViewById(R.id.edit_button);
             editButton.setOnClickListener(new EditButtonOnClickListener(task));
             View runButton = convertView.findViewById(R.id.run_button);
             runButton.setOnClickListener(new RunButtonOnClickListener(task));
 
             return convertView;
         }
 
         private View makeView(ViewGroup parent) {
             return mInflater.inflate(R.layout.list_row, parent, false);
         }
     }
 
     private class AddButtonOnClickListener implements View.OnClickListener {
 
         @Override
         public void onClick(View v) {
             startActivity(new Intent(MainActivity.this, EditActivity.class));
         }
     }
 
     private class RunAllButtonOnClickListener implements View.OnClickListener {
 
         public void onClick(View _) {
             Database.Task[] tasks = mDatabase.getTasks();
             int length = tasks.length;
             int[] ids = new int[length];
             for (int i = 0; i < length; i++) {
                 ids[i] = tasks[i].getId();
             }
             ServiceUtil.startMainService(MainActivity.this, ids);
         }
     }
 
     private static final String LOG_TAG = "activity";
 
     // documents
     private Database mDatabase;
 
     // views
     private Adapter mAdapter;
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
 
     public boolean onOptionsItemSelected(MenuItem item) {
         Intent i = new Intent(this, AboutActivity.class);
         startActivity(i);
         return true;
     }
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
         mAdapter = new Adapter();
         ListView list = (ListView)findViewById(R.id.directory_list);
         list.setAdapter(mAdapter);
 
         View runAllButton = findViewById(R.id.run_all_button);
         runAllButton.setOnClickListener(new RunAllButtonOnClickListener());
         View addButton = findViewById(R.id.add_button);
         addButton.setOnClickListener(new AddButtonOnClickListener());
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         Util.writeDatabase(this, mDatabase);
     }
 
     @Override
     protected void onResume() {
         super.onResume();
 
         initializeApplicationDirectory();
         mDatabase = Util.readDatabase(this);
         mAdapter.notifyDataSetChanged();
     }
 
     private void showError(String msg) {
         Log.e(LOG_TAG, msg);
         Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
     }
 
     private boolean makeDirectory(File directory) {
         if (!directory.isDirectory() && !directory.mkdir()) {
             String fmt = "Cannot create directory: %s";
             showError(String.format(fmt, directory.getAbsolutePath()));
             return false;
         }
         return true;
     }
 
     private void initializeApplicationDirectory() {
         File directory = Util.getApplicationDirectory();
         boolean initialized = directory.exists();
         if (!makeDirectory(directory)) {
             return;
         }
         File logDirectory = Util.getLogDirectory();
         if (!makeDirectory(logDirectory)) {
             return;
         }
         if (!initialized) {
             OldDatabase.importOldDatabase(this);
         }
     }
 
     private OnPositiveListener getPositiveListener() {
         return new ConfirmDialogOnPositiveListener();
     }
 }
