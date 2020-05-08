 package edu.gatech.oad.rocket.findmythings.control;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import edu.gatech.oad.rocket.findmythings.model.Item;
import edu.gatech.oad.rocket.findmythings.MainActivity;
 import edu.gatech.oad.rocket.findmythings.R;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Color;
 import android.text.Spannable;
 import android.text.SpannableString;
 import android.text.style.ForegroundColorSpan;
 import android.text.style.RelativeSizeSpan;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.Filter;
 import android.widget.Filterable;
 
 import android.widget.TextView;
 
 /**
  * CS 2340 - FindMyStuff Android App
  *
  * @author TeamRocket
  * */
 public class Adapter extends ArrayAdapter<Item> implements Filterable {
 
 	/**
 	 * Gets context from the activity using this adapter
 	 */
 	private Context mContext;
 
 	/**
 	 * Reference to the list passed into the adapter
 	 */
 	private List<Item> mList;
 	
 	/**
 	 * Instance of the text filter used to filter search results
 	 */
 	private TextFilter filter;
 
 	/**
 	 * constructor1
 	 * @param context
 	 * @param textViewResourceId
 	 * @param objects
 	 */
 	public Adapter(Context context, int textViewResourceId, List<Item> objects) {
 		super(context, textViewResourceId, objects);
 		
 		mContext = context;
 		mList = objects;
 	}
 
 	/**
 	 * constructor2
 	 * @param context
 	 * @param resource
 	 * @param textViewResourceId
 	 * @param objects
 	 */
 	public Adapter(Context context, int resource, int textViewResourceId,
 			List<Item> objects) {
 		super(context,resource,textViewResourceId, objects);
 		mContext=context;
 		mList = objects;
 		
 	}
 	
 	/**
 	 * sets the main list of the Adapter
 	 * @param List<Item> l
 	 */
 	public void setList(List<Item> l) {
 		if (!l.isEmpty()) {
 			mList = l;
 		}
 		else mList = null;
 		
 		notifyDataSetChanged();
 	}
 	
 	/**
 	 * Sets the filter for this ArrayAdapter
 	 */
 	@Override
 	public Filter getFilter() {
 	    if (filter == null)
 	        filter = new TextFilter();
 
 	    return filter;
 	}
 	
 	/**
 	 * returns the number of items on main list
 	 * @return int count
 	 */
 	@Override
 	public int getCount() {
 		if(mList==null)
 			return 0;
 	    return mList.size();
 	}
 	
 	/**
 	 * returns the current View being used
 	 * @param int position
 	 * @param View convertView
 	 * @param ViewGroup parent
 	 * @return View
 	 */
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 	 
 	 View row = convertView;
 	 if (row == null || mList == null) {
          LayoutInflater inflater = ((Activity)mContext).getLayoutInflater();
          row = inflater.inflate (R.layout.arrayadapter_view, parent, false);
 	 }
 	 if(mList == null || mList.isEmpty() || position>mList.size()) 
 		 return row;
 	
 	 Item temp = mList.get(position);
 	 
 	 Spannable span = new SpannableString(temp.toString() + " - " + temp.getLoc() + temp.getSummary());
 	 int start = temp.toString().length();
 	 int stop = temp.getLoc().length()+ temp.toString().length() +  temp.getSummary().length()+ 3;
 
 	 //Span changes the color of the text to light gray to mimic hint and summary
 	 span.setSpan(new ForegroundColorSpan(Color.LTGRAY),start,stop,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
 	 TextView frag = (TextView)row.findViewById(R.id.item_list);
 
 	 //New span spans the last span so that span doesn't span the full text size.
 	 // Actually just makes the last span into a smaller text
 	 Spannable span2 = new SpannableString(span);
 	 int start2 = start + temp.getLoc().length() + 3;
 	 span2.setSpan(new RelativeSizeSpan(0.8f),start2,stop,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
 
 	 frag.setText(span2);
 
 	 return row;
 	}
 	
 	/**
 	 * Class used for filtering items in the ArrayAdapter
 	 * @author Justin
 	 *
 	 */
 	private class TextFilter extends Filter {
 		
 		public TextFilter() {}
 
 		/**
 		 * filtering by sequence of characters
 		 * @param CharSequence constraint
 		 * @return FilterResults 
 		 */
 		@Override
 		protected FilterResults performFiltering(CharSequence constraint) {
 			FilterResults results = new FilterResults();
 			if(constraint != null && constraint.length()>0) {
 	            ArrayList<Item> list = new ArrayList<Item>();
 	            for(Item a : mList)
 	            	if(a.getAll().toLowerCase().contains(constraint.toString().toLowerCase()))
 	            		list.add(a);
 	            results.values = list;
 	            results.count = list.size();
 			}
 		return results;	
 		}
 
 		/**
 		 * @param CharSequence constraint
 		 * @param FilterResults results
 		 */
 		@SuppressWarnings("unchecked")
 		@Override
 		protected void publishResults(CharSequence constraint,
 				FilterResults results) {
 			
			MainActivity.update((ArrayList<Item>)results.values);
             notifyDataSetChanged();
 			
 		}
 		
 	}
 	
 }
