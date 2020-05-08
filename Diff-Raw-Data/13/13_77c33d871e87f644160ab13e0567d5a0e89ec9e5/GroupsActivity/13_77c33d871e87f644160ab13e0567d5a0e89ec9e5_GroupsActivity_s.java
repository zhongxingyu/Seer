 package com.example.repbase.activities;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.TimeoutException;
 
 import org.json.JSONException;
 
 import com.example.repbase.Common;
 import com.example.repbase.DBInterface;
 import com.example.repbase.PaintedSimpleAdapter;
 import com.example.repbase.R;
 import com.example.repbase.classes.GroupWithJSONSkills;
 import com.example.repbase.classes.SessionState;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class GroupsActivity extends ListActivity implements OnClickListener{
 //TODO: try to add buttons directly to llHeader (currently view element is used)
 //	private TextView test;
 	
 //	private final String ATTRIBUTE_GROUP_ID = "gid";
 //	private final String ATTRIBUTE_GROUP_NAME = "gname";
 //	private final String ATTRIBUTE_ACC_CODE = "accCode";
 	
 //	private ListView lvGroups;
 	private Button btnEnterIntoGroup;
 	private Button btnCreateGroup;
 	private TextView tvGreeting;
 	private LinearLayout llHeader;
 
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_custom_list);
 		refresh();
 //			lvGroups.addHeaderView(vHeader);
 			
 			
 //			ArrayList<Map<String, Object>> data = new ArrayList<Map<String, Object>>(
 //					listGroups.size());
 //			Map<String, Object> m;
 //			for (int i = 0; i < listGroups.size(); i++) {
 //				m = new HashMap<String, Object>();
 //				m.put(ATTRIBUTE_GROUP_ID, listGroups.get(i).getID());
 //				m.put(ATTRIBUTE_GROUP_NAME, listGroups.get(i).getName());
 //				m.put(ATTRIBUTE_ACC_CODE, listGroups.get(i).getAccessCode());
 //				data.add(m);
 //			}
 //		    
 //		    String[] from = new String[] {ATTRIBUTE_GROUP_NAME, ATTRIBUTE_ACC_CODE};
 //		    int[] to = new int[] {R.id.tvGroupName, R.id.tvAccCode};
 //		    
 //			PaintedSimpleAdapter sAdapter = new PaintedSimpleAdapter(this, data,
 //					R.layout.grouplist_item, from, to);
 //
 //		    lvGroups = (ListView) findViewById(R.id.lvGroups);
 //		    lvGroups.setAdapter(sAdapter);
 //		    
 //		    lvGroups.setOnItemClickListener(new OnItemClickListener() {
 ////		        @Override
 //		        public void onItemClick(AdapterView<?> list, View view, int position, long id) {
 //		        	Map<String, Object> m = (Map<String, Object>)list.getItemAtPosition(position);
 //		        	Log.i(Common.TEMP_TAG, "onListItemClick: " + position + "; " + m.get(ATTRIBUTE_GROUP_ID));
 //		        	
 //		        	Intent intent = new Intent(getApplicationContext(), SingleGroupActivity.class);
 //		        	intent.putExtra("groupId",  (Integer)m.get(ATTRIBUTE_GROUP_ID));
 //		        	startActivity(intent);
 //		        	}
 //
 //		        });
 			
 //			lvGroups = (ListView)findViewById(R.id.lvGroups);
 			
 
 //			lvGroups.setAdapter(adapter);			
 			
 //			lvGroups.setOnItemClickListener(new OnItemClickListener() {
 //
 //				public void onItemClick(AdapterView<?> list, View view,
 //						int position, long id) {
 //					GroupWithJSONSkills g = (GroupWithJSONSkills)list.getItemAtPosition(position);
 //		        	Intent intent = new Intent(getApplicationContext(), SingleGroupActivity.class);
 //		        	intent.putExtra("groupId",  g.getId());
 //		        	startActivity(intent);
 //				}
 //			});
 	}
 
 	@Override
 	protected void onRestart() {
 		super.onRestart();
 		refresh();
 	}
 
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		GroupWithJSONSkills g = (GroupWithJSONSkills) l.getItemAtPosition(position);
 		Intent intent = new Intent(this, SingleGroupActivity.class);
 		intent.putExtra("groupId", g.getId());
 		startActivity(intent);
 	}
 	
 //	@Override
 	public void onClick(View v) {
 		Intent intent = null;
 		switch (v.getId()) {
 			case  R.id.btnEnterIntoGroup:
 				intent = new Intent(this, AllGroupsListActivity.class);
 				break;
 			case R.id.btnCreateGroup:
 				intent = new Intent(this, CreateNewGroupActivity.class);
 				break;
 		}
 		startActivity(intent);
 	}
 	
 	private void refresh() {
 		try {
			// list with user's groups
			List<GroupWithJSONSkills> lGroups = new ArrayList<GroupWithJSONSkills>();
			// lvGroups = getListView();
 
 			// create header for the list from xml
 			// inflate EnterIntoGroup button
 			View vHeader = getLayoutInflater().inflate(
 					R.layout.activity_groups_header, null);
 			btnEnterIntoGroup = (Button) vHeader
 					.findViewById(R.id.btnEnterIntoGroup);
 			btnEnterIntoGroup
 					.setText(getString(R.string.btnEnterIntoGroupText));
 			btnEnterIntoGroup.setOnClickListener(this);
 
 			// inflate CreateGroup button
 			btnCreateGroup = (Button) vHeader.findViewById(R.id.btnCreateGroup);
 			btnCreateGroup.setText(getString(R.string.btnCreateNewGroupText));
 			btnCreateGroup.setOnClickListener(this);
 
 			// set text for case when user doesn't have groups
 			String greating = getString(R.string.tvGroupsGreetingTextFail);
 			// check user's groups
 			if (SessionState.currentUser.getGroupsIDList().size() != 0) {
 				// fill list
 				for (int groupId : SessionState.currentUser.getGroupsIDList()) {
 					GroupWithJSONSkills group = new GroupWithJSONSkills(groupId);
 					if (!group.getDelStatus())
 						lGroups.add(group); // add only groups witch wasn't
 											// deleted
 				}
 				// listGroups doesn't contain deleted groups!
 				if (lGroups.size() != 0) {
 					ArrayAdapter<GroupWithJSONSkills> adapter = new ArrayAdapter<GroupWithJSONSkills>(
 							this, android.R.layout.simple_list_item_1, lGroups);
 					setListAdapter(adapter);
 					// set greeting text: "You exist in those groups:"
 					greating = getString(R.string.tvGroupsGreetingText);
				}
 			}
 
 			tvGreeting = (TextView) vHeader.findViewById(R.id.tvGroupsGreeting);
 			tvGreeting.setText(greating);
 
 			llHeader = (LinearLayout) findViewById(R.id.llHeader);
 			llHeader.removeAllViews();
 			llHeader.addView(vHeader);
 		} catch (Exception e) {
 			Log.d(Common.EXC_TAG, this.getClass().getName(), e);
 			Common.ShowMessageBox(this, e.getMessage());
 		}
 	}
 }
