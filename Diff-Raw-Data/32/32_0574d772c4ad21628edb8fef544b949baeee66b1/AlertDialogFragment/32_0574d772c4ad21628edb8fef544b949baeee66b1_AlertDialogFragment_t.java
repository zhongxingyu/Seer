 /**
  * 
  */
 package com.varun.android.widgets;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface.OnClickListener;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.util.Log;
 
 import com.varun.android.widgets.dialogfragmentlib.R;
 
 /**
  * @author dev
  *
  */
 public class AlertDialogFragment extends DialogFragment {
 
 	private static final String TAG = AlertDialogFragment.class.getSimpleName();
 	private String title = null;
 	private String message = null;
 	private Drawable icon = null;
 	private OnClickListener positiveOnClickListener = null;
 	private OnClickListener negativeOnClickListener = null;
 	private String positiveButtonText = null;
 	private String negativeButtonText = null;
 	
 	/**
 	 * Constructor
 	 */
 	public AlertDialogFragment() {
 	}	
 	
 	
 
 	@Override
 	public Dialog onCreateDialog(Bundle savedInstanceState) {
 		super.onCreateDialog(savedInstanceState);
 		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 		builder.setIcon(this.icon);
 		builder.setTitle(this.title);
 		builder.setMessage(this.message);
		
		if (this.positiveButtonText == null) {
			Log.i(TAG,"positiveButtonText null. Setting default");
			builder.setPositiveButton(getString(R.string.ok), this.positiveOnClickListener);
		} else {
			Log.i(TAG,"Setting positiveButtonText: " + this.positiveButtonText + " listener: " + this.positiveOnClickListener);
			builder.setPositiveButton(this.positiveButtonText, this.positiveOnClickListener);
 		}
		if (this.negativeButtonText == null) {
			Log.i(TAG,"negativeButtonText null. Setting default");
			builder.setNegativeButton(getString(R.string.cancel), this.negativeOnClickListener);
		} else {
			Log.i(TAG,"Setting negativeButtonText: " + this.negativeButtonText + " listener: " + this.negativeOnClickListener);
 			builder.setNegativeButton(this.negativeButtonText, this.negativeOnClickListener);
 		}
 		return builder.create();
 	}
 	
 	/**
 	 * @return the icon
 	 */
 	public Drawable getIcon() {
 		return icon;
 	}
 
 	/**
 	 * @param icon the icon to set
 	 */
 	public AlertDialogFragment setIcon(Drawable icon) {
 		this.icon = icon;
 		return this;
 	}
 	
 
 	/**
 	 * @return the positionButtonText
 	 */
 	public String getPositiveButtonText() {
 		return positiveButtonText;
 	}
 
 	/**
 	 * @param positiveButtonText the positionButtonText to set
 	 */
 	public AlertDialogFragment setPositiveButtonText(String positiveButtonText) {
 		this.positiveButtonText = positiveButtonText;
 		return this;
 	}
 
 	/**
 	 * @return the negativeButtonText
 	 */
 	public String getNegativeButtonText() {
 		return negativeButtonText;
 	}
 
 	/**
 	 * @param negativeButtonText the negativeButtonText to set
 	 */
 	public AlertDialogFragment setNegativeButtonText(String negativeButtonText) {
 		this.negativeButtonText = negativeButtonText;
 		return this;
 	}
 
 	/**
 	 * @return the positiveOnClickListener
 	 */
 	public OnClickListener getPositiveOnClickListener() {
 		return positiveOnClickListener;
 	}
 
 	/**
	 * @param positiveButtonText the positiveButtonText to set. Set null for default
 	 * @param positiveOnClickListener the positiveOnClickListener to set
 	 */
 	public AlertDialogFragment setPositiveOnClickListener(String positiveButtonText, OnClickListener positiveOnClickListener) {
 		this.positiveOnClickListener = positiveOnClickListener;
 		return this;
 	}
 
 	/**
 	 * @return the negativeOnClickListener
 	 */
 	public OnClickListener getNegativeOnClickListener() {
 		return negativeOnClickListener;
 	}
 
 	/**
	 * @param negativeButtonText the negativeButtonText to set. Set null for default
 	 * @param negativeOnClickListener the negativeOnClickListener to set
 	 */
 	public AlertDialogFragment setNegativeOnClickListener(String negativeButtonText, OnClickListener negativeOnClickListener) {
 		this.negativeOnClickListener = negativeOnClickListener;
 		this.negativeButtonText = negativeButtonText;
 		return this;
 	}
 
 	/**
 	 * @return the title
 	 */
 	public String getTitle() {
 		return title;
 	}
 
 	/**
 	 * @param title the title to set
 	 */
 	public AlertDialogFragment setTitle(String title) {
 		this.title = title;
 		return this;
 	}
 
 	/**
 	 * @return the message
 	 */
 	public String getMessage() {
 		return message;
 	}
 
 	/**
 	 * @param message the message to set
 	 */
 	public AlertDialogFragment setMessage(String message) {
 		this.message = message;
 		return this;
 	}
 	
 	public void dismiss() {
 		getDialog().dismiss();
 	}
 
 }
