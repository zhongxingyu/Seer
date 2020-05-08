 package edu.umich.yourcast;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.EditText;
 
 public class NewGameDialog extends DialogFragment {
 	public final static String RUGBY = "edu.umich.yourcast.rugby";
 	
 	// Use this instance of the interface to deliver action events
 	private NewGameDialogListener mListener;
 
 	private View v;
 
 	public String getMatchInfo() {
 		EditText home_team = (EditText) v.findViewById(R.id.home_team);
 		EditText away_team = (EditText) v.findViewById(R.id.away_team);
 		EditText time = (EditText) v.findViewById(R.id.time);
 		EditText session_pass = (EditText) v.findViewById(R.id.session_pass);
 
 		JSONObject object = new JSONObject();
 		try {
 			object.put("sport", RUGBY); 
 			object.put("home team", home_team.getText().toString());
 			object.put("away team", away_team.getText().toString());
			object.put("session_pass", session_pass.getText().toString());
 			object.put("time", time.getText().toString());
 
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 
 		Log.d("MYMY", "getMatchInfo");
 		return object.toString();
 	}
 
 	// Override the Fragment.onAttach() method to instantiate the
 	// NoticeDialogListener
 	@Override
 	public void onAttach(Activity activity) {
 		super.onAttach(activity);
 		// Verify that the host activity implements the callback interface
 		try {
 			// Instantiate the NoticeDialogListener so we can send events to the
 			// host
 			mListener = (NewGameDialogListener) activity;
 		} catch (ClassCastException e) {
 			// The activity doesn't implement the interface, throw exception
 			throw new ClassCastException(activity.toString()
 					+ " must implement NewGameDialogListener");
 		}
 	}
 
 	@Override
 	public Dialog onCreateDialog(Bundle savedInstanceState) {
 		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 		// Get the layout inflater
 		LayoutInflater inflater = getActivity().getLayoutInflater();
 
 		// Inflate and set the layout for the dialog
 		// Pass null as the parent view because its going in the dialog layout
 		v = inflater.inflate(R.layout.dialog_new_game, null);
 		builder.setView(v)
 			.setTitle(R.string.enter_game_info)
 				// Add action buttons
 				.setPositiveButton(R.string.new_game,
 						new DialogInterface.OnClickListener() {
 							@Override
 							public void onClick(DialogInterface dialog, int id) {
 								mListener
 										.onDialogPositiveClick(NewGameDialog.this);
 							}
 						})
 				.setNegativeButton(R.string.cancel,
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int id) {
 								dialog.dismiss();
 							}
 						});
 		return builder.create();
 	}
 
 	public interface NewGameDialogListener {
 		public void onDialogPositiveClick(NewGameDialog dialog);
 	}
 
 }
