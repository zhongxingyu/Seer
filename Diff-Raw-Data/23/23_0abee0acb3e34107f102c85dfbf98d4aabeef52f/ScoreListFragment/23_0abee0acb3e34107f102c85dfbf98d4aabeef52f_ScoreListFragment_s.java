 package com.livejournal.karino2.guitarscorevisualizer;
 
 import android.app.Activity;
 import android.content.Context;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.support.v4.app.ListFragment;
 import android.support.v4.app.LoaderManager;
 import android.support.v4.content.Loader;
 import android.view.ActionMode;
 import android.view.ContextMenu;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AbsListView;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 import android.widget.TextView;
 
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 /**
  * A list fragment representing a list of Scores. This fragment
  * also supports tablet devices by allowing list items to be given an
  * 'activated' state upon selection. This helps indicate which item is
  * currently being viewed in a {@link ScoreDetailFragment}.
  * <p/>
  * Activities containing this fragment MUST implement the {@link Callbacks}
  * interface.
  */
 public class ScoreListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
 
     /**
      * The serialization (saved instance state) Bundle key representing the
      * activated item position. Only used on tablets.
      */
     private static final String STATE_ACTIVATED_POSITION = "activated_position";
 
     Database getDatabase(Context ctx) { return Database.getInstance(ctx); }
 
 
     /**
      * The fragment's current callback object, which is notified of list item
      * clicks.
      */
     private Callbacks mCallbacks = sDummyCallbacks;
 
     /**
      * The current activated item position. Only used on tablets.
      */
     private int mActivatedPosition = ListView.INVALID_POSITION;
 
     @Override
     public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
         SimpleCursorLoader loader;
         return new SimpleCursorLoader(getActivity()) {
 
             @Override
             public Cursor loadInBackground() {
                 Cursor cursor = getDatabase(getContext()).getScoreInfos();
                 if(cursor!=null){
                     cursor.getCount();
                 }
                 return cursor;
             }
         };
     }
 
     @Override
     public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
         adapter.swapCursor(cursor);
     }
 
     @Override
     public void onLoaderReset(Loader<Cursor> cursorLoader) {
         adapter.swapCursor(null);
     }
 
 
     /**
      * A callback interface that all activities containing this fragment must
      * implement. This mechanism allows activities to be notified of item
      * selections.
      */
     public interface Callbacks {
         /**
          * Callback for when an item has been selected.
          */
         public void onItemSelected(long id);
     }
 
     /**
      * A dummy implementation of the {@link Callbacks} interface that does
      * nothing. Used only when this fragment is not attached to an activity.
      */
     private static Callbacks sDummyCallbacks = new Callbacks() {
         @Override
         public void onItemSelected(long id) {
         }
     };
 
     /**
      * Mandatory empty constructor for the fragment manager to instantiate the
      * fragment (e.g. upon screen orientation changes).
      */
     public ScoreListFragment() {
     }
 
 
     SimpleCursorAdapter adapter;
 
 
     public void reloadCursor() {
         Loader<Object> loader = getLoaderManager().getLoader(0);
         if(loader != null)
             loader.forceLoad();
     }
 
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
 
         adapter = new SimpleCursorAdapter(getActivity(), R.layout.score_list_item, null, new String[]{"DATE", "TITLE", "SCORE"}, new int[]{R.id.textViewDate, R.id.textViewSubject, R.id.textViewScore}, 0);
         adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder(){
 
             public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                 if(columnIndex == 1)
                 {
                     TextView tv = (TextView)view;
                     SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
                     tv.setText(sdf.format(new Date(cursor.getLong(columnIndex))));
                     tv.setTag(cursor.getLong(0)); // ID
                     return true;
                 }
                 return false;
             }});
 
 
         setListAdapter(adapter);
         getLoaderManager().initLoader(0, null, this);
 
     }
 
     @Override
     public void onViewCreated(View view, Bundle savedInstanceState) {
         super.onViewCreated(view, savedInstanceState);
         getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
         getListView().setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
             @Override
             public void onItemCheckedStateChanged(ActionMode actionMode, int i, long l, boolean b) {
 
             }
 
             @Override
             public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                 MenuInflater inflater = actionMode.getMenuInflater();
                 inflater.inflate(R.menu.list_context, menu);
                 return true;
             }
 
             @Override
             public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                 return false;
             }
 
             @Override
             public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                 switch(menuItem.getItemId()) {
                     case R.id.list_context_delete_item:
                         long[] ids = getListView().getCheckedItemIds();
                         for(long itemId : ids) {
                             getDatabase(getActivity()).deleteScore(itemId);
                         }
                         reloadCursor();
                         actionMode.finish();
                         return true;
                 }
                 return false;
             }
 
             @Override
             public void onDestroyActionMode(ActionMode actionMode) {
 
             }
         });
 
         // Restore the previously serialized activated item position.
         if (savedInstanceState != null
                 && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
             setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
         }
     }
 
     @Override
     public void onAttach(Activity activity) {
         super.onAttach(activity);
 
         // Activities containing this fragment must implement its callbacks.
         if (!(activity instanceof Callbacks)) {
             throw new IllegalStateException("Activity must implement fragment's callbacks.");
         }
 
         mCallbacks = (Callbacks) activity;
     }
 
     @Override
     public void onDetach() {
         super.onDetach();
 
         // Reset the active callbacks interface to the dummy implementation.
         mCallbacks = sDummyCallbacks;
     }
 
     @Override
     public void onListItemClick(ListView listView, View view, int position, long id) {
         super.onListItemClick(listView, view, position, id);
 
         // Notify the active callbacks interface (the activity, if the
         // fragment is attached to one) that an item has been selected.
         mCallbacks.onItemSelected((Long)view.findViewById(R.id.textViewDate).getTag());
     }
 
 
     @Override
     public void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         if (mActivatedPosition != ListView.INVALID_POSITION) {
             // Serialize and persist the activated item position.
             outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
         }
     }
 
     /**
      * Turns on activate-on-click mode. When this mode is on, list items will be
      * given the 'activated' state when touched.
      */
     public void setActivateOnItemClick(boolean activateOnItemClick) {
         // When setting CHOICE_MODE_SINGLE, ListView will automatically
         // give items the 'activated' state when touched.
         getListView().setChoiceMode(activateOnItemClick
                 ? ListView.CHOICE_MODE_SINGLE
                 : ListView.CHOICE_MODE_NONE);
     }
 
     private void setActivatedPosition(int position) {
         if (position == ListView.INVALID_POSITION) {
             getListView().setItemChecked(mActivatedPosition, false);
         } else {
             getListView().setItemChecked(position, true);
         }
 
         mActivatedPosition = position;
     }
 }
