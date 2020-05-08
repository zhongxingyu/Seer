 package com.net.rmopenmenu;
 import java.util.ArrayList;
 
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.support.v4.app.ListFragment;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 
 public class SearchList extends ListFragment {
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		Bundle b = this.getArguments();
 		ArrayList<String> combined = b.getStringArrayList("combined");
 		
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item, combined);
 		
 		setListAdapter(adapter);
 	}
 	
 	@Override
 	public void onActivityCreated(Bundle savedState) {
 	    super.onActivityCreated(savedState);
 	    
 	    getListView().setOnItemLongClickListener(new OnItemLongClickListener() {
 
             public boolean onItemLongClick(AdapterView<?> av, View v, int position, long id) {
             		
             	String selectedItem = ((TextView) v).getText().toString();
         		boolean menu = (selectedItem.endsWith("\n\n")? false : true);
         		Toast.makeText(getActivity(), "Added to Favorites", Toast.LENGTH_SHORT).show();
         		
         		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
         		String favorites = prefs.getString((menu? "favoritemenus" : "favoriterests"), "");
        		favorites += (",,, " + selectedItem.replace("\n\n", ""));
         		SharedPreferences.Editor editor = prefs.edit();
         		editor.putString((menu? "favoritemenus" : "favoriterests"), favorites);
         		
         		editor.commit();
         		
                 return true;
             }
         });
 	}
 	
 	@Override
 	public void onListItemClick(ListView l, View v, int position, long id) {
 		Intent myIntent = new Intent(getActivity(), SearchActivity.class);
     	String selectedItem = ((TextView) v).getText().toString();
     	
 		boolean menu = (selectedItem.endsWith("\n\n")? false : true);
 
 		myIntent.putExtra("query", selectedItem.replace("\n\n", "").split("\n")[0].trim());
 		myIntent.putExtra("menu", menu);
 		
 		startActivity(myIntent);
 	}
 }
