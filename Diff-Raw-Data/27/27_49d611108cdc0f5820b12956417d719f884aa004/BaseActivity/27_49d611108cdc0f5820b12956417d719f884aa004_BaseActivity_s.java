 package com.gmail.matsushige.nfcv2;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.gmail.matsushige.R;
 import com.gmail.matsushige.nfcv2.db.ActLogDatabaseActivity;
 import com.gmail.matsushige.nfcv2.db.Database;
 import com.gmail.matsushige.nfcv2.db.TemporaryUsersDatabaseOperate;
 import com.gmail.matsushige.nfcv2.db.UsersDatabase;
 
 /**
  * Created by sasaki on 13/09/26.
  */
 public class BaseActivity extends Activity{
     protected LinearLayout linearLayout;
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         menu.add(Menu.NONE, 0, 0, "usersDatabase");
         menu.add(Menu.NONE, 1, 0, "logDatabase");
         menu.add(Menu.NONE, 2, 0, "actlogDatabase");
         menu.add(Menu.NONE, 3, 0, "スタート画面");
         menu.add(Menu.NONE, 4, 0, "tempusersDatabase");
         return super.onCreateOptionsMenu(menu);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem mi) {
         Relay.getRelay(0).open();
         Relay.getRelay(1).open();
         if (CountTimeAllUser.isUsed) {
             CountTimeAllUser.finish = true;
         }
         if (CountRelayTime.isUsed) {
             CountRelayTime.finish = true;
         }
         switch (mi.getItemId()) {
             case 0:
                 setContentView(R.layout.check_users);
                 TextView checkUsersText = (TextView) findViewById(R.id.textViewCheckUsers);
                 UsersDatabase.read(this);
                 checkUsersText.setText(UsersDatabase.usersText);
                 break;
 
             case 1:
                 setContentView(R.layout.check_log);
                 TextView checkLogText = (TextView) findViewById(R.id.textViewCheckLog);
                 Database.read(this);
                 checkLogText.setText(Database.logText);
                 break;
 
             case 2:
                 Intent intent = new Intent();
                 intent.setClass(this, ActLogDatabaseActivity.class);
                 startActivity(intent);
                 break;
 
             case 3:
                 setContentView(R.layout.nfc_main);
                 linearLayout = (LinearLayout) findViewById(R.id.linearLayoutMain);
                 LayoutInflater li = getLayoutInflater();
                 li.inflate(R.layout.nfc_main_start, linearLayout);
                 break;
 
             case 4:
                 setContentView(R.layout.check_temp_users);
                 TextView tempusersText = (TextView) findViewById(R.id.textViewCheckTempUsers);
                TemporaryUsersDatabaseOperate.read(this);
                 tempusersText.setText(TemporaryUsersDatabaseOperate.tempText);
                 break;
 
             default:
                 Toast.makeText(getApplicationContext(), "fault", Toast.LENGTH_SHORT)
                         .show();
                 break;
         }
         return true;
     }
 }
