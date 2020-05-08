 package net.hogelab.android.AppDetailsSettings.Dialog;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.hogelab.android.AppDetailsSettings.ListAdapter.LabelColorListAdapter;
 import net.hogelab.android.AppDetailsSettings.R;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.os.Bundle;
 import android.util.Log;
 
 
 //--------------------------------------------------
 // class MainActivity
 
 public class LabelColorDialog extends DialogFragment implements DialogInterface.OnClickListener {
 
 	private static final String TAG = LabelColorDialog.class.getSimpleName();
 
 
 	//--------------------------------------------------
 	// interfaces
 
 	public interface LabelColorDialogOnClickListener {
 		public void onClickLabelColorDialogList(int position);
 	}
 
 
 	//--------------------------------------------------
 	// static functions
 
	public static LabelColorDialog newInstance() {
 		LabelColorDialog dialog = new LabelColorDialog();
 		dialog.setCancelable(true);
 
 		return dialog;
     }
 
 
 	@Override
 	public Dialog onCreateDialog(Bundle savedInstanceState) {
 		final LabelColorDialogOnClickListener flistener = getListener();
 
 		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 		builder.setTitle(R.string.dialog_labelcolor_title);
         builder.setNegativeButton(android.R.string.cancel, null);
         builder.setAdapter(getLabelColorListAdapter(), new OnClickListener() {
 			@Override
 			public void onClick(DialogInterface arg0, int arg1) {
 				if (flistener != null) {
 					flistener.onClickLabelColorDialogList(arg1);
 				}
 			}
 		});
 
 		return builder.create();  
 	}
 
 
 	@Override
 	public void onClick(DialogInterface dialog, int which) {
 		switch(which) {
 
 		case DialogInterface.BUTTON_POSITIVE:
 			break;
 
 		default:
 			break;
 		}
 	}
 
 
 	private LabelColorDialogOnClickListener getListener() {
 		try {
			return (LabelColorDialogOnClickListener)getActivity();
 		} catch (ClassCastException e) {
 			Log.e(TAG, "must implement onClickLabelColorDialog");
 		}
 
 		return null;
 	}
 
 	private LabelColorListAdapter getLabelColorListAdapter() {
 		List<Integer> list = new ArrayList<Integer>();
 
 		int[] entries = getResources().getIntArray(R.array.labelcolor_index_entries);
 		for (int i = 0; i < entries.length; i++) {
 			list.add(entries[i]);
 		}
 
 		return new LabelColorListAdapter(getActivity(), list);
 	}
 }
