 package com.bonkler.flipdroid;
 
 import android.app.Activity;
 import android.os.Bundle;
 
 import android.util.Log;
 
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.ListFragment;
 import android.support.v4.app.LoaderManager;
 import android.support.v4.content.Loader;
 import android.support.v4.widget.*;
 
 import android.database.Cursor;
 import android.content.Intent;
 import android.view.View;
 import android.view.LayoutInflater;
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 
 import android.widget.TextView;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemLongClickListener;
 
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.ActionMode;
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.app.SherlockListFragment;
 
 public class MainActivity extends SherlockFragmentActivity
 {
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
      
         FragmentManager fm = getSupportFragmentManager();
 
         // if no fragment exists as the root view
         if (fm.findFragmentById(android.R.id.content) == null) {
             DeckListFragment list = new DeckListFragment();
             fm.beginTransaction().add(android.R.id.content, list).commit();
         }
     }
 
     // A fragment that will hold the list of FlipDecks
     public static class DeckListFragment extends SherlockListFragment implements LoaderManager.LoaderCallbacks<Cursor>, SimpleCursorAdapter.ViewBinder {
 
         private FlipCursorLoader mLoader;
         private SimpleCursorAdapter mAdapter;
         private static FlipDeck activeDeck;
 
         private ActionMode mActionMode;
 
         public static boolean needRefresh = false;
 
         String[] fromColumns = {FlipDroidContract.MyDecks.COLUMN_DECK_NAME, FlipDroidContract.MyDecks.COLUMN_DECK_CONTENTS};
         int[] toViews = {R.id.deck_name, R.id.card_total};
 
         @Override
         public void onActivityCreated(Bundle savedInstanceState) {
             super.onActivityCreated(savedInstanceState);
 
             // Initialize an adapter with a null cursor. We will update this cursor in the onLoadFinished() callback.
             mAdapter = new SimpleCursorAdapter(getSherlockActivity(), R.layout.deck_item, null, fromColumns, toViews, 0);
             mAdapter.setViewBinder(this);
             setEmptyText("No decks found.");
             setListAdapter(mAdapter);
             setListShownNoAnimation(false); // Don't display a loading animation.
 
             // Initialize the loader, using this fragment as the callback object.
             mLoader = (FlipCursorLoader) getSherlockActivity().getSupportLoaderManager().initLoader(1, null, this);
 
             // Enable context menu on long press for each list item.
             // registerForContextMenu(getListView());
 
             setHasOptionsMenu(true);
 
             getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
             getListView().setOnItemLongClickListener(new OnItemLongClickListener() {
                 @Override
                 public boolean onItemLongClick(AdapterView parent, View view, int position, long id) {
                     mActionMode = getSherlockActivity().startActionMode(mActionModeCallback);
                     getListView().setItemChecked(position, true);
                     activeDeck = new FlipDeck(mAdapter.getCursor(), position);
                     return true;
                 }
             });
         }
 
         @Override
         public void onResume() {
             super.onResume();
             if (needRefresh) {
                 needRefresh = false;
                 mLoader.onContentChanged();
             }
         }
 
         @Override
         public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
             inflater.inflate(R.menu.deck_action_menu, menu);
         }
 
         @Override
         public boolean onOptionsItemSelected(MenuItem item) {
             switch (item.getItemId()) {
                 case R.id.new_deck_option:
                     startNewDeck();
                     return true;
                 default:
                     return super.onOptionsItemSelected(item);
             }
         }
 
         private void startNewDeck() {
             AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
             LayoutInflater inflater = getActivity().getLayoutInflater();
             final View view = inflater.inflate(R.layout.new_deck_dialog, null); 
             builder.setView(view);
             builder.setMessage("Enter a name for this deck:");
             builder.setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int id) {
                     EditText name = (EditText) view.findViewById(R.id.new_deck_name);
                     String s = name.getText().toString();
                     FlipDeck deck = new FlipDeck(s);
                     mLoader.insert(deck);
                 }
             });
             builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int id) {
                     // dismiss the dialog.
                 }
             });
 
             AlertDialog dialog = builder.create();
             dialog.show();
         }
 
         private void startEditDeck() {
             AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
             LayoutInflater inflater = getActivity().getLayoutInflater();
             final View view = inflater.inflate(R.layout.new_deck_dialog, null);
 
             // Populate the text field
             EditText oldName = (EditText) view.findViewById(R.id.new_deck_name);
             int position = getListView().getCheckedItemPosition();
             Cursor c = mAdapter.getCursor();
             final FlipDeck deck = new FlipDeck(c, position);
             oldName.setText(deck.getName()); 
 
             builder.setView(view);
             builder.setMessage("Enter a new name for this deck:");
             builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int id) {
                     EditText name = (EditText) view.findViewById(R.id.new_deck_name);
                     String s = name.getText().toString();
                     deck.setName(s);
                     mLoader.update(deck);
                 }
             });
             builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int id) {
                     // dismiss the dialog.
                 }
             });
 
             AlertDialog dialog = builder.create();
             dialog.show();
         }
 
         private void startShuffleDeck() {
             AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
             LayoutInflater inflater = getActivity().getLayoutInflater();
             final View view = inflater.inflate(R.layout.base_deck_dialog, null);
             final FlipDeck deck = activeDeck;
 
             builder.setView(view);
             builder.setMessage("Shuffle this deck?");
             builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int id) {
                     deck.shuffleSelf();
                     mLoader.update(deck);
                 }
             });
             builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int id) {
                     // dismiss the dialog.
                 }
             });
 
             AlertDialog dialog = builder.create();
             dialog.show();
 
         }
 
         private void startDeleteDeck() {
             AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
             LayoutInflater inflater = getActivity().getLayoutInflater();
             final View view = inflater.inflate(R.layout.base_deck_dialog, null);
             final FlipDeck deck = activeDeck;
 
             builder.setView(view);
             builder.setMessage("Delete this deck and all its contents?");
             builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int id) {
                     mLoader.delete(deck);
                 }
             });
             builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int id) {
                     // dismiss the dialog.
                 }
             });
 
             AlertDialog dialog = builder.create();
             dialog.show();
 
         }
 
         private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
             // Called when the action mode is created; startActionMode() was called
             @Override
             public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                 // Inflate a menu resource providing context menu items
                 MenuInflater inflater = mode.getMenuInflater();
                 inflater.inflate(R.menu.deck_context_menu, menu);
                 return true;
             }
 
             // Called each time the action mode is shown. Always called after onCreateActionMode, but
             // may be called multiple times if the mode is invalidated.
             @Override
             public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                 return false; // Return false if nothing is done
             }
 
             // Called when the user selects a contextual menu item
             @Override
             public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                 switch (item.getItemId()) {
                     case R.id.edit_deck_option:
                         startEditDeck();
                         return true;
                     case R.id.shuffle_deck_option:
                         startShuffleDeck();
                         return true;
                     case R.id.delete_deck_option:
                         startDeleteDeck();
                         return true;
                     default:
                         return false;
                 }
             }
 
             // Called when the user exits the action mode
             @Override
             public void onDestroyActionMode(ActionMode mode) {
                 mActionMode = null;
                 int i = getListView().getCheckedItemPosition();
                 if (i >= 0)
                     getListView().setItemChecked(i, false);
             }
         };
 
         @Override
         public void onListItemClick(ListView I, View v, int position, long id) {
 
             Cursor c = mAdapter.getCursor();
             activeDeck = new FlipDeck(c, position);
 
             Intent intent = new Intent(getSherlockActivity(), BrowseDeckActivity.class);
             startActivity(intent);
         }
 
         public static FlipDeck getActiveDeck() {
             return activeDeck;
         }
 
         // VIEWBINDER
         @Override
         public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
             String s = cursor.getString(columnIndex);
             if (view instanceof TextView) {
                 if (view.getId() == R.id.card_total) {
                    if (s == null || s.length() == 0) {
                         s = "0 cards";
                     }
                     else if (!(s.contains(","))) {
                         s = "1 card";
                     }
                     else {
                         s = "" + s.split(",").length + " cards";
                     }
                 }
                 ((TextView) view).setText(s);
                 return true;
             }
             return false;
         }
 
         // LOADER CALLBACKS
 
         // Called when a new loader is needed.
         @Override
         public Loader<Cursor> onCreateLoader(int id, Bundle args) {
             return new FlipCursorLoader(
                 getActivity(),
                 FlipDroidDBHelper.getInstance(getActivity()),
                 FlipDroidContract.MyDecks.TABLE_NAME,
                 null,
                 null,
                 null,
                 null);
         }
 
         // Called when an existing loader finishes its loading task.
         @Override
         public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
             mAdapter.swapCursor(c); // Swap in our new cursor.
             setListShownNoAnimation(true);
         }
 
         // Called when an existing loader is reset.
         @Override
         public void onLoaderReset(Loader<Cursor> loader) {
             mAdapter.swapCursor(null);
         }
     }
 }
