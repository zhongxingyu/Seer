 package net.three_headed_monkey.ui;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.*;
 import android.widget.*;
 import android.telephony.PhoneNumberUtils;
 import net.three_headed_monkey.R;
 import com.googlecode.androidannotations.annotations.*;
 import net.three_headed_monkey.ThreeHeadedMonkeyApplication;
 import net.three_headed_monkey.data.PhoneNumberInfo;
 import net.three_headed_monkey.ui.adapter.PhoneNumberInfoListAdapter;
 
 
 @EActivity(R.layout.phonenumbers_settings_activity)
 public class PhoneNumbersSettingsActivity extends Activity {
 
     @ViewById
     TextView text_phonenumber;
 
     @ViewById
     ListView phonenumbers_list;
 
     @Bean
     PhoneNumberInfoListAdapter adapter;
 
     @App
     ThreeHeadedMonkeyApplication application;
 
     @Trace
     @AfterViews
     void bindAdapter(){
         phonenumbers_list.setAdapter(adapter);
         phonenumbers_list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
         phonenumbers_list.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
 
             @Override
             public void onItemCheckedStateChanged(ActionMode mode, int position,
                                                   long id, boolean checked) {
                 // Here you can do something when items are selected/de-selected,
                 // such as update the title in the CAB
             }
 
             @Override
             public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                 // Respond to clicks on the actions in the CAB
                 switch (item.getItemId()) {
                     case R.id.action_delete:
                         long[] positions = phonenumbers_list.getCheckedItemIds();
                        for(int currentPositionIndex = 0; positions.length !=0 && currentPositionIndex < positions.length; currentPositionIndex++) {
                            application.phoneNumberSettings.removePhoneNumber(adapter.getItem((int)positions[currentPositionIndex]));
                         }
                        mode.finish();
                         return true;
                     default:
                         return false;
                 }
             }
 
             @Override
             public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                 // Inflate the menu for the CAB
                 MenuInflater inflater = mode.getMenuInflater();
                 inflater.inflate(R.menu.phonenumbers_settings_activity_context, menu);
                 return true;
             }
 
             @Override
             public void onDestroyActionMode(ActionMode mode) {
                 // Here you can make any necessary updates to the activity when
                 // the CAB is removed. By default, selected items are deselected/unchecked.
             }
 
             @Override
             public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                 // Here you can perform updates to the CAB due to
                 // an invalidate() request
                 return false;
             }
         });    }
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu items for use in the action bar
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.phonenumbers_settings_activity_actions, menu);
         return super.onCreateOptionsMenu(menu);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // Handle presses on the action bar items
         switch (item.getItemId()) {
             case R.id.action_add:
                 Toast.makeText(getApplicationContext(), R.string.phonenumbers_settings_add_dialog_no_all_values_set, Toast.LENGTH_SHORT).show();
                 addNewPhoneNumber();
                 return true;
             case R.id.action_delete:
 
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     private String addNewPhoneNumber() {
         AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
         alertDialogBuilder.setTitle("Add new phonenumber");
         alertDialogBuilder.setCancelable(false);
         final EditText input = new EditText(this);
 
         LinearLayout layout = new LinearLayout(getApplicationContext());
         layout.setOrientation(LinearLayout.VERTICAL);
 
         final EditText inputName = new EditText(getApplicationContext());
         inputName.setHint("Name");
         layout.addView(inputName);
 
         final EditText inputNumber = new EditText(getApplicationContext());
         inputNumber.setHint("Phonenumber");
         layout.addView(inputNumber);
 
         alertDialogBuilder.setView(layout);
         alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id) {
                 String possibleNewPhonenumber = inputNumber.getText().toString();
                 String possibleNewName = inputName.getText().toString();
                 if(possibleNewPhonenumber == null || possibleNewName == null) {
                     Toast.makeText(getApplicationContext(), R.string.phonenumbers_settings_add_dialog_no_all_values_set, Toast.LENGTH_SHORT).show();
                     dialog.cancel();
                 }
                 if(application.phoneNumberSettings.nameExists(possibleNewName)) {
                     Toast.makeText(getApplicationContext(), R.string.phonenumbers_settings_add_dialog_invalid_name, Toast.LENGTH_SHORT).show();
                     dialog.cancel();
                 }
                 if(!PhoneNumberUtils.isGlobalPhoneNumber(possibleNewPhonenumber)) {
                     Toast.makeText(getApplicationContext(), R.string.phonenumbers_settings_add_dialog_invalid_phonenumber, Toast.LENGTH_SHORT).show();
                     dialog.cancel();
                 }
                 else {
                     PhoneNumberInfo phoneNumberInfo = new PhoneNumberInfo(PhoneNumberUtils.formatNumber(possibleNewPhonenumber), possibleNewName);
                     application.phoneNumberSettings.addPhoneNumber(phoneNumberInfo);
                 }
 
 
             }
         })
                 .setNegativeButton("No", new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int id) {
                         dialog.cancel();
                     }
                 });
 
         AlertDialog alertDialog = alertDialogBuilder.create();
 
         alertDialog.show();
         return null;
     }
 
 
 
 }
