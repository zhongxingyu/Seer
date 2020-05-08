 package com.trydish.find;
 import com.trydish.main.R;
 import com.trydish.main.R.drawable;
 
 import android.content.Context;
 import android.graphics.Matrix;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.GridView;
 import android.widget.ImageView;
 
 public class ImageAdapter extends BaseAdapter {
 	private Context mContext;
 
 	public ImageAdapter(Context c) {
 		mContext = c;
 	}
 
 	public int getCount() {
 		return mThumbIds.length;
 	}
 
 	public Object getItem(int position) {
 		return null;
 	}
 
 	public long getItemId(int position) {
 		return 0;
 	}
 
 	// create a new ImageView for each item referenced by the Adapter
 	public View getView(int position, View convertView, ViewGroup parent) {
 		ImageView imageView;
 		if (convertView == null) {  // if it's not recycled, initialize some attributes
 			imageView = new ImageView(mContext);
 			imageView.setLayoutParams(new GridView.LayoutParams(400, 400)); // 255, 200
			imageView.setMaxHeight(300);
			imageView.setMaxWidth(300);
			imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
			imageView.setPadding(0, 0, 0, 0);
 		} else {
 			imageView = (ImageView) convertView;
 		}
 
 		imageView.setImageResource(mThumbIds[position]);
 		return imageView;
 	}
 
 	// references to our images
 	private Integer[] mThumbIds = {
 			R.drawable.food, R.drawable.wings,
 			R.drawable.dimsum, R.drawable.burger,
			R.drawable.food1, R.drawable.food, R.drawable.wings,
			R.drawable.dimsum, R.drawable.burger,
			R.drawable.food1, R.drawable.food, R.drawable.wings,
			R.drawable.dimsum, R.drawable.burger,
			R.drawable.food1, R.drawable.food, R.drawable.wings,
			R.drawable.dimsum, R.drawable.burger,
			R.drawable.food1, R.drawable.food, R.drawable.wings,
			R.drawable.dimsum, R.drawable.burger,
			R.drawable.food1
 	};
 }
