 package info.izumin.android.easydialogfragment;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.app.Fragment;
 import android.content.DialogInterface;
 import android.os.Bundle;
 
 public class AlertDialogFragment extends DialogFragment {
 
 	private static final String KEY_ICON = "key_icon",
 			KEY_TITLE_ID = "key_title_id", KEY_TITLE = "key_title",
 			KEY_MESSAGE_ID = "key_message_id", KEY_MESSAGE = "key_message",
 			KEY_NEGATIVE_TEXT_ID = "key_negative_text_id",
 			KEY_NEGATIVE_TEXT = "key_negative_text",
 			KEY_NEUTRAL_TEXT_ID = "key_neutral_text_id",
 			KEY_NEUTRAL_TEXT = "key_neutral_text",
 			KEY_POSITIVE_TEXT_ID = "key_positive_text_id",
 			KEY_POSITIVE_TEXT = "key_positive_text",
 			KEY_CANCELABLE = "key_cancelable";
 	
 	private Callbacks callbacks;
 	
 	@Override
 	public Dialog onCreateDialog(Bundle savedInstanceState) {
 		Fragment targetFragment = getTargetFragment();
 		if (targetFragment != null && targetFragment instanceof Callbacks) {
 			callbacks = (Callbacks) targetFragment;
 		} else if (getActivity() instanceof Callbacks) {
 			callbacks = (Callbacks) getActivity();
 		} else {
 			callbacks = new AlertDialogFragment.Callbacks() {
 				@Override
 				public void onDialogClicked(DialogInterface dialog, int which) {}
 				@Override
				public void onDialogCancelled(DialogInterface dialog) {}
 			};
 		}
 		
 		DialogInterface.OnClickListener listener =
 				new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						callbacks.onDialogClicked(dialog, which);
 					}
 				};
 		
 		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 		Bundle args = new Bundle();
 		
 		if (args.containsKey(KEY_ICON)) builder.setIcon(args.getInt(KEY_ICON));
 		
 		if (args.containsKey(KEY_TITLE_ID))
 			builder.setTitle(args.getInt(KEY_TITLE_ID));
 		else if (args.containsKey(KEY_TITLE))
 			builder.setTitle(args.getString(KEY_TITLE));
 		
 		if (args.containsKey(KEY_MESSAGE_ID))
 			builder.setMessage(args.getInt(KEY_MESSAGE_ID));
 		else if (args.containsKey(KEY_MESSAGE))
 			builder.setMessage(args.getString(KEY_MESSAGE));
 		
 		if (args.containsKey(KEY_NEGATIVE_TEXT_ID))
 			builder.setNegativeButton(args.getInt(KEY_NEGATIVE_TEXT_ID), listener);
 		else if (args.containsKey(KEY_NEGATIVE_TEXT))
 			builder.setNegativeButton(args.getString(KEY_NEGATIVE_TEXT), listener);
 		
 		if (args.containsKey(KEY_NEUTRAL_TEXT_ID))
 			builder.setNeutralButton(args.getInt(KEY_NEUTRAL_TEXT_ID), listener);
 		else if (args.containsKey(KEY_NEGATIVE_TEXT))
 			builder.setNeutralButton(args.getString(KEY_NEUTRAL_TEXT), listener);
 		
 		if (args.containsKey(KEY_POSITIVE_TEXT_ID))
 			builder.setPositiveButton(args.getInt(KEY_POSITIVE_TEXT_ID), listener);
 		else if (args.containsKey(KEY_POSITIVE_TEXT))
 			builder.setPositiveButton(args.getString(KEY_POSITIVE_TEXT), listener);
 		
 		if (args.containsKey(KEY_CANCELABLE))
 			builder.setCancelable(args.getBoolean(KEY_CANCELABLE));
 		
 		return builder.create();
 	}
 	
 	@Override
 	public void onCancel(DialogInterface dialog) {
 		callbacks.onDialogCancelled(dialog);
 	}
 	
 	public static class Builder {
 		
 		private static final int ID_DEFAULT = -1;
 		
 		private int iconId = ID_DEFAULT;
 		
 		private String title, message;
 		private int titleId = ID_DEFAULT, messageId = ID_DEFAULT;
 		
 		private String negativeText, positiveText, neutralText;
 		private int negativeTextId = ID_DEFAULT, positiveTextId = ID_DEFAULT,
 				neutralTextId = ID_DEFAULT;
 		
 		private boolean cancelable = true;
 		
 		private Fragment targetFragment;
 		private int requestCode;
 		
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
 		 * Set the message to display using the given resource id.
 		 * @param messageId
 		 * @return This Builder object to allow for chaining of calls to set methods
 		 */
 		public Builder setMessage(int messageId) {
 			this.messageId = messageId;
 			return this;
 		}
 		
 		/**
 		 * Set the label to display in the negative button
 		 * @param text
 		 * @return This Builder object to allow for chaining of calls to set methods
 		 */
 		public Builder setNegativeButtonLabel(String text) {
 			this.negativeText = text;
 			return this;
 		}
 		
 		/**
 		 * Set the label to display in the negative button using the given resource id.
 		 * @param textId
 		 * @return This Builder object to allow for chaining of calls to set methods
 		 */
 		public Builder setNegativeButtonLabel(int textId) {
 			this.negativeTextId = textId;
 			return this;
 		}
 		
 		/**
 		 * Set the label to display in the neutral button
 		 * @param text
 		 * @return This Builder object to allow for chaining of calls to set methods
 		 */
 		public Builder setNeutralButtonLabel(String text) {
 			this.neutralText = text;
 			return this;
 		}
 		
 		/**
 		 * Set the label to display in the neutral button using the given resource id.
 		 * @param textId
 		 * @return This Builder object to allow for chaining of calls to set methods
 		 */
 		public Builder setNeutralButtonLabel(int textId) {
 			this.neutralTextId = textId;
 			return this;
 		}
 		
 		/**
 		 * Set the label to display in the positive button
 		 * @param text
 		 * @return This Builder object to allow for chaining of calls to set methods
 		 */
 		public Builder setPositiveButtonLabel(String text) {
 			this.positiveText = text;
 			return this;
 		}
 		
 		/**
 		 * Set the label to display in the positive button using the given resource id.
 		 * @param textId
 		 * @return This Builder object to allow for chaining of calls to set methods
 		 */
 		public Builder setPositiveButtonLabel(int textId) {
 			this.positiveTextId = textId;
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
 		 * Optional target for this fragment.
 		 * @param targetFragment
 		 * @param requestCode
 		 * @return This Builder object to allow for chaining of calls to set methods
 		 */
 		public Builder setTargetFragment(Fragment targetFragment, int requestCode) {
 			this.targetFragment = targetFragment;
 			this.requestCode = requestCode;
 			return this;
 		}
 		
 		/**
 		 * Creates a AlertDialogFragment with the arguments supplied to this builder.
 		 * @return
 		 */
 		public AlertDialogFragment create() {
 			AlertDialogFragment f = new AlertDialogFragment();
 			
 			if (targetFragment != null)
 				f.setTargetFragment(targetFragment, requestCode);
 			
 			Bundle args = new Bundle();
 			if (iconId != ID_DEFAULT) args.putInt(KEY_ICON, iconId);
 			
 			if (titleId != ID_DEFAULT) args.putInt(KEY_TITLE_ID, titleId);
 			else if (title != null) args.putString(KEY_TITLE, title);
 			
 			if (messageId != ID_DEFAULT) args.putInt(KEY_MESSAGE_ID, messageId);
 			else if (message != null) args.putString(KEY_MESSAGE, message);
 			
 			if (negativeTextId != ID_DEFAULT)
 				args.putInt(KEY_NEGATIVE_TEXT_ID, negativeTextId);
 			else if (negativeText != null)
 				args.putString(KEY_NEGATIVE_TEXT, negativeText);
 			
 			if (neutralTextId != ID_DEFAULT)
 				args.putInt(KEY_NEUTRAL_TEXT_ID, neutralTextId);
 			else if (neutralText != null)
 				args.putString(KEY_NEUTRAL_TEXT, neutralText);
 			
 			if (positiveTextId != ID_DEFAULT)
 				args.putInt(KEY_POSITIVE_TEXT_ID, positiveTextId);
 			else if (positiveText != null)
 				args.putString(KEY_POSITIVE_TEXT, positiveText);
 			
 			args.putBoolean(KEY_CANCELABLE, cancelable);
 			f.setArguments(args);
 			return f;
 		}
 	}
 
 	public interface Callbacks {
 		void onDialogClicked(DialogInterface dialog, int which);
 		void onDialogCanceled(DialogInterface dialog);
 	}
 }
