 /*
  * This work is licensed under the Creative Commons Attribution-NonCommercial 3.0 New Zealand License. 
  * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc/3.0/nz/ or send a 
  * letter to Creative Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.
  */
 
 package com.pentacog.mctracker;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.DialogInterface.OnClickListener;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.text.ClipboardManager;
 import android.view.ContextMenu;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.ListView;
 import android.widget.Toast;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 
 /**
  * 
  * @author Affian
  *
  */
 public class MCServerTrackerActivity extends ListActivity {
 	
 	public static final int PACKET_REQUEST_CODE = 254;
 	public static final String SERVER_CACHE_FILE = "mcTrackerServerCache.json";
 	private static final int JSON_ERROR_DIALOG = 10;
 	private static final int IO_ERROR_DIALOG = 20;
 	private static final int NO_SERVER_DIALOG = 30;
 	private static final int SERVER_REFRESH_RATE = 300000;
 	public static final String ERROR_CHAR = "\u200B";
 	
 	private static ServerListAdapter serverList = null;
 	private static long lastRefresh = 0;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	
     	super.onCreate(savedInstanceState);
 		
 
 		ListView lv = getListView();
 		lv.setTextFilterEnabled(true);
 		lv.setCacheColorHint(Color.TRANSPARENT);
 		lv.setBackgroundResource(R.drawable.dirt_tile);
 		
 		registerForContextMenu(lv);
 
     }
     
     /**
 	 * @see android.app.Activity#onResume()
 	 */
 	@Override
 	protected void onResume() {
 		super.onResume();
 		
 		if (serverList == null) {
 		
 			try {
 				InputStream instream = openFileInput(SERVER_CACHE_FILE);
 				 
 			    // if file the available for reading
 			    if (instream != null) {
 			        // prepare the file for reading
 			        InputStreamReader inputreader = new InputStreamReader(instream);
 			        BufferedReader br = new BufferedReader(inputreader, 4000);
 				
 					JSONArray servers = null;
 					String jsonOutput = "";
 					String temp = null;
 					while ((temp = br.readLine()) != null) {
 						jsonOutput += temp;
 					}
 					
 					servers = new JSONArray(jsonOutput);
 					serverList = new ServerListAdapter(servers);
 					setListAdapter(serverList);
 					lastRefresh = System.currentTimeMillis();
 					br.close();
 					instream.close();
 				}
 			} catch (IOException e) {
 				/*
 				 * Breaks here on first load with file not found
 				 * New one will be created on next save
 				 */
 			} catch (JSONException e) {
 				/*
 				 * If the JSON somehow becomes corrupt then this will fire
 				 * telling the user why their servers have vanished
 				 */
 				showDialog(JSON_ERROR_DIALOG);
 			}
 		
 		} else if(System.currentTimeMillis() - lastRefresh > SERVER_REFRESH_RATE) {
 			serverList.refresh();
 			lastRefresh = System.currentTimeMillis();
 		}
 		
 		if (serverList == null) {
 			serverList = new ServerListAdapter();
 			showDialog(NO_SERVER_DIALOG);
 			lastRefresh = System.currentTimeMillis();
 		}
 		setListAdapter(serverList);
 	}
 
 	/**
 	 * @see android.app.Activity#onPause()
 	 */
 	@Override
 	protected void onPause() {
 		InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
 		if (mgr.isActive(getListView())) {
 			mgr.hideSoftInputFromWindow(getListView().getWindowToken(), 0);
 		}
 		super.onPause();
 	}
 
 	/**
 	 * @see android.app.ListActivity#onDestroy()
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	protected void onDestroy() {
 		new SaveServerListTask(getApplicationContext()).execute(serverList.getServerList());
 		super.onDestroy();
 	}
 
 	/**
 	 * @see android.app.Activity#onCreateDialog(int)
 	 */
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		switch (id) {
 		case JSON_ERROR_DIALOG:
 			builder.setTitle(R.string.cache_corrupt);
 			builder.setMessage(R.string.cache_corrupt_message);
 			builder.setNeutralButton(R.string.ok, new OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					dialog.dismiss();
 				}
 			});
 			return builder.create();
 		case NO_SERVER_DIALOG:
 			builder.setTitle(R.string.no_servers);
 			builder.setMessage(R.string.no_server_message);
 			builder.setPositiveButton(R.string.yes, new OnClickListener() {
 				
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					Intent addServer = new Intent(MCServerTrackerActivity.this, AddServerActivity.class);
 			        startActivityForResult(addServer, AddServerActivity.ADD_SERVER_ACTIVITY_ID);
 				}
 			});
 			builder.setNegativeButton(R.string.no, new OnClickListener() {
 				
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					dialog.dismiss();
 				}
 			});
 			return builder.create();
 		case IO_ERROR_DIALOG:
 			break;
 		}
 		return super.onCreateDialog(id);
 	}
 
 	/**
      * Helper function to update the server list
      */
     public void updateListView() {
     	ServerListAdapter adapter = (ServerListAdapter) MCServerTrackerActivity.this.getListAdapter();
 		adapter.notifyDataSetChanged();
     }
     
     /**
      * @see android.app.Activity#onCreateOptionsMenu(Menu menu)
      */
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.server_list_menu, menu);
         return true;
     }
     
     /**
      * @see android.app.Activity#onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
      */
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
       super.onCreateContextMenu(menu, v, menuInfo);
       MenuInflater inflater = getMenuInflater();
       inflater.inflate(R.menu.server_context, menu);
       final AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
      
       Server server = serverList.getItem(info.position);
       
       
       
       if (!server.favorite) {
     	  menu.add(Menu.NONE, 124, Menu.NONE, R.string.favorites_add);
     	  
       } else {
     	  menu.add(Menu.NONE, 124, Menu.NONE, R.string.favorites_remove);
       }
  
     }
     
     /**
      * @see android.app.Activity#onOptionsItemSelected(MenuItem item)
      */
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case R.id.menu_refresh:
             serverList.refresh();
             lastRefresh = System.currentTimeMillis();
             return true;
         case R.id.menu_add_server:
             Intent addServer = new Intent(this, AddServerActivity.class);
             startActivityForResult(addServer, AddServerActivity.ADD_SERVER_ACTIVITY_ID);
             return true;
         case R.id.menu_search:
         	InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
         	mgr.showSoftInput(getListView(), InputMethodManager.SHOW_FORCED);
         	return true;
         default:
             return super.onOptionsItemSelected(item);
         }
     }
     
     /**
      * @see android.app.Activity#onContextItemSelected(MenuItem item)
      */
 	@Override
     public boolean onContextItemSelected(MenuItem item) {
       final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
       
       switch (item.getItemId()) {
       case R.id.context_copy:
     	  Server server = serverList.getItem(info.position);
     	  String address = server.address.toString() + ":" + server.port;
     	  ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
     	  clipboard.setText(address);
     	  Toast.makeText(this, address + " Copied", Toast.LENGTH_SHORT).show();
           return true;
       case R.id.context_delete:
     	  deleteServer(info.position);
           return true;
       case R.id.context_edit:
     	  editServer(info.position);
     	  return true;
       case 124:
     	  Server s = serverList.getItem(info.position);
     	  s.favorite = !s.favorite;
     	  serverList.sort();
     	  return true;
       default:
         return super.onContextItemSelected(item);
       }
     }
     
     /**
 	 * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
 	 */
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 				
 		if (keyCode == KeyEvent.KEYCODE_SEARCH) {
 			InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
         	mgr.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
         	return true;
 		}
 		return super.onKeyDown(keyCode, event);
 	}
 
 	/**
      * Presents a dialog confirming server deletion
      * @param position index of server to delete
      */
     private void deleteServer(final int position) {
     	AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setMessage(R.string.delete_caution);
 		builder.setPositiveButton(R.string.yes, new OnClickListener() {
 
 			@SuppressWarnings("unchecked")
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				serverList.remove(position);
 				new SaveServerListTask(getApplicationContext()).execute(serverList.getServerList());
 				updateListView();
 			}
 		});
 		builder.setNegativeButton(R.string.no, new OnClickListener() {
 
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				dialog.dismiss();
 			}
 		});
 
 		builder.create().show();
 
     }
     
     private void editServer(int position) {
     	Server server = serverList.getItem(position);
     	Intent addServer = new Intent(this, AddServerActivity.class);
     	addServer.putExtra(Server.SERVER_ID, server.id);
     	addServer.putExtra(Server.SERVER_NAME, server.name);
     	addServer.putExtra(Server.SERVER_ADDRESS, server.address);
     	addServer.putExtra(Server.SERVER_PORT, "" + server.port);
     	
         startActivityForResult(addServer, AddServerActivity.ADD_SERVER_ACTIVITY_ID);
     }
 
 	/**
 	 * Used to collect new server data from the AddServer Activity
 	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
 	 */
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		
 		if (requestCode == AddServerActivity.ADD_SERVER_ACTIVITY_ID && resultCode == RESULT_OK) {
 			String serverName = data.getStringExtra(Server.SERVER_NAME);
 			String serverAddress = data.getStringExtra(Server.SERVER_ADDRESS);
 			String serverPort = data.getStringExtra(Server.SERVER_PORT);
 			int serverId = data.getIntExtra(Server.SERVER_ID, -1);
 			
 			if (serverId == -1) {
 				Server newServer = new Server(serverName, serverAddress);
				newServer.port = Integer.parseInt(serverPort);
 				getServerData(newServer);
 			} else {
 				Server server = serverList.getItem(serverId);
 				server.name = serverName;
 				server.address = serverAddress;
				server.port = Integer.parseInt(serverPort);
 				server.queried = false;
 				serverList.sort();
 			}
 		}
 		
 		
 		super.onActivityResult(requestCode, resultCode, data);
 	}
 	
 	/**
 	 * Performs request for data from the server
 	 * @param server Server object to populate with data
 	 */
 	private void getServerData(Server server) {
 		final ProgressDialog dialog = ProgressDialog.show(this, "", "Requesting Server Info", true);
 		GetServerDataTask task = new GetServerDataTask(server, new GetServerDataTask.ServerDataResultHandler() {
 			
 			@SuppressWarnings("unchecked")
 			@Override
 			public void onServerDataResult(final Server server, String result) {
 				dialog.dismiss();
 				if (result == null) {
 					serverList.add(server);
 					new SaveServerListTask(getApplicationContext()).execute(serverList.getServerList());
 					updateListView();
 				} else {
 					AlertDialog.Builder builder = new AlertDialog.Builder(MCServerTrackerActivity.this);
 					builder.setMessage("Failed to contact server\n" + result);
 					builder.setPositiveButton("Try Again", new OnClickListener() {
 						
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							getServerData(server);
 						}
 					});
 					builder.setNegativeButton("Cancel", new OnClickListener() {
 						
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							dialog.dismiss();
 						}
 					});
 					builder.setNeutralButton("Back", new OnClickListener() {
 						
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							dialog.dismiss();
 							Intent addServer = new Intent(MCServerTrackerActivity.this, AddServerActivity.class);
 					    	addServer.putExtra(Server.SERVER_ID, server.id);
 					    	addServer.putExtra(Server.SERVER_NAME, server.name);
 					    	addServer.putExtra(Server.SERVER_ADDRESS, server.address);
 					    	addServer.putExtra(Server.SERVER_PORT, "" + server.port);
 					    	
 					        startActivityForResult(addServer, AddServerActivity.ADD_SERVER_ACTIVITY_ID);
 						}
 					});
 					
 					builder.create().show();
 				}
 			}
 		});
 		
 		
 		
 		task.execute();
 	}
     
     
   
 }
