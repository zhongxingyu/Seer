 package org.everyuse.android.adapter;
 
 import java.util.ArrayList;
 
 import org.everyuse.android.util.SearchSuggestionProvider;
 
 import android.content.Context;
import android.util.Log;
 import android.widget.ArrayAdapter;
 import android.widget.Filter;
 import android.widget.Filterable;
 
 public class QuerySuggestionAdapter extends ArrayAdapter<String> implements Filterable {
	private final String TAG = getClass().getSimpleName();

 	private ArrayList<String> resultList;
 	private String target_attr = "";
 
 	public static final String ATTR_ITEM = "item";
 	public static final String ATTR_PURPOSE = "purpose";
 
 	public QuerySuggestionAdapter(Context context, int textViewResourceId) {
 		super(context, textViewResourceId);
 	}
 
 	public QuerySuggestionAdapter(Context context, int textViewResourceId, String target_attr) {
 		super(context, textViewResourceId);
 
 		this.target_attr = target_attr;
 	}
 
 	@Override
 	public int getCount() {
 		if (resultList == null) {
 			return 0;
 		} else {
 			return resultList.size();
 		}
 	}
 
 	@Override
 	public String getItem(int index) {
 		if (resultList == null) {
 			return "";
 		} else {
 			return resultList.get(index);
 		}
 	}
 
 	@Override
 	public Filter getFilter() {
 		Filter filter = new Filter() {
 			@Override
 			protected FilterResults performFiltering(CharSequence constraint) {
 				FilterResults filterResults = new FilterResults();
 				if (constraint != null) {
					Log.d(TAG, "Retrieving Query Suggestion for " + constraint.toString() + ", " + target_attr);

 					// Retrieve the autocomplete results.
 					resultList = SearchSuggestionProvider.autocomplete(constraint.toString(), target_attr);
 
 					// Assign the data to the FilterResults
 					filterResults.values = resultList;
 					filterResults.count = resultList.size();
 				}
 				return filterResults;
 			}
 
 			@Override
 			protected void publishResults(CharSequence constraint, FilterResults results) {
 				if (results != null && results.count > 0) {
 					notifyDataSetChanged();
 				} else {
 					notifyDataSetInvalidated();
 				}
 			}
 		};
 		return filter;
 	}
 }
