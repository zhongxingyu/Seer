 package edu.vt.ece4564.cats_app;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ExecutionException;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.Toast;
 
 public class SelectGroupActivity extends Activity implements OnClickListener {
 
 	private ListView groupListView;
 	private EditText groupNameField;
 	private EditText groupPassField;
 	private Button submitGroupButton;
 	private Button createGroupButton;
 	private String username;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_select_group);
 
 		Intent i = getIntent();
 		username = i.getStringExtra("username");
 
 		groupListView = (ListView) findViewById(R.id.groupListView);
 		groupNameField = (EditText) findViewById(R.id.groupNameBox);
 		groupPassField = (EditText) findViewById(R.id.groupPassBox);
 		submitGroupButton = (Button) findViewById(R.id.submitGroupButton);
 		createGroupButton = (Button) findViewById(R.id.createGroupButton);
 		
 		submitGroupButton.setOnClickListener(this);
 		createGroupButton.setOnClickListener(this);
 
 		String url = "http://chatallthestuff.appspot.com/user/groups?username=" + username;
 		SendRequestTask request = new SendRequestTask();
 		request.execute(url);
 		try {
 			String result = request.get();
 
 			JSONArray j = new JSONArray(result);
 
 			final List<Map<String,String>> places = new ArrayList<Map<String,String>>();
 			for(int k = 0; k < j.length(); k++){
 				JSONObject jo = (JSONObject) j.get(k);
 				Map<String,String> row = new HashMap<String, String>();
 				row.put("id", jo.getString("groupName")); //Big text is group name
 				places.add(row); //TODO small text = date/time of last posting?
 			}
 			if(places.isEmpty()){
 				groupListView.setVisibility(View.GONE);
 			}
 			else{
 				SimpleAdapter a = new SimpleAdapter(
 						this,
 						places,
 						android.R.layout.simple_list_item_2,
 						new String[] {"id", ""},
 						new int[] {android.R.id.text1,android.R.id.text2});
 				groupListView.setAdapter(a);
 				groupListView.setOnItemClickListener(new OnItemClickListener(){
 
 					@Override
 					public void onItemClick(AdapterView<?> arg0, View arg1,
 							int position, long id) {
 						// TODO On click, send to activity that displays posts in group
 						Intent postIntent = new Intent(arg1.getContext(),ShowPostsActivity.class);
 						//postIntent.putExtra("groupName",);
 						postIntent.putExtra("username", username);
 						postIntent.putExtra("groupName",places.get(position).get("id"));
 						startActivity(postIntent);
 						/*Toast toast = Toast.makeText(getApplicationContext(), 
 								"position: " + position,Toast.LENGTH_SHORT);
 						toast.show();*/
 					}
 
 				});
 			}
 
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ExecutionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_select_group, menu);
 		return true;
 	}
 
 	@Override
 	public void onClick(View arg0) {
 		if(arg0.getId() == R.id.submitGroupButton){
 
 			String groupName = groupNameField.getText().toString().trim();
 			String groupPassword = groupPassField.getText().toString();
 
 			String url = "http://chatallthestuff.appspot.com/group/add?groupname=" +
 					groupName + "&password=" + groupPassword + "&username=" + username;
 			SendRequestTask request = new SendRequestTask();
 			request.execute(url);
 			try {
 				String result = request.get();
 				if(result.contains("Valid")){
					//TODO send to group viewing activity for that group
					Toast toast = Toast.makeText(getApplicationContext(), 
							"YaY!",Toast.LENGTH_SHORT);
					toast.show();
 				}
 				else if(result.contains("Duplicate")){
 					Toast toast = Toast.makeText(getApplicationContext(), 
 							"Sorry, you are already a member of that group.",Toast.LENGTH_SHORT);
 					toast.show();
 				}
 				else{
 					Toast toast = Toast.makeText(getApplicationContext(), 
 							"Sorry, you couldn't be added to that group.",Toast.LENGTH_SHORT);
 					toast.show();
 				}
 			} catch (InterruptedException e) {
 				Toast toast = Toast.makeText(getApplicationContext(), 
 						"Something went wrong! Please try again.",Toast.LENGTH_SHORT);
 				toast.show();
 				e.printStackTrace();
 			} catch (ExecutionException e) {
 				Toast toast = Toast.makeText(getApplicationContext(), 
 						"Something went wrong! Please try again.",Toast.LENGTH_SHORT);
 				toast.show();
 				e.printStackTrace();
 			}
 		}
 		else if(arg0.getId() == R.id.createGroupButton){
 			// TODO send to create screen/dialog
 		}
 
 	}
 }
