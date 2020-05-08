 package coupling.app;
 
 import com.nit.coupling.R;
 import coupling.app.BL.BLFactory;
 import coupling.app.BL.BLShopListOverview;
 import coupling.app.com.IBLConnector;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
import android.widget.TextView;
 
 public class FragmentShopList extends Fragment implements IBLConnector{
 
 	private AdapterShopListOverview adapter;
 	
 	private BLShopListOverview blShopListOverview;
 
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 
 		View rootView = inflater.inflate(R.layout.fragment_main_shoplist, container, false);
 		blShopListOverview = BLFactory.getInstance().getShopListOverview();
 
 		
		
		
 		final ListView lv = (ListView) rootView.findViewById(R.id.shoplist_lv);
 		adapter = new AdapterShopListOverview(this.getActivity(), blShopListOverview);
 		
 		lv.setAdapter(adapter);
 
 		final EditText listName = (EditText) rootView.findViewById(R.id.etListName);
 
 		Button addList = (Button) rootView.findViewById(R.id.btnAddList);
 
 
 		addList.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				String title = listName.getText().toString();
 				if(!title.isEmpty()){
 					blShopListOverview.createList(title);
 					adapter.refreshList();
 					
 					lv.setSelection(lv.getCount() - 1);
 					listName.setText("");
 					final InputMethodManager im = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
 					im.hideSoftInputFromWindow(listName.getWindowToken(), 0);
 				}
 			}
 		});
 
 		lv.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View v, int position,
 					long id) {
 				
 				Cursor cursor = (Cursor) adapter.getItem(position);
 				
 				Intent i = new Intent(getActivity(), ShopList.class);
 				Bundle bundle = new Bundle();
 				
 				bundle.putString("Title", cursor.getString(cursor.getColumnIndexOrThrow("Title")));
 				bundle.putLong("Id", id);
 
 				i.putExtras(bundle);
 				startActivity(i);
 			}
 		});
 		setHasOptionsMenu(true);
 
 		return rootView;
 	}
 
 
 
 	@Override
 	public void onPause() {
 		super.onPause();
 		blShopListOverview.unsetBLConnector();
 	}
 
 
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		blShopListOverview.setBLConnector(this);
 	}
 
 
 
 	@Override
 	public void Refresh() {
 		this.getActivity().runOnUiThread(new Runnable() {
 			
 			@Override
 			public void run() {
 				adapter.refreshList();
 			}
 		});
 	}
 
 
 
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		// TODO Auto-generated method stub
 		inflater.inflate(R.menu.shop_list_overview_actionbar, menu);
 
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.invite:
 			Intent inviteIntent = new Intent(getActivity(), Invite.class);
 			startActivity(inviteIntent);
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 	
 	
 	
 }
