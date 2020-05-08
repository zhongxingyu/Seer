 package se.chalmers.touchdeck.gui.dialogs;
 
 import java.util.Observable;
 import java.util.Observer;
 
 import android.R.string;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.util.Log;
 import android.widget.EditText;
 
 /**
  * A dialog shown to the user that lets it choose a name when creating a new pile
  * 
  * @author group17
  */
 public class PileNameDialog extends Observable {
 	private EditText			input;
 	private final DialogText	dt;
 	private final String		msg;
 	private final String		defaultName;
 
 	/**
 	 * Creates a new Dialog object
 	 * 
 	 * @param o The object that will listen to the input
 	 * @param id The id of the button that was pressed
 	 * @param msg The message that will be shown to the user
 	 */
 	public PileNameDialog(Observer o, int id, String msg, String defaultName) {
 		dt = new DialogText(o, id);
 		this.msg = msg;
 		this.defaultName = defaultName;
 	}
 
 	/**
 	 * Shows the dialog in the specified activity. Prompts the user to enter an name for the pile to be created. If no
 	 * name is entered, a default name is given
 	 * 
 	 * @param act The activity to show the dialog in
 	 */
 	public void show(Activity act) {
 		// A text input for the user to enter the name in
 		input = new EditText(act);
 		AlertDialog.Builder alert = new AlertDialog.Builder(act);
 
 		alert.setTitle("Create pile");
 		alert.setMessage(msg);
 
 		// Set an EditText view to get user input
 		alert.setView(input);
 		// What to do if the ok-button is pressed
 		alert.setPositiveButton(string.ok, new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int id) {
 				// OK
 				// Check if a name was entered
				if (input.getText().toString().trim().equals("")) {
 					// Set the name to a unique default value
 					dt.setText(defaultName);
 					Log.d("dialog", "Name is (default) " + defaultName);
 				} else {
 					// Set the name to the entered value
 					dt.setText(input.getText().toString());
 					Log.d("dialog", "Name is " + input.getText().toString());
 				}
 
 			}
 		});
 		// What to do if the cancel-button is pressed
 		alert.setNegativeButton(string.cancel, new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int id) {
 				// Cancelled
 				Log.d("dialog", "You cancelled!");
 			}
 		});
 		// Show the dialog
 		alert.show();
 
 	}
 
 }
