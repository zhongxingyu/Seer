 package io.ehdev.android.drivingtime.view.fragments;
 
 import android.app.FragmentManager;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Toast;
 import dagger.ObjectGraph;
 import io.ehdev.android.drivingtime.R;
 import io.ehdev.android.drivingtime.adapter.DrivingRecordAdapter;
 import io.ehdev.android.drivingtime.backend.StringHelper;
 import io.ehdev.android.drivingtime.backend.model.Record;
 import io.ehdev.android.drivingtime.backend.model.Task;
 import io.ehdev.android.drivingtime.database.dao.DatabaseHelper;
 import io.ehdev.android.drivingtime.module.ModuleGetters;
 import io.ehdev.android.drivingtime.view.PostEditExecution;
 import io.ehdev.android.drivingtime.view.dialog.EditRecordDialog;
 import io.ehdev.android.drivingtime.view.dialog.InsertOrEditRecordDialog;
 import io.ehdev.android.drivingtime.view.dialog.ShowDialog;
 import io.ehdev.android.drivingtime.view.entry.DisplayProgressRecordRow;
 
 import javax.inject.Inject;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 public class TaskDrivingRecordReviewFragment extends AbstractListDrivingRecordFragment {
 
     public static final String TAG = TaskDrivingRecordReviewFragment.class.getSimpleName();
     private Task drivingTask;
     private DisplayProgressRecordRow progressBar;
 
     @Inject
     protected DatabaseHelper databaseHelper;
 
     public TaskDrivingRecordReviewFragment(Task drivingTask){
         this.drivingTask = drivingTask;
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState){
         super.onCreate(savedInstanceState);
         ObjectGraph objectGraph = ObjectGraph.create( ModuleGetters.getInstance());
         objectGraph.inject(this);
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
         List<Record> taskEntries = getTaskEntries();
         setAdapter(new DrivingRecordAdapter(getActivity(), taskEntries));
         View view = super.onCreateView(inflater, container, savedInstanceState);
         progressBar = (DisplayProgressRecordRow)view.findViewById(R.id.taskProgressDialog);
         setProgressBar(taskEntries);
         return view;
     }
 
     private void setProgressBar(List<Record> listOfEntries) {
 
         long requiredTime = drivingTask.getRequiredHours().getMillis();
         long spentTime = getRemainingTime(listOfEntries);
 
         progressBar.setRightText(String.format("%s Required", StringHelper.getPeriodAsString(requiredTime)));
         progressBar.setMaxOfProgress(requiredTime);
 
         progressBar.setCurrentProgress(spentTime);
         progressBar.setLeftText(String.format("Remaining : %s", StringHelper.getPeriodAsString((requiredTime - spentTime))));
 
         progressBar.invalidate();
     }
 
     private List<Record> getTaskEntries() {
         List<Record> drivingRecordList;
         try{
             drivingRecordList = new ArrayList<Record>();
             List<Record> list = databaseHelper.getDrivingRecordForTask(drivingTask.getId(), databaseHelper.getTaskDao());
             for(Record record : list){
                 databaseHelper.getTaskDao().refresh(record.getDrivingTask());
                 drivingRecordList.add(record);
             }
             return drivingRecordList;
         } catch (Exception e) {
             Log.i(TAG, e.getMessage());
             return new ArrayList<Record>();
         }
     }
 
     private long getRemainingTime(List<Record> listOfEntries) {
         long requiredTime = 0;
         if(listOfEntries == null || listOfEntries.isEmpty())
             return requiredTime;
 
         for(Record record : listOfEntries)
             requiredTime += record.getDurationOfDriving().getMillis();
 
         return requiredTime;
     }
 
     protected int getViewId(){
         return R.layout.aggregated_list_view;
     }
 
     protected PostEditExecution getReloadAdapter(){
         return new PostEditExecution() {
             public void execute(){
                 new AsyncTask<Void, Void, List<Record>>(){
 
                     @Override
                     protected List<Record> doInBackground(Void... params) {
                         return getTaskEntries();
                     }
 
                     @Override
                     protected void onPostExecute(List<Record> records){
                         getAdapter().replaceDataSet(records);
                         setProgressBar(records);
                     }
                 }.execute();
             }
         };
     }
 
    protected ShowDialog<Record> getShowDialog(){
         return new ShowDialog<Record>() {
             @Override
             public void showDialog(Record recordToEdit) {
                 try {
                     FragmentManager fm = getChildFragmentManager();
                     InsertOrEditRecordDialog insertRecordDialog = getInsertRecordDialog(recordToEdit);
                     insertRecordDialog.show(fm, "Insert Record Dialog");
                 } catch (Exception e) {
                     Toast.makeText(getActivity(), "Unable to create view", Toast.LENGTH_LONG);
                     Log.i(TAG, e.getMessage());
                 }
             }
 
             private InsertOrEditRecordDialog getInsertRecordDialog(Record recordToEdit) throws SQLException {
                 List<Task> drivingTaskList = databaseHelper.getTaskDao().queryForAll();
                 return new EditRecordDialog(recordToEdit, drivingTaskList, getReloadAdapter());
             }
         };
     }
 
     @Override
     protected DatabaseHelper getDatabaseHelper() {
         return databaseHelper;
     }
 }
