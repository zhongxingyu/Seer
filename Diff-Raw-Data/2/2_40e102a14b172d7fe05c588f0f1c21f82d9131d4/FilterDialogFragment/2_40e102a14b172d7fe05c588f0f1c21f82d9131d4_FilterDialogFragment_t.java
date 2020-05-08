 package cz.cvut.localtrade;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.widget.ScrollView;
 
 public class FilterDialogFragment extends DialogFragment {
 
 	@Override
 	public Dialog onCreateDialog(Bundle savedInstanceState) {
 		LayoutInflater inflater = getActivity().getLayoutInflater();
 
 		ScrollView v = new ScrollView(getActivity());
 		v.setPadding(15, 15, 15, 15);
 		v.addView(inflater.inflate(R.layout.filter_items_dialog, null));
 
 		// Use the Builder class for convenient dialog construction
 		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 		builder.setView(v);
 		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
 
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 
 			}
 		});
		builder.setNegativeButton(R.string.cancel,
 				new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 
 					}
 				});
 		return builder.create();
 	}
 
 }
