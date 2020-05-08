 package com.app.settleexpenses;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 import com.app.settleexpenses.service.DbAdapter;
 import com.app.settleexpenses.service.IDbAdapter;
 import com.app.settleexpenses.service.ServiceLocator;
 
 
 public class CreateEvent extends Activity {
 
     private EditText titleText;
     private static final int ACTIVITY_ADD_EXPENSE = 0;
     private final Activity currentActivity = this;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.create_event);
        setTitle(getString(R.string.create_event));
 
         titleText = (EditText) findViewById(R.id.title);
         final long eventId = getIntent().getLongExtra(DbAdapter.EVENT_ID, -1);
         titleText.setText(eventTitle(eventId));
 
         Button confirmButton = (Button) findViewById(R.id.confirm);
 
         confirmButton.setOnClickListener(createEventHandler(eventId));
     }
 
     private String eventTitle(long eventId) {
         IDbAdapter dbAdapter = ServiceLocator.getDbAdapter();
         String title = "";
         if(eventId != -1) {
             title = dbAdapter.getEventById(eventId).getTitle();
         }
         return title;
     }
 
     private View.OnClickListener createEventHandler(final long eventId) {
         return new View.OnClickListener() {
 
             public void onClick(View view) {
                 String title = titleText.getText().toString();
                 if (isInvalid(title)) return;
 
                 IDbAdapter dbAdapter = ServiceLocator.getDbAdapter();
                 long newEventId = dbAdapter.createOrUpdateEvent(eventId, title);
 
                 Intent intent = new Intent(currentActivity, AddExpenses.class);
                 intent.putExtra(DbAdapter.EVENT_ID, newEventId);
                 startActivityForResult(intent, ACTIVITY_ADD_EXPENSE);
             }
 
         };
     }
 
     private boolean isInvalid(String title) {
         if (title != null && title.trim().length() == 0) {
             Toast toast = Toast.makeText(currentActivity, getString(R.string.no_event_name_validation), Toast.LENGTH_LONG);
             toast.show();
             return true;
         }
         return false;
     }
 }
