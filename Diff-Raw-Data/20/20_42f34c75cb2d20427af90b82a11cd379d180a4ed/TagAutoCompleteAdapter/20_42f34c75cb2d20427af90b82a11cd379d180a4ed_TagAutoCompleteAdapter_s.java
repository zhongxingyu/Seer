 package org.droidstack.adapter;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.sf.stackwrap4j.StackWrapper;
 import net.sf.stackwrap4j.entities.Tag;
 import net.sf.stackwrap4j.query.TagQuery;
 
 import org.droidstack.R;
 import org.droidstack.util.Const;
 
 import android.content.Context;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.Filter;
 import android.widget.Filterable;
 import android.widget.TextView;
 
 public class TagAutoCompleteAdapter extends BaseAdapter implements Filterable {
 	
 	private class TagFilter extends Filter {
		
		private boolean isRequestOngoing = false;
		
 		@Override
 		protected FilterResults performFiltering(CharSequence constraint) {
			if (isRequestOngoing || constraint == null) return null;
 			try {
 				FilterResults results = new FilterResults();
 				TagQuery query = new TagQuery();
 				query.setFilter(constraint.toString());
 				List<Tag> tags = mAPI.listTags(query);
 				results.count = tags.size();
 				results.values = tags;
 				return results;
 			}
 			catch (Exception e) {
 				Log.e(Const.TAG, "TagFilter.performFiltering error while fetching", e);
 				return null;
 			}
 		}
 
 		@Override
 		protected void publishResults(CharSequence constraint,
 				FilterResults results) {
 			if (results == null) return;
 			tags.clear();
 			for (Tag t: (ArrayList<Tag>) results.values) {
 				tags.add(t.getName());
 			}
 			notifyDataSetChanged();
 		}
		
 	}
 	
 	private final ArrayList<String> tags;
 	private final TagFilter filter;
 	private final Context context;
 	private final LayoutInflater inflater;
 	
 	private StackWrapper mAPI;
 	
 	public TagAutoCompleteAdapter(Context context, String endpoint) {
 		this.context = context;
 		
 		mAPI = new StackWrapper(endpoint, Const.APIKEY);
 		tags = new ArrayList<String>();
 		filter = new TagFilter();
 		
 		inflater = LayoutInflater.from(context);
 	}
 	
 	@Override
 	public Filter getFilter() {
 		return filter;
 	}
 
 	@Override
 	public int getCount() {
 		return tags.size();
 	}
 
 	@Override
 	public Object getItem(int position) {
 		return tags.get(position);
 	}
 
 	@Override
 	public long getItemId(int position) {
 		return position;
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		if (convertView != null) {
			((TextView)convertView).setText(tags.get(position));
 			return convertView;
 		}
 		else {
 			TextView v = (TextView) inflater.inflate(R.layout.item_dropdown, null);
 			v.setText(tags.get(position));
 			return v;
 		}
 	}
 
 }
