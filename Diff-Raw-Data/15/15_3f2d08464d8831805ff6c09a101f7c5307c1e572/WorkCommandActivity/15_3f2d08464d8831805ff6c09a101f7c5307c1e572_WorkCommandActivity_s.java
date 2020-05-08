 package com.jinheyu.lite_mms;
 
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.*;
 import android.view.*;
 import android.widget.*;
 import com.jinheyu.lite_mms.data_structures.Constants;
 import com.jinheyu.lite_mms.data_structures.User;
 import com.jinheyu.lite_mms.data_structures.WorkCommand;
 import com.jinheyu.lite_mms.netutils.BadRequest;
 import org.json.JSONException;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 
 public class WorkCommandActivity extends FragmentActivity implements UpdateWorkCommand {
 
     @Override
     public void beforeUpdateWorkCommand() {
 
     }
 
     @Override
     public Intent getParentActivityIntent() {
         return new Intent(this, MyApp.getCurrentUser().getDefaultActivity());
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         int currentStatus = getIntent().getIntExtra("symbol", 0);
         MenuInflater inflater = getMenuInflater();
         switch (MyApp.getCurrentUser().getGroupId()) {
             case User.TEAM_LEADER:
                 _setTeamLeaderMenu(menu, inflater, currentStatus);

                 break;
             case User.DEPARTMENT_LEADER:
                 _setDepartmentLeaderMenu(menu, inflater, currentStatus);

             default:
                 break;
         }
         return super.onCreateOptionsMenu(menu);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         MenuItemWrapper menuItemWrapper = new MenuItemWrapper(this);
         WorkCommand workCommand = getWorkCommandFragment().getWorkCommand();
         switch (item.getItemId()) {
             case R.id.quick_carryForward:
                 menuItemWrapper.carryForwardQuickly(workCommand);
                 break;
             case R.id.carry_forward:
                 menuItemWrapper.carryForward(workCommand);
                 break;
             case R.id.end_work_command:
                 menuItemWrapper.endWorkCommand(workCommand);
                 break;
             case R.id.add_weight:
                 menuItemWrapper.addWeight(workCommand);
                 break;
             case R.id.action_dispatch:
                 menuItemWrapper.dispatch(workCommand);
                 break;
             case R.id.action_refuse:
                 menuItemWrapper.refuse(workCommand);
                 break;
             case R.id.action_confirm_retrieve:
                 menuItemWrapper.confirmRetrieve(workCommand);
                 break;
             case R.id.action_deny_retrieve:
                 menuItemWrapper.denyRetrieve(workCommand);
                 break;
         }
         return super.onOptionsItemSelected(item);
     }
 
     @Override
     public void updateWorkCommand(WorkCommand workCommand) {
         WorkCommandFragment fragment = getWorkCommandFragment();
         fragment.updateWorkCommand(workCommand);
     }
 
     @Override
     public void updateWorkCommandFailed(Exception ex) {
         Toast.makeText(this, "加载工单失败", Toast.LENGTH_SHORT).show();
     }
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_work_command_detail);
         getActionBar().setDisplayHomeAsUpEnabled(true);
         updateView();
         int workCommandId = getIntent().getIntExtra("workCommandId", 0);
         new GetWorkCommandAsyncTask(this).execute(workCommandId);
     }
 
     private void _setDepartmentLeaderMenu(Menu menu, MenuInflater inflater, int currentStatus) {
         if (currentStatus != Constants.STATUS_LOCKED) {
             inflater.inflate(R.menu.department_leader_dispatch, menu);
         } else {
             inflater.inflate(R.menu.department_leader_locked, menu);
         }
     }
 
     private void _setTeamLeaderMenu(Menu menu, MenuInflater inflater, int currentStatus) {
         if (currentStatus == Constants.STATUS_ENDING) {
             inflater.inflate(R.menu.team_leader_work_command_menu, menu);
         }
     }
 
     private WorkCommandFragment getWorkCommandFragment() {
         List<Fragment> fragments = getSupportFragmentManager().getFragments();
         return (WorkCommandFragment) fragments.get(0);
     }
 
     private void updateView() {
         FragmentManager fragmentManager = getSupportFragmentManager();
         FragmentTransaction transaction = fragmentManager.beginTransaction();
         transaction.replace(R.id.fragment, new WorkCommandFragment());
         transaction.commit();
     }
 
 }
