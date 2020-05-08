 package com.feedme.activity;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.*;
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
 public class ViewEntriesActivity extends Activity
 {
 
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.entries_home);
         handleButtons();
 
         final JournalDao journalDao = new JournalDao(getApplicationContext());
         Journal[] myList = journalDao.getAllEntriesAsArray();
 
         TableLayout tl = (TableLayout) findViewById(R.id.myTableLayout);
 
         int j = 0;
         while (j < myList.length)
         {
             final String entryDate = myList[j].getDate();
             TableRow tr1 = new TableRow(this);  //create new row
 
             tr1.setBackgroundColor(0xFFD2EDFC);
             TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(
                     TableRow.LayoutParams.FILL_PARENT,
                     TableRow.LayoutParams.WRAP_CONTENT);
             tr1.setLayoutParams(layoutParams);
             tr1.setClickable(true);
             tr1.setPadding(5, 5, 5, 5);
 
 //            ImageView imageView = new ImageView(this);
 //            imageView.setImageDrawable(R.drawable.);
 
             LinearLayout linearLayout = new LinearLayout(this);
 
             LinearLayout.LayoutParams linearLayoutParams = new TableRow.LayoutParams(
                     LinearLayout.LayoutParams.FILL_PARENT,
                     LinearLayout.LayoutParams.WRAP_CONTENT);
 
             linearLayout.setOrientation(LinearLayout.VERTICAL);
 
             TextView feedAmount = new TextView(this);
             feedAmount.setText(myList[j].getOunces());
 
             linearLayout.addView(feedAmount);
 
             TextView babyWake = new TextView(this);
             babyWake.setText("Wake / Sleep");
 
             linearLayout.addView(babyWake);
 
             TextView babyDiaper = new TextView(this);
             babyDiaper.setText(myList[j].getStartTime());
 
             linearLayout.addView(babyDiaper);
 
             tr1.setOnClickListener(new View.OnClickListener()
             {
                 public void onClick(View v)
                 {
                     Intent intent = new Intent(v.getContext(), ViewEntryActivity.class);
                     intent.putExtra("entryDate", entryDate);
                     startActivityForResult(intent, 3);
                 }
             });

             final BabyDao babyDao = new BabyDao(getApplicationContext());
             Baby baby = babyDao.getBaby(myList[j].getChild());
 
 
             /* Add row to TableLayout. */
             tl.addView(tr1, new TableLayout.LayoutParams(
                     TableRow.LayoutParams.FILL_PARENT,
                     TableRow.LayoutParams.WRAP_CONTENT));
 
             j++; //iterator
         }
 
     }
 
     public void handleButtons()
     {
 
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu)
     {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.menu, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item)
     {
 
         switch (item.getItemId()) {
             case R.id.home:
                 startActivity(new Intent(ViewEntriesActivity.this,
                         HomeActivity.class));
             case R.id.settings:
                 startActivity(new Intent(ViewEntriesActivity.this,
                         SettingsActivity.class));
                 break;
         }
         return true;
     }
 
 }
