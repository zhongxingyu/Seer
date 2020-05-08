 package com.app.wordservant;
 
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.CursorAdapter;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.SimpleCursorAdapter;
 import android.widget.TextView;
 
 public class ScriptureBankFragment extends Fragment{
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
     }
 	private class ScriptureQuerier extends AsyncTask<Void, Void, Cursor>{
 
 		@Override
 		protected Cursor doInBackground(Void... params) {
 			// Gets the scripture that matches the scripture id.
 			String [] columns_to_retrieve = {WordServantContract.ScriptureEntry._ID, WordServantContract.ScriptureEntry.COLUMN_NAME_REFERENCE};
 	        SQLiteDatabase wordservantReadableDatabase = new WordServantDbHelper(getActivity(), WordServantContract.DB_NAME, null, WordServantDbHelper.DATABASE_VERSION).getWritableDatabase();
 			Cursor scriptureQuery = wordservantReadableDatabase.query(WordServantContract.ScriptureEntry.TABLE_NAME, columns_to_retrieve, null, null, null, null, null);
 			scriptureQuery.moveToFirst();
 			return scriptureQuery;
 		}
 		protected void onPostExecute(final Cursor scriptureQuery){
 
 			
 	        String [] fromColumns = {WordServantContract.ScriptureEntry.COLUMN_NAME_REFERENCE};
 			int [] toViews = {R.id.list_entry};
 			CursorAdapter scriptureAdapter = new SimpleCursorAdapter(getActivity(), R.layout.list_layout, scriptureQuery, fromColumns, toViews);
 			ListView scriptureList = (ListView) getActivity().findViewById(R.id.scripture_bank_list);
 			
 			//Remove progress bar and show list.
 			ProgressBar pBar = (ProgressBar) getActivity().findViewById(R.id.loadingCircle);
 			TextView loadingText = (TextView) getActivity().findViewById(R.id.loadingText);
 			LinearLayout listLayout = (LinearLayout) getActivity().findViewById(R.id.listLayout);
 			listLayout.setGravity(Gravity.NO_GRAVITY);
 			pBar.setVisibility(ProgressBar.GONE);
 			loadingText.setVisibility(TextView.GONE);
 			scriptureList.setVisibility(ListView.VISIBLE);
 			
 			scriptureList.setAdapter(scriptureAdapter);
 			getActivity().registerForContextMenu(scriptureList);
 			scriptureList.setOnItemClickListener(new OnItemClickListener(){
 				public void onItemClick(AdapterView<?> parent, View linearLayout, int position,long id) {
 					// TODO Auto-generated method stub
 					// Bundle data
 					Intent intent = new Intent(linearLayout.getContext(),EditScripture.class);
 					scriptureQuery.moveToPosition(position);
 					intent.putExtra("scripture_id",scriptureQuery.getInt(0));
 			    	startActivity(intent);
 					
 				}
 			});
 		}
 	}
     
     /**
      * When the activity starts clear out any scriptures displayed on the screen 
      * and populate the screen based on the scriptures in the database.
      */
     public void onStart(){
     	super.onStart();
     	new ScriptureQuerier().execute();
     	
     	//Functionality for the input scripture button.
         Button inputScripture = (Button) getActivity().findViewById(R.id.input_scripture);
         inputScripture.setOnClickListener(new OnClickListener(){
 
 			public void onClick(View scriptureBank) {
 				// Starts a new input scripture.
 				if(getActivity().findViewById(R.id.fragmentHolder)==null){
 					Intent intent = new Intent(scriptureBank.getContext(),InputScriptureManual.class);
 			    	startActivity(intent);
 				}else{
 					FragmentManager fragmentManager = getFragmentManager();
 					FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
 					
 					InputScriptureFragment inputScriptureFragment = new InputScriptureFragment();
 					fragmentTransaction.add(R.id.fragmentHolder, inputScriptureFragment, "input_scripture");
 					fragmentTransaction.commit();
 				}
 
 			}
         	
         });
         
         //Functionality for the selected scripture button.
         Button selectScripture = (Button) getActivity().findViewById(R.id.select_scripture);
         selectScripture.setOnClickListener(new OnClickListener(){
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				Intent intent = new Intent(getActivity(),SelectScripture.class);
 				startActivity(intent);
 			}
         	
         });
 
     }
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
 		return inflater.inflate(R.layout.scripture_bank, null);
 		
 	}
 	public void redraw() {
 		// Reset the progress bar layout.
		ProgressBar pBar = (ProgressBar) getActivity().findViewById(R.id.progressBar1);
 		TextView loadingText = (TextView) getActivity().findViewById(R.id.loadingText);
 		LinearLayout listLayout = (LinearLayout) getActivity().findViewById(R.id.listLayout);
 		ListView scriptureList = (ListView) getActivity().findViewById(R.id.scripture_bank_list);
 		listLayout.setGravity(Gravity.CENTER);
 		pBar.setVisibility(ProgressBar.VISIBLE);
 		loadingText.setVisibility(TextView.VISIBLE);
 		scriptureList.setVisibility(ListView.GONE);
 		
 		new ScriptureQuerier().execute();
 	}
 
 }
