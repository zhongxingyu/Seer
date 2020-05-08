 package org.minyanmate.minyanmate;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.ContactsContract;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.LoaderManager;
 import android.support.v4.content.CursorLoader;
 import android.support.v4.content.Loader;
 import android.support.v4.widget.CursorAdapter;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ListView;
 import android.widget.Toast;
 
 import org.minyanmate.minyanmate.adapters.RemovableContactListAdapter;
 import org.minyanmate.minyanmate.contentprovider.MinyanMateContentProvider;
 import org.minyanmate.minyanmate.database.MinyanContactsTable;
 
 /**
  * Created by Jeff on 3/14/14.
  */
 
 // TODO this should inherit the fragment used inside MinyanScheduleSettingsActivity
 
 public class ContactsManagerFragment extends Fragment implements
         LoaderManager.LoaderCallbacks<Cursor> {
 
     // Codes for LoaderManager
     public static final int CONTACT_LOADER = 2;
 
     // Code for startActivityForResult
     public final static int PICK_CONTACT = 10;
 
     private ListView contactList;
 
     public ContactsManagerFragment() {
         super();
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
         View rootView = inflater.inflate(
                 R.layout.fragment_manage_contacts, container, false);
 
         // Use this to declare the use of a custom menu, thus using onCreateOptionsMenu
         setHasOptionsMenu(true);
 
         // Initialize the LoaderManager
         getLoaderManager().initLoader(CONTACT_LOADER, null, this);
 
         contactList = (ListView) rootView.findViewById(R.id.minyan_setting_contactsList);
 
         return rootView;
     }
 
 
     @Override
     public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
 
 
         // First inflate the new menu options, and only afterwards inflate the base menu
         // Diagram below, reference http://stackoverflow.com/a/4954800/1993865
         /*
             Menu:    ()
             inflate new: ( new )
             inflate base: ( new, old)
         */
         menuInflater.inflate(R.menu.contacts_more_options, menu);
         super.onCreateOptionsMenu(menu, menuInflater);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
 
         switch (item.getItemId()) {
             case R.id.moreOptsMenu_AddNewContact:
                 pickNewContact();
                 return true;
 
             default:
                 break;
         }
 
         return false;
     }
 
     /**
      * The handler to select a single Contact using Android's contact picker
      */
     public void pickNewContact() {
         Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
         intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
         startActivityForResult(intent, PICK_CONTACT);
     }
 
 
     @Override
     public void onActivityResult(int reqCode, int resultCode, Intent data) {
 
         switch(reqCode) {
             case (PICK_CONTACT):
                 if (resultCode == Activity.RESULT_OK) {
 
                     Uri result = data.getData();
                     saveContactData(result);
 
                 }
                 break;
 
             default:
                 break;
         }
     }
 
     /**
      * Given a Uri for a contact, confirm the associated contact has a phone number,
      * and if so, create a new Activity to select the schedules it should become associated with.
      * @param data
      */
     private void saveContactData(Uri data) {
         String phoneNumberId = data.getLastPathSegment();
 
         // FIXME this isn't catching existing contacts
         // Check whether the contact is already entered into the table, if so, ignore the selection
         Cursor temp1 = getActivity().getContentResolver().query(
                 MinyanMateContentProvider.CONTENT_URI_CONTACTS, null,
                 MinyanContactsTable.COLUMN_PHONE_NUMBER_ID + "=?",
                 new String[] { phoneNumberId }, null);
 
         if (temp1.getCount() > 0) {
             Toast.makeText(getActivity(), "Contact already exists!", Toast.LENGTH_SHORT).show();
             temp1.close();
             return;
         }
         temp1.close();
 
         // Otherwise if the contact has a phone number, begin an activity to select options for the contact
         Cursor temp2 = getActivity().getContentResolver().query(
                 ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                 ContactsContract.CommonDataKinds.Phone._ID + "=?",
                 new String[] { phoneNumberId }, null);
 
         if (temp2.moveToFirst()) {
             if ( temp2.getString(temp2.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)) != null)
             {
 
                 // Begin activity
                 Intent intent = new Intent(getActivity(), ContactManagerActivity.class);
                intent.putExtra(ContactManagerActivity.PHONE_ID, Integer.parseInt(phoneNumberId));
                 getActivity().startActivity(intent);
 
             } else // this code doesn't appear to be reachable, either a phone uri exists or it doesn't
                 Toast.makeText(getActivity(), "Contact has no phone number! Not added!", Toast.LENGTH_SHORT).show();
         } else {
             Toast.makeText(getActivity(), "Failed to save contact!", Toast.LENGTH_SHORT).show();
         }
         // is this reached?
         temp2.close();
     }
 
 
     @Override
     public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
 
         switch (id) {
             case CONTACT_LOADER:
 
                 return new CursorLoader(getActivity(),
                     MinyanMateContentProvider.CONTENT_URI_CONTACTS,
                         new String[] { MinyanContactsTable.COLUMN_MINYAN_SCHEDULE_ID,
                                 MinyanContactsTable.COLUMN_PHONE_NUMBER_ID},
                     null, null, null);
         }
 
         return null;
     }
 
     @Override
     public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
 
         switch(cursorLoader.getId()) {
 
             case CONTACT_LOADER:
 
                 CursorAdapter adapter = new RemovableContactListAdapter(getActivity(), cursor,
                         new RemovableContactListAdapter.IndistinctContactCallbacks());
                 contactList.setAdapter(adapter);
                 break;
 
             default:
 
                 break;
         }
 
     }
 
     @Override
     public void onLoaderReset(Loader<Cursor> cursorLoader) {
 
     }
 }
