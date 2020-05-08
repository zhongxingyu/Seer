 package com.praszapps.owetracker.adapter;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Context;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.praszapps.owetracker.R;
 import com.praszapps.owetracker.bo.Due;
 
 public class DueAdapter extends ArrayAdapter<Due> {
 
 	//Declaring variables
 	LayoutInflater inflater;
 	Context mContext;
 	int layoutResourceId;
 	ArrayList<Due> dueData = null;
 	String currency;
 	String friendName;
 
 	public DueAdapter(Context mContext, int layoutResourceId, ArrayList<Due> data, String currency, String friendName) {
 
 		//Initializing views
 		super(mContext, layoutResourceId, data);
 		this.mContext = mContext;
 		this.layoutResourceId = layoutResourceId;
 		this.dueData = data;
 		this.currency = currency;
 		Log.e("Pani", currency);
 		this.friendName = friendName;
 
 	}
 	
 	public void setCurrency(String currency) {
 		this.currency = currency;
 	}
 	
	public void setFriendName(String friendName) {
		this.friendName = friendName;
	}
	
 	public void updateAdapter(ArrayList<Due> dueList) {
 		// Updating adapter
 	    this.dueData = dueList;
 	    this.notifyDataSetChanged();
 	}
 	
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		
 		View listItem = convertView;
 		// Inflating the oweboard_details_list_item.xml
 		LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
 		listItem = inflater.inflate(layoutResourceId, parent, false);
 		TextView textViewDate = (TextView) listItem.findViewById(R.id.textViewDate);
 		TextView textViewAmtDetails = (TextView) listItem.findViewById(R.id.textViewAmtDetails);
 		TextView textViewReason = (TextView) listItem.findViewById(R.id.textViewReason);
 		ImageView imageViewDueStatus = (ImageView) listItem.findViewById(R.id.imageViewDueStatus);
 		textViewDate.setText(dueData.get(position).getFormattedDate());
 		String summary = null;
 		//Log.e("Pani", dueData.get(position).getCurrency());
 		if(dueData.get(position).getAmount() >=0 ) {
 			summary = "I owe "+currency+Math.abs(dueData.get(position).getAmount());
 		} else {
 			
 			summary = friendName+" owes "+currency+Math.abs(dueData.get(position).getAmount());
 		}
 		
 		textViewAmtDetails.setText(summary);
 		textViewReason.setText(dueData.get(position).getReason());
 		dueData.get(position).setCurrency(currency);
 		
 		if (dueData.get(position).getAmount() <= 0) {
 			imageViewDueStatus.setImageResource(R.drawable.bluesquare);
 		} else {
 			imageViewDueStatus.setImageResource(R.drawable.redsquare);
 		}
 		return listItem;
 	}
 	
 }
