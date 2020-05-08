 package com.remirran.digitalmenu;
 
 import com.remirran.digitalmenu.data.Order;
 import com.remirran.digitalmenu.data.Tools;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class OrderAdapter extends BaseAdapter {
 	private Context ctx;
 	private LayoutInflater linf;
 	
 	public OrderAdapter(Context context) {
 		ctx = context;
 		linf = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 	}
 	
 	@Override
 	public int getCount() {
 		return Order.getSize();
 	}
 
 	@Override
 	public Object getItem(int position) {
 		return Order.getItemByIndex(position);
 	}
 
 	@Override
 	public long getItemId(int position) {
 		return position;
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		View view = convertView;
 		if (view == null) {
 			view = linf.inflate(R.layout.main_order_item, parent, false);
 		}
 		
 		((TextView) view.findViewById(R.id.order_name)).setText(Order.getItemByIndex(position).getName());
 		((TextView) view.findViewById(R.id.order_count)).setText("x"+Order.getCountByIndex(position)+"=");
 		((TextView) view.findViewById(R.id.order_price)).setText(Tools.formatCurrency(ctx, Order.getSumByIndex(position)));
 		ImageView iv = (ImageView) view.findViewById(R.id.order_cancel);
 		iv.setOnClickListener(imgRemoveClickListener);
		iv.setTag(position);
 		return view;
 	}
 	
 	OnClickListener imgRemoveClickListener = new OnClickListener() {
 		
 		@Override
 		public void onClick(View v) {
 			Order.remove((Integer) v.getTag());
 			((CafeMenuActivity) ctx).updateOrderDetails();
 		}
 	};
 	
 }
