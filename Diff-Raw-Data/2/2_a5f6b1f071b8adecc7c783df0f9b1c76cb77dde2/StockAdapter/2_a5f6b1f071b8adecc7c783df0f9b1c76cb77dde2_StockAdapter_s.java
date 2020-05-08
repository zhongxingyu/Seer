 package com.p130001.pseviewer.adapter;
 
 import java.util.ArrayList;
 
 import com.p130001.pseviewer.R;
 import com.p130001.pseviewer.list.StockList;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 public class StockAdapter extends BaseAdapter{
 	private Context mContext;
 	private ArrayList<StockList> mItems;
 
 	// Constructor
 	public StockAdapter(Context c, ArrayList<StockList> items) {
 		mContext = c;
 		this.mItems = items;
 	}
 
 	public int getCount() {
 		return mItems.size();
 	}
 
 	public StockList getItem(int position) {
 		return mItems.get(position);
 	}
 
 	public long getItemId(int position) {
 		return 0;
 	}
 
 	public View getView(int position, View convertView, ViewGroup parent) {
 
 		LayoutInflater inflater = (LayoutInflater) mContext
 				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
 		View rowView = inflater.inflate(R.layout.row_item, parent, false);
 		
 		TextView tvName = (TextView) rowView.findViewById(R.id.tvName);
 		TextView tvCode = (TextView) rowView.findViewById(R.id.tvCode);
 		TextView tvPercentChange = (TextView) rowView.findViewById(R.id.tvPercentChange);
 		TextView tvPrice = (TextView) rowView.findViewById(R.id.tvPrice);
 		TextView tvVolume = (TextView) rowView.findViewById(R.id.tvVolume);
 		ImageView ivArrow = (ImageView) rowView.findViewById(R.id.ivArrow);
 
 		StockList item = this.mItems.get(position);
 		double percentChange = Double.parseDouble(item.getPercentChange());
 		int color, arrow;
 		
 		if (percentChange < 0) {
 			color = R.color.red;
 			arrow = R.drawable.img_arrow_down;
 		} else if (percentChange > 0) {
 			color = R.color.green;
 			arrow = R.drawable.img_arrow_up;
 		} else {
 			color = R.color.blue;
 			arrow = R.drawable.img_arrow;
 		}
 
 		tvName.setText(item.getName());
 		tvName.setTextColor(mContext.getResources().getColor(color));
 		
 		tvCode.setText(item.getCode());
 		tvCode.setTextColor(mContext.getResources().getColor(color));
 		
 		tvPercentChange.setText(item.getPercentChange());
 		tvPercentChange.setTextColor(mContext.getResources().getColor(color));
 		
 		tvPrice.setText(item.getPrice());
 		tvPrice.setTextColor(mContext.getResources().getColor(color));
 		
 		tvVolume.setText(item.getVolume());
 		tvVolume.setTextColor(mContext.getResources().getColor(color));
 		
 		ivArrow.setImageResource(arrow);
 		
 		return rowView;
 	}
 }
