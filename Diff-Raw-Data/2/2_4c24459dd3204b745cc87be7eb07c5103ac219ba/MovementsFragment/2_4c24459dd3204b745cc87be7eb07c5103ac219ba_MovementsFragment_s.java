 package com.cachirulop.moneybox.fragment;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ListView;
 
 import com.cachirulop.moneybox.R;
 import com.cachirulop.moneybox.activity.IMoneyboxListener;
 import com.cachirulop.moneybox.activity.MainActivity;
 import com.cachirulop.moneybox.activity.MovementDetailActivity;
 import com.cachirulop.moneybox.adapter.MoneyboxMovementAdapter;
 import com.cachirulop.moneybox.entity.Movement;
 import com.cachirulop.moneybox.manager.MovementsManager;
 
 public class MovementsFragment extends Fragment {
 	/** Constant to identify the click in the list */
 	static final int EDIT_MOVEMENT_REQUEST = 0;
 
 	/**
 	 * Constant to identify the context menu option to get money from the
 	 * moneybox.
 	 */
 	static final int CONTEXT_MENU_GET = 0;
 
 	/** Constant to identify the context menu option to delete a movement */
 	static final int CONTEXT_MENU_DELETE = 1;
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		return inflater.inflate(R.layout.movements_tab, container, false);
 	}
 
 	/**
 	 * Creates the ListView object to contains the movements information and
 	 * register the context menu.
 	 */
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		ListView listView;
 		MainActivity parent;
 		
 		parent = (MainActivity) getActivity();
 
 		listView = (ListView) parent.findViewById(R.id.lvMovements);
 		listView.setAdapter(new MoneyboxMovementAdapter(parent, parent.getCurrentMoneybox()));
 		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 			public void onItemClick(AdapterView<?> a, View v, int position,
 					long id) {
 				onMovementClick(a, v, position, id);
 			}
 		});
 
 		registerForContextMenu(listView);
 		
 		super.onActivityCreated(savedInstanceState);
 	}
 
 	/**
 	 * Called when returns of the edit detail window. Refresh the movement list
 	 * to show the possible changes do it in the edit detail window.
 	 */
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 
 		switch (requestCode) {
 		case EDIT_MOVEMENT_REQUEST:
 			if (resultCode == Activity.RESULT_OK) {
 				refresh();
 			}
 			break;
 		}
 	}
 
 	/**
 	 * Refresh the movements list.
 	 */
 	@Override
 	public void onResume() {
 		super.onResume();
 
 		refresh();
 	}
 
 	/**
 	 * Called when a movement in the list is clicked.
 	 * 
 	 * Show a window to edit the movement (MovementDetailActivity).
 	 * 
 	 * @param a
 	 *            Adapter of the view that launch the event.
 	 * @param v
 	 *            View that launch the event.
 	 * @param position
 	 *            Position of the item clicked
 	 * @param id
 	 *            Identifier of the clicked item.
 	 */
 	protected void onMovementClick(AdapterView<?> a, View v, int position,
 			long id) {
 		Intent i;
 		Movement m;
 
 		m = ((Movement) a.getAdapter().getItem(position));
 		i = new Intent(getActivity(), MovementDetailActivity.class);
 		i.putExtra("movement", m);
 
 		startActivityForResult(i, EDIT_MOVEMENT_REQUEST);
 	}
 
 	/**
 	 * Refresh the list of movements.
 	 * Call to the adapter of the ListView with the movements to refresh the
 	 * information.
 	 */
 	public void refresh() {
 		ListView listView;
 		MoneyboxMovementAdapter adapter;
 		MainActivity parent;
 		
 		parent = (MainActivity) getActivity();
 		
 		listView = (ListView) getActivity().findViewById(R.id.lvMovements);
 		
 		adapter = (MoneyboxMovementAdapter) listView.getAdapter();
 		adapter.setMoneybox(parent.getCurrentMoneybox());
 		adapter.refreshMovements();
 		
 		updateTotal();
 	}
 
 	/**
 	 * Update the total amount calling the main tab activity.
 	 */
 	private void updateTotal() {
 		((IMoneyboxListener) getActivity()).onUpdateTotal();
 	}
 
 	/**
 	 * Creates a context menu for the list of movements, showing an option for
 	 * delete the movement and another to get the movement.
 	 */
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 
 		if (v.getId() == R.id.lvMovements) {
 			AdapterView.AdapterContextMenuInfo info;
 			ListView listView;
 			Movement selected;
 
 			listView = (ListView) getActivity().findViewById(R.id.lvMovements);
 			info = (AdapterView.AdapterContextMenuInfo) menuInfo;
 
 			selected = (Movement) ((MoneyboxMovementAdapter) listView
 					.getAdapter()).getItem(info.position);
 
 			MenuItem item;
 
 			menu.setHeaderTitle(selected.getInsertDateFormatted());
 			item = menu.add(Menu.NONE, CONTEXT_MENU_GET, 0,
 					R.string.get_from_moneybox);
 			if (selected.getGetDate() != null) {
 				item.setEnabled(false);
 			}
 			item = menu.add(Menu.NONE, CONTEXT_MENU_DELETE, 1, R.string.delete);
 		}
 	}
 
 	/**
 	 * Handles the selected item on the context menu. Could be an option to
 	 * delete the item or to get it from the moneybox.
 	 */
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		if (item.getItemId() == CONTEXT_MENU_GET
 				|| item.getItemId() == CONTEXT_MENU_DELETE) {
 
 			AdapterView.AdapterContextMenuInfo info;
 			ListView listView;
 			Movement selected;
 
 			listView = (ListView) getActivity().findViewById(R.id.lvMovements);
 			info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
 
 			selected = (Movement) ((MoneyboxMovementAdapter) listView
 					.getAdapter()).getItem(info.position);
 
 			if (item.getItemId() == CONTEXT_MENU_GET) {
 				MovementsManager.getMovement(selected);
 			} else if (item.getItemId() == CONTEXT_MENU_DELETE) {
 				MovementsManager.deleteMovement(selected);
 			}
 
 			refresh();
 		}
 
 		return true;
 	}
 	
 	/**
 	 * Don't close the application, select the default tab.
 	 * 
 	 * It doesn't work!!
 	 */
 	public void onBackPressed() {
 	    // TODO: It doesn't work
 		((IMoneyboxListener) getActivity()).onSelectDefaultTab();
 	}
 }
