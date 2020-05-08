 package com.asa.easysal.ui;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.TextView;
 
 import com.asa.easysal.R;
 
 public class CalculateDialogFragment extends DialogFragment {
 	private double[] results;
 
 	private final static String EXTRA_DOUBLE_VALUES = "values";
 
 	public static CalculateDialogFragment newInstance(int title, int layoutId) {
 		CalculateDialogFragment frag = new CalculateDialogFragment();
 		Bundle args = new Bundle();
 		args.putInt("title", title);
 		args.putInt("layout", layoutId);
 		frag.setArguments(args);
 		return frag;
 	}
 
 	public void setResults(double[] results) {
 		this.results = results;
 	}
 
 	@Override
 	public Dialog onCreateDialog(Bundle savedInstanceState) {
 		if (savedInstanceState != null) {
 			double[] savedArr = savedInstanceState
 					.getDoubleArray(EXTRA_DOUBLE_VALUES);
 			if (savedArr != null) {
 				int length = savedArr.length;
 				results = new double[length];
 				for (int i = 0; i < length; i++) {
 					results[i] = savedArr[i];
 				}
 			}
 		}
 		int title = getArguments().getInt("title");
 		LayoutInflater inflater = (LayoutInflater) getActivity()
 				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		View view = inflater.inflate(R.layout.results, null);
		TextView otText = (TextView) view.findViewById(R.id.results_overtime);
		TextView otTitle = (TextView) view
				.findViewById(R.id.results_overtime_title);
 		// Sets the results
 		((TextView) view.findViewById(R.id.results_hourly)).setText(""
 				+ results[0]);
 		((TextView) view.findViewById(R.id.results_daily)).setText(""
 				+ results[1]);
 		((TextView) view.findViewById(R.id.results_weekly)).setText(""
 				+ results[2]);
 		((TextView) view.findViewById(R.id.results_biweekly)).setText(""
 				+ results[3]);
 		((TextView) view.findViewById(R.id.results_monthly)).setText(""
 				+ results[4]);
 		((TextView) view.findViewById(R.id.results_yearly)).setText(""
 				+ results[5]);
		otText.setText("" + results[5]);
 		if (results.length == 7) {
			if (results[6] > 0) {
				otText.setText("" + results[6]);
			} else {
				otText.setVisibility(View.GONE);
				otTitle.setVisibility(View.GONE);
			}
		} else {
			otText.setVisibility(View.GONE);
			otTitle.setVisibility(View.GONE);
 		}
 
 		// view = Util.setView(list, view);
 		return new AlertDialog.Builder(getActivity())
 				.setIcon(R.drawable.ic_launcher)
 				.setTitle(title)
 				.setPositiveButton(R.string.resultsCalculate,
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog,
 									int whichButton) {
 								dialog.dismiss();
 							}
 						}).setView(view).create();
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		outState.putDoubleArray(EXTRA_DOUBLE_VALUES, results);
 	}
 }
