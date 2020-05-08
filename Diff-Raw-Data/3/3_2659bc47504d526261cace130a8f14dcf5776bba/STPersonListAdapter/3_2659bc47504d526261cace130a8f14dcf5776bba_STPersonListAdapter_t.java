 package com.scarredions.stab;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.graphics.Bitmap;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AutoCompleteTextView;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.SimpleCursorAdapter;
 import android.widget.TextView;
 
 /**
  * ListAdapter for the person list gallery.
  * @author ianyh
  *
  */
 public class STPersonListAdapter extends BaseAdapter implements DialogInterface.OnClickListener {
 
     private Context context;
     private STMenuListAdapter menuListAdapter;
     
     private STDataController dataController;
     private SimpleCursorAdapter contactsAutoCompleteAdapter;
     
     private AlertDialog addPersonDialog;
     
     public STPersonListAdapter(Context context, STDataController dataController) {
         this.context = context;
         this.dataController = dataController;
     }
     
     /**
      * Adds a person not taken from contacts
      * @param name
      */
     public void add(String name) {
         add(name, STConstants.PERSON_NULL_ID);
     }
     
     /**
      * Adds a person with name and id from the contacts table.
      * @param name
      * @param id
      */
     public void add(String name, String id) {
         dataController.addPerson(name, id);
         menuListAdapter.updateMenuListFooter();
     }
     
     /**
      * Opens an AlertDialog for adding a person to the list.
      * 
      * The dialog itself is saved in internal state for testing purposes.
      */
     public void addPersonByDialog() {
         LayoutInflater factory = LayoutInflater.from(context);
         LinearLayout dialogLayout = (LinearLayout) factory.inflate(R.layout.dialog_person_entry, null);
         AutoCompleteTextView textEntryView = (AutoCompleteTextView) dialogLayout.findViewById(R.id.name_edit);
         textEntryView.setAdapter(contactsAutoCompleteAdapter);
         
         addPersonDialog = new AlertDialog.Builder(context)
             .setView(dialogLayout)
             .setPositiveButton("OK", this)
             .setNegativeButton("Cancel", this)
             .create();
         
         addPersonDialog.show();
     }
     
     /**
      * 
      * @return null if there is no open dialog, otherwise returns the dialog
      */
     public AlertDialog getAddPersonDialog() {
         return addPersonDialog;
     }
     
     public int getCount() {
         return dataController.getPersonCount();
     }
 
     public STDataController getDataController() {
         return dataController;
     }
 
     public Object getItem(int position) {
         return dataController.getPersonName(position);
     }
     
     public long getItemId(int position) {
         return position;
     }
         
     public STMenuListAdapter getMenuListAdapter() {
         return menuListAdapter;
     }
     
     /**
      * Inflates from contact.xml
      */
     public View getView(int position, View convertView, ViewGroup parent) {
         if (convertView == null) {
             LayoutInflater factory = LayoutInflater.from(context);
             LinearLayout layout = (LinearLayout) factory.inflate(R.layout.contact, null);
             ImageView contactPhotoView = (ImageView) layout.findViewById(R.id.contact_image);
             TextView contactNameView = (TextView) layout.findViewById(R.id.contact_name);
             
             Bitmap contactPhoto = dataController.getPersonPhoto(position);
             if (contactPhoto != null) {
                 contactPhotoView.setImageBitmap(contactPhoto);
             }
             contactNameView.setText(dataController.getPersonName(position));
             return layout;
         } else {
             return convertView;
         }
     }
     
     /**
      * Click handler for add person dialog.
      * 
      * Fails silently if the name is empty.
      */
     public void onClick(DialogInterface dialog, int whichButton) {
         if (whichButton == DialogInterface.BUTTON_POSITIVE) {
             AlertDialog d = (AlertDialog) dialog;
             AutoCompleteTextView nameView = (AutoCompleteTextView) d.findViewById(R.id.name_edit);
             String name = nameView.getText().toString();
             if (name.equals("")) {
                 addPersonDialog = null;
                 return;
             }
             String contactId = dataController.getAndClearAutoCompletedContactId();
            if (contactId == null) {
                contactId = STConstants.PERSON_NULL_ID;
            }
             add(name, contactId);
             this.notifyDataSetChanged();
         }
         
         addPersonDialog = null;
     }
     
     public void setContactsAutoCompleteAdapter(SimpleCursorAdapter adapter) {
         contactsAutoCompleteAdapter = adapter;
     }
         
     public void setMenuListAdapter(STMenuListAdapter mla) {
         this.menuListAdapter = mla;
     }
     
 }
