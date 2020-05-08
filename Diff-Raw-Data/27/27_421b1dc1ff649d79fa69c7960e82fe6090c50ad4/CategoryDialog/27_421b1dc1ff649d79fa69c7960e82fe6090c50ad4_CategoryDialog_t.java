 package com.escoand.android.wceu;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.content.DialogInterface;
 import android.os.Bundle;
 
 public class CategoryDialog extends DialogFragment {
 	CategoryDialogListener listener;
 
 	public interface CategoryDialogListener {
 		public void onCategorySelected(String category);
 	}
 
 	@Override
 	public Dialog onCreateDialog(Bundle savedInstanceState) {
 		AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
 
 		adb.setTitle(R.string.menuRegion);
 		adb.setItems(R.array.categories, new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				listener.onCategorySelected(getResources().getStringArray(
 						R.array.categorieValues)[which]);
 			}
 		});
 		adb.setCancelable(true);
 
 		return adb.create();
 	}

	public void setListener(CategoryDialogListener listener) {
		this.listener = listener;
	}
}
