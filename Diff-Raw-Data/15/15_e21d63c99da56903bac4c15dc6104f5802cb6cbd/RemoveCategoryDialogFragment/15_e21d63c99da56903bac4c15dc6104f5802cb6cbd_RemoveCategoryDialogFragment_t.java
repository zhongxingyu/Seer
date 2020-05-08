 package budgetapp.main;
 /**
  * Dialog fragment for removing a category
  */
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class RemoveCategoryDialogFragment extends DialogFragment {
 	
 	public Dialog onCreateDialog(Bundle savedInstanceState) {
 	    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 	    // Get the layout inflater
 	    LayoutInflater inflater = getActivity().getLayoutInflater();
 	    final View view = inflater.inflate(R.layout.dialog_remove_category, null);
 	    // Inflate and set the layout for the dialog
 	    // Pass null as the parent view because its going in the dialog layout
 	    builder.setView(view);
 	  
 	    // Add action buttons
 	           builder.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
 	        	   
 	               @Override
 	               public void onClick(DialogInterface dialog, int id) {
 	            	   EditText category = (EditText)view.findViewById(R.id.dialog_category_name);
 	            	   if(MainActivity.datasource.removeCategory(category.getText().toString())==true)
	            	   {
	            		   ((MainActivity) getActivity()).updateSpinner();
 	            		   Toast.makeText(view.getContext(), "Successfully removed "+ category.getText().toString() , Toast.LENGTH_LONG).show();
	            	   }
 	            	   else
 	            		   Toast.makeText(view.getContext(), "Could not remove "+ category.getText().toString(), Toast.LENGTH_LONG).show();
 	               }
 	           })
 	           .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 	               public void onClick(DialogInterface dialog, int id) {
 	                   RemoveCategoryDialogFragment.this.getDialog().cancel();
 	                   
 	               }
 	           });   
 	          // System.out.println("itdd is_ " + category.getText().toString());
 	    return builder.create();
 	}
 }
