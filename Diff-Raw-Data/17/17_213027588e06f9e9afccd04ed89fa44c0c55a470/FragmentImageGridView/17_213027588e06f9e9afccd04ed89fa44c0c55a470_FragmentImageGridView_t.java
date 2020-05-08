 package com.plingnote;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Point;
 import android.os.Bundle;
 import android.os.Vibrator;
 import android.support.v4.app.Fragment;
 import android.util.SparseBooleanArray;
 import android.view.ActionMode;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
 import android.widget.AbsListView.MultiChoiceModeListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.BaseAdapter;
 import android.widget.GridView;
 import android.widget.ImageView;
 import android.widget.TextView;
 /**
  * a public class that shows a list of notes in a gridview.
  * @author magnushuttu
  *
  */
 public class FragmentImageGridView extends Fragment implements OnItemClickListener{
 
 	public FragmentImageGridView(){}
 
 	private LayoutInflater layoutInflater;
 	private Point imgSize;
 	private DatabaseHandler db;
 	private List<Note> notes = new ArrayList<Note>();		
 	private ActionMode actionBar;
 	private GridView gView;
	private ImageAdapter imgAdapter;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 	}
 
 	public void setImgSize(int x, int y){
 		imgSize.set(x, y);
 	}
 
 	public static FragmentImageGridView instantiate(int index){
 		FragmentImageGridView f = new FragmentImageGridView();
 
 		Bundle args = new Bundle();
 		args.putInt("index", index);
 
 
 		f.setArguments(args);
 		return f;
 
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		if (container == null){
 			return null;
 		}
 		db = DatabaseHandler.getInstance(getActivity());;
 		final View grid;
 		if(getResources().getConfiguration().orientation == getResources().getConfiguration().ORIENTATION_PORTRAIT){
 			grid = inflater.inflate(R.layout.fragment_gridview, container, false); 
 		} else{
 			grid = inflater.inflate(R.layout.fragment_gridview_land, container, false); 
 		}
		imgAdapter = new ImageAdapter(getActivity());
 		gView = (GridView) grid.findViewById(R.id.grid);
		gView.setAdapter(imgAdapter);
 		gView.setOnItemClickListener(this);
 
 		// Make it possible for the user to select multiple items.
 		gView.setChoiceMode(gView.CHOICE_MODE_MULTIPLE_MODAL);
 		gView.setMultiChoiceModeListener(new LongPress());
 
 		layoutInflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
 		imgSize = new Point();
 		int side = getActivity().getResources().getDisplayMetrics().widthPixels / 4 ;
 		setImgSize(side,side);
 
 		refreshNotes();
 
 		return grid;
 	}
 
 
 	public class ImageAdapter extends BaseAdapter {
 		private final Context mContext;
 
 
 		public ImageAdapter(Context context) {
 			super();
 			mContext = context;
 		}
 
 		@Override
 		public int getCount() {
 			return numberOfNotes();
 		}
 
 		@Override
 		public Object getItem(int position) {
 			return imageIds;
 		}
 
 		@Override
 		public long getItemId(int position) {
 			return position;
 		}
 
 		/**
 		 * Returns a View with three view baked into it, these create an image with some 
 		 * text on top.
 		 */
 		public View getView(int position, View convertView, ViewGroup parent) {
 			View v = new View(getActivity());
 			v = layoutInflater.inflate(R.layout.image_item, null);
 			ImageView imgView = (ImageView) v.findViewById(R.id.gridview_image);
 			TextView tvTitle = (TextView) v.findViewById(R.id.gridview_image_title);
 			TextView tvText = (TextView) v.findViewById(R.id.gridview_image_text);
 
 			if (convertView == null) {
 				tvTitle.findViewById(R.id.gridview_image_title);
 				tvText.findViewById(R.id.gridview_image_text);
 				tvTitle.setText(notes.get(position).getTitle());
 				tvText.setText(notes.get(position).getText());
 			} else {
 				v = convertView;
 			}
 
 			imgView.setBackgroundResource(imageIds[position]);
 			return v;
 		}
 
 		/**
 		 * stores all the used images.
 		 */
 		private int[] imageIds = new int[]{
 				R.drawable.category_banking, R.drawable.category_chat, R.drawable.category_fun,
 				R.drawable.category_lunch, R.drawable.category_meeting, R.drawable.category_shop,
 				R.drawable.category_write, R.drawable.category_banking, R.drawable.category_chat, R.drawable.category_fun,
 				R.drawable.category_lunch, R.drawable.category_meeting, R.drawable.category_shop,
 				R.drawable.category_write, R.drawable.category_banking, R.drawable.category_chat, R.drawable.category_fun,
 				R.drawable.category_lunch, R.drawable.category_meeting, R.drawable.category_shop,
 				R.drawable.category_write
 		};
 
 	}
 
 	/**
 	 * Deletes all the notes in the list notes and then adds freshly from the database.
 	 */
 	public void refreshNotes(){
 		clearNotes();
 
 		for(Note n : db.getNoteList()){
 			addNote(n);
 		}
		gView.setAdapter(imgAdapter);
 	}
 
 	/**
 	 * adds a note to the list notes.
 	 * @param note adds this object to the list.
 	 */
 	public void addNote(Note note){
 		notes.add(note);
 	}
 
 	/**
 	 * Deletes all notes in the list notes.
 	 */
 	public void clearNotes(){
 		notes.clear();
 	}
 
 	/**
 	 * @return returns an int with the size of the list notes.
 	 */
 	public int numberOfNotes(){
 		return notes.size();
 	}
 
 	@Override
 	public void onItemClick(AdapterView parent, View v, int position, long id) {
 		Intent editNote = new Intent(getActivity(), ActivityNote.class);
 
 		// Get the ID of the clicked note.
 		int noteId = notes.get(position).getId();
 		editNote.putExtra(IntentExtra.id.toString(), noteId);
 
 		startActivity(editNote);		
 	}
 
 	/**
 	 * Remove checked notes from the list.
 	 */
 	public void removeItem() {
 		// Get the positions of all the checked items.
 		SparseBooleanArray checkedItemPositions = gView
 				.getCheckedItemPositions();
 		
 		// Walk through the notes and delete the checked ones.
 		for (int i = notes.size() - 1; i >= 0; i--) {
 			if (checkedItemPositions.get(i)) {
 				db.deleteNote(notes.get(i).getId());
 			}
 		}
 		// Refresh the note list.
 		refreshNotes();
 	}
 
 
 	/**
 	 * Private class handling long presses, which forces the action bar to show
 	 * up. Users can also choose multiple notes.
 	 * 
 	 * @author Magnus Huttu
 	 * 
 	 */
 	private class LongPress implements MultiChoiceModeListener {
 
 		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
 			// Listen to user input and perform the action of choice.
 			switch (item.getItemId()) {
 			case R.id.remove:
 				removeItem(); // Delete the selected notes.
				imgAdapter.notifyDataSetChanged();
 				mode.finish(); // Close the action bar after deletion.
 			default:
 				return false;
 			}
 
 		}
 
 		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
 			actionBar = mode;
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
 			gView.getCheckedItemPositions().clear();
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
			
 			switch (gView.getCheckedItemCount()) {
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
 				mode.setSubtitle("" + gView.getCheckedItemCount()
 						+ " notes selected");
 				break;
 			}
 		}
 	}
 
 	/**
 	 * Close the contextual action bar (top menu) when changing to map view.
 	 */
 	@Override
 	public void setUserVisibleHint(boolean isActive) {
 		super.setUserVisibleHint(isActive);
 
 		// Check if current view
 		if (isVisible()) {
 			if (!isActive) {
 				// If user leaves the list view, a close the top menu.
 				if(actionBar != null) {
 					actionBar.finish();
 				}
 			}
 		}
 	}
 }
