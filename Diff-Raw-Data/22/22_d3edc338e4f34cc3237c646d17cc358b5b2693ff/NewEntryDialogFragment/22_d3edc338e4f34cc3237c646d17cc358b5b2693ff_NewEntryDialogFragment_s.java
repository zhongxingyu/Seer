 package net.analogyc.wordiary;
 
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.view.LayoutInflater;
 
 /**
  * Allows creating a new entry
  */
 public class NewEntryDialogFragment extends DialogFragment {
 
 	public interface NewEntryDialogListener {
 		public void onDialogPositiveClick(DialogFragment dialog);
 	}
 
 	protected NewEntryDialogListener mListener;
 
 	@Override
 	public void onAttach(Activity activity) {
 		super.onAttach(activity);
 		// Verify that the host activity implements the callback interface
 		try {
 			// Instantiate the NoticeDialogListener so we can send events to the host
 			mListener = (NewEntryDialogListener) activity;
 		} catch (ClassCastException e) {
 			// The activity doesn't implement the interface, throw exception
 			throw new ClassCastException(activity.toString() + " must implement NewEntryDialogListener");
 		}
 	}
 
 	@Override
 	public Dialog onCreateDialog(Bundle savedInstanceState) {
 		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 		// Get the layout inflater
 		LayoutInflater inflater = getActivity().getLayoutInflater();
 
 		// Inflate and set the layout for the dialog
 		// Pass null as the parent view because its going in the dialog layout
		builder.setView(inflater.inflate(R.layout.dialog_custom, null))
 				.setTitle(R.string.dialog_new_entry)
 				// Add action buttons
 				.setPositiveButton(R.string.go, new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int id) {
 						mListener.onDialogPositiveClick(NewEntryDialogFragment.this);
 					}
 				})
 				.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						NewEntryDialogFragment.this.getDialog().cancel();
 					}
 				});
 
		return builder.create();
	}
 
 }
