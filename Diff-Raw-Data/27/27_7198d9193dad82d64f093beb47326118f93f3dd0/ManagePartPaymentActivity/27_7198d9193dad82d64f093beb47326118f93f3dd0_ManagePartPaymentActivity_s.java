 package com.emiexpert;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.RadioButton;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.emiexpert.PartPayment.PartPaymentType;
 
 public class ManagePartPaymentActivity extends Activity {
 
 	TextView tvCurInterest, tvNewInterest, tvSaving;
 	ListView lv;
 	long currentInterest;
 	Loan adjustedLoan;
 	EMIAdapter adapter;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.manage_partpayment);
 
 		initializeGlobals();
 
 		setLoan();
 
 	}
 
 	public void LoadLists() {
 
 		if (adjustedLoan.getAllEmis().size() > 0) {
 			adapter = new EMIAdapter(getApplicationContext(),
 					adjustedLoan.getAllEmis());
 			lv.setAdapter(adapter);
 			lv.setOnItemClickListener(new OnItemClickListener() {
 
 				@Override
 				public void onItemClick(AdapterView<?> parent, View view,
 						int position, long id) {
 					// TODO Auto-generated method stub
 					displayDialog(position);
 
 				}
 			});
 		}
 	}
 
 	private void setLoan() {
 		// copying object to new object
 		try {
 			adjustedLoan = (Loan) MainActivity.mLoan.clone();
 			// adjustedLoan = MainActivity.mLoan;
 			LoadLists();
 		} catch (CloneNotSupportedException e) {
 			// TODO Auto-generated catch block
 			// e.printStackTrace();
 		}
 	}
 
 	private void initializeGlobals() {
 
 		tvCurInterest = (TextView) findViewById(R.id.tvCurInterest);
 		currentInterest = MainActivity.mLoan.getTotalInterest();
 		tvCurInterest.setText("Rs." + currentInterest);
 		tvNewInterest = (TextView) findViewById(R.id.tvNewInterest);
 		tvSaving = (TextView) findViewById(R.id.tvSavedInterest);
 
 		lv = (ListView) findViewById(R.id.lvMonthlyEMI);
 	}
 
 	public void reset(View v) {
 		setLoan();
 		tvNewInterest.setText(getResources().getString(R.string.rs));
 		tvSaving.setText(getResources().getString(R.string.rssaved));
 	}
 
 	public void showInfo(View v) {
 		Intent intent = new Intent(this, InfoActivity.class);
 		startActivity(intent);
 		finish();
 	}
 	
 	public void manageInterest(View v) {
 
 		if (MainActivity.mLoan != null) {
 
 			Intent myIntent = new Intent(this, ManageInterestActivity.class);
 			startActivity(myIntent);
 			finish();
 		} 
 	}
 	
 	public void gotoHome(View v) {
 		finish();
 	}
 
 	private void displayDialog(final int month) {
 		int curMonth = month + 1;
 		final Dialog partPaymentDialog = new Dialog(
 				ManagePartPaymentActivity.this, R.style.DialogWindowNoTitle);
 
 		partPaymentDialog.setContentView(R.layout.dialog_part_payment);
 		TextView textMonth = (TextView) partPaymentDialog
 				.findViewById(R.id.text_month);
 		final EditText textPartPayment = (EditText) partPaymentDialog
 				.findViewById(R.id.edit_text_part);
		textMonth.setText("Add part payment for Month " + curMonth);
 		Button btnRemove = (Button) partPaymentDialog
 				.findViewById(R.id.btn_remove);
 		Button btnAdd = (Button) partPaymentDialog.findViewById(R.id.btn_add);

 		final RadioButton radioEmi = (RadioButton) partPaymentDialog
 				.findViewById(R.id.radio_emi);
 		// final RadioButton radioDuration = (RadioButton) partPaymentDialog
 		// .findViewById(R.id.radio_duration);
 		radioEmi.setChecked(true);
 
 		btnRemove.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				partPaymentDialog.dismiss();
 
 			}
 		});
 		btnAdd.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 
 				if (textPartPayment.getText().toString().length() > 0) {
 					double amount = Double.parseDouble(textPartPayment
 							.getText().toString());
 					if (radioEmi.isChecked()) {
 						addPartPayment(month, amount, PartPaymentType.LESS_EMI);
 					} else {
 						addPartPayment(month, amount,
 								PartPaymentType.LESS_DURATION);
 					}
 				} else {
 					Toast.makeText(ManagePartPaymentActivity.this,
 							"Add part payment amount", Toast.LENGTH_SHORT)
 							.show();
 				}
 
 				partPaymentDialog.dismiss();
 			}
 		});
 		partPaymentDialog.show();
 
 	}
 
 	private void addPartPayment(int month, double amount, PartPaymentType type) {
 		PartPayment payment = new PartPayment(month, amount, type);
 		adjustedLoan.addPartPayment(payment);
 		tvNewInterest.setText("Rs." + adjustedLoan.getTotalInterest());
 		tvSaving.setText("You Saved Rs." + getSaving());
 		adapter.notifyDataSetChanged();
 	}
 
 	private long getSaving() {
 		long saving = currentInterest - adjustedLoan.getTotalInterest();
 
 		if (saving <= 0) {
 			saving = 0;
 		}
 		return saving;
 
 	}
 }
