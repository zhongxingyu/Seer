 package com.andronym.donatieplus;
 
 import android.content.Context;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.GridView;
 import android.widget.ImageView;
 
 public class ImageAdapter extends BaseAdapter {
 	
 	private Context mContext;
 
 	public ImageAdapter(Context c) {
 		// TODO Auto-generated constructor stub
 		mContext = c;
 	}
 
 	@Override
 	public int getCount() {
 		// TODO Auto-generated method stub
 		return mThumbIds.length;
 	}
 
 	@Override
 	public Object getItem(int position) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public long getItemId(int position) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		// TODO Auto-generated method stub
 		ImageView imageView;
 		if(convertView == null)
 		{
 			imageView = new ImageView(mContext);
 			imageView.setLayoutParams(new GridView.LayoutParams(150, 150));
 			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
 			imageView.setPadding(8, 8, 8, 8);
 		}else{
 			imageView = (ImageView) convertView;
 		}
 		
 		imageView.setImageResource(mThumbIds[position]);
 		return imageView;
 	}
 	
 	private Integer[] mThumbIds = {
 		R.drawable.alzheimer, 
 		R.drawable.greenpeace,
 		R.drawable.ifaw,
 		R.drawable.kwf,
		R.drawable.rode_kruis,
 		R.drawable.unicef,
 		R.drawable.warchild,
 		R.drawable.wish,
 		R.drawable.wnf
 	};
 
 }
