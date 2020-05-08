 
 package edu.crabium.android.GUI;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import edu.crabium.android.IMissData;
 import edu.crabium.android.R;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.Window;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.View.OnCreateContextMenuListener;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 ;
 
 public class SelectedGroupMemberActivity extends Activity {
 	LinearLayout	SelectedGroupMemberLinearLayout;
     private ListView SelectedGroupMemberListView;
     private Button BackButton;
     
     private static String group_name;
 	private static final String SelectedGroupMemberColumn1 = "title";
 	private static final String SelectedGroupMemberColumn2 = "content";
 	private SimpleAdapter adapter;
 	List<Map<String,String>> SelectedGroupMemberDisplay = new ArrayList<Map<String, String>>();
 	
     public void onCreate(Bundle savedInstanceState) {
     	requestWindowFeature(Window.FEATURE_NO_TITLE);
         super.onCreate(savedInstanceState);
         setContentView(R.layout.selected_group_member);
         
         Bundle bundle = this.getIntent().getExtras();
         group_name = bundle.getString("group_name");
         SelectedGroupMemberLinearLayout = (LinearLayout) findViewById(R.id.selected_group_member_linearlayout);
         SelectedGroupMemberListView = (ListView) findViewById(R.id.selected_group_member_listView);
 		setContentView(SelectedGroupMemberLinearLayout);
         
 		getGroups(SelectedGroupMemberDisplay);
 		
 		final String[] from = {SelectedGroupMemberColumn1, SelectedGroupMemberColumn2};
 		int[] to = {android.R.id.text1, android.R.id.text2};
 		adapter = new SimpleAdapter(this, SelectedGroupMemberDisplay,android.R.layout.simple_list_item_2, from, to);
 		SelectedGroupMemberListView.setAdapter(adapter);
 		SelectedGroupMemberListView.setItemsCanFocus(true); 
 		SelectedGroupMemberListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE); 
 
 		
 		SelectedGroupMemberListView.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {         
             @Override   
             public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) { 
             	menu.setHeaderTitle("  "); 
                 menu.add(0, Menu.FIRST, 0, "删除");  
             }   
         });  
 		
         BackButton = (Button)findViewById(R.id.back_button);
         BackButton.setOnClickListener(new Button.OnClickListener() {
             public void onClick(View v) {
                 Intent intent = new Intent(SelectedGroupMemberActivity.this, SetReplyActivity.class);
                 startActivity(intent);
                 SelectedGroupMemberActivity.this.finish();
             }
         });	
     }
 
     public boolean onContextItemSelected(MenuItem item) {
     	AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo)item.getMenuInfo(); 
     	
     	if (item.getItemId() == Menu.FIRST) {
     		int pos = (int) SelectedGroupMemberListView.getAdapter().getItemId(menuInfo.position);
 			@SuppressWarnings("unchecked")
     		//Map<String,String> map = (Map<String, String>)SelectedGroupMemberListView.getItemAtPosition(pos);
     		Map<String, String> pair = SelectedGroupMemberDisplay.get(menuInfo.position);
     		Log.d("HELLO", pair.get(SelectedGroupMemberColumn1) + pair.get(SelectedGroupMemberColumn2));
     		
     		IMissData.delPersonInGroup(pair.get(SelectedGroupMemberColumn1),pair.get(SelectedGroupMemberColumn2), group_name);
     		SelectedGroupMemberActivity.this.finish();
 			Intent intent = new Intent(SelectedGroupMemberActivity.this, SelectedGroupMemberActivity.class);
 			Bundle bundle = new Bundle();
 			bundle.putString("group_name", group_name);
 			intent.putExtras(bundle);
 			startActivity(intent);
 			
     	} 
         return super.onContextItemSelected(item);   
     } 
     
     private void getGroups(List<Map<String,String>> to){
    	Map<String, String> map = IMissData.getPersonsFromGroup(group_name);
    	Set<String> keys = map.keySet();
    	
    	for(String key : keys){
         	Map<String, String> item = new HashMap<String, String>();
        	item.put(SelectedGroupMemberColumn1, key);
        	item.put(SelectedGroupMemberColumn2, map.get(key));
         	to.add(item);
     	}	
     }
 }
 
