 package com.eceapp.sasel;
 
 import java.io.UnsupportedEncodingException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Locale;
 import java.util.regex.Pattern;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.ListView;
 
 
 
 public class SingleServerPage extends Activity {
 	public static final String SINGLE_SERVER_DATA_BACK = "singleServerDateBack";
 	private Server theServer; 
 	private ArrayList<String> serverNames;
 	private String originalName;
 	private ArrayList<String> displayedIpList;
 	private ArrayList<String> displayedOsList;
 	private ArrayList<String> displayedUserList = new ArrayList<String>();
 	private ArrayList<String> displayedAliasList;
 	private ArrayList<String> displayedServerRoleList;
 
 	private String googleAccountName = null;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_single_server_page);
 		
 		try {
 			populatePage();
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.single_server_page, menu);
 		return true;
 	}
 
 	public void populatePage() throws UnsupportedEncodingException{
 		//get the server data.
 		Intent intent = getIntent();
 		//if (intent == null) return;
 		Bundle extras = intent.getExtras();
 		//if (extras == null) return;
 		
 		theServer = (Server)extras.get(MainMenu.SINGLE_SERVER_DATA);    
 		googleAccountName = extras.getString(Preparation.GOOGLE_ACCOUNT_NAME);
 		serverNames = (ArrayList<String>) extras.get(MainMenu.SERVER_NAME_LIST);    
 		originalName = theServer.getName();
 				
 		//Set server name
 	    String serverName = theServer.getName();
 	    EditText serverNameEditText = (EditText) findViewById(R.id.serverNameEditText);
 	    serverNameEditText.setText(serverName);
 	    
 	    //Set server location
 	    String location = theServer.getLocation();
 	    EditText serverLocationEditText = (EditText) findViewById(R.id.serverLocationEditText);
 	    serverLocationEditText.setText(location);
 	    
 	    //Set IP list
 	    displayedIpList = theServer.getIPList();
 		ArrayAdapter<String> targetListAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, displayedIpList);
 		ListView ipListView = (ListView) findViewById(R.id.ipListView);
 		ipListView.setAdapter(targetListAdapter);
 		
 		ipListView.setOnItemLongClickListener(deleteIpListener);
 		
 		//Set OS list
 		displayedOsList=theServer.getOSList();
 	    targetListAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, displayedOsList);
 		ListView osListView = (ListView) findViewById(R.id.osListView);
 		osListView.setAdapter(targetListAdapter);    
 		osListView.setOnItemLongClickListener(deleteOsListener);
 
 		//Set User list 
 		ArrayList<User> userList = theServer.users;
 		for (User aUser:userList){
 			String userName = aUser.getUserName();
 				String passWord = new String (aUser.getPassword(),"UTF-8");
 			String userRole = aUser.getRole();
 			displayedUserList.add(userName+"   -   "+""+passWord+"   -   "+""+userRole);
 			System.out.println("userList "+userList);
 		}
 		
 	    targetListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, displayedUserList);
 		ListView userListView = (ListView) findViewById(R.id.userListView);
 		userListView.setAdapter(targetListAdapter);  
 		
 		userListView.setOnItemLongClickListener(deleteUserListener);
 		
 		//set aliases list
 		displayedAliasList = theServer.getAliasList();
 		
 	    targetListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, displayedAliasList);
 		ListView aliaseListView = (ListView) findViewById(R.id.aliasListView);
 		aliaseListView.setAdapter(targetListAdapter);    
 		aliaseListView.setOnItemLongClickListener(deleteAliasListener);
 		
 		//set server role list
 		displayedServerRoleList = theServer.getRolesList();
 	    targetListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, displayedServerRoleList);
 		ListView serverRoleListView = (ListView) findViewById(R.id.serverRoleListView);
 		serverRoleListView.setAdapter(targetListAdapter);  
 		
 		serverRoleListView.setOnItemLongClickListener(deleteServerRoleListener);
 		
 	}
 	
 	private boolean isServerNameUnique(String serverName){
 		for (String iterServerName: serverNames){
 			if (iterServerName.equals(serverName) && ! serverName.equals(originalName)){	
 				return false;
 			}
 		}
 		return true;
 	}
 	
 	public void saveSingleServer(View v){
 		EditText serverNameEditText = (EditText) findViewById(R.id.serverNameEditText);
 		String serverName = serverNameEditText.getText().toString();
 		EditText serverLocationEditText = (EditText) findViewById(R.id.serverLocationEditText);
 		String serverLocation = serverLocationEditText.getText().toString();		
 		
 		if (isServerNameUnique(serverName)==false){ 
 			serverNameEditText.setError("Server name already exists");
 			
 		}
 		else if(serverName.length()==0) {
 			serverNameEditText.setError("Can not be empty");
 		}
 		else{
 			this.theServer.setLocation(serverLocation);
 			this.theServer.setServerName(serverName);		
 			Intent intent = getIntent();
 			intent.putExtra(SINGLE_SERVER_DATA_BACK,this.theServer);
 			System.out.println("singSRV back "+this.theServer);
 			setResult(RESULT_OK,intent);
 			finish();	
 		}
 	}		
 
 
 	public void addUserHandler(View v){
 		try {
 			EditText userNameEditText = (EditText)findViewById(R.id.userNameEditText);
 			EditText passWordEditText = (EditText)findViewById(R.id.passWordEditText);
 			EditText userRoleEditText = (EditText)findViewById(R.id.userRoleEditText);
 			String newUserName = userNameEditText.getText().toString();
 			String newUserRole = userRoleEditText.getText().toString();
 			System.out.println(newUserName+"-"+newUserRole);
 			byte[] newPassWord = ((String)passWordEditText.getText().toString()).getBytes();
 			
 			if (newUserName.length()==0){
 				userNameEditText.setError("Can not be empty");	
 			}else{
 				//Constructor => User(String name, String role, byte[] password, String changerName, String initDate)
 				SimpleDateFormat d_fmt = new SimpleDateFormat("yyyymmdd:HHMMSS", Locale.getDefault());
 				Date currentDate = new Date();
 				//String currentDate = DateTime(d_fmt);
 				User newUser = new User(newUserName, newUserRole, newPassWord, googleAccountName, d_fmt.format(currentDate));
 				System.out.println("userCreatd "+newUser);
 				if (theServer.addUser(newUser) != true) return;
 				
 				//======Retrieve the data from the newly created user, and display onto the UI =======
 				String userName = newUser.getUserName();
 				String passWord;
 		
 				passWord = new String (newUser.getPassword(),"UTF-8");
 		
 				String userRole = newUser.getRole();
 				displayedUserList.add(0, userName+"   -   "+""+passWord+"   -   "+""+userRole);
 				ArrayAdapter<String> targetListAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, displayedUserList);
 				ListView targetListView = (ListView) findViewById(R.id.userListView);
 				targetListView.setAdapter(targetListAdapter);
 				
 				clearEditText(userNameEditText);
 				clearEditText(passWordEditText);
 				clearEditText(userRoleEditText);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void clearEditText(final EditText theET){
 		if (theET == null || ! (theET instanceof EditText)) return;
 		theET.setText("");
 	}
 	
 	public void addOsHandler(View v){	
 		EditText addOsEditText = (EditText)findViewById(R.id.addOsEditText); 
 		String newOs = addOsEditText.getText().toString();
 		
 		if (newOs.length()==0 ){
 			addOsEditText.setError("Can not be empty");	
 		}
 		else{
 			if (this.theServer.addOS(newOs))	
 				displayedOsList.add(0,newOs);
 			
 			ArrayAdapter<String> targetListAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, displayedOsList);
 			ListView targetListView = (ListView) findViewById(R.id.osListView);
 			targetListView.setAdapter(targetListAdapter);
 			addOsEditText.setText("");	
 		}
 	}	
 	
 		
 	public void addIpHandler(View v){	
 		EditText addIpEditText = (EditText)findViewById(R.id.addIpEditText); 
 		String newIp = addIpEditText.getText().toString();
 		
 		Pattern ipPattern = Pattern.compile(Server.IPV4_REGEX);
 		boolean ipValidation = ipPattern.matcher(newIp).find();
 		
 		if (newIp.length()==0 ){
 			addIpEditText.setError("Can not be empty");	
 		}else if (ipValidation==false){
 			addIpEditText.setError("Invalid Ip");
 		}else{
 			if (this.theServer.addIP(newIp))
 				displayedIpList.add(0, newIp);
 			
 			ArrayAdapter<String> targetListAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, this.theServer.getIPList());
 			ListView targetListView = (ListView) findViewById(R.id.ipListView);
 			targetListView.setAdapter(targetListAdapter);
 			addIpEditText.setText("");	
 		}
 	}	
 
 	public void addAliasHandler(View v){
 		EditText addAliasEditText = (EditText)findViewById(R.id.addAliasEditText); 
 		String newAlias = addAliasEditText.getText().toString();
 
 		if (newAlias.length()==0){
 			addAliasEditText.setError("Can not be empty");	
 		}else{
 			if (this.theServer.addAlias(newAlias))
 				displayedAliasList.add(0, newAlias); //Meant to preserve WSIWYG ordering during entry-population
 			
 			ArrayAdapter<String> aliaseListAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, this.theServer.getAliasList());
 			ListView aliaseListView = (ListView) findViewById(R.id.aliasListView);
 			aliaseListView.setAdapter(aliaseListAdapter);
 			addAliasEditText.setText("");	
 		}
 	}
 	
 	public void addServerRoleHandler(View v){
 		EditText addServerRoleEditText = (EditText)findViewById(R.id.addServerRoleEditText); 
 		String newServerRole = addServerRoleEditText.getText().toString();
 		
 		if (newServerRole.length()==0){
 			addServerRoleEditText.setError("Can not be empty");	
 		}
 		else{
 			if (this.theServer.addRole(newServerRole)){
 				displayedServerRoleList.add(0, newServerRole);
 			}
 			
 			ArrayAdapter<String> targetListAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, displayedServerRoleList);
 			ListView targetListView = (ListView) findViewById(R.id.serverRoleListView);
 			targetListView.setAdapter(targetListAdapter);
 			addServerRoleEditText.setText("");	
 		}		
 	}
 	
 	
 	private OnItemLongClickListener deleteUserListener = new OnItemLongClickListener(){
 		@Override
 	    public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id){
 			
 			final String nameForDeletion = displayedUserList.get(position);
 			final int itemPostion = position;
 			
 			AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
 			builder.setTitle("Delete User: "+nameForDeletion+"?");  
 			builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					return;
 				}
 			});
 			
 			builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					deleteItemInServer(R.id.userListView,itemPostion);
 					return;
 				}
 			});
 			AlertDialog deleteDialog = builder.create();
 			deleteDialog.show();
 			return true;
 	    }
 	};
 	
 	private OnItemLongClickListener deleteOsListener = new OnItemLongClickListener(){
 		@Override
 	    public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id){
 			
 			final String nameForDeletion = displayedOsList.get(position);
 			final int itemPostion = position;
 			
 			AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
 			builder.setTitle("Delete OS: "+nameForDeletion+"?");  
 			builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					return;
 				}
 			});
 			
 			builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					deleteItemInServer(R.id.osListView,itemPostion);
 					return;
 				}
 			});
 			AlertDialog deleteDialog = builder.create();
 			deleteDialog.show();
 			return true;
 	    }
 	};	
 	
 	
 	private OnItemLongClickListener deleteIpListener = new OnItemLongClickListener(){
 		
 		@Override
 	    public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id){
 			
 			final String nameForDeletion = displayedIpList.get(position);
 			final int itemPostion = position;
 			
 			AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
 			builder.setTitle("Delete Ip: "+nameForDeletion+"?");  
 			builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					return;
 				}
 			});
 			
 			builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					deleteItemInServer(R.id.ipListView,itemPostion);
 					return;
 				}
 			});
 			AlertDialog deleteDialog = builder.create();
 			deleteDialog.show();
 			return true;
 	    }
 	};
 	
 	private OnItemLongClickListener deleteAliasListener = new OnItemLongClickListener(){
 		
 		@Override
 	    public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id){
 			
 			final String nameForDeletion = displayedAliasList.get(position);
 			final int itemPostion = position;
 			
 			AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
 			builder.setTitle("Delete Alias: "+nameForDeletion+"?");  
 			builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					return;
 				}
 			});
 			
 			builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					deleteItemInServer(R.id.aliasListView, itemPostion);
 					return;
 				}
 			});
 			AlertDialog deleteDialog = builder.create();
 			deleteDialog.show();
 			return true;
 	    }
 	};
 	
 	private OnItemLongClickListener deleteServerRoleListener = new OnItemLongClickListener(){
 		
 		@Override
 	    public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id){
 			
 			final String nameForDeletion = displayedServerRoleList.get(position);
 			final int itemPostion = position;
 			
 			AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
 			builder.setTitle("Delete Server Role: "+nameForDeletion+"?");  
 			builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					return;
 				}
 			});
 			
 			builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					deleteItemInServer(R.id.serverRoleListView,itemPostion);
 					return;
 				}
 			});
 			AlertDialog deleteDialog = builder.create();
 			deleteDialog.show();
 			return true;
 	    }
 	};
 	
 	private void deleteItemInServer(int viewListId, int itemPosition){
 		//Historically, we were able to use switch-case but as of September 2013, we've had issues
 		//with id's being generated as public static int .. instead of public static final int ..
 		//Which makes the compiler interpreted as non-final
 		if(viewListId == R.id.userListView){ 
 			
 			ArrayAdapter<String> targetListAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, displayedUserList);
 			ListView targetListView = (ListView) findViewById(R.id.userListView);
 
 			if (theServer.removeUserByIndex(itemPosition) == true)
 				displayedUserList.remove(itemPosition);
 			targetListView.setAdapter(targetListAdapter);
 		}
 		
 		else if (viewListId == R.id.osListView){
 			displayedOsList.remove(itemPosition);
 			ArrayAdapter<String> targetListAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, displayedOsList);
 			ListView targetListView = (ListView) findViewById(R.id.osListView);
 			
 			theServer.removeOsByIndex(itemPosition);
 			targetListView.setAdapter(targetListAdapter);
 		}
 		
 		else if(viewListId == R.id.ipListView){
 			displayedIpList.remove(itemPosition);
 
 			theServer.removeIpByIndex(itemPosition);
 			
 			ArrayAdapter<String> targetListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, displayedIpList);
 			ListView targetListView = (ListView) findViewById(R.id.ipListView);
 			targetListView.setAdapter(targetListAdapter);
 		}
 		
 		else if (viewListId == R.id.aliasListView){
 			displayedAliasList.remove(itemPosition);
 			ArrayAdapter<String> targetListAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, displayedAliasList);
 			ListView targetListView = (ListView) findViewById(R.id.aliasListView);
 			
 			theServer.removeAliasByIndex(itemPosition);
 			targetListView.setAdapter(targetListAdapter);
 		}
 		
 		else if (viewListId == R.id.serverRoleListView){			
 			displayedServerRoleList.remove(itemPosition);
 			ArrayAdapter<String> targetListAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, displayedServerRoleList);
 			ListView targetListView = (ListView) findViewById(R.id.serverRoleListView);
 			
 			theServer.removeServerRoleByIndex(itemPosition);
 			targetListView.setAdapter(targetListAdapter);
 		}				
 	}
 }
