 package com.amca.android.replace.user;
 
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
 import com.actionbarsherlock.app.SherlockListActivity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.Toast;
 import com.amca.android.replace.R;
 import com.amca.android.replace.http.HTTPTransfer;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 public class UserForm extends SherlockListActivity {
 
 	static final int USER_FORM_DETAIL_REQUEST = 1;
 	private String mode = null;
 	private List<String> dataList = new ArrayList<String>();
 	private List<String> attributeList = new ArrayList<String>();
 	private ProgressBar progressBar;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_user_form);
 		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
 
 		progressBar = (ProgressBar) findViewById(R.id.progressBar1);
 		progressBar.setVisibility(View.VISIBLE);
 
 		Intent intent = getIntent();
 		mode = intent.getStringExtra("mode");
 
 		if (mode.equals("register")) {
 			setTitle("Registration");
 		}
 		attributeList.add("Username");
 		attributeList.add("Password");
 		attributeList.add("Alias");
 		attributeList.add("Favourite Foods");
 		attributeList.add("Favourite Drinks");
 		attributeList.add("Favourite Books");
 		attributeList.add("Favourite Movies");
 		attributeList.add("Gender");
 		attributeList.add("Occupation");
 		attributeList.add("Date of Birth");
 
 		for (String s : attributeList) {
 			dataList.add("null");
 		}
 
 		if (mode.equals("update")) {
 			String userId = intent.getStringExtra("userId");
 			progressBar = (ProgressBar) findViewById(R.id.progressBar1);
 			HashMap<String, String> data = new HashMap<String, String>();
 			data.put("userId", userId);
 			HTTPUserForm http = new HTTPUserForm();
 			http.setContext(UserForm.this);
 			http.setData(data);
 			http.execute("authenticate/detail/");
 		} else {
 			progressBar.setVisibility(View.INVISIBLE);
 		}
 
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
 				android.R.layout.simple_list_item_1, attributeList);
 		setListAdapter(adapter);
 	}
 
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		Intent intent = new Intent(UserForm.this, UserFormDetail.class);
 		intent.putExtra("data", dataList.get(position));
 		intent.putExtra("attribute", getListAdapter().getItem(position)
 				.toString());
 		intent.putExtra("mode", this.mode);
 		startActivityForResult(intent, USER_FORM_DETAIL_REQUEST);
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode,
 			Intent intent) {
 		if (requestCode == USER_FORM_DETAIL_REQUEST) {
 			if (resultCode == RESULT_OK) {
 				Bundle extras = intent.getExtras();
 				dataList.set(
 						attributeList.indexOf(extras.getString("attribute")),
 						extras.getString("data"));
 			}
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuItem menuSave = menu.add("Save");
 		menuSave.setIcon(R.drawable.content_save);
 		menuSave.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
 				| MenuItem.SHOW_AS_ACTION_WITH_TEXT);
 		menuSave.setOnMenuItemClickListener(new OnMenuItemClickListener() {
 			@Override
 			public boolean onMenuItemClick(final MenuItem item) {
 				HashMap<String, String> data = new HashMap<String, String>();
 				data.put("userName", dataList.get(0));
 				data.put("userPassword", dataList.get(1));
 				data.put("userAlias", dataList.get(2));
 				data.put("userFoods", dataList.get(3));
 				data.put("userDrinks", dataList.get(4));
 				data.put("userBooks", dataList.get(5));
 				data.put("userMovies", dataList.get(6));
 				data.put("userGender", dataList.get(7));
 				data.put("userOccupation", dataList.get(8));
 				data.put("userDOB", dataList.get(9));
 
 				HTTPUserForm http = new HTTPUserForm();
 				http.setContext(UserForm.this);
 				http.setData(data);
 				http.execute("authenticate/" + mode + "/");
 				return true;
 			}
 		});
 		return true;
 	}
 
 	class HTTPUserForm extends HTTPTransfer {
 		@Override
 		protected void onPostExecute(String result) {
 			super.onPostExecute(result);
 			System.out.println(result);
 			if (mode.equals("register")) {
				if (result.equals("OKE")) {
 					finish();
 				} else {
 					Toast.makeText(getApplicationContext(),
 							"profile already exist", Toast.LENGTH_SHORT).show();
 				}
 				return;
 			}
 
 			try {
 				JSONArray jArray = new JSONArray(result);
 				JSONObject json_data = jArray.getJSONObject(0);
 				dataList.set(0, json_data.getString("userName"));
 				dataList.set(1, json_data.getString("userPassword"));
 				dataList.set(2, json_data.getString("userAlias"));
 				dataList.set(3, json_data.getString("userFoods"));
 				dataList.set(4, json_data.getString("userDrinks"));
 				dataList.set(5, json_data.getString("userBooks"));
 				dataList.set(6, json_data.getString("userMovies"));
 				dataList.set(7, json_data.getString("userGender"));
 				dataList.set(8, json_data.getString("userOccupation"));
 				dataList.set(9, json_data.getString("userDOB"));
 			} catch (JSONException e) {
 				System.out.println("Error parsing data " + e.toString());
 			}
 			setTitle(dataList.get(2));
 			progressBar.setVisibility(View.INVISIBLE);
 		}
 	}
 
 }
