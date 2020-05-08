 package wei.mark.pingpongboss.misc.adapter;
 
 import java.util.ArrayList;
 
 import wei.mark.pingpongboss.R;
 import android.app.ListActivity;
 import android.content.Context;
 import android.support.v4.app.ListFragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.CheckedTextView;
 import android.widget.Filter;
 
 public class StringAdapter extends ArrayAdapter<String> {
 	ViewHolder holder;
 	ArrayList<String> strings;
 	ArrayList<String> filteredStrings;
 	ListFragment listFragment;
 	ListActivity listActivity;
 
 	public StringAdapter(Context context, int textViewResourceId,
 			ArrayList<String> objects, ListFragment fragment) {
 		super(context, textViewResourceId, objects);
 		strings = objects;
 		filteredStrings = strings;
 		listFragment = fragment;
 	}
 
 	public StringAdapter(Context context, int textViewResourceId,
 			ArrayList<String> objects, ListActivity activity) {
 		super(context, textViewResourceId, objects);
 		strings = objects;
 		filteredStrings = strings;
 		listActivity = activity;
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		// get references to all the Views
 		if (convertView == null) {
 			LayoutInflater inflater = (LayoutInflater) getContext()
 					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
 			convertView = inflater.inflate(R.layout.item_player_search, parent,
 					false);
 
 			holder = new ViewHolder();
 			holder.name = (CheckedTextView) convertView.findViewById(R.id.name);
 
 			convertView.setTag(holder);
 		} else {
 			holder = (ViewHolder) convertView.getTag();
 		}
 
 		// fill in the Views
 		String s = filteredStrings.get(position);
 		if (s != null) {
 			holder.name.setText(s);
 			if (listFragment != null)
 				holder.name.setChecked(listFragment.getListView()
 						.getCheckedItemPosition() == position);
 			else if (listActivity != null)
 				holder.name.setChecked(listActivity.getListView()
 						.getCheckedItemPosition() == position);
 		}
 		return convertView;
 	}
 
 	@Override
 	public int getCount() {
 		return filteredStrings.size();
 	}
 
 	@Override
 	public int getViewTypeCount() {
 		return 1;
 	}
 
 	@Override
 	public Filter getFilter() {
 		return new Filter() {
 
 			@SuppressWarnings("unchecked")
 			@Override
 			protected void publishResults(CharSequence constraint,
 					FilterResults results) {
 				filteredStrings = (ArrayList<String>) results.values;
 				notifyDataSetChanged();
 			}
 
 			@Override
 			protected FilterResults performFiltering(CharSequence constraint) {
 				constraint = constraint.toString().toLowerCase();
 
 				ArrayList<String> list = new ArrayList<String>();
 				for (String string : strings) {
 					if (string.toLowerCase().contains(constraint))
 						list.add(string);
 				}
 
 				FilterResults results = new FilterResults();
 				results.values = list;
 				results.count = list.size();
 				return results;
 			}
 		};
 	}
 
 	public String getString(int position) {
 		return filteredStrings.get(position);
 	}
 
 	static class ViewHolder {
 		CheckedTextView name;
 
 		String string;
 	}
 }
