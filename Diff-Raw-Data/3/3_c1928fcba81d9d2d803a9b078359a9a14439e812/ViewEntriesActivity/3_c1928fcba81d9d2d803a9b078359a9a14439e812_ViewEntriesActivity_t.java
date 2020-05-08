 package com.feedme.activity;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import com.feedme.R;
 import com.feedme.dao.BabyDao;
 import com.feedme.dao.JournalDao;
 import com.feedme.model.Baby;
 import com.feedme.model.Journal;
 
 
 /**
  * User: dayel.ostraco
  * Date: 1/16/12
  * Time: 12:29 PM
  */
 public class ViewEntriesActivity extends Activity {
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.entries_home);
         handleButtons();
 
         final JournalDao journalDao = new JournalDao(getApplicationContext());
         Journal[] myList = journalDao.getAllEntriesAsArray();
 
         TableLayout tl = (TableLayout)findViewById(R.id.myTableLayout);
 
         int j = 0;
         while (j < myList.length) {
             TableRow tr = new TableRow(this);  //create new row
 
             tr.setLayoutParams(new TableRow.LayoutParams(    //set params of row
                     TableRow.LayoutParams.FILL_PARENT,
                     TableRow.LayoutParams.FILL_PARENT));
 
             final Button b = new Button(this);                    //create button for child
             tr.setPadding(5,5,5,5);
 
             b.setText(myList[j].getDate());                     //put child's name on button
 
             final BabyDao babyDao = new BabyDao(getApplicationContext());
             Baby baby = babyDao.getBaby(myList[j].getChild());
             if ((baby.getSex()).equals("Male")) {
                 tr.setBackgroundColor(0xFF7ED0FF);
                 b.setBackgroundColor(0xFF7ED0FF);
             } else {
                 tr.setBackgroundColor(0xFFFF99CC);
                 b.setBackgroundColor(0xFFFF99CC);
             }
 
             b.setLayoutParams(new TableRow.LayoutParams(        //set params of button
                     TableRow.LayoutParams.FILL_PARENT,
                     TableRow.LayoutParams.FILL_PARENT));
 
             //button listener for each entry
             b.setOnClickListener(new View.OnClickListener() {
                 public void onClick(View v) {
                     Intent intent = new Intent(v.getContext(), ViewEntryActivity.class);
                     intent.putExtra("entryDate", b.getText());
                     startActivityForResult(intent, 3);
                 }
             });
 
             /* Add Button to row. */
             tr.addView(b);
 
             /* Add row to TableLayout. */
             tl.addView(tr,new TableLayout.LayoutParams(
                     TableRow.LayoutParams.FILL_PARENT,
                     TableRow.LayoutParams.WRAP_CONTENT));
 
             j++; //iterator
         }
 
     }
 
     public void handleButtons() {
 
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
         }
         return true;
     }
 
 }
