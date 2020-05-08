 package nl.q42.hue.dialogs;
 
 import nl.q42.hue2.R;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.app.FragmentManager;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.os.Bundle;
 
 public class ErrorDialog extends DialogFragment {
 	private ErrorDialogCallback callback;
 	
 	public static void showNetworkError(FragmentManager frag) {
 		show(frag, R.string.dialog_network_error_title, R.string.dialog_network_error);
 	}
 	
 	public static void show(FragmentManager frag, int title, int message) {
 		show(frag, title, message, null);
 	}
 	
 	public static void show(FragmentManager frag, int title, int message, ErrorDialogCallback callback) {
 		newInstance(title, message, callback).show(frag, "dialog_error");
 	}
 	
 	public static ErrorDialog newInstance(int title, int message) {
 		return newInstance(title, message, null);
 	}
 	
 	public static ErrorDialog newInstance(int title, int message, ErrorDialogCallback callback) {
 		ErrorDialog dialog = new ErrorDialog();
 		
 		Bundle args = new Bundle();
 		args.putInt("title", title);
 		args.putInt("message", message);
 		dialog.setArguments(args);
 		
 		return dialog;
 	}
 	
 	@Override
 	public Dialog onCreateDialog(Bundle savedInstanceState) {
 		int title = getArguments().getInt("title");
 		int message = getArguments().getInt("message");
 		
 		return new AlertDialog.Builder(getActivity())
 			.setTitle(title)
 			.setMessage(message)
 			.setNegativeButton(R.string.dialog_ok, new OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					dialog.cancel();
 				}
 			})
 			.create();
 	}
 	
 	@Override
 	public void onCancel(DialogInterface dialog) {
 		if (callback != null) callback.onClose();
 	}
 	
 	public void setCallback(ErrorDialogCallback callback) {
 		this.callback = callback;
 	}
 	
 	public interface ErrorDialogCallback {
 		public void onClose();
 	}
 }
