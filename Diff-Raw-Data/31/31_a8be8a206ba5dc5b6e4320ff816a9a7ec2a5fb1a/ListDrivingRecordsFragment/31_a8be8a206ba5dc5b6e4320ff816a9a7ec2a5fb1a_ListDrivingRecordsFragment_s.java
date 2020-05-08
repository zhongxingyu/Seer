 package io.ehdev.android.drivingtime.view.fragments;
 
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import com.actionbarsherlock.app.SherlockFragment;
 import io.ehdev.android.drivingtime.R;
 import io.ehdev.android.drivingtime.adapter.DrivingRecordAdapter;
 import io.ehdev.android.drivingtime.backend.model.Record;
 import io.ehdev.android.drivingtime.database.dao.DrivingRecordDao;
 import io.ehdev.android.drivingtime.database.dao.DrivingTaskDao;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class ListDrivingRecordsFragment extends SherlockFragment {
 
     private static final String TAG = ListDrivingRecordsFragment.class.getName();
     private List<Record> drivingRecordList;
     private DrivingRecordAdapter adapter;
 
     private void getAllEntries() {
         try{
             drivingRecordList = new ArrayList<Record>();
             DrivingRecordDao drivingRecordDao = new DrivingRecordDao(getSherlockActivity());
             DrivingTaskDao drivingTaskDao = new DrivingTaskDao(getSherlockActivity());
             List<Record> list = drivingRecordDao.getDao().queryForAll();
             for(Record record : list){
                 drivingTaskDao.getDao().refresh(record.getDrivingTask());
                 drivingRecordList.add(record);
             }
         } catch (Exception e) {
             Log.i(TAG, e.getMessage());
         }
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
     {
         Log.d(TAG, "onCreateView");
         getAllEntries();
         View view = inflater.inflate(R.layout.list_view, null);
         ListView listView = (ListView) view.findViewById(R.id.listOfAllRecords);
         adapter = new DrivingRecordAdapter(getSherlockActivity(), drivingRecordList);
         listView.setAdapter(adapter);
         listView.setSelector(R.drawable.custom_selector);
         listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 
             @Override
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                getSherlockActivity().startActionMode(new EditDeleteActionMode(adapter));
                adapter.setSelected(position);
             }
         });
 
         return view;
 
     }
 }
