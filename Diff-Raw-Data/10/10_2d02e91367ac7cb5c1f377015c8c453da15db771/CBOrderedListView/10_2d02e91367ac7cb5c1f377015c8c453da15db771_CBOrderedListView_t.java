 /**
  * @Title: CBOrderedList.java
  * @Package: com.android.cb.view
  * @Author: Raiden Awkward<raiden.ht@gmail.com>
  * @Date: 2012-4-7
  */
 package com.android.cb.view;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import com.android.cb.R;
 import com.android.cb.support.CBOrder;
 import com.android.cb.support.CBOrder.OrderedItem;
 
 import android.content.Context;
 import android.util.AttributeSet;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 
 /**
  * @author raiden
  *
  * @Description list view to show ordered items
  */
 public class CBOrderedListView extends ListView {
 
 	public interface Callback {
 		public void onOrderedItemEditInList(CBOrder.OrderedItem item);
 		public void onOrderedItemRemoveInList(CBOrder.OrderedItem item);
 		public void onOrderedItemListUpdate();
 	}
 
 	private Callback mCallback = null;
 	private CBOrderedListView.ListAdapter mAdapter = null;
 
 	public CBOrderedListView(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 		initListView();
 	}
 
 	public CBOrderedListView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		initListView();
 	}
 
 	public CBOrderedListView(Context context) {
 		super(context);
 		initListView();
 	}
 
 	private void initListView() {
 
 	}
 
 	public void setOrder(CBOrder order) {
 		if (order == null)
 			return;
 
 		LinkedList<CBOrder.OrderedItem> orderedList = order.getOrderedItemsWithCount();
 		if (orderedList == null)
 			return;
 
 		mAdapter = new ListAdapter(this.getContext(), orderedList, this);
 		this.setAdapter(mAdapter);
 	}
 
 	protected void onOrderItemEdit(CBOrder.OrderedItem item) {
 		if (mCallback == null)
 			return;
 
 		mCallback.onOrderedItemEditInList(item);
 	}
 
 	protected void onOrderItemRemove(CBOrder.OrderedItem item) {
 		if (mCallback == null)
 			return;
 		mCallback.onOrderedItemRemoveInList(item);
 	}
 
 	public void refresh() {
 		if (mCallback == null)
 			return;
 
 		mCallback.onOrderedItemListUpdate();
 	}
 
 	public Callback getCallback() {
 		return mCallback;
 	}
 
 	public void setCallback(Callback callback) {
 		this.mCallback = callback;
 	}
 
	public class ListItemView {
 		private TextView mViewName = null;
 		private TextView mViewPrice = null;
 		private TextView mViewCount = null;
 		private CBDialogButton mButtonEdit = null;
 		private CBDialogButton mButtonRemove = null;
 
 		private View mBaseView = null;
 
 		public ListItemView(View baseView) {
 			mBaseView = baseView;
 		}
 
 		public TextView getViewName() {
 			return mViewName;
 		}
 
 		public void setViewName(int id) {
 			this.mViewName = (TextView) mBaseView.findViewById(id);
 		}
 
 		public TextView getViewPrice() {
 			return mViewPrice;
 		}
 
 		public void setViewPrice(int id) {
 			this.mViewPrice = (TextView) mBaseView.findViewById(id);
 		}
 
 		public TextView getViewCount() {
 			return mViewCount;
 		}
 
 		public void setViewCount(int id) {
 			this.mViewCount = (TextView) mBaseView.findViewById(id);
 		}
 
 		public CBDialogButton getButtonEdit() {
 			return mButtonEdit;
 		}
 
 		public void setButtonEdit(int id) {
 			this.mButtonEdit = (CBDialogButton) mBaseView.findViewById(id);
 		}
 
 		public CBDialogButton getButtonRemove() {
 			return mButtonRemove;
 		}
 
 		public void setButtonRemove(int id) {
 			this.mButtonRemove = (CBDialogButton) mBaseView.findViewById(id);
 		}
 
 		public View getBaseView() {
 			return mBaseView;
 		}
 
 		public void setBaseView(View baseView) {
 			this.mBaseView = baseView;
 		}
 	} // class ListItemView
 
 	public class ListAdapter extends ArrayAdapter<CBOrder.OrderedItem> {
 
 		private LayoutInflater mInflater;
 		private static final int sListItemSource = R.layout.ordered_list_item;
 		private CBOrderedListView mOrderedListView;
 
 		public ListAdapter(Context context, List<OrderedItem> objects, CBOrderedListView listView) {
 			super(context, 0, objects);
 			mInflater = LayoutInflater.from(CBOrderedListView.this.getContext());
 
 			mOrderedListView = listView;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			if (convertView == null) {
 				convertView = mInflater.inflate(sListItemSource, null, false);
 			}
 
 			final CBOrder.OrderedItem orderedItem = this.getItem(position);
 
 			ListItemView listItemView = (ListItemView) convertView.getTag();
 			if (listItemView == null) {
 				listItemView = new ListItemView(convertView);
 				listItemView.setButtonRemove(R.id.button_remove);
 				listItemView.setButtonEdit(R.id.button_edit);
 				listItemView.setViewCount(R.id.view_count);
 				listItemView.setViewName(R.id.view_name);
 				listItemView.setViewPrice(R.id.view_price);
 
 				convertView.setTag(listItemView);
 			}
 
 			listItemView.getButtonEdit().setOnClickListener(new Button.OnClickListener() {
 				public void onClick(View arg0) {
 					mOrderedListView.onOrderItemEdit(orderedItem);
 				}
 			});
 
 			listItemView.getButtonRemove().setOnClickListener(new Button.OnClickListener() {
 				public void onClick(View arg0) {
 					mOrderedListView.onOrderItemRemove(orderedItem);
 				}
 			});
 
 			listItemView.getViewName().setText(orderedItem.item.getDish().getName());
 			listItemView.getViewPrice().setText(mOrderedListView.getContext().getResources().getString(R.string.ordered_listitem_label_price)
 							+ String.valueOf(orderedItem.item.getDish().getPrice()));
 			listItemView.getViewCount().setText(mOrderedListView.getContext().getResources().getString(R.string.ordered_listitem_label_count)
 							+ orderedItem.count);
 
 			return convertView;
 		}
 
 	} // class CBAdapter
 
 }
