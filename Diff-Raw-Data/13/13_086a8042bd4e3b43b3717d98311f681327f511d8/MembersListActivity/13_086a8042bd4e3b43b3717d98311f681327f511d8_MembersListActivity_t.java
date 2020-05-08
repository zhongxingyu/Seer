 package com.moac.android.opensecretsanta.activity;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.provider.ContactsContract;
 import android.provider.ContactsContract.CommonDataKinds.Email;
 import android.provider.ContactsContract.CommonDataKinds.Phone;
 import android.provider.ContactsContract.Contacts;
 import android.text.InputType;
 import android.util.Log;
 import android.view.View;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.*;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import com.moac.android.opensecretsanta.OpenSecretSantaApplication;
 import com.moac.android.opensecretsanta.R;
 import com.moac.android.opensecretsanta.database.DatabaseManager;
 import com.moac.android.opensecretsanta.types.Group;
 import com.moac.android.opensecretsanta.types.Member;
 import com.moac.android.opensecretsanta.types.Restriction;
 
 import java.util.*;
 
 public class MembersListActivity extends Activity {
 
     private final static String TAG = "MembersListActivity";
     private final static int PICK_CONTACT_REQUEST = 1;
 
     private List<MemberRowDetails> items;
     private ArrayAdapter<MemberRowDetails> aa;
     ListView mList;
 
     // UI Components
     ImageButton mAddMemberButton = null;
     ImageButton mAddManualEntryButton = null;
 
     Group mGroup;
     DatabaseManager mDatabase;
 
     private View.OnClickListener mDeleteClickListener;
     private View.OnClickListener mRestrictClickListener;
 
     private ViewSwitcher mSwitcher;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         initialiseUIComponents();
         initialise();
     }
 
     @Override
     public void onStart() {
         super.onStart();
         Log.v(TAG, "onStart() - start");
         mSwitcher.reset();
         populateMembersList();
         Log.v(TAG, "onStart() - end");
     }
 
     @Override
     public void onDestroy() {
         super.onDestroy();
     }
 
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         Log.v(TAG, "onActivityResult() - start");
         if(requestCode == PICK_CONTACT_REQUEST && data != null) {
             Log.v(TAG, "onActivityResult() - PICK_CONTACT");
 
             Uri result = data.getData();
             Log.v(TAG, "Got a result: " + result.toString());
             loadContactInfo(result);
         }
     }
 
     private void initialiseUIComponents() {
         setContentView(R.layout.members_list_view);
 
         mSwitcher = (ViewSwitcher) findViewById(R.id.memberListSwitcher);
         mSwitcher.setAnimateFirstView(false);
 
         // Obtain handles to UI objects
         mAddMemberButton = (ImageButton) findViewById(R.id.addMemberFromContactsButton);
         mAddMemberButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 // Open the contacts list.
                 addParticipantHandler(v);
             }
         });
 
         mAddManualEntryButton = (ImageButton) findViewById(R.id.addManualEntryButton);
         mAddManualEntryButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 showManualEntryDialog();
             }
         });
 
         mDeleteClickListener = new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 final MemberListAdapter.DeleteViewTag tag = (MemberListAdapter.DeleteViewTag) view.getTag();
                 // Confirm delete of group
                 AlertDialog.Builder builder = new AlertDialog.Builder(MembersListActivity.this);
                 builder.setTitle("Delete " + tag.memberName + " from the group?")
                   .setNegativeButton("Cancel", null)
                   .setCancelable(true)
                   .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                       @Override
                       public void onClick(DialogInterface dialog, int id) {
                           // The view is the button, which has as tag the memberId
                           removeParticipantFromGroup(tag.memberId);
                       }
                   });
                 builder.setIcon(R.drawable.ic_menu_delete);
                 AlertDialog alert = builder.create();
                 alert.show();
             }
         };
 
         mRestrictClickListener = new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 Log.v(TAG, "onClick restrict");
 
                 // No point if there's only one person!
                 if(items.size() > 1) {
                     // Extract from the tag.
                     MemberRowDetails selectedItem = (MemberRowDetails) view.getTag();
                     long memberId = selectedItem.getMemberId();
                     String memberName = selectedItem.getMemberName();
 
                     RestrictionBuilder rb = new RestrictionBuilder(memberId, memberName);
                     rb.openRestrictionsDialog();
                 }
             }
         };
 
         mList = (ListView) findViewById(R.id.membersList);
         // Onclick listener to open notify mode selection dialog
         mList.setOnItemClickListener(new OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> myAdapter, View myView, int myItemInt, long mylng) {
                 Log.v(TAG, "onItemClick members");
 
                 MemberRowDetails selected = (MemberRowDetails) aa.getItem(myItemInt);
 
                 String lookupKey = selected.getLookupKey();
 
                 // If null then is a manual entry - or at least, we can't do anything with it..
                 if(!(lookupKey == null || lookupKey.equals(""))) {
                     Log.v(TAG, "onItemClick() lookupKey: " + lookupKey);
                     // Override the name, in case it's different from the entry in the contacts list.
                     RetrieveMemberTask task = new RetrieveMemberTask(selected.getMemberName());
 
                     // Build up the Uri to use in the lookup
                     Uri lookupUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
                     Uri res = ContactsContract.Contacts.lookupContact(getContentResolver(), lookupUri);
 
                     // Execute the lookup
                     task.execute(res);
                 } else {
                     // For a manual entry - no override as it only has the one name.
 
                     // Just populate and open the dialog.
                     RetrievedContactDetails contact = new RetrievedContactDetails();
                     contact.name = selected.getMemberName();
                     contact.contacts.add(new ContactModeRowDetails(Constants.NAME_ONLY_CONTACT_MODE, null));
 
                     showContactModeDialog(contact, selected.getMemberName());
                 }
             }
         });
 
         // Define long-click listener for rename
         mList.setOnItemLongClickListener(new OnItemLongClickListener() {
 
             @Override
             public boolean onItemLongClick(AdapterView<?> myAdapter, View myView,
                                            int myItemInt, long arg3) {
                 MemberRowDetails selectedItem = (MemberRowDetails) ((ListView) myAdapter).getItemAtPosition(myItemInt);
                 showRenameMemberDialog(selectedItem);
                 return true;
             }
         });
     }
 
     // initialise with the list of members if any already exist for this group
     private void initialise() {
         mDatabase = OpenSecretSantaApplication.getDatabase();
 
         Bundle extras = getIntent().getExtras();
         if(extras != null) {
             long groupId = extras.getLong(Constants.GROUP_ID);
             mGroup = mDatabase.queryById(groupId, Group.class);
         }
 
         items = new ArrayList<MemberRowDetails>();
         aa = new MemberListAdapter(this, R.layout.member_row, items, mRestrictClickListener, mDeleteClickListener);
         mList.setAdapter(aa);
     }
 
     private void showManualEntryDialog() {
         Log.v(TAG, "showManualEntryDialog()");
 
         AlertDialog.Builder builder = new AlertDialog.Builder(MembersListActivity.this);
         builder.setTitle("Member Name");
         builder.setIcon(R.drawable.ic_user);
 
         // Set an EditText view to get user input
         final EditText input = new EditText(this);
         input.setSingleLine();
         input.setSingleLine();
         input.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
         builder.setView(input);
 
         builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int whichButton) {
                 String memberName = input.getText().toString().trim();
                 Log.v(TAG, "memberName:" + memberName);
                 if(memberName != null && !memberName.equals("")) {
                     Log.v(TAG, "Attempting to create/update: " + memberName);
                     Member member = new Member();
                     member.setName(memberName);
                     member.setContactMode(Constants.NAME_ONLY_CONTACT_MODE);
                     member.setGroup(mGroup);
                     mDatabase.create(member);
                     populateMembersList();
                 }
                 dialog.dismiss();
             }
         });
 
         builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int whichButton) {
                 dialog.dismiss();
             }
         });
 
         AlertDialog alert = builder.create();
         //display dialog box
         alert.show();
     }
 
     private void showRenameMemberDialog(final MemberRowDetails _selectedItem) {
         Log.v(TAG, "showRenameMemberDialog()");
 
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setTitle("Rename Member");
         builder.setIcon(R.drawable.ic_user);
 
         // Set an EditText view to get user input
         final EditText input = new EditText(this);
         input.setSingleLine();
         input.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
         input.setText(_selectedItem.memberName);
 
         builder.setView(input);
         builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int whichButton) {
 
                 String memberName = input.getText().toString().trim();
                 Log.v(TAG, "memberName:" + memberName);
 
                 if(memberName != null && !memberName.equals("") && !memberName.equals(_selectedItem.memberName)) {
                     Log.v(TAG, "Attempting to updating member name from: " + _selectedItem.memberName + " to " + memberName);
                     updateMemberName(_selectedItem.getMemberId(), memberName);
                 }
                 // Force hide of the keyboard.
                 InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                 imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                 dialog.dismiss();
             }
         });
 
         builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int whichButton) {
                 // Force hide of the keyboard.
                 InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                 imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                 dialog.dismiss();
             }
         });
 
         AlertDialog alert = builder.create();
         //display dialog box
         alert.show();
     }
 
     private void updateMemberName(final long _memberId, final String _memberName) {
 
         AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
 
             @Override
             protected Boolean doInBackground(Void... params) {
 
                 // Get the member with that id.
                 try {
 
                     // If new member name exists already for this group, then fail.
                     Member existingWithNewName = mDatabase.queryMemberWithNameForGroup(mGroup.getId(), _memberName);
 
                     if(existingWithNewName == null) {
                         Member existingMember = mDatabase.queryById(_memberId, Member.class);
 
                         // Only bother if it's actually changing.
                         if(!existingMember.getName().equals(_memberName)) {
                             existingMember.setName(_memberName);
                             mDatabase.update(existingMember);
                             // Need to force redraw  to use new name in the draw.
                             mGroup.setReady(false);
                             mDatabase.update(mGroup);
                         }
                     } else {
                         return Boolean.FALSE;
                     }
                 } catch(SQLException exp) {
                     Log.e(TAG, exp.getMessage(), exp);
                     return Boolean.FALSE;
                 }
 
                 return Boolean.TRUE;
             }
 
             @Override
             protected void onPostExecute(Boolean result) {
                 if(result.equals(Boolean.TRUE)) {
                     populateMembersList();
                 } else {
                     Toast.makeText(MembersListActivity.this, "Sorry, that member already exists", Toast.LENGTH_SHORT).show();
                 }
             }
         };
 
         task.execute();
     }
 
     private void addParticipantHandler(View v) {
         Intent intent = new Intent(Intent.ACTION_PICK);
         intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
         startActivityForResult(intent, PICK_CONTACT_REQUEST);
     }
 
     public void removeParticipantFromGroup(final long memberId) {
         Log.v(TAG, "remove " + memberId);
 
         AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
 
             @Override
             protected Void doInBackground(Void... params) {
                 mDatabase.delete(memberId, Member.class);
                 mGroup.setReady(false);
                 mDatabase.update(mGroup);
                 return null;
             }
 
             @Override
             protected void onPostExecute(Void v) {
                 // Kick off another task.
                 populateMembersList();
             }
         };
 
         task.execute();
     }
 
     // Fill the list.
     private void populateMembersList() {
 
         AsyncTask<Void, Void, List<MemberRowDetails>> task = new AsyncTask<Void, Void, List<MemberRowDetails>>() {
 
             @Override
             protected List<MemberRowDetails> doInBackground(Void... params) {
                 Log.v(TAG, "populateMembersList() - populating member list");
                 List<MemberRowDetails> rows = new ArrayList<MemberRowDetails>();
                 HashMap<Long, MemberRowDetails> restrictions = new HashMap<Long, MemberRowDetails>();
 
                 List<Member> members = mDatabase.queryAllMembersForGroup(mGroup.getId());
                 Log.v(TAG, "number of members found " + members.size());
 
                 for(Member member : members) {
                     //  A member's name is unique for a group.
                     String memberName = member.getName();
                     long id = member.getId();
                     String contactDetail = member.getContactDetail();
                     int contactMode = member.getContactMode();
                     String lookupKey = member.getLookupKey();
                     List<Restriction> restrictionList = mDatabase.queryAllRestrictionsForMemberId(id);
 
                     Log.v(TAG, "populdateMembersList() lookupKey: " + lookupKey);
                     MemberRowDetails row = new MemberRowDetails(id, lookupKey, memberName, contactMode, contactDetail, restrictionList.size());
                     restrictions.put(id, row);
                     rows.add(row);
                 }
 
                 Log.v(TAG, "populateMembersList() - row count: " + rows.size());
                 Collections.sort(rows);
                 return rows;
             }
 
             @Override
             protected void onPostExecute(List<MemberRowDetails> rows) {
 
                 // Set this here - NOT in the background thread, otherwise might corrupt listadapter.
                 setMembersListVisible(rows.size() > 0);
 
                 items = rows;
                 aa.clear();
                 for(MemberRowDetails dr : items) {
                     aa.add(dr);
                 }
                 Log.v(TAG, "onPostExecute() rows: " + rows.size());
                 aa.notifyDataSetChanged();
             }
         };
 
         task.execute();
     }
 
     private void setMembersListVisible(boolean visible) {
         if(visible) {
             mSwitcher.setDisplayedChild(1);
         } else {
             mSwitcher.setDisplayedChild(0);
         }
     }
 
     private class RetrievedContactDetails {
         String lookupKey;
         String name;
         List<ContactModeRowDetails> contacts = new ArrayList<ContactModeRowDetails>();
     }
 
     /**
      * Responsible for loading the details out of the contact list
      *
      * @author peter
      */
     private class RetrieveMemberTask extends AsyncTask<Uri, Void, RetrievedContactDetails> {
 
         String mNameOverride = null;
 
         public RetrieveMemberTask() {
             super();
         }
 
         /*
          * Override name if using a different name to the one in the contact list.
          */
         public RetrieveMemberTask(String _nameOverride) {
             super();
             mNameOverride = _nameOverride;
         }
 
         @Override
         protected RetrievedContactDetails doInBackground(Uri... uris) {
             Log.v(TAG, "RetrieveMemberTask() - doInBackgrounnd() ");
             RetrievedContactDetails result = new RetrievedContactDetails();
             if(uris != null && uris[0] != null) {
                 result = loadContact(getContentResolver(), uris[0]);
             }
 
             // TODO Thinking bout using results.contact == null
             // as the indicator that nothing can be retrieved
             // or perhaps no selectors.
 
             // Use the user selected name instead.
             if(mNameOverride != null) {
                 result.name = mNameOverride;
             }
 
             return result;
         }
 
         @Override
         protected void onPostExecute(RetrievedContactDetails contact) {
             Log.v(TAG, "loadContactInfo() - onPostExecute() ");
 
             if(contact.name == null || contact.name.trim().equals("")) {
                 Toast.makeText(MembersListActivity.this,
                   "Can't add a contact without a name.", Toast.LENGTH_SHORT).show();
                 return;
             }
 
 			/*
              * This can mean that contact doesn't exist or they have an
 			 * entry, but no details.
 			 */
 
             // See if we could retrieve anything
             if(contact.contacts.size() == 0) {
                 Toast.makeText(MembersListActivity.this,
                   "There's no contact details available for this person.", Toast.LENGTH_SHORT).show();
 
                 // Clear the lookupKey so this doesn't happen again.
                 contact.lookupKey = null;
             }
 
             // Add a manual entry option
             contact.contacts.add(new ContactModeRowDetails(Constants.NAME_ONLY_CONTACT_MODE, null));
 
             showContactModeDialog(contact, mNameOverride);
         }
     }
 
     /**
      * Load contact information on a background thread.
      * Used when adding a new entry.
      */
     private void loadContactInfo(Uri contactUri) {
         RetrieveMemberTask task = new RetrieveMemberTask();
         task.execute(contactUri);
     }
 
     /*
      * Extract the relevant details from the contacts list using the
      * supplied Uri for a specific contact.
      *
      */
     public RetrievedContactDetails loadContact(ContentResolver contentResolver, Uri contactUri) {
         RetrievedContactDetails contactInfo = new RetrievedContactDetails();
         long contactId = -1;
 
         if(contactUri != null) {
 
             // Load the display name for the specified person
             Cursor cursor = contentResolver.query(contactUri,
               new String[]{ Contacts._ID, Contacts.DISPLAY_NAME, Contacts.LOOKUP_KEY }, null, null, null);
             try {
                 if(cursor.moveToFirst()) {
                     contactId = cursor.getLong(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                     contactInfo.lookupKey = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                     contactInfo.name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
 
                     Log.v(TAG, "loadContact() - lookupKey: " + contactInfo.lookupKey);
                 }
             } finally {
                 cursor.close();
             }
 
             // TODO - This should be expanded to handle other communication types.
             contactInfo.contacts.addAll(findMobileNumbers(contentResolver, contactId));
             contactInfo.contacts.addAll(findEmailAddresses(contentResolver, contactId));
         }
 
         return contactInfo;
     }
 
     /*
      * Extracts only the mobile numbers from the cursor.
      *
      */
     private Set<ContactModeRowDetails> findMobileNumbers(ContentResolver contentResolver, long contactId) {
 
         Set<ContactModeRowDetails> results = new HashSet<ContactModeRowDetails>();
 
         // Ignore any phone numbers if we have no telephony features.
         // Apparently checking for PackageManager.hasSystemFeature is unreliable.
         // See - https://groups.google.com/forum/?fromgroups=#!topic/android-developers/yiRhchAA3JA
         if(android.telephony.SmsManager.getDefault() == null) {
             return Collections.emptySet();
         }
 
         // Load the phone number (if any).
         Cursor cursor = contentResolver.query(Phone.CONTENT_URI,
           null,
           Phone.CONTACT_ID + "=" + contactId + " and ( " +
             Phone.TYPE + "=" + Phone.TYPE_MOBILE
             + " or " + Phone.TYPE + "=" + Phone.TYPE_MAIN
             + " or " + Phone.TYPE + "=" + Phone.TYPE_HOME
             + " or " + Phone.TYPE + "=" + Phone.TYPE_WORK_MOBILE
             + " or " + Phone.TYPE + "=" + Phone.TYPE_WORK + " )",
           null,
           null);
 
         try {
 
             while(cursor.moveToNext()) {
                 String phoneNo =
                   cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).trim();
                 Log.v(TAG, "loadContact() - found mobile phone number " + phoneNo);
 
                 // Just some *very* basic validation - can't trust contacts.
                 if(phoneNo.length() > 0) {
                     ContactModeRowDetails newContact = new ContactModeRowDetails(Integer.valueOf(Constants.SMS_CONTACT_MODE), phoneNo);
                     results.add(newContact);
                 }
             }
 
             return results;
         } finally {
             cursor.close();
         }
     }
 
     /**
      * Finds all unique, valid email addresses in the contact data.
      *
      * @param contentResolver
      * @param contactId
      * @return
      */
     private static Set<ContactModeRowDetails> findEmailAddresses(ContentResolver contentResolver, long contactId) {
         Set<ContactModeRowDetails> results = new HashSet<ContactModeRowDetails>();
 
         // Load the phone number (if any).
         Cursor cursor = contentResolver.query(Email.CONTENT_URI,
           null,
           Email.CONTACT_ID + "=" + contactId,
           null,
           null);
 
         try {
 
             while(cursor.moveToNext()) {
                 String email = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA1));
                 boolean isValid =
                   org.apache.commons.validator.routines.EmailValidator.getInstance().isValid(email);
                 Log.v(TAG, "loadContact() - found email address " + email + " isValid: " + isValid);
                 if(isValid) {
                     results.add(new ContactModeRowDetails(Integer.valueOf(Constants.EMAIL_CONTACT_MODE), email));
                 }
             }
 
             return results;
         } finally {
             cursor.close();
         }
     }
 
     /*
      * Name override is for when the same contact is added to the list multiple times. but the name is modified.
      */
     private void showContactModeDialog(final RetrievedContactDetails contact, String memberName) {
         Log.v(TAG, "showContactModeDialog()");
 
         AlertDialog.Builder builder = new AlertDialog.Builder(MembersListActivity.this);
 
         final String nameToUse = (memberName == null ? contact.name : memberName);
         builder.setTitle("How to notify " + nameToUse + "?");
         builder.setIcon(R.drawable.portfolio);
 
         final List<ContactModeRowDetails> contactList = new ArrayList<ContactModeRowDetails>(contact.contacts);
         builder.setSingleChoiceItems(
           new ContactModeListAdapter(MembersListActivity.this, R.layout.contact_mode_row, contactList), -1,
           new DialogInterface.OnClickListener() {
               // Click listener
               @Override
               public void onClick(DialogInterface dialog, int item) {
 
                   Member member = mDatabase.queryMemberWithNameForGroup(mGroup.getId(), nameToUse);
                   if (member == null) {
                       // Doesn't exist - create new
                       member = new Member();
                       member.setGroup(mGroup);
                       member.setName(nameToUse);
                       member.setLookupKey(contact.lookupKey);
                       member.setContactMode(contactList.get(item).contactMode);
                       member.setContactDetail(contactList.get(item).contactDetail);
                       mDatabase.create(member);
                       mGroup.setReady(false);
                       mDatabase.update(mGroup);
                   } else if (contact != null) { // can't modify contact mode anything when no contact!
                       // Check if they actually updated anything... don't make it redraw unnecessarily.
                       boolean isDirty = false;
                       boolean isKeyDirty = member.getLookupKey() == null ? member.getLookupKey() != contact.lookupKey : !member.getLookupKey().equals(contact.lookupKey);
                       boolean isContactDetailDirty = member.getContactDetail() == null ? member.getContactDetail() != contactList.get(item).contactDetail
                          : !member.getContactDetail().equals(contactList.get(item).contactDetail);
 
                       // Check if there are actually updates
                       if (isKeyDirty) {
                           member.setLookupKey(contact.lookupKey);
                           isDirty = true;
                       }
                       if (member.getContactMode() != (contactList.get(item).contactMode)) {
                           member.setContactMode(contactList.get(item).contactMode);
                           isDirty = true;
                       }
 
                       if (isContactDetailDirty) {
                           member.setContactDetail(contactList.get(item).contactDetail);
                           isDirty = true;
                       }
                       if (isDirty) {
                           mDatabase.update(member);
                           mGroup.setReady(false);
                           mDatabase.update(mGroup);
                       }
                   };
 
                   populateMembersList();
 
                   dialog.dismiss();
               }
           });
 
         AlertDialog alert = builder.create();
         alert.show();
     }
 
     private class RestrictionBuilder {
 
         List<RestrictionRowDetails> mRestrictionRows;
         long mMemberId;
         String mMemberName;
         boolean changed = false;
 
         public RestrictionBuilder(long _memberId, String _memberName) {
             mMemberId = _memberId;
             mMemberName = _memberName;
         }
 
         public void setRestrictions(final List<RestrictionRowDetails> newRestrictions) {
             Log.v(TAG, "setRestrictions() - start");
 
             AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
 
                 @Override
                 protected Void doInBackground(Void... params) {
                     Log.v(TAG, "setRestrictions() - setting changed restrictions for: " + mMemberName + " length: " + newRestrictions.size());
                     mGroup.setReady(false);
                     mDatabase.update(mGroup);
                     mDatabase.deleteAllRestrictionsForMember(mMemberId);
                     for(RestrictionRowDetails rester : newRestrictions) {
                         if(rester.restricted) {
                             Log.v(TAG, "setRestrictions() - restricting: " + rester.toMemberId);
                             Restriction r = new Restriction();
                             Member fromMember = mDatabase.queryById(rester.fromMemberId, Member.class);
                             Member toMember = mDatabase.queryById(rester.toMemberId, Member.class);
                             r.setMember(fromMember);
                             r.setOtherMember(toMember);
                             mDatabase.create(r);
                         }
                     }
                     return null;
                 }
 
                 @Override
                 protected void onPostExecute(Void v) {
                     // Kick off another task.
                     populateMembersList();
                 }
             };
 
             // Only execute and if changed.
             if(changed)
                 task.execute();
 
             Log.v(TAG, "setRestrictions() - end");
         }
 
         /*
          *
          * Builds the full list.
          */
         public List<RestrictionRowDetails> getRestrictionRows(long _memberId) {
             ArrayList<RestrictionRowDetails> rows = new ArrayList<RestrictionRowDetails>();
 
             List<Restriction> restrictionList = mDatabase.queryAllRestrictionsForMemberId(_memberId);
             Log.v(TAG, "getRestrictionRows() - memberId: " + _memberId + " has restriction size: " + restrictionList.size());
 
             List<Member> otherMembers = mDatabase.queryAllMembersForGroupExcept(mGroup.getId(), _memberId);
             for(Member otherMember : otherMembers) {
                 boolean isRestricted = false;
 
                 for(Restriction restriction : restrictionList) {
                     if(restriction.getOtherMemberId() == otherMember.getId()) {
                         isRestricted = true;
                         break;
                     }
                 }
 
                 Log.v(TAG, "getRestrictionRows() - row: " +
                   otherMember.getId() + "(" + otherMember.getName() + ")"
                   + " isRestricted: " + isRestricted);
 
                 rows.add(new RestrictionRowDetails(_memberId, otherMember.getId(), otherMember.getName(), isRestricted));
             }
 
             Collections.sort(rows);
             return rows;
         }
 
         private void openRestrictionsDialog() {
 
             AsyncTask<Void, Void, List<RestrictionRowDetails>> task = new AsyncTask<Void, Void, List<RestrictionRowDetails>>() {
 
                 @Override
                 protected List<RestrictionRowDetails> doInBackground(Void... params) {
                     Log.v(TAG, "populateRestrictionList() - populating restriction list");
                     return getRestrictionRows(mMemberId);
                 }
 
                 @Override
                 protected void onPostExecute(List<RestrictionRowDetails> items) {
                     Log.v(TAG, "populateRestrictionList() - opening restriction list");
 
                     mRestrictionRows = items;
 
                     // Take the values and populate the dialog
                     AlertDialog.Builder builder = new AlertDialog.Builder(MembersListActivity.this);
                     builder.setTitle("Select restrictions for\n" + mMemberName);
 
                     builder.setAdapter(new RestrictionListAdapter(MembersListActivity.this, R.layout.member_restriction_row, items, mRestrictClickListener), null);
                     builder.setCancelable(true);
                     builder.setPositiveButton("OK", mRestrictOkClickListener);
                     builder.setIcon(R.drawable.ic_present_restricted);
 
                     AlertDialog alert = builder.create();
                     alert.setOwnerActivity(MembersListActivity.this);
                     alert.show();
                 }
             };
 
             task.execute();
         }
 
         private View.OnClickListener mRestrictClickListener = new View.OnClickListener() {
             @Override
             public void onClick(View view) {
 
                 Log.v(TAG, "mRestrictClickListener() - start()");
 
                 // TODO Got to be a better way than this.
                 // Should be able to access the list at the end and iterate.
 
                 // The view is the button, which has as tag the memberId to restrict or allow
                 RestrictionRowDetails res = (RestrictionRowDetails) view.getTag();
                 res.restricted = !((CheckBox) view).isChecked();
                 RestrictionBuilder.this.changed = true;
                 Log.v(TAG, "mRestrictClickListener() - res.restricted = " + res.restricted);
             }
         };
 
         private DialogInterface.OnClickListener mRestrictOkClickListener = new DialogInterface.OnClickListener() {
 
             @Override
             public void onClick(DialogInterface dialog, int which) {
 
                 // By doing this here and not on the callback, we don't have to
                 // create/delete a row each time the user toggles.
 
                 switch(which) {
                     case DialogInterface.BUTTON_POSITIVE:
                         setRestrictions(mRestrictionRows);
                         break;
                 }
             }
         };
     }
 }
