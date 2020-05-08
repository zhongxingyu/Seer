 package com.megginson.sloopsql;
 
 import android.app.Fragment;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.inputmethod.EditorInfo;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 import com.megginson.sloopsql.R;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Activity for executing SQL queries.
  */
 public class ScriptFragment extends Fragment
 {
 
     //
 	// Constants
 	//
 
 	/**
 	 * Property name for saved SQL query.
 	 */
 	public final static String SCRIPT_TEXT_PROPERTY = "queryText";
 
 
 	/**
 	 * Static constructor
 	 */
 	public final static ScriptFragment newInstance(String scriptText)
 	{
 		ScriptFragment fragment =new ScriptFragment();
 		fragment.mScriptText = scriptText;
 		return fragment;
 	}
 
 
 	//
 	// Internal fragment state
 	//
 
 	/**
 	 * The fragment's root view. set in {@link #onCreateView}
 	 */
 	private View mFragmentView;
 
 	/**
 	 * The database we're currently querying.
 	 */
 	private SQLiteDatabase mDatabase;
 
 	/**
 	 * The current SQL query text.
 	 */
 	private String mScriptText;
 
 	//
 	// Fragment lifecycle methods
 	//
 
     /** 
 	 * Lifecycle event: fragment first created.
 	 */
     @Override
     public void onCreate(Bundle savedInstanceState)
 	{
         super.onCreate(savedInstanceState);
 		setHasOptionsMenu(true);
 
 		if (savedInstanceState != null)
 		{
 			mScriptText = savedInstanceState.getString(SCRIPT_TEXT_PROPERTY);
 		}
 
 		mDatabase = new DatabaseHandler(getActivity()).getReadableDatabase();
     }
 
 	/**
 	 * Lifecycle event: fragment destroyed.
 	 */
 	@Override
 	public void onDestroy()
 	{
 		super.onDestroy();
 
 		// free resources
 
 		if (mDatabase != null)
 		{
 			mDatabase.close();
 			mDatabase = null;
 		}
 	}
 
 	/**
 	 * Lifecycle event: fragment creates or recreates its view.
 	 */
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
 	{
 		mFragmentView = inflater.inflate(R.layout.script, container, false);
 
 		setup_ui();
 
 		return mFragmentView;
 	}
 
 	/**
 	 * Lifecycle event: Android wants us to save the instance state.
 	 */
 	@Override
 	public void onSaveInstanceState(Bundle savedInstanceState)
 	{
 		savedInstanceState.putString(SCRIPT_TEXT_PROPERTY, mScriptText);
 	}
 
 	/**
 	 * Lifecycle event: Android is creating the options menu.
 	 */
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
 	{
 		inflater.inflate(R.menu.script_menu, menu);
 	}
 
 	/**
 	 * Lifecycle event: the user has selected a menu item.
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item)
 	{
 		// Handle item selection
 		switch (item.getItemId())
 		{
 			case R.id.item_execute_script:
 				do_execute_script();
 				return true;
 			default:
 				return super.onOptionsItemSelected(item);
 		}
 	}
 
 
 	//
 	// Callbacks for actions a user has performed in the UI
 	//
 
 	/**
 	 * Action: execute the SQL script.
 	 *
 	 * Use the text content of {@link #mQueryView} for the query and assign
 	 * to {@link #mQueryText}
 	 */
 	private void do_execute_script()
 	{		
 		mScriptText = get_script_view().getText().toString();
 
 		if (mScriptText != null && mScriptText.length() > 0)
 		{
			String statements[] = Util.splitSQL(mScriptText).toArray(new String[0]);
			if (statements.length > 0)
			{
				new QueryTask().execute(statements);
				return;
			}
 		}
 	}
 
 	//
 	// UI methods
 	//
 	// (It's not safe to store references to UI components directly
 	// in variables, since the fragment's state can outlast UI
 	// changes, so we use dynamic accessors instead.)
 	//
 
 	/**
 	 * Set up the UI components of the activity.
 	 */
 	private void setup_ui()
 	{
 		EditText scriptView = get_script_view();
 		scriptView.setText(mScriptText);
 	}
 
 	/**
 	 * Get the script edit view from the UI.
 	 *
 	 * This is the view that holds the SQL text for our script.
 	 *
 	 * @return the script view, or null if the UI isn't set up.
 	 */
 	private EditText get_script_view()
 	{
 		if (mFragmentView != null)
 		{
 			return (EditText)mFragmentView.findViewById(R.id.edit_script);
 		}
 		else
 		{
 			return null;
 		}
 	}
 
 
 	//
 	// Internal helper classes
 	//
 
 	/**
 	 * Task for running a database query in the background.
 	 */
 	private class QueryTask extends AsyncTask<String, Integer, AsyncResult<Cursor>>
 	{
 
 		/**
 		 * Text of the SQL query.
 		 */
 		private String mScriptText;
 
 
 		/**
 		 * Run the SQL query
 		 *
 		 * This method runs in a background thread.  The {@link ASyncTask}
 		 * takes care of getting it to {@link #onPostExecute} in the
 		 * main thread.
 		 *
 		 * @param queries An array of SQL queries to execute (always
 		 * just one for now)
 		 * @return The result of executing the query.
 		 */
 		@Override
 		protected AsyncResult<Cursor> doInBackground(String ... queries)
 		{
 			Cursor cursor = null;
 			try
 			{
 				for (int i = 0; i < queries.length; i++)
 				{
 					cursor = mDatabase.rawQuery(queries[i], null);
 					// save the successful query text
 					mScriptText = queries[i];
 				} 
 				return new AsyncResult<Cursor>(cursor);
 			}
 			catch (Throwable t)
 			{
 				return new AsyncResult<Cursor>(t);
 			}
 		}
 
 		/**
 		 * Handle the SQL result
 		 * 
 		 * This method runs in the main UI thread.  It receives the
 		 * rescult of {@link #doInBackground}, and can use it to
 		 * modify the UI safely.
 		 *
 		 * @param result The result of the background query.
 		 */
 		@Override
 		protected void onPostExecute(AsyncResult<Cursor> result)
 		{
 			// If there's an error, tell the user and proceed no further.
 			if (result.isError())
 			{
 				Util.toast(getActivity(), result.getThrowable().getMessage());
 				return;
 			}
 
 			// Register the new query result
 			Cursor cursor = result.getResult();
 			if (result == null)
 			{
 				return;
 			}
 
 			// Look up the views we're going to need.
 			TextView messageView = (TextView)mFragmentView.findViewById(R.id.text_message);
 
 			// Set up the message view.
 			messageView.setText(String.format(getString(R.string.message_query_result), cursor.getCount()));
 			cursor.close();	
 		}

 	}
 
 }
 
