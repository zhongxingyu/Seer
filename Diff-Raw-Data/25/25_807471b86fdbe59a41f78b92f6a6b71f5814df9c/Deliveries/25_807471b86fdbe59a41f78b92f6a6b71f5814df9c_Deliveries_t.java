 package at.roadrunner.android.activity;
 
 import java.util.ArrayList;
 
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.LayoutInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Adapter;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import at.roadrunner.android.R;
 import at.roadrunner.android.controller.DeliveryController;
 import at.roadrunner.android.model.Delivery;
 
 public class Deliveries extends ListActivity {
 	@SuppressWarnings("unused")
 	private static final String TAG = "Deliveries";
 	
 	private static final int MENU_ITEM_SHOW = 0;
 	private static final int MENU_ROUTE_SHOW = 1;
 	
 	private ProgressDialog _progressDialog = null;
 	private ArrayList<Delivery> _deliveries = null;
 	private DeliveryAdapter _adapter;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_deliveries);
 		
 		_adapter = new DeliveryAdapter(this, R.layout.row_item_deliveries);
 		setListAdapter(_adapter);
 		
 		registerForContextMenu(getListView());
 		
 		// Runnable to fill the items in a Thread
 		Runnable showDeliveries = new Runnable() {
 			@Override
 			public void run() {
 				getDeliveries();
 			}
 		};
 
 		// start a new Thread to get the items
 		new Thread(null, showDeliveries, "getDeliveriesOfCouchDB").start();
 
 		// show the Progressbar
 		_progressDialog = ProgressDialog.show(this, getString(R.string.app_progress_pleasewait),getString(R.string.app_progress_retdata), true);
 	}
 	
 
 	private void getDeliveries() {
 		_deliveries = new DeliveryController(this).getDeliveries();
 		runOnUiThread(updateActivity);
 	}
 	
 	/*
 	 * fill the adapter with the deliveries of the list
 	 */
 	private final Runnable updateActivity = new Runnable() {
 
 		@Override
 		public void run() {
 			if (_deliveries != null) {
 				_adapter.notifyDataSetChanged();
 				for (int i = 0; i < _deliveries.size(); i++) {
 					_adapter.add(_deliveries.get(i));
 				}
 			}
 
 			_adapter.notifyDataSetChanged();
 			_progressDialog.dismiss();
 		}
 	};
 	
 	/*
 	 * DeliveryAdapter
 	 */
 	private class DeliveryAdapter extends ArrayAdapter<Delivery> {
 
 		public DeliveryAdapter(Context context, int textViewResourceId) {
 			super(context, textViewResourceId);
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			View view = convertView;
 			if (view == null) {
 				LayoutInflater layInf = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 				view = layInf.inflate(R.layout.row_item_deliveries, null);
 			}
 
 			Delivery delivery = _deliveries.get(position);
 			if (delivery != null) {
 				// get Views
 				TextView txtFrom = (TextView) view.findViewById(R.id.deliveries_from);
 				TextView txtFromSub = (TextView) view.findViewById(R.id.deliveries_from_sub);
 				TextView txtTo = (TextView) view.findViewById(R.id.deliveries_to);
 				TextView txtToSub = (TextView) view.findViewById(R.id.deliveries_to_sub);
 				
 				// set values of Views
 				txtFrom.setText(delivery.getFrom().getRecipient());
 				txtFromSub.setText(delivery.getFrom().getFormatedAddress());
 				txtTo.setText(delivery.getDestination().getRecipient());
 				txtToSub.setText(delivery.getDestination().getFormatedAddress());
 			}
 
 			return view;
 		}
 	}
 
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		showItem(_deliveries.get(position));
 	}
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenuInfo menuInfo) {
 		
 		menu.add(0, MENU_ITEM_SHOW, 0, "Show Item");
 		menu.add(0, MENU_ROUTE_SHOW, 0, "Show Route");
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
 		Delivery delivery = null;
 		switch (item.getItemId()) {
 			case MENU_ITEM_SHOW:
 				 delivery = (Delivery) getListAdapter().getItem(info.position);
 				 if (delivery != null) {
 					 showItem(delivery);
 				 }
 				return true;
 			case MENU_ROUTE_SHOW:
 				Intent mapIntent = new Intent(this, DeliveryMap.class);
 				delivery = (Delivery) getListAdapter().getItem(info.position);
 				if (delivery != null) {
					mapIntent.putExtra("Delivery", delivery);
 				}
 				startActivity(mapIntent);
 				return true;
 			default:
 				return super.onContextItemSelected(item);
 		}
 	}
 	
 	private void showItem(Delivery delivery) {
 		Intent intent = new Intent(this, Items.class);
 		intent.putExtra("Delivery", delivery);
 		startActivity(intent);
 	}
 }
