 package com.rolflekang.kube95;
 
import java.util.Calendar;

 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 public class PantAdapter extends ArrayAdapter<Pant> {
	@SuppressWarnings("unused")
 	private Context context;
 	private LayoutInflater mInflater;
 	private Pantekassa pantList;
 
 	public PantAdapter(Context context, int textViewResourceId, Pantekassa pantList) {
 		super(context, textViewResourceId, pantList);
 		this.pantList = pantList;
 		this.context = context;
 		this.mInflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		
 	}
 	@Override
 	public int getCount() {
 		return pantList.size();
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		ViewHolder holder;
 
 		if (convertView == null) {
 			convertView = mInflater.inflate(R.layout.pantlistrow, parent, false);
 			holder = new ViewHolder();
 			holder.amount = (TextView) convertView.findViewById(R.id.pantrow_amount);
 			holder.date = (TextView) convertView.findViewById(R.id.pantrow_date);
 			holder.user = (TextView) convertView.findViewById(R.id.pantrow_user);
 			convertView.setTag(holder);
 		} else {
 			holder = (ViewHolder) convertView.getTag();
 		}
//		holder.date.setVisibility(8);
 		// Fill in the actual story info
 		Pant p = pantList.get(position);
 
 		holder.amount.setText(p.getAmount() + "kr");
		//TODO: Use Calendar since getMonth... is deprecated
		holder.date.setText(p.getDate().getDate() + "." + (p.getDate().getMonth()+1) + "." + (p.getDate().getYear()+1900));
 		holder.user.setText(p.getUser());
 		
 		
 		
 		return convertView;
 	}
 
 	static class ViewHolder {
 		TextView amount;
 		TextView date;
 		TextView user;
 	}
 
 }
