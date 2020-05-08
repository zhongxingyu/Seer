 package io.ehdev.android.drivingtime.view.fragments;
 
 import android.app.Fragment;
 import android.app.FragmentManager;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.Toast;
 import dagger.ObjectGraph;
 import io.ehdev.android.drivingtime.R;
 import io.ehdev.android.drivingtime.adapter.AggregatedDrivingRecordAdapter;
 import io.ehdev.android.drivingtime.backend.model.Record;
 import io.ehdev.android.drivingtime.backend.model.Task;
 import io.ehdev.android.drivingtime.database.dao.DatabaseHelper;
 import io.ehdev.android.drivingtime.module.ModuleGetters;
 import io.ehdev.android.drivingtime.view.activity.ListEntriesForTaskActivity;
 import io.ehdev.android.drivingtime.view.dialog.InsertOrEditRecordDialog;
 import io.ehdev.android.drivingtime.view.dialog.InsertRecordDialog;
 import org.joda.time.DateTime;
 import org.joda.time.Duration;
 
 import javax.inject.Inject;
 import java.sql.SQLException;
 import java.util.List;
 
 public class MainFragment extends Fragment {
 
     @Inject
    private DatabaseHelper databaseHelper;
 
     private AggregatedDrivingRecordAdapter aggregatedDrivingRecordAdapter;
 
     private static final String TAG = MainFragment.class.getName();
 
     @Override
     public void onCreate(Bundle savedInstanceState){
         super.onCreate(savedInstanceState);
         ObjectGraph objectGraph = ObjectGraph.create(new ModuleGetters(getActivity()));
         objectGraph.inject(this);
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
     {
 
         Log.d(MainFragment.class.getName(), "onCreateView");
         View view = inflater.inflate(R.layout.main, null);
         showEntryDialogFromButtonClick(view);
         addAdapterToListView(view);
         return view;
 
     }
 
     private void showEntryDialogFromButtonClick(View view) {
         (view.findViewById(R.id.addTimeToLog)).setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 try {
                     FragmentManager fm = getChildFragmentManager();
                     InsertOrEditRecordDialog insertRecordDialog = getInsertRecordDialog();
                     insertRecordDialog.show(fm, "Insert Record Dialog");
                 } catch (Exception e) {
                     Toast.makeText(getActivity(), "Unable to create view", Toast.LENGTH_LONG);
                     Log.i(TAG, e.getMessage());
                 }
             }
 
             private InsertOrEditRecordDialog getInsertRecordDialog() throws SQLException {
                 List<Task> drivingTaskList = databaseHelper.getTaskDao().queryForAll();
                 Record drivingRecord = new Record(drivingTaskList.get(0), new DateTime(), Duration.standardHours(1));
                 return new InsertRecordDialog(drivingRecord, drivingTaskList, aggregatedDrivingRecordAdapter);
             }
         });
     }
 
     private void addAdapterToListView(View view) {
         ListView newListView = (ListView)view.findViewById(R.id.currentStatusView);
         aggregatedDrivingRecordAdapter = new AggregatedDrivingRecordAdapter(getActivity(), databaseHelper.createDrivingRecordList());
         newListView.setAdapter(aggregatedDrivingRecordAdapter);
         newListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                 Log.i(TAG, "OnClick");
                 Intent newActivity = new Intent();
                 newActivity.setClass(getActivity(), ListEntriesForTaskActivity.class);
                 newActivity.putExtra("taskId", aggregatedDrivingRecordAdapter.getItem(position).getTaskId());
                 getActivity().startActivity(newActivity);
             }
         });
     }
 }
