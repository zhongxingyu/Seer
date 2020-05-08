 package com.hmd.emotibox;
 
 import android.view.View;
 import android.os.Bundle;
 import android.app.Activity;
 import android.widget.Toast;
 import android.widget.Button;
 import android.widget.TableRow;
 import android.graphics.Typeface;
 import android.widget.TableLayout;
 import android.text.ClipboardManager;
 import android.widget.TableLayout.LayoutParams;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MenuInflater;
 import android.app.Dialog;
 import android.app.AlertDialog;
 import android.database.Cursor;
 import android.util.Log;
 
 import com.hmd.emotibox.AboutDialog;
 
 public class FirstTabActivity extends Activity {
     final static public int ABOUT_DIALOG = 0x10000000;
     final static public int HELP_DIALOG = 0x20000000;
     private DbAdapter mDbHelper;
    private Button btnMostUsed1;
    private Button btnMostUsed2;
    private Button btnMostUsed3;
    private Button btnMostUsed4;
    private Button btnMostUsed5;
     
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         setContentView(R.layout.first_tab);
         
         String chars[] = {
             "\u2665", "\u263A", "\u2639", "\u263B", "\u2620",
             "\u266A", "\u266B", "\u2601", "\u2602", "\u2603", 
             "\u263C", "\u263E", "\u2729", "\u2708", "\u2709",
             "\u00AE", "\u00A9", "\u2122", "\u2627", "\u2624",
             "\u2642", "\u2640", "\u262F", "\u262E", "\u267A",
             "\u2622", "\u2623", "\u2707", "\u262A", "\u262D",
         };
         Typeface typeface = Typeface.createFromAsset(getAssets(),"fonts/DejaVuSans.ttf");
         TableLayout table = (TableLayout) findViewById(R.id.table1);
         
         TableRow row = new TableRow(this);
        row.setBackgroundResource(R.color.white);
         for (int i = 0; i < 5; i++){
             Button button = new Button(this);
             button.setHeight(55);
             button.setWidth(55);
             button.setText("");
             button.setTag("");
             button.setTextSize(25);
             button.setTypeface(typeface);
             row.addView(button);
         }
         table.addView(row, new TableLayout.LayoutParams(
                 LayoutParams.FILL_PARENT,
                 LayoutParams.WRAP_CONTENT));
         for (int i = 0; i < chars.length; i++){
             if ((i % 5 == 0) | (i == 0)){
                 row = new TableRow(this);
                 table.addView(row, new TableLayout.LayoutParams(
                         LayoutParams.FILL_PARENT,
                         LayoutParams.WRAP_CONTENT));
             }
             Button button = new Button(this);
             button.setHeight(55);
             button.setWidth(55);
             button.setText(chars[i]);
             button.setTag(chars[i]);
             button.setTextSize(25);
             button.setTypeface(typeface);
             button.setOnClickListener(new View.OnClickListener() {
                 public void onClick(View v) {
                     ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                     String ch = v.getTag().toString();
                     Toast.makeText(FirstTabActivity.this, "Copied: " + ch, Toast.LENGTH_SHORT).show();
                     cm.setText(ch);
                 }
             });
             row.addView(button);
         }
         
         Log.d("Emotibox", "Fetching most used");
         mDbHelper = new DbAdapter(this);
         mDbHelper.open();
         Cursor c = mDbHelper.fetchMostUsed();
         if (c != null) {
             int count = c.getCount();
             Log.d("Emotibox", "Processing " + count + " records");
             c.moveToFirst();
             if (count > 0){
                 while (!c.isLast()){
                     Log.w("--MostUsed", "Key: "+ c.getInt(0) + " - Value: " + c.getInt(1));
                     c.moveToNext();
                 }
             }
         }
     }
     
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.main, menu);
         return true;
     }
     
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.about:
                 showDialog(ABOUT_DIALOG);
                 break;
             case R.id.help:
                 showDialog(HELP_DIALOG);
                 break;
             case R.id.quit:
                 onQuit();
         }
         return true;
     }
     
     public void onQuit(){
         this.finish();
     }
     
     protected Dialog onCreateDialog(int id) {
         AlertDialog.Builder builder;
         switch (id) {
             case ABOUT_DIALOG:
                 builder = new AboutDialog(FirstTabActivity.this);
                 break;
             case HELP_DIALOG:
                 builder = new HelpDialog(FirstTabActivity.this);
                 break;
             default:
                 builder = null;
         }
         return builder.create();
     }
 }
