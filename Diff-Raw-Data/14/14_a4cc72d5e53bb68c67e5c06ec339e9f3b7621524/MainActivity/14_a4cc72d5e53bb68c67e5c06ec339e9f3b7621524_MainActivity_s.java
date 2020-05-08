 package com.xianglanqi.angrygirl;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 
 import net.simonvt.menudrawer.MenuDrawer;
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.view.Display;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.GridView;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.umeng.analytics.MobclickAgent;
 import com.xianglanqi.angrygirl.adapter.CalendarDayAdapter;
 import com.xianglanqi.angrygirl.adapter.CalendarWeekAdapter;
 import com.xianglanqi.angrygirl.adapter.MenuAdapter;
 import com.xianglanqi.angrygirl.data.CalendarDB;
 import com.xianglanqi.angrygirl.data.CalendarData;
 import com.xianglanqi.angrygirl.data.WXAdapter;
 import com.xianglanqi.angrygirl.model.CalendarCell;
 import com.xianglanqi.angrygirl.model.MenuCell;
 import com.xianglanqi.angrygirl.model.Mood;
 import com.xianglanqi.angrygirl.observer.CalendarDayObserver;
 
 public class MainActivity extends Activity implements CalendarDayObserver {
 
     private MenuDrawer menuDrawer;
 
     private GridView gridViewCalendarDay;
 
     private CalendarDayAdapter adapterCalendarDay;
 
     private GridView gridViewCalendarWeek;
 
     private CalendarWeekAdapter adapterCalendarWeek;
 
     private TextView textViewCalendarMonth;
 
     private ImageView imageViewMore;
 
     private CalendarDB calendarDB;
 
     private TextView textViewCalendarDayDescription;
 
     private Button buttonShareToTimeline;
 
     private ViewGroup menuView;
 
     private ListView menuListView;
 
     private MenuAdapter menuAdapter;
 
     private static final int RESULT_LOAD_IMAGE = 1024;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         // data
         // MobclickAgent.setDebugMode(true);
         WXAdapter.getInstance().regToWX(this);
         calendarDB = new CalendarDB(this);
 
         setContentView(R.layout.activity_main);
 
         // menu view
         this.menuDrawer = MenuDrawer.attach(this);
         this.menuDrawer.setContentView(R.layout.activity_main);
         this.menuView = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.activity_menu, null);
         menuView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
         this.menuListView = (ListView) menuView.findViewById(R.id.listview_menulist);
         this.menuAdapter = new MenuAdapter(this);
         this.menuListView.setAdapter(this.menuAdapter);
         this.menuListView.setOnItemClickListener(new OnItemClickListener() {
 
             @Override
             public void onItemClick(AdapterView<?> parent, View convenView, int position, long id) {
                 if (position == 0) {
                     MainActivity.this.menuDrawer.closeMenu(true);
                     Intent i = new Intent(Intent.ACTION_PICK,
                             android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
 
                     startActivityForResult(i, RESULT_LOAD_IMAGE);
                     return;
                 }
                 final MenuCell cell = MainActivity.this.menuAdapter.getItem(position);
                 if (cell.isParent()) {
                     return;
                 }
                 CalendarData.getIntance().setCalendar(cell.getYear(), cell.getMonth() - 1);
                 MainActivity.this.changeMonth();
                 MainActivity.this.menuDrawer.closeMenu(true);
             }
         });
         this.menuDrawer.setMenuView(menuView);
 
         gridViewCalendarWeek = (GridView) findViewById(R.id.gridview_calendar_week);
         adapterCalendarWeek = new CalendarWeekAdapter(this);
         gridViewCalendarWeek.setAdapter(adapterCalendarWeek);
 
         gridViewCalendarDay = (GridView) findViewById(R.id.gridview_calendar_day);
         adapterCalendarDay = new CalendarDayAdapter(this);
         gridViewCalendarDay.setAdapter(adapterCalendarDay);
 
         textViewCalendarMonth = (TextView) findViewById(R.id.textview_calendar_month);
         imageViewMore = (ImageView) findViewById(R.id.imageview_more);
         imageViewMore.setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View v) {
 
                 if (menuDrawer.isMenuVisible()) {
                     menuDrawer.closeMenu(true);
                 } else {
                     menuDrawer.openMenu(true);
                 }
             }
         });
 
         this.buttonShareToTimeline = (Button) findViewById(R.id.button_share_to_timeline);
         if (WXAdapter.getInstance().isSupportTimeline()) {
             final OnClickListener listener = new OnClickListener() {
 
                 @Override
                 public void onClick(View arg0) {
                     MainActivity.this.textViewCalendarDayDescription.setVisibility(View.INVISIBLE);
                     MainActivity.this.buttonShareToTimeline.setVisibility(View.INVISIBLE);
                     shotscreenAndSend();
                 }
             };
             this.buttonShareToTimeline.setOnClickListener(listener);
         } else {
             this.buttonShareToTimeline.setEnabled(false);
         }
         this.textViewCalendarDayDescription = (TextView) findViewById(R.id.textview_day_description);
         this.textViewCalendarDayDescription.setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View arg0) {
                 Intent intent = new Intent(MainActivity.this, EditDescriptionActivity.class);
                 intent.putExtra("cell", (CalendarCell) textViewCalendarDayDescription.getTag());
                 startActivity(intent);
             }
         });
 
         BackgroundUtil.changeBackground(this,
                 new ImageView[] { ((ImageView) this.menuView.findViewById(R.id.menu_background)),
                         ((ImageView) findViewById(R.id.main_background)) });
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         changeMonth();
 
         this.buttonShareToTimeline.setVisibility(View.VISIBLE);
         MobclickAgent.onResume(this);
         CalendarData.getIntance().registObserver(this);
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         MobclickAgent.onPause(this);
         CalendarData.getIntance().unRegistObserver(this);
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
 
         if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
             Uri selectedImage = data.getData();
             String[] filePathColumn = { MediaStore.Images.Media.DATA };
 
             Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
             cursor.moveToFirst();
 
             int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
             final String picturePath = cursor.getString(columnIndex);
             cursor.close();
 
             BackgroundUtil.changeBackground(picturePath, true, this,
                     new ImageView[] { ((ImageView) this.menuView.findViewById(R.id.menu_background)),
                             ((ImageView) findViewById(R.id.main_background)) });
             MainActivity.this.menuAdapter.notifyDataSetChanged();
         }
     }
 
     @SuppressLint("SimpleDateFormat")
     private void setMonth() {
         SimpleDateFormat format = new SimpleDateFormat("yyyy年M月");
         textViewCalendarMonth.setText(format.format(CalendarData.getIntance().getCurrentCalendar().getTime()));
         int year = CalendarData.getIntance().getCurrentCalendar().get(Calendar.YEAR);
         int month = CalendarData.getIntance().getCurrentCalendar().get(Calendar.MONTH);
         CalendarData.getIntance().setMoodMap(calendarDB.getMood(year, month));
     }
 
     private void changeMonth() {
         setMonth();
         this.adapterCalendarDay.notifyDataSetChanged();
         this.textViewCalendarDayDescription.setVisibility(View.GONE);
     }
 
     @SuppressWarnings("deprecation")
     private void shotscreenAndSend() {
         View view = getWindow().getDecorView();
         Display display = this.getWindowManager().getDefaultDisplay();
         view.layout(0, 0, display.getWidth(), display.getHeight());
         view.setDrawingCacheEnabled(true);// 允许当前窗口保存缓存信息，这样getDrawingCache()方法才会返回一个Bitmap
         Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
 
         WXAdapter.getInstance().sendImageToTimeline("", bitmap);
 
         if (!bitmap.isRecycled()) {
             bitmap.recycle();
         }
     }
 
     @Override
     public void dayChanged(CalendarCell cell) {
         if (cell.getMood() == Mood.UNKNOWN) {
             return;
         }
         this.textViewCalendarDayDescription.setVisibility(View.VISIBLE);
         String prefix = String.format("[点我] %d年%d月%d日: %s", cell.getYear(), cell.getMonth() + 1, cell.getDay(),
                 cell.getDescription());
         this.textViewCalendarDayDescription.setText(prefix);
         this.textViewCalendarDayDescription.setTag(cell);
     }
 }
