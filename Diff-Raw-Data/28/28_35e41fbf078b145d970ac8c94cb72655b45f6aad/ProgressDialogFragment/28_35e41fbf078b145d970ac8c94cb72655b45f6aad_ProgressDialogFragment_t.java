 package info.izumin.android.easydialogfragment;
 
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.app.ProgressDialog;
 import android.os.Bundle;
 
 public class ProgressDialogFragment extends DialogFragment {
 
 	private static final String KEY_ICON = "key_icon",
 			KEY_TITLE_ID = "key_title_id", KEY_TITLE = "key_title",
 			KEY_MESSAGE = "key_message",
 			KEY_INDETERMINATE = "key_indeterminate",
 			KEY_CANCELABLE = "key_cancelable";
 
 	public Dialog onCreateDialog(Bundle savedInstanceState) {
 		ProgressDialog dialog = new ProgressDialog(getActivity());
		Bundle args = getArguments();
 		
 		if (args.containsKey(KEY_ICON)) dialog.setIcon(args.getInt(KEY_ICON));
 		
 		if (args.containsKey(KEY_TITLE_ID))
 			dialog.setTitle(args.getInt(KEY_TITLE_ID));
 		else if (args.containsKey(KEY_TITLE))
 			dialog.setTitle(args.getString(KEY_TITLE));
 		
 		if (args.containsKey(KEY_MESSAGE))
 			dialog.setMessage(args.getString(KEY_MESSAGE));
 		
 		if (args.containsKey(KEY_INDETERMINATE))
 			dialog.setCancelable(args.getBoolean(KEY_INDETERMINATE));
 
 		if (args.containsKey(KEY_CANCELABLE))
 			dialog.setCancelable(args.getBoolean(KEY_CANCELABLE));
 		
 		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 		return dialog;
 	}
 
 	public static class Builder {
 		private static final int ID_DEFAULT = -1;
 		
 		private int iconId = ID_DEFAULT;
 		
 		private String title, message;
 		private int titleId = ID_DEFAULT;
 		
 		private boolean indeterminate = true, cancelable = true;
 		
 		/**
 		 * Set the resource id of the Drawable to be used in the title.
 		 * @param iconId
 		 * @return This Builder object to allow for chaining of calls to set methods
 		 */
 		public Builder setIcon(int iconId) {
 			this.iconId = iconId;
 			return this;
 		}
 		
 		/**
 		 * Set the title displayed in the Dialog.
 		 * @param title
 		 * @return This Builder object to allow for chaining of calls to set methods
 		 */
 		public Builder setTitle(String title) {
 			this.title = title;
 			return this;
 		}
 		
 		/**
 		 * Set the title using the given resource id.
 		 * @param titleId
 		 * @return This Builder object to allow for chaining of calls to set methods
 		 */
 		public Builder setTitle(int titleId) {
 			this.titleId = titleId;
 			return this;
 		}
 		
 		/**
 		 * Set the message to display.
 		 * @param message
 		 * @return This Builder object to allow for chaining of calls to set methods
 		 */
 		public Builder setMessage(String message) {
 			this.message = message;
 			return this;
 		}
 		
 		/**
 		 * Sets whether the dialog is cancelable or not. Default is true.
 		 * @param cancelable
 		 * @return This Builder object to allow for chaining of calls to set methods
 		 */
 		public Builder setIndeterminate(boolean indeterminate) {
 			this.indeterminate = indeterminate;
 			return this;
 		}
 		
 		/**
 		 * Sets whether the dialog is cancelable or not. Default is true.
 		 * @param cancelable
 		 * @return This Builder object to allow for chaining of calls to set methods
 		 */
 		public Builder setCancelable(boolean cancelable) {
 			this.cancelable = cancelable;
 			return this;
 		}
 		
 		/**
 		 * Creates a AlertDialogFragment with the arguments supplied to this builder.
 		 * @return
 		 */
 		public ProgressDialogFragment create() {
 			ProgressDialogFragment f = new ProgressDialogFragment();
 			
 			
 			Bundle args = new Bundle();
 			if (iconId != ID_DEFAULT) args.putInt(KEY_ICON, iconId);
 			
 			if (titleId != ID_DEFAULT) args.putInt(KEY_TITLE_ID, titleId);
 			else if (title != null) args.putString(KEY_TITLE, title);
 			
 			if (message != null) args.putString(KEY_MESSAGE, message);
 			
 			args.putBoolean(KEY_INDETERMINATE, indeterminate);
 			args.putBoolean(KEY_CANCELABLE, cancelable);
 			f.setArguments(args);
 			return f;
 		}
 	}
 }
