 package cmput301f13t10;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import cs.ualberta.cmput301f13t10.R;
 
 /**
  * A dialog box that allows the user to enter a title for a new adventure.
  * TODO Change Adventure to add the start section to the list of sections.
  * TODO Change Section to not allow the renaming of the start section.
  * TODO Implement the click listeners in the EditLibraryView. Cancel listener does nothing. Accept listener creates a new Adventure with provided title.
  * TODO Maybe an error for a non-titled new adventure. Otherwise name it Untitled but this could get confusing.
  * TODO Still need to find out where to store the new adventure. Should I implement cache?
  * TODO Should the adventures have an author or date created?
  * @author Braeden Soetaert
  * 
  */
 public class DeleteSectionDialogFragment extends DialogFragment
 {
 
 	// Use this instance of the interface to deliver action events
 	DeleteSectionDialogListener mListener;
 
 	/*
 	 * The activity that creates an instance of this dialog fragment must
 	 * implement this interface in order to receive event callbacks. Each method
 	 * passes the DialogFragment in case the host needs to query it.
 	 */
 	public interface DeleteSectionDialogListener
 	{
 		public void onDeleteConfirm( DialogFragment dialog );
 
 		public void onDeleteCancel( DialogFragment dialog );
 	}
 
 	// Override the Fragment.onAttach() method to instantiate the
 	// NoticeDialogListener
 	@Override
 	public void onAttach( Activity activity )
 	{
 		super.onAttach( activity );
 		// Verify that the host activity implements the callback interface
 		try
 		{
 			// Instantiate the NoticeDialogListener so we can send events to the
 			// host
 			mListener = (DeleteSectionDialogListener) activity;
 		}
 		catch( ClassCastException e )
 		{
 			// The activity doesn't implement the interface, throw exception
 			throw new ClassCastException( activity.toString() + " must implement NoticeDialogListener" );
 		}
 	}
 
 	@Override
 	public Dialog onCreateDialog( Bundle savedInstanceState )
 	{
 		// Build the dialog and set up the button click handlers
 		AlertDialog.Builder builder = new AlertDialog.Builder( getActivity() );
		String sectionTitle = getArguments().getString( AppConstants.SECTION_TITLE );
 		String titleString = "Section: " + sectionTitle;
 		builder.setTitle( titleString ).setMessage( R.string.delete_section_prompt )
 				.setPositiveButton( R.string.delete_section_accept, new DialogInterface.OnClickListener()
 				{
 					public void onClick( DialogInterface dialog, int id )
 					{
 						// Send the positive button event back to the host
 						// activity
 						mListener.onDeleteConfirm( DeleteSectionDialogFragment.this );
 					}
 				} ).setNegativeButton( R.string.delete_section_cancel, new DialogInterface.OnClickListener()
 				{
 					public void onClick( DialogInterface dialog, int id )
 					{
 						// Send the negative button event back to the host
 						// activity
 						mListener.onDeleteCancel( DeleteSectionDialogFragment.this );
 					}
 				} );
 		return builder.create();
 	}
 }
