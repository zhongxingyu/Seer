 package io.ehdev.android.drivingtime.view.dialog;
 
 import android.content.DialogInterface;
 import dagger.ObjectGraph;
 import io.ehdev.android.drivingtime.adapter.AggregatedDrivingRecordAdapter;
 import io.ehdev.android.drivingtime.backend.model.Record;
 import io.ehdev.android.drivingtime.backend.model.Task;
 import io.ehdev.android.drivingtime.database.dao.DatabaseHelper;
 import io.ehdev.android.drivingtime.module.ModuleGetters;
 
 import javax.inject.Inject;
 import java.util.List;
 
 public class InsertRecordDialog extends InsertOrEditRecordDialog {
     private AggregatedDrivingRecordAdapter aggregatedDrivingRecordAdapter;
 
     @Inject
    protected DatabaseHelper dao;
 
     public InsertRecordDialog(Record drivingRecord, List<Task> drivingTaskList, AggregatedDrivingRecordAdapter aggregatedDrivingRecordAdapter) {
         super(drivingRecord, drivingTaskList);
         this.aggregatedDrivingRecordAdapter = aggregatedDrivingRecordAdapter;
     }
 
     @Override
     public void onDismiss(DialogInterface dialog){
         super.onDismiss(dialog);
         ObjectGraph objectGraph = ObjectGraph.create(new ModuleGetters(getActivity()));
         objectGraph.inject(this);
 
         aggregatedDrivingRecordAdapter.replaceDataSet(dao.createDrivingRecordList());
     }
 
 }
