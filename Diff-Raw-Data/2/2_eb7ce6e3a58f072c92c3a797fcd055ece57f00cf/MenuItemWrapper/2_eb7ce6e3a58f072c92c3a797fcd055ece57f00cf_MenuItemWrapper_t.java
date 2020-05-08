 package com.jinheyu.lite_mms;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.view.ActionMode;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 import com.jinheyu.lite_mms.data_structures.Constants;
 import com.jinheyu.lite_mms.data_structures.Department;
 import com.jinheyu.lite_mms.data_structures.Team;
 import com.jinheyu.lite_mms.data_structures.WorkCommand;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 
 
 public class MenuItemWrapper {
     private Activity mActivity;
     private ActionMode mActionMode;
     private int checkedItemIndex;
     private EditText weightEditText;
     private EditText cntEditText;
 
     public MenuItemWrapper(Activity activity) {
         this.mActivity = activity;
     }
 
     public MenuItemWrapper(Activity activity, ActionMode mode) {
         this.mActivity = activity;
         this.mActionMode = mode;
     }
 
     public void addWeight(final WorkCommand workCommand) {
         new AlertDialog.Builder(mActivity).setTitle(R.string.add_weight
         ).setView(getAddWeightView(workCommand)
         ).setNeutralButton(R.string.part_weight, new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 _addWeightToWorkCommand(false, workCommand);
             }
         }
         ).setNegativeButton(android.R.string.cancel, null
         ).setPositiveButton(R.string.completely_weight, new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 _addWeightToWorkCommand(true, workCommand);
             }
         }
         ).show();
         weightEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
             @Override
             public void onFocusChange(View v, boolean hasFocus) {
                 if (hasFocus) {
                     showKeyBoard(v);
                 }
             }
         });
     }
 
     private void showKeyBoard(final View v) {
         Runnable runnable = new Runnable() {
             @Override
             public void run() {
                 InputMethodManager inputMethodManager = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                 inputMethodManager.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
             }
         };
         v.postDelayed(runnable, 500);
     }
 
     public void carryForward(final WorkCommand workCommand) {
         final int workCommandId = workCommand.getId();
 
         newDialogBuilder(mActivity.getString(R.string.confirm_carry_forward, workCommandId),
                 String.format("工单%d结转中", workCommandId),
                 new XProgressableRunnable.XRunnable() {
                     @Override
                     public Void run() throws Exception {
                         MyApp.getWebServieHandler().updateWorkCommand(workCommandId, Constants.ACT_CARRY_FORWARD, null);
                         return null;
                     }
                 },
                 mActivity.getString(R.string.carryForward_success, workCommandId)
         ).setView(getWorkCommandProcessedView(workCommand)).show();
     }
 
     public void carryForward(final List<WorkCommand> workCommandList) {
         StringBuilder stringBuilder = new StringBuilder();
         boolean first = true;
         for (WorkCommand workCommand : workCommandList) {
             stringBuilder.append(first ? "" : " ,");
             stringBuilder.append(workCommand.getId());
             first = false;
         }
         final String workCommandsStr = stringBuilder.toString();
 
         newDialogBuilder(mActivity.getString(R.string.confirm_carry_forward, workCommandsStr),
                 String.format("工单%s批量结转中", workCommandsStr),
                 new XProgressableRunnable.XRunnable() {
                     @Override
                     public Void run() throws Exception {
                         for (WorkCommand workCommand : workCommandList) {
                             MyApp.getWebServieHandler().updateWorkCommand(workCommand.getId(), Constants.ACT_CARRY_FORWARD, null);
                         }
                         return null;
                     }
                 },
                 mActivity.getString(R.string.carryForward_success, workCommandsStr)
         ).show();
 
     }
 
     public void carryForwardQuickly(final WorkCommand workCommand) {
         if (_checkWeightAndCntValue(workCommand, 0, 0)) {
             final int workCommandId = workCommand.getId();
             newDialogBuilder(mActivity.getString(R.string.confirm_carry_forward_quickly, workCommandId),
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
     }
 
     public void carryForwardQuickly(final List<WorkCommand> workCommandList) {
 
         StringBuilder stringBuilder = new StringBuilder();
         boolean first = true;
         for (WorkCommand workCommand : workCommandList) {
             if (_checkWeightAndCntValue(workCommand, 0, 0)) {
                 stringBuilder.append(first ? "" : " ,");
                 stringBuilder.append(workCommand.getId());
                 first = false;
             } else {
                 return;
             }
         }
         final String workCommandIds_str = stringBuilder.toString();
 
         newDialogBuilder(mActivity.getString(R.string.confirm_carry_forward_quickly, workCommandIds_str),
                 String.format("工单%s批量快速结转中", workCommandIds_str),
                 new XProgressableRunnable.XRunnable() {
                     @Override
                     public Void run() throws Exception {
                         for (WorkCommand workCommand : workCommandList) {
                             MyApp.getWebServieHandler().updateWorkCommand(workCommand.getId(), Constants.ACT_QUICK_CARRY_FORWARD, null);
                         }
                         return null;
                     }
                 },
                 mActivity.getString(R.string.quick_carryForward_success, workCommandIds_str)
         ).show();
     }
 
     public void confirmRetrieve(final WorkCommand workCommand) {
         newDialogBuilder(mActivity.getString(R.string.confirm_retrieve),
                 String.format("工单%d确认回收中", workCommand.getId()),
                 new XProgressableRunnable.XRunnable() {
                     @Override
                     public Void run() throws Exception {
                         int weight = Integer.parseInt(weightEditText.getText().toString());
                         int cnt = Integer.parseInt(cntEditText.getText().toString());
                         HashMap<String, String> params = new HashMap<String, String>();
                         params.put("weight", String.valueOf(weight));
                         if (!workCommand.measured_by_weight()) {
                             params.put("quantity", String.valueOf(cnt));
                         }
                         MyApp.getWebServieHandler().updateWorkCommand(workCommand.getId(), Constants.ACT_AFFIRM_RETRIEVAL, params);
                         return null;
                     }
                 },
                 mActivity.getString(R.string.confirm_retrieve_sucess, workCommand.getId())
         ).setView(getConfirmRetrieveView(workCommand)).show();
         weightEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
             @Override
             public void onFocusChange(View v, boolean hasFocus) {
                 if (hasFocus) {
                     showKeyBoard(v);
                 }
             }
         });
     }
 
     public void denyRetrieve(final WorkCommand workCommand) {
         final int workCommandId = workCommand.getId();
         newDialogBuilder(mActivity.getString(R.string.refuse_retrieval, workCommandId),
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
         newDialogBuilder(mActivity.getString(R.string.refuse_retrieval, workCommandIdsStr),
                 String.format("工单%s批量拒绝回收中", workCommandIdsStr),
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
 
     public void dispatch(final WorkCommand workCommand) {
         final int workCommandId = workCommand.getId();
         final Department department = Department.getDepartmentById(workCommand.getDepartmentId());
 
         newDialogBuilder(mActivity.getString(R.string.confirm_assign, workCommandId),
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
 
     public void dispatch(final int[] workCommandIds, final int departmentId) {
         final Department department = Department.getDepartmentById(departmentId);
         final String workCommandIdsStr = Arrays.toString(workCommandIds);
         newDialogBuilder(mActivity.getString(R.string.confirm_assign, workCommandIdsStr),
                 String.format("工单%s批量分配中", workCommandIdsStr),
                 new XProgressableRunnable.XRunnable() {
                     @Override
                     public Void run() throws Exception {
                         final Team team = department.getTeamList().get(checkedItemIndex);
                         final String teamId = String.valueOf(team.getId());
                         for (int workCommandId : workCommandIds) {
                             MyApp.getWebServieHandler().updateWorkCommand(workCommandId, Constants.ACT_ASSIGN, new HashMap<String, String>() {{
                                 put("team_id", teamId);
                             }});
                         }
                         return null;
                     }
                 },
                 mActivity.getString(R.string.confirm_assign_success, workCommandIdsStr)
         ).setSingleChoiceItems(department.getTeamNames(), 0, new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 checkedItemIndex = which;
             }
         }
         ).show();
     }
 
     public void endWorkCommand(final WorkCommand workCommand) {
         if (_checkWeightAndCntValue(workCommand, 0, 0)) {
             final int workCommandId = workCommand.getId();
             newDialogBuilder(mActivity.getString(R.string.confirm_end, workCommandId),
                     String.format("工单%d结束中", workCommandId),
                     new XProgressableRunnable.XRunnable() {
                         @Override
                         public Void run() throws Exception {
                             MyApp.getWebServieHandler().updateWorkCommand(workCommandId, Constants.ACT_END, null);
                             return null;
                         }
                     },
                     mActivity.getString(R.string.end_success, workCommandId)
             ).setView(getWorkCommandProcessedView(workCommand)).show();
         }
     }
 
     public void endWorkCommand(final List<WorkCommand> workCommandList) {
 
         StringBuilder stringBuilder = new StringBuilder();
         boolean first = true;
         for (WorkCommand workCommand : workCommandList) {
             if (!_checkWeightAndCntValue(workCommand, 0, 0)) {
                 return;
             } else {
                 stringBuilder.append(first ? "" : " ,");
                 stringBuilder.append(workCommand.getId());
                 first = false;
             }
         }
         final String workCommandIdsStr = stringBuilder.toString();
         newDialogBuilder(mActivity.getString(R.string.confirm_end, workCommandIdsStr),
                 String.format("工单%s批量结束中", workCommandIdsStr),
                 new XProgressableRunnable.XRunnable() {
                     @Override
                     public Void run() throws Exception {
                         for (WorkCommand workCommand : workCommandList) {
                             MyApp.getWebServieHandler().updateWorkCommand(workCommand.getId(), Constants.ACT_END, null);
                         }
                         return null;
                     }
                 },
                 mActivity.getString(R.string.end_success, workCommandIdsStr)
         ).show();
     }
 
     public void refuse(final int[] workCommandIds) {
         final String workCommandIdsStr = Arrays.toString(workCommandIds);
         newDialogBuilder(mActivity.getString(R.string.confirm_refuse, workCommandIdsStr),
                 String.format("工单%s批量打回中", workCommandIdsStr),
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
 
     public void refuse(final WorkCommand workCommand) {
         final int workCommandId = workCommand.getId();
         newDialogBuilder(mActivity.getString(R.string.confirm_refuse, workCommandId),
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
 
     public void retrieveQualityInspection(final WorkCommand workCommand) {
         final int workCommandId = workCommand.getId();
         newDialogBuilder(mActivity.getString(R.string.confirm_retrieve_qi, workCommandId),
                 String.format("工单%d质检结果打回中", workCommandId),
                 new XProgressableRunnable.XRunnable() {
                     @Override
                     public Void run() throws Exception {
                         MyApp.getWebServieHandler().updateWorkCommand(workCommandId, Constants.ACT_RETRIVE_QI, null);
                         return null;
                     }
                 },
                 mActivity.getString(R.string.confirm_retrieve_qi_success, workCommandId)
         ).show();
     }
 
     private void _addWeightToServer(final boolean isFinished, final WorkCommand workCommand, final int weight, final int cnt) {
         XProgressableRunnable.Builder builder = new XProgressableRunnable.Builder(mActivity);
         builder.msg(mActivity.getString(R.string.add_work_command_weight));
         builder.run(new XProgressableRunnable.XRunnable() {
             @Override
             public Void run() throws Exception {
                 HashMap<String, String> params = new HashMap<String, String>();
                 params.put("weight", String.valueOf(weight));
                 params.put("is_finished", isFinished ? "1" : "0");
                 if (!workCommand.measured_by_weight()) {
                     params.put("quantity", String.valueOf(cnt));
                 }
                 MyApp.getWebServieHandler().updateWorkCommand(workCommand.getId(), Constants.ACT_ADD_WEIGHT, params);
                 return null;
             }
         });
         builder.after(new Runnable() {
             @Override
             public void run() {
                 if (isFinished) {
                     mActivity.onNavigateUp();
                 } else {
                     if (mActivity instanceof WorkCommandActivity) {
                         new GetWorkCommandAsyncTask((WorkCommandActivity) mActivity).execute(workCommand.getId());
                     }
                 }
             }
         });
         builder.okMsg(mActivity.getString(R.string.add_work_command_weight_success));
         builder.create().start();
     }
 
     private void _addWeightToWorkCommand(final boolean isFinished, final WorkCommand workCommand) {
         final int weight = Utils.parseInt(weightEditText.getText().toString(), 0);
         final int cnt = Utils.parseInt(cntEditText.getText().toString(), 0);
         if (_checkWeightAndCntValue(workCommand, weight, cnt)) {
             if (isFinished) {
                 String alertMessage = null;
                 if (weight + workCommand.getProcessedWeight() < workCommand.getOrgWeight()) {
                     alertMessage = mActivity.getString(R.string.previous_weight_greater);
                 }
                 if (!workCommand.measured_by_weight() && (cnt + workCommand.getProcessedCnt() < workCommand.getOrgCnt())) {
                     alertMessage = mActivity.getString(R.string.previous_count_greater);
                 }
                 if (!Utils.isEmptyString(alertMessage)) {
                     AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                     builder.setMessage(alertMessage).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialog, int which) {
                             _addWeightToServer(true, workCommand, weight, cnt);
                         }
                     }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialog, int which) {
                             dialog.dismiss();
                         }
                     });
                     builder.show();
                     return;
                 }
             }
             _addWeightToServer(isFinished, workCommand, weight, cnt);
         }
     }
 
     private boolean _checkWeightAndCntValue(WorkCommand workCommand, int weight, int cnt) {
         int currentWeight = weight + workCommand.getProcessedWeight();
         int currentCnt = cnt + workCommand.getProcessedCnt();
         if (currentWeight <= 0) {
             Toast.makeText(mActivity, mActivity.getString(R.string.invalid_weight_data, workCommand.getId()), Toast.LENGTH_SHORT).show();
             return false;
         }
         if (!workCommand.measured_by_weight() && currentCnt <= 0) {
             Toast.makeText(mActivity, mActivity.getString(R.string.invalid_cnt_data, workCommand.getId()), Toast.LENGTH_SHORT).show();
             return false;
         }
         if (workCommand.getOrgCnt() * Utils.getMaxTimes(mActivity) <= currentCnt) {
             Toast.makeText(mActivity, R.string.too_large_data, Toast.LENGTH_SHORT).show();
             return false;
         }
         return true;
     }
 
     private View getAddWeightView(WorkCommand workCommand) {
         LayoutInflater inflater = mActivity.getLayoutInflater();
         View rootView = inflater.inflate(R.layout.fragement_add_weight, null);
 
         TextView weightText = (TextView) rootView.findViewById(R.id.dialog_add_current_weight);
         weightText.setText(String.format("%d 公斤", workCommand.getProcessedWeight()));
         TextView cntText = (TextView) rootView.findViewById(R.id.dialog_add_current_cnt);
         cntText.setText(String.format("%d %s", workCommand.getProcessedCnt(), workCommand.getUnit()));
 
         weightEditText = (EditText) rootView.findViewById(R.id.dialog_add_edittext_weight);
         cntEditText = (EditText) rootView.findViewById(R.id.dialog_add_edittext_count);
         if (workCommand.measured_by_weight()) {
             rootView.findViewById(R.id.count_row).setVisibility(View.GONE);
             rootView.findViewById(R.id.processed_cnt_row).setVisibility(View.GONE);
         } else {
             rootView.findViewById(R.id.count_row).setVisibility(View.VISIBLE);
             rootView.findViewById(R.id.processed_cnt_row).setVisibility(View.VISIBLE);
         }
         return rootView;
     }
 
     private View getConfirmRetrieveView(WorkCommand workCommand) {
         LayoutInflater inflater = mActivity.getLayoutInflater();
         View view = inflater.inflate(R.layout.fragment_confirm_retrieve, null);
         weightEditText = (EditText) view.findViewById(R.id.dialog_confirm_processed_weight);
         weightEditText.setText(String.valueOf(workCommand.getProcessedWeight()));
         cntEditText = (EditText) view.findViewById(R.id.dialog_confirm_processed_quantity);
         cntEditText.setText(String.valueOf(workCommand.getProcessedCnt()));
         TextView cntView = (TextView) view.findViewById(R.id.dialog_confirm_label_quantity);
         cntView.setText(mActivity.getString(R.string.confirm_processed_quantity, workCommand.getUnit()));
 
         if (workCommand.measured_by_weight()) {
             view.findViewById(R.id.count_row).setVisibility(View.GONE);
         } else {
             view.findViewById(R.id.count_row).setVisibility(View.VISIBLE);
         }
         return view;
     }
 
     private View getWorkCommandProcessedView(final WorkCommand workCommand) {
         LayoutInflater inflater = mActivity.getLayoutInflater();
         View rootView = inflater.inflate(R.layout.fragment_work_command_proccessed, null);
 
         TextView orgWeightView = (TextView) rootView.findViewById(R.id.org_weight);
         orgWeightView.setText(String.format("%d 公斤", workCommand.getOrgWeight()));
 
         TextView orgCntView = (TextView) rootView.findViewById(R.id.org_cnt);
         orgCntView.setText(String.format("%d %s", workCommand.getOrgCnt(), workCommand.getUnit()));
 
        TextView processedWeightView = (TextView) rootView.findViewById(R.id.processed_weight);
         processedWeightView.setText(String.format("%d 公斤", workCommand.getProcessedWeight()));
 
         TextView processedCntView = (TextView) rootView.findViewById(R.id.processed_cnt);
         processedCntView.setText(String.format("%d %s", workCommand.getProcessedCnt(), workCommand.getUnit()));
 
         return rootView;
     }
 
     private AlertDialog.Builder newDialogBuilder(final String titleString, final String startString, final XProgressableRunnable.XRunnable runnable, final String okString) {
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
