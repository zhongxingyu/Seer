 package com.utils.batsurvey;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.view.*;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class BatSurvey extends ListActivity {
     // Links a list of Surveys to a ListView of survey_item views.
     private class SurveyAdapter extends ArrayAdapter<Survey> {
         private ArrayList<Survey> items;
         
         public SurveyAdapter(Context context, int textViewResourceId, ArrayList<Survey> items){
             super(context, textViewResourceId, items);
             this.items = items;
         }
         
         @Override
         public View getView(int position, View convertView, ViewGroup parent) {
             View v = convertView;
             if (v == null) {
                 LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                 v = vi.inflate(R.layout.survey_item, null);
             }
             Survey s = items.get(position);
             if (s != null) {
                 TextView nameTextView = (TextView) v.findViewById(R.id.survey_name);
                 if (nameTextView != null) {
                     nameTextView.setText(s.getName());
                 }
                 TextView dateTextView = (TextView) v.findViewById(R.id.survey_date);
                 if (dateTextView != null) {
                     Date date = s.getDate();
                     Calendar calendar = Calendar.getInstance();
                     calendar.setTime(date);
                     String dateString = String.format("%02d/%02d/%04d",
                         calendar.get(Calendar.DAY_OF_MONTH),
                         calendar.get(Calendar.MONTH),
                         calendar.get(Calendar.YEAR));
                     dateTextView.setText(dateString);
                 }
             }
             return v;
         }
     }
     
     private ArrayList<Survey> surveys = null;
     private SurveyAdapter adapter;
     
     private void createSurvey() {
         // Create a dialog for entering the survey name,
         // then add the created survey to the list.
         AlertDialog.Builder alert = new AlertDialog.Builder(this);
         alert.setTitle(R.string.enter_text);
         
         final EditText input = new EditText(this);
         input.setSingleLine(true);
         alert.setView(input);
         
         alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int whichButton) {
                 String name = input.getText().toString();
                 if (name.length() != 0) {
                     Survey s = new Survey();
                     Date date = new Date();
                     s.setName(name);
                     s.setDate(date);
                     surveys.add(s);
                     adapter.notifyDataSetChanged();
                 }
                 dialog.dismiss();
             }
         });
         alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
             @Override
            public void onClick(DialogInterface dialog, int whichBUtton) {
                 dialog.cancel();
             }
         });
         alert.show();
     }
     
     private void removeCheckedItems() {
         ListView listView = this.getListView();
         int itemCount = listView.getChildCount();
         // Create a list of items to be removed.
         ArrayList<Survey> toRemove = new ArrayList<Survey>();
         for (int i = 0; i < itemCount; i++) {
             View v = listView.getChildAt(i);
             CheckBox c = (CheckBox) v.findViewById(R.id.survey_check);
             if (c.isChecked()) {
                 toRemove.add(this.surveys.get(i));
                 c.setChecked(false);
             }
         }
         // Remove the items from the list and update the adapter.
         for (int i = 0; i < toRemove.size(); i++) {
             this.surveys.remove(toRemove.get(i));
         }
         this.adapter.notifyDataSetChanged();
     }
     
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         this.surveys = new ArrayList<Survey>();
         this.adapter = new SurveyAdapter(this, R.layout.survey_item, surveys);
         setListAdapter(this.adapter);
         
         // Add handler for the "new" button.
         final Button new_button = (Button) findViewById(R.id.new_button);
         new_button.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 createSurvey();
             }
         });
         
         // Add handler for the "delete" button.
         final Button delete_button = (Button) findViewById(R.id.delete_button);
         delete_button.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 removeCheckedItems();
             }
         });
     }
 }
