 package com.caa.ui;
 
 import java.util.ArrayList;
 
 import android.app.ListActivity;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 import com.caa.bspace.R;
 
 public class FilesViewActivity extends ListActivity
 {
 	ArrayAdapter<String> adapter;
 	ArrayList<String> listItems=new ArrayList<String>();
 	
 	public void onCreate(Bundle savedInstanceState) {
 		  super.onCreate(savedInstanceState);
 
		  
 		  adapter= new ArrayAdapter<String>(this, R.layout.list_item, listItems);
 		  setListAdapter(adapter);
 
 		  ListView lv = getListView();
 		  lv.setTextFilterEnabled(true);
 		  
 		  lv.setOnItemClickListener(new OnItemClickListener() {
 			    public void onItemClick(AdapterView<?> parent, View view,
 			        int position, long id) {
 			    	 listItems.add("Jay");
 			    	 adapter.notifyDataSetChanged();
 			    }
 			  });
 	}
 }
