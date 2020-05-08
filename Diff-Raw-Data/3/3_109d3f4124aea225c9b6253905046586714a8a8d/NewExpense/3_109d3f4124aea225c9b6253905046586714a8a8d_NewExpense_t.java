 package org.barbon.acash;
 
 import android.app.Activity;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 
 import android.content.DialogInterface;
 
 import android.os.Bundle;
 
 import android.text.format.DateFormat;
 
 import android.view.View;
 
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.TextView;
 
 import java.util.Date;
 
public class NewExpense extends Activity {
     private EditText transferAmount;
 
     private TextView transferDateView;
     private Date transferDate;
 
     private static final int DATE_DIALOG_ID = 0;
 
     // set the transaction date
     private DatePickerDialog.OnDateSetListener dateSet =
         new DatePickerDialog.OnDateSetListener() {
             public void onDateSet(DatePicker view, int year,
                                   int month, int day) {
                 setTransferDate(year, month, day);
             }
         };
 
     // reset the dialog date when the user closes it
     private DialogInterface.OnDismissListener dateDismissed =
         new DialogInterface.OnDismissListener() {
             public void onDismiss(DialogInterface dialog) {
                 removeDialog(DATE_DIALOG_ID);
             }
         };
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.newexpense);
 
         transferDateView = (TextView) findViewById(R.id.transfer_date);
         transferAmount = (EditText) findViewById(R.id.transfer_amount);
 
         // show date picker when clicking on the transfer date
         transferDateView.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 showDialog(DATE_DIALOG_ID);
             }
         });
 
         setTransferDate(new Date());
         transferAmount.setText("");
     }
 
     @Override
     protected Dialog onCreateDialog(int id) {
         switch (id) {
         case DATE_DIALOG_ID:
             DatePickerDialog dateDialog =
                 new DatePickerDialog(this, dateSet,
                                      transferDate.getYear() + 1900,
                                      transferDate.getMonth(),
                                      transferDate.getDay());
 
             dateDialog.setOnDismissListener(dateDismissed);
 
             return dateDialog;
         }
 
         return null;
     }
 
     private void setTransferDate(Date date) {
         transferDate = date;
 
         java.text.DateFormat dateFormat =
             DateFormat.getDateFormat(getApplicationContext());
 
         transferDateView.setText(dateFormat.format(transferDate));
     }
 
     private void setTransferDate(int year, int month, int day) {
         setTransferDate(new Date(year - 1900, month, day));
     }
 }
