 package org.byu.cs.gen;
 
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.message.BasicNameValuePair;
 import org.byu.cs.gen.global.CheckBoxifiedIndexAdapter;
 import org.byu.cs.gen.global.CheckBoxifiedText;
 import org.byu.cs.gen.global.HttpInterface;
 
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.DialogInterface.OnDismissListener;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.SubMenu;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.Spinner;
 
 /**
  * This class provides the interface for displaying the results of a search.
  * When the class is loaded, it creates a new thread which performs the search and
  * updates the results screen when the search is complete.
  * Items from the results can then be selected and put into a new or existing todo list.
  * @author Scott Slaugh & Seth Dickson
  *
  */
 public class SearchResults extends ListActivity {
 	
 	private SearchResults instanceRef = this;
 	private Intent this_intent;
 	private Context context = this;
 	private Bundle extras;
 	
 	private Intent searchIntent;
 	private Intent homeScreenIntent;
 	private Intent todoIntent;
 	private Intent show_list;
 	
 	private CheckBoxifiedIndexAdapter cbla;
 	private String search_id;
 	private String searchResults;
 	private String listsResults;
 	private String list_id;
 	private String list_name;
 	private String todo_lists[];
 	private ArrayList<String> list_pos_id;
 	private List<NameValuePair> nameValuePairs;
 	private Spinner listSpinner;
 	ArrayAdapter<String> adapter;
 	private EditText mCreateListText;
 	private ArrayList<Integer> searches;
 	
 	static final int DIALOG_NEW_LIST = 0;
 	
 	public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.checklist);  
         init(savedInstanceState);
 	}
 	
 	protected void onNewIntent(Intent intent){
 		setIntent(intent);
 		init(null);
 	}
 	
 	private void init(Bundle savedInstanceState){
         this_intent = getIntent();
         extras = this_intent.getExtras();
         
         searchIntent = new Intent(this, SearchResults.class); //Use this Intent for any searches, just change the extras.
         homeScreenIntent = new Intent(this, TwentyMinuteGen.class);
         todoIntent = new Intent(this, Todo.class);
         show_list = new Intent(this, ShowList.class);
 
         cbla = new CheckBoxifiedIndexAdapter(this);
         listSpinner = (Spinner) findViewById(R.id.spinner);
         listSpinner.setScrollBarStyle(View.SCROLLBARS_INSIDE_INSET);
        listSpinner.setScrollbarFadingEnabled(true);
         list_pos_id = new ArrayList<String>();
         nameValuePairs = new ArrayList<NameValuePair>(2); 
         list_id = null;
         list_name = null;
         searches = extras.containsKey("searches") ? extras.getIntegerArrayList("searches") : new ArrayList<Integer>();
 
         if(savedInstanceState==null)
 	        new SearchLoader().execute();
         else{
         	Log.i("twenty", "savedStateRestored");
         	parseResults(savedInstanceState.getString("results"));
         	parseLists(savedInstanceState.getString("lists"));
         	search_id = savedInstanceState.getString("search_id");
         	cbla.makeIndex();
     		setListAdapter(cbla);
             listSpinner.setAdapter(adapter);
         }
 		
         ListView lv = getListView();
         registerForContextMenu(lv);
     	lv.setTextFilterEnabled(false);
     	lv.setFastScrollEnabled(true);
     	
         Button addToListButton = (Button) findViewById(R.id.floating_button);
         addToListButton.setOnClickListener(new View.OnClickListener() {
         	public void onClick(View view) {
         		nameValuePairs.clear();
         		ArrayList<CheckBoxifiedText> selected = cbla.getSelectedItems();
 				Iterator<CheckBoxifiedText> iter = selected.iterator();
 				// Add your data  
 				while(iter.hasNext()){
 					CheckBoxifiedText item = iter.next();
 					if(item.getChecked() && !item.isParent())
 						nameValuePairs.add(new BasicNameValuePair(item.getPersonId().toString(), item.getText().toString())); 
 				}
         	    int position = listSpinner.getSelectedItemPosition(); //Check here to see if New list...
         	    if(position==listSpinner.getCount() - 1){
         	    	showDialog(DIALOG_NEW_LIST);
         	    }
         	    else{
         	    	list_id = list_pos_id.get(position);
         	    	list_name = listSpinner.getSelectedItem().toString();
         	    	insert_people();
         	    }
         	}
         });   		
 	}
 	@Override
 	protected void onSaveInstanceState(Bundle outState){
         super.onSaveInstanceState(outState);
         outState.putString("results", searchResults);
 		outState.putString("lists", listsResults);
 		outState.putString("search_id", search_id);
 	}
 	
 	/**
 	 * This class performs a search displays the results in a checklist.
 	 * @author Scott Slaugh & Seth Dickson
 	 */
 	private class SearchLoader extends AsyncTask<Void,Void,String> {
 		private ProgressDialog searchProgress;
 		
 		@Override
 		public void onPreExecute() {
 			//Display an indeterminate progress dialog while the search is performed.
 			String dialog = "Searching . .";
 			searchProgress = ProgressDialog.show(instanceRef, "", dialog); 
 		}
 
 		@Override
 		protected String doInBackground(Void... arg0) {
 			
 			try {
 				search_id = extras.get("search_id").toString();
 				//Call the search endpoint on the webservice.
 				HttpResponse response = HttpInterface.getInstance().executeGet("family/search.json?search_id=" + search_id);
 				String httpResult = HttpInterface.getResponseBody(response);
 				searchResults = httpResult;
 				parseResults(httpResult);
 				
 				response = HttpInterface.getInstance().executeGet("todo/lists.json");
 				httpResult = HttpInterface.getResponseBody(response);
 				listsResults = httpResult;
 				parseLists(httpResult);
 				
 			} catch (Exception e) {
 				Log.e("Error", "Problem with HttpClient!", e);
 			}
 			return null;
 		}
 		
 		@Override
 		protected void onPostExecute(String result) {
 			//Get rid of the progress dialog.
 			searchProgress.dismiss();
 			cbla.makeIndex();
 			setListAdapter(cbla);
 	        listSpinner.setAdapter(adapter);
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	public void parseResults(String httpResult){
 		searchResults = httpResult;
 		Log.i("twenty", "Search result: " + httpResult); 
 		cbla = new CheckBoxifiedIndexAdapter(this);
 		//Create a map object from the result.
 		List<String> parseResult = (List<String>)HttpInterface.parseJSON(httpResult);
 		//Iterate over every person in the list
 		int index = 0;
 		for (String cPerson : parseResult) {
 			Map<String,Object> personInfo = (Map<String,Object>)HttpInterface.parseJSON(cPerson);
 			String personId = personInfo.get("id").toString();
 			//Get the person's full name
 			String personName = (personInfo.get("name")!=null) ? personInfo.get("name").toString() : personId;
 			
 			cbla.addItem(new CheckBoxifiedText(personName, personId, "parent", false), index++);
             List<String> parseTasks = (List<String>)personInfo.get("tasks");
             //Add a list of tasks for this person
             for (String cTask : parseTasks) {
             	Map<String,Object> taskInfo = (Map<String,Object>)HttpInterface.parseJSON(cTask);
                 cbla.addItem(new CheckBoxifiedText(taskInfo.get("name").toString(), personId, "child",false), index++);
             }
 		}
 	}
 	/**Request the current todo lists for this user to display **/
 	@SuppressWarnings("unchecked")
 	public void parseLists(String httpResult){
 		listsResults = httpResult;
 		//Create a map object from the result.
 		List<String> parseResult = (List<String>)HttpInterface.parseJSON(httpResult);
 
 		int size = (parseResult.isEmpty()) ? 1 : parseResult.size() + 1;
         todo_lists=new String[size];
         
 		if (!list_pos_id.isEmpty())
 			list_pos_id.clear();
 		//Iterate over every person in the list
 		int index=0;
 		for (String cList : parseResult) {
 			Log.i("twenty","cList: " + cList);
 			Map<String,Object> listInfo = (Map<String,Object>)HttpInterface.parseJSON(cList);
 			//Get the list name
 			String listName = listInfo.get("name").toString();
 			if (listName.compareTo("completed_tasks")==0)
 				continue;
 
 			todo_lists[index]=listName;
 			list_pos_id.add(listInfo.get("id").toString());
 			index++;
 		}
 		todo_lists[index] = "New list...";		
 		
         adapter = new ArrayAdapter<String>(instanceRef, android.R.layout.simple_spinner_item, todo_lists);
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 	}
 
 	/** 
 	 * Create a Dialog for entering a name for a new list
 	 */
 	protected Dialog onCreateDialog(int id) {
 		final Dialog dialog;
     	
 		switch(id) {
 	    case DIALOG_NEW_LIST:
 	    	dialog = new Dialog(context);
 	    	dialog.setContentView(R.layout.new_list_dialog);
 	    	dialog.setTitle("Create a new list...");
 	    	dialog.setOnDismissListener(new OnDismissListener(){
 				@Override
 				public void onDismiss(DialogInterface arg0) {
 					if(list_id!=null)
 						insert_people();
 				}
 	    	});
 	    	
 	        Button createListButton = (Button) dialog.findViewById(R.id.create);
 	        mCreateListText = (EditText) dialog.findViewById(R.id.create_list);
 	        createListButton.setOnClickListener(new View.OnClickListener() {
 	        	@SuppressWarnings("unchecked")
 				public void onClick(View view) {
 		            // Add your data  
 	        		if(mCreateListText.getText().length()==0)
 	        			return;
 		            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);  
 		            nameValuePairs.add(new BasicNameValuePair("name", mCreateListText.getText().toString())); 
 					try {
 						HttpResponse response = HttpInterface.getInstance().executePost("todo/lists.json", nameValuePairs);
 						String httpResult = HttpInterface.getResponseBody(response);	
 						Log.i("twenty", "/lists response: "+httpResult);
 						Map<String,Object> listInfo = (Map<String,Object>)HttpInterface.parseJSON(httpResult);	
 						list_id = listInfo.get("new_list_id").toString();
 						list_name = mCreateListText.getText().toString();
 					} catch (ClientProtocolException e) {
 						e.printStackTrace();
 					} catch (IOException e) {}
 					mCreateListText.setText("");
 					dialog.dismiss();
 	        	}
 	        }); 	    	
 	        break;
 	    default:
 	        dialog = null;
 	    }
 	    return dialog;
 	}	
 
 	/**
 	 * Send a request to twenty to insert the selected people/tasks into the chosen todo list
 	 */
 	private void insert_people(){
 	    nameValuePairs.add(new BasicNameValuePair("todo_list", list_id));
 		try {
 			HttpResponse response = HttpInterface.getInstance().executePost("family/insert_people.json", nameValuePairs);
 			HttpInterface.getResponseBody(response);
     	    show_list.putExtra("list_id", list_id);
     	    show_list.putExtra("list_name", list_name);
     	    startActivity(show_list);				
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 		} catch (IOException e) {}		
 	}	
 	
     @SuppressWarnings("unchecked")
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
     	SubMenu sbmenu = menu.addSubMenu("Searches");
     	sbmenu.setIcon(R.drawable.suggestion);
 		String httpResult = "";
 		try {
 			HttpResponse response = HttpInterface.getInstance().executeGet("family/searches.json");
 			httpResult = HttpInterface.getResponseBody(response);
 
 		} catch (IOException e) { e.printStackTrace(); }
 		
 		List<String> parseResult = (List<String>)HttpInterface.parseJSON(httpResult);
 		for (String cSearch : parseResult) {
 			Map<String,Object> searchInfo = (Map<String,Object>)HttpInterface.parseJSON(cSearch);
 			String search_name = searchInfo.get("name").toString();
 			int search_id = Integer.parseInt(searchInfo.get("id").toString());
 			
 			sbmenu.add(Menu.NONE, search_id, search_id, search_name);
 			searches.add(search_id);
 		}
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.genmenu, menu);
         return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item){
         switch (item.getItemId()) {
         	case R.id.home:
         		startActivity(homeScreenIntent);
         		return true;
 	        case R.id.todo:
 	        	startActivity(todoIntent);
 	            return true;        		
         }
         if(searches.contains(item.getItemId())){
         	if(search_id.compareTo(Integer.toString(item.getItemId()))==0)
         		return true;
 			searchIntent.putExtra("progress_dialog", "Searching . .");
 			searchIntent.putExtra("search_id", item.getItemId());
 			searchIntent.putExtra("searches", searches);
 			startActivity(searchIntent);
 			return true;
         }
     	return super.onOptionsItemSelected(item);
     }     
 	
 }
