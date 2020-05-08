 package com.jinheyu.lite_mms;
 
 
 import android.annotation.SuppressLint;
 import android.app.ActionBar;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.FragmentTransaction;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.BaseAdapter;
 import android.widget.LinearLayout;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.jinheyu.lite_mms.data_structures.Constants;
 import com.jinheyu.lite_mms.data_structures.QualityInspectionReport;
 import com.jinheyu.lite_mms.data_structures.WorkCommand;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import static android.content.DialogInterface.OnClickListener;
 
 
 public class QualityInspectorWorkCommandActivity extends FragmentActivity implements ActionBar.TabListener, UpdateWorkCommand {
 
     private static final int CREATE_QUALITY_INSPECTION_REPORT_CODE = 98;
     private ViewPager mViewPager;
     private int workCommandId;
     private MyFragmentPagerAdapter fragmentPagerAdapter;
     private WorkCommand workCommand;
 
     @Override
     public void beforeUpdateWorkCommand() {
         ((QualityInspectionReportListFragment) fragmentPagerAdapter.getRegisteredFragment(0)).beforeUpdateWorkCommand();
         ((WorkCommandFragment) fragmentPagerAdapter.getRegisteredFragment(1)).beforeUpdateWorkCommand();
     }
 
     @Override
     public Intent getParentActivityIntent() {
         return new Intent(this, MyApp.getCurrentUser().getDefaultActivity());
     }
 
 
     @Override
     public void onBackPressed() {
         final QualityInspectionReportListFragment qualityInspectionReportListFragment = (QualityInspectionReportListFragment) this.fragmentPagerAdapter.getRegisteredFragment(0);
         if (qualityInspectionReportListFragment.isModified()) {
             AlertDialog.Builder builder = new AlertDialog.Builder(this);
             builder.setMessage("您已经修改了质检报告，退出前保存质检报告?");
             builder.setTitle("警告");
             builder.setNegativeButton(android.R.string.cancel, null);
             builder.setPositiveButton(R.string.save, new OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialog, int which) {
                     saveQualityInspectionReports(workCommand, MyApp.getQualityInspectionReports());
                 }
             });
             builder.setNeutralButton(R.string.unsave, new OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialog, int which) {
                     finish();
                 }
             });
             builder.show();
         } else {
             finish();
         }
     }
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_quality_inspector_work_command);
         // make action bar hide title
         final ActionBar actionBar = getActionBar();
         actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
         actionBar.setDisplayShowTitleEnabled(false);
         actionBar.setDisplayShowHomeEnabled(false);
         // add pager
         mViewPager = (ViewPager) findViewById(R.id.pager);
         fragmentPagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager());
         mViewPager.setAdapter(fragmentPagerAdapter);
         mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
             @Override
             public void onPageSelected(int position) {
                 actionBar.setSelectedNavigationItem(position);
             }
         });
 
         // add tabs
         actionBar.removeAllTabs();
         for (int i = 0; i < fragmentPagerAdapter.getCount(); i++) {
             actionBar.addTab(
                     actionBar.newTab()
                             .setText(i == 0 ? getString(R.string.quality_inspection_report_list) :
                                     getString(R.string.work_command_detail))
                             .setTabListener(this));
         }
 
         workCommandId = getIntent().getIntExtra("workCommandId", 0);
         /**
          * 之所以要将获取工单信息和质检报告列表的操作统一放在Activity中进行，原因是这两个Fragment的信息要时刻保证一致，
          * 不能一个刷新了一个还没有刷新
          */
         new GetWorkCommandAsyncTask(this).execute(workCommandId);
     }
 
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         int status = getIntent().getIntExtra("symbol", 0);
         if (status == Constants.STATUS_QUALITY_INSPECTING) {
             getMenuInflater().inflate(R.menu.quality_inspector_work_command_menu, menu);
         } else if (status == Constants.STATUS_FINISHED) {
             getMenuInflater().inflate(R.menu.quality_inspector_work_command_retrieve_menu, menu);
         }
         return super.onCreateOptionsMenu(menu);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
        if (workCommand == null) {
            return false;
        }
         QualityInspectionReportListFragment qualityInspectionReportListFragment;
         qualityInspectionReportListFragment = (QualityInspectionReportListFragment) ((MyFragmentPagerAdapter) mViewPager.getAdapter()).getRegisteredFragment(0);
 
         switch (item.getItemId()) {
             case R.id.action_complete_quality_inspection:
                 if (!qualityInspectionReportListFragment.loading()) {
                     List<QualityInspectionReport> qualityInspectionReports = MyApp.getQualityInspectionReports();
                     if (qualityInspectionReports.isEmpty()) {
                         Toast.makeText(QualityInspectorWorkCommandActivity.this, "请至少填写一条质检报告！", Toast.LENGTH_SHORT).show();
                     } else if (!qualityInspectionReportListFragment.isModified()) {
                         Toast.makeText(QualityInspectorWorkCommandActivity.this, "您没有对质检报告作出变动，不必提交!", Toast.LENGTH_SHORT).show();
                     } else {
                         showNoticeDialog(qualityInspectionReports, this.workCommand);
                     }
                 }
                 break;
             case R.id.action_new_quality_inspection_report:
                 Intent intent = new Intent(QualityInspectorWorkCommandActivity.this, CreateQIReportStep1.class);
                 intent.putExtra("workCommand", workCommand);
                 startActivityForResult(intent, CREATE_QUALITY_INSPECTION_REPORT_CODE);
                 break;
             case R.id.action_reset:
                 qualityInspectionReportListFragment.resetContent();
                 Toast.makeText(QualityInspectorWorkCommandActivity.this, "重置成功！", Toast.LENGTH_SHORT).show();
                 break;
             case R.id.action_retrieve_quality_inspection_report:
                 MenuItemWrapper wrapper = new MenuItemWrapper(this);
                 wrapper.retrieveQualityInspection(workCommand);
                 break;
             default:
                 break;
         }
         return super.onOptionsItemSelected(item);
     }
 
     @Override
     public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
     }
 
     @Override
     public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
         mViewPager.setCurrentItem(tab.getPosition());
     }
 
     @Override
     public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
 
     }
 
     @Override
     public void updateWorkCommand(WorkCommand workCommand) {
         ((QualityInspectionReportListFragment) fragmentPagerAdapter.getRegisteredFragment(0)).updateWorkCommand(workCommand);
         ((WorkCommandFragment) fragmentPagerAdapter.getRegisteredFragment(1)).updateWorkCommand(workCommand);
         this.workCommand = workCommand;
     }
 
     @Override
     public void updateWorkCommandFailed(Exception ex) {
         ((QualityInspectionReportListFragment) fragmentPagerAdapter.getRegisteredFragment(0)).updateWorkCommandFailed(ex);
         ((WorkCommandFragment) fragmentPagerAdapter.getRegisteredFragment(1)).updateWorkCommandFailed(ex);
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (requestCode == CREATE_QUALITY_INSPECTION_REPORT_CODE && resultCode == RESULT_OK) {
             QualityInspectionReportListFragment qualityInspectionReportListFragment = (QualityInspectionReportListFragment) ((MyFragmentPagerAdapter) mViewPager.getAdapter()).getRegisteredFragment(0);
             ((BaseAdapter) qualityInspectionReportListFragment.getListAdapter()).notifyDataSetChanged();
             qualityInspectionReportListFragment.setTextViewQualityInspected();
             qualityInspectionReportListFragment.setModified(true);
         }
     }
 
     private void saveQualityInspectionReports(final WorkCommand workCommand, final List<QualityInspectionReport> qualityInspectionReports) {
 
         XProgressableRunnable.Builder<Void> builder = new XProgressableRunnable.Builder<Void>(this);
         builder.msg("正在保存质检报告列表");
         builder.run(new XProgressableRunnable.XRunnable<Void>() {
             @Override
             public Void run() throws Exception {
                 MyApp.getWebServieHandler().saveQualityInspectionReports(workCommand, qualityInspectionReports);
                 return null;
             }
         });
         builder.after(new Runnable() {
             @Override
             public void run() {
                 // 删除对应的本地图片
                 for (QualityInspectionReport qualityInspectionReport: MyApp.getQualityInspectionReports()) {
                     if (!Utils.isEmptyString(qualityInspectionReport.getLocalPicPath())) {
                         new File(qualityInspectionReport.getLocalPicPath()).delete();
                     }
                 }
                 finish();
             }
         });
         builder.okMsg("保存成功");
         builder.create().start();
     }
 
     private void showNoticeDialog(List<QualityInspectionReport> qualityInspectionReports, WorkCommand workCommand) {
         android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
         Fragment prev = getSupportFragmentManager().findFragmentByTag("NOTICE_FRAGMENT");
         if (prev != null) {
             ft.remove(prev);
         }
         DialogFragment dialog = new NoticeDialogFragment(qualityInspectionReports, workCommand);
         dialog.show(ft, "NOTICE_FRAGMENT");
     }
 
     class MyFragmentPagerAdapter extends FragmentPagerAdapter {
 
         List<Fragment> registeredFragments;
 
         public MyFragmentPagerAdapter(FragmentManager fm) {
             super(fm);
             registeredFragments = new ArrayList<Fragment>();
             registeredFragments.add(new QualityInspectionReportListFragment());
             registeredFragments.add(new WorkCommandFragment());
         }
 
         @Override
         public int getCount() {
             return 2;
         }
 
         @Override
         public Fragment getItem(int i) {
             return registeredFragments.get(i);
         }
 
         @Override
         public CharSequence getPageTitle(int position) {
             return position == 0 ? getString(R.string.quality_inspection_report_list) :
                     getString(R.string.work_command_detail);
         }
 
         Fragment getRegisteredFragment(int position) {
             return registeredFragments.get(position);
         }
     }
 
     private class NoticeDialogFragment extends DialogFragment {
         private final List<QualityInspectionReport> qualityInspectionReports;
         private final WorkCommand workCommand;
 
         public NoticeDialogFragment(List<QualityInspectionReport> qualityInspectionReports,
                                     WorkCommand workCommand) {
             this.qualityInspectionReports = qualityInspectionReports;
             this.workCommand = workCommand;
         }
 
         @SuppressLint("ResourceAsColor")
         @Override
         public Dialog onCreateDialog(Bundle savedInstanceState) {
             AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
             builder.setTitle(String.format("确认提交工单%d质检结果?", workCommandId));
             View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_quality_inspect, null);
             TableLayout tableLayoutResults = (TableLayout) view.findViewById(R.id.tableLayoutResults);
 
             builder.setView(view);
             int totalWeight = 0;
             int totalQuantity = 0;
             boolean odd = true;
             float dpSize = (getResources().getDisplayMetrics().densityDpi / 160.0f);
             for (QualityInspectionReport qir : qualityInspectionReports) {
                 TableRow tableRow = new TableRow(getActivity());
                 tableRow.setGravity(Gravity.CENTER_VERTICAL);
                 tableRow.setPadding(0, (int) (3 * dpSize), 0, (int) (3 * dpSize));
                 if (odd) {
                     tableRow.setBackgroundColor(getResources().getColor(R.color.odd_row));
                 }
                 odd = !odd;
                 TextView textViewResult = new TextView(getActivity());
                 textViewResult.setText(qir.getLiterableResult());
                 textViewResult.setTextAppearance(getActivity(), android.R.style.TextAppearance_Large);
                 textViewResult.setPadding((int) (10 * dpSize), 0, 0, 0);
                 TextView textViewWeight = new TextView(getActivity());
                 if (!workCommand.measured_by_weight()) {
                     totalQuantity += qir.getQuantity();
                 }
                 textViewWeight.setTextAppearance(getActivity(), android.R.style.TextAppearance_Large);
                 textViewWeight.setText(Utils.getQIRWeightAndQuantity(qir, workCommand));
                 textViewWeight.setPadding((int) (5 * dpSize), 0, 0, 0);
                 tableRow.addView(textViewResult, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                 tableRow.addView(textViewWeight, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                 tableLayoutResults.addView(tableRow);
                 totalWeight += qir.getWeight();
             }
 
             TextView textViewWeight = (TextView) view.findViewById(R.id.textViewTotalWeight);
             textViewWeight.setText(Utils.getWeightAndQuantity(totalWeight, totalQuantity, workCommand));
             builder.setNegativeButton(android.R.string.cancel, null);
             final int finalTotalWeight = totalWeight;
             builder.setPositiveButton(R.string.submit, new OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialogInterface, int i) {
                     if (finalTotalWeight < workCommand.getProcessedWeight()) {
                         AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                         builder.setTitle(R.string.warning);
                         builder.setMessage(String.format("工单重量为%d, 你提交的质检重量是%d, 是否仍然要提交质检结果?",
                                 workCommand.getProcessedWeight(), finalTotalWeight));
                         builder.setNegativeButton(android.R.string.cancel, null);
                         builder.setPositiveButton(android.R.string.ok, new OnClickListener() {
                             @Override
                             public void onClick(DialogInterface dialog, int which) {
                                 submitQualityInspectionReport();
                             }
                         });
                         builder.show();
                     } else {
                         submitQualityInspectionReport();
                     }
                 }
             });
             return builder.create();
         }
 
         private void submitQualityInspectionReport() {
 
             XProgressableRunnable.Builder<Void> builder = new XProgressableRunnable.Builder<Void>(QualityInspectorWorkCommandActivity.this);
             builder.msg("正在提交质检结果");
             builder.run(new XProgressableRunnable.XRunnable<Void>() {
                 @Override
                 public Void run() throws Exception {
                     MyApp.getWebServieHandler().submitQualityInspection(workCommand, qualityInspectionReports);
                     return null;
                 }
             });
             builder.okMsg("提交成功");
             builder.after(new Runnable() {
                 @Override
                 public void run() {
                     // clear the local pictures
                     for (QualityInspectionReport qualityInspectionReport: MyApp.getQualityInspectionReports()) {
                         if (!Utils.isEmptyString(qualityInspectionReport.getLocalPicPath())) {
                             new File(qualityInspectionReport.getLocalPicPath()).delete();
                         }
                     }
                     finish();
                 }
             });
             builder.create().start();
         }
 
     }
 }
