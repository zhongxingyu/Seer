 package com.itay.mandewho;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.parse.*;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 
 public class RideListAdapter extends ArrayAdapter<ParseObject> {
 
 	private ArrayList<ParseObject> ridesList;
 	private Context context;
 	public RideListAdapter(Context context, int textViewResourceId,
 			List<ParseObject> objects) {
 		super(context, textViewResourceId, objects);
 		this.ridesList = new ArrayList<ParseObject>();
 		if(objects!=null && !objects.isEmpty()){
 			this.ridesList.addAll(objects);
 		}
 		this.context=context;
 	}
 	
 	public View getView(int position, View convertView, ViewGroup parent)
 	{
 		LayoutInflater inflater = (LayoutInflater) context
 		        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		View rideView = inflater.inflate(R.layout.live_list_line, parent,false);

 		return rideView;	
 	}
 
}
