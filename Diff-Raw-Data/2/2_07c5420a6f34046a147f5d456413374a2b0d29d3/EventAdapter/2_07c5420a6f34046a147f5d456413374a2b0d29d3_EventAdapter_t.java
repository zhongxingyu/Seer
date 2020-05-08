 package org.gkgk.tankfan;
 
 import java.util.Map;
 import java.util.List;
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.widget.ListAdapter;
 import android.database.DataSetObserver;
 import android.provider.BaseColumns;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
 
 public class EventAdapter implements ListAdapter {
 
 	private static final String TAG = EventAdapter.class.getSimpleName();
 
     Context context;
     List<Map<String, String>> data = new ArrayList<Map<String, String>>();
 
 	public EventAdapter(Context context) {
 		this.context = context;
        this.data = new AdapterHelper(context, DBHelper.EVENTS_TABLE).loadData();
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 
 		Map<String, String> obj = this.getItem(position);
 
 		View view = convertView;
 		if (view == null) {
 			LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			view = inflater.inflate(R.layout.event_row, null);
         }
 
 		((TextView) view.findViewById(R.id.eventTitle)).setText(obj.get("title"));
 		((TextView) view.findViewById(R.id.eventDate)).setText(obj.get("eventdate"));
 
 		return view;
 	}
 
 	@Override
 	public void registerDataSetObserver(DataSetObserver observer) {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void unregisterDataSetObserver(DataSetObserver observer) {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public int getCount() {
 		return this.data.size();
 	}
 
 	@Override
 	public Map<String, String> getItem(int position) {
         return this.data.get(position);
     }
 
 	@Override
 	public long getItemId(int position) {
 		return Long.valueOf(this.getItem(position).get(BaseColumns._ID));
 	}
 
 	@Override
 	public boolean hasStableIds() {
 		return true;
 	}
 
 	@Override
 	public int getItemViewType(int position) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public int getViewTypeCount() {
 		return 1;
 	}
 
 	@Override
 	public boolean isEmpty() {
 		return this.getCount() > 0;
 	}
 
 	@Override
 	public boolean areAllItemsEnabled() {
 		return true;
 	}
 
 	@Override
 	public boolean isEnabled(int position) {
 		return true;
 	}
 }
