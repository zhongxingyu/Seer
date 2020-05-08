 /*
  * Copyright (C) 2011 asksven
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
 
 import java.util.List;
 
 import android.app.AlertDialog;
 import android.content.ActivityNotFoundException;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.Uri;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.support.v4.app.FragmentTransaction;
 import android.support.v4.app.ListFragment;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ListView;
 import android.widget.Toast;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 
 import com.asksven.commandcenter.valueobjects.CollectionManager;
 import com.asksven.commandcenter.valueobjects.Command;
 import com.asksven.commandcenter.valueobjects.CommandCollection;
 import com.asksven.commandcenter.valueobjects.CommandListAdapter;
 import com.asksven.commandcenter.R;
 
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
     boolean mDualPane;
     int mCurCheckPosition = 0;
     
 //	private CommandDBHelper m_myDB = null;
     private List<Command> m_myItems;
     private Command m_myCommand = null;
     private String m_strCollectionName = null;
     private CommandListAdapter m_myAdapter = null;
     
     static final int CONTEXT_EDIT_ID 		= 100;
     static final int CONTEXT_DELETE_ID 		= 101;
     static final int CONTEXT_EXECUTE_ID 	= 102;
     static final int CONTEXT_ADDFAV_ID	 	= 103;
     
     /** each frgment gets its own ID for handling the context menu callback */
     int m_iContextMenuId = 0;
     
     static final int REQUEST_CODE_PICK_FILE_OR_DIRECTORY = 1;
 
 
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState)
     {
         super.onActivityCreated(savedInstanceState);
         
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
 
         // populate list with our commands, based on preferences
 //        m_myDB = new CommandDBHelper(getActivity());
         
 //        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
 //        boolean bShowFavs = preferences.getBoolean("showOnlyFavorites", false);
         
         CommandCollection myCollection =
         		CollectionManager.getInstance(getActivity()).getCollectionByName(m_strCollectionName);
      
         m_myItems = myCollection.getEntries();
         
 //        if (!bShowFavs)
 //        {
 //        	m_myItems = m_myDB.fetchAllRows();
 //        }
 //        else
 //        {
 //        	m_myItems = m_myDB.fetchFavoriteRows();
 //        }
         m_myAdapter = new CommandListAdapter(getActivity(), m_myItems);
         setListAdapter(m_myAdapter);
         
         registerForContextMenu(getListView()); 
 		
         // Check to see if we have a frame in which to embed the details
         // fragment directly in the containing UI.
         View detailsFrame = getActivity().findViewById(R.id.details);
         mDualPane = detailsFrame != null && detailsFrame.getVisibility() == View.VISIBLE;
 
         if (savedInstanceState != null)
         {
             // Restore last state for checked position.
             mCurCheckPosition = savedInstanceState.getInt("curChoice", 0);
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
         outState.putInt("curChoice", mCurCheckPosition);
     }
 
     @Override
     public void onListItemClick(ListView l, View v, int position, long id)
     {
     	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
     	boolean bexec = preferences.getBoolean("execOnSelect", false);
     	
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
             if (details == null || details.getShownKey() != m_myCommand.getId())
             {
                 // Make new fragment to show this selection.
                 details = BasicDetailsFragment.newInstance(key);
 
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
         menu.add(m_iContextMenuId, CONTEXT_EDIT_ID, Menu.NONE, "Edit");
         menu.add(m_iContextMenuId, CONTEXT_EXECUTE_ID, Menu.NONE, "Execute");
         
 //        mItem = menu.add(Menu.NONE, CONTEXT_ADDFAV_ID, Menu.NONE, "Add to Favorites");    
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
 	    		case CONTEXT_EDIT_ID:
 	    	    	
 	    	    	if (m_myCommand != null)
 	    	    	{
 	    	            showDetails(m_myCommand.getId());
 	    	    	}
 	    			return true;
 	     
 	    		case CONTEXT_EXECUTE_ID:
 	    	    	if (m_myCommand != null)
 	    	    	{
 		    			Log.i(getClass().getSimpleName(), "Running command");
 		    			executeCommand(m_myCommand);
 		    			refreshList();
 		    			return true;
 	   	    		}
 	
 	//    		case CONTEXT_ADDFAV_ID:
 	//    			m_myCommand.setFavorite(1);
 	//    			m_myDB.updateCommand(m_myCommand.getId(), m_myCommand);
 	//    			refreshList(-1); 
 	//    			return true;
 	
 	    			
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
 				    	
 				        cmd.execute(strSelection);
 				        Toast.makeText(getActivity(), "Executing " + m_myCommand.getCommand(), Toast.LENGTH_LONG).show();
 		    			refreshList();
 				    }
 				});
 				AlertDialog alert = builder.show();
 			}
 		}
 		else
 		{
 			m_myCommand.execute();
 			Toast.makeText(getActivity(), "Executing " + m_myCommand.getCommand(), Toast.LENGTH_LONG).show();
 
 		}
 	
     }
     
     private void refreshList()
     {
     	// todo refresh
     	m_myAdapter.notifyDataSetChanged();
     }
 
 
 }
