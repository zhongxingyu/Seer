 package com.CMPUT301F12T07.crowdsource.tabviews;
 
 import java.util.List;
 
 import com.CMPUT301F12T07.crowdsource.R;
 import com.CMPUT301F12T07.crowdsource.taskmodeldb.DBHandler;
 import com.CMPUT301F12T07.crowdsource.taskmodeldb.Task;
 import com.CMPUT301F12T07.crowdsource.taskmodeldb.TaskLoadHandler;
 
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.provider.Settings.Secure;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.ListView;
 
 public class FeedSectionFragment extends Fragment {
 	
 	private ListView myList;
 	private ListView randomTask;
 	private List<Task> tasks;
 	private List<Task> randTask;
 	private DBHandler db;
 	
     public FeedSectionFragment() {
     }
 
     public static final String ARG_SECTION_NUMBER = "0";
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
             Bundle savedInstanceState) {
     	
     	View myFeed = inflater.inflate(R.layout.activity_feed, container, false);
     	
     	db = new DBHandler(inflater.getContext());
         
         myList = (ListView) myFeed.findViewById(R.id.tasklist);
         
         // Adds listener for when a Task is clicked in the ListView
         myList.setOnItemClickListener(new OnItemClickListener() {
         	public void onItemClick(AdapterView<?> parent, View view, int position, long id){
         		Intent intent = new Intent(view.getContext(), TaskLoadHandler.class);
         		// TODO: dateCreate = 0
        		if (!tasks.get(position).get_dateCreate().equals("0")) {
         			intent.putExtra("taskLocalObject", tasks.get(position).get_tid());
         		} else {
         			intent.putExtra("taskWebObject", tasks.get(position).get_wid());
         		}
         		startActivity(intent);
         	}
         });
         
         // TODO: MAKE THIS WORK
 //        this.randTask = db.getRandomTask(Secure.getString(inflater.getContext().getContentResolver(), Secure.ANDROID_ID));
 //        randomTask = (ListView) myFeed.findViewById(R.id.randomTask);
 //        randomTask.setBackgroundColor(Color.GRAY);
 //        randomTask.setAdapter(new TaskListAdapter(inflater.getContext(), randTask));
 //        randomTask.setOnItemClickListener(new OnItemClickListener() {
 //        	public void onItemClick(AdapterView<?> parent, View view, int position, long id){
 //        		Intent intent = new Intent(view.getContext(), TaskLoadHandler.class);
 //        		if (randTask.get(position).get_dateCreate() != null) {
 //        			intent.putExtra("taskLocalObject", tasks.get(position).get_tid());
 //        		} else {
 //        			intent.putExtra("taskWebObject", tasks.get(position).get_wid());
 //        		}
 //        		startActivity(intent);
 //        	}
 //        });
     	
     	
         
         return myFeed;
     }
     
     @Override
 	public void onResume() {
 		super.onResume();
 		this.tasks = db.getAllTasks();
 		myList.setAdapter(new TaskListAdapter(getActivity(), tasks));
 	}
 }
