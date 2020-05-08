 package com.group5.android.fd.activity.dialog;
 
 import android.app.Dialog;
 import android.content.Context;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup.LayoutParams;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.EditText;
 
 import com.group5.android.fd.R;
 import com.group5.android.fd.entity.AbstractEntity;
 import com.group5.android.fd.entity.ItemEntity;
 import com.group5.android.fd.entity.OrderItemEntity;
 
 public class NumberPickerDialog extends Dialog implements OnClickListener {
 	protected EditText m_vwQuantity;
 	protected Button m_vwPlus;
 	protected Button m_vwSubtract;
 	protected Button m_vwbtnSet;
 	protected Button m_vwCancel;
 	protected boolean m_isSet = false;
 	protected AbstractEntity m_entity = null;
 
 	/*
 	 * oldQuantity va quantity luu tru gia tri so luong truoc va sau khi duoc
 	 * thay doi
 	 */
 
 	public NumberPickerDialog(Context context) {
 		super(context);
 		initLayout();
 	}
 
 	public void setEntity(AbstractEntity entity) {
 		m_entity = entity;
 		if (entity instanceof ItemEntity) {
 			setQuantity(2);
 
 		} else if (entity instanceof OrderItemEntity) {
 			setQuantity(((OrderItemEntity) m_entity).quantity);
 		}
 
 	}
 
 	protected void setQuantity(int quantity) {
 		m_vwQuantity.setText(String.valueOf(quantity));
 		m_vwQuantity.selectAll();
 	}
 
 	public AbstractEntity getEntity() {
 		return m_entity;
 	}
 
 	protected void initLayout() {
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 
 		setContentView(R.layout.dialog_quantity_set);
 		getWindow().setLayout(LayoutParams.FILL_PARENT,
 				LayoutParams.WRAP_CONTENT);
 
 		m_vwQuantity = (EditText) findViewById(R.id.txtQuantity);
 		m_vwPlus = (Button) findViewById(R.id.btnPlus);
 		m_vwSubtract = (Button) findViewById(R.id.btnSubtract);
 		m_vwbtnSet = (Button) findViewById(R.id.btnSet);
 		m_vwCancel = (Button) findViewById(R.id.btnCancel);
 
 		/*
 		 * thiet lap gia tri mac dinh cho text hien thi o EditText
 		 */
 
 		m_vwbtnSet.setOnClickListener(this);
 		m_vwPlus.setOnClickListener(this);
 		m_vwSubtract.setOnClickListener(this);
 		m_vwCancel.setOnClickListener(this);
 
 	}
 
 	@Override
 	public void onClick(View v) {
 		int quantity = getQuantity();
 		switch (v.getId()) {
 		case R.id.btnSet:
 			m_isSet = true;
 			dismiss();
 			break;
 		case R.id.btnPlus:
 			quantity += 1;
 			setQuantity(quantity);
 			break;
 		case R.id.btnSubtract:
 			if (quantity > 0) {
 				quantity -= 1;
 			} else {
 				quantity = 0;
 			}
 			setQuantity(quantity);
 			break;
 		case R.id.btnCancel:
 			dismiss();
 			break;
 		}
 	}
 
 	public int getQuantity() {
 		try {
 			return Integer.valueOf(m_vwQuantity.getText().toString());
 
 		} catch (NumberFormatException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return 0;
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if (keyCode == KeyEvent.KEYCODE_BACK) {
 			dismiss();
 		}
 		return true;
 	}
 
 	public boolean isSet() {
 		return m_isSet;
 	}
 }
