 package com.jacob.patton.bereaconvo;
 
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.Toast;
 import com.actionbarsherlock.app.SherlockListFragment;
 
 // This uses SherlockListFragment to implement the list. 
 public class MenuFragment extends SherlockListFragment{
 
 	
 	// setting the interface variable. 
 	onArticleSelected mainActivityCall;
 	 
 	// creates adapter for listview. 
 	SimpleAdapter myList2;
 	// list used in teh dapter. 
 	List<HashMap<String, Object>> convodata;
 	
 	// used to check if there is data in the list. 
 	boolean hasData;
 	
 	
 	// MainActivity class must implement this interface so the frag can deliver the message/subroutine
 	public interface onArticleSelected{
 		// these subroutine must be in the MainActivity class.
 		// used to pass the position of the article to display
 		public void displayArticleData(int position);
 		//used to mark the convo as attended. 
 		public void  markConvo(int position);
 				
 	}
 	
 	
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 	 	
 		// creates the list for the adapter. 
 		convodata = new ArrayList<HashMap<String, Object>>();
 		// This maps where the information goes. 
 		String[] from = new String[] {"title", "date", "time","checkmark"};
 		int[] to = new int[] {R.id.lvTitle,R.id.lvDate,R.id.lvTime,R.id.lvCheckmark};
 		// creating the simple adapter. 
 		myList2 = new SimpleAdapter(getActivity(),convodata,R.layout.listview_row,from, to);
 		// setting the adapter
 		setListAdapter(myList2);
 		
 		// find our layout to get access before inflating. 
 		LinearLayout mylayout = (LinearLayout)inflater.inflate(R.layout.fragment_menu, container, false);
 		
 		// Find the list. 
 		ListView list = (ListView)mylayout.findViewById(android.R.id.list);
 		// Enables long click on the list, and what to do. 
 		list.setLongClickable(true);
 	    list.setOnItemLongClickListener(new OnItemLongClickListener() {
 	    	
 	    	// sets the action for long click. 
 	      @Override
 	      public boolean onItemLongClick(AdapterView<?> parent, View view,int position, long id) {
	       	if(hasData == true){
 	       		// currently makes a toast to say clicked, shoudl be chanced to marked.      
 	    	  Toast.makeText(getActivity(),
 	            "Marked as attended",
 	            Toast.LENGTH_SHORT).show();
 	        // Return true to consume the click event. In this case the
 	        // onListItemClick listener is not called anymore.
 	        
 	    	  mainActivityCall.markConvo(position);
 	       	}
 	       	
 	        return true;
 	      }
 	    });
 		
 		
 		//returns the final layout with long clcik enabled. 
 		return mylayout;
 		 
 		 
 }
 	
 
 	
 	public void onAttach(Activity activity){
 		super.onAttach(activity);
 		 // This makes sure that the container activity has implemented
         // the callback interface. If not, it throws an exception.
 		try{
 			mainActivityCall = (onArticleSelected) activity;
 			
 		} catch (ClassCastException e) {
             throw new ClassCastException(activity.toString()
                     + " must implement OnHeadlineSelectedListener");
         }
 	}
 	
 	
 
 	
 
 	public void onListItemClick(ListView l, View v, int position, long id) {
 	  // on click, whichever title you selected, it passes the position to the main activity which passes the correct array. 
		if(hasData == true){
			mainActivityCall.displayArticleData(position);
 		}
	}
 
 
 	
 	
 	public void updateMenu(List<String[]> data){
 		 // clears the data in the list. 
 		 convodata.clear();
 		 
 		 // maps the new convodata with the data it just recieved. 
 	        for(int i = 0; i < data.size(); i++){
 	        	hasData = true;
 	            HashMap<String, Object> map = new HashMap<String, Object>();
 	            map.put("title", data.get(i)[4]);
 	            map.put("date", data.get(i)[3]);
 	            map.put("time", data.get(i)[2]);
 	            if(data.get(i)[7].equals("0")){
 	            	map.put("checkmark", R.drawable.shadow_checkmark);
 	            }
 	            else{
 	            	map.put("checkmark", R.drawable.green_checkmark);
 	            }
 	            convodata.add(map);
 	        }
 		 
 	        if(convodata.isEmpty()){
 	        	hasData = false;
 	        	HashMap<String, Object> map = new HashMap<String, Object>();
 	            map.put("title", "No convos found");
 	            map.put("date", "");
 	            map.put("time", "");
 	            map.put("checkmark", R.drawable.exclamation_mark);
 	            
 	            convodata.add(map);
	            
 	        }
 	        // notified the list that the data has been updated. 
 			myList2.notifyDataSetChanged();
 		}
 		
 }
