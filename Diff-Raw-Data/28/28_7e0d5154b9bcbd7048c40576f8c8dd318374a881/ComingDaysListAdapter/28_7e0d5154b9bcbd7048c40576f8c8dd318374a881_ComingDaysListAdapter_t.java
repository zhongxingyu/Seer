 package com.ese2013.mensaunibe;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.TextView;
 
 import com.ese2013.mensaunibe.CurrentDayListAdapter.ViewHolder;
 import com.ese2013.mensaunibe.model.Model;
 import com.ese2013.mensaunibe.model.menu.DailyMenu;
 import com.ese2013.mensaunibe.model.menu.Menuplan;
 
 public class ComingDaysListAdapter extends BaseAdapter{
 
 	private Context context;
 	private int resource;
 	private LayoutInflater inflater;
 
 	private ArrayList<ListItem> items;
 	private ArrayList<Menuplan> mMenus;
 	private int mMensaId;
 
 	public ComingDaysListAdapter(Context context, int resource, int mensaId) {
 		super();
 		this.context = context;
 		this.resource = resource;
 		this.items = new ArrayList<ListItem>();
 		mMensaId = mensaId;
 		populate();
 	}
 
 	public View getView(int position, View convertView, ViewGroup parent) {
 		View view = convertView;
 		if(view == null) {
 			inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			view = inflater.inflate(this.resource, parent, false);
 			ViewHolder viewHolder = new ViewHolder();
 			viewHolder.date = (TextView) view.findViewById(R.id.menu_date);
 			viewHolder.title = (TextView) view.findViewById(R.id.menu_title);
 			viewHolder.text = (TextView) view.findViewById(R.id.menu_text);
 			view.setTag(viewHolder);
 
 		}
 		ViewHolder holder = (ViewHolder) view.getTag();
		//list is not consistent, if we don't clear the holder
		clearHolder(holder);
		
 		ListItem item = items.get(position);
 
 		if(item.isSection()) {
 			ListSectionItem si = (ListSectionItem) item;
 			holder.date.setLongClickable(false);
 			holder.date.setText(si.toString());
 			holder.date.setVisibility(View.VISIBLE);
 		} else {
 			DailyMenu dm = (DailyMenu) item;
 			holder.title.setText(dm.getTitle());
 			holder.title.setVisibility(View.VISIBLE);
 			holder.text.setText(dm.getMenu());
 			holder.text.setVisibility(View.VISIBLE);
 
 		}
 		return view;
 	}
 
	private void clearHolder(ViewHolder holder) {
		holder.date.setText("");
		holder.date.setVisibility(View.GONE);
		holder.title.setText("");
		holder.title.setVisibility(View.GONE);
		holder.text.setText("");
		holder.text.setVisibility(View.GONE);
	}

 	private void populate() {
 		//fill
 		mMenus = Model.getInstance().getComingDaysMenu(mMensaId);
 		for(Menuplan m : mMenus) {
 			items.add(new ListSectionItem(m.getDate().toText(false)));
 			for (DailyMenu dm : m){
 				items.add(dm);
 			}
 		}
 	}
 
 	public long getItemId(int position) {
 		return position;
 	}
 
 	public int getCount() {
 		return items.size();
 	}
 
 	@Override
 	public Object getItem(int arg0) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 }
