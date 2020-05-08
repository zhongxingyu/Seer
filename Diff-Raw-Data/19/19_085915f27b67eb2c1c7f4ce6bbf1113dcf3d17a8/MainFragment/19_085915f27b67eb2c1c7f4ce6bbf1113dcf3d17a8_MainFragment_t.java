 package com.photographers_assistant;
 
 import android.app.ListFragment;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.Toast;
 
 public class MainFragment extends ListFragment {
 	String[] menuItems ={
 			   "Calendar",
 			   "Equipment",
 			   "Contacts",
 			   "Locations",
 			   "Contracts",
 			   "Poses"
 			 };
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		ListAdapter myListAdapter = new ArrayAdapter<String>(
 				getActivity(), android.R.layout.simple_list_item_1, menuItems);
 		setListAdapter(myListAdapter);
 	}
 	
 	@Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         // Inflate the layout for this fragment
         return inflater.inflate(R.layout.main_fragment, container, false);
     }
 	
 	@Override
 	public void onListItemClick(ListView l, View v, int position, long id) {
 		// TODO Auto-generated method stub
 		Toast.makeText(getActivity(),
 				getListView().getItemAtPosition(position).toString(),
 				Toast.LENGTH_LONG).show();
 	}
 }
