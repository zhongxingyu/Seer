 package edu.sru.andgate.bitbot.missionlist;
 
 import java.util.ArrayList;
 import java.util.Hashtable;
 
 import edu.sru.andgate.bitbot.R;
 import android.app.ListActivity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class MissionListActivity extends ListActivity {
 	// private ProgressDialog m_ProgressDialog = null; 
 	 private ArrayList<CustomListView> myMissions = null;
 	 private missionListAdapter mission_adapter;
 	 //private Runnable viewOrders;
 	 final Hashtable<String, String> mission_list = new Hashtable<String, String>();
 	 public void onCreate(Bundle savedInstanceState) {
 		 super.onCreate(savedInstanceState);
 	        setContentView(R.layout.mission_main);
 	        myMissions = new ArrayList<CustomListView>();
 	        this.mission_adapter = new missionListAdapter(this, R.layout.mission_row, myMissions);
 	        setListAdapter(this.mission_adapter);
 	       
 	        getMissions();
 	        run();
 	        
 	        
 	 }
 
 	 @Override
 	 protected void onListItemClick(ListView l, View v, int position, long id) {
 		 	Intent myIntent = new Intent(MissionListActivity.this, MissionBriefingActivity.class);
         	myIntent.putExtra("Filename",  mission_list.get(v.getTag().toString()));
         	startActivity(myIntent);
 	 }
 	 
 	 public void run() {
 	            if(myMissions != null && myMissions.size() > 0){
 	                mission_adapter.notifyDataSetChanged();
 	                for(int i=0;i<myMissions.size();i++)
 	                mission_adapter.add(myMissions.get(i));
 	            }
 	 }
 	       
 	 private void getMissions(){
 	          try{
 	              myMissions = new ArrayList<CustomListView>();
 	              CustomListView mission1 = new CustomListView();
 	              mission1.setMissionName("Arena");
 	              mission1.setMissionDescription("This dont mean shit");
	              mission1.setFileName(null);
 	              CustomListView mission2 = new CustomListView();
 	              mission2.setMissionName("Target Practice");
 	              mission2.setMissionDescription("This dont mean shit");
 	              mission2.setFileName("target_practice.xml");
 	              mission_list.put(mission2.getMissionName(), mission2.getFileName());
 	              myMissions.add(mission1);
 	              myMissions.add(mission2);
 	            } catch (Exception e) { 
 	              Log.e("BACKGROUND_PROC", e.getMessage());
 	            }
 	        }
 }
