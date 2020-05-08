 package com.group5.android.fd.activity.dialog;
 
 import android.content.Context;
 import android.view.View;
 import android.widget.Toast;
 
 import com.group5.android.fd.R;
 import com.group5.android.fd.entity.OrderItemEntity;
 
 public class QuantityRemoverDialog extends NumberPickerDialog {
 	protected OrderItemEntity m_orderItem;
 
 	public QuantityRemoverDialog(Context context) {
 		super(context);
 		m_vwQuantity.setText("1");
 		onQuantityChange();
 	}
 
 	public void setOrderItem(OrderItemEntity orderItem) {
 		m_orderItem = orderItem;
 		// m_vwItemName.setText(item.itemName);
 		m_vwQuantity.setText(String.valueOf(m_orderItem.quantity));
 		onQuantityChange();
 	}
 
 	public OrderItemEntity getOrderItem() {
 		return m_orderItem;
 	}
 
 	@Override
 	public void onClick(View v) {
 		switch (v.getId()) {
 		case R.id.btnSet:
 			if (getQuantity() < 0) {
				Toast.makeText(
						getContext(),
						R.string.quantityselectordialog_please_enter_a_valid_quantity,
						Toast.LENGTH_SHORT);
 				m_vwQuantity.requestFocus();
 			} else {
 				dismiss();
 			}
 			break;
 		case R.id.btnPlus:
 			quantity += 1;
 			m_vwQuantity.setText(String.valueOf(quantity));
 			break;
 		case R.id.btnSubtract:
			if (quantity > 0) {
				quantity -= 1;
			} else {
				quantity = 0;
 			}
			m_vwQuantity.setText(String.valueOf(quantity));
 			break;
 		case R.id.btnCancel:
 			quantity = oldQuantity;
 			dismiss();
 			break;
 		}
 	}
 
 }
