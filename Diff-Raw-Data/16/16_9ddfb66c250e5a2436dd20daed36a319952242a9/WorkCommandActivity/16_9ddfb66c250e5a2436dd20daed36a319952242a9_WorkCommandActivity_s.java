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
 
 
 class GetWorkCommandTask extends AsyncTask<Void, Void, Void> {
     private WorkCommandActivity mActivity;
     private Exception ex;
 
     public GetWorkCommandTask(WorkCommandActivity activity) {
         this.mActivity = activity;
     }
 
     @Override
     protected Void doInBackground(Void... params) {
         try {
             int workCommandId = mActivity.getIntent().getIntExtra("workCommandId", 0);
             WorkCommand workCommand = MyApp.getWebServieHandler().getWorkCommand(workCommandId);
             mActivity.setWorkCommand(workCommand);
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
     protected void onPostExecute(Void param) {
         if (ex == null) {
             mActivity.updateView();
         } else {
             Toast.makeText(mActivity, "加载工单失败", Toast.LENGTH_SHORT).show();
         }
 
     }
 }
 
 public class WorkCommandActivity extends FragmentActivity {
     private WorkCommand mWorkCommand;
 
     @Override
     public Intent getParentActivityIntent() {
         return new Intent(this, MyApp.getCurrentUser().getDefaultActivity());
     }
 
     public void setWorkCommand(WorkCommand workCommand) {
         this.mWorkCommand = workCommand;
     }
 
     public void updateView() {
         FragmentManager fragmentManager = getSupportFragmentManager();
         FragmentTransaction transaction = fragmentManager.beginTransaction();
         transaction.replace(R.id.fragment, new WorkCommandFragment());
         transaction.commit();
     }
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_work_command_detail);
         getActionBar().setDisplayHomeAsUpEnabled(true);
         new GetWorkCommandTask(this).execute();
     }
 
     class WorkCommandFragment extends Fragment {
         private View rootView;
 
         @Override
         public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
             switch (MyApp.getCurrentUser().getGroupId()) {
                 case User.TEAM_LEADER:
                     _setTeamLeaderMenu(menu, inflater);
                     break;
                 case User.DEPARTMENT_LEADER:
                     _setDepartmentLeaderMenu(menu, inflater);
                 default:
                     break;
             }
             super.onCreateOptionsMenu(menu, inflater);
         }
 
         @Override
         public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
             rootView = inflater.inflate(R.layout.fragment_work_command_detail, null);
             setHasOptionsMenu(true);
             _initView();
             return rootView;
         }
 
         @Override
         public boolean onOptionsItemSelected(MenuItem item) {
             MenuItemWrapper menuItemWrapper = new MenuItemWrapper(getActivity());
             switch (item.getItemId()) {
                 case R.id.quick_carryForward:
                     menuItemWrapper.carryForwardQuickly(mWorkCommand);
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
                     menuItemWrapper.dispatch(mWorkCommand);
                     break;
                 case R.id.action_refuse:
                     menuItemWrapper.refuse(mWorkCommand);
                     break;
                 case R.id.action_confirm_retrieve:
                     menuItemWrapper.confirmRetrieve(mWorkCommand);
                     break;
                 case R.id.action_deny_retrieve:
                     menuItemWrapper.denyRetrieve(mWorkCommand);
                     break;
             }
             return super.onOptionsItemSelected(item);
         }
 
         private void _initView() {
             _setIdTextViewAndStatus();
             _setExtra();
             _setHandleType();
             _setOrderNumber();
             _setCustomer();
             _setProductAndSpecType();
             _setTechReq();
             _setProcedureAndPreviousProcedure();
             _setOrgWeightAndCnt();
             _setProcessedWeightAndCnt();
             _setPic();
             _setBackgroundColor();
         }
 
         private void _setBackgroundColor() {
             TableLayout layout = (TableLayout) rootView.findViewById(R.id.layout);
             boolean odd = true;
             for (int i = 0; i < layout.getChildCount(); i++) {
                 View row = layout.getChildAt(i);
                 if (row instanceof TableRow && row.getVisibility() == View.VISIBLE) {
                     row.setBackgroundColor(Color.parseColor(odd ? "#F9F9F9" : "#FFFFFF"));
                     odd = !odd;
                 }
             }
         }
 
         private void _setCustomer() {
             TextView textView = (TextView) rootView.findViewById(R.id.customer_name);
             textView.setText(mWorkCommand.getCustomerName());
         }
 
         private void _setDepartmentLeaderMenu(Menu menu, MenuInflater inflater) {
 
             if (mWorkCommand.getStatus() != Constants.STATUS_LOCKED) {
                 inflater.inflate(R.menu.department_leader_dispatch, menu);
             } else {
                 inflater.inflate(R.menu.department_leader_locked, menu);
             }
         }
 
         private void _setExtra() {
             List<String> extraMessages = new ArrayList<String>();
             if (mWorkCommand.isUrgent()) {
                 extraMessages.add("加急");
             }
             if (mWorkCommand.isRejected()) {
                 extraMessages.add("退镀");
             }
             TextView textView = (TextView) rootView.findViewById(R.id.extra);
             View extraView = rootView.findViewById(R.id.extra_row);
             if (extraMessages.isEmpty()) {
                 extraView.setVisibility(View.GONE);
             } else {
                 extraView.setVisibility(View.VISIBLE);
                 textView.setText(Utils.join(extraMessages, " ,"));
             }
         }
 
         private void _setHandleType() {
             TextView textView = (TextView) rootView.findViewById(R.id.handleType);
             textView.setText(mWorkCommand.getHandleTypeString());
         }
 
         private void _setIdTextViewAndStatus() {
             TextView idTextView = (TextView) rootView.findViewById(R.id.work_command_id);
             idTextView.setText(String.format("%d(%s)", mWorkCommand.getId(), mWorkCommand.getStatusString()));
         }
 
         private void _setOrderNumber() {
             TextView textView = (TextView) rootView.findViewById(R.id.order_number);
             textView.setText(String.valueOf(mWorkCommand.getOrderNumber()));
         }
 
         private void _setOrgWeightAndCnt() {
             TextView textView = (TextView) rootView.findViewById(R.id.org_weight_and_cnt);
             TextView weightAndCntView = (TextView) rootView.findViewById(R.id.org_weight_and_cnt_view);
             if (mWorkCommand.measured_by_weight()) {
                 weightAndCntView.setText(R.string.org_weight);
                 textView.setText(String.format("%d 千克", mWorkCommand.getOrgWeight()));
             } else {
                 weightAndCntView.setText(R.string.org_weight_and_cnt);
                 textView.setText(String.format("%d千克/%d%s", mWorkCommand.getOrgWeight(), mWorkCommand.getOrgCnt(), mWorkCommand.getUnit()));
             }
         }
 
         private void _setPic() {
             ImageButton imageButton = (ImageButton) rootView.findViewById(R.id.image);
             new GetImageTask(imageButton, mWorkCommand.getPicPath()).execute();
         }
 
         private void _setProcedureAndPreviousProcedure() {
             TextView textView = (TextView) rootView.findViewById(R.id.procedure_and_previous);
             textView.setText(String.format("%s(%s)", mWorkCommand.getProcedure(),
                    Utils.isEmptyString(mWorkCommand.getPreviousProcedure()) ? "\"\"" : mWorkCommand.getPreviousProcedure()));
         }
 
         private void _setProcessedWeightAndCnt() {
             TextView textView = (TextView) rootView.findViewById(R.id.processed_weight_and_cnt);
             TextView weightAndCntView = (TextView) rootView.findViewById(R.id.processed_weight_and_cnt_row);
             if (mWorkCommand.measured_by_weight()) {
                 weightAndCntView.setText(R.string.processed_weight);
                 textView.setText(String.format("%d千克", mWorkCommand.getProcessedWeight()));
             } else {
                 weightAndCntView.setText(R.string.processed_weight_and_cnt);
                 textView.setText(String.format("%d千克/%d%s", mWorkCommand.getProcessedWeight(), mWorkCommand.getProcessedCnt(), mWorkCommand.getUnit()));
             }
         }
 
         private void _setProductAndSpecType() {
             TextView textView = (TextView) rootView.findViewById(R.id.product_name_and_spec_type);
             textView.setText(String.format("%s(%s-%s)", mWorkCommand.getProductName(),
                    Utils.isEmptyString(mWorkCommand.getSpec()) ? "\"\"" : mWorkCommand.getSpec(),
                    Utils.isEmptyString(mWorkCommand.getType()) ? "\"\"" : mWorkCommand.getType()));
         }
 
         private void _setTeamLeaderMenu(Menu menu, MenuInflater inflater) {
             if (mWorkCommand.getStatus() == Constants.STATUS_ENDING) {
                 inflater.inflate(R.menu.team_leader_work_command_menu, menu);
             }
         }
 
         private void _setTechReq() {
             TextView textView = (TextView) rootView.findViewById(R.id.tech_req);
             textView.setText(mWorkCommand.getTechReq());
         }
 
     }
 
 }
