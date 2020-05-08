 package com.andreibacalu.android.copied.dialogs;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.View;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.EditText;
 import android.widget.Toast;
 
 import com.andreibacalu.android.copied.R;
 import com.andreibacalu.android.copied.application.CopiedApplication;
 
 public class AddTextDialogFragment extends DialogFragment implements
 		android.content.DialogInterface.OnClickListener {
 
 	public static final int TYPE_CREATE = 0;
 	public static final int TYPE_EDIT = 1;
 	private static final String TAG_LOG = AddTextDialogFragment.class
 			.getSimpleName();
 
 	private final String EXTRA_INPUTED_TEXT = "extra_inputed_text";
 	private final String EXTRA_EDITABLE_TEXT = "extra_editable_text";
 	private final String EXTRA_TYPE_DIALOG = "extra_type_dialog";
 
 	private EditText editText;
 	private int type;
 	private String textToBeEdited;
 	private AddTextDialogFragmentActionResponse listener;
 
 	public AddTextDialogFragment(int type, String textToBeEdited,
 			AddTextDialogFragmentActionResponse listener) {
 		this.type = type;
 		this.textToBeEdited = textToBeEdited;
 		this.listener = listener;
 	}
 
 	public AddTextDialogFragment() {
 	}
 
 	@Override
 	public Dialog onCreateDialog(Bundle savedInstanceState) {
 		editText = new EditText(getActivity());
 		editText.setMaxLines(3);
 		editText.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
 		editText.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				InputMethodManager keyboard = (InputMethodManager) getActivity()
 						.getSystemService(Context.INPUT_METHOD_SERVICE);
 				keyboard.showSoftInput(editText, 0);
 			}
 		}, 200);
 		if (savedInstanceState != null) {
 			textToBeEdited = savedInstanceState.getString(EXTRA_EDITABLE_TEXT,
 					"");
 			type = savedInstanceState.getInt(EXTRA_TYPE_DIALOG);
 			editText.setText(savedInstanceState.getString(EXTRA_INPUTED_TEXT,
 					""));
 		} else if (!TextUtils.isEmpty(textToBeEdited)) {
 			editText.setText(textToBeEdited);
 		}
 		return new AlertDialog.Builder(getActivity())
 				.setTitle(
 						type == TYPE_CREATE ? R.string.add_new_text_title
 								: R.string.edit_text_title)
 				.setPositiveButton(getString(android.R.string.ok), this)
 				.setNegativeButton(getString(android.R.string.cancel), null)
 				.setView(editText).create();
 	}
 
 	@Override
 	public void onClick(DialogInterface dialog, int position) {
 		String textToBeAdded = editText.getText().toString();
 		switch (type) {
 		case TYPE_CREATE:
 			addText(textToBeAdded);
 			break;
 		case TYPE_EDIT:
 			updateText(textToBeAdded);
 			break;
 		}
 		dialog.dismiss();
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle arg0) {
 		arg0.putString(EXTRA_INPUTED_TEXT, editText.getText().toString());
 		arg0.putString(EXTRA_EDITABLE_TEXT, textToBeEdited);
 		arg0.putInt(EXTRA_TYPE_DIALOG, type);
 	}
 
 	private void updateText(String textToBeAdded) {
 		if (textToBeAdded != null && !textToBeAdded.trim().isEmpty()
 				&& !CopiedApplication.clipboarStringsContain(textToBeAdded)) {
 			Log.i(TAG_LOG, "updating text: " + textToBeAdded);
 			CopiedApplication.replaceString(textToBeEdited, textToBeAdded);
 			CopiedApplication.setCurrentSelectedString(textToBeAdded);
 			listener.onTextUpdated();
 		} else if (textToBeAdded != null && !textToBeAdded.trim().isEmpty()) {
 			Log.i(TAG_LOG, "update already exists: " + textToBeAdded);
 			Toast.makeText(getActivity(),
 					getString(R.string.text_already_exists), Toast.LENGTH_LONG)
 					.show();
 		} else {
 			Log.i(TAG_LOG, "update empty");
 			Toast.makeText(getActivity(), getString(R.string.text_empty),
 					Toast.LENGTH_LONG).show();
 		}
 	}
 
 	private void addText(String textToBeAdded) {
 		if (textToBeAdded != null && !textToBeAdded.trim().isEmpty()
 				&& !CopiedApplication.clipboarStringsContain(textToBeAdded)) {
 			Log.i(TAG_LOG, "adding text: " + textToBeAdded);
 			CopiedApplication.addStringToClipboard(textToBeAdded);
 			listener.onTextAdded(textToBeAdded);
 		} else if (textToBeAdded != null && !textToBeAdded.trim().isEmpty()) {
 			Log.i(TAG_LOG, "already exists: " + textToBeAdded);
 			Toast.makeText(getActivity(),
 					getString(R.string.text_already_exists), Toast.LENGTH_LONG)
 					.show();
 		} else {
 			Log.i(TAG_LOG, "empty");
 			Toast.makeText(getActivity(), getString(R.string.text_empty),
 					Toast.LENGTH_LONG).show();
 		}
 	}
 
 	public interface AddTextDialogFragmentActionResponse {
 		void onTextUpdated();
 
 		void onTextAdded(String textAdded);
 	}
 
 	public int getType() {
 		return type;
 	}
 
 	public String getInputedText() {
 		return editText.getText().toString();
 	}
 
 	public void setListener(AddTextDialogFragmentActionResponse listener) {
 		this.listener = listener;
 	}
 }
