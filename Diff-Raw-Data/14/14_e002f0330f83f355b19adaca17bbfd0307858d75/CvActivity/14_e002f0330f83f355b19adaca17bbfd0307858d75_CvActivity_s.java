 /*
  * Copyright (c) 2013, acbelter
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification,
  * are permitted provided that the following conditions are met:
  *
  * Redistributions of source code must retain the above copyright notice, this
  * list of conditions and the following disclaimer.
  *
  * Redistributions in binary form must reproduce the above copyright notice, this
  * list of conditions and the following disclaimer in the documentation and/or
  * other materials provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
  * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
  * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package com.acbelter.hhtest;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.Intent;
import android.graphics.Point;
 import android.os.Bundle;
 import android.text.InputFilter;
 import android.text.Spanned;
 import android.text.format.DateFormat;
import android.view.Display;
 import android.view.View;
 import android.view.View.OnClickListener;
import android.view.WindowManager;
 import android.widget.*;
 import android.widget.DatePicker.OnDateChangedListener;
 import com.acbelter.hhtest.R.id;
 import com.acbelter.hhtest.R.layout;
 import com.acbelter.hhtest.R.string;
 import com.acbelter.hhtest.R.style;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class CvActivity extends Activity {
     public static final String NAME = "com.acbelter.hhtest.NAME";
     public static final String BD_YEAR = "com.acbelter.hhtest.BD_YEAR";
     public static final String BD_MONTH = "com.acbelter.hhtest.BD_MONTH";
     public static final String BD_DAY = "com.acbelter.hhtest.BD_DAY";
     public static final String SEX = "com.acbelter.hhtest.SEX";
     public static final String OFFICE = "com.acbelter.hhtest.OFFICE";
     public static final String SALARY = "com.acbelter.hhtest.SALARY";
     public static final String PHONE = "com.acbelter.hhtest.PHONE";
     public static final String EMAIL = "com.acbelter.hhtest.EMAIL";
 
     private static final String REPLY = "com.acbelter.hhtest.REPLY";
     private static final String REPLY_DIALOG_STATE = "com.acbelter.hhtest.REPLY_DIALOG_STATE";
     private static final String DP_DIALOG_STATE = "com.acbelter.hhtest.DP_DIALOG_STATE";
     private static final String BIRTH_DATE = "com.acbelter.hhtest.BIRTH_DATE";
 
     private static final int RQ_SEND_CV = 1;
 
     private static final String DF_MASK = "dd.MM.yyyy";
 
     private EditText mName;
     private TextView mBirthDate;
     private Spinner mSexSpinner;
     private EditText mOffice;
     private EditText mSalary;
     private EditText mPhone;
     private EditText mEmail;
     /**
      * Fields for saving dialogs states after screen rotation.
      */
     private boolean mReplyDialogState;
     private String mReply;
     private boolean mDpDialogState;
     private int mBdYear;
     private int mBdMonth;
     private int mBdDay;
 
     /**
      * True, if the date of birth was changed and now it's less than the current date.
      */
     private boolean mCorrectBirthDate;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_cv);
 
         mName = (EditText) findViewById(id.name);
         mBirthDate = (TextView) findViewById(id.birth_date);
         mSexSpinner = (Spinner) findViewById(id.sex_spinner);
         mOffice = (EditText) findViewById(id.office);
         mSalary = (EditText) findViewById(id.salary);
         mPhone = (EditText) findViewById(id.phone);
         mEmail = (EditText) findViewById(id.email);
         /*
         InputFilter for applying strings consists of only letters,
         spaces, points and dashes as name.
          */
         InputFilter nameFilter = new InputFilter() {
             @Override
             public CharSequence filter(CharSequence source, int start, int end,
                                        Spanned dest, int dstart, int dend) {
                 for (int i = start; i < end; i++) {
                     char c = source.charAt(i);
                     if (!Character.isLetter(c) && !(c == ' ') && !(c == '.') && !(c == '-')) {
                         return "";
                     }
                 }
 
                 return null;
             }
         };
 
         mName.setFilters(new InputFilter[]{nameFilter});
         mName.requestFocus();
     }
 
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         outState.putBoolean(REPLY_DIALOG_STATE, mReplyDialogState);
         outState.putBoolean(DP_DIALOG_STATE, mDpDialogState);
         if (mReplyDialogState) {
             outState.putString(REPLY, mReply);
         }
         if (mDpDialogState) {
             outState.putInt(BD_YEAR, mBdYear);
             outState.putInt(BD_MONTH, mBdMonth);
             outState.putInt(BD_DAY, mBdDay);
         }
         outState.putCharSequence(BIRTH_DATE, mBirthDate.getText());
     }
 
     @Override
     public void onRestoreInstanceState(Bundle savedInstanceState) {
         super.onRestoreInstanceState(savedInstanceState);
         mBirthDate.setText(savedInstanceState.getCharSequence(BIRTH_DATE));
         if (savedInstanceState.getBoolean(REPLY_DIALOG_STATE)) {
             buildReplyDialog(this, savedInstanceState.getString(REPLY)).show();
             mReplyDialogState = true;
             return;
         }
         if (savedInstanceState.getBoolean(DP_DIALOG_STATE)) {
             buildDatePickerDialog(this,
                     savedInstanceState.getInt(BD_YEAR),
                     savedInstanceState.getInt(BD_MONTH),
                     savedInstanceState.getInt(BD_DAY)).show();
             mDpDialogState = true;
         }
     }
 
     private int[] getBirthDateValue() {
         String strDate = mBirthDate.getText().toString();
         SimpleDateFormat sdf = new SimpleDateFormat(DF_MASK);
 
         Date date;
         try {
             date = sdf.parse(strDate);
         } catch (ParseException e) {
             return null;
         }
 
         Calendar c = Calendar.getInstance();
         c.setTime(date);
 
         int[] dateArray = new int[3];
         dateArray[0] = c.get(Calendar.YEAR);
         dateArray[1] = c.get(Calendar.MONTH);
         dateArray[2] = c.get(Calendar.DAY_OF_MONTH);
 
         return dateArray;
     }
 
     private void setBirthDateValue(int[] dateArray) {
         if (dateArray == null) {
             mBirthDate.setText(string.date_mask);
             return;
         }
 
         Calendar c = Calendar.getInstance();
 
         c.set(Calendar.YEAR, dateArray[0]);
         c.set(Calendar.MONTH, dateArray[1]);
         c.set(Calendar.DAY_OF_MONTH, dateArray[2]);
 
         mBirthDate.setText(DateFormat.format(DF_MASK, c));
     }
 
     /**
      * Checks the correctness of the entered data.
      * @return True, if the entered data correct.
      */
     private boolean checkInputData() {
         // It's assumed that the date of birth is correct if it isn't empty.
         if (isEmpty(mName)) {
             Toast.makeText(this, getString(string.toast_name), Toast.LENGTH_SHORT).show();
             return false;
         }
 
         Calendar c = Calendar.getInstance();
         Date currentDate = c.getTime();
 
         mCorrectBirthDate = false;
         int[] date = getBirthDateValue();
         if (date != null) {
             c.set(Calendar.YEAR, date[0]);
             c.set(Calendar.MONTH, date[1]);
             c.set(Calendar.DAY_OF_MONTH, date[2]);
             Date birthDate = c.getTime();
              // It's assumed that the date of birth is correct if it's less than the current date.
             if (birthDate.compareTo(currentDate) >= 0) {
                 Toast.makeText(this, getString(string.toast_birth_date), Toast.LENGTH_SHORT).show();
                 return false;
             }
             mCorrectBirthDate = true;
         }
         // It's assumed that the salary is correct if isn't zero and the first digit isn't zero.
         String strSalary = mSalary.getText().toString();
         if (strSalary.length() > 1 && strSalary.startsWith("0")) {
             Toast.makeText(this, getString(string.toast_salary), Toast.LENGTH_SHORT).show();
             return false;
         }
         // It's assumed that the email is correct if it matches with regexp.
         String strPhone = mPhone.getText().toString();
         final String phonePattern = "^\\+?\\d+$";
         Pattern pp = Pattern.compile(phonePattern);
         Matcher pm = pp.matcher(strPhone);
         if (strPhone.length() > 0 && !pm.matches()) {
             Toast.makeText(this, getString(string.toast_phone), Toast.LENGTH_SHORT).show();
             return false;
         }
         /* It's assumed that the email is correct if it matches with regexp.
          The regexp description:
          www.mkyong.com/regular-expressions/how-to-validate-email-address-with-regular-expression
 
          Using this regexp gives more accurate results than using Patterns.EMAIL_ADDRESS.
          */
         String strEmail = mEmail.getText().toString();
         final String emailPattern = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                 + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
         Pattern ep = Pattern.compile(emailPattern);
         Matcher em = ep.matcher(strEmail);
         if (strEmail.length() > 0 && !em.matches()) {
             Toast.makeText(this, getString(string.toast_email), Toast.LENGTH_SHORT).show();
             return false;
         }
 
         return true;
     }
 
     private boolean isEmpty(EditText editText) {
         return editText.getText().toString().trim().length() < 1;
     }
 
     /**
      * Called when user press the button to set date of birth.
      * @param view Clicked button view.
      */
     public void setBirthDate(View view) {
         int[] dateArray = getBirthDateValue();
         if (dateArray == null) {
             dateArray = new int[3];
             Calendar c = Calendar.getInstance();
             dateArray[0] = c.get(Calendar.YEAR);
             dateArray[1] = c.get(Calendar.MONTH);
             dateArray[2] = c.get(Calendar.DAY_OF_MONTH);
         }
 
         buildDatePickerDialog(this, dateArray[0], dateArray[1], dateArray[2]).show();
         mDpDialogState = true;
     }
 
     /**
      * Called when user press the button to send curriculum vitae.
      * @param view Clicked button view.
      */
     public void sendCv(View view) {
         Intent cvIntent = new Intent(this, ReplyActivity.class);
         if (checkInputData()) {
             cvIntent.putExtra(NAME, mName.getText().toString().trim());
 
             if (mCorrectBirthDate) {
                 int[] date = getBirthDateValue();
                 cvIntent.putExtra(BD_YEAR, date[0]);
                 cvIntent.putExtra(BD_MONTH, date[1]);
                 cvIntent.putExtra(BD_DAY, date[2]);
             }
 
             if (mSexSpinner.getSelectedItemPosition() > 0) {
                 cvIntent.putExtra(SEX, mSexSpinner.getSelectedItem().toString());
             }
             if (!isEmpty(mOffice)) {
                 cvIntent.putExtra(OFFICE, mOffice.getText().toString().trim());
             }
             if (!isEmpty(mSalary)) {
                 cvIntent.putExtra(SALARY, mSalary.getText().toString());
             }
             if (!isEmpty(mPhone)) {
                 cvIntent.putExtra(PHONE, mPhone.getText().toString());
             }
             if (!isEmpty(mEmail)) {
                 cvIntent.putExtra(EMAIL, mEmail.getText().toString().trim());
             }
 
             startActivityForResult(cvIntent, RQ_SEND_CV);
         }
     }
 
     /**
      * Creates custom DatePickerDialog.
      * @param context Context of the dialog.
      * @param year Starting year.
      * @param month Starting month.
      * @param day Starting day of month.
      * @return New instance of the dialog.
      */
     private Dialog buildDatePickerDialog(Context context,
                                          int year, int month, int day) {
         final Dialog d = new Dialog(context, style.MyDialogStyle);
         d.setContentView(layout.birth_date_picker);
         d.setCancelable(false);
 
         final DatePicker picker = (DatePicker) d.findViewById(id.birth_date_picker);
         mBdYear = year;
         mBdMonth = month;
         mBdDay = day;
 
         picker.init(year, month, day, new OnDateChangedListener() {
             @Override
             public void onDateChanged(DatePicker view, int year, int month, int day) {
                 mBdYear = year;
                 mBdMonth = month;
                 mBdDay = day;
             }
         });
 
         Button setButton = (Button) d.findViewById(id.set_button);
         setButton.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 d.dismiss();
                 setBirthDateValue(new int[]{picker.getYear(), picker.getMonth(),
                         picker.getDayOfMonth()});
                 mDpDialogState = false;
             }
         });
 
         Button cancelButton = (Button) d.findViewById(id.cancel_button);
         cancelButton.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 d.dismiss();
                 mDpDialogState = false;
             }
         });
 
         return d;
     }
 
     /**
      * Creates dialog with employer's reply.
      * @param context Context of the dialog.
      * @param replyText Employer's reply text.
      * @return New instance of the dialog.
      */
     private Dialog buildReplyDialog(Context context, String replyText) {
         final Dialog d = new Dialog(context, style.MyDialogStyle);
         d.setContentView(layout.reply);
         d.setCancelable(false);
 
        Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
        Point p = new Point();
        display.getSize(p);

        d.getWindow().setLayout((int) (0.8f * p.x), (int) (0.6f * p.y));
 
         TextView replyTextView = (TextView) d.findViewById(id.reply_text);
         if (replyText != null) replyTextView.setText(replyText);
 
         Button closeButton = (Button) d.findViewById(id.close_button);
         closeButton.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View view) {
                 d.dismiss();
                 mReplyDialogState = false;
                 mReply = null;
             }
         });
 
         return d;
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (resultCode == RESULT_OK) {
             if (requestCode == RQ_SEND_CV) {
                 mReply = data.getStringExtra(ReplyActivity.REPLY);
                 buildReplyDialog(this, mReply).show();
                 mReplyDialogState = true;
             }
         }
     }
 }
