 package com.telecom.navigation;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.telecom.util.HttpUtil;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.RatingBar;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class EvaluateActivity extends BaseActivity {
 
     private View mLinearBottom;
     private RatingBar mRatingBar;
     private TextView mTxtTitle;
     private Button btn_back_to_main;
     private View mLinearRating;
 
     private SimpleDateFormat mDateFormat;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.evaluate_activity);
 
         mRatingBar = (RatingBar) findViewById(R.id.ratingbar);
         mLinearRating = findViewById(R.id.linear_rating);
         mLinearBottom = findViewById(R.id.linear_bottom);
         mTxtTitle = (TextView) findViewById(R.id.txt_title);
         btn_back_to_main = (Button) findViewById(R.id.btn_back_to_main);
 
         SharedPreferences settings = getSharedPreferences(
                 AdvertisementActivity.EXTRA_KEY_SHARE_PREF, Activity.MODE_PRIVATE);
         boolean isFirstUse = settings.getBoolean(AdvertisementActivity.EXTRA_KEY_SHARE_FIRST, true);
 
         if (!isFirstUse) {
             mTxtTitle.setVisibility(View.GONE);
             mLinearRating.setVisibility(View.GONE);
             btn_back_to_main.setVisibility(View.GONE);
             mLinearBottom.setVisibility(View.VISIBLE);
         }
 
         mDateFormat = new SimpleDateFormat("yyyy-MM-dd%HH:mm:ss");
     }
 
     public void onBtnSubmitClick(View view) {
         Toast.makeText(getApplicationContext(), R.string.msg_toast_submit, Toast.LENGTH_LONG)
                 .show();
         mEndTime = new Date();
 
         List<BasicNameValuePair> paramsReport = new ArrayList<BasicNameValuePair>();
         paramsReport.add(new BasicNameValuePair("opt", "report"));
         paramsReport.add(new BasicNameValuePair("prod_id", mProId));
         paramsReport.add(new BasicNameValuePair("imsi", mIMSI));
         paramsReport
                 .add(new BasicNameValuePair("train_start_time", mDateFormat.format(mStartTime)));
         paramsReport.add(new BasicNameValuePair("train_end_time", mDateFormat.format(mEndTime)));
         paramsReport.add(new BasicNameValuePair("user_id", mUserId));
         paramsReport.add(new BasicNameValuePair("user_phone", mUserPhone));
        paramsReport.add(new BasicNameValuePair("app_list", mAppList == null ? "0" : mAppList
                .substring(0, mAppList.length() - 1)));
         paramsReport
                 .add(new BasicNameValuePair("service_level", "" + (int) mRatingBar.getRating()));
 
         // List<BasicNameValuePair> paramsStudy = new
         // ArrayList<BasicNameValuePair>();
         // paramsStudy.add(new BasicNameValuePair("opt", "study"));
         // paramsStudy.add(new BasicNameValuePair("prod_id", mProId));
         // paramsStudy.add(new BasicNameValuePair("imsi", mIMSI));
         // paramsStudy.add(new BasicNameValuePair("train_start_time",
         // mDateFormat.format(mStartTime)));
         // paramsStudy.add(new BasicNameValuePair("train_end_time",
         // mDateFormat.format(mEndTime)));
 
         new EvaluateTask(paramsReport).execute();
     }
 
     private class EvaluateTask extends AsyncTask<Void, Void, Boolean> {
 
         private List<BasicNameValuePair> mParamsReport;
         private List<BasicNameValuePair> mParamsStudy;
 
         public EvaluateTask(List<BasicNameValuePair> paramsReport) {
             mParamsReport = paramsReport;
         }
 
         @Override
         protected Boolean doInBackground(Void... params) {
             Log.d("Evaluate", "post report");
             String resultReport = HttpUtil.doGet("http://118.121.17.250/bass/AppRequest",
                     mParamsReport);
             // String resultStudy =
             // HttpUtil.doGet("http://118.121.17.250/bass/AppRequest",
             // mParamsStudy);
             if (!TextUtils.isEmpty(resultReport)) {
                 try {
                     JSONObject object = new JSONObject(resultReport);
                     if ("Success".equals(object.getString("Result"))) {
                         return true;
                     }
                 } catch (JSONException e) {
                    // TODO Auto-generated catch block
                     e.printStackTrace();
                 }
             }
             return false;
         }
 
         @Override
         protected void onPostExecute(Boolean result) {
             if (result) {
                 mLinearRating.setVisibility(View.GONE);
                 mLinearBottom.setVisibility(View.VISIBLE);
                 mTxtTitle.setText(R.string.txt_thanks_evaluate);
 
                 SharedPreferences settings = getSharedPreferences(
                         AdvertisementActivity.EXTRA_KEY_SHARE_PREF, Activity.MODE_PRIVATE);
                 Editor editor = settings.edit();
                 editor.putBoolean(AdvertisementActivity.EXTRA_KEY_SHARE_FIRST, false);
                 editor.commit();
             } else {
                 Toast.makeText(getApplicationContext(), R.string.msg_submit_error,
                         Toast.LENGTH_LONG).show();
             }
         }
     }
 
     public void onBtnExitClick(View view) {
         Intent intent = new Intent(Intent.ACTION_MAIN);
         intent.addCategory(Intent.CATEGORY_HOME);
         intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
         startActivity(intent);
         android.os.Process.killProcess(android.os.Process.myPid());
     }
 
     public void onBtbBackToMainClick(View view) {
         Intent intent = new Intent(this, AppliactionCategoryActivity.class);
         intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
         startActivity(intent);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         return true;
     }
 
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
         return true;
     }
 
     @Override
     public boolean onMenuItemSelected(int featureId, MenuItem item) {
         return true;
     }
 }
