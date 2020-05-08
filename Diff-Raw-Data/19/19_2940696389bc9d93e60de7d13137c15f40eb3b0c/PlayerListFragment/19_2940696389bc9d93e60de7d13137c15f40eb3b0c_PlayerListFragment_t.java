 package edu.mines.msmith1.universepoint;
 
 import android.app.Activity;
 import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
 import android.view.View;
import android.view.ViewGroup;
 import android.widget.ListView;
 
 public class PlayerListFragment extends ListFragment {
 	ListItemSelectedListener playerSelectedListener;
 	
 	public interface ListItemSelectedListener {
 		public void listItemSelected(int position);
 	}
 	
 	 @Override
 	 public void onAttach(Activity activity) {
 		 super.onAttach(activity);
 		 
 		 try {
 			 playerSelectedListener = (ListItemSelectedListener) activity;
 		 } catch (ClassCastException e) {
 			 throw new ClassCastException(activity.toString() + " must implement ListItemSelectedListener");
 		 }
 	 }
 	 
	 @Override
	 public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		 return inflater.inflate(R.layout.player_list, container, false);
		 
	 }
	 
 	 //Send info back to GameRunner class
 	 @Override
 	 public void onListItemClick(ListView l, View v, int position, long id) {
 		 playerSelectedListener.listItemSelected(position);
 	 }
 }
