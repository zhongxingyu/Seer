 package com.pnegre.safe;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.widget.EditText;
 import com.pnegre.safe.database.Database;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 import java.util.RandomAccess;
 
 
 abstract class CommonSecret extends Activity {
     protected EditText mETsitename, mETusername, mETpassword;
     protected SafeApp mApp;
     protected Database mDatabase;
 
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.newsecret);
        mApp = (SafeApp) getApplication();
        mDatabase = mApp.getDatabase();
 
         mETsitename = (EditText) findViewById(R.id.sitename);
         mETusername = (EditText) findViewById(R.id.siteusname);
         mETpassword = (EditText) findViewById(R.id.sitepassword);
     }
 
 
     // Inflate res/menu/menuactivity.xml
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.newsecretmenu, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.secretsave:
                 newSecret();
                 break;
 
             case R.id.genpassword:
                 mETpassword.setText(generatePassword(8));
                 mETpassword.requestFocus();
                 break;
         }
 
         return true;
     }
 
     protected void newSecret() {
         String sname = mETsitename.getText().toString();
         String usname = mETusername.getText().toString();
         String pw = mETpassword.getText().toString();
         if (sname.equals("") || pw.equals("") || usname.equals("")) {
             AlertDialog.Builder alert = new AlertDialog.Builder(this);
             alert.setTitle("No Blanks!");
             alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {
                 }
             });
             alert.show();
         } else {
             saveNewSecret(sname, pw, usname);
         }
     }
 
     protected String generatePassword(int len) {
 
 
         RandPass rp = new RandPass(RandPass.NUMBERS | RandPass.UPPER);
         return rp.getPass(8);
 
 
 
 
 
 
 //        Random rnd = new Random();
 //        final String AB = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
 //        final String allChars = AB + AB.toLowerCase() + "0123456789";
 //        StringBuilder sb = new StringBuilder(len);
 //        for (int i=0; i<len; i++) {
 //            sb.append(allChars.charAt(rnd.nextInt(allChars.length())));
 //        }
 //        return sb.toString();
     }
 
     protected abstract void saveNewSecret(String sname, String pw, String usname);
 }
 
 
 
 
