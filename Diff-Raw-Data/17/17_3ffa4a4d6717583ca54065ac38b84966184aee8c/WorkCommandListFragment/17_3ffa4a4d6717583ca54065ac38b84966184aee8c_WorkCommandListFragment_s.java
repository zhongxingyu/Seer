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
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 
 public abstract class WorkCommandListFragment extends ListFragment implements PullToRefreshAttacher.OnRefreshListener {
     public static final String ARG_SECTION_NUMBER = "section_number";
     private HashSet<Integer> mSelectedPositions = new HashSet<Integer>();
     private ActionMode mActionMode;
     private PullToRefreshAttacher mPullToRefreshAttacher;
     private ProgressDialog mProgressDialog;
     protected ActionMode.Callback mActionModeListener = new ActionMode.Callback() {
 
         @Override
         public boolean onCreateActionMode(ActionMode mode, Menu menu) {
             MenuInflater mInflater = mode.getMenuInflater();
             mInflater.inflate(R.menu.team_leader_work_command_list_menu, menu);
             mode.setTitle(getString(R.string.please_select));
             return true;
         }
 
         @Override
         public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
             return false;
         }
 
         @Override
         public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
             TeamLeaderMenuItemWrapper wrapper = new TeamLeaderMenuItemWrapper(getActivity());
             switch (item.getItemId()) {
                 case R.id.carry_forward:
                     wrapper.carryForward(getCheckedWorkCommandIds());
                     return true;
                 case R.id.quick_carryForward:
                     wrapper.carryForwardQuickly(getCheckedWorkCommandIds());
                     return true;
                 default:
                     return false;
             }
         }
 
         @Override
         public void onDestroyActionMode(ActionMode mode) {
             mActionMode = null;
             clearAllCheckedItems();
         }
 
     };
 
     public void dismissProcessDialog() {
         if (mProgressDialog != null) {
             mProgressDialog.dismiss();
         }
     }
     private void clearAllCheckedItems() {
         mSelectedPositions.clear();
     }
     private int[] getCheckedWorkCommandIds() {
         List<WorkCommand> workCommands = getCheckedWorkCommands();
         int[] result = new int[workCommands.size()];
         for (int i = 0; i < workCommands.size(); i++) {
             result[i] = workCommands.get(i).getId();
         }
         return result;
     }
 
     private List<WorkCommand> getCheckedWorkCommands() {
         List<WorkCommand> result = new ArrayList<WorkCommand>();
         for (Integer position : mSelectedPositions) {
             result.add(getWorkCommandAtPosition(position));
         }
         return result;
     }
 
     public View getItemView(final int position, View convertView) {
         final WorkCommand workCommand = getWorkCommandAtPosition(position);
         ViewHolder viewHolder;
         if (convertView.getTag() == null) {
             viewHolder = new ViewHolder((TextView) convertView.findViewById(R.id.idTextView), (CheckBox) convertView.findViewById(R.id.check));
             convertView.setTag(viewHolder);
         } else {
             viewHolder = (ViewHolder) convertView.getTag();
         }
         viewHolder.checkBox.setVisibility(isInActionMode() ? View.VISIBLE : View.GONE);
         viewHolder.idTextView.setText(String.valueOf(workCommand.getId()));
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
         convertView.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 if (isInActionMode()) {
                     CheckBox checkBox = (CheckBox) v.findViewById(R.id.check);
                     checkBox.toggle();
                 } else {
                     Intent intent = new Intent(getActivity(), WorkCommandActivity.class);
                     intent.putExtra("work_command", getWorkCommandAtPosition(position));
                     startActivity(intent);
 
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
 
     private void selectAtPosition(int position) {
         mSelectedPositions.add(position);
     }
 
     private WorkCommand getWorkCommandAtPosition(int position) {
         try {
             return (WorkCommand) getListAdapter().getItem(position);
         } catch (ClassCastException e) {
             throw new ClassCastException(getListAdapter().toString() + "无WorkCommand");
         }
     }
 
     private boolean isCheckedAtPosition(int position) {
         return mSelectedPositions.contains(position);
     }
 
     private boolean isInActionMode() {
         return mActionMode != null;
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
 
     private void startActionMode() {
         mActionMode = getActivity().startActionMode(mActionModeListener);
     }
 
     private boolean deselectAtPosition(int position) {
         return mSelectedPositions.remove(position);
     }
 
     protected int getSymbol() {
         return getArguments() != null ? getArguments().getInt(ARG_SECTION_NUMBER) : 0;
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
         View rootView = inflater.inflate(R.layout.fragment_work_command_list, container, false);
         final ListView listView = (ListView) rootView.findViewById(android.R.id.list);
         TextView noDataView = (TextView) rootView.findViewById(android.R.id.empty);
         noDataView.setMovementMethod(new ScrollingMovementMethod());
 
        mPullToRefreshAttacher = ((PullToRefresh) getActivity()).getPullToRefreshAttacher();
        mPullToRefreshAttacher.addRefreshableView(listView, this);
        mPullToRefreshAttacher.addRefreshableView(noDataView, this);
         listView.setEmptyView(noDataView);
         mProgressDialog = ProgressDialog.show(WorkCommandListFragment.this.getActivity(), getString(R.string.loading_data), getString(R.string.please_wait), true);
         loadWorkCommandList();
         return rootView;
     }
 
     protected abstract void loadWorkCommandList();
 
     @Override
     public void onRefreshStarted(View view) {
         loadWorkCommandList();
     }
 
     public void setRefreshComplete() {
         mPullToRefreshAttacher.setRefreshComplete();
     }
 }
 
 class ViewHolder {
     public TextView idTextView;
     public CheckBox checkBox;
 
     public ViewHolder(TextView idTextView, CheckBox checkBox) {
         this.idTextView = idTextView;
         this.checkBox = checkBox;
     }
 }
 
 class WorkCommandListAdapter extends BaseAdapter {
     private final LayoutInflater mInflater;
     private WorkCommandListFragment mFragment;
     private int mResource;
     private Context mContext;
     private List<WorkCommand> mWorkCommandList;
 
     public WorkCommandListAdapter(WorkCommandListFragment fragment, List<WorkCommand> workCommandList) {
         mContext = fragment.getActivity();
         this.mFragment = fragment;
         mWorkCommandList = workCommandList;
         this.mResource = R.layout.fragment_work_command;
         mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
 
