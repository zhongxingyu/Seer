 /*
  * Copyright (C) 2007 The Android Open Source Project
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
 
 package com.android.contacts;
 
 import com.android.contacts.ui.DisplayGroupsActivity;
 import com.android.contacts.ui.DisplayGroupsActivity.Prefs;
 import com.android.contacts.util.Constants;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.app.SearchManager;
 import android.content.AsyncQueryHandler;
 import android.content.ContentResolver;
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Resources;
 import android.database.CharArrayBuffer;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.graphics.Typeface;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.os.Parcelable;
 import android.preference.PreferenceManager;
 import android.provider.ContactsContract;
 import android.provider.Settings;
 import android.provider.Contacts.ContactMethods;
 import android.provider.Contacts.People;
 import android.provider.Contacts.PeopleColumns;
 import android.provider.Contacts.Phones;
 import android.provider.ContactsContract.CommonDataKinds;
 import android.provider.ContactsContract.Contacts;
 import android.provider.ContactsContract.Data;
 import android.provider.ContactsContract.Intents;
 import android.provider.ContactsContract.Presence;
 import android.provider.ContactsContract.RawContacts;
 import android.provider.ContactsContract.CommonDataKinds.Email;
 import android.provider.ContactsContract.CommonDataKinds.Phone;
 import android.provider.ContactsContract.CommonDataKinds.Photo;
 import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
 import android.provider.ContactsContract.Contacts.AggregationSuggestions;
 import android.provider.ContactsContract.Intents.Insert;
 import android.provider.ContactsContract.Intents.UI;
 import android.telephony.TelephonyManager;
 import android.text.TextUtils;
 import android.util.Log;
 import android.util.SparseArray;
 import android.view.ContextMenu;
 import android.view.ContextThemeWrapper;
 import android.view.Gravity;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AbsListView;
 import android.widget.AdapterView;
 import android.widget.AlphabetIndexer;
 import android.widget.ArrayAdapter;
 import android.widget.FasttrackBadgeWidget;
 import android.widget.Filter;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.ResourceCursorAdapter;
 import android.widget.SectionIndexer;
 import android.widget.TextView;
 import android.widget.AbsListView.OnScrollListener;
 
 import java.lang.ref.SoftReference;
 import java.lang.ref.WeakReference;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Locale;
 
 /*TODO(emillar) I commented most of the code that deals with modes and filtering. It should be
  * brought back in as we add back that functionality.
  */
 
 
 /**
  * Displays a list of contacts. Usually is embedded into the ContactsActivity.
  */
 public final class ContactsListActivity extends ListActivity implements
         View.OnCreateContextMenuListener, View.OnClickListener {
     private static final String TAG = "ContactsListActivity";
 
     private static final boolean ENABLE_ACTION_ICON_OVERLAYS = true;
 
     private static final String LIST_STATE_KEY = "liststate";
     private static final String FOCUS_KEY = "focused";
 
     static final int MENU_ITEM_VIEW_CONTACT = 1;
     static final int MENU_ITEM_CALL = 2;
     static final int MENU_ITEM_EDIT_BEFORE_CALL = 3;
     static final int MENU_ITEM_SEND_SMS = 4;
     static final int MENU_ITEM_SEND_IM = 5;
     static final int MENU_ITEM_EDIT = 6;
     static final int MENU_ITEM_DELETE = 7;
     static final int MENU_ITEM_TOGGLE_STAR = 8;
 
     private static final int SUBACTIVITY_NEW_CONTACT = 1;
     private static final int SUBACTIVITY_VIEW_CONTACT = 2;
     private static final int SUBACTIVITY_DISPLAY_GROUP = 3;
 
     /**
      * The action for the join contact activity.
      * <p>
      * Input: extra field {@link #EXTRA_AGGREGATE_ID} is the aggregate ID.
      *
      * TODO: move to {@link ContactsContract}.
      */
     public static final String JOIN_AGGREGATE =
             "com.android.contacts.action.JOIN_AGGREGATE";
 
     /**
      * Used with {@link #JOIN_AGGREGATE} to give it the target for aggregation.
      * <p>
      * Type: LONG
      */
     public static final String EXTRA_AGGREGATE_ID =
             "com.android.contacts.action.AGGREGATE_ID";
 
     public static final String AUTHORITIES_FILTER_KEY = "authorities";
 
     /** Mask for picker mode */
     static final int MODE_MASK_PICKER = 0x80000000;
     /** Mask for no presence mode */
     static final int MODE_MASK_NO_PRESENCE = 0x40000000;
     /** Mask for enabling list filtering */
     static final int MODE_MASK_NO_FILTER = 0x20000000;
     /** Mask for having a "create new contact" header in the list */
     static final int MODE_MASK_CREATE_NEW = 0x10000000;
     /** Mask for showing photos in the list */
     static final int MODE_MASK_SHOW_PHOTOS = 0x08000000;
     /** Mask for hiding additional information e.g. primary phone number in the list */
     static final int MODE_MASK_NO_DATA = 0x04000000;
     /** Mask for showing a call button in the list */
     static final int MODE_MASK_SHOW_CALL_BUTTON = 0x02000000;
 
     /** Unknown mode */
     static final int MODE_UNKNOWN = 0;
     /** Default mode */
     static final int MODE_DEFAULT = 4 | MODE_MASK_SHOW_PHOTOS;
     /** Custom mode */
     static final int MODE_CUSTOM = 8;
     /** Show all starred contacts */
     static final int MODE_STARRED = 20 | MODE_MASK_SHOW_PHOTOS;
     /** Show frequently contacted contacts */
     static final int MODE_FREQUENT = 30 | MODE_MASK_SHOW_PHOTOS;
     /** Show starred and the frequent */
     static final int MODE_STREQUENT = 35 | MODE_MASK_SHOW_PHOTOS | MODE_MASK_SHOW_CALL_BUTTON;
     /** Show all contacts and pick them when clicking */
     static final int MODE_PICK_CONTACT = 40 | MODE_MASK_PICKER | MODE_MASK_SHOW_PHOTOS;
     /** Show all contacts as well as the option to create a new one */
     static final int MODE_PICK_OR_CREATE_CONTACT = 42 | MODE_MASK_PICKER | MODE_MASK_CREATE_NEW;
     /** Show all people through the legacy provider and pick them when clicking */
     static final int MODE_LEGACY_PICK_PERSON = 43 | MODE_MASK_PICKER;
     /** Show all people through the legacy provider as well as the option to create a new one */
     static final int MODE_LEGACY_PICK_OR_CREATE_PERSON = 44 | MODE_MASK_PICKER | MODE_MASK_CREATE_NEW;
     /** Show all contacts and pick them when clicking, and allow creating a new contact */
     static final int MODE_INSERT_OR_EDIT_CONTACT = 45 | MODE_MASK_PICKER | MODE_MASK_CREATE_NEW;
     /** Show all phone numbers and pick them when clicking */
     static final int MODE_PICK_PHONE = 50 | MODE_MASK_PICKER | MODE_MASK_NO_PRESENCE;
     /** Show all phone numbers through the legacy provider and pick them when clicking */
     static final int MODE_LEGACY_PICK_PHONE =
             51 | MODE_MASK_PICKER | MODE_MASK_NO_PRESENCE | MODE_MASK_NO_FILTER;
     /** Show all postal addresses and pick them when clicking */
     static final int MODE_PICK_POSTAL =
             55 | MODE_MASK_PICKER | MODE_MASK_NO_PRESENCE | MODE_MASK_NO_FILTER;
     /** Show all postal addresses and pick them when clicking */
     static final int MODE_LEGACY_PICK_POSTAL =
             56 | MODE_MASK_PICKER | MODE_MASK_NO_PRESENCE | MODE_MASK_NO_FILTER;
     static final int MODE_GROUP = 57 | MODE_MASK_SHOW_PHOTOS;
     /** Run a search query */
     static final int MODE_QUERY = 60 | MODE_MASK_NO_FILTER;
     /** Run a search query in PICK mode, but that still launches to VIEW */
     static final int MODE_QUERY_PICK_TO_VIEW = 65 | MODE_MASK_NO_FILTER | MODE_MASK_PICKER;
 
     /** Show join suggestions followed by an A-Z list */
     static final int MODE_JOIN_CONTACT = 70 | MODE_MASK_PICKER | MODE_MASK_NO_PRESENCE
             | MODE_MASK_NO_DATA | MODE_MASK_SHOW_PHOTOS;
 
     /** Maximum number of suggestions shown for joining aggregates */
     static final int MAX_SUGGESTIONS = 4;
 
     static final String NAME_COLUMN = Contacts.DISPLAY_NAME;
     //static final String SORT_STRING = People.SORT_STRING;
 
     static final String[] CONTACTS_SUMMARY_PROJECTION = new String[] {
         Contacts._ID, // 0
         Contacts.DISPLAY_NAME, // 1
         Contacts.STARRED, //2
         Contacts.TIMES_CONTACTED, //3
         Presence.PRESENCE_STATUS, //4
         Contacts.PHOTO_ID, //5
         Contacts.HAS_PHONE_NUMBER, //6
         Contacts.LOOKUP_KEY, //7
     };
     static final String[] LEGACY_PEOPLE_PROJECTION = new String[] {
         People._ID, // 0
         People.DISPLAY_NAME, // 1
         People.STARRED, //2
         PeopleColumns.TIMES_CONTACTED, //3
         People.PRESENCE_STATUS, //4
     };
     static final int SUMMARY_ID_COLUMN_INDEX = 0;
     static final int SUMMARY_NAME_COLUMN_INDEX = 1;
     static final int SUMMARY_STARRED_COLUMN_INDEX = 2;
     static final int SUMMARY_TIMES_CONTACTED_COLUMN_INDEX = 3;
     static final int SUMMARY_PRESENCE_STATUS_COLUMN_INDEX = 4;
     static final int SUMMARY_PHOTO_ID_COLUMN_INDEX = 5;
     static final int SUMMARY_HAS_PHONE_COLUMN_INDEX = 6;
     static final int SUMMARY_LOOKUP_KEY = 7;
 
     static final String[] PHONES_PROJECTION = new String[] {
         Data._ID, //0
         CommonDataKinds.Phone.TYPE, //1
         CommonDataKinds.Phone.LABEL, //2
         CommonDataKinds.Phone.NUMBER, //3
         Contacts.DISPLAY_NAME, // 4
         RawContacts.CONTACT_ID, // 5
     };
     static final String[] LEGACY_PHONES_PROJECTION = new String[] {
         Phones._ID, //0
         Phones.TYPE, //1
         Phones.LABEL, //2
         Phones.NUMBER, //3
         People.DISPLAY_NAME, // 4
     };
     static final int PHONE_ID_COLUMN_INDEX = 0;
     static final int PHONE_TYPE_COLUMN_INDEX = 1;
     static final int PHONE_LABEL_COLUMN_INDEX = 2;
     static final int PHONE_NUMBER_COLUMN_INDEX = 3;
     static final int PHONE_DISPLAY_NAME_COLUMN_INDEX = 4;
     static final int PHONE_CONTACT_ID_COLUMN_INDEX = 5;
 
     static final String[] POSTALS_PROJECTION = new String[] {
         Data._ID, //0
         CommonDataKinds.StructuredPostal.TYPE, //1
         CommonDataKinds.StructuredPostal.LABEL, //2
         CommonDataKinds.StructuredPostal.DATA, //3
         Contacts.DISPLAY_NAME, // 4
     };
     static final String[] LEGACY_POSTALS_PROJECTION = new String[] {
         ContactMethods._ID, //0
         ContactMethods.TYPE, //1
         ContactMethods.LABEL, //2
         ContactMethods.DATA, //3
         People.DISPLAY_NAME, // 4
     };
     static final int POSTAL_ID_COLUMN_INDEX = 0;
     static final int POSTAL_TYPE_COLUMN_INDEX = 1;
     static final int POSTAL_LABEL_COLUMN_INDEX = 2;
     static final int POSTAL_ADDRESS_COLUMN_INDEX = 3;
     static final int POSTAL_DISPLAY_NAME_COLUMN_INDEX = 4;
 
     private static final int QUERY_TOKEN = 42;
 
     private ContactItemListAdapter mAdapter;
 
     int mMode = MODE_DEFAULT;
 
     private QueryHandler mQueryHandler;
     private String mQuery;
     private boolean mJustCreated;
     private boolean mSyncEnabled;
 
 //    private boolean mDisplayAll;
     private boolean mDisplayOnlyPhones;
 
     private Uri mGroupUri;
 
     private long mQueryAggregateId;
 
     /**
      * Used to keep track of the scroll state of the list.
      */
     private Parcelable mListState = null;
     private boolean mListHasFocus;
 
     private String mShortcutAction;
 
     private int mScrollState;
 
     /**
      * Internal query type when in mode {@link #MODE_QUERY_PICK_TO_VIEW}.
      */
     private int mQueryMode = QUERY_MODE_NONE;
 
     private static final int QUERY_MODE_NONE = -1;
     private static final int QUERY_MODE_MAILTO = 1;
     private static final int QUERY_MODE_TEL = 2;
 
     /**
      * Data to use when in mode {@link #MODE_QUERY_PICK_TO_VIEW}. Usually
      * provided by scheme-specific part of incoming {@link Intent#getData()}.
      */
     private String mQueryData;
 
     private Handler mHandler = new Handler();
 
     private static final String CLAUSE_ONLY_VISIBLE = Contacts.IN_VISIBLE_GROUP + "=1";
     private static final String CLAUSE_ONLY_PHONES = Contacts.HAS_PHONE_NUMBER + "=1";
 
     private class DeleteClickListener implements DialogInterface.OnClickListener {
         private Uri mUri;
 
         public DeleteClickListener(Uri uri) {
             mUri = uri;
         }
 
         public void onClick(DialogInterface dialog, int which) {
             getContentResolver().delete(mUri, null, null);
         }
     }
 
     @Override
     protected void onCreate(Bundle icicle) {
         super.onCreate(icicle);
 
         // Resolve the intent
         final Intent intent = getIntent();
 
         // Allow the title to be set to a custom String using an extra on the intent
         String title = intent.getStringExtra(UI.TITLE_EXTRA_KEY);
         if (title != null) {
             setTitle(title);
         }
 
         final String action = intent.getAction();
         mMode = MODE_UNKNOWN;
 
         setContentView(R.layout.contacts_list_content);
 
         Log.i(TAG, "Called with action: " + action);
         if (UI.LIST_DEFAULT.equals(action)) {
             mMode = MODE_DEFAULT;
             // When mDefaultMode is true the mode is set in onResume(), since the preferneces
             // activity may change it whenever this activity isn't running
         } else if (UI.LIST_GROUP_ACTION.equals(action)) {
             mMode = MODE_GROUP;
             String groupName = intent.getStringExtra(UI.GROUP_NAME_EXTRA_KEY);
             if (TextUtils.isEmpty(groupName)) {
                 finish();
                 return;
             }
             buildUserGroupUri(groupName);
         } else if (UI.LIST_ALL_CONTACTS_ACTION.equals(action)) {
             mMode = MODE_CUSTOM;
             mDisplayOnlyPhones = false;
         } else if (UI.LIST_STARRED_ACTION.equals(action)) {
             mMode = MODE_STARRED;
         } else if (UI.LIST_FREQUENT_ACTION.equals(action)) {
             mMode = MODE_FREQUENT;
         } else if (UI.LIST_STREQUENT_ACTION.equals(action)) {
             mMode = MODE_STREQUENT;
         } else if (UI.LIST_CONTACTS_WITH_PHONES_ACTION.equals(action)) {
             mMode = MODE_CUSTOM;
             mDisplayOnlyPhones = true;
         } else if (Intent.ACTION_PICK.equals(action)) {
             // XXX These should be showing the data from the URI given in
             // the Intent.
             final String type = intent.resolveType(this);
             if (Contacts.CONTENT_TYPE.equals(type)) {
                 mMode = MODE_PICK_CONTACT;
             } else if (People.CONTENT_TYPE.equals(type)) {
                 mMode = MODE_LEGACY_PICK_PERSON;
             } else if (Phone.CONTENT_TYPE.equals(type)) {
                 mMode = MODE_PICK_PHONE;
             } else if (Phones.CONTENT_TYPE.equals(type)) {
                 mMode = MODE_LEGACY_PICK_PHONE;
             } else if (StructuredPostal.CONTENT_TYPE.equals(type)) {
                 mMode = MODE_PICK_POSTAL;
             } else if (ContactMethods.CONTENT_POSTAL_TYPE.equals(type)) {
                 mMode = MODE_LEGACY_PICK_POSTAL;
             }
         } else if (Intent.ACTION_CREATE_SHORTCUT.equals(action)) {
             if (intent.getComponent().getClassName().equals("alias.DialShortcut")) {
                 mMode = MODE_PICK_PHONE;
                 mShortcutAction = Intent.ACTION_CALL;
                 setTitle(R.string.callShortcutActivityTitle);
             } else if (intent.getComponent().getClassName().equals("alias.MessageShortcut")) {
                 mMode = MODE_PICK_PHONE;
                 mShortcutAction = Intent.ACTION_SENDTO;
                 setTitle(R.string.messageShortcutActivityTitle);
             } else {
                 mMode = MODE_PICK_OR_CREATE_CONTACT;
                 mShortcutAction = Intent.ACTION_VIEW;
                 setTitle(R.string.shortcutActivityTitle);
             }
         } else if (Intent.ACTION_GET_CONTENT.equals(action)) {
             final String type = intent.resolveType(this);
             if (Contacts.CONTENT_ITEM_TYPE.equals(type)) {
                 mMode = MODE_PICK_OR_CREATE_CONTACT;
             } else if (Phone.CONTENT_ITEM_TYPE.equals(type)) {
                 mMode = MODE_PICK_PHONE;
             } else if (Phones.CONTENT_TYPE.equals(type)) {
                 mMode = MODE_LEGACY_PICK_PHONE;
             } else if (StructuredPostal.CONTENT_ITEM_TYPE.equals(type)) {
                 mMode = MODE_PICK_POSTAL;
             } else if (ContactMethods.CONTENT_POSTAL_TYPE.equals(type)) {
                 mMode = MODE_LEGACY_PICK_POSTAL;
             }
         } else if (Intent.ACTION_INSERT_OR_EDIT.equals(action)) {
             mMode = MODE_INSERT_OR_EDIT_CONTACT;
         } else if (Intent.ACTION_SEARCH.equals(action)) {
             // See if the suggestion was clicked with a search action key (call button)
             if ("call".equals(intent.getStringExtra(SearchManager.ACTION_MSG))) {
                 String query = intent.getStringExtra(SearchManager.QUERY);
                 if (!TextUtils.isEmpty(query)) {
                     Intent newIntent = new Intent(Intent.ACTION_CALL_PRIVILEGED,
                             Uri.fromParts("tel", query, null));
                     startActivity(newIntent);
                 }
                 finish();
                 return;
             }
 
             // See if search request has extras to specify query
             if (intent.hasExtra(Insert.EMAIL)) {
                 mMode = MODE_QUERY_PICK_TO_VIEW;
                 mQueryMode = QUERY_MODE_MAILTO;
                 mQueryData = intent.getStringExtra(Insert.EMAIL);
             } else if (intent.hasExtra(Insert.PHONE)) {
                 mMode = MODE_QUERY_PICK_TO_VIEW;
                 mQueryMode = QUERY_MODE_TEL;
                 mQueryData = intent.getStringExtra(Insert.PHONE);
             } else {
                 // Otherwise handle the more normal search case
                 mMode = MODE_QUERY;
             }
             mMode = MODE_QUERY;
 
         // Since this is the filter activity it receives all intents
         // dispatched from the SearchManager for security reasons
         // so we need to re-dispatch from here to the intended target.
         } else if (Intents.SEARCH_SUGGESTION_CLICKED.equals(action)) {
             // See if the suggestion was clicked with a search action key (call button)
             Intent newIntent;
             if ("call".equals(intent.getStringExtra(SearchManager.ACTION_MSG))) {
                 newIntent = new Intent(Intent.ACTION_CALL_PRIVILEGED, intent.getData());
             } else {
                 newIntent = new Intent(Intent.ACTION_VIEW, intent.getData());
             }
             startActivity(newIntent);
             finish();
             return;
         } else if (Intents.SEARCH_SUGGESTION_DIAL_NUMBER_CLICKED.equals(action)) {
             Intent newIntent = new Intent(Intent.ACTION_CALL_PRIVILEGED, intent.getData());
             startActivity(newIntent);
             finish();
             return;
         } else if (Intents.SEARCH_SUGGESTION_CREATE_CONTACT_CLICKED.equals(action)) {
             // TODO actually support this in EditContactActivity.
             String number = intent.getData().getSchemeSpecificPart();
             Intent newIntent = new Intent(Intent.ACTION_INSERT, Contacts.CONTENT_URI);
             newIntent.putExtra(Intents.Insert.PHONE, number);
             startActivity(newIntent);
             finish();
             return;
         }
 
         if (JOIN_AGGREGATE.equals(action)) {
             mMode = MODE_JOIN_CONTACT;
             mQueryAggregateId = intent.getLongExtra(EXTRA_AGGREGATE_ID, -1);
             if (mQueryAggregateId == -1) {
                 Log.e(TAG, "Intent " + action + " is missing required extra: "
                         + EXTRA_AGGREGATE_ID);
                 setResult(RESULT_CANCELED);
                 finish();
             }
 
             setTitle(R.string.titleJoinAggregate);
         }
 
         if (mMode == MODE_UNKNOWN) {
             mMode = MODE_DEFAULT;
         }
 
         // Setup the UI
         final ListView list = getListView();
         // Tell list view to not show dividers. We'll do it ourself so that we can *not* show
         // them when an A-Z headers is visible.
         list.setDividerHeight(0);
         list.setFocusable(true);
         list.setOnCreateContextMenuListener(this);
         if ((mMode & MODE_MASK_NO_FILTER) != MODE_MASK_NO_FILTER) {
             list.setTextFilterEnabled(true);
         }
 
         if ((mMode & MODE_MASK_CREATE_NEW) != 0) {
             // Add the header for creating a new contact
             final LayoutInflater inflater = getLayoutInflater();
             View header = inflater.inflate(android.R.layout.simple_list_item_1, list, false);
             TextView text = (TextView) header.findViewById(android.R.id.text1);
             text.setText(R.string.pickerNewContactHeader);
             list.addHeaderView(header);
         }
 
         // Set the proper empty string
         setEmptyText();
 
         mAdapter = new ContactItemListAdapter(this);
         setListAdapter(mAdapter);
         getListView().setOnScrollListener(mAdapter);
 
         // We manually save/restore the listview state
         list.setSaveEnabled(false);
 
         if (mMode == MODE_JOIN_CONTACT) {
             mQueryHandler = new SuggestionsQueryHandler(this, mQueryAggregateId);
         } else {
             mQueryHandler = new QueryHandler(this);
         }
         mJustCreated = true;
 
         // TODO(jham) redesign this
         mSyncEnabled = true;
 //        // Check to see if sync is enabled
 //        final ContentResolver resolver = getContentResolver();
 //        IContentProvider provider = resolver.acquireProvider(Contacts.CONTENT_URI);
 //        if (provider == null) {
 //            // No contacts provider, bail.
 //            finish();
 //            return;
 //        }
 //
 //        try {
 //            ISyncAdapter sa = provider.getSyncAdapter();
 //            mSyncEnabled = sa != null;
 //        } catch (RemoteException e) {
 //            mSyncEnabled = false;
 //        } finally {
 //            resolver.releaseProvider(provider);
 //        }
     }
 
     private int[] mLocation = new int[2];
     private Rect mRect = new Rect();
 
     /** {@inheritDoc} */
     public void onClick(View v) {
         if (v.getId() == R.id.call_button) {
             final int position = (Integer) v.getTag();
             Cursor c =  (Cursor) mAdapter.getItem(position);
             if (c != null) {
                 callContact(c);
             }
         }
     }
 
     private void setEmptyText() {
         TextView empty = (TextView) findViewById(R.id.emptyText);
         // Center the text by default
         int gravity = Gravity.CENTER;
 
         if (mDisplayOnlyPhones) {
             empty.setText(getText(R.string.noContactsWithPhoneNumbers));
         } else if (mMode == MODE_STREQUENT || mMode == MODE_STARRED) {
             empty.setText(getText(R.string.noFavoritesHelpText));
         } else {
             boolean hasSim = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE))
                     .hasIccCard();
 
             if (hasSim) {
                 if (mSyncEnabled) {
                     empty.setText(getText(R.string.noContactsHelpTextWithSync));
                 } else {
                     empty.setText(getText(R.string.noContactsHelpText));
                 }
             } else {
                 if (mSyncEnabled) {
                     empty.setText(getText(R.string.noContactsNoSimHelpTextWithSync));
                 } else {
                     empty.setText(getText(R.string.noContactsNoSimHelpText));
                 }
             }
             gravity = Gravity.NO_GRAVITY;
         }
         empty.setGravity(gravity);
     }
 
     private void buildUserGroupUri(String group) {
         mGroupUri = Uri.withAppendedPath(Contacts.CONTENT_GROUP_URI, group);
     }
 
     /**
      * Sets the mode when the request is for "default"
      */
     private void setDefaultMode() {
         // Load the preferences
         SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 
         mDisplayOnlyPhones = prefs.getBoolean(Prefs.DISPLAY_ONLY_PHONES,
                 Prefs.DISPLAY_ONLY_PHONES_DEFAULT);
 
         // Update the empty text view with the proper string, as the group may have changed
         setEmptyText();
     }
 
     @Override
     protected void onResume() {
         super.onResume();
 
         mScrollState = OnScrollListener.SCROLL_STATE_IDLE;
         boolean runQuery = true;
         Activity parent = getParent();
 
         // Do this before setting the filter. The filter thread relies
         // on some state that is initialized in setDefaultMode
         if (mMode == MODE_DEFAULT) {
             // If we're in default mode we need to possibly reset the mode due to a change
             // in the preferences activity while we weren't running
             setDefaultMode();
         }
 
         // See if we were invoked with a filter
         if (parent != null && parent instanceof DialtactsActivity) {
             String filterText = ((DialtactsActivity) parent).getAndClearFilterText();
             if (filterText != null && filterText.length() > 0) {
                 getListView().setFilterText(filterText);
                 // Don't start a new query since it will conflict with the filter
                 runQuery = false;
             } else if (mJustCreated) {
                 getListView().clearTextFilter();
             }
         }
 
         if (mJustCreated && runQuery) {
             // We need to start a query here the first time the activity is launched, as long
             // as we aren't doing a filter.
             startQuery();
         }
         mJustCreated = false;
     }
 
     @Override
     protected void onRestart() {
         super.onRestart();
 
         // The cursor was killed off in onStop(), so we need to get a new one here
         // We do not perform the query if a filter is set on the list because the
         // filter will cause the query to happen anyway
         if (TextUtils.isEmpty(getListView().getTextFilter())) {
             startQuery();
         } else {
             // Run the filtered query on the adapter
             ((ContactItemListAdapter) getListAdapter()).onContentChanged();
         }
     }
 
     @Override
     protected void onSaveInstanceState(Bundle icicle) {
         super.onSaveInstanceState(icicle);
         // Save list state in the bundle so we can restore it after the QueryHandler has run
         icicle.putParcelable(LIST_STATE_KEY, mList.onSaveInstanceState());
         icicle.putBoolean(FOCUS_KEY, mList.hasFocus());
     }
 
     @Override
     protected void onRestoreInstanceState(Bundle icicle) {
         super.onRestoreInstanceState(icicle);
         // Retrieve list state. This will be applied after the QueryHandler has run
         mListState = icicle.getParcelable(LIST_STATE_KEY);
         mListHasFocus = icicle.getBoolean(FOCUS_KEY);
     }
 
     @Override
     protected void onStop() {
         super.onStop();
 
         // We don't want the list to display the empty state, since when we resume it will still
         // be there and show up while the new query is happening. After the async query finished
         // in response to onRestart() setLoading(false) will be called.
         mAdapter.setLoading(true);
         mAdapter.changeCursor(null);
         mAdapter.clearImageFetching();
 
         if (mMode == MODE_QUERY) {
             // Make sure the search box is closed
             SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
             searchManager.stopSearch();
         }
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
 
         // If Contacts was invoked by another Activity simply as a way of
         // picking a contact, don't show the options menu
         if ((mMode & MODE_MASK_PICKER) == MODE_MASK_PICKER) {
             return false;
         }
 
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.list, menu);
         return true;
     }
 
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
         final boolean defaultMode = (mMode == MODE_DEFAULT);
         menu.findItem(R.id.menu_display_groups).setVisible(defaultMode);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.menu_display_groups: {
                 final Intent intent = new Intent(this, DisplayGroupsActivity.class);
                 startActivityForResult(intent, SUBACTIVITY_DISPLAY_GROUP);
                 return true;
             }
             case R.id.menu_search: {
                 startSearch(null, false, null, false);
                 return true;
             }
             case R.id.menu_add: {
                 final Intent intent = new Intent(Intent.ACTION_INSERT, Contacts.CONTENT_URI);
                 startActivity(intent);
                 return true;
             }
             case R.id.menu_import_export: {
                 showDialog(R.id.dialog_import_export);
                 return true;
             }
             case R.id.menu_accounts: {
                 final Intent intent = new Intent(Settings.ACTION_SYNC_SETTINGS);
                 intent.putExtra(AUTHORITIES_FILTER_KEY, new String[] {
                     ContactsContract.AUTHORITY
                 });
                 startActivity(intent);
                 return true;
             }
         }
         return false;
     }
 
     @Override
     protected Dialog onCreateDialog(int id) {
         switch (id) {
             case R.id.dialog_import_export: {
                 return createImportExportDialog();
             }
         }
         return super.onCreateDialog(id);
     }
 
     /**
      * Create a {@link Dialog} that allows the user to pick from a bulk import
      * or bulk export task across all contacts.
      */
     private Dialog createImportExportDialog() {
         // Wrap our context to inflate list items using correct theme
         final Context dialogContext = new ContextThemeWrapper(this, android.R.style.Theme_Light);
         final Resources res = dialogContext.getResources();
         final LayoutInflater dialogInflater = (LayoutInflater)dialogContext
                 .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
         // Adapter that shows a list of string resources
         final ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(this,
                 android.R.layout.simple_list_item_1) {
             @Override
             public View getView(int position, View convertView, ViewGroup parent) {
                 if (convertView == null) {
                     convertView = dialogInflater.inflate(android.R.layout.simple_list_item_1,
                             parent, false);
                 }
 
                 final int resId = this.getItem(position);
                 ((TextView)convertView).setText(resId);
                 return convertView;
             }
         };
 
         if (TelephonyManager.getDefault().hasIccCard()) {
             adapter.add(R.string.import_from_sim);
         }
         if (res.getBoolean(R.bool.config_allow_import_from_sdcard)) {
             adapter.add(R.string.import_from_sdcard);
         }
         if (res.getBoolean(R.bool.config_allow_export_to_sdcard)) {
             adapter.add(R.string.export_to_sdcard);
         }
 
         final DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int which) {
                 dialog.dismiss();
 
                 final int resId = adapter.getItem(which);
                 switch (resId) {
                     case R.string.import_from_sim: {
                         doImportFromSim();
                         break;
                     }
                     case R.string.import_from_sdcard: {
                         doImportFromSdCard();
                         break;
                     }
                     case R.string.export_to_sdcard: {
                         doExportToSdCard();
                         break;
                     }
                 }
             }
         };
 
         final AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setTitle(R.string.dialog_import_export);
         builder.setNegativeButton(android.R.string.cancel, null);
         builder.setSingleChoiceItems(adapter, -1, clickListener);
         return builder.create();
     }
 
     private void doImportFromSim() {
         Intent importIntent = new Intent(Intent.ACTION_VIEW);
         importIntent.setType("vnd.android.cursor.item/sim-contact");
         importIntent.setClassName("com.android.phone", "com.android.phone.SimContacts");
         startActivity(importIntent);
     }
 
     private void doImportFromSdCard() {
         Intent intent = new Intent(this, ImportVCardActivity.class);
         startActivity(intent);
     }
 
     private void doExportToSdCard() {
         VCardExporter exporter = new VCardExporter(ContactsListActivity.this, mHandler);
         exporter.startExportVCardToSdCard();
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode,
             Intent data) {
         switch (requestCode) {
             case SUBACTIVITY_NEW_CONTACT:
                 if (resultCode == RESULT_OK) {
                     // Contact was created, pass it back
                     returnPickerResult(null, data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME),
                             data.getData(), 0);
                 }
                 break;
 
             case SUBACTIVITY_VIEW_CONTACT:
                 if (resultCode == RESULT_OK) {
                     mAdapter.notifyDataSetChanged();
                 }
                 break;
 
             case SUBACTIVITY_DISPLAY_GROUP:
                 // Mark as just created so we re-run the view query
                 mJustCreated = true;
                 break;
         }
     }
 
     @Override
     public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
         // If Contacts was invoked by another Activity simply as a way of
         // picking a contact, don't show the context menu
         if ((mMode & MODE_MASK_PICKER) == MODE_MASK_PICKER) {
             return;
         }
 
         AdapterView.AdapterContextMenuInfo info;
         try {
              info = (AdapterView.AdapterContextMenuInfo) menuInfo;
         } catch (ClassCastException e) {
             Log.e(TAG, "bad menuInfo", e);
             return;
         }
 
         Cursor cursor = (Cursor) getListAdapter().getItem(info.position);
         if (cursor == null) {
             // For some reason the requested item isn't available, do nothing
             return;
         }
         long id = info.id;
         Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, id);
         long rawContactId = ContactsUtils.queryForRawContactId(getContentResolver(), id);
         Uri rawContactUri = ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawContactId);
 
         // Setup the menu header
         menu.setHeaderTitle(cursor.getString(SUMMARY_NAME_COLUMN_INDEX));
 
         // View contact details
         menu.add(0, MENU_ITEM_VIEW_CONTACT, 0, R.string.menu_viewContact)
                 .setIntent(new Intent(Intent.ACTION_VIEW, contactUri));
 
         if (cursor.getInt(SUMMARY_HAS_PHONE_COLUMN_INDEX) != 0) {
             // Calling contact
             menu.add(0, MENU_ITEM_CALL, 0,
                     getString(R.string.menu_call));
             // Send SMS item
             menu.add(0, MENU_ITEM_SEND_SMS, 0, getString(R.string.menu_sendSMS));
         }
 
         // Star toggling
         int starState = cursor.getInt(SUMMARY_STARRED_COLUMN_INDEX);
         if (starState == 0) {
             menu.add(0, MENU_ITEM_TOGGLE_STAR, 0, R.string.menu_addStar);
         } else {
             menu.add(0, MENU_ITEM_TOGGLE_STAR, 0, R.string.menu_removeStar);
         }
 
         // Contact editing
         menu.add(0, MENU_ITEM_EDIT, 0, R.string.menu_editContact)
                 .setIntent(new Intent(Intent.ACTION_EDIT, rawContactUri));
         menu.add(0, MENU_ITEM_DELETE, 0, R.string.menu_deleteContact);
     }
 
     @Override
     public boolean onContextItemSelected(MenuItem item) {
         AdapterView.AdapterContextMenuInfo info;
         try {
              info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
         } catch (ClassCastException e) {
             Log.e(TAG, "bad menuInfo", e);
             return false;
         }
 
         Cursor cursor = (Cursor) getListAdapter().getItem(info.position);
 
         switch (item.getItemId()) {
             case MENU_ITEM_TOGGLE_STAR: {
                 // Toggle the star
                 ContentValues values = new ContentValues(1);
                 values.put(Contacts.STARRED, cursor.getInt(SUMMARY_STARRED_COLUMN_INDEX) == 0 ? 1 : 0);
                 final Uri selectedUri = this.getContactUri(info.position);
                 getContentResolver().update(selectedUri, values, null, null);
                 return true;
             }
 
             case MENU_ITEM_CALL: {
                 callContact(cursor);
                 return true;
             }
 
             case MENU_ITEM_SEND_SMS: {
                 smsContact(cursor);
                 return true;
             }
 
             case MENU_ITEM_DELETE: {
                 final Uri selectedUri = getContactUri(info.position);
                 doContactDelete(selectedUri);
                 return true;
             }
         }
 
         return super.onContextItemSelected(item);
     }
 
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         switch (keyCode) {
             case KeyEvent.KEYCODE_CALL: {
                 if (callSelection()) {
                     return true;
                 }
                 break;
             }
             case KeyEvent.KEYCODE_DEL: {
                 final int position = getListView().getSelectedItemPosition();
                 if (position != ListView.INVALID_POSITION) {
                     final Uri selectedUri = getContactUri(position);
                     doContactDelete(selectedUri);
                     return true;
                 }
                 break;
             }
         }
 
         return super.onKeyDown(keyCode, event);
     }
 
     /**
      * Prompt the user before deleting the given {@link Contacts} entry.
      */
     protected void doContactDelete(Uri contactUri) {
         new AlertDialog.Builder(ContactsListActivity.this)
             .setTitle(R.string.deleteConfirmation_title)
             .setIcon(android.R.drawable.ic_dialog_alert)
             .setMessage(R.string.deleteConfirmation)
             .setNegativeButton(android.R.string.cancel, null)
             .setPositiveButton(android.R.string.ok, new DeleteClickListener(contactUri))
             .show();
     }
 
     @Override
     protected void onListItemClick(ListView l, View v, int position, long id) {
         // Hide soft keyboard, if visible
         InputMethodManager inputMethodManager = (InputMethodManager)
                 getSystemService(Context.INPUT_METHOD_SERVICE);
         inputMethodManager.hideSoftInputFromWindow(mList.getWindowToken(), 0);
 
         if (mMode == MODE_INSERT_OR_EDIT_CONTACT) {
             Intent intent;
             if (position == 0) {
                 // Insert
                 intent = new Intent(Intent.ACTION_INSERT, Contacts.CONTENT_URI);
             } else {
                 // Edit
                 final Uri uri = getSelectedUri(position);
                 intent = new Intent(Intent.ACTION_EDIT, uri);
             }
             intent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
             final Bundle extras = getIntent().getExtras();
             if (extras != null) {
                 intent.putExtras(extras);
             }
             startActivity(intent);
             finish();
         } else if (id != -1) {
             final Uri uri = getSelectedUri(position);
             if ((mMode & MODE_MASK_PICKER) == 0) {
                 final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                 startActivityForResult(intent, SUBACTIVITY_VIEW_CONTACT);
             } else if (mMode == MODE_JOIN_CONTACT) {
                 returnPickerResult(null, null, uri, id);
             } else if (mMode == MODE_QUERY_PICK_TO_VIEW) {
                 // Started with query that should launch to view contact
                 final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                 startActivity(intent);
                 finish();
             } else if (mMode == MODE_PICK_CONTACT
                     || mMode == MODE_PICK_OR_CREATE_CONTACT
                     || mMode == MODE_LEGACY_PICK_PERSON
                     || mMode == MODE_LEGACY_PICK_OR_CREATE_PERSON) {
                 if (mShortcutAction != null) {
                    // Subtract one if we have Create Contact at the top
                    Cursor c = (Cursor) mAdapter.getItem(position
                            - (mMode == MODE_PICK_OR_CREATE_CONTACT
                                    || mMode == MODE_LEGACY_PICK_OR_CREATE_PERSON ? 1:0));
                     returnPickerResult(c, c.getString(SUMMARY_NAME_COLUMN_INDEX), uri, id);
                 } else {
                     returnPickerResult(null, null, uri, id);
                 }
             } else if (mMode == MODE_PICK_PHONE) {
                 if (mShortcutAction != null) {
                     Cursor c = (Cursor) mAdapter.getItem(position);
                     returnPickerResult(c, c.getString(PHONE_DISPLAY_NAME_COLUMN_INDEX), uri, id);
                 } else {
                     returnPickerResult(null, null, uri, id);
                 }
             } else if (mMode == MODE_PICK_POSTAL
                     || mMode == MODE_LEGACY_PICK_POSTAL
                     || mMode == MODE_LEGACY_PICK_PHONE) {
                 returnPickerResult(null, null, uri, id);
             }
         } else if ((mMode & MODE_MASK_CREATE_NEW) == MODE_MASK_CREATE_NEW
                 && position == 0) {
             // TODO: Hook this up to new edit contact activity (bug 2092559)
             /*Intent newContact = new Intent(Intents.Insert.ACTION, People.CONTENT_URI);
             startActivityForResult(newContact, SUBACTIVITY_NEW_CONTACT);*/
         } else {
             signalError();
         }
     }
 
     /**
      * @param uri In most cases, this should be a lookup {@link Uri}, possibly
      *            generated through {@link Contacts#getLookupUri(long, String)}.
      */
     private void returnPickerResult(Cursor c, String name, Uri uri, long id) {
         final Intent intent = new Intent();
 
         if (mShortcutAction != null) {
             Intent shortcutIntent;
             if (Intent.ACTION_VIEW.equals(mShortcutAction)) {
                 // This is a simple shortcut to view a contact.
                 shortcutIntent = new Intent(mShortcutAction, uri);
                 final Bitmap icon = loadContactPhoto(id, null);
                 if (icon != null) {
                     intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, icon);
                 } else {
                     intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                             Intent.ShortcutIconResource.fromContext(this,
                                     R.drawable.ic_launcher_shortcut_contact));
                 }
             } else {
                 // This is a direct dial or sms shortcut.
                 String number = c.getString(PHONE_NUMBER_COLUMN_INDEX);
                 int type = c.getInt(PHONE_TYPE_COLUMN_INDEX);
                 String scheme;
                 int resid;
                 if (Intent.ACTION_CALL.equals(mShortcutAction)) {
                     scheme = Constants.SCHEME_TEL;
                     resid = R.drawable.badge_action_call;
                 } else {
                     scheme = Constants.SCHEME_SMSTO;
                     resid = R.drawable.badge_action_sms;
                 }
 
                 // Make the URI a direct tel: URI so that it will always continue to work
                 Uri phoneUri = Uri.fromParts(scheme, number, null);
                 shortcutIntent = new Intent(mShortcutAction, phoneUri);
 
                 // Find the Contacts._ID for this phone number
                 long contactId = c.getLong(PHONE_CONTACT_ID_COLUMN_INDEX);
                 intent.putExtra(Intent.EXTRA_SHORTCUT_ICON,
                         generatePhoneNumberIcon(contactId, type, resid));
             }
             shortcutIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
             intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
             intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);
             setResult(RESULT_OK, intent);
         } else {
             setResult(RESULT_OK, intent.setData(uri));
         }
         finish();
     }
 
     /**
      * Generates a phone number shortcut icon. Adds an overlay describing the type of the phone
      * number, and if there is a photo also adds the call action icon.
      *
      * @param contactId The person the phone number belongs to
      * @param type The type of the phone number
      * @param actionResId The ID for the action resource
      * @return The bitmap for the icon
      */
     private Bitmap generatePhoneNumberIcon(long contactId, int type, int actionResId) {
         final Resources r = getResources();
         boolean drawPhoneOverlay = true;
 
         Bitmap photo = loadContactPhoto(contactId, null);
         if (photo == null) {
             // If there isn't a photo use the generic phone action icon instead
             Bitmap phoneIcon = getPhoneActionIcon(r, actionResId);
             if (phoneIcon != null) {
                 photo = phoneIcon;
                 drawPhoneOverlay = false;
             } else {
                 return null;
             }
         }
 
         // Setup the drawing classes
         int iconSize = (int) r.getDimension(android.R.dimen.app_icon_size);
         Bitmap icon = Bitmap.createBitmap(iconSize, iconSize, Bitmap.Config.ARGB_8888);
         Canvas canvas = new Canvas(icon);
 
         // Copy in the photo
         Paint photoPaint = new Paint();
         photoPaint.setDither(true);
         photoPaint.setFilterBitmap(true);
         Rect src = new Rect(0,0, photo.getWidth(),photo.getHeight());
         Rect dst = new Rect(0,0, iconSize,iconSize);
         canvas.drawBitmap(photo, src, dst, photoPaint);
 
         // Create an overlay for the phone number type
         String overlay = null;
         switch (type) {
             case Phone.TYPE_HOME:
                 overlay = getString(R.string.type_short_home);
                 break;
 
             case Phone.TYPE_MOBILE:
                 overlay = getString(R.string.type_short_mobile);
                 break;
 
             case Phone.TYPE_WORK:
                 overlay = getString(R.string.type_short_work);
                 break;
 
             case Phone.TYPE_PAGER:
                 overlay = getString(R.string.type_short_pager);
                 break;
 
             case Phone.TYPE_OTHER:
                 overlay = getString(R.string.type_short_other);
                 break;
         }
         if (overlay != null) {
             Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DEV_KERN_TEXT_FLAG);
             textPaint.setTextSize(20.0f);
             textPaint.setTypeface(Typeface.DEFAULT_BOLD);
             textPaint.setColor(r.getColor(R.color.textColorIconOverlay));
             textPaint.setShadowLayer(3f, 1, 1, r.getColor(R.color.textColorIconOverlayShadow));
             canvas.drawText(overlay, 2, 16, textPaint);
         }
 
         // Draw the phone action icon as an overlay
         if (ENABLE_ACTION_ICON_OVERLAYS && drawPhoneOverlay) {
             Bitmap phoneIcon = getPhoneActionIcon(r, actionResId);
             if (phoneIcon != null) {
                 src.set(0, 0, phoneIcon.getWidth(), phoneIcon.getHeight());
                 int iconWidth = icon.getWidth();
                 dst.set(iconWidth - 20, -1, iconWidth, 19);
                 canvas.drawBitmap(phoneIcon, src, dst, photoPaint);
             }
         }
 
         return icon;
     }
 
     /**
      * Returns the icon for the phone call action.
      *
      * @param r The resources to load the icon from
      * @param resId The resource ID to load
      * @return the icon for the phone call action
      */
     private Bitmap getPhoneActionIcon(Resources r, int resId) {
         Drawable phoneIcon = r.getDrawable(resId);
         if (phoneIcon instanceof BitmapDrawable) {
             BitmapDrawable bd = (BitmapDrawable) phoneIcon;
             return bd.getBitmap();
         } else {
             return null;
         }
     }
 
     Uri getUriToQuery() {
         switch(mMode) {
             case MODE_JOIN_CONTACT:
             case MODE_FREQUENT:
             case MODE_STARRED:
             case MODE_DEFAULT:
             case MODE_INSERT_OR_EDIT_CONTACT:
             case MODE_PICK_CONTACT:
             case MODE_PICK_OR_CREATE_CONTACT:{
                 return Contacts.CONTENT_URI;
             }
             case MODE_STREQUENT: {
                 return Contacts.CONTENT_STREQUENT_URI;
             }
             case MODE_LEGACY_PICK_PERSON:
             case MODE_LEGACY_PICK_OR_CREATE_PERSON: {
                 return People.CONTENT_URI;
             }
             case MODE_PICK_PHONE: {
                 return Phone.CONTENT_URI;
             }
             case MODE_LEGACY_PICK_PHONE: {
                 return Phones.CONTENT_URI;
             }
             case MODE_PICK_POSTAL: {
                 return StructuredPostal.CONTENT_URI;
             }
             case MODE_LEGACY_PICK_POSTAL: {
                 return ContactMethods.CONTENT_URI;
             }
             case MODE_QUERY_PICK_TO_VIEW: {
                 if (mQueryMode == QUERY_MODE_MAILTO) {
                     return Uri.withAppendedPath(Email.CONTENT_FILTER_URI, Uri.encode(mQueryData));
                 } else if (mQueryMode == QUERY_MODE_TEL) {
                     return Uri.withAppendedPath(Phone.CONTENT_FILTER_URI, Uri.encode(mQueryData));
                 }
             }
         }
         return null;
     }
 
     /**
      * Build the {@link Contacts#CONTENT_LOOKUP_URI} for the given
      * {@link ListView} position, using {@link #mAdapter}.
      */
     private Uri getContactUri(int position) {
         if (position == ListView.INVALID_POSITION) {
             throw new IllegalArgumentException("Position not in list bounds");
         }
 
         final Cursor cursor = (Cursor)mAdapter.getItem(position);
         switch(mMode) {
             case MODE_LEGACY_PICK_PERSON:
             case MODE_LEGACY_PICK_OR_CREATE_PERSON: {
                 final long personId = cursor.getLong(SUMMARY_ID_COLUMN_INDEX);
                 return ContentUris.withAppendedId(People.CONTENT_URI, personId);
             }
 
             default: {
                 // Build and return soft, lookup reference
                 final long contactId = cursor.getLong(SUMMARY_ID_COLUMN_INDEX);
                 final String lookupKey = cursor.getString(SUMMARY_LOOKUP_KEY);
                 return Contacts.getLookupUri(contactId, lookupKey);
             }
         }
     }
 
     /**
      * Build the {@link Uri} for the given {@link ListView} position, which can
      * be used as result when in {@link #MODE_MASK_PICKER} mode.
      */
     private Uri getSelectedUri(int position) {
         if (position == ListView.INVALID_POSITION) {
             throw new IllegalArgumentException("Position not in list bounds");
         }
 
         final long id = mAdapter.getItemId(position);
         switch(mMode) {
             case MODE_LEGACY_PICK_PERSON:
             case MODE_LEGACY_PICK_OR_CREATE_PERSON: {
                 return ContentUris.withAppendedId(People.CONTENT_URI, id);
             }
             case MODE_PICK_PHONE: {
                 return ContentUris.withAppendedId(Data.CONTENT_URI, id);
             }
             case MODE_LEGACY_PICK_PHONE: {
                 return ContentUris.withAppendedId(Phones.CONTENT_URI, id);
             }
             case MODE_PICK_POSTAL: {
                 return ContentUris.withAppendedId(Data.CONTENT_URI, id);
             }
             case MODE_LEGACY_PICK_POSTAL: {
                 return ContentUris.withAppendedId(ContactMethods.CONTENT_URI, id);
             }
             default: {
                 return getContactUri(position);
             }
         }
     }
 
     String[] getProjectionForQuery() {
         switch(mMode) {
             case MODE_JOIN_CONTACT:
             case MODE_STREQUENT:
             case MODE_FREQUENT:
             case MODE_STARRED:
             case MODE_QUERY:
             case MODE_DEFAULT:
             case MODE_INSERT_OR_EDIT_CONTACT:
             case MODE_GROUP:
             case MODE_PICK_CONTACT:
             case MODE_PICK_OR_CREATE_CONTACT: {
                 return CONTACTS_SUMMARY_PROJECTION;
             }
             case MODE_LEGACY_PICK_PERSON:
             case MODE_LEGACY_PICK_OR_CREATE_PERSON: {
                 return LEGACY_PEOPLE_PROJECTION ;
             }
             case MODE_PICK_PHONE: {
                 return PHONES_PROJECTION;
             }
             case MODE_LEGACY_PICK_PHONE: {
                 return LEGACY_PHONES_PROJECTION;
             }
             case MODE_PICK_POSTAL: {
                 return POSTALS_PROJECTION;
             }
             case MODE_LEGACY_PICK_POSTAL: {
                 return LEGACY_POSTALS_PROJECTION;
             }
             case MODE_QUERY_PICK_TO_VIEW: {
                 if (mQueryMode == QUERY_MODE_MAILTO) {
                     return CONTACTS_SUMMARY_PROJECTION;
                 } else if (mQueryMode == QUERY_MODE_TEL) {
                     return PHONES_PROJECTION;
                 }
                 break;
             }
         }
 
         // Default to normal aggregate projection
         return CONTACTS_SUMMARY_PROJECTION;
     }
 
     private Bitmap loadContactPhoto(long contactId, BitmapFactory.Options options) {
         Cursor cursor = null;
         Bitmap bm = null;
         try {
             Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId);
             Uri photoUri = Uri.withAppendedPath(contactUri, Contacts.Photo.CONTENT_DIRECTORY);
             cursor = getContentResolver().query(photoUri, new String[] {Photo.PHOTO},
                     null, null, null);
             if (cursor.moveToFirst()) {
                 bm = ContactsUtils.loadContactPhoto(cursor, 0, options);
             }
         } finally {
             if (cursor != null) {
                 cursor.close();
             }
         }
         return bm;
     }
 
     /**
      * Return the selection arguments for a default query based on
      * {@link #mDisplayAll} and {@link #mDisplayOnlyPhones} flags.
      */
     private String getContactSelection() {
         if (mDisplayOnlyPhones) {
             return CLAUSE_ONLY_VISIBLE + " AND " + CLAUSE_ONLY_PHONES;
         } else {
             return CLAUSE_ONLY_VISIBLE;
         }
     }
 
     private Uri getContactFilterUri(String filter) {
         if (!TextUtils.isEmpty(filter)) {
             return Uri.withAppendedPath(Contacts.CONTENT_FILTER_URI, Uri.encode(filter));
         } else {
             return Contacts.CONTENT_URI;
         }
     }
 
     private Uri getPeopleFilterUri(String filter) {
         if (!TextUtils.isEmpty(filter)) {
             return Uri.withAppendedPath(People.CONTENT_FILTER_URI, Uri.encode(filter));
         } else {
             return People.CONTENT_URI;
         }
     }
 
     private static String getSortOrder(String[] projectionType) {
         /* if (Locale.getDefault().equals(Locale.JAPAN) &&
                 projectionType == AGGREGATES_PRIMARY_PHONE_PROJECTION) {
             return SORT_STRING + " ASC";
         } else {
             return NAME_COLUMN + " COLLATE LOCALIZED ASC";
         } */
 
         return NAME_COLUMN + " COLLATE LOCALIZED ASC";
     }
 
     void startQuery() {
         mAdapter.setLoading(true);
 
         // Cancel any pending queries
         mQueryHandler.cancelOperation(QUERY_TOKEN);
 
         String[] projection = getProjectionForQuery();
         Uri uri = getUriToQuery();
 
         // Kick off the new query
         switch (mMode) {
             case MODE_GROUP:
                 mQueryHandler.startQuery(QUERY_TOKEN, null,
                         mGroupUri, projection, getContactSelection(), null,
                         getSortOrder(projection));
                 break;
 
             case MODE_DEFAULT:
             case MODE_PICK_CONTACT:
             case MODE_PICK_OR_CREATE_CONTACT:
             case MODE_INSERT_OR_EDIT_CONTACT:
                 mQueryHandler.startQuery(QUERY_TOKEN, null, uri,
                         projection, getContactSelection(), null,
                         getSortOrder(projection));
                 break;
 
             case MODE_LEGACY_PICK_PERSON:
             case MODE_LEGACY_PICK_OR_CREATE_PERSON:
                 mQueryHandler.startQuery(QUERY_TOKEN, null, uri,
                         projection, null, null,
                         getSortOrder(projection));
                 break;
 
             case MODE_QUERY: {
                 mQuery = getIntent().getStringExtra(SearchManager.QUERY);
                 mQueryHandler.startQuery(QUERY_TOKEN, null, getContactFilterUri(mQuery),
                         projection, null, null,
                         getSortOrder(projection));
                 break;
             }
 
             case MODE_QUERY_PICK_TO_VIEW: {
                 mQueryHandler.startQuery(QUERY_TOKEN, null, uri, projection, null, null,
                         getSortOrder(projection));
                 break;
             }
 
             case MODE_STARRED:
                 mQueryHandler.startQuery(QUERY_TOKEN, null, uri,
                         projection, Contacts.STARRED + "=1", null,
                         getSortOrder(projection));
                 break;
 
             case MODE_FREQUENT:
                 mQueryHandler.startQuery(QUERY_TOKEN, null, uri,
                         projection,
                         Contacts.TIMES_CONTACTED + " > 0", null,
                         Contacts.TIMES_CONTACTED + " DESC, "
                         + getSortOrder(projection));
                 break;
 
             case MODE_STREQUENT:
                 mQueryHandler.startQuery(QUERY_TOKEN, null, uri, projection, null, null, null);
                 break;
 
             case MODE_PICK_PHONE:
             case MODE_LEGACY_PICK_PHONE:
                 mQueryHandler.startQuery(QUERY_TOKEN, null, uri,
                         projection, null, null, getSortOrder(projection));
                 break;
 
             case MODE_PICK_POSTAL:
                 mQueryHandler.startQuery(QUERY_TOKEN, null, uri,
                         projection, null, null, getSortOrder(projection));
                 break;
 
             case MODE_LEGACY_PICK_POSTAL:
                 mQueryHandler.startQuery(QUERY_TOKEN, null, getUriToQuery(),
                         projection,
                         ContactMethods.KIND + "=" + android.provider.Contacts.KIND_POSTAL, null,
                         getSortOrder(projection));
                 break;
 
             case MODE_JOIN_CONTACT:
                 Uri suggestionsUri = Contacts.CONTENT_URI.buildUpon()
                         .appendEncodedPath(String.valueOf(mQueryAggregateId))
                         .appendEncodedPath(AggregationSuggestions.CONTENT_DIRECTORY)
                         .appendQueryParameter("limit", String.valueOf(MAX_SUGGESTIONS))
                         .build();
                 mQueryHandler.startQuery(QUERY_TOKEN, null, suggestionsUri, projection,
                         null, null, null);
                 break;
         }
     }
 
     /**
      * Called from a background thread to do the filter and return the resulting cursor.
      *
      * @param filter the text that was entered to filter on
      * @return a cursor with the results of the filter
      */
     Cursor doFilter(String filter) {
         final ContentResolver resolver = getContentResolver();
 
         String[] projection = getProjectionForQuery();
 
         switch (mMode) {
             case MODE_DEFAULT:
             case MODE_PICK_CONTACT:
             case MODE_PICK_OR_CREATE_CONTACT:
             case MODE_INSERT_OR_EDIT_CONTACT: {
                 return resolver.query(getContactFilterUri(filter), projection,
                         getContactSelection(), null, getSortOrder(projection));
             }
 
             case MODE_LEGACY_PICK_PERSON:
             case MODE_LEGACY_PICK_OR_CREATE_PERSON: {
                 return resolver.query(getPeopleFilterUri(filter), projection, null, null,
                         getSortOrder(projection));
             }
 
             case MODE_STARRED: {
                 return resolver.query(getContactFilterUri(filter), projection,
                         Contacts.STARRED + "=1", null,
                         getSortOrder(projection));
             }
 
             case MODE_FREQUENT: {
                 return resolver.query(getContactFilterUri(filter), projection,
                         Contacts.TIMES_CONTACTED + " > 0", null,
                         Contacts.TIMES_CONTACTED + " DESC, "
                         + getSortOrder(projection));
             }
 
             case MODE_STREQUENT: {
                 Uri uri;
                 if (!TextUtils.isEmpty(filter)) {
                     uri = Uri.withAppendedPath(Contacts.CONTENT_STREQUENT_FILTER_URI,
                             Uri.encode(filter));
                 } else {
                     uri = Contacts.CONTENT_STREQUENT_URI;
                 }
                 return resolver.query(uri, projection, null, null, null);
             }
 
             case MODE_PICK_PHONE: {
                 Uri uri = getUriToQuery();
                 if (!TextUtils.isEmpty(filter)) {
                     uri = Uri.withAppendedPath(Phone.CONTENT_FILTER_URI, Uri.encode(filter));
                 }
                 return resolver.query(uri, projection, null, null,
                         getSortOrder(projection));
             }
 
             case MODE_LEGACY_PICK_PHONE: {
                 //TODO: Support filtering here (bug 2092503)
             }
         }
         throw new UnsupportedOperationException("filtering not allowed in mode " + mMode);
     }
 
     /**
      * Calls the currently selected list item.
      * @return true if the call was initiated, false otherwise
      */
     boolean callSelection() {
         ListView list = getListView();
         if (list.hasFocus()) {
             Cursor cursor = (Cursor) list.getSelectedItem();
             return callContact(cursor);
         }
         return false;
     }
 
     boolean callContact(Cursor cursor) {
         return callOrSmsContact(cursor, false /*call*/);
     }
 
     boolean smsContact(Cursor cursor) {
         return callOrSmsContact(cursor, true /*sms*/);
     }
 
     /**
      * Calls the contact which the cursor is point to.
      * @return true if the call was initiated, false otherwise
      */
     boolean callOrSmsContact(Cursor cursor, boolean sendSms) {
         if (cursor != null) {
             boolean hasPhone = cursor.getInt(SUMMARY_HAS_PHONE_COLUMN_INDEX) != 0;
             if (!hasPhone) {
                 // There is no phone number.
                 signalError();
                 return false;
             }
 
             String phone = null;
             Cursor phonesCursor = null;
             phonesCursor = queryPhoneNumbers(cursor.getLong(SUMMARY_ID_COLUMN_INDEX));
             if (phonesCursor == null || phonesCursor.getCount() == 0) {
                 // No valid number
                 signalError();
                 return false;
             } else if (phonesCursor.getCount() == 1) {
                 // only one number, call it.
                 phone = phonesCursor.getString(phonesCursor.getColumnIndex(Phone.NUMBER));
             } else {
                 phonesCursor.moveToPosition(-1);
                 while (phonesCursor.moveToNext()) {
                     if (phonesCursor.getInt(phonesCursor.
                             getColumnIndex(Data.IS_SUPER_PRIMARY)) != 0) {
                         // Found super primary, call it.
                         phone = phonesCursor.
                         getString(phonesCursor.getColumnIndex(Phone.NUMBER));
                         break;
                     }
                 }
             }
 
             if (phone == null) {
                 // Display dialog to choose a number to call.
                 PhoneDisambigDialog phoneDialog = new PhoneDisambigDialog(
                         this, phonesCursor, sendSms);
                 phoneDialog.show();
             } else {
                 if (sendSms) {
                     ContactsUtils.initiateSms(this, phone);
                 } else {
                     ContactsUtils.initiateCall(this, phone);
                 }
             }
             return true;
         }
 
         return false;
     }
 
     private Cursor queryPhoneNumbers(long contactId) {
         Uri baseUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId);
         Uri dataUri = Uri.withAppendedPath(baseUri, Contacts.Data.CONTENT_DIRECTORY);
 
         Cursor c = getContentResolver().query(dataUri,
                 new String[] {Data._ID, Phone.NUMBER, Data.IS_SUPER_PRIMARY},
                 Data.MIMETYPE + "=?", new String[] {Phone.CONTENT_ITEM_TYPE}, null);
         if (c != null && c.moveToFirst()) {
             return c;
         }
         return null;
     }
 
     /**
      * Signal an error to the user.
      */
     void signalError() {
         //TODO play an error beep or something...
     }
 
     Cursor getItemForView(View view) {
         ListView listView = getListView();
         int index = listView.getPositionForView(view);
         if (index < 0) {
             return null;
         }
         return (Cursor) listView.getAdapter().getItem(index);
     }
 
     private static class QueryHandler extends AsyncQueryHandler {
         protected final WeakReference<ContactsListActivity> mActivity;
 
         public QueryHandler(Context context) {
             super(context.getContentResolver());
             mActivity = new WeakReference<ContactsListActivity>((ContactsListActivity) context);
         }
 
         @Override
         protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
             final ContactsListActivity activity = mActivity.get();
             if (activity != null && !activity.isFinishing()) {
                 activity.mAdapter.setLoading(false);
                 activity.getListView().clearTextFilter();
                 activity.mAdapter.changeCursor(cursor);
 
                 // Now that the cursor is populated again, it's possible to restore the list state
                 if (activity.mListState != null) {
                     activity.mList.onRestoreInstanceState(activity.mListState);
                     if (activity.mListHasFocus) {
                         activity.mList.requestFocus();
                     }
                     activity.mListHasFocus = false;
                     activity.mListState = null;
                 }
             } else {
                 cursor.close();
             }
         }
     }
 
     /**
      * Query handler for the suggestions query used in the Join Contacts UI.  Once the
      * suggestions query is complete, the handler launches an A-Z query.  The entire search is only
      * done once the second query is complete.
      */
     private static final class SuggestionsQueryHandler extends QueryHandler {
         boolean mSuggestionsQueryComplete;
         private final long mAggregateId;
 
         public SuggestionsQueryHandler(Context context, long aggregateId) {
             super(context);
             mAggregateId = aggregateId;
         }
 
         @Override
         protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
             if (mSuggestionsQueryComplete) {
                 super.onQueryComplete(token, cookie, cursor);
                 return;
             }
 
             mSuggestionsQueryComplete = true;
 
             final ContactsListActivity activity = mActivity.get();
             if (activity != null && !activity.isFinishing()) {
                 if (cursor.getCount() > 0) {
                     activity.mAdapter.setSuggestionsCursor(cursor);
                 } else {
                     activity.mAdapter.setSuggestionsCursor(null);
                 }
                 startQuery(QUERY_TOKEN, null, Contacts.CONTENT_URI, CONTACTS_SUMMARY_PROJECTION,
                         Contacts._ID + " != " + mAggregateId, null,
                         getSortOrder(CONTACTS_SUMMARY_PROJECTION));
 
             } else {
                 cursor.close();
             }
         }
     }
 
     final static class ContactListItemCache {
         public TextView header;
         public View divider;
         public TextView nameView;
         public ImageView callView;
         public CharArrayBuffer nameBuffer = new CharArrayBuffer(128);
         public TextView labelView;
         public CharArrayBuffer labelBuffer = new CharArrayBuffer(128);
         public TextView dataView;
         public CharArrayBuffer dataBuffer = new CharArrayBuffer(128);
         public ImageView presenceView;
         public FasttrackBadgeWidget photoView;
     }
 
     final static class PhotoInfo {
         public int position;
         public long photoId;
 
         public PhotoInfo(int position, long photoId) {
             this.position = position;
             this.photoId = photoId;
         }
         public FasttrackBadgeWidget photoView;
     }
 
     private final class ContactItemListAdapter extends ResourceCursorAdapter
             implements SectionIndexer, OnScrollListener {
         private SectionIndexer mIndexer;
         private String mAlphabet;
         private boolean mLoading = true;
         private CharSequence mUnknownNameText;
         private CharSequence[] mLocalizedLabels;
         private boolean mDisplayPhotos = false;
         private boolean mDisplayCallButton = false;
         private boolean mDisplayAdditionalData = true;
         private HashMap<Long, SoftReference<Bitmap>> mBitmapCache = null;
         private HashSet<ImageView> mItemsMissingImages = null;
         private int mFrequentSeparatorPos = ListView.INVALID_POSITION;
         private boolean mDisplaySectionHeaders = true;
         private int[] mSectionPositions;
         private Cursor mSuggestionsCursor;
         private int mSuggestionsCursorCount;
         private ImageFetchHandler mHandler;
         private static final int FETCH_IMAGE_MSG = 1;
 
         public ContactItemListAdapter(Context context) {
             super(context, R.layout.contacts_list_item, null, false);
 
             mHandler = new ImageFetchHandler();
             mAlphabet = context.getString(com.android.internal.R.string.fast_scroll_alphabet);
 
             mUnknownNameText = context.getText(android.R.string.unknownName);
             // TODO: use a different method of finding labels
 //            switch (mMode) {
 //                case MODE_PICK_POSTAL:
 //                    mLocalizedLabels = EditContactActivity.getLabelsForMimetype(mContext,
 //                            CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE);
 //                    mDisplaySectionHeaders = false;
 //                    break;
 //                case MODE_PICK_PHONE:
 //                    mLocalizedLabels = EditContactActivity.getLabelsForMimetype(mContext,
 //                            CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
 //                    mDisplaySectionHeaders = false;
 //                    break;
 //                default:
                     mLocalizedLabels = context.getResources().getStringArray(android.R.array.phoneTypes);
 //                    EditContactActivity.getLabelsForMimetype(mContext,
 //                            CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
 //                    break;
 //            }
 
             // Do not display the second line of text if in a specific SEARCH query mode, usually for
             // matching a specific E-mail or phone number. Any contact details
             // shown would be identical, and columns might not even be present
             // in the returned cursor.
             if (mQueryMode != QUERY_MODE_NONE) {
                 mDisplayAdditionalData = false;
             }
 
             if ((mMode & MODE_MASK_NO_DATA) == MODE_MASK_NO_DATA) {
                 mDisplayAdditionalData = false;
             }
 
             if ((mMode & MODE_MASK_SHOW_CALL_BUTTON) == MODE_MASK_SHOW_CALL_BUTTON) {
                 mDisplayCallButton = true;
             }
 
             if ((mMode & MODE_MASK_SHOW_PHOTOS) == MODE_MASK_SHOW_PHOTOS) {
                 mDisplayPhotos = true;
                 setViewResource(R.layout.contacts_list_item_photo);
                 mBitmapCache = new HashMap<Long, SoftReference<Bitmap>>();
                 mItemsMissingImages = new HashSet<ImageView>();
             }
 
             if (mMode == MODE_STREQUENT || mMode == MODE_FREQUENT) {
                 mDisplaySectionHeaders = false;
             }
         }
 
         private class ImageFetchHandler extends Handler {
 
             @Override
             public void handleMessage(Message message) {
                 if (ContactsListActivity.this.isFinishing()) {
                     return;
                 }
                 switch(message.what) {
                     case FETCH_IMAGE_MSG: {
                         final ImageView imageView = (ImageView) message.obj;
                         final PhotoInfo info = (PhotoInfo)imageView.getTag();
                         final long photoId = info.photoId;
                         if (photoId == 0) {
                             break;
                         }
 
                         Bitmap photo = null;
                         try {
                             photo = ContactsUtils.loadContactPhoto(mContext, photoId, null);
                         } catch (OutOfMemoryError e) {
                             // Not enough memory for the photo, do nothing.
                         }
 
                         if (photo == null) {
                             break;
                         }
 
                         mBitmapCache.put(photoId, new SoftReference<Bitmap>(photo));
 
                         // Make sure the photoId on this image view has not changed
                         // while we were loading the image.
                         synchronized (imageView) {
                             final PhotoInfo updatedInfo = (PhotoInfo)imageView.getTag();
                             long currentPhotoId = updatedInfo.photoId;
                             if (currentPhotoId == photoId) {
                                 imageView.setImageBitmap(photo);
                                 mItemsMissingImages.remove(imageView);
                             }
                         }
                         break;
                     }
                 }
             }
 
             public void clearImageFecthing() {
                 removeMessages(FETCH_IMAGE_MSG);
             }
         }
 
         public void setSuggestionsCursor(Cursor cursor) {
             if (mSuggestionsCursor != null) {
                 mSuggestionsCursor.close();
             }
             mSuggestionsCursor = cursor;
             mSuggestionsCursorCount = cursor == null ? 0 : cursor.getCount();
         }
 
         private SectionIndexer getNewIndexer(Cursor cursor) {
             /* if (Locale.getDefault().getLanguage().equals(Locale.JAPAN.getLanguage())) {
                 return new JapaneseContactListIndexer(cursor, SORT_STRING_INDEX);
             } else { */
                 return new AlphabetIndexer(cursor, SUMMARY_NAME_COLUMN_INDEX, mAlphabet);
             /* } */
         }
 
         /**
          * Callback on the UI thread when the content observer on the backing cursor fires.
          * Instead of calling requery we need to do an async query so that the requery doesn't
          * block the UI thread for a long time.
          */
         @Override
         protected void onContentChanged() {
             CharSequence constraint = getListView().getTextFilter();
             if (!TextUtils.isEmpty(constraint)) {
                 // Reset the filter state then start an async filter operation
                 Filter filter = getFilter();
                 filter.filter(constraint);
             } else {
                 // Start an async query
                 startQuery();
             }
         }
 
         public void setLoading(boolean loading) {
             mLoading = loading;
         }
 
         @Override
         public boolean isEmpty() {
             if ((mMode & MODE_MASK_CREATE_NEW) == MODE_MASK_CREATE_NEW) {
                 // This mode mask adds a header and we always want it to show up, even
                 // if the list is empty, so always claim the list is not empty.
                 return false;
             } else {
                 if (mLoading) {
                     // We don't want the empty state to show when loading.
                     return false;
                 } else {
                     return super.isEmpty();
                 }
             }
         }
 
         @Override
         public int getItemViewType(int position) {
             if (getSeparatorId(position) != 0) {
                 // We don't want the separator view to be recycled.
                 return IGNORE_ITEM_VIEW_TYPE;
             }
             return super.getItemViewType(position);
         }
 
         @Override
         public View getView(int position, View convertView, ViewGroup parent) {
             if (!mDataValid) {
                 throw new IllegalStateException(
                         "this should only be called when the cursor is valid");
             }
 
             // Handle the separator specially
             int separatorId = getSeparatorId(position);
             if (separatorId != 0) {
                 LayoutInflater inflater =
                         (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                 TextView view = (TextView) inflater.inflate(R.layout.list_separator, parent, false);
                 view.setText(separatorId);
                 return view;
             }
 
             boolean showingSuggestion;
             Cursor cursor;
             if (mSuggestionsCursorCount != 0 && position < mSuggestionsCursorCount + 2) {
                 showingSuggestion = true;
                 cursor = mSuggestionsCursor;
             } else {
                 showingSuggestion = false;
                 cursor = mCursor;
             }
 
             int realPosition = getRealPosition(position);
             if (!cursor.moveToPosition(realPosition)) {
                 throw new IllegalStateException("couldn't move cursor to position " + position);
             }
 
             View v;
             if (convertView == null) {
                 v = newView(mContext, cursor, parent);
             } else {
                 v = convertView;
             }
             bindView(v, mContext, cursor);
             bindSectionHeader(v, realPosition, mDisplaySectionHeaders && !showingSuggestion);
             return v;
         }
 
         private int getSeparatorId(int position) {
             int separatorId = 0;
             if (position == mFrequentSeparatorPos) {
                 separatorId = R.string.favoritesFrquentSeparator;
             }
             if (mSuggestionsCursorCount != 0) {
                 if (position == 0) {
                     separatorId = R.string.separatorJoinAggregateSuggestions;
                 } else if (position == mSuggestionsCursorCount + 1) {
                     separatorId = R.string.separatorJoinAggregateAll;
                 }
             }
             return separatorId;
         }
 
         @Override
         public View newView(Context context, Cursor cursor, ViewGroup parent) {
             final View view = super.newView(context, cursor, parent);
 
             final ContactListItemCache cache = new ContactListItemCache();
             cache.header = (TextView) view.findViewById(R.id.header);
             cache.divider = view.findViewById(R.id.list_divider);
             cache.nameView = (TextView) view.findViewById(R.id.name);
             cache.callView = (ImageView) view.findViewById(R.id.call_button);
             if (cache.callView != null) {
                 cache.callView.setOnClickListener(ContactsListActivity.this);
             }
             cache.labelView = (TextView) view.findViewById(R.id.label);
             cache.dataView = (TextView) view.findViewById(R.id.data);
             cache.presenceView = (ImageView) view.findViewById(R.id.presence);
             cache.photoView = (FasttrackBadgeWidget) view.findViewById(R.id.photo);
             view.setTag(cache);
 
             return view;
         }
 
         @Override
         public void bindView(View view, Context context, Cursor cursor) {
             final ContactListItemCache cache = (ContactListItemCache) view.getTag();
 
             TextView dataView = cache.dataView;
             TextView labelView = cache.labelView;
             int typeColumnIndex;
             int dataColumnIndex;
             int labelColumnIndex;
             int defaultType;
             int nameColumnIndex;
             boolean displayAdditionalData = mDisplayAdditionalData;
             switch(mMode) {
                 case MODE_PICK_PHONE:
                 case MODE_LEGACY_PICK_PHONE: {
                     nameColumnIndex = PHONE_DISPLAY_NAME_COLUMN_INDEX;
                     dataColumnIndex = PHONE_NUMBER_COLUMN_INDEX;
                     typeColumnIndex = PHONE_TYPE_COLUMN_INDEX;
                     labelColumnIndex = PHONE_LABEL_COLUMN_INDEX;
                     defaultType = Phone.TYPE_HOME;
                     break;
                 }
                 case MODE_PICK_POSTAL:
                 case MODE_LEGACY_PICK_POSTAL: {
                     nameColumnIndex = POSTAL_DISPLAY_NAME_COLUMN_INDEX;
                     dataColumnIndex = POSTAL_ADDRESS_COLUMN_INDEX;
                     typeColumnIndex = POSTAL_TYPE_COLUMN_INDEX;
                     labelColumnIndex = POSTAL_LABEL_COLUMN_INDEX;
                     defaultType = StructuredPostal.TYPE_HOME;
                     break;
                 }
                 default: {
                     nameColumnIndex = SUMMARY_NAME_COLUMN_INDEX;
                     dataColumnIndex = -1;
                     typeColumnIndex = -1;
                     labelColumnIndex = -1;
                     defaultType = Phone.TYPE_HOME;
                     displayAdditionalData = false;
                 }
             }
 
             // Set the name
             cursor.copyStringToBuffer(nameColumnIndex, cache.nameBuffer);
             int size = cache.nameBuffer.sizeCopied;
             if (size != 0) {
                 cache.nameView.setText(cache.nameBuffer.data, 0, size);
             } else {
                 cache.nameView.setText(mUnknownNameText);
             }
 
             // Make the call button visible if requested.
             if (mDisplayCallButton) {
                 int pos = cursor.getPosition();
                 cache.callView.setVisibility(View.VISIBLE);
                 cache.callView.setTag(pos);
             } else {
                 cache.callView.setVisibility(View.GONE);
             }
 
             // Set the photo, if requested
             if (mDisplayPhotos) {
 
                 long photoId = 0;
                 if (!cursor.isNull(SUMMARY_PHOTO_ID_COLUMN_INDEX)) {
                     photoId = cursor.getLong(SUMMARY_PHOTO_ID_COLUMN_INDEX);
                 }
 
                 final int position = cursor.getPosition();
                 cache.photoView.setTag(new PhotoInfo(position, photoId));
 
                 // Build soft lookup reference
                 final long contactId = cursor.getLong(SUMMARY_ID_COLUMN_INDEX);
                 final String lookupKey = cursor.getString(SUMMARY_LOOKUP_KEY);
                 cache.photoView.assignContactUri(Contacts.getLookupUri(contactId, lookupKey));
 
                 if (photoId == 0) {
                     cache.photoView.setImageResource(R.drawable.ic_contact_list_picture);
                 } else {
 
                     Bitmap photo = null;
 
                     // Look for the cached bitmap
                     SoftReference<Bitmap> ref = mBitmapCache.get(photoId);
                     if (ref != null) {
                         photo = ref.get();
                         if (photo == null) {
                             mBitmapCache.remove(photoId);
                         }
                     }
 
                     // Bind the photo, or use the fallback no photo resource
                     if (photo != null) {
                         cache.photoView.setImageBitmap(photo);
                     } else {
                         // Cache miss
                         cache.photoView.setImageResource(R.drawable.ic_contact_list_picture);
 
                         // Add it to a set of images that are populated asynchronously.
                         mItemsMissingImages.add(cache.photoView);
 
                         if (mScrollState != OnScrollListener.SCROLL_STATE_FLING) {
 
                             // Scrolling is idle or slow, go get the image right now.
                             sendFetchImageMessage(cache.photoView);
                         }
                     }
                 }
             }
 
             if (!displayAdditionalData) {
                 cache.dataView.setVisibility(View.GONE);
                 cache.labelView.setVisibility(View.GONE);
                 cache.presenceView.setVisibility(View.GONE);
                 return;
             }
 
             // Set the data.
             cursor.copyStringToBuffer(dataColumnIndex, cache.dataBuffer);
 
             size = cache.dataBuffer.sizeCopied;
             if (size != 0) {
                 dataView.setText(cache.dataBuffer.data, 0, size);
                 dataView.setVisibility(View.VISIBLE);
             } else {
                 dataView.setVisibility(View.GONE);
             }
 
             // Set the label.
             if (!cursor.isNull(typeColumnIndex)) {
                 labelView.setVisibility(View.VISIBLE);
                 int type = cursor.getInt(typeColumnIndex);
 
                 if (type != CommonDataKinds.BaseTypes.TYPE_CUSTOM) {
                     try {
                         labelView.setText(mLocalizedLabels[type - 1]);
                     } catch (ArrayIndexOutOfBoundsException e) {
                         labelView.setText(mLocalizedLabels[defaultType - 1]);
                     }
                 } else {
                     cursor.copyStringToBuffer(labelColumnIndex, cache.labelBuffer);
                     // Don't check size, if it's zero just don't show anything
                     labelView.setText(cache.labelBuffer.data, 0, cache.labelBuffer.sizeCopied);
                 }
             } else {
                 // There is no label, hide the the view
                 labelView.setVisibility(View.GONE);
             }
 
             // Set the proper icon (star or presence or nothing)
             ImageView presenceView = cache.presenceView;
             if ((mMode & MODE_MASK_NO_PRESENCE) == 0) {
                 int serverStatus;
                 if (!cursor.isNull(SUMMARY_PRESENCE_STATUS_COLUMN_INDEX)) {
                     serverStatus = cursor.getInt(SUMMARY_PRESENCE_STATUS_COLUMN_INDEX);
                     presenceView.setImageResource(
                             Presence.getPresenceIconResourceId(serverStatus));
                     presenceView.setVisibility(View.VISIBLE);
                 } else {
                     presenceView.setVisibility(View.GONE);
                 }
             } else {
                 presenceView.setVisibility(View.GONE);
             }
 
         }
 
         private void bindSectionHeader(View view, int position, boolean displaySectionHeaders) {
             final ContactListItemCache cache = (ContactListItemCache) view.getTag();
             if (!displaySectionHeaders) {
                 cache.header.setVisibility(View.GONE);
                 cache.divider.setVisibility(View.VISIBLE);
             } else {
                 final int section = getSectionForPosition(position);
                 if (getPositionForSection(section) == position) {
                     cache.header.setText(mIndexer.getSections()[section].toString());
                     cache.header.setVisibility(View.VISIBLE);
                     cache.divider.setVisibility(View.GONE);
                 } else {
                     cache.header.setVisibility(View.GONE);
                     cache.divider.setVisibility(View.VISIBLE);
                 }
             }
         }
 
         @Override
         public void changeCursor(Cursor cursor) {
             // Get the split between starred and frequent items, if the mode is strequent
             mFrequentSeparatorPos = ListView.INVALID_POSITION;
             if (cursor != null && cursor.getCount() > 0 && mMode == MODE_STREQUENT) {
                 cursor.move(-1);
                 for (int i = 0; cursor.moveToNext(); i++) {
                     int starred = cursor.getInt(SUMMARY_STARRED_COLUMN_INDEX);
                     if (starred == 0) {
                         if (i > 0) {
                             // Only add the separator when there are starred items present
                             mFrequentSeparatorPos = i;
                         }
                         break;
                     }
                 }
             }
 
             super.changeCursor(cursor);
 
             // Update the indexer for the fast scroll widget
             updateIndexer(cursor);
 
             // Clear the photo bitmap cache, if there is one
             if (mBitmapCache != null) {
                 mBitmapCache.clear();
             }
         }
 
         private void updateIndexer(Cursor cursor) {
             if (mIndexer == null) {
                 mIndexer = getNewIndexer(cursor);
             } else {
                 if (Locale.getDefault().equals(Locale.JAPAN)) {
                     if (mIndexer instanceof JapaneseContactListIndexer) {
                         ((JapaneseContactListIndexer)mIndexer).setCursor(cursor);
                     } else {
                         mIndexer = getNewIndexer(cursor);
                     }
                 } else {
                     if (mIndexer instanceof AlphabetIndexer) {
                         ((AlphabetIndexer)mIndexer).setCursor(cursor);
                     } else {
                         mIndexer = getNewIndexer(cursor);
                     }
                 }
             }
 
             int sectionCount = mIndexer.getSections().length;
             if (mSectionPositions == null || mSectionPositions.length != sectionCount) {
                 mSectionPositions = new int[sectionCount];
             }
             for (int i = 0; i < sectionCount; i++) {
                 mSectionPositions[i] = ListView.INVALID_POSITION;
             }
         }
 
         /**
          * Run the query on a helper thread. Beware that this code does not run
          * on the main UI thread!
          */
         @Override
         public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
             return doFilter(constraint.toString());
         }
 
         public Object [] getSections() {
             if (mMode == MODE_STARRED) {
                 return new String[] { " " };
             } else {
                 return mIndexer.getSections();
             }
         }
 
         public int getPositionForSection(int sectionIndex) {
             if (mMode == MODE_STARRED) {
                 return -1;
             }
 
             if (mIndexer == null) {
                 Cursor cursor = mAdapter.getCursor();
                 if (cursor == null) {
                     // No cursor, the section doesn't exist so just return 0
                     return 0;
                 }
                 mIndexer = getNewIndexer(cursor);
             }
 
             int position = mSectionPositions[sectionIndex];
             if (position == ListView.INVALID_POSITION) {
                 position = mSectionPositions[sectionIndex] =
                         mIndexer.getPositionForSection(sectionIndex);
             }
 
             return position;
         }
 
         public int getSectionForPosition(int position) {
             // The current implementations of SectionIndexers (specifically the Japanese indexer)
             // only work in one direction: given a section they can calculate the position.
             // Here we are using that existing functionality to do the reverse mapping. We are
             // performing binary search in the mSectionPositions array, which itself is populated
             // lazily using the "forward" mapping supported by the indexer.
 
             int start = 0;
             int end = mSectionPositions.length;
             while (start != end) {
 
                 // We are making the binary search slightly asymmetrical, because the
                 // user is more likely to be scrolling the list from the top down.
                 int pivot = start + (end - start) / 4;
 
                 int value = getPositionForSection(pivot);
                 if (value <= position) {
                     start = pivot + 1;
                 } else {
                     end = pivot;
                 }
             }
 
             // The variable "start" cannot be 0, as long as the indexer is implemented properly
             // and actually maps position = 0 to section = 0
             return start - 1;
         }
 
         @Override
         public boolean areAllItemsEnabled() {
             return mMode != MODE_STARRED;
         }
 
         @Override
         public boolean isEnabled(int position) {
             if (mSuggestionsCursorCount > 0) {
                 return position != 0 && position != mSuggestionsCursorCount + 1;
             }
             return position != mFrequentSeparatorPos;
         }
 
         @Override
         public int getCount() {
             if (mSuggestionsCursorCount != 0) {
                 // When showing suggestions, we have 2 additional list items: the "Suggestions"
                 // and "All contacts" headers.
                 return mSuggestionsCursorCount + super.getCount() + 2;
             }
             else if (mFrequentSeparatorPos != ListView.INVALID_POSITION) {
                 // When showing strequent list, we have an additional list item - the separator.
                 return super.getCount() + 1;
             } else {
                 return super.getCount();
             }
         }
 
         private int getRealPosition(int pos) {
             if (mSuggestionsCursorCount != 0) {
                 // When showing suggestions, we have 2 additional list items: the "Suggestions"
                 // and "All contacts" separators.
                 if (pos < mSuggestionsCursorCount + 2) {
                     // We are in the upper partition (Suggestions). Adjusting for the "Suggestions"
                     // separator.
                     return pos - 1;
                 } else {
                     // We are in the lower partition (All contacts). Adjusting for the size
                     // of the upper partition plus the two separators.
                     return pos - mSuggestionsCursorCount - 2;
                 }
             } else if (mFrequentSeparatorPos == ListView.INVALID_POSITION) {
                 // No separator, identity map
                 return pos;
             } else if (pos <= mFrequentSeparatorPos) {
                 // Before or at the separator, identity map
                 return pos;
             } else {
                 // After the separator, remove 1 from the pos to get the real underlying pos
                 return pos - 1;
             }
         }
 
         @Override
         public Object getItem(int pos) {
             if (mSuggestionsCursorCount != 0 && pos <= mSuggestionsCursorCount) {
                 mSuggestionsCursor.moveToPosition(getRealPosition(pos));
                 return mSuggestionsCursor;
             } else {
                 return super.getItem(getRealPosition(pos));
             }
         }
 
         @Override
         public long getItemId(int pos) {
             if (mSuggestionsCursorCount != 0 && pos < mSuggestionsCursorCount + 2) {
                 if (mSuggestionsCursor.moveToPosition(pos - 1)) {
                     return mSuggestionsCursor.getLong(mRowIDColumn);
                 } else {
                     return 0;
                 }
             }
             return super.getItemId(getRealPosition(pos));
         }
 
         public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                 int totalItemCount) {
             // no op
         }
 
         public void onScrollStateChanged(AbsListView view, int scrollState) {
             mScrollState = scrollState;
             if (scrollState == OnScrollListener.SCROLL_STATE_FLING) {
                 // If we are in a fling, stop loading images.
                 clearImageFetching();
             } else if (mDisplayPhotos) {
                 processMissingImageItems(view);
             }
         }
 
         private void processMissingImageItems(AbsListView view) {
             for (ImageView iv : mItemsMissingImages) {
                 sendFetchImageMessage(iv);
             }
         }
 
         private void sendFetchImageMessage(ImageView view) {
             Message msg = new Message();
             msg.what = FETCH_IMAGE_MSG;
             msg.obj = view;
             mHandler.sendMessage(msg);
         }
 
         /** Clear all pending messages on the {@link ImageFetchHandler} */
         public void clearImageFetching() {
             mHandler.clearImageFecthing();
         }
     }
 }
