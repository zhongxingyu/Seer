 package jp.gr.java_conf.neko_daisuki.simplemediascanner;
 
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.Locale;
 
 import android.content.Context;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.FragmentActivity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AbsListView;
 import android.widget.BaseAdapter;
 import android.widget.CompoundButton;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class EditActivity extends FragmentActivity implements ScheduleFragment.OnScheduleGivenListener {
 
     private class Adapter extends BaseAdapter {
 
         private class ScheduleComparator implements Comparator<Database.Schedule> {
 
             @Override
             public int compare(Database.Schedule lhs, Database.Schedule rhs) {
                 if (lhs.isDaily() && !rhs.isDaily()) {
                     return 1;
                 }
                 if (!lhs.isDaily() && rhs.isDaily()) {
                     return -1;
                 }
                 int n = lhs.getHour() - rhs.getHour();
                 if (n != 0) {
                     return n;
                 }
                 return lhs.getMinute() - rhs.getMinute();
             }
         }
 
         private class CheckBoxListener implements CompoundButton.OnCheckedChangeListener {
 
             private abstract class Proc {
 
                 public abstract void run();
             }
 
             private class CheckedProc extends Proc {
 
                 @Override
                 public void run() {
                     mDatabase.addScheduleToTask(mId, mScheduleId);
                 }
             }
 
             private class UncheckedProc extends Proc {
 
                 @Override
                 public void run() {
                     mDatabase.removeScheduleFromTask(mId, mScheduleId);
                 }
             }
 
             private int mScheduleId;
             private Proc mCheckedProc = new CheckedProc();
             private Proc mUncheckedProc = new UncheckedProc();
 
             public CheckBoxListener(int scheduleId) {
                 mScheduleId = scheduleId;
             }
 
             @Override
             public void onCheckedChanged(CompoundButton buttonView,
                                          boolean isChecked) {
                 (isChecked ? mCheckedProc : mUncheckedProc).run();
             }
         }
 
         // documents
         private Database.Schedule[] mSchedules = new Database.Schedule[0];
 
         // helpers
         private LayoutInflater mInflater;
         private Comparator<Database.Schedule> mComparator = new ScheduleComparator();
 
         public Adapter() {
             String name = Context.LAYOUT_INFLATER_SERVICE;
             mInflater = (LayoutInflater)getSystemService(name);
         }
 
         @Override
         public void notifyDataSetChanged() {
             mSchedules = mDatabase.getSchedules();
             Arrays.sort(mSchedules, mComparator);
             super.notifyDataSetChanged();
         }
 
         @Override
         public int getCount() {
             return mSchedules.length;
         }
 
         @Override
         public Object getItem(int position) {
             return mSchedules[position];
         }
 
         @Override
         public long getItemId(int position) {
             return mSchedules[position].getId();
         }
 
         @Override
         public View getView(int position, View convertView, ViewGroup parent) {
             if (convertView == null) {
                 return getView(position, makeView(), parent);
             }
             initializeView(position, convertView);
             return convertView;
         }
 
         private boolean contains(int[] a, int key) {
             int length = a.length;
             for (int i = 0; i < length; i++) {
                 if (a[i] == key) {
                     return true;
                 }
             }
             return false;
         }
 
         private void initializeView(int position, View view) {
             Database.Schedule schedule = mSchedules[position];
             int scheduleId = schedule.getId();
 
             CompoundButton checkBox = (CompoundButton)view.findViewById(R.id.checkbox);
             int[] ids = mDatabase.getScheduleIdsOfTask(mId);
            checkBox.setChecked(contains(ids, scheduleId));
             checkBox.setOnCheckedChangeListener(new CheckBoxListener(scheduleId));
 
             boolean isDaily = schedule.isDaily();
             String hour = isDaily ? String.format(Locale.ROOT, "%02d", schedule.getHour())
                                   : " *";
             int minute = schedule.getMinute();
             TextView scheduleText = (TextView)view.findViewById(R.id.schedule_text);
             scheduleText.setText(String.format("%s:%02d", hour, minute));
         }
 
         private View makeView() {
             return mInflater.inflate(R.layout.row_schedule, null);
         }
     }
 
     private class AddScheduleButtonOnClickListener implements View.OnClickListener {
 
         @Override
         public void onClick(View v) {
             DialogFragment fragment = new ScheduleFragment();
             fragment.show(getSupportFragmentManager(), "new schedule");
         }
     }
 
     private class OkayButtonOnClickListener implements View.OnClickListener {
 
         public void onClick(View _) {
             mDatabase.editTask(mId, mDirectoryEditText.getText().toString());
             Util.writeDatabase(EditActivity.this, mDatabase);
             finish();
         }
     }
 
     private class CancelButtonOnClickListener implements View.OnClickListener {
 
         public void onClick(View _) {
             finish();
         }
     }
 
     public static final String EXTRA_ID = "id";
 
     // documents
     private Database mDatabase;
     private int mId;
 
     // views
     private EditText mDirectoryEditText;
     private Adapter mAdapter;
 
     @Override
     public void onScheduleGiven(ScheduleFragment fragment, int hour,
                                 int minute) {
         int scheduleId = mDatabase.addSchedule(hour, minute);
         mDatabase.addScheduleToTask(mId, scheduleId);
         mAdapter.notifyDataSetChanged();
     }
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_edit);
 
         mDatabase = Util.readDatabase(this);
         int taskId = getIntent().getIntExtra(EXTRA_ID, -1);
         mId = taskId != -1 ? taskId : mDatabase.addTask("");
 
         mDirectoryEditText = (EditText)findViewById(R.id.directory_text);
         mDirectoryEditText.setText(mDatabase.getTask(mId).getPath());
 
         View addScheduleButton = findViewById(R.id.add_schedule_button);
         addScheduleButton.setOnClickListener(new AddScheduleButtonOnClickListener());
         View okButton = findViewById(R.id.ok_button);
         okButton.setOnClickListener(new OkayButtonOnClickListener());
         View cancelButton = findViewById(R.id.cancel_button);
         cancelButton.setOnClickListener(new CancelButtonOnClickListener());
 
         mAdapter = new Adapter();
         int list_id = R.id.schedule_list;
         AbsListView scheduleList = (AbsListView)findViewById(list_id);
         scheduleList.setAdapter(mAdapter);
         mAdapter.notifyDataSetChanged();
     }
 }
