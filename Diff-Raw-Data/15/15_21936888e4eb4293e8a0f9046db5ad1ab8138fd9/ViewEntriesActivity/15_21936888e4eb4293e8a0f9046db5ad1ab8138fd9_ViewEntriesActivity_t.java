 package com.feedme.activity;
 
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.*;
 import android.widget.*;
 import com.feedme.R;
 import com.feedme.dao.*;
 import com.feedme.model.Baby;
 import com.feedme.model.BaseObject;
 import com.feedme.ui.JournalTable;
 import com.feedme.util.BaseObjectComparator;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.List;
 
 
 /**
  * User: dayel.ostraco
  * Date: 1/16/12
  * Time: 12:29 PM
  */
 public class ViewEntriesActivity extends BaseActivity {
 
     private static final SimpleDateFormat ANDROID_FORMAT = new SimpleDateFormat("M-d-yyyy ");
     private static final SimpleDateFormat HEADER_FORMAT = new SimpleDateFormat("MMMMMMMMM, d yyyy");
 
     private static final int SWIPE_MIN_DISTANCE = 80;
     private static final int SWIPE_MAX_OFF_PATH = 350;
     private static final int SWIPE_THRESHOLD_VELOCITY = 300;
     private GestureDetector gestureDetector;
 
     private JournalTable journalTable = new JournalTable();
     private Baby baby;
     private Calendar calendar;
 
     public static final int VIEW_ENTRIES_ACTIVITY_ID = 50;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.entries_home);
 
         gestureDetector = new GestureDetector(new SlideGesture());
         View journalView = findViewById(R.id.journalScroll);
 
         // Set the touch listener for the  view to be our custom gesture listener
         journalView.setOnTouchListener(new View.OnTouchListener() {
             public boolean onTouch(View v, MotionEvent event) {
                 return gestureDetector.onTouchEvent(event);
             }
         });
 
         googleAnalyticsTracker.startNewSession(TRACKING_ID, this);
         googleAnalyticsTracker.trackPageView("/View-Entries");
 
         calendar = Calendar.getInstance();
         if (getIntent().getExtras() != null && getIntent().getExtras().get("journalDate") != null) {
             calendar = (Calendar) getIntent().getExtras().get("journalDate");
         }
 
         TextView journalHeader = (TextView) findViewById(R.id.journalHeader);
         journalHeader.setText(HEADER_FORMAT.format(calendar.getTime()));
 
         baby = (Baby) getIntent().getSerializableExtra("baby");
         Bundle bundle = new Bundle();
         bundle.putSerializable("baby", baby);
 
         styleActivity(baby.getSex());
         handleButtons(bundle);
 
         List<BaseObject> history = getHistory(baby, ANDROID_FORMAT.format(calendar.getTime()), getApplicationContext());
         TableLayout tableLayout = (TableLayout) findViewById(R.id.myTableLayout);
 
         if (history.size() > 0) {
             journalTable.buildRows(this, history, baby, tableLayout);
         }
 
     }
 
     public void handleButtons(final Bundle bundle) {
         Button childButton = (Button) findViewById(R.id.childScreen);
         childButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
 
                 Intent intent = new Intent(v.getContext(), ViewBabyActivity.class);
                 intent.putExtras(bundle);
                 startActivityForResult(intent, VIEW_ENTRIES_ACTIVITY_ID);
             }
         });
 
         Button previousButton = (Button) findViewById(R.id.previousDay);
         previousButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
 
                 calendar.add(Calendar.DAY_OF_YEAR, -1);
                 Intent intent = new Intent(v.getContext(), ViewEntriesActivity.class);
                 bundle.putSerializable("journalDate", calendar);
                 intent.putExtras(bundle);
                 startActivityForResult(intent, VIEW_ENTRIES_ACTIVITY_ID);
                 ViewEntriesActivity.this.overridePendingTransition(
                         R.anim.slide_in_left,
                         R.anim.slide_out_right
                 );
             }
         });
 
         Button nextButton = (Button) findViewById(R.id.nextDay);
         nextButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
 
                 calendar.add(Calendar.DAY_OF_YEAR, 1);
                 Intent intent = new Intent(v.getContext(), ViewEntriesActivity.class);
                 bundle.putSerializable("journalDate", calendar);
                 intent.putExtras(bundle);
                 startActivityForResult(intent, VIEW_ENTRIES_ACTIVITY_ID);
                 ViewEntriesActivity.this.overridePendingTransition(
                         R.anim.slide_in_right,
                         R.anim.slide_out_left
                 );
             }
         });
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.menu, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
 
         switch (item.getItemId()) {
             case R.id.home:
                 startActivity(new Intent(ViewEntriesActivity.this,
                         HomeActivity.class));
                 break;
             case R.id.settings:
                 startActivity(new Intent(ViewEntriesActivity.this,
                         SettingsActivity.class));
                 break;
             case R.id.report:
                 startActivity(new Intent(ViewEntriesActivity.this,
                         ReportBugActivity.class));
                 break;
         }
         return true;
     }
 
     /**
      * Returns a List containing Diapers, Naps and Feedings sorted by Date and then StartTime.
      *
      * @param baby
      * @param date
      * @param context
      * @return
      */
     private List<BaseObject> getHistory(Baby baby, String date, Context context) {
 
         JournalDao journalDao = new JournalDao(context);
         DiaperDao diaperDao = new DiaperDao(context);
         NapDao napDao = new NapDao(context);
 
         List<BaseObject> history = new ArrayList<BaseObject>();
         history.addAll(journalDao.getEntriesForChildByDate(baby.getId(), date));
         history.addAll(diaperDao.getDiapersForChildByDate(baby.getId(), date));
         history.addAll(napDao.getNapsForChildByDate(baby.getId(), date));
 
         Collections.sort(history, new BaseObjectComparator());
         Collections.reverse(history);
 
         return history;
     }
 
    /**
     * Allows the ScrollView and the Gestures to peacefully coexist.
     *
     * @param ev
     * @return
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev){

        super.dispatchTouchEvent(ev);

        return gestureDetector.onTouchEvent(ev);

    }

     class SlideGesture extends GestureDetector.SimpleOnGestureListener {
 
         @Override
         public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
             Intent intent = new Intent(ViewEntriesActivity.this.getBaseContext(), ViewEntriesActivity.class);
             Bundle bundle = new Bundle();
             bundle.putSerializable("baby", baby);
 
             if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH) {
                 return false;
             }
 
             // right to left swipe
             if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                 calendar.add(Calendar.DAY_OF_YEAR, 1);
                 bundle.putSerializable("journalDate", calendar);
                 intent.putExtras(bundle);
                 startActivity(intent);
                 ViewEntriesActivity.this.overridePendingTransition(
                         R.anim.slide_in_right,
                         R.anim.slide_out_left
                 );
 
             // right to left swipe
             } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                 calendar.add(Calendar.DAY_OF_YEAR, -1);
                 bundle.putSerializable("journalDate", calendar);
                 intent.putExtras(bundle);
                 startActivity(intent);
                 ViewEntriesActivity.this.overridePendingTransition(
                         R.anim.slide_in_left,
                         R.anim.slide_out_right
                 );
             }
 
             return false;
         }
 
         // It is necessary to return true from onDown for the onFling event to register
         @Override
         public boolean onDown(MotionEvent e) {
             return true;
         }
     }
 }
