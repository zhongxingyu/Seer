 package ca.skule.froshapplication;
 
 import android.os.Bundle;
 //import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.View;
 import android.widget.AdapterView;
 import java.util.HashMap;
 import android.widget.ArrayAdapter;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 
 public class ListsActivity extends ListActivity {
 	// Array of all locations used in this application
 	static String[] NameList = new String[] {
 		"Bahen Institute of Technology",
 		"Convocation Hall",
 		"Engineering Annex",
 		"Front Campus",
 		"Galbraith Building",
 		"Lassonde Mining Building",
 		"Mechanical Engineering Building",
 		"Queen's Park",
 		"Sandford Fleming Building",
 		"Wallberg Buildling"	
 	};
 	// HashMap, with keys as location names, and objects as location codes
 	static HashMap<String,String> CodeMap = new HashMap<String,String>();
 	{
 		CodeMap.put("Bahen Institute of Technology","BA");
 		CodeMap.put("Convocation Hall","CH");
 		CodeMap.put("Engineering Annex","EA");
 		CodeMap.put("Front Campus","FC");
 		CodeMap.put("Galbraith Building","GB");
 		CodeMap.put("Lassonde Mining Building","MB");
 		CodeMap.put("Mechanical Engineering Building","MC");
 		CodeMap.put("Queen's Park","QP");
 		CodeMap.put("Sandford Fleming Building","SF");
 		CodeMap.put("Wallberg Buildling","WB");
 	}
 	// Hashmap with keys as location codes, and objects as coordinates in "x,y" format
 	static HashMap<String,String> CoMap = new HashMap<String,String>();
 	{
 		CoMap.put("BA", "325,1040");
 		CoMap.put("CH", "564,979");
 		CoMap.put("EA", "530,1109");
 		CoMap.put("GB", "488,1048");
 		CoMap.put("FC", "630,887");
 		CoMap.put("MB", "694,1160");
 		CoMap.put("MC", "659,1082");
 		CoMap.put("QP", "957,635");
 		CoMap.put("SF", "618,1048");
 		CoMap.put("WB", "530,1163");	
 	}
 private ListView lists;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		final ListView lists = getListView();
 		//Setting the OnClick
 		lists.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 		    @Override
 		    public void onItemClick(AdapterView<?> av, View v, int pos, long id) {
 		        onListItemClick(v,pos,id);
 		    }
 		    protected void onListItemClick(View v, int pos, long id) {
 		    	//Listener Testing
 		    	/*AlertDialog.Builder adb = new AlertDialog.Builder(
 		    			ListsActivity.this);
 		    			adb.setTitle("ListView OnClick");
 		    			adb.setMessage("Selected Item is = "
 		    			+ lists.getItemAtPosition(pos));
 		    			adb.setPositiveButton("Ok", null);
 		    			adb.show();*/
 		    	//Sending Coordinate Data.
 		    	Intent intent = new Intent (v.getContext(), MapActivity.class);
 				intent.putExtra("Co-ordinates", ListsActivity.CoMap.get(ListsActivity.CodeMap.get(lists.getItemAtPosition(pos))));
 				//Testing Intent content
 				/*AlertDialog.Builder adb = new AlertDialog.Builder(
 		    			ListsActivity.this);
 		    			adb.setTitle("ListView OnClick");
 		    			adb.setMessage("Selected Item is = "
 		    			+ intent.getStringExtra("Co-ordinates"));
 		    			adb.setPositiveButton("Ok", null);
 		    			adb.show();*/
 				startActivity(intent);
 		    }
 		});
 		
 		//Setting up the actual ListView
 		ListAdapter adapter = createAdapter();
 		lists.setAdapter(adapter);
 		
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.list, menu);
 		return true;
 	}	
 	protected ListAdapter createAdapter()
 	{
 	ListAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,NameList);
 	return adapter;
 	}
 
 	
 }
