 package net.potterpcs.recipebook;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentTransaction;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.LayoutInflater;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ListView;
 
 public class DirectionsEditor extends Fragment {
 	// Bundle extra for the directions array
 	static final String STATE_DIRS = "directions";
 	
 	// Bundle extra for the photos array
 	static final String STATE_PHOTOS = "direction-photos";
 	
 	// Handle to the parent activity
 	private RecipeEditor activity;
 
 	// ListView and ListAdapter
 	ListView listview;
 	private ArrayAdapter<String> adapter;
 	
 	// Lists for the recipe's directions and the photos for each direction
 	ArrayList<String> directions;
 	ArrayList<String> photoUris;
 	
 	// The sequence number of the current direction (for move up/down operations)
 	int currentDirection;
 	
	// The URI of the current photo (for editing and moving)
	String currentPhoto;
	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		activity = (RecipeEditor) getActivity();
 		long rid = activity.getRecipeId();
 		directions = new ArrayList<String>();
 		photoUris = new ArrayList<String>();
 		currentDirection = -1;
		currentPhoto = null;
 		
 		if (savedInstanceState != null) {
 			// Load from the saved state if possible
 			String[] savedDirections = savedInstanceState.getStringArray(STATE_DIRS);
 			String[] savedUris = savedInstanceState.getStringArray(STATE_PHOTOS);
 			if (savedDirections != null) {
 				directions.addAll(Arrays.asList(savedDirections));
 			}
 			if (savedUris != null) {
 				photoUris.addAll(Arrays.asList(savedUris));
 			}
 		} else {
 			// There's no saved state, so we can load a recipe from the database
 			// if needed, or start a new one.
 			if (rid > 0) {
 				// Load an existing recipe for editing
 				RecipeBook app = (RecipeBook) activity.getApplication();
 				Cursor c = app.getData().getRecipeDirections(rid);
 
 				c.moveToFirst();
 				while (!c.isAfterLast()) {
 					String value = c.getString(c.getColumnIndex(RecipeData.DT_STEP));
 					directions.add(value);
 
 					String photo = c.getString(c.getColumnIndex(RecipeData.DT_PHOTO));
 					photoUris.add(photo);
 					c.moveToNext();
 				}
 				c.close();
 			} else {
 				// Start a new recipe (this requires no extra setup)
 			}
 		}
 
 		adapter = new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, directions);
 		return inflater.inflate(R.layout.directionsedit, container, false);
 	}
 	
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 		MenuInflater inflater = getActivity().getMenuInflater();
 		inflater.inflate(R.menu.directionscontext, menu);
 		
 		// Don't display the "Remove Photo" option if there's no photo to remove.
 		// On the other hand, if there's already a photo, don't try to add another.
 		// TODO Maybe allow a "Change Photo" option
 		if (photoUris.get(((AdapterContextMenuInfo) menuInfo).position) == null) {
 			menu.removeItem(R.id.ctxremovephotodirection);
 		} else {
 			menu.removeItem(R.id.ctxphotodirection);
 		}
 	}
 	
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
 		String selected = adapter.getItem(info.position);
 		String selectedPhoto = photoUris.get(info.position);
 		
 		switch (item.getItemId()) {
 		case R.id.ctxdeletedirection:
 			// "Delete" option
 			adapter.remove(selected);
 			photoUris.remove(info.position);
 			currentDirection = -1;
			currentPhoto = null;
 			return true;
 		case R.id.ctxeditdirection:
 			// "Edit" option
 			currentDirection = info.position;
			currentPhoto = photoUris.get(info.position);
 			
 			// Set the editor box to have the old text
 			EditText edit = ((EditText) getView().findViewById(R.id.directionsedit));
 			edit.setText(selected);
 			edit.requestFocus();
 			adapter.remove(selected);
 			photoUris.remove(info.position);
 			
 			// Put a placeholder into the list
 			// TODO Remove the placeholder if saved during editing
 			adapter.insert(getResources().getString(R.string.recipereplacetext), currentDirection);
 			return true;
 		case R.id.ctxmovedowndirection:
 			// "Move Down" option
 			currentDirection = -1;
			currentPhoto = null;
 			if (info.position < adapter.getCount() - 1) {
 				// We can't move the last direction down
 				adapter.remove(selected);
 				adapter.insert(selected, info.position + 1);
 				
 				photoUris.remove(info.position);
 				photoUris.add(info.position + 1, selectedPhoto);
 				return true;
 			}
 			return false;
 		case R.id.ctxmoveupdirection:
 			// "Move Up" option
 			currentDirection = -1;
			currentPhoto = null;
 			if (info.position > 0) {
 				// We can't move the first direction up
 				adapter.remove(selected);
 				adapter.insert(selected, info.position - 1);
 				
 				photoUris.remove(info.position);
 				photoUris.add(info.position - 1, selectedPhoto);
 				return true;
 			}
 			return false;
 		case R.id.ctxphotodirection:
 			// "Attach Photo" option
 			attachPhoto(info.position);
 			return true;
 		case R.id.ctxremovephotodirection:
 			// "Remove Photo" option
 			photoUris.set(info.position, null);
 			return true;
 		default:
 			return super.onContextItemSelected(item);
 		}
 	}
 	
 	@Override
 	public void onStart() {
 		super.onStart();
 
 		listview = (ListView) activity.findViewById(R.id.directionslist);
 		listview.setAdapter(adapter);
 
 		// Set up the "+" button
 		ImageButton add = (ImageButton) getActivity().findViewById(R.id.adddirection);
 		add.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				EditText edit = (EditText) getView().findViewById(R.id.directionsedit);
 				
 				if (edit.getText().length() > 0) {
 					// As long as there's *something* in the edit box
 					if (currentDirection == -1) {
 						// If we're inserting a new direction...
 						adapter.add(edit.getText().toString());
 						photoUris.add(null);
 					} else {
 						// If we're editing a direction that was already there
 						adapter.insert(edit.getText().toString(), currentDirection);
						photoUris.add(currentDirection, currentPhoto);
 						adapter.remove(getResources().getString(R.string.recipereplacetext));
 						currentDirection = -1;
 					}
 				}
 				edit.setText("");
 				edit.requestFocus();
 			}
 		});
 		
 		listview.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view,
 					int position, long id) {
 				if (photoUris.get(position) != null) {
 					// Show a bigger photo if the direction has one.
 					// This is just a smaller version of the "recipe photo" handler.
 					FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
 					PhotoDialog pd = PhotoDialog.newInstance(photoUris.get(position));
 					pd.show(ft, "dialog");
 				}
 			}
 		});
 		registerForContextMenu(listview);
 	}
 
 	public String[] getDirections() {
 		// Directions are intended to be in a set order, and they are stored in
 		// the database with sequence numbers. In code, we just use the index in
 		// an array.
 		String[] dirs = new String[adapter.getCount()];
 		int l = dirs.length;
 		for (int i = 0; i < l; i++) {
 			if (!adapter.getItem(i).contentEquals(
 					getResources().getString(R.string.recipereplacetext))) {
 				dirs[i] = adapter.getItem(i);
 			}
 		}
 		return dirs;
 	}
 	
 	public String[] getPhotos() {
 		// Photos are stored with each direction, but are handled separately.
 		String[] photos = new String[photoUris.size()];
 		for (int i = 0; i < photos.length; i++) {
 			photos[i] = photoUris.get(i);
 		}
 		return photos;
 	}
 	
 	private void attachPhoto(int position) {
 		activity.onAttachDirectionPhoto(position);
 	}
 	
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (resultCode == Activity.RESULT_OK) {
 			// If the user chose a picture, we can attach it to the direction,
 			// and it will appear next to it in the recipe viewer.
 			String selectedImageUri = data.getData().toString();
 			photoUris.set(requestCode, selectedImageUri);
 		}
 	}
 	
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		// Save the directions and photos arrays, so that we can refill the
 		// lists after a pause/restore.
 		super.onSaveInstanceState(outState);
 		outState.putStringArray(STATE_DIRS, getDirections());
 		outState.putStringArray(STATE_PHOTOS, getPhotos());
 	}
 }
