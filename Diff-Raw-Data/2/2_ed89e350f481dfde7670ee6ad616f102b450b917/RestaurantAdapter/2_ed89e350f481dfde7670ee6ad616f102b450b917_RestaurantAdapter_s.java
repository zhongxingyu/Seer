 package apt.tutorial;
 
 import android.app.Activity;
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import apt.tutorial.LunchListActivity.RestaurantHolder;
 
 public class RestaurantAdapter extends ArrayAdapter<Restaurant> {
 	private Context mContext;
 	
 	public RestaurantAdapter(Context context) {
 		super(context, android.R.layout.simple_list_item_1);
 		mContext = context;
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		View row = convertView;
		RestaurantHolder resturant = getItem(position);
 		
 		if (row == null) {
 			LayoutInflater inflater = ((Activity) getContext()).getLayoutInflater();
 			row = inflater.inflate(R.layout.row, null);
 		
 			holder = new RestaurantHolder(row);
 			row.setTag(holder);
 		} else {
 			holder = (RestaurantHolder) row.getTag();
 		}
 
 		holder.populateFrom((Restaurant) getItem(position), row, mContext);
 
 		return row;
 	}
 
 }
