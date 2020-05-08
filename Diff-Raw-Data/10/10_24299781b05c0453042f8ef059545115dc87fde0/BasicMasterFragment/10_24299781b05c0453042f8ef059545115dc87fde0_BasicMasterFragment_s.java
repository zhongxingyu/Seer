 /*
  * Copyright (C) 2011-12 asksven
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.asksven.commandcenter;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.ActivityNotFoundException;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.support.v4.app.FragmentTransaction;
 import android.support.v4.app.ListFragment;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.FrameLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 
 import com.asksven.android.system.Devices;
 import com.asksven.commandcenter.utils.Configuration;
 import com.asksven.commandcenter.valueobjects.CollectionManager;
 import com.asksven.commandcenter.valueobjects.Command;
 import com.asksven.commandcenter.valueobjects.CommandCollection;
 import com.asksven.commandcenter.valueobjects.CommandDBHelper;
 import com.asksven.commandcenter.valueobjects.CommandListAdapter;
 import com.asksven.commandcenter.R;
 import com.google.ads.AdRequest;
 import com.google.ads.AdView;
 
 /**
  * @author sven
  *
  */
 /**
  * This is the "top-level" fragment, showing a list of items that the
  * user can pick.  Upon picking an item, it takes care of displaying the
  * data to the user as appropriate based on the currrent UI layout.
  */
 public class BasicMasterFragment extends ListFragment
 {
 	private static final String TAG = "BasicMasterFragment";
     boolean mDualPane;
     int mCurCheckPosition = 0;
     
     private List<Command> m_myItems;
     
     /** the currently selected command (to be run by thread */    
     private Command m_myCommand = null;
     
     private String m_strCollectionName = null;
     private CommandListAdapter m_myAdapter = null;
     boolean m_bEditable = false;
     
     static final int CONTEXT_EDIT_ID 		= 100;
     static final int CONTEXT_VIEW_ID 		= 101;
     static final int CONTEXT_DELETE_ID 		= 102;
     static final int CONTEXT_EXECUTE_ID 	= 103;
     static final int CONTEXT_ADDUSER_ID	 	= 104;
     static final int CONTEXT_ADD_ID		 	= 105;
     static final int CONTEXT_REFRESH	 	= 106;
     static final int CONTEXT_RELOAD		 	= 107;
     
     /** each frgment gets its own ID for handling the context menu callback */
     int m_iContextMenuId = 0;
     
     static final int REQUEST_CODE_PICK_FILE_OR_DIRECTORY = 1;
 
 
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState)
     {
         super.onActivityCreated(savedInstanceState);
 
     	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
     	boolean bUpdateCache = preferences.getBoolean("autoRunStatus", true);
     	boolean bForceDualPane = preferences.getBoolean("dualPaneOnSmallScreens", false);
     	        
 		// detect free/full version and enable/disable ads
 		if (!Configuration.isFullVersion(getActivity()))
 		{
 			AdView adView = (AdView)getActivity().findViewById(R.id.adView);
 			// if ads are part of the view 
 			if (adView != null)
 			{
 				adView.loadAd(new AdRequest());
 			}
 		}
 
         Bundle args = getArguments();
         if (args != null)
         {
         	m_strCollectionName = args.getString("collection");
         	m_iContextMenuId = args.getInt("id");
         }
         else
         {
         	m_strCollectionName = "commands.json";
         }
         
         // refresh thread
         this.refreshCommandsCache();
         
         CommandCollection myCollection =
         		CollectionManager.getInstance(getActivity()).getCollectionByName(m_strCollectionName, false);
      
         m_bEditable = myCollection.isEditable();
         
         m_myItems = myCollection.getEntries();
         
         m_myAdapter = new CommandListAdapter(getActivity(), m_myItems);
         setListAdapter(m_myAdapter);
         
         registerForContextMenu(getListView()); 
 		
         // Check to see if we have a frame in which to embed the details
         // fragment directly in the containing UI.
         View detailsFrame = getActivity().findViewById(R.id.details);
         boolean bPortrait = detailsFrame != null && detailsFrame.getVisibility() == View.VISIBLE;
         if (bPortrait)
         {
         	if ( (Devices.isTablet(getActivity())) || bForceDualPane )
         	{
         		mDualPane = true;
         		FrameLayout details = (FrameLayout)getActivity().findViewById(R.id.details);
         		details.setVisibility(View.VISIBLE);
 
         	}
         	else
         	{
         		mDualPane = false;
         		FrameLayout details = (FrameLayout)getActivity().findViewById(R.id.details);
         		details.setVisibility(View.GONE);
         	}
         }
 
         if (savedInstanceState != null)
         {
             // Restore last state for checked position.
             mCurCheckPosition = savedInstanceState.getInt(m_strCollectionName + "_curChoice", 0);
         }
 
         if (mDualPane)
         {
             // In dual-pane mode, the list view highlights the selected item.
             getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
             // Make sure our UI is in the correct state.
             showDetails(mCurCheckPosition);
         }
     }
 
     @Override
     public void onSaveInstanceState(Bundle outState)
     {
         super.onSaveInstanceState(outState);
         outState.putInt(m_strCollectionName + "_curChoice", mCurCheckPosition);
     }
 
     @Override
     public void onResume()
     {
     	super.onResume();
     	
     	// if editable refresh from database
     	if (m_bEditable)
     	{
     		m_myAdapter.reloadFromDatabase();
     	}
     	else
     	{
     		// refresh thread
     		this.refreshCommandsCache();         
     	}
     }
     
     /**
      * Run a thread to refresh the cached command status 
      */
     private void refreshCommandsCache()
     {
     	// we don't use an AsyncTask here because of the limitiation
     	// see http://stackoverflow.com/questions/4080808/asynctask-doinbackground-does-not-run
     	Thread myThread = new Thread(new Runnable()
     	{
     	    public void run()
     	    {
     	    	Log.i(TAG, "Refreshing command cache");
     			// update Command List cache
     	    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
     	    	boolean updateCache = preferences.getBoolean("autoRunStatus", true);
 
         		CommandCollection myCollection =
                 		CollectionManager.getInstance(getActivity()).getCollectionByName(m_strCollectionName, updateCache);
                 m_myItems = myCollection.getEntries();
                 if (m_myAdapter != null)
                 {
                 	m_myAdapter.notifyDataSetChanged();
                 }
                 Log.i(TAG, "Finished refreshing command cache");
 
     	    }
     	});
     	getActivity().runOnUiThread(myThread);
     	
     }
 
 
     @Override
     public void onListItemClick(ListView l, View v, int position, long id)
     {
     	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
     	boolean bexec = preferences.getBoolean("execOnSelect", false);
     	m_myAdapter.setSelection(position);
     	
     	if (bexec)
     	{
 			Log.i(getClass().getSimpleName(), "Running command");
 			m_myCommand = m_myItems.get(position);
 			executeCommand(m_myCommand);
 			refreshList();    		
     	}
     	else
     	{
 	    	m_myCommand = m_myItems.get(position);
 	        showDetails(m_myCommand.getId());
     	}
     }
 
     /**
      * Helper function to show the details of a selected item, either by
      * displaying a fragment in-place in the current UI, or starting a
      * whole new activity in which it is displayed.
      */
     void showDetails(int key)
     {
         mCurCheckPosition = key;
 
         if (mDualPane)
         {
             // We can display everything in-place with fragments, so update
             // the list to highlight the selected item and show the data.
             getListView().setItemChecked(key, true);
 
             // Check what fragment is currently shown, replace if needed.
             BasicDetailsFragment details = (BasicDetailsFragment)
                     getFragmentManager().findFragmentById(R.id.details);
 
             if ((m_myCommand == null) || (details == null) || (details.getShownKey() != m_myCommand.getId()) ) 
             {
                 // Make new fragment to show this selection.
                 details = BasicDetailsFragment.newInstance(key, m_strCollectionName);
 
                 // Execute a transaction, replacing any existing fragment
                 // with this one inside the frame.
                 FragmentTransaction ft = getFragmentManager().beginTransaction();
                 ft.replace(R.id.details, details);
                 ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                 ft.commit();
             }
 
         }
         else
         {
             // Otherwise we need to launch a new activity to display
             // the dialog fragment with selected text.
             Intent intent = new Intent();
             intent.setClass(getActivity(), BasicDetailsActivity.class);
             intent.putExtra("index", key);
             intent.putExtra("collection", m_strCollectionName);
             startActivity(intent);
         }
     }
     
     public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo info)
     {
     	super.onCreateContextMenu(menu, v, info);
         menu.setHeaderTitle("Actions");
         // menu depends on capabilities
         if (m_bEditable)
         {
         	menu.add(m_iContextMenuId, CONTEXT_ADD_ID, Menu.NONE, "Add");
         	menu.add(m_iContextMenuId, CONTEXT_EDIT_ID, Menu.NONE, "Edit");
         	menu.add(m_iContextMenuId, CONTEXT_DELETE_ID, Menu.NONE, "Delete");
         	menu.add(m_iContextMenuId, CONTEXT_EXECUTE_ID, Menu.NONE, "Execute");
         	
         }
         else
         {
         	menu.add(m_iContextMenuId, CONTEXT_VIEW_ID, Menu.NONE, "View");
         	menu.add(m_iContextMenuId, CONTEXT_EXECUTE_ID, Menu.NONE, "Execute");
         	menu.add(m_iContextMenuId, CONTEXT_ADDUSER_ID, Menu.NONE, "Copy to User");
 //        	menu.add(m_iContextMenuId, CONTEXT_REFRESH, Menu.NONE, "Refresh");
 //        	menu.add(m_iContextMenuId, CONTEXT_RELOAD, Menu.NONE, "Reload");
         }
    } 
     
     @Override
     public boolean onContextItemSelected(MenuItem item)
     {
     	AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo(); 
     	
     	// check if the called back fragment is the one that has initiated the menu action
     	// based on the group id. if not do noting
     	if (item.getGroupId() == m_iContextMenuId)
     	{
    		m_myCommand = m_myItems.get(menuInfo.position);
     	
 	    	switch(item.getItemId())
 	    	{
 	    		case CONTEXT_ADD_ID:    	    	
 	    	    	if (m_myCommand != null)
 	    	    	{
 	    	            showDetails(-1);
 	    	    	}
 	    			return true;
     			
 	    		case CONTEXT_DELETE_ID:
 	    	    	
 	    	    	if (m_myCommand != null)
 	    	    	{
 	    	    		Log.i(TAG, "Deleting command");
 	    	    		// TODO add yes/no dialog
 	    	    		deleteCommand(m_myCommand.getId());
 	    	    	}
 	    			return true;
 
 	    		case CONTEXT_EDIT_ID:
 	    	    	
 	    	    	if (m_myCommand != null)
 	    	    	{
 	    	    		Log.i(TAG, "Editing command");
 	    	            showDetails(m_myCommand.getId());
 	    	    	}
 	    			return true;
 	     
 	    		case CONTEXT_EXECUTE_ID:
 	    	    	if (m_myCommand != null)
 	    	    	{
 		    			Log.i(TAG, "Running command");
 		    			executeCommand(m_myCommand);
 		    			refreshList();
 	   	    		}	
 	    			return true;
 	    	    	
 	    		case CONTEXT_RELOAD:
 	    	    	if (m_myCommand != null)
 	    	    	{
 //		    			new RefreshCommandsCacheTask().execute("");
 		    			this.refreshCommandsCache();
 	   	    		}	
 	    			return true;
 	
 	    		case CONTEXT_REFRESH:
 	    	    	if (m_myCommand != null)
 	    	    	{
 		    			refreshList();
 		    			return true;
 	   	    		}	
 
 	    		case CONTEXT_ADDUSER_ID:
 	    	    	if (m_myCommand != null)
 	    	    	{
 		    			Log.i(TAG, "Copying command to user commands");
 		    			copyToUser(m_myCommand);
 		    			refreshList();
 		    			return true;
 	   	    		}	
 
 	    		default:
 	    			return false;
 	    	}
     	}
     	else
     	{
     		return super.onContextItemSelected(item);
     	}
 
     	
      } 
     
     /** execute the selected command */
     private final void executeCommand(final Command cmd)
     {
         SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
         
 		if (!cmd.getCommandValues().equals(""))
 		{
 			// handle whatever values are defined for the command
 			// values can be either a list of "|" separated items to pick from
 			// or a sub-command to be executed first
 			// sub-commands are of the form "??something??:somewhere"
 			// allowed sub-commands are 
 			String strPickFile = "??pickfile??";
 			String strPickDir = "??pickdir??";
 			// using OpenIntent's FileManager: http://www.openintents.org/en/node/159
 			
 			
 			if ((cmd.getCommandValues().startsWith(strPickFile)) || (cmd.getCommandValues().startsWith(strPickDir)))
 			{
 				// check for additional params in the sub-command
 				CharSequence[] tokens = cmd.getCommandValues().split("\\:");
 				String strSuggestion = "";
 				if (tokens.length > 1)
 				{
 					strSuggestion = (String)tokens[1];
 				}
 				Intent myIntent = null;
 				if (cmd.getCommandValues().startsWith(strPickFile))
 				{
 					myIntent = new Intent("org.openintents.action.PICK_FILE");
 					myIntent.putExtra("org.openintents.extra.TITLE", "Pick a file");
 					myIntent.putExtra("org.openintents.extra.BUTTON_TEXT", "Pick");
 					
 				}
 				if (cmd.getCommandValues().startsWith(strPickDir))
 				{
 					myIntent = new Intent("org.openintents.action.PICK_DIRECTORY");
 					myIntent.putExtra("org.openintents.extra.TITLE", "Pick a directory");
 					myIntent.putExtra("org.openintents.extra.BUTTON_TEXT", "Pick");
 				}
 				
 				if (myIntent == null)
 				{
 					Toast.makeText(getActivity(), "sub-command could not be resolved, check the syntax of your command", 
 							Toast.LENGTH_SHORT).show();
 					return;
 				}
 				if (!strSuggestion.equals(""))
 				{
 					myIntent.setData(Uri.parse("file://" + strSuggestion));
 				}
 				
 				try
 				{
 					startActivityForResult(myIntent, REQUEST_CODE_PICK_FILE_OR_DIRECTORY);
 				}
 				catch (ActivityNotFoundException e)
 				{
 					// No compatible file manager was found.
 					Toast.makeText(getActivity(), "You must install OpenIntent's FileManager to use this feature", 
 							Toast.LENGTH_SHORT).show();
 				}
 				
 			}
 			else
 			{
 				final CharSequence[] items = cmd.getCommandValues().split("\\|");
 	
 				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 				builder.setTitle("Pick a Value");
 				builder.setCancelable(false);
 	
 				builder.setItems(items, new DialogInterface.OnClickListener() {
 				    public void onClick(DialogInterface dialog, int item) {
 				    	String strSelection = (String) items[item];
 				    	CharSequence[] tokens = strSelection.split("\\:");
 				    	strSelection = (String) tokens[0];
 				    	
 				    	m_myCommand = cmd;
 //				        cmd.execute(strSelection);
 				        new ExecuteCommandTask().execute(strSelection);
 				        Toast.makeText(getActivity(), "Executing " + m_myCommand.getCommand(), Toast.LENGTH_LONG).show();
 		    			//refreshList();
 		    			//refreshCommandsCache();
 				    }
 				});
 				AlertDialog alert = builder.show();
 			}
 		}
 		else
 		{
 //			ArrayList<String> myRes = m_myCommand.execute();
 			new ExecuteCommandTask().execute("");
 			Toast.makeText(getActivity(), "Executing " + m_myCommand.getCommand(), Toast.LENGTH_LONG).show();
 //			showDialog(myRes);
 			//refreshList();
 			//refreshCommandsCache();
 			
 
 		}
 	
     }
     
     private void showDialog(ArrayList<String> myItems)
     {
     	if ( (myItems != null) && (myItems.size() > 0) )
     	{
 //        	Dialog dialog = new Dialog(getActivity());
 //        	
 //        	dialog.setContentView(R.layout.dialog);
 //        	dialog.setTitle("Returned");
 //
 //        	
 //        	String strText = "";
 //        	if (myItems != null)
 //        	{
 //        		for (int i=0; i<myItems.size(); i++)
 //        		{
 //    				strText = strText + myItems.get(i) + "\n";
 //        		}
 //        	}
 //        	TextView text = (TextView) dialog.findViewById(R.id.text);
 //        	text.setText(strText);
 //        	dialog.show();
 
     		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
         	boolean bShowTopLines = preferences.getBoolean("show_first_lines", false);
         	int nLines = 5;
         	try
         	{
             	nLines = Integer.valueOf(preferences.getString("number_lines", "5"));
         	}
         	catch (Exception e)
         	{
         	}
 
 
     		String strText = "";
 
         	if (myItems != null)
         	{
         		int nFrom = 0;
         		int nTo = myItems.size();
         		
         		// calculate what lines are to be shown depending on prefs
         		if (bShowTopLines)
         		{
         			if (nLines > 0)
         			{
         				nTo = nFrom + nLines + 1;
         			}
         		}
         		else
         		{
         			if (nLines > 0)
         			{
         				nFrom = nTo - nLines;
         			}
         		}
         		
         		for (int i = nFrom; i < nTo; i++)
         		{
     				strText = strText + myItems.get(i) + "\n";
         		}
         	}
         	m_myAdapter.showResult(strText);
 
     	}
     }
     private void refreshList()
     {
     	// todo refresh
     	m_myAdapter.notifyDataSetChanged();
     }
     
     void deleteCommand(int iKey)
     {
     	// delete the command from the database and reload
     	// works only for "user" commands
     	if (m_bEditable)
     	{
     		m_myAdapter.deleteItem(iKey);
     	}
     	else
     	{
     		Log.e(TAG, "deleteCommand can not be called for non editable command sets");
     	}
     }
     
     void copyToUser(Command myCommand)
     {
     	CommandDBHelper myDB = new CommandDBHelper(getActivity());
     	myCommand.setName("Copy of " + myCommand.getName());
     	myDB.addCommand(myCommand);
     }
     
 	private class ExecuteCommandTask extends AsyncTask<String, Void, ArrayList<String>>
 	{
 	     protected ArrayList<String> doInBackground(String... args)
 	     {
 	         return m_myCommand.execute(args[0]);
 	     }
 
 	     protected void onPostExecute(ArrayList<String> result)
 	     {
 	         //mImageView.setImageBitmap(result);
 	    	 showDialog(result);
 	    	 refreshList();
 	     }
 	 }
 
 
 }
