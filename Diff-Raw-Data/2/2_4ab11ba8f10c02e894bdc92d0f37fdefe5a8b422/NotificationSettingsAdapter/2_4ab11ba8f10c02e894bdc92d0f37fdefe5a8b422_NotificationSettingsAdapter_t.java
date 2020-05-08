 package com.ese2013.mensaunibe.notification;
 
 import java.util.ArrayList;
 
 import com.ese2013.mensaunibe.R;
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageButton;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * @author group17
  * @author Andreas Hohler
  */
 
 public class NotificationSettingsAdapter extends BaseAdapter {
 	private Context context;
 	private int resource;
 	private LayoutInflater inflater;
 
 	private ArrayList<String> items;
 	
 	public NotificationSettingsAdapter(Context context, int resource, ArrayList<String> keywords ) {
 		super();
 		this.context = context;
 		this.resource = resource;
 		populate( keywords );
 	}
 
 	public View getView(int position, View convertView, ViewGroup parent) {
 		View view = convertView;
 		if(view == null) {
 			ViewHolder viewHolder = new ViewHolder();
 			inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			view = inflater.inflate(this.resource, parent, false);
 			viewHolder.text = (TextView) view.findViewById(R.id.notification_list_text);
 			viewHolder.delete = (ImageButton) view.findViewById(R.id.delete_keyword);
 			view.setTag(viewHolder);
 		}
 		
 
 		ViewHolder holder = (ViewHolder) view.getTag();
 		clearHolder(holder);
 		
 		String item = items.get(position);
 		
 		holder.delete.setOnClickListener( new NotificationEntryListener(item, this) );
 		holder.text.setText( item );
 		holder.text.setVisibility(View.VISIBLE);
 		holder.delete.setVisibility(View.VISIBLE);
 		return view;
 	}
 
 	/**
 	 * Is the public method to repopulate the whole List.
 	 */
 	
 	@Override
 	public void notifyDataSetChanged() {
 		super.notifyDataSetChanged();
 	}
 
 	/**
 	 * Populates the List with the data from the Model.
 	 * Can show a toast, if no data is available
 	 */
 	private void populate( ArrayList<String> keywords ) {
 		//fill
 		items = new ArrayList<String>(keywords);
 		if(items.size() == 0) Toast.makeText(this.context, context.getString(R.string.notification_no_keywords), Toast.LENGTH_LONG).show();
 	}
 
 	/**
 	 * Returns the keyword of a specific list position
 	 * @param position: position of the item
 	 * @return the keyword
 	 */
 	public String getItem(int position) {
 		return items.get(position);
 	}
 
 	/**
 	 * returns just the Id of an list item (it's the position itself)
 	 * @param position - position of the item
 	 * @return position of the item
 	 */
 	public long getItemId(int position) {
 		return position;
 	}
 
 	/**
 	 * @return the size of the list
 	 */
 	public int getCount() {
 		return items.size();
 	}
 	
 	static class ViewHolder {
 		public TextView text;
 		public ImageButton delete;
 	}
 	
 	/**
 	 * Clears all the views and hide them
 	 * @param ViewHolder that holds all views
 	 */
 	private void clearHolder(ViewHolder holder) {
 		holder.text.setText("");
 		holder.text.setVisibility(View.GONE);
 		holder.delete.setVisibility(View.GONE);
 	}
 
 	public boolean add(String string) {
 		if(items.contains(string)) return false;
		String res = string.replaceAll("\\s", "");
		if(res.length() < 1) return false;
 		items.add(string);
 		return true;
 	}
 	
 	public void remove(String string) {
 		items.remove(string);
 	}
 
 	public ArrayList<String> getKeywords() {
 		return items;
 	}
 }
