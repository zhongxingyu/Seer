 package com.dzebsu.acctrip.eventcurrencies.management;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.dzebsu.acctrip.BaseSupportStableDialog;
 import com.dzebsu.acctrip.R;
 import com.dzebsu.acctrip.SimpleDialogListener;
 import com.dzebsu.acctrip.currency.utils.CurrencyUtils;
 import com.dzebsu.acctrip.db.datasources.CurrencyPairDataSource;
 import com.dzebsu.acctrip.models.CurrencyPair;
 import com.dzebsu.acctrip.models.dictionaries.Currency;
 
 public class EditCurrencyPairDialog extends BaseSupportStableDialog {
 
 	private Currency primaryCurrency;
 
 	private CurrencyPair currencyPair;
 
 	private boolean leftOri = true;
 
 	private View view;
 
 	private SimpleDialogListener listener = null;
 
 	public SimpleDialogListener getListener() {
 		return listener;
 	}
 
 	public void setListener(SimpleDialogListener listener) {
 		this.listener = listener;
 	}
 
 	private TextView firstCurrencyCode = null;
 
 	private TextView secondCurrencyCode = null;
 
 	private EditText firstCurrencyValue = null;
 
 	private EditText secondCurrencyValue = null;
 
 	public static EditCurrencyPairDialog newInstance(Currency primaryCurrency, CurrencyPair currencyPair) {
 		EditCurrencyPairDialog dialog = new EditCurrencyPairDialog();
 		dialog.setPrimaryCurrency(primaryCurrency);
 		dialog.setCurrencyPair(currencyPair);
 		return dialog;
 	}
 
 	@Override
 	public void onAttach(Activity activity) {
 		super.onAttach(activity);
 		if (activity instanceof SimpleDialogListener) {
 			this.listener = (SimpleDialogListener) activity;
 		}
 	}
 
 	@Override
 	public void onDetach() {
 		super.onDetach();
 		listener = null;
 	}
 
 	@Override
 	public Dialog onCreateDialog(Bundle savedInstanceState) {
 		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 		builder.setTitle(R.string.edit_currency_rate).setNegativeButton(R.string.back, new OnClickListener() {
 
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				if (listener != null) listener.negativeButtonDialog(null);
 				dismiss();
 
 			}
 		}).setPositiveButton(R.string.save, new OnClickListener() {
 
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				saveChanges();
 				if (listener != null) listener.positiveButtonDialog(null);
 				dismiss();
 				Toast.makeText(getActivity(), R.string.currency_rate_edited, Toast.LENGTH_SHORT).show();
 			}
 		});
 		LayoutInflater inflater = this.getActivity().getLayoutInflater();
 		view = inflater.inflate(R.layout.row_currency_pair_list_wf, null);
 		((ImageButton) view.findViewById(R.id.cur_row_reverse_button)).setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				flipCurrencies();
 			}
 		});
 		firstCurrencyCode = ((TextView) view.findViewById(R.id.cur_row_cur1));
 		secondCurrencyCode = ((TextView) view.findViewById(R.id.cur_row_cur2));
 		firstCurrencyValue = ((EditText) view.findViewById(R.id.cur_row_et_cur1));
 		secondCurrencyValue = ((EditText) view.findViewById(R.id.cur_row_et_cur2));
 		firstCurrencyValue.setText("1.00");
 		if (leftOri = CurrencyUtils.isPrimaryLeftOrientation(currencyPair.getRate())) {
 			firstCurrencyCode.setText(primaryCurrency.getCode());
 			secondCurrencyCode.setText(currencyPair.getSecondCurrency().getCode());
 			secondCurrencyValue.setText(CurrencyUtils.formatDecimalImportant(currencyPair.getRate()));
 		} else {
 			firstCurrencyCode.setText(currencyPair.getSecondCurrency().getCode());
 			secondCurrencyCode.setText(primaryCurrency.getCode());
 			secondCurrencyValue.setText(CurrencyUtils.formatDecimalImportant(1 / currencyPair.getRate()));
 		}
 		builder.setView(view);
 		return builder.create();
 	}
 
 	protected void flipCurrencies() {
 		leftOri = !leftOri;
 		if (leftOri) {
 			firstCurrencyCode.setText(primaryCurrency.getCode());
 			secondCurrencyCode.setText(currencyPair.getSecondCurrency().getCode());
 		} else {
 			firstCurrencyCode.setText(currencyPair.getSecondCurrency().getCode());
 			secondCurrencyCode.setText(primaryCurrency.getCode());
 		}
 		String s = secondCurrencyValue.getText().toString();
 		secondCurrencyValue.setText(firstCurrencyValue.getText().toString());
 		firstCurrencyValue.setText(s);
 	}
 
 	protected void saveChanges() {
 		double c1;
 		double c2;
 		String s1;
 		String s2;
 		if (leftOri) {
 			s1 = firstCurrencyValue.getText().toString();
 			s2 = secondCurrencyValue.getText().toString();
 		} else {
 			s1 = secondCurrencyValue.getText().toString();
 			s2 = firstCurrencyValue.getText().toString();
 		}
 		c1 = CurrencyUtils.getDouble(s1);
 		c2 = CurrencyUtils.getDouble(s2);
		if (c1 == 0.) c1 = 1.;
		if (c2 == 0.) c2 = 1.;
 		new CurrencyPairDataSource(getActivity()).update(currencyPair.getEventId(), currencyPair.getSecondCurrency()
 				.getId(), c2 / c1);
 	}
 
 	public Currency getPrimaryCurrency() {
 		return primaryCurrency;
 	}
 
 	public void setPrimaryCurrency(Currency primaryCurrency) {
 		this.primaryCurrency = primaryCurrency;
 	}
 
 	public CurrencyPair getCurrencyPair() {
 		return currencyPair;
 	}
 
 	public void setCurrencyPair(CurrencyPair currencyPair) {
 		this.currencyPair = currencyPair;
 	}
 }
