 package com.jinheyu.lite_mms;
 
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.ListFragment;
 import android.text.method.ScrollingMovementMethod;
 import android.view.*;
 import android.widget.*;
 import com.jinheyu.lite_mms.data_structures.WorkCommand;
 import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;
 import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 
 public abstract class WorkCommandListFragment extends ListFragment implements PullToRefreshAttacher.OnRefreshListener {
     /**
      * just used in array from getSymbol()
      */
     public static final int DEPARTMENT_ID_INDEX = 0, TEAM_ID_INDEX = 0, STATUS_INDEX = 1;
     public static final String ARG_SECTION_NUMBER = "section_number";
     protected ActionMode mActionMode;
     private ActionMode.Callback mActionModeListener = getActionModeCallback();
     private HashSet<Integer> mSelectedPositions = new HashSet<Integer>();
     private PullToRefreshAttacher mPullToRefreshAttacher;
     private ProgressDialog mProgressDialog;
     private boolean isLoadingWorkCommandList;
 
     public void dismissProcessDialog() {
         if (mProgressDialog != null) {
             mProgressDialog.dismiss();
         }
     }
 
     public View getItemView(final int position, View convertView) {
         final WorkCommand workCommand = getWorkCommandAtPosition(position);
         ViewHolder viewHolder;
         if (convertView.getTag() == null) {
             viewHolder = new ViewHolder((ImageButton) convertView.findViewById(R.id.image),
                     (TextView) convertView.findViewById(R.id.idTextView),
                     (CheckBox) convertView.findViewById(R.id.check),
                     (TextView) convertView.findViewById(R.id.extra),
                     (TextView) convertView.findViewById(R.id.customer_name),
                     (TextView) convertView.findViewById(R.id.product_name),
                     (TextView) convertView.findViewById(R.id.org_weight),
                     (TextView) convertView.findViewById(R.id.spec_and_type));
             convertView.setTag(viewHolder);
         } else {
             viewHolder = (ViewHolder) convertView.getTag();
         }
         setViewHold(position, workCommand, viewHolder);
 
         convertView.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 if (isInActionMode()) {
                     CheckBox checkBox = (CheckBox) v.findViewById(R.id.check);
                     checkBox.toggle();
                 } else {
                     if (!isLoadingWorkCommandList) {
                         Intent intent = new Intent(getActivity(), WorkCommandActivity.class);
                         intent.putExtra("workCommandId", getWorkCommandIdAtPosition(position));
                         getActivity().startActivity(intent);
                     }
                 }
             }
         });
         convertView.setOnLongClickListener(new View.OnLongClickListener() {
             @Override
             public boolean onLongClick(View v) {
                 if (isInActionMode()) {
                     return false;
                 } else {
                     startActionMode();
                     CheckBox checkBox = (CheckBox) v.findViewById(R.id.check);
                     checkBox.setChecked(true);
                     return true;
                 }
             }
         });
         return convertView;
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
         View rootView = inflater.inflate(R.layout.fragment_work_command_list, container, false);
         mPullToRefreshAttacher = ((WorkCommandListActivity) getActivity()).getPullToRefreshAttacher();
         final PullToRefreshLayout ptrLayout = (PullToRefreshLayout) rootView.findViewById(R.id.ptr_layout);
         ptrLayout.setPullToRefreshAttacher(mPullToRefreshAttacher, this);
 
         final ListView listView = (ListView) rootView.findViewById(android.R.id.list);
         TextView noDataView = (TextView) rootView.findViewById(android.R.id.empty);
         noDataView.setMovementMethod(new ScrollingMovementMethod());
 
         listView.setEmptyView(noDataView);
         return rootView;
     }
 
     @Override
     public void onRefreshStarted(View view) {
         loadWorkCommandList();
         isLoadingWorkCommandList = true;
     }
 
     @Override
     public void onResume() {
         super.onResume();
         mProgressDialog = newProgressDialog();
         loadWorkCommandList();
     }
 
     public void setRefreshComplete() {
         mPullToRefreshAttacher.setRefreshComplete();
         isLoadingWorkCommandList = false;
     }
 
     protected void clearAllCheckedItems() {
         mProgressDialog = newProgressDialog();
         loadWorkCommandList();
         mSelectedPositions.clear();
     }
 
     protected abstract ActionMode.Callback getActionModeCallback();
 
     protected int[] getCheckedWorkCommandIds() {
         List<WorkCommand> workCommands = getCheckedWorkCommands();
         int[] result = new int[workCommands.size()];
         for (int i = 0; i < workCommands.size(); i++) {
             result[i] = workCommands.get(i).getId();
         }
         return result;
     }
 
     protected List<WorkCommand> getCheckedWorkCommands() {
         List<WorkCommand> result = new ArrayList<WorkCommand>();
         for (Integer position : mSelectedPositions) {
             result.add(getWorkCommandAtPosition(position));
         }
         return result;
     }
 
     protected int[] getSymbols() {
         return getArguments() != null ? getArguments().getIntArray(ARG_SECTION_NUMBER) : new int[]{0, 0};
     }
 
     protected abstract void loadWorkCommandList();
 
     private boolean deselectAtPosition(int position) {
         return mSelectedPositions.remove(position);
     }
 
     private WorkCommand getWorkCommandAtPosition(int position) {
         try {
             return (WorkCommand) getListAdapter().getItem(position);
         } catch (ClassCastException e) {
             throw new ClassCastException(getListAdapter().toString() + "无WorkCommand");
         }
     }
 
     private int getWorkCommandIdAtPosition(int position) {
         return getWorkCommandAtPosition(position).getId();
     }
 
     private boolean isCheckedAtPosition(int position) {
         return mSelectedPositions.contains(position);
     }
 
     private boolean isInActionMode() {
         return mActionMode != null;
     }
 
     private ProgressDialog newProgressDialog() {
         return ProgressDialog.show(WorkCommandListFragment.this.getActivity(), getString(R.string.loading_data), getString(R.string.please_wait), true);
     }
 
     private void selectAtPosition(int position) {
         mSelectedPositions.add(position);
     }
 
     private void setActionModeSubTitle() {
         if (mActionMode != null) {
             final int checkedCount = mSelectedPositions.size();
             switch (checkedCount) {
                 case 0:
                     mActionMode.setSubtitle("未选择");
                     break;
                 default:
                     mActionMode.setSubtitle("已选中" + checkedCount + "项");
                     break;
             }
         }
     }
 
     private void setViewHold(final int position, WorkCommand workCommand, ViewHolder viewHolder) {
         viewHolder.checkBox.setVisibility(isInActionMode() ? View.VISIBLE : View.GONE);
         viewHolder.idTextView.setText(String.valueOf(workCommand.getId()));
         List<String> extraMessages = new ArrayList<String>();
         if (workCommand.isUrgent()) {
             extraMessages.add("加急");
         }
         if (workCommand.isRejected()) {
             extraMessages.add("退镀");
         }
         if (extraMessages.isEmpty()) {
             viewHolder.extraTextView.setVisibility(View.GONE);
         } else {
             viewHolder.extraTextView.setVisibility(View.VISIBLE);
             viewHolder.extraTextView.setText(Utils.join(extraMessages, ", "));
         }
 
         viewHolder.checkBox.setChecked(isCheckedAtPosition(position));
         viewHolder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
             @Override
             public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                 if (isChecked) {
                     selectAtPosition(position);
                 } else {
                     deselectAtPosition(position);
                 }
                 setActionModeSubTitle();
             }
         });
 
         viewHolder.customerTextView.setText(workCommand.getCustomerName());
         viewHolder.productTextView.setText(workCommand.getProductName());
 
         viewHolder.orgWeightTextView.setText(workCommand.measured_by_weight() ? String.format("%d千克", workCommand.getOrgWeight()) : String.format("%d千克/%d%s", workCommand.getOrgWeight(), workCommand.getOrgCnt(), workCommand.getUnit()));
         viewHolder.specTypeTextView.setText(String.format("(%s-%s)",
                Utils.isEmptyString(workCommand.getSpec()) ? "\"\"" : workCommand.getSpec(),
                Utils.isEmptyString(workCommand.getType()) ? "\"\"" : workCommand.getType()));
         new GetImageTask(viewHolder.imageButton, workCommand.getPicPath(), false).execute();
     }
 
     private void startActionMode() {
         mActionMode = getActivity().startActionMode(mActionModeListener);
     }
 }
 
 class ViewHolder {
     public ImageButton imageButton;
     public TextView idTextView;
     public CheckBox checkBox;
     public TextView extraTextView;
     public TextView productTextView;
     public TextView customerTextView;
     public TextView orgWeightTextView;
     public TextView specTypeTextView;
 
     public ViewHolder(ImageButton imageButton, TextView idTextView, CheckBox checkBox, TextView extraTextView, TextView customerTextView, TextView productTextView, TextView orgWeightTextView, TextView specTypeTextView) {
         this.imageButton = imageButton;
         this.idTextView = idTextView;
         this.checkBox = checkBox;
         this.extraTextView = extraTextView;
         this.customerTextView = customerTextView;
         this.productTextView = productTextView;
         this.orgWeightTextView = orgWeightTextView;
         this.specTypeTextView = specTypeTextView;
     }
 
 }
 
 class WorkCommandListAdapter extends BaseAdapter {
     private final LayoutInflater mInflater;
     private WorkCommandListFragment mFragment;
     private int mResource;
     private List<WorkCommand> mWorkCommandList;
 
     public WorkCommandListAdapter(WorkCommandListFragment fragment, List<WorkCommand> workCommandList) {
         this.mFragment = fragment;
         mWorkCommandList = workCommandList;
         this.mResource = R.layout.fragment_work_command;
         mInflater = (LayoutInflater) fragment.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
     }
 
     @Override
     public int getCount() {
         return mWorkCommandList.size();
     }
 
     @Override
     public Object getItem(int position) {
         return mWorkCommandList.get(position);
     }
 
     @Override
     public long getItemId(int position) {
         return mWorkCommandList.get(position).getId();
     }
 
     @Override
     public View getView(final int position, View convertView, ViewGroup parent) {
         if (convertView == null) {
             convertView = mInflater.inflate(mResource, null);
         }
         return mFragment.getItemView(position, convertView);
     }
 
 
 }
 
