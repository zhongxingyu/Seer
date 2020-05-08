 package me.taedium.android.add;
 
 import me.taedium.android.HeaderActivity;
 import me.taedium.android.R;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.WindowManager;
 import android.widget.Button;
 import android.widget.TextView;
 
 public abstract class WizardActivity extends HeaderActivity {
     protected final static int ACTIVITY_ADD_PEOPLE = 550;
     protected final static int ACTIVITY_ADD_TIME = 551;
     protected final static int ACTIVITY_ADD_ENVIRONMENT = 552;
     protected final static int ACTIVITY_ADD_LOCATION = 553;
     protected final static int ACTIVITY_ADD_TAGS = 554;
     protected final static int ACTIVITY_FIRST_START = 555;
     
     protected final static int DIALOG_HELP = 1001;
     
     protected String title = "";
     protected String helpText = "";
     
     protected Bundle data;
     protected Button bBack, bNext;
     
     protected void initializeWizard(final Context context, final Class<?> cls, final int requestCode) {
         // Initialize the header and help button
         initializeHeader(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 showDialog(DIALOG_HELP);
             }
         });
 
         // Initialize the back and previous buttons
         bNext = (Button)findViewById(R.id.bAddNext);
         bNext.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 fillData();
                 Intent i = new Intent(context, cls);
                 i.putExtras(data);
                 startActivityForResult(i, requestCode);
             }
         });
         bBack = (Button)findViewById(R.id.bAddBack);
         bBack.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 fillData();
                 Intent i = new Intent();
                 i.putExtras(data);
                 setResult(RESULT_OK, i);
                 finish();
             }
         });
     }
     
     // Helper to format string correctly when adding them to the data bundle
     // Inserts newlines correctly and removes tabs
     protected String escapeString(String text) {
     	return text.replace("\n", "\\n").replace("\t", "");
     }
     
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent i) {
         if (resultCode == RESULT_OK) {
             data = i.getExtras();
         }
     }
 
     @Override
     protected Dialog onCreateDialog(int id) {
         Button bOk;
         final Dialog dialog;
         dialog = new Dialog(this, R.style.Dialog);
         WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
         lp.dimAmount = 0.0f;
         dialog.getWindow().setAttributes(lp);
         dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
         switch(id) {
             case DIALOG_HELP:
                 dialog.setContentView(R.layout.dialog_help);
                 dialog.setTitle(title);
                 TextView text = (TextView)dialog.findViewById(R.id.tvHelpText);
                 text.setText(helpText);
                 bOk = (Button)dialog.findViewById(R.id.bOk);
                 bOk.setOnClickListener(new View.OnClickListener() {
                     public void onClick(View v) {
                         dismissDialog(DIALOG_HELP);
                     }
                 });
                 break;
             default:
                 // This activity dialog not known to this activity
                 return super.onCreateDialog(id);
         }
         return dialog;
     }
     
     protected abstract void fillData();
     protected abstract void restoreData();
 }
