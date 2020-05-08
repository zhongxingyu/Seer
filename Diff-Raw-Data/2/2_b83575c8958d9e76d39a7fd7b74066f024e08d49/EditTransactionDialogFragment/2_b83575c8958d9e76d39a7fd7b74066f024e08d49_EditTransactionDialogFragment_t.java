 package budgetapp.main;
 /**
  * Dialog Fragment for adding a new category
  * 
  */
 import budgetapp.util.BudgetEntry;
 import budgetapp.util.Money;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.support.v4.app.DialogFragment;
 
 public class EditTransactionDialogFragment extends DialogFragment {
 
 	public Dialog onCreateDialog(Bundle savedInstanceState) {
 	    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 	    // Get the layout inflater
 	    LayoutInflater inflater = getActivity().getLayoutInflater();
 	    // Inflate and set the layout for the dialog
 	    final View view = inflater.inflate(R.layout.dialog_edit_transaction, null);
 	    
 	     final BudgetEntry theEntry = getArguments().getParcelable("entry");
 	     builder.setView(view);
 	     TextView date = (TextView)view.findViewById(R.id.dialog_edit_transaction_date_textview);
 	     date.setText(theEntry.getDate());
 
 	     final EditText category = (EditText)view.findViewById(R.id.dialog_edit_transaction_category_edittext);
 	     category.setText(theEntry.getCategory());
 	     
 	     final EditText value = (EditText)view.findViewById(R.id.dialog_edit_transaction_value_edittext);
 	     value.setText(""+theEntry.getValue().get());
 	     
 	     final EditText comment = (EditText)view.findViewById(R.id.dialog_add_comment);
 	     comment.setText(theEntry.getComment());
 	     
 	    // Add action buttons
 	           builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
 	        	   
 	               @Override
 	               public void onClick(DialogInterface dialog, int id) {
 	            	   
 	            	   // See if the new entry is ok
 	            	   long newId;
 	            	   String newDate;
 	            	   String newCategory;
 	            	   double newValue;
 	            	   int newFlags;
 	            	   String newComment;
 	            	   try{
 	            		   newId = theEntry.getId();
 	            		   if(newId<0)
 	            			   throw new Exception();
 	            		   
 	            		   newDate = theEntry.getDate();
 	            		   if(newDate.equalsIgnoreCase(""))
 	            			   throw new Exception();
 	            		   
 	            		   newCategory = category.getText().toString(); 
 	            		   if(newCategory.equalsIgnoreCase(""))
 	            			   throw new Exception();
 	            		   
 	            		   newValue = Double.parseDouble(value.getText().toString());
 	            		   if(newValue==0)
 	            			   throw new Exception();
 	            		   
 	            		   newFlags = theEntry.getFlags();
 	            		   
 	            		   newComment = comment.getText().toString(); // Comment can be empty, no biggie
 	            		   
 	            		   BudgetEntry newEntry = new BudgetEntry(
 		            			   newId, 
 		            			   new Money(newValue), 
 		            			   newDate, 
 		            			   newCategory,
 		            			   newFlags,
 		            			   newComment);
 		            	   
 		            	   MainActivity.datasource.editTransactionEntry(theEntry, newEntry);
 
 		            	   ((StatsActivity)getActivity()).updateEntry(newEntry);
 	            		   Toast.makeText(view.getContext(), "Successfully edited transaction" , Toast.LENGTH_LONG).show();
 		            		
 	            	   }
 	            	   catch(Exception e){
 	            		   e.printStackTrace();
 	            		   Toast.makeText(view.getContext(), "Could not edit transaction" , Toast.LENGTH_LONG).show();
 	            	   }
 	            	   
	            	   ((StatsActivity)getActivity()).updateLog();
 	            	   ((StatsActivity)getActivity()).updateStats();
 	            	   
 	               }
 	           })
 	           .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 	               public void onClick(DialogInterface dialog, int id) {
 	                   EditTransactionDialogFragment.this.getDialog().cancel();
 	               }
 	           });   
 	    return builder.create();
 	}
 }
