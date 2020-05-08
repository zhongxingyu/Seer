 package com.zjuhjz.yacleaner.customclass;
 
 import java.util.List;
 import java.util.Map;
 
 import android.content.Context;
 import android.graphics.Color;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.SimpleAdapter;
 
 
 
 public class ProcessListAdapter extends SimpleAdapter {
 	private String[] colours = new String[] {"#CCCCCC", "#FFFFFF"};
 	private List<? extends Map<String, ?>> data;
	
 	public ProcessListAdapter(Context context,
 			List<? extends Map<String, ?>> data, int resource, String[] from,
 			int[] to) {
 		super(context, data, resource, from, to);
 		this.data=data;
 		// TODO Auto-generated constructor stub
 	}
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) 
 	{
 		View view = super.getView(position, convertView, parent);
 		view.setBackgroundColor(Color.parseColor(colours[position % colours.length]));
 		if(view.isSelected()==true)
 		{
 			view.setBackgroundColor(Color.parseColor("#00FFFF"));
 		}
 		return view;
 	}
 
 }
