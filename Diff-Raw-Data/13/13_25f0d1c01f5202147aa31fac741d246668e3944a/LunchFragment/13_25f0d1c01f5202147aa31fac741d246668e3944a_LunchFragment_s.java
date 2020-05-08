 package csci498.csmyth.lunchlist;
 
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.support.v4.app.ListFragment;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.database.Cursor;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.CursorAdapter;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.RadioGroup;
 import android.widget.TextView;
 
 public class LunchFragment extends ListFragment {
 	public final static String ID_EXTRA = "csci498.csmyth.lunchlist._ID";
 	
 	Cursor model = null;
 	RestaurantHelper helper = null;
 	RestaurantAdapter adapter = null;
 	SharedPreferences prefs = null;
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setHasOptionsMenu(true);
 	}
      
 	@Override
 	public void onResume() {
 		super.onResume();
        
         prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
         helper = new RestaurantHelper(getActivity());
         
         initList();
         prefs.registerOnSharedPreferenceChangeListener(prefListener);
     }
 	
 	@Override
 	public void onPause() {
 		helper.close();
 		super.onPause();
 	}
 	
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		inflater.inflate(R.menu.option, menu);
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if (item.getItemId() == R.id.add) {
 			startActivity(new Intent(getActivity(), DetailForm.class));	
 			return true;
 		} else if (item.getItemId() == R.id.prefs) {	
 			startActivity(new Intent(getActivity(), EditPreferences.class));
 			return true;
 		}
		
 		return super.onOptionsItemSelected(item);
 	}
 	
 	@Override
 	public void onListItemClick(ListView list, View view, int position, long id) {
 		Intent intent = new Intent(getActivity(), DetailForm.class);
 		
 		intent.putExtra(ID_EXTRA, String.valueOf(id));
 		startActivity(intent);
 	}
 	
 	private void initList() {
 		if (model != null) {
 			model.close();
 		}
 		model = helper.getAll(prefs.getString("sort_order", "name"));
         adapter = new RestaurantAdapter(model);
         setListAdapter(adapter);
 	}
 	
 	private SharedPreferences.OnSharedPreferenceChangeListener prefListener = 
 			new SharedPreferences.OnSharedPreferenceChangeListener() {
 		public void onSharedPreferenceChanged(SharedPreferences sharedPrefs, String key) {
 			if (key.equals("sort_order")) {
 				initList();
 			}
 		}
 	};
 	
 	class RestaurantAdapter extends CursorAdapter {
 		RestaurantAdapter(Cursor c) {
 			super(getActivity(), c);
 		}
 		
 		@Override
 		public void bindView(View row, Context ctxt, Cursor c) {
 			RestaurantHolder holder = (RestaurantHolder)row.getTag();
			
 			holder.populateFrom(c, helper);
 		}
 		
 		@Override
 		public View newView(Context ctxt, Cursor c, ViewGroup parent) {
			LayoutInflater inflater = getLayoutInflater();
 			View row = inflater.inflate(R.layout.row, parent, false);
 			RestaurantHolder holder = new RestaurantHolder(row);
			
 			row.setTag(holder);
 		
			return (row);
 		}
 	}
 	
 	static class RestaurantHolder {
 		private TextView name = null;
 		private TextView address = null;
 		private ImageView icon = null;
 		
 		RestaurantHolder(View row) {
 			name = (TextView)row.findViewById(R.id.title);
 			address = (TextView)row.findViewById(R.id.address);
 			icon = (ImageView)row.findViewById(R.id.icon);
 		}
 		
 		void populateFrom(Cursor c, RestaurantHelper helper) {
 			name.setText(helper.getName(c));
 			address.setText(helper.getAddress(c));
 			
 			if (helper.getType(c).equals("@string/sit_down")) {
 				icon.setImageResource(R.drawable.ball_red);
 			} else if (helper.getType(c).equals("@string/take_out")) {
 				icon.setImageResource(R.drawable.ball_yellow);
 			} else {
 				icon.setImageResource(R.drawable.ball_green);
 			}
 		}	
 	}
 
 }
