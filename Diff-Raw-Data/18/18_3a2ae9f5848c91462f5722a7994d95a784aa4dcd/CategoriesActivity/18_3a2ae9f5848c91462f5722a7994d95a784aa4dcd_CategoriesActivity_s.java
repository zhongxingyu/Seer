 package kwik.app.activities;
 
 import java.util.HashMap;
 import java.util.List;
 
 import kwik.Util;
 import kwik.app.R;
 import kwik.app.activities.custom.KwikFragmentActivity;
 import kwik.remote.api.Category;
 import kwik.remote.api.SubCategory;
 import kwik.services.KwikAPIService;
 import kwik.services.KwikNotificationService;
 import android.app.AlertDialog.Builder;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.ResultReceiver;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 
 public class CategoriesActivity extends KwikFragmentActivity implements OnItemClickListener, OnItemLongClickListener {
 	
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
		
 		/* Asociamos la vista del search list con la activity */
 		this.setContentView(R.layout.item_list);
 		
 		Intent NotifIntent = new Intent(Intent.ACTION_SYNC, null, this, KwikNotificationService.class);
 		NotifIntent.putExtra("token", "d69bf21da285f8634533b9f7cc487ed"); // maximovs
 																			// -
 																			// maximovs
 		NotifIntent.putExtra("command", KwikNotificationService.NOTIFY_ORDERS_CMD);
 		
 		Intent intent = new Intent(Intent.ACTION_SYNC, null, this, KwikAPIService.class);
 		final boolean subcategory_activity;
 		
 		if (this.getIntent() != null && this.getIntent().getExtras() != null) {
 			intent.putExtra("category_id", this.getIntent().getExtras().getInt("category_id"));
 			intent.putExtra("command", KwikAPIService.GET_SUBCATEGORIES_CMD);
 			this.setTitle(this.getIntent().getExtras().getInt("category_name"));
 			subcategory_activity = true;
 		} else {
 			subcategory_activity = false;
 			intent.putExtra("command", KwikAPIService.GET_CATEGORIES_CMD);
 		}
 		
 		/*
 		 * Se pasa un callback (ResultReceiver), con el cual se procesará la
 		 * respuesta del servicio. Si se le pasa null como parámetro del
 		 * constructor se usa uno de los threads disponibles del proceso. Dado
 		 * que en el procesamiento del mismo se debe modificar la UI, es
 		 * necesario que ejecute con el thread de UI. Es por eso que se lo
 		 * instancia con un objeto Handler (usando el el thread de UI para
 		 * ejecutarlo).
 		 */
 		intent.putExtra("receiver", new ResultReceiver(new Handler()) {
 			@Override
 			protected void onReceiveResult(int resultCode, Bundle resultData) {
 				super.onReceiveResult(resultCode, resultData);
 				if (resultCode == KwikAPIService.STATUS_OK) {
 					
 					if (!subcategory_activity) {
 						@SuppressWarnings("unchecked")
 						List<Category> catList = (List<Category>) resultData.getSerializable("return");
 						populateCatList(catList);
 					} else {
 						@SuppressWarnings("unchecked")
 						List<SubCategory> catList = (List<SubCategory>) resultData.getSerializable("return");
 						populateSubCatList(catList);
 					}
 					
 				} else if (resultCode == KwikAPIService.STATUS_CONNECTION_ERROR) {
 					Log.d(TAG, "Connection error.");
				} else {
 					Log.d(TAG, "Unknown error.");
 				}
 			}
 			
 		});
 		
 		ListView vi = (ListView) findViewById(R.id.listview);
 		
 		vi.setOnItemClickListener(this);
 		vi.setOnItemLongClickListener(this);
 		
 		this.startService(intent);
 		
 		startService(NotifIntent);
 		startService(intent);
 		
 	}
 	
 	private void populateSubCatList(List<SubCategory> categories) {
 		String[] map_fields = { "name", "id", "category_id" };
 		String[] desired_fields = { "name" };
 		
 		ListAdapter adapter = new SimpleAdapter(this, Util.getMapped(categories, map_fields), R.layout.item_list_item,
 				desired_fields, new int[] { R.id.title });
 		
 		ListView vi = (ListView) findViewById(R.id.listview);
 		
 		vi.setAdapter(adapter);
 	}
 	
 	private void populateCatList(List<Category> categories) {
 		String[] map_fields = { "name", "id" };
 		String[] desired_fields = { "name" };
 		
 		ListAdapter adapter = new SimpleAdapter(this, Util.getMapped(categories, map_fields), R.layout.item_list_item,
 				desired_fields, new int[] { R.id.title });
 		ListView vi = (ListView) findViewById(R.id.listview);
 		vi.setAdapter(adapter);
 	}
 	
 	@Override
 	public void onItemClick(AdapterView<?> view, View v, int position, long arg3) {
 		ListView vi = (ListView) view;
 		@SuppressWarnings("unchecked")
 		HashMap<String, Object> map = (HashMap<String, Object>) vi.getItemAtPosition(position);
 		
 		Integer id = (Integer) map.get("id");
 		String category_name = (String) map.get("name");
 		Integer category_id = (Integer) map.get("category_id");
 		
 		if (category_id != null) {
 			Intent intent = new Intent(v.getContext(), ProductsActivity.class);
 			intent.putExtra("subcategory_id", id);
 			intent.putExtra("category_id", category_id);
 			intent.putExtra("category_name", category_name);
 			startActivity(intent);
 		} else {
 			Intent intent = new Intent(v.getContext(), CategoriesActivity.class);
 			intent.putExtra("category_id", id);
 			intent.putExtra("category_name", category_name);
 			startActivity(intent);
 		}
 		
 	}
 	
 	@Override
 	public boolean onItemLongClick(final AdapterView<?> view, final View v, final int position, long arg3) {
 		ListView vi = (ListView) view;
 		@SuppressWarnings("unchecked")
 		HashMap<String, Object> map = (HashMap<String, Object>) vi.getItemAtPosition(position);
 		
 		final Integer id = (Integer) map.get("id");
 		final String category_name = (String) map.get("name");
 		final Integer category_id = (Integer) map.get("category_id");
 		
 		if (category_id != null) {
 			return true;
 		} else {
 			Builder b = new Builder(v.getContext());
 			b.setTitle(getResources().getString(R.string.Category) + " " + map.get("name"));
 			String[] options = { getResources().getString(R.string.show_products) };
 			
 			b.setItems(options, new OnClickListener() {
 				
 				@Override
 				public void onClick(DialogInterface arg0, int arg1) {
 					if (arg1 == 0) {
 						Intent intent = new Intent(v.getContext(), ProductsActivity.class);
 						intent.putExtra("category_id", id);
 						intent.putExtra("category_name", category_name);
 						startActivity(intent);
 					}
 				}
 			});
 			
 			b.create().show();
 			return false;
 		}
 		
 	}
 	
 }
