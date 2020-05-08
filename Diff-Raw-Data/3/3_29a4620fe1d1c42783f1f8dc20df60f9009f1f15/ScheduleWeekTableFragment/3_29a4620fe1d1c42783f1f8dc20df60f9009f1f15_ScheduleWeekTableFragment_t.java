 package ua.edu.tntu.schedule;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ListView;
 
 import java.util.ArrayList;
 
 import ua.edu.tntu.R;
 
 /**
  * Created by Silvestr on 16.11.13.
  */
 public class ScheduleWeekTableFragment extends Fragment {
 
     private String groupName;
     private boolean switchSubGroup;
     private boolean changeWeek;
     private boolean middleOfTheList;
     private ArrayList<ScheduleBlok> scheduleList;
 
     private ScheduleXMLResourceParser scheduleParser;
     private ArrayList<ScheduleBlok> scheduleTempList;
 
 
     private static String TAG = "myLogs";
 
     public ScheduleWeekTableFragment(String groupName, boolean switchSubGroup, boolean changeWeek) {
         this.groupName = groupName;
         this.switchSubGroup = switchSubGroup;
         this.changeWeek = changeWeek;
         this.middleOfTheList = false;
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setHasOptionsMenu(true);
 
         scheduleParser = new ScheduleXMLResourceParser(this.getActivity().getApplicationContext(), this.groupName, this.switchSubGroup);
 
         scheduleList = scheduleParser.getSchedule();
 
         scheduleTempList = new ArrayList<ScheduleBlok>();
 
 
         if (this.changeWeek == true) {
             for (int i = 0; i < scheduleList.size(); i++) {
                 if (scheduleList.get(i).getLecture() == null && scheduleList.get(i).getNameOfDay() == null) {
                     break;
                 } else {
                     scheduleTempList.add(scheduleList.get(i));
                 }
             }
         } else {
             for (int i = 0; i < scheduleList.size(); i++) {
                 if (scheduleList.get(i).getLecture() == null && scheduleList.get(i).getNameOfDay() == null) {
                     middleOfTheList = true;
                     i++;
                 }
                 if (middleOfTheList == true) {
                     scheduleTempList.add(scheduleList.get(i));
                 }
             }
         }
 
         View rootView = inflater.inflate(R.layout.fragment_schedule_table, container, false);
         ListView listView = (ListView) rootView.findViewById(R.id.schedule_item_list_view);
         ScheduleListViewAdapter scheduleListViewAdapter = new ScheduleListViewAdapter(this.getActivity().getApplicationContext(), scheduleTempList);
         listView.setAdapter(scheduleListViewAdapter);
         return rootView;
     }
 }
