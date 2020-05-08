 package se.flashcards;
 
 import android.annotation.TargetApi;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.WindowManager.LayoutParams;
 import android.widget.EditText;
 import android.widget.TextView;
 
 import com.actionbarsherlock.app.SherlockDialogFragment;
 
 public class WriteTextDialogFragment extends SherlockDialogFragment {
 	
 	//containing fragments or activities must implement this
 	public interface OnTextMadeListener {
 		public void onTextMade(String tag, CharSequence text);
 	}
 	
 	public static WriteTextDialogFragment newInstance(String title, String hint, String text, String positiveButton, String negativeButton) {
 		WriteTextDialogFragment fragment = new WriteTextDialogFragment();
 		Bundle args = new Bundle();
 		args.putString("title", title);
 		args.putString("hint", hint);
 		args.putString("text", text);
 		args.putString("pos", positiveButton);
 		args.putString("neg", negativeButton);
 		fragment.setArguments(args);
 		return fragment;
 		
 	}
 	
	//.setIconAttribute API 11+ (?)
	@TargetApi(11)
 	@Override
     public Dialog onCreateDialog(Bundle savedInstanceState) {
 		String title = getArguments().getString("title");
 		String hint = getArguments().getString("hint");
 		String text = getArguments().getString("text");
 		String posButton = getArguments().getString("pos");
 		String negButton = getArguments().getString("neg");
 		
         LayoutInflater factory = LayoutInflater.from(getActivity());
         final View textEntryView = factory.inflate(R.layout.make_new_dialog, null);
         final EditText textView = (EditText)textEntryView.findViewById(R.id.text);
         textView.setHint(hint);
         textView.requestFocus();
         textView.setText(text);
         textView.setSelection(textView.getText().length());
         Dialog dialog = new AlertDialog.Builder(getActivity())
       //      .setIconAttribute(android.R.attr.alertDialogIcon)
             .setTitle(title)
             .setView(textEntryView)
             .setPositiveButton(posButton, new DialogInterface.OnClickListener() {
             	@Override
                 public void onClick(DialogInterface dialog, int whichButton) {
                 	Fragment target = getTargetFragment();
                 	if (target != null) {
                 		((OnTextMadeListener)target).onTextMade(getTag(), textView.getText());
                 	} else {
                 		((OnTextMadeListener)getActivity()).onTextMade(getTag(), textView.getText());
                 	}
                 }
             })
             .setNegativeButton(negButton, new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {
                 	textView.setText("");
                 }
             })
             .create();
         dialog.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
         return dialog;
     }
 }
