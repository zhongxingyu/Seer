 package com.example.AndroidBasicUI;
 
 import android.app.*;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: thinhdd
  * Date: 10/9/13
  * Time: 4:37 PM
  * To change this template use File | Settings | File Templates.
  */
 public class DetailActivity extends Activity
 {
     private static final String TAR = "DetailActivity.class";
     private TextView detail_tvAddress;
     private TextView detail_tvFieldAddress;
     private TextView detail_tvBirthDay;
     private TextView detail_tvFieldBirthDay;
     private TextView detail_tvOnline;
     private TextView detail_tvFieldOnline;
     private TextView detail_tvGender;
     private TextView detail_tvFieldGender;
     private RadioGroup detail_rgGenderGroup;
     private TextView detail_tvLogOut;
     private CheckBox detail_cbShowListContactCB;
     private RelativeLayout detail_rlRelativeListContact;
     private int mDay;
     private int mMonth;
     private int mYear;
     private int mHour;
     private int mMinute;
     static final int DATE_DIALOG_ID = 0;
     static final int TIME_DIALOG_ID = 1;
 
     @Override
     protected void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.detail);
         setUpViewDetailActivity();
         setUpActionViewOnClick();
         if(savedInstanceState!= null)
         {
             detail_tvAddress.setText(savedInstanceState.get("address").toString());
         }
 
     }
 
     private void setUpActionViewOnClick()
     {
         detail_tvFieldAddress.setOnClickListener(new View.OnClickListener()
         {
             @Override
             public void onClick(View v)
             {
                 showListProvinceDialog(v);
             }
         });
         detail_tvFieldBirthDay.setOnClickListener(new View.OnClickListener()
         {
             @Override
             public void onClick(View v)
             {
                 showDialog(DATE_DIALOG_ID);
             }
         });
         detail_tvFieldOnline.setOnClickListener(new View.OnClickListener()
         {
             @Override
             public void onClick(View v)
             {
                 showDialog(TIME_DIALOG_ID);
             }
         });
         detail_rgGenderGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
         {
             @Override
             public void onCheckedChanged(RadioGroup group, int checkedId)
             {
                 int selectIdGroupRadio = detail_rgGenderGroup.getCheckedRadioButtonId();
                 String gender = ((RadioButton) findViewById(selectIdGroupRadio)).getText().toString();
                 detail_tvGender.setText(gender);
                 detail_rgGenderGroup.setVisibility(View.GONE);
             }
         });
         detail_tvFieldGender.setOnClickListener(new View.OnClickListener()
         {
             @Override
             public void onClick(View v)
             {
                 detail_rgGenderGroup.setVisibility(View.VISIBLE);
             }
         });
         detail_cbShowListContactCB.setOnClickListener(new View.OnClickListener()
         {
             @Override
             public void onClick(View v)
             {
                 if (detail_cbShowListContactCB.isChecked())
                     detail_rlRelativeListContact.setVisibility(View.VISIBLE);
                 else
                     detail_rlRelativeListContact.setVisibility(View.GONE);
             }
         });
 
         detail_tvLogOut.setOnClickListener(new View.OnClickListener()
         {
             @Override
             public void onClick(View v)
             {
                 Intent androidBasicUI = new Intent(v.getContext(), AndroidBasicUI.class);
                 v.getContext().startActivity(androidBasicUI);
                 finish();
             }
         });
 
 
     }
     private void setUpViewDetailActivity()
     {
         detail_tvAddress = (TextView) findViewById(R.id.detail_tvAddress);
         detail_tvFieldAddress = (TextView) findViewById(R.id.detail_tvFieldAddress);
         detail_tvBirthDay = (TextView) findViewById(R.id.detail_tvBirthDay);
         detail_tvFieldBirthDay = (TextView) findViewById(R.id.detail_tvFieldBirthDay);
         detail_tvOnline = (TextView) findViewById(R.id.detail_tvOnline);
         detail_tvFieldOnline = (TextView) findViewById(R.id.detail_tvFieldOnline);
         detail_tvGender = (TextView) findViewById(R.id.detail_tvGender);
         detail_tvFieldGender = (TextView) findViewById(R.id.detail_tvFieldGender);
         detail_tvLogOut = (TextView) findViewById(R.id.detail_tvLogout);
         detail_rgGenderGroup = (RadioGroup) findViewById(R.id.detail_rgGender);
         detail_cbShowListContactCB = (CheckBox) findViewById(R.id.detail_cbShowListContact);
         detail_rlRelativeListContact = (RelativeLayout) findViewById(R.id.detail_rlContactList);
     }
     public void showListProvinceDialog(View v)
     {
         final String[] listProvince = getResources().getStringArray(R.array.province);
         AlertDialog.Builder builder=new AlertDialog.Builder(this);
         builder.setTitle("Show List Province");
         builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
 
             @Override
             public void onClick(DialogInterface dialog, int which) {}
         });
 
         builder.setSingleChoiceItems(listProvince,-1, new DialogInterface.OnClickListener() {
 
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 detail_tvAddress.setText(listProvince[which]);
             }
         });
         builder.show();
     }
 
     @Override
     protected Dialog onCreateDialog(int id) {
         switch (id) {
             case DATE_DIALOG_ID:
                 return new DatePickerDialog(this,
                         mDateSetListener,
                         mYear, mMonth, mDay);
             case TIME_DIALOG_ID:
                 return new TimePickerDialog(this,timePickerListener,mHour, mMinute,false);
         }
         return null;
     }
     private void updateDisplay() {
         detail_tvBirthDay.setText(mDay + "/" + mMonth + 1 + "/" + mYear);
     }
 
     private DatePickerDialog.OnDateSetListener mDateSetListener =
             new DatePickerDialog.OnDateSetListener() {
 
                 public void onDateSet(DatePicker view, int year,
                                       int monthOfYear, int dayOfMonth) {
                     mYear = year;
                     mMonth = monthOfYear;
                     mDay = dayOfMonth;
                     updateDisplay();
                 }
             };
     private TimePickerDialog.OnTimeSetListener timePickerListener =
             new TimePickerDialog.OnTimeSetListener() {
                 public void onTimeSet(TimePicker view, int selectedHour,
                                       int selectedMinute) {
                     mHour = selectedHour;
                     mMinute = selectedMinute;
                     ViewGroup vg = (ViewGroup) view.getChildAt(0);
                    String setTime = ((Button) vg.getChildAt(2)).getText().toString();
                     detail_tvOnline.setText(new StringBuilder().append(pad(mHour))
                             .append(":").append(pad(mMinute)).append(setTime));
 
                 }
             };
     private static String pad(int c) {
         if (c >= 10)
             return String.valueOf(c);
         else
             return "0" + String.valueOf(c);
     }
 
     @Override
     protected void onSaveInstanceState(Bundle outState)
     {
         super.onSaveInstanceState(outState);    //To change body of overridden methods use File | Settings | File Templates.
         outState.putString("address", detail_tvAddress.getText().toString());
     }
 }
