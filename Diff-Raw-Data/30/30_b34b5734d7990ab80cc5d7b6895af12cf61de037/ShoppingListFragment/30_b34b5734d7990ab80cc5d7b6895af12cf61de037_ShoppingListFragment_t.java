 package com.coffeestrike.bettershoppinglist;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import org.json.JSONException;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.ListFragment;
 import android.util.Log;
 import android.view.ActionMode;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AbsListView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.ListView;
 import android.widget.TextView;
 
 
 public class ShoppingListFragment extends ListFragment {
 	
 	public static String TAG = "ShoppingListFragment";
 	private ShoppingList mItemList;
	public static final int NEW_ITEM = 0;
	public static final int EDIT_ITEM = 1;
 	
 
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
 		super.onCreateOptionsMenu(menu, inflater);
 		inflater.inflate(R.menu.main, menu);
 		Log.d(TAG, "options menu inflated");
 	}
 	
 
 	@Override
 	public void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);
 		setHasOptionsMenu(true);
 		getActivity().setTitle(R.string.app_name);
 		mItemList = ShoppingList.get(getActivity());
 		setListAdapter(new ShoppingListAdapter(mItemList));
 		setRetainInstance(true);
 	}
 	
 	@Override 
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 	        Bundle savedInstanceState){
 
 		View v = inflater.inflate(R.layout.shopping_list, null);
 		
 		ListView listView = (ListView)v.findViewById(android.R.id.list);
 		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
 		listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
 			
 			@Override
 			public boolean onPrepareActionMode(ActionMode arg0, Menu arg1) {
 				return false;
 			}
 			
 			@Override
 			public void onDestroyActionMode(ActionMode arg0) {}
 			
 			@Override
 			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
 				MenuInflater inflater = mode.getMenuInflater();
 				inflater.inflate(R.menu.context_action_bar, menu);
 				return true;
 			}
 			
 			@Override
 			public boolean onActionItemClicked(ActionMode mode, MenuItem menuItem) {
 				switch(menuItem.getItemId()){
 					case R.id.menu_item_delete:
 						ShoppingListAdapter adapter = (ShoppingListAdapter)getListAdapter();
 						for(int position = adapter.getCount(); position >= 0; position--){
 							if(getListView().isItemChecked(position)){
 								mItemList.remove(position);
 							}
 						}
 						mode.finish();
 						adapter.notifyDataSetChanged();
 						return true;
 					default:
 						return false;
 				}
 
 			}
 			
 			@Override
 			public void onItemCheckedStateChanged(ActionMode arg0, int arg1, long arg2,
 					boolean arg3) {
 			}
 		});
 		
 		Button b = (Button)v.findViewById(R.id.addItem_button);
 		b.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				newListItem();
 			}
 		});
 
 		return v;
 	
 	}
 	
 	
 	
 	@Override
 	public void onPause() {
 		super.onPause();
 		try {
 			new ShoppingListJSONSerializer("shoppinglist.json", getActivity()).saveList(mItemList);
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item){
 		switch(item.getItemId()){
 			case R.id.add_button:
 				newListItem();
 				return true;
 			case R.id.sort_alpha:
 				mItemList.sortAlpha();
 				((ShoppingListAdapter)getListAdapter()).notifyDataSetChanged();
 				return true;
 			case R.id.clear_all:
 				mItemList.clear();
 				((ShoppingListAdapter)getListAdapter()).notifyDataSetChanged();
 				return true;
 		}
 		
 		return false;
 	}
 	
 	private void newListItem(){
 		FragmentManager fm = getActivity().getSupportFragmentManager();
 		EditItemDialog newItem = EditItemDialog.newInstance(new Item());
 		newItem.setTargetFragment(this, NEW_ITEM);
 		newItem.show(fm, TAG);
 	}
 	
 	@Override
 	public void onActivityResult (int requestCode, int resultCode, Intent data){
 		Log.d(TAG, "onActivityResult");
 		if(resultCode != Activity.RESULT_OK){	
 			return;
 		}
 		if(requestCode == NEW_ITEM){
 			mItemList.add( (Item) data.getSerializableExtra(EditItemDialog.EXTRA_ITEM) );
 			((ShoppingListAdapter)getListAdapter()).notifyDataSetChanged();
 		}
 		else if(requestCode == EDIT_ITEM){
 			// Nothing needs to be done here.
 			// The dialog fragment handles editing
 		}
 
 	}
 	
 
 	@Override
 	public void onListItemClick(ListView l, View v, int position, long id) {
 		Log.d(TAG, "list item "+ position + " clicked.");
 		super.onListItemClick(l, v, position, id);
 		Item item = (Item) l.getItemAtPosition(position);
 		editListItem(item, position);
 		
 	}
 	
 
 	private void editListItem(Item item, int position) {
 		FragmentManager fm = getActivity().getSupportFragmentManager();
 		EditItemDialog editItem = EditItemDialog.editInstance(item, position);
 		editItem.setTargetFragment(this, EDIT_ITEM);
 		editItem.show(fm, TAG);
 		
 	}
 
 
 
 	private class ShoppingListAdapter extends ArrayAdapter<Item> {
 
 		public ShoppingListAdapter(ArrayList<Item> list){
 			super(getActivity(), 0, list);
 		}
 		
 		@Override
 		public View getView (final int position, View convertView, ViewGroup parent){
 			if(convertView == null){
 				convertView = getActivity().getLayoutInflater().inflate(R.layout.item, null);
 			}
 			Item i = getItem(position);
 			CheckBox checkbox = (CheckBox) convertView.findViewById(R.id.item_checkBox);
 			checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 				
 				@Override
 				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 					if(isChecked){
 						getItem(position).setStatus(1);
 					
 					}
 					else{
 						getItem(position).setStatus(0);
 					}
 					
 				}
 			});
 			TextView itemText = (TextView) convertView.findViewById(R.id.item_text);
 			TextView itemQty = (TextView) convertView.findViewById(R.id.item_qty);
 			checkbox.setChecked(i.getStatus() == 1);
 			itemText.setText(i.getDescription());
 			itemQty.setText(String.valueOf(i.getQty()));
 
 			return convertView;
 		}
 		
 		
 	}
 	
 
 	
 
 }
