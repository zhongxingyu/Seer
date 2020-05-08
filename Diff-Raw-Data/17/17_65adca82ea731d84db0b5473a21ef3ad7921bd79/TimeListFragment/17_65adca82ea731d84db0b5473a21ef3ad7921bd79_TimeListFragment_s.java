 package com.anirudhr.timeMan;
 
 import android.app.AlertDialog;
 import android.content.ContentValues;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.LoaderManager;
 import android.support.v4.content.Loader;
 import android.support.v4.content.CursorLoader;
 import android.view.ContextMenu;
 import android.view.View;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.widget.ListView;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.app.SherlockListFragment;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.anirudhr.timeMan.R;
 import com.anirudhr.timeMan.db.MyTodoContentProvider;
 import com.anirudhr.timeMan.db.TodoTable;
 
 public class TimeListFragment extends SherlockFragmentActivity
 {
 	static String[] dummyData;
 	private static final int ACTIVITY_CREATE = 0;
 	private static final int ACTIVITY_EDIT = 1;
 	private static final int ADD_PROJECT_ID = Menu.FIRST;
 	private static final int DELETE_ID = Menu.FIRST + 1;
 	private static final int EDIT_ID = Menu.FIRST + 2;
 
 	public static class CurrentListFragment extends SherlockListFragment implements LoaderManager.LoaderCallbacks<Cursor>	{
         private TimeListAdapter mAdapter = null;
         private TimeUtilites ts;
         private XmlUtilites utilities;
         private Handler resetHandler = new Handler();
         private Runnable resetTask;
         
         @Override 
         public void onCreate(Bundle savedInstanceState){
         	super.onCreate(savedInstanceState);
 	        GlobalAccess.sha = getSherlockActivity();
         }
         @Override public void onActivityCreated(Bundle savedInstanceState) {
 	        super.onActivityCreated(savedInstanceState);
 	        setEmptyText("No data");
             registerForContextMenu(getListView());
             setListShown(false);
             getLoaderManager().initLoader(0, null, this);
 	        setHasOptionsMenu(true);
 	        
 	        //create instance of the timeStructures
 	        ts = new TimeUtilites(getActivity());
 	        utilities = new XmlUtilites(getActivity());
         }	
 		@Override
 		public void onResume(){
 			super.onResume();
 			trackExpiration();
 			
 			if(resetHandler!=null)
 				resetHandler.removeCallbacks(resetTask);
 			resetTask = new Runnable() {
 				public void run() {			
 						trackExpiration();
 					}
 				};
 	        resetHandler.postDelayed(resetTask, ts.getExpirationTime() - System.currentTimeMillis());
 		}
 		
 		public void trackExpiration(){
 			if(ts.timeToReset()){
 				utilities.writeToXML();
 				try{
 					GlobalAccess.changeData();
 				}catch(NullPointerException e){
 					//changeData will be called by StatisticsFragment.allowStaticAccess() at a later time, dont worry
 				}
 				resetDatabase();
 			}
 		}
         
 		@Override
 		public void onCreateContextMenu(ContextMenu menu, View v,
 		  ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 		menu.add(0, DELETE_ID, 0, R.string.menu_delete);
 		menu.add(0, EDIT_ID, 1, R.string.menu_edit);
 		}
 		
 		@Override
 		public boolean onContextItemSelected(android.view.MenuItem item) {
 			final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
 			switch (item.getItemId()) {
 			case DELETE_ID:
 				AlertDialog.Builder builder = new AlertDialog.Builder(getView().getContext());
 	        	builder.setMessage("Are you sure you want to delete this item?")
 	        	       .setCancelable(false)
 	        	       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 	        	           public void onClick(DialogInterface dialog, int id) {
 	        	                try{
 	        	                	Uri uri = Uri.parse(MyTodoContentProvider.CONTENT_URI + "/" + info.id);
 	        	      			  	getActivity().getContentResolver().delete(uri, null, null);
 	        	                }catch(Exception e){
 	        	                	e.printStackTrace();
 	        	                }
 	        	           }
 	        	       })
 	        	       .setNegativeButton("No", new DialogInterface.OnClickListener() {
 	        	           public void onClick(DialogInterface dialog, int id) {
 	        	                dialog.cancel();
 	        	           }
 	        	       });
 	        	AlertDialog alert = builder.create();
 	        	alert.show();
 			  return true;
 			case EDIT_ID:
 			Intent i = new Intent(getActivity(), TaskDetailActivity.class);
 			Uri todoUri = Uri.parse(MyTodoContentProvider.CONTENT_URI + "/" + info.id);
 			i.putExtra(MyTodoContentProvider.CONTENT_ITEM_TYPE, todoUri);
 			startActivityForResult(i, ACTIVITY_EDIT);
 			}
 			return super.onContextItemSelected(item);
 		}
 		
 		@Override
 		public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 			MenuItem projectItem = menu.add(Menu.NONE, ADD_PROJECT_ID, 0, "Add Activity");
 			projectItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
 			super.onCreateOptionsMenu(menu, inflater);
 	    }
 		
 		@Override public void onListItemClick(ListView l, View v, int position, long id) {
 			if(ts.getRunning() == 0){
 				//no task running at all
 				final long start = System.currentTimeMillis();
 				ts.setCurrentTaskStart(start);
 				ts.setRunning(id);
 				ts.startTimer(v, start, id);
 			}
 			else
 			{
 				//stop all running tasks
 				ts.killTimer(v);
 				updateDatabase(ts.getRunning());
 				ts.setRunning(0);
 			}
         }
 		private void updateDatabase(long id){
			long time = System.currentTimeMillis() - ts.getCurrentTaskStart() + ts.getOffsetFromDatabase(id);
			ContentValues values = new ContentValues();
			values.put(TodoTable.COLUMN_ID, id);
			values.put(TodoTable.COLUMN_TIME, time);
			Uri todoUri = Uri.parse(MyTodoContentProvider.CONTENT_URI + "/" + id);
			getActivity().getContentResolver().update(todoUri, values, null, null);
 		}
 		
 		private void resetDatabase(){
 			//updates and fills ALL entries with set value ##CAREFUL
 			ContentValues values = new ContentValues();
 			values.put(TodoTable.COLUMN_TIME, 0);
 			Uri todoUri = Uri.parse(String.valueOf(MyTodoContentProvider.CONTENT_URI));
 			GlobalAccess.a.getContentResolver().update(todoUri, values, null, null);
 		}
 		
 		 @Override public boolean onOptionsItemSelected(MenuItem item) {
             switch (item.getItemId()) {
             	case ADD_PROJECT_ID:
             		Intent i = new Intent(getActivity(), TaskDetailActivity.class);
             		startActivityForResult(i, ACTIVITY_CREATE);
             		break;
             }	
             return false;
 		 }
 		static final String[] projection = { TodoTable.COLUMN_ID, TodoTable.COLUMN_ACTIVITY, TodoTable.COLUMN_TIME, TodoTable.COLUMN_PRIORITY, TodoTable.COLUMN_PRODUCTIVE };
 		
 		@Override
 		public Loader<Cursor> onCreateLoader(int id, Bundle args) {
 			String sortOrder = TodoTable.COLUMN_PRIORITY;
 			CursorLoader cursorLoader = new CursorLoader(getActivity(), 
 					MyTodoContentProvider.CONTENT_URI, projection, null, null, sortOrder);
 			return cursorLoader;
 		}
 		@Override
 		public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
 			if(mAdapter == null){
 				mAdapter = new TimeListAdapter(getActivity().getApplicationContext(), getActivity(), data, true);
 				setListAdapter(mAdapter);
 			}else{
 				mAdapter.changeCursor(data);
 			}
             if (isResumed()) {
                 setListShown(true);
             } else {
                 setListShownNoAnimation(true);
             }
 		}
 		@Override
 		public void onLoaderReset(Loader<Cursor> loader) {
 			//mAdapter.swapCursor(null);
 		}
 	}
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		FragmentManager fm = getSupportFragmentManager();
 		if (fm.findFragmentById(android.R.id.content) == null) {
 			CurrentListFragment list = new CurrentListFragment();
 			fm.beginTransaction().add(android.R.id.content, list).commit();
 		}		
 	}
 }
