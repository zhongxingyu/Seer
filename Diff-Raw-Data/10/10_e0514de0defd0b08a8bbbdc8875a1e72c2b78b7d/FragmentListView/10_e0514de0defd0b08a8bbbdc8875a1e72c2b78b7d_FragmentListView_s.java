 package com.plingnote;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Vibrator;
 import android.support.v4.app.ListFragment;
 import android.util.SparseBooleanArray;
 import android.view.ActionMode;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ListView;
 
 /**
  * Class displaying the saved notes as a list. The user can click on a note to
  * view or edit it.
  * 
  * @author Linus Karlsson
  * 
  */
 public class FragmentListView extends ListFragment {
 	private DatabaseHandler db;
 	private NoteAdapter noteAdapter;
 	private List<Note> notes = new ArrayList<Note>();
 
 	@Override
 	public void onActivityCreated(Bundle savedState) {
 		super.onActivityCreated(savedState);
 
 		db = DatabaseHandler.getInstance(getActivity());
 		// addNotes();
 		refreshNotes();
 
 		// Make it possible for the user to select multiple items.
 		getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
 		getListView().setMultiChoiceModeListener(new LongPress());
 
 		noteAdapter = new NoteAdapter(getActivity(),
 				android.R.layout.simple_list_item_activated_1, notes);
 		setListAdapter(noteAdapter);
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		return inflater.inflate(R.layout.fragment_listview, container, false);
 	}
 
 	/**
 	 * Handles single clicks on the notes. Opens the note of choice in a new
 	 * activity.
 	 */
 	@Override
 	public void onListItemClick(ListView parent, View v, int position, long id) {
 		Intent editNote = new Intent(getActivity(), ActivityNote.class);
 
 		// Get the row ID of the clicked note.
 		int rowID = notes.get(position).getRowId();
 		editNote.putExtra(Utils.QUERY_NOTE, rowID);
 		
 		// Start edit view.
 		startActivity(editNote);
 	}
 
 	/**
 	 * Private class handling long presses, which forces the action bar to show
 	 * up. Users can also choose multiple notes.
 	 * 
 	 * @author Linus Karlsson
 	 * 
 	 */
 	private class LongPress implements ListView.MultiChoiceModeListener {
 
 		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
 			// Listen to user input and perform the action of choice.
 			switch (item.getItemId()) {
 			case R.id.remove:
 				removeListItem(); // Delete the selected notes.
 				mode.finish(); // Close the action bar after deletion.
 			default:
 				return false;
 			}
 
 		}
 
 		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
 
 			// Make the mobile vibrate on long click
 			((Vibrator) getActivity()
 					.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(50);
 
 			// Display contextual action bar to user.
 			MenuInflater inflater = mode.getMenuInflater();
 			inflater.inflate(R.menu.multi_select_menu, menu);
 			mode.setTitle("Select notes");
 			return true;
 		}
 
 		public void onDestroyActionMode(ActionMode mode) {
 			// Nothing to do here
 		}
 
 		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
 			return true;
 		}
 
 		/**
 		 * Called everytime the state of the list is changed, for example when a
 		 * note is selected. Displays the number of selected notes to the user.
 		 */
 		public void onItemCheckedStateChanged(ActionMode mode, int position,
 				long id, boolean checked) {
 
 			switch (getListView().getCheckedItemCount()) {
 			case (0):
 				// If no note is selected, don't set any subtitle.
 				mode.setSubtitle(null);
 				break;
 			case (1):
 				// If one note is selected
 				mode.setSubtitle("One note selected");
 				break;
 			default:
 				// If more than one time are selected, display the number of
 				// selected notes to user.
 				mode.setSubtitle("" + getListView().getCheckedItemCount()
 						+ " notes selected");
 				break;
 			}
 		}
 
 	}
 
 	/**
 	 * Get notes from database and add them to a list.
 	 */
 	public void refreshNotes() {
 
 		// Clear list from previous notes.
 		notes.clear();
 
 		for (Note n : db.getNoteList()) {
 			this.notes.add(n);
 		}
 	}
 
 	/**
 	 * The number of notes displayed to the user.
 	 * 
 	 * @return number of notes displayed on the screen.
 	 */
 	public int numberOfNotes() {
 		return noteAdapter.getCount();
 	}
 
 	/**
 	 * Remove checked notes from the list.
 	 */
 	public void removeListItem() {
 
 		// Get the positions of all the checked items.
 		SparseBooleanArray checkedItemPositions = getListView()
 				.getCheckedItemPositions();
 
 		// Walk through the notes and delete the checked ones.
 		for (int i = getListView().getCount() - 1; i >= 0; i--) {
 			if (checkedItemPositions.get(i)) {
 				db.deleteNote(notes.get(i).getRowId());
 			}
 		}
 
 		// Refresh the note list.
 		refreshNotes();
 
 		// Update the adapter.
 		noteAdapter.notifyDataSetChanged();
 	}
 
 	public void addNotes() {
 		for (int i = 1; i <= 15; i++) {
 			db.insertNote("Note nr." + i,
 					"This is the actual text of the note", null, "img", null);
 		}
 	}
 
 }
