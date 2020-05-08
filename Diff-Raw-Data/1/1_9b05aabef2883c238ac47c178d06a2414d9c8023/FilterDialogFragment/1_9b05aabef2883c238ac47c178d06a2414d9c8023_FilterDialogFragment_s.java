 package pl.wyroslak.salesmanhelper;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.EditText;
 
 public class FilterDialogFragment extends DialogFragment {
 
 	public interface FilterDialogListener {
 		public void onDialogPositiveClick(DialogFragment dialog);

 		public void onDialogNegativeClick(DialogFragment dialog);
 	};
 
 	private FilterDialogListener mListener;
 
 	@Override
 	public void onAttach(Activity activity) {
 		super.onAttach(activity);
 		try {
 			mListener = (FilterDialogListener) activity;
 		} catch (ClassCastException e) {
 			throw new ClassCastException(activity.toString()
 					+ " must implement FilterDialogListener");
 		}
 	}
 
 	@Override
 	public Dialog onCreateDialog(Bundle savedInstanceState) {
 		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 		LayoutInflater inflater = getActivity().getLayoutInflater();
 		String filter = getArguments().getString("filter");
 		View v = inflater.inflate(R.layout.dialog_filter, null);
 		if (!filter.equals("")) {
 			EditText et = (EditText) v.findViewById(R.id.filter_edit_text);
 			et.setText(filter);
 			et.setSelection(filter.length());
 		}
 
 		builder.setView(v)
 				.setPositiveButton(R.string.action_filter,
 						new DialogInterface.OnClickListener() {
 
 							@Override
 							public void onClick(DialogInterface dialog,
 									int which) {
 								mListener
 										.onDialogPositiveClick(FilterDialogFragment.this);
 							}
 						})
 				.setNegativeButton(R.string.action_cancel,
 						new DialogInterface.OnClickListener() {
 
 							@Override
 							public void onClick(DialogInterface dialog,
 									int which) {
 								mListener
 										.onDialogNegativeClick(FilterDialogFragment.this);
 							}
 						});
 		return builder.create();
 	}
 
 }
