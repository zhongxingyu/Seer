 package com.shvelo.guesslogo;
 
 import android.content.Context;
 import android.graphics.Color;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 
 public class ImageAdapter extends BaseAdapter {
 	Context context;
 
 	public ImageAdapter(Context context) {
 		this.context = context;
 	}
 
 	public int getCount() {
 		return BrandManager.size();
 	}
 
 	public View getView(int position, View convertView, ViewGroup parent) {
		View v;
 		
 		Brand item = (Brand)getItem(position);
 		
 		if (convertView == null) {
			LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			v = li.inflate(R.layout.grid_item, null);
		} else {
			v = convertView;
 		}
		
		ImageView iv = (ImageView) v.findViewById(R.id.grid_item);
		iv.setImageDrawable(item.logo);
		
 		if (item.guessed) {
 			v.setBackgroundColor(Color.GRAY);
 		}
		
 		return v;
 	}
 
 	public Object getItem(int position) {
 		return BrandManager.get(position);
 	}
 
 	public long getItemId(int arg0) {
 		return (long) arg0;
 	}
 }
