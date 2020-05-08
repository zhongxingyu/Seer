 package net.three_headed_monkey.ui;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.view.*;
 import android.widget.*;
 import android.telephony.PhoneNumberUtils;
 import net.three_headed_monkey.R;
 import com.googlecode.androidannotations.annotations.*;
 import net.three_headed_monkey.ThreeHeadedMonkeyApplication;
 import net.three_headed_monkey.data.PhoneNumberInfo;
 import net.three_headed_monkey.ui.adapter.PhoneNumberInfoListAdapter;
 
import java.util.*;

 
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
     void bindAdapter() {
         phonenumbers_list.setAdapter(adapter);
         phonenumbers_list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
         phonenumbers_list.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
 
             @Override
             public void onItemCheckedStateChanged(ActionMode mode, int position,
                                                   long id, boolean checked) {
             }
 
             @Override
             public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                 switch (item.getItemId()) {
                     case R.id.action_delete:
                         long[] positions = phonenumbers_list.getCheckedItemIds();
                        Arrays.sort(positions);
                        for (int currentPositionIndex = positions.length-1; currentPositionIndex >= 0; currentPositionIndex--) {
                             application.phoneNumberSettings.removePhoneNumber(adapter.getItem((int) positions[currentPositionIndex]));
                         }
                         mode.finish();
                         return true;
                     default:
                         return false;
                 }
             }
 
             @Override
             public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                 MenuInflater inflater = mode.getMenuInflater();
                 inflater.inflate(R.menu.phonenumbers_settings_activity_context, menu);
                 return true;
             }
 
             @Override
             public void onDestroyActionMode(ActionMode mode) {
             }
 
             @Override
             public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                 return false;
             }
         });
     }
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.phonenumbers_settings_activity_actions, menu);
         return super.onCreateOptionsMenu(menu);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.action_add:
                 addNewPhoneNumber();
                 return true;
             case R.id.action_delete:
 
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     private String addNewPhoneNumber() {
         final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.phonenumberssettings_add_dialog_title);
         alertDialogBuilder.setCancelable(false);
         final EditText input = new EditText(this);
 
         LinearLayout layout = new LinearLayout(getApplicationContext());
         layout.setOrientation(LinearLayout.VERTICAL);
 
         final EditText inputName = new EditText(getApplicationContext());
        inputName.setHint(R.string.phonenumberssettings_add_dialog_inputname_hint);
         layout.addView(inputName);
 
         final EditText inputNumber = new EditText(getApplicationContext());
        inputNumber.setHint(R.string.phonenumberssettings_add_dialog_inputnumber_hint);
         layout.addView(inputNumber);
 
         alertDialogBuilder.setView(layout);
        alertDialogBuilder.setPositiveButton(R.string.phonenumberssettings_add_dialog_yes, new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int id) {
 
             }
         })
                .setNegativeButton(R.string.phonenumberssettings_add_dialog_no, new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int id) {
                     }
                 });
 
         final AlertDialog alertDialog = alertDialogBuilder.create();
 
         alertDialog.show();
         alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 String possibleNewPhonenumber = inputNumber.getText().toString();
                 String possibleNewName = inputName.getText().toString();
                 if (possibleNewPhonenumber == null || possibleNewName == null) {
                    Toast.makeText(getApplicationContext(), R.string.phonenumberssettings_add_dialog_error_not_all_values_set, Toast.LENGTH_SHORT).show();
                 }
                 else if (application.phoneNumberSettings.nameExists(possibleNewName)) {
                    Toast.makeText(getApplicationContext(), R.string.phonenumberssettings_add_dialog_error_name_already_exists, Toast.LENGTH_SHORT).show();
                 }
                 else if (!PhoneNumberUtils.isGlobalPhoneNumber(possibleNewPhonenumber)) {
                    Toast.makeText(getApplicationContext(), R.string.phonenumberssettings_add_dialog_error_invalid_phonenumber, Toast.LENGTH_SHORT).show();
                 } else {
                      PhoneNumberInfo phoneNumberInfo = new PhoneNumberInfo(PhoneNumberUtils.formatNumber(possibleNewPhonenumber), possibleNewName);
                     application.phoneNumberSettings.addPhoneNumber(phoneNumberInfo);
                     alertDialog.dismiss();
                 }
             }
         });
         return null;
     }
 
 
 }
