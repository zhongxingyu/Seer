 /*
  * Copyright (C) 2010 The Android Open Source Project
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
 
 package com.android.calendar.event;
 
 import com.android.calendar.AsyncQueryService;
 import com.android.calendar.CalendarController;
 import com.android.calendar.CalendarController.EventHandler;
 import com.android.calendar.CalendarController.EventInfo;
 import com.android.calendar.CalendarController.EventType;
 import com.android.calendar.CalendarEventModel;
 import com.android.calendar.CalendarEventModel.Attendee;
 import com.android.calendar.DeleteEventHelper;
 import com.android.calendar.R;
 import com.android.calendar.Utils;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Fragment;
 import android.content.AsyncQueryHandler;
 import android.content.ContentProviderOperation;
 import android.content.ContentResolver;
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.database.Cursor;
 import android.database.MatrixCursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.Calendar.Attendees;
 import android.provider.Calendar.Calendars;
 import android.provider.Calendar.Events;
 import android.provider.Calendar.Reminders;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Toast;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 
 public class EditEventFragment extends Fragment implements EventHandler {
     private static final String TAG = "EditEventActivity";
 
     private static final String BUNDLE_KEY_MODEL = "key_model";
     private static final String BUNDLE_KEY_EDIT_STATE = "key_edit_state";
     private static final String BUNDLE_KEY_EVENT = "key_event";
     private static final String BUNDLE_KEY_READ_ONLY = "key_read_only";
 
     private static final boolean DEBUG = false;
 
     private static final int TOKEN_EVENT = 1;
     private static final int TOKEN_ATTENDEES = 1 << 1;
     private static final int TOKEN_REMINDERS = 1 << 2;
     private static final int TOKEN_CALENDARS = 1 << 3;
     private static final int TOKEN_ALL = TOKEN_EVENT | TOKEN_ATTENDEES | TOKEN_REMINDERS
             | TOKEN_CALENDARS;
     private static final int TOKEN_UNITIALIZED = 1 << 31;
 
     /**
      * A bitfield of TOKEN_* to keep track which query hasn't been completed
      * yet. Once all queries have returned, the model can be applied to the
      * view.
      */
     private int mOutstandingQueries = TOKEN_UNITIALIZED;
 
     EditEventHelper mHelper;
     CalendarEventModel mModel;
     CalendarEventModel mOriginalModel;
     CalendarEventModel mRestoreModel;
     EditEventView mView;
     QueryHandler mHandler;
 
     private AlertDialog mModifyDialog;
     int mModification = Utils.MODIFY_UNINITIALIZED;
 
     private EventInfo mEvent;
     private EventBundle mEventBundle;
     private Uri mUri;
     private long mBegin;
     private long mEnd;
 
     private Activity mContext;
     private Done mOnDone = new Done();
     private Menu mMenu;
 
     private boolean mSaveOnDetach = true;
     private boolean mIsReadOnly = false;
     public boolean mShowModifyDialogOnLaunch = false;
 
     private InputMethodManager mInputMethodManager;
 
     // TODO turn this into a helper function in EditEventHelper for building the
     // model
     private class QueryHandler extends AsyncQueryHandler {
         public QueryHandler(ContentResolver cr) {
             super(cr);
         }
 
         @Override
         protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
             // If the query didn't return a cursor for some reason return
             if (cursor == null) {
                 return;
             }
 
             // If the Activity is finishing, then close the cursor.
             // Otherwise, use the new cursor in the adapter.
             final Activity activity = EditEventFragment.this.getActivity();
             if (activity == null || activity.isFinishing()) {
                 cursor.close();
                 return;
             }
             long eventId;
             switch (token) {
                 case TOKEN_EVENT:
                     if (cursor.getCount() == 0) {
                         // The cursor is empty. This can happen if the event
                         // was deleted.
                         cursor.close();
                         mOnDone.setDoneCode(Utils.DONE_EXIT);
                         mOnDone.run();
                         return;
                     }
                     mOriginalModel = new CalendarEventModel();
                     EditEventHelper.setModelFromCursor(mOriginalModel, cursor);
                     EditEventHelper.setModelFromCursor(mModel, cursor);
                     cursor.close();
 
                     mOriginalModel.mUri = mUri.toString();
 
                     mModel.mUri = mUri.toString();
                     mModel.mOriginalStart = mBegin;
                     mModel.mOriginalEnd = mEnd;
                     mModel.mIsFirstEventInSeries = mBegin == mOriginalModel.mStart;
                     mModel.mStart = mBegin;
                     mModel.mEnd = mEnd;
 
                     eventId = mModel.mId;
 
                     // TOKEN_ATTENDEES
                     if (mModel.mHasAttendeeData && eventId != -1) {
                         Uri attUri = Attendees.CONTENT_URI;
                         String[] whereArgs = {
                             Long.toString(eventId)
                         };
                         mHandler.startQuery(TOKEN_ATTENDEES, null, attUri,
                                 EditEventHelper.ATTENDEES_PROJECTION,
                                 EditEventHelper.ATTENDEES_WHERE /* selection */,
                                 whereArgs /* selection args */, null /* sort order */);
                     } else {
                         setModelIfDone(TOKEN_ATTENDEES);
                     }
 
                     // TOKEN_REMINDERS
                     if (mModel.mHasAlarm) {
                         Uri rUri = Reminders.CONTENT_URI;
                         String[] remArgs = {
                                 Long.toString(eventId), Integer.toString(Reminders.METHOD_ALERT),
                                 Integer.toString(Reminders.METHOD_DEFAULT)
                         };
                         mHandler
                                 .startQuery(TOKEN_REMINDERS, null, rUri,
                                         EditEventHelper.REMINDERS_PROJECTION,
                                         EditEventHelper.REMINDERS_WHERE /* selection */,
                                         remArgs /* selection args */, null /* sort order */);
                     } else {
                         setModelIfDone(TOKEN_REMINDERS);
                     }
 
                     // TOKEN_CALENDARS
                     String[] selArgs = {
                         Long.toString(mModel.mCalendarId)
                     };
                     mHandler.startQuery(TOKEN_CALENDARS, null, Calendars.CONTENT_URI,
                             EditEventHelper.CALENDARS_PROJECTION, EditEventHelper.CALENDARS_WHERE,
                             selArgs /* selection args */, null /* sort order */);
 
                     setModelIfDone(TOKEN_EVENT);
                     break;
                 case TOKEN_ATTENDEES:
                     try {
                         while (cursor.moveToNext()) {
                             String name = cursor.getString(EditEventHelper.ATTENDEES_INDEX_NAME);
                             String email = cursor.getString(EditEventHelper.ATTENDEES_INDEX_EMAIL);
                             int status = cursor.getInt(EditEventHelper.ATTENDEES_INDEX_STATUS);
                             int relationship = cursor
                                     .getInt(EditEventHelper.ATTENDEES_INDEX_RELATIONSHIP);
                             if (relationship == Attendees.RELATIONSHIP_ORGANIZER) {
                                 if (email != null) {
                                     mModel.mOrganizer = email;
                                     mModel.mIsOrganizer = mModel.mOwnerAccount
                                             .equalsIgnoreCase(email);
                                     mOriginalModel.mOrganizer = email;
                                     mOriginalModel.mIsOrganizer = mOriginalModel.mOwnerAccount
                                             .equalsIgnoreCase(email);
                                 }
 
                                 if (TextUtils.isEmpty(name)) {
                                     mModel.mOrganizerDisplayName = mModel.mOrganizer;
                                     mOriginalModel.mOrganizerDisplayName =
                                             mOriginalModel.mOrganizer;
                                 } else {
                                     mModel.mOrganizerDisplayName = name;
                                     mOriginalModel.mOrganizerDisplayName = name;
                                 }
                             }
 
                             if (email != null) {
                                 if (mModel.mOwnerAccount != null &&
                                         mModel.mOwnerAccount.equalsIgnoreCase(email)) {
                                     int attendeeId =
                                         cursor.getInt(EditEventHelper.ATTENDEES_INDEX_ID);
                                     mModel.mOwnerAttendeeId = attendeeId;
                                     mModel.mSelfAttendeeStatus = status;
                                     mOriginalModel.mOwnerAttendeeId = attendeeId;
                                     mOriginalModel.mSelfAttendeeStatus = status;
                                     continue;
                                 }
                             }
                             Attendee attendee = new Attendee(name, email);
                             attendee.mStatus = status;
                             mModel.addAttendee(attendee);
                             mOriginalModel.addAttendee(attendee);
                         }
                     } finally {
                         cursor.close();
                     }
 
                     setModelIfDone(TOKEN_ATTENDEES);
                     break;
                 case TOKEN_REMINDERS:
                     try {
                         // Add all reminders to the models
                         while (cursor.moveToNext()) {
                             int minutes = cursor.getInt(EditEventHelper.REMINDERS_INDEX_MINUTES);
                             mModel.mReminderMinutes.add(minutes);
                             mOriginalModel.mReminderMinutes.add(minutes);
                         }
                     } finally {
                         cursor.close();
                     }
 
                     setModelIfDone(TOKEN_REMINDERS);
                     break;
                 case TOKEN_CALENDARS:
                     try {
                         if (mModel.mCalendarId == -1) {
                             // Populate Calendar spinner only if no calendar is set e.g. new event
                             MatrixCursor matrixCursor = Utils.matrixCursorFromCursor(cursor);
                             if (DEBUG) {
                                 Log.d(TAG, "onQueryComplete: setting cursor with "
                                         + matrixCursor.getCount() + " calendars");
                             }
                             mView.setCalendarsCursor(matrixCursor, isAdded() && isResumed());
                         } else {
                             // Populate model for an existing event
                             EditEventHelper.setModelFromCalendarCursor(mModel, cursor);
                             EditEventHelper.setModelFromCalendarCursor(mOriginalModel, cursor);
                         }
                     } finally {
                         cursor.close();
                     }
 
                     setModelIfDone(TOKEN_CALENDARS);
                     break;
             }
         }
     }
 
     private void setModelIfDone(int queryType) {
         synchronized (this) {
             mOutstandingQueries &= ~queryType;
             if (mOutstandingQueries == 0) {
                 if (mRestoreModel != null) {
                     mModel = mRestoreModel;
                 }
                 if (mShowModifyDialogOnLaunch && mModification == Utils.MODIFY_UNINITIALIZED) {
                     if (!TextUtils.isEmpty(mModel.mRrule)) {
                         displayEditWhichDialog();
                     } else {
                         mModification = Utils.MODIFY_ALL;
                     }
 
                 }
                 mView.setModel(mModel);
                 mView.setModification(mModification);
                 if (mMenu != null) {
                     updateActionBar();
                 }
             }
         }
     }
 
     private void updateActionBar() {
         if (mMenu == null) {
             return;
         }
         MenuItem cancelItem = mMenu.findItem(R.id.action_cancel);
         MenuItem deleteItem = mMenu.findItem(R.id.action_delete);
         MenuItem editItem = mMenu.findItem(R.id.action_edit);
         boolean canModifyEvent = EditEventHelper.canModifyEvent(mModel);
         boolean canModifyCalendar = EditEventHelper.canModifyCalendar(mModel);
 
         if (canModifyCalendar && mModel.mUri != null) {
             deleteItem.setVisible(true);
         } else {
             deleteItem.setVisible(false);
         }
         if (mIsReadOnly) {
             mMenu.findItem(R.id.action_done).setVisible(false);
         }
         if (mModification == Utils.MODIFY_UNINITIALIZED) {
             cancelItem.setVisible(false);
             if (canModifyEvent) {
                 editItem.setVisible(true);
             } else {
                 editItem.setVisible(false);
             }
             return;
         } else {
             editItem.setVisible(false);
         }
         boolean canRespond = EditEventHelper.canRespond(mModel);
 
         if (canRespond || canModifyEvent) {
             cancelItem.setVisible(true);
         } else {
             cancelItem.setVisible(false);
         }
     }
 
     public EditEventFragment() {
         this(null, false);
     }
 
     public EditEventFragment(EventInfo event, boolean readOnly) {
         mEvent = event;
         mIsReadOnly = readOnly;
         setHasOptionsMenu(true);
     }
 
     private void startQuery() {
         mUri = null;
         mBegin = -1;
         mEnd = -1;
         if (mEvent != null) {
             if (mEvent.id != -1) {
                 mModel.mId = mEvent.id;
                 mUri = ContentUris.withAppendedId(Events.CONTENT_URI, mEvent.id);
             }
             if (mEvent.startTime != null) {
                 mBegin = mEvent.startTime.toMillis(true);
             }
             if (mEvent.endTime != null) {
                 mEnd = mEvent.endTime.toMillis(true);
             }
         } else if (mEventBundle != null) {
             if (mEventBundle.id != -1) {
                 mModel.mId = mEventBundle.id;
                 mUri = ContentUris.withAppendedId(Events.CONTENT_URI, mEventBundle.id);
             }
             mBegin = mEventBundle.start;
             mEnd = mEventBundle.end;
         }
 
         if (mBegin <= 0) {
             // use a default value instead
             mBegin = mHelper.constructDefaultStartTime(System.currentTimeMillis());
         }
         if (mEnd < mBegin) {
             // use a default value instead
             mEnd = mHelper.constructDefaultEndTime(mBegin);
         }
 
         // Kick off the query for the event
         boolean newEvent = mUri == null;
         if (!newEvent) {
             mModel.mCalendarAccessLevel = Calendars.NO_ACCESS;
             mOutstandingQueries = TOKEN_ALL;
             if (DEBUG) {
                 Log.d(TAG, "startQuery: uri for event is " + mUri.toString());
             }
             mHandler.startQuery(TOKEN_EVENT, null, mUri, EditEventHelper.EVENT_PROJECTION,
                     null /* selection */, null /* selection args */, null /* sort order */);
         } else {
             mOutstandingQueries = TOKEN_CALENDARS;
             if (DEBUG) {
                 Log.d(TAG, "startQuery: Editing a new event.");
             }
             mModel.mStart = mBegin;
             mModel.mEnd = mEnd;
             mModel.mSelfAttendeeStatus = Attendees.ATTENDEE_STATUS_ACCEPTED;
 
             // Start a query in the background to read the list of calendars
             mHandler.startQuery(TOKEN_CALENDARS, null, Calendars.CONTENT_URI,
                     EditEventHelper.CALENDARS_PROJECTION,
                     EditEventHelper.CALENDARS_WHERE_WRITEABLE_VISIBLE, null /* selection args */,
                     null /* sort order */);
 
             mModification = Utils.MODIFY_ALL;
             updateActionBar();
             mView.setModification(mModification);
         }
     }
 
     @Override
     public void onAttach(Activity activity) {
         super.onAttach(activity);
         mContext = activity;
 
         mHelper = new EditEventHelper(activity, null);
         mHandler = new QueryHandler(activity.getContentResolver());
         mModel = new CalendarEventModel(activity);
         mInputMethodManager = (InputMethodManager)
                 activity.getSystemService(Context.INPUT_METHOD_SERVICE);
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
             Bundle savedInstanceState) {
 //        mContext.requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
         View view;
         if (mIsReadOnly) {
             view = inflater.inflate(R.layout.edit_event_single_column, null);
         } else {
             view = inflater.inflate(R.layout.edit_event, null);
         }
         mView = new EditEventView(mContext, view, mOnDone);
         startQuery();
         return view;
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         if (savedInstanceState != null) {
             if (savedInstanceState.containsKey(BUNDLE_KEY_MODEL)) {
                 mRestoreModel = (CalendarEventModel) savedInstanceState.getSerializable(
                         BUNDLE_KEY_MODEL);
             }
             if (savedInstanceState.containsKey(BUNDLE_KEY_EDIT_STATE)) {
                 mModification = savedInstanceState.getInt(BUNDLE_KEY_EDIT_STATE);
             }
             if (savedInstanceState.containsKey(BUNDLE_KEY_EVENT)) {
                 mEventBundle = (EventBundle) savedInstanceState.getSerializable(BUNDLE_KEY_EVENT);
             }
             if (savedInstanceState.containsKey(BUNDLE_KEY_READ_ONLY)) {
                 mIsReadOnly = savedInstanceState.getBoolean(BUNDLE_KEY_READ_ONLY);
             }
         }
     }
 
 
     @Override
     public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
         super.onCreateOptionsMenu(menu, inflater);
         inflater.inflate(R.menu.edit_event_title_bar, menu);
         synchronized (this) {
             mMenu = menu;
             updateActionBar();
         }
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.action_done:
                 if (EditEventHelper.canModifyEvent(mModel) || EditEventHelper.canRespond(mModel)) {
                     if (mView != null && mView.prepareForSave()) {
                         if (mModification == Utils.MODIFY_UNINITIALIZED) {
                             mModification = Utils.MODIFY_ALL;
                         }
                         mOnDone.setDoneCode(Utils.DONE_SAVE | Utils.DONE_EXIT);
                         mOnDone.run();
                     } else {
                         mOnDone.setDoneCode(Utils.DONE_REVERT);
                         mOnDone.run();
                     }
                 } else if (EditEventHelper.canAddReminders(mModel) && mModel.mId != -1
                         && mOriginalModel != null && mView.prepareForSave()) {
                     saveReminders();
                     mOnDone.setDoneCode(Utils.DONE_EXIT);
                     mOnDone.run();
                 } else {
                     mOnDone.setDoneCode(Utils.DONE_REVERT);
                     mOnDone.run();
                 }
                 break;
             case R.id.action_cancel:
                 mOnDone.setDoneCode(Utils.DONE_REVERT);
                 mOnDone.run();
                 break;
             case R.id.action_delete:
                 mOnDone.setDoneCode(Utils.DONE_DELETE);
                 mOnDone.run();
                 break;
             case R.id.action_edit:
                 if (mIsReadOnly) {
                     CalendarController.getInstance(mContext).sendEventRelatedEvent(this,
                             EventType.EDIT_EVENT, mModel.mId, mModel.mStart, mModel.mEnd, -1, -1);
                 } else if (!TextUtils.isEmpty(mModel.mRrule)) {
                     displayEditWhichDialog();
                 } else {
                     mModification = Utils.MODIFY_ALL;
                     updateActionBar();
                     mView.setModification(mModification);
                 }
                 break;
         }
         return true;
     }
 
     private void saveReminders() {
         ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>(3);
         boolean changed = EditEventHelper.saveReminders(ops, mModel.mId, mModel.mReminderMinutes,
                 mOriginalModel.mReminderMinutes, false /* no force save */);
 
         if (!changed) {
             return;
         }
 
         AsyncQueryService service = new AsyncQueryService(getActivity());
         service.startBatch(0, null, Calendars.CONTENT_URI.getAuthority(), ops, 0);
         // Update the "hasAlarm" field for the event
         Uri uri = ContentUris.withAppendedId(Events.CONTENT_URI, mModel.mId);
         int len = mModel.mReminderMinutes.size();
         boolean hasAlarm = len > 0;
         if (hasAlarm != mOriginalModel.mHasAlarm) {
             ContentValues values = new ContentValues();
             values.put(Events.HAS_ALARM, hasAlarm ? 1 : 0);
             service.startUpdate(0, null, uri, values, null, null, 0);
         }
 
         Toast.makeText(mContext, R.string.saving_event, Toast.LENGTH_SHORT).show();
     }
 
     protected void displayEditWhichDialog() {
         if (!TextUtils.isEmpty(mModel.mRrule) && mModification == Utils.MODIFY_UNINITIALIZED) {
             final boolean notSynced = mModel.mSyncId == null;
             boolean isFirstEventInSeries = mModel.mIsFirstEventInSeries;
             int itemIndex = 0;
             CharSequence[] items;
 
             if (notSynced) {
                 // If this event has not been synced, then don't allow deleting
                 // or changing a single instance.
                 if (isFirstEventInSeries) {
                     // Still display the option so the user knows all events are
                     // changing
                     items = new CharSequence[1];
                 } else {
                     items = new CharSequence[2];
                 }
             } else {
                 if (isFirstEventInSeries) {
                     items = new CharSequence[2];
                 } else {
                     items = new CharSequence[3];
                 }
                 items[itemIndex++] = mContext.getText(R.string.modify_event);
             }
             items[itemIndex++] = mContext.getText(R.string.modify_all);
 
             // Do one more check to make sure this remains at the end of the list
             if (!isFirstEventInSeries) {
                 items[itemIndex++] = mContext.getText(R.string.modify_all_following);
             }
 
             // Display the modification dialog.
             if (mModifyDialog != null) {
                 mModifyDialog.dismiss();
                 mModifyDialog = null;
             }
             mModifyDialog = new AlertDialog.Builder(mContext)
                     .setTitle(R.string.edit_event_label).setItems(items, new OnClickListener() {
                 public void onClick(DialogInterface dialog, int which) {
                     if (which == 0) {
                         mModification = notSynced ? Utils.MODIFY_ALL : Utils.MODIFY_SELECTED;
                         mModel.mOriginalEvent = notSynced ? null : mModel.mSyncId;
                     } else if (which == 1) {
                         mModification = notSynced ? Utils.MODIFY_ALL_FOLLOWING : Utils.MODIFY_ALL;
                     } else if (which == 2) {
                         mModification = Utils.MODIFY_ALL_FOLLOWING;
                     }
 
                     mView.setModification(mModification);
                     updateActionBar();
                 }
             }).show();
         }
     }
 
     class Done implements EditEventHelper.EditDoneRunnable {
         private int mCode = -1;
 
         public void setDoneCode(int code) {
             mCode = code;
         }
 
         public void run() {
             // We only want this to get called once, either because the user
             // pressed back/home or one of the buttons on screen
             mSaveOnDetach = false;
 
             if ((mCode & Utils.DONE_SAVE) != 0 && mModel != null
                     && (EditEventHelper.canRespond(mModel)
                             || EditEventHelper.canModifyEvent(mModel))
                     && !isEmptyNewEvent()
                     && !mModel.isUnchanged(mOriginalModel)
                     && mHelper.saveEvent(mModel, mOriginalModel, mModification)) {
                 int stringResource;
                 if (mModel.mUri != null) {
                     stringResource = R.string.saving_event;
                 } else {
                     stringResource = R.string.creating_event;
                 }
                 Toast.makeText(mContext, stringResource, Toast.LENGTH_SHORT).show();
             }
 
             if ((mCode & Utils.DONE_DELETE) != 0 && mModel != null
                     && EditEventHelper.canModifyCalendar(mModel)) {
                 long begin = mModel.mStart;
                 long end = mModel.mEnd;
                 int which = -1;
                 switch (mModification) {
                     case Utils.MODIFY_SELECTED:
                         which = DeleteEventHelper.DELETE_SELECTED;
                         break;
                     case Utils.MODIFY_ALL_FOLLOWING:
                         which = DeleteEventHelper.DELETE_ALL_FOLLOWING;
                         break;
                     case Utils.MODIFY_ALL:
                         which = DeleteEventHelper.DELETE_ALL;
                         break;
                 }
                 DeleteEventHelper deleteHelper = new DeleteEventHelper(
                         mContext, mContext, !mIsReadOnly /* exitWhenDone */);
                 // TODO update delete helper to use the model instead of the cursor
                deleteHelper.delete(begin, end, mOriginalModel, which);
             }
 
             if ((mCode & Utils.DONE_EXIT) != 0) {
                 // This will exit the edit event screen, should be called
                 // when we want to return to the main calendar views
                 EditEventFragment.this.getActivity().finish();
             }
 
             // Hide a software keyboard so that user won't see it even after this Fragment's
             // disappearing.
             final View focusedView = mContext.getCurrentFocus();
             if (focusedView != null) {
                 mInputMethodManager.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
                 focusedView.clearFocus();
             }
         }
     }
 
     boolean isEmptyNewEvent() {
         if (mOriginalModel != null) {
             // Not new
             return false;
         }
 
         return isEmpty();
     }
 
     private boolean isEmpty() {
         if (mModel.mTitle != null) {
             String title = mModel.mTitle.trim();
             if (title.length() > 0) {
                 return false;
             }
         }
 
         if (mModel.mLocation != null) {
             String location = mModel.mLocation.trim();
             if (location.length() > 0) {
                 return false;
             }
         }
 
         if (mModel.mDescription != null) {
             String description = mModel.mDescription.trim();
             if (description.length() > 0) {
                 return false;
             }
         }
 
         return true;
     }
 
     @Override
     public void onPause() {
         Activity act = getActivity();
         if (mSaveOnDetach && act != null && !mIsReadOnly && !act.isChangingConfigurations()
                 && mView.prepareForSave()) {
             mOnDone.setDoneCode(Utils.DONE_SAVE);
             mOnDone.run();
         }
         super.onPause();
     }
 
     @Override
     public void onDestroy() {
         if (mView != null) {
             mView.setModel(null);
         }
         if (mModifyDialog != null) {
             mModifyDialog.dismiss();
             mModifyDialog = null;
         }
 
         super.onDestroy();
     }
 
     @Override
     public void eventsChanged() {
         // TODO Requery to see if event has changed
     }
 
     @Override
     public void onSaveInstanceState(Bundle outState) {
         mView.prepareForSave();
         outState.putSerializable(BUNDLE_KEY_MODEL, mModel);
         outState.putInt(BUNDLE_KEY_EDIT_STATE, mModification);
         if (mEventBundle == null && mEvent != null) {
             mEventBundle = new EventBundle();
             mEventBundle.id = mEvent.id;
             if (mEvent.startTime != null) {
                 mEventBundle.start = mEvent.startTime.toMillis(true);
             }
             if (mEvent.endTime != null) {
                 mEventBundle.end = mEvent.startTime.toMillis(true);
             }
         }
 
         outState.putSerializable(BUNDLE_KEY_EVENT, mEventBundle);
         outState.putBoolean(BUNDLE_KEY_READ_ONLY, mIsReadOnly);
     }
 
     @Override
     public long getSupportedEventTypes() {
         return EventType.USER_HOME;
     }
 
     @Override
     public void handleEvent(EventInfo event) {
         // It's currently unclear if we want to save the event or not when home
         // is pressed. When creating a new event we shouldn't save since we
         // can't get the id of the new event easily.
         if ((false && event.eventType == EventType.USER_HOME) || (event.eventType == EventType.GO_TO
                 && mSaveOnDetach)) {
             if (mView != null && mView.prepareForSave()) {
                 mOnDone.setDoneCode(Utils.DONE_SAVE);
                 mOnDone.run();
             }
         }
     }
 
     private static class EventBundle implements Serializable {
         long id = -1;
         long start = -1;
         long end = -1;
     }
 }
