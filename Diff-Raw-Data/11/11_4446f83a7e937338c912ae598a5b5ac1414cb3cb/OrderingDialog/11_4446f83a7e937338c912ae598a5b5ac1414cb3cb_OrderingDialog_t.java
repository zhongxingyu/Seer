 /**
  * @Title: CBOrderingDialog.java
  * @Package: com.android.cb.view
  * @Author: Raiden Awkward<raiden.ht@gmail.com>
  * @Date: 2012-4-6
  */
 package com.android.cb.view;
 
 import com.android.cb.R;
 import com.android.cb.source.CBSettings;
 import com.android.cb.support.CBDish;
 import com.android.cb.support.CBIFOrderHandler;
 import com.android.cb.support.CBMenuItem;
 
 import android.content.Context;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 /**
  * @author raiden
  *
  * @Description ordering dish with count in this dialog
  */
 public class OrderingDialog extends CBBaseDialog {
 
 	public interface Callback {
 		public void onItemAddedToOrder(boolean succeed);
 		public void onItemDeletedFromOrder(boolean succeed);
 		public boolean onItemDeletingFromOrder(CBMenuItem item);
 	}
 
 	Callback mCallback = null;
 	CBMenuItem mMenuItem = null;
 	CBIFOrderHandler mOrderHandler = null;
 	int mOriginalCount = 0;
 
 	private TextView mViewName;
 	private EditText mEditCount;
 	private CBDialogButton mButtonIncrease;
 	private CBDialogButton mButtonDecrease;
 	private CBDialogButton mButtonOk;
 	private CBDialogButton mButtonClear;
 	private CBDialogButton mButtonQuit;
 
 	public OrderingDialog(Context context, boolean cancelable,
 			OnCancelListener cancelListener) {
 		super(context, cancelable, cancelListener);
 		initView();
 	}
 
 	public OrderingDialog(Context context, int theme) {
 		super(context, theme);
 		initView();
 	}
 
 	public OrderingDialog(Context context) {
 		super(context, R.style.CBTranslucentDialog);
 		initView();
 	}
 
 	private void initView() {
 		this.setContentView(R.layout.dialog_ordering);
 		setCanceledOnTouchOutside(true);
 
 		mViewName = (TextView) this.findViewById(R.id.view_name);
 
 		mEditCount = (EditText) this.findViewById(R.id.edit_count);
 		mEditCount.selectAll();
 
 		mButtonIncrease = (CBDialogButton) this.findViewById(R.id.button_increase);
 		mButtonIncrease.setOnClickListener(new Button.OnClickListener() {
 			public void onClick(View v) {
 				increaseCount();
 			}
 		});
 
 		mButtonDecrease = (CBDialogButton) this.findViewById(R.id.button_decrease);
 		mButtonDecrease.setOnClickListener(new Button.OnClickListener() {
 			public void onClick(View v) {
 				decreaseCount();
 			}
 		});
 
 		mButtonOk = (CBDialogButton) this.findViewById(R.id.button_ok);
 		mButtonOk.setOnClickListener(new Button.OnClickListener() {
 			public void onClick(View v) {
 				submit();
 			}
 		});
 
 		mButtonClear = (CBDialogButton) this.findViewById(R.id.button_clear);
 		mButtonClear.setOnClickListener(new Button.OnClickListener() {
 			public void onClick(View v) {
 				clear();
 			}
 		});
 
 		mButtonQuit = (CBDialogButton) this.findViewById(R.id.button_quit);
 		mButtonQuit.setOnClickListener(new Button.OnClickListener() {
 			public void onClick(View v) {
 				OrderingDialog.this.dismiss();
 			}
 		});
 	}
 
 	public void setMenuItem(CBMenuItem item) {
 		if (item == null)
 			return;
 
 		CBDish dish = item.getDish();
 		mViewName.setText(dish.getName());
 
 		if (mOrderHandler != null) {
 			int count = mOrderHandler.getItemOrederedCount(item);
 			mOriginalCount = count;
 
 			if (count == 0)
 				count = 1;
 			setCount(count);
 		}
 
 		mMenuItem = item;
 	}
 
 	private int getCount() {
 		if (mEditCount == null)
 			return 0;
 
 		String countStr = mEditCount.getText().toString().trim();
 		if (countStr.startsWith(this.getContext().getString(R.string.ordering_multiply_char)))
 			countStr = countStr.substring(1);
 
 		return Integer.valueOf(countStr);
 	}
 
 	private void setCount(int count) {
		if (count < 0 || count > CBSettings.getIntValue(CBSettings.CB_SETTINGS_ORDERING_DIALOG_MAX_ITEM_COUNT))
 			return;
 
 		String countStr = this.getContext().getString(R.string.ordering_multiply_char) + String.valueOf(count);
 		mEditCount.setText(countStr);
 		mEditCount.selectAll();
 	}
 
 	public void increaseCount() {
 		int count = getCount();
 		setCount(++count);
 	}
 
 	public void decreaseCount() {
 		int count = getCount();
 
 		setCount(--count);
 	}
 
 	public void clear() {
 		setCount(0);
 	}
 
 	public void submit() {
 		if (mMenuItem == null || mOrderHandler == null)
 			return;
 
 		int count = getCount();
 
 		if (count == mOriginalCount) {
 			dismiss();
 			return;
 		}
 
 		if (count > 0) {
 			boolean res = mOrderHandler.addItemToOrder(mMenuItem, count);
 			if (mCallback != null) {
 				mCallback.onItemAddedToOrder(res);
 			}
 		} else if (count == 0) {
 			if (mCallback != null)
 				if (mCallback.onItemDeletingFromOrder(mMenuItem) == false)
 					return;
 
 			boolean res = mOrderHandler.removeItemFromOrder(mMenuItem);
 			if (mCallback != null) {
 				mCallback.onItemDeletedFromOrder(res);
 			}
 		} else {
 
 		}
 
 		dismiss();
 	}
 
 	/**
 	 * @Description this method should be set before 'setMenuItem' called
 	 * @return void
 	 */
 	public void setOrderHandler(CBIFOrderHandler handler) {
 		mOrderHandler = handler;
 	}
 
 	public void setCallback(Callback callback) {
 		mCallback = callback;
 	}
 
 }
