 package net.taviscaron.bsuirschedule.activity;
 
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 
 import net.taviscaron.bsuirschedule.R;
 import net.taviscaron.bsuirschedule.adapter.LessonsListAdapter;
 import net.taviscaron.bsuirschedule.core.Constants;
 import net.taviscaron.bsuirschedule.core.DateUtil;
 import net.taviscaron.bsuirschedule.model.Lesson;
 import net.taviscaron.bsuirschedule.model.LessonsListModel;
 import net.taviscaron.bsuirschedule.model.Schedule;
 import net.taviscaron.bsuirschedule.storage.DBHelper;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.support.v4.view.PagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.text.TextUtils;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class LessonsListActivity extends Activity {
     public static final String GROUP_NAME_EXTRA = "groupName";
     public static final String CURRENT_GROUP_BUNDLE_KEY = "currentGroup";
     
     private String currentGroup;
     private List<LessonsListModel> models;
     private DBHelper dbHelper = new DBHelper(this);
     private AsyncTask<Void, Void, Void> lessonsLoadTask;
     
     private PagerAdapter pageAdapter = new PagerAdapter() {
         @Override
         public Object instantiateItem(ViewGroup container, int position) {
             LessonsListModel model = models.get(position);
             
             View view = getLayoutInflater().inflate(R.layout.lessons_list, null);
             
             TextView titleView = (TextView) view.findViewById(R.id.lesson_title);
             titleView.setText(model.getTitle());
             
             LessonsListAdapter listAdapter = new LessonsListAdapter(LessonsListActivity.this, model.getCursor());
             ListView lessonsList = (ListView) view.findViewById(R.id.lessons_list);
             lessonsList.setAdapter(listAdapter);
             
             if (position == 0) {
                 TextView prevPage = (TextView) view.findViewById(R.id.lesson_pref_page);
                 prevPage.setVisibility(View.GONE);
             } else if (position == models.size() - 1) {
                 TextView nextPage = (TextView) view.findViewById(R.id.lesson_next_page);
                 nextPage.setVisibility(View.GONE);
             }
             
             container.addView(view);
             return view;
         }
         
         @Override
         public int getCount() {
             return (models != null) ? models.size() : 0;
         }
         
         @Override
         public void destroyItem(ViewGroup container, int position, Object object) {
             container.removeView((View) object);
         }
         
         @Override
         public boolean isViewFromObject(View container, Object id) {
             return (container == id);
         }
         
         @Override
         public int getItemPosition(Object object) {
             return POSITION_NONE;
         }
     };
     
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.schedule_screen);
         
         // set state
         Intent intent = getIntent();
         if (intent != null) {
             currentGroup = intent.getStringExtra(GROUP_NAME_EXTRA);
         } else if (savedInstanceState != null) {
             currentGroup = savedInstanceState.getString(CURRENT_GROUP_BUNDLE_KEY);
         }
         
         ViewPager viewPager = (ViewPager) findViewById(R.id.lessons_lists_view_pager);
         viewPager.setAdapter(pageAdapter);
     }
     
     @Override
     protected void onNewIntent(Intent intent) {
         String newGroup = intent.getStringExtra(GROUP_NAME_EXTRA);
         if (newGroup != null && !newGroup.equals(currentGroup)) {
             setIntent(intent);
             currentGroup = newGroup;
         }
     }
     
     @Override
     protected void onResume() {
         super.onResume();
         refreshLists();
         updateLessonsHeaderLaber();
     }
     
     @Override
     protected void onStop() {
         super.onStop();
         
         if (lessonsLoadTask != null) {
             try {
                 lessonsLoadTask.cancel(true);
             } catch (NullPointerException e) {
                 // ignore
             }
         }
     }
     
     @Override
     protected void onDestroy() {
         super.onDestroy();
         dbHelper.close();
     }
     
     @Override
     public void onBackPressed() {
         if(lessonsLoadTask == null) {
             super.onBackPressed();
         }
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.schedule_menu, menu);
         return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         Intent intent = null;
         switch (item.getItemId()) {
             case R.id.choose_schedule_menu_item:
                 intent = new Intent(this, ManageSchedulesActivity.class);
                 startActivity(intent);
                 break;
             case R.id.settings_menu_item:
                 intent = new Intent(this, SettingsActivity.class);
                 startActivity(intent);
                 break;
             default:
                 return super.onOptionsItemSelected(item);
         }
         return true;
     }
     
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         outState.putString(CURRENT_GROUP_BUNDLE_KEY, currentGroup);
     }
     
     private void updateLessonsHeaderLaber() {
         Date date = new Date();
         int dayOfWeekIndex = DateUtil.weekDayOfDate(date);
         String dayOfWeek = getResources().getStringArray(R.array.week_days)[dayOfWeekIndex];
         String todayDate = DateUtil.simpleDateFormat(date);
         int workWeek = DateUtil.workWeekFromDate(date);
         
         String result = getString(R.string.lessons_list_header_today_label, todayDate, dayOfWeek, workWeek);
         
         TextView label = (TextView) findViewById(R.id.lessons_list_header_label);
         label.setText(result);
         
         String currentGroupLabel = getString(R.string.lessons_list_header_group_label, currentGroup);
         TextView currentGroupTextView = (TextView) findViewById(R.id.lessons_group);
         currentGroupTextView.setText(currentGroupLabel);
     }
     
     private void refreshLists() {
         new AsyncTask<Void, Void, Void>() {
             private List<LessonsListModel> loadedModels = new LinkedList<LessonsListModel>();
             private String[] regularDaysLabels = getResources().getStringArray(R.array.week_days);
             private SQLiteDatabase db = dbHelper.getReadableDatabase();
             
             protected void onPreExecute() {
                 lessonsLoadTask = this;
             }
             
             @Override
             protected Void doInBackground(Void... params) {
                 try {
                     String schedId = loadSchedId();
                     
                     addDay(schedId, getString(R.string.week_day_today), DateUtil.today());
                     addDay(schedId, getString(R.string.week_day_tomorrow), DateUtil.tomorrow());
                     
                     addWeekDay(schedId, 0);
                     addWeekDay(schedId, 1);
                     addWeekDay(schedId, 2);
                     addWeekDay(schedId, 3);
                     addWeekDay(schedId, 4);
                     addWeekDay(schedId, 5);
                     addWeekDay(schedId, 6);
                 } catch (Exception e) {
                     // ignore all
                 }
                 return null;
             }
             
             private String loadSchedId() {
                Cursor cursor = db.query(Schedule.TABLE_NAME, null, String.format("%s = '%s'", Schedule.GROUP_ATTR, currentGroup), null, null, null, null);
                 cursor.moveToFirst();
                 String schedId = cursor.getString(0);
                 cursor.close();
                 return schedId;
             }
             
             private void addDay(String schedId, String name, Date date) {
                 int day = DateUtil.weekDayOfDate(date);
                 int week = DateUtil.workWeekFromDate(date);
                 String selection = String.format("%s = %d AND %s & (1 << %d) > 0", Lesson.DAY_ATTR, day, Lesson.WEEKS_ATTR, week);
                 String label = String.format("%s (%s)", name, regularDaysLabels[day]);
                 addModel(schedId, label, selection);
             }
             
             private void addWeekDay(String schedId, int index) {
                 addModel(schedId, regularDaysLabels[index], String.format("%s = %d", Lesson.DAY_ATTR, index));
             }
             
             private void addModel(String schedId, String title, String selection) {
                 StringBuilder whereCause = new StringBuilder(selection);
                 
                 // filter by group
                 whereCause.append(String.format(" and %s = %s", Lesson.SCHEDULE_ATTR, schedId));
                 
                 // filter by subgroup
                 SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(LessonsListActivity.this);
                 String currentSubgroup = sp.getString(Constants.CURRENT_SUBGROUP_PREF_KEY, null);
                 if (!TextUtils.isEmpty(currentSubgroup)) {
                     int subgroup = Integer.parseInt(currentSubgroup);
                     whereCause.append(String.format(" and %s & (1 << %d) > 0", Lesson.SUBGROUP_ATTR, subgroup));
                 }
                 
                 String[] orderBy = new String[] { Lesson.TIME_ATTR, Lesson.WEEKS_ATTR, Lesson.SUBGROUP_ATTR, Lesson.SUBJ_ATTR };
                 Cursor cursor = db.query(Lesson.TABLE_NAME, null, new String(whereCause), null, null, null, TextUtils.join(",", orderBy));
                 if (cursor != null && cursor.moveToFirst()) {
                     startManagingCursor(cursor);
                     loadedModels.add(new LessonsListModel(title, cursor));
                 } else {
                     cursor.close();
                 }
             }
             
             protected void onCancelled() {
                 try {
                     db.close();
                     db = null;
                 } catch (SQLException e) {
                     // ignore
                 }
             }
             
             @Override
             protected void onPostExecute(Void result) {
                 models = loadedModels;
                 pageAdapter.notifyDataSetChanged();
                 lessonsLoadTask = null;
             }
         }.execute();
     }
 }
