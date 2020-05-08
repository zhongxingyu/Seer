 package ru.inventos.yum;
 
 import java.util.ArrayList;
 import java.util.Locale;
 
 import android.content.Context;
 import android.content.Intent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageButton;
 import android.widget.TextView;
 
 public class MainListAdapter extends BaseAdapter implements OnClickListener, Updatable {
 	private final static int MAX_LINE_LENGTH = 18;
 	private ArrayList<View> mItems;
 	private Context mContext;
 	private LayoutInflater mInflater;
 	private Cart mCart; 
 	private NetStorage mNetStorage;
 	private LunchItem[] mLunchItems;
	private int[] mLunchIds;
 	private String mCurrentCategory;
 	
 	public MainListAdapter(Context context, Cart cart, NetStorage netStorage) {
 		mContext = context;
 		mInflater = LayoutInflater.from(context);
 		mCart = cart;
 		mNetStorage = netStorage;
 		mItems = new ArrayList<View>();
 	}
 	
 	@Override
 	public void update() {
 		this.notifyDataSetChanged();
 	}
 	
 	
 	
 	public void setCategory(String name) {
 		mItems.clear();
 		mCurrentCategory = name;
 		mNetStorage.getLunchList(this);		
 	}
 	
 	private static String getTextWithTabs(String str, int charsInStr) {
 		int len = str.length();
 		int[] ind = new int[len];
 		int chars = 0;
 		int qty = 0;
 		for(int i = 0; i < len; i++) {
 			chars++;
 			if ((str.charAt(i) == ' ') && 
 					(str.indexOf(' ', i + 1) - i + chars - 1 > charsInStr)) {
 				ind[qty] = i;
 				chars = 0; 
 				qty++; 
 			}
 			else if ((str.charAt(i) == '(') && 
 					(str.indexOf(')', i + 1) - i + chars > charsInStr)) {
 				ind[qty] = i - 1;
 				chars = 0;
 				qty++;
 				
 			}
 			 
 		}
 		String res = ""; 
 		int start = 0;
 		for (int i = 0; i < qty; i++) {
 			res = res + str.substring(start, ind[i]) +"\n\t";
 			start = ind[i] + 1;
 		}
 		res = res + str.substring(start, len);
 		return res;		
 	}
 	
 	
 	
 	public void UpdateList(LunchItem[] lunchItems) {
		mLunchItems = lunchItems;
 		View view = null; 
 		int num = 0;
		if ((mLunchItems != null) && (lunchItems.length > 0)) {
			mLunchIds = new int[lunchItems.length];
			for(LunchItem item: mLunchItems) {
 				if (item.category.equals(mCurrentCategory) && (item.count != 0)) {
					mLunchIds[num] = item.id;
 					num++;
 					view = mInflater.inflate(R.layout.main_list_item, null);
 					TextView tv =  (TextView) view.findViewById(R.id.main_list_item_name);
 					String str = Integer.toString(num) + ". " + item.name + " (" 
 							+ Integer.toString(item.weight) + " г.)";
 					str = getTextWithTabs(str, MAX_LINE_LENGTH);
 					tv.setText(str);
 					tv =  (TextView) view.findViewById(R.id.main_list_item_price);
 					str = String.format(Locale.US, "%.2f", item.price)  + " ";						
 					tv.setText(str);
 					ImageButton btn = (ImageButton) view.findViewById(R.id.main_list_item_add_btn); 
 					btn.setTag(new Integer(num-1));
 					btn.setOnClickListener(this);
 					mItems.add(view);
 				}
 			}
 		}
 		this.notifyDataSetChanged();
 	}
 	
 	
 	@Override
     public View getView(int position, View convertView, ViewGroup parent) {
		CartItem item = mCart.getItem(mLunchIds[position]);
 		View view = mItems.get(position);
 		TextView count = (TextView) view.findViewById(R.id.main_list_item_count);
 		ImageButton btn = (ImageButton) view.findViewById(R.id.main_list_item_add_btn); 
 		if (item == null) {
 			count.setText("");
 			btn.setImageResource(R.drawable.button_add);			
 		}
 		else {
 			count.setText(Integer.toString(item.count));
 			btn.setImageResource(R.drawable.button_more);			
 		}
 		return view;
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
 	
 	@Override 
 	public void onClick(View v) {
 		Integer index = (Integer) v.getTag();
		int ind = index.intValue();		
		CartItem citem = mCart.getItem(mLunchIds[ind]);
 		LunchItem item = mLunchItems[ind];
 		if (citem == null) {						
			mCart.add(mLunchIds[ind], item.name, item.price, item.weight);			
 		}
 		else {
 			Intent intent = new Intent(mContext, Portion.class);
			intent.putExtra(Consts.PORTION_ELEMENT_ID, mLunchIds[ind]);
 			intent.putExtra(Consts.PORTION_MAX_COUNT, item.count);
 			mContext.startActivity(intent);
 		}
 		this.notifyDataSetChanged();
 	}
 }
