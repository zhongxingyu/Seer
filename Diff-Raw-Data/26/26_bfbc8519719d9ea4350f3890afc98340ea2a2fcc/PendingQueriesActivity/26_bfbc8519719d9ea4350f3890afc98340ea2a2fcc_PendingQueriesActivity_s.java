 package com.bioinformaticsapp;
 
 import java.util.List;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.AlertDialog.Builder;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.SimpleCursorAdapter;
 
 import com.bioinformaticsapp.fragments.BLASTQueryParametersDialog;
 import com.bioinformaticsapp.models.BLASTQuery;
 import com.bioinformaticsapp.models.BLASTQuery.BLASTJob;
 import com.bioinformaticsapp.models.SearchParameter;
 
 public class PendingQueriesActivity extends BLASTQueryListingActivity {
 
 
 	private static final int RUNNING_CURSOR_LOADER = 0x03;
 	
 	private final static int REFRESH_MENU_ITEM = 0;
 	
 	/** Called when the activity is first created. */
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         
     	super.onCreate(savedInstanceState);
 		
 		filterCondition = BLASTJob.COLUMN_NAME_BLASTQUERY_JOB_STATUS +" = ?";
 		values = new String[]{BLASTQuery.Status.SUBMITTED.toString()};
     	
         int[] viewId = new int[]{R.id.query_job_id_label, R.id.query_job_status_label};
         
         //We only wish to show running and finished queries:
         String [] dataColumns = new String[]{BLASTJob.COLUMN_NAME_BLASTQUERY_JOB_ID, BLASTJob.COLUMN_NAME_BLASTQUERY_JOB_STATUS};
         
         getLoaderManager().initLoader(RUNNING_CURSOR_LOADER, null, this);
         
         mCursorAdapter = new SimpleCursorAdapter(this, R.layout.blastquery_list_item, null, dataColumns, viewId);
         
         setListAdapter(mCursorAdapter);
         
         registerForContextMenu(getListView());
      
     }
 
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onResume()
 	 */
 	@Override
 	protected void onResume() {
 		super.onResume();
 		getLoaderManager().restartLoader(RUNNING_CURSOR_LOADER, null, this);
 	}
 
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		
 		MenuItem item = menu.add(0, REFRESH_MENU_ITEM, 0, "Refresh");
 		
 		item.setIcon(android.R.drawable.ic_popup_sync);
 		
 		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
 		
 		return true;
 	}
 
 	
 
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		
 		boolean itemSelectionHandled = false;
 		
 		int itemId = item.getItemId();
 		
 		switch(itemId){
 		case REFRESH_MENU_ITEM:
 			getLoaderManager().restartLoader(RUNNING_CURSOR_LOADER, null, this);
 			itemSelectionHandled = true;
 			break;
 			
 		default:
 			itemSelectionHandled = super.onOptionsItemSelected(item);
 			break;
 			
 		}
 		
 		return itemSelectionHandled;
 	}
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 		
 		MenuInflater menuInflater = getMenuInflater();
 		menu.setHeaderTitle("Select an option:");
 		menuInflater.inflate(R.menu.general_context_menu, menu);
 		
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		
 		boolean itemSelectionHandled = false;
 		
 		AdapterView.AdapterContextMenuInfo menuinfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
 		
 		int itemId = item.getItemId();
 		
 		switch(itemId){
 		
 		case R.id.delete_menu_item: {
 			doDeleteAction(menuinfo.id);
 			itemSelectionHandled = true;
 		}
 		break;
 		
 		case R.id.view_parameters_menu_item: {
 			
			BLASTQueryParametersDialog dialog = new BLASTQueryParametersDialog();
 			BLASTQuery selected = queryController.findBLASTQueryById(menuinfo.id);
 			List<SearchParameter> parameters = parametersController.getParametersForQuery(menuinfo.id);
 			selected.updateAllParameters(parameters);
			Bundle bundle = new Bundle();
			bundle.putSerializable("query", selected);
			dialog.setArguments(bundle);
			dialog.show(getFragmentManager(), "dialog");
 			
 			itemSelectionHandled = true;
 		}
 		break;
 		
 		default:
 			itemSelectionHandled = super.onContextItemSelected(item);
 			break;
 		}
 		
 		getLoaderManager().restartLoader(RUNNING_CURSOR_LOADER, null, this);
 		
 		return itemSelectionHandled;
 		
 	}
 	
 	private void doDeleteAction(final long id){
 		
 		AlertDialog.Builder builder = new Builder(this);
 		builder = builder.setTitle("Deleting");
 		builder.setIcon(android.R.drawable.ic_dialog_alert);
 		builder = builder.setMessage(R.string.delete_query_message);
 		builder.setCancelable(false);
 		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
 			
 			public void onClick(DialogInterface dialog, int which) {
 				
 				deleteQuery(id);
 				getLoaderManager().restartLoader(RUNNING_CURSOR_LOADER, null, PendingQueriesActivity.this);
 			}
 		});
 		
 		builder.setNegativeButton("Cancel", null);
 		
 		Dialog dialog = builder.create();
 		dialog.show();
 		
 	}
 
 }
