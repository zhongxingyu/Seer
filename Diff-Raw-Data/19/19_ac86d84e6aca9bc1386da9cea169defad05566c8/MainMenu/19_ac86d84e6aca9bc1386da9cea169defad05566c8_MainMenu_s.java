 package com.eceapp.sasel;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.ListView;
 
 public class MainMenu extends Activity {
 	public static final String SINGLE_SERVER_DATA = "singleServerData";
 
 	public static final String SERVER_NAME_LIST = "serverNameList";
 	private static final int WAITING_FOR_SINGLE_SERVER_DATA = 1;
 	private ArrayList <String> displayedServerList = new ArrayList<String>();
 	private HashMap<String,Server> serverMap = new HashMap<String,Server>();
 	private String checkedOutServerName = null;
 	private String googleAccountName = null;
 
 	ArrayAdapter<String> adapter;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main_menu);
 
 		Intent intent = getIntent();
 		Bundle extras = intent.getExtras();
 		
 		serverMap = (HashMap<String, Server>)extras.get(Preparation.SERVER_MAP);
 		googleAccountName = extras.getString(Preparation.GOOGLE_ACCOUNT_NAME);
 		
 		adapter = new ArrayAdapter<String>(
 				this,android.R.layout.simple_list_item_1, displayedServerList
 		);
 
 		ListView listView = (ListView) findViewById(R.id.mainServerList);
 		listView.setAdapter(adapter);
 		listView.setOnItemClickListener(serverButtonClicked); 
 		listView.setOnItemLongClickListener(longClickListener);
 		
 		//Populate the listView with all servers available
 		loadAllServers();
 
 		//Setting up the search method here
 		final EditText searchServer_et = (EditText)findViewById(R.id.searchServersET);
 		searchServer_et.addTextChangedListener( new TextWatcher(){
 			@Override
 			public void beforeTextChanged(CharSequence s, int start, int before, int count){
 				displayedServerList.clear();
 				adapter.notifyDataSetChanged();
 			}
 			
 			@Override
 			public void afterTextChanged(Editable edT){
 				String searchParams = edT.toString();
 				displayedServerList.clear();
 				searchServer_et.setError(null);
 				
 				//No parameters specified, do nothing
 				if (searchParams.length() == 0){
 					loadAllServers();
 					return;
 				}
 				
 				//Get the listView cleared in preparation for search results
 				adapter.notifyDataSetChanged();
 				
 				ArrayList<Server> searchResults = searchServers(searchParams);
 				
 				if (searchResults.size()==0) {
 					searchServer_et.setError("No server found!");
 					return;
 				}
 				
 				for (Server s: searchResults){
 					displayedServerList.add(s.getName());
 					System.out.println("Found server "+s);
 				}
 				adapter.notifyDataSetChanged();
 			}
 			
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before, int count){
 				String searchText = s.toString();
 				System.out.println("sequences s "+searchText);
 				clearDisplayList();
 				if (searchText.length() == 0) return;
 			}
 		});
 	}
 	
 	public ArrayList<Server> searchServers(String query){
 		//Contract is that searching starts from the beginning of 
 		//each word in a string string -- Francois's preference
 		String stQuery = "[\\s]*("+query+")"; 
 		ArrayList<Server> matches = new ArrayList<Server>();
 		
 		for (Server srv : serverMap.values()){
 			if (srv.searchAllAttributes(stQuery)) matches.add(srv);
 		}
 
 		return matches;
 	}
 	
 	public void clearDisplayList(){
 		displayedServerList.clear();
 		adapter.notifyDataSetChanged();
 	}
 	
 	public void loadAllServers(){
 		for (Server s : serverMap.values()){
 			displayedServerList.add(s.getName());
 		}
 		adapter.notifyDataSetChanged();
 	}
 
 	public ArrayList<String> getServerListNames(){
 		ArrayList<String> srvNames = new ArrayList<String>();
 		srvNames.addAll(serverMap.keySet());
 		return srvNames;
 	}
 	public void createNewServer(View v){
 		checkedOutServerName = null; //Not checking-out any existing server
 		Intent newServerIntent = new Intent(v.getContext(), SingleServerPage.class);
 		Server emptyServer = new Server();
 
 		newServerIntent.putExtra(SINGLE_SERVER_DATA, emptyServer);
         newServerIntent.putExtra(SERVER_NAME_LIST, getServerListNames());
         newServerIntent.putExtra(Preparation.GOOGLE_ACCOUNT_NAME, googleAccountName);
 		startActivityForResult(newServerIntent, WAITING_FOR_SINGLE_SERVER_DATA);
 	}
 	
 	private OnItemClickListener serverButtonClicked = new OnItemClickListener() {
 		@Override
 	    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
 	    	checkedOutServerName = displayedServerList.get(position);
 	        Intent intent = new Intent(v.getContext(),SingleServerPage.class);
 	        Server theServer = serverMap.get(checkedOutServerName);	 
 	        
 	        intent.putExtra(Preparation.GOOGLE_ACCOUNT_NAME, googleAccountName);
 	        intent.putExtra(SINGLE_SERVER_DATA, theServer);
 	        intent.putExtra(SERVER_NAME_LIST, getServerListNames());
 
 	        startActivityForResult(intent, WAITING_FOR_SINGLE_SERVER_DATA);
 	    }
 	};
 	
 	private OnItemLongClickListener longClickListener = new OnItemLongClickListener(){
 		@Override
 	    public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id){
 			final String nameForDeletion = displayedServerList.get(position);
 			
 			AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
 			builder.setTitle("Delete server: "+nameForDeletion+"?");  
 			builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					return;
 				}
 			});
 			
 			builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					Server st = serverMap.remove(nameForDeletion);
 					if (st != null){
 						displayedServerList.remove(nameForDeletion);
 						adapter.notifyDataSetChanged();
 					}
 				}
 			});
 			
 			AlertDialog deleteDialog = builder.create();
 			deleteDialog.show();
 			return true;
 	    }
 	};
 	//function for the Exit button
 	public void exitServer(View v){
 		exitMainMenu();
 	}
 	
 	//Function to query uniqueness of a server name across the hash map of servers
 	public boolean serverNameInMap(String queryName){
 		return (serverMap.get(queryName) != null);
 	}
 	
 	public void exitMainMenu(){
 		AlertDialog.Builder exitBuilder = new AlertDialog.Builder(this);
 		exitBuilder.setTitle(R.string.pushData);
 		
 		exitBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				Intent toParent = new Intent();
 				toParent.putExtra(Preparation.RETURNED_SERVERS_KEY, serverMap);
 				System.out.println("Server data to be uploaded in MainMenu"+serverMap);
 				setResult(RESULT_OK, toParent);
 				finish();
 			}
 		});
 		
 		exitBuilder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				Intent toParent = new Intent();
 				setResult(RESULT_CANCELED, toParent);
 				finish();
 			}
 		});
 		
 		AlertDialog exitDialog = exitBuilder.create();
 		exitDialog.show();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main_menu, menu);
 		return true;
 	}
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data){
 		if (requestCode == WAITING_FOR_SINGLE_SERVER_DATA){
 			if (resultCode == RESULT_OK){
 				Bundle extras = data.getExtras();
 				Server newServer = (Server)extras.get(SingleServerPage.SINGLE_SERVER_DATA_BACK);
 				//checkedOutServerName = newServer.getName();
 				System.out.println("checkOUTSRV "+checkedOutServerName);
 				System.out.println("This just came in "+newServer);
 				String newServerName = newServer.getName();
 				
 				//Out with the old
 				if (checkedOutServerName != null) serverMap.remove(checkedOutServerName);
 				
 				serverMap.put(newServerName, newServer);
 				displayedServerList.remove(checkedOutServerName);
 				displayedServerList.add(0, newServerName);
 				adapter.notifyDataSetChanged();
 				
 			}
 			
 			else if (resultCode == RESULT_CANCELED){
 				//Do-something in the future
 			}
 		}
 	}
 	
 	//Thanks to Stack-Overflow, function to detect when back button is pressed
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event){
 		if ((!serverMap.isEmpty()) && keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0){
 			//The back-button got pressed
 			exitMainMenu();
 			return true;
 		}
 		
 		return super.onKeyDown(keyCode,  event);
 	}
 }
