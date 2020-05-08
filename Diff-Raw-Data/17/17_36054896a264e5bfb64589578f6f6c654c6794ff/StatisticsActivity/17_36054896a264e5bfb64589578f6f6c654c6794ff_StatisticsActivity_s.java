 package com.group7.dragonwars;
 
 import java.text.DecimalFormat;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 
 import com.group7.dragonwars.engine.Database.Database;
 
 public class StatisticsActivity extends Activity implements OnClickListener {
 
     private Button btnMenu;
     private DecimalFormat decformat = new DecimalFormat("#.##");
 
     @Override
     protected final void onCreate(final Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_statistics);
         btnMenu = (Button) findViewById(R.id.btnMenu);
         btnMenu.setOnClickListener(this);
     }
 
     @Override
     protected final void onStart() {
         super.onStart();
         Database db = new Database(getApplicationContext());
 
         /* TODO do the Database.Entry class properly to avoid all this */
         String[] statsEntries = new String[6];
 
         Database.Entry entry = db.GetSummedEntries();
         db.Close();
 
         statsEntries[0] =
            String.format("Damage dealt: %f",
                           decformat.format(entry.DAMAGEDEALT));
         statsEntries[1] =
            String.format("Damage received: %f",
                           decformat.format(entry.DAMAGERECEIVED));
         statsEntries[2] =
            String.format("Distance travelled: %f",
                           decformat.format(entry.DISTANCETRAVELLED));
         statsEntries[3] =
            String.format("Gold collected: %d",
                           decformat.format(entry.GOLDCOLLECTED));
         statsEntries[4] =
            String.format("Units killed: %d",
                           decformat.format(entry.UNITSKILLED));
         statsEntries[5] =
            String.format("Units produced: %d",
                           decformat.format(entry.UNITSMADE));
 
         ArrayAdapter<String> adapter =
             new ArrayAdapter<String>(getBaseContext(),
                                      android.R.layout.simple_list_item_1,
                                      statsEntries);
 
         ListView statsList = (ListView) findViewById(R.id.statsList);
         statsList.setAdapter(adapter);
     }
 
     @Override
     public final void onClick(final View v) {
         if (v == this.btnMenu) {
             Intent intent = new Intent(this, MainMenuActivity.class);
             startActivity(intent);
             finish();
         }
     }
 }
