 /**
  * Copyright (C) 2012 SINTEF <fabien@fleurey.com>
  *
  * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.gnu.org/licenses/lgpl-3.0.txt
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.sensapp.android.sensappdroid.fragments;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.FragmentManager;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Color;
 import android.net.Uri;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.ListFragment;
 import android.support.v4.app.LoaderManager.LoaderCallbacks;
 import android.support.v4.content.CursorLoader;
 import android.support.v4.content.Loader;
 import android.util.Log;
 import android.view.*;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.widget.*;
 import android.widget.AbsListView.OnScrollListener;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import org.sensapp.android.sensappdroid.R;
 import org.sensapp.android.sensappdroid.activities.SensAppService;
 import org.sensapp.android.sensappdroid.api.SensAppHelper;
 import org.sensapp.android.sensappdroid.contract.SensAppContract;
 import org.sensapp.android.sensappdroid.database.MeasureTable;
 import org.sensapp.android.sensappdroid.datarequests.DeleteGraphSensorsTask;
 import org.sensapp.android.sensappdroid.datarequests.DeleteMeasuresTask;
 import org.sensapp.android.sensappdroid.graph.*;
 import org.sensapp.android.sensappdroid.preferences.GeneralPrefFragment;
 import org.sensapp.android.sensappdroid.preferences.PreferencesActivity;
 import org.sensapp.android.sensappdroid.restrequests.PutMeasuresTask;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class GraphsListFragment extends ListFragment implements LoaderCallbacks<Cursor>{
 
 	private static final int MENU_DELETE_ID = Menu.FIRST + 1;
 	private static final int MENU_MANAGE_ID = Menu.FIRST + 2;
 
     private SimpleCursorAdapter adapter;
 	private OnGraphSelectedListener graphSelectedListener;
     private NewGraphDialogFragment newGraphDialog;
     private GraphDetailsView graph;
 
     public interface OnGraphSelectedListener {
 		public void onGraphSelected(Uri uri);
 	}
 
     public static class NewGraphDialogFragment extends DialogFragment{
 
         public static NewGraphDialogFragment newInstance() {
             NewGraphDialogFragment frag = new NewGraphDialogFragment();
             return frag;
         }
 
         @Override
         public Dialog onCreateDialog(Bundle savedInstanceState) {
             View v = LayoutInflater.from(getActivity()).inflate(R.layout.alert_dialog_new_graph, null);
             final EditText graphName = (EditText) v.findViewById(R.id.graph_name_edit);
             return new AlertDialog.Builder(getActivity())
                     .setTitle("Add a new Graph")
                     .setView(v)
                     .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog, int whichButton) {
                             if(!graphName.getText().toString().isEmpty()){
                                 ContentValues values = new ContentValues();
                                 values.put(SensAppContract.Graph.TITLE, graphName.getText().toString());
                                Cursor c = getActivity().getContentResolver().query(Uri.parse(SensAppContract.Graph.CONTENT_URI + "/title/" + graphName.getText().toString()), null, null, null, null);
                                if(c.getCount()==0)
                                    getActivity().getContentResolver().insert(SensAppContract.Graph.CONTENT_URI, values);
                                c.close();
                                 values.clear();
                             }
                         }
                     }).create();
         }
     }
 
 	@Override
 	public void onAttach(Activity activity) {
 		super.onAttach(activity);
 		try {
 			graphSelectedListener = (OnGraphSelectedListener) activity;
 		} catch (ClassCastException e) {
 			throw new ClassCastException(activity.toString() + " must implement OnMeasureSelectedListener");
 		}
 	}
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		return inflater.inflate(R.layout.graph_list, container, false);
 	}
 	
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
         setHasOptionsMenu(true);
         newGraphDialog = new NewGraphDialogFragment();
         Cursor cursor = getActivity().getContentResolver().query(SensAppContract.Graph.CONTENT_URI, null, null, null, null);
         adapter = new SimpleCursorAdapter(getActivity(), R.layout.graph_simple_row, cursor, new String[]{SensAppContract.Graph.TITLE}, new int[]{R.id.graph_name});
         getLoaderManager().initLoader(0, null, this);
         setListAdapter(adapter);
         registerForContextMenu(getListView());
        //cursor.close();
 	}
 
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		inflater.inflate(R.menu.graph_menu, menu);
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		Intent i = new Intent(getActivity(), SensAppService.class);
 		Uri uri = getActivity().getIntent().getData();
 		if (uri == null) {
 			uri = SensAppContract.Measure.CONTENT_URI;
 		}
 		i.setData(uri);
 		switch (item.getItemId()) {
 		case R.id.add_graph:
             NewGraphDialogFragment.newInstance().show(getFragmentManager(), "NewGraphDialog");
             return true;
 		case R.id.preferences:
 			startActivity(new Intent(getActivity(), PreferencesActivity.class));
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 	
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 		menu.add(0, MENU_DELETE_ID, 0, "Delete graph");
 		menu.add(0, MENU_MANAGE_ID, 0, "Manage graph sensors");
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
         String title = getGraphTitleByID(info.id);
 		switch (item.getItemId()) {
 		case MENU_DELETE_ID:
             //Delete the graph
             new DeleteGraphSensorsTask(getActivity(), SensAppContract.Graph.CONTENT_URI).execute(SensAppContract.Graph.ID + " = " + info.id);
             //Delete Graph Sensors attached
             new DeleteGraphSensorsTask(getActivity(), SensAppContract.GraphSensor.CONTENT_URI).execute(SensAppContract.GraphSensor.GRAPH + " = " + info.id);
             return true;
 		case MENU_MANAGE_ID:
             ManageGraphSensorFragment.newInstance(title, info.id).show(getFragmentManager(), "ManageGraphDialog");
             return true;
 		}
 		return super.onContextItemSelected(item);
 	}
 	
 	@Override
 	public void onListItemClick(ListView l, View v, int position, long id) {
         Cursor c = getActivity().getContentResolver().query(Uri.parse(SensAppContract.Graph.CONTENT_URI + "/" + id), null, null, null, null);
 		//Only one graph
         c.moveToFirst();
         String graphName = c.getString(c.getColumnIndex(SensAppContract.Graph.TITLE));
         c.close();
         graphSelectedListener.onGraphSelected(Uri.parse(SensAppContract.Graph.CONTENT_URI + "/" + id + "/" + graphName));
 	}
 
     @Override
     public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
         String[] projection = {SensAppContract.Graph.TITLE};
         CursorLoader cursorLoader = new CursorLoader(getActivity(), SensAppContract.Graph.CONTENT_URI, projection, null, null, null);
         return cursorLoader;
     }
 
     @Override
     public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
         //adapter.swapCursor(cursor);
     }
 
     @Override
     public void onLoaderReset(Loader<Cursor> cursorLoader) {
         //adapter.swapCursor(null);
     }
 
     private String getGraphTitleByID(long id){
         Cursor cursor = getActivity().getContentResolver().query(SensAppContract.Graph.CONTENT_URI, new String[]{SensAppContract.Graph.TITLE}, SensAppContract.Graph.ID + " = " + id, null, null);
         //Only one data in the cursor
         cursor.moveToFirst();
         //Put the cursor on the first value then returns the first and only column
         String s = cursor.getString(0);
         cursor.close();
         return s;
     }
 }
