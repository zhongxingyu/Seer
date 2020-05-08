 package ru.inventos.yum.adapters;
 
 import java.util.ArrayList;
 import java.util.Locale;
 
 import ru.inventos.yum.Cart;
 import ru.inventos.yum.CartItem;
 import ru.inventos.yum.Consts;
 import ru.inventos.yum.R;
 import ru.inventos.yum.Utils;
 import ru.inventos.yum.R.id;
 import ru.inventos.yum.R.layout;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.TextView;;
 
 public class OrderListAdapter extends BaseAdapter {
	private final static int MAX_LINE_LENGTH = 18;
 	private ArrayList<View> mItems;
 	private Context mContext;
 	private LayoutInflater mInflater;
 	private Cart mCart; 
 		
 	public OrderListAdapter(Context context, Cart cart) {
 		mContext = context;
 		mInflater = LayoutInflater.from(context);
 		mCart = cart;
 		mItems = new ArrayList<View>();
 		createList();
 	}
 	
 	public void createList() {
 		int count = mCart.getCount();
 		View view;
 		for (int i = 0; i < count; i++) {
 			view = mInflater.inflate(R.layout.order_list_item, null);
 			CartItem item = mCart.getItemByIndex(i);
 			TextView name = (TextView) view.findViewById(R.id.order_item_name);
 			String str = Integer.toString(i+1) + ". " + item.name + " (" 
 					+ Integer.toString(item.weight) + " г.)";
 			str = Utils.getFormatText(str, MAX_LINE_LENGTH, true);
 			name.setText(str);
 			TextView  price = (TextView) view.findViewById(R.id.order_item_price);
 			str = Integer.toString(item.count) + " x " 
 					+ String.format(Locale.US, "%.2f", item.price) + " " + Consts.RU_SYMBOL;
 			price.setText(str);
 			mItems.add(view);
 		}	
 	}
 	
 	@Override
     public View getView(int position, View convertView, ViewGroup parent) {
 		return mItems.get(position);
 	}
 	
 	@Override
 	public int getCount() {
 		return mItems.size(); 
 	}
 	
 	@Override
 	public Object getItem(int position) {
 		return null;
 	}
 
 	@Override
 	public long getItemId(int position) {
 		return position;
 	}
 }
