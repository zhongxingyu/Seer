 package pl.orellana.doto;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.widget.Toast;
 
 public class LongPressDialog extends DialogFragment {
 
 	public LongPressDialog() {
 
 	}
 
 	@Override
 	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Bundle bu = getArguments();
		final int p = bu.getInt("position", -1);
 		AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
 		b.setTitle("Actions...");
 		b.setItems(new String[] { "Edit", "Delete" },
 				new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						switch (which) {
 						case 0: // Edit
 							((MainActivity) getActivity()).editOne(p);
 							break;
 						case 1: // Delete
 							((MainActivity) getActivity()).deleteOne(p);
 							break;
 						default:
 							Toast.makeText(getActivity(), "Unimplemented",
 									Toast.LENGTH_LONG).show();
 
 						}
 
 					}
 				});
 
 		return b.create();
 	}
 }
