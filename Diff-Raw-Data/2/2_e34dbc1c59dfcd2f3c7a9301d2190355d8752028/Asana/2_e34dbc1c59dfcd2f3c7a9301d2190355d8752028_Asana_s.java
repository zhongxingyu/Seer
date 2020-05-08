 package com.designs_1393.asana;
 
 import com.designs_1393.asana.workspace.*;
 
 // General
 import android.os.Bundle;
 import android.content.SharedPreferences;
 import android.content.Context;
 import android.view.View;
 
 import android.support.v4.app.DialogFragment;
 
 // Shared preferences
 import android.content.SharedPreferences;
 
 // Database
 import android.database.Cursor;
 
 // Widgets
 import android.widget.ListView;
 import android.widget.ExpandableListView;
 import android.widget.ExpandableListView.OnChildClickListener;
 
 // ActionBarSherlock
 import com.actionbarsherlock.app.SherlockExpandableListActivity;
 
 // View
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.LayoutInflater;
 
 // Widgets
 import android.widget.TextView;
 import android.widget.EditText;
 
 // Links
 import android.text.util.Linkify;
 
 // Dialog
 import android.app.Dialog;
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 
 import android.util.Log;
 
 /**
  * Main application activity.
  */
 public class Asana extends SherlockExpandableListActivity
 {
 	final String APP_TAG = "Asana";
 	private SharedPreferences sharedPrefs;
 	private DialogFragment apiKeyDialog;
 
 	private Context ctx;
 	private Cursor workspaceCursor;
 
 	private DatabaseAdapter dbAdapter;
 
 
 	private void showDialog()
 	{
 		View v = getLayoutInflater()
 		         .inflate(R.layout.apikeydialog, null);
 		TextView instructions =
 			(TextView) v.findViewById( R.id.apiKeyInstructions );
 
 		final EditText apiKeyInput =
 			(EditText) v.findViewById( R.id.apiKeyEntry );
 
 		// apply links to instructions
 		Linkify.addLinks( instructions, Linkify.WEB_URLS );
 
 		new AlertDialog.Builder( this )
 			.setView( v )
 			.setTitle( "API Key" )
 			.setCancelable( true )
 			.setPositiveButton( "Okay", new DialogInterface.OnClickListener() {
 					public void onClick( DialogInterface dialog, int which )
 					{
 						SharedPreferences.Editor editor = sharedPrefs.edit();
 						if( editor == null )
 						{
 							Log.i(APP_TAG, "Editor is null");
 						}
 						editor.putString(
 							"api key",
 							apiKeyInput.getText().toString());
 						editor.commit();
 					}
 				})
 			.setNegativeButton( "Cancel", new DialogInterface.OnClickListener() {
 				public void onClick( DialogInterface dialog, int which ){}
 			}).create().show();
 	}
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 
 		// store application context
 		ctx = getApplicationContext();
 
 		sharedPrefs = getSharedPreferences("AsanaPrefs", Context.MODE_PRIVATE);
 
 		if(sharedPrefs.getString( "api key", "not found" ).equals("not found"))
 		{
 			showDialog();
 		}
 
 
 		// get shared preferences containing API key
 		sharedPrefs = getSharedPreferences(
 			"AsanaPrefs",
 			Context.MODE_PRIVATE);
 
 		// get and store workspaces from Asana
 		AsanaFacade aFacade = new AsanaFacade( sharedPrefs, ctx );
 		aFacade.retreiveWorkspaces();
 		aFacade.retreiveProjects();
 
 		// set layout content from the cache database
 		dbAdapter = new DatabaseAdapter( ctx );
 		dbAdapter.open();
 
 		workspaceCursor = dbAdapter.getWorkspaces( true );
		setListAdapter( new ExpandableWorkspaceAdapter( ctx, workspaceCursor ) );
 		dbAdapter.close();
 
 		setContentView( R.layout.workspace_list );
 	}
 
 
 	@Override
 	public boolean onChildClick( ExpandableListView parent,
 	                             View v,
 	                             int groupPosition,
 	                             int childPosition,
 	                             long id )
 	{
 		Cursor childrenCursor = ((ExpandableWorkspaceAdapter)getExpandableListAdapter())
 		                            .getChildrenCursor( workspaceCursor );
 		childrenCursor.moveToPosition( childPosition );
 
 		long projectID = childrenCursor.getLong(
 		                     childrenCursor.getColumnIndex(
 		                         DatabaseAdapter.PROJECTS_COL_ASANA_ID
 		                     )
 		                 );
 
 		String projectName = childrenCursor.getString(
 		                         childrenCursor.getColumnIndex(
 		                             DatabaseAdapter.PROJECTS_COL_NAME
 		                         )
 		                     );
 
 		childrenCursor.close();
 
 		Log.i( APP_TAG, "Project with ID: " +projectID +" clicked!" );
 		Log.i( APP_TAG, "Project with name: " +projectName +" clicked!" );
 
 		// we've handled the click, so return true
 		return true;
 	}
 
 
 	public void onListItemClick( ListView l, View v, int pos, long id )
 	{
 		Cursor tempCursor = workspaceCursor;
 		tempCursor.moveToPosition( pos );
 
 		String workspaceName =
 			tempCursor.getString(
 				tempCursor.getColumnIndexOrThrow(
 					DatabaseAdapter.WORKSPACES_COL_NAME
 				)
 			);
 
 		Log.i( APP_TAG, workspaceName +" pressed!" );
 	}
 }
