 /**
  * 
  */
 package ua.org.furry.activity;
 
 import ua.org.furry.R;
 import android.app.ListActivity;
 import android.os.Bundle;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 /**
  * @author moro
  *
  */
 public class AttendeeListActivity extends ListActivity {
 	String[] listItems = {"exploring", "android",
             "list", "activities"};
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
		setContentView(R.layout.attendee_list_view);
 
         setListAdapter(new ArrayAdapter(this,
         android.R.layout.simple_list_item_1, listItems));
 
     /*	
 		TextView textview = new TextView(this);
         textview.setText("This is the second tab");
         setContentView(textview);
         */
 	}
 
 }
