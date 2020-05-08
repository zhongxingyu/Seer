 package cryptocast.client;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 
 /**
  * This class is used to pop up an error message.
  */
 public class ErrorFragment extends DialogFragment {
     private String message;
 
     /**
      * Creates a new ErrorFragment which can be used to print the given error message.
      * @param message Error message describing the error which occured before this fragment pops up.
      */
     public ErrorFragment(String message) {
         super();
         this.message = message;
     }
 
    /** @return The error message */
    public String getMessage() { return message; }

 //    @Override
 //    public Dialog onCreateDialog(Bundle savedInstanceState) {
 //        // Use the Builder class for convenient dialog construction
 //        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 //        builder.setMessage(message)
 //            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
 //                   public void onClick(DialogInterface dialog, int id) {
 //                       // do sth when ok
 //                   }
 //               });
 //        // Create the AlertDialog object and return it
 //        return builder.create();
 //    }
 }
