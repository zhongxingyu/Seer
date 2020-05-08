 /* 
  * Copyright (C) 2009 Vincent Behar
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package ffck.members.activities;
 
 import static ffck.members.Members.CONTENT_URI;
 import static ffck.members.Members.Columns.ADDRESS;
 import static ffck.members.Members.Columns.BIRTH_DATE;
 import static ffck.members.Members.Columns.CITY;
 import static ffck.members.Members.Columns.CODE;
 import static ffck.members.Members.Columns.EMAIL;
 import static ffck.members.Members.Columns.EMAIL_2;
 import static ffck.members.Members.Columns.FIRST_NAME;
 import static ffck.members.Members.Columns.GENDER;
 import static ffck.members.Members.Columns.LAST_LICENSE;
 import static ffck.members.Members.Columns.LAST_NAME;
 import static ffck.members.Members.Columns.PHONE_HOME;
 import static ffck.members.Members.Columns.PHONE_MOBILE;
 import static ffck.members.Members.Columns.PHONE_MOBILE_2;
 import static ffck.members.Members.Columns.PHONE_OTHER;
 import static ffck.members.Members.Columns.POSTAL_CODE;
 import ffck.members.R;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.ContentValues;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.Contacts;
 import android.text.TextUtils;
 import android.text.util.Linkify;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import java.util.Arrays;
 import java.util.List;
 
 /**
  * FFCK Member details activity. Display the details of a member, and allow to
  * delete it. When the member is deleted, this activity will be finished. Also
  * allow to add the member to your contacts.
  */
 public class MemberDetailsActivity extends Activity {
 
     /*
      * Dialogs identifiers
      */
 
     /** Identifier for the 'Add to contacts' dialog */
     private static final int DIALOG_ADD_TO_CONTACTS = 1;
 
     /** Identifier for the 'Delete' dialog */
     private static final int DIALOG_DELETE = 2;
 
     /*
      * DB->View mapping
      */
 
     /** Fields that will be 'linkified' */
     private static final List<String> LINKIFIED_FIELDS = Arrays.asList(new String[] {
             PHONE_HOME, PHONE_MOBILE, PHONE_MOBILE_2, PHONE_OTHER, EMAIL, EMAIL_2
     });
 
     /** Source for the DB->View mapping : Columns names */
     private static final String[] FROM = {
             LAST_NAME, FIRST_NAME, GENDER, BIRTH_DATE, PHONE_MOBILE, PHONE_MOBILE_2, PHONE_HOME,
             PHONE_OTHER, EMAIL, EMAIL_2, ADDRESS, POSTAL_CODE, CITY, CODE, LAST_LICENSE
     };
 
     /** Destination for the DB->View mapping : View IDs */
     private static final int[] TO = {
             R.id.member_details_last_name, R.id.member_details_first_name,
             R.id.member_details_gender, R.id.member_details_birth_date,
             R.id.member_details_phone_mobile, R.id.member_details_phone_mobile_2,
             R.id.member_details_phone_home, R.id.member_details_phone_other,
             R.id.member_details_email, R.id.member_details_email_2, R.id.member_details_address,
             R.id.member_details_postal_code, R.id.member_details_city, R.id.member_details_code,
             R.id.member_details_last_license
     };
 
     /*
      * Instance-specific variables
      */
 
     /** The cursor holding the member's data */
     private Cursor cursor;
 
     /*
      * Activity lifecycle
      */
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.member_details);
 
         // retrieve the cursor for the member represented by the URI
         Uri uri = getIntent().getData();
         cursor = managedQuery(uri, null, null, null, null);
 
         // nothing found, let's just going back...
         if (cursor == null || !cursor.moveToFirst()) {
             setResult(Activity.RESULT_CANCELED);
             finish();
             return;
         }
 
         // map the cursor's data to the view
         for (int i = 0; i < TO.length; i++) {
             TextView view = (TextView)findViewById(TO[i]);
             String text = cursor.getString(cursor.getColumnIndexOrThrow(FROM[i]));
             view.setText(text);
             if (LINKIFIED_FIELDS.contains(FROM[i])) {
                 Linkify.addLinks(view, Linkify.ALL);
             }
         }
     }
 
     /*
      * Menu management
      */
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         if (!super.onCreateOptionsMenu(menu)) {
             return false;
         }
 
         // create the menu from XML
         MenuInflater menuInflater = getMenuInflater();
         menuInflater.inflate(R.menu.member_details_menu, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         if (super.onOptionsItemSelected(item)) {
             return true;
         }
 
         // when a menu item is selected :
         switch (item.getItemId()) {
             case R.id.member_details_menu_add_contact:
                 showDialog(DIALOG_ADD_TO_CONTACTS);
                 return true;
             case R.id.member_details_menu_delete:
                 showDialog(DIALOG_DELETE);
                 return true;
             default:
                 return false;
         }
     }
 
     /*
      * Dialogs management
      */
 
     @Override
     protected Dialog onCreateDialog(int id) {
         switch (id) {
             case DIALOG_ADD_TO_CONTACTS:
                 AlertDialog.Builder addToContacts = new AlertDialog.Builder(this);
                 addToContacts.setIcon(android.R.drawable.ic_dialog_alert);
                 addToContacts.setTitle(R.string.dialog_add_to_contacts_title);
                 addToContacts.setMessage(getString(R.string.dialog_add_to_contacts_text,
                         get(FIRST_NAME), get(LAST_NAME)));
                 addToContacts.setPositiveButton(R.string.dialog_add_to_contacts_button_ok,
                         new OnClickListener() {
                             @Override
                             public void onClick(DialogInterface dialog, int which) {
                                 addToContacts();
                             }
                         });
                 addToContacts.setNegativeButton(R.string.dialog_add_to_contacts_button_nok, null);
                 return addToContacts.create();
             case DIALOG_DELETE:
                 AlertDialog.Builder delete = new AlertDialog.Builder(this);
                 delete.setIcon(android.R.drawable.ic_dialog_alert);
                 delete.setTitle(R.string.dialog_delete_member_title);
                 delete.setMessage(getString(R.string.dialog_delete_member_text, get(FIRST_NAME),
                         get(LAST_NAME)));
                 delete.setPositiveButton(R.string.dialog_delete_member_button_ok,
                         new OnClickListener() {
                             @Override
                             public void onClick(DialogInterface dialog, int which) {
                                 deleteMember();
                             }
                         });
                 delete.setNegativeButton(R.string.dialog_delete_member_button_nok, null);
                 return delete.create();
             default:
                 return null;
         }
     }
 
     /*
      * Menu actions
      */
 
     /**
      * Add the member (represented by the cursor of this activity) to the
      * contacts. Will display a success message after adding.
      */
     private void addToContacts() {
         String firstName = get(FIRST_NAME);
         String lastName = get(LAST_NAME);
         String phoneMobile = get(PHONE_MOBILE);
         String phoneHome = get(PHONE_HOME);
         String email = get(EMAIL);
 
         ContentValues values = new ContentValues();
 
         // create a new contact with the name
        values.put(Contacts.People.NAME, firstName + " " + lastName);
         Uri uri = MemberDetailsActivity.this.getContentResolver().insert(
                 Contacts.People.CONTENT_URI, values);
 
         // add the phone numbers
         Uri phoneUri = Uri.withAppendedPath(uri, Contacts.People.Phones.CONTENT_DIRECTORY);
         if (!TextUtils.isEmpty(phoneMobile)) {
             values.clear();
            values.put(Contacts.Phones.TYPE, Contacts.Phones.TYPE_MOBILE);
            values.put(Contacts.Phones.NUMBER, phoneMobile);
             MemberDetailsActivity.this.getContentResolver().insert(phoneUri, values);
         }
         if (!TextUtils.isEmpty(phoneHome)) {
             values.clear();
            values.put(Contacts.Phones.TYPE, Contacts.Phones.TYPE_HOME);
            values.put(Contacts.Phones.NUMBER, phoneHome);
             MemberDetailsActivity.this.getContentResolver().insert(phoneUri, values);
         }
 
         // add the email
         if (!TextUtils.isEmpty(email)) {
             Uri emailUri = Uri.withAppendedPath(uri,
                     Contacts.People.ContactMethods.CONTENT_DIRECTORY);
             values.clear();
            values.put(Contacts.People.ContactMethods.KIND, Contacts.KIND_EMAIL);
            values.put(Contacts.People.ContactMethods.DATA, email);
            values.put(Contacts.People.ContactMethods.TYPE,
                    Contacts.People.ContactMethods.TYPE_HOME);
             MemberDetailsActivity.this.getContentResolver().insert(emailUri, values);
         }
 
         // display success message (toaster)
         Toast.makeText(MemberDetailsActivity.this,
                 getString(R.string.toast_add_member_to_contacts, firstName, lastName),
                 Toast.LENGTH_LONG).show();
     }
 
     /**
      * Delete the member represented by the cursor of this activity, and finish
      * the activity. Will display a success message after deletion.
      */
     private void deleteMember() {
         // delete the member
         Uri uri = Uri.withAppendedPath(CONTENT_URI, get(CODE));
         getContentResolver().delete(uri, null, null);
 
         // display success message (toaster)
         Toast.makeText(MemberDetailsActivity.this,
                 getString(R.string.toast_delete_member, get(FIRST_NAME), get(LAST_NAME)),
                 Toast.LENGTH_SHORT).show();
 
         // finish the activity
         setResult(Activity.RESULT_OK);
         finish();
     }
 
     /*
      * Helper methods
      */
 
     /**
      * Retrieve the value in the given columnName, for the current member
      * 
     * @param columnName for which the value should be retrieved (see Members.Columns)
      * @return String : value retrieved
      */
     private String get(String columnName) {
         return cursor.getString(cursor.getColumnIndexOrThrow(columnName));
     }
 
 }
