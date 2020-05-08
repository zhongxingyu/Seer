 package cs2114.group.friendtracker;
 
 import android.content.Intent;
 
 import android.view.View;
 
 import android.widget.TextView;
 
 import android.widget.ScrollView;
 
 import android.widget.RelativeLayout;
 
 import android.os.Bundle;
 
 import android.app.Activity;
 
 /**
  * DayActivity to show the day schedule view.
  *
  * @author Tianyu Geng (tony1)
  * @author Chris Schweinhart (schwein)
  * @author Elena Nadolinski (elena)
  * @version 2012.04.29
  */
 public class DayActivity extends Activity {
     private DayModel model;
     private RelativeLayout rl;
     private DayViewBgd dv;
     private ScrollView sv;
     private TextView nameText;
     private TextView dateText;
     /**
      * The height of an hour.
      */
     public static final int HOUR_HEIGHT = 80;
     /**
      * The left margin for the events.
      */
     public static final int EVENT_LEFT_MARGIN = 60;
 
     /**
      * Called when the activity is created.
      *
      * @param savedInstanceState  The state for parent
      */
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         // DatabaseFiller filler = new DatabaseFiller(this);
         // filler.fill();
         // long ownerId = filler.getPersonId(1);
 
         long ownerId = getIntent().getLongExtra("id", 1);
         model = new DayModel(this.getApplicationContext(), ownerId);
 
         // initialize the GUI
         this.setContentView(R.layout.dayview);
         sv = (ScrollView) findViewById(R.id.sv);
         sv.setSmoothScrollingEnabled(true);
 
         // for names and date
         nameText = (TextView) findViewById(R.id.nameText);
         dateText = (TextView) findViewById(R.id.dateText);
 
         // inside scrollview
         rl = new RelativeLayout(this);
         sv.addView(rl);
         dv = new DayViewBgd(this);
         fillContents();
 
     }
 
     /**
      * Fill the content of the RelativeLayout with the dayViewBgd as the
      * background and EventViews.
      */
     private void fillContents() {
         rl.removeAllViews();
         rl.addView(dv);
         nameText.setText(model.getOwnerName());
         dateText.setText(model.getDate());
         if (model.getEvents() != null && !model.getEvents().isEmpty()) {
 
             final EventView headEv =
                     new EventView(this, model.getEvents().get(0));
 
             RelativeLayout.LayoutParams lp =
                     new RelativeLayout.LayoutParams(100000, 0);
             lp.setMargins(headEv.leftMargin(), headEv.eventPos(), 0, 0);
 
             rl.addView(headEv, lp);
             for (int i = 1; i < model.getEvents().size(); i++) {
                 // Log.d("Tracker-Test", "event="+e);
 
                 EventView ev =
                         new EventView(this, model.getEvents().get(i));
 
                 RelativeLayout.LayoutParams lp2 =
                         new RelativeLayout.LayoutParams(100000, 0);
                 lp2.setMargins(ev.leftMargin(), ev.eventPos(), 0, 0);
                 rl.addView(ev, lp2);
             }
             // Log.d("Scroll", "Scroll Y=" + headEv.eventPos());
             // Scroll to the first Event.
             sv.post(new Runnable() {
                 public void run() {
                     sv.smoothScrollTo(0,
                             headEv.eventPos() - headEv.leftMargin());
                 }
             });
 
         }
 
     }
 
     /**
      * Standard onResume
      */
     public void onResume() {
         super.onResume();
         if (model != null) {
             model.updateEvents();
             fillContents();
         }
     }
 
     /**
      * Method for when the addButton is clicked.
      *
      * @param addButton  The button clicked
      */
     public void addButtonClicked(View addButton) {
 
         Intent addEvent =
                 new Intent(getApplicationContext(),
                         EditEventActivity.class);
        addEvent.putExtra("personId", model.getOwnerId() + "");
 
         startActivity(addEvent);
     }
 
     /**
      * Method for when the previousDay Button is clicked.
      *
      * @param previousButton  The button clicked
      */
     public void previousButtonClicked(View previousButton) {
         model.previousDay();
         fillContents();
     }
 
     /**
      * Method for when the nextDay Button is clicked.
      *
      * @param nextButton  The button clicked
      */
     public void nextButtonClicked(View nextButton) {
         model.nextDay();
         fillContents();
     }
 
     /**
      * Method for when the today button is clicked.
      *
      * @param todayButton  The button clicked
      */
     public void todayButtonClicked(View todayButton) {
         model.today();
         fillContents();
     }
 
 
 }
