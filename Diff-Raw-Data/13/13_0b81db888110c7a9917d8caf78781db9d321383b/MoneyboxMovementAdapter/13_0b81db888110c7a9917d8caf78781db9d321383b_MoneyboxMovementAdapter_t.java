 package com.cachirulop.moneybox.adapter;
 
 import java.text.DateFormat;
 import java.util.Date;
 import java.util.List;
 
 import android.content.Context;
 import android.graphics.Color;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.TextView;
 
 import com.cachirulop.moneybox.R;
 import com.cachirulop.moneybox.activity.MovementsActivity;
 import com.cachirulop.moneybox.entity.Movement;
 import com.cachirulop.moneybox.manager.CurrencyManager;
 import com.cachirulop.moneybox.manager.MovementsManager;
 
 public class MoneyboxMovementAdapter extends BaseAdapter {
 	private MovementsActivity _parent;
 	private LayoutInflater _inflater;
 	private List<Movement> _lstMovements = null;
 
 	public MoneyboxMovementAdapter(Context context) {
 		_parent = (MovementsActivity) context;
 		_inflater = LayoutInflater.from(_parent);
 	}
 
 	public int getCount() {
 		if (_lstMovements == null) {
 			refreshMovements();
 		}
 
 		return _lstMovements.size();
 	}
 
 	public Object getItem(int position) {
 		if (_lstMovements == null) {
 			refreshMovements();
 		}
 
 		return _lstMovements.get(position);
 	}
 
 	public long getItemId(int position) {
 		if (_lstMovements == null) {
 			refreshMovements();
 		}
 
 		return _lstMovements.get(position).getIdMovement();
 	}
 
 	public View getView(int position, View convertView, ViewGroup parent) {
 		if (convertView == null) {
 			convertView = _inflater.inflate(R.layout.movement_row, null);
 		}
 
 		Movement m;
 		TextView txtDate;
 		TextView txtDescription;
 		TextView txtAmount;
 
 		m = _lstMovements.get(position);
 		txtDate = (TextView) convertView
 				.findViewById(R.id.txtRowMovementDate);
 		txtDescription = (TextView) convertView
 				.findViewById(R.id.txtRowMovementDescription);
 		txtAmount = (TextView) convertView
 				.findViewById(R.id.txtRowMovementAmount);
 
 		txtDate.setText(formatDate(m.getInsertDate()));
 		txtAmount.setText(CurrencyManager.formatAmount(m.getAmount()));
 		txtDescription.setText(m.getDescription());
 
 		if (m.isBreakMoneybox()) {
 			txtDate.setTextColor(Color.RED);
 			txtAmount.setTextColor(Color.RED);
 			txtDescription.setTextColor(Color.RED);
 		}
 		else if (m.getAmount() < 0) {
 			txtDate.setTextColor(Color.YELLOW);
 			txtAmount.setTextColor(Color.YELLOW);
 			txtDescription.setTextColor(Color.YELLOW);
 		}
 		else {
			int blue;
			
			blue = Color.rgb(5, 143, 255);
			
			txtDate.setTextColor(blue);
			txtAmount.setTextColor(blue);
			txtDescription.setTextColor(blue);
 		}
 
 		return convertView;
 	}
 
 	private String formatDate(Date d) {
 		return DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
 				DateFormat.SHORT).format(d);
 	}
 	
 	public void refreshMovements() {
 		_lstMovements = MovementsManager.getAllMovements();
 		notifyDataSetChanged();
 	}
 
 }
