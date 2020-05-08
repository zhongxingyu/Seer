 package com.radicaldynamic.groupinform.activities;
 
 import java.util.Iterator;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.app.AlertDialog.Builder;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.InputFilter;
 import android.text.Spanned;
 import android.text.method.QwertyKeyListener;
 import android.text.method.TextKeyListener;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnFocusChangeListener;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.radicaldynamic.groupinform.R;
 import com.radicaldynamic.groupinform.adapters.FormBuilderSelectItemListAdapter;
 import com.radicaldynamic.groupinform.application.Collect;
 import com.radicaldynamic.groupinform.views.TouchListView;
 import com.radicaldynamic.groupinform.xform.Field;
 
 public class FormBuilderSelectItemList extends ListActivity
 {
     @SuppressWarnings("unused")
     private static final String t = "FormBuilderSelectItemList: ";
     
     private static final int MENU_ADD = Menu.FIRST;
     
     private static final int DIALOG_EDIT_ITEM = 1;
 
     private static final int REQUEST_TRANSLATIONS = 1;
     
     public static final String KEY_SINGLE = "singleselect";    
     public static final String KEY_DEFAULT = "instancedefault";
 
     private FormBuilderSelectItemListAdapter mAdapter;
     private Builder mAlertDialog;
     private View mAlertDialogView;
     private Field mItem;                                    // Currently selected item to be added/edited        
     private boolean mSingleSelect;                          // Whether this is a single select field (see KEY_SINGLE)
     
     private TouchListView.DropListener onDrop = new TouchListView.DropListener() {
         @Override
         public void drop(int from, int to)
         {
             Field item = mAdapter.getItem(from);
 
             mAdapter.remove(item);
             mAdapter.insert(item, to);
         }
     };
 
     private TouchListView.RemoveListener onRemove = new TouchListView.RemoveListener() {
         @Override
         public void remove(int which)
         {
             final Field item = mAdapter.getItem(which);
 
             mAlertDialog = new AlertDialog.Builder(FormBuilderSelectItemList.this);
             mAlertDialog.setCancelable(false);
             mAlertDialog.setIcon(R.drawable.ic_dialog_alert);
             mAlertDialog.setTitle(R.string.tf_confirm_removal);
             mAlertDialog.setMessage(getString(R.string.tf_confirm_removal_msg, item.getLabel().toString()));
             
             mAlertDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {                                              
                     mAdapter.remove(item);                        
 
                     Toast.makeText(
                             getApplicationContext(), 
                             getString(R.string.tf_removed_with_param, item.getLabel().toString()), 
                             Toast.LENGTH_SHORT).show();
 
                     // Trigger a refresh of the view (and display any pertenent messages)
                     if (mAdapter.isEmpty())
                         refreshView();
                 }
             });
             
             mAlertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {
                 }
             });
 
             mAlertDialog.show();
         }
     };
     
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data)
     {
         if (resultCode == RESULT_CANCELED)
             return;
         
         switch (requestCode) {
         case REQUEST_TRANSLATIONS:
             // Reflect any changes to the label translation in the dialog
             EditText label = (EditText) mAlertDialogView.findViewById(R.id.label); 
             label.setText(mItem.getLabel().toString());
             
             if (mItem.getLabel().isTranslated())
                 toggleEditText(label, false);
             else 
                 toggleEditText(label, true);
             
             break;
         }        
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
 
         setTitle(getString(R.string.app_name) + " > " + getString(R.string.tf_list_items));
         setContentView(R.layout.fb_main);
 
         // Remove things from this view that are not needed
         findViewById(R.id.pathText).setVisibility(View.GONE);
         findViewById(R.id.jumpPreviousButton).setVisibility(View.GONE);        
         
         if (savedInstanceState == null) {
             Intent intent = getIntent();
         
             if (intent != null) {                
                 // Determine if this is a single or multiple select type (see key comments)
                 mSingleSelect = intent.getBooleanExtra(KEY_SINGLE, false);
                 
                 // Set preselected states for list items
                 Iterator<Field> it = Collect.getInstance().getFormBuilderState().getField().getChildren().iterator();
                 
                 while (it.hasNext()) {
                     Field i = it.next();
                     
                     // Default to "off"
                     i.setItemDefault(false);
                     
                     /*
                      * Items must be explicitly enabled/disabled because
                      * the defaults may have changed since last loading
                      */                    
                     for (String def : intent.getStringExtra(KEY_DEFAULT).split("\\s+")) {
                         if (i.getItemValue().equals(def))
                             i.setItemDefault(true);      
                     }
                 }
             }
         } else {           
             mSingleSelect = savedInstanceState.getBoolean(KEY_SINGLE);
             
             Object data = getLastNonConfigurationInstance();
             
             if (data instanceof Field) {
                 mItem = (Field) data;
             }
         }
     }
     
     public Dialog onCreateDialog(int id)
     {
         mAlertDialog = new AlertDialog.Builder(this);
         
         switch (id) {
         case DIALOG_EDIT_ITEM:            
             mAlertDialog.setInverseBackgroundForced(true);
             mAlertDialog.setCancelable(false);
             
             // Attach the layout to this mAlertDialog    
             LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
             mAlertDialogView = inflater.inflate(R.layout.fb_item_dialog, null);
             
             final EditText label = (EditText) mAlertDialogView.findViewById(R.id.label);
             final Button labelI18n = (Button) mAlertDialogView.findViewById(R.id.labelI18n);
             final EditText value = (EditText) mAlertDialogView.findViewById(R.id.value);
             final CheckBox preselected = (CheckBox) mAlertDialogView.findViewById(R.id.preselected);
             
             // Labels default to upper case characters at the beginning
             label.setKeyListener(new QwertyKeyListener(TextKeyListener.Capitalize.SENTENCES, false));
             
             // Access translations for label & hints
             labelI18n.setOnClickListener(new OnClickListener() {
                 public void onClick(View v) {
                     // For use by translation activity
                     Collect.getInstance().getFormBuilderState().setItem(mItem); 
                     
                     // Forward to translation screen
                     Intent i = new Intent(FormBuilderSelectItemList.this, FormBuilderI18nList.class);
                     i.putExtra(FormBuilderI18nList.KEY_FIELDTEXT_TYPE, FormBuilderI18nList.KEY_ITEM_LABEL);
                     i.putExtra(FormBuilderI18nList.KEY_TRANSLATION_ID, mItem.getLabel().getRef());
                     startActivityForResult(i, REQUEST_TRANSLATIONS);
                 }
             });
             
             // Prevent spaces from being entered into the value field        
             value.setFilters(new InputFilter[] { new NoSpaces() });
             
             /*
              * If the user moves to input a value and one is not already provided then
              * attempt to prepopulate this field by using the label as a source and
              * removing all spaces & other special characters.
              */
             value.setOnFocusChangeListener(new OnFocusChangeListener() {
                 @Override
                 public void onFocusChange(View v, boolean hasFocus)
                 {
                     if (hasFocus == false)
                         return;
                     
                     if (value.getText().toString().length() == 0 && label.getText().toString().length() > 0)
                         value.setText(label.getText().toString().replaceAll("[^a-zA-Z0-9]", "").toLowerCase());
                 }
            });
             
             if (mItem.isEmpty()) {
                 mAlertDialog.setTitle(getText(R.string.tf_create_list_item));            
             } else {
                 mAlertDialog.setTitle(getText(R.string.tf_edit_list_item));
                 
                 label.setText(mItem.getLabel().toString());
                 value.setText(mItem.getItemValue());
                 
                 // Translated labels should not be accessible
                 if (mItem.getLabel().isTranslated())
                     toggleEditText(label, false);
                 else 
                     toggleEditText(label, true);
                 
                 if (mItem.isItemDefault())
                     preselected.setChecked(true);
             }
 
             mAlertDialog.setView(mAlertDialogView);
 
             mAlertDialog.setPositiveButton(getText(R.string.ok), new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface mAlertDialog, int whichButton) {
                     boolean error = false;
                     
                     String labelText = label.getText().toString().trim();
                     String valueText = value.getText().toString().trim();
 
                     // Both label and value are required
                     if ((!mItem.getLabel().isTranslated() && labelText.length() == 0) || valueText.length() == 0) {
                         Toast.makeText(getApplicationContext(), getString(R.string.tf_item_missing_label_or_value), Toast.LENGTH_LONG).show();
                         
                         // Preserve user input
                         if (!mItem.getLabel().isTranslated())
                             mItem.setLabel(labelText);
                         
                         mItem.setItemValue(valueText);
                         mItem.setItemDefault(preselected.isChecked());
                         
                         error = true;
                     }
                     
                     // Ensure uniqueness of value
                     Iterator<Field> it = Collect.getInstance().getFormBuilderState().getField().getChildren().iterator();
                     
                     // User input not preserved here (doing so makes the routine much more complex)
                     while (it.hasNext()) {
                         Field i = it.next();
                         
                         if (i.getItemValue().equals(value.getText().toString()) && !i.getLabel().toString().equals(mItem.getLabel().toString())) {
                             Toast.makeText(getApplicationContext(), getString(R.string.tf_item_value_not_unique), Toast.LENGTH_LONG).show();                        
                             error = true;
                         }
                     }
                     
                     if (error) {
                         removeDialog(DIALOG_EDIT_ITEM);
                         showDialog(DIALOG_EDIT_ITEM);
                     } else {
                         if (!mItem.getLabel().isTranslated())
                             mItem.setLabel(labelText);
                         
                         mItem.setItemValue(valueText);
                         
                         // Special handling for the preselected checkbox (this is sensitive to the select type)
                         if (preselected.isChecked()) {
                             // Ensure that no other item is selected
                             if (mSingleSelect) {
                                 Iterator<Field> itemIterator = Collect.getInstance().getFormBuilderState().getField().getChildren().iterator();
                                 
                                 while (itemIterator.hasNext())
                                     itemIterator.next().setItemDefault(false);
                             }                       
                             
                             mItem.setItemDefault(true);
                         } else {
                             mItem.setItemDefault(false);
                         }
                         
                         // Add new items to the list
                         if (mItem.isEmpty()) {
                             mItem.setEmpty(false);
                             Collect.getInstance().getFormBuilderState().getField().getChildren().add(mItem);
                         }
                         
                        removeDialog(DIALOG_EDIT_ITEM);
                        
                         refreshView();
                     }
                 }
             });
            
            mAlertDialog.setNegativeButton(getText(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    removeDialog(DIALOG_EDIT_ITEM);                    
                }
            });
 
             break;
         }
         
         return mAlertDialog.show();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu)
     {
         super.onCreateOptionsMenu(menu);
         menu.add(0, MENU_ADD, 0, getString(R.string.tf_create_list_item)).setIcon(R.drawable.ic_menu_add);
         return true;
     }
     
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event)
     {
         switch (keyCode) {
         case KeyEvent.KEYCODE_BACK:
             setResult(RESULT_OK);
             finish();
             return true;
         }
 
         return super.onKeyDown(keyCode, event);
     }
     
     @Override
     protected void onListItemClick(ListView listView, View view, int position, long id)
     {
         mItem = (Field) getListAdapter().getItem(position);
         showDialog(DIALOG_EDIT_ITEM);
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item)
     {
         switch (item.getItemId()) {
         case MENU_ADD:
             mItem = new Field();
             mItem.setType("item");            
             showDialog(DIALOG_EDIT_ITEM);
             return true;
         }
         
         return super.onOptionsItemSelected(item);
     }    
     
     @Override
     public void onPause()
     {
         super.onPause();
         // TODO?
     }
     
     @Override
     protected void onResume()
     {
         super.onResume();
         refreshView();
     }    
 
     @Override
     public Object onRetainNonConfigurationInstance() 
     {
         return mItem;
     }
     
     @Override
     protected void onSaveInstanceState(Bundle outState)
     {
         super.onSaveInstanceState(outState);
         outState.putBoolean(KEY_SINGLE, mSingleSelect);
     }
     
     private static class NoSpaces implements InputFilter  
     {
         @Override
         public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend)
         {
             return source.subSequence(start, end).toString().replaceAll("\\s+", "");
         }        
     }
 
     private void toggleEditText(EditText v, Boolean b)
     {
         v.setEnabled(b);                    
         v.setFocusable(b);
         v.setFocusableInTouchMode(b);
     }
     
     /* 
      * Refresh the view (displaying the currently active field 
      * or the top level of the form if no field is currently active)
      */
     private void refreshView()
     {
         mAdapter = new FormBuilderSelectItemListAdapter(getApplicationContext(), Collect.getInstance().getFormBuilderState().getField().getChildren());        
         
         // Provide a hint to users if the list is empty
         if (mAdapter.isEmpty()) {
             TextView nothingToDisplay = (TextView) findViewById(R.id.nothingToDisplay);
             nothingToDisplay.setVisibility(View.VISIBLE);
         } else {
             TextView nothingToDisplay = (TextView) findViewById(R.id.nothingToDisplay);
             nothingToDisplay.setVisibility(View.INVISIBLE);
         }
         
         setListAdapter(mAdapter);
 
         TouchListView tlv = (TouchListView) getListView();
 
         tlv.setDropListener(onDrop);
         tlv.setRemoveListener(onRemove);
     }
 }
