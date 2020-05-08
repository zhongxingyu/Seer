 /*
  * Copyright 2012  Matthew Mole <code@gairne.co.uk>, Carlos Eduardo da Silva <kaduardo@gmail.com>
  * 
  * This file is part of ahgdc.
  * 
  * ahgdc is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * ahgdc is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with ahgdc.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package android.hgd;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 
 import android.app.AlertDialog;
 import android.app.TabActivity;
 import android.content.ContentResolver;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.content.res.Resources;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.preference.PreferenceManager;
 import android.text.InputType;
 import android.text.method.PasswordTransformationMethod;
 import android.text.method.TransformationMethod;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.inputmethod.EditorInfo;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.TabHost;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import android.hgd.ahgdConstants;
 
 import jhgdc.library.HGDClient;
 import jhgdc.library.JHGDException;
 import jhgdc.library.Playlist;
 import jhgdc.library.PlaylistItem;
 
 /**
  * This is the main entrypoint into the application.
  * 
  * @author Matthew Mole
  */
 public class ahgdClient extends TabActivity implements ThreadListener {
     /** Called when the activity is first created. */
 	public static HGDClient jc;
 	
 	public static String SERVER_FILENAME = "hgd_server.config";
 	
 	private Handler handler;
 	
 	private WorkerThread worker;
 	private PeriodicThread refresher;
 	
 	//Temporary state variables
 	private String toVoteOff;
 	
 	//filebrowser
 	private ListView filelist;
 	private Browser browser;
 	private String[] listItems = {};
 	private ArrayAdapter myAdapter; //TODO fix this warning
 	
 	//playlist
 	private ArrayList<HashMap<String, String>> songData;
 	private ListView songlist;
 	private SimpleAdapter songAdapter;
 	
 	//activitylist
 	private ArrayAdapter activitiesAdapter;
 	private ListView activitylist;
 	private String[] activityItems = {};
 	
 	//servers
 	private ListView serverlist;
 	private TextView currentServer;
 	private ArrayAdapter<String> serverAdapter;
 	private Button addServer;
 	private ArrayList<ServerDetails> servers = new ArrayList<ServerDetails>();
 	
 	//
 	// USER INTERFACE CREATION
 	//
 	
 	public void createUI() {
 		TabHost tabs = getTabHost();
         jc = new HGDClient();
         handler = new Handler();
         worker = new WorkerThread(this);
         worker.start();
         
         this.getLayoutInflater().inflate(R.layout.main, tabs.getTabContentView(), true);
         
         Resources resources = getResources();
         
         TabHost.TabSpec t_upload = tabs.newTabSpec("filebrowser").setContent(R.id.filebrowser).setIndicator("Upload", resources.getDrawable(R.drawable.tab_note));
         TabHost.TabSpec t_playlist = tabs.newTabSpec("playlist").setContent(R.id.playlist).setIndicator("Playlist", resources.getDrawable(R.drawable.tab_playlist));
         TabHost.TabSpec t_servers = tabs.newTabSpec("servers").setContent(R.id.serversframe).setIndicator("Servers", resources.getDrawable(R.drawable.tab_servers));
         TabHost.TabSpec t_activities = tabs.newTabSpec("activities").setContent(R.id.activitylist).setIndicator("Active", resources.getDrawable(R.drawable.tab_activities));
         
         tabs.addTab(t_upload);
         tabs.addTab(t_playlist);
         tabs.addTab(t_servers);
         tabs.addTab(t_activities);
         
         init_upload_tab();
         init_playlist_tab();
         init_servers_tab();
         init_activities_tab();
         
         //Playlist listener
         getTabWidget().getChildAt(1).setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				resetSongAdapter();
 				getTabHost().setCurrentTab(1);
 			}
         });
         
         refresher = new PeriodicThread(new Long(PreferenceManager.getDefaultSharedPreferences(this).getInt("timeout", 10) * 1000), worker);
         refresher.start();
 	}
 	
 	@Override
 	public void onConfigurationChanged(Configuration newConfig)
 	{
 	    super.onConfigurationChanged(newConfig);
 	}
 	
 	/**
 	 * Activity has been started - perhaps after having been killed.
 	 */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         File root = Environment.getExternalStorageDirectory();
         if (!root.canWrite()){
         	Toast.makeText(getApplicationContext(), "Cannot write to root", Toast.LENGTH_SHORT).show();
         }
         SERVER_FILENAME = (new File(root, SERVER_FILENAME)).getAbsolutePath();
         
         createUI();
         /*
          * 127.0.0.1 gives connection refused:
          * See http://stackoverflow.com/questions/3497253/java-net-connectexception-connection-refused-android-emulator
          */
     }
     
     /**
      * Activity has just been started (following onCreate) or has been restarted
      * having been previously stopped
      */
     @Override
     public void onStart() {
     	super.onStart();
     }
     
     /**
      * Activity has just been resumed having been previously paused
      */
     @Override
     public void onResume() {
     	super.onResume();
     }
     
     /**
      * Activity has been paused because another activity is in the foreground (has focus)
      */
     @Override
     public void onPause() {
     	super.onPause();
     }
     
     /**
      * Activity has been stopped because it is no longer visible
      */
     @Override
     public void onStop() {
     	super.onStop();
     }
     
     /**
      * Activity has become visible again after being stopped
      */
     @Override
     public void onRestart() {
     	super.onRestart();
     }
     
     /**
      * Activity is about to shut down gracefully - i.e. without being killed for memory.
      */
     @Override
     public void onDestroy() {
     	super.onDestroy();
     	
     	worker.die();
     }
     
     //
     // Initialisation of user interface components
     //
     
     public void init_playlist_tab() {
     	songlist = (ListView) findViewById(R.id.playlist);
     	songlist.setOnItemClickListener(new OnItemClickListener() {
     		public void onItemClick(AdapterView parent, View v, int position, long id) {
     			playlistClicked(position);
     		}
     	});
     	
     	resetSongAdapter();
     	
     	songData = new ArrayList<HashMap<String, String>>();
     	/*HashMap<String, String> map;
     	
         map = new HashMap<String, String>();
         map.put("title", "Refresh");
         map.put("artist", "");
         map.put("user", "");
         map.put("duration", "");
         map.put("voted", "");
         songData.add(map);*/
     	
     	songAdapter = new SimpleAdapter(this.getBaseContext(), songData, R.layout.playlistitem,
                 new String[] {"title", "artist", "user", "duration", "voted"}, new int[] {R.id.title, R.id.artist, R.id.user, R.id.duration, R.id.voted});
         
         songlist.setAdapter(songAdapter);
     }
     
     public void init_servers_tab() {
     	currentServer = (TextView) findViewById(R.id.currentserver);
     	currentServer.setText("Not connected");
     	
     	serverlist = (ListView) findViewById(R.id.serverlist);
     	
     	serverlist.setOnItemClickListener(new OnItemClickListener() {
     		public void onItemClick(AdapterView parent, View v, int position, long id) {
     			serverlistClicked(position);
     		}
     	});
     	
     	serverlist.setOnItemLongClickListener(new OnItemLongClickListener() {
     		public boolean onItemLongClick(AdapterView parent, View v, int position, long id) {
     			return serverlistLongClicked(position);
     		}
     	});
     	
         serverAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, convertServers(servers));
         serverlist.setAdapter(serverAdapter);
         
         addServer = (Button) findViewById(R.id.serveradd);
         addServer.setOnClickListener(new OnClickListener() {
         	public void onClick(View v) {
                 addServerClicked();
             }
         });
         
         readServerConfig();
         resetServerAdapter();
     }
     
     public void init_upload_tab() {
         filelist = (ListView) findViewById(R.id.filebrowser);
         filelist.setOnItemClickListener(new OnItemClickListener() {
         	public void onItemClick(AdapterView parent, View v, int position, long id) {
         		filelistClicked(position);
         	}
         });
 		
         renewBrowser();
         myAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems);
         filelist.setAdapter(myAdapter);
     }
     
     public void init_activities_tab() {
     	activitylist = (ListView) findViewById(R.id.activitylist);
     	
     	activitiesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, activityItems);
         activitylist.setAdapter(activitiesAdapter);
         
         resetActivityAdapter();
     }
     
     //
     // Updating the data of the User Interface components (refreshes them)
     //
     
     public void resetServerAdapter() {
     	serverAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, convertServers(servers));
         serverlist.setAdapter(serverAdapter);
     }
     
     public void resetFileListAdapter() {
     	myAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems);       
         filelist.setAdapter(myAdapter);
     }
     
     public void resetSongAdapter() {
     	worker.getPlaylist();
     }
     
     public void resetActivityAdapter() {
     	worker.getActive();
     }
     
     //
     // User Interface reactions
     //
     
     private void playlistClicked(int position) {
     	AlertDialog.Builder alert = new AlertDialog.Builder(this);
 	    alert.setTitle("Vote off?");
 	    alert.setMessage("Would you like to vote off the current song?");
 	    
 	    alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
 			
 			public void onClick(DialogInterface dialog, int which) {
 				vote();
 			}
 		});
 	    
 	    alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 				dialog.cancel();
 			}
 		});
 	    
 	    alert.show();
     }
     
     private void serverlistClicked(final int position) {
     	AlertDialog.Builder alert = new AlertDialog.Builder(this);
 	    alert.setTitle("password");
 	    alert.setMessage("enter password");
 	    
 	    final EditText input = new EditText(this);
 	    input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
 	    alert.setView(input);
 	    
 	    alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
 			
 			public void onClick(DialogInterface dialog, int which) {
 				connectServer(servers.get(position), input.getText().toString());
 			}
 		});
 	    
 	    alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 				dialog.cancel();
 			}
 		});
 	    
 	    alert.show();
     }
     
     private boolean serverlistLongClicked(final int position) {
     	AlertDialog.Builder alert = new AlertDialog.Builder(this);
     	
     	alert.setTitle("Server deletion");
 		alert.setMessage("Are you sure you want to delete the server?");
     	
 		alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 				deleteServer(position);
 		    }
 		});
 		   
 		alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 		    	dialog.cancel();
 		    }
 		});
 
 		alert.show();
     	return true;
     }
     
     private void addServerClicked() {
     	Intent myIntent = new Intent(getApplicationContext(), AddServerActivity.class);
         startActivityForResult(myIntent, ahgdConstants.SERVER_DETAILS);
     }
     
     private void settingsClicked() {
     	Intent myIntent = new Intent(getApplicationContext(), PreferencesActivity.class);
         startActivityForResult(myIntent, ahgdConstants.UPDATE_SETTINGS);
     }
     
     private void renewBrowser() {
     	if (PreferenceManager.getDefaultSharedPreferences(this).getString("browser", "Music").equals("Music")) {
     		browser = new MusicBrowser(getContentResolver());
     	}
     	else {
     		browser = new FileBrowser();
     	}
         browser.reset();
         listItems = browser.getFilelist();
     }
     
     private void filelistClicked(int position) {
     	int action = browser.update(listItems[position]);
     	if (action == Browser.NO_ACTION) {
     		//
     	}
     	else if (action == Browser.VALID_TO_UPLOAD) {
     		worker.uploadFile(browser.getPath());
     	}
     	else if (action == Browser.DIRECTORY) {
     		listItems = browser.getFilelist();
     		resetFileListAdapter();
     	}
     }
     
     //
     // Dialog box reactions
     //
     private void deleteServer(int position) {
     	servers.remove(position);
         writeServerConfig();
         resetServerAdapter();
     }
     
     private void addServer(String entry) {
     	String user = entry.split("@")[0];
 		String hostname = entry.split("@")[1].split(":")[0];
 		String port = entry.split("@")[1].split(":")[1];
 		
 		servers.add(new ServerDetails(hostname, port, user));
 		writeServerConfig();
 		resetServerAdapter();
     }
     
     private void connectServer(ServerDetails server, String entry) {
 		worker.connectToServer(server, entry);
     }
  
     public void log(String tag, String message) {
     	Log.i(tag, message);
     }
     
     /**
      * Vote off the current song
      */
     public void vote() {
     	worker.voteSong();
     }
 
     public String[] convertServers(ArrayList<ServerDetails> arraylist) {
     	String[] toRet = new String[arraylist.size()];
     	for (int i = 0; i < arraylist.size(); i++) {
     		toRet[i] = arraylist.get(i).toString();
     	}
     	return toRet;
     }
     
     public void writeServerConfig() {
     	try {
     		FileOutputStream os = new FileOutputStream(new File(SERVER_FILENAME));
     		OutputStreamWriter out = new OutputStreamWriter(os);
     		for (String line : convertServers(servers)) {
     			out.write(line + "\n");
     		}
     		out.close();
     	}
     	catch (java.io.IOException e) {
     		Toast.makeText(getApplicationContext(), "IOException in writeServerConfig: " + e.getMessage() + " | " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
     		return;
     	}
     }
     
     public void readServerConfig() {
     	servers = new ArrayList<ServerDetails>();
     	try {
     		File input = new File(SERVER_FILENAME);
     		if (!input.exists()) {
     			Toast.makeText(getApplicationContext(), "File does not exist: " + SERVER_FILENAME, Toast.LENGTH_SHORT).show();
     			input.createNewFile();
     		}
     		if (!input.canRead()) {
     			Toast.makeText(getApplicationContext(), "canRead = False: " + SERVER_FILENAME, Toast.LENGTH_SHORT).show();
     			return;
     		}
     		if (!input.canWrite()) {
     			Toast.makeText(getApplicationContext(), "canWrite = False: " + SERVER_FILENAME, Toast.LENGTH_SHORT).show();
     			return;
     		}
     		if (!input.isFile()) {
     			Toast.makeText(getApplicationContext(), "Not a file: " + SERVER_FILENAME, Toast.LENGTH_SHORT).show();
     			return;
     		}
     		FileInputStream fis = new FileInputStream(input);
     		InputStreamReader isr = new InputStreamReader(fis);
     		BufferedReader in = new BufferedReader(isr);
     	    String line;
     	    while ((line = in.readLine()) != null) {
     	    	  servers.add(ServerDetails.toServerDetails(line));
     	    }
     	    in.close();
     	}
     	catch (java.io.FileNotFoundException e) {
     		Toast.makeText(getApplicationContext(), "FileNotFoundException in readServerConfig: " + e.getMessage() + " | " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
     		return;
     	}
     	catch (java.io.IOException e) {
     		Toast.makeText(getApplicationContext(), "IOException in readServerConfig: " + e.getMessage() + " | " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
     		return;
     	}
     }
     
     /**
      * See the warning attached to notify()
      */
     public void notifyPlaylist(final Playlist receivedPlaylist) {
     	handler.post(new Runnable() {
     		public void run() {
     			songData = new ArrayList<HashMap<String, String>>();
     	    	HashMap<String, String> map;
     	    	
     	    	try {
     	    		ArrayList<PlaylistItem> playlist = receivedPlaylist.getItems();
     	    		
     	    		if (playlist.isEmpty()) {
     	    			map = new HashMap<String, String>();
     	    		}
     	    		else {
     	    			map = new HashMap<String, String>();
     	    			
     	            	for (PlaylistItem p : playlist) {
     	            		map = new HashMap<String, String>();
     	                    map.put("title", p.getTitle());
     	                    map.put("artist", p.getArtist());
     	                    map.put("user", p.getUser());
     	                    map.put("duration", p.getDuration() + "s");
     	                    if (p.haveVoted().equals("1")) {
     	                    	map.put("voted", "Voted");
     	                    }
     	                    else {
         	                    map.put("voted", "Not voted");
     	                    }
     	                    songData.add(map);
     	            	}
     	    		}
     	    	}
     	    	catch (NullPointerException e) {
     	    		map = new HashMap<String, String>();
     	    	}
     	    	
     	    	songAdapter = new SimpleAdapter (getApplicationContext(), songData, R.layout.playlistitem,
     	                new String[] {"title", "artist", "user", "duration", "voted"}, new int[] {R.id.title, R.id.artist, R.id.user, R.id.duration, R.id.voted});
     	        
     	        songlist.setAdapter(songAdapter);
     		}
     	});
     }
 
     //TODO: Use strings.xml text
     /**
      * If the worker thread needs to send a message to the User Interface thread, it calls the notify function.
      * THIS IS ON THE WORKER THREADS STACK ON EXECUTION, not the user interface thread. Changing the UI from the
      * worker thread is dangerous, therefore we create a runnable and give it to the User Interface to execute.
      */
 	public void notify(final int message, final String extraInformation) {
 		handler.post(new Runnable() {
 			public void run() {
 				switch (message) {
 				case ahgdConstants.THREAD_CONNECTION_SUCCESS: {
 					currentServer.setText(extraInformation);
 					Toast.makeText(getApplicationContext(), "Connected Successfully", Toast.LENGTH_SHORT).show();
 					break;
 				}
 				case ahgdConstants.THREAD_UPLOAD_SUCCESS: {
 					Toast.makeText(getApplicationContext(), "Uploaded Successfully", Toast.LENGTH_SHORT).show();
 					break;
 				}
 				case ahgdConstants.THREAD_VOTING_SUCCESS: {
 					Toast.makeText(getApplicationContext(), "Voted Successfully", Toast.LENGTH_SHORT).show();
 					break;
 				}
 				case ahgdConstants.THREAD_CONNECTION_GENFAIL:
 				case ahgdConstants.THREAD_CONNECTION_IOFAIL: {
 					currentServer.setText("Not currently connected");
 					Toast.makeText(getApplicationContext(), "General error whilst connecting", Toast.LENGTH_SHORT).show();
 					Toast.makeText(getApplicationContext(), extraInformation, Toast.LENGTH_SHORT).show();
 					break;
 				}
 				case ahgdConstants.THREAD_CONNECTION_PASSWORD_GENFAIL:
 				case ahgdConstants.THREAD_CONNECTION_PASSWORD_IOFAIL: {
 					currentServer.setText("Not currently connected");
 					Toast.makeText(getApplicationContext(), "General error whilst logging in", Toast.LENGTH_SHORT).show();
 					Toast.makeText(getApplicationContext(), extraInformation, Toast.LENGTH_SHORT).show();
 					break;
 				}
 				case ahgdConstants.THREAD_UPLOAD_FILENOTFOUND: {
 					Toast.makeText(getApplicationContext(), "File not found", Toast.LENGTH_SHORT).show();
 					Toast.makeText(getApplicationContext(), extraInformation, Toast.LENGTH_SHORT).show();
 					break;
 				}
 				case ahgdConstants.THREAD_UPLOAD_GENFAIL: 
 				case ahgdConstants.THREAD_UPLOAD_IOFAIL: {
 					Toast.makeText(getApplicationContext(), "General error whilst uploading", Toast.LENGTH_SHORT).show();
 					Toast.makeText(getApplicationContext(), extraInformation, Toast.LENGTH_SHORT).show();
 					break;
 				}
 				case ahgdConstants.THREAD_UPLOAD_NOTAUTH: {
 					Toast.makeText(getApplicationContext(), "Not logged in", Toast.LENGTH_SHORT).show();
 					Toast.makeText(getApplicationContext(), extraInformation, Toast.LENGTH_SHORT).show();
 					break;
 				}
 				case ahgdConstants.THREAD_UPLOAD_NOTCONNECTED: {
 					Toast.makeText(getApplicationContext(), "Not connected", Toast.LENGTH_SHORT).show();
 					Toast.makeText(getApplicationContext(), extraInformation, Toast.LENGTH_SHORT).show();
 					break;
 				}
 				case ahgdConstants.THREAD_VOTING_GENFAIL:
 				case ahgdConstants.THREAD_VOTING_IOFAIL: {
 					Toast.makeText(getApplicationContext(), "General error whilst voting", Toast.LENGTH_SHORT).show();
 					Toast.makeText(getApplicationContext(), extraInformation, Toast.LENGTH_SHORT).show();
 					break;
 				}
 				case ahgdConstants.THREAD_VOTING_NOTAUTH: {
 					Toast.makeText(getApplicationContext(), "Not logged in", Toast.LENGTH_SHORT).show();
 					Toast.makeText(getApplicationContext(), extraInformation, Toast.LENGTH_SHORT).show();
 					break;
 				}
 				case ahgdConstants.THREAD_VOTING_NOTCONNECTED: {
 					Toast.makeText(getApplicationContext(), "Not connected", Toast.LENGTH_SHORT).show();
 					Toast.makeText(getApplicationContext(), extraInformation, Toast.LENGTH_SHORT).show();
 					break;
 				}
 				}
 			}
 		});
 	}
 
 	public void notifyActive(final String[] activities) {
 		handler.post(new Runnable() {
 			public void run() {
 				activityItems = activities;
 				activitiesAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, activityItems);
 		        activitylist.setAdapter(activitiesAdapter);
 			}
 		});
 	}
 	
 	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
 		if (requestCode == ahgdConstants.SERVER_DETAILS) {
             if (resultCode == RESULT_OK) {
                 String contents = intent.getStringExtra(ahgdConstants.SERVER_DATA);
                 addServer(contents);
             } else if (resultCode == RESULT_CANCELED) {
             	//Fine
             }
         }
 		else if (requestCode == ahgdConstants.UPDATE_SETTINGS) {
 			//if (resultCode == RESULT_OK) {
 				//Toast.makeText(getApplicationContext(), "Updated settings", Toast.LENGTH_SHORT).show();
 				renewBrowser();
 				resetFileListAdapter();
 				refresher.setPeriod(new Long(PreferenceManager.getDefaultSharedPreferences(this).getInt("timeout", 10) * 1000));
             //} else if (resultCode == RESULT_CANCELED) {
             //	Toast.makeText(getApplicationContext(), "Updated settings fail", Toast.LENGTH_SHORT).show();
 			//	
             //}
 		}
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 	    MenuInflater inflater = getMenuInflater();
 	    inflater.inflate(R.menu.mainmenu, menu);
 	    return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    switch (item.getItemId()) {
 	    case R.id.settings:
 	        settingsClicked();
 	        return true;
 	    default:
 	        return super.onOptionsItemSelected(item);
 	    }
 	}
 }
