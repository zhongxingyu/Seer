 package com.telecom.navigation;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.text.TextUtils;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.telecom.model.Customer;
 import com.telecom.model.Master;
 import com.telecom.util.JsonUtil;
 
 public class AuthenticationActivity extends BaseActivity {
     private View mLayoutInputNumber;
 
     private View mLayoutLinearAuth;
 
     private TextView mTxtUserName;
 
     private TextView mTxtMaster;
 
     private MyAsyncTask mMyAsyncTask;
 
     private IMSITask mImsiTask;
 
     private EditText mEditUserPhone;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         // The look of this sample is set via a style in the manifest
         setContentView(R.layout.authentication_activity);
 
         mLayoutInputNumber = findViewById(R.id.linear_input_number);
         mLayoutLinearAuth = findViewById(R.id.linear_auth);
         mTxtUserName = (TextView) findViewById(R.id.txt_user_name);
         mTxtMaster = (TextView) findViewById(R.id.txt_master);
         mEditUserPhone = (EditText) findViewById(R.id.edit_user_phone);
 
         mImsiTask = new IMSITask(mTxtUserName);
         mImsiTask.execute();
     }
 
     public boolean isMobileNO(CharSequence mobiles) {
//        Pattern p = Pattern.compile("^[1]([3][0-9]{1}|59|58|80|88|89)[0-9]{8}$");
        Pattern p = Pattern.compile("^[1][0-9]{10}$");
         Matcher m = p.matcher(mobiles);
         return m.find();
     }
 
     public void onBtnAuthClick(View v) {
         if (TextUtils.isEmpty(mEditUserPhone.getText()) || !isMobileNO(mEditUserPhone.getText())) {
             Toast.makeText(this, R.string.str_format_input_phone, Toast.LENGTH_LONG).show();
             return;
         }
 
         mMyAsyncTask = new MyAsyncTask(mTxtMaster);
         mMyAsyncTask.execute();
 
         mUserPhone = mEditUserPhone.getText().toString();
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
         if (mMyAsyncTask != null) {
             mMyAsyncTask.cancel(true);
         }
         mImsiTask.cancel(true);
     }
 
     public void onBtnNextClick(View v) {
         Intent intent = new Intent(this, AppliactionCategoryActivity.class);
         startActivity(intent);
     }
 
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
         if (mLayoutLinearAuth.getVisibility() == View.VISIBLE) {
             return super.onPrepareOptionsMenu(menu);
         } else {
             menu.getItem(0).setTitle(R.string.btn_exit_txt);
             return true;
         }
     }
 
     @Override
     public boolean onMenuItemSelected(int featureId, MenuItem item) {
 
         if (mLayoutLinearAuth.getVisibility() == View.VISIBLE) {
             return super.onMenuItemSelected(featureId, item);
         } else {
             switch (item.getItemId()) {
             case R.id.menu_item_close:
                 Intent intent = new Intent(Intent.ACTION_MAIN);
                 intent.addCategory(Intent.CATEGORY_HOME);
                 intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                 startActivity(intent);
                 android.os.Process.killProcess(android.os.Process.myPid());
                 break;
             }
             return true;
         }
     }
 
     private class MyAsyncTask extends AsyncTask<Void, Void, Master> {
 
         private TextView mTxtMaster;
 
         public MyAsyncTask(TextView txtView) {
             mTxtMaster = txtView;
         }
 
         @Override
         protected Master doInBackground(Void... params) {
             Master master = JsonUtil.getMasterInfo(mUserPhone);
             if (master != null) {
                 mUserId = master.getUserId();
             }
             return master;
         }
 
         @Override
         protected void onPostExecute(Master result) {
 
             if (result == null || TextUtils.isEmpty(result.getUserId())
                     || TextUtils.isEmpty(result.getUserName())) {
                 Toast.makeText(getApplicationContext(), R.string.txt_toast_get_message_failed,
                         Toast.LENGTH_LONG).show();
                 return;
             }
 
             mLayoutInputNumber.setVisibility(View.GONE);
             mLayoutLinearAuth.setVisibility(View.VISIBLE);
             mTxtMaster.setText(getString((R.string.str_format_master_info), result.getUserName()));
         }
     }
 
     private class IMSITask extends AsyncTask<Void, Void, String> {
 
         private TextView mTxtMaster;
 
         public IMSITask(TextView txtView) {
             mTxtMaster = txtView;
         }
 
         @Override
         protected String doInBackground(Void... params) {
             Customer customer = JsonUtil.getCustomerInfoByIMSI(mIMSI);
             if (customer != null) {
                 mProId = customer.getProdId();
             }
             return customer == null ? null : customer.getCustomerName();
         }
 
         @Override
         protected void onPostExecute(String result) {
             if (TextUtils.isEmpty(result)) {
                 Toast.makeText(getApplicationContext(), R.string.txt_toast_get_message_failed,
                         Toast.LENGTH_LONG).show();
             }
 
             mTxtMaster.setText(getString(R.string.str_format_master_name, result));
         }
     }
 }
