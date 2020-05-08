 package com.jinheyu.lite_mms;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.view.ActionMode;
 import com.jinheyu.lite_mms.data_structures.Constants;
 import com.jinheyu.lite_mms.data_structures.Department;
 import com.jinheyu.lite_mms.data_structures.Team;
 import com.jinheyu.lite_mms.data_structures.WorkCommand;
 import com.jinheyu.lite_mms.netutils.BadRequest;
 import org.json.JSONException;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.HashMap;
 
 public class MenuItemWrapper {
     private Activity mActivity;
     private ActionMode mActionMode;
     private int checkedItemIndex;
 
     public MenuItemWrapper(Activity activity) {
         this.mActivity = activity;
     }
 
     public MenuItemWrapper(Activity activity, ActionMode mode) {
         this.mActivity = activity;
         this.mActionMode = mode;
     }
 
     public void carryForward(final int workCommandId) {
         newBuilder(mActivity.getString(R.string.confirm_carry_forward, workCommandId),
                 String.format("工单%d结转中", workCommandId),
                 new XProgressableRunnable.XRunnable() {
                     @Override
                     public Void run() throws Exception {
                         MyApp.getWebServieHandler().updateWorkCommand(workCommandId, Constants.ACT_CARRY_FORWARD, null);
                         return null;
                     }
                 },
                 mActivity.getString(R.string.carryForward_success, workCommandId)
         ).show();
     }
 
     public void carryForward(final int[] workCommandIds) {
         final String workCommands_str = Arrays.toString(workCommandIds);
 
         newBuilder(mActivity.getString(R.string.confirm_carry_forward, workCommands_str),
                 String.format("工单%s批量结转中", workCommands_str),
                 new XProgressableRunnable.XRunnable() {
                     @Override
                     public Void run() throws Exception {
                         for (int workCommandId : workCommandIds) {
                             MyApp.getWebServieHandler().updateWorkCommand(workCommandId, Constants.ACT_CARRY_FORWARD, null);
                         }
                         return null;
                     }
                 },
                 mActivity.getString(R.string.carryForward_success, workCommands_str)
         ).show();
 
     }
 
     public void carryForwardQuickly(final int workCommandId) {
         newBuilder(mActivity.getString(R.string.confirm_carry_forward_quickly, workCommandId),
                 String.format("工单%d快速结转中", workCommandId),
                 new XProgressableRunnable.XRunnable() {
                     @Override
                     public Void run() throws Exception {
                         MyApp.getWebServieHandler().updateWorkCommand(workCommandId, Constants.ACT_QUICK_CARRY_FORWARD, null);
                         return null;
                     }
                 },
                 mActivity.getString(R.string.quick_carryForward_success, workCommandId)
         ).show();
     }
 
     public void carryForwardQuickly(final int[] workCommandIds) {
         final String workCommandIds_str = Arrays.toString(workCommandIds);
 
         newBuilder(mActivity.getString(R.string.confirm_carry_forward_quickly, workCommandIds_str),
                 String.format("工单%s批量快速结转中", workCommandIds_str),
                 new XProgressableRunnable.XRunnable() {
                     @Override
                     public Void run() throws Exception {
                         for (int workCommandId : workCommandIds) {
                             MyApp.getWebServieHandler().updateWorkCommand(workCommandId, Constants.ACT_QUICK_CARRY_FORWARD, null);
                         }
                         return null;
                     }
                 },
                 mActivity.getString(R.string.quick_carryForward_success, workCommandIds_str)
         ).show();
     }
 
     public void confirm_retrieve(final int workCommandId) {
         newBuilder(mActivity.getString(R.string.confirm_retrieve_work_command, workCommandId), String.format("工单%d确认回收中", workCommandId), new XProgressableRunnable.XRunnable() {
             @Override
             public Void run() throws Exception {
                 //TODO
 //                MyApp.getWebServieHandler().updateWorkCommand(workCommandId, Constants.ACT_AFFIRM_RETRIEVAL, null);
                 return null;
             }
         }, mActivity.getString(R.string.confirm_retrieve_sucess, workCommandId)).show();
     }
 
     public void deny_retrieve(final int workCommandId) {
         newBuilder(mActivity.getString(R.string.refuse_retrieval, workCommandId),
                 String.format("工单%s拒绝回收中", workCommandId),
                 new XProgressableRunnable.XRunnable() {
                     @Override
                     public Void run() throws Exception {
                         MyApp.getWebServieHandler().updateWorkCommand(workCommandId, Constants.ACT_REFUSE_RETRIEVAL, null);
                         return null;
                     }
                 }, mActivity.getString(R.string.refuse_retrieval_success, workCommandId)).show();
     }
 
     public void deny_retrieve(final int[] workCommandIds) {
         final String workCommandIdsStr = Arrays.toString(workCommandIds);
         newBuilder(mActivity.getString(R.string.refuse_retrieval, workCommandIdsStr),
                 String.format("工单%s拒绝回收中", workCommandIdsStr),
                 new XProgressableRunnable.XRunnable() {
                     @Override
                     public Void run() throws Exception {
                         for (int workCommandId : workCommandIds) {
                             MyApp.getWebServieHandler().updateWorkCommand(workCommandId, Constants.ACT_REFUSE_RETRIEVAL, null);
                         }
                         return null;
                     }
                 }, mActivity.getString(R.string.refuse_retrieval_success, workCommandIdsStr)).show();
     }
 
     public void dispatch(final int workCommandId, final int departmentId) {
         final Department department = Department.getDepartmentById(departmentId);
 
         newBuilder(mActivity.getString(R.string.confirm_assign, workCommandId),
                 String.format("工单%s分配中", workCommandId),
                 new XProgressableRunnable.XRunnable() {
                     @Override
                     public Void run() throws Exception {
                         final Team team = department.getTeamList().get(checkedItemIndex);
                         MyApp.getWebServieHandler().updateWorkCommand(workCommandId, Constants.ACT_ASSIGN, new HashMap<String, String>() {{
                             put("team_id", String.valueOf(team.getId()));
                         }});
                         return null;
                     }
                 },
                 mActivity.getString(R.string.confirm_assign_success, workCommandId)
         ).setSingleChoiceItems(department.getTeamNames(), 0, new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 checkedItemIndex = which;
             }
         }
         ).show();
     }
 
     public void dispatch(final int[] workCommandIds) {
 
     }
 
     public void endWorkCommand(final int workCommandId) {
         newBuilder(mActivity.getString(R.string.confirm_end, workCommandId),
                 String.format("工单%d结束中", workCommandId),
                 new XProgressableRunnable.XRunnable() {
                     @Override
                     public Void run() throws Exception {
                         MyApp.getWebServieHandler().updateWorkCommand(workCommandId, Constants.ACT_END, null);
                         return null;
                     }
                 },
                 mActivity.getString(R.string.end_success, workCommandId)
         ).show();
     }
 
     public void endWorkCommand(final int[] checkedWorkCommandIds) {
         final String workCommandIdsStr = Arrays.toString(checkedWorkCommandIds);
         newBuilder(mActivity.getString(R.string.confirm_end, workCommandIdsStr),
                 String.format("工单%s批量结束中", workCommandIdsStr),
                 new XProgressableRunnable.XRunnable() {
                     @Override
                     public Void run() throws Exception {
                         for (int workCommandId : checkedWorkCommandIds) {
                             MyApp.getWebServieHandler().updateWorkCommand(workCommandId, Constants.ACT_END, null);
                         }
                         return null;
                     }
                 },
                 mActivity.getString(R.string.end_success, workCommandIdsStr)
         ).show();
     }
 
    public void refuse(final int[] workCommandIds) {
        final String workCommandIdsStr = Arrays.toString(workCommandIds);
        newBuilder(mActivity.getString(R.string.confirm_refuse, workCommandIdsStr),
                String.format("工单%s批量打回中", workCommandIds),
                new XProgressableRunnable.XRunnable() {
                    @Override
                    public Void run() throws Exception {
                        for (int workCommandId : workCommandIds) {
                            MyApp.getWebServieHandler().updateWorkCommand(workCommandId, Constants.ACT_REFUSE, null);
                        }
                        return null;
                    }
                },
                mActivity.getString(R.string.confirm_refuse_success, workCommandIdsStr)
        ).show();
     }
 
     public void refuse(final int workCommandId) {
         newBuilder(mActivity.getString(R.string.confirm_refuse, workCommandId),
                 String.format("工单%s打回中", workCommandId),
                 new XProgressableRunnable.XRunnable() {
                     @Override
                     public Void run() throws Exception {
                         MyApp.getWebServieHandler().updateWorkCommand(workCommandId, Constants.ACT_REFUSE, null);
                         return null;
                     }
                 },
                 mActivity.getString(R.string.confirm_refuse_success, workCommandId)
         ).show();
     }
 
     private AlertDialog.Builder newBuilder(final String titleString, final String startString, final XProgressableRunnable.XRunnable runnable, final String okString) {
         return new AlertDialog.Builder(mActivity)
                 .setTitle(titleString)
                 .setNegativeButton(android.R.string.cancel, null)
                 .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         XProgressableRunnable.Builder builder = new XProgressableRunnable.Builder(mActivity);
                         builder.msg(startString).run(runnable).after(new Runnable() {
                             @Override
                             public void run() {
                                 if (mActionMode == null) {
                                     mActivity.onNavigateUp();
                                 } else {
                                     mActionMode.finish();
                                 }
                             }
                         }).okMsg(okString).create().start();
                     }
                 });
     }
 }
