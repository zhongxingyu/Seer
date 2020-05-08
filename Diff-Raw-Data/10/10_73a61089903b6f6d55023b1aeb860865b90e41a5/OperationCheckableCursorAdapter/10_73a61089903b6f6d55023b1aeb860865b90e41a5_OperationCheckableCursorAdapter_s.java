 package org.maupu.android.tmh.ui.widget;
 
 import java.util.Currency;
 
 import org.maupu.android.tmh.R;
 import org.maupu.android.tmh.database.CurrencyData;
 import org.maupu.android.tmh.database.OperationData;
 import org.maupu.android.tmh.database.object.Operation;
 import org.maupu.android.tmh.ui.StaticData;
 import org.maupu.android.tmh.util.NumberUtil;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.view.View;
 import android.widget.TextView;
 
 public class OperationCheckableCursorAdapter extends CheckableCursorAdapter {
 
 	public OperationCheckableCursorAdapter(Context context, int layout,
 			Cursor c, String[] from, int[] to) {
 		super(context, layout, c, from, to);
 	}
 	
 	@Override
 	public void bindView(View view, Context context, Cursor cursor) {
 		super.bindView(view, context, cursor);
 		
 		TextView tvAmount = (TextView)view.findViewById(R.id.amount);
 		int idxAmount = cursor.getColumnIndex(OperationData.KEY_AMOUNT);
 		Double amount = cursor.getDouble(idxAmount);
 		
 		int idxCurrencySymbol = cursor.getColumnIndex(CurrencyData.KEY_SHORT_NAME);
 		String currencySymbol = cursor.getString(idxCurrencySymbol);
 		
 		TextView tvConvAmount = (TextView)view.findViewById(R.id.convAmount);
		int idxConvAmount = cursor.getColumnIndex(CurrencyData.KEY_CURRENCY_LINKED);
 		Double convAmount = Double.parseDouble(cursor.getString(idxConvAmount));
 		
 		// format amount
 		tvAmount.setText(NumberUtil.formatDecimalLocale(amount) + " " + currencySymbol);
 		tvConvAmount.setText(NumberUtil.formatDecimalLocale(convAmount) + " " + 
 				Currency.getInstance(StaticData.getMainCurrency().getIsoCode()).getSymbol());
 		
 		// Set color for amount
 		if(amount >= 0d)
 			tvAmount.setTextColor(Operation.COLOR_POSITIVE_AMOUNT);
 		else
 			tvAmount.setTextColor(Operation.COLOR_NEGATIVE_AMOUNT);
 	}
 
 }
