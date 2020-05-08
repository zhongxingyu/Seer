 package cs169.project.thepantry;
 
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.ListFragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 public class NavMenuFragment extends ListFragment {
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		return inflater.inflate(R.layout.list, null);
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		String[] colors = getResources().getStringArray(R.array.nav_menu_items);
		ArrayAdapter<String> colorAdapter = new ArrayAdapter<String>(getActivity(), 
 				android.R.layout.simple_list_item_1, android.R.id.text1, colors);
		setListAdapter(colorAdapter);
 	}
 	
 	@Override
 	public void onListItemClick(ListView lv, View v, int position, long id) {
 		Context context = getActivity();
 		Intent intent = null;
 		switch (position) {
 		case 0:
 			intent = new Intent(context, HomePageActivity.class);
 			break;
 		case 1:
 			InventoryActivity invAct = new InventoryActivity();
 			intent = new Intent(context, invAct.getClass());
 			break;
 		case 2:
 			intent = new Intent(context, ShoppingListActivity.class);
 			break;
 		case 3:
 			intent = new Intent(context, LoginActivity.class);
 			break;
 		case 4:
 			intent = new Intent(context, SettingsActivity.class);
 			break;
 		}
		startActivity(intent);
 	}
 
 }
