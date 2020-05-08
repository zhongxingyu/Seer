 package com.jinheyu.lite_mms;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentTransaction;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.TextView;
 import com.jinheyu.lite_mms.data_structures.Constants;
 import com.jinheyu.lite_mms.data_structures.User;
 import com.jinheyu.lite_mms.data_structures.WorkCommand;
 import com.jinheyu.lite_mms.netutils.BadRequest;
 import org.json.JSONException;
 
 import java.io.IOException;
 
 
 public class WorkCommandActivity extends FragmentActivity implements ImageFragment.ImageFragmentListener {
     private WorkCommand mWorkCommand;
 
     public static void startFromActivity(Activity activity, WorkCommand workCommand, boolean replace) {
         Intent intent = new Intent(activity, WorkCommandActivity.class);
         intent.putExtra("work_command", workCommand);
         if (replace) {
             activity.finish();
         }
         activity.startActivity(intent);
     }
 
     @Override
     public String getFragmentPicUrl() {
         return mWorkCommand.getPicPath();
     }
 
     @Override
     public Intent getParentActivityIntent() {
         return new Intent(this, MyApp.getCurrentUser().getDefaultActivity());
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         switch (MyApp.getCurrentUser().getGroupId()) {
             case User.TEAM_LEADER:
                 _setTeamLeaderMenu(menu);
                 break;
             case User.DEPARTMENT_LEADER:
                 _setDepartmentLeaderMenu(menu);
             default:
                 break;
         }
         return super.onCreateOptionsMenu(menu);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         MenuItemWrapper menuItemWrapper = new MenuItemWrapper(this);
         int workCommandId = mWorkCommand.getId();
         switch (item.getItemId()) {
             case R.id.quick_carryForward:
                 menuItemWrapper.carryForwardQuickly(workCommandId);
                 break;
             case R.id.carry_forward:
                 menuItemWrapper.carryForward(mWorkCommand);
                 break;
             case R.id.end_work_command:
                 menuItemWrapper.endWorkCommand(mWorkCommand);
                 break;
             case R.id.add_weight:
                 menuItemWrapper.addWeight(mWorkCommand);
                 break;
             case R.id.action_dispatch:
                 menuItemWrapper.dispatch(workCommandId, mWorkCommand.getDepartmentId());
                 break;
             case R.id.action_refuse:
                 menuItemWrapper.refuse(workCommandId);
                 break;
             case R.id.action_confirm_retrieve:
                menuItemWrapper.confirm_retrieve(mWorkCommand);
                 break;
             case R.id.action_deny_retrieve:
                menuItemWrapper.deny_retrieve(workCommandId);
                 break;
         }
         return super.onOptionsItemSelected(item);
     }
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_work_command_detail);
         getActionBar().setDisplayHomeAsUpEnabled(true);
         _initView();
     }
 
     private void _initView() {
         mWorkCommand = getIntent().getParcelableExtra("work_command");
         _setIdTextView();
         _setOrderNumber();
         _setCustomer();
         _setOrgWeight();
         _setOrgCnt();
         _setPic();
     }
 
     private void _setCustomer() {
         TextView textView = (TextView) findViewById(R.id.customer_name);
         textView.setText(mWorkCommand.getCustomerName());
     }
 
     private void _setOrderNumber() {
          TextView textView = (TextView) findViewById(R.id.order_number);
         textView.setText(String.valueOf(mWorkCommand.getOrderNumber()));
     }
 
     private void _setOrgWeight() {
         TextView textView = (TextView) findViewById(R.id.org_weight);
         textView.setText(mWorkCommand.getOrgWeight() + " 千克");
     }
 
     private void _setOrgCnt() {
         TextView textView = (TextView) findViewById(R.id.org_cnt);
         textView.setText(mWorkCommand.getOrgCnt() + " " + mWorkCommand.getUnit());
     }
 
     private void _setDepartmentLeaderMenu(Menu menu) {
         if (mWorkCommand.getStatus() != Constants.STATUS_LOCKED) {
             getMenuInflater().inflate(R.menu.department_leader_dispatch, menu);
         } else {
             getMenuInflater().inflate(R.menu.department_leader_locked, menu);
         }
     }
 
     private void _setIdTextView() {
         TextView idTextView = (TextView) findViewById(R.id.work_command_id);
         idTextView.setText(String.valueOf(mWorkCommand.getId()));
     }
 
     private void _setPic() {
         String url = mWorkCommand.getPicPath();
         getIntent().putExtra("picUrl", url);
         if (!Utils.isEmptyString(url)) {
             FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
             ImageFragment imageFragment = new ImageFragment();
             ft.add(R.id.image_row, imageFragment, "IMAGE");
             ft.commit();
         }
     }
 
     private void _setTeamLeaderMenu(Menu menu) {
         if (mWorkCommand.getStatus() == Constants.STATUS_ENDING) {
             getMenuInflater().inflate(R.menu.team_leader_work_command_menu, menu);
         }
     }
 }
 
 class GetWorkCommandTask extends AsyncTask<Integer, Void, WorkCommand> {
     private Exception ex;
     private boolean replace;
     private Activity mActivity;
 
     public GetWorkCommandTask(Activity activity) {
         this.mActivity = activity;
         this.replace = false;
     }
 
     public GetWorkCommandTask(Activity activity, boolean replace) {
         this.mActivity = activity;
         this.replace = replace;
     }
 
     @Override
     protected WorkCommand doInBackground(Integer... params) {
         try {
             return MyApp.getWebServieHandler().getWorkCommand(params[0]);
         } catch (BadRequest badRequest) {
             badRequest.printStackTrace();
             ex = badRequest;
         } catch (IOException e) {
             e.printStackTrace();
             ex = e;
         } catch (JSONException e) {
             e.printStackTrace();
             ex = e;
         }
         return null;
     }
 
     @Override
     protected void onPostExecute(WorkCommand workCommand) {
         if (ex == null) {
             WorkCommandActivity.startFromActivity(mActivity, workCommand, replace);
         }else{
             Utils.displayError(mActivity, ex);
         }
     }
 }
