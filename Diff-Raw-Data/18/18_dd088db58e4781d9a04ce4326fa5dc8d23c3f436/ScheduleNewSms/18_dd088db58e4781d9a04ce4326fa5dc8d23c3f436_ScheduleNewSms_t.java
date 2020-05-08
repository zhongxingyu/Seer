 package net.retsat1.starlab.smssender;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.NoSuchElementException;
 
 import javax.xml.bind.ValidationException;
 
 import net.retsat1.starlab.android.timepicker.DetailedTimePicker;
 import net.retsat1.starlab.smssender.dao.PhoneContactDao;
 import net.retsat1.starlab.smssender.dao.PhoneContactDaoImpl;
 import net.retsat1.starlab.smssender.dao.SmsMessageDao;
 import net.retsat1.starlab.smssender.dao.SmsMessageDaoImpl;
 import net.retsat1.starlab.smssender.dto.SmsMessage;
 import net.retsat1.starlab.smssender.service.SendingService;
 import net.retsat1.starlab.smssender.utils.MyLog;
 import net.retsat1.starlab.smssender.validators.LenghtNumberValidator;
 import net.retsat1.starlab.smssender.validators.NumberHighPaidValidator;
 import net.retsat1.starlab.smssender.validators.NumberValidator;
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.provider.ContactsContract;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AutoCompleteTextView;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.SimpleAdapter;
 import android.widget.Toast;
 
 public class ScheduleNewSms extends Activity implements OnClickListener {
     private static final String TAG = ScheduleNewSms.class.getSimpleName();
 
     private Button sendButton;
 
     private DatePicker datePicker;
 
     private DetailedTimePicker timePicker;
 
     private AutoCompleteTextView numberEditText;
 
     private EditText messageEditText;
     private List<NumberValidator> validators;
     private SmsMessageDao smsMessageDao;
 
     private ProgressDialog progressDialog;
 
     private PhoneContactDao phoneContactDao;
     /**
      * used to edit and copy mode;
      */
     private SmsMessage smsMessage;
 
    private int screenMode = 3;
     public static final int SCREEN_MODE_NEW = 3;
     public static final int SCREEN_MODE_EDIT = 1;
     public static final int SCREEN_MODE_COPY = 2;
     public static final String SCREEN_MODE = "SCREEN_MODE";
 
    // public void setScreenMode(int screenMode) {
    // Log.d(TAG, "ScreenMode == " + screenMode);
    // this.screenMode = screenMode;
    // }

     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         MyLog.v(TAG, "onCreate");
         setContentView(R.layout.schedule);
         validators = new ArrayList<NumberValidator>();
         validators.add(new NumberHighPaidValidator());
         validators.add(new LenghtNumberValidator());
         datePicker = (DatePicker) findViewById(R.id.dataPicker);
         timePicker = (DetailedTimePicker) findViewById(R.id.detailedTimePicker);
         numberEditText = (AutoCompleteTextView) findViewById(R.id.numberEditText);
         messageEditText = (EditText) findViewById(R.id.messageEditText);
         sendButton = (Button) findViewById(R.id.send_button);
         sendButton.setOnClickListener(this);
         smsMessageDao = new SmsMessageDaoImpl(this);
         phoneContactDao = new PhoneContactDaoImpl(this);
         setAdapterForNumberEditor();
        // setScreenMode(SCREEN_MODE_NEW);
         initEditMode();
         initCopyMode();
     }
 
     private void initCopyMode() {
         Bundle bundle = getIntent().getExtras();
         if (bundle == null) {
             return; // This is not a edit mode;
         }
         int screenMode = bundle.getInt(SCREEN_MODE);
         if (screenMode != SCREEN_MODE_COPY) {
             return; // This is not a copy mode;
         }
         Integer smsID = bundle.getInt(SmsMessage.SMS_ID);
         Log.d(TAG, "smsID " + smsID);
         smsMessage = smsMessageDao.searchByID(smsID);
         inflateSmsMessage();
        // setScreenMode(SCREEN_MODE_COPY);
     }
 
     private void initEditMode() {
         Bundle bundle = getIntent().getExtras();
         if (bundle == null) {
             return; // This is not a edit mode;
         }
         int screenMode = bundle.getInt(SCREEN_MODE);
         if (screenMode != SCREEN_MODE_EDIT) {
             return; // This is not a edit mode;
         }
         Integer smsID = bundle.getInt(SmsMessage.SMS_ID);
         Log.d(TAG, "smsID " + smsID);
         smsMessage = smsMessageDao.searchByID(smsID);
         inflateSmsMessage();
        // setScreenMode(SCREEN_MODE_EDIT);
     }
 
     private void inflateSmsMessage() {
         numberEditText.setText(smsMessage.number);
         messageEditText.setText(smsMessage.message);
         Date d = new Date(smsMessage.deliveryDate);
         datePicker.updateDate(d.getYear() + 1900, d.getMonth(), d.getDate());
         timePicker.setCurrentHour(d.getHours());
         timePicker.setCurrentMinute(d.getMinutes());
         timePicker.setCurrentSecond(d.getSeconds());
 
     }
 
     protected static int contactUpdateStatus = 0;
 
     public boolean isContactUpdateIsNeed() {
         if (contactUpdateStatus == 0) {
             return true;
         }
         return false;
     }
 
     @Override
     public void onConfigurationChanged(Configuration newConfig) {
         MyLog.d(TAG, "onConfigurationChanged");
         super.onConfigurationChanged(newConfig);
     }
 
     private static class ContactContainer extends HashMap<String, String> {
         /**
          * SerialID
          */
         private static final long serialVersionUID = 1424324242115L;
 
         /**
          * Method how to display contact in AutoComplete method
          */
         @Override
         public String toString() {
             return this.get("name") + " <" + this.get("phone") + ">";
         }
     }
 
     private static final String SELECTION_CONTACT_WITH_NUMBERS = ContactsContract.Contacts.HAS_PHONE_NUMBER + "='1'";
     private static final String[] PROJECTION_CONTACT_WITH_NUMBERS = new String[] { ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME,
             ContactsContract.Contacts.HAS_PHONE_NUMBER, };
     private static final String CONTACT_NAME = "name";
     protected static final String CONTACT_PHONE = "phone";
 
     private static SimpleAdapter phoneAdapter;
 
     private void setAdapterForNumberEditor() {
         if (isContactUpdateIsNeed()) {
             progressDialog = ProgressDialog.show(this, "Working..", "Update contacts", true, false);
         }
 
         Thread t = new Thread() {
             public void run() {
                 contactUpdateStatus = 1;
                 Cursor cursor = getAllContactWithNumbers();
 
                 if (cursor == null) {
                     MyLog.d(TAG, "No contacts to display");
                 } else {
 
                     ArrayList<ContactContainer> phones = new ArrayList<ContactContainer>();
                     phoneAdapter = new SimpleAdapter(ScheduleNewSms.this, phones, R.layout.phone_row_entry, new String[] { CONTACT_NAME, CONTACT_PHONE },
                             new int[] { R.id.row_display_name, R.id.row_phone_number });
 
                     while (cursor.moveToNext()) {
                         String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                         String displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                         Cursor pCur = getAllPhoneByContactId(id);
                         MyLog.d(TAG, "Contact " + id + " how many phone numbers " + pCur.getCount());
                         while (pCur.moveToNext()) {
                             String phone = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA));
                             addPhoneToList(phone, displayName, phones);
                         }
                         pCur.close();
                     }
                     cursor.close();
                     ScheduleNewSms.this.runOnUiThread(new Runnable() {
                         @Override
                         public void run() {
                             MyLog.d(TAG, "numberEditText setAdapter ");
                             numberEditText.setAdapter(phoneAdapter);
                             progressDialog.dismiss();
                             contactUpdateStatus = 2;
                         }
                     });
                 }
                 ;
             }
 
             /**
              * Aggregate phones
              * 
              * @param phone
              *            phone number
              * @param displayName
              * @param phones
              *            place where we aggregate phone numbers
              */
             private void addPhoneToList(String phone, String displayName, ArrayList<ContactContainer> phones) {
                 ContactContainer phoneRow = new ContactContainer();
                 phoneRow.put("name", displayName);
                 phoneRow.put("phone", phone);
                 phones.add(phoneRow);
                 MyLog.i("Pratik", "PHONE: " + displayName + " <" + phone + "> ");
             }
 
             private Cursor getAllPhoneByContactId(String id) {
                 String[] selection = new String[] { id };
                 String[] phoneProjection = new String[] { ContactsContract.CommonDataKinds.Phone.DATA, ContactsContract.CommonDataKinds.Phone.TYPE };
                 String where = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?";
                 return getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, phoneProjection, where, selection, null);
             }
 
             private Cursor getAllContactWithNumbers() {
                 return getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, PROJECTION_CONTACT_WITH_NUMBERS, SELECTION_CONTACT_WITH_NUMBERS, null,
                         null);
             }
         };
         if (isContactUpdateIsNeed()) {
             t.start();
         } else {
             assert (phoneAdapter == null);
             MyLog.d(TAG, "phone adapter " + phoneAdapter);
             numberEditText.setAdapter(phoneAdapter);
         }
     }
 
     private static int idCode = 0;
 
     private static final long DATE_27_III_2011 = 1301258571993L;
 
     private void addMessageToSend(String number, String message) {
         logTime();
         MyLog.d(TAG, "sendMessage number " + number + " message " + message);
         try {
             validNumber(number);
         } catch (ValidationException e) {
             Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
         }
         idCode++;
         long timeNow = System.currentTimeMillis();
         long scheduledTimeMillis = getSetupTime();
         int reqCode = (int) ((timeNow - DATE_27_III_2011) + idCode);
         SmsMessage smsMessage = createNewMessage(reqCode, number, message, scheduledTimeMillis);
         smsMessageDao.insert(smsMessage);
         alarmSetup(smsMessage);
     }
 
     private void validNumber(String number) throws ValidationException {
         for (NumberValidator validator : validators) {
             if (!validator.isValid(number)) {
                 throw new ValidationException(getResources().getString(validator.getErrorMessageRef()));
 
             }
         }
     }
 
     private SmsMessage createNewMessage(int reqCode, String number, String message, long scheduledTimeMillis) {
         SmsMessage smsMessage = new SmsMessage();
         smsMessage.id = reqCode;
         smsMessage.number = number;
         smsMessage.message = message;
         smsMessage.deliveryDate = scheduledTimeMillis;
         smsMessage.dateOfSetup = System.currentTimeMillis();
         smsMessage.dateOfStatus = System.currentTimeMillis();
         smsMessage.messageStatus = SmsMessage.STATUS_UNSENT;
         smsMessage.deliveryStatus = SmsMessage.STATUS_UNSENT;
         return smsMessage;
     }
 
     private void alarmSetup(SmsMessage smsMessage) {
 
         MyLog.d(TAG, "Data when" + smsMessage.deliveryDate);
         Intent intent = new Intent(this, SendingService.class);
         intent.putExtra(SmsMessage.SMS_ID, smsMessage.id);
         PendingIntent pendingIntent = PendingIntent.getService(this, smsMessage.id, intent, PendingIntent.FLAG_ONE_SHOT);
         AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
         alarmManager.set(AlarmManager.RTC_WAKEUP, smsMessage.deliveryDate, pendingIntent);
     }
 
     private long getSetupTime() {
         Calendar calendar = GregorianCalendar.getInstance();
         calendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(), timePicker.getCurrentHour(), timePicker.getCurrentMinute(),
                 timePicker.getCurrentSecond());
         calendar.set(Calendar.MILLISECOND, 0);
         return calendar.getTimeInMillis();
     }
 
     private void logTime() {
         MyLog.d(TAG, "year: " + datePicker.getYear());
         MyLog.d(TAG, "month: " + datePicker.getMonth());
         MyLog.d(TAG, "day: " + datePicker.getDayOfMonth());
         MyLog.d(TAG, "hh: " + timePicker.getCurrentHour());
         MyLog.d(TAG, "mm: " + timePicker.getCurrentMinute());
         MyLog.d(TAG, "ss: " + timePicker.getCurrentSecond());
     }
 
     @Override
     public void onClick(View v) {
         switch (v.getId()) {
         case R.id.send_button:
             if (screenMode == SCREEN_MODE_NEW) {
                 String number = numberEditText.getText().toString();
                 String message = messageEditText.getText().toString();
                 addMessageToSend(number, message);
             } else if (screenMode == SCREEN_MODE_EDIT) {
                 smsMessage.number = numberEditText.getText().toString();
                 smsMessage.message = messageEditText.getText().toString();
                 updateMessage();
                 smsMessageDao.update(smsMessage);
             } else if (screenMode == SCREEN_MODE_COPY) {
                 smsMessage.number = numberEditText.getText().toString();
                 smsMessage.message = messageEditText.getText().toString();
                 updateMessage();
                 smsMessageDao.insert(smsMessage);
             }
             finish();
             break;
         default:
             throw new NoSuchElementException("This exception should never be call, please define all menu button");
         }
     }
 
     private void updateMessage() {
         MyLog.d(TAG, "sendMessage number " + smsMessage.number + " message " + smsMessage.message);
         try {
             validNumber(smsMessage.number);
         } catch (ValidationException e) {
             Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
         }
         smsMessage.deliveryDate = getSetupTime();
         smsMessage.dateOfSetup = System.currentTimeMillis();
         smsMessage.dateOfStatus = System.currentTimeMillis();
         smsMessage.messageStatus = SmsMessage.STATUS_UNSENT;
         smsMessage.deliveryStatus = SmsMessage.STATUS_UNSENT;
         alarmSetup(smsMessage);
 
     }
 
 }
